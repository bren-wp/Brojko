#!/usr/bin/env python3
"""Compare the three owner PNGs against actual Android emulator captures.

Developer-only dependencies: pillow, numpy. No network, no production assets.
Do NOT resize the files: all PNGs must be 941x1672 pixels.
"""
import argparse
import json
from pathlib import Path
import numpy as np
from PIL import Image

SIZE = (941, 1672)
ROI = {
    "splash": {
        "upper_tiles": (0, 90, 941, 675),
        "logo": (55, 680, 880, 990),
        "tagline": (45, 1285, 900, 1430),
        "lower_accents": (0, 1450, 941, 1672),
    },
    "home": {
        "logo": (130, 200, 780, 475),
        "score_cards": (50, 475, 915, 665),
        "primary_button": (125, 678, 810, 884),
        "modes": (35, 900, 908, 1305),
        "shortcuts": (35, 1330, 910, 1640),
    },
    "bravo": {
        "crown": (345, 300, 600, 535),
        "heading": (250, 520, 710, 710),
        "score_and_stars": (170, 795, 780, 957),
        "three_buttons": (165, 960, 770, 1385),
        "board_backdrop": (30, 236, 900, 1130),
    },
}


def evaluate(owner, emulator, section):
    with Image.open(owner) as a, Image.open(emulator) as b:
        if a.size != SIZE or b.size != SIZE:
            raise ValueError(f"{section}: native {SIZE} required; got {a.size}, {b.size}")
        src = np.asarray(a.convert("RGB"), dtype=np.int16)
        out = np.asarray(b.convert("RGB"), dtype=np.int16)
    dif = np.abs(src - out)
    regions = {"full": (0, 0, *SIZE), **ROI[section]}
    scores = {}
    for name, (x0, y0, x1, y1) in regions.items():
        delta = dif[y0:y1, x0:x1]
        mean_by_pixel = delta.mean(axis=2)
        scores[name] = {
            "mae": round(float(delta.mean()), 3),
            "near_12_percent": round(float((mean_by_pixel < 12).mean() * 100), 2),
            "far_48_percent": round(float((mean_by_pixel > 48).mean() * 100), 2),
        }
    return scores


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for screen in ROI:
        parser.add_argument(f"--{screen}-owner", type=Path, required=True)
        parser.add_argument(f"--{screen}-emulator", type=Path, required=True)
    parser.add_argument("--json-out", type=Path)
    args = parser.parse_args()
    results = {}
    for screen in ROI:
        results[screen] = evaluate(getattr(args, screen + "_owner"),
                                   getattr(args, screen + "_emulator"), screen)
        print(f"{screen}: full RGB MAE {results[screen]['full']['mae']}/255")
        for name, score in results[screen].items():
            if name != "full":
                print(f"  {name}: {score['mae']}/255")
    if args.json_out:
        args.json_out.write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")
    print("MAE is not a similarity percentage; use identical fixture data.")


if __name__ == "__main__":
    main()
