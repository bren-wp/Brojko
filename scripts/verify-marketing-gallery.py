#!/usr/bin/env python3
"""Validate the English-first marketing gallery and separate Croatian gallery.

No web requests, no artificial images and no screenshots packaged into the APK.
Release asset existence must also be checked before merging a gallery update.
"""
import ast
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
stage = (ROOT / "scripts/stage-marketing-screenshots.py").read_text(encoding="utf-8")
local_stage = (ROOT / "scripts/stage-localized-store-assets.py").read_text(encoding="utf-8")
readme = (ROOT / "README.md").read_text(encoding="utf-8")
croatian = (ROOT / "README.hr.md").read_text(encoding="utf-8")
workflow = (ROOT / ".github/workflows/android-ui-qa.yml").read_text(encoding="utf-8")

def literal_assignment(source, name):
    for node in ast.parse(source).body:
        if isinstance(node, ast.Assign) and any(
            isinstance(target, ast.Name) and target.id == name for target in node.targets
        ):
            return ast.literal_eval(node.value)
    raise AssertionError("Missing staging inventory: " + name)

mapping = literal_assignment(stage, "CAPTURES")
pages = literal_assignment(local_stage, "PAGES")
assert isinstance(mapping, dict) and len(mapping) == 28
assert len(set(mapping.values())) == len(mapping)
assert len(pages) == len(set(pages)) == 23
assert 'if (( ${#screenshots[@]} != 28 )); then' in workflow
assert 'if (( ${#english[@]} != 23 )); then' in workflow
assert "python3 scripts/stage-marketing-screenshots.py" in workflow
assert "--english-gallery" in workflow

verified = re.search(
    r"Gallery uses native English captures from release \*\*(v\d+\.\d+\.\d+)\*\*",
    readme,
)
assert verified, "README must identify the exact English gallery release"
tag = verified.group(1)
prefix = "https://github.com/bren-wp/Brojko/releases/download/"
assert "releases/latest/download/brojko-" not in readme
gallery = readme.split("## Real Android screenshots", 1)[1].split("## Your game, your data", 1)[0]
english = re.findall(r'src="([^"]+brojko-en-[^"]+\.png)"', gallery)
expected = {prefix + tag + "/brojko-en-" + page + ".png" for page in pages}
assert len(english) == len(expected) and set(english) == expected, "English gallery must cover every native state exactly once"
all_game_images = re.findall(r'src="([^"]+/brojko-[^"]+\.png)"', readme)
assert all(url in expected or url.endswith("/brojko-icon.png") for url in all_game_images), "Croatian game captures must not appear in English README"
assert "actual android captures" in readme.lower()
assert 'alt="Real English Android screenshot of Brojko:' in gallery
for name, origin in mapping.items():
    assert name.endswith(".png") and origin.endswith(".png")
    assert any(url.endswith("/" + name) for url in re.findall(r'src="([^"]+)"', croatian)), "Missing Croatian gallery image: " + name
print(f"OK: {len(pages)} English native states in README; {len(mapping)} original Croatian images in README.hr.md")
