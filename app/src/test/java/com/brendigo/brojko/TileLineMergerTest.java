package com.brendigo.brojko;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/** Pure, deterministic line-merge contracts shared by 4x4 and 5x5 modes. */
public final class TileLineMergerTest {
    private void expect(int[] values,int occupied,int width,int[] expected,
            long points,int merges){
        int[] input=values.clone();
        TileLineMerger.Packed result=TileLineMerger.pack(values,occupied,width);
        assertArrayEquals("All tiles must be packed in traversal order",
            expected,result.cells);
        assertEquals(points,result.points);
        assertEquals(merges,result.merges);
        assertArrayEquals("Merge must not mutate its input",input,values);
    }

    @Test public void pairsMergeOnlyOnceAndNeverChainWithinOneSwipe(){
        expect(new int[]{2,2,2,2},4,4,new int[]{4,4,0,0},8,2);
        expect(new int[]{2,2,4,4},4,4,new int[]{4,8,0,0},12,2);
        expect(new int[]{4,4,8,0},3,4,new int[]{8,8,0,0},8,1);
        expect(new int[]{2,4,2,4},4,4,new int[]{2,4,2,4},0,0);
        expect(new int[]{8,8,8,8,8},5,5,new int[]{16,16,8,0,0},32,2);
    }

    @Test public void trailingEmptyPositionsAndSingleTileRemainStable(){
        expect(new int[]{2,0,0,0},1,4,new int[]{2,0,0,0},0,0);
        expect(new int[]{0,0,0,0,0},0,5,new int[]{0,0,0,0,0},0,0);
        expect(new int[]{4,8,16,32,64},5,5,
            new int[]{4,8,16,32,64},0,0);
    }

    @Test public void maxValueNeverOverflowsOrMergesPastSupportedTile(){
        int cap=1073741824;
        expect(new int[]{cap,cap,0,0},2,4,
            new int[]{cap,cap,0,0},0,0);
        expect(new int[]{cap/2,cap/2,cap/2,cap/2},4,4,
            new int[]{cap,cap,0,0},(long)cap*2,2);
    }

    @Test public void invalidOccupiedOrWidthNeverReadsPastInput(){
        for(int occupied:new int[]{-1,3,4}){
            try{TileLineMerger.pack(new int[]{2,2},occupied,3);
                fail("Expected rejected occupied="+occupied);
            }catch(IllegalArgumentException expected){}
        }
        try{TileLineMerger.pack(new int[]{2},0,0);
            fail("Zero-width line must be rejected");
        }catch(IllegalArgumentException expected){}
        try{TileLineMerger.pack(null,0,4);
            fail("Null line must be rejected");
        }catch(IllegalArgumentException expected){}
    }

    @Test public void exhaustiveFourTileLinesKeepIndependent2048ReferenceRules(){
        // Every length-4 line over {0,2,4,8}, 256 possible combinations.
        int[] alphabet={0,2,4,8};
        for(int code=0;code<256;code++){
            int n=code;
            int[] input=new int[4],compacted=new int[4];
            int count=0;
            for(int i=0;i<4;i++){
                input[i]=alphabet[n&3];n>>>=2;
                if(input[i]!=0)compacted[count++]=input[i];
            }
            int[] expected=new int[4];
            int k=0;long gain=0;int merges=0;
            // Independent reference: pop the first surviving tile; merge
            // only with the next surviving tile, then advance two.
            for(int i=0;i<count;){
                int value=compacted[i++];
                if(i<count && compacted[i]==value){
                    value+=compacted[i++];
                    gain+=value;merges++;
                }
                expected[k++]=value;
            }
            TileLineMerger.Packed packed=TileLineMerger.pack(compacted,count,4);
            assertArrayEquals("line="+Arrays.toString(input),
                expected,packed.cells);
            assertEquals("line="+Arrays.toString(input),gain,packed.points);
            assertEquals("line="+Arrays.toString(input),merges,packed.merges);
        }
    }
}
