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

    // Renders the same production BoardScreen on an independent, non-attached
    // Android View. The Activity window cannot resize the golden viewport.
    private void captureReference(MainActivity activity,
                                  MainActivity.BoardScreen view,String name){
        assertEquals(941,view.getWidth());
        assertEquals(1672,view.getHeight());
        Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
        try{
            view.draw(new Canvas(bitmap));
            File directory=new File(activity.getExternalFilesDir(null),"screenshots");
            assertTrue(directory.isDirectory()||directory.mkdirs());
            try(FileOutputStream output=new FileOutputStream(
                    new File(directory,name+".png"))){
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));
            }catch(java.io.IOException failure){
                throw new AssertionError("Cannot save reference capture "+name,failure);
            }
        }finally{bitmap.recycle();}
    }

    @Test public void missionRewardsAreOnlyGivenOncePerPeriod(){
        rule.getScenario().onActivity(activity->{
            int originalStars=activity.stars,originalFlags=activity.rewardFlags;
            int originalMerges=activity.todayMerges,originalPoints=activity.todayPoints;
            int originalStreak=activity.playDayStreak,originalTile=activity.recordTile;
            int originalBest=activity.best;
            try{
                activity.stars=0;activity.rewardFlags=0;
                TileGame savedGame=activity.game;
                int savedWeekPoints=activity.weekPoints;String savedWeek=activity.weekStart;
                boolean savedUndo=activity.undoUsedThisGame;
                try{
                    activity.todayMerges=20;activity.todayPoints=1000;
                    activity.weekStart=activity.currentWeekStart();activity.weekPoints=10000;
                    activity.playDayStreak=7;activity.recordTile=1024;activity.best=50000;
                    TileGame eligible=new TileGame("classic","",201L);
                    eligible.peak=512;activity.game=eligible;activity.undoUsedThisGame=false;
                    activity.updateRewards();
                    assertEquals(2000,activity.stars);
                    assertEquals(63,activity.rewardFlags);
                    activity.updateRewards();
                    assertEquals("Rewards must not duplicate",2000,activity.stars);
                    activity.statsDay="2000-01-01";
                    activity.ensureDay();
                    assertEquals("Daily flags must reset on new day",60,activity.rewardFlags);
                }finally{
                    activity.game=savedGame;activity.weekPoints=savedWeekPoints;
                    activity.weekStart=savedWeek;activity.undoUsedThisGame=savedUndo;
                }
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
                assertTrue("Newly achieved daily bonus should be announced",
                    activity.dailyBonusGrantedThisAttempt);
                capture(activity,"daily-success");
                // A second valid daily attempt is allowed, but no second reward.
                activity.dailyBonusGrantedThisAttempt=false;
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
                assertFalse("Replay must not claim a second reward",
                    activity.dailyBonusGrantedThisAttempt);
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
                    activity.page="daily";
                    capture(activity,"daily-theme-"+theme);
                    activity.page="missions";
                    capture(activity,"missions-theme-"+theme);
                    activity.page="themes";
                    capture(activity,"themes-theme-"+theme);
                    activity.page="stats";
                    capture(activity,"stats-theme-"+theme);
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

    @Test public void themesNavigationAndSettingsLanguageFitSafeArea(){
        rule.getScenario().onActivity(activity->{
            MainActivity.BoardScreen view=activity.screen;
            try{
                activity.page="themes";
                capture(activity,"themes-safe-area-and-tabs");
                MainActivity.BoardScreen.Tap settingsTab=null;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Postavke".equals(tap.label)&&tap.top>790)settingsTab=tap;
                }
                assertNotNull("Themes must expose Settings bottom tab",settingsTab);
                float bottom=settingsTab.bottom*view.scale+view.offY;
                int contentBottom=view.getHeight()-view.getPaddingBottom();
                assertTrue("All bottom navigation text/tap areas must fit the safe viewport: "+
                    bottom+" > "+contentBottom,bottom<=contentBottom+1);
                assertTrue("Tabs need usable touch height",settingsTab.bottom-settingsTab.top>=48);
                settingsTab.action.run();
                assertEquals("settings",activity.page);
                capture(activity,"settings-language-dropdown");
                boolean hasLanguage=false;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Jezik".equals(tap.label)){
                        assertTrue("Language must have a sufficiently large touch target",
                            tap.bottom-tap.top>=48);
                        hasLanguage=true;
                    }
                }
                assertTrue("Language selection must be interactive",hasLanguage);
            }finally{activity.page="home";}
        });
    }

    @Test public void launcherAdaptiveRoundAndThemedIconRender() {
        rule.getScenario().onActivity(activity->{
            if(android.os.Build.VERSION.SDK_INT<26)return;
            try{
                android.graphics.drawable.Drawable icon=activity.getPackageManager()
                    .getApplicationIcon(activity.getPackageName());
                assertTrue("Adaptive launcher icon required",
                    icon instanceof android.graphics.drawable.AdaptiveIconDrawable);
                renderLauncherAsset(activity,icon,"launcher-adaptive");
                int roundRes=activity.getResources().getIdentifier(
                    "ic_launcher_round","mipmap",activity.getPackageName());
                assertTrue("Separate round launcher resource required",roundRes!=0);
                android.graphics.drawable.Drawable round=activity.getResources()
                    .getDrawable(roundRes,activity.getTheme());
                renderLauncherAsset(activity,round,"launcher-round");
                if(android.os.Build.VERSION.SDK_INT>=33){
                    android.graphics.drawable.AdaptiveIconDrawable adaptive=
                        (android.graphics.drawable.AdaptiveIconDrawable)icon;
                    android.graphics.drawable.Drawable monochrome=
                        adaptive.getMonochrome();
                    assertNotNull("Android themed monochrome layer required",
                        monochrome);
                    monochrome.setTint(android.graphics.Color.DKGRAY);
                    renderLauncherAsset(activity,monochrome,
                        "launcher-android13-monochrome");
                }
            }catch(android.content.pm.PackageManager.NameNotFoundException error){
                throw new AssertionError("Brojko launcher must be installed",error);
            }
        });
    }

    private void renderLauncherAsset(MainActivity activity,
        android.graphics.drawable.Drawable drawable,String filename){
        Bitmap bmp=Bitmap.createBitmap(512,512,Bitmap.Config.ARGB_8888);
        try{
            Canvas canvas=new Canvas(bmp);
            drawable.setBounds(0,0,512,512);
            drawable.draw(canvas);
            assertTrue("Launcher cannot be entirely transparent",
                android.graphics.Color.alpha(bmp.getPixel(256,256))>0);
            File folder=new File(activity.getExternalFilesDir(null),"screenshots");
            assertTrue(folder.isDirectory()||folder.mkdirs());
            try(FileOutputStream out=new FileOutputStream(
                new File(folder,filename+".png"))){
                assertTrue("Unable to save launcher image",
                    bmp.compress(Bitmap.CompressFormat.PNG,100,out));
            }catch(java.io.IOException error){
                throw new AssertionError("Saving launcher icon failed",error);
            }
        }finally{bmp.recycle();}
    }

    @Test public void buttonPressFeedbackAndReleaseSafety(){
        rule.getScenario().onActivity(activity->{
            MainActivity.BoardScreen screen=activity.screen;
            String previous=activity.page;
            try{
                activity.page="home";
                Bitmap bitmap=Bitmap.createBitmap(screen.getWidth(),screen.getHeight(),
                    Bitmap.Config.ARGB_8888);
                try{screen.draw(new Canvas(bitmap));}finally{bitmap.recycle();}
                MainActivity.BoardScreen.Tap target=null;
                for(MainActivity.BoardScreen.Tap tap:screen.taps)
                    if("Postavke".equals(tap.label)){target=tap;break;}
                assertNotNull("Settings shortcut missing",target);
                long now=android.os.SystemClock.uptimeMillis();
                float downX=screen.offX+(target.right-4)*screen.scale;
                float upX=screen.offX+(target.right+7)*screen.scale;
                float y=screen.offY+
                    (target.top+(target.bottom-target.top)/2)*screen.scale;
                MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,
                    downX,y,0);
                screen.onTouchEvent(down);
                assertNotNull("Pressed state should be visible",screen.pressedTap);
                MotionEvent up=MotionEvent.obtain(now,now+80,MotionEvent.ACTION_UP,
                    upX,y,0);
                screen.onTouchEvent(up);down.recycle();up.recycle();
                assertNull("Release clears pressed state",screen.pressedTap);
                assertEquals("Release outside original card cannot navigate",
                    "home",activity.page);
                now=android.os.SystemClock.uptimeMillis();
                float centerX=screen.offX+(target.left+target.right)/2*screen.scale;
                down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,centerX,y,0);
                up=MotionEvent.obtain(now,now+75,MotionEvent.ACTION_UP,centerX,y,0);
                screen.onTouchEvent(down);screen.onTouchEvent(up);
                down.recycle();up.recycle();
                assertEquals("Normal Settings tap must navigate",
                    "settings",activity.page);
            }finally{activity.page=previous;}
        });
    }

    @Test public void originalReferenceViewportMaintainsFullGameplayControls(){
        rule.getScenario().onActivity(activity->{
            // An offscreen instance avoids Activity relayout and device insets
            // changing the requested 941x1672 reference viewport mid-test.
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            try{
                if(activity.game==null)
                    activity.game=new TileGame("classic","",441L);
                view.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                String[] pages={"splash","home","game","win","daily",
                    "missions","stats","themes","settings","help","over","pause"};
                for(String page:pages){
                    activity.page=page;
                    captureReference(activity,view,"reference-941x1672-"+page);
                }
                activity.page="game";
                Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                assertEquals(941,view.getWidth());
                assertEquals(1672,view.getHeight());
                assertTrue("Reference aspect must use compact board",
                    view.compactBoardLayout);
                assertEquals(171f,view.gridY,.1f);
                boolean newButton=false,finishButton=false;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Novo".equals(tap.label)||"Završi".equals(tap.label)){
                        float edge=view.offY+tap.bottom*view.scale;
                        assertTrue("Game action clipped: "+tap.label+" edge="+edge,
                            edge<=1673);
                        if("Novo".equals(tap.label))newButton=true;
                        if("Završi".equals(tap.label))finishButton=true;
                    }
                }
                assertTrue("The three-button game toolbar must exist",
                    newButton&&finishButton);
                activity.page="settings";
                bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                boolean lastSetting=false;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("O aplikaciji".equals(tap.label)){
                        lastSetting=true;
                        assertTrue("Settings last row clipped",
                            view.offY+tap.bottom*view.scale<=1673);
                        assertTrue("Settings touch target is too small",
                            tap.bottom-tap.top>=48);
                    }
                }
                assertTrue("About app settings item missing",lastSetting);
                activity.page="win";
                bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                int visibleActions=0;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Nastavi igrati".equals(tap.label)||
                       "Nova igra".equals(tap.label)||
                       "Podijeli rezultat".equals(tap.label)){
                        visibleActions++;
                        assertTrue("Victory action clipped: "+tap.label,
                            view.offY+tap.bottom*view.scale<=1673);
                    }
                }
                assertEquals("Victory must show all 3 functional buttons",
                    3,visibleActions);
                activity.page="home";
                bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                int shortcuts=0;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Misije".equals(tap.label)||"Statistika".equals(tap.label)||
                       "Teme".equals(tap.label)||"Postavke".equals(tap.label)){
                        shortcuts++;
                        assertTrue("Home shortcut is clipped: "+tap.label,
                            view.offY+tap.bottom*view.scale<=1673);
                    }
                }
                assertEquals("All four reference home shortcuts must be visible",
                    4,shortcuts);
                activity.page="daily";
                bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                boolean challengeAction=false;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if(tap.label.contains("izazov")||"Igraj ponovno".equals(tap.label)){
                        challengeAction=true;
                        assertTrue("Daily challenge action is clipped",
                            view.offY+tap.bottom*view.scale<=1673);
                    }
                }
                assertTrue("Daily CTA must be visible and clickable",
                    challengeAction);
                activity.page="themes";
                bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                boolean settingsTab=false;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Postavke".equals(tap.label)&&tap.top>=620){
                        settingsTab=true;
                        assertTrue("Themes bottom navigation is clipped",
                            view.offY+tap.bottom*view.scale<=1673);
                    }
                }
                assertTrue("Theme preview must include all four tabs",settingsTab);
                activity.page="stats";
                bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                assertTrue("Statistics chart must be selectable",
                    view.taps.stream().anyMatch(tap->
                        "Grafikon bodova".equals(tap.label)));
            }finally{
                activity.game=previousGame;
                activity.page=previousPage;
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
            if(android.os.Build.VERSION.SDK_INT>=28){
                assertTrue("Display Nunito weight must be heavy",
                    activity.screen.displayFont.getWeight()>=900);
                assertTrue("Bold Nunito must be heavier than regular",
                    activity.screen.boldFont.getWeight()>activity.screen.regularFont.getWeight());
            }
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
    @Test public void iconOnlyControlsUseLocalizedLabelsAndGenerousTouchAreas(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            TileGame previousGame=activity.game;
            try{
                activity.game=new TileGame("classic","",777L);
                activity.page="game";
                capture(activity,"game-disabled-undo");
                assertFalse("New game must not expose an inert Undo action",
                    activity.screen.taps.stream().anyMatch(tap->
                        "Poništi potez".equals(tap.label)));
                // A legal merge creates an undo snapshot and enables both controls.
                activity.game.cells=new int[16];
                activity.game.cells[0]=2;
                activity.game.cells[1]=2;
                assertTrue(activity.game.move(TileGame.LEFT).changed);
                assertTrue(activity.game.canUndo());
                capture(activity,"game-enabled-undo");
                boolean pause=false,undo=false;
                for(MainActivity.BoardScreen.Tap tap:activity.screen.taps){
                    if("Pauziraj igru".equals(tap.label)||
                        "Poništi potez".equals(tap.label)){
                        assertTrue("Icon touch width should be >= 54 design units",
                            tap.right-tap.left>=53.9f);
                        assertTrue("Icon touch height should be >= 54 design units",
                            tap.bottom-tap.top>=53.9f);
                        if("Pauziraj igru".equals(tap.label))pause=true;
                        else undo=true;
                    }
                }
                assertTrue("Pause must have a localized label",pause);
                assertTrue("Undo must have a localized label",undo);
                activity.page="daily";
                capture(activity,"daily-back-a11y");
                assertTrue("Back action must have a localized label",
                    activity.screen.taps.stream().anyMatch(tap->
                        "Natrag".equals(tap.label)));
            }finally{
                activity.game=previousGame;
                activity.page=previousPage;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void launchBackgroundMatchesBrojkoCreamBeforeFirstFrame(){
        rule.getScenario().onActivity(activity->{
            android.util.TypedValue background=new android.util.TypedValue();
            assertTrue("Startup theme must define a window background",
                activity.getTheme().resolveAttribute(
                    android.R.attr.windowBackground,background,true));
            assertEquals("Avoid an unbranded white launch frame",
                MainActivity.CREAM,background.data);
        });
    }

    @Test public void dailyResultModalKeepsBottomActionInsideItsCard(){
        rule.getScenario().onActivity(activity->{
            TileGame priorGame=activity.game;
            String priorPage=activity.page;
            try{
                activity.game=new TileGame("daily",activity.today(),901L);
                activity.page="dailyWin";
                capture(activity,"daily-modal-safe-padding");
                boolean found=false;
                for(MainActivity.BoardScreen.Tap tap:activity.screen.taps){
                    if("Izbornik".equals(tap.label)){
                        found=true;
                        assertTrue("Bottom action needs inner modal padding",
                            tap.bottom<=678);
                        assertTrue("Bottom action must remain >= 48 units high",
                            tap.bottom-tap.top>=48);
                    }
                }
                assertTrue("Daily modal exit action missing",found);
            }finally{
                activity.game=priorGame;
                activity.page=priorPage;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void everyThemeThumbnailUsesItsOwnTileTreatment(){
        rule.getScenario().onActivity(activity->{
            MainActivity.BoardScreen board=activity.screen;
            android.graphics.Canvas savedCanvas=board.c;
            Bitmap[] previews=new Bitmap[3];
            try{
                for(int style=0;style<3;style++){
                    previews[style]=Bitmap.createBitmap(90,90,Bitmap.Config.ARGB_8888);
                    Canvas canvas=new Canvas(previews[style]);
                    canvas.drawColor(MainActivity.CREAM);
                    board.c=canvas;
                    board.styledTile(10,10,68,68,style==1?6:style==2?24:14,
                        MainActivity.CORAL,style);
                }
                int flatVsRound=0,glassVsRound=0;
                for(int y=10;y<78;y+=3)for(int x=10;x<78;x+=3){
                    if(previews[0].getPixel(x,y)!=previews[1].getPixel(x,y))
                        flatVsRound++;
                    if(previews[0].getPixel(x,y)!=previews[2].getPixel(x,y))
                        glassVsRound++;
                }
                assertTrue("Flat preview must be visibly different",flatVsRound>50);
                assertTrue("Glass preview must be visibly different",glassVsRound>50);
            }finally{
                board.c=savedCanvas;
                for(Bitmap img:previews)if(img!=null)img.recycle();
            }
        });
    }

    @Test public void compactHistoryRowsHaveReadableSizeAndDoNotOverlap(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            String previousHistory=activity.prefs.getString("history","[]");
            MainActivity.BoardScreen custom=activity.new BoardScreen(activity);
            try{
                activity.prefs.edit().putString("history",
                    "[{\"score\":100,\"peak\":16},{\"score\":200,\"peak\":32},"+
                    "{\"score\":300,\"peak\":64},{\"score\":400,\"peak\":128},"+
                    "{\"score\":500,\"peak\":256}]").commit();
                custom.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                custom.layout(0,0,941,1672);
                activity.page="stats";
                Bitmap img=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{custom.draw(new Canvas(img));}finally{img.recycle();}
                int previousBottom=-1,count=0;
                for(MainActivity.BoardScreen.Tap tap:custom.taps){
                    if(!tap.label.startsWith("Detalji partije"))continue;
                    assertTrue("History touch target must be >= 54 units",
                        tap.bottom-tap.top>=54f);
                    if(previousBottom>=0)assertTrue("History rows cannot overlap",
                        tap.top>=previousBottom);
                    previousBottom=(int)tap.bottom;
                    count++;
                }
                assertEquals("All five history rows must stay accessible",5,count);
                assertTrue("History list must scroll instead of clipping",
                    custom.maxScroll>0);
            }finally{
                activity.prefs.edit().putString("history",previousHistory).commit();
                activity.page=previousPage;
            }
        });
    }

    @Test public void blockedDailyBoardShowsDailyFailureWithoutFalseMoveLimit(){
        rule.getScenario().onActivity(activity->{
            TileGame previous=activity.game;
            String previousPage=activity.page;
            try{
                TileGame daily=new TileGame("daily",activity.today(),501L);
                daily.cells=new int[]{
                    2,4,2,4,
                    4,2,4,2,
                    2,4,2,4,
                    4,2,4,2
                };
                daily.peak=4;
                daily.moves=12;
                activity.game=daily;
                activity.page="game";
                activity.move(TileGame.LEFT);
                assertEquals("Blocked daily board should show its own result",
                    "dailyFail",activity.page);
                assertTrue("Blocked session must be flagged game over",
                    daily.gameOver);
                assertEquals("No false swipe should be counted",12,daily.moves);
                capture(activity,"daily-no-moves");
            }finally{
                activity.game=previous;
                activity.page=previousPage;
                activity.persist();
            }
        });
    }

    @Test public void animationLevelTapZonesDoNotOverlapOnEitherLayout(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            MainActivity.BoardScreen custom=activity.new BoardScreen(activity);
            try{
                int[][] sizes={{1080,2400},{941,1672}};
                for(int[] size:sizes){
                    custom.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                        View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(size[1],
                        View.MeasureSpec.EXACTLY));
                    custom.layout(0,0,size[0],size[1]);
                    activity.page="themes";
                    Bitmap bmp=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{custom.draw(new Canvas(bmp));}finally{bmp.recycle();}
                    MainActivity.BoardScreen.Tap[] targets=new MainActivity.BoardScreen.Tap[3];
                    for(MainActivity.BoardScreen.Tap tap:custom.taps){
                        if("Niska".equals(tap.label))targets[0]=tap;
                        if("Srednja".equals(tap.label))targets[1]=tap;
                        if("Visoka".equals(tap.label))targets[2]=tap;
                    }
                    for(int i=0;i<3;i++){
                        assertNotNull("Missing animation tap "+i,targets[i]);
                        assertTrue("Tap must fit the content width",
                            targets[i].right<=380);
                        assertTrue("Tap must be wide enough",
                            targets[i].right-targets[i].left>=53);
                        if(i>0)assertTrue("Animation tap targets cannot overlap",
                            targets[i-1].right<targets[i].left);
                    }
                }
            }finally{activity.page=oldPage;}
        });
    }

    @Test public void screenshotQaCoversAllThreeRealGameModes(){
        rule.getScenario().onActivity(activity->{
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            MainActivity.BoardScreen reference=activity.new BoardScreen(activity);
            try{
                reference.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                reference.layout(0,0,941,1672);
                String[] modes={"classic","relaxed","daily"};
                int[] sizes={4,5,4};
                for(int i=0;i<modes.length;i++){
                    TileGame g=new TileGame(modes[i],
                        i==2?activity.today():"",710L+i);
                    activity.game=g;
                    activity.page="game";
                    capture(activity,"game-real-"+modes[i]);
                    assertEquals("Board grid size",sizes[i]*sizes[i],g.cells.length);
                    boolean finish=false;
                    for(MainActivity.BoardScreen.Tap tap:activity.screen.taps)
                        if("Završi".equals(tap.label))finish=true;
                    assertTrue("Game mode must provide a functional finish button",finish);
                    captureReference(activity,reference,
                        "reference-941x1672-game-"+modes[i]);
                    assertEquals("Board dimensions must stay square",
                        368f,reference.gridSize,0.1f);
                    assertTrue("Board must fit within the logical viewport",
                        reference.gridX+reference.gridSize<=400);
                }
            }finally{
                activity.game=previousGame;
                activity.page=previousPage;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void stylePreviewsAndHelpEquationHaveRealOnDeviceScreenshots(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldStyle=activity.tileStyle;
            try{
                activity.page="help";
                capture(activity,"help-visible-merge-equation");
                activity.page="themes";
                for(int style=0;style<3;style++){
                    activity.tileStyle=style;
                    capture(activity,"themes-selected-style-"+style);
                    boolean optionFound=false;
                    String target=new String[]{"Zaobljene","Ravne","Staklaste"}[style];
                    for(MainActivity.BoardScreen.Tap tap:activity.screen.taps){
                        if(target.equals(tap.label)){
                            optionFound=true;
                            assertTrue("Tile-style preview must have a useful touch height",
                                tap.bottom-tap.top>=48);
                        }
                    }
                    assertTrue("Tile style option must be reachable",optionFound);
                }
            }finally{
                activity.tileStyle=oldStyle;
                activity.page=oldPage;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void allScreensKeepActionsReachableOnSmallPhones(){
        rule.getScenario().onActivity(activity->{
            String originalPage=activity.page;
            TileGame originalGame=activity.game;
            int originalHistoryIndex=activity.selectedHistoryIndex;
            String originalHistory=activity.prefs.getString("history","[]");
            MainActivity.BoardScreen compact=activity.new BoardScreen(activity);
            try{
                activity.game=new TileGame("classic","",98234L);
                activity.selectedHistoryIndex=0;
                activity.prefs.edit().putString("history",
                    "[{\"score\":100,\"peak\":16},{\"score\":200,\"peak\":32},"+
                    "{\"score\":300,\"peak\":64},{\"score\":400,\"peak\":128},"+
                    "{\"score\":500,\"peak\":256}]").commit();
                compact.measure(View.MeasureSpec.makeMeasureSpec(360,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY));
                compact.layout(0,0,360,600);
                String[] pages={
                    "splash","home","game","pause","win","over","confirm",
                    "confirmEnd","dailyWin","dailyFail","daily","missions",
                    "stats","historyDetail","themes","settings","language",
                    "backup","help","privacy","about"
                };
                for(String page:pages){
                    activity.page=page;
                    Bitmap bmp=Bitmap.createBitmap(360,600,Bitmap.Config.ARGB_8888);
                    try{
                        compact.draw(new Canvas(bmp));
                        File folder=new File(activity.getExternalFilesDir(null),
                            "screenshots");
                        assertTrue(folder.isDirectory()||folder.mkdirs());
                        try(FileOutputStream out=new FileOutputStream(
                                new File(folder,"compact-360x600-"+page+".png"))){
                            assertTrue(bmp.compress(Bitmap.CompressFormat.PNG,100,out));
                        }catch(java.io.IOException ex){throw new AssertionError(ex);}
                    }finally{bmp.recycle();}
                    float visible=600f/compact.scale;
                    for(MainActivity.BoardScreen.Tap tap:compact.taps){
                        assertTrue(page+": target crosses horizontal safe area: "+tap.label,
                            tap.left>=0 && tap.right<=400 && tap.right>tap.left);
                        assertTrue(page+": target has invalid vertical bounds: "+tap.label,
                            tap.top>=0 && tap.bottom>tap.top);
                        float firstScrollable=Math.max(0,tap.bottom-visible);
                        float lastScrollable=Math.min(compact.maxScroll,tap.top);
                        assertTrue(page+": target not fully reachable: "+tap.label+
                            " scroll="+compact.maxScroll+" top="+tap.top+
                            " bottom="+tap.bottom+" viewport="+visible,
                            firstScrollable<=lastScrollable+1f);
                    }
                }
            }finally{
                activity.prefs.edit().putString("history",originalHistory).commit();
                activity.page=originalPage;
                activity.game=originalGame;
                activity.selectedHistoryIndex=originalHistoryIndex;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void splashBrandDoesNotChangeWithGameplayTileStyle(){
        rule.getScenario().onActivity(activity->{
            int oldStyle=activity.tileStyle;
            String oldPage=activity.page;
            MainActivity.BoardScreen branding=activity.new BoardScreen(activity);
            try{
                branding.measure(View.MeasureSpec.makeMeasureSpec(400,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(880,View.MeasureSpec.EXACTLY));
                branding.layout(0,0,400,880);
                int[] reference=null;
                activity.page="splash";
                for(int style=0;style<3;style++){
                    activity.tileStyle=style;
                    Bitmap bmp=Bitmap.createBitmap(400,880,Bitmap.Config.ARGB_8888);
                    int[] pixels=new int[400*880];
                    try{
                        branding.draw(new Canvas(bmp));
                        bmp.getPixels(pixels,0,400,0,0,400,880);
                    }finally{bmp.recycle();}
                    if(reference==null)reference=pixels;
                    else assertArrayEquals(
                        "Brand must not change when gameplay tile style changes",
                        reference,pixels);
                    capture(activity,"splash-brand-style-"+style);
                }
            }finally{
                activity.tileStyle=oldStyle;
                activity.page=oldPage;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void brandedSplashFitsCompletelyOnShortAndReferencePhones(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="splash";
                int[][] viewports={{360,600},{941,1672},{1080,2400}};
                for(int[] size:viewports){
                    view.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                            View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                            View.MeasureSpec.EXACTLY));
                    view.layout(0,0,size[0],size[1]);
                    Bitmap bitmap=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{view.draw(new Canvas(bitmap));}
                    finally{bitmap.recycle();}
                    assertEquals("Splash cannot require scrolling",0f,view.maxScroll,.01f);
                    float lastSloganEdge=view.offY+824f*view.scale;
                    assertTrue("Brand slogan clipped at "+
                        size[0]+"x"+size[1]+": "+lastSloganEdge,
                        lastSloganEdge<=size[1]+1f);
                    float firstContentEdge=view.offY+74f*view.scale;
                    assertTrue("Splash upper decoration must stay visible",
                        firstContentEdge>=0);
                }
            }finally{activity.page=oldPage;}
        });
    }

    @Test public void referenceSixMissionsRewardOnlyActualProgress(){
        rule.getScenario().onActivity(activity->{
            TileGame prior=activity.game;
            String prev=activity.page,week=activity.weekStart;
            int flags=activity.rewardFlags,stars=activity.stars,points=activity.weekPoints;
            boolean undo=activity.undoUsedThisGame;
            try{
                activity.game=new TileGame("classic","",807L);
                activity.game.peak=512;
                activity.weekStart=activity.currentWeekStart();
                activity.weekPoints=10000;
                activity.rewardFlags=0;activity.stars=0;
                activity.undoUsedThisGame=true;
                activity.updateRewards();
                assertEquals("Weekly 10k grants 200",200,activity.stars);
                assertEquals("Using Undo disqualifies 512",0,activity.rewardFlags&2);
                activity.undoUsedThisGame=false;
                activity.updateRewards();
                assertEquals("512 without Undo adds 100",300,activity.stars);
                activity.updateRewards();
                assertEquals("Neither reward repeats",300,activity.stars);
                activity.page="missions";
                capture(activity,"original-six-missions");
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap bmp=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{screen.draw(new Canvas(bmp));}finally{bmp.recycle();}
                assertTrue("Missions must have visible final card",
                    screen.maxScroll<=180);
            }finally{
                activity.game=prior;activity.page=prev;activity.weekStart=week;
                activity.rewardFlags=flags;activity.stars=stars;activity.weekPoints=points;
                activity.undoUsedThisGame=undo;activity.persist();
            }
        });
    }

    @Test public void originalThemeChoiceUsesCenteredIndicatorsUnderTiles(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try{
                activity.page="themes";
                MainActivity.BoardScreen custom=activity.new BoardScreen(activity);
                custom.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                custom.layout(0,0,941,1672);
                for(int theme=0;theme<3;theme++){
                    activity.theme=theme;
                    Bitmap bmp=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                    try{custom.draw(new Canvas(bmp));}
                    finally{bmp.recycle();}
                    assertEquals("Three theme options remain tappable",3,
                        (int)custom.taps.stream().filter(tap->
                            "Svijetla".equals(tap.label)||
                            "Tamna".equals(tap.label)||
                            "Živopisna".equals(tap.label)).count());
                    capture(activity,"original-theme-selection-"+theme);
                }
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void originalPixelReferencesUseIdenticalRepresentativeGameData(){
        rule.getScenario().onActivity(activity->{
            TileGame prior=activity.game;
            String priorPage=activity.page;
            int priorBest=activity.best,priorGames=activity.plays;
            int priorTotal=activity.total,priorTile=activity.recordTile;
            int priorStreak=activity.playDayStreak;
            String priorHistory=activity.prefs.getString("history","[]");
            MainActivity.BoardScreen reference=activity.new BoardScreen(activity);
            try{
                reference.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                reference.layout(0,0,941,1672);
                activity.game=new TileGame("classic","",2124L);
                activity.game.cells=new int[16];
                activity.game.cells[0]=2;activity.game.cells[1]=2;
                assertTrue("Golden game should have an active Undo history",
                    activity.game.move(TileGame.LEFT).changed);
                assertTrue(activity.game.canUndo());
                activity.game.cells=new int[]{
                    2,4,8,16,
                    8,16,2,4,
                    32,64,8,2,
                    128,16,32,256
                };
                activity.game.peak=256;activity.game.moves=33;
                activity.game.score=1316;activity.best=3864;
                activity.page="game";
                captureReference(activity,reference,"golden-state-game-941x1672");
                activity.game.peak=2048;activity.game.reached2048=true;
                activity.game.score=2048;activity.best=12560;
                activity.page="win";
                captureReference(activity,reference,"golden-state-win-941x1672");
                org.json.JSONArray history=new org.json.JSONArray();
                int[] scores={12560,6144,3072,8192,1024,4608,5120,3840,6400,3000};
                int[] peaks={1024,256,128,512,1024,256,512,128,256,64};
                for(int i=0;i<10;i++){
                    org.json.JSONObject entry=new org.json.JSONObject();
                    try{
                        entry.put("score",scores[i]);
                        entry.put("peak",peaks[i]);
                        entry.put("day","12. tra 2024.");
                    }catch(org.json.JSONException error){throw new AssertionError(error);}
                    history.put(entry);
                }
                activity.prefs.edit().putString("history",history.toString()).commit();
                activity.best=12560;activity.plays=248;activity.total=958272;
                activity.recordTile=1024;activity.playDayStreak=7;
                activity.page="stats";
                captureReference(activity,reference,"golden-state-stats-941x1672");
            }finally{
                activity.prefs.edit().putString("history",priorHistory).commit();
                activity.game=prior;activity.page=priorPage;activity.best=priorBest;
                activity.plays=priorGames;activity.total=priorTotal;
                activity.recordTile=priorTile;activity.playDayStreak=priorStreak;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void periodicMissionBadgesReflectRealLocalBoundaries(){
        rule.getScenario().onActivity(activity->{
            String daily=activity.timeRemainingInPeriod(false);
            String weekly=activity.timeRemainingInPeriod(true);
            assertTrue("Daily countdown must have hours",daily.contains("h "));
            assertTrue("Daily countdown must include minutes",daily.endsWith("min"));
            assertTrue("Weekly countdown must have days",weekly.contains("d "));
            assertTrue("Weekly countdown must have hours",weekly.endsWith("h"));
            String original=activity.page;
            try{
                activity.page="missions";
                capture(activity,"original-mission-period-header");
            }finally{activity.page=original;}
        });
    }

}
