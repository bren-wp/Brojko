#!/usr/bin/env python3
"""Compare Brojko real-emulator PNGs against the owner's reference PNGs.

Pillow is required only for this developer QA script, not by the Android app.
All images are compared at native resolution, with no resizing or alignment.
"""
import argparse
from pathlib import Path
from zipfile import ZipFile
from PIL import Image, ImageChops, ImageStat

SCREENS = {
    "Splash": ("06-2", "splash-full-width-portrait-reference-941x1672.png"),
    "Početni": ("13-3", "home-source-accent-rays-941x1672.png"),
    "Igra 4×4": ("13-4", "golden-state-game-941x1672.png"),
    "Pobjeda": ("14-5", "golden-state-win-941x1672.png"),
    "Dnevni izazov": ("15-6", "daily-large-safe-primary-rays-941x1672.png"),
    "Misije": ("16-7", "missions-six-real-progress-941x1672.png"),
    "Statistika": ("17-8", "golden-state-stats-941x1672.png"),
    "Teme": ("18-9", "themes-peach-style-swatches-941x1672.png"),
    "Postavke": ("19-10", "reference-settings-polished-941x1672.png"),
}

def screenshot(archive, name):
    matched = [item for item in archive.namelist() if item.endswith("/" + name)]
    if len(matched) != 1:
        raise ValueError(f"Expected one real screenshot for {name}; found {len(matched)}")
    with archive.open(matched[0]) as stream:
        with Image.open(stream) as image:
            return image.convert("RGB")

def rgb_mae(reference, actual):
    if reference.size != actual.size:
        raise ValueError(f"Image sizes differ: {reference.size} / {actual.size}; refusing resize")
    return sum(ImageStat.Stat(ImageChops.difference(reference, actual)).mean) / 3

def main():
    parser = argparse.ArgumentParser(description="Brojko 941x1672 real-emulator pixel QA")
    parser.add_argument("--reference-dir", type=Path, required=True)
    parser.add_argument("--artifact", type=Path, required=True)
    parser.add_argument("--baseline-artifact", type=Path)
    parser.add_argument("--reference-copy-index", default="7")
    parser.add_argument("--markdown", type=Path)
    args = parser.parse_args()
    rows = [
        "# Brojko — pixel fidelity audit",
        "",
        "RGB MAE: mean absolute difference of RGB channels per pixel (lower is closer).",
        "The comparison never rescales images and is not proof of total 1:1 fidelity.",
        "Real data, timestamps and the source mockup's 3D lighting can differ.",
        "",
        "| Zaslon | Trenutno | Prije | Promjena |",
        "|---|---:|---:|---:|",
    ]
    with ZipFile(args.artifact) as latest:
        if args.baseline_artifact:
            previous = ZipFile(args.baseline_artifact)
        else:
            previous = None
        try:
            for label, (token, name) in SCREENS.items():
                found = sorted(args.reference_dir.glob(
                    f"Slika ChatGPT*{token}({args.reference_copy_index}).png"))
                if len(found) != 1:
                    raise ValueError(f"Expected one reference for {token}; found {len(found)}")
                with Image.open(found[0]) as image:
                    reference = image.convert("RGB")
                    current = rgb_mae(reference, screenshot(latest, name))
                    baseline = rgb_mae(reference, screenshot(previous, name)) if previous else None
                before = f"{baseline:.3f}" if baseline is not None else "—"
                delta = f"{current-baseline:+.3f}" if baseline is not None else "—"
                rows.append(f"| {label} | {current:.3f} | {before} | {delta} |")
        finally:
            if previous:
                previous.close()
    report = "\n".join(rows) + "\n"
    if args.markdown:
        args.markdown.parent.mkdir(parents=True, exist_ok=True)
        args.markdown.write_text(report, encoding="utf-8")
    print(report)

if __name__ == "__main__":
    main()
