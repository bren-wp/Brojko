# BROJKO — faza 138: šest izvorno poravnatih zvjezdica nagrada

**Izvorne i stvarne slike:** vlasnikov PNG Misija `Slika ChatGPT 24. lis 2026. 22_15_16-7` i stvarna Android snimka `phase58-missions-941x1672.png` iz [izdanja v1.12.219](https://github.com/bren-wp/Brojko/releases/tag/v1.12.219). Oboje 941×1672 px, bez promjene veličine ili registracije.

Gold-pixel mask: R>200, 100<G<235, B<115, R>G+12; ROI x775–864, zasebni y-raspon za svaku zvjezdicu. Svi centroidi računati iz stvarnih RGB piksela.

| Nagrada | Izvorni y-centar | Android v219 y-centar | Korekcija, native Canvas jedinice |
|---|---:|---:|---:|
| Spoji 20 | 456,04 | 455,65 | +0,17 |
| Dosegni 512 | 633,61 | 639,34 | −2,44 |
| Igraj 7 | 928,35 | 937,91 | −4,06 |
| Ostvari 10.000 | 1105,53 | 1121,33 | −6,72 |
| Majstor | 1390,25 | 1408,35 | −7,69 |
| Kralj | 1556,16 | 1575,43 | −8,19 |

Promjena djeluje samo na `icon("star",...)` u izvornoj svijetloj portretnoj kompoziciji Misija. Zadržani su svi stvarni iznosi nagrada, stvarni rezultat/progres, touch zone, okvir pozadine, tekstualni centri, dark theme, landscape i ostali zasloni.

Novi `OwnerMissionRewardStarsPhase138Test` mjeri svih šest zlatnih ikona na Android Canvas renderu 941×1672 s maksimalnim vertikalnim odstupanjem ±5,5px. **Nije dokaz 0% ukupnog odstupanja.** Nakon novog CI-ja treba usporediti stvarne slike i RGB MAE istih šest ROI područja. Bez uspješnog CI-ja promjena se ne smatra provjerenom.
