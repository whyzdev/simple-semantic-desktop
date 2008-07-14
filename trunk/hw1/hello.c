/*
hello.c - Hello World 
Askaht Singhal, Fall 2007, Oberlin College

This progrm says Hello world.
*/


#include <stdio.h>

int main(){

  int k=42;
  float p = (22.0/7.0)*1000;
  printf("%s", "Hello World\n");
  printf("I am the king of the world when i have C! These numbers came out of magic! %d , %d, %d \n", *(&k + 1),*(& k + 2),*( & k + 3));
  printf("%%Xd test: %9d\n", k );
  printf("%%f test: %f\n", p);
  printf("%%X.Yf test: %3.4f\n",p);
  printf("%%.Yf test: %.7f\n", p);



  return 0;

}
