package com.compomics.util.experiment.biology;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 18, 2010
 * Time: 8:56:22 AM
 * This class modelizes a protein.
 */
public class Protein {

    // Attributes

    private String accession;
    private String description = "";


    // Constructors

    public Protein() {
    }

    public Protein(String accession) {
        this.accession = accession;
    }

    public Protein(String accession, String description) {
        this.accession = accession;
        this.description = description;
    }


    // Methods

    public String getAccession() {
        return accession;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSameAs(Protein anotherProtein) {
        return accession.equals(anotherProtein.getAccession());
    }
}
