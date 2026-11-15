"""Native owner-vs-emulator Victory ROI audit tests.

Synthetic PNGs only: private owner references must never enter the repo.
"""
import contextlib
import importlib.util
import io
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
from zipfile import ZipFile

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location(
    "brojko_measure_owner_pixels", ROOT / "scripts/measure-owner-pixels.py"
)
audit = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(audit)


class OwnerVictoryPixelAuditTest(unittest.TestCase):
    def run_audit(self, reference_size=(941, 1672)):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            references = directory / "references"
            references.mkdir()
            reference = references / "Slika ChatGPT 24. lis 2026. 22_15_14-5(20261030-210605).png"
            Image.new("RGB", reference_size, (255, 248, 239)).save(reference)
            screenshot = Image.new("RGB", (941, 1672), (255, 248, 239))
            # A nonidentical central crown pixel must affect only measured ROIs.
            screenshot.putpixel((400, 400), (254, 195, 75))
            image = directory / "candidate.png"
            screenshot.save(image)
            archive = directory / "emulator.zip"
            with ZipFile(archive, "w") as zip_file:
                zip_file.write(
                    image,
                    "android-screenshots/screenshots/golden-state-win-941x1672.png",
                )
            report = directory / "metrics.json"
            args = [
                "measure-owner-pixels.py", "--references", str(references),
                "--capture-zip", str(archive), "--screen", "5",
                "--report", str(report),
            ]
            with patch.object(sys, "argv", args), contextlib.redirect_stdout(io.StringIO()):
                if reference_size == (941, 1672):
                    audit.main()
                    return json.loads(report.read_text(encoding="utf-8"))
                with self.assertRaisesRegex(ValueError, "Wrong reference size"):
                    audit.main()

    def test_victory_only_requires_just_victory_owner_reference(self):
        report = self.run_audit()
        self.assertEqual(report["screen_filter"], 5)
        self.assertEqual(len(report["screens"]), 1)
        entry = report["screens"][0]
        self.assertEqual(entry["screen"], "Pobjeda")
        self.assertGreater(entry["regions"]["3D kruna"]["rgb_mae_0_to_255"], 0)
        self.assertLess(entry["regions"]["3D kruna"]["exact_rgb_pixels_percent"], 100)
        self.assertEqual(entry["regions"]["Zlatne zrake krune"]["rgb_mae_0_to_255"], 0)
        self.assertEqual(entry["regions"]["Tri aktivne akcije"]["exact_rgb_pixels_percent"], 100)

    def test_reference_must_be_original_native_size(self):
        self.run_audit(reference_size=(940, 1672))


if __name__ == "__main__":
    unittest.main()
