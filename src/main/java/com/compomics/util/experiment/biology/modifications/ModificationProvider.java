package com.compomics.util.experiment.biology.modifications;

/**
 * Interface for a modification provider.
 *
 * @author Marc Vaudel
 */
public interface ModificationProvider {
    
    /**
     * Returns the modification with the given name name. Null if none found.
     *
     * @param name The name of the modification of interest.
     *
     * @return The modification of interest
     */
    public Modification getModification(String name);

}
