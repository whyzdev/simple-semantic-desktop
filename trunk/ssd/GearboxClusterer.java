package ssd;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.*;
import java.io.File;

import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.util.Distance;
import com.aliasi.util.Files;

public class GearboxClusterer {
	/**
	 *	GearboxClusterer is the class that implements the 5 algorithms for    
	 *  finding clustering distance. They're implemented as subclasses of the type
	 *  Distance<Document> (POS_DISTANCE, NER_DISTANCE, etc.) GEARBOX_DISTANCE is the class
	 *  that combines distances from all of these classes.
	 *  
	 *  
	 * @param  url  an absolute URL giving the base location of the image
	 * @param  name the location of the image, relative to the url argument
	 * @return      the image at the specified URL
	 * @see         Image
	 */
	
	//store user-chosen thresholds for each clusteirng

	//store distances from each clustering

	//store name of each clustering type (???)


	/**
	 * 
	 */
	HierarchicalClusterer<Document> clClusterer;

	static final int N_SLICES=100;
	static  boolean DIAG1=false;
	static final boolean DIAG2=false;
	static final int N_SECTIONS=20;
	Set<Document> docSet;
	static public double[][] cosine_distances;
	static public double[][] ner_distances;
	static public double[][] compression_distances;
	static public double[][] section_cosine_distances;
	static public double[][] pos_distances;
	static public double[][] gearbox_distances;
	static public double[][][] section_cosines;
	static public double[] section_variances;
	static public double[][][] pos_cosines;
	static public double[] pos_variances;
	static public String[] PartsOfSpeech;
	static private long[] doc_sizes;
	static private long wordlist_csize;
	static private File wordlist; 
	double currentThreshold;
	static Dendrogram<Document> completeLinkDendrogram; 
	static TextProcessor tp;
	static public HierarchicalClusterer<Document> slClusterer;
	
	public double precision;
	public double recall;
	public double f_measure;
	
	public GearboxClusterer(Set<Document> docSet, TextProcessor tp){
		this.docSet= docSet;

		this.tp=tp;
		ner_distances= new double[docSet.size()][docSet.size()];
		cosine_distances= new double[docSet.size()][docSet.size()];
		section_cosine_distances= new double[docSet.size()][docSet.size()];
		compression_distances= new double[docSet.size()][docSet.size()];
		pos_distances= new double[docSet.size()][docSet.size()];
		gearbox_distances= new double[docSet.size()][docSet.size()];
		doc_sizes = new long[docSet.size()];
		section_cosines = new double[N_SECTIONS][docSet.size()][docSet.size()];
		section_variances = new double[N_SECTIONS];


		for(int i=0;i<docSet.size();i++){
			doc_sizes[i]=0;
			for(int j=0;j<docSet.size();j++)
				pos_distances[i][j]=section_cosine_distances[i][j]=ner_distances[i][j]=cosine_distances[i][j]=compression_distances[i][j]=gearbox_distances[i][j]=0;
		}

		for(int h=0;h<N_SECTIONS;h++){
			Iterator<Document> it= docSet.iterator();
			while(it.hasNext()){
				Document di=it.next();
				Iterator<Document> jt= docSet.iterator();
				while(jt.hasNext()){
					Document dj=jt.next();
					section_cosines[h][dj.id][di.id]=section_cosines[h][di.id][dj.id]= 1- Document.cosine(getSection(di.mText_str,h,N_SECTIONS),getSection(dj.mText_str,h,N_SECTIONS));
				}
			}

			double section_average =0;			
			for(double[] ar : section_cosines[h])
				for(double n : ar)
					section_average +=n;			
			section_average =section_average /(docSet.size() * docSet.size());


			section_variances[h]=0;
			for(double[] ar : section_cosines[h])
				for(double n : ar)
					section_variances[h] +=Math.pow(n-section_average,2);
			section_variances[h] =section_variances[h] /(docSet.size() * docSet.size());
		}

		double vsum=0;
		for(double v : section_variances)
			vsum+=v;

		for(int i=0;i<section_variances.length;i++)
			section_variances[i]=section_variances[i]/vsum;

		writeArray(section_variances,"./section_variances.csv");

		for (Document a: docSet)
			for (Document b: docSet)
				if (a.id != b.id)
					cosine_distances[b.id][a.id]=cosine_distances[a.id][b.id]=1.0 - a.cosine(b);
				else 
					cosine_distances[a.id][b.id]=0;
		HashSet<String> pos_set=new HashSet<String>();

		for (Document d : docSet)		
			pos_set.addAll(d.mPosTexts.keySet());
		PartsOfSpeech = new String[pos_set.size()];
		PartsOfSpeech = pos_set.toArray(PartsOfSpeech);	
		writeArray(PartsOfSpeech,"./partsofspeech.txt");

		pos_cosines = new double[PartsOfSpeech.length][docSet.size()][docSet.size()];
		pos_variances = new double[PartsOfSpeech.length];

		for (int i=0; i < PartsOfSpeech.length;i++){
			for (Document a: docSet)
				for (Document b: docSet){
					String atext,btext; 
					if ((btext=b.mPosTexts.get(PartsOfSpeech[i])) != null && (atext=a.mPosTexts.get(PartsOfSpeech[i]))!= null)
						pos_cosines[i][b.id][a.id]=pos_cosines[i][a.id][b.id]= 1.0 - Document.cosine(atext,btext);
					else
						pos_cosines[i][b.id][a.id]=pos_cosines[i][a.id][b.id]=cosine_distances[a.id][b.id];
				}

			double avg =0;			
			for(double[] ar : pos_cosines[i])
				for(double n : ar)
					avg +=n;			
			avg =avg /(docSet.size() * docSet.size());

			pos_variances[i]=0;
			for(double[] ar : pos_cosines[i])
				for(double n : ar)
					pos_variances[i] +=Math.pow(n-avg,2);
			pos_variances[i] =pos_variances[i] /(docSet.size() * docSet.size());
		}

		double vsum2=0;
		for(double v : pos_variances)
			vsum2+=v;

		for(int i=0;i<pos_variances.length;i++)
			pos_variances[i]=pos_variances[i]/vsum2;
		writeArray(pos_variances,"./pos_variances.csv");
	}

