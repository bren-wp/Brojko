# BROJKO — Changelog

## Unreleased — faza 151: mjerljiva QA priprema Pobjede

- Dodano zasebno RGB MAE i exact-RGB mjerenje 3D krune, zlatnih zraka, natpisa i akcija Pobjede na izvornom viewportu 941×1672.
- Pixel audit podržava `--screen 5` za samostalnu provjeru Pobjede bez svih devet privatnih referenci, uz automatizirane sintetičke testove metrike (ne vlasničkih slika).
- README galerija povezana je s 28 stvarnih PNG-ova provjerenog v1.12.250 izdanja.
- Release QA štiti GitHub prerelease metapodatke za nepotpisane preview pakete; bez izmjene gameplaya ili korisničkih podataka.
- Nakon pronalaska izvornog vlasničkog PNG-a i usporedbe s v1.12.250 stvarnim emulatorom napravljena je ograničena korekcija vodoravnog zlatnog gradijenta i narančastog podnožja krune samo na svijetlom referentnom portretu; novi PR MAE još mora potvrditi poboljšanje. 1:1 nije potvrđen.

## v1.12.250 — 14. studenoga 2026.

- Spojeni PR #150: vizualne korekcije Naslovnice i postojeći emulator QA.
- Objavljeno izdanje sa 28 stvarnih PNG slika, instalabilnim testnim `app-debug.apk`, nepotpisanim release APK/AAB, SBOM datotekama i SHA256SUMS.
- GitHub `prerelease` metapodatak zahtijeva zasebnu provjeru: izdanje je prvotno označeno `false` iako nema produkcijski upload potpis.

## Povijesna implementacija — zaštita lokalne povijesti (spojeni PR #147)

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
