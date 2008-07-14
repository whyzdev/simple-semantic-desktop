/*

format.c : command line text formatting utility
written by Akshat Singhal, Fall 2007


*/


#include <stdio.h>
#include <string.h>

#define TRUE 1
#define FALSE 0
#define MAX_WORD 100
#define MAX_SENTENCE 1000


int myatoi(char s[]);
int format(int w, int rightalign, int justified, int skipmultiple);
void printLeft(char *sentence, int len);
void printRight(char *sentence, int len, int w);
void printJustified(char *sentence, int len, int w);
void showhelp();

int main(int argc, char *argv[]){

  int i=0;
  int width=72;
  int rightalign=FALSE;
  int justified=FALSE;
  int skipmultiple=FALSE;

  //1. Handle command line flags
  if (argc > 1){
    for(i=1;i<argc;i++){
      if((!strcmp(argv[i],"-w")) &&  (i<argc-1))
	width = myatoi(argv[++i]);      
      else if (!strcmp(argv[i],"-r")){
	rightalign=TRUE;
	justified=FALSE;
      }
      else if (!strcmp(argv[i],"-j")){
	justified=TRUE;
	rightalign=FALSE;
      }
      else if (!strcmp(argv[i],"-s"))
	skipmultiple=TRUE;      
      else if (!strcmp(argv[i],"-h")){
	showhelp();
	return 1;
      }
    }
  }
  // 2. Check Width
  if (width > MAX_SENTENCE){
    fprintf(stderr, "Invalid width: %d.\nEnter a width between %d and %d\n", width,1,MAX_SENTENCE);
    return -1;
  }
  
  // 3. call format()
  format(width,rightalign,justified, skipmultiple);
  return 0;
}


int myatoi(char s[]){
  /*
    myatoi(): a function to take a line of characters and convert it to an int
  */
  // 1. Init
  int k=0, value=0;
  
  // 2. Loop until you reach the end of the line or string
  while (s[k] != '\0' && s[k] != '\n'){
    // 3. Left Shit and Add to the variable value 
    value = (value *10) + (s[k++]-'0');
  }
  return value;
}


