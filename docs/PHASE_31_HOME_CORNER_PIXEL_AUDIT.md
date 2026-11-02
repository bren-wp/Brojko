# BROJKO — faza 31: Home, prvi izvorni koloristički prolaz

Referenca: vlasnikov `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261102-201007).png` (941 × 1672 px). Emulator: `home-controlled-reference-941x1672.png` iz [Android UI QA #41369365604](https://github.com/bren-wp/Brojko/actions/runs/41369365604), isto **941 × 1672** bez reskaliranja i pomaka. Referentne vrijednosti **12.560 / 3.864** postoje samo u testnoj memoriji; stvarna aplikacija i nadalje prikazuje korisničke rezultate.

## Potvrđeni početni MAE

| Regija (px) | RGB MAE / 255 prije nove iteracije |
|---|---:|
| Cijeli Home | **23,186** |
| Gornje dekorativne pločice, y=0..205 | **23,007** |
| Logo x=140..800, y=170..460 | **35,613** |
| Primarni CTA x=55..880, y=650..915 | **21,158** |
| Kartice načina x=30..900, y=900..1300 | **27,126** |

Navedene vrijednosti su početne, a ne rezultat novih izmjena.

## Usklađivanje kutnih pločica

Umjesto prethodnog generičkog `styledTile` renderera, svijetli portretni Home sada ponovno koristi `splashFloatingTile` za **samo četiri dekorativne kutne pločice**. Time koraljna 2, tirkizna 4, plava 8 i žuta 16 imaju dosljedne gradijente, korpus i reljef. Tamna tema, aktivna igra, podaci i upravljačke površine nisu izmijenjeni.

Instrumentacijski test `homeCornerArtUsesSourceSaturatedPalette` provjerava boje, prisutnost sva 3 načina igre i sva 4 prečaca te snima stvarni Android screenshot `home-saturated-corner-art-941x1672.png`.

### Sljedeći koraci prije prihvaćanja

- [ ] Build, lint i instrumentacijski testovi na posljednjem PR HEAD-u.
- [ ] Preuzeti stvarni emulator screenshot i usporediti s **istim** 941 × 1672 PNG originalom.
- [ ] Regionalni RGB MAE kutnih pločica mora se smanjiti bez regresije u ostatku zaslona.
- [ ] Zatim zasebno revidirati logotip, glavni CTA, minijaturne pločice i donje prečace; tek nakon toga prijeći na aktivnu ploču Klasično 4×4.

**Zaslon nije potvrđen kao 1:1. Nema automatskog mergea ni izdanja.**
