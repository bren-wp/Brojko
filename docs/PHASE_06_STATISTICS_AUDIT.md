# BROJKO — zaslon 6/10: Statistika

Otvoreni PR: [#28](https://github.com/bren-wp/Brojko/pull/28) (Draft, bez spajanja). **Originalna vlasnikova referenca**: `Slika ChatGPT 24. lis 2026. 22_15_17-8(20261030-210523).png` (**941 × 1672 px**). **Stvarni početni emulator PNG**: `golden-state-stats-941x1672.png` iz prethodno uspješnog Android screenshot artefakta `Brojko-daily-faa27860-qa.zip` (commit `faa27860`).

Oba PNG-a uspoređena su u izvornoj rezoluciji bez skaliranja, iskrivljavanja ili umjetnog poravnanja.

## Početne izmjerene razlike

| Izvorni ROI (x0,y0,x1,y1) | RGB MAE (niže je bolje) |
|---|---:|
| Cijeli zaslon (0,0,941,1672) | **25.410** |
| Zaglavlje (20,30,920,306) | 29.891 |
| Tri gornja KPI-ja (20,305,923,540) | 31.211 |
| Donja dva KPI-ja (20,535,920,760) | 33.443 |
| Grafikon (22,765,922,1160) | 36.705 |
| Povijest partija (20,1160,920,1645) | 13.306 |
| Naslov i stupci grafikona (65,790,885,1115) | 44.613 |

MAE je srednja apsolutna RGB razlika svakog piksela (0–255). To **nije** potvrda 1:1. Najveći neusklađeni detalj izvornog dizajna jest položaj i geometrija čitavog grafičkog bloka; prethodni emulator prikazuje grafikon približno 60–90 px previsoko. Logotip i organski 3D oblici još su umjetnički različiti od izvorne ilustracije.

## Izmjene na istom PR-u

- Gornja tri KPI bloka produžena su i spuštena, dva dodatna KPI-ja postavljena su niže kako bi raspored nalikovao izvornim karticama.
- Ikone krune, gamepada, mreže, zvijezde i plamena povećane su **samo na svijetlom portretnom zaslonu Statistika**.
- Brojčane vrijednosti statistike imaju veći font uz ograničenje da ne dodiruju rubove kartica.
- Dodane su tri jasne, zasebne koraljne dekorativne zrake sa svake strane naslova (bez promjene zajedničkog brendinga ostalih zaslona).
- Grafikon posljednjih deset dovršenih partija pomaknut je niže; zadržana je funkcionalna promjena **Bodovi / Najveća pločica**, s postojećim stvarnim povijesnim vrijednostima i kronološkim redoslijedom.
- Stupci koriste ublaženi gradijent, vrijednosti ostaju uz stvarne visine, legenda i dodirne zone prilagođene su novoj geometriji.
- Panel povijesti pomaknut je ispod grafikona i vizualno komprimiran kako bi svih pet rezultata stalo u izvorni prikaz 941 × 1672.
- Važno: pet redova i njihova mala visina nisu idealni za 48-dp dodirne ciljeve. **Za uključeno TalkBack/touch exploration** redovi se povećavaju na 48 logičkih jedinica te je dopušteno skrolanje, bez preklapanja djelatnih zona. Vizualna 1:1 referenca vrijedi samo za uobičajeni način prikaza.

## Podaci, interakcije, testovi

- Nema promjene `best`, `plays`, `total`, `recordTile`, kronologije i vremena završetka u `history()`, niti `chartMetric` izračuna.
- Broj osvojenih pobjeda koristi stvarno provjerene `won` oznake u povijesti. Ako stari zapis nema vrijeme, vrijeme se **ne izmišlja**.
- Maketa sadrži 10 povijesnih stupaca koji nisu posve međusobno konzistentni s prvih pet redaka povijesti. Stvarna aplikacija **uvijek** izračunava i grafikon i povijest iz istog arhiviranog skupa podataka, umjesto da prikazuje umjetnički izmišljene rezultate.
- Test `statisticsSourceLayoutRetainsRealChartAndHistoryActions` stvara privremeni fixture rezultata u emulatoru, provjerava 5 pravih detaljnih akcija, prebacivanje grafikona i zapisuje `stats-source-layout-941x1672.png`. Vraća ranije vrijednosti i preferences.
- Validator `scripts/verify-emulator-screenshots.py` zahtijeva novu screenshot datoteku 941 × 1672.
- `tools/visual_pixel_audit.py` podržava šest zasebnih regija za detaljnu kontrolu.

## Obavezna završna provjera

1. Gradle compilation, lint i unit testovi moraju proći za posljednji commit.
2. Android 16 edge-to-edge navigacija/insets i emulator QA moraju završiti uspješno.
3. Novi `golden-state-stats-941x1672.png` i `stats-source-layout-941x1672.png` usporediti sa source PNG-om bez reskaliranja, po šest gornjih regija.
4. Testirati 360 × 600 te 1080 × 2400, sva tri izgleda, ispravnost prebacivanja grafikona i pet povijesnih detalja.
5. Sve lokalne regresije popraviti ili vratiti; nedovršene testove ne nazivati uspješnim.

**Stanje:** radna implementacija, **1:1 još nije potvrđeno**, PR ostaje Draft. **Sljedeći zaslon po redu: 7/10 — Teme i izgled.**
