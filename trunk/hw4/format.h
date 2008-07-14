/* format.c */
int main(int argc, char *argv[]);
int myatoi(char s[]);
int format(int w, int rightalign, int justified, int skipmultiple);
void printLeft(char *sentence, int len);
void printRight(char *sentence, int len, int w);
void printJustified(char *sentence, int len, int w);
void showhelp(void);
