/*
  rundmc.c - program to solve the N-queens problem using recursion
  - Akshat Singhal, CS 241, Fall 07, Oberlin College
  (also, a tribute to Run DMC's bangin' music - see solve())
*/

//C Preprocessor
#include <stdio.h>
#define MAX_QUEENS 20

//function prototypes
int getsize(int low, int high);
int getint(void);
void init_board(int board[], int max);
void print_board(int board[], int max);
int check_board(int board[], int row);
void solve(int board[], int current_row, int max);



int main(){
  //define board and init it
  int board[MAX_QUEENS];
  int size=getsize(1,MAX_QUEENS);
  init_board(board,size);

  //Solve board
  printf("\nSolutions to an %dx%d board:\n----------------------------\n",size,size);  
  solve(board,0,size);

  return 0;
}

int getsize(int low, int high){//prompts user for the size of N-queens problem
  printf("Please enter a number between %d and %d: ",low,high);
  int size=getint();
  if (size >= low && size <=high)
    return size;
  else
    return -1;
}

int getint(){//receives an integer from stdin
  int r=0;
  char c;
  while ((c=getchar()) != EOF && (c != '\n'))
    if (c >= '0' && c <= '9')
      r = r*10 + (c-'0');
  return r;
}

void init_board(int board[], int max){//initializes board
  for(int i=0;i<max;i++)
    board[i]=-1;
  return;
}

void print_board(int board[], int max){//prints a board
  for (int j=0;j<max*2 + 1;j++)
    putchar('-');
  putchar('\n');
  for (int i=0;i<max;i++){    
    putchar('|');   
    for (int k=0;k<max;k++){
      if (k==board[i])
	putchar('0');
      else
	putchar('X');
      putchar('|');
    }
    putchar('\n');
  }
  for (int j=0;j<max*2 + 1;j++)
    putchar('-');
  putchar('\n');
  return;
}

void print_board_solution(int board[], int max){//prints a solution from the board
  for (int i=0;i<max-1;i++)
    printf("(%d,%d)",i,board[i]);
  printf("(%d,%d)\n",max-1,board[max-1]);
}


int check_board(int board[], int row){//checks board for conflicting queens
  for (int i=0;i<=row;i++)
    if (board[i] != -1)
      for (int k=0;k<=row;k++)
	//check for queens on vertical, horizontal and diagonals
	if (((board[k] == board[i]) || 
	     (board[k] == board[i] + (i - k)) ||
	     (board[k] == board[i] - (i - k))) 
	    && (k != i) &&(board[k]!=-1)) {
	  return 0;
	}
  return 1;
}


void solve(int board[], int current_row, int max){//Hail jesus of recursive faith, segfaults I don't want to throw
  //if the current row,
  //is to the max, yo
  if (current_row == max)
    //flip that sh*t and return it
    return;
  
  //Loop i through positions on the board,
  //Drop the for loop - the magic is in motion
  //Better be efficient like Ron Jeremy in Love Potion
  for (int i=0;i<max;i++){

    //Move the queen, like a pound of green
    board[current_row]=i;
    
    //Check the board, check that sh*t
    if (check_board(board,current_row)==1) 
      //pass that stack, solve the other bit
      solve(board,current_row+1,max);   

    //Smooth sailing like yours truly MC
    //The board be solved or all be hell
    //If I got to the end, it's time to tell
    if (current_row==max -1 ) 
      //Spark that sh*t, shout out to queens
      //if the haze is dutch and the board is clean
      if (check_board(board,max-1)==1 && board[max-1]!=-1){
	//send the solution, 
	//ain't no confusion
	print_board_solution(board,max);   
      }
  }

  //check yourself,
  //before you wreck yourself

  //Biggie tells it better, peace to his urn

  if (current_row==max -1 )
    board[current_row]=-1;
  return;  	

  //Spread the word it takes time to learn

  //Playin with C, you playin' with fire
  //It don't give a f*ck about what you desire
  //C shoots like my 9 in the jacket
  //Pull the trigger, my ni**** 
  //But don't you smack it
}

