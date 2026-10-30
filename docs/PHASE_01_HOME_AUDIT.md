# BROJKO — zaslon 1/10: Početni (Home), 941 × 1672 px

Izvorni standard: korisnikova datoteka `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261030-210611).png`. Usporedba s izvornim, stvarnim emulator PNG-om `home-source-accent-rays-941x1672.png` iz GitHub Actions workflowa #41127642373 na PR-u #28 (`f3a4ef15`). Oba su PNG-a 941 × 1672, bez skaliranja, umjetnog poravnanja ili pomicanja.

## Početno stanje — RGB MAE

| Područje | ROI (x0,y0,x1,y1) | MAE |
|---|---|---:|
| Cijeli zaslon | (0,0,941,1672) | 23.928 |
| Logotip i ukrasi | (100,150,840,480) | 32.543 |
| Kartice rezultata | (30,450,910,680) | 19.118 |
| Glavni CTA | (55,650,880,920) | 21.793 |
| Načini 4×4, 5×5 i dnevni | (30,900,900,1325) | 26.036 |
| Donji prečaci | (25,1300,910,1625) | 18.529 |

MAE je prosječna apsolutna RGB razlika svih piksela u zadanom ROI-ju (0–255; manja vrijednost je bolje). Sama metrika **nije** potvrda savršene 1:1 identičnosti. Originalna slika uključuje ilustrativne detalje (posebno logotip, mekane sjene), dok aplikacija crtana Android Canvas vektorima ostaje potpuno interaktivna.

## Izmjene u ovoj iteraciji

- Topli, blago zasjenjeni Home score/mode/shortcut cardovi; ostale vrste kartica ostaju nepromijenjene.
- Poseban Home motiv četiriju lebdećih pločica, s mekim difuznim sjenama i većim brojevima.
- Tri svijetla stupca dodana na desni rub kartice najboljeg rezultata kao na referenci.
- Tri svjetlije podloge klasičnog, opuštenog i dnevnog načina.
- Poseban ilustrirani, reljefni Home kalendar sa žutom zvijezdom; mali funkcionalni kalendari drugdje ostaju isti.
- Glavni koraljni CTA dobiva topliju/prikladniju nijansu gradijenta, bez promjene handlera ili dodirne zone.

### Stvarni podaci i vizualna usporedba

Na vlasnikovoj referenci stoji `12.560` / `3.864` i CTA `Nova igra`. Normalna aplikacija prikazuje **stvarni** `best`, `lastScore` i `Nastavi igru` ako postoji partija u tijeku. Nije prihvatljivo mijenjati produkcijske rezultate da bi izgledali kao marketing maketa.

Da bi razlike u podacima bile izolirane od vizualnih razlika, Android instrumentacijski test `homeControlledGoldenMatchesSourceDataWithoutFakingPlayerStats` privremeno postavlja demo-podatke **samo u memoriji odvojene testne aktivnosti** i izvozi `home-controlled-reference-941x1672.png`. Test sve ranije vrijednosti vraća bez zapisivanja u preferences. U samoj aplikaciji ništa nije lažirano.

## Obavezni završni QA

1. Android PR compilation, testDebugUnitTest, lintDebug, APK/AAB packaging.
2. Real emulator: Android UI smoke i screenshot QA, 941×1672, 360×600, visok telefon, sve tri teme.
3. Android 16 navigacija / edge-to-edge.
4. Usporediti novi kontrolirani screenshot s referencom i provjeriti da se nijedan funkcionalan gumb nije izgubio.
5. Ako ROI rezultati regresiraju, vratiti lokalnu promjenu prije spajanja.

**Status:** Ovo je namjerno uski rad na ekranu Home. PR #28 ostaje Draft, bez automatskog spajanja/izdavanja.


## Potvrđena iteracija — `31ab840`

Sva tri GitHub Actions workflowa završila su uspješno:
- [Android PR validation #41130372774](https://github.com/bren-wp/Brojko/actions/runs/41130372774)
- [Android 16 navigation/insets QA #41130373035](https://github.com/bren-wp/Brojko/actions/runs/41130373035)
- [Android UI smoke + emulator screenshots #41130372871](https://github.com/bren-wp/Brojko/actions/runs/41130372871)

Novi stvarni emulator ZIP: `Brojko-real-device-UI`, artifact ID `13012288787`. Izvor slike (941×1672) ostaje isti, nema interpolacije niti naknadnog pomicanja radi boljeg rezultata. Usporedba je provedena nad *istim* screenshotom `home-source-accent-rays-941x1672.png` u staroj i novoj verziji.

| Regija (pixels) | Prije (f3a4ef15) | Poslije (31ab840) | Delta |
|---|---:|---:|---:|
| Cijeli Home | 23.928 | 22.770 | −1.158 |
| Logotip (100,175–840,470) | 34.863 | 34.210 | −0.653 |
| Rezultatske kartice (30,450–908,680) | 19.125 | 17.027 | −2.098 |
| Primarni CTA (55,650–880,920) | 21.793 | 20.166 | −1.627 |
| Načini igre (30,900–900,1325) | 26.036 | 25.911 | −0.125 |
| Donji prečaci (25,1300–910,1625) | 18.529 | 14.813 | −3.716 |

**Ograničenja:** Nijedan od gore navedenih brojeva ne dokazuje 1:1 identičnost. Priloženi original sadrži organske, nacrtane 3D oblike i vlastitu tipografiju logotipa; trenutni izvor koristi vektore i Nunito. Stoga je potrebno dodatno uskladiti izvorni brand asset, tile bevel, i tipografiju u istom viewportu. Nakon toga provjeriti i visoke i kratke uređaje.

### Jasni redoslijed zaslona za daljnji rad

1. Početni — trenutačni aktivni zaslon, CI potvrđen, **1:1 nije potvrđeno**
2. Klasično 4×4 — sljedeći nakon završnog Home pixel passa
3. Pobjeda „Bravo!”
4. Dnevni izazov
5. Misije i postignuća
6. Statistika
7. Teme i izgled
8. Postavke
9. Splash
10. Ikona i brending

Svi zahvati ostaju u postojećem PR-u #28 dok korisnik ne zatraži drukčije. Nema umjetnog prikazivanja fiktivnih rezultata u produkciji.
