# BROJKO — zaslon 2/10: Klasično 4×4

Referenca: korisnikova slika `Slika ChatGPT 24. lis 2026. 22_15_13-4(20261030-210602).png`, izvorno 941 × 1672 px. Stvarni emulator: `golden-state-game-941x1672.png` iz uspješnog CI workflowa [#41130372871](https://github.com/bren-wp/Brojko/actions/runs/41130372871), commit `31ab840`. U oba PNG-a iste su vrijednosti svih 16 pločica i HUD rezultata, bez reskaliranja, pomicanja ili dodavanja izmišljene grafike.

## Početna pixel baseline

| Regija | ROI (x0, y0, x1, y1) | RGB MAE |
|---|---|---:|
| Cijeli 941×1672 | (0,0,941,1672) | 21.730 |
| Naslov i koraljni akcenti | (30,70,500,350) | 37.725 |
| HUD: rekord, rezultat i pauza | (450,80,940,350) | 22.431 |
| Krem okvir 4×4 ploče | (20,350,920,1320) | 23.017 |
| Pločice sa stvarnim vrijednostima | (38,395,910,1288) | 24.965 |
| Donje tri kontrole | (35,1330,925,1605) | 23.708 |

RGB MAE = srednja apsolutna razlika svih R/G/B kanala (0–255), manji rezultat znači veću sličnost. Sam MAE ne dokazuje pixel-perfect 1:1.

## Vizualni zahvati u PR #28, samo gameplay 4×4

- Oznaka `4×4`: referentna širina 84 logičke jedinice umjesto prevelikih 93; ista funkcija značke.
- Pauza i gornji Undo: krem kartice, mekan vanjski reljef, veće, čitljive ikonice, aktivna Undo akcija samo kad stvarna partija dopušta poništavanje.
- Sve stvarne numeričke pločice: uklanjanje tvrdog dvostrukog obruba, širi topliji gradijent, suptilna pozadinska sjena; zadržani stvarni brojevi, redoslijed i deterministička logika poteza.
- Brojevi na pločicama: umjereno smanjeni samo za prikaz `game` / 4×4 / svijetla tema / `Zaobljene`, kako višeznamenkasti brojevi ne bi dodirivali rubove.
- Novo/Poništi: ilustriranije krem kartice i veće ikonice, ali ista logika spremanja, Undo i svih dodirnih zona.
- Gumb Završi i njegov potvrđujući modal nisu semantički promijenjeni.

## Funkcionalni ugovor

- Fiksna vizualna referenca od 16 vrijednosti služi **samo testiranju**; produkcijska igra i generator pločica zadržavaju stvarno stanje.
- Undo ne smije biti klikabilan kad nema prethodnog poteza; kad se odigra potez, mora biti klikabilan i ne smije dirati stanje ranije partije.
- Novo pokreće postojeću provjerenu akciju traženja potvrde i spremanja; Završi pokreće `confirmEnd`.
- Svi dijelovi ploče uključujući zadnji red ostaju dostupni gestama i na drugim gustoćama/veličinama zaslona.
- Ostali načini (opušteno 5×5, dnevno), sve tri teme, stilovi pločica, splash, Home i win-modal ostaju neizmijenjeni sem ove vizualne grane.

## Gate prije završetka

1. Android PR validation: Gradle compilation, lint i lokalni unit testovi.
2. Android 16 navigation/insets QA i Android emulator screenshot smoke testovi.
3. Preuzeti novu stvarnu snimku `golden-state-game-941x1672.png`; usporediti istu originalnu referencu i gornje ROI-jeve.
4. Posebno testirati Pause/Undo/Novo/Završi i swipe u zadnjem redu, te kratki 360×600 uređaj.
5. Vratiti pojedinačne promjene ako je regionalni rezultat lošiji ili ako je narušena pristupačnost.

**Status:** u razvoju. PR #28 ostaje Draft; nije potvrđeno da je zaslon doslovno pixel-perfect 1:1. 

**Sljedeći zaslon nakon Klasično 4×4: 3/10 — Pobjeda „Bravo!”.**
