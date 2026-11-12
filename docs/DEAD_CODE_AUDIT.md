# BROJKO — dead-code audit (radna revizija)

## Doseg
Provjereni su `MainActivity.java`, `TileGame.java`, `AppReminder.java`,
`AppReminderBoot.java`, `ClassicBrandContours.java`,
`OwnerHeadlineContours.java`, `SplashBrandContours.java` , novoodvojeni
`LocalBackupRepository.java` i `BoardAccessibilityDelegate.java` prema aktualnom `main` stanju.

## Rezultati ovog koraka
- Izmještanje backup metode u `LocalBackupRepository` jest
  **refactoring**, ne brisanje funkcionalnosti/mrtvog koda.
- Nije automatski izbrisana nijedna metoda na temelju jednog tekstualnog poziva.
- `getBackupBytes`, `readBackupBytes` i `validateBackupHistory`
  namjerno ostaju kompatibilni delegati zbog instrumentacijskih testova.
- `migrateLegacy`, `onActivityResult`, `onPause`, `onReceive`,
  `onDraw` i UI callbackovi ostaju aktivni ili mogu biti pozvani indirektno.

## Metodologija za buduća sigurna uklanjanja
1. `scripts/audit-production-code.py` označava isključivo *kandidate*.
2. Provjerava se stvarna putanja poziva (uključujući Canvas, JNI,
   lifecycle, reflection, test i stari JSON format).
3. Svako dokazano uklanjanje dobiva zaseban diff, regresijski test i
   izmjerenu promjenu APK/AAB veličine.

**Granica dokaza:** niti statička provjera niti uspješan compile ne dokazuju
potpunu nedostižnost metode. Puni dead-code audit svih 7.000+ redaka i
njegove potvrđene eliminacije ostaju otvorene.


## PR #135 — ponovno provjereni padovi stvarnog emulatorskog testiranja

Iz Android UI smoke #846 potvrđeno je **5 neuspjeha od 214 testova**.
Svi stari testovi pretpostavljali su sinkroni prijelaz na novu igru ili
Home, iako sada postoji obvezni asinkroni archive barrier. Pet
funkcionalnih scenarija premješteno je iz gigantskog
`ScreensSmokeTest.java` u `GameplayAsyncArchiveFlowTest.java`.
Novi testovi stvarno pritiskaju nativne Canvas akcije, u izoliranom
SharedPreferences području provjeravaju stare partije i ne čekaju
na main niti. Testovi ne prikrivaju neuspjeli commit niti preskaču
realni `GameArchiveCoordinator`.

**Ograničenje:** audit skripta ostaje konzervativna i ne dokazuje
potpunu nedostižnost svih Java/Android callbackova. Bez dokazivog
nedostižnog produkcijskog simbola nije brisana aktivna gameplay metoda.

## Novi modularni resursi nisu mrtav kod

`GameArtworkData` i `GameCalendar` uvode se kao živ izvor za
originalni native Canvas i lokalizaciju perioda. Imena, redoslijed
i vrijednosti 10 ilustracijskih nizova nisu izmijenjeni.
Ne brišemo nijedan lifecycle, delegate ili Canvas callback
samo na temelju tekstualnih rezultata audita.

## PR #135 — dokazano uklanjanje test-only produkcijskih delegata

U svih 28 Android/JVM testnih Java datoteka i 13 ranije skeniranih
produkcijskih klasa pronađeni su sljedeći kandidati koji **više nemaju
produkcijski pozivni lanac**:

| Izvorni simbol | Dokaz pozivatelja | Akcija |
|---|---|---|
| `MainActivity.getBackupBytes()` | samo Android testovi | uklonjen |
| `MainActivity.readBackupBytes()` | samo Android testovi | uklonjen |
| `MainActivity.validateBackupHistory()` | samo Android testovi | uklonjen |
| `MainActivity.archive()` | samo Android testovi nakon asinkronog refactoringa | uklonjen |
| `LocalHistoryRepository.archive()` | starija metoda testova, produkcija koristi `archiveSnapshot` | uklonjen |
| `LocalBackupRepository.restore()` | sync testna kompatibilna fasada, produkcija koristi `validate/apply` | uklonjen |

Testna kompatibilnost i provjere stvarnog upisa **nisu izbrisane**:
`BackupTestBridge.java` postoji isključivo u
`app/src/androidTest`, a `LocalHistoryRepositoryTest` i
`ScreensSmokeTest` sada testiraju produkcijski `archiveSnapshot`.

**Nisu dead code:** četiri AndroidX
`ExploreByTouchHelper` override metode u
`BoardAccessibilityDelegate`. One su pozvane framework-dispatch
putanjom, pa ih obična brojanja izravnih Java poziva ne prepoznaju.

Brisanje nepoznatih metoda, proizvoljnih vektorskih kontura, funkcija
povratne kompatibilnosti datoteka i Android lifecycle callbackova
nije dopušteno bez dokaza. Rezultat nije tvrdnja o potpunom 100%
dead-code auditu: preostaje ručna inspekcija `BoardScreen` i
verifikacija R8 usage reporta nakon objavljene minificirane gradnje.
