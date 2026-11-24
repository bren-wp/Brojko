package com.brendigo.brojko;
/** Logical text interval after the icon, valid in both LTR and RTL. */
final class ButtonLabelGeometry {
    final float center,width;
    private ButtonLabelGeometry(float center,float width){this.center=center;this.width=width;}
    static ButtonLabelGeometry calculate(float x,float width,float iconRight){
        float right=x+width-12f;
        float left=Math.min(right,Math.max(x+12f,iconRight+8f));
        return new ButtonLabelGeometry((left+right)/2f,Math.max(0f,right-left));
    }
}
