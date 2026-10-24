# Brojko — provjera za Google Play

Stanje: izvorni kod i CI pripravljeni za testiranje; **nije potvrđena 1:1 vizualna podudarnost** sa svim originalnim referencama i **release AAB nije produkcijski potpisan bez GitHub secrets**.

1. Potvrditi primarni jezik hrvatski i tekstove iz `fastlane/metadata/android/hr-HR`.
2. Pregledati zaslone u više rezolucija; u listing postavljati **samo stvarne snimke aplikacije**, ne generirane makete.
3. Usporediti sve elemente s grafičkim referencama, osobito pozadinu, font, kartice, pločice i navigaciju.
4. Testirati swipe, igranje 4×4/5×5, dnevno ograničenje 80 poteza, jednokratnu nagradu, undo, backup/restore.
5. Testirati Google Play preporuke za targetSdk i privolu/pravila na dan predaje.
6. Provjeriti Google Play Console Data safety: Brojko trenutno nema mrežne SDK-ove, oglase ni analitiku; lokalni podaci ostaju na uređaju.
7. Konfigurirati potpisivanje release AAB-a trajnim privatnim upload ključem (4 GitHub secrets). Ključ ne smije biti u repozitoriju.
8. Izraditi produkcijski potpisani AAB, provjeriti integritet paketa i Google Play upload. Poslije toga provesti odgovarajuće testiranje i prijaviti za produkciju.

Izdanje na GitHubu nije isto što i odobrenje Google Playa.
