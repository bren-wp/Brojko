package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Device-level guard: a restored daily game with 80 spent moves may never
 * open an 81st move, spawn tiles, inflate score or lose saved progress.
 */
@RunWith(AndroidJUnit4.class)
public final class DailyLimitResumeTest {
    @Rule public ActivityScenarioRule<MainActivity> rule=
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void stalePlayableDailySessionTransitionsToFailureWithoutNewMove(){
        rule.getScenario().onActivity(activity->{
            SharedPreferences originalPrefs=activity.prefs;
            TileGame originalGame=activity.game;
            String originalPage=activity.page;
            SharedPreferences isolated=activity.getSharedPreferences(
                "brojko-daily-expired-regression",Context.MODE_PRIVATE);
            try{
                assertTrue(isolated.edit().clear().commit());
                TileGame source=new TileGame("daily",activity.today(),4155L);
                Arrays.fill(source.cells,0);
                source.cells[0]=2;source.cells[1]=2;
                source.moves=80;source.score=0;source.peak=2;
                TileGame loaded=TileGame.fromJson(
                    new JSONObject(source.toJson().toString()));
                assertNotNull("Legacy saved daily session must remain loadable",loaded);
                activity.prefs=isolated;
                activity.game=loaded;
                activity.page="game";
                activity.persist();
                int[] before=loaded.cells.clone();
                activity.move(TileGame.RIGHT);
                assertEquals("Expired challenge must show loss", "dailyFail",
                    activity.page);
                assertTrue("Expired gameplay cannot continue",loaded.gameOver);
                assertArrayEquals("No 81st swipe or RNG spawn",before,loaded.cells);
                assertEquals(80,loaded.moves);
                assertEquals(0,loaded.score);
                assertEquals("Persisted state still has exactly 80 moves",
                    80,TileGame.fromJson(new JSONObject(
                        isolated.getString("session",""))).moves);
            }catch(Exception problem){
                throw new AssertionError("Expired daily session regression",problem);
            }finally{
                activity.prefs=originalPrefs;
                activity.game=originalGame;
                activity.page=originalPage;
                activity.screen.invalidate();
                assertTrue(isolated.edit().clear().commit());
            }
        });
    }
}
