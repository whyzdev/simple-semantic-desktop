/*
 * Important: this class is not being used, currently
 * 
 */package ssd;
import java.util.*;
import java.io.*;

public class CompressionClusterer{
	Set<Document> docSet;
	static SysCommandExecutor bash;
	double[][] distances;
	long[][] differences;
	long s_differences=0;	
	
	public static void main(String args[]){
		if (args.length == 2){
			System.out.println("need to write test routine for compressionclusterer");
		}
	
	}
	
	
	public CompressionClusterer(Set<Document> docSet){
		//constructor
		this.docSet=docSet;
		bash = new SysCommandExecutor();
	}
	
	public double[][] getDistances(){
		
		differences = new long[docSet.size()][docSet.size()];
		int i=0,j=0;
		
		Iterator<Document> it = docSet.iterator();
		while(it.hasNext()){			
			Document a=it.next();
			Iterator<Document> jt = docSet.iterator();		
			while(jt.hasNext()){
				Document b=jt.next();
				differences[a.id][b.id]=getCompressionDifference(a,b);
				s_differences += differences[a.id][b.id];
			}		
		}
		for(i=0;i<docSet.size();i++){
			for(j=0;j<docSet.size();j++){
				distances[i][j]=(differences[i][j]*docSet.size())/s_differences;
			}
		}
		
		return distances;						
	}
	
	private long getCompressionDifference(Document a, Document b){
		long a_size=compressed_size(new Document[]{a});
		long b_size=compressed_size(new Document[]{b});
		long t_size = (compressed_size(new Document[]{a,b}) + compressed_size(new Document[]{b,a}))/2 ; // to make the measure more symmetric
			//(!!!)This is the main implementation of the measure compressibility=sz(a)+sz(b) - sz(a,b)
		double d = 1 - ((a_size+b_size - t_size)/(a_size+b_size)); 
		return 0;		
	}
		
	private long compressed_size(Document[] ar){
		
		long size=-1;
		String command = "./paqnowrite -2 tmp_archive ";
		//write to disk
		
		try {
		//File tempTextFile=new File("tmp_text");
		//FileWriter fw=new FileWriter(tempTextFile);

//		for(int i=0;i<ar.length;i++){			
			//fw.write(ar[i].mText_str + "\n");		
		//}
		
		for(int i=0;i<ar.length;i++){			
			command += ar[i].mFile.getAbsolutePath() + " ";		
		}
		
//		fw.close();		
			
		}
		catch(Exception e){
			System.err.println("Error writing temp file for compression clusterer");
		}
		
		try{
		//needs correction
		//bash.runCommand("./paqnowrite -2 tmp_archive tmp_text");
			System.err.println("PAQ commandline: " + command);
		bash.runCommand(command);
			
		System.out.println(bash.getCommandOutput());
		File tempArchiveFile=new File("tmp_archive");
		size=tempArchiveFile.length();
		}
		catch(Exception e){
			System.err.println("Error running PAQ");
		}
		
		return size;
		
		

	}
	
	
	
}

class CompressorThread implements Runnable{
	Document[] ar;
	long size;
	
	public CompressorThread(Document[] ar){
		this.ar=ar;
	}
	public void run(){
		
			size=-1;
			String command = "./paqnowrite -2 tmp_archive ";
			//write to disk
			
			try {
			//File tempTextFile=new File("tmp_text");
			//FileWriter fw=new FileWriter(tempTextFile);

//			for(int i=0;i<ar.length;i++){			
				//fw.write(ar[i].mText_str + "\n");		
			//}
			
			for(int i=0;i<ar.length;i++){			
				command += ar[i].mFile.getAbsolutePath() + " ";		
			}
			
//			fw.close();		
				
			}
			catch(Exception e){
				System.err.println("Error writing temp file for compression clusterer");
			}
			
			try{
			//needs correction
			//bash.runCommand("./paqnowrite -2 tmp_archive tmp_text");
				System.err.println("PAQ commandline: " + command);
			//bash.runCommand(command);
				
//			System.out.println(bash.getCommandOutput());
			File tempArchiveFile=new File("tmp_archive");
			size=tempArchiveFile.length();
			}
			catch(Exception e){
				System.err.println("Error running PAQ");
			}					
}
}
