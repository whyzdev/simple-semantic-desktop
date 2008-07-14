import java.util.*;

/* Akshat Singhal - Chris Fry */
/* Parser.java */

public class Parser
{
    
    boolean diag1=false, diag2=false, diag3=false, diag4=false;
    boolean diag5=false;
    ParseStack stack;

  
    public Parser(Lexer lexer){
	stack = new ParseStack(lexer);
    }

    public Node parse()
    {
	boolean succ=parseProgram();	
	if (succ) {

	    if (diag3){
		System.out.println("size: " + stack.getSize());
		System.out.println("stackpointer: " + stack.getStackPointer());
		System.out.println("top of stack is: " + stack.top());
	    }
	    ProgramNode progNode = (ProgramNode)stack.pop();
	    return progNode;
	}
	else { 
	    return new ErrorNode();
	}
    }




    private boolean parseProgram()
    {
	if (diag1)
	    System.out.println("I got here(parseProgram)");
	
	ProgramNode progNode;
	Object tempNode;
	int old_stackpointer = stack.getStackPointer();

	boolean succ = true;
	int numDecs = 0;



	while(succ==true)
	    {
		succ = parseVarDecl();
		if(!succ)
		    {
			succ = parseFunDecl();
			tempNode = stack.getNext();
		    }
		if(succ)
		    {
			stack.incStackPointer();
			numDecs++;
			
			if (diag5)
			    System.out.println("-Success! numDecs:" + numDecs);
		    }
		
	    }
	if (diag5)
	    System.out.println("Final numDecs:" + numDecs);
	if (diag2)
	    System.out.println("stack:" + stack.toString());
	progNode = new ProgramNode(numDecs+1);/*so we don't need a dynamic array*/
	stack.decStackPointer();
	for(int i = numDecs; i > 0;i--){
	    progNode.children[i]=(Node)stack.remove();
	    if (diag2)
		System.out.println("numDecs:" + i + ",last node: "+ progNode.children[i]);
	}


	stack.incStackPointer();
	if(stack.getNext() instanceof EOFToken)
	    {//NIRVANA:reaching EOF at the end of a valid program
		stack.remove();
		stack.insert(progNode);
		return true;
	    }
	else
	    {//something else but not EOF after a valid Program
		//don't stick on the correct program parsed so far,
		// just go to errorHandler()
		errorHandler();
		return false;
	    }
	
	// }
    }
    
    private void errorHandler(){
	;
    }

    private boolean parseVarDecl()
    {
	if (diag1)
	    System.out.println("I got here(parseVarDecl)");

	VarDeclNode varNode = new VarDeclNode();
	Object temp;
	int old_stackpointer = stack.getStackPointer();

	temp = stack.getNext();

	if(temp instanceof Token) {
	    
	    Token token = (Token)temp;
	    if (token.symbolNumber == 0 || token.symbolNumber == 1 || token.symbolNumber == 2) {
		//If token is an int, float, or void

		//note: the switch case has been replaced by an if that does the job
		    
		/*		switch (token.symbolNumber)
				{
				case 0:*/
		temp=stack.getNext();
		if (diag2)
		    System.out.println("new next token:" + (Token)temp);
		if(temp instanceof IdentifierToken)  {//If token is an identifier 
			
		    if (diag2)
			System.out.println("instanceof IdentifierToken found!");

		    temp=stack.getNext();
		    if(((Token)temp).symbolNumber == 34) {//If token is a ";"
				
			temp=stack.remove();
			varNode.children[2]=new TerminalNode((Token)temp);
			temp=stack.remove();
			varNode.children[1]=new TerminalNode((Token)temp);
			temp=stack.remove();
			varNode.children[0]=new TerminalNode((Token)temp);

			stack.insert(varNode);
			return true;
		    }
		    else if(((Token)temp).symbolNumber == 10) { //If token is a "["
				
			temp=stack.getNext();
			if(temp instanceof IntegerToken) {//if token is an integer
					
			    temp=stack.getNext();
			    if(((Token)temp).symbolNumber == 11) {//if token is an "]"
						
				temp=stack.getNext();
				if(((Token)temp).symbolNumber == 34) {//if token is a ";"
							
				    temp=stack.remove();
				    varNode.children[5]=new TerminalNode((Token)temp);
				    temp=stack.remove();
				    varNode.children[4]=new TerminalNode((Token)temp);
				    temp=stack.remove();
				    varNode.children[3]=new TerminalNode((Token)temp);
				    temp=stack.remove();
				    varNode.children[2]=new TerminalNode((Token)temp);
				    temp=stack.remove();
				    varNode.children[1]=new TerminalNode((Token)temp);
				    temp=stack.remove();
				    varNode.children[0]=new TerminalNode((Token)temp);

				    stack.insert(varNode);
				    return true;
				}	
				
			    }
			}
		    }
		}
		else if(((Token)temp).symbolNumber == 14) {//if token is a "*"
			
		    temp=stack.getNext();
		    if(temp instanceof IdentifierToken) {//if token is an identifier
				
			temp=stack.getNext();
			if(((Token)temp).symbolNumber == 34) {//if token is a ";"
					
			    temp=stack.remove();
			    varNode.children[3]=new TerminalNode((Token)temp);
			    temp=stack.remove();
			    varNode.children[2]=new TerminalNode((Token)temp);
			    temp=stack.remove();
			    varNode.children[1]=new TerminalNode((Token)temp);
			    temp=stack.remove();
			    varNode.children[0]=new TerminalNode((Token)temp);

			    stack.insert(varNode);
			    return true;
			}
		    }
		}
		/*		    case 1:
				    ;
				    case 2:	
				    ;
	
				    }*/

	    }
	}
	stack.setStackPointer(old_stackpointer);
	return false;

    }// end of parseVarDecl()

