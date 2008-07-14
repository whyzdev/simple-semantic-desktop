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
