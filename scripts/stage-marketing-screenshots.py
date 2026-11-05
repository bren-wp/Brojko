#!/usr/bin/env python3
"""Stage verified, genuine Android emulator PNGs for the public Brojko gallery.

Input files are created by Android instrumentation, never by a generator or
synthetic README illustration. Called only after emulator screenshot QA passes.
"""
from pathlib import Path
import shutil
import struct

CAPTURES = {
    "brojko-splash.png": "three-screen-source-splash-941x1672.png",
    "brojko-home.png": "owner-home-12560-3864-941x1672.png",
    "brojko-classic.png": "reference-941x1672-game-classic.png",
    "brojko-relaxed.png": "relaxed-raised-gameplay-941x1672.png",
    "brojko-daily.png": "daily-refined-board-gift-941x1672.png",
    "brojko-victory.png": "owner-bravo-full-board-2048-941x1672.png",
    "brojko-missions.png": "missions-six-real-progress-941x1672.png",
    "brojko-statistics.png": "golden-state-stats-941x1672.png",
    "brojko-themes.png": "themes-original-final-aligned-941x1672.png",
    "brojko-settings.png": "settings-phase08-source-on-941x1672.png",
    "brojko-game-over.png": "game-over-raised-portrait-941x1672.png",
    "brojko-icon.png": "phase10-adaptive-icon-safe-512.png",
}
ROOT = Path("android-screenshots/screenshots")
TARGET = Path("marketing-screenshots")


def verify_png(path: Path) -> tuple[int, int]:
    if not path.is_file() or path.stat().st_size < 2048:
        raise ValueError(f"Missing or empty emulator screenshot: {path}")
    with path.open("rb") as stream:
        header = stream.read(24)
    if (
        len(header) != 24 or header[:8] != b"\x89PNG\r\n\x1a\n"
        or header[12:16] != b"IHDR"
    ):
        raise ValueError(f"Invalid real PNG format: {path}")
    width, height = struct.unpack(">II", header[16:24])
    if width < 320 or height < 320 or width > 4096 or height > 4096:
        raise ValueError(f"Unexpected emulator resolution {width}x{height}: {path}")
    return width, height


def main() -> None:
    TARGET.mkdir(exist_ok=True)
    for destination, origin in CAPTURES.items():
        source = ROOT / origin
        width, height = verify_png(source)
        if "941x1672" in origin and (width, height) != (941, 1672):
            raise ValueError(f"Wrong portrait reference geometry: {source}")
        shutil.copyfile(source, TARGET / destination)
        print(f"Real Android screenshot: {destination} ({width}x{height})")
    if len(tuple(TARGET.glob("*.png"))) != len(CAPTURES):
        raise AssertionError("Marketing gallery incomplete")


if __name__ == "__main__":
    main()
