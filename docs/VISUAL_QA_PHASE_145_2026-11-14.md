# BROJKO — faza 145: zaobljene stranice 3D poklona i izvorne boje stvarnog grafikona

Datum: 14. studenoga 2026.

## Dokaz i baseline

Izvorna slika vlasnika: `Slika ChatGPT 24. lis 2026. 22_15_15-6`
(Dnevni izazov) i `22_15_17-8` (Statistika), obje
**941 × 1672**. Baseline su **stvarni emulator PNG-ovi** iz
[v1.12.244](https://github.com/bren-wp/Brojko/releases/tag/v1.12.244)
i [run #43539850459](https://github.com/bren-wp/Brojko/actions/runs/43539850459),
artefakt `Brojko-real-device-UI` ID `14091922065`, konkretno
`daily-refined-board-gift-941x1672.png` i
`golden-state-stats-941x1672.png`.

RGB MAE je prosječna apsolutna pogreška po kanalu na rasponu
0–255. **Nije postotak vizualnog odstupanja.** Nema resizea,
pomicanja, registracije, simuliranih datuma ni promjene korisničkih
rezultata.

| Izvorni ROI | Koordinate (x0,y0,x1,y1) | v1.12.244 RGB MAE |
| --- | --- | ---: |
| 3D poklon, cijeli | (90,1300,290,1510) | 17,976349 |
| Ljubičasto tijelo | (95,1400,287,1510) | 20,442551 |
| Zlatne vrpce | (100,1400,265,1508) | 20,798709 |
| Zlatna mašna | (90,1300,285,1420) | 18,529729 |
| Statistika, grafikon | (55,790,880,1100) | 21,949750 |
| Stupci i natpisi | (55,880,880,1080) | 26,296380 |
| Osnovica | (33,1091,850,1097) | 8,416837 |

## Analiza razlika

- Dar u izvorniku ima **konveksne tamnije ljubičaste bočne plohe**,
  kojih na prethodnoj vektorskoj ilustraciji nedostaje. U novom
  pokušaju samo u *svijetlom referentnom portretu* iscrtavaju se
  dvije uske, zaobljene, poluprozirne sjene **samo na gornjih
  52 % već potvrđene siluete**. Prvi pokušaj sjenčanja cijele
  visine pokazao je regresiju i nije zadržan. Ne mijenjaju se mašna, zlatne vrpce, hitboxovi,
  dimenzije, nagrade, dnevni ciljevi ili 2048 logika.
- U istom izvoru medijan boje koraljnog stupca je RGB
  **(254,135,91)** nasuprot prethodnih **(253,141,97)**.
  Medijan tirkiznog je **(56,216,199)** nasuprot prethodnih
  **(47,207,191)**. Nova baza pigmenata samo na svijetlom
  vlasničkom grafikonu: koraljna `#FE8357`, tirkizna
  `#34D7C7`; zadržana je postojeća suptilna gradijentna
  rasvjeta i sav stvarni povijesni prikaz.
- Postojeći originalni rezultati u referenci nisu u istom
  redoslijedu kao QA fixture. Ne kopirati ni preslagivati
  stvarne rezultate radi smanjenja broja: na stvarnim
  testnim snimkama prikazuju se stvarni sačuvani redci.
- Očuvana je faza 144: izvorna tanka osnovica ispod
  stvarnih pozitivnih stupaca ostaje, bez stvaranja
  lažne osnovice za praznu povijest.

## Testiranje i odluka o spajanju

Novi `OwnerGiftAndChartPigmentsPhase145Test`:
- Crta stvarni Canvas poklona u rezoluciji 941×1672 i
  provjerava ljubičasto tijelo i dva zlatna remena.
- Kreira 10 stvarnih JSON povijesnih redaka **isključivo u
  izoliranom QA SharedPreferencesu**, provjerava prisutnost
  dviju boja na stvarnim stupcima i potpuno nepromijenjenu
  aktivnu partiju te izvorni arhiv. Briše samo QA pohranu
  u `finally`.
- Postojeći instrumentacijski testovi nadalje provjeravaju
  redoslijed 10 redaka, tabove, praznu povijest bez osi
  i funkcionalan lokalni backup bez cloud pretvaranja.

**Prihvatni kriterij**: CI build/Lint/unit/SCA, API 36 navigacija
i puni emulator screenshot test moraju biti zeleni na
**zadnjem SHA-u**. Ponoviti RGB MAE sa stvarnim Android
screenshotovima i istim vlasnikovim PNG-ovima. Ako se
mjerena područja pogoršaju, vratiti ili korigirati
konkretnu regresijsku izmjenu.

Nakon svakog uspješnog mergea obvezno objaviti **novi GitHub
Release APK + AAB** (dopušteno nepotpisano preview izdanje
s instalabilnim *debug-signed* testnim APK-om, SHA256SUMS,
SBOM-om i stvarnim emulator PNG-ovima).

Nije dopuštena tvrdnja o 1:1 ili 0% odstupanja dok ju
pixel-diff stvarno ne potvrdi.

## Kontrolno mjerenje — prvi pokušaj i ograničavanje regresije

Na originalnim PNG-ovima i stvarnim novim Android snimkama
[run #43541746829](https://github.com/bren-wp/Brojko/actions/runs/43541746829),
artefakt `Brojko-real-device-UI` ID `14092685911`,
izmjereno je sljedeće (941×1672, iste originalne koordinate):

| ROI | v1.12.244 | Prvi pokušaj | Promjena |
| --- | ---: | ---: | ---: |
| 3D poklon cijeli | **17,976349** | 18,158452 | **+0,182103 regresija** |
| Tijelo | **20,442551** | 20,737816 | **+0,295265 regresija** |
| Vrpce | **20,798709** | 21,026319 | **+0,227609 regresija** |
| Mašna | 18,529729 | 18,527422 | −0,002308 |
| Grafikon cijeli | **21,949750** | 22,098445 | **+0,148695 regresija** |
| Stupci | **26,296380** | 26,409895 | **+0,113515 regresija** |
| Osnovica | **8,416837** | 8,467020 | **+0,050183 regresija** |

**Zbog toga prvi pokušaj nije prihvaćen.** Iz samih stvarnih
PNG-ova napravljen je kontrolirani *counterfactual* bez
interpolacije: sjenčanje samo do stvarnog y=1460
predviđa približno **−0,15 MAE** u cijelom poklon
ROI-u, jer donje sjenčanje čini original svjetlijim
i pogoršava usporedbu. Preračunato na 400-unit
Canvas: gornjih oko 52 % tijela. Prethodna tamna
donja ploha više se ne crta.

Za grafikon prvi pokušaj s dvije boje dokazano je
pogoršao MAE. Koraljni pigment vraćen je 1:1 na
v1.12.244, a jedino je tirkizni pomaknut za otprilike
(+5,+6,+6) RGB kanala. Odvojena simulacija
na stvarnim pikselima predviđa oko **−0,123 MAE**
u ukupnom ROI-u grafikona. Nisu izmijenjeni
stvarni redci, kronološki slijed, visine, osi,
datumi ili tabovi.

**To su predviđanja, ne rezultati drugog commita.**
Prije spajanja potreban je novi zeleni emulator
workflow i novi stvarni RGB MAE na posljednjem SHA-u.
