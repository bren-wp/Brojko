# Brojko — dodatna faza 13: Game Over (30. listopada 2026.)

## Izvor i ograničenje vizualne podudarnosti

Korisnik je dostavio deset osnovnih referentnih prikaza, uključujući `Bravo!` (pobjedu), ali **nije dostavio zaseban Game Over PNG**. Game Over je zato izveden iz postojećeg brendinga Brojko: tamnoplavi Nunito naslovi, koraljna glavna akcija, tople krem kartice, tirkizna najveća pločica, meke sjene i 3D reljef. Ne postoji objektivan izvorni RGB MAE za taj modal; **1:1 podudarnost nije potvrđena**. Nove ilustracije nisu generirane u chatu.

## Identificirani problem

Prije ove faze `over` je u referentnom portretu koristio isti generički `resultOverlay` kao non-portrait rezultat. Izračun visine sadržaja na portretnom 941×1672 ostajao je `880` umjesto stvarnog kraja modala, stvarajući oko 169 nepotrebnih logičkih jedinica pomicanja u odnosu na vidljivi raspon `1672 / (941/400) ≈ 711`. Isto nasljeđeno skaliranje nije pružalo poseban 3D brand sustav za poraz.

## Implementirano

1. `compactGameOverCard()` unutar `MainActivity.BoardScreen` — **samo** portretni Game Over, bez izmjene pobjede `Bravo!`, 2048 enginea ili drugih modalnih ekrana.
2. Zamračena originalna stvarna ploča ostaje u pozadini; sve pozadinske dodirne akcije brišu se prije crtanja modala.
3. Glavna kartica `(42,168,316,525)`, reljefna koraljna ikona cilja, naslov `Kraj igre`, objašnjenje `Nema više mogućih poteza.`. Dvije odvojene statusne pločice koriste **stvarni** `game.score` i `game.maxTile()`, ne statičke ilustracijske brojeve.
4. Akcije: `Nova igra` na y=483, `Povratak` na y=559 i `Podijeli rezultat` na y=624. Sve su stvarno dodirljive, odvojene i barem 48 logičkih jedinica visoke.
5. Za portretno stanje `over` koristi se `contentHeight=705` kako na 941×1672 nema fantomskog pomicanja, dok kratki 360×600 zadržava **potrebno** pomicanje radi dostupnosti donje akcije. 1080×2400 nastavlja koristiti postojeći visoki layout.
6. `Nova igra` poziva postojeći `newGame(game.mode)` i time najprije **lokalno arhivira** završenu partiju. `Povratak` vodi na Home bez gubitka završene sesije, a `Podijeli rezultat` koristi stvarni rezultat.

## Regresijska kontrola

- `gameOverPortraitKeepsRealScoreAndAllThreeActionsReachable`: tri rezolucije (941×1672, 360×600, 1080×2400), točno tri vidljive modalne akcije, bez interaktivnih pozadinskih gumba, bez preklapanja, dostupne donje akcije i bez nepotrebnog scrolla na standardnim portretima; test promjene stvarnog rezultata usporedbom boja 100×100 ROI oko brojki.
- `gameOverNewGameArchivesExactlyOnceAndBackKeepsSession`: povratak na Home ne dira rezultat ili history; potvrđena nova igra lokalno arhivira završenu igru s ispravnim `score=6144`, `moves=34`, `peak=512`, `mode=classic`. Test uvažava arhivu ograničenu na posljednjih 20 partija.
- Emulator screenshot: `game-over-raised-portrait-941x1672.png` u `Brojko-real-device-UI`. `scripts/verify-emulator-screenshots.py` zahtijeva tu stvarnu PNG datoteku dimenzije 941×1672, bez resamplinga.

## Prije prihvaćanja

- Pokrenuti i zaključiti sva tri GitHub Actions workflowa **na zadnjem HEAD-u**: Gradle build/lint/unit, Android 16 insets i emulator UI test/screenshot.
- Ako QA padne, pregledati izvorni log i korigirati pravi uzrok, bez proglašavanja prolaza.
- Pregledati stvarnu novu emulator snimku, uključujući tamnu/živopisnu temu, kontraste, izgled na malim zaslonima i TalkBack.
- Nema vlasnikova posebnog Game Over PNG-a: usporedba 1:1 nije dokaziva i ne smije se izmišljati.
- Zadržati PR u stanju **Draft**; bez mergea ili izdanja dok finalni QA ne prođe.

**Status:** ciljane izmjene koda, testovi i obvezna snimka spremljeni u postojećem PR-u. CI potvrda i finalni vizualni audit tek slijede.

**Sljedeće:** dnevni završni prikazi `dailyWin` i `dailyFail`, pa puni regresijski i security/performance audit.
