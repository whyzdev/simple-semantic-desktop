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
import com.aliasi.util.ScoredObject;


import com.aliasi.tokenizer.EnglishStopListFilterTokenizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseFilterTokenizer;
import com.aliasi.tokenizer.PorterStemmerFilterTokenizer;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.lm.TokenizedLM;

import java.io.*;
import java.util.*;

import java.io.File;
import java.util.*;
import com.aliasi.util.Files;

public class Document {

    HashMap<Integer,Entity> entities;
    LinkedList<Sentence> sentences;
    String polarity;
    String subjectivity;
    String annotatedHTML;
    String infoHTML;
    String name;
    ScoredObject[] significantPhrasesVsCluster;
    ScoredObject[] collocations;
    TokenizedLM foregroundModel;

    final File mFile;
    final char[] mText; // don't really need to store
    final String mText_str; // don't really need to store
    final ObjectToCounterMap<String> mTokenCounter
	= new ObjectToCounterMap<String>();
    final double mLength;
    Document(File file) throws IOException {
	mFile = file; // includes name
	name = mFile.toString();
	mText = Files.readCharsFromFile(file,Strings.UTF8);
	Tokenizer tokenizer = createTokenizer(mText);
	String token;
	while ((token = tokenizer.nextToken()) != null)
	    mTokenCounter.increment(token.toLowerCase());
	mLength = length(mTokenCounter);
	mText_str = Files.readFromFile(file,Strings.UTF8);
	entities = new HashMap<Integer,Entity>();
	sentences = new LinkedList<Sentence>();
	annotatedHTML="";
    }
    double cosine(Document thatDoc) {
	return product(thatDoc) / (mLength * thatDoc.mLength);
    }
    double product(Document thatDoc) {
	double sum = 0.0;
	for (String token : mTokenCounter.keySet()) {
	    int count = thatDoc.mTokenCounter.getCount(token);
	    if (count == 0) continue;
	    // tf = sqrt(count); sum += tf1 * tf2
	    sum += Math.sqrt(count * mTokenCounter.getCount(token));
	}
	return sum;
    }
    public String toString() {
	return mFile.getParentFile().getName() + "/"  + mFile.getName();
    }
    static double length(ObjectToCounterMap<String> otc) {
	double sum = 0.0;
	for (Counter counter : otc.values()) {
	    double count = counter.doubleValue();
	    sum += count;  // tf =sqrt(count); sum += tf * tf
	}
	return Math.sqrt(sum);
    }
    static Tokenizer createTokenizer(char[] cs) {
	Tokenizer tokenizer
	    = IndoEuropeanTokenizerFactory
	    .FACTORY.tokenizer(cs,0,cs.length);
	// tokenizer = new LowerCaseFilterTokenizer(tokenizer);
	// tokenizer = new EnglishStopListFilterTokenizer(tokenizer);
	// tokenizer = new PorterStemmerFilterTokenizer(tokenizer);
	return tokenizer;
    }


    public void printEntities(){
	System.out.println("----------------------\nEntities of "+ mFile);
	for(int i=0;i<entities.size();i++){
	    if (entities.get(i)!=null)
		System.out.println("" + i + ": " + entities.get(i));
	}
	
    }

    public void underlineBigScorers(){

	/*	for (int i=0;i<significantPhrases.length;i++){
	    String[] terms = (String[])significantPhrases[i].getObject();
	    for(int j=0;j<terms.length;j++){
		this.annotatedHTML.replaceAll(terms[j].replaceAll("[\\W]",""), "<b><u>" +terms[j].replaceAll("[\\W]","")+ "</u></b>");
	    }
	    }*/

	for (int i=0;i<significantPhrasesVsCluster.length;i++){
	    String[] terms = (String[])significantPhrasesVsCluster[i].getObject();
	    for(int j=0;j<terms.length;j++){
		this.annotatedHTML.replaceAll(terms[j].replaceAll("[\\W]",""), "<b><u>" +terms[j].replaceAll("[\\W]","")+ "</u></b>");
	    }
	}

    }
    
    public void addSignificanceSizeCSS(){	
	double scoreavg=0;
	double max_score= sentences.get(0).score;
	double min_score= sentences.get(0).score;
	int min_score_index=0;
	int max_score_index=0;

	for(int i=0;i<sentences.size();i++){
	    Sentence s= (Sentence)sentences.get(i);
	    scoreavg+=s.score;
	    if (s.score > max_score){
		max_score = s.score;
		max_score_index = i;		
	    }

	    if (s.score < min_score){
		min_score = s.score;
		min_score_index = i;		
	    }
	}

	scoreavg = scoreavg / (sentences.size()+1);
	double css_font_px_increment_size=(max_score-min_score)/20;
	
	String styleinfo = "<style>";
	for(int i=0;i<sentences.size();i++){
	    Sentence s= (Sentence)sentences.get(i);
	    styleinfo  += "#sentence" + i + " {font-size:" +  (9+ (s.score*10)) + "pt;}\n";
	}	
	styleinfo  += "</style>";
	this.annotatedHTML = styleinfo +  this.annotatedHTML ;
    }

    public void calculateSentenceScore(Sentence s){
	IndoEuropeanTokenizerFactory tf = new IndoEuropeanTokenizerFactory() ;
	String[] tokens = tf.tokenizer(s.doc.mText,s.start,s.end-s.start).tokenize();
	for (int i=0;i<tokens.length;i++){
	    String[] tokarray = {tokens[i]};
	    //	    s.score+=1-forwardmodel.tokenProbability(tokarray, 0, 1);
	    s.score += 1;
	}
	s.score = s.score /tokens.length;
    }

    public void createInfoHTML(String entitiesPath, String clustername){	
	infoHTML = "<html><body><p>" ;
	infoHTML +="<br> <b>Length</b>:" + mLength + " words</b>";
	infoHTML +="<br> <b>Subjectivity</b>:" + subjectivity;
	infoHTML +="<br> <b>Polarity</b>:" + polarity;
	infoHTML += "<br><b>Entities:</b><br>";
	Iterator it = entities.values().iterator();
	while(it.hasNext()){
	    Entity e = (Entity) it.next();
	    infoHTML += " <a href='file://"+ entitiesPath + "/" + clustername + "/" + e.id + ".html'>" + e.occurences.get(0)+"</a>";
	}
	infoHTML += "</p></body></html>" ;

    }

    static class Sentence{
	static boolean DIAG1 =false;
	int start, end;
	double score;
	Document doc;
	int id;
	public Sentence(int start, int end, int id, Document doc){
	    if (DIAG1)
		System.err.println(start + "," + end + "," + doc.mText_str.substring(start,end-start));
	    this.start=start;
	    this.end=end;
	    this.doc=doc;
	    this.id=id;
	    score=0;
	}
	public String toString(){
	    return doc.mText_str.substring(start,end);
	}
	

    }

    


}
