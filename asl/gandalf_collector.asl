// collector behaviour for protectors that are not assigned to protect slot
{ include("fetch_gold.asl") }
{ include("goto_depot.asl") }
{ include("search_unvisited.asl") }
{ include("search_quadrant.asl") }
{ include("allocation_protocol.asl") }

{ register_function("carrying.gold",0,"carrying_gold") }
{ register_function("jia.path_length",4,"jia.path_length") }

free.
my_capacity(1).

/* choose goal logic (simplified copy from miner.asl) */
@cgod2[atomic]
+!choose_goal
 :  container_has_space &
    .findall(gold(X,Y),gold(X,Y),LG) &
    evaluate_golds(LG,LD) &
    .length(LD) > 0 &
    .min(LD,d(D,NewG,_)) &
    worthwhile(NewG)
 <- !change_to_fetch(NewG).

+!choose_goal : carrying_gold(NG) & NG > 0
 <- !change_to_goto_depot.

+!choose_goal <- !change_to_search.

evaluate_golds([],[]) :- true.
evaluate_golds([gold(GX, GY)|R],[d(U,gold(GX,GY),Annot)|RD])
  :- evaluate_gold(gold(GX,GY),U,Annot) &
     evaluate_golds(R,RD).
evaluate_golds([_|R],RD)
  :- evaluate_golds(R,RD).

evaluate_gold(gold(X,Y),Utility,Annot)
  :- pos(AgX,AgY,_) & jia.path_length(AgX,AgY,X,Y,D) &
     jia.add_fatigue(D,Utility) &
     check_commit(gold(X,Y),Utility,Annot).

check_commit(_,0,in_my_place)   :- true.
check_commit(G,_,not_committed) :- not committed_to(G,_,_).
check_commit(gold(X,Y),MyD,committed_by(Ag,at(OtX,OtY),far(OtD)))
  :- committed_to(gold(X,Y),_,Ag) &
     jia.ag_pos(Ag,OtX,OtY) &
     jia.path_length(OtX,OtY,X,Y,OtD) &
     MyD < OtD.

worthwhile(gold(_,_)) :- carrying_gold(0).
worthwhile(gold(GX,GY)) :-
     carrying_gold(NG) & NG > 0 &
     pos(AgX,AgY,Step) &
     depot(_,DX,DY) &
     steps(_,TotalSteps) &
     AvailableSteps = TotalSteps - Step &
     jia.add_fatigue(jia.path_length(AgX,AgY,GX,GY),NG,  CN4) &
     jia.add_fatigue(jia.path_length(GX,  GY,DX,DY),NG+1,CN5) &
     AvailableSteps > (CN4 + CN5) * 1.1.
