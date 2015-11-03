package com.compomics.util.pride.prideobjects.webservice;

/**
 * An enum that constructs the template queries for the PRIDE webservice.
 *
 * @author Kenneth Verheggen
 */
public enum PrideQuery {

    // projects
    GET_PROJECT_BY_ACCESSION("project/{projectAccession}"),
    GET_PROJECTS_BY_CRITERIA("project/list"),
    // assay
    GET_ASSAY_BY_ACCESSION("assay/{assayAccession}"),
    GET_ASSAYS_BY_PROJECT("assay/list/project/{projectAccession}"),
    // files
    GET_FILES_BY_PROJECT("file/list/project/{projectAccession}"),
    GET_FILES_BY_ASAY("file/list/assay/{assayAccession}"),
    // protein identifications
    GET_ALL_PROTEIN_IDENTIFICATIONS_BY_PROJECT("protein/list/project/{projectAccession}"),
    GET_PROTEIN_IDENTIFICATION_BY_PROJECT("protein/list/project/{projectAccession}/protein/{accession}"),
    GET_ALL_PROTEIN_IDENTIFICATIONS_BY_ASSAY("protein/list/assay/{assayAccession}"),
    // peptide identifications
    GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT("peptide/list/project/{projectAccession}"),
    GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT_AND_SEQUENCE("peptide/list/project/{projectAccession}/sequence/{peptideSequence}"),
    GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY("peptide/list/assay/{assayAccession}"),
    GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY_AND_SEQUENCE("peptide/list/assay/{assayAccession}/sequence/{peptideSequence}");

    /**
     * The root URL to the PRIDE webservice.
     */
    private static final String ROOT_URL = "https://www.ebi.ac.uk:443/pride/ws/archive/";
    /**
     * The template of a query.
     */
    private final String queryTemplate;

    /**
     * Creates a new PrideQuery instance.
     *
     * @param queryTemplate the query template
     */
    private PrideQuery(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }

    /**
     * Returns the actual query template.
     *
     * @param count a boolean indicating if the query is to be used for counting
     * json objects (faster than iterating a list of objects)
     * @return the actual query template for a count or list
     */
    public String getQueryTemplate(boolean count) {
        String temp = ROOT_URL;
        if (count) {
            temp += queryTemplate.replace("list", "count");
        } else {
            temp += queryTemplate;
        }
        return temp;
    }
}
