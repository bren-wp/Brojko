#!/usr/bin/env python3
"""Production source audit for BROJKO (run during each Android PR).

This is a static guard and a dead-code *candidate* report. It cannot replace
Android lint, merged-manifest inspection, SCA, penetration testing or Play review.
"""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
SRC = APP / "java/com/brendigo/brojko"
ANDROID = "{http://schemas.android.com/apk/res/android}"
manifest = ET.parse(APP / "AndroidManifest.xml").getroot()
app = manifest.find("application")
assert app is not None, "Missing Android application node"
assert app.get(ANDROID + "usesCleartextTraffic") == "false", "Cleartext must stay disabled"
assert app.get(ANDROID + "allowBackup") == "false", "System cloud backup must be opted out"
assert app.get(ANDROID + "debuggable") != "true", "Do not enable production debugging"

allowed_permissions = {
    "android.permission.VIBRATE",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.RECEIVE_BOOT_COMPLETED",
}
permissions = {
    elem.get(ANDROID + "name")
    for elem in manifest.findall("uses-permission")
}
assert permissions <= allowed_permissions, "Unexpected manifest permission: " + str(permissions - allowed_permissions)
assert "android.permission.INTERNET" not in permissions

exported = []
for tag in ("activity", "service", "receiver", "provider"):
    for elem in app.findall(tag):
        name = elem.get(ANDROID + "name", "")
        is_exported = elem.get(ANDROID + "exported", "false") == "true"
        if is_exported:
            exported.append(name)
        if tag == "provider":
            assert not is_exported, "Do not export a storage provider"
        if tag == "receiver":
            assert not is_exported, "Do not export game notification receivers"
assert exported == [".MainActivity"], "Only the launcher activity may be exported: " + str(exported)

java = {p.name: p.read_text(encoding="utf-8") for p in sorted(SRC.glob("*.java"))}
# Explicit production source inventory. New files must be reviewed instead of
# silently bypassing the security/dead-code checks.
expected_sources = {
    "AppReminder.java", "AppReminderBoot.java",
    "ClassicBrandContours.java", "SplashBrandContours.java",
    "MainActivity.java", "TileGame.java", "OwnerHeadlineContours.java",
    "LocalBackupRepository.java", "LocalHistoryRepository.java",
    "GameArchiveCoordinator.java",
    "BackupDocumentIo.java",
    "BackNavigationPolicy.java",
    "BoardAccessibilityDelegate.java",
}
assert set(java) == expected_sources, (
    "Audit inventory changed: added=" + str(set(java)-expected_sources)
    + ", missing=" + str(expected_sources-set(java))
)
combined = "\n".join(java.values())

# Keep the app entirely offline and prevent new unsafe dynamic entry points.
blocked = (
    "Runtime.getRuntime().exec(",
    "DexClassLoader(",
    "PathClassLoader(",
    "System.loadLibrary(",
    "setJavaScriptEnabled(true)",
    "MODE_WORLD_READABLE",
    "MODE_WORLD_WRITEABLE",
    "PendingIntent.FLAG_MUTABLE",
)
for needle in blocked:
    assert needle not in combined, "Review prohibited/unsafe source API: " + needle
