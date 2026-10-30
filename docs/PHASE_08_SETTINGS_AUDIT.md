# Faza 08/10 — Postavke · 30. listopada 2026.

## Referentni materijali

- Izvorni vlasnikov PNG: `Slika ChatGPT 24. lis 2026. 22_15_19-10(20261030-222415).png`, izvorna veličina **941 × 1672**.
- Usporedna stvarna emulator snimka: `reference-settings-polished-941x1672.png` iz javnog GitHub Actions artifacta `Brojko-real-device-UI` workflowa [#41130372871](https://github.com/bren-wp/Brojko/actions/runs/41130372871).
- Prethodno je i dokument `docs/VISUAL_QA_2026-10-28.md` bilježio MAE 15,87 na starijem baselineu. **Novo referentno početno mjerenje** odnosi se na snimku iz #41130372871 i nije isto QA izvođenje.

## Native 941 × 1672 baseline, bez skaliranja, pomaka ili resamplinga

| ROI izvornog PNG-a, (x0,y0,x1,y1) | Početni RGB MAE |
|---|---:|
| Cijeli zaslon (0,0,941,1672) | **15,462** |
| Naslov i dekoracije (110,48,820,210) | 35,145 |
| Prva četiri retka (30,220,900,820) | 12,173 |
| Sigurnosna kopija/sinkronizacija (32,828,905,998) | 26,993 |
| Privatnost i informacije (30,1000,905,1300) | 12,639 |
| Donja kartica (30,1310,905,1645) | 21,571 |
| Lijevi stupac ikona (55,220,185,1300) | 18,638 |
| Desne sklopke (710,225,875,810) | 17,198 |
| Donji logo (220,1330,720,1530) | **51,760** |

Usporedba sadrži različita realna početna stanja: referentni UI prikazuje uključene obavijesti i nepostojeći cloud-sinkronizacijski prekidač, dok originalni emulator prikazuje stvarne lokalne postavke. Najveća razlika u donjem logotipu proizlazi iz organski oblikovanog ilustracijskog wordmarka (PNG) i postojećeg programskog Nunito/Canvas wordmarka. MAE **nije** dokaz pixel-perfect 1:1.

## Izvedeno na PR #28

- Postavke dobivaju vlastite koraljne dvokrake naslovne dekoracije umjesto globalnih tankih `rays` iz drugih zaslona.
- Nijansirani reljefni kvadrati ikona s lokalnim tankim rubnim odsjajem i mekanom sjenom, bez promjene boja gameplay pločica ili ikona na drugim ekranima.
- Namjenski SVG-ekvivalentni Canvas vektori za bijeli zvučnik sa zvučnim valovima, oblak za lokalne kopije i puni bijeli štit s koraljnim detaljem.
- Tirkizne i neutralne sklopke s nježnim vertikalnim gradijentom, reljefnim bijelim kružićem i aktivnim stanjem vezanim uz postojeće spremanje.
- Sloj svijetlog krem gradijenta na izborniku jezika, bez promjene stvarnog jezika (trenutno hrvatski).
- Test `settingsScreenEightMatchesReferenceControlsWithoutFakeSync`: 7 punih dodirnih zona, uključeno stanje zvuka, vibracije i obavijesti samo tijekom kontrolirane snimke, nezavisno spremanje prvih dvaju prekidača, i stvarne akcije za lokalnu kopiju, jezik, privatnost i informacije.
- Obvezni PNG `settings-phase08-source-on-941x1672.png` iz stvarnog Android emulatora. Test na kraju vraća prethodne lokalne postavke.
- `tools/visual_pixel_audit.py --screen Postavke --controlled-settings --include-regions` mjeri osam regija bez transformacija izvornog PNG-a.

## Odstupanja namjerno nisu lažirana

Mockup prikazuje `Sinkronizacija napretka` kao uključenu cloud opciju, ali aplikacija nema udaljenu sinkronizaciju. U produkciji kartica i dalje ispravno otvara **lokalnu sigurnosnu kopiju**, uz pojašnjenje `Lokalna kopija`. Ovdje **nije dopušteno** prikazati nepostojeću funkciju kao aktivnu.

Broj verzije i stvarno korisničko stanje proizlaze iz aplikacije; originalni ilustracijski `v1.0.0` ne smije zamijeniti stvarnu verziju. Nema generiranja novih referentnih slika u chatu.

## Kriteriji prihvaćanja

1. Build, lint, unit, Android 16 i emulator screenshot QA moraju uspješno završiti na **zadnjem** PR HEAD-u.
2. Preuzeti **novi** screenshot `settings-phase08-source-on-941x1672.png`, računati full/ROI MAE i dokazati stvarnu promjenu bez regresije.
3. Testirati zvuk/vibraciju/obavijesti, lokalni export/import, hrvatski izbornik jezika, pravne stranice i informacije, TalkBack, Android 15/16, 360×600 i 1080×2400.
4. Ne tvrditi da je potpuno 1:1 ako PNG pokazuje različit logo, nepoklopljene ikone, boje ili geometriju.
5. Ostaviti PR kao Draft i **ne izdavati release** do uspješnog završnog QA.

**Status:** implementacija i testovi su commitani; završni emulator screenshot, ROI usporedba i CI za zadnji HEAD trebaju potvrdu. Sljedeća faza: **09/10 — Splash**.
