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
