#include <stdio.h>
#include <string.h> 
#include <math.h>

#include "util.h"
#include "hw03.h"
#include "game.h"



chomper* chompers[3];
lobber* lobbers[3],*dennis,*boomer,*zoomer;
decal* dec;
char board[(int)(PLATEAU_WIDTH/METERS_PER_SQUARE)][(int)(PLATEAU_HEIGHT/METERS_PER_SQUARE)];
int  b_width,b_height;
int turn_count,n_lobbers_destroyed,n_chompers_destroyed;
char inputline[255];

int main(){
  //Init
  initialize();

  //Print Status
  printf("Welcome to Ice Plateau C\n----------------------\
--\nAll bearings are calculated based on the top\nof the screen b\
eing 0, and to the right being\n90 degrees.\n\
The bottom left corner of the map is the origin\n\
with the y-axis being vertical.\n");
  turn_count=1;
  int menu_exit=FALSE;
  while(!menu_exit){    
    //loop through game turns
    printf("\nTurn %d begins!\n",turn_count);
    menu_exit=menu();
    if (!menu_exit){
      int i=0;
      for(i=0;i<3;i++){
	default_move_chomper(chompers[i]);
	default_move_lobber(lobbers[i]);
      }
      update_coords();
    }      
    turn_count++;    
    if (n_lobbers_destroyed == 3){
      printf("All Lobbers destroyed, you lose.");
      menu_exit=TRUE;
    }
    if (n_chompers_destroyed == 3){
      printf("All Chompers destroyed, you win.");
      menu_exit=TRUE;
    }
    

  }
  print_endgame();
  return 0;
}

int menu(){
  int main_input,menu_turn_ends=0,l;

  while(!menu_turn_ends){
    printf("Main Menu: (1) Shoot, (2) Move, (3) Change Bearing, (4) Status, (5) Exit\n");
    getline(inputline,255);

    main_input=myatoi(inputline);


    switch(main_input){
    case 1:
      if ((l=lobber_menu())!=4)
	menu_turn_ends=shoot_menu(lobbers[l-1]);
      else
	menu_turn_ends=FALSE;
      break;
    case 2:
      if ((l=lobber_menu())!=4)
	menu_turn_ends=move_lobber_menu(lobbers[l-1]);
      else
	menu_turn_ends=FALSE;
      break;
    case 3:
      if ((l=lobber_menu())!=4)
	changebearing_menu(lobbers[l-1]);      
      menu_turn_ends=FALSE;
      break;
    case 4:
      print_status();
      menu_turn_ends=FALSE;
      break;
    case 5:
      return TRUE;
    }
  }
  return FALSE;
}


int lobber_menu(){
  int lobber_input;
  while (1){
    printf("Choose: ");
    int i;
    for (i=0;i<3;i++){
      if (lobbers[i]->alive)
	printf("(%d) %s, ",(i+1),lobbers[i]->fullname);
      else
	printf("(%d) %s, ",(i+1),"XXXXXXXX");
    }
    printf("(4) Cancel\n");
    getline(inputline,255);
    lobber_input=myatoi(inputline);
    if (!(lobber_input >=1 && lobber_input <= 4))
      print_int_error(1,4);
    else 
      return lobber_input;
  }
}


int shoot_menu(lobber *l){
  float bearing, angle, velocity;
  if (l->alive){
    printf("%s lobs at max velocity of %.2f m/s with a blast of %.2f meters\n",l->fullname,l->max_shot,l->blast);
    bearing=radians(ask_float("Bearing",0,360));
    angle=ask_float("Angle of launch",0,180);
    velocity=ask_float("Weapon velocity",0,l->max_shot);
    shoot(l,bearing,angle,velocity);
    return TRUE;  
  }
  else {
    printf("Invalid shot: %s is Destroyed.",l->fullname);
    return FALSE;
  }
}

int move_lobber_menu(lobber *l){
  move_lobber(l);
  return TRUE;
}


