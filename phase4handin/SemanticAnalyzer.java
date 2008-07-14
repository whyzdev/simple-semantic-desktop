import java.util.*;


/*
  Chris Fry and Akshat Singhal
  SemanticAnalyzer for the Akshat and Chris Compiler (ACC)
  performs semantic analysis of a parse tree. Returns true
  if successfull, false if not
*/

public class SemanticAnalyzer{
    Node parseTree;
    ArrayList errors;
    SymbolTable symbolTable = new SymbolTable();
    final int INTSIZE=4;
    final int POINTERSIZE=4;
    final int MINSTACKFRAMESIZE=112;

    public SemanticAnalyzer(Node parseTree){
	this.parseTree = parseTree;
	errors = new ArrayList();

    }

    public Node getAnnotatedTree(){
	return parseTree;
    }

    public ArrayList getErrors(){
	return errors;
    }

    public ArrayList checkProgram(){
	Node[] children = parseTree.getChildren();
	int numChildren = parseTree.getNumChildren();
	Context context = new Context();
	symbolTable.addContext(context);
	for(int i = 0; i < numChildren; i++){
	    Node temp;
	    if((temp = children[i]) instanceof VarDeclNode)
		errors.addAll(checkVarDecl(temp, context));
	    else
		errors.addAll(checkFunDecl(temp, context));
	}


	return errors;
    }// end of checkProgram()
    

    
    public ArrayList checkVarDecl(Node varDec, Context passedContext){
	return passedContext.add(new VarSymbol((VarDeclNode)varDec));		
    }// end of checkVarDecl() 

    
    /*
    // might need this if we want functions to be usable without declaration in C
    // &&&&&&- could we call it addFunDecl()
    public ArrayList checkFunHeader(Node funDec, Context passedContext){
    return passedContext.add(new FunSymbol(funDec));
    }// end of checkFunDecl()
    */
    public ArrayList checkFunDecl(Node funDec, Context passedContext){
	ArrayList returnList=new ArrayList();
	Context newContext = new Context();
	Node[] params = new Node[0];
	if (funDec.children.length==4)
	    params = funDec.children[2].getChildren();
	else 
	    params = funDec.children[3].getChildren();

	for (int i=0; i<params.length;i++){
	    if (!(params[i] instanceof VoidNode))
		returnList.addAll(newContext.add(new VarSymbol((ParamNode)params[i])));
	}
	returnList.addAll(passedContext.add(new FunSymbol((FunDeclNode)funDec)));


	symbolTable.addContext(newContext);

	returnList.addAll(checkCompStmt((Node)funDec.children[funDec.children.length-1], newContext));
	((FunDeclNode)funDec).allDeclarations=getDeclarations((Node)funDec.children[funDec.children.length-1]);
	((FunDeclNode)funDec).allCalls=getCalls((Node)funDec.children[funDec.children.length-1]);
	((FunDeclNode)funDec).stackSize=getStackFrameSize(((FunDeclNode)funDec), params);


	//System.out.println("all declarations : \n" + ((FunDeclNode)funDec).allDeclarations);
	//System.out.println("stack size : " + ((FunDeclNode)funDec).stackSize);
	//	System.out.println("all calls : \n" );
	//	for (int i=0;i<((FunDeclNode)funDec).allCalls.size();i++){
	//	    System.out.println(((Node)((FunDeclNode)funDec).allCalls.get(i)).reducedNode());
	//	    }
	symbolTable.clearContexts();	
	return returnList;
    }// end of checkFunDecl()


