

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
    static final int ZERO_OCCURENCE_PENALTY=15;
    static final int CHISQ_WINNER_LIMIT=5;
    static final boolean WEIGH_BY_ENTITY_SUM=true;

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

    static String[] typesOfEntities;
    static int[] frequenciesOfEntities;
    static String[] namesOfEntities ;
    static DoccSet[] documentsOfEntities ;
    static String[] namesOfEntities_2;	
    static AkshatDocument[] docArray;
    static int n_entities;
    

    public static void main(String[] args) throws Exception{
	posNameArray = new String[3];
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
	docArray = new AkshatDocument[inputFiles.length];
	docSet = new HashSet<AkshatDocument>();
	n_documents = inputFiles.length;

	NodeList n ;
	
	n = getSentenceNodeList(outDir + "/_entityanalysis");

	typesOfEntities = new String[n.getLength()];
	namesOfEntities = new String[n.getLength()];
	namesOfEntities_2 = new String[n.getLength()];
	documentsOfEntities = new DoccSet[n.getLength()];
	frequenciesOfEntities = new int[n.getLength()];

	for (int i=0;i<n.getLength();i++)
	    frequenciesOfEntities[i]=0;

	int highestid=0;

	int ctr=-1;
	for (int i=0;i<n.getLength();i++){	    
	    String sentencetext = n.item(i).getFirstChild().getNodeValue();
	    System.out.println("sentencetext:" + sentencetext);

	    if (sentencetext.indexOf("AkshatEntityMachine") != -1){
		ctr++;
		docArray[ctr]=new AkshatDocument(inputFiles[ctr],-1,ctr);
		docArray[ctr].entityFreqs=new int[n.getLength()];
	    }			
	    else{

	    NodeList nl2 = n.item(i).getChildNodes();
	    for (int j=0;j<nl2.getLength();j++){

		    String nodeval= nl2.item(j).getFirstChild().getNodeValue();
		    System.out.println("nodeval: " + nodeval);
		    int id = Integer.parseInt(nl2.item(j).getAttributes().getNamedItem("ID").getNodeValue());

		    if (id > highestid)
			highestid=id;

		    if (id!= -1 ){
			if (documentsOfEntities[id] == null )
			    documentsOfEntities[id] = new DoccSet(id,new HashSet<String>(),0);		
		    
			documentsOfEntities[id].ht.add(inputFiles[ctr]);
			typesOfEntities[id] =nl2.item(j).getAttributes().getNamedItem("TYPE").getNodeValue();		    

			if (namesOfEntities[id] !=null)
			    namesOfEntities[id] += ", " + nodeval;
			else 
			    namesOfEntities[id] = ", " + nodeval;

			if (namesOfEntities_2[id] == null || namesOfEntities_2[id].length() < nodeval.length())
			    namesOfEntities_2[id]=nodeval;
			frequenciesOfEntities[id]++;
			docArray[ctr].entityFreqs[id]++;
		    

		    }
		}
	    }
	}
	
	for (int i=0;i<n.getLength();i++){	    
	    if ( documentsOfEntities[i] != null )
		documentsOfEntities[i].frequency = frequenciesOfEntities[i];
	    else
		documentsOfEntities[i] = new DoccSet();
	}

	n_entities = highestid+1;
	Arrays.sort(documentsOfEntities, byFreq);
	for (int i=0;i<n_entities;i++)
	    if (typesOfEntities[i] != null)
		System.out.println(namesOfEntities[i]);	
	
	for(int i=0;i<ctr;i++){
	    for(int j=0;j<n_entities;j++)
		docArray[i].entitySum+=docArray[i].entityFreqs[j];
	    docSet.add(docArray[i]);
	}
	
	System.out.println(makeClusters(6,false));


    }


    static Set<Set<AkshatDocument>> makeClusters(int K, boolean singleLinkClustering){
	//calculate distances and put them in for each document
	distances = new double[docArray.length][docArray.length];	
	for(int i=0;i<docArray.length;i++)
	    for(int j=0;j<docArray.length;j++)
		distances[i][j]=distance(docArray[i],docArray[j]);

	for(int i=0;i<n_documents;i++){
	    System.out.printf("[%d] ", i);
	    for(int j=0;j<n_documents;j++)
		System.out.printf("%.1f ", distances[i][j]);
	    System.out.println();
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

    public static double distance(AkshatDocument doca, AkshatDocument docb){
	double dist=0;
	for (int i=0;i<n_entities;i++){
	    if (WEIGH_BY_ENTITY_SUM)
 		dist+=Math.abs( ((double)doca.entityFreqs[i]/(double)doca.entitySum)-((double)docb.entityFreqs[i]/(double)docb.entitySum))  ;
	    else
 		dist+=Math.abs(doca.entityFreqs[i]-docb.entityFreqs[i]);
	    //	    if (doca.entityFreqs[i] == 0 || docb.entityFreqs[i] ==0)
	    //		dist+=ZERO_OCCURENCE_PENALTY/(doca.entitySum+docb.entitySum);
	}
	return dist;
    }



    static Distance<AkshatDocument> CUSTOM_DISTANCE
        = new Distance<AkshatDocument>() {
	public double distance(AkshatDocument doca, AkshatDocument docb){
	    return doca.distances[docb.index];
	}
    };





    public static void useLingPipeNegativityTest(String inDir, String outDir){
	
	try {
	    ProcessBuilder pb = new ProcessBuilder( "./corefall.sh " + outDir);
	    pb.directory(new File("/local/s/lingpipe/demos/generic/bin/"));
	    System.err.println("Starting thread");
	    Process p = pb.start();
	    FileOutputStream fos = new FileOutputStream("lpout");
	    StreamGobbler outputGobbler = new 
		StreamGobbler(p.getInputStream(), "OUTPUT", fos);
            outputGobbler.start();
	    int exitVal=p.waitFor();
	    fos.close();
	}
	catch(Exception e){
	    System.err.println("ERROR:Error executing lingpipe Entity Coreference tagger");
	    e.printStackTrace();
	}
    }


    public static void useLingPipeSubjectivityTest(String inDir, String outDir){
	
	try {
	    ProcessBuilder pb = new ProcessBuilder( "./corefall.sh " + outDir);
	    pb.directory(new File("/local/s/lingpipe/demos/generic/bin/"));
	    System.err.println("Starting thread");
	    Process p = pb.start();
	    FileOutputStream fos = new FileOutputStream("lpout");
	    StreamGobbler outputGobbler = new 
		StreamGobbler(p.getInputStream(), "OUTPUT", fos);
            outputGobbler.start();
	    int exitVal=p.waitFor();
	}
	catch(Exception e){
	    System.err.println("ERROR:Error executing lingpipe Entity Coreference tagger");
	    e.printStackTrace();
	}
    }

    public static void useLingPipeCoRefFinder(String outDir){
	
	try {
	    //	    ProcessBuilder pb = new ProcessBuilder( "cat "+ outDir  +  "/temp_entity/* |  ./cmd_coref_en_news_muc6.sh > " + outDir + "_entityanalysis");
	    ProcessBuilder pb = new ProcessBuilder( "./cmd_coref_en_news_muc6.sh -inDir=" + outDir + "/temp_entity -outDir=" + outDir);
	    pb.directory(new File("/local/s/lingpipe/demos/generic/bin"));
	    System.err.println("Starting thread " + pb.environment());
	    
	    Process p = pb.start();
	    FileOutputStream fos = new FileOutputStream("lpout");
	    StreamGobbler outputGobbler = new 
		StreamGobbler(p.getInputStream(), "OUTPUT", fos);
            outputGobbler.start();
	    int exitVal=p.waitFor();
	    fos.close();
	}
	catch(Exception e){
	    System.err.println("ERROR:Error executing lingpipe Entity Coreference tagger");
	    e.printStackTrace();
	}
    }



    private static NodeList  getSentenceNodeList(String filename){

	try {
	    File file = new File(filename);
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(file);
	    doc.getDocumentElement().normalize();
	    NodeList nodeLst = doc.getElementsByTagName("s");
	    return nodeLst;
	}
	catch (Exception e) {
	    System.err.println("ERROR: error reading XML file <" + filename + ">");
	    e.printStackTrace();
	}	           
	return null;
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


class DoccSet {
    HashSet<String> ht;
    int index;
    int frequency;
    String chosenname;
    String[] names;
    DoccSet(int index, HashSet<String> ht, int frequency, String[] names, String chosenname){
	this.index=index;
	this.frequency=frequency;
	this.ht=ht;
	this.names=names;
	this.chosenname=chosenname;
    }

    DoccSet(int index, HashSet<String> ht, int frequency){
	this.index=index;
	this.frequency=frequency;
	this.ht=ht;
    }

    DoccSet(){
	this.frequency=0;
    }

}

class EntityFreqComparator implements Comparator<DoccSet>{
    public int compare(DoccSet a,DoccSet b){
	if(a.frequency > b.frequency)
	    return -1;
	else 	if(a.frequency < b.frequency)
	    return 1;
	else
	    return 0;
    }
}
 