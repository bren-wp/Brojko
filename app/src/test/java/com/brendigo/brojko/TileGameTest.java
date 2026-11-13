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
    @Test public void rejectImpossibleUndoPeakOrMergeCounters() throws Exception {
        TileGame g=empty(4);
        g.cells[0]=2;g.cells[1]=2;
        assertTrue(g.move(TileGame.LEFT).changed);
        assertEquals(1,g.merges);
        assertEquals(1,g.moves);
        org.json.JSONObject hugePeak=g.toJson();
        hugePeak.getJSONObject("undo").put("peak",1073741824);
        assertNull("Undo cannot have a greater peak than the active game",
            TileGame.fromJson(hugePeak));
        org.json.JSONObject impossibleMerges=g.toJson();
        impossibleMerges.getJSONObject("undo").put("merges",1);
        assertNull("One undo snapshot with zero moves cannot contain merges",
            TileGame.fromJson(impossibleMerges));
    }
    @Test public void engineMoveAndMergeCountersStayNonNegativeAtLimit() {
        TileGame g=empty(4);
        g.cells[0]=2;g.cells[1]=2;
        g.moves=Integer.MAX_VALUE;
        g.merges=Integer.MAX_VALUE;
        assertTrue(g.move(TileGame.LEFT).changed);
        assertEquals(Integer.MAX_VALUE,g.moves);
        assertEquals(Integer.MAX_VALUE,g.merges);
    }
    @Test public void countersSaturateInsteadOfOverflowing() {
        assertEquals(Integer.MAX_VALUE,TileGame.saturatedAdd(Integer.MAX_VALUE-2,50));
        assertEquals(Integer.MAX_VALUE,TileGame.saturatedAdd(Integer.MAX_VALUE,250));
        assertEquals(400,TileGame.saturatedAdd(150,250));
        assertEquals(0,TileGame.saturatedAdd(-5,-1));
    }
    @Test public void rejectMalformedUndoRatherThanSilentlyDiscardingIt() throws Exception {
        TileGame g=empty(4);
        g.cells[0]=2;g.cells[1]=2;
        assertTrue(g.move(TileGame.LEFT).changed);
        org.json.JSONObject malformedBoard=g.toJson();
        malformedBoard.getJSONObject("undo").put("cells",new org.json.JSONArray("[2]"));
        assertNull("Truncated undo board must invalidate the save",
            TileGame.fromJson(malformedBoard));
        org.json.JSONObject malformedType=g.toJson();
        malformedType.put("undo","corrupt");
        assertNull("Undo must be a JSON object",TileGame.fromJson(malformedType));
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
    @Test public void malformedNumbersCannotSilentlyAlterRestoredProgress()
            throws Exception {
        TileGame base=new TileGame("classic","",41L);
        for(String field:new String[]{"size","score","moves","merges","peak"}){
            org.json.JSONObject decimal=base.toJson();
            decimal.put(field,3.5d);
            assertNull("Fractional "+field+" must be rejected",
                TileGame.fromJson(decimal));
            org.json.JSONObject string=base.toJson();
            string.put(field,"4");
            assertNull("Text "+field+" must be rejected",
                TileGame.fromJson(string));
        }
        org.json.JSONObject peak=base.toJson();
        peak.put("peak",3);
        assertNull("Peak cannot be a non-power-of-two tile",TileGame.fromJson(peak));
        org.json.JSONObject overflow=base.toJson();
        overflow.put("score",((long)Integer.MAX_VALUE)+1L);
        assertNull("Overflow cannot wrap into a score",TileGame.fromJson(overflow));
    }
    @Test public void malformedBoardAndFlagsCannotCorruptSavedGame()
            throws Exception{
        TileGame base=new TileGame("classic","",77L);
        org.json.JSONObject decimalTile=base.toJson();
        decimalTile.getJSONArray("cells").put(0,2.5d);
        assertNull(TileGame.fromJson(decimalTile));
        org.json.JSONObject textTile=base.toJson();
        textTile.getJSONArray("cells").put(0,"2");
        assertNull(TileGame.fromJson(textTile));
        org.json.JSONObject rng=base.toJson();
        rng.put("rng",12345L);
        assertNull("RNG requires its exact serialized string format",
            TileGame.fromJson(rng));
        org.json.JSONObject badMode=base.toJson();
        badMode.put("mode",123);
        assertNull(TileGame.fromJson(badMode));
        for(String flag:new String[]{"reached","gameOver","archived","ack"}){
            org.json.JSONObject badFlag=base.toJson();
            badFlag.put(flag,"false");
            assertNull("Boolean value must not be coerced: "+flag,
                TileGame.fromJson(badFlag));
        }
    }
    @Test public void invalidUndoTypesAndImpossiblePeaksAreRejected()
            throws Exception{
        TileGame base=empty(4);
        base.cells[0]=2;base.cells[1]=2;
        assertTrue(base.move(TileGame.LEFT).changed);
        org.json.JSONObject fractional=base.toJson();
        fractional.getJSONObject("undo").put("moves",0.5d);
        assertNull(TileGame.fromJson(fractional));
        org.json.JSONObject text=base.toJson();
        text.getJSONObject("undo").getJSONArray("cells").put(0,"2");
        assertNull(TileGame.fromJson(text));
        org.json.JSONObject falseString=base.toJson();
        falseString.getJSONObject("undo").put("reached","false");
        assertNull(TileGame.fromJson(falseString));
        org.json.JSONObject peak=base.toJson();
        peak.getJSONObject("undo").put("peak",3);
        assertNull(TileGame.fromJson(peak));
    }
    @Test public void validIntegerSaveStillRoundTripsWithLegacyMissingFields()
            throws Exception{
        TileGame game=new TileGame("daily","2026-11-12",42L);
        org.json.JSONObject legacy=game.toJson();
        legacy.remove("day");legacy.remove("merges");legacy.remove("moves");
        legacy.remove("reached");legacy.remove("ack");legacy.remove("archived");
        TileGame restored=TileGame.fromJson(legacy);
        assertNotNull("Missing old optional fields remain backward compatible",restored);
        assertArrayEquals(game.cells,restored.cells);
        assertEquals(game.score,restored.score);
        assertEquals(game.peak,restored.peak);
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

    @Test public void expiredDailyGameCannotMoveEvenIfRestoredAsPlayable(){
        TileGame game=new TileGame("daily","2026-11-13",1234L);
        Arrays.fill(game.cells,0);
        game.cells[0]=2;game.cells[1]=2;
        game.moves=80;game.score=0;game.peak=2;
        int[] before=game.cells.clone();
        TileGame.Result blocked=game.move(TileGame.RIGHT);
        assertFalse("An expired challenge cannot merge or spawn",blocked.changed);
        assertTrue("Invalid saved playable state must become terminal",blocked.lost);
        assertTrue(game.gameOver);
        assertArrayEquals(before,game.cells);
        assertEquals(80,game.moves);
        assertEquals(0,game.score);
    }

    @Test public void finalLegalDailyMoveStillWorksButEightyFirstCannot(){
        TileGame game=new TileGame("daily","2026-11-13",1234L);
        Arrays.fill(game.cells,0);
        game.cells[0]=2;game.cells[1]=2;
        game.moves=79;game.peak=2;
        TileGame.Result finalMove=game.move(TileGame.RIGHT);
        assertTrue("80th move must still be playable",finalMove.changed);
        assertEquals(80,game.moves);
        assertTrue(game.isDailyOutOfMoves());
        int[] after=game.cells.clone();
        int beforeScore=game.score;
        TileGame.Result extra=game.move(TileGame.LEFT);
        assertFalse("81st move cannot change the board",extra.changed);
        assertTrue(extra.lost);
        assertTrue(game.gameOver);
        assertArrayEquals(after,game.cells);
        assertEquals(beforeScore,game.score);
        assertEquals(80,game.moves);
    }

}
