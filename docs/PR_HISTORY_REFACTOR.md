# BROJKO — faza nastavka kroz PR

Grana: `feature/brojko-history-refactor-security-qa`, polazni commit `62ea18a9e2734d31ca010cfa60c15466c78831f2`. **Nema izravnog pisanja na main.**

## Ovaj zahvat

- Odvojena funkcija arhiviranja stvarnih partija iz `MainActivity` u
  `LocalHistoryRepository` bez promjene formata `history` JSON-a.
- Očuvani su redoslijed (najnovija prva), limit od 20 redaka,
  prihvatljivo postupanje s oštećenom poviješću i sinkroni `commit()`.
- `game.archived` se postavlja tek nakon uspješnog upisa; `lastScore`
  ostaje u `MainActivity` kao dio aplikacijskog statea.
- Četiri instrumentacijska testa koriste zaseban privatni preferences
  prostor, bez diranja stvarnog korisničkog napretka.
- Statički security/dead-code inventory ažuriran je za novu klasu.

## Nastavak nakon zelenog CI-a

Sljedeća granica za refactoring jest premještanje navigacijskog stanja,
a potom postepeno izdvajanje native Canvas renderera uz izravnu
941×1672 usporedbu s izvornim referencama. TalkBack na fizičkim
uređajima, svi zasloni 1:1, SBOM nalaze build alata i konačni
security/dead-code audit ne označavamo završenima bez dokaza.
