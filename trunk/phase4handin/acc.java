import java.util.*;
import java.io.*;
/*
 *
 ACC - Akshat and Chris Compiler for C
 - acc is the main class for the compiler. At this point, 
 it just calls the Lexer on the given input and prints out tokens.
 10/3/2005
 *
*/
public class acc
{
    
    public static void main(String args[]) 
    {
	String filename="a.out.s";
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
		filename=args[0]+".s";
	    }
	Lexer lexer = new Lexer(reader);
	Parser parser = new Parser(lexer);
	Node syntaxTree=parser.parse();
	SemanticAnalyzer semAnalyzer = new SemanticAnalyzer(syntaxTree);
	ArrayList errors1 = semAnalyzer.checkProgram();
	ArrayList errors = parser.getErrors();
	if ((errors1.size() == 0) && (errors.size()==0)){
	    System.out.println("Program is free of errors");
	    CodeGenerator codegen1= new CodeGenerator(syntaxTree,filename);
	}
	else
	    System.out.println("Errors found in program:");
	
	if(syntaxTree instanceof ErrorNode){
	    for(int i = 0;i<errors.size(); i++)
		System.out.println(errors.get(i));
	    System.out.print("\n");
	}

	for(int i = 0;i<errors1.size(); i++)
	    System.out.println(errors1.get(i));
	
	    System.out.print("\n");
	

	    reader.close();
	}
	catch (FileNotFoundException f){
	    System.out.println("File " + args[0]  + "not found");
	}
	catch (IOException f){
	    System.out.println("I/O Exception");
	}
	

	
	
		

	
    }
}
