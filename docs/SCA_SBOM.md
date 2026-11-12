# BROJKO — Software Bill of Materials i CVE/SCA kontrole

## Izvor informacija

CycloneDX Gradle plugin `org.cyclonedx.bom:3.5.1` generira popis iz
**razriješenih** direktnih i tranzitivnih ovisnosti, a ne iz ručno
održavanog popisa. Izvršava se zadatkom `./gradlew cyclonedxBom`;
agregirani JSON nalazi se u `build/reports/cyclonedx/bom.json`.

Za svako novo izdanje release workflow kopira dokument u
`SBOM.cdx.json` te izvršava `anchore/scan-action@v7` na tom SBOM-u.
To je stvarno skeniranje Grype baze poznatih ranjivosti, s
`fail-build: true` za **high/critical** nalaze. Izvještaj SARIF
(`grype.sarif`) i izvorni SBOM spremaju se kao workflow artefakti;
uspješan release uključuje `SBOM.cdx.json`.

## Granice i obvezne ručne provjere

- "Nema prijavljenih CVE-a" ne znači odsutnost sigurnosnih propusta.
- Usporeno osvježavanje baze ranjivosti, PURL metapodaci i CPE heuristika
  mogu rezultirati lažno negativnim/pozitivnim nalazima.
- Pozitivne nalaze treba procijeniti prema stvarnom Android putu izvršavanja,
  pogođenoj verziji, popravljivoj verziji i eksploatabilnosti.
- Izdvojeno provjeravati Gradle/AGP, Actions i signing supply chain;
  njihova implementacijska okolina nije nužno runtime ovisnost aplikacije.
- Ne zatvarati sigurnosni audit bez stvarnog CI rezultata i pregledanih
  izvještaja, posebno za verziju koja se objavljuje.

Reference: https://github.com/CycloneDX/cyclonedx-gradle-plugin ,
https://github.com/anchore/scan-action , https://mas.owasp.org/.
