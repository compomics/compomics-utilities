package com.compomics.software.settings;

/**
 * Interface for a path key.
 *
 * @author Marc Vaudel
 */
public interface PathKey {

    /**
     * Returns the id of the path.
     *
     * @return the id of the path
     */
    public String getId();

    /**
     * Returns the description of the path.
     *
     * @return the description of the path
     */
    public String getDescription();
}
