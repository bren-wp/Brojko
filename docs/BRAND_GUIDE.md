# Brojko — vizualna specifikacija

Primarni identitet: **Brojko by Brendigo**. Slogan: **Spajaj brojeve. Ruši rekorde.**

## Paleta
| Element | HEX |
|---|---|
| Koraljna | `#FF7A5B` |
| Tirkizna | `#2EC4B6` |
| Tamnoplava tipografija | `#1E2A44` |
| Krem pozadina | `#FFF8EE` |
| Pločica 8 | `#3D98F0` |
| Pločica 16 | `#FFBB4A` |
| Pločica 64 | `#FF6F96` |
| Pločica 128 | `#BD60E9` |

Tipografija: lokalno pripremljeni **Nunito Variable** (SIL OFL 1.1), bez mrežnog preuzimanja tijekom rada aplikacije; lokalna skripta provjerava izvor fonta i licencu prije Android gradnje.

## Dizajnerske referencije
Korisničke slike predstavljaju cilj za početni izbornik, 4×4 igru, zaslon pobjede, dnevni izazov, misije i postignuća, statistiku, teme i postavke. UI se crta programatski, tako da raspored ostaje prikladan za različite rezolucije. Točne nijanse, sjenčanja, tipografija i sve animacije još zahtijevaju vizualno QA testiranje na uređaju. Ekrani se ne smiju lažno prikazivati kao 1:1 testirani dok nisu uspoređeni sa stvarnim screenshotovima.

## Upravljanje
Povlačenje unutar ploče gore/dolje/lijevo/desno; početni izbornik i akcije upravljaju se velikim tipkama. Rezultat, status, pobjeda i kraj partije stvarne su vrijednosti enginea, nisu fiksni tekstovi iz predložaka.


## Launcher v1.6.21

Ikona zadržava originalni četverobojni motiv (2, 4, 8, 16) i dominantno svijetlo slovo B. Dodani su glatki sjajni rubovi i kratke svjetlosne zrake te prilagođena veličina u adaptive foreground maski. Testirati kružni i zaobljeni Android launcher prije izjave o pikselnoj podudarnosti.


## Nunito font — v1.7

Aplikacija u CI-u uključuje **Nunito Variable** s potvrđenog izvornog Google Fonts commita (SIL OFL 1.1). Skripta `scripts/prepare-nunito.sh` verificira SHA-1 Git blob izvornog fonta i licence te ih umeće u Android `assets`. Glavni tekst koristi težinu 500, oznake kartica 800, istaknuti naslovi i logotip 950. Pri lokalnoj izgradnji prvo pokrenuti skriptu. Font se ne preuzima u aplikaciji i nije potrebno mrežno dopuštenje.

## Launcher ikona — izvorni portretni brending v1.12.97

Obje bojene Android VectorDrawable varijante (`ic_brojko.xml` i `ic_brojko_foreground.xml`) koriste četiri individualno zaobljene, odvojene pločice 2/4/8/16 s originalnom coral/teal/blue/yellow paletom, finim donjim rubom te dominantno većim svijetlim slovom **B** s koraljnom granicom. Nisu uvedeni bitmap placeholderi; svi oblici ostaju skalabilni unutar Android adaptive/round maske. Android 13 monochrome sloj ostaje zaseban za sistemsko temiranje; naziv i autor zadržavaju **Brojko** i **Brendigo** (veliko B).

Pregled provode stvarni Android emulator screenshotovi launcher-adaptive, launcher-round, launcher-android13-monochrome te `launcher-four-tiles-B-reference-512.png`. Izvornu marketinšku ilustraciju nemoguće je potpuno preslikati u Android VectorDrawable bez njezine napredne 3D rasvjete, ali struktura i brend motiv trebaju ostati dosljedni. 

## Završna vizualna faza 10/10 — Android launcher resursi

- **Jedan kanonski vektor:** `drawable/ic_brojko.xml` koristi četiri zaobljene
  pločice 2/4/8/16, koraljno uokvirenje i dominantno bijelo slovo B.
  Pre-Android 26 `mipmap-anydpi/ic_launcher.xml` i
  `ic_launcher_round.xml` imaju sada identičnu geometriju i paletu,
  bez neusklađenog prikaza ravnih kutova. Nema rasterskih zamjena.
- **Android 26+ maske:** `drawable/ic_brojko_foreground.xml` ima
  jednoliko skaliran sadržaj u središtu viewporta 108 × 108, faktor
  **0.69** oko koordinata (54,54) kako bi se izbjeglo rezanje numeričkih
  pločica kružnim, zaobljenim i drugim maskama. Brojevi i B ostaju
  vektorski, bez promjene gameplay prikaza ili originalnih brend boja.
- **Android 13+ themed icon:** `ic_brojko_monochrome.xml` ostaje
  jednobojni sloj pod kontrolom Androida, bez prisilnog koraljnog tona.
- **Automatski guard:** `python3 scripts/verify-brand-assets.py`
  provjerava identičnost fallback vektora, mask-safe foreground
  skaliranje, kanonsku paletu, svijetli B i oba themed sloja.
  Pokreće se kao obvezni korak Android PR validation.
- **Emulator snimke:** novi `phase10-adaptive-icon-safe-512.png`
  nastaje izvođenjem stvarnog Android `VectorDrawable` foregrounda
  rezolucije 512×512; uz već postojeće adaptive, round i monochrome
  snimke omogućuje dodatnu provjeru.
- **Ograničenje:** referentna marketinška ikona dio je većeg brend
  lista, nije izolirani 512×512 izvor. Nije korektno izmišljati
  referentni pixel MAE bez istih dimenzija, skaliranja i maske.
  Sve verzije Android maski trebaju biti vizualno provjerene na
  stvarnim emulator snimkama; **1:1 nije potvrđen**.
