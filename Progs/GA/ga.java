/* 

........GGGGGGGGGGGGG...............AAA...............
.....GGG::::::::::::G..............A:::A..............
...GG:::::::::::::::G.............A:::::A.............
..G:::::GGGGGGGG::::G............A:::::::A............
.G:::::G.......GGGGGG...........A:::::::::A...........
G:::::G........................A:::::A:::::A..........
G:::::G.......................A:::::A.A:::::A.........
G:::::G....GGGGGGGGGG........A:::::A...A:::::A........
G:::::G....G::::::::G.......A:::::A.....A:::::A.......
G:::::G....GGGGG::::G......A:::::AAAAAAAAA:::::A......
G:::::G........G::::G.....A:::::::::::::::::::::A.....
.G:::::G.......G::::G....A:::::AAAAAAAAAAAAA:::::A....
..G:::::GGGGGGGG::::G...A:::::A.............A:::::A...
...GG:::::::::::::::G..A:::::A...............A:::::A..
.....GGG::::::GGG:::G.A:::::A.................A:::::A.
........GGGGGG...GGGGAAAAAAA...................AAAAAAA

ga.java - A function value maximizing Genetic Algorithm implemented in Java.
written by Akshat Singhal at Oberlin College, Spring 07, CSCI 364 on 3-14-07

*/

import java.io.*;
import java.util.*;

/* ga - the main class used by the genetic algorithm */
class ga{
    boolean selectionMethod  ;
    int populationSize ;
    int bitStringLength ;
    int numberOfGenerations ;
    double probCrossover  ;
    double probMutation       ;
    boolean elitism;
    Chromo[] population;    

    static boolean diag3=false;
    static boolean diag4=false;
    static boolean diag5=true;
    static boolean diag6=true;
    static boolean diag7=false;

    public static void main(String args[]){
	if (args.length != 7){
	    printArgumentScreen();
	    return ;
	}

	ga ga1=new ga();
	
	ga1.selectionMethod=(args[0].toUpperCase().charAt(0)=='R');
	ga1.populationSize=Integer.parseInt(args[1]);
	ga1.bitStringLength=Integer.parseInt(args[2]);
	ga1.numberOfGenerations=Integer.parseInt(args[3]);
	ga1.probCrossover=(int)(Double.parseDouble(args[4])*100);
	ga1.probMutation=(int)(Double.parseDouble(args[5])*100);
	ga1.elitism=(Integer.parseInt(args[6])==1);
	
	if (ga1.populationSize <= 1 || ga1.bitStringLength <= 4 || 
	    ga1.numberOfGenerations <=1 || ga1.probCrossover <0 || 
	    ga1.probCrossover > 100 || ga1.probMutation <0 || 
	    ga1.probMutation > 100 || ga1.bitStringLength %2 ==1){
	    printArgumentScreen();
	    return;
	}
	    
	if (diag6){
	    System.out.print("\nStarting Genetic Algorithm with the following parameters:\nSelection Method:");
	    if (ga1.selectionMethod==true)
		System.out.print("Roulette Wheel Selection\n");
	    else
		System.out.print("Alternative Selection\n");
	    System.out.println("Population Size: " + ga1.populationSize);
	    System.out.println("Bit String Length: " + ga1.bitStringLength);
	    System.out.println("Number of Generations: " + 
			       ga1.numberOfGenerations);
	    System.out.println("Probability of Crossover: " + 
			       ga1.probCrossover);
	    System.out.println("Probability of Mutation: " + ga1.probMutation);
	    System.out.print("Elitism: " );
	    if (ga1.elitism == true)
		System.out.print("enabled\n");
	    else 
		System.out.print("disabled\n");
	}
	ga1.run();	
    }

