//Good work, Chris. I like the code. We'll blast this one. *Bob, the builder.* CAN WE FIX IT? *Bob the builder* YES WE CAN!
//--------------------------------------------------------
//testing, testing, testing : I feel like writing a number of small test case programs to see what works and doesn't work yet.
//--------------------------------------------------------
//Concern 1: we might need codegenL() functions for a few of the expresioncodegenL()'s children because we need to be 
//able to return a pointer if the user says *(x+12)=10; (which I guess is the same as saying x[12]=10;), and currently our
//codegens won't do that.
//--------------------------------------------------------
//Concern 2: we still haven't compiled a single output file in gcc, and that is a little scary. Let's do that first.
//--------------------------------------------------------
//Concern 3: more of a reminder, but we need to take care of .commons too
//--------------------------------------------------------
//Q:what is the return type of codegen(). is every codegen() supposed to return a register, or is only expr codegen supposed to return a register?
//Akshat:codegen() returns a register when it needs to. No registers are returned for levels statement and above. Expressions will return a register, but I don't think expression-statements will.
//That, or we can just start assigning temporary variable names to every single return from add/term expression codegens,
//and that would enables us to put stuff on the stack frame if more registers are needed.

import java.util.*;
import java.util.Hashtable;
import java.io.*;

public class CodeGenerator {
    Node syntaxTree;

    int registerCount;
    int globalLabelCounter;
    Hashtable labelTable= new Hashtable();
    ArrayList commons = new ArrayList(); //list of common, i.e. global variable declarations

    final String dotsection = ".section";
    final String dottext = "\".text\"";
    final String dotalign = ".align";
    final String dotglobal = ".global";
    final String dotcommon = ".common";
    final String prologue = "!#PROLOGUE#";
    final String save = "save";
    final String stackpointer = "%sp";
    final String ret = "ret";
    final String restore = "restore";
    final String store = "st";
    final String load = "ld";
    final String add = "add";
    final String sub = "sub";
    final String move = "mov";
    final String multiply = "smul";
    final String call = "call";
    final String nop = "nop";
    final String divide = "sdiv";
    final String lessthan = "bl";
    final String greaterthan = "bg";
    final String lessorequal = "ble";
    final String greaterorequal = "bge";
    final String equal = "be";
    final String notequal = "bne";
    final String and = "and";
    final String or = "or";
    final String branch = "b";
    final String compare = "cmp";
    final String zero = "0";
    final String modfunction = ".rem";
    final String sethi= "sethi";
    
    final String INTSIZE="4";
    final String POINTERSIZE="4";
    PrintWriter filewriter;

    public CodeGenerator(Node syntaxTree, String filename){

	this.syntaxTree = syntaxTree;

	try{
	    filewriter = new PrintWriter (new FileWriter(new File(filename),false));
	}
	catch(Exception e){
	    System.out.println("invalid output file path");
	}

	programCodeGen(syntaxTree);
	registerCount=0;
	globalLabelCounter=1;
	filewriter.close();
    }

    /*    
	  String codegen(Node node){
	  if (node instanceof ProgramNode)
	  return programCodeGen(node);
	  else if (node instanceof VarDeclNode)
	  return varDeclCodeGen(node);
	  else if (node instanceof FunDeclNode)
	  return funDeclCodeGen(node);
	  else if (node instanceof CompStmtNode)
	  return compStmtCodeGen(node);
	  else if (node instanceof StmtListNode)
	  return stmtListCodeGen(node);
	  else if (node instanceof LocalDecsNode)
	  return localDecsCodeGen(node);
	  else if (node instanceof ExprStmtNode)
	  return exprStmtCodeGen(node);
	  else if (node instanceof IfStmtNode)
	  return ifStmtCodeGen(node);
	  else if (node instanceof IfStartNode)
	  return ifStartCodeGen(node);
	  else if (node instanceof IfRemNode)
	  return ifRemCodeGen(node);
	  else if (node instanceof WhileStmtNode)
	  return whileStmtCodeGen(node);
	  else if (node instanceof ForStmtNode)
	  return forStmtCodeGen(node);
	  else if (node instanceof ReturnStmtNode)
	  return returnStmtCodeGen(node);
	  else if (node instanceof ExpressionNode)
	  return expressionCodeGen(node);
	  else if (node instanceof OrExprNode)
	  return orExprCodeGen(node);
	  else if (node instanceof AndExprNode)
	  return andExprCodeGen(node);
	  else if (node instanceof RelExprNode)
	  return relExprCodeGen(node);
	  else if (node instanceof AddExprNode)
	  return addExprCodeGen(node);
	  else if (node instanceof TermNode)
	  return termCodeGen(node);
	  else if (node instanceof UnaryExprNode)
	  return unaryExprCodeGen(node);
	  else if (node instanceof PrimaryExprNode)
	  return primaryExprCodeGen(node);	
	  return new String();
	  }
    */
    

    private void programCodeGen(Node node){
	emit(dotsection,dottext);

	for (int i=0; i< node.children.length ; i++){
	    if (node.children[i] instanceof VarDeclNode)
		globalVarDeclCodeGenHead((VarDeclNode)node.children[i]);
	}
	
	for (int i=0; i< node.children.length ; i++){
	    if (node.children[i] instanceof FunDeclNode)
		funDeclCodeGen(node.children[i]);
	}
	
	for (int i=0; i< node.children.length ; i++){
	    if (node.children[i] instanceof VarDeclNode)
		globalVarDeclCodeGenFoot((VarDeclNode)node.children[i]);
	}

	return;
    }

    private void globalVarDeclCodeGenHead(VarDeclNode node){
	
	if (node.children.length==2){
	    commons.add(node.children[1].toString());
	}
	else if (node.children.length==3){
	    if (node.children[1].toString().equals("asterisk")){
		commons.add(node.children[1].toString());
	    }
	    else{
		commons.add(node.children[1].toString()+"[0]");
	    }	    
	}

	return;
	
    }


    private void globalVarDeclCodeGenFoot(VarDeclNode node){
	
	if (node.children.length==2){
	    emit(dotcommon,node.children[1].toString(),INTSIZE, INTSIZE);	    
	}
	else if (node.children.length==3){
	    if (node.children[1].toString().equals("asterisk")){
		emit(dotcommon,node.children[1].toString(),POINTERSIZE,POINTERSIZE);	    
	    }
	    else{
		String arraylength=new Integer(Integer.parseInt(INTSIZE)*((NumNode)node.children[2]).value).toString();
		emit(dotcommon,node.children[1].toString(), arraylength ,INTSIZE);	    
	    }	    
	}

	return;
	
    }

    private void varDeclCodeGen(Node node, ResourceTable resourceTable){
	if ((node.children.length==3) && (node.children[2] instanceof NumNode))
	    for (int i=0;i<((NumNode)node.children[2]).value;i++)
		resourceTable.declareLocalVar(node.children[1].toString()+"["+i+"]");
	else
	    resourceTable.declareLocalVar(node.children[node.children.length-1].toString());
	return;
    }

    private void funDeclCodeGen(Node node){
	FunDeclNode funNode = (FunDeclNode)node;
	String functionlabel;
	int nargs=0;	
	int stackFrameSize=funNode.stackSize;
	String[] args = new String[0];
	Node params = new ParamsNode(0);
	

	if (node.children.length==4)
	    params = node.children[2];
	else
	    params = node.children[3];


	//code block to get nargs and args[]
	//--------------------------------------
	if (!(params.children[0] instanceof VoidNode)){
	    nargs= params.children.length;
	    args = new String[nargs];
	    for (int i=0;i<nargs;i++)
		args[i]= params.children[i].children[node.children[2].children[i].children.length-1].toString();
	}  
	//--------------------------------------
	
    
	//code block to get nlocals and locals[]
	//--------------------------------------
	int nlocals = funNode.nlocals; //number of all local declarations within the function
	String[] locals = new String[nlocals];
	for (int i=0;i<funNode.allDeclarations.size();i++){
	    Node currentNode = ((Node)funNode.allDeclarations.get(i));
	    if ((currentNode.children.length==3) && (currentNode.children[2] instanceof NumNode)) {
		int j;
		for (j=0;j<((NumNode)currentNode.children[2]).value;j++)
		    locals[i+j]=currentNode.children[1].toString()+"["+j+"]";
		i+=j;
	    }
	    else
		locals[i]=currentNode.children[currentNode.children.length-1].toString();
	}
	//--------------------------------------


	int nOutgoingArgs = funNode.maxcallsize;//nOutgoingArgs: size of biggest outgoing call from this function

	ResourceTable resourceTable = new ResourceTable(stackFrameSize,nargs,args,nlocals,locals,nOutgoingArgs, this);//create the ResourceTable
	functionlabel=getLabel(((FunDeclNode)node).signature);//get a new unused label

	emit(dotalign,INTSIZE);//.align 4
	emit(dotglobal,functionlabel);//.global <functionname>



	emitLabel(functionlabel);//<functionname>:


	emit(prologue,"0");//prologue 0
	emit(save,stackpointer,"-"+stackFrameSize,stackpointer);// save %sp,-(stackframe size), %sp
	emit(prologue,"1");//prologue 1

	for (int i=0;i<Math.min(nargs,6);i++)
	    if (!resourceTable.getPassedArgFP(i).equals(ResourceTable.NOTFOUND))
		emit(store,resourceTable.iRegTable[i][0],resourceTable.getPassedArgFP(i)); //st %i0,[fp+68]
	
	compStmtCodeGen(node.children[node.children.length-1], resourceTable);
	return;
    }
	
    private String getLabel(String signature){
	if( signature.substring(0,4).equals("main"))
	    return "main";
	
	int labelcounter=0;
	String returnLabel=signature.substring(0,signature.indexOf("_"))+"0";
	while (labelTable.containsValue(returnLabel))
	    returnLabel=returnLabel.substring(0,returnLabel.length()-1)+(new Integer(labelcounter++)).toString();
	    
	labelTable.put(signature, returnLabel);
	return returnLabel;
    }
	
    private String getLabel(){
	String returnLabel=".LL1";

	while (labelTable.containsValue(returnLabel))
	    returnLabel=".LL"+(new Integer(globalLabelCounter++)).toString();

	labelTable.put(returnLabel, returnLabel);
	//System.out.println(labelTable);
	return returnLabel; // return a unique label

    }

    private void emitLabel(String labelstring){
	filewriter.println(labelstring+":");
	return;
    }

    private void compStmtCodeGen(Node node, ResourceTable resourceTable){
	if ((node.children != null) && (node.children.length!=0)){
	    if (node.children.length==2){
		localDecsCodeGen(node.children[0], resourceTable);
		stmtListCodeGen(node.children[1], resourceTable);
	    }
	    else if (node.children[0] instanceof LocalDecsNode)
		localDecsCodeGen(node.children[0], resourceTable);
	    else
		stmtListCodeGen(node.children[0], resourceTable);	
	}
	return;
    }

    private void stmtCodeGen(Node node, ResourceTable resourceTable){

	if (node instanceof CompStmtNode)
	    compStmtCodeGen(node, resourceTable);
	else if (node instanceof ExprStmtNode)
	    exprStmtCodeGen(node, resourceTable);
	else if (node instanceof IfStmtNode)
	    ifStmtCodeGen(node, resourceTable);
	else if (node instanceof WhileStmtNode)
	    whileStmtCodeGen(node, resourceTable);
	else if (node instanceof ForStmtNode)
	    forStmtCodeGen(node, resourceTable);
	else if (node instanceof ReturnStmtNode){
	    returnStmtCodeGen(node, resourceTable);	      
	}
	else
	    System.out.println("compiler error: unknown statement: " + node );
	return;
    }

    private void stmtListCodeGen(Node node, ResourceTable resourceTable){
	for (int i=0;i<node.children.length;i++)
	    stmtCodeGen(node.children[i], resourceTable);
	return;
    }

    private void localDecsCodeGen(Node node, ResourceTable resourceTable){
	for (int i=0; i<node.children.length;i++)
	    varDeclCodeGen(node.children[i], resourceTable);

	return;
    }



    private void exprStmtCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	if(node.children.length==1)
	    returnVal = expressionCodeGen(node.children[0], resourceTable);
	
