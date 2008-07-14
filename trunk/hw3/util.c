#include <math.h>
#include <stdio.h>
#include "util.h"


#ifndef M_PI
#define M_PI           3.14159265358979323846  /* pi */
#endif

double Pi=M_PI;


double  getdistance(double x1,double y1,double x2,double y2){
  return sqrt( pow(x1 - x2,2) + pow(y1 - y2,2) );  
}


int getline(char s[], int lim) {

  /* A function to read in a line of input from the user
     read characters using getchar() until a newline is reached and store the values up to lim-1 in s. If the newline fits, copy it in.
     return the number of characters read into s, not including the null
     be sure s is null terminated
  */
  char c;
  int k=0;
  lim = lim -1;
  while ((c=getchar()) != '\n' && c != EOF){
    if (k < lim)
      s[k++]=c;
  }
  if (k < lim)
    s[k++]='\n';
  s[k]='\0';
  return k;
  
}

int myatoi(char s[]){
  /*
    A function to take a line from getline() and convert it to an int
  */
  int k=0, value=0;
  while (s[k] != '\0' && s[k] != '\n'){
    value = (value *10) + (s[k++]-'0');
    //dprint((s[k-1]-'0'));
  }
  // dprint(value);
  return value;
}


double myatof(char s[]) {
  /* A function to take a line from getline() and convert it to an double */
  int k=0,j=0;
  double value=0;
       
  while (s[k] != '\0' && s[k] != '.' && s[k] != '\n'){
    value = value *10 + (s[k++]-'0');
  }
       
  if (s[k] == '.'){
    j=k++;
    while (s[k] != '\0' && s[k] != '\n'){
      value = value + ((double)(s[k]-'0'))/pow(10,(double)(k-j));
      k++; 
    }       
  }
  return value;			  
}


double radians(double degrees){
  /* Convert degrees to radians*/
  return degrees * (Pi/180);
}

double degrees(double radians){
  return radians * (180/Pi);
}
 
