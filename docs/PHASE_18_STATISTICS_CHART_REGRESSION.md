# Brojko — faza 18: Statistika, grafikon i dodirne zone

Datum: 31. listopada 2026.

## Polazište

- Referentni zaslon korisnika **Statistika**, izvorni PNG 941 × 1672, ranije izmjereni povijesni RGB MAE **25.410**. To nije rezultat ovog commita i ne predstavlja dokaz pixel-perfect 1:1.
- Nastavak na `main` nakon spojenih PR-ova [#28](https://github.com/bren-wp/Brojko/pull/28) i [#29](https://github.com/bren-wp/Brojko/pull/29). Potonji je na svom HEAD-u imao tri zelena workflowa i donosi vidljivu strelicu Natrag u Postavkama te Splash od 3,2 sekunde.

## Potvrđen noviji, stvarni baseline iz PR #29

Iz [uspješnog emulator QA-a PR-a #29](https://github.com/bren-wp/Brojko/actions/runs/41145698068) preuzet je stvarni artefakt `Brojko-real-device-UI`, datoteka `stats-source-layout-941x1672.png`. Uspoređen je sa izvornom korisnikovom slikom 941×1672 bez promjene veličine, rotacije ili pomicanja. Oba prikaza koriste isti native viewport, ali kontrolirana testna povijest sadrži pet partija dok vlasnikov ilustrativni grafikon prikazuje deset; to je razlika u podacima, ne nužno UI elementima.

| Područje / native ROI | RGB MAE (PR #29) |
|---|---:|
| Cijeli zaslon | **21.287** |
| Zaglavlje (20,30,920,306) | 30.786 |
| Tri KPI kartice (20,305,923,540) | 27.364 |
| Dva KPI sažetka (20,535,920,760) | 21.451 |
| Grafikon (22,765,922,1160) | 25.825 |
| Povijest partija (20,1160,920,1645) | 14.294 |
| Detalji grafikona (65,790,885,1115) | **32.495** |

**Ovo je reproducibilni baseline posljednje spojene verzije PR #29, NE rezultat nove faze 18.** Raniji MAE 25.410 pripada starijem i drukčijem screenshot fixtureu `golden-state-stats-941x1672.png`; brojke nisu izravno usporedba istih fixturea. Novi MAE se računa tek iz artefakta nakon prolaznih CI testova PR #30. Pri mjerenju paziti na stvarni sadržaj povijesti i vremenske oznake; nikad ne izmišljati postignute vrijednosti kako bi screenshot izgledao bliže ilustraciji.

## Identificirani UI problemi

1. U oba oblika grafikona `Statistika → Zadnjih 10 partija` crtana je obojena traka najmanje **4 logičke jedinice** i za rezultat **0**. Time je prikazana nepostojeća pozitivna vrijednost. Oznaka bodova već je bila skrivena kada je broj nula.
2. Touch zone dvaju prekidača **Bodovi** i **Najveća pločica** imale su visinu samo **35** logičkih jedinica, dok za ostale kontrole zahtijevamo barem **48**. Same vizualne kartice i referentne pozicije ostale su ispravne.
3. U višem `statistics()` dijelu ostao je nepotreban `topScoreIndex` koji se izračunavao, ali nigdje nije koristio.

## Implementacija

- U `statisticsCompact()` i `statistics()` obojena stupčasta traka i brojčana oznaka crtaju se **samo ako je vrijednost strogo veća od 0**. Nula ostaje prazan dio grafikona, ali kronološka oznaka partije ostaje vidljiva.
- Za pozitivnu vrijednost čuva se postojeća visina `Math.max(4f, ...)`, izvorne boje, omjer, brojke, redoslijed partija i pozicije; nema promjene korisničkih rezultata, arhive ili pravila 2048.
- Nevidljive dodirne zone grafikona proširene su sa 35 na **48** logičkih jedinica, bez izmjene vidljivog vizualnog rasporeda i bez ulaska u susjedne akcije.
- Uklonjen neupotrebljavani `topScoreIndex`. Opacitet gradijenta pri pozitivnim stupcima eksplicitno je postavljen prije crtanja kako ne bi naslijedio prozirnost iz prethodne sjene.

## Novi regresijski test

`statsZeroValuesNeverProduceFakeChartBars`:
- Stvarna Android `BoardScreen` za **941×1672** i **1080×2400**.
- U oba načina grafikona, uz kontrolirane lokalne JSON zapise za `0` bodova / `0` najveću pločicu, provjerava da na dnu stupca ostaje ista boja kao na praznom dijelu grafikona.
- U istim koordinatama nakon pozitivne promjene `400` bodova / `128` pločice mora biti vidljiva stvarna obojena traka.
- Provjerava da obje dodirne zone za promjenu mjere imaju visinu **najmanje 48**.
- Stvara **stvarnu** Android snimku `stats-zero-history-bars-941x1672.png`, obveznu u `scripts/verify-emulator-screenshots.py`.
- Po završetku vraća korisničku povijest, odabranu metriku i stranicu.

## Kriteriji prije spajanja

1. Sva tri workflowa trebaju završiti uspješno na **posljednjem HEAD-u**: Gradle/lint/test, Android 16 navigacija i emulator UI/screenshot.
2. Iz emulator QA-a preuzeti snimku i provjeriti prikaz obje metrike, pet arhivskih redaka, datuma, KPI kartica, dostupnosti tipki te svih 941×1672 rubova.
3. Ponovno pokrenuti `tools/visual_pixel_audit.py --screen Statistika --include-regions` s originalnim korisničkim PNG-om i **novim** stvarnim artefaktom. Bez artefakta i identične testne vrijednosti ne tvrditi novu MAE brojku ili 1:1.
4. Bez objave izdanja ili automatskog spajanja dok ne prođu svi kriteriji.

**Status:** kod i instrumentacijski testovi spremljeni u zasebnoj grani; završni emulator rezultat nije još potvrđen.
