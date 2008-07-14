
int main(int c){
  int a;
  a=20;
  {
    int* a;    
    //a=30;
    printf("%d\n",a);
  }
  printf("%d\n",a);
}
