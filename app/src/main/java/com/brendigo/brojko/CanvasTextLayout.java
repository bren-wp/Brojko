package com.brendigo.brojko;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import java.util.Locale;

/** Shaped single-line text for mixed-direction and complex-script Canvas labels. */
final class CanvasTextLayout {
    private CanvasTextLayout(){}

    static boolean requiresLayout(String text,Locale locale){
        return LocaleManager.isRtl(locale)||requiresScriptFallback(text);
    }

    private static boolean requiresScriptFallback(String text){
        for(int i=0;i<text.length();){
            int cp=text.codePointAt(i);
            i+=Character.charCount(cp);
            if(cp<0x590)continue;
            byte direction=Character.getDirectionality(cp);
            if(direction==Character.DIRECTIONALITY_RIGHT_TO_LEFT||
               direction==Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC||
               direction==Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING||
               direction==Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE||
               direction==Character.DIRECTIONALITY_RIGHT_TO_LEFT_ISOLATE)return true;
            switch(Character.UnicodeScript.of(cp)){
                case DEVANAGARI: case ARABIC: case HEBREW: case HAN:
                case HIRAGANA: case KATAKANA: case HANGUL: case THAI:
                    return true;
                default: break;
            }
        }
        return false;
    }

    private static TextPaint shapedPaint(String text,Paint paint,Locale locale){
        TextPaint textPaint=new TextPaint(paint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextLocale(locale);
        Typeface current=paint.getTypeface();
        // Android's system family provides the script-specific font fallback.
        if(requiresScriptFallback(text))textPaint.setTypeface(Typeface.create("sans-serif",
            current!=null&&current.isBold()?Typeface.BOLD:Typeface.NORMAL));
        return textPaint;
    }

    static float measure(String text,Paint paint,Locale locale){
        if(!requiresLayout(text,locale))return paint.measureText(text);
        return Layout.getDesiredWidth(text,shapedPaint(text,paint,locale));
    }

    static void draw(Canvas canvas,String text,float x,float baseline,Paint paint,Locale locale){
        if(text.isEmpty())return;
        if(!requiresLayout(text,locale)){
            canvas.drawText(text,x,baseline,paint);
            return;
        }
        TextPaint textPaint=shapedPaint(text,paint,locale);
        int width=Math.max(1,(int)Math.ceil(Layout.getDesiredWidth(text,textPaint))+2);
        StaticLayout layout=StaticLayout.Builder.obtain(text,0,text.length(),textPaint,width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(LocaleManager.isRtl(locale)?
                TextDirectionHeuristics.FIRSTSTRONG_RTL:TextDirectionHeuristics.FIRSTSTRONG_LTR)
            // Single line: builder defaults are simple breaking and no hyphenation.
            .setIncludePad(false).setMaxLines(1).build();
        float lineLeft=layout.getLineLeft(0);
        float advance=layout.getLineRight(0)-lineLeft;
        float anchor=paint.getTextAlign()==Paint.Align.CENTER?advance/2:
            paint.getTextAlign()==Paint.Align.RIGHT?advance:0;
        canvas.save();
        try{
            canvas.translate(x-anchor-lineLeft,baseline-layout.getLineBaseline(0));
            layout.draw(canvas);
        }finally{canvas.restore();}
    }
}
