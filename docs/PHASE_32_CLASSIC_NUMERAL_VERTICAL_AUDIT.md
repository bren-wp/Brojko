# BROJKO — faza 32: Klasično 4×4, redovi pločica i položaj brojki

**Original:** `Slika ChatGPT 24. lis 2026. 22_15_13-4(20261102-201004).png`. **Stvarni Android emulator:** `golden-state-game-941x1672.png` iz uspješnog [UI QA #41518187649](https://github.com/bren-wp/Brojko/actions/runs/41518187649). Obje slike su 941 × 1672 piksela bez skaliranja i pomaka. Fixture ima jednake brojeve na ploči i rezultatima kao referenca; ne mijenja korisnikov stvarni napredak.

## Izmjerena regresija položaja

U prvom redu, položaj bijelih brojki izmjeren je mascom RGB kanala >238 u unutrašnjosti ćelije, bez obruba i pozadine:

| Broj | Centroid originala (y px) | Centroid emulacije (y px) | Previsoko |
|---|---:|---:|---:|
| 2 | 512,50 | 494,49 | 18,01 px |
| 4 | 512,24 | 495,02 | 17,22 px |
| 8 | 511,07 | 493,21 | 17,86 px |
| 16 | 511,47 | 494,69 | 16,78 px |
| 8 (drugi red) | 736,41 | 715,21 | 21,20 px |

## Lokalni popravak

- U aktivnom **svijetlom Klasično 4×4** prikazu početni redak spušten je za **2 dizajnerske jedinice**, a razmak redaka povećan je za **1 dizajnersku jedinicu**.
- Bazna pozicija bijelih brojki pomaknuta je s **0,62** na **0,68** relativne visine pločice, što približava izvornu visinu brojke.
- Pobjednički / zamrznuti board, opušteni 5×5, Dnevni izazov, tamne teme i stilovi drugih pločica ostali su netaknuti.
- Nema promjene fizike 2048, logike poteza, bodova, datoteka sa spremljenim rezultatima ili hit targeta.
- Dodan instrumentacijski `classicNumeralBaselineMatchesOwnerReference`: iz realnog Canvas prikaza računa centroid bijele brojke 2 i potvrđuje y u rasponu **505–520 px**, bez mijenjanja rezultata. Test snima `classic-source-numeral-baseline-941x1672.png`.

## Završni QA

- [ ] Prolazak build/lint/unit, Android 16 QA i UI emulator instrumentation za posljednji HEAD.
- [ ] Novo mjerenje MAE cijelog Klasično 4×4 i po zasebnim redovima pločica, bez pomicanja PNG-a.
- [ ] Provjera stvarnog centrirovanja brojeva, bijelog highlighta i svih triju gumbi.
- [ ] Nastaviti idući zaslon tek nakon dokaza stabilnog QA-a na posljednjem commit SHA.

**Ne tvrditi 1:1. Ovaj PR je Draft i nije objava aplikacije.**
