package com.brendigo.brojko;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Android UI input barrier regression tests. These tests do not change
 * persisted player data or race the real background archive worker.
 */
@RunWith(AndroidJUnit4.class)
public final class ArchiveTransitionInputGuardTest {
    @Rule public ActivityScenarioRule<MainActivity> scenario=
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void rapidNewAndEndCommandsAreIgnoredWhileSaving(){
        scenario.getScenario().onActivity(activity->{
            TileGame original=activity.game;
            String oldPage=activity.page;
            boolean oldBusy=activity.gameTransitionBusy;
            boolean oldEnabled=activity.screen.isEnabled();
            try{
                TileGame waiting=new TileGame("classic","",9908L);
                waiting.moves=4;
                waiting.score=64;
                activity.game=waiting;
                activity.page="game";
                activity.gameTransitionBusy=true;
                activity.screen.setEnabled(false);
                activity.newGame("relaxed");
                activity.newGame("daily");
                activity.endSession();
                activity.move(TileGame.LEFT);
                activity.undo();
                activity.handleBackNavigation();
                assertSame("No new session while archive barrier is raised",
                    waiting,activity.game);
                assertEquals("game",activity.page);
                assertEquals(64,waiting.score);
                assertEquals(4,waiting.moves);
            }finally{
                activity.gameTransitionBusy=oldBusy;
                activity.game=original;
                activity.page=oldPage;
                activity.screen.setEnabled(oldEnabled);
            }
        });
    }

    @Test public void onPauseStatePersistCannotOverrideAnInflightArchive(){
        scenario.getScenario().onActivity(activity->{
            TileGame original=activity.game;
            String oldPage=activity.page;
            boolean oldBusy=activity.gameTransitionBusy;
            try{
                TileGame placeholder=new TileGame("classic","",7878L);
                placeholder.moves=2;
                activity.game=placeholder;
                activity.gameTransitionBusy=true;
                activity.persist();
                activity.handleBackNavigation();
                assertTrue(activity.gameTransitionBusy);
                assertSame(placeholder,activity.game);
            }finally{
                activity.gameTransitionBusy=oldBusy;
                activity.game=original;
                activity.page=oldPage;
            }
        });
    }
}