	resourceTable.releaseRegister(returnVal);
	return;
    }

    // I think that all of the statements are by nature 'void', any thoughts???

    
    private void ifStmtCodeGen(Node node, ResourceTable resourceTable){
	if(node.children.length == 1) {
	    String fail = getLabel();
	    String reg = expressionCodeGen(node.children[0].children[1], resourceTable);
	    emit(compare, reg, "0");
	    emit(equal, fail);
	    emit(nop);
	    stmtCodeGen(node.children[0].children[2], resourceTable);
	    emitLabel(fail);
	    resourceTable.releaseRegister(reg);
	}
	else{
	    String fail = getLabel();
	    String success = getLabel();
	    String reg = expressionCodeGen(node.children[0].children[1], resourceTable);
	    emit(compare, reg, "0");
	    emit(equal, fail);
	    emit(nop);
	    stmtCodeGen(node.children[0].children[2], resourceTable);
	    emit(branch, success);
	    emit(nop);
	    emitLabel(fail);
	    resourceTable.releaseRegister(reg);
	    stmtCodeGen(node.children[1].children[1], resourceTable);
	    emitLabel(success);
	}
    }


    private void whileStmtCodeGen(Node node, ResourceTable resourceTable){
	String begin = getLabel();
	String end = getLabel();
	emitLabel(begin);
	String reg = expressionCodeGen(node.children[1], resourceTable);
	emit(compare, reg, "0");
	emit(equal, end);
	emit(nop);
	stmtCodeGen(node.children[2], resourceTable);
	emit(branch, begin);
	emit(nop);
	emitLabel(end);
	resourceTable.releaseRegister(reg);	
    }

    //should work, testing due
    private void forStmtCodeGen(Node node, ResourceTable resourceTable){	
	String thirdexpr;
	String begin = getLabel();
	String end = getLabel();
	String comparisonreg;
	exprStmtCodeGen(node.children[1],resourceTable);

	emitLabel(begin);
	if (node.children[2].children.length!=0){
	    comparisonreg = expressionCodeGen(node.children[2].children[0], resourceTable);
	    emit(compare,comparisonreg, "0"); 
	    emit(equal, end);
	    emit(nop);	
	    resourceTable.releaseRegister(comparisonreg);
	}	

	if (node.children.length==5){
	    stmtCodeGen(node.children[4],resourceTable);
	    thirdexpr=expressionCodeGen(node.children[3], resourceTable);
	    resourceTable.releaseRegister(thirdexpr);
	}			      
	else {
	    stmtCodeGen(node.children[3],resourceTable);
	}

	emit(branch, begin);
	emit(nop);
	emitLabel(end);		
	
    }


    private void returnStmtCodeGen(Node node, ResourceTable resourceTable){

	if(node.children.length == 0){ //case of "return;" so just return
	    emit(ret);
	    emit(restore);
	}
	else{ // case of "return expression;" so evaluate the expression, move the result to the return register, then return
	    String reg = expressionCodeGen(node.children[0], resourceTable);
	    emit(move, reg, "%i0");
	    resourceTable.releaseRegister(reg);
	    emit(ret);
	    emit(restore);
	}
	return;
    }

   

    private String expressionCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = new String();
	String reg1 ="";
	if(node.children.length==1){
	    reg1 = orExprCodeGen(node.children[0], resourceTable);
	    return reg1;
	}

	String stacklocRegister = orExprCodeGenL(node.children[0], resourceTable);
	if(((TerminalNode)node.children[1]).token.symbolNumber == 27){ //operator +=
	    String reg2 = expressionCodeGen(node.children[2], resourceTable);
	    reg1= resourceTable.getRegister();

	    emit (load, "["+stacklocRegister+"]", reg1);
	    emit(add, reg1, reg2 ,reg2);
	    resourceTable.releaseRegister(reg1);

	    emit(store, reg2, "["+stacklocRegister+"]");

	    resourceTable.releaseRegister(stacklocRegister);
	    returnVal=reg2;
	}
	else if(((TerminalNode)node.children[1]).token.symbolNumber == 28){ //operator -=
	    String reg2 = expressionCodeGen(node.children[2], resourceTable);
	    reg1= resourceTable.getRegister();

	    emit (load, "["+stacklocRegister+"]", reg1);
	    emit(sub, reg1, reg2 ,reg2);
	    resourceTable.releaseRegister(reg1);

	    emit(store, reg2, "["+stacklocRegister+"]");
	    resourceTable.releaseRegister(stacklocRegister);
	    returnVal=reg2;
	}
	else{ //operator =
	    String reg2 = expressionCodeGen(node.children[2], resourceTable);
	    emit(store, reg2, "["+stacklocRegister+"]");
	    resourceTable.releaseRegister(stacklocRegister);
	    returnVal=reg2;
	}
	
	return returnVal;

    }


    //The OR code is fine. The OR instruction is a bitwise and not logical OR,
    //but I don't see how a logical OR is different from a bitwise OR.
    //We cool.
    //- Akshat
    private String orExprCodeGen(Node node, ResourceTable resourceTable){	
	String returnVal = resourceTable.NOTFOUND;

	String reg1 = resourceTable.NOTFOUND;
	String reg2 = resourceTable.NOTFOUND;

	reg1 = andExprCodeGen(node.children[0], resourceTable);
	
	if(node.children.length == 1)
	    returnVal = reg1;
	else{
	    for(int i = 1; i < node.children.length; i++){//any number of additive expressions with '||' in between
		reg2 = andExprCodeGen(node.children[i], resourceTable);
		emit(or, reg1, reg2, reg1);
		resourceTable.releaseRegister(reg2);
	    }	    
	    returnVal = reg1;
	}

	return returnVal;
    }



    //the AND instruction on Sparc is a bitwise AND, which is not always the same as a logical AND
    //to do a logical AND, we basically write code that checks each expresseion in the children array
    //and branches to "fail" if a zero is found.
    //- Akshat
    private String andExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	String reg1 = resourceTable.NOTFOUND;
	String reg2 = resourceTable.NOTFOUND;

	reg1 = relExprCodeGen(node.children[0], resourceTable);
	
	if(node.children.length == 1)
	    returnVal = reg1;
	else{
	    String end1=getLabel();
	    String end2=getLabel();

	    emit(compare, reg1, zero);
	    emit(equal, end1);
	    emit(nop);
	    resourceTable.releaseRegister(reg1);

	    for(int i = 1; i < node.children.length; i++){//any number of additive expressions with '&&' in between
		reg2 = relExprCodeGen(node.children[i], resourceTable);
		emit(compare, reg2, zero);
		emit(equal, end1);
		emit(nop);
		resourceTable.releaseRegister(reg2);
	    }
	    
	    reg1=resourceTable.getRegister();
	    emit(move,"1",reg1);
	    emit(branch,end2);
	    emit(nop);
	    emitLabel(end1);	
	    emit(move,"0",reg1);
	    emitLabel(end2);	
	    returnVal=reg1;
	}	

	return returnVal;
    }

    // It's much more complex than the other methods below
    // both because this starts with branching (and thus also labeling) but also because we have
    // to combine relational operators. I'm thinking the best way to do this is to think in the
    // following manner: a < b > c ---> (a < b) && (b > c).
    private String relExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	String fail = getLabel(); 
	
	String reg1 = "";
	String reg2 = "";
	reg1 = addExprCodeGen(node.children[0], resourceTable);

	if (node.children.length != 1) {	    
	    for(int i = 1; i < node.children.length;i++){//any number of additive expressions with operators in between

		int symbol = ((TerminalNode)node.children[i]).token.symbolNumber;
		reg2 = addExprCodeGen(node.children[i+1], resourceTable);

		//test reg1 vs reg2, branch to fail if fail
		if(symbol == 21) { //operator <
		    emit(compare, reg1, reg2);
		    emit(greaterorequal, fail);
		    emit(nop);
		}
		else if(symbol == 22) { //operator >
		    emit(compare, reg1, reg2);
		    emit(lessorequal,  fail);
		    emit(nop);
		}
		else if(symbol == 23) { //operator <=
		    emit(compare, reg1, reg2);
		    emit(greaterthan, fail);
		    emit(nop);
		}
		else if(symbol == 24) { //operator >=
		    emit(compare, reg1, reg2);
		    emit(lessthan,  fail);
		    emit(nop);
		}
		else if(symbol == 25) { //operator ==
		    emit(compare, reg1, reg2);
		    emit(notequal, fail);
		    emit(nop);
		}
		else if(symbol == 26) { //operator !=
		    emit(compare, reg1, reg2);
		    emit(equal, fail);
		    emit(nop);
		}
	    
		emit(move, reg2, reg1); //move the value of reg2 into reg1, so that the next loop will make correct comparison
		resourceTable.releaseRegister(reg2);
		i++; // need to increment twice to get around the operator
	    }

	    String end = getLabel();
	    emit(move, "1", reg1);
	    emit(branch, end);
	    emit(nop);
	    emitLabel(fail);
	    emit(move, "0", reg1);
	    emitLabel(end);
	}
	
	returnVal = reg1;

 
	return returnVal;
    }

    // should be done but can't test until term is done...
    private String addExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	
	if(node.children.length == 1)
	    returnVal = termCodeGen(node.children[0], resourceTable);
	else{
	    String reg1 = "";
	    String reg2 = "";
	    reg1 = termCodeGen(node.children[0], resourceTable);
	    
	    for(int i = 1; i < node.children.length;i++){//any number of terms with operators in between
		int symbol = ((TerminalNode)node.children[i]).token.symbolNumber;
		reg2 = termCodeGen(node.children[i+1], resourceTable);
		if(symbol == 19) { //operator +		
		    if ((node.children[i-1].type.equals(Symbol.INTSTAR))
			&& (node.children[i+1].type.equals(Symbol.INT))){
			emit(multiply,reg2,"4",reg2);
		    }
		    else  if ((node.children[i-1].type.equals(Symbol.INT))
			      && (node.children[i+1].type.equals(Symbol.INTSTAR))){
			emit(multiply,reg1,"4",reg1);
		    }

		    emit(add, reg1, reg2, reg1); 
		}
		else if(symbol == 20) { //operator -
		    if ((node.children[i-1].type.equals(Symbol.INTSTAR))
			&& (node.children[i+1].type.equals(Symbol.INT))){
			emit(multiply,reg2,"4",reg2);
			emit(sub, reg1, reg2, reg1);
		    }
		    else  if ((node.children[i-1].type.equals(Symbol.INTSTAR))
			      && (node.children[i+1].type.equals(Symbol.INTSTAR))){
			emit(sub, reg1, reg2, reg1);
			emit(divide, reg1, "4", reg1);			
		    }
		    else {
			emit(sub, reg1, reg2, reg1);
		    }
		}
		i++;
	    }
	    returnVal = reg1;
	    resourceTable.releaseRegister(reg2);
	}

	return returnVal;
    }


    // we need the command for % before this method will work!!!!!
    private String termCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	String reg1 = "";
	String reg2 = "";
	reg1 = unaryExprCodeGen(node.children[0], resourceTable);
	returnVal=reg1;

	if(node.children.length != 1){	    
	    for(int i = 1; i < node.children.length;i++){//any number of unary exprs with operators in between
		int symbol = ((TerminalNode)node.children[i]).token.symbolNumber;
		reg2 = unaryExprCodeGen(node.children[i+1], resourceTable);
		if(symbol == 14) { //operator *
		    
		    emit(multiply, reg1, reg2, reg1);
		}
		else if(symbol == 18) { //operator /
		    emit(divide, reg1, reg2, reg1);
		}
		else if(symbol == 16) { //operator %
		    emit(move, reg1, "%o0");
		    emit(move, reg2, "%o1");
		    emit(call,modfunction);
		    emit(nop);
		    emit(move, "%o0", reg1);		    
		}
		i++;
	    }
	    returnVal = reg1;
	    resourceTable.releaseRegister(reg2);
	}
	
	return returnVal;
    }



    // we need to figure out ! before this method will work!!!!
    private String unaryExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	if(node.children.length == 1)
	    returnVal = primaryExprCodeGen(node.children[0], resourceTable);
	else if(node.children.length == 2){
	    int symbol = ((TerminalNode)node.children[0]).token.symbolNumber;
	    if(symbol == 19){ //operator +
		returnVal = unaryExprCodeGen(node.children[1], resourceTable);
	    }
	    else if(symbol == 20){ //operator -
		returnVal = unaryExprCodeGen(node.children[1], resourceTable);
		emit(sub, "%r0", returnVal, returnVal);
	    }
	    else if(symbol == 14){ //operator *
		returnVal = unaryExprCodeGen(node.children[1], resourceTable);
		emit(load, "[" + returnVal + "]", returnVal);
	    }
	    else if(symbol == 17){ //operator !
		String makeone = getLabel();
		String makezero = getLabel();
		returnVal= unaryExprCodeGen(node.children[1], resourceTable);
		emit(compare,returnVal,"0");
		emit(equal,makeone);
		emit(nop);
		emit(move, "0", returnVal);
		emit(branch, makezero);
		emit(nop);
		emitLabel(makeone);
		emit(move, "1", returnVal);
		emitLabel(makezero);		
	    }
	    else if(symbol == 15){ //operator &
		String reg1="";
		String lookupstring="";

		Node pnode=findPrimaryExpr(node.children[1]);
		if ((pnode.children.length==1) || (pnode.children.length==4))
		    lookupstring=pnode.children[0].toString();			 
		String location=resourceTable.lookupStackFrame(lookupstring);
		if (location.equals(resourceTable.NOTFOUND))
		    location = resourceTable.lookupStackFrame(lookupstring+"[0]");

		String offset = location.substring(4,location.length()-1);		

		if (pnode.children.length==4){
		    reg1=expressionCodeGen(pnode.children[2],resourceTable);		    
		    emit(add,"%fp",reg1,reg1);
		    emit(add,offset,reg1,reg1);
		    returnVal=reg1;
		}
		else{
		    reg1=resourceTable.getRegister();
		    emit(add,"%fp",offset,reg1);
		    returnVal=reg1;;
		}

		/*
		  returnVal = resourceTable.getRegister();



		  lookupstring=pnode.children[0].toString()+"["+Integer.toString(((NumNode)pnode.children[2]).value)+"]";
		  else 

		  emit(add, "%fp", offset, returnVal);		*/
	    }

	}

	return returnVal;
    }

    private String primaryExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;

	if(node.children.length==1){ //case of epression, id, num, or real
	    if(node.children[0] instanceof ExpressionNode)
		return expressionCodeGen(node.children[0], resourceTable);
	    else if(node.children[0] instanceof IdentifierNode){		
		String reg = resourceTable.getRegister(node.children[0].toString());

		if (reg.equals(resourceTable.NOTFOUND)){
		    reg = resourceTable.lookupStackFrame(node.children[0].toString()+"[0]");
		    if (!reg.equals(resourceTable.NOTFOUND)){
			String offset = reg.substring(4,reg.length()-1);
			reg=resourceTable.getRegister();
			emit(add, "%fp", offset, reg);		    
		    }
		}

		returnVal = reg;

		if (reg.equals(resourceTable.NOTFOUND)) {
		    if (commons.contains(node.children[0].toString())){
			String reg1=resourceTable.getRegister();  
			String reg2=resourceTable.getRegister();		  
			emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
			emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
			emit(load, "["+reg2+"]",reg2);			
			returnVal=reg2;		    
			resourceTable.releaseRegister(reg1);
		    }
		    else if (commons.contains(node.children[0].toString()+"[0]")) {
			String reg1=resourceTable.getRegister();  
			String reg2=resourceTable.getRegister();		  
			emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
			emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
			returnVal=reg2;		    
			resourceTable.releaseRegister(reg1);			    
		    }
		}



	    }
	    else if(node.children[0] instanceof NumNode){ 
		String reg = resourceTable.getRegister();
		emit(move, new Integer(((NumNode)node.children[0]).value).toString(), reg);
		returnVal = reg;
	    }
		
	}

	else if(node.children.length == 4){ // case of array or function
	    if(node.children[2] instanceof ExpressionNode){

		String temp = resourceTable.lookupStackFrame(node.children[0].toString() + "[0]");		

		if (!temp.equals(resourceTable.NOTFOUND)){
		    String exprReg = expressionCodeGen(node.children[2], resourceTable);
		    emit(multiply, exprReg, "4", exprReg); // to multiply by four so that the offset is ok

		    String tempReg = resourceTable.getRegister();
		    //resourceTable.setRegister(tempReg, node.children[0].toString() + ""
		    temp = temp.substring(4, temp.length()-1);
		    String reg1 = resourceTable.getRegister();
		    emit(add, "%fp",temp,reg1);
		    emit(add, reg1, exprReg, reg1);
		    //		emit(load, temp + "(" + exprReg + ")",tempReg);
		    emit(load, "[" + reg1 + "]", tempReg);
		    resourceTable.releaseRegister(reg1);
		    resourceTable.lookupVariables_Stack("[%fp" + temp + "]"); // gives us ar[0]
		    resourceTable.releaseRegister(exprReg);
		    returnVal = tempReg;
		}
		else if (commons.contains(node.children[0].toString()+"[0]")) {
		    String exprReg = expressionCodeGen(node.children[2], resourceTable);
		    emit(multiply, exprReg, "4", exprReg); // to multiply by four so that the offset is ok

		    String reg1=resourceTable.getRegister();  
		    emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
		    emit(or,reg1,"%lo("+node.children[0].toString()+")",reg1);
		    emit(add,reg1,exprReg,reg1);
		    emit(load, "["+reg1+"]",reg1);
		    returnVal=reg1;		    
		    resourceTable.releaseRegister(exprReg);		    
		
		}


	    }
	    else if(node.children[2] instanceof ArgsExprNode){
		if (node.children[2].children.length>6) {
		    int spcounter=92;
		    for (int i=6;i<Math.max(node.children[2].children.length,6);i++) {			
			String tempReg;
			emit(store,(tempReg=expressionCodeGen(node.children[2].children[i],resourceTable)),"[%sp+"+spcounter+"]");
			spcounter += 4;
			resourceTable.releaseRegister(tempReg);
		    }
		}
		for (int i=0;i<Math.min(node.children[2].children.length,6);i++) {
		    String tempReg;
		    emit(move,(tempReg=expressionCodeGen(node.children[2].children[i],resourceTable)),"%o"+i);
		    resourceTable.releaseRegister(tempReg);
		}
		emit(call,labelTableLookup(((PrimaryExprNode)node).callsignature),"0");
		emit(nop);
		emit(nop);
		returnVal="%o0";
	    }
	}

	return returnVal;
    }


    

    private String expressionCodeGenL(Node node, ResourceTable resourceTable){	
	return orExprCodeGenL(node.children[0], resourceTable);
    }    
    private String orExprCodeGenL(Node node, ResourceTable resourceTable){	
	return andExprCodeGenL(node.children[0], resourceTable);
    }
    private String andExprCodeGenL(Node node, ResourceTable resourceTable){
	return relExprCodeGenL(node.children[0], resourceTable);
    }
    private String relExprCodeGenL(Node node, ResourceTable resourceTable){
	return addExprCodeGenL(node.children[0], resourceTable);
    }
    private String addExprCodeGenL(Node node, ResourceTable resourceTable){
	return termCodeGenL(node.children[0], resourceTable);
    }
    private String termCodeGenL(Node node, ResourceTable resourceTable){
	return unaryExprCodeGenL(node.children[0], resourceTable);
    }
    private String unaryExprCodeGenL(Node node, ResourceTable resourceTable){
	return primaryExprCodeGenL(node.children[0], resourceTable);
    }

    private String primaryExprCodeGenL(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;

	if(node.children.length==1){ //case of epression, id, num, or real
	    if(node.children[0] instanceof ExpressionNode)
		return expressionCodeGenL(node.children[0], resourceTable);
	    else if(node.children[0] instanceof IdentifierNode){		
		String temp=resourceTable.lookupStackFrame(node.children[0].toString());
		if (!temp.equals(resourceTable.NOTFOUND)){		    
		    String outputReg=resourceTable.getRegister();
		    temp = temp.substring(4, temp.length()-1);
		    emit(add,"%fp",temp,outputReg);
		    returnVal=outputReg;		
		}
		else if (commons.contains(node.children[0].toString())){		    
		    String reg1=resourceTable.getRegister();  
		    String reg2=resourceTable.getRegister();		  
		    emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
		    emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
		    returnVal=reg2;		    
		    resourceTable.releaseRegister(reg1);
		} 
	    }
	    
	}		    
	else if(node.children.length == 4){ // case of array or function
	    if(node.children[2] instanceof ExpressionNode){
		String temp = resourceTable.lookupStackFrame(node.children[0].toString() + "[0]");		
		String exprReg = expressionCodeGen(node.children[2], resourceTable);		
		emit(multiply, exprReg, "4", exprReg); // to multiply by four so that the offset is ok		

		if (!temp.equals(resourceTable.NOTFOUND)){
		    temp = temp.substring(4, temp.length()-1);

		    String tempReg = resourceTable.getRegister();

		    //emit(load, tempReg, temp + "(" + exprReg + ")");
		    emit(add,  "%fp",exprReg,tempReg);
		    resourceTable.releaseRegister(exprReg);
		    emit(add, tempReg,temp,tempReg);
		    returnVal = tempReg;
		}
		else if (commons.contains(node.children[0].toString()+"[0]")) {
		    String reg1=resourceTable.getRegister();  
		    String reg2=resourceTable.getRegister();  
		    emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
		    emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
		    emit(add,reg2,exprReg,reg2);
		    returnVal=reg2;		    
		    resourceTable.releaseRegister(exprReg);		    
		    resourceTable.releaseRegister(reg1);		
		}
	    }
	}
	return returnVal;
    }


    void emit(String operation, String operand1, String operand2, String operand3){
	if (operand3==null){
	    if (operand2==null){
		if (operand1==null){//emit without operands
		    filewriter.println("\t"+operation);
		}
		else{//emit with 1 operand
		    filewriter.println("\t"+operation+"\t" + operand1);				    
		}
	    }
	    else {//emit with 2 operands
		filewriter.println("\t"+operation+"\t" + operand1 + ", " + operand2 );				    
	    }
	    
	}
	else {//emit with 3 operands
	    filewriter.println("\t"+operation+"\t" + operand1 + ", " + operand2 + ", " + operand3 );				    
	    
	}
	    
	return;
    }

    void emit(String operation, String operand1, String operand2){
	emit(operation, operand1,operand2,null);
	return;
    }

    void emit(String operation, String operand1){
	emit(operation, operand1,null,null);
	return;
    }

    void emit(String operation){
	emit(operation, null,null,null);
	return;
    }

    String labelTableLookup(String signature){
	if( signature.substring(0,4).equals("main"))
	    return "main";
	return (String)labelTable.get(signature);
    }

    Node findPrimaryExpr(Node node){
	if (node instanceof PrimaryExprNode){
	    if ((node.children.length==1) && (node.children[0] instanceof ExpressionNode))
		return findPrimaryExpr(node.children[0]);
	    else
		return node;
	}
	else if ((node.children!=null) && (node.children.length > 0))
	    return findPrimaryExpr(node.children[0]);
	else 
	    return new PrimaryExprNode(0);
    }

}
import java.util.ArrayList;

