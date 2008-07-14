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
     -CommentToken
     *
     *
     */


public abstract class Token{
    /*Parent abstract Token class*/
    boolean diag1=false;
    String lexeme;/*actual String scanned from the given text file*/
    String stringvalue;/* print this token as*/
    int symbolNumber;/*the number of the entry in the symbol table*/
    int lineNumber; /* the line number of the token */

    public String toString(){
	return stringvalue;

    }

}

class IntegerToken extends Token{
    int value;/*Integer value of the Integer token*/
    public IntegerToken(String lexeme, int symbolNumber, int line){
	this.symbolNumber=symbolNumber;
	this.lineNumber=line;
	value = Integer.parseInt(lexeme,10); 
	stringvalue="num("+Integer.toString(value)  +")";
	if (diag1)
	    System.out.println(stringvalue);
    }

    public int getValue(){
	return this.value;
    }
}


class FloatToken extends Token{
    float value;/*FLoat value of the token*/
    public FloatToken(String lexeme, int symbolNumber, int line){
	this.symbolNumber=symbolNumber;
	this.lineNumber=line;
	value = Float.parseFloat(lexeme); /*parse the float string to get a value*/ 
	stringvalue="real("+ lexeme  +")";
	if (diag1)
	    System.out.println(stringvalue);
    }
    
    public float getValue(){
	return this.value;
    }
}

class OperatorToken extends Token{

    public OperatorToken(String lexeme, int symbolNumber, String stringvalue, int line){
	this.lexeme=lexeme;
	this.stringvalue=stringvalue;
	this.symbolNumber=symbolNumber;
	this.lineNumber=line;
	if (diag1)
	    System.out.println(stringvalue);
    }

}

class KeywordToken extends Token{

    public KeywordToken(String lexeme, int symbolNumber, int line){
	this.symbolNumber=symbolNumber;
	this.lexeme=lexeme;
	this.lineNumber=line;
	stringvalue=lexeme;
	if (diag1)
	    System.out.println(stringvalue);
    }

}


class IdentifierToken extends Token{
    
    public IdentifierToken(String lexeme, int symbolNumber, int line){
	this.symbolNumber=symbolNumber;
	this.lineNumber=line;
	stringvalue="id("+lexeme+")";    	
	if (diag1)
	    System.out.println(stringvalue);
    }
}

class CommentToken extends Token{
    public CommentToken(String lexeme, int line){
	this.lexeme=lexeme;
	this.lineNumber=line;
	stringvalue="comment";
	if (diag1)
	    System.out.println(stringvalue);
    }
    
}

class ErrorToken extends Token{
    public ErrorToken(String lexeme, int line){
	this.lineNumber=line;
	stringvalue="Invalid syntax: " + lexeme;
	symbolNumber = -1;/*negative symbol number for Errors*/
	if (diag1)
	    System.out.println(stringvalue);
    }
}

class EOFToken extends Token{/*returns this token if the lexer successfully reaches the EOF*/
    public EOFToken(){
	stringvalue="\n";
	symbolNumber = -2;/*negative symbol number for EOF token*/
	if (diag1)
	    System.out.println(stringvalue);
    }
    
}
