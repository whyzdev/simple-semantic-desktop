#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <unistd.h>
#include <stdlib.h>

#define TRUE 1
#define FALSE 0

#define INTERNAL_NODE -2
#define ROOT_NODE -3
#define NON_EXISTENT -1

#define ENCODING_BUFFER_SIZE 20

#define NOT_NULL 1


typedef struct symbol symbol;

struct symbol
{
  int character;
  int frequency;
  symbol *prev;
   symbol *next;
   symbol *left;
   symbol *right;
   symbol *parent;
};
 


void update_frequency(symbol *r, int c);
int count_parentless_nodes(symbol *r);
symbol *get_rarest_parentless_node(symbol *r);
symbol *new_node(symbol *n1, symbol *n2);
int get_frequency(symbol *h, int c);
char *get_encoding(symbol *h, int c);


symbol *init_symbol(int c);
symbol *remove_symbol(symbol *r, int c);
symbol *seek_symbol(symbol *r, int c);
void insert_symbol(symbol *r, symbol *s);
void free_symbol(symbol *r);
int exists_symbol(symbol *r,int c);
void print_symbol(symbol *s);
void print_frequency_order(symbol *r);
void str_reverse(char * buf, int buflen);


int main(int argc, char *argv[]){

  // 1. Initialization
  int c;
  symbol *root;
  root=init_symbol(ROOT_NODE);
  root->frequency=NON_EXISTENT;
  root->parent=root;
  
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
  
  // 2. count the frequency of each character in inputfile 
  while((c=fgetc(inputfile))!=EOF){
    update_frequency(root,c);
  }
  update_frequency(root,-1);


  // 3. Make the huffman tree
  while(count_parentless_nodes(root) > 1){
    symbol *node1,*node2, *newnode;
    node1 = get_rarest_parentless_node(root);
    node1->parent=node1; //temporarily
    node2 = get_rarest_parentless_node(root);
    node2->parent=node2; //temporarily
    newnode = new_node(node1, node2);
    insert_symbol(root,newnode);
  }


  // 4. Output symbol mappings
  for(c=-1;c<256;c++){
    char *encodingstring;
    encodingstring=get_encoding(root,c);
    int freq=get_frequency(root,c);
    if (freq > 0){
      /*if (c <=127 && c>=32 )
	fprintf(outputfile,"%c\t%d\t%s\n", c, freq, encodingstring);    
      else */
	fprintf(outputfile,"%d\t%d\t%s\n", c, freq, encodingstring);    
    }
    free(encodingstring);
  }
  fprintf(outputfile,"\n");    

  
  // 5. Output encoding of inputfile
  rewind(inputfile);
  while((c=fgetc(inputfile))!=EOF){
    char *encodingstring;
    encodingstring=get_encoding(root,c);
    fprintf(outputfile, "%s", encodingstring);
    free(encodingstring);
  }
  char *encodingstring;
  encodingstring=get_encoding(root,EOF);
  fprintf(outputfile, "%s", encodingstring);
  free(encodingstring);
  fprintf(outputfile,"\n");    

  free_symbol(root);
  
  
  // 6. Close file pointers
  fclose(inputfile);
  fclose(outputfile);
}



void update_frequency(symbol *r, int c){
  /*
    update_frequency() - given the root of a symbol table-tree and a character,
    increases the frequency of that character in the symbol table-tree,
    creating nodes as necessary.
  */
  symbol *s;
  if (!exists_symbol(r,c))
    s=init_symbol(c);
  else
    s=remove_symbol(r,c);

  s->frequency++;     
  insert_symbol(r,s);
}

int count_parentless_nodes(symbol *r){
  /* count_parentless_nodes() - 
     when called from the head node of a symbol table-tree,
     returns the number of parentless nodes in it.
  */

  if (r->next==NULL){
    if (r->parent==NULL)
      return 1;
    else 
      return 0;
  }
  else {
    if (r->parent==NULL)
      return 1+count_parentless_nodes(r->next);
    else 
      return count_parentless_nodes(r->next);
  }

}



symbol* get_rarest_parentless_node(symbol *r){
  /* get_rarest_parentless_node() - 
     when called from the head node of a symbol table-tree,
     returns a pointer to the parentless node with the 
     lowest frequency.
  */
  
  if (r->character!= ROOT_NODE && r->parent == NULL)
    return r;
  else if (r->next != NULL)
    return get_rarest_parentless_node(r->next);
  else {
    fprintf(stderr,"ERROR: Unexpected error: too much pruning");
    return NULL;
  }      
}



symbol* new_node(symbol *n1,symbol *n2){
  /* new_node() - 
     given two symbols from the symbol table-tree,
     combines them into one Internal Node and then 
     returns a pointer to the new internal node.
  */

  // Init 
  symbol *s;
  s=init_symbol(INTERNAL_NODE);

  // Set left and right children
  s->left=n1;
  s->right=n2;
  
  
  // Set parents of children
  n1->parent=s;
  n2->parent=s;

  // Set frequency 
  s->frequency = n1->frequency + n2->frequency;

  return s;
}


