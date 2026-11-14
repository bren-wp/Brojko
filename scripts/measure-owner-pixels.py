#!/usr/bin/env python3
"""Measure unmodified owner reference PNGs against genuine emulator PNGs.

Usage:
  python3 scripts/measure-owner-pixels.py \
      --references "/private/original-brojko-pngs" \
      --capture-zip "/private/BROJKO-emulator.zip" \
      --report "/private/brojko-rgb-audit.json"

Install locally (not required by the Android build): pip install numpy Pillow
Reference images are never copied into the repository or the Android app.
"""
from __future__ import annotations

import argparse
from io import BytesIO
import json
from pathlib import Path
import re
import sys
from zipfile import ZipFile

try:
    import numpy as np
    from PIL import Image
except ImportError as error:
    sys.exit("Install local audit dependencies: python3 -m pip install numpy Pillow\n" + str(error))

# The owner's October 24 source names and the fixed 941x1672 native
# captures. No resizing, alignment, sharpening, AI regeneration or shifts.
SCREENS = {
    2: ("Splash", "reference-941x1672-splash.png"),
    3: ("Naslovnica", "home-controlled-reference-941x1672.png"),
    4: ("Klasično 4x4", "golden-state-game-941x1672.png"),
    5: ("Pobjeda", "golden-state-win-941x1672.png"),
    6: ("Dnevni izazov", "daily-refined-board-gift-941x1672.png"),
    7: ("Misije", "missions-six-real-progress-941x1672.png"),
    8: ("Statistika", "golden-state-stats-941x1672.png"),
    9: ("Teme i izgled", "themes-original-final-aligned-941x1672.png"),
    10: ("Postavke", "settings-phase08-source-on-941x1672.png"),
}
REGIONS = {
    6: {"3D poklon": (90, 1300, 290, 1510)},
    8: {"Grafikon": (55, 790, 880, 1100)},
    9: {
        "Sve tri kartice": (32, 370, 905, 790),
        "Zivopisna kartica": (635, 393, 900, 672),
        "Zivopisna lijeva pozadina": (645, 405, 685, 655),
        "Zivopisna desna pozadina": (855, 405, 890, 648),
        "Pregled plocica": (30, 820, 500, 1220),
    },
}
REFERENCE_SIZE = (941, 1672)
# Source 22_15_05-1 is a separate landscape brand board, not a
# 941x1672 application screen; intentionally excluded from this audit.


def load_reference(directory: Path, number: int) -> tuple[Path, np.ndarray]:
    # Match the owner's actual -N source suffix, not a screenshot made by CI.
    matches = [
        file for file in directory.glob("*.png")
        if re.search(r"22_15_\d+-" + str(number) + r"(?:\D|$)", file.name)
    ]
    if len(matches) != 1:
        raise ValueError(
            f"Expected exactly one original owner source -{number}, "
            f"found {len(matches)}: {[p.name for p in matches]}"
        )
    image = Image.open(matches[0]).convert("RGB")
    if image.size != REFERENCE_SIZE:
        raise ValueError(f"Wrong reference size {matches[0]}: {image.size}")
    return matches[0], np.asarray(image, dtype=np.int16)


def load_capture(archive: ZipFile, filename: str) -> np.ndarray:
    matches = [path for path in archive.namelist()
               if path.endswith("/" + filename) or path == filename]
    if len(matches) != 1:
        raise ValueError(f"Expected one genuine emulator image {filename}: {matches}")
    image = Image.open(BytesIO(archive.read(matches[0]))).convert("RGB")
    if image.size != REFERENCE_SIZE:
        raise ValueError(f"Wrong emulator image size {filename}: {image.size}")
    return np.asarray(image, dtype=np.int16)


def metrics(owner: np.ndarray, emulator: np.ndarray, bounds=None) -> dict:
    if bounds is not None:
        x0, y0, x1, y1 = bounds
        if not (0 <= x0 < x1 <= 941 and 0 <= y0 < y1 <= 1672):
            raise ValueError(f"Invalid fixed ROI: {bounds}")
        owner, emulator = owner[y0:y1, x0:x1], emulator[y0:y1, x0:x1]
    delta = np.abs(owner - emulator)
    return {
        "rgb_mae_0_to_255": round(float(np.mean(delta)), 6),
        "exact_rgb_pixels_percent": round(
            float(np.mean(np.all(delta == 0, axis=2)) * 100), 6
        ),
        "pixels": int(delta.shape[0] * delta.shape[1]),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--references", type=Path, required=True)
    parser.add_argument("--capture-zip", type=Path, required=True)
    parser.add_argument("--report", type=Path, help="Write audit JSON outside the repo")
    args = parser.parse_args()

    output = {
        "comparison": "unaligned native 941x1672 owner vs genuine Android emulator",
        "reference_board_1": "landscape brand sheet (excluded)",
        "screens": [],
    }
    with ZipFile(args.capture_zip) as archive:
        for number, (name, capture_name) in SCREENS.items():
            source_file, owner = load_reference(args.references, number)
            emulator = load_capture(archive, capture_name)
            entry = {
                "screen": name,
                "owner_source": source_file.name,
                "emulator_capture": capture_name,
                **metrics(owner, emulator),
                "regions": {
                    region: metrics(owner, emulator, xy)
                    for region, xy in REGIONS.get(number, {}).items()
                },
            }
            output["screens"].append(entry)
            print(
                f"{name:19} RGB MAE={entry['rgb_mae_0_to_255']:9.6f} "
                f"exact RGB={entry['exact_rgb_pixels_percent']:6.3f}%"
            )
    result = json.dumps(output, ensure_ascii=False, indent=2) + "\n"
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(result, encoding="utf-8")
        print("Audit report:", args.report)


if __name__ == "__main__":
    main()
