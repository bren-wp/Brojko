package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Validation on worker and commit on caller thread share a safe immutable plan. */
@RunWith(AndroidJUnit4.class)
public final class BackupValidationWorkerTest {
    private static final String PACKAGE="com.brendigo.brojko";
    private static String repeat(char value,int length){
        char[] result=new char[length];
        java.util.Arrays.fill(result,value);
        return new String(result);
    }
    private SharedPreferences testPrefs(){
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        return c.getSharedPreferences("brojko-backup-worker-test",Context.MODE_PRIVATE);
    }

    @Test public void workerValidatesBeforeAnyUserPreferenceMutation() throws Exception {
        SharedPreferences prefs=testPrefs();
        assertTrue(prefs.edit().clear().putInt("best",64)
            .putString("futureSetting","do-not-remove").commit());
        ExecutorService executor=Executors.newSingleThreadExecutor();
        try{
            byte[] original=LocalBackupRepository.export(prefs,"2026-11-12",PACKAGE);
            JSONObject edited=new JSONObject(new String(original,StandardCharsets.UTF_8));
            edited.getJSONObject("data").put("best",2048);
            byte[] importBytes=edited.toString().getBytes(StandardCharsets.UTF_8);
            Future<LocalBackupRepository.RestorePlan> prepared=executor.submit(
                ()->LocalBackupRepository.validate(PACKAGE,importBytes));
            LocalBackupRepository.RestorePlan plan=prepared.get();
            assertEquals("Validation must not commit to preferences",
                64,prefs.getInt("best",-1));
            LocalBackupRepository.apply(prefs,plan);
            assertEquals(2048,prefs.getInt("best",-1));
            assertEquals("do-not-remove",prefs.getString("futureSetting",null));
        }finally{
            executor.shutdownNow();
            assertTrue(prefs.edit().clear().commit());
        }
    }

    @Test public void invalidDataFailsOnWorkerWithoutChangingProgress() throws Exception {
        SharedPreferences prefs=testPrefs();
        assertTrue(prefs.edit().clear().putInt("best",4096).commit());
        Map<String,?> before=new HashMap<>(prefs.getAll());
        ExecutorService executor=Executors.newSingleThreadExecutor();
        try{
            byte[] valid=LocalBackupRepository.export(prefs,"2026-11-12",PACKAGE);
            JSONObject edited=new JSONObject(new String(valid,StandardCharsets.UTF_8));
            edited.getJSONObject("data").put("best","NOT_A_NUMBER");
            byte[] invalid=edited.toString().getBytes(StandardCharsets.UTF_8);
            Future<Boolean> rejected=executor.submit(()->{
                try{
                    LocalBackupRepository.validate(PACKAGE,invalid);
                    return false;
                }catch(IllegalArgumentException expected){
                    return true;
                }
            });
            assertTrue("Invalid backup must be rejected before commit",rejected.get());
            assertEquals(before,prefs.getAll());
        }finally{
            executor.shutdownNow();
            assertTrue(prefs.edit().clear().commit());
        }
    }
    @Test public void deeplyNestedBackupCannotOverflowParserOrChangeProgress() throws Exception {
        SharedPreferences prefs=testPrefs();
        assertTrue(prefs.edit().clear().putInt("best",4096).commit());
        try{
            String arrays=repeat('[',80)+"0"+repeat(']',80);
            String source="{\"format\":\"brendigo.brojko.backup.v1\","
                +"\"package\":\""+PACKAGE+"\",\"data\":{\"best\":4,\"extra\":"+arrays+"}}";
            try{
                LocalBackupRepository.validate(PACKAGE,
                    source.getBytes(StandardCharsets.UTF_8));
                fail("Reject excessive nesting before org.json recursion");
            }catch(IllegalArgumentException expected){
                assertTrue(expected.getMessage().contains("ugniježđenih"));
            }
            assertEquals(4096,prefs.getInt("best",-1));
        }finally{
            assertTrue(prefs.edit().clear().commit());
        }
    }

    @Test public void nestedHistoryAndMalformedUtf8AreRejectedWithoutRestore() throws Exception {
        SharedPreferences prefs=testPrefs();
        assertTrue(prefs.edit().clear().putInt("best",2048).commit());
        try{
            byte[] original=LocalBackupRepository.export(prefs,"2026-11-13",PACKAGE);
            JSONObject root=new JSONObject(new String(original,StandardCharsets.UTF_8));
            root.getJSONObject("data").put("history",repeat('[',80)+"0"+repeat(']',80));
            try{
                LocalBackupRepository.validate(PACKAGE,
                    root.toString().getBytes(StandardCharsets.UTF_8));
                fail("Embedded history must have bounded nesting");
            }catch(IllegalArgumentException expected){
                assertTrue(expected.getMessage().contains("ugniježđenih"));
            }
            byte[] invalid=java.util.Arrays.copyOf(original,original.length+1);
            invalid[invalid.length-1]=(byte)0xff;
            try{
                LocalBackupRepository.validate(PACKAGE,invalid);
                fail("Invalid UTF-8 must not be silently replaced");
            }catch(IllegalArgumentException expected){
                assertTrue(expected.getMessage().contains("UTF-8"));
            }
            assertEquals(2048,prefs.getInt("best",-1));
        }finally{
            assertTrue(prefs.edit().clear().commit());
        }
    }

}
