package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The spectrum annotation preferences specific to a spectrum and an
 * identification assumption.
 *
 * @author Marc Vaudel
 */
public class SpecificAnnotationPreferences {

    /**
     * The key of the currently annotated spectrum.
     */
    private String spectrumKey;
    /**
     * The currently annotated spectrumIdentificationAssumption.
     */
    private SpectrumIdentificationAssumption spectrumIdentificationAssumption;
    /**
     * The types of ions to annotate.
     */
    private HashMap<Ion.IonType, HashSet<Integer>> selectedIonsMap = new HashMap<Ion.IonType, HashSet<Integer>>();
    /**
     * If true neutral losses will be automatically deduced from the spectrum
     * identification assumption.
     */
    private boolean neutralLossesAuto = true;
    /**
     * The neutral losses searched for.
     */
    private NeutralLossesMap neutralLossesMap = new NeutralLossesMap();
    /**
     * The fragment charge to be searched for.
     */
    private ArrayList<Integer> selectedCharges = new ArrayList<Integer>();
    /**
     * Fragment ion accuracy used for peak matching.
     */
    private double fragmentIonAccuracy;
    /**
     * Indicates whether the fragment ion accuracy is in ppm.
     */
    private boolean fragmentIonPpm = false;

    /**
     * Constructor.
     *
     * @param spectrumKey the key of the spectrum to annotate
     * @param spectrumIdentificationAssumption the spectrum identification
     * assumption to annotate with
     */
    public SpecificAnnotationPreferences(String spectrumKey, SpectrumIdentificationAssumption spectrumIdentificationAssumption) {
        this.spectrumKey = spectrumKey;
        this.spectrumIdentificationAssumption = spectrumIdentificationAssumption;
    }

    /**
     * Returns the key of the spectrum to annotate.
     *
     * @return the key of the spectrum to annotate
     */
    public String getSpectrumKey() {
        return spectrumKey;
    }

    /**
     * Returns the spectrum identification assumption to annotate with.
     *
     * @return the spectrum identification assumption to annotate with
     */
    public SpectrumIdentificationAssumption getSpectrumIdentificationAssumption() {
        return spectrumIdentificationAssumption;
    }

    /**
     * Returns the charge of the precursor.
     *
     * @return the charge of the precursor
     */
    public int getPrecursorCharge() {
        return spectrumIdentificationAssumption.getIdentificationCharge().value;
    }

    /**
     * Returns the map of ions to annotate.
     *
     * @return the map of ions to annotate
     */
    public HashMap<Ion.IonType, HashSet<Integer>> getIonTypes() {
        return selectedIonsMap;
    }

    /**
     * Returns the type of peptide fragment ions annotated.
     *
     * @return the type of peptide fragment ions annotated
     */
    public HashSet<Integer> getFragmentIonTypes() {
        if (selectedIonsMap.get(Ion.IonType.PEPTIDE_FRAGMENT_ION) == null) {
            return new HashSet<Integer>();
        } else {
            return selectedIonsMap.get(Ion.IonType.PEPTIDE_FRAGMENT_ION);
        }
    }

    /**
     * Sets the map of ions to annotate.
     *
     * @param selectedIonsMap the map of ions to annotate
     */
    public void setSelectedIonsMap(HashMap<Ion.IonType, HashSet<Integer>> selectedIonsMap) {
        this.selectedIonsMap = selectedIonsMap;
    }

    /**
     * Clears the ion types annotated.
     */
    public void clearIonTypes() {
        selectedIonsMap.clear();
    }

    /**
     * Adds a new ion type and subtype to annotate.
     *
     * @param ionType a new ion type to annotate
     * @param subType the ion sub type
     */
    public void addIonType(Ion.IonType ionType, int subType) {
        if (!selectedIonsMap.containsKey(ionType)) {
            selectedIonsMap.put(ionType, new HashSet<Integer>());
        }
        this.selectedIonsMap.get(ionType).add(subType);
    }

