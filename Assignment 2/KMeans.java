import java.util.*;
import java.lang.Math.*;

public class KMeans extends ClusteringAlgorithm
{
	// Number of clusters
	private int k;

	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;
	
	// Array of k clusters, class cluster is used for easy bookkeeping
	private Cluster[] clusters;
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and memberlists with the ID's (which are Integer objects) of the datapoints that are member of that cluster.
	// You also want to remember the previous members so you can check if the clusters are stable.
	static class Cluster
	{
		float[] prototype;

		Set<Integer> currentMembers;
		Set<Integer> previousMembers;
		  
		public Cluster()
		{
			currentMembers = new HashSet<Integer>();
			previousMembers = new HashSet<Integer>();
		}
	}
	// These vectors contains the feature vectors you need; the feature vectors are float arrays.
	// Remember that you have to cast them first, since vectors return objects.
	private Vector<float[]> trainData;
	private Vector<float[]> testData;

	// Results of test()
	private double hitrate;
	private double accuracy;

    /// Length of the floats
    int floatLength;
	
	public KMeans(int k, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.k = k;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;
		prefetchThreshold = 0.5;
        
        int floatLength = trainData.firstElement().length;
		
		// Here k new cluster are initialized
		clusters = new Cluster[k];
		for (int ic = 0; ic < k; ic++)
			clusters[ic] = new Cluster();
	}


	public boolean train()
	{
	 	//implement k-means algorithm here:
		// Step 1: Select an initial random partioning with k clusters
        Random random = new Random();        

        /// Select the partitioning at random.
        for (int i = 0; i < k; i++) {
            clusters[i].currentMembers.add(random.nextInt(trainData.capacity()));

            for (int j = 0; j < i; j++) {
                if (clusters[i] == clusters[j]) {
                    i--; /// Number not unique, retry.
                    break;
                }
            }
        }

        /// Calculate all prototypes.
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].prototype = calculatePrototype(clusters[i]);
        }

		// Step 2: Generate a new partition by assigning each datapoint to its closest cluster center
        for (int i = 0; i < trainData.size(); i++) {
            double smallestDistance = 
                    calculateDistance(clusters[0].prototype, trainData.get(i));
            int clusterIndex = 0;

            for (int j = 1; j < k; j++) {
                double distance = 
                        calculateDistance(clusters[j].prototype, 
                                          trainData.get(i));

                if (distance < smallestDistance) {
                    smallestDistance = distance;
                    clusterIndex = j;
                }
            }

            clusters[clusterIndex].currentMembers.add(i);
        }

        int numberOfTransfers = 0;
        do {
            /// Recalculate the prototypes.
            for (int i = 0; i < k; i++) {
                clusters[i].prototype = calculatePrototype(clusters[i]);
            }

            for (int i = 0; i < trainData.size(); i++) {
                double smallestDistance = 
                        calculateDistance(clusters[0].prototype, trainData.get(i));
                int clusterIndex = 0;

                for (int j = 1; j < k; j++) {
                    double distance = 
                            calculateDistance(clusters[j].prototype, 
                                              trainData.get(i));

                    if (distance < smallestDistance) {
                        smallestDistance = distance;
                        clusterIndex = j;
                    }
                }

                if (!clusters[clusterIndex].currentMembers.contains(i)) {
                    numberOfTransfers++;

                    clusters[clusterIndex].currentMembers.add(i);

                    // Remove it from the other cluster.
                    removeFromOldCluster(clusterIndex, i);
                }
            }
        /// A rather random number for now.
        } while (numberOfTransfers < trainData.size() * 0.001);

		return false;
	}

	public boolean test()
	{
		// iterate along all clients. Assumption: the same clients are in the same order as in the testData
		// for each client find the cluster of which it is a member
		// get the actual testData (the vector) of this client
		// iterate along all dimensions
		// and count prefetched htmls
		// count number of hits
		// count number of requests
		// set the global variables hitrate and accuracy to their appropriate value
		return true;
	}

    public float[] calculatePrototype(Cluster cluster) {
        float[] prototype = new float[floatLength];

        /// Make sure that the prototype is filled with zero's.
        Arrays.fill(prototype, 0);

        /// Add all values to the prototype.
        for (Integer memberId : cluster.currentMembers) {
            float[] member = trainData.get(memberId);
            for (int i = 0; i < prototype.length; i++) {
                prototype[i] += member[i];
            }
        }

        for (int i = 0; i < prototype.length; i++) {
            prototype[i] /= cluster.currentMembers.size();
        }

        return prototype;
    }

    /// Calculates Euclidean distance between data vector X and prototype P.
    public double calculateDistance(float[] prototype, float[] pattern) {
        double sum = 0;

        for (int i = 0; i < prototype.length; i++) {
            sum += Math.pow((double) (pattern[i] - prototype[i]), 2);
        }

        return Math.sqrt(sum);
    }

    public void removeFromOldCluster(int currentCluster, int id) {
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i].currentMembers.contains(id)) {
                clusters[i].currentMembers.remove(id);
                clusters[i].previousMembers.add(id);
            }
        }
    }

	// The following members are called by RunClustering, in order to present information to the user
	public void showTest()
	{
		System.out.println("Prefetch threshold=" + this.prefetchThreshold);
		System.out.println("Hitrate: " + this.hitrate);
		System.out.println("Accuracy: " + this.accuracy);
		System.out.println("Hitrate+Accuracy=" + (this.hitrate + this.accuracy));
	}
	
	public void showMembers()
	{
		for (int i = 0; i < k; i++)
			System.out.println("\nMembers cluster["+i+"] :" + clusters[i].currentMembers);
	}
	
	public void showPrototypes()
	{
		for (int ic = 0; ic < k; ic++) {
			System.out.print("\nPrototype cluster["+ic+"] :");
			
			for (int ip = 0; ip < dim; ip++)
				System.out.print(clusters[ic].prototype[ip] + " ");
			
			System.out.println();
		 }
	}

	// With this function you can set the prefetch threshold.
	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
}
