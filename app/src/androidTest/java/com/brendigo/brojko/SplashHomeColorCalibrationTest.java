package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Source-matched 941 x 1672 light Splash/Home color regression.
 * Verifies the production Canvas, not a screenshot pasted into the app.
 */
@RunWith(AndroidJUnit4.class)
public final class SplashHomeColorCalibrationTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static void assertNear(String label,int actual,int target,int tolerance){
        assertTrue(label + ": expected " + target + " +/- " + tolerance
            + " but was " + actual,Math.abs(actual-target)<=tolerance);
    }

    @Test public void lightHomeHeroDarkensToRightAndScoresKeepIvoryFace(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
            try{
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                activity.page="home";
                activity.theme=0;
                screen.draw(new Canvas(bitmap));
                // Samples stay outside the CTA lettering, play icon and bevel.
                int left=bitmap.getPixel(300,740);
                int right=bitmap.getPixel(730,740);
                assertNear("Home hero coral left green",Color.green(left),121,13);
                assertNear("Home hero coral right green",Color.green(right),102,13);
                assertTrue("Source CTA must darken across the face",
                    Color.green(left)-Color.green(right)>=12);
                assertNear("Warm score-card green",Color.green(bitmap.getPixel(300,625)),249,3);
                assertNear("Warm score-card blue",Color.blue(bitmap.getPixel(300,625)),238,4);

                boolean hasPlay=false,hasClassic=false,hasRelaxed=false,hasDaily=false;
                for(MainActivity.BoardScreen.Tap tap:screen.taps){
                    hasPlay|=tap.label.contains("Nova igra")||tap.label.contains("Nastavi igru");
                    hasClassic|=tap.label.contains("Klasično");
                    hasRelaxed|=tap.label.contains("Opušteno");
                    hasDaily|=tap.label.contains("Dnevni izazov");
                }
                assertTrue("Main CTA remains interactive",hasPlay);
                assertTrue("All three game modes remain interactive",
                    hasClassic&&hasRelaxed&&hasDaily);
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;bitmap.recycle();
            }
        });
    }

    @Test public void lightSplashTaglineRetainsPaleIvoryBehindLiveText(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
            try{
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                activity.page="splash";activity.theme=0;
                screen.draw(new Canvas(bitmap));
                int pill=bitmap.getPixel(450,1325);
                assertNear("Tagline pill green",Color.green(pill),245,4);
                assertNear("Tagline pill blue",Color.blue(pill),230,6);
                assertTrue("Splash never scrolls",screen.maxScroll==0f);
                assertTrue("Splash has no functional game hit targets",screen.taps.isEmpty());
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;bitmap.recycle();
            }
        });
    }
}
