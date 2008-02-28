package ssd;

import com.aliasi.util.Files;

import com.aliasi.classify.Classification;
import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.classify.JointClassification;

import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TokenizedLM;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;


import java.util.HashSet;
import java.util.Iterator;


public class Subjectivizer {

    File mPolarityDir;
    String[] mCategories;
    DynamicLMClassifier mClassifier;
    LMClassifier loadedModelClassifier;
    static final String SUBJECTIVITY_DATA_DIR="../data/subjectivitydata";

    Subjectivizer() {
        System.out.println("\nBASIC SUBJECTIVITY DEMO");
        mPolarityDir = new File(SUBJECTIVITY_DATA_DIR);
        System.out.println("\nData Directory=" + mPolarityDir);
        mCategories = new String[] { "plot", "quote" };
        int nGram = 8;
        mClassifier = 
            DynamicLMClassifier
            .createNGramProcess(mCategories,nGram);
    }

    void run() throws ClassNotFoundException, IOException {
	loadModel("../data/subjectivitydata/subjectivity.model");
	//        train();
	//        evaluate();
    }

    void loadModel(String model) throws ClassNotFoundException, IOException{
	FileInputStream fileIn = new FileInputStream(model);
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
	loadedModelClassifier= (LMClassifier) objIn.readObject();
        Streams.closeInputStream(objIn);
    }

    void train() throws IOException {
        int numTrainingChars = 0;
        System.out.println("\nTraining.");
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            File file = new File(mPolarityDir,
                                 mCategories[i] + ".tok.gt9.5000");
            String data = Files.readFromFile(file);
            String[] sentences = data.split("\n");
            System.out.println("# Sentences " + category + "=" + sentences.length);
            int numTraining = (sentences.length * 9) / 10;
            for (int j = 0; j < numTraining; ++j) {
                String sentence = sentences[j];
                numTrainingChars += sentence.length();
                mClassifier.train(category,sentence);
            }
        }
        
        System.out.println("\nCompiling.\n  Model file=subjectivity.model");
        FileOutputStream fileOut = new FileOutputStream("../data/subjectivity.model");
        ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
        mClassifier.compileTo(objOut);
        objOut.close();

        System.out.println("  # Training Cases=" + 9000);
        System.out.println("  # Training Chars=" + numTrainingChars);
    }

    void evaluate() throws IOException {
        ClassifierEvaluator evaluator
            = new ClassifierEvaluator(mClassifier, mCategories);
        System.out.println("\nEvaluating.");
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            File file = new File(mPolarityDir,
                                 mCategories[i] + ".tok.gt9.5000");
            String data = Files.readFromFile(file);
            String[] sentences = data.split("\n");
            int numTraining = (sentences.length * 9) / 10;
            for (int j = numTraining; j < sentences.length; ++j) {
                evaluator.addCase(category,sentences[j]);
            }
        }
        System.out.println();
        System.out.println(evaluator.toString());
    }

    public static void main(String[] args) {
        try {
            new Subjectivizer().run();
        } catch (Throwable t) {
            System.out.println("Thrown: " + t);
            t.printStackTrace(System.out);
        }
    }

}

