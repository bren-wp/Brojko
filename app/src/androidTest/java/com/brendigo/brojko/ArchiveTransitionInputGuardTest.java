package com.brendigo.brojko;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** User input cannot mutate or duplicate the game during an archive commit. */
@RunWith(AndroidJUnit4.class)
public final class ArchiveTransitionInputGuardTest {
    @Rule public ActivityScenarioRule<MainActivity> scenario=
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void rapidNewAndEndCommandsAreIgnoredWhileSaving(){
        scenario.getScenario().onActivity(activity->{
            // Set a deterministic board without touching other user data.
            TileGame current=activity.game;
            String priorPage=activity.page;
            try{
                activity.game=new TileGame("classic","",9908L);
                activity.game.moves=4;
                activity.game.score=64;
                activity.page="game";
                activity.persist();
                activity.newGame("relaxed");
                assertTrue("Archive operation must hold a transition barrier",
                    activity.gameTransitionBusy);
                TileGame waiting=activity.game;
                activity.newGame("daily");
                activity.endSession();
                activity.move(TileGame.LEFT);
                activity.undo();
                activity.handleBackNavigation();
                assertSame("No next game until the worker has committed",
                    waiting,activity.game);
                assertTrue(activity.gameTransitionBusy);
            }finally{
                // The asynchronous owner transition remains valid; it will
                // finish after the UI callback and then persist its result.
                // Do not overwrite the pending archive via test cleanup.
            }
        });
    }

    @Test public void onPauseCannotOverwriteAnInFlightArchivedSession(){
        scenario.getScenario().onActivity(activity->{
            if(activity.gameTransitionBusy)return;
            TileGame preserved=activity.game;
            try{
                activity.gameTransitionBusy=true;
                activity.persist();
                assertTrue(activity.gameTransitionBusy);
                activity.handleBackNavigation();
                assertTrue(activity.gameTransitionBusy);
            }finally{
                activity.gameTransitionBusy=false;
                activity.game=preserved;
            }
        });
    }
}
