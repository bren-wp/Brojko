# BROJKO — faza 50: mjerena odstupanja, najveći prioriteti

**Datum:** 7. studenoga 2026. · **Polazište:** spojeni PR #66 i izdanje `v1.12.154` na `main`.

## Provjereni izvori (vlasnikovi originali, ne zamjenske slike)

Uspoređeni su korisnikovi izvorni PNG-ovi `Slika ChatGPT 24. lis 2026. 22_15_16-7` (Misije), `...22_15_18-9` (Teme) i `...22_15_17-8` (Statistika), svaki **941×1672**. Izvor se čuva izvan javnog repozitorija. Android emulator snimke preuzete su iz **stvarnog GitHub Actions** `Brojko-real-device-UI` ZIP-a, artefakt `13551772551`, run [42317908821](https://github.com/bren-wp/Brojko/actions/runs/42317908821) (PR #66). Niti jedan render ne zamjenjuje izvorni PNG.

Usporedba je izvedena piksel po piksel u stvarnim dimenzijama, **bez resizea, pomicanja i poravnavanja**, uz apsolutnu RGB razliku i srednju vrijednost triju kanala (MAE, 0–255). Slikovni podaci mogu biti različiti jer produkcija koristi stvarni korisnički napredak, a instrumentacija kontrolirano stanje; razlika u sadržaju **nije automatski UI pogreška**.

| Zaslon / regija | Izvorna emulatorska snimka | RGB MAE prije faze 50 |
|---|---|---:|
| Misije — sve | `phase49-missions-941x1672.png` | **23,130** |
| Misije — logo/naslov y=0..270 | isto | **28,432** |
| Misije — dnevne kartice y=270..746 | isto | 19,765 |
| Misije — tjedne kartice y=746..1243 | isto | 24,043 |
| Misije — postignuća y=1243..1672 | isto | 22,471 |
| Teme — sve | `phase49-themes-941x1672.png` | **22,394** |
| Teme — logo/naslov y=0..337 | isto | 22,005 |
| Teme — odabir i pločice y=337..802 | isto | **28,263** |
| Teme — pregled/opcije y=802..1475 | isto | 21,660 |
| Teme — donja navigacija y=1475..1672 | isto | 11,710 |
| Statistika — sve | `golden-state-stats-941x1672.png` | **20,116** |
| Statistika — logo/naslov y=0..330 | isto | 24,553 |
| Statistika — KPI y=330..755 | isto | 23,002 |
| Statistika — graf y=755..1220 | isto | 21,051 |
| Statistika — povijest y=1220..1672 | isto | 13,203 |

## Identificirane uzročne razlike i ispravke

1. **Mali logotip Br2jko na Misijama/Temama/Statistici:** prije se iscrtavao generičkim Nunito slovima, iako izvor ima posebno nepravilne, snažno zaobljene 3D navy znakove. U ROI `(290,0,680,180)` izvorni navy maskirani dio Misija ima **11.382 px**, a stari Android samo **7.979 px**. Nova `ownerMiniBrand()` koristi **postojeće vlasnikove vektorske konture** iz Homea, njihovu reljefnu koraljnu brojku 2, zrake i stvarnu oznaku Brendigo u lokalnom Canvas transformu. Za Misije je izabrana uža, malo niža geometrija od ostalih dvaju zaslona. Ostale teme zadržavaju svoj fontni fallback.
2. **Statistika — 3D KPI ikone:** `polishedIcon("gamepad")` postavlja prozirnost `0x35` za donju sjenu i ne vraća `Paint` na punu neprozirnost prije gradienta — kontroler je izgledao blijedosivo. Slična je greška u `grid` predlošku (alfa `0x26`). Originalni ROI navy kontrolera ima **3.583 px**, prethodni Android **524 px**; narančasta mreža original **3.903 px**, stari Android **0 px**. Obje gradijentne površine sada imaju neprozirnu boju prije `setShader()`. Kruna KPI-a u svijetlom portretnom statističkom načinu ima toplu narančastu, bez nepotrebnih žutih točaka.
3. **Misije — sunce u sekciji Dnevne:** original ima osam debelih zaobljenih sunčevih zraka oko osvijetljenoga središta. Stari renderer bio je generički niz sitnih točkica. Dodan je `missionSectionSun()` isključivo za svijetli portret, bez diranja nagrada ili kartica.
4. **Teme — položaj pločica:** kod x=110, izvorni red koraljne pločice započinje oko **y=453 px**, stari Android oko **y=441 px**. Drugi red izvora počinje oko **y=549 px**, stari Android oko **y=537 px**. Sve tri kompaktne 2×2 ilustracije pomaknute su **+5 logičkih jedinica ≈ 11,76 px** prema dolje, s pripadajućim brojkama. Indikatori odabira i natpisi slijede isti odmak; korisničke dodirne površine ostaju iste. Natpisi su optički suženi samo po x-osi (0,84), bez promjene vidljive visine izvornog Nunito teksta.

## QA i stvarni kriterij završetka

Dodani instrumentacijski testovi:
- `sourceNavyLogoUsesNativeContoursOnThreeWorstScreens` — logo na sva tri najproblematičnija zaslona.
- `statisticsKpiIconsAreOpaqueLikeOwnerSource` — nezavisne maskirane ROI provjere kontrolera, mreže i krune.
- `missionsDailySunHasEightSolidGoldenRays` — stvarne zlatne zrake i aktivna navigacija.
- `themesPortraitSwatchesMatchSourceRowOffsets` — koraljni donji rub prvoga reda, plavi početak drugoga, natpis, tri aktivne teme.

**Novi RGB MAE još nije poznat.** Za validaciju treba uspješno dovršiti Android PR validation, Android 16 navigation QA i UI/screenshot QA na zadnjem SHA, otvoriti novi emulator artefakt te ponovno izmjeriti potpuno iste regije. Ako se MAE povećao ili je vizualno lošije, dorađivati ili vratiti konkretan zahvat, ne mijenjati stvarni napredak kako bi fotografija ispala ljepše.

## Dalje (svi ostali ekrani)

Nakon provjere tih triju najvećih izvora ostaju Dnevni izazov, Klasično 4×4, Opušteno 5×5, Splash, Home, Postavke i svi modalni rezultati/potvrde, s lokalnim ROI metrikama, upravljanjem živim podacima, svijetlom/tamnom temom i interaktivnim kontrolama. **CI prolaz nije jednak 1:1.** Bez odgovarajućih izvornika nije dopušteno proglasiti svaki ekran potpuno identičnim.
