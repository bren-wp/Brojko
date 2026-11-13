# BROJKO — faza 140: stvarni izvorni PNG-ovi i Native Themes ROI audit

Datum: 13. studenoga 2026.

## Izvor istine

- **Korisnikove izvorne reference:** devet vlastitih PNG datoteka
  `Slika ChatGPT 24. lis 2026.` iz privatne datotečne knjižnice
  (`-2`, `-3`, `-4`, `-5`, `-6`, `-7`, `-8`, `-9`, `-10`).
- **Android baseline:** stvarne, neizmijenjene emulatorske slike iz
  [GitHub run #43516154091](https://github.com/bren-wp/Brojko/actions/runs/43516154091),
  arhiva `brojko-release-emulator-proof`, izdanje
  [v1.12.239](https://github.com/bren-wp/Brojko/releases/tag/v1.12.239).
- Oba ulaza imaju **941×1672**, RGB, usporedba apsolutnih razlika u sva
  tri kanala, **bez resizea, cropanja, poravnanja ili interpolacije**
  cijelog zaslona. ROI se izdvajaju u fiksnim izvornim koordinatama.
- Izvorne reference nisu dodane u javni Git niti u APK/AAB. Nema
  generiranja zamjenskih slika; sve brojke su iz stvarnih PNG-ova.

## Cjelokupni baseline (RGB MAE, raspon 0–255)

| Zaslon | v1.12.239 | % potpuno jednakih RGB piksela |
| --- | ---: | ---: |
| Splash | 10,761 | 6,061 |
| Home | 13,121 | 3,526 |
| Classic 4×4 | 9,533 | 2,686 |
| Pobjeda | 13,722 | 0,546 |
| Dnevni izazov | 13,763 | 3,289 |
| Misije | 13,314 | 1,459 |
| Statistika | 12,959 | 0,817 |
| Teme i izgled | 13,432 | 4,525 |
| Postavke | 10,846 | 3,449 |

**MAE nije postotak odstupanja.** Dinamički rezultati, datumi i
lokalna arhiva ne smiju biti zamijenjeni demonstracijskim vrijednostima
s referentne slike radi boljeg rezultata.

## Najveće potvrđene lokalne razlike

| Izvorni ROI | Koordinate (x0,y0,x1,y1) | MAE v1.12.239 |
| --- | --- | ---: |
| Teme, sve kartice | 32,370,905,790 | 19,630 |
| Teme, živopisna kartica | 635,393,900,672 | **24,842** |
| Teme, živopisni donji rub | 640,618,885,665 | **26,220** |
| Teme, živopisna lijeva pozadina | 645,405,685,655 | **26,546** |
| Teme, živopisna desna pozadina | 855,405,890,648 | **28,059** |
| Postavke, lokalna sigurnosna kopija | 32,828,905,998 | 19,426 |
| Dnevni izazov, nagrada | 35,1320,900,1550 | 18,748 |
| Dnevni izazov, ciljevi | 45,955,895,1325 | 18,499 |

## Uski zahvat na izvornom renderu „Teme i izgled“

Korigirati samo svijetli vlasnički portretni prikaz
`themeChoiceSurface(... preset=2)`: gornji i donji pigment te
prozirne cijan-ljubičaste slojeve, uz izdubljenu podlogu 2×2 uzorka.
**Nikada ne mijenjati** tile boje u stvarnoj 2048 partiji, pozadinski
`TileGame` RNG, rezultat, povijest, sigurnosne kopije, prefse, dodirne
zone kartica ili podatke u `SharedPreferences`.

Cilj su potvrđena 24,842/26,220/26,546/28,059 RGB MAE područja,
ali poboljšanje se **mora ponovo izmjeriti** na novom emulatorskom
artefaktu posljednjeg SHA-a, s istim izvornim PNG-om i istim ROI-em.
Ako se lokalni ili globalni rezultat pogorša, prilagodbu korigirati
ili vratiti. Ne tvrditi 1:1 dok je stvarni pixel diff različit od nule.

## Regresijski QA

- `OwnerThemesProgressPreservationPhase140Test`: devet rendera
  (941×1672, 360×800, 1080×2400 × 3 stila pločica), živi
  hitboxovi tema i neizmijenjena serijalizirana 2048 sesija, najbolji
  rezultat, zvuk, vibracija i jačina animacija.
- Ostaju postojeće `OwnerCornerPixelPhase135Test`,
  `OwnerThemePickerPhase131Test`, `StatsThemesOwnerSourcePhase56Test`,
  screenshot i API 36 insets provjere.
- Zadržati zahtjev za svaki novi uspješni merge: novi GitHub
  Release **APK + AAB**, bez obveznog produkcijskog potpisa, uz jasnu
  oznaku unsigned preview.

Otvoreno: ostalih osam zaslona, odgovarajući modalni prikazi,
tamna/živopisna tema i sve dodatne rezolucije zahtijevaju ponovljene
vlasničke ROI usporedbe. Ova iteracija nije dokaz 0% odstupanja.
