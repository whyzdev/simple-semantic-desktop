/*
 *
 Perceptron.java - the Perceptron class implements a Perceptron.
 - Akshat Singhal, 04/24/07 - Oberlin College
*/
import java.io.*;
import java.util.*;

public class Perceptron {
    double[] weights;
    int N=0;//N=number of inputs/weights
    double alpha=0.0;//alpha = learning rate
    double threshold=0.0;//threshold = threshold value
    double error_threshold=0.0;//threshold = threshold value    
    double[][] weight_history;
    int HISTORYSIZE = 5, historyctr=0;
    int currentError = 0;
    int currentSetError = 5;
    static boolean DIAG =false;
    static int MAXRUNS = 10000;

    /*
     *
     Perceptron(3 arguments) - constructor 
    */
    public Perceptron(int N, double alpha, double threshold){
	this.N=N;
	this.alpha=alpha;
	this.threshold=threshold;
	weights = new double[N];
	for (int i=0;i<N;i++){
	    weights[i]=Math.random() - 0.5;
	}
	weight_history=new double[HISTORYSIZE][N];
    }

    /*
     *
     Perceptron(no arguments) - constructor for a blank
     perceptron
    */
    public Perceptron(){
	this.N=0;
	this.alpha=0;
	this.threshold=0;
	weights = new double[0];
	weight_history=new double[HISTORYSIZE][0];
    }

    /*
     *
     Perceptron(4 arguments) - constructor 
    */
    public Perceptron(int N, double alpha, double threshold,double[] weights){
	this.N=N;
	this.alpha=alpha;
	this.threshold=threshold;
	this.weights=weights;
	weight_history=new double[HISTORYSIZE][N];
    }


    /*
     *
     Y()- gives output from the perceptron, 1 if it will fire, 0 if it won't
    */
    public int Y(int[] input){
	return Step(X(input));
    }

    /*
     *
     Step() - returns a 1 if passed value x is above threshold and 0 otherwise
    */
    public int Step(double x){
	if (x > threshold)
	    return 1;
	else
	    return 0;
    }

    /*
     *
     X() - returns the sum of all input and weight pairs
    */
    private double X(int[] input){
	double returnvalue=0;
	for (int i=0;i<N;i++){
	    returnvalue += ((double)input[i])*weights[i];
	}
	return returnvalue;
    }

    /*
     *
     trainOne() - trains the Perceptron on one set of inputs and their desired
     output.
    */    
    public void trainOne(int[] input, int output_actual, int output_desired){
	double[] newweights = new double[N];
	for (int i=0; i<N; i++){
	    newweights[i]=weights[i] + 
		alpha*(output_desired- output_actual)*input[i];
	}
	currentError =  output_desired - output_actual;
	currentSetError += Math.abs(currentError);
	setWeights(newweights);
	
    }

    /*
     *
     train() - trains the perceptron on a number of input sets      
     */
    public void train(int[][] input, int[] output_desired, 
		      boolean debug, int epoch_counter){
	currentSetError =0;
	for (int i=0;i<output_desired.length;i++){
	    int currentY=0;
	    trainOne(input[i],currentY=Y(input[i]),output_desired[i]);
	    if (debug)
		printStatusLine(epoch_counter, input[i], currentY, output_desired[i]);
	}	
    }


    /*
     *
     trainOnFiles() - trains the perceptron on given input and expected output
     files
     */
    public void trainOnFiles(String input_filename, String output_filename, 
			     double alpha, double threshold, boolean debug){
	try {
	    Scanner training_input 
		= new Scanner (new File(input_filename)).useDelimiter("\n");
	    Scanner expected_output = new Scanner (new File(output_filename));
	    this.alpha=alpha;
	    this.threshold=threshold;
	
	    //determine N, the number of inputs
	    Scanner currentline = new Scanner(training_input.next());
	    N=0;
	    while(currentline.hasNext()){
		N++;currentline.next();}
	    if (DIAG)
		System.err.println("N is " + N);
	    currentline.close();
	    training_input.close();
	    training_input = new Scanner (new File(input_filename));	
	    

	    //initialize weight matrix for N inputs
	    weights = new double[N];
	    for (int i=0;i<N;i++)
		weights[i]=Math.random() - 0.5;


	    //determine k, the number of input sets	
	    int k=0;
	    while(expected_output.hasNext()){
		k++;expected_output.next();}
	    expected_output.close();
	    expected_output = new Scanner (new File(output_filename));
	    if (DIAG)
		System.err.println("k = " + k );
	
	    //maintainence
	    if (debug)
		printStatusHeader();
	    int epoch_counter=0;	
	    int[][] input = new int[k][N];
	    int[] output= new int[k];
	
	
	    //parse all input sets into a 2D array 
	    for (int i=0;i<k;i++){
		for (int j=0;j<N;j++){
		    input[i][j] = Integer.parseInt(training_input.next());	
		    if (DIAG)
			System.err.printf("%d ",input[i][j]);
		}
		if (DIAG)
		    System.err.printf("\n");
	    }
	
	    //parse expected output array
	    for (int i=0;i<k;i++){
		output[i] = Integer.parseInt(expected_output.next());
	    }
	    if (DIAG)
		System.err.println("parsed output");

	    //Run a loop for training as long as you can or need to		
	    while(getError() > 0 && epoch_counter < MAXRUNS){
		epoch_counter++;
		//train this Perceptron on input and outputs
		train(input, output, debug, epoch_counter);		    
	    }
	    if (DIAG)
		System.err.println("training done");

	}
	catch(Exception e) {
	    System.err.println("ERROR: I/O Error:" + e);
	}
    }


