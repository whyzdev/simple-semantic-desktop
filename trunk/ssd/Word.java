package ssd;
 
import java.util.*;

public class Word implements Comparable{
    public ChiSqDocument document;
    public String text;
    public double probability_g;
    public double chiSquare;

    public ArrayList<Sentence> sentences;
    public ArrayList<Occurrence> occurrences;
    public HashMap<Word,Integer> coOccurrences;

    public Word(ChiSqDocument document, Sentence sentence, String text){
	this.text = text;
	this.document=document;
	occurrences = new ArrayList<Occurrence>();
	sentences = new ArrayList<Sentence>();
	sentences.add(sentence);
	coOccurrences = new HashMap<Word,Integer>();
    }
    
    public void addOccurrence(Occurrence o){
	occurrences.add(o);
    }

    public int getFrequency(){
	return occurrences.size();
    }
    public int getCoOccurrence(Word word){
	if (word.document != this.document)
	    return 0;	
	if (coOccurrences.containsKey(word))
	    return ((Integer)this.coOccurrences.get(word)).intValue();
	else 
	    return 0;
    }


    public int compareTo(Object otherword) throws ClassCastException {
    if (!(otherword instanceof Word))
      throw new ClassCastException("A Word object expected.");
	    if(this.getFrequency() > ((Word)otherword).getFrequency())
		return -1;
	    else if(this.getFrequency() < ((Word)otherword).getFrequency())
		return 1;
	    else
		return 0;
    }
    
    public void calculateChiSquare(){
	    //loop over all frequent terms G
	int n_w=0;
	double chiSquare=0;
	
	for(int i=0;i<document.frequencyTable.length;i++){
		Word g = document.frequencyTable[i];
		n_w += this.getCoOccurrence(g); //co-occurrence of this word with word g 		
	}
	
	double n_w_times_p_g=probability_g*((double)n_w) ; 
	
	if (n_w_times_p_g > 0.0001){ //estimate when denominator is too small
	for(int i=0;i<document.frequencyTable.length;i++){
		Word g = document.frequencyTable[i];
		double freq = ((double)this.getCoOccurrence(g)); //co-occurrence of this word with word g 
		chiSquare += (freq - n_w_times_p_g)*(freq - n_w_times_p_g);
	}	
	chiSquare = chiSquare / n_w_times_p_g;
	}
	else
	    chiSquare=0;
	this.chiSquare = chiSquare;
    }
    
    public double getChiSquare(){
	return chiSquare;    
    }
    
}

