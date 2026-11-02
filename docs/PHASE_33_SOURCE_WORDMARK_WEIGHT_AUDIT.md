# BROJKO — faza 33: Splash → Home, izvornom logotipu bliži nativni raster

Datum: 2. studenoga 2026.

## Referentne slike i uspješna polazna verzija

- Vlasnikov original Splash: `Slika ChatGPT 24. lis 2026. 22_15_06-2(20261102-201008).png`.
- Vlasnikov original Home: `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261102-201007).png`.
- Stvarne Android emulator snimke: `splash-phase09-owner-941x1672.png`, `home-controlled-reference-941x1672.png` iz uspješnog [Android UI #41521025947](https://github.com/bren-wp/Brojko/actions/runs/41521025947), artefakt `Brojko-real-device-UI` ID `13177223960`.
- Izvorne i stvarne snimke imaju **941 × 1672 px**, bez pomicanja, skaliranja, resamplinga ili izmišljanja statusne trake.

| Zaslon | Posljednji potvrđeni RGB MAE cijelog zaslona, 0–255 |
|---|---:|
| Splash | 18,691 |
| Home | 22,376 |
| Klasično 4×4 | 16,925 |
| Pobjeda | 18,165 |
| Dnevni izazov | 16,846 |
| Misije | 23,589 |
| Statistika | 20,955 |
| Teme | 22,739 |
| Postavke | 15,354 |

Gornji MAE brojevi prethode ovoj iteraciji. Ne dokazuju 1:1.

## Izmjerena razlika maski tamnoplavih slova

U identičnom izvornom ROI-ju svaki se piksel broji ako `red<95, green<125, blue<158, blue>red`:

| Logotip | Koordinate ROI | Izvorni popunjeni tamnoplavi px | Emulator prije izmjene |
|---|---|---:|---:|
| Splash | (60,675)–(885,1060) | **92.082** | **81.114** |
| Home | (120,185)–(800,470) | **54.366** | **46.102** |

Oblici su po vanjskim granicama već blizu; unutarnji volumen slova je previše tanak. Ne mijenjati globalni Nunito jer bi to pogodilo čitljivost, hrvatsku dijakritiku, statistike, gumbi i raspored svih zaslona.

## Nova lokalna izmjena

`raisedWordmarkLetters()` koristi **FILL_AND_STROKE**, debljinu od 1,8 dizajnerskih jedinica i ROUND spojeve isključivo za `Br` i `jko` u svijetlom logotipu Splasha i Homea. Koraljna pločica 2, zrakice, stvarna igra, dodirna područja, tamni izgled i font drugih elemenata ostaju neizmijenjeni.

Novi Android test `splashAndHomeSourceWordmarkRetainsHeavyNavySilhouette` snima stvarni `941×1672` renderer u obje stranice, broji izvornu tamnoplavu masku i zaustavlja CI ako nije punija od ranije verzije ili je ekstremno predebela. Screenshotovi: `splash-owner-wordmark-weight-941x1672.png`, `home-owner-wordmark-weight-941x1672.png`.

## Obvezna potvrda

- [ ] Android PR build/lint/unit završio s `success` na posljednjem SHA-u.
- [ ] Android 16 navigation/insets završio s `success` na posljednjem SHA-u.
- [ ] Android UI smoke + screenshot završio s `success` i uključuje nove PNG-ove.
- [ ] Na novim emulator snimkama izmjeriti **regionalni i globalni RGB MAE prema istim vlasnikovim PNG-ovima**.
- [ ] Zadržati izmjenu samo ako regionalni pixel MAE stvarno pada bez značajne regresije drugih zaslona. Inače korigirati ili vratiti.
- [ ] Nakon potvrde nastaviti Klasično 4×4, Pobjedu, Dnevni izazov, Misije, Statistiku, Teme i Postavke — zasebnim lokalnim, mjerljivim koracima.

**Nisu generirane ilustracije za chat ili statične pozadine umjesto aplikacije. Nije dokazano potpuno 1:1.**