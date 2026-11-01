# BROJKO — faza 19: točan prikaz praznih metrika u Statistici

Datum: 1. studenoga 2026.  
Grana: `fix/stats-empty-metrics-20261101`  
Polazište: `main` na `a1b827e2c2a6cccdb335b58a8b9bb21186ac03d6` nakon spojenog PR-a #30.

## Utvrđeni problem

Prethodni renderer provjeravao je samo je li arhiva potpuno prazna. Kada postoje dovršene partije, ali je svih posljednjih deset vrijednosti odabrane metrike jednako nuli, korisnik je vidio os i oznake partija bez stupaca **i bez objašnjenja**. Takav izgled bio je nejasan nakon ranijeg popravka koji je ispravno uklonio lažne stupce za nulu.

## Stvarne promjene

- `statisticsChartMaximum()` računa nenegativan maksimum za odabranu metriku i dijeli se između referentnog kompaktnog i produženog prikaza.
- `count == 0`: ostaje postojeći tekst „Grafikon će se prikazati nakon prve završene partije.”
- `count > 0 && maximum == 0`: prikazuje se „Nema zabilježenih bodova.” ili „Nema zabilježenih pločica.” prema stvarnom izboru.
- Pozitivne vrijednosti, relativne visine i boje stupaca, položaji KPI kartica, povijest, vrijeme, globalni rekordi i formati postavki nisu izmijenjeni.
- Naslijeđeni negativni podaci ne mogu povećati maksimum niti proizvesti lažno pozitivnu traku; izvorni se arhivski JSON ne mijenja.
- Prethodna zaštita od stupaca za vrijednost nula, povećane dodirne zone, Splash od 3200 ms i strelica Natrag u Postavkama ostaju netaknuti.

## Regresijski QA

Instrumentacijski test `statisticsEmptyAndZeroOnlyMetricsKeepHistoryAccessible` provjerava:

1. Stvarni Canvas renderer na 941 × 1672 i 360 × 600.
2. Svijetlu i tamnu temu, Bodove i Najveću pločicu.
3. Da dva zapisa s nulom i dalje imaju dvije funkcionalne akcije „Detalji partije” i oba prekidača grafikona.
4. Da prazna arhiva nema lažne poveznice prema povijesti.
5. Da negativne naslijeđene vrijednosti ne stvaraju pozitivne stupce; vrijednost `1` ostaje valjana.
6. Da izvorna povijest, tema, stranica i odabrana metrika budu vraćene nakon testa.

Validator očekuje **stvarne Android PNG snimke**, bez skaliranja:
- `stats-zero-only-scores-941x1672.png`
- `stats-zero-only-tiles-941x1672.png`
- `stats-no-history-941x1672.png`

## Vizualni audit / ograničenja

Povijesni RGB MAE za Statistiku iz PR-a #29 iznosio je **21,287** (941 × 1672). **Nije** rezultat ove faze i ne smije se koristiti za usporedbu različitih fixturea. Nove PNG snimke valja preuzeti iz **uspješnog** Android UI smoke and screenshot QA workflowa na posljednjem HEAD-u i ručno provjeriti KPI kartice, oba grafa, tipografiju, kartice povijesti, datume i rubove.

Izvorni vlasnikov PNG `Slika ChatGPT 24. lis 2026. 22_15_17-8(20261030-210523).png` nije prisutan u aktualnim GitHub izvorima. Bez ponovnog pribavljanja vlasnikove izvorne reference nije moguće provesti novu valjanu RGB MAE usporedbu niti tvrditi da je rezultat 1:1. Za usporedbu treba koristiti **identičan skup stvarnih podataka** ili razliku podataka jasno odvojiti od vizualnih promjena.

## Status prihvaćanja

Draft PR se **ne smije spojiti** bez uspješnih workflowa `Android pull request validation`, `Android 16 navigation bar and inset QA` i `Android UI smoke and screenshot QA` na posljednjem commitu, pregleda snimki i vlasnikove izvorne PNG reference za pravi pixel audit. **Nema objave novog izdanja.**

Sljedeći zaslon za zasebno detaljno vizualno ispitivanje: **Teme i izgled**, nakon što su regresije Statistike razriješene.
