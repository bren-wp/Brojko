# BROJKO — faza 59: ciljano dotjerivanje tri vlasnička ekrana

Datum: 8. studenoga 2026. Ograničenje: **isključivo** Misije i postignuća, Dnevni izazov te aktivno Klasično 4×4.

Izvorne slike (941 × 1672 px), dostavljene u razgovoru:
- `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261107-222329).png` — Misije i postignuća
- `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261107-222329).png` — Dnevni izazov
- `Slika ChatGPT 24. lis 2026. 22_15_13-4(20261107-222329).png` — Klasično 4×4

Bez generiranja novih slika u chatu, bez dodavanja referentnih PNG-ova u APK i bez zamjene nativnih interaktivnih elemenata slikom.

## Stvarno izmjerena prethodna verzija (nakon spojenog PR #76)

Android UI workflow: [42423660458](https://github.com/bren-wp/Brojko/actions/runs/42423660458), stvarni artefakt `Brojko-real-device-UI` id `13594827447`. Sva tri PNG-a `phase58-<page>-941x1672.png` imaju izvornu rezoluciju, usporedba RGB MAE po kanalu bez skaliranja, registracije, cropanja ili prilagođavanja vremenskih podataka:

| Mjera | MAE 0–255 |
|---|---:|
| Misije cijeli ekran | **21,336** |
| Misije zaglavlje y0–300 | 15,354 |
| Misije dnevne y300–746 | 21,057 |
| Misije tjedne y746–1218 | **25,035** |
| Misije postignuća y1218–1672 | 21,719 |
| Dnevni cijeli ekran | **14,772** |
| Dnevni zaglavlje y0–380 | 15,586 |
| Dnevni naslov x240–695/y20–225 | **29,250** |
| Dnevni ploča x190–740/y390–947 | 13,249 |
| Dnevni ciljevi y955–1300 | 17,587 |
| Dnevni CTA y1510–1672 | 20,381 |
| Klasično cijeli ekran | **14,664** |
| Klasično HUD y0–350 | 13,577 |
| Klasično ploča y370–1320 | 13,817 |
| Klasično kontrole y1320–1645 | **20,185** |

MAE nije postotak podudaranja. **Nije potvrđeno 0 % odstupanja.**

## Izmjerena lokalna odstupanja i promjene

### 1. Misije i postignuća
- Stari emulatorov koraljni naslov imao je raspon obojenih piksela oko `x418–849/y190–272`, a original je oko `x425–823/y189–277`. Samo taj koraljni niz lokalno je sužen/pomaknut i produbljen u boju `#FF5935`.
- Navy slova izvorno su medijana RGB `25,40,73`, stara Android tinta oko `34,46,69`; sada lokalno koriste `#162548` kao osnovu gradijenta. Povećana je debljina obruba obiju naslovnih cjelina kako bi približno odgovarala izvornoj 3D tipografiji.
- Izvorni tjedni panel u čistim regijama: RGB približno `229,245,253`, stari Android `237,247,255`. U izvornoj svijetloj kompoziciji boja se produbljuje s `#E9F5FF` na `#DDF2FD`. Redovi, napredak, stvarne nagrade i podaci ostaju nepromijenjeni.

### 2. Dnevni izazov
- Glavni navy naslov izvornog PNG-a ima medijanu oko RGB `25,43,79`, stari Android `33,45,69`; koraljni naslov izvorno `253,101,59`, stari Android `247,121,91`.
- Samo izvorna, svijetla, portretna kompozicija dobila je temeljne boje `#16284E` i `#FF663D` te nešto puniji obrub slova. Ne mijenjaju se aktualni datum, broj dana zaredom, cilj, nagrada ni pokretanje stvarnog izazova.

### 3. Klasično 4×4
- Aktivna donja gumb-kartica “Završi” ima nešto previše plavog kanala u koraljnoj površini; podešen je samo RGB gradijent od `#FF9A71 → #FF6341` na `#FF9A69 → #FF6339`.
- “Novo” i stvarno aktivni “Poništi” dobivaju suptilnije toplo-ivory gradijente **samo na izvornom svijetlom aktivnom Classic ekranu**; stvarni Tap pravokutnici, Undo stanje, rezultat i ploča ostaju netaknuti.
- Stara `phase58-game` testna slika prikazivala je *sivo, onemogućeno Poništi*, dok referenca prikazuje aktivno dugme. Umjesto lažnog enablea u produkciji, instrumentacijski test prvo obavlja stvarni merge potez `TileGame.LEFT` i potvrđuje `canUndo()`, pa tek potom snima izvorni reprezentativni raspored. Funkcionalnost u novoj igri bez prethodnog poteza i dalje mora držati Poništi onemogućenim.

## Responzivnost i QA

Prethodna faza 58 već je uskladila ova tri izvora na portretnim 941×1672, 360×800, 393×873, 1080×2400, 1440×3200 i osigurala skrolanje na 320×480/360×600. Faza 59 ne mijenja skalu, insets, breakpoints, geometriju tapova ni druge stranice. Postojeća `OwnerThreeScreenPortraitPhase58Test` sada provjerava i stvarni Undo te izvozi nativne PNG-ove.

**Prije mergea:**
- [ ] Prolaz Android build/lint/unit, Android16 i emulator UI instrumentacije na zadnjem SHA.
- [ ] Ponovno preuzeti stvarne 941×1672 screenshotove, izmjeriti globalni i regionalni RGB MAE, bez davanja lažnih postotaka.
- [ ] Provjeriti da je nativno snimljeni `Poništi` aktivan tek nakon stvarnog poteza.
- [ ] Potvrditi stvarne male/velike portrete i jednake aktivnosti tipki.
- [ ] Vratiti svaku lokalnu korekciju koja povećava pogrešku ili uzrokuje regresiju.

**Napomena o sadržaju:** referentna misija prikazuje proizvoljni preostali broj sati, rezultat 12/20 i 4/20 postignuća; dnevni izazov sadrži datum 14. ožujka 2025. Produkcija namjerno prikazuje stvarni lokalni datum i stvarno izračunate podatke. Usporedba njihovih piksela stoga ima neizbježni podatkovni doprinos pogrešci.