	public static void main(String[] args) throws Exception {
		// should implement testing function

	}

	public static double variance(double[][] matrix){
		double avg=average(matrix);
		double variance=0;
		for(double[] ar : matrix)
			for(double n : ar)
				variance +=Math.pow(n-avg,2);
		variance = variance /(matrix.length * matrix[0].length);
		return variance;
	}

	public static double average(double[][] matrix){
		double avg =0;			
		for(double[] ar : matrix)
			for(double n : ar)
				avg +=n;			
		avg =avg /(matrix.length * matrix[0].length);
		return avg;
	}

	public Set<Set<Document>> makeClusters(Set<Document> docSet, int minClusterSize) throws Exception{		

		// This was used when I was trying to find the distance between two "similar" documents, and setting the threshold to at least that.
		//	Document a_doc=new Document(new File("a_doc.txt"));
		//	Document similar_doc=new Document(new File("similar_doc.txt"));

		this.docSet=docSet;
		//reCalculateDistances();
		//slClusterer = new SingleLinkClusterer<Document>(GEARBOX_DISTANCE);
		clClusterer = new CompleteLinkClusterer<Document>(GEARBOX_DISTANCE);

		completeLinkDendrogram =clClusterer.hierarchicalCluster(docSet);

		//HierarchicalClusterer<Document> slClusterer = new SingleLinkClusterer<Document>(COSINE_DISTANCE);
		//Dendrogram<Document> singleLinkDendrogram = slClusterer.hierarchicalCluster(docSet);

		//	System.out.println(COSINE_DISTANCE.distance(a_doc,similar_doc));
		//	System.out.println(singleLinkDendrogram.partitionDistance(singleLinkDendrogram.score()-COSINE_DISTANCE.distance(a_doc,similar_doc)));
		//	System.out.println(completeLinkDendrogram.partitionDistance(COSINE_DISTANCE.distance(a_doc,similar_doc)));

		if (DIAG1)
			System.out.println(completeLinkDendrogram.partitionDistance(findBestThreshold(completeLinkDendrogram, minClusterSize)));
		//currentThreshold=findBestThreshold(completeLinkDendrogram, minClusterSize);


		return completeLinkDendrogram.partitionK(getK_optimalScatter(1));


	}

