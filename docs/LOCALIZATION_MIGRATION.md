# Global locale support and verification

English is the explicit default. All thirty requested locales have implemented resource inventories: en, hr, de, fr,
ar, es, he, it, pt-BR, sl, pl, cs, ja, ko, zh-Hans, sk, nl, uk, sv, da, nb, fi, hu, ro, bg, el, tr, hi, id, vi. Each has the same 247 string keys and two plural resources.
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
All requested locales are exposed in the picker; linguistic review and native QA remain required. Full audit and native sign-off
remain release gates.

## Final QA expansion
PR validation now covers API 24–36 individually. Capture shards cover five locales at all four requested viewports per job, producing 2,520 native captures across all shards. Compact detail actions and RTL help equation corrected after visual inspection of real captures. Native QA still required for the new commit.

Real capture review found icon/label collisions on localized CTA buttons; the new shared logical interval preserves icon clearance in both directions. Attached-View tests exercise mirrored accessibility hit targets and physical left swipes in both Arabic and Hebrew. Capture inventory now also includes the actual Relaxed and Daily gameplay modes (23 states per language/viewport, 2,760 captures total), with deterministic test-only history and game-result fixtures. These changes require a fresh CI run and review, not an assertion of pixel identity.

## Capture evidence hardening

All six capture jobs in [PR #159 final locale run](https://github.com/bren-wp/Brojko/actions/runs/45502544028) reported 460 native images and 40 staged Play images each (2,760 and 240 in total). The older validation checked headers/dimensions, not full PNG decoding. PR #160 adds Pillow verify + decode and SHA-256 manifests for each shard, with checked-out Git SHA and Actions run identifiers. New evidence is pending CI. This is not owner-reference RGB certification or native-speaker proofreading.
