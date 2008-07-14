int a[20];

int main()
{
int k;
int b[20];

k=0;
a[0] = a[1] = 1;
while(k<20){
  b[19-k] = a[k] = a[k-1]+a[k-2];
  k=k+1;
  }
printi(b[0]);
}

