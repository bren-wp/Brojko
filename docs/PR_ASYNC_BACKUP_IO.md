# BROJKO — PR: background SAF i validacija sigurnosne kopije

## Svrha
Ukloniti sporo `ContentResolver.openInputStream/openOutputStream` čitanje i
zapis te obradu neprovjerenog JSON-a s Android glavne niti, bez promjene
formata `brendigo.brojko.backup.v1`, `applicationId` ili izgleda.

## Promijenjene granice
- Novi `BackupDocumentIo`: isključivo SAF tokovi, maksimalno **512.000 B**.
  Truncation mode `wt` izbjegava ostatke starog sadržaja pri prepisivanju.
- `LocalBackupRepository.validate()`: CPU-intenzivna i nepoznata
  parsiranja obavljaju se na serijskom workeru, a ne u Canvas event callbacku.
- `LocalBackupRepository.apply()`: jedina, vrlo kratka atomska
  transakcija izvršava se na glavnoj niti nakon potpune provjere.
  To nije potpuno uklanjanje svih sinkronih diskovnih commit operacija.
- `MainActivity`: privremeno blokira interakciju tijekom importa, a
  vraća je nakon uspjeha/neuspjeha. Nakon uspješnog importa postavlja se
  `skipPersistOnPauseAfterImport` prije rekreiranja Activityja.
- Odlazak/rotacija tijekom uvoza: obrada datoteke može završiti, ali
  promjena podataka se ne primjenjuje na uništenu aktivnost.
  Korisnik ponovno odabire uvoz. Izvoz može dovršiti započeti zapis.
- Existing `getBackupBytes()`, `readBackupBytes()`,
  `LocalBackupRepository.restore()` ostaju kompatibilni za testove
  i stara lokalna spremanja. Legacy i partial kopije ostaju
  nedestruktivne; potpuni manifest vrijedi kao prije.

## Testna matrica
- JVM: kratki tokovi, exactly-512000, 512001 odbijanje, prekinuto
  čitanje, ispravni upisi i odbijanje prevelikog izvoza.
- Android: validacija na zasebnoj radnoj niti bez ikakve promjene
  prefova prije `apply`, potpuni uvoz i odbijanje neispravnog broja.
- Postojeći: backup/history/session/undo, Android 15/16 insets,
  emulator 941×1672, TalkBack virtualne radnje i security/SCA.
- Manual: SAF provider različitih proizvođača, podjela zaslona,
  back/rotation tijekom importa, spori dokumenti i fizički uređaji.

## Preostali rizici
`SharedPreferences.Editor.commit()` za važan restore i arhiviranje
partije i dalje su sinkroni na UI niti. Njihovo asinkrono izvođenje
zahtijeva transakcijsku serijalizaciju session+history, lifecycle
testove i on-device mjerenje prije promjene. Ovaj PR ne tvrdi
da je cjelokupno spremanje neblokirajuće.
