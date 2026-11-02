# BROJKO — faza 30: Splash, zasićenost i reljef pločica

Datum: 2. studenoga 2026. — referentni zaslon **941 × 1672 px**.

## Izvor i metodologija

Uspoređen je izvorni vlasnikov Splash PNG `Slika ChatGPT 24. lis 2026. 22_15_06-2(20261102-201008).png` i **stvarna Android Canvas** snimka `splash-phase09-owner-941x1672.png` iz uspješnog Android UI artefakta workflowa [41369365604](https://github.com/bren-wp/Brojko/actions/runs/41369365604), ID artefakta 13109066443. Obje snimke imaju 941 × 1672 piksela; nisu rastezane, rotirane ni poravnane. U produkciju nisu unesene referentne slike ni fiktivni rezultati.

Početni **RGB MAE cijelog Splasha: 19,433 / 255**. To nije 1:1 podudaranje.

### Stvarni uzorci boja prije ove izmjene (RGB)

| Pločica / točka na izvorniku | Izvor | Stvarni Android renderer prije | Izmjerena razlika |
|---|---|---|---|
| Koraljna **2** (200,270) | 253,126,84 | 251,134,107 | Previše plava i svijetla |
| Tirkizna **4** (600,280) | 58,226,218 | 63,197,184 | Nedostaje zasićenost G/B |
| Plava **8** (140,600) | 45,148,249 | 68,152,234 | Preslabo plava |
| Žuta **16** (800,500) | 255,201,90 | 251,192,94 | Hladnija/potamnjenija |
| Ljubičasta **64** (120,1160) | 181,77,229 | 196,117,233 | Previše blijeda |

## Implementacija

Metoda `BoardScreen.splashFloatingTile` sada koristi odvojene, podešene gornje i donje vrijednosti gradijenta za koraljnu, tirkiznu, plavu, žutu i ljubičastu **isključivo na svijetlom Splashu**. Dodatno su nijansirani donji reljef i bijeli obrub. Promjena ne dodiruje stvarne 4×4 / 5×5 pločice, njihove vrijednosti, gestikulacije, napredak igrača, dnevni izazov, tamnu temu ili trajanje Splasha (3200 ms).

Regresijski test `splashOwnerTilePaletteKeepsSaturatedFaces` crta **stvarni Android Canvas** u 941×1672 te provjerava RGB uzorke, nepostojanje scrollanja i snima `splash-saturated-faces-941x1672.png`.

## Obvezna provjera prije oznake 1:1

- [ ] Android PR validation: javni Gradle/Java build, lint i testovi — **na zadnjem HEAD-u**.
- [ ] Android 16 edge-to-edge / navigation QA — na zadnjem HEAD-u.
- [ ] Android UI emulator screenshot QA — na zadnjem HEAD-u.
- [ ] Preuzeti novi PNG i izračunati **RGB MAE cijelog Splasha i pojedinih obojenih pločica** prema istom izvorniku.
- [ ] Ako se ukupni/regionalni MAE poveća, korigirati novu paletu. Ne tvrditi 1:1 bez mjerenja.
- [ ] Nakon Splasha nastaviti Home, zatim ostale zaslone u korisnikovu redoslijedu.

**Ovo je samo prvi ciljani prolaz. Ne spajati Draft PR niti objaviti novo izdanje prije vizualnog i funkcionalnog QA-a.**
