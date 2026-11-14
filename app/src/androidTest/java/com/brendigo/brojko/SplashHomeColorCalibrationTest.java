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

    @Test public void homeHeroBevelRetainsRealButtonActionsAndUnchangedGameState(){
        rule.getScenario().onActivity(activity->{
            String beforePage=activity.page;
            int beforeTheme=activity.theme;
            TileGame beforeGame=activity.game;
            String serialized=beforeGame==null?null:beforeGame.toJson().toString();
            int beforeBest=activity.best;
            try{
                for(int theme: new int[]{0,1,2}){
                    for(int[] viewport:new int[][]{{941,1672},{360,800},
                            {1080,2400}}){
                        activity.page="home";
                        activity.theme=theme;
                        MainActivity.BoardScreen view=activity.new BoardScreen(activity);
                        view.measure(View.MeasureSpec.makeMeasureSpec(viewport[0],
                                View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(viewport[1],
                                View.MeasureSpec.EXACTLY));
                        view.layout(0,0,viewport[0],viewport[1]);
                        Bitmap image=Bitmap.createBitmap(viewport[0],viewport[1],
                            Bitmap.Config.ARGB_8888);
                        try{
                            view.draw(new Canvas(image));
                            boolean livePlay=false;
                            for(MainActivity.BoardScreen.Tap tap:view.taps)
                                if(tap.label.contains("Nova igra")||
                                        tap.label.contains("Nastavi igru")){
                                    livePlay=tap.action!=null&&tap.left<=58.01f&&
                                        tap.right>=341.99f;
                                }
                            assertTrue("Bevel must keep the real play hitbox in "+
                                theme+" "+viewport[0]+"x"+viewport[1],livePlay);
                            assertTrue("The illustrated button must still fit",
                                view.offX+342f*view.scale<=viewport[0]+.1f);
                            if(theme==0&&viewport[0]==941){
                                int upper=image.getPixel(350,713);
                                int face=image.getPixel(350,739);
                                assertTrue("Source hero face keeps a warm top",
                                    Color.red(upper)>245&&
                                    Color.red(upper)>Color.green(upper)+95);
                                assertTrue("Bevel fades to the darkened face",
                                    Color.green(upper)>Color.green(face));
                            }
                        }finally{image.recycle();}
                    }
                }
                assertEquals("Pure Canvas bevel must not change best",
                    beforeBest,activity.best);
                assertEquals("Pure Canvas bevel must not replace the TileGame",
                    beforeGame,activity.game);
                if(beforeGame!=null)
                    assertEquals("No game JSON change during preview",
                        serialized,activity.game.toJson().toString());
            }finally{
                activity.page=beforePage;
                activity.theme=beforeTheme;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void lightHomeScoreHeadingsRetainSourceSizedInkBounds(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            try{
                activity.page="home";activity.theme=0;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.auditTextLayout=true;
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap bitmap=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
                try{screen.draw(new Canvas(bitmap));}finally{bitmap.recycle();}
                android.graphics.RectF first=null,last=null;
                for(MainActivity.BoardScreen.TextRegion region:screen.textRegions){
                    if("Najbolji rezultat".equals(region.text))first=region.bounds;
                    if("Zadnji rezultat".equals(region.text))last=region.bounds;
                }
                assertNotNull("First score label must be present",first);
                assertNotNull("Last score label must be present",last);
                assertTrue("Source first header should be compact: "+first,
                    first.width()>=135f&&first.width()<=198f);
                assertTrue("Source last header should be compact: "+last,
                    last.width()>=120f&&last.width()<=187f);
                assertNear("First header center",Math.round(first.centerX()),271,17);
                assertNear("Last header center",Math.round(last.centerX()),701,17);
                assertTrue("Header must not overlap scores",
                    first.bottom<562&&last.bottom<562);
            }finally{activity.page=oldPage;activity.theme=oldTheme;}
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
