package ssd;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;

import java.io.*;

import java.io.File;
import java.io.IOException;

public class Polarizer {

    File mPolarityDir;
    String[] mCategories;
    DynamicLMClassifier mClassifier;
    LMClassifier loadedModelClassifier;
    
    static final String POLARITY_DATA_DIRECTORY="../lib/polaritydata";

    Polarizer() throws Exception{
        //System.out.println("\nBASIC POLARITY DEMO");
//        mPolarityDir = new File(POLARITY_DATA_DIRECTORY,"txt_sentoken");
        //System.out.println("\nData Directory=" + mPolarityDir);
    	mCategories = new String[2];
        mCategories[0] = "neg";
        mCategories[1] = "pos";
        
        int nGram = 8;
        mClassifier 
            = DynamicLMClassifier
            .createNGramProcess(mCategories,nGram);
    }

    void run() throws ClassNotFoundException, IOException {
	loadModel("../lib/polaritydata/polarity.model");
	//        train();
	//	saveTrainingModel();
        //evaluate();
    }


    void loadModel(String model) throws ClassNotFoundException,IOException{
	FileInputStream fileIn = new FileInputStream(model);
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
	loadedModelClassifier= (LMClassifier) objIn.readObject();
        Streams.closeInputStream(objIn);
    }

    void saveTrainingModel() throws IOException{
	FileOutputStream fileOut = new FileOutputStream("../lib/polaritydata/polarity.model");
	ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
	mClassifier.compileTo(objOut);
	objOut.close();
    }

    boolean isTrainingFile(File file) {
	//        return file.getName().charAt(2) != '9';  // test on fold 9
	return true;
    }

    void train() throws IOException {
        int numTrainingCases = 0;
        int numTrainingChars = 0;
        System.out.println("\nTraining.");
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            File file = new File(mPolarityDir,mCategories[i]);
            File[] trainFiles = file.listFiles();
            for (int j = 0; j < trainFiles.length; ++j) {
                File trainFile = trainFiles[j];
                if (isTrainingFile(trainFile)) {
                    ++numTrainingCases;
                    String review = Files.readFromFile(trainFile);
                    numTrainingChars += review.length();
                    mClassifier.train(category,review);
                }
            }
        }
        System.out.println("  # Training Cases=" + numTrainingCases);
        System.out.println("  # Training Chars=" + numTrainingChars);
    }

    void evaluate() throws IOException {
        System.out.println("\nEvaluating.");
        int numTests = 0;
        int numCorrect = 0;
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            File file = new File(mPolarityDir,mCategories[i]);
            File[] trainFiles = file.listFiles();
            for (int j = 0; j < trainFiles.length; ++j) {
                File trainFile = trainFiles[j];
                if (!isTrainingFile(trainFile)) {
                    String review = Files.readFromFile(trainFile);
                    ++numTests;
                    Classification classification
                        = mClassifier.classify(review);
                    if (classification.bestCategory().equals(category))
                        ++numCorrect;
                }
            }
        }
        System.out.println("  # Test Cases=" + numTests);
        System.out.println("  # Correct=" + numCorrect);
        System.out.println("  % Correct=" 
                           + ((double)numCorrect)/(double)numTests);
    }

    public static void main(String[] args) {
        try {
            new Polarizer().run();
        } catch (Throwable t) {
            System.out.println("Thrown: " + t);
            t.printStackTrace(System.out);
        }
    }

}

