# BROJKO — faza 58: isključivo tri vlasnička zaslona

Datum: 7. studenoga 2026. Usporediti **isključivo** ova tri PNG izvornika:
- `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261107-222329).png` — Misije i postignuća
- `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261107-222329).png` — Dnevni izazov
- `Slika ChatGPT 24. lis 2026. 22_15_13-4(20261107-222329).png` — Klasično 4×4, aktivan gameplay

Izvornici imaju **941 × 1672 px**. Nisu dio aplikacije i nisu korišteni za statičnu, nefunkcionalnu pozadinu.

## Početno mjerenje

Stvarna Android Canvas PNG snimka iz uspješnog PR #75 [UI workflowa 42422006393](https://github.com/bren-wp/Brojko/actions/runs/42422006393), artefakt `13593744093`. Referenca i emulator uspoređeni su u RGB-u, na istoj rezoluciji, **bez resampliranja i bez pomicanja**. MAE: prosječna apsolutna razlika RGB kanala (raspon 0–255), nije postotak.

| Regija, vlasnički izvor | Početni RGB MAE |
|---|---:|
| Misije — cijeli ekran | 21,905 |
| Misije — zaglavlje (y0–300) | 19,051 |
| Misije — dnevne kartice (y300–746) | 22,333 |
| Misije — tjedne kartice (y746–1218) | **26,497** |
| Misije — postignuća (y1218–1672) | 23,072 |
| Dnevni — cijeli ekran | 16,286 |
| Dnevni — zaglavlje (y0–380) | 22,247 |
| Dnevni — naslov (x240–695,y20–225) | **52,902** |
| Dnevni — ilustrativna ploča (x190–740,y390–947) | 13,249 |
| Dnevni — ciljevi (y955–1300) | 19,485 |
| Dnevni — CTA (y1510–1672) | 20,381 |
| Klasično 4×4 — cijeli ekran, kontrolirana stvarna partija | 14,756 |
| Klasično — gornji HUD (y0–350) | 13,888 |
| Klasično — 4×4 ploča (y370–1320) | 14,473 |
| Klasično — donje kontrole (y1320–1645) | **21,736** |

Napomena: stvarni korisnički napredak, tjedni brojači, niz odigranih dana, datum izazova i brojevi na grafičkim karticama **moraju se računati iz stvarnog stanja**. Referentna slika s 12/20, 5/7, 6.250/10.000, 14. ožujka 2025. ili primjerom 4/20 postignuća ne smije se prekopirati u produkcijsko stanje. U trenutnoj aplikaciji postoje dva stvarno implementirana postignuća i zato se njihov omjer prikazuje s nazivnikom 2.

## Faza 58 — promjene, strogo ograničene

1. **Visoki portret:** `missions`, `daily` i **samo** `game` s načinom `classic` i pločom 4×4 sada koriste istu nativnu vlasničku kompoziciju 400×710 kao na 941×1672. Na 360×800/393×873/1080×2400/1440×3200 prikaz se uniformno skalira i vertikalno centrira bez deformacije. Na 320×480 i 360×600 ostaje skrolanje, ne skrivanje akcija. Aktivni 5×5, Daily gameplay, ostale stranice, njihove figure i stanja ostaju netaknuti.
2. **Dnevni naslov:** dva zasebna nativna tekstualna reda lokalno su skalirana/pomaknuta radi smanjenja raspora; izvorno `Dnevni` tinta x271–667/y50–150, stari Android x266–658/y37–128; `izazov` izvorno x325–641/y142–222, stari Android x315–650/y147–234.
3. **Misije naslov:** navy dio izvorno x130–398/y189–276, stari Android x116–379/y185–262; coral dio izvorno x425–823/y189–277, stari Android x415–849/y182–266. Dva lokalna Canvas transformiranja popravljaju vizualnu geometriju bez promjene tekstualnog sadržaja.

## Funkcionalnost i regresija

Nova klasa `OwnerThreeScreenPortraitPhase58Test` pravi **stvarne native PNG snimke** u instrumentacijskom testu na 941×1672, 360×800 i 1080×2400 za sva tri zaslona. Ispituje i 393×873, 1440×3200, 320×480 i 360×600, te:
- fiksnu izvornu kompoziciju, jednaku skalu X/Y, centriranje, dostupne akcije;
- stvarne tri pomoći i gumb pokretanja Dnevnog izazova;
- stvarno pokretanje nove partije, završetak i pauzu Klasičnog 4×4;
- skrolanje do zadnjih kartica na kratkim uređajima;
- stvarne dnevne/tjedne brojače i gameplay bodove, samo u izoliranom testnom fixtureu za ponovljive snimke.

## Prije spajanja

- [ ] Build, lint, unit, emulator screenshot i Android 16 navigation/insets na **posljednjem SHA**.
- [ ] Izvući nove PNG-ove i izmjeriti isti RGB MAE na 941×1672; uskladiti samo regije koje su se poboljšale, vratiti regresije.
- [ ] Vizualno provjeriti nativne 360×800 i 1080×2400 bez odsijecanja ili preklapanja.
- [ ] Testirati no-undo, aktivni/nedovršeni Daily, stvarno skupljanje nagrada, stvarne tap-zone.
- [ ] Ne tvrditi 0 % odstupanja bez raster-binarne identičnosti izvornika.

Ovaj je PR namijenjen samo trima prikazima; ostale zaslone ne redizajnirati.
