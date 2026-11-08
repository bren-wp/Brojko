# BROJKO — faza 63: sigurnost, mrtvi kod, Google Play i Android ovisnosti

**Datum:** 8. studenoga 2026.  
**Polazište:** `main` commit `5ce84ee4` (uspješan Android release i emulatorski QA).  
**Opseg:** pregled svih **6 produkcijskih Java datoteka**, `AndroidManifest.xml`, verzija ovisnosti u Gradleu, sva 4 GitHub Actions workflowa, sigurnosnih politika, testova, 28 realnih snimki i ključnih tokova igre. Ovo je **statička revizija + dostupni Android instrumentacijski testovi**, ne zamjena za certificirani penetration test, puni SCA niti Google Play Console odobrenje.

## 1. Stvarno potvrđena podudarnost dizajna

Stvarni screenshot iz najnovijeg uspješnog emulatorskog UI runa `42434297231`, rezolucija **941×1672 px**, uspoređen je s izvornim korisnikovim slikama bez promjene rezolucije i bez pomicanja. Niže su RGB MAE vrijednosti 0–255, **ne postotci**.

| Ekran | RGB MAE | Status |
|---|---:|---|
| Misije i postignuća | 18,868 | Najveće ukupno odstupanje |
| Teme i izgled | 17,636 | Drugi prioritet (birači, kartice, boje, natpisi) |
| Statistika | 17,466 | Treći prioritet (naslov, KPI, graf) |
| Postavke | 15,752 | Četvrti prioritet |
| Klasični gameplay 4×4 | 14,193 | Novi PR #82 uspješno poboljšao donje kontrole |
| Dnevni izazov | 14,622 | Usporedba stanja i datuma |
| Pobjeda | 14,335 | 3D kruna, akcije |
| Home | 13,926 | Kartice i ilustracije |
| Splash | 11,273 | 3D logotip i pločice |

**Gameplay dokaz:** Klasično 4×4 cijeli ekran **14,688 → 14,193**, kontrole **20,019 → 17,475**, ikona Novo **41,098 → 24,417**, Poništi **36,816 → 17,490**. Stvarna igra i brojanje bodova ostaju nepromijenjeni. **Ne postoji dokaz 0% odstupanja ni na jednom ekranu.**

## 2. Ovisnosti, najnovije stabilne verzije i testirana kompatibilnost

| Element | Stanje | Revizija |
|---|---|---|
| Android Gradle Plugin | 9.5.1 | Najnovija stabilna grana dostupna projektu, zadržano |
| Gradle | 9.8.2 | Najnovije stabilno 19. 10. 2026., zadržano |
| JDK / Java | 17 | Službeni kompatibilni JDK za AGP 9.5 |
| `compileSdk` | 36 | **Android 17 SDK 37 isproban u CI-ju, nije dostupan** |
| `targetSdk` | 36 | Ispunjava zahtjev za Google Play od 31. 8. 2026. |
| `minSdk` | 24 | Zadržana kompatibilnost s Android 7.0+ |
| AndroidX Core | 1.17.0 | Najnovije sigurno izgradivo u dostupnom CI-ju |
| AndroidX Activity | 1.13.0 | Najnovije stabilno (1.14 je alpha) |
| AndroidX Test | 1.7.0; ext JUnit 1.3.0 | Najnovije stabilno (1.8/1.4 alpha) |
| JUnit 4 | 4.13.2 | Zadržano |
| `org.json` za JVM testove | 20260814 | Zadržano, nije produkcijska runtime ovisnost |

**Pokušaj nadogradnje**: isprobani su `compileSdk=37` i `androidx.core:core:1.19.1`, kao i `sdkmanager "platforms;android-37"` u sva četiri workflowa. Sva tri glavna CI posla pala su u koraku instalacije SDK-a s **`Failed to find package 'platforms;android-37'`** (runs `42435672452`, `42435672431`, `42435672393`), prije kompilacije. Android 17 je javno izdan, ali konkretan automatizirani build image/SDK repozitorij ne nudi stabilni paket kroz postojeću konfiguraciju. Izmjena je zato **vraćena na zadnju izgradivu kombinaciju**, a Dependabot zadržava privremeni izuzetak za Core 1.18+/1.19+ do testiranog prelaska. **Ne izdavati neizgradiv APK/AAB.**

Reference: https://developer.android.com/build/releases/agp-9-5-0-release-notes ; https://developer.android.com/jetpack/androidx/releases/core ; https://developer.android.com/jetpack/androidx/releases/activity ; https://developer.android.com/jetpack/androidx/releases/test ; https://support.google.com/googleplay/android-developer/answer/11926878?hl=hr .

## 3. Sigurnosni audit — komponenta po komponenta

