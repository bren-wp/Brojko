# Brojko — faza 130: stvarni pixel audit, Statistika i Pobjeda

Datum: 11. studenoga 2026. Deset vlasnikovih referenci dostavljeno je ponovno (devet ekranâ 941×1672 i branding ploča 1448×1086).

## Provjerena osnova

- Spojeni PR #129 (phase128), release [v1.12.211](https://github.com/bren-wp/Brojko/releases/tag/v1.12.211).
- Završeni stvarni Android 941×1672 emulatorski QA [42942720893](https://github.com/bren-wp/Brojko/actions/runs/42942720893) (uspjeh); originalni ZIP artefakt `Brojko-real-device-UI` #13820824717.
- Usporedba izvornog RGB bez skaliranja, registracije, lokalnog pomaka, normalizacije i retuširanja. MAE je srednja apsolutna razlika kanala od 0 do 255. **Nije postotak odstupanja**.

| Zaslon | RGB MAE prije faze 130 |
|---|---:|
| Početni izbornik | 14,6142 |
| Statistika | **14,3249** |
| Pobjeda | **14,2140** |
| Misije i postignuća | 13,8982 |
| Teme i izgled | 13,8620 |
| Dnevni izazov | 13,7624 |
| Klasično 4×4 | 13,6700 |
| Postavke | 11,5111 |
| Splash | 10,7615 |

### Visoko odstupajuće lokalne regije

| Regija | RGB MAE / 255 |
|---|---:|
| Postavke — backup/sinkronizacija (funkcionalno različito!) | 26,5501 |
| Pobjeda — kruna | 23,5472 |
| Pobjeda — tri akcije x160..790/y965..1390 | **20,6571** |
| Teme — kartice x40..900/y380..800 | **20,2323** |
| Klasično — donje kontrole | 20,0184 |
| Statistika — tri gornje KPI kartice | **19,4692** |

## Ciljana poboljšanja

1. **Statistika — grafika „Niz pobjeda”**: izvorni purpurni plamen ima raspon x556..648/y593..715, dok je stari generički Android plamen imao pretjeranu širinu x537..689, a vrh mu je počinjao tek oko y624. Na temelju izvornog vlasničkog motiva dodan je **nativni skalabilni Android Canvas Path** s otvorenim donjim utorom i lokalnim purpurnim gradijentom; samo za izvorni svijetli portretni Stats, a ne kao screenshot overlay. Statičke koordinate nisu korisnički podatci.
2. **Pobjeda — donji plavi gumb i narančasti rub**: izvorna plava površina završava oko y1377, dok je testni prikaz završavao tek oko y1388 (prevelik donji lip). Smanjena je **samo slikana** visina donjeg plavog gumba i njegov podložni rub, uz očuvanje istoga `Tap` pravokutnika i pravog Android dijeljenja. Glavni koraljni gumb dobiva kraći, dublje crveni rub koji je bliži izvorniku. Drugi gumbi/ostale teme nisu mijenjani.
3. **Instrumentacijska regresija**: `OwnerStatsVictoryPhase130Test` crta stvarni Android Canvas na 941×1672, provjerava šest plamenih/prozirnih uzoraka, obrub plavog gumba, sve tri žive Victory akcije, te da UI render ne mijenja broj partija ili rekord.

## Prije spajanja

- [ ] PR Android build, lint i unit
- [ ] Android 16 edge-to-edge / navigacija
- [ ] Emulator 941×1672 UI screenshot QA (novi i svi stari testovi)
- [ ] Preuzeti **novi** artefakt te usporediti cijeli Stats i Victory te njihove ROI s pre-faza-130 referencama, bez skaliranja.
- [ ] Vratiti lokalnu promjenu koja povećava grešku u važnoj regiji ili uzrokuje funkcionalnu regresiju.
- [ ] Provjeriti TalkBack i 320×480, 360×800, 1080×2400; ne mijenjati dinamičke bodove, postavke ni povijest.

**Napomena:** Postavke pokazuju lokalnu sigurnosnu kopiju, a dostavljena ilustracija sklopku cloud sinkronizacije. To nije moguće podudarno implementirati uz istinit UI bez stvarnog sinkronizacijskog backend-a, korisničkog pristanka i sigurnog protokola. Ne glumiti implementirani cloud.

**Status:** promjene su prijedlog. Nema deklaracije 0 % odstupanja; tek novi uspješan Android artefakt i ponovljena usporedba mogu potvrditi stvarnu vizualnu dobit.
