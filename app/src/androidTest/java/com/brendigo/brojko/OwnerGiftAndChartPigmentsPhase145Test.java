package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Pixel checks on true 941x1672 Canvas; all scores are isolated QA-only rows. */
@RunWith(AndroidJUnit4.class)
public final class OwnerGiftAndChartPigmentsPhase145Test {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private Bitmap screenshot(MainActivity.BoardScreen screen){
        screen.measure(View.MeasureSpec.makeMeasureSpec(
            941,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(
            1672,View.MeasureSpec.EXACTLY));
        screen.layout(0,0,941,1672);
        Bitmap bmp=Bitmap.createBitmap(941,1672,Bitmap.Config.ARGB_8888);
        screen.draw(new Canvas(bmp));
        return bmp;
    }

    @Test public void ownerGiftKeepsGoldenRibbonsAndDoesNotMutateProgress(){
        rule.getScenario().onActivity(activity->{
            String oldPage=activity.page;
            int oldTheme=activity.theme;
            TileGame ongoing=activity.game;
            String before=ongoing==null?null:ongoing.toJson().toString();
            try{
                activity.page="daily";activity.theme=0;
                MainActivity.BoardScreen screen=
                    activity.new BoardScreen(activity);
                Bitmap png=screenshot(screen);
                try{
                    assertTrue(screen.referencePortraitLayout);
                    int purple=0,gold=0;
                    for(int y=1400;y<1510;y+=3)
                        for(int x=95;x<287;x+=3){
                            int c=png.getPixel(x,y);
                            if(Color.red(c)>85&&Color.blue(c)>130&&
                                    Color.blue(c)>Color.green(c)*1.25f)
                                purple++;
                            if(Color.red(c)>195&&Color.green(c)>105&&
                                    Color.green(c)>Color.blue(c)*1.25f)
                                gold++;
                        }
                    assertTrue("Gift must retain raised purple body",purple>240);
                    assertTrue("Gift must retain two golden straps",gold>70);
                    if(ongoing!=null)assertEquals(before,
                        ongoing.toJson().toString());
                }finally{png.recycle();}
            }finally{
                activity.page=oldPage;activity.theme=oldTheme;
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }

    @Test public void chartTintsAreDrawnOnlyForRealArchivedRows(){
        rule.getScenario().onActivity(activity->{
            SharedPreferences real=activity.prefs;
            SharedPreferences qa=activity.getSharedPreferences(
                "brojko-phase145-qa-only",Context.MODE_PRIVATE);
            String oldPage=activity.page;
            int oldTheme=activity.theme,oldMetric=activity.chartMetric;
            TileGame ongoing=activity.game;
            String session=ongoing==null?null:ongoing.toJson().toString();
            try{
                JSONArray ten=new JSONArray();
                for(int i=0;i<10;i++){
                    JSONObject row=new JSONObject();
                    try{
                        row.put("score",512+i*512);
                        row.put("peak",16+i*16);
                        row.put("day","14. stu 2026.");
                        row.put("mode","classic");
                    }catch(Exception e){throw new AssertionError(e);}
                    ten.put(row);
                }
                String before=ten.toString();
                assertTrue(qa.edit().clear().putString("history",before).commit());
                activity.prefs=qa;
                activity.page="stats";activity.theme=0;activity.chartMetric=0;
                MainActivity.BoardScreen screen=activity.new BoardScreen(activity);
                Bitmap png=screenshot(screen);
                try{
                    assertTrue(screen.referencePortraitLayout);
                    int teal=0,coral=0;
                    for(int y=890;y<1070;y+=3)
                        for(int x=45;x<870;x+=3){
                            int c=png.getPixel(x,y);
                            if(Color.green(c)>195&&Color.blue(c)>165&&
                                Color.red(c)<110)teal++;
                            if(Color.red(c)>225&&Color.green(c)>100&&
                                Color.green(c)<175&&Color.blue(c)<155)coral++;
                        }
                    assertTrue("Real history uses teal bars",teal>60);
                    assertTrue("Real history uses coral bars",coral>60);
                    assertEquals(before,qa.getString("history",null));
                    if(ongoing!=null)assertEquals(session,
                        ongoing.toJson().toString());
                }finally{png.recycle();}
            }finally{
                activity.prefs=real;activity.page=oldPage;
                activity.theme=oldTheme;activity.chartMetric=oldMetric;
                assertTrue(qa.edit().clear().commit());
                activity.updateBars();activity.screen.invalidate();
            }
        });
    }
}