/*
  Akshat Singhal, Chris Fry
  Context.java
  class for context frame for the semantic analysis 
  of the Akshat and Chris Compiler (ACC)

  SymbolTable:
  (symbolTable)
  |---------|
  |-Context1|-->context1 is special, it is never popped off, it has all the top level var and fun decl's.
  |-Context2| 
  |-Context3| 
  |---------|

  Context:
  (symbols)
  |--------| 
  |-Symbol1| 
  |-Symbol2|
  |-Symbol3|
  |--------|

*/

public class Context{
    ArrayList symbols;
    
    public Context(){
	//constructor for Context
	symbols = new ArrayList();

    }

    public ArrayList add(Symbol symbol){
	//	System.out.println("tried to add symbol [" + symbol.identifier + "] of linenumber " + symbol.linenumber);
	ArrayList returnerrors = new ArrayList();
	Symbol checksymbol;
	if (symbol instanceof FunSymbol) {
	    checksymbol=getFunbySig(((FunSymbol)symbol).signature);		
	    //	    System.out.println("found symbol: " + checksymbol);
 	    if (!(checksymbol instanceof ErrorSymbol))
		returnerrors.add("[" + symbol.linenumber + "]:duplicate function declaration for " + symbol.identifier);
	}
	else if (symbol instanceof VarSymbol)  {
	    checksymbol=getVar(symbol.identifier);		
	    //	    System.out.println("found symbol: " + checksymbol);
	    if (!(checksymbol instanceof ErrorSymbol))
		returnerrors.add("[" + symbol.linenumber + "]:duplicate variable/parameter declaration for " + symbol.identifier);
	    
	}	
	else
	    returnerrors.add("Compiler error: invalid symbol entered into table");
	symbols.add(symbol);
	return returnerrors;
    }


    // returns type if symbol is in context, "" if not
    public Symbol getVar(String identifier){
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){	    
	    if((temp = (Symbol)symbols.get(i)).identifier.equals(identifier) && 
	       (temp instanceof VarSymbol))
		return temp;
	    /*	    else
		    System.out.println("tried to compare " + temp);*/
	}

	temp = new ErrorSymbol();
	return temp;
    }


    public Symbol getFunbySig(String signature){
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && ((FunSymbol)temp).signature.equals(signature)) {
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }

    public Symbol getFunbyIdent(String identifier){
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && (temp.identifier.equals(identifier)) ) {
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }
    

    public Symbol removeFunBySig(String signature){
	//tries to remove a function by looking for it by signature
	//returns an errorsymbol if not found
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && ((FunSymbol)temp).signature.equals(signature)) {
		symbols.remove(i);
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }


    /*
      //removeFunByIdent is a little ambiguous and possibly useless
    public Symbol removeFunbyIdent(String identifier){
	//tries to remove a function by looking for it by identifier, 
	//removes the first such function it can find
	//returns an errorsymbol if not found
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && (temp.identifier == identifier)) {
		symbols.remove(i);
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }
    */


    public Symbol removeVar(String identifier){
	//tries to remove a variable by looking for it by signature
	//returns an errorsymbol if not found
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    if((temp = (Symbol)symbols.get(i)).identifier.equals(identifier) && 
	       (temp instanceof VarSymbol)){
		symbols.remove(i);
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }



}
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
					newToken = new FloatToken(currentToken, symbolNumber, getLineCount());//modified in phase 3
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
import java.util.*;
import java.io.*;

public class LexerTest{
    
    public static void main(String args[]) 
    {
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
	    }
	Lexer lexer = new Lexer(reader);
	
	while(lexer.hasMoreTokens()){
	    System.out.print( ((Token)lexer.getNextToken()).toString() + " ");
	}
	
	System.out.print("\n");
	
	reader.close();
	}
	catch (FileNotFoundException f){
	    System.out.println("File " + args[0]  + "not found");
	}
	catch (IOException f){
	    System.out.println("I/O Exception");
	}
	

	
	
		

	
    }
}
import java.util.*;
public abstract class Node{
    //----------------------
    // New! Semantic Analysis attributes for the Node class.
    static final String INT = "int";
    static final String FLOAT = "float";
    static final String INTSTAR = "int*";
    static final String FLOATSTAR = "float*";
    static final String VOIDSTAR = "void*";
    static final String ERROR = "error";

    String type;
    boolean lvalue;
    boolean rvalue;
    //----------------------------

    Node[] children;
    String nodeStringValue;
    int numChildren;
    int linenumber;


    public String toString(){
	String returnstring = "( " + nodeStringValue;	
	for (int i=0;i<children.length;i++)
	    if (children[i] != null)
		returnstring += " " + children[i].toString();
	returnstring += " )";
	return returnstring;
    }// end of toString()

    public String printTree(){
	String returnstring = "<" + nodeStringValue + ">";
	String currentstring;
	if (children != null) {
	    for (int i=0;i<children.length;i++){
		if (children[i] != null) {
		    currentstring=children[i].printTree();    
		    if (currentstring.lastIndexOf("null") != -1)
			returnstring += "\n-|" + (String)(children[i].toString()).replaceAll("\n","\n-");
		    else
			returnstring += "\n-|" + (String)(currentstring).replaceAll("\n","\n-");
		}
	    }
	    
	}
	return returnstring;
    }//end of printTree
    
    public Node reducedNode(){
	if (children != null){
	    if ((children.length == 1)  && 
		((this instanceof OrExprNode) || (this instanceof AndExprNode) ||
		 (this instanceof RelExprNode) || (this instanceof AddExprNode) || (this instanceof TermNode) ||
		 (this instanceof UnaryExprNode) || (this instanceof PrimaryExprNode)  
		 )) // new reducedNode() only reduces single children nodes for the descendants of ExpressionNode
		{
		//System.out.println("reduced " + this + " to "  + children[0]);
		return children[0].reducedNode();
	    }
	    for (int i=0;i<children.length;i++){ 
		if (children[i] != null) {
		    //System.out.println("children[i]:" + children[i]);
		    children[i]=children[i].reducedNode();
		}
	    }
	}
	return this;
    }// end of reducedNode()

    public Node reduceChildren(){
	if (children != null){
	    for (int i=0;i<children.length;i++){ 
		if (children[i] != null) {
		    //System.out.println("children[i]:" + children[i]);
		    children[i]=children[i].reducedNode();
		}
	    }
	}
	return this;
    }// end of reduceChildren()   

    public Node[] getChildren(){
	return children;
    }

    public int getNumChildren(){
	return numChildren;
    }
}

class TerminalNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public TerminalNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }

    public int getSymbolNumber(){
	return token.symbolNumber;
    }
}

class ErrorNode extends Node{
    public ErrorNode(){
	children=new Node[0];
	numChildren = 0;
	nodeStringValue="Error";	
    }
}

class ProgramNode extends Node{
    public ProgramNode(int numChildren){
	children=new Node[numChildren];
	this.numChildren = numChildren;
	nodeStringValue="program";
    }  
}

class IdentifierNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public IdentifierNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.lexeme;
    }

    public String toString(){
	return nodeStringValue;
    }
}


class IntNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public IntNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class FloatNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public FloatNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class VoidNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public VoidNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class IfNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public IfNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class ElseNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public ElseNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class WhileNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public WhileNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class ForNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public ForNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class ReturnNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public ReturnNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class VarDeclNode extends Node{
    
    public VarDeclNode(int numChildren){
	this.numChildren = numChildren;	
	children=new Node[numChildren];
	nodeStringValue="var-declaration";
    }  
}

class RealNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public float value;
    public RealNode(FloatToken token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
	this.value = token.getValue();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class NumNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public int value;
    public NumNode(IntegerToken token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
	this.value = token.getValue();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class FunDeclNode extends Node{
    public ArrayList allDeclarations;
    public ArrayList allCalls;
    public int maxcallsize;
    public int nlocals;
    public int stackSize;
    public String signature;
    
    public FunDeclNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="fun-declaration";
    }  
}

class ParamsNode extends Node{
    public ParamsNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="params";
    }  
}

class ParamNode extends Node{
    
    public ParamNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="param";
    }  
}

class CompStmtNode extends StatementNode{    
    public CompStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="compound-statement";
    }  
}

class StmtListNode extends Node{
    public StmtListNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="statement-list";
    }  
}

class LocalDecsNode extends Node{
    public LocalDecsNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="local-declarations";
    }  
}

abstract class StatementNode extends Node{
   
}

class ExprStmtNode extends StatementNode{
    public ExprStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="expression-statement";
    }  
}
//OptExprNode changed to ExprNode to generic-ise the idea
class ExprNode extends Node{
    public ExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="optional-expression";
    }  
}

class IfStmtNode extends StatementNode{
    public IfStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="if-statement";
    }  
}

class IfStartNode extends Node{
    public IfStartNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="if-start";
    }  
}

class IfRemNode extends Node{
    public IfRemNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="if-remainder";
    }  
}

class WhileStmtNode extends StatementNode{
    public WhileStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="while-statement";
    }  
}

class ForStmtNode extends StatementNode{
    public ForStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="for-statement";
    }  
}

class ReturnStmtNode extends StatementNode{
    public ReturnStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="return-statement";
    }  
}

class ExpressionNode extends Node{
    public ExpressionNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="expression";
    }  
}

class OrExprNode extends Node{
    public OrExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="or-expr";
    }  
}

class AndExprNode extends Node{
    public AndExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="and-expr";
    }  
}

class RelExprNode extends Node{
    public RelExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="rel-expr";
    }  
}

class AddExprNode extends Node{
    public AddExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="add-expr";
    }  
}

class TermNode extends Node{
    public TermNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="term";
    }  
}


class UnaryExprNode extends Node{
    public UnaryExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="unary-expr";
    }  
}

class PrimaryExprNode extends Node{
    boolean isCall;
    String callsignature;
    public PrimaryExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="primary-expr";
	isCall=false;
	callsignature="";
    }  
}

class ArgsExprNode extends Node{
    public ArgsExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="args";
    }  
}




import java.util.*;

public class ParseStack{
    private ArrayList stack;
    private int stackPointer;
    boolean diag4=false,diag1=false, diag2=false, diag3=false;
    //diag2 are important messages that should be outputted if stackPointer is being a bad boy

    Lexer lexer;
    
    public String toString(){
	String returnstring="[ ";
	for( int i = 0; i <stack.size(); i++)
	    returnstring += "<" + i + ">" + stack.get(i) + ",\n";
	return returnstring + " ]";
    }
    
    public String printTree(){
	String returnstring="";
	for( int i = 0; i <stack.size(); i++)
	    if (stack.get(i) instanceof Node)
		 returnstring += ((Node)stack.get(i)).printTree();
	    else
		 returnstring += stack.get(i).toString();
	return returnstring;
    }

    public ParseStack(Lexer lexer){
	this.lexer=lexer;
	stack = new ArrayList();
	stackPointer=0;

    }

    public Object getNext(){
	//getNext() gets the next item on stack, pointed to by the stackPointer:
	//- if stack is empty, it gets a token from Lexer and returns it, setting SP = 1
	// **(in this case, the newly returned element is put on stack) 
	//- if SP is pointing to an existing element of stack, that element is returned, setting SP=SP+1
	//
	//- if SP is pointing right above stack, a new token from Lexer is returned, setting SP=SP+1
	// **(in this case, the newly returned element is put on stack) 
	//
	// if SP points to something that is way above stack, it returns an EOFToken().
	

	if (stack.size()==0){
	    //System.out.println("I read " + lexer.peekNextToken());	    
	    if (lexer.hasMoreTokens()){
		stack.add(lexer.getNextToken());
		stackPointer++;
		return stack.get(stackPointer-1);
	    }
	    else
		return new EOFToken();
	}
	else if (stackPointer < stack.size()){ 	    
	    return stack.get(stackPointer++);
	}
	else if (stackPointer == stack.size()) // execute this case only if 
	    //stackpointer points to element right on top of stack, i.e. asks for a new element
	    { 
		if (lexer.hasMoreTokens()){
		    stack.add(lexer.getNextToken());
		    return stack.get(stackPointer++);
		}
		else
		return new EOFToken();
	    }
	else{//execute this case if stackpointer is point somewhere absurdly above the top of stack
	    if (diag2)
		System.out.println("**Diag warning: invalid stack ref: getNext(), SP=" + 
				   stackPointer + " for a stack of size " + stack.size());
	    return new EOFToken();
	}
    }

    public Object get(int index){
	return stack.get(index);
    }
    
    //testing purposes only
    public Object peekNext(){
	return stack.get(stackPointer);
    }

    public void push(Object o){
	stack.add(o);
    }

    public Object pop(){
	return stack.remove(stack.size()-1);     	
    }
    public Object top(){
	return stack.get(stack.size()-1);     	
    }

    public Object remove()
    {
	//remove() removes the stack element at stackPointer, and decreases stackPointer by 1
	if (diag3)
	    System.out.println("--Removed " + stack.get(stackPointer -1));

	if (stackPointer<=stack.size() && stackPointer >0)
	    return stack.remove(--stackPointer);
	else{//execute this case if stackpointer is point somewhere absurdly above the top of stack
	    if (diag2)
		System.out.println("**Diag warning: invalid stack ref: remove(" + 
				   stackPointer + ") for a stack of size " + stack.size());
	    return new EOFToken();
	}

    }

    public void insert(Object o)
    {
	//insert() adds given Object to stack at stackPointer, moving other elements up,
	// but DOES NOT increment stackPointer.
	if (stackPointer<=stack.size() && stackPointer >=0)
	    stack.add(stackPointer, o);
	else{//execute this case if stackpointer is point somewhere absurdly above the top of stack
	    if (diag2)
		System.out.println("**Diag warning: invalid stack ref: insert at(" + 
				   stackPointer + ") for a stack of size " + stack.size());
	    stack.add(stack.size(),o);
	}

    }
    

    public int getStackPointer(){
	return stackPointer;	
    }
    public int getSize(){
	return stack.size();	
    }
    public void  setStackPointer(int s){
	stackPointer=s;
    }
    public void incStackPointer(){
	stackPointer++;
    }
    public void decStackPointer(){
	stackPointer--;
    }

}
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
	    stack.remove(); // get rid of the ')'
	    ifStartNode.children[1] = (ExpressionNode)stack.remove();
	    stack.remove(); // get rid of the '('
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
	    succ = parseExpression(); // why do we do Expression instead of ExprStmt like the others???
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
public class ResourceTable{
    final static String NOTFOUND="<notfound>";
    
    String[][] gRegTable;
    String[][] lRegTable;
    String[][] iRegTable;
    String[][] oRegTable;
    String[][] stackFrameTable;
    int stackFrameSize;
    int incomingArgsRangeBegin;// range 'begin' and 'end' for begin and end of stack frame space
    int incomingArgsRangeEnd;
    int tempSpaceRangeBegin;
    int tempSpaceRangeEnd;
    int outgoingArgsRangeBegin;
    int outgoingArgsRangeEnd;
    int localVarsRangeBegin;
    int localVarsRangeEnd;
  
