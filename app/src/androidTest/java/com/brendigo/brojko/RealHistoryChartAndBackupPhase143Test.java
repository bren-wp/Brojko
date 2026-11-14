package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
            int[] peaks={2,4,8,16,32,64,128,256,512,2048};
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
                        // The reference graph uses a subtle line below the
                        // ten *actual* bars. Check it on both score and
                        // largest-tile tabs at the owner's native resolution.
                        int baseline=bmp.getPixel(50,1094);
                        assertTrue("Owner chart axis is visible under real history",
                            Color.red(baseline)<250 &&
                            Color.green(baseline)<247 &&
                            Color.blue(baseline)<245);
                        assertEquals(10,screen.history().length());
                        assertEquals(metric==0?12560:2048,
                            screen.statisticsChartMaximum(
                                screen.history(),10));
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
                                    region.bounds.centerY()<1100f){
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

    @Test public void emptyHistoryHasNoChartAxisOrInventedScores(){
        rule.getScenario().onActivity(activity -> {
            SharedPreferences originalPrefs=activity.prefs;
            SharedPreferences qa=activity.getSharedPreferences(
                "brojko-phase144-empty-chart",Context.MODE_PRIVATE);
            String originalPage=activity.page;
            int originalTheme=activity.theme;
            int originalMetric=activity.chartMetric;
            TileGame existingGame=activity.game;
            String json=existingGame==null?null:
                existingGame.toJson().toString();
            try{
                assertTrue(qa.edit().clear()
                    .putString("history","[]").commit());
                activity.prefs=qa;activity.page="stats";
                activity.theme=0;
                for(int metric=0;metric<2;metric++){
                    activity.chartMetric=metric;
                    MainActivity.BoardScreen screen=
                        activity.new BoardScreen(activity);
                    Bitmap png=render(screen);
                    try{
                        assertEquals(0,screen.history().length());
                        int blank=png.getPixel(50,1094);
                        assertTrue("No false axis for an empty archive",
                            Color.red(blank)>247&&
                            Color.green(blank)>245&&
                            Color.blue(blank)>240);
                        assertEquals("No sample or demo scores inserted",
                            "[]",qa.getString("history",null));
                    }finally{png.recycle();}
                }
                if(existingGame!=null)assertEquals(
                    "Empty chart must never touch active 2048 session",
                    json,existingGame.toJson().toString());
            }finally{
                activity.prefs=originalPrefs;
                activity.page=originalPage;
                activity.theme=originalTheme;
                activity.chartMetric=originalMetric;
                assertTrue(qa.edit().clear().commit());
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
