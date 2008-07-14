/*

additup.h - (header file for) a program to take arbitrarily large numbers from stdin (one
line at a time) and print their sum.

*/

#ifndef BIGINT
#define BIGINT 1
#define TRUE 1
#define FALSE 0

struct bigint {
  short  digit;
  struct bigint *next,*prev;
  short isTail;
  short  isHead;
} ;

typedef struct bigint BigInt;


#endif

