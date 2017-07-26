package com.compomics.util.math.clustering;

import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import no.uib.jsparklines.renderers.util.Util;

/**
 * K-means clustering.
 *
 * @author Harald Barsnes
 */
public class KMeansClustering {

    /**
     * The number of clusters.
     */
    private final int NUM_CLUSTERS;
    /**
     * The number of samples.
     */
    private final int NUM_SAMPLES;
    /**
     * The number of values for each sample.
     */
    private final int NUM_VALUES;
    /**
     * The sample data.
     */
    private final double SAMPLES[][];
    /**
     * The sample identifiers.
     */
    private final String SAMPLE_IDS[];
    /**
     * The centroids.
     */
    private double centroids[][];
    /**
     * The current cluster each sample belongs to.
     */
    private int clusters[];
    /**
     * The maximum number of iteration.
     */
    private int maxIterations = 500; // @TODO: what should the default be..?

    /**
     * Constructor.
     *
     * @param samples the data
     * @param sampleIds the sample identifiers
     * @param numClusters the number of clusters
     */
    public KMeansClustering(double samples[][], String[] sampleIds, int numClusters) {

        SAMPLES = samples;
        SAMPLE_IDS = sampleIds;
        NUM_SAMPLES = samples.length;
        NUM_VALUES = samples[0].length;
        NUM_CLUSTERS = numClusters;

        if (NUM_CLUSTERS > NUM_SAMPLES) {
            throw new IllegalArgumentException("The number of clusters cannot be bigger than the number of samples! #clusters: " + NUM_CLUSTERS + ", #samples: " + NUM_SAMPLES);
        }

        initialize();
    }

    /**
     * Constructor.
     *
     * @param dataFile the file with the data
     * @param numClusters the number of clusters
     */
    public KMeansClustering(File dataFile, int numClusters) {

        SampleData sampleData = readDataFromFile(dataFile);

        SAMPLES = sampleData.getSamples();
        SAMPLE_IDS = sampleData.getSampleIds();
        NUM_SAMPLES = SAMPLES.length;
        NUM_VALUES = SAMPLES[0].length;
        NUM_CLUSTERS = numClusters;

        if (NUM_CLUSTERS > NUM_SAMPLES) {
            throw new IllegalArgumentException("The number of clusters cannot be bigger than the number of samples! #clusters: " + NUM_CLUSTERS + ", #samples: " + NUM_SAMPLES);
        }

        initialize();
    }

    /**
     * Set up the empty clusters and set the initial centroids.
     */
    private void initialize() {

        // set up the yet empty clusters
        clusters = new int[NUM_SAMPLES];

        // add the initial centroids
        centroids = new double[NUM_CLUSTERS][NUM_VALUES];

        // set the initial random centroids
        Random rand = new Random();
        for (int centroidCounter = 0; centroidCounter < NUM_CLUSTERS; centroidCounter++) {
            int randomSample = rand.nextInt(NUM_SAMPLES);
            System.arraycopy(SAMPLES[randomSample], 0, centroids[centroidCounter], 0, NUM_VALUES);
        }
    }

    /**
     * Run the k-means clustering.
     *
     * @param waitingHandler the waiting handler
     */
    public void kMeanCluster(WaitingHandler waitingHandler) {

        boolean clustersChanged = true;

        // asign the samples to the clusters
        assignToClusters();

        int iterationCounter = 0;

        // iterate until the clustering no longer changes
        while (clustersChanged && iterationCounter < maxIterations && !waitingHandler.isRunCanceled()) {

            // calculate the new centroids
            calculateNewCentroids();

            // assign the samples to the new centroids
            clustersChanged = assignToClusters();

            iterationCounter++;
        }
    }

