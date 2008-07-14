package ssd;
 


import java.util.*;
import java.io.*;

public class extractColoredText{
    public static void main(String args[]) throws Exception{


	//	while(sc.hasNext())
	//	    System.out.println(sc.next());
				 
	ChiSqDocument doc1 = new ChiSqDocument(new File(args[0]));
	//	System.out.println("<"+((Sentence)doc1.sentences.get(0)).toString()+">");
	//System.out.println(doc1.getWordCount());
	//	doc1.printFrequencies();
	System.out.println(doc1.printToHTML());
    }

    
}
