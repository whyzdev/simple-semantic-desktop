import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.hmm.TagWordLattice;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;

import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Arrays;
import java.util.Iterator;

public class POSDemo {

    static TokenizerFactory TOKENIZER_FACTORY 
        = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");

    public static void main(String[] args) 
        throws ClassNotFoundException, IOException {
	String modelFilename= "/local/s/lingpipe/demos/models/pos-en-general-brown.HiddenMarkovModel";
	if (args.length>0)
	    modelFilename = args[0];
	
        System.out.println("Reading model from file=" + modelFilename);
        FileInputStream fileIn = new FileInputStream(modelFilename);
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
        Streams.closeInputStream(objIn);
        HmmDecoder decoder = new HmmDecoder(hmm);

        InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);
        while (true) {
            System.out.print("\n\nINPUT> ");
            System.out.flush();
            String line = bufReader.readLine();
            if (line == null || line.length() < 1 
                || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
                break;
            char[] cs = line.toCharArray();

            Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length);
            String[] tokens = tokenizer.tokenize();

            firstBest(tokens,decoder);

            nBest(tokens,decoder);

            confidence(tokens,decoder);
        }
        Streams.closeReader(bufReader);
    }


    static void firstBest(String[] tokens, HmmDecoder decoder) {
        String[] tags = decoder.firstBest(tokens);
        System.out.println("\nFIRST BEST");
        for (int i = 0; i < tokens.length; ++i)
            System.out.print(tokens[i] + "_" + tags[i] + " ");
        System.out.println();
    }

    static final int MAX_N_BEST = 5;

    static void nBest(String[] tokens, HmmDecoder decoder) {
        System.out.println("\nN BEST");
        System.out.println("#   JointLogProb         Analysis");
        Iterator nBestIt = decoder.nBest(tokens);
        for (int n = 0; n < MAX_N_BEST && nBestIt.hasNext(); ++n) {
            ScoredObject tagScores = (ScoredObject) nBestIt.next();
            double score = tagScores.score();
            String[] tags = (String[]) tagScores.getObject();
            System.out.print(n + "   " + format(score) + "  ");
            for (int i = 0; i < tokens.length; ++i)
                System.out.print(tokens[i] + "_" + pad(tags[i],5));
            System.out.println();
        }        
    }

    static void confidence(String[] tokens, HmmDecoder decoder) {
        System.out.println("\nCONFIDENCE");
	System.out.println("#   Token          (Prob:Tag)*");
        TagWordLattice lattice = decoder.lattice(tokens);
        for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex) {
            ScoredObject[] tagScores = lattice.log2ConditionalTags(tokenIndex);
	    System.out.print(pad(Integer.toString(tokenIndex),4));
	    System.out.print(pad(tokens[tokenIndex],15));
            for (int i = 0; i < 5; ++i) {
		double logProb = tagScores[i].score();
		double conditionalProb = Math.pow(2.0,logProb);
		String tag = (String) tagScores[i].getObject();
                System.out.print(" " + format(conditionalProb) 
				 + ":" + pad(tag,4));
	    }
	    System.out.println();
	}
    }

    static String format(double x) {
	return Strings.decimalFormat(x,"#,##0.000",9);
    }

    static String pad(String in, int length) {
	if (in.length() > length) return in.substring(0,length-3) + "...";
	if (in.length() == length) return in;
	StringBuffer sb = new StringBuffer(length);
	sb.append(in);
	while (sb.length() < length) sb.append(' ');
	return sb.toString();
	
    }
}
