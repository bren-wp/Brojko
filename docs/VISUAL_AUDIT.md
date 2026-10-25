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

## v1.9 — ispravak geometrije prema 941×1672 referenci

- Izvorne slike: 941 × 1672 px; Android emulator 1080 × 2400 px. Različit aspect ratio mijenja položaj i veličinu sadržaja.
- Na uskom 16:9 omjeru ekran igre zadržava puni promjer pločica, ali gornji rub ploče pomiče s 222 na 171 logičkih jedinica; pause/undo komande prelaze u desni stupac uz score.
- Gumbi Novo/Poništi/Završi prelaze na y=555 (ili 578 za dnevnu igru), a sekundarne akcije skrivaju se samo u kompaktnoj varijanti. To su funkcionalne prilagodbe, ne snimka zaslona pretvorena u lažno sučelje.
- Instrumentacijski test renderira 12 glavnih stanja na točno 941×1672 i provjerava završne rubove dodirnih zona. Rezultati se pohranjuju kao `reference-941x1672-*.png` u QA artefakt.
- Adaptive ikona: veličina foreground sloja 0.81→0.88; na dominantno B dodan dubinski obris, uz zadržavanje postojeće varijante vector ikone.
- **Preostale razlike**: marketing mockovi koriste potpuno popunjene demonstracijske ploče, specifične 3D ilustracije i vlastiti render tekstura. To nije moguće pouzdano izmjeriti kao savršen pixel-diff bez identičnih ulaznih stanja i bitmap slojeva.

## v1.9 — referentni kompaktni raspored postavki i pobjede

U dodatnoj vizualnoj kontroli primijećeno je da pri 941×1672 originalni raspored svih sedam redaka Postavki i footer logotipa zahtijeva previše visine, iako na visokom emulatoru sve izgleda uredno. Uski viewport sada koristi redove visine 57 jedinica i korak 63, s footerom na y=556–694. Prekidači i kartica jezika prate isti pomak i imaju najmanje 48 jedinica dodirne visine. Modal pobjede za uske ekrane zadržava sve tri radne akcije u jednome vidljivome okviru (panel y=181–684). Instrumentacijski test eksplicitno mjeri fizičke koordinate donjeg retka postavki i sva tri gumba pobjede, uz test ploče i tipografije.

## v1.9 — referentni raspored početnog ekrana, izazova i misija

Snimke na 941×1672 otkrile su da su donje četiri kartice početne stranice, nagrada dnevnog izazova i posljednja misija izlazile ispod granice referentne slike. U odgovoru na to dodana je sadržajna prilagodba za omjer stranica 16:9 (bez deformiranja fonta ili kartica): početni ekran raspoređuje rezultatske kartice, veliki CTA, tri moda i četiri prečaca unutar y=205–668 logičkih jedinica. Dnevni izazov koristi sažeti preview 4×4 i kraće kartice ciljeva te CTA do y=704. Misije su raspoređene u dnevne, tjedne i postignuća s preglednim progresom, svih pet stavki unutar y=161–655. Za visoke telefone zadržan je prostrani raspored.

UI test u izoliranom 941×1672 prikazu provjerava da sva četiri prečaca i glavni gumb izazova imaju dodirne zone unutar kadra. Za 1:1 provjeru boja, logotipa i 3D detalja potrebna je dodatna usporedba odgovarajućih referentnih PNG-ova sa stvarnim snimkama istog stanja i tolerancijama.

## v1.9 — kompaktne Statistike i Teme / 941 × 1672

Statistika sada ima dvije vrste rasporeda. Na kraćoj referentnoj visini pet metričkih kartica, prebacivanje grafikona, stupci do 10 partija i pet redaka povijesti smješteni su u y=145–704, uz funkcionalne dodirne zone za promjenu prikaza i povijest. Na višim telefonima ostaje postojeći prostrani raspored s većim grafom. U referentnom rasporedu onemogućen je nepotreban višak skrolanja za statistiku.

