int ffunc(int a, int* b, float c, float* d, void* e){
  return 1;
}

int gfunc(int a, int* b, float c, float* d){
  return 1;
}

int hfunc(int a, int* b, float c){
  return 1;
}


int ifunc(int a, int* b){
  return 1;
}


int* intstarfunc(int a){
  return &a;
}

float* floatstarfunc(float a){
  return &a;
}

void* voidstarfunc(void* a){
  return a;
}




int main(void){
  int a;
  int* b;
  void* v;
  int c;
  int d;
  int e;
  

  int h;
  int i;
  int j;

  float f;
  float z;
  float x;
  float y;
  float* g;

  int zzz[20];
  a=a/c*d/e*h/i/j;
  f=z*x**&y/f*f;

  a=*(b+a);
  
  a=zzz[23];
  a=*(zzz+23);
  a=!a;
  if (!(a+b))
    a=a;
  a=ffunc(a,b,f,g,v);
  a=gfunc(a,b,f,g);
  a=hfunc(a,b,f);
  a=ifunc(a,b);
  b=intstarfunc(a);
  g=floatstarfunc(f);
  v=voidstarfunc(v);

  return 1;
  /*  g=g/g/g/g/g;*/
  /*









  a=a+c;
  f=f+f;
  b=b+a;
  b=a+b;

  g=g+a;
  g=a+g;


  a=a-c;
  f=f-f;
  b=b-a;


  g=g-a;

  a=g-g;
  a=b-b;*/
}

