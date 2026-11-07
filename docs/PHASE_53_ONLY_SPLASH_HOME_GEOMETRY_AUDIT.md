# BROJKO: vlasničke reference — dva ekrana, lokalna geometrijska dorada

Datum: 7. studenoga 2026.

## Opseg

**Isključivo Splash i Home**. Referentne datoteke koje je dostavio vlasnik:
- `Slika ChatGPT 24. lis 2026. 22_15_06-2(20261107-202348).png` — Splash
- `Slika ChatGPT 24. lis 2026. 22_15_13-3(20261107-202347).png` — Home

Obje imaju točno **941 × 1672 px**. U aplikaciju nisu ugrađene statične slike zaslona. Zadržani su stvarni rezultati, aktivne kartice, postojeće opcije igre, pristupačnost, lokalna pohrana i sva navigacija. Nisu dirani gameplay, teme ni ostale stranice.

## Nalazi iz stvarne Android snimke

Za početnu usporedbu korištena je stvarna Android Canvas snimka prethodnog uspješnog [UI workflowa 42326124866](https://github.com/bren-wp/Brojko/actions/runs/42326124866), artefakt `Brojko-real-device-UI`, oba izvornika **bez resampliranja**. To je *prije ove grane* i prije konačne provjere najnovijih boja.

- Splash koraljna pločica **2**: izvorni obojeni otisak približno x141–402, y139–400; prethodni Android x134–406, y130–402.
- Splash tirkizna pločica **4**: izvornik približno x560–802, y174–425; prethodni Android x551–827, y164–432.
- Splash žuta **16**: izvornik počinje približno x739, y417; prethodni Android x744, y412.
- Home ukrasna 4×4 minijatura: izvornik ima približno **57 px** horizontalni i vertikalni korak; stari Android približno **59 px**. U izvornoj referenci je koraljna pločica obojena na (121,973); prije izmjene tamo je bila prazna kremasta kartica.

Pred-izmjenski RGB MAE (po kanalu; 0–255): Splash **12,352**; Home **14,919**. To su brojke iz ranijeg artefakta, *nisu* rezultat ove dorade niti dokaz pixel-perfect podudaranja.

## Lokalna izmjena

1. U svijetlom portretnom Splashu smanjene su i lokalno pozicionirane dekorativne 2 i 4; 16 je minimalno pomaknuta. Tekst na pločicama se transformira zajedno s površinom.
2. Na Homeu je razmak minijature **Klasično 4 × 4** smanjen s 2,2 na 1,3 dizajnerskih jedinica, uz lokalnu korekciju pomaka. Pločice i naslov ostaju nativno nacrtani, a dodirna kartica zadržava isti `Tap` pravokutnik.
3. Dodana su dva Android instrumentacijska testa izvornog Canvas renderera za provjeru izvornog obojenog otiska te dostupnosti svih glavnih Home akcija.

## Provjera prije mergea / tvrdnje 1:1

- [ ] Build, lint, unit i oba nova instrumentacijska testa prolaze **na posljednjem HEAD-u**.
- [ ] Dohvatiti **nove** stvarne 941×1672 emulatorske PNG-ove na HEAD-u ovog PR-a.
- [ ] Izračunati RGB MAE cijelog Splasha i Homea, te ROI: Splash gornje pločice (x0–941,y110–670), Home klasična minijatura (x50–310,y925–1175), uz usporedbu na identičnom state fixtureu.
- [ ] Ako ROI ili cijeli zaslon regresira, prilagoditi ili vratiti odgovarajući lokalni transform.
- [ ] Vizualno potvrditi na 320×480, 360×800, 941×1672 i landscape: nijedna važna akcija ne smije biti odrezana niti nedostupna.
- [ ] Potvrditi stvarni izgled 3D sjena, mekanih rubova i tipografije na uređaju.

**Trenutačni status:** kod i testovi su predani na kontroliranu granu; bez nove stvarne emulatorske snimke nije vjerodostojno tvrditi **0 % odstupanja**.
