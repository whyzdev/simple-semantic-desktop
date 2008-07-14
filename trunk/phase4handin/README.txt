Chris Fry
Akshat Singhal
Compilers - Phase 4 - Code Generation.
-----------------------------------------------------------------------
acc.java is the main file of the program (i.e. contains the main method).
"acc" stands for "Akshat and Chris Compiler".

Syntax:
javac *.java
java acc testfile.c

the output of the compiler will be output to the file testfile.c.s.

Optionally, running the program as:

./accompile cfilename

will compile the Csub file cfilename.c in ACC, then compile it in gcc,
and would print the integer output of main() to stdout.

* Note: System output from the compiler is collected in the file accoutput.
    
Also, in order to compare with GCC, you can run the shell script:
./gccompile cfilename
and it would do the same using GCC.


----------------------------------------------------
Please note: "compiler error" - all errors that we think the user should never ever get, and 
errors that occurred because of bad coding on our parts are marked as compiler 
errors so that the  hypothetical customer/user can call us and tell us that they found a "bug".

**************************************************