    int getStackFrameSize(FunDeclNode funDec, Node[] params){
	ArrayList declarations=funDec.allDeclarations;
	ArrayList calls =funDec.allCalls;

	VarDeclNode currentDeclaration = new VarDeclNode(0);
	ParamNode currentParam  = new ParamNode(0);
	ArgsExprNode currentCallArgs= new ArgsExprNode(0);
	int count=MINSTACKFRAMESIZE;
	int deccount=0;
	for (int i=0;i < declarations.size(); i++){
	    currentDeclaration=(VarDeclNode)declarations.get(i);
	    if (currentDeclaration.children.length==2)
		deccount +=INTSIZE;
	    if (currentDeclaration.children.length==3){
		if (currentDeclaration.children[1].toString().equals("asterisk"))		    
		    deccount +=POINTERSIZE;		
		else
		    deccount +=INTSIZE*((NumNode)currentDeclaration.children[2]).value;
	    }	    		
	}
	funDec.nlocals=deccount/4;

	int maxargslength=6;
	int argscount=0;
	for (int i=0;i < calls.size(); i++){
	    currentCallArgs=(ArgsExprNode)((PrimaryExprNode)calls.get(i)).children[2];
	    if (currentCallArgs.children.length > maxargslength)
		maxargslength = currentCallArgs.children.length;
	}

	funDec.maxcallsize=maxargslength;
	if (maxargslength > 6)
	    argscount=(maxargslength-6)*INTSIZE;

	/*
	int paramscount =0 ;

	if (params.length>6){
	    for (int i=0;i< params.length;i++){
		if (!(params[i] instanceof VoidNode)) {
		    currentParam=(ParamNode)params[i];
		    if (currentParam.children.length==2)//for int vals
			paramscount +=INTSIZE;
		    if (currentParam.children.length==3)//for pointer vals
			paramscount +=POINTERSIZE;
		    if (currentParam.children.length==4)//for array refs
			paramscount +=POINTERSIZE;		    
		}
	    }
	    paramscount -=24;	    
	}
	*/
	
	//count += deccount+paramscount;
	count += deccount+argscount;

	if (count%8!=0)
	    count+=4;
	return count;
	    
    }

    ArrayList getDeclarations(Node node){
	ArrayList returnList = new ArrayList();
	if (node instanceof VarDeclNode){
	    returnList.add(node);

	}
	else if (node.children != null)	    
	    for (int i=0; i<node.children.length; i++){
		if (node.children[i] != null)
		    returnList.addAll(getDeclarations(node.children[i]));	    
	    }
	return returnList;	
    } 
    /*    
    ArrayList getCalls(Node csNode){
	Node stmtlistnode= new StmtListNode(0);
	ArrayList returnList = new ArrayList();

	if (csNode.children.length==2)
	    stmtlistnode=csNode.children[1];
	else {
	    if (csNode.children[0] instanceof StmtListNode)
		stmtlistnode=csNode.children[0];
	    else
		return returnList;
	}

	Node returnedcall;
	Node returnedcompstmt;

	for (int i=0; i<stmtlistnode.children.length; i++){
	    if (stmtlistnode.children[i] != null){
		returnedcall=getCallFromStatement(stmtlistnode.children[i]);
		returnedcompstmt=getCompStmtFromStatementg(stmtlistnode.children[i]);		
		if (returnedcall instanceof PrimaryExprNode)
		    csNode.calls.add(new CallSymbol(returnedcall));
	    }
	}

	

	ArrayList returnList = new ArrayList();
	if (node instanceof VarDeclNode){
	    returnList.add(node);

	}
	else if (node.children != null)	    
	return returnList;	
				    
    } 
    */

    ArrayList getCalls(Node node){

	ArrayList returnList = new ArrayList();

	if (node instanceof PrimaryExprNode){
	    if (((PrimaryExprNode)node).isCall)
		returnList.add(node);
	}
	else if (node.children != null)	    
	    for (int i=0; i<node.children.length; i++){
		if (node.children[i] != null)
		    returnList.addAll(getCalls(node.children[i]));	    
	    }
	return returnList;	
    }
    
    public ArrayList checkCompStmt(Node compStmtNode){
	ArrayList returnList = new ArrayList();
	if ((compStmtNode.children == null) || (compStmtNode.children.length == 0))
	    return returnList;
	if (compStmtNode.children.length==2) {
	    returnList.addAll(checkLocalDecs(compStmtNode.children[0]));
	    returnList.addAll(checkStmtList(compStmtNode.children[1]));

	    //||***** possibly pop off the context added by the checkLocalDecs once we're done here? 
	    //Actually, Yes, because only then can you have true scoping and end-of-scoping
	}
	else if (compStmtNode.children[0] instanceof LocalDecsNode)
	    returnList.addAll(checkLocalDecs(compStmtNode.children[0]));
	else if (compStmtNode.children[0] instanceof StmtListNode)
	    returnList.addAll(checkStmtList(compStmtNode.children[0]));
	else
	    returnList.add("[" + compStmtNode.linenumber + "]: compiler error: invalid node found in Compound Statement");
	if (compStmtNode.children.length==2)
	    symbolTable.removeTopContext();
	return returnList;
	
    }// end of checkCompStmt()
    
