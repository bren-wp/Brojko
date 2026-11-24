#!/usr/bin/env python3
"""Merge actual resolved Gradle plugin modules into the build-only CycloneDX BOM."""
import argparse
import json
from pathlib import Path
from urllib.parse import quote

OLD = {
    ("org.bouncycastle", n, "1.80.2")
    for n in ("bcprov-jdk18on", "bcpkix-jdk18on", "bcutil-jdk18on")
} | {
    ("com.google.guava", "guava", v)
    for v in ("33.3.1-jre", "33.4.0-jre", "33.7.1-jre")
} | {
    ("org.apache.httpcomponents", "httpclient", "4.5.6"),
    ("org.apache.httpcomponents", "httpmime", "4.5.6"),
    ("org.apache.commons", "commons-lang3", "3.16.0"),
    ("org.jdom", "jdom2", "2.0.6"),
    ("org.bitbucket.b_c", "jose4j", "0.9.5"),
    ("com.fasterxml.jackson.core", "jackson-core", "2.22.2"),
    ("com.fasterxml.jackson.core", "jackson-databind", "2.22.2"),
}

def coordinate(value):
    if not isinstance(value, list) or len(value) != 3 or not all(
            isinstance(part, str) and part for part in value):
        raise ValueError("Invalid plugin module coordinate")
    return tuple(value)

def merge(bom, graph):
    if bom.get("bomFormat") != "CycloneDX" or graph.get("schema") != 1:
        raise ValueError("Invalid input format")
    components = bom["components"]
    root = bom["metadata"]["component"]["bom-ref"]
    refs = {(c.get("group", ""), c["name"], c.get("version", "")): c["bom-ref"]
            for c in components}
    nodes = {}
    for item in graph["components"]:
        key = coordinate(item["coordinate"])
        if key in nodes:
            raise ValueError("Duplicate plugin module: " + repr(key))
        nodes[key] = {coordinate(dep) for dep in item["dependencies"]}
    roots = {coordinate(dep) for dep in graph["roots"]}
    if not nodes or not roots or not roots <= nodes.keys():
        raise ValueError("Empty or incomplete plugin classpath graph")
    if any(not deps <= nodes.keys() for deps in nodes.values()):
        raise ValueError("Plugin graph references unresolved modules")
    if ("com.android.tools.build", "gradle", "9.5.2") not in nodes:
        raise ValueError("Plugin graph missing resolved AGP")
    vulnerable = OLD & (set(refs) | set(nodes))
    if vulnerable:
        raise ValueError("Known unpatched build coordinates: " + repr(sorted(vulnerable)))
    for key in sorted(nodes):
        if key not in refs:
            group, name, version = key
            purl = "pkg:maven/" + quote(group, safe="") + "/" + quote(name, safe="") + "@" + quote(version, safe="")
            refs[key] = purl
            components.append({"type": "library", "group": group, "name": name,
                               "version": version, "purl": purl, "bom-ref": purl,
                               "properties": [{"name": "brojko:scope", "value": "gradle-plugin-classpath"}]})
    edges = {}
    for edge in bom["dependencies"]:
        edges.setdefault(edge["ref"], set()).update(edge.get("dependsOn", []))
    edges.setdefault(root, set()).update(refs[key] for key in roots)
    for key, deps in nodes.items():
        edges.setdefault(refs[key], set()).update(refs[dep] for dep in deps)
    bom["dependencies"] = [{"ref": ref, "dependsOn": sorted(deps)}
                           for ref, deps in sorted(edges.items())]
    bom["metadata"].setdefault("properties", []).append(
        {"name": "brojko:plugin-classpath-components", "value": str(len(nodes))})
    return bom

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("build_bom", type=Path)
    p.add_argument("graph", type=Path)
    args = p.parse_args()
    bom = json.loads(args.build_bom.read_text(encoding="utf-8"))
    graph = json.loads(args.graph.read_text(encoding="utf-8"))
    result = merge(bom, graph)
    args.build_bom.write_text(json.dumps(result, indent=2)+"\n", encoding="utf-8")
    print(f"Build SBOM includes {len(graph['components'])} resolved plugin classpath modules.")

if __name__ == "__main__":
    main()
