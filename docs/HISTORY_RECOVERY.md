# BROJKO — očuvanje oštećene povijesti partija

## Zašto je važna ova zaštita

Starija implementacija na oštećeni zapis `history` reagirala je
praznim nizom i njegovim upisivanjem, čime je mogla trajno prebrisati
izvorni zapis. Prva zaštita u PR-u #147 blokirala je arhiviranje, ali je
regresijski UI test ispravno zahtijevao da igra i dalje radi.

## Kako sada radi oporavak

- Ako `history` nije valjani JSON niz, izvorna sirova vrijednost
  sprema se u zasebni lokalni ključ `historyRecovery`.
- U **istoj atomarnoj transakciji** spremaju se nova povijest s
  dovršenom partijom, arhivirana sesija i najnoviji rezultat.
- `historyRecovery` ostaje dostupan u ručno izvezenoj lokalnoj
  sigurnosnoj kopiji. Prilikom uvoza novih/ranijih kopija čuva se
  postojeći zapis za oporavak ako ga stara potpuna kopija nije sadržavala.
- Ako već postoji **drugačiji** zapis `historyRecovery`, novi se
  ne smije prepisati: arhiviranje se tada odbija dok se slučaj ne
  razriješi, kako se ne bi izgubila ranija potencijalno oporavljiva
  povijest.
- Normalna ispravna povijest zadržava prethodni redoslijed, arhiviranje
  bez duplikata i limit od 20 zapisa. Nema promjena UI-a, pravila igre,
  `applicationId` ni Android dozvola.

## Koraci za korisnika

1. Ne brišite podatke aplikacije i nemojte je ponovno instalirati.
2. U Postavkama izvezite lokalnu sigurnosnu kopiju kroz Androidov
   sustav za odabir datoteka. Ta kopija uključuje i `historyRecovery`.
3. Spremite izvezenu datoteku na sigurno mjesto prije mogućeg vraćanja
   starije sigurnosne kopije.
4. Nečitljivi sadržaj nije automatski popravljen, ali je sačuvan za
   kasniju dijagnostiku. Opseg ručnog popravka ovisi o sadržaju.

## Testiranje

- `LocalHistoryRepositoryTest`: nevaljane JSON vrijednosti, arhiviranje
  bez gubitka izvornika, ponovni pokušaji i zaštita već postojećeg
  drugačijeg sadržaja `historyRecovery`.
- `ScreensSmokeTest`: stvarna dovršena partija ostaje arhivirana i
  nečitljivi ulaz očuvan u oporavku.
- `BackupValidationWorkerTest`: lokalni izvoz/uvoz oporavljivih
  podataka, uključujući stare kompletne kopije bez novog ključa.

Kod i testovi opisuju očekivano ponašanje; uspješno izvršavanje
potvrđuje se tek rezultatima GitHub Actions instrumentacije.
