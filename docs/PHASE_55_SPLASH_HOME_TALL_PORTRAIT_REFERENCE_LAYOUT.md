# BROJKO — samo dva ekrana: vjerna kompozicija na visokim i malim telefonima

Datum: 7. studenoga 2026.

## Izvor i potvrđeni rezultati

Vlasnikova dva nova PNG-a, **941 × 1672 px**, koriste se samo za usporedbu:
- Splash: `Slika ChatGPT 7. stu 2026. 21_49_54(1).png`
- Home: `Slika ChatGPT 7. stu 2026. 21_49_49(1).png`

Stvarni Android emulator iz [PR #71 UI QA](https://github.com/bren-wp/Brojko/actions/runs/42415139180), koji je prošao, ima isti broj piksela i stvarnu nativnu 3D Canvas grafiku.

| ROI | PR #70 (MAE) | PR #71 (MAE) |
|---|---:|---:|
| Splash cijeli | 12,044 | **11,273** |
| Splash plava 8 | 29,734 | **23,534** |
| Home cijeli | 14,071 | **13,926** |
| Home tri načina igre | 14,354 | **13,874** |

MAE je srednja apsolutna RGB pogreška po kanalu (0–255); to **nije** postotak, a posebno nije dokaz 0 % odstupanja.

## Utvrđen problem na visokim telefonima

Prethodna izravna emulatorska snimka na 360×800 (omjer 20:9) koristila je Splash u 880 dizajnerskih jedinica s različitim položajem i oblikom logotipa, udaljenim dekoracijama i oštrom ljubičastom pločicom 256. Home je rabio drugi kompozicijski raspored (`scoresY=269`, `playY=372` itd.), iako korisnik traži isto vlasnikovo crtanje na svim veličinama. Referentnih 941×1672 koristi kraću izvornu kompoziciju u logičkih 400×710.

## Lokalna implementacija

- Samo za `page=splash` i `page=home`, visoki portret (omjer >=1,85) koristi **istu referentnu 400×710 kompoziciju** i originalni nativni vektorski BROJKO logotip.
- Skala ostaje `usableWidth/400`, bez deformiranja X/Y omjera. Ako ima dodatne visine, sadržaj se centrira: `offY += max(0, (usableHeight - 710*scale)/2)`.
- Referentna slika na **941×1672** je nedirnuta (`offY=0`, svi postojeći izvori, kartice, rezultati i geometrija ostaju jednaki).
- Kraći portreti zadržavaju Home skrolanje do donjih kartica i zasebno ograničenje Splash skale da slogan ne izađe iz zaslona. Landscape zadržava svoj odgovarajući raspored.
- Nijedna druga stranica, igriva ploča, lokalna statistika, tema, način igre, sustav dodira ni gameplay nisu promijenjeni.

## Regresijska pokrivenost

Nova klasa `SplashHomePortraitSourceResponsiveTest` testira:
- Referentni 941×1672 i visoke portrete 360×800, 393×873, 1080×2400 i 1440×3200.
- Potpuno očuvani nativni path logotipa, ispravno skaliranje i okomiti pomak.
- Svih osam aktivnih Home odredišta, njihove vidljive dodirne granice i Splash bez skrolanja.
- Male portrete 320×480, 360×600 i 360×640 uz dostupnost posljednje Home kartice pomoću skrolanja.

Postojeći `ScreensSmokeTest` dodatno ima 17-veličinsku Splash/Home matricu, dark-mode/inset/touch QA i zaseban test neprijeklopljenih akcijskih područja drugih zaslona. Nova promjena njih ne smije pokvariti.

## Prije spajanja

- [ ] Build, lint i unit testovi na HEAD-u.
- [ ] Android emulator instrumentacijski testovi na HEAD-u.
- [ ] Android 16 edge-to-edge/safe area provjera na HEAD-u.
- [ ] Preuzeti nove stvarne PNG-ove i provjeriti 941×1672 (bez regresije) te 360×800 i 1080×2400.
- [ ] Potvrditi da se na 320×480 donjim karticama može pristupiti skrolanjem, a da Splash nikad ne odsijeca slogan.
- [ ] Izračunati RGB MAE samo za novu 941×1672 snimku prema izvorniku i ne tvrditi idealnih 0 % bez stvarnih podataka.

Ne koristiti reference kao statične pozadine aplikacije. **Ovim se poboljšava proporcionalnost, ne dokazuje pikselna jednakost svih uređaja.**
