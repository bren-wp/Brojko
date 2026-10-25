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


## Splash nije skrolabilan: potpuna vidljivost na kratkim zaslonima

Pregled 21 dodatne screenshot slike s viewportom 360×600 otkrio je da splash do automatskog prijelaza na početnu stranicu prikazuje samo dio dekoracije i logotipa, dok slogan može biti ispod vidljivog područja. Splash se sada ne tretira kao skrolabilni kratki sadržaj: cijeli dizajn visine 880 jedinica prilagođava se stvarnoj vidljivoj visini, uz horizontalno centriranje. Uklonjena je potreba da se na uvodnom zaslonu pomiče sadržaj koji automatski nestaje. Instrumentacijski test provjerava y-granicu slogana i izostanak skrolanja za 360×600, 941×1672 i 1080×2400.


## Reference 941×1672: obnovljeno svih šest izvornih misija (2026-10-25)

Korisničke izvorne PNG reference pokazuju dvije dnevne, dvije tjedne i dvije kartice postignuća. UI je usklađen, uključujući dnevni cilj 512 bez Undo i tjednih 10.000 bodova, sa stvarnim dedupliciranim nagradama i trajanjem po danu/tjednu. Premješteni su svi redci, naslovne trake, ikone i progresne trake u kompaktni i visoki raspored. Lokalno su dodana polja za weekPoints, weekStart i undoUsedThisGame; prethodne sigurnosne kopije ostaju kompatibilne jer su novi ključevi neobvezni.


## Referentne PNG slike: tipografija splasha, raspored tema i sigurna zona ikone

Sada dostupne izvorne slike pokazale su da je središnji Brojko logo na splashu trebao biti znatno veći i da je slogan bio previsoko pri dnu. Splash je usklađen povećanjem brand bloka (77→108 dizajnerskih jedinica) i pomicanjem slogana 43 jedinice prema gore. Oznake tri teme sada su radio-badge ispod numeriranih 2×2 pločica, a nazivi ispod kartica kao u referenci. Referentni lijevi preview: legenda se nalazi iznad mini mreže 4×4, pa su raspored mreže i središnji panel recalibrirani bez preklapanja ikona, radijskih kontrola i donjih tabova. Adaptive foreground launcher vektor smanjen je na 75% radi sigurnog kružnog Android maskiranja i provjere čitljivosti četiri broja. Zadržane su postojeće interaktivne zone, sve palete tema i pohranjeni izbor.


## Izvorna ikonografija referentnih ekrana

Kada je izvorni PNG dostupan, njegovi detalji imaju prednost pred prethodnim generičkim UI pretpostavkama: tri male kartice stilova Zaobljene/Ravne/Staklaste prikazuju **praznu obojenu pločicu**, a ne broj 2. Oba theme prekidača koriste koraljnu aktivnu boju kao izvorna slika (za razliku od glavne stranice Postavke, koja koristi tirkizne prekidače). Dnevni izazov sada razlikuje ružičastu pločicu 256, zlatnu zvijezdu i plavu ikonu za poteze. Misije prikazuju vlastite male numerirane pločice za spajanja 2/4, pločicu 512 bez Undo i četiri pločice za Majstora spajanja, umjesto generičkih meta na svim karticama. Sav proračun ciljeva i nagrada ostaje isti kao u prethodnom commitu.


## Referentna stanja radi stvarne provjere 1:1 na 941×1672

U Android testove uvedena su reprezentativna, **samo testna** stanja iz originalnog screenshot dizajna: klasična 4×4 ploča s 16 zadanih brojeva i rezultatom 1.316, pobjednički modal te statistika s 10 uzoraka, rezultatom 12.560 i 248 partija. Stvarna korisnička pohrana se nakon testnih snimki vraća i ne mijenja. Dodani su brojevi bodova iznad svih stupaca grafikona u oba layouta, jer se vide na referenci, a dosad su nedostajali. Logo ima više unutarnjeg prostora između slova s descenderom i natpisa by Brendigo. Ovi testni screenshotovi služe za usporedbu s korisničkim izvorima, no same po sebi ne dokazuju jednakost rasterskih piksela.


## Sekcijski headeri i stvarne periodičke oznake u Misijama

