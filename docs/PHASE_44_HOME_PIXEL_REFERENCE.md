# Brojko — Home pixel-by-pixel calibration, phase 44

## Owner-defined scope

**Home only**, using the supplied portrait reference image at **941 × 1672** and the brand sheet as a styling guide. Splash and Bravo! are not modified. No screenshots are substituted for functional UI. Actual stored scores, gameplay, navigation and touch rectangles remain live.

## Grounded baseline

The current main branch includes merged PR #43 (head `1ffbf68`). Verified **real Android emulator** image from UI run [41554953769](https://github.com/bren-wp/Brojko/actions/runs/41554953769), artifact `13194396439`, filename `owner-home-12560-3864-941x1672.png`. Compare against the owner-supplied `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261102-232106).png`.

Equal RGB pixel resolution, **no resizing or alignment shift**, actual best/last scores of 12,560 and 3,864:

| Region (native pixel y) | Before-change MAE, 0–255 |
|---|---:|
| Full 941×1672 | **19.82** |
| Logo top y150–490 | **27.69** |
| Score cards y480–640 | **24.72** |
| Primary CTA y678–900 | **19.46** |
| Three modes y900–1295 | **21.76** |
| Four shortcuts y1300–1590 | **15.15** |

These are **baseline values**, not the post-PR results. They cannot prove pixel-perfect 1:1.

## Exact correction plan

- **Logo, B glyph:** owner's actual navy B bounding rectangle approximately x170..318, y215..398. Prior emulator B x183..306, y217..391; the adjacent `r` already fits within ~2 pixels. Render B and r as separate native glyphs on **light portrait Home only**, expanding just B by 20% horizontally and ~5% vertically. Splash uses the original shared wordmark path.
- **Score cards:** first score starts near native y568 in reference vs ~y582 in emulator. Move both live score strings up 6 design units (~14 native pixels), both titles up 5 units (~12px), mini-statogram up 5 units and right statistics icon up 4 units. The cards and all tappable bounds remain unchanged.
- **Primary `Nova igra`:** source play shape x224..293; previous emulator x243..314. Recenter the **rounded**, shadowed native triangle about 8–9 design units left. Increase light Home CTA heading toward reference typography, but keep text fitted to its original width. Existing button action is untouched.
- **Dnevni izazov:** reference calendar starts around native y960 and reaches y1125; old emblem began ~y1007. Enlarge light Home calendar from 69 to 88 design units and move its center from y+68 to y+56. Do not touch the game mode, label, other theme calendars or the entire card's input region.
- **Decorative corner 2 and 4:** owner top-left coral connected silhouette x0..196 versus emulator x0..190; owner top-right teal x685..920 and y0..209 versus emulator x716..933 and y0..200. Adjust the light portrait 2 by +3.5% horizontal scale; adjust the 4 by -9 design units horizontal translation plus 8% horizontal / 6% vertical scale and a 2-unit lower local offset. Both are decorative Home-only Canvas transforms, not actual playable tiles.
- All other screens and layouts intentionally left unchanged; no fake stats or hardcoded reference text in production.

## Regression gates

The new `ownerHomePhase44PositionsAndActionsMatch941x1672Reference` instrumentation test creates a **real Android Canvas capture** (`owner-home-phase44-12560-3864-941x1672.png`) and verifies the score baseline, the calendar's top color region and that the two score cards, three game modes and four shortcuts remain genuine actions.

Review `Android pull request validation`, `Android 16 navigation bar and inset QA`, and `Android UI smoke and screenshot QA` at the **last head SHA**. Then download the new screenshot artifact, compare full-image and focused ROI MAE against the above owner reference, inspect cropping on 320×480 and 360×600, and keep the PR in **Draft** if any part regresses. The reference's illustration-level rounded glyphs may require additional native path calibration to achieve 1:1; passing tests is not equivalent to perfect pixel identity.
