all: hello dos2unix diamond
hello: hello.c
	gcc hello.c -o hello -Wall -std=c99 -pedantic
dos2unix: dos2unix.c
	gcc dos2unix.c -o dos2unix -Wall -std=c99 -pedantic
diamond: diamond.c
	gcc diamond.c -o diamond -Wall -std=c99 -pedantic
clean:
	rm -f *~ hello dos2unix diamond
