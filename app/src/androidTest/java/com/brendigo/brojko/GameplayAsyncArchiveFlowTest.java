package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Gameplay transitions on the real Canvas and real SharedPreferences.
 * Unlike pre-worker smoke tests, checks run AFTER the asynchronous archive
 * confirms disk persistence. This class never waits on Android's main thread.
 */
@RunWith(AndroidJUnit4.class)
public final class GameplayAsyncArchiveFlowTest {
    @Rule public final ActivityScenarioRule<MainActivity> rule=
        new ActivityScenarioRule<>(MainActivity.class);

    private static final class Fixture {
        final SharedPreferences originalPrefs;
        final SharedPreferences staging;
        final TileGame originalGame;
        final String originalPage,originalMode,originalDay,originalStatsDay,
            originalWeekStart,originalLastDay,originalRewardDate;
        final int originalPlays,originalLastScore,originalStars,originalFlags,
            originalStreak,originalPlayStreak,originalRecord,originalWeekPoints,
            originalTodayMerges,originalTodayPoints;
        final boolean originalUndo,originalAward;

        Fixture(MainActivity a,String testName) {
            originalPrefs=a.prefs;
            originalGame=a.game;
            originalPage=a.page;
            originalMode=a.pendingMode;
            originalDay=a.lastDaily;
            originalStatsDay=a.statsDay;
            originalWeekStart=a.weekStart;
            originalLastDay=a.lastPlayDay;
            originalRewardDate=a.dailyRewardDate;
            originalPlays=a.plays;
            originalLastScore=a.lastScore;
            originalStars=a.stars;
            originalFlags=a.rewardFlags;
            originalStreak=a.streak;
            originalPlayStreak=a.playDayStreak;
            originalRecord=a.recordTile;
            originalWeekPoints=a.weekPoints;
            originalTodayMerges=a.todayMerges;
            originalTodayPoints=a.todayPoints;
            originalUndo=a.undoUsedThisGame;
            originalAward=a.dailyBonusGrantedThisAttempt;
            staging=a.getSharedPreferences("brojko-async-flow-test-"+testName,
                Context.MODE_PRIVATE);
            assertTrue(staging.edit().clear().commit());
            a.prefs=staging;
        }

        void stage(MainActivity a,String mode,String page,int score,
                int moves,int peak,boolean gameOver,long seed){
            TileGame candidate=new TileGame(mode,
                "daily".equals(mode)?a.today():"",seed);
            candidate.score=score;
            candidate.moves=moves;
            // Archive history stores maxTile() from the ACTUAL board, not
            // the lifetime peak. Make the scenario fixture internally
            // consistent instead of weakening gameplay assertions.
            if(peak>0)candidate.cells[0]=peak;
            candidate.peak=peak;
            candidate.gameOver=gameOver;
            a.game=candidate;
            a.page=page;
            // The previous game MUST have a matching on-disk session before
            // the worker may atomically archive it. No fake in-memory-only
            // session is allowed by the production repository.
            a.persist();
            assertEquals("Fixture must expose its requested maximum tile",
                peak,candidate.maxTile());
            assertEquals(candidate.toJson().toString(),
                staging.getString("session",null));
        }

        void restore(MainActivity a){
            a.prefs=originalPrefs;
            a.game=originalGame;
            a.page=originalPage;
            a.pendingMode=originalMode;
            a.lastDaily=originalDay;
            a.statsDay=originalStatsDay;
            a.weekStart=originalWeekStart;
            a.lastPlayDay=originalLastDay;
            a.dailyRewardDate=originalRewardDate;
            a.plays=originalPlays;
            a.lastScore=originalLastScore;
            a.stars=originalStars;
            a.rewardFlags=originalFlags;
            a.streak=originalStreak;
            a.playDayStreak=originalPlayStreak;
            a.recordTile=originalRecord;
            a.weekPoints=originalWeekPoints;
            a.todayMerges=originalTodayMerges;
            a.todayPoints=originalTodayPoints;
            a.undoUsedThisGame=originalUndo;
            a.dailyBonusGrantedThisAttempt=originalAward;
            a.screen.setEnabled(true);
            a.updateBars();
            a.screen.invalidate();
            // Preferences used for regression fixtures are isolated from the
            // player's real progress and may safely be destroyed.
            assertTrue(staging.edit().clear().commit());
        }

