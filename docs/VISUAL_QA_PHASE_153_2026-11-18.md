# BROJKO — faza 153: Misije i postignuća (18. 11. 2026.)

## Potvrđeni početni GitHub podaci

- `main` prije rada: `d1cf3296dcfb11be69cf4eb6007ebff328b0d7e8` (spojeni PR #152).
- Objavljeno izdanje [v1.12.252](https://github.com/bren-wp/Brojko/releases/tag/v1.12.252): 34 artefakta, uključujući 28 stvarnih PNG-ova, `prerelease: true`, unsigned preview.
- [Post-release UI QA #44198775951](https://github.com/bren-wp/Brojko/actions/runs/44198775951): `completed/success`, HEAD odgovara početnom `main`.
- Posljednja stvarna emulatorska galerija: `Brojko-real-device-UI`, GitHub Actions artifact **#14380488483**. Izvršavanje je završilo uspješno; PNG datoteke iz ZIP-a nisu u ovom izvještaju zasebno rasterizirane/uspoređene.
- Pri početnom pregledu: 0 otvorenih PR-ova i 0 otvorenih issuea.

## Izvor i status pixel audita

Vlasnikova izvorna referenca: `Slika ChatGPT 24. lis 2026. 22_15_16-7(20261111-200342).png`, dimenzije **941×1672** prema prethodno dokumentiranom auditu. Pri pretraživanju raspoloživih korisničkih datoteka PNG **nije pronađen**. Ne nalazi se u javnom repozitoriju, i ne smije se zamijeniti generiranom ilustracijom.

**Stanje mjerenja faze 153:** bez otvorene izvorne PNG datoteke i pregledane emulatorske snimke nema legitimnog novog RGB MAE, postotka identičnih piksela, ROI prije/poslije ni dokaza poboljšanja. U ovoj fazi **nema promjena geometrije, gradijenata, zvjezdica ili ikona** jer vizualni kandidat bez vlasničkog usporednog mjerenja ne bi bio dokazano bolji.

Povijesni podaci, koji se **ne smiju** predstavljati kao aktualni `v1.12.252` baseline: u [fazi 137](PHASE_137_MISSIONS_PASTEL_PIXEL_AUDIT.md) nakon `v1.12.220` zabilježeni su cijeli zaslon RGB MAE 13,6830 i 1,4578 % identičnih RGB piksela. [Faza 138](PHASE_138_OWNER_REWARD_STARS_PIXEL_AUDIT.md) posebno navodi pozicije šest zvjezdica u starijem izdanju.

### Obvezni ROI za sljedeću stvarnu usporedbu (native 941×1672)

Svi intervali su `[x0,y0,x1,y1)`; bez resamplinga i bez ponovnog poravnavanja.

| Regija | ROI | RGB MAE prije | RGB MAE poslije | Exact RGB prije/poslije |
|---|---|---|---|---|
| Cijeli zaslon Misije | `(0,0,941,1672)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Zlatna nagrada Spoji 20 | `(775,415,865,510)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Zlatna nagrada Dosegni 512 | `(775,610,865,695)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Zlatna nagrada Igraj 7 | `(775,890,865,975)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Zlatna nagrada 10.000 | `(775,1065,865,1155)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Zlatna nagrada Majstor | `(775,1340,865,1460)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Zlatna nagrada Kralj | `(775,1520,865,1630)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Dnevne kartice | `(25,298,915,760)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Tjedne kartice | `(25,758,915,1235)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Postignuća | `(25,1218,915,1645)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Ikone zadataka | `(45,375,220,1630)` | nije izmjeren | nije izmjeren | nije izmjeren |
| Brojači i napredak | `(225,350,735,1620)` | nije izmjeren | nije izmjeren | nije izmjeren |

Izvještaj nakon izvršenog QA-ja treba dokumentirati URL i ID novog GitHub Actions artefakta, checksum PNG-a, per-ROI vrijednosti i 254-state usporedbu prema kontrolnoj galeriji. Trenutačno to **nije obavljeno**.

## Stvarni zahvat u kodu

- `MissionProgress` odvaja obradu prikaznih brojača od vrijednosti pohranjenih u `SharedPreferences`. Za važeće pozitivne misije ostaju ista ispunjenost i isti brojevi. Za nevaljane negativne vrijednosti prikaz se ograničava na nulu; izvorni podaci se ne prepisuju.
- Zasebno binarno postignuće 1024 ostaje 0/1 ili 1/1. Nedostignuta pločica 512 nije vizualno polovično završeno postignuće.
- `MissionProgressTest`: pet JVM testova, uključujući 12/20, 6250/10000, 512/1024, negativne i granične vrijednosti.
- `MissionsPhase153RegressionTest`: dva stvarna Android instrumentacijska testa — render svih triju tema u viewportovima 320×480, 360×800, 941×1672 i 1080×2400 bez zamjene stvarnog modela; zasebna provjera svijetle zlatne površine i tamnijeg reljefa na svih šest nagradnih zvjezdica.
- `OwnerMissionRewardStarsPhase138Test` ostaje neizmijenjen, kao postojeći regresijski test pozicije šest zlatnih maski.
- README sada upućuje na stvarnu v1.12.252 galeriju. Nije dodana ni jedna umjetna slika ili nova mrežna dozvola.

## QA i sigurnosni kriteriji prije spajanja

- [ ] `Android pull request validation` na posljednjem HEAD-u (Gradle, Android Lint, JVM testovi, statički pregled, CycloneDX runtime/build i SCA).
- [ ] `Android 16 navigation bar and inset QA` na posljednjem HEAD-u.
- [ ] `Android UI smoke and screenshot QA` na posljednjem HEAD-u s točnim brojem instrumentacijskih testova.
- [ ] Pregledati sve nove emulatorske snimke i 254-state regresijski izvještaj.
- [ ] Usporediti originalni vlasnički PNG protiv aktualnog baselinea i nove emulatorske slike; izmjeriti gore navedene ROI-je.
- [ ] Provjeriti interaktivna stanja, TalkBack, osvježavanje datuma, male i velike zaslone i sva tri stila pločica.
- [ ] Provjeriti postojeći [registar build-toolchain GHSA nalaza](SCA_SBOM.md) zasebno od Android runtime rizika.
- [ ] Nakon reviewa, uspješnog CI-ja i verificiranog vizualnog rezultata razmotriti spajanje te tek onda objaviti novo izdanje s APK/AAB, 28 PNG-ova, dva SBOM-a i kontrolnim sumama.

**Status:** razvojni Draft PR; nema tvrdnje o pixel-perfect 1:1, prošlim testovima za novi HEAD, produkcijskom potpisu ni novom izdanju.
