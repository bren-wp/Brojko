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

/** Real Android Canvas pixel checks against the supplied 941x1672 art. */
@RunWith(AndroidJUnit4.class)
public class OwnerStatsVictoryPhase130Test {
    @Rule public ActivityScenarioRule<MainActivity> rule=
        new ActivityScenarioRule<>(MainActivity.class);

    private static Bitmap draw(MainActivity.BoardScreen screen){
        screen.measure(View.MeasureSpec.makeMeasureSpec(941,
                View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1672,
                View.MeasureSpec.EXACTLY));
        screen.layout(0,0,941,1672);
        Bitmap image=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
        screen.draw(new Canvas(image));
        return image;
    }

    private static void isPurple(Bitmap bmp,int x,int y){
        int pixel=bmp.getPixel(x,y);
        assertTrue("Expected source-purple flame at "+x+","+y+
            ", found "+Integer.toHexString(pixel),
            Color.red(pixel)<205 && Color.green(pixel)<140 &&
            Color.blue(pixel)>175 && Color.blue(pixel)>Color.red(pixel)+25);
    }

    @Test public void statisticsStreakFlameUsesSourceNarrowTallOutline(){
        rule.getScenario().onActivity(activity->{
            String page=activity.page;
            int theme=activity.theme;
            int best=activity.best,plays=activity.plays;
            try{
                activity.page="stats";activity.theme=0;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                Bitmap bmp=draw(screen);
                try{
                    assertTrue(screen.referencePortraitLayout);
                    // Real owner flame x556..648 / y593..715. Its apex
                    // must not be a short, wide teardrop (old x537..689).
                    isPurple(bmp,605,609);
                    isPurple(bmp,603,630);
                    isPurple(bmp,569,670);
                    isPurple(bmp,629,684);
                    int notch=bmp.getPixel(600,700);
                    assertTrue("Owner lower flame notch stays open",
                        Color.red(notch)>215 &&
                        Color.green(notch)>180 &&
                        Color.blue(notch)>215);
                    int outside=bmp.getPixel(542,671);
                    assertTrue("Flame no longer leaks to old wide edge",
                        Color.red(outside)>215);
                    assertEquals("Rendering must not alter saved best",best,
                        activity.best);
                    assertEquals("Rendering must not alter play count",plays,
                        activity.plays);
                }finally{bmp.recycle();}
            }finally{
                activity.page=page;activity.theme=theme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }

    @Test public void victoryBlueShareFaceEndsAtOwnerBoundsAndTapsRemain(){
        rule.getScenario().onActivity(activity->{
            String page=activity.page;
            int theme=activity.theme;
            TileGame previous=activity.game;
            try{
                activity.page="win";activity.theme=0;
                activity.game=new TileGame("classic","",130L);
                activity.game.score=2048;
                activity.game.peak=2048;
                activity.game.reached2048=true;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                Bitmap bmp=draw(screen);
                try{
                    assertTrue(screen.referencePortraitLayout);
                    int blue=bmp.getPixel(466,1350);
                    assertTrue("Source blue face stays saturated",
                        Color.blue(blue)>175&&
                        Color.blue(blue)>Color.red(blue)+50);
                    int below=bmp.getPixel(466,1387);
                    assertTrue("No oversized blue button lip at y1387",
                        Color.red(below)>225 &&
                        Color.green(below)>190 &&
                        Color.blue(below)>155);
                    assertEquals("Three real victory actions remain",3,
                        screen.taps.size());
                    assertEquals("Nastavi igrati",screen.taps.get(0).label);
                    assertEquals("Nova igra",screen.taps.get(1).label);
                    assertEquals("Podijeli rezultat",screen.taps.get(2).label);
                }finally{bmp.recycle();}
            }finally{
                activity.page=page;activity.theme=theme;
                activity.game=previous;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
