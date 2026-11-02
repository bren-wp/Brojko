# BROJKO — faza 26: Dnevni izazov, mašna poklona

Datum: 2. studenoga 2026.  
Izvorni vlasnikov PNG: `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261101-200916).png`, **941 × 1672 px**.  
Stvarni Android screenshot nakon spojenog PR-a #34: `daily-gift-opaque-ribbon-941x1672.png`, artefakt [Android UI QA #41362165949](https://github.com/bren-wp/Brojko/actions/runs/41362165949).

## Provjereno polazište

PR #34 spojen je u `main`, pa je za daljnje izmjene otvorena nova grana. Za posljednji commit PR-a #34 potvrđena su tri uspješna workflowa. Originalna referenca i realna emulator snimka uspoređene su bez skaliranja i pomicanja, istog determinističkog testnog fixturea.

| Regija | Početni RGB MAE |
|---|---:|
| Cijeli Dnevni izazov | 17,027 |
| Panel nagrade [40,1315,899,1512) | 22,013 |
| Poklon [100,1310,320,1540) | 25,717 |
| Mašna [115,1310,310,1410) | **28,971** |
| Tijelo i vrpce [120,1400,302,1530) | 31,339 |

Zlatne bočne vrpce iz PR-a #34 sada su na pravim pozicijama i potvrđene stvarnim pikselima: x=170 i 259 na y=1480; ljubičasta sredina x=214. Preostalo veliko odstupanje je mašna. Vlasnikov original prikazuje izdignute **šuplje zlatne petlje** s tamnonarančastim unutarnjim rubovima (npr. x=165 i x=263 oko y=1374), dok prethodni emulator prikazuje široke, ravne svijetložute oblike.

## Stvarna produkcijska dorada

U postojećem Android Java/Canvas rendereru `dailyRewardGift()`:
- Preoblikovane su dvije Bezier petlje i lokalna sjenka, uz odmak od posljednjeg cilja.
- Uklonjeni su veliki puni svijetli ovali i dodane tamnojantarne uvučene unutrašnjosti petlji koje odgovaraju ilustraciji.
- Zadržane su dvije odvojene neprozirne zlatne vrpce, ljubičasto središte, postojeći poklopac, stvarni iznos i funkcionalnost dnevnog izazova.
- Za dvije petlje koristi se petlja bez alokacije privremenog niza na svakom Canvas crtanju. Nisu dodani novi fontovi, mrežni servisi ili grafički resursi.

## Regresijski test i prihvat

`dailyGiftBowHasHollowAmberLoopsOnRealEmulator` na Android emulatoru renderira stvarnu sliku 941 × 1672 i provjerava dvije jantarne unutrašnjosti mašne (x=165/263, y=1374), obje zlatne okomite vrpce, ljubičastu sredinu i očuvan niz dana. Screenshot validator dodatno zahtijeva `daily-gift-bow-hollows-941x1672.png`.

- [ ] Sva tri obavezna workflowa uspješna na posljednjem commit SHA.
- [ ] Iz stvarnog novog emulator ZIP-a izmjereni RGB MAE istih regija prema izvorniku.
- [ ] Provjeriti vizualno da petlje ne dodiruju posljednji cilj i da nitko nije dobio lažnu nagradu.
- [ ] Nastaviti preostale regije u istom Draft PR-u.

**Bez mergea ili izdanja bez zahtjeva korisnika. Ne tvrditi 1:1 dok ne postoji pixel potvrda.**


## Prva prava emulator iteracija i drugi geometrijski zahvat

GitHub Actions `41363562944` nakon prvog zahvata završio je uspješno. Snimka iz stvarnog Android emulatorskog artefakta `13106382021` ima izvornih 941 × 1672 px i isti fixture. Usporedba s vlasnikovim izvornikom provedena je bez poravnanja ili skaliranja:

| Regija | Prije | Nakon prve iteracije |
|---|---:|---:|
| Cijeli zaslon | 17,027 | **16,970** |
| Panel nagrade | 22,013 | **21,486** |
| Poklon | 25,717 | **23,955** |
| Mašna | 28,971 | **24,398** |
| Tijelo i vrpce | 31,339 | 31,339 |

Detaljniji pixel audit pokazao je još jednu razliku: u vlasnikovu izvorniku sredina iznad mašne na x=210, y=1365 je gotovo bijela `(238,236,254)`, a na prvoj novoj snimci bila je prerano zlatna `(255,220,99)`. Originalni čvor počinje niže: oko y=1378 i ostaje zlatan na y=1390. Zato je u svijetlom portretnom prikazu centralni čvor spušten promjenom `top-size*.22` na `top-size*.11` i skraćen s `size*.21` na `size*.18`. Tamna tema i ostali layouti čuvaju dosadašnju geometriju.