Zaslon Teme i izgled sada ima zaseban kompaktni raspored: 3 tematske kartice s oznakom odabira, preview svih 16 brojeva, tri stila pločica, aktivne prekidače zvuka i vibracije, slider animacija i sve četiri stavke donje navigacije unutar y=158–704. Izvorni visoki raspored ostao je za telefone omjera 20:9. QA snima realni Android renderer u 941×1672 i provjerava da donji tab Postavke stane na ekran te da je grafikon statistike interaktivan.

## v1.10 — Dnevni izazov i Misije, nagrade i ikone

- Izvorne 941×1672 referentne snimke: donji dio Dnevnog izazova sadrži veliki ljubičasti poklon, naziv nagrade i istaknut CTA; prethodna aplikacija imala je samo usku traku s jednom zvijezdom.
- Compact 16:9 preview ploče blago je smanjen (236→221 projektnih jedinica), a visina 3 cilja 59→51, čime se dobiva 80 jedinica visine za karticu nagrade bez skrivenog CTA gumba.
- Dodana je vlastita SVG-like vektorska ilustracija ljubičaste poklon-kutije i zlatne vrpce izravno u Android Canvas ikonografski sustav, uz čitljiv natpis i stvarno stanje osvajanja (250 zvjezdica jednom dnevno).
- Misije imaju tri odvojena, tematski obojena pozadinska panela. Svi paneli dobivaju tamne ekvivalente na tamnoj temi. Progres i nagrade ostaju nepromijenjeni i stvarno se računaju.
- Ikone redova razlikuju spajanje (mreža), dnevne bodove (meta), kontinuitet igranja (kalendar), 1024 (mreža) i rekord (kruna); osvojeni cilj ostaje označen kvačicom.
- Emulator QA snima dodatno dnevni izazov, misije, statistiku i teme u sve tri teme. Preostale razlike uključuju ilustrativni 3D glow, sjene i fotografski izgled mockupova, za što bi trebale zasebne originalne grafičke datoteke.

## v1.10 — dovršetak zaglavlja dnevnog izazova

Zaglavlje dnevnog izazova sada koristi veliki dvoredni naslov **Dnevni / izazov** bez dodatnog logotipa, izdvojeni flame/streak panel s brojem uzastopnih dana, odvojenu datumsku kapsulu i koraljne zrake uz primarni gumb. Na prostranim telefonima ostaje i drugi motivacijski red, dok se na referentnom 941×1672 izostavlja radi preglednosti. Nije dodan novi hardkodirani datum: prikazuje se stvarni datum uređaja.

## v1.11 — gumbi, siguran dodir i launcher

Sve Canvas tipke i navigacijske kartice sada dijele povratnu informaciju na pritisak. ACTION_UP aktivira samo izvorno dodirnutu kontrolu kada je otpuštena unutar nje, dok pomicanje sadržaja i ACTION_CANCEL prekidaju interakciju. Regresijski instrumentacijski test pokriva otpuštanje izvan gumba i valjan klik. Za Android 13+ dodana je monokromatska tematska ikona s četiri pločice i dominantnim B te zasebna okrugla varijanta; obojena ikona zadržana je. Ove promjene ne dokazuju pikselnu jednakost 3D mockupu.

### Test ikone na stvarnom Android launcheru

Novi Android UI test renderira adaptivnu i okruglu ikonu u PNG 512×512 te na Androidu 13+ i monokromatski sloj pod tematskom bojom. Test zahtijeva dostupne slojeve resursa i neprozirnu jezgru ikone; PNG snimke ulaze u GitHub Actions UI QA artefakt zajedno s 941×1672 prikazima svih ekrana. Time se dokazuje da launcher resursi nisu samo prisutni u kodu, nego se i mogu crtati na uređaju.

## v1.12 — detaljna usporedba devet referentnih prikaza

Izvorno dostavljeno devet ekraniziranih referenci plus zasebna brand ploča uspoređeni su s 941×1672 stvarnim Android QA snimkama v1.11.38. Prepoznata je jasna geometrijska razlika u Postavkama: nazivi su bili centrirani, dok original počinje poravnat s lijevim rubom teksta na x≈94 logičke jedinice. Ispravljeno je zajedničkom funkcijom za lijevo crtanje svih naziva te redova s dodatnom vrijednosti. U kompaktnim Statistika naslov je prevelikim odmakom djelovao presitno; sada se crta zasebni naslov od 47 jedinica ispod kompaktne brand oznake. Svaka numerička pločica koristi dodatni sjaj na gornjem lijevom rubu i ugrađenu sjenu/debljinu.