int changebearing_menu(lobber *l){

  char buf[70];
  sprintf(buf,"Current bearing %.2f, new bearing",degrees(l->bearing));
  l->bearing=radians(ask_float(buf,0,360));    
  return TRUE;    
}


void shoot(lobber *l,double bearing, double angle,double velocity){
  double shotdistance;
  shotdistance=fabs(velocity * velocity * sin(2 * bearing) / GRAVITY);

  
  dec->x = l->x + shotdistance * sin(bearing);
  dec->y = l->y + shotdistance * cos(bearing);
  dec->alive=TRUE;


  printf("%s shot %.2f meters hitting (%.2f,%.2f)\n", l->fullname, shotdistance, dec->x,dec->y);
  if (! (dec->x < PLATEAU_WIDTH && dec->y < PLATEAU_HEIGHT && 
	 dec->x >= 0 && dec->y > 0)){
    printf("Which is past the edge of the plateau.\n");
    dec->alive=FALSE;
  }

  int i;
  for (i=0;i<3;i++){
    if (chompers[i]->alive)
      if (getdistance(chompers[i]->x,chompers[i]->y,dec->x,dec->y) < l->blast){
	chompers[i]->alive=FALSE;
	printf("%s destroyed!\n",chompers[i]->fullname);
	  n_chompers_destroyed++;
      }      

    if (lobbers[i]->alive)
      if (getdistance(lobbers[i]->x,lobbers[i]->y,dec->x,dec->y) < l->blast){
	lobbers[i]->alive=FALSE;
	printf("%s destroyed!\n",lobbers[i]->fullname);
	  n_lobbers_destroyed++;
      }      
  }


  return;
}

float ask_float(char float_name[], double lower_limit, double upper_limit){
  double f ;
  while(1)
    {
      printf("%s? >",float_name);
      getline(inputline,255);
      f = myatof(inputline);
      if (f > upper_limit || f < lower_limit)
	print_float_error(lower_limit,upper_limit);
      else
	return f;
    }
}

void print_int_error(int a,int b){
  printf("Bad input. Please enter a number between %d and %d.",a,b);
}


void print_float_error(double a,double b){
  printf("Bad input. Please enter a number between %.2f and %.2f.",a,b);
}


