package ssd;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.*;
import java.util.*;

import java.io.File;
import com.aliasi.util.Files;

public class TextProcessor{
	static final int MIN_CLUSTER_SIZE=8;
	static final int N_CLUSTERINGS=2;
	static final int WIN32=1;
	static final int LINUX=0;
	static final int PLATFORM=LINUX;

	File inDir;
	File outDir;
	File refDir;
	String resultFileName;
	
	
	static SysCommandExecutor bash;
	Ui_MainWindow ui;
	Set<Set<Document>> referencePartition;
	Set<Set<Document>> responsePartition;
	
	GearboxClusterer gb;

	Set<Set<Document>> clusters_set;
	ArrayList<DocumentCluster> clusters;
	Set<Document> docSet;


	String menuHTML;
	File entitiesDir;


	DataTabWidget dataTab;
	DocTreeWidget docTree;
	BrowserWidget previewBrowser;

	double[] distance_variances;

	public static void main(String args[]) throws Exception{
		bash = new SysCommandExecutor();				
		// Sample command on bash: 	
		//bash.runCommand("ls");
		//System.out.println(bash.getCommandOutput());

		String[] s = {""};
		QApplication.initialize(s);
		QWidget widget = new QWidget();		
		Ui_MainWindow ui = new Ui_MainWindow();
		SSDMainWindow mw = new SSDMainWindow(ui);
		ui.setupUi(mw);
		mw.show();

		TextProcessor tp;
		if (args.length == 1){
			//Create a new TextProcessor if data is already given.
			
			tp = new TextProcessor(args[0]+"/test",args[0]+"/test_out", args[0]+"/reference_partition");	
			System.out.println("Input Document Set size: " + tp.docSet.size());
			tp.process();
			//Connect it to UI
			tp.connectUi(ui);

		}	
		else {
			//create a blank TP if no data given.
			tp = new TextProcessor();
			//Note: In this case, the program will be expecting the user to load the input and output directories using the File Menu.
			System.err.println("Arguments required: java ssd.TextProcessor inDir outDir");
			System.exit(-1);

		}

		tp.setSliders();
		ui.acceptButton.click();
		QApplication.exec();	


		return;
	}

	public void connectUi(Ui_MainWindow ui){
		/* Connect slots and signals */	
		this.ui=ui;
		//Connect Document Tree to Apply button and TP
		docTree = new DocTreeWidget(ui.docTree, this);
		ui.acceptButton.clicked.connect(docTree, "updateBins()");
		ui.actionExport_Bins.triggered.connect(docTree, "writeTrainingDirectory()");

		//Connect preview browser to Document Tree
		previewBrowser = new BrowserWidget(ui.previewBrowser,this);
		ui.docTree.itemClicked.connect(previewBrowser, "changeURL(QTreeWidgetItem, int)");

		//Connect the Data Viewer to TP and to the Document Tree
		dataTab = new DataTabWidget(ui.tab_data,this);
		docTree.updated.connect(dataTab, "updateData(int)");	
		ui.comboBox.currentStringChanged.connect(dataTab,"updateData()");



		//Connect NLP Info Tab with Document Tree
		NLPInfoWidget nlpInfoViewer = new NLPInfoWidget(ui.tab_NLP, this);
		ui.docTree.itemClicked.connect(nlpInfoViewer, "updateNLPData(QTreeWidgetItem, int)");
		


		// Update bins
		docTree.updateBins();

	}

	public TextProcessor(String inDir, String outDir, String refDir) throws Exception{
		distance_variances = new double[5];
		this.inDir=new File(inDir);
		this.outDir=new File(outDir);	
		this.refDir=new File(refDir);
		
		docSet = new HashSet<Document>();

		if (PLATFORM==WIN32)
			bash.runCommand("run.bat " + inDir + " " + outDir);
		else
			bash.runCommand("./run.sh " + inDir + " " + outDir);
		
		
		referencePartition = new HashSet<Set<Document>>();
		int i=0;
		
		for (File file: (new File(outDir + "/temp_phase1")).listFiles()) {
			if (file.isFile()){
				Document doc = new Document(file);
				doc.id=i++;
				docSet.add(doc);		
			}
		}
		
		for (File subdir : this.refDir.listFiles()) {
			if (subdir.isDirectory()){
				Set<Document> refSet = new HashSet<Document>();
				for (File file: subdir.listFiles()){
					if (file.isFile()){
						Document doc = getFileFromDocSet(docSet,file.getName());
						refSet.add(doc);
					}							
				}
				referencePartition.add(refSet);
			}
		}
		
		
		

		clusters = new ArrayList<DocumentCluster>();
	}

public static Document getFileFromDocSet(Set<Document> ds,String filename){
	Iterator<Document> it = ds.iterator(); 
	while (it.hasNext()){
		Document d =it.next();
		if (d.mFile.getName().equals(filename))
			return d;
	}
	System.err.println("Document in Reference Set does not exist in Testing set : " + filename);
	return null;	
}
	public TextProcessor() throws Exception{

		docSet = new HashSet<Document>();

		clusters = new ArrayList<DocumentCluster>();
	}

