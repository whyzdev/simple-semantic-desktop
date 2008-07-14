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

