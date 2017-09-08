package com.compomics.util.experiment.io.biology.protein;

import com.compomics.util.experiment.biology.proteins.Protein;

/**
 * Interface for a protein iterator.
 * 
 *
 * @author Marc Vaudel
 */
public interface ProteinIterator {

    /**
     * Returns the next protein. Null if end was reached. If an error is encountered while iterating, the exception is thrown as runtime exception.
     * 
     * @return the next protein
     */
    public Protein getNextProtein();
    
}
