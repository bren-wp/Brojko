# Brojko — faza 128: stvarni pixel audit i nativne zrake pobjede

Datum: 11. studenoga 2026. Izvor: korisnikovih devet PNG referenci veličine **941 × 1672**, plus zasebna branding ploča 1448 × 1086 koja nije zaslon aplikacije. Referentni PNG-ovi nisu dodani u Android aplikaciju.

## Reproducibilna polazna točka

- Uspješan Android UI workflow: [42937807059](https://github.com/bren-wp/Brojko/actions/runs/42937807059), head SHA `b1c86222a168dcbded01ec710c24138e436be96d`.
- Izvorni artifact: `Brojko-real-device-UI`, ID `13820170554` (testna grana PR-a #127, čije su izmjene poslije spojene).
- Usporedba identičnih 941 × 1672 piksela; RGB MAE = srednja apsolutna razlika RGB kanala na skali 0–255, bez resizea, cropanja cijelog zaslona, pomicanja ili normalizacije.
- Rezultati, datumi, stanje postignuća i vizualni AA ostaju dinamički i utječu na razliku. MAE **nije** postotak vizualne podudarnosti.

| Zaslon | Izvor u korisnikovim slikama | Emulatorski PNG u artefaktu | RGB MAE |
|---|---|---|---:|
| Splash | 06-2 | splash-full-width-portrait-reference | 10,761 |
| Početni | 13-3 | home-source-accent-rays | 14,614 |
| Klasično 4×4 | 13-4 | golden-state-game | 13,670 |
| Pobjeda | 14-5 | golden-state-win | **14,335** |
| Dnevni izazov | 15-6 | daily-large-safe-primary-rays | 13,762 |
| Misije | 16-7 | missions-phase126-weekly-target-owner | 13,899 |
| Statistika | 17-8 | golden-state-stats | **14,325** |
| Teme | 18-9 | themes-phase127-source-control-ink | 13,862 |
| Postavke | 19-10 | reference-settings-polished | 11,516 |

## Najveće lokalne razlike (prije faze 128)

| Regija u izvornim 941 × 1672 koordinatama | RGB MAE |
|---|---:|
| Postavke — lokalna kopija/sinkronizacija x32–905/y828–998 | **26,55** |
| Pobjeda — kruna x300–630/y340–520 | **24,87** |
| Pobjeda — akcije x160–790/y965–1390 | 20,66 |
| Teme — kartice x40–900/y380–800 | 20,23 |
| Klasično — kontrole x35–925/y1330–1605 | 20,02 |
| Statistika — gornje KPI kartice x20–923/y305–540 | 19,47 |

## Konkretan zahvat: pobjednička kruna

Izvorna ilustracija ima tri **zlatne nativne zrake** oko podnožja krune, koje su prije bile potpuno odsutne u Canvas pobjedničkom modalu:

- lijeva: x333–388, y494–543;
- desna: x551–606, y494–547;
- srednja: x464–482, y535–565.

Na pet unutarnjih izvornih piksela `(345,505), (370,530), (596,505), (560,539), (473,550)` izvornik prikazuje zlato (približno RGB 254/196–203/71–83), dok je stari emulator prikazivao gotovo bijelu podlogu (RGB 255/248/239).

Dodane su samo tri skalabilne, zaobljene **Android Canvas** linije na izvornim koordinatama za svijetli portretni `win` prikaz, s izoliranim zlatnim bojama; nisu zamijenjene slike ekrana, nisu promijenjeni rezultati ni sve tri funkcionalne akcije pobjede. Ostale teme, omjeri i modali nisu namjerno mijenjani. Novi instrumentacijski test `OwnerVictoryGoldRaysPhase128Test` provjerava pet zlatnih piksela, izvorni omjer i tri izvorne aktivne radnje.

## Obvezno prije spajanja i tvrdnje o 1:1

1. Android PR validation: build, lint, unit.
2. Android 16 inset/edge-to-edge QA.
3. Android emulator UI test, uključujući novu klasu, na posljednjem SHA.
4. Preuzeti **novi** artefakt i usporediti `golden-state-win-941x1672.png` s dostavljenim izvornikom; ponovno izračunati cijeli zaslon, krunu, modalne akcije i sve ostale lokalne ROI. Vratiti zahvat ako bilo koji važan ROI regresira.
5. Provjeriti modalne akcije i tamnu/živopisnu temu, male i velike portrete, te da se i dalje prikazuju **stvarni** podaci.
6. Prazna mjesta u Postavkama nisu dokaz cloud sinkronizacije: projekt trenutno podržava lokalni izvoz/uvoz i ne smije tvrditi da sinkronizira s poslužiteljem.

**Status:** statička korekcija i instrumentacijski test su implementirani u PR-u. Novi emulator pixel rezultat, strogi 1:1 i produkcijski release nisu potvrđeni dok CI i usporedba ne završe.
