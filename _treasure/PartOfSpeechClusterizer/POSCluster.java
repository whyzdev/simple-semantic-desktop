

import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.ClusterScore;
import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.cluster.Dendrogram;

import com.aliasi.util.Counter;
import com.aliasi.util.Distance;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import com.aliasi.tokenizer.EnglishStopListFilterTokenizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseFilterTokenizer;
import com.aliasi.tokenizer.PorterStemmerFilterTokenizer;
import com.aliasi.tokenizer.Tokenizer;


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

import javax.swing.*; 

public class POSCluster{
    static final int ZERO_OCCURENCE_PENALTY=0;
    static final int CHISQ_WINNER_LIMIT=0;
    static final double AVOIDANTS_COEFF=0.01;
    static HashMap<String,TreeSet<String>> posTable_set;
    static HashMap<String,ArrayList<String>> posTable;
    static ArrayList<Hashtable<String,int[]>> posCounts;
    static String[] docs;
    static int n_documents=0;
    static int n_pos=0;
    static int[] tokenCount;
    static double[][] distances;
    static Set<AkshatDocument> docSet;
    static String[] posNameArray;
    static String inDir;
    static String outDir;
    static ChiSqDocument[] chiSquaredDocuments;
    public static void main(String[] args) throws Exception{
	inDir = args[0];  
	outDir = args[1];
	posTable_set = new HashMap<String,TreeSet<String>>();
	posTable = new HashMap<String,ArrayList<String>>();
	posCounts = new ArrayList<Hashtable<String,int[]>>();
	//Populate the Part of Speech matrices for each document in passed directory

	File in_directory = new File(inDir);
	docs = in_directory.list();
	chiSquaredDocuments = new ChiSqDocument[docs.length];
	//read in documents and calculate chi squared measures
	for (int i=0;i<docs.length;i++){				
	    chiSquaredDocuments[i] = new ChiSqDocument(new File(inDir + "/" + docs[i]));
	}
	
	//lingpipe the POS tags from inDir to outDir (ONLY IF OUTDIR IS EMPTY)
	if (((new File(outDir)).list().length )==0){
	    System.out.println("outDir is " + outDir);
	    useLingPipePOS(inDir,outDir+"/temp_pos");
	}


	File tagged_directory = new File(outDir + "/temp_pos");
	docs = tagged_directory.list();
	if (docs == null){
	    System.err.println("ERROR: No Files in inDir");
	    System.exit(-1);
	}
	


	//Get all words for all POS's in the master list
	for (int i=0;i<docs.length;i++){				
	    NodeList n = getTokenNodeList(tagged_directory.toString() + "/" + docs[i]);   
	    for (int j=0;j<n.getLength();j++){
		Node fstNode = n.item(j);
		String cpos= fstNode.getAttributes().item(0).getNodeValue(); //get part of speech
		String ctoken = fstNode.getFirstChild().getNodeValue();		    						    
		if (!cpos.equals("nil")){		    
		    //make sure this POS has an entry in the posTable_set
		    if (!posTable_set.containsKey(cpos))			   
			posTable_set.put(cpos,new TreeSet<String>());
		    TreeSet<String> wordlist = posTable_set.get(cpos);			
		    wordlist.add(ctoken);		
		}
	    }
	}
	
		
	

	Set keySet = posTable_set.keySet();
	Iterator i1 = keySet.iterator();
	posNameArray= new String[keySet.size()];
	int ctrr=0;
	while (i1.hasNext()){
	    String i_pos = ((String)i1.next());
	    String[] a = new String[1];
	    ArrayList<String> arr = new ArrayList<String>(((TreeSet<String>)posTable_set.get(i_pos)));
	    posTable.put(i_pos, arr);
	    System.out.println(i_pos);
	    posNameArray[ctrr++]=i_pos;
	}
		Arrays.sort(posNameArray);
	n_documents = docs.length;	
	n_pos = posTable.size();
	tokenCount= new int[n_documents];
		
	docSet = new HashSet<AkshatDocument>();
		
	for (int i=0;i<docs.length;i++){				
	    Hashtable<String,int[]> ht = new Hashtable<String,int[]>();
	    tokenCount[i]=0;
	    String s1="";
	    Iterator l= posTable.keySet().iterator();
	    while(l.hasNext())
		ht.put((s1=(String)l.next()),new int[posTable.get(s1).size()]);
	    
	    NodeList n = getTokenNodeList(tagged_directory.toString() + "/" + docs[i]);   
	    for (int j=0;j<n.getLength();j++){
		Node fstNode = n.item(j);
		String cpos= fstNode.getAttributes().item(0).getNodeValue(); //get part of speech
		String ctoken = fstNode.getFirstChild().getNodeValue();		    						    
		if (!cpos.equals("nil")
		    && !isChiSquaredWinner(ctoken,chiSquaredDocuments[i])
		    ){		    
		    ArrayList<String> masterWordList = ((ArrayList<String>)posTable.get(cpos));
			
		    //make sure this POS has an entry in this document's posCount table
		    if (!ht.containsKey(cpos))			   
			ht.put(cpos,new int[masterWordList.size()]);
			
		    int[] wordCountVector = ht.get(cpos);
		    wordCountVector[masterWordList.indexOf(ctoken)]++;
		    tokenCount[i]++;
		}
	    }
	    docSet.add(new AkshatDocument(docs[i],ht,tokenCount[i],i));
	    posCounts.add(ht);
	}	   
	

	/*
	//for debugging only
	int[] test1 = posCounts.get(0).get("nns");

	for(int k=0;k<test1.length;k++)
	System.out.println(posTable.get("nns").get(k) + "=" + test1[k]);

	*/

	
			
	//Create Clusters based on Part of Speech Matrices       
	//Remember: at this point, posCounts.get(Document's Index).get(POS's Index)[Word's Index] gives you a count of that word as that POS in the document.

	//For each POS k, the cluster element is:
	// posCounts.get(i).get(k) (for all i)
	
	String[] chosenPartsOfSpeech = {"vb","np"};
	Set<Set<AkshatDocument>> s = makeClusters(chosenPartsOfSpeech,10,false);
	System.out.println(s);


/*	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    createAndShowGUI();
		}
		});*/

    }


