# Brojko — produkcijska tehnička, sigurnosna i vizualna revizija
Datum: 5. studenoga 2026. — autor proizvoda: Brendigo

> Ovaj je dokument interni audit i **nije** dio marketinškog README-a. Statusi su označeni samo ondje gdje postoje dokazi u kodu ili automatiziranim testovima. Potpuna podudarnost 1:1 zahtijeva još izvorne PNG reference svih zaslona i izravno mjerenje snimki emulatora.

## 1. Inventura i funkcionalna površina

Android Java projekt, jedna aplikacija `com.brendigo.brojko`, bez internetske dozvole, minSdk 24, targetSdk/compileSdk 36 i Java 17. `TileGame.java` je zaseban deterministički 2048 engine, `MainActivity.java` obuhvaća više od 4.800 linija Canvas renderera i navigacije; `ScreensSmokeTest.java` provodi velike instrumentacijske setove na emulatoru. Izmjene ne uvode vanjske usluge ni podatkovnu bazu.

Funkcionalna područja: Splash, Home, Klasično 4×4, Opušteno 5×5, Dnevni izazov, stvarni Gameplay, Pause, Undo, Victory/Bravo, Game Over, Daily Win/Fail, Misije, Statistika/Povijest, Teme, Postavke, Jezik, Uvoz/izvoz napretka, Privatnost, Pomoć, O aplikaciji te adaptivne ikone.

## 2. Rizici i provjerene korekcije

| Prioritet | Uočeno | Što je učinjeno / sljedeća radnja |
|---|---|---|
| P0 | Gradle 9.6.0 zaostao za sigurnosnim popravcima 9.8.2 | **Ažurirano** u sva četiri GitHub Actions workflowa; provjeriti stvarne buildove na PR-u |
| P0 | GitHub Actions koriste zastarjeli Node 20 lanac (checkout v4, setup-java v4, release v2 itd.) | **Ažurirani** održavani Node 24 tagovi; odvojeno pravo pisanja samo izdavačkom poslu |
| P1 | AndroidX Core/Activity/instrumentacijski testovi i JVM JSON test modul zaostaju | **Activity i instrumentacijski testovi ažurirani**; Core 1.19.1 zahtijeva compileSdk ≥37, no stabilni Google SDK Manager na CI-ju ne nudi android-37. Zato je kompatibilni AndroidX Core 1.17.0 zadržan uz compile/targetSdk 36, umjesto uvođenja neizgradivog projekta |
| P1 | Uvoz JSON kopije provjerava format/tip/podatke, ali ne provjerava identitet paketa iako se upisuje u izvoz | **Ispravljeno**: odbiti kopiju bez `com.brendigo.brojko` identiteta prije promjene SharedPreferences; novi instrumentacijski regresijski test |
| P1 | README prikazuje poveznice na zastarjeli workflow umjesto trajnih, stvarnih screenshotova | **Preuređen** marketinški README; 12 stvarnih emulator PNG-ova stavlja se u Release assets nakon zelenog UI QA |
| P1 | `MainActivity.java` (više tisuća linija) nosi navigaciju, backup, touch, stilove, crtanje i mrežu | Preporuka: odvojiti `GameScreenRenderer`, `HomeRenderer`, `BackupRepository` i `NavigationController` tek uz očuvanje 1:1 i regresijskih testova |
| P1 | Canvas koristi jednu pristupačnu View površinu s custom akcijama, ali bez zasebnih virtualnih čvorova za sve kontrole | Preporuka: `ExploreByTouchHelper`/virtual nodes i fokusne granice za TalkBack; provjeriti kontraste i veličine dodira |
| P1 | Android release može proizvesti nepotpisani AAB ako secrets nisu postavljeni | Zadržati poštenu oznaku **unsigned / not Play-ready**; za Play obvezno potpisivanje izvornim kompatibilnim ključem, bez mijenjanja potpisnog identiteta |
| P2 | Font Nunito nije pohranjen kao binarni asset u GitHubu; tijekom gradnje povlači se provjereno verzioniran upstream | Postoji provjera izvornog Git bloba i OFL licence; lokalni korisnik aplikacije ništa ne preuzima. Za potpuno offline build treba omogućiti licencirano lokalno pohranjivanje provjerenog fonta |
| P2 | Reproducibilnost lokalne gradnje: nema `gradlew`/wrapper JAR-a | CI koristi zaključanu Gradle verziju. Dodati Gradle Wrapper binarne datoteke s provjerom SHA-256 nakon odobrenja izvornog artefakta |
| P2 | Nema automatskih mjesečnih provjera novih ovisnosti | Uključen Dependabot za Gradle i GitHub Actions uz ograničen broj PR-ova |
| P2 | Engine koristi `int` za rezultat i namjerno ga zasićuje na `Integer.MAX_VALUE` | U budućnosti razmotriti `long` uz migraciju `SharedPreferences` i dokumentiranu kompatibilnost spremljenih partija; nikada bez testova |
| P2 | Performanse dugih Canvas stranica i česte alokacije pri crtanju | Potrebni `Profile GPU Rendering`, jank benchmark i analiza alokacija; izbjegavati refaktoriranje bez mjerenja |

