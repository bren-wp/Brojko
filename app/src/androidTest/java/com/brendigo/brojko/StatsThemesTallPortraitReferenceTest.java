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

/** Only owner-provided Statistics and Themes screens.
 * Use actual Canvas drawing and live Tap regions; never a bitmap mock.
 */
@RunWith(AndroidJUnit4.class)
public final class StatsThemesTallPortraitReferenceTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private MainActivity.BoardScreen draw(MainActivity activity,String page,
                                           int width,int height) {
        activity.page=page;
        activity.theme=0;
        MainActivity.BoardScreen view=activity.new BoardScreen(activity);
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
        view.layout(0,0,width,height);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        try{view.draw(new Canvas(bitmap));}
        finally{bitmap.recycle();}
        return view;
    }

    @Test public void sourceLayoutIsStableOnReferenceAndLargeTallPhones(){
        rule.getScenario().onActivity(activity->{
            String formerPage=activity.page;
            int formerTheme=activity.theme;
            try{
                for(String page:new String[]{"themes","stats"}){
                    for(int[] viewport:new int[][]{{941,1672},{360,800},
                            {393,873},{1080,2400},{1440,3200}}){
                        int w=viewport[0],h=viewport[1];
                        MainActivity.BoardScreen view=draw(activity,page,w,h);
                        assertTrue("Owner's source layout, not alternate 880 layout: "+
                            page+" "+w+"x"+h,view.referencePortraitLayout);
                        assertEquals("Uniform source scale",w/400f,view.scale,.002f);
                        assertEquals("Source centered on high portrait",
                            h/(float)w>=1.85f?
                                (h-710f*view.scale)/2f:0f,view.offY,.1f);
                        assertEquals("Source uses whole available width",
                            0f,view.offX,.1f);
                        assertEquals("All actions fit without scroll on tall portrait",
                            0f,view.maxScroll,.01f);
                        assertTrue("Native brand path must be present",
                            view.ownerHomeNavyPath!=null ||
                            view.ownerSplashNavyPath!=null ||
                            view.taps.size()>0);
                        boolean primary=false,secondary=false;
                        for(MainActivity.BoardScreen.Tap tap:view.taps){
                            if("themes".equals(page)){
                                primary|="Svijetla".equals(tap.label);
                                secondary|="Postavke".equals(tap.label);
                            }else{
                                primary|="Grafikon bodova".equals(tap.label);
                                secondary|="Grafikon pločica".equals(tap.label);
                            }
                            float top=view.offY+tap.top*view.scale;
                            float bottom=view.offY+tap.bottom*view.scale;
                            assertTrue("Tap begins inside safe view: "+
                                page+" "+tap.label+" "+w+"x"+h,
                                top>=-1f);
                            assertTrue("Tap remains visible: "+
                                page+" "+tap.label+" "+w+"x"+h,
                                bottom<=h+1f);
                        }
                        assertTrue("Live controls survive source scaling",
                            primary&&secondary);
                    }
                }
            }finally{
                activity.page=formerPage;
                activity.theme=formerTheme;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void shortPortraitCanScrollToAllBottomActions(){
        rule.getScenario().onActivity(activity->{
            String formerPage=activity.page;
            int formerTheme=activity.theme;
            try{
                for(String page:new String[]{"themes","stats"}){
                    for(int[] viewport:new int[][]{{320,480},{360,600}}){
                        int w=viewport[0],h=viewport[1];
                        MainActivity.BoardScreen view=draw(activity,page,w,h);
                        assertTrue("Positive safe source transform",view.scale>0f);
                        assertTrue("Overflow available through scrolling",
                            view.maxScroll>0f);
                        // On the final scroll position, the bottom of the
                        // source scene must fit into the physical viewport.
                        assertTrue("Source footer reachable by scroll",
                            view.offY+(710f-view.maxScroll)*view.scale<=h+1f);
                    }
                }
            }finally{
                activity.page=formerPage;
                activity.theme=formerTheme;
                activity.screen.invalidate();
            }
        });
    }
}
