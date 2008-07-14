package ssd;
import com.trolltech.qt.gui.QApplication;
import java.io.*;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QTreeWidgetItem;
import com.trolltech.qt.gui.QWidget;

/*
  Run.java : testing class for the GUI of SSD
- Akshat Singhal 2/27/08
 */
    public class Run{
    public static void main(String args[]) throws Exception{
    	fixwebkb();
		String p ="testingttt.__.np" ;
		String posname = p.substring(p.indexOf(".__.")+4,p.length());
		String docname = p.substring(0,p.indexOf(".__."));
		System.out.println(docname+"|"+posname);
        QApplication.initialize(args);
	    
	    Ui_MainWindow ui = new Ui_MainWindow();
	    SSDMainWindow mw = new SSDMainWindow(ui);
	    ui.setupUi(mw);	          	    
	    populateUi(ui);
	    mw.show();
	    
	    // HOW TO MAKE 'ACCEPT' button update the tree.


	    //test


	    /*

            QPushButton hello = new QPushButton("Hello World!");
            hello.resize(120, 40);
            hello.setWindowTitle("Hello World");
            hello.show();
	    */
	    //ui.previewBrowser.setSource(new QUrl("file:///local/s/tinycorpus_out/temp_phase1/japan.html"));
	    
            QApplication.exec();
    }

	public static void populateUi(Ui_MainWindow ui){
	    // trying to add an item to treewidget.
	    ui.docTree.clear();
	    QTreeWidgetItem __item1 = new QTreeWidgetItem(ui.docTree);
	    __item1.setText(0, com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "New Item!"));

	}
	public static void fixwebkb() throws Exception{
		File f = new File("/local/s/datasets/webkb");
		File n = new File("/local/s/datasets/webkb_new");
		n.mkdir();
		for (File categoryf : f.listFiles()){
			for (File univf : categoryf.listFiles()){

			    //File univdir = new File (n,univf.getName());
				//if (!univdir.exists())
				//univdir.mkdir();
				File categorydir = new File (n,categoryf.getName());
				if (!categorydir.exists())
					categorydir.mkdir();

				for (File f2 : univf.listFiles())
				    if (Math.random()>.75)
					DocTreeWidget.copyFile(f2, new File(categorydir + "/" + f2.getName()));
			}				
		}
	}



}