# BROJKO — faza 142: izvorni ROI, stvarni podaci i sigurnost regresija

**Polazište:** potvrđeno izdanje [v1.12.241](https://github.com/bren-wp/Brojko/releases/tag/v1.12.241), GitHub SHA `eab1b8ac446db4ed0d5570ddd24e7633f94566ab`.

## Kako se mjeri

Originalni vlasnikovi PNG-ovi, 941 × 1672, iz privatne knjižnice:
`22_15_15-6` (Dnevni izazov), `22_15_19-10` (Postavke) i
`22_15_17-8` (Statistika). Uspoređuju se sa **stvarnim PNG-ovima**
iz prethodnog Android emulator testa (PR #141, zadnji SHA), ne
generiranim slikama. RGB MAE = srednja apsolutna razlika kanala,
skala 0–255. Nema skaliranja, pomicanja, zaokretanja ni poravnanja.
**Manje je bolje; MAE nije postotak odstupanja.**

| Mjerenje | Izvorni ROI | Baseline v1.12.241 (MAE) |
| --- | --- | ---: |
| Dnevni izazov cijeli | (0,0)-(941,1672) | 13,7632 |
| Poklon i ilustracija | (90,1300)-(290,1510) | 20,8462 |
| Ljubičasto tijelo dara | (95,1400)-(287,1510) | 24,3818 |
| Zlatna mašna | (90,1300)-(285,1420) | 19,8282 |
| Nagradni panel | (35,1320)-(900,1550) | 18,7479 |
| Postavke cijele | (0,0)-(941,1672) | 10,7643 |
| Backup red | (32,828)-(905,998) | 19,3292 |
| Backup ilustracija | (65,850)-(170,976) | 23,4472 |
| Backup referentni prekidač | (725,853)-(872,959) | 42,7231 |
| Statistika cijela, **kontrolirani povijesni fixture** | (0,0)-(941,1672) | 12,9129 |
| Statistika grafikon, **kontrolirani fixture** | (55,790)-(880,1100) | 22,3378 |
| Statistika stupci, **kontrolirani fixture** | (55,880)-(880,1080) | 26,2964 |
| Statistika cijela, **prazna stvarna arhiva** | (0,0)-(941,1672) | 15,0206 |

Za Statistiku su potrebne **dvije različite snimke**. Popunjeni
kontrolirani test koristi realni renderer s testnim vrijednostima i
`golden-state-stats-941x1672.png` za provjeru izgleda grafova.
`stats-no-history-941x1672.png` provjerava da je prazna arhiva
**doista prazna**. Testni podaci nikad se ne upisuju u korisničku
arhivu niti se renderirani demo-scoreovi objavljuju kao pravi podaci.
Činjenica da je referenca popunjena **ne opravdava izmišljanje**
povijesti u aplikaciji.

U referenci Postavki postoji cloud-sync toggle. Aplikacija sada
ispravno otvara **lokalnu sigurnosnu kopiju**, ne cloud servis.
Njezina funkcionalnost i oznaka `Lokalna kopija` ostaju netaknute;
ne imitirati nepostojeći uključeni cloud switch radi MAE broja.

## Dovršeni ciljano ograničeni zahvati

- **Poklon:** geometrija iz v1.12.241 ostala je netaknuta.
  Prvi pokusaj s dodatnim ljubičastim spotlightom i sjenkom
  poklopca pogoršao je stvarni ROI pa je **vraćen**. Zadržan
  je samo izmjereno bolji zlatni odsjaj ruba mašne.
  Ne mijenja 256/3000/80 ciljeve, datum, nagrade ili igru.
- **Postavke:** pokušaj druge kružne strelice povećao je
  odstupanje pa je **vraćen** prethodni cloud pictogram.
  Umjesto toga preciznije se kalibrira samo donji ljubičasti
  pigment backup ikone u svijetlom referentnom portretu.
  Ostale ikone, stvarna funkcija lokalne kopije, navigacija
  i pohrana ostaju nepromijenjene.
- **Statistika:** dodatna vodoravna os u praznom/punom
  grafikonu nije poboljšala pixel diff i **vraćena je**.
  Prikazuju se isključivo stvarno spremljeni rezultati,
  bez demo-scoreova ili lažnih povijesnih redaka.

## Obvezni kriteriji prihvaćanja

1. PR Gradle build, Lint, unit testovi, SCA i pregled koda zeleni.
2. API 36 navigacijske trake/insets uspješno testirane.
3. Instrumentacijski testovi sva 3 zaslona i stvarne screenshot
   datoteke dostupne za zadnji SHA.
4. Ponovo usporediti navedene ROI-je s izvornim PNG-ovima.
   Bilo koji značajan lokalni regresijski zahvat **vratiti**.
5. Potvrditi da korisnička igra, povijest, rekord, postavke, backup
   i prava navigacija nisu promijenjeni.
6. Nakon mergea obvezno objaviti **novo GitHub izdanje APK + AAB**
   (bez produkcijskog potpisa dopušteno, ispravno označiti debug
   testni APK i nepotpisane release pakete).

Niti jedna izmjena ne opravdava tvrdnju 0% odstupanja bez stvarnog
pixel-pixel rezultata. Također se ne tvrdi Play nadogradnja jer ona
zahtijeva odgovarajući postojeći upload potpis.

## Prva iteracija i odluka prema stvarnim izmjerenim ROI-jevima

Prvi emulatorski test SHA `e162f76cc16183d24c914994a44b74c6c7355935`
završio je uspješno:
[Actions 43528669889](https://github.com/bren-wp/Brojko/actions/runs/43528669889),
artefakt `Brojko-real-device-UI` ID `14087375260`.
Usporedba s istim vlasničkim PNG-ovima, istim fiksturnim
podacima i istim prethodno navedenim pravokutnicima:

| ROI | v1.12.241 MAE | Prvi pokušaj | Δ |
| --- | ---: | ---: | ---: |
| Dar — cijeli lokalni ROI | **20,8462** | 21,9843 | +1,1380 |
| Dar — ljubičasto tijelo | **24,3818** | 26,9210 | +2,5393 |
| Dar — zlatna mašna | 19,8282 | **19,5708** | −0,2575 |
| Backup ikona | **23,4472** | 23,5599 | +0,1127 |
| Statistika — grafikon s testnim podacima | **22,3378** | 22,3610 | +0,0232 |
| Statistika — prazan grafikon | **28,7684** | 28,7861 | +0,0176 |

**Odluka:** sačuvati samo potvrđeni dobitak zlatne mašne;
vratiti spotlight/seam, dual arrows i dodatnu chart axis.
U novom commitu `94b988800ad3d28c1ed41f127aeac3875590ffeb`
testira se druga korekcija *samo* nijanse backup
piktograma, bez funkcionalnih zahvata.

Ovo nisu završne metrike novog SHA-a: novu snimku
treba preuzeti i usporediti nakon završetka CI-ja. Merge
je dopušten samo bez značajne lokalne regresije.
