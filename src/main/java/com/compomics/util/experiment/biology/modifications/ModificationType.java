package com.compomics.util.experiment.biology.modifications;

/**
 * Enum for the different types of modifications supported.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum ModificationType {

    /**
     * Modification at specific amino acids.
     */
    modaa(0, "Modification at specific amino acids.", "Particular Amino Acid"),
    /**
     * Modification at the N-terminus of a protein.
     */
    modn_protein(1, "Modification at the N-terminus of a protein", "Protein N-term"),
    /**
     * Modification at the N-terminus of a protein at specific amino acids.
     */
    modnaa_protein(2, "Modification at the N-terminus of a protein at specific amino acids.",
            "Protein N-term - Particular Amino Acid(s)"),
    /**
     * Modification at the C-terminus of a protein.
     */
    modc_protein(3, "Modification at the C-terminus of a protein.", "Protein C-term"),
    /**
     * Modification at the C-terminus of a protein at specific amino acids.
     */
    modcaa_protein(4, "Modification at the C-terminus of a protein at specific amino acids.",
            "Protein C-term - Particular Amino Acid(s)"),
    /**
     * Modification at the N-terminus of a peptide.
     */
    modn_peptide(5, "Modification at the N-terminus of a peptide", "Peptide N-term"),
    /**
     * Modification at the N-terminus of a peptide at specific amino acids.
     */
    modnaa_peptide(6, "Modification at the N-terminus of a peptide at specific amino acids.",
            "Peptide N-term - Particular Amino Acid(s)"),
    /**
     * Modification at the C-terminus of a peptide.
     */
    modc_peptide(7, "Modification at the C-terminus of a peptide.", "Peptide C-term"),
    /**
     * Modification at the C-terminus of a peptide at specific amino acids.
     */
    modcaa_peptide(8, "Modification at the C-terminus of a peptide at specific amino acids.",
            "Peptide C-term - Particular Amino Acid(s)");

    /**
     * The index of the type, must be the index in the values array.
     */
    public final int index;
    /**
     * The description of the type.
     */
    public final String description;
    /**
     * The short name of the type.
     */
    public final String shortName;

    /**
     * Empty default constructor.
     */
    private ModificationType() {
        index = 0;
        description = "";
        shortName = "";
    }

    /**
     * Constructor.
     *
     * @param index the index of the type
     * @param description the description of the type
     * @param shortName the short name of the type
     */
    private ModificationType(int index, String description, String shortName) {
        this.index = index;
        this.description = description;
        this.shortName = shortName;
    }

    /**
     * Returns a boolean indicating whether the modification type targets the
     * N-terminus of a protein or of a peptide.
     *
     * @return a boolean indicating whether the modification type targets the
     * N-terminus of a protein or of a peptide
     */
    public boolean isNTerm() {

        return this == modn_peptide
                || this == modn_protein
                || this == modnaa_peptide
                || this == modnaa_protein;

    }

    /**
     * Returns a boolean indicating whether the modification type targets the
     * C-terminus of a protein or of a peptide.
     *
     * @return a boolean indicating whether the modification type targets the
     * C-terminus of a protein or of a peptide
     */
    public boolean isCTerm() {

        return this == modc_peptide
                || this == modc_protein
                || this == modcaa_peptide
                || this == modcaa_protein;

    }

    @Override
    public String toString() {
        return shortName;
    }

    /**
     * Returns all of the modification type options as a string.
     * 
     * @return the modification type options
     */
    public static String getTypesAsString() {
        
        StringBuilder modTypesAsString = new StringBuilder();
        
        for(ModificationType modType :values()) {
            if (modTypesAsString.length() > 0) {
                modTypesAsString.append("; ");
            }
            modTypesAsString.append(modType.description).append(": ").append(modType.description);
        }
        
        modTypesAsString.append(".");
        
        return modTypesAsString.toString();
    }
}
