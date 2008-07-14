package ssd;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.*;
import java.util.*;

import java.io.File;
import com.aliasi.util.Files;

public class NLPInfoWidget extends QWidget{
    QWidget nlpTab;
    TextProcessor tp;
    
    public Signal1<Integer> updated = new Signal1<Integer>();

    public NLPInfoWidget(QWidget nlpTab, TextProcessor tp){
	this.nlpTab=nlpTab;
	this.tp=tp;
    }

    
    public void updateNLPData(QTreeWidgetItem item, int col){
	//System.out.println("Data passed by signal: " + item.data(2,0));
	Iterator it = tp.docSet.iterator();
	while(it.hasNext()){
	    Document d = (Document) it.next();
	    if ((new Integer(d.id)).equals((Integer)item.data(2,0))){

		//Display the Subjectivity and Polarity of this document.
		//System.out.println("Checked successfully against document id: " + d.id);
		tp.ui.label_12.setText("Subjectivity : " + d.subjectivity);
		tp.ui.label_14.setText("Polarity : " + d.polarity);


		
		//Display the Named Entities
		tp.ui.listWidget.clear();tp.ui.listWidget_2.clear();tp.ui.listWidget_3.clear();

		for(int i=0;i<d.entities.size();i++){
		    if (d.entities.get(i)!=null){
			if (d.entities.get(i).type.equals("PERSON"))
			    tp.ui.listWidget.addItem(d.entities.get(i).occurences.get(0).toString());
			if (d.entities.get(i).type.equals("ORGANIZATION"))
			    tp.ui.listWidget_2.addItem(d.entities.get(i).occurences.get(0).toString());			
			if (d.entities.get(i).type.equals("LOCATION"))
			    tp.ui.listWidget_3.addItem(d.entities.get(i).occurences.get(0).toString());			
		    }

		}		

		//Display Similar and Dissimilar documents based on cluster threshold
	    }
	}
		
	updated.emit(1);	
    }


}