    public ArrayList checkCompStmt(Node compStmtNode, Context context){
	ArrayList returnList = new ArrayList();
	Node stmts = new CompStmtNode(0);
	if ((compStmtNode.children == null) || (compStmtNode.children.length == 0)){
	    returnList.add("[" + compStmtNode.linenumber + "]: No return statement in function " + ((FunSymbol)symbolTable.getCurrentFunctionSymbol()).signature );
	    return returnList;
	}
	if (compStmtNode.children.length==2) {
	    returnList.addAll(checkLocalDecs(compStmtNode.children[0], context));
	    returnList.addAll(checkStmtList(compStmtNode.children[1]));
	    stmts  = compStmtNode.children[1];
	    //||***** possibly pop off the context added by the checkLocalDecs once we're done here? 
	    //Actually, Yes, because only then can you have true scoping and end-of-scoping
	}
	else if (compStmtNode.children[0] instanceof LocalDecsNode) {
	    returnList.add("[" + compStmtNode.linenumber + "]: No return statement in function " + ((FunSymbol)symbolTable.getCurrentFunctionSymbol()).signature );
	    returnList.addAll(checkLocalDecs(compStmtNode.children[0], context));
	}
	else if (compStmtNode.children[0] instanceof StmtListNode) {
	    returnList.addAll(checkStmtList(compStmtNode.children[0]));
	    stmts  = compStmtNode.children[0];
	}
	else
	    returnList.add("[" + compStmtNode.linenumber + "]: compiler error: invalid node found in Compound Statement");
	
	boolean foundreturn=false;
	int foundat=0;
	for (int i=0;i< stmts.children.length;i++){
	    if (stmts.children[i] instanceof ReturnStmtNode) {
		foundreturn=true;	    
		foundat = i;
		break;
	    }
	}
	if (!foundreturn)
	    returnList.add("[" +compStmtNode.linenumber+ "]: No return statement in function " + ((FunSymbol)symbolTable.getCurrentFunctionSymbol()).signature );

	
	if (foundreturn &&  (foundat != (stmts.children.length-1)) )
	    returnList.add("["+compStmtNode.linenumber+"]: unreachable statements after return in function  " + ((FunSymbol)symbolTable.getCurrentFunctionSymbol()).signature );

	return returnList;
	
    }// end of checkCompStmt()
    
    public ArrayList checkLocalDecs(Node node){
	ArrayList returnList = new ArrayList();
	Node[] children = node.getChildren();
	int numChildren = node.getNumChildren();
	Context context = new Context();
	for(int i = 0; i < numChildren; i++){
	    Node temp;
	    if((temp = children[i]) instanceof VarDeclNode)
		returnList.addAll(checkVarDecl(temp, context));
	    else
		returnList.add("[" + node.linenumber + "]: compiler error: invalid declaration type found ");
	}
	symbolTable.addContext(context);
	return returnList;
	
    }// end of checkLocalDecs()


    
    public ArrayList checkLocalDecs(Node node, Context context){
	ArrayList returnList = new ArrayList();
	Node[] children = node.getChildren();
	int numChildren = node.getNumChildren();
	for(int i = 0; i < numChildren; i++){
	    Node temp;
	    if((temp = children[i]) instanceof VarDeclNode)
		returnList.addAll(checkVarDecl(temp, context));
	    else
		returnList.add("[" + node.linenumber + "]: compiler error: invalid declaration type found ");
	}
	return returnList;
	
    }// end of checkLocalDecs()


