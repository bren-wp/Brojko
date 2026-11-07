# BROJKO — faza 57: Teme i izgled + Statistika na visokim portretima

Datum: 7. 11. 2026. Reference su korisnikovi PNG-ovi iz razgovora:
- `Slika ChatGPT 24. lis 2026. 22_15_18-9(20261107-214610).png` — Teme i izgled
- `Slika ChatGPT 24. lis 2026. 22_15_17-8(20261107-214609).png` — Statistika

Obje slike su 941 × 1672 px. Za pixel-audit koriste se **isključivo kao referenca**, ne kao bitmapa u Android aplikaciji.

## Potvrđeno prethodno stanje

PR [#74](https://github.com/bren-wp/Brojko/pull/74) je spojen; tri GitHub Actions provjere na njegovoj grani bile su uspješne:
- Android build/lint/unit (42419475618)
- Android 16 navigation/insets (42419475635)
- stvarni Android UI emulator i PNG artefakti (42419475671).

Emulatorski PNG: `golden-state-stats-941x1672.png` i `themes-original-final-aligned-941x1672.png` iz ZIP artefakta `13592423751`.

Usporedba bez resizea, bez pomicanja i bez retuširanja, RGB MAE (0–255):
- Teme **17,715** (prije PR #74 **17,715**).
- Statistika **17,576** (prije PR #74 **17,677**).
- Statistika prva KPI regija x20–328, y308–540: **24,559 → 24,266**.
- Statistika plavi KPI x20–478, y551–749: **18,517 → 16,995**.

Razlike između korisnikovih ilustrativnih 10 bodova na grafu i istinskih spremljenih partija u emulatoru ne treba popravljati lažiranjem podataka u produkciji. **MAE nije postotak poklapanja. Nije potvrđeno 0 % odstupanja.**

## Faza 57 — stvarni problem prilagodbe velikim i malim ekranima

Za `themes` i `stats` Android je prije ovoga pri omjeru visina/širina >=1,85 prelazio na drugi 880-unit prikaz. To mijenja položaje i relativne dimenzije od vlasnikove izvorne 400×710 kompozicije: izbor tema, donji navigacijski meni, pet KPI kartica i grafikon/povijest.

Izmjena je isključivo na opsegu `page=="themes"` ili `page=="stats"`, kada je zaslon visok portret:
- Omjer širine/visine ne deformira elemente. Skala je `usableWidth / 400` u x i y.
- Čuva se izvorni `referencePortraitLayout` i postojeći nativni sadržaj.
- Ako 710 logičkih jedinica stane u prikaz, cijeli se izvorni prikaz okomito centrira; inače postojeći kratki portreti i dalje skrolaju.
- Izvornih **941×1672** ostaje točno na istom kodnom putu, s nepromijenjenim bojama, okvirima, ikonama, tipografijom, rezultatima i slikom.
- Vodoravna orijentacija i gameplay zadržavaju postojeći ponašajni način.

## Regresijski testovi

`StatsThemesTallPortraitReferenceTest` radi s produkcijskim Canvas kodom i živim hitboxima.
- Reference 941×1672 i visoki portreti **360×800, 393×873, 1080×2400, 1440×3200**.
- Kriteriji: izvorna kompozicija, proporcije, centriranje, neodrezani hitboxi, svi ključni odabiri.
- Kratki portreti **320×480** i **360×600** — skrolanje do dna bez rezanja.
- Na emulatorskom CI-ju stvarni PNG: `phase57-source-themes-360x800.png`, `phase57-source-themes-1080x2400.png`, `phase57-source-stats-360x800.png`, `phase57-source-stats-1080x2400.png`.

## Gate prije spajanja

- [ ] Build, lint, unit i Android UI instrumentacijski testovi zadnjeg SHA-a.
- [ ] Android 16 edge-to-edge / navigacijske sistemske trake.
- [ ] Pregledati **nove** 941×1672 PNG-ove radi potvrde da nema regresije prema dvije izvorne reference.
- [ ] Pregledati 360×800 i 1080×2400 PNG-ove da ekran bude cijeli i dodirni elementi dostupni.
- [ ] U slučaju funkcionalne ili vizualne regresije vratiti lokalnu izmjenu, ne mijenjati drugu stranicu.
