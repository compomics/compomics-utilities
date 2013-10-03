package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the spectrum annotation preferences.
 *
 * @author Marc Vaudel
 */
public class AnnotationPreferences implements Serializable {

    /**
     * Serial version UID for post-serialization compatibility.
     */
    static final long serialVersionUID = -524156803097913546L;
    /**
     * If true, the automatic y-axis zoom excludes background peaks. False
     * includes all peaks in the auto zoom.
     */
    private boolean yAxisZoomExcludesBackgroundPeaks = true;
    /**
     * If true, the ion table is shown as an intensity versionm, false displays
     * the standard Mascot version.
     */
    private boolean intensityIonTable = true;
    /**
     * If true, bars are shown in the bubble plot highlighting the ions.
     */
    private boolean showBars = false;
    /**
     * If true, all peaks are shown, false displays the annotated peaks, and the
     * non-annotated in the background.
     */
    private boolean showAllPeaks = false;
    /**
     * The intensity limit used when only the most intense peaks are to be
     * annotated.
     */
    private double intensityLimit = 0.75;
    /**
     * Shall PeptideShaker use automatic annotation.
     */
    private boolean automaticAnnotation = true;
    /**
     * The types of ions to annotate.
     */
    private HashMap<Ion.IonType, ArrayList<Integer>> selectedIons = new HashMap<Ion.IonType, ArrayList<Integer>>();
    /**
     * The neutral losses searched for.
     */
    private NeutralLossesMap neutralLossesMap = new NeutralLossesMap();
    /**
     * Shall neutral losses be only considered for ions containing amino acids
     * of interest?
     */
    private boolean neutralLossesSequenceDependant = true;
    /**
     * The maximum fragment charge to be searched for.
     */
    private ArrayList<Integer> selectedCharges = new ArrayList<Integer>();
    /**
     * Fragment ion accuracy used for peak matching.
     */
    private double fragmentIonAccuracy;
    /**
     * The currently inspected peptide.
     */
    private Peptide currentPeptide;
    /**
     * The charge of the currently inspected precursor.
     */
    private int currentPrecursorCharge = 0;
    /**
     * If true, the automatic forward ion de novo tags are shown.
     */
    private boolean showForwardIonDeNovoTags = false;
    /**
     * If true, the automatic rewind ion de novo tags are shown.
     */
    private boolean showRewindIonDeNovoTags = false;
    /**
     * The charge for the fragment ions in the de novo sequencing.
     */
    private int deNovoCharge = 1;

    /**
     * Constructor.
     */
    public AnnotationPreferences() {
    }
    
    /**
     * Updates the annotation parameters based on the search parameters
     * 
     * @param searchParameters the search parameters where to take the information from
     */
    public void setPreferencesFromSearchParamaers(SearchParameters searchParameters)  {
        clearIonTypes();
        addIonType(Ion.IonType.PEPTIDE_FRAGMENT_ION, searchParameters.getIonSearched1());
        addIonType(Ion.IonType.PEPTIDE_FRAGMENT_ION, searchParameters.getIonSearched2());
        addIonType(Ion.IonType.PRECURSOR_ION);
        addIonType(Ion.IonType.IMMONIUM_ION);
        addIonType(Ion.IonType.REPORTER_ION);
        setFragmentIonAccuracy(searchParameters.getFragmentIonAccuracy());
    }

    /**
     * Sets the annotation settings for the current peptide and precursor
     * charge.
     *
     * @param currentPeptide
     * @param currentPrecursorCharge
     * @param newSpectrum
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     */
    public void setCurrentSettings(Peptide currentPeptide, int currentPrecursorCharge, boolean newSpectrum) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        this.currentPeptide = currentPeptide;
        this.currentPrecursorCharge = currentPrecursorCharge;

