# BROJKO — faza 24: Dnevni izazov, uvodni dekorativni potezi

Datum: 1. studenoga 2026.  
Grana i PR: `fix/missions-reward-star-reference-20261101`, Draft PR #34.

## Prije izmjene

Korisnikov original: `Slika ChatGPT 24. lis 2026. 22_15_15-6(20261101-200916).png`, 941 × 1672 px. Stvarna Android emulator snimka: `daily-source-streak7-941x1672.png`, iz uspješnog PR #34 screenshot workflowa. Oba PNG-a uspoređena bez promjene veličine ili pozicije.

| Područje | Originalni ROI u pikselima | RGB MAE prije |
|---|---|---:|
| Cijeli Dnevni izazov | [0,0,941,1672) | 17,512 |
| Lijevi uvodni potezi | [70,250,165,360) | 13,603 |
| Desni uvodni potezi | [775,250,870,360) | 13,150 |
| Naslov i uvodni tekst | [68,238,870,372) | 18,079 |
| Ploča | [165,385,770,955) | 14,073 |
| Ciljevi | [40,957,899,1310) | 19,343 |
| Nagrada | [40,1315,899,1512) | 26,064 |

U izvorniku postoje četiri zasićena narančasta/žuta dekorativna poteza pored dvorednog uvodnog teksta. Na stvarnoj prethodnoj emulator snimci ti potezi **u potpunosti nedostaju**, iako već postoje drugi ukrasi pored ploče i CTA-a. Detekcija izvornih narančastih piksela daje 1325 piksela u lijevom i 1207 u desnom ROI-u, a prethodni emulator u oba ROI-a ima 0.

## Promjene u produkcijskom Canvasu

- Dodana `dailyIntroMarks()`: četiri zasićena zaobljena poteza vektorski iscrtana prema izmjerenim koordinatama originala, bez novih mrežnih ovisnosti, slika ili animacijskih petlji.
- Geometrija i boje su statične, bez privremenih nizova po iscrtavanju.
- Ukras se prikazuje **isključivo u svijetloj portretnoj referentnoj temi**; postojeća tamna tema i drugačiji layout ostaju netaknuti.
- Datumi, dnevni streak, prava nagrada, tri cilja, pokretanje izazova, spremanje podataka i stvarne dodirne površine ostaju nepromijenjeni.

## Stvarna instrumentacijska provjera

`dailyIntroStrokesMatchOwnerSourcePixelsWithoutTouchOverlap` uzima pravi Canvas bitmap s Android emulatora u rezoluciji 941 × 1672, koristi privremeni fixture sa streakom 7 i provjerava četiri točke izvornika, njihov narančasti raspon boja, tri dostupne akcije pomoći, jedan CTA i nepromijenjen dnevni streak. Snima `daily-intro-rays-source-941x1672.png`, koja je obvezna u screenshot validatoru.

## Kriteriji završetka

- [ ] `Android pull request validation` uspješan na posljednjem SHA.
- [ ] `Android 16 navigation bar and inset QA` uspješan na posljednjem SHA.
- [ ] `Android UI smoke and screenshot QA` uspješan na posljednjem SHA.
- [ ] Preuzeti **novu stvarnu snimku** i mjeriti iste ROI-e; usporedba datuma iz različitih dana nije dokaz podudaranja tipografije.
- [ ] PR i dalje ostaje Draft; **bez mergea i bez novog izdanja**.
- [ ] Ne tvrditi 1:1 prije odgovarajuće pixel potvrde svakog zaslona.


## Nastavak u istom Draft PR-u: pravi 3D poklon (faza 25)

Vlasnikov izvorni PNG 941 × 1672 uspoređen je sa stvarnom emulator snimkom `daily-intro-rays-source-941x1672.png` nakon uspješne prethodne faze, bez pomicanja ili skaliranja. Sljedeće regije pokazuju zašto vizual poklona nije bio dovoljno usklađen:

