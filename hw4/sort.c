/*

sort.c : command line sort utility
written by Akshat Singhal, Fall 2007


*/



#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "debug.h"

#define TRUE 1
#define FALSE 0
#define MAX_BUFFER 1024*1024 //1024*1024
#define MAX_LINE 1024 //1024

/* sort.c */
int main(int argc, char **argv);
int string_comparator(char* a, char* b);
void print_buffer(char **buf, int len);
void asort();
void showhelp();
long mystrtol(char *start, char **rest);
static int cmpr(const void *a, const void *b) ;
int numeric;
int folding;
int reverse;


int main(int argc, char **argv){
  // 1. Init
  numeric=FALSE;
  folding=FALSE;
  reverse=FALSE;
  int i=0;

  // 2. Handle command line flags
  if (argc > 1){
    for(i=1;i<argc;i++){
      if (!strcmp(argv[i],"-n"))
	numeric=TRUE;
      else if (!strcmp(argv[i],"-f"))
	folding=TRUE;
      else if (!strcmp(argv[i],"-r"))
	reverse=TRUE;
      else if (!strcmp(argv[i],"-h")){
	showhelp();
	return 1;
      }
    }
  }

  // 3. Call asort() to sort stdin
  asort();
  return 0;
}


void print_buffer(char *buf[], int len)
{
  int i;
  for(i=0; i<len; i++)
    printf("%s\n", buf[i]);
}


 
/* sorting C-strings array using qsort() example */
void asort()
{
  /*
    asort(): sort and output stdin
  */

  // 1. Init
  int c;
  char **buf;
  int ct_row=0;
  int ct_col=0;
  
  // 2. Initialize Buffer
  buf=malloc(MAX_BUFFER * sizeof(char *));
  buf[0]=malloc((MAX_LINE+1)*sizeof(char));


  // 3. Loop until EOF reached
  while((c=getchar())!=EOF){
    // 4. Add characters to buffer
    if (ct_row < MAX_BUFFER && ct_col < MAX_LINE){
      if (c!='\n')
	buf[ct_row][ct_col++]=c;         
    }    
    
    // 5. If newline reached, start feeding in a 
    //    fresh line to buffer
    if(c=='\n'){
      buf[ct_row][ct_col++]='\0';
      ct_row++;
      ct_col=0;
      if (ct_row<MAX_BUFFER)
	buf[ct_row]=malloc((MAX_LINE+1)*sizeof(char));
    }      
  }

  // 6. qsort() all lines of the buffer 
  qsort(buf, (unsigned)ct_row,  sizeof(char*), cmpr);

  // 7. print all lines of the buffer
  print_buffer(buf, ct_row);
  
  // 8. Repent for your sins
  while(ct_row > -1)
    free(buf[ct_row--]);  
  free(buf);
}
 
void showhelp(){
  /*
    showhelp(): prints the help information
  */

  printf("---------------------------------\n\
Simple Textfile formatter.\n\
---------------------------------\n\
- By Akshat Singhal\n\
\n\
Permitted flags:\n\
-h : help\n\
-f : (Foldable case) case insensitive comparison \n\
-n : Numeric sorting : sorting by the number in strings' beginning\n\
-r : Reverse sorting order (descending)\n\
\n");
}

long mystrtol(char *s, char **rest){
  /*
    mystrtol(): Parses out long integer from a given sentence
    and also sets a pointer to the rest of the string    
  */


  int k=0;
  long value=0;
  int lastnumeric=0;
  while (s[k] != '\0' && s[k] != '\n'){
    if (s[k] <='9' && s[k] >='0'){
      value = (value *10) + (s[k++]-'0');
      lastnumeric=k;
    }
    else 
      k++;
  }

  *rest=&s[lastnumeric];

  return value;
}

static int cmpr(const void *a, const void *b) { 
  /*
    cmpr(): Comparator method that compares as needed according to the 
    global variables numeric, reverse, and folding.
  */

  // 1. Init 
  char *A=*(char **)a;
  char *B= *(char **)b;
  int asc_order=0;

  

  // 2. If in Numeric mode, compare by numeric values 
  //    from the beginnings of lines.
  if (numeric) {
    char *endptr;
    int anum=mystrtol(A,&endptr);
    int bnum=mystrtol(B,&endptr);    
    if (anum > bnum)
      asc_order = 1;
    else if (anum==bnum)
      asc_order =0;
    else 
      asc_order = -1;
  }

  // 2. If not in Numeric mode, or if Numeric mode didn't
  //    find any difference, compare ASCII
  if ((!numeric) || asc_order==0){
    // 3. If folding is on, UPPERCASE both strings and compare
    if(folding){
      char* c=malloc((MAX_LINE+1)*sizeof(char));
      char* d=malloc((MAX_LINE+1)*sizeof(char));
      int i=0;
      for(i=0;i<MAX_LINE && A[i] != '\0';i++){
	c[i]=A[i];
	if (c[i] >= 'a' && c[i] <='z')
	  c[i]='A' - 'a'+c[i];
      }
      for(i=0;i<MAX_LINE && B[i] != '\0';i++){
	d[i]=B[i];
	if (d[i] >= 'a' && d[i] <='z')
	  d[i]='A' - 'a'+d[i];
      }
      asc_order=strcmp(c,d);
    }
    // 3. If folding is not on, do simple strcmp compare
    else 
      asc_order=strcmp(*(char **)a, *(char **)b);
  }

  //4. If Reverse mode, negate the comparison value and return, 
  //   otherwise, simply return the comparison value
  if (! reverse)
    return asc_order;
  else
    return -asc_order;
}
