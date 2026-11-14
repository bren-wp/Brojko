# BROJKO — faza 144: 3D zlatne vrpce i stvarna osnovica statističkog grafikona

14. studenoga 2026. / Polazište: `v1.12.243`, `main` commit
`6a6ef417e39d09be77ec966f4af9552a4a8e0ace`.

## Izvori i mjerilo

Originalne PNG reference korisnika, izvorno **941 × 1672 px**:
- Dnevni izazov: `Slika ChatGPT 24. lis 2026. 22_15_15-6`
- Statistika: `Slika ChatGPT 24. lis 2026. 22_15_17-8`

Baseline: stvarni Android emulator screenshoti iz [PR #143 UI QA](https://github.com/bren-wp/Brojko/actions/runs/43533423980), originalni ZIP artefakt `Brojko-real-device-UI` ID `14088014065`, `daily-refined-board-gift-941x1672.png` i `golden-state-stats-941x1672.png`.

| Izvorni ROI (x0,y0,x1,y1) | v1.12.243 RGB MAE |
| --- | ---: |
| Dnevni poklon `90,1300,290,1510` | 19,7165 |
| Ljubičasto tijelo `95,1400,287,1510` | 22,5806 |
| Statistika grafikon `55,790,880,1100` | 22,3378 |
| Statistika stupci `55,880,880,1080` | 26,2964 |

RGB MAE raspon je **0–255**, nije postotak odstupanja. Pixel-diff
izvodi se bez crop-pomaka, resizea, interpolacije i izmišljanja
korisničkih rezultata.

## Uočena mjerljiva vizualna razlika

Vlasnikova referenca ima tanku vodoravnu osnovicu ispod stupaca
(x oko 33–847, y oko 1092–1095); aktualni renderer završava
stupce bez te crte. Prethodni pokušaj iz faze 142 dodavao je
drugu osnovicu **prije** stupaca, unutar pogrešnog x raspona,
što je povisilo MAE. Novi zahvat crta diskretnu
`#e2dfd9` crtu **nakon** stupaca, x14–360 logičkih jedinica,
y464.35–465.55, samo za svijetli vlasnički portret i
**isključivo uz pozitivnu stvarno spremljenu vrijednost**.

Prije Android testa provedena je odvojena *simulacija
prebojavanja piksela* na **stvarnim vlasničkim i baznim
screenshotovima**. Za x33–847 / y1092–1095, ton
RGB(226,223,217), simulacija predviđa približno −0,372 MAE
unutar cjelokupnog grafikonskog ROI-a. To je **procjena**,
ne prihvatni rezultat: stvarni Canvas/antialias treba
ponovno izmjeriti na emulatoru zadnjeg SHA-a.

Na ilustraciji poklona dodani su mali odsjaji unutar
postojećih zlatnih remena. Nema promjena širine,
visine, mašne, pozicije, stranica, ciljeva izazova ni
same 2048 mehanike. Odsjaji se smiju zadržati samo
ako stvarni poklon ROI nije pogoršan.

## Uvjeti prihvata i sigurnost podataka

- Android CI kompilacija, Lint, unit + SCA moraju biti zeleni.
- API 36 navigacija/insets, Android UI instrumentation i
  stvarni PNG capture moraju biti zeleni **na posljednjem SHA-u**.
- Prošireni `RealHistoryChartAndBackupPhase143Test`:
  oba taba moraju crtati pravi sačuvani 10-redni QA arhiv,
  os mora biti vidljiva, prazni arhiv ne smije stvarati
  crtu, stupce ili demo rezultate. Test koristi isključivo
  izolirani QA SharedPreferences.
- Korisnička partija, rekord, povijest, sigurnosne kopije,
  opcije i dostupne kontrole ne smiju se promijeniti.
- Novi stvarni emulatorski PNG usporediti s istim izvornim
  ROI-jevima. Regresiju vratiti ili korigirati.
- Nakon uspješnog PR mergea obvezan novi GitHub Release
  s instalabilnim testnim `app-debug.apk`, nepotpisanim
  `app-release-unsigned.apk`, `app-release.aab`,
  `SHA256SUMS`, oba SBOM-a i stvarnim screenshotovima.
  Produkcijski potpis nije preduvjet za preview release.

**Ne tvrditi** apsolutno 1:1 ili produkcijsku potpisanu
Play nadogradnju bez zasebnog dokaza.
