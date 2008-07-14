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

public class TrainModel {

	private static File TRAINING_DIR; 


	private static File TESTING_DIR ;
	private static File MODELS_DIR ;
	private static File TESTING_RESULTS_DIR ;

	private static String[] CATEGORIES; 
	private static String[] PartsOfSpeech; 

	private static int NGRAM_SIZE ;
	private static HashMap<String,LMClassifier> compiledClassifiers;
	private static HashMap<String,DynamicLMClassifier> classifiers ;
	
	private static DynamicLMClassifier standardClassifier;
	
	
	public static void main(String[] args)	
	throws ClassNotFoundException, IOException {
		if (args.length != 4){
			System.out.println("No Arguments. No Model Trainer.");
			return;
		}
		TRAINING_DIR=new File(args[0]);
		TESTING_DIR=new File(args[1]);
		TESTING_RESULTS_DIR = new File(args[2]);
		MODELS_DIR=new File(args[3]);

		/*
		 * 
		 * 
		 * Possible improvement: add 'everything' as a POS, so that baseline results are 
		 * taken along with POS results
		 * - nice-if improvement: compression classifier.
		 * 
		 * 
		 */
		CATEGORIES  = TRAINING_DIR.list();
		NGRAM_SIZE =CATEGORIES.length;
		/*CATEGORIES = new String[categoryfiles.length];
    	for(int i=0;i<categoryfiles.length;i++){
    		CATEGORIES[i]=categoryfiles[i].getName();
    	}*/

		classifiers = new HashMap<String,DynamicLMClassifier>();

		// 1. PREPARE DOCSET
		ArrayList<Set<ssd.Document>> docSet = new ArrayList<Set<ssd.Document>>();
		for(int i=0; i<CATEGORIES.length; ++i) {				
			File classDir = new File(TRAINING_DIR,CATEGORIES[i]);
			if (!classDir.isDirectory()) {
				System.out.println("Could not find training directory=" 
						+ classDir);
				System.out.println("Should you question how you got here?");
			}

			String[] trainingFiles = classDir.list();
			HashSet<Document> docs = new HashSet<Document>();
			for (int j = 0; j < trainingFiles.length; ++j) {
				File file = new File(classDir,trainingFiles[j]);
				Document doc = new Document(file);
				docs.add(doc);
			}
			docSet.add(i,docs);
			POSFinder posfinder = new POSFinder(docs);
			posfinder.process();
		}


		PartsOfSpeech = getPartsOfSpeech(docSet);
		//PartsOfSpeech = new String[]{"all"};
		for (int i=0;i<PartsOfSpeech.length;i++){
			DynamicLMClassifier classifier 
			= DynamicLMClassifier.createNGramProcess(CATEGORIES,NGRAM_SIZE);
			classifiers.put(PartsOfSpeech[i], classifier);
		}





		//compiling
		System.out.println("Compiling");
		compiledClassifiers = new HashMap<String,LMClassifier>();
		
		if (!MODELS_DIR.exists())
			MODELS_DIR.mkdir();		
		for(File f: MODELS_DIR.listFiles())
			f.delete();
		
		
		


		for(int h=0;h<PartsOfSpeech.length;h++){
			for(int i=0; i<CATEGORIES.length; ++i) {
				Iterator<Document> it = docSet.get(i).iterator();
				while(it.hasNext()){
					Document d = it.next();					
					//String text = Files.readFromFile(file);
					System.out.println("Training on " + CATEGORIES[i] + "/" + d.mFile.getName());					
					DynamicLMClassifier classifier = classifiers.get(PartsOfSpeech[h]);
					if (d.mPosTexts.get(PartsOfSpeech[h]) != null) 
						classifier.train(CATEGORIES[i],d.mPosTexts.get(PartsOfSpeech[h]));					
				}				
			}
			saveTrainingModel(classifiers.get(PartsOfSpeech[h]), new File(MODELS_DIR,PartsOfSpeech[h]+".model").getAbsolutePath(), PartsOfSpeech[h]);
		}
		
		for(int i=0; i<CATEGORIES.length; ++i) {
			Iterator<Document> it = docSet.get(i).iterator();
			while(it.hasNext()){
				Document d = it.next();					
				System.out.println("Training on " + CATEGORIES[i] + "/" + d.mFile.getName());					
				standardClassifier 
				= DynamicLMClassifier.createNGramProcess(CATEGORIES,NGRAM_SIZE);				 
				standardClassifier.train(CATEGORIES[i] , d.mText_str);
					
			}				
		}
		saveTrainingModel(standardClassifier, new File(MODELS_DIR,"standard.model").getAbsolutePath(), "standard");

	}

	private static String[] getPartsOfSpeech(ArrayList<Set<ssd.Document>> docSet){
		HashSet<String> pos = new HashSet<String>();

		for(int i=0; i<CATEGORIES.length; ++i) {							
			Set<Document> docs = docSet.get(i);
			Iterator<Document> it = docs.iterator();
			while(it.hasNext()){
				Document d = it.next();
				pos.addAll(d.mPosTexts.keySet());
			}			
		}
		String[] s = new String[pos.size()];
		return pos.toArray(s);
	}

	private static JointClassification jointJointClassify(ssd.Document doc){
		double[] jointprobs = new double[CATEGORIES.length]; 
		for(double d : jointprobs)
			d=0;
		double[] ranks = new double[CATEGORIES.length];
		for(int i=0;i<CATEGORIES.length;i++)
			ranks[i]=0;

		JointClassification jc = new JointClassification(CATEGORIES,jointprobs);

		for (String pos : PartsOfSpeech){
			if (doc.mPosTexts.get(pos) != null){
				JointClassification jc1 = compiledClassifiers.get(pos).classify(doc.mPosTexts.get(pos));
				ranks=updateRanks(jc1,ranks);
//				jc = addJointClassificationProbabilities(jc1,jc,((double)doc.mPosTexts.get(pos).length()/(double)doc.mText.length));
			}
		}


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
		
		double entropy = getEntropy(probSet);

		for(int i=0;i<CATEGORIES.length;i++){
			//if (jc1.bestCategory().equals(CATEGORIES[i]))
//			ranks[i] += jc1.conditionalProbability(0)-(probabilityavg*CATEGORIES.length-jc1.conditionalProbability(0))/(CATEGORIES.length-1);
			//ranks[i] +=probabilitySD*getCategoryConditionalProbability(CATEGORIES[i],jc1);
			ranks[i] +=entropy * getCategoryConditionalProbability(CATEGORIES[i],jc1);
		}

		return ranks;
	}
	
	public  static double getEntropy(double[] P ){
		double entropy = 0;
		for (double p : P)
			entropy += p*Math.log(p)/Math.log(2);
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
