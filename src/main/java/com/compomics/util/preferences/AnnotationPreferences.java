package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Ion.IonType;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class contains the spectrum annotation preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
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
     * If true, the ion table is shown as an intensity version, false displays
     * the standard Mascot version.
     */
    private boolean intensityIonTable = true; //@TODO: move to another class
    /**
     * If true, bars are shown in the bubble plot highlighting the ions.
     */
    private boolean showBars = false; //@TODO: move to another class
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
     * Shall automatic annotation be used.
     */
    private boolean automaticAnnotation = true;
    /**
     * The types of ions to annotate.
     *
     * @deprecated use selectedIonsMap instead
     */
    private HashMap<Ion.IonType, ArrayList<Integer>> selectedIons = new HashMap<Ion.IonType, ArrayList<Integer>>();
    /**
     * The types of ions to annotate.
     */
    private HashMap<Ion.IonType, HashSet<Integer>> selectedIonsMap = new HashMap<Ion.IonType, HashSet<Integer>>();
    /**
     * The neutral losses searched for.
     *
     * @deprecated use the SpecificAnnotationPreferencesClass
     */
    private NeutralLossesMap neutralLossesMap;
    /**
     * List of neutral losses to annotate.
     */
    private ArrayList<NeutralLoss> neutralLossesList;
    /**
     * Shall neutral losses be only considered for ions containing amino acids
     * of interest?
     *
     * @deprecated use neutralLossesAuto instead
     */
    private boolean neutralLossesSequenceDependant;
    /**
     * If true neutral losses will be automatically deduced from the spectrum
     * identification assumption.
     */
    private Boolean neutralLossesAuto = true;
    /**
     * The fragment charge to be searched for.
     *
     * @deprecated use the SpecificAnnotationPreferencesClass
     */
    private ArrayList<Integer> selectedCharges;
    /**
     * Fragment ion accuracy used for peak matching.
     */
    private double fragmentIonAccuracy;
    /**
     * Indicates whether the fragment ion accuracy is in ppm.
     */
    private Boolean fragmentIonPpm = false;
    /**
     * The currently inspected peptide.
     *
     * @deprecated use the specific annotation preferences instead
     */
    private Peptide currentPeptide;
    /**
     * The currently annotated spectrumIdentificationAssumption.
     *
     * @deprecated use the specific annotation preferences instead
     */
    private SpectrumIdentificationAssumption spectrumIdentificationAssumption;
    /**
     * The charge of the currently inspected precursor.
     *
     * @deprecated use the specific annotation preferences instead
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
     * If there are more than one matching peak for a given annotation setting
     * this value to true results in the most accurate peak being annotated,
     * while setting this to false annotates the most intense peak.
     */
    private Boolean highResolutionAnnotation = true;

    /**
     * Constructor.
     */
    public AnnotationPreferences() {
    }

    /**
     * Returns the annotation preferences specific to a spectrum and an
     * identification assumption.
     *
     * @param spectrumKey the key of the spectrum to annotate
     * @param spectrumIdentificationAssumption the spectrum identification
     * assumption to annotate with
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
     *
     * @return the annotation preferences specific to a spectrum and an
     * identification assumption
     *
     * @throws IOException exception thrown whenever an error occurred while
     * interacting with a file while mapping potential modification sites
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while mapping potential modification sites
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing an object from the ProteinTree
     * @throws SQLException exception thrown whenever an error occurred while
     * interacting with the ProteinTree
     */
    public SpecificAnnotationPreferences getSpecificAnnotationPreferences(String spectrumKey, SpectrumIdentificationAssumption spectrumIdentificationAssumption, 
            SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences) 
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        if (neutralLossesAuto == null) { // Backward compatibility
            neutralLossesAuto = true;
        }
        if (fragmentIonPpm == null) { // Backward compatibility
            fragmentIonPpm = false;
        }

        SpecificAnnotationPreferences specificAnnotationPreferences = new SpecificAnnotationPreferences(spectrumKey, spectrumIdentificationAssumption);
        specificAnnotationPreferences.setNeutralLossesAuto(neutralLossesAuto);
        if (neutralLossesAuto) {
            specificAnnotationPreferences.setNeutralLossesMap(SpectrumAnnotator.getDefaultLosses(spectrumIdentificationAssumption, sequenceMatchingPreferences, ptmSequenceMatchingPreferences));
        } else {
            NeutralLossesMap tempNeutralLossesMap = new NeutralLossesMap();
            for (NeutralLoss neutralLoss : getNeutralLosses()) {
                tempNeutralLossesMap.addNeutralLoss(neutralLoss, 1, 1);
            }
            specificAnnotationPreferences.setNeutralLossesMap(tempNeutralLossesMap);
        }
        ArrayList<Integer> charges = new ArrayList<Integer>(4);
        int precursorCharge = spectrumIdentificationAssumption.getIdentificationCharge().value;
        if (precursorCharge == 1) {
            charges.add(precursorCharge);
        } else {
            for (int charge = 1; charge < precursorCharge; charge++) {
                charges.add(charge);
            }
        }
        specificAnnotationPreferences.setSelectedCharges(charges);
        specificAnnotationPreferences.setSelectedIonsMap((HashMap<Ion.IonType, HashSet<Integer>>) selectedIonsMap.clone());
        specificAnnotationPreferences.setFragmentIonAccuracy(fragmentIonAccuracy);
        specificAnnotationPreferences.setFragmentIonPpm(fragmentIonPpm);
        return specificAnnotationPreferences;
    }

    /**
     * Constructor setting preferences from search parameters.
     *
     * @param searchParameters the search parameters
     */
    public AnnotationPreferences(SearchParameters searchParameters) {
        setPreferencesFromSearchParameters(searchParameters);
    }

    /**
     * Updates the annotation parameters based on search parameters.
     *
     * @param searchParameters the search parameters where to take the
     * information from
     */
    public void setPreferencesFromSearchParameters(SearchParameters searchParameters) {
        clearIonTypes();
        addIonType(Ion.IonType.PEPTIDE_FRAGMENT_ION, searchParameters.getIonSearched1());
        addIonType(Ion.IonType.PEPTIDE_FRAGMENT_ION, searchParameters.getIonSearched2());
        addIonType(Ion.IonType.TAG_FRAGMENT_ION, searchParameters.getIonSearched1());
        addIonType(Ion.IonType.TAG_FRAGMENT_ION, searchParameters.getIonSearched2());
        addIonType(Ion.IonType.PRECURSOR_ION);
        addIonType(Ion.IonType.IMMONIUM_ION);
        addIonType(Ion.IonType.REPORTER_ION);
        setFragmentIonAccuracy(searchParameters.getFragmentIonAccuracy());
        PTMFactory ptmFactory = PTMFactory.getInstance();
        for (String modName : searchParameters.getModificationProfile().getAllModifications()) {
            PTM ptm = ptmFactory.getPTM(modName);
            for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                addNeutralLoss(neutralLoss);
            }
        }
    }

    /**
     * Returns whether neutral losses are considered only for amino acids of
     * interest or not.
     *
     * @return a boolean indicating whether neutral losses are considered only
     * for amino acids of interest or not.
     */
    public boolean areNeutralLossesSequenceAuto() {
        if (neutralLossesAuto == null) { // Backward compatibility
            neutralLossesAuto = true;
        }
        return neutralLossesAuto;
    }

    /**
     * Clears the considered neutral losses.
     */
    public void clearNeutralLosses() {
        if (neutralLossesList == null && neutralLossesMap != null) { // backwards compatibility        
            neutralLossesMap.clearNeutralLosses();
            neutralLossesList = new ArrayList<NeutralLoss>();
        }
        neutralLossesList.clear();
    }

    /**
     * Returns the considered neutral losses.
     *
     * @return the considered neutral losses
     */
    public ArrayList<NeutralLoss> getNeutralLosses() {
        if (neutralLossesList == null && neutralLossesMap != null) { // backwards compatibility
            neutralLossesList = neutralLossesMap.getAccountedNeutralLosses();
        }
        if (neutralLossesList == null) {
            neutralLossesList = new ArrayList<NeutralLoss>(2);
        }
        return neutralLossesList;
    }

    /**
     * Adds a neutral loss.
     *
     * @param neutralLoss a new neutral loss
     */
    public void addNeutralLoss(NeutralLoss neutralLoss) {
        if (neutralLossesList == null && neutralLossesMap != null) { // backwards compatibility
            neutralLossesList = neutralLossesMap.getAccountedNeutralLosses();
        }
        if (neutralLossesList == null) {
            neutralLossesList = new ArrayList<NeutralLoss>(2);
        }
        boolean alreadyInList = false;
        for (NeutralLoss tempNeutralLoss : neutralLossesList) {
            if (neutralLoss.isSameAs(tempNeutralLoss)) {
                alreadyInList = true;
                break;
            }
        }
        if (!alreadyInList) {
            neutralLossesList.add(neutralLoss);
        }
    }

    /**
     * Returns the type of ions annotated.
     *
     * @return the type of ions annotated
     */
    public HashMap<Ion.IonType, HashSet<Integer>> getIonTypes() {
        if (selectedIonsMap == null && selectedIons != null) { // backwards compatibility
            backwardsCompatibilitySelectedIonsFix();
        }
        return selectedIonsMap;
    }

    /**
     * Backwards compatibility fix for the selection ions.
     */
    private void backwardsCompatibilitySelectedIonsFix() {
        selectedIonsMap = new HashMap<Ion.IonType, HashSet<Integer>>();
        Iterator<IonType> iterator = selectedIons.keySet().iterator();
        while (iterator.hasNext()) {
            IonType tempIonType = iterator.next();
            ArrayList<Integer> tempIntegers = selectedIons.get(tempIonType);
            HashSet<Integer> tempSet = new HashSet<Integer>();
            for (Integer temp : tempIntegers) {
                tempSet.add(temp);
            }
            selectedIonsMap.put(tempIonType, tempSet);
        }
    }

    /**
     * Returns the type of peptide fragment ions annotated.
     *
     * @return the type of peptide fragment ions annotated
     */
    public HashSet<Integer> getFragmentIonTypes() {
        if (selectedIonsMap == null && selectedIons != null) { // backwards compatibility
            backwardsCompatibilitySelectedIonsFix();
        }
        if (selectedIonsMap.get(Ion.IonType.PEPTIDE_FRAGMENT_ION) == null) {
            return new HashSet<Integer>();
        } else {
            return selectedIonsMap.get(Ion.IonType.PEPTIDE_FRAGMENT_ION);
        }
    }

    /**
     * Adds a new ion type and subtype to annotate.
     *
     * @param ionType a new ion type to annotate
     * @param subType the ion sub type
     */
    public void addIonType(Ion.IonType ionType, int subType) {
        if (selectedIonsMap == null && selectedIons != null) { // backwards compatibility
            backwardsCompatibilitySelectedIonsFix();
        }
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
        if (selectedIonsMap == null && selectedIons != null) { // backwards compatibility
            backwardsCompatibilitySelectedIonsFix();
        }
        if (!selectedIonsMap.containsKey(ionType)) {
            selectedIonsMap.put(ionType, new HashSet<Integer>());
        }
        for (int subType : Ion.getPossibleSubtypes(ionType)) {
            this.selectedIonsMap.get(ionType).add(subType);
        }
    }

    /**
     * Clears the ion types annotated.
     */
    public void clearIonTypes() {
        selectedIonsMap.clear();
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
            neutralLossesAuto = true;
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
     * Indicates whether the fragment ion accuracy is in ppm.
     *
     * @return a boolean indicating whether the fragment ion accuracy is in ppm
     */
    public boolean isFragmentIonPpm() {
        if (fragmentIonPpm == null) { // Backward compatibility
            fragmentIonPpm = false;
        }
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
     * Returns the intensity limit. [0.0 - 1.0], where 0.0 means that all peaks
     * are considered for annotations, while 0.75 means that only the 75% most
     * intense peaks are considered for annotations.
     *
     * @return the intensityLimit
     */
    public double getAnnotationIntensityLimit() {
        return intensityLimit;
    }

    /**
     * Sets the annotation level. [0.0 - 1.0], where 0.0 means that all peaks
     * are considered for annotations, while 0.75 means that only the 75% most
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
     * @param showAllPeaks if all peaks are to be shown
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
     * @param showBars if the bars in the bubble plot are to be shown
     */
    public void setShowBars(boolean showBars) {
        this.showBars = showBars;
    }

    /**
     * If true, the ion table is shown as an intensity version, false displays
     * the standard Mascot version.
     *
     * @return if true, the ion table is shown as an intensity version, false
     * displays the standard Mascot version
     */
    public boolean useIntensityIonTable() {
        return intensityIonTable;
    }

    /**
     * Set if the intensity or m/z ion table should be shown.
     *
     * @param intensityIonTable if the intensity or m/z ion table should be
     * shown
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
     * Set if the automatic y-axis zoom only considers the annotated peaks.
     *
     * @param yAxisZoomExcludesBackgroundPeaks if the automatic y-axis zoom only
     * considers the annotated peaks
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

    /**
     * Returns true if the peak annotation should be based on the most accurate
     * mz value, false bases the annotation on the most intense peak.
     *
     * @return the highResolutionAnnotation
     */
    public boolean isHighResolutionAnnotation() {
        if (highResolutionAnnotation == null) {
            highResolutionAnnotation = true;
        }
        return highResolutionAnnotation;
    }

    /**
     * Set if the peak annotation should be based on the most accurate mz value,
     * or on the most intense peak.
     *
     * @param highResolutionAnnotation the highResolutionAnnotation to set
     */
    public void setHighResolutionAnnotation(boolean highResolutionAnnotation) {
        this.highResolutionAnnotation = highResolutionAnnotation;
    }
}
