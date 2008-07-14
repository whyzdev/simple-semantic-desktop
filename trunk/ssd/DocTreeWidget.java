package ssd;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.*;
import java.util.*;

import java.io.File;
import com.aliasi.util.Files;
import com.aliasi.cluster.ClusterScore;

public class DocTreeWidget extends QTreeWidget{
	QTreeWidget treeWidget;
	TextProcessor tp;

	public Signal1<Integer> updated = new Signal1<Integer>();

	public DocTreeWidget(QTreeWidget treeWidget, TextProcessor tp){
		this.treeWidget=treeWidget;
		this.tp=tp;
		//this.setAcceptDrops(true);
		//this.setDragEnabled(true);

	}


	public void updateBins(){
		treeWidget.clear();	
		//loop through all clusters in the TextProcessor and make appropriate bins.
		//tp.clusters=tp.gb.makeClusters(tp.docSet, tp.MIN_CLUSTER_SIZE);
		try{
			tp.responsePartition=tp.gb.reMakeClusters(tp.MIN_CLUSTER_SIZE);
			tp.clusters=tp.convertSetSetToDocumentCluster(tp.responsePartition);
			ClusterScore results=new ClusterScore(tp.referencePartition,tp.responsePartition);
			Files.writeStringToFile(results.toString(), new File("./_cluster_results.txt"));
			System.err.println(results);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		Iterator it4 = tp.clusters.iterator();

		for(int i=0;it4.hasNext();i++){
			DocumentCluster currentcluster =(DocumentCluster)it4.next();
			//1. Add in a parent level node from 'currentcluster.name'
			QTreeWidgetItem clusteritem = new QTreeWidgetItem();
			clusteritem.setText(0,currentcluster.name);
			clusteritem.setToolTip(0, currentcluster.name);
			clusteritem.setData(1,0,currentcluster.url);
			clusteritem.setData(2,0,currentcluster.id);
			clusteritem.setData(3,0,"cluster");
			treeWidget.addTopLevelItem(clusteritem);



			Iterator it5 = currentcluster.docs.iterator();
			for(int j=0;it5.hasNext();j++){
				//1.1. Add child nodes for documents to cluster's node
				Document currentdoc = (Document)it5.next();	
				String docname = currentdoc.mFile.getName() + "(d" + currentdoc.id +")";
				QTreeWidgetItem docitem =new QTreeWidgetItem();
				docitem.setData(1,0,currentdoc.url);
				docitem.setData(2,0,currentdoc.id);
				docitem.setData(3,0,currentdoc);

				docitem.setToolTip(0, docname);
				docitem.setText(0,docname);
				//docitem.setFlags( Qt.ItemFlag.ItemIsDragEnabled);				
				clusteritem.addChild(docitem);
			}

		}
		treeWidget.expandAll();

		System.err.println("Emitting update from Doctree.");
		updated.emit(1);


		/*
	//Toy code: trying to add an item to treewidget.
	treeWidget.clear();
	QTreeWidgetItem __item1 = new QTreeWidgetItem(treeWidget);
	__item1.setText(0, com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "New Item!"));
	__item.setIcon(0, new QIcon(new QPixmap("classpath:doc/html/com/trolltech/qt/images/linguist-fileopen.png")));
		 */	    
	}

	public void writeTrainingDirectory() {

		File outputdir = new File(tp.outDir.getAbsolutePath() +  "/train");
		if (!outputdir.exists())
			outputdir.mkdir();
		System.out.println("Writing Training Directory " + outputdir);

		QTreeWidgetItem rootitem= this.treeWidget.invisibleRootItem();
		/*
		 * //System.out.println(this.children());
		Iterator<QObject> it = this.children().iterator();
		while(it.hasNext()){
			QObject obj = it.next();
			System.out.println(obj.objectName() +":"+ obj.getClass() );
		}		
		System.out.println("n_elements: " + this.treeWidget.topLevelItemCount());
		 */

		System.err.println(rootitem.childCount());
		for (int i=0; i < rootitem.childCount(); i++){
			QTreeWidgetItem currentitem = rootitem.child(i);
			System.err.println(currentitem.text(0));
			ArrayList<Document> docs = getChildren(currentitem);
			System.err.println(docs);
			if (docs.size() > 0) {
				File clusterdir = new File(outputdir.getAbsolutePath() + "/" + currentitem.text(0));
				clusterdir.mkdir();
				Iterator<Document> it2= docs.iterator();
				while(it2.hasNext()){
					Document d = it2.next();
					try {
						copyFile(d.mFile ,new File(clusterdir.getAbsolutePath() + "/" + d.mFile.getName()));
					}
					catch (Exception e){
						System.err.println("ERROR writing training directory");
						e.printStackTrace();
					}				
				}
			}
		}


	}

	private ArrayList<Document>  getChildren(QTreeWidgetItem clusteritem){
		ArrayList<Document> children= new ArrayList<Document>();
		for (int i=0;i<clusteritem.childCount();i++){
			if (clusteritem.child(i).childCount()>0)
				children.addAll(getChildren(clusteritem.child(i)));
			else
				children.add((Document)clusteritem.child(i).data(3,0));
		}
		return children; 
	}
	// Copy file

	public static void copyFile(File fSource, File fDest) throws IOException
	{
		// Declare variables
		InputStream sIn = null;
		OutputStream sOut = null;

		try
		{
			// Declare variables
			int nLen = 0;
			sIn = new FileInputStream(fSource);
			sOut = new FileOutputStream(fDest);

			// Transfer bytes from in to out
			byte[] bBuffer = new byte[1024];
			while ((nLen = sIn.read(bBuffer)) > 0)
			{
				sOut.write(bBuffer, 0, nLen);
			}

			// Flush
			sOut.flush();
		}
		finally
		{
			// Close streams
			try
			{
				if (sIn != null)
					sIn.close();
				if (sOut != null)
					sOut.close();
			}
			catch (IOException eError)
			{
			}
		}
	}	

}

