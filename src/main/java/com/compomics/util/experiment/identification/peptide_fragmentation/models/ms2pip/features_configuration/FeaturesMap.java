package com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_configuration;

import com.compomics.util.experiment.identification.peptide_fragmentation.models.ms2pip.features_generation.Ms2pipFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * This class contains a map of selected features indexed by category.
 *
 * @author Marc Vaudel
 */
public class FeaturesMap {
    
    /**
     * Current version of the features. This number should be incremented when non backward compatible changes are made to the implemented features.
     */
    public final static int currentVersion = 0;
    
    /**
     * Version of the features used to create the map.
     */
    private final int version = currentVersion;
    
    /**
     * Map of the different features indexed by category.
     */
    private final HashMap<String, ArrayList<Ms2pipFeature>> featuresMap = new HashMap<String, ArrayList<Ms2pipFeature>>(4);

    /**
     * Constructor.
     */
    public FeaturesMap() {
        
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
        
        ArrayList<Ms2pipFeature> categoryFeatures = featuresMap.get(category);
        
        if (categoryFeatures == null) {
            categoryFeatures = new ArrayList<Ms2pipFeature>(1);
            featuresMap.put(category, categoryFeatures);
        }
        
        categoryFeatures.add(ms2pipFeature);
        
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
     * Returns the list of features in the map for the given category.
     * 
     * @param category the category.
     * 
     * @return the list of features in the map for the given category
     */
    public ArrayList<Ms2pipFeature> getFeatures(String category) {
        return featuresMap.get(category);
    }
    
}
