#!/usr/bin/env python3
"""Validate Brojko's official Croatian Google Play README badge without network access."""
from pathlib import Path
import re

README = Path(__file__).resolve().parents[1] / "README.md"
PLAY_URL = "https://play.google.com/store/apps/details?id=com.brendigo.brojko"
BADGE_URL = (
    "https://play.google.com/intl/en_us/badges/static/images/badges/"
    "hr_badge_web_generic.png"
)
text = README.read_text(encoding="utf-8")

# Both the prominent HTML button and the Markdown download badge must have
# the unmodified Google badge image linking to the same package listing.
html = re.search(
    r'<a\s+href="' + re.escape(PLAY_URL) +
    r'"[^>]*>\s*<img\s+src="' + re.escape(BADGE_URL) + r'"[^>]*>\s*</a>',
    text,
    re.DOTALL,
)
markdown = f"[![Preuzmite na Google Play]({BADGE_URL})]({PLAY_URL})"
assert html is not None, "README header Google Play image must link to Brojko"
assert markdown in text, "README download Google Play badge must link to Brojko"
assert 'alt="Preuzmite na Google Play"' in html.group(), "Google Play badge needs alt text"
print("OK: both Croatian Google Play badges link to com.brendigo.brojko")
