import importlib.util
from pathlib import Path
import struct
import tempfile
import unittest

path=Path(__file__).resolve().parents[1]/"scripts/stage-localized-store-assets.py"
spec=importlib.util.spec_from_file_location("assets",path)
assets=importlib.util.module_from_spec(spec)
spec.loader.exec_module(assets)

class LocalizedStoreAssetsTest(unittest.TestCase):
    def test_committed_metadata_matches_locale_config(self):
        assets.validate_metadata()

    def test_dimensions_and_missing_capture(self):
        with tempfile.TemporaryDirectory() as temp:
            png=Path(temp)/"capture.png"
            png.write_bytes(b"\x89PNG\r\n\x1a\n"+struct.pack(">I",13)+b"IHDR"+
                struct.pack(">II",941,1672)+b"\0"*1100)
            self.assertEqual((941,1672),assets.png_dimensions(png))
            assets.validate_capture(png,(941,1672))
            with self.assertRaises(AssertionError):
                assets.validate_capture(png,(360,800))
            with self.assertRaises(AssertionError):
                assets.validate_capture(Path(temp)/"missing.png",(941,1672))

if __name__=="__main__":
    unittest.main()
