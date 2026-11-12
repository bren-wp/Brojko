# BROJKO — pristupačnost i responsive QA

## Implementirano
`BoardAccessibilityDelegate` stvara zasebne virtualne TalkBack čvorove
za stvarne aktivne Canvas gumbe. Čvorovi koriste postojeće semantičke
oznake i transformirane koordinate; vizualni layout i igre nisu mijenjani.
Pozivi ostaju povezani s izvornim `Tap.action` callbackovima.
Postojeće akcije za pomicanje pločica u četiri smjera ostaju na
glavnom Canvas elementu; tipkovnički D-pad i dalje radi.

## Obvezne ručne i automatizirane provjere
- Na 941×1672 izvornom viewportu usporediti bitmap prije i poslije:
  delegate ne smije promijeniti nijedan RGB piksel.
- TalkBack: proći gumbe u razumljivom redoslijedu, aktivirati Undo,
  teme, postavke, sigurnosnu kopiju i navigaciju te pogledati fokusne
  granice na pravoj veličini prozora.
- Switch Access i tipkovnica: potvrditi sve akcije bez preciznog dodira.
- 320×480, 360×600, 941×1672, 1080×2400, landscape,
  font scaling 200–400%, foldable i 3-button/gesture navigation.
- Android 7–16 prema podržanim API razinama.
- Na fizičkim uređajima utvrditi dinamiku poziva, scroll/focus,
  battery i sustavne insets; emulatorski test ne zamjenjuje ovo.

## Ograničenja
Igra je Canvas sučelje: virtualna djeca predstavljaju samo postojeće
gumbe, ne svaku pojedinu pločicu. Ploča za igranje ostaje dostupna preko
četiri postojeće usmjerene akcije. Fizički TalkBack, Switch Access i
realni foldable uređaji još nisu potvrđeni; ne navoditi 100% accessibility.
