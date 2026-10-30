#!/usr/bin/env bash
set -euo pipefail
test -f app/build/outputs/apk/debug/app-debug.apk
test -f app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

# Switch actual navigation modes: simulated insets alone are insufficient.
overlays="$(adb shell cmd overlay list --user 0 | tr -d '\r')"
for mode in gestural threebutton; do
  component="com.android.internal.systemui.navbar.$mode"
  if ! printf '%s\n' "$overlays" | grep -Fq "$component"; then
    echo "::error::Navigation overlay $component is missing on emulator" >&2
    exit 1
  fi
  adb shell cmd overlay enable --user 0 "$component"
  adb wait-for-device
  sleep 3
  adb shell am force-stop com.brendigo.brojko || true
  echo "Verifying real window insets with $mode navigation"
  log="edge-to-edge-$mode.txt"
  adb shell am instrument -w \
    -e class 'com.brendigo.brojko.ScreensSmokeTest#edgeToEdgeInsetsAreAppliedBeforeScreenControls,com.brendigo.brojko.ScreensSmokeTest#edgeToEdgeCriticalButtonsAreOutsideSystemBars' \
    com.brendigo.brojko.test/androidx.test.runner.AndroidJUnitRunner | tee "$log"
  grep -Eq '^OK [(]2 tests[)]$' "$log"
done
