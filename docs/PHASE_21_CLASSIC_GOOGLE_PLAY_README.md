# BROJKO — faza 21: Klasična 4×4 + Google Play README

Datum: 1. studenoga 2026.  
Grana: `feat/google-play-readme-classic-qa-20261101`

## README: vidljiva Google Play značka

U početni blok i odjeljak Preuzimanje dodana je **službena hrvatska Google Play PNG značka** koja je klikabilna i upućuje na točan URL:

`https://play.google.com/store/apps/details?id=com.brendigo.brojko`

Izvori slike i klikabilne veze različite su adrese. Značka koristi originalni Googleov resurs bez promjene boja ili proporcija:
`https://play.google.com/intl/en_us/badges/static/images/badges/hr_badge_web_generic.png`.

Značka se učitava s Googleova poslužitelja u prikazu README-a, a **nije dodana u Android runtime** niti zahtijeva novu Android dozvolu, servis ili biblioteku. Repozitorijski link na GitHub izdanja ostaje alternativan, a tekst ne tvrdi da je objava dostupna svim državama i uređajima.

## Vizualni audit 4×4 — izvorni PNG nasuprot CI-ju

- Izvorna vlasnikova 4×4 referenca: `Slika ChatGPT 24. lis 2026. 22_15_13-4(20261101-200919).png`, **941 × 1672 px**.
- Dvije stvarne emulator snimke: `classic-four-by-four-reference-941x1672.png` (samo početne dvije pločice) i **`golden-state-game-941x1672.png`** (izdvojeni fixture sa svih 16 vidljivih vrijednosti kao na korisnikovoj referenci).
- Usporedba na nativnim dimenzijama bez pomaka i skaliranja: RGB MAE `40,053` za *neusporedivu praznu ploču*, nasuprot **`20,785`** za istu popunjenu ploču.
- Poznati početni regionalni RGB MAE za usporediv fixture: zaglavlje `23,728`, ploča `21,453`, akcijske kontrole `22,759`.

**Neusporedivi testni podaci ne smiju se tumačiti kao vizualna regresija.** Izvorni PNG je ilustracija, a stvarno stanje igre u produkciji ostaje isključivo rezultat korisnikovih poteza.

## Stvarni zahvat u native Java/Canvas rendereru

`classicRaisedTile()` dobiva lokalni uski bijeli specularni prijelaz na gornjih 20% pločice, jer je gornji rub izvornog vlasnikova dizajna vidljivo svjetliji od plohe. Nema promjena u pravilima 2048, stvarnim bojama drugih načina igre, podacima ili mrežnim postavkama. Ostaju postojeći izljev sjene, zaobljeni rubovi, selektor teme i čitljive brojke.

Novi `classicFourByFourReferenceUsesSourceTopLightingAndRealControls` instrumentacijski test:
1. Izolirano stvara dosljedan fixture na 941 × 1672 s 16 pločica i aktivnim Undo stanjem.
2. Provjerava da je gornji koraljni dio pločice svjetliji od središnjeg dijela bez pozivanja posebnog bitmap renderera.
3. Provjerava sva tri stvarna akcijska područja: `Novo`, `Poništi`, `Završi`.
4. Provjerava da sam prikaz ne mijenja rezultat.
5. Sprema novu **stvarnu emulator snimku** `classic-source-lighting-941x1672.png`.

Validator zahtijeva tu novu snimku.

### Prva stvarna QA iteracija i kalibracija

Prva potpuna Android UI QA serija za commit `b5e29cf` (run [41350868215](https://github.com/bren-wp/Brojko/actions/runs/41350868215)) bila je **uspješna**. Preuzet je njezin stvarni emulator ZIP, bez lažnih prikaza.

| Ista regija (941 × 1672 px) | Prije | Nakon prvog sjaja |
|---|---:|---:|
| Cijeli zaslon | 20,785 | **20,459** |
| Ploča 4×4 | 21,453 | **20,867** |
| Gornji rub prve pločice | 12,659 | 14,230 |
| Sredina prve pločice | 16,412 | 16,412 |

Prvi sjaj je poboljšao ukupno odstupanje, ali bio je **prejak u gornjoj ROI pločice**. Zbog toga je njegova početna neprozirnost smanjena s `0x6a` na `0x46`, bez mijenjanja pozicije ili ostalih vizualnih komponenti. Sljedeća emulator serija mora potvrditi mjerljive rezultate zadnjeg commita; gornja tablica **nije** mjerenje zadnje kalibracije.

**1:1 se ne smije tvrditi bez cjelovitog vizualnog prihvaćanja.**

## Kriteriji prije zatvaranja

- [ ] Sva tri workflowa uspješna na posljednjem commit SHA.
- [ ] Vizualno potvrđena stvarna screenshot snimka (bez generiranih prikaza u chatu).
- [ ] Zabilježen nov RGB MAE na istom vlasnikovu PNG-u.
- [ ] Validirana Google Play README veza.
- [ ] Nastaviti daljnje vizualne dorade po ostalim zaslonima, uključujući Misije, unutar istog Draft PR-a.

**Ne spajati PR i ne objavljivati novo izdanje bez korisnikova zahtjeva.**
