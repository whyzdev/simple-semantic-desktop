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
  unsigned long l=9000000;
  cout << "------------------------------" << endl;
  cout << "INITIAL VALUES" << endl;
  cout << "------------------------------" << endl;

  cout <<"a:" << a << endl;
  cout <<"b:" << b << endl;
  cout <<"c:" << c << endl;
  cout <<"l (unsigned long):" << l << endl;

  cout << "------------------------------" << endl;
  cout << "Multiplication" << endl;
  cout << "------------------------------" << endl;
  cout <<"a*b:" << (a*b) << endl;
  cout <<"b*c:" << (b*c) << endl;
  cout <<"c*a:" << (c*a) << endl;
  cout <<"b*l:" << (c*a) << endl;

  cout << "------------------------------" << endl;
  cout << "Addition" << endl;
  cout << "------------------------------" << endl;
  cout <<"a+b:" << (a*b) << endl;
  cout <<"b+a:" << (b*c) << endl;
  cout <<"c+a*b:" << (c*a) << endl;
  cout <<"a*c+l:" << (c*a) << endl;


  
}
