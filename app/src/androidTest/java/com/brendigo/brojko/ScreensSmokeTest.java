package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/**
 * Android device/emulator QA: launches the actual Activity, renders every view,
 * checks primary navigation and exports screenshots at the emulator's native app-viewport resolution.
 */
@RunWith(AndroidJUnit4.class)
public class ScreensSmokeTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private void capture(MainActivity activity,String name) {
        MainActivity.BoardScreen view=activity.screen;
        // Capture the actual on-device layout without changing its dimensions.
        // Forcing 400x880 while keeping real-device system insets distorted
        // screenshots and hid screen-size problems.
        int width=view.getWidth(),height=view.getHeight();
        assertTrue("View must be laid out by Android",width>0&&height>0);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        File root=activity.getExternalFilesDir(null);
        assertNotNull(root);
        File directory=new File(root,"screenshots");
        assertTrue(directory.isDirectory()||directory.mkdirs());
        File image=new File(directory,name+".png");
        try(FileOutputStream output=new FileOutputStream(image)){
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));
        }catch(Exception error){throw new AssertionError("Could not save screenshot "+name,error);}
        finally{bitmap.recycle();}
    }

    @Test public void missionRewardsAreOnlyGivenOncePerPeriod(){
        rule.getScenario().onActivity(activity->{
            int originalStars=activity.stars,originalFlags=activity.rewardFlags;
            int originalMerges=activity.todayMerges,originalPoints=activity.todayPoints;
            int originalStreak=activity.playDayStreak,originalTile=activity.recordTile;
            int originalBest=activity.best;
            try{
                activity.stars=0;activity.rewardFlags=0;
                activity.todayMerges=20;activity.todayPoints=1000;
                activity.playDayStreak=7;activity.recordTile=1024;activity.best=50000;
                activity.updateRewards();
                assertEquals(1800,activity.stars);
                assertEquals(31,activity.rewardFlags);
                activity.updateRewards();
                assertEquals("Repeated refresh must not duplicate rewards",1800,activity.stars);
                activity.statsDay="2000-01-01";
                activity.ensureDay();
                assertEquals("Daily flags must reset on a new day",28,activity.rewardFlags);
                assertEquals(0,activity.todayMerges);
            }finally{
                activity.stars=originalStars;activity.rewardFlags=originalFlags;
                activity.todayMerges=originalMerges;activity.todayPoints=originalPoints;
                activity.playDayStreak=originalStreak;activity.recordTile=originalTile;
                activity.best=originalBest;
                activity.statsDay=activity.today();
            }
        });
    }

    @Test public void dailyChallengeAwardsOnceAndEnforcesMoveLimit() {
        rule.getScenario().onActivity(activity->{
            TileGame originalGame=activity.game;
            String originalPage=activity.page;
            String oldCompleted=activity.lastDaily,oldRewardDate=activity.dailyRewardDate;
            int oldStars=activity.stars,oldStreak=activity.streak;
            try {
                activity.lastDaily="";
                activity.dailyRewardDate="";
                activity.streak=0;
                activity.stars=0;
                activity.game=new TileGame("daily",activity.today(),123L);
                activity.game.cells=new int[16];
                activity.game.cells[0]=2;
                activity.game.cells[1]=2;
                activity.game.score=2996;
                activity.game.moves=79;
                activity.game.peak=256;
                activity.page="game";
                activity.move(TileGame.LEFT);
                assertEquals("dailyWin",activity.page);
                assertEquals(80,activity.game.moves);
                assertEquals(3000,activity.game.score);
                assertEquals(activity.today(),activity.lastDaily);
                assertEquals(250,activity.stars);
                capture(activity,"daily-success");
                // A second valid daily attempt is allowed, but no second reward.
                activity.game=new TileGame("daily",activity.today(),124L);
                activity.game.cells=new int[16];
                activity.game.cells[0]=2;
                activity.game.cells[1]=2;
                activity.game.score=2996;
                activity.game.moves=79;
                activity.game.peak=256;
                activity.page="game";
                activity.move(TileGame.LEFT);
                assertEquals(250,activity.stars);
                // Failing the 80th move must finish the challenge, not keep
                // showing a misleading active daily task.
                activity.game=new TileGame("daily",activity.today(),125L);
                activity.game.cells=new int[16];
                activity.game.cells[0]=2;
                activity.game.cells[1]=2;
                activity.game.moves=79;
                activity.game.peak=4;
                activity.page="game";
                activity.move(TileGame.LEFT);
                assertEquals("dailyFail",activity.page);
                assertTrue(activity.game.gameOver);
                capture(activity,"daily-failed");
            }finally{
                activity.game=originalGame;activity.page=originalPage;
                activity.lastDaily=oldCompleted;
                activity.dailyRewardDate=oldRewardDate;
                activity.stars=oldStars;activity.streak=oldStreak;
                activity.persist();
            }
        });
    }

    @Test public void compactDisplayKeepsBottomActionsReachable(){
        rule.getScenario().onActivity(activity->{
            activity.page="missions";
            MainActivity.BoardScreen view=activity.screen;
            view.measure(View.MeasureSpec.makeMeasureSpec(360,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY));
            view.layout(0,0,360,600);
            Bitmap bitmap=Bitmap.createBitmap(360,600,Bitmap.Config.ARGB_8888);
            try{
                view.draw(new Canvas(bitmap));
                assertTrue("Compact screens should scroll",view.maxScroll>10);
                long time=android.os.SystemClock.uptimeMillis();
                MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,180,500,0);
                MotionEvent move=MotionEvent.obtain(time,time+30,MotionEvent.ACTION_MOVE,180,260,0);
                MotionEvent up=MotionEvent.obtain(time,time+60,MotionEvent.ACTION_UP,180,260,0);
                view.onTouchEvent(down);view.onTouchEvent(move);view.onTouchEvent(up);
                down.recycle();move.recycle();up.recycle();
                view.draw(new Canvas(bitmap));
                assertTrue("User must be able to reach lower cards",view.scrollOffset>10);
                assertEquals("missions",activity.page);
            }finally{bitmap.recycle();}
        });
    }

    @Test public void themedAndSettingsScreensRenderWithoutCrashing() {
        rule.getScenario().onActivity(activity->{
            int previousTheme=activity.theme;
            try{
                for(int theme=0;theme<3;theme++){
                    activity.theme=theme;
                    activity.page="home";
                    capture(activity,"home-theme-"+theme);
                    activity.page="settings";
                    capture(activity,"settings-theme-"+theme);
                    activity.page="game";
                    if(activity.game==null)activity.newGame("classic");
                    capture(activity,"game-theme-"+theme);
                }
                // Scheduling an inexact local alarm must not require exact alarm
                // permissions, any server, or login credentials.
                AppReminder.configure(activity,true);
                AppReminder.configure(activity,false);
            }finally{
                activity.theme=previousTheme;
                activity.page="home";
                activity.persist();
            }
        });
    }

    @Test public void statsHistoryDetailsAndLongThemePagesAreAccessible(){
        rule.getScenario().onActivity(activity->{
            String backup=activity.prefs.getString("history","[]");
            MainActivity.BoardScreen screen=activity.screen;
            try{
                activity.prefs.edit().putString("history",
                    "[{\"score\":12600,\"moves\":91,\"peak\":1024,"+
                    "\"mode\":\"classic\",\"day\":\"25.10.2026.\"}, "+
                    "{\"score\":6400,\"moves\":70,\"peak\":512,"+
                    "\"mode\":\"relaxed\",\"day\":\"24.10.2026.\"}]").commit();
                activity.page="stats";
                capture(activity,"stats-with-history");
                assertTrue("History list must be scrollable",
                    screen.maxScroll>0);
                activity.selectedHistoryIndex=0;
                activity.page="historyDetail";
                capture(activity,"history-detail");
                activity.page="themes";
                capture(activity,"theme-preview-controls");
                boolean hasBottomSettings=false;
                for(MainActivity.BoardScreen.Tap tap:screen.taps){
                    if("Postavke".equals(tap.label)&&tap.top>=800&&tap.bottom<=880){
                        hasBottomSettings=true;
                        tap.action.run();
                        break;
                    }
                }
                assertTrue("Bottom themes navigation must fit on screen",hasBottomSettings);
                assertEquals("settings",activity.page);
                capture(activity,"settings-via-theme-navigation");
                activity.page="daily";
                capture(activity,"daily-with-reward");
            }finally{
                activity.prefs.edit().putString("history",backup).commit();
                activity.page="home";
            }
        });
    }

    @Test public void shortScreenKeepsBoardLargeAndSwipeFunctional(){
        rule.getScenario().onActivity(activity->{
            activity.newGame("classic");
            MainActivity.BoardScreen view=activity.screen;
            view.measure(View.MeasureSpec.makeMeasureSpec(360,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY));
            view.layout(0,0,360,600);
            Bitmap bitmap=Bitmap.createBitmap(360,600,Bitmap.Config.ARGB_8888);
            try{
                view.draw(new Canvas(bitmap));
                assertTrue("Short game displays must scroll",view.maxScroll>10);
                assertEquals("Board should retain width-based scaling",.9f,view.scale,.02f);
                java.util.Arrays.fill(activity.game.cells,0);
                activity.game.cells[0]=2;
                activity.game.cells[1]=2;
                long time=android.os.SystemClock.uptimeMillis();
                float y=(view.gridY+33)*view.scale+view.offY;
                MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,
                    100*view.scale,y,0);
                MotionEvent up=MotionEvent.obtain(time,time+80,MotionEvent.ACTION_UP,
                    295*view.scale,y,0);
                view.onTouchEvent(down);view.onTouchEvent(up);
                down.recycle();up.recycle();
                assertEquals("Swipe must merge tiles even in scrollable mode",4,
                    activity.game.score);
                int moves=activity.game.moves;
                // Scroll outside the tile board without triggering a tile move.
                time=android.os.SystemClock.uptimeMillis();
                down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,5,520,0);
                MotionEvent move=MotionEvent.obtain(time,time+40,MotionEvent.ACTION_MOVE,
                    5,250,0);
                up=MotionEvent.obtain(time,time+60,MotionEvent.ACTION_UP,5,250,0);
                view.onTouchEvent(down);view.onTouchEvent(move);view.onTouchEvent(up);
                down.recycle();move.recycle();up.recycle();
                view.draw(new Canvas(bitmap));
                assertTrue("Lower buttons remain reachable",view.scrollOffset>10);
                assertEquals("Scrolling must not move tiles",moves,activity.game.moves);
            }finally{bitmap.recycle();}
        });
    }

    @Test public void bundledNunitoFontAndLicenseArePresent() throws Exception {
        rule.getScenario().onActivity(activity->{
            for(String asset:new String[]{"Nunito.ttf","Nunito-OFL.txt"}){
                try(java.io.InputStream stream=activity.getAssets().open(asset)){
                    assertTrue("Bundled asset must not be empty: "+asset,stream.available()>1000);
                }catch(java.io.IOException problem){
                    throw new AssertionError("Missing font resource: "+asset,problem);
                }
            }
        });
    }

    @Test public void renderAllScreensAndGameFlow() {
        rule.getScenario().onActivity(activity->{
            activity.newGame("classic");
            String[] pages={
                "splash","home","game","pause","win","over","confirm","confirmEnd",
                "daily","missions","stats","themes","settings",
                "language","backup","privacy","about","help","dailyWin","dailyFail","historyDetail"
            };
            for(String page:pages){
                activity.page=page;
                capture(activity,page);
            }
            activity.page="home";
            capture(activity,"home-ready");
            float x=200*activity.screen.scale+activity.screen.offX;
            float y=374*activity.screen.scale+activity.screen.offY;
            long time=android.os.SystemClock.uptimeMillis();
            MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,x,y,0);
            MotionEvent up=MotionEvent.obtain(time,time+30,MotionEvent.ACTION_UP,x,y,0);
            activity.screen.onTouchEvent(down);
            activity.screen.onTouchEvent(up);
            down.recycle();up.recycle();
            assertEquals("game",activity.page);
            capture(activity,"game-active");
            assertNotNull(activity.game);
            assertTrue(activity.game.cells.length==16||activity.game.cells.length==25);
        });
    }
}
