package com.brendigo.brojko;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * An independently-written board oracle checks every swipe direction on
 * randomized 4x4/5x5 boards. It models pre-spawn 2048 rules; the real engine
 * must add exactly one deterministic 2-or-4 tile afterwards.
 */
public final class TileGameMovementInvariantTest {
    private static final class Expected {
        final int[] cells;
        final long points;
        final int merges;
        Expected(int[] cells,long points,int merges){
            this.cells=cells;this.points=points;this.merges=merges;
        }
    }

    private static Expected reference(int[] board,int width,int direction){
        int[] result=board.clone();
        long points=0;
        int mergeCount=0;
        for(int line=0;line<width;line++){
            int[] order=new int[width];
            ArrayList<Integer> compact=new ArrayList<>();
            for(int step=0;step<width;step++){
                int index;
                switch(direction){
                    case TileGame.LEFT:index=line*width+step;break;
                    case TileGame.RIGHT:index=line*width+(width-1-step);break;
                    case TileGame.UP:index=step*width+line;break;
                    default:index=(width-1-step)*width+line;break;
                }
                order[step]=index;
                if(board[index]!=0)compact.add(board[index]);
                result[index]=0;
            }
            int destination=0;
            for(int source=0;source<compact.size();source++){
                int value=compact.get(source);
                if(source+1<compact.size() &&
                   value==compact.get(source+1) && value<(1<<30)){
                    value*=2;
                    points+=value;
                    mergeCount++;
                    source++;
                }
                result[order[destination++]]=value;
            }
        }
        return new Expected(result,points,mergeCount);
    }

    @Test public void allDirectionsPreserve2048BoardRulesAndUndoSeed() {
        Random rng=new Random(20261113L);
        int[] values={0,0,0,2,2,4,8,16,32,64,128,256};
        for(String mode:new String[]{"classic","relaxed"}){
            int width="relaxed".equals(mode)?5:4;
            for(int sample=0;sample<120;sample++){
                int[] input=new int[width*width];
                for(int i=0;i<input.length;i++)
                    input[i]=values[rng.nextInt(values.length)];
                for(int direction=TileGame.LEFT;direction<=TileGame.DOWN;
                        direction++){
                    TileGame g=new TileGame(mode,"",8125L+sample);
                    g.cells=input.clone();
                    g.peak=g.maxTile();
                    g.score=0;g.moves=0;g.merges=0;
                    g.gameOver=false;
                    Expected wanted=reference(input,width,direction);
                    boolean changed=!Arrays.equals(input,wanted.cells);
                    TileGame.Result result=g.move(direction);
                    String context=mode+"/"+sample+"/"+direction;
                    assertEquals("Changed "+context,changed,result.changed);
                    assertEquals("Points "+context,wanted.points,result.points);
                    assertEquals("Merges "+context,wanted.merges,result.merges);
                    assertEquals("Score "+context,wanted.points,g.score);
                    assertEquals("Move count "+context,changed?1:0,g.moves);
                    assertEquals("Total merges "+context,
                        wanted.merges,g.merges);
                    if(changed){
                        int spawnCount=0;
                        for(int i=0;i<input.length;i++){
                            if(wanted.cells[i]!=g.cells[i]){
                                assertEquals("Spawn must occupy empty cell "+context,
                                    0,wanted.cells[i]);
                                assertTrue("Spawn must be 2 or 4 "+context,
                                    g.cells[i]==2 || g.cells[i]==4);
                                spawnCount++;
                            }
                        }
                        assertEquals("Exactly one spawn per legal swipe "+context,
                            1,spawnCount);
                        int[] after=g.cells.clone();
                        assertTrue("Undo available "+context,g.canUndo());
                        assertEquals("Undo score delta "+context,
                            wanted.points,g.undo());
                        assertArrayEquals("Undo restores original board "+context,
                            input,g.cells);
                        assertEquals("Undo restores move count "+context,0,g.moves);
                        assertEquals("Undo restores merges "+context,0,g.merges);
                        assertFalse("Undo only once "+context,g.canUndo());
                        TileGame.Result replay=g.move(direction);
                        assertEquals("Replay points "+context,
                            wanted.points,replay.points);
                        assertArrayEquals("Replay reproduces seeded spawn "+context,
                            after,g.cells);
                    }else{
                        assertArrayEquals("Blocked direction does not spawn "+context,
                            input,g.cells);
                        assertFalse("No undo snapshot for no-op "+context,g.canUndo());
                    }
                }
            }
        }
    }

    @Test public void blockedFullBoardEndsGameButDoesNotAddMoveOrSpawn() {
        TileGame g=new TileGame("classic","",911L);
        g.cells=new int[]{2,4,2,4,4,2,4,2,2,4,2,4,4,2,4,2};
        g.peak=4;
        int[] before=g.cells.clone();
        TileGame.Result result=g.move(TileGame.LEFT);
        assertFalse(result.changed);
        assertTrue(result.lost);
        assertTrue(g.gameOver);
        assertEquals(0,g.moves);
        assertArrayEquals(before,g.cells);
    }
}
