# Faza 09/10 — Splash (30. listopada 2026.)

## Originalna referenca i stvarni screenshot
- Original: `Slika ChatGPT 24. lis 2026. 22_15_06-2(20261030-222615).png`, točno **941 × 1672 px**.
- Stvarni Android screenshot: `splash-full-width-portrait-reference-941x1672.png` iz javnog artefakta `Brojko-real-device-UI` u uspješnom [Android UI QA #41130372871](https://github.com/bren-wp/Brojko/actions/runs/41130372871).
- Original i screenshot uspoređeni su bez skaliranja, iskrivljenja, pomaka ili poravnanja. PNG reference se ne koristi kao neinteraktivna pozadina aplikacije.

## Izmjereni baseline (RGB MAE, od 0 do 255)
| Izvorna ROI, native 941×1672 px | Prije |
|---|---:|
| Cijeli zaslon (0,0,941,1672) | **20.770** |
| Gornje lebdeće pločice (0,95,941,700) | 20.676 |
| Pločica 2 (70,115,420,450) | 29.564 |
| Pločica 4 (530,120,875,460) | 26.871 |
| Glavni logotip (60,675,885,1060) | 28.396 |
| Potpis by Brendigo (470,930,860,1040) | 19.097 |
| Donji dekorativni oblici (0,1040,941,1672) | 18.935 |
| Slogan (50,1250,900,1470) | 11.568 |
| Donji broj 256, blur (635,1420,941,1672) | **36.387** |

Metrike pokazuju da slika prije faze 09 **nije** 1:1. Vlasnikova ilustracija sadrži organski reljef i bogatu soft-focus dubinu koju originalni native Canvas prikazuje pojednostavljeno. Statusna traka 9:41, signal i baterija pripadaju Android sistemskom UI-u; ne smiju biti lažirani crtanjem statičkog vremena.

## Izmjene u istoj otvorenoj grani PR #28

1. Uveden isključivo Splash renderer `splashFloatingTile()`: mekana topla sjena, odvojena donja ekstruzija, vertikalni sjajni gradijent, bijeli refleksni obrub i mali gornji glint.
2. Za nativni portretni prikaz poboljšane su geometrije šest lebdećih numeričkih objekata, ponajprije pomaknuta koraljna pločica 2 i smanjeni kvadrati 4/8/16, bez promjene pravih brojčanih vrijednosti ili 2048 fizike.
3. Dodani su izvorni dekorativni koraljni i tirkizni potezi oko gornjih pločica i donje pločice 64. Posebna `splashAccentStroke()` ne mijenja zajedničke ukrase drugih zaslona.
4. Uveden `splashEdgeGlowTile()`: u donjem desnom rubu udaljena plava 256 pločica s mekim sjajem i zamućenim brojem, kao na referenci. Razlikuje se od stvarne, oštre igre.
5. `splashAmbient()` koristi mekši halo za dekorativne pozadinske siluete. Slogan, HTML-safe naslov i `by Brendigo` ostaju programski tekst, bez vanjskih resursa.
6. Test `splashPhaseNineRendersOwnerIllustrationAndSoftEdgeTiles` snima `splash-phase09-owner-941x1672.png`, provjerava dekorativne koraljne zrake, donji plavi broj, nedostatak scrollanja i dostupnost slogana na 360×600.
7. Validator zahtijeva novu **stvarnu** emulator snimku, a `tools/visual_pixel_audit.py --screen Splash --screen09-splash --include-regions` provjerava puni native screenshot i osam manjih regija.

## Obvezna provjera prije finalizacije
- Sva tri workflowa na **posljednjem** SHA-u: Android PR validation (build/lint/unit), Android 16 edge-to-edge i Android UI smoke/screenshot QA.
- Nakon uspješnog screenshot QA preuzeti novi `Brojko-real-device-UI`, izračunati MAE full i po ROI na neizmijenjenom izvornom PNG-u; dokumentirati poboljšanja i lokalne regresije.
- Testirati funkciju prijelaza Splash→Home i ispravan sistemski status bar na Androidu 15/16.
- Testirati 360×600, 941×1672, 1080×2400; svi tekstovi moraju ostati unutar viewporta i bez lažnih dodirnih zona. Provjeriti da Splash izgleda jednako bez obzira na stil pločica u gameplayu.
- Provjeriti da se na drugim zaslonima nije promijenio globalni izgled, da nijedna igra ili spremljeni podaci nisu promijenjeni.
- Ostaviti PR kao Draft i ne izdavati release prije stvarnog dokaza prihvatljive vizualne i funkcionalne kvalitete.

**Status:** početni MAE potvrđen, kod i novi regresijski testovi ažurirani. Završna emulator snimka i MAE na zadnjem commit SHA-u **još nisu potvrđeni**. Nema tvrdnje o 1:1.

**Sljedeća faza:** 10/10 — pregled brendinga, ikona i preostalih UI stanja.
