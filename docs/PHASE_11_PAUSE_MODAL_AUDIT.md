# Brojko — dodatna QA faza 11: Pauza i nastavak (30. listopada 2026.)

## Kontekst

Nastavak u **istom otvorenom PR-u #28**, na grani
`feat/pixel-victory-modal-phase4`, nakon deset glavnih zaslona.

Koristi se postojeći vizualni sustav Brojko: toplu krem karticu,
koraljnu/tirkiznu paletu, Nunito tipografiju, reljefne ikone,
meke sjene i radijuse. **Nije priložen vlasnikov zasebni Pause PNG.**
Stoga ovaj korak nije dokaz o izvornom dizajnu 1:1 i nije korektno
izmišljati RGB MAE.

## Stvarni problem pronađen i ispravljen

- Prethodni `pauseOverlay` sadržavao je jednostavnu karticu od y=255
  do y=625 sa statičnim tekstom i dvije akcije.
- Kod `onDraw` pritom je za `page="pause"` računao
  `contentHeight=880`, što je na originalnom portretnom omjeru
  941×1672 stvaralo **oko 170 nepotrebnih logičkih jedinica scrolla**.
  Modal nije bio izvan viewporta, ali je scroll uvodio nestabilno
  pozicioniranje i nepotrebno pomicanje igre ispod njega.
- Za portretni zaslon Pauze koristi se `contentHeight=650`,
  što odgovara posljednjem rubu modala na y=643; na referentnoj
  rezoluciji i na 360×600 nema dodatnog scrolla.

## UI/UX provedba

- `pauseOverlay()` sada crta overlay s karticom od y=229 do y=643,
  s prepoznatljivom reljefnom koraljnom ikonom pauze.
- Naslov: **Igra je pauzirana**; pomoćni tekst:
  **Nastavi kad budeš spreman.**
- Stvarni `game.score` prikazuje se u posebno povišenoj
  kartici sa statističkom ikonom. Ne koriste se fiksni testni brojevi
  u produkcijskoj aplikaciji.
- Funkcionalni gumbi **Nastavi** (koraljni, y=483..549) i
  **Izbornik** (neutralni, y=561..622) imaju odvojene dodirne zone,
  bez preklapanja ili iza zaslona.
- Pozadinski prikaz partije ostaje zamračen i zamrznut, a
  `taps.clear()` prije modala uklanja pristup pozadinskim akcijama.
- Postojeće stanje igre, polja pločica, broj poteza, rezultat
  i spremanje partije se ne mijenjaju.

## Regression testovi

`pauseModalKeepsTheRealGameAndUsesOnlyTwoSafeActions` provjerava:
- Responzivnost **941×1672**, **360×600** i **1080×2400**.
- Točno dvije aktivne akcije modala, bez pozadinskih hit zona.
- Odsutnost nepravilnog scrolla, dodirne zone visine najmanje
  60 logičkih jedinica, bez preklapanja i izvan viewporta.
- Stvarnu promjenu prikazanog rezultata nakon promjene
  `TileGame.score` (samo testni podaci).
- `Nastavi` vraća u igru s istim rezultatom, potezima
  i rasporedom pločica; `Izbornik` vraća na početni zaslon
  bez brisanja ili arhiviranja aktivne partije.
- Novu stvarnu screenshot snimku:
  `pause-branded-modal-941x1672.png`.
  `scripts/verify-emulator-screenshots.py` zahtijeva PNG
  dimenzija 941×1672.

## Ranija CI regresija obuhvaćena istim PR-om

U [Android UI QA #41139952329](https://github.com/bren-wp/Brojko/actions/runs/41139952329)
test iz faze 07 `themesPhaseSevenRendersOwnerLayoutAndKeepsRealPreferences`
pao je s porukom **Dark preview uses navy material**.
Uzrok je pogrešan testni pixel: (158,188) pada na
zaobljeni transparentni kut kartice (x=152, y=183, radius=17),
a ne na tamnoplavu neprozirnu plohu.

Test sada uzorkuje unutarnje mjesto (195,189), unutar
tamne podloge i iznad 2×2 malih pločica. Produkcijski
renderer nije bilo potrebno dodatno mijenjati.

## QA kriteriji prije prihvata

1. Na **najnovijem HEAD-u**: sva tri obvezna GitHub Actions
   workflowa (build/lint/unit, Android 16 edge-to-edge i
   emulator UI screenshot) moraju biti uspješna.
2. Tek nakon izvođenja testa preuzeti novu stvarnu
   `pause-branded-modal-941x1672.png` i vizualno provjeriti
   kontrast, centriranje, natpise, kartice, ikone, sadržaj,
   pozadinsku igru i rubove.
3. Bez izmišljanja referentnog RGB MAE za Pause: izvorni
   screenshot vlasnika nije dostavljen.
4. Držati PR kao **Draft**, bez mergea ili objave releasea
   dok se sve provjere i rezultati ne dovrše.

**Status:** ciljani kod/testovi pripremljeni i spremljeni;
pixel-perfect 1:1 nije potvrđen.
**Dalje:** provjera svih CI rezultata i preostalih
modalnih stanja `confirm`, `confirmEnd`, `over`,
`dailyWin` i `dailyFail`.
