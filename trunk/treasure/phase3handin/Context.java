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
