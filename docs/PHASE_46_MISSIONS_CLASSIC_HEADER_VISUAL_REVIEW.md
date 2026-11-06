# Brojko — faza 46: naslov misija i prioriteti vizualne dorade

Datum: 7. studenoga 2026. · Brendigo

## Što je potvrđeno

Vlasnik je priložio dvije izrezane PNG slike, **371 × 152** (Misije i postignuća) i **282 × 155** (Klasično 4×4). One su korisne za inspekciju obrisa slova, odnosa boja, sjenčanja i razmaka, ali nisu puni izvorni zasloni. Sam PNG bez lanca nastanka ne dokazuje je li snimljen na fizičkom uređaju ili emulatoru. Ne izmišljati izvornu rezoluciju ni MAE metriku za takve izreske.

Repozitorij koristi Android Canvas renderer; galerija iz CI procesa nastaje instrumentacijskim crtanjem istoga produkcijskog `BoardScreen` na emulatoru. Dio usporednih snimaka koristi *izolirano pripremljene testne rezultate*, a ne osobnu povijest korisnika. Takav kadar je stvarni Android render, ali nije dokaz potpuno identične tipografije niti snimka korisničke partije.

## Implementirano u ovoj fazi

- **Misije / dvobojni naslov:** uklonjene dvije zasebne `fitted()` kutije čije se nominalne širine mogu preklapati. Obje riječi sada mjere *stvarnu* širinu učitanoga Nunito Black fonta, koriste jednaku visinu, fiksiran optički razmak i zajedničko centriranje. Na uskom zaslonu skalira se veličina obiju riječi **zajedno**, bez neovisnog horizontalnog izobličavanja.
- **Materijal slova:** u svijetloj temi naslov dobiva diskretan topli sjaj, zaobljeni spoj konture, suptilno tamniji donji rub i vrlo blagu sjenu. Boje navy i koraljna ostaju kao u korisničkoj referenci. Tamna tema čuva dosadašnju paletu.
- **Regresijski test:** instrumentacija provjerava dvije zasebne površine tekstualnog inka na 941×1672, 360×640 i 320×480, u svijetloj i tamnoj temi, te prisutnost funkcionalnog gumba Natrag.
- Nisu dirani spremanje igre, rezultati, misije, nagrade, touch zone, pravila 2048 ili korisničke postavke.

## Preostali prioriteti (nisu proglašeni dovršenima)

1. **Splash + Home:** u punoj vlasnikovoj referenci izmjeriti zaobljenost Brojko logotipa, 3D prijelaz, visinu reljefa brojke 2, pozadinski sjaj, byline i razmake. Provjeriti na više omjera zaslona i u tamnoj temi.
2. **Klasično 4×4 i Opušteno 5×5:** usporediti zaobljene tamnoplave znakove, refleksiju na 4×4 znački, ikone Pauza/Poništi, omjer HUD-a i pločice. Klasični naslov i dalje ima zasebno podešenu konturu i gradijent.
3. **Misije:** ponovno snimiti novi renderer, usporediti puni referentni zaslon i ROI naslova, zatim doraditi rubove kartica i sekcijske simbole po izmjerenim razlikama.
4. **Ostali prozori:** Dnevni izazov, pobjeda/kraj, Statistika, Teme, Postavke i svi modali s neovisnim screenshot auditom.
5. **Objava:** ne nazivati ovo pixel-perfect 1:1 dok svježi emulator PNG nije uspoređen s *punim izvornikom* i dok sva tri CI workflowa ne prođu za zadnji SHA.

## Kriterij prihvaćanja

Dvije boje naslova bez međusobnog dodirivanja, nijedna riječ ne prelazi rub, nativni renderer i aktivne akcije sačuvani, Android lint/unit/UI QA zeleni. CI uspjeh je nužan, ali sam po sebi nije dovoljan dokaz 1:1.