    public ArrayList checkStmtList(Node node){
	ArrayList returnList = new ArrayList();
	Node[] children = node.getChildren();
	int numChildren = node.getNumChildren();
	Node temp;

	for(int i = 0; i < children.length; i++){
	    temp = children[i];
	    if(temp instanceof StatementNode)
		returnList.addAll(checkStmt(temp));
	    else
		returnList.add( "[" + temp.linenumber + "]: compiler error: invalid statement found :" + temp);
	}
	return returnList;
    }// end of checkStmtList()

    
    public ArrayList checkStmt(Node node){
	if (node instanceof ExprStmtNode)
	    return checkExprStmt(node);
	else if (node instanceof CompStmtNode)
	    return checkCompStmt(node);
	else if (node instanceof IfStmtNode)
	    return checkIfStmt(node);
	else if (node instanceof WhileStmtNode)
	    return checkWhileStmt(node);
	else if (node instanceof ForStmtNode)
	    return checkForStmt(node);
	else if (node instanceof ReturnStmtNode)
	    return checkReturnStmt(node);
	else{
	    ArrayList returnList = new ArrayList();
	    returnList.add("[" + node.linenumber + "]: compiler error: invalid statement type found : "+ node);
	    return (returnList);
	}
    }// end of checkStmt()

    public ArrayList checkExprStmt(Node node){
	if (node.children.length!=0)
	    return checkExpression(node.children[0]);
	else
	    return new ArrayList();
    }

    public ArrayList checkIfStmt(Node node){
	ArrayList returnList = new ArrayList();	
	if ((node.children==null) || (node.children.length ==0))
	    returnList.add("[" + node.linenumber + "]: compiler error: invalid if statement  found");	
	else {
	    returnList.addAll(checkIfStart(node.children[0]));
	    if (node.children.length == 2)
		returnList.addAll(checkIfRem(node.children[1]));	    
	}	
	return returnList;
    }

    public ArrayList checkIfStart(Node node){
	ArrayList returnList = new ArrayList();	
	if ((node.children!= null) && (node.children.length == 3)){
	    if (!(node.children[0] instanceof IfNode))
		returnList.add("[" + node.linenumber + "]:compiler error: if statement without 'if'.");
	    returnList.addAll(checkExpression(node.children[1]));
	    returnList.addAll(checkStmt(node.children[2]));
	}	
	else 
	    returnList.add("[" + node.linenumber + "]: compiler error: invalid if statement  found ");	
	return returnList;
    }

    public ArrayList checkIfRem(Node node){
	ArrayList returnList = new ArrayList();	
	if ((node.children!= null) && (node.children.length == 2)){
	    if (!(node.children[0] instanceof ElseNode))
		returnList.add("[" + node.linenumber + "]:compiler error: else statement without 'else'."); 
	    returnList.addAll(checkStmt(node.children[1]));
	}	
	else 
	    returnList.add("[" + node.linenumber + "]: compiler error: invalid if-else statement  found ");	
	return returnList;
    }


    public ArrayList checkWhileStmt(Node node){
	ArrayList returnList = new ArrayList();	
	if ((node.children!= null) && (node.children.length == 3)){
	    if (!(node.children[0] instanceof WhileNode))
		returnList.add("[" + node.linenumber + "]:compiler error: while statement without 'while'.");
	    returnList.addAll(checkExpression(node.children[1]));
	    returnList.addAll(checkStmt(node.children[2]));
	}	
	else 
	    returnList.add("[" + node.linenumber + "]: compiler error: invalid while statement found ");	
	return returnList;
    }

    public ArrayList checkForStmt(Node node){
	ArrayList returnList = new ArrayList();	
	if ((node.children!= null) && ((node.children.length == 4) || (node.children.length == 5))){
	    if (!(node.children[0] instanceof ForNode))
		returnList.add("[" + node.linenumber + "]:compiler error: for statement without 'for'.");
	    returnList.addAll(checkExprStmt(node.children[1]));
	    returnList.addAll(checkExprStmt(node.children[2]));
	    if (node.children.length==4)
		returnList.addAll(checkStmt(node.children[3]));
	    else{
		returnList.addAll(checkExpression(node.children[3]));
		returnList.addAll(checkStmt(node.children[4]));
	    }
	}	
	else 
	    returnList.add("[" + node.linenumber + "]: compiler error: invalid while statement found ");	
	return returnList;
    }

