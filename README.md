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
| **Opušteno 5×5** — više prostora | Ciljevi: pločica 256, 3.000 bodova i najviše 80 poteza | Misije sa stvarnim nagradama u zvjezdicama, 250 zvjezdica za dnevni izazov, statistika i niz uspješnih dana |
| Brze kontrole povlačenjem | Poništavanje poteza | Tri teme i tri stila pločica |

## ✨ Zbog čega igrati Brojko?

- 🎨 **Prepoznatljive boje:** koraljna, tirkizna, plava, žuta i ljubičasta; reljefne pločice s gradijentima i sjajnim rubovima.
- ⚡ **Bez čekanja:** igra je lokalna i ne treba internet ni prijavu.
- 🎯 **Ciljevi koji imaju smisla:** dnevni zadaci i postignuća s jednokratnim nagradama u zvjezdicama, bez kupnji u aplikaciji.
- 💾 **Sigurnosne kopije:** lokalni JSON izvoz i uvoz preko Androidova odabira datoteka.\n- 📈 **Statistika:** odigrane partije, bodovi, postignute najveće pločice i nedavne partije.
- 🔊 **Ugodno igranje:** podesivi zvuk, vibracija, animacije i opcionalni lokalni podsjetnik na dnevni izazov.
- 🔒 **Privatnost:** nema oglasa, analitike, praćenja ni mrežnih SDK-ova u ovoj verziji.

## 🧭 Ekrani

Brendirani splash · Početni izbornik · Kako igrati · Igra 4×4 · Igra 5×5 · Pauza · Pobjeda · Kraj igre · Dnevni izazov · Misije · Statistika · Teme i izgled · Postavke · Privatnost · Sigurnosna kopija · O igri.

Boje i sastav temelje se na dizajnerskim referencama **Brojko by Brendigo**. Vizualna preciznost 1:1 zahtijeva dodatnu provjeru stvarnih uređaja i prilagodbu; sam build nije dokaz pikselne identičnosti.

## ⭐ Zvjezdice i misije

Zvjezdice su lokalni pokazatelj ostvarenog napretka; nisu valuta, ne mogu se kupiti, prenijeti niti zamijeniti za stvarne proizvode. Dnevni izazov završava uspjehom ako se do 80. poteza dosegnu 256 i 3.000 bodova; u suprotnom nastupa kraj izazova. Za prvo uspješno rješenje toga dana dodjeljuje se 250 zvjezdica. Dnevne nagrade mogu se osvojiti jednom dnevno, a nagrade za niz dana i postignuća samo jednom. Napredak i zvjezdice spremaju se lokalno i uključuju u JSON sigurnosnu kopiju.

## 🎨 Vizualni identitet

Dizajn prati korisničkih deset referentnih slika: reljefne pločice, originalne boje, vektorske ikone, snažno zaobljeni gumbi i prikazi statistike. Potpunu usporedbu i preostale razlike dokumentira [vizualni audit](docs/VISUAL_AUDIT.md).

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

## Vizualni QA — v1.5

Zasloni su ponovno usklađivani prema referentnom rasporedu: brendirana zaglavlja, igra 4×4/5×5, tri gumba na ekranu pobjede, dnevni reward panel, misije, detalji povijesti, statistički grafikon, pregled tema sa stvarnim prekidačima i skrolanje na manjim uređajima. CI snima te ekrane u zasebnom Android UI workflowu. Precizna pixel-to-pixel jednakost još mora biti potvrđena nakon vizualnog pregleda snimki; referentni marketing prikazi nisu isto što i stvarni gameplay početni state.

## Usklađivanje v1.6

Nadograđeni su dubina kartica i reljef pločica, zaobljeni naslovi, dvobojna zaglavlja, ikona za novi početak i odzivnost igraće ploče na nižim Android ekranima. Instrumentacijski QA uključuje kontrolu gesta pri 360×600 i kompletne snimke svih glavnih zaslona.

Izdavanje: GitHub Actions automatski objavljuje testni APK i AAB; za Play Store produkciju nužan je valjan produkcijski upload potpis.


### Font i reproducibilna lokalna izgradnja

