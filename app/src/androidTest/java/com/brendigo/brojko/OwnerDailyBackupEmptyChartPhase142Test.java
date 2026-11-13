package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Phase 142: the owner's reference illustration never substitutes for
 * the real user archive, local backup, or unfinished daily challenge.
 * These visual checks render native Canvas at the owner's 941x1672.
 */
@RunWith(AndroidJUnit4.class)
public final class OwnerDailyBackupEmptyChartPhase142Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static Bitmap render(MainActivity.BoardScreen view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(
                941,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
        view.layout(0,0,941,1672);
        Bitmap bitmap=Bitmap.createBitmap(941,1672,
            Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    @Test public void backupIsAnHonestLocalActionNotFakeCloudSwitch() {
        rule.getScenario().onActivity(activity -> {
            String previousPage=activity.page;
            int previousTheme=activity.theme;
            TileGame previousGame=activity.game;
            String previousSession=previousGame==null?null:
                previousGame.toJson().toString();
            try {
                activity.page="settings";
                activity.theme=0;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.auditTextLayout=true;
                Bitmap pixels=render(screen);
                try {
                    assertTrue(screen.referencePortraitLayout);
                    boolean localCopy=false;
                    for(MainActivity.BoardScreen.TextRegion text:screen.textRegions){
                        if("Lokalna kopija".equals(text.text)){
                            RectF bounds=text.bounds;
                            localCopy=bounds.width()>0f &&
                                bounds.height()>0f;
                        }
                    }
                    assertTrue("Must disclose real local-only backup",localCopy);
                    int actions=0;
                    for(MainActivity.BoardScreen.Tap tap:screen.taps){
                        if("Sinkronizacija napretka".equals(tap.label)){
                            actions++;
                            assertNotNull(tap.action);
                            assertTrue("Whole row remains touchable",
                                tap.right-tap.left>=350f);
                        }
                        assertFalse("No fictitious sync-enabled toggle",
                            tap.label.startsWith("Sinkronizacija napretka uključeno"));
                    }
                    assertEquals("One backup navigation",1,actions);
                }finally{pixels.recycle();}
                if(previousGame!=null)assertEquals(
                    "Settings rendering must not rewrite active game",
                    previousSession,previousGame.toJson().toString());
            }finally {
                activity.page=previousPage;activity.theme=previousTheme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }

    @Test public void dailyAndStatsPreservePlayerSessionWhenDrawn() {
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            int oldChartMetric=activity.chartMetric;
            TileGame oldGame=activity.game;
            int oldBest=activity.best;
            try {
                TileGame fixture=new TileGame("classic","",142L);
                fixture.cells=new int[]{
                    2,4,8,16,32,64,128,256,
                    512,1024,0,0,2,4,8,16};
                fixture.score=6932;
                fixture.moves=31;
                fixture.peak=1024;
                activity.game=fixture;
                activity.theme=0;
                activity.best=8192;
                String session=fixture.toJson().toString();
                for(String page:new String[]{"daily","stats"}){
                    activity.page=page;
                    MainActivity.BoardScreen screen=
                        activity.new BoardScreen(activity);
                    Bitmap bitmap=render(screen);
                    try {
                        assertTrue("Owner portrait composition",screen.referencePortraitLayout);
                        assertFalse("Real actions still available",screen.taps.isEmpty());
                        if("stats".equals(page)){
                            boolean scores=false,peak=false;
                            for(MainActivity.BoardScreen.Tap tap:screen.taps){
                                scores|="Grafikon bodova".equals(tap.label);
                                peak|="Grafikon pločica".equals(tap.label);
                            }
                            assertTrue("Both real chart toggles retained",scores&&peak);
                        }
                    }finally{bitmap.recycle();}
                    assertEquals("Rendering "+page+" must not replace progress",
                        session,fixture.toJson().toString());
                    assertEquals("Best result unchanged",8192,activity.best);
                }
            }finally {
                activity.page=oldPage;activity.theme=oldTheme;
                activity.chartMetric=oldChartMetric;
                activity.game=oldGame;activity.best=oldBest;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
