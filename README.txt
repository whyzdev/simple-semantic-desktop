Chris Fry
Akshat Singhal
Compilers - Phase 3 - Semantic Analysis

acc.java is the main file of the program (i.e. contains the main method).
"acc" stands for "Akshat and Chris Compiler".

Syntax:
javac *.java
java acc testfile.c

the output of the  compiler will be output to the file testfile.c.s.





Optionally, running the program as:

./accompile cfilename

will compile the Csub file cfilename.c in ACC, then compile it in gcc,
and would print the output to System.out.




Please note: "compiler error" - all errors that we think the user should never ever get, and 
errors that occurred because of bad coding on our parts are marked as compiler 
errors so that the  hypothetical customer/user can call us and tell us that they found a "bug".

**************************************************
Two things we are aware of don't work: global variables have not been implemented.
Also, there are pointer issues that may crop up. We have tested them, but aren't sure that we have
hit all possible combinations of pointer use.