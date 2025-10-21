// protector agent
// - jeho cílem je dojit na depot a blokovat přístup nepřátel
// - jakmile je ve stavu, že chrání depot, může přebírat zlato od horníků
// - a do depotu jej umístit, dočasně tedy opustí ochranné pozice, uloží zlato a hned se tam vrátí

{ include("moving.asl") } //

/* beliefs */

free.
my_capacity(1). // kapacita 1, protože maximálně sebere zlato, které je přímo u něj

/* Initial goal */

// hned na začátku zjistí souřadnice depotu a zahají cíl k němu dojít
+pos(_,_,0)
  <- ?gsize(S,_,_);
     .print("Protector starting simulation ", S);
     !start_run_to_depot(S).

// dojit na depot a zkusit to znovu, dokud se to nepovede.
// pokud mam prirazene misto (protect_target), jdu na nej, jinak na depot
+!start_run_to_depot(S) : protect_target(TX,TY)
  <- .print("Protector running to assigned target (",TX,",",TY,")!");
     !!run_to_depot(TX, TY).

+!start_run_to_depot(S) : not protect_target(_, _)
  <- ?depot(S,DX,DY);
     .print("Protector running to depot at (", DX, ",", DY, ")!");
     !!run_to_depot(DX, DY).


// agresivní rekurzivní plán: bude se neustále snažit být na souřadnicích depotu (DX,DY).
// navigace k targetu (ktere muze byt i soused depotu)
+!run_to_depot(DX, DY)
  :  not pos(DX, DY, _) & not protecting
  <- .print("Protector navigating to ",DX,",",DY,"...");
     !pos(DX, DY);
     !!run_to_depot(DX, DY).

// pokud jsme uz v ochranne pozici, nestale se pokousime o dalsi pohyb
+!run_to_depot(DX, DY) : protecting
 <- .print("Protector in position - staying put (protecting)").

// pokud mame zlato, opustime pozici a jdeme k depu
+carrying_gold(N) : N > 0 & protecting
 <- .print("Protector got gold, leaving protect pos to deposit");
   ?depot(S,DX,DY);
   !!run_to_depot(DX, DY).

// konec simulace
+end_of_simulation(S,R)
  <- .drop_all_desires;
     -+free;
     .print("-- PROTECTOR END ",S,": ",R).

@rl[atomic]
+restart
  <- .print("*** Protector Start it all again!");
     .drop_all_desires;
     !start_run_to_depot(S).
