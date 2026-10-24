# Brojko — vizualna i funkcionalna QA matrica

## Izvor istine
Korisnički dostavljenih **10 referentnih slika** definiraju ciljni identitet. Dizajn prikazuje: brending i ikonu, splash, glavni izbornik, ploču 4×4, pobjedu, dnevni izazov, misije, statistiku, teme, postavke.

**Zasad nije potvrđena pikselna identičnost.** Programatski UI oponaša raspored i paletu boja, ali nijanse, svjetla, reljef tipografije, dekoracija i raspored svih elemenata zahtijevaju vizualni QA na uređajima.

## Reprodukcija vizualnog QA
- Otvoriti aplikaciju bez interneta, nakon brisanja podataka.
- Provjeriti portretne rezolucije 360×640, 360×800, 393×873, 412×915, 480×960 i zaslon s izrezom.
- Usporediti margine, veličine fontova, boje (#FF7054 / #27C7BB / #192B4D / #FFF8EE), sjene, polumjere i raspored elemenata po screenshotu.
- Reproducirati stanje ploče 4×4 te 5×5 (velike pločice s 4-5 znamenki, bez preklapanja).
- Provjeriti prikaz pobjede, pauze i kraja igre s aktivnim overlayom (bez aktiviranja pozadinskih tipki).
- Potvrditi da su prikazi responzivni: bez preklapanja statusne trake, navigacijskog područja i zaslona s izrezom.
- Uključiti temni način, živopisnu temu, vibraciju/isključivanje i animacije.

## Gameplay QA
- [ ] 2+2+2+2 spaja se u 4+4, ne u 8 u istom potezu.
- [ ] 4+4+8+8 daje 8+16 i 24 boda.
- [ ] Potez bez pomicanja ne ubacuje novu pločicu.
- [ ] Poništavanje vraća ploču, bodove i RNG te ostaje ispravno nakon ponovnog pokretanja.
- [ ] 2048 ne dodjeljuje više pobjeda za jedno postignuće.
- [ ] Puna ploča bez spajanja završava partiju.
- [ ] Ponovno pokretanje procesa čuva zadnju partiju.
- [ ] Dnevni izazov reproducibilan je za isti datum, ne dijeli napredak između različitih datuma.
- [ ] Izvoz/uvoz JSON kopije vraća napredak, postavke i povijest.
- [ ] Android API 23, 28, 33, 35 i 36 pregled i interakcije.
- [ ] Test sa zvukom isključenim, vibracijom i povratnim događajima u pozadini.
- [ ] Provjera TalkBacka i dostupnosti svih interaktivnih elemenata (trenutni Canvas zahtijeva dodatnu obradu).

## Za Google Play
GitHub Actions izgradnja i emulator nisu ekvivalent produkcijskoj provjeri. Potrebni su potpisani AAB, uređajno testiranje, provjera pristupačnosti, izjave o podacima, provjera store grafike i dopušteni tester/production access koraci prema računu.
