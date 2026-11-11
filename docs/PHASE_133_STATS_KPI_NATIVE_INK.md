# Brojko — faza 133: tri gornje KPI kartice Statistike, 941×1672

Datum: 11. studenoga 2026. **Polazni, potvrđeni Android prikaz**: `Brojko-real-device-UI` iz [UI QA 42937807059](https://github.com/bren-wp/Brojko/actions/runs/42937807059), ista source-like KPI kompozicija kao v1.12.211. Izvorna referenca: vlasnikov `17-8` PNG 941×1672. RGB MAE je prosjek apsolutnih razlika kanala, raspon 0–255; nije postotak.

## Prije izmjene

- Gornja tri KPI-ja kao cjelina: **19,4692 RGB MAE** u regiji `x20..923, y305..540` (i dalje značajno različite nijanse pastelnih podloga, font rasterizacija i refleksije).
- Izvučene su stvarne obojene maske **brojki**, bez miješanja ostalog UI-ja i bez OCR-a.

| Brojka | Izvorni obojeni ink (x1..x2/y1..y2) | Android prije | Lokalna korekcija |
|---|---|---|---|
| 12.560 | x69..297 / y442..499 | x55..303 / y442..499 | širina ×.92; pomak +1.7 log. x |
| 248 | x422..553 / y442..499 | x406..548 / y440..499 | širina ×.92; pomak +4.25 log. x |
| 1024 | x694..859 / y444..498 | x669..860 / y440..499 | širina ×.865; pomak +5.1 log. x |

Te su referentne brojke **samo izolirani testni fixture**, ne produkcijski prikaz lažnog rekorda. Logika formata i vrijednosti dolazi iz `best`, `plays` i `recordTile` kao i ranije.

## Tri naslova KPI-ja

Tamne maske dvorednih zaglavlja: izvorni `Najbolji rezultat` x184..277/y353..402, Android x175..282/y361..412; izvorni `Ukupno partija` x492..581/y354..407, Android x486..595/y361..412; izvorni `Najveća pločica` x781..870/y355..409, Android x771..876/y360..412.

Samo ta tri natpisa (light original portrait) dobit će lokalnu horizontalnu skalu ×0.86/0.84/0.86 i podizanje za 3.7 logičkih jedinica, s malim lokalnim X pomacima. Promjene ne zahvaćaju ikone, kartice, vrstu jezika, povijest, grafikon, TalkBack niti interaktivni DOM/Android touch.

## Zaštite i valjanost

- `OwnerStatsKpiPhase133Test` provjerava izvorne granice triju vrijednosti na pravom Android Canvas rendereru na 941×1672, te da render ne mijenja stvarne statistike.
- Na 360×800 i 1080×2400, u svijetloj i tamnoj temi, provjerava da su pravi grafički tabovi dostupni.
- Novi screenshotovi moraju se izvući iz uspješnog **najnovijeg** UI CI artefakta. Bez toga su korekcije hipoteze, ne dokaz o manjem MAE.
- Prije mergea izračunati isti RGB MAE gornjeg ROI-ja, cijelog Stats, i pojedinačnih naslova/brojki. Vratiti zahvat koji pogorša bitan ROI.
- `#131` sadrži i zasebnu fazu 132 Postavke; obje moraju proći build/lint/unit, Android 16 i emulator UI na posljednjem SHA.

**Rezultat 1:1 još nije potvrđen.** Ilustracije koriste native Canvas vektore i stvarne dinamičke vrijednosti, nikada slike zaslona kao lažnu interaktivnu aplikaciju.
