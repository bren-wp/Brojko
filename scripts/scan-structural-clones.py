#!/usr/bin/env python3
"""Report token-normalized Java clone candidates, not proven semantic clones."""
from collections import defaultdict
from pathlib import Path
import re

TOKEN = re.compile(
    r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|'
    r"'(?:\\.|[^'\\])*'|[A-Za-z_$][\w$]*|"
    r'0[xX][0-9a-fA-F]+|\d+(?:\.\d+)?[fFdDlL]?|[^\s]'
)
KEYWORDS = set(("if else for while do switch case default return throw try catch "
                "finally new null true false this super break continue class enum "
                "interface static final public private protected void int long float "
                "double boolean byte char short").split())

def tokens(source):
    result = []
    line = 1
    end = 0
    for match in TOKEN.finditer(source):
        line += source.count("\n", end, match.start())
        value = match.group()
        if not value.startswith(("//", "/*")):
            if value.startswith(('"', "'")):
                normalized = "LITERAL"
            elif value[0].isdigit():
                normalized = "NUMBER"
            elif re.fullmatch(r"[A-Za-z_$][\w$]*", value) and value not in KEYWORDS:
                normalized = "IDENT"
            else:
                normalized = value
            result.append((normalized, line))
        line += value.count("\n")
        end = match.end()
    return result

def candidates(sources, window=64, stride=8):
    if window < 16 or stride < 1:
        raise ValueError("invalid clone window or stride")
    buckets = defaultdict(list)
    for path, source in sorted(sources.items()):
        stream = tokens(source)
        for start in range(0, len(stream)-window+1, stride):
            chunk = stream[start:start+window]
            key = tuple(value for value, _ in chunk)
            # Exclude most data arrays and one-expression snippets.
            if key.count(";") < 2 or key.count("(") < 2:
                continue
            locations = buckets[key]
            # Adjacent overlapping windows are not independent copies.
            if locations and locations[-1][0] == path and start < locations[-1][3]+window:
                continue
            locations.append((path, chunk[0][1], chunk[-1][1], start))
    return sorted(
        (locations for locations in buckets.values() if len(locations) > 1),
        key=lambda group: (-len(group), group[0])
    )

def main():
    root = Path(__file__).resolve().parents[1]
    sources = {
        str(path.relative_to(root)): path.read_text(encoding="utf-8")
        for path in (root / "app/src/main/java").rglob("*.java")
    }
    groups = candidates(sources)
    print(f"Structural clone candidates: {len(groups)} groups across {len(sources)} files.")
    print("Token windows are heuristic; overlapping groups and false positives are possible.")
    for group in groups[:40]:
        print("REVIEW:", ", ".join(f"{path}:{first}-{last}" for path, first, last, _ in group[:8]))
    print("Confirm control/data flow, lifecycle and behavior before refactoring.")

if __name__ == "__main__":
    main()
