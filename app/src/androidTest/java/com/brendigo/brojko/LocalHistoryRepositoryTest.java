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

/** Actual worker archive regression using isolated SharedPreferences. */
@RunWith(AndroidJUnit4.class)
public final class LocalHistoryRepositoryTest {
    private SharedPreferences isolated(){
        Context ctx=InstrumentationRegistry.getInstrumentation().getTargetContext();
        return ctx.getSharedPreferences("brojko-history-qa",Context.MODE_PRIVATE);
    }

    @Test public void archivePersistsLatestGameAndKeepsLastNineteen() throws Exception{
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().commit());
        try{
            JSONArray previous=new JSONArray();
            for(int i=0;i<23;i++)
                previous.put(new JSONObject().put("score",100+i).put("moves",i+1));
            TileGame game=new TileGame("classic","",9955L);
            game.moves=3;game.score=64;
            String snapshot=game.toJson().toString();
            assertTrue(prefs.edit().putString("session",snapshot)
                .putString("history",previous.toString()).commit());
            assertTrue(LocalHistoryRepository.archiveSnapshot(prefs,snapshot,
                "12. stu 2026.","20:00"));
            assertFalse("Worker never changes the live game",game.archived);
            assertTrue(TileGame.fromJson(new JSONObject(
                prefs.getString("session",""))).archived);
            JSONArray result=new JSONArray(prefs.getString("history","[]"));
            assertEquals(20,result.length());
            assertEquals(64,result.getJSONObject(0).getInt("score"));
            assertEquals("classic",result.getJSONObject(0).getString("mode"));
            assertEquals("12. stu 2026.",result.getJSONObject(0).getString("day"));
            assertEquals(100,result.getJSONObject(1).getInt("score"));
            assertEquals(118,result.getJSONObject(19).getInt("score"));
            assertTrue(LocalHistoryRepository.archiveSnapshot(prefs,snapshot,
                "12. stu 2026.","20:01"));
            assertEquals("Retry must not duplicate a row",result.toString(),
                prefs.getString("history","[]"));
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void malformedLegacyHistoryStillPreservesNewGame() throws Exception{
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().commit());
        try{
            TileGame game=new TileGame("relaxed","",7721L);
            game.moves=5;game.score=32;
            String session=game.toJson().toString();
            assertTrue(prefs.edit().putString("session",session)
                .putString("history","{bad json").commit());
            assertTrue(LocalHistoryRepository.archiveSnapshot(prefs,session,
                "12. stu","20:02"));
            JSONArray result=new JSONArray(prefs.getString("history","[]"));
            assertEquals(1,result.length());
            assertEquals(32,result.getJSONObject(0).getInt("score"));
            assertTrue(LocalHistoryRepository.hasCommittedArchive(prefs));
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void emptyOrUnplayedSessionNeverMutatesHistory(){
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().putString("history","[]").commit());
        try{
            assertFalse(LocalHistoryRepository.archiveSnapshot(prefs,
                null,"today","now"));
            TileGame game=new TileGame("classic","",91L);
            assertEquals(0,game.moves);
            String session=game.toJson().toString();
            assertTrue(prefs.edit().putString("session",session).commit());
            assertFalse(LocalHistoryRepository.archiveSnapshot(prefs,
                session,"today","now"));
            assertEquals("[]",prefs.getString("history",null));
            assertEquals(session,prefs.getString("session",null));
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void nonObjectLegacyRowsAreSkippedNotCopied() throws Exception{
        JSONObject newest=new JSONObject().put("score",10);
        JSONArray previous=new JSONArray()
            .put(20).put(JSONObject.NULL).put(new JSONObject().put("score",5));
        JSONArray after=LocalHistoryRepository.prepend(newest,previous);
        assertEquals(2,after.length());
        assertEquals(10,after.getJSONObject(0).getInt("score"));
        assertEquals(5,after.getJSONObject(1).getInt("score"));
    }
}
