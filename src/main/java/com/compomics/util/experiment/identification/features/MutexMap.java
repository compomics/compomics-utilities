package com.compomics.util.experiment.identification.features;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * Mutex map for the features cache. Note that the current implementation supports a single cache only.
 *
 * @author Marc Vaudel
 */
public class MutexMap {
    
    /**
     * Semaphore for the different object types.
     */
    final static HashMap<IdentificationFeaturesCache.ObjectType, Semaphore> mutexMap = new HashMap<IdentificationFeaturesCache.ObjectType, Semaphore>(IdentificationFeaturesCache.ObjectType.values().length);
    
    

}
