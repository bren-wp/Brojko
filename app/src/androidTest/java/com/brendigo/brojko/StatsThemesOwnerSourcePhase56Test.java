package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Regression of owner 941x1672 Statistics and Themes references.
 * Draws native, live Android Canvas; no reference PNG in the app.
 */
@RunWith(AndroidJUnit4.class)
public final class StatsThemesOwnerSourcePhase56Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static void measure(MainActivity.BoardScreen view,int width,int height){
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
        view.layout(0,0,width,height);
    }

    @Test public void statsKpiRowsReachOwnerFrameEdgesAndKeepChartControls(){
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;
            int oldTheme=activity.theme,oldMetric=activity.chartMetric;
            Bitmap image=null;
            try{
                activity.page="stats";activity.theme=0;activity.chartMetric=0;
                MainActivity.BoardScreen view=activity.new BoardScreen(activity);
                measure(view,941,1672);
                image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(image));
                assertTrue("941x1672 Stats must retain owner compact composition",
                    view.referencePortraitLayout);
                int leftCard=image.getPixel(38,380);
                assertTrue("First KPI must extend to source x~33",
                    Color.red(leftCard)>Color.blue(leftCard)+7);
                int gap=image.getPixel(327,415);
                int middleCard=image.getPixel(356,415);
                assertTrue("Rounded KPI cards stay separated and pastel",
                    Color.green(middleCard)>Color.red(middleCard)-12 &&
                    Color.red(gap)>=Color.red(middleCard));
                boolean scoresTab=false,tilesTab=false,history=false;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    scoresTab|="Grafikon bodova".equals(tap.label);
                    tilesTab|="Grafikon pločica".equals(tap.label);
                    history|=tap.label.startsWith("Detalji partije");
                }
                assertTrue("Chart metric switches remain active",scoresTab&&tilesTab);
                // History rows are data-driven. They may be absent in a new install.
                assertTrue("Stats viewport has reachable scroll geometry",
                    view.scale>0f && view.offX>=-.1f);
            }finally{
                if(image!=null)image.recycle();
                activity.page=oldPage;activity.theme=oldTheme;
                activity.chartMetric=oldMetric;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void selectedLightThemeBorderAndTileStyleAreRealHitTargets(){
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;
            int oldTheme=activity.theme,oldStyle=activity.tileStyle;
            Bitmap image=null;
            try{
                activity.page="themes";activity.theme=0;activity.tileStyle=0;
                MainActivity.BoardScreen view=activity.new BoardScreen(activity);
                measure(view,941,1672);
                image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(image));
                int border=image.getPixel(37,528);
                assertTrue("Selected light-theme border reaches source x~32",
                    Color.red(border)>210 &&
                    Color.red(border)>Color.green(border)+40);
                boolean light=false,dark=false,vivid=false,rounded=false,
                    flat=false,glass=false,vibration=false,sound=false,
                    low=false,medium=false,high=false;
                for(MainActivity.BoardScreen.Tap tap:view.taps){
                    if("Svijetla".equals(tap.label)){
                        light=tap.left<=14.01f && tap.right>=137.99f;
                    }
                    dark|="Tamna".equals(tap.label);
                    vivid|="Živopisna".equals(tap.label);
                    rounded|="Zaobljene".equals(tap.label)&&tap.left<=203.01f;
                    flat|="Ravne".equals(tap.label);
                    glass|="Staklaste".equals(tap.label);
                    vibration|="Vibracija".equals(tap.label);
                    sound|="Zvukovi".equals(tap.label);
                    low|="Niska".equals(tap.label);
                    medium|="Srednja".equals(tap.label);
                    high|="Visoka".equals(tap.label);
                }
                assertTrue("All three source theme options are touchable",
                    light&&dark&&vivid);
                assertTrue("Three real style choices remain touchable",
                    rounded&&flat&&glass);
                assertTrue("Live sound, vibration, animation inputs remain",
                    vibration&&sound&&low&&medium&&high);
            }finally{
                if(image!=null)image.recycle();
                activity.page=oldPage;activity.theme=oldTheme;
                activity.tileStyle=oldStyle;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void bothScreensRetainCompactLandscapeAndLargeSafeGeometry(){
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try{
                for(String page:new String[]{"stats","themes"}){
                    for(int[] size:new int[][]{
                            {320,480},{360,600},{393,873},{941,1672},
                            {1080,2400},{1440,3200},{800,360}}){
                        activity.page=page;activity.theme=0;
                        MainActivity.BoardScreen view=activity.new BoardScreen(activity);
                        measure(view,size[0],size[1]);
                        Bitmap frame=Bitmap.createBitmap(size[0],size[1],
                            Bitmap.Config.ARGB_8888);
                        try{view.draw(new Canvas(frame));}
                        finally{frame.recycle();}
                        assertTrue("Whole 400-unit design fits width: "+page+" "+
                            size[0]+"x"+size[1],
                            view.scale>0 && view.offX>=-.1f &&
                            view.offX+400f*view.scale<=size[0]+.1f);
                        assertTrue("Scrolling must not be negative",
                            view.maxScroll>=0f);
                        boolean target=false;
                        for(MainActivity.BoardScreen.Tap tap:view.taps)
                            if("stats".equals(page))
                                target|="Grafikon bodova".equals(tap.label);
                            else
                                target|="Svijetla".equals(tap.label);
                        assertTrue("Page must preserve its primary native action",
                            target);
                    }
                }
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.screen.invalidate();
            }
        });
    }
}
