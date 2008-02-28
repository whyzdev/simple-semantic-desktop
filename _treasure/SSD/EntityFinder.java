import com.aliasi.util.AbstractExternalizable;

import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.coref.EnglishMentionFactory;
import com.aliasi.coref.MentionFactory;
import com.aliasi.coref.Mention;
import com.aliasi.coref.WithinDocCoref;
import com.aliasi.sentences.SentenceChunker;


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
import java.io.File;

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

public class EntityFinder {

    final static String MODEL_LOCATION = "/local/s/lingpipe/demos/models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
    final static boolean DIAG2=true;

    static Pattern MALE_PRONOUNS 
	= Pattern.compile("\\b(He|he|Him|him|His|his)\\b");
    static Pattern FEMALE_PRONOUNS
	= Pattern.compile("\\b(She|she|Her|her|Hers|hers)\\b");

    private Chunker mEntityChunker;

    protected final com.aliasi.tokenizer.TokenizerFactory mTokenizerFactory;
    protected final com.aliasi.sentences.SentenceModel mSentenceModel;
    protected final com.aliasi.sentences.SentenceChunker mSentenceChunker;



    public EntityFinder() throws Exception{
        mTokenizerFactory 
            =  new com.aliasi.tokenizer.IndoEuropeanTokenizerFactory();
        mSentenceModel
            = new com.aliasi.sentences.MedlineSentenceModel();
        mSentenceChunker
            = new com.aliasi.sentences.SentenceChunker(mTokenizerFactory,mSentenceModel);
	mEntityChunker 
	    = (Chunker) AbstractExternalizable.readObject(new File(MODEL_LOCATION));
    }

    public void processCluster(DocumentCluster cluster){
	MentionFactory mf = new EnglishMentionFactory();
	WithinDocCoref coref = new WithinDocCoref(mf);
	Iterator it2 = cluster.docs.iterator();
	for(int j=0;it2.hasNext();j++){
	    Document doc = (Document) it2.next();
	    if (DIAG2)
		System.out.println("Recognizing Entities in Document " + doc);
	    getDocumentEntities(doc, cluster,mf,coref);
	    doc.printEntities();
	}
	return;
    }


    public void getDocumentEntities(Document doc, DocumentCluster cluster, MentionFactory mf, WithinDocCoref coref){
	char[] cs = doc.mText;
	int start=0;
	int end=cs.length-1;
	String text = doc.mText_str;

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
	    doc.annotatedHTML+=text.substring(pos,sentStart);
	    //writer.characters(text.substring(pos,sentStart));
	    //writer.startSimpleElement("s","i",Integer.toString(i));
	    getSentenceEntities(sentenceText,i,mf,coref,doc,cluster,sentStart);
	    
	    //writer.endSimpleElement("s");
	    doc.sentences.add(new Document.Sentence(start,end,i,doc));
	    pos = sentEnd;
	}
	//writer.characters(text.substring(pos));
    }	    


    public void getSentenceEntities(String sentenceText, 
				    int sentId, MentionFactory mf,
				    WithinDocCoref coref, Document doc, DocumentCluster cluster, int sent_start) {
	
	doc.annotatedHTML +=" <p id='sentence" + doc.sentences.size() + "'>";
	Chunking mentionChunking
	    = mEntityChunker.chunk(sentenceText);

	Set chunkSet = new TreeSet(Chunk.TEXT_ORDER_COMPARATOR);
	chunkSet.addAll(mentionChunking.chunkSet());


	addPronouns(MALE_PRONOUNS,"MALE_PRONOUN",sentenceText,chunkSet);
	addPronouns(FEMALE_PRONOUNS,"FEMALE_PRONOUN",sentenceText,chunkSet);

		
	Iterator it = chunkSet.iterator();
	String text = mentionChunking.charSequence().toString();
	int pos = 0;
	    int end=0;
	while (it.hasNext()) {
	    Chunk neChunk = (Chunk) it.next();
	    int start = neChunk.start();
	    end = neChunk.end();
	    String type = neChunk.type();
	    String chunkText = text.substring(start,end);
	    Mention mention = mf.create(chunkText,type);
	    int mentionId = coref.resolveMention(mention,sentId);

	    //	    int a=0;
	    //	    arrlist.add(new Integer(a)); // Why did we add this line?

	    if (!cluster.entities.containsKey(new Integer(mentionId))){
		cluster.entities.put(new Integer(mentionId),new Entity(mentionId,type,cluster));
	    }	   
	    cluster.entities.get(mentionId).addOccurence(sent_start+start,sent_start+end,doc,chunkText);

	    if (!doc.entities.containsKey(new Integer(mentionId))){		
		doc.entities.put(new Integer(mentionId),cluster.entities.get(mentionId));
	    }

	    doc.annotatedHTML +=text.substring(pos,start);
	    doc.annotatedHTML +="<a href='../entities/"+ cluster.name + "/" + mentionId + ".html' target='infoframe'>" + chunkText + "</a>";

	    //String whitespace = text.substring(pos,start);
	    //	    writer.characters(whitespace);
	    //	    writer.startSimpleElement("ENAMEX",
	    //				      "TYPE",type,
	    //				      "ID",Integer.toString(mentionId));
	    //	    writer.characters(chunkText);
	    //	    writer.endSimpleElement("ENAMEX");
	    pos = end;
	}
	doc.annotatedHTML +=text.substring(end);
	doc.annotatedHTML +=" </p>";
	//	String whitespace = text.substring(pos);
	//	writer.characters(whitespace);
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


