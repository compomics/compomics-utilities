package com.compomics.util.pride;

/**
 * Interface for a PRIDE object.
 *
 * @author Marc Vaudel
 */
public interface PrideObject {

    /**
     * Returns the name to use when serializing the object.
     * 
     * @return the name to use when serializing the object
     */
    public String getFileName();
}
