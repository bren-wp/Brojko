# Brojko — faza 45: Gameplay, Klasično 4×4 i Opušteno 5×5

**Datum:** 5. studenoga 2026. · **Referentni viewport:** 941 × 1672 px · Brendigo

## Odabir zaslona

Prioritet je **aktivna Klasična igra 4×4**, zatim **aktivna Opuštena igra 5×5**. Prethodni izravni audit vlasnikove 4×4 slike (datoteka `Slika ChatGPT 24. lis 2026. 22_15_13-4(...).png`) i stvarnog Android emulatora potvrdio je da je **naslov i tri koraljne zrake** područje s najvećom razlikom na toj stranici. Opušteni 5×5 nema zaseban novodostavljeni owner PNG koji se može vjerodostojno usporediti 1:1; ovdje se stil konzistentno usklađuje s Klasičnom igrom, ne predstavlja kao egzaktna reprodukcija nepostojeće 5×5 reference.

### Povijesno potvrđene 4×4 RGB MAE metrike (nije aktualni release)

| Regija | Povijesni MAE (0–255) |
|---|---:|
| Cijeli 941×1672 | **16.925** |
| Naslov/zrake x30..500 y70..350 | **36.48** |
| Ploča/okvir x20..920 y350..1320 | **14.52** |
| Tri kontrole x35..925 y1330..1605 | **23.64** |

Izvor su ranije dokumentirani artefakti u `docs/PHASE_34_CLASSIC_HEADING_PIXEL_AUDIT.md`. Nove vrijednosti nakon ovoga PR-a **još nisu izmjerene** na istom izvornom PNG-u. Različiti `game.score`, ćelije ili animacijska faza mogu lažno povećati MAE.

## Implementirane lokalne korekcije

### Klasično 4×4
- Nativni `classicGameplayHeading()` crta naslov „Klasično” dodatno zaobljenom `FILL_AND_STROKE` tipografijom, tamnoplavim gradijentom i debljom izvornoj silueti prilagođenom konturom. Ne koristi statičnu sliku.
- Poziva se **samo za aktivnu, svijetlu, portretnu Klasičnu igru**, nikad na tamnoj temi niti ispod modala. Značka 4×4, score HUD i izvorna zračna dekoracija ostaju u istim položajima.
- Pločice 4×4, njihova izvorno prilagođena visina brojeva, gameplay logika, boje, gumbi i pravila 2048 ostaju netaknuti.

### Opušteno 5×5
- Novi **`relaxedRaisedTile()`** crta 25 manjih pločica s usklađenom dubinom, mekim odsjajem i širokim toplim gradijentom, bez promjene boja izvornog brendinga.
- Četveroznamenkasti brojevi zadržavaju dobru čitljivost: prilagođena je lokalna veličina fonta, a polazna visina teksta je `0.66 × tileHeight` umjesto generic `0.62`, bez prepisivanja vrijednosti ćelija.
- Kartice `Novo` i `Poništi` dobile su isti povišeni krem materijal kao Klasično, ali samo **dok je stvarno otvoren Gameplay** u svijetloj portretnoj temi s `Zaobljene` stilom. Tamna, živopisna, Ravna/Staklena tema, 4×4, Dnevni izazov, Pause, Bravo i Game Over ne mijenjaju se.
- Nema mrežnih poziva, novih runtime ovisnosti, posebnih dopuštenja ili spremanja demo-rezultata.

## Funkcionalni ugovor

- Klasično ostaje 16 polja i deterministički algoritam 2048; Opušteno ostaje 25 polja.
- Stvarni `MoveResult`, rezultat, najveća pločica, povijest, mogućnost Undo, swipe senzori, spremanje i ponovno pokretanje ne smiju biti promijenjeni.
- `Novo` uvijek koristi postojeću potvrdu nove igre; `Poništi` poziva stvarni Undo samo ako postoji povratni potez; `Završi` poziva postojeći modal `confirmEnd`.
- Sve četiri akcije `Pauza`, `Novo`, `Poništi` i `Završi` moraju ostati dostupne, a 5. red ne smije biti sakriven ispod kontrola.

## Novi stvarni emulator QA

Instrumentacijski test `relaxedFiveByFiveHasBrandedRaisedTilesAndRealActions` izvodi isti **941×1672 nativni Canvas** sa 25 stvarnih ćelija, provjerava obojene pločice, donji razmak, činjenicu da crtanje ne mijenja ni ćelije ni bodove, te četiri dodirne akcije; snima `relaxed-raised-gameplay-941x1672.png`.

`scripts/verify-emulator-screenshots.py` zahtijeva ovu novu stvarnu snimku. Nakon uspješne objave skripta `scripts/stage-marketing-screenshots.py` koristi baš tu snimku kao `brojko-relaxed.png` u GitHub Release galeriji. README zadržava korisnički, marketinški sadržaj bez testnih objašnjenja.

## Uvjeti dovršetka

1. Na zadnjem PR head SHA-u moraju proći **Android pull request validation**, **Android UI smoke and screenshot QA**, te Android 16/insets kad je workflow potreban prema promijenjenim putanjama.
2. Snimiti i usporediti isti stvarni fixture 4×4 s vlasnikovim originalom; izračunati puni RGB MAE i ROI naslova. Ako je naslov pogoršan, vratiti namjensku konturu prije mergea.
3. Potvrditi 5×5 na 941×1672, 360×600, 320×480 i u landscapeu, s uključenim Undo te s dugim vrijednostima 1024/2048.
4. Bez umetanja statičkih referentnih screenshotova u produkcijski UI, bez lažiranja rezultata i bez tvrdnje o potpunom 1:1 bez usporedivog izvornog PNG-a.
5. Po potvrdi zelenog CI-a provjeriti da novo izdanje sadrži APK, AAB, SHA256SUMS i novu stvarnu sliku Opuštenog načina.
