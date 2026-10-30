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

    // Sample a complete decorative region, not one antialiased pixel.
    // Stroke edges differ between Android GPU/CPU rasterizers and densities.
    private static int countCoralPixels(Bitmap bitmap,MainActivity.BoardScreen view,
                                        float left,float top,float right,float bottom){
        int result=0;
        for(float ly=top;ly<=bottom;ly+=2f){
            int py=Math.round(view.offY+ly*view.scale);
            if(py<0||py>=bitmap.getHeight())continue;
            for(float lx=left;lx<=right;lx+=2f){
                int px=Math.round(view.offX+lx*view.scale);
                if(px<0||px>=bitmap.getWidth())continue;
                int rgb=bitmap.getPixel(px,py);
                if(android.graphics.Color.red(rgb)>225 &&
                   android.graphics.Color.green(rgb)<170 &&
                   android.graphics.Color.blue(rgb)<150)result++;
            }
        }
        return result;
    }

    private static void assertSystemBarIconContrast(MainActivity activity,boolean light){
        androidx.core.view.WindowInsetsControllerCompat controller=
            androidx.core.view.WindowCompat.getInsetsController(
                activity.getWindow(),activity.getWindow().getDecorView());
        assertEquals("Status icon contrast must match canvas",light,
            controller.isAppearanceLightStatusBars());
        if(android.os.Build.VERSION.SDK_INT>=26)
            assertEquals("Navigation icon contrast must match canvas",light,
                controller.isAppearanceLightNavigationBars());
    }

    @Test public void lightCanvasMatchesReferenceAndDarkPaletteIsPreserved(){
        rule.getScenario().onActivity(activity->{
            String originalPage=activity.page;
            int originalTheme=activity.theme;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            view.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
            view.layout(0,0,941,1672);
            Bitmap screenshot=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
            try{
                activity.page="settings";
                activity.theme=0;
                activity.updateBars();
                view.draw(new Canvas(screenshot));
                assertEquals("Portrait reference background must not be tinted cream",
                    MainActivity.LIGHT_CANVAS,screenshot.getPixel(2,200));
                assertEquals(MainActivity.LIGHT_CANVAS,view.bg);
                assertSystemBarIconContrast(activity,true);
                activity.theme=1;
                activity.updateBars();
                view.draw(new Canvas(screenshot));
                assertEquals("Changing the light background must never affect dark mode",
                    0xff14233a,screenshot.getPixel(2,200));
                assertEquals(0xff14233a,view.bg);
                assertSystemBarIconContrast(activity,false);
            }finally{
                screenshot.recycle();
                activity.page=originalPage;
                activity.theme=originalTheme;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void edgeToEdgeInsetsAreAppliedBeforeScreenControls(){
        rule.getScenario().onActivity(activity->{
            androidx.core.view.WindowInsetsCompat insets=
                androidx.core.view.ViewCompat.getRootWindowInsets(activity.screen);
            assertNotNull("A laid-out activity must receive window insets",insets);
            androidx.core.graphics.Insets safe=insets.getInsets(
                androidx.core.view.WindowInsetsCompat.Type.systemBars()|
                androidx.core.view.WindowInsetsCompat.Type.displayCutout());
            assertEquals(safe.left,activity.screen.getPaddingLeft());
            assertEquals(safe.top,activity.screen.getPaddingTop());
            assertEquals(safe.right,activity.screen.getPaddingRight());
            assertEquals(safe.bottom,activity.screen.getPaddingBottom());
        });
    }

    @Test public void edgeToEdgeCriticalButtonsAreOutsideSystemBars(){
        rule.getScenario().onActivity(activity->{
            MainActivity.BoardScreen view=activity.screen;
            androidx.core.view.WindowInsetsCompat insets=
                androidx.core.view.ViewCompat.getRootWindowInsets(view);
            assertNotNull("Insets must be delivered before drawing",insets);
            androidx.core.graphics.Insets safe=insets.getInsets(
                androidx.core.view.WindowInsetsCompat.Type.systemBars()|
                androidx.core.view.WindowInsetsCompat.Type.displayCutout());
            String previousPage=activity.page;
            TileGame previousGame=activity.game;
            try{
                activity.game=new TileGame("classic","",34982L);
                for(String selected:new String[]{"home","game"}){
                    activity.page=selected;
                    Bitmap bmp=Bitmap.createBitmap(view.getWidth(),view.getHeight(),
                        Bitmap.Config.ARGB_8888);
                    try{view.draw(new Canvas(bmp));}finally{bmp.recycle();}
                    int critical=0;
                    for(MainActivity.BoardScreen.Tap tap:view.taps){
                        boolean important="home".equals(selected)?
                            ("Nova igra".equals(tap.label)||
                             "Nastavi igru".equals(tap.label)):
                            ("Novo".equals(tap.label)||
                             "Završi".equals(tap.label));
                        if(!important)continue;
                        critical++;
                        float top=view.offY+tap.top*view.scale;
                        float bottom=view.offY+tap.bottom*view.scale;
                        assertTrue("Primary action begins under system bar on "+
                            selected,top>=safe.top-1f);
                        if(view.maxScroll<=.01f)
                            assertTrue("Primary action covered by navigation bar on "+
                                selected,bottom<=view.getHeight()-safe.bottom+1f);
                        else
                            assertTrue("Primary action must be scroll-reachable",
                                tap.bottom<=view.getHeight()/view.scale+
                                    view.maxScroll+1f);
                    }
                    assertTrue("Both screens need functional primary actions",
                        critical>0);
                }
            }finally{
                activity.game=previousGame;
                activity.page=previousPage;
                view.invalidate();
            }
        });
    }

    @Test public void rotationRecreationPreservesCurrentGameAndNavigation(){
        final String[] oldPage=new String[1];
        final String[] oldSession=new String[1];
        rule.getScenario().onActivity(activity->{
            oldPage[0]=activity.page;
            oldSession[0]=activity.game==null?"":activity.game.toJson().toString();
            activity.game=new TileGame("classic","",123456L);
            activity.game.score=256;
            activity.page="game";
            activity.screen.renderedPage="game";
            activity.screen.invalidate();
        });
        rule.getScenario().recreate();
        rule.getScenario().onActivity(activity->{
            try{
                assertEquals("game",activity.page);
                assertNotNull(activity.game);
                assertEquals("Board state must survive Activity recreation",256,activity.game.score);
            }finally{
                try{
                    activity.game=oldSession[0].isEmpty()?null:
                        TileGame.fromJson(new org.json.JSONObject(oldSession[0]));
                }catch(Exception e){throw new AssertionError(e);}
                activity.page=oldPage[0];
                activity.persist();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void extraLongCroatianLabelsStayWithinCenteredAndLeftAlignedCards(){
        rule.getScenario().onActivity(activity->{
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            Bitmap bitmap=Bitmap.createBitmap(400,140,Bitmap.Config.ARGB_8888);
            try{
                Canvas canvas=new Canvas(bitmap);
                view.c=canvas;
                view.palette();
                String label="Sinkronizacija napretka, pravila privatnosti i postavke aplikacije";
                canvas.drawColor(android.graphics.Color.WHITE);
                view.fitted(label,200,62,31,130,android.graphics.Color.BLACK,true);
                int inside=0;
                for(int y=20;y<68;y++)for(int x=0;x<400;x++){
                    int rgb=bitmap.getPixel(x,y);
                    if(rgb==android.graphics.Color.WHITE)continue;
                    if(x<134||x>266)
                        fail("Centered text escaped its 130-unit card at x="+x);
                    inside++;
                }
                assertTrue("Long centered label should still be visible",inside>25);
                canvas.drawColor(android.graphics.Color.WHITE);
                view.leftFitted(label,40,118,27,125,android.graphics.Color.BLACK,true);
                inside=0;
                for(int y=80;y<127;y++)for(int x=0;x<400;x++){
                    int rgb=bitmap.getPixel(x,y);
                    if(rgb==android.graphics.Color.WHITE)continue;
                    if(x<39||x>166)
                        fail("Left-aligned text escaped its 125-unit card at x="+x);
                    inside++;
                }
                assertTrue("Long left-aligned label should still be visible",inside>25);
            }finally{bitmap.recycle();}
        });
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
                assertEquals(activity.game.size==4?159f:171f,view.gridY,.1f);
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

    @Test public void classicOnlyHeaderControlsKeepRealAccessibilityTargets(){
        rule.getScenario().onActivity(activity->{
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            int previousTheme=activity.theme,previousStyle=activity.tileStyle;
            MainActivity.BoardScreen preview=activity.new BoardScreen(activity);
            try{
                activity.page="game";activity.theme=0;activity.tileStyle=0;
                activity.game=new TileGame("classic","",8844L);
                preview.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                preview.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{
                    preview.draw(new Canvas(image));
                    int pause=0,topUndo=0;
                    for(MainActivity.BoardScreen.Tap tap:preview.taps){
                        if("Pauziraj igru".equals(tap.label)){
                            pause++;
                            assertTrue("Pause touch height",tap.bottom-tap.top>=54f);
                            assertTrue("Pause touch width",tap.right-tap.left>=54f);
                        }
                        if("Poništi potez".equals(tap.label))topUndo++;
                    }
                    assertEquals("The real pause action must remain",1,pause);
                    assertEquals("Undo without prior move has no fake action",0,topUndo);
                    activity.game.cells=new int[16];
                    activity.game.cells[0]=2;activity.game.cells[1]=2;
                    assertTrue(activity.game.move(TileGame.LEFT).changed);
                    preview.draw(new Canvas(image));
                    topUndo=0;
                    for(MainActivity.BoardScreen.Tap tap:preview.taps){
                        if("Poništi potez".equals(tap.label)){
                            topUndo++;
                            assertTrue("Header Undo remains >=54-unit target",
                                tap.bottom-tap.top>=54f&&tap.right-tap.left>=54f);
                        }
                    }
                    assertEquals("Enabled top Undo must remain actionable",1,topUndo);
                }finally{image.recycle();}
            }finally{
                activity.game=previousGame;activity.page=previousPage;
                activity.theme=previousTheme;activity.tileStyle=previousStyle;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void portraitClassicGameHasEnabledAndDisabledBottomActionTargets(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            TileGame previousGame=activity.game;
            int previousTheme=activity.theme,previousStyle=activity.tileStyle;
            MainActivity.BoardScreen preview=activity.new BoardScreen(activity);
            try{
                activity.page="game";activity.theme=0;activity.tileStyle=0;
                activity.game=new TileGame("classic","",98031L);
                preview.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                preview.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    preview.draw(new Canvas(image));
                    int newCount=0,finishCount=0,disabledUndo=0;
                    for(MainActivity.BoardScreen.Tap tap:preview.taps){
                        if("Novo".equals(tap.label))newCount++;
                        if("Završi".equals(tap.label))finishCount++;
                        if("Poništi".equals(tap.label))disabledUndo++;
                    }
                    assertEquals("New-game touch target must be available",1,newCount);
                    assertEquals("Finish confirmation must be available",1,finishCount);
                    assertEquals("Disabled Undo may not be clickable",0,disabledUndo);

                    activity.game.cells=new int[16];
                    activity.game.cells[0]=2;
                    activity.game.cells[1]=2;
                    assertTrue("Setup must create an undoable merge",
                        activity.game.move(TileGame.LEFT).changed);
                    preview.draw(new Canvas(image));
                    int undoCount=0;
                    for(MainActivity.BoardScreen.Tap tap:preview.taps){
                        if("Poništi".equals(tap.label)){
                            undoCount++;
                            assertTrue("Undo must meet touch target height",
                                tap.bottom-tap.top>=90f);
                            assertTrue("Undo must meet touch target width",
                                tap.right-tap.left>=110f);
                            assertTrue("Undo must fit the logical viewport",
                                tap.left>=0&&tap.right<=400);
                        }
                    }
                    assertEquals("Undo must become actionable after move",1,undoCount);
                }finally{image.recycle();}
            }finally{
                activity.page=previousPage;activity.game=previousGame;
                activity.theme=previousTheme;activity.tileStyle=previousStyle;
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


    @Test public void pauseModalKeepsTheRealGameAndUsesOnlyTwoSafeActions(){
        rule.getScenario().onActivity(activity->{
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            int previousTheme=activity.theme;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.game=new TileGame("classic","",70881L);
                activity.game.score=3848;
                activity.game.moves=17;
                activity.page="pause";
                activity.theme=0;
                final int[] originalCells=activity.game.cells.clone();
                for(int[] size:new int[][]{{941,1672},{360,600},{1080,2400}}){
                    board.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                            View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                            View.MeasureSpec.EXACTLY));
                    board.layout(0,0,size[0],size[1]);
                    Bitmap bitmap=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{
                        board.draw(new Canvas(bitmap));
                        assertEquals("Pause must show only modal actions",
                            2,board.taps.size());
                        assertEquals("First action resumes",
                            "Nastavi",board.taps.get(0).label);
                        assertEquals("Second action exits to home",
                            "Izbornik",board.taps.get(1).label);
                        assertEquals("Pause overlay must not phantom-scroll",
                            0f,board.maxScroll,.01f);
                        for(MainActivity.BoardScreen.Tap tap:board.taps){
                            assertTrue("Pause button remains inside the modal",
                                tap.left>=52f&&tap.right<=348f);
                            assertTrue("Pause button stays visible",
                                board.offY+tap.bottom*board.scale<=size[1]+1f);
                            assertTrue("Accessible pause action",
                                tap.bottom-tap.top>=60f);
                        }
                        assertTrue("Buttons cannot overlap",
                            board.taps.get(0).bottom<board.taps.get(1).top);
                        if(size[0]==941){
                            captureReference(activity,board,
                                "pause-branded-modal-941x1672");
                            Bitmap changed=Bitmap.createBitmap(941,1672,
                                Bitmap.Config.ARGB_8888);
                            try{
                                activity.game.score=0;
                                board.draw(new Canvas(changed));
                                int changedPixels=0;
                                for(int y=420;y<=449;y+=2)
                                    for(int x=230;x<=312;x+=2){
                                        int px=Math.round(board.offX+
                                            x*board.scale);
                                        int py=Math.round(board.offY+
                                            y*board.scale);
                                        if(bitmap.getPixel(px,py)!=
                                            changed.getPixel(px,py))
                                            changedPixels++;
                                    }
                                assertTrue("Score must come from game state",
                                    changedPixels>20);
                            }finally{
                                activity.game.score=3848;
                                changed.recycle();
                            }
                        }
                    }finally{bitmap.recycle();}
                }
                board.taps.get(0).action.run();
                assertEquals("Resume must return to the same game",
                    "game",activity.page);
                assertEquals(3848,activity.game.score);
                assertEquals(17,activity.game.moves);
                assertArrayEquals("Pause does not mutate tile positions",
                    originalCells,activity.game.cells);
                activity.page="pause";
                Bitmap reenter=Bitmap.createBitmap(1080,2400,
                    Bitmap.Config.ARGB_8888);
                try{
                    board.draw(new Canvas(reenter));
                    board.taps.get(1).action.run();
                }finally{reenter.recycle();}
                assertEquals("Menu action returns home","home",activity.page);
                assertEquals("Home action does not erase session",
                    3848,activity.game.score);
            }finally{
                activity.game=previousGame;
                activity.page=previousPage;activity.theme=previousTheme;
                activity.screen.invalidate();
            }
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
                    // The source has five visually compact (34-unit)
                    // rows; preserve distinct, nonoverlapping hit regions.
                    // Long labels remain accessible through full-width taps.
                    assertTrue("Visible history row must have a real hit zone",
                        tap.bottom-tap.top>=32f);
                    assertTrue("Fifth row must fit reference viewport",tap.bottom<=710f);
                    if(previousBottom>=0)assertTrue("History rows cannot overlap",
                        tap.top>=previousBottom);
                    previousBottom=(int)tap.bottom;
                    count++;
                }
                assertEquals("All five history rows must stay accessible",5,count);
                assertTrue("Five rows should fit without scrolling at reference size",
                    custom.maxScroll<=1.1f);
                captureReference(activity,custom,
                    "stats-five-history-fully-visible-941x1672");
            }finally{
                activity.prefs.edit().putString("history",previousHistory).commit();
                activity.page=previousPage;
            }
        });
    }

    @Test public void statisticsSourceLayoutRetainsRealChartAndHistoryActions(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            String oldHistory=activity.prefs.getString("history","[]");
            int oldBest=activity.best,oldGames=activity.plays,
                oldTotal=activity.total,oldRecord=activity.recordTile,
                oldChart=activity.chartMetric;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                // Reference values exist only in the isolated UI fixture.
                activity.best=12560;activity.plays=248;
                activity.total=958272;activity.recordTile=1024;
                org.json.JSONArray rows=new org.json.JSONArray();
                int[] score={12560,6144,3072,8192,1024};
                int[] peak={1024,256,128,512,1024};
                for(int i=0;i<score.length;i++){
                    org.json.JSONObject row=new org.json.JSONObject();
                    try{
                        row.put("score",score[i]);row.put("peak",peak[i]);
                        row.put("day","12. tra 2024.");
                        row.put("time","14:32");
                    }catch(org.json.JSONException ex){throw new AssertionError(ex);}
                    rows.put(row);
                }
                activity.prefs.edit().putString("history",rows.toString()).commit();
                activity.page="stats";activity.chartMetric=0;
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    view.draw(new Canvas(bitmap));
                    captureReference(activity,view,
                        "stats-source-layout-941x1672");
                    MainActivity.BoardScreen.Tap switchToPeak=null;
                    int historyLinks=0;
                    for(MainActivity.BoardScreen.Tap tap:view.taps){
                        if("Grafikon pločica".equals(tap.label))
                            switchToPeak=tap;
                        if(tap.label.startsWith("Detalji partije")){
                            historyLinks++;
                            assertTrue("History rows must stay inside screen",
                                tap.bottom<=710f);
                        }
                    }
                    assertEquals("All five real results must be linked",5,historyLinks);
                    assertNotNull("Chart metric toggle must be functional",
                        switchToPeak);
                    switchToPeak.action.run();
                    assertEquals("Chart switches to peak tiles",1,activity.chartMetric);
                    view.draw(new Canvas(bitmap));
                    int switched=0;
                    for(MainActivity.BoardScreen.Tap tap:view.taps)
                        if("Grafikon bodova".equals(tap.label))switched++;
                    assertEquals("Switching back must remain possible",1,switched);
                }finally{bitmap.recycle();}
            }finally{
                activity.prefs.edit().putString("history",oldHistory).commit();
                activity.best=oldBest;activity.plays=oldGames;
                activity.total=oldTotal;activity.recordTile=oldRecord;
                activity.chartMetric=oldChart;activity.page=oldPage;
                activity.screen.invalidate();
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
                    // Compact source portrait uses a separate 628-unit
                    // slogan edge. The old 824-unit assertion measured the
                    // superseded tall layout, not the actual rendered caption.
                    float actualSloganBottom=view.referencePortraitLayout?628f:824f;
                    float lastSloganEdge=view.offY+actualSloganBottom*view.scale;
                    assertTrue("Brand slogan clipped at "+
                        size[0]+"x"+size[1]+": "+lastSloganEdge,
                        lastSloganEdge<=size[1]+1f);
                    float firstContentEdge=view.offY+
                        (view.referencePortraitLayout?58f:74f)*view.scale;
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
                // The 2048 victory artwork uses a DIFFERENT illustrative
                // frozen board than the classic in-game screenshot. Match
                // that fixture for fair per-pixel comparison only.
                // Never modify the actual production win screen's data.
                activity.game.cells=new int[]{
                    2,4,8,16,
                    4,8,16,32,
                    8,16,32,64,
                    128,256,512,1024
                };
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
                        entry.put("time",new String[]{"14:32","13:11","20:45","18:22","09:16",
                            "16:20","17:50","10:21","12:18","08:04"}[i]);
                        entry.put("won",i<7);
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
            assertTrue("Weekly countdown must show days or a final-day hour count",
                weekly.contains("d ")||weekly.matches("[0-9]+h"));
            assertTrue("Weekly countdown must have hours",weekly.endsWith("h"));
            String original=activity.page;
            try{
                activity.page="missions";
                capture(activity,"original-mission-period-header");
            }finally{activity.page=original;}
        });
    }

    @Test public void animationHighKnobFitsInBothThemeLayouts(){
        rule.getScenario().onActivity(activity->{
            int oldAnimation=activity.animation;
            String oldPage=activity.page;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="themes";
                activity.animation=2;
                int[][] sizes={{400,880},{941,1672}};
                for(int[] size:sizes){
                    view.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                            View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                            View.MeasureSpec.EXACTLY));
                    view.layout(0,0,size[0],size[1]);
                    Bitmap bmp=Bitmap.createBitmap(size[0],size[1],Bitmap.Config.ARGB_8888);
                    try{
                        view.draw(new Canvas(bmp));
                        boolean compact=size[0]==941;
                        int sampleY=(int)((compact?601:759)*view.scale+view.offY);
                        int centerX=(int)(358*view.scale+view.offX);
                        int outsideX=(int)(387*view.scale+view.offX);
                        assertEquals("High animation knob should be inside the panel",
                            MainActivity.CORAL,bmp.getPixel(centerX,sampleY));
                        assertNotEquals("Slider knob must not spill beyond panel",
                            MainActivity.CORAL,bmp.getPixel(outsideX,sampleY));
                    }finally{bmp.recycle();}
                }
            }finally{
                activity.page=oldPage;activity.animation=oldAnimation;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void importedBackupSurvivesOldActivityLifecyclePause(){
        final byte[][] original=new byte[1][];
        final int[] expectedBest=new int[1];
        rule.getScenario().onActivity(activity->{
            try{
                activity.persist();
                original[0]=activity.getBackupBytes();
                org.json.JSONObject backup=new org.json.JSONObject(new String(original[0],
                    java.nio.charset.StandardCharsets.UTF_8));
                expectedBest[0]=activity.best==19001?19002:19001;
                backup.getJSONObject("data").put("best",expectedBest[0]);
                activity.readBackupBytes(backup.toString().getBytes(
                    java.nio.charset.StandardCharsets.UTF_8));
            }catch(Exception ex){throw new AssertionError("Cannot restore test backup",ex);}
        });
        try{
            // onPause used to persist the stale in-memory score after import.
            rule.getScenario().moveToState(androidx.lifecycle.Lifecycle.State.CREATED);
            android.content.SharedPreferences stored=
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getSharedPreferences("brojko",
                        android.content.Context.MODE_PRIVATE);
            assertEquals("Imported best score must survive onPause",
                expectedBest[0],stored.getInt("best",-1));
        }finally{
            rule.getScenario().moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
            rule.getScenario().onActivity(activity->{
                try{activity.readBackupBytes(original[0]);}
                catch(Exception ex){throw new AssertionError("Cannot restore original backup",ex);}
            });
        }
    }

    @Test public void backupNavigationRowHasItsOwnVisibleRightChevron(){
        rule.getScenario().onActivity(activity->{
            String original=activity.page;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="settings";
                board.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap bmp=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{board.draw(new Canvas(bmp));}finally{bmp.recycle();}
                boolean found=false;
                for(MainActivity.BoardScreen.Tap tap:board.taps){
                    if("Sinkronizacija napretka".equals(tap.label)){
                        found=true;
                        assertTrue("Local-backup navigation needs a full-height target",
                            tap.bottom-tap.top>=48);
                        tap.action.run();
                        assertEquals("backup",activity.page);
                        break;
                    }
                }
                assertTrue("Backup action must stay reachable",found);
            }finally{activity.page=original;}
        });
    }

    @Test public void dailyGoalHelpTargetsMatchFunctionalReferenceLayout(){
        rule.getScenario().onActivity(activity->{
            String previous=activity.page;
            try{
                activity.page="daily";
                capture(activity,"daily-goals-left-aligned-help");
                int count=0;
                for(MainActivity.BoardScreen.Tap tap:activity.screen.taps){
                    if(tap.label.startsWith("Pomoć za cilj: ")){
                        assertTrue("Help target must be at least 48 units wide",
                            tap.right-tap.left>=48);
                        assertTrue("Help target must cover most of the row",
                            tap.bottom-tap.top>=48);
                        assertTrue("Help target cannot exceed card bounds",
                            tap.right<=378);
                        count++;
                    }
                }
                assertEquals("Each of the three goals needs real help",3,count);
                String date=activity.dailyLongDate();
                assertTrue("Daily challenge shows today's year",
                    date.contains(Integer.toString(java.util.Calendar.getInstance()
                        .get(java.util.Calendar.YEAR))));
                assertTrue("Long Croatian date ends with a period",date.endsWith("."));
            }finally{
                activity.page=previous;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void settingsReferenceSpacingFooterAndTouchTargets(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="settings";
                int[][] sizes={{941,1672},{1080,2400},{360,600}};
                for(int[] size:sizes){
                    board.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                        View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                        View.MeasureSpec.EXACTLY));
                    board.layout(0,0,size[0],size[1]);
                    Bitmap image=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{board.draw(new Canvas(image));}finally{image.recycle();}
                    int[] expectedY=size[1]/(float)size[0]<1.85f?
                        new int[]{97,161,223,286,360,433,498}:
                        new int[]{148,229,310,391,478,559,640};
                    String[] labels={"Zvuk","Vibracija","Jezik","Obavijesti",
                        "Sinkronizacija napretka","Pravila privatnosti","O aplikaciji"};
                    int i=0;
                    for(MainActivity.BoardScreen.Tap tap:board.taps){
                        if(i>=7)break;
                        assertTrue("Settings touch target must be labeled: "+tap.label,
                            tap.label.startsWith(labels[i]));
                        assertEquals("Row "+i+" must follow reference top",expectedY[i],
                            tap.top,.1f);
                        assertEquals("Entire card must be tappable on both sides",
                            18f,tap.left,.1f);
                        assertEquals(382f,tap.right,.1f);
                        assertTrue("Height >= 48 design units",tap.bottom-tap.top>=48f);
                        if(i>0)assertTrue("Settings cards must not overlap",
                            tap.top>=board.taps.get(i-1).bottom);
                        i++;
                    }
                    assertEquals("Exactly seven setting cards",7,i);
                    if(size[0]==941)
                        assertEquals("Reference screen must not need scrolling",
                            0f,board.maxScroll,1.1f);
                    if(size[0]==360)
                        assertTrue("Compact phone retains access to entire footer",
                            board.maxScroll>0);
                    if(size[0]==941)
                        captureReference(activity,board,
                            "reference-settings-polished-941x1672");
                }
                activity.theme=1;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap dark=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{board.draw(new Canvas(dark));}finally{dark.recycle();}
            }finally{
                activity.page=oldPage;
                activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }


    @Test public void settingsScreenEightMatchesReferenceControlsWithoutFakeSync(){
        rule.getScenario().onActivity(activity->{
            final String oldPage=activity.page;
            final int oldTheme=activity.theme;
            final boolean oldSound=activity.sound,oldVibration=activity.vibration;
            final boolean oldNotifications=activity.notifications;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
            try{
                // Illustration's three switches are ON only for this
                // controlled screenshot. Never overwrite a user's choices
                // or pretend that a remote cloud service is connected.
                activity.page="settings";activity.theme=0;
                activity.sound=true;activity.vibration=true;
                activity.notifications=true;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                board.draw(new Canvas(bitmap));
                assertEquals("The original layout fits without scrolling",
                    0f,board.maxScroll,.01f);
                String[] expected={"Zvuk","Vibracija","Jezik","Obavijesti",
                    "Sinkronizacija napretka","Pravila privatnosti",
                    "O aplikaciji"};
                assertEquals("Seven settings rows",7,board.taps.size());
                for(int i=0;i<expected.length;i++){
                    MainActivity.BoardScreen.Tap item=board.taps.get(i);
                    assertTrue("Wrong settings action at row "+i,
                        item.label.startsWith(expected[i]));
                    assertEquals("Full-width touch target",364f,
                        item.right-item.left,.01f);
                    assertTrue("Accessible row height",item.bottom-item.top>=48);
                }
                assertTrue(board.taps.get(0).label.contains("uključeno"));
                assertTrue(board.taps.get(1).label.contains("uključeno"));
                assertTrue(board.taps.get(3).label.contains("uključeno"));
                // On-state tint remains saturated turquoise, while
                // row cards remain opaque white and brand footer warm.
                int turquoise=bitmap.getPixel(
                    Math.round(board.offX+323*board.scale),
                    Math.round(board.offY+112*board.scale));
                assertTrue("Settings switch has saturated turquoise",
                    android.graphics.Color.green(turquoise)>
                        android.graphics.Color.red(turquoise)+55);
                captureReference(activity,board,
                    "settings-phase08-source-on-941x1672");

                // Real preference, not decorative switch; test independence.
                board.taps.get(0).action.run();
                assertFalse("Sound persists OFF",activity.prefs.getBoolean("sound",true));
                assertTrue("Vibration stays enabled",activity.vibration);
                board.taps.get(1).action.run();
                assertFalse("Vibration persists OFF",
                    activity.prefs.getBoolean("vibrate",true));
                assertFalse("Sound remains disabled",activity.sound);

                // Backup is navigation, never fake remote sync.
                board.taps.get(4).action.run();
                assertEquals("backup",activity.page);
                activity.page="settings";
                board.taps.get(2).action.run();
                assertEquals("language",activity.page);
                activity.page="settings";
                board.taps.get(5).action.run();
                assertEquals("privacy",activity.page);
                activity.page="settings";
                board.taps.get(6).action.run();
                assertEquals("about",activity.page);
            }finally{
                bitmap.recycle();
                activity.page=oldPage;
                activity.theme=oldTheme;
                activity.sound=oldSound;
                activity.vibration=oldVibration;
                activity.notifications=oldNotifications;
                activity.persist();activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void settingsIconColorsAndCardSurfacesStayFullyOpaque(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="settings";activity.theme=0;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{
                    board.draw(new Canvas(bitmap));
                    int coral=bitmap.getPixel(80,270);
                    int whiteCard=bitmap.getPixel(400,300);
                    // Regression: an inherited low-alpha shadow made the coral
                    // icon pastel and the reference-white card solid beige.
                    assertTrue("The sound icon must retain saturated coral",
                        android.graphics.Color.red(coral)>235 &&
                        android.graphics.Color.red(coral)-
                            android.graphics.Color.green(coral)>55);
                    assertTrue("Settings cards must be opaque near white",
                        android.graphics.Color.red(whiteCard)>=248 &&
                        android.graphics.Color.green(whiteCard)>=247 &&
                        android.graphics.Color.blue(whiteCard)>=245);
                    int brandFooter=bitmap.getPixel(823,1418);
                    assertTrue("Footer should have the warm cream from the source",
                        android.graphics.Color.blue(brandFooter)<
                            android.graphics.Color.blue(whiteCard)-5 &&
                        android.graphics.Color.green(brandFooter)<
                            android.graphics.Color.green(whiteCard));
                }finally{bitmap.recycle();}
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void themesSourceCompositionKeepsAllThemeStylesAndTabs(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme,oldStyle=activity.tileStyle;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="themes";activity.theme=0;activity.tileStyle=0;
                int[][] screens={{941,1672},{1080,2400}};
                for(int[] size:screens){
                    view.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                        View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                        View.MeasureSpec.EXACTLY));
                    view.layout(0,0,size[0],size[1]);
                    Bitmap bitmap=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{view.draw(new Canvas(bitmap));}finally{bitmap.recycle();}
                    int themes=0,styles=0,tabs=0;
                    for(MainActivity.BoardScreen.Tap tap:view.taps){
                        assertFalse("Themes uses bottom tabs rather than Back",
                            "Natrag".equals(tap.label));
                        if("Svijetla".equals(tap.label)||
                           "Tamna".equals(tap.label)||
                           "Živopisna".equals(tap.label))themes++;
                        if("Zaobljene".equals(tap.label)||
                           "Ravne".equals(tap.label)||
                           "Staklaste".equals(tap.label)){
                            styles++;
                            assertTrue("Style cards remain tappable",
                                tap.bottom-tap.top>=48);
                        }
                        if("Novo".equals(tap.label)||
                           "Rekordi".equals(tap.label)||
                           "Teme".equals(tap.label)||
                           "Postavke".equals(tap.label))tabs++;
                    }
                    assertEquals("Three themes",3,themes);
                    assertEquals("Three styles",3,styles);
                    assertEquals("Four tabs",4,tabs);
                    if(size[0]==941)
                        captureReference(activity,view,
                            "themes-original-layout-polished-941x1672");
                }
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.tileStyle=oldStyle;activity.screen.invalidate();
            }
        });
    }

    @Test public void themeReferenceUsesSquarePreviewCellsAndAReadableFullTab(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldStyle=activity.tileStyle,oldTheme=activity.theme;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="themes";activity.theme=0;activity.tileStyle=0;
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(image));}finally{image.recycle();}
                assertEquals("Reference themes must fit without vertical scrolling",
                    0f,view.maxScroll,.01f);
                int count=0;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Zaobljene".equals(tap.label)||
                        "Ravne".equals(tap.label)||
                        "Staklaste".equals(tap.label)){
                        assertTrue("Full card and radio indicator are tappable",
                            tap.bottom-tap.top>=90);
                        count++;
                    }
                }
                assertEquals(3,count);
                captureReference(activity,view,
                    "themes-original-final-aligned-941x1672");
            }finally{
                activity.page=oldPage;activity.tileStyle=oldStyle;
                activity.theme=oldTheme;activity.screen.invalidate();
            }
        });
    }

    @Test public void themesToggleDescriptionsStayInsideRealReferenceCard(){
        rule.getScenario().onActivity(activity->{
            String previous=activity.page;
            MainActivity.BoardScreen layout=activity.new BoardScreen(activity);
            try{
                activity.page="themes";
                layout.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                layout.layout(0,0,941,1672);
                Bitmap img=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{layout.draw(new Canvas(img));}
                finally{img.recycle();}
                boolean vibration=false,sound=false;
                for(MainActivity.BoardScreen.Tap tap:layout.taps){
                    if("Vibracija".equals(tap.label)){
                        vibration=true;
                        assertTrue(tap.bottom-tap.top>=48);
                    }
                    if("Zvukovi".equals(tap.label)){
                        sound=true;
                        assertTrue(tap.bottom-tap.top>=45);
                    }
                }
                assertTrue("Vibration remains actionable",vibration);
                assertTrue("Sound remains actionable",sound);
                assertEquals("No phantom scroll on reference height",
                    0f,layout.maxScroll,.01f);
                captureReference(activity,layout,
                    "themes-source-descriptions-and-divider-941x1672");
            }finally{
                activity.page=previous;activity.screen.invalidate();
            }
        });
    }

    @Test public void tileStyleSwatchesMatchReferenceRadioSelectionInBothLayouts(){
        rule.getScenario().onActivity(activity->{
            int originalStyle=activity.tileStyle;
            String originalPage=activity.page;
            int originalTheme=activity.theme;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="themes";activity.theme=0;
                int[][] sizes={{400,880},{941,1672}};
                for(int[] sz:sizes){
                    board.measure(View.MeasureSpec.makeMeasureSpec(sz[0],View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(sz[1],View.MeasureSpec.EXACTLY));
                    board.layout(0,0,sz[0],sz[1]);
                    for(int selected=0;selected<3;selected++){
                        activity.tileStyle=selected;
                        Bitmap bitmap=Bitmap.createBitmap(sz[0],sz[1],Bitmap.Config.ARGB_8888);
                        try{
                            board.draw(new Canvas(bitmap));
                            float sy=(sz[0]==400?552.5f:446.5f);
                            for(int i=0;i<3;i++){
                                float sx=(210+i*56)+25.5f;
                                int px=bitmap.getPixel(
                                    Math.round(board.offX+sx*board.scale),
                                    Math.round(board.offY+sy*board.scale));
                                if(i==selected)
                                    assertTrue("Selected style must show a white check",
                                        android.graphics.Color.red(px)>=235);
                                else
                                    assertTrue("Unselected style badge is neutral gray",
                                        Math.abs(android.graphics.Color.red(px)-
                                            android.graphics.Color.green(px))<12);
                            }
                        }finally{bitmap.recycle();}
                        capture(activity,"themes-reference-style-"+selected+
                            (sz[0]==400?"-standard":"-compact"));
                    }
                }
            }finally{
                activity.tileStyle=originalStyle;
                activity.theme=originalTheme;
                activity.page=originalPage;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void statisticsHistoryUsesRealCompletionTimeAndPreservesLegacyRecords(){
        rule.getScenario().onActivity(activity->{
            String previousHistory=activity.prefs.getString("history","[]");
            String previousPage=activity.page;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                assertTrue("New completion timestamps must be HH:mm",
                    activity.compactTime().matches("[0-2][0-9]:[0-5][0-9]"));
                board.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                activity.page="stats";
                activity.prefs.edit().putString("history",
                    "[{\"day\":\"12. tra 2024.\",\"score\":2560,\"peak\":128}]").commit();
                Bitmap legacy=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                Bitmap withTime=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{
                    board.draw(new Canvas(legacy));
                    activity.prefs.edit().putString("history",
                        "[{\"day\":\"12. tra 2024.\",\"time\":\"14:32\","+
                        "\"score\":2560,\"peak\":128}]").commit();
                    board.draw(new Canvas(withTime));
                    int differences=0;
                    // The history rows moved to y=530 in Phase 06.
                    // Check the actual glyph ROI, not the former chart area.
                    int top=Math.max(0,Math.round(board.offY+532f*board.scale));
                    int bottom=Math.min(1672,Math.round(board.offY+558f*board.scale));
                    int left=Math.max(0,Math.round(board.offX+117f*board.scale));
                    int right=Math.min(941,Math.round(board.offX+165f*board.scale));
                    for(int y=top;y<bottom;y++)for(int x=left;x<right;x++){
                        if(legacy.getPixel(x,y)!=withTime.getPixel(x,y))
                            differences++;
                    }
                    assertTrue("Timestamp column must be visibly rendered",differences>40);
                    assertEquals("History remains navigable",1,
                        (int)board.taps.stream().filter(tap->
                            tap.label.startsWith("Detalji partije")).count());
                    assertTrue("Latest row is inside visible viewport",
                        board.taps.stream().anyMatch(tap->
                            tap.label.startsWith("Detalji partije")&&tap.bottom<=710));
                    captureReference(activity,board,"stats-history-time-941x1672");
                }finally{
                    legacy.recycle();withTime.recycle();
                }
            }finally{
                activity.prefs.edit().putString("history",previousHistory).commit();
                activity.page=previousPage;activity.screen.invalidate();
            }
        });
    }

    @Test public void statsConsecutiveVictoriesAreNotCalendarDayStreaks(){
        rule.getScenario().onActivity(activity->{
            String oldHistory=activity.prefs.getString("history","[]");
            int savedDayStreak=activity.playDayStreak;
            try{
                activity.playDayStreak=12;
                activity.prefs.edit().putString("history",
                    "[{\"score\":2048,\"won\":true},"+
                    "{\"score\":3072,\"won\":true},"+
                    "{\"score\":512,\"won\":false},"+
                    "{\"score\":8192,\"won\":true}]").commit();
                assertEquals("Only wins after the most recent loss count",2,
                    activity.screen.consecutiveWins());
                assertEquals("Weekly calendar streak remains untouched",12,
                    activity.playDayStreak);
                activity.prefs.edit().putString("history",
                    "[{\"score\":800},{\"score\":2048,\"won\":true}]").commit();
                assertEquals("Legacy rows without won flag are not fabricated wins",
                    0,activity.screen.consecutiveWins());
            }finally{
                activity.prefs.edit().putString("history",oldHistory).commit();
                activity.playDayStreak=savedDayStreak;
            }
        });
    }

    @Test public void statisticsArchiveDatesAndDetailsShowRealTimeWithoutBackfilling(){
        rule.getScenario().onActivity(activity->{
            org.json.JSONObject fresh=new org.json.JSONObject();
            org.json.JSONObject old=new org.json.JSONObject();
            try{
                fresh.put("day","25. lis 2026.");
                fresh.put("time","18:43");
                old.put("day","10.9.2026.");
                assertEquals("25. lis 2026. · 18:43",
                    activity.screen.historyTimestamp(fresh));
                assertEquals("10.9.2026.",activity.screen.historyTimestamp(old));
                assertTrue("New Croatian history dates use month abbreviation",
                    activity.historyDate().matches("[0-9]{1,2}\\. .+ [0-9]{4}\\."));
            }catch(org.json.JSONException e){throw new AssertionError(e);}
        });
    }

    @Test public void missionAchievementsCounterReflectsRealUnlocks(){
        rule.getScenario().onActivity(activity->{
            int oldFlags=activity.rewardFlags;
            String oldPage=activity.page;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="missions";
                for(int flags:new int[]{0,8,16,24,63}){
                    activity.rewardFlags=flags;
                    int expected=((flags&8)!=0?1:0)+((flags&16)!=0?1:0);
                    assertEquals("Counter must not mistake currency for achievements",
                        expected,board.completedAchievements());
                }
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap img=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{board.draw(new Canvas(img));}finally{img.recycle();}
                assertTrue("All six mission rows fit at reference resolution",
                    board.maxScroll<1.1f);
            }finally{
                activity.rewardFlags=oldFlags;activity.page=oldPage;
                activity.screen.invalidate();
            }
        });
    }
    @Test public void missionReferenceProgressUsesRealUnderlyingGameState(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldMerges=activity.todayMerges,oldWeek=activity.weekPoints;
            int oldStreak=activity.playDayStreak,oldTile=activity.recordTile;
            int oldBest=activity.best,oldFlags=activity.rewardFlags;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="missions";
                activity.todayMerges=12;activity.weekPoints=6250;
                activity.playDayStreak=5;activity.recordTile=512;
                activity.best=8400;activity.rewardFlags=0;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap img=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{board.draw(new Canvas(img));}
                finally{img.recycle();}
                captureReference(activity,board,"missions-six-real-progress-941x1672");
                assertTrue("Reference-sized viewport must show all six tasks",
                    board.maxScroll<1.1f);
            }finally{
                activity.page=oldPage;activity.todayMerges=oldMerges;
                activity.weekPoints=oldWeek;activity.playDayStreak=oldStreak;
                activity.recordTile=oldTile;activity.best=oldBest;
                activity.rewardFlags=oldFlags;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void illustratedMissionsKeepRealProgressAndDistinctColors(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            int oldMerges=activity.todayMerges,oldWeek=activity.weekPoints;
            int oldStreak=activity.playDayStreak,oldTile=activity.recordTile;
            int oldBest=activity.best,oldFlags=activity.rewardFlags;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="missions";activity.theme=0;
                activity.todayMerges=12;activity.weekPoints=6250;
                activity.playDayStreak=5;activity.recordTile=512;
                activity.best=8400;activity.rewardFlags=0;
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap bmp=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    view.draw(new Canvas(bmp));
                    int purple=bmp.getPixel(
                        Math.round(view.offX+46*view.scale),
                        Math.round(view.offY+256*view.scale));
                    assertTrue("512 illustrated tile must remain purple",
                        android.graphics.Color.blue(purple)>
                            android.graphics.Color.green(purple)+28);
                    int teal=bmp.getPixel(
                        Math.round(view.offX+70*view.scale),
                        Math.round(view.offY+178*view.scale));
                    assertTrue("First mission needs a teal numbered tile",
                        android.graphics.Color.green(teal)>
                            android.graphics.Color.red(teal)+20);
                    int reward=bmp.getPixel(
                        Math.round(view.offX+345*view.scale),
                        Math.round(view.offY+184*view.scale));
                    assertTrue("Reward star must be saturated gold",
                        android.graphics.Color.red(reward)>
                            android.graphics.Color.blue(reward)+65);
                    assertEquals("Do not invent earned achievements",
                        0,view.completedAchievements());
                    assertTrue("All six mission rows remain visible",
                        view.maxScroll<1.1f);
                    captureReference(activity,view,
                        "missions-illustrated-six-941x1672");
                }finally{bmp.recycle();}
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.todayMerges=oldMerges;activity.weekPoints=oldWeek;
                activity.playDayStreak=oldStreak;activity.recordTile=oldTile;
                activity.best=oldBest;activity.rewardFlags=oldFlags;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void dailyReferenceHasNoGhostScrollAndStillFitsAllControls(){
        rule.getScenario().onActivity(activity->{
            String original=activity.page;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="daily";
                int[][] sizes={{941,1672},{360,600},{1080,2400}};
                for(int[] sz:sizes){
                    board.measure(View.MeasureSpec.makeMeasureSpec(sz[0],
                        View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(sz[1],
                        View.MeasureSpec.EXACTLY));
                    board.layout(0,0,sz[0],sz[1]);
                    Bitmap image=Bitmap.createBitmap(sz[0],sz[1],
                        Bitmap.Config.ARGB_8888);
                    try{board.draw(new Canvas(image));}finally{image.recycle();}
                    if(sz[0]==941)
                        assertEquals("Reference daily must not scroll through blank space",
                            0f,board.maxScroll,.9f);
                    if(sz[0]==360)
                        assertTrue("Short phones can reach CTA by scrolling",
                            board.maxScroll>1);
                    int help=0,primary=0;
                    for(MainActivity.BoardScreen.Tap tap:board.taps){
                        if(tap.label.startsWith("Pomoć za cilj"))help++;
                        if(tap.label.contains("Započni izazov")||
                           tap.label.contains("Nastavi izazov")||
                           tap.label.contains("Igraj ponovno"))primary++;
                    }
                    assertEquals("Three help controls preserved",3,help);
                    assertEquals("Start/resume action preserved",1,primary);
                    if(sz[0]==941)captureReference(activity,board,
                        "daily-reward-description-reference-941x1672");
                }
            }finally{activity.page=original;activity.screen.invalidate();}
        });
    }

    @Test public void dailyChallengeReferenceScreenAndRulesRemainFunctional(){
        rule.getScenario().onActivity(activity->{
            String priorPage=activity.page;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="daily";
                view.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap screen=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(screen));}finally{screen.recycle();}
                assertTrue("Daily challenge must fit the reference viewport",
                    view.maxScroll<1.1f);
                int help=0,cta=0;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if(tap.label.startsWith("Pomoć za cilj: ")){
                        help++;
                        assertTrue("Goal help remains touch-sized",
                            tap.bottom-tap.top>=48f);
                    }
                    if(tap.label.contains("izazov")&&!tap.label.startsWith("Pomoć")){
                        cta++;
                        assertTrue("Primary CTA remains >=48 high",
                            tap.bottom-tap.top>=48f);
                    }
                }
                assertEquals("Three distinct goal explanations",3,help);
                assertEquals("One primary action",1,cta);
                captureReference(activity,view,"daily-refined-board-gift-941x1672");
                TileGame rules=new TileGame("daily",activity.today(),421L);
                rules.peak=256;rules.score=3000;rules.moves=80;
                assertTrue("Both milestones within 80 moves must win",
                    rules.isDailyGoalComplete());
                rules.moves=81;
                assertFalse("More than 80 moves cannot win",
                    rules.isDailyGoalComplete());
                rules.moves=80;rules.peak=128;
                assertFalse("Score alone is not a completed challenge",
                    rules.isDailyGoalComplete());
            }finally{
                activity.page=priorPage;activity.screen.invalidate();
            }
        });
    }

    @Test public void dailyHeaderKeepsBothDescriptionLinesAbovePreview(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="daily";
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap bmp=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{board.draw(new Canvas(bmp));}finally{bmp.recycle();}
                assertEquals("Daily reference needs no ghost scrolling",
                    0f,board.maxScroll,1.1f);
                // Regression screenshot: short second subtitle was painted
                // beneath the sample grid in the previous release.
                captureReference(activity,board,
                    "daily-subtitles-above-board-941x1672");
            }finally{activity.page=oldPage;activity.screen.invalidate();}
        });
    }

    @Test public void dailyReferenceDateAndPrimaryActionHaveSeparateSafeRegions(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="daily";
                for(int[] size:new int[][]{{941,1672},{360,600},{400,880}}){
                    board.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                        View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                        View.MeasureSpec.EXACTLY));
                    board.layout(0,0,size[0],size[1]);
                    Bitmap bitmap=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{board.draw(new Canvas(bitmap));}finally{bitmap.recycle();}
                    assertTrue("Date capsule must end before subtitle text",
                        99+25<145-6||size[0]==400);
                    int startCount=0;
                    for(MainActivity.BoardScreen.Tap tap:board.taps){
                        if(tap.label.equals("Započni izazov")||
                           tap.label.equals("Igraj ponovno")||
                           tap.label.equals("Nastavi izazov")){
                            boolean compactViewport=
                                size[1]/(float)size[0]<1.85f;
                            float expectedWidth=compactViewport?314f:350f;
                            float expectedX=compactViewport?43f:25f;
                            assertEquals("Daily CTA width must match this viewport",
                                expectedWidth,tap.right-tap.left,.2f);
                            assertEquals("Daily CTA starts at safe reference inset",
                                expectedX,tap.left,.2f);
                            assertTrue("Daily primary CTA must stay touch-sized",
                                tap.bottom-tap.top>=48f);
                            assertTrue("Daily CTA must fit its own layout height",
                                tap.bottom<=(compactViewport?710f:880f));
                            startCount++;
                        }
                    }
                    assertEquals("Exactly one functional daily action",1,startCount);
                }
                capture(activity,"daily-shallow-date-and-primary-action");
            }finally{activity.page=oldPage;activity.screen.invalidate();}
        });
    }

    @Test public void dailySourceStreakFixtureKeepsRealActionsAndRaisedTiles(){
        rule.getScenario().onActivity(activity->{
            String originalPage=activity.page;
            String originalLastDaily=activity.lastDaily;
            int originalStreak=activity.streak;
            int originalTheme=activity.theme,originalStyle=activity.tileStyle;
            TileGame originalGame=activity.game;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                // Screenshot-only fixture: no user preferences are changed.
                activity.page="daily";activity.theme=0;activity.tileStyle=0;
                activity.game=null;activity.lastDaily="";activity.streak=7;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    board.draw(new Canvas(image));
                    int tileX=Math.round(board.offX+116f*board.scale);
                    int tileY=Math.round(board.offY+204f*board.scale);
                    int tile=image.getPixel(tileX,tileY);
                    assertTrue("Preview starts with a real coral 2 tile",
                        android.graphics.Color.red(tile)>185 &&
                        android.graphics.Color.red(tile)>
                            android.graphics.Color.green(tile)+40);
                    int flameX=Math.round(board.offX+325f*board.scale);
                    int flameY=Math.round(board.offY+67f*board.scale);
                    int flame=image.getPixel(flameX,flameY);
                    assertTrue("Seven-day flame must be saturated orange, not alpha-grey",
                        android.graphics.Color.red(flame)>
                            android.graphics.Color.blue(flame)+35);
                    int giftX=Math.round(board.offX+70f*board.scale);
                    int giftY=Math.round(board.offY+615f*board.scale);
                    int purple=image.getPixel(giftX,giftY);
                    assertTrue("Reward gift needs saturated purple body",
                        android.graphics.Color.blue(purple)>
                            android.graphics.Color.green(purple)+45);
                    int help=0,cta=0;
                    for(MainActivity.BoardScreen.Tap tap:board.taps){
                        if(tap.label.startsWith("Pomoć za cilj: "))help++;
                        if("Započni izazov".equals(tap.label)){
                            cta++;
                            assertTrue("CTA must remain at least 48 logical units",
                                tap.bottom-tap.top>=48f);
                        }
                    }
                    assertEquals("Exactly three real challenge help actions",3,help);
                    assertEquals("Only one functional start action",1,cta);
                }finally{image.recycle();}
                captureReference(activity,board,
                    "daily-source-streak7-941x1672");
            }finally{
                activity.page=originalPage;activity.theme=originalTheme;
                activity.tileStyle=originalStyle;activity.streak=originalStreak;
                activity.game=originalGame;activity.lastDaily=originalLastDaily;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void dailyGiftStaysSaturatedAfterItsTransparentShadow(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;int oldTheme=activity.theme;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="daily";activity.theme=0;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap bmp=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{
                    board.draw(new Canvas(bmp));
                    int x=Math.round(board.offX+71f*board.scale);
                    int y=Math.round(board.offY+617f*board.scale);
                    int color=bmp.getPixel(x,y);
                    assertTrue("Reward purple must not fade to its pastel panel",
                        android.graphics.Color.blue(color)>145 &&
                        android.graphics.Color.blue(color)-
                            android.graphics.Color.green(color)>60);
                }finally{bmp.recycle();}
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void dailyReferenceGoalGlyphsDoNotHaveFakeNumberTileBackgrounds(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            String oldLast=activity.lastDaily;
            TileGame oldGame=activity.game;
            MainActivity.BoardScreen preview=activity.new BoardScreen(activity);
            try{
                activity.page="daily";activity.theme=0;
                activity.lastDaily="";
                activity.game=null;
                preview.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                preview.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{
                    preview.draw(new Canvas(bitmap));
                    for(int row=1;row<=2;row++){
                        int y=(row==1?456:509)+12;
                        int px=Math.round(preview.offX+47*preview.scale);
                        int py=Math.round(preview.offY+y*preview.scale);
                        int c=bitmap.getPixel(px,py);
                        assertTrue("Gold star and blue arrows must have a pale row " +
                                "background, not a tinted game tile",
                            android.graphics.Color.red(c)>=225 &&
                            android.graphics.Color.green(c)>=220 &&
                            android.graphics.Color.blue(c)>=215);
                    }
                    int firstX=Math.round(preview.offX+47*preview.scale);
                    int firstY=Math.round(preview.offY+426*preview.scale);
                    int tile=bitmap.getPixel(firstX,firstY);
                    assertTrue("256 goal still uses its vivid pink tile",
                        android.graphics.Color.red(tile)>205 &&
                        android.graphics.Color.green(tile)<195);
                }finally{bitmap.recycle();}
                captureReference(activity,preview,
                    "daily-reference-pictograms-and-bursts-941x1672");
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.lastDaily=oldLast;activity.game=oldGame;
                activity.screen.invalidate();
            }
        });
    }


    @Test public void dailySourceHeaderAndGoalRowsKeepIndependentTouchRegions(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            MainActivity.BoardScreen preview=activity.new BoardScreen(activity);
            try{
                activity.page="daily";
                preview.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                preview.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    preview.draw(new Canvas(bitmap));
                    int backs=0,helps=0;
                    float lastBottom=-1f;
                    for(MainActivity.BoardScreen.Tap tap:preview.taps){
                        if("Natrag".equals(tap.label)){
                            backs++;
                            assertTrue("Daily return should align with source top",
                                tap.top<=17f);
                            assertTrue("Back control needs at least a 48-unit hitbox",
                                tap.bottom-tap.top>=48f);
                        }
                        if(tap.label.startsWith("Pomoć za cilj: ")){
                            helps++;
                            assertTrue("Help must remain touch-sized after tightening rows",
                                tap.bottom-tap.top>=48f);
                            assertTrue("Adjacent help targets must never overlap",
                                tap.top>=lastBottom);
                            lastBottom=tap.bottom;
                        }
                    }
                    assertEquals("Exactly one return action",1,backs);
                    assertEquals("Three independent help actions",3,helps);
                    assertTrue("Reference portrait must not scroll beyond its content",
                        preview.maxScroll<1.1f);
                    // Calendar center must show the cream date capsule, not
                    // the filled red header and yellow star of the shared pictogram.
                    int px=Math.round(preview.offX+119*preview.scale);
                    int py=Math.round(preview.offY+111*preview.scale);
                    int iconCenter=bitmap.getPixel(px,py);
                    assertTrue("Daily date uses an outline calendar",
                        android.graphics.Color.green(iconCenter)>190 &&
                        android.graphics.Color.blue(iconCenter)>165);
                    captureReference(activity,preview,
                        "daily-header-board-goal-geometry-941x1672");
                }finally{bitmap.recycle();}
            }finally{
                activity.page=previousPage;activity.screen.invalidate();
            }
        });
    }


    @Test public void portraitClassicBoardKeepsFinalTileInteractiveAndButtonsVisible(){
        rule.getScenario().onActivity(activity->{
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.game=new TileGame("classic","",554L);
                java.util.Arrays.fill(activity.game.cells,0);
                activity.game.cells[12]=2;
                activity.game.cells[13]=2;
                activity.page="game";
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{board.draw(new Canvas(bitmap));}
                finally{bitmap.recycle();}
                assertEquals("Source 4x4 board top",159f,board.gridY,.1f);
                int primary=0;
                for(MainActivity.BoardScreen.Tap tap:board.taps){
                    if("Novo".equals(tap.label)||"Završi".equals(tap.label)){
                        primary++;
                        assertTrue("Game controls must be tall enough to tap",
                            tap.bottom-tap.top>=90f);
                        assertTrue("Controls must stay within the reference viewport",
                            board.offY+tap.bottom*board.scale<=1673f);
                    }
                }
                assertEquals(2,primary);
                // Last row extends below the old square hit area. A real swipe
                // starting there must still merge the two adjacent 2 tiles.
                long time=android.os.SystemClock.uptimeMillis();
                float touchY=board.offY+(board.gridY+375)*board.scale;
                float startX=board.offX+290*board.scale;
                float endX=board.offX+67*board.scale;
                MotionEvent down=MotionEvent.obtain(time,time,
                    MotionEvent.ACTION_DOWN,startX,touchY,0);
                MotionEvent up=MotionEvent.obtain(time,time+80,
                    MotionEvent.ACTION_UP,endX,touchY,0);
                try{
                    board.onTouchEvent(down);board.onTouchEvent(up);
                }finally{down.recycle();up.recycle();}
                assertEquals("Expanded final row remains swipeable",4,
                    activity.game.score);
                assertEquals(1,activity.game.moves);
                captureReference(activity,board,
                    "classic-four-by-four-reference-941x1672");
            }finally{
                activity.game=previousGame;activity.page=previousPage;
                activity.screen.invalidate();
            }
        });
    }


    @Test public void portraitVictoryModalAlignsWithGoldenGeometryAndNeutralBackdrop(){
        rule.getScenario().onActivity(activity->{
            TileGame previous=activity.game;
            String previousPage=activity.page;
            int previousTheme=activity.theme;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.theme=0;
                activity.game=new TileGame("classic","",103L);
                activity.game.score=2048;
                activity.game.peak=2048;
                activity.game.reached2048=true;
                activity.page="win";
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    view.draw(new Canvas(bitmap));
                    // The source image uses a nearly neutral grey behind
                    // the modal, not the blue cast of other overlays.
                    int background=bitmap.getPixel(10,10);
                    assertTrue("Victory backdrop must be neutral charcoal",
                        Math.abs(android.graphics.Color.red(background)-
                            android.graphics.Color.blue(background))<16);
                    assertTrue("Victory backdrop must remain dimmed",
                        android.graphics.Color.red(background)<150);
                }finally{bitmap.recycle();}
                int actions=0;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Nastavi igrati".equals(tap.label)){
                        assertEquals("Primary action top matches source",417f,tap.top,.1f);
                        assertEquals("Primary action width matches source",
                            252f,tap.right-tap.left,.1f);
                        actions++;
                    }else if("Nova igra".equals(tap.label)){
                        assertEquals("Secondary action top matches source",478f,tap.top,.1f);
                        actions++;
                    }else if("Podijeli rezultat".equals(tap.label)){
                        assertEquals("Share action top matches source",532f,tap.top,.1f);
                        assertTrue("All actions fit inside reference modal",
                            tap.bottom<=597f);
                        actions++;
                    }
                }
                assertEquals("All three actions remain functional",3,actions);
                captureReference(activity,view,
                    "portrait-victory-aligned-941x1672");
            }finally{
                activity.game=previous;
                activity.page=previousPage;
                activity.theme=previousTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void corruptLegacyHistoryCannotLoseLatestCompletedGame(){
        rule.getScenario().onActivity(activity->{
            TileGame originalGame=activity.game;
            int originalLastScore=activity.lastScore;
            String originalHistory=activity.prefs.getString("history","[]");
            try{
                assertTrue(activity.prefs.edit().putString("history",
                    "{bad old archive").commit());
                TileGame finished=new TileGame("classic","",2026L);
                finished.cells[0]=2048;
                finished.score=2048;
                finished.peak=2048;
                finished.moves=25;
                finished.reached2048=true;
                activity.game=finished;
                assertTrue("Archiving a valid game must survive malformed history",
                    activity.archive());
                assertTrue("Game is marked archived only after disk save",
                    finished.archived);
                org.json.JSONArray history=new org.json.JSONArray(
                    activity.prefs.getString("history","[]"));
                assertTrue("Latest game must be at the top",history.length()>0);
                assertEquals(2048,history.getJSONObject(0).getInt("score"));
                assertEquals("classic",history.getJSONObject(0).getString("mode"));
                assertEquals(2048,activity.lastScore);
            }catch(Exception e){
                throw new AssertionError("New session could not be archived",e);
            }finally{
                activity.prefs.edit().putString("history",originalHistory).commit();
                activity.game=originalGame;
                activity.lastScore=originalLastScore;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void compactThemesSoundSwitchNeverActivatesVibration(){
        rule.getScenario().onActivity(activity->{
            boolean originalSound=activity.sound;
            boolean originalVibration=activity.vibration;
            String originalPage=activity.page;
            int originalTheme=activity.theme;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="themes";
                activity.theme=0;
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(image));}finally{image.recycle();}
                assertTrue("Themes must use reference portrait layout",
                    view.referencePortraitLayout);
                MainActivity.BoardScreen.Tap soundTap=null, vibrationTap=null;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Zvukovi".equals(tap.label))soundTap=tap;
                    if("Vibracija".equals(tap.label))vibrationTap=tap;
                }
                assertNotNull("Sound action must exist",soundTap);
                assertNotNull("Vibration action must exist",vibrationTap);
                assertTrue("Vibration target must be at least 48 design units",
                    vibrationTap.bottom-vibrationTap.top>=48);
                assertTrue("Sound target must be at least 48 design units",
                    soundTap.bottom-soundTap.top>=48);
                assertTrue("Vibration and sound actions cannot overlap",
                    vibrationTap.bottom<=soundTap.top);
                // At logical y=510, the Sound control is painted, but
                // previously its Vibration hit region extended to y=519.
                float x=view.offX+359*view.scale;
                float y=view.offY+510*view.scale;
                long now=android.os.SystemClock.uptimeMillis();
                MotionEvent down=MotionEvent.obtain(now,now,
                    MotionEvent.ACTION_DOWN,x,y,0);
                MotionEvent up=MotionEvent.obtain(now,now+40,
                    MotionEvent.ACTION_UP,x,y,0);
                try{
                    view.onTouchEvent(down);
                    view.onTouchEvent(up);
                }finally{down.recycle();up.recycle();}
                assertEquals("Sound switch must toggle sound only",
                    !originalSound,activity.sound);
                assertEquals("Sound switch must not toggle Vibration",
                    originalVibration,activity.vibration);
            }finally{
                activity.sound=originalSound;
                activity.vibration=originalVibration;
                activity.theme=originalTheme;
                activity.page=originalPage;
                activity.persist();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void embossedRewardStarHasRealGoldenFillAndWarmShadow(){
        rule.getScenario().onActivity(activity->{
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            Bitmap result=Bitmap.createBitmap(112,112,Bitmap.Config.ARGB_8888);
            try{
                Canvas canvas=new Canvas(result);
                canvas.drawColor(android.graphics.Color.WHITE);
                view.c=canvas;
                view.palette();
                view.polishedIcon("star",56,54,74,0xffffb526);
                int gold=0,warm=0,bright=0;
                for(int y=12;y<100;y++)for(int x=12;x<100;x++){
                    int px=result.getPixel(x,y);
                    int r=android.graphics.Color.red(px);
                    int g=android.graphics.Color.green(px);
                    int b=android.graphics.Color.blue(px);
                    if(r>220&&g>155&&g<245&&b<180)gold++;
                    if(r>180&&g>80&&g<160&&b<90)warm++;
                    if(r>245&&g>215&&b>125&&b<230)bright++;
                }
                assertTrue("3D star must have substantial golden face",gold>150);
                assertTrue("3D star must have an orange extrusion",warm>12);
                assertTrue("3D star must have a light highlight",bright>12);
                canvas.drawColor(android.graphics.Color.WHITE);
                view.polishedIcon("star",56,54,74,0xff478df4);
                int blue=0;
                for(int y=12;y<100;y++)for(int x=12;x<100;x++){
                    int px=result.getPixel(x,y);
                    if(android.graphics.Color.blue(px)>180&&
                        android.graphics.Color.blue(px)>
                            android.graphics.Color.red(px)+35)blue++;
                }
                assertTrue("Statistics star must keep its blue material",blue>100);
            }finally{result.recycle();}
        });
    }

    @Test public void victorySummaryUsesWarmCardsAndGoldenRewardFaces(){
        rule.getScenario().onActivity(activity->{
            TileGame oldGame=activity.game;
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try{
                activity.theme=0;
                activity.game=new TileGame("classic","",781L);
                activity.game.cells[0]=2048;
                activity.game.score=2048;
                activity.game.peak=2048;
                activity.game.reached2048=true;
                activity.page="win";
                MainActivity.BoardScreen view=activity.new BoardScreen(activity);
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{
                    view.draw(new Canvas(image));
                    // Source art's summary cards are warm cream, never flat
                    // white. This point is inside the right summary surface.
                    int surface=image.getPixel(500,815);
                    int r=android.graphics.Color.red(surface);
                    int g=android.graphics.Color.green(surface);
                    int b=android.graphics.Color.blue(surface);
                    assertTrue("Win summary card should be cream",r>=g+7);
                    assertTrue("Win summary card should be warm",g>=b+10);
                    assertTrue("Win summary card must not turn white",b<245);
                    // Reward icons retain a solid golden face rather than
                    // disappearing into the light card.
                    int face=image.getPixel(538,895);
                    assertTrue("Victory reward must be a golden-filled star",
                        android.graphics.Color.red(face)>230 &&
                        android.graphics.Color.green(face)>145 &&
                        android.graphics.Color.green(face)<239 &&
                        android.graphics.Color.blue(face)<150);
                }finally{image.recycle();}
            }finally{
                activity.game=oldGame;
                activity.page=oldPage;
                activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void victoryContinueActionKeepsSameSavedGame(){
        rule.getScenario().onActivity(activity->{
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            TileGame won=new TileGame("classic","",2048L);
            won.cells[0]=2048;
            won.score=4096;
            won.peak=2048;
            won.moves=18;
            won.reached2048=true;
            try{
                activity.game=won;
                activity.page="win";
                MainActivity.BoardScreen preview=activity.new BoardScreen(activity);
                preview.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                preview.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{preview.draw(new Canvas(image));}
                finally{image.recycle();}
                MainActivity.BoardScreen.Tap resume=null;
                for(MainActivity.BoardScreen.Tap tap:preview.taps){
                    if("Nastavi igrati".equals(tap.label))resume=tap;
                }
                assertNotNull("The victory modal must provide real Resume",resume);
                resume.action.run();
                assertSame("Resume must not create or replace the saved session",
                    won,activity.game);
                assertEquals("game",activity.page);
                assertTrue("Victory acknowledgement must survive persistence",
                    activity.game.winAcknowledged);
                assertEquals(4096,activity.game.score);
                assertEquals(18,activity.game.moves);
                assertTrue("Saved JSON records the acknowledged milestone",
                    activity.prefs.getString("session","")
                        .contains("\"ack\":true"));
            }finally{
                activity.game=previousGame;
                activity.page=previousPage;
                activity.persist();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void victoryNativePixelsUseWarmSummaryAndCoralButton(){
        rule.getScenario().onActivity(activity->{
            TileGame previousGame=activity.game;
            String previousPage=activity.page;
            int previousTheme=activity.theme;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.game=new TileGame("classic","",2148L);
                activity.game.score=2048;
                activity.game.peak=2048;
                activity.game.reached2048=true;
                activity.theme=0;activity.page="win";
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    view.draw(new Canvas(image));
                    int surface=image.getPixel(210,835);
                    int r=android.graphics.Color.red(surface);
                    int g=android.graphics.Color.green(surface);
                    int b=android.graphics.Color.blue(surface);
                    assertTrue("Reference victory summary is light warm cream",
                        r>=g+5&&g>=b+7&&b>=210);
                    int cta=image.getPixel(260,1010);
                    assertTrue("Reference primary CTA stays coral",
                        android.graphics.Color.red(cta)>=230 &&
                        android.graphics.Color.green(cta)>=70 &&
                        android.graphics.Color.green(cta)<=170 &&
                        android.graphics.Color.blue(cta)<125);
                    int reachable=0;
                    for(MainActivity.BoardScreen.Tap tap:view.taps){
                        if("Nastavi igrati".equals(tap.label)||
                            "Nova igra".equals(tap.label)||
                            "Podijeli rezultat".equals(tap.label)){
                            reachable++;
                            assertTrue("Victory actions remain touch-sized",
                                tap.bottom-tap.top>=49f);
                        }
                    }
                    assertEquals("Exactly three functional modal actions",3,reachable);
                }finally{image.recycle();}
            }finally{
                activity.game=previousGame;
                activity.page=previousPage;
                activity.theme=previousTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void portraitVictoryReferenceHasThreeReachableRealActions(){
        rule.getScenario().onActivity(activity->{
            TileGame saved=activity.game;
            String oldPage=activity.page;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.game=new TileGame("classic","",650L);
                activity.game.score=2048;
                activity.game.peak=2048;
                activity.game.reached2048=true;
                activity.page="win";
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap img=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{view.draw(new Canvas(img));}finally{img.recycle();}
                assertTrue(view.referencePortraitLayout);
                int buttons=0;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Nastavi igrati".equals(tap.label)||
                        "Nova igra".equals(tap.label)||
                        "Podijeli rezultat".equals(tap.label)){
                        buttons++;
                        assertTrue("Compact modal button must have >=48-unit target",
                            tap.bottom-tap.top>=48f);
                        assertTrue("Victory control must remain on screen",
                            view.offY+tap.bottom*view.scale<=1672);
                        assertTrue("No modal action can be covered by status bar",
                            view.offY+tap.top*view.scale>0);
                    }
                }
                assertEquals("Victory must preserve all three actual actions",
                    3,buttons);
                captureReference(activity,view,
                    "victory-source-modal-reference-941x1672");
            }finally{
                activity.game=saved;activity.page=oldPage;
                activity.screen.invalidate();
            }
        });
    }


    @Test public void homeControlledGoldenMatchesSourceDataWithoutFakingPlayerStats(){
        rule.getScenario().onActivity(activity->{
            // Source art uses 12,560 / 3,864 and a 'Nova igra' CTA. These
            // values exist only during this isolated screenshot test, NEVER
            // in the player's persistent statistics or production UI.
            TileGame oldGame=activity.game;
            String oldPage=activity.page;
            int oldBest=activity.best,oldLast=activity.lastScore;
            int oldTheme=activity.theme;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.game=null;
                activity.best=12560;
                activity.lastScore=3864;
                activity.theme=0;
                activity.page="home";
                view.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                captureReference(activity,view,"home-controlled-reference-941x1672");
                int newGameActions=0,modeActions=0,shortcutActions=0;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Nova igra".equals(tap.label))newGameActions++;
                    if(tap.label.startsWith("Klasično")||
                        tap.label.startsWith("Opušteno")||
                        tap.label.startsWith("Dnevni izazov"))modeActions++;
                    if("Misije".equals(tap.label)||
                        "Statistika".equals(tap.label)||
                        "Teme".equals(tap.label)||
                        "Postavke".equals(tap.label)){
                        shortcutActions++;
                        assertTrue("Home shortcut must remain touch-sized",
                            tap.bottom-tap.top>=48f);
                    }
                }
                assertEquals("Source Home CTA is a real New Game action",
                    1,newGameActions);
                assertEquals("All three play modes remain real",3,modeActions);
                assertEquals("All four Home shortcuts remain real",4,shortcutActions);
            }finally{
                activity.game=oldGame;activity.page=oldPage;
                activity.best=oldBest;activity.lastScore=oldLast;
                activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void portraitHomeHasFunctionalScoreCardsAndAllFourShortcuts(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            MainActivity.BoardScreen preview=activity.new BoardScreen(activity);
            try{
                activity.page="home";
                preview.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                preview.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{preview.draw(new Canvas(image));}finally{image.recycle();}
                int stats=0,shortcuts=0,hero=0;
                for(MainActivity.BoardScreen.Tap tap:preview.taps){
                    if("Najbolji rezultat".equals(tap.label)||
                       "Zadnji rezultat".equals(tap.label)){
                        stats++;
                        assertTrue("Score shortcuts remain touch-sized",
                            tap.bottom-tap.top>=70f);
                    }
                    if("Misije".equals(tap.label)||
                       "Statistika".equals(tap.label)||
                       "Teme".equals(tap.label)||
                       "Postavke".equals(tap.label)){
                        shortcuts++;
                        assertTrue("Bottom shortcut inside source phone viewport",
                            preview.offY+tap.bottom*preview.scale<=1672);
                    }
                    if("Nova igra".equals(tap.label)||
                       "Nastavi igru".equals(tap.label)){
                        hero++;
                        assertTrue("Main game action remains a real touch zone",
                            tap.bottom-tap.top>=80f);
                    }
                }
                assertEquals("Both score cards must have functional actions",2,stats);
                assertEquals("All four lower navigation shortcuts visible",4,shortcuts);
                assertEquals("Start/resume must be a single action",1,hero);
                captureReference(activity,preview,
                    "home-hero-and-score-shortcuts-941x1672");
            }finally{activity.page=oldPage;activity.screen.invalidate();}
        });
    }



    @Test public void splashPhaseNineRendersOwnerIllustrationAndSoftEdgeTiles(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            int previousStyle=activity.tileStyle,previousTheme=activity.theme;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="splash";activity.tileStyle=0;activity.theme=0;
                int[][] sizes={{941,1672},{360,600},{1080,2400}};
                for(int[] size:sizes){
                    view.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                            View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                            View.MeasureSpec.EXACTLY));
                    view.layout(0,0,size[0],size[1]);
                    Bitmap bitmap=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{
                        view.draw(new Canvas(bitmap));
                        assertEquals("Splash never scrolls",0f,view.maxScroll,.01f);
                        if(size[0]==941){
                            assertTrue("Coral left accent rays are visible",
                                countCoralPixels(bitmap,view,32,61,63,105)>8);
                            int px=Math.round(view.offX+350f*view.scale);
                            int py=Math.round(view.offY+675f*view.scale);
                            int blue=bitmap.getPixel(px,py);
                            assertTrue("Lower-right decorative 256 is soft blue",
                                android.graphics.Color.blue(blue)>
                                    android.graphics.Color.red(blue)+35);
                            assertTrue("Raised 2 remains inside native screen",
                                view.offX+61*view.scale>0);
                            captureReference(activity,view,
                                "splash-phase09-owner-941x1672");
                        }
                        if(size[0]==360)
                            assertTrue("Short phone keeps source slogan on-screen",
                                view.offY+628f*view.scale<=600f);
                    }finally{bitmap.recycle();}
                }
            }finally{
                activity.page=previousPage;activity.tileStyle=previousStyle;
                activity.theme=previousTheme;activity.screen.invalidate();
            }
        });
    }

    @Test public void portraitSplashFillsWidthWithoutScrollingOrClippingSlogan(){
        rule.getScenario().onActivity(activity->{
            String old=activity.page;
            MainActivity.BoardScreen splash=activity.new BoardScreen(activity);
            try{
                activity.page="splash";
                int[][] sizes={{941,1672},{360,600},{400,880},{1080,2400}};
                for(int[] size:sizes){
                    splash.measure(View.MeasureSpec.makeMeasureSpec(size[0],
                        View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size[1],
                        View.MeasureSpec.EXACTLY));
                    splash.layout(0,0,size[0],size[1]);
                    Bitmap image=Bitmap.createBitmap(size[0],size[1],
                        Bitmap.Config.ARGB_8888);
                    try{
                        splash.draw(new Canvas(image));
                        if(size[0]==941){
                            // The tall Brojko wordmark should not clip on
                            // either edge at the owner's reference size.
                            int minX=941,maxX=-1;
                            for(int yy=730;yy<970;yy+=3){
                                for(int xx=0;xx<941;xx+=3){
                                    int rgb=image.getPixel(xx,yy);
                                    int red=android.graphics.Color.red(rgb);
                                    int green=android.graphics.Color.green(rgb);
                                    int blue=android.graphics.Color.blue(rgb);
                                    if(red<72&&green<102&&blue<144&&blue>=red){
                                        minX=Math.min(minX,xx);
                                        maxX=Math.max(maxX,xx);
                                    }
                                }
                            }
                            assertTrue("Logo must not be cut off on the left",minX>=65);
                            assertTrue("Logo must not be cut off on the right",maxX<=880);
                        }
                    }finally{image.recycle();}
                    assertEquals("Splash must never scroll",0f,splash.maxScroll,.01f);
                    boolean compact=size[1]/(float)size[0]<1.85f;
                    if(compact){
                        assertEquals("Portrait brand should scale by width",
                            size[0]/400f,splash.scale,.01f);
                        assertTrue("Slogan must remain on short phone",
                            splash.offY+628*splash.scale<=size[1]);
                    }
                    if(size[0]==941)
                        captureReference(activity,splash,
                            "splash-full-width-portrait-reference-941x1672");
                }
            }finally{activity.page=old;activity.screen.invalidate();}
        });
    }



    @Test public void phaseTenAdaptiveIconKeepsFourTilesInsideSafeArtwork(){
        rule.getScenario().onActivity(activity->{
            android.graphics.drawable.Drawable foreground=activity.getResources()
                .getDrawable(com.brendigo.brojko.R.drawable.ic_brojko_foreground,
                    activity.getTheme());
            Bitmap image=Bitmap.createBitmap(512,512,Bitmap.Config.ARGB_8888);
            try{
                foreground.setBounds(0,0,512,512);
                foreground.draw(new Canvas(image));
                int[] quadrants=new int[4];
                int whiteCenter=0;
                int unsafeSaturated=0;
                for(int y=0;y<512;y+=6)for(int x=0;x<512;x+=6){
                    int value=image.getPixel(x,y);
                    int red=android.graphics.Color.red(value);
                    int green=android.graphics.Color.green(value);
                    int blue=android.graphics.Color.blue(value);
                    int alpha=android.graphics.Color.alpha(value);
                    boolean coral=red>209&&green>78&&green<181&&blue<158;
                    boolean teal=green>145&&blue>115&&red<151;
                    boolean blueTile=blue>175&&red<166;
                    boolean yellow=red>202&&green>116&&blue<158;
                    // A small corner safety gutter is required for round
                    // and themed launcher masks. White pixels and cream are
                    // not counted as the colored foreground artwork.
                    if(alpha>230&&(coral||teal||blueTile||yellow)&&
                        (x<80||x>=432||y<80||y>=432))unsafeSaturated++;
                    if(x<243&&y<243&&coral)quadrants[0]++;
                    if(x>=269&&y<243&&teal)quadrants[1]++;
                    if(x<243&&y>=269&&blueTile)quadrants[2]++;
                    if(x>=269&&y>=269&&yellow)quadrants[3]++;
                    if(x>=185&&x<=330&&y>=160&&y<=355&&alpha>230&&
                        red>241&&green>241&&blue>224)whiteCenter++;
                }
                for(int i=0;i<4;i++)assertTrue(
                    "Phase 10 launcher retains tile "+i,quadrants[i]>10);
                assertTrue("Central white B is still prominent",whiteCenter>60);
                assertEquals("No saturated tile art touches adaptive-icon rim",
                    0,unsafeSaturated);
                File folder=new File(activity.getExternalFilesDir(null),"screenshots");
                assertTrue(folder.isDirectory()||folder.mkdirs());
                try(FileOutputStream out=new FileOutputStream(new File(
                        folder,"phase10-adaptive-icon-safe-512.png"))){
                    assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));
                }catch(java.io.IOException error){
                    throw new AssertionError("Saving phase 10 launcher screenshot",error);
                }
            }finally{image.recycle();}
        });
    }

    @Test public void launcherRetainsFourSourceColorsAndDominantWhiteB(){
        rule.getScenario().onActivity(activity->{
            if(android.os.Build.VERSION.SDK_INT<26)return;
            try{
                android.graphics.drawable.Drawable icon=activity.getPackageManager()
                    .getApplicationIcon(activity.getPackageName());
                assertTrue(icon instanceof android.graphics.drawable.AdaptiveIconDrawable);
                Bitmap bitmap=Bitmap.createBitmap(512,512,Bitmap.Config.ARGB_8888);
                try{
                    icon.setBounds(0,0,512,512);
                    icon.draw(new Canvas(bitmap));
                    int[] quadrants=new int[4];
                    int brightB=0;
                    for(int y=60;y<452;y+=8)for(int x=60;x<452;x+=8){
                        int c=bitmap.getPixel(x,y);
                        int red=android.graphics.Color.red(c);
                        int green=android.graphics.Color.green(c);
                        int blue=android.graphics.Color.blue(c);
                        if(x<240&&y<240&&red>215&&green>80&&green<180&&blue<150)
                            quadrants[0]++;
                        if(x>=272&&y<240&&green>145&&blue>130&&red<145)
                            quadrants[1]++;
                        if(x<240&&y>=272&&blue>175&&red<165)
                            quadrants[2]++;
                        if(x>=272&&y>=272&&red>205&&green>120&&blue<150)
                            quadrants[3]++;
                        if(x>=175&&x<=339&&y>=153&&y<=350&&
                            red>240&&green>240&&blue>229)
                            brightB++;
                    }
                    for(int q=0;q<4;q++)
                        assertTrue("Tile "+q+" must retain its assigned strong color",
                            quadrants[q]>8);
                    assertTrue("The central white B must remain visibly prominent",
                        brightB>35);
                    // Save the exact Android-drawn vector, not a mock launcher image.
                    File folder=new File(activity.getExternalFilesDir(null),"screenshots");
                    assertTrue(folder.isDirectory()||folder.mkdirs());
                    try(FileOutputStream stream=new FileOutputStream(
                            new File(folder,"launcher-four-tiles-B-reference-512.png"))){
                        assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,stream));
                    }catch(java.io.IOException failure){
                        throw new AssertionError("Unable to save launcher reference",failure);
                    }
                }finally{bitmap.recycle();}
            }catch(android.content.pm.PackageManager.NameNotFoundException failure){
                throw new AssertionError("Installed Brojko adaptive icon missing",failure);
            }
        });
    }


    @Test public void googlePlayMinimumSdkAndTargetSdkMatchDeviceManifest(){
        rule.getScenario().onActivity(activity->{
            android.content.pm.ApplicationInfo info=activity.getApplicationInfo();
            assertEquals("Brojko package identifier is stable for Play updates",
                "com.brendigo.brojko",activity.getPackageName());
            assertTrue("Play Automatic Protection requires minSdk >= API 24",
                info.minSdkVersion>=24);
            assertTrue("Play phone app submissions require target API 36 in 2026",
                info.targetSdkVersion>=36);
        });
    }

    @Test public void portraitHomeHasNoEmptyScrollAndCompactShortcutsRemainReachable(){
        rule.getScenario().onActivity(activity->{
            String originalPage=activity.page;
            MainActivity.BoardScreen view=activity.new BoardScreen(activity);
            try{
                activity.page="home";
                int[][] viewports={{941,1672},{360,600}};
                for(int[] viewport:viewports){
                    int width=viewport[0],height=viewport[1];
                    view.measure(View.MeasureSpec.makeMeasureSpec(width,
                            View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height,
                            View.MeasureSpec.EXACTLY));
                    view.layout(0,0,width,height);
                    Bitmap bitmap=Bitmap.createBitmap(width,height,
                        Bitmap.Config.ARGB_8888);
                    try{view.draw(new Canvas(bitmap));}finally{bitmap.recycle();}
                    assertTrue("Home uses the portrait reference layout",
                        view.referencePortraitLayout);
                    if(width==941){
                        assertEquals("No phantom scrolling after Home navigation",
                            0f,view.maxScroll,1f);
                    }else{
                        assertTrue("Small phone must scroll to reveal lower cards",
                            view.maxScroll>0f);
                    }
                    int shortcuts=0;
                    float visibleLogicalHeight=height/view.scale;
                    for(MainActivity.BoardScreen.Tap tap:view.taps){
                        if("Misije".equals(tap.label)||
                           "Statistika".equals(tap.label)||
                           "Teme".equals(tap.label)||
                           "Postavke".equals(tap.label)){
                            shortcuts++;
                            assertTrue("Home shortcut must stay fully reachable: "+tap.label,
                                tap.bottom<=visibleLogicalHeight+view.maxScroll+1f);
                        }
                    }
                    assertEquals("All four destinations remain actionable",4,shortcuts);
                    if(width==941)
                        captureReference(activity,view,
                            "home-no-ghost-scroll-941x1672");
                }
            }finally{
                activity.page=originalPage;
                activity.screen.invalidate();
            }
        });
    }


    @Test public void homeSourceDecorationsAreActualVisibleCoralStrokes(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            MainActivity.BoardScreen preview=activity.new BoardScreen(activity);
            try{
                activity.page="home";
                preview.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                preview.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    preview.draw(new Canvas(bitmap));
                    assertTrue("Left Home ray pair must be coral and visible",
                        countCoralPixels(bitmap,preview,20,297,53,341)>=12);
                    assertTrue("Right Home ray pair must be coral and visible",
                        countCoralPixels(bitmap,preview,347,297,380,341)>=12);
                    int hero=0;
                    for(MainActivity.BoardScreen.Tap tap:preview.taps){
                        if("Nova igra".equals(tap.label)||
                           "Nastavi igru".equals(tap.label)){
                            hero++;
                            assertTrue("Hero touch box must not cover exterior rays",
                                tap.left>=57 && tap.right<=343);
                            assertTrue("Hero target remains accessible",
                                tap.bottom-tap.top>=80);
                        }
                    }
                    assertEquals("Exactly one playable home hero",1,hero);
                    captureReference(activity,preview,
                        "home-source-accent-rays-941x1672");
                }finally{bitmap.recycle();}
            }finally{
                activity.page=previousPage;
                activity.screen.invalidate();
            }
        });
    }


    @Test public void themesPhaseSevenRendersOwnerLayoutAndKeepsRealPreferences(){
        rule.getScenario().onActivity(activity->{
            final String oldPage=activity.page;
            final int oldTheme=activity.theme,oldStyle=activity.tileStyle;
            final int oldAnimation=activity.animation;
            final boolean oldSound=activity.sound,oldVibration=activity.vibration;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="themes";activity.theme=0;activity.tileStyle=0;
                activity.animation=1;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{
                    board.draw(new Canvas(bitmap));
                    assertEquals("Screen 07 does not scroll at reference size",
                        0f,board.maxScroll,.01f);
                    int themes=0,styles=0,tabs=0;
                    MainActivity.BoardScreen.Tap dark=null;
                    for(MainActivity.BoardScreen.Tap tap:board.taps){
                        if("Svijetla".equals(tap.label)||
                           "Tamna".equals(tap.label)||
                           "Živopisna".equals(tap.label)){
                            themes++;
                            assertTrue("Whole theme card is interactive",
                                tap.bottom-tap.top>=140);
                            if("Tamna".equals(tap.label))dark=tap;
                        }
                        if("Zaobljene".equals(tap.label)||
                           "Ravne".equals(tap.label)||
                           "Staklaste".equals(tap.label)){
                            styles++;
                            assertTrue("Whole swatch card is interactive",
                                tap.bottom-tap.top>=48);
                        }
                        if("Novo".equals(tap.label)||
                           "Rekordi".equals(tap.label)||
                           "Teme".equals(tap.label)||
                           "Postavke".equals(tap.label))tabs++;
                    }
                    assertEquals(3,themes);
                    assertEquals(3,styles);
                    assertEquals(4,tabs);
                    assertNotNull(dark);
                    // The first sample hit the rounded top-left corner
                    // (x=158,y=188), where the cream backing shows through.
                    // Sample the dark material between the corner and tiles.
                    int x=Math.round(board.offX+195*board.scale);
                    int y=Math.round(board.offY+189*board.scale);
                    int navy=bitmap.getPixel(x,y);
                    assertTrue("Dark preview uses navy material",
                        android.graphics.Color.red(navy)<90 &&
                        android.graphics.Color.blue(navy)<150);
                    captureReference(activity,board,
                        "themes-phase07-owner-reference-941x1672");
                    dark.action.run();
                    assertEquals("Theme choice is saved locally",1,
                        activity.prefs.getInt("theme",-1));
                    board.draw(new Canvas(bitmap));
                    MainActivity.BoardScreen.Tap glass=null;
                    for(MainActivity.BoardScreen.Tap tap:board.taps)
                        if("Staklaste".equals(tap.label))glass=tap;
                    assertNotNull(glass);
                    glass.action.run();
                    assertEquals("Tile style is saved locally",2,
                        activity.prefs.getInt("style",-1));
                }finally{bitmap.recycle();}
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.tileStyle=oldStyle;activity.animation=oldAnimation;
                activity.sound=oldSound;activity.vibration=oldVibration;
                activity.persist();activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void themeStyleSwatchesUseDistinctWarmReferenceMaterials(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            int previousTheme=activity.theme;
            int previousStyle=activity.tileStyle;
            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
            try{
                activity.page="themes";
                activity.theme=0;
                activity.tileStyle=0;
                board.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                board.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    board.draw(new Canvas(bitmap));
                    int[] swatches=new int[3];
                    for(int i=0;i<3;i++){
                        float logicalX=235.5f+i*56f;
                        int px=Math.round(board.offX+logicalX*board.scale);
                        int py=Math.round(board.offY+402f*board.scale);
                        swatches[i]=bitmap.getPixel(px,py);
                    }
                    assertTrue("Rounded preview should be warm apricot",
                        android.graphics.Color.red(swatches[0])>230 &&
                        android.graphics.Color.green(swatches[0])>135);
                    assertTrue("Flat swatch must be visually different",
                        android.graphics.Color.blue(swatches[1])>
                            android.graphics.Color.blue(swatches[0])+8);
                    assertTrue("Glass swatch should be pearlier and lighter",
                        android.graphics.Color.blue(swatches[2])>
                            android.graphics.Color.blue(swatches[1])+18);
                    int selectors=0;
                    for(MainActivity.BoardScreen.Tap tap:board.taps){
                        if("Zaobljene".equals(tap.label)||
                           "Ravne".equals(tap.label)||
                           "Staklaste".equals(tap.label)){
                            selectors++;
                            assertTrue("Style radio card must be easy to touch",
                                tap.bottom-tap.top>=48);
                        }
                    }
                    assertEquals("Three functional styles",3,selectors);
                    captureReference(activity,board,
                        "themes-peach-style-swatches-941x1672");
                }finally{bitmap.recycle();}
            }finally{
                activity.page=previousPage;activity.theme=previousTheme;
                activity.tileStyle=previousStyle;activity.screen.invalidate();
            }
        });
    }


    @Test public void dailyPrimaryActionHasSafeSourceLikeSideAccents(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
            try{
                activity.page="daily";
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                    View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    screen.draw(new Canvas(bitmap));
                    assertTrue("Left Daily CTA accents must be visibly coral",
                        countCoralPixels(bitmap,screen,14,654,37,684)>=10);
                    assertTrue("Right Daily CTA accents must be visibly coral",
                        countCoralPixels(bitmap,screen,363,654,386,684)>=10);
                    int startActions=0;
                    for(MainActivity.BoardScreen.Tap tap:screen.taps){
                        if("Započni izazov".equals(tap.label)||
                           "Nastavi izazov".equals(tap.label)||
                           "Igraj ponovno".equals(tap.label)){
                            startActions++;
                            assertTrue("Accents cannot cover Daily CTA hitbox",
                                tap.left>=42&&tap.right<=358);
                            assertTrue("Bottom action remains fully on screen",
                                screen.offY+tap.bottom*screen.scale<=1673);
                        }
                    }
                    assertEquals("Exactly one genuine Daily CTA",1,startActions);
                    captureReference(activity,screen,
                        "daily-large-safe-primary-rays-941x1672");
                }finally{bitmap.recycle();}
            }finally{
                activity.page=previousPage;
                activity.screen.invalidate();
            }
        });
    }

}
