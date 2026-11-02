# BROJKO — faze 37 i 38: Statistika → Teme i izgled, nativni Pixel Audit

Datum: 2. studenoga 2026.

## Izvori

Vlasnikovi `Slika ChatGPT 24. lis 2026. 22_15_17-8(20261102-201001).png` (Statistika) i `Slika ChatGPT 24. lis 2026. 22_15_18-9(20261102-201002).png` (Teme i izgled), oba **941×1672 px**. Referentni Android emulator: uspješan [UI QA #41525523484](https://github.com/bren-wp/Brojko/actions/runs/41525523484), snimke `golden-state-stats-941x1672.png` i `themes-peach-style-swatches-941x1672.png`.

| Zaslon | RGB MAE prije novih zahvata |
|---|---:|
| Statistika, cijeli 941×1672 | **20,955** |
| Teme i izgled, cijeli 941×1672 | **22,739** |

Nema skaliranja, umjetnog poravnavanja, demonstracijskih podataka u produkciji ni tvrdnje o potvrđenom 1:1.

## Faza 37 — Statistika: stvarni bug prikaza ljubičastog plamena

- Statistika prikazuje plamen u kartici `Niz pobjeda`. Izvorni ROI `x500..665,y555..730` ima **5961** zasićen ljubičast piksel (`70<R<195,40<G<155,B>R+25,B>G+70`).
- Stvarna prethodna emulacija imala je **0** takvih piksela: `polishedIcon("flame")` nakon sjene s `p.setColor(0x270b1740)` zadržavao je 15% Paint alpha prije gradijenta; samo Daily resetirao je neprozirnost.
- Sada se potpuno neproziran Paint vraća za **sve** flame prikaze, uključujući statički KPI plamen, bez promjene rezultata i pohrane.
- `statisticsStreakFlameUsesOpaqueSourcePurple` provjerava pravi Android Canvas i zahtijeva više od 1500 zasićenih ljubičastih piksela. Screenshot: `stats-opaque-streak-flame-941x1672.png`.

## Faza 38 — Teme i izgled: izvorni dvodimenzionalni živopisni gradijent

- Na originalnoj Živopisnoj kartici donji lijevi izloženi rub `(650,650)` ima RGB **(9,162,253)**, a donji desni `(860,635)` RGB **(161,76,253)**.
- Stara stvarna Android snimka: **(31,121,234)** i **(38,119,235)** — gotovo identična plava boja, jer je upotrebljavan samo okomiti gradijent.
- Novi nativni `RadialGradient` uvodi cijan sloj lijevo i ljubičasti desno, isključivo u svijetloj portretnoj minijaturi teme Živopisna; prave igraće pločice ostaju netaknute.
- `vividThemePreviewHasSourceCyanAndPurpleCorners` provjerava RGB oba ruba i sva 3 funkcionalna gumba teme, bilježi pravi screenshot `themes-vivid-cyan-purple-source-941x1672.png`.

## Izmjerena prethodna Daily/Missions iteracija i korekcija regresije

Usporedba stvarnog screenshot QA artefakta uspješnog runa [#41529651510](https://github.com/bren-wp/Brojko/actions/runs/41529651510) s izvornicima:

| Zaslon/ROI | Prije | Nakon prvog prolaza |
|---|---:|---:|
| Dnevni izazov, cijeli | 16,846 | **16,744** |
| Dnevni izazov, nagrada | 19,666 | **18,859** |
| Dnevni izazov, dodatni badge ROI | 6,497 | **3,729** |
| Misije, cijeli | 23,590 | **23,644** |
| Misije, tjedni dio | 27,870 | **28,026** |

Uočen je razlog regresije Misija: `Ostvari 10.000 bodova ukupno` dobio je izvorni dvoredni opis, ali narančasta traka ostala je **na y1139..1154 px** i preklapala tekst, dok je na originalu traka **y1169..1179 px**. Popravak na istoj grani spušta samo traku te misije za **13 dizajnerskih jedinica**, a brojilo za 10. Test `missionProgressTracksMatchSourceRowLengths` dodatno provjerava realan narančasti piksel pri `x=160,y=500` dizajnerskih jedinica.

To su samo uspoređene stare/prethodne snimke; rezultate *ovog* novog položaja treba ponovno mjeriti.

## Preostala provjera

- [ ] 3 GitHub Actions workflowa zadnjeg PR HEAD-a: compilation/lint/unit, API36 insets, real emulator UI screenshots
- [ ] Novi PNG 941×1672 i regionalni/full RGB MAE za Daily/Missions/Stats/Themes, bez resamplinga
- [ ] Ako je bilo koji relevantni ROI lošiji, korigirati ili vratiti dotični zahvat
- [ ] Zaslon **Postavke**: ne kopirati marketinški cloud-sync ON toggle ako aplikacija podržava samo lokalni backup; ovdje je razlika funkcionalna, a ne samo vizualna. Ne mijenjati stvarne sound/vibration/notification postavke radi demonstracijskog 1:1
- [ ] Finalne launcher ikone i brending provjeravati kao posebne originalne izvore
- [ ] Bez mergea i APK/AAB releasea dok nije potvrđen stvarni QA

Ovo je dokument o izvornom kodu i realnim Android screenshotovima, ne generirane ilustracije u chatu.
