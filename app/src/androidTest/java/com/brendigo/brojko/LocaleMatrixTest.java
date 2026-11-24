package com.brendigo.brojko;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.Arrays;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real Activity/Canvas locale QA, separate from the Croatian owner-reference suite. */
@RunWith(AndroidJUnit4.class)
public final class LocaleMatrixTest {
    private static final String[] PAGES={"splash","home","game","pause","win","over",
        "confirm","confirmEnd","dailyWin","dailyFail","daily","missions","stats",
        "historyDetail","themes","settings","language","backup","help","privacy","about",
        "gameRelaxed","gameDaily"};

    private void choose(Context context,LocaleManager.Language language){
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
            ()->assertTrue(LocaleManager.select(context,language)));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test public void allLanguagesSurviveLaunchAndExposeLocalizedNavigation(){
        Context context=ApplicationProvider.getApplicationContext();
        try{
            for(LocaleManager.Language language:LocaleManager.Language.values()){
                choose(context,language);
                try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
                    scenario.onActivity(activity->{
                        assertEquals(language,activity.uiLanguage);
                        assertEquals(language,LocaleManager.selected(activity));
                        assertEquals(LocaleManager.isRtl(activity.uiLocale),
                            activity.screen.getLayoutDirection()==View.LAYOUT_DIRECTION_RTL);
                        activity.game=new TileGame("classic","",521L);
                        activity.page="home";
                        MainActivity.BoardScreen board=activity.new BoardScreen(activity);
                        render(board,941,1672).recycle();
                        String settings=board.uiText(R.string.settings_title);
                        MainActivity.BoardScreen.Tap found=null;
                        for(MainActivity.BoardScreen.Tap tap:board.taps)
                            if(settings.equals(tap.label)){found=tap;break;}
                        assertNotNull(language.tag+": settings control",found);
                        float x=board.physicalX((found.left+found.right)/2f);
                        float y=board.offY+(found.top+found.bottom)/2f*board.scale;
                        long now=android.os.SystemClock.uptimeMillis();
                        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
                        MotionEvent up=MotionEvent.obtain(now,now+40,MotionEvent.ACTION_UP,x,y,0);
                        try{board.onTouchEvent(down);board.onTouchEvent(up);}
                        finally{down.recycle();up.recycle();}
                        assertEquals(language.tag+": physical tap reaches settings","settings",activity.page);
                        assertNotNull(board.localizedResources.getQuantityString(
                            R.plurals.daily_moves_remaining,1,1));
                    });
                    scenario.recreate();
                    scenario.onActivity(activity->assertEquals(language,activity.uiLanguage));
                }
            }
        }finally{choose(context,LocaleManager.Language.CROATIAN);}
    }

    @Test public void rtlPhysicalSwipesAndVirtualControlsUseAttachedView(){
        Context context=ApplicationProvider.getApplicationContext();
        try{
            for(LocaleManager.Language language:new LocaleManager.Language[]{
                LocaleManager.Language.ARABIC,LocaleManager.Language.HEBREW}){
                choose(context,language);
                try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
                    scenario.onActivity(activity->{
                        MainActivity.BoardScreen board=activity.screen;
                        assertTrue(board.isAttachedToWindow());
                        assertEquals(View.LAYOUT_DIRECTION_RTL,board.getLayoutDirection());
                        activity.navigate("home");
                        drawAttached(board);
                        MainActivity.BoardScreen.Tap settings=null;
                        int settingsIndex=-1;
                        for(int i=0;i<board.taps.size();i++){
                            if(board.uiText(R.string.settings_title).equals(board.taps.get(i).label)){
                                settings=board.taps.get(i);settingsIndex=i;break;
                            }
                        }
                        assertNotNull(settings);
                        float x=board.physicalX((settings.left+settings.right)/2f);
                        float y=board.offY+(settings.top+settings.bottom)/2f*board.scale;
                        // The virtual accessibility child resolves the same mirrored point.
                        int virtualId=board.accessibilityHelper.getVirtualViewAt(x,y);
                        assertEquals(settingsIndex+1,virtualId);
                        assertTrue(board.accessibilityHelper.onPerformActionForVirtualView(
                            virtualId,android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK,null));
                        assertEquals("settings",activity.page);
                        TileGame expected=new TileGame("classic","",941L);
                        Arrays.fill(expected.cells,0);expected.cells[1]=2;expected.cells[2]=2;
                        activity.game=new TileGame("classic","",941L);
                        activity.game.cells=expected.cells.clone();
                        activity.sound=false;activity.vibration=false;
                        activity.navigate("game");drawAttached(board);
                        float middle=board.physicalX(200f);
                        float boardY=board.offY+(board.gridY+120f)*board.scale;
                        expected.move(TileGame.LEFT);
                        sendSwipe(board,middle,boardY,middle-50f*board.scale,boardY);
                        assertArrayEquals(language.tag+": physical left swipe",expected.cells,activity.game.cells);
                        assertEquals(expected.score,activity.game.score);
                        assertEquals(expected.moves,activity.game.moves);
                    });
                }
            }
        }finally{choose(context,LocaleManager.Language.CROATIAN);}
    }

    private static void drawAttached(MainActivity.BoardScreen board){
        assertTrue(board.getWidth()>0&&board.getHeight()>0);
        Bitmap bitmap=Bitmap.createBitmap(board.getWidth(),board.getHeight(),Bitmap.Config.ARGB_8888);
        try{board.draw(new Canvas(bitmap));}finally{bitmap.recycle();}
    }

    private static void sendSwipe(MainActivity.BoardScreen board,float x,float y,float endX,float endY){
        long now=android.os.SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
        MotionEvent up=MotionEvent.obtain(now,now+60,MotionEvent.ACTION_UP,endX,endY,0);
        try{assertTrue(board.dispatchTouchEvent(down));assertTrue(board.dispatchTouchEvent(up));}
        finally{down.recycle();up.recycle();}
    }

    @Test public void captureEveryLocalizedScreenAtRequestedViewports(){
        String capture=InstrumentationRegistry.getArguments().getString("captureLocales","false");
        if(!"true".equals(capture))return;
        boolean full="true".equals(InstrumentationRegistry.getArguments().getString("fullViewports","false"));
        int offset=Integer.parseInt(InstrumentationRegistry.getArguments().getString("localeOffset","0"));
        int count=Integer.parseInt(InstrumentationRegistry.getArguments().getString("localeCount","30"));
        int[][] sizes=full?new int[][]{{320,480},{360,800},{941,1672},{1080,2400}}:
            new int[][]{{941,1672}};
        Context context=ApplicationProvider.getApplicationContext();
        try{
            LocaleManager.Language[] languages=LocaleManager.Language.values();
            assertTrue(offset>=0&&count>0&&offset<languages.length);
            for(int index=offset;index<Math.min(offset+count,languages.length);index++){
                LocaleManager.Language language=languages[index];
                choose(context,language);
                try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
                    scenario.onActivity(activity->{
                        activity.game=new TileGame("classic","",521L);
                        activity.theme=0;
                        for(int[] size:sizes){
                            MainActivity.BoardScreen board=activity.new BoardScreen(activity);
                            for(String page:PAGES){
                                prepareCapture(activity,page);
                                Bitmap bitmap=render(board,size[0],size[1]);
                                try{
                                    File root=activity.getExternalFilesDir("locale-screenshots");
                                    assertNotNull(root);
                                    File dir=new File(root,language.tag+"/"+size[0]+"x"+size[1]);
                                    assertTrue(dir.isDirectory()||dir.mkdirs());
                                    try(FileOutputStream output=new FileOutputStream(new File(dir,page+".png"))){
                                        assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));
                                    }
                                }catch(java.io.IOException failure){
                                    throw new AssertionError(language.tag+"/"+page,failure);
                                }finally{bitmap.recycle();}
                            }
                        }
                        activity.page="home";
                    });
                }
            }
        }finally{choose(context,LocaleManager.Language.CROATIAN);}
    }

    // Test-only deterministic scenarios; no fixture values ship in the app.
    private static void prepareCapture(MainActivity activity,String page){
        String mode="gameRelaxed".equals(page)?"relaxed":
            ("gameDaily".equals(page)||"dailyWin".equals(page)||"dailyFail".equals(page))?"daily":"classic";
        TileGame game=new TileGame(mode,"daily".equals(mode)?activity.today():"",521L);
        for(int i=0;i<12;i++)game.move(i%4);
        if("win".equals(page)){
            Arrays.fill(game.cells,0);game.cells[0]=1024;game.cells[1]=1024;
            game.peak=1024;game.move(TileGame.LEFT);
        }else if("dailyWin".equals(page)||"dailyFail".equals(page)){
            Arrays.fill(game.cells,0);game.cells[0]=2;game.cells[1]=2;
            game.moves=79;game.score="dailyWin".equals(page)?2996:0;
            game.peak="dailyWin".equals(page)?256:2;
            game.move(TileGame.LEFT);
            activity.dailyBonusGrantedThisAttempt="dailyWin".equals(page);
            if("dailyFail".equals(page))game.gameOver=true;
        }else if("over".equals(page)){
            for(int i=0;i<16;i++)game.cells[i]=(i/4+i%4)%2==0?2:4;
            game.move(TileGame.LEFT);
        }
        activity.game=game;
        activity.page=page.startsWith("game")?"game":page;
        if("historyDetail".equals(page)||"stats".equals(page)){
            try{
                org.json.JSONObject row=new org.json.JSONObject()
                    .put("score",game.score).put("moves",game.moves)
                    .put("peak",game.maxTile()).put("mode",game.mode)
                    .put("day","2026-11-24").put("time","12:00")
                    .put("won",game.reached2048);
                assertTrue(activity.prefs.edit().putString("history",
                    new org.json.JSONArray().put(row).toString()).commit());
                activity.selectedHistoryIndex=0;
            }catch(org.json.JSONException invalid){throw new AssertionError(invalid);}
        }
    }

    private static Bitmap render(MainActivity.BoardScreen board,int width,int height){
        board.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
        board.layout(0,0,width,height);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        board.draw(new Canvas(bitmap));
        return bitmap;
    }
}
