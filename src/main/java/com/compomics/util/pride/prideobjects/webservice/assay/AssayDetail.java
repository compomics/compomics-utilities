package com.compomics.util.pride.prideobjects.webservice.assay;

import com.compomics.util.pride.prideobjects.webservice.project.ContactDetail;

/**
 * The PRIDE AssayDetail object
 *
 * @author Kenneth Verheggen
 */
public class AssayDetail {

    /**
     * flag if ms level 2 annotation is available
     */
    private boolean ms2Annotation;
    /**
     * experimental factors reported for this assay
     */
    private String experimentalFactor;
    /**
     * number of proteins in this assay
     */
    private int proteinCount;
    /**
     * number of peptides in this assay
     */
    private int peptideCount;
    /**
     * number of unique peptides in this assay
     */
    private int uniquePeptideCount;
    /**
     * number of identified spectra in this assay
     */
    private int identifiedSpectrumCount;
    /**
     * total number of spectra in this assay
     */
    private int totalSpectrumCount;
    /**
     * software used for the data/result generation
     */
    private String[] softwares;
    /**
     * submitter of the dataset
     */
    private ContactDetail[] contacts;
    /**
     * disease annotation provided for this assay (if applicable)
     */
    private String[] diseases;
    /**
     * quantification methods used
     */
    private String[] quantMethods;
    /**
     * flag to indicate if a chromatogram is available
     */
    private boolean chromatogram;
    /**
     * sample details reported for this assay
     */
    private String[] sampleDetails;
    /**
     * reported instrument information
     */
    private String[] instrumentNames;
    /**
     * the short label give to the assay by the submitter
     */
    private String shortLabel;
    /**
     * the title give to the assay by the submitter
     */
    private String title;
    /**
     * the accession assigned to the assay
     */
    private String assayAccession;
    /**
     * the project this assay belongs to
     */
    private String projectAccession;
    /**
     * additional keywords added to this assay
     */
    private String keywords;
    /**
     * the species reported for this assay
     */
    private String[] species;
    /**
     * reported modifications
     */
    private String[] ptmNames;

    /**
     * Create a new AssayDetail object.
     *
     */
    public AssayDetail() {
    }

    /**
     * Returns whether the assay has an MS2 annotation.
     *
     * @return the name
     */
    public boolean isMs2Annotation() {
        return ms2Annotation;
    }

    /**
     * Set whether the assay has an MS2 annotation
     *
     * @param ms2Annotation a boolean
     */
    public void setMs2Annotation(boolean ms2Annotation) {
        this.ms2Annotation = ms2Annotation;
    }

    /**
     * Returns the experimental factor
     *
     * @return the name
     */
    public String getExperimentalFactor() {
        return experimentalFactor;
    }

    /**
     * Set the experimental factor.
     *
     * @param experimentalFactor the experimental factor to set
     */
    public void setExperimentalFactor(String experimentalFactor) {
        this.experimentalFactor = experimentalFactor;
    }

    /**
     * Returns the protein count
     *
     * @return the protein count
     */
    public int getProteinCount() {
        return proteinCount;
    }

    /**
     * Set the protein count name.
     *
     * @param proteinCount the protein count to set
     */
    public void setProteinCount(int proteinCount) {
        this.proteinCount = proteinCount;
    }

    /**
     * Returns the peptide count
     *
     * @return the peptide count
     */
    public int getPeptideCount() {
        return peptideCount;
    }

    /**
     * Set the peptide count.
     *
     * @param peptideCount the peptide count to set
     */
    public void setPeptideCount(int peptideCount) {
        this.peptideCount = peptideCount;
    }

    /**
     * Returns the unique peptide count
     *
     * @return the unique peptide count
     */
    public int getUniquePeptideCount() {
        return uniquePeptideCount;
    }

    /**
     * Set the unique peptide count.
     *
     * @param uniquePeptideCount the unique peptide count to set
     */
    public void setUniquePeptideCount(int uniquePeptideCount) {
        this.uniquePeptideCount = uniquePeptideCount;
    }

    /**
     * Returns the identified spectrum count
     *
     * @return the identified spectrum count
     */
    public int getIdentifiedSpectrumCount() {
        return identifiedSpectrumCount;
    }

