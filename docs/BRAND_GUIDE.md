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

Preporučeno pismo: Nunito (ako je lokalno ugrađen i licenciran); za početnu implementaciju koristi se Android `sans-serif-rounded`, bez učitavanja fonta s interneta.

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
