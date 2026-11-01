# BROJKO — faza 22: Misije i postignuća, stvarni pixel audit

Datum: 1. studenoga 2026.  
Postojeći Draft PR: [#33](https://github.com/bren-wp/Brojko/pull/33)  
Polazište: `2707e99dc9a82304b693286be54cbdb466ddd4e4`

## Izvori i način usporedbe

Izvorna korisnikova referenca: `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261101-200917).png` (941 × 1672). Stvarna početna Android emulator snimka: `missions-six-real-progress-941x1672.png`, iz uspješnog workflowa PR-a #33. Uspoređeno bez skaliranja i pomicanja, metrikom RGB MAE (prosjek apsolutnih razlika triju kanala, 0–255).

| Regija | Koordinate px [x0,y0,x1,y1) | RGB MAE prije ove faze |
|---|---|---:|
| Cijeli zaslon Misije | [0,0,941,1672) | **25,862** |
| Dnevna progresna traka | [242,495,595,520) | 59,992 |
| Kralj rezultata — donji dio kartice | [242,1560,593,1592) | 26,521 |
| Zaglavlje | [20,15,920,294) | 33,534 |
| Tri odjeljka | [25,300,901,1635) | 27,190 |

**Ograničenje:** izvornik sadrži demonstracijski brojač postignuća `4 / 20`, dok stvarna igra ima dva implementirana postignuća i prikazuje `0 / 2` kad nijedno nije osvojeno. Ovo nije dopušteno lažirati. Vremenski brojači u referenci također ne odgovaraju stvarnom vremenu emulatora. Cjelokupni RGB MAE zbog tih razlika nije samo mjera geometrije i boja.

## Potvrđeni popravci

- Traka aktivne misije `Spoji 20 pločica` na svijetlom portretnom zaslonu sada koristi izvorno **plavu** paletu `#2AA5FF` (prije tirkizna `#2EC4B6`). Isti stvarni napredak `value / 20` ostaje vezan uz lokalnu igru.
- Traka `Kralj rezultata` sada je **ljubičasta** `#A16CFA` (prije narančasta). Omjer i dalje izračunava stvarni `best / 50000`.
- Ikonice i njihove vlastite boje nisu dirane, kao ni tamna tema, spremanje, misije, zvijezde, nagrade ili njihova jednokratna dodjela.
- Zaglavlje: specifična misijska strelica Natrag podignuta je s generičkog y=43 na y=16 logičkih jedinica kako bi odgovarala korisnikovu izvornom PNG-u, uz istu funkciju `navigate("home")` i isti pristupačni naziv `Natrag`.
- Dodano je šest koraljno-narančastih dekorativnih poteza lijevo i desno od naslova; geometrija je statična i ne alocira nizove u svakom Canvas okviru. Naslov je prilagođen tako da dekoracija ne prekriva tekst.
- Novi test također mjeri stvarne boje lijevog i desnog poteza te pristupačno dodirno područje tipke Natrag.
- Nedovršeno binarno postignuće `Majstor spajanja` pri vrijednosti `512 / 1024` zadržava traku bez napretka i brojač `0 / 1`; nema izmišljene 50%-tne nagrade.

## Regresijski testovi i QA

Novi `missionsSourceProgressPalettePreservesRealFractions` instrumentacijski test koristi izvorne mjere 941 × 1672 i deterministički fixture `todayMerges=12`, `weekPoints=6250`, `playDayStreak=5`, `recordTile=512`, `best=8400`, `rewardFlags=0` te provjerava:
1. Plavi dnevni i ljubičasti rekordni napredak na stvarnim bitmap pikselima.
2. Nenagrađeno binarno postignuće te nepromjenjivost korisničkih brojčanih vrijednosti.
3. Svih šest dostupnih misija na referentnoj visini, bez lažnog scrollanja.
4. Stvarni Android screenshot `missions-source-progress-palette-941x1672.png`, obvezan u screenshot validatoru.

Novi RGB MAE smije se zabilježiti **tek nakon uspješnog screenshot QA-a posljednjeg commita** i preuzimanja aktualnog artefakta. Ne tvrditi `1:1` dok se svi geometrijski, tipografski i sadržajni detalji ne validiraju identičnim fixtureom.

**Bez mergea i bez novog izdanja.** Google Play značka u README-u ostaje povezana na `https://play.google.com/store/apps/details?id=com.brendigo.brojko`.
