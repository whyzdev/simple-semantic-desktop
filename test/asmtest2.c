int numbernumber;
int foofoo;
int barbar;
int* pointerthing;
int samplearray[20];

void function1(int a, int b ){
  int a1;
  int b2;
  a=a1;
  return;
}

void function2(int a, int b ){
  int a1;
  int b2;
  a=a1;
  return;
}

void function3(int a, int b,int c, int d,int e, int f,int g, int h ){
  int a1;
  int b2;
  a1=a;
  a1=b;
  a1=c;
  a1=d;
  a1=e;
  a1=f;
  a1=g;
  a1=h;

  return;
}

void function4(int a, int b ){
  int a1;
  int b2;
  function2(a1,b2);
  function1(a1,b2);
  a=a1;
  return;
}

int main(void){
  /*int a;
  function1(1,2);
  a=12;
  a=123;
  function2(1,2);
  a=123;
  a=123;
  function3(1,2,3,4,5,6,7,8);
  for (;;){
      a=123;
  }
  a=123;
  function4(1,2);
  a=123;
  function1(1,2);*/
  foofoo=1;
  barbar=2;
  numbernumber=1;
  pointerthing=&foofoo;
  samplearray[2]=1;
  return 1;
}