    CodeGenerator codeGenerator;
    public ResourceTable(int stackFrameSize, int nargs, String[] args, int nlocals, String[] locals, int nOutgoingArgs, CodeGenerator codeGenerator){
	this.codeGenerator = codeGenerator;
	this.stackFrameSize = stackFrameSize;
	gRegTable = new String[4][2];//g's: not preserved thru calls
	lRegTable = new String[7][2];//preserved thru calls
	iRegTable = new String[6][2];//incoming params
	oRegTable = new String[6][2];//outgoing params, not preserved thru calls
	stackFrameTable = new String[(stackFrameSize/4)+Math.max(nargs,6)][2];	
	//what do the magic numbers mean?:
	//4: size of int/pointer
	//nargs: number of this function's incoming parameters
	for (int i=0;i<4;i++){
	    gRegTable[i][0]="%g"+i;
	    gRegTable[i][1]="<free>";
	}

	for (int i=0;i<7;i++){
	    lRegTable[i][0]="%l"+i;
	    lRegTable[i][1]="<free>";
	}

	for (int i=0;i<6;i++){
	    iRegTable[i][0]="%i"+i;
	    if (nargs>i)
		iRegTable[i][1]=args[i];
	    else
		iRegTable[i][1]="<free>";
	}

	for (int i=0;i<6;i++){
	    oRegTable[i][0]="%o"+i;
	    oRegTable[i][1]="<free>";
	}

	//initializing the stack frame of the current method
	//remember: sp+x == (fp-size)+x
	//remember: 17=68/4

	
	for (int i=0;i<17;i++){
	    stackFrameTable[i][0]="[%sp+"+ (i*4) + "]";
	    stackFrameTable[i][1]="<systemreserved>";
	}
	

	outgoingArgsRangeBegin=17;
	outgoingArgsRangeEnd=17+Math.max(nOutgoingArgs,6);

	for (int i=outgoingArgsRangeBegin;i<outgoingArgsRangeEnd;i++){
	    stackFrameTable[i][0]="[%sp+"+ (i*4) + "]";
	    stackFrameTable[i][1]="<callarg"+(i-17)+">";
	}	

	//	System.out.println("the number of locals is: " + nlocals);
	tempSpaceRangeBegin=outgoingArgsRangeEnd;
	tempSpaceRangeEnd=(stackFrameSize/4)-nlocals;
	//    	System.out.println("tempSpaceRangeBegin=: " + tempSpaceRangeBegin);
	//    	System.out.println("tempSpaceRangeEnd=: " + tempSpaceRangeEnd);

	for (int i=tempSpaceRangeBegin;i<tempSpaceRangeEnd;i++){
	    stackFrameTable[i][0]="[%sp+"+ (i*4) + "]";
	    stackFrameTable[i][1]="<temp>";
	}


	localVarsRangeBegin=tempSpaceRangeEnd;
	localVarsRangeEnd=stackFrameSize/4;
	int localdecscounter=0;
	for (int i=localVarsRangeBegin;i<localVarsRangeEnd;i++){
	    stackFrameTable[i][0]="[%fp-"+ (stackFrameSize-i*4) + "]";
	    if (nlocals>localdecscounter)
		stackFrameTable[i][1]="<reserved_"+ locals[localdecscounter++]+">";
	}
    
	incomingArgsRangeBegin=(stackFrameSize/4);
	incomingArgsRangeEnd=((stackFrameSize/4)+ Math.max(nargs,6));
	int argscounter=0;
	for (int i=(stackFrameSize/4);i<((stackFrameSize/4)+ Math.max(nargs,6));i++){//getting the passed args
	    stackFrameTable[i][0]="[%fp+" + ((i*4)-stackFrameSize+68) + "]";
	    if (nargs>argscounter)
		stackFrameTable[i][1]=args[argscounter++];
	    else
		stackFrameTable[i][1]="<free>";
	}
	
	//the only place where you'd really like to use the [%fp+x] references would be when
	//getting passed arguments from the function calling this one.


	//[%sp+x] references are used for when accessing stuff within this function's stack frame.
	//when you say "save", the magic of register window happens:
	//-space is reserved in memory for this functions stack frame. space looks like this:
	// [sp+0]:
	// .
	// . [this area for storing register window between calls]
	// .
	// [sp+60]:
	// [sp+64]: special useless (for us)register
	// [sp+68]: parameter 1
	// .
	// . [this area has outgoing parameters]
	// .
	// [sp+88]: parameter 6 // stuff till here is stored in reg's %i0-%i5
	// [sp+92]: parameter 7
	// .
	// . [this area has extra outgoing parameters]
	// .
	// [sp+92+(4*x)]: outgoing parameter 6+x // where x: number of outgoing params beyond 6
	// .
	// . [this area has 'temporaries']
	// .
	// [fp-(4*L)]: local variable number 1 // where L: number of local variables	
	// .
	// . [this area has local variables]
	// .
	// [fp]
	// .
	// . [data we don't look at]
	// .
	// [fp+68]
	// .
	// . [this area has incoming params]
	// .
	// [fp+92]
	// .
	// . [this area has incoming params]
	// .
	// [fp+92+(4*x)]: incoming parameter 6+y // where y: number of incoming params beyond 6
	
	//print();
    }

    //get the frame pointer reference for incoming params
    String getPassedArgFP(int argnum){
	if (stackFrameTable.length > incomingArgsRangeBegin+argnum-1)
	    return stackFrameTable[incomingArgsRangeBegin+argnum][0];
	else
	    return NOTFOUND;//how to deal with errors here? what would cause an error?
    }
    
    // return an open register which contains 'id', if in reg. already, return that register
    // if in memory, allocate a register for it, load the val that register and return that register
    String getRegister(String id){
	String returnVal;
	String stackFrameRef;
	if (!(returnVal=lookupRegisters(id)).equals(NOTFOUND))
	    return returnVal;
	else if (!(stackFrameRef=lookupStackFrame(id)).equals(NOTFOUND)){	    
	    returnVal=getRegister();
	    setRegister(returnVal, id);
	    codeGenerator.emit (codeGenerator.load,stackFrameRef,returnVal);
	    return returnVal;
	}	
	else
	    return NOTFOUND;
    }
    

    // returns the first free register. If there are no free register, frees %l0
    // by storing it's val, then returns %l0
    public String getRegister(){
	String returnVal=lookupRegisters("<free>");
	if (returnVal.equals(NOTFOUND)){	    
	    //put an l variable into the stack frame and release its register
	    String stackFrameRef=lookupStackFrame(lookupVariables_Registers("%l0"));
	    codeGenerator.emit(codeGenerator.store,"%l0",stackFrameRef);
	    lRegTable[0][1]="<free>";
	    returnVal="%l0";
	}	
	setRegister(returnVal, "<taken>");
	return returnVal;
    }


    private void setRegister(String reg, String id){

	for (int i=0;i<gRegTable.length;i++)
	    if (gRegTable[i][0].equals(reg))
		gRegTable[i][1]=id;


	for (int i=0;i<lRegTable.length;i++)
	    if (lRegTable[i][0].equals(reg))
		lRegTable[i][1]=id;

	for (int i=0;i<iRegTable.length;i++)
	    if (iRegTable[i][0].equals(reg))
		iRegTable[i][1]=id;


	for (int i=0;i<oRegTable.length;i++)
	    if (oRegTable[i][0].equals(reg))
		oRegTable[i][1]=id;

	return;
    }


    public void releaseRegister(String reg){
	setRegister(reg,"<free>");
	return;
    }

    // replaces a spot reserved for it, with it's actual instance.
    public void declareLocalVar(String id){
	for(int i=0;i<stackFrameTable.length;i++)
	    if (stackFrameTable[i][1].equals("<reserved_"+id+">"))
		stackFrameTable[i][1]=id;	    
    }



    public void print(){

	System.out.println("---------------------------------------------------------");
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<gRegTable.length;i++)
	    System.out.println(" " + i + ": " +gRegTable[i][0] + " : " + gRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<lRegTable.length;i++)
	    System.out.println(" " + i + ": " +lRegTable[i][0] + " : " + lRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<iRegTable.length;i++)
	    System.out.println(" " + i + ": " +iRegTable[i][0] + " : " + iRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<oRegTable.length;i++)
	    System.out.println(" " + i + ": " +oRegTable[i][0] + " : " + oRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<stackFrameTable.length;i++)
	    System.out.println(" " + i + ": " +stackFrameTable[i][0] + " : " + stackFrameTable[i][1]);
	System.out.println("---------------------------------------------------------");
    }



    // looks for an id in all registers    
    String lookupRegisters(String id){	    
	String returnLocation=NOTFOUND;
    

	returnLocation=getLocation(id,lRegTable);
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;
	
	returnLocation=getLocation(id,iRegTable);
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;

	/*
	returnLocation = getLocation(id,gRegTable);    
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;

	returnLocation=getLocation(id,oRegTable);
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;
	*/
	return returnLocation;
    }

    // looks for 'id' in the stack frame
    String lookupStackFrame(String id){							
	return getLocation(id,stackFrameTable);
    }

    // looks for the variable stored in 'location'
    String lookupVariables_Registers(String location){
	String returnVariable=NOTFOUND;
    
	returnVariable=getVariable(location,lRegTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	returnVariable = getVariable(location,gRegTable);    
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	returnVariable=getVariable(location,iRegTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	returnVariable=getVariable(location,oRegTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	return returnVariable;
    }

    String lookupVariables_Stack(String location){
	String returnVariable = NOTFOUND;
	
	returnVariable=getVariable(location,stackFrameTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	return returnVariable;
    }

    // return the location in the given table (one of the above tables presumably)
    // NOTFOUND otherwise
    String getLocation(String id, String[][] table){
	for (int i=0;i<table.length;i++)
	    if (table[i][1].equals(id))
		return table[i][0];
	return NOTFOUND;
    }


    // return the variable stored in the given location
    // NOTFOUND otherwise
    private String getVariable(String location, String[][] table){
	for (int i=0;i<table.length;i++)
	    if (table[i][0].equals(location))
		return table[i][1];
	return NOTFOUND;	
    }

    /*  
    private boolean backupRegs(ResourceTable resourceTable){
	int i = 0;
	
	for(int i = 0; i< iRegTable.length; i++){
	    ;
	}

	return true;
    }
    */
}
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
/* Symbol.java */
/* Akshat Singhal and Chris Fry */
/* this is the symbol classes of the Akshat and
   Chris Compiler (ACC) */



public abstract class Symbol{
    final static String INT = "int";
    final static String FLOAT = "float";
    final static String INTSTAR = "int*";
    final static String FLOATSTAR = "float*";
    final static String VOIDSTAR = "void*";
    final static String ERROR = "error";

    String identifier;
    String type;
    boolean lvalue;
    int linenumber;
}

class VarSymbol extends Symbol{
    int arraylength;    

    public VarSymbol(VarDeclNode varDeclNode){
	this.linenumber=varDeclNode.linenumber;
	this.lvalue=true;
	Node[] children = varDeclNode.getChildren();	
	if (varDeclNode.children[1].toString().equals("asterisk")) {
	    this.type = children[0].toString() + "*";
	    this.identifier = children[2].toString();	    
	    this.arraylength=0;
	}
	else {
	    this.type = children[0].toString();
	    this.identifier = children[1].toString();	    
	    if (children.length==3) {
		this.type=this.type+"*";
		this.arraylength = ((NumNode)children[2]).value;
		this.lvalue=false;
	    }
	}
    }

    public VarSymbol(ParamNode paramNode){
	this.linenumber=paramNode.linenumber;
	this.lvalue=true;
	Node[] children = paramNode.getChildren();
	    if (children[1].toString().equals("asterisk")) {
		this.type = children[0].toString() + "*";
		this.identifier = children[2].toString();	    
		this.arraylength=0;
	    }
	    else {
		this.type = children[0].toString();
		this.identifier = children[1].toString();	    
		if (children.length==3){
		    this.type=this.type+"*";
		    this.arraylength = ((NumNode)children[2]).value;
		    this.lvalue=false;
		}
	    }	    	
    }

    public String toString(){
	return type+ " " + identifier;
    }
}

class FunSymbol extends Symbol{
    String signature;

    public FunSymbol(FunDeclNode funNode){
	ParamsNode params = new ParamsNode(0);
	this.linenumber=funNode.linenumber;

	
	if (funNode.children.length==5){
	this.type = funNode.children[0].toString()+"*";		
	this.identifier = funNode.children[2].toString();

	params = ((ParamsNode)funNode.children[3]);

	}
	else {
	    this.type = funNode.children[0].toString();
	    this.identifier = funNode.children[1].toString();
	    
	    params = ((ParamsNode)funNode.children[2]);	
	}


	String sig;
	sig =  "" + identifier ;		

	for(int i = 0; i < params.getNumChildren(); i++){
	    if (params.children[i] instanceof VoidNode)
		sig +=  "_" + "void";// not this isn't right yet
	    else {		
		if (params.children[i]!=null && params.children[i].children.length==3)
		    sig +=  "_" + params.children[i].children[0] + "*";
		else
		    sig +=  "_" + params.children[i].children[0] ;
	    }

	}
	this.signature = sig;
	funNode.signature=sig;
    }
    
    public String toString(){
	return type+"_"+signature;
    }
}


    
class ErrorSymbol extends Symbol{
    final int SYMBOLNOTFOUND = 0;
    int errorType=-1;
    String[] errorTypes=new String[4];

    public ErrorSymbol(){
	identifier = "error";
	type = ERROR;
    }
    
    public ErrorSymbol(int reason) {
	errorTypes[0]="Symbol Not Found";
	this.errorType = reason;
	identifier = "error";
	type = ERROR;
    }

    public String toString(){
	return identifier;
    }

}

class CallSymbol extends Symbol{

    public CallSymbol(PrimaryExprNode node){
	String identifier;
	String type;
	boolean lvalue;
	int linenumber;
    }  
}

import java.util.ArrayList;

/*
  Akshat Singhal, Chris Fry
  SymbolTable.java
  symbol table for the semantic analyzer 
  of the Akshat and Chris Compiler (ACC)
*/

// need to put symbol numbers in here too

public class SymbolTable{
    ArrayList symbolTable;
    int stackpointer;
    ArrayList savedContexts;
    public ArrayList calls;
    public SymbolTable(){
	symbolTable = new ArrayList();
	savedContexts = new ArrayList();
	stackpointer = 0;
    }
    public void removeTopContext(){
	if (symbolTable.size()>1){
	    savedContexts.add(symbolTable.remove(symbolTable.size()-1)); 
	    stackpointer--;
	}
	else
	    System.out.println("compiler error: error in checking compound statement scopes ");
    }

    public void addContext(Context frame){
	symbolTable.add(frame);
	stackpointer++;
    }


    public void clearContexts(){
	while (symbolTable.size()>1){
	    savedContexts.add(symbolTable.remove(symbolTable.size()-1)); 
	    stackpointer--;
	}
    }

    /*
    // returns the type of identifier, if it's in the symbolTable, "" otherwise
    public String getSymbolType(String identifier){
	int i = stackpointer-1;
	String temp;
	while(i>=0){
	    if ((temp = ((Context)symbolTable.get(i)).getType(identifier)) != "")
		return temp;   
	    else
		i--;
	}

	return "";
    }
    */


    public Symbol getCurrentFunctionSymbol(){
	Context mainContext = ((Context)symbolTable.get(0));
	for (int i=mainContext.symbols.size()-1;i>=0;i--)	    
	    if (mainContext.symbols.get(i) instanceof FunSymbol)
		return ((Symbol)mainContext.symbols.get(i));
	return new ErrorSymbol();
    }

    public Symbol getSymbol(String identifier){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getVar(identifier);
	    if (temp instanceof ErrorSymbol)
		temp = currentContext.getFunbyIdent(identifier);
	    
	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }

    public Symbol getFunbySig(String signature){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getFunbySig(signature);	    

	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }


    public Symbol getFunbyIdent(String identifier){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getFunbyIdent(identifier);	    

	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }


    public Symbol getVar(String identifier){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getVar(identifier);	    

	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }


}
import java.util.*;
import java.io.*;
/*
 *
 ACC - Akshat and Chris Compiler for C
 - acc is the main class for the compiler.At this point, 
 it just calls the Lexer on the given input and prints out tokens.
 10/3/2005
 *
*/
public class TestLexer
{
    
    public static void main(String args[]) 
    {
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
	    }
	Lexer lexer = new Lexer(reader);
	
	while(lexer.hasMoreTokens()){
	    Token temp = (Token)lexer.getNextToken();
	    System.out.println(temp.toString() + " " + "symbolNumber: " + temp.symbolNumber);
	}

	System.out.print("\n");

	reader.close();
	}
	catch (FileNotFoundException f){
	    System.out.println("File " + args[0]  + "not found");
	}
	catch (IOException f){
	    System.out.println("I/O Exception");
	}
	

	
	
		

	
    }
}
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
	this.lexeme=lexeme;
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
import java.util.*;
import java.io.*;
/*
 *
 ACC - Akshat and Chris Compiler for C
 - acc is the main class for the compiler. At this point, 
 it just calls the Lexer on the given input and prints out tokens.
 10/3/2005
 *
*/
public class acc
{
    
    public static void main(String args[]) 
    {
	String filename="a.out.s";
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
		filename=args[0]+".s";
	    }
	Lexer lexer = new Lexer(reader);
	Parser parser = new Parser(lexer);
	Node syntaxTree=parser.parse();
	SemanticAnalyzer semAnalyzer = new SemanticAnalyzer(syntaxTree);
	ArrayList errors1 = semAnalyzer.checkProgram();
	ArrayList errors = parser.getErrors();
	if ((errors1.size() == 0) && (errors.size()==0)){
	    System.out.println("Program is free of errors");
	    CodeGenerator codegen1= new CodeGenerator(syntaxTree,filename);
	}
	else
	    System.out.println("Errors found in program:");
	
	if(syntaxTree instanceof ErrorNode){
	    for(int i = 0;i<errors.size(); i++)
		System.out.println(errors.get(i));
	    System.out.print("\n");
	}

	for(int i = 0;i<errors1.size(); i++)
	    System.out.println(errors1.get(i));
	
	    System.out.print("\n");
	

	    reader.close();
	}
	catch (FileNotFoundException f){
	    System.out.println("File " + args[0]  + "not found");
	}
	catch (IOException f){
	    System.out.println("I/O Exception");
	}
	

	
	
		

	
    }
}
//Good work, Chris. I like the code. We'll blast this one. *Bob, the builder.* CAN WE FIX IT? *Bob the builder* YES WE CAN!
//--------------------------------------------------------
//testing, testing, testing : I feel like writing a number of small test case programs to see what works and doesn't work yet.
//--------------------------------------------------------
//Concern 1: we might need codegenL() functions for a few of the expresioncodegenL()'s children because we need to be 
//able to return a pointer if the user says *(x+12)=10; (which I guess is the same as saying x[12]=10;), and currently our
//codegens won't do that.
//--------------------------------------------------------
//Concern 2: we still haven't compiled a single output file in gcc, and that is a little scary. Let's do that first.
//--------------------------------------------------------
//Concern 3: more of a reminder, but we need to take care of .commons too
//--------------------------------------------------------
//Q:what is the return type of codegen(). is every codegen() supposed to return a register, or is only expr codegen supposed to return a register?
//Akshat:codegen() returns a register when it needs to. No registers are returned for levels statement and above. Expressions will return a register, but I don't think expression-statements will.
//That, or we can just start assigning temporary variable names to every single return from add/term expression codegens,
//and that would enables us to put stuff on the stack frame if more registers are needed.

