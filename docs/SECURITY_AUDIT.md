# BROJKO — security audit i SCA opseg

## Implementirani tehnički korak
Premještena je validacija i transakcijska primjena lokalnih JSON kopija
u `LocalBackupRepository` bez pristupa Activity-ju, vanjskoj mreži ili
vlastitom filesystemu. Inicijacija SAF dokumenata ostaje u `MainActivity`.
Repozitorij obrađuje stare kopije nedestruktivno. Android manifest i
postojeća politika `allowBackup=false` ostaju nepromijenjeni.

## Napadna površina
- Lokalni uvoz neautentificiranog JSON-a kroz korisnikov SAF odabir:
  **srednji rizik** manipuliranih lokalnih rezultata; ograničenje 512 KB,
  validacija tipova, povijesti i igre; autenticitet nije kriptografski zajamčen.
- Android broadcast/podsjetnici: `exported=false`, opt-in dozvole;
  provjeriti konačni merged manifest.
- Dependency supply chain: SBOM mora popisati i tranzitivne ovisnosti;
  skeniranje poznatih CVE-a mora dati artefakt i zaključak, ne samo
  inventuru `gradle dependencies`.
- GitHub Actions: najmanje potrebne dozvole, bez izlaganja signing secrets;
  postoji otvoren posao zaključavanja svih `uses:` na provjerene SHA-e.
- Android instrumentation i TalkBack na fizičkom uređaju ostaju obvezni
  za konačni zaključak.

## Neproglašeno dovršenim
Nisu provedeni neovisni penetration test, analiza svih CVE nalaza,
fizičko accessibility testiranje ni formalna OWASP MASVS certifikacija.
Sve preostale rizike treba evidentirati s ozbiljnošću, datotekom, dokazom i
reprodukcijskim testom prije zatvaranja.

## Dokazani nalaz iz CI #231: visok rizik alatnog lanca

Puni gradle konfiguracijski SBOM od 139 komponenti prijavio je tri
High/Critical GHSA nalaza: `GHSA-9pwp-9qqc-pr26`,
`GHSA-qp49-qgx5-5m26`, `GHSA-xxph-c9ww-hj94`.
Putanje pripadaju `com.android.tools.lint:lint-gradle:32.5.1` i
njegovim transitive paketima `bcprov-jdk18on` i `guava`.
Izvještaj `docs/SCA_SBOM.md` sadrži ostala tri Medium nalaza.
Nalazi su **otvoreni** za ocjenu i ažuriranje build tooling-a.

Android produkcijski runtime ima odvojen SBOM i release-blocking
Grype kontrolu; uspjeh tog testa bit će potvrđen tek kad CI prođe.

## Dodatne zaštite u PR #137 (13. 11. 2026.)

- Sigurnosne kopije: prije parsiranja lokalnog SAF dokumenta provjerava se
  valjani UTF-8 i najviše 64 razine JSON ugniježđenosti. To se odvojeno
  primjenjuje na vanjski dokument i ugrađene JSON tekstove sesije/povijesti.
  Ograničenje od 512.000 bajtova i v1 format ostaju nepromijenjeni.
- Android instrumentacijski regresijski testovi pokrivaju duboko ugniježđeni
  dokument, duboko ugniježđenu povijest i neispravan UTF-8 te provjeravaju
  da se postojeći lokalni rezultat ne mijenja.
- Sva četiri GitHub Actions workflowa koriste provjerene fiksne commit SHA
  vrijednosti za vanjske akcije. Posebni PR gate i testovi odbijaju nove
  promjenjive oznake ili nepinnane Docker akcije.
- Aktualni release workflow ima dvije jasno odvojene putanje: kada su
  dostupna sva četiri signing secreta, verificira potpisani APK i AAB i
  objavljuje stabilno izdanje; bez produkcijskog ključa objavljuje isključivo
  **unsigned preview** s nepodpisanim release APK/AAB, debug APK-om za testiranje
  i SHA-256 sumama. Preview mora imati GitHub `prerelease: true` i **nije**
  pogodan za Google Play nadogradnju. Provjera da se upload certifikat podudara
  s Play Consoleom ostaje zaseban, neispunjen zahtjev.

**Preostali rizici:** V1 JSON sigurnosne kopije i dalje nemaju
kriptografski dokaz autentičnosti; korisnik ih uvozi po izboru i može
izmijeniti lokalne rezultate. Nijedna nova zaštita ne tvrdi drugačije.
Potrebna je i neovisna provjera je li korišteni upload certifikat
identičan certifikatu registriranom u Google Play Consoleu.
Nalazi za build-toolchain GHSA i neovisni Android runtime/pixel audit nisu
ovime automatski zatvoreni. CI rezultat PR-a i stvarni signing ostaju
obvezni dokazi prije spajanja i objave.

## Dodatna provjera 22. 11. 2026. — strogi uvoz pre-v1 partija (PR na provjeri)

`MainActivity.migrateLegacy()` na početnom commitu `eb72e9d` koristio je
`JSONObject.getInt()/optInt()` i `JSONArray.getInt()`: na Androidu takve
metode mogu pretvoriti decimalan broj ili broj spremljen kao tekst u cijeli
broj bez odbijanja nevaljane izvorne vrijednosti. To je rizik neprimjetnog
mijenjanja lokalnog napretka tijekom migracije starog ključa `board`.

Predložena korekcija koristi postojeće stroge provjere u `TileGame`
za dimenziju ploče, svaku pločicu i postojeći score; odsutan stari
`score` i dalje ima zadanu vrijednost 0. Nevaljan unos mora ostaviti
izvorni `board` ključ netaknut i ne smije stvoriti novu sesiju.
Android instrumentacijski testovi pokrivaju decimalne, tekstualne,
izvanrasponske i valjane pre-v1 vrijednosti. Rezultati testova i release
status moraju se provjeriti na stvarnom PR head commitu: ovaj odjeljak
ne tvrdi da je Android emulator već prošao provjeru.