    public ArrayList checkReturnStmt(Node node){
	ArrayList returnList = new ArrayList();	
	if ((node.children!= null) && ((node.children.length==1)||(node.children.length==0))){	    
	    if (node.children.length == 1)
		returnList.addAll(checkExpression(node.children[0]));	    
	    String returnType = symbolTable.getCurrentFunctionSymbol().type;
	    if (returnType.equals("void")) {
		if (node.children.length !=0)
		    returnList.add("[" + node.linenumber 
				   + "]: invalid return type for function ");
	    }
	    else {
		if (node.children.length==0) 
		    returnList.add("[" + node.linenumber 
				   + "]: invalid return type for function ");		    
		else {
		    //System.out.println("returnType = " + returnType +  " | nodetype = " + node.children[0].type);
		    if (!node.children[0].type.equals(returnType))
			returnList.add("[" + node.linenumber 
				   + "]: invalid return type for function ");
		}

	    }				
	}	
	else 
	    returnList.add("[" + node.linenumber + "]: compiler error: invalid return statement found ");	
	return returnList;
    }

    public ArrayList checkExpression(Node node){
	ArrayList returnList = new ArrayList();
	if (node.children.length == 1) {
	    returnList.addAll(checkOrExpr(node.children[0]));	    
	    //	    System.out.println("in checkexpr, type = " + node.children[0].type);
	    node.type=node.children[0].type;
	    node.lvalue=node.children[0].lvalue;	    
	}
	else {
	    returnList.addAll(checkOrExpr(node.children[0]));
	    returnList.addAll(checkExpression(node.children[2]));	    
	    if (!node.children[0].lvalue)
		returnList.add("[" + node.children[0].linenumber + "]: Not an l-value: " + findLexemeValue(node));
	    if (!node.children[0].type.equals(node.children[2].type))
		returnList.add("[" + node.children[0].linenumber + "]: incompatible types for assignment: " 
			       + " value of type(" + node.children[2].type 
			       + ") assigned to lvalue of type(" + node.children[0].type + ") "  );	    


	    //	    System.out.println("in checkexpr, types: " + node.children[0].type + " "+ node.children[2].type );
	    //	    System.out.println("in checkexpr, nodes: " + node.children[0] + " "+ node.children[2] );

	    node.type=node.children[0].type;	    
	    node.lvalue=false;
	}
	return returnList;
    }// end of checkExpression()

    public ArrayList checkOrExpr(Node node){
	ArrayList returnList = new ArrayList();
	node.type=node.INT;

	for (int i=0; i<node.children.length;i++){
	    returnList.addAll(checkAndExpr(node.children[i]));
	}

	if (node.children.length==1){
	    node.type=node.children[0].type;
	    node.lvalue=node.children[0].lvalue;
	}
	else
	    node.lvalue=false;

	return returnList;
    }// end of checkOrExpr()

    public ArrayList checkAndExpr(Node node){
	ArrayList returnList = new ArrayList();
	node.type=node.INT;

	for (int i=0; i<node.children.length;i++){
	    returnList.addAll(checkRelExpr(node.children[i]));
	}

	if (node.children.length==1){
	    node.type=node.children[0].type;
	    node.lvalue=node.children[0].lvalue;
	}
	else
	    node.lvalue=false;

	return returnList;
    }// end of checkAndExpr()

    public ArrayList checkRelExpr(Node node){
	ArrayList returnList = new ArrayList();
	String childrenType="";//caution
	node.type=node.INT;

	for (int i=0; i<node.children.length;i++){
	    returnList.addAll(checkAddExpr(node.children[i]));

	    if (i==0)
		childrenType=node.children[i].type;
	    else if (!node.children[i].type.equals(childrenType))
		returnList.add("[" + node.children[i].linenumber + "]:invalid type for relational operator");
	    i++; // increment for relational operator node
	}

	if (node.children.length==1){
	    node.type=node.children[0].type;
	    node.lvalue=node.children[0].lvalue;
	}
	else
	    node.lvalue=false;

	return returnList;
    }// end of checkRelExpr()

