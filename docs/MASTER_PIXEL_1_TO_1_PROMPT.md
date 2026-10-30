# BROJKO — MASTER PROMPT ZA PIXEL-PERFECT ANDROID RAZVOJ

## Kontekst i izvor istine
Nastavi postojeću Android igru **Brojko**, repo `bren-wp/Brojko`, paket
`com.brendigo.brojko`. Koristi **postojeći otvoreni PR #28**
(`feat/pixel-victory-modal-phase4`), bez novog repozitorija, novog PR-a,
arhitekturnog resetiranja, promjene identiteta aplikacije ili lažnih podataka.

Referenca je deset korisnikovih izvornih PNG-ova dostavljenih 30.10.2026.,
uključujući brending, Splash, Home, Klasično 4×4, pobjedu „Bravo!”,
Dnevni izazov, Misije i postignuća, Statistiku, Teme i izgled te Postavke.
Referentne snimke portretnih zaslona imaju **941 × 1672** piksela.
Nemoj generirati nove slike u chatu ni zamjenjivati korisnikove reference
generičkim stock ilustracijama.

## Strogi proces — jedan zaslon po iteraciji
1. Svaki put pročitaj najnoviji PR HEAD, nove review komentare, sva tri
   obvezna Actions workflowa i postojeći `docs/PHASE_XX_..._AUDIT.md`.
2. Nastavi sljedeći nedovršeni zaslon istog PR-a; ne mijenjaj nepotrebno
   ostale zaslone. Redoslijed: 1 Home, 2 Klasično 4×4, 3 Pobjeda,
   4 Dnevni izazov, 5 Misije, 6 Statistika, **7 Teme i izgled**,
   8 Postavke, 9 Splash i 10 brending/ikone i preostali prikazi.
3. Prikupi originalni PNG i **stvarnu** snimku emulatora istog zaslona
   na 941×1672. Bez rastezanja, pomicanja, resamplinga ili kozmetičkog
   poravnanja piksela. Ako se reference ne mogu programatski usporediti,
   to izričito navedi kao otvorenu provjeru.
4. Mjeri RGB MAE cijelog zaslona te regije: logotip/naslov, glavne kartice,
   podloge, ikone, navigacija, tekst, gumbi i stanja. Dokumentiraj
   baseline, promjene i regresije sa stvarnim artefaktima, ne procjenama.
5. Uredi XML/Canvas/Java resurse postojeće aplikacije, uključujući boje,
   gradijente, sjene, radijuse, tipografiju Nunito, razmake, pozicije,
   klikabilne zone, boje pločica, prilagodljive elemente i animacije.
   Nemoj tvrdnju „1:1” iznositi dok stvarna slika to ne potvrdi.
6. Provjeri zaslon u svim relevantnim stanjima, svijetloj/tamnoj/živopisnoj
   temi, različitim stilovima pločica, Android 15/16 edge-to-edge,
   na 941×1672, 360×600 i 1080×2400. UI mora ostati čitljiv i
   upravljiv uz sustavno skaliranje fonta i TalkBack.
7. Sačuvaj postojeće ponašanje igre 2048, 4×4 i 5×5, pobjedu, undo,
   statistikе, izazove, misije, postignuća, postavke, zaštitu privatnosti
   i trajno spremanje. Demo rezultati smiju postojati samo u testovima.
8. Ne koristi lažnu cloud sinkronizaciju, lažne rekorde ili nagrade,
   uklanjanje korisničkih podataka, vanjske resurse ni privatne ključeve
   u repozitoriju. Prije refaktora napravi ciljanu provjeru sigurnosti,
   mrtvog koda, performansi i pristupačnosti.
9. Dodaj instrumentacijski test interakcija i screenshot test zaslona.
   Obavezni CI: build + lint + unit, Android 16 insets/navigation,
   Android UI emulator/screenshot. Ako padne bilo koji korak,
   pročitaj konkretne logove, ispravi razlog pada i ponovi provjeru.
10. Ažuriraj fazni audit i PR opis s točnim commit SHA, testovima,
    pixel usporedbama, otvorenim razlikama i sljedećim zaslonom.
    Zadrži PR kao Draft sve dok QA i usporedba nisu zadovoljavajući.
    Bez mergea/releasea prije uspješnog završnog QA.

## Vizualni sustav
- Osnovne boje: koraljna **#FF7A5B**, tirkizna **#2EC4B6**,
  tamnoplava **#1E2A44**, krem **#FFF8EE**, bež **#EADFCF**.
- Pločice: 2 koraljna, 4 tirkizna, 8 plava, 16 žuta,
  više vrijednosti u ružičastoj/ljubičastoj/plavoj/zelenoj paleti
  prema originalnim PNG-ovima.
- Font: Nunito, težine prema referenci; točni brojevi i hrvatski
  dijakritički znakovi; bez improviziranih zamjenskih glifova.
- Reljefne kartice s mekanim 3D sjenama, toplim bijelim površinama,
  konzistentnim zaobljenjima i suptilnim refleksijama.
- Sve boje/pozicije iz PNG-a provjeriti mjerenjem; navedeni heks
  kodovi su početni brend tokeni, ne zamjena za vizualni pixel audit.

## Trenutačna faza — 7/10: Teme i izgled
Ciljano ponovi sljedeće iz vlasnikove reference:
- gornji Brojko logotip, narančasti/koraljni naslovni naglasak,
  naslov „Teme i izgled” i podnaslov;
- tri interaktivne kartice **Svijetla, Tamna, Živopisna**;
- stvarni 2×2 preview i točne oznake 2, 4, 8, 16 za svaku temu;
- točan prikaz odabrane teme (koraljni okvir, kvačica i radio oznaka);
- lijevi „Pregled” 4×4 sa svih 16 vrijednosti i stvarnim stilom;
- tri stilа **Zaobljene, Ravne, Staklaste** s različitim materijalima;
- neovisne sklopke **Vibracija** i **Zvukovi**, stvarno spremanje;
- funkcionalan odabir **Niska, Srednja, Visoka** za animacije;
- četiri donje kartice **Novo, Rekordi, Teme i izgled, Postavke**,
  točne ikone, aktivno stanje i dodirne zone.
Ne mijenjaj globalnu fiziku pločica, rezultate ni ostale zaslone radi
kozmetičke korekcije ove faze.

## Kriterij izvještaja
Na kraju svake iteracije vrati samo provjerljive stavke:
- aktualni PR i commit,
- točno promijenjene datoteke,
- što stvarno radi,
- vrijednosti pixel-diffa prije/poslije (ako su izmjerene),
- CI status zadnjeg HEAD-a,
- otvorene nedostatke,
- **sljedeći zaslon**.
Bez neprovjerenih tvrdnji „sve savršeno”, „100%” ili „1:1”.
