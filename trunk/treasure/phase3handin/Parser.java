import java.util.*;

/* Akshat Singhal - Chris Fry */
/* Parser.java 
   performs parsing for the Akshat and Chris Compiler (ACC)
   parse() returns a parse tree if successful, an error node if not.
*/

public class Parser
{
    private ParseStack stack;
    
    final int PROGRAM=0;
    final int VARDECL=1;
    final int FUNDECL=2;
    final int PARAM=3;
    final int COMPSTMT=4;
    final int LOCALDECS=5;
    final int STMTLIST=6;
    final int STATEMENT=7;
    final int EXPRSTMT=8;
    final int IFSTMT=9;
    final int IFSTART=10;
    final int IFREM=11;
    final int WHILESTMT=12;
    final int FORSTMT=13;
    final int RETURNSTMT=14;
    final int OPTEXPR=15;
    final int PARAMS=16;
    final int EXPR=17;
    final int OREXPR=18;
    final int RELEXPR=19;
    final int ADDEXPR=20;
    final int TERM=21;
    final int UNARYEXPR=22;
    final int PRIMARYEXPR=23;
    final int ARGS=24;


    ArrayList errors; // instantiate in constructor
    boolean error = false;
    Lexer lexer;
  
    public Parser(Lexer lexer){
	stack = new ParseStack(lexer);
	this.lexer= lexer;
	errors = new ArrayList();
    }

    public Node parse()
    {
	boolean succ=parseProgram();	
	errors.addAll(lexer.errorList);	
	if (succ && (!error) && (lexer.errorList.isEmpty())) {
	    ProgramNode progNode = (ProgramNode)stack.pop();
	    return progNode;
	}
	else { 
	    return new ErrorNode();
	}
    }




    private boolean parseProgram()
    {
	ProgramNode progNode = new ProgramNode(0);
	Object tempNode;
	int old_stackpointer = stack.getStackPointer();

	boolean succ = true;
	int numDecs = 0;



	while(succ==true)
	    {

		succ = parseVarDecl();
		if(!succ){		    
		    succ = parseFunDecl();				    
		}

		if(succ)
		    {
			stack.incStackPointer();
			numDecs++;
		    }
		
	    }


	
	if(numDecs != 0){
	    progNode = new ProgramNode(numDecs);/*so we don't need a dynamic array*/
	   	    
	    for(int i = numDecs-1; i >= 0;i--){
	
		if (stack.get(stack.getStackPointer()-1) instanceof Node)
		    progNode.children[i]=(Node)stack.remove();
	
	    }

	}

	if (stack.getNext() instanceof EOFToken)
	    {//NIRVANA:reaching EOF at the end of a valid program
		progNode.linenumber = 0;
		stack.insert(progNode);
		return true;
	    }
	
	//something else but not EOF after a valid Program
	//don't stick on the correct program parsed so far,
	// just go to errorHandler()
	errorHandler(old_stackpointer, PROGRAM, "EOF");
	return false;
	    
	
	// }
    }
    
    private boolean errorHandler(int old_stackpointer, int function, Object expected){
	Object temp;
	stack.decStackPointer();
	temp = stack.getNext();
	if(temp instanceof EOFToken){
	    return false; 
	}
	
	if(! (temp instanceof Token)){
	    errors.add("Compiler error: " + temp);
	    return false;
	}




	
	switch (function){
	case PROGRAM:
	    temp = stack.get(stack.getStackPointer()-1);
	    if(temp instanceof ErrorToken){
		errors.add("Line #" + ((ErrorToken)temp).lineNumber + " " + ((ErrorToken)temp).stringvalue); 
		error=true;
		return false;
	    }
	    else{
		errors.add("Line #" + ((Token)temp).lineNumber + " " + "'EOF' expected, '" + temp + "' found");
		error=true;
		return false;
	    }
	    

	case VARDECL:
	    temp = stack.get(stack.getStackPointer()-1);
	    if(temp instanceof ErrorToken){
		errors.add("Line #" + ((ErrorToken)temp).lineNumber + " " + ((ErrorToken)temp).stringvalue); 
		error=true;
		stack.remove();
		stack.setStackPointer(old_stackpointer);
		return true;
	    }
	    else{
		stack.setStackPointer(old_stackpointer);
		return false;
	    }






	case EXPRSTMT:
	case IFSTMT:
	case IFSTART:
	case IFREM:
	case WHILESTMT:
	case FORSTMT:
	case RETURNSTMT:
	case OPTEXPR:
	case EXPR:
	case OREXPR:
	case RELEXPR:
	case ADDEXPR:
	case TERM:
	case UNARYEXPR:
	case PRIMARYEXPR:
	case ARGS:
	case STATEMENT:
	case PARAM:
	case FUNDECL:
	    temp = stack.get(stack.getStackPointer()-1);
	    if(temp instanceof ErrorToken){
		errors.add("Line #" + ((ErrorToken)temp).lineNumber + " " + ((ErrorToken)temp).stringvalue); 
		error=true;
		stack.remove();
		stack.setStackPointer(old_stackpointer);
		return false;
	    }
	    else{
		errors.add("Line #" + ((Token)temp).lineNumber + " " + "'" + expected + "' expected, '" + temp + "' found");
		error=true;
		return false;
	    }

	case STMTLIST:
	case LOCALDECS:
	case COMPSTMT:
	case PARAMS:
	    temp = stack.get(stack.getStackPointer()-1);
	    if(temp instanceof ErrorToken){
		errors.add("Line #" + ((ErrorToken)temp).lineNumber + " " + ((ErrorToken)temp).stringvalue); 
		error=true;
		stack.remove();
		stack.setStackPointer(old_stackpointer);
		return true;
	    }
	    else{
		errors.add("Line #" + ((Token)temp).lineNumber + " " + "'" + expected + "' expected, '" + temp + "' found");
		error=true;
		return false;
	    }
	
	}
	return false;
    }



