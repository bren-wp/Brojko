# BROJKO — faza 137: ciljano izjednačavanje tonova Misija

Datum: 12. studenoga 2026. Razvoj direktno na `main`, bez Pull Requesta.

## Izvorni pixel-by-pixel baseline iz v1.12.219

Reference vlasnika i stvarne snimke Android emulatora, obje 941×1672, bez skaliranja.

| Zaslon | RGB MAE (0–255), prije faze 137 | Točno isti RGB pikseli |
|---|---:|---:|
| Misije i postignuća | 13,8165 | 1,3398 % |
| Teme i izgled | 13,4282 | 4,5254 % |
| Naslovnica | 12,4051 | 3,5428 % |

Stvarni emulator: [Android CI v1.12.219](https://github.com/bren-wp/Brojko/actions/runs/43133781991), dokazni PNG-ovi `phase58-missions-941x1672.png`, `themes-original-final-aligned-941x1672.png`, `owner-home-12560-3864-941x1672.png`. Originali: korisnikovi PNG-ovi `Slika ChatGPT 24. lis 2026. 22_15_16-7`, `22_15_18-9`, `22_15_13-3`.

## Regije najvećeg odstupanja

Misije: srednji stupac x313..626/y836..1254 imao je RGB MAE **20,81**, srednji stupac y418..836 **19,18**. U tim regijama su tekst, napredak, ilustracije i različite vrijednosti pravih rezultata pa se cijeli ROI ne može popraviti samo jednim pigmentom. Nije dopušteno lažno prikazati `4/20` ili obojeni napredak kao da je stvarno osvojen.

Stvarne ravne RGB zakrpe, medijan 11×11:

| Podloga / koordinata | Original | v1.12.219 Android | Očekivana korekcija |
|---|---|---|---|
| Tjedna plava, x320/y830 | 232/246/254 | 237/247/255 | malo manje crvene |
| Postignuća zelena, x350/y1300 | 223/251/237 | 235/252/242 | više zelene, manje crvene |

Promijenjen je samo ulazni pastel gradijenta `missions()` u svijetlom izvornom portretu. Dodan instrumentacijski test koji uspoređuje oba stvarna Android RGB područja s referencom, tolerancija ±8 po kanalu. Ostali oblici, navigacija, hit-zone, dinamičke vrijednosti, tema 1 i responsivni prikazi ostaju netaknuti.

## Status

Promjena je kandidat za mjerenje, **nije dokaz 0 % ili smanjenog RGB MAE**. Nakon uspješnog novog Android QA izdanja potrebno je izvući stvarne PNG-ove i usporediti iste ROI-je i puni ekran. Povrat koraka ako se pogoršaju. Potpuna 1:1 jednakost znači točan svaki RGB kanal svakog piksela i zahtijeva usklađivanje fontova, ilustracija, sjena i funkcionalnih stanja.
