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


## Revizija v1.5 — pregled svih glavnih ekrana

| Zaslon | Provedena izmjena | Daljnji QA |
|---|---|---|
| Launcher | Korekcija boja, sjaja i safe-zone adaptivne ikone | Test Pixel/Samsung launchera |
| Splash | Zadržan brendirani dinamički prikaz | Provjera omjera 20:9 |
| Početni | Tri moda + četiri povezana prečaca, karta rezultata | Vizualna usporedba fonta i sjena |
| Klasično/Opušteno | Zaglavlje sa score podacima, glavni gumbi i realni board | Provjera gesti 4×4/5×5 |
| Pauza/Pobjeda/Poraz | Pobjeda s tri funkcionalna gumba | Test resume/undo/new |
| Dnevni izazov | Ikonice, kapsula datuma, pregled, tri cilja, reward +250 | Test ponoći i isteka poteza |
| Misije | Progres + stvarne nagrade | Test prijenosa dana |
| Statistika | Kontrastne metric kartice, grafikon Bodovi/Pločica, 5 povijesnih redova, detalji partije | Test duge povijesti |
| Teme | 3 pregleda, pločice, zvuk/vibracija, interaktivna jačina animacija, donja navigacija | Provjera aktivnih stanja |
| Postavke | Pomaknute kartice ispod zaglavlja, uredan footer | Test obavijesti/backup |

**Napomena:** Jednostavni Android vektori i sistemski rounded font ne mogu jamčiti identične piksele ilustrativnoj raster-maketi. Nema tvrdnje o verificiranoj 1:1 podudarnosti bez diffa snimki istog stanja i istog viewporta.

## Revizija v1.6 — usklađivanje prema originalnim referencama

Provjeren je svaki primarni vizualni sustav: launcher i splash, početna, klasična/opuštena ploča, pauza, pobjeda/poraz, dnevni izazov, misije, statistika i povijest, teme, postavke te pomoćni prikazi.

- Zajedničke kartice: dvije dubinske sjene, mekan vertikalni gradijent i tanki unutarnji svijetli rub.
- Zajedničke pločice: dimenzijama prilagođen donji reljef, varijabilan sjaj i obris, bez crtanja negativnih rect dimenzija pri animaciji.
- Naglašeni fontovi: deblji rounded font, kalibracija širine teksta i suptilna sjena za veće naslove.
- Igre 4×4/5×5: veća ploča, više mjesta za brojke, razlikovni simbol „Novo”.
- Sekundarna zaglavlja: dvobojna Dnevni izazov, Teme i izgled, Misije i postignuća; zasebni naslov Postavke bez dodatnog logotipa.
- Kompaktni viewport: game board zadržava širinu i geste, dok se izvan ploče može vertikalno doći do donjih kontrola.
- QA: instrumentacijski test na 360×600 mjeri stanje ploče i skrolanje te snima sve ključne ekrane.

**Granica 1:1:** originalne marketinške reference prikazuju fiktivne vrijednosti i drukčiji omjer stranica od Pixel 6 Android emulatora. Za strogi pixel-diff treba ista rezolucija, kontrolirana početna ploča i identičan font/izvorna grafika. Bez toga podudarnost svih piksela nije moguće istinito potvrditi.

## Revizija tema v1.7.24

Prema referenci, donja navigacijska traka na ekranu **Teme i izgled** nalazi se unutar standardne visine zaslona, bez potrebe za pomicanjem na telefonima s dovoljno prostora. Uklonjen je suvišni gumb koji je gurao navigaciju izvan ekrana, te su dodane odabrane konture i potvrda na aktivnoj temi. Promjena se provjerava instrumentacijskim testom: sva 4 taba u koordinatama y=805–877 te stvaran prijelaz na Postavke. Niži zasloni i dalje koriste vertikalno pomicanje.

## Revizija Nunito težina v1.7.25

Testni ekran v1.7.23 dokazao je da sam izvorni variable Nunito nije dovoljan: Android je neke težine iz `Typeface.create(base, weight, ...)` prikazivao pretanko. Ispravljeno je eksplicitnim `Typeface.Builder` s osi `wght` (500/850/1000) i postupnim zadebljanjem prikaza teksta. Na starijim API-jevima koristi se rezervna obitelj/fake bold. CI instrumentacijski test provjerava da je `displayFont` doista teži od običnog teksta.

## Nunito v1.7.26 — rezultat vizualne provjere

Na screenshotovima v1.7.25 težina 1000 uz `fakeBold` i `FILL_AND_STROKE` prenaglašavala je slova i smanjivala čitljivost. Uklonjeno je umjetno zadebljanje, a težine Nunito osi `wght` standardizirane su na 500 za tekst, 750 za podebljane natpise i 900 za display naslove, logotip i brojke. To je izravna korekcija prema referentnoj tipografiji, uz potrebu ponovne usporedbe snimki.

## Sigurni vidljivi prostor i dodirni ciljevi — v1.8

QA snimke v1.7.26 otkrile su da je na Android uređaju 1080×2400 px navigacija tema bila odrezana na dnu jer se tema bez potrebe skalirala prema punoj širini uz neuračunate vertikalne sistemske insets. Sada se normalni zaslon tema skaluje prema minimalnoj dimenziji **stvarno upotrebljive površine**, jednako kao većina ostalih ekrana. Niži zasloni koriste pomicanje. U Postavkama prikaz jezika dodan je zaobljen dropdown s ikonom strelice. Instrumentacijski QA uspoređuje donju koordinatu stvarne navigacijske dodirne zone s krajem vidljive površine i provjerava minimalni touch target. Vizualne reference ostaju cilj, a ne tvrdnja o dokazanoj pikselnoj jednakosti.

## Ikone statistike — dodatna revizija v1.8

Kartice statistike sada koriste značenjski i vizualno dosljedne vektorske simbole iz izvornog dizajna: krunu za rekord, gamepad za broj partija, mrežu četiriju kvadrata za najveću pločicu, zvijezdu za prosjek bodova i plamen za niz igranja. Svi su nacrtani u zajedničkim 24×24 lokalnim koordinatama i istom sustavu boja, skaliranja i poravnanja. Prikaz treba provjeriti na svijetloj i tamnoj temi u snimkama emulatora, uključujući uvećani prikaz.
