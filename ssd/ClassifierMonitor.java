package ssd;



import java.util.*;
import java.io.*;
import com.aliasi.util.Files;

import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.JointClassification;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Streams;

import java.io.IOException;

public class ClassifierMonitor {



	private static File TESTING_DIR ;
	private static File MODELS_DIR ;
	private static File TESTING_RESULTS_DIR ;

	private static String[] CATEGORIES; 
	private static String[] PartsOfSpeech; 

	private static int NGRAM_SIZE ;
	private static HashMap<String,LMClassifier> compiledClassifiers;
	private static LMClassifier standardClassifier;

	public static void main(String[] args) 

	throws ClassNotFoundException, IOException, Exception {
		if (args.length != 4){
			System.out.println("No Arguments. No Model Trainer.");
			return;
		}		
		TESTING_DIR=new File(args[0]);
		TESTING_RESULTS_DIR = new File(args[1]);
		MODELS_DIR=new File(args[2]);
		File CATEGORIES_DIR=new File(args[3]);
		CATEGORIES  = CATEGORIES_DIR.list();
		NGRAM_SIZE =CATEGORIES.length;


		File[] models = MODELS_DIR.listFiles(); 
		PartsOfSpeech=new String[models.length];
		compiledClassifiers = new HashMap<String,LMClassifier>();

		for (int i=0;i<models.length;i++){

			LMClassifier classifier
			=  loadModel(models[i].getAbsolutePath());
			PartsOfSpeech[i]=models[i].getName().substring(0, models[i].getName().length()-6);
			compiledClassifiers.put(PartsOfSpeech[i], classifier);


		}
		standardClassifier = compiledClassifiers.get("all");



		//testing 
//		ConfusionMatrix confMatrix = new ConfusionMatrix(CATEGORIES);


		while (true){
			File[] testingFiles = TESTING_DIR.listFiles();
			if (testingFiles.length==0)
				Thread.sleep(3000);
			else 
				for (int j=0; j<testingFiles.length;  j++) {				
					Document d = new Document(testingFiles[j]);
					Set<Document> s =new HashSet<Document>();
					s.add(d);
					POSFinder p = new POSFinder(s);
					p.process();
					JointClassification jc = 
						jointJointClassify(d);
					String bestCategory = jc.bestCategory();
					JointClassification jc_standard = standardClassifier.classify(d.mText_str);

					System.out.println("File <"+testingFiles[j]+"> belongs to folder: " + bestCategory);				

					File bestCategoryDirectory = new File(TESTING_RESULTS_DIR,bestCategory);				
					if (!bestCategoryDirectory.exists())
						bestCategoryDirectory.mkdir();


					DocTreeWidget.copyFile(d.mFile, new File(bestCategoryDirectory,d.mFile.getName())); //this is just a file copy function
					d.mFile.delete();
				}
		}
	}		



	public static double variance(double[] m){
		double avg=0,var=0;

		for (double d: m)
			avg += d;

		avg=avg/m.length;

		for (double d: m)
			var += Math.pow(d-avg,2);
		var=var/m.length;

		return var;
	}

	private static JointClassification jointJointClassify(ssd.Document doc){

		double[][] classprobs = new double[PartsOfSpeech.length][CATEGORIES.length];	
		double[] jointprobs = new double[CATEGORIES.length]; 
		double[] ranks = new double[CATEGORIES.length];		
		double[] classif_variances = new double[PartsOfSpeech.length];

		for(double d : jointprobs)
			d=0;

		for(int i=0;i<CATEGORIES.length;i++)
			ranks[i]=0;

		JointClassification jc = new JointClassification(CATEGORIES,jointprobs);

		for (int i=0;i<PartsOfSpeech.length;i++){
			String pos=PartsOfSpeech[i];
			if (doc.mPosTexts.get(pos) != null){
				JointClassification jc1 = compiledClassifiers.get(pos).classify(doc.mPosTexts.get(pos));				
				for (int j=0;j<CATEGORIES.length;j++)					
					classprobs[i][j]=getCategoryConditionalProbability(CATEGORIES[j],jc1);
			}
		}

		for (int i=0;i<classif_variances.length;i++){
			classif_variances[i]=variance(classprobs[i]);//get the variance of each classifier into classif_variances
		}

		double average_variance=0, max_classif_variance =-1, sum_variance=0;
		for(double d : classif_variances){
			sum_variance += d;
			if (d > max_classif_variance)
				max_classif_variance=d;
		}

		average_variance = sum_variance / classif_variances.length;
		//say we cheat and let the average be the same as the 'all' partofspeech
		for (int i=0;i<PartsOfSpeech.length;i++)
			if (PartsOfSpeech[i].equals("all"))
				average_variance= classif_variances[i];
		//average_variance=max_classif_variance;
		/*switch this all back, write the paper, finish the presentation. WRAP THE FUCK UP */
		/* No! this method achieves 67% accuracy on 20news! */

		for (int i=0;i<CATEGORIES.length;i++)
			for (int j=0;j<classif_variances.length;j++)	
				ranks[i] +=  classif_variances[j]*classprobs[j][i]/sum_variance;


		double ranksum=0;
		for (double r: ranks)			
			ranksum+=r;



		for(int i=0;i<CATEGORIES.length;i++)
			ranks[i]=ranks[i]/ranksum;


		for(int i=0;i<CATEGORIES.length;i++)				
			ranks[i]=Math.log(ranks[i])/Math.log(2);



		ranks = sort_with_categories(ranks);
		//the new transformed matrix is a matrix of log2 of probabilities of belonging calculated from ranks 
		jc = new JointClassification(CATEGORIES,ranks);

		return jc;
	}

