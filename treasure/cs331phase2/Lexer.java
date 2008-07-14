import java.util.*;
import java.io.*;
/***
 *
 *
 Lexer.java -
 Lexer does the Lexical Analysis. 
 -lex() gets the next available token and puts it in lastTokenObject.
 -getNextToken() returns the lastTokenObject and runs lex().
 -peekNextToken() returns the lastTokenObject.
 -hasMoreTokens() tells whether there are more tokens to be returned.

 NOTE: (we're using '#' as a null character because we 
 couldn't find a proper null character in Java.)

 *
 *
 ***/
public class Lexer    
{
    boolean diag1=false;
    Hashtable hashtable;
    Hashtable operatorStringTable;
    int hashcount=0,opStTblCount=0;
    Token lastTokenObject;
    boolean moreTokens=true;//boolean used by hasMoreTokens()

    boolean eofReached=false;//End of File reached?
    boolean comment=false;//in a comment?
    int expLastTime=0; // gotta pick up the possible extra + or -
    
    BufferedReader reader;//the reader
    char c[]=new char[3];/* character buffer, c[0] is most used, c[1] a temp slot, 
			    c[2] only used for cases of possible signed exponents */
    String currentToken;//holds the current lexeme
    int readint=0;//characters are read into this first
    int linecount=1;
    ArrayList errorList;

