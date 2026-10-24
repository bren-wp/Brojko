# Privatnost — Brojko by Brendigo

**Verzija dokumenta:** 24. listopada 2026.

Brojko je offline logička igra. Izvorni kod ove verzije ne prikuplja ni ne šalje osobne podatke razvojnom timu. Nema oglasa, korisničkih računa, telemetrije, sustava analitike ni prijenosâ na vlastiti poslužitelj.

## Podaci na uređaju
Igra lokalno sprema mrežu pločica, rezultat, rekorde, povijest partija, datum dnevnog izazova i postavke izgleda, zvuka i vibracije u privatnim Android postavkama (SharedPreferences). Podaci ostaju na uređaju dok ih korisnik ne izbriše brisanjem podataka aplikacije ili deinstalacijom. Aplikacija ne traži pristup lokaciji, kontaktima ni osobnim datotekama.

## Dozvole
Aplikacija traži samo dozvolu **VIBRATE** za kratku vibraciju pri potezu kada je uključena. Vibracija se može isključiti u postavkama.

## Dijeljenje rezultata
Kada korisnik pritisne **Podijeli**, Androidov standardni dijalog za dijeljenje šalje tekst rezultata u aplikaciju koju korisnik sam odabere. Brojko ne šalje rezultat automatski.

## Djeca
Igra nema društvene značajke, oglase ni kupnje.

## Kontakt
[brendigo.com](https://brendigo.com)

Ovaj tekst opisuje funkcionalnosti sadržane u izvornom kodu. Prije produkcijske objave potrebno je provjeriti ponašanje točne izgrađene verzije, izjave u Google Play Consoleu i konačne zahtjeve za politiku privatnosti.
