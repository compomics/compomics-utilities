package com.compomics.util.pride.prideobjects.webservice.peptide;

/**
 * The PRIDE PsmDetail object
 *
 * @author Kenneth Verheggen
 */
public class PsmDetail {

    /**
     * a unique ID assigned by PRIDE to this peptide identification
     */
    private String id;
    /**
     * the project in which this peptide was reported
     */
    private String projectAccession;
    /**
     * modifications reported for this peptide
     */
    private ModificationLocation[] modifications;
    /**
     * The assay in which this peptide was reported
     */
    private String assayAccession;
    /**
     * the peptide sequence identified
     */
    private String sequence;
    /**
     * the reported accession of the protein containing the peptide
     */
    private String proteinAccession;
    /**
     * the search engine(s) used to generate the identification
     */
    private String[] searchEngines;
    /**
     * the scores reported by the search engine(s) represented as key - value
     * pairs
     */
    private SearchEngineScore[] searchEngineScores;
    /**
     * the reported retention time
     */
    private Number retentionTime;
    /**
     * the reported charge
     */
    private int charge;
    /**
     * the reported position on the protein where the peptide starts
     */
    private int startPosition;
    /**
     * the reported position on the protein where the peptide ends
     */
    private int endPosition;
    /**
     * the experimental/reported mz value of the peptide
     */
    private Number experimentalMZ;
    /**
     * reported amino acid preceding the peptide
     */
    private String preAA;
    /**
     * reported amino acid following the peptide
     */
    private String postAA;
    /**
     * the ID of the spectrum used for the identification
     */
    private String spectrumID;
    /**
     * the reported ID of this peptide identification (usually but not
     * necessarily unique to an assay)
     */
    private String reportedID;
    /**
     * the theoretical mz value of the peptide Number calculatedMZ;
     */
    private Number calculatedMZ;

    /**
     * Creates a new PsmDetail object
     *
     */
    public PsmDetail() {
    }

    /**
     * Returns the pride id for this psm
     *
     * @return the pride id for this psm
     */
    public String getId() {
        return id;
    }

    /**
     * Set the pride id for this psm
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
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
     * Set the accession of the project
     *
     * @param projectAccession the accession of the project
     */
    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    /**
     * Returns the modifications
     *
     * @return the modifications
     */
    public ModificationLocation[] getModifications() {
        return modifications;
    }

    /**
     * Set the modifications of the psm
     *
     * @param modifications the modifications
     */
    public void setModifications(ModificationLocation[] modifications) {
        this.modifications = modifications;
    }

    /**
     * Returns the assay of the psm
     *
     * @return the assay of the psm
     */
    public String getAssayAccession() {
        return assayAccession;
    }

    /**
     * Set the assay of the psm
     *
     * @param assayAccession the assay of the psm
     */
    public void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
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
     * Set the sequence of the psm
     *
     * @param sequence the sequence of the psm
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the accession of the parent protein
     *
     * @return the accession of the parent protein
     */
    public String getProteinAccession() {
        return proteinAccession;
    }

    /**
     * Set the protein accession of the parent protein
     *
     * @param proteinAccession the protein accession of the parent protein
     */
    public void setProteinAccession(String proteinAccession) {
        this.proteinAccession = proteinAccession;
    }

    /**
     * Returns the search engines
     *
     * @return the search engines
     */
    public String[] getSearchEngines() {
        return searchEngines;
    }

    /**
     * Set the search engines that were used
     *
     * @param searchEngines the search engines that were used
     */
    public void setSearchEngines(String[] searchEngines) {
        this.searchEngines = searchEngines;
    }

    /**
     * Returns the search engine scores for the psm
     *
     * @return the search engine scores for the psm
     */
    public SearchEngineScore[] getSearchEngineScores() {
        return searchEngineScores;
    }

    /**
     * Set the search engine scores for the psm
     *
     * @param searchEngineScores the search engine scores for the psm
     */
    public void setSearchEngineScores(SearchEngineScore[] searchEngineScores) {
        this.searchEngineScores = searchEngineScores;
    }

    /**
     * Returns the retention time for the psm
     *
     * @return the retention time for the psm
     */
    public Number getRetentionTime() {
        return retentionTime;
    }

    /**
     * Set the retention time for the psm
     *
     * @param retentionTime the retention time for the psm
     */
    public void setRetentionTime(Number retentionTime) {
        this.retentionTime = retentionTime;
    }

    /**
     * Returns the calculated mz
     *
     * @return the calculated mz
     */
    public Number getCalculatedMZ() {
        return calculatedMZ;
    }

    /**
     * Set the calculated mz
     *
     * @param calculatedMZ the calculated mz
     */
    public void setCalculatedMZ(Number calculatedMZ) {
        this.calculatedMZ = calculatedMZ;
    }

    /**
     * Returns the charge of the psm
     *
     * @return the charge of the psm
     */
    public int getCharge() {
        return charge;
    }

    /**
     * Set the charge of the psm
     *
     * @param charge the charge of the psm
     */
    public void setCharge(int charge) {
        this.charge = charge;
    }

    /**
     * Returns the starting position of the psm in the protein
     *
     * @return the starting position of the psm in the protein
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * Set the starting position of the psm in the protein
     *
     * @param startPosition the starting position of the psm in the protein
     */
    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * Returns the ending position of the psm in the protein
     *
     * @return the ending position of the psm in the protein
     */
    public int getEndPosition() {
        return endPosition;
    }

    /**
     * Set the ending position of the psm in the protein
     *
     * @param endPosition the ending position of the psm in the protein
     */
    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    /**
     * Returns the experimental mass to charge ratio
     *
     * @return the experimental mass to charge ratio
     */
    public Number getExperimentalMZ() {
        return experimentalMZ;
    }

    /**
     * Set the experimental mass to charge ratio
     *
     * @param experimentalMZ the experimental mass to charge ratio
     */
    public void setExperimentalMZ(Number experimentalMZ) {
        this.experimentalMZ = experimentalMZ;
    }

    /**
     * Returns the amino acids before the peptide
     *
     * @return the amino acids before the peptide
     */
    public String getPreAA() {
        return preAA;
    }

    /**
     * Set the amino acids before the peptide
     *
     * @param preAA the amino acids before the peptide
     */
    public void setPreAA(String preAA) {
        this.preAA = preAA;
    }

    /**
     * the amino acids after the peptide
     *
     * @return the amino acids after the peptide
     */
    public String getPostAA() {
        return postAA;
    }

    /**
     * Set the amino acids after the peptide
     *
     * @param postAA the amino acids after the peptide
     */
    public void setPostAA(String postAA) {
        this.postAA = postAA;
    }

    /**
     * Returns the ID of the spectrum
     *
     * @return the ID of the spectrum
     */
    public String getSpectrumID() {
        return spectrumID;
    }

    /**
     * Set the ID of the spectrum
     *
     * @param spectrumID the ID of the spectrum
     */
    public void setSpectrumID(String spectrumID) {
        this.spectrumID = spectrumID;
    }

    /**
     * Returns the reported ID
     *
     * @return the reported ID
     */
    public String getReportedID() {
        return reportedID;
    }

    /**
     * Set the reported ID
     *
     * @param reportedID the reported ID
     */
    public void setReportedID(String reportedID) {
        this.reportedID = reportedID;
    }

}
