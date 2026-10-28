#!/usr/bin/env python3
"""Verify completed instrumentation and actual full-resolution Android captures.

Run this as ONE command from android-emulator-runner's script, which executes
script lines individually rather than as a persistent multiline shell.
Uses only Python's standard library, and is not packaged with the Android app.
"""
from pathlib import Path
import re
import struct
import sys

ROOT = Path("android-screenshots/screenshots")
TEST_LOG = Path("instrumentation-results.txt")
CAPTURES = (
    "splash-full-width-portrait-reference-941x1672.png",
    "home-source-accent-rays-941x1672.png",
    "golden-state-game-941x1672.png",
    "golden-state-win-941x1672.png",
    "daily-large-safe-primary-rays-941x1672.png",
    "missions-six-real-progress-941x1672.png",
    "golden-state-stats-941x1672.png",
    "themes-peach-style-swatches-941x1672.png",
    "reference-settings-polished-941x1672.png",
)
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
COMPACT_CAPTURES = (
    "compact-360x600-home.png",
    "compact-360x600-game.png",
    "compact-360x600-daily.png",
    "compact-360x600-missions.png",
    "compact-360x600-stats.png",
    "compact-360x600-themes.png",
    "compact-360x600-settings.png",
)
MINIMUM_TOTAL_CAPTURES = 145



def verify() -> None:
    if not TEST_LOG.is_file():
        raise ValueError("instrumentation-results.txt is missing: test did not start")
    results = TEST_LOG.read_text(encoding="utf-8", errors="replace")
    matched = re.search(r"(?m)^OK \((\d+) tests?\)\s*$", results)
    if not matched or "FAILURES!!!" in results or "INSTRUMENTATION_FAILED" in results:
        raise ValueError("Instrumented tests did not finish successfully")
    count = int(matched.group(1))
    if count < 64:
        raise ValueError(f"Only {count} tests passed; 64 is the regression baseline")
    captures = list(ROOT.glob("*.png"))
    if len(captures) < MINIMUM_TOTAL_CAPTURES:
        raise ValueError(f"Expected at least {MINIMUM_TOTAL_CAPTURES} screen captures, "
                         f"found {len(captures)}")
    for name, expected_size in (
        *((name, (941, 1672)) for name in CAPTURES),
        *((name, (360, 600)) for name in COMPACT_CAPTURES),
    ):
        path = ROOT / name
        if not path.is_file():
            raise ValueError(f"Missing real Android screenshot: {path}")
        with path.open("rb") as capture:
            header = capture.read(24)
        if len(header) != 24 or header[:8] != PNG_SIGNATURE:
            raise ValueError(f"Invalid PNG screenshot: {path}")
        if header[12:16] != b"IHDR":
            raise ValueError(f"Missing PNG IHDR: {path}")
        width, height = struct.unpack(">II", header[16:24])
        if (width, height) != expected_size:
            raise ValueError(f"Wrong screenshot size {width}x{height}: {path}")
    print(f"Android QA: {count} tests; {len(captures)} PNGs including "
          f"{len(CAPTURES)} reference and {len(COMPACT_CAPTURES)} compact screens")


if __name__ == "__main__":
    try:
        verify()
    except (OSError, ValueError) as exc:
        print(f"::error::{exc}", file=sys.stderr)
        sys.exit(1)