	/*
	void updateThresholdSlider(){
		this.tp.ui.thresholdSlider.setValue((int) (99.0 * currentThreshold));				
	}

	double getThresholdSlider(){
		System.err.println("dendrogram score is " + completeLinkDendrogram.score() + ", selected Threshold: " + ((double)this.tp.ui.thresholdSlider.value()* completeLinkDendrogram.score())/99.0);
		return ((double)this.tp.ui.thresholdSlider.value()* maxDistance())/99.0;

	}*/

	/*public static void reCalculateDistances(){
		Iterator<Document> it1 = tp.docSet.iterator();
		Iterator<Document> it2 = tp.docSet.iterator();
		while(it1.hasNext()){
			Document doc1= it1.next();
			while(it2.hasNext()){
				Document doc2= it2.next();			
				if (cosine_distances[doc1.id][doc2.id] ==0 )
					cosine_distances[doc2.id][doc1.id]=cosine_distances[doc1.id][doc2.id]= COSINE_DISTANCE.distance(doc1, doc2) ;
				if (compression_distances[doc1.id][doc2.id] ==0)
					compression_distances[doc2.id][doc1.id]=compression_distances[doc1.id][doc2.id]=COMPRESSION_DISTANCE.distance(doc1,doc2);
				if (ner_distances[doc1.id][doc2.id] ==0)
					ner_distances[doc2.id][doc1.id]=ner_distances[doc1.id][doc2.id]= NER_DISTANCE.distance(doc1, doc2) ;
			}
		}
		//cosine_distances=rescaleMatrix(cosine_distances);
		//compression_distances=rescaleMatrix(compression_distances);
		//ner_distances=rescaleMatrix(ner_distances);
	}*/

	static double[][] rescaleMatrix(double[][] M){
		double min = M[0][0], max=M[0][0];
		for(int i=0;i<M.length;i++){
			for(int j=0;j<i;j++){
				//	System.err.print(M[i][j]+",");
				if ( M[i][j] < min )
					min=M[i][j];
				if ( M[i][j] > max)
					max=M[i][j];
			}
			//System.err.print("\n");
		}
		//System.err.println("Rescaling: found (max: "+max+"min:"+min+")\n");
		for(int i=0;i<M.length;i++){
			for(int j=0;j<M.length;j++){
				//System.err.print(M[i][j]+":");
				M[i][j] = (M[i][j]-min) / (max-min) ;
				//System.err.print(M[i][j]+"\n");
			}
			//System.err.print("\n");			
			//M[i][i]=0;
		}
		return M;		
	}

	public Set<Set<Document>> reMakeClusters(int minClusterSize ) throws Exception{
		//System.err.println("reMakeClusters was called");
		cosine_distances=rescaleMatrix(cosine_distances);
		compression_distances=rescaleMatrix(compression_distances);
		ner_distances=rescaleMatrix(ner_distances);
		section_cosine_distances=rescaleMatrix(section_cosine_distances);
		pos_distances=rescaleMatrix(pos_distances);

		clClusterer = new CompleteLinkClusterer<Document>(GEARBOX_DISTANCE);
		//slClusterer = new SingleLinkClusterer<Document>(GEARBOX_DISTANCE);
		//System.err.println("Max Distance for cluster: "+clClusterer.getMaximumValue());

		Dendrogram<Document> completeLinkDendrogram =clClusterer.hierarchicalCluster(docSet);
		//completeLinkDendrogram.
		System.err.println(completeLinkDendrogram.prettyPrint());
		writeDistanceCSVs();
		Files.writeStringToFile(completeLinkDendrogram.prettyPrint(), new File("./_dendrogram.txt"));
		//System.out.println("Within cluster scatter:");

	//	tp.distance_variances[0]=variance(compression_distances);		
	//	tp.distance_variances[1]=variance(ner_distances);
	//	tp.distance_variances[2]=variance(cosine_distances);
	//.distance_variances[3]=variance(section_cosine_distances);
	//	tp.distance_variances[4]=variance(pos_distances);
		
		return correctSingletonClusters(completeLinkDendrogram.partitionK(getK_optimalScatter(Integer.parseInt(tp.ui.npartitions.toPlainText()))));
		//return correctSingletonClusters(completeLinkDendrogram.partitionK(Integer.parseInt(tp.ui.npartitions.toPlainText())));
		//return completeLinkDendrogram.partitionDistance( Double.parseDouble(tp.ui.npartitions.toPlainText()) );
	}
	
	

