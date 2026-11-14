package com.brendigo.brojko;

import android.content.SharedPreferences;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Local-only backup data handling. No Activity, UI, network, or filesystem access.
 * All validation finishes before the atomic SharedPreferences commit.
 *
 * Legacy backups remain additive; an explicit, consistent scope manifest is
 * required before known, omitted user-state keys may be removed.
 */
final class LocalBackupRepository {
    private LocalBackupRepository() {}

    private static final String BACKUP_FORMAT="brendigo.brojko.backup.v1";
    private static final String[] BACKUP_KEYS={
        "best","games","total","wins","tile","totalMerges","streak",
        "stars","rewardFlags","lastScore",
        "lastDaily","dailyRewardDate","statsDay","todayMerges","todayPoints",
        "weekStart","weekPoints","undoUsedThisGame","playDayStreak",
        "lastPlayDay","theme","style","animation","sound","vibrate","notifications",
        "session","history","historyRecovery"
    };
    static byte[] export(SharedPreferences prefs,String created,String packageName) throws Exception{
        JSONObject root=new JSONObject();
        root.put("format",BACKUP_FORMAT);
        root.put("created",created);
        root.put("package",packageName);
        JSONObject data=new JSONObject();
        JSONArray includedKeys=new JSONArray();
        java.util.Map<String,?> stored=prefs.getAll();
        for(String key:BACKUP_KEYS){
            Object value=stored.get(key);
            if(value!=null){
                data.put(key,value);
                includedKeys.put(key);
            }
        }
        // Full snapshots have an explicit key manifest. Older backups are
        // imported as non-destructive patches, never as implicit replacements.
        root.put("scope","full");
        root.put("includedKeys",includedKeys);
        root.put("data",data);
        return root.toString(2).getBytes(StandardCharsets.UTF_8);
    }
    // Validate archived history before touching saved preferences. A JSON
    // array can be syntactically valid yet contain numbers, nulls, malformed
    // rows or impossible tile values. Never import those as game history.
    // Older legitimate rows may omit optional fields such as "won" and "time".
    static void validateHistory(JSONArray history){
        if(history.length()>200)
            throw new IllegalArgumentException("Oštećena povijest partija.");
        for(int i=0;i<history.length();i++){
            JSONObject row=history.optJSONObject(i);
            if(row==null)
                throw new IllegalArgumentException("Neispravan zapis povijesti.");
            for(String key:new String[]{"score","moves","peak"}){
                if(!row.has(key))continue;
                Object raw=row.opt(key);
                if(!(raw instanceof Number))
                    throw new IllegalArgumentException("Neispravan broj u povijesti.");
                double value=((Number)raw).doubleValue();
                if(value<0||value>Integer.MAX_VALUE||value!=Math.floor(value))
                    throw new IllegalArgumentException("Neispravna vrijednost povijesti.");
                if("peak".equals(key)&&value!=0){
                    long tile=(long)value;
                    if(tile<2||(tile&(tile-1))!=0)
                        throw new IllegalArgumentException("Neispravna pločica u povijesti.");
                }
            }
            for(String key:new String[]{"mode","day","time"}){
                if(!row.has(key))continue;
                Object value=row.opt(key);
                if(!(value instanceof String)||((String)value).length()>64)
                    throw new IllegalArgumentException("Neispravan tekst u povijesti.");
            }
            if(row.has("won")&&!(row.opt("won") instanceof Boolean))
                throw new IllegalArgumentException("Neispravna pobjeda u povijesti.");
        }
    }
    /** Immutable transport of validated data across worker/main threads. */
    static final class RestorePlan {
        final JSONObject data;
        final boolean completeSnapshot;
        private RestorePlan(JSONObject data,boolean completeSnapshot){
            this.data=data;
            this.completeSnapshot=completeSnapshot;
        }
    }

    /**
     * Bound parser recursion before passing untrusted SAF bytes to org.json.
     * JSONObject/JSONArray parsing can recursively walk a syntactically valid
     * 512-kB document with thousands of nested arrays. Strings are scanned
     * verbatim (respecting escaped quotes), including embedded session JSON.
     * This is a depth guard, not a substitute for the JSON syntax parser.
     */
    static void rejectExcessiveNesting(String json){
        int depth=0;
        boolean inString=false,escaped=false;
        for(int i=0;i<json.length();i++){
            char ch=json.charAt(i);
            if(inString){
                if(escaped)escaped=false;
                else if(ch=='\\')escaped=true;
                else if(ch=='"')inString=false;
            }else if(ch=='"'){
                inString=true;
            }else if(ch=='{'||ch=='['){
                if(++depth>64)
                    throw new IllegalArgumentException("Previše ugniježđenih JSON podataka.");
            }else if(ch=='}'||ch==']'){
                if(--depth<0)
                    throw new IllegalArgumentException("Neispravna JSON struktura.");
            }
        }
        if(inString||depth!=0)
            throw new IllegalArgumentException("Neispravna JSON struktura.");
    }