import java.util.*;
import java.util.Hashtable;
import java.io.*;

public class CodeGenerator {
    Node syntaxTree;

    int registerCount;
    int globalLabelCounter;
    Hashtable labelTable= new Hashtable();
    ArrayList commons = new ArrayList(); //list of common, i.e. global variable declarations

    final String dotsection = ".section";
    final String dottext = "\".text\"";
    final String dotalign = ".align";
    final String dotglobal = ".global";
    final String dotcommon = ".common";
    final String prologue = "!#PROLOGUE#";
    final String save = "save";
    final String stackpointer = "%sp";
    final String ret = "ret";
    final String restore = "restore";
    final String store = "st";
    final String load = "ld";
    final String add = "add";
    final String sub = "sub";
    final String move = "mov";
    final String multiply = "smul";
    final String call = "call";
    final String nop = "nop";
    final String divide = "sdiv";
    final String lessthan = "bl";
    final String greaterthan = "bg";
    final String lessorequal = "ble";
    final String greaterorequal = "bge";
    final String equal = "be";
    final String notequal = "bne";
    final String and = "and";
    final String or = "or";
    final String branch = "b";
    final String compare = "cmp";
    final String zero = "0";
    final String modfunction = ".rem";
    final String sethi= "sethi";
    
    final String INTSIZE="4";
    final String POINTERSIZE="4";
    PrintWriter filewriter;

    public CodeGenerator(Node syntaxTree, String filename){

	this.syntaxTree = syntaxTree;

	try{
	    filewriter = new PrintWriter (new FileWriter(new File(filename),false));
	}
	catch(Exception e){
	    System.out.println("invalid output file path");
	}

	programCodeGen(syntaxTree);
	registerCount=0;
	globalLabelCounter=1;
	filewriter.close();
    }

    /*    
	  String codegen(Node node){
	  if (node instanceof ProgramNode)
	  return programCodeGen(node);
	  else if (node instanceof VarDeclNode)
	  return varDeclCodeGen(node);
	  else if (node instanceof FunDeclNode)
	  return funDeclCodeGen(node);
	  else if (node instanceof CompStmtNode)
	  return compStmtCodeGen(node);
	  else if (node instanceof StmtListNode)
	  return stmtListCodeGen(node);
	  else if (node instanceof LocalDecsNode)
	  return localDecsCodeGen(node);
	  else if (node instanceof ExprStmtNode)
	  return exprStmtCodeGen(node);
	  else if (node instanceof IfStmtNode)
	  return ifStmtCodeGen(node);
	  else if (node instanceof IfStartNode)
	  return ifStartCodeGen(node);
	  else if (node instanceof IfRemNode)
	  return ifRemCodeGen(node);
	  else if (node instanceof WhileStmtNode)
	  return whileStmtCodeGen(node);
	  else if (node instanceof ForStmtNode)
	  return forStmtCodeGen(node);
	  else if (node instanceof ReturnStmtNode)
	  return returnStmtCodeGen(node);
	  else if (node instanceof ExpressionNode)
	  return expressionCodeGen(node);
	  else if (node instanceof OrExprNode)
	  return orExprCodeGen(node);
	  else if (node instanceof AndExprNode)
	  return andExprCodeGen(node);
	  else if (node instanceof RelExprNode)
	  return relExprCodeGen(node);
	  else if (node instanceof AddExprNode)
	  return addExprCodeGen(node);
	  else if (node instanceof TermNode)
	  return termCodeGen(node);
	  else if (node instanceof UnaryExprNode)
	  return unaryExprCodeGen(node);
	  else if (node instanceof PrimaryExprNode)
	  return primaryExprCodeGen(node);	
	  return new String();
	  }
    */
    

    private void programCodeGen(Node node){
	emit(dotsection,dottext);

	for (int i=0; i< node.children.length ; i++){
	    if (node.children[i] instanceof VarDeclNode)
		globalVarDeclCodeGenHead((VarDeclNode)node.children[i]);
	}
	
	for (int i=0; i< node.children.length ; i++){
	    if (node.children[i] instanceof FunDeclNode)
		funDeclCodeGen(node.children[i]);
	}
	
	for (int i=0; i< node.children.length ; i++){
	    if (node.children[i] instanceof VarDeclNode)
		globalVarDeclCodeGenFoot((VarDeclNode)node.children[i]);
	}

	return;
    }

    private void globalVarDeclCodeGenHead(VarDeclNode node){
	
	if (node.children.length==2){
	    commons.add(node.children[1].toString());
	}
	else if (node.children.length==3){
	    if (node.children[1].toString().equals("asterisk")){
		commons.add(node.children[1].toString());
	    }
	    else{
		commons.add(node.children[1].toString()+"[0]");
	    }	    
	}

	return;
	
    }


    private void globalVarDeclCodeGenFoot(VarDeclNode node){
	
	if (node.children.length==2){
	    emit(dotcommon,node.children[1].toString(),INTSIZE, INTSIZE);	    
	}
	else if (node.children.length==3){
	    if (node.children[1].toString().equals("asterisk")){
		emit(dotcommon,node.children[1].toString(),POINTERSIZE,POINTERSIZE);	    
	    }
	    else{
		String arraylength=new Integer(Integer.parseInt(INTSIZE)*((NumNode)node.children[2]).value).toString();
		emit(dotcommon,node.children[1].toString(), arraylength ,INTSIZE);	    
	    }	    
	}

	return;
	
    }

    private void varDeclCodeGen(Node node, ResourceTable resourceTable){
	if ((node.children.length==3) && (node.children[2] instanceof NumNode))
	    for (int i=0;i<((NumNode)node.children[2]).value;i++)
		resourceTable.declareLocalVar(node.children[1].toString()+"["+i+"]");
	else
	    resourceTable.declareLocalVar(node.children[node.children.length-1].toString());
	return;
    }

    private void funDeclCodeGen(Node node){
	FunDeclNode funNode = (FunDeclNode)node;
	String functionlabel;
	int nargs=0;	
	int stackFrameSize=funNode.stackSize;
	String[] args = new String[0];
	Node params = new ParamsNode(0);
	

	if (node.children.length==4)
	    params = node.children[2];
	else
	    params = node.children[3];


	//code block to get nargs and args[]
	//--------------------------------------
	if (!(params.children[0] instanceof VoidNode)){
	    nargs= params.children.length;
	    args = new String[nargs];
	    for (int i=0;i<nargs;i++)
		args[i]= params.children[i].children[node.children[2].children[i].children.length-1].toString();
	}  
	//--------------------------------------
	
    
	//code block to get nlocals and locals[]
	//--------------------------------------
	int nlocals = funNode.nlocals; //number of all local declarations within the function
	String[] locals = new String[nlocals];
	for (int i=0;i<funNode.allDeclarations.size();i++){
	    Node currentNode = ((Node)funNode.allDeclarations.get(i));
	    if ((currentNode.children.length==3) && (currentNode.children[2] instanceof NumNode)) {
		int j;
		for (j=0;j<((NumNode)currentNode.children[2]).value;j++)
		    locals[i+j]=currentNode.children[1].toString()+"["+j+"]";
		i+=j;
	    }
	    else
		locals[i]=currentNode.children[currentNode.children.length-1].toString();
	}
	//--------------------------------------


	int nOutgoingArgs = funNode.maxcallsize;//nOutgoingArgs: size of biggest outgoing call from this function

	ResourceTable resourceTable = new ResourceTable(stackFrameSize,nargs,args,nlocals,locals,nOutgoingArgs, this);//create the ResourceTable
	functionlabel=getLabel(((FunDeclNode)node).signature);//get a new unused label

	emit(dotalign,INTSIZE);//.align 4
	emit(dotglobal,functionlabel);//.global <functionname>



	emitLabel(functionlabel);//<functionname>:


	emit(prologue,"0");//prologue 0
	emit(save,stackpointer,"-"+stackFrameSize,stackpointer);// save %sp,-(stackframe size), %sp
	emit(prologue,"1");//prologue 1

	for (int i=0;i<Math.min(nargs,6);i++)
	    if (!resourceTable.getPassedArgFP(i).equals(ResourceTable.NOTFOUND))
		emit(store,resourceTable.iRegTable[i][0],resourceTable.getPassedArgFP(i)); //st %i0,[fp+68]
	
	compStmtCodeGen(node.children[node.children.length-1], resourceTable);
	return;
    }
	
    private String getLabel(String signature){
	if( signature.substring(0,4).equals("main"))
	    return "main";
	
	int labelcounter=0;
	String returnLabel=signature.substring(0,signature.indexOf("_"))+"0";
	while (labelTable.containsValue(returnLabel))
	    returnLabel=returnLabel.substring(0,returnLabel.length()-1)+(new Integer(labelcounter++)).toString();
	    
	labelTable.put(signature, returnLabel);
	return returnLabel;
    }
	
    private String getLabel(){
	String returnLabel=".LL1";

	while (labelTable.containsValue(returnLabel))
	    returnLabel=".LL"+(new Integer(globalLabelCounter++)).toString();

	labelTable.put(returnLabel, returnLabel);
	//System.out.println(labelTable);
	return returnLabel; // return a unique label

    }

    private void emitLabel(String labelstring){
	filewriter.println(labelstring+":");
	return;
    }

    private void compStmtCodeGen(Node node, ResourceTable resourceTable){
	if ((node.children != null) && (node.children.length!=0)){
	    if (node.children.length==2){
		localDecsCodeGen(node.children[0], resourceTable);
		stmtListCodeGen(node.children[1], resourceTable);
	    }
	    else if (node.children[0] instanceof LocalDecsNode)
		localDecsCodeGen(node.children[0], resourceTable);
	    else
		stmtListCodeGen(node.children[0], resourceTable);	
	}
	return;
    }

    private void stmtCodeGen(Node node, ResourceTable resourceTable){

	if (node instanceof CompStmtNode)
	    compStmtCodeGen(node, resourceTable);
	else if (node instanceof ExprStmtNode)
	    exprStmtCodeGen(node, resourceTable);
	else if (node instanceof IfStmtNode)
	    ifStmtCodeGen(node, resourceTable);
	else if (node instanceof WhileStmtNode)
	    whileStmtCodeGen(node, resourceTable);
	else if (node instanceof ForStmtNode)
	    forStmtCodeGen(node, resourceTable);
	else if (node instanceof ReturnStmtNode){
	    returnStmtCodeGen(node, resourceTable);	      
	}
	else
	    System.out.println("compiler error: unknown statement: " + node );
	return;
    }

    private void stmtListCodeGen(Node node, ResourceTable resourceTable){
	for (int i=0;i<node.children.length;i++)
	    stmtCodeGen(node.children[i], resourceTable);
	return;
    }

    private void localDecsCodeGen(Node node, ResourceTable resourceTable){
	for (int i=0; i<node.children.length;i++)
	    varDeclCodeGen(node.children[i], resourceTable);

	return;
    }



    private void exprStmtCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	if(node.children.length==1)
	    returnVal = expressionCodeGen(node.children[0], resourceTable);
	
