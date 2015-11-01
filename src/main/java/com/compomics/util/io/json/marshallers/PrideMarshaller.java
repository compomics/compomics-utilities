package com.compomics.util.io.json.marshallers;

import com.compomics.util.io.json.JsonMarshaller;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetailList;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectDetail;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummaryList;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetailList;

/**
 * A json marshaller for use with the PRIDE webservice
 *
 * @author Kenneth Verheggen
 */
public class PrideMarshaller extends JsonMarshaller {

    /**
     * Create a PRIDE marshaller
     */
    public PrideMarshaller() {
        super();
    }

    @Override
    protected void init() {
        super.init();
        builder.setDateFormat("YYYY-MM-DD");
    }

    /**
     * Convert from JSON to a list of ProjectDetails
     *
     * @param jsonURL the URL for the JSON object
     * @return an instance of the objectType containing the JSON information
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public ProjectDetail getProjectDetail(String jsonURL) throws IOException {
        return (ProjectDetail) gson.fromJson(new InputStreamReader(new URL(jsonURL).openStream()), ProjectDetail.class);
    }

    /**
     * Convert from JSON to a list of ProjectDetails
     *
     * @param jsonURL the URL for the JSON object
     * @return an instance of the objectType containing the JSON information
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public ProjectSummaryList getProjectSummaryList(String jsonURL) throws IOException {
        return (ProjectSummaryList) gson.fromJson(new InputStreamReader(new URL(jsonURL).openStream()), ProjectSummaryList.class);
    }

    /**
     * Convert from JSON to a list of AssaytDetails
     *
     * @param jsonURL the URL for the JSON object
     * @return an instance of the objectType containing the JSON information
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public AssayDetailList getAssayDetailList(String jsonURL) throws IOException {
        return (AssayDetailList) gson.fromJson(new InputStreamReader(new URL(jsonURL).openStream()), AssayDetailList.class);
    }

    /**
     * Convert from JSON to a list of FileDetails
     *
     * @param jsonURL the URL for the JSON object
     * @return an instance of the objectType containing the JSON information
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public FileDetailList getFileDetailList(String jsonURL) throws IOException {
        return (FileDetailList) gson.fromJson(new InputStreamReader(new URL(jsonURL).openStream()), FileDetailList.class);
    }

    /**
     * Convert from JSON to a list of ProteinDetails
     *
     * @param jsonURL the URL for the JSON object
     * @return an instance of the objectType containing the JSON information
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public ProteinDetailList getProteinDetailList(String jsonURL) throws IOException {
        return (ProteinDetailList) gson.fromJson(new InputStreamReader(new URL(jsonURL).openStream()), ProteinDetailList.class);
    }

    /**
     * Convert from JSON to a list of ProteinDetails
     *
     * @param jsonURL the URL for the JSON object
     * @return an instance of the objectType containing the JSON information
     * @throws IOException thrown if the webservice is down or the json response
     * was invalid
     */
    public PsmDetailList getPeptideDetailList(String jsonURL) throws IOException {
        return (PsmDetailList) gson.fromJson(new InputStreamReader(new URL(jsonURL).openStream()), PsmDetailList.class);
    }

}
