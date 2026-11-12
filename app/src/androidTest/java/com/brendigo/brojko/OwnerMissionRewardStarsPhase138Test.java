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

/** Owner source: verify 6 genuine reward star illustrations, never mock scores. */
@RunWith(AndroidJUnit4.class)
public final class OwnerMissionRewardStarsPhase138Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void allSixRewardStarsAlignToMeasuredSourceCenters() {
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try {
                activity.page="missions";
                activity.theme=0;
                MainActivity.BoardScreen view=activity.new BoardScreen(activity);
                view.measure(View.MeasureSpec.makeMeasureSpec(
                    941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(
                    1672,View.MeasureSpec.EXACTLY));
                view.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try {
                    view.draw(new Canvas(image));
                    // Owner's six gold masks at x≈822, 941×1672 source.
                    // The wide, separate y-intervals exclude all label ink.
                    int[][] samples={
                        {415,510,456}, {610,695,634},
                        {890,975,928}, {1065,1155,1106},
                        {1340,1460,1390}, {1520,1630,1556}
                    };
                    for(int i=0;i<samples.length;i++){
                        int[] interval=samples[i];
                        long sumY=0,sumX=0;int pixels=0;
                        for(int y=interval[0];y<interval[1];y++)
                            for(int x=775;x<865;x++){
                                int c=image.getPixel(x,y);
                                int red=Color.red(c),green=Color.green(c),
                                    blue=Color.blue(c);
                                if(red>200 && green>100 && green<235 &&
                                   blue<115 && red>green+12){
                                    sumY+=y;sumX+=x;pixels++;
                                }
                            }
                        assertTrue("Gold reward icon missing in row "+i,
                            pixels>1300);
                        double centerY=sumY/(double)pixels;
                        double centerX=sumX/(double)pixels;
                        assertEquals("Reward star vertical source center "+i,
                            interval[2],centerY,5.5d);
                        assertEquals("Reward star horizontal source center "+i,
                            822d,centerX,3d);
                    }
                    // Keep all six real progress/reward illustrations.
                    assertTrue("Missions must retain its real accessible Back",
                        view.taps.size()>0);
                }finally{image.recycle();}
            }finally{
                activity.page=oldPage;
                activity.theme=oldTheme;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }
}
