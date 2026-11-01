# BROJKO — faza 20: Statistika i Teme, novi audit izvornih PNG-ova

Datum: 1. studenoga 2026.

## Izvorni dokazi

Deset PNG-ova korisnik je ponovno priložio u razgovoru. Među njima:

- Statistika: `Slika ChatGPT 24. lis 2026. 22_15_17-8(20261101-200917).png`, 941 × 1672 px.
- Teme i izgled: `Slika ChatGPT 24. lis 2026. 22_15_18-9(20261101-200917).png`, 941 × 1672 px.

Vlasnikova ilustrativna Statistika prikazuje deset grafičkih vrijednosti i niz pobjeda `7`. Dosadašnji Android CI fixture `stats-source-layout-941x1672.png` sadrži pet zabilježenih partija, različite vremenske podatke i nema isto stanje pobjeda. Usporedba RGB MAE stoga uključuje **razliku podataka**; ne smije se pripisati isključivo geometriji, boji ni rendereru. Stvarni korisnički rezultati se ne nadomještaju maketom.

## Potvrđeni početni RGB MAE prije promjena ove faze

Izvor: uspješna Android screenshot QA serija PR-a #31, run `41344625869`, artefakt `Brojko-real-device-UI`. PNG-ovi su uspoređeni na izvornim dimenzijama bez pomaka, rotacije ili skaliranja, s prosjekom apsolutne razlike RGB kanala (0–255).

| Regija | Koordinate (x0,y0,x1,y1) | RGB MAE |
|---|---|---:|
| Statistika, cijeli zaslon | (0,0,941,1672) | **21,287** |
| Statistika, zaglavlje | (20,30,920,306) | 30,786 |
| Statistika, gornja tri KPI-ja | (20,305,923,540) | 27,364 |
| Statistika, donja dva KPI-ja | (20,535,920,760) | 21,451 |
| Statistika, grafikon | (22,765,922,1160) | 25,825 |
| Statistika, stupci | (65,790,885,1115) | 32,495 |
| Statistika, povijest | (20,1160,920,1645) | 14,294 |
| Teme, cijeli zaslon | (0,0,941,1672) | **24,483** |
| Teme, zaglavlje | (20,20,920,375) | 24,662 |
| Teme, izbor teme | (20,385,920,780) | 39,365 |
| Teme, glavni panel | (20,800,920,1500) | 22,658 |
| Teme, prikaz pločica | (45,830,460,1490) | 25,832 |
| Teme, opcije | (455,830,900,1490) | 23,717 |
| Teme, donja navigacija | (20,1480,920,1670) | 13,654 |

Ove vrijednosti su **baseline prije promjene u ovoj fazi**, ne rezultat dorade.

## Stvarne izmjene

1. **Statistika**: naslov široke KPI kartice „Niz pobjeda” na referentnom portretnom Canvas rasporedu ostaje jedan redak. Ne mijenjaju se izračun stvarnog niza, arhiva, font drugih kartica ni brojčane vrijednosti.
2. **Teme i izgled**: izvorna animacijska kontrola sastoji se od jednolične vodoravne podloge i kratkog koraljnog odabranog segmenta, ne od napunjenog postotnog napretka i velike kružne ručice. Tri odabira `Niska`, `Srednja`, `Visoka` i spremljena postavka `animation` ostaju funkcionalni.
3. **Gornji odabir tema**: na izvornom PNG-u tamna i živopisna podloga započinju više i zauzimaju širi pregled od prethodno renderiranog (približno 102 × 96 logičkih jedinica s pomakom +18 od vrha kartice). Odvojeni geometrijski parametri za svijetlu i druge dvije teme sada renderiraju tamni/živopisni pregled oko 110 × 115, bliže stvarnoj referenci. Obje 2×2 minijature sada imaju 40 × 40 i korigirani vertikalni početak, bez promjene stvarne ploče igre.
4. Instrumentacijski test `themeAnimationUsesThreeSourcePillsAndPersistsSelection` provjerava boju sva tri odabira, tri dodirne zone, spremanje vrijednosti i stvarnu snimku `themes-animation-pill-941x1672.png`.
5. Test `themesDarkAndVividPreviewsFillRealSourceBounds` provjerava da obje tamne/živopisne podloge dosežu izvorne gornje i donje zone bez brisanja funkcionalnih kartica; snimka `themes-choice-bounds-941x1672.png`.
6. Emulator validator zahtijeva obje nove stvarne PNG snimke.

## Uvjeti

- Potrebni su uspješni zadnji HEAD checkovi: `Android pull request validation`, `Android 16 navigation bar and inset QA`, `Android UI smoke and screenshot QA`.
- Prije prihvaćanja usporediti **novu stvarnu emulator snimku** s istim izvornim PNG-ovima, istim koordinatama i kontroliranim fixtureom. Odvojiti razlike u podacima od vizualnih elemenata.
- Ako izmjena povećava MAE ili kvari dodirne zone i postojeće testove, vratiti je ili ispraviti u istom PR-u.
- Novi release ili spajanje nije dio ove razvojne faze. **Pixel-perfect 1:1 nije potvrđen.**
