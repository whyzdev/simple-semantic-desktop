package ssd;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.*;
import java.util.*;

import java.io.File;
import com.aliasi.util.Files;

public class TextProcessor{
    static final int MIN_CLUSTER_SIZE=8;

    File inDir;
    File outDir;


    Set<Set<Document>> clusters_set;
    ArrayList<DocumentCluster> clusters;
    Set<Document> docSet;


    String menuHTML;
    File entitiesDir;

    public static void main(String args[]) throws Exception{
	TextProcessor tp = new TextProcessor(args[0],args[1]);
	System.out.println(tp.docSet.size());
	tp.process();
	Ui_MainWindow ui = showUi();	
	tp.populateUi(ui);
	
	return;
    }

    public TextProcessor(String inDir, String outDir) throws Exception{
	this.inDir=new File(inDir);
	this.outDir=new File(outDir);	

	docSet = new HashSet<Document>();
	
	for (File file : (new File(outDir + "/temp_phase1")).listFiles()) {
	    Document doc = new Document(file);
	    docSet.add(doc);            
        }

	clusters = new ArrayList<DocumentCluster>();

    }

    public void process() throws Exception{
	NLPClusterer n = new NLPClusterer();
	clusters_set = n.makeClusters(docSet,MIN_CLUSTER_SIZE); //clustering by cosine measure
	
	
	Polarizer polarizer = new Polarizer();
	Subjectivizer subjectivizer = new Subjectivizer();
	SignificanceMeasurizer sm = new SignificanceMeasurizer();

	polarizer.run();
	subjectivizer.run();

	Iterator it = docSet.iterator();
	while(it.hasNext()){
	    Document doc = (Document)it.next();
	    if (polarizer.loadedModelClassifier.classify(doc.mText_str).bestCategory().equals("pos"))
		doc.polarity="Positive";
	    else
		doc.polarity="Negative";

	    if (subjectivizer.loadedModelClassifier.classify(doc.mText_str).bestCategory().equals("plot"))
		doc.subjectivity="Objective";
	    else
		doc.subjectivity="Subjective";
	    
	    
	    System.out.println("Document: " + doc + " Polarity: " + doc.polarity);
	    System.out.println("Document: " + doc + " Subjectivity: " + doc.subjectivity );
	    Iterator it2=doc.sentences.iterator();
	    while(it2.hasNext()){
		doc.calculateSentenceScore((Document.Sentence)it.next());
	    }
	}

	
	EntityFinder entityFinder = new EntityFinder();
	entitiesDir = new File(outDir,"entities");
	entitiesDir.mkdir();
	File docDir = new File(outDir,"docs");
	docDir.mkdir();
	
	Iterator it3 = clusters_set.iterator();
	for(int i=0;it3.hasNext();i++){
	    Set<Document> docs = (Set<Document>) it3.next();
	    DocumentCluster currentcluster = new DocumentCluster(docs,"cluster" + i);
	    sm.process(currentcluster);
	    entityFinder.processCluster(currentcluster);
	    currentcluster.printEntities();
	    currentcluster.printEntitiesHTML(entitiesDir,docDir);
	    clusters.add(currentcluster);	    
	}

	menuHTML =menuHTMLbegin();
	printAnnotatedHTML();
	menuHTML +=menuHTMLend();
	FileWriter menu_fw = new FileWriter(new File(outDir + "/menu.html"));
	menu_fw.write(menuHTML);
	menu_fw.close();
	
    }

    public void printAnnotatedHTML() throws Exception{
	try {
	copy(new File("mootools.js"),new File(outDir + "/mootools.js"));
	copy(new File("webpages/index.html"),new File(outDir + "/index.html"));
	copy(new File("bg.jpg"),new File(outDir + "/bg.jpg"));
	copy(new File("bg1.jpg"),new File(outDir + "/bg1.jpg"));
	}
	catch (Exception e){
	    e.printStackTrace();
	}
	
	Iterator it4 = clusters.iterator();
	for(int i=0;it4.hasNext();i++){
	    DocumentCluster currentcluster =(DocumentCluster)it4.next();
	    currentcluster.dir = new File(outDir,currentcluster.name);
	    currentcluster.dir.mkdir();
	    File docDir = new File(outDir,"docs");
	    File infoDir = new File(outDir,"info");

	    infoDir.mkdir();
	    // Add this cluster to the menu html file.
	    menuHTML = menuHTML +  "<li><a href='#' title='' class='toggler'>" + currentcluster.name  +  "</a></li>\n";
	    menuHTML = menuHTML +  "<div style=\"border-top: medium none; border-bottom: medium none; overflow: hidden; padding-top: 0px; padding-bottom: 0px; visibility: hidden; opacity: 0; height: 0px;\" class=\"accordion\">\n";
	    //<a href="" title="index.php">AJAX programming</a>
						      
	    Iterator it5 = currentcluster.docs.iterator();
	    for(int j=0;it5.hasNext();j++){
		Document currentdoc = (Document)it5.next();	
		currentdoc.underlineBigScorers();
		currentdoc.addSignificanceSizeCSS();
		currentdoc.createInfoHTML(entitiesDir.toString(),currentcluster.name);
		//??? add code for NER links
		Files.writeStringToFile(currentdoc.annotatedHTML.replaceAll("\n","<br>"),new File(docDir,currentdoc.mFile.getName())); 
		Files.writeStringToFile(currentdoc.infoHTML,new File(infoDir,currentdoc.mFile.getName())); 
		String docname = currentdoc.mFile.getName();
		String docpath = "file://"+ docDir + "/" + currentdoc.mFile.getName();
		String docinfopath = "file://"+ infoDir + "/" + currentdoc.mFile.getName();
		menuHTML += "<A HREF = '" + docpath + "' target = 'showframe' onClick='javascript:parent.infoframe.location=\"file://"+ docinfopath+ "  \";'>" + docname + "</A>\n";
		
	    }

	    menuHTML +="</div>";
	}

    }

    String menuHTMLbegin(){
	try {
	return new Scanner(new File("menu_begin_html.html")).useDelimiter("\\A").next();
	}
	catch(Exception e){
	    return "<error opening menu>";
	}
    }

    String menuHTMLend(){
	try {
	return new Scanner(new File("menu_end_html.html")).useDelimiter("\\A").next();
	}
	catch(Exception e){
	    return "<error opening menu>";
	}

    }


    void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
	
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static Ui_MainWindow showUi(){
	String[] s = {""};
	QApplication.initialize(s);
	    QWidget widget = new QWidget();
	    QMainWindow mw = new QMainWindow();
	    Ui_MainWindow ui = new Ui_MainWindow();
	    ui.setupUi(mw);
	    mw.show();
	    /*

            QPushButton hello = new QPushButton("Hello World!");
            hello.resize(120, 40);
            hello.setWindowTitle("Hello World");
            hello.show();
	    */

            QApplication.exec();
	    return ui;
    }

    public void populateUi(Ui_MainWindow ui){
	/*	ui.treeWidget_SuggestedBins.*/
	/*
	populateDocumentBins(ui);
	populateGearboxThresholds(ui);
	populateClusteringData(ui);
	//populateClusteristan(ui);
	//populateSummaries(ui);
	populateNLPData(ui);
	*/

    }
        
}