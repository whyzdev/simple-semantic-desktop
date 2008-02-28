import java.util.*;
import java.io.*;

public class averageSentenceLength{
    public static void main(String args[]) throws Exception{
	ChiSqDocument doc1 = new ChiSqDocument(new File(args[0]));
	doc1.printSentenceLengths();
	System.out.println("Average:"+doc1.averageSentenceLength());
    }

    
}
