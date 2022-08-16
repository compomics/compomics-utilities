package com.compomics.util.experiment.identification.validation.percolator;

import com.compomics.util.experiment.personalization.UrParameter;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.EnumMap;

/**
 * This class serves as  a cache for Percolator features.
 *
 * @author Marc Vaudel
 */
public class PercolatorFeaturesCache implements UrParameter, Serializable {
    
    /**
     * Dummy object for instantiation purposes.
     */
    public final static PercolatorFeaturesCache dummy = new PercolatorFeaturesCache();

    /**
     * Map of the Percolator values.
     */
    public final EnumMap<PercolatorFeature, Object> cache = new EnumMap<>(PercolatorFeature.class);
    
    @Override
    public long getParameterKey() {
        
        return ObjectStreamClass.lookup(PercolatorFeaturesCache.class).getSerialVersionUID();
        
    }
    
}