	public int getK_optimalScatter(int leastK){
		double[] scatter = new double[docSet.size()];
		double[] percentages = new double[docSet.size()];

		for(int i=1;i<docSet.size()*(.7);i++){
			System.out.println(""+i+":"+completeLinkDendrogram.withinClusterScatter(i,GEARBOX_DISTANCE));
			scatter[i]=completeLinkDendrogram.withinClusterScatter(i,GEARBOX_DISTANCE);			
		}

		int maxIndex=leastK-1;
		for(int i=leastK-1;i<(docSet.size()*.7)-1;i++){
			percentages[i] = (scatter[i] - scatter[i+1])/scatter[i];
			if (percentages[i] > percentages[maxIndex])
				maxIndex=i;
		}

		System.out.println("best K:" + (maxIndex +1));
		return maxIndex+1;

	}

	public Set<Set<Document>> combineClusters(int n_clusters_desired, Set<Set<Document>> s){
		Set<Set<Document>> returnset = new HashSet<Set<Document>>();

		return s;

	}

	public static double maxDistance(){
		double max_distance=0;
		for(int i=0;i<tp.docSet.size();i++)
			for(int j=0;j<tp.docSet.size();j++)
				if (gearbox_distances[i][j]>max_distance)
					max_distance=gearbox_distances[i][j];
		return max_distance;

	}



	public static double findBestThreshold(Dendrogram<Document> d, int SmallestPermissibleClusterSize)
	{
		//CODE FOR TRYING OUT DIFFERENT VALUES OF PARTITION THRESHOLD AND SEEING SIZES OF INDIVIDUAL CLUSTERS	
		double proximity_score=d.score();	
		double[][] result = new double[N_SLICES][4]; // threshold_vs_avg_vs_sd_vs_number of 1-sized clusters

		for (int i=0;i<N_SLICES;i++){
			double threshold=(double)i*(proximity_score/N_SLICES);
			Set<Set<Document>> partitions = d.partitionDistance(threshold);

			if (DIAG1)
				System.out.printf( "threshold=%f, gives %d partitions of sizes:\n", threshold, partitions.size() );
			int[] sizes= new int[partitions.size()];
			int ctr=0;
			int sizetotal =0;
			result[i][3]=0;
			Iterator it = partitions.iterator();
			while(it.hasNext()){
				Set<Document> s =(Set<Document>) it.next();
				if (DIAG1)
					System.out.print(s.size() + ", ");
				sizes[ctr++]=s.size();
				sizetotal +=s.size();
				if (s.size() < SmallestPermissibleClusterSize)
					result[i][3]++;
			}
			double size_average = sizetotal/partitions.size();
			double standard_dev=0;
			for(int j=0;j<sizes.length;j++){
				standard_dev +=Math.sqrt(Math.pow(sizes[j]-size_average,2));

			}
			if (DIAG1)
				System.out.printf("\n Average cluster size: %.1f\n",size_average );
			if (DIAG1)
				System.out.printf("\n Cluster size S.D.:  %.1f\n",standard_dev );
			result[i][0]=threshold;
			result[i][1]=size_average;
			result[i][2]=standard_dev;
		}

		double min_sd = result[0][2];
		int min_sd_index=0;
		for( int i=1;i<result.length;i++){
			if (result[i][2] < min_sd){
				min_sd_index=i;
				min_sd=result[i][2];
			}
		}

		double min_avg_distance = Math.abs(result[0][1]-7.0);
		int min_avg_distance_index=0;
		for( int i=1;i<result.length;i++){
			if (Math.abs(result[i][1]-7.0) < min_avg_distance){
				min_avg_distance_index=i;
				min_avg_distance=Math.abs(result[i][1]-7.0);
			}
		}

		double min_violators = result[0][3];
		int min_violators_index=0;
		for( int i=1;i<result.length;i++){
			if (result[i][3] < min_violators){
				min_violators_index=i;
				min_violators=result[i][3];
			}
		}
		if (DIAG2)
			System.out.println("Threshold with the minimum number of clusters below size " + SmallestPermissibleClusterSize+ " : "+ result[min_violators_index][0]);
		return result[min_violators_index][0];

	}


