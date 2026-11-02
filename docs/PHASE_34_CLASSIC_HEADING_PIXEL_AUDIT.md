# BROJKO — faza 34: Klasično 4×4, izvorna tipografija naslova

**Datum:** 2. studenoga 2026.  
**Vlasnikov izvornik:** `Slika ChatGPT 24. lis 2026. 22_15_13-4(20261102-201004).png`.  
**Stvarni Android emulator:** `golden-state-game-941x1672.png` iz potvrđenog UI testa `41521025947`.  

Oba PNG-a imaju **941 × 1672** piksela i uspoređena su bez skaliranja ili poravnanja. Izvorna demo-ploča u testu služi isključivo radi ponovljivosti: produkcijski rezultati ostaju stvarni.

## Stvarna polazna razlika

- Ukupni RGB MAE cijele stranice Klasično: **16,925** (0–255; manje je bolje).
- Naslov i dekoracije (x30–500, y70–350): **36,48**.
- Ploča (x20–920, y350–1320): **14,52**.
- Donje kontrole (x35–925, y1330–1605): **23,64**.
- Sam naslov / krajnji ROI (x30–485, y180–330), tamnoplava maska `R<100,G<140,B<170,B>R`: **17.176** pokrivenih izvornih piksela nasuprot **13.731** u emulatoru. Izgled je bio pretanak.

## Izmjena

Za naslov `Klasično` **isključivo svijetlog aktivnog Klasično 4×4** dodano je blago zaobljeno nativno `FILL_AND_STROKE` iscrtavanje uz zadržavanje istog horizontalnog fittinga i vertikalne skale. Ostali natpisi, zvukovi, tematika, drugi načini igre, touch područja, score i 2048 engine nisu mijenjani.

U već postojećem instrumentacijskom testu `classicFourByFourReferenceUsesSourceTopLightingAndRealControls` dodana je provjera približavanja izvornoj površini tamnoplavih slova, a nastavljen je stvarni screenshot test i provjera funkcionalnih gumba `Novo`, `Poništi`, `Završi`.

## Obvezan završni korak

- [ ] Android build/lint/unit; Android 16 insets; Android UI smoke/screenshot prolaze na **posljednjem HEAD-u**.
- [ ] Usporediti najnoviji stvarni `golden-state-game-941x1672.png` protiv istog izvornog PNG-a i obavezno dokumentirati **naslov i full-screen RGB MAE**.
- [ ] Ako izmjena pogorša naslov ili drugi ROI, prilagoditi debljinu ili vratiti.
- [ ] Nakon potvrde nastaviti originalni popup `Bravo!` / Pobjeda, zatim Dnevni izazov itd.

**Nema tvrdnje o potpunom 1:1 podudaranju bez novog mjerenja. PR ne spajati dok QA nije zelen.**
