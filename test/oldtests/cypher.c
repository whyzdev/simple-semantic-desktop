/*
Chris Fry -- cfry
cypher.c
*/

# include <stdio.h>
# include <stdlib.h>
# include "cypher.h"
# include "char_fun.h"

int main()
{
  cypher();
  return 0;
}

void cypher()
{
  FILE* ifp;
  FILE* ofp;
  char c;
  char* inputArray;
  char* outputArray;
  int key, i, j, k, l;

  ifp = fopen("input.txt", "r");
  ofp = fopen("output.txt", "w");

  numChars = 0;
  while(fscanf(ifp, "%c", &c) != EOF) 
    {
      ++numChars;  
    }

  fclose(ifp);

  inputArray = (char*)malloc(numChars * sizeof(char));
 
  ifp = fopen("input.txt", "r");

  l = 0;
  while(fscanf(ifp, "%c", &c) != EOF) 
    {
      inputArray[l] = c;
      ++l;  
    }

  printf("Please enter key: ");
  scanf("%d", &key);

  if(key < 0)
    {
      printf("Error, key less than zero.");
      exit(1);
    }

  key = key%26;

  outputArray = (char*)malloc(numChars * sizeof(char));

  for(k = 0; k < i; ++k)
    {
      c = inputArray[k];

      if(isLetter(c))
	{
	  if(isUpperCase(c))
	    {
	      c += key;
	      if(!isUpperCase(c))
		c -= 26;
	    }
	  else
	    {
	      c += key;
	      if(!isLowerCase(c))
		c -= 26;
	    }
	}
     
      outputArray[k] = c;
    }

  for(j = 0; j <= i; ++j)
    {
      fprintf(ofp, "%c", outputArray[j]);
    }

  fclose(ifp);
  fclose(ofp);
}
