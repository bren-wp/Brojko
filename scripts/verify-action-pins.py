#!/usr/bin/env python3
"""Reject movable GitHub Actions tags in every BROJKO workflow.

Version comments may follow the immutable 40-character commit SHA.
Review upstream changelogs and update pins deliberately when upgrading.
"""
from pathlib import Path
import re
import sys

USE = re.compile(r"^\s*(?:-\s*)?uses:\s*([^\s#]+)")
SHA = re.compile(r"^[0-9a-f]{40}$")


def check_workflows(root: Path):
    workflow_dir = root / ".github" / "workflows"
    issues = []
    inspected = 0
    workflows = sorted(workflow_dir.glob("*.yml")) + sorted(workflow_dir.glob("*.yaml"))
    if not workflows:
        return 0, ["No GitHub Actions workflows found"]
    for path in workflows:
        for lineno, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            match = USE.match(line)
            if not match:
                continue
            action = match.group(1)
            inspected += 1
            if action.startswith("./"):
                continue  # Repo-local action, versioned with this same commit.
            if action.startswith("docker://"):
                if not re.search(r"@sha256:[0-9a-f]{64}$", action):
                    issues.append(f"{path.name}:{lineno}: container action is not digest pinned")
                continue
            repository, sep, revision = action.rpartition("@")
            if not sep or not SHA.fullmatch(revision) or "/" not in repository:
                issues.append(f"{path.name}:{lineno}: unpinned action {action}")
    if inspected == 0:
        issues.append("No uses: statements were inspected")
    return inspected, issues


def main():
    count, issues = check_workflows(Path(__file__).resolve().parents[1])
    for issue in issues:
        print("::error::" + issue, file=sys.stderr)
    if issues:
        return 1
    print(f"Checked {count} pinned or local Actions references across workflows.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
