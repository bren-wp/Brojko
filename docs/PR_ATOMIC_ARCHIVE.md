# PR — Sigurno arhiviranje partije izvan glavne niti

## Invarijante

1. **Ne kreiraj novu sesiju dok povijest nije uspješno trajno pohranjena.**
2. Povijesni red i stara session-JSON vrijednost s `archived=true`
   moraju se atomarno zapisati u istom SharedPreferences commitu.
3. Za vrijeme commit-a nemoj mijenjati ploču, izvršavati Undo ili
   dopustiti da `onPause` spremi zastarjelu sesiju.
4. Jedan serijski worker i jedna transakcija na proces; redoslijed
   događaja je determinističan. Dvostruki klik ne može duplicirati red.
5. Rotation zadržava aktivnu transakciju u procesu i ponovno veže
   Activity. Ako proces padne nakon upisa, stari session ostaje
   `archived=true`, pa se ne arhivira ponovno.
6. Neispravan ili izmijenjen session snapshot ne smije izbrisati
   povijest ili pokrenuti novu partiju.
7. Nema promjene formata korisničkih podatka, ID-a aplikacije,
   korisničkog dizajna ni drugih komponenata.

## Provjera

- PR Gradle testovi, Lint, obvezni SBOM/CVE.
- Instrumentacijski testovi: atomic session+history, idempotencija,
  mismatch snapshot odbijanje, neuspješan commit i sporo spremanje.
- API 36: navigacija i screenshot QA.
- Ručno na uređaju: rotacija tijekom sporog commita, prekid procesa
  neposredno prije/poslije upisa, reset sustava/datoteke i I/O greške.

## Granice

`SharedPreferences.apply()` za uobičajene češće promjene ostaje
asinkron. `migrateLegacy()`, te potvrda vraćene sigurnosne kopije
preko SAF-a, i dalje koriste izravni atomarni commit na UI niti.
Ne može se tvrditi da *nijedna* operacija spremanja ne blokira.
