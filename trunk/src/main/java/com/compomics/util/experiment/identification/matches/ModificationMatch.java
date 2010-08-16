package com.compomics.util.experiment.identification.matches;


import com.compomics.util.experiment.biology.PTM;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 1:23:45 PM
 * This class modelizes the match between theoretic ptm and identification results.
 */
public class ModificationMatch {

    // Attributes

    private PTM theoreticPtm;
    private boolean variable;
    private int modifiedSite; // the location in the peptide sequence


    // Constructors

    public ModificationMatch(PTM theoreticPtm, boolean variable, int modifiedSite) {
        this.theoreticPtm = theoreticPtm;
        this.variable = variable;
        this.modifiedSite = modifiedSite;
    }


    // Methods

    public boolean isVariable() {
        return variable;
    }

    public PTM getTheoreticPtm() {
        return theoreticPtm;
    }

    public int getModificationSites() {
        return modifiedSite;
    }


}
