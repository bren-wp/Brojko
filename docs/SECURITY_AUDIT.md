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
