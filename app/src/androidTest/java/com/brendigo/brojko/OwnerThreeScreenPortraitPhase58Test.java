package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;

/**
 * Owner's three independent 941x1672 sources: Missions, Daily Challenge and
 * ACTIVE Classic 4x4. Capture real production Canvas and live hitboxes.
 * Never replace the game UI with a static reference image.
 */
@RunWith(AndroidJUnit4.class)
public final class OwnerThreeScreenPortraitPhase58Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static final int[][] OWNER_CLASSIC={
        {2,4,8,16},{8,16,2,4},{32,64,8,2},{128,16,32,256}
    };

    private MainActivity.BoardScreen render(MainActivity a,String page,int width,
                                             int height,boolean exportScreenshot) {
        a.page=page;
        a.theme=0;
        MainActivity.BoardScreen screen=a.new BoardScreen(a);
        screen.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
        screen.layout(0,0,width,height);
        Bitmap pixels=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        try{
            screen.draw(new Canvas(pixels));
            if(exportScreenshot){
                File base=a.getExternalFilesDir(null);
                assertNotNull(base);
                File directory=new File(base,"screenshots");
                assertTrue(directory.isDirectory()||directory.mkdirs());
                File path=new File(directory,"phase58-"+page+"-"+width+"x"+
                    height+".png");
                try(FileOutputStream file=new FileOutputStream(path)){
                    assertTrue("Real PNG from Canvas",pixels.compress(
                        Bitmap.CompressFormat.PNG,100,file));
                }catch(IOException failure){
                    throw new AssertionError("Native screenshot failed",failure);
                }
            }
        }finally{pixels.recycle();}
        return screen;
    }

    private static boolean hasAction(MainActivity.BoardScreen board,String label){
        for(MainActivity.BoardScreen.Tap tap:board.taps)
            if(label.equals(tap.label))return true;
        return false;
    }

    private static void assertPrimaryActions(String page,MainActivity.BoardScreen board){
        if("missions".equals(page)){
            assertTrue("Missions has a functional Back action",
                hasAction(board,"Natrag"));
        }else if("daily".equals(page)){
            assertTrue("Daily challenge has a functional Back action",
                hasAction(board,"Natrag"));
            int helps=0,starts=0;
            for(MainActivity.BoardScreen.Tap tap:board.taps){
                if(tap.label.startsWith("Pomoć za cilj: "))helps++;
                if("Započni izazov".equals(tap.label)||
                   "Nastavi izazov".equals(tap.label)||
                   "Igraj ponovno".equals(tap.label))starts++;
            }
            assertEquals("Three functional Daily task descriptions",3,helps);
            assertEquals("One real Daily start/resume action",1,starts);
        }else{
            assertTrue("Classic New Game remains functional",
                hasAction(board,"Novo"));
            assertTrue("Classic Finish remains functional",
                hasAction(board,"Završi"));

            assertTrue("Classic Pause remains functional",
                hasAction(board,"Pauziraj igru"));
        }
    }

    @Test public void originalAndTallPortraitsReuseExactlySameOwnerCanvas(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme,oldBest=activity.best;
            int oldMerges=activity.todayMerges,oldWeek=activity.weekPoints,
                oldStreak=activity.playDayStreak,oldTile=activity.recordTile,
                oldFlags=activity.rewardFlags,oldDailyStreak=activity.streak;
            String oldLastDaily=activity.lastDaily;
            TileGame oldGame=activity.game;
            try{
                activity.game=new TileGame("classic","",2124L);
                // Source has an ACTIVE Undo tile. First perform a
                // genuine merge to enter the live undo-enabled state,
                // then load the source's illustrative visible board.
                // Never force an enabled button in production code.
                activity.game.cells=new int[]{2,2,4,8,8,16,2,4,
                    32,64,8,2,128,16,32,256};
                assertTrue("Real move enables Undo",
                    activity.game.move(TileGame.LEFT).changed);
                assertTrue("Undo snapshot exists",
                    activity.game.canUndo());
                activity.game.cells=new int[]{2,4,8,16,8,16,2,4,
                    32,64,8,2,128,16,32,256};
                activity.game.score=1316;
                activity.best=3864;
                activity.todayMerges=12;
                activity.weekPoints=6250;
                activity.playDayStreak=5;
                activity.recordTile=512;
                activity.rewardFlags=0;
                activity.streak=7;
                activity.lastDaily="";
                for(String page:new String[]{"missions","daily","game"}){
                    for(int[] size:new int[][]{{941,1672},{360,800},
                        {393,873},{1080,2400},{1440,3200}}){
                        int w=size[0],h=size[1];
                        boolean capture=(w==941||w==360||w==1080);
                        MainActivity.BoardScreen view=render(
                            activity,page,w,h,capture);
                        assertTrue("Owner portrait composition: "+page+" "+w+
                            "x"+h,view.referencePortraitLayout);
                        assertEquals("Do not stretch source X and Y differently",
                            w/400f,view.scale,.002f);
                        assertEquals("Keep 400-unit artwork within the viewport",
                            0f,view.offX,.2f);
                        assertEquals("No vertical clipping of owner scene",
                            w==941?0f:(h-710f*view.scale)/2f,
                            view.offY,.2f);
                        assertEquals("No phantom scroll on tall portraits",
                            0f,view.maxScroll,.02f);
                        float last="missions".equals(page)?709f:
                            "daily".equals(page)?704f:671f;
                        assertTrue("The full owner source scene fits",
                            view.offY+last*view.scale<=h+1f);
                        assertPrimaryActions(page,view);
                        if("game".equals(page)){
                            assertTrue("After a genuine merge Undo is an active action",
                                hasAction(view,"Poništi"));
                        }
                        for(MainActivity.BoardScreen.Tap tap:view.taps){
                            assertTrue("Action not clipped vertically: "+page+
                                " / "+tap.label,
                                view.offY+tap.top*view.scale>=-1f &&
                                view.offY+tap.bottom*view.scale<=h+1f);
                        }
                    }
                }
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.best=oldBest;activity.todayMerges=oldMerges;
                activity.weekPoints=oldWeek;activity.playDayStreak=oldStreak;
                activity.recordTile=oldTile;activity.rewardFlags=oldFlags;
                activity.streak=oldDailyStreak;
                activity.lastDaily=oldLastDaily;activity.game=oldGame;
                activity.screen.invalidate();
            }
        });
    }

    @Test public void compactScreensScrollWithoutLosingEndActions(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            TileGame oldGame=activity.game;
            try{
                activity.game=new TileGame("classic","",2124L);
                for(String page:new String[]{"missions","daily","game"}){
                    for(int[] size:new int[][]{{320,480},{360,600}}){
                        MainActivity.BoardScreen v=render(activity,page,
                            size[0],size[1],false);
                        assertTrue("No clipped unscrollable source: "+page,
                            v.maxScroll>0f);
                        assertPrimaryActions(page,v);
                        float last="missions".equals(page)?709f:
                            "daily".equals(page)?704f:671f;
                        assertTrue("Bottom content can be reached with scroll",
                            v.offY+(last-v.maxScroll)*v.scale<=size[1]+1f);
                    }
                }
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.game=oldGame;activity.screen.invalidate();
            }
        });
    }
}
