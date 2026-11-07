# BROJKO — Faza 51: Dnevni izazov → Klasično 4×4 → Postavke → ostali ekrani

**Datum:** 7. studenoga 2026. · **Polazište:** `main` na `v1.12.155`, commit `d869107`.

## Izvori i početna mjerenja

Uspoređeni su vlasnikovi originalni portretni PNG-ovi iz listopada 2026. i prethodno prikupljene snimke **stvarnog Android Canvas renderera** iz Android UI testova (run #42317908821; 941×1672). Originali su privatni i **nisu** dodani u GitHub repozitorij.

Udaljenost je apsolutni srednji RGB kanalni MAE, 0–255; niža je bolja. Prije aktualne izmjene:

| Zaslon | Testni Android ekran | Početni MAE | Primjedba |
|---|---|---:|---|
| Dnevni izazov | `phase49-daily-941x1672.png` | 16,69 | Datum, niz dana i korisnikov napredak **nisu** izmišljeni da odgovaraju umjetničkoj referenci |
| Klasično 4×4 | `classic-source-lighting-941x1672.png` | 16,81 | Test s istih 16 pločica i bodovima 3.864 / 1.316 kao na izvorniku; nasumična nova partija **ne može** služiti za MAE |
| Postavke | `settings-phase08-source-on-941x1672.png` | 15,30 | Referenca prikazuje nepostojeću mrežnu sinkronizaciju i v1.0.0; produkcija ispravno prikazuje **Lokalna kopija** i stvaran broj verzije |
| Home | `owner-home-phase44-12560-3864-941x1672.png` | 14,92 | Deterministički testni rezultati; na uređaju su stvarne korisnikove vrijednosti |
| Pobjeda | `victory-source-modal-reference-941x1672.png` | 16,45 | Testni izvorni broj 2.048 i tri funkcionalne akcije |

**Splash**: vlasnikov original sadrži nacrtanu iOS-statusnu traku i šire zamućene pločice u pozadini; Android UI snimka prikazuje drugačiji sustav i ne smije lažirati iOS statusnu traku. Njegov sirovi full-screen MAE zato **nije** izravno usporediv ni legitiman kriterij za 1:1. Potrebna je registrirana usporedba ROI logotipa i pločica, bez OS kroma.

## Implementirano u ovoj grani

### 1. Dnevni izazov
- Nova `dailyPreviewColor(int)`: zasebno kalibrirana paleta za 10 ilustriranih 3D pločica u **svijetlom 941×1672 portretu**. Mjereni uzorci source top-face RGB (npr. 2≈253/140/93, 4≈52/225/211, 8≈77/172/253, 16≈253/204/90) prethodno su bili manje zasićeni na 4 i 8.
- Iste pločice, redoslijed, veličine, 3D osvjetljenje, rubovi i tipografija ostaju nativni; stvarni `tileColor()`, bodovanje, dnevni cilj, datum i korisničke nagrade **nisu** promijenjeni.
- U tamnoj i živopisnoj temi te kod drugih oblika zaslona zadržane su postojeće boje.
- Android regresijski test provjerava 9 bitnih nijansi, stvarne gameplay palete i funkcionalne tipke Natrag/Započni.

### 2. Klasično 4×4
- Izvorni rukom oblikovani navy naslov **Klasično**, koji ranije nije mogao biti precizno prikazan običnim Nunito fontom, izdvojen je u 12 SVG-ekvivalentnih Canvas kontura (`ClassicBrandContours.NAVY`, 175 čvorova, približna tolerancija konture 1,65px).
- `ownerClassicVectorHeading()` boja nativni `Path` s EVEN_ODD otvorima u slovima, navy 3D gradijentom i mekom sjenom, **bez** prikaza izvorne bitmap slike u igri.
- Ovaj tretman upotrebljava se samo u aktivnom svijetlom referentnom načinu Klasično 4×4. Sve tamne/živopisne teme, kompaktni 20:9/landscape i postojeći HUD/4×4 hitboxi zadržavaju svoje odvojene putanje.
- Nativni vektorski trag se pojavljuje u testnom `TextRegion` pod stvarnom vidljivom granicom, a ne u dodatnom prikrivenom tekstu. Prilagođen je stari test koji je pogrešno zahtijevao da rukom nacrtani naslov nikad ne dodiruje značku; izvorni PNG **namjerno slojevito** postavlja koraljnu značku ispred donjeg navy obrisa.

### 3. Postavke
- Svijetli referentni portretni footer ponovno koristi postojeću vektorsku, zaobljenu siluetu Br2jko logotipa s lokalno smanjenim 3D koraljnim znakom 2 i njegovim zrakama, umjesto fallback napisa.
- Sva četiri prekidača i tri povezana retka ostaju stvarne interaktivne kontrole. Lokalna kopija nije lažno predstavljena kao cloud sync.
- Podnožje nastavlja prikazivati **stvarnu** verziju aplikacije i autora Brendigo. Test verificira sedam aktivnih redaka i lokalni path.

## Zasebna kontrola ostalih zaslona

- **Home:** postojeći nativni vektorski Br2jko, rezultati, glavna akcija i prečaci ostaju netaknuti. Za daljnje izmjene potrebna je izolirana usporedba naslova, 3D pozadinskih pločica i stvarnih touch zona.
- **Pobjeda:** postojeći vlasnikov nativni Bravo! Path, kruna, koraljni/ivory/plavi akcijski pojasevi ostaju netaknuti. Testirati rezultat i `Nastavi igrati`/`Nova igra`/`Podijeli rezultat`.
- **Splash:** izolirati korisnikove grafičke elemente od iOS/Android statusne trake i usporediti boje, dubinu i layout. Ne dodavati statusni chrome kao bitmap.
- **Opušteno 5×5:** održati 25-polja, čitljivost 1024/2048, stvarni Pauza/Poništi i 3D značku u obje teme. Između dostavljenih referenci nema pune samostalne vlasnikove slike načina 5×5, pa je neutemeljeno proglasiti potpuno 1:1.
- **Svi modalni prozori:** postojeći testovi sedam modalnih stanja s aktivnim stvarnim gumbima, bez klikova ispod modala. Pauza i kraj igre nemaju zasebnu punu vlasnikovu referencu.

## Release gate

- [ ] `Android pull request validation`: build/lint/unit na završnom commit SHA.
- [ ] `Android UI smoke and screenshot QA`: svi emulatorski testovi, izvještaj i svježi `phase51-` PNG-ovi.
- [ ] `Android 16 navigation bar and inset QA`: geste i 3 tipke.
- [ ] Tek nakon rezultata preuzeti nove screenshotove, usporediti iste ROI prema istim izvorima i prijaviti **stvarni** MAE nakon izmjene.
- [ ] Spojiti tek kada su tri CI procesa zelena. Izrada novog AAB/APK izdanja iz `main`, bez tvrdnje pixel-perfect 1:1 prije punog audita.

Nema screenshotova korisnikovih izvornih slika ugrađenih u aplikaciju, novih dopuštenja ili promjene lokalnih podataka.
