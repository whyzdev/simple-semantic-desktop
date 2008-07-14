////////
////////
/////////
////////
///////// (NOW DEPRECATED!)

package ssd;


import java.util.Iterator;
import java.util.Set;

import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.util.Distance;

public class CosineClusterer { // taken from Lingpipe cluster example

    static final int N_SLICES=100;
    static  boolean DIAG1=true;
    static final boolean DIAG2=true;
    Set<Document> docSet;

    public static void main(String[] args) throws Exception {
	//        File dir = new File(args[0]);
	//	DIAG1=true;
	//	NLPClusterer n= new NLPClusterer();
	//	n.makeClusters(dir,3);
	
    }

    public Set<Set<Document>> makeClusters(Set<Document> docSet, int minClusterSize) throws Exception{		
	
	// This was used when I was trying to find the distance between two "similar" documents, and setting the threshold to at least that.
	//	Document a_doc=new Document(new File("a_doc.txt"));
	//	Document similar_doc=new Document(new File("similar_doc.txt"));

	this.docSet=docSet;

        HierarchicalClusterer<Document> clClusterer
            = new CompleteLinkClusterer<Document>(COSINE_DISTANCE);
        Dendrogram<Document> completeLinkDendrogram
            = clClusterer.hierarchicalCluster(docSet);

        HierarchicalClusterer<Document> slClusterer
            = new SingleLinkClusterer<Document>(COSINE_DISTANCE);
        Dendrogram<Document> singleLinkDendrogram
            = slClusterer.hierarchicalCluster(docSet);

	//	System.out.println(COSINE_DISTANCE.distance(a_doc,similar_doc));
	//	System.out.println(singleLinkDendrogram.partitionDistance(singleLinkDendrogram.score()-COSINE_DISTANCE.distance(a_doc,similar_doc)));
	//	System.out.println(completeLinkDendrogram.partitionDistance(COSINE_DISTANCE.distance(a_doc,similar_doc)));
	if (DIAG1)
	    System.out.println(completeLinkDendrogram.partitionDistance(findBestThreshold(completeLinkDendrogram, minClusterSize)));
	return completeLinkDendrogram.partitionDistance(findBestThreshold(completeLinkDendrogram, minClusterSize));
	
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
	    return 1.0 - doc1.cosine(doc2);
	}
    };

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
		result[i][j]=doci.cosine(docj);
		j++;
	    }
	    i++;
	}
	    
	return result;
	

    }

}
