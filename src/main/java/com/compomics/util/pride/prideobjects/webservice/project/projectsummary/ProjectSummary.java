package com.compomics.util.pride.prideobjects.webservice.project.projectsummary;

/**
 * The PRIDE ProjectSummary object
 *
 * @author Kenneth Verheggen
 */
public class ProjectSummary {

    /**
     * the project's accession number
     */
    private String accession;
    /**
     * the tissue annotation for the project
     */
    private String[] tissues;
    /**
     * the title given to the project
     */
    private String title;
    /**
     * the description provided for the project
     */
    private String projectDescription;
    /**
     * the number of assays associated with this project
     */
    private int numAssays;
    /**
     * the type of submission (complete or partial)
     */
    private String submissionType;
    /**
     * the date the project has been made public
     */
    private String publicationDate;
    /**
     * specific tags added to the project for classification
     */
    private String[] projectTags;
    /**
     * the species annotation for the project
     */
    private String[] species;
    /**
     * the instrument annotation for the project
     */
    private String[] instrumentNames;
    /**
     * the Post Translational Modifications (PTM) annotated for the project
     */
    private String[] ptmNames;

    /**
     * Creates a new SearchEngineScore object
     *
     */
    public ProjectSummary() {
    }

    /**
     * Returns the project accession
     *
     * @return the project accession
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Set the project accession
     *
     * @param accession the project accession
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
     * Set the tissues
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
     * Set the title
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the project description
     *
     * @return the project description
     */
    public String getProjectDescription() {
        return projectDescription;
    }

    /**
     * Set the project description
     *
     * @param projectDescription the project description
     */
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    /**
     * Returns the number of assays for this project
     *
     * @return the number of assays for this project
     */
    public int getNumAssays() {
        return numAssays;
    }

    /**
     * Set the number of assays for this project
     *
     * @param numAssays the number of assays for this project
     */
    public void setNumAssays(int numAssays) {
        this.numAssays = numAssays;
    }

    /**
     * Returns the submission type
     *
     * @return the submission type
     */
    public String getSubmissionType() {
        return submissionType;
    }

    /**
     * Set the submission type
     *
     * @param submissionType the submission type
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
     * Set the publication date
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
     * Set the project tags
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
     * Set the species
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
     * Set the instrument names
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
     * Set the ptm names
     *
     * @param ptmNames the ptm names
     */
    public void setPtmNames(String[] ptmNames) {
        this.ptmNames = ptmNames;
    }

}
