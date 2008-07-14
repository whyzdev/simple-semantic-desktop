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
    
    public int stackSize;

    
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
    public PrimaryExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="primary-expr";
	isCall=false;
    }  
}

class ArgsExprNode extends Node{
    public ArgsExprNode(int numChildren){
	this.numChildren = numChildren;
	children=new Node[numChildren];
	nodeStringValue="args";
    }  
}




