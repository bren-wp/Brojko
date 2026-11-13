package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Prevent visual calibration from rewriting an existing 2048 session.
 * Reference PNGs are not bundled in the app.
 */
@RunWith(AndroidJUnit4.class)
public final class FourScreenVisualProgressPhase141Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void renderFourScreensAndThreeViewportsWithoutSavingDemoData() {
        rule.getScenario().onActivity(activity -> {
            TileGame originalGame=activity.game;
            String originalPage=activity.page;
            int originalTheme=activity.theme,originalStyle=activity.tileStyle;
            int originalBest=activity.best,originalAnimation=activity.animation;
            boolean originalSound=activity.sound,originalVibration=activity.vibration;
            try {
                TileGame active=new TileGame("classic","",141L);
                active.cells=new int[]{2,4,8,16,4,8,16,32,8,16,32,64,
                    128,256,512,1024};
                active.score=12864;
                active.moves=37;
                active.merges=19;
                active.peak=1024;
                activity.game=active;
                activity.theme=0;
                activity.tileStyle=0;
                activity.best=22528;
                activity.animation=1;
                activity.sound=true;
                activity.vibration=true;
                String session=active.toJson().toString();
                for(String page:new String[]{"themes","settings","daily","stats"}){
                    for(int[] size:new int[][]{
                            {941,1672},{360,600},{1080,2400}}){
                        activity.page=page;
                        MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                        screen.measure(View.MeasureSpec.makeMeasureSpec(
                                size[0],View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(
                                size[1],View.MeasureSpec.EXACTLY));
                        screen.layout(0,0,size[0],size[1]);
                        Bitmap frame=Bitmap.createBitmap(size[0],size[1],
                            Bitmap.Config.ARGB_8888);
                        try {
                            screen.draw(new Canvas(frame));
                            assertTrue("Live 400-unit layout fits width: "+page,
                                screen.scale>0f && screen.offX>=-.1f &&
                                screen.offX+400f*screen.scale<=size[0]+.1f);
                            assertFalse("Screen contains real hitboxes: "+page,
                                screen.taps.isEmpty());
                        }finally{frame.recycle();}
                        assertEquals("Rendered UI must not rewrite progress on "+page,
                            session,active.toJson().toString());
                        assertEquals("Record must remain unchanged",22528,activity.best);
                        assertEquals("Theme unchanged",0,activity.theme);
                        assertEquals("Tile style unchanged",0,activity.tileStyle);
                        assertEquals("Animation setting unchanged",1,activity.animation);
                        assertTrue("Sound setting unchanged",activity.sound);
                        assertTrue("Vibration setting unchanged",activity.vibration);
                    }
                }
            }finally{
                activity.game=originalGame;
                activity.page=originalPage;
                activity.theme=originalTheme;
                activity.tileStyle=originalStyle;
                activity.best=originalBest;
                activity.animation=originalAnimation;
                activity.sound=originalSound;
                activity.vibration=originalVibration;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }
}