    /**
     * Assign the samples to the clusters.
     *
     * @return true if the clustering changed
     */
    private boolean assignToClusters() {

        boolean clustersChanged = false;

        for (int sampleNumber = 0; sampleNumber < NUM_SAMPLES; sampleNumber++) {

            double minimumValue = Double.MAX_VALUE;
            int selectedCentroidNumber = 0;

            // find the closest cluster
            for (int centroidNumber = 0; centroidNumber < NUM_CLUSTERS; centroidNumber++) {
                double distance = distSampleToCentroid(sampleNumber, centroidNumber);
                if (distance < minimumValue) {
                    minimumValue = distance;
                    selectedCentroidNumber = centroidNumber;
                }
            }

            // check if the sample's cluster assignment changed
            if (clusters[sampleNumber] != selectedCentroidNumber) {
                clustersChanged = true;
            }

            // add to the closest cluster
            clusters[sampleNumber] = selectedCentroidNumber;
        }

        return clustersChanged;
    }

    /**
     * Calculate new centroids.
     */
    private void calculateNewCentroids() {

        // clear the centroids
        clearCentroids();

        // calculate new centroids
        for (int centroidNumber = 0; centroidNumber < NUM_CLUSTERS; centroidNumber++) {

            int totalInCluster = 0;

            for (int sampleCounter = 0; sampleCounter < NUM_SAMPLES; sampleCounter++) {
                if (clusters[sampleCounter] == centroidNumber) {
                    for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
                        centroids[centroidNumber][valueNumber] += SAMPLES[sampleCounter][valueNumber];
                    }
                    totalInCluster++;
                }
            }

            if (totalInCluster > 0) {
                for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
                    centroids[centroidNumber][valueNumber] /= totalInCluster;
                }
            }
        }
    }

    /**
     * Clear the centroids.
     */
    private void clearCentroids() {
        for (int centroidNumber = 0; centroidNumber < NUM_CLUSTERS; centroidNumber++) {
            for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
                centroids[centroidNumber][valueNumber] = 0.0;
            }
        }
    }

    /**
     * Calculate the Euclidean distance between a sample and a centroid.
     *
     * @param sampleNumber the sample number
     * @param centroidNumber the centroid number
     * @return the Euclidean distance
     */
    private double distSampleToCentroid(int sampleNumber, int centroidNumber) {
        double distance = 0;
        for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
            distance += Math.pow(SAMPLES[sampleNumber][valueNumber] - centroids[centroidNumber][valueNumber], 2);
        }
        return Math.sqrt(distance);
    }

    /**
     * Calculate the Euclidean distance between two samples.
     *
     * @param sampleNumber1 the samples number of the first sample
     * @param sampleNumber2 the sample number of the second sample
     * @return the Euclidean distance
     */
    private double distSampleToSample(int sampleNumber1, int sampleNumber2) {
        double distance = 0;
        for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
            distance += Math.pow(SAMPLES[sampleNumber1][valueNumber] - SAMPLES[sampleNumber2][valueNumber], 2);
        }
        return Math.sqrt(distance);
    }

    /**
     * Main method for testing purposes.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // example with direct input
        // the sample data
        double samples[][] = new double[][]{
            {1.018387278, 0.983270041, 1.063472453, 0.713225975, 0.731043734, 0.973387687, 0.936300274, 1.039486067, 1.134279088, 0.986361721},
            {0.981590377, 1.02987824, 1.089762055, 0.927909537, 0.745221317, 0.709817942, 0.655031878, 1.047253604, 0.952668566, 1.037703939},
            {1.03694662, 1.080079418, 1.041962748, 1.258192406, 1.342060684, 0.996528485, 0.924128553, 0.936412377, 0.920298185, 0.918456169},
            {0.990287761, 0.892692992, 0.914314664, 1.279408351, 1.31410923, 0.941641721, 0.910025757, 1.064973225, 1.041036986, 1.049735711},
            {1.040106591, 0.938527051, 0.965804511, 0.695864906, 0.813267072, 1.064862452, 1.128367944, 0.9798703, 1.268314349, 0.890250862},
            {1.283690338, 1.221861511, 1.237727692, 1.131154141, 0.991934148, 0.962126821, 0.943197586, 0.872215846, 0.912011518, 0.829430491},
            {0.981473817, 0.805082739, 0.979007845, 0.685868656, 0.467881815, 1.30464142, 1.031580941, 1.120770021, 1.163524042, 0.948936962},
            {0.935739165, 0.961540471, 0.948513884, 1.1214119, 1.139158941, 0.952546774, 1.061539826, 0.967187465, 0.969725485, 1.066965917},
            {0.98084797, 0.99517748, 0.967601553, 1.408483587, 1.242533492, 0.809655819, 1.012664473, 0.972120169, 0.90671428, 1.064156888},
            {1.114446123, 1.024968093, 1.034149441, 0.783212889, 0.801006499, 0.983516619, 1.026256729, 0.996830977, 0.975588315, 0.942473673},
            {0.905988305, 0.908986417, 0.925003413, 1.19651456, 1.106383596, 0.997060333, 1.030914868, 1.07807453, 1.146596783, 1.079137402},
            {1.040040646, 1.049901339, 0.989359079, 1.017323675, 1.008910963, 0.983004953, 0.984566787, 1.040902927, 1.02390089, 1.015875601},
            {1.038052043, 0.999666309, 1.011292944, 0.862294159, 0.878858798, 0.98299443, 0.963822514, 0.982571918, 0.975889047, 1.009450539},
            {0.821272331, 0.767589262, 0.817114369, 1.059135199, 0.884487875, 1.091284726, 1.022820961, 1.148307617, 1.032334252, 1.167097238},
            {1.016334545, 1.090488723, 0.981954941, 1.223423201, 1.07287664, 0.967790703, 0.894805565, 1.103557481, 1.031495908, 1.028484672},
            {0.991456092, 0.665417264, 0.862248473, 1.005142654, 0.919656901, 1.244190762, 1.056869139, 1.031395099, 0.898937035, 0.946095374}
        };

        // the sample identifiers
        String sampleNames[] = new String[]{"O95071", "Q6ZT21", "Q99590", "Q14517", "Q9P219", "Q14692",
            "Q8TF74", "Q13427", "Q9ULD9", "Q9UPN9", "P51805", "Q92621", "Q5SRE5", "Q8TB73", "Q96CP6", "Q13671"};
        KMeansClustering kMeansClutering = new KMeansClustering(samples, sampleNames, 5);
//        
//        // example with input from file - tab separated input, no header, first column assumed to be the sample ids 
//        KMeansClustering kMeansClutering = new KMeansClustering(new File("C:\\Users\\hba041\\Desktop\\clustering_data.txt"), 30);

        // print the initial centroids
        System.out.println("Centroids initialized at:");
        kMeansClutering.printCentroids();
        System.out.print("\n");

        // exectute the clustering
        kMeansClutering.kMeanCluster(new WaitingHandlerCLIImpl());

        // print the clustering results
        kMeansClutering.printClusters();

        // print the centroid results
        System.out.println("Centroids finalized at:");
        kMeansClutering.printCentroids();
        System.out.print("\n");
    }

    /**
     * Print the centroids.
     */
    public void printCentroids() {
        for (int centroidNumber = 0; centroidNumber < NUM_CLUSTERS; centroidNumber++) {
            System.out.print("     " + (centroidNumber + 1) + "\t\t");
            for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
                if (valueNumber > 0) {
                    System.out.print("\t");
                }
                System.out.print(Util.roundDouble(centroids[centroidNumber][valueNumber], 2));
            }
            System.out.println();
        }
    }

    /**
     * Print the current clusters.
     */
    public void printClusters() {
        for (int clusterIndex = 0; clusterIndex < NUM_CLUSTERS; clusterIndex++) {
            System.out.println("Cluster " + (clusterIndex + 1) + " includes:");
            for (int sampleIndex = 0; sampleIndex < NUM_SAMPLES; sampleIndex++) {
                if (clusters[sampleIndex] == clusterIndex) {
                    System.out.print("     " + SAMPLE_IDS[sampleIndex] + "\t");
                    for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
                        if (valueNumber > 0) {
                            System.out.print("\t");
                        }
                        System.out.print(Util.roundDouble(SAMPLES[sampleIndex][valueNumber], 2));
                    }
                    System.out.println();
                }
            }
            System.out.println();
        }
    }

    /**
     * Get the sample names of all the members in the given cluster.
     *
     * @param clusterIndex the index of the cluster
     * @return the sample names of all the members in the given cluster
     */
    public ArrayList<String> getClusterMembers(int clusterIndex) {

        ArrayList<String> clusterMembers = new ArrayList<>();

        for (int sampleIndex = 0; sampleIndex < NUM_SAMPLES; sampleIndex++) {
            if (clusters[sampleIndex] == clusterIndex) {
                clusterMembers.add(SAMPLE_IDS[sampleIndex]);
            }
        }

        return clusterMembers;
    }

    /**
     * Returns a hashmap with the values for the members in the given cluster.
     * Key: sample id, value: the data points.
     *
     * @param clusterIndex the index of the cluster
     * @return the values for the members in the given cluster
     */
    public HashMap<String, ArrayList<Double>> getClusterMembersData(int clusterIndex) {

        HashMap<String, ArrayList<Double>> clusterMembers = new HashMap<>();

        for (int sampleIndex = 0; sampleIndex < NUM_SAMPLES; sampleIndex++) {
            if (clusters[sampleIndex] == clusterIndex) {

                ArrayList<Double> values = new ArrayList<>();

                for (int valueNumber = 0; valueNumber < NUM_VALUES; valueNumber++) {
                    values.add(SAMPLES[sampleIndex][valueNumber]);
                }

                clusterMembers.put(SAMPLE_IDS[sampleIndex], values);
            }
        }

        return clusterMembers;
    }

    /**
     * Read sample data from file.
     *
     * @param dataFile the file to read from
     * @return the sample data
     */
    private SampleData readDataFromFile(File dataFile) {

        SampleData sampleData = null;

        try {
            FileReader f = new FileReader(dataFile);
            BufferedReader br = new BufferedReader(f);

            String line = br.readLine();

            ArrayList<String> sampleIds = new ArrayList<>();
            ArrayList<ArrayList<Double>> sampleDataAsArray = new ArrayList<>();
            int numSamples = 0;
            int numValues = 0;

            while (line != null) {

                String[] values = line.split("\\t");
                sampleIds.add(values[0]);

                ArrayList<Double> tempData = new ArrayList<>();
                for (int i = 1; i < values.length; i++) {
                    tempData.add(Double.parseDouble(values[i]));
                }
                sampleDataAsArray.add(tempData);

                if (numValues == 0) {
                    numValues = values.length - 1;
                }

                numSamples++;
                line = br.readLine();
            }

            String sampleNames[] = new String[numSamples];
            for (int i = 0; i < numSamples; i++) {
                sampleNames[i] = sampleIds.get(i);
            }

            double samples[][] = new double[numSamples][numValues];
            for (int i = 0; i < numSamples; i++) {
                for (int j = 0; j < numValues; j++) {
                    samples[i][j] = sampleDataAsArray.get(i).get(j);
                }
            }

            sampleData = new SampleData(samples, sampleNames);

            br.close();
            f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sampleData;
    }

    /**
     * Returns the number of clusters.
     *
     * @return the number of clusters
     */
    public int getNumberOfClusters() {
        return NUM_CLUSTERS;
    }

    /**
     * Returns the maximum number of iterations.
     *
     * @return the maximum number of iterations
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * Set the maximum number of iterations.
     *
     * @param maxIterations the maximum number of iterations
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * Sample data.
     */
    private class SampleData {

        /**
         * The sample data.
         */
        private double samples[][];

        /**
         * The sample identifiers.
         */
        private String sampleIds[];

        /**
         * The sample data.
         *
         * @param samples the data
         * @param sampleIds the sample identifiers.
         */
        public SampleData(double samples[][], String sampleIds[]) {
            this.samples = samples;
            this.sampleIds = sampleIds;
        }

        /**
         * Returns the samples.
         *
         * @return the samples
         */
        public double[][] getSamples() {
            return samples;
        }

        /**
         * Returns the sample identifiers.
         *
         * @return the sample identifiers
         */
        public String[] getSampleIds() {
            return sampleIds;
        }
    }
}