Originalna referenca pokazuje neovisne piktograme i satne oznake u Dnevnim i Tjednim misijama. Dodani su izvoru bliži simboli za dnevne/tjedne ciljeve te mali neutralni satni badge s stvarnim vremenom do sljedeće lokalne ponoći ili idućeg ponedjeljka. Ukupan broj skupljenih zvjezdica premješten je u naslovni dio Postignuća, tako da više ne prelazi preko prve dnevne kartice. Vrijeme nije hardkodirano i ne tvrdi da je cloud sinkronizacija omogućena. Izračun se temelji na lokalnom vremenu telefona i prikazuje se pri idućem iscrtavanju zaslona.


## Stvarni usporedni screenshot: brojke Najbolji/Rezultat

Testno stanje 4×4 prema dostavljenom izvorniku (rezultat 1.316, najbolji 3.864) otkrilo je da dva broja imaju prevelike natpisne širine te se gotovo spajaju. Igra sada računa kompaktnije, nepoklapajuće score natpise u obje varijante headera. Testna referentna ploča dobiva stvarnu spremljenu Undo povijest prije injektiranja 16 brojeva, tako da gumb Poništi u njezinoj QA snimci ima aktivni izgled kao u originalnom dizajnu. Stvarni engine i spremljene korisničke partije nisu mijenjani.


## Korekcija sekcijskih ikona nakon konačne usporedne QA snimke

Završna PNG snimka 941×1672 pokazala je da su novi simboli sekcija preklopili centrirane riječi Dnevne, Tjedne i Postignuća. Centri naslova pomaknuti su udesno, a vremenske i reward značke unutar sigurnih granica iznad redaka. Sunce na dnevnoj grupi više nije zvjezdica, a tjedni zadatak Igraj 7 dana koristi stvarni mali kalendar s čitljivom sedmicom, umjesto zvjezdice preko brojke. Kad je do ponedjeljka manje od 24 sata, odbrojivač prikazuje samo sate, a ne zbunjujući oblik 0d 21h. To su ciljani vizualni popravci zasnovani na usporedbi s izvornim PNG-ovima i stvarnim Android QA snimkama.


## Nastavak usporedbe 10 originalnih PNG-a — rub klizača, opis tema i lokalna kopija

Nakon usporedbe sa stvarnim emulator snimkama v1.12.60 utvrđeno je da klizač razine animacije Visoka u oba dizajnerska rasporeda prelazi preko desne granice kartice: lijevi rub bio je 368, a kružić je dosezao 391 (>380). Nova geometrija koristi kružić između x=211 i x=370, uz dosljedno punjenje staze i tri nepoklapajuće dodirne zone. Instrumentacijski test provjerava koraljni piksel unutar centra i izostanak prolijevanja izvan kartice na 400×880 i 941×1672. Opis lijevog pregleda stilova vraćen je u dva retka iznad mreže (umjesto sićušnog jednorednog teksta); mreža je spuštena bez ulaska u zvučne kontrole. Redak Sinkronizacija napretka otvoreno pokazuje sekundarni tekst Lokalna kopija i novu strelicu za navigaciju u sigurnosnu kopiju, bez lažnog aktiviranja cloud sinkronizacije. Provjereno da se otvara stvarna stranica Sigurnosna kopija.


## Dnevni izazov — funkcionalna pomoć i tekst prema izvorniku

Originalni PNG referentnog dnevnog izazova koristi lijevo poravnanje naziva i opisa sva tri cilja, vlastitu ikonu 256/zvijezda/dvosmjerna strelica te mali informacijski znak desno od svakog reda. Prethodni prikaz crtao je sva tri opisa centrirano i nije sadržavao funkcionalne informacije o cilju. Sada sva tri cilja imaju tekst na stalnom x=105, vlastitu namjensku pomoćnu dodirnu zonu 54×51/72 dizajnerske jedinice s TalkBack nazivom te stvarnu Toast poruku opisa cilja. Dvosmjerna strelica zamjenjuje generičku Refresh ikonu, a datum na dnevnom izazovu sada koristi puni hrvatski naziv mjeseca i razmak nakon ikone. Nisu mijenjani datumi obračuna, trajni napredak i game engine. Dodan je instrumentacijski test sva tri nova pomoćna cilja i stvarne lokalizacije datuma.


## Kompaktna statistika nakon usporedbe izvorne 941×1672 slike

