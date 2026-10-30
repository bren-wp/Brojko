# Brojko — Postavke: povratak i čitljiv Splash (31.10.2026.)

## Prijavljeni problemi
1. U zaslonu **Postavke** korisnik nema vidljivu ikonu za povratak, iako Android sistemska gesta radi. `settings()` za razliku od ostalih detaljnih zaslona nije pozivao već implementirani `back()`.
2. **Splash** se automatski zatvara za `1350` milisekundi. U stvarnoj aplikaciji animirani logo i dekoracije nestanu prije nego što se mogu vidjeti.

## Izmjene na `main` nakon spojenog PR #28
- `settings()` počinje s `back()`, čime se prikazuje originalni vektorski povratni gumb u gornjem lijevom kutu i pristupačna dodirna zona najmanje **54 × 54** logičkih jedinica. Pritiskom vodi na `home`, dok Android sistemski Back i dalje koristi postojeći `handleBackNavigation()`.
- Lijevi koraljni ukras u naslovu Postavki odmaknut je od touch površine strelice kako se ne bi preklapao s kontrolom.
- Trajanje početnog brend prikaza sada je **3200 ms (3,2 sekunde)**, definirano konstantom `MainActivity.SPLASH_DURATION_MS`. Nema izmjena slike, logotipa, rasporeda ili gameplaya. Ako korisnik već navigira sa Splash zaslona, postojeći callback neće prebaciti drugu stranicu na Home.
- Postojeći instrumentacijski testovi Postavki prilagođeni su **osam** stvarnih touch zona (jedna strelica + sedam kartica), bez brisanja ijednog testa interakcije ili spremanja postavki.
- `settingsBackArrowIsVisibleAccessibleAndNavigatesHome`: provjera 3 viewporta (941×1672, 360×600, 1080×2400), svijetle/tamne teme, touch područja, vizualne pozicije i oba načina povratka. Snimka `settings-back-arrow-941x1672.png` iz stvarnog Android renderera obvezna je u screenshot CI-ju.
- `splashBrandRemainsVisibleForAtLeastThreeSeconds`: provjerava da konfiguracija traje od 3000 do 4500 ms, bez `sleep` poziva koji bi uzrokovali nestabilnost emulator testova.

## QA i isporuka
Povezana polazna implementacija je **već spojeni** [PR #28](https://github.com/bren-wp/Brojko/pull/28); ovaj popravak zato ima **novu granu i novi Draft PR**. Nijedna ranija grana niti spremanje partija nije resetirano.

Prije mergea provjeriti sva tri workflowa za **zadnji HEAD**: Gradle + lint + unit + APK/AAB, Android 16 insets i stvarne screenshot/emulator testove. Postavke moraju pokazivati strelicu na svim navedenim uređajima, a Splash trajati oko 3,2 sekunde od prikaza početnog zaslona. Ne tvrditi 1:1 bez ponovnog mjerenja originalnog PNG-a sa snimkom zadnjeg SHA-a. Nema automatskog objavljivanja izdanja.