	public void process() throws Exception{
		{
		EntityFinder entityFinder = new EntityFinder();
		DocumentCluster allDocuments = new DocumentCluster(docSet,"allDocuments");
		entityFinder.processCluster(allDocuments,this);
		entityFinder=null;
		POSFinder posFinder = new POSFinder(docSet,this);
		posFinder.process();
		posFinder=null;
		}
		clusters = cluster_all();
		


		//Polarizer polarizer = new Polarizer();
		//Subjectivizer subjectivizer = new Subjectivizer();
		//SignificanceMeasurizer sm = new SignificanceMeasurizer();

		//polarizer.run();
		//subjectivizer.run();
/*
		Iterator<Document> it = docSet.iterator();
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

			//Iterator<Document.Sentence> it2=doc.sentences.iterator();
			//while(it2.hasNext()){
			//	doc.calculateSentenceScore((Document.Sentence)it.next());
			//}
		}
*/

		//EntityFinder entityFinder = new EntityFinder();
		//entitiesDir = new File(outDir,"entities");
		//entitiesDir.mkdir();
		File docDir = new File(outDir,"docs");
		docDir.mkdir();

		Iterator it3 = clusters_set.iterator();
		for(int i=0;it3.hasNext();i++){
			Set<Document> docs = (Set<Document>) it3.next();
			DocumentCluster currentcluster = new DocumentCluster(docs,"cluster" + i);
			//sm.process(currentcluster);
			//entityFinder.processCluster(currentcluster);
			//currentcluster.printEntities();
			//currentcluster.printEntitiesHTML(entitiesDir,docDir);
			//currentcluster.url = "file://";
			clusters.add(currentcluster);	    
		}

		//menuHTML =menuHTMLbegin();
		//printAnnotatedHTML();
		//menuHTML +=menuHTMLend();
		//FileWriter menu_fw = new FileWriter(new File(outDir + "/menu.html"));
		//menu_fw.write(menuHTML);
		//menu_fw.close();

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

	private ArrayList<DocumentCluster> cluster_all(){		
		gb = new GearboxClusterer(docSet,this);
		try{	
			clusters_set = gb.makeClusters(docSet,MIN_CLUSTER_SIZE); //clustering by combination of measures			
		}
		catch(Exception e){
			System.err.println("ERROR: Caught exception while clustering: \n");
			e.printStackTrace();
		}

		return convertSetSetToDocumentCluster(clusters_set);
	}

	public ArrayList<DocumentCluster> convertSetSetToDocumentCluster(Set<Set<Document>> clusters_set){
		ArrayList<DocumentCluster> return_clusterlist = new ArrayList<DocumentCluster>(); 
		Iterator it3 = clusters_set.iterator();
		for(int i=0;it3.hasNext();i++){
			Set<Document> docs = (Set<Document>) it3.next();
			for (Document d : docs)
				d.cluster_id=i;
			DocumentCluster currentcluster = new DocumentCluster(docs,"cluster" + i);				
			return_clusterlist.add(currentcluster);	    
		}
		return return_clusterlist ;
	}

	public Document getDocument(int docid){
		for (Document d: docSet){
			if (d.id==docid)
				return d;
		}
		return docSet.iterator().next();		
	}



	public void setSliders(){	
		System.err.println(distance_variances[0] + "," + distance_variances[1] + "," +distance_variances[2] );
		int[] slidervals=new int[5];
		double max=0,min=1;
		for(double d:distance_variances)
			if (d>max)
				max=d;

		for(double d:distance_variances)
			if (d<min)
				min=d;

		for (int i=0;i<5;i++){
			//slidervals[i]=(int)(4 + 95.0*((distance_variances[i]-min)/(max-min)));
			slidervals[i]=(int)(Math.random()*99);
		}

		this.ui.verticalSlider.setValue( slidervals[0] );
		this.ui.verticalSlider_2.setValue( slidervals[1] );
		this.ui.verticalSlider_3.setValue( slidervals[2] );
		this.ui.verticalSlider_4.setValue( slidervals[3] );
		this.ui.verticalSlider_5.setValue( slidervals[4] );	
	}

}