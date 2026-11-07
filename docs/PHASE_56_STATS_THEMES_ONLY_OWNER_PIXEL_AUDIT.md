# BROJKO — faza 56: Statistika + Teme i izgled, samo dva zaslona

Datum: 7. studenoga 2026.

## Vlasnikove izvorne reference

- **Teme i izgled**: `Slika ChatGPT 24. lis 2026. 22_15_18-9(20261107-213447).png`, **941 × 1672 px**.
- **Statistika**: `Slika ChatGPT 24. lis 2026. 22_15_17-8(20261107-213447).png`, **941 × 1672 px**.

Nisu spremljene u APK/AAB, nisu generirane zamjenske slike ni korištene kao plošne pozadine aplikacije.

## Stvarna početna usporedba na `main`

Preuzeta stvarna Android emulatorska snimka iz uspješnog [Android UI QA 42416282132](https://github.com/bren-wp/Brojko/actions/runs/42416282132), ZIP `Brojko-real-device-UI`, artefakt `13590959533`. Izvornici i snimke imaju identičnu rezoluciju; RGB MAE računa se **bez skaliranja, pomicanja i poravnavanja**, prosjek apsolutne razlike triju kanala (0–255).

Za Teme korištena je `themes-original-final-aligned-941x1672.png`; za Statistiku `golden-state-stats-941x1672.png` s kontroliranim emulatorskim fixtureom.

| Regija | Početni RGB MAE |
|---|---:|
| Teme — cijeli zaslon | **19,816** |
| Teme — zaglavlje (y0–370) | 15,880 |
| Teme — izbor (y370–790) | **24,510** |
| Teme — glavni panel (y790–1480) | 21,310 |
| Teme — ploča (x30–465,y810–1470) | **24,650** |
| Teme — opcije (x465–910,y810–1470) | 22,120 |
| Teme — navigacija (y1480–1672) | 11,750 |
| Statistika — cijeli zaslon | **19,020** |
| Statistika — zaglavlje (y0–306) | 19,670 |
| Statistika — prva tri KPI-ja (y306–550) | **26,360** |
| Statistika — druga dva KPI-ja (y550–770) | 16,850 |
| Statistika — grafikon (y770–1160) | **22,610** |
| Statistika — povijest (y1160–1672) | 13,330 |

**To su početne vrijednosti prije izmjena faze 56, nisu konačni rezultati.** Statistika u vlasnikovom primjeru prikazuje demonstracijskih deset grafičkih vrijednosti, ali lokalna arhiva emulatora ima drukčiji redoslijed i vrijednosti. To nije samo vizualna razlika. U produkciji se uvijek prikazuju stvarni `history()`, `best`, `plays`, `recordTile`, `consecutiveWins()`; nije dozvoljeno lažirati rezultate radi makete.

## Lokalno promijenjeno samo na ova dva ekrana

**Statistika**
- Izvorne tri gornje pastelne KPI kartice zauzimaju x≈33–323, x≈345–612 i x≈630–896 (native 941); prilagođeni su x/širine unutar kompaktnog `statisticsCompact()` rasporeda.
- Obje niže KPI kartice poravnate su s izvornim lijevim i desnim rubom, bez promjene dinamičkih vrijednosti.
- Naslov `Zadnjih 10 partija` dobio je lokalno poravnanje. Stupci zadržavaju stvarni sadržaj, ali imaju osnovnu liniju oko 1080 px kao na izvorniku, umjesto oko 1065 px.

**Teme i izgled**
- Rub odabrane teme `Svijetla` proširen je na x≈33–325 px te je proširena **stvarna dodirna zona** kako rub ne bi bio neaktivan.
- Indikatori aktivne teme lokalno su podignuti 3 logičke jedinice.
- Lijevi pregled (naslov, objašnjenje, 4×4 dekorativni uzorak) približen je stvarnim rubovima referentne slike. Promjena je isključivo vizualna: aktivne pločice igre i mehanika spajanja nisu mijenjane.
- Okvir `Zaobljene` postao je širi, ali je njegova odabrana dodirna zona jednako proširena.
- Sve tri teme, tri stila, vibracija, zvukovi, jačina animacija i navigacija ostaju pravi odabiri s postojećom lokalnom pohranom.

## Regression QA

Nova `StatsThemesOwnerSourcePhase56Test` klasa provjerava:
1. stvarni 941×1672 Android Canvas, pastelnog prvog KPI-ja i aktivne tabove grafikona;
2. izvorni koraljni rub odabrane teme i istodobnu prisutnost triju izbora tema, triju stilova, postavki zvuka/vibracije i triju jačina animacije;
3. dinamičko skaliranje na 320×480, 360×600, 393×873, 941×1672, 1080×2400, 1440×3200 i 800×360.

Postojeći `ScreensSmokeTest` dodatno ima pokrivenost drugih zaslona, no ova faza namjerno **ne mijenja** njihov UI.

## Obvezno prije spajanja

- [ ] Android PR build/lint/unit, Android 16 edge-to-edge i UI emulator screenshot QA **na najnovijem SHA**.
- [ ] Preuzeti nove stvarne 941×1672 snimke za oba ekrana i ponovno izračunati iste ROI vrijednosti. Smanjenje MAE potvrditi mjerenjem; vratiti svaku regresijsku lokalnu izmjenu.
- [ ] Potvrditi stvarne dodirne zone, navigaciju, TalkBack/safe-area i scrolling na manjim i većim ekranima.
- [ ] Ne tvrditi 0 % odstupanja bez binarno jednakog rendera i očekivanog podatkovnog stanja.

Praktična napomena: iste pikselne koordinate na telefonima različitih omjera nisu isto što i ispravan responzivni raspored; zadržava se izvorni 941×1672 za vizualno mjerenje, a prikaz drugih uređaja prilagođava se bez rastezanja ili blokiranih kontrola.


## Faza 56 — potvrđeni rezultat prvog prolaza, prije korekcije unutarnjeg sadržaja

Na SHA-u `ed67f23516cfa0c629d2cc75ff99f4763ee10d4d` uspješno su završili Android build/lint/unit, Android16 insets i **stvarni Android screenshot workflow [42418288816](https://github.com/bren-wp/Brojko/actions/runs/42418288816)**, artefakt `13592986326`.

Isti vlasnički PNG-ovi i identične 941×1672 emulatorske snimke uspoređeni su bez resizea, pomicanja i poravnanja:

| Regija | Prije faze 56 | Prvi rezultat | Razlika |
|---|---:|---:|---:|
| Teme cijeli | 19,816 | **17,715** | −2,101 |
| Teme naslov (x180–775,y205–325) | 41,538 | **26,310** | −15,228 |
| Teme izbor (y370–790) | 24,512 | **23,193** | −1,319 |
| Teme pregled (x30–465,y810–1470) | 24,654 | **18,929** | −5,725 |
| Statistika cijeli | 19,020 | **17,677** | −1,343 |
| Statistika naslov (x210–735,y160–295) | 53,655 | **40,615** | −13,040 |
| Statistika grafikon (y770–1160) | 22,607 | **20,119** | −2,488 |
| Statistika gornje kartice (y306–550) | 26,360 | **25,615** | −0,745 |
| Statistika donje kartice (y550–770) | **16,854** | 17,093 | **+0,239, mala regresija** |
| Statistika prva KPI ROI (x20–328,y308–540) | **23,777** | 24,559 | **+0,782, lokalna regresija** |

### Lokalna korekcija nakon mjerenja

Na sljedećem SHA-u `7b36465f5c62640dad046068448e8b04a63e3985` zadržani su izvorno široki vanjski okviri, ali su **samo ikona i naslov** prve kartice `Najbolji rezultat` i kartice `Prosjek bodova` pomaknuti +6 dizajnerskih jedinica udesno, kako bi kruna i plava zvijezda ponovno odgovarale izvornim centrima. Stvarne brojke ostaju na već usklađenim pozicijama.

Ovo je nova, još **nemjerena** lokalna korekcija. Ne tvrditi da su regresije uklonjene prije novog uspješnog emulator artefakta na zadnjem SHA-u. Ni jedan screenshot nije ugrađen u APK/AAB, gameplay i sve druge stranice nisu dirani.
