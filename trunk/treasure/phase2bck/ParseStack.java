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
