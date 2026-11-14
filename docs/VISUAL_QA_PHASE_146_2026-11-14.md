# BROJKO — faza 146: korekcija preuranjeno spojenih vizualnih regresija

14. studenoga 2026.

## Zašto postoji ovaj hitni korektivni PR

PR #145 automatski je spojen dok je detaljni pixel audit
još tražio korekcije. U `main` commit
`6e65a7c002c3d4d2087877c4913960a582980e84`
ušao je **prvi**, mjerljivo lošiji eksperiment. Nakon spajanja
su se na staroj razvojnoj grani pojavile korekcije `36eb81f`,
ali nisu bile dio merge commita.

**Ovaj PR donosi ispravljene vizualne elemente**, ne dira
ostale izvorne gameplay i QA promjene #145.

## Izvor, mjerenje i razlog korekcije

Originalne korisničke PNG slike `22_15_15-6` (Dnevni izazov)
i `22_15_17-8` (Statistika), stvarna rezolucija 941×1672.
Baseline Android v1.12.244, stvarni emulator
[run #43539850459](https://github.com/bren-wp/Brojko/actions/runs/43539850459).
Prvi PR #145 emulatorski screenshot
[run #43541746829](https://github.com/bren-wp/Brojko/actions/runs/43541746829),
artefakt #14092685911.
Usporedba bez skaliranja/pomicanja ili lažnog rezultata.

| Stvarni izvorni ROI | v1.12.244 MAE | Regresijski PR #145 MAE |
| --- | ---: | ---: |
| 3D poklon cijeli, x90–290/y1300–1510 | **17,976349** | 18,158452 |
| Ljubičasto tijelo x95–287/y1400–1510 | **20,442551** | 20,737816 |
| Zlatne vrpce x100–265/y1400–1508 | **20,798709** | 21,026319 |
| Statistika grafikon x55–880/y790–1100 | **21,949750** | 22,098445 |
| Statistika stupci x55–880/y880–1080 | **26,296380** | 26,409895 |

RGB MAE (0–255) **nije postotak**.

## Odabrana korekcija i kontrolirana predikcija

**Poklon:** na prikazu iz stvarnog emulatora tamnija
ljubičasta facetna sjena bila je bolja iznad
**y=1460**, ali je ozbiljno kvarila donji dio.
Skraćivanje sjene na 52 % visine 400-unit
Canvas tijela, unutar izvorne siluete,
predviđa približno **−0,150** ukupnog poklon
ROI MAE prema v1.12.244. Stvarna slika
mora to ponovno potvrditi.

**Grafikon:** zajednička promjena koraljne i tirkizne
boje pogoršala je prikaz. Koraljna je vraćena
točno na v1.12.244; samo je izvorni tirkizni
pigment umjereno pomaknut (+5,+6,+6) prema
izvornoj RGB nijansi. Pixel-recolor kontrola
na istim vlasnikovim i emulator PNG-ovima
predviđa približno **−0,123** ukupnog grafikonskog
MAE prema v1.12.244.

Ove dvije vrijednosti su *predikcije*, a ne potvrđeni
rezultati novog SHA-a. Prihvat samo nakon
novog stvarnog emulatorskog mjerenja i
zelenih Gradle/Lint/unit/SCA, Android 16 i
UI instrumentacijskih testova.

## Očuvanje podataka

Ni jedna promjena ne smije:
- mijenjati `TileGame`, korisnički rezultat,
  povijest, sesiju, nagrade ili datum;
- dodavati demonstracijske stupce ili rezultate;
- pretvoriti funkcionalnu lokalnu kopiju u
  nepostojeću cloud sinkronizaciju;
- zahtijevati produkcijski keystore za novo izdanje.

Nakon uspješnog spajanja: obvezan **novi GitHub
Release APK + AAB** s testnim debug APK-om,
nepotpisanim release paketima, SHA256SUMS,
SBOM-ovima i 28 stvarnih Android snimki.
