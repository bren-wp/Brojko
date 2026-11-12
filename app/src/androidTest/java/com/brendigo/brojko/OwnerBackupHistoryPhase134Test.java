package com.brendigo.brojko;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Phase 134: malformed imported history must never overwrite real progress. */
@RunWith(AndroidJUnit4.class)
public class OwnerBackupHistoryPhase134Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void validLegacyHistoryRowsMayOmitOptionalProperties() throws Exception {
        BackupTestBridge.validateHistory(new JSONArray(
            "[{\"score\":0,\"moves\":0,\"peak\":0},"+
            "{\"score\":4,\"moves\":1,\"peak\":2,\"won\":true}]"));
        BackupTestBridge.validateHistory(new JSONArray("[]"));
    }

    @Test public void emptyOrUnknownBackupNeverErasesProgress() {
        rule.getScenario().onActivity(activity -> {
            activity.persist();
            Map<String,?> before=new HashMap<>(activity.prefs.getAll());
            try {
                JSONObject original=new JSONObject(
                    new String(BackupTestBridge.export(activity),StandardCharsets.UTF_8));
                String[] badData={"{}", "{\"not_a_brojko_setting\":true}"};
                for(String raw:badData){
                    JSONObject candidate=new JSONObject(original.toString());
                    candidate.put("data",new JSONObject(raw));
                    boolean rejected=false;
                    try{
                        BackupTestBridge.restore(activity,candidate.toString().getBytes(
                            StandardCharsets.UTF_8));
                    }catch(IllegalArgumentException expected){
                        rejected=true;
                    }
                    assertTrue("Empty/unknown backup must be rejected",rejected);
                    assertEquals("No user progress can be removed by empty data",
                        before,activity.prefs.getAll());
                }
            }catch(Exception e){
                throw new AssertionError("Cannot validate empty backup protection",e);
            }
        });
    }

    @Test public void sparseLegacyBackupKeepsUnrelatedUserProgress() {
        rule.getScenario().onActivity(activity -> {
            activity.persist();
            Map<String,?> original=new HashMap<>(activity.prefs.getAll());
            try{
                byte[] full=BackupTestBridge.export(activity);
                JSONObject patched=new JSONObject(
                    new String(full,StandardCharsets.UTF_8));
                int newBest=activity.prefs.getInt("best",0)==19137?19138:19137;
                patched.put("data",new JSONObject().put("best",newBest));
                try {
                    BackupTestBridge.restore(activity,patched.toString().getBytes(
                        StandardCharsets.UTF_8));
                    assertEquals("Selected imported preference should update",
                        newBest,activity.prefs.getInt("best",-1));
                    for(Map.Entry<String,?> item:original.entrySet()){
                        if("best".equals(item.getKey()))continue;
                        assertEquals("Sparse import must preserve "+item.getKey(),
                            item.getValue(),activity.prefs.getAll().get(item.getKey()));
                    }
                }finally{
                    BackupTestBridge.restore(activity,full);
                }
            }catch(Exception e){
                throw new AssertionError("Sparse legacy backup lost progress",e);
            }
        });
    }

    @Test public void malformedHistoryDoesNotReplaceSavedProgress() {
        rule.getScenario().onActivity(activity -> {
            activity.persist();
            Map<String,?> before=new HashMap<>(activity.prefs.getAll());
            final String[] invalid={
                "[null]",
                "[42]",
                "[\"not-a-game\"]",
                "[{\"score\":-1}]",
                "[{\"score\":\"9000\"}]",
                "[{\"score\":1.5}]",
                "[{\"moves\":-3}]",
                "[{\"peak\":3}]",
                "[{\"won\":\"true\"}]",
                "[{\"day\":23}]"
            };
            try {
                byte[] source=BackupTestBridge.export(activity);
                for(String history:invalid){
                    JSONObject imported=new JSONObject(
                        new String(source,StandardCharsets.UTF_8));
                    imported.getJSONObject("data").put("history",history);
                    boolean rejected=false;
                    try {
                        BackupTestBridge.restore(activity,
                            imported.toString().getBytes(StandardCharsets.UTF_8));
                    }catch(Exception invalidBackup){
                        rejected=true;
                    }
                    assertTrue("Reject malformed imported history: "+history,rejected);
                    assertEquals("Do not clear progress on failed import",
                        before,activity.prefs.getAll());
                }
            }catch(Exception ex){
                throw new AssertionError("History backup QA failed",ex);
            }
        });
    }
    @Test public void eightKeyLegacyRestoreNeverErasesSavedSessionOrHistory(){
        rule.getScenario().onActivity(activity -> {
            activity.persist();
            try{
                byte[] original=BackupTestBridge.export(activity);
                try{
                    String session=new TileGame("classic","",94821L).toJson().toString();
                    String history="[{\"score\":8,\"moves\":2,\"peak\":4}]";
                    assertTrue(activity.prefs.edit()
                        .putString("session",session).putString("history",history).commit());
                    JSONObject exported=new JSONObject(new String(
                        BackupTestBridge.export(activity),StandardCharsets.UTF_8));
                    JSONObject partial=new JSONObject();
                    partial.put("best",120).put("games",2).put("total",8);
                    partial.put("theme",0).put("style",0);
                    partial.put("sound",true).put("vibrate",false);
                    partial.put("notifications",false);
                    // Old backups have no manifest, even if eight typical keys exist.
                    JSONObject legacy=new JSONObject(exported.toString());
                    legacy.put("data",partial);
                    legacy.remove("scope");
                    legacy.remove("includedKeys");
                    BackupTestBridge.restore(activity,legacy.toString().getBytes(StandardCharsets.UTF_8));
                    assertEquals("Legacy restore must keep the running session",
                        session,activity.prefs.getString("session",null));
                    assertEquals("Legacy restore must keep past games",
                        history,activity.prefs.getString("history",null));
                    // A damaged modern full export with a stale manifest
                    // must also be applied as a non-destructive patch.
                    exported.put("data",partial);
                    BackupTestBridge.restore(activity,exported.toString().getBytes(StandardCharsets.UTF_8));
                    assertEquals(session,activity.prefs.getString("session",null));
                    assertEquals(history,activity.prefs.getString("history",null));
                }finally{
                    BackupTestBridge.restore(activity,original);
                }
            }catch(Exception e){
                throw new AssertionError("Sparse backup removed user progress",e);
            }
        });
    }

    @Test public void explicitFullBackupRemovesOnlyOmittedKnownFields(){
        rule.getScenario().onActivity(activity -> {
            activity.persist();
            try{
                byte[] original=BackupTestBridge.export(activity);
                try{
                    JSONObject exported=new JSONObject(new String(original,
                        StandardCharsets.UTF_8));
                    JSONObject data=exported.getJSONObject("data");
                    data.remove("session");
                    data.remove("history");
                    JSONArray included=new JSONArray();
                    java.util.Iterator<String> keys=data.keys();
                    while(keys.hasNext())included.put(keys.next());
                    exported.put("includedKeys",included);
                    assertTrue(activity.prefs.edit()
                        .putString("session",new TileGame("classic","",82L).toJson().toString())
                        .putString("history","[]")
                        .putString("futureSetting","keep-me").commit());
                    BackupTestBridge.restore(activity,exported.toString().getBytes(StandardCharsets.UTF_8));
                    assertFalse("Full restore must remove omitted stored session",
                        activity.prefs.contains("session"));
                    assertFalse("Full restore must remove omitted game history",
                        activity.prefs.contains("history"));
                    assertEquals("Unrelated settings must not be deleted",
                        "keep-me",activity.prefs.getString("futureSetting",null));
                }finally{
                    activity.prefs.edit().remove("futureSetting").commit();
                    BackupTestBridge.restore(activity,original);
                }
            }catch(Exception e){
                throw new AssertionError("Explicit full backup restore failed",e);
            }
        });
    }


}