| Regija | Koordinate [x0,y0,x1,y1) | Početni RGB MAE |
|---|---|---:|
| Cijeli Dnevni izazov | [0,0,941,1672) | 17,430 |
| Panel nagrade | [40,1315,899,1512) | 26,064 |
| Ilustracija poklona | [100,1310,320,1540) | **38,240** |
| Mašna | [115,1310,310,1410) | 32,276 |
| Tijelo i zlatna vrpca | [120,1400,302,1530) | **56,510** |
| Tekst nagrade | [350,1315,830,1520) | 23,855 |

### Potvrđen uzrok i stvarne izmjene

Android `Paint` zadržavao je prozirnu alfa-vrijednost iz obruba poklona i pri kasnijem iscrtavanju zlatne vrpce. Metoda `gradient()` ne mijenja alfa-kanal `Paint` objekta; zato je zlatna vrpca bila ružičasto-prozirna umjesto neprozirno zlatne. Taj problem ispravljen je **lokalnim resetiranjem boje/alfе prije gradijenta**, bez globalne promjene zajedničkog crtanja.

Uz to je proširena ljubičasta kutija `0,80 → 0,94` puta veličine ilustracije i malo produljena. Bezier geometrija dviju petlji mašne smanjena je u visinu i širinu: prethodna mašna dosezala je oko y=1322 px i gotovo dodirivala zadnju karticu cilja, dok je u referenci zlatni vrh približno y=1351 px. Geometrija je prilagođena upravo tom odmaku.

- Nema vanjskih slika, ovisnosti ili mrežnih servisa.
- Iznos stvarne nagrade `+250`, stanje `finished`, pravo pokretanje izazova i korisnička pohrana nisu izmijenjeni.
- Dodan je instrumentacijski test `dailyGiftHasOpaqueRibbonCompactBowAndSourceWidth`, koji na stvarnoj 941 × 1672 Canvas snimci provjerava neprozirnu žutu vrpcu, široko ljubičasto tijelo, donji položaj mašne i očuvan streak.
- Validator zahtijeva stvarni `daily-gift-opaque-ribbon-941x1672.png`.

### Kriterij prihvaćanja

Ove su vrijednosti **početna mjerenja, a ne rezultat najnovijeg commita**. Novi stvarni screenshot i sva tri uspješna GitHub Actions workflowa potrebni su za konačnu ponovljenu usporedbu; ako novi ROI MAE poraste ili novi test padne, ispravak se nastavlja u **istom PR-u #34**. PR ostaje Draft, bez mergea i izdanja.


### Prva QA iteracija i ispravljena regresija vrpce

Android screenshot QA `41360518251` za commit `77f582a` završio je uspješno i dostavio stvarni PNG. Njegova usporedba pokazala je **povećanje** ukupne pogreške jer je raniji, poluprozirni središnji pojas zamijenjen jednom neprozirnom vrpcom *na pogrešnom mjestu*:

| Ista regija | Prije | Nakon prve iteracije | Promjena |
|---|---:|---:|---:|
| Cijeli Dnevni izazov | 17,430 | 17,614 | +0,184 |
| Panel nagrade | 26,064 | 27,232 | +1,169 |
| Poklon | 38,240 | 43,978 | +5,738 |
| Mašna | 32,276 | **29,092** | −3,184 |
| Tijelo i vrpca | 56,510 | 70,948 | +14,438 |

Izvornik ne prikazuje središnji zlatni pojas. Na y=1480 px ima dvije odvojene zlatne vrpce, oko x=156–183 i x=247–271, između kojih je vidljiva ljubičasta površina oko x=214. Prva nova snimka imala je jednu vrpcu x≈197–230. Zato su sada zamijenjene **dvjema lokalnim neprozirnim vrpcama** i širokim zlatnim povezom na poklopcu, a regresijski test traži zlatnu boju na x=170 i x=259 te ljubičastu na x=214.

To je stvaran uzrok regresije, ne promjena u korisničkom stanju ili testnom fixtureu. **Prva iteracija nije prihvaćena.** Sljedeća Android screenshot serija na posljednjem commitu mora ponovno dokazati smanjenje RGB MAE. Tamna tema, stvarno osvajanje nagrada i povijest korisnika nisu predmet fixturea; ne smiju se lažirati.