    static Set<Set<AkshatDocument>> makeClusters(String[] chosenPartsOfSpeech, int K, boolean singleLinkClustering){
	//calculate distances and put them in for each document

	distances = new double[posCounts.size()][posCounts.size()];	
	
	Iterator it1= docSet.iterator();
	while(it1.hasNext()){	
	    AkshatDocument current = (AkshatDocument)it1.next();
	    int i=current.index;
	    double[] distances_local = new double[n_documents];

	    Iterator it2= docSet.iterator();
	    while(it2.hasNext()){	
		AkshatDocument current2 = (AkshatDocument)it2.next();
		int j=current2.index;

		distances[i][j]=0;
		for(int k=0;k<chosenPartsOfSpeech.length;k++){
		    distances[i][j]+=distance(posCounts.get(i).get(chosenPartsOfSpeech[k]),posCounts.get(j).get(chosenPartsOfSpeech[k]),
					      current,current2);
		}
		
		double lowestdistance=0;
		for(int k=0;k<posNameArray.length;k++){
		    distances[i][j]-=AVOIDANTS_COEFF*distance(posCounts.get(i).get(posNameArray[k]),posCounts.get(j).get(posNameArray[k]),
					      current,current2);
		    if (distances[i][j] < lowestdistance)
			lowestdistance = distances[i][j];
		}
		
		for(int k=0;k<n_documents;k++){
		    for(int l=0;l<n_documents;l++){
			distances[i][j] += Math.abs(lowestdistance);
		    }
		}
		
		distances_local[j]=distances[i][j];		
	    }

	    current.setDistances(distances_local);
	}
	
	Dendrogram<AkshatDocument> dendrogram;

	if (!singleLinkClustering){
	    HierarchicalClusterer<AkshatDocument> clClusterer
		= new CompleteLinkClusterer<AkshatDocument>(CUSTOM_DISTANCE);
	    dendrogram
		= clClusterer.hierarchicalCluster(docSet);
	    return dendrogram.partitionK(K);
	}
	else{
	    HierarchicalClusterer<AkshatDocument> slClusterer
		= new SingleLinkClusterer<AkshatDocument>(CUSTOM_DISTANCE);
	    dendrogram
		= slClusterer.hierarchicalCluster(docSet);
	    return dendrogram.partitionK(K);
	}



	/*	System.out.println(completeLinkDendrogram.prettyPrint());

		for (int k = 1; k <= 10;++k) {
		Set<Set<AkshatDocument>> clusteringK= completeLinkDendrogram.partitionK(k);
		System.out.println(k + "  " + clusteringK);
		}*/
    }

