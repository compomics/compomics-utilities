package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.utils.ExperimentObject;

/**
 * This class models a protein.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:22 AM
 */
public class Protein extends ExperimentObject {

    /**
     * The protein accession
     */
    private String accession;
    /**
     * Boolean indicating if the protein is not existing (decoy protein for instance)
     */
    private boolean decoy;
    /**
     * The protein description
     */
    private String description = "";

    /**
     * Constructor for a protein
     */
    public Protein() {
    }

    /**
     * Constructor for a protein
     *
     * @param accession The protein accession
     */
    public Protein(String accession, boolean isDecoy) {
        this.accession = accession;
        this.decoy = isDecoy;
    }

    /**
     * indicates if the protein is factice (from a decoy database for instance)
     * @return a boolean indicating if the protein is factice
     */
    public boolean isDecoy() {
        return decoy;
    }

    /**
     * Constructor for a protein
     *
     * @param accession     The protein accession
     * @param description   The protein description
     */
    public Protein(String accession, String description, boolean isDecoy) {
        this.accession = accession;
        this.description = description;
        this.decoy = isDecoy;
    }

    /**
     * Getter for the protein accession
     *
     * @return the protein accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Getter for the protein description
     *
     * @return the protein description
     */
    public String getDescription() {
        return description;
    }

    /**
     * A method to compare proteins. For now accession based.
     * 
     * @param anotherProtein    an other protein
     * @return a boolean indicating if the proteins are identical
     */
    public boolean isSameAs(Protein anotherProtein) {
        return accession.equals(anotherProtein.getAccession());
    }
}
