

CC = gcc 
CFLAGS = -g -std=c99 -Wall -pedantic

all: additup

sum:	additup.c additup.h
	$(CC) $(CFLAGS) additup.c -o additup

clean:
	-rm -f additup *~  *.o
