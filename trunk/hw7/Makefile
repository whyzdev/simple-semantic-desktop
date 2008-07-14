

CC = g++ 
CFLAGS = -g -Wall -pedantic -O3
OFLAGS = -c -g -O3

all: test1 test2 factorial

factorial:	factorial.cpp BigInteger.o BigInteger.h
	$(CC) $(CFLAGS) factorial.cpp BigInteger.o -o factorial

test1:	test1.cpp BigInteger.o BigInteger.h
	$(CC) $(CFLAGS) test1.cpp BigInteger.o -o test1

test2:	test2.cpp BigInteger.o BigInteger.h
	$(CC) $(CFLAGS) test2.cpp BigInteger.o -o test2

BigInteger.o: BigInteger.h BigInteger.cpp
	$(CC) $(OFLAGS) BigInteger.cpp -o BigInteger.o

clean:
	-rm -f test1 test2 factorial *~  *.o