Izvorna referenca prikazuje svih pet redaka povijesti na istom zaslonu, a dosadašnji renderer ih je smjestio ispod donjega ruba. Rekalibrirana su dva retka KPI kartica, visina stupčastog grafikona i početak povijesti tako da pet međusobno različitih 48-unit dodirnih zona stane u y=466..706 bez pomicanja na originalnoj rezoluciji. Kartice visoke 44 jedinice imaju vizualni međurazmak, dok se 360×600 i dalje skrola prema stvarnom sadržaju. U testu se sada izričito potvrđuju svi redci, nulto skrolanje na 941×1672 te screenshot novog stanja. Zbijenim KPI karticama smanjena je veličina vrijednosti, čime se sprječava njihovo preklapanje s drugom linijom naslova.


## Postavke — zasebna 1:1 revizija referentnog ekrana (25.10.2026.)

Ovaj prolaz obrađuje samo **Postavke**, prema korisničkom originalnom PNG-u i stvarnoj Android emulator snimci v1.12.64. Popravljeno je sljedeće: (1) naslov i koraljni zraci optički pomaknuti prema izvornoj referenci; (2) sedam kartica ima zasebno izmjerene y-pozicije 97, 161, 223, 286, 360, 433 i 498 na zaslonu 941×1672, s većim pauzama između skupina; (3) kartice su malo šire i imaju umjerenije sjene, samo na stranici Postavke; (4) ikone se crtaju kao zaobljeni, ujednačeni obojeni kvadrati sa suptilnim osvjetljenjem umjesto prenaglašenih pločica s tri obruba; (5) Vibracija ima stvarne valove, a lokalna kopija relevantniju ikonu oblaka; (6) svi prekidači imaju istu x poziciju, istu visinu i zadržavaju svoje stvarno funkcionalno stanje; (7) padajući izbor jezika ima usklađene rubove, natpis i chevron; (8) cijeli redak ima zasebnu punu dodirnu zonu; (9) donji Brojko logotip nema više natpis by Brendigo desno od logotipa, već verzija i by Brendigo stoje u dva centrirana retka ispod, kao na izvorniku; (10) na kompaktnoj referenci nema nepotrebnog skrolanja, a na nižem telefonu footer je i dalje dostupan pomicanjem. Verzija prikazana u footeru ostaje stvarna verzija aplikacije, a sinkronizacija i dalje iskreno otvara lokalnu kopiju umjesto lažnog mrežnog prekidača. Nijedna stvarna korisnička postavka niti spremljena partija ne mijenja se nadogradnjom.

**QA:** novi Android instrumentacijski test provjerava sedam redaka, njihove točne logičke koordinate, minimalne dodirne zone, izostanak preklapanja i skrolanje za 941×1672, 1080×2400 i 360×600, plus tamnu temu. Izvorni referentni PNG kao dizajnerska ilustracija i dalje sadrži 3D sjene koje standardni Canvas ne može dokazivo reproducirati potpuno identično.


### Korekcija pronađena na stvarnoj v1.12.65 snimci

Nakon provjere fizičkih piksela 941×1672 primijećen je **opacity regression**: sjena kartice postavljala je Paint alpha na 0x22, a slijedeći linearni gradijent nasljeđivao je prozirnost, čineći bijelu karticu bež, a koraljnu ikonu blijedoružičastom. Prije svakog zasebnog gradijenta u `settingsPanel` i `settingsIconTile` sada se eksplicitno vraća potpuno neprozirna bijela boja Paint objekta. Usporedni primjer: v1.12.65 x80,y270 imao je RGB(238,211,195) umjesto zasićenog koralja, a karta x400,y300 RGB(248,240,229) umjesto skoro bijele. Android instrumentacijski test sada provjerava i zasićenost koraljne ikonice i neprozirnu bijelu karticu. Ovo je temeljeno na stvarnom screenshotu, a ne na procjeni koda.


## QA snimke moraju prikazivati stvarnu GitHub Release verziju