**Launcher audit**: Stvarni Android adaptive render v1.11.38 zaklanjao je donje kutne brojeve masivnim B. B i njegov dubinski sloj smanjeni su na 84% unutar postojeće podloge, na sve četiri vektorske varijante (legacy, legacy-round, adaptive foreground i drawable). Provjeriti u naknadnom emulator UI zipu PNG slike adaptive/round/themed (sada već postoje u regresijskom testu).

**Granica provjerljivosti**: reference prikazuju namještene odigrane ploče i statistiku, dok emulator koristi čiste ili programatski proizvedene podatke. Brojke, pločice i povijest moraju ostati stvarne i ne smiju se lažirati radi podudarnosti. Potpuna 1:1 jednakost 3D slika i tekstura nije utvrđena.

## v1.12 — posljednja revizija proporcija launcher brojeva

Snimka adaptive ikone 512×512 iz stvarnog emulatora v1.12.39 otkrila je da 8 i 16 još izlaze ispod bijelog slova B i da je broj 2 razmjerno velik. Brojevi su pomaknuti prema vanjskim kutovima i skalirani neovisno (83%, 83%, 80%, 77%) na svim obojenim vektorskim varijantama. Logo B i pozadina zadržavaju prethodni reljef. Emulator provjerava prikaz, ali izvorna 3D ikona i dalje se ne može zvati identičnom vektoru bez izvornog raster asseta.


## v1.12 — pregled stvarnih emulator snimki i ispravci sučelja (25. 10. 2026.)

Pregledan je `Brojko-real-device-UI` artefakt prethodnog uspješnog emulator workflowa s 68 snimki, uključujući sve primarne ekrane, 3 teme, Android launcher ikone i referentni viewport 941×1672. Uočena su odstupanja koja se mogu dokazati iz izvornog Canvas rasporeda:

- Odabrana tema imala je oznaku potvrde ispod donjeg ruba kartice; indikator je sada pozicioniran unutar kartice u punom i kompaktnom rasporedu.
- Kompaktne kartice za načine igre sada imaju veći donji razmak ispod podnaslova; tekst ostaje čitljiv bez preklapanja susjednih kartica.
- Ikonički gumbi Natrag, Pauza i Poništi dobili su opisna hrvatska pristupačna imena i minimalnu interaktivnu zonu 54×54 dizajnerske jedinice; vizualna veličina ikona ostaje vjerna dizajnu.
- Poruka nakon ponovljenog dnevnog izazova više ne tvrdi da je dodijeljeno novih 250 zvjezdica. Podatak o dodjeli je UI-only; trajno stanje nagrada ostaje zaštićeno postojećom provjerom `dailyRewardDate`.
- Dodan je instrumentacijski regresijski test za opise i veličine zona ikonografskih gumba, uz proširenje provjere dnevne nagrade.

Ova revizija **nije potvrda apsolutne 1:1 podudarnosti**. Izvorni dizajnerski PNG-ovi još nisu u repozitoriju kao golden reference; za pixel-diff treba iste ulazne podatke, prikaz i izvorne grafičke slojeve. Testovi i novo izdanje ovise o uspješnom CI prolazu nakon commita.


## Dodatna dorada nakon pregleda 70 snimki v1.12.41

Emulator QA za v1.12.41 završio je uspješno. Na stvarnim snimkama potvrđeno je da su oznake teme unutar selekcijskih kartica i da novi testni prikazi pokrivaju ikonografske akcije. U modalu uspješnog dnevnog izazova posljednji gumb bio je poravnat uz sam donji rub panela; primarna i sekundarna akcija su podignute uz 18 dizajnerskih jedinica slobodnog prostora. Ovaj razmak sada provjerava regresijski test. Android launch tema na API 23–30 i API 31+ eksplicitno postavlja krem pozadinu `#FFF8EE` prije prvog Canvas kadra i koristi jedinstvenu koraljnu `#FF7A5B` boju naglaska. Time se smanjuje rizik bijelog početnog bljeska; snimka prvog stvarnog prikaza na različitim uređajima i dalje ostaje dio završnog manualnog QA.


