# 🎮 Brojko — Spajaj brojeve. Ruši rekorde.

**Brojko by Brendigo** je šarena Android logička igra nadahnuta pravilima 2048. Jednostavna za naučiti, izazovna za savladati.

## ✨ Zašto Brojko?
- 🧩 Klasični način **4×4** i opušteni način **5×5**
- 🎯 **Dnevni izazov** s reproducibilnim početnim rasporedom
- 🏆 Osobni rekordi, statistika i postignuća
- ↩️ Poništavanje posljednjeg poteza
- 🎨 Tri vizualne teme i hrvatsko sučelje
- 📱 Nativni Android, rad bez interneta i bez korisničkih računa

## 📲 Instalacija i izgradnja
Projekt koristi Java Android Views i Android Gradle Plugin 8.9.2. U Android Studiju otvorite korijenski direktorij; Gradle sinkronizira ovisnosti. GitHub Actions automatski pokreće izgradnju APK-a i AAB-a. Build zahtijeva Android SDK 36 i Java 17.

**Release potpisivanje:** postavite secrets `BROJKO_KEYSTORE_B64`, `BROJKO_STORE_PASSWORD`, `BROJKO_KEY_ALIAS`, `BROJKO_KEY_PASSWORD`. Bez njih se izrade ne smiju predstavljati kao potpisani produkcijski paketi. AAB i APK se isporučuju kao CI artifacts; GitHub Releases nastaje za tag `v*`.

## 🔒 Privatnost
Brojko pohranjuje napredak lokalno na uređaju. Nema oglasa, analitike ni mrežnih zahtjeva u izvornom kodu. Prije objave provjerite Play Console Data Safety i konačne podatke o privatnosti.

## ℹ️ Status
Ovo je inicijalna implementacija; vizualni raspored koristi programatski Canvas i mora proći testiranje na različitim ekranima prije tvrdnje da je **1:1** sa svim dizajnerskim referencama. Postignuća su osnovna, a dnevni izazov koristi predodređen početni raspored.

**© Brendigo** · [brendigo.com](https://brendigo.com)
