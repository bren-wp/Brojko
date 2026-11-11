# Brojko — faza 132: izvornim pikselima približena kartica lokalne kopije

Datum: 11. studenoga 2026.

## Stanje izvornika i aktualnog projekta

- Izvorna referenca: korisnikov PNG **Postavke**, 941×1672.
- Potvrđeni emulator baseline (spojeni #129, release v1.12.211): [UI QA 42942720893](https://github.com/bren-wp/Brojko/actions/runs/42942720893), artefakt `Brojko-real-device-UI` #13820824717.
- Izvorni ROI kartice oko sinkronizacije/lokalne kopije, `x32..905/y828..998`: **RGB MAE 26,5501 / 255** prije ove faze. To nije postotak nepodudarnosti.
- PR #130 je već spojen na `main`. Njegove korekcije Stats/Victory/Themes/Classic treba sačuvati; faza 132 namjerno mijenja samo tekstualnu geometriju jedne kartice Postavki.

## Što je promijenjeno

Referenca ima kompaktan jedan naslov `Sinkronizacija napretka`, čiji je tamni raspon izmjeren na **x215..628/y899..937**. Prethodni stvarni Android emulatorski naslov bio je **x216..642/y873..909**, dakle oko **27 px previsoko** i 14 px previše udesno na desnoj strani. Produkcijska aplikacija **nema cloud servis**, već istinsku akciju `Lokalna kopija` koja vodi na korisnički izvoz/uvoz JSON datoteke. Prethodno je preširoki dvoredni naslov zauzimao više vodoravnog prostora i bio postavljen previsoko.

- U **svijetlom** izvornom portretnom 941×1672 prikazu, samo glavna tipografija lokalne kopije ograničena je na **176 logičkih jedinica** (oko 414 piksela) umjesto 218; spuštena je za **12 logičkih jedinica** (oko 28 piksela), a dopunski tekst za tri jedinice.
- Stvarni tekst `Lokalna kopija`, desna strelica i čitava aktivna kartica **ostaju**. Nije dodana lažna sklopka koja bi korisnika navela na vjerovanje da je cloud povezan.
- Isti font, zajednička grafika ikona, dark mode, stvarni podaci, igra, rezultat i ostali zasloni nisu izmijenjeni.

## Nove zaštite

`OwnerSettingsBackupPhase132Test` na stvarnom Android Canvas prikazu:
1. mjeri stvarne granice naslovnog tamnog teksta u 941×1672, prema izvornim x215..628/y899..937 s rezervom za rasterizaciju;
2. potvrđuje da je subtitle `Lokalna kopija` ispod naslova i unutar kartice;
3. provjerava jednu aktivnu dostupnu akciju unutar cijelog retka;
4. provjerava dostupnost iste lokalne kopije na 360×800 i 941×1672 u svijetloj i tamnoj temi.

## Potrebna provjera prije spajanja / izdanja

- [ ] `Android pull request validation` uspješno prolazi za posljednji SHA.
- [ ] `Android 16 navigation bar and inset QA` uspješno prolazi.
- [ ] `Android UI smoke and screenshot QA` uspješno prolazi, uključujući fazu 132.
- [ ] Ponovno preuzeti svježu izvornu 941×1672 snimku (bez resizea) i izračunati cijeli Settings i ROI `x32..905/y828..998` prema **istom** originalnom PNG-u.
- [ ] Ne prihvaćati povećanje ROI pogreške bez opravdanja i provjeriti TalkBack, male zaslone, izvoz/uvoz JSON i ponašanje tamne teme.

**Nijedan ROI niti cijeli zaslon još nije certificiran 1:1.** Točna vizualna ekvivalencija sinkronizacijske sklopke iz makete nije moguća dok ne postoji proizvodna cloud usluga, opt-in i odgovarajući backend, pa se funkcionalno istinit UI ne smije nadomjestiti lažnom sklopkom.
