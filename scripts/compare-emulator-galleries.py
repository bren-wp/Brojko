#!/usr/bin/env python3
"""Compare every PNG in two genuine Android emulator screenshot artifact ZIPs.

No image generation, scaling, alignment, colour correction or artificial game
results. Use official "Brojko-real-device-UI" archives from GitHub Actions.

Usage:
  python3 -m pip install numpy Pillow
  python3 scripts/compare-emulator-galleries.py \
      --baseline BROJKO-v249-emulator.zip \
      --candidate BROJKO-pr150-emulator.zip \
      --report /tmp/brojko-pr150-regression.json
"""
from __future__ import annotations

import argparse
import hashlib
from io import BytesIO
import json
from pathlib import Path
import sys
from zipfile import ZipFile, BadZipFile

try:
    import numpy as np
    from PIL import Image
except ImportError as exc:
    sys.exit("Install audit dependencies: python3 -m pip install numpy Pillow\n"
             + str(exc))


def image_paths(archive: ZipFile) -> set[str]:
    names = [name for name in archive.namelist()
             if name.startswith("android-screenshots/screenshots/")
             and name.lower().endswith(".png")
             and not name.endswith("/")]
    if len(names) != len(set(names)):
        raise ValueError("Duplicate image paths in the archive")
    return set(names)


def read_png(archive: ZipFile, path: str) -> np.ndarray:
    # Both sources are genuine emulator captures; never crop or resize.
    with Image.open(BytesIO(archive.read(path))) as image:
        if image.format != "PNG":
            raise ValueError("Not a PNG: " + path)
        image.load()
        return np.asarray(image.convert("RGB"), dtype=np.int16)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--baseline", type=Path, required=True)
    parser.add_argument("--candidate", type=Path, required=True)
    parser.add_argument("--report", type=Path, help="Write JSON outside Git")
    args = parser.parse_args()

    report: dict = {
        "baseline_artifact": args.baseline.name,
        "candidate_artifact": args.candidate.name,
        "pixel_policy": "unscaled, unaligned, exact source dimensions",
        "screens": [],
    }
    try:
        with ZipFile(args.baseline) as baseline, ZipFile(args.candidate) as candidate:
            old = image_paths(baseline)
            new = image_paths(candidate)
            if not old or not new:
                raise ValueError("Missing required screenshot gallery")
            report["missing_from_candidate"] = sorted(old - new)
            report["new_in_candidate"] = sorted(new - old)
            unchanged = 0
            for path in sorted(old & new):
                first = baseline.read(path)
                second = candidate.read(path)
                if hashlib.sha256(first).digest() == hashlib.sha256(second).digest():
                    report["screens"].append({
                        "path": path, "byte_identical": True, "rgb_mae": 0.0,
                        "exact_rgb_percent": 100.0,
                    })
                    unchanged += 1
                    continue

                a = read_png(baseline, path)
                b = read_png(candidate, path)
                if a.shape != b.shape:
                    report["screens"].append({
                        "path": path, "error": "native screenshot dimensions differ",
                        "baseline_shape": a.shape, "candidate_shape": b.shape,
                    })
                    continue
                diff = np.abs(a - b)
                report["screens"].append({
                    "path": path, "byte_identical": False,
                    "rgb_mae": round(float(diff.mean()), 6),
                    "exact_rgb_percent": round(
                        float(np.all(diff == 0, axis=2).mean()) * 100, 6
                    ),
                })
            report["screenshot_count_before"] = len(old)
            report["screenshot_count_after"] = len(new)
            report["exact_png_byte_matches"] = unchanged
            report["visually_changed"] = len(old & new) - unchanged
    except (ValueError, OSError, BadZipFile) as error:
        print("Cannot compare artifacts:", error, file=sys.stderr)
        return 2

    print("Baseline / candidate PNGs:",
          report["screenshot_count_before"], "/",
          report["screenshot_count_after"])
    print("Exact PNG byte matches:", report["exact_png_byte_matches"])
    print("Changed matches:", report["visually_changed"])
    if report["missing_from_candidate"]:
        print("ERROR: missing screenshots:", report["missing_from_candidate"])
    if report["new_in_candidate"]:
        print("New screenshots:", report["new_in_candidate"])
    differences = [
        x for x in report["screens"]
        if not x.get("byte_identical", False)
    ]
    for entry in sorted(differences, key=lambda x: x.get("rgb_mae", float("inf")),
                        reverse=True)[:30]:
        print(f"{entry['path']}: MAE={entry.get('rgb_mae', 'shape changed')}")
    if args.report is not None:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(
            json.dumps(report, indent=2, ensure_ascii=False) + "\n",
            encoding="utf-8"
        )
    return 1 if report["missing_from_candidate"] or any(
        "error" in entry for entry in report["screens"]
    ) else 0


if __name__ == "__main__":
    sys.exit(main())