    public static double distance(int[] a,int[] b, AkshatDocument doca, AkshatDocument docb){
	double dist=0;
	if (a.length != b.length)
	    System.err.println("warning: unequal lengths of arrays compared:"+a.length+","+b.length);

	int a_sum=1,b_sum=1;
	for (int i=0;i<a.length && i<b.length;i++){
	    a_sum+=a[i];
	    b_sum+=b[i];
	}

	for (int i=0;i<a.length && i<b.length;i++){
	    dist+=Math.abs(((double)a[i]/(double)a_sum) -  ((double)b[i]/(double)b_sum));
	    if (a[i] != b[i] && (a[i]==0 || b[i]==0))
		dist+=ZERO_OCCURENCE_PENALTY/(a_sum*b_sum);
	}
	return dist;
    }




    static Distance<AkshatDocument> CUSTOM_DISTANCE
        = new Distance<AkshatDocument>() {
	public double distance(AkshatDocument doca, AkshatDocument docb){
	    return doca.distances[docb.index];
	}
    };





    public static void useLingPipePOS(String inDir, String outDir){
	
	try {
	    ProcessBuilder pb = new ProcessBuilder( "./cmd_pos_en_general_brown.sh", "-inDir=" + inDir, "-outDir=" + outDir);
	    pb.directory(new File("/local/s/lingpipe/demos/generic/bin"));
	    System.err.println("Starting thread");
	    Process p = pb.start();
	    FileOutputStream fos = new FileOutputStream("lpout");
	    StreamGobbler outputGobbler = new 
		StreamGobbler(p.getInputStream(), "OUTPUT", fos);
            outputGobbler.start();
	    int exitVal=p.waitFor();
	}
	catch(Exception e){
	    System.err.println("ERROR:Error executing lingpipe POS tagger");
	    e.printStackTrace();
	}
    }

    private static NodeList  getTokenNodeList(String filename){

	try {
	    File file = new File(filename);
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(file);
	    doc.getDocumentElement().normalize();
	    NodeList nodeLst = doc.getElementsByTagName("token");
	    return nodeLst;
	}
	catch (Exception e) {
	    System.err.println("ERROR: error reading XML file <" + filename + ">");
	    e.printStackTrace();
	}	           
	return null;
    }

    private static void printARFF(String pos, String filename){
	try{
	    PrintStream output = new PrintStream( new File( filename) );
	    output.println("@RELATION "+ pos  + "\n");
	    Iterator i=posTable.get(pos).iterator();
	    while(i.hasNext()){
		String s2=(String)i.next();
		if (Pattern.matches("\\p{Alnum}*",s2))
		    output.println("@ATTRIBUTE "+ s2  + "\tNUMERIC\n");
		else 
		    output.println("@ATTRIBUTE At"+ ((int)(Math.random()*1000000)) + "\tNUMERIC\n");
	    }
	    output.println("\n@ATTRIBUTE _filename STRING\n");
	    output.println("\n@DATA");
	
	    for(int j=0;j<posCounts.size();j++){
		int[] counts = posCounts.get(j).get(pos);
		for(int k=0;k<counts.length;k++)
		    output.print(counts[k] + ",");	    
		output.println(docs[j]);
	    }
	    output.flush();
	    output.close();
	}
	catch(Exception e){
	    e.printStackTrace();
	}
    }


    private static void printDistances(String filename){
	try{
	    PrintStream output = new PrintStream( new File( filename) );
	    output.print("*\t");
	    for(int i=0;i<docs.length;i++)
		output.print(docs[i] + "\t");
	    output.print("\n");
	
	    for(int i=0;i<docs.length;i++){
		output.print(docs[i] + "\t");
		for(int j=0;j<docs.length;j++)
		    output.print(distances[i][j] + "\t");
		output.print("\n");
	    }
	
	    output.flush();
	    output.close();
	}
	catch(Exception e){
	    e.printStackTrace();
	}
    }

    public static boolean isChiSquaredWinner(String ctoken,ChiSqDocument doc) {
	for(int i=0;i<CHISQ_WINNER_LIMIT && i<doc.chiSquareTable.length;i++){
	    if (ctoken.equals(doc.chiSquareTable[i].text)){
		System.err.println("avoiding word " + ctoken + " because it is a ChiSq Winner");
		return true;
	    }
	}
	return false;	
    }

}

