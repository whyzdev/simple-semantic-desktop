#include <iostream>
#include <fstream>
#include <ostream>
#include <string>
#include <vector>
#include "BigInteger.h"


using namespace std;

BigInteger::BigInteger( int value/*=0*/){  
  /*
    BigInteger(int): constructor to construct BigInteger from int
  */
  if (value == 0)
    digits.insert(digits.begin(),value);

  while (value > 0){
    digits.insert(digits.begin(),value % 10);
    value /=10;
  }  
}

void BigInteger::print(ostream & out  ) const{
  /*
    print(): prints the BigInteger
  */
  unsigned long i=0;

  for(i=0;i<digits.size();i++)
    out << digits[i];
  
  out << '\n';  
}

BigInteger::BigInteger( string value ){
  /*
    BigInteger(string): constructor to construct a BigInteger from a string
  */
  unsigned long i;
  for(i=0;i<value.size();i++)
    digits.push_back(value.at(i)-'0');  
}

bool BigInteger::isZero() const {
  /*
    isZero(): method to check if the BigInteger is Zero
  */
  unsigned long i;
  for(i=0;i<digits.size();i++)
    if (digits.at(i) != 0)
      return false;
  return true;
}

ostream& operator<<( ostream & out,const  BigInteger & value){
  /*
    overloaded operator <<, for printing BigInteger to a output stream
   */
  value.print(out);
  return out;
  
}


istream& operator>>( istream& in, BigInteger & value) {
  /*
    overloaded operator >>, for getting a BigInteger from an input stream
   */

  int c;
  value.digits.clear();
  while (!in.eof()){
    if ((c = in.get()) <= '9' && c >= '0')
      value.digits.push_back(c-'0');
    else 
      break;
  }
  return in;
}

int compareTo(const BigInteger & lhs, const BigInteger & rhs) {
  /*
    compareTo() : compares two BigInteger's for the various overloaded comparison operators
   */
    
    int i=0,j=0;
    while(i < lhs.digits.size() && lhs.digits.at(i) == 0 )
      i++;
    while(j < rhs.digits.size() && rhs.digits.at(j) == 0 )
      j++;
    
    while(j < rhs.digits.size() && i < lhs.digits.size() && lhs.digits.at(i) == rhs.digits.at(j)){
      i++;j++;}
    
    int a=0,b=0;

    if (j < rhs.digits.size())
      a=rhs.digits[j];
    else 
      a=0;

    if (i < lhs.digits.size())
      b=lhs.digits[i];
    else 
      b=0;

    return b-a;

  
}


/* Various overloaded comparison operators*/

bool operator< ( const BigInteger & lhs, const BigInteger & rhs ){
  return compareTo(lhs,rhs) < 0;

}
bool operator<=( const BigInteger & lhs, const BigInteger & rhs ){
  return compareTo(lhs,rhs) <= 0;
}

bool operator> ( const BigInteger & lhs, const BigInteger & rhs ){
  return compareTo(lhs,rhs) > 0;
}
bool operator>=( const BigInteger & lhs, const BigInteger & rhs ){
  return compareTo(lhs,rhs) >= 0;
}
bool operator==( const BigInteger & lhs, const BigInteger & rhs ){
  return compareTo(lhs,rhs) == 0;
}
bool operator!=( const BigInteger & lhs, const BigInteger & rhs ){
  return compareTo(lhs,rhs) != 0;
}





const BigInteger &  BigInteger::operator+=( const BigInteger & rhs ){
  /*
    overloaded operator += for adding a BigInteger to this BigInteger
   */  
  BigInteger temp;
  temp=rhs + (*this);
  temp.strip_leading_zeroes();
  digits.clear();
  for(unsigned long i=0;i<temp.digits.size();i++)
    digits.push_back(temp.digits.at(i));    
}

BigInteger operator+( const BigInteger & lhs, const BigInteger & rhs ){
  /*
    overloaded operator + for adding two BigIntegers 
   */  
    
  int i_lhs=lhs.digits.size()-1, i_rhs=rhs.digits.size()-1;
  int carry=0;
  BigInteger temp;

  temp.digits.clear();

  while(i_lhs >= 0 && i_rhs >= 0 ){
    int n = lhs.digits[i_lhs] + rhs.digits[i_rhs] + carry;
    carry = n / 10;    
    n=n%10;
    temp.digits.insert(temp.digits.begin(),n);
    i_lhs--;
    i_rhs--;
  }
  
  while(i_lhs >=0){
    int n = lhs.digits[i_lhs] + carry;
    carry = n / 10;    
    n=n%10;
    temp.digits.insert(temp.digits.begin(),n);
    i_lhs--;
  }

  while(i_rhs >=0){
    int n = rhs.digits[i_rhs] + carry;
    carry = n / 10;    
    n=n%10;
    temp.digits.insert(temp.digits.begin(),n);
    i_rhs--;
  }

    
  temp.strip_leading_zeroes();
  return temp;
}

