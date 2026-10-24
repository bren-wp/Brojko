<div align="center">

# 🎮 Brojko
### Spajaj brojeve. Ruši rekorde.

**Nativna Android logička igra · by [Brendigo](https://brendigo.com)**

![Android](https://img.shields.io/badge/Android-6.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin? No](https://img.shields.io/badge/Native-Java-FF7054)
![Offline](https://img.shields.io/badge/Igra-bez%20interneta-27C7BB)
![CI](https://github.com/bren-wp/Brojko/actions/workflows/android-release.yml/badge.svg)

**Jedan potez, jedno spajanje, novi rekord.** 🧩

[📦 Preuzmi izdanje](https://github.com/bren-wp/Brojko/releases) · [🔨 Izvorni kod](https://github.com/bren-wp/Brojko) · [🔐 Privatnost](PRIVACY.md)

</div>

---

## 🔢 Spoji. Napreduj. Pobijedi.

Brojko je originalno brendirana mobilna igra spajanja jednakih brojeva inspirirana poznatim mehanikama **2048**. Povuci prstom po mreži, spoji jednake pločice, osvoji 2048 i nastavi dok možeš. Kratke partije prikladne su za nekoliko minuta opuštanja ili duže obaranje rezultata.

| 🎲 Način igre | 🔥 Izazov | 🏆 Napredak |
|---|---|---|
| **Klasično 4×4** — strateško spajanje | **Dnevni izazov** — isti početak na dan | Lokalni rekord i povijest partija |
| **Opušteno 5×5** — više prostora | Ciljevi: pločica 256, 3.000 bodova, 80 poteza | Misije, statistika i niz uspješnih dana |
| Brze kontrole povlačenjem | Poništavanje poteza | Tri teme i tri stila pločica |

## ✨ Zbog čega igrati Brojko?

- 🎨 **Prepoznatljive boje:** koraljna, tirkizna, plava, žuta i ljubičasta; reljefne pločice s gradijentima i sjajnim rubovima.
- ⚡ **Bez čekanja:** igra je lokalna i ne treba internet ni prijavu.
- 🎯 **Ciljevi koji imaju smisla:** pravi dnevni zadaci povezani s rezultatima.
- 💾 **Sigurnosne kopije:** lokalni JSON izvoz i uvoz preko Androidova odabira datoteka.\n- 📈 **Statistika:** odigrane partije, bodovi, postignute najveće pločice i nedavne partije.
- 🔊 **Ugodno igranje:** podesivi zvuk, vibracija i animacije.
- 🔒 **Privatnost:** nema oglasa, analitike, praćenja ni mrežnih SDK-ova u ovoj verziji.

## 🧭 Ekrani

Početni izbornik · Igra 4×4 · Igra 5×5 · Pauza · Pobjeda · Kraj igre · Dnevni izazov · Misije · Statistika · Teme i izgled · Postavke · Privatnost · Sigurnosna kopija · O igri.

Boje i sastav temelje se na dizajnerskim referencama **Brojko by Brendigo**. Vizualna preciznost 1:1 zahtijeva dodatnu provjeru stvarnih uređaja i prilagodbu; sam build nije dokaz pikselne identičnosti.

## 🛠️ Razvoj

- Nativni Android u **Javi**, bez internetskih ovisnosti u aplikaciji.
- Android Gradle Plugin 8.9.2; JDK 17; Gradle 8.11.1; Android SDK 36.
- `minSdk 23`; `targetSdk 36`.
- Neovisni deterministički **TileGame** engine s testovima pomicanja, spajanja, RNG-a, poništavanja i 2048.
- Spremanje partije, rezultata i postavki u privatnu lokalnu pohranu.
- CI: JVM unit tests, Android lint, emulator UI smoke test i snimke zaslona, debug APK, release APK i AAB, GitHub izdanje uz svaki uspješni build grane `main`.

### Kako izgraditi

Otvorite projekt u Android Studiju ili koristite instalirani Gradle i Android SDK:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

**Za Google Play obvezno je potpisati release AAB stabilnim privatnim upload ključem.** U GitHub Actions secrets postavite `BROJKO_KEYSTORE_B64`, `BROJKO_STORE_PASSWORD`, `BROJKO_KEY_ALIAS` i `BROJKO_KEY_PASSWORD`. Bez njih release AAB i release APK nisu potpisani za produkciju. **Debug APK je namijenjen instalacijskom testiranju**, a ne objavi. Ne spremajte privatne ključeve u repozitorij.

## 📱 Google Play

Prije objave završiti: vizualnu kontrolu na više gustoća i veličina zaslona, testiranje vraćanja igre i daily izazova, stvarni uređaj/emulator, pristupačnost, release potpisivanje, listing/screenshotove, izjave o podacima i Play Console testiranje ako je obvezno za račun. Pogledajte [PRIVACY.md](PRIVACY.md).

---

<div align="center">

**Brojko — by Brendigo** · [brendigo.com](https://brendigo.com)

© Brendigo. All rights reserved.

</div>
