package ssd;
 

/*

Implementation notes on Frequency analysis for keyword extraction:
data items:
Words
word frequencies
word co occurrences
word occurrence adjacencies in text

chi squared measure data
clustering data



*/

import java.util.*;
import java.io.*;


public class ChiSqDocument {
    //parameters to the program
    private static boolean USE_STOP_WORDS = true;
    private static boolean CLEAN_PUNCTUATION = true;
    private static boolean NORMALIZE_CASE = true;
    private static double FREQUENT_SET_SIZE=0.1;



    public ArrayList<Sentence> sentences = new ArrayList<Sentence>();
    public ArrayList<Word> words = new ArrayList<Word>();
    public ArrayList<Occurrence> occurrences = new ArrayList<Occurrence>();
    public ArrayList<String> tokens = new ArrayList<String>();//this array might turn out to be redundant

    public TreeSet<String> stopwords = new TreeSet<String>();

    public Word[] chiSquareTable;
    public Word[] frequencyTable;

    Comparator<Word> byFreq = new WordFreqComparator();
    Comparator<Word> byChiSquare = new ChiSquareComparator();

    public String text;    
    public double chiSquareSum=0;
    
    public ChiSqDocument(File textfile) throws Exception{

	//Read the supplied file into a variable (inefficient)
	String textcache="";
	BufferedReader reader = new BufferedReader(new FileReader(textfile));
	while (reader.ready())
	    textcache+=reader.readLine();

	//Clean punctuation
	if (CLEAN_PUNCTUATION){
	    textcache=textcache.replaceAll("[\\p{Punct}&&[^\\.]]+","");
	}
	//Make everything lowercase
	if (NORMALIZE_CASE)
	    textcache=textcache.toLowerCase();


	//Read Stop Words from english.stop (words like a, an, the)
	if (USE_STOP_WORDS){
	    Scanner s1 = new Scanner(new File("./english.stop"));
	    while(s1.hasNext())
		stopwords.add(s1.next().toLowerCase());
	}	


	//Tokenize the text
	StringTokenizer st = new StringTokenizer(textcache);	


	//Variables to keep track of the parsing
	this.text = "";
	String sentenceSoFar="";
	ArrayList<Occurrence> sentenceWordsSoFar = new ArrayList<Occurrence>();
	int sentenceBeginIndex=0;
	Sentence currentSentence = new Sentence(this);
	int wordBeginIndex=0;

	while(st.hasMoreTokens()){
	    //Read a token
	    String nextToken=st.nextToken();
	    // If its not a Stop word,
	    if(USE_STOP_WORDS && !stopwords.contains(nextToken)){
		//add token to tokens array
		tokens.add(nextToken);
	        
		//add token to the current sentence and the text
		sentenceSoFar += " " + nextToken;
		text += " " + nextToken;

		//call countWord to increment the counted frequency of this word
		sentenceWordsSoFar.add(countWord(currentSentence,nextToken, wordBeginIndex,text.length()-1,tokens.size()-1));
	    
		//If the Token ends with a "." or is a "."
		if (nextToken.equals(".") || 
		    (nextToken.length()>=1 
		     && nextToken.substring(nextToken.length()-1,nextToken.length()).equals("."))){

		    //Trim the "."
		    nextToken = nextToken.substring(0,nextToken.length()-1);		

		    //The sentence is now complete. Fill it up with Words.
		    currentSentence.setParams(sentenceSoFar,sentenceWordsSoFar,sentenceBeginIndex,text.length());

		    //Loop over all the unique Words that appear in the sentence
		    Iterator j=currentSentence.words.iterator();
		    while(j.hasNext()){			
			Word wordInSentence=((Word)j.next());
			//For each word in the sentence, loop again over all 
			//words in the sentence 
			Iterator k=currentSentence.words.iterator();
			while(k.hasNext()){
			    Word otherWordInSentence=((Word)k.next());
			    if (otherWordInSentence != wordInSentence){

				//update Co-Occurence counts
				Integer coOccurrence=new Integer(0);
				if (wordInSentence.coOccurrences.containsKey(otherWordInSentence))
				    coOccurrence=((Integer)wordInSentence.coOccurrences.get(otherWordInSentence));
				coOccurrence = new Integer(coOccurrence.intValue() + 1 );
				wordInSentence.coOccurrences.put(otherWordInSentence,coOccurrence);			    

			    }
			}
		    }

		    //add this sentence to the sentences list
		    sentences.add(currentSentence);	    

		    //blank out all counter variables
		    sentenceBeginIndex=text.length()+1;
		    currentSentence=new Sentence(this);
		    sentenceSoFar="";
		}
	    }
	}
	this.text=textcache;
			
	//Create a table sorted by Frequency of Words
	frequencyTable = new Word[words.size()];	
	for (int i=0;i<frequencyTable.length;i++)
	    frequencyTable[i]=words.get(i);
	Arrays.sort(frequencyTable,byFreq);


	//Create a subset G with the most frequent Words
	Word[] temp = new Word[((int)((double)frequencyTable.length*FREQUENT_SET_SIZE))]; 
	for (int i=0;i<temp.length;i++){
	    temp[i]=frequencyTable[i];
	}
	frequencyTable=temp;
	

	//Calculate Chi-squared measure for set G of most frequent words
	for (int i=0;i<frequencyTable.length;i++){
	    Word nextword = frequencyTable[i];
	    nextword.probability_g=((double)nextword.getFrequency())/((double)getWordCount());
	    nextword.calculateChiSquare();
	}
	
	//Create a table of frequent Words sorted by Chi-square 
	chiSquareTable = new Word[frequencyTable.length];
	for (int i=0;i<chiSquareTable.length;i++){
	    chiSquareTable[i]=frequencyTable[i];
	    chiSquareSum+=chiSquareTable[i].chiSquare;
	}
	Arrays.sort(chiSquareTable,byChiSquare);
	
	reader.close();
    }

