# BROJKO — faza 148: izmjerena korekcija kartice Živopisna

Datum: 14. studenoga 2026. Polazište: `main` SHA
`63832d746d685d1ccc37070185e18b33d1e0a880`, izdanje
[v1.12.247](https://github.com/bren-wp/Brojko/releases/tag/v1.12.247).

## Izvorni dokazi i ograničenja

Osobne vlasnikove slike iz privatne knjižnice:
`Slika ChatGPT 24. lis 2026. 22_15_06-2` do `22_15_19-10`,
bez landcape brend ploče `-1`. Stvarni Android API 35
emulatorski PNG artefakt `Brojko-real-device-UI`, ID
`14095782673`, uspješni
[run 43549460298](https://github.com/bren-wp/Brojko/actions/runs/43549460298).

Usporedba izvornih i emulatorskih `941x1672` RGB slika:
**nema resizea, pomicanja, rotacije, crop-poravnanja ni zamjene
stvarnih podataka u igri.** Srednja apsolutna pogreška po RGB kanalu
(`MAE`) ima vrijednost 0–255; to **nije postotak**.

| Zaslon | v1.12.247 RGB MAE |
| --- | ---: |
| Splash | 10.761475 |
| Naslovnica | 12.405045 |
| Klasično 4×4 | 9.532597 |
| Pobjeda | 13.721977 |
| Dnevni izazov | 13.654453 |
| Misije i postignuća | 13.331780 |
| Statistika | 12.838262 |
| Teme i izgled | 13.159406 |
| Postavke | 10.491908 |

Podaci su iz aktualnog emulatorskog ZIP-a, a ne iz starih
QA postotaka ili iz vizualne procjene.

## Uski zahvat: kartica Živopisna

Izvor `-9` vs `themes-original-final-aligned-941x1672.png`.

| ROI (x0,y0,x1,y1) | Baseline MAE |
| --- | ---: |
| Tri kartice `(32,370,905,790)` | 18.6374 |
| Živopisna `(635,393,900,672)` | 19.9829 |
| Lijeva pozadina `(645,405,685,655)` | 16.7824 |
| Desna pozadina `(855,405,890,648)` | 17.5269 |
| Ploča pregleda `(30,820,500,1220)` | 15.8073 |

Neovisno *istraživanje geometrije* na originalnim RGB slikama
pokazuje da dio Živopisne kartice ima najmanju pogrešku kada
je emulatorska ilustracija pomaknuta približno 2–3 **fizička
piksela ulijevo**, lica 2×2 pločica približno 3 piksela
gore, znamenke približno 5 piksela gore, a natpis oko 6
piksela gore. Ta pretraga ne predstavlja novu mjernu vrijednost
Android rendera i ne smije zamijeniti stvarno precrtavanje.

U `themesCompact()` samo za `theme=0`,
`referencePortraitLayout` i karticu `i=2` korigirani su
Canvas prijevod, položaj 2×2 ilustracije, znamenke i natpis.
**Hitbox kartice, radio oznaka, sva druga dva prikaza teme,
stvarni TileGame, 4×4 i 5×5 gameplay, spremanje i povijest
ostaju nepromijenjeni.**

## Provjera novog SHA-a — obvezno prije spajanja

1. `Android pull request validation` mora proći build, Lint,
   jedinice i SCA.
2. `Android 16 navigation bar and inset QA` mora ostati zelen.
3. Puna emulatorska instrumentacija i stvarni PNG dokaz moraju
   biti uspješni na **istom** SHA-u.
4. Ponoviti izvornu 941×1672 usporedbu s novim stvarnim
   `Brojko-real-device-UI` artefaktom. Nije dopušteno
   prijaviti hipotetski ili pomaknuti screenshot kao rezultat.
5. Prihvatiti korekciju samo ako ciljani ROI Živopisne kartice
   stvarno padne ispod 19.9829 bez regresije većeg područja;
   inače vratiti promjenu ili nastaviti mjerenu korekciju.

Reproducibilan lokalni alat, **bez uključivanja privatnih
korisničkih PNG-ova u Git**:

```sh
python3 -m pip install numpy Pillow
python3 scripts/measure-owner-pixels.py \
  --references "/private/originali" \
  --capture-zip "/private/BROJKO-v247-emulator.zip" \
  --report "/private/brojko-v247-rgb.json"
```

Alat nameće izvorne dimenzije i strogo fiksne nazive devet
prikaza, a zapisuje i potpuno iste RGB piksele kao postotak.
Tek stvarni emulatorski rezultat nakon izmjene može potvrditi
napredak. Nema tvrdnje o 0% odstupanja.
