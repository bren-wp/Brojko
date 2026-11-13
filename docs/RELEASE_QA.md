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

## Automatska izdanja APK + AAB (potpis nije obvezan za GitHub)

**Za svako uspješno izvršavanje release workflowa** nakon spajanja u
`main`, te za ručno pokretanje ili `v*` tag, objavi novo GitHub
izdanje tek nakon Android builda, Linta, unit testova, instrumentacijskih
testova, stvarnih emulator snimki, SDK provjere i sigurnosnog skeniranja.
GitHub broj izvođenja koristi se za naziv automatskog izdanja.

### Bez konfiguriranog produkcijskog upload ključa

Automatski objavi jasno označeno `unsigned preview` izdanje, uz
`prerelease=true` i sljedeće datoteke:

| Datoteka | Namjena | Potpis |
| --- | --- | --- |
| `app-debug.apk` | Instalabilna verzija za razvojno testiranje | Android debug ključ; **nije** ključ Google Play aplikacije |
| `app-release-unsigned.apk` | Release APK za eventualno naknadno potpisivanje | Nepotpisan; **ne može se izravno instalirati** |
| `app-release.aab` | Android App Bundle za eventualno naknadno potpisivanje | Nepotpisan; **ne može se poslati na Google Play kao konačna nadogradnja** |
| `SHA256SUMS` | Provjera preuzetih APK/AAB datoteka | SHA-256 suma sva tri distribuirana paketa |

Objaviti i dva CycloneDX SBOM izvještaja i **28 stvarnih emulator
snimki**, bez generiranja zamjenskih slika. Runtime CVE nalaz visoke ili
kritične ozbiljnosti, pad testova, nepostojeća datoteka ili pogrešan
SDK blokiraju objavu; potpis nije uvjet za GitHub pre-release.

### S postojećim produkcijskim ključem

Ako su sve četiri repository secrets vrijednosti prisutne:

- `BROJKO_KEYSTORE_B64` (Base64 kodirani postojeći keystore)
- `BROJKO_STORE_PASSWORD`
- `BROJKO_KEY_ALIAS`
- `BROJKO_KEY_PASSWORD`

objaviti `app-release.apk`, `app-release.aab` i `SHA256SUMS`
kao potpisano izdanje tek nakon provjere `apksigner` i
`jarsigner` potpisa. Djelomično konfigurirani secrets ostaju greška;
nikada ne prebacivati na debug potpis kada je produkcijski konfiguriran
samo djelomično. Keystore i lozinke ne ulaze u Git ni logove.

**Potpisan nije isto što i ispravno potpisan za postojeće Play
instalacije.** Prije objave u Google Play Console usporediti fingerprint
upload certifikata, ispitati `versionCode` i nadogradnju preko stare
verzije bez gubitka lokalnog napretka.

### Galerija i regresijski testovi

Odvojeni `android-ui-qa.yml` smije dodati realne screenshotove
GitHub izdanju samo ako ono već sadrži `SHA256SUMS`, AAB te potpuni
skup paketa za potpisano ili nepotpisano izdanje.
`tools/test_release_gate.py` provjerava main, tag i ručno izdanje
bez signing ključa, odbijanje nepotpunih ključeva, sadržaj workflowa
i točne SHA256SUMS stavke za nepotpisano izdanje.

## Poznat uzrok prethodnog pada

Nakon PR-a #137, `Build and release Android`
[run 43510154436](https://github.com/bren-wp/Brojko/actions/runs/43510154436)
pao je zato što su nedostajale četiri signing tajne. PR #138
omogućio je samo post-merge QA bez GitHub izdanja.
Ovaj zahvat dodatno dopušta stvarno GitHub izdanje bez
produkcijskog potpisa na izričit zahtjev korisnika, uz ispravnu
oznaku datoteka i zabranu prikazivanja debug/unsigned paketa kao
spremnih za Google Play.
