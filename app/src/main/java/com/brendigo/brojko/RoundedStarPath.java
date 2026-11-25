package com.brendigo.brojko;

import android.graphics.Path;

/** Shared rounded-star contour; callers retain their reference-specific vertices. */
final class RoundedStarPath {
    private RoundedStarPath() {}

    static Path create(float[] x,float[] y,float corner){
        if(x.length!=10||y.length!=10)throw new IllegalArgumentException("Ten vertices required");
        Path star=new Path();
        for(int i=0;i<10;i++){
            int previous=(i+9)%10,next=(i+1)%10;
            float startX=x[i]*corner+x[previous]*(1f-corner);
            float startY=y[i]*corner+y[previous]*(1f-corner);
            float endX=x[i]*corner+x[next]*(1f-corner);
            float endY=y[i]*corner+y[next]*(1f-corner);
            if(i==0)star.moveTo(startX,startY);
            else star.lineTo(startX,startY);
            star.quadTo(x[i],y[i],endX,endY);
        }
        star.close();
        return star;
    }
}