    private boolean parseVarDecl()
    {
	VarDeclNode varNode;
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	
	temp = stack.getNext();
	if(temp instanceof EOFToken)
	    {
		stack.setStackPointer(old_stackpointer);
		return false;
	    }
	
	if(!(temp instanceof Token)){
	    errorHandler(old_stackpointer, VARDECL, null);
	    return false;
	}
	
	
	if(((Token)temp).symbolNumber == 0 || ((Token)temp).symbolNumber == 1) {//If token is an int or float
	    
	    temp=stack.getNext();
	    
	    if(temp instanceof IdentifierToken){ //If token is an identifier 
		
		temp=stack.getNext();
		
		if(((Token)temp).symbolNumber == 34){ //If token is a ";"
		    
		    varNode = new VarDeclNode(2);
		    
		    temp=stack.remove(); //get rid of the semic
		    temp=stack.remove(); //get the id
		    varNode.children[1]=new IdentifierNode((Token)temp);
		    temp=stack.remove(); //get the type
		    String string = temp.toString();
		    if(string.compareTo("int") == 0)
			varNode.children[0]=new IntNode((Token)temp);
		    else
			varNode.children[0]=new FloatNode((Token)temp);
		    varNode.linenumber = ((Token)temp).lineNumber;
		    stack.insert(varNode);
		    return true;
		}
		else if(((Token)temp).symbolNumber == 10) //If token is a "["
		    temp=stack.getNext();
		else if(errorHandler(old_stackpointer, VARDECL, null))
		    return parseVarDecl();
		else
		    return false;
		
		if(temp instanceof IntegerToken) //if token is an integer
		    temp=stack.getNext();
		else if(errorHandler(old_stackpointer, VARDECL, null))
		    return parseVarDecl();
		else
		    return false;
		
		
		if(((Token)temp).symbolNumber == 11) //if token is an "]"
		    temp=stack.getNext();
		else if(errorHandler(old_stackpointer, VARDECL, null))
		    return parseVarDecl();
		else
		    return false;
		
		
		if(((Token)temp).symbolNumber == 34) {//if token is a ";"
		    
		    varNode = new VarDeclNode(3);
		    
		    temp=stack.remove(); //get rid of the semic
		    temp=stack.remove(); //get rid of the "]"
		    temp=stack.remove(); //get the num
		    varNode.children[2]=new NumNode((IntegerToken)temp);
		    temp=stack.remove(); //get rid of the "["
		    temp=stack.remove(); //get the id
		    varNode.children[1]=new IdentifierNode((Token)temp);
		    temp=stack.remove(); //get the type



		    String string = temp.toString();
		    if(string.compareTo("int") == 0)
			varNode.children[0]=new IntNode((Token)temp);
		    else
			varNode.children[0]=new FloatNode((Token)temp);
		    varNode.linenumber = ((Token)temp).lineNumber;
		    stack.insert(varNode);
		    return true;
		}					
		else if(errorHandler(old_stackpointer, VARDECL, null))
		    return parseVarDecl();
		else
		    return false;
	    }			  
	    else if(((Token)temp).symbolNumber == 14) //if token is a "*"
		temp=stack.getNext();
	    else if(errorHandler(old_stackpointer, VARDECL, null))
		return parseVarDecl();
	    else
		return false;
	    
	    
	    if(temp instanceof IdentifierToken) //if token is an identifier
		temp=stack.getNext();
	    else if(errorHandler(old_stackpointer, VARDECL, null))
		return parseVarDecl();
	    else
		return false;
	    
	    
	    if(((Token)temp).symbolNumber == 34) {//if token is a ";"
		
		varNode = new VarDeclNode(3);
		
		temp=stack.remove(); //get rid of the semic
		temp=stack.remove(); //get the id
		varNode.children[2]=new IdentifierNode((Token)temp);
		temp=stack.remove(); //get the "*"
		varNode.children[1]=new TerminalNode((Token)temp);
		temp=stack.remove(); //get the type
		String string = temp.toString();
		if(string.compareTo("int") == 0)
		    varNode.children[0]=new IntNode((Token)temp);
		else
		    varNode.children[0]=new FloatNode((Token)temp);
		varNode.linenumber=((Token)temp).lineNumber;
		stack.insert(varNode);
		return true;
	    }
	}
	else if(((Token)temp).symbolNumber == 2){// void
	    temp=stack.getNext();
	    
	    
	    if((temp instanceof Token) && ((Token)temp).symbolNumber == 14)
		temp=stack.getNext();

	    if(temp instanceof IdentifierToken)
		temp=stack.getNext();
	    else if(errorHandler(old_stackpointer, VARDECL, null))
		return parseVarDecl();
	    else
		return false;
	    
	    
	    if(((Token)temp).symbolNumber == 34){//if token is a ";"
		
		varNode = new VarDeclNode(3);
		
		temp=stack.remove(); //get rid of the semic
		temp=stack.remove(); //get the id
		varNode.children[2]= new IdentifierNode((Token)temp);
		temp=stack.remove(); //get the semic
		varNode.children[1]= new TerminalNode((Token)temp);
		temp=stack.remove(); //get the type
		varNode.children[0]= new VoidNode((Token)temp);
		varNode.linenumber=((Token)temp).lineNumber;
		stack.insert(varNode);
		return true;
	    }
	}
	else if(errorHandler(old_stackpointer, VARDECL, null))
	    return parseVarDecl();
	else
	    return false;
	
	//shouldn't ever get here  
	stack.setStackPointer(old_stackpointer);
	return false;
	
    }// end of parseVarDecl()

    
    private boolean parseFunDecl(){
	FunDeclNode funNode = new FunDeclNode(0);
	Object temp;
	boolean succ = false;
	boolean pointerreturn = false;
	int old_stackpointer = stack.getStackPointer();
	
	temp=stack.getNext();
	
	if(!(temp instanceof Token)){
	    errorHandler(old_stackpointer, FUNDECL, "Token");
	    return false;
	}
	    
	
	if (temp instanceof EOFToken)
	    {
		stack.setStackPointer(old_stackpointer);
		return false;
	    }
		    
	if (((Token)temp).symbolNumber == 0 || ((Token)temp).symbolNumber == 1 || ((Token)temp).symbolNumber == 2) //if token is int, float, or void
	    temp=stack.getNext();
	else if(errorHandler(old_stackpointer, FUNDECL, "int, float, or void"))
	    return false;
	
	if (((Token)temp).symbolNumber == 14){
	    temp=stack.getNext();
	    pointerreturn=true;
	}
	
	    
	
	if(temp instanceof IdentifierToken) //If token is an identifier	
	    {
		succ = parseParams(); //try to parse next tokens as params
		//stack.incStackPointer();
	    }
	else if(errorHandler(old_stackpointer, FUNDECL, "identifier"))
	    return parseVarDecl();
	else
	    return false;
	   
	
	if(succ){
	    succ = parseCompStmt(); //try to parse next tokens as a compd statement
	    stack.incStackPointer();
	}
	else if(errorHandler(old_stackpointer, FUNDECL, "valid parameters"))
	    return parseVarDecl();
	else
	    return false;
	    

	if (succ){
 	    if (!pointerreturn){
		funNode = new FunDeclNode(4);
		temp=stack.remove();
	    
		funNode.children[3]= (CompStmtNode)temp;
		    
	 
		temp=stack.remove(); // get the params
		funNode.children[2]= (ParamsNode)temp;

		temp=stack.remove(); // get the id
		funNode.children[1]= new IdentifierNode((Token)temp);

		temp=stack.remove(); // get the type		
	    }
	    else {

		funNode = new FunDeclNode(5);
		
		temp=stack.remove();		
		funNode.children[4]= (CompStmtNode)temp;		
		
		temp=stack.remove(); // get the params
		funNode.children[3]= (ParamsNode)temp;

		temp=stack.remove(); // get the id
		funNode.children[2]= new IdentifierNode((Token)temp);

		temp=stack.remove(); // get the *

		funNode.children[1]= (TerminalNode)new TerminalNode((Token)temp);



		temp=stack.remove(); // get the type		
	    }

	    funNode.linenumber=((Token)temp).lineNumber;
	    String string = temp.toString();
	    if(string.compareTo("int") == 0)
		funNode.children[0]=new IntNode((Token)temp);
	    else if(string.compareTo("float") == 0)
		funNode.children[0]=new FloatNode((Token)temp);
	    else
		funNode.children[0]=new VoidNode((Token)temp);
	    

	    stack.insert(funNode);
	    
	    return true;	    
	}
   	    
	//if method hasn't returned TRUE yet, it means something failed, so return false.
	return false;
    
    } // end of parseFunDecl()
    
