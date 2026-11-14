# BROJKO — Phase 143 owner 1:1 visual / functional QA

Datum početka: 14. studenoga 2026.

## Polazište i autentične reference

Prethodno potvrđeno [v1.12.242](https://github.com/bren-wp/Brojko/releases/tag/v1.12.242),
`main` SHA `cd3ec96a0777ebf961cfacf39cda7e49678a3249`.
Originalne vlasnikove PNG slike 941×1672 (24. listopada 2026.)
uspoređene su sa **stvarnim** emulator PNG-ovima iz
`Brojko-pr142-final-94b9888`, bez skaliranja, poravnanja,
pomicanja, zamjene slika ili umetanja demo vrijednosti u produkciju.

| Područje | ROI u stvarnim pikselima (x0,y0,x1,y1) | RGB MAE v1.12.242 |
| --- | --- | ---: |
| Dnevni izazov — poklon ukupno | (90,1300,290,1510) | 20,6888 |
| Dnevni izazov — zlatna mašna | (90,1300,285,1420) | 19,5708 |
| Dnevni izazov — ljubičasto tijelo | (95,1400,287,1510) | 24,3818 |
| Postavke — backup ikona | (65,850,170,976) | 22,9438 |
| Postavke — backup red | (32,828,905,998) | 19,2650 |
| Statistika — grafikon, izolirani arhivski fixture | (55,790,880,1100) | 22,3378 |
| Statistika — stupci, isti fixture | (55,880,880,1080) | 26,2964 |

RGB MAE je na skali od 0 do 255. **Nije postotak**.

## Implementacija

1. **3D poklon**: samo na svijetlom referentnom portretu,
   ljubičasto tijelo dobiva troslojni gradijent prilagođen
   stvarnom originalu i svjetliji bočni završetak. Postojeći
   potvrđeni oblik/položaj mašne, dva zlatna remena,
   poklopac i površina kartice ostaju isti.
2. **Sigurnosna kopija**: izvorni PNG prikazuje svijetli
   *ispunjeni* oblak i unutarnje **ljubičaste** kružne
   strelice. Prethodna kontura bila je šuplja bijela,
   s bijelom strelicom. Ova izmjena mijenja samo
   dekorativni prikaz ikone u svijetlom referentnom
   portretu i omekšava njezin pretjerani okvir/sjenu.
   `Sinkronizacija napretka` i dalje *funkcionalno otvara
   lokalnu sigurnosnu kopiju*; ne postoji prikrivena
   aktivna cloud sinkronizacija niti cloud switch.
3. **Grafikon stvarnih partija**: vrijednosti i maksimum
   čitaju samo stvarne `history` zapise, najnovijih 10,
   s kronološkim prikazom na osi. Ujedinjen pristup
   `statisticsChartValue` za obje veličine zaslona
   prikazuje stare negativne/nevaljale vrijednosti kao
   nulte stupce, ne izmišlja povijesne podatke i ne
   prepisuje spremljene partije.

## Funkcionalni test protiv gubitka podataka

`RealHistoryChartAndBackupPhase143Test`:
- Kreira 10 pravih JSON arhivskih redaka **samo** u
  posebnom `SharedPreferences` prostoru QA-a.
- Crta graf bodova i graf najveće pločice na stvarnom
  Android Canvasu; svih 10 naljepnica mora odgovarati
  spremljenim rezultatima u kronološkom redoslijedu.
- Potvrđuje da oba korisnička taba ostaju funkcionalna.
- Izdvojeni arhiv ne smije se prepisati renderiranjem;
  originalna aktivna sesija mora ostati ista.
- Klik na backup red mora otvoriti `backup` i ne
  smije promijeniti aktivnu partiju.
- Na kraju vraća originalni `SharedPreferences`,
  ekran, temu i izbornik grafikona te briše
  isključivo testni izdvojeni arhiv.

## Uvjeti za merge i izdanje

- Uspješni Gradle/Lint/unit/sigurnosni PR gate.
- Uspješan Android 16 edge-to-edge i emulatorski
  instrumentacijski UI test.
- **Obvezno** dohvatiti stvarni screenshot na
  posljednjem SHA-u, usporediti s istim izvornim
  PNG-ovima i istim ROI-jevima, vratiti promjene
  koje pogoršavaju ciljani rezultat.
- Nakon mergea obvezan novi GitHub Release s
  `app-debug.apk`, nepotpisanim release APK/AAB,
  checksum datotekom, SBOM-ovima i stvarnim
  screenshot dokazima. Komercijalno potpisivanje
  nije obvezno; debug APK služi samo za testiranje.

**Rezultat na posljednjem SHA-u ostaje nepoznat do ponovnog
stvarnog emulatorskog mjerenja.** Nije moguće tvrditi 0%
odstupanja, proizvodnu cloud sinkronizaciju ili produkcijsko
Play potpisivanje samo na temelju uspješnog builda.
