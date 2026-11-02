# BROJKO — faza 29: Splash i Home, adaptivni zasloni

Datum: 2. studenoga 2026.  
Postojeći Draft PR: [#35](https://github.com/bren-wp/Brojko/pull/35)  
Izvornici vlasnika: Splash i Home, **941 × 1672 px**. Vizualni izvornici služe za pixel audit; nisu zamijenjeni generiranim slikama.

## Cilj i zaštita izvornog izgleda

Prvo se provjeravaju Splash i Home. Na referentnom zaslonu 941 × 1672 izračun širine ostaje **941 / 400**, bez dodatnog reskaliranja, pomicanja ili promjene elemenata. Trajanje Splasha ostaje **3200 ms**, a Home sa stvarnim rezultatima, funkcionalnim načinima igre i prečacima.

Uočen je rizik kraćih zaslona: Splash u kompaktnoj kompoziciji koristi položaj slogana do y≈628 logičkih jedinica; na 320 × 480 dosadašnja skala širine `320 / 400 = 0,8` postavljala je donji rub do ≈502 px, izvan zaslona. Sada se samo na kompaktnom Splashu ograničava skala s `min(width/400, height/640)`, čime slogan ostaje vidljiv i ne zahtijeva scroll. Za landscape Splash cijela se kompozicija uklapa u 880 logičkih jedinica.

Za landscape Home dopušteno je vertikalno pomicanje ako donji prečaci ne stanu. Home u landscapeu računa veće dodirne kartice (skala do `height/580`) i čuva mogućnost pristupa svim četirima prečacima umjesto dodatnog smanjivanja. Ništa ne prepisuje lokalne korisničke rezultate.

## Testna matrica 17 veličina i omjera

| Skupina | Testirani viewporti (px) |
|---|---|
| Vrlo mali / kratki telefoni | 320×480, 360×600, 360×640 |
| Uobičajeni telefoni | 360×800, 393×873, 412×915, 540×960 |
| Veći telefoni / tablet portret | 600×1024, 800×1280, 941×1672, 1080×2400, 1440×3200 |
| Landscape i veliki ekrani | 480×320, 800×360, 960×540, 1280×800, 2560×1600 |

Instrumentacijski test `splashAndHomeFitSeventeenScreenSizesAndKeepAllActions` koristi stvarni `MainActivity.BoardScreen` Java/Canvas renderer, svaki put s točnim dimenzijama, bez prepravljanja PNG-a. Provjerava:

- Splash: skala veća od nule, sadržaj širine 400 unutar viewporta, nijedan odrezani slogan i nikakvo pomicanje; 3200 ms trajanja ostaje nepromijenjeno.
- Home: dvije prave statističke kartice, tri načina igre, četiri funkcionalne navigacijske kartice i jedan gumb `Nova igra` / `Nastavi igru`.
- Na kratkim zaslonima sva akcijska područja dostupna su uz scroll, uz provjeru da donji prečaci mogu u cijelosti doći u vidljivi dio.
- Za landscape se provjerava fizička širina prečaca od najmanje 48 renderiranih piksela na testnim dimenzijama (to nije zamjena za test stvarne dodirne površine u dp).
- Zaseban test `splashHomeDarkThemeAlsoRetainsSmallAndLargeGeometry` izvršava oba zaslona u tamnoj temi na 320×480, 941×1672, 800×360 i 1280×800.

Osam novih snimaka izvornog Canvas renderera dostupno je u Android emulator artefaktu: za svaki od **Splash** i **Home** snima se 320×480, 360×800, 800×360 i referentnih 941×1672 px. `scripts/verify-emulator-screenshots.py` mora potvrditi stvarnu PNG potpisnu sekvencu i navedene dimenzije.

## Granice verifikacije

Ova matrica pokriva tipične i rubne veličine, **ne doslovno sve postojeće Android uređaje**, njihove gustoće, postavke povećanog fonta, proizvođačke prilagodbe ili sve inačice operacijskog sustava. GitHub Actions dodatno pokreće Android 16 / API 36 navigacijske i system-bar testove na pravom emulatoru; za potpunije jamstvo potrebno je i testiranje na drugim fizičkim uređajima, DPI gustoćama, font-scale postavkama i Android verzijama.

- [ ] Android pull request validation uspješan na posljednjem HEAD-u.
- [ ] Android 16 navigation bar and inset QA uspješan na posljednjem HEAD-u.
- [ ] Android UI smoke and screenshot QA uspješan na posljednjem HEAD-u i sadrži 8 novih PNG-ova.
- [ ] Vizualna usporedba izvornog Splash/Home PNG-a s **identičnim fixtureom** i stvarnim emulator snimkama 941×1672.
- [ ] Pokazati regionalne RGB MAE vrijednosti bez tvrdnje o 1:1 prije stvarne usporedbe.

**Bez mergea i bez novog izdanja.** Čuvaju se svi postojeći podaci, paket i potpisivanje.
