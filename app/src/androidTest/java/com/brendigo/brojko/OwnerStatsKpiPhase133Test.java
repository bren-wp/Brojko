package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** 941x1672 native owner Stats KPI ink verification. */
@RunWith(AndroidJUnit4.class)
public class OwnerStatsKpiPhase133Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static RectF findInk(MainActivity.BoardScreen screen,
                                  String label,float minY,float maxY){
        for(MainActivity.BoardScreen.TextRegion region:screen.textRegions)
            if(label.equals(region.text) &&
                region.bounds.top>=minY && region.bounds.bottom<=maxY)
                return region.bounds;
        return null;
    }

    private static void checkBox(String name,RectF actual,
                                  float l,float t,float r,float b,
                                  float tolerance){
        assertNotNull(name+" must draw real native text",actual);
        assertTrue(name+" left "+actual.left,
            Math.abs(actual.left-l)<=tolerance);
        assertTrue(name+" right "+actual.right,
            Math.abs(actual.right-r)<=tolerance);
        assertTrue(name+" top "+actual.top,
            Math.abs(actual.top-t)<=tolerance);
        assertTrue(name+" bottom "+actual.bottom,
            Math.abs(actual.bottom-b)<=tolerance);
    }

    @Test public void upperThreeKpiDigitsApproachOriginalPixelBoxes(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            int oldBest=activity.best,oldPlays=activity.plays;
            int oldRecord=activity.recordTile;
            try{
                activity.page="stats";activity.theme=0;
                activity.best=12560;activity.plays=248;
                activity.recordTile=1024;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.auditTextLayout=true;
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap actual=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    screen.draw(new Canvas(actual));
                    assertTrue(screen.referencePortraitLayout);
                    // Measurements from the actual supplied original PNG:
                    // 12.560 x69..297, 248 x422..553, 1024 x694..859.
                    // Compare native glyph bounds without resizing or
                    // faking any production player data.
                    checkBox("Best 12.560",
                        findInk(screen,"12.560",420,525),
                        69,442,297,499,17f);
                    checkBox("Played 248",
                        findInk(screen,"248",420,525),
                        422,442,553,499,17f);
                    checkBox("Peak 1024",
                        findInk(screen,"1024",420,525),
                        694,444,859,498,17f);
                    assertEquals("Best remains unchanged by rendering",
                        12560,activity.best);
                    assertEquals("Played remains unchanged by rendering",
                        248,activity.plays);
                    assertEquals("Peak remains unchanged by rendering",
                        1024,activity.recordTile);
                    boolean back=false;
                    for(MainActivity.BoardScreen.Tap tap:screen.taps)
                        if("Natrag".equals(tap.label)){
                            back=true;assertNotNull(tap.action);
                        }
                    assertTrue("Statistics Back remains reachable",back);
                }finally{actual.recycle();}
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.best=oldBest;activity.plays=oldPlays;
                activity.recordTile=oldRecord;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }

    @Test public void tallAndDarkStatisticsRetainLiveData(){
        rule.getScenario().onActivity(activity->{
            String page=activity.page;int theme=activity.theme;
            try{
                for(int currentTheme:new int[]{0,1}){
                    for(int[] size:new int[][]{{360,800},{1080,2400}}){
                        activity.page="stats";activity.theme=currentTheme;
                        MainActivity.BoardScreen screen=
                            activity.new BoardScreen(activity);
                        screen.measure(View.MeasureSpec.makeMeasureSpec(
                                size[0],View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(
                                size[1],View.MeasureSpec.EXACTLY));
                        screen.layout(0,0,size[0],size[1]);
                        Bitmap image=Bitmap.createBitmap(size[0],size[1],
                            Bitmap.Config.ARGB_8888);
                        try{screen.draw(new Canvas(image));}
                        finally{image.recycle();}
                        boolean hasChart=false;
                        for(MainActivity.BoardScreen.Tap tap:screen.taps)
                            if("Grafikon bodova".equals(tap.label) &&
                                tap.action!=null)hasChart=true;
                        assertTrue("Charts stay interactive",hasChart);
                    }
                }
            }finally{
                activity.page=page;activity.theme=theme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
