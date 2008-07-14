package ssd;

import java.util.*;

public class Entity{
    static 	boolean DIAG1 =false;
    int id;
    String type;         
    int clusterfreq;
    ArrayList<Integer> docfreqs;
    ArrayList<Document> documents;
    ArrayList<EntityOccurence> occurences;
    DocumentCluster cluster;
    int location_verified=-1;
    Set<String> occurenceSet;
    
    public Entity(int id, String type, DocumentCluster cluster){
	this.id=id;
	this.type=type;
	this.cluster=cluster;
	clusterfreq=0;
	docfreqs= new ArrayList<Integer>();
	documents = new ArrayList<Document>();
	occurences= new ArrayList<EntityOccurence>();
    }
    

    public void addOccurence(int start,int end, Document doc,String value){
	occurences.add(new EntityOccurence(start,end,doc,value));	
	if (!documents.contains(doc)){
	    documents.add(doc);
	    docfreqs.add(new Integer(0));
	}
	docfreqs.set(documents.indexOf(doc),new Integer(docfreqs.get(documents.indexOf(doc)).intValue() +1));
	clusterfreq++;
	if (type.equals("LOCATION") && location_verified!=-1){
	    if (cluster.checkLocation(value))
		location_verified=1;
	    else 
		location_verified=0;	    
	}
	    
	return;
    }
    

    public String toString(){
	String returnstring="";
	if (location_verified == -1 || location_verified == 1){
	 returnstring = "<" + type + "> '" + occurences.get(0) + "' [freq:" + clusterfreq+ "]in {";
	for(int i=0;i<documents.size();i++){
	    returnstring += documents.get(i) + "(" + docfreqs.get(i) + "), ";
	}
	returnstring += "}";
	}
	return returnstring;
    }
    
    static class EntityOccurence{
	int start,end;
	Document doc;
	String value;
	public EntityOccurence(int start, int end, Document doc, String value){
	    if (DIAG1){
		System.err.println(start + "," + end);
		System.err.println(doc.mText_str.substring(start,end));
	    }
	    this.start=start;
	    this.end=end;
	    this.doc=doc;
	    this.value=value.replaceAll("[\\W]"," ").trim();
	}
	public String toString(){
	    //   return doc.mText_str.substring(start,end-start);
	    return value;
	}
    }

    public String printUniqueOccurences () {
	String returnstring="";
	occurenceSet = new TreeSet();

	Iterator it = occurences.iterator();
	
	while(it.hasNext())
	    occurenceSet.add(it.next().toString());
	occurenceSet.remove(occurences.get(0).toString());

	it = occurenceSet.iterator();
	while(it.hasNext()){
	    String e =(String) it.next();
	    returnstring +=  e.toString() + ",";	    	    
	}
	return returnstring;

    }
}