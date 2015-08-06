package com.compomics.util.experiment;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.FragmentationMethod;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Generic class gathering information on a shotgun proteomics protocol.
 *
 * @author Marc Vaudel
 */
public class ShotgunProtocol implements Serializable {

    /**
     * The enzyme used for digestion.
     */
    private Enzyme enzyme;
    /**
     * The PTMs enriched for.
     */
    private ArrayList<String> enrichedPtms;
    /**
     * The PTMs resulting of a labeling in a map: name of the PTM - boolean
     * indicating whether the labeling is complete.
     */
    private HashMap<String, Boolean> labellingPtms;
    /**
     * The MS1 resolution.
     */
    private Double ms1Resolution;
    /**
     * Boolean indicating whether the MS1 resolution is in ppm.
     */
    private boolean ms1ResolutionPpm;
    /**
     * The fragmentation method used.
     */
    private FragmentationMethod fragmentationMethod;
    /**
     * The MS2 resolution.
     */
    private Double ms2Resolution;
    /**
     * Boolean indicating whether the MS2 resolution is in ppm.
     */
    private boolean ms2ResolutionPpm;

    /**
     * Returns the enzyme used for digestion.
     *
     * @return the enzyme used for digestion
     */
    public Enzyme getEnzyme() {
        return enzyme;
    }

    /**
     * Sets the enzyme used for digestion.
     *
     * @param enzyme the enzyme used for digestion
     */
    public void setEnzyme(Enzyme enzyme) {
        this.enzyme = enzyme;
    }

    /**
     * Returns the PTMs used for enrichment. Null if no enrichment.
     *
     * @return the PTMs used for enrichment
     */
    public ArrayList<String> getEnrichedPtms() {
        return enrichedPtms;
    }

    /**
     * Sets the PTMs used for enrichment.
     *
     * @param enrichedPtms the PTMs used for enrichment
     */
    public void setEnrichedPtms(ArrayList<String> enrichedPtms) {
        this.enrichedPtms = enrichedPtms;
    }

    /**
     * Returns the MS1 mass resolution.
     *
     * @return the MS1 mass resolution
     */
    public Double getMs1Resolution() {
        return ms1Resolution;
    }

    /**
     * Sets the MS1 mass resolution.
     *
     * @param ms1Resolution the MS1 mass resolution
     */
    public void setMs1Resolution(Double ms1Resolution) {
        this.ms1Resolution = ms1Resolution;
    }

    /**
     * Indicates whether the MS1 resolution is in ppm.
     *
     * @return a boolean indicating whether the MS1 resolution is in ppm
     */
    public boolean isMs1ResolutionPpm() {
        return ms1ResolutionPpm;
    }

    /**
     * Sets whether the MS1 resolution is in ppm.
     *
     * @param ms1ResolutionPpm a boolean indicating whether the MS1 resolution
     * is in ppm
     */
    public void setMs1ResolutionPpm(boolean ms1ResolutionPpm) {
        this.ms1ResolutionPpm = ms1ResolutionPpm;
    }

    /**
     * Returns the fragmentation method.
     *
     * @return the fragmentation method
     */
    public FragmentationMethod getFragmentationMethod() {
        return fragmentationMethod;
    }

    /**
     * Sets the fragmentation method.
     *
     * @param fragmentationMethod the fragmentation method
     */
    public void setFragmentationMethod(FragmentationMethod fragmentationMethod) {
        this.fragmentationMethod = fragmentationMethod;
    }

    /**
     * Returns the MS2 resolution.
     *
     * @return the MS2 resolution
     */
    public Double getMs2Resolution() {
        return ms2Resolution;
    }

    /**
     * Sets the MS2 resolution.
     *
     * @param ms2Resolution the MS2 resolution
     */
    public void setMs2Resolution(Double ms2Resolution) {
        this.ms2Resolution = ms2Resolution;
    }

    /**
     * Indicates whether the MS2 resolution is in ppm.
     *
     * @return a boolean indicating whether the MS2 resolution is in ppm
     */
    public boolean isMs2ResolutionPpm() {
        return ms2ResolutionPpm;
    }

    /**
     * Sets whether the MS2 resolution is in ppm.
     *
     * @param ms2ResolutionPpm a boolean indicating whether the MS2 resolution
     * is in ppm
     */
    public void setMs2ResolutionPpm(boolean ms2ResolutionPpm) {
        this.ms2ResolutionPpm = ms2ResolutionPpm;
    }

    /**
     * Adds a label.
     *
     * @param ptmName the name of the modification used for the label
     * @param complete boolean indicating whether the labeling is complete
     * (true) or partial (false)
     */
    public void addLabel(String ptmName, boolean complete) {
        if (labellingPtms == null) {
            labellingPtms = new HashMap<String, Boolean>(1);
        }
        labellingPtms.put(ptmName, complete);
    }

    /**
     * Returns the PTMs used for labeling in a map: name of the PTM - boolean
     * indicating whether the labeling is complete.
     *
     * @return the PTMs used for labeling
     */
    public HashMap<String, Boolean> getLabellingPtms() {
        return labellingPtms;
    }

    /**
     * Backward compatibility inferring the protocol from search settings.
     *
     * @param searchParameters the search settings where to take the information
     * from
     *
     * @return the inferred protocol
     */
    public static ShotgunProtocol inferProtocolFromSearchSettings(SearchParameters searchParameters) {
        ShotgunProtocol shotgunProtocol = new ShotgunProtocol();
        shotgunProtocol.setEnzyme(searchParameters.getEnzyme());
        shotgunProtocol.setMs1Resolution(searchParameters.getPrecursorAccuracy());
        shotgunProtocol.setMs1ResolutionPpm(searchParameters.isPrecursorAccuracyTypePpm());
        shotgunProtocol.setMs2Resolution(searchParameters.getFragmentIonAccuracy());
        shotgunProtocol.setMs2ResolutionPpm(false);
        return shotgunProtocol;
    }
}
