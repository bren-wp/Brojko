#!/usr/bin/env python3
"""Check that the README only advertises real screenshots staged by Android QA.

No web requests, no artificial images and no screenshots packaged into the APK.
"""
import ast
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
stage = (ROOT / "scripts/stage-marketing-screenshots.py").read_text(encoding="utf-8")
readme = (ROOT / "README.md").read_text(encoding="utf-8")
workflow = (ROOT / ".github/workflows/android-ui-qa.yml").read_text(encoding="utf-8")

syntax = ast.parse(stage)
mapping = None
for node in syntax.body:
    if isinstance(node, ast.Assign) and any(
        isinstance(target, ast.Name) and target.id == "CAPTURES"
        for target in node.targets
    ):
        mapping = ast.literal_eval(node.value)
        break
assert isinstance(mapping, dict) and len(mapping) == 28, (
    "The production screenshot gallery must cover 28 real Android captures"
)
assert len(set(mapping.values())) == len(mapping), "Do not reuse one capture for multiple screens"
assert "if (( ${#screenshots[@]} != 28 )); then" in workflow, (
    "Publication asset count must match the real screenshots in CAPTURES"
)
# Link images to a release that already contains all real emulator captures.
# The latest release may temporarily have only APK/AAB while UI QA is running.
verified = re.search(
    r"Galerija koristi provjerene snimke iz izdanja \*\*(v\d+\.\d+\.\d+)\*\*",
    readme,
)
assert verified, "README must identify its complete verified screenshot release"
gallery_tag = verified.group(1)
assert "releases/latest/download/brojko-" not in readme, (
    "Do not embed images from a release before screenshot QA has finished"
)
for name, origin in mapping.items():
    assert name.endswith(".png") and origin.endswith(".png")
    url = "https://github.com/bren-wp/Brojko/releases/download/" + gallery_tag + "/" + name
    assert f'src="{url}"' in readme, "Missing screenshot in README: " + name
    assert f'alt="Stvarna Android snimka Brojko:' in readme, "Screenshot alt text is required"
assert "realne snimke" in readme.lower() or "stvarne snimke" in readme.lower()
assert "python3 scripts/stage-marketing-screenshots.py" in workflow
print(f"OK: README advertises {len(mapping)} distinct Android captures from verified emulator QA")
