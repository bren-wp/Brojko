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
 * Checks the three gold rays from the supplied native 941x1672 victory
 * artwork. The UI is drawn by the real BoardScreen, not composited from PNGs.
 */
@RunWith(AndroidJUnit4.class)
public class OwnerVictoryGoldRaysPhase128Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static void assertSourceGold(Bitmap image,int x,int y){
        int pixel=image.getPixel(x,y);
        assertTrue("Expected gold ray at ("+x+","+y+"), got "+
            Integer.toHexString(pixel),
            Color.red(pixel)>245 &&
            Color.green(pixel)>177 && Color.green(pixel)<218 &&
            Color.blue(pixel)>45 && Color.blue(pixel)<112);
    }

    @Test public void crownHasThreeGoldSourceRaysAndRealActions(){
        rule.getScenario().onActivity(activity->{
            TileGame oldGame=activity.game;
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try{
                activity.theme=0;
                activity.game=new TileGame("classic","",128L);
                activity.game.score=2048;
                activity.game.peak=2048;
                activity.game.reached2048=true;
                activity.page="win";
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    screen.draw(new Canvas(image));
                    assertTrue(screen.referencePortraitLayout);
                    // Before the fix these five owner source pixels were
                    // pale ivory (~255/248/239), not golden.
                    assertSourceGold(image,345,505);
                    assertSourceGold(image,370,530);
                    assertSourceGold(image,596,505);
                    assertSourceGold(image,560,539);
                    assertSourceGold(image,473,550);
                    assertEquals("Victory retains all three actions",3,
                        screen.taps.size());
                    assertEquals("Nastavi igrati",screen.taps.get(0).label);
                    assertEquals("Nova igra",screen.taps.get(1).label);
                    assertEquals("Podijeli rezultat",screen.taps.get(2).label);
                }finally{
                    image.recycle();
                }
            }finally{
                activity.game=oldGame;
                activity.page=oldPage;
                activity.theme=oldTheme;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }
}
