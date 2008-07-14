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
