"""Post-merge signing regression tests. No GitHub secrets or Android SDK required."""
from pathlib import Path
from tempfile import TemporaryDirectory
import os
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[1]
RELEASE = ROOT / ".github/workflows/android-release.yml"
UI = ROOT / ".github/workflows/android-ui-qa.yml"


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


class PostMergeReleaseGateTest(unittest.TestCase):
    def test_shell_is_valid(self):
        self.assertEqual(0, subprocess.run(
            ["bash", "-n"], input=signing_script(), text=True,
            capture_output=True).returncode)

    def test_main_without_secrets_is_qa_only(self):
        run, output, summary = execute_gate("push", "refs/heads/main")
        self.assertEqual(0, run.returncode, run.stderr)
        self.assertIn("enabled=false", output)
        self.assertIn("not published", summary)
        self.assertIn("no stable release", run.stdout.lower())

    def test_tag_without_secrets_fails(self):
        run, _, _ = execute_gate("push", "refs/tags/v1.12.237")
        self.assertNotEqual(0, run.returncode)
        self.assertIn("requires", run.stdout)

    def test_dispatch_without_secrets_fails(self):
        run, _, _ = execute_gate("workflow_dispatch", "refs/heads/main")
        self.assertNotEqual(0, run.returncode)

    def test_partially_configured_signing_never_publishes(self):
        run, output, _ = execute_gate("push", "refs/heads/main",
                                      {"KEY_ALIAS": "brojko-upload"})
        self.assertNotEqual(0, run.returncode)
        self.assertNotIn("enabled=true", output)

    def test_release_upload_and_checksums_restricted_to_signed_artifacts(self):
        release = RELEASE.read_text(encoding="utf-8")
        for step in (
            "Verify signed release APK and signed Android App Bundle",
            "Generate verified portable SHA256 checksums",
            "Upload signed build artifacts",
        ):
            self.assertIn("- name: " + step + "\n        if: steps.signing.outputs.enabled == 'true'", release)
        self.assertIn("if: steps.signing.outputs.enabled == 'true' && "
                      "(startsWith(github.ref, 'refs/tags/v')", release)
        self.assertIn("Brojko-postmerge-QA-UNSIGNED-NOT-FOR-DISTRIBUTION", release)

    def test_gallery_requires_existing_signed_release(self):
        workflow = UI.read_text(encoding="utf-8")
        for artifact in ("app-release.apk", "app-release.aab", "SHA256SUMS"):
            self.assertIn(artifact, workflow)
        self.assertIn("steps.signed_release.outputs.available == 'true'", workflow)
        self.assertIn("gh release view", workflow)


if __name__ == "__main__":
    unittest.main()
