# BROJKO — faza 151: 3D kruna Pobjede i poštena mjerna osnova

Datum: 15. studenoga 2026. Polazište `main`: `efe62fea18951d4c553ba0d1e59c18e30f68334c`.
Provedena je inspekcija aktualnog Java Canvas koda, instr. testova i zadnjih QA workflowa. Promjene u ovoj fazi **ne precrtavaju krunu** i ne modificiraju gameplay.

## Zatečeno

- `MainActivity.BoardScreen.victoryCrown()` crta skalabilnu 3D zlatnu krunu.
- `victoryCrownGoldRays()` dodaje tri zlatne zrake izvan siluete u izvornom svijetlom portretu.
- `compactVictoryCard()` zadržava žive gumbe Nastavi igrati, Nova igra i Podijeli rezultat.
- Testovi `OwnerVictoryGoldRaysPhase128Test` i `OwnerStatsVictoryPhase130Test` osiguravaju nativne uzorke piksela i funkcionalne akcije.
- Prethodno prijavljeni RGB MAE područja krune: **24,1629** (povijesna vrijednost iz prethodne korisnikove provjere, **nije ponovno izmjerena na ovom PR-u**).
- Izvorni vlasnički PNG iz listopada nije među datotekama izvornog repozitorija; ne smije se zamijeniti umjetno generiranom referencom.

## Novo mjerljivo poboljšanje QA alata

`scripts/measure-owner-pixels.py` sada mjeri šest zasebnih pobjedničkih ROI-a, sve u nativnim koordinatama 941 × 1672:

| ROI | (x0,y0,x1,y1) |
|---|---|
| 3D kruna | (300,340,630,520) |
| Vrh i gornji rub krune | (370,365,565,455) |
| Zlatne zrake krune | (330,485,615,570) |
| Natpis Bravo | (210,525,700,770) |
| Kartice rezultata | (160,795,760,974) |
| Tri aktivne akcije | (160,965,790,1390) |

`--screen 5` omogućuje audit isključivo Pobjede, bez umjetnog uvjeta da je dostupno svih devet referenci. Dodani su sintetički JVM-neovisni Python regresijski testovi metrika; sintetički PNG-ovi **nisu i ne predstavljaju** vlasničku referencu.

Primjer (datoteke ostaju izvan repozitorija):

```sh
python3 -m pip install numpy Pillow
python3 scripts/measure-owner-pixels.py \
  --references "/private/brojko-owner-pngs" \
  --capture-zip "/private/BROJKO-pr151-emulator.zip" \
  --screen 5 \
  --report "/private/BROJKO-pr151-victory.json"
```

Nakon nove provjere treba usporediti s istom vlasničkom referencom i istim ROI-jem prije izmjene Canvas krune. Zadržati samo korekcije koje smanjuju RGB MAE i ne izazivaju druge vizualne ili funkcionalne regresije.

## QA ograničenja

Zadnji provjereni `main` emulator workflow: https://github.com/bren-wp/Brojko/actions/runs/43641352379. Usporedba svih **254** screenshot stanja između **novog PR artefakta** i starog ZIP-a nije dovršena ovom statičkom izmjenom. Ne postoji verificirana nova MAE vrijednost niti se može potvrditi 1:1.

GitHub `v1.12.250` release sadrži 28 stvarnih PNG-ova i unsigned preview pakete, ali je `prerelease=false`; workflow je dobio eksplicitnu provjeru i korekciju ove klasifikacije za buduća izdanja.
