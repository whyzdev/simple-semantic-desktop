
package ssd;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.*;
import java.util.*;

import java.io.File;
import com.aliasi.util.Files;

public class DataTabWidget extends QTabWidget{
    QWidget dataTab;
    TextProcessor tp;
    QColor[] c ;

    public DataTabWidget(QWidget dataTab, TextProcessor tp){
	this.dataTab=dataTab;
	this.tp=tp;
	c = new QColor[10];
	
	for (int i=0;i<10;i++){
		c[i]=new QColor((int)(255*Math.random()),(int)(255*Math.random()),(int)(255*Math.random()));
	}
    }


    public void updateData(){    	
    	/*c[0] = new QColor(255,204,51);
    	c[1] = new QColor(51,102,255);
    	c[2] = new QColor(51,255,102);
    	c[3] = new QColor(204,153,255);
    	c[4] = new QColor(46,138,46);
    	c[5] = new QColor(204,153,102);
    	c[6] = new QColor(156,79,156);
    	c[7] = new QColor(255,82,82);
    	c[8] = new QColor(152,102,128);
    	c[9] = new QColor(152,102,102);*/	
    	
    	
    	
    	System.err.println("updating Data Tab");
	String chosenOption = tp.ui.comboBox.currentText();	
	double[][] distances = new double[tp.docSet.size()][tp.docSet.size()];

	/* SET COLUMN AND ROW HEADERS */
	tp.ui.tableWidget.clear();		
	tp.ui.tableWidget.setColumnCount(tp.docSet.size());
	tp.ui.tableWidget.setRowCount(tp.docSet.size());
	tp.ui.tableWidget.clear();	
	Iterator<Document> it = tp.docSet.iterator();	
	
	int i=0;
	while(it.hasNext()){
	    Document doci = (Document)it.next();
	    QColor clustercolor = c[tp.getDocument(i).cluster_id%10];
	    
	    //create column item
		QTableWidgetItem __colItem = new QTableWidgetItem();
		__colItem.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "d" + i));
		__colItem.setBackground(new QBrush(new QColor(clustercolor)));
		__colItem.setForeground(new QBrush(invertColor(__colItem.background().color())));
	    
		tp.ui.tableWidget.setHorizontalHeaderItem(i, __colItem);
		
		//create row item
		QTableWidgetItem __rowItem = new QTableWidgetItem();
		__rowItem.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "d" + i));
		__rowItem.setBackground(new QBrush(new QColor(clustercolor)));
		__rowItem.setForeground(new QBrush(invertColor(__colItem.background().color())));
		tp.ui.tableWidget.setVerticalHeaderItem(i, __rowItem);
		i++;
	}
	
	
	if (chosenOption.equals(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Compression Distance"))){
	    distances = tp.gb.compression_distances;
	    
	}
	else if(chosenOption.equals(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Sectioned Distance"))){
	    distances = tp.gb.section_cosine_distances;
	    		
	}
	else if(chosenOption.equals(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Frequent Term Distance"))){
	    ;
	}
	else if(chosenOption.equals(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "PartsOfSpeech Distance"))){
	    distances = tp.gb.pos_distances;
	    

	}
	else if(chosenOption.equals(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Named Entity Distance"))){
	    distances = tp.gb.ner_distances;
	    
	}
	else if(chosenOption.equals(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Total Weighted Distance"))){
		distances = tp.gb.gearbox_distances;
	    
	}
	else if(chosenOption.equals(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Cosine Distance"))){
	    distances = tp.gb.cosine_distances;
	    
	}
	
	
	
    for ( i=0;i<tp.docSet.size();i++)
		for(int j=0;j<tp.docSet.size();j++){
		    QTableWidgetItem __item = new QTableWidgetItem();
		    __item.setText(Double.toString(distances[i][j]));
		    //QColor clustercolor = avgRGB(c[i],c[j]);
		    QColor clustercolor = avgRGB(c[tp.getDocument(i).cluster_id%10],c[tp.getDocument(j).cluster_id%10]);
		    //__item.setBackground(new QBrush(c));
		    __item.setBackground(new QBrush(new QColor(clustercolor).lighter(100+(int)( 100*(1-distances[i][j])))));
		    __item.setForeground(new QBrush(invertColor(__item.background().color())));
//		    __item.setBackground(new QBrush(new QColor(c.red() + (int)((255.0-c.red())*distances[i][j]),c.green() + (int)((255.0-c.green())*distances[i][j]),c.blue() + (int)((255.0-c.blue())*distances[i][j])) ));
		    tp.ui.tableWidget.setItem(i, j, __item);
		    
		}		
	
    }
    
    public QColor avgRGB(QColor q1, QColor q2 ){
    	
    	QColor ret = new QColor(0,0,0);  
    	/*
    	ret.setRed((q1.red()+q2.red())/2);
    	ret.setBlue((q1.blue()+q2.blue())/2);
    	ret.setGreen((q1.green()+q2.green())/2);
    	*/
    	ret.setHsv((q1.hue()+q2.hue())/2, (q1.saturation()+q2.saturation())/2, (q1.value()+q2.value())/2);
    	
    	return ret;
    }
    
    public QColor invertColor(QColor q){
    	return new QColor(255-q.red(),255-q.blue(),255-q.green());
    }

    public void updateData(int k){
    	System.err.println("Intercepted update from Doctree with value " + k);
	updateData();
    }

    



}

