package com.compomics.util.pride.prideobjects.webservice.project;

/**
 * The PRIDE ProjectDetail object
 *
 * @author Kenneth Verheggen
 */
public class ProjectDetail {

    /**
     *
     * relevant keywords associated with the project
     */
    private String keywords;
    /**
     *
     * details of the submitter of the dataset
     */
    private ContactDetail submitter;
    /**
     *
     * the Digital Object Identifier (DOI) for the project (if available)
     */
    private String doi;
    /**
     *
     * annotation to indicate that the dataset is a re-analysis based on other
     * public data
     */
    private String reanalysis;
    /**
     *
     * the type(s) of experiment preformed
     */
    private String[] experimentTypes;
    /**
     *
     * the date the project has been submitted
     */
    private String submissionDate;
    /**
     *
     * publications/references associated with the project
     */
    private Reference[] references;
    /**
     *
     * the Lab-Head or PI associated to the dataset
     */
    private ContactDetail[] labHeads;
    /**
     *
     * project meta-data: information about the sample processing
     */
    private String sampleProcessingProtocol;
    /**
     *
     * project meta-data: information about the data processing
     */
    private String dataProcessingProtocol;
    /**
     *
     * links to other datasets related to this project (if available)
     */
    private String otherOmicsLink;
    /**
     *
     * the quantification method(s) used with the dataset (if any)
     */
    private String[] quantificationMethods;
    /**
     *
     * project statistics: number of spectra
     */
    private int numSpectra;
    /**
     *
     * project statistics: number of reported peptides
     */
    private int numPeptides;
    /**
     *
     * project statistics: number of spectra
     */
    private int numUniquePeptidesnumber;
    /**
     *
     * project statistics: number of reported proteins
     */
    private int numProteins;
    /**
     *
     * project statistics: number of identified spectra
     */
    private int numIdentifiedSpectra;
    /**
     *
     * the project's accession number
     */
    private String accession;
    /**
     *
     * the tissue annotation for the project
     */
    private String[] tissues;
    /**
     *
     * the title given to the project
     */
    private String title;
    /**
     *
     * the description provided for the project
     */
    private String projectDescription;
    /**
     *
     * the number of assays associated with this project
     */
    private int numAssays;
    /**
     *
     * the type of submission(complete or partial)
     */
    private String submissionType;
    /**
     *
     * the date the project has been made public
     */
    private String publicationDate;
    /**
     *
     * specific tags added to the project for classification
     */
    private String[] projectTags;
    /**
     *
     * the species annotation for the project
     */
    private String[] species;
    /**
     *
     * the instrument annotation for the project
     */
    private String[] instrumentNames;
    /**
     *
     * the Post Translational Modifications (PTM) annotated for the project
     */
    private String[] ptmNames;

