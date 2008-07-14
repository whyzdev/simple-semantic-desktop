package ssd;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.*;
import java.util.*;

import java.io.File;
import com.aliasi.util.Files;

public class BrowserWidget extends QTextBrowser{
    QTextBrowser previewBrowser;
    TextProcessor tp;
    
    public Signal1<Integer> updated = new Signal1<Integer>();

    public BrowserWidget(QTextBrowser previewBrowser, TextProcessor tp){
	this.previewBrowser=previewBrowser;
	this.tp=tp;
    }

    
    public void changeURL(QTreeWidgetItem it, int col){
	System.out.println("Passed qurl: " + it.data(1,0));
	previewBrowser.setSource(new QUrl((String)it.data(1,0)));
	updated.emit(1);	
    }



}