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
