

CC = gcc 
CFLAGS = -g -std=c99 -Wall -pedantic

all: sum

sum:	sum.c sum.h
	$(CC) $(CFLAGS) sum.c -o sum

clean:
	-rm -f sum *~  *.o
