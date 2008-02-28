package ssd;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

import java.util.*;

public class SignificanceMeasurizer {

    private static int NGRAM = 4;
    private static int MIN_COUNT = 10;
    private static int MAX_NGRAM_REPORTING_LENGTH = 3;
    private static int NGRAM_REPORTING_LENGTH = 3;
    private static int MAX_COUNT = 1000;

    /*    private static File BACKGROUND_DIR 
	= new File("../../data/rec.sport.hockey/train");
    private static File FOREGROUND_DIR 
    = new File("../../data/rec.sport.hockey/test");*/


    public static void process(DocumentCluster cluster) throws IOException {
	IndoEuropeanTokenizerFactory tokenizerFactory 
	    = new IndoEuropeanTokenizerFactory();

	System.out.println("Training background model");
	TokenizedLM backgroundModel = buildModel(tokenizerFactory,
						 NGRAM,
						 cluster);
	
	backgroundModel.sequenceCounter().prune(3);

	System.out.println("\nAssembling collocations in Training");
	ScoredObject[] coll 
	    = backgroundModel.collocations(NGRAM_REPORTING_LENGTH,
					   MIN_COUNT,MAX_COUNT);

	System.out.println("\nCollocations in Order of Significance:");
	report(coll);
	cluster.collocations = coll;
	cluster.backgroundModel=backgroundModel;

	Iterator it = cluster.docs.iterator();
	while (it.hasNext()){
	    Document currentdoc=(Document)it.next();
	    System.out.println("Training foreground model for " + currentdoc);
	    TokenizedLM foregroundModel = buildModel(tokenizerFactory,
						     NGRAM,
						     currentdoc);
	    foregroundModel.sequenceCounter().prune(3);
	    
	    System.out.println("\nAssembling New Terms in Test vs. Training");
	    ScoredObject[] newTerms 
		= foregroundModel.newTerms(NGRAM_REPORTING_LENGTH,
					   MIN_COUNT,
					   MAX_COUNT,
				       backgroundModel);
	    
	    System.out.println("\nNew Terms in Order of Signficance:");
	    report(newTerms);
	    currentdoc.significantPhrasesVsCluster = newTerms;
	    currentdoc.collocations 
		= foregroundModel.collocations(NGRAM_REPORTING_LENGTH,
					       MIN_COUNT,MAX_COUNT);
	    report(currentdoc.collocations);
	    currentdoc.foregroundModel=foregroundModel;
	}

	System.out.println("\nDone.");
    } 

    private static TokenizedLM buildModel(TokenizerFactory tokenizerFactory,
					  int ngram,
					  DocumentCluster cluster) 
	throws IOException {

	Set<Document> trainingFiles = cluster.docs;
	TokenizedLM model = 
	    new TokenizedLM(tokenizerFactory,
			    ngram);
	System.out.println("Training on "+cluster.name);
	Iterator it = trainingFiles.iterator();
	for (int j = 0; it.hasNext(); ++j) {
	    Document currentdoc = (Document) it.next();
	    String text = currentdoc.mText_str;
	    model.train(text);
	}
	return model;
    }


    private static TokenizedLM buildModel(TokenizerFactory tokenizerFactory,
					  int ngram,
					  Document doc) 
	throws IOException {
	TokenizedLM model = 
	    new TokenizedLM(tokenizerFactory,
			    ngram);
	System.out.println("Training on "+doc.name);
		    
	String text = doc.mText_str;
	model.train(text);
	
	return model;
    }

    private static void report (ScoredObject[] nGrams) {
	for (int i=0; i<nGrams.length; ++i){
	    double score = nGrams[i].score();
	    String[] toks = (String[]) nGrams[i].getObject();
	    report_filter(score,toks);
	}
    }
    
    private static void report_filter(double score, String[] toks) {
	String accum = "";
	for (int j=0; j<toks.length; ++j) {
	    if (nonCapWord(toks[j])) return;
	    accum += " "+toks[j];
	}
	System.out.println("Score: "+score+" with :"+accum);
    }

    private static boolean nonCapWord(String tok) {
	if (!Character.isUpperCase(tok.charAt(0)))
	    return true;
	for (int i = 1; i < tok.length(); ++i) 
	    if (!Character.isLowerCase(tok.charAt(i))) 
		return true;
	return false;
    }

}
