package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Source-size QA for the local-backup row; does not invent cloud sync. */
@RunWith(AndroidJUnit4.class)
public class OwnerSettingsBackupPhase132Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static RectF ink(MainActivity.BoardScreen screen,String label){
        for(MainActivity.BoardScreen.TextRegion region:screen.textRegions)
            if(label.equals(region.text))return region.bounds;
        return null;
    }

    @Test public void sourceBackupTypographyAndRealNavigationRemainIntact(){
        rule.getScenario().onActivity(activity->{
            String previousPage=activity.page;
            int previousTheme=activity.theme;
            try{
                activity.page="settings";
                activity.theme=0;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                screen.auditTextLayout=true;
                screen.measure(View.MeasureSpec.makeMeasureSpec(941,
                        View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1672,
                        View.MeasureSpec.EXACTLY));
                screen.layout(0,0,941,1672);
                Bitmap rendered=Bitmap.createBitmap(941,1672,
                    Bitmap.Config.ARGB_8888);
                try{
                    screen.draw(new Canvas(rendered));
                    assertTrue("Owner reference viewport must remain active",
                        screen.referencePortraitLayout);
                    RectF title=ink(screen,"Sinkronizacija napretka");
                    RectF subtitle=ink(screen,"Lokalna kopija");
                    assertNotNull("Backup title must be rendered",title);
                    assertNotNull("Honest local-only caption must remain",subtitle);
                    // Reference dark ink: x215..628/y899..937; previously
                    // deployed Android: x216..642/y873..909.
                    // Permit font raster/density rounding, but reject the
                    // previous clearly over-wide and high positioning.
                    assertTrue("Backup heading near source x position",
                        title.left>=202f && title.left<=233f &&
                        title.right>=608f && title.right<=648f);
                    assertTrue("Backup heading has source-like width",
                        title.width()<=427f && title.width()>=379f);
                    assertTrue("Backup heading near source baseline",
                        title.top>=891f && title.top<=919f &&
                        title.bottom>=926f && title.bottom<=955f);
                    assertTrue("Subtitle must be below, not over the title",
                        subtitle.top>=title.bottom);
                    assertTrue("Backup subtitle must fit the same card",
                        subtitle.bottom<981f);

                    int matches=0;
                    for(MainActivity.BoardScreen.Tap tap:screen.taps){
                        if("Sinkronizacija napretka".equals(tap.label)){
                            matches++;
                            assertNotNull("Real backup action required",tap.action);
                            assertTrue("Backup tap target must stay full-row",
                                tap.right-tap.left>=360f &&
                                tap.bottom-tap.top>=54f);
                        }
                    }
                    assertEquals("Only one backup action should exist",1,matches);
                }finally{rendered.recycle();}
            }finally{
                activity.page=previousPage;
                activity.theme=previousTheme;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void compactBackupChangesDoNotAffectDarkThemeOrSmallPhone(){
        rule.getScenario().onActivity(activity->{
            String page=activity.page;
            int theme=activity.theme;
            try{
                for(int currentTheme:new int[]{0,1}){
                    for(int[] size:new int[][]{{360,800},{941,1672}}){
                        activity.page="settings";
                        activity.theme=currentTheme;
                        MainActivity.BoardScreen screen=
                            activity.new BoardScreen(activity);
                        screen.measure(View.MeasureSpec.makeMeasureSpec(
                                size[0],View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(
                                size[1],View.MeasureSpec.EXACTLY));
                        screen.layout(0,0,size[0],size[1]);
                        Bitmap image=Bitmap.createBitmap(size[0],size[1],
                            Bitmap.Config.ARGB_8888);
                        try{screen.draw(new Canvas(image));}
                        finally{image.recycle();}
                        boolean hasBackup=false;
                        for(MainActivity.BoardScreen.Tap tap:screen.taps)
                            if("Sinkronizacija napretka".equals(tap.label)){
                                hasBackup=tap.action!=null;
                                assertTrue(tap.right<=400f);
                            }
                        assertTrue("Local backup reachable at "+
                            size[0]+"px theme "+currentTheme,hasBackup);
                    }
                }
            }finally{
                activity.page=page;activity.theme=theme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
