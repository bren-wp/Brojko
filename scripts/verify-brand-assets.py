#!/usr/bin/env python3
"""Build-time guard for Brojko's native, non-rasterized launcher branding.

The two pre-26 launcher resources must follow the same approved vector as
the app icon, while Android 26+ foreground art retains a mask-safe inset.
No screenshot images or proprietary font/asset bundles are generated.
"""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1] / "app/src/main/res"
DRAWABLE = ROOT / "drawable"
MIPMAP = ROOT / "mipmap-anydpi"
V26 = ROOT / "mipmap-anydpi-v26"
ANDROID = "{http://schemas.android.com/apk/res/android}"
COLORS = ("#FFF8EE", "#FF7A5B", "#2EC4B6", "#3D98F0", "#FFBB4A")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def validate() -> None:
    canonical = (DRAWABLE / "ic_brojko.xml").read_bytes()
    tree = ET.fromstring(canonical)
    require(tree.tag == "vector", "Main Brojko icon must remain VectorDrawable")
    for color in COLORS:
        require(color.encode() in canonical, f"Missing canonical brand color {color}")
    require(b"#FFFDF5" in canonical, "Central white B must remain")
    for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
        source = (MIPMAP / name).read_bytes()
        require(source == canonical, f"Legacy {name} drifted from approved vector")

    foreground = ET.parse(DRAWABLE / "ic_brojko_foreground.xml").getroot()
    require(foreground.tag == "vector", "Adaptive foreground must be vector")
    require(foreground.attrib[ANDROID + "viewportWidth"] == "108",
            "Android adaptive icon must use a 108-unit viewport")
    groups = [child for child in foreground if child.tag == "group"]
    require(len(groups) == 1, "Adaptive foreground must have one root inset group")
    for prop in ("scaleX", "scaleY"):
        value = float(groups[0].attrib[ANDROID + prop])
        require(.65 <= value <= .70,
                f"Adaptive artwork safe inset unexpected: {prop}={value}")
    raw = (DRAWABLE / "ic_brojko_foreground.xml").read_text()
    for color in COLORS:
        require(color in raw, f"Missing foreground brand color {color}")
    require("#FFFDF5" in raw, "Adaptive B lost its light face")

    for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
        adaptive = ET.parse(V26 / name).getroot()
        require(adaptive.tag == "adaptive-icon",
                f"{name} must be adaptive on Android 26+")
        parts = {child.tag: child.attrib.get(ANDROID + "drawable")
                 for child in adaptive}
        require(parts.get("foreground") == "@drawable/ic_brojko_foreground",
                f"{name} lost its foreground")
        require(parts.get("monochrome") == "@drawable/ic_brojko_monochrome",
                f"{name} lost Android 13 monochrome")
    monochrome = (DRAWABLE / "ic_brojko_monochrome.xml").read_text()
    require("#FFFFFFFF" in monochrome, "Monochrome B layer is missing")
    for color in COLORS[1:]:
        require(color not in monochrome,
                "Monochrome must be theme-tinted, not branded color")
    print("Brojko launcher assets: pre-26 parity, adaptive safe zone, "
          "four colors, centered B, and monochrome valid.")


if __name__ == "__main__":
    validate()
