/*

additup.c - a program to take arbitrarily large numbers from stdin (one
line at a time) and print their sum.

*/


#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "additup.h"


BigInt zero;

/* additup.c */
int main(int argc, char **argv);
void test_BigInt(void);
BigInt *append_BigInt(BigInt *a, int digit);
void init_BigInt(BigInt *b);
void free_BigInt(BigInt *b);
void sum_BigInt(BigInt *a, BigInt *sum);
void print_BigInt(BigInt *b);



int main(int argc, char **argv){
  /*
    main(): the main function
  */

  // 1. Init
  BigInt *current, *head_current, *sum, *head_sum;
  int c;
  head_current=malloc(sizeof(BigInt));
  current=head_current;

  head_sum=malloc(sizeof(BigInt));
  sum=head_sum;
  
  // 2. Initialize the current BigNum holder (a), and the 
  //    Sum Accumulator (sum), and the zero BigNum (zero)
  init_BigInt(current);
  init_BigInt(sum);
  init_BigInt(&zero);

  // test_BigInt();
  
  // 3. Loop until EOF
  while((c=getchar())!=EOF){
    if (!isspace(c)){    
      //4. if a newline is encountered,         
      //4. if a numeric char is encountered, 
      if ( c>= '0' && c <='9'){
	// 5. Add a BigInt link to a for this digit
	current=append_BigInt(current,c-'0');	
      }      
    }

    if (c=='\n'){

      // 4. Print the number we just read in
      //print_BigInt(head_current);
      putchar('\n');      

      // 5. Add 'a' to what we've summed up so far
      sum_BigInt(head_current,head_sum);

      // 6. Deallocate 'a' and point it to a fresh BigInt
      free_BigInt(head_current);	      
      head_current=malloc(sizeof(BigInt));
      current=head_current;
      init_BigInt(current);

      // 7. print the sum      
      printf("Total:");
      print_BigInt(head_sum);
      putchar('\n');      
    }
    else 
      putchar(c);

  }

  // 8. Free the remaining BigInts
  free_BigInt(head_current);	
  free_BigInt(head_sum);	      

}

void test_BigInt(){
  /*
    test_BigInt() : tests the BigInt functions
  */
  
  BigInt *test, *t, *test2, *t2;
  test=malloc(sizeof(BigInt));
  test2=malloc(sizeof(BigInt));

  t=test;
  t2=test2;

  init_BigInt(test);
  init_BigInt(test2);
  printf("---------------\ntest:\n\n");
  t=append_BigInt(t,4);
  t=append_BigInt(t,2);
  t=append_BigInt(t,0);
  print_BigInt(test);
  printf("\n");
  t2=append_BigInt(t2,7);
  t2=append_BigInt(t2,4);
  t2=append_BigInt(t2,3);
  print_BigInt(test2);
  printf("\n");
  sum_BigInt(test,test2);
  print_BigInt(test2);

  printf("\n");
  printf("---------------\n");
  free_BigInt(test);
  free_BigInt(test2);

}


BigInt* append_BigInt(BigInt *a, int digit){
  /*
    append_BigInt(): Given the tail of a BigInt, 
    append Given digit to it
  */

  if (digit <=9 && digit >=0){
    // 1. Set a's digit to passed digit and make it not tail
    a->digit=digit;
    a->isTail=FALSE;

    // 2. create and initialize new BigInt and attach it as
    //    the BigInt after 'a'
    a->next=malloc(sizeof(BigInt));
    a->next->digit=-1;
    a->next->prev=a;      
    a->next->next=NULL;      
    a->next->isTail=TRUE;
    a->next->isHead=FALSE;

    // 3. Move the pointer on to the next (newly init-ed) BigInt
    a=a->next;
  }
  return a;
}


void init_BigInt(BigInt *b){
  /*
    init_BigInt(): Initialize a fresh BigInt to 0
  */

  b->digit=0;
  b->isTail=TRUE;
  b->isHead=TRUE;
  b->next=NULL;
  b->prev=NULL;
}


void free_BigInt(BigInt *b){
  /*
    init_BigInt(): Initialize a fresh BigInt to 0
  */

  BigInt *c, *temp;
  c=b;
  
  // 1. Rewind to the head of the BigInt linkedlist
  while(!c->isHead){
    c=c->prev;
  }

  // 2. Start freeing BigInts starting from the head
  //    until the tail is reached
  while(!c->isTail){
    temp=c->next;
    free(c);
    c=temp;
  }
  // 3. Free the tail
  free(c);
}


void sum_BigInt(BigInt * a, BigInt *sum){
  /*
    sum_BigInt(): add two BigInts and store output in the second one.
  */

  // 1. Init
  BigInt *i, *s;
  i=a;
  s=sum;
  short accumulator=0;
  int ReachedSumHead=FALSE, ReachedIHead=FALSE;

  // 2. move ahead to the end of both BigInts
  while(!s->isTail)
    s=s->next;
  while(!i->isTail)
    i=i->next;

  accumulator += s->digit + i->digit;    
  s->digit=accumulator % 10;      
  accumulator = accumulator /10;

  // 3. Loop Until the Heads of both s and i is reached
  while(! (ReachedSumHead  && ReachedIHead)){     

    // 4. Move to the previous node of i until its head is reached.
    if (!i->isHead)
      i=i->prev;
    // 4. Set i to zero if its Head has been reached
    else {
      i=&zero;
      ReachedIHead=TRUE;
    }

    // 5. Move to the previous node of s until its head is reached
    if(!s->isHead)
      s=s->prev;    
    // 5. If head is reached, append a new head before it
    else{
      // 6. Create and properly Init a BigInt for the head's head
      s->prev=malloc(sizeof(BigInt));
      init_BigInt(s->prev);
      s->prev->isHead=TRUE;
      s->prev->isTail=FALSE;
      s->prev->next=s;
      s->prev->prev=NULL;

      // 7. Move s to the new head
      s->isHead=FALSE;
      s=s->prev;            
      ReachedSumHead=TRUE;          
    }

    // 8. Add s and i's digits to accumulator 
    accumulator += s->digit + i->digit;    
    // 9. Append the tens digit of accumulator
    //    to the head of s.
    s->digit=accumulator % 10;      
    // 10. Subtract this added digit from accumulator
    //     and then divide it by 10.
    accumulator = accumulator /10;
    
    // 11. If the end of both was reached and there's
    //     an extra leading zero in s, remove it.
    if (ReachedSumHead && ReachedIHead && s->digit==0){
      s->next->isHead=TRUE;
      s=s->next;
      free(s->prev);
      s->prev=NULL;    
    } 
    
  }
}


void print_BigInt(BigInt *b){
  /*
    print_BigInt(): Prints a BigInt.
  */
  BigInt *c;
  c=b;

  // 1. Rewind to the head of the BigInt linkedlist
  while(!c->isHead){
    c=c->prev;
  }

  // 2. Print the digit in each linked BigInt until
  //    the tail is reached.
  while(!c->isTail){
    putchar('0' + c->digit);
    c=c->next;
  }
}

