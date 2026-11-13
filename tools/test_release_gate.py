"""Release/signing policy regression tests; no secrets or Android SDK needed."""
from pathlib import Path
from tempfile import TemporaryDirectory
import os
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[1]
RELEASE = ROOT / ".github/workflows/android-release.yml"
UI = ROOT / ".github/workflows/android-ui-qa.yml"
CHECKSUMS = ROOT / "scripts/generate-checksums.sh"


def signing_script():
    lines = RELEASE.read_text(encoding="utf-8").splitlines()
    step = next(i for i, line in enumerate(lines)
                if line.strip() == "- name: Inspect production signing configuration")
    start = next(i for i in range(step + 1, len(lines))
                 if lines[i] == "        run: |") + 1
    collected = []
    for line in lines[start:]:
        if line and not line.startswith("          "):
            break
        collected.append(line[10:] if line else "")
    return "\n".join(collected) + "\n"


def execute_gate(event, ref, secrets=None):
    with TemporaryDirectory() as temp:
        folder = Path(temp)
        output = folder / "output"
        summary = folder / "summary"
        env = {
            "PATH": os.environ.get("PATH", ""),
            "HOME": str(folder),
            "GITHUB_OUTPUT": str(output),
            "GITHUB_STEP_SUMMARY": str(summary),
            "RUNNER_TEMP": str(folder),
            "RELEASE_EVENT": event,
            "RELEASE_REF": ref,
            "STORE": "",
            "STORE_PASSWORD": "",
            "KEY_ALIAS": "",
            "KEY_PASSWORD": "",
        }
        env.update(secrets or {})
        result = subprocess.run(["bash", "-e"], input=signing_script(),
                                text=True, capture_output=True, env=env)
        return result, output.read_text() if output.exists() else "", (
            summary.read_text() if summary.exists() else "")


class ReleaseGateTest(unittest.TestCase):
    def test_shell_syntax(self):
        self.assertEqual(0, subprocess.run(
            ["bash", "-n"], input=signing_script(),
            text=True, capture_output=True).returncode)

    def assert_unsigned_release_allowed(self, event, ref):
        run, output, summary = execute_gate(event, ref)
        self.assertEqual(0, run.returncode, run.stderr)
        self.assertIn("enabled=false", output)
        self.assertIn("unsigned", run.stdout.lower())
        self.assertIn("prerelease", summary)

    def test_main_without_secrets_publishes_unsigned(self):
        self.assert_unsigned_release_allowed("push", "refs/heads/main")

    def test_tag_without_secrets_publishes_unsigned(self):
        self.assert_unsigned_release_allowed("push", "refs/tags/v1.12.999")

    def test_manual_without_secrets_publishes_unsigned(self):
        self.assert_unsigned_release_allowed("workflow_dispatch", "refs/heads/main")

    def test_partially_configured_signing_fails_closed(self):
        for key in ("STORE", "STORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD"):
            with self.subTest(key=key):
                run, output, _ = execute_gate(
                    "push", "refs/heads/main", {key: "test-value"})
                self.assertNotEqual(0, run.returncode)
                self.assertNotIn("enabled=true", output)
                self.assertIn("Partial", run.stdout)

    def test_release_assets_and_policy(self):
        release = RELEASE.read_text(encoding="utf-8")
        for step in (
            "Verify signed release APK and signed Android App Bundle",
            "Generate verified portable SHA256 checksums",
            "Upload signed build artifacts",
            "Publish signed GitHub release",
        ):
            self.assertIn("- name: " + step +
                          "\n        if: steps.signing.outputs.enabled == 'true'", release)
        for step in (
            "Verify unsigned packages and debug-test APK",
            "Generate checksums for unsigned preview packages",
            "Upload unsigned APK, unsigned AAB, test APK and QA evidence",
            "Publish unsigned GitHub preview release",
        ):
            self.assertIn("- name: " + step +
                          "\n        if: steps.signing.outputs.enabled == 'false'", release)
        self.assertIn("prerelease: true", release)
        self.assertIn("prerelease: false", release)
        self.assertIn("fail_on_unmatched_files: true", release)
        self.assertIn("app-debug.apk", release)
        self.assertIn("app-release-unsigned.apk", release)
        self.assertIn("app-release.aab", release)
        self.assertIn("scripts/generate-checksums.sh --unsigned-release", release)
        self.assertIn("28", release)

    def test_unsigned_checksum_manifest_only_contains_published_files(self):
        files = (
            "app/build/outputs/apk/debug/app-debug.apk",
            "app/build/outputs/apk/release/app-release-unsigned.apk",
            "app/build/outputs/bundle/release/app-release.aab",
        )
        with TemporaryDirectory() as temp:
            folder = Path(temp)
            for path in files:
                target = folder / path
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(b"Brojko test fixture: " + path.encode())
            result = subprocess.run(
                ["bash", str(CHECKSUMS), "--unsigned-release"],
                cwd=folder, text=True, capture_output=True)
            self.assertEqual(0, result.returncode, result.stderr)
            manifest = (folder / "SHA256SUMS").read_text()
            names = [line.split("  ", 1)[1] for line in manifest.splitlines()]
            self.assertEqual(["app-debug.apk", "app-release-unsigned.apk",
                              "app-release.aab"], names)
            (folder / files[1]).unlink()
            missing = subprocess.run(
                ["bash", str(CHECKSUMS), "--unsigned-release"],
                cwd=folder, text=True, capture_output=True)
            self.assertNotEqual(0, missing.returncode)

    def test_gallery_accepts_signed_or_unsigned_release(self):
        workflow = UI.read_text(encoding="utf-8")
        for artifact in ("app-release.apk", "app-release-unsigned.apk",
                         "app-debug.apk", "app-release.aab", "SHA256SUMS"):
            self.assertIn(artifact, workflow)
        self.assertIn("steps.verified_release.outputs.available == 'true'", workflow)
        self.assertIn("gh release view", workflow)


if __name__ == "__main__":
    unittest.main()
