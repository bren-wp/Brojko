package com.brendigo.brojko;

import android.graphics.Paint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class CanvasDirectionTest {
    @Test public void rtlMappingPreservesTouchAndAccessibilityCoordinates(){
        for(boolean rtl:new boolean[]{false,true}){
            for(float x:new float[]{0f,28f,200f,350f,400f}){
                float physical=CanvasDirection.physicalX(x,12f,2.3f,rtl);
                assertEquals(x,CanvasDirection.logicalX(physical,12f,2.3f,rtl),.001f);
            }
        }
        assertEquals(362f,CanvasDirection.logicalX(38f,0f,1f,true),.001f);
        assertEquals(Paint.Align.RIGHT,CanvasDirection.opposite(Paint.Align.LEFT));
        assertEquals(Paint.Align.CENTER,CanvasDirection.opposite(Paint.Align.CENTER));
    }

    @Test public void physicalSwipesRemainCorrectOnUnmirroredPuzzle(){
        assertEquals(TileGame.LEFT,CanvasDirection.swipeDirection(-40f,1f,false));
        assertEquals(TileGame.LEFT,CanvasDirection.swipeDirection(40f,1f,true));
        assertEquals(TileGame.RIGHT,CanvasDirection.swipeDirection(-40f,1f,true));
        assertEquals(TileGame.UP,CanvasDirection.swipeDirection(1f,-40f,true));
        assertEquals(TileGame.DOWN,CanvasDirection.swipeDirection(1f,40f,true));
    }
}