    public Lexer(BufferedReader reader1)
    {
	hashtable = new Hashtable();
	operatorStringTable = new Hashtable();
	errorList = new ArrayList();
	reader=reader1;
	opStTblCount=8; //operatorStringTable count starts from 8, goes up to 35	
	
	operatorStringTable.put(new Integer(opStTblCount++),"leftp");//operator string table has strings for  
	operatorStringTable.put(new Integer(opStTblCount++),"rightp");// every operator
	operatorStringTable.put(new Integer(opStTblCount++),"leftsqb");
	operatorStringTable.put(new Integer(opStTblCount++),"rightsqb");
	operatorStringTable.put(new Integer(opStTblCount++),"colon");
	operatorStringTable.put(new Integer(opStTblCount++),"dot");
	operatorStringTable.put(new Integer(opStTblCount++),"asterisk"); /* It's not mult or 'value at' because * can do both*/
	operatorStringTable.put(new Integer(opStTblCount++),"addressof");
	operatorStringTable.put(new Integer(opStTblCount++),"mod");
	operatorStringTable.put(new Integer(opStTblCount++),"NOT");
	operatorStringTable.put(new Integer(opStTblCount++),"div");
	operatorStringTable.put(new Integer(opStTblCount++),"plus");
	operatorStringTable.put(new Integer(opStTblCount++),"minus");
	operatorStringTable.put(new Integer(opStTblCount++),"lessthan");
	operatorStringTable.put(new Integer(opStTblCount++),"greaterthan");
	operatorStringTable.put(new Integer(opStTblCount++),"less-or-eq");
	operatorStringTable.put(new Integer(opStTblCount++),"greater-or-eq");
	operatorStringTable.put(new Integer(opStTblCount++),"equals");
	operatorStringTable.put(new Integer(opStTblCount++),"noteq");
	operatorStringTable.put(new Integer(opStTblCount++),"pluseq");
	operatorStringTable.put(new Integer(opStTblCount++),"minuseq");
	operatorStringTable.put(new Integer(opStTblCount++),"AND");
	operatorStringTable.put(new Integer(opStTblCount++),"OR");
	operatorStringTable.put(new Integer(opStTblCount++),"leftc");
	operatorStringTable.put(new Integer(opStTblCount++),"rightc");
	operatorStringTable.put(new Integer(opStTblCount++),"comma");
	operatorStringTable.put(new Integer(opStTblCount++),"semic");
	operatorStringTable.put(new Integer(opStTblCount++),"assign");

	
	hashcount=0;//hashcount starts from 0, goes up to 36

	
	hashtable.put("int",new Integer(hashcount++)); // 0
	hashtable.put("float",new Integer(hashcount++)); // 1
	hashtable.put("void",new Integer(hashcount++)); // 2
	hashtable.put("if",new Integer(hashcount++)); // 3
	hashtable.put("else",new Integer(hashcount++)); // 4
	hashtable.put("while",new Integer(hashcount++)); // 5
	hashtable.put("for",new Integer(hashcount++)); // 6
	hashtable.put("return",new Integer(hashcount++)); // 7
	hashtable.put("(",new Integer(hashcount++)); // 8
	hashtable.put(")",new Integer(hashcount++)); // 9
	hashtable.put("[",new Integer(hashcount++)); // 10
	hashtable.put("]",new Integer(hashcount++)); // 11
	hashtable.put(":",new Integer(hashcount++)); // 12
	hashtable.put(".",new Integer(hashcount++)); // 13
	hashtable.put("*",new Integer(hashcount++)); // 14
	hashtable.put("&",new Integer(hashcount++)); // 15
	hashtable.put("%",new Integer(hashcount++)); // 16
	hashtable.put("!",new Integer(hashcount++)); // 17
	hashtable.put("/",new Integer(hashcount++)); // 18
	hashtable.put("+",new Integer(hashcount++)); // 19
	hashtable.put("-",new Integer(hashcount++)); // 20
	hashtable.put("<",new Integer(hashcount++)); // 21
	hashtable.put(">",new Integer(hashcount++)); // 22
	hashtable.put("<=",new Integer(hashcount++)); // 23
	hashtable.put(">=",new Integer(hashcount++)); // 24
	hashtable.put("==",new Integer(hashcount++)); // 25
	hashtable.put("!=",new Integer(hashcount++)); // 26
	hashtable.put("+=",new Integer(hashcount++)); // 27
	hashtable.put("-=",new Integer(hashcount++)); // 28
	hashtable.put("&&",new Integer(hashcount++)); // 29
	hashtable.put("||",new Integer(hashcount++)); // 30
	hashtable.put("{",new Integer(hashcount++)); // 31
	hashtable.put("}",new Integer(hashcount++)); // 32
	hashtable.put(",",new Integer(hashcount++)); // 33
	hashtable.put(";",new Integer(hashcount++)); // 34
	hashtable.put("=",new Integer(hashcount++)); // 35
	//hashcount is now 36

	c[0]='#';
	c[1]='#';
	c[2]='#';
    }

    
    private Token lex()
    {
	currentToken="";
	
	
	int state=0;
	int laststate=0;
	boolean tokenFinished = false;/*tokenFinished becomes true if end of file is reached or if no input is available*/
	Token newToken=new ErrorToken("no token", getLineCount());

	if (eofReached){ 
	    moreTokens=false;
	    return new EOFToken();
	}

	/*State numbers*/
	final int INITIALSTATE=0;
	final int NUMBERSTATE=1;
	final int ALPHASTATE=2;
	final int FINALSTATE=3;
	final int DOUBLESYMBOLSTATE=4;
	final int PIPESTATE=5;
	final int ERRORSTATE=7;
	final int COMMENTSTATE=8;
	final int REALNUMSTATE=9;
	final int HEXNUMSTATE=10;
	final int EXPONENTSTATE=11;

	while (!tokenFinished)
	    {
		if(expLastTime > 0)
		    {
			if(expLastTime > 1)
			    expLastTime--;
			else
			    {
				c[0]=c[2];
				c[2]='#';
				expLastTime=0;
			    }
		    }
	
		if(c[0] == '#')
		    try {
			readint=reader.read();
			c[0]=(char)readint;
			if (c[0]=='\n' )
			    linecount++;
		    }
		    catch(IOException e){
			tokenFinished=true;
			System.err.println("No Input");
		    }
		    		
		
		if(readint == -1) /* End Of File */
		    {	
			eofReached=true;
			state=FINALSTATE;
			//tokenFinished=true;
			//moreTokens=false;
			if (diag1)
			    System.err.println("I got a readint =-1");
		    }	
		

		switch (state) 
		    {
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case INITIALSTATE:
			if(isWhiteSpace(c[0]) )
			    {
				state=INITIALSTATE; /* whitespace. loop again.*/
				c[0]='#';
			    }
			else if ((c[0] >= '0') && (c[0] <='9'))
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=NUMBERSTATE; /* numbers */
			    }
			else if(((c[0] >= 'a') && (c[0] <='z')) || 
				((c[0] >= 'A') && (c[0] <='Z')))
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=ALPHASTATE; /* keyword or identifier */
			    }
			else if(c[0] == '(' || c[0] == ')' || 
				c[0] == '[' || c[0] == ']' || 
				c[0] == ':' || c[0] == '%' || 
				c[0] == '{' || c[0] == '}' || 
				c[0] == ',' || c[0] == ';' || 
				c[0] == '*')
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=FINALSTATE; /* single symbol. go to final state */

			    }
			else if(c[0] == '!' || c[0] == '+' ||
				c[0] == '-' || c[0] == '=' ||
				c[0] == '>' || c[0] == '<' ||
				c[0] == '&' || c[0] == '/' ||  c[0] == '.')
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=DOUBLESYMBOLSTATE; /* possible double symbol */
			    }
			else if(c[0] == '|' ) 
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=PIPESTATE; /* pipe */
			    }
			else if(readint == -1)
			    {
				eofReached=true;
				//state=FINALSTATE;
				tokenFinished=true;
				moreTokens=false;
				if (diag1)
				    System.err.println("I got a readint =-1");	
			    }
			else
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=ERRORSTATE; /* error token: unrecognized symbol */  
			    }
		    
			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case NUMBERSTATE: /* numbers */
			if(isWhiteSpace(c[0]) )
			    {
				c[0]='#';
				laststate=state;
				state=FINALSTATE;
			    }
			else if((c[0] >= '0') && (c[0] <='9'))
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
			    }
			else if( (c[0] == 'x' || c[0]=='X') && currentToken.charAt(0) == '0' && currentToken.length()==1)
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());				
				c[0]='#';
				state=HEXNUMSTATE; /* hex numbers */
			    }
			else if(c[0] == '.')
			    {
				currentToken = currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				state=REALNUMSTATE; /* real numbers */
			    }
			else if(c[0] == 'e' || c[0] == 'E')
			    {
				c[1]=c[0];
				c[0]='#';
				laststate = state;
				state=EXPONENTSTATE;
			    }
			else
			    {
				laststate=state;
				state=FINALSTATE;
			    }
			
			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case ALPHASTATE: /* keyword or identifier */
			if(isWhiteSpace(c[0]) )
			    {
				c[0]='#';
				laststate=state;
				state=FINALSTATE;
			    }
			else if(((c[0] >= 'a') && (c[0] <='z')) || 
				((c[0] >= 'A') && (c[0] <='Z')) ||
				(c[0] >= '0') && (c[0] <='9'))
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
			    }
			else
			    {
				laststate=state;
				state=FINALSTATE;
			    }

			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case FINALSTATE: /* final state. create token. return it.*/
			{
			    int symbolNumber;
			    
			    if(eofReached && (laststate == 0)){ // unclean way of returning an EOF Token if you've reached EOF and have a blank token
				
				return new EOFToken();
				
			    }
			    
			    switch (laststate){				
			    case INITIALSTATE: /* create single symbol */

				symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
				newToken = new OperatorToken(currentToken, symbolNumber, (String)operatorStringTable.get(new Integer(symbolNumber)), getLineCount());
				break;
			    case NUMBERSTATE: /* must be regular integer*/
				if(!hashtable.contains(currentToken)) /* new integer token */
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new IntegerToken(currentToken, hashcount, getLineCount());
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
					newToken = new IntegerToken(currentToken, symbolNumber, getLineCount());
				    }
				break;
			    case ALPHASTATE: /* keyword or identifier */
				if(!hashtable.containsKey(currentToken)) /* must be new identifier */
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new IdentifierToken(currentToken, hashcount, getLineCount());
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();

					if(symbolNumber < 8) /* must be keyword */
					    {
						newToken = new KeywordToken(currentToken, symbolNumber, getLineCount()); 
					    }
					else /* repeat identifier */ 
					    {
						newToken = new IdentifierToken(currentToken, symbolNumber, getLineCount());
					    }

				    }
				break;
			    case DOUBLESYMBOLSTATE: /* possible double symbols... could be single symbols */
			    	symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
				newToken = new OperatorToken(currentToken, symbolNumber, (String)operatorStringTable.get(new Integer(symbolNumber)), getLineCount());
				break;
			    case PIPESTATE: /* pipe. create an operator token */
				symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
				newToken = new OperatorToken(currentToken, symbolNumber, (String)operatorStringTable.get(new Integer(symbolNumber)), getLineCount());
				break;
			    case ERRORSTATE: /* error token */
				newToken = new ErrorToken(currentToken, getLineCount());
				break;
			    case REALNUMSTATE: /* real numbers */
				if(!hashtable.containsKey(currentToken))
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new FloatToken(currentToken, hashcount, getLineCount());
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
					newToken = new KeywordToken(currentToken, symbolNumber, getLineCount());
				    }
				break;
			    case EXPONENTSTATE: /* real numbers */
				if(!hashtable.containsKey(currentToken))
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new FloatToken(currentToken, hashcount, getLineCount());
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
					newToken = new KeywordToken(currentToken, symbolNumber, getLineCount());
				    }
				break;
			    case HEXNUMSTATE: /* hexidecimal numbers. already converted to equivalent to case 1 above */
				if(!hashtable.contains(currentToken)) /* new integer token */
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new IntegerToken(currentToken, hashcount, getLineCount());
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
					newToken = new IntegerToken(currentToken, symbolNumber, getLineCount());
				    }
				break;
			    case COMMENTSTATE:
				if(comment==true)
				    newToken = new ErrorToken("Unfinished comment at end of file", getLineCount());
				else
				    newToken=new CommentToken(currentToken, getLineCount());	
				   				
				break;
			    }

			    currentToken = "";
			    state=0;
			    
			    return newToken;

			}
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case DOUBLESYMBOLSTATE: /* possible double symbols */
			if(isWhiteSpace(c[0] ))
			    {
				c[0]='#';
				laststate=state;
				state=FINALSTATE;
			    }
			else
			    {
				switch (currentToken.charAt(0)){
				case '!':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '+':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '-':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '=':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{ 
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '>':				  
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{					 
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '<':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '&':
				    if(c[0] == '&')
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{		
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '/':
				    if(c[0] == '*')
					{
					    currentToken="";
					    c[0]='#';
					    laststate=state;
					    state=COMMENTSTATE; /* commenting */
					}
				    else
					{
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '.':
				    if ((c[0] >= '0') && (c[0] <='9'))
					{
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    laststate=state;
					    state=REALNUMSTATE;
					}
				    else
					{
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				default:
				    laststate=state;
				    state=FINALSTATE;
				}
			    }

			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case PIPESTATE: /* pipe | */
			if(c[0] == '|')
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=FINALSTATE;
			    }
			else
			    {
				state=ERRORSTATE; /* single '|' error */
			    }
			 		
			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case ERRORSTATE: /* error token */
			laststate=state;
			state=FINALSTATE;

			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case COMMENTSTATE: /* commenting */
			comment = true;
			laststate=state;
			if(c[0] == '*')
			    {
				c[1]=c[0];
				c[0]='#';
			    }
			
			else if((c[0] == '/') && (c[1] == '*'))
			    {
				c[0] = '#';
				c[1] = '#';
				comment=false;
				
				state=FINALSTATE;
			    }
			else
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[1] = '#';
				c[0]='#';
			    }
			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case REALNUMSTATE: /* a period is the input */
			if(isWhiteSpace(c[0]) )
			    {
				laststate=state;
				state=FINALSTATE;
			    }
			else if((c[0] >= '0') && (c[0] <='9'))
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
			    }
			else if(c[0] == 'e' || c[0] == 'E')
			    {
				c[1]=c[0];
				c[0]='#';
				laststate=state;
				state=EXPONENTSTATE;
			    }
			else
			    {
				laststate=state;
				state=FINALSTATE;
			    }
			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case EXPONENTSTATE: /* real numbers */
			if(isWhiteSpace(c[0]))
			    {
				if(c[1] != '#')
				    {
					char temp = c[0];
					c[0]=c[1];
				
					state=FINALSTATE;
					expLastTime=3;
				    }
				else
				    {
					laststate = state;
					state=FINALSTATE;
				    }
			    }
			else if((c[0] >= '0') && (c[0] <='9'))
			    {
				if( !(c[1] == 'e' || c[1] == 'E'))
				    currentToken=currentToken.concat((new Character(c[0])).toString());
				else
				    {
					currentToken=currentToken.concat((new Character(c[1])).toString());
					if(c[2] == '-' || c[2] == '+')
					    {
						currentToken=currentToken.concat((new Character(c[2])).toString());
						c[2]='#';
					    }
					currentToken=currentToken.concat((new Character(c[0])).toString());
					c[1]='#';
				    }

				c[0]='#';
			    }
			else if(c[0] == '-' || c[0] == '+')
			    {
				if( !(c[1] == 'e' || c[1] == 'E'))
				    state=FINALSTATE;
				else
				    {
					c[2] = c[0];
					c[0] = '#';
				    }
			    }
			else
			    {
				if(c[1] != '#')
				    {
					char temp = c[0];
					c[0]=c[1];
				
					c[1]=c[0];
					    
					state=FINALSTATE;
					expLastTime=3;
				    }
				else
				    {
					laststate = state;
					state=FINALSTATE;
				    }
			    }
			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case HEXNUMSTATE: /* hexidecimal integers */
			if ((currentToken.length()==2) && (isWhiteSpace(c[0]) || 
			    (!((c[0] >= '0') && (c[0] <='9') || ((c[0] >= 'a') && (c[0] <='f')) || 
			       ((c[0] >= 'A') && (c[0] <='F')) )))) /* so, currentToken = "0x" */
			    {
				currentToken="Invalid suffix on integer";
				laststate=state;
				state=ERRORSTATE;				
			    }			    
			else if(isWhiteSpace(c[0] ))
			    {
				c[0]='#';
				currentToken= (new Integer(Integer.parseInt(currentToken.substring(2),16))).toString();
				laststate=state;
				state=FINALSTATE;
			    }
			else if ((c[0] >= '0') && (c[0] <='9') || ((c[0] >= 'a') && (c[0] <='f')) || 
				((c[0] >= 'A') && (c[0] <='F')) )
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate = state;
				state=HEXNUMSTATE;
			    }
			else
			    {
				currentToken=(new Integer(Integer.parseInt(currentToken.substring(2),16))).toString();
				
				laststate = state;
				state=FINALSTATE;
			    }
			    

			    
			break;
			
		    }
	    

	    }
	return newToken;/*it should never get here, if it does, an ErrorToken is returned*/
    }
    
    private boolean isWhiteSpace(char c1)
    {
	return (c1 == ' ' || c1 == '\t' || c1 == '\n' || c1=='\r' || c1=='\f');
    }
   
    public Token getNextToken()
    {
	Token returnToken;

	if (lastTokenObject==null)
	    lastTokenObject=lex();	    
	
	while(lastTokenObject instanceof CommentToken){
	    lastTokenObject=lex();
	}

	while(lastTokenObject instanceof ErrorToken){
	    errorList.add(("Line #" + ((ErrorToken)lastTokenObject).lineNumber + " " + ((ErrorToken)lastTokenObject).stringvalue));
	    lastTokenObject=lex();
	}

	
	returnToken=lastTokenObject;
	lastTokenObject=lex();

	return returnToken;
    }

    public Token peekNextToken()
    {
	if (lastTokenObject != null)
	    return lastTokenObject;
	else{
	    lastTokenObject=lex();
	    return lastTokenObject;
	}
    }

    public boolean hasMoreTokens(){       
	return moreTokens;	
    }
        
    public int getLineCount(){
	return linecount;
    }

}
