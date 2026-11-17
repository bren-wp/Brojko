# BROJKO — faza 152: Dnevni izazov i 3D poklon

Početni `main` SHA: `7f39f429c2786d3b4dc607e4cd6e49fcd2d51bd3` (v1.12.251).
Ova faza ne prepisuje Java/Canvas arhitekturu, nema promjena u `TileGame`, datumima, misijama, rezultatima ni SharedPreferences.

## Izvor i stvarna početna mjerenja

- Originalni privatni vlasnikov PNG: `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261111-200341).png`, 941×1672.
- Stvarni Android PNG iz uspješnog PR #151: `daily-refined-board-gift-941x1672.png`, artifact `Brojko-real-device-UI` #14183935233, run https://github.com/bren-wp/Brojko/actions/runs/43767988311.
- Metoda: surovi RGB MAE (skala 0–255), bez skaliranja, pomaka, interpolacije ili normalizacije; exact-RGB je udio potpuno identičnih RGB piksela.
- Slike su ostale izvan repozitorija, nisu zamijenjene sintetičkim screenshotovima.

| Izvorno područje (x0,y0,x1,y1) | RGB MAE prije | Exact RGB prije |
|---|---:|---:|
| Cijeli Dnevni izazov (0,0,941,1672) | 13.657163 | 3.28935% |
| Kartica nagrade (40,1315,899,1512) | 19.156354 | 0.47866% |
| Poklon (100,1310,320,1540) | 19.220079 | 0.35771% |
| Mašna (115,1310,310,1410) | 18.846171 | 0.71282% |
| **Tijelo i vrpce (120,1400,302,1530)** | **25.279065** | **0%** |
| Donji desni rub (270,1440,310,1525) | 30.694706 | 0% |
| Donji lijevi rub (110,1440,160,1525) | 26.005804 | 0% |
| Sredina tijela (185,1425,247,1512) | 13.220986 | 0% |
| Donji CTA (105,1530,840,1660) | 12.714289 | 0.26688% |

Izvornik ima voluminozno tijelo poklona koje se sužava prema dnu. Prethodni emulator crta pravokutno tijelo sve do ruba x≈300 na y≈1480–1510, gdje izvorni PNG prikazuje lilac pozadinu. Središnja ljubičasta nijansa već je razmjerno bliska izvoru; proizvoljna promjena svih boja bi povećala pogreške.

## Mjerljivi zahvat

U `MainActivity.BoardScreen.dailyRewardGift()` uveden je `Path sourceBody` samo kada vrijedi `referencePortraitLayout && theme == 0`. Oba gornja vrha, zaobljenje, bočne strane i donji rub određeni su skalabilnim Canvas Bezier krivuljama. Tijelo i zlatne okomite vrpce klipaju se unutar putanje; poklopac i mašna ostaju izvan klipa. Za ostale teme/rasporede ostaje stari `drawRoundRect`. Svi touch targeti, datum, lokalni reward, zvuk, animacije i score logika ostaju nepromijenjeni.

Instrumentacijski `OwnerGiftAndChartPigmentsPhase145Test` dodatno provjerava da na (300,1480) i (300,1500) postoji pozadina umjesto preširoke ljubičaste plohe, da ljubičasta sredina ostaje nacrtana te da samo iscrtavanje ne mijenja postojeću `TileGame` sesiju.

## Kriterij prihvaćanja (još nije ispunjen)

- [ ] Android PR validation: Gradle build/Lint/unit/SBOM/High-Critical runtime CVE.
- [ ] Android 16 oboje navigacije i TalkBack/potpuni accessibility QA.
- [ ] Android emulator UI smoke, stvarni Android screenshotovi i broj instrumentacijskih testova.
- [ ] Novi `daily-refined-board-gift-941x1672.png` usporediti s **istim** originalnim privatnim PNG-om.
- [ ] ROI `Tijelo i vrpce` MAE mora pasti ispod 25.279065; ne prihvatiti štetnu regresiju cijelog Dnevnog izazova.
- [ ] Usporediti svih 254 stvarnih screenshot stanja sa starim verificiranim main artefaktom; provjeriti neplanirane razlike i slučajne promjene dnevnih podataka.
- [ ] Provjeriti male i velike zaslone te tamnu i živopisnu temu.
- [ ] Po uspješnom mergeu objaviti novo izdanje, provjeriti APK/AAB, SBOM, SHA256SUMS i 28 stvarnih PNG-ova.

**Status:** zahvat je izvorno predložen, bez pretpostavke o poboljšanju. Novi MAE ostaje nepoznat do stvarne emulatorske snimke; nijedan zaslon nije proglašen 1:1.
