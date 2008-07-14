public abstract class Node{
    Node[] children;
    String nodeStringValue;
    public String toString(){
	String returnstring = "( " + nodeStringValue;	
	for (int i=0;i<children.length;i++)
	    if (children[i] != null)
		returnstring += " " + children[i].toString();
	returnstring += " )";
	return returnstring;
    }
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
    }
    
    public Node reducedNode(){
	if (children !=null){
	    if (children.length == 1){
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
    }   

    public Node reduceChildren(){
	if (children !=null){
	    for (int i=0;i<children.length;i++){ 
		if (children[i] != null) {
		    //System.out.println("children[i]:" + children[i]);
		    children[i]=children[i].reducedNode();
		}
	    }
	}
	return this;
    }   
}

class TerminalNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public TerminalNode(Token token){
	children=new Node[0];
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
	nodeStringValue="Error";	
    }
}

class ProgramNode extends Node{
    public ProgramNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="program";
    }  
}

class IdentifierNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    public IdentifierNode(Token token){
	children=new Node[0];
	this.token=token;
	nodeStringValue=token.toString();
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
	this.token=token;
	nodeStringValue=token.toString();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class VarDeclNode extends Node{
    public VarDeclNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="var-declaration";
    }  
}

class RealNode extends Node{
    Node[] children;
    String nodeStringValue;
    Token token;
    float value;
    public RealNode(FloatToken token){
	children=new Node[0];
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
    int value;
    public NumNode(IntegerToken token){
	children=new Node[0];
	this.token=token;
	nodeStringValue=token.toString();
	this.value = token.getValue();
    }

    public String toString(){
	return nodeStringValue;
    }
}

class FunDeclNode extends Node{
    public FunDeclNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="fun-declaration";
    }  
}

class ParamsNode extends Node{
    public ParamsNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="params";
    }  
}

class ParamNode extends Node{
    public ParamNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="param";
    }  
}

class CompStmtNode extends StatementNode{
    public CompStmtNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="compound-statement";
    }  
}

class StmtListNode extends Node{
    public StmtListNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="statement-list";
    }  
}

class LocalDecsNode extends Node{
    public LocalDecsNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="local-declarations";
    }  
}

abstract class StatementNode extends Node{
   
}

class ExprStmtNode extends StatementNode{
    public ExprStmtNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="expression-statement";
    }  
}
//OptExprNode changed to ExprNode to generic-ise the idea
class ExprNode extends Node{
    public ExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="optional-expression";
    }  
}

class IfStmtNode extends StatementNode{
    public IfStmtNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="if-statement";
    }  
}

class IfStartNode extends Node{
    public IfStartNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="if-start";
    }  
}

class IfRemNode extends Node{
    public IfRemNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="if-remainder";
    }  
}

class WhileStmtNode extends StatementNode{
    public WhileStmtNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="while-statement";
    }  
}

class ForStmtNode extends StatementNode{
    public ForStmtNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="for-statement";
    }  
}

class ReturnStmtNode extends StatementNode{
    public ReturnStmtNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="return-statement";
    }  
}

class ExpressionNode extends Node{
    public ExpressionNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="expression";
    }  
}

class OrExprNode extends Node{
    public OrExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="or-expr";
    }  
}

class AndExprNode extends Node{
    public AndExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="and-expr";
    }  
}

class RelExprNode extends Node{
    public RelExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="rel-expr";
    }  
}

class AddExprNode extends Node{
    public AddExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="add-expr";
    }  
}

class TermNode extends Node{
    public TermNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="term";
    }  
}


class UnaryExprNode extends Node{
    public UnaryExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="unary-expr";
    }  
}

class PrimaryExprNode extends Node{
    public PrimaryExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="primary-expr";
    }  
}

class ArgsExprNode extends Node{
    public ArgsExprNode(int numChildren){
	children=new Node[numChildren];
	nodeStringValue="args";
    }  
}