    /*
    //Dummy parseFunDecl() for testing
    private boolean parseFunDecl(){
	return false;
    }
    */

    
    private boolean parseFunDecl()
    {
	if (diag1)
	    System.out.println("I got here(parseFunDecl)");
	FunDeclNode funNode = new FunDeclNode();
	Object temp;
	boolean succ = false;
	int old_stackpointer = stack.getStackPointer();

	temp=stack.getNext();
	if(temp instanceof Token)
	    {
		Token token=(Token)temp;
		if (token.symbolNumber == 0 || token.symbolNumber == 1 || token.symbolNumber == 2) { 
		    //if token is int, float, or void
		    temp=stack.getNext();
		    if(temp instanceof IdentifierToken) {//If token is an identifier	
			temp=stack.getNext();
			if(((Token)temp).symbolNumber == 8) { //If token is a "("			    
			    succ = parseParams(); //try to parse next tokens as params
			    temp=stack.getNext(); 
			    if(succ) {		

				temp=stack.getNext();
				if(((Token)temp).symbolNumber == 9){ //If token is a ")"
				    succ = parseCompStmt(); 
				    //try to parse next tokens as a compd statement
				    if(succ) { 								
					//QUESTION:SHOULD A GETNEXT() BE DONE AFTER SUCC OR BEFORE?
					temp=stack.getNext(); 
					if(diag5)
					    System.out.println("stack is: " + stack);
					temp=stack.remove();
					funNode.children[5]=new TerminalNode((Token)temp);
					temp=stack.remove();
					funNode.children[4]=new TerminalNode((Token)temp);
					temp=stack.remove();
					funNode.children[3]=new TerminalNode((Token)temp);
					temp=stack.remove();
					funNode.children[2]=new TerminalNode((Token)temp);
					temp=stack.remove();
					funNode.children[1]=new TerminalNode((Token)temp);
					temp=stack.remove();
					funNode.children[0]=new TerminalNode((Token)temp);
					stack.insert(funNode);
					if (diag5)  
					    System.out.println(" created funNode:" + funNode);
					if(diag5)
					    System.out.println("stack is: " + stack);
					return true;
					
				    }
				}
			    }
			}
		    }
		}
	    }
	//if method hasn't returned TRUE yet, it means something failed, so return false.
	stack.setStackPointer(old_stackpointer);	
	return false;
    
    } // end of parseFunDecl()
    
    private boolean parseParams(){
	/*	Token temp;
	temp=stack.getNext();
	if ( temp instanceof IdentifierToken)
	    if (((Token)temp).stringvalue=="fakeparams")
		*/
	return true;
    }
    
    private boolean parseCompStmt(){
	return true;
    }
    
} // end of class Parser