| Područje | Provjereno | Stanje |
|---|---|---|
| Manifest i napadna površina | Jedina javna aktivnost je launcher; ostali primatelji nisu izloženi | Boot receiver sada izričito `exported=false`, akcija mora biti `BOOT_COMPLETED` |
| Dozvole | `VIBRATE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` | Nema `INTERNET`, kontakata, lokacije, skladišta ni SMS-a |
| Mreža i cleartext | `usesCleartextTraffic=false`, nema WebView, HTTP ni analytics SDK-a | Lokalna logika; potvrditi i konačni merged manifest |
| Podsjetnici | Opt-in, runtime POST_NOTIFICATIONS kontrola, immutable `PendingIntent` | Sistemni reboot receiver više nije izvana izložen |
| Storage | Privatni `SharedPreferences`; Android backup isključen | Podaci nisu dodatno kriptografski zaštićeni na razini aplikacije |
| Uvoz/izvoz | SAF URI, najviše 512000 B, verzija formata, package ID, tipovi ključeva | Nema provjere autentičnosti potpisa JSON datoteke — korisnik ručno bira izvor |
| Legacy migracija | Prethodno izravno upisivanje niza pločica iz starog JSON-a | Sada se odbijaju negativne/pogrešne/prevelike pločice i negativni score; commit sesije i uklanjanje legacy zapisa su atomski |
| Engine 2048 | Deterministički RNG, granice 4×4/5×5, max pločica, undo, move/score | Undo sada dodatno odbija `peak>current peak`, preveliki peak i nemoguće brojače spajanja |
| Responzivnost | Testovi na malim/velikim zaslonima, Android 16 insets | Potrebne ručne provjere s fizičkim uređajima i TalkBackom |
| Potpisivanje | Secrets izvan Gita; release signing samo ako su točno konfigurirani | Izrađeni `unsigned` release paketi **nisu** spremni za Play upload bez ispravnog upload certifikata |
| CI i dependency inventory | Gradle build/lint/unit + emulator Android35/36; dodan statički security gate | `releaseRuntimeClasspath` inventura je podloga za naknadnu analizu CVE-a, nije sama po sebi SCA |

### Rizici koji ostaju otvoreni

1. **Sigurnosna kopija je neautentificirani JSON**, bez digitalnog potpisa — napadač koji ima pristup datoteci može izmijeniti rezultate; import je eksplicitna radnja korisnika. Za buduću promjenu formata razmotriti provjeru integriteta i sigurno migriranje postojećih kopija.
2. **Cijeli `MainActivity` je velik**, s Canvas crtanjem, navigacijom, pohranom i uvozom; razdvajanje renderer/repository/controller zahtijeva ekvivalentne 941px screenshotove i gameplay regresiju.
3. **Virtual accessibility čvorovi** i TalkBack trebaju zasebnu audit provjeru.
4. **SBOM, kompletna transitive CVE analiza i vanjski penetration test** nisu provedeni. Dependency report iz CI-ja samo popisuje resolved dependency tree.
5. **Google Play** zahtijeva dodatnu provjeru Play Console: potpis, `versionCode`, točnost prikaza Data Safety obrasca, pravila privatnosti, sadržaj listanja, screenshotova i stvarne produkcijske dostupnosti.

## 4. Full dead-code audit — potvrđena uklanjanja i preostali kandidati

- Pregledano svih šest Java produkcijskih izvora; automatizirani audit pretražuje metode i navodi **kandidate**, bez automatskog brisanja na temelju brojanja teksta. Android lifecycle, refleksija, `navigate(page)`, UI `Tap` callbackovi i `BroadcastReceiver` zahtijevaju strožu analizu dosega.
- **Potvrđeno mrtav kod:** `BoardScreen.classicGameplayHeading()` imao je samo definiciju, bez poziva u svim produkcijskim i testnim Java datotekama. Uklonjeno **1175 bajtova** bez promjene stvarnog `ownerClassicVectorHeading()` koji se renderira na ekranu.
- Prethodno uklonjena `compactDate()` metoda ostaje uklonjena.
- **Ne uklanjati:** `migrateLegacy()` (stara spremljena stanja), `onActivityResult()` (SAF), `onReceive()` (alarm), `onDraw()`, `handleOnBackPressed()` (Android lifecycle) i ilustracijski helperi pozvani dinamičkim UI putem.
- Skripta `scripts/audit-production-code.py` izvršava se pri svakom PR-u i releaseu te označava potencijalno nedohvatljive simbole za ručnu potvrdu.

## 5. Google Play revizija — granice tvrdnji

Google Play od 31. 8. 2026. za nova izdanja/updates Android telefona traži `targetSdk >=36`, što projekt ispunjava. Očuvati `applicationId=com.brendigo.brojko` i `minSdk=24`. Izdanje treba imati produkcijski upload potpis, monotono rastući `versionCode`, transparentne opise i stvarne screenshotove, politiku privatnosti i dosljedan Data Safety obrazac. Status objave i usklađenost sa svim pravilima mogu se konačno potvrditi **samo u Play Console**.

## 6. Kriteriji završetka PR-a

- [ ] Android pull request validation: statički audit, unit, lint, debug/release APK/AAB, compiled package, dependency inventory.
- [ ] Android 16 navigation/insets QA (oba navigacijska načina).
- [ ] Emulator 35: svi instrumentacijski testovi + sve 28 stvarne snimke.
- [ ] Ponovljeni 941×1672 screen ROI audit mora pokazati da security/dead-code promjene nisu promijenile gameplay i UI.
- [ ] Potpisani AAB i Play Console Data Safety kontrola zasebno prije produkcijske objave.

**Važno:** "full audit" znači proći i dokumentirati sve dostupne površine u repozitoriju, ne proglasiti neprovjerene vanjske i runtime aspekte završenima.