        JSONArray history() throws Exception {
            return new JSONArray(staging.getString("history","[]"));
        }
    }

    private Fixture fixture;

    private void begin(String testName,String mode,String page,
            int score,int moves,int peak,boolean gameOver,long seed){
        rule.getScenario().onActivity(a->{
            fixture=new Fixture(a,testName);
            fixture.stage(a,mode,page,score,moves,peak,gameOver,seed);
        });
    }

    private static void tap(MainActivity activity,int index,String expectedLabel){
        MainActivity.BoardScreen view=activity.new BoardScreen(activity);
        view.measure(android.view.View.MeasureSpec.makeMeasureSpec(941,
                android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(1672,
                android.view.View.MeasureSpec.EXACTLY));
        view.layout(0,0,941,1672);
        Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
        try{
            view.draw(new Canvas(bitmap));
            assertTrue("Missing Canvas control "+expectedLabel,index<view.taps.size());
            assertEquals(expectedLabel,view.taps.get(index).label);
            view.taps.get(index).action.run();
        }finally{bitmap.recycle();}
    }

    private void awaitTransaction(){
        long deadline=SystemClock.uptimeMillis()+20000L;
        while(SystemClock.uptimeMillis()<deadline){
            AtomicBoolean complete=new AtomicBoolean();
            rule.getScenario().onActivity(a->
                complete.set(!a.gameTransitionBusy));
            if(complete.get())return;
            SystemClock.sleep(35L);
        }
        fail("Archive worker did not finish or return to Android UI within 20s");
    }

    private void cleanup(){
        if(fixture!=null){
            rule.getScenario().onActivity(a->fixture.restore(a));
            fixture=null;
        }
    }

    @Test public void gameOverNewGameArchivesExactlyOnceAndBackKeepsSession()
            throws Exception{
        begin("gameOver","classic","over",6144,34,512,true,12041L);
        try{
            rule.getScenario().onActivity(a->{
                tap(a,1,"Povratak");
                assertEquals("home",a.page);
                assertEquals(6144,a.game.score);
                a.page="over";
                tap(a,0,"Nova igra");
                assertTrue("New game waits for the durable archive",
                    a.gameTransitionBusy);
                assertEquals("over",a.page);
                assertEquals(6144,a.game.score);
            });
            awaitTransaction();
            rule.getScenario().onActivity(a->{
                assertEquals("game",a.page);
                assertNotNull(a.game);
                assertEquals("classic",a.game.mode);
                try{
                    JSONArray history=fixture.history();
                    assertEquals(1,history.length());
                    assertEquals(6144,history.getJSONObject(0).getInt("score"));
                    assertEquals(34,history.getJSONObject(0).getInt("moves"));
                    assertEquals(512,history.getJSONObject(0).getInt("peak"));
                }catch(Exception e){throw new AssertionError(e);}
            });
        }finally{cleanup();}
    }

    @Test public void confirmNewGameArchivesBeforeSwitchingToRequestedMode()
            throws Exception{
        begin("confirmNew","classic","game",4096,15,8,false,90127L);
        try{
            rule.getScenario().onActivity(a->{
                a.requestNew("relaxed");
                assertEquals("confirm",a.page);
                assertEquals("relaxed",a.pendingMode);
                tap(a,0,"Započni novo");
                assertTrue(a.gameTransitionBusy);
                assertEquals("confirm",a.page);
            });
            awaitTransaction();
            rule.getScenario().onActivity(a->{
                assertEquals("game",a.page);
                assertEquals("relaxed",a.game.mode);
                assertEquals(25,a.game.cells.length);
                try{
                    JSONArray history=fixture.history();
                    assertEquals(1,history.length());
                    assertEquals(4096,history.getJSONObject(0).getInt("score"));
                    assertEquals(15,history.getJSONObject(0).getInt("moves"));
                }catch(Exception e){throw new AssertionError(e);}
            });
        }finally{cleanup();}
    }

    @Test public void confirmationCancelIsNonDestructiveAndConfirmedEndArchives()
            throws Exception{
        begin("confirmEnd","classic","confirm",3072,12,8,false,71484L);
        try{
            rule.getScenario().onActivity(a->{
                tap(a,1,"Odustani");
                assertEquals("game",a.page);
                assertEquals(3072,a.game.score);
                a.page="confirmEnd";
                tap(a,1,"Nastavi igrati");
                assertEquals("game",a.page);
                assertEquals("[]",fixture.staging.getString("history","[]"));
                a.page="confirmEnd";
                tap(a,0,"Završi i spremi");
                assertTrue(a.gameTransitionBusy);
                assertEquals("confirmEnd",a.page);
            });
            awaitTransaction();
            rule.getScenario().onActivity(a->{
                assertEquals("home",a.page);
                assertNull(a.game);
                try{
                    JSONArray history=fixture.history();
                    assertEquals(1,history.length());
                    assertEquals(3072,history.getJSONObject(0).getInt("score"));
                    assertEquals(12,history.getJSONObject(0).getInt("moves"));
                }catch(Exception e){throw new AssertionError(e);}
            });
        }finally{cleanup();}
    }

    @Test public void dailyResultButtonsPreserveRewardDateAndArchiveRealScore()
            throws Exception{
        begin("dailyWin","daily","dailyWin",3024,78,256,false,12552L);
        try{
            rule.getScenario().onActivity(a->{
                a.dailyRewardDate=a.today();
                a.dailyBonusGrantedThisAttempt=true;
                a.stars=500;
                a.persist();
                tap(a,1,"Izbornik");
                assertEquals("home",a.page);
                assertEquals(3024,a.game.score);
                a.page="dailyWin";
                tap(a,0,"Igraj ponovno");
                assertTrue(a.gameTransitionBusy);
                assertEquals("dailyWin",a.page);
            });
            awaitTransaction();
            rule.getScenario().onActivity(a->{
                assertEquals("game",a.page);
                assertEquals("daily",a.game.mode);
                assertEquals(a.today(),a.dailyRewardDate);
                assertFalse(a.dailyBonusGrantedThisAttempt);
                try{
                    JSONArray history=fixture.history();
                    assertEquals(1,history.length());
                    assertEquals(3024,history.getJSONObject(0).getInt("score"));
                    assertEquals(78,history.getJSONObject(0).getInt("moves"));
                }catch(Exception e){throw new AssertionError(e);}
            });
        }finally{cleanup();}
    }

    @Test public void failedDailyReplayCannotClaimSuccessReward()
            throws Exception{
        begin("dailyFail","daily","dailyFail",1728,80,64,true,56044L);
        try{
            rule.getScenario().onActivity(a->{
                a.dailyRewardDate="";
                a.dailyBonusGrantedThisAttempt=false;
                a.persist();
                tap(a,0,"Igraj ponovno");
                assertTrue(a.gameTransitionBusy);
                assertEquals("dailyFail",a.page);
            });
            awaitTransaction();
            rule.getScenario().onActivity(a->{
                assertEquals("game",a.page);
                assertEquals("daily",a.game.mode);
                assertEquals("",a.dailyRewardDate);
                assertFalse(a.dailyBonusGrantedThisAttempt);
                try{
                    JSONArray history=fixture.history();
                    assertEquals(1,history.length());
                    assertEquals(1728,history.getJSONObject(0).getInt("score"));
                    assertEquals(80,history.getJSONObject(0).getInt("moves"));
                }catch(Exception e){throw new AssertionError(e);}
            });
        }finally{cleanup();}
    }
}
