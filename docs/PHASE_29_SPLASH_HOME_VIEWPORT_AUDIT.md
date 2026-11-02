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


## Drugi prolaz — izvorne boje Splasha i minijatura Klasične igre

Nakon što je prvi prolaz uspješno testiran na **17 veličina** (Android UI run `41369365604`, ukupno **110 instrumentacijskih testova**) preuzeta je stvarna snimka. Izvorni Splash i Home, dostavljeni od vlasnika na 941×1672, uspoređeni su s jednakim emulator screenshotom bez skaliranja i pomicanja. Rezultat prvog prolaza ostao je **bitno identičan** prethodnom screenshotu na toj rezoluciji, što potvrđuje da adaptivni raspored nije regresirao izvorni izgled.

| Regija | RGB MAE nakon prvog prolaza |
|---|---:|
| Splash cijeli zaslon | 19,433 |
| Splash logo | 27,803 |
| Splash slogan | 11,578 |
| Home cijeli zaslon — istovjetan fixture | 23,186 |
| Home logo | 24,901 |
| Home rezultati | 18,279 |
| Home glavna tipka | 21,474 |
| Home kartice načina | 26,953 |
| Home prečaci | 14,142 |

### Utvrđene lokalne razlike

Splashov koraljni sloganski tekst `Spajaj brojeve.` na izvorniku ima približno **8351 zasićeni koraljni piksel** i dominantnu boju `(253,90,62)`, dok prethodna stvarna snimka ima **6015** i `(255,122,91)`. Novi nativni Canvas crta ga u `#FD5A3E` te umjereno povećava vertikalni profil slova, **samo u svijetlom kompaktno-orijentiranom Splashu**. Zadržava se ista horizontalna pozicija i 3200 ms trajanja.

Na Homeu je dekorativna minijatura igre 4×4 imala preusko i previsoko obojeno područje — približno x70..281 i y941..1102 prema vlasnikovom x72..297 i y947..1115, izmjereno na istom izrezu i kriteriju koraljne boje. Samo ta svijetla portretna minijatura koristi pločice 22,8 (prije 21,5) dizajnerskih jedinica te je pomaknuta 4 jedinice udesno i 3 dolje. **Aktivna ploča 4×4, pravila igre, 5×5, tamna tema i korisnički podaci nisu promijenjeni.**

Dodana dva instrumentacijska testa:
- `splashSourceCoralSloganKeepsTallStrongLettering`, stvarni PNG `splash-coral-source-941x1672.png`;
- `homeSourceClassicThumbnailHasFullColoredGrid`, stvarni PNG `home-classic-preview-source-941x1672.png`, s privremenim, izoliranim rezultatima 12.560/3.864.

Obje snimke moraju proći screenshot validator. **Gornje MAE vrijednosti su početne za drugi prolaz**: konačno poboljšanje treba provjeriti isključivo prema posljednjem uspješnom Android emulator artefaktu. Ako je regionalni MAE povećan, kalibraciju treba vratiti ili korigirati u istom Draft PR-u.


## Treći prolaz — 11 dodatnih window/inset konfiguracija i stvarni touch događaji

Na prethodnom HEAD-u `1e96d427` sve tri Android CI provjere bile su uspješne. Uvedena je dodatna regresijska provjera `splashHomeInsetAndCompactLandscapeTouchMatrix`, usmjerena na neobično niske prozore u vodoravnom prikazu (split-screen, resizable activity, foldable) i sigurna područja oko sistemskih traka.

- Dodatne dimenzije: **240×320, 320×240, 320×480, 393×873, 480×320, 600×280, 800×240, 800×360, 960×540, 1280×800 i 1080×2400**; svaka u svijetloj i tamnoj temi.
- Testna `BoardScreen` instanca ima 16 px vodoravnog i 24 px okomitog paddinga kako bi se testirala geometrija safe-area insets bez izmišljanja punih emulator uređaja.
- Splash test potvrđuje da je slogan unutar sigurnog prostora, širina sadržaja ne prelazi rubove te da nema skrolanja.
- Home test šalje **stvarne Android `MotionEvent.ACTION_DOWN` / `ACTION_UP` događaje** u središte svakog od četiri prečaca nakon pomicanja sadržaja, i potvrđuje točno odredište: misije, statistika, teme i postavke. Ne kreira niti mijenja igračev rezultat.
- Na niskom landscape Home prikazu cilj je do 48 dp širine prečaca, ali skala uvijek ostaje ograničena raspoloživom širinom i visinom kartice. U geometriji koja fizički ne može zadovoljiti 48 dp, prečaci koriste najveću izvedivu skalu uz mogućnost skrolanja.
- Izvorni **941×1672 portretni Splash/Home**, trajanje Splasha, grafičke boje i položaji nisu promijenjeni.

**Ograničenja:** više sintetičkih viewporta ne znači testiranje svakog fizičkog Android uređaja; stvarne razlike gustoće, font scale, multi-window upravljanja i sistemskih insets po proizvođaču i dalje zahtijevaju uređaje/emulatore. CI rezultat nove revizije potrebno je potvrditi na posljednjem HEAD-u. Nije tvrdnja o 1:1 podudaranju.