void initialize(){
  n_lobbers_destroyed=0;
  n_chompers_destroyed=0;
  
  b_width=PLATEAU_WIDTH/METERS_PER_SQUARE;
  b_height=PLATEAU_HEIGHT/METERS_PER_SQUARE;
  static chomper a,b,c;
  chompers[0]=&a;
  chompers[1]=&b;
  chompers[2]=&c;

  static lobber d,e,f;
  lobbers[0]=boomer=&d;
  lobbers[1]=zoomer=&e;
  lobbers[2]=dennis=&f;
    

  static decal g;
  dec = &g;

  dec->x=dec->y=-1;
  dec->alive=FALSE;
  dec->display_char='*';


  (*boomer).move_speed=BOOMER_MOVE_SPEED;
  (*boomer).max_shot=BOOMER_MAX_SHOT;
  (*boomer).blast=BOOMER_BLAST;
  (*boomer).bearing=radians(BOOMER_BEARING);
  (*boomer).x=BOOMER_X;
  (*boomer).y=BOOMER_Y;
  (*boomer).alive=BOOMER_ALIVE;
  (*boomer).display_char='B';
  strcpy(boomer->fullname,"Boomer");

  (*zoomer).move_speed=ZOOMER_MOVE_SPEED;
  (*zoomer).max_shot=ZOOMER_MAX_SHOT;
  (*zoomer).blast=ZOOMER_BLAST;
  (*zoomer).bearing=radians(ZOOMER_BEARING);
  (*zoomer).x=ZOOMER_X;
  (*zoomer).y=ZOOMER_Y;
  (*zoomer).alive=ZOOMER_ALIVE;
  (*zoomer).display_char='Z';
  strcpy(zoomer->fullname,"Zoomer");

  
  
  (*dennis).move_speed=DENNIS_MOVE_SPEED;
  (*dennis).max_shot=DENNIS_MAX_SHOT;
  (*dennis).blast=DENNIS_BLAST;
  (*dennis).bearing=radians(DENNIS_BEARING);
  (*dennis).x=DENNIS_X;
  (*dennis).y=DENNIS_Y;
  (*dennis).alive=DENNIS_ALIVE;
  (*dennis).display_char='D';
  strcpy(dennis->fullname,"Dennis");
    
  
  (*chompers[0]).move_speed=CHOMPER_MOVE_SPEED;
  (*chompers[0]).x=CHOMPER1_X;
  (*chompers[0]).y=CHOMPER1_Y;
  (*chompers[0]).alive=CHOMPER1_ALIVE;
  (*chompers[0]).bearing=radians(CHOMPER1_BEARING);
  (*chompers[0]).display_char='1';
  strcpy(chompers[0]->fullname,"Chomper 1");
  autotarget_chomper(chompers[0]);


  (*chompers[1]).move_speed=CHOMPER_MOVE_SPEED;
  (*chompers[1]).x=CHOMPER2_X;
  (*chompers[1]).y=CHOMPER2_Y;
  (*chompers[1]).alive=CHOMPER2_ALIVE;				 
  (*chompers[1]).bearing=radians(CHOMPER2_BEARING);
  (*chompers[1]).display_char='2';
  strcpy(chompers[1]->fullname,"Chomper 2");
  autotarget_chomper(chompers[1]);

  (*chompers[2]).move_speed=CHOMPER_MOVE_SPEED;
  (*chompers[2]).x=CHOMPER3_X;
  (*chompers[2]).y=CHOMPER3_Y;
  (*chompers[2]).alive=CHOMPER3_ALIVE;
  (*chompers[2]).bearing=radians(CHOMPER3_BEARING);
  (*chompers[2]).display_char='3';
  strcpy(chompers[2]->fullname,"Chomper 3");
  autotarget_chomper(chompers[2]);

  update_coords();
  
  
}


void update_coords(){
  int i=0;
  for( i=0;i<3;i++){
    
    chompers[i]->actualx=(int)(chompers[i]->x/METERS_PER_SQUARE);
    chompers[i]->actualy=(int)(chompers[i]->y/METERS_PER_SQUARE);

    if (! ((chompers[i]->actualx < b_width) &&
	   (chompers[i]->actualy < b_height) ))
      chompers[i]->alive=FALSE;


    lobbers[i]->actualx=(int)(lobbers[i]->x/METERS_PER_SQUARE);
    lobbers[i]->actualy=(int)(lobbers[i]->y/METERS_PER_SQUARE);

    if (! ((lobbers[i]->actualx < b_width) &&
	   (lobbers[i]->actualy < b_height) ))
      lobbers[i]->alive=FALSE;
  }

  dec->actualx=dec->x/METERS_PER_SQUARE;
  dec->actualy=dec->y/METERS_PER_SQUARE;

  if (! (dec->actualx < b_width && dec->actualy < b_height && 
	 dec->actualx >= 0 && dec->actualy > 0)){
    dec->alive=FALSE;
  }

}


void default_move_chomper( chomper *c){
  autotarget_chomper(c);
  move_chomper(c);
  attack_chomper(c);
}

void attack_chomper(chomper *c){
  double distances[3];
  int i;

  for (i=0;i<3;i++){
    if (lobbers[i]->alive){
      distances[i]=getdistance(c->x,c->y,lobbers[i]->x,lobbers[i] ->y);
      if(distances[i] < c->move_speed){
	lobbers[i]->alive=FALSE;
	printf("%s just killed %s!",c->fullname,lobbers[i]->fullname);
	n_lobbers_destroyed++;
      }
    }  
  }
  

}

