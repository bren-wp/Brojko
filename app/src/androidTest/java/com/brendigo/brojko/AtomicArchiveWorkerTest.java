package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Durable worker transactions: one commit contains the archived session and
 * its history row; no in-memory TileGame mutation occurs on the worker.
 */
@RunWith(AndroidJUnit4.class)
public final class AtomicArchiveWorkerTest {
    private SharedPreferences isolated(){
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        return c.getSharedPreferences("brojko-archive-atomic-qa",Context.MODE_PRIVATE);
    }

    private static TileGame game(long seed,int score){
        TileGame g=new TileGame("classic","",seed);
        g.score=score;
        g.moves=3;
        return g;
    }

    @Test public void workerCommitsSessionAndHistoryBeforeTransition() throws Exception{
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().commit());
        ExecutorService worker=Executors.newSingleThreadExecutor();
        try{
            TileGame original=game(9911L,512);
            String snapshot=original.toJson().toString();
            assertTrue(prefs.edit().putString("session",snapshot)
                .putString("history","[]").commit());

            Future<Boolean> completed=worker.submit(()->LocalHistoryRepository
                .archiveSnapshot(prefs,snapshot,"12. stu 2026.","21:55"));
            assertTrue("Archive must reach durable storage",completed.get(20,TimeUnit.SECONDS));
            assertFalse("Worker may not mutate in-memory game",original.archived);

            JSONObject stored=new JSONObject(prefs.getString("session",""));
            assertTrue("Old session and history must be in one commit",
                stored.getBoolean("archived"));
            assertEquals(512,stored.getInt("score"));
            assertEquals("Last score must be durable in the same commit",
                512,prefs.getInt("lastScore",-1));
            assertTrue(LocalHistoryRepository.hasCommittedArchive(prefs));
            JSONArray history=new JSONArray(prefs.getString("history","[]"));
            assertEquals(1,history.length());
            assertEquals(512,history.getJSONObject(0).getInt("score"));
            assertEquals("classic",history.getJSONObject(0).getString("mode"));
            assertEquals("12. stu 2026.",history.getJSONObject(0).getString("day"));

            assertTrue("Replayed archive must be idempotent",
                LocalHistoryRepository.archiveSnapshot(prefs,snapshot,
                    "12. stu 2026.","22:00"));
            assertEquals("No duplicated row after a replay",
                history.toString(),prefs.getString("history","[]"));
        }finally{
            worker.shutdownNow();
            assertTrue(prefs.edit().clear().commit());
        }
    }

    @Test public void staleSessionCannotOverwriteAnUnrelatedSavedGame(){
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().commit());
        try{
            String current=game(11L,400).toJson().toString();
            String stale=game(22L,800).toJson().toString();
            assertTrue(prefs.edit().putString("session",current)
                .putString("history","[]").commit());
            assertFalse(LocalHistoryRepository.archiveSnapshot(prefs,stale,
                "12. stu","22:00"));
            assertEquals(current,prefs.getString("session",null));
            assertEquals("[]",prefs.getString("history",null));
            assertFalse(LocalHistoryRepository.hasCommittedArchive(prefs));
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void malformedSnapshotNeverChangesSavedProgress() throws Exception{
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().commit());
        try{
            String session=game(33L,4096).toJson().toString();
            assertTrue(prefs.edit().putString("session",session)
                .putString("history","[]").commit());
            JSONObject corrupt=new JSONObject(session);
            corrupt.put("score",-2);
            assertFalse(LocalHistoryRepository.archiveSnapshot(prefs,
                corrupt.toString(),"12. stu","22:00"));
            assertEquals(session,prefs.getString("session",null));
            assertEquals("[]",prefs.getString("history",null));
        }finally{assertTrue(prefs.edit().clear().commit());}
    }

    @Test public void failedDiskCommitDoesNotPermitNewSession(){
        SharedPreferences actual=isolated();
        assertTrue(actual.edit().clear().commit());
        try{
            String snapshot=game(44L,64).toJson().toString();
            assertTrue(actual.edit().putString("session",snapshot)
                .putString("history","[]").commit());
            SharedPreferences failing=(SharedPreferences)Proxy.newProxyInstance(
                SharedPreferences.class.getClassLoader(),
                new Class<?>[]{SharedPreferences.class},(proxy,method,args)->{
                    if("edit".equals(method.getName())){
                        return Proxy.newProxyInstance(
                            SharedPreferences.Editor.class.getClassLoader(),
                            new Class<?>[]{SharedPreferences.Editor.class},
                            (editor,action,arguments)->{
                                if("commit".equals(action.getName()))return false;
                                if("apply".equals(action.getName()))return null;
                                if(SharedPreferences.Editor.class.isAssignableFrom(
                                    action.getReturnType()))return editor;
                                return null;
                            });
                    }
                    return method.invoke(actual,args);
                });
            assertFalse("Must reject a failed commit before changing game",
                LocalHistoryRepository.archiveSnapshot(failing,snapshot,
                    "12. stu","22:00"));
            assertEquals(snapshot,actual.getString("session",null));
            assertEquals("[]",actual.getString("history",null));
        }finally{assertTrue(actual.edit().clear().commit());}
    }

    @Test public void oldHistoryLimitAndOrderRemainIdentical() throws Exception{
        SharedPreferences prefs=isolated();
        assertTrue(prefs.edit().clear().commit());
        try{
            JSONArray past=new JSONArray();
            for(int i=0;i<22;i++)
                past.put(new JSONObject().put("score",100+i));
            String snapshot=game(55L,2048).toJson().toString();
            assertTrue(prefs.edit().putString("session",snapshot)
                .putString("history",past.toString()).commit());
            assertTrue(LocalHistoryRepository.archiveSnapshot(prefs,snapshot,
                "12. stu","22:01"));
            JSONArray history=new JSONArray(prefs.getString("history","[]"));
            assertEquals(20,history.length());
            assertEquals(2048,history.getJSONObject(0).getInt("score"));
            assertEquals(100,history.getJSONObject(1).getInt("score"));
            assertEquals(118,history.getJSONObject(19).getInt("score"));
        }finally{assertTrue(prefs.edit().clear().commit());}
    }
}
