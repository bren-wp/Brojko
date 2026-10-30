# Faza 07/10 — Teme i izgled (30.10.2026.)

## Referenca i opseg
- Original: korisnikov `Slika ChatGPT 24. lis 2026. 22_15_18-9(20261030-222444).png`,
  dimenzija 941 × 1672 px.
- Repo/PR: `bren-wp/Brojko#28`, grana
  `feat/pixel-victory-modal-phase4`.
- Izmjene samo u prikazu odabira tema/stilova i njegovim testovima;
  ne mijenja se igra 2048, postojeći rezultati, način čuvanja ili
  vizualni prikazi stvarnih pločica u gameplayu.

## Početna referentna kompozicija
Gornji Brojko logo i naslov u toploj krem kartici; tri 2×2 pregledne
kartice Svijetla/Tamna/Živopisna; lijevo „Pregled” 4×4 (16 pločica);
desno materijali Zaobljene/Ravne/Staklaste, dvije sklopke, izbor
animacijske jačine; dolje četveročlana navigacija.

## Ova iteracija
- Izdvojen `themeChoiceSurface`: posebne krem, tamnoplave i
  ljubičasto-plave gradientne površine pregleda teme, meki reljef
  i zasebni svijetli obrub. Promjena ne zadire u stvarni renderer
  pločica i paletu igre.
- Izdvojen `themeBoardTray`: pastelni gradijent i meka sjena
  za 16-pločični vizualni pregled u standardnoj i referentnoj
  portretnoj geometriji.
- `themesPhaseSevenRendersOwnerLayoutAndKeepsRealPreferences`
  provjerava tri teme, tri stila, četiri kartice navigacije, odabir,
  stvarno trajno spremanje postavki i izrađuje snimku
  `themes-phase07-owner-reference-941x1672.png`.
- Screenshot validator zahtijeva stvarnu punu 941×1672 snimku.
- Popravljen i prethodni CI pad iz faze 06:
  `statisticsHistoryUsesRealCompletionTimeAndPreservesLegacyRecords`
  testirao je vremensku oznaku u staroj ROI visini 475–501,
  dok povijest sada počinje na y=530. Sada provjerava stvarnu
  vidljivu ROI 532–558. Produkcijski podaci nisu mijenjani.

## Potrebna provjera prije prihvaćanja
1. Sva tri GitHub Actions workflowa na zadnjem commitu moraju završiti.
2. Iz `Brojko-real-device-UI` preuzeti stvarni
   `themes-phase07-owner-reference-941x1672.png` i usporediti RGB MAE
   s originalnom slikom BEZ resamplinga.
3. Usporediti zasebne ROI: logo/naslov, tri izbora teme,
   16 pločica lijevo, tri stila, dvije sklopke, slider, donji nav.
4. Provjeriti persistenciju (theme/style/audio/vibration/animation)
   nakon restarta, TalkBack, Android 16 i 360×600.
5. Ispraviti svaku CI/pixel regresiju prije mergea i prije izdanja.

**Status:** kod/test spremni na istoj grani; screenshot pixel-diff i
posljednji CI tek treba potvrditi. **Nije potvrđeno 1:1.**
**Sljedeći zaslon nakon završnog QA:** 8/10 — Postavke.
