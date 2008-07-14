#ifndef GAME
#define GAME



  typedef struct {    
    double x;
    double y;
    char display_char;
    int alive;
    double bearing;    
    double move_speed;
    double max_shot;
    double blast;
    int actualx,actualy;
    char fullname[10];
  } lobber;


  typedef struct {
    double x;
    double y;
    int actualx,actualy;
    char display_char;
    int alive;
    double bearing;   
    double move_speed; 
    char fullname[10];  
    lobber * target;
  } chomper;


typedef struct{
  double x;
  double y;
  int actualx;
  int actualy;
  char display_char;
  int alive;
} decal;


int main(void);
int menu(void);
int lobber_menu(void);
int shoot_menu(lobber *l);
int move_lobber_menu(lobber *l);
int changebearing_menu(lobber *l);
void shoot(lobber *l, double bearing, double angle, double velocity);
float ask_float(char float_name[], double lower_limit, double upper_limit);
void print_int_error(int a, int b);
void print_float_error(double a, double b);
void initialize(void);
void update_coords(void);
void default_move_chomper(chomper *c);
void attack_chomper(chomper *c);
void autotarget_chomper(chomper *c);
void move_chomper(chomper *c);
void move_lobber(lobber *l);
void default_move_lobber(lobber *l);
void print_board(void);
void print_endgame(void);
void print_summary(void);
void print_status(void);

#endif
