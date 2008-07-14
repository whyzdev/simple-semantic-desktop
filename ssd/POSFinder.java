package ssd;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.util.Files;
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
import java.io.File;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Scanner;
import java.util.HashMap;


public class POSFinder {

	static TokenizerFactory TOKENIZER_FACTORY 
	= new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");

	Set<Document> docSet;

	public static void main(String[] args) 
	throws ClassNotFoundException, IOException {

	}
	public POSFinder(Set<Document> docSet) {
		this.docSet=docSet;		
	}

	public POSFinder(Set<Document> docSet, TextProcessor tp) {
		this.docSet=docSet;
		this.tp=tp;
		posDir=new File(tp.outDir + "/pos");
	}

	TextProcessor tp;
	File posDir; 
	public void process() throws ClassNotFoundException, IOException {
		if (posDir != null && posDir.exists() && posDir.list().length > 0){
			for (File posfile : posDir.listFiles()){
				String p =posfile.getName();
				if (p.indexOf(".__.") != -1){				
				String posname = java.net.URLDecoder.decode(p.substring(p.indexOf(".__.")+4,p.length()));
				String docname = p.substring(0,p.indexOf(".__."));
				tp.getFileFromDocSet(docSet, docname).mPosTexts = new HashMap<String,String>();
				tp.getFileFromDocSet(docSet, docname).mPosTexts.put(posname, Files.readFromFile(posfile));
				
				}
			}
		}
		else{
			if (posDir != null && !posDir.exists()){
				posDir.mkdir();
			}

			System.out.println("Reading POS tagging HMM from file=pos-en-general-brown.HiddenMarkovModel");
			FileInputStream fileIn = new FileInputStream("../lib/lingpipe/demos/models/pos-en-general-brown.HiddenMarkovModel");
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
			Streams.closeInputStream(objIn);
			HmmDecoder decoder = new HmmDecoder(hmm);

			Iterator<Document> it=docSet.iterator();
			while(it.hasNext()){
				Document d = it.next();
				Scanner sc = new Scanner(d.mText_str);
				while (sc.hasNextLine()) {        	        	
					String line = sc.nextLine(); 
					if (line == null || line.length() < 1 
							|| line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
						break;
					char[] cs = line.toCharArray();

					Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length);
					String[] tokens = tokenizer.tokenize();
					String[] tags = decoder.firstBest(tokens);				
					for (int i = 0; i < tokens.length; ++i){
						addToken(tags[i], tokens[i], d);        	
						addToken("all", tokens[i], d);
					}
				}
				if(d.mPosTexts.get("nil") != null){
					d.mPosTexts.remove("nil");
					//System.err.println("Removed nil set");
				}

				System.out.println("Document " + d + "'s POS wordbags:");
				Iterator<String> it2=d.mPosTexts.keySet().iterator();
				while(it2.hasNext()){
					String tag=it2.next();
					String tokens = d.mPosTexts.get((Object)tag);
					System.out.println("mPosTexts["+tag+"]:-------------");
					System.out.println(tokens);
					if (posDir !=null && !tag.equals("*"))
						com.aliasi.util.Files.writeStringToFile(tokens,new File(posDir + "/" + d.mFile.getName() + ".__." + java.net.URLEncoder.encode(tag)));
				}

			}
		}
	}

	private void addToken(String tag, String token, Document d){
		if (d.mPosTexts.get((Object)tag) == null){
			d.mPosTexts.put(tag, token);
		}
		else {
			String tokenlist = d.mPosTexts.get((Object)tag);
			tokenlist += " " + token;
			d.mPosTexts.put(tag, tokenlist);
		}		
	}
}