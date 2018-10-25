package com.compomics.util.pride;

import com.compomics.util.io.json.marshallers.PrideMarshaller;
import com.compomics.util.pride.prideobjects.webservice.PrideQuery;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;

import java.io.IOException;
import java.net.URL;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetailList;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectDetail;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummaryList;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetailList;

/**
 * A java representation of the PRIDE web service.
 *
 * @author Kenneth Verheggen
 */
public class PrideWebService {

    /**
     * Empty default constructor
     */
    public PrideWebService() {
    }

    /**
     * The pride json marshaller to interpret json objects.
     */
    private static final PrideMarshaller MARSHALLER = new PrideMarshaller();
    /**
     * The page size that is to be used for buffering large lists (if the query
     * supports this).
     */
    private static final int PAGE_SIZE = 1000;

    /**
     * Returns the reachable query for a given root + query + pride filter
     * instances.
     *
     * @param queryRoot the root for the query
     * @param query the criteria for the query
     * @param filters the filters for the query
     * @return the reachable query for a given root + query + pride filter
     * instances
     */
    private static String getQueryURL(String queryRoot, String query, PrideFilter... filters) {
        String queryURLAsString = queryRoot + "?";
        if (!query.isEmpty()) {
            queryURLAsString = queryURLAsString + "query=" + query;
        }
        if (filters != null && filters.length > 0) {
            for (PrideFilter aFilter : filters) {
                queryURLAsString += "&" + aFilter.getType().toString() + "=" + aFilter.getValue().replace(" ", "%20");
            }
        }
        System.out.println(queryURLAsString);
        return queryURLAsString;
    }

