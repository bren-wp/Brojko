package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/** Source-composition geometry and live touch areas at portrait screen extremes. */
@RunWith(AndroidJUnit4.class)
public final class SplashHomePortraitSourceResponsiveTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private MainActivity.BoardScreen render(MainActivity activity, String page,
                                             int width, int height) {
        activity.page=page;
        activity.theme=0;
        MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
        screen.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
        screen.layout(0,0,width,height);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        try{screen.draw(new Canvas(bitmap));}
        finally{bitmap.recycle();}
        return screen;
    }

    @Test public void tallPhonesReuse941x1672NativeOwnerComposition() {
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try{
                for(String page:new String[]{"splash","home"}){
                    for(int[] vp:new int[][]{{941,1672},{360,800},{393,873},
                                            {1080,2400},{1440,3200}}){
                        int w=vp[0],h=vp[1];
                        MainActivity.BoardScreen screen=render(activity,page,w,h);
                        assertTrue(page+" must keep the native source composition at "
                            +w+"x"+h,screen.referencePortraitLayout);
                        assertEquals("Never nonuniformly stretch the source geometry",
                            w/400f,screen.scale,.001f);
                        assertEquals("Source reference stays fixed; tall screens center",
                            h/(float)w<1.85f?0f:
                                Math.max(0f,(h-710f*screen.scale)/2f),
                            screen.offY,.05f);
                        assertEquals("Source 400-unit plane fills viewport width",
                            0f,screen.offX,.05f);
                        if("splash".equals(page)){
                            assertNotNull("Native 941px owner wordmark is retained",
                                screen.ownerSplashNavyPath);
                            assertTrue("Splash is not a scrollable page",
                                screen.maxScroll==0f&&screen.taps.isEmpty());
                        }else{
                            assertNotNull("Home source navy wordmark retained",
                                screen.ownerHomeNavyPath);
                            assertEquals("Home needs no scrolling on tall phones",
                                0f,screen.maxScroll,.01f);
                            boolean play=false,classic=false,relaxed=false,daily=false,
                                    missions=false,stats=false,themes=false,settings=false;
                            for(MainActivity.BoardScreen.Tap tap:screen.taps){
                                play|=tap.label.contains("Nova igra")||
                                    tap.label.contains("Nastavi igru");
                                classic|=tap.label.startsWith("Klasično");
                                relaxed|=tap.label.startsWith("Opušteno");
                                daily|=tap.label.startsWith("Dnevni izazov");
                                missions|="Misije".equals(tap.label);
                                stats|="Statistika".equals(tap.label);
                                themes|="Teme".equals(tap.label);
                                settings|="Postavke".equals(tap.label);
                                assertTrue("Clickable Home area clipped at "+w+"x"+h+
                                    ": "+tap.label,
                                    screen.offY+tap.top*screen.scale>=-.1f &&
                                    screen.offY+tap.bottom*screen.scale<=h+.1f);
                            }
                            assertTrue("Every Home action remains reachable at "+w+"x"+h,
                                play&&classic&&relaxed&&daily&&missions&&stats&&
                                themes&&settings);
                        }
                    }
                }
            }finally{
                activity.page=oldPage;
                activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void tinyPhonesPreserveSplashSloganAndScrollableHomeActions(){
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try{
                for(int[] vp:new int[][]{{320,480},{360,600},{360,640}}){
                    int w=vp[0],h=vp[1];
                    MainActivity.BoardScreen splash=render(activity,"splash",w,h);
                    assertEquals("Never scroll a splash",0f,splash.maxScroll,.01f);
                    assertTrue("Source slogan stays on-screen at "+w+"x"+h,
                        splash.offY+628f*splash.scale<=h+.5f);
                    MainActivity.BoardScreen home=render(activity,"home",w,h);
                    assertTrue("Home must allow scrolling to all shortcuts",
                        home.maxScroll>=0f);
                    boolean settings=false;
                    for(MainActivity.BoardScreen.Tap tap:home.taps)
                        settings|="Postavke".equals(tap.label);
                    assertTrue("Bottom settings action is retained",settings);
                    // The last card remains reachable after scrolling
                    // the exact maxScroll, rather than being permanently clipped.
                    MainActivity.BoardScreen.Tap bottom=null;
                    for(MainActivity.BoardScreen.Tap tap:home.taps)
                        if("Postavke".equals(tap.label))bottom=tap;
                    assertNotNull(bottom);
                    float scrolledBottom=home.offY+
                        (bottom.bottom-home.maxScroll)*home.scale;
                    assertTrue("Bottom Home action must be scroll-reachable at "+w+"x"+h,
                        scrolledBottom<=h+.5f);
                }
            }finally{
                activity.page=oldPage;
                activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }
}
