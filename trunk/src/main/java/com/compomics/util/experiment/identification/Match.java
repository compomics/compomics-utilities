/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This is an abstract class for an identification match
 *
 * @author marc
 */
public abstract class Match extends ExperimentObject {
    
    /**
     * returns the key of a match
     * @return the key of a match
     */
    public abstract String getKey();
}