    /**
     * Set the identified spectrum count.
     *
     * @param identifiedSpectrumCount the identifiedSpectrumCount to set
     */
    public void setIdentifiedSpectrumCount(int identifiedSpectrumCount) {
        this.identifiedSpectrumCount = identifiedSpectrumCount;
    }

    /**
     * Returns the total spectrum count
     *
     * @return the total spectrum count
     */
    public int getTotalSpectrumCount() {
        return totalSpectrumCount;
    }

    /**
     * Set the total spectrum count.
     *
     * @param totalSpectrumCount the totalSpectrumCount to set
     */
    public void setTotalSpectrumCount(int totalSpectrumCount) {
        this.totalSpectrumCount = totalSpectrumCount;
    }

    /**
     * Returns the used software(s)
     *
     * @return the used software(s)
     */
    public String[] getSoftwares() {
        return softwares;
    }

    /**
     * Set the used softwares.
     *
     * @param softwares the softwares to set
     */
    public void setSoftwares(String[] softwares) {
        this.softwares = softwares;
    }

    /**
     * Returns the contactdetails
     *
     * @return the contactdetails
     */
    public ContactDetail[] getContacts() {
        return contacts;
    }

    /**
     * Set the contacts.
     *
     * @param contacts the contacts to set
     */
    public void setContacts(ContactDetail[] contacts) {
        this.contacts = contacts;
    }

    /**
     * Returns the associated diseases
     *
     * @return the associated diseases
     */
    public String[] getDiseases() {
        return diseases;
    }

    /**
     * Set the diseases.
     *
     * @param diseases the diseases to set
     */
    public void setDiseases(String[] diseases) {
        this.diseases = diseases;
    }

    /**
     * Returns the quantification methods (if any)
     *
     * @return the quantification methods
     */
    public String[] getQuantMethods() {
        return quantMethods;
    }

    /**
     * Set the contact quantMethods.
     *
     * @param quantMethods the quant methods to set
     */
    public void setQuantMethods(String[] quantMethods) {
        this.quantMethods = quantMethods;
    }

    /**
     * Returns whether the chromatogram is included
     *
     * @return a boolean indicating the presence of a chromatogram
     */
    public boolean isChromatogram() {
        return chromatogram;
    }

    /**
     * Set the chromatogram boolean.
     *
     * @param chromatogram the chromatogram boolean
     */
    public void setChromatogram(boolean chromatogram) {
        this.chromatogram = chromatogram;
    }

    /**
     * Returns the sample details (if any)
     *
     * @return the sample details
     */
    public String[] getSampleDetails() {
        return sampleDetails;
    }

    /**
     * Set the sample details.
     *
     * @param sampleDetails the sample details to set
     */
    public void setSampleDetails(String[] sampleDetails) {
        this.sampleDetails = sampleDetails;
    }

    /**
     * Returns the instrument names
     *
     * @return the instrument names
     */
    public String[] getInstrumentNames() {
        return instrumentNames;
    }

    /**
     * Set the instrument names.
     *
     * @param instrumentNames the instrumentNames to set
     */
    public void setInstrumentNames(String[] instrumentNames) {
        this.instrumentNames = instrumentNames;
    }

    /**
     * Returns the short label
     *
     * @return the short label
     */
    public String getShortLabel() {
        return shortLabel;
    }

    /**
     * Set the shortLabel.
     *
     * @param shortLabel the shortLabel to set
     */
    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    /**
     * Returns the assay title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the assay accession
     *
     * @return the accession
     */
    public String getAssayAccession() {
        return assayAccession;
    }

    /**
     * Set the assayAccession name.
     *
     * @param assayAccession the assayAccession to set
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
     * Set the contact projectAccession.
     *
     * @param projectAccession the projectAccession to set
     */
    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    /**
     * Returns the keywords
     *
     * @return the keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Set the keywords.
     *
     * @param keywords the keywords to set
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Returns the species
     *
     * @return the species
     */
    public String[] getSpecies() {
        return species;
    }

    /**
     * Set the species.
     *
     * @param species the species to set
     */
    public void setSpecies(String[] species) {
        this.species = species;
    }

    /**
     * Returns the ptms
     *
     * @return the ptms
     */
    public String[] getPtmNames() {
        return ptmNames;
    }

    /**
     * Set the ptmNames
     *
     * @param ptmNames the ptm names to set
     */
    public void setPtmNames(String[] ptmNames) {
        this.ptmNames = ptmNames;
    }

}