    /* run() - once the parameters for the ga class have been initialized,
       run the genetic algorithm for the specified number of generations*/
    private void run(){
	population= new Chromo[populationSize];

	// Create a Population of Chromosomes
	for (int i=0; i<populationSize;i++){
	    population[i]=new Chromo(bitStringLength);	    
	}

	if (diag4)
	    System.out.println("finding fittest, beginning algorithm");
	int momindex=0,dadindex=0,fittestindex=selectFittest(), loserindex=0;
	boolean ifCrossover=false;
	if (diag4)
	    System.out.println("fittest chromosome number: "  + fittestindex);
	
	for (int i=1; i<=numberOfGenerations; i++){
	    // Select the unlucky (most unfit) Chromo among the population
	    loserindex = selectLoser();
	    if (elitism)
		if(loserindex==fittestindex)
		    loserindex=(int)(Math.random()*(populationSize-1));
		    
	    if (diag4)
		System.out.println("dieing chromosome number: "  + loserindex);
	    // Choose the lucky(probably fit) candidates for crossover
	    momindex=selectWinner();

	    while((dadindex=selectWinner()) == momindex)
		;
	    // Do crossovers among the chosen ones
	    // Kill the unlucky individual
	    //and add new child to population
	    if ((ifCrossover=sayYesByProbability(probCrossover))==true)
		population[loserindex]=population[momindex].crossover(population[dadindex]);
	    else
		population[loserindex]=population[momindex];
	    if (diag3)
		if (ifCrossover)
		    System.out.format("Chromos %d (f()=%.2f) and %d (f()=%.2f) produced %d (f()=%.2f) \n", momindex, population[momindex].fitness, dadindex, population[dadindex].fitness, loserindex, population[loserindex].fitness);
		else
		    System.out.format("Chromos %d (f()=%.2f) asexually produced %d (f()=%.2f) \n" , momindex, population[momindex].fitness, loserindex, population[loserindex].fitness);

	    // Find fittest among the population and name him Moses
	    fittestindex=selectFittest();
	    if (diag4)
		System.out.println("fittest chromosome number: "  
				   + fittestindex);

	    // Randomly mutate the population
	    for(int j=0;j<populationSize;j++)
		if (elitism){
		    if (j!=fittestindex)
			population[j].mutate(probMutation);
		}
		else
		    population[j].mutate(probMutation);
	    if (diag5)
		System.out.format("Generation %d's Highest Fitness:f(%.2f,%.2f)=%.2f, Average Fitness: %.2f\n",i,population[fittestindex].getX(),population[fittestindex].getY(),population[fittestindex].fitness, getAverageFitness());
	}
    }

    /* printArgumentScreen() - prints a screen describing  correct arguments*/
    private static void printArgumentScreen(){
	System.out.println("Insufficient or invalid Arguments. GA takes arguments of the format:" );
	System.out.println("java ga SelectionMethod PopulationSize BitStringLength NumberOfGenerations ProbCrossover ProbMutation Elitism " );
	System.out.println("example: $ java ga R 20 10 100 0.7 0.01 1\n" );
	System.out.println("SelectionMethod - [R or A] Roulette Wheel Selection (R)  or Alternative Selection (A). " );
	System.out.println("PopulationSize - [1 to 2E9] Number of Chromosomes in the population. " );
	System.out.println("BitStringLength - [10 to 100, even numbers only] length of the bitstring that represents both solutions" );
	System.out.println("NumberOfGenerations - [1 to 2E9] number of iterations the system should run through " );
	System.out.println("ProbCrossover - [0.00 to 1.00] Probability of two selected individuals to crossover  " );
	System.out.println("ProbMutation - [0.00 to 1.00] Probability of mutation in a Chromosome in the population " );
	System.out.println("Elitism - [0 or 1] Whether the overall best solution is saved or not \n" );
	
    }