int get_frequency(symbol *h, int c){
  /*
    get_frequency() - given the root of a symbol table-tree and a character, 
    returns the frequency of that character in the table.
  */
  symbol *s;
  s= seek_symbol(h,c);
  if (s!=NULL)
    return s->frequency;  
  else 
    return 0;
}

char* get_encoding(symbol *h, int c){
  /*
    get_encoding() - given the root of a symbol table-tree and a character, 
    returns the encoding (i.e. the code) of that character as a char*
  */
  char *buf;
  buf = malloc(sizeof(char)*ENCODING_BUFFER_SIZE);
  int bufctr=0;
  
  symbol *s, *s_i;
  s=seek_symbol(h,c);
  if (!(s == NULL)){
    s_i=s;
    while(s_i->parent != NULL){    
      if (s_i->parent->left==s_i)
	buf[bufctr++]='0';
      else if (s_i->parent->right==s_i)      
	buf[bufctr++]='1';
      else 
	fprintf(stderr,"ERROR: unexpected error, weirdly orphaned node detected");    
      s_i=s_i->parent;
    }
  }
  buf[bufctr]='\0';  
  str_reverse(buf,bufctr);

  return buf;  
}


 

symbol* init_symbol(int c){
  /*
    init_symbol() - creates a fresh symbol table entry at the
    passed pointer location (s) for the character (c), which
    can also be an INTERNAL_NODE.
  */

  symbol *s;
  // memory allocation
  s = malloc(sizeof(symbol));
  
  
  // init
  s->character=c;
  s->frequency=0;

  // set all pointers to null
  s->prev=NULL;
  s->next=NULL;
  s->left=NULL;
  s->right=NULL;
  s->parent=NULL;

  return s;
}


symbol* remove_symbol(symbol *r, int c){  
  /*
    remove_symbol() - seperates the symbol representing the passed character (c) 
    from the symbol table-tree whose root is (r)
  */

  // 1. find the symbol for (c)
  symbol *s;
  s=seek_symbol(r,c);

  // 2. Set the neighbours right
  if (s->prev != NULL)
    s->prev->next=s->next;

  if (s->next != NULL)
    s->next->prev=s->prev;

  // 3. chop off neighbours
  s->next=NULL;
  s->prev=NULL;

  // 4. return symbol
  return s;
}

symbol* seek_symbol(symbol *r, int c){
  /*
    seek_symbol()-
   returns a pointer to the exact symbol table entry 
   in the table (r) carrying the character (c)
  */

  if (r->character == c)
    return r;
  if (r== NULL)
    return NULL;
  else if (r->next == NULL)
    return NULL;
  else if (r->next == r){
    fprintf(stderr,"ERROR: bad programmer\n");
    return NULL;
  }
  else {
    return seek_symbol(r->next,c);
  }
}



void insert_symbol(symbol *r,symbol *s){
  /*
    insert_symbol() - inserts symbol s in the approporiate
    place in the symbol table-tree represented by r
  */

  if (r->frequency < s->frequency){
    if (r->next != NULL && r->next->frequency < s->frequency)
      insert_symbol(r->next,s);    
    else {
      s->next = r->next;
      if (r->next != NULL)
	r->next->prev=s;
      r->next = s;
      s->prev=r;

    }    
  }

  else if (r->frequency > s->frequency) {
    fprintf(stderr,"ERROR: insertion error\n");
  }

  else if (r->frequency == s->frequency || 
      (r->frequency > s->frequency && 
       r->prev->frequency < s->frequency)){
    s->prev=r->prev;
    if (r->prev != NULL)
      r->prev->next=s;
    r->prev=s;
    s->next=r;

  }
  
}

void free_symbol(symbol *r){
  /* 
free_symbol() - frees memory used by the passed symbol and all others after it 
*/
  if (r->next != NULL){
    free_symbol(r->next);    
  }
  free(r);      
}

int exists_symbol(symbol *r, int c){
  /* 
     exists_symbol() - returns a boolean as to whether the passed character(c) 
     has an entry in the symbol table-tree represented by r
  */

  symbol *s;
  s=seek_symbol(r,c);

  if (s == NULL)
    return FALSE;
  else 
    return TRUE;
}

void print_symbol(symbol *s){
  /* 
     print_symbol() - diagnostic function to print a symbol. Not very good
*/
  if (s->character != ROOT_NODE && s->character != INTERNAL_NODE ){
    print_symbol(s->left);
    print_symbol(s->right);
  }
  if (s!=NULL)
    printf("%c\n",s->character);  
}

void print_frequency_order(symbol *r){
  /* 
     print_frequency_order() - diagnostic function to print the frequencies in a symbol table-tree. Not very good
  */
  printf("%d,",r->frequency);
  if (r->next != NULL)
    print_frequency_order(r->next);
  else
    printf("\n");
}

void str_reverse(char * buf, int buflen){
  /*
    str_reverse()- given a string and its length, reverses the string inplace
  */
  int i;
  char temp;
  for( i=0;i<buflen/2;i++){
    temp = buf[buflen-i-1];
    buf[buflen-i-1] = buf[i];
    buf[i]=temp;
  }  
}
