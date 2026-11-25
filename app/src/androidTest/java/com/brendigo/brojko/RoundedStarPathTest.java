package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Compare actual native raster pixels against the pre-extraction path formula. */
@RunWith(AndroidJUnit4.class)
public final class RoundedStarPathTest {
    @Test public void allReferenceCornerVariantsRetainExactRaster(){
        for(float corner:new float[]{.64f,.72f,.73f,.76f,.82f}){
            for(float[] radii:new float[][]{{10.2f,5.15f},{10.6f,6.45f},{10.6f,5.85f},{10.6f,5.75f},{10.6f,5.35f},{11.3f,7.1f}}){
                float[] x=new float[10],y=new float[10];
                for(int i=0;i<10;i++){
                    double angle=-Math.PI/2+i*Math.PI/5;
                    float radius=radii[i%2];
                    x[i]=12+(float)Math.cos(angle)*radius;
                    y[i]=12+(float)Math.sin(angle)*radius;
                }
                Path expected=new Path();
                for(int i=0;i<10;i++){
                    int prev=(i+9)%10,next=(i+1)%10;
                    float startX=x[i]*corner+x[prev]*(1f-corner);
                    float startY=y[i]*corner+y[prev]*(1f-corner);
                    float endX=x[i]*corner+x[next]*(1f-corner);
                    float endY=y[i]*corner+y[next]*(1f-corner);
                    if(i==0)expected.moveTo(startX,startY);
                    else expected.lineTo(startX,startY);
                    expected.quadTo(x[i],y[i],endX,endY);
                }
                expected.close();
                Bitmap oldImage=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888);
                Bitmap newImage=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888);
                try{
                    Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
                    Canvas oldCanvas=new Canvas(oldImage),newCanvas=new Canvas(newImage);
                    oldCanvas.scale(4,4);newCanvas.scale(4,4);
                    oldCanvas.drawPath(expected,paint);
                    newCanvas.drawPath(RoundedStarPath.create(x,y,corner),paint);
                    assertTrue("Contour raster must remain identical",oldImage.sameAs(newImage));
                }finally{oldImage.recycle();newImage.recycle();}
            }
        }
    }
}
