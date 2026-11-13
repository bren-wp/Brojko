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
 * The 941x1672 Themes visual calibration must remain a pure Canvas change.
 * Never replace game progress with a reference PNG or demo score.
 */
@RunWith(AndroidJUnit4.class)
public final class OwnerThemesProgressPreservationPhase140Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void previewAcrossStylesAndScreenSizesDoesNotMutateSavedSession() {
        rule.getScenario().onActivity(activity -> {
            TileGame savedGame=activity.game;
            String savedPage=activity.page;
            int savedTheme=activity.theme;
            int savedStyle=activity.tileStyle;
            int savedBest=activity.best;
            int savedAnimation=activity.animation;
            boolean savedSound=activity.sound;
            boolean savedVibration=activity.vibration;
            try {
                TileGame fixture=new TileGame("classic","",140L);
                fixture.cells=new int[]{
                    2,4,8,16,32,64,128,256,
                    512,1024,2048,0,2,4,8,16};
                fixture.score=9216;
                fixture.moves=42;
                fixture.peak=2048;
                activity.game=fixture;
                String before=fixture.toJson().toString();
                activity.best=16384;
                activity.sound=true;
                activity.vibration=false;
                activity.animation=1;
                activity.theme=0;
                activity.page="themes";
                for(int style=0;style<3;style++){
                    activity.tileStyle=style;
                    for(int[] size:new int[][]{
                            {941,1672},{360,800},{1080,2400}}){
                        MainActivity.BoardScreen view=activity.new BoardScreen(activity);
                        view.measure(View.MeasureSpec.makeMeasureSpec(
                                size[0],View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(
                                size[1],View.MeasureSpec.EXACTLY));
                        view.layout(0,0,size[0],size[1]);
                        Bitmap bitmap=Bitmap.createBitmap(size[0],size[1],
                            Bitmap.Config.ARGB_8888);
                        try {
                            view.draw(new Canvas(bitmap));
                            assertTrue("Themes must fit screen width",
                                view.scale>0f && view.offX>=-.1f &&
                                view.offX+400f*view.scale<=size[0]+.1f);
                            boolean activeLight=false,activeVivid=false;
                            for(MainActivity.BoardScreen.Tap tap:view.taps){
                                activeLight|="Svijetla".equals(tap.label) &&
                                    tap.action!=null;
                                activeVivid|="Živopisna".equals(tap.label) &&
                                    tap.action!=null;
                            }
                            assertTrue("Real theme hitboxes at all sizes",
                                activeLight&&activeVivid);
                        } finally {bitmap.recycle();}
                        assertEquals("Canvas must not change active session",
                            before,fixture.toJson().toString());
                        assertEquals("Reference style must not change best",
                            16384,activity.best);
                        assertTrue("Theme preview does not flip sound",
                            activity.sound);
                        assertFalse("Theme preview does not flip vibration",
                            activity.vibration);
                        assertEquals("Theme preview does not change animation",
                            1,activity.animation);
                    }
                }
            }finally{
                activity.game=savedGame;
                activity.page=savedPage;
                activity.theme=savedTheme;
                activity.tileStyle=savedStyle;
                activity.best=savedBest;
                activity.animation=savedAnimation;
                activity.sound=savedSound;
                activity.vibration=savedVibration;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }
}
