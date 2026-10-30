# Faza 10/10 — Brending i launcher ikone · 30. listopada 2026.

## Referenca, postojeće stanje i granice mjerenja

- Vlasnikova marketinška referenca: `Slika ChatGPT 24. lis 2026. 22_15_05-1(20261030-222617).png` (1448 × 1086 px). U gornjem lijevom dijelu nalazi se velika ikona s četiri 3D pločice **2, 4, 8, 16**, koraljnim vanjskim rubom i središnjim velikim bijelim **B**. U blizini su primjeri alternativne svijetle/tamne ikone, logo, paleta i Nunito tipografija.
- Stvarna Android emulator ikona: `launcher-four-tiles-B-reference-512.png`, zajedno s `launcher-adaptive.png`, `launcher-round.png` i `launcher-android13-monochrome.png`, iz artefakta `Brojko-real-device-UI` u ranijem uspješnom QA [#41130372871](https://github.com/bren-wp/Brojko/actions/runs/41130372871).
- Izvorni marketing-list **nije** pojedinačni 512×512 PNG launchera. Izravni RGB MAE nije usporediv: izrezivanje, promjena veličine i Android adaptive maska promijenili bi izvornu ilustraciju. Ne izmišljati brojke niti proglasiti 1:1.

## Vizualni nedostatci pronađeni u projektu

1. `drawable/ic_brojko.xml` ima četiri zaobljene pločice, dok stariji `mipmap-anydpi/ic_launcher.xml` i `ic_launcher_round.xml` sadrže povijesnu izvedbu s ravnijim pločicama i manjim B. To se posebno vidi na uređajima API 24/25.
2. Postojeći Android 26+ adaptive foreground koristi faktor 0.75 unutar 108×108 viewporta i približava bojene uglove i brojeve kružnom izrezu. Masku kontrolira Android, ne treba je imitirati pozadinskom statičkom PNG slikom.
3. Standardna i okrugla launcher varijanta ne smiju odstupiti u brendu, dok Android 13+ monochrome prikaz mora ostati jednobojan i prepustiti tint sustavu.

## Implementacija u istom otvorenom PR #28

- Oba legacy launcher XML-a zamijenjena su **identičnim** kanonskim `ic_brojko.xml`, uključujući iste individualno zaobljene 2/4/8/16 pločice i veliku svijetlu B. Ne mijenja se identitet aplikacije ni paket `com.brendigo.brojko`.
- `ic_brojko_foreground.xml` skaliran je oko centra (54,54) faktorom **0.69**, čime je smanjen rizik rezanja brojeva na kružnim i drugim Android launcher maskama. Nema rasterskih ikona ni vanjskih asseta.
- Monochrome vektor nije obojen brand bojama, ostaje kompatibilan sa sustavnim ikonama Androida 13+.
- Novi instrumentacijski test `phaseTenAdaptiveIconKeepsFourTilesInsideSafeArtwork` crta stvarni Android foreground VectorDrawable na 512×512 i provjerava boje sva četiri kvadranta, veliki svijetli B te slobodni vanjski rub oko bojenih pločica. Spremanje stvarne snimke: `phase10-adaptive-icon-safe-512.png`.
- `scripts/verify-emulator-screenshots.py` zahtijeva postojanje i ispravne dimenzije tog artefakta, povrh prethodnih adaptive/round/themed snimki.
- Novi `scripts/verify-brand-assets.py` parsira Android vektorske resurse i obvezno provjerava istu geometriju legacy izvora, sigurni faktor foregrounda, boje, B, oba Android 26+ adaptive XML-a i jednobojno temiranje. Poziva se u `android-pr-validation.yml`.
- `docs/BRAND_GUIDE.md` ažuriran je i izbačena je zastarjela napomena o `sans-serif-rounded` fontu (projekt već koristi lokalno pripremljen Nunito Variable).

## Završni QA prije prihvata

1. Na **trenutačnom zadnjem HEAD-u** pričekati potpuni rezultat Android PR validation, Android 16 insets i emulator UI screenshot QA. Ako dođe do pada, čitati točan log i ispraviti uzrok.
2. Iz `Brojko-real-device-UI` izdvojiti stvarne adaptive, round, themed i novu `phase10-adaptive-icon-safe-512.png` snimku. Vizualno provjeriti nijanse, bjelinu B, 2/4/8/16 i masku na maloj veličini launchera; uočena odstupanja dokumentirati.
3. Provjeriti API 24/25 vector fallback, Android 26+ adaptive i Android 13+ monochrome. Ne izmišljati alternativno podesivu ikonu ako aplikacija tu mogućnost ne implementira.
4. Testirati da screenshotovi svih prethodnih ekrana i funkcionalna stanja (Splash, Home, igra 4×4/5×5, rezultat, Pause, Win/Game Over, Dnevni, Misije, Statistika, Teme, Postavke) nisu narušeni.
5. PR ostaje **Draft**; bez mergea ili novog izdanja dok obvezne provjere i usporedbe ne prođu. Nije dokazana potpuna podudarnost s 3D ilustracijom **piksel po piksel**.

## Trenutačni status

Kod i testovi spremljeni u istoj grani. Posljednji emulator screenshot i results na finalnom commit SHA-u **nisu još provjereni**. Faza 10 je implementacijski prolaz, a ne proglašenje finalne kvalitete.

**Sljedeći korak:** konačna regresijska/pixel QA provjera svih deset faza, čišćenje eventualnih padova i tek zatim razmatranje spremnosti za izdanje.
