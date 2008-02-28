
import java.lang.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CleanTextForNER{

    static String inDir;
    static String outDir;

    public static void main(String args[]) throws Exception{

	inDir=args[0];
	outDir=args[1];
	File inDir=new File(args[0]);
	File outDir=new File(args[1]);


	

	new File(outDir + "/temp_entity").mkdir();
	new File(outDir+"/_entityanalysis").createNewFile();
	File tempDir = new File(outDir + "/temp_entity");

	Comparator<DoccSet> byFreq = new EntityFreqComparator();

	//	System.out.println("Making directory " + tempDir + " is directory: " + tempDir.isDirectory());

	String[] inputFiles = inDir.list();
	
	for (int i=0;i<inputFiles.length;i++){
	    Scanner sc = new Scanner(new File(inDir + "/" + inputFiles[i]));
	    String temp="\n.AkshatEntityMachine.\n";
	    while(sc.hasNext())
		temp += "\n" + sc.nextLine();	
	    
	    temp=temp.replaceAll("<style[^>]*>[^<]*</style>"," ");
	    temp=temp.replaceAll("<STYLE[^>]*>[^<]*</STYLE>"," ");
	    temp=temp.replaceAll("<[a-zA-Z/!][^>]*>"," ");

	    PrintStream file = new PrintStream(new FileOutputStream(tempDir + "/"+inputFiles[i]));
	    file.println(temp);
	    file.close();		    		    
	}

    }
    

}