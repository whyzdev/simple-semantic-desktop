all: rundmc_c rundmc_java
rundmc_c: rundmc.c
	gcc rundmc.c -o rundmc -Wall -std=c99 -O4 -pedantic
rundmc_java: rundmc.java
	javac rundmc.java
rundmc_gcj: rundmc.java
	gcj --main=rundmc -o rundmc.jcompiled -O4 rundmc.java
clean:
	rm -f *~ rundmc rundmc.class