assert "PendingIntent.FLAG_IMMUTABLE" in java["AppReminder.java"]
assert 'Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())' in java["AppReminderBoot.java"]
assert "getBoolean(\"notifications\",false)" in java["AppReminderBoot.java"]
assert "packageName.equals(root.optString(\"package\",\"\")" in java["LocalBackupRepository.java"]
# The 512-kB cap moved with SAF I/O from Activity to a dedicated worker.
assert "size>MAX_BYTES" in java["BackupDocumentIo.java"]
assert "bytes.length>512000" in java["LocalBackupRepository.java"]
assert "onActivityResult" in java["MainActivity.java"]
assert "legacyScore<0" in java["MainActivity.java"]
assert "imported.toJson().toString()" in java["MainActivity.java"]
assert "older.peak>game.peak" in java["TileGame.java"]
assert "if(completeSnapshot)" in java["LocalBackupRepository.java"]
assert "editor.remove(key)" in java["LocalBackupRepository.java"]
assert "LocalBackupRepository.restore(" in java["MainActivity.java"]
assert "LocalBackupRepository.validate(" in java["MainActivity.java"]
assert "LocalBackupRepository.apply(" in java["MainActivity.java"]
assert "BackupDocumentIo.read(" in java["MainActivity.java"]
assert "BackupDocumentIo.write(" in java["MainActivity.java"]
assert "backupIo.shutdown()" in java["MainActivity.java"]
assert "backupImportBusy||gameTransitionBusy" in java["MainActivity.java"]
assert "LocalHistoryRepository.archive(" in java["MainActivity.java"]
assert "GameArchiveCoordinator.start(" in java["MainActivity.java"]
assert "LocalHistoryRepository.archiveSnapshot(" in java["GameArchiveCoordinator.java"]
assert 'putString("session",archivedSession)' in java["LocalHistoryRepository.java"]
assert 'putInt("lastScore",snapshot.score).commit()' in java["LocalHistoryRepository.java"]
assert "LocalHistoryRepository.hasCommittedArchive(prefs)" in java["MainActivity.java"]
assert "if(gameTransitionBusy)return;" in java["MainActivity.java"]
assert "GameArchiveCoordinator.attach(this)" in java["MainActivity.java"]

gradle = (ROOT / "app/build.gradle").read_text(encoding="utf-8")
assert "minSdk 24" in gradle and "targetSdk 36" in gradle
assert "compileSdk 36" in gradle, "AndroidX Core newer releases require unavailable SDK 37"
assert "androidx.core:core:1.18.0" in gradle
assert "minifyEnabled true" in gradle and "shrinkResources true" in gradle
assert not re.search(r"(implementation|androidTestImplementation|testImplementation)[^\n]*:\+['\"]", gradle)

# Candidate discovery only: Android lifecycle methods, UI callbacks, tests,
# dynamic page routing and Java reflection make automatic deletion unsafe.
# Confirm one-by-one before touching code.
candidates = []
for path, body in java.items():
    for match in re.finditer(
        r"\b(?:void|boolean|int|long|float|String|JSONObject|JSONArray|byte\[\])\s+"
        r"([A-Za-z_]\w*)\s*\([^;{}]*?\)\s*(?:throws\s+[A-Za-z, ]+)?\s*\{",
        body,
    ):
        method = match.group(1)
        # Use all production classes, not merely the defining file.
        mentions = len(re.findall(r"\b" + re.escape(method) + r"\s*\(", combined))
        if mentions == 1 and method not in {
            "onCreate", "onPause", "onDestroy", "onDraw", "onReceive",
            "onActivityResult", "onRequestPermissionsResult",
            "onSaveInstanceState", "onKeyDown", "onInitializeAccessibilityNodeInfo",
            "handleOnBackPressed", "onTouchEvent", "performClick", "onClick",
        }:
            candidates.append(path + ":" + method)
assert "classicGameplayHeading(" not in combined, "Proven unreferenced Classic renderer returned"
print("Security source audit: manifest exports, allowed permissions, offline API, "
      "backup bounds, undo integrity, release flags and pinned dependencies: OK")
print("Dead-code candidate scan (NOT proof of unreachability):")
for item in candidates:
    print("  REVIEW: " + item)
print("Scanned %d production Java classes; %d potential dead-code symbols."
      % (len(java), len(candidates)))

# Screenshot/gameplay test refactoring preserves all five old cases
# in their asynchronous, deterministic instrumentation suite.
flow_test=(ROOT/"app/src/androidTest/java/com/brendigo/brojko/GameplayAsyncArchiveFlowTest.java").read_text(encoding="utf-8")
for name in (
    "failedDailyReplayCannotClaimSuccessReward",
    "confirmationCancelIsNonDestructiveAndConfirmedEndArchives",
    "confirmNewGameArchivesBeforeSwitchingToRequestedMode",
    "gameOverNewGameArchivesExactlyOnceAndBackKeepsSession",
    "dailyResultButtonsPreserveRewardDateAndArchiveRealScore",
):
    assert ("void "+name+"(") in flow_test, "Missing gameplay regression: "+name
assert "awaitTransaction()" in flow_test
assert "gameTransitionBusy" in flow_test
