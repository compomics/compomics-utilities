package com.compomics.util.experiment.biology;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:22 AM
 * This class modelizes a protein.
 */
public class Protein {

    /**
     * The protein accession
     */
    private String accession;
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
     * @param accession The protein accession
     */
    public Protein(String accession) {
        this.accession = accession;
    }

    /**
     * Constructor for a protein
     * @param accession     The protein accession
     * @param description   The protein description
     */
    public Protein(String accession, String description) {
        this.accession = accession;
        this.description = description;
    }


    /**
     * Getter for the protein accession
     * @return the protein accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Getter for the protein description
     * @return the protein description
     */
    public String getDescription() {
        return description;
    }

    /**
     * A method to compare proteins. For now accession based.
     * @param anotherProtein    an other protein
     * @return a boolean indicating if the proteins are identical
     */
    public boolean isSameAs(Protein anotherProtein) {
        return accession.equals(anotherProtein.getAccession());
    }
}
