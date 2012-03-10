package com.compomics.util.pride;

import java.io.Serializable;

/**
 * Interface for a pride object
 *
 * @author marc
 */
public interface PrideObject {
    
    /**
     * Returns the name to use when serializing the object
     * @return the name to use when serializing the object
     */
    public String getFileName();
    
}
