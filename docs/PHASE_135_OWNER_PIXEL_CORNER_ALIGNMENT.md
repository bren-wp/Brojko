# BROJKO — faza 135: originalni pikseli najodstupnijih zaslona

Datum: 12. studenoga 2026. Cilj: originalnih 941×1672 piksela bez interpolacije. 
Ova faza ispravlja samo **izvorno uspoređene dekorativne elemente**. Interaktivnost i stvarni napredak ostaju nepromijenjeni.

## Polazna referenca i metode

Originalni vlasnikovi PNG-ovi `Slika ChatGPT 24. lis 2026. 22_15_13-3` (Home), 
`...22_15_16-7` (Misije) i `...22_15_18-9` (Teme), svi 941×1672.
Stvarni emulatorski prikazi iz uspješnog UI QA v1.12.216
[run 43127186416](https://github.com/bren-wp/Brojko/actions/runs/43127186416).
Izvori su nezavisni; korisničke reference **nisu** pridodane produkcijskom APK-u.

| Zaslon | Polazni RGB MAE (0–255) | Udio potpuno jednakih RGB piksela |
|---|---:|---:|
| Misije i postignuća | 13,9238 | 1,34 % |
| Naslovnica | 13,8979 | 3,52 % |
| Teme i izgled | 13,7633 | 4,37 % |

Navedeni MAE **nije postotak odstupanja**; nula znači doslovno identične RGB kanale za sve piksele. 
Različiti dinamički rezultati, periodi, završenost postignuća, sistemski fontovi i reljef materijala ostaju otvoreni.

## Pojedinačna mjerenja

- Home lijeva plava dekoracija **8**: referentni centar obojenih piksela oko (55,330); 
  prethodni stvarni emulator oko (43,424) — 94 px prenisko.
- Home desna žuta dekoracija **16**: referentni centar oko (873,372); 
  prethodni emulator oko (893,429) — 57 px prenisko i 20 px previše desno.
- Plava dekoracija u ROI `x=0..155,y=220..470`: referenca 15.436 zasićenih plavih piksela,
  emulator 8.972. Zlatna u `x=760..941,y=220..510`: 20.906 nasuprot 13.031.
- Misije, strelica Natrag, tamna tinta u ROI `x=45..125,y=58..130`:
  referenca 827 piksela, prethodni Android 293; uzrok je generički tanki chevron bez vodoravnog dijela.

## Implementirana promjena

- Home: izvorno skalirane dekoracije 8/16 imaju neovisne pomake u vlasnikovom 
  svijetlom portretnom rasporedu. Tekst, glavna navigacija i dodirni ciljevi ne pomiču se.
- Misije: izvorno oblikovana nativna Canvas strelica na istom `button()` kontrolnom elementu.
  TalkBack oznaka `Natrag` i dodirna zona ostaju sačuvani.
- `OwnerCornerPixelPhase135Test` potvrđuje položaj zasićenih boja i da strelica ostaje interaktivna.
- Marketinški README pokazuje samo 28 potvrđenih stvarnih snimki iz objavljenog izdanja v1.12.217.

## Potrebno za potpunu jednakost

Nakon ove izmjene ponoviti puni emulatorski screenshot QA, izvući stvarne 941×1672 snimke
i ponovno izračunati cijeli RGB MAE i regije. Bez stvarnog ponovnog mjerenja ne smije
se tvrditi da se ukupni MAE smanjio. **0 % / MAE 0 nije potvrđeno.**
Za preostale razlike Misija i Tema treba izjednačiti izvorne ikonice, vektorske 
silhuete, font rasterizaciju, sjene i realna stanja korisničkih podataka.

## Potvrđeno nakon v1.12.218

Stvarne PNG snimke uzete su iz potvrđenog emulator proof artefakta
[Build and release Android #218](https://github.com/bren-wp/Brojko/actions/runs/43132063872),
s istim originalnim PNG referencama pri **941×1672**, bez bilo kakvog skaliranja.

| Područje | RGB MAE prije | RGB MAE poslije | Smanjenje |
|---|---:|---:|---:|
| Cijela Naslovnica | 13,8979 | **12,4051** | -1,4928 |
| Plava dekorativna 8 | 43,388 | **19,846** | -23,542 |
| Žuta dekorativna 16 | 29,435 | **11,889** | -17,546 |
| Cijele Misije | 13,9238 | **13,8148** | -0,1090 |
| Strelica Natrag | 41,472 | **12,019** | -29,453 |
| Teme — bez izmjena u fazi 135 | 13,7633 | 13,7633 | 0 |

**Geometrijski rezultat:** plava dekoracija: y=405,4 → **329,5** (referenca 329,6);
žuta: y=424,6 → **371,8** (referenca 370,8).
Originalna strelica ima 827 tamnih piksela u mjernom ROI-ju;
Android prije 293, nakon nativne korekcije 1017.

RGB MAE je prosjek apsolutnih razlika kanala na skali 0–255,
**nije postotak**. Ni jedan cijeli zaslon još nije identičan referenci.
Sljedeći koraci uključuju izvorno mjerenje pozadinskih nijansi i
pretpreglednih pločica na Temama (faza 136), zatim tipografije i detalja Misija.
