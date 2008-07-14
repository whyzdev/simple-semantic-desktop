int f(int b, int c){
  *(b+c);
}

int g(int b, int c){
  b=c;
}
int main(void){
  int a[20];
  int j;
  a[j]=f(a[1],a[2]);
  return a[1];
}


int main2(void){
  int a[20];
  int j;
  return 1.2;
  a[j]=f(a[1],a[2]);
}