    public String toString(){
	String returnstring="\n\n--\n";
 	//for (int i=0;i<frequencyTable.length;i++)
	//returnstring += ((Word)frequencyTable[i]).text + "==" + ((Word)frequencyTable[i]).getFrequency() + "\n";
	
	for (int i=0;i<chiSquareTable.length;i++)
	    returnstring += ((Word)chiSquareTable[i]).text + "==" + ((Word)chiSquareTable[i]).getChiSquare() + "\n";
	
	return returnstring;
    }

    public String printToHTML(){

	String returnstring="<html><body>";
	Iterator i = occurrences.iterator();
	while(i.hasNext()){
	    Word cword=((Occurrence)i.next()).word;
	    int color3=((int)(255.0*cword.getFrequency()/frequencyTable[0].getFrequency()));
	    int color2=((int)(cword.chiSquare*255.0/chiSquareTable[0].getChiSquare()));
	    int color1=0;
	    
	    returnstring += "<font color=" + String.format("\"#%2x%2x%2x\"", color1,color2,color3).replaceAll(" ","0") + ">" + cword.text + "</font> ";
	}

	 returnstring+="</body></html>";
	 return returnstring;
    }

    public void printResults(){
	printFrequencies();
	printCoOccurrences();
	printChiSquares();
	printChiSquaresAndFrequencies();
    }

    public void printChiSquares(){
	String returnstring="\n\n--\nChi-Squares:\n-\n";
	for (int i=0;i<chiSquareTable.length;i++){
	    returnstring += ((Word)chiSquareTable[i]).text + ":" + ((Word)chiSquareTable[i]).getChiSquare() + ", ";
	    if (i%3==0)
		returnstring += "\n";
	}
	System.out.println(returnstring);
    }


    public void printChiSquaresAndFrequencies(){
	String returnstring="\n\n--\nFinal Results :\n----------------------------------------\nTerm\tChi^2\tFrequency----------------------------\n";

	for (int i=0;i<chiSquareTable.length;i++){
	    Word curWord=((Word)chiSquareTable[i]);
	    returnstring += curWord.text + "\t" + curWord.getChiSquare() + "\t" + curWord.getFrequency()+ "\n ";
	    if (curWord.getChiSquare() == 0)
		i=chiSquareTable.length;
	}
	System.out.println(returnstring);
    }


