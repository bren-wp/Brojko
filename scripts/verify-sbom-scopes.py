#!/usr/bin/env python3
"""Keep Android release-runtime SCA separate from build-tool SCA.

This script verifies the actual CycloneDX package references in both files.
It rejects a broad, all-configuration SBOM being mislabeled as a runtime BOM.
"""
import json
from pathlib import Path
import sys


def components(path):
    doc = json.loads(Path(path).read_text(encoding="utf-8"))
    if doc.get("bomFormat") != "CycloneDX":
        raise ValueError(f"Not a CycloneDX SBOM: {path}")
    values = doc.get("components")
    if not isinstance(values, list) or not values:
        raise ValueError(f"Missing resolved components: {path}")
    return {(x.get("group", ""), x.get("name", ""), x.get("version", ""))
            for x in values}


def main():
    if len(sys.argv) != 3:
        raise ValueError("Usage: verify-sbom-scopes.py runtime.json full-build.json")
    runtime = components(sys.argv[1])
    complete = components(sys.argv[2])
    expected = {
        ("androidx.core", "core", "1.18.0"),
        ("androidx.activity", "activity", "1.13.0"),
        ("androidx.customview", "customview", "1.2.0"),
    }
    missing = expected - runtime
    if missing:
        raise ValueError("Shipped Android dependencies missing from runtime SBOM: "
                         + repr(sorted(missing)))
    if any(group.startswith("com.android.tools") for group, name, ver in runtime):
        raise ValueError("Build-tool packages leaked into runtime SBOM")
    if not any(group.startswith("com.android.tools") for group, name, ver in complete):
        raise ValueError("Complete build SBOM is missing Android build tools")
    if len(complete) <= len(runtime):
        raise ValueError("Complete build SBOM must include more packages than runtime SBOM")
    print(f"CycloneDX scope verified: {len(runtime)} release-runtime packages, "
          f"{len(complete)} all-configuration packages.")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError) as error:
        print(f"::error::{error}", file=sys.stderr)
        sys.exit(1)
