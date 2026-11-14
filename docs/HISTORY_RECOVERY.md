# BROJKO — očuvanje oštećene povijesti partija

## Pozadina

Stariji kod u `LocalHistoryRepository.archiveSnapshot()` na nečitljiv
JSON iz postavke `history` reagirao je stvaranjem novog praznog
`JSONArray`. Sljedeći `commit()` trajno je zamijenio izvorni sadržaj
i time otežao oporavak starih rezultata.

## Nova zaštita (PR #147)

- Ako `history` nije valjani JSON niz, arhiviranje vraća `false`
  prije izmjene `session`, `history` ili `lastScore`.
- Trenutačna partija i originalan nečitljiv zapis ostaju sačuvani.
- UI prikazuje postojeću poruku da partija nije spremljena.
- Ispravna povijest i dalje se arhivira s dosadašnjim ograničenjem
  od 20 zapisa; ne mijenjaju se `applicationId`, Android dozvole,
  shema podataka ni izgledi zaslona.

## Postupak za korisnika

1. Nemojte brisati podatke aplikacije ili je ponovno instalirati.
2. U Postavkama izvezite lokalnu sigurnosnu kopiju kroz Androidov odabir
   datoteka i sačuvajte original.
3. Ako imate prethodnu **provjerenu** sigurnosnu kopiju, možete razmotriti
   njezino vraćanje. Stari zapis nemojte prepisati bez posebne kopije.
4. Ako problem ostane, sačuvajte izvoz za daljnju dijagnostiku.

**Ograničenje:** nečitljiv JSON ne popravlja se automatski. Korisnik
može nastaviti postojeću partiju, ali novu partiju nakon arhiviranja
nije moguće pokrenuti dok povijest ne bude popravljena ili vraćena
iz ispravne sigurnosne kopije. Nije dodan destruktivan “reset”.

## QA

Instrumentacijski test `LocalHistoryRepositoryTest` provjerava
nevaljane JSON ulaze, netaknutu izvornu povijest, aktivnu sesiju i
`lastScore`; nakon popravka provjerava uspješno arhiviranje.

Ova dokumentacija opisuje namjeravano ponašanje promjene; uspjeh
Android instrumentacijskih testova utvrđuje se zasebno u CI-u.