	static Distance<Document> COSINE_DISTANCE
	= new Distance<Document>() {
		public double distance(Document doc1, Document doc2) {
			return cosine_distances[doc1.id][doc2.id];
		}
	};


	static Distance<Document> NER_DISTANCE
	= new Distance<Document>() {
		public double distance(Document doc1, Document doc2) {
			return 1.0 - Document.cosine(doc1.mEntityText,doc2.mEntityText);
		}
	};

	static Distance<Document> SECTION_COSINE_DISTANCE
	= new Distance<Document>() {
		public double distance(Document doc1, Document doc2) {
			double cosinesum=0;
			for(int i=0;i<N_SECTIONS;i++){
				cosinesum += section_cosines[i][doc1.id][doc2.id] * section_variances[i];
			}
			return (cosinesum/(1.0*N_SECTIONS) + cosine_distances[doc1.id][doc2.id])/2.0;
		}
	};

	static String getSection(String s, int sectionid, int nsections){
		return s.substring( (int) (((double)s.length()*sectionid)/(double)nsections),(int)(  ((double)s.length()*(sectionid+1))/(double)nsections));
	}


	static Distance<Document> POS_DISTANCE
	= new Distance<Document>() {
		public double distance(Document doc1, Document doc2) {
			double cosinesum=0;
			int tagcounter=1;

			for (int i=0;i<PartsOfSpeech.length;i++){
				cosinesum+=pos_cosines[i][doc1.id][doc2.id]*pos_variances[i];
			}

			return cosinesum;
		}
	};


	Distance<Document> GEARBOX_DISTANCE
	= new Distance<Document>() {
		public double distance(Document doc1, Document doc2) {

			if (section_cosine_distances[doc1.id][doc2.id] ==0 )
				section_cosine_distances[doc2.id][doc1.id]=section_cosine_distances[doc1.id][doc2.id]= SECTION_COSINE_DISTANCE.distance(doc1, doc2) ;
			if (cosine_distances[doc1.id][doc2.id] ==0 )
				cosine_distances[doc2.id][doc1.id]=cosine_distances[doc1.id][doc2.id]= COSINE_DISTANCE.distance(doc1, doc2) ;
			if (compression_distances[doc1.id][doc2.id] ==0)
				compression_distances[doc2.id][doc1.id]=compression_distances[doc1.id][doc2.id]=COMPRESSION_DISTANCE.distance(doc1,doc2);
			if (ner_distances[doc1.id][doc2.id] ==0)
				ner_distances[doc2.id][doc1.id]=ner_distances[doc1.id][doc2.id]= NER_DISTANCE.distance(doc1, doc2) ;
			if (pos_distances[doc1.id][doc2.id] ==0)
				pos_distances[doc2.id][doc1.id]=pos_distances[doc1.id][doc2.id]= POS_DISTANCE.distance(doc1, doc2) ;


			gearbox_distances[doc2.id][doc1.id]=gearbox_distances[doc1.id][doc2.id]=mergeDistancesByInput(cosine_distances[doc1.id][doc2.id],compression_distances[doc1.id][doc2.id],ner_distances[doc1.id][doc2.id],section_cosine_distances[doc1.id][doc2.id], pos_distances[doc1.id][doc2.id]);
//gearbox_distances[doc2.id][doc1.id]=gearbox_distances[doc1.id][doc2.id]=mergeMaxMin(cosine_distances[doc1.id][doc2.id],compression_distances[doc1.id][doc2.id],ner_distances[doc1.id][doc2.id],section_cosine_distances[doc1.id][doc2.id], pos_distances[doc1.id][doc2.id]);
			
			return gearbox_distances[doc1.id][doc2.id];
		}
	};
	static double compressionRatio(Document doc){
		return 1.0*doc_sizes[doc.id]/(1.0*doc.mFile.length());
	}

