import java.util.*;
import java.io.*;

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
	
	while(lexer.hasMoreTokens()){
	    System.out.print( ((Token)lexer.getNextToken()).toString() + " ");
	}

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
