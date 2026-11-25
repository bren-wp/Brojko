package com.brendigo.brojko;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RewardPolicyTest {
    private static int[] earned(int merges,int peak,boolean undo,int streak,
                                int tile,int best,int week) {
        int[] result = {0,0,0};
        RewardPolicy.evaluate(merges,peak,undo,streak,tile,best,week,
            (bit,amount,reached)->{
                result[2]++;
                if(reached){result[0] |= bit;result[1] += amount;}
            });
        return result;
    }

    @Test public void exactThresholdsPreserveFlagsAndAmounts(){
        int[] earned=earned(20,512,false,7,1024,50000,10000);
        assertEquals(63,earned[0]);
        assertEquals(2000,earned[1]);
        assertEquals(6,earned[2]);
    }

    @Test public void belowThresholdsDoNotGrant(){
        int[] earned=earned(19,511,false,6,1023,49999,9999);
        assertEquals(0,earned[0]);
        assertEquals(0,earned[1]);
    }

    @Test public void undoSuppressesOnlyNoUndoMission(){
        int[] earned=earned(20,512,true,7,1024,50000,10000);
        assertEquals(61,earned[0]);
        assertEquals(1900,earned[1]);
    }

    @Test public void missingGameDoesNotGrantPeakMission(){
        assertEquals(0,earned(0,0,false,0,0,0,0)[0]);
    }
}