    /**
     * Validate a SAF document on the I/O worker. No SharedPreferences access
     * or user-state mutation occurs until apply() is called on the main thread.
     */
    static RestorePlan validate(String packageName,byte[] bytes) throws Exception{
        if(bytes==null||bytes.length>512000)
            throw new IllegalArgumentException("Datoteka je prevelika.");
        String document;
        try{
            document=StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString();
        }catch(CharacterCodingException invalid){
            throw new IllegalArgumentException("Datoteka nije valjani UTF-8.",invalid);
        }
        rejectExcessiveNesting(document);
        JSONObject root=new JSONObject(document);
        if(!BACKUP_FORMAT.equals(root.optString("format")))
            throw new IllegalArgumentException("Nepodržani oblik sigurnosne kopije.");
        // Match the package identity already written to every exported
        // backup. Never import unrelated or tampered app state.
        if(!packageName.equals(root.optString("package","")))
            throw new IllegalArgumentException("Sigurnosna kopija nije za Brojko.");
        JSONObject data=root.getJSONObject("data");
        // Legacy V1 backups have no explicit completeness marker. Even when
        // they contain common settings, they may omit a saved game or history.
        // Never infer permission to delete progress from eight arbitrary keys.
        int knownKeys=0;
        for(String key:BACKUP_KEYS)if(data.has(key))knownKeys++;
        if(knownKeys==0)
            throw new IllegalArgumentException("Sigurnosna kopija nema podatke za vraćanje.");
        boolean completeSnapshot=false;
        if("full".equals(root.optString("scope",""))){
            JSONArray declared=root.optJSONArray("includedKeys");
            if(declared!=null && declared.length()==knownKeys){
                java.util.Set<String> included=new java.util.HashSet<>();
                boolean valid=true;
                for(int i=0;i<declared.length();i++){
                    Object candidate=declared.get(i);
                    if(!(candidate instanceof String)||
                        !included.add((String)candidate)){
                        valid=false;
                        break;
                    }
                }
                if(valid){
                    for(String key:BACKUP_KEYS){
                        if(data.has(key)!=included.contains(key)){
                            valid=false;
                            break;
                        }
                    }
                }
                completeSnapshot=valid && included.size()==knownKeys;
            }
        }
        if(data.has("session")){
            String session=data.getString("session");
            rejectExcessiveNesting(session);
            TileGame restored=TileGame.fromJson(new JSONObject(session));
            if(restored==null)throw new IllegalArgumentException("Oštećena spremljena partija.");
        }
        if(data.has("history")){
            String history=data.getString("history");
            rejectExcessiveNesting(history);
            validateHistory(new JSONArray(history));
        }
        // Validate each key against its actual SharedPreferences storage
        // type before clearing any existing settings. A backup with e.g.
        // "best":"100" previously committed successfully, then getInt()
        // crashed on the next launch. Never coerce untrusted JSON values.
        for(String key:BACKUP_KEYS){
            if(!data.has(key))continue;
            Object value=data.get(key);
            boolean booleanKey=key.equals("sound")||key.equals("vibrate")||
                key.equals("notifications")||key.equals("undoUsedThisGame");
            boolean stringKey=key.equals("session")||key.equals("history")||
                key.equals("historyRecovery")||
                key.equals("lastDaily")||key.equals("dailyRewardDate")||
                key.equals("statsDay")||key.equals("weekStart")||
                key.equals("lastPlayDay");
            if(booleanKey){
                if(!(value instanceof Boolean))
                    throw new IllegalArgumentException("Nevaljana logička postavka.");
            }else if(stringKey){
                if(!(value instanceof String))
                    throw new IllegalArgumentException("Nevaljani tekstualni podatci.");
                if(!key.equals("session")&&!key.equals("history")&&
                    !key.equals("historyRecovery")&&((String)value).length()>20)
                    throw new IllegalArgumentException("Nevaljani datum.");
            }else{
                if(!(value instanceof Integer)||((Integer)value)<0)
                    throw new IllegalArgumentException("Nevaljani brojčani podatci.");
                if((key.equals("theme")||key.equals("style")||
                    key.equals("animation"))&&((Integer)value)>2)
                    throw new IllegalArgumentException("Nevaljane postavke.");
            }
        }
        return new RestorePlan(data,completeSnapshot);
    }

    /** One atomic durable write; applies a fully validated document only. */
    static void apply(SharedPreferences prefs,RestorePlan plan) throws Exception{
        if(plan==null)throw new IllegalArgumentException("Neispravna sigurnosna kopija.");
        JSONObject data=plan.data;
        boolean completeSnapshot=plan.completeSnapshot;
        SharedPreferences.Editor editor=prefs.edit();
        // Only an explicit, internally consistent full snapshot can remove
        // omitted Brojko backup fields. Never clear unrelated/future settings.
        // Legacy and altered partial exports remain non-destructive patches.
        if(completeSnapshot){
            for(String key:BACKUP_KEYS){
                // A full export made before recovery support cannot authorize
                // deletion of later quarantined data. Retain that evidence.
                if(!data.has(key)&&!key.equals("historyRecovery"))editor.remove(key);
            }
        }
        for(String key:BACKUP_KEYS){
            if(!data.has(key))continue;
            Object value=data.get(key);
            if(value instanceof Integer)editor.putInt(key,(Integer)value);
            else if(value instanceof Boolean)editor.putBoolean(key,(Boolean)value);
            else editor.putString(key,(String)value);
        }
        if(!editor.commit())throw new IllegalStateException("Neuspješno spremanje.");
    }

}
