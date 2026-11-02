# BROJKO — faze 35 i 36: Dnevni izazov → Misije i postignuća

Datum: 2. studenoga 2026.

## Izvori i mjerne granice

Originali vlasnika: `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261102-201005).png` (Dnevni izazov) i `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261102-201001).png` (Misije). Početne *stvarne* Android emulator snimke potječu iz uspješnog [UI QA #41525523484](https://github.com/bren-wp/Brojko/actions/runs/41525523484), artefakt `Brojko-real-device-UI` ID `13179933385`. Analiza je na **941 × 1672 px**, bez skaliranja, poravnanja, umetanja statične ilustracije ili mijenjanja stvarnih rezultata.

| Izvorna stranica / ROI | RGB MAE prije ovih zahvata |
|---|---:|
| Dnevni izazov, cijeli zaslon | **16,846** |
| Dnevni izazov, naslov (0,0–941,370) | 22,837 |
| Dnevni izazov, ploča (150,365–790,955) | 13,600 |
| Dnevni izazov, ciljevi (45,950–900,1320) | 18,432 |
| Dnevni izazov, nagrada (35,1320–900,1550) | 19,666 |
| Dnevni izazov, gumb (25,1500–920,1670) | 21,064 |
| Misije, cijeli zaslon | **23,590** |

To su **početna mjerenja**, a ne potvrđeni rezultati novih commitova.

## Faza 35 — Dnevni izazov

- Izvorni svijetli portretni reward panel nema ljubičasti tekst `+250` u desnoj zoni. Prethodna prava emulacija imala je **750** ljubičasto obojenih piksela u ROI `x730..890, y1360..1495`, nasuprot **28** na izvorniku.
- Uklonjen je samo taj dekorativni broj u svijetloj portretnoj reward kartici; `+250` i dalje se prikazuje kod osvajanja u result overlayu, a stvarna logika dodjele nagrade nije promijenjena.
- Naslov `Posebna nagrada` veći je (19,5 umjesto 18 dizajnerskih jedinica) i više se ne komprimira zbog zasebne `+250` oznake.
- Novi `dailySourceRewardCardHasNoExtraPurpleBadge` instrumentacijski test zahtijeva manje od 65 zasićenih ljubičastih piksela na izvornom ROI-ju, testira stvarni gumb `Započni izazov` te snima `daily-reward-no-extra-badge-941x1672.png`.

Originalni mockup prikazuje datum 14. ožujka 2025. i niz od 7 dana. Produkcijski Android prikazuje **stvarni današnji datum i stvarni korisnikov niz**, nikad fiksno kopirane marketinške vrijednosti.

## Faza 36 — Misije i postignuća

- Trake običnih misija na izvorniku dosežu približno `x631 px`, a trake zadataka s višeznamenkastim brojačima završavaju oko `x573 px`; raniji renderer koristio je istu širinu trake 151 dizajnersku jedinicu svugdje.
- U svijetlom portretnom prikazu obične trake sada imaju **164** dizajnerske jedinice, a trake za 10.000 i 50.000 bodova imaju **140**; brojila su zasebno pomaknuta, pa se ne preklapaju s trakom ni sa zvjezdicama.
- Brojevi napretka i postotak obojenog dijela računaju se iz stvarnog stanja, nema prisilnog punjenja 512 misije ili izmišljanja 4/20 postignuća kao na ilustraciji.
- Opis `Ostvari 10.000 bodova ukupno` sada je identičan izvornom tekstu: `Skupi ukupno 10.000 bodova u svim igrama.`, u dva reda.
- `Majstor spajanja`: tekst `Ostvari pločicu 1024.`.
- Novi instrumentacijski test `missionProgressTracksMatchSourceRowLengths` testira stvarne svijetle piksele oko x616 na prvom retku i x590 na retku 10k, čuva početne vrijednosti i snima `missions-source-track-lengths-941x1672.png`.

## Sljedeći koraci — nisu preuranjeno proglašeni gotovima

- [ ] Android PR validation (build/lint/unit), Android 16 insets QA i Android UI smoke/screenshot QA uspješni na **posljednjem HEAD-u**.
- [ ] Novi emulator screenshot 941 × 1672 px te ponovno izračunat **full-screen i ROI RGB MAE** prema istim referencama.
- [ ] Svaku regresiju popraviti ili vratiti. Ne tvrditi 1:1 prije stvarnog mjerenja.
- [ ] Nastaviti **Statistika → Teme i izgled → Postavke → ikone/brending**, redom i s odvojenim testovima.
- [ ] Bez objavljivanja nepotvrđenih APK/AAB izdanja. Pohranu korisničkih partija ne dirati.
