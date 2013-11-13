/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.experiment.identification.tags;

/**
 * Interface for a sequence tag compontent
 *
 * @author Marc
 */
public interface TagComponent {
    
    /**
     * Returns the tag component as String like a peptide sequence. Note: this does not include modifications.
     * 
     * @return the tag component as String like a peptide sequence
     */
    public String asSequence();
    /**
     * Returns the mass of the tag component.
     * 
     * @return the mass of the tag component
     */
    public Double getMass();
    /**
     * Indicates whether another component is the same as the component of interest
     * 
     * @param anotherCompontent another component
     * 
     * @return a boolean indicating whether the other component is the same as the one of interest
     */
    public boolean isSameAs(TagComponent anotherCompontent);
}
