package ssd;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class DocTreeWidget extends QTreeWidget{
    QTreeWidget treeWidget;
    TextProcessor tp;
    
    public Signal1<Integer> valueChanged = new Signal1<Integer>();

    public DocTreeWidget(QTreeWidget treeWidget, TextProcessor tp){
	this.treeWidget=treeWidget;
	this.tp=tp;
    }


    public DocTreeWidget(QTreeWidget treeWidget){
	this.treeWidget=treeWidget;
	this.tp=tp;
    }
    
    public void updateBins(){
	    // trying to add an item to treewidget.
	    treeWidget.clear();
	    QTreeWidgetItem __item1 = new QTreeWidgetItem(treeWidget);
	    __item1.setText(0, com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "New Item!"));
	    valueChanged.emit(0);

	    //loop through all clusters in the TextProcessor and make appropriate bins.
	    
    }
    
}