    public ArrayList checkAddExpr(Node node){
	//System.err.println("got in checkAddExpr with node: " + findLexemeValue(node) + " of length " + node.children.length );
	ArrayList returnList = new ArrayList();
	
	returnList.addAll(checkTerm(node.children[0]));
	String lastType = node.children[0].type;
	//System.err.println("lastType: " + lastType + " on node " + findLexemeValue(node));
	for(int i = 1; i < node.children.length; i++){

	    returnList.addAll(checkTerm(node.children[++i]));

	    if (((TerminalNode)node.children[i-1]).token.symbolNumber == 19){ // detect a + operator
		lastType=plusCompare(lastType,node.children[i].type);
	    }
	    else if (((TerminalNode)node.children[i-1]).token.symbolNumber == 20) { // detect a - operator
		lastType=minusCompare(lastType,node.children[i].type);
	    }
	    
	    if (lastType.equals("type-compare-error") || lastType.equals(Node.ERROR)) {
		returnList.add("[" + node.linenumber + "]: invalid type combination for addition/subtraction operator");
		lastType=node.children[i].type;//arbitrarily assign a type to lastType because 
		//type-compare-error is neither pretty nor a real type
	    }
	    	    
	}
	
	if (node.children.length==1) {
	    node.lvalue=node.children[0].lvalue;
	}
	else
	    node.lvalue=false;
	
	node.type = lastType;
	return returnList;     
    }// end of checkAddExpr()

    private String plusCompare(String type1, String type2){
	if (type1.equals(Node.INT)) {
	    if (type2.equals(Node.INT))
		return Node.INT; //int + int --> int 
	    if (type2.equals(Node.INTSTAR))
		return Node.INTSTAR; //int + int* --> int*
	    if (type2.equals(Node.FLOATSTAR))
		return Node.FLOATSTAR; //int + float*--> float*
	}
	if (type1.equals(Node.FLOAT) && type2.equals(Node.FLOAT))
	    return Node.FLOAT; // float + float --> float
	if (type1.equals(Node.INTSTAR) && type2.equals(Node.INT))
	    return Node.INTSTAR; // int* + int --> int*
	if (type1.equals(Node.FLOATSTAR) && type2.equals(Node.INT))
	    return Node.FLOATSTAR; // float + float --> float	    
	return Node.ERROR;
    }

    private String minusCompare(String type1, String type2){

	if (type1.equals(Node.INT) && type2.equals(Node.INT))
	    return Node.INT; // int - int --> int
	if (type1.equals(Node.FLOAT) && type2.equals(Node.FLOAT))
	    return Node.FLOAT; // float - float --> float
	if (type1.equals(Node.INTSTAR) && type2.equals(Node.INT))
	    return Node.INTSTAR; // int* - int --> int*
	if (type1.equals(Node.FLOATSTAR) && type2.equals(Node.INT))
	    return Node.FLOATSTAR; // float - int --> float*	    
	if (type1.equals(Node.INTSTAR) && type2.equals(Node.INTSTAR))
	    return Node.INT; // int* - int* --> int
	if (type1.equals(Node.FLOATSTAR) && type2.equals(Node.FLOATSTAR))
	    return Node.INT; // float* - float* --> int
	return Node.ERROR;
    }


    public ArrayList checkTerm(Node node){
     	ArrayList returnList = new ArrayList();
	String childrenType="";//caution

	if (node.children.length == 1){
	    returnList.addAll(checkUnaryExpr(node.children[0]));
	    node.lvalue=node.children[0].lvalue;
	}
	else {
	    for (int i=0; i<node.children.length;i=i+2){
		returnList.addAll(checkUnaryExpr(node.children[i]));

		if (i==0)
		    childrenType=node.children[i].type;
		else if ((!node.children[i].type.equals(childrenType)))			  
		    returnList.add("[" + node.children[i].linenumber + "]:invalid type for mult/div operator");



		if ((i!=node.children.length-1) && ((TerminalNode)node.children[i+1]).token.symbolNumber == 16) //check for %
		    if (!childrenType.equals(node.INT))
			returnList.add("[" + node.children[i].linenumber + "]: % operator can only be applied to ints");
	    }
	    
	    if (!(childrenType.equals(Node.INT) || childrenType.equals(Node.FLOAT) ))
		returnList.add("[" + node.linenumber + "]:invalid type for mult/div operator");
	    
	    node.lvalue=false;	 
	}
	
	node.type=node.children[0].type;
	return returnList;
    }// end of checkTerm()

