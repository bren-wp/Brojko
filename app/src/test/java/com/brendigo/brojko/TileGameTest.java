package com.brendigo.brojko;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;

/** Regression tests for the rules, merges, undo and seeded challenges. */
public class TileGameTest {
    private TileGame empty(int n) {
        TileGame game=new TileGame(n==5?"relaxed":"classic","",42L);
        Arrays.fill(game.cells,0);
        return game;
    }
    @Test public void leftMergesExactlyOnce() {
        TileGame g=empty(4);
        g.cells[0]=2;g.cells[1]=2;g.cells[2]=2;g.cells[3]=2;
        TileGame.Result r=g.move(TileGame.LEFT);
        assertTrue(r.changed);assertEquals(8,r.points);assertEquals(2,r.merges);
        assertEquals(4,g.cells[0]);assertEquals(4,g.cells[1]);assertEquals(1,g.moves);
    }
    @Test public void movingWithoutChangeCannotSpawn() {
        TileGame g=empty(4);g.cells[0]=2;
        int[] before=g.cells.clone();
        TileGame.Result r=g.move(TileGame.LEFT);
        assertFalse(r.changed);assertArrayEquals(before,g.cells);assertEquals(0,g.moves);
    }
    @Test public void reverseAndUndoRestoresAllState() {
        TileGame g=empty(4);g.cells[0]=2;g.cells[1]=2;
        int[] before=g.cells.clone();
        assertTrue(g.move(TileGame.RIGHT).changed);
        assertEquals(4,g.cells[3]);assertEquals(4,g.score);
        assertEquals(4,g.undo());assertArrayEquals(before,g.cells);
        assertEquals(0,g.score);assertEquals(0,g.moves);assertFalse(g.canUndo());
    }
    @Test public void verticalMergesDoNotDoubleMerge() {
        TileGame g=empty(4);g.cells[0]=4;g.cells[4]=4;g.cells[8]=8;
        assertTrue(g.move(TileGame.UP).changed);assertEquals(8,g.score);
        assertEquals(8,g.cells[0]);assertEquals(8,g.cells[4]);
    }
    @Test public void fullBoardWithoutMatchesLoses() {
        TileGame g=empty(4);
        int[] blocked={2,4,2,4,4,2,4,2,2,4,2,4,4,2,4,2};
        g.cells=blocked;assertFalse(g.canMove());assertFalse(g.move(TileGame.LEFT).changed);
    }
    @Test public void sameSeedReproducesChallengeSequence() {
        TileGame a=new TileGame("daily","2026-10-24",123456L);
        TileGame b=new TileGame("daily","2026-10-24",123456L);
        assertArrayEquals(a.cells,b.cells);
        for(int dir:new int[]{TileGame.UP,TileGame.LEFT,TileGame.DOWN,TileGame.RIGHT}){
            a.move(dir);b.move(dir);assertArrayEquals(a.cells,b.cells);
        }
    }
    @Test public void fiveByFiveMode() {
        TileGame g=new TileGame("relaxed","",41L);
        assertEquals(5,g.size);assertEquals(25,g.cells.length);
    }
    @Test public void first2048OnlyTriggersOnce() {
        TileGame g=empty(4);g.cells[0]=1024;g.cells[1]=1024;
        TileGame.Result result=g.move(TileGame.LEFT);
        assertTrue(result.first2048);
        assertTrue(g.reached2048);assertEquals(2048,g.score);
        assertFalse(g.move(TileGame.RIGHT).first2048);
    }
}
