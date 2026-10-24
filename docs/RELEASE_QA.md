# Brojko — Release quality checklist

## Što je provjereno automatizacijom
- Determinističke 2048 operacije u JVM testovima, uključujući slučaj ponovnog spajanja, RNG i undo.
- Android lint, API 36 Gradle build, APK/AAB izrada.
- API 35 emulator: otvaranje svih 16 primarnih stanja, početak igre, spremanje stvarnih Android snimki.
- Kompaktni viewport: interaktivno pomicanje dugih ekrana; gumbi ostaju dostupni.

## Potrebne ručne provjere prije Google Playa
- Usporediti referentni grafički identitet i ekran po ekran, osobito font (referenca koristi Nunito), stil sjena, ilustracije i ikonu, sve u stvarnom prikazu.
- Provjeriti 360×640, 360×780, 393×873, 412×915, 1080×2400 uz sistemske trake i izreze.
- Testirati kratke/duge swipe pokrete na 4×4 i 5×5, promjene tema, zaslone pobjede, poraza i bez interneta.
- Provjeriti TalkBack elemente i touch target veličine, osobito na malim ekranima.
- Testirati oporavak nakon prisilnog zatvaranja, nadogradnje, invalidne sigurnosne kopije i 24h rollovera.
- Postaviti produkcijski upload keystore u GitHub Actions secrets, potvrditi potpis release AAB-a, testirati kroz Play internu testnu stazu.

## Ograničenja
Programatski Canvas zasloni ne predstavljaju još pikselno identičnu reprodukciju korisničkih dizajnerskih referenci. GitHub Actions build i smoke test ne zamjenjuju ručni vizualni i funkcionalni QA na stvarnim uređajima. Nepotpisani release AAB nije spreman za Google Play produkciju.
