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


# Subregions are source-PNG pixel coordinates, never scaled or aligned.
# Per-screen and local scores are both important: large empty backgrounds
# must not hide deteriorating controls, digits or raised tile details.
REGIONS = {
    "Splash": [("Logotip", (110, 580, 835, 1100))],
    "Početni": [("Glavni gumb", (120, 665, 820, 910))],
    "Igra 4×4": [("Ploča", (35, 365, 920, 1325)),
                 ("Pločice", (45, 410, 910, 1285))],
    "Pobjeda": [("Rezultat i akcije", (135, 450, 805, 1415))],
    "Dnevni izazov": [("Ploča", (195, 360, 755, 970))],
    "Misije": [("Kartice nagrada", (720, 345, 920, 1580))],
    "Statistika": [("KPI kartice", (30, 300, 915, 760))],
    "Teme": [("Kartice tema", (40, 380, 900, 800)),
             ("Pretpregled pločica", (55, 940, 900, 1385))],
    "Postavke": [("Retci postavki", (30, 220, 910, 1370))],
}

def screenshot(archive, name):
    matched = [item for item in archive.namelist() if item.endswith("/" + name)]
    if len(matched) != 1:
        raise ValueError(f"Expected one real screenshot for {name}; found {len(matched)}")
    with archive.open(matched[0]) as stream:
        with Image.open(stream) as image:
            return image.convert("RGB")

def rgb_mae(reference, actual, region=None):
    if reference.size != actual.size:
        raise ValueError(f"Image sizes differ: {reference.size} / {actual.size}; refusing resize")
    if region is not None:
        left, top, right, bottom = region
        if not (0 <= left < right <= reference.width and
                0 <= top < bottom <= reference.height):
            raise ValueError(f"Invalid native-resolution ROI: {region}")
        reference = reference.crop(region)
        actual = actual.crop(region)
    return sum(ImageStat.Stat(ImageChops.difference(reference, actual)).mean) / 3

def main():
    parser = argparse.ArgumentParser(description="Brojko 941x1672 real-emulator pixel QA")
    parser.add_argument("--reference-dir", type=Path, required=True)
    parser.add_argument("--artifact", type=Path, required=True)
    parser.add_argument("--baseline-artifact", type=Path)
    parser.add_argument("--reference-copy-index", default="7")
    parser.add_argument("--markdown", type=Path)
    parser.add_argument("--include-regions", action="store_true",
                        help="Also report important real-pixel subregions per screen")
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
                    current_image = screenshot(latest, name)
                    previous_image = screenshot(previous, name) if previous else None
                    current = rgb_mae(reference, current_image)
                    baseline = rgb_mae(reference, previous_image) if previous_image else None
                    before = f"{baseline:.3f}" if baseline is not None else "—"
                    delta = f"{current-baseline:+.3f}" if baseline is not None else "—"
                    rows.append(f"| {label} | {current:.3f} | {before} | {delta} |")
                    if args.include_regions:
                        for region_name, bounds in REGIONS.get(label, []):
                            local = rgb_mae(reference, current_image, bounds)
                            prev = rgb_mae(reference, previous_image, bounds) if previous_image else None
                            old = f"{prev:.3f}" if prev is not None else "—"
                            change = f"{local-prev:+.3f}" if prev is not None else "—"
                            rows.append(f"| ↳ {region_name} | {local:.3f} | {old} | {change} |")
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
