# BROJKO — faza 39: samo 3 zaslona, Splash → Home → Pobjeda

Datum: 2. studenoga 2026.

## Izvori i stvarni baseline

**Vlasnikove nove datoteke 941 × 1672 px** (nisu ugrađene u aplikaciju niti dodane repozitoriju):

- Splash — `Slika ChatGPT 24. lis 2026. 22_15_06-2(20261102-215423).png`.
- Home — `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261102-215433).png`.
- Pobjeda — `Slika ChatGPT 24. lis 2026. 22_15_14-5(20261102-215545).png`.

Stvarni Android renderer prethodne potvrđene verzije: [Android UI #41525523484](https://github.com/bren-wp/Brojko/actions/runs/41525523484), screenshot artifact ID **13179933385**, snimke `splash-owner-wordmark-weight-941x1672.png`, `home-controlled-reference-941x1672.png`, `golden-state-win-941x1672.png`. Svi PNG-ovi uspoređeni su u RGB-u bez pomicanja, skaliranja, smanjivanja ili automatskog poravnanja. Fixture za Home i pobjedu koristi samo testnu memoriju, a produkcija stvarne bodove.

| Zaslon | RGB MAE cijeli zaslon /255 prije izmjena | Mjereni važni ROI-ji |
|---|---:|---|
| Splash | **18,587** | Glavni logotip (70,650–895,1050): 26,895; gornje pločice (0,110–941,660): 17,217; slogan (50,1280–890,1440): 11,728 |
| Home | **22,241** | Logo (70,175–895,480): 33,777; rezultatne kartice (30,485–910,680): 20,984; CTA (55,675–895,880): 19,452 |
| Pobjeda | **18,077** | Cijeli modal (120,450–820,1400): 26,121; naslov (220,510–790,755): 34,755; gumbi (155,975–760,1360): 28,808 |

Gornje vrijednosti su **baseline**, nisu nova potvrđena podudaranja.

## Implementacija

### 1. Splash

- Trajanje **2700 ms** od prvog onCreate-a; postojeći `screen.postDelayed` automatski navigira na `home` samo ako je i dalje prikazan `splash`. Ne blokira ostale radnje na obnovljenoj sesiji.
- Novi ugovor testova: **2000 ms ≤ Splash ≤ 3000 ms**; postojeći 17-viewport responsive audit ostaje.
- U vlasnikovu izvoru koraljna **2** naginje se udesno (+), tirkizna **4** i žuta **16** ulijevo (−). Stari native renderer imao je **sva tri nagiba obrnuta**. Rotacije popravljene samo za svijetlu portretnu Splash ilustraciju.
- Test `splashOwnerTileTopEdgesUseCorrectRotations`: stvarni nativni PNG 941×1672 i gornji obrisi obojenih pločica.

### 2. Home

- Gornja lijeva koraljna pločica: izvorni centroid (92,102) px, stari Android (83,105). Samo dekorativni transform +4 dizajnerske jedinice po X i −1,4 po Y.
- Gornja desna tirkizna: izvorni centroid (811,93), stari Android (855,109). Samo dekorativni transform −18 po X, −7 po Y.
- Originalni glavni koraljni CTA u izvoru prestaje na **y≈879 px**, stari nativni donji 3D rub na **y≈895 px**. Smanjen samo `homeHero` lip sa 6 na 1 dizajnersku jedinicu, bez promjene touch područja, navigacije, boje ploče i korisničkih rezultata.
- Test `homeSourceCornerCentersAndHeroLip`: centroidi obojenih maski, boja ispod gumba i tri aktivne prečice.

### 3. Pobjeda „Bravo!”

Položaj nativnih tipki starog screenshot-a i originala pri x≈250 px:

| Gumb | Originalni obojeni raspon (y px) | Stari emulator |
|---|---|---|
| Nastavi igrati (koraljni) | **994–1120** | 981–1111 |
| Nova igra (krem) | **1122–1267** | 1125–1241 |
| Podijeli rezultat (plavi) | **1269–1377** | 1252–1372 |

Korigirane su samo geometrije: primarni `y422,h53`, sekundarni `y478,h60`, share `y539,h49`, i njihovi postojeći funkcionalni `Tap` handleri.

Test `victoryOwnerThreeActionBandsKeepRealCommands` mjeri stvarnu boju na y1115/1250/1370, tri stvarna `Tap` i nepromijenjeni rezultat. Postojeći testovi za nastavak igre, sesiju, nagrade, navigaciju i sharing ostaju.

## Obvezan završni QA

- [ ] Android build/lint/unit na **zadnjem PR39 HEAD-u**.
- [ ] Android 16 edge-to-edge/insets QA na **zadnjem PR39 HEAD-u**.
- [ ] Instrumentacijski Android UI testovi + stvarne nove PNG snimke na **zadnjem PR39 HEAD-u**.
- [ ] Usporediti točno navedena tri PNG originala s novim screenshotovima 941×1672 te izračunati cijeli i regionalni RGB MAE.
- [ ] Kod bilo kakve regresije korigirati samo konkretni odgovarajući zaslon, ponoviti testove i usporedbu.
- [ ] Provjeriti Splash→Home (2,7 s), realne Home gumbe, stvarni pobjednički rezultat i sva tri gumba.
- [ ] Ne tvrditi 1:1 samo na temelju kompilacije; ne spajati i ne izdavati APK/AAB dok provjere nisu gotove.

**Opseg ovog PR-a strogo su navedena tri zaslona. Ne dirati rad u PR38 za Daily/Missions/Stats/Themes.**


## Faza 39.1 — ponovna usporedba nakon zelenog UI CI-a 9100a625

Korištene su **posljednje tri** korisnikove PNG datoteke iz razgovora:
- Splash: `Slika ChatGPT 24. lis 2026. 22_15_06-2(20261102-223941).png`
- Home: `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261102-223941).png`
- Pobjeda: `Slika ChatGPT 24. lis 2026. 22_15_14-5(20261102-223929).png`

Pravi Android screenshotovi iz **potpuno zelenog** [UI QA 41535505288](https://github.com/bren-wp/Brojko/actions/runs/41535505288), artefakt `13183767537`: `three-screen-source-splash-941x1672.png`, `home-controlled-reference-941x1672.png`, `golden-state-win-941x1672.png`. Svi su uspoređeni **piksel za piksel RGB** bez interpolacije i pomicanja na 941×1672. Isti testni fixture mora se koristiti pri staroj i novoj emulaciji.

| Zaslon | Prije 39.0 | Provjereni 9100a625 | Delta |
|---|---:|---:|---:|
| Splash, cijeli | 18,587 | **17,497** | **−1,090** |
| Splash, gornje pločice x0..941 y90..700 | 17,170 | **14,207** | **−2,963** |
| Home, cijeli (kontrolirani fixture) | 22,241 | **20,512** | **−1,729** |
| Home, gornji dio x0..941 y0..500 | 30,250 | **27,177** | **−3,073** |
| Home, glavna CTA x55..895 y675..880 | 19,452 | **18,473** | **−0,979** |
| Pobjeda, cijeli (`golden-state-win`, isti fixture) | 18,077 | **16,693** | **−1,384** |
| Pobjeda, gumbi x155..760 y975..1380 | 29,546 | **18,537** | **−11,009** |

**Opasnost od lažne regresije:** `three-screen-source-victory-941x1672.png` snimljen je s drugačijim game board testnim fixtureom (gotovo sve prazne pločice) i daje cijeli MAE **19,431**. Ta brojka **nije usporediva** sa starim `golden-state-win` koji sadržava punu referentnu ploču. Na `golden-state-win` istoga fixturea stvarna Pobjeda se poboljšala. Produkcija uvijek pokazuje **stvarno korisnikovo stanje igre**.

### Faza 39.2 — difuzni odsjaj i Home minijatura (nakon mjerenja gore)

- `splashFloatingTile`: nativni radijalni bijeli odsjaj malog intenziteta gore lijevo, samo za koraljnu/tirkiznu dekoraciju u svijetlom referentnom portretu (uključujući Home top-corners); igraće ćelije nisu izmijenjene. Teal točka (610,210) izvorni RGB≈(132,252,242), prethodni native (59,227,218). Test `splashOwnerTileTopEdgesUseCorrectRotations` proširen stvarnom pixel-provjerom te točke.
- `homeClassicPreviewTile`: mekša ivica i zasićeniji gradijenti samo malih 4×4 dekorativnih pločica na Homeu. Originalna koraljna pločica (90,975) ≈(253,97,71), ranija prava emulacija (255,134,105). Funkcionalno stanje i 4×4 igra nisu mijenjani.
- `homeSourceCornerCentersAndHeroLip` proširen testom koraljnog piksela u minijaturi.
- **Nove vrijednosti RGB MAE nakon posljednja dva zahvata još nisu potvrđene**. Ne koristiti tablicu iznad kao dokaz njihova uspjeha: treba čekati završetak i analizu stvarnih PNG-ova na zadnjem commitu.

### Stroge granice

U ovom PR-u nema izmjena Daily/Missions/Stats/Themes/Settings, nema statičnih screenshotova kao pozadinskih slika, nema kopiranja lažnih demo bodova u produkciju, nema novih release artefakata ni mergea bez zelenog CI-a i aktualnog vizualnog testa.
