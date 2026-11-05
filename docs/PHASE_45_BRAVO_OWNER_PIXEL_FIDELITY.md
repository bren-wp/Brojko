# Brojko — Phase 45: owner's **Bravo!** victory pixel audit

## Ranking and source of truth

Checked the three user-supplied 941×1672 PNGs in the current conversation against **real Android Canvas captures** from the last verified PR44 emulator archive. The main branch only advanced through Gradle and Dependabot changes since that image: the game renderer is unchanged.

| Screen / exact game fixture | RGB MAE, 0–255 |
|---|---:|
| **Victory — Bravo!** `golden-state-win-941x1672.png`, 2048 with representative 4×4 board | **16.855** |
| Home `owner-home-12560-3864-941x1672.png`, best=12,560/latest=3,864 | 16.153 |
| Splash `three-screen-source-splash-941x1672.png` | 15.679 |

The owner also supplied a brand sheet, **not a fourth app screen**. The above is the rank among the three source-comparable screens, not every screen the game supports. Do not call passing tests or these metrics full 1:1 fidelity.

Source victory file: `Slika ChatGPT 24. lis 2026. 22_15_14-5(20261102-225729).png`. Screenshot image from the real Android emulator attached to `brojko-pr44-home-full-vector-emulator.zip`.

### Measured native owner ROIs

| Owner ROI (left, top, right, bottom) | Before-change MAE |
|---|---:|
| Crown (345,300,600,535) | 30.265 |
| Hand-lettered Bravo (250,520,710,710) | **40.042** |
| Result and stars (170,795,780,957) | 22.697 |
| Three working buttons (165,960,770,1385) | 20.410 |
| Dimmed live game board (30,236,900,1130) | 21.336 |

## This phase (source-measured native Android changes)

**1. The hand-lettered navy wordmark:** the supplied source's actual dark ink mask is x260..688, y534..697, containing about **31,479 navy pixels**. Earlier native Nunito lettering had a different silhouette despite reasonably similar bounding box and roughly 32,398 pixels. The illustration is not a standard font glyph. Store **eleven compact source-outline contours**, including fully transparent holes inside the letters, as Android Canvas `Path` with `EVEN_ODD` fill and a cached navy gradient. The owner artwork's local binary mask has an approximate **98.2% source-contour IoU** at native pixels; this is a geometric preview, **not** a tested final-emulator MAE. Apply only to the light portrait victory modal. Preserve the old responsive Nunito renderer in dark and landscape.

**2. Golden crown:** owner's broad connected gold bounds **x379..562, y375..517**, with ~15,736 pixels; old emulator was **x379..557, y348..518**, with ~18,996 pixels. Remove exaggerated 1.15× height and scale natively about the rim by **0.969× vertically**, **1.03× horizontally** with a small positional correction. First emulator CI found that the **stricter gold-pixel threshold** began at y394 (allowed y370..393) due to the retained too-pale gradient: the old gradient had green 235 / blue 124 at its top. The owner's visible gold samples around y385 are RGB(254,210,84), and around y450 RGB(254,200,70). Light-portrait crown gradient is now calibrated with darker #FFD657 → #FFBE37 to retain a genuine rich yellow peak. The failure was **not** accepted by relaxing the test; subsequent emulator capture must verify the corrected pixel first.

**3. Functional contract:** this is a real 2048 win state, not a painted screenshot. `Nastavi igrati` retains and acknowledges the current session; `Nova igra` restarts through the game's existing archive logic; `Podijeli rezultat` starts the Android share with the actual score. Only these three buttons may be tappable; background dimming and the real frozen board remain. The game board, scoring, notifications, saved game and other pages have no modifications.

## Regression and release gate

- Instrumentation `ownerBravo941VectorInkAndRealModalActions` records `owner-bravo-vector-941x1672.png`, tests navy source bounds/counters/volume, and validates the exact three live actions.
- Existing `ownerBravoCrownRetainsSourceVerticalGoldSilhouette` tests the crown's source vertical bounds.
- Build/lint/unit, Android emulator screenshot QA, and Android 16 insets must pass at the **latest PR head**.
- Download actual newest `golden-state-win-941x1672.png` from emulator artifacts and compare exact same owner fixture with the baseline, both whole-image MAE and heading/crown ROI (no resizing or automatic alignment).
- Check compact 320×480, 360×600, 941×1672, landscape, themes and action focus. No merge/new release if code or screen state regresses.
- A true 1:1 claim requires all pixels and functions to match; this PR is an iterative step, not such a claim.

## Next screens

Once Bravo is verified, proceed by **newest measured MAE**: Home (remaining mode thumbnails, stats cards and lower shortcuts), then Splash (brand-letter contour, blurred distant tiles and tagline). Subsequently rank the gameplay 4×4, 5×5, Daily and remaining screens against their separate owner source images and real emulator captures, preserving exact play states.
