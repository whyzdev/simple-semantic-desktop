/*
brain - a java class for training and using a single Perceptron
- Akshat Singhal, 04/24/07 Oberlin College
*/ 


import java.io.*;
import java.util.*;

    /*
     *
     * main() 
     * - if given two arguments, main takes an already trained configuration
     * , runs a given set of input sets on the perceptron and returns a 1 for
     * when the perceptron will fire and a 0 otherwise
     * - if given four or more arguments, it trains the perceptron on the 
     * given input and expected output for given alpha and theta
     *
     */    
public class brain {
        
    public static void main(String args[]){
	try {
	    Scanner training_input;
	    Scanner expected_output;
	    double alpha;
	    double threshold;
	    boolean debug;
	    
	    if (args.length == 2){		
		Perceptron perceptron = new Perceptron();
		perceptron.readConfiguration(args[1]);
		perceptron.runInputFile(args[0]);		
	    }
	    else if (args.length >= 4){
		alpha = Double.parseDouble(args[2]);
		threshold = Double.parseDouble(args[3]);
		debug = (args.length>4);	
		Perceptron perceptron = new Perceptron();
		perceptron.trainOnFiles(args[0], args[1], alpha, threshold, debug);
		perceptron.writeConfiguration();		
	    }		    
	    
	}
	catch (Exception e){
	    System.err.println("ERROR: invalid input:" + e);
	}	
    }


}
