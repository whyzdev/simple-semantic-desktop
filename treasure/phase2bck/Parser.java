import java.util.*;

/* Akshat Singhal - Chris Fry */
/* Parser.java */

public class Parser
{
    
    private boolean diag1=false, diag2=false, diag3=false, diag4=false, diag12=false;
    private boolean diag5=false, diag8=false, diag9=false, diag10=false, diag11=false;
    private boolean diag13=false,diag14=false, diag15=false, diag16=false;
    private ParseStack stack;
    private int state;

  
    public Parser(Lexer lexer){
	stack = new ParseStack(lexer);
    }

    public Node parse()
    {
	state = 0; //Very Initial state.
	
	boolean succ=parseProgram();	
	if (succ) {
	    state = 1; // successfully parsed program
	    if (diag3){
		System.out.println("size: " + stack.getSize());
		System.out.println("stackpointer: " + stack.getStackPointer());
		System.out.println("top of stack is: " + stack.top());
	    }
	    ProgramNode progNode = (ProgramNode)stack.pop();
	    return progNode;
	}
	else { 
	    errorHandler();
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
			state=2; // successfully parsed a function or variable declaration
			stack.incStackPointer();
			numDecs++;
			
			if (diag5)
			    System.out.println("-Success! numDecs:" + numDecs);
		    }
		
	    }


	
	if(numDecs != 0){
	    progNode = new ProgramNode(numDecs);/*so we don't need a dynamic array*/
	   	    
	    for(int i = numDecs-1; i >= 0;i--){
		if (diag12){
		    System.out.println("%% numDecs: "+numDecs);
		    System.out.println("%% stackpointer before parseProgram remove: " + stack.getStackPointer());
		    System.out.println("%% stack before parseProgram remove: " + stack);

		}
		if (stack.get(stack.getStackPointer()-1) instanceof Node)
		    progNode.children[i]=(Node)stack.remove();
		else 
		    if (diag14)
			System.out.println("error parsing " + stack.remove());
	    }

	}

	if (diag9) {
	    System.out.println("%% stackpointer before EOFTOken remove: " + stack.getStackPointer());
	    System.out.println("%% stack before EOFTOken remove: " + stack);
	    System.out.println("%% stack size before EOFTOken remove: " + stack.getSize());
	}

	if (stack.getNext() instanceof EOFToken)
	    {//NIRVANA:reaching EOF at the end of a valid program
		state = 4; // successfully reached EOF after an empty/valid program
		stack.insert(progNode);
		return true;
	    }
	
