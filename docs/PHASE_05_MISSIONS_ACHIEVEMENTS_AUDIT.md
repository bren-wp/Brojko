# BROJKO — zaslon 5/10: Misije i postignuća

**Aktivni PR:** #28, grana `feat/pixel-victory-modal-phase4` (ostaje Draft). Izvorna vlasnikova referenca: `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261030-210527).png`, 941 × 1672 px. Emulator baseline: `missions-six-real-progress-941x1672.png` iz [Android screenshot QA #41134544958](https://github.com/bren-wp/Brojko/actions/runs/41134544958), s podacima 12/20, 0/1, 5/7, 6.250/10.000, 0/1 i 8.400/50.000.

## Polazna izmjerena vizualna razlika

| ROI (x0,y0,x1,y1) na nativnim 941 × 1672 slikama | RGB MAE |
|---|---:|
| Cijeli zaslon (0,0,941,1672) | **26.189** |
| Zaglavlje (0,0,941,315) | 28.190 |
| Dnevne kartice (25,298,915,760) | 22.994 |
| Tjedne kartice (25,758,915,1235) | 29.628 |
| Postignuća (25,1218,915,1645) | 28.599 |
| Lijeve ilustracije zadataka (45,375,220,1630) | 35.258 |
| Desne nagrade (730,360,915,1625) | 20.032 |
| Tekst i trake napretka (225,350,735,1620) | 28.798 |

RGB MAE: prosječna apsolutna razlika RGB kanala po pikselu (niža vrijednost je bolja). Slike se **ne skaliraju i ne naknadno poravnavaju**. Ovo je početno, ne završno mjerenje. Ilustrativni 3D izvornik i native Canvas su različiti rasterizacijski mediji.

## Izmjene zaslona 5/10

- Lokalni Canvas prikazi umjesto jednostavnih placeholder ikona, **samo na svijetlom portretnom zaslonu Misije**:
  - Zadak 20 spajanja: rotirane 2/4 pločice i vektorski pokazivač prsta
  - Dosezanje 512 bez Undo: zakrenuta ljubičasta 512 s toplim zrakama
  - 7 dana uzastopno: reljefni kalendar s istaknutom sedmicom
  - 10.000 bodova: povećana koraljna meta i plava strelica
  - Majstor spajanja: čitljiv niz 2/4/8/16 u 2×2 mreži
  - Kralj rezultata: velika zlatna reljefna kruna
- Svjetliji pastelni blokovi dnevnih, tjednih i postignuća; plava kalendarska ikona u tjednom zaglavlju, umjesto crvenog dnevnog kalendara.
- Veća i malo bolje raspoređena tipografija naslovnog natpisa.
- Svijetle zlatne kartice za nagrade, veće zlatne zvjezdice i čitljiviji broj osvojenih zvjezdica.
- Nema bitmap resursa preuzetih izvana, novih runtime ovisnosti ni programskog mijenjanja podataka korisnika.

## Funkcionalni uvjeti i vjernost podacima

- `rewardFlags`, `todayMerges`, `weekPoints`, `playDayStreak`, `recordTile`, `best` i niz dana moraju ostati **stvarni**.
- Source maketa prikazuje „4/20” postignuća, ali stvarna igra ima samo **dva definirana postignuća**. UI zato ispravno koristi `completedAchievements()/2` (ne prikazivati izmišljeni nazivnik 20).
- Kod nagrada ne dodjeljuje zvjezdice, ne mijenja uvjete misija i ne daje lažne potvrde dovršetka.
- Test `illustratedMissionsKeepRealProgressAndDistinctColors` koristi uzorak stanja **samo u memoriji testne aktivnosti**, vraća prijašnje vrijednosti nakon provjere, provjerava tri osnovne boje ikona i da broj osvojenih postignuća ostane nula.
- Validator emulatora zahtijeva stvarnu snimku `missions-illustrated-six-941x1672.png`. Postojeća snimka `missions-six-real-progress-941x1672.png` ostaje obvezna za usporedbu.

## Gate prije proglašenja 1:1

1. Uspješan `Android pull request validation` (Java/Gradle, lint, unit testovi).
2. Uspješan Android 16 edge-to-edge/insets QA.
3. Uspješan instrumentalni emulator QA s novom stvarnom snimkom 941×1672, plus 360×600 i 1080×2400 bez odsijecanja sadržaja.
4. Ponoviti sve gornje ROI vrijednosti. Ako neka konkretna ilustracija pogorša podudarnost, promijeniti je ili vratiti bez utjecaja na ostali prikaz.
5. Obavezno ponovno provjeriti ispravnu dodjelu nagrada i brojač postignuća.
6. **Ne tvrditi da je pixel-perfect 1:1** ako zadnja izmjerena snimka i dalje pokazuje razlike; PR ostaje Draft.

**Sljedeći zaslon po redu: 6/10 — Statistika.**
