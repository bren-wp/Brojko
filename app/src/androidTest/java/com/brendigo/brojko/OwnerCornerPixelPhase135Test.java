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

/** Phase 135: source-corner coordinates and true navy back-arrow footprint. */
@RunWith(AndroidJUnit4.class)
public class OwnerCornerPixelPhase135Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static Bitmap actualPortrait(MainActivity activity,String name){
        activity.page=name;
        MainActivity.BoardScreen canvas=activity.new BoardScreen(activity);
        canvas.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
        canvas.layout(0,0,941,1672);
        Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
        canvas.draw(new Canvas(bitmap));
        return bitmap;
    }

    @Test public void homeCornerNumbersAreInsideOwnerReferenceBands(){
        rule.getScenario().onActivity(activity -> {
            String previousPage=activity.page;int previousTheme=activity.theme;
            try{
                activity.theme=0;
                Bitmap bitmap=actualPortrait(activity,"home");
                try{
                    long blueY=0,goldY=0;int bluePixels=0,goldPixels=0;
                    for(int y=220;y<470;y++)for(int x=0;x<155;x++){
                        int rgb=bitmap.getPixel(x,y);
                        int r=Color.red(rgb),g=Color.green(rgb),b=Color.blue(rgb);
                        if(b>r+55 && b>g+8){bluePixels++;blueY+=y;}
                    }
                    for(int y=220;y<510;y++)for(int x=760;x<941;x++){
                        int rgb=bitmap.getPixel(x,y);
                        int r=Color.red(rgb),g=Color.green(rgb),b=Color.blue(rgb);
                        if(r>b+80 && g>b+40){goldPixels++;goldY+=y;}
                    }
                    assertTrue("Blue 8 artwork must be present",bluePixels>8000);
                    assertTrue("Gold 16 artwork must be present",goldPixels>10000);
                    float blueCenterY=blueY/(float)bluePixels;
                    float goldCenterY=goldY/(float)goldPixels;
                    // Reference centres: blue y~330, gold y~371.
                    // Fail when either falls back to v1.12.216 y~405/425.
                    assertTrue("Owner blue tile y "+blueCenterY,
                        blueCenterY>=280f && blueCenterY<=371f);
                    assertTrue("Owner gold tile y "+goldCenterY,
                        goldCenterY>=325f && goldCenterY<=402f);
                }finally{bitmap.recycle();}
            }finally{
                activity.page=previousPage;activity.theme=previousTheme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }

    @Test public void missionsBackArrowHasReferenceNavyInkAndRealAction(){
        rule.getScenario().onActivity(activity -> {
            String oldPage=activity.page;int oldTheme=activity.theme;
            try{
                activity.theme=0;
                activity.page="missions";
                MainActivity.BoardScreen canvas=activity.new BoardScreen(activity);
                canvas.measure(View.MeasureSpec.makeMeasureSpec(941,
                    View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(
                    1672,View.MeasureSpec.EXACTLY));
                canvas.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    canvas.draw(new Canvas(bitmap));
                    int navy=0;
                    for(int y=58;y<130;y++)for(int x=45;x<125;x++){
                        int rgb=bitmap.getPixel(x,y);
                        if(Color.red(rgb)<80 && Color.green(rgb)<100 &&
                                Color.blue(rgb)<150)navy++;
                    }
                    // Reference ink spans about 827 pixels, not 293-pixel
                    // chevron from the previous real emulator image.
                    assertTrue("Source Missions back arrow ink "+navy,navy>=540);
                    boolean actionable=false;
                    for(MainActivity.BoardScreen.Tap tap:canvas.taps)
                        if("Natrag".equals(tap.label)&&tap.right>tap.left+45f &&
                                tap.bottom>tap.top+45f)actionable=true;
                    assertTrue("Back arrow remains a real accessible action",
                        actionable);
                }finally{bitmap.recycle();}
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
