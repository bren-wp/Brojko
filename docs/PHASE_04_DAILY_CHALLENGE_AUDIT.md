# BROJKO — 4/10: Dnevni izazov

**Otvoreni PR:** #28 (grana `feat/pixel-victory-modal-phase4`). **Original:** `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261030-210527).png` (941×1672). **Stvarni baseline:** `reference-941x1672-daily.png` iz uspješnog Android UI QA workflowa [#41130372871](https://github.com/bren-wp/Brojko/actions/runs/41130372871) na commitu `31ab840`. Usporedba se izvodi na nativnim RGB pikselima, bez resamplinga, translacije ili naknadne korekcije.

## Početno stanje (RGB mean absolute error, niže je bolje)

| Regija (x0,y0,x1,y1 u izvornim px) | MAE |
|---|---:|
| Cijeli zaslon (0,0,941,1672) | 19.052 |
| Naslov/streak/datum (0,0,941,375) | 24.408 |
| Ploča 4×4 i dekoracije (150,365,790,955) | 16.910 |
| Tri cilja (45,955,895,1325) | 19.247 |
| Današnja nagrada (35,1320,900,1550) | 25.388 |
| CTA (25,1500,920,1670) | 23.001 |

Ovi brojevi **ne znače pixel-perfect 1:1**. Usporedba sadrži i stvarnu razliku dinamičkih vrijednosti: originalna slika prikazuje datum **14. ožujka 2025.** i streak **7**, dok živa aplikacija prikazuje lokalni **današnji datum** i stvarni korisnički niz. Nikada ne upisivati ili prikazivati tuđe testne rezultate u produkciji radi boljeg MAE-a.

## Implementacija na aktivnom PR-u

- Uža krem datumska kapsula, točno u prostoru izvorne ilustracije, s inteligentnim sažimanjem duljeg stvarnog datuma bez diranja korisničkog dana.
- Topliji svijetli panel iza niza dana i tople pastelne površine iza ilustracije, ciljeva i nagrade.
- Četiri puta četiri obojene pločice s prigušenim obrubom, vektor-gradijentom i mekšom sjenom; samo ilustrativni preview na ovoj stranici, ne mijenja gameplay ni druga mjesta u aplikaciji.
- Razdvojene narančaste/žute zrake lijevo i desno od ploče, bez zahvata preko kartice ili stvarnih tipki.
- Veće plave, dvosmjerne strelice za treći cilj, dok tri funkcionalna gumba `Pomoć za cilj` ostaju s pravim Toast objašnjenjem.
- Poklon-kutija od lokalne Canvas vektorske grafike, s volumenom, ljubičastim bočnim plohama, zlatnom vrpcom, lukovima i ukrasnim zrakama. Bez slanja korisničkih podataka ili korištenja vanjskih grafičkih servisa.
- Koraljni CTA ima glađi dvostruki reljef i zaobljeni bijeli `play` simbol. Ostala tri načina igre i drugi CTA gumbi ostaju nepromijenjeni.

## Funkcionalni i pristupačni uvjeti

- Start/nastavak/ponavljanje poziva postojeće callbacke i `requestNew("daily")`, odnosno `navigate("game")`.
- Niz dana, datum, završetak, broj poteza, uvjet `256 + 3000 bodova + max 80 poteza` i jednokratna nagrada izračunavaju se kao i ranije; nijedna vrijednost nije proizvoljno zamijenjena demo-brojkom.
- Screenshot test `dailySourceStreakFixtureKeepsRealActionsAndRaisedTiles` privremeno postavlja `streak=7` **samo u memoriji testne aktivnosti**; mora vratiti ranije stanje bez poziva `persist()` i zapisati `daily-source-streak7-941x1672.png`.
- Instrumentacijski QA treba provjeriti boje prikazanog koraljnog `2`, ljubičaste poklon-kutije, tri nepreklapajuća `Pomoć` dodirna polja i jedan pravi CTA.
- Zadržati Android 16 edge-to-edge te 360×600 i 1080×2400 prikaze. Potvrditi da su svi elementi dohvatljivi, a tekstovi ne prelaze granice kartica.

## Obavezna provjera nakon izrade

1. Svi Gradle, lint i Android unit testovi moraju završiti **success**.
2. Emulator instrumentation QA i Android 16 inset QA moraju završiti **success** za isti završni commit.
3. Preuzeti pravi PNG `reference-941x1672-daily.png` i `daily-source-streak7-941x1672.png`; ponoviti ROI MAE te vizualno provjeriti razmake, tekst, ikonografiju i sjene.
4. Ako su pojedine regije regresirale, ispraviti ili vratiti zahvat samo u toj regiji, umjesto da se skriva razlika proizvoljnim podacima.
5. PR #28 ostaje **Draft** bez spajanja i izdanja dok nije potvrđena završna usporedba.

**Sljedeći zaslon po redu: 5/10 — Misije i postignuća.**
