#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <unistd.h>
#include <stdlib.h>

#define TRUE 1
#define FALSE 0


#define ENCODING_BUFFER_SIZE 500
#define MAX_CODE_LENGTH 50
#define NOT_FOUND -2

typedef struct symbol symbol;


int getsymbol(char *buf);
char *codes[257];

int main(int argc, char *argv[]){
  //1. Initialization
  int c=0,freq=0;
  char encodingstring[MAX_CODE_LENGTH];


  int i;
  for(i=0;i<257;i++){
    codes[i]=malloc(MAX_CODE_LENGTH * sizeof(char));
    strcpy(codes[i],"");
  }
  
  // 1.a. init: file pointers
  FILE *inputfile;
  FILE *outputfile;
  inputfile=NULL;
  outputfile=NULL;

  if (argc >=2)
    inputfile = fopen(argv[1],"r");
  
  if (argc >=3)
    outputfile = fopen(argv[2],"w");
  
  if (inputfile == NULL) {
    fprintf(stderr, "Can't open input file. \n");
    exit(1);
  }

  if (outputfile == NULL) {
    outputfile=stdout;
  }

  // 2.Read encodings into a buffer
  int endcon=FALSE;
  char buf[ENCODING_BUFFER_SIZE];

  while(!endcon){    
    if (fgets(buf,ENCODING_BUFFER_SIZE,inputfile)==NULL)
      endcon=TRUE;

    if (strcmp(buf,"\n")==0)
      endcon=TRUE;
    sscanf(buf,"%d\t%d\t%s",&c, &freq, encodingstring);
    if (endcon != TRUE){
      if (c != -1){
	strcpy(codes[c],encodingstring);
      }
      else {
	strcpy(codes[256],encodingstring);
      }    
    }
        
  }
  
  // 2.Read encoded message and print decoded output
  int bufctr=0;
  int c_out=0;
  while((c=fgetc(inputfile))!=EOF){
    buf[bufctr++]=(char)c;
    buf[bufctr]='\0';

    if ((c_out=getsymbol(buf)) != NOT_FOUND){
      if (c_out != 256)
	fputc(c_out,outputfile);
      bufctr=0;
    }
  }
    

  // 3. Cleanup
  for(i=0;i<257;i++){
    free(codes[i]);
  }


  fclose(inputfile);
  fclose(outputfile);




  return 0;
}

int getsymbol(char *buf){  
  /*
    getsymbol() - given a code, returns the character that 
    the code represents according to the given file's huffman tree.
    returns NOT_FOUND if a character for the code is not found
  */
  int i;

  for (i=0;i<257;i++){
    if (strlen(codes[i])>0){
      if (strcmp(buf,codes[i])==0)
	return i;
    }
  }
  return NOT_FOUND;
}
