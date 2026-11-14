# BROJKO — faza 150: svih devet izvora i 3D rub gumba Naslovnice

Datum: 14. studenoga 2026. Polazište `main`:
`8467a49c6fe4ea24922d544437199c33352cfd8f`, stvarni
[Android emulator run #43555064369](https://github.com/bren-wp/Brojko/actions/runs/43555064369),
artefakt `Brojko-real-device-UI` ID `14098720489`,
izdanje [v1.12.249](https://github.com/bren-wp/Brojko/releases/tag/v1.12.249).

## Izvor istine i način mjerenja

Devet originalnih vlasnikovih PNG referenci od
`Slika ChatGPT 24. lis 2026. 22_15_06-2` do
`22_15_19-10` (jedina slika `22_15_05-1` je zasebna
brend ilustracija, nije Android portretni zaslon).
Usporedba svih PNG-ova: originalni RGB **941 × 1672**
nasuprot stvarnom emulator PNG-u iste rezolucije.
**Nema promjene veličine, poravnanja, umjetnih podataka,
generiranja referentnih slika ni optičkog uklanjanja razlika.**

RGB MAE je prosjek apsolutne pogreške po kanalu (0–255);
**nije postotak odstupanja**.

| Ekran | RGB MAE v1.12.249 |
| --- | ---: |
| Splash | 10.761475 |
| Naslovnica | **12.405045** |
| Klasično 4×4 | 9.532597 |
| Pobjeda | 13.721977 |
| Dnevni izazov | 13.654453 |
| Misije i postignuća | 13.325021 |
| Statistika | 12.838262 |
| Teme i izgled | 13.000179 |
| Postavke | 10.496738 |

**Ni jedan prikaz još nije 1:1.** Nisu napravljene lažne
korekcije rezultata iz povijesti, dnevnog datuma ni vrijednosti
u statističkom grafikonu. Za vjerno *funkcionalno* sučelje
stvarni podaci mogu se razlikovati od referentnog primjera.

## Najveći statički uzorak — Naslovnica / Nova igra

| ROI u izvornim pikselima | Početni RGB MAE |
| --- | ---: |
| CTA s ukrasnim linijama (100,665,850,900) | 12.686977 |
| Gumb s obrubom (135,683,810,880) | **12.215564** |
| **Gornja reljefna zona** (180,686,770,711) | **15.200045** |
| Ploča gumba bez teksta (200,713,755,739) | **1.403881** |
| Donji reljef/lip (180,851,755,880) | **13.634423** |
| Natpis i okolna zona (325,743,745,850) | **16.053553** |

Oba dijela ploče gumba imaju približno ispravne osnovne
pigmente (MAE 1.4), ali gornji obrub i završna sjena
izgledaju ravnije i odaju razliku. **Ne mijenjati cjelokupan
koraljni gradijent**: time bi se već kalibrirana sredina
ponovno pogoršala.

## Uski zahvat

Samo za `page=home`, `theme=0`,
`referencePortraitLayout=true` u `MainActivity.BoardScreen`:

- Dodan je postupno proziran topli sjaj visine 9 logičkih
  jedinica u `ownerHomeHeroGradient()` bez novih slika.
- U `button()` je lokalno ublažen previše bijeli
  kontur (1.2 → 0.95 jedinica, alpha 0x86 → 0x56).
- **Ne diraju se** 284 × 82 logičke dimenzije CTA gumba,
  pozicija, njegov hitbox, klik akcija, natpis, ikona,
  animacija, tamna/živopisna tema, dnevni izazov, pobjeda,
  TileGame, arhiva ni backup shema.

## Zaštita svih 254 stvarnih screenshot stanja

Referentni GitHub Actions ZIP sadrži **254** PNG-ova, uključujući
različite teme, sekundarne zaslone, modale, veći i manji viewport.
Novi skript `scripts/compare-emulator-galleries.py` uspoređuje
**svaku** PNG datoteku iz prethodnog i novog stvarnog
`Brojko-real-device-UI` artefakta. Usporedba provjerava
postojanje, izvorne dimenzije, SHA-256 i izravni RGB MAE.
Nikada ne uklanja stvarne razlike pomicanjem/slikanjem.

```sh
python3 scripts/compare-emulator-galleries.py \
  --baseline "/private/BROJKO-v249-emulator.zip" \
  --candidate "/private/BROJKO-pr150-emulator.zip" \
  --report "/private/BROJKO-pr150-galleries.json"
```

Usporedba dva **različita** emulator QA pokretanja za
potpuno isti PR #149 kod pokazala je 200 byte-identičnih
od 254 PNG-ova; 54 se razlikuju zbog vremenskih/testnih
stanja i stvarnih podataka. Takva varijabilnost
**nije razlog za fabriciranje** rezultata ili prikrivanje
prave regresije; dodatno ručno pregledati sve razlike
izvan ciljane Home ilustracije i odvojiti dinamičke
od stvarnih vizualnih odstupanja.

## Obvezno potvrditi na novom PR SHA-u

1. Android Gradle build, Lint, unit testovi, CVE/SBOM.
2. Android 16 oboje navigacije i edge-to-edge QA.
3. Svi emulatorski instrumentacijski testovi, screenshot QA
   i prilagodba ekrana na 941×1672, 360×800 i 1080×2400.
4. Preuzeti i usporediti novi stvarni screenshot s istim
   vlasnikovim PNG-om i istim ROI koordinatama.
5. Zadržati promjenu **samo ako** gornji reljef 15.200045
   i/ili cijeli gumb 12.215564 poboljšaju rezultat
   **bez** pogoršanja cijelog Home zaslona 12.405045.
   U suprotnome prilagoditi ili vratiti eksperiment.

CLI za mjerljiv audit:
```sh
python3 -m pip install numpy Pillow
python3 scripts/measure-owner-pixels.py \
  --references "/private/brojko-owner-pngs" \
  --capture-zip "/private/BROJKO-current-real-emulator.zip" \
  --report "/private/brojko-home-qa.json"
```
Privatne reference ostaju izvan javnog repozitorija.

## Otvoreni prioriteti nakon Naslovnice

- Pobjeda, Dnevni izazov i Misije: 3D kruna, poklon,
  ikone, nagrade, sjene i tekst.
- Statistika: pravi spremljeni podaci i statička
  geometrija grafikona; ne prepisivati podatke radi
  umjetnog snimkovnog podudaranja.
- Teme i Postavke: sve kontrole, svijetli/tamni/
  živopisni prikaz, uvoz/izvoz, bez nepostojeće
  cloud sinkronizacije.
- Ostali modalni i sekundarni zasloni (ukupno 28 u
  emulator QA): potrebno zasebno osigurati njihove
  vlasničke reference prije tvrdnje o 1:1.
