import java.util.*;
    /*
     *
     *
     Token.java - this file contains definitions for the
     Token abstract class, of which there are subclasses:
     -IntegerToken
     -FloatToken
     -OperatorToken
     -KeywordToken
     -IdentifierToken
     -ErrorToken
     -EOFToken
     *
     *
     */


public abstract class Token{
    /*Parent abstract Token class*/
 
    String lexeme;/*actual String scanned from the given text file*/
    String stringvalue;/* print this token as*/
    int symbolNumber;/*the number of the entry in the symbol table*/
    
    public String toString(){
	return stringvalue;
    }

}

class IntegerToken extends Token{
    int value;/*Integer value of the Integer token*/
    public IntegerToken(String lexeme, int symbolNumber){
	this.symbolNumber=symbolNumber;
	value = Integer.parseInt(lexeme,10); 
	stringvalue="num("+Integer.toString(value)  +")";
    }

}


class FloatToken extends Token{
    float value;/*FLoat value of the token*/
    public FloatToken(String lexeme, int symbolNumber){
	this.symbolNumber=symbolNumber;
	value = Float.parseFloat(lexeme); /*parse the float string to get a value*/ 
	stringvalue="real("+ lexeme  +")";
    }
    
}

class OperatorToken extends Token{

    public OperatorToken(String lexeme, int symbolNumber, String stringvalue){
	this.lexeme=lexeme;
	this.stringvalue=stringvalue;
	this.symbolNumber=symbolNumber;
    }

}

class KeywordToken extends Token{

    public KeywordToken(String lexeme, int symbolNumber){
	this.symbolNumber=symbolNumber;
	this.lexeme=lexeme;
	stringvalue=lexeme;
    }

}


class IdentifierToken extends Token{
    
    public IdentifierToken(String lexeme, int symbolNumber){
	this.symbolNumber=symbolNumber;
	stringvalue="id("+lexeme+")";    
    }
}

class CommentToken extends Token{
    public CommentToken(String lexeme){
	this.lexeme=lexeme;
	stringvalue="comment";
    }
    
}

class ErrorToken extends Token{
    public ErrorToken(String lexeme){
	stringvalue="err(" + lexeme + ")";
	symbolNumber = -1;/*negative symbol number for Errors*/
    }
}

class EOFToken extends Token{/*returns this token if the lexer successfully reaches the EOF*/
    public EOFToken(){
	stringvalue="\n";
	symbolNumber = -2;/*negative symbol number for EOF token*/
    }
    
}