## 3. Security & privacy audit

**Potvrđeno iz manifesta i koda:** `android:usesCleartextTraffic=false`, `android:allowBackup=false`, nema `INTERNET` dozvole, `MainActivity` je jedina namjerno exported aktivnost (LAUNCHER), lokalni `AppReminder` receiver nije exported; `AppReminderBoot` exported samo za BOOT_COMPLETED i provjerava intent action. Podsjetnik zahtijeva opt-in, dozvola POST_NOTIFICATIONS se provjerava. PendingIntent je immutable, ne zahtijeva vanjski API. Podaci se spremaju u privatni SharedPreferences; nisu šifrirani, ali nisu opisani kao visoko osjetljivi.

**Uvoz kopije:** limit od 512000 bajtova, tipovi postavki, broj elemenata povijesti i game state provjeravaju se prije `Editor.clear()`; pridodana provjera izvornog Android paketa. Dodani testovi sprječavaju brisanje postojećeg napretka pri nevaljanoj kopiji. Korisnik kopiju dobrovoljno izvozi u SAF datoteku, koja može biti dostupna drugim aplikacijama prema izboru korisnika.

**Što nije dokazano:** potpuni penetration test Android OS-a, analitička provjera svih transitive dependency CVE-ova ili fizički test na svakom proizvođaču Androida. Za produkcijsku potvrdu potrebni su SBOM, Gradle dependency insight, ažuran dependency scanner, manifest merged pregled, release signing report i QA na barem nekoliko fizičkih uređaja.

## 4. Dead-code & maintainability audit

- Četiri ručno održavana workflowa ponavljaju Java/SDK/Gradle pripremu; opravdano dijele razne emulator matrice, ali bi mogli koristiti reusable workflow bez gubitka izolacije dozvola.
- `MainActivity.BoardScreen` kombinira crtanje, hitboxove, fontove, gesture input i accessibility; povezanost otežava potpunu provjeru neiskorištenih grana i kontrola.
- **Dokazano neiskorišteno i uklonjeno:** `MainActivity.compactDate()` imao je isključivo vlastitu definiciju; nema poziva u produkcijskom Java kodu, Android instrumentacijskim ni JVM testovima. Uklonjena je metoda bez promjene formata datuma koje koriste ostali zasloni.
- Trenutačno se mrtvi kod **ne briše na temelju naziva**, jer su brojna stanja otvorena indirektno kroz string `navigate()`, `Tap.action`, primatelje broadcasta ili testne fixturee. Potrebno je mapiranje svih navigacijskih ulaza, zatim provjera pokrivenosti iz emulator testa prije svakog uklanjanja.
- `testImplementation org.json` koristi se samo u JVM testovima i nije produkcijska mrežna ovisnost; zadržano odvojeno.
- Zastarjele/deprecated Android API uporabe (npr. `startActivityForResult`) trebaju postupnu migraciju na Activity Result API uz regresiju nakon rekreacije procesa. Nisu uklonjene napamet jer je čuvanje korisničkog napretka važnije od kozmetičke zamjene.

**Zaključak:** provedena je revizija glavnih površina i pripremljen prioritetni popis, ali nije vjerodostojno tvrditi da je završena potpunom kontrolom dosega svake metode. U projektu nema potvrđenih dokaza o zlonamjernom ili dupliciranom kodu.

## 5. 1:1 vizualna matrica i kriteriji prihvaćanja

