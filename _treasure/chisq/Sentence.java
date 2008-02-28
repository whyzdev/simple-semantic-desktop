import java.util.*;

public class Sentence{
    public Document document;
    public int beginIndex;
    public int endIndex;
    public String text;

    public ArrayList<Occurrence> occurrences;
    public TreeSet<Word> words;


    public Sentence(Document document){
	this.document=document;
	words = new TreeSet<Word>();
    }

    public void setParams(String text, ArrayList<Occurrence> occurrences, int beginIndex, int endIndex){
	this.text=text;
	this.occurrences = occurrences;
	this.words = words;
	this.beginIndex=beginIndex;
	this.endIndex=endIndex;
	
	Iterator i = occurrences.iterator();
	while(i.hasNext()){
	    Word nextword=(((Occurrence)i.next()).word);
	    words.add(nextword);
	}
    }



    public String toString(){
	return document.substr(beginIndex,endIndex);
    }

    public int getLength(){
	return endIndex - beginIndex;
    }

}
