# BROJKO — zaslon 3/10: Pobjeda „Bravo!”

Referenca: vlasnikova izvorna slika `Slika ChatGPT 24. lis 2026. 22_15_14-5(20261030-210605).png`, rezolucija **941 × 1672**. Baseline: stvarna emulator snimka `golden-state-win-941x1672.png` iz uspješno završenog Android UI QA workflowa [#41130372871](https://github.com/bren-wp/Brojko/actions/runs/41130372871), commit `31ab840`. Mjerenje provodi RGB mean absolute error, bez reskaliranja i geometrijskog poravnanja.

## Polazne metrike

| ROI u izvornim pikselima | RGB MAE |
|---|---:|
| Cijeli ekran (0,0,941,1672) | 19.329 |
| Modal (135,450,805,1415) | 27.045 |
| Kruna (255,330,650,570) | 27.492 |
| Natpis Bravo! (210,525,700,770) | 45.213 |
| Natpis o pločici 2048 (220,710,735,810) | 26.087 |
| Kartice rezultata (160,795,760,974) | 23.188 |
| Zvjezdice (470,850,745,940) | 16.413 |
| Tri CTA gumba (160,975,775,1385) | 27.440 |
| Zasjenjena pozadina (20,100,890,420) | 16.671 |

`RGB MAE` nije dokaz apsolutne identičnosti 1:1. Vlasnikova ilustracija sadrži ručno oblikovanu tipografiju i meke 3D materijale, dok aplikacija radi s interaktivnim Android Canvas prikazom.

## Funkcionalni ugovor

- **Nastavi igrati** potvrđuje pobjedu, sprema i nastavlja *istu* partiju i rezultat.
- **Nova igra** koristi postojeću funkciju `newGame`; ne stvara lažne povijesne rezultate.
- **Podijeli rezultat** pokreće Android share s aktualnim rezultatom.
- Pozadinska ploča za stvarnog korisnika prikazuje njegove stvarne pločice.
- Snapshot fixture `originalPixelReferencesUseIdenticalRepresentativeGameData` privremeno postavlja demonstracijsku 4×4 ploču iz referentnog PNG-a samo u testnoj aktivnosti: `2,4,8,16 / 4,8,16,32 / 8,16,32,64 / 128,256,512,1024`. Usluga i stvarni podaci igre ne mijenjaju se.

## Iteracija u postojećem PR-u #28

- Tipografija `Bravo!`: korigirana pretjerana visina, smanjen bijeli obrub; zadržana blaga rotacija pojedinih znakova.
- Glavni CTA: jači koraljni gradijent, ublažen tvrdi bijeli obrub i debeli tamnocrveni rub.
- Gumb za dijeljenje: plavi gradijent i ublažen vanjski rub.
- Gumb `Nova igra`: topliji, mekši krem reljef; dvije kružne strelice kao na izvornoj ilustraciji.
- Kartice rezultata: približene toploj nijansi RGB(249,241,228).
- Natpis `Dosegnuli ste 2048!`: pomaknut i zasićenijeg kontrasta radi izvornog vizualnog standarda.
- Dodan instrumentacijski test `victoryNativePixelsUseWarmSummaryAndCoralButton`, koji provjerava osnovne boje i tri funkcionalna touch targeta.

## Uvjeti prije zaključivanja

1. Gradle build, Android lint i unit testovi moraju proći za završni commit.
2. Android 16 navigation/inset QA mora proći.
3. Prava testna snimka `golden-state-win-941x1672.png` mora se ponovno usporediti s istom izvornom referencom.
4. Mora se pregledati barem full-screen, heading, modal, stars i buttons ROI; regrese vratiti ili ispraviti.
5. Treba provjeriti 360×600 i ostale ekrane radi regresija.
6. PR ostaje Draft dok uvjeti nisu potvrđeni; novo izdanje nije potrebno prije spajanja.

**Sljedeći zaslon po redu nakon Pobjede: 4/10 — Dnevni izazov.**
