# Brojko — faza 16: Misije i postignuća (31.10.2026.)

## Polazište

Otvoreni [PR #28](https://github.com/bren-wp/Brojko/pull/28), grana `feat/pixel-victory-modal-phase4`. Od deset vlasnikovih PNG referenci, **Misije i postignuća** imaju jedno od najvećih poznatih odstupanja, povijesni **RGB MAE 26.186** na izvornoj rezoluciji **941×1672**, bez skaliranja. To je povijesno mjerenje iz prethodnog emulator artefakta, **ne rezultat ove faze niti 1:1 dokaz**.

## Funkcionalna i vizualna regresija

Kartica **Majstor spajanja** opisuje **otključavanje** pločice 1024. Postojeći prikaz uz prethodno dosegnutu pločicu 512 imao je proturječje: brojčani brojač **0/1**, ali plava/ljubičasta traka bila je **50 % ispunjena** po omjeru `512 / 1024`. Korisnik nije otključao postignuće, a UI je prikazivao djelomičnu nagradu, za razliku od dva druga strogo binarna zadatka.

Ispravljeno je da traka postignuća prikazuje 0 % za `recordTile < 1024`, odnosno 100 % za `recordTile >= 1024`, dosljedno natpisima **0/1** i **1/1**. Ostali dnevni i tjedni zadaci ostaju proporcionalni stvarnim brojačima. Pravila igre, spremljeni rezultati, povijest i već dodijeljene nagrade nisu promijenjeni.

## Performanse i dead code

- Niz četiri numeričke pločice **2, 4, 8, 16** prebačen u ponovno korištenu statičku konstantu `MISSION_TILE_VALUES`.
- Dekorativne koraljne zrake prebačene u `MISSION_CORAL_RAYS` umjesto lokalnog `new float[][]` tijekom svakog `onDraw()` poziva.
- Nema novih mrežnih ovisnosti, rasteriziranih novih ikona, screenshot zamjena ni generiranih slika u chatu.

## Novi stvarni emulator dokaz

`missionMasterUnlockBarMatchesItsBinaryCounter()` na pravom Android rendereru provjerava:

1. `recordTile=0` i `recordTile=512` moraju imati **identičnu** neispunjenu traku postignuća (0/1).
2. `recordTile=1024` mora na istom mjestu imati drukčije obojenu, ispunjenu traku (1/1).
3. Samo renderiranje ne smije postaviti `rewardFlags` ili dodijeliti zvjezdice.
4. Reproducibilni kontrolirani fixture: 12/20 današnjih spajanja, 6250/10000 tjednih bodova, 5/7 dana, najbolji rezultat 8400, bez aktivne partije, bez izmišljenih otključanih nagrada.
5. Stvarni PNG 941×1672: `missions-phase16-binary-unlock-941x1672.png`. QA ga obvezno zahtijeva kroz `scripts/verify-emulator-screenshots.py`. Test vraća sve ranije stanje bez pisanja promijenjenih vrijednosti u korisničku pohranu.

Za usporedbu izvornog PNG-a s **novim** stvarnim emulator artefaktom:

```bash
python3 tools/visual_pixel_audit.py \
  --screen Misije --screen16-missions --include-regions \
  --reference-dir /path/to/owner-pngs \
  --artifact /path/to/Brojko-real-device-UI.zip
```

Program odbija dimenzije koje se ne podudaraju i ne pomiče niti skalira piksele. Izvorni PNG mora biti jedinstven unutar mape. **Nije izračunan novi MAE** prije dostupnosti zadnjeg stvarnog screenshot artefakta.

## CI stanje i ograničenja

Prethodni commit `b654a0fa` prošao je `Android pull request validation`; dva emulator workflowa tada su još bila u tijeku. Novi commitovi faze 16 imaju vlastita tri obvezna workflowa i potrebno je provjeriti **sve završne rezultate na istom zadnjem HEAD-u**. Ne tvrditi da je Android 16 ili UI audit zelen bez rezultata iz Actions.

Za konačno prihvaćanje potrebno je: uspješan Gradle/lint/unit/release PR validation, Android 16 gesture/3-button test, stvarni emulator QA i vizualna usporedba istog 941×1672 izvora. Prije spajanja PR mora ostati **Draft**, bez novog izdanja.

**Sljedeći zaslon nakon uspješnog ovog QA:** Statistika, zatim Teme i ponovno mjerenje svih devet dostupnih vlasnikovih PNG referenci.
