# BROJKO — sigurno vraćanje lokalnih sigurnosnih kopija

## Provjereni problem u prethodnoj implementaciji

U `MainActivity.readBackupBytes()` kopija je mogla biti proglašena potpunom
ako je sadržavala osam uobičajenih postavki (`best`, `games`, `total`,
`theme`, `style`, `sound`, `vibrate`, `notifications`).
Poziv `SharedPreferences.Editor.clear()` zatim je uklanjao i polja koja
nisu bila u kopiji, primjerice `session` i `history`.
To je rizik gubitka korisničkog napretka.

## Kompatibilno ponašanje

- Postojeći format `brendigo.brojko.backup.v1` ostaje nepromijenjen.
- Novi izvoz dodaje `scope: "full"` i `includedKeys` koji nabraja
  točno ključeve u izvezenom `data` objektu.
- Import bez tih metapodataka (stare kopije) uvijek je **nedestruktivan**:
  mijenja samo provjerene ključeve koji su prisutni.
- Ako se popis ključeva ne slaže sa sadržajem, import je također
  nedestruktivan, čak i ako datoteka tvrdi da je potpuna.
- U izričitoj potpunoj kopiji uklanjaju se samo izostavljena poznata polja
  iz `BACKUP_KEYS`, ne i drugi ili budući ključevi postavki.
- Validacija sadržaja i `commit()` ostaju prije potvrde uspjeha;
  stranom `package` identitetu i neispravnom JSON-u i dalje se odbija pristup.

## Regresijski scenariji

Dodani instrumentacijski testovi u
`OwnerBackupHistoryPhase134Test.java` obuhvaćaju:
1. osam legacy postavki uz postojeći `session` i `history`;
2. preinačeni novi backup sa zastarjelim `includedKeys` manifestom;
3. izričitu potpunu zamjenu s uklanjanjem samo poznatih izostavljenih polja;
4. očuvanje nepoznatog/budućeg `futureSetting` ključa.

**Granica provjere:** statičko čitanje i testni kod ne dokazuju izvršavanje
na Androidu. CI mora izvršiti instrumentacijski test prije distribucije.
Sustav ne jamči kriptografsku autentičnost ručno izabrane JSON kopije.