Postojeće referentne korisničke slike na 941×1672 px uspoređuju se sa stvarnim emulator renderima **bez skaliranja ili automatiziranog pomaka**, uz istu temu i stanje igre. Uvijek zasebno mjeriti globalni RGB MAE i ROI (logo, akcije, kartice, HUD, 3D pločice, podnožje). Greška treba ići dolje, a svaka regresija zahtijeva analizu.

| Zaslon | Status | Prioritet daljnjeg 1:1 rada |
|---|---|---|
| Home | PR #44 (potvrđen MAE ~16.15/255) | Kartice rezultata, 4×4/5×5 kalibracija, dnevni kalendar i donje kartice |
| Splash | Kontrolirano trajanje 2.7 s → Home | 3D sjene udaljenih pločica, točan izvorni logotip i blur |
| Pobjeda / Bravo | Testiran nativni dijalog i stvarni rezultati | Kruna, sve tri zvjezdice, tekst, sjene i razmak gumba |
| Gameplay 4×4, 5×5 | Postoje smoke/screenshot matrice | HUD, osjetljiva područja pločica, brojevi, animacija spajanja, Undo |
| Dnevni izazov i njegovi rezultati | Postoje posebne regresijske slike | Poklon, traka dnevnog cilja, trajni rezultati, dnevno resetiranje |
| Misije i postignuća | Postoje screenshotovi realnog napretka | Visine kartica, ikone, razmaci i nagrade |
| Statistika i povijest | Postoje scenariji 0 i više rezultata | Grafovi, nema-rezultata stanje, hrvatski datumi, pomicanje |
| Teme / Postavke / Backup | Postoje sve svijetle/tamne/živopisne provjere | Aktivni obrubi, kontrasti, statusne trake, odabiri i navigacija |
| Pause, Confirm, Game Over | Postoje zasebne kontrolne slike | Modalni hitboxovi, tekstovi, sjene, neprobojna pozadina |
| Launcher ikona | Android adaptive / round / monochrome kontrole | Vizualne maske i usporedba s originalnim brand sheetom |

Za svaku promjenu: **Android 35 emulator UI QA + Android 36 insets + Gradle build/lint/unit + 320×480/360×600/941×1672/landscape + TalkBack**, bez lažnih screenshot-fixtureova u produkciji. Sve kontrole moraju ostati klikabilne, a rezultat nikad ne smije biti hardkodiran na 2048 samo zbog reference.

## 6. Distribucija i provjere

Spajanje u `main` pokreće zasebno, numerirano izdanje `v1.12.<run_number>`. Za svaku verziju: debug APK, release APK, release AAB i SHA256SUMS. Release workflow potpisuje samo ako su postavljeni odgovarajući privatni secrets. Nakon uspješnog emulator UI QA, **poseban job** (pravo pisanja isključivo na trusted workflow_run main) objavljuje 12 stvarnih PNG slika pod stabilnim imenima `brojko-*.png` u isto izdanje. Time README prikazuje trajne, stvarne snimke, a ne zastarjele GitHub Actions artefakte.

**Preostalo za dokaz pune produkcijske spremnosti:** provjera potpisnog ključa i Play upload mogućnosti, zeleni checks najnovijeg SHA-a, potvrda svih screenshot assets u izdanju, fizički QA uređaja, pun odgovor na Android accessibility testove i izvori za svaki preostali zaslon.

## 7. Izvori za nadograđene biblioteke

Službene stranice: Gradle 9.8.2 release notes; Android Gradle Plugin 9.5.0 release notes; AndroidX Core 1.17.0 (SDK 36 kompatibilnost; 1.19.1 traži SDK 37 koji nije dostupan u stabilnom CI-u); Activity 1.13.0; AndroidX Test 1.7.0 / ext JUnit 1.3.0; Maven Central `org.json:json:20260814`; Node24 GitHub Actions repozitoriji. Preferiraju se stabilne verzije, ne alpha/pre-release.

## 8. Kriterij zatvaranja audita

Nije dovoljno da se aplikacija samo kompilira. Pored automatiziranih provjera treba potvrditi: stvarne screenshotove u README-u, sve funkcije različitih načina igre, backup restore u nepouzdanom stanju, obavijesti na Android 13+, pristupačnost, minimalni SDK i sve Play Console pravne obveze. **Dok to nije dokumentirano i izmjereno, projekt ne treba nazivati 100% 1:1 niti 100% security audited.**
