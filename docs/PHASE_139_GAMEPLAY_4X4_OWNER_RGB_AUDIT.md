# BROJKO — Faza 139: gameplay 4×4 native pigment and tile depth

Datum: 12. 11. 2026. Izravni razvoj na `main`, bez PR-a.

## Izvorne reference i ravnopravno stanje

Korisnikov original `Slika ChatGPT 24. lis 2026. 22_15_13-4(20261111-200411).png` i Android `classic-source-lighting-941x1672.png` iz uspješnog v1.12.221 emulatora. **Obje slike 941×1672, bez skaliranja**. Android test koristi identičnu ilustrativnu postavu svih 16 pločica, best=3864, score=1316. Izvorni fixture koristi se isključivo za QA; stvarni korisnički napredak i proizvodna igra ostaju dinamički.

| ROI | Prije, v1.12.221 RGB MAE (0–255) |
|---|---:|
| Cijeli 941×1672 | 12,635 |
| Naslov x30..500/y70..350 | 12,724 |
| HUD x450..940/y80..350 | 16,148 |
| Okvir ploče x20..920/y350..1320 | 14,119 |
| Sve pločice x38..910/y395..1288 | 15,176 |
| Donje kontrole x35..925/y1330..1605 | 13,451 |
| Sam gumb Završi x625..925/y1330..1605 | **18,295** |

## Mjerena odstupanja

Površina nativnog gumba `Završi` u prethodnom emulatoru ima zasićeniju crvenu i previše zelene/plave: primjer (700,1450), original RGB 253/127/83, emulator 255/136/91; (830,1510), original 253/112/71, emulator 255/124/82. Koristiti novi nativni koraljni vertikalni pigment samo u svijetloj 941px Classic kompoziciji, bez pomicanja gumba ili kontrole.

Na 4×4 ploči je stari native renderer koristio gotovo ujednačenu boju lijevo/desno unutar jednog reda, dok original ima horizontalni 3D pad osvjetljenja (gornja koraljna 2 y450 ima G 134 lijevo do 124 desno). Dodan je slab, nativni prozirni horizontalni overlay prije numerala i podešen izvorni ton koraljne pločice 2. Boje i pločice drugih tema, Opušteno 5×5, Dnevni izazov, Undo, pravila, stanje i responsive layout ne mijenjaju se.

Novi `OwnerGameplayColorPhase139Test` crta pravi 941×1672 Android Canvas i provjerava RGB područja, bočni pad pigmenta, broj stvarnih akcija i da se rezultat ne mijenja samim renderiranjem.

## Preostala vizualna odstupanja

- Opušteno 5×5 nema korisnikovu samostalnu originalnu 941×1672 referencu. Funkcionalne i stilske provjere nastavljaju se bez neutemeljene tvrdnje 1:1.
- Misije: razlike u obrisima malih ilustracija, širini i rasterizaciji natpisa i statičkoj referenci napretka. Stvarne nagrade nikada se ne falsificiraju.
- Teme: mali preostali RGB pomaci i izvorna tipografija kontrola.
- Naslovnica: slobodne 3D ilustracije, tipografija i vrijednosti kartica.

**Rezultat nakon promjene i 0% odstupanja nisu potvrđeni** dok svježi CI ne završi te se iste referentne regije ne izmjere na novoj stvarnoj Android snimci. Vratiti svaku promjenu koja pogorša relevantan ROI bez opravdanja.
