# Faza 12 — Potvrda nove igre i završetka partije

Datum: 30. listopada 2026. · PR [#28](https://github.com/bren-wp/Brojko/pull/28) · grana `feat/pixel-victory-modal-phase4`.

## Reference, granice i cilj

Vlasnik je dostavio dizajnerske reference za deset glavnih Brojko prikaza, ali **nije dostavio zaseban PNG** za `confirm` ili `confirmEnd`. Izgledi su stoga izrađeni u postojećem sustavu: topla krem kartica, koraljna primarna akcija, tirkizni naglasak za završetak, reljefni vektorski simboli, Nunito, zaobljene površine i blage sjene. Ne treba izmišljati 1:1 screenshot rezultat niti RGB MAE za ova dva modala.

## Promjene

- `confirmOverlay()`: jedinstvena topla kartica s koraljnom ikonom nove igre, naslov **Nova partija?**, informacijom o spremanju u povijest, prikazom **stvarnog rezultata** i nazivom sljedećeg načina rada (Klasično 4×4 / Opušteno 5×5 / Dnevni izazov). Akcije: `Započni novo` i `Odustani`.
- `confirmEndOverlay()`: zasebna tirkizna ikona kvačice, naslov **Završiti partiju?**, obavijest o lokalnom arhiviranju, stvarni rezultat; akcije `Završi i spremi` i `Nastavi igrati`.
- Dijeljene **isključivo modalne** metode `confirmModalBadge` i `confirmSessionSummary`; globalni gameplay HUD, pravila, pločice i spremanje nisu mijenjani.
- `contentHeight` za `confirm` i `confirmEnd` na portretnom zaslonu sada je 654 logičke jedinice, umjesto nepotrebnih 880. Oba modala i sve dodirne zone stanu u viewport **941×1672** i **360×600** bez fantomskog scrollanja; visoke ekrane 1080×2400 obrađuje postojeće skaliranje.
- Renderiranje iza modala zadržava zamrznutu stvarnu ploču. `taps.clear()` prije modala sprječava slučajno aktiviranje gumba igre.

## Testovi i očekivana semantika

- `confirmationModalsStayOnScreenAndShowLiveSessionSummary`: obvezne tri rezolucije 941×1672, 360×600, 1080×2400, dva odvojena dodirna područja >=48, vidljiva unutar ekrana, bez dodatnog scrolla. Stvarne 941×1672 emulator snimke: `confirm-new-session-941x1672.png` i `confirm-end-session-941x1672.png`.
- `confirmationCancelIsNonDestructiveAndConfirmedEndArchives`: `Odustani` i `Nastavi igrati` vraćaju izvornu partiju bez promjene rezultata, poteza ili povijesti. `Završi i spremi` mora prije uklanjanja aktivne partije zapisati stvarni rezultat, poteze i mode u lokalnu povijest.
- `confirmNewGameArchivesBeforeSwitchingToRequestedMode`: `requestNew("relaxed")` otvara potvrdu, ne zapisuje povijest prije pristanka. Potvrđivanje sprema staru 4×4 igru te započinje novu 5×5 partiju. Stari korisnički/testni podaci i prefovi vraćaju se nakon testa.
- `scripts/verify-emulator-screenshots.py` zahtijeva obje nove snimke stvarnog Android prikaza. Nema generiranja slika u chatu.

## Rizici / provjere prije prihvata

1. Izvršiti Android PR validation (Gradle build/lint/unit), Android 16 insets i emulator UI screenshot QA na **posljednjem HEAD-u**. Raniji zeleni CI za prethodne commitove nije dokaz da je najnoviji prošao.
2. Preuzeti oba stvarna 941×1672 PNG-a iz posljednjeg `Brojko-real-device-UI` artefakta i provjeriti tekst, poravnanja, kontrast, dodirne zone, tamnu temu i načine rada bez lažiranja reference.
3. Potvrditi da arhiviranje na neuspjeh ne briše aktivnu partiju i da dugotrajna pohrana ostaje lokalna.
4. Ne spajati Draft PR i ne objavljivati release dok potrebne provjere ne prođu.

**Status:** implementacija i testovi committani; pixel-perfect 1:1 nije dokazan jer zasebni referentni PNG nije dostavljen. Sljedeća stanja za audit: `over`, `dailyWin`, `dailyFail` i finalna regresija svih glavnih zaslona.
