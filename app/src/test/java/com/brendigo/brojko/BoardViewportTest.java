package com.brendigo.brojko;
import org.junit.Test;
import static org.junit.Assert.*;

public final class BoardViewportTest {
    private BoardViewport at(String page,String mode,int size,float w,float h){
        return BoardViewport.calculate(page,mode,size,w,h,0,0,1f,false,0,0,false);
    }
    @Test public void ownerPortraitGeometryStaysStable(){
        BoardViewport v=at("game","classic",4,941,1672);
        assertTrue(v.referencePortraitLayout);assertTrue(v.compactBoardLayout);
        assertEquals(941f/400f,v.scale,.0001f);assertEquals(159f,v.gridY,0);
        assertEquals(0f,v.offX,.001f);assertEquals(0f,v.offY,.001f);
        assertEquals(0f,v.maxScroll,.001f);
    }
    @Test public void compactDetailsFitReferenceAndShortViewportsCanScroll(){
        for(String page:new String[]{"language","backup","help","privacy","about","historyDetail"}){
            assertEquals(page,0f,at(page,"",0,941,1672).maxScroll,.001f);
            assertTrue(page,at(page,"",0,320,480).maxScroll>0);
        }
    }
    @Test public void pageChangeClearsPreviousScroll(){
        BoardViewport v=BoardViewport.calculate("stats","",0,320,480,0,0,1f,true,5,100,true);
        assertEquals(0f,v.scrollOffset,0);
        assertTrue(v.maxScroll>0);
    }
}
