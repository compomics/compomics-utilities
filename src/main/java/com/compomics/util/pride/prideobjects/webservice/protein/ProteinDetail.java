package com.compomics.util.pride.prideobjects.webservice.protein;

/**
 * The PRIDE ProteinDetail object
 *
 * @author Kenneth Verheggen
 */
public class ProteinDetail {

    /**
     * a description of the protein (usually inferred from protein accession)
     */
    String[] description;
    /**
     * the assay within the project that identified the protein
     */
    String assayAccession;
    /**
     * the project that identified the protein
     */
    String projectAccession;
    /**
     * the submitted accession of the identified protein
     */
    String accession;
    /**
     * the AA sequence of the identified protein (may be inferred from protein
     * accession)
     */
    String sequence;
    /**
     * accession synonyms; either UniProt accession or EnsEMBL ID
     */
    String[] synonyms;
    /**
     * ['INFERRED' or 'SUBMITTED' or 'NOT_AVAILABLE']: SUBMITTED (provided with
     * dataset) or INFERRED (inferred by PRIDE based on protein accession)
     */
    String sequenceType;

    /**
     * Creates a new ProjectDetail object
     *
     */
    public ProteinDetail() {
    }

    /**
     * Returns the description
     *
     * @return the description
     */
    public String[] getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description the description
     */
    public void setDescription(String[] description) {
        this.description = description;
    }

    /**
     * Returns the assay accession
     *
     * @return the assay accession
     */
    public String getAssayAccession() {
        return assayAccession;
    }

    /**
     * Sets the assay accession
     *
     * @param assayAccession the assay accession
     */
    public void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
    }

    /**
     * Returns the project accession
     *
     * @return the project accession
     */
    public String getProjectAccession() {
        return projectAccession;
    }

    /**
     * Sets the project accession
     *
     * @param projectAccession the project accession
     */
    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    /**
     * Returns the protein accession
     *
     * @return the protein accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Sets the protein accession
     *
     * @param accession the protein accession
     */
    public void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * Returns the sequence
     *
     * @return the sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence
     *
     * @param sequence the sequence
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the synonyms
     *
     * @return the synonyms
     */
    public String[] getSynonyms() {
        return synonyms;
    }

    /**
     * Sets the synonyms
     *
     * @param synonyms the synonyms
     */
    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    /**
     * Returns the sequence type
     *
     * @return the sequence type
     */
    public String getSequenceType() {
        return sequenceType;
    }

    /**
     * Sets the sequence type
     *
     * @param sequenceType the sequence type
     */
    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

}
