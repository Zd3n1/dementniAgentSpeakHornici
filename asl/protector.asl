// protector agent
// - jeho cílem je dojít na depot a blokovat přístup nepřátel
// - jakmile je ve stavu, že chrání depot, může přebírat zlato od horníků, kteří ho donesou k němu
// - přinesené zlato vkládá do depotu, dočasně tedy opustí ochranné pozice, uklidí zlato a pak chrání dál

{ include("moving.asl") }

/* beliefs */
free.
my_capacity(1). // kapacita 1, protože maximálně sebere zlato, které je přímo u něj

// hned na začátku zjistí souřadnice depotu a zahají cíl k němu dojít
+pos(_,_,0) <-
    ?gsize(S,_,_);
    .print("Protector starting simulation ", S);
    !start_run_to_depot(S).

// dojít na depot a zkusit to znovu, dokud se to nepovede
// pokud mám přiřazené místo (protect_target), jdu na něj, jinak přímo na depot
+!start_run_to_depot(S) : protect_target(TX,TY) <-
    .print("Protector running to assigned target (",TX,",",TY,")!");
    !!run_to_depot(TX, TY).

+!start_run_to_depot(S) : not protect_target(_, _) <-
    ?depot(S,DX,DY);
    .print("Protector running to depot at (", DX, ",", DY, ")!");
    !!run_to_depot(DX, DY).

// agresivní rekurzivní plán: bude se neustále snažit být na souřadnicích depotu (DX,DY).
// navigace k targetu (ktere muze byt i soused depotu)
+!run_to_depot(DX, DY) :  not pos(DX, DY, _) & not protecting <-
    .print("Protector navigating to ",DX,",",DY,"...");
     !pos(DX, DY);
     !!run_to_depot(DX, DY).

// vstup do ochranného stavu, po úspěšné navigaci na cílovou pozici
+!run_to_depot(DX, DY): pos(DX, DY, _) & not protecting <-
    .print("Protector arrived at target, starting protection at (",DX,",",DY,")");
    +protecting;             // zapamatujeme si, že chráníme
    +protect_pos(DX, DY);    // zapamatujeme si pozici, kterou chráníme
    !!protect_and_perceive.  // spustíme hlavní plán pro chránění a sběr zlata

// pokud jsme v ochranné pozici, prostě cyklíme tento program do nekonečna
+!protect_and_perceive(DX, DY): protecting <-
    .print("Protector in position - staying put (protecting)");
    !!protect_and_perceive.

+gold_reported(GX, GY) <-
    .drop_all_desires;
    .print("GOLD REPORTED").

// konec simulace
+end_of_simulation(S,R) <-
    .drop_all_desires;
    -+free;
    .print("-- PROTECTOR END ",S,": ",R).

@rl[atomic]
+restart <-
    .print("*** Protector Start it all again!");
    .drop_all_desires;
    !start_run_to_depot(S).
