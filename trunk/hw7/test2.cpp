#include <iostream>
#include <fstream>
#include <ostream>
#include <string>
#include <vector>
#include "BigInteger.h"

int main(){
  BigInteger a(511);
  BigInteger b("30");
  BigInteger c(700);
  BigInteger d("511");
  unsigned long l=9000000;
  cout << "------------------------------" << endl;
  cout << "INITIAL VALUES" << endl;
  cout << "------------------------------" << endl;

  cout <<"a:" << a << endl;
  cout <<"b:" << b << endl;
  cout <<"c:" << c << endl;
  cout <<"d:" << d << endl;
  cout <<"l (unsigned long):" << l << endl;


  cout << "------------------------------" << endl;
  cout << "Comparison" << endl;
  cout << "------------------------------" << endl;
  cout <<"a>b:" << (a>b) << endl;
  cout <<"b<c:" << (b<c) << endl;
  cout <<"c<a:" << (c<a) << endl;
  cout <<"c<=a:" << (c<=a) << endl;
  //  cout <<"b<=b:" << (b<=b) << endl;
  cout <<"a>=b:" << (a>=b) << endl;
  cout <<"d==a:" << (d==a) << endl;


  cout << "------------------------------" << endl;
  cout << "Plus Equals/Star Equals" << endl;
  cout << "------------------------------" << endl;
  c+=b;
  cout <<"after c+=b:" << c << endl;
  b+=a;
  cout <<"after b+=a:" << b << endl;
  c*=b;
  cout <<"after c*=b:" << c << endl;


  cout << "------------------------------" << endl;
  cout << "Helper Functions" << endl;
  cout << "------------------------------" << endl;
  a.multiplyBy10(4);
  cout <<"after a.multiplyBy10(4)" << a << endl;
  a.decrement();
  cout <<"after a.decrement():" << a << endl;
  b.decrement();
  cout <<"after b.decrement():" << b << endl;

  /*BigInteger one(1);
  
  while(b >= one){
    b.decrement();
    b.print(cout);
    }*/
  
}
