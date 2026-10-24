# Brojko — vizualni audit prema 10 dostavljenih slika (25. 10. 2026.)

## Izvorni dizajn
Referentni PNG-ovi uključuju: brending/Play ikonu, splash, početni izbornik, klasičnu ploču, čestitku 2048, dnevni izazov, misije/postignuća, statistiku, teme i postavke. Većina referentnih portretnih slika je 941 × 1672 px (omjer 0,5628), dok Pixel 6 emulator koristi 1080 × 2400 px (omjer 0,45). To znači da se istodobno ne mogu zadržati apsolutno identični pikseli i raspored bez praznih područja ili rezanja na svakom uređaju.

## Izmjene u v1.4
- Splash: veliki brojčani 3D motivi, crveni kvadrat 2 u Brendigo natpisu, slogan u krem okviru.
- Glavni ekran: početni logotip, slojevite pločice oko logotipa, naglašeni koraljni CTA s obrubom, 3 pločice načina 4×4 / 5×5 / dnevni izazov, 4 kartice s opisima i 3D ikonama.
- Koncentrirane brand boje: koraljna `#FF7A5B`, tirkizna `#2EC4B6`, tamna `#1E2A44`, krem `#FFF8EE`.
- Gumbe: prilagodljiv polumjer, unutarnji svijetli obris i donja sjena.
- Ikone: samostalne vektorske ilustracije krune, zvjezdica, mete, statistike, boja, zupčanika, kalendara, reprodukcije, stanke i potvrde.
- Postavke: 7 redaka poput reference sa zvukom, vibracijom, jezikom, opcionalnim lokalnim obavijestima, kopijom napretka, privatnošću i informacijama.
- Brendirana završna kartica s dinamičkom verzijom aplikacije. Nema lažnih tvrdnji o cloud sinkronizaciji: odabir "Sinkronizacija napretka" otvara eksplicitni lokalni izvoz/uvoz.

## Ispravak nakon emulator testa v1.4.16
Emulator je otkrio sistematsko odstupanje vektorskih ikona: canvas je koristio gornji lijevi kut umjesto središta ikone. Zajednička funkcija za crtanje sada koristi `(centerX-width/2, centerY-width/2)` u svim slučajevima. Usklađeni su vertikalni razmaci ikonice/naslova/opisa u četiri početne kartice. U footeru postavki uklonjen je dupliciran potpis, a Android launcher sada ima `adaptive-icon` s dovoljno sigurnog odmaka od maske.

## Nedovršeno za dokaz 1:1 podudarnosti
- Referentne slike su marketinške makete sa simuliranim rezultatima, datumima i 4×4 pločom već punom brojeva; aplikacija koristi stvarna dinamička stanja i počinje sa samo dvije pločice.
- Nije ugrađena identična font datoteka iz reference. Android zamjenski font `sans-serif-rounded` odstupa po širini i težini znakova.
- Sjaj, reljef, omekšane sjene i ilustracije su aproksimacija Android Canvas vektorima, a ne originalni grafički slojevi.
- Za objektivan pixel-diff potrebno je imati odobrene "golden" reference normalizirane na isti viewport i iste testne vrijednosti bez sustavnog status bara.
- Produkcijski AAB mora biti potpisan stvarnim upload ključem; uređajni QA ne dokazuje produkcijsko odobrenje u Google Playu.

## Ponovljivi QA postupak
- Emulator workflow prikazuje i snima splash, home, game, 5x5, daily, dailyWin/dailyFail, victory, themes, missions, stats, settings, help, privacy.
- Testirati sve akcije na različitim ekranima, uključujući opcionalnu dozvolu obavijesti i ponovno pokretanje uređaja.
- Usporediti slike istih stanja pri istim dimenzijama: margin, typographic baseline, position/size for cards, icons, buttons and gradient tones.
- Ne deklarirati pixel-perfect dok nisu pregledane snimke i dok nema mjerljivih tolerancija i potvrđenih golden referenci.
