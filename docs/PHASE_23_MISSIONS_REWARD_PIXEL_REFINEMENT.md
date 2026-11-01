# BROJKO — faza 23: Misije, zvjezdice nagrada i stvarni tekstovi

Datum: 1. studenoga 2026.  
Izvorna vlasnikova referenca: `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261101-200917).png`, 941 × 1672 px.  
Polazna stvarna emulator snimka nakon PR-a #33: `missions-source-progress-palette-941x1672.png`. Nema skaliranja, rotacije ni pomicanja.

## Utvrđena odstupanja u prvom retku Misija

Na jednakom izrezu `x=770..859, y=399..487` mjerena je zlatna zvjezdica nagrade (detektor: R>190, 90<G<230, B<145, R>G+30).

| Metrika | Vlasnikov PNG | Dosadašnji emulator |
|---|---:|---:|
| Broj zlatnih piksela | 2293 | 1912 |
| X težište (px) | 821,84 | 811,28 |
| Y težište (px) | 456,09 | 432,73 |

Emulatorova zvjezdica bila je manja, ~10 px previše lijevo i ~23 px previsoko. Radi se o usporedbi **stvarnih PNG datoteka**, bez nadomještanja korisnikova rezultata.

## Stvarna implementacija u Java/Canvasu

- U portretnom, svijetlom prikazu Misija pomaknuta je samo zlatna nagradna zvjezdica (x+4, y+10 logičkih jedinica) i povećan njen vektorski promjer s 31 na 35 jedinica. Tekst `+50/+100/…`, kartice, brojači, progress i dodjela nagrada ostaju nepromijenjeni.
- Gornji gradijent zlatne zvjezdice u Misijama kalibriran je s `#FFED87` na `#FFD45B`, jer izvorna ilustracija ima snažniju žutu boju i manje blijed gornji rub. Svi ostali zasloni zadržavaju postojeću paletu zvjezdice.
- Bez promjena logike misija ili stvarnih korisničkih podataka ispravljeni su opisi `Spoji 20 pločica`, `Dosegni 512 bez poništavanja`, `Igraj 7 dana zaredom` i `Kralj rezultata` prema izvornom hrvatskom tekstu. Ciljevi, iznosi, izračuni i spremanje ostaju identični.
- Nisu dodani novi resursi, servisi ni biblioteke.

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
