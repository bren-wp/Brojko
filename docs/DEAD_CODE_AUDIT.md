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
