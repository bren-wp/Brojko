# BROJKO — status vizualnog QA-a, 12. studenoga 2026.

## Provjerena referentna infrastruktura
- Dev alat `tools/visual_pixel_audit.py` uspoređuje vlasnikove originalne
  PNG reference i prave Android emulator snimke bez skaliranja ili poravnanja.
- `scripts/verify-emulator-screenshots.py` u release QA-u provjerava stvarne
  941×1672/360×600/1080×2400 snimke, funkcionalne modale i stanja.
- `scripts/stage-marketing-screenshots.py` priprema 28 različitih
  provjerenih snimki, bez AI mockupova i bez stavljanja slike u igru.
- `BoardAccessibilityDelegate` je necrtačka komponenta: nije legitimno
  pretpostaviti 0% pixela promjene bez nove snimke; renderer nije mijenjan.

## Postojeći izmjereni baseline (v1.12.221, ne novo mjerenje)
Prema `docs/PHASE_139_GAMEPLAY_4X4_OWNER_RGB_AUDIT.md`:
- Gameplay 4×4, cijeli 941×1672: RGB MAE **12,635 / 255**.
- Ploča: RGB MAE **14,119 / 255**.
- Donje kontrole: RGB MAE **13,451 / 255**.

Raniji dokument `docs/PHASE_63_SECURITY_DEAD_CODE_DEPENDENCIES_GOOGLE_PLAY_2026-11-08.md`
sadrži stariji baseline za devet glavnih ekrana; vrijednosti se ne smiju
miješati s novijim fixtureovima.

## Što nedostaje za cilj 1:1
- Ponoviti per-screen / per-ROI usporedbu na **istom** fixtureu (pločice,
  rezultat, tema, zoom, DPI i statusne trake).
- Izvorne korisnikove reference nisu u repozitoriju; ovaj commit ne sadrži
  novo izračunati RGB MAE za svih 28 ekrana.
- Za svaku razliku zasebno bilježiti RGB MAE, SSIM, geometriju,
  baseline commit, original i stvarni Android screenshot.
- Reference za neke modale/5×5 i dalje treba potvrditi; nikada ne
  proglašavati 0% bez stvarne pixel usporedbe.

Poboljšanja se rade nativnim Canvas elementima, a ne maskiranjem
razlika ili neinteraktivnom pozadinskom slikom.