Izvorni dizajn koristi Nunito. Prije Gradle izgradnje pokrenuti `bash scripts/prepare-nunito.sh` kako bi se preuzeo i kriptografski provjerio licencirani font (SIL OFL 1.1), uključen unutar APK/AAB paketa. Aplikaciji za rad nije potreban internet. GitHub Actions to izvršava automatski.

**v1.7.24:** zaslon tema sada uključuje vidljivu donju navigaciju, obrise odabranih tema i stilova pločica te uklanja nepotrebni dodatni gumb kako bi ostao vjeran referentnom prikazu.

## UI/UX provjera v1.8

Zaslon **Teme i izgled** prilagođava cijeli sadržaj, uključujući tekst donje navigacije, stvarno dostupnoj visini prozora unutar Androidovih sistemskih rubova na višim telefonima. Na kraćim zaslonima ostaje omogućen scroll. U Postavkama je odabir jezika prikazan u zaobljenoj kartici s ikonom otvaranja, kao na dizajnerskoj referenci. Android UI test provjerava da kartica jezika ima dovoljno veliku dodirnu zonu te da dno navigacijskog elementa ne prelazi stvarni vidljivi prostor. Dodatne razlike u odnosu na referentne ilustracije i dalje se dokumentiraju i ne predstavljaju dokaz apsolutne 1:1 jednakosti.

## Pixel alignment: v1.9

Usklađivanje je provjereno izravno prema korisnikovim deset referentnih prikaza (9 zaslona + brand board). Za telefonski omjer blizak 16:9, klasična/opuštena ploča pomaknuta je više kako bi sva 4×4 / 5×5 polja i tri akcijska gumba ostali unutar istog zaslona, bez smanjivanja pločica. Na dugim telefonima ostaju dodatne naredbe za izbornik i dijeljenje. Android instrumentacijski test sada snima ključne ekrane pri **941×1672**, identično referentnim slikama (isključujući Android status/navigation bar prikaz), te provjerava da su gumbi Novo i Završi unutar dostupne površine.

Adaptive ikona dodatno ispunjava masku launchera; dominantno svijetlo slovo B dobilo je tamniju donju dubinsku sjenu. Navedene vizualne provjere potvrđuju raspored i funkcionalnost, ali ne dokazuju identičnost svih piksela bez fiksnih testnih podataka i zasebnog slikovnog diffa.

**Dodatni v1.9 polish:** referentni 16:9 viewport prikazuje svih sedam redaka postavki i završnu logo karticu bez skrivanja; modal pobjede ima sva tri funkcionalna gumba unutar prikaza. To se provjerava automatskim Android testovima uz snimanje referentne rezolucije.

**v1.9 UI polish:** Početni zaslon (oba rezultata, primarni gumb, tri igre i četiri prečaca), dnevni izazov (ploča, svi ciljevi, nagrada i gumb) te svih pet misija imaju poseban raspored za referentni omjer 941×1672. Kartice nisu rastezane vertikalno: promijenjeni su razmaci i stvarne pozicije elemenata. QA provjerava dostupnost prečaca i glavne radnje.

**Kompaktne statistike i teme:** na uskim ekranima 16:9 sve metričke kartice, grafikon i pregled povijesti te sve opcije tema, kontrole zvuka/vibracije, animacije i donji tabovi raspoređeni su unutar vidljivog kadra bez vertikalnog izobličenja teksta. Na visokim telefonima zadržan je prostran dizajn.

## Vizualni polish v1.10

Dnevni izazov sad ima kompaktan, funkcionalan kartični prikaz nagrade s originalno stiliziranom vektorskom ljubičastom poklon-kutijom i zlatnom vrpcom. Sadržaj se temelji na stvarnom datumu, rekordu, stanju ciljeva i osvojenoj dnevnoj nagradi. Tri zadatka, nagrada i gumb ostaju unutar referentne visine 941×1672. Misije i postignuća imaju pastelno odvojene dnevne, tjedne i trajne cjeline te semantičke ikone zadataka (pločice, meta, kalendar, kruna) umjesto jedne generičke ikone na svim redovima. Svjetla, tamna i živopisna tema snimaju se u Android UI QA za početnu, igru, postavke, dnevni izazov, misije, statistiku i teme. Izvorne ilustracije ostaju vizualni cilj; točna pikselna identičnost nije potvrđena.
