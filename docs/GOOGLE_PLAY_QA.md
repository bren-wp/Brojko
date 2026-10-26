# Brojko — Google Play i vizualna QA provjera (26. 10. 2026.)

## Zahtjevi za Android i Google Play

- `applicationId=com.brendigo.brojko` ostaje nepromijenjen zbog postojećih instalacija i identiteta aplikacije.
- `minSdk=24` (Android 7.0) za Google Play Automatic Protection na temelju upozorenja na računu. Android 6.0 / API 23 više nije podržan.
- `targetSdk=36` (Android 16) za mobilne nove aplikacije i ažuriranja od 31. kolovoza 2026. Prema službenim pravilima: https://support.google.com/googleplay/android-developer/answer/11926878?hl=hr
- Release Gradle zadatak mora proizvesti APK i AAB. `scripts/verify-play-sdk.sh` provjerava **kompajlirani manifest APK-ova** preko `aapt dump badging`, odnosno paket, min API >= 24 i target API >= 36, i blokira izdanje ako provjera padne.
- `.github/workflows/android-pr-validation.yml` na PR-u radi testiranje, lint i izgradnju debug/instrumentacijskih APK-ova bez tajnih signing vrijednosti. To **nije** zamjena za emulator testove; postojeći `android-ui-qa.yml` izvodi stvarni instrumentacijski QA nakon uspješnoga Build and release workflowa.
- Android launcher koristi native adaptive foreground, round i monochrome XML slojeve. Referenca: https://developer.android.com/develop/ui/compose/system/icon_design_adaptive. Google Play listing ikona (512×512 PNG, bez prozirnosti prema smjernicama), feature graphic, screenshotovi, točni opisi i Data Safety obrazac moraju se provjeriti **zasebno u Play Console**. Te kontrole se ne mogu označiti dovršenima samo na temelju koda.

## Upload i potpisivanje

- **Ne uključivati privatni ključ, upload keystore ni zaporke u Git.**
- Signed AAB je dostupan samo ako je CI/gradnja konfigurirana s ispravnim **postojećim** Play upload ključem. Ispravan minSdk i uspješni testovi ne dokazuju valjan potpis.
- Google Play upravlja objavljenim **versionCode** vrijednostima: prije uploadanja provjeriti da novi kod verzije nije već iskorišten. Ne pokušavati prepisati postojeći objavljeni bundle istim brojem.
- Testirati potpuni korisnički tok, Play App Signing certifikat i stvarnu Play Console provjeru nakon uploadanja.

## Vizualni QA 1:1

Korisničke reference imaju osnovni viewport 941×1672. Nastaviti usporedbu stvarnih emulator PNG-ova s istim podacima, rezolucijom, temom i skalom. Različiti omjeri stranica zahtijevaju responzivne rasporede; matematička istovjetnost svih piksela na svakoj rezoluciji nije moguća bez neželjenoga odrezivanja ili rastezanja.

### Ispravci u PR #9

- Za `home` na 941×1672 uklonjeno je lažno dodatno skrolanje: posljednje četiri kartice završavaju oko y=690, pa je logička visina sadržaja smanjena s 880 na 710. Na 360×600 ostaje stvarno skrolanje radi dostupnosti svih kartica.
- Dulji opisi četiriju donjih početnih kartica sada zadržavaju zadanu veličinu fonta, a horizontalni razmak teksta prilagodi se samo kada bi prešao širinu kartice. Ostali ekran i pravila igre nisu mijenjani.
- Novi emulator regresijski test potvrđuje položaj i dohvatljivost donjih prečaca na 941×1672 i 360×600, te package/minSdk/targetSdk iz stvarne instalirane Android aplikacije.
- Sva sedam retka Postavki, tri teme, statistika, misije, dnevni izazov i modal pobjede ostaju pokriveni prethodnim instrumentacijskim render-testovima. **Takvi testovi ne dokazuju potpunu 1:1 identičnost** s 3D marketinškim ilustracijama.

### Funkcionalnost i transparentnost

`Sinkronizacija napretka` u Postavkama vodi na **lokalni izvoz/uvoz**; ne postoji cloud račun ni automatski sync. Ne uključivati lažni aktivni prekidač niti navoditi da se podaci automatski sinkroniziraju; stvarni UI mora jasno opisivati ovu razliku. U opisima na Google Playu također ne oglašavati funkcije koje aplikacija ne sadrži.
