# Brojko — phase 39: two Splash references and victory dialog

## Scope

The three supplied PNGs are each 941 × 1672 pixels. The first two Splash screenshots are byte-identical; the third is the Victory screen. Only these images are covered here.

The production Canvas renders every piece of UI natively, not by displaying a full-screen screenshot. Gameplay data, real scores, local storage, and actions stay functional.

## Startup behavior

The Android Splash is visible for 2800 milliseconds, within the requested 2–3-second interval, before automatically navigating to Home. On configuration recreation the Activity restores the current page instead of replaying Splash. A delayed callback never overrides another page.

## Reference checkpoints

| Component | Source reference | Implementation and regression check |
| --- | --- | --- |
| Splash background | Cream/off-white RGB approximately (254,252,244) | Splash-only #FEFCF4, with original Home canvas untouched |
| Floating numbers | Raised 2, 4, 8, 16, 64 and softly defocused distant ornaments | Canvas gradients, bevels, shadows, and preserved spacing |
| Identity | Navy Brojko wordmark, coral number 2, by Brendigo | Native illustrated lettering and no development placeholders |
| Slogan | Warm cream pill, coral and turquoise typography | Previously calibrated source measurements preserved |
| Victory background | Dimmed winning board, score HUD, bottom cards | Existing native portrait backdrop; no active taps behind dialog |
| Modal | Crown, tilted Bravo!, three golden stars, warm score cards | Source-aligned native geometry and raised fills preserved |
| Score | The sample image displays 2.048 | Real game.score; changes correctly if a game scores 4.096 |
| Actions | Nastavi igrati, Nova igra, Podijeli rezultat | Exactly three interactive and accessible modal buttons |

## Screenshots and automated gate

Android instrumented test ownerSplashAndVictoryReferencePixelsAreNativeAndFunctional saves owner-splash-01-02-native-941x1672.png and owner-victory-03-native-941x1672.png and checks key visual anchors and functional actions. splashAutoAdvancesBetweenTwoAndThreeSeconds asserts the requested interval. The 17-viewport test asserts the chosen 2800 ms and responsive geometry.

**Acceptance:** compare Android emulator renders against source PNGs using full RGB MAE and localized ROI masks (wordmark, number tiles, byline, tagline, crown, modal headline, stars and three buttons). Passing CI does not by itself prove pixel-perfect 1:1; do not merge or publish until the final head SHA has three green workflows and the visual comparison is reviewed.
