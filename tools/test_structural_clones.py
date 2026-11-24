import importlib.util
from pathlib import Path
import unittest

path = Path(__file__).resolve().parents[1] / "scripts/scan-structural-clones.py"
spec = importlib.util.spec_from_file_location("clones", path)
clones = importlib.util.module_from_spec(spec)
spec.loader.exec_module(clones)

class StructuralCloneTest(unittest.TestCase):
    def test_renamed_identifiers_and_literals(self):
        a = 'void one(){ int a = read(1); save(a); log("first"); return; }'
        b = 'void two(){ int b = fetch(2); store(b); print("second"); return; }'
        self.assertEqual(
            [v for v, _ in clones.tokens(a)],
            [v for v, _ in clones.tokens(b)],
        )
        self.assertTrue(clones.candidates({"A.java": a, "B.java": b}, window=24, stride=1))

    def test_comments_not_tokenized(self):
        self.assertEqual(
            [v for v, _ in clones.tokens('save(1); // private fake()') ],
            [v for v, _ in clones.tokens('save(1);')],
        )

    def test_one_snippet_not_clone(self):
        self.assertEqual([], clones.candidates({"A.java": "void a(){ save(1); }"}))

if __name__ == "__main__":
    unittest.main()
