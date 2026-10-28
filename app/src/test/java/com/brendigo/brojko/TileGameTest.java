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
        g.cells=blocked;assertFalse(g.canMove());
        TileGame.Result result=g.move(TileGame.LEFT);
        assertFalse(result.changed);assertTrue(result.lost);assertTrue(g.gameOver);
    }
    @Test public void pairOfPairsMustNotMergeTwice() {
        TileGame g=empty(4);
        g.cells[0]=4;g.cells[1]=4;g.cells[2]=8;g.cells[3]=8;
        TileGame.Result r=g.move(TileGame.LEFT);
        assertEquals(24,r.points);assertEquals(2,r.merges);
        assertEquals(8,g.cells[0]);assertEquals(16,g.cells[1]);
    }
    @Test public void oneInvalidSwipeCannotChangeBoard() {
        TileGame g=empty(4);
        g.cells[0]=2;
        int[] initial=g.cells.clone();
        assertFalse(g.move(99).changed);
        assertArrayEquals(initial,g.cells);
        assertEquals(0,g.score);assertEquals(0,g.moves);
    }
    @Test public void jsonRoundTripPreservesUndoAndRandomSequence(){
        TileGame a=new TileGame("daily","2026-10-24",1234L);
        a.move(TileGame.UP);a.move(TileGame.LEFT);
        TileGame b=TileGame.fromJson(a.toJson());
        assertNotNull(b);assertArrayEquals(a.cells,b.cells);
        assertEquals(a.score,b.score);assertEquals(a.moves,b.moves);
        assertEquals(a.canUndo(),b.canUndo());
        assertEquals(a.undo(),b.undo());
        assertArrayEquals(a.cells,b.cells);
        a.move(TileGame.RIGHT);b.move(TileGame.RIGHT);
        assertArrayEquals(a.cells,b.cells);
    }
    @Test public void corruptSessionMustNotLoad(){
        org.json.JSONObject corrupt=new org.json.JSONObject();
        try{
            corrupt.put("size",4);corrupt.put("mode","classic");corrupt.put("rng","123");
            corrupt.put("score",0);corrupt.put("cells",new org.json.JSONArray("[2,4]"));
        }catch(Exception ignored){}
        assertNull(TileGame.fromJson(corrupt));
    }
    @Test public void sameSeedReproducesChallengeSequence() {
        TileGame a=new TileGame("daily","2026-10-24",123456L);
        TileGame b=new TileGame("daily","2026-10-24",123456L);
        assertArrayEquals(a.cells,b.cells);
        for(int dir:new int[]{TileGame.UP,TileGame.LEFT,TileGame.DOWN,TileGame.RIGHT}){
            a.move(dir);b.move(dir);assertArrayEquals(a.cells,b.cells);
        }
    }
    @Test public void dailyChallengeUsesInclusiveEightyMoveLimit() {
        TileGame g=new TileGame("daily","2026-10-24",55L);
        g.peak=256;g.score=3000;g.moves=80;
        assertTrue(g.isDailyGoalComplete());
        assertFalse(g.isDailyOutOfMoves());
        g.score=2999;
        assertFalse(g.isDailyGoalComplete());
        assertTrue(g.isDailyOutOfMoves());
        g.moves=79;
        assertFalse(g.isDailyOutOfMoves());
        g.moves=81;g.score=3000;
        assertFalse(g.isDailyGoalComplete());
        assertTrue(g.isDailyOutOfMoves());
    }
    @Test public void classicModeNeverUsesDailyMoveLimit() {
        TileGame g=new TileGame("classic","",55L);
        g.peak=256;g.score=3000;g.moves=800;
        assertFalse(g.isDailyGoalComplete());
        assertFalse(g.isDailyOutOfMoves());
    }
    @Test public void rejectWrongModeAndNegativeCounters() throws Exception {
        TileGame original=new TileGame("classic","",55L);
        org.json.JSONObject doc=original.toJson();
        doc.put("mode","relaxed");
        assertNull(TileGame.fromJson(doc));
        doc.put("mode","classic");doc.put("score",-1);
        assertNull(TileGame.fromJson(doc));
        doc.put("score",0);doc.put("moves",-20);
        assertNull(TileGame.fromJson(doc));
    }
    @Test public void rejectCorruptedUndoAndInvalidTile() throws Exception {
        TileGame g=new TileGame("classic","",79L);
        Arrays.fill(g.cells,0);
        g.cells[4]=2;
        assertTrue(g.move(TileGame.UP).changed);
        org.json.JSONObject valid=g.toJson();
        org.json.JSONObject undo=valid.optJSONObject("undo");
        assertNotNull(undo);
        undo.put("rng","0");
        assertNull(TileGame.fromJson(valid));
        org.json.JSONObject badTile=g.toJson();
        badTile.getJSONArray("cells").put(0,3);
        assertNull(TileGame.fromJson(badTile));
    }
    @Test public void deterministicStressAcrossModesNeverCorruptsGameOrUndo() {
        // More than 4,000 reproducible real merges/turns across 4×4, 5×5,
        // and daily modes. Catches accidental loss of deterministic state.
        for(String mode:new String[]{"classic","relaxed","daily"}){
            for(long seed=1;seed<=16;seed++){
                TileGame game=new TileGame(mode,
                    "daily".equals(mode)?"2026-10-28":"",seed*72319L);
                for(int turn=0;turn<220&&!game.gameOver;turn++){
                    int[] before=game.cells.clone();
                    int score=game.score,moves=game.moves;
                    TileGame.Result result=game.move((turn*7+(int)seed)%4);
                    assertTrue("Score cannot decrease",game.score>=score);
                    assertEquals("Valid moves increment exactly once",
                        moves+(result.changed?1:0),game.moves);
                    assertEquals("Board keeps the configured size",
                        game.size*game.size,game.cells.length);
                    for(int tile:game.cells)
                        assertTrue("Tile values must be powers of two or zero",
                            tile==0 || (tile>=2 && (tile&(tile-1))==0));
                    if(!result.changed)
                        assertArrayEquals("Blocked move cannot change tiles",
                            before,game.cells);
                    if(turn%17==0){
                        TileGame clone=TileGame.fromJson(game.toJson());
                        assertNotNull("A saved active game must load",clone);
                        assertArrayEquals(game.cells,clone.cells);
                        assertEquals(game.score,clone.score);
                        assertEquals(game.moves,clone.moves);
                        assertEquals(game.canUndo(),clone.canUndo());
                    }
                }
            }
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
