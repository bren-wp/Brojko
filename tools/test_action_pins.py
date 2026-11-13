"""Regression tests for immutable Actions references, no GitHub API needed."""
import importlib.util
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / "scripts" / "verify-action-pins.py"
spec = importlib.util.spec_from_file_location("brojko_action_pins", SCRIPT)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ActionPinTests(unittest.TestCase):
    def check(self, workflow):
        with TemporaryDirectory() as tmp:
            root = Path(tmp)
            folder = root / ".github" / "workflows"
            folder.mkdir(parents=True)
            (folder / "test.yml").write_text(workflow, encoding="utf-8")
            return module.check_workflows(root)

    def test_accepts_immutable_sha_with_version_comment(self):
        count, issues = self.check(
            "steps:\n  - uses: actions/checkout@" + ("a" * 40) + " # v7\n"
        )
        self.assertEqual(1, count)
        self.assertEqual([], issues)

    def test_rejects_mutable_version_tag(self):
        _, issues = self.check("steps:\n  - uses: actions/checkout@v7\n")
        self.assertTrue(any("unpinned" in issue for issue in issues))

    def test_allows_local_action(self):
        count, issues = self.check("steps:\n  - uses: ./.github/actions/build\n")
        self.assertEqual((1, []), (count, issues))

    def test_docker_requires_digest(self):
        _, issues = self.check("steps:\n  - uses: docker://alpine:latest\n")
        self.assertTrue(any("digest pinned" in issue for issue in issues))


if __name__ == "__main__":
    unittest.main()
