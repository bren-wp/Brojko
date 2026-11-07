# BROJKO — faza 52: samo Splash i Home (941 × 1672)

Datum: 7. studenoga 2026.

## Referentni materijal

- Splash: korisnikova originalna slika `Slika ChatGPT 24. lis 2026. 22_15_06-2(20261107-202348).png`.
- Home: korisnikova originalna slika `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261107-202347).png`.
- Prethodno odobreni Android UI workflow: [#42326124866](https://github.com/bren-wp/Brojko/actions/runs/42326124866); stvarne native-Canvas snimke `owner-splash-01-02-native-941x1672.png` i `owner-home-phase44-12560-3864-941x1672.png`.
- Na Home emulator-fixtureu 12.560 / 3.864 služe isključivo za usporedbu screenshotova. Produkcija ostaje vezana uz lokalne stvarne rezultate.

## Početna izmjerena razlika (RGB MAE po kanalu; 0–255)

| Regija | Prije |
|---|---:|
| Cijeli Splash | 12,352 |
| Splash wordmark x60–885, y670–1060 | 9,324 |
| Splash slogan x38–905, y1300–1440 | 12,618 |
| Cijeli Home | 14,919 |
| Home wordmark x120–800, y185–470 | 9,404 |
| Home score cards x35–900, y470–651 | 18,548 |
| Home CTA x36–900, y670–880 | 14,021 |
| Home mode cards x30–911, y892–1300 | 16,057 |
| Home shortcuts x25–910, y1310–1610 | 15,506 |

Za sve izračune korištene su dvije slike u izvornom omjeru 941 × 1672, uz apsolutnu RGB razliku bez resampliranja. **Ove vrijednosti prethode izmjenama faze 52 i nisu rezultati nove verzije.**

## Izmjene isključivo dvaju zaslona

1. **Home CTA**: proizvoljni gotovo okomiti gradijent zamijenjen je dijagonalnom nativnom Canvas interpolacijom boja, kalibriranom iz izvorne koraljne površine. Na izvorniku zelena komponenta npr. na (300,740) iznosi približno 121, a na (730,740) oko 102. Prethodni Android render oboje je crtao oko 132. Geometrija, bijeli play simbol, tekst i dodirna površina ostaju isti.
2. **Home score cards**: svijetla boja kartice pomaknuta je iz `#FCF5E9` u `#FEF8EE` radi približavanja referentnom toplom ivory rasteru. Brojke su stvarni lokalno spremljeni rezultati.
3. **Splash slogan**: pozadina svijetle pilule promijenjena je iz `#FFF0DB` u `#FFF5E6`. Tekst ostaje nativni, a ne dio ugrađene referentne slike.

Tamna tema, landscape, gameplay, score storage, tile generation i ostale stranice nisu namjerno mijenjani. Dodana su dva instrumentacijska testa boje uz provjeru stvarnih dodirnih kartica.

## Obvezni prihvatni kriteriji

- [ ] Android build, lint i unit testovi na posljednjem SHA-u prolaze.
- [ ] Novi Splash/Home emulator screenshotovi pri 941 × 1672 uspoređeni su s istim originalima.
- [ ] Novim regionalnim MAE mjerenjem potvrditi poboljšanje; vratiti izmjenu ako je regija lošija.
- [ ] Na 360×800, 393×873, 941×1672 te landscapeu provjeriti da nema odrezanih kartica, logotipa ni nedostupnih akcija.
- [ ] Vizualna provjera izvornog 3D svjetla, fonta i sjena (MAE nije zamjena za vizualni pregled).

**Nije moguće tvrditi 0 % odstupanja bez pikselno jednakih stvarnih snimaka i provjere na ciljnom uređaju.** Nije ugrađena screenshot pozadina u aplikaciju i nije promijenjen prikaz korisničkih rezultata.
