# Brojko — vizualna specifikacija

Primarni identitet: **Brojko by Brendigo**. Slogan: **Spajaj brojeve. Ruši rekorde.**

## Paleta
| Element | HEX |
|---|---|
| Koraljna | `#FF7054` |
| Tirkizna | `#27C7BB` |
| Tamnoplava tipografija | `#192B4D` |
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
