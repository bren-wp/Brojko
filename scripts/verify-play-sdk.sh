#!/usr/bin/env bash
# Verifies the *compiled* Android APK manifest rather than just the Gradle DSL.
# Keep this independent from release-signing secrets.
set -euo pipefail

if (( $# == 0 )); then
  echo "Usage: bash scripts/verify-play-sdk.sh app-*.apk [...]" >&2
  exit 2
fi

SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
AAPT="${SDK_ROOT}/build-tools/36.0.0/aapt"
if [[ ! -x "${AAPT}" ]]; then
  echo "Android aapt executable was not found at ${AAPT}" >&2
  exit 2
fi

for apk in "$@"; do
  if [[ ! -s "${apk}" ]]; then
    echo "APK is missing or empty: ${apk}" >&2
    exit 1
  fi
  badging="$("${AAPT}" dump badging "${apk}")"
  package="$(sed -nE "s/^package: name='([^']+)'.*/\1/p" <<< "${badging}" | head -n 1)"
  minimum="$(sed -nE "s/^sdkVersion:'([0-9]+)'.*/\1/p" <<< "${badging}" | head -n 1)"
  target="$(sed -nE "s/^targetSdkVersion:'([0-9]+)'.*/\1/p" <<< "${badging}" | head -n 1)"
  printf 'Compiled Play manifest: %s package=%s minSdk=%s targetSdk=%s\n' \
    "${apk}" "${package}" "${minimum}" "${target}"
  if [[ "${package}" != "com.brendigo.brojko" ||
        ! "${minimum}" =~ ^[0-9]+$ || ! "${target}" =~ ^[0-9]+$ ||
        "${minimum}" -lt 24 || "${target}" -lt 36 ]]; then
    echo "Play manifest validation failed: Brojko must use its stable package, minSdk >= 24 and targetSdk >= 36." >&2
    exit 1
  fi
done
