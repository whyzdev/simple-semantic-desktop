import java.util.*;
public class Occurrence {
    public Document document;
    public int beginIndex;
    public int endIndex;
    public int tokenIndex;
    public Word word;
    public Sentence sentence;
    public String text;

    public Occurrence(Document document, Sentence sentence,Word word, int beginIndex, int endIndex,int tokenPosition){
	this.word=word;
	this.sentence=sentence;
	this.document=document;
	this.beginIndex=beginIndex;
	this.endIndex=endIndex;
	this.tokenIndex=tokenIndex;
	this.text=this.word.text;
    }
    public Occurrence(){
	;
    }

    
}
