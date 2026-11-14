package com.brendigo.brojko;

import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Restoring a local SAF backup must not revive stale Activity UI state. */
@RunWith(AndroidJUnit4.class)
public final class BackupRestoreRecreationTest {
    @Rule public ActivityScenarioRule<MainActivity> scenario =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void postImportStateBundleNeverContainsStaleGame(){
        scenario.getScenario().onActivity(activity->{
            boolean originalFlag=activity.skipPersistOnPauseAfterImport;
            String originalPage=activity.page;
            try{
                activity.page="game";
                activity.skipPersistOnPauseAfterImport=true;
                Bundle state=new Bundle();
                activity.onSaveInstanceState(state);
                assertEquals("home",state.getString("brojko_page"));
                assertFalse("Old board must not be included in restore recreation",
                    state.containsKey("brojko_game"));
            }finally{
                activity.page=originalPage;
                activity.skipPersistOnPauseAfterImport=originalFlag;
            }
        });
    }

    @Test public void realRecreationReloadsImportedBoardNotStaleBoard(){
        final String[] originalSession={null};
        final String[] importedSession={null};
        scenario.getScenario().onActivity(activity->{
            originalSession[0]=activity.prefs.getString("session",null);
            TileGame stale=new TileGame("classic","",181L);
            stale.moves=3;
            stale.score=16;
            activity.game=stale;
            activity.page="backup";
            TileGame imported=new TileGame("relaxed","",182L);
            imported.moves=9;
            imported.score=512;
            importedSession[0]=imported.toJson().toString();
            assertTrue(activity.prefs.edit()
                .putString("session",importedSession[0]).commit());
            activity.skipPersistOnPauseAfterImport=true;
        });
        scenario.getScenario().recreate();
        scenario.getScenario().onActivity(activity->{
            try{
                assertNotNull("Imported session must load on recreation",activity.game);
                assertEquals("relaxed",activity.game.mode);
                assertEquals(importedSession[0],activity.game.toJson().toString());
                assertEquals(importedSession[0],
                    activity.prefs.getString("session",null));
                assertEquals("home",activity.page);
            }finally{
                // Return this isolated instrumented app to its previous state.
                SharedPreferences.Editor editor=activity.prefs.edit();
                if(originalSession[0]==null)editor.remove("session");
                else editor.putString("session",originalSession[0]);
                assertTrue(editor.commit());
                try{
                    activity.game=originalSession[0]==null?null:
                        TileGame.fromJson(new JSONObject(originalSession[0]));
                }catch(Exception invalid){activity.game=null;}
                activity.page="home";
                activity.screen.invalidate();
            }
        });
    }
}
