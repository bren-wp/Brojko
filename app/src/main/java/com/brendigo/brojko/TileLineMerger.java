package com.brendigo.brojko;

/**
 * Pure 2048 row/column merge. No Android, RNG, SharedPreferences, UI or
 * direction-dependent state. TileGame selects the traversal order.
 *
 * A line is already compacted into values[0..occupied); zero-filled trailing
 * positions are produced here. The input array is never modified.
 */
final class TileLineMerger {
    private static final int MAX_MERGE_INPUT=1<<30;

    private TileLineMerger(){}

    static final class Packed {
        final int[] cells;
        final long points;
        final int merges;

        Packed(int[] cells,long points,int merges){
            this.cells=cells;
            this.points=points;
            this.merges=merges;
        }
    }

    static Packed pack(int[] values,int occupied,int width){
        if(values==null || width<=0 || occupied<0 ||
           occupied>width || occupied>values.length)
            throw new IllegalArgumentException("Invalid compacted tile line");
        int[] output=new int[width];
        int destination=0;
        int mergedCount=0;
        long earned=0L;
        for(int i=0;i<occupied;i++){
            int value=values[i];
            if(i+1<occupied && value==values[i+1] &&
               value<MAX_MERGE_INPUT){
                int doubled=value*2;  // <= 1 << 30, always fits int
                output[destination++]=doubled;
                earned+=doubled;
                mergedCount++;
                i++;                   // no tile may merge twice per swipe
            }else{
                output[destination++]=value;
            }
        }
        return new Packed(output,earned,mergedCount);
    }
}
