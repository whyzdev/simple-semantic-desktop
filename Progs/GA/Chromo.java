/* Chromo.java - Chromosome class for use with ga.java, a Genetic Algorithm
   for maximizing a function's value 
   written by Akshat Singhal, Spring 07, at Oberlin College
*/

import java.io.*;
import java.util.*;
import java.lang.Math;

class Chromo{
    boolean diag1=false;
    boolean diag2=false;
    public BitSet bitstring;
    public double fitness;
    public double f_value;
    private int bitstringlength;
    public int fitnessrank;

    /* Chromo() - constructor for the Chromosome class */
    public Chromo(int BitStringLength){
	this.bitstringlength=BitStringLength;
	double x=Math.random()*20;
	double y=Math.random()*20;
	bitstring=makeBitString(x,y);
	if (diag1)
	    System.out.println("X is " + x + ", i.e. " 
			       + getX() + " after conversion" );
	if (diag1)
	    System.out.println("Y is " + y + ", i.e. " 
			       + getY() + " after conversion" );
	updateFitness();
	if (diag2)
	    System.out.format("f(%.3f,%.3f)=%.3f\n",getX(),getY(),f_value);
	//	    System.out.println("f(" + getX() + "," + getY() + ")=" + f_value );
    }

    /* makeBitString() - makes a bit string of required length from two 
       double precision numbers */
    public BitSet makeBitString(double x, double y){
	BitSet returnset = new BitSet(bitstringlength);
	int bitcounter=0;
	double bitpowercounter=4;
	double remainder=x;
	while(bitcounter<bitstringlength/2){
	    //	    System.out.println(bitcounter);
	    returnset.set(bitcounter, 
			  (((int)(remainder/Math.pow(2.0,bitpowercounter))) 
			   == 1));
	    remainder=remainder%Math.pow(2.0,bitpowercounter);
	    bitcounter++;
	    bitpowercounter--;

	}
	//	System.out.println("-");
	bitpowercounter=4;
	remainder=y;

	while(bitcounter<bitstringlength){
	    //	System.out.println(bitcounter);
	    returnset.set(bitcounter, 
			  (((int)(remainder/Math.pow(2.0,bitpowercounter))) 
			   == 1) );
	    remainder=remainder%Math.pow(2.0,bitpowercounter);
	    bitcounter++;
	    bitpowercounter--;
	}

	
	if (diag1)
	    System.out.println("The Number " + x + "," + y 
			       + "converts to " + returnset);	
	return returnset;
    }
    
    /* getX() -  returns the first value from the bit string of this Chromo*/
    public double getX(){
	int bitcounter=0;
	double result=0;
	double bitpowercounter=4;
	while(bitcounter<bitstringlength/2){
	    if (bitstring.get(bitcounter))
		result += Math.pow(2,bitpowercounter);
	    bitcounter++;
	    bitpowercounter--;
	}
	
	return result;
    }


    /* getY() -  returns the second value from the bit string of this Chromo*/
    public double getY(){
	int bitcounter=bitstringlength/2;
	double result=0;
	double bitpowercounter=4;
	while(bitcounter<bitstringlength){
	    if (bitstring.get(bitcounter))
		result += Math.pow(2,bitpowercounter);
	    bitcounter++;
	    bitpowercounter--;
	}
	
	return result;
    }

    /* mutate() -  flips bits in the bit string with given probability*/
    public void mutate(double mutationprobability){
	for (int i=0;i<bitstringlength/2;i++)
	    if (sayYesByProbability(mutationprobability))
		bitstring.set(i,!bitstring.get(i));
	updateFitness();
	return;
    }

    /* crossover() - does a uniform crossover on two bitstrings */
    public Chromo crossover(Chromo chromo2){
	Chromo kiddo=new Chromo(bitstringlength);
	for (int i=0;i<bitstringlength;i++)
	    if (Math.random() > 0.5)
		kiddo.bitstring.set(i,bitstring.get(i));
	    else
		kiddo.bitstring.set(i,chromo2.bitstring.get(i));
	kiddo.updateFitness();
	return kiddo;
    }
    
    /* updateFitness() - updates the fitness value of this Chromo*/
    public void updateFitness(){
	f_value=fitness=objective_function(getX(),getY());
	//remove infeasible solutions from the system
	while (Double.isNaN(fitness)) {
	    bitstring=makeBitString(20*Math.random(),20*Math.random());
	    f_value=fitness=objective_function(getX(),getY());
	}
	return;	
    }

    /*objective_function() - the objective function to maximize */
    private static double objective_function(double x, double y){
	double k=10;
	return ((Math.sin(x-k)*Math.sin(y-k))/((x-k)*(y-k)));
    }

    /* sayYesByProbability() - returns true with a given probability */
    public static boolean sayYesByProbability(double probability){
	if (Math.random()<=probability)
	    return true;
	else 
	    return false;
    }
    
}
