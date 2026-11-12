# BROJKO — faza 136: kalibracija originalnih RGB boja Tema

Datum: 12. studenoga 2026.
Izvor: vlasnikov original `Slika ChatGPT 24. lis 2026. 22_15_18-9` (941×1672).
Stvarni Android baseline: potvrđeni emulator QA v1.12.216 (run 43127186416), `themes-original-final-aligned-941x1672.png`.
Mjerenja: neinterpolirane 9×9 RGB zakrpe u istoj rezoluciji.

| Izvorni element | (x,y) | Referentni RGB | Android prije |
|---|---|---|---|
| Svijetla tirkizna pločica | 206,474 | 35,221,215 | 33,216,205 |
| Živopisna zelena | 800,474 | 118,235,110 | 98,229,85 |
| Živopisna cijan | 699,570 | 3,181,254 | 26,176,255 |
| Živopisna ljubičasta | 800,570 | 179,103,253 | 160,88,255 |
| Krem panel gore | 635,810 | 254,250,242 | 255,253,250 |
| Krem panel dolje | 635,1270 | 253,250,244 | 255,253,250 |

Korekcije su ograničene na `themesCompact()` i njegove dekorativne
pretpreglede u svijetlom vlasnikovom portretnom prikazu: nativni pigmenti,
2 % (umjesto 14 %) bijele refleksije za izvorni cijan i topli krem gradijent
velike podloge. Nisu dirani igra, pravila, gameplay pločice, postavke ni
touch targeti. `OwnerCornerPixelPhase135Test` sada provjerava šest stvarnih
RGB zakrpa preko pravog Android Canvas prikaza.

**Cijeli zaslon još nije 1:1.** Prije tvrdnje da je odstupanje smanjeno,
potrebno je ponoviti emulatorsko snimanje i ponovo izračunati RGB MAE i ROI.
Nikada ne zamijeniti interaktivno sučelje statičnom slikom izvornika.
