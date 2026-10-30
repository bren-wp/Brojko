# Brojko — faza 15: završni CI, security i vizualni audit

Datum: **31.10.2026.** · [Otvoreni PR #28](https://github.com/bren-wp/Brojko/pull/28) · Grana `feat/pixel-victory-modal-phase4`.

## 1. Potvrđeni padovi GitHub Actions i uzrok

Izravno su pročitani stvarni logovi ovih izvođenja (commitovi prije ove faze):

- [Android PR validation #41141565271](https://github.com/bren-wp/Brojko/actions/runs/41141565271): Java compilation failure `:app:compileDebugAndroidTestJavaWithJavac`.
- [Android 16 insets #41141565118](https://github.com/bren-wp/Brojko/actions/runs/41141565118): ista Java kompilacijska pogreška, ne dokaz Android 16 UI regresije.
- [Android UI screenshot #41141239028](https://github.com/bren-wp/Brojko/actions/runs/41141239028): instrumentacija nije mogla završiti zbog iste kompilacijske pogreške.
- [Android 16 insets #41142019235](https://github.com/bren-wp/Brojko/actions/runs/41142019235): šest prijavljenih `unreported exception JSONException` na `ScreensSmokeTest.java` (linije 1091, 1094, 1217, 1279, 1541, 1607 prethodnog commita).

**Root cause:** sedam novih izraza `new org.json.JSONArray(String)` bilo je pozivano u `ActivityScenario.onActivity` lambda-testovima, koji ne mogu deklarirati checked `JSONException`. Produkcijski 2048 engine nije uzrok ovog build pada.

**Korekcija:** `ScreensSmokeTest.checkedHistory(String)` strogo parsira `JSONArray`, a neispravan JSON pretvara u `AssertionError` s izvornim uzrokom. Zamijenjeni su pozivi iz novo dodanih testova, uključujući inline `oldHistory`. **Testovi nisu uklonjeni**, niti je skriven invalidan JSON. Commit `95aefa5458898b34633237671542139602f4c1cc`.

## 2. Stvarni nalaz sigurnosnog audita lokalne sigurnosne kopije

`MainActivity.readBackupBytes` ranije je dopuštao proizvoljni `String` za ključ `best`, `stars` i ostale brojače, te proizvoljni `Integer` za string datume. Zato je JSON poput `{"best":"9999"}` mogao proći validaciju i biti trajno spremljen putem `SharedPreferences.Editor.clear().commit()`. Sljedeće `getInt("best")` pri pokretanju aplikacije moglo je izazvati `ClassCastException`.

**Ispravljeno:** prije bilo kakve promjene preferencija svi poznati ključevi validiraju se strogo prema vlastitom tipu:

- Cjelobrojne vrijednosti: sve statistike/brojači, tema, stil i jačina animacije; broj ne može biti negativan, a vrijednosti tema/stil/animacija moraju biti 0–2.
- Logičke vrijednosti: `sound`, `vibrate`, `notifications`, `undoUsedThisGame`.
- String vrijednosti: `session`, `history` i datumski ključevi; datumi imaju maksimalno 20 znakova. Postojeće detaljne kontrole veličine kopije, strukture sesije i duljine povijesti ostaju na snazi.

Uklonjena je i mrtva lokalna varijabla `expected`, koja se učitavala, a nikada nije koristila. Commit `b595c5e8bbcd2ba90d7d009e5ef85177d5fca705`.

**Novi regresijski test:** `malformedBackupTypesNeverOverwriteSavedUserData` priprema pet JSON primjeraka s neispravnim tipom polja (score, audio, datum, history i animation), provjerava njihovo odbijanje i uspoređuje cijeli `SharedPreferences.getAll()` prije i poslije svakog pokušaja. Commit `9094615dc5a0d8d586208e05d0ca5af8d42dbbe6`.

## 3. Povijesno potvrđen pixel audit osnovnih zaslona

Arhivirani stvarni Android UI artefakt `Brojko-real-device-UI` iz ranije uspješne iteracije s **73 prolazna instrumentacijska testa** i originalni vlasnikovi PNG-ovi upotrijebljeni su za mjerenje **native 941×1672 RGB MAE**. Nije korišteno pomicanje, resampling, rastezanje ni rekonstrukcija originala. Nazivi benchmark snimki u tablici su doslovne datoteke iz arhiviranog artefakta.

| Original korisnika / zaslon | Stvarni stariji emulator PNG | RGB MAE (0–255) |
|---|---|---:|
| 2 · Splash | `splash-full-width-portrait-reference-941x1672.png` | **20.770** |
| 3 · Home | `home-source-accent-rays-941x1672.png` | **22.770** |
| 4 · Classic 4×4 | `golden-state-game-941x1672.png` | **21.730** |
| 5 · Win | `golden-state-win-941x1672.png` | **19.329** |
| 6 · Daily | `daily-large-safe-primary-rays-941x1672.png` | **19.052** |
| 7 · Missions | `missions-six-real-progress-941x1672.png` | **26.186** |
| 8 · Statistics | `golden-state-stats-941x1672.png` | **25.410** |
| 9 · Themes | `themes-peach-style-swatches-941x1672.png` | **24.089** |
| 10 · Settings | `reference-settings-polished-941x1672.png` | **15.462** |

**Važno:** to je **povijesni baseline, ne rezultat ovog PR HEAD-a**. Neke snimke koriste kontrolirana testna stanja, pa se MAE ne može tumačiti kao čista mjera geometrije ako podaci nisu identični izvornim ilustracijama. Marketinški brand-sheet (#1, 1448×1086) nije usporediv s Android launcherom 512×512 bez neizbježnog izreza, skaliranja i OS maske. Nijedan izračun ne potvrđuje 1:1.

**Najveći vizualni dug:** Misije (26.186), Statistika (25.410), Teme (24.089), nakon potvrde novih CI rezultatā. Nikada ne pripisivati te brojke novom kodu dok se ne izradi i izmjeri screenshot zadnjeg SHA-a.

## 4. Preostali QA i release preflight

1. Na novom PR HEAD-u moraju proći **sva tri** workflowa: Android PR validation (Gradle 9 / Android 36, lint, unit, debug/release i AAB), Android 16 navigation/insets i Android UI emulator/screenshot.
2. Emulator mora stvarno izraditi sve prethodno zahtijevane screenshotove za 10 osnovnih zaslona + Pause, Confirm, Game Over, DailyWin/Fail; provjeriti stvarnu funkciju dodirnih zona, bez screenshot lažiranja.
3. Izračunati **novi** MAE za iste fixture/ROI parove na potpuno istim 941×1672 koordinatama, osobito misije, statistiku i teme; ispraviti uočene regresije zasebno.
4. Provjeriti restore postojećih korisničkih podataka, import valjanih i zlonamjerno pokvarenih kopija, neponavljanu dnevnu nagradu, 2048 engine i arhiviranje točno jedanput.
5. Tek kada sva tri obvezna workflowa i stvarni UI auditi prođu, razmotriti uklanjanje Draft statusa, merge i release; **ne izdavati ništa iz padajućeg PR-a**.

**Status pri pisanju:** konkretne kompilacijske pogreške ispravljene u kodu, backup importer ojačan i dodani regresijski testovi. Novi CI uspjeh / novi pixel MAE / produkcijska spremnost **nisu još potvrđeni**.
