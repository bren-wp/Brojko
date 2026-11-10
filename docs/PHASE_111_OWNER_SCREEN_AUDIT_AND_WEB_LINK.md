# BROJKO — faza 111: stvarni audit svih dostavljenih referenci i web povezivosti

**Datum:** 10. studenoga 2026.  
**Osnova:** `main` nakon spajanja PR-a #110 (`a2fdce4a`), izdanje [v1.12.196](https://github.com/bren-wp/Brojko/releases/tag/v1.12.196).  
**Emulatorski izvor:** uspješan [Android UI smoke and screenshot QA #42740986386](https://github.com/bren-wp/Brojko/actions/runs/42740986386), artefakt `Brojko-real-device-UI` #13729896718, stvarni Android Canvas 941×1672.  
**Izvornici:** devet vlasnikovih priloženih izvornih PNG-ova, `06-2`, `13-3`, `13-4`, `14-5`, `15-6`, `16-7`, `17-8`, `18-9`, `19-10` (svaki 941×1672). Deseta priložena grafika `05-1` je **branding/marketing ploča 1448×1086**, a ne zaseban Android ekran: za nju treba zasebno testirati launcher, adaptive icon i logo, a ne rastezati je u portretni screenshot.

## Rezultati: prije ove PR izmjene

Sljedeće su **stvarno izmjerene** vrijednosti na identičnoj 941×1672 rezoluciji, bez automatskog poravnavanja, rastezanja, retuširanja ili pomicanja. RGB MAE je srednja apsolutna pogreška kanala RGB u rasponu 0–255; nije postotak vjernosti. `Točno jednaki` znači da su sve tri komponente RGB pojedinog piksela jednako zapisane, bez tolerancije. Različiti datumi i stvarni rezultati igre također mijenjaju piksele.

| Zaslon | RGB MAE / 255 | Točno jednaki RGB pikseli |
|---|---:|---:|
| Splash | 10,761 | 6,061 % |
| Početni izbornik | 14,642 | 3,503 % |
| Klasično 4×4 | 13,680 | 2,641 % |
| Pobjeda — Bravo | 14,335 | 0,537 % |
| Dnevni izazov | 14,619 | 3,285 % |
| Misije i postignuća | **17,781** | 1,320 % |
| Statistika | **16,170** | 0,772 % |
| Teme i izgled | **16,115** | 4,342 % |
| Postavke | 13,108 | 3,425 % |

**Nije postignut 1:1 ni na jednom od ovih zaslona.** Postotak identičnih RGB piksela nije zamjena za vizualnu ocjenu, jer mala razlika u osvjetljenju/antialiasingu mijenja brojne piksele.

### Najveći lokalni prioriteti

- **Misije:** tekst/progress regija x225–735/y350–1620 **MAE 21,89**; tjedni dio 20,61; ilustracije 19,75. Referenca pokazuje `4/20`, a aplikacija ima samo dvije stvarno implementirane trajne misije i zato prikazuje `0/2`. Ne smije se izmišljati `4/20` radi snimke; 20 pravih postignuća zahtijeva zasebnu implementaciju i migraciju stanja.
- **Teme:** opcije x465–900/y940–1385 **MAE 23,27**, izbor tema 23,08. Sačuvati tri žive teme, tri stila pločica, postojeće postavke i stvarne dodirne zone.
- **Statistika:** grafikon x22–922/y765–1160 **MAE 20,21**, gornje KPI kartice 19,47. Prikazane vrijednosti moraju ostati stvarni rezultati iz povijesti; referentni graf ima drukčije datume/redoslijed od testa, pa statična imitacija nije dopuštena.
- **Postavke:** red za sinkronizaciju/lokalnu kopiju x32–905/y828–998 **MAE 26,55**, jer ilustracija prikazuje aktivnu cloud sinkronizaciju, koja u trenutačnom projektu nije implementirana. Ne treba uključivati lažnu sklopku.

## Izmjene u fazi 111

1. **O igri → web:** prethodno statičan niz `brendigo.com` dobiva funkcionalan gumb `Posjeti Brendigo web`, fiksni `https://brendigo.com/` i vanjski preglednik putem korisnički aktiviranog Android `ACTION_VIEW` intenta. Android aplikacija ne prenosi napredak niti uvodi `INTERNET` dozvolu; potvrdu URL-a pokriva instrumentacijski test.
2. **Pixel QA:** skripta `tools/visual_pixel_audit.py` sada uz MAE mjeri i točan broj različitih RGB piksela. `--require-exact` opcionalno odbija čak i jedan različit piksel, a `--baseline-artifact` s `--max-mae-increase` omogućuje odustajanje od regresija na cijelom zaslonu i opcionalnim ROI regijama.
3. **CI:** dodat je sintaktički i sintetički regression test pixel-audit skripte. Bez vlasnikovih privatnih referenci u CI-u se ne smije tvrdnjom zamijeniti stvarna usporedba; lokalni ručni audit i dalje je obvezan za svaku buduću vizualnu izmjenu.

## Ponovljiva provjera (lokalno s vlasnikovim originalima)

```bash
python3 -m pip install 'Pillow>=11.3,<13'
python3 tools/visual_pixel_audit.py \
  --reference-dir /putanja/do/izvornih/png \
  --artifact Brojko-real-device-UI.zip \
  --include-regions \
  --markdown audit.md
```

Usporedba nakon nove izmjene može koristiti `--baseline-artifact prethodni.zip --max-mae-increase 0` kako bi se odbila svaka MAE regresija, uključujući navedene regije. `--require-exact` provjerava strogu jednakost (očekivano će pasti dok god postoje razlike). Referentne slike su isključivo za QA i **nisu ugrađene kao pozadina ili vizualna zamjena za UI**.

## Web aplikacija / cloud sinkronizacija: neriješeno

Repozitorij sadrži Android aplikaciju, lokalne `SharedPreferences` i korisnički pokrenut JSON izvoz/uvoz. Nema web aplikacijskog modula, javno utvrđenog Brojko API-ja, poslužiteljskog računa, autentikacije ni sigurnog cloud sync protokola. Stranica `brendigo.com` je web autora. **Vanjska poveznica s gumba O igri nije sinkronizacija** i ne smije se tako prikazivati.

Za stvarnu Android↔web sinkronizaciju trebaju usuglašeni web/backend repozitorij, endpointi, identitet korisnika, opt-in, TLS, pravila rješavanja sukoba, brisanje podataka i revizija pravila privatnosti/Data Safety. **Ne spajati cloud sync bez te infrastrukture.** Prioritet zadržati ispravan lokalni gameplay i stvarne podatke.

## Validacija prije spajanja

- Android PR build/lint/unit, Android 16 edge-to-edge, Android UI screenshot workflow na posljednjem SHA.
- Potvrda `O igri` gumba i standardnog HTTPS browser intenta.
- Potvrda da se privatnost, dozvole, podatkovni model, igra 4×4/5×5/dnevna, statistika i backup nisu promijenili.
- Stvarnu novu 941×1672 slikovnu provjeru napraviti na novom CI artefaktu, ne na pretpostavljenom prikazu.
- Ne objavljivati tvrdnju o nultom odstupanju ili produkcijskom AAB potpisu bez provjere.