    /* selectWinner() - Uses Roulette-Wheel Selection or 
       Tournament Selection to select a lucky(probably fit) Chromosome */    
    private int selectWinner(){
	double averagefitness=getAverageFitness();
	if (selectionMethod){ //Roulette wheel selection
	    int counter=0;
	    int i=(int)(Math.random()*(populationSize-1));
	    while(true){
		if (sayYesByProbability(population[i].fitness/averagefitness))
		    return i;
		else
		    i=(i+1)%populationSize;
	    }
	}
	else { //Tournament selection

	    int tournamentsize=populationSize/10;
	    boolean tournament_is_incomplete=true;
	    int[] tournament=new int[tournamentsize];
	    int tcounter=0;
	    for (int i=0;i<tournamentsize;i++)
		tournament[i]=-1;
	    while (tournament_is_incomplete){
		boolean newentry_is_unique=true;
		int newentry=(int)(Math.random()*(populationSize-1));
		for (int i=0;i<tournamentsize;i++)
		    newentry_is_unique = 
			newentry_is_unique && (newentry != tournament[i]);		
		if (newentry_is_unique)
		    tournament[tcounter++]=newentry;
		if (tcounter==tournamentsize)
		    tournament_is_incomplete=false;
	    }
	    if (diag7){
		System.out.print("Tournament set: ");
		for(int k=0;k<tournamentsize;k++)
		    System.out.print(tournament[k] + ",");
		System.out.print("\n");
	    }
	    double highestfitness=population[tournament[0]].fitness;
	    int mostfit=0;
	    for (int i=0;i<tournamentsize;i++)
		if (population[tournament[i]].fitness > highestfitness){
		    highestfitness=population[tournament[i]].fitness;
		    mostfit=i;
		}
	    return mostfit;
	}
    }

    /* selectFittest() - returns index of the most fit Chromosome */
    private int selectFittest(){
	double highestfitness=population[0].fitness;
	int mostfit=0;
	for (int i=0;i<populationSize;i++)
	    if (population[i].fitness > highestfitness){
		highestfitness=population[i].fitness;
		mostfit=i;
	    }
	return mostfit;
	
    }

    /* selectLoser() - returns index of the most unfit Chromosome */
    private int selectLoser(){
	double lowestfitness=population[0].fitness;
	int leastfit=0;
	for (int i=0;i<populationSize;i++)
	    if (population[i].fitness < lowestfitness){
		lowestfitness=population[i].fitness;
		leastfit=i;
	    }
	return leastfit;
	
    }

    /* getAverageFitness() - returns average fitness of the system */
    private double getAverageFitness(){
	double averagefitness=0;
	for (int i=0; i<populationSize;i++)
	    averagefitness+=population[i].fitness;
	averagefitness=averagefitness/populationSize;
	return averagefitness;
    }

    /*sayYesByProbability() - returns true with a given probability*/
    public static boolean sayYesByProbability(double probability){
	if (Math.random()<=probability)
	    return true;
	else 
	    return false;
    }

    /*randomized loser selection - discarded because it was too slow */

    /*
      private int selectLoser(){
      double averagefitness=getAverageFitness();
      if (selectionMethod){ //Roulette wheel selection
      int counter=0;
      int i=(int)(Math.random()*(populationSize-1));
      while(true){
      if (sayYesByProbability( 1 -(population[i].fitness/averagefitness)*(population[i].fitness/averagefitness) ))
      return i;
      else
      i=(i+1)%populationSize;
      }
      }
      else { //Tournament selection
      int tournamentsize=10;
      boolean tournament_is_incomplete=true;
      int[] tournament=new int[tournamentsize];
      int tcounter=0;
      for (int i=0;i<tournamentsize;i++)
      tournament[i]=-1;
      while (tournament_is_incomplete){
      boolean newentry_is_unique=true;
      int newentry=(int)(Math.random()*(populationSize-1));
      for (int i=0;i<tournamentsize;i++)
      newentry_is_unique = newentry_is_unique && (newentry != tournament[i]);		
      if (newentry_is_unique)
      tournament[tcounter++]=newentry;
      if (tcounter==tournamentsize)
      tournament_is_incomplete=false;
      }
	    
      double lowestfitness=population[tournament[0]].fitness;
      int leastfit=0;
      for (int i=0;i<tournamentsize;i++)
      if (population[tournament[i]].fitness < lowestfitness){
      lowestfitness=population[tournament[i]].fitness;
      leastfit=i;
      }
      return leastfit;
      }
      }*/
    
}
