import importlib.util
from pathlib import Path
import struct
from PIL import Image
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
            Image.new("RGB",(941,1672),(20,50,100)).save(png)
            self.assertEqual((941,1672),assets.png_dimensions(png))
            assets.validate_capture(png,(941,1672))
            self.assertEqual(64,len(assets.digest(png)))
            self.assertEqual(assets.digest(png),assets.capture_record(png,"capture.png",(941,1672))["sha256"])
            with self.assertRaises(AssertionError):
                assets.validate_capture(png,(360,800))
            with self.assertRaises(AssertionError):
                assets.validate_capture(Path(temp)/"missing.png",(941,1672))

    def test_header_only_and_corrupt_png_are_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            png=Path(temp)/"capture.png"
            png.write_bytes(b"\x89PNG\r\n\x1a\n"+struct.pack(">I",13)+b"IHDR"+
                struct.pack(">II",941,1672)+b"\0"*1100)
            self.assertEqual((941,1672),assets.png_dimensions(png))
            with self.assertRaises((OSError,SyntaxError,AssertionError,ValueError)):
                assets.validate_capture(png,(941,1672))
            Image.new("RGB",(941,1672),(20,50,100)).save(png)
            data=png.read_bytes()
            png.write_bytes(data[:len(data)//2])
            with self.assertRaises((OSError,SyntaxError,AssertionError,ValueError)):
                assets.validate_capture(png,(941,1672))

    def test_english_gallery_rejects_incomplete_inventory(self):
        with tempfile.TemporaryDirectory() as temp:
            with self.assertRaisesRegex(ValueError,"Incomplete English"):
                assets.publish_english_gallery(Path(temp),Path(temp),[])

    def test_english_gallery_copies_exact_verified_bytes(self):
        with tempfile.TemporaryDirectory() as temp:
            root=Path(temp)
            captures=root/"captures"
            records=[]
            for page in assets.PAGES:
                relative=f"en/941x1672/{page}.png"
                path=captures/relative
                path.parent.mkdir(parents=True,exist_ok=True)
                path.write_bytes(page.encode())
                records.append({"path":relative,"sha256":assets.digest(path)})
            assets.publish_english_gallery(captures,root/"output",records)
            for page in assets.PAGES:
                self.assertEqual(page.encode(),(root/"output"/"english-gallery"/("brojko-en-"+page+".png")).read_bytes())
            (captures/"en/941x1672/home.png").write_bytes(b"changed")
            with self.assertRaisesRegex(ValueError,"changed"):
                assets.publish_english_gallery(captures,root/"output",records)

if __name__=="__main__":
    unittest.main()
