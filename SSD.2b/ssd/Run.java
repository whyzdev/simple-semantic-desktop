package ssd;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

/*
  Run.java : testing class for the GUI of SSD
- Akshat Singhal 2/27/08
 */
    public class Run{
    public static void main(String args[]){

            QApplication.initialize(args);
	    QWidget widget = new QWidget();
	    QMainWindow mw = new QMainWindow();
	    Ui_MainWindow ui = new Ui_MainWindow();	    
	    ui.setupUi(mw);	    
	    mw.show();
	    //	    populateUi(ui);

	    // HOW TO MAKE 'ACCEPT' button update the tree.
	    DocTreeWidget d = new DocTreeWidget(ui.docTree);	    
	    ui.acceptButton.clicked.connect(d, "updateBins()");

	    /*

            QPushButton hello = new QPushButton("Hello World!");
            hello.resize(120, 40);
            hello.setWindowTitle("Hello World");
            hello.show();
	    */

            QApplication.exec();
    }

	public static void populateUi(Ui_MainWindow ui){
	    // trying to add an item to treewidget.
	    ui.docTree.clear();
	    QTreeWidgetItem __item1 = new QTreeWidgetItem(ui.docTree);
	    __item1.setText(0, com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "New Item!"));

	}



}