Snimke nakon izdanja v1.12.66 bile su buildane u zasebnom GitHub Actions workflowu koji je upotrebljavao svoj `GITHUB_RUN_NUMBER` (npr. 57) dok je objavljeno izdanje imalo broj 66. To **ne dokazuje da preuzeti APK ima pogrešnu verziju**; release APK izgrađen je u release workflowu s vlastitim run brojem. Kako bi screenshot i izdanje bili usporedivi, `android-ui-qa.yml` sada se aktivira na dovršetak uspješnog workflowa Build and release Android, preuzima točan `head_sha` i nasljeđuje njegov `run_number` kao `BROJKO_BUILD_NUMBER`. Gradle daje tom eksplicitnom broju prednost nad internim brojem QA izvođenja. Android emulator nakon instalacije izričito provjerava `dumpsys versionName` i prekida test ako se ne podudara. Workflow_dispatch ostaje podržan za ručnu QA provjeru. Time su slijed build–release–QA i UI screenshoti determinističniji, a usporedbe verzija konačno ispravne.


## Ekran 2/10 — Teme i izgled — original 941×1672 (25.10.2026.)

Prema korisničkoj originalnoj ilustraciji i QA snimci v1.12.67: vraćena je kremasta zaobljena podloga naslova Teme i izgled; uklonjen nepotrebni zasebni gumb Natrag jer su dostatni donji tabovi i Android Back; samo odabrana tema sada ima izdignutu bijelu karticu i koraljni obod, a preostale imaju direktan pregled obojene 2×2 ploče; uvedene su manje izražene sjene i obrubi SAMO u miniprikazima ovog zaslona, bez zahvata u stvarni game board i njegovu mehaniku; nazivi Zaobljene, Ravne, Staklaste svi su u jednom retku bez prelamanja; kompaktni stilovi dobili su male radio markere ispod uzoraka; sekcijski naslovi Pregled/Stil pločica lijevo su poravnati, sa suptilnom okomitom razdjelnicom; donja navigacija na temama ima neutralni vektorski simbol krune i izdvojenu koraljnu paletu kao na originalu. Novi UI test provjerava sve 3 teme, 3 stila, 4 funkcionalna taba, odsutnost dupliciranog Back gumba, dodirne zone te novi screenshot na 941×1672 i visokom telefonu. Proizvodni ekran ne tvrdi da je 3D osvjetljenje pikselski identično statičnoj ilustraciji.


### Drugi pixel-pregled Teme i izgled — korekcija prema stvarnoj v1.12.68 snimci

Na emulator screenshotu v1.12.68 (941×1672) utvrđeno je da je logotip djelomično odrezan pri vrhu, pill naslov bio za oko 30 px previsoko, naslovne riječi i podnaslov pomaknuti prema gore u odnosu na original, prve tri ploče približno 17 px previsoko, mreža 4×4 imala je vidljivo presitne pločice po visini, a aktivni tab imao je skraćen naslov Teme. Korigirane su logotipske vrijednosti brand(52,42), naslovni pill 85–138, baseline 125, podnaslov 154, položaji ploča +7 logičkih jedinica, sekcijski naslovi u glavnom panelu, razdjelnica x=193, 4×4 mreža sa 36×36 skoro kvadratnim prikazima i y korakom 39.6, reducirana visina kartica stilova s radio indikatorima ispod i puni vizualni naziv aktivnog donjeg taba **Teme i izgled**. Dodan Android test za originalni 941×1672 viewport i dodirnu dohvatljivost sva tri stila. Ove izmjene ne mijenjaju algoritam igre, stilove na stvarnoj ploči ili sačuvane postavke.


### Treći vizualni prolaz — nedostajući opisi, separator i živopisna paleta

Emulator snimka v1.12.70 potvrdila je novi raspored i 35/35 testova, ali korisnički izvor pokazuje tri tekstualna opisa koji su prije nedostajali: **Kada spojiš brojeve.** pod Vibracija, **Zvučni efekti u igri.** pod Zvukovi i **Koliko su izražene animacije spajanja.** pod Jačina animacija. Vraćeni su u obje varijante layouta uz male neutralne horizontalne separatore, donju ikonu vibracije s valovima i tamnoplave zvučne simbole umjesto tirkiznih. Svaki prekidač ostaje stvaran i zadržava istu spremljenu vrijednost i nepreklapajuću dodirnu zonu. Živopisna preview tema ima violet-plavi gradijent iza četiri svoja broja kao u originalu umjesto plošne pastelne pozadine. Lijeva 4×4 preview pozadina je sužena na x=29..192 kako ne bi prekrivala razdjelnicu x=193; unutarnje kvadratne 4×4 pločice ostaju identične. Novi emulator test snima ta dva informativna retka i potvrđuje funkcionalne prekidače, sigurnu veličinu područja dodira te nulti nepotrebni scroll na 941×1672.


