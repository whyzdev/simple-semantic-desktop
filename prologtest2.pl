parents(brandon, alice, greg).
parents(alice, sue, bob).
parents(greg, lucille, bud).

granparents(P, Gmom, GPa) :-parents(P,M,D), parents(M,GMom, GPa).
size([],0).
size([H|T], N):-size(T,N1), N is N1+1.
sum([],0).
sum([H|T], N):-sum(T,N1), N is N1+H.

factorial(0,1).

factorial(N, F):-
    N>0,
    N1 is N-1,
    factorial(N1, F1),
    F is N * F1.

reverse([],[]).
    reverse([H|T],[R]):-reverse(T,R1), R is [R1|H].
