# Global locale support and verification

English is the explicit default. Nine locales are implemented: en, hr, de, fr,
ar, es, he, it, pt-BR. Each has the same 247 string keys and two plural resources.
Arabic and Hebrew use RTL Canvas navigation and mirrored hit/accessibility
bounds. The puzzle's physical direction and wordmark remain intrinsic artwork.
Mixed scripts use Android StaticLayout and system-font fallback when necessary.

Android 24–32 uses a separate private language preference; Android 13+ uses the
platform LocaleManager. Search and pagination are functional. Legacy backup
data stays compatible; new history dates are invariant ISO values. Display
only strips imported control/format characters from legacy history labels.

Existing owner screenshot assertions stay Croatian. Separate locale-matrix
instrumentation and capture workflows exercise supported languages across
platform boundaries. The captured PNGs are actual production Canvas output,
not mockups or resized reference screenshots. Their presence alone does not
certify visual equivalence, translation accuracy or all-device verification.
Remaining 21 languages are not exposed yet. Full audit and native sign-off
remain release gates.
