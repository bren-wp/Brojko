package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real Android accessibility provider regression for Canvas virtual buttons. */
@RunWith(AndroidJUnit4.class)
public class BoardAccessibilityVirtualTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void settingsButtonsExposeIndependentTalkBackNodes(){
        rule.getScenario().onActivity(activity -> {
            String previous=activity.page;
            MainActivity.BoardScreen view=activity.screen;
            Bitmap bitmap=null;
            try{
                activity.navigate("settings");
                int width=Math.max(1,view.getWidth());
                int height=Math.max(1,view.getHeight());
                bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(bitmap));
                assertTrue("Real Canvas settings buttons must exist",view.taps.size()>2);
                AccessibilityNodeProvider provider=view.getAccessibilityNodeProvider();
                assertNotNull("Virtual accessibility provider must be installed",provider);
                AccessibilityNodeInfo root=provider.createAccessibilityNodeInfo(-1);
                assertNotNull("Canvas root must be inspectable",root);
                assertTrue("Each real button needs a virtual child",root.getChildCount()>0);
                AccessibilityNodeInfo first=provider.createAccessibilityNodeInfo(1);
                assertNotNull("First visible virtual control must exist",first);
                assertEquals("Virtual node must expose the live button label",
                    view.taps.get(0).label,first.getContentDescription().toString());
                assertTrue("Virtual button must be clickable",first.isClickable());
                Rect bounds=new Rect();
                first.getBoundsInScreen(bounds);
                assertFalse("TalkBack focus bounds cannot be empty",bounds.isEmpty());
                first.recycle();
                root.recycle();
            }finally{
                if(bitmap!=null)bitmap.recycle();
                activity.navigate(previous);
            }
        });
    }

    @Test public void virtualBackControlPerformsTheRealNavigation(){
        rule.getScenario().onActivity(activity -> {
            MainActivity.BoardScreen view=activity.screen;
            Bitmap bitmap=null;
            try{
                activity.navigate("settings");
                bitmap=Bitmap.createBitmap(Math.max(view.getWidth(),1),
                    Math.max(view.getHeight(),1),Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(bitmap));
                int back=-1;
                for(int i=0;i<view.taps.size();i++){
                    if("Natrag".equals(view.taps.get(i).label)){
                        back=i+1;
                        break;
                    }
                }
                assertTrue("Settings must expose the real Back button",back>0);
                AccessibilityNodeProvider provider=view.getAccessibilityNodeProvider();
                assertNotNull(provider);
                assertTrue("TalkBack click must activate Back",
                    provider.performAction(back,AccessibilityNodeInfo.ACTION_CLICK,null));
                assertEquals("home",activity.page);
            }finally{
                if(bitmap!=null)bitmap.recycle();
                activity.navigate("home");
            }
        });
    }
}