Instrumentacijski test dodatno mjeri stvarni krem-bijeli prostor na x=210, y=1365 i zlatni čvor na x=210, y=1390. **Gornji MAE rezultat je prva iteracija, ne nova izmjena čvora.** Novu iteraciju treba izračunati iz posljednjeg uspješnog emulator artefakta.


## Faza 27: stvarna ciljna pločica 256 na Dnevnom izazovu

Nakon mjerenja zadnje uspješne emulator snimke `daily-source-streak7-941x1672.png` iz PR-a #35, uspoređene s originalom `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261101-200916).png`, otkriven je prevelik bijeli natpis **256** na prvoj ciljnoj pločici. Oba izvora imaju točno 941 × 1672, bez pomicanja ili skaliranja.

U podregiji `[108,1000,181,1060)` maska svijetlih, gotovo neutralnih piksela (`min(R,G,B)>220`, `max-min<30`) ima **617 piksela na originalu**, a **1168 na prethodnoj stvarnoj emulator snimci**. Primjetna razlika dolazi od veličine natpisa, ne od stvarnog cilja igre.

U izvornom Java/Canvas `dailyGoalRow()` veličina fonta broja 256 smanjena je s 20 na 16 jedinica, a osnovna linija blago je povišena, isključivo za svijetlu portretnu temu. Nije promijenjena vrijednost pločice, uvjet `peak>=256`, spremanje igre, stil ostalih tema ni dodirne zone pomoći.

Novi `dailyGoal256UsesCompactSourceSizedNumeral` instrumentacijski test izravno čita stvarne piksele determinističkog emulator fixturea na 941 × 1672, provjerava da bijeli natpis ostaje čitljiv, ali ne prepunjava pločicu, i sprema stvarni `daily-goal-256-compact-941x1672.png`. Screenshot validator zahtijeva tu novu PNG datoteku.

**Prethodnih 617/1168 piksela nisu rezultati nove izmjene.** Za prihvaćanje je potrebno preuzeti posljednji screenshot QA ZIP i izračunati isti maskirani broj, RGB MAE ciljne pločice i cijelog zaslona. PR ostaje Draft, bez mergea ili nove objave, sve dok ne bude potvrđena nova snimka.


## Faza 28 — zvjezdica cilja „Ostvari 3000 bodova”

Nastavak na istoj otvorenoj razvojnoj grani i u Draft PR-u #35. Izvornik `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261101-200916).png` i stvarni emulator screenshot `daily-goal-256-compact-941x1672.png` posljednjeg uspješnog QA-a imaju točno 941 × 1672 px i odgovarajući fixture. Nisu skalirani niti pomicani.

U izvornom drugom dnevnom cilju nagradna zvjezdica ima **puni, izdignuti zlatni volumen**. Stvarni prikaz u Android rendereru bio je blijed, uži i blago previsoko/lijevo. U istom ROI-u `[60,1080,195,1210)`, uz zasićenu zlatnu masku `R>190 && G>90 && B<155 && R>G+24`, izvorni PNG ima **4063 piksela**, a prethodni emulator **2582**. Izvorni maskirani centroid je približno (146,64; 1150,63), a prethodni (142,04; 1146,28). Polazni RGB MAE ovog ROI-a je **16,024**.

**Stvarna promjena:** u postojećem nativnom Java/Canvas rendereru za svijetlu portretnu Dnevnu igru povećana je ciljna zlatna zvjezdica s 35 na 38,5 logičkih jedinica, pomaknuta približno 2 jedinice udesno i 1 dolje, produbljena je zlatna ispuna i smanjen bijeli sjaj. Zaobljene petokrake dobile su jači volumen i manje šiljaste rubove. Ostale zvjezdice, druge teme, nagrade, vrijednost cilja, pravilnik igre i svi korisnički podaci zadržavaju postojeće ponašanje.

Instrumentacijski test `daily3000PointStarMatchesSourceGoldAreaAndKeepsRealGoal` prikazuje pravi Android Canvas u 941 × 1672, mjeri broj i centroid zlatnih piksela, provjerava funkcionalnu pomoć druge misije, da iscrtavanje nije pokrenulo lažnu igru ili promijenilo streak te snima `daily-goal-3000-gold-star-941x1672.png`. Validator zahtijeva novu stvarnu snimku.

- [ ] Sva tri obavezna workflowa uspješna na **posljednjem HEAD-u**.
- [ ] Novi RGB MAE, maska i centroid iz posljednjeg stvarnog emulator artefakta.
- [ ] Korekcija u istom PR-u ako test ili regionalna usporedba pokažu regresiju.
- [ ] Ostali zasloni i UI stanja ostaju otvoreni za budući pixel audit.

Nijedan rezultat nove izmjene ne smije se navesti kao potvrđen prije stvarnog screenshot QA-a. Nema mergea ni izdanja. Izmjerena blizina reference nije automatski 1:1.
