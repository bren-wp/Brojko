# Brojko — source-specific glyph and 3D tile alignment (2026-11-03)

## Reference scope
Only the owner's supplied **Splash**, **Home**, **Bravo!** PNGs at **941 × 1672**. The supplied fourth image is the brand sheet, not an additional app screen. No chat images or reference-image-as-app-UI screenshots are produced.

## Baseline (real Android emulator from merged PR #41)
Measured with equal-resolution PNGs, without scaling, using the existing `tools/owner_three_screen_pixel_audit.py` approach. RGB MAE is an average absolute channel difference **out of 255**, not a percent:
- Splash: **16.39** overall; logo ROI ~**30.99**.
- Home (real best 12,560 and latest 3,864 fixture): **20.97** overall; logo ROI ~**35.01**.
- Bravo (real scored game fixture 2048): **16.72** overall; headline ROI ~**41.07**.

Measurements are before this PR and are **not** claimed as a new, validated 1:1 result.

## Pixel alignment changes
- Source/renderer overlap alignment on native 941 px width shows Splash Br glyphs need ~**+13 px** and jko ~**+1 px**; Home Br ~**+16 px** and jko ~**+9 px**. Shift only the affected letter groups using local Canvas transforms in `splashWordmark()`.
- Source Splash coral 2 ornament sits ~**+6 px** to the right, Home ~**+12 px**; Home ornament was approximately **6% taller** than the artwork. Adjust only that decorative logo ornament, not gameplay tiles.
- Apply a soft top-to-bottom raised-ink gradient and thicker rounded strokes to the light-theme Splash/Home wordmark. Source navy RGB shifts from roughly (35,49,82) at the top to (29,41,71) below.
- Apply a restrained top-to-bottom deep-navy gradient to the light portrait Bravo! heading, where owner sampled RGB is roughly (31,52,87) near the top and (18,33,62) below.
- Splash remains **2700 ms**, then automatically enters Home, preserving elapsed duration across rotation. Existing Home modes, actions, actual score and shared animations are untouched.

## Acceptance
1. Re-run `Android pull request validation`, `Android 16 navigation bar and inset QA` and `Android UI smoke and screenshot QA` on **the exact current PR head SHA**.
2. Download newly captured emulator screenshots. Compare **same game state**: Splash `three-screen-source-splash-941x1672.png`, Home `owner-home-12560-3864-941x1672.png`, Bravo `owner-bravo-full-board-2048-941x1672.png` against the three supplied PNGs, plus logo/CTA/score/crown subregions.
3. Check 320×480, 360×600, 941×1672 and landscape layouts for clipping.
4. If MAE or visual readability regresses, revert/tune specific transforms; never change the functional game to force a reference fixture.
5. No claim of fully pixel-perfect 1:1, merge or release until latest-head CI and visual comparison are reviewed.