## v1.12 — nastavak: provjera svakog zaslona, ikone i dodirnih zona

Na stvarnom Android emulator artefaktu v1.12.42 pregledane su snimke Splash, Home, 4×4/5×5 gameplaya, pobjede/poraza, dnevnog izazova, misija, statistike, povijesti, tema, postavki, jezika, sigurnosne kopije, privatnosti, informacija o aplikaciji i sve tri teme. Identificirana su i ciljano ispravljena tri nova odstupanja:

- **Launcher ikona**: u adaptive foreground i svim legacy/round vectorima središnji B više ne prekriva brojke 8 i 16. Motiv i paleta ostaju isti, a središnji znak smanjen je i pomaknut kako bi četiri broja ostala čitljiva na kružnoj maski. Potrebna je vizualna kontrola novih CI snimki prije potvrde konačne kvalitete.
- **Odabir stila**: svi thumbnaili sada pozivaju renderer s izričitim stilom (Zaobljene/Ravne/Staklaste); ranije su sva tri slučaja bila nacrtana prema samo trenutno odabranom stilu. Postojeća igra i spremljene korisničke postavke ostaju neizmijenjene.
- **Statistika**: pet redaka povijesti dobiva 54 jedinice visine umjesto 27, s nenametljivim razmacima; `maxScroll` raste ovisno o broju rezultata. Dodana su dva regresijska emulator testa, za stvarno različite thumbnail renderinge i pet nepoklapajućih dodirnih zona.

Ne izvoditi zaključak o 1:1 pikselnoj identičnosti bez izvornih referentnih PNG-ova i odgovarajućih golden diffs.


## Regresijski popravak dnevnog poraza i dead-code provjera

Nakon detaljnijeg pregleda svih modalnih stanja pronađeno je da završetak dnevnog izazova zbog potpuno blokirane ploče prikazuje generički modal poraza, dok poseban modal dnevnog poraza tvrdnjom o 80 potrošenih poteza pogrešno objašnjava svaki neuspjeh. Sada dnevna partija uvijek vodi u odgovarajući rezultat, uz točnu poruku o preostalim potezima ili blokiranoj ploči. Dodan je Android regresijski test za potpuno blokiranu dnevnu ploču u 12. potezu. Dva nekorištena privatna Canvas pomoćna renderera (`scoreCard` i `switchRow`) uklonjena su nakon provjere odsutnosti pozivatelja. Nisu mijenjani trajni korisnički podatci ni javne funkcionalnosti.


## Završna korekcija launcher vektora prema stvarnoj 512×512 Android snimci

Pregled adaptivne ikone nakon promjene proporcija središnjeg B u izdanju v1.12.43 otkrio je preostalu pogrešku: broj 16 prikazivao je konturu nalik na 12, dok je osmica imala ukrižene otvorene petlje. Četiri međusobno usklađene color-vector datoteke sada imaju ispravljenu šesticu s donjom zatvorenom petljom i dvije pravilno složene petlje osmice. Nije generirana rasterizirana ikona, nije promijenjen Brojko motiv ni paleta; Android simulator kroz postojeći QA ponovno snima adaptive i round varijante. Potrebna je dodatna stvarna vizualna potvrda, a ne samo prolazak builda.


## Mikroispravak kontrola animacije — obje visine zaslona

Natpisi Niska/Srednja/Visoka sada imaju uravnotežen razmak unutar kartice na standardnom i kompaktnom ekranu Teme i izgled. Tri dodirne zone su nepoklapajuće (208–261, 262–315, 316–378 dizajnerskih jedinica); položaji gumba i aktivna spremljena razina ostaju nepromijenjeni. Dodan je instrumentacijski test obje geometrije zaslona.


