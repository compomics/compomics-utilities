package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models the match between theoretic ptm and identification results.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 1:23:45 PM
 */
public class ModificationMatch extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 7129515983284796207L;
    /**
     * the theoretic modiffication name. The modification can be accessed via the PTM factory
     */
    private String theoreticPtm;
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
     * @param theoreticPtm the theoretic PTM
     * @param variable     true for variable modifications, false otherwise
     * @param modifiedSite the position of the modification in the sequence
     */
    public ModificationMatch(String theoreticPtm, boolean variable, int modifiedSite) {
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
     * getter for the theoretic PTM name
     *
     * @return the theoretic PTM name
     */
    public String getTheoreticPtm() {
        return theoreticPtm;
    }
    
    /**
     * Sets the theoretic PTM
     * @param ptm the theoretic PTM name
     */
    public void setTheoreticPtm(String ptm) {
        this.theoreticPtm = ptm;
    }

    /**
     * getter for the modification site, 1 is the first amino acid.
     *
     * @return the index of the modification in the sequence
     */
    public int getModificationSite() {
        return modifiedSite;
    }
}
