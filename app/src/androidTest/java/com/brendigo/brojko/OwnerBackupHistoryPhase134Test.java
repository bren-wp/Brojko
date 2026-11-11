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