## Ekran 2 — Teme i izgled (izvorni PNG reference)

Usporedba je pokazala nedostajuće radio oznake ispod kartica **Zaobljene/Ravne/Staklaste** u standardnom layoutu; kompaktni ih je već imao. Dodana su tri jasno vidljiva kružića i bijela oznaka odabranog stila. Pritom je vodoravna razdjelna linija spuštena, a dodirni cilj svih stilova produžen do y=563, bez preklapanja s idućom funkcionalnom kontrolom Vibracija (počinje na y=564). Stil Staklaste sada prikazuje karakterističan bijeli dijagonalni odsjaj u oba layouta, pa se razlikuje od Zaobljenih na izvornom previewu. Uveden je Android screenshot i provjera odabranog radio kružića za sve tri vrijednosti stila na dva layouta, bez promjene stvarne igraće ploče, spremljene teme ili postavki.


### Ispravak preklapanja opisa i prekidača nakon QA v1.12.72

Stvarna nova snimka pokazala je da su opisi `Kada spojiš brojeve.` i `Zvučni efekti u igri.` ispisivani preko prekidača jer je renderer odobravao `leftFitted(..., maxWidth=123)` od x=245 sve do 368, iako je prekidač već počinjao na x=324. Oba standardna i kompaktna prikaza sada daju natpisima najveću širinu **86** logičkih jedinica, s preostalim razmakom od najmanje 6 jedinica do prekidača na **x=337..380**. Stvarno aktivno stanje, hrvatski tekst i puna dodirna zona ostali su nepromijenjeni. Ukupna širina panela ostaje do x=380; sada niti opis niti prekidač ne prelaze dopuštenu granicu. Vizualno verificirati na stvarnoj emulator snimci.


## Ekran 3 — Statistika: povijest partija prema izvornom PNG-u (2026-10-25)

Usporedba s izvornom 941×1672 referencom (`Slika ChatGPT ... 22_15_17-8(1).png`) i emulator v1.12.73 pokazala je da je povijest bila pet zasebnih pločica s velikim sjenama, dok original koristi **jedan veliki krem-bijeli panel, pet niskih svijetlih redaka, datum+vrijeme, obojenu vrijednost bodova, eventualnu krunu za stvarni rekord, obojeni bedž za najveću pločicu i tanki chevron**. Sada je struktura precrtana s x=16..384 roditeljskim panelom y=443..708, naslovom u x=30, redcima y=467+48i i punim dodirnim zonama y..y+48 (44-unit vizualni red + 4-unit međuprostor). Svaki završetak nove partije pohranjuje stvarno lokalno vrijeme HH:mm uz postojeći dan i rezultat; stari povijesni zapisi ostaju važeći i prikazuju samo datum, bez izmišljenog vremena. Bedževi broja koriste žutu, ljubičastu, plavu ili zelenu referentnu paletu ovisno o dosegnutoj pločici; kruna se prikazuje samo ako rezultat stvarno izjednačava ili prelazi najbolji spremljeni rezultat. Kôd ne mijenja postojeće rezultate, poredak, spremljene partije ni pravila obračuna. Novi Android regresijski test provjerava razliku rasterskih piksela između zapisa s vremenom i bez vremena te dostupnost dodirnih zona.


### Ispravljena semantika petog statističkog KPI-ja

Izvorna slika prikazuje **„Niz pobjeda”**, a implementacija je dosad na istome mjestu prikazivala `playDayStreak` (broj uzastopnih dana igranja), što je faktualno druga metrika. Sada se broj uzastopnih pobjeda računa iz postojećih spremljenih `history[].won` vrijednosti: svaka najnovija pobjeda povećava niz dok se ne naiđe na izgubljenu partiju. Nepostojeći legacy `won` flag tretira se kao nedokazana pobjeda (0), ne kao izmišljena pobjeda. Tjedne misije **Igraj 7 dana zaredom** i dalje koriste nepromijenjeni `playDayStreak`; podaci korisnika ostaju sačuvani. Maksimalno pouzdano prebrojavanje ograničeno je brojem spremljenih nedavnih rezultata. Dodan je regresijski test koji razlikuje niz od dviju pobjeda od kalendarskog niza od 12 dana.


