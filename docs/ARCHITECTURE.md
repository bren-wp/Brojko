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

## Izvan glavne niti: SAF import/export (novi PR)

- `BackupDocumentIo` radi datotečno čitanje i zapis preko SAF-a na
  jednokratnoj serijskoj pozadinskoj radnoj niti aktivnosti.
- `LocalBackupRepository.validate()` na radniku provjerava granicu
  od 512.000 bajtova, format, tipove i spremljenu sesiju/povijest;
  vraća samo verificirani `RestorePlan`, bez promjene preferencija.
- `LocalBackupRepository.apply()` izvodi jedini atomski `commit()` na
  glavnoj niti nakon validacije, radi izbjegavanja utrke sa spremanjem
  aktivne partije; taj kratki, rijetki commit i dalje može blokirati i
  nije proglašen potpuno asinkronim.
- Tijekom importa zaslonski unos, tipkovnica i virtualne TalkBack akcije
  privremeno su blokirani. Pri promjeni orijentacije ili uništenju Activityja
  nedovršena validacija se odbacuje **bez promjene korisničkih podataka**.
- Eksport nastavlja već započeti SAF zapis i kada se aktivnost zatvori;
  ne šalje UI obavijesti uništenoj aktivnosti.

## Serijalizirano arhiviranje (PR: atomic archive worker)

- `GameArchiveCoordinator` prima nepromjenjivi JSON snapshot aktivne
  partije na glavnoj niti, a **jedan** process-scoped executor osigurava
  redoslijed komitova. Tijekom arhiviranja su Canvas, DPAD, TalkBack i
  Android Back privremeno blokirani.
- `LocalHistoryRepository.archiveSnapshot` na radnoj niti jednom
  `SharedPreferences.Editor.commit()` operacijom upisuje i
  `history` i staru `session` s `archived=true`. Ako proces
  stane nakon commita, pri ponovnom pokretanju stara partija više nije
  neoznačena/neplanirano dvostruko arhivirana.
- Glavna nit zamjenjuje `game` i radi postojeći
  `persist().apply()` tek nakon povratne informacije uspješnog upisa.
- `onPause` i `persist` ne prepisuju staru partiju dok je
  transakcija aktivna. Activity nakon rotacije preuzima čekajuću
  transakciju preko `attach/detach` bez veze na stari Activity.
- `MainActivity.archive()` je zadržan samo radi kompatibilnosti
  ranijih instrumentacijskih testova; produkcijske
  `newGame/endSession` grane koriste radnika.
- `migrateLegacy()` i restore SAF sigurnosne kopije još zadržavaju
  pojedinačne sinkrone transakcije te su odvojeni sljedeći zadatak.

## Izolacija stabilne native grafike i hrvatskih razdoblja

- `GameArtworkData.java`: konture Home/Bravo te svi nepromjenjivi
  victory/misijski nizovi premješteni su bez promjene ijedne
  koordinatne i numeričke vrijednosti. `MainActivity` zadržava
  izvorna imena polja kao alias, stoga Canvas ostaje nepromijenjen.
- `GameCalendar.java`: sve hrvatske oznake datuma i perioda
  koristi identičan postojeći algoritam. Metode aktivnosti su
  delegati, a dodani JVM testovi potvrđuju format i oblik trajanja.
