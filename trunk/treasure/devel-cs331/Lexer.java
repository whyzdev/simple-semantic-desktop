import java.util.*;
import java.io.*;


public class Lexer    
{
    Hashtable hashtable;
    Hashtable operatorStringTable;
    int hashcount=0,opStTblCount=0;
    Token lastTokenObject;
    boolean moreTokens=true;
    
    BufferedReader reader;
    char c[]=new char[2];
    String currentToken;
    int readint=0;

    public Lexer(BufferedReader reader1)
    {
	hashtable = new Hashtable();
	operatorStringTable = new Hashtable();
	/*
	  if(args.length == 0)
	  {
	  reader = new BufferedReader(new InputStreamReader(System.in));
	  }
	  else 
	  {
	  reader = new BufferedReader(new FileReader(args[0]));
	  }
	*/
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
	operatorStringTable.put(new Integer(opStTblCount++),"less");
	operatorStringTable.put(new Integer(opStTblCount++),"greater");
	operatorStringTable.put(new Integer(opStTblCount++),"less-or-eq");
	operatorStringTable.put(new Integer(opStTblCount++),"greater-or-eq");
	operatorStringTable.put(new Integer(opStTblCount++),"equals");
	operatorStringTable.put(new Integer(opStTblCount++),"not eq");
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

	hashtable.put("int",new Integer(hashcount++));
	hashtable.put("float",new Integer(hashcount++));
	hashtable.put("void",new Integer(hashcount++));
	hashtable.put("if",new Integer(hashcount++));
	hashtable.put("else",new Integer(hashcount++));
	hashtable.put("while",new Integer(hashcount++));
	hashtable.put("for",new Integer(hashcount++));
	hashtable.put("return",new Integer(hashcount++));
	hashtable.put("(",new Integer(hashcount++));
	hashtable.put(")",new Integer(hashcount++));
	hashtable.put("[",new Integer(hashcount++));
	hashtable.put("]",new Integer(hashcount++));
	hashtable.put(":",new Integer(hashcount++));
	hashtable.put(".",new Integer(hashcount++));
	hashtable.put("*",new Integer(hashcount++));
	hashtable.put("&",new Integer(hashcount++));
	hashtable.put("%",new Integer(hashcount++));
	hashtable.put("!",new Integer(hashcount++));
	hashtable.put("/",new Integer(hashcount++));
	hashtable.put("+",new Integer(hashcount++));
	hashtable.put("-",new Integer(hashcount++));
	hashtable.put("<",new Integer(hashcount++));
	hashtable.put(">",new Integer(hashcount++));
	hashtable.put("<=",new Integer(hashcount++));
	hashtable.put(">=",new Integer(hashcount++));
	hashtable.put("==",new Integer(hashcount++));
	hashtable.put("!=",new Integer(hashcount++));
	hashtable.put("+=",new Integer(hashcount++));
	hashtable.put("-=",new Integer(hashcount++));
	hashtable.put("&&",new Integer(hashcount++));
	hashtable.put("||",new Integer(hashcount++));
	hashtable.put("{",new Integer(hashcount++));
	hashtable.put("}",new Integer(hashcount++));
	hashtable.put(",",new Integer(hashcount++));
	hashtable.put(";",new Integer(hashcount++));
	hashtable.put("=",new Integer(hashcount++));


	c[0]='#';
	c[1]='#';
    }

    boolean eofReached=false;
    boolean eofReturned=false;
    boolean eoComment=false;
    