    public ArrayList checkUnaryExpr(Node node){

     	ArrayList returnList = new ArrayList();
	
	if ((node.children!=null) && (node.children.length == 1)){
	    returnList.addAll(checkPrimaryExpr(node.children[0]));
	    node.lvalue=node.children[0].lvalue;
	    node.type=node.children[0].type;
	}
	else {
	    //	    System.out.println("checkUnaryexpr got node: \n" + node.printTree());
	    //	    System.out.println("children: \n" + node.children.length);
	    //	    System.out.println("child 0: \n" + node.children[0]);
	    //	    System.out.println("child 1: \n" + node.children[1]);
	    //	    System.out.println("child 1 type: \n" + node.children[1].type);		    
	    returnList.addAll(checkUnaryExpr(node.children[1]));
	    //System.out.println("child 1 type: \n" + node.children[1].type);
	    if (((TerminalNode)node.children[0]).token.symbolNumber == 19) { // check for +
		if (!(node.children[1].type.equals(node.INT) || node.children[1].type.equals(node.FLOAT)))
		    returnList.add("[" + node.linenumber + "]: unary operator '+' is invalid on " + findLexemeValue(node.children[1])+"'");
		node.type = node.children[1].type;
		node.lvalue = false;
	    } 
	    else if (((TerminalNode)node.children[0]).token.symbolNumber == 20) { // check for -
		if (!(node.children[1].type.equals(node.INT) || node.children[1].type.equals(node.FLOAT)))
		    returnList.add("[" + node.linenumber + "]: unary operator '-' is invalid on " + findLexemeValue(node.children[1])+"'");
		node.type = node.children[1].type;
		node.lvalue = false;
	    } 
	    else if (((TerminalNode)node.children[0]).token.symbolNumber == 15) { // check for &
		node.type = node.children[1].type+"*";
		if ((!(node.children[1].type.equals(node.INT) || node.children[1].type.equals(node.FLOAT)))
		    || (!(node.children[1].lvalue==true))){
		    returnList.add("[" + node.linenumber + "]: unary operator '&' is invalid on '" + findLexemeValue(node.children[1])+"'"); 
		    node.type = node.children[1].type;
		}
		node.lvalue = false;
	    } 
	    else if (((TerminalNode)node.children[0]).token.symbolNumber == 14) { // check for *
		node.type = node.children[1].type.substring(0,node.children[1].type.length()-1);
		if ((!(node.children[1].type.equals(node.INTSTAR) || node.children[1].type.equals(node.FLOATSTAR)))){
		    returnList.add("[" + node.linenumber + "]: unary operator '*' is invalid on '" + findLexemeValue(node.children[1])+"'");		    
		    node.type = node.children[1].type;
		}
		node.lvalue = true;
	    } 
	    else if (((TerminalNode)node.children[0]).token.symbolNumber == 17) { // check for !
		node.type=node.INT;
		node.lvalue=false;
	    } 
	    
	}
	
	return returnList;
     
    }// end of checkUnaryExpr()

