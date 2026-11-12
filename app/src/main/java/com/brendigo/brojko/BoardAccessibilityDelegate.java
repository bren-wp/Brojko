package com.brendigo.brojko;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import java.util.List;

/**
 * Virtual accessibility children for functional Canvas controls.
 * There is no visual rendering and no modification of game state here.
 * Buttons retain their touch geometry and the exact existing actions.
 */
final class BoardAccessibilityDelegate extends ExploreByTouchHelper {
    private static final int MAX_CONTROLS=200;
    private static final int CONTROL_ID_OFFSET=1;
    private final MainActivity.BoardScreen view;

    BoardAccessibilityDelegate(MainActivity.BoardScreen host){
        super(host);
        view=host;
    }

    private Rect boundsFor(MainActivity.BoardScreen.Tap tap){
        float scale=Math.max(0.01f,view.scale);
        int left=Math.round(view.offX+tap.left*scale);
        int top=Math.round(view.offY+tap.top*scale);
        int right=Math.max(left+1,Math.round(view.offX+tap.right*scale));
        int bottom=Math.max(top+1,Math.round(view.offY+tap.bottom*scale));
        return new Rect(left,top,right,bottom);
    }

    private boolean visible(int index){
        if(index<0||index>=view.taps.size())return false;
        MainActivity.BoardScreen.Tap tap=view.taps.get(index);
        if(tap.action==null||tap.label==null||tap.label.trim().isEmpty())return false;
        Rect rect=boundsFor(tap);
        return Rect.intersects(rect,new Rect(0,0,view.getWidth(),view.getHeight()));
    }

    @Override protected int getVirtualViewAt(float x,float y){
        int count=Math.min(MAX_CONTROLS,view.taps.size());
        for(int i=0;i<count;i++){
            if(visible(i)&&boundsFor(view.taps.get(i)).contains((int)x,(int)y))
                return CONTROL_ID_OFFSET+i;
        }
        return ExploreByTouchHelper.INVALID_ID;
    }

    @Override protected void getVisibleVirtualViews(List<Integer> ids){
        int count=Math.min(MAX_CONTROLS,view.taps.size());
        for(int i=0;i<count;i++)if(visible(i))ids.add(CONTROL_ID_OFFSET+i);
    }

    @Override protected void onPopulateNodeForVirtualView(int virtualId,
            AccessibilityNodeInfoCompat node){
        int index=virtualId-CONTROL_ID_OFFSET;
        if(!visible(index))throw new IllegalArgumentException("Unknown Canvas control");
        MainActivity.BoardScreen.Tap tap=view.taps.get(index);
        node.setContentDescription(tap.label);
        node.setClassName("android.widget.Button");
        node.setFocusable(true);
        node.setClickable(true);
        node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
        setBoundsInScreenFromBoundsInParent(node,boundsFor(tap));
    }

    @Override protected boolean onPerformActionForVirtualView(int virtualId,
            int action,Bundle arguments){
        int index=virtualId-CONTROL_ID_OFFSET;
        if(!view.isEnabled()||action!=AccessibilityNodeInfo.ACTION_CLICK||!visible(index))return false;
        MainActivity.BoardScreen.Tap tap=view.taps.get(index);
        sendEventForVirtualView(virtualId,AccessibilityEvent.TYPE_VIEW_CLICKED);
        tap.action.run();
        view.invalidate();
        invalidateRoot();
        return true;
    }

    @Override protected void onPopulateNodeForHost(AccessibilityNodeInfoCompat node){
        super.onPopulateNodeForHost(node);
        node.setClassName("android.view.View");
    }
}
