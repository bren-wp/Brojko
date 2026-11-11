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

/** Owner's reference-light Themes controls, native 941 x 1672 viewport. */
@RunWith(AndroidJUnit4.class)
public class OwnerThemePickerPhase131Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void inactiveRadiosKeepSourceWhiteHaloAndGreyCore(){
        rule.getScenario().onActivity(activity -> {
            String savedPage=activity.page;
            int savedTheme=activity.theme;
            int savedTileStyle=activity.tileStyle;
            try {
                activity.page="themes";
                activity.theme=0;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap image=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try {
                    screen.draw(new Canvas(image));
                    assertTrue("Source-light reference portrait",screen.referencePortraitLayout);
                    for(int cx:new int[]{477,773}) {
                        int halo=image.getPixel(cx,668);
                        assertTrue("White selector ring, x="+cx,
                            Color.red(halo)>244 && Color.green(halo)>244 &&
                            Color.blue(halo)>244);
                        int core=image.getPixel(cx,690);
                        assertTrue("Grey selector center, x="+cx,
                            Color.red(core)>=213 && Color.red(core)<=229 &&
                            Math.abs(Color.red(core)-Color.green(core))<4);
                    }
                    // Source vivid picker reaches the right edge, but
                    // does not leak purple into the cream left margin.
                    int left=image.getPixel(638,470);
                    int right=image.getPixel(897,550);
                    assertTrue("Left margin stays ivory",Color.red(left)>225);
                    assertTrue("Vivid gradient reaches source right edge",
                        Color.blue(right)>160 && Color.red(right)<205);
                    boolean dark=false,vivid=false;
                    for(MainActivity.BoardScreen.Tap tap:screen.taps){
                        if("Tamna".equals(tap.label))dark=tap.action!=null;
                        if("Živopisna".equals(tap.label))vivid=tap.action!=null;
                    }
                    assertTrue("Dark choice remains actionable",dark);
                    assertTrue("Vivid choice remains actionable",vivid);
                }finally{image.recycle();}
                assertEquals("Draw does not modify stored theme",0,activity.theme);
                assertEquals("Draw does not change tile style",savedTileStyle,
                    activity.tileStyle);
            }finally{
                activity.page=savedPage;
                activity.theme=savedTheme;
                activity.tileStyle=savedTileStyle;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }
}
