package com.brendigo.brojko;

import android.content.SharedPreferences;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Pre-v1 migration must not silently round damaged JSON into playable progress. */
@RunWith(AndroidJUnit4.class)
public final class LegacyMigrationValidationTest {
    @Rule public ActivityScenarioRule<MainActivity> scenario =
        new ActivityScenarioRule<>(MainActivity.class);

    private static JSONObject legacy(Object size,Object score,Object firstTile)
            throws Exception {
        JSONArray cells=new JSONArray();
        for(int i=0;i<16;i++)cells.put(i==0?firstTile:0);
        return new JSONObject().put("n",size).put("score",score)
            .put("cells",cells);
    }

    @Test public void rejectsFractionalStringAndOutOfRangeLegacyNumbers()
            throws Exception {
        JSONObject[] malformed={
            legacy(4.5d,128,2),
            legacy("4",128,2),
            legacy(4,128.75d,2),
            legacy(4,"128",2),
            legacy(4,128,2.5d),
            legacy(4,128,"2"),
            legacy(4,128,3),
            legacy(4,128,2147483648L)
        };
        scenario.getScenario().onActivity(activity->{
            SharedPreferences prefs=activity.prefs;
            String previousSession=prefs.getString("session",null);
            String previousBoard=prefs.getString("board",null);
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            try{
                for(JSONObject invalid:malformed){
                    String original=invalid.toString();
                    assertTrue(prefs.edit().remove("session")
                        .putString("board",original).commit());
                    activity.game=null;
                    activity.migrateLegacy();
                    assertNull("Malformed legacy data must not become a game",
                        activity.game);
                    assertNull("No session may be committed for invalid legacy",
                        prefs.getString("session",null));
                    assertEquals("Failed migration keeps the original bytes",
                        original,prefs.getString("board",null));
                }
            }finally{
                SharedPreferences.Editor restore=prefs.edit();
                if(previousSession==null)restore.remove("session");
                else restore.putString("session",previousSession);
                if(previousBoard==null)restore.remove("board");
                else restore.putString("board",previousBoard);
                assertTrue(restore.commit());
                activity.game=previousGame;
                activity.page=previousPage;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void validLegacyBoardMigratesWithExactScoreAndTile()
            throws Exception {
        scenario.getScenario().onActivity(activity->{
            SharedPreferences prefs=activity.prefs;
            String previousSession=prefs.getString("session",null);
            String previousBoard=prefs.getString("board",null);
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            try{
                String original;
                try{original=legacy(4,128,2).toString();}
                catch(Exception error){throw new AssertionError(error);}
                assertTrue(prefs.edit().remove("session")
                    .putString("board",original).commit());
                activity.game=null;
                activity.migrateLegacy();
                assertNotNull("A well-formed legacy board must be migrated",
                    activity.game);
                assertEquals(128,activity.game.score);
                assertEquals(2,activity.game.cells[0]);
                assertEquals(4,activity.game.size);
                assertNull("Original legacy key removed only on success",
                    prefs.getString("board",null));
                assertNotNull(prefs.getString("session",null));
            }finally{
                SharedPreferences.Editor restore=prefs.edit();
                if(previousSession==null)restore.remove("session");
                else restore.putString("session",previousSession);
                if(previousBoard==null)restore.remove("board");
                else restore.putString("board",previousBoard);
                assertTrue(restore.commit());
                activity.game=previousGame;
                activity.page=previousPage;
                activity.screen.invalidate();
            }
        });
    }
}
