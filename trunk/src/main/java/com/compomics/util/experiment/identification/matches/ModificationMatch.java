package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.PTM;


/**
 * This class models the match between theoretic ptm and identification results.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 1:23:45 PM
 */
public class ModificationMatch {

    /**
     * the theoretic modiffication
     */
    private PTM theoreticPtm;
    /**
     * is the modification variable?
     */
    private boolean variable;
    /**
     * the location in the peptide sequence
     */
    private int modifiedSite;


    /**
     * constructor for a modification match
     *
     * @param theoreticPtm  the theoretic PTM
     * @param variable      true for variable modifications, false otherwise
     * @param modifiedSite  the position of the modification in the sequence
     */
    public ModificationMatch(PTM theoreticPtm, boolean variable, int modifiedSite) {
        this.theoreticPtm = theoreticPtm;
        this.variable = variable;
        this.modifiedSite = modifiedSite;
    }

    /**
     * returns a boolean indicating if the modification is variable
     *
     * @return a boolean indicating if the modification is variable
     */
    public boolean isVariable() {
        return variable;
    }

    /**
     * getter for the theoretic PTM
     *
     * @return the theoretic PTM
     */
    public PTM getTheoreticPtm() {
        return theoreticPtm;
    }

    /**
     * getter for the modification site
     *
     * @return the index of the modification in the sequence
     */
    public int getModificationSites() {
        return modifiedSite;
    }
}