    /**
     * Creates a new ProjectDetail object
     *
     */
    public ProjectDetail() {
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
     * Sets the keywords
     *
     * @param keywords the keywords
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Returns the submitter
     *
     * @return the submitter
     */
    public ContactDetail getSubmitter() {
        return submitter;
    }

    /**
     * Sets the submitter
     *
     * @param submitter the submitter
     */
    public void setSubmitter(ContactDetail submitter) {
        this.submitter = submitter;
    }

    /**
     * Returns the DOI of the associated publication
     *
     * @return the DOI
     */
    public String getDoi() {
        return doi;
    }

    /**
     * Sets the DOI of the project
     *
     * @param doi the DOI of the project
     */
    public void setDoi(String doi) {
        this.doi = doi;
    }

    /**
     * Sets the reanalysis type
     *
     * @return the reanalysis type
     */
    public String getReanalysis() {
        return reanalysis;
    }

    /**
     * Returns the reanalysis
     *
     * @param reanalysis the reanalysis type
     */
    public void setReanalysis(String reanalysis) {
        this.reanalysis = reanalysis;
    }

    /**
     * Returns the experiment types
     *
     * @return the experiment types
     */
    public String[] getExperimentTypes() {
        return experimentTypes;
    }

    /**
     * Set the experiment types
     *
     * @param experimentTypes the experiment types
     */
    public void setExperimentTypes(String[] experimentTypes) {
        this.experimentTypes = experimentTypes;
    }

    /**
     * Returns the submission date
     *
     * @return the submission date
     */
    public String getSubmissionDate() {
        return submissionDate;
    }

    /**
     * Sets the submission date
     *
     * @param submissionDate the submission date
     */
    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    /**
     * Returns the references
     *
     * @return the references
     */
    public Reference[] getReferences() {
        return references;
    }

    /**
     * Set the references
     *
     * @param references the references
     */
    public void setReferences(Reference[] references) {
        this.references = references;
    }

    /**
     * Returns the labhead contact details
     *
     * @return the labhead contact details
     */
    public ContactDetail[] getLabHeads() {
        return labHeads;
    }

    /**
     * Set the labhead contact details
     *
     * @param labHeads the labhead contact details
     */
    public void setLabHeads(ContactDetail[] labHeads) {
        this.labHeads = labHeads;
    }

    /**
     * Returns the sample processing protocol
     *
     * @return the sample processing protocol
     */
    public String getSampleProcessingProtocol() {
        return sampleProcessingProtocol;
    }

    /**
     * Set the sample processing protocol
     *
     * @param sampleProcessingProtocol the sample processing protocol
     */
    public void setSampleProcessingProtocol(String sampleProcessingProtocol) {
        this.sampleProcessingProtocol = sampleProcessingProtocol;
    }

    /**
     * Returns the data processing protocol
     *
     * @return the data processing protocol
     */
    public String getDataProcessingProtocol() {
        return dataProcessingProtocol;
    }

    /**
     * Set the data processing protocol
     *
     * @param dataProcessingProtocol the data processing protocol
     */
    public void setDataProcessingProtocol(String dataProcessingProtocol) {
        this.dataProcessingProtocol = dataProcessingProtocol;
    }

    /**
     * Returns the link to other omics studies
     *
     * @return the link to other omics studies
     */
    public String getOtherOmicsLink() {
        return otherOmicsLink;
    }

    /**
     * Sets a link to other omics studies
     *
     * @param otherOmicsLink a link to other omics studies
     */
    public void setOtherOmicsLink(String otherOmicsLink) {
        this.otherOmicsLink = otherOmicsLink;
    }

    /**
     * Returns the quant methods
     *
     * @return the quant methods
     */
    public String[] getQuantificationMethods() {
        return quantificationMethods;
    }

    /**
     * Set the quant methods
     *
     * @param quantificationMethods the quant methods
     */
    public void setQuantificationMethods(String[] quantificationMethods) {
        this.quantificationMethods = quantificationMethods;
    }

    /**
     * Returns the number of spectra
     *
     * @return the number of spectra
     */
    public int getNumSpectra() {
        return numSpectra;
    }

    /**
     * Set the number of spectra
     *
     * @param numSpectra the number of spectra
     */
    public void setNumSpectra(int numSpectra) {
        this.numSpectra = numSpectra;
    }

    /**
     * Returns the number of peptides
     *
     * @return the number of peptides
     */
    public int getNumPeptides() {
        return numPeptides;
    }

    /**
     * Set the number of peptides
     *
     * @param numPeptides the number of peptides
     */
    public void setNumPeptides(int numPeptides) {
        this.numPeptides = numPeptides;
    }

    /**
     * Returns the number of unique peptides
     *
     * @return the number of unique peptides
     */
    public int getNumUniquePeptidesnumber() {
        return numUniquePeptidesnumber;
    }

    /**
     * Sets the number of unique peptides
     *
     * @param numUniquePeptidesnumber the number of unique peptides
     */
    public void setNumUniquePeptidesnumber(int numUniquePeptidesnumber) {
        this.numUniquePeptidesnumber = numUniquePeptidesnumber;
    }

    /**
     * Returns the number of proteins
     *
     * @return the number of proteins
     */
    public int getNumProteins() {
        return numProteins;
    }

    /**
     * Sets the number of proteins
     *
     * @param numProteins the number of proteins
     */
    public void setNumProteins(int numProteins) {
        this.numProteins = numProteins;
    }

    /**
     * Returns the number of identified spectra
     *
     * @return the number of identified spectra
     */
    public int getNumIdentifiedSpectra() {
        return numIdentifiedSpectra;
    }

    /**
     * Set the number of identified spectra
     *
     * @param numIdentifiedSpectra the number of identified spectra
     */
    public void setNumIdentifiedSpectra(int numIdentifiedSpectra) {
        this.numIdentifiedSpectra = numIdentifiedSpectra;
    }

    /**
     * Returns the accession
     *
     * @return the accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Set the accession
     *
     * @param accession the accession of the project
     */
    public void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * Returns the tissues
     *
     * @return the tissues
     */
    public String[] getTissues() {
        return tissues;
    }

    /**
     * Sets the tissues
     *
     * @param tissues the tissues
     */
    public void setTissues(String[] tissues) {
        this.tissues = tissues;
    }

    /**
     * Returns the title
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title
     *
     * @param title the title of the project
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of the project
     *
     * @return the description of the project
     */
    public String getProjectDescription() {
        return projectDescription;
    }

    /**
     * Sets the description of the project
     *
     * @param projectDescription the description of the project
     */
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    /**
     * Returns the number of assays
     *
     * @return the number of assays
     */
    public int getNumAssays() {
        return numAssays;
    }

    /**
     * Sets the number of assays
     *
     * @param numAssays the number of assays
     */
    public void setNumAssays(int numAssays) {
        this.numAssays = numAssays;
    }

    /**
     * Returns the submission type
     *
     * @return the type of submission
     */
    public String getSubmissionType() {
        return submissionType;
    }

    /**
     * Sets the type of the submission
     *
     * @param submissionType the type of the submission
     */
    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    /**
     * Returns the publication date
     *
     * @return the publication date
     */
    public String getPublicationDate() {
        return publicationDate;
    }

    /**
     * Sets the publication date
     *
     * @param publicationDate the publication date
     */
    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    /**
     * Returns the project tags
     *
     * @return the project tags
     */
    public String[] getProjectTags() {
        return projectTags;
    }

    /**
     * Sets the project tags
     *
     * @param projectTags the project tags
     */
    public void setProjectTags(String[] projectTags) {
        this.projectTags = projectTags;
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
     * Sets the species
     *
     * @param species the species
     */
    public void setSpecies(String[] species) {
        this.species = species;
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
     * Sets the instrument names
     *
     * @param instrumentNames the instrument names
     */
    public void setInstrumentNames(String[] instrumentNames) {
        this.instrumentNames = instrumentNames;
    }

    /**
     * Returns the ptm names
     *
     * @return the ptm names
     */
    public String[] getPtmNames() {
        return ptmNames;
    }

    /**
     * Sets the ptm names
     *
     * @param ptmNames the ptm names
     */
    public void setPtmNames(String[] ptmNames) {
        this.ptmNames = ptmNames;
    }

}
