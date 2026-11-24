package com.brendigo.brojko;

import android.graphics.Paint;

/** Coordinate and gesture mapping independent of labels, game state and theme. */
final class CanvasDirection {
    static final float WIDTH=400f;
    private CanvasDirection(){}
    static float logicalX(float physical,float origin,float scale,boolean rtl){
        float x=(physical-origin)/Math.max(.1f,scale);
        return rtl?WIDTH-x:x;
    }
    static float physicalX(float logical,float origin,float scale,boolean rtl){
        return origin+(rtl?WIDTH-logical:logical)*Math.max(.01f,scale);
    }
    static Paint.Align opposite(Paint.Align align){
        return align==Paint.Align.LEFT?Paint.Align.RIGHT:
            align==Paint.Align.RIGHT?Paint.Align.LEFT:Paint.Align.CENTER;
    }
    static int swipeDirection(float dx,float dy,boolean rtl){
        // The puzzle grid is intentionally not mirrored. Screen-space swipes
        // always move tiles toward the corresponding physical screen edge.
        if(rtl)dx=-dx;
        return Math.abs(dx)>Math.abs(dy)?
            (dx<0?TileGame.LEFT:TileGame.RIGHT):
            (dy<0?TileGame.UP:TileGame.DOWN);
    }
}
