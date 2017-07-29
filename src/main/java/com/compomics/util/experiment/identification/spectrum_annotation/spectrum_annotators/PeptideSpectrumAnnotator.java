package com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.MathException;

/**
 * Annotates a spectrum with peptide fragments. Warning: use one iterator per
 * thread.
 *
 * @author Marc Vaudel
 */
public class PeptideSpectrumAnnotator extends SpectrumAnnotator {

    /**
     * The theoretic peptide to match.
     */
    private Peptide peptide;

    /**
     * Constructor.
     */
    public PeptideSpectrumAnnotator() {

    }

    /**
     * Sets a new peptide to match.
     *
     * @param peptide the new peptide
     * @param precursorCharge the new precursor charge
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     */
    public void setPeptide(Peptide peptide, int precursorCharge, SpecificAnnotationSettings specificAnnotationSettings) {
        setPeptide(peptide, null, precursorCharge, specificAnnotationSettings);
    }

    /**
     * Sets a new peptide to match.
     *
     * @param peptide the new peptide
     * @param possibleFragmentIons the possible fragment ions of the peptide
     * @param precursorCharge the new precursor charge
     * @param specificAnnotationSettings if provided, only the ions detectable
     * using these settings will be selected
     */
    public void setPeptide(Peptide peptide, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons, int precursorCharge, SpecificAnnotationSettings specificAnnotationSettings) {
        if (specificAnnotationSettings != null && super.specificAnnotationSettings == null
                || specificAnnotationSettings == null && super.specificAnnotationSettings != null
                || specificAnnotationSettings != null && super.specificAnnotationSettings != null && specificAnnotationSettings != super.specificAnnotationSettings
                || this.peptide == null
                || !this.peptide.getKey().equals(peptide.getKey())
                || !this.peptide.sameModificationsAs(peptide)
                || this.precursorCharge != precursorCharge) {

            // Set new values
            this.peptide = peptide;
            this.precursorCharge = precursorCharge;
            if (possibleFragmentIons == null) {
                theoreticalFragmentIons = fragmentFactory.getFragmentIons(peptide, specificAnnotationSettings);
            } else {
                theoreticalFragmentIons = possibleFragmentIons;
            }
            if (massShift != 0 || massShiftNTerm != 0 || massShiftCTerm != 0) {
                updateMassShifts();
            }
        }
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a
     * given peak according to the annotation settings.
     *
     * @param peptide the peptide
     * @param specificAnnotationSettings the specific annotation settings
     * @param peak the peak to match
     * @return a list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, SpecificAnnotationSettings specificAnnotationSettings, Peak peak) {
        setPeptide(peptide, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);
        return matchPeak(specificAnnotationSettings, peak);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches
     * using the intensity filter.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurred when estimating the noise level
     * @throws org.apache.commons.math.MathException exception thrown if a math
     * exception occurred when estimating the noise level
     */
    public synchronized ArrayList<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, MSnSpectrum spectrum, Peptide peptide) throws InterruptedException, MathException {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, true);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurred when estimating the noise level
     * @throws org.apache.commons.math.MathException exception thrown if a math
     * exception occurred when estimating the noise level
     */
    public synchronized ArrayList<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, MSnSpectrum spectrum, Peptide peptide, boolean useIntensityFilter) throws InterruptedException, MathException {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, null, useIntensityFilter);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     * @param possiblePeptideFragments the possible peptide fragments for this
     * peptide
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurred when estimating the noise level
     * @throws org.apache.commons.math.MathException exception thrown if a math
     * exception occurred when estimating the noise level
     */
    public synchronized ArrayList<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, MSnSpectrum spectrum, Peptide peptide,
            HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possiblePeptideFragments, boolean useIntensityFilter) throws InterruptedException, MathException {

        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        setMassTolerance(specificAnnotationSettings.getFragmentIonAccuracy(), specificAnnotationSettings.isFragmentIonPpm(), annotationSettings.getTiesResolution());
        if (spectrum != null) {
            double intensityLimit = useIntensityFilter ? spectrum.getIntensityLimit(annotationSettings.getIntensityThresholdType(), annotationSettings.getAnnotationIntensityLimit()) : 0.0;
            setSpectrum(spectrum, intensityLimit);
        }
        setPeptide(peptide, possiblePeptideFragments, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);

        ArrayList<Integer> precursorCharges = new ArrayList<Integer>();

        // possible charges for the precursor
        for (int i = 1; i <= precursorCharge; i++) {
            precursorCharges.add(i);
        }

        HashMap<Ion.IonType, HashSet<Integer>> ionTypes = specificAnnotationSettings.getIonTypes();
        for (Ion.IonType ionType : ionTypes.keySet()) {
            HashMap<Integer, ArrayList<Ion>> ionMap = theoreticalFragmentIons.get(ionType.index);
            if (ionMap != null) {
                HashSet<Integer> subtypes = ionTypes.get(ionType);
                for (int subType : subtypes) {
                    ArrayList<Ion> ions = ionMap.get(subType);
                    if (ions != null) {
                        for (Ion ion : ions) {

                            if (lossesValidated(specificAnnotationSettings.getNeutralLossesMap(), ion)) {

                                ArrayList<Integer> ionPossibleCharges = (ionType == Ion.IonType.PRECURSOR_ION) ? precursorCharges : specificAnnotationSettings.getSelectedCharges();

                                for (Integer charge : ionPossibleCharges) {
                                    if (chargeValidated(ion, charge, precursorCharge)) {
                                        IonMatch ionMatch = matchInSpectrum(ion, charge);
                                        if (ionMatch != null) {
                                            result.add(ionMatch);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the ion matches corresponding to fragment ions indexed by amino
     * acid number in the sequence. 1 is first amino acid.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum The spectrum to match
     * @param peptide The peptide of interest
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return the ion matches corresponding to fragment ions indexed by amino
     * acid number in the sequence
     *
     * @throws java.lang.InterruptedException exception thrown if a threading
     * error occurred when estimating the noise level
     * @throws org.apache.commons.math.MathException exception thrown if a math
     * exception occurred when estimating the noise level
     */
    public HashMap<Integer, ArrayList<IonMatch>> getCoveredAminoAcids(AnnotationSettings annotationSettings,
            SpecificAnnotationSettings specificAnnotationSettings, MSnSpectrum spectrum, Peptide peptide, boolean useIntensityFilter) throws InterruptedException, MathException {

        HashMap<Integer, ArrayList<IonMatch>> matchesMap = new HashMap<Integer, ArrayList<IonMatch>>();
        ArrayList<IonMatch> matches = getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, useIntensityFilter);

        for (IonMatch ionMatch : matches) {
            Ion ion = ionMatch.ion;
            int number;
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                if (ion.getSubType() == PeptideFragmentIon.A_ION
                        || ion.getSubType() == PeptideFragmentIon.B_ION
                        || ion.getSubType() == PeptideFragmentIon.C_ION) {
                    number = ((PeptideFragmentIon) ion).getNumber();
                } else {
                    number = peptide.getSequence().length() + 1 - ((PeptideFragmentIon) ion).getNumber();
                }
                if (!matchesMap.containsKey(number)) {
                    matchesMap.put(number, new ArrayList<IonMatch>());
                }
                matchesMap.get(number).add(ionMatch);
            }
        }

        return matchesMap;
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param specificAnnotationSettings the specific annotation settings
     * @param peptide The peptide of interest
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(SpecificAnnotationSettings specificAnnotationSettings, Peptide peptide) {
        return getExpectedIons(specificAnnotationSettings, peptide, null);
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param specificAnnotationSettings the specific annotation settings
     * @param peptide The peptide of interest
     * @param possibleFragmentIons the possible fragment ions for the given
     * peptide
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(SpecificAnnotationSettings specificAnnotationSettings, Peptide peptide, HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> possibleFragmentIons) {
        setPeptide(peptide, possibleFragmentIons, specificAnnotationSettings.getPrecursorCharge(), specificAnnotationSettings);
        return getExpectedIons(specificAnnotationSettings);
    }

    @Override
    public ArrayList<IonMatch> getCurrentAnnotation(MSnSpectrum spectrum, AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings, boolean useIntensityFilter) throws InterruptedException, MathException {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide, useIntensityFilter);
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide. /!\ this method will work only if the PTM found in the peptide
     * are in the PTMFactory.
     *
     * @param peptide the peptide of interest
     * @param sequenceMatchingSettings the sequence matching settings for
     * peptide to protein mapping
     * @param ptmSequenceMatchingSettings the sequence matching settings for PTM
     * to peptide mapping
     *
     * @return the expected possible neutral losses
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
    public static NeutralLossesMap getDefaultLosses(Peptide peptide, SequenceMatchingPreferences sequenceMatchingSettings,
            SequenceMatchingPreferences ptmSequenceMatchingSettings) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        PTMFactory pTMFactory = PTMFactory.getInstance();
        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        String sequence = peptide.getSequence();
        int aaMin = sequence.length();
        int aaMax = 0;

        for (NeutralLoss neutralLoss : IonFactory.getDefaultNeutralLosses()) {
            char[] aas = neutralLoss.aminoAcids;
            if (aas != null) {
                for (char aa : aas) {
                    int firstIndex = sequence.indexOf(aa);
                    if (firstIndex != -1) {
                        aaMin = Math.min(firstIndex, aaMin);
                        aaMax = Math.max(sequence.lastIndexOf(aa), aaMax);
                    }
                }
                if (aaMin < sequence.length()) {
                    neutralLossesMap.addNeutralLoss(neutralLoss, aaMin + 1, sequence.length() - aaMax);
                }
            }
        }

        int modMin = sequence.length();
        int modMax = 0;

        if (peptide.isModified()) {
            for (ModificationMatch modMatch : peptide.getModificationMatches()) {
                PTM ptm = pTMFactory.getPTM(modMatch.getTheoreticPtm());
                for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                    ArrayList<Integer> indexes = peptide.getPotentialModificationSites(ptm, sequenceMatchingSettings, ptmSequenceMatchingSettings);
                    if (!indexes.isEmpty()) {
                        Collections.sort(indexes);
                        modMin = indexes.get(0);
                        modMax = indexes.get(indexes.size() - 1);
                    }
                    neutralLossesMap.addNeutralLoss(neutralLoss, modMin, sequence.length() - modMax + 1);
                }
            }
        }

        return neutralLossesMap;
    }

    /**
     * Returns the currently inspected peptide.
     *
     * @return the currently inspected peptide
     */
    public Peptide getCurrentlyLoadedPeptide() {
        return peptide;
    }
}