## Potpuno snimanje sva tri načina igre

UI workflow sada snima **stvarna stanja** sva tri načina igre, ne samo generičku klasičnu ploču: Klasično 4×4, Opušteno 5×5 i Dnevni izazov 4×4. Za svaki se stvara stvarni emulator screenshot i posebna referentna snimka renderera na 941×1672. Test mjeri broj ćelija, kvadratni izgled mreže, granice vidljivog boarda i postojanje aktivne kontrole Završi. Ti QA snimci ne podrazumijevaju da su referentni marketinški PNG-ovi učitani ili da je 1:1 usporedba provedena.


## Android launcher mask safe-area — završna geometrijska korekcija

Emulator snimka v1.12.45 pokazala je ispravne nove konture 8 i 6, no njihovi donji potezi i dalje su bili odrezani kružnom launcher maskom. Stoga je donja lijeva brojka pomaknuta za +6 po x i −7 po y, a donja desna za −9 po y u izvornim vektorskim koordinatama prije adaptive skaliranja. Zadržani su isti oblik B, sva četiri polja, njihove boje i zaobljenost. Ovaj zahvat cilja stvarni Android mask clipping, ne samo kozmetičko smanjivanje ikone; provjeru treba potvrditi novom emulator snimkom.


## UI/UX dorada nakon pregleda 78 stvarnih QA snimki (25. 10. 2026.)

Ponovno su vizualno pregledani početni ekran, klasični i opušteni 4×4/5×5 način, dnevni izazov, modali, misije, statistika, teme, postavke, pomoćni zasloni i sve varijante ikone iz v1.12.48. Utvrđena je nesukladnost gornje ikone Poništi (prikazivala se aktivno čak i kad ne postoji potez za poništavanje), što je ispravljeno zajedničkom funkcijom `headerUndo`. Sada je onemogućena i vizualno i semantički, bez lažnog dodirnog cilja, dok se nakon prvog valjanog poteza aktivira. Instrumentacijski test provjerava oba stanja.

Na zaslonu Teme i izgled tri prikaza stila dobila su vidljivi broj 2, jednako kao pločice u samoj igri; stvarni izbor stila i prethodno spremljene postavke nisu mijenjani. Primjer u Kako igrati redizajniran je u preglednu jednadžbu **2 + 2 → 4**, s međusobno nepoklapajućim pločicama i znakovima. Novi Android instrumentacijski test sprema screenshot sva tri odabrana stila, provjerava zone dodira i snima novi prikaz pomoći. Ovo nije tvrdnja o automatski dokazanoj apsolutnoj 1:1 identičnosti u nedostatku usporedivih izvornih golden PNG datoteka.


## Regresijska pokrivenost svih 21 zaslona na malom Android uređaju

Za sve 21 podržano stanje (`splash`, početni, tri modalna završetka igre, potvrde, dnevni izazov, misije, statistiku i povijest, teme, postavke te pomoćne stranice) UI instrumentacija sada generira zasebne 360×600 PNG snimke. Za svaku stvarno interaktivnu površinu računa se dohvatljivost vertikalnim pomicanjem od 0 do `maxScroll` uz dopuštenu širinu dizajna 400 jedinica. Time su pokrivene geometrijske regresije koje nisu vidljive na visokom Pixel 6 emulatoru. Vizualna 1:1 usporedba s izvornim dizajnerskim PNG-ovima i dalje nije moguća bez usporedivih datoteka i istih stanja igre.


## Konstantan Brojko logotip kroz sva tri stila igre

Otklonjena je ovisnost brendiranog kvadrata 2 u logotipu te ukrasnih pločica splash/početnog zaslona o postavci stila igraće ploče. Brendirane ilustracije sada zadržavaju postojeći reljefni izgled, dok opcije Zaobljene, Ravne i Staklaste i dalje mijenjaju stvarnu igru i preglede stilova. Novi Android instrumentacijski test uspoređuje sve piksele splash renderera za sva tri stila i arhivira odgovarajuće snimke; time se čuva vizualni identitet pri promjenama korisničkih postavki.
