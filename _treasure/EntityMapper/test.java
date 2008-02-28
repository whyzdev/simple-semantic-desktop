import java.util.*;
import java.util.regex.*;
import java.io.*;

public class test{
    public static void main (String args[]) throws Exception{
	//regex test
	Scanner sc = new Scanner(new File("/local/s/maddox/9630"));
	String temp="\n.AkshatEntityMachine.\n";
	while(sc.hasNext())
	    temp += "\n" + sc.nextLine();	
	
	//	temp=temp.replaceAll("<!--.*-->", " ");    
	temp=temp.replaceAll("<STYLE.*?>.*?</STYLE>"," ");
	/*	temp=temp.replaceAll("<(s(?:cript|tyle))[^>]*>.*?</(s(?:cript|tyle))[^>]*>"," ");
	temp=temp.replaceAll("<(S(?:CRIPT|TYLE))[^>]*>.*?</(S(?:CRIPT|TYLE))[^>]*>"," ");
	temp=temp.replaceAll("<style[\\s.]*?/style>"," ");
	temp=temp.replaceAll("<STYLE[\\s.]*?/STYLE>"," ");*/
	//	temp=temp.replaceAll("<[a-zA-Z/!][^>]*>"," ");
	
	PrintStream file = new PrintStream(new FileOutputStream("/local/s/maddox_out/test"));
	file.println(temp);
	file.close();		    		    
	
	

	//code for testing html color printing
	if (false){	int color1=200;
	int color2=3;
	int color3=100;
	java.awt.Color wordcolor = new java.awt.Color(color1,color2,0);
	int color=color1*255*255+color2*255+color3;
	java.text.DecimalFormat nft = new
	    java.text.DecimalFormat("00");
	nft.setDecimalSeparatorAlwaysShown(false);
	//	System.out.println(nft.format(color1));

	System.out.printf("#%2x%2x%2x" , color1,color2,color3);
	}
	/*	String str = "The End.";
	System.out.println(str.substring(str.length()-1,str.length()));
	*/


	
	/*
//scanner test
	Scanner sc = new Scanner (new File(args[0]),"US-ASCII");
	Pattern onlyWords = Pattern.compile("[^\\s]+");
	//	    System.out.println(sc.toString());	
	sc.useDelimiter(Pattern.compile("[\\s]+"));
		while(sc.hasNext(onlyWords)){	    
		    System.out.print(" " + sc.next(onlyWords));
		}

	System.out.println(sc);
*/
	/*
	String str="";
	BufferedReader reader = new BufferedReader(new FileReader(args[0]));
	while (reader.ready())
	    str+=reader.readLine();
	StringTokenizer st = new StringTokenizer(str);
	while(st.hasMoreTokens())
	    System.out.println(st.nextToken());
	*/




		    /*		

		    //hashmap test
	HashMap<String,Integer> h = new HashMap<String,Integer>();
	h.put("a",new Integer(42));
	h.put("b",new Integer(21));
	h.put("c",new Integer(33));
	Set<MapEntry<String,Integer>> entries = h.entrySet();
	int[] words = new int[entries.length];
	Iterator i = entries.iterator();
	while(i.hasNext())
	    
	

		h.put("b",45);
	h.put("c",53);
	h.put("d",21);
	h.put("e",54);
	h.put("f",31);
	h.put("g",41);
	h.put("h",46);
	h.put("i",16);
	h.sort();
	System.out.println(h);
	*/
    }
}

