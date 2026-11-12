package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real SharedPreferences regression for atomic, local game archiving. */
@RunWith(AndroidJUnit4.class)
public class LocalHistoryRepositoryTest {
    private SharedPreferences isolated(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        return context.getSharedPreferences("brojko-history-qa",Context.MODE_PRIVATE);
    }

    @Test public void archivePersistsLatestGameAndKeepsLastNineteen() throws Exception {
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().commit());
        try{
            JSONArray previous=new JSONArray();
            for(int i=0;i<23;i++)
                previous.put(new JSONObject().put("score",100+i).put("moves",i+1));
            assertTrue(prefs.edit().putString("history",previous.toString()).commit());

            TileGame game=new TileGame("classic","",9955L);
            game.moves=3;
            game.score=64;
            assertFalse(game.archived);
            assertTrue(LocalHistoryRepository.archive(prefs,game,"12. stu 2026.","20:00"));
            assertTrue("Game must be marked archived only after disk commit",game.archived);
            JSONArray result=new JSONArray(prefs.getString("history","[]"));
            assertEquals(20,result.length());
            assertEquals(64,result.getJSONObject(0).getInt("score"));
            assertEquals("classic",result.getJSONObject(0).getString("mode"));
            assertEquals("12. stu 2026.",result.getJSONObject(0).getString("day"));
            assertEquals(100,result.getJSONObject(1).getInt("score"));
            assertEquals(118,result.getJSONObject(19).getInt("score"));
            assertTrue(LocalHistoryRepository.archive(prefs,game,"12. stu 2026.","20:01"));
            assertEquals("Archived games must not duplicate rows",
                result.toString(),prefs.getString("history","[]"));
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void malformedLegacyHistoryStillPreservesNewGame() throws Exception {
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().putString("history","{bad json").commit());
        try{
            TileGame game=new TileGame("relaxed","",7721L);
            game.moves=5;
            game.score=32;
            assertTrue(LocalHistoryRepository.archive(prefs,game,"12. stu","20:02"));
            JSONArray result=new JSONArray(prefs.getString("history","[]"));
            assertEquals(1,result.length());
            assertEquals(32,result.getJSONObject(0).getInt("score"));
            assertTrue(game.archived);
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void emptyOrUnplayedSessionNeverMutatesHistory(){
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().putString("history","[]").commit());
        try{
            assertTrue(LocalHistoryRepository.archive(prefs,null,"today","now"));
            TileGame game=new TileGame("classic","",91L);
            assertEquals(0,game.moves);
            assertTrue(LocalHistoryRepository.archive(prefs,game,"today","now"));
            assertEquals("[]",prefs.getString("history",null));
            assertFalse(game.archived);
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void nonObjectLegacyRowsAreSkippedNotCopied() throws Exception {
        JSONObject newest=new JSONObject().put("score",10);
        JSONArray previous=new JSONArray()
            .put(20).put(JSONObject.NULL).put(new JSONObject().put("score",5));
        JSONArray after=LocalHistoryRepository.prepend(newest,previous);
        assertEquals(2,after.length());
        assertEquals(10,after.getJSONObject(0).getInt("score"));
        assertEquals(5,after.getJSONObject(1).getInt("score"));
    }
}
