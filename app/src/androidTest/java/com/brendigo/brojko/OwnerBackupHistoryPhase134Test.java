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
        MainActivity.validateBackupHistory(new JSONArray(
            "[{\"score\":0,\"moves\":0,\"peak\":0},"+
            "{\"score\":4,\"moves\":1,\"peak\":2,\"won\":true}]"));
        MainActivity.validateBackupHistory(new JSONArray("[]"));
    }

    @Test public void emptyOrUnknownBackupNeverErasesProgress() {
        rule.getScenario().onActivity(activity -> {
            activity.persist();
            Map<String,?> before=new HashMap<>(activity.prefs.getAll());
            try {
                JSONObject original=new JSONObject(
                    new String(activity.getBackupBytes(),StandardCharsets.UTF_8));
                String[] badData={"{}", "{\"not_a_brojko_setting\":true}"};
                for(String raw:badData){
                    JSONObject candidate=new JSONObject(original.toString());
                    candidate.put("data",new JSONObject(raw));
                    boolean rejected=false;
                    try{
                        activity.readBackupBytes(candidate.toString().getBytes(
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
                byte[] full=activity.getBackupBytes();
                JSONObject patched=new JSONObject(
                    new String(full,StandardCharsets.UTF_8));
                int newBest=activity.prefs.getInt("best",0)==19137?19138:19137;
                patched.put("data",new JSONObject().put("best",newBest));
                try {
                    activity.readBackupBytes(patched.toString().getBytes(
                        StandardCharsets.UTF_8));
                    assertEquals("Selected imported preference should update",
                        newBest,activity.prefs.getInt("best",-1));
                    for(Map.Entry<String,?> item:original.entrySet()){
                        if("best".equals(item.getKey()))continue;
                        assertEquals("Sparse import must preserve "+item.getKey(),
                            item.getValue(),activity.prefs.getAll().get(item.getKey()));
                    }
                }finally{
                    activity.readBackupBytes(full);
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
                byte[] source=activity.getBackupBytes();
                for(String history:invalid){
                    JSONObject imported=new JSONObject(
                        new String(source,StandardCharsets.UTF_8));
                    imported.getJSONObject("data").put("history",history);
                    boolean rejected=false;
                    try {
                        activity.readBackupBytes(
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
}
