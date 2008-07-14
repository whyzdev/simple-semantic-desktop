import java.util.*;
import java.io.*;
/*
 *
 ACC - Akshat and Chris Compiler for C
 - acc is the main class for the compiler.At this point, 
 it just calls the Lexer on the given input and prints out tokens.
 10/3/2005
 *
*/
public class acc
{
    
    public static void main(String args[]) 
    {
	try {
	BufferedReader reader;
	if(args.length == 0)
	    {
		reader = new BufferedReader(new InputStreamReader(System.in));
	    }
	else 
	    {
		reader = new BufferedReader(new FileReader(args[0]));
	    }
	Lexer lexer = new Lexer(reader);
	Parser parser = new Parser(lexer);
	Node syntaxTree=parser.parse();
	//System.out.println(syntaxTree.printTree());
	System.out.println(syntaxTree.reduceChildren());
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
