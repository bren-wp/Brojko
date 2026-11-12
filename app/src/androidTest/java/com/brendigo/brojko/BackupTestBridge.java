package com.brendigo.brojko;

import org.json.JSONArray;

/**
 * Test-source only bridge to current repository APIs. These compatibility
 * helpers never enter the release APK and keep legacy QA cases executable.
 */
final class BackupTestBridge {
    private BackupTestBridge(){}

    static byte[] export(MainActivity activity) throws Exception{
        return LocalBackupRepository.export(activity.prefs,activity.today(),
            activity.getPackageName());
    }

    static void restore(MainActivity activity,byte[] bytes) throws Exception{
        LocalBackupRepository.RestorePlan verified=
            LocalBackupRepository.validate(activity.getPackageName(),bytes);
        LocalBackupRepository.apply(activity.prefs,verified);
        activity.skipPersistOnPauseAfterImport=true;
    }

    static void validateHistory(JSONArray rows){
        LocalBackupRepository.validateHistory(rows);
    }
}
