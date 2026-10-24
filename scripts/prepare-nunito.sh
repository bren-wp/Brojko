#!/usr/bin/env bash
set -euo pipefail
# Nunito by Vernon Adams and the Nunito Project Authors — SIL OFL 1.1.
# Immutable upstream Google Fonts revision and SHA-1 Git blob verification.
UPSTREAM="e7db6ca603fd4862010383ff17ffd6cc4ffac1c0"
BASE="https://raw.githubusercontent.com/google/fonts/${UPSTREAM}/ofl/nunito"
mkdir -p app/src/main/assets
tmp_font="$(mktemp)"
tmp_license="$(mktemp)"
trap 'rm -f "$tmp_font" "$tmp_license"' EXIT
curl --fail --silent --show-error --location --retry 3 \
  "${BASE}/Nunito%5Bwght%5D.ttf" -o "$tmp_font"
curl --fail --silent --show-error --location --retry 3 \
  "${BASE}/OFL.txt" -o "$tmp_license"
expected_font="2ec1f4b0676c83ef33049db87b311171699e9192"
expected_license="c8210f08ca3cece05b4f7baa428afe8e1abe3293"
test "$(git hash-object "$tmp_font")" = "$expected_font" || {
  echo "ERROR: Nunito checksum mismatch" >&2; exit 1;
}
test "$(git hash-object "$tmp_license")" = "$expected_license" || {
  echo "ERROR: Nunito license checksum mismatch" >&2; exit 1;
}
cp "$tmp_font" app/src/main/assets/Nunito.ttf
cp "$tmp_license" app/src/main/assets/Nunito-OFL.txt
echo "Bundled verified Nunito (SIL OFL 1.1) for Brojko."