    public ArrayList checkPrimaryExpr(Node node){
	ArrayList returnList = new ArrayList();
	if (node.children.length == 1) {
	    if (node.children[0] instanceof ExpressionNode){
		returnList.addAll(checkExpression(node.children[0]));
		node.type=node.children[0].type;
		node.lvalue=node.children[0].lvalue;
	    }
	    else if (node.children[0] instanceof IdentifierNode){
		Symbol returnedSymbol=symbolTable.getVar(node.children[0].toString());
		if (returnedSymbol instanceof ErrorSymbol){
		    returnList.add("[" + node.linenumber + "]: variable '" + findLexemeValue(node) + "' undeclared ");
		    node.children[0].type=node.ERROR;
		    node.children[0].lvalue=true;
		    node.type=node.children[0].type;
		    node.lvalue=node.children[0].lvalue;
		}
		else{
		    node.children[0].type=returnedSymbol.type;
		    node.children[0].lvalue=true;
		    node.type=node.children[0].type;
		    node.lvalue=node.children[0].lvalue;
		}		
	    }
	    else if (node.children[0] instanceof RealNode){
		node.children[0].type=node.FLOAT;
		node.children[0].lvalue=false;
		node.type=node.children[0].type;
		node.lvalue=node.children[0].lvalue;
	    }
	    else if (node.children[0] instanceof NumNode){
		node.children[0].type=node.INT;
		node.children[0].lvalue=false;
		node.type=node.children[0].type;
		node.lvalue=node.children[0].lvalue;
	    }	    
	}
	else if (node.children.length==4) {
	    if (node.children[2] instanceof ExpressionNode){
		Symbol returnedSymbol=symbolTable.getVar(node.children[0].toString());		

		if (returnedSymbol instanceof ErrorSymbol){
		    returnList.add("[" + node.linenumber + "]: variable '" + node.children[0].toString() + "' undeclared.");
		    node.type=node.ERROR;
		    node.lvalue=false;

		}
		else{
		    returnList.addAll (checkExpression(node.children[2]));
		    if (node.children[2].type.equals(node.INT) 
			&& (returnedSymbol.type.equals(node.INTSTAR) || returnedSymbol.type.equals(node.FLOATSTAR))){			

			node.type = returnedSymbol.type.substring(0,returnedSymbol.type.length()-1);
			node.lvalue=true;
		    }
		    else {
			node.type = returnedSymbol.type;
			node.lvalue=true;
			returnList.add("[" + node.linenumber + "]: invalid array reference of '" + findLexemeValue(node)+"'");
		    }
		}
	    }
	    else if (node.children[2] instanceof ArgsExprNode) {
		returnList.addAll(checkArgsExpr(node.children[2]));
		Symbol returnedSymbol=symbolTable.getFunbySig(createFunSig(node.children[0],node.children[2]));


		//		System.out.println("checked function is :" + returnedSymbol);
		/*

		//--------------------------------------------------------
		//Code commented out because codegen needs to be able to make calls to undeclared functions
		//--------------------------------------------------------

		if (returnedSymbol instanceof ErrorSymbol){
		    Symbol returnedSymbol2 = symbolTable.getFunbyIdent(node.children[0].toString());
		    if (!(returnedSymbol2 instanceof ErrorSymbol))
			returnList.add("[" + node.linenumber 
				       + "]: incorrect arguments for function '" + node.children[0] + "'");
		    else
			returnList.add("[" + node.linenumber 
				       + "]: function '" + node.children[0] + "' not declared.");		    
		}	
		*/
		//		System.out.println("primaryexpr, checked function of type :" + returnedSymbol.type);
		node.type = returnedSymbol.type;
		node.lvalue=false;
		((PrimaryExprNode)node).isCall=true;
		((PrimaryExprNode)node).callsignature=createFunSig(node.children[0],node.children[2]);
	    }
	}
	else
	    returnList.add("[" + node.linenumber + "]: Compiler error: primaryexpr of invalid length found");
	    
     	return returnList;
    }// end of checkPrimaryExpr()

    static private String createFunSig(Node funIdentifier, Node funArgs){
	String sig;
	sig =  "" + funIdentifier.toString() ;		
	
	if (funArgs.children.length==0)
	    sig += "_void";

	for(int i = 0; i < funArgs.children.length; i++){
	    sig +=  "_" + funArgs.children[i].type;
	}
	return sig;

    }

    public ArrayList checkArgsExpr(Node node){
	ArrayList returnList = new ArrayList();	
	for (int i=0; i<node.children.length;i++){
	    if (node.children[i] instanceof ExpressionNode)
		returnList.addAll(checkExpression(node.children[i]));
	    else
		returnList.add("[" + node.linenumber + "]:invalid argument");
	}
     	return returnList;
    }// end of checkArgsExpr()

    public static String findLexemeValue(Node node){
	if (node instanceof PrimaryExprNode){
	    if (node.children!=null && node.children.length>=1 && (node.children[0] instanceof ExpressionNode))
		return "expression beginning with '" + findLexemeValue(node.children[0])+ "'";	    
	    return node.children[0].toString();
	}
	else if ((node.children!=null) && (node.children.length > 0))
	    return findLexemeValue(node.children[0]);
	else if ((node.children!=null) && (node.children.length == 0))
	    return node.nodeStringValue;
	else if (node.children==null)
	    return node.nodeStringValue;
	else
	    return "unknown";	    
    }


}// end of class SemanticAnalyzer