    /**
     * Adds a new ion type to annotate. All subtypes will be annotated.
     *
     * @param ionType a new ion type to annotate
     */
    public void addIonType(Ion.IonType ionType) {
        if (!selectedIonsMap.containsKey(ionType)) {
            selectedIonsMap.put(ionType, new HashSet<Integer>());
        }
        for (int subType : Ion.getPossibleSubtypes(ionType)) {
            this.selectedIonsMap.get(ionType).add(subType);
        }
    }

    /**
     * Returns the map of neutral losses to annotate.
     *
     * @return the map of neutral losses to annotate
     */
    public NeutralLossesMap getNeutralLossesMap() {
        return neutralLossesMap;
    }

    /**
     * Sets the map of neutral losses to annotate.
     *
     * @param neutralLossesMap the map of neutral losses to annotate
     */
    public void setNeutralLossesMap(NeutralLossesMap neutralLossesMap) {
        this.neutralLossesMap = neutralLossesMap;
    }

    /**
     * Clears the considered neutral losses.
     */
    public void clearNeutralLosses() {
        neutralLossesMap.clearNeutralLosses();
    }

    /**
     * Adds a neutral loss.
     *
     * @param neutralLoss a new neutral loss
     */
    public void addNeutralLoss(NeutralLoss neutralLoss) {
        neutralLossesMap.addNeutralLoss(neutralLoss, 1, 1);
    }

    /**
     * Returns the charges selected for annotation.
     *
     * @return the charges selected for annotation
     */
    public ArrayList<Integer> getSelectedCharges() {
        return selectedCharges;
    }

    /**
     * Sets the charges selected for annotation.
     *
     * @param selectedCharges the charges selected for annotation
     */
    public void setSelectedCharges(ArrayList<Integer> selectedCharges) {
        this.selectedCharges = selectedCharges;
    }

    /**
     * Clears the selected charges.
     */
    public void clearCharges() {
        selectedCharges.clear();
    }

    /**
     * Add a charge to take into account when annotating the spectrum.
     *
     * @param selectedCharge a charge to take into account when annotating the
     * spectrum
     */
    public void addSelectedCharge(int selectedCharge) {
        if (!selectedCharges.contains(selectedCharge)) {
            selectedCharges = new ArrayList<Integer>(selectedCharges);
            selectedCharges.add(selectedCharge);
        }
    }

    /**
     * Returns the fragment ion accuracy.
     *
     * @return the fragment ion accuracy
     */
    public double getFragmentIonAccuracy() {
        return fragmentIonAccuracy;
    }

    /**
     * Sets the fragment ion accuracy.
     *
     * @param fragmentIonAccuracy the fragment ion accuracy
     */
    public void setFragmentIonAccuracy(double fragmentIonAccuracy) {
        this.fragmentIonAccuracy = fragmentIonAccuracy;
    }

    /**
     * Indicates whether the fragment ion accuracy is in ppm.
     *
     * @return a boolean indicating whether the fragment ion accuracy is in ppm
     */
    public boolean isFragmentIonPpm() {
        return fragmentIonPpm;
    }

    /**
     * Sets whether the fragment ion accuracy is in ppm.
     *
     * @param fragmentIonPpm a boolean indicating whether the fragment ion
     * accuracy is in ppm
     */
    public void setFragmentIonPpm(boolean fragmentIonPpm) {
        this.fragmentIonPpm = fragmentIonPpm;
    }

    /**
     * Indicates whether neutral losses should be automatically selected.
     *
     * @return a boolean indicating whether neutral losses should be
     * automatically selected
     */
    public boolean isNeutralLossesAuto() {
        return neutralLossesAuto;
    }

    /**
     * Sets whether neutral losses should be automatically selected.
     *
     * @param neutralLossesAuto a boolean indicating whether neutral losses
     * should be automatically selected
     */
    public void setNeutralLossesAuto(boolean neutralLossesAuto) {
        this.neutralLossesAuto = neutralLossesAuto;
    }
}
