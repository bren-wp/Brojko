# BROJKO — faza 54: samo Splash i Home, izmjereni izvornici 941 × 1672

Datum: 7. studenoga 2026.

## Referentni izvor

Dvije nove vlasnikove referentne PNG datoteke:
- Splash: `Slika ChatGPT 7. stu 2026. 21_49_54.png`
- Home: `Slika ChatGPT 7. stu 2026. 21_49_49.png`

Obje imaju točno 941×1672 piksela. One **nisu** sadržaj aplikacije, nego izvor za vizualnu usporedbu.

Snimke iz stvarnih Android UI testova:
- *Prije* faze 53: [main Android UI run 42412938342](https://github.com/bren-wp/Brojko/actions/runs/42412938342), PNG `owner-splash-01-02-native-941x1672.png` i `owner-home-phase44-12560-3864-941x1672.png`.
- *Nakon* faze 53: [PR #70 Android UI run 42413680522](https://github.com/bren-wp/Brojko/actions/runs/42413680522) — `success`, potpuno iste vrste PNG-ova.

Na Home snimci rezultati **12.560** i **3.864** su **isključivo emulatorski fixture** za jednaku usporedbu. Prava igra nastavlja koristiti spremljene vrijednosti igrača.

## Stvarna prije/nakon usporedba faze 53

Prosječna apsolutna RGB razlika po kanalu (MAE 0–255), bez resampliranja:

| ROI | Prije faze 53 | Poslije faze 53 |
|---|---:|---:|
| Splash cijeli | 12.241 | **12.044** |
| Splash gornje pločice (0,90)-(941,680) | 13.802 | **13.175** |
| Splash koraljna 2 (100,110)-(450,480) | **11.969** | 12.372 — lokalna regresija |
| Splash tirkizna 4 (500,140)-(840,460) | 14.634 | **11.200** |
| Splash žuta 16 (680,365)-(941,730) | 17.313 | **16.225** |
| Home cijeli | 14.433 | **14.071** |
| Home tri kartice načina igre (35,900)-(908,1305) | 16.130 | **14.518** |
| Home minijatura Klasično (60,920)-(320,1160) | 23.848 | **16.204** |
| Home rezultati (50,475)-(915,665) | 16.439 | **16.439** |
| Home CTA (125,678)-(810,884) | 13.395 | **13.395** |

## Faza 54 — dodatne izolirane promjene

- **Splash:** boje i prozirnosti udaljenih zamućenih pločica radi snažnijeg cyan, peach i coral osvjetljenja, pomak donje cyan površine te umjereno smanjenje prevelike plave 8. Glavne kartice i izvorni vektorski navy logotip nisu dirani.
- **Home:** toplije kremaste kartice s rezultatom; lokalno prilagođene tri boje površina načina igre; dodano lokalno toplo osvjetljenje gornjeg lijevog dijela koraljne tipke `Nova igra`. Dodirne površine, stanje igre, statistika i sve druge stranice nisu promijenjene.

## Obvezna provjera (faza 54)

- [ ] Android build, lint, unit, UI i Android 16 QA: svi prolaze **na posljednjem SHA-u**.
- [ ] Preuzeti **novi** emulatorski ZIP iz faze 54, usporediti na identičnom 941×1672 izvorniku i fixtureu.
- [ ] Regresiju koraljne 2, nastalu u fazi 53, obraditi odvojeno — ova faza **ne mijenja** njezinu geometriju.
- [ ] Provjeriti region-specific MAE: Splash lower ambience (0,950)-(941,1672), Home score/cards/CTA/modes; vratiti regionalnu izmjenu ako pogoršava rezultat.
- [ ] 360×800, 393×873, 320×480, 941×1672 i landscape: pregledati sigurnu zonu, clipping, skrolanje i funkcionalnost kartica.

### Ograničenja

Emulator i umjetnička referenca nisu identičan način rasterizacije: 0 % odstupanja **nije** dokazana činjenica, naročito za OS statusnu traku i izvorno 3D osvjetljenje. Nikada ne zamijeniti funkcionalni prikaz statičnom slikom.
