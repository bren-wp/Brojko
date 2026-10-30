# Faza 4/10 — Pobjednički modal „Bravo!”

Referenca: vlasnikova izvorna slika `14-5` (941 × 1672 px), bez skaliranja. Uspoređeni su stvarni PNG-ovi iz GitHub Actions emulatora (run #41029836723, izdanje v1.12.116) s dostavljenim izvornim PNG-om.

## Polazne vrijednosti — release v1.12.116

| ROI (pikseli, left/top/right/bottom) | RGB MAE (niže je bolje) |
|---|---:|
| Cijeli zaslon (0,0,941,1672) | 22.302 |
| Pobjednički modal (135,450,805,1415) | 32.563 |
| Kruna (255,330,650,570) | 27.972 |
| Naslov (210,525,700,770) | 56.022 |
| Akcijski gumbi (165,965,775,1370) | 32.920 |
| Pozadina (20,100,890,420) | 17.316 |

Usporedba: Pillow ImageChops RGB apsolutna razlika, srednja vrijednost za R, G, B, izvorna rezolucija bez crop-a za poravnanje ili resamplinga. MAE nije dokaz doslovne 1:1 jednakosti jer izvorna ilustracija sadrži umjetničke teksture.

## Vizualni ispravci u PR #28

- Za pobjedu u referentnom portretnom omjeru koristi se izvorna geometrija `x=59, y=200, w=282, h=397` u projektnih 400 jedinica.
- Naslov se crta individualnim Nunito glifovima uz blago različite nagibe, bijeli obrub i akcentne zrake.
- Kruna se prikazuje kao prava vektorska grafika s toplim gradijentom, donjom usnom, odsjajem i suptilnom sjenom.
- Koraljni CTA i plavi gumb za dijeljenje imaju ciljano podešene boje reljefa; sekundarni gumb za novu igru topliju krem površinu.
- Pozadinska ploča prikazuje stvarnu završenu partiju, a ne marketinšku bitmapu.

## Funkcionalni ugovor

- **Nastavi igrati:** koristi istu instancu igre, označuje `winAcknowledged` i trajno sprema stanje.
- **Nova igra:** postojeći `newGame` najprije sigurno arhivira trenutačnu partiju; ne briše rezultat prije potvrde spremanja.
- **Podijeli rezultat:** koristi Android `ACTION_SEND` s aktualnim brojem bodova.
- Govorni čitač ekrana dobiva „Bravo!” i stvaran rezultat pri prvom dosezanju 2048.
- Izmjene ne uvode screenshot kao pozadinu ili lažne podatke.

## Reproducibilni QA

1. Pokrenuti GitHub Actions **Android pull request validation**, **Android UI smoke and screenshot QA** i **Android 16 navigation bar and inset QA**.
2. Iz emulator artefakta preuzeti `golden-state-win-941x1672.png` i druge referentne prikaze. Za stabilan pregled stanja koristiti postojeće testove `portraitVictoryModalAlignsWithGoldenGeometryAndNeutralBackdrop` i `portraitVictoryReferenceHasThreeReachableRealActions`.
3. Ponoviti usporedbu istim `tools/visual_pixel_audit.py` uz `--reference-dir`, `--artifact`, `--baseline-artifact` i `--include-regions` kada su obje snimke dostupne; evidentirati i regionalne rezultate.
4. Ako se MAE modala poveća ili testiranje bude neuspješno, ispraviti novu implementaciju prije spajanja.

**Status:** PR #28 je radni PR; rezultati QA-a nakon izmjena upisuju se tek kad ih potvrdi stvarni emulator. Objavljeni release ostaje v1.12.116 dok svi uvjeti nisu ispunjeni.