    /*
     *     
     writeConfiguration() - prints out the configuration of this perceptron to
     Standard Output.
     */
    public void writeConfiguration(){	
	System.out.print(N + " ");
	System.out.print(alpha + " ");
	System.out.print(threshold + " ");
	for (int i=0;i<N;i++)
	    System.out.print(weights[i] + " ");
	System.out.print("\n");	
    }


    /*
     *     
     readConfiguration() - reads this perceptron's configuration from 
     a config file that was created using writeConfiguration()
     */

    public void readConfiguration(String config_filename){	
	try{
	    Scanner config_file = new Scanner(new File(config_filename));
	    N = Integer.parseInt(config_file.next());
	    if (DIAG)
		System.err.println("N is " + N);
	
	    alpha = Double.parseDouble(config_file.next());
	    threshold = Double.parseDouble(config_file.next());		
	
	    weights = new double[N];
	    for (int i=0;i<N;i++){
		weights[i]=Double.parseDouble(config_file.next());
	    }
	    config_file.close();	
	}
	catch(Exception e) {
	    System.err.println("ERROR: I/O Error:" + e);
	}
    }



    /*
     *
     *runInputFile() - Given an input file's filename,
     *runs the perceptron on each input set and gives
     *appropriate output
     *@params(0) - run_input_filename - filename of input file
     *
     */    
    public void runInputFile(String run_input_filename){
	try{
	    //load input file for this run
	    Scanner run_input = new Scanner(new File(run_input_filename));

	    //loop through file
	    while (run_input.hasNext()){
		//gather inputs for current input set
		int[] input = new int[N];
		for (int i=0;i<N;i++){
		    input[i]=Integer.parseInt(run_input.next());
		}

		//output whether perceptron will fire or not 
		//on this input set
		System.out.println(Y(input));		    
	    }	
	}
	catch(Exception e) {
	    System.err.println("ERROR: I/O Error:" + e);
	}	
    }


    /*
     *
     printStatusHeader() - In order to print the debugging output of this 
     Perceptron's training, prints the header column of the worksheet
     *
     */    
    public void printStatusHeader(){
	System.err.print("Epoch\t");
	for (int i=0;i<N;i++)
	    System.err.printf("X%d\t",i+1);
	System.err.print("Y(exp)\t");
	System.err.print("Y(act)\t");
	System.err.print("Error\t");
	for (int i=0;i<N;i++)
	    System.err.printf("W%d\t",i+1);
	System.err.print("\n");
    }




    /*
     *
     printStatusLine() - In order to print the debugging output of this 
     Perceptron's training, prints one line for one  epoch and one input set
     *
     */    
    public void printStatusLine(int epoch, int[] input, 
				int output_actual, int output_desired){
	System.err.printf("%d\t", epoch);
	for (int i=0;i<N;i++)
	    System.err.printf("%d\t",input[i]);
	System.err.printf("%d\t",output_desired);
	System.err.printf("%d\t",output_actual);
	System.err.printf("%d\t",currentError);
	for (int i=0;i<N;i++)
	    System.err.printf("%.1f\t",weights[i]);
	System.err.print("\n");	
    }

    /*
     *
     setWeights() - replace the weight matrix of the Perceptron
     *
     */
    private void setWeights(double[] weights){
	if (weights.length == N){
	    weight_history[(historyctr++) % HISTORYSIZE] = this.weights;
	    this.weights = weights;	    
	}
	else
	    System.err.println("ERROR:invalid Weight list used for update");
    }

    /*
     *
     setThreshold() - set the Threshold value of this perceptron
     *
     */
    public void setThreshold(double threshold){
	this.threshold=threshold;
    }

    /*
     *
     setLearningRate() - set the learning rate of this Perceptron
     *
     */
    public void setLearningRate(double alpha){
	this.alpha=alpha;
    }


    /*
     *
     getError() - returns the sum of all errors in last epoch
     *
     */
    public int getError(){
	return currentSetError;

    }

}
