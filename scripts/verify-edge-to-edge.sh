#!/usr/bin/env bash
# Guard against accidental reintroduction of deprecated Android 15 system-bar
# color setter calls and attributes in the API 35+ theme.
set -euo pipefail
java=app/src/main/java/com/brendigo/brojko/MainActivity.java
api35=app/src/main/res/values-v35/styles.xml
test -s "$java"
test -s "$api35"

# Match executable calls, not explanatory comments mentioning the API.
if grep -En '^[[:space:]]*[[:alpha:]_].*(\.setStatusBarColor\(|\.setNavigationBarColor\(|WindowCompat\.enableEdgeToEdge\()' "$java" ; then
  echo "Deprecated bar-color setup reintroduced in MainActivity" >&2
  exit 1
fi
if grep -Eq '<item name="android:(statusBarColor|navigationBarColor)"' "$api35"; then
  echo "API 35+ theme must not specify deprecated system bar colors" >&2
  exit 1
fi
grep -Fq 'WindowCompat.setDecorFitsSystemWindows(getWindow(),false);' "$java"
grep -Fq 'WindowInsetsCompat.Type.systemBars()' "$java"
grep -Fq 'WindowInsetsCompat.Type.displayCutout()' "$java"
grep -Fq 'controller.setAppearanceLightStatusBars(lightBackground);' "$java"
grep -Fq 'controller.setAppearanceLightNavigationBars(lightBackground);' "$java"
echo "Android 15/16 edge-to-edge source and API 35 theme guard passed"
