# BROJKO — Changelog

## Unreleased — zaštita lokalne povijesti (PR #147)

### Ispravci
- Arhiviranje više ne zamjenjuje nečitljiv JSON povijesti praznim
  nizom, čime je prije mogao nestati izvorni lokalni zapis.
- Ako je sadržaj nečitljiv, transakcija se odbija bez izmjene
  spremljene sesije, povijesti ili posljednjeg rezultata.

- Nakon uspješnog vraćanja lokalne kopije ponovno stvaranje Activityja više
  ne prenosi zastarjelu ploču i stranice preko uvezene sesije.

### QA i dokumentacija
- Proširen Android instrumentacijski regresijski test za nečitljive
  vrijednosti i ponovno arhiviranje nakon popravka.
- Dodan instrumentacijski test stvarnog `ActivityScenario.recreate()`
  nakon uvoza spremljene partije.
- Dodane korisničke upute za očuvanje podataka
  (`docs/HISTORY_RECOVERY.md`).

### Ograničenja
- Promjena ne popravlja oštećene datoteke automatski.
- CI build, emulator i stvarna vizualna mjerenja moraju biti potvrđeni
  prije spajanja; nova verzija ne proglašava se izdanom.
- `applicationId` i format pohrane nisu promijenjeni.
