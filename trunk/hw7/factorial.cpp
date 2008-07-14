#include <iostream>
#include <fstream>
#include <ostream>
#include <string>
#include <vector>
#include "BigInteger.h"
BigInteger  factorial(BigInteger & n);
int main(){
  BigInteger n,f;
  cin >> n;
  f=factorial(n);
  cout << f;  
}

BigInteger  factorial(BigInteger  & n){
  /*
    factorial():Calculates factorial by tail recursion
  */

  BigInteger one(1);
  BigInteger f(1);
  while(n >= one){
    f *=n;
    n.decrement();
  }
  return f;
}


