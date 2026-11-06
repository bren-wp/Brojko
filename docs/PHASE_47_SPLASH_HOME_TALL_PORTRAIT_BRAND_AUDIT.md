# Brojko — faza 47: dosljedan Splash / Home na visokim portretnim zaslonima

Datum: 7. studenoga 2026. · Brendigo

## Provjerene stvarne Android snimke

Pregledani su izvorni PNG-ovi koje je snimio instrumentirani Android emulator (PR #62, workflow **42310800421**, artefakt `Brojko-real-device-UI`):
- `three-screen-source-splash-941x1672.png` i `owner-home-12560-3864-941x1672.png`;
- `splash-responsive-360x800.png` i `home-responsive-360x800.png`;
- `reference-941x1672-game-classic.png` i `relaxed-raised-gameplay-941x1672.png`.

To su pravi renderi produkcijskog `BoardScreen` unutar emulatora (ponekad s determinističkim demonstracijskim stanjem), **ne** korisnikove osobne snimke i **ne** dokaz da su svi zasloni pixel-perfect u odnosu na vlasnikove originale.

## Utvrđeni problem

`referencePortraitLayout` razlikuje portrete omjera približno 16:9 od 20:9. Na 941×1672 Splash i Home već upotrebljavaju nativne vektorske konture vlasnikova tamnoplavoga logotipa, dok je 360×800 render prikazivao znatno drugačiji Nunito fallback, a Home je dodavao zaseban slogan odmah iznad kartica s rezultatima. Zbog toga oblici slova, vizualni sjaj i vertikalni razmaci nisu dosljedni na visokim Android uređajima.

## Promjene u stvarnom kodu

- **Splash, svijetla tema:** 20:9 portret koristi iste postojeće konture `SplashBrandContours.NAVY`, boje, sjenu i uzdignutu brojku 2 kao izvorni 941×1672 izgled. Na većem osnovnom platnu logo je pomaknut lokalnim Canvas transformom, bez mijenjanja ukrasnih pločica ili slogana. Nema bitmap pozadina i nema duploga ispisa navy fonta.
- **Home, svijetla tema:** visoki portret rabi izvorni `ownerHomeNavyWordmark` i 3D embleme s umjerenom skalom, a nepotrebni dupli slogan iznad kartica više se ne prikazuje. Izvorni raspored kartica, akcija i brojčanih rezultata nije pomaknut.
- **Vektorski reljef:** izvorne navy konture dobile su vrlo tanak zaobljeni obrub i meku sjenu, sa zadržanim postojećim gradijentima; kratki 941×1672 portret ne dobiva izmijenjenu geometriju.
- **Klasično 4×4:** nakon pregleda stvarne emulatorske snimke, naslov je lokalno pomaknut za 4 dizajnerske jedinice prema gore, kako donji navy potezi ne bi dodirivali gornji rub koraljne 4×4 značke. Ploča, iznos bodova, status ikone, veličina gumba i touch zone nisu dirnuti.
- **Opušteno 5×5:** naslov dobiva mjerenu širinu izvornog Nunito Black fonta, blagi 3D gradijent, zaobljenu konturu i sjenu samo u aktivnoj svijetloj igri; pločice, ostale teme, statistika i geste ostaju netaknuti. Dodatni test na 941×1672 i 1080×2400 provjerava razdvajanje naslova, HUD-a i koraljne 5×5 značke.

## Regresijski kriteriji

Instrumentacija dodatno crta `941×1672`, `360×800` i `320×720`, provjerava upotrebu nativnih SVG-ekvivalentnih Canvas putanja, vidljivost oznake `by Brendigo`, odsutnost Home duplikata slogana i dostupnost `Nova igra / Nastavi igru` te `Klasično` akcije. Klasični naslov zasebno se provjerava prema rubu 4×4 značke, uz aktivnu Pauzu i pravila za Undo.

**Izmjene su u izvornoj grani.** Nužni su još CI/Android emulator i *nova* vizualna usporedba njegovih PNG-ova s punim vlasnikovim izvornicima prije tvrdnje 1:1 ili mergea.

## Sljedeće po prioritetu

1. Izolirani ROI audit osvjetljenja, zaobljenosti i vertikalnog položaja Splash/Home na 941×1672 i 360×800, bez lažiranja rezultata.
2. **Opušteno 5×5**: nastavak provjere 3D pločica, pozicije rezultata, akcijskih ikona i čitljivosti 512/1024/2048 na malim zaslonima; izmjere novu tipografiju prema punoj vlasnikovoj referenci ako je dostupna.
3. Dnevni izazov → Misije i postignuća → Statistika → Teme → Postavke → Pause / Bravo / kraj partije, svaki zaslon pojedinačno, s testom.
