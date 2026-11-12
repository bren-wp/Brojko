package com.brendigo.brojko;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Back must not navigate while a slow SAF restore is processing. */
@RunWith(AndroidJUnit4.class)
public final class BackupBusyInteractionTest {
    @Rule public ActivityScenarioRule<MainActivity> scenario =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void systemBackIsGuardedDuringImport(){
        scenario.getScenario().onActivity(activity->{
            activity.navigate("backup");
            try{
                activity.backupDocumentBusy=true;
                activity.screen.setEnabled(false);
                activity.handleBackNavigation();
                assertEquals("backup",activity.page);
            }finally{
                activity.backupDocumentBusy=false;
                activity.screen.setEnabled(true);
            }
            activity.handleBackNavigation();
            assertEquals("settings",activity.page);
        });
    }
}
