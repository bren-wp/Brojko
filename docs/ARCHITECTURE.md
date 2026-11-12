# BROJKO — tehnička arhitektura i postupni refactoring

## Trenutačni korak: odvajanje lokalnih sigurnosnih kopija

`MainActivity` ostaje Android lifecycle, SAF i Canvas koordinator. Odvojena je
`LocalBackupRepository` s odgovornostima:

- serijalizacija v1 JSON kopije iz privatnih SharedPreferences
- provjera formata, aplikacijskog identiteta i granica podataka
- provjera kompletne/legacy/sparse kopije
- validacija povijesti te spremljene `TileGame` sesije
- atomska primjena verificiranih vrijednosti u SharedPreferences

Public-facing paket, ključ `brojko`, format v1 i Android Intent requestCode
601/602 ostali su isti. `MainActivity.getBackupBytes()`,
`MainActivity.readBackupBytes()` i
`MainActivity.validateBackupHistory()` ostaju kompatibilni pozivi.
Flag `skipPersistOnPauseAfterImport` pripada Activity-ju i dalje se postavlja
tek nakon uspješnog restorea.

### Granice

- `TileGame`: čista deterministička pravila 4×4/5×5/daily, undo i RNG
- `LocalBackupRepository`: lokalni podaci bez reference na Activity i View
- `MainActivity`: lifecycle, navigacija, poslovna orkestracija i trenutačni
  Canvas renderer
- `AppReminder` / `AppReminderBoot`: lokalni opt-in podsjetnici

## Sljedeći sigurni koraci

1. Odvojiti navigacijski state/router, prvo uz testove svih Back/modala.
2. Izdvojiti Canvas renderere po zaslonima bez promjene koordinata i
   `Paint` stanja ili privremenog zamjenjivanja prikaza statičnim slikama.
3. Izdvojiti statistiku i sustav nagrada uz migracijske testove.
4. Za svaku promjenu ponoviti Android unit/instrumentation QA i per-screen
   pixel ROI audit na identičnom 941×1672 viewportu.

Ovo nije deklaracija da je cijeli `MainActivity` već refaktoriran.