    private Token lex()
    {
	currentToken="";
	
	
	int state=0;
	int laststate=0;
	boolean tokenFinished = false;/*tokenFinished becomes true if end of file is reached or if no input is available*/
	Token newToken=new ErrorToken("no token");

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
	final int PIPEDECIMALSTATE=5;
	final int ERRORSTATE=7;
	final int COMMENTSTATE=8;
	final int REALNUMSTATE=9;
	final int HEXNUMSTATE=10;




	while (!tokenFinished)
	    {
		if(c[0] == '#') 
		    try {
			readint=reader.read();
			c[0]=(char)readint;
		    }
		    catch(IOException e){
			tokenFinished=true;
			System.err.println("No Input");
		    }
		//	System.out.println("<"+c[0]+">" + "{"+readint+"}");
		
		
		if(readint == -1) /* End Of File */
		    {							    	
			eofReached=true;
			state=FINALSTATE;
			tokenFinished=true;
			moreTokens=false;
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
				c[1]=c[0];
				c[0]='#';			
				laststate=state;
				state=DOUBLESYMBOLSTATE; /* possible double symbol */
			    }
			else if(c[0] == '|' ) 
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				laststate=state;
				state=PIPEDECIMALSTATE; /* pipe or single decimal */
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
			if(isWhiteSpace(c[0] ))
			    {
				c[0]='#';
				laststate=state;
				state=FINALSTATE;
			    }
			else if((c[0] >= '0') && (c[0] <='9'))
			    {
				if(c[1] == 'e' || c[1] == 'E')
				    {
					currentToken=currentToken.concat((new Character(c[1])).toString());
					currentToken=currentToken.concat((new Character(c[0])).toString());
					c[0]='#';
					c[1]='#';
					state=9; /* real numbers */
				    }
				else
				    {
					currentToken=currentToken.concat((new Character(c[0])).toString());
					c[0]='#';
				    }
			    }
			else if(currentToken.charAt(0) == '0' && (c[0] == 'x' || c[0]=='X')
								  && currentToken.length()==1)
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());				
				c[0]='#';
				state=HEXNUMSTATE; /* hex numbers */
			    }
			else if(c[0] == '.')
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
				state=9; /* real numbers */
			    }
			else if(c[0] == 'e' || c[0] == 'E')
			    {
				c[1] = c[0];
				c[0]='#';
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
			if(isWhiteSpace(c[0] ))
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
			    
			    if (eofReached && (currentToken=="" || readint==-1)){ // unclen way of returning an EOF Token if you've reached EOF and have a blank token				
				
				return new EOFToken();
				
			    }
			    switch (laststate){				
			    case INITIALSTATE: /* create single symbol */
				/*
				  System.out.println("tried to create symbol number for <" + currentToken+">"); //REMOVE
				System.out.println("laststate: " + laststate); //REMOVE
				System.out.println("eofReached:" + eofReached); //REMOVE
				
				*/
				symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
				newToken = new OperatorToken(currentToken, symbolNumber, (String)operatorStringTable.get(new Integer(symbolNumber)));
				break;
			    case NUMBERSTATE: /* must be regular integer*/
				if(!hashtable.contains(currentToken)) /* new integer token */
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new IntegerToken(currentToken, hashcount);
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
					newToken = new IntegerToken(currentToken, symbolNumber);
				    }
				break;
			    case ALPHASTATE: /* keyword or identifier */
				if(!hashtable.containsKey(currentToken)) /* must be new identifier */
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new IdentifierToken(currentToken, hashcount);
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
					//					System.out.println("\nI got symbol number " + symbolNumber);
					if(symbolNumber < 8) /* must be keyword */
					    {
						newToken = new KeywordToken(currentToken, symbolNumber); 
					    }
					else /* repeat identifier */ 
					    {
						newToken = new IdentifierToken(currentToken, symbolNumber);
					    }
					//	System.out.println("\nmy new token is : " + newToken);
				    }
				break;
			    case DOUBLESYMBOLSTATE: /* possible double symbols... could be single symbols */
				/*		
  System.out.println("tried to create symbol number for <" + currentToken+">"); //REMOVE
				System.out.println("laststate: " + laststate); //REMOVE
				System.out.println("eofReached:" + eofReached); //REMOVE
				*/
			    	symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
				newToken = new OperatorToken(currentToken, symbolNumber, (String)operatorStringTable.get(new Integer(symbolNumber)));
				break;
			    case PIPEDECIMALSTATE: /* pipe. create an operator token */
				symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
				newToken = new OperatorToken(currentToken, symbolNumber, (String)operatorStringTable.get(new Integer(symbolNumber)));
				break;
			    case ERRORSTATE: /* error token */
				newToken = new ErrorToken(currentToken);
				break;
			    case REALNUMSTATE: /* real numbers */
				++hashcount;
				hashtable.put(currentToken, new Integer(hashcount));
				newToken = new FloatToken(currentToken, hashcount);
				break;
			    case HEXNUMSTATE: /* hexidecimal numbers. already converted to equivalent to case 1 above */
				if(!hashtable.contains(currentToken)) /* new integer token */
				    {
					++hashcount;
					hashtable.put(currentToken, new Integer(hashcount));
					newToken = new IntegerToken(currentToken, hashcount);
				    }
				else
				    {
					symbolNumber = ((Integer)hashtable.get(currentToken)).intValue();
					newToken = new IntegerToken(currentToken, symbolNumber);
				    }
				break;
			    case COMMENTSTATE:
				if(eoComment==true)
				    {
					newToken=new CommentToken(currentToken);
				    }
				else
				    {					
					newToken = new ErrorToken("Unfinished comment at end of file");
				    }
				eoComment=false;
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
				currentToken=currentToken.concat((new Character(c[1])).toString());
				c[0]='#';
				c[1]='#';
				laststate=state;
				state=FINALSTATE;
			    }
			else
			    {
				switch (c[1]){
				case '!':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '+':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '-':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '=':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{ 
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '>':				  
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{					 
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '<':
				    if(c[0] == '=')
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '&':
				    if(c[0] == '&')
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    else
					{		
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '/':
				    if(c[0] == '*')
					{
					    currentToken="";
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=COMMENTSTATE; /* commenting */
					}
				    else
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				case '.':
				    if ((c[0] >= '0') && (c[0] <='9'))
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    currentToken=currentToken.concat((new Character(c[0])).toString());
					    c[0]='#';
					    c[1]='#';
					    laststate=state;
					    state=9;
					}
				    else
					{
					    currentToken=currentToken.concat((new Character(c[1])).toString());
					    c[1]='#';
					    laststate=state;
					    state=FINALSTATE;
					}
				    break;
				default:
				    currentToken=currentToken.concat((new Character(c[1])).toString());
				    c[1]='#';
				    laststate=state;
				    state=FINALSTATE;
				}
			    }

			break;
			//***************************************************************
			//***************************************************************
			//***************************************************************
		    case PIPEDECIMALSTATE: /* pipe or single decimal */
			switch(currentToken.charAt(0)){
			case '|':
			    if(c[0] == '|')
				{
				    currentToken=currentToken.concat((new Character(c[0])).toString());
				    c[0]='#';
				    laststate=state;
				    state=FINALSTATE;
				}
			    else
				{
				    state=7; /* single '|' error */
				}
			    break;
			case '.':
			    if((c[0] >= '0') && (c[0] <='9'))
				{
				    currentToken=currentToken.concat((new Character(c[0])).toString());
				    c[0]='#';
				    laststate=state;
				    state=9; /* confirmed to be real number token */
				}
			    else
				{
				    state=7; /* single '.' error */
				}
			    break;
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

			laststate=state;
			if(c[0] == '*')
			    {
				c[1] = c[0];
				c[0]='#';
			    }
			
			else if ((c[0] == '/') && (c[1] == '*'))
			    {
				currentToken=currentToken.concat((new Character(c[1])).toString());
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0] = '#';
				c[1] = '#';
				eoComment=true;
				
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
		    case REALNUMSTATE: /* real numbers */
			if(isWhiteSpace(c[0] ))
			    {
				c[0]='#';
				laststate=state;
				state=FINALSTATE;
			    }
			else if((c[0] >= '0') && (c[0] <='9') || (c[0]=='e' || c[0]=='E'))
			    {
				currentToken=currentToken.concat((new Character(c[0])).toString());
				c[0]='#';
			    }
			else
			    {
				laststate = state;
				state=FINALSTATE;
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
	    
		/*
		if(readint == -1) /* End Of File */
		/*{

			return new EOFToken();			
		    }*/
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
    

}
