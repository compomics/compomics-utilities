package com.compomics.util.math.clustering.settings;

import java.io.Serializable;

/**
 * Settings for k-means clustering.
 *
 * @author Marc Vaudel
 */
public class KMeansClusteringSettings implements Serializable {

    /**
     * The number of clusters to use.
     */
    private int nClusters = 18;

    /**
     * Constructor.
     */
    public KMeansClusteringSettings() {
        
    }

    /**
     * Returns the number of clusters to use.
     * 
     * @return the number of clusters to use
     */
    public int getnClusters() {
        return nClusters;
    }

    /**
     * Sets the number of clusters to use.
     * 
     * @param nClusters the number of clusters to use
     */
    public void setnClusters(int nClusters) {
        this.nClusters = nClusters;
    }
    
    
    
}
