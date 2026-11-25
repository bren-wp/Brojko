#!/usr/bin/env bash
# Retry only the observed temporary Maven HTTP 429 resolution failure.
set -euo pipefail
log=$(mktemp)
trap 'rm -f "$log"' EXIT
for attempt in 1 2 3; do
  set +e
  ./gradlew "$@" 2>&1 | tee "$log"
  status=${PIPESTATUS[0]}
  set -e
  if [[ "$status" == 0 ]]; then exit 0; fi
  if [[ "$attempt" == 3 ]] || ! grep -Eq 'Received status code 429 from server' "$log" ||
     ! grep -Fq 'Could not resolve' "$log" || ! grep -Fq 'repo.maven.apache.org/maven2/' "$log"; then
    exit "$status"
  fi
  echo "::warning::Maven Central rate limit; retrying dependency resolution ($attempt/3)"
  sleep $((attempt * 30))
done
exit 1
