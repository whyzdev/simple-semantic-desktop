import java.util.*;
import java.io.*;

public class generateIO{
    static int BINARY_NUM_SIZE=8;
    static int OR_NUM_SIZE=2;
    static int NAND_NUM_SIZE=3;

    public static void main(String args[]){
	    try {

	if (Integer.parseInt(args[0])==0){
	    //generate input and output for majority func

		BufferedWriter out1 = new BufferedWriter(new FileWriter("or_input"));		
		BufferedWriter out2 = new BufferedWriter(new FileWriter("or_exp_output"));		
		int[] bitstring = new int[OR_NUM_SIZE];
		for (int i=0;i<((int)Math.pow(2.0,OR_NUM_SIZE));i++){
		    int[] binnum = intToBinArray(i,OR_NUM_SIZE);
		    writeArray(out1, binnum);
		    out2.write(or(binnum) + "\n");		    
		}		
		out1.close();
		out2.close();		

	}

	if (Integer.parseInt(args[0])==1){
	    //generate input and output for majority func

		BufferedWriter out1 = new BufferedWriter(new FileWriter("major_input"));		
		BufferedWriter out2 = new BufferedWriter(new FileWriter("major_exp_output"));		
		int[] bitstring = new int[BINARY_NUM_SIZE];
		for (int i=0;i<((int)Math.pow(2.0,BINARY_NUM_SIZE));i++){
		    int[] binnum = intToBinArray(i,BINARY_NUM_SIZE);
		    writeArray(out1, binnum);
		    out2.write(majority(binnum) + "\n");		    
		}

		
		out1.close();
		out2.close();		

	}
	else if (Integer.parseInt(args[0])==2){
	    //generate input and output for odd function

		BufferedWriter out1 = new BufferedWriter(new FileWriter("odd_input"));		
		BufferedWriter out2 = new BufferedWriter(new FileWriter("odd_exp_output"));		
		BufferedWriter out3 = new BufferedWriter(new FileWriter("odd_datapoints"));		
		int[] bitstring = new int[BINARY_NUM_SIZE];
		for (int i=0;i<((int)Math.pow(2.0,BINARY_NUM_SIZE));i++){
		    int[] binnum = intToBinArray(i,BINARY_NUM_SIZE);
		    writeArray(out1, binnum);
		    writeDataPoint(out3, binnum, oddbits(binnum));
		    out2.write(oddbits(binnum) + "\n");		    
		}
		
		out1.close();
		out2.close();		
		out3.close();		

	    

	}

	else if (Integer.parseInt(args[0])==3){
	    //generate input and output for odd number function
	    // fn4, i.e. the odd number function returns 1 if a 
	    // given bitstring represents an odd number, 0 otherwise
		BufferedWriter out1 = new BufferedWriter(new FileWriter("fn4_input"));		
		BufferedWriter out2 = new BufferedWriter(new FileWriter("fn4_exp_output"));		
		int[] bitstring = new int[BINARY_NUM_SIZE];
		for (int i=0;i<((int)Math.pow(2.0,BINARY_NUM_SIZE));i++){
		    int[] binnum = intToBinArray(i,BINARY_NUM_SIZE);
		    writeArray(out1, binnum);
		    out2.write(oddnumber(i) + "\n");		    
		}		
		out1.close();
		out2.close();			    
	}


	else if (Integer.parseInt(args[0])==4){
	    //generate input and output for NAND function
		BufferedWriter out1 = new BufferedWriter(new FileWriter("nand_input"));		
		BufferedWriter out2 = new BufferedWriter(new FileWriter("nand_exp_output"));		
		int[] bitstring = new int[NAND_NUM_SIZE];
		for (int i=0;i<((int)Math.pow(2.0,NAND_NUM_SIZE));i++){
		    int[] binnum = intToBinArray(i,NAND_NUM_SIZE);
		    writeArray(out1, binnum);
		    out2.write(nand(binnum) + "\n");		    
		}		
		out1.close();
		out2.close();			    
	}




	    } 
	    catch (IOException e) {
	    }  

    }

    public static int[] intToBinArray(int n, int size){
	int[] returnarray = new int[size];
	int ctr=size-1;
	while (n > 0){
	    returnarray[size-1-ctr]=
		(int)((double)n/(double)Math.pow(2.0,ctr));
	    n%=Math.pow(2.0,ctr);
	    ctr--;
	}
	return returnarray;	
    }

    public static void writeArray(BufferedWriter outfile, int[] binnum) 
	throws IOException{
	for (int i=0;i<binnum.length;i++)
	    outfile.write(binnum[i] + " ");
	outfile.write("\n");	
    }

    public static void writeDataPoint(BufferedWriter outfile, int[] binnum, int output) 
	throws IOException{
	for (int i=0;i<binnum.length;i++)
	    outfile.write(binnum[i] + "");
	outfile.write("," + output + "\n");	
    }

    public static int majority(int[] binnum){
	int num_ones=0;
	for (int i=0;i<BINARY_NUM_SIZE;i++)
	    if (binnum[i]==1)
		num_ones++;
	if (num_ones > (BINARY_NUM_SIZE/2))
	    return 1;
	else 
	    return 0;		   	
    }

    public static int oddbits(int[] binnum){
	int num_ones=0;
	for (int i=0;i<BINARY_NUM_SIZE;i++)
	    if (binnum[i]==1)
		num_ones++;
	if ((num_ones % 2)==1)
	    return 1;
	else 
	    return 0;		   	
    }

    public static int or(int[] binnum){
	int num_ones=0;
	for (int i=0;i<OR_NUM_SIZE;i++)
	    if (binnum[i]==1)
		return 1;
	return 0;		   	
    }


    public static int nand(int[] binnum){
	int num_ones=0;
	for (int i=0;i<NAND_NUM_SIZE;i++)
	    if (binnum[i]==1)
		num_ones++;
	if (num_ones == NAND_NUM_SIZE)
	    return 1;
	else 
	    return 0;		   	
    }


    public static int oddnumber(int num){
	if (num%2==1)
	    return 1;
	else 
	    return 0;
    }

}
