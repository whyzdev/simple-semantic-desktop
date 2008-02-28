
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;

import com.aliasi.coref.EnglishMentionFactory;
import com.aliasi.coref.MentionFactory;
import com.aliasi.coref.Mention;
import com.aliasi.coref.WithinDocCoref;

import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.SimpleElementHandler;

import com.aliasi.util.FastCache;
import com.aliasi.util.Reflection;
import com.aliasi.util.Streams;
import com.aliasi.util.ScoredObject;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CorefDemo extends AbstractSentenceDemo {

    static Pattern MALE_PRONOUNS 
	= Pattern.compile("\\b(He|he|Him|him|His|his)\\b");
    static Pattern FEMALE_PRONOUNS
	= Pattern.compile("\\b(She|she|Her|her|Hers|hers)\\b");

    private Chunker mEntityChunker;

    public CorefDemo(String tokenizerFactoryClassName,
		     String sentenceModelClassName,
		     String chunkerResourceName,
		     String genre) {
	super(tokenizerFactoryClassName,sentenceModelClassName,
	      "Coreference Demo",
	      "Coreference Demo for " + genre);
	mEntityChunker 
	    = (Chunker) readResource(chunkerResourceName);
    }

    /**
     * Extract sentences from the specified character slice,
     * wrapping them in XML sentence elements and deferring
     * their text to <code>processSentence</code> for further
     * processing.
     *
     * @param cs Underlying characters.
     * @param start Index of the first character of slice.
     * @param end Index of one past the last character of the slice.
     * @param writer SAXWriter to which output is written.
     * @param properties Properties for the processing.
     * @throws SAXException If there is an error during processing.
     */
    public void process(char[] cs, int start, int end,
			SAXWriter writer,
			Properties properties) 
	throws SAXException {

	MentionFactory mf = new EnglishMentionFactory();
	WithinDocCoref coref = new WithinDocCoref(mf);
	String text = new String(cs,start,end-start);

	Chunking sentenceChunking
	    = mSentenceChunker.chunk(cs,start,end);
	Iterator sentenceIt
	    = sentenceChunking.chunkSet().iterator();
	int pos = 0;
	for (int i = 0; sentenceIt.hasNext(); ++i) {
	    Chunk sentenceChunk = (Chunk) sentenceIt.next();
	    int sentStart = sentenceChunk.start();
	    int sentEnd = sentenceChunk.end();
	    String sentenceText = text.substring(sentStart,sentEnd);

	    writer.characters(text.substring(pos,sentStart));
	    writer.startSimpleElement("s","i",Integer.toString(i));
	    processSentence(sentenceText,writer,properties,i,mf,coref);
	    writer.endSimpleElement("s");
	    pos = sentEnd;
	}
	writer.characters(text.substring(pos));
    }	    

    public void processSentence(String sentenceText, SAXWriter writer,
				Properties properties,
				int sentId) {
	throw new IllegalStateException("not used");
    }


    public void processSentence(String sentenceText, SAXWriter writer,
				Properties properties,
				int sentId, MentionFactory mf,
				WithinDocCoref coref) 
	throws SAXException {

	Chunking mentionChunking
	    = mEntityChunker.chunk(sentenceText);

	Set chunkSet = new TreeSet(Chunk.TEXT_ORDER_COMPARATOR);
	chunkSet.addAll(mentionChunking.chunkSet());


	addPronouns(MALE_PRONOUNS,"MALE_PRONOUN",sentenceText,chunkSet);
	addPronouns(FEMALE_PRONOUNS,"FEMALE_PRONOUN",sentenceText,chunkSet);

		
	Iterator it = chunkSet.iterator();
	String text = mentionChunking.charSequence().toString();
	int pos = 0;
	while (it.hasNext()) {
	    Chunk neChunk = (Chunk) it.next();
	    int start = neChunk.start();
	    int end = neChunk.end();
	    String type = neChunk.type();
	    String chunkText = text.substring(start,end);
	    Mention mention = mf.create(chunkText,type);
	    int mentionId = coref.resolveMention(mention,sentId);

	    String whitespace = text.substring(pos,start);
	    writer.characters(whitespace);
	    writer.startSimpleElement("ENAMEX",
				      "TYPE",type,
				      "ID",Integer.toString(mentionId));
	    writer.characters(chunkText);
	    writer.endSimpleElement("ENAMEX");
	    pos = end;
	}
	String whitespace = text.substring(pos);
	writer.characters(whitespace);
    }

    void addPronouns(Pattern pattern, String tag, String sentenceText, Set chunkSet) {
	java.util.regex.Matcher matcher = pattern.matcher(sentenceText);
	int pos = 0;
	while (matcher.find(pos)) {
	    Chunk proChunk = ChunkFactory.createChunk(matcher.start(),
						      matcher.end(),
						      tag);
	    // incredibly inefficient quadratic algorithm here, but bounded by sentence
	    Iterator it = chunkSet.iterator();
	    while (it.hasNext()) {
		Chunk chunk = (Chunk) it.next();
		if (overlap(chunk.start(),chunk.end(),
			    proChunk.start(),proChunk.end()))
		    it.remove();
	    }
	    chunkSet.add(proChunk);
	    pos = matcher.end();
	}
    }

    static boolean overlap(int start1, int end1,
			   int start2, int end2) {
	return java.lang.Math.max(start1,start2)
	    < java.lang.Math.min(end1,end2);
    }

}





import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;


import java.io.*;

import java.util.Iterator;

public class NEDemo {


    static final int MAX_N_BEST_CHUNKS = 8;

    public static void main(String[] args) throws Exception {
	InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);
	String modelFilename= "/local/s/lingpipe/demos/models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
	File modelFile = new File(modelFilename);

	System.out.println("Reading chunker from file=" + modelFile);
	ConfidenceChunker chunker 
	    = (ConfidenceChunker) AbstractExternalizable.readObject(modelFile);
	String line="";
        while (bufReader.ready()) {
            line +=  bufReader.readLine() + " " ;         
	}
	String temp=line;
	temp=temp.replaceAll("<!--.*-->", " ");    
	temp=temp.replaceAll("<style.*/style>"," ");
	temp=temp.replaceAll("<STYLE.*/STYLE>"," ");
	temp=temp.replaceAll("<[a-zA-Z/!][^>]*>"," ");
	line=temp;
	int currentPeriodLocation=0;

	while (line.indexOf(".",currentPeriodLocation)!=-1){
	    temp=line.substring(currentPeriodLocation,line.indexOf(".",currentPeriodLocation));
	    currentPeriodLocation=line.indexOf(".",currentPeriodLocation)+1;
	    char[] cs = temp.toCharArray();    
	    Iterator it = chunker.nBestChunks(cs,0,cs.length,MAX_N_BEST_CHUNKS);
	    System.out.println(temp);
	    System.out.println("Rank          Conf      Span    Type     Phrase");
	    for (int n = 0; it.hasNext(); ++n) {
		Chunk chunk = (Chunk) it.next();
		double conf = Math.pow(2.0,chunk.score());
		int start = chunk.start();
		int end = chunk.end();
		String phrase = temp.substring(start,end);
		System.out.println(n + " "
				   + Strings.decimalFormat(conf,"0.0000",12)
				   + "       (" + start
				   + ", " + end
				   + ")       " + chunk.type()
				   + "         " + phrase);
	    }
        }

    }
}