    /*
    
    private boolean parseFunDecl(){
	FunDeclNode funNode = new FunDeclNode(0);
	Object temp;
	boolean succ = false;
	int old_stackpointer = stack.getStackPointer();
	
	temp=stack.getNext();
	
	if(!(temp instanceof Token)){
	    errorHandler(old_stackpointer, FUNDECL, "Token");
	    return false;
	}
	    
	
	if (temp instanceof EOFToken)
	    {
		stack.setStackPointer(old_stackpointer);
		return false;
	    }
		    
	if (((Token)temp).symbolNumber == 0 || ((Token)temp).symbolNumber == 1 || ((Token)temp).symbolNumber == 2) //if token is int, float, or void
	    temp=stack.getNext();
	else if(errorHandler(old_stackpointer, FUNDECL, "int, float, or void"))
	    return false;
	   
	if(temp instanceof IdentifierToken) //If token is an identifier	
	    {
		succ = parseParams(); //try to parse next tokens as params
		//stack.incStackPointer();
	    }
	else if(errorHandler(old_stackpointer, FUNDECL, "identifier"))
	    return parseVarDecl();
	else
	    return false;
	   
	
	if(succ){
	    succ = parseCompStmt(); //try to parse next tokens as a compd statement
	    stack.incStackPointer();
	}
	else if(errorHandler(old_stackpointer, FUNDECL, "valid parameters"))
	    return parseVarDecl();
	else
	    return false;
	    

	if (succ){
 	    
	    funNode = new FunDeclNode(4);
	    temp=stack.remove();
	    
	    funNode.children[3]= (CompStmtNode)temp;
		    
	 
	    temp=stack.remove(); // get the params
	    funNode.children[2]= (ParamsNode)temp;

	    temp=stack.remove(); // get the id
	    funNode.children[1]= new IdentifierNode((Token)temp);
	    temp=stack.remove(); // get the type
	    funNode.linenumber=((Token)temp).lineNumber;
	    String string = temp.toString();
	    if(string.compareTo("int") == 0)
		funNode.children[0]=new IntNode((Token)temp);
	    else if(string.compareTo("float") == 0)
		funNode.children[0]=new FloatNode((Token)temp);
	    else
		funNode.children[0]=new VoidNode((Token)temp);
	    
	    stack.insert(funNode);
	    
	    return true;	    
	}
   	    
	//if method hasn't returned TRUE yet, it means something failed, so return false.
	return false;
    
    } // end of parseFunDecl()
    */
  
    private boolean parseParams(){
	boolean succ = true;
	boolean gotOne = false;
	int numParams = 0;
	ParamsNode paramsNode;
	Object temp;
	int old_stackpointer = stack.getStackPointer();

	temp = stack.getNext();
	
	if(((Token)temp).symbolNumber == 8) //If token is a "("
	    stack.remove();
	else if(errorHandler(old_stackpointer, PARAMS, "("))
	    return parseParam();
	else
	    return false;  
	  
	temp=stack.getNext();
	if(((Token)temp).symbolNumber==2) //temp is void	
	    {
		if(((Token)(temp=stack.getNext())).symbolNumber==9)// ')', case of 'void' params
		    {
			paramsNode= new ParamsNode(1);
			stack.remove();
			paramsNode.children[0]= new VoidNode((Token)stack.remove());

			paramsNode.linenumber=((Token)temp).lineNumber;

			stack.insert(paramsNode);
			stack.incStackPointer();
			return true;
		    }
		else
		    stack.decStackPointer();
	    }
	
	stack.decStackPointer();
	while(succ==true){
	    
	    succ = parseParam();
	    
	    if(succ){
		stack.incStackPointer();
		gotOne = true;
		numParams++;

		temp = stack.getNext();
		if(((Token)temp).symbolNumber == 33) //if it's ","
		    stack.remove();
		else {
		    succ = false;
		    stack.decStackPointer();
		}
    }
	}
	
	temp = stack.getNext();

	if(((Token)temp).symbolNumber == 9){ //If token is a ")"
	    stack.remove();	    
	    succ=true;
	}
	else if(errorHandler(old_stackpointer, PARAMS, ")"))
	    return parseParam();
	else
	    return false;
		
	if(gotOne && succ){
   	    paramsNode = new ParamsNode(numParams);	    
	    for(int i=numParams-1; i >= 0; i--)
		paramsNode.children[i]=(ParamNode)stack.remove();
	    paramsNode.linenumber=paramsNode.children[0].linenumber;

	    stack.insert(paramsNode);
	    stack.incStackPointer();
	   
	    return true;
	}

	return false;
    } // end of parseParams()
  
       
    private boolean parseParam(){
	ParamNode paramNode = new ParamNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	
	temp = stack.getNext();

	if(!(temp instanceof Token)){
	    errorHandler(old_stackpointer, PARAM, "Token");
	    return false;
	}
	    	    
	if(((Token)temp).symbolNumber == 0 || ((Token)temp).symbolNumber == 1){ //int or float
	    temp=stack.getNext();

	    if(temp instanceof IdentifierToken){ //If token is an identifier 
		temp=stack.getNext();
	
		if(((Token)temp).symbolNumber == 10){ // if it's "["
		    temp=stack.getNext();

		    if(((Token)temp).symbolNumber == 11){ // if it's "]"
			paramNode = new ParamNode(4);
			
			temp=stack.remove(); //remove the "]"
			paramNode.children[3]=new IdentifierNode((Token)temp);
			temp=stack.remove(); //remove the "["
			paramNode.children[2]=new IdentifierNode((Token)temp);
			temp=stack.remove(); // get the id
			paramNode.children[1]=new IdentifierNode((Token)temp);
			temp=stack.remove(); //get the type
			String string = temp.toString();
			if(string.compareTo("int") == 0)
			    paramNode.children[0]=new IntNode((Token)temp);
			else 
			    paramNode.children[0]=new FloatNode((Token)temp);

			paramNode.linenumber=((Token)temp).lineNumber;
			stack.insert(paramNode);
			return true;
		    }
		    else if(errorHandler(old_stackpointer, PARAM, "]"))
			return parseParam();
		    else
			return false;
		
		}
		else{
		    stack.decStackPointer();
		    paramNode = new ParamNode(2);
		    
		    temp=stack.remove(); //get the id
		    paramNode.children[1]=new IdentifierNode((Token)temp);
		    temp=stack.remove(); //get the type
		    String string = temp.toString();
		    if(string.compareTo("int") == 0)
			paramNode.children[0]=new TerminalNode((Token)temp);
		    else
			paramNode.children[0]=new FloatNode((Token)temp);
		    
		    paramNode.linenumber=((Token)temp).lineNumber;
		    stack.insert(paramNode);
		    return true;
		}
	    }
	    else if(((Token)temp).symbolNumber == 14){ //if it's "*"
		temp=stack.getNext();
		if(temp instanceof IdentifierToken){
		    paramNode = new ParamNode(3);
			
		    temp=stack.remove(); // get the id
		    paramNode.children[2]=new IdentifierNode((Token)temp);
		    temp=stack.remove(); //get the "*"
		    paramNode.children[1]=new TerminalNode((Token)temp);
		    temp=stack.remove(); // get the type
		    String string = temp.toString();
		    if(string.compareTo("int") == 0)
			paramNode.children[0]=new TerminalNode((Token)temp);
		    else
			paramNode.children[0]=new FloatNode((Token)temp);
			
		    paramNode.linenumber=((Token)temp).lineNumber;			
		    stack.insert(paramNode);
		    return true;
		}
		else if(errorHandler(old_stackpointer, PARAM, "Identifier"))
		    return parseParam();
		else
		    return false;
		    
	    }
	    else if(errorHandler(old_stackpointer, PARAM, "Identifier or *"))
		return parseParam();
	    else
		return false;
		
	}
	else if(((Token)temp).symbolNumber == 2) // void
	    temp = stack.getNext();
	else if(errorHandler(old_stackpointer, PARAM, "int, float or void"))
	    return parseParam();
	else
	    return false;
	    
	
	if(((Token)temp).symbolNumber == 14)// if it's "*"
	    temp=stack.getNext();
	else if(errorHandler(old_stackpointer, PARAM, "*"))
	    return parseParam();
	else
	    return false;
	    

	if(temp instanceof IdentifierToken){
	    paramNode = new ParamNode(3);

	    temp=stack.remove(); // get the id
	    paramNode.children[2]=new IdentifierNode((Token)temp);
	    temp=stack.remove(); //get the "*"
	    paramNode.children[1]=new TerminalNode((Token)temp);
	    temp=stack.remove(); //get the type
	    paramNode.children[0]=new VoidNode((Token)temp);
	    
	    paramNode.linenumber=((Token)temp).lineNumber;
	    stack.insert(paramNode);
	    return true;
	}
	else if(errorHandler(old_stackpointer, PARAM, "Identifier"))
	    return parseParam();
	else
	    return false;
	    
		
	//stack.setStackPointer(old_stackpointer);
	//return false;
    }// end of parseParam()

