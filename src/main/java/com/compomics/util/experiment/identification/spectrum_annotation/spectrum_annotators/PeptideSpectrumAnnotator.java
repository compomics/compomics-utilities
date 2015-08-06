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

/**
 * Annotates a spectrum with peptide fragments. Warning: not multi-thread safe,
 * use different annotators for each thread.
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
     */
    public void setPeptide(Peptide peptide, int precursorCharge) {
        if (this.peptide == null || !this.peptide.getKey().equals(peptide.getKey()) || !this.peptide.sameModificationsAs(peptide) || this.precursorCharge != precursorCharge) {
            this.peptide = peptide;
            this.precursorCharge = precursorCharge;
            theoreticalFragmentIons = fragmentFactory.getFragmentIons(peptide);
            if (massShift != 0 || massShiftNTerm != 0 || massShiftCTerm != 0) {
                updateMassShifts();
            }
            spectrumAnnotation.clear();
            unmatchedIons.clear();
        }
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a
     * given peak.
     *
     * @param peptide the peptide
     * @param specificAnnotationPreferences the specific annotation preferences
     * @param peak the peak to match
     * @return a list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, SpecificAnnotationSettings specificAnnotationPreferences, Peak peak) {
        setPeptide(peptide, specificAnnotationPreferences.getPrecursorCharge());
        return matchPeak(specificAnnotationPreferences, peak);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationPreferences the annotation preferences
     * @param specificAnnotationPreferences the specific annotation preferences
     * @param spectrum the spectrum to match
     * @param peptide the peptide of interest
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public synchronized ArrayList<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences, MSnSpectrum spectrum, Peptide peptide) {

        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        if (spectrum != null) {
            setSpectrum(spectrum, spectrum.getIntensityLimit(annotationPreferences.getAnnotationIntensityLimit()));
        }

        setPeptide(peptide, specificAnnotationPreferences.getPrecursorCharge());
        setMassTolerance(specificAnnotationPreferences.getFragmentIonAccuracy(), specificAnnotationPreferences.isFragmentIonPpm(), annotationPreferences.isHighResolutionAnnotation());

        ArrayList<Integer> precursorCharges = new ArrayList<Integer>();

        // possible charges for the precursor
        for (int i = 1; i <= precursorCharge; i++) {
            precursorCharges.add(i);
        }

        HashMap<Ion.IonType, HashSet<Integer>> ionTypes = specificAnnotationPreferences.getIonTypes();
        for (Ion.IonType ionType : ionTypes.keySet()) {
            HashMap<Integer, ArrayList<Ion>> ionMap = theoreticalFragmentIons.get(ionType.index);
            if (ionMap != null) {
                HashSet<Integer> subtypes = ionTypes.get(ionType);
                for (int subType : subtypes) {
                    ArrayList<Ion> ions = ionMap.get(subType);
                    if (ions != null) {
                        for (Ion ion : ions) {

                            if (lossesValidated(specificAnnotationPreferences.getNeutralLossesMap(), ion)) {

                                ArrayList<Integer> ionPossibleCharges;

                                if (ionType == Ion.IonType.PRECURSOR_ION) {
                                    ionPossibleCharges = precursorCharges;
                                } else {
                                    ionPossibleCharges = specificAnnotationPreferences.getSelectedCharges();
                                }

                                for (int charge : ionPossibleCharges) {
                                    if (chargeValidated(ion, charge, precursorCharge)) {
                                        String key = IonMatch.getMatchKey(ion, charge);
                                        boolean matchFound = false;
                                        boolean alreadyAnnotated = spectrumAnnotation.containsKey(key);
                                        if (!alreadyAnnotated && !unmatchedIons.contains(key)) {
                                            matchFound = matchInSpectrum(ion, charge);
                                        }
                                        if (alreadyAnnotated || matchFound) {
                                            result.add(spectrumAnnotation.get(key));
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
     * @param annotationPreferences the annotation preferences
     * @param specificAnnotationPreferences the specific annotation preferences
     * @param spectrum The spectrum to match
     * @param peptide The peptide of interest
     *
     * @return the ion matches corresponding to fragment ions indexed by amino
     * acid number in the sequence
     */
    public HashMap<Integer, ArrayList<IonMatch>> getCoveredAminoAcids(AnnotationSettings annotationPreferences,
            SpecificAnnotationSettings specificAnnotationPreferences, MSnSpectrum spectrum, Peptide peptide) {

        HashMap<Integer, ArrayList<IonMatch>> matchesMap = new HashMap<Integer, ArrayList<IonMatch>>();
        ArrayList<IonMatch> matches = getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences, spectrum, peptide);

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
     * @param specificAnnotationPreferences the specific annotation preferences
     * @param peptide The peptide of interest
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(SpecificAnnotationSettings specificAnnotationPreferences, Peptide peptide) {
        setPeptide(peptide, specificAnnotationPreferences.getPrecursorCharge());
        return getExpectedIons(specificAnnotationPreferences);
    }

    @Override
    public ArrayList<IonMatch> getCurrentAnnotation(MSnSpectrum spectrum, AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences) {
        return getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences, spectrum, peptide);
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide. /!\ this method will work only if the PTM found in the peptide
     * are in the PTMFactory.
     *
     * @param peptide the peptide of interest
     * @param sequenceMatchingPreferences the sequence matching preferences for
     * peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for PTM to peptide mapping
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
    public static NeutralLossesMap getDefaultLosses(Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences, 
            SequenceMatchingPreferences ptmSequenceMatchingPreferences) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        PTMFactory pTMFactory = PTMFactory.getInstance();
        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        String sequence = peptide.getSequence();
        int aaMin = sequence.length();
        int aaMax = 0;

        if (IonFactory.getInstance().getDefaultNeutralLosses().contains(NeutralLoss.H2O)) {
            int firstIndex = sequence.indexOf("D");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("D"), aaMax);
            }
            firstIndex = sequence.indexOf("E");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("E"), aaMax);
            }
            firstIndex = sequence.indexOf("S");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("S"), aaMax);
            }
            firstIndex = sequence.indexOf("T");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("T"), aaMax);
            }
            if (aaMin < sequence.length()) {
                neutralLossesMap.addNeutralLoss(NeutralLoss.H2O, aaMin + 1, sequence.length() - aaMax);
            }
        }

        aaMin = sequence.length();
        aaMax = 0;

        if (IonFactory.getInstance().getDefaultNeutralLosses().contains(NeutralLoss.NH3)) {
            int firstIndex = sequence.indexOf("K");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("K"), aaMax);
            }
            firstIndex = sequence.indexOf("N");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("N"), aaMax);
            }
            firstIndex = sequence.indexOf("Q");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("Q"), aaMax);
            }
            firstIndex = sequence.indexOf("R");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(sequence.lastIndexOf("R"), aaMax);
            }
            if (aaMin < sequence.length()) {
                neutralLossesMap.addNeutralLoss(NeutralLoss.NH3, aaMin + 1, sequence.length() - aaMax);
            }
        }

        int modMin = sequence.length();
        int modMax = 0;

        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            PTM ptm = pTMFactory.getPTM(modMatch.getTheoreticPtm());
            if (ptm == null) {
                throw new IllegalArgumentException("PTM " + modMatch.getTheoreticPtm() + " not loaded in PTM factory.");
            }
            for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                ArrayList<Integer> indexes = peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences);
                if (!indexes.isEmpty()) {
                    Collections.sort(indexes);
                    modMin = indexes.get(0);
                    modMax = indexes.get(indexes.size() - 1);
                }
                neutralLossesMap.addNeutralLoss(neutralLoss, modMin, sequence.length() - modMax + 1);
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
