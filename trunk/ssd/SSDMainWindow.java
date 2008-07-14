package ssd;
import java.util.*;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;
public class SSDMainWindow extends QMainWindow {
	Ui_MainWindow ui;
	public SSDMainWindow(Ui_MainWindow ui){
	this.ui=ui;
	}
	
	protected void resizeEvent(QResizeEvent arg__1){
		//void resizeAction(){
		//DOESN'T WORK
		/*System.err.println("DIAG: Resize action detected");
		List<QObject> helloChildren= this.children();

		QWidget tabWidget_top = (QWidget) this.findChild(QWidget.class,"centralwidget").findChild(QFrame.class,"frame").findChild(QTabWidget.class,"tabWidget_top");
		QWidget tabWidget_bottom = (QWidget) this.findChild(QWidget.class,"centralwidget").findChild(QFrame.class,"frame").findChild(QTabWidget.class,"tabWidget_bottom");
		
		QWidget frame = (QWidget) this.findChild(QWidget.class,"centralwidget").findChild(QFrame.class,"frame");
		QTabWidget tabWidget = (QTabWidget) this.findChild(QWidget.class,"centralwidget").findChild(QFrame.class,"frame").findChild(QTabWidget.class,"tabWidget_top");
		QWidget tableWidget = tabWidget.widget(2);
		
		//.findChild(QTabWidget.class,"tab_data").findChild(QTabWidget.class,"tableWidget");
*/		
		ui.tableWidget.setGeometry(new QRect(0, 30, this.width()-210, 281));
				
		ui.frame.setGeometry(new QRect(0, 0, this.width()-6, 811));
		
		
		ui.tabWidget_top.setGeometry(new QRect(180 , 0, this.width() - 200, 341));
		ui.tabWidget_bottom.setGeometry(new QRect(180, 340, this.width() - 200, 441));
	/*	
		Iterator<QObject> it = tabWidget.children().iterator();
		while(it.hasNext()){			
			QObject currentwidgetobject = it.next();
			System.err.println(currentwidgetobject.objectName());
			if(currentwidgetobject.objectName().equals("tabWidget_top")){
				QWidget tabWidget_top=(QWidget) currentwidgetobject;
				tabWidget_top.setGeometry(new QRect(180 , 0, this.width() - 200, 341));
			}
			if(currentwidgetobject.objectName().equals("tabWidget_bottom")){
				QWidget tabWidget_bottom=(QWidget) currentwidgetobject;
				tabWidget_bottom.setGeometry(new QRect(180 , 0, this.width() - 200, 341));
			}
		}		
*/
		
	}
}	
