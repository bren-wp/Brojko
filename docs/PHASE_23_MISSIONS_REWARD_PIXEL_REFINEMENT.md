# BROJKO — faza 23: Misije, zvjezdice nagrada i stvarni tekstovi

Datum: 1. studenoga 2026.  
Izvorna vlasnikova referenca: `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261101-200917).png`, 941 × 1672 px.  
Polazna stvarna emulator snimka nakon PR-a #33: `missions-source-progress-palette-941x1672.png`. Nema skaliranja, rotacije ni pomicanja.

## Utvrđena odstupanja u prvom retku Misija

Na jednakom izrezu `x=770..859, y=399..487` mjerena je zlatna zvjezdica nagrade (detektor: R>190, 90<G<230, B<145, R>G+30).

| Metrika | Vlasnikov PNG | Dosadašnji emulator |
|---|---:|---:|
| Broj zlatnih piksela | 2276 | 1850 |
| X težište (px) | 821,91 | 811,31 |
| Y težište (px) | 456,22 | 433,24 |

Emulatorova zvjezdica bila je manja, ~10 px previše lijevo i ~23 px previsoko. Radi se o usporedbi **stvarnih PNG datoteka**, bez nadomještanja korisnikova rezultata.

## Stvarna implementacija u Java/Canvasu

- U svijetlom portretnom prikazu Misija zlatna nagradna zvjezdica pomaknuta je x+4 i y+10 logičkih jedinica. Početni eksperiment s promjerom 35 ispravno je poravnao težište, ali je **prekrio natpis `+50`**. Zato je promjer vraćen na izvornu širinu od **31 jedinice**, vrhovi su zaobljeni, a natpis pomaknut niže za 6 jedinica. Prava funkcionalnost i iznosi nagrada nisu promijenjeni.
- Gornji gradijent zlatne zvjezdice u svijetlim Misijama kalibriran je s `#FFED87` na `#FFD45B`. Ostali zasloni i tamna tema zadržavaju postojeću paletu.
- Bez promjena logike misija ili stvarnih korisničkih podataka ispravljeni su opisi `Spoji 20 pločica`, `Dosegni 512 bez poništavanja`, `Igraj 7 dana zaredom` i `Kralj rezultata` prema izvornom hrvatskom tekstu. Ciljevi, iznosi, izračuni i spremanje ostaju identični.
- Nisu dodani novi resursi, servisi ni biblioteke.


## Prva emulator iteracija (prije korekcije preklapanja)

Uspješna Android UI QA serija `41354884986` provedena je na commitu `c483179`, **102 instrumentacijska testa**. Usporedba na 941×1672 bez skaliranja:

| Regija | Prije | Nakon prve iteracije |
|---|---:|---:|
| Cijeli zaslon | 24,991 | 24,534 |
| Zvjezdica prve dnevne nagrade | 33,363 | **11,997** |
| Prva kartica nagrade | 23,169 | **15,174** |

Prva povećana zvjezdica imala je 2569 zlatnih piksela i težište (820,33;454,79). Vlasnikov original ima 2276 piksela i težište (821,91;456,22). **Otkriveno preklapanje s natpisom** zahtijevalo je dodatnu korekciju vektorskih dimenzija i visine teksta; gornje poboljšanje nije završna potvrda. Potrebno je novo mjerenje nakon commita `38758d9`.

Maska zlatnih piksela računa se na **signed integer RGB kanalima** (izbjegnut uint8 overflow): R>190, 90<G<230, B<145 i R>G+30 u području [770,860) × [399,488).

## Regresijski QA

`missionsGoldenRewardStarMatchesOwnerSourceBounds` koristi pravi Canvas renderer na emulatoru i deterministički fixture; provjerava dovoljnu površinu zlatne zvjezdice, X/Y težište unutar 12 px vlasnikove izvorne reference i odsutnost neželjenih nagrada. Test sprema **stvarnu** datoteku `missions-reward-star-source-941x1672.png`, koju screenshot validator obavezno zahtijeva.

Postojeći `illustratedMissionsKeepRealProgressAndDistinctColors` uzorkuje novu stvarnu poziciju zvjezdice, ne stare prazne piksele.

### Za dovršetak

- [ ] Android pull request validation zelen na zadnjem SHA.
- [ ] Android 16 navigation bar and inset QA zelen na zadnjem SHA.
- [ ] Android UI smoke and screenshot QA zelen na zadnjem SHA.
- [ ] Preuzeti novi ZIP sa stvarnim screenshotovima i ponovno izračunati MAE te težište zvjezdice.
- [ ] Ako se bilo koja mjerena promjena pogorša, kalibrirati u istom PR-u i ponovno pokrenuti testove.
- [ ] Nastaviti sljedeći zaslon tek uz stvarne dokaze.

**Bez mergea i bez objavljivanja novog izdanja. 1:1 nije potvrđen.**


## Nastavak u istom Draft PR-u — izvorni naslovi odjeljaka

Na stvarnoj Android snimci iz uspješnog workflowa `41355720844` uspoređeni su vlasnikov PNG i emulator, 941 × 1672 px, bez promjene dimenzija, pomaka ni stanja testnog fixturea.

| Regija | Koordinate [x0,y0,x1,y1) | RGB MAE prije |
|---|---|---:|
| Cijeli zaslon Misija | [0,0,941,1672) | 24,384 |
| Dnevne — naslov | [130,305,465,375) | 35,662 |
| Tjedne — naslov | [130,775,465,845) | 38,336 |
| Postignuća — naslov | [130,1210,530,1300) | 46,264 |

Pritom je na izvornoj slici svaki naziv odjeljka poravnat blizu 150. piksela po X osi, uz podebljanu tipografiju, a prethodni emulator imao je različito centriranje i tanji font. U svijetlom portretnom prikazu uveden je specijalizirani lokalni Canvas `missionSectionTitle()` koji koristi postojeći `displayFont` (Nunito Black), vlastito lijevo poravnanje i veličinu 20,5 logičkih jedinica, bez dodatnih resursa, fontova ili vanjskih biblioteka. Tamna tema i druge veličine zadržavaju postojeći prikaz.

Regresijski test `missionsSectionHeadingsUseSourceWeightAndLeftAlignment` provjerava stvarne tamne piksele sva tri zaglavlja, zajednički početni rub, ograničenu širinu i nepromijenjene korisničke vrijednosti. Snima pravi Android PNG `missions-section-titles-941x1672.png`, koji je dodan obveznom validatoru. **Gornje RGB MAE vrijednosti su početne, ne rezultat nove izmjene.**

Novi screenshot QA nakon posljednjeg SHA mora ponovno potvrditi sva tri regionalna RGB MAE i uspješne instrumentacijske testove prije bilo kakvog mergea.
