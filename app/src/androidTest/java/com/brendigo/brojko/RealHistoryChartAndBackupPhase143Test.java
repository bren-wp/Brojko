package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Native 941x1672 integration tests. The comparison fixture uses
 * separately named SharedPreferences; NO real user's history is written,
 * seeded, erased or replaced. Score bars still come only from history().
 */
@RunWith(AndroidJUnit4.class)
public final class RealHistoryChartAndBackupPhase143Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static Bitmap render(MainActivity.BoardScreen view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(
                941,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(
                1672,View.MeasureSpec.EXACTLY));
        view.layout(0,0,941,1672);
        Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    @Test public void bothChartsDrawActualTenArchivedScoresInChronologicalOrder(){
        rule.getScenario().onActivity(activity -> {
            SharedPreferences originalPrefs=activity.prefs;
            SharedPreferences isolated=activity.getSharedPreferences(
                "brojko-phase143-history-qa",Context.MODE_PRIVATE);
            String originalPage=activity.page;
            TileGame originalGame=activity.game;
            int originalTheme=activity.theme;
            int originalChart=activity.chartMetric;
            String gameJson=originalGame==null?null:
                originalGame.toJson().toString();
            int[] scores={2560,6144,3072,8192,1024,4608,
                12560,5120,3840,6400};
            int[] peaks={16,32,64,128,256,512,1024,2048,128,64};
            try {
                assertTrue(isolated.edit().clear().commit());
                JSONArray recentFirst=new JSONArray();
                // The persisted archive format stores newest first.
                // Render in chronological order without changing rows.
                for(int i=scores.length-1;i>=0;i--){
                    try{
                        JSONObject row=new JSONObject();
                        row.put("score",scores[i]);
                        row.put("peak",peaks[i]);
                        row.put("day","14. stu 2026.");
                        row.put("time","10:00");
                        row.put("mode","classic");
                        row.put("won",i==7);
                        recentFirst.put(row);
                    }catch(Exception err){throw new AssertionError(err);}
                }
                String expected=recentFirst.toString();
                assertTrue(isolated.edit().putString("history",expected).commit());
                activity.prefs=isolated;
                activity.page="stats";
                activity.theme=0;
                for(int metric=0;metric<2;metric++){
                    activity.chartMetric=metric;
                    MainActivity.BoardScreen screen=
                        activity.new BoardScreen(activity);
                    screen.auditTextLayout=true;
                    Bitmap bmp=render(screen);
                    try{
                        assertTrue(screen.referencePortraitLayout);
                        assertEquals(10,activity.history().length());
                        assertEquals(metric==0?12560:2048,
                            screen.statisticsChartMaximum(
                                activity.history(),10));
                        int[] expectedValues=metric==0?scores:peaks;
                        int found=0;
                        float lastX=-1f;
                        for(int value:expectedValues){
                            String text=activity.fmt(value);
                            MainActivity.BoardScreen.TextRegion match=null;
                            for(MainActivity.BoardScreen.TextRegion region:
                                    screen.textRegions){
                                if(text.equals(region.text)&&
                                    region.bounds.centerY()>770f&&
                                    region.bounds.centerY()<1130f){
                                    match=region;
                                    break;
                                }
                            }
                            assertNotNull("Chart label from saved history "+text,
                                match);
                            assertTrue("Bars must remain oldest to newest",
                                match.bounds.centerX()>lastX);
                            lastX=match.bounds.centerX();
                            found++;
                        }
                        assertEquals(10,found);
                        int toggles=0;
                        for(MainActivity.BoardScreen.Tap tap:screen.taps)
                            if("Grafikon bodova".equals(tap.label)||
                               "Grafikon pločica".equals(tap.label))
                                toggles++;
                        assertEquals("Both real history charts accessible",
                            2,toggles);
                    }finally{bmp.recycle();}
                }
                assertEquals("Rendering cannot rewrite history",expected,
                    isolated.getString("history",null));
                if(originalGame!=null)
                    assertEquals("Do not mutate ongoing real session",
                        gameJson,originalGame.toJson().toString());
            }finally{
                activity.prefs=originalPrefs;
                activity.page=originalPage;
                activity.theme=originalTheme;
                activity.chartMetric=originalChart;
                assertTrue(isolated.edit().clear().commit());
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }

    @Test public void backupButtonReallyNavigatesWithoutChangingGame(){
        rule.getScenario().onActivity(activity -> {
            String previousPage=activity.page;
            int previousTheme=activity.theme;
            TileGame game=activity.game;
            String snapshot=game==null?null:game.toJson().toString();
            try{
                activity.page="settings";activity.theme=0;
                MainActivity.BoardScreen screen=
                    activity.new BoardScreen(activity);
                screen.auditTextLayout=true;
                Bitmap bmp=render(screen);
                try{
                    MainActivity.BoardScreen.Tap backup=null;
                    boolean localDisclosure=false;
                    for(MainActivity.BoardScreen.TextRegion region:
                            screen.textRegions)
                        localDisclosure|="Lokalna kopija".equals(region.text);
                    for(MainActivity.BoardScreen.Tap tap:screen.taps)
                        if("Sinkronizacija napretka".equals(tap.label))
                            backup=tap;
                    assertTrue("No pretend cloud sync",localDisclosure);
                    assertNotNull("Reachable local backup action",backup);
                    assertNotNull(backup.action);
                    backup.action.run();
                    assertEquals("backup",activity.page);
                    if(game!=null)
                        assertEquals("Navigation must not lose user progress",
                            snapshot,game.toJson().toString());
                }finally{bmp.recycle();}
            }finally{
                activity.page=previousPage;activity.theme=previousTheme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
