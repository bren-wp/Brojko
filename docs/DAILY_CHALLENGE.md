# Dnevni izazov — specifikacija Brojko

- Svaki lokalni datum generira isti početni RNG raspored iz stabilnog datuma (offline).
- Ciljevi moraju biti istodobno ispunjeni u trenutku najkasnije 80. valjanog poteza: najveća pločica >=256 i score >=3.000.
- Nevaljan swipe ne broji se kao potez.
- Na dovršetak se prikazuje zaseban modalni ekran, čuva niz uspješnih dana i dodjeljuje se 250 zvjezdica **jednom po lokalnom danu**.
- Ako nakon 80. valjanog poteza oba cilja nisu zadovoljena, ekran neuspjeha završava izazov. Novi pokušaj kreće istim datumom i početnim RNG rasporedom.
- Dostizanje pločice 2048 ili blokada ploče i dalje imaju svoja standardna stanja kada izazov prije toga nije uspješno dovršen.
- Posebna nagrada, datum dobivanja i niz uspješnih dana ostaju u lokalnoj pohrani i JSON sigurnosnoj kopiji.

## Testovi
- JVM unit test: inkluzivna granica 80 poteza, nedovršeno stanje na 79 i 80, klasični način ne podliježe ograničenju.
- Android instrumentacijski test: uspješan 80. potez, nagrada od 250 jednom dnevno, neuspjeh na 80. potezu i snimke obaju rezultata.
- Sigurnost: pri učitavanju spremljenih partija provjeravaju se mode/veličina, rasponi, spremljeni undo i dopuštene vrijednosti pločica.
