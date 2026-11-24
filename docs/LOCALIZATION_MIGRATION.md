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

## Targeted copy review

PR #160 corrects selected backup, no-cloud, daily reward and undo mission messages in English, Croatian, German and French. This is a targeted linguistic pass; no native-speaker sign-off or complete proofreading of 30 languages is claimed. The existing Croatian artwork and all format placeholders are preserved.

## English-first release evidence

The production default remains English, regardless of device locale when no app language is saved. The new instrumentation test clears the dedicated locale preferences and API 33+ platform app locale, launches the real Activity, checks English navigation, mode labels, reminder text, LTR layout and recreation, then restores the Croatian test runner state. The release UI workflow captures 23 separate English states from the real production Canvas at 941×1672, decodes and hashes the PNGs, and publishes them separately from the unchanged 28 Croatian owner-reference gallery images. This preserves the original screenshot regression baseline and avoids pretending translated text is RGB-identical. Fresh CI and release publication are required.

## Shared locale test APKs

Run 45509800848 had two verified failures from Maven Central HTTP 429 while resolving httpmime 4.5.14, which disabled the repository and prevented Guava and other modules resolving. The next revision builds debug/test APKs once, publishes a checkout-SHA and SHA256-verified pair, and reuses the identical bytes in all 13 API jobs and six capture jobs. It limits emulator matrix concurrency and retries only the observed Maven resolution rate limit, at most twice. No test cases or locale/viewport coverage are removed. Fresh CI required.

## Targeted 30-locale terminology review

Reviewed the two backup/synchronization labels across all 30 locale inventories. English is primary. Replace misleading progress-sync wording with saving/backing up progress, and clarify that cloud sync is absent, not merely disabled. German already used appropriate wording and is unchanged. 29 locale XML files changed; resource keys, format placeholders, artwork, touch geometry and layout are unchanged. Croatian text deliberately changes to accurately describe the feature; fresh screenshot review is required. This is a targeted terminology review, not full linguistic sign-off.
