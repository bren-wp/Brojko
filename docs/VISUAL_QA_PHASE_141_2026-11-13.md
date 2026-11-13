# BROJKO — faza 141: Teme, Postavke, Dnevni izazov i Statistika

Datum: 13. studenoga 2026.

## Referentni dokaz

Uspoređeni su originalni vlasnikovi 941×1672 PNG-ovi
`Slika ChatGPT 24. lis 2026.` i neizmijenjeni stvarni Android
emulatorski PNG iz `Brojko-real-device-UI` za **v1.12.240**
(PR #140, workflow #43519208645, SHA `3f2713650541...`).
Isti pixel-grid, RGB MAE 0–255; **bez** resizea, registracije,
poravnanja, usklađivanja dinamičkih brojeva ili generiranja slika.

| Zaslon | Cijeli ekran — MAE v1.12.240 | Najveći pregledani ROI — MAE |
|---|---:|---|
| Teme i izgled | 13,201 | Oznake stilova x470–900/y970–1040: 28,448 |
| Postavke | 10,852 | Backup kontrola x725–872/y853–959: 42,723 |
| Dnevni izazov | 13,763 | Tekst nagrade x365–860/y1340–1490: 25,753 |
| Statistika | 12,959 | Grafikon/stupci x55–880/y880–1080: 26,606 |

Dodatni ROI:

- Teme: sve kartice 18,637; style picker 16,589;
  pregled pločica 15,538; donja navigacija 13,300.
- Postavke: backup cijeli red 20,815; backup ikona 19,867;
  ikona Zvuk 21,651; obavijesti 15,214.
- Dnevni izazov: nagradni panel 20,105; gift artwork 23,548;
  ciljevi 18,710; glavna akcija 10,544.
- Statistika: grafikon cijeli 19,356; zaglavlje grafikona 15,052;
  povijest 14,992.

**MAE nije postotak odstupanja.** Izvorni mockup ima ilustrativne
streak/score/history vrijednosti i funkcionalno nepostojeći
cloud-sync prekidač. Produkcijski UI zadržava **stvarne** korisničke
rezultate, datum, aktivnu sesiju i lokalni backup bez lažne cloud
funkcionalnosti. Razlike tih područja ne smiju se prikriti lažnim
podacima ili statičnom slikom.

## Ograničene vizualne korekcije

1. **Teme:** samo u vlasničkom svijetlom referentnom portretu,
   topliji/dublji izvorni gradijent „Zaobljene” mini pločice i
   suptilna sjena mini „Ravne” pločice. Ne mijenja `TileGame`
   niti stvarne pločice ili dodirne zone.
2. **Postavke:** samo svijetli vlasnički portret, smanjena bijela
   glazura na koraljnim ikonama i prilagođen sjaj lokalne backup
   ikone, bez promjene funkcionalnosti. „Sinkronizacija napretka”
   ostaje iskren lokalni backup i ne postaje lažni cloud prekidač.
3. **Dnevni izazov:** isprobana je korekcija proporcije
   ljubičastog darovnog tijela, ali je **vraćena** nakon stvarnog
   emulatorskog mjerenja: lokalni gift ROI povećao je MAE s 23,548
   na 27,100, a cijeli zaslon s 13,763 na 13,815.
   Stara, dokazano bolja geometrija ostaje aktivna. Kriteriji nagrada,
   streak i aktivna partija nisu mijenjani.
4. **Statistika:** samo ilustracija stupaca u svijetlom vlasničkom
   portretu koristi izvornije koraljne/teal pigmente. Ne mijenja
   podatke, redoslijed povijesti, visine stupaca, datume, maksimalni
   rezultat, broj partija ni funkcionalnost tabs.

## Testovi i kriteriji kvalitete

- `FourScreenVisualProgressPhase141Test`: 4 ekrana × 3
  rezolucije 941×1672 / 360×600 / 1080×2400; pravilo:
  ne smiju se promijeniti JSON aktivne sesije, najbolji rezultat,
  stil, tema, zvuk, vibracija, animacija i aktivni gumbi.
- Obvezan zeleni PR build + Lint + unit + SCA; Android 16
  edge-to-edge/navigacija; cjelokupni emulator screenshot QA.
- **Prije spajanja** ponovno preuzeti screenshotove zadnjeg SHA
  i izračunati svaki gore navedeni ROI, uz cijele zaslone.
  Regresiju u bitnom lokalnom području treba vratiti ili korigirati.
- Novo izdanje APK + AAB nakon mergea obvezno; unsigned
  preview + jasno označeni testni `app-debug.apk` dopušteni.

Ovo je iteracija prema 1:1, a ne tvrdnja da je ostvarena potpuna
pikselna podudarnost ili da je izvršen test nadogradnje potpisanog
Play paketa na fizičkom uređaju.

## Rezultat prvog kontrolnog mjerenja prije vraćanja Daily poklona

Izvorne reference su **iste** kao baseline. Nova snimka:
[UI QA run #43523373558](https://github.com/bren-wp/Brojko/actions/runs/43523373558),
artefakt `Brojko-real-device-UI` ID `14084578273`, PR SHA
`413c7494a524eecf327b927208d069ac42317800`.
Stvarne izvorne PNG slike i dvije stvarne emulatorske arhive,
bez interpolacije ili registracije.

| Zaslon / ROI | v1.12.240 | Prvi rezultat | Razlika |
|---|---:|---:|---:|
| Teme cijeli | 13,201 | **13,159** | −0,042 |
| Teme mini „Zaobljene” | 23,333 | **20,324** | −3,009 |
| Teme mini „Ravne” | 13,119 | **11,602** | −1,517 |
| Postavke cijeli | 10,852 | **10,763** | −0,089 |
| Postavke ikona Zvuk | 21,651 | **19,266** | −2,385 |
| Postavke backup ikona | 19,867 | **19,047** | −0,820 |
| Statistika cijeli | 12,959 | **12,913** | −0,046 |
| Statistika grafikon | 19,356 | **19,130** | −0,226 |
| Statistika stupci | 26,606 | **26,296** | −0,310 |
| Dnevni izazov cijeli | **13,763** | 13,815 | **+0,052, regresija** |
| Dnevni poklon | **23,548** | 27,100 | **+3,552, regresija** |

Na sljedećem SHA-u `00cb505196f0d5c5efb5ec56a0bf2d5a01632fbd`
izričito je **vraćena isključivo promjena geometrije poklona**,
bez poništavanja lokalnih poboljšanja ostala tri zaslona.
Budući da je revert promjena koda, za završno prihvaćanje
potrebni su novi zeleni CI/emulator rezultati zadnjeg SHA-a.

Nije primjereno tvrditi 1:1 ili 0% odstupanja; preostali ROI
su dokumentirani za sljedeće iteracije.
