#!/usr/bin/env bash
# Fail before starting the Gradle wrapper if either pinned checksum changes.
set -euo pipefail
cd "$(dirname "$0")/.."
printf '%s  %s\n' '621008afc4f5d59357fcf06ffa644dd5aade5e091bdc363742710445f73cdaf8' gradle/wrapper/gradle-wrapper.jar | sha256sum -c -
grep -Fxq 'distributionUrl=https\://services.gradle.org/distributions/gradle-9.9.2-bin.zip' gradle/wrapper/gradle-wrapper.properties
grep -Fxq 'distributionSha256Sum=971fcd7150ca4ba04852c0b2ca0d2d0ab9468e14b03473eab19528809914c76b' gradle/wrapper/gradle-wrapper.properties
grep -Fxq 'org.gradle.caching=false' gradle.properties