    public void printCoOccurrences(){
	String returnstring="\n\n--\nCo-Occurences:\n-\n |";
	char c='a';
	for (int i=0;i<frequencyTable.length && i < 26;i++)
	    returnstring += (c++) + "  |";
	returnstring +="\n";
	c='a';
	for (int i=0;i<frequencyTable.length && i < 26;i++){
	    returnstring += (c++) + "|";
	    for (int j=0;j<frequencyTable.length && j < 26;j++)
		returnstring += frequencyTable[i].getCoOccurrence(frequencyTable[j]) + "|";
	    returnstring += "\n";
	}	       	
	c='a';
	for (int i=0;i<frequencyTable.length && i < 26;i++)
	    returnstring += (c++) + ":" + frequencyTable[i].text + "\n";
	System.out.println(returnstring);
    }

    public void printFrequencies(){
	System.out.println("Frequencies:\n-\n");


	for(int i=0;i<frequencyTable.length;i++){
	    Word nextword=frequencyTable[i];
	    System.out.print(nextword.text + ":" + nextword.occurrences.size() + ", ");
	    if (i%4==0)
		System.out.print("\n");		
	}
    }
	
		

    private Occurrence countWord(Sentence sentence, String wordtext, int beginIndex, int endIndex, int tokenIndex){
	boolean foundWord=false;
	Occurrence newocc= new Occurrence();


	//if words array already has this word
	//increment its frequency (point to occurrence)
	Iterator i = words.iterator();
	while(i.hasNext()){
	    Word nextword = ((Word)i.next());	    
	    if(nextword.text.equals(wordtext)){
		newocc=new Occurrence(this,sentence,nextword,beginIndex,endIndex,tokenIndex);
		nextword.occurrences.add(newocc);		
		foundWord=true;
	    }	    
	}	
	//else
	if (!foundWord){
	    //create a new occurrences array, i.e. set frequency =1 
	    Word newword = new Word(this,sentence, wordtext);
	    newocc = new Occurrence(this,sentence,newword,beginIndex,endIndex,tokenIndex);
	    newword.occurrences.add(newocc);
	    words.add(newword);
	}

	occurrences.add(newocc);			
	return newocc;		
    }

    public String substr(int begin, int end){
	return text.substring(begin,end);
    }

    public int getWordCount(){
	return occurrences.size();
    }

    public String averageSentenceLength(){
	return String.valueOf(words.size()/sentences.size());
    }

    public void printSentenceLengths(){
	int maxSize=0;
	for(int i=0;i<sentences.size();i++){
	    if(maxSize < sentences.get(i).words.size())
		maxSize = sentences.get(i).words.size();
	}

	int[] counts = new int[maxSize+1];

	for(int i=0;i<maxSize;i++)
	    counts[i]=0;
	for(int i=0;i<sentences.size();i++){
	    counts[sentences.get(i).words.size()]++;
	}

	for(int i=0;i<maxSize;i++)
	    if (counts[i]!=0)
		System.out.println(counts[i] + " Sentences of length " + i);
	    
    }

    public void printDocumentWithoutKeywords(int n_keywords, String newfilepath) throws IOException{
	String returntext=this.text;
	for (int i=0;i<n_keywords;i++){
	    returntext = returntext.replaceAll(chiSquareTable[i].text, " ");
	}
	
	PrintStream file = new PrintStream(new FileOutputStream(newfilepath));
	file.println(returntext);
	file.close();		    	
    }
    
}







class WordFreqComparator implements Comparator<Word>{
    public int compare(Word worda, Word wordb){
	if(worda.getFrequency() > wordb.getFrequency())
	    return -1;
	else if(worda.getFrequency() < wordb.getFrequency())
	    return 1;
	else
	    return 0;
    }
}
    
    
class ChiSquareComparator implements Comparator<Word>{
    public int compare(Word worda, Word wordb){
	if(worda.getChiSquare() > wordb.getChiSquare())
	    return -1;
	else if(worda.getChiSquare() < wordb.getChiSquare())
	    return 1;
	else
	    return 0;
    }

}
