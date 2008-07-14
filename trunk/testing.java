import java.awt.*;
import java.util.*;
public class testing{
    public static void main(String args[]){	
	ArrayList closedlist = new ArrayList();
	for (int i=0;i<10;i++)
	    closedlist.add(new Point((int)(10*Math.random()),(int)(10*Math.random())));
	closedlist.add(new Point(0,3));
	System.out.println(closedlist.contains(new Point(0,3)));
	pt = new Point(4,3);
	pt2 = new Point(4,3);
	System.out.println(pt.equals(pt2));
    }
}