import java.util.*;

public class ParseStack{
    ArrayList stack;
    int stackPointer;
    boolean diag4=false,diag1=false, diag2=true;
    //diag2 are important messages that should be outputted if stackPointer is being a bad boy

    Lexer lexer;
    
    public String toString(){
	return stack.toString();
    }

    public ParseStack(Lexer lexer){
	this.lexer=lexer;
	stack = new ArrayList();
	stackPointer=0;

    }

    public Object getNext(){

	if (stack.size()==0 && stackPointer==0){
	    //System.out.println("I read " + lexer.peekNextToken());	    
	    if (lexer.hasMoreTokens()){
		stack.add(lexer.getNextToken());
		stackPointer++;
		return stack.get(stackPointer-1);
	    }
	    return new EOFToken();
	}
	else if (stackPointer < stack.size()){ 	    
	    return stack.get(stackPointer++);
	}
	else if (stackPointer == stack.size()) // execute this case only if 
	    //stackpointer points to element right on top of stack, i.e. asks for a new element
	    { 
		if (diag4)
		    System.out.println("I read " + lexer.peekNextToken());
		if (lexer.hasMoreTokens()){
		    stack.add(lexer.getNextToken());
		    if (diag1)
			System.out.println("stackdump: " + stack);
		    if (diag1)
			System.out.println("stackpointer: " + stackPointer);
		    return stack.get(stackPointer++);
		}
		return new EOFToken();
	    }
	else{//execute this case if stackpointer is point somewhere absurdly above the top of stack
	    if (diag2)
		System.out.println("**Diag warning: invalid stack ref: getNext(), SP=" + 
				   stackPointer + " for a stack of size " + stack.size());
	    return new EOFToken();
	}
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
	
	if (stackPointer<=stack.size() && stackPointer >0)
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
