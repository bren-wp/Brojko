# BROJKO — stvarni pixel/ROI audit devet zaslona

## Verifikacija izvora i metode

- Reference: devet originalnih korisničkih PNG datoteka `Slika ChatGPT 24. lis 2026.`, iz korisnikove datotečne knjižnice.
- Android izlaz: realni PNG artefakti GitHub izdanja **v1.12.225**,
  nastali na emulatorskom Android testu. Referenca i stvarni prikaz
  imaju **941 × 1672 piksela**.
- Nisu korišteni resize, pomak, crop, registracija, AI generirana
  zamjena niti interpolacija.
- Metoda: NumPy/Pillow `mean(abs(int16(reference.rgb)-
  int16(android.rgb)))` preko svih RGB kanala; vrijednost od 0 do
  255. Ne smije se tumačiti kao postotak niti kao vjerojatnost 1:1.
- Ovaj audit mjeri verziju **v1.12.225**, ne kasnije izmjene.
  Različito stanje pločica ili korisničke statistike povećava MAE
  čak i ako je stil podudaran.

## Cijeli ekrani

| Zaslon | RGB MAE (0–255) | % potpuno jednakih piksela |
|---|---:|---:|
| Splash | 10.761 | 6.061 |
| Naslovnica | 12.405 | 3.543 |
| Klasično 4×4 | 12.184 | 2.681 |
| Pobjeda | 13.722 | 0.546 |
| Dnevni izazov | 13.771 | 3.289 |
| Misije i postignuća | 13.342 | 1.458 |
| Statistika | 12.959 | 0.817 |
| Teme i izgled | 13.428 | 4.525 |
| Postavke | 10.844 | 3.449 |

## Najveća ROI odstupanja

| Područje | RGB MAE |
|---|---:|
| Teme — kartice izbora teme | 19.749 |
| Postavke — sigurnosna kopija | 19.426 |
| Dnevni izazov — kartica nagrade | 18.748 |
| Postavke — ikonice | 18.618 |
| Dnevni izazov — ciljevi | 18.499 |
| Statistika — grafikon | 18.055 |
| Postavke — sklopke | 16.217 |
| Teme — pretpregled pločica | 16.175 |
| Klasična igra — HUD rezultata | 16.148 |
| Pobjeda — rezultat i akcije | 16.132 |

ROI koordinate primjenjuju se u izvornom pixel prostoru prema
`tools/visual_pixel_audit.py`. ROI mogu uključivati stvarne
različite brojeve u rezultatu, datumu ili pločicama, stoga
prije geometrijskih korekcija treba usporediti *identične fixtureove*.

## Sljedeći koraci

1. Kalibrirati nativne Canvas boje/tipografiju/kutove po
   pojedinačnom ROI-u uz identične dinamičke podatke.
2. Ponoviti snimanje na najnovijem buildu, izračunati
   MAE + SSIM i usporediti bez manipulacije referencama.
3. Potvrditi originalne reference za 5×5, sve modale,
   tamni prikaz, launcher i druge neobuhvaćene zaslone.

**0% odstupanja nije postignuto niti tvrđeno.**
