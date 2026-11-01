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
