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

/** Gameplay pigment calibration against the owner's original 941x1672 PNG. */
@RunWith(AndroidJUnit4.class)
public final class OwnerGameplayColorPhase139Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void finishFaceAndClassicTilesFollowMeasuredOwnerPigments() {
        rule.getScenario().onActivity(activity -> {
            TileGame original=activity.game;
            String oldPage=activity.page;
            int oldTheme=activity.theme,oldStyle=activity.tileStyle,
                oldAnimation=activity.animation,oldBest=activity.best;
            try {
                activity.theme=0;activity.tileStyle=0;activity.animation=0;
                activity.page="game";
                activity.game=new TileGame("classic","",139L);
                activity.game.cells=new int[]{
                    2,4,8,16,8,16,2,4,
                    32,64,8,2,128,16,32,256};
                activity.game.score=1316;activity.game.peak=256;
                activity.best=3864;
                MainActivity.BoardScreen screen=
                    activity.new BoardScreen(activity);
                screen.measure(View.MeasureSpec.makeMeasureSpec(
                    941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(
                    1672,View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try {
                    screen.draw(new Canvas(image));
                    int[][] samples={
                        {700,1450,253,127,83},
                        {830,1510,253,112,71},
                        {820,1570,253,110,69}
                    };
                    for(int[] s:samples){
                        int pixel=image.getPixel(s[0],s[1]);
                        int[] got={Color.red(pixel),
                            Color.green(pixel),Color.blue(pixel)};
                        for(int channel=0;channel<3;channel++)
                            assertTrue("Owner coral finish x="+s[0]+
                                " y="+s[1]+" channel="+channel+
                                ": "+got[channel]+" expected "+s[2+channel],
                                Math.abs(got[channel]-s[2+channel])<=13);
                    }
                    int leftOrange=image.getPixel(90,450);
                    int rightOrange=image.getPixel(200,450);
                    assertTrue("Classic coral tile needs real horizontal depth",
                        Color.green(leftOrange)-Color.green(rightOrange)>=3);
                    int leftTeal=image.getPixel(290,450);
                    int rightTeal=image.getPixel(420,450);
                    assertTrue("Classic teal tile needs native edge shading",
                        Color.red(leftTeal)-Color.red(rightTeal)>=3);
                    int commands=0;
                    for(MainActivity.BoardScreen.Tap tap:screen.taps)
                        if("Novo".equals(tap.label)||
                           "Poništi".equals(tap.label)||
                           "Završi".equals(tap.label))commands++;
                    // The fixture has no previous move: Poništi must be
                    // visible but intentionally non-clickable. Novo and
                    // Završi remain real, accessible actions.
                    assertFalse("No undo exists before any move",
                        activity.game.canUndo());
                    assertEquals("Only enabled actions expose Tap targets",
                        2,commands);
                    assertEquals("Rendering must not fabricate a score",
                        1316,activity.game.score);
                }finally{image.recycle();}
            }finally{
                activity.game=original;activity.page=oldPage;
                activity.theme=oldTheme;activity.tileStyle=oldStyle;
                activity.animation=oldAnimation;activity.best=oldBest;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
