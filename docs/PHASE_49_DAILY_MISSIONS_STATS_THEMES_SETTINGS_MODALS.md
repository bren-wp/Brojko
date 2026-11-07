# BROJKO — faza 49: Dnevni izazov → Misije → Statistika → Teme → Postavke → dijalozi

**Datum:** 7. studenoga 2026.  **Polazište:** `main` poslije spojenog PR-a #65 (v1.12.153).

## Izvor vizualne provjere

Provjereni su postojeći PNG-ovi iz Android instrumentacijskog emulatora, uključujući:
- `daily-refined-board-gift-941x1672.png`
- `missions-six-real-progress-941x1672.png`
- `golden-state-stats-941x1672.png`
- `themes-original-final-aligned-941x1672.png`
- `settings-phase08-source-on-941x1672.png`
- `pause-branded-modal-941x1672.png`
- `game-over-raised-portrait-941x1672.png`

To su stvarni renderi produkcijskog Android Canvas sustava, često s izoliranim demonstracijskim podacima. Nisu dokaz opće podudarnosti 1:1 bez pune originalne vlasnikove slike, identičnog stanja i objektivno izmjerenih ROI razlika.

## Implementirano — zaslon po zaslon

| Zaslon | Konkretna promjena u Java/Canvas kodu | Funkcionalni integritet |
|---|---|---|
| **Dnevni izazov** | Dvije naslovne linije sada koriste isti mjerljivi Nunito Black, blagi reljef i osvijetljeni gornji rub. Zadržani su osnovna linija, font-size ograničenja i postojeće dvije koraljne nijanse. | Današnji datum, korisnički niz, tri stvarna cilja, nagrada i gumbi nisu promijenjeni. |
| **Misije i postignuća** | Tri međunaslova Dnevne / Tjedne / Postignuća dobili su tanki rounded stroke, vrlo blagu sjenu, suptilni navy gradijent i audit granica inka. Dvobojni glavni naslov ostaje iz faze 46. | Bez promjene napretka, zvjezdica, nagrada ili vremena preostalog do reseta. |
| **Statistika** | Veliki naslov dobiva isti vizualni materijal kao Dnevni. Posebno je obrađen kompaktni te standardni naslov. | Grafikoni, povijest, metrika bodova/pločica i detalji partije ostaju povezani sa stvarnim podacima. |
| **Teme i izgled** | Obje boje naslova mjere se zajedno prema stvarnoj širini glifova. Naslov je centriran kao cjelina s konzistentnim optičkim razmakom; više se ne prilagođavaju dvije kutije neovisno. | Tri teme, tri stila pločica, vibracija, zvukovi i tri razine animacije ostaju aktivni i spremljeni lokalno. |
| **Postavke** | Naslov koristi rounded 3D osvjetljenje i mjerene granice. | Sedam kartica, lokalna kopija, jezik, pravila privatnosti i podaci o aplikaciji ne mijenjaju se. |
| **Pauza** | Naslov `Igra je pauzirana` koristi isti osvijetljeni materijal. | Nastavak i Izbornik ostaju jedine funkcionalne akcije modala. |
| **Kraj igre** | Glavni naslov `Kraj igre` dobiva isti materijal u kompaktnom i visokom portretnom prikazu. | Pravi rezultat, najveća pločica i Nova igra / Povratak / Podijeli ostaju očuvani. |
| **Dnevna pobjeda / neuspjeh** | Ujednačen je glavni naslov, s mjerenjem i prilagodbom za dugačak tekst. | Izračun nagrade i zaštita od dvostruke dodjele nisu mijenjani. |
| **Potvrde nove partije i završetka** | Naslovi `Nova partija?` i `Završiti partiju?` dobivaju istu osvijetljenu konturu. | Samo dvije modalne akcije; arhiva ostaje netaknuta dok korisnik ne potvrdi. |
| **Pobjeda / Bravo!** | Izvorni nativni `ownerBravoVectorHeading()` i troakcijska grafika nisu mijenjani jer već imaju zasebnu vlasnikovu referentnu geometriju. | Nastavak, Nova igra i Dijeljenje ostaju funkcionalni. |

## Način implementacije

Jedna pomoćna metoda `illustratedTitle()` mjeri stvarni Nunito font i crta vlastiti tekst u produkcijskom Canvasu — nema dodatnih datoteka, vanjskih mrežnih resursa, ugrađenih screenshotova niti bitmap zamjena. U svijetloj temi koristi neprozirni font, blagu obrubnu liniju, tamniji donji gradient i lokalnu sjenu. U tamnoj temi zadržava ravni čitljivi tekst. Shader, sjena i stil boje vraćaju se nakon svakog natpisa da sljedeći elementi ne naslijede prozirnost.

## Android testovi

- `illustratedHeadingsRemainDistinctOnEveryDetailScreen`: pet glavnih zaslona × dvije teme × 941×1672/360×800, sa stvarnim granicama glifova, nedodirivanjem dvobojnih riječi i aktivnom navigacijom.
- `missionSectionTitleInkKeepsSeparateCardHeadings`: sva tri naslova unutar svoje sekcije, s usklađenim lijevim rubom i razmakom od broja postignuća.
- `allResultAndConfirmationModalTitlesKeepOnlyLiveControls`: sedam modalnih stanja × dva portretna formata, bez nevidljivih klikova s podloge, sa stvarnim nazivima i ispravnim brojem akcija.

Svaka testna slika dobiva prefiks `phase49-`. Stanje igračeve pohrane i stvarni rezultati nisu namjerno mijenjani u produkcijskom kodu; instrumentacija koristi izolirani testni `TileGame` i vraća prijašnje stanje.

## Preostalo prije završetka

- [ ] Gradle/lint/unit, Android UI screenshot QA i Android 16 insets uspješni na **zadnjem SHA**.
- [ ] Preuzeti nove Android PNG datoteke i usporediti sa **punim vlasnikovim izvornicima** na istim veličinama i stanju aplikacije.
- [ ] Ako se pojavi lokalna regresija, popraviti u istoj grani bez zasjenjivanja funkcionalnih gumba.
- [ ] Provjeriti testove TalkBacka, kontrast tamne teme, dugačke rezultate i sve lokalizirane natpise prije tvrdnje „sve 1:1”.
- [ ] Novo izdanje objaviti **tek nakon** prolaska završnih provjera i spajanja u `main`.

**Napomena:** ovo je dokumentirani tehnički i funkcionalni prolaz. UI nije proglašen piksel-po-piksel identičnim bez usporedbe novih emulator rendera i potpunih vlasnikovih originala.
