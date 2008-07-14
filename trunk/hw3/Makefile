# Demonstrating how things all work together

CC = gcc -g -std=c99 -Wall -pedantic

all: game

util:	util.c util.h
	$(CC) -o util.o -c util.c 

game:	game.c game.h util
	$(CC) -o game game.c util.o /usr/lib/libm.a

clean:
	-rm -f game  *~  *.o
