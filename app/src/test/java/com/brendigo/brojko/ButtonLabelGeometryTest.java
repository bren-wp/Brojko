package com.brendigo.brojko;
import org.junit.Test;
import static org.junit.Assert.*;
public final class ButtonLabelGeometryTest {
    @Test public void longLabelsNeverConsumeIconOrOuterPadding(){
        for(float width:new float[]{165,284,314,342,352}){
            float x=25f,iconRight=x+70.5f;
            ButtonLabelGeometry g=ButtonLabelGeometry.calculate(x,width,iconRight);
            assertTrue(g.width>0f);
            assertEquals(iconRight+8f,g.center-g.width/2f,.001f);
            assertEquals(x+width-12f,g.center+g.width/2f,.001f);
        }
    }
    @Test public void insufficientSpaceDoesNotProduceNegativeWidth(){
        ButtonLabelGeometry g=ButtonLabelGeometry.calculate(0,30,29);
        assertEquals(0f,g.width,0);
    }
}