	static Distance<Document> COMPRESSION_DISTANCE
	= new Distance<Document>() {
		public double distance(Document doc1, Document doc2) {
			//return 1;
			if (wordlist==null)
			wordlist = new File("./basic_wordlist.txt");
			long a_size,b_size,t_size;
			if (wordlist_csize == 0)
				wordlist_csize = compressed_size(new File[]{wordlist});
			if (doc_sizes[doc1.id] == 0)
				doc_sizes[doc1.id]=compressed_size(new File[]{wordlist, doc1.mFile}) - wordlist_csize ;	
			if (doc_sizes[doc2.id] == 0)
				doc_sizes[doc2.id]=compressed_size(new File[]{wordlist, doc2.mFile}) - wordlist_csize ;
			//t_size = (compressed_size(new Document[]{doc1,doc2}) + compressed_size(new Document[]{doc2,doc1}))/2 ; // to make the measure more symmetric
			t_size = compressed_size(new File[]{wordlist,doc1.mFile,doc2.mFile}) - wordlist_csize; // once should be enough 
			a_size=doc_sizes[doc1.id];
			b_size=doc_sizes[doc2.id];
			//(!!!)This is the main implementation of the measure compressibility=sz(a)+sz(b) - sz(a,b)

			double dfsq = (double)a_size+b_size - t_size;

			double d = 1 -  (dfsq/Math.min(a_size , b_size) );
			System.out.println("a_size["+ doc1.mFile.getName() +"]:" + a_size + ", b_size["+ doc2.mFile.getName() +"]:" + b_size + ", t_size:" + t_size  + ", distance=" + d );			
			//return (1.0*t_size/(1.0+doc1.mFile.length()+doc1.mFile.length())) *d; // this line added compression ratio to the equation. Not good.
			return d;

		}
	};

	static double mergeDistancesByInput(double cosine_distance, double compression_distance, double ner_distance, double section_cosine_distance, double pos_distance){
		//System.err.println("mergeDistancesByInput called. UI Status: "+ (tp!= null && tp.ui !=null));

		if (tp!= null && tp.ui!=null){
			//System.err.println("Read values from slider for Gearbox.");
			double squaredvalue = Math.pow(((double)tp.ui.verticalSlider_3.value()/99.0) * cosine_distance,2);
			squaredvalue +=Math.pow(((double)tp.ui.verticalSlider.value()/99.0) * compression_distance,2);
			squaredvalue +=Math.pow(((double)tp.ui.verticalSlider_2.value()/99.0) * ner_distance,2) ;
			squaredvalue +=Math.pow(((double)tp.ui.verticalSlider_4.value()/99.0) * section_cosine_distance,2) ;
			squaredvalue +=Math.pow(((double)tp.ui.verticalSlider_5.value()/99.0) * pos_distance,2) ;
			squaredvalue=squaredvalue/5;
			//return squaredvalue;
			return  Math.pow(squaredvalue, .5);
		}
		else 
			return 1.0 * cosine_distance + 1.0 * compression_distance + 1.0 * ner_distance + pos_distance+section_cosine_distance;
	}

	static double mergeMaxMin(double cosine_distance, double compression_distance, double ner_distance, double section_cosine_distance, double pos_distance) {
		double averageDistance = (cosine_distance+ner_distance+compression_distance+pos_distance+section_cosine_distance)/5;
		if (averageDistance > .5)
			return Math.max(cosine_distance,Math.max(ner_distance,Math.max(compression_distance,Math.max(section_cosine_distance,pos_distance))));
		else
			return Math.min(cosine_distance,Math.min(ner_distance,Math.min(compression_distance,Math.min(section_cosine_distance,pos_distance))));
	}



	public double[][] getDistanceMatrix(){
		/* This work could be speeded up significantly */
		double[][] result = new double[docSet.size()][docSet.size()];
		int i=0,j=0;
		Iterator it = docSet.iterator();	

		while(it.hasNext()){
			//System.out.println("i=" + i);
			Document doci = (Document)it.next();
			Iterator jt = docSet.iterator();	
			j=0;
			while(jt.hasNext()){
				//System.out.println("j=" + j);
				Document docj = (Document) jt.next();		
				result[i][j]=GEARBOX_DISTANCE.distance(doci,docj);
				j++;
			}
			i++;
		}

		return result;


	}
	
