# BROJKO — faza 48: 5×5 3D pločice, ikone, kontrole i preostali zasloni

Datum: 7. studenoga 2026. · Brendigo · ciljna grana: `main`

## Izvor i ograničenje provjere

Polazište je aktualni `main` nakon spojenih PR-ova #63 i #64. Za vizualni pregled korištene su **stvarne Android Canvas snimke** iz prethodnog uspješnog GitHub Actions emulator artefakta:
`relaxed-raised-gameplay-941x1672.png`, `relaxed-raised-gameplay-tall-1080x2400.png`, `daily-refined-board-gift-941x1672.png`, `missions-six-real-progress-941x1672.png`, `golden-state-stats-941x1672.png`, `themes-original-final-aligned-941x1672.png`, `settings-phase08-source-on-941x1672.png` i `pause-branded-modal-941x1672.png`.

To su **testni renderi produkcijskog Android BoardScreena**, ponekad s determinističkim primjerima podataka. Ne treba ih predstavljati kao korisničke partije ili dokaz egzaktnog 1:1 podudaranja s potpunim vlasnikovim slikama.

## Opušteno 5×5 — napravljene korekcije

1. Značka `5×5` prije je koristila generički `card()` i niz sitnih ukrasnih točaka. Aktivna svijetla igra sa stilom **Zaobljene** dobila je vlastiti `relaxedGameBadge()`: tamniji donji 3D rub, topli koraljni gradijent, tanak svjetlosni odsjaj, zaobljeni vrh i **šest pravih zaobljenih zraka** (ne sitne točke). Vanjski okvir od 141×50 dizajnerskih jedinica ostaje na istom mjestu.
2. Kontrole **Pauza** i gornji **Poništi** sada upotrebljavaju isti blago izdignuti krem materijal kao aktivni donji gumbi. Zadržane su pozicije i stanje omogućeno/onemogućeno. Na portretu 941×1672 kontrole ostaju desno gore; visoki zaslon i dalje koristi postojeći raspored u zasebnom retku.
3. Donji **Poništi** u onemogućenom stanju koristi istu krem geometriju kao gumbi pored njega, ali je ikona prigušena i bez aktivnog dodirnog područja. Nakon pravog poteza i dalje se aktiviraju oba Undo mjesta.
4. Renderer `relaxedRaisedTile()` za svih 25 polja zadržava postojeće povišene 3D rubove, pastelne prijelaze, refleksiju i stvarne vrijednosti. Nisu mijenjani brojčani podaci, algoritam spajanja, pohrana ili raspored ploče.

## Dodani automatizirani testovi

- `relaxedBadgeRaysAndTwoUndoControlsUseLiveRaisedArtwork`: 941×1672, 1080×2400 i 360×800; provjerava stvarnu koraljnu boju značke i zraka, aktivnu Pauzu, neaktivni Undo na novoj igri i **obje** Undo kontrole nakon stvarnog spajanja.
- `relaxedFourDigitValuesFitAllRaisedTilesWithoutClipping`: na tri portretna zaslona crta stvarne `1024` i `2048` te provjerava da nisu izvan rubova petostupčanog polja. Bodovi i 25 ćelija moraju ostati nepromijenjeni.

## Redom pregledani ostali zasloni i što još treba verificirati

| Zaslon | Stanje pregleda | Sljedeći kriterij |
|---|---|---|
| Dnevni izazov | Postoji stvarni prikaz ciljeva, ploče, nagrade i CTA iz emulatora. Nisu mijenjani u ovoj grani. | Datum i stvarni cilj, početak izazova, ikone pomoći, četveroznamenkaste pločice, clipping na 320×480 i usporedba s vlasnikovim cijelim PNG-om |
| Misije i postignuća | Dvobojni naslov i napredak imaju postojeće izolirane tests; render sa šest zadataka pregledan. | Nagrade moraju odgovarati stvarnom napretku, sve kartice i 3D zvijezde moraju stati bez dodirivanja |
| Statistika | Pregledan pravi Android render s KPI karticama, grafom i poviješću; demonstracijski brojevi nisu produkcijska povijest. | Nulti graf, vrlo veliki brojevi, akcije Bodovi/Pločica, realni detalji povijesti, dark mode |
| Teme i izgled | U pregledanom renderu vidljive su tri teme, pregled ploče, stilovi, vibracija, zvukovi i slider. | Ispravan odabir, reprezentativne ikonice, tri stilova pločica i razmaci na manjim zaslonima |
| Postavke | Pregledan render s lokalnom kopijom, privatošću i About, uz 3D ikone i gornju navigaciju. | Provjeriti dostupnost svih redaka, lokalnu kopiju bez lažne mrežne sinkronizacije, postojanost postavki, realan broj verzije |
| Pauza, kraj i pobjeda | Pregledan render pauze, postoje odvojeni testni prikazi kraja i pobjede. | Onemogućiti dodir ispod modala, potvrde, navigaciju, 3D konture i dostupnost svih tipki |

## Granice dovršetka

Ova faza **ne tvrdi** da je svaki drugi zaslon 1:1. Ne smije se spojiti dok na **posljednjem commit SHA** ne prođu `Android pull request validation`, `Android UI smoke and screenshot QA` i `Android 16 navigation bar and inset QA`. Nakon toga treba provjeriti stvarne nove snimke i eventualne regresije prije pripreme novog izdanja.

**Nema statičnih screenshotova umetnutih u produkcijski UI, lažnih rezultata niti novih runtime ovlasti.**
