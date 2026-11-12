package com.brendigo.brojko;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Persists completed games before the Activity may replace an active session.
 * No Android UI, navigation, network or time dependency lives in this class.
 */
final class LocalHistoryRepository {
    private static final int MAX_HISTORY_ENTRIES=20;
    private static final int MAX_INSPECTED_LEGACY_ENTRIES=200;

    private LocalHistoryRepository() {}

    /**
     * Worker-only atomic archive transaction. A snapshot is never shared as a
     * mutable TileGame: validate it, then commit both previous session and
     * history to disk before the UI is allowed to replace that game.
     *
     * If the same archived snapshot was already persisted (e.g. after a
     * recreation), return success without adding a duplicate history row.
     * Any unexpected concurrent session change is rejected, not overwritten.
     */
    static boolean archiveSnapshot(SharedPreferences prefs,String sourceSession,
            String day,String time){
        try{
            if(sourceSession==null||sourceSession.isEmpty())return false;
            TileGame snapshot=TileGame.fromJson(new JSONObject(sourceSession));
            if(snapshot==null||snapshot.archived||snapshot.moves<=0)return false;

            JSONObject archived=new JSONObject(sourceSession);
            archived.put("archived",true);
            String archivedSession=archived.toString();
            String persistedSession=prefs.getString("session",null);
            if(archivedSession.equals(persistedSession)){
                // commit() can report failure after SharedPreferences already
                // updated its in-memory map. A retry must NEVER treat that
                // memory-only state as proof the history reached disk.
                // Recommit the exact same row+session, never append twice.
                JSONArray existing=new JSONArray(prefs.getString("history","[]"));
                JSONObject latest=existing.optJSONObject(0);
                if(latest==null||latest.optInt("score",-1)!=snapshot.score||
                   latest.optInt("moves",-1)!=snapshot.moves||
                   latest.optInt("peak",-1)!=snapshot.maxTile()||
                   !snapshot.mode.equals(latest.optString("mode"))||
                   latest.optBoolean("won")!=snapshot.reached2048)
                    return false;
                return prefs.edit().putString("history",existing.toString())
                    .putString("session",archivedSession)
                    .putInt("lastScore",snapshot.score).commit();
            }
            // Never archive a stale/foreign game snapshot or overwrite one
            // restored from a backup while this task was waiting in the queue.
            if(!sourceSession.equals(persistedSession))return false;

            JSONObject row=new JSONObject();
            row.put("score",snapshot.score).put("moves",snapshot.moves)
                .put("peak",snapshot.maxTile()).put("mode",snapshot.mode)
                .put("day",day).put("time",time)
                .put("won",snapshot.reached2048);

            JSONArray previous;
            try{previous=new JSONArray(prefs.getString("history","[]"));}
            catch(Exception invalidHistory){previous=new JSONArray();}
            JSONArray recent=prepend(row,previous);
            // SharedPreferences writes all entries in one atomic file update.
            // Crucial: a crash between archive and the next newGame() cannot
            // produce an archived history row with an unarchived old session.
            return prefs.edit().putString("history",recent.toString())
                .putString("session",archivedSession)
                .putInt("lastScore",snapshot.score).commit();
        }catch(Exception error){return false;}
    }

    /**
     * An archived session found during a fresh process start means that the
     * archive already committed, but the follow-up new game may not have
     * been persisted before the previous process died.
     */
    static boolean hasCommittedArchive(SharedPreferences prefs){
        try{
            String source=prefs.getString("session","");
            if(source==null||source.isEmpty())return false;
            TileGame previous=TileGame.fromJson(new JSONObject(source));
            return previous!=null&&previous.archived;
        }catch(Exception invalid){return false;}
    }

    /** Preserves the old ordering, 20-row cap and corrupted-row tolerance. */
    static JSONArray prepend(JSONObject latest,JSONArray previous){
        JSONArray result=new JSONArray();
        result.put(latest);
        for(int i=0;i<Math.min(MAX_INSPECTED_LEGACY_ENTRIES,previous.length())&&
            result.length()<MAX_HISTORY_ENTRIES;i++){
            JSONObject prior=previous.optJSONObject(i);
            if(prior!=null)result.put(prior);
        }
        return result;
    }
}