    /*    
    private boolean errorHandler(){
	return true;
    }
    */

    private boolean parseCompStmt(){
	
	boolean succ = true;
	boolean ld = false; //does compound statement have local declarations?
	boolean sl = false; //does compound statement have a statement list?
	int old_stackpointer = stack.getStackPointer();
	CompStmtNode compStmtNode = new CompStmtNode(0);

	Object temp = stack.getNext();

	
	if ((!(temp instanceof Token)) && 
	    !(errorHandler(old_stackpointer, COMPSTMT, "Token")))	    
	    return false;

	if((((Token)temp).symbolNumber == 31)){ //if it's "{"
	    //code added in phase 3 to handle {}
	    //---------------------------------
	    temp=stack.getNext();
	    if((((Token)temp).symbolNumber == 32)){ //if it's "}"
		stack.remove(); // remove the }
		stack.remove(); // remove the {
		compStmtNode.linenumber = ((Token)temp).lineNumber;
		stack.insert(compStmtNode);
		return true;
	    }
	    else {
		stack.decStackPointer();
	    }
	    //--------------------------------
	    succ = parseLocalDecs();	   
	}
	else {
	    //special intervention: will have to fail this function if something that is not a { is found
	    //or else everything becomes mad recursive.
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
	
	
	if(succ){
	    ld = true;
	    stack.incStackPointer();
	    succ = parseStmtList();
	   
	}
	else 
	    succ = parseStmtList();	
	if(succ){
	    stack.incStackPointer();
	    temp = stack.getNext();
	    sl = true;
	} 
	else {
	    temp = stack.getNext();
	}


	
	if(( temp instanceof Token &&  ((Token)temp).symbolNumber == 32)){ //if it's "}"
	    compStmtNode = new CompStmtNode(2);

	    if(ld && sl){
		
		temp=stack.remove(); // remove the "}"
		temp=stack.remove(); //get the StmtList
		compStmtNode.children[1]= (StmtListNode)temp;
		temp=stack.remove(); // get the LocalDecs
		compStmtNode.children[0]= (LocalDecsNode)temp;
		temp=stack.remove(); //remove the "{"

		compStmtNode.linenumber=((Token)temp).lineNumber;
		stack.insert(compStmtNode);	
		return true;
	    }
	    
	    if(sl){
		compStmtNode = new CompStmtNode(1);

		temp=stack.remove(); // remove the "}"
		temp=stack.remove(); //get the StmtList
		compStmtNode.children[0]= (StmtListNode)temp;
		temp=stack.remove(); // remove the "{"

		compStmtNode.linenumber=((Token)temp).lineNumber;
		stack.insert(compStmtNode);	
		return true;
	    } 
	    
	    if(ld){
		compStmtNode = new CompStmtNode(1);

		temp=stack.remove(); // remove the "}"
		temp=stack.remove(); //get the LocalDecs
		compStmtNode.children[0]= (LocalDecsNode)temp;
		temp=stack.remove(); // remove the "{"

		compStmtNode.linenumber=((Token)temp).lineNumber;
		stack.insert(compStmtNode);	
		return true;
	    }
	}   
	else 
	    errorHandler(old_stackpointer, COMPSTMT, "}");

	//may return false since there may be no localdecs or statementlist
	return false;

    } // end of parseCompStmt()

    
    private boolean parseLocalDecs(){
	LocalDecsNode localDecsNode = new LocalDecsNode(0);
	int old_stackpointer = stack.getStackPointer();

	boolean succ = true;
	int numChildren = -1;

	while(succ==true)
	    {
		succ = parseVarDecl();
	
		if(succ)
		    {
			stack.incStackPointer();
			numChildren++;
		    }
	    }

	if(numChildren != -1){
	    localDecsNode = new LocalDecsNode(numChildren+1);
	    //stack.decStackPointer(); //can't remember why we do this...
	    for(int i = numChildren; i >= 0;i--){
		localDecsNode.children[i]=(VarDeclNode)stack.remove();
	    }

	    localDecsNode.linenumber=localDecsNode.children[0].linenumber;

	    stack.insert(localDecsNode);
	    //System.out.println("leaving parseLocalDecs, stack:" + stack.toString() + " stackpointer = " + stack.getStackPointer());
	    return true;
	}
	else{
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
    } // end of parseLocalDecs()
    

    private boolean parseStmtList(){
	//parseStmtList was modified because it returns a node with 2 children for a 1 statement list
	
	StmtListNode stmtListNode = new StmtListNode(0);
	int old_stackpointer = stack.getStackPointer();
	
	boolean succ = true;
	int numChildren = 0;


	while(succ==true){
	    succ = parseStmt();
		
	    if(succ)
		{
		   
		    stack.incStackPointer();
		    numChildren++;
		}
	}

	//System.out.println("stmt-list:::::::, stackpointer: " + stack.getStackPointer() + " stack: " + stack);

	if(numChildren != 0){
	    
	    stmtListNode = new StmtListNode(numChildren); // this line was modified in SemAnl stage because array need only be as long as the number of children of stmtList
	    
	    for(int i = numChildren-1; i >= 0;i--){
		Object temp = stack.remove();
		if(! (temp instanceof StatementNode)){
		    errorHandler(old_stackpointer, STMTLIST, "Statement");
		    return false;
		}

		stmtListNode.children[i]=(StatementNode)temp;
	    }
	    
	    stmtListNode.linenumber=stmtListNode.children[0].linenumber;

	    stack.insert(stmtListNode);
	    return true;
	}
	else{
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
    } // end of parseStmtList()

  
    // might be some modifying required for the below function
    // for now, StatementNode is an abstract class. This is the only
    // function right now that doesn't make anything
    private boolean parseStmt(){
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;

	//Object temp=stack.getNext();
	
	//System.out.println("parseStmt:::: stackPointer: " + stack.getStackPointer() + " stack: " + stack);
	succ = parseExprStmt();
	if(!succ)
	    succ = parseCompStmt();
	if(!succ)
	    succ = parseIfStmt();
	if(!succ)
	  succ = parseWhileStmt();
	if(!succ)
	    succ = parseForStmt();
	if(!succ)
	    succ = parseReturnStmt();
	if(succ)
	    return true;
	else {

	    // new code added in phase 3
	    //---------------------------------
	    Object temp = stack.getNext();
	    if ((temp instanceof Token) && (((Token)temp).symbolNumber== 34)){
		stack.remove();//remove the ;
		stack.insert(new ExprStmtNode(0));
		return true;
	    }
	    else
		stack.decStackPointer();	
	    //---------------------------------
	}
	    return false;
    }

    private boolean parseIfStmt(){
	IfStmtNode ifStmtNode = new IfStmtNode(0);
	int old_stackpointer = stack.getStackPointer();

	boolean succ = true;
	
	//System.out.println("in if-stmt");
	Object temp = stack.getNext();
	if(! ((temp instanceof Token) && (((Token)temp).symbolNumber == 3))){ // has to be an 'if'	    
	    stack.decStackPointer();
	    return false;
	}

	ifStmtNode.linenumber=((Token)temp).lineNumber;

	stack.decStackPointer();
	succ=parseIfStart();
	if(succ)
	    {
		stack.incStackPointer();
		succ=parseIfRem();
	    }
	else
	    errorHandler(old_stackpointer, IFSTMT, "if statement");
	
	
	if(succ){
	    //System.out.println("about to make ifStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	    stack.incStackPointer();
	    ifStmtNode = new IfStmtNode(2);
	    ifStmtNode.children[1] = (IfRemNode)stack.remove();
	    ifStmtNode.children[0] = (IfStartNode)stack.remove();
	    
	    stack.insert(ifStmtNode);
	    return true;
	}
	else {
	    //System.out.println("about to make ifStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	    ifStmtNode = new IfStmtNode(1);
	    ifStmtNode.children[0] = (IfStartNode)stack.remove();
	    
	    stack.insert(ifStmtNode);
	    return true;
	}	
	
    }//end of parseIfStmt()
    
    private boolean parseIfStart(){
	IfStartNode ifStartNode = new IfStartNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	
	//System.out.println("in if-start");

	temp = stack.getNext();
	
	if(!(temp instanceof Token)){
	    errorHandler(old_stackpointer,IFSTART, "if");
	    return false;
	}
	ifStartNode.linenumber=((Token)temp).lineNumber;

	if((((Token)temp).symbolNumber == 3)) // if it's "if"
	    temp=stack.getNext();
	else if( errorHandler(old_stackpointer,IFSTART, "if")) 
	    return parseIfStart();
	else
	    return false;

	if((((Token)temp).symbolNumber == 8)) // if it's "("
	    succ = parseExpression();
	else if( errorHandler(old_stackpointer,IFSTART, "(")) 
	    return parseIfStart();
	else
	    return false;
	    

	if(succ){
	    stack.incStackPointer();
	    temp=stack.getNext();
	}
	else {
	    errorHandler(old_stackpointer,IFSTART, "Expression");
	    return false;
	}
	
	//System.out.println("before parseStmt:::: stackpointer: " + stack.getStackPointer() + " stack: " + stack);	
	if((((Token)temp).symbolNumber == 9)) // if it's ")"
	    succ = parseStmt();
	else {
	    errorHandler(old_stackpointer,IFSTART, "Expression");
	    return false;
	}


	//System.out.println("finished parseStmt:::: stackpointer: " + stack.getStackPointer() + " stack: " + stack);	
	
	    
	
	if(succ){
	    stack.incStackPointer();
	    ifStartNode = new IfStartNode(3);
	    ifStartNode.children[2] = (StatementNode)stack.remove();
	    stack.remove();
	    ifStartNode.children[1] = (ExpressionNode)stack.remove();
	    stack.remove();
	    ifStartNode.children[0] = new IfNode((Token)stack.remove());
	    
	    stack.insert(ifStartNode);
	    return true;
	}
	else{
	    errorHandler(old_stackpointer, IFSTART, "statement");
	    return false;
	}
	
    }
    
    private boolean parseIfRem(){
	IfRemNode ifRemNode = new IfRemNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();	
	boolean succ = false;

	temp = stack.getNext();
	
	if(!(temp instanceof Token)){	    
	    return false;
	}
	    
	ifRemNode.linenumber=((Token)temp).lineNumber;

	if((((Token)temp).symbolNumber == 4)) // if it's "else"
	    succ=parseStmt();
	else {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}

	if(succ){
	    //System.out.println("bout to make ifRemNode, stackpointer: " + stack.getStackPointer() + " stack: " + stack);

	    stack.incStackPointer();
	    ifRemNode = new IfRemNode(2);
	    ifRemNode.children[1] = (StatementNode)stack.remove();
	    ifRemNode.children[0] = new ElseNode((Token)stack.remove());
	    stack.insert(ifRemNode);
	    return true;
	}
	else{
	    errorHandler(old_stackpointer, IFREM, "statement");
	    return false;
	}
    } 
    
    private boolean parseWhileStmt(){
	WhileStmtNode whileStmtNode = new WhileStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	
	temp = stack.getNext();

	if(!(temp instanceof Token))
	    return false;

	if((((Token)temp).symbolNumber == 5)) // if it's "while"
	    succ = parseExpression();
	else  {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}

	whileStmtNode.linenumber=((Token)temp).lineNumber;

	if(succ){
	    stack.incStackPointer();
	    succ = parseStmt();
	}
	else {
	    errorHandler(old_stackpointer,WHILESTMT,"Expression"); 
	    return false;
	}

	//System.out.println("bout to make WhileStmtNode, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	if(succ){
	    stack.incStackPointer();
	    whileStmtNode = new WhileStmtNode(3);
	    whileStmtNode.children[2] = (StatementNode)stack.remove();
	    whileStmtNode.children[1] = (ExpressionNode)stack.remove();
	    whileStmtNode.children[0] = new WhileNode((Token)stack.remove());
	    //System.out.println("tried to insert ifStartNode: " + ifStartNode);
	    stack.insert(whileStmtNode);
	    return true;
	}
	else{
	    errorHandler(old_stackpointer,WHILESTMT,"Statement"); 
	    return false;
	}
	
    }
    
    private boolean parseForStmt(){
	ForStmtNode forStmtNode = new ForStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	int numChildren = 0;
	boolean succ_expr = false;

	//System.out.println("starting forStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	temp = stack.getNext();

	if(!(temp instanceof Token)) {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}

	if((((Token)temp).symbolNumber == 6)) // if it's "for"
	    temp=stack.getNext();
	else {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}

	forStmtNode.linenumber=((Token)temp).lineNumber;

	if((((Token)temp).symbolNumber == 8)) // if it's "("
	    succ = parseExprStmt();
	else {
	    errorHandler(old_stackpointer, FORSTMT, "(");
	    return false;
	}

	if(succ){
	    stack.incStackPointer();
	    succ = parseExprStmt();
	}
	else{
	    errorHandler(old_stackpointer, FORSTMT, "expression or ;");
	    return false;
	}
	
	if(succ){
	    stack.incStackPointer();
	    succ = parseExpression();
	}
	else{
	    errorHandler(old_stackpointer, FORSTMT, "expression or ;");
	    return false;
	}

	if(succ){
	    succ_expr = true;
	    stack.incStackPointer();
	    temp=stack.getNext();
	}
	else {
	    temp=stack.getNext();
	}

	if((((Token)temp).symbolNumber == 9)) // if it's ")"
	    succ=parseStmt();
	else{
	    errorHandler(old_stackpointer, FORSTMT, ")");
	    return false;
	}
	    
	//System.out.println("finishing forStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	if(succ){
	    stack.incStackPointer();
	    if(succ_expr){
		forStmtNode = new ForStmtNode(5);
	    
		forStmtNode.children[4] = (StatementNode)stack.remove();
		stack.remove();
		forStmtNode.children[3] = (ExpressionNode)stack.remove();
	    }
	    else{
		forStmtNode = new ForStmtNode(4);
	    
		forStmtNode.children[3] = (StatementNode)stack.remove();
		stack.remove();
	    }
	    forStmtNode.children[2] = (ExprStmtNode)stack.remove();
	    forStmtNode.children[1] = (ExprStmtNode)stack.remove();
	    stack.remove();
	    forStmtNode.children[0] = new ForNode((Token)stack.remove());
	    
	    stack.insert(forStmtNode);
	    return true;
	}
	else{
	    errorHandler(old_stackpointer, FORSTMT, "Statement");
	    return false;
	}

    }
    


    // dummy parseReturnStmt for testing only - works only for stmt->return;
    private boolean parseReturnStmt(){
	//System.out.println("entered parseReturnStmt with stack \n" + stack);
	ReturnStmtNode returnStmtNode = new ReturnStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;

	temp = stack.getNext();

	if(!(temp instanceof Token)) {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}

	if((((Token)temp).symbolNumber == 7)) { // if it's "return"
	    //System.out.println("parsed return token ");
	    succ=parseExpression();       
	    if (succ)
		stack.incStackPointer();
	    //System.out.println("parseexpr succ? : " + succ);
	}
	else {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}

	returnStmtNode.linenumber=((Token)temp).lineNumber;

	//	stack.incStackPointer(); // IMP: This line was commented in phase 3 to take care of misparsing of return-stmt
	temp=stack.getNext();
	if(((Token)temp).symbolNumber == 34){ // if it's ";"
    
	    if (succ) {
		
		returnStmtNode = new ReturnStmtNode(1);
		returnStmtNode.linenumber=((Token)temp).lineNumber;

		stack.remove(); //get rid of the semic
		returnStmtNode.children[0] = ((Node)stack.remove());
		stack.remove(); //get rid of the return
		//System.out.println("successfully parsed return node: " + returnStmtNode);
		stack.insert(returnStmtNode);
		return true;
		
	    }
	    else {
		returnStmtNode = new ReturnStmtNode(0);
		returnStmtNode.linenumber=((Token)temp).lineNumber;

		stack.remove(); //get rid of the semic
		stack.remove(); //get rid of return
		//	System.out.println("successfully parsed return node: " + returnStmtNode);
		stack.insert(returnStmtNode);
		return true;
	    }
	}
	else {
	    errorHandler(old_stackpointer,RETURNSTMT,";");
	    return false;
	}

    }// end of parseReturnStmt()
    
       
    private boolean parseExprStmt(){
	ExprStmtNode exprStmtNode = new ExprStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();	
	boolean succ = true;
	
	succ=parseExpression();	
	if (succ)
	    stack.incStackPointer();
	
	temp=stack.getNext();
	
	if((temp instanceof Token) && ((Token)temp).symbolNumber == 34  ) // if it's ";"
	    temp=stack.remove(); // get rid of ";"
	else {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
		
	
	if(succ){
	    exprStmtNode = new ExprStmtNode(1);
	    exprStmtNode.children[0] = (ExpressionNode)stack.remove();
	    exprStmtNode.linenumber=exprStmtNode.children[0].linenumber;
	    stack.insert(exprStmtNode);
	    return true;
	}	
	else {
	    stack.insert(exprStmtNode);
	    return true;	    
	}
	

    } // end of parseExprStmt
    

  




    private boolean parseExpression(){
	ExpressionNode exprNode = new ExpressionNode(0);
	int old_stackpointer = stack.getStackPointer();	
	boolean succ = true;	
	boolean osucc = false;	
	boolean succ1 = false; //success as expr->or-expr
	boolean succ2 = false; //success as expr->or-expr assign-op expr
	Object temp;
	int numDecs=0;

	temp=stack.getNext();
	if (!(temp instanceof Token)){
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
	

	stack.setStackPointer(old_stackpointer);
	succ = parseOrExpr();
	stack.incStackPointer();
	osucc = succ;
	succ1=succ;
	if (succ)
	    numDecs++;	   
	

	temp=stack.getNext();
	if (osucc && (temp instanceof Token) && 
	    ((((Token)temp).symbolNumber == 27) ||  // if '+=' found  
	     (((Token)temp).symbolNumber == 28) || //  if '-=' found
	     (((Token)temp).symbolNumber == 35)) ) { //  if '=' found
	    numDecs++;   
	    succ=parseExpression();
	    if (succ){
		succ2=true;
		numDecs++;
		stack.incStackPointer();		
	    }
	    else {
		errorHandler(old_stackpointer, EXPR, "Or-Expression");
	    } 
		
	}
	else 
	    stack.decStackPointer();
	

	if (osucc){
	    if (succ2) {
		
		exprNode = new ExpressionNode(numDecs);
		temp = stack.remove();//remove the Expression
		exprNode.children[--numDecs] = (ExpressionNode)temp;
		
		
		temp = stack.remove();//remove the assignop
		exprNode.children[--numDecs] = new TerminalNode((OperatorToken)temp);
		
		
		temp = stack.remove();//remove the Or-expression
		exprNode.children[--numDecs] = (OrExprNode) temp;		
	    }
	    else if (succ1) {
		//		System.out.println("stack before parseexpr: " + stack);
		exprNode=new ExpressionNode(1);
		temp = stack.remove();//remove the Or-expression
	       
		exprNode.children[--numDecs] = (OrExprNode) temp;
	    }		
	    
	    exprNode.linenumber= exprNode.children[0].linenumber;
	    stack.insert(exprNode);
	    stack.setStackPointer(old_stackpointer);
	    return true;
	}
	else {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
	
    }
    
    
    private boolean parseOrExpr(){
	OrExprNode orExprNode = new OrExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;
	osucc=succ=parseAndExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	}
	
	while (succ){
	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && (((Token)temp).symbolNumber == 30)) {
		stack.remove();
		succ=parseAndExpr();
		if (succ){
		    stack.incStackPointer();
		    numDecs++;
		}
	    }
	    else {
		succ=false;
	    }	    
	}
	
	if (osucc){
	    stack.decStackPointer();
	    orExprNode = new OrExprNode(numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		orExprNode.children[i]=(AndExprNode)temp;		
	    }
	    orExprNode.linenumber= orExprNode.children[0].linenumber;
	    stack.insert(orExprNode);
	    return true;
	}
	stack.setStackPointer(old_stackpointer);
	return false;		
    }
    

    private boolean parseAndExpr(){
	AndExprNode andExprNode = new AndExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;

	osucc=succ=parseRelExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	}
	
	while (succ){   
	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && (((Token)temp).symbolNumber == 29)) { // &&
		stack.remove();
		succ=parseRelExpr();
		if (succ){
		    stack.incStackPointer();
		    numDecs++;
		}
	    }
	    else {
		succ=false;
	    }
	    
	}

	if (osucc){
	    stack.decStackPointer();
	    andExprNode = new AndExprNode(numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		andExprNode.children[i]=(RelExprNode)temp;		
	    }

	    andExprNode.linenumber= andExprNode.children[0].linenumber;

	    stack.insert(andExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }

    

    private boolean parseRelExpr(){
	RelExprNode relExprNode = new RelExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;
	osucc=succ=parseAddExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	}
	
	while (succ){
	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && 
		((((Token)temp).symbolNumber >= 21)  && //check for relational operators
		 (((Token)temp).symbolNumber <= 26) )) {
		succ=parseAddExpr();		
		if (succ){		
		    stack.incStackPointer();
		    numDecs +=2;
		}
		else {
		    errorHandler(old_stackpointer, RELEXPR, "Add-expression");
		    return false;
		}		    
	    }
	    else 
		succ=false;			    	    
	}
	
	if (osucc){
	    stack.decStackPointer();
	    relExprNode = new RelExprNode(numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		if (temp instanceof AddExprNode)
		    relExprNode.children[i]=(AddExprNode)temp;		
		else if (temp instanceof Token)
		    relExprNode.children[i]=new TerminalNode((Token)temp);		
		else {
		    errorHandler(old_stackpointer, RELEXPR, "add-expression or relational operators");
		    return false;		    
		}
		    
	    }

	    relExprNode.linenumber= relExprNode.children[0].linenumber;
	    stack.insert(relExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }


    private boolean parseAddExpr(){
	AddExprNode addExprNode = new AddExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;
	osucc=succ=parseTerm();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	}
	else{
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
	    
	
	while (succ){
	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && 
		((((Token)temp).symbolNumber == 20)  ||
		 (((Token)temp).symbolNumber == 19) )) {
		succ=parseTerm();		
		if (succ){		
		    stack.incStackPointer();
		    numDecs +=2;
		}
		else{
		    errorHandler(old_stackpointer,ADDEXPR,"term");
		    return false;
		}		    
	    }
	    else 
		succ=false;			    	    
	}
	
	if (osucc){
	    stack.decStackPointer();
	    addExprNode = new AddExprNode(numDecs);

	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		if (temp instanceof TermNode)
		    addExprNode.children[i]=(TermNode)temp;		
		else if (temp instanceof Token)
		    addExprNode.children[i]=new TerminalNode((Token)temp);		
		else {
		    errorHandler(old_stackpointer, ADDEXPR, "Term or +/-");
		    return false;
		}
		    
	    }

	    addExprNode.linenumber= addExprNode.children[0].linenumber;
	    stack.insert(addExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }
    


    private boolean parseTerm(){
	TermNode termNode = new TermNode(0);
	int old_stackpointer = stack.getStackPointer();

	boolean succ = true;
	boolean osucc=false;
	int numDecs=0;
	Object temp;

	osucc=succ=parseUnaryExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	}
	
	while (succ){
	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && 
		((((Token)temp).symbolNumber == 16)  ||
		 (((Token)temp).symbolNumber == 18) ||
		 (((Token)temp).symbolNumber == 14) )) {
		succ=parseUnaryExpr();		
		if (succ){		
		    stack.incStackPointer();
		    numDecs +=2;
		}
		else{
		    errorHandler(old_stackpointer, TERM, "unary-expr");
		}	    		
	    }
	    else 
		succ=false;			    	    
	}
	
	if (osucc){
	    stack.decStackPointer();
	    termNode = new TermNode(numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		if (temp instanceof UnaryExprNode)
		    termNode.children[i]=(UnaryExprNode)temp;		
		else if (temp instanceof Token)
		    termNode.children[i]=new TerminalNode((Token)temp);		
		else { 
		    errorHandler(old_stackpointer, TERM, "unary-expr or */%/'/'");
		}		    
	    }

	    termNode.linenumber= termNode.children[0].linenumber;
	    stack.insert(termNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }

    private boolean parseUnaryExpr(){
	UnaryExprNode unaryExprNode = new UnaryExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	boolean succ1 = true;
	boolean succ2 = true;

	int numDecs=0;
	Object temp;
	if (!(succ1=parsePrimaryExpr())) {
	    temp=stack.getNext();		
	    succ2= ((temp instanceof Token) && 
		    ((((Token)temp).symbolNumber == 19) || 
		     (((Token)temp).symbolNumber == 20) || 
		     (((Token)temp).symbolNumber == 17) || 
		     (((Token)temp).symbolNumber == 15) || 
		     (((Token)temp).symbolNumber == 14) )) ;
	}

	if (succ2){
	    unaryExprNode = new UnaryExprNode(2);
	    succ = parseUnaryExpr();
	    if (succ){
		stack.incStackPointer();
		temp=stack.remove();
		if (temp instanceof UnaryExprNode)
		    unaryExprNode.children[1]=(Node)temp;
		else {
		    errorHandler(old_stackpointer,UNARYEXPR,"Unary-expr");
		    return false;
		}
		temp=stack.remove();
		if (temp instanceof Token)
		    unaryExprNode.children[0]=new TerminalNode((Token)temp);
		else {
		    errorHandler(old_stackpointer,UNARYEXPR,"unary operator");
		    return false;
		}
		unaryExprNode.linenumber= unaryExprNode.children[1].linenumber;
		stack.insert(unaryExprNode);
		return true;
	    }
	}

	if (succ1){
	    unaryExprNode = new UnaryExprNode(1);
	    stack.incStackPointer();
	    temp=stack.remove();
	    if (temp instanceof PrimaryExprNode)
		unaryExprNode.children[0]=(Node)temp;
	    else {
		errorHandler(old_stackpointer,UNARYEXPR,"Primary-expr");
		return false;
	    }

	    unaryExprNode.linenumber= unaryExprNode.children[0].linenumber;
	    stack.insert(unaryExprNode);
	    return true;
	}
		
	stack.setStackPointer(old_stackpointer);
	return false;		
    }



    private boolean parsePrimaryExpr(){
	PrimaryExprNode primaryExprNode = new PrimaryExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean succ = false, succ1=false, succ2=false;
	boolean succ3=false, succ4=false, succ21=false, succ22=false, succ23=false ;

	int numDecs=0;
	Object temp;
	temp=stack.getNext();	
	succ1= (temp instanceof Token) && ((Token)temp).symbolNumber == 8;//detect a (
	succ2= (temp instanceof IdentifierToken);
	succ3= (temp instanceof IntegerToken);
	succ4= (temp instanceof FloatToken);

	if (temp instanceof Token)
	    primaryExprNode.linenumber=((Token)temp).lineNumber;
	else 
	    primaryExprNode.linenumber=((Node)temp).linenumber;

	if (succ1){
	    succ=parseExpression();
	    if (succ){
		stack.incStackPointer();
		temp=stack.getNext();
		succ1= (temp instanceof Token) && ((Token)temp).symbolNumber == 9; // detect a )
	    }
	    else {
		errorHandler(old_stackpointer,PRIMARYEXPR,"expression");
		return false;
	    }
	    if (succ1){
		primaryExprNode = new PrimaryExprNode(1);
		stack.remove();//remove )
		temp=stack.remove();
		stack.remove();//remove (
		if (temp instanceof Node)
		    primaryExprNode.children[0]=(Node)temp;
		else {
		    errorHandler(old_stackpointer, PRIMARYEXPR, "primaryexpr node");
		    return false;
		} 		    
		primaryExprNode.linenumber=((Node)temp).linenumber;
		stack.insert(primaryExprNode);
		return true;
	    }
	    else {
		errorHandler(old_stackpointer,PRIMARYEXPR,")");
		return false;
	    }			 	    
	}
	
	if (succ2){
	    temp=stack.getNext();
	    succ21= (temp instanceof Token) && ((Token)temp).symbolNumber == 10; // detect a [
	    succ22= (temp instanceof Token) && ((Token)temp).symbolNumber == 8; // detect a (
	    succ23= !(succ21 || succ22);

	    if (succ21){
		succ = parseExpression();
		if (succ){
		    stack.incStackPointer();
		    temp=stack.getNext();
		    succ21= (temp instanceof Token) && ((Token)temp).symbolNumber == 11;//detect a ]
		}
		else  {
		    errorHandler(old_stackpointer,PRIMARYEXPR,"expression");
		    return false;
		}		
		if (succ21){
		    primaryExprNode = new PrimaryExprNode(4);
		    temp=stack.remove();//remove and add a ]
		    primaryExprNode.children[3]=new TerminalNode((Token)temp);
		    temp=stack.remove();//remove and add an Expression Node
		    primaryExprNode.children[2]=(Node)temp;
		    temp=stack.remove();//remove and add a [
		    primaryExprNode.children[1]=new TerminalNode((Token)temp);
		    temp=stack.remove();//remove and add a identifier
		    primaryExprNode.children[0]=new IdentifierNode((IdentifierToken)temp );

		    primaryExprNode.linenumber=((Token)temp).lineNumber;
		    stack.insert(primaryExprNode);
		    return true;
		}
		else  {
		    errorHandler(old_stackpointer,PRIMARYEXPR,"]");
		    return false;
		}
	    }
	    if (succ22){
		succ = parseArgsExpr();
		if (succ){
		    stack.incStackPointer();
		    temp=stack.getNext();
		    succ22= (temp instanceof Token) && ((Token)temp).symbolNumber == 9;//detect a )
		}
		else  {
		    errorHandler(old_stackpointer,PRIMARYEXPR,"]");
		    return false;
		}

		if (succ22){
		    primaryExprNode = new PrimaryExprNode(4);
		    temp=stack.remove();//remove and add a ]
		    primaryExprNode.children[3]=new TerminalNode((Token)temp);
		    temp=stack.remove();//remove and add an Expression Node
		    primaryExprNode.children[2]=(Node)temp;
		    temp=stack.remove();//remove and add a [
		    primaryExprNode.children[1]=new TerminalNode((Token)temp);
		    temp=stack.remove();//remove and add a identifier
		    primaryExprNode.children[0]=new IdentifierNode((IdentifierToken)temp);
		    primaryExprNode.linenumber=((Token)temp).lineNumber;
		    stack.insert(primaryExprNode);
		    return true;
		}
		else  {
		    errorHandler(old_stackpointer,PRIMARYEXPR,"]");
		    return false;
		}

	    }
	    if (succ23){
		stack.decStackPointer();
		primaryExprNode = new PrimaryExprNode(1);
		temp=stack.remove();//remove and add a identifier
		primaryExprNode.children[0]=new IdentifierNode((IdentifierToken)temp);
		primaryExprNode.linenumber=((Token)temp).lineNumber;
		stack.insert(primaryExprNode);
		return true;		
	    }	
	    	    
	}
    
	
	if (succ3){
	    primaryExprNode = new PrimaryExprNode(1);
	    temp=stack.remove();//remove and add a int number
	    primaryExprNode.children[0]=new NumNode((IntegerToken)temp);
	    primaryExprNode.linenumber=((Token)temp).lineNumber;

	    stack.insert(primaryExprNode);
	    return true;	    
	}
	
	if (succ4){
	    primaryExprNode = new PrimaryExprNode(1);
	    temp=stack.remove();//remove and add a float number
	    primaryExprNode.children[0]=new RealNode((FloatToken)temp);
	    primaryExprNode.linenumber=((Token)temp).lineNumber;

	    stack.insert(primaryExprNode);
	    return true;	    
	}
	
	stack.setStackPointer(old_stackpointer);
	return false;		
    } 



    private boolean parseArgsExpr(){
	ArgsExprNode argsExprNode = new ArgsExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;

	//new code added in phase 3
	//------------------------------------ 
	temp=stack.getNext();
	stack.setStackPointer(old_stackpointer);
	if ((temp instanceof Token) && (((Token)temp).symbolNumber == 9)){ //checking for a ')'	    
	    stack.insert(argsExprNode);
	    return true;
	}
	//------------------------------------ 

	osucc=succ=parseExpression();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	}
	else   {
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
	
	while (succ){
	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && (((Token)temp).symbolNumber == 33)) { //checking for a ','
		stack.remove();
		succ=parseExpression();
		if (succ){
		    stack.incStackPointer();
		    numDecs++;
		}
		else{
		    errorHandler(old_stackpointer,ARGS,"expression");
		}		    
	    }
	    else {
		succ=false;
	    }
	    
	}
	
	if (osucc){
	    stack.decStackPointer();
	    argsExprNode = new ArgsExprNode(numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		argsExprNode.children[i]=(ExpressionNode)temp;
		//		argsExprNode.lexemeValue += ((Node)temp).lexemeValue  + ",";
	    }

	    argsExprNode.linenumber = argsExprNode.children[0].linenumber;
	    
	    stack.insert(argsExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    } // end of parseArgs()

    public ArrayList getErrors(){
	return errors;
    }

} // end of class Parser