void autotarget_chomper(chomper *c){
  double distances[3],min;
  int i,chosen1=0;

  min=getdistance(c->x,c->y,lobbers[0]->x,lobbers[0]->y);

  for (i=0;i<3;i++){
    distances[i]=getdistance(c->x,c->y,lobbers[i]->x,lobbers[i]->y);
    if(distances[i] < min)
      chosen1=i;      
  }

  c->target=lobbers[chosen1];
  c->bearing = atan( (c->x - c->target->x) / (c->y - c->target->y) );
}

void move_chomper(chomper *c){
  c->x = c->x + c->move_speed * sin(c->bearing);
  c->y = c->y + c->move_speed * cos(c->bearing);      
}

void move_lobber(lobber *l){
  l->x = l->x + l->move_speed * sin(l->bearing);
  l->y = l->y + l->move_speed * cos(l->bearing);      
}


void default_move_lobber( lobber *l){
  move_lobber(l);
}





void print_board(){
  int i=0,j=0;

  const char *const fighter_color = "\033[0;47;35m";
  const char *const border_color = "\033[0;47;34m";
  const char *const grid_color = "\033[0;47;30m";
  const char *const decal_color = "\033[0;47;31m";



  const char *const normal = "\033[0m";  
  for( i=0;i<b_width;i++){
    for( j=0;j<b_height;j++){
      board[i][j]=' ';
    }
  }
  for ( i=0;i<3;i++){
    if (chompers[i]->alive)
      board[(int)(*chompers[i]).actualx][(int)(*chompers[i]).actualy]=(*chompers[i]).display_char;
    if (lobbers[i]->alive)
      board[(int)(*lobbers[i]).actualx][(int)(*lobbers[i]).actualy]=(*lobbers[i]).display_char;
  }

  if (dec->alive)
    board[dec->actualx][dec->actualy]=dec->display_char;
  
  printf("%s", border_color);
  putchar('+');
  for( i=0;i<b_width*2;i++)
    putchar('-');
  putchar('+');
  printf("%s", normal);
  putchar('\n');



  for( i=0;i<b_width;i++){
  printf("%s", border_color);
    putchar('|');
  printf("%s", grid_color);
    for( j=0;j<b_height;j++){
      putchar('.');
      if (board[i][j] != ' '){
	if (board[i][j] != '*')
	  printf("%s", fighter_color);
	else 
	  printf("%s", decal_color);
	putchar(board[i][j]);
	printf("%s", grid_color);
      }
      else
	putchar(board[i][j]);
    }
  printf("%s", border_color);
    putchar('|');
  printf("%s", normal);
    putchar('\n');
  }

  printf("%s", border_color);
  putchar('+');
  for( i=0;i<b_width*2;i++)
    putchar('-');
  putchar('+');
  printf("%s", normal);
  putchar('\n');

}

void print_endgame(){
  int i;
  printf("Lobbers destroyed:");
  for (i=0;i<3;i++)
    if (!lobbers[i]->alive){
      printf("%s%s",lobbers[i]->fullname,(i<2) ? ", ": "");
    }

  printf("\nChompers destroyed:");
  for (i=0;i<3;i++)
    if (!chompers[i]->alive){
      printf("%s%s",chompers[i]->fullname,(i<2) ? ", ": "");
    }

  printf("\nGoodbye!\n");

}
  
void print_summary(){

  int i;
  chomper *c;
  lobber *l;


  for (i=0;i<3;i++){
    l=lobbers[i];
    printf("%9s: Position: (%.2f, %.2f)  Bearing: %3.2f  Speed %1.2f\n",
	   l->fullname,l->x,l->y,degrees(l->bearing),l->move_speed);
    printf("           Status: %s\n",(l->alive)?"Alive" : "Destroyed");
  }


  for (i=0;i<3;i++){
    c=chompers[i];
    printf("%9s: Position: (%.2f, %.2f)  Bearing: %3.2f  Speed %1.2f\n",
	   c->fullname,c->x,c->y,degrees(c->bearing),c->move_speed);
    printf("           Status: %s\n",(c->alive)?"Alive" : "Destroyed");
    printf("           Chasing: %s\n",c->target->fullname);
  }
 
}


void print_status(){
  print_summary();
  print_board();
  
}
