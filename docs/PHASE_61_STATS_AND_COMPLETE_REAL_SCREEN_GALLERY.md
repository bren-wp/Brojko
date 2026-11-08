# BROJKO — faza 61: Statistika i potpuna galerija stvarnih ekrana

Datum: 8. studenoga 2026. Polazište: grana `main`, merge PR-a #79 (`3009a66d`).

## Izvor, usporediva konfiguracija i metoda

Izvorna vizualna mjerila su korisnikove priložene slike, veličine **941 × 1672 px**. Aktualne snimke dolaze iz uspješne Android UI instrumentacije [42429641571](https://github.com/bren-wp/Brojko/actions/runs/42429641571), artefakt **Brojko-real-device-UI** (ID `13596633266`), na stvarnom Android Canvas rendereru.

Svaki RGB MAE je prosjek apsolutnih razlika po sva tri kanala, na skali **0–255**, pri identičnim rezolucijama bez skaliranja, premještanja ili umjetnog poravnanja. **MAE nije postotak podudarnosti.** Usporedba ne smije podmetati fiksne rezultate, datum, broj misija ni pomoćna stanja u produkcijski UI.

| Referentni ekran | RGB MAE | Najveći lokalni prioritet |
|---|---:|---|
| Misije i postignuća | 18,866 | Podloge ilustracija i dio dnevnih redova |
| Teme i izgled | 17,636 | Naslov 24,575; birač tema 23,193; opcije 21,941 |
| Statistika | 17,486 | **Naslov 40,018**; gornji KPI 25,366; grafikon 20,119 |
| Postavke | 15,760 | Ikone, prekidači i tipografija |
| Klasično 4×4 gameplay | 14,688 | **Donje kontrole 20,019**; HUD 13,847; ploča 13,817 |
| Dnevni izazov | 14,622 | Dvoredni naslov i nagrada |
| Bravo / Pobjeda | 14,335 | 3D naslov, kruna i gumbi |
| Home | 13,926 | Ilustrirani načini i gumbi |
| Splash | 11,273 | 3D pločice i optičke konture |

Nakon PR-a #79 na Misijama: globalni MAE **21,239→18,866**, tjedni dio **25,035→19,590**, postignuća **21,719→18,733**. Na posljednjoj misiji postoji lokalno dodatno odstupanje ilustracije; ono se ne smije zanemariti zbog boljeg prosjeka.

## Lokalni zahvat u ovoj fazi — statistički naslov

U izvoru tamnoplava slova naslova Statistika imaju približne granice **x238–714, y184–285**, dok stvarni Android render daje **x238–713, y186–283** i malo tanju tiskovnu masu. Zato je promijenjen samo render naslova u svijetloj nativnoj 941×1672 kompoziciji: lokalni Y-omjer i debljina ispune/obruba. Nisu mijenjani statistički podaci, grafikon, povijest, datumi, touch pravokutnici ni druga tema.

**Ovo je hipoteza za stvarno testiranje, a ne potvrđeni dobitak.** Usporediti `golden-state-stats-941x1672.png` s izvornikom prije spajanja. Posebno izmjeriti naslov x210–735/y160–295, cijeli ekran, prva tri KPI-ja i grafikon. Vratiti izmjenu ako se bilo koji relevantan ROI pogorša.

## README — prava galerija iz aplikacije

Prijašnja galerija sadržavala je 12 emulatorskih PNG-ova. Ova faza proširuje staging i izdanje na **28 stvarnih snimki**: Splash, Home, 4×4, 5×5, aktivni Classic/Relaxed/Daily gameplay, Dnevni izazov, Misije, Statistika, povijest detaljno, Teme, tamni/živopisni gameplay, Postavke, Jezik, Pauza, potvrda nove/završene igre, Pobjeda, Game Over, dnevni uspjeh/neuspjeh, Pomoć, Privatnost, Backup, About i ikona.

Svaka datoteka ima deterministički izvorni naziv u `scripts/stage-marketing-screenshots.py` i provjeru PNG zaglavlja/rezolucije. Svih 28 izvora izravno je potvrđeno unutar stvarnog artefakta iz runa `42429641571`, bez stvaranja novih slika. CI u PR-u sada provjerava da je svih 28 dostupno prije spajanja, a galeria se nakon uspješnog main-release emulatora objavljuje kao asseti istog GitHub izdanja.

## Regresijski uvjeti

1. `Android pull request validation` (build, lint, unit, brand assets, README gallery).
2. `Android 16 navigation bar and inset QA`.
3. `Android UI smoke and screenshot QA`, 169+ instrumentalnih provjera i svih 28 snimki.
4. Vizualni audit na izvornom 941×1672 ekranu, bez rastezanja; funkcionalni i responzivni testovi na manjim/većim zaslonima.
5. Tek po potvrdi objaviti novi release APK/AAB; ne tvrditi da su release datoteke potpisane ako ne postoji valjana konfiguracija potpisivanja.

Nijedna izvorna referentna slika niti emulatorski screenshot ne smije biti zamjena za nativni interaktivni UI ili ugrađena u APK kao statična podloga. **Cilj je 1:1, ali 0% odstupanja nije dokazano.**