    /**
     * Returns a count for the given query.
     *
     * @param query the query that delivers objects that should be counted
     * @return the count for the given query
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    private static int getCount(String query) throws IOException {
        return (Integer) MARSHALLER.fromJson(Integer.class, new URL(query.replace("list", "count")));
    }

    /**
     * Returns a ProjectSummaryList for the given query and filters.
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
            ProjectSummaryList subSummary = MARSHALLER.getProjectSummaryList(projectListURL + "&page=" + page);
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
     * Returns a project count for the given criteria and filters.
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
     * Returns the project details for a given accession.
     *
     * @param projectAccession the project accession
     * @return the project details for a given accession
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static ProjectDetail getProjectDetail(String projectAccession) throws IOException {
        URL URL = new URL(PrideQuery.GET_PROJECT_BY_ACCESSION.getQueryTemplate(false).replace("{projectAccession}", projectAccession));
        return (ProjectDetail) MARSHALLER.fromJson(ProjectDetail.class, URL);
    }

    /**
     * Returns the assay details for a given project.
     *
     * @param projectAccession the project accession
     * @return the assay details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static AssayDetailList getAssayDetails(String projectAccession) throws IOException {
        //assays
        String assayListURL = PrideQuery.GET_ASSAYS_BY_PROJECT.getQueryTemplate(false).replace("{projectAccession}", projectAccession);
        AssayDetailList assayDetails = (AssayDetailList) MARSHALLER.getAssayDetailList(assayListURL);
        return assayDetails;
    }

    /**
     * Returns the assay count for a given project.
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
     * Returns the assay details for a given assay.
     *
     * @param assayAccession the project assayAccession
     * @return the assay details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static AssayDetail getAssayDetail(String assayAccession) throws IOException {
        URL URL = new URL(PrideQuery.GET_ASSAY_BY_ACCESSION.getQueryTemplate(false).replace("{assayAccession}", assayAccession));
        return (AssayDetail) MARSHALLER.fromJson(AssayDetail.class, URL);
    }

    /**
     * Returns a list of file details for a given project.
     *
     * @param projectAccession the project accession
     * @return a list of file details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static FileDetailList getProjectFileDetails(String projectAccession) throws IOException {
        String fileListURL = PrideQuery.GET_FILES_BY_PROJECT.getQueryTemplate(false).replace("{projectAccession}", projectAccession);
        FileDetailList files = MARSHALLER.getFileDetailList(fileListURL);
        return files;
    }

    /**
     * Returns a count of file details for a given project.
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
     * Returns a list of file details for a given assay.
     *
     * @param assayAccession the assay accession
     * @return a list of file details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static FileDetailList getAssayFileDetails(String assayAccession) throws IOException {
        String fileListURL = PrideQuery.GET_FILES_BY_ASAY.getQueryTemplate(false).replace("{assayAccession}", assayAccession);
        FileDetailList fileDetails = MARSHALLER.getFileDetailList(fileListURL);
        return fileDetails;
    }

    /**
     * Returns a count of file details for a given assay.
     *
     * @param assayAccession the assay accession
     * @return a count of file details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getAssayFileCount(String assayAccession) throws IOException {
        return getCount(PrideQuery.GET_FILES_BY_ASAY.getQueryTemplate(false).replace("{assayAccession}", assayAccession));
    }

    /**
     * Returns a list of proteins, gotten from the paged query.
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
            ProteinDetailList subList = (ProteinDetailList) MARSHALLER.getProteinDetailList(queryURL + "?show=" + PAGE_SIZE + "&page=" + page);
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
     * Returns a list of protein details for a given project.
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
     * Returns a count of protein details for a given project.
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
     * accession.
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
        return (ProteinDetailList) MARSHALLER.getProteinDetailList(proteinIdentificationList);

    }

    /**
     * Returns a count of protein details for a given project and a given
     * protein accession.
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
     * Returns a list of protein details for a given assay.
     *
     * @param assayAccession the assay accession
     * @return a list of protein details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static ProteinDetailList getProteinIdentificationByAssay(String assayAccession) throws IOException {
        String proteinIdentificationList = PrideQuery.GET_ALL_PROTEIN_IDENTIFICATIONS_BY_ASSAY.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession);
        return (ProteinDetailList) MARSHALLER.getProteinDetailList(proteinIdentificationList);
    }

    /**
     * Returns a count of protein details for a given assay.
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

    /**
     * Returns a list of peptide to spectrum matches, gotten from the paged
     * query.
     *
     * @param queryURL the root query URL
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
            PsmDetailList subList = (PsmDetailList) MARSHALLER.getPeptideDetailList(queryURL + "?show=" + PAGE_SIZE + "&page=" + page);
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
     * Returns a list of PSM details for a given project.
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
     * Returns a count of PSM details for a given project.
     *
     * @param projectAccession the project accession
     * @return a count of PSM details for a given project
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getPSMCountByProject(String projectAccession) throws IOException {
        return getCount(PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT.getQueryTemplate(true)
                .replace("{projectAccession}", projectAccession));
    }

    /**
     * Returns a list of PSM details for a given project and a given peptide
     * sequence.
     *
     * @param projectAccession the project accession
     * @param sequence the peptide sequence
     * @return a list of PSM details for a given project and a given peptide
     * sequence
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static PsmDetailList getPSMsByProjectAndSequence(String projectAccession, String sequence) throws IOException {
        //assays
        String peptideIdentificationList = PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_PROJECT_AND_SEQUENCE.getQueryTemplate(false)
                .replace("{projectAccession}", projectAccession)
                .replace("{peptideSequence}", sequence);
        return (PsmDetailList) MARSHALLER.getPeptideDetailList(peptideIdentificationList);
    }

    /**
     * Returns a count of PSM details for a given project and a given peptide
     * sequence.
     *
     * @param projectAccession the project accession
     * @param sequence the peptide sequence
     * @return a count of PSM details for a given project and a given peptide
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
     * Returns a list of PSM details for a given assay.
     *
     * @param assayAccession the project accession
     * @return a list of PSM details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static PsmDetailList getPSMsByAssay(String assayAccession) throws IOException {
        String peptideIdentificationList = PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession);
        return bufferPSMs(peptideIdentificationList);
    }

    /**
     * Returns a count of PSM details for a given assay.
     *
     * @param assayAccession the project accession
     * @return a count of PSM details for a given assay
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static int getPSMCountByAssay(String assayAccession) throws IOException {
        return getCount(PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession));
    }

    /**
     * Returns a list of PSM details for a given assay and a given peptide
     * sequence.
     *
     * @param assayAccession the project accession
     * @param sequence the peptide sequence
     * @return a list of PSM details for a given assay and a given peptide
     * sequence
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public static PsmDetailList getPSMsByAssayAndSequence(String assayAccession, String sequence) throws IOException {
        String peptideIdentificationList = PrideQuery.GET_ALL_PEPTIDE_IDENTIFICATIONS_BY_ASSAY_AND_SEQUENCE.getQueryTemplate(false)
                .replace("{assayAccession}", assayAccession)
                .replace("{peptideSequence}", sequence);
        return (PsmDetailList) MARSHALLER.getPeptideDetailList(peptideIdentificationList);
    }

    /**
     * Returns a count of PSM details for a given assay and a given peptide
     * sequence.
     *
     * @param assayAccession the project accession
     * @param sequence the peptide sequence
     * @return a count of PSM details for a given assay and a given peptide
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