### Dopunsko poliranje statističkih površina i vremenskih zapisa

Gornjih pet KPI kartica i roditelj grafikona koriste sada zasebni `statsSoftCard` sa suptilnom sjenom i pastelnim gradijentom, umjesto teškog 6-unit reljefnog ruba rezerviranog za igraće pločice. Barovi po izvornom uzorku izmjenjuju koraljnu i tirkiznu boju, pri čemu je rekordni maksimum koraljan. Redak povijesti u klasičnom/visokom prikazu i zaslon detalja sada pokazuju vrijeme samo ako ono stvarno postoji; naslijeđeni zapisi ostaju bez sata. Novo arhivirani zapisi koriste mjesec na hrvatskom (`d. MMM yyyy.`) i HH:mm umjesto 25.10.2026. Brojači, datum dosadašnjih zapisa i njihovi rezultati ostaju nepromijenjeni. Android instrumentacijski test potvrđuje da točno poznati datum i vrijeme daju `25. lis 2026. · 18:43`, dok stariji redak bez vremena ostaje neizmijenjen.


## Ekran 4/10 — Misije i postignuća: zasebna vizualna revizija

Uspoređeni su konkretan korisnički izvor (izdvojeni dio `07-Misije.jpg`) i stvarni `reference-941x1672-missions.png` iz uspješnog v1.12.77 Android emulatora. Identificirani su i ispravljeni: (1) centralno umjesto lijevog poravnanja dugih naslova i opisa svih šest kartica, (2) prejaka 6-unit dubina i višestruke sjene koje su se preklapale između sekcija, (3) nedostajuće dvoredno objašnjenje dnevnog zadatka s pločicom 512, (4) pogrešna nagrada za Majstor spajanja koja je prikazivala sirovi maksimum pločice kao npr. 4/1024, umjesto 0/1 ili 1/1, (5) indikator Postignuća prikazivao je spremljenu star valutu, ali nije govorio koliko su postignuća završena. Sada pokazuje **stvarni broj riješenih zadataka od dva trenutno implementirana (0/2..2/2)**; referentni „4/20” ne može se vjerodostojno reproducirati bez izrade preostalih 18 postignuća. (6) na 941×1672 maknuto je nepotrebno skrolanje s ekrana misija jer se kompletan sadržaj završava na y≈709, a zasloni kraći od toga i dalje se pomiču. Koristi se lokalni `missionSurface` s plitkom sjenom i kontroliranim neprozirnim gradijentom, bez globalnih promjena igre ili drugih zaslona. Sve šest nagrada, njihove bit zastavice i stvarni napredak ostaju nepromijenjeni.


## Ekran 5 — Dnevni izazov: prvi vizualni prolaz prema referentnom PNG-u

Usporedio sam korisnički izvorni Dnevni izazov `Slika ChatGPT 24. lis 2026. 22_15_15-6(1).png` (941×1672) i screenshot aplikacije v1.12.80 `reference-941x1672-daily.png`. Pronađene razlike: nedostajala je zajednička blijedobež podloga iza tri bijela cilja; ljubičasta reward kartica nije prikazivala opis `Dovrši sva tri cilja i osvoji posebnu nagradu!`; natpis i +250 su bili skupljeni u sredinu, i prikaz dopušta pomicanje kroz dodatnih 170 logičkih jedinica praznog prostora zbog generičkog sadržaja visine 880 umjesto stvarne portretne visine 710. Ugrađena je grupna panel podloga bez promjene dodirnih zona, detaljnija tipografija nagrade (uz stvarnih +250 zvjezdica), nešto veći poklon, te `contentHeight=710` samo u zbijenom Daily layoutu. Kratki 360×600 telefon i dalje može pomicati sadržaj do CTA; referenca 941×1672 više se ne može pomaknuti u prazno. Novi emulator test provjerava nulti ghost-scroll na referenci, pozitivan scroll na malom telefonu te tri cilja s pomoćnim gumbima i glavni start/resume gumb na tri viewporta.


### Druga usporedba izvornog dnevnog naslova i ploče

