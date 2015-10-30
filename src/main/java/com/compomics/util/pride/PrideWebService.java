package com.compomics.util.pride;

import com.compomics.util.io.json.marshallers.PrideMarshaller;
import com.compomics.util.pride.prideobjects.webservice.PrideQuery;
import com.compomics.util.pride.prideobjects.webservice.assay.AssayDetail;
import com.compomics.util.pride.prideobjects.webservice.assay.AssayDetailList;
import com.compomics.util.pride.prideobjects.webservice.file.FileDetailList;
import com.compomics.util.pride.prideobjects.webservice.peptide.PsmDetailList;
import com.compomics.util.pride.prideobjects.webservice.project.ProjectDetail;
import com.compomics.util.pride.prideobjects.webservice.project.projectsummary.ProjectSummaryList;
import com.compomics.util.pride.prideobjects.webservice.protein.ProteinDetailList;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;
import java.io.IOException;
import java.net.URL;

/**
 * A java representation of the PRIDE web service
 *
 * @author Kenneth Verheggen
 */
public class PrideWebService {

    /**
     * The pride json marshaller to interpret json objects
     */
    private static final PrideMarshaller marshaller = new PrideMarshaller();
    /**
     * The page size that is to be used for buffering large lists (if the query
     * supports this)
     */
    private static final int pageSize = 1000;

    /**
     * Returns the reachable query for a given root + query + pride filter
     * instances
     *
     * @param queryRoot the root for the query
     * @param query the criteria for the query
     * @param filters the filters for the query
     * @return the reachable query for a given root + query + pride filter
     * instances
     */
    private static String getQueryURL(String queryRoot, String query, PrideFilter... filters) {
        String queryURLAsString = queryRoot + "?query=" + query;
        if (filters != null && filters.length > 0) {
            for (PrideFilter aFilter : filters) {
                queryURLAsString += aFilter.getType().toString() + "=" + aFilter.getValue().replace(" ", "%20");
            }
        }
        return queryURLAsString;
    }