	static double[] updateRanks(JointClassification jc1,double[] ranks){
		double probabilityavg=0;
		double[] probSet = new double[CATEGORIES.length];
		for(int i=0;i<CATEGORIES.length;i++){
			probabilityavg +=getCategoryConditionalProbability(CATEGORIES[i],jc1);
			probSet[i]=getCategoryConditionalProbability(CATEGORIES[i],jc1);
		}		
		probabilityavg=probabilityavg/CATEGORIES.length;

		double probabilitySD=0;
		for(int i=0;i<CATEGORIES.length;i++){
			probabilitySD+=Math.pow(getCategoryConditionalProbability(CATEGORIES[i],jc1)-probabilityavg,2);
		}
		probabilitySD=probabilitySD/CATEGORIES.length;

		//double entropy = getEntropy(probSet);

		for(int i=0;i<CATEGORIES.length;i++){
			//if (jc1.bestCategory().equals(CATEGORIES[i]))
//			ranks[i] += jc1.conditionalProbability(0)-(probabilityavg*CATEGORIES.length-jc1.conditionalProbability(0))/(CATEGORIES.length-1);
			//ranks[i] +=probabilitySD*getCategoryConditionalProbability(CATEGORIES[i],jc1);
			ranks[i] +=probabilitySD * getCategoryConditionalProbability(CATEGORIES[i],jc1);
		}

		return ranks;
	}

	public  static double getEntropy(double[] P ){
		double entropy = 0;
		for (double p : P)				
			if (1/(p*Math.log(1/p)) != 0)
				entropy += -p*Math.log(p);
		double psum=0;
		for (double p : P)
			psum+=p;
		System.err.println("Sum of probs: "+psum + "[ ");
		for (double p : P)
			System.err.printf("%1.2f,",p);
		System.err.println("Entropy: "+entropy);

		return entropy;
	}

	/*
	private static JointClassification jointJointClassify(ssd.Document doc){
		double[] jointprobs = new double[CATEGORIES.length]; 
		for(double d : jointprobs)
			d=0;
		JointClassification jc = new JointClassification(CATEGORIES,jointprobs);

		for (String pos : PartsOfSpeech){
			if (doc.mPosTexts.get(pos) != null){
				JointClassification jc1 = compiledClassifiers.get(pos).classify(doc.mPosTexts.get(pos));
				jc = addJointClassificationProbabilities(jc1,jc,((double)doc.mPosTexts.get(pos).length()/(double)doc.mText.length));
			}
		}

		return jc;
	}
	 */
	private static JointClassification addJointClassificationProbabilities( JointClassification jc1, JointClassification jc, double posOccurenceProbability){

		double[] jointprobs = new double[CATEGORIES.length]; 
		for(double d : jointprobs)
			d=2;

		for(int i=0;i<CATEGORIES.length;i++){
			jointprobs[i] += getCategoryProbability(CATEGORIES[i],jc);
			jointprobs[i] += getCategoryProbability(CATEGORIES[i],jc1);			
		}
		for(int i=0;i<CATEGORIES.length;i++){
			System.err.println(CATEGORIES[i] +":"+ jointprobs[i]);		
		}
		System.err.println("_---------------------------_");
		jointprobs = sort_with_categories(jointprobs);
		for(int i=0;i<CATEGORIES.length;i++){
			System.err.println(CATEGORIES[i] +":"+ jointprobs[i]);		
		}
		JointClassification returnjc = new JointClassification(CATEGORIES,jointprobs);
		return returnjc;
	}

	private static double getCategoryProbability(String category, JointClassification jc){
		for(int i=0;i<jc.size();i++){
			if (jc.category(i).equals(category))
				return jc.jointLog2Probability(i);				
		}
		System.err.println("ERROR: unknown category in classifier. Bad Internal Error");
		return -99;
	}

	private static double getCategoryConditionalProbability(String category, JointClassification jc){
		for(int i=0;i<jc.size();i++){
			if (jc.category(i).equals(category))
				return jc.conditionalProbability(i);				
		}
		System.err.println("ERROR: unknown category in classifier. Bad Internal Error");
		return -99;
	}

	public static double[] sort_with_categories(double[] array){
		String[] newcategories = CATEGORIES.clone();
		for (int i = 1; i < array.length; i++){
			int j = i;
			double B = array[i];
			String B_str = newcategories[i];
			while ((j > 0) && (array[j-1] < B)){
				array[j] = array[j-1];
				newcategories[j]=newcategories[j-1];
				j--;
			}
			array[j] = B;
			newcategories[j] = B_str;
		}
		CATEGORIES=newcategories;
		return array;

	}

	static LMClassifier loadModel(String model) throws ClassNotFoundException,IOException{
		System.out.println("-----Loading Model File <" + model+">-----");
		FileInputStream fileIn = new FileInputStream(model);
		ObjectInputStream objIn = new ObjectInputStream(fileIn);
		LMClassifier loadedModelClassifier= (LMClassifier) objIn.readObject();
		Streams.closeInputStream(objIn);
		return loadedModelClassifier;
	}

	static void saveTrainingModel(DynamicLMClassifier mClassifier, String filename, String pos) throws IOException{
		System.out.println("-----Saving Model("+pos+") to <" + filename+">-----");
		FileOutputStream fileOut = new FileOutputStream(filename);
		ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
		mClassifier.compileTo(objOut);
		objOut.close();
	}


}
