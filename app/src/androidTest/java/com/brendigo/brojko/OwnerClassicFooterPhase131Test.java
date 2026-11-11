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

/** Classic 4x4 bottom action material against the 941x1672 owner reference. */
@RunWith(AndroidJUnit4.class)
public class OwnerClassicFooterPhase131Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void sourceBottomActionFacesAndLiveCommands(){
        rule.getScenario().onActivity(activity -> {
            TileGame old=activity.game;
            String page=activity.page;
            int theme=activity.theme;
            try{
                activity.theme=0;activity.page="game";
                activity.game=new TileGame("classic","",131L);
                activity.game.cells=new int[]{
                    2,4,8,16,
                    8,16,2,4,
                    32,64,8,2,
                    128,16,32,256
                };
                activity.game.peak=256;
                activity.game.score=1316;
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
                    assertTrue("Source 941x1672 portrait",screen.referencePortraitLayout);
                    // Previously the finish button ended near y1580,
                    // leaving pure canvas at native (735,1590).
                    int lowerFace=image.getPixel(735,1590);
                    assertTrue("Finish face now reaches owner lower edge",
                        Color.red(lowerFace)>225 &&
                        Color.green(lowerFace)<175 &&
                        Color.blue(lowerFace)<135);
                    // Letter baselines must extend into y1530..1556
                    // without baking a text screenshot into the app.
                    int navy=0;
                    for(int y=1530;y<=1562;y++)
                        for(int x=115;x<=232;x++){
                            int rgb=image.getPixel(x,y);
                            if(Color.red(rgb)<90&&Color.green(rgb)<120&&
                                Color.blue(rgb)<170)navy++;
                        }
                    assertTrue("Native Novo caption baseline",navy>100);
                    assertEquals("Rendering cannot change score",1316,
                        activity.game.score);
                    boolean newGame=false,finish=false;
                    for(MainActivity.BoardScreen.Tap tap:screen.taps){
                        if("Novo".equals(tap.label))newGame=tap.action!=null;
                        if("Završi".equals(tap.label))finish=tap.action!=null;
                    }
                    assertTrue("New game remains actionable",newGame);
                    assertTrue("Finish remains actionable",finish);
                }finally{image.recycle();}
            }finally{
                activity.game=old;
                activity.page=page;activity.theme=theme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
