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

## PR #160 — evidence and remediation status

The final PR #159 runtime SCA reported 0 matches across 38 packages; build-tool High/Critical findings remained. The previously measured paths above identify Android Lint/SDK configurations, not merely the root plugin classpath. PR #160 archives buildEnvironment inventories alongside runtime inventories and publishes both SARIF files with release assets. Exact current transitive paths and patched version compatibility still require a fresh resolved graph and scans; no advisory is suppressed or marked fixed.

## Scoped remediation candidates (PR #160)

Previously measured Lint dependency paths, from the recorded CycloneDX graph, are listed above. The following candidate updates are limited to exact previously observed requests in Lint-named configurations, with no global force or app runtime upgrade:

- BC provider / PKIX / utility: aligned to 1.85 (CVE-2026-8763, CVE-2026-13506, CVE-2026-5588).
- Guava JRE: 33.7.2-jre (CVE-2026-102554).
- HttpClient / HttpMime: aligned to 4.5.14 (CVE-2020-13956).
- Commons Lang: 3.18.0 (CVE-2025-48924).

The referenced patched versions were checked against the GitHub advisory database and Maven Central. Full app configuration inventory is also archived because buildEnvironment alone does not cover the Lint worker classpath. This is an unverified remediation candidate until Gradle resolution, lint/build tests, both SBOMs and both scans confirm the result. Any residual finding stays open; no suppression or SBOM exclusion is added. See https://github.com/advisories/GHSA-9pwp-9qqc-pr26, https://github.com/advisories/GHSA-qp49-qgx5-5m26, https://github.com/advisories/GHSA-wg6q-6289-32hp, https://github.com/advisories/GHSA-xxph-c9ww-hj94, https://github.com/advisories/GHSA-7r82-7xv7-xcpj and https://github.com/advisories/GHSA-j288-q9x7-2f5v.

## Independent verification of PR #160 head 579b9b2

Run 45507848719 artifact 14974247877 contains 37 runtime and 137 app/build-configuration components (excluding BOM root) and zero findings in both SARIF files; all six previous advisories are absent. However artifact 14974092479 independently confirms root `buildEnvironment` still selects BC 1.80.2, Commons Lang 3.16.0 and Guava 33.7.1-jre. The app Lint upgrade alone therefore did not cover the plugin classpath. The next commit adds exact-version root classpath remediation, a resolved Gradle module graph export, and a build-only CycloneDX merge with validation of graph edges and known old coordinates. The high/critical build-tool SCA now blocks CI and release. The fresh combined graph/scan is pending, not presumed clean.

## Expanded build SCA, run 45510192667 (head 4be843a)

Original artifact 14974418289 contains runtime and expanded build SBOMs, resolved plugin classpath and both SARIF reports. Runtime: 0 findings. Build: 6 High + 1 Medium; previous six advisories absent. Shortest paths computed directly from the resolved plugin graph:

| Module | Resolved | Advisory | Remediation candidate |
|---|---|---|---|
| JDOM2 via AGP -> jetifier-processor | 2.0.6 | GHSA-2363-cqg2-863c | 2.0.6.1 |
| jose4j via AGP -> bundletool 1.18.3 | 0.9.5 | GHSA-3677-xxcr-wjqv | 0.9.7 (fixed since 0.9.6) |
| Jackson Core via CycloneDX plugin -> core-java 13.2.0 -> jackson-dataformat-xml | 2.22.2 | GHSA-p6pp-m3f8-5c89 / GHSA-7hhh-6rmp-j9qf | 2.22.4 (fixed since 2.22.3) |
| Jackson Databind via same CycloneDX path | 2.22.2 | GHSA-cxp5-3px4-pw24 / GHSA-wv8q-qhhj-9h54 | 2.22.4 (fixed since 2.22.3) |
| Kotlin Gradle Plugin directly via AGP 9.5.2 | 2.2.10 | GHSA-r937-wjx7-w2jp | OPEN: fixed since 2.4.20-Beta1; compatible AGP/Gradle upgrade needs testing |

Advisory details: https://github.com/advisories/GHSA-2363-cqg2-863c, https://github.com/advisories/GHSA-3677-xxcr-wjqv, https://github.com/advisories/GHSA-p6pp-m3f8-5c89, https://github.com/advisories/GHSA-7hhh-6rmp-j9qf, https://github.com/advisories/GHSA-cxp5-3px4-pw24, https://github.com/advisories/GHSA-wv8q-qhhj-9h54, https://github.com/advisories/GHSA-r937-wjx7-w2jp. Scoped exact-version plugin-classpath overrides align Jackson BOM, core, databind and XML/YAML modules without changing jackson-annotations 2.22. Runtime configurations unchanged. Kotlin 2.4.20 documents Gradle support only through 9.7.0; current wrapper is 9.8.2, so no blind Kotlin force is used. Explicitly disable Gradle task-output caching as defense in depth; this does not close the advisory. All overrides require fresh resolution and scans; no SCA suppression. Current High/Critical build SCA is blocking.