	private static long compressed_size(File[] ar){

		long size=-1;

		while(size <= 0){
			SysCommandExecutor bash = new SysCommandExecutor();

			try {
				String command ;
				if (TextProcessor.PLATFORM==TextProcessor.WIN32)
					command = "paqnowrite.exe a tmp_archive -1 ";
				else
					command = "./paqnowrite a tmp_archive -1 ";
				
				for(int i=0;i<ar.length;i++){			
					command += ar[i].getAbsolutePath() + " ";		
				}
				if (DIAG1)
					System.err.println("PAQ commandline: " + command);
				bash.runCommand(command);


				String paqoutput = bash.getCommandOutput();
				if (DIAG1)
					System.err.println(paqoutput);
				Scanner sc = new Scanner(paqoutput);
				size=0;
				while(sc.hasNext()){
					String cur_line=sc.nextLine();
					size += Long.parseLong(cur_line);
				}			
			}
			catch(Exception e){
				e.printStackTrace();
				System.err.println("Error writing temp file for compression clusterer");
			}
		}
		return size;
	}

	

	public static void writeDistanceCSVs(){
		/*		static public double[][] cosine_distances;
		static public double[][] ner_distances;
		static public double[][] compression_distances;
		static public double[][] section_cosine_distances;
		static public double[][] pos_distances;
		static public double[][] gearbox_distances;*/

		writeArray(gearbox_distances, "./gearbox_distances.csv");
		writeArray(ner_distances, "./ner_distances.csv");
		writeArray(pos_distances, "./pos_distances.csv");
		writeArray(compression_distances, "./compression_distances.csv");
		writeArray(cosine_distances, "./cosine_distances.csv");
		writeArray(section_cosine_distances, "./section_cosine_distances.csv");


	}


	public static void writeArray(double[][] distances, String filename){
		String s;
		s = "0";

		for(int i=1;i<tp.docSet.size();i++)
			s+= ","+i;
		s +="\n";

		for(int j=0;j<tp.docSet.size();j++){
			s += "" + j + "," +distances[0][j]; 
			for(int i=1;i<tp.docSet.size();i++){
				s += "," + distances[i][j]; 
			}
			s += "\n";
		}
		//System.out.println(filename + ":");
		//System.out.println(s);
		try{
			Files.writeStringToFile(s, new File(filename));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	public static void writeArray(double[] distances, String filename){
		String s;
		s = "0";

		for(int i=1;i<tp.docSet.size();i++)
			s+= ","+i;
		s +="\n";

		for(int j=0;j<distances.length;j++){
			s += "" + distances[j];
			s += "\n";
		}
		try{
			Files.writeStringToFile(s, new File(filename));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void writeArray(String[] distances, String filename){
		String s;
		s = "0";

		for(int i=1;i<tp.docSet.size();i++)
			s+= ","+i;
		s +="\n";

		for(int j=0;j<distances.length;j++){
			s += "" + distances[j];
			s += "\n";
		}
		try{
			Files.writeStringToFile(s, new File(filename));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static Set<Set<Document>> correctSingletonClusters(Set<Set<Document>> s){		
		return s;							
		/*
		ArrayList<Set<Document>> docsetarray = new ArrayList<Set<Document>>();				
		docsetarray.addAll(s);



		for(int i=0;i<docsetarray.size();i++){
			Set<Document> currentcluster = docsetarray.get(i);
			if (currentcluster.size()==1){
				Document singleton_document=(Document)currentcluster.toArray()[0];
				double[] avgDistances = new double[s.size()];
				avgDistances[0]=1;
				int minIndex=0;
				//calculate average distance from all clusters.
				for(int j=0;j<docsetarray.size();j++){
					if (i!=j){
						Set<Document> compare_to_cluster = docsetarray.get(j);
						double averageDistance=0;
						int docCount=0;
						Iterator<Document> it_2= compare_to_cluster.iterator();
						while(it_2.hasNext()){
							Document compare_to_document = it_2.next();
							if (gearbox_distances[compare_to_document.id][singleton_document.id] < gearbox_distances[compare_to_document.id][singleton_document.id])
							docCount++;
						}
						averageDistance = averageDistance/docCount;
						avgDistances[j]=averageDistance;
						if (avgDistances[j] < avgDistances[0]){							
							minIndex=j;
						}
					}
				}
				System.out.println("Document " + singleton_document.name  +  " should be moved to cluster: " + docsetarray.get(minIndex));
				Set<Document> bestset = new Set<Document>((docsetarray.get(minIndex));
				bestset.add(singleton_document);
			}
		}

		s.clear();
		for(int i=0;i<docsetarray.size();i++)
			s.add(docsetarray.get(i));
		return s;*/
	}
}