        if (newSpectrum && automaticAnnotation) {
            resetAutomaticAnnotation();
        } else {
            if (neutralLossesSequenceDependant) {
                neutralLossesMap = SpectrumAnnotator.getDefaultLosses(currentPeptide);
            }
        }
    }

    /**
     * Updates the neutral losses and charge annotation settings.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     */
    public void resetAutomaticAnnotation() throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        selectedCharges.clear();
        for (int charge = 1; charge < currentPrecursorCharge; charge++) {
            selectedCharges.add(charge);
        }

        neutralLossesMap = SpectrumAnnotator.getDefaultLosses(currentPeptide);
    }

    /**
     * Returns whether neutral losses are considered only for amino acids of
     * interest or not.
     *
     * @return a boolean indicating whether neutral losses are considered only
     * for amino acids of interest or not.
     */
    public boolean areNeutralLossesSequenceDependant() {
        return neutralLossesSequenceDependant;
    }

    /**
     * Set whether neutral losses are considered only for amino acids of
     * interest or not.
     *
     *
     * @param neutralLossesSequenceDependant
     */
    public void setNeutralLossesSequenceDependant(boolean neutralLossesSequenceDependant) {
        this.neutralLossesSequenceDependant = neutralLossesSequenceDependant;
    }

    /**
     * Returns the fragment ion charges considered for the desired precursor
     * charge.
     *
     * @return the fragment ion charges considered
     */
    public ArrayList<Integer> getValidatedCharges() {
        return selectedCharges;
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
            selectedCharges.add(selectedCharge);
        }
    }

    /**
     * Clears the considered neutral losses.
     */
    public void clearNeutralLosses() {
        neutralLossesMap.clearNeutralLosses();
    }

    /**
     * Returns the considered neutral losses.
     *
     * @return the considered neutral losses
     */
    public NeutralLossesMap getNeutralLosses() {
        return neutralLossesMap;
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
     * Returns the type of ions annotated.
     *
     * @return the type of ions annotated
     */
    public HashMap<Ion.IonType, ArrayList<Integer>> getIonTypes() {
        return selectedIons;
    }

    /**
     * Returns the type of peptide fragment ions annotated.
     *
     * @return the type of peptide fragment ions annotated
     */
    public ArrayList<Integer> getFragmentIonTypes() {
        if (selectedIons.get(Ion.IonType.PEPTIDE_FRAGMENT_ION) == null) {
            return new ArrayList<Integer>();
        } else {
            return selectedIons.get(Ion.IonType.PEPTIDE_FRAGMENT_ION);
        }
    }

    /**
     * Clears the ion types annotated.
     */
    public void clearIonTypes() {
        selectedIons.clear();
    }

    /**
     * Adds a new ion type and subtype to annotate.
     *
     * @param ionType a new ion type to annotate
     * @param subType the ion sub type
     */
    public void addIonType(Ion.IonType ionType, int subType) {
        if (!selectedIons.containsKey(ionType)) {
            selectedIons.put(ionType, new ArrayList<Integer>());
        }
        this.selectedIons.get(ionType).add(subType);
    }

    /**
     * Adds a new ion type to annotate. All subtypes will be annotated.
     *
     * @param ionType a new ion type to annotate
     */
    public void addIonType(Ion.IonType ionType) {
        if (!selectedIons.containsKey(ionType)) {
            selectedIons.put(ionType, new ArrayList<Integer>());
        }
        for (int subType : Ion.getPossibleSubtypes(ionType)) {
            this.selectedIons.get(ionType).add(subType);
        }
    }

    /**
     * Sets whether the default PeptideShaker annotation should be used.
     *
     * @param automaticAnnotation a boolean indicating whether the default
     * PeptideShaker annotation should be used
     */
    public void useAutomaticAnnotation(boolean automaticAnnotation) {
        this.automaticAnnotation = automaticAnnotation;

        if (automaticAnnotation) {
            neutralLossesSequenceDependant = true;
        }
    }

    /**
     * Returns whether PeptideShaker should automatically set the annotations.
     *
     * @return a boolean indicating whether PeptideShaker should automatically
     * set the annotations
     */
    public boolean useAutomaticAnnotation() {
        return automaticAnnotation;
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
     * Returns the current precursor charge.
     *
     * @return the current precursor charge
     */
    public int getCurrentPrecursorCharge() {
        return currentPrecursorCharge;
    }

    /**
     * Returns the intensity limit. [0.0 - 1.0], where 1.0 means that all peaks
     * are considered for annotations, while 0.3 means that only the 30% most
     * intense peaks are considered for annotations.
     *
     * @return the intensityLimit
     */
    public double getAnnotationIntensityLimit() {
        return intensityLimit;
    }

    /**
     * Sets the annotation level. [0.0 - 1.0], where 1.0 means that all peaks
     * are considered for annotations, while 0.3 means that only the 30% most
     * intense peaks are considered for annotations.
     *
     * @param intensityLimit the intensityLimit to set
     */
    public void setAnnotationLevel(double intensityLimit) {
        this.intensityLimit = intensityLimit;
    }

    /**
     * If true, all peaks are shown, false displays the annotated peaks, and the
     * non-annotated in the background.
     *
     * @return true if all peaks are to be shown
     */
    public boolean showAllPeaks() {
        return showAllPeaks;
    }

    /**
     * Set if all peaks or just the annotated ones are to be shown.
     *
     * @param showAllPeaks
     */
    public void setShowAllPeaks(boolean showAllPeaks) {
        this.showAllPeaks = showAllPeaks;
    }

    /**
     * If true, bars are shown in the bubble plot highlighting the ions.
     *
     * @return true if bars are to be shown in the bubble plot
     */
    public boolean showBars() {
        return showBars;
    }

    /**
     * Set if the bars in the bubble plot are to be shown or not.
     *
     * @param showBars
     */
    public void setShowBars(boolean showBars) {
        this.showBars = showBars;
    }

    /**
     * If true, the ion table is shown as an intensity versionm, false displays
     * the standard Mascot version.
     *
     * @return if true, the ion table is shown as an intensity versionm, false
     * displays the standard Mascot version
     */
    public boolean useIntensityIonTable() {
        return intensityIonTable;
    }

    /**
     * Set if the intensity or m/z ion table should be shown.
     *
     * @param intensityIonTable
     */
    public void setIntensityIonTable(boolean intensityIonTable) {
        this.intensityIonTable = intensityIonTable;
    }

    /**
     * Returns true if the automatic y-axis zoom excludes background peaks.
     * False if includes all peaks.
     *
     * @return true if the automatic y-axis zoom excludes background peaks
     */
    public boolean yAxisZoomExcludesBackgroundPeaks() {
        return yAxisZoomExcludesBackgroundPeaks;
    }

    /**
     * Set if the automatic y-axis zoom only considers the anotated peaks.
     *
     * @param yAxisZoomExcludesBackgroundPeaks
     */
    public void setYAxisZoomExcludesBackgroundPeaks(boolean yAxisZoomExcludesBackgroundPeaks) {
        this.yAxisZoomExcludesBackgroundPeaks = yAxisZoomExcludesBackgroundPeaks;
    }

    /**
     * Returns true if the automatic forward ion de novo tags are to be shown.
     *
     * @return the showForwardIonDeNovoTags
     */
    public boolean showForwardIonDeNovoTags() {
        return showForwardIonDeNovoTags;
    }

    /**
     * Set if the automatic forward ion de novo tags are to be shown.
     *
     * @param showForwardIonDeNovoTags the showForwardIonDeNovoTags to set
     */
    public void setShowForwardIonDeNovoTags(boolean showForwardIonDeNovoTags) {
        this.showForwardIonDeNovoTags = showForwardIonDeNovoTags;
    }

    /**
     * Returns true if the automatic rewind ion de novo tags are to be shown.
     *
     * @return the showRewindIonDeNovoTags
     */
    public boolean showRewindIonDeNovoTags() {
        return showRewindIonDeNovoTags;
    }

    /**
     * Set if the automatic rewind ion de novo tags are to be shown.
     *
     * @param showRewindIonDeNovoTags the showRewindIonDeNovoTags to set
     */
    public void setShowRewindIonDeNovoTags(boolean showRewindIonDeNovoTags) {
        this.showRewindIonDeNovoTags = showRewindIonDeNovoTags;
    }

    /**
     * Returns the charge to use for the fragment ions in the de novo
     * sequencing.
     *
     * @return the charge to use for the fragment ions in the de novo sequencing
     */
    public int getDeNovoCharge() {
        return deNovoCharge;
    }

    /**
     * Set the charge to use for the fragment ions in the de novo sequencing
     *
     * @param deNovoCharge the charge to use for the fragment ions in the de
     * novo sequencing
     */
    public void setDeNovoCharge(int deNovoCharge) {
        this.deNovoCharge = deNovoCharge;
    }
}