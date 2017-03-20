package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * This class contains a map of selected features indexed by category.
 *
 * @author Marc Vaudel
 */
public class FeaturesMap {

    /**
     * Current version of the features. This number should be incremented when
     * non backward compatible changes are made to the implemented features.
     */
    public final static int currentVersion = 0;

    /**
     * Version of the features used to create the map.
     */
    private final int version = currentVersion;

    /**
     * Map of the different features indexed by category.
     */
    private final HashMap<String, Ms2pipFeature[]> featuresMap = new HashMap<String, Ms2pipFeature[]>(4);
    /**
     * The number of features in the map.
     */
    private int nFeatures = 0;

    /**
     * Constructor.
     */
    public FeaturesMap() {

        for (Class implementedFeature : Ms2pipFeature.implementations) {
            
            featuresMap.put(implementedFeature.getName(), new Ms2pipFeature[0]);
            
        }
    }

    /**
     * Returns the version of the features used to create the map.
     *
     * @return the version of the features used to create the map
     */
    public int getVersion() {
        return version;
    }

    /**
     * Adds a feature to the map.
     *
     * @param ms2pipFeature the feature to add.
     */
    public void addFeature(Ms2pipFeature ms2pipFeature) {

        String category = ms2pipFeature.getCategory();

        Ms2pipFeature[] categoryFeatures = featuresMap.get(category);

        categoryFeatures = Arrays.copyOf(categoryFeatures, categoryFeatures.length + 1);
        categoryFeatures[categoryFeatures.length - 1] = ms2pipFeature;

        nFeatures++;
    }

    /**
     * Returns the different categories of the features in this map.
     *
     * @return the different categories of the features in this map
     */
    public Set<String> getCategories() {
        return featuresMap.keySet();
    }

    /**
     * Returns the features in the map for the given category.
     *
     * @param category the category.
     *
     * @return the list of features in the map for the given category
     */
    public Ms2pipFeature[] getFeatures(String category) {
        return featuresMap.get(category);
    }

    /**
     * Returns the number of features in the map.
     *
     * @return the number of features in the map
     */
    public int getnFeatures() {
        return nFeatures;
    }

}
