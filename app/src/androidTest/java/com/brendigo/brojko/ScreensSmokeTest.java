package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/**
 * Android device/emulator QA: launches the actual Activity, renders every view,
 * checks primary navigation and exports unmodified 400×880 screenshots.
 */
@RunWith(AndroidJUnit4.class)
public class ScreensSmokeTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private void capture(MainActivity activity,String name) {
        MainActivity.BoardScreen view=activity.screen;
        view.measure(View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(880,View.MeasureSpec.EXACTLY));
        view.layout(0,0,400,880);
        Bitmap bitmap=Bitmap.createBitmap(400,880,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        File root=activity.getExternalFilesDir(null);
        assertNotNull(root);
        File directory=new File(root,"screenshots");
        assertTrue(directory.isDirectory()||directory.mkdirs());
        File image=new File(directory,name+".png");
        try(FileOutputStream output=new FileOutputStream(image)){
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));
        }catch(Exception error){throw new AssertionError("Could not save screenshot "+name,error);}
        finally{bitmap.recycle();}
    }

    @Test public void renderAllScreensAndGameFlow() {
        rule.getScenario().onActivity(activity->{
            activity.newGame("classic");
            String[] pages={
                "home","game","pause","win","over","confirm","confirmEnd",
                "daily","missions","stats","themes","settings",
                "language","backup","privacy","about"
            };
            for(String page:pages){
                activity.page=page;
                capture(activity,page);
            }
            activity.page="home";
            capture(activity,"home-ready");
            float x=200*activity.screen.scale+activity.screen.offX;
            float y=374*activity.screen.scale+activity.screen.offY;
            long time=android.os.SystemClock.uptimeMillis();
            MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,x,y,0);
            MotionEvent up=MotionEvent.obtain(time,time+30,MotionEvent.ACTION_UP,x,y,0);
            activity.screen.onTouchEvent(down);
            activity.screen.onTouchEvent(up);
            down.recycle();up.recycle();
            assertEquals("game",activity.page);
            capture(activity,"game-active");
            assertNotNull(activity.game);
            assertTrue(activity.game.cells.length==16||activity.game.cells.length==25);
        });
    }
}
