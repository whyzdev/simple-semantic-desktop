//Q:what is the return type of codegen(). is every codegen() supposed to return a register, or is only expr codegen supposed to return a register?
import java.util.*;
import java.util.Hashtable;
import java.io.*;
public class CodeGenerator{
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
	    if (node.children[i] instanceof FunDeclNode)
		funDeclCodeGen(node.children[i]);
	}
	
	for (int i=0; i< node.children.length ; i++){
	    if (node.children[i] instanceof VarDeclNode)
		globalVarDeclCodeGen((VarDeclNode)node.children[i]);
	}

	return;
    }

    private void globalVarDeclCodeGen(VarDeclNode node){
	
	if (node.children.length==2){
	    emit(dotcommon,node.children[1].toString(),INTSIZE, INTSIZE);	    
	}
	else if (node.children.length==3){
	    if (node.children[1].toString().equals("asterisk"))
		emit(dotcommon,node.children[1].toString(),POINTERSIZE,POINTERSIZE);	    
	    else{
		String arraylength=new Integer(Integer.parseInt(INTSIZE)*((NumNode)node.children[2]).value).toString();
		emit(dotcommon,node.children[1].toString(), arraylength ,INTSIZE);	    
	    }	    
	}

	commons.add(node.children[1].toString());
	//will probably need a new table ADT for common variables and arrays
	return;
	
    }

    private void varDeclCodeGen(Node node, ResourceTable resourceTable){
	if ((node.children.length==3) && (node.children[2] instanceof NumNode))
	    for (int i=0;i<((NumNode)node.children[2]).value;i++)
		resourceTable.declareLocalVar(node.children[1]);
	resourceTable.declareLocalVar(node.children[node.children.length-1]);
	return;
    }

    private void funDeclCodeGen(Node node, ResourceTable resourceTable){
	FunDeclNode funNode = (FunDeclNode)node;
	String functionlabel;
	int nargs=0;	
	int stackFrameSize=funNode.stackSize;
	String[] args = new String[0];
	Node params = new ParamsNode(0);
	

	if (node.children.length==4)
	    params = node.children[2];
	else
	    params = node.chuldren[3];


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
	int nlocals = (funNode.stackSize/4)-Math.max(nargs,6);	
	String[] locals = new String[nlocals];
	for (int i=0;i<funNode.allDeclarations.size();i++){
	    Node currentNode = ((Node)funNode.allDeclarations.get(i));
	    if ((currentNode.children.length==3) && (currentNode.children[2] instanceof NumNode)) {
		for (int j=0;j<((NumNode)currentNode.children[2]).value;j++)
		    locals[i+j]=currentNode.children[1].toString()+"["+j+"]";
		i+=j;
	    }
	    else
		locals[i]=currentNode.children[currentNode.children.length-1].toString();
	}
	//--------------------------------------


	int nOutgoingArgs = funNode.maxcallsize;//nOutgoingArgs: size of biggest outgoing call from this function

	ResourceTable resourceTable = new ResourceTable(stackFrameSize,nargs,args,nlocals,locals,nOutgoingArgs, this);//create the ResourceTable

	emit(dotalign,INTSIZE);//.align 4
	emit(dotglobal,node.children[1].toString());//.global <functionname>
	functionlabel=getLabel(node.children[1].toString());//get a new unused label
	defineLabel(functionlabel);//<functionname>:
	emit(prologue,"0");//prologue 0
	emit(save,stackpointer,"-"+stackFrameSize,stackpointer);// save %sp,-(stackframe size), %sp
	emit(prologue,"1");//prologue 1

	for (int i=0;i<Math.min(nargs,6);i++)
	    if (!resourceTable.getPassedArgFP(i).equals(NOTFOUND))
		emit(store,resourceTable.iRegTable[i][0],resourceTable.getPassedArgFP(i)); //st %i0,[fp+68]
	
	compStmtCodeGen(node.children[node.children.length-1], resourceTable);
	emit(ret);//ret
	emit(restore);//restore
	return;
    }
	
    private String getLabel(String functionname){
	int labelcounter=0;
	String returnLabel=functionname;
	while (labelTable.containsValue(returnLabel))
	    returnLabel=functionname+(new Integer(labelcounter)).toString();
	    
	labelTable.put(functionname, returnLabel);
	return returnLabel;
    }
	
    private String getLabel(){
	String returnLabel="LL1";
	while (labelTable.containsValue(returnLabel))
	    returnLabel="LL"+(new Integer(globalLabelCounter)).toString();

	return returnLabel; // return a unique label
    }

    private void defineLabel(String labelstring){
	filewriter.println(labelstring+":");
	return;
    }

    private void compStmtCodeGen(Node node, ResourceTable resourceTable){
	if (node.children.length==2){
	    localDecsCodeGen(node.children[0], resourceTable);
	    stmtListCodeGen(node.children[1], resourceTable);
	}
	else if (node.children[0] instanceof LocalDecsNode)
	    localDecsCodeGen(node.children[0], resourceTable);
	else
	    stmtListCodeGen(node.children[0], resourceTable);	
	return;
    }

    private void stmtCodeGen(Node node, ResourceTable resourceTable){
	return;
    }

    private void stmtListCodeGen(Node node, ResourceTable resourceTable){
	for (int i=0;i<node.children.length;i++)
	    stmtCodeGen(node.children[0], resourceTable);
	return;
    }
    private String localDecsCodeGen(Node node, ResourceTable resourceTable){
	for (int i=0; i<node.children.length;i++)
	    varDeclCodeGen(node.children[i], resourceTable);
	return new String();
    }
    private String exprStmtCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String ifStmtCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String ifStartCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String ifRemCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String whileStmtCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String forStmtCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String returnCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String expressionCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String orExprCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String andExprCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String relExprCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String addExprCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String termCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String unaryExprCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }
    private String primaryExprCodeGen(Node node, ResourceTable resourceTable){
	return new String();
    }	

    private String returnStmtCodeGen(Node node, ResourceTable resourceTable){
	return new String();
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
        
}

 //**********************************************************************
    /* are we not using this anymore???, I can't remember...


      private String expressionCodeGen(Node node, ResourceTable resourceTable){
      String returnVal = new String();
      String reg1 ;
      if(node.children.length==1){
      reg1 = orExprCodeGen(node.children[0], resourceTable);
      return reg1;
      }

      String stacklocRegister = orExprCodeGenL(node.children[0], resourceTable);
      if(((TerminalNode)node.children[1]).token.symbolNumber == 27){ //operator +=
      String reg2 = expressionCodeGen(node.children[2], resourceTable);
      reg1 = resourceTable.getRegister();	    
      emit(add, reg1, reg2, reg1);
      resourceTable.releaseRegister(reg2);
      emit(store, reg1, "["+stacklocRegister+"]");
      returnVal=reg1;
      }
      else if(((TerminalNode)node.children[1]).token.symbolNumber == 28){ //operator -=
      String reg2 = expressionCodeGen(node.children[2], resourceTable);
      emit(sub, reg1, reg2, reg1);
      resourceTable.releaseRegister(reg2);
      String id = resourceTable.lookupVariables_Registers(reg1);
      String stackloc = resourceTable.lookupStackFrame(id);
      emit(store, reg1, stackloc);
      returnVal=reg1;
      }
      else{ //operator =
      String reg2 = expressionCodeGen(node.children[2], resourceTable);
      emit(move, reg2, reg1);
      resourceTable.releaseRegister(reg2);
      String id = resourceTable.lookupVariables_Registers(reg1);
      String stackloc = resourceTable.lookupStackFrame(id);
      emit(store, reg1, stackloc);
      returnVal=reg1;
      }

      return returnVal;
      }
    */