const BigInteger & BigInteger::operator=( const BigInteger & rhs ){  digits.clear();
  /*
    overloaded operator = for copying a BigInteger's value to this BigInteger 
   */  
    
  unsigned long i=0;
  while( i< rhs.digits.size())
    digits.push_back(rhs.digits[i++]);
}


BigInteger operator*( const BigInteger & lhs, const BigInteger & rhs ){ //( for groups )
  /*
    overloaded operator * for multiplying two BigIntegers 
   */  
    
  BigInteger result(0);
  //cerr << "result:" << result << endl;
  int i=lhs.digits.size()-1;
  while (i >=0){
    //cerr << "rhs:" << rhs << endl;
    //cerr << "digit:" << lhs.digits.at(i) << endl;
    BigInteger t(0);
    t=rhs*(lhs.digits.at(i));
    //cerr << "t=rhs*digit:" << t << endl;
    t.multiplyBy10((unsigned long)lhs.digits.size()-1-i);
    //cerr << "t.multiplyBy10("<< lhs.digits.size()-1-i << "):" << t << endl;
    //t.print(//cerr);
    result +=t;
    //cerr << "result:" << result << endl;
    i--;
  }
  result.strip_leading_zeroes();
  return result;
}



void BigInteger::multiplyBy10(unsigned long times){
  /*
    multiplyBy10(): multiplies this BigInteger with 10 as many times as specified
   */  
  
  for(unsigned long i=0;i<times;i++)
    digits.push_back(0);
  
}



BigInteger operator*( const BigInteger & lhs, const unsigned long rhs ){
  /*
    overloaded operator * : multiplies a BigInteger with an unsigned long
   */  
    
  int i_lhs=lhs.digits.size()-1;
  unsigned long carry=0;
  BigInteger temp;

  temp.digits.clear();

  while(i_lhs >= 0){
    unsigned long n = ((unsigned long)lhs.digits[i_lhs]) * rhs + carry;
    carry = n / 10;    
    n=n%10;
    temp.digits.insert(temp.digits.begin(),n);
    i_lhs--;
  }

  
  while (carry > 0){
    int n=carry%10;
    temp.digits.insert(temp.digits.begin(),n);
    carry = carry / 10;
  }
  
  temp.strip_leading_zeroes();
  return temp;  

}


const BigInteger & BigInteger::decrement(){
  /*
    decrement(): subtracts 1 from this BigInteger
   */  
  unsigned long currentdigit=digits.size()-1;

  digits[currentdigit]--;


  while(digits[currentdigit]==-1 && currentdigit >=0){
    digits[currentdigit]+=10;
    currentdigit--;
    digits[currentdigit]--;
  }
    strip_leading_zeroes();

}



const BigInteger::BigInteger & BigInteger::operator*=( const unsigned long rhs ) {
  /*
    overloaded operator *= : multiplies the number on RHS to this BigInteger
   */
  
  BigInteger temp;
  temp=(*this)*rhs;

  digits.clear();
  for(unsigned long i=0;i<temp.digits.size();i++)
    digits.push_back(temp.digits.at(i));    
    
}

const BigInteger::BigInteger & BigInteger::operator*=( const BigInteger & rhs ) { //(for groups)
  /*
    overloaded operator *= : multiplies the BigInteger on RHS to this BigInteger
   */
  
  BigInteger temp;
  temp=(*this)*rhs;

  digits.clear();
  for(unsigned long i=0;i<temp.digits.size();i++)
    digits.push_back(temp.digits.at(i));    

  
}
 

void BigInteger::strip_leading_zeroes(){
  /*
    strip_leading_zeroes() : removes any excess zeroes from the most significant bits of BigInteger
   */
  int i=0;
  while(i < digits.size() && digits.at(i) == 0 )
    i++;

  digits.erase(digits.begin(),digits.begin()+i);    

}