	//something else but not EOF after a valid Program
	//don't stick on the correct program parsed so far,
	// just go to errorHandler()
	errorHandler();
	return false;
	    
	
	// }
    }
    
    private boolean errorHandler(){
	if (state != 14)
	    System.err.println("Oops! In the generic error handler at state: " + state);
	return true;
    }



    private boolean parseVarDecl()
    {
	VarDeclNode varNode;
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	state=11;//parsevardecl started
	temp = stack.getNext();
	if(temp instanceof EOFToken)
	    {
		stack.setStackPointer(old_stackpointer);
		return false;
	    }
	state=12;//parsevardecl found a non-eof
	if(!(temp instanceof Token)) {

	    cont = errorHandler();
	}


	if(cont && (((Token)temp).symbolNumber == 0 || ((Token)temp).symbolNumber == 1)) {//If token is an int or float
	    state=13;//parsevardecl found an int or float token
	    temp=stack.getNext();

	    
	    if(cont && (temp instanceof IdentifierToken)){ //If token is an identifier 
		state=14;//parsevardecl - found identifier
		temp=stack.getNext();
		
		if(cont && (((Token)temp).symbolNumber == 34)){ //If token is a ";"
		    state=15;//parsevardecl - found ;
		    
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
		    
		    stack.insert(varNode);
		    return true;
		}
		else if(cont && (((Token)temp).symbolNumber == 10)) //If token is a "["
		    temp=stack.getNext();
		else
		    cont = errorHandler();
		
		if(cont && (temp instanceof IntegerToken)) //if token is an integer
		    temp=stack.getNext();
		else
		    cont = errorHandler();
		
		if(cont && (((Token)temp).symbolNumber == 11)) //if token is an "]"
		    temp=stack.getNext();
		else
		    cont = errorHandler();
		
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
		    
		    stack.insert(varNode);
		    return true;
		}					
		else
		    cont = errorHandler();
	    }			  
	    else if(cont && (((Token)temp).symbolNumber == 14)) //if token is a "*"
		temp=stack.getNext();
	    else
		cont = errorHandler();
	    
	    if(cont && (temp instanceof IdentifierToken)) //if token is an identifier
		temp=stack.getNext();
	    else
		cont = errorHandler();
	    
	    if(cont && (((Token)temp).symbolNumber == 34)) {//if token is a ";"
		
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
		
		stack.insert(varNode);
		return true;
	    }
	}
	else if(cont && (((Token)temp).symbolNumber == 2)){// void
	    temp=stack.getNext();

	    if(cont && (temp instanceof IdentifierToken))
		temp=stack.getNext();
	    else
		cont = errorHandler();

	    if(cont && (((Token)temp).symbolNumber == 34)){//if token is a ";"

		varNode = new VarDeclNode(2);
		
		temp=stack.remove(); //get rid of the semic
		temp=stack.remove(); //get the id
		varNode.children[1]= new IdentifierNode((Token)temp);
		temp=stack.remove(); //get the type
		varNode.children[0]= new VoidNode((Token)temp);
	
		stack.insert(varNode);
		return true;
	    }
	}
	else
	    cont = errorHandler();				
	    
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

	FunDeclNode funNode = new FunDeclNode(0);
	Object temp;
	boolean succ = false;
	boolean cont = true;
	int old_stackpointer = stack.getStackPointer();
	
	//System.out.println("stack size = " + stack.getSize() + "stackPointer: " + stack.getStackPointer());	
	temp=stack.getNext();
	state=5;//starting to parse a function
	if(!(temp instanceof Token))
	    cont = errorHandler();
	
	if ((cont) && (temp instanceof EOFToken))
	    {
		stack.setStackPointer(old_stackpointer);
		return false;
	    }
	state=5;//parsefuncdel() - first item on stack is a valid token
	    
	if (cont && (((Token)temp).symbolNumber == 0 || 
		     ((Token)temp).symbolNumber == 1 || ((Token)temp).symbolNumber == 2)) //if token is int, float, or void
	    {
		temp=stack.getNext();
		state=6;//parsefuncdel() - first item on stack is a valid int/float/void
	    }
	else
	    cont = errorHandler();	


	if(cont && (temp instanceof IdentifierToken)) //If token is an identifier	
	    {
		//temp=stack.getNext();
		state=6;//parsefuncdel() - next item on stack is an identifier
		state=7;//parsefuncdel() - next item on stack is a (
		//System.out.println("going to parseParams, stack: " + stack.toString() + " stackpointer= " + stack.getStackPointer());
		succ = parseParams(); //try to parse next tokens as params
		//stack.incStackPointer();
		
		if (diag8)
		    System.out.println("Incrememented stack pointer to : " + stack.getStackPointer());
	    }
	else
	    cont = errorHandler();
	
	if(cont && succ){
	    state=8;//parsefuncdel() - parsed params successfully
	    //	    temp=stack.getNext();
 	    state=9;//parsefuncdel() - next item on stack is a ")"
	    if (diag10){
		System.out.println("stack before parseCompStmt() - " + stack + "\n stackpointer: " + stack.getStackPointer());
	    }
	    succ = parseCompStmt(); //try to parse next tokens as a compd statement
	    //	    stack.incStackPointer();
	    if (diag10)
		System.out.println("stack after parseCompStmt() - " + stack + "\n stackpointer: " + stack.getStackPointer());
	    stack.incStackPointer();
	}
	else
	    cont = errorHandler();

	if (succ && cont){
 	    state=10;//parsefuncdel() - parsed a compound statement successfully
	    funNode = new FunDeclNode(4);
	    temp=stack.remove();
	    if (diag10)
		System.out.println("tried to remove a compstmtnode, removed a " + temp);
	    funNode.children[3]= (CompStmtNode)temp;
		    
	 
	    temp=stack.remove(); // get the params
	    funNode.children[2]= (ParamsNode)temp;

	    temp=stack.remove(); // get the id
	    funNode.children[1]= new IdentifierNode((Token)temp);
	    temp=stack.remove(); // get the type
	    String string = temp.toString();
	    if(string.compareTo("int") == 0)
		funNode.children[0]=new IntNode((Token)temp);
	    else if(string.compareTo("float") == 0)
		funNode.children[0]=new FloatNode((Token)temp);
	    else
		funNode.children[0]=new VoidNode((Token)temp);
	    
	    stack.insert(funNode);
	    
	    if (diag5)  
		System.out.println(" created funNode:" + funNode);
	    if(diag5)
		System.out.println("stack is: " + stack);
	    
	    return true;	    
	}
   	    
	//if method hasn't returned TRUE yet, it means something failed, so return false.
	errorHandler();
	return false;
    
    } // end of parseFunDecl()
    
  
    private boolean parseParams(){
	boolean succ = true;
	boolean cont = true;
	boolean gotOne = false;
	int numParams = 0;
	ParamsNode paramsNode;
	Object temp;

	temp = stack.getNext();
	if (diag10)
	    System.out.println("temp = " + temp + " should be (");
	if(cont && (((Token)temp).symbolNumber == 8)) { //If token is a "("
	    stack.remove();	    
	    cont=true;
	} 

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

	if(cont && (((Token)temp).symbolNumber == 9)) { //If token is a ")"
	    stack.remove();	    
	    cont=true;
	} 

	


	
		
	if(gotOne && cont){
   	    paramsNode = new ParamsNode(numParams);	    
	    for(int i=numParams-1; i >= 0; i--){

	

		paramsNode.children[i]=(ParamNode)stack.remove();
	    }
	    stack.insert(paramsNode);
	    stack.incStackPointer();
	   
	    
	    return true;
	}

	
	return false;
    } // end of parseParams()
  
    /*    
    //Dummy parseParam() for testing
    private boolean parseParam(){
    System.out.println("in parseParam");
    stack.incStackPointer();
    stack.remove();
    stack.insert(new ParamNode(0));
    return true;
    }
    */
    
    private boolean parseParam(){
	ParamNode paramNode = new ParamNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;

	temp = stack.getNext();

	if(!(temp instanceof Token))
	    cont = errorHandler(); 
	    
	    
	if(cont && (((Token)temp).symbolNumber == 0 || ((Token)temp).symbolNumber == 1)){   
	    temp=stack.getNext();


	    if(cont && (temp instanceof IdentifierToken)){ //If token is an identifier 
		temp=stack.getNext();
	

		if(cont && (((Token)temp).symbolNumber == 10)){ // if it's "["
		    temp=stack.getNext();

			    
		    if(cont && (((Token)temp).symbolNumber == 11)){ // if it's "]"
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
			
			stack.insert(paramNode);
			return true;
		    }
		    else
			cont = errorHandler();
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
		    
		    stack.insert(paramNode);
		    return true;
		}
	    }
	    else if(cont && (((Token)temp).symbolNumber == 14)){ //if it's "*"
		    
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
						
		    stack.insert(paramNode);
		    return true;
		}
		else
		    cont = errorHandler();
	    }
	    else
		cont = errorHandler();
	}
	else if(cont && (((Token)temp).symbolNumber == 2))// void
	    temp = stack.getNext();
	else
	    cont = errorHandler();
	
	if(((Token)temp).symbolNumber == 14)// if it's "*"
	    temp=stack.getNext();
	else
	    cont = errorHandler();

	if(temp instanceof IdentifierToken){
	    paramNode = new ParamNode(3);

	    temp=stack.remove(); // get the id
	    paramNode.children[2]=new IdentifierNode((Token)temp);
	    temp=stack.remove(); //get the "*"
	    paramNode.children[1]=new TerminalNode((Token)temp);
	    temp=stack.remove(); //get the type
	    String string = temp.toString();
	    if(string.compareTo("int") == 0)
		paramNode.children[0]=new TerminalNode((Token)temp);
	    else
		paramNode.children[0]=new FloatNode((Token)temp);
	    
	    stack.insert(paramNode);
	    return true;
	}
	else
	    cont = errorHandler();
		
	stack.setStackPointer(old_stackpointer);
	return false;
    }// end of parseParam()
    
    
    private boolean parseCompStmt(){
	
	boolean succ = true;
	boolean cont = true;
	boolean ld = false; //does compound statement have local declarations?
	boolean sl = false; //does compound statement have a statement list?
	int old_stackpointer = stack.getStackPointer();
	CompStmtNode compStmtNode = new CompStmtNode(0);

	Object temp = stack.getNext();

	
	if(!(temp instanceof Token))
	    cont = errorHandler(); 
	
	if(cont && (((Token)temp).symbolNumber == 31)){ //if it's "{"
	    succ = parseLocalDecs();
	   
	}
	else {
	    //special intervention: will have to fail this function if something that is not a { is found
	    //or else everything becomes mad recursive.
	    stack.setStackPointer(old_stackpointer);
	    cont = errorHandler();
	    
	    return false;
	}
	
	
	if(cont && succ){
	    ld = true;
	    stack.incStackPointer();
	    succ = parseStmtList();
	   
	}
	else if(cont)
	    succ = parseStmtList();
	else
	    cont = errorHandler();
	
	if(cont && succ){
	    stack.incStackPointer();
	    temp = stack.getNext();
	    sl = true;
	}
	else if(cont){
	    temp = stack.getNext();
	}
	else
	    cont = errorHandler();


	
	if(cont && ( temp instanceof Token &&  ((Token)temp).symbolNumber == 32)){ //if it's "}"
	    compStmtNode = new CompStmtNode(2);

	    if(ld && sl){
		
		temp=stack.remove(); // remove the "}"
		temp=stack.remove(); //get the StmtList
		compStmtNode.children[1]= (StmtListNode)temp;
		temp=stack.remove(); // get the LocalDecs
		compStmtNode.children[0]= (LocalDecsNode)temp;
		temp=stack.remove(); //remove the "{"
	
		stack.insert(compStmtNode);	
		return true;
	    }
	    
	    if(sl){
		compStmtNode = new CompStmtNode(1);

		temp=stack.remove(); // remove the "}"
		temp=stack.remove(); //get the StmtList
		compStmtNode.children[0]= (StmtListNode)temp;
		temp=stack.remove(); // remove the "{"

		stack.insert(compStmtNode);	
		return true;
	    } 
	    
	    if(ld){
		compStmtNode = new CompStmtNode(1);

		temp=stack.remove(); // remove the "}"
		temp=stack.remove(); //get the LocalDecs
		compStmtNode.children[0]= (LocalDecsNode)temp;
		temp=stack.remove(); // remove the "{"

		stack.insert(compStmtNode);	
		return true;
	    }
	}   
	else 
	    cont = errorHandler();

	//may return false since there may be no localdecs or statementlist
	stack.setStackPointer(old_stackpointer);
	return false;

    } // end of parseCompStmt()

    
    private boolean parseLocalDecs(){
	LocalDecsNode localDecsNode = new LocalDecsNode(0);
	int old_stackpointer = stack.getStackPointer();

	boolean succ = true;
	boolean cont = true;
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
	StmtListNode stmtListNode = new StmtListNode(0);
	int old_stackpointer = stack.getStackPointer();
	
	boolean succ = true;
	int numChildren = 0;
	boolean cont=true;

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
	    
	    stmtListNode = new StmtListNode(numChildren+1);
	    
	    for(int i = numChildren-1; i >= 0;i--){
		stmtListNode.children[i]=(StatementNode)stack.remove();
	    }
	   
	    
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
	boolean cont=true;
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
	else
	    return false;
    }

    private boolean parseIfStmt(){
	IfStmtNode ifStmtNode = new IfStmtNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	
	//System.out.println("in if-stmt");
	Object temp = stack.getNext();
	if(! ((temp instanceof Token) && (((Token)temp).symbolNumber == 3))){ // has to be an 'if'
	    stack.decStackPointer();
	    return false;
	}

	stack.decStackPointer();
	succ=parseIfStart();
	if(succ)
	    {
		stack.incStackPointer();
		succ=parseIfRem();
	    }
	else
	    cont = errorHandler();
	
	
	if(succ && cont){
	    //System.out.println("about to make ifStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	    stack.incStackPointer();
	    ifStmtNode = new IfStmtNode(2);
	    ifStmtNode.children[1] = (IfRemNode)stack.remove();
	    ifStmtNode.children[0] = (IfStartNode)stack.remove();
	    
	    stack.insert(ifStmtNode);
	    return true;
	}
	else if(cont){
	    //System.out.println("about to make ifStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	    ifStmtNode = new IfStmtNode(1);
	    ifStmtNode.children[0] = (IfStartNode)stack.remove();
	    
	    stack.insert(ifStmtNode);
	    return true;
	}
	else
	    return false;
	
    }
    
    private boolean parseIfStart(){
	IfStartNode ifStartNode = new IfStartNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	
	//System.out.println("in if-start");

	temp = stack.getNext();
	
	if(!(temp instanceof Token))
	    cont = errorHandler();
	
	if(cont && (((Token)temp).symbolNumber == 3)) // if it's "if"
	    temp=stack.getNext();
	else
	    cont = errorHandler();
	
	if(cont && (((Token)temp).symbolNumber == 8)) // if it's "("
	    succ = parseExpression();
	else
	    cont = errorHandler();

	if(succ && cont){
	    stack.incStackPointer();
	    temp=stack.getNext();
	}
	else
	    cont = errorHandler();
	
	//System.out.println("before parseStmt:::: stackpointer: " + stack.getStackPointer() + " stack: " + stack);	
	if(cont && (((Token)temp).symbolNumber == 9)) // if it's ")"
	    succ = parseStmt();
	else
	    cont = errorHandler();

	//System.out.println("finished parseStmt:::: stackpointer: " + stack.getStackPointer() + " stack: " + stack);	
	
	    
	
	if(succ && cont){
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
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
	
    }
    
    private boolean parseIfRem(){
	IfRemNode ifRemNode = new IfRemNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = false;

	temp = stack.getNext();
	
	if(!(temp instanceof Token))
	    cont = errorHandler();
	
	if(cont && (((Token)temp).symbolNumber == 4)) // if it's "else"
	    succ=parseStmt();
	else
	    cont = errorHandler();

	if(cont && succ){
	    //System.out.println("bout to make ifRemNode, stackpointer: " + stack.getStackPointer() + " stack: " + stack);

	    stack.incStackPointer();
	    ifRemNode = new IfRemNode(2);
	    ifRemNode.children[1] = (StatementNode)stack.remove();
	    ifRemNode.children[0] = new ElseNode((Token)stack.remove());
	    stack.insert(ifRemNode);
	    return true;
	}
	else{
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
    } 
    
    private boolean parseWhileStmt(){
	WhileStmtNode whileStmtNode = new WhileStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	
	temp = stack.getNext();

	if(!(temp instanceof Token))
	    cont = errorHandler();

	if(cont && (((Token)temp).symbolNumber == 5)) // if it's "while"
	    succ = parseExpression();
	else{
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}


	
	if(succ && cont){
	    stack.incStackPointer();
	    succ = parseStmt();
	}
	else
	    cont = errorHandler();

	//System.out.println("bout to make WhileStmtNode, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	if(succ && cont){
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
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}
	
    }
    
    private boolean parseForStmt(){
	ForStmtNode forStmtNode = new ForStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	int numChildren = 0;
	boolean succ_expr = false;

	//System.out.println("starting forStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	temp = stack.getNext();

	if(!(temp instanceof Token))
	    cont = errorHandler();

	if(cont && (((Token)temp).symbolNumber == 6)) // if it's "for"
	    temp=stack.getNext();
	else{
	    stack.setStackPointer(old_stackpointer);
	    return false;
	}

	if(cont && (((Token)temp).symbolNumber == 8)) // if it's "("
	    succ = parseExprStmt();
	else
	    cont = errorHandler();

	if(succ && cont){
	    stack.incStackPointer();
	    succ = parseExprStmt();
	}
	
	if(succ && cont){
	    stack.incStackPointer();
	    succ = parseExpression();
	}

	if(succ && cont){
	    succ_expr = true;
	    stack.incStackPointer();
	    temp=stack.getNext();
	}
	else if(cont){
	    temp=stack.getNext();
	}

	if(cont && (((Token)temp).symbolNumber == 9)) // if it's ")"
	    succ=parseStmt();
	else
	    cont = errorHandler();
	//System.out.println("finishing forStmt, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	if(succ && cont){
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
	    cont = errorHandler();
	    if(cont){
		stack.setStackPointer(old_stackpointer);
		return false;
	    }
	}
	//shouldn't get here
	System.out.println("Something is horribly wrong!! Probably won't get a good parse...");
	return false;

    }
    


    // dummy parseReturnStmt for testing only - works only for stmt->return;
    private boolean parseReturnStmt(){
	ReturnStmtNode returnStmtNode = new ReturnStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc = true;

	if (diag12)
	    System.out.println("entered parseReturnstmt");	

	temp = stack.getNext();

	if (diag12)
	    System.out.println("next parsed token(should be return)="+ temp);

	if(!(temp instanceof Token)) {
	    cont = errorHandler();
	    osucc=false;
	}

	if(cont && (((Token)temp).symbolNumber == 7)) { // if it's "return"
	    if (diag16)
		System.out.println("#$ found return. current stack: " + stack + "\n stackpointer: "+stack.getStackPointer());
	    succ=parseExpression();       
	    if (diag16)
		System.out.println("#$ after parseExpression() in return. current stack: " + stack + "\n stackpointer: "+stack.getStackPointer());
	    stack.incStackPointer();
	}
	else {
	    cont = errorHandler();
	    osucc=false;
	}
	
	temp=stack.getNext();
	if(osucc && cont && (((Token)temp).symbolNumber == 34)){ // if it's ";"
	    if(diag16)
		System.out.println(">> >> end of returnstmt: " + temp);
	    
	    if (succ) {
		returnStmtNode = new ReturnStmtNode(1);
		stack.remove(); //get rid of the semic
		returnStmtNode.children[0] = ((Node)stack.remove());
		stack.remove(); //get rid of the return
		if (diag12)
		    System.out.println("tried to insert returnNode "+ returnStmtNode);
		stack.insert(returnStmtNode);
		return true;
		
	    }
	    else {
		returnStmtNode = new ReturnStmtNode(0);
		
		stack.remove(); //get rid of the semic
		stack.remove(); //get rid of return
		if (diag12)
		System.out.println("tried to insert returnNode "+ returnStmtNode);
		stack.insert(returnStmtNode);
		return true;
	    }
	}

	stack.setStackPointer(old_stackpointer);
	return false;	
    }
    

  
	
	

    
    private boolean parseExprStmt(){
	ExprStmtNode exprStmtNode = new ExprStmtNode(0);
	Object temp;
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc=true;
	//boolean osucc = true; // osucc = Overall Success of the function.
	
	//System.out.println("parseExprStmt::::: stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	succ=parseExpression();	
	if (succ)
	    stack.incStackPointer();
	
	temp=stack.getNext();
	/*
	if(!(temp instanceof Token))
	    cont = errorHandler();
	*/
	if( cont &&  (temp instanceof Token) && ((Token)temp).symbolNumber == 34  ) // if it's ";"
	    temp=stack.remove(); // get rid of ";"
	else {
	    cont = errorHandler();
	    osucc=false;
	}
	
	
	//System.out.println("bout to go horribly wrong::::: stackpointer: " + stack.getStackPointer() + " stack: " + stack);	
	
	if(succ && cont){
	    exprStmtNode = new ExprStmtNode(1);
	    exprStmtNode.children[0] = (ExpressionNode)stack.remove();
	    stack.insert(exprStmtNode);
	    return true;
	}	
	
	if(cont && osucc){ 
	    stack.insert(exprStmtNode);
	    return true;
	}
	
	stack.setStackPointer(old_stackpointer);
	//System.out.println("parseExprStmt::::: stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	return false;	
    } // end of parseExprStmt
    

  




    private boolean parseExpression(){
	ExpressionNode exprNode = new ExpressionNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc = true;
	boolean succ1 = false; //success as expr->or-expr
	boolean succ2 = false; //success as expr->or-expr assign-op expr
	Object temp;
	int numDecs=0;

	temp=stack.getNext();
	if (!(temp instanceof Token)){
	    osucc=false;
	    cont=errorHandler();
	}
	
	if (osucc) {
	    stack.setStackPointer(old_stackpointer);
	    succ = parseOrExpr();
	    stack.incStackPointer();
	    osucc = succ;
	    succ1=osucc;
	    if (succ)
		numDecs++;
	    
	    //System.out.println("successfully parsed Or-expr in expr");
	}

	temp=stack.getNext();
	if (osucc && (temp instanceof Token) && 
	    ((((Token)temp).symbolNumber == 27) || 
	     (((Token)temp).symbolNumber == 28) ||
	     (((Token)temp).symbolNumber == 35)) ) {	    
	    numDecs++;   
	    succ=parseExpression();
	    if (succ){
		succ2=true;
		numDecs++;
		stack.incStackPointer();		
	    }
	    else 
		osucc=false;
	}
	else {
	    stack.decStackPointer();
	}

	if (osucc){
	    if (succ2) {
		try {
		    exprNode = new ExpressionNode(numDecs);
		    temp = stack.remove();//remove the Expression
		    if(diag13) 
			System.out.println("%% expected ExpressionNode, got " + temp );
		    exprNode.children[--numDecs] = (ExpressionNode)temp;


		    temp = stack.remove();//remove the assignop
		    if(diag13)
			System.out.println("%% expected assignop, got " + temp );
		    exprNode.children[--numDecs] = new TerminalNode((OperatorToken)temp);


		    temp = stack.remove();//remove the Or-expression

		    if(diag13)
			System.out.println("%% expected or-expr, got " + temp);
		    exprNode.children[--numDecs] = (OrExprNode) temp;
		}
		catch (Exception e) {
		    if (e instanceof ClassCastException){
			System.err.println("Parser failed parsing " + temp + "\nstack:" + stack);
		    }
		    if ((e instanceof NullPointerException) || (e instanceof ArrayIndexOutOfBoundsException)){
			System.err.println("Parser failed parsing due to stack reference error at:  " + temp  + "\nstack:" + stack);
		    }

		    
		}
	    }
	    else if (succ1) {
		try {
		    exprNode=new ExpressionNode(1);
		    temp = stack.remove();//remove the Or-expression
		    if(diag13)
			System.out.println("% % expected OrExprNode, got " + temp);
		    exprNode.children[--numDecs] = (OrExprNode) temp;
		}
		catch (Exception e) {
		    if (e instanceof ClassCastException){
			System.err.println("Parser failed parsing " + temp + "\nstack:" + stack);
		    }
		    if ((e instanceof NullPointerException) || (e instanceof ArrayIndexOutOfBoundsException)){
			System.err.println("Parser failed parsing due to stack reference error at:  " + temp + "\nstack:" + stack);
		    }		    
		}
		
	    }
	    stack.insert(exprNode);
	    stack.setStackPointer(old_stackpointer);
	    return true;
	}
	
	stack.setStackPointer(old_stackpointer);
	return false;
	
    }

    
    private boolean parseOrExpr(){
	OrExprNode orExprNode = new OrExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;

	osucc=succ=parseAndExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	    osucc=true;
	}
	
	while (succ){
	    if (diag13)
		System.out.println("parsing loop in parseOrExpr");

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
	    stack.insert(orExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }
    

    private boolean parseAndExpr(){
	AndExprNode andExprNode = new AndExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;

	osucc=succ=parseRelExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	    osucc=true;
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
		//osucc=false;
		//stack.decStackPointer();
	    }
	    
	}
	//System.out.println("andExpr::::::::, stackpointer: " + stack.getStackPointer() + " stack: " + stack);	

	if (osucc){
	    //System.out.println("got here to make an andExpr, stackpointer: " + stack.getStackPointer() + " stack: " + stack);
	    stack.decStackPointer();
	    andExprNode = new AndExprNode(numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		andExprNode.children[i]=(RelExprNode)temp;		
	    }
	    stack.insert(andExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }

    

    private boolean parseRelExpr(){
	RelExprNode relExprNode = new RelExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;

	osucc=succ=parseAddExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	    osucc=true;
	}
	
	while (succ){
	    if (diag13)
		System.out.println("parsing loop in parseRelExpr");

	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && 
		((((Token)temp).symbolNumber >= 21)  &&
		 (((Token)temp).symbolNumber <= 26) )) {
		succ=parseAddExpr();		
		if (succ){		
		    stack.incStackPointer();
		    numDecs +=2;
		}

		if (diag14)
		    System.out.println("$$$ stack : " + stack + " \nSP:" + stack.getStackPointer());
		
	    }
	    else {
		succ=false;		

	    }
	    
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
		else if (diag13)
		    System.out.println("unrecognizable symbol : " +  temp);
		    
	    }
	    if (diag14)
		System.out.println("stack before insert:" + stack + "\n inserting new relExprNode : " + relExprNode);
	    stack.insert(relExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }


    private boolean parseAddExpr(){
	AddExprNode addExprNode = new AddExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;

	osucc=succ=parseTerm();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	    osucc=true;
	}
	
	while (succ){
	    if (diag13)
		System.out.println("parsing loop in parseAddExpr");

	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && 
		((((Token)temp).symbolNumber == 20)  ||
		 (((Token)temp).symbolNumber == 19) )) {
		succ=parseTerm();		
		if (succ){		
		    stack.incStackPointer();
		    numDecs +=2;
		}

		if (diag14)
		    System.out.println("$$$ stack : " + stack + " \nSP:" + stack.getStackPointer());
		
	    }
	    else {
		succ=false;		

	    }
	    
	}
	
	if (osucc){
	    stack.decStackPointer();
	    addExprNode = new AddExprNode(numDecs);
	    //System.out.println("about to insert:::::: stackpointer: " + stack.getStackPointer() + " numDecs: " + numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		if (temp instanceof TermNode)
		    addExprNode.children[i]=(TermNode)temp;		
		else if (temp instanceof Token)
		    addExprNode.children[i]=new TerminalNode((Token)temp);		
		else if (diag13)
		    System.out.println("unrecognizable symbol : " +  temp);
		    
	    }
	    if (diag14)
		System.out.println("stack before insert:" + stack + "\n inserting new addExprNode : " + addExprNode);
	    stack.insert(addExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }



    private boolean parseTerm(){
	TermNode termNode = new TermNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc=false;
	int numDecs=0;
	Object temp;

	osucc=succ=parseUnaryExpr();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	    osucc=true;
	}
	
	while (succ){
	    if (diag13)
		System.out.println("parsing loop in parseTerm");

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

		if (diag14)
		    System.out.println("$$$ stack : " + stack + " \nSP:" + stack.getStackPointer());
		
	    }
	    else {
		succ=false;		

	    }
	    
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
		else if (diag13)
		    System.out.println("unrecognizable symbol : " +  temp);
		    
	    }
	    if (diag14)
		System.out.println("stack before insert:" + stack + "\n inserting new termNode : " + termNode);
	    stack.insert(termNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }

    private boolean parseUnaryExpr(){
	if (diag14)
	    System.out.println("entered parseUnaryExpr");
	UnaryExprNode unaryExprNode = new UnaryExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean succ1 = true;
	boolean succ2 = true;

	int numDecs=0;
	Object temp;
	succ1=(parsePrimaryExpr());
	if (!succ1) {
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
		temp=stack.remove();
		if (temp instanceof Token)
		    unaryExprNode.children[0]=new TerminalNode((Token)temp);
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
	    stack.insert(unaryExprNode);
	    return true;
	}
	
	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }

    private boolean oldparsePrimaryExpr(){
	PrimaryExprNode primaryExprNode = new PrimaryExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc = true;
	int numDecs=0;
	Object temp;
	temp=stack.getNext();	
	try {	       
	    if (diag13)
		System.out.println("tried to parse " + (((Token)temp).toString()) + " as an primary-expr. " );
	    if (osucc && (temp instanceof IdentifierToken) && ((((Token)temp).toString()).equals("id(unaryexpr)"))) {
		temp=stack.remove();
		primaryExprNode= new PrimaryExprNode(1);
		primaryExprNode.children[0]=new IdentifierNode((Token)temp);
		stack.insert(primaryExprNode);
		if (diag13)
		    System.out.println("inserted an primary-expr node " + primaryExprNode);
		stack.setStackPointer(old_stackpointer);
		return true;	
	    }
	}
	catch(Exception e){
	    System.err.println("could not parse " + temp + " as an primary-expr");
	}
	
	stack.setStackPointer(old_stackpointer);
	return false;		
    } 


    private boolean parsePrimaryExpr(){
	PrimaryExprNode primaryExprNode = new PrimaryExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = false, succ1=false, succ2=false, succ3=false, succ4=false, succ21=false, succ22=false, succ23=false ;
	boolean osucc = false;
	
	if (diag15)
	    System.out.println("entered parsePrimaryExpr() with stack: " + stack);

	int numDecs=0;
	Object temp;
	temp=stack.getNext();	
	if (diag15)
	    System.out.println("1.) temp = " + temp);
	succ1= (temp instanceof Token) && ((Token)temp).symbolNumber == 8;//detect a (
	succ2= (temp instanceof IdentifierToken);
	succ3= (temp instanceof IntegerToken);
	succ4= (temp instanceof FloatToken);
	osucc=(succ1||succ2||succ3||succ4);
	if (diag15)
	    System.out.println("1.) succ2 = " + succ2);
	if (succ1){
	    succ=parseExpression();
	    if (succ){
		stack.incStackPointer();
		temp=stack.getNext();
		if (diag15)
		    System.out.println("2.) temp = " + temp);
		succ1= (temp instanceof Token) && ((Token)temp).symbolNumber == 9; // detect a )
	    }
	    if (succ1){
		primaryExprNode = new PrimaryExprNode(1);
		stack.remove();//remove )
		temp=stack.remove();
		if (diag15)
		    System.out.println("3.) temp = " + temp);
		stack.remove();//remove (
		primaryExprNode.children[0]=(Node)temp;
 		if (diag15)
		    System.out.println("tried to insert node " + primaryExprNode);
		stack.insert(primaryExprNode);
		return true;
	    }			 	    
	}
	
	if (succ2){
	    temp=stack.getNext();
	    if (diag15)
		System.out.println("4.) temp = " + temp);
	    succ21= (temp instanceof Token) && ((Token)temp).symbolNumber == 10; // detect a [
	    succ22= (temp instanceof Token) && ((Token)temp).symbolNumber == 8; // detect a (
	    succ23= !(succ21 || succ22);
	    if (succ21){
		if (diag15)
		    System.out.println("5.) temp = " + temp);
		succ = parseExpression();
		if (succ){
		    stack.incStackPointer();
		    temp=stack.getNext();
		    succ21= (temp instanceof Token) && ((Token)temp).symbolNumber == 11;//detect a ]
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
		    if (diag15)
			System.out.println("tried to insert node " + primaryExprNode);
		    stack.insert(primaryExprNode);
		    return true;
		}
	    }
	    if (succ22){
		succ = parseArgsExpr();
		if (succ){
		    stack.incStackPointer();
		    temp=stack.getNext();
		    succ22= (temp instanceof Token) && ((Token)temp).symbolNumber == 9;//detect a )
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
		    if (diag15)
			System.out.println("tried to insert node " + primaryExprNode);
		    stack.insert(primaryExprNode);
		    return true;
		}
	    }
	    if (succ23){
		stack.decStackPointer();
		primaryExprNode = new PrimaryExprNode(1);
		temp=stack.remove();//remove and add a identifier
		primaryExprNode.children[0]=new IdentifierNode((IdentifierToken)temp);
 		if (diag15)
		    System.out.println("tried to insert node " + primaryExprNode);
		stack.insert(primaryExprNode);
		return true;
		
	    }	
	    	    
	}
    
	
	if (succ3){
	    primaryExprNode = new PrimaryExprNode(1);
	    temp=stack.remove();//remove and add a identifier
	    primaryExprNode.children[0]=new NumNode((IntegerToken)temp);
	    if (diag15)
		System.out.println("tried to insert node " + primaryExprNode);
	    stack.insert(primaryExprNode);
	    return true;	    
	}
	
	if (succ4){
	    primaryExprNode = new PrimaryExprNode(1);
	    temp=stack.remove();//remove and add a identifier
	    primaryExprNode.children[0]=new RealNode((FloatToken)temp);
	    if (diag15)
		System.out.println("tried to insert node " + primaryExprNode);
	    stack.insert(primaryExprNode);
	    return true;	    
	}
	
	stack.setStackPointer(old_stackpointer);
	return false;		
    } 





    /* do not also forget how to handle comments, because comments can appear everywhere*/
    /* do not forget to modify parsereturnstmt*/
    private boolean parseArgsExpr(){
	ArgsExprNode argsExprNode = new ArgsExprNode(0);
	int old_stackpointer = stack.getStackPointer();
	boolean cont = true;
	boolean succ = true;
	boolean osucc=true;
	int numDecs=0;
	Object temp;

	osucc=succ=parseExpression();
	
	if (succ){
	    stack.incStackPointer();
	    numDecs++;
	    osucc=true;
	}
	
	while (succ){
	    if (diag13)
		System.out.println("parsing loop in parseArgsExpr");

	    temp=stack.getNext();
	    if (succ && (temp instanceof Token) && (((Token)temp).symbolNumber == 33)) {
		stack.remove();
		succ=parseExpression();
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
	    argsExprNode = new ArgsExprNode(numDecs);
	    for (int i=numDecs-1;i >=0 ; i--){		
		temp=stack.remove();
		argsExprNode.children[i]=(ExpressionNode)temp;
	    }
	    stack.insert(argsExprNode);
	    return true;
	}	
	stack.setStackPointer(old_stackpointer);
	return false;		
    }

    /*
      private boolean parseExpression(){
      Object temp = stack.getNext();
      stack.remove();
      stack.insert(new ExpressionNode(0));
      //System.out.println("parseCompStmt: " + stack.toString() + " stackpoint = " + stack.getStackPointer());
      return true;
	
      //return false;
      }
    */
    
} // end of class Parser
