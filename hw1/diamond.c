/*
diamond.c - prints a beautiful diamond
Akshat Singhal, Fall 2007
*/
#include <stdio.h>

int getint();

int main(){
  int spaces=0, stars=0, n=0;
  printf("I will print a diamond for you, enter a size between 1-9: ");
  n = getint();
  printf("\n");
  printf("I have for you a wonderful diamond of size %d: \n\n",n);
  spaces = n -1;
  stars = 1;
  

  while(stars <= n*2){
    for (int k = 0;k<spaces;k++)
      putchar(' ');
    for (int k = 0;k<stars;k++)
      putchar('*');    
    spaces--;
    stars = stars + 2;
    putchar('\n');
  }

  stars -=4;
  spaces = 1;

  while(stars >= 1){
    for (int k = 0;k<spaces;k++)
      putchar(' ');
    for (int k = 0;k<stars;k++)
      putchar('*');    
    spaces++;
    stars = stars - 2;
    putchar('\n');
  }

  return 0;
}


  int getint(){
    int c;    
    while (!(((c=getchar()) >= '0' && c <= '9') || (c == EOF)))
      ;

    if (c >= '0' && c <= '9')
      return c - '0';
    else 
      return -1;    
  }
