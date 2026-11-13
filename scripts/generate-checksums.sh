#!/usr/bin/env bash
# Generate checksums for filenames as published by GitHub Releases.
set -euo pipefail
shopt -s nullglob

# PRs exercise the checksum generator without signing keys; only the
# verified, signed release outputs may appear in published SHA256SUMS.
if [[ "${1:-}" == "--release-only" ]]; then
  artifacts=(
    app/build/outputs/apk/release/app-release.apk
    app/build/outputs/bundle/release/app-release.aab
  )
  minimum=2
elif [[ $# -eq 0 ]]; then
  artifacts=(
    app/build/outputs/apk/debug/*.apk
    app/build/outputs/apk/release/*.apk
    app/build/outputs/bundle/release/*.aab
  )
  minimum=3
else
  echo "Usage: bash scripts/generate-checksums.sh [--release-only]" >&2
  exit 2
fi
if (( ${#artifacts[@]} < minimum )); then
  echo "::error::Missing APK/AAB build outputs; refusing empty release checksums" >&2
  exit 1
fi

for file in "${artifacts[@]}"; do
  if [[ ! -s "$file" ]]; then
    echo "::error::Missing or empty checksum input: $file" >&2
    exit 1
  fi
done

declare -A seen=()
: > SHA256SUMS
for file in "${artifacts[@]}"; do
  name="${file##*/}"
  if [[ -n "${seen[$name]:-}" ]]; then
    echo "::error::Duplicate release filename: $name" >&2
    exit 1
  fi
  seen[$name]=1
  digest="$(sha256sum "$file" | cut -d ' ' -f1)"
  printf '%s  %s\n' "$digest" "$name" >> SHA256SUMS
done
LC_ALL=C sort -k2,2 SHA256SUMS -o SHA256SUMS
test -s SHA256SUMS
echo "Checksums generated for ${#artifacts[@]} APK/AAB packages"
