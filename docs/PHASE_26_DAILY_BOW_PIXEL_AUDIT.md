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
