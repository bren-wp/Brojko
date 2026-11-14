# BROJKO — Changelog

## Unreleased — zaštita lokalne povijesti (PR #147)

### Ispravci
- Nečitljivi stari JSON povijesti čuva se pod `historyRecovery` u istoj
  atomarnoj transakciji u kojoj se arhivira nova dovršena partija.
- Uz očuvanje izvorne nečitljive vrijednosti igra nastavlja raditi,
  bez gubitka zadnjeg rezultata i bez dupliranja zapisa.
- Sprečava se prepisivanje drugačijeg već spremljenog oporavka.
- Lokalna sigurnosna kopija uključuje oporavljivi zapis; uvoz starijih
  potpunih kopija ne briše kasnije nastalu povijest za oporavak.

- Nakon uspješnog vraćanja lokalne kopije ponovno stvaranje Activityja više
  ne prenosi zastarjelu ploču i stranice preko uvezene sesije.

### QA i dokumentacija
- Prošireni Android instrumentacijski regresijski testovi za nečitljive
  vrijednosti, atomarno očuvanje, sigurno arhiviranje i oporavak kroz kopiju.
- Dodan instrumentacijski test stvarnog `ActivityScenario.recreate()`
  nakon uvoza spremljene partije.
- Dodane korisničke upute za očuvanje podataka
  (`docs/HISTORY_RECOVERY.md`).

### Ograničenja
- Promjena ne popravlja oštećene datoteke automatski.
- CI build, emulator i stvarna vizualna mjerenja moraju biti potvrđeni
  prije spajanja; nova verzija ne proglašava se izdanom.
- `applicationId` i format pohrane nisu promijenjeni.
