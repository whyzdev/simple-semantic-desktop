 
import java.util.*;

    public class AkshatDocument{
	String name;
	int tokenCount;
	int index;
	double[] distances;
	int[] entityFreqs;
	int entitySum;

	AkshatDocument(String a, int c, int d){
	    name=a;
	    tokenCount=c+1;
	    index=d;
	    entitySum=1;
	}

	public String toString(){
	    return name;
	}
	public  void setDistances(double[] d){
	    distances=d;
	}
    }
