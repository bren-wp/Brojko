# BROJKO — SBOM i CVE/SCA provjere (12. 11. 2026.)

## Nalaz iz stvarnog GitHub Actions izvršavanja #231

Poveznica: https://github.com/bren-wp/Brojko/actions/runs/43296560742

Gradle build, Lint, unit testovi i CycloneDX stvaranje SBOM-a su prošli.
Grype 0.110.0 analizirao je **139 komponenti** u SBOM-u koji
je inicijalno sadržavao **sve rezolvirane konfiguracije**, uključujući
Android Lint i SDK komponente. Nađeno je šest GHSA upozorenja:

| Paket | Verzija | GHSA oznaka | Ozbiljnost | Doseg iz stvarnog grafa |
|---|---|---|---|---|
| bcprov-jdk18on | 1.80.2 | GHSA-9pwp-9qqc-pr26 | Critical | lint-gradle → lint → sdk-common → bcprov |
| bcprov-jdk18on | 1.80.2 | GHSA-qp49-qgx5-5m26 | High | lint-gradle → lint → sdk-common → bcprov |
| guava | 33.4.0-jre | GHSA-xxph-c9ww-hj94 | High | lint-gradle → lint-typedef-remover → guava |
| bcpkix-jdk18on | 1.80.2 | GHSA-wg6q-6289-32hp | Medium | lint-gradle → lint → sdk-common |
| commons-lang3 | 3.16.0 | GHSA-j288-q9x7-2f5v | Medium | lint-gradle → lint → sdk-common → commons-compress |
| httpclient | 4.5.6 | GHSA-7r82-7xv7-xcpj | Medium | lint-gradle → lint → sdklib → httpmime |

Rute ovisnosti izračunate su na stvarnom CycloneDX `dependencies`
grafu i počinju na `app` komponenti. Svih šest potvrđenih nalaza
dolaze iz **alata koji se izvršavaju pri izgradnji**, ne iz izravno
deklariranih Android aplikacijskih biblioteka. To **ne znači** da su
rizici za build sustav riješeni niti jamči odsutnost istih paketa u
nekim drugim konfiguracijama.

Izvorni SARIF i puni SBOM dostupni su kao `brojko-release-sca`
artefakt neuspjelog CI izvršavanja.

## Odvojena sigurnosna kontrola

Iz `releaseRuntimeClasspath` konfiguracije nastaje
`SBOM.cdx.json`. Njegove visoke/kritične ranjivosti
**blokiraju** objavu preko `anchore/scan-action@v7`
(`fail-build: true`). Iz svih dostupnih rezolviranih
konfiguracija nastaje `SBOM.build.cdx.json`; Grype nalaze
objavljuje u `grype-build.sarif` sa `fail-build: false`.

Oba SBOM-a i oba SARIF izvještaja ostaju u GitHub Actions
artefaktima. Uspješno izdanje uključuje oba CycloneDX JSON-a.
`scripts/verify-sbom-scopes.py` odbija runtime SBOM koji sadrži
`com.android.tools` i potpuno build stablo bez tih komponenti.

## Otvorena sigurnosna obveza

- Procijeniti AGP/Gradle/Android Lint tranzitivne nadogradnje koje
  rješavaju potvrđene nalaze, bez nasumičnog dependency forcinga
  koji može narušiti build.
- Build-tool High/Critical ostaju **otvoreni** u registru rizika.
  Njihov utjecaj različit je od CVE-a u finalnom Android runtimeu.
- Ponovno skenirati obje granice pri svakoj objavi i usporediti
  zatečene GHSA oznake.
- Snimka SBOM-a nije zamjena za penetracijsko testiranje i
  analizu stvarnog potpisanog APK/AAB artefakta.

Dokumentacija konfiguriranja CycloneDX 3.5.1:
https://github.com/CycloneDX/cyclonedx-gradle-plugin
