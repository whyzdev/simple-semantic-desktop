#ifndef BIGINTEGER
#define BIGINTEGER

#include <iostream>
#include <fstream>
#include <ostream>
#include <string>
#include <vector>

using namespace std;



class BigInteger{

  friend istream & operator>>( istream & in, BigInteger & valxe);
  friend ostream & operator<<( ostream & out, const BigInteger & value);
  friend int compareTo (const BigInteger & lhs, const BigInteger & rhs) ;

  friend BigInteger operator+( const BigInteger & lhs, const BigInteger & rhs );
  friend BigInteger operator*( const BigInteger & lhs, const BigInteger & rhs ); //( for groups )
  friend BigInteger operator*( const BigInteger & lhs, const unsigned long rhs );


  
 public:
  BigInteger( int value = 0 );
  void print( ostream & out = cout ) const;
  BigInteger( string value );
  bool isZero( ) const;
  const BigInteger & operator+=( const BigInteger & rhs );
  const BigInteger & operator=( const BigInteger & rhs );
  const BigInteger & decrement();
  void multiplyBy10(unsigned long times);
  const BigInteger & operator*=( const unsigned long rhs );    
  const BigInteger & operator*=( const BigInteger & rhs ); //(for groups) 
  void strip_leading_zeroes();
  
 private:
  vector<int> digits;
    

};

int compareTo(const BigInteger & lhs, const BigInteger & rhs);
istream & operator>>( istream & in, BigInteger & value);
ostream & operator<<( ostream & out, const BigInteger & value);

BigInteger operator+( const BigInteger & lhs, const BigInteger & rhs );
BigInteger operator*( const BigInteger & lhs, const BigInteger & rhs ); //( for groups )
BigInteger operator*( const BigInteger & lhs, const unsigned long rhs );




bool operator< ( const BigInteger & lhs, const BigInteger & rhs );
bool operator<=( const BigInteger & lhs, const BigInteger & rhs );
bool operator> ( const BigInteger & lhs, const BigInteger & rhs );
bool operator>=( const BigInteger & lhs, const BigInteger & rhs );
bool operator==( const BigInteger & lhs, const BigInteger & rhs );
bool operator!=( const BigInteger & lhs, const BigInteger & rhs );



#endif
