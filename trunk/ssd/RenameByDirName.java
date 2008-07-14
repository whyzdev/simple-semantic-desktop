package ssd;
import java.io.File;
public class RenameByDirName{
	public static void main(String args[]) throws Exception{
		if (args.length==1){
			File maindir = new File(args[0]);
			File[] maindirlist = maindir.listFiles() ;
			for (int i=0;i<maindirlist.length;i++){
				File currentdir =maindirlist[i];  
				if (currentdir.list() != null){
					File[] filelist = currentdir.listFiles();
					for (int j=0;j<filelist.length;j++){
						File currentfile =filelist[j];
						System.err.print("--"+filelist[j].getCanonicalPath()+"/" +maindirlist[i].getName()+"-"+filelist[j].getName()+":");				
						File newfile = new File(currentdir, maindirlist[i].getName()+"-"+filelist[j].getName());
						System.err.println(currentfile.renameTo(newfile));
						
					}
				}
				
				System.err.println("-"+currentdir.getName() + "(isDirectory?" + (currentdir.list()!= null)+")");
			}

		}
	}

}