    /**
     * Returns a count for the given query
     *
     * @param query the query that delivers objects that should be counted
     * @return the count for the given query
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    private static int getCount(String query) throws IOException {
        return (Integer) marshaller.fromJson(Integer.class, new URL(query.replace("list", "count")));
    }

    //project info  
    /**
     * Returns a ProjectSummaryList for the given query and filters
     *
     * @param query the criteria for a search
     * @param filters the filters for a search
     * @return a ProjectSummaryList for the given query and filters
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static ProjectSummaryList getProjectSummaryList(String query, PrideFilter... filters) throws IOException {
        ProjectSummaryList summaryList = new ProjectSummaryList();
        String projectListURL = getQueryURL(PrideQuery.GET_PROJECTS_BY_CRITERIA.getQueryTemplate(false), query, filters);
        //buffer the results
        boolean moreResults = true;
        int page = 0;
        while (moreResults) {
            ProjectSummaryList subSummary = marshaller.getProjectSummaryList(projectListURL + "&page=" + page);
            if (subSummary == null || subSummary.getList().isEmpty()) {
                moreResults = false;
            } else {
                summaryList.getList().addAll(subSummary.getList());
            }
            page++;
        }
        return summaryList;
    }

    /**
     * Returns a project count for the given criteria and filters
     *
     * @param query the criteria for the search
     * @param filters the filters for the search
     * @return a project count for the given criteria and filters
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getProjectCount(String query, PrideFilter... filters) throws IOException {
        return getCount(getQueryURL(PrideQuery.GET_PROJECTS_BY_CRITERIA.getQueryTemplate(true), query, filters));
    }

    /**
     * Returns the project details for a given accession
     *
     * @param projectAccession the project accession
     * @return the project details for a given accession
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static ProjectDetail getProjectDetail(String projectAccession) throws IOException {
        URL URL = new URL(PrideQuery.GET_PROJECT_BY_ACCESSION.getQueryTemplate(false).replace("{projectAccession}", projectAccession));
        return (ProjectDetail) marshaller.fromJson(ProjectDetail.class, URL);
    }

    //assay info
    /**
     * Returns the assay details for a given project
     *
     * @param projectAccession the project accession
     * @return the assay details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static AssayDetailList getAssayDetails(String projectAccession) throws IOException {
        //assays
        String assayListURL = PrideQuery.GET_ASSAYS_BY_PROJECT.getQueryTemplate(false).replace("{projectAccession}", projectAccession);
        AssayDetailList assayDetails = (AssayDetailList) marshaller.getAssayDetailList(assayListURL);
        return assayDetails;
    }

    /**
     * Returns the assay count for a given project
     *
     * @param projectAccession the project accession
     * @return the assay count for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getAssayCount(String projectAccession) throws IOException {
        return getCount(PrideQuery.GET_ASSAYS_BY_PROJECT.getQueryTemplate(true).replace("{projectAccession}", projectAccession));
    }

    /**
     * Returns the assay details for a given assay
     *
     * @param assayAccession the project assayAccession
     * @return the assay details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static AssayDetail getAssayDetail(String assayAccession) throws IOException {
        URL URL = new URL(PrideQuery.GET_ASSAY_BY_ACCESSION.getQueryTemplate(false).replace("{assayAccession}", assayAccession));
        return (AssayDetail) marshaller.fromJson(AssayDetail.class, URL);
    }

    //file info
    /**
     * Returns a list of file details for a given project
     *
     * @param projectAccession the project accession
     * @return a list of file details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static FileDetailList getProjectFileDetails(String projectAccession) throws IOException {
        String fileListURL = PrideQuery.GET_FILES_BY_PROJECT.getQueryTemplate(false).replace("{projectAccession}", projectAccession);
        FileDetailList files = marshaller.getFileDetailList(fileListURL);
        return files;
    }

    /**
     * Returns a count of file details for a given project
     *
     * @param projectAccession the project accession
     * @return a count of file details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getProjectFileCount(String projectAccession) throws IOException {
        return getCount(PrideQuery.GET_FILES_BY_PROJECT.getQueryTemplate(true).replace("{projectAccession}", projectAccession));
    }

    /**
     * Returns a list of file details for a given assay
     *
     * @param assayAccession the assay accession
     * @return a list of file details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static FileDetailList getAssayFileDetails(String assayAccession) throws IOException {
        String fileListURL = PrideQuery.GET_FILES_BY_ASAY.getQueryTemplate(false).replace("{assayAccession}", assayAccession);
        FileDetailList fileDetails = marshaller.getFileDetailList(fileListURL);
        return fileDetails;
    }

    /**
     * Returns a count of file details for a given assay
     *
     * @param assayAccession the assay accession
     * @return a count of file details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getAssayFileCount(String assayAccession) throws IOException {
        return getCount(PrideQuery.GET_FILES_BY_ASAY.getQueryTemplate(false).replace("{assayAccession}", assayAccession));
    }

    //protein info
    /**
     * Returns a list of proteins, gotten from the paged query
     *
     * @param queryURL the root query url
     * @return a list of proteins, gotten from the paged query
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    private static ProteinDetailList bufferProteinIdentifications(String queryURL) throws IOException {
        ProteinDetailList proteins = new ProteinDetailList();
        //buffer the results
        boolean moreResults = true;
        int page = 0;
        while (moreResults) {
            ProteinDetailList subList = (ProteinDetailList) marshaller.getProteinDetailList(queryURL + "?show=" + pageSize + "&page=" + page);
            if (subList.getList().isEmpty()) {
                moreResults = false;
            } else {
                proteins.getList().addAll(subList.getList());
            }
            page++;
        }
        return proteins;
    }

    /**
     * Returns a list of protein details for a given project
     *
     * @param projectAccession the project accession
     * @return a list of protein details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static ProteinDetailList getProteinIdentificationByProject(String projectAccession) throws IOException {
        String proteinIdentificationList = PrideQuery.GET_ALL_PROTEIN_IDENTIFICATIONS_BY_PROJECT.getQueryTemplate(false).replace("{projectAccession}", projectAccession);
        return bufferProteinIdentifications(proteinIdentificationList);
    }

    /**
     * Returns a count of protein details for a given project
     *
     * @param projectAccession the project accession
     * @return a count of protein details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getProteinIdentificationCountByProject(String projectAccession) throws IOException {
        return getCount(PrideQuery.GET_ALL_PROTEIN_IDENTIFICATIONS_BY_PROJECT.getQueryTemplate(true).replace("{projectAccession}", projectAccession));
    }

    /**
     * Returns a list of protein details for a given project and a given protein
     * accession
     *
     * @param projectAccession the project accession
     * @param proteinAccesion the protein accession
     *
     * @return a list of protein details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static ProteinDetailList getProteinIdentificationsByProjectAndProtein(String projectAccession, String proteinAccesion) throws IOException {
        String proteinIdentificationList = PrideQuery.GET_PROTEIN_IDENTIFICATION_BY_PROJECT.getQueryTemplate(false)
                .replace("{projectAccession}", projectAccession)
                .replace("{accession}", proteinAccesion);
        return (ProteinDetailList) marshaller.getProteinDetailList(proteinIdentificationList);

    }

    /**
     * Returns a count of protein details for a given project and a given
     * protein accession
     *
     * @param projectAccession the project accession
     * @param proteinAccession the protein accession
     *
     * @return a count of protein details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getProteinIdentificationsCountByProjectAndProtein(String projectAccession, String proteinAccession) throws IOException {
        String proteinIdentificationList = PrideQuery.GET_PROTEIN_IDENTIFICATION_BY_PROJECT.getQueryTemplate(true)
                .replace("{projectAccession}", projectAccession)
                .replace("{accession}", proteinAccession);
        return getCount(proteinIdentificationList);
    }

    /**
     * Returns a list of protein details for a given assay
     *
     * @param assayAccession the assay accession
     * @return a list of protein details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static ProteinDetailList getProteinIdentificationByAssay(String assayAccession) throws IOException {
        String proteinIdentificationList = PrideQuery.GET_ALL_PROTEIN_IDENTIFICATIONS_BY_ASSAY.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession);
        return (ProteinDetailList) marshaller.getProteinDetailList(proteinIdentificationList);
    }

    /**
     * Returns a count of protein details for a given assay
     *
     * @param assayAccession the assay accession
     * @return a count of protein details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getProteinIdentificationCountByAssay(String assayAccession) throws IOException {
        String proteinIdentificationList = PrideQuery.GET_ALL_PROTEIN_IDENTIFICATIONS_BY_ASSAY.getQueryTemplate(true)
                .replace("{assayAccession}", assayAccession);
        return getCount(proteinIdentificationList);
    }

    //peptide info
    /**
     * Returns a list of peptide to spectrum matches, gotten from the paged
     * query
     *
     * @param queryURL the root query url
     * @return a list of peptide to spectrum matches, gotten from the paged
     * query
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    private static PsmDetailList bufferPSMs(String queryURL) throws IOException {
        PsmDetailList psms = new PsmDetailList();
        //buffer the results
        boolean moreResults = true;
        int page = 0;
        while (moreResults) {
            PsmDetailList subList = (PsmDetailList) marshaller.getPeptideDetailList(queryURL + "?show=" + pageSize + "&page=" + page);
            if (subList.getList().isEmpty()) {
                moreResults = false;
            } else {
                psms.getList().addAll(subList.getList());
            }
            page++;
        }
        return psms;
    }

    /**
     * Returns a list of psm details for a given project
     *
     * @param projectAccession the project accession
     * @return a list of protein details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static PsmDetailList getPSMsByProject(String projectAccession) throws IOException {
        //assays
        String peptideIdentificationList = PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT.getQueryTemplate(false)
                .replace("{projectAccession}", projectAccession);
        return bufferPSMs(peptideIdentificationList);
    }

    /**
     * Returns a count of psm details for a given project
     *
     * @param projectAccession the project accession
     * @return a count of psm details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getPSMCountByProject(String projectAccession) throws IOException {
        return getCount(PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT.getQueryTemplate(true)
                .replace("{projectAccession}", projectAccession));
    }

    /**
     * Returns a list of psm details for a given project and a given peptide
     * sequence
     *
     * @param projectAccession the project accession
     * @param sequence the peptide sequence
     * @return a list of psm details for a given project and a given peptide
     * sequence
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static PsmDetailList getPSMsByProjectAndSequence(String projectAccession, String sequence) throws IOException {
        //assays
        String peptideIdentificationList = PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT_AND_SEQUENCE.getQueryTemplate(false)
                .replace("{projectAccession}", projectAccession)
                .replace("{peptideSequence}", sequence);
        return (PsmDetailList) marshaller.getPeptideDetailList(peptideIdentificationList);
    }

    /**
     * Returns a count of psm details for a given project and a given peptide
     * sequence
     *
     * @param projectAccession the project accession
     * @param sequence the peptide sequence
     * @return a count of psm details for a given project and a given peptide
     * sequence
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getPSMCountByProjectAndSequence(String projectAccession, String sequence) throws IOException {
        return getCount(PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT_AND_SEQUENCE.getQueryTemplate(true)
                .replace("{projectAccession}", projectAccession)
                .replace("{peptideSequence}", sequence));
    }

    /**
     * Returns a list of psm details for a given assay
     *
     * @param assayAccession the project accession
     * @return a list of psm details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static PsmDetailList getPSMsByAssay(String assayAccession) throws IOException {
        //assays
        String peptideIdentificationList = PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession);
        return bufferPSMs(peptideIdentificationList);
    }

    /**
     * Returns a count of psm details for a given assay
     *
     * @param assayAccession the project accession
     * @return a count of psm details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getPSMCountByAssay(String assayAccession) throws IOException {
        return getCount(PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession));
    }

    /**
     * Returns a list of psm details for a given assay and a given peptide
     * sequence
     *
     * @param assayAccession the project accession
     * @param sequence the peptide sequence
     * @return a list of psm details for a given assay and a given peptide
     * sequence
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static PsmDetailList getPSMsByAssayAndSequence(String assayAccession, String sequence) throws IOException {
        //assays
        String peptideIdentificationList = PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY_AND_SEQUENCE.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession)
                .replace("{peptideSequence}", sequence);
        return (PsmDetailList) marshaller.getPeptideDetailList(peptideIdentificationList);
    }

    /**
     * Returns a count of psm details for a given assay and a given peptide
     * sequence
     *
     * @param assayAccession the project accession
     * @param sequence the peptide sequence
     * @return a count of psm details for a given assay and a given peptide
     * sequence
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getPSMCountByAssayAndSequence(String assayAccession, String sequence) throws IOException {
        return getCount(PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY_AND_SEQUENCE.getQueryTemplate(true)
                .replace("{assayAccession}", assayAccession)
                .replace("{peptideSequence}", sequence));
    }
}
