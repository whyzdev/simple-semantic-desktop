import java.io.File;
import java.util.*;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;
import com.aliasi.lm.TokenizedLM;
import java.net.URLEncoder;
import java.io.*;


public class DocumentCluster{
    String name;
    HashMap<Integer,Entity> entities;
    Set<Document> docs;
    String airportlist;
    File dir;
    ScoredObject[] collocations;
    TokenizedLM backgroundModel;

    public DocumentCluster(Set<Document> docs, String name){
	this.name=name;
	this.docs=docs;
	entities = new HashMap<Integer,Entity>();
	try {
	airportlist = Files.readFromFile(new File("_Cities_With_Airports.txt"));
	}
	catch(Exception e){
	    System.err.println("ERROR: Airports corpus missing");
	}
    }    
    public void printEntities(){
	System.out.println("----------------------\nEntities of "+ name);
	for(int i=0;i<entities.size();i++){
	    if (entities.get(i)!=null)
		System.out.println("" + i + ": " + entities.get(i));
	}

    }

    public boolean checkLocation(String location){
	location=location.trim();
	if (airportlist.indexOf(location)==-1)
	    return false;
	else 
	    return true;
    }

    public void printEntitiesHTML(File entitiesDir, File docDir) throws Exception{
	DocumentCluster cluster =this;
	File outDir = new File(entitiesDir, cluster.name);
	outDir.mkdir();
	Iterator it = cluster.entities.values().iterator();
	while(it.hasNext()){
	    Entity e = (Entity)it.next();
	    FileWriter efile = new FileWriter(outDir + "/" + e.id + ".html");
	    efile.write("<html><head></head><body>\n");
	    efile.write("<h1>"+ e.occurences.get(0) + "</h1>\n");
	    efile.write("<h2>type:"+ e.type + "</h2>");
	    efile.write("<p><a target='showframe' href='http://www.google.com/search?hl=en&q=" +	java.net.URLEncoder.encode(e.occurences.get(0).toString())
			+"+site%3Aen.wikipedia.org&btnI=I%27m+Feeling+Lucky'>Search This Entity on Wikipedia</a>");
	    efile.write("<p><a target='showframe' href='http://www.google.com/search?hl=en&q=" +	java.net.URLEncoder.encode(e.occurences.get(0).toString())
			+"+'>Search This Entity on Google</a>");
	    String occurences=e.printUniqueOccurences();
	    if (!occurences.equals(""))
		efile.write("<p><b>Also Occurs in this cluster as</b>:"+ occurences + "</p>");
	    efile.write("<p><b>Occurs in this cluster "+ e.clusterfreq + " times</p>");
	    efile.write("<p><b>Occurs in these documents</b>:");
	    Iterator it2 = e.documents.iterator();
	    while(it2.hasNext()){
		Document d = (Document)it2.next();
		efile.write("<A HREF='file://" + docDir + "/" + d.mFile.getName() + "' target='showframe'>"+d.mFile.getName()+" </A>,\n");
	    }
	    efile.write("</body></html>");	    
	    efile.close();

	}
    }
}