	resourceTable.releaseRegister(returnVal);
	return;
    }

    // I think that all of the statements are by nature 'void', any thoughts???

    
    private void ifStmtCodeGen(Node node, ResourceTable resourceTable){
	if(node.children.length == 1) {
	    String fail = getLabel();
	    String reg = expressionCodeGen(node.children[0].children[1], resourceTable);
	    emit(compare, reg, "0");
	    emit(equal, fail);
	    emit(nop);
	    stmtCodeGen(node.children[0].children[2], resourceTable);
	    emitLabel(fail);
	    resourceTable.releaseRegister(reg);
	}
	else{
	    String fail = getLabel();
	    String success = getLabel();
	    String reg = expressionCodeGen(node.children[0].children[1], resourceTable);
	    emit(compare, reg, "0");
	    emit(equal, fail);
	    emit(nop);
	    stmtCodeGen(node.children[0].children[2], resourceTable);
	    emit(branch, success);
	    emit(nop);
	    emitLabel(fail);
	    resourceTable.releaseRegister(reg);
	    stmtCodeGen(node.children[1].children[1], resourceTable);
	    emitLabel(success);
	}
    }


    private void whileStmtCodeGen(Node node, ResourceTable resourceTable){
	String begin = getLabel();
	String end = getLabel();
	emitLabel(begin);
	String reg = expressionCodeGen(node.children[1], resourceTable);
	emit(compare, reg, "0");
	emit(equal, end);
	emit(nop);
	stmtCodeGen(node.children[2], resourceTable);
	emit(branch, begin);
	emit(nop);
	emitLabel(end);
	resourceTable.releaseRegister(reg);	
    }

    //should work, testing due
    private void forStmtCodeGen(Node node, ResourceTable resourceTable){	
	String thirdexpr;
	String begin = getLabel();
	String end = getLabel();
	String comparisonreg;
	exprStmtCodeGen(node.children[1],resourceTable);

	emitLabel(begin);
	if (node.children[2].children.length!=0){
	    comparisonreg = expressionCodeGen(node.children[2].children[0], resourceTable);
	    emit(compare,comparisonreg, "0"); 
	    emit(equal, end);
	    emit(nop);	
	    resourceTable.releaseRegister(comparisonreg);
	}	

	if (node.children.length==5){
	    stmtCodeGen(node.children[4],resourceTable);
	    thirdexpr=expressionCodeGen(node.children[3], resourceTable);
	    resourceTable.releaseRegister(thirdexpr);
	}			      
	else {
	    stmtCodeGen(node.children[3],resourceTable);
	}

	emit(branch, begin);
	emit(nop);
	emitLabel(end);		
	
    }


    private void returnStmtCodeGen(Node node, ResourceTable resourceTable){

	if(node.children.length == 0){ //case of "return;" so just return
	    emit(ret);
	    emit(restore);
	}
	else{ // case of "return expression;" so evaluate the expression, move the result to the return register, then return
	    String reg = expressionCodeGen(node.children[0], resourceTable);
	    emit(move, reg, "%i0");
	    resourceTable.releaseRegister(reg);
	    emit(ret);
	    emit(restore);
	}
	return;
    }

   

    private String expressionCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = new String();
	String reg1 ="";
	if(node.children.length==1){
	    reg1 = orExprCodeGen(node.children[0], resourceTable);
	    return reg1;
	}

	String stacklocRegister = orExprCodeGenL(node.children[0], resourceTable);
	if(((TerminalNode)node.children[1]).token.symbolNumber == 27){ //operator +=
	    String reg2 = expressionCodeGen(node.children[2], resourceTable);
	    reg1= resourceTable.getRegister();

	    emit (load, "["+stacklocRegister+"]", reg1);
	    emit(add, reg1, reg2 ,reg2);
	    resourceTable.releaseRegister(reg1);

	    emit(store, reg2, "["+stacklocRegister+"]");

	    resourceTable.releaseRegister(stacklocRegister);
	    returnVal=reg2;
	}
	else if(((TerminalNode)node.children[1]).token.symbolNumber == 28){ //operator -=
	    String reg2 = expressionCodeGen(node.children[2], resourceTable);
	    reg1= resourceTable.getRegister();

	    emit (load, "["+stacklocRegister+"]", reg1);
	    emit(sub, reg1, reg2 ,reg2);
	    resourceTable.releaseRegister(reg1);

	    emit(store, reg2, "["+stacklocRegister+"]");
	    resourceTable.releaseRegister(stacklocRegister);
	    returnVal=reg2;
	}
	else{ //operator =
	    String reg2 = expressionCodeGen(node.children[2], resourceTable);
	    emit(store, reg2, "["+stacklocRegister+"]");
	    resourceTable.releaseRegister(stacklocRegister);
	    returnVal=reg2;
	}
	
	return returnVal;

    }


    //The OR code is fine. The OR instruction is a bitwise and not logical OR,
    //but I don't see how a logical OR is different from a bitwise OR.
    //We cool.
    //- Akshat
    private String orExprCodeGen(Node node, ResourceTable resourceTable){	
	String returnVal = resourceTable.NOTFOUND;

	String reg1 = resourceTable.NOTFOUND;
	String reg2 = resourceTable.NOTFOUND;

	reg1 = andExprCodeGen(node.children[0], resourceTable);
	
	if(node.children.length == 1)
	    returnVal = reg1;
	else{
	    for(int i = 1; i < node.children.length; i++){//any number of additive expressions with '||' in between
		reg2 = andExprCodeGen(node.children[i], resourceTable);
		emit(or, reg1, reg2, reg1);
		resourceTable.releaseRegister(reg2);
	    }	    
	    returnVal = reg1;
	}

	return returnVal;
    }



    //the AND instruction on Sparc is a bitwise AND, which is not always the same as a logical AND
    //to do a logical AND, we basically write code that checks each expresseion in the children array
    //and branches to "fail" if a zero is found.
    //- Akshat
    private String andExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	String reg1 = resourceTable.NOTFOUND;
	String reg2 = resourceTable.NOTFOUND;

	reg1 = relExprCodeGen(node.children[0], resourceTable);
	
	if(node.children.length == 1)
	    returnVal = reg1;
	else{
	    String end1=getLabel();
	    String end2=getLabel();

	    emit(compare, reg1, zero);
	    emit(equal, end1);
	    emit(nop);
	    resourceTable.releaseRegister(reg1);

	    for(int i = 1; i < node.children.length; i++){//any number of additive expressions with '&&' in between
		reg2 = relExprCodeGen(node.children[i], resourceTable);
		emit(compare, reg2, zero);
		emit(equal, end1);
		emit(nop);
		resourceTable.releaseRegister(reg2);
	    }
	    
	    reg1=resourceTable.getRegister();
	    emit(move,"1",reg1);
	    emit(branch,end2);
	    emit(nop);
	    emitLabel(end1);	
	    emit(move,"0",reg1);
	    emitLabel(end2);	
	    returnVal=reg1;
	}	

	return returnVal;
    }

    // It's much more complex than the other methods below
    // both because this starts with branching (and thus also labeling) but also because we have
    // to combine relational operators. I'm thinking the best way to do this is to think in the
    // following manner: a < b > c ---> (a < b) && (b > c).
    private String relExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	String fail = getLabel(); 
	
	String reg1 = "";
	String reg2 = "";
	reg1 = addExprCodeGen(node.children[0], resourceTable);

	if (node.children.length != 1) {	    
	    for(int i = 1; i < node.children.length;i++){//any number of additive expressions with operators in between

		int symbol = ((TerminalNode)node.children[i]).token.symbolNumber;
		reg2 = addExprCodeGen(node.children[i+1], resourceTable);

		//test reg1 vs reg2, branch to fail if fail
		if(symbol == 21) { //operator <
		    emit(compare, reg1, reg2);
		    emit(greaterorequal, fail);
		    emit(nop);
		}
		else if(symbol == 22) { //operator >
		    emit(compare, reg1, reg2);
		    emit(lessorequal,  fail);
		    emit(nop);
		}
		else if(symbol == 23) { //operator <=
		    emit(compare, reg1, reg2);
		    emit(greaterthan, fail);
		    emit(nop);
		}
		else if(symbol == 24) { //operator >=
		    emit(compare, reg1, reg2);
		    emit(lessthan,  fail);
		    emit(nop);
		}
		else if(symbol == 25) { //operator ==
		    emit(compare, reg1, reg2);
		    emit(notequal, fail);
		    emit(nop);
		}
		else if(symbol == 26) { //operator !=
		    emit(compare, reg1, reg2);
		    emit(equal, fail);
		    emit(nop);
		}
	    
		emit(move, reg2, reg1); //move the value of reg2 into reg1, so that the next loop will make correct comparison
		resourceTable.releaseRegister(reg2);
		i++; // need to increment twice to get around the operator
	    }

	    String end = getLabel();
	    emit(move, "1", reg1);
	    emit(branch, end);
	    emit(nop);
	    emitLabel(fail);
	    emit(move, "0", reg1);
	    emitLabel(end);
	}
	
	returnVal = reg1;

 
	return returnVal;
    }

    // should be done but can't test until term is done...
    private String addExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	
	if(node.children.length == 1)
	    returnVal = termCodeGen(node.children[0], resourceTable);
	else{
	    String reg1 = "";
	    String reg2 = "";
	    reg1 = termCodeGen(node.children[0], resourceTable);
	    
	    for(int i = 1; i < node.children.length;i++){//any number of terms with operators in between
		int symbol = ((TerminalNode)node.children[i]).token.symbolNumber;
		reg2 = termCodeGen(node.children[i+1], resourceTable);
		if(symbol == 19) { //operator +		
		    if ((node.children[i-1].type.equals(Symbol.INTSTAR))
			&& (node.children[i+1].type.equals(Symbol.INT))){
			emit(multiply,reg2,"4",reg2);
		    }
		    else  if ((node.children[i-1].type.equals(Symbol.INT))
			      && (node.children[i+1].type.equals(Symbol.INTSTAR))){
			emit(multiply,reg1,"4",reg1);
		    }

		    emit(add, reg1, reg2, reg1); 
		}
		else if(symbol == 20) { //operator -
		    if ((node.children[i-1].type.equals(Symbol.INTSTAR))
			&& (node.children[i+1].type.equals(Symbol.INT))){
			emit(multiply,reg2,"4",reg2);
			emit(sub, reg1, reg2, reg1);
		    }
		    else  if ((node.children[i-1].type.equals(Symbol.INTSTAR))
			      && (node.children[i+1].type.equals(Symbol.INTSTAR))){
			emit(sub, reg1, reg2, reg1);
			emit(divide, reg1, "4", reg1);			
		    }
		    else {
			emit(sub, reg1, reg2, reg1);
		    }
		}
		i++;
	    }
	    returnVal = reg1;
	    resourceTable.releaseRegister(reg2);
	}

	return returnVal;
    }


    // we need the command for % before this method will work!!!!!
    private String termCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	String reg1 = "";
	String reg2 = "";
	reg1 = unaryExprCodeGen(node.children[0], resourceTable);
	returnVal=reg1;

	if(node.children.length != 1){	    
	    for(int i = 1; i < node.children.length;i++){//any number of unary exprs with operators in between
		int symbol = ((TerminalNode)node.children[i]).token.symbolNumber;
		reg2 = unaryExprCodeGen(node.children[i+1], resourceTable);
		if(symbol == 14) { //operator *
		    
		    emit(multiply, reg1, reg2, reg1);
		}
		else if(symbol == 18) { //operator /
		    emit(divide, reg1, reg2, reg1);
		}
		else if(symbol == 16) { //operator %
		    emit(move, reg1, "%o0");
		    emit(move, reg2, "%o1");
		    emit(call,modfunction);
		    emit(nop);
		    emit(move, "%o0", reg1);		    
		}
		i++;
	    }
	    returnVal = reg1;
	    resourceTable.releaseRegister(reg2);
	}
	
	return returnVal;
    }



    // we need to figure out ! before this method will work!!!!
    private String unaryExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;
	if(node.children.length == 1)
	    returnVal = primaryExprCodeGen(node.children[0], resourceTable);
	else if(node.children.length == 2){
	    int symbol = ((TerminalNode)node.children[0]).token.symbolNumber;
	    if(symbol == 19){ //operator +
		returnVal = unaryExprCodeGen(node.children[1], resourceTable);
	    }
	    else if(symbol == 20){ //operator -
		returnVal = unaryExprCodeGen(node.children[1], resourceTable);
		emit(sub, "%r0", returnVal, returnVal);
	    }
	    else if(symbol == 14){ //operator *
		returnVal = unaryExprCodeGen(node.children[1], resourceTable);
		emit(load, "[" + returnVal + "]", returnVal);
	    }
	    else if(symbol == 17){ //operator !
		String makeone = getLabel();
		String makezero = getLabel();
		returnVal= unaryExprCodeGen(node.children[1], resourceTable);
		emit(compare,returnVal,"0");
		emit(equal,makeone);
		emit(nop);
		emit(move, "0", returnVal);
		emit(branch, makezero);
		emit(nop);
		emitLabel(makeone);
		emit(move, "1", returnVal);
		emitLabel(makezero);		
	    }
	    else if(symbol == 15){ //operator &
		String reg1="";
		String lookupstring="";

		Node pnode=findPrimaryExpr(node.children[1]);
		if ((pnode.children.length==1) || (pnode.children.length==4))
		    lookupstring=pnode.children[0].toString();			 
		String location=resourceTable.lookupStackFrame(lookupstring);
		if (location.equals(resourceTable.NOTFOUND))
		    location = resourceTable.lookupStackFrame(lookupstring+"[0]");

		String offset = location.substring(4,location.length()-1);		

		if (pnode.children.length==4){
		    reg1=expressionCodeGen(pnode.children[2],resourceTable);		    
		    emit(add,"%fp",reg1,reg1);
		    emit(add,offset,reg1,reg1);
		    returnVal=reg1;
		}
		else{
		    reg1=resourceTable.getRegister();
		    emit(add,"%fp",offset,reg1);
		    returnVal=reg1;;
		}

		/*
		  returnVal = resourceTable.getRegister();



		  lookupstring=pnode.children[0].toString()+"["+Integer.toString(((NumNode)pnode.children[2]).value)+"]";
		  else 

		  emit(add, "%fp", offset, returnVal);		*/
	    }

	}

	return returnVal;
    }

    private String primaryExprCodeGen(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;

	if(node.children.length==1){ //case of epression, id, num, or real
	    if(node.children[0] instanceof ExpressionNode)
		return expressionCodeGen(node.children[0], resourceTable);
	    else if(node.children[0] instanceof IdentifierNode){		
		String reg = resourceTable.getRegister(node.children[0].toString());

		if (reg.equals(resourceTable.NOTFOUND)){
		    reg = resourceTable.lookupStackFrame(node.children[0].toString()+"[0]");
		    if (!reg.equals(resourceTable.NOTFOUND)){
			String offset = reg.substring(4,reg.length()-1);
			reg=resourceTable.getRegister();
			emit(add, "%fp", offset, reg);		    
		    }
		}

		returnVal = reg;

		if (reg.equals(resourceTable.NOTFOUND)) {
		    if (commons.contains(node.children[0].toString())){
			String reg1=resourceTable.getRegister();  
			String reg2=resourceTable.getRegister();		  
			emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
			emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
			emit(load, "["+reg2+"]",reg2);			
			returnVal=reg2;		    
			resourceTable.releaseRegister(reg1);
		    }
		    else if (commons.contains(node.children[0].toString()+"[0]")) {
			String reg1=resourceTable.getRegister();  
			String reg2=resourceTable.getRegister();		  
			emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
			emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
			returnVal=reg2;		    
			resourceTable.releaseRegister(reg1);			    
		    }
		}



	    }
	    else if(node.children[0] instanceof NumNode){ 
		String reg = resourceTable.getRegister();
		emit(move, new Integer(((NumNode)node.children[0]).value).toString(), reg);
		returnVal = reg;
	    }
		
	}

	else if(node.children.length == 4){ // case of array or function
	    if(node.children[2] instanceof ExpressionNode){

		String temp = resourceTable.lookupStackFrame(node.children[0].toString() + "[0]");		

		if (!temp.equals(resourceTable.NOTFOUND)){
		    String exprReg = expressionCodeGen(node.children[2], resourceTable);
		    emit(multiply, exprReg, "4", exprReg); // to multiply by four so that the offset is ok

		    String tempReg = resourceTable.getRegister();
		    //resourceTable.setRegister(tempReg, node.children[0].toString() + ""
		    temp = temp.substring(4, temp.length()-1);
		    String reg1 = resourceTable.getRegister();
		    emit(add, "%fp",temp,reg1);
		    emit(add, reg1, exprReg, reg1);
		    //		emit(load, temp + "(" + exprReg + ")",tempReg);
		    emit(load, "[" + reg1 + "]", tempReg);
		    resourceTable.releaseRegister(reg1);
		    resourceTable.lookupVariables_Stack("[%fp" + temp + "]"); // gives us ar[0]
		    resourceTable.releaseRegister(exprReg);
		    returnVal = tempReg;
		}
		else if (commons.contains(node.children[0].toString()+"[0]")) {
		    String exprReg = expressionCodeGen(node.children[2], resourceTable);
		    emit(multiply, exprReg, "4", exprReg); // to multiply by four so that the offset is ok

		    String reg1=resourceTable.getRegister();  
		    emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
		    emit(or,reg1,"%lo("+node.children[0].toString()+")",reg1);
		    emit(add,reg1,exprReg,reg1);
		    emit(load, "["+reg1+"]",reg1);
		    returnVal=reg1;		    
		    resourceTable.releaseRegister(exprReg);		    
		
		}


	    }
	    else if(node.children[2] instanceof ArgsExprNode){
		if (node.children[2].children.length>6) {
		    int spcounter=92;
		    for (int i=6;i<Math.max(node.children[2].children.length,6);i++) {			
			String tempReg;
			emit(store,(tempReg=expressionCodeGen(node.children[2].children[i],resourceTable)),"[%sp+"+spcounter+"]");
			spcounter += 4;
			resourceTable.releaseRegister(tempReg);
		    }
		}
		for (int i=0;i<Math.min(node.children[2].children.length,6);i++) {
		    String tempReg;
		    emit(move,(tempReg=expressionCodeGen(node.children[2].children[i],resourceTable)),"%o"+i);
		    resourceTable.releaseRegister(tempReg);
		}
		emit(call,labelTableLookup(((PrimaryExprNode)node).callsignature),"0");
		emit(nop);
		emit(nop);
		returnVal="%o0";
	    }
	}

	return returnVal;
    }


    

    private String expressionCodeGenL(Node node, ResourceTable resourceTable){	
	return orExprCodeGenL(node.children[0], resourceTable);
    }    
    private String orExprCodeGenL(Node node, ResourceTable resourceTable){	
	return andExprCodeGenL(node.children[0], resourceTable);
    }
    private String andExprCodeGenL(Node node, ResourceTable resourceTable){
	return relExprCodeGenL(node.children[0], resourceTable);
    }
    private String relExprCodeGenL(Node node, ResourceTable resourceTable){
	return addExprCodeGenL(node.children[0], resourceTable);
    }
    private String addExprCodeGenL(Node node, ResourceTable resourceTable){
	return termCodeGenL(node.children[0], resourceTable);
    }
    private String termCodeGenL(Node node, ResourceTable resourceTable){
	return unaryExprCodeGenL(node.children[0], resourceTable);
    }
    private String unaryExprCodeGenL(Node node, ResourceTable resourceTable){
	return primaryExprCodeGenL(node.children[0], resourceTable);
    }

    private String primaryExprCodeGenL(Node node, ResourceTable resourceTable){
	String returnVal = resourceTable.NOTFOUND;

	if(node.children.length==1){ //case of epression, id, num, or real
	    if(node.children[0] instanceof ExpressionNode)
		return expressionCodeGenL(node.children[0], resourceTable);
	    else if(node.children[0] instanceof IdentifierNode){		
		String temp=resourceTable.lookupStackFrame(node.children[0].toString());
		if (!temp.equals(resourceTable.NOTFOUND)){		    
		    String outputReg=resourceTable.getRegister();
		    temp = temp.substring(4, temp.length()-1);
		    emit(add,"%fp",temp,outputReg);
		    returnVal=outputReg;		
		}
		else if (commons.contains(node.children[0].toString())){		    
		    String reg1=resourceTable.getRegister();  
		    String reg2=resourceTable.getRegister();		  
		    emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
		    emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
		    returnVal=reg2;		    
		    resourceTable.releaseRegister(reg1);
		} 
	    }
	    
	}		    
	else if(node.children.length == 4){ // case of array or function
	    if(node.children[2] instanceof ExpressionNode){
		String temp = resourceTable.lookupStackFrame(node.children[0].toString() + "[0]");		
		String exprReg = expressionCodeGen(node.children[2], resourceTable);		
		emit(multiply, exprReg, "4", exprReg); // to multiply by four so that the offset is ok		

		if (!temp.equals(resourceTable.NOTFOUND)){
		    temp = temp.substring(4, temp.length()-1);

		    String tempReg = resourceTable.getRegister();

		    //emit(load, tempReg, temp + "(" + exprReg + ")");
		    emit(add,  "%fp",exprReg,tempReg);
		    resourceTable.releaseRegister(exprReg);
		    emit(add, tempReg,temp,tempReg);
		    returnVal = tempReg;
		}
		else if (commons.contains(node.children[0].toString()+"[0]")) {
		    String reg1=resourceTable.getRegister();  
		    String reg2=resourceTable.getRegister();  
		    emit(sethi,"%hi("+node.children[0].toString()+")",reg1);
		    emit(or,reg1,"%lo("+node.children[0].toString()+")",reg2);
		    emit(add,reg2,exprReg,reg2);
		    returnVal=reg2;		    
		    resourceTable.releaseRegister(exprReg);		    
		    resourceTable.releaseRegister(reg1);		
		}
	    }
	}
	return returnVal;
    }


    void emit(String operation, String operand1, String operand2, String operand3){
	if (operand3==null){
	    if (operand2==null){
		if (operand1==null){//emit without operands
		    filewriter.println("\t"+operation);
		}
		else{//emit with 1 operand
		    filewriter.println("\t"+operation+"\t" + operand1);				    
		}
	    }
	    else {//emit with 2 operands
		filewriter.println("\t"+operation+"\t" + operand1 + ", " + operand2 );				    
	    }
	    
	}
	else {//emit with 3 operands
	    filewriter.println("\t"+operation+"\t" + operand1 + ", " + operand2 + ", " + operand3 );				    
	    
	}
	    
	return;
    }

    void emit(String operation, String operand1, String operand2){
	emit(operation, operand1,operand2,null);
	return;
    }

    void emit(String operation, String operand1){
	emit(operation, operand1,null,null);
	return;
    }

    void emit(String operation){
	emit(operation, null,null,null);
	return;
    }

    String labelTableLookup(String signature){
	if( signature.substring(0,4).equals("main"))
	    return "main";
	return (String)labelTable.get(signature);
    }

    Node findPrimaryExpr(Node node){
	if (node instanceof PrimaryExprNode){
	    if ((node.children.length==1) && (node.children[0] instanceof ExpressionNode))
		return findPrimaryExpr(node.children[0]);
	    else
		return node;
	}
	else if ((node.children!=null) && (node.children.length > 0))
	    return findPrimaryExpr(node.children[0]);
	else 
	    return new PrimaryExprNode(0);
    }

}
import java.util.ArrayList;

/*
  Akshat Singhal, Chris Fry
  Context.java
  class for context frame for the semantic analysis 
  of the Akshat and Chris Compiler (ACC)

  SymbolTable:
  (symbolTable)
  |---------|
  |-Context1|-->context1 is special, it is never popped off, it has all the top level var and fun decl's.
  |-Context2| 
  |-Context3| 
  |---------|

  Context:
  (symbols)
  |--------| 
  |-Symbol1| 
  |-Symbol2|
  |-Symbol3|
  |--------|

*/

public class Context{
    ArrayList symbols;
    
    public Context(){
	//constructor for Context
	symbols = new ArrayList();

    }

    public ArrayList add(Symbol symbol){
	//	System.out.println("tried to add symbol [" + symbol.identifier + "] of linenumber " + symbol.linenumber);
	ArrayList returnerrors = new ArrayList();
	Symbol checksymbol;
	if (symbol instanceof FunSymbol) {
	    checksymbol=getFunbySig(((FunSymbol)symbol).signature);		
	    //	    System.out.println("found symbol: " + checksymbol);
 	    if (!(checksymbol instanceof ErrorSymbol))
		returnerrors.add("[" + symbol.linenumber + "]:duplicate function declaration for " + symbol.identifier);
	}
	else if (symbol instanceof VarSymbol)  {
	    checksymbol=getVar(symbol.identifier);		
	    //	    System.out.println("found symbol: " + checksymbol);
	    if (!(checksymbol instanceof ErrorSymbol))
		returnerrors.add("[" + symbol.linenumber + "]:duplicate variable/parameter declaration for " + symbol.identifier);
	    
	}	
	else
	    returnerrors.add("Compiler error: invalid symbol entered into table");
	symbols.add(symbol);
	return returnerrors;
    }


    // returns type if symbol is in context, "" if not
    public Symbol getVar(String identifier){
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){	    
	    if((temp = (Symbol)symbols.get(i)).identifier.equals(identifier) && 
	       (temp instanceof VarSymbol))
		return temp;
	    /*	    else
		    System.out.println("tried to compare " + temp);*/
	}

	temp = new ErrorSymbol();
	return temp;
    }


    public Symbol getFunbySig(String signature){
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && ((FunSymbol)temp).signature.equals(signature)) {
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }

    public Symbol getFunbyIdent(String identifier){
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && (temp.identifier.equals(identifier)) ) {
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }
    

    public Symbol removeFunBySig(String signature){
	//tries to remove a function by looking for it by signature
	//returns an errorsymbol if not found
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && ((FunSymbol)temp).signature.equals(signature)) {
		symbols.remove(i);
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }


    /*
      //removeFunByIdent is a little ambiguous and possibly useless
    public Symbol removeFunbyIdent(String identifier){
	//tries to remove a function by looking for it by identifier, 
	//removes the first such function it can find
	//returns an errorsymbol if not found
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    temp = (Symbol)symbols.get(i);
	    if((temp instanceof FunSymbol) && (temp.identifier == identifier)) {
		symbols.remove(i);
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }
    */


    public Symbol removeVar(String identifier){
	//tries to remove a variable by looking for it by signature
	//returns an errorsymbol if not found
	Symbol temp;
	for(int i = symbols.size() -1; i >= 0; i--){
	    if((temp = (Symbol)symbols.get(i)).identifier.equals(identifier) && 
	       (temp instanceof VarSymbol)){
		symbols.remove(i);
		return temp;
	    }
	}

	temp = new ErrorSymbol();
	return temp;
    }



}
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
					newToken = new FloatToken(currentToken, symbolNumber, getLineCount());//modified in phase 3
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
import java.util.*;
import java.io.*;

public class LexerTest{
    
    public static void main(String args[]) 
    {
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
	    }
	Lexer lexer = new Lexer(reader);
	
	while(lexer.hasMoreTokens()){
	    System.out.print( ((Token)lexer.getNextToken()).toString() + " ");
	}
	
	System.out.print("\n");
	
	reader.close();
	}
	catch (FileNotFoundException f){
	    System.out.println("File " + args[0]  + "not found");
	}
	catch (IOException f){
	    System.out.println("I/O Exception");
	}
	

	
	
		

	
    }
}
import java.util.*;
public abstract class Node{
    //----------------------
    // New! Semantic Analysis attributes for the Node class.
    static final String INT = "int";
    static final String FLOAT = "float";
    static final String INTSTAR = "int*";
    static final String FLOATSTAR = "float*";
    static final String VOIDSTAR = "void*";
    static final String ERROR = "error";

    String type;
    boolean lvalue;
    boolean rvalue;
    //----------------------------

    Node[] children;
    String nodeStringValue;
    int numChildren;
    int linenumber;


    public String toString(){
	String returnstring = "( " + nodeStringValue;	
	for (int i=0;i<children.length;i++)
	    if (children[i] != null)
		returnstring += " " + children[i].toString();
	returnstring += " )";
	return returnstring;
    }// end of toString()

    public String printTree(){
	String returnstring = "<" + nodeStringValue + ">";
	String currentstring;
	if (children != null) {
	    for (int i=0;i<children.length;i++){
		if (children[i] != null) {
		    currentstring=children[i].printTree();    
		    if (currentstring.lastIndexOf("null") != -1)
			returnstring += "\n-|" + (String)(children[i].toString()).replaceAll("\n","\n-");
		    else
			returnstring += "\n-|" + (String)(currentstring).replaceAll("\n","\n-");
		}
	    }
	    
	}
	return returnstring;
    }//end of printTree
    
    public Node reducedNode(){
	if (children != null){
	    if ((children.length == 1)  && 
		((this instanceof OrExprNode) || (this instanceof AndExprNode) ||
		 (this instanceof RelExprNode) || (this instanceof AddExprNode) || (this instanceof TermNode) ||
		 (this instanceof UnaryExprNode) || (this instanceof PrimaryExprNode)  
		 )) // new reducedNode() only reduces single children nodes for the descendants of ExpressionNode
		{
		//System.out.println("reduced " + this + " to "  + children[0]);
		return children[0].reducedNode();
	    }
	    for (int i=0;i<children.length;i++){ 
		if (children[i] != null) {
		    //System.out.println("children[i]:" + children[i]);
		    children[i]=children[i].reducedNode();
		}
	    }
	}
	return this;
    }// end of reducedNode()

    public Node reduceChildren(){
	if (children != null){
	    for (int i=0;i<children.length;i++){ 
		if (children[i] != null) {
		    //System.out.println("children[i]:" + children[i]);
		    children[i]=children[i].reducedNode();
		}
	    }
	}
	return this;
    }// end of reduceChildren()   

    public Node[] getChildren(){
	return children;
    }

    public int getNumChildren(){
	return numChildren;
    }
}

class TerminalNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public TerminalNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }

    public int getSymbolNumber(){
	return token.symbolNumber;
    }
}

class ErrorNode extends Node{
    public ErrorNode(){
	children=new Node[0];
	numChildren = 0;
	nodeStringValue="Error";	
    }
}

class ProgramNode extends Node{
    public ProgramNode(int numChildren){
	children=new Node[numChildren];
	this.numChildren = numChildren;
	nodeStringValue="program";
    }  
}

class IdentifierNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public IdentifierNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.lexeme;
    }

    public String toString(){
	return nodeStringValue;
    }
}


class IntNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public IntNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class FloatNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public FloatNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class VoidNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public VoidNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class IfNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public IfNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class ElseNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public ElseNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class WhileNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public WhileNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class ForNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public ForNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class ReturnNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public ReturnNode(Token token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class VarDeclNode extends Node{
    
    public VarDeclNode(int numChildren){
	this.numChildren = numChildren;	
	children=new Node[numChildren];
	nodeStringValue="var-declaration";
    }  
}

class RealNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public float value;
    public RealNode(FloatToken token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
	this.value = token.getValue();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class NumNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public int value;
    public NumNode(IntegerToken token){
	children=new Node[0];
	numChildren = 0;
	this.token=token;
	nodeStringValue=token.toString();
	this.value = token.getValue();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class FunDeclNode extends Node{
    public ArrayList allDeclarations;
    public ArrayList allCalls;
    public int maxcallsize;
    public int nlocals;
    public int stackSize;
    public String signature;
    
    public FunDeclNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="fun-declaration";
    }  
}

class ParamsNode extends Node{
    public ParamsNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="params";
    }  
}

class ParamNode extends Node{
    
    public ParamNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="param";
    }  
}

class CompStmtNode extends StatementNode{    
    public CompStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="compound-statement";
    }  
}

class StmtListNode extends Node{
    public StmtListNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="statement-list";
    }  
}

class LocalDecsNode extends Node{
    public LocalDecsNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="local-declarations";
    }  
}

abstract class StatementNode extends Node{
   
}

class ExprStmtNode extends StatementNode{
    public ExprStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="expression-statement";
    }  
}
//OptExprNode changed to ExprNode to generic-ise the idea
class ExprNode extends Node{
    public ExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="optional-expression";
    }  
}

class IfStmtNode extends StatementNode{
    public IfStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="if-statement";
    }  
}

class IfStartNode extends Node{
    public IfStartNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="if-start";
    }  
}

class IfRemNode extends Node{
    public IfRemNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="if-remainder";
    }  
}

class WhileStmtNode extends StatementNode{
    public WhileStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="while-statement";
    }  
}

class ForStmtNode extends StatementNode{
    public ForStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="for-statement";
    }  
}

class ReturnStmtNode extends StatementNode{
    public ReturnStmtNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="return-statement";
    }  
}

class ExpressionNode extends Node{
    public ExpressionNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="expression";
    }  
}

class OrExprNode extends Node{
    public OrExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="or-expr";
    }  
}

class AndExprNode extends Node{
    public AndExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="and-expr";
    }  
}

class RelExprNode extends Node{
    public RelExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="rel-expr";
    }  
}

class AddExprNode extends Node{
    public AddExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="add-expr";
    }  
}

class TermNode extends Node{
    public TermNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="term";
    }  
}


class UnaryExprNode extends Node{
    public UnaryExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="unary-expr";
    }  
}

class PrimaryExprNode extends Node{
    boolean isCall;
    String callsignature;
    public PrimaryExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="primary-expr";
	isCall=false;
	callsignature="";
    }  
}

class ArgsExprNode extends Node{
    public ArgsExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="args";
    }  
}




import java.util.*;

public class ParseStack{
    private ArrayList stack;
    private int stackPointer;
    boolean diag4=false,diag1=false, diag2=false, diag3=false;
    //diag2 are important messages that should be outputted if stackPointer is being a bad boy

    Lexer lexer;
    
    public String toString(){
	String returnstring="[ ";
	for( int i = 0; i <stack.size(); i++)
	    returnstring += "<" + i + ">" + stack.get(i) + ",\n";
	return returnstring + " ]";
    }
    
    public String printTree(){
	String returnstring="";
	for( int i = 0; i <stack.size(); i++)
	    if (stack.get(i) instanceof Node)
		 returnstring += ((Node)stack.get(i)).printTree();
	    else
		 returnstring += stack.get(i).toString();
	return returnstring;
    }

    public ParseStack(Lexer lexer){
	this.lexer=lexer;
	stack = new ArrayList();
	stackPointer=0;

    }

    public Object getNext(){
	//getNext() gets the next item on stack, pointed to by the stackPointer:
	//- if stack is empty, it gets a token from Lexer and returns it, setting SP = 1
	// **(in this case, the newly returned element is put on stack) 
	//- if SP is pointing to an existing element of stack, that element is returned, setting SP=SP+1
	//
	//- if SP is pointing right above stack, a new token from Lexer is returned, setting SP=SP+1
	// **(in this case, the newly returned element is put on stack) 
	//
	// if SP points to something that is way above stack, it returns an EOFToken().
	

	if (stack.size()==0){
	    //System.out.println("I read " + lexer.peekNextToken());	    
	    if (lexer.hasMoreTokens()){
		stack.add(lexer.getNextToken());
		stackPointer++;
		return stack.get(stackPointer-1);
	    }
	    else
		return new EOFToken();
	}
	else if (stackPointer < stack.size()){ 	    
	    return stack.get(stackPointer++);
	}
	else if (stackPointer == stack.size()) // execute this case only if 
	    //stackpointer points to element right on top of stack, i.e. asks for a new element
	    { 
		if (lexer.hasMoreTokens()){
		    stack.add(lexer.getNextToken());
		    return stack.get(stackPointer++);
		}
		else
		return new EOFToken();
	    }
	else{//execute this case if stackpointer is point somewhere absurdly above the top of stack
	    if (diag2)
		System.out.println("**Diag warning: invalid stack ref: getNext(), SP=" + 
				   stackPointer + " for a stack of size " + stack.size());
	    return new EOFToken();
	}
    }

    public Object get(int index){
	return stack.get(index);
    }
    
    //testing purposes only
    public Object peekNext(){
	return stack.get(stackPointer);
    }

    public void push(Object o){
	stack.add(o);
    }

    public Object pop(){
	return stack.remove(stack.size()-1);     	
    }
    public Object top(){
	return stack.get(stack.size()-1);     	
    }

    public Object remove()
    {
	//remove() removes the stack element at stackPointer, and decreases stackPointer by 1
	if (diag3)
	    System.out.println("--Removed " + stack.get(stackPointer -1));

	if (stackPointer<=stack.size() && stackPointer >0)
	    return stack.remove(--stackPointer);
	else{//execute this case if stackpointer is point somewhere absurdly above the top of stack
	    if (diag2)
		System.out.println("**Diag warning: invalid stack ref: remove(" + 
				   stackPointer + ") for a stack of size " + stack.size());
	    return new EOFToken();
	}

    }

    public void insert(Object o)
    {
	//insert() adds given Object to stack at stackPointer, moving other elements up,
	// but DOES NOT increment stackPointer.
	if (stackPointer<=stack.size() && stackPointer >=0)
	    stack.add(stackPointer, o);
	else{//execute this case if stackpointer is point somewhere absurdly above the top of stack
	    if (diag2)
		System.out.println("**Diag warning: invalid stack ref: insert at(" + 
				   stackPointer + ") for a stack of size " + stack.size());
	    stack.add(stack.size(),o);
	}

    }
    

    public int getStackPointer(){
	return stackPointer;	
    }
    public int getSize(){
	return stack.size();	
    }
    public void  setStackPointer(int s){
	stackPointer=s;
    }
    public void incStackPointer(){
	stackPointer++;
    }
    public void decStackPointer(){
	stackPointer--;
    }

}
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
	    stack.remove(); // get rid of the ')'
	    ifStartNode.children[1] = (ExpressionNode)stack.remove();
	    stack.remove(); // get rid of the '('
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
	    succ = parseExpression(); // why do we do Expression instead of ExprStmt like the others???
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
public class ResourceTable{
    final static String NOTFOUND="<notfound>";
    
    String[][] gRegTable;
    String[][] lRegTable;
    String[][] iRegTable;
    String[][] oRegTable;
    String[][] stackFrameTable;
    int stackFrameSize;
    int incomingArgsRangeBegin;// range 'begin' and 'end' for begin and end of stack frame space
    int incomingArgsRangeEnd;
    int tempSpaceRangeBegin;
    int tempSpaceRangeEnd;
    int outgoingArgsRangeBegin;
    int outgoingArgsRangeEnd;
    int localVarsRangeBegin;
    int localVarsRangeEnd;
  
    CodeGenerator codeGenerator;
    public ResourceTable(int stackFrameSize, int nargs, String[] args, int nlocals, String[] locals, int nOutgoingArgs, CodeGenerator codeGenerator){
	this.codeGenerator = codeGenerator;
	this.stackFrameSize = stackFrameSize;
	gRegTable = new String[4][2];//g's: not preserved thru calls
	lRegTable = new String[7][2];//preserved thru calls
	iRegTable = new String[6][2];//incoming params
	oRegTable = new String[6][2];//outgoing params, not preserved thru calls
	stackFrameTable = new String[(stackFrameSize/4)+Math.max(nargs,6)][2];	
	//what do the magic numbers mean?:
	//4: size of int/pointer
	//nargs: number of this function's incoming parameters
	for (int i=0;i<4;i++){
	    gRegTable[i][0]="%g"+i;
	    gRegTable[i][1]="<free>";
	}

	for (int i=0;i<7;i++){
	    lRegTable[i][0]="%l"+i;
	    lRegTable[i][1]="<free>";
	}

	for (int i=0;i<6;i++){
	    iRegTable[i][0]="%i"+i;
	    if (nargs>i)
		iRegTable[i][1]=args[i];
	    else
		iRegTable[i][1]="<free>";
	}

	for (int i=0;i<6;i++){
	    oRegTable[i][0]="%o"+i;
	    oRegTable[i][1]="<free>";
	}

	//initializing the stack frame of the current method
	//remember: sp+x == (fp-size)+x
	//remember: 17=68/4

	
	for (int i=0;i<17;i++){
	    stackFrameTable[i][0]="[%sp+"+ (i*4) + "]";
	    stackFrameTable[i][1]="<systemreserved>";
	}
	

	outgoingArgsRangeBegin=17;
	outgoingArgsRangeEnd=17+Math.max(nOutgoingArgs,6);

	for (int i=outgoingArgsRangeBegin;i<outgoingArgsRangeEnd;i++){
	    stackFrameTable[i][0]="[%sp+"+ (i*4) + "]";
	    stackFrameTable[i][1]="<callarg"+(i-17)+">";
	}	

	//	System.out.println("the number of locals is: " + nlocals);
	tempSpaceRangeBegin=outgoingArgsRangeEnd;
	tempSpaceRangeEnd=(stackFrameSize/4)-nlocals;
	//    	System.out.println("tempSpaceRangeBegin=: " + tempSpaceRangeBegin);
	//    	System.out.println("tempSpaceRangeEnd=: " + tempSpaceRangeEnd);

	for (int i=tempSpaceRangeBegin;i<tempSpaceRangeEnd;i++){
	    stackFrameTable[i][0]="[%sp+"+ (i*4) + "]";
	    stackFrameTable[i][1]="<temp>";
	}


	localVarsRangeBegin=tempSpaceRangeEnd;
	localVarsRangeEnd=stackFrameSize/4;
	int localdecscounter=0;
	for (int i=localVarsRangeBegin;i<localVarsRangeEnd;i++){
	    stackFrameTable[i][0]="[%fp-"+ (stackFrameSize-i*4) + "]";
	    if (nlocals>localdecscounter)
		stackFrameTable[i][1]="<reserved_"+ locals[localdecscounter++]+">";
	}
    
	incomingArgsRangeBegin=(stackFrameSize/4);
	incomingArgsRangeEnd=((stackFrameSize/4)+ Math.max(nargs,6));
	int argscounter=0;
	for (int i=(stackFrameSize/4);i<((stackFrameSize/4)+ Math.max(nargs,6));i++){//getting the passed args
	    stackFrameTable[i][0]="[%fp+" + ((i*4)-stackFrameSize+68) + "]";
	    if (nargs>argscounter)
		stackFrameTable[i][1]=args[argscounter++];
	    else
		stackFrameTable[i][1]="<free>";
	}
	
	//the only place where you'd really like to use the [%fp+x] references would be when
	//getting passed arguments from the function calling this one.


	//[%sp+x] references are used for when accessing stuff within this function's stack frame.
	//when you say "save", the magic of register window happens:
	//-space is reserved in memory for this functions stack frame. space looks like this:
	// [sp+0]:
	// .
	// . [this area for storing register window between calls]
	// .
	// [sp+60]:
	// [sp+64]: special useless (for us)register
	// [sp+68]: parameter 1
	// .
	// . [this area has outgoing parameters]
	// .
	// [sp+88]: parameter 6 // stuff till here is stored in reg's %i0-%i5
	// [sp+92]: parameter 7
	// .
	// . [this area has extra outgoing parameters]
	// .
	// [sp+92+(4*x)]: outgoing parameter 6+x // where x: number of outgoing params beyond 6
	// .
	// . [this area has 'temporaries']
	// .
	// [fp-(4*L)]: local variable number 1 // where L: number of local variables	
	// .
	// . [this area has local variables]
	// .
	// [fp]
	// .
	// . [data we don't look at]
	// .
	// [fp+68]
	// .
	// . [this area has incoming params]
	// .
	// [fp+92]
	// .
	// . [this area has incoming params]
	// .
	// [fp+92+(4*x)]: incoming parameter 6+y // where y: number of incoming params beyond 6
	
	//print();
    }

    //get the frame pointer reference for incoming params
    String getPassedArgFP(int argnum){
	if (stackFrameTable.length > incomingArgsRangeBegin+argnum-1)
	    return stackFrameTable[incomingArgsRangeBegin+argnum][0];
	else
	    return NOTFOUND;//how to deal with errors here? what would cause an error?
    }
    
    // return an open register which contains 'id', if in reg. already, return that register
    // if in memory, allocate a register for it, load the val that register and return that register
    String getRegister(String id){
	String returnVal;
	String stackFrameRef;
	if (!(returnVal=lookupRegisters(id)).equals(NOTFOUND))
	    return returnVal;
	else if (!(stackFrameRef=lookupStackFrame(id)).equals(NOTFOUND)){	    
	    returnVal=getRegister();
	    setRegister(returnVal, id);
	    codeGenerator.emit (codeGenerator.load,stackFrameRef,returnVal);
	    return returnVal;
	}	
	else
	    return NOTFOUND;
    }
    

    // returns the first free register. If there are no free register, frees %l0
    // by storing it's val, then returns %l0
    public String getRegister(){
	String returnVal=lookupRegisters("<free>");
	if (returnVal.equals(NOTFOUND)){	    
	    //put an l variable into the stack frame and release its register
	    String stackFrameRef=lookupStackFrame(lookupVariables_Registers("%l0"));
	    codeGenerator.emit(codeGenerator.store,"%l0",stackFrameRef);
	    lRegTable[0][1]="<free>";
	    returnVal="%l0";
	}	
	setRegister(returnVal, "<taken>");
	return returnVal;
    }


    private void setRegister(String reg, String id){

	for (int i=0;i<gRegTable.length;i++)
	    if (gRegTable[i][0].equals(reg))
		gRegTable[i][1]=id;


	for (int i=0;i<lRegTable.length;i++)
	    if (lRegTable[i][0].equals(reg))
		lRegTable[i][1]=id;

	for (int i=0;i<iRegTable.length;i++)
	    if (iRegTable[i][0].equals(reg))
		iRegTable[i][1]=id;


	for (int i=0;i<oRegTable.length;i++)
	    if (oRegTable[i][0].equals(reg))
		oRegTable[i][1]=id;

	return;
    }


    public void releaseRegister(String reg){
	setRegister(reg,"<free>");
	return;
    }

    // replaces a spot reserved for it, with it's actual instance.
    public void declareLocalVar(String id){
	for(int i=0;i<stackFrameTable.length;i++)
	    if (stackFrameTable[i][1].equals("<reserved_"+id+">"))
		stackFrameTable[i][1]=id;	    
    }



    public void print(){

	System.out.println("---------------------------------------------------------");
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<gRegTable.length;i++)
	    System.out.println(" " + i + ": " +gRegTable[i][0] + " : " + gRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<lRegTable.length;i++)
	    System.out.println(" " + i + ": " +lRegTable[i][0] + " : " + lRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<iRegTable.length;i++)
	    System.out.println(" " + i + ": " +iRegTable[i][0] + " : " + iRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<oRegTable.length;i++)
	    System.out.println(" " + i + ": " +oRegTable[i][0] + " : " + oRegTable[i][1]);
	System.out.println("---------------------------------------------------------");

	for(int i=0;i<stackFrameTable.length;i++)
	    System.out.println(" " + i + ": " +stackFrameTable[i][0] + " : " + stackFrameTable[i][1]);
	System.out.println("---------------------------------------------------------");
    }



    // looks for an id in all registers    
    String lookupRegisters(String id){	    
	String returnLocation=NOTFOUND;
    

	returnLocation=getLocation(id,lRegTable);
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;
	
	returnLocation=getLocation(id,iRegTable);
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;

	/*
	returnLocation = getLocation(id,gRegTable);    
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;

	returnLocation=getLocation(id,oRegTable);
	if (!returnLocation.equals(NOTFOUND))
	    return returnLocation;
	*/
	return returnLocation;
    }

    // looks for 'id' in the stack frame
    String lookupStackFrame(String id){							
	return getLocation(id,stackFrameTable);
    }

    // looks for the variable stored in 'location'
    String lookupVariables_Registers(String location){
	String returnVariable=NOTFOUND;
    
	returnVariable=getVariable(location,lRegTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	returnVariable = getVariable(location,gRegTable);    
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	returnVariable=getVariable(location,iRegTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	returnVariable=getVariable(location,oRegTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	return returnVariable;
    }

    String lookupVariables_Stack(String location){
	String returnVariable = NOTFOUND;
	
	returnVariable=getVariable(location,stackFrameTable);
	if (!returnVariable.equals(NOTFOUND))
	    return returnVariable;

	return returnVariable;
    }

    // return the location in the given table (one of the above tables presumably)
    // NOTFOUND otherwise
    String getLocation(String id, String[][] table){
	for (int i=0;i<table.length;i++)
	    if (table[i][1].equals(id))
		return table[i][0];
	return NOTFOUND;
    }


    // return the variable stored in the given location
    // NOTFOUND otherwise
    private String getVariable(String location, String[][] table){
	for (int i=0;i<table.length;i++)
	    if (table[i][0].equals(location))
		return table[i][1];
	return NOTFOUND;	
    }

    /*  
    private boolean backupRegs(ResourceTable resourceTable){
	int i = 0;
	
	for(int i = 0; i< iRegTable.length; i++){
	    ;
	}

	return true;
    }
    */
}
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
/* Symbol.java */
/* Akshat Singhal and Chris Fry */
/* this is the symbol classes of the Akshat and
   Chris Compiler (ACC) */



public abstract class Symbol{
    final static String INT = "int";
    final static String FLOAT = "float";
    final static String INTSTAR = "int*";
    final static String FLOATSTAR = "float*";
    final static String VOIDSTAR = "void*";
    final static String ERROR = "error";

    String identifier;
    String type;
    boolean lvalue;
    int linenumber;
}

class VarSymbol extends Symbol{
    int arraylength;    

    public VarSymbol(VarDeclNode varDeclNode){
	this.linenumber=varDeclNode.linenumber;
	this.lvalue=true;
	Node[] children = varDeclNode.getChildren();	
	if (varDeclNode.children[1].toString().equals("asterisk")) {
	    this.type = children[0].toString() + "*";
	    this.identifier = children[2].toString();	    
	    this.arraylength=0;
	}
	else {
	    this.type = children[0].toString();
	    this.identifier = children[1].toString();	    
	    if (children.length==3) {
		this.type=this.type+"*";
		this.arraylength = ((NumNode)children[2]).value;
		this.lvalue=false;
	    }
	}
    }

    public VarSymbol(ParamNode paramNode){
	this.linenumber=paramNode.linenumber;
	this.lvalue=true;
	Node[] children = paramNode.getChildren();
	    if (children[1].toString().equals("asterisk")) {
		this.type = children[0].toString() + "*";
		this.identifier = children[2].toString();	    
		this.arraylength=0;
	    }
	    else {
		this.type = children[0].toString();
		this.identifier = children[1].toString();	    
		if (children.length==3){
		    this.type=this.type+"*";
		    this.arraylength = ((NumNode)children[2]).value;
		    this.lvalue=false;
		}
	    }	    	
    }

    public String toString(){
	return type+ " " + identifier;
    }
}

class FunSymbol extends Symbol{
    String signature;

    public FunSymbol(FunDeclNode funNode){
	ParamsNode params = new ParamsNode(0);
	this.linenumber=funNode.linenumber;

	
	if (funNode.children.length==5){
	this.type = funNode.children[0].toString()+"*";		
	this.identifier = funNode.children[2].toString();

	params = ((ParamsNode)funNode.children[3]);

	}
	else {
	    this.type = funNode.children[0].toString();
	    this.identifier = funNode.children[1].toString();
	    
	    params = ((ParamsNode)funNode.children[2]);	
	}


	String sig;
	sig =  "" + identifier ;		

	for(int i = 0; i < params.getNumChildren(); i++){
	    if (params.children[i] instanceof VoidNode)
		sig +=  "_" + "void";// not this isn't right yet
	    else {		
		if (params.children[i]!=null && params.children[i].children.length==3)
		    sig +=  "_" + params.children[i].children[0] + "*";
		else
		    sig +=  "_" + params.children[i].children[0] ;
	    }

	}
	this.signature = sig;
	funNode.signature=sig;
    }
    
    public String toString(){
	return type+"_"+signature;
    }
}


    
class ErrorSymbol extends Symbol{
    final int SYMBOLNOTFOUND = 0;
    int errorType=-1;
    String[] errorTypes=new String[4];

    public ErrorSymbol(){
	identifier = "error";
	type = ERROR;
    }
    
    public ErrorSymbol(int reason) {
	errorTypes[0]="Symbol Not Found";
	this.errorType = reason;
	identifier = "error";
	type = ERROR;
    }

    public String toString(){
	return identifier;
    }

}

class CallSymbol extends Symbol{

    public CallSymbol(PrimaryExprNode node){
	String identifier;
	String type;
	boolean lvalue;
	int linenumber;
    }  
}

import java.util.ArrayList;

/*
  Akshat Singhal, Chris Fry
  SymbolTable.java
  symbol table for the semantic analyzer 
  of the Akshat and Chris Compiler (ACC)
*/

// need to put symbol numbers in here too

public class SymbolTable{
    ArrayList symbolTable;
    int stackpointer;
    ArrayList savedContexts;
    public ArrayList calls;
    public SymbolTable(){
	symbolTable = new ArrayList();
	savedContexts = new ArrayList();
	stackpointer = 0;
    }
    public void removeTopContext(){
	if (symbolTable.size()>1){
	    savedContexts.add(symbolTable.remove(symbolTable.size()-1)); 
	    stackpointer--;
	}
	else
	    System.out.println("compiler error: error in checking compound statement scopes ");
    }

    public void addContext(Context frame){
	symbolTable.add(frame);
	stackpointer++;
    }


    public void clearContexts(){
	while (symbolTable.size()>1){
	    savedContexts.add(symbolTable.remove(symbolTable.size()-1)); 
	    stackpointer--;
	}
    }

    /*
    // returns the type of identifier, if it's in the symbolTable, "" otherwise
    public String getSymbolType(String identifier){
	int i = stackpointer-1;
	String temp;
	while(i>=0){
	    if ((temp = ((Context)symbolTable.get(i)).getType(identifier)) != "")
		return temp;   
	    else
		i--;
	}

	return "";
    }
    */


    public Symbol getCurrentFunctionSymbol(){
	Context mainContext = ((Context)symbolTable.get(0));
	for (int i=mainContext.symbols.size()-1;i>=0;i--)	    
	    if (mainContext.symbols.get(i) instanceof FunSymbol)
		return ((Symbol)mainContext.symbols.get(i));
	return new ErrorSymbol();
    }

    public Symbol getSymbol(String identifier){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getVar(identifier);
	    if (temp instanceof ErrorSymbol)
		temp = currentContext.getFunbyIdent(identifier);
	    
	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }

    public Symbol getFunbySig(String signature){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getFunbySig(signature);	    

	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }


    public Symbol getFunbyIdent(String identifier){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getFunbyIdent(identifier);	    

	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }


    public Symbol getVar(String identifier){
	int i = stackpointer-1;
	Context currentContext;
	Symbol temp;

	while(i>=0){   
	    currentContext = (Context)symbolTable.get(i);
	    temp = currentContext.getVar(identifier);	    

	    if  (!(temp instanceof ErrorSymbol))
		return temp;		
	    else
		i--;
	}

	return new ErrorSymbol();
    }


}
import java.util.*;
import java.io.*;
/*
 *
 ACC - Akshat and Chris Compiler for C
 - acc is the main class for the compiler.At this point, 
 it just calls the Lexer on the given input and prints out tokens.
 10/3/2005
 *
*/
public class TestLexer
{
    
    public static void main(String args[]) 
    {
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
	    }
	Lexer lexer = new Lexer(reader);
	
	while(lexer.hasMoreTokens()){
	    Token temp = (Token)lexer.getNextToken();
	    System.out.println(temp.toString() + " " + "symbolNumber: " + temp.symbolNumber);
	}

	System.out.print("\n");

	reader.close();
	}
	catch (FileNotFoundException f){
	    System.out.println("File " + args[0]  + "not found");
	}
	catch (IOException f){
	    System.out.println("I/O Exception");
	}
	

	
	
		

	
    }
}
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
	this.lexeme=lexeme;
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
import java.util.*;
import java.io.*;
/*
 *
 ACC - Akshat and Chris Compiler for C
 - acc is the main class for the compiler. At this point, 
 it just calls the Lexer on the given input and prints out tokens.
 10/3/2005
 *
*/
public class acc
{
    
    public static void main(String args[]) 
    {
	String filename="a.out.s";
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
		filename=args[0]+".s";
	    }
	Lexer lexer = new Lexer(reader);
	Parser parser = new Parser(lexer);
	Node syntaxTree=parser.parse();
	SemanticAnalyzer semAnalyzer = new SemanticAnalyzer(syntaxTree);
	ArrayList errors1 = semAnalyzer.checkProgram();
	ArrayList errors = parser.getErrors();
	if ((errors1.size() == 0) && (errors.size()==0)){
	    System.out.println("Program is free of errors");
	    CodeGenerator codegen1= new CodeGenerator(syntaxTree,filename);
	}
	else
	    System.out.println("Errors found in program:");
	
	if(syntaxTree instanceof ErrorNode){
	    for(int i = 0;i<errors.size(); i++)
		System.out.println(errors.get(i));
	    System.out.print("\n");
	}

	for(int i = 0;i<errors1.size(); i++)
	    System.out.println(errors1.get(i));
	
	    System.out.print("\n");
	

	    reader.close();
	}
	catch (FileNotFoundException f){
	    System.out.println("File " + args[0]  + "not found");
	}
	catch (IOException f){
	    System.out.println("I/O Exception");
	}
	

	
	
		

	
    }
}
