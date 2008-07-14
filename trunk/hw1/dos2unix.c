/*
dos2unix.c - Converts text files from DOS to Unix Format 
Akshat Singhal


*/

#include <stdio.h>

int main(){
  int c;
  while ((c=getchar()) != EOF)
    if (c == '\r'){
      if ((c=getchar()) == '\n')
	putchar('\n');
    }
    else
      putchar(c);
    
  return 0;
}
