import java.util.*;

//import KMeans.Cluster;

public class Kohonen extends ClusteringAlgorithm
{
	// Size of clustersmap
	private int n;

	// Number of epochs
	private int epochs;
	
	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;

	private double initialLearningRate; 
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and a memberlist with the ID's (Integer objects) of the datapoints that are member of that cluster.  
	private Cluster[][] clusters;

	// Vector which contains the train/test data
	private Vector<float[]> trainData;
	private Vector<float[]> testData;
	
	// Results of test()
	private double hitrate;
	private double accuracy;
	
	static class Cluster
	{
			float[] prototype;

			Set<Integer> currentMembers;

			public Cluster()
			{
				currentMembers = new HashSet<Integer>();
			}
	}
	
	public Kohonen(int n, int epochs, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.n = n;
		this.epochs = epochs;
		prefetchThreshold = 0.5;
		initialLearningRate = 0.8;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;       
		
		Random rnd = new Random();

		// Here n*n new cluster are initialized
		clusters = new Cluster[n][n];
		for (int i = 0; i < n; i++)  {
			for (int i2 = 0; i2 < n; i2++) {
				clusters[i][i2] = new Cluster();
				clusters[i][i2].prototype = new float[dim];
				
				/// Initializing random vector for each cluster
				for (int i3 = 0; i3 < dim; i3++) {
				    clusters[i][i2].prototype[i3] = rnd.nextFloat();
				}
				
			}
		}
	}

	
	public boolean train()
	{
		// Step 1: initialize map with random vectors (A good place to do this, is in the initialisation of the clusters)
		
	    // Repeat 'epochs' times:
	    for (int t = 0; t < epochs; t++) {
	        
	        /// Informing about progress
	        System.out.print("Epoch " + t + " / " + epochs + "\r");
	        
			// Step 2: Calculate the squareSize and the learningRate, these decrease lineary with the number of epochs.
	        double learningRate = initialLearningRate*(1 - t/(double) epochs);
	        double neighborhoodSize = n/2.0*(1 - t/(double) epochs);
	        
			// Step 3: Every input vector is presented to the map (always in the same order)

	        for (float[] inputVector : trainData) {

			// For each vector its Best Matching Unit is found, and :
	            int[] BMUIndex = findBMU(inputVector);
	        
				// Step 4: All nodes within the neighbourhood of the BMU are changed, you don't have to use distance relative learning.
	            updateNodes(BMUIndex, neighborhoodSize, learningRate, inputVector);
	        }
	    }
	    int idx = 0;
	    for (float[] inputVector : trainData) {

		// For each vector its Best Matching Unit is found, and :
	        int[] BMUIndex = findBMU(inputVector);
	            
            clusters[BMUIndex[0]][BMUIndex[1]].currentMembers.add(idx);
            idx++;
        }
	    // Since training kohonen maps can take quite a while, presenting the user with a progress bar would be nice
		return true;
	}
	
	public boolean test()
	{
	    double totalURLs = 0;
        double totalRequests = 0;
        double totalHits = 0;
		
        // iterate along all clients
        for (int i = 0; i < testData.size(); i++) {
            
            // get the actual testData (the vector) of this client
            float[] client = testData.get(i);
            
            // for each client find the cluster of which it is a member
            int[] clusterIndex = findBMU(client);
            
            Cluster cluster = clusters[clusterIndex[0]][clusterIndex[1]];
            float[] prototype = cluster.prototype;
            
            // iterate along all dimensions
            for (int j = 0; j < client.length; j++) {
                int prototypeDimension = 
                        (prototype[j] > prefetchThreshold) ? 1 : 0;
                
                // and count prefetched htmls
                /// Count the number of prefetched URLs.
                totalURLs += prototypeDimension;
                
                // count number of requests
                /// Count the number of requests.
                totalRequests += client[j];
                
                // count number of hits
                if (prototypeDimension == 1 && client[j] == 1) {
                   totalHits++; 
                }
            }
                
        }
        
        // set the global variables hitrate and accuracy to their appropriate value
        this.hitrate = (totalHits / totalRequests);
        this.accuracy = (totalHits / totalURLs);
    	return true;
	}
	
	/// Calculates Euclidean distance between data vector X and prototype P.
    public double calculateDistance(float[] prototype, float[] pattern) {
        double sum = 0;

        for (int i = 0; i < prototype.length; i++) {
            sum += Math.pow((double) (pattern[i] - prototype[i]), 2);
        }

        return Math.sqrt(sum);
    }
    
    /// Finds Best Matching Unit
    public int[] findBMU(float[] inputVector) {
        double smallestDistance = 
                calculateDistance(clusters[0][0].prototype, inputVector);
        int[] clusterIndex = {0, 0};

        for (int i = 0; i < n; i++) {
            for (int i2 = 0; i2 < n; i2++) {
                double distance = 
                        calculateDistance(clusters[i][i2].prototype, 
                                          inputVector);
    
                if (distance < smallestDistance) {
                    smallestDistance = distance;
                    clusterIndex = new int[] {i, i2};
                }
            }
        }
        
        return clusterIndex;
    }
    
    /// Each node in the neighborhood of BMU gets updated to resemble more input vector.
    public void updateNodes(int[] BMUIndex, double neighborhoodSize, double learningRate, float[] inputVector) {
        int minSize = Math.max((int) (Math.ceil(BMUIndex[0] - neighborhoodSize)), 0);
        int maxSize = Math.min((int) (Math.floor(BMUIndex[0] + neighborhoodSize))+1, n);
        int minSize2 = Math.max((int) (Math.ceil(BMUIndex[1] - neighborhoodSize)), 0);
        int maxSize2 = Math.min((int) (Math.floor(BMUIndex[1] + neighborhoodSize))+1, n);
        
        for (int i = minSize; i < maxSize; i++) {
            for (int i2 = minSize2; i2 < maxSize2; i2++) {
                updatePrototype(i, i2, learningRate, inputVector);
            }
        }
    }
    
    /// Updating specific prototype to resemble input vector better.
    public void updatePrototype(int i, int i2, double learningRate, float[] inputVector) {
        for (int i3 = 0; i3 < dim; i3++) {
            clusters[i][i2].prototype[i3] = (float)((1-learningRate)*clusters[i][i2].prototype[i3] +
                    learningRate*inputVector[i3]);
        }
    }

	public void showTest()
	{
		System.out.println("Initial learning Rate=" + initialLearningRate);
		System.out.println("Prefetch threshold=" + prefetchThreshold);
		System.out.println("Hitrate: " + hitrate);
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Hitrate+Accuracy=" + (hitrate + accuracy));
	}
 
 
	public void showMembers()
	{
		for (int i = 0; i < n; i++)
			for (int i2 = 0; i2 < n; i2++)
				System.out.println("\nMembers cluster["+i+"]["+i2+"] :" + clusters[i][i2].currentMembers);
	}

	public void showPrototypes()
	{
		for (int i = 0; i < n; i++) {
			for (int i2 = 0; i2 < n; i2++) {
				System.out.print("\nPrototype cluster["+i+"]["+i2+"] :");
				
				for (int i3 = 0; i3 < dim; i3++)
					System.out.print(" " + clusters[i][i2].prototype[i3]);
				
				System.out.println();
			}
		}
	}

	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
	
}

