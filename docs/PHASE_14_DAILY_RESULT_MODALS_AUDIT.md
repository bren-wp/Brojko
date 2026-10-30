# Brojko — faza 14: dnevni izazov (pobjeda i neuspjeh)

Datum: 30. listopada 2026.  
PR: [bren-wp/Brojko#28](https://github.com/bren-wp/Brojko/pull/28), grana `feat/pixel-victory-modal-phase4`.

## Vizualni izvori i ograničenja

Među deset vlasnikovih PNG referenci nalazi se zaslon **Dnevni izazov** prije početka igranja, ali nema zasebnih PNG-ova `dailyWin` i `dailyFail`. Za ove dvije dodatne modalne situacije upotrijebljen je postojeći usvojeni Brojko stil: Nunito, toplokrem zaobljene kartice, koraljni CTA, tirkiz za uspjeh, lokalne reljefne vektorske ikone i blage sjene.

**Ovo nije verifikacija pixel-perfect 1:1.** Ne postoji usporediv izvorni PNG 941×1672 za izračun RGB MAE, pa se takva vrijednost neće izmišljati. Također se ne generiraju slike u chatu.

## Otkriveno u postojećoj implementaciji

- Oba završna prikaza koristila su jedan generički modal s rezultatom u jednoj kartici, bez odvojene najveće pločice i bez posebno izrađenih ikona.
- Na portretnom omjeru `contentHeight` je ostajao 880 (previše za posljednji gumb na y=676), pa se na 941×1672 dobivalo nepotrebno pomicanje, a na malim uređajima pozicija gumba ovisila je o ručnom skrolanju.
- Poruka uspjeha oslanjala se na prolaznu memorijsku zastavicu `dailyBonusGrantedThisAttempt`. U novom prikazu stanje preuzete nagrade ima i eksplicitnu provjeru **trajnog** `dailyRewardDate`, pa se ne prikazuje neistina nakon ponovnog prikaza zaslona.

## Implementirano

1. Novi `compactDailyResultOverlay(boolean success)`, odvojen od visokog postojećeg rasporeda na uređajima s dugim omjerom ekrana.
2. Kartica modala na `(27,177,346,511)`, brendirana reljefna ikona **star** ili **target**, stanja:
   - `Dnevni izazov riješen!` — svi ciljevi dovršeni, tirkizna nagrada;
   - `Izazov nije uspio` — ograničenje od 80 poteza ili iscrpljeni mogući potezi, koraljni naglasak.
3. Odvojene tople pločice **Tvoj rezultat** i **Najveća pločica**, s isključivo stvarnim `game.score` i `game.maxTile()`. Testni brojevi ne postoje u produkcijskom rendereru.
4. Posebna kartica nagrade ili objašnjenja (y=476..530). Prva pobjeda može prikazati `+250 zvjezdica osvojeno!`; ponovno otvaranje provjerava `dailyRewardDate`, a ne lažira novu dodjelu.
5. Funkcionalni gumbi `Igraj ponovno` (y=543..604) i `Izbornik` (y=615..670) ostaju odvojeni. Ponovni pokušaj poziva postojeći `newGame("daily")`, koji prethodnu odigranu partiju arhivira prije otvaranja sljedeće.
6. Portretni `contentHeight=700`: na izvornom **941×1672** nema dodatnog skrolanja; na kratkom **360×600** dopušta se potrebni pomak za donju akciju.
7. Visoki postojeći modal nije općenito resetiran; njegova poruka nakon ponovnog prikaza nagrade također provjerava `dailyRewardDate`.

## Automatizirana provjera

**`dailyWinAndFailPortraitUseLiveDataWithSafeActions`**
- Faze `dailyWin` i `dailyFail`, realistični kontrolirani primjerci aktivne ili izgubljene partije, 941×1672, 360×600, 1080×2400.
- Točno dvije modalne akcije, bez podložnih interaktivnih gumba, minimalne touch zone 48, bez preklapanja, vidljivost nakon potrebnog scrolla.
- Render rezultata mora se promijeniti kada se promijeni stvarni `game.score`; renderer sam ne smije povećavati poteze ni dodjeljivati nagrade.
- Dvije **stvarne** Android snimke: `daily-win-raised-portrait-941x1672.png` i `daily-fail-raised-portrait-941x1672.png`.

**`dailyResultButtonsPreserveRewardDateAndArchiveRealScore`**
- `Izbornik` vraća na Home bez brisanja dovršene sesije ili pisanja u povijest.
- `Igraj ponovno` pokreće novu dnevnu partiju, a prethodnu sprema s rezultatom i brojem poteza.
- `dailyRewardDate` mora ostati na već nagrađenom datumu, a `dailyBonusGrantedThisAttempt` mora biti resetirana za novi pokušaj.
- Test po završetku vraća spremljenu povijest i privremeno modificirane korisničke brojače/nagrade.

Postojeći `dailyChallengeAwardsOnceAndEnforcesMoveLimit` nastavlja testirati jedinstvenu nagradu od 250 zvjezdica i automatski neuspjeh na 80. potezu.

`scripts/verify-emulator-screenshots.py` sada zahtijeva obje nove snimke dimenzija **941×1672**.

## Gates prije spajanja

- Sva tri GitHub Actions workflowa na **najnovijem** commitu moraju završiti zeleno: Gradle build/lint/unit, Android 16 edge-to-edge i Android emulator UI/screenshot QA.
- Iz `Brojko-real-device-UI` preuzeti stvarne modalne snimke i provjeriti tipografiju, geometriju, boje, kontrast, male uređaje i TalkBack. Bez lažne tvrdnje o RGB MAE/1:1 kad nema izvornog PNG-a.
- Pokrenuti regresijske testove spajanja pločica, arhiviranja, ponovljenih dnevnih nagrada, oba ishoda, lokalnog spremanja i 21 postojeće rute.
- PR ostaje **Draft**, bez mergea i bez novog izdanja prije provjere.

**Status:** implementacija, instrumentacijski testovi i screenshot zahtjevi su commitani. Zadnji CI rezultat, stvarna snimka i konačna UI podudarnost **nisu još potvrđeni**.

**Sljedeća faza:** završni sistematski pregled CI pada, svi modalni i glavni zasloni, vizualne regresije, sigurnost, dead-code i release-preflight.