int format(int w, int rightalign, int justified, int skipmultiple){

  //1. Init
  int c;
  int ct_spaces=0;
  int ct_newlines=0;
  int n_lines=0;
  int n_paragraphs=0;
  char lastword[MAX_WORD+1];
  char sentence[MAX_SENTENCE+1];
  int i_sentence=0;
  int i_lastword=0;
  char nextchar;
  int i=0;
  int n_newlines_to_print=0;
  int EOFCondition = FALSE;


  // 2. Loop until EOF is reached


  while(!EOFCondition){    
    c=getchar();
    if (c==EOF)
      EOFCondition=TRUE;

    // 3. Count a newline or space if it occurs
    if (c==' ' || c=='\t') {
      ct_spaces++;
    }
    else if (c=='\n') {
      ct_newlines++;
    }
    else {      
      // 3. If a char is encountered after one newline, just append a space to output
      if (ct_newlines ==1){
	ct_newlines=0;
	ct_spaces=0;
	nextchar=' ';
      }
      // 3. If a char is encountered after more than one newline, 
      //    append a newline (or more) to output
      else if (ct_newlines > 1){
	nextchar='\n';
	n_paragraphs++;
	ct_spaces=0;
	// 3.a. Append only one newline if skipmultiple=TRUE, 
	//      otherwise append as many newlines as in Input.
	if (skipmultiple)
	  n_newlines_to_print=1;
	else 
	  n_newlines_to_print=ct_newlines;
	ct_newlines=0;
      }

      // 3. If a char is encountered after one newline, just append a space to output
      else if (ct_spaces >= 1){
	nextchar=' ';
	ct_spaces=0;
      }
      // 3. If a char is encountered after a char, append it to output
      else {
	nextchar=c;	
	lastword[i_lastword++]=c;
	ct_spaces=0;
      }
      // 4. If this should be the end of a sentence
      if (nextchar == '\n' || nextchar == ' ' || EOFCondition){	
	// 5. add the last word to sentence if it fits
	if ( (i_sentence + i_lastword -1)< w){
	  for(i=i_sentence;i< (i_sentence + i_lastword);i++)
	    sentence[i] = lastword[i-i_sentence];
	  i_sentence=i;	
	}
	// 5. print out old sentence and add last word to a new sentence 
	//    (if last word does not fit)
	else {	  
	  sentence[i_sentence++]='\0';
	  if (rightalign)
	    printRight(sentence,i_sentence,w);
	  else if (justified)
	    printJustified(sentence,i_sentence,w);
	  else 
	    printLeft(sentence,i_sentence);	  
	  // 6. add last word to a new sentence
	  for(i_sentence=0;i_sentence<i_lastword;i_sentence++)
	    sentence[i_sentence]=lastword[i_sentence];	  	 
	}
	
	// 7. add the next character to sentence
	sentence[i_sentence++]=nextchar;
	n_newlines_to_print--;

	// 8. If the next character to add to line is a newline,
	if (nextchar=='\n'){
	  // 9. add as many newlines to sentence as needed
	  while((n_newlines_to_print--) > 0)
	    sentence[i_sentence++]=nextchar;	
	  sentence[i_sentence++]='\0';

	  // 10. print the current sentence as per alignment
	  if (rightalign)
	    printRight(sentence,i_sentence,w);
	  else if (justified)
	    printJustified(sentence,i_sentence,w);
	  else 
	    printLeft(sentence,i_sentence);	  
	  i_sentence=0;
	}
	
	// 11. Start adding to the last word
	i_lastword=0;
	lastword[i_lastword++]=c;
	
	// 12. If the End of File has been reached,
	if (EOFCondition) {
	  sentence[i_sentence++]='\0';
	  // 13. print the last remaining sentence as per alignment
	  if (rightalign)
	    printRight(sentence,i_sentence,w);
	  else if (justified)
	    printJustified(sentence,i_sentence,w);
	  else 
	    printLeft(sentence,i_sentence);
	}
	  
      }                 
    }
    // 14. Repeat from Step 3.
  }
  
  return n_lines;
}


void printLeft( char *sentence, int len){
  /*
    printLeft(): prints a string using Left Align
  */

  int i=0;
  while (i<len)
    putchar(sentence[i++]);
  putchar('\n');	  
}

void printRight( char *sentence, int len, int w){
  /*
    printRight(): prints a string using Right Align
  */


  int i_last_non_ws=0;  
  int i=0;

  i=0;
  while (i<len){
    if(sentence[i]!=' ' && sentence[i]!='\n' && sentence[i]!='\0')
      i_last_non_ws=i;
    i++;
  }

  int n_spaces = w - i_last_non_ws +1 ;

  i=0;
  while (i++<n_spaces)
    putchar(' ');

  i=0;
  while (i<len)
    putchar(sentence[i++]);
  putchar('\n');	  
}

void printJustified(char *sentence, int len, int w){
  /*
    printJustified(): prints a string using Justified Align
  */

  int i_last_non_ws=0;  
  int i=0;

  i=0;
  while (i<len){
    if(sentence[i]!=' ' && sentence[i]!='\n' && sentence[i]!='\0')
      i_last_non_ws=i;
    i++;
  }

  int n_spaces = w - i_last_non_ws +1 ;


  int n_gaps = 0;    
  int i_gap=0;
 
  i=0;
  while (i<len){
    if (sentence[i] == ' ' && i < len-2)
      n_gaps++;
    i++;
  }

  int total_spaces = n_spaces + n_gaps;
  int last_total=0;

  i=0;
  while (i<len){
    if (sentence[i] != ' ')
      putchar(sentence[i]);
    else if (i<len){
      i_gap++;
      int j=0;
      while(j++<(((i_gap * total_spaces)/n_gaps)-last_total))
	putchar(' ');
      last_total=(i_gap * total_spaces)/n_gaps;
    }
    i++;
  }
  putchar('\n');	  
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
-s : Skip multiple lines, i.e. ignore more than two lines\n\
-r : Right align output\n\
-j : Justify output\n\
-w <width> : set the width of output, defaults to 72\n\
\n");
}
