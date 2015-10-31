package com.compomics.util.pride.prideobjects.webservice.file;

/**
 * The PRIDE FileDetail object
 *
 * @author Kenneth Verheggen
 */
public class FileDetail {

    /**
     * the name of the file*
     */
    private String fileName;
    /**
     * the accession of the project
     */
    private String projectAccession;
    /**
     * The type of the file ['RESULT' or 'PEAK' or 'SEARCH' or 'RAW' or 'QUANT'
     * or 'GEL' or 'FASTA' or 'SPECTRUM_LIBRARY' or 'MS_IMAGE_DATA' or
     * 'OPTICAL_IMAGE' or 'OTHER']: *
     */
    private String fileType;
    /**
     * The source of the file SUBMITTED (part of the original dataset) or
     * GENERATED (added to the submission by PRIDE)*
     */
    private String fileSource;
    /**
     * size in bytes
     */
    private int fileSize;
    /**
     * the assay accession
     */
    private String assayAccession;
    /**
     * public FTP download link String downloadLink;
     */
    private String downloadLink;

    /**
     * Creates a new FileDetail object
     *
     */
    public FileDetail() {
    }

    /**
     * Returns the filename
     *
     * @return the filename
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the filename
     *
     * @param fileName the filename
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
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
     * Set the project accession
     *
     * @param projectAccession the project accession
     */
    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    /**
     * Returns the file type
     *
     * @return the file type
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Set the file type
     *
     * @param fileType the file type to set
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * Returns the file source
     *
     * @return the file source
     */
    public String getFileSource() {
        return fileSource;
    }

    /**
     * Set the file source
     *
     * @param fileSource the file source to set
     */
    public void setFileSource(String fileSource) {
        this.fileSource = fileSource;
    }

    /**
     * Returns the file size in bytes
     *
     * @return the file size in bytes
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * Set the file size in bytes
     *
     * @param fileSize the file size in bytes
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
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
     * Set the assay accession
     *
     * @param assayAccession the assay accession
     */
    public void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
    }

    /**
     * Returns the downloadlink
     *
     * @return the downloadlink
     */
    public String getDownloadLink() {
        return downloadLink;
    }

    /**
     * Set the downloadlink
     *
     * @param downloadLink the downloadlink to set
     */
    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

}
