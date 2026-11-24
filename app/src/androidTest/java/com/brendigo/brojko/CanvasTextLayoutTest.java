package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class CanvasTextLayoutTest {
    @Test public void bidiAndComplexScriptsDrawUsingSystemShaping(){
        String[] tags={"ar","he","hi","ja","ko","zh-Hans"};
        String[] labels={"مرحبا 2048","שלום 2048","नमस्ते 2048","こんにちは 2048","안녕하세요 2048","你好 2048"};
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(26f);
        paint.setTextAlign(Paint.Align.CENTER);
        for(int i=0;i<tags.length;i++){
            Locale locale=Locale.forLanguageTag(tags[i]);
            assertTrue(CanvasTextLayout.requiresLayout(labels[i],locale));
            assertTrue(CanvasTextLayout.measure(labels[i],paint,locale)>0);
            Bitmap bitmap=Bitmap.createBitmap(400,80,Bitmap.Config.ARGB_8888);
            try{
                CanvasTextLayout.draw(new Canvas(bitmap),labels[i],200f,54f,paint,locale);
                int count=0;
                for(int y=0;y<80;y++)for(int x=0;x<400;x++)
                    if(Color.alpha(bitmap.getPixel(x,y))>0)count++;
                assertTrue(tags[i]+": expected shaped text pixels",count>20);
            }finally{bitmap.recycle();}
        }
        assertTrue(LocaleManager.isRtl(Locale.forLanguageTag("ar")));
        assertTrue(LocaleManager.isRtl(Locale.forLanguageTag("he")));
        assertFalse(CanvasTextLayout.requiresLayout("English 2048",Locale.ENGLISH));
    }
}