Korisnički originalni Dnevni izazov koristi dva retka uvodnog teksta, dok je ranija implementacija na 941×1672 prikazivala jednu jedinu skraćenu rečenicu. U portretnom rasporedu naslov `izazov` podignut je za 6 logičkih jedinica, datumska kapsula također za 6, a objašnjenje je podijeljeno u dvije logičke linije s baselineom 153 i 167 (`Riješi današnji izazov i osvoji posebnu nagradu.` / `Pokaži koliko daleko možeš stići!`). Dodane su odvojene koraljno-zlatne dekoracije lijevo i desno od ploče na originalnim mjestima bez prelaženja preko polja. Standardni visoki layout ne mijenja tekst ni dimenzije ploče. Zadržan je nulti nepotrebni scroll na referenci, kao i funkcionalna provjera sva tri cilja.


## Dnevni izazov — novi prolaz usporedbe s izvornim 941×1672 PNG-om

Nastavak poslije v1.12.82: usporedba je pokazala da je 4×4 ogledna ploča na Androidu preuska i preniska u odnosu na izvornu ilustraciju. Portretni panel sada se prikazuje na x=85,5, y=165, dimenzija 229×229 (ranije x=89, y=171, veličina 221×221); sve 16 pločica povećane su s 46×46 na 50×50 dizajnerskih jedinica s razmakom 4,5, uz sigurne rubove ploče i bez prekrivanja teksta iznad ili ciljeva ispod. Tri cilja imaju ujednačene 49-unit redke i tekst prema originalu: Dosegni 256, Ostvari 3000 bodova i Završi u 80 poteza; njihovi opisi objašnjavaju pravilo umjesto da prikazuju netočne uvjete. Zajednička kremasta podloga ostaje sačuvana. Kartica posebne nagrade dobila je programatski nacrtanu ljubičasto-zlatnu ilustraciju poklona sa zlatnim mašnama (bez generiranja slika u razgovoru), a +250 i stvarno odobravanje nagrade ostali su nepromijenjeni. Svaka pomoćna ikona ima zasebnu dodirnu zonu od najmanje 48 jedinica. Dodan je instrumentacijski test sa snimkom stvarnog zaslona i provjerama 256/3000/80 te odbijanja nakon 81 poteza. Ovo je nastavak vizualne revizije, ne tvrdnja o apsolutnoj jednakosti izvornog slojevitog 3D rasterskog dizajna.


### Naknadna korekcija po v1.12.83 stvarnoj snimci: uvodni tekst

Na screenshotu v1.12.83 druga rečenica uvoda završavala je iza ploče i bila djelomično odsječena — automatski emulator testovi bili su zeleni jer nisu provjeravali vizualno preklapanje teksta. Portretni datum pomaknut je s y=108 na y=99, prvi red opisa na baseline y=145 i drugi red na y=159, a okvir ploče počinje na y=167. Time između drugog retka i ploče ostaje najmanje 8 dizajnerskih jedinica, a datum se više ne sudara s prvim retkom. Dodan je zaseban screenshot-regresijski test za referentni zaslon, uz prethodne funkcionalne provjere. Ovo je popravak potvrđen iz pravog Android rendera, ne samo statičkom analizom.


## Ekran Dnevni izazov — poliranje v1.12.84 → novi release (2026-10-26)

Stvarna usporedba 941×1672 iz `brojko-v84-usporedba` pokazala je razliku u dva statična elementa: (1) datumski pill bio je visok 32 logičke jedinice, gotovo bez razmaka do prve linije opisa; nova visina je 25 s centriranom ikoncom i smanjenom tipografijom, čime ostaje vidljiva pauza prije opisa. (2) veliki koraljni CTA imao je globalni stil reljefa (pomak 5), zbog čega je donja sjena bila mnogo jača nego na izvornom PNG-u; za **samo ekran `daily`** koristi se 2.5-unit lip s tanjim bijelim obrubom, a ostale istaknute tipke unutar igre ostaju netaknute. (3) tri kratke točkice iznad riječi Dnevni zamijenjene su trima pravilno rotiranim koraljnim potezima, prema referentnom dizajnu. Dodan je Android test za visinu i područje datuma te puna funkcionalna dodirna zona CTA na 941×1672, 400×880 i 360×600. Izazov je i dalje dinamičan (današnji datum, pravi streak, jednokratna nagrada), pa te informacije ne smiju biti zaleđene u ilustraciju.
