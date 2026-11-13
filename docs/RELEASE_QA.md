# Brojko — Release quality checklist

## Što je provjereno automatizacijom
- Determinističke 2048 operacije u JVM testovima, uključujući slučaj ponovnog spajanja, RNG i undo.
- Android lint, API 36 Gradle build, APK/AAB izrada.
- API 35 emulator: otvaranje svih 16 primarnih stanja, početak igre, spremanje stvarnih Android snimki.
- Kompaktni viewport: interaktivno pomicanje dugih ekrana; gumbi ostaju dostupni.

## Potrebne ručne provjere prije Google Playa
- Usporediti referentni grafički identitet i ekran po ekran, osobito font (referenca koristi Nunito), stil sjena, ilustracije i ikonu, sve u stvarnom prikazu.
- Provjeriti 360×640, 360×780, 393×873, 412×915, 1080×2400 uz sistemske trake i izreze.
- Testirati kratke/duge swipe pokrete na 4×4 i 5×5, promjene tema, zaslone pobjede, poraza i bez interneta.
- Provjeriti TalkBack elemente i touch target veličine, osobito na malim ekranima.
- Testirati oporavak nakon prisilnog zatvaranja, nadogradnje, invalidne sigurnosne kopije i 24h rollovera.
- Postaviti produkcijski upload keystore u GitHub Actions secrets, potvrditi potpis release AAB-a, testirati kroz Play internu testnu stazu.

## Ograničenja
Programatski Canvas zasloni ne predstavljaju još pikselno identičnu reprodukciju korisničkih dizajnerskih referenci. GitHub Actions build i smoke test ne zamjenjuju ručni vizualni i funkcionalni QA na stvarnim uređajima. Nepotpisani release AAB nije spreman za Google Play produkciju.

## Obvezna politika potpisanog stabilnog izdanja (PR #137)

U GitHub Actions spremiti postojeći Android upload ključ i pripadajuće
vrijednosti kao *repository secrets*:
`BROJKO_KEYSTORE_B64`, `BROJKO_STORE_PASSWORD`,
`BROJKO_KEY_ALIAS` i `BROJKO_KEY_PASSWORD`. Ne spremati
keystore ili lozinke u Git repozitorij niti ih ispisivati u logove.

Nakon pripreme ključa release postupak provjerava alias pomoću
`keytool`, izgrađuje i provjerava potpis APK-a s `apksigner` te
potpis AAB-a s `jarsigner`. Pri nedostatku bilo koje tajne,
nepostojećem potpisu ili neispravnom paketu javna objava se zaustavlja.
Objavljeni `SHA256SUMS` opisuje samo `app-release.apk` i
`app-release.aab`; debug APK se koristi samo za testiranje.

**Potpisan** nije isto što i **ispravno potpisan za nadogradnju**:
prije slanja na Google Play usporediti fingerprint upload certifikata
s Play Console, potvrditi monotoni `versionCode` i instalirati novo
izdanje preko prethodnog na testnom uređaju bez brisanja lokalnih
rezultata. Ne tvrditi da je distribucija spremna bez tih dokaza.

## Post-merge QA bez konfiguriranog potpisivanja

Nakon PR-a #137 utvrđen je stvarni pad workflowa na spojenom
commitu `37cb5b61d5ea`: izvršavanje
[43510154436](https://github.com/bren-wp/Brojko/actions/runs/43510154436)
zaustavilo se na koraku `Require configured production upload key`
jer četiri signing secreta nisu postavljena u GitHub Actions okruženju.

Ispravljena je razlika između obveznog QA nakon spajanja i
objavljivanja **potpisanog** stabilnog izdanja:

- `push main` bez ijednog signing secreta izvršava build,
  Lint, JVM/instrumentacijske testove, SBOM i sigurnosni sken;
  rezultate čuva kao jasno označene **QA-only, not for distribution**
  artefakte. U GitHub Release ne objavljuje APK/AAB.
- `push main` s ispravno konfiguriranim svim ključevima i dalje
  automatski provjerava potpise i objavljuje stabilni Release.
- `workflow_dispatch` i `v*` tag zahtijevaju sve signing
  podatke i moraju prijaviti **failure** kada nedostaju.
- Djelomično konfigurirani signing uvijek je pogreška i
  blokira objavu. Ne postoje fallback ključevi ili debug
  potpis kao zamjena za produkcijski upload potpis.
- Odvojeni UI QA workflow dodaje stvarnu galeriju isključivo ako
  odgovarajuće izdanje već ima `app-release.apk`,
  `app-release.aab` i `SHA256SUMS`.
  Kada izdanje ne postoji, screenshotovi ostaju u QA artefaktima.

Ovo sprječava lažni pad post-merge testova, **ali ne znači da je
produkcijsko izdanje objavljeno**. Da bi ponovno bilo automatsko,
potrebno je u GitHub Settings → Secrets and variables → Actions
konfigurirati sve četiri tajne i usporediti certifikat s Play Console.
