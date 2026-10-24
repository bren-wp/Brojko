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
 * checks primary navigation and exports screenshots at the emulator's native app-viewport resolution.
 */
@RunWith(AndroidJUnit4.class)
public class ScreensSmokeTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private void capture(MainActivity activity,String name) {
        MainActivity.BoardScreen view=activity.screen;
        // Capture the actual on-device layout without changing its dimensions.
        // Forcing 400x880 while keeping real-device system insets distorted
        // screenshots and hid screen-size problems.
        int width=view.getWidth(),height=view.getHeight();
        assertTrue("View must be laid out by Android",width>0&&height>0);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
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

    @Test public void compactDisplayKeepsBottomActionsReachable(){
        rule.getScenario().onActivity(activity->{
            activity.page="missions";
            MainActivity.BoardScreen view=activity.screen;
            view.measure(View.MeasureSpec.makeMeasureSpec(360,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY));
            view.layout(0,0,360,600);
            Bitmap bitmap=Bitmap.createBitmap(360,600,Bitmap.Config.ARGB_8888);
            try{
                view.draw(new Canvas(bitmap));
                assertTrue("Compact screens should scroll",view.maxScroll>10);
                long time=android.os.SystemClock.uptimeMillis();
                MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,180,500,0);
                MotionEvent move=MotionEvent.obtain(time,time+30,MotionEvent.ACTION_MOVE,180,260,0);
                MotionEvent up=MotionEvent.obtain(time,time+60,MotionEvent.ACTION_UP,180,260,0);
                view.onTouchEvent(down);view.onTouchEvent(move);view.onTouchEvent(up);
                down.recycle();move.recycle();up.recycle();
                view.draw(new Canvas(bitmap));
                assertTrue("User must be able to reach lower cards",view.scrollOffset>10);
                assertEquals("missions",activity.page);
            }finally{bitmap.recycle();}
        });
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
