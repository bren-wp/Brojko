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
    "Početni": [("Logotip", (100, 175, 840, 470)),
                 ("Kartice rezultata", (30, 450, 908, 680)),
                 ("Glavni gumb", (55, 650, 880, 920)),
                 ("Načini igre", (30, 900, 900, 1325)),
                 ("Donji prečaci", (25, 1300, 910, 1625))],
    "Igra 4×4": [("Naslov i zrake", (30, 70, 500, 350)),
                 ("HUD rezultata", (450, 80, 940, 350)),
                 ("Okvir ploče", (20, 350, 920, 1320)),
                 ("Pločice 4×4", (38, 395, 910, 1288)),
                 ("Donje kontrole", (35, 1330, 925, 1605))],
    "Pobjeda": [("Rezultat i akcije", (135, 450, 805, 1415))],
    "Dnevni izazov": [("Naslov, niz i datum", (0, 0, 941, 375)),
                       ("Ploča 4x4", (150, 365, 790, 955)),
                       ("Ciljevi", (45, 955, 895, 1325)),
                       ("Kartica nagrade", (35, 1320, 900, 1550)),
                       ("Gumb", (25, 1500, 920, 1670))],
    "Misije": [("Zaglavlje", (0, 0, 941, 315)),
               ("Dnevni zadaci", (25, 298, 915, 760)),
               ("Tjedni zadaci", (25, 758, 915, 1235)),
               ("Postignuca", (25, 1218, 915, 1645)),
               ("Ilustracije", (45, 375, 220, 1630)),
               ("Kartice nagrada", (730, 360, 915, 1625)),
               ("Tekst i napredak", (225, 350, 735, 1620))],
    "Statistika": [("Zaglavlje", (20, 30, 920, 306)),
                  ("Tri KPI kartice", (20, 305, 923, 540)),
                  ("Dva KPI sazetka", (20, 535, 920, 760)),
                  ("Grafikon", (22, 765, 922, 1160)),
                  ("Povijest partija", (20, 1160, 920, 1645)),
                  ("Naslovi i stupci", (65, 790, 885, 1115))],
    "Teme": [("Kartice tema", (40, 380, 900, 800)),
             ("Pretpregled pločica", (55, 940, 900, 1385))],
    "Postavke": [("Naslov i koraljne zrake", (110, 48, 820, 210)),
                 ("Prva cetiri retka", (30, 220, 900, 820)),
                 ("Sigurnosna kopija", (32, 828, 905, 998)),
                 ("Privatnost i informacije", (30, 1000, 905, 1300)),
                 ("Sve ikonice", (55, 220, 185, 1300)),
                 ("Tri sklopke", (710, 225, 875, 810)),
                 ("Donji logo i byline", (220, 1330, 720, 1530)),
                 ("Zavrsna krem kartica", (30, 1310, 905, 1645))],
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
    parser.add_argument("--reference-copy-index",
                        help="Optional exact suffix for multiple copies, e.g. 9")
    parser.add_argument("--markdown", type=Path)
    parser.add_argument("--include-regions", action="store_true",
                        help="Also report important real-pixel subregions per screen")
    parser.add_argument("--screen", choices=sorted(SCREENS),
                        help="Audit just one requested screen during pixel-by-pixel work")
    parser.add_argument("--controlled-home", action="store_true",
                        help="Compare Home against the instrumented source-data fixture, not a live session")
    parser.add_argument("--controlled-daily", action="store_true",
                        help="Compare Daily against instrumented seven-day streak fixture")
    parser.add_argument("--controlled-settings", action="store_true",
                        help="Compare Settings with ON switches in test-only fixture")
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
            for label, (token, default_name) in SCREENS.items():
                if args.screen and label != args.screen:
                    continue
                name = ("home-controlled-reference-941x1672.png"
                        if args.controlled_home and label == "Početni"
                        else "daily-source-streak7-941x1672.png"
                        if args.controlled_daily and label == "Dnevni izazov"
                        else "settings-phase08-source-on-941x1672.png"
                        if args.controlled_settings and label == "Postavke"
                        else default_name)
                pattern = (f"Slika ChatGPT*{token}({args.reference_copy_index}).png"
                           if args.reference_copy_index is not None
                           else f"Slika ChatGPT*{token}*.png")
                found = sorted(args.reference_dir.glob(pattern))
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
