#!/usr/bin/env bash
# Retry interrupted ADB file transfer only; never waive the screenshot gate.
set -euo pipefail
mkdir -p android-screenshots
for attempt in 1 2 3 4; do
  echo "BROJKO Android screenshot transfer: attempt ${attempt}/4"
  if adb pull /sdcard/Android/data/com.brendigo.brojko/files/screenshots/ android-screenshots/; then
    if python3 scripts/verify-emulator-screenshots.py; then
      echo "Verified all genuine emulator screenshots."
      exit 0
    fi
  fi
  if [ "$attempt" -lt 4 ]; then
    echo "::warning::ADB screenshot transfer was incomplete; retrying."
    adb wait-for-device || true
    sleep "$(( attempt * 2 ))"
  fi
done
echo "::error::Unable to retrieve and verify all Android screenshots after four attempts."
exit 1
