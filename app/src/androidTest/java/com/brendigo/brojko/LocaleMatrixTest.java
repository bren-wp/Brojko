package com.brendigo.brojko;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
        "historyDetail","themes","settings","language","backup","help","privacy","about"};

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
                                activity.page=page;
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

    private static Bitmap render(MainActivity.BoardScreen board,int width,int height){
        board.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
        board.layout(0,0,width,height);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        board.draw(new Canvas(bitmap));
        return bitmap;
    }
}
