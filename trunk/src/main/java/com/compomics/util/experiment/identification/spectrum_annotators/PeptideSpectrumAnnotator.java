/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.experiment.identification.spectrum_annotators;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import static com.compomics.util.experiment.identification.SpectrumAnnotator.getSpectrumAnnotation;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Annotates a spectrum with peptide fragments
 *
 * @author Marc
 */
public class PeptideSpectrumAnnotator extends SpectrumAnnotator {
    
    /**
     * The theoretic peptide to match.
     */
    private Peptide peptide;
    /**
     * Constructor
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
        if (this.peptide == null || !this.peptide.isSameAs(peptide) || !this.peptide.sameModificationsAs(peptide) || this.precursorCharge != precursorCharge) {
            this.peptide = peptide;
            this.precursorCharge = precursorCharge;
            theoreticalFragmentIons = fragmentFactory.getFragmentIons(peptide);
            if (massShift != 0 || massShiftNTerm != 0 || massShiftCTerm != 0) {
                for (Ion ion : theoreticalFragmentIons) {
                    if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                        if (ion.getSubType() == PeptideFragmentIon.A_ION || ion.getSubType() == PeptideFragmentIon.B_ION || ion.getSubType() == PeptideFragmentIon.C_ION) {
                            ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);
                        } else if (ion.getSubType() == PeptideFragmentIon.X_ION || ion.getSubType() == PeptideFragmentIon.Y_ION || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                            ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);
                        }
                    }
                }
            }
            spectrumAnnotation.clear();
            unmatchedIons.clear();
        }
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a
     * given peak.
     *
     * @param peptide The peptide
     * @param iontypes The fragment ions selected
     * @param charges The charges of the fragment to search for
     * @param precursorCharge The precursor charge as deduced by the search
     * engine
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * maximal position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param peak The peak to match
     * @return A list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, HashMap<Ion.IonType, ArrayList<Integer>> iontypes, ArrayList<Integer> charges, int precursorCharge, NeutralLossesMap neutralLosses, Peak peak) {
        setPeptide(peptide, precursorCharge);
        return matchPeak(iontypes, charges, precursorCharge, neutralLosses, peak);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param iontypes The expected ions to look for
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * first position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param charges List of expected charges
     * @param precursorCharge the precursor charge
     * @param spectrum The spectrum to match
     * @param peptide The peptide of interest
     * @param intensityLimit The intensity limit to use
     * @param mzTolerance The m/z tolerance to use
     * @param isPpm a boolean indicating whether the mass tolerance is in ppm or
     * in Da
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public ArrayList<IonMatch> getSpectrumAnnotation(HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, MSnSpectrum spectrum, Peptide peptide, double intensityLimit, double mzTolerance, boolean isPpm) {

        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        if (spectrum != null) {
            setSpectrum(spectrum, intensityLimit);
        }

        setPeptide(peptide, precursorCharge);
        setMassTolerance(mzTolerance, isPpm);

        ArrayList<Integer> precursorCharges = new ArrayList<Integer>();

        // we have to keep the precursor charges separate from the fragment ion charges
        for (int i = 1; i <= precursorCharge; i++) {
            precursorCharges.add(i);
        }

        if (theoreticalFragmentIons != null) {
            for (Ion peptideIon : theoreticalFragmentIons) {

                if (iontypes.containsKey(peptideIon.getType())
                        && iontypes.get(peptideIon.getType()).contains(peptideIon.getSubType())
                        && lossesValidated(neutralLosses, peptideIon)) {

                    ArrayList<Integer> tempCharges;

                    // have to treat precursor charges separetly, as to not increase the max charge for the other ions
                    if (peptideIon.getType() == Ion.IonType.PRECURSOR_ION) {
                        tempCharges = precursorCharges;
                    } else {
                        tempCharges = charges;
                    }

                    for (int charge : tempCharges) {
                        if (chargeValidated(peptideIon, charge, precursorCharge)) {
                            String key = IonMatch.getPeakAnnotation(peptideIon, new Charge(Charge.PLUS, charge));
                            if (!spectrumAnnotation.containsKey(key)
                                    && !unmatchedIons.contains(key)) {
                                matchInSpectrum(peptideIon, charge);
                            }
                            if (!unmatchedIons.contains(key)) {
                                result.add(spectrumAnnotation.get(key));
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
     * acid number in the sequence. 1 is first amino-acid.
     *
     * @param iontypes The expected ions to look for
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * first position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param charges List of expected charges
     * @param precursorCharge the precursor charge
     * @param spectrum The spectrum to match
     * @param peptide The peptide of interest
     * @param intensityLimit The intensity limit to use
     * @param mzTolerance The m/z tolerance to use
     * @param isPpm a boolean indicating whether the mass tolerance is in ppm or
     * in Da
     * @return the ion matches corresponding to fragment ions indexed by amino
     * acid number in the sequence
     */
    public HashMap<Integer, ArrayList<IonMatch>> getCoveredAminoAcids(HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, MSnSpectrum spectrum, Peptide peptide, double intensityLimit, double mzTolerance, boolean isPpm) {

        HashMap<Integer, ArrayList<IonMatch>> matchesMap = new HashMap<Integer, ArrayList<IonMatch>>();
        ArrayList<IonMatch> matches = getSpectrumAnnotation(iontypes, neutralLosses, charges, precursorCharge, spectrum, peptide, intensityLimit, mzTolerance, isPpm);

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
     * @param iontypes The expected ions to look for
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * first position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param charges List of expected charges
     * @param peptide The peptide of interest
     * @param precursorCharge The precursor charge
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public HashMap<Integer, ArrayList<Ion>> getExpectedIons(HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int precursorCharge, Peptide peptide) {
        setPeptide(peptide, precursorCharge);
        return getExpectedIons(iontypes, neutralLosses, charges, precursorCharge);
    }
    
    @Override
    public ArrayList<IonMatch> getCurrentAnnotation(MSnSpectrum spectrum, HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses, ArrayList<Integer> charges) {
        return getSpectrumAnnotation(iontypes, neutralLosses, charges, precursorCharge, spectrum, peptide, intensityLimit, mzTolerance, isPpm);
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide. /!\ this method will work only if the PTM found in the peptide
     * are in the PTMFactory.
     *
     * @param peptide the peptide of interest
     * @return the expected possible neutral losses
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public static NeutralLossesMap getDefaultLosses(Peptide peptide) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        PTMFactory pTMFactory = PTMFactory.getInstance();
        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        int aaMin = peptide.getSequence().length();
        int aaMax = 0;

        if (IonFactory.getInstance().getDefaultNeutralLosses().contains(NeutralLoss.H2O)) {
            int firstIndex = peptide.getSequence().indexOf("D");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("D"), aaMax);
            }
            firstIndex = peptide.getSequence().indexOf("E");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("E"), aaMax);
            }
            firstIndex = peptide.getSequence().indexOf("S");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("S"), aaMax);
            }
            firstIndex = peptide.getSequence().indexOf("T");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("T"), aaMax);
            }
            if (aaMin < peptide.getSequence().length()) {
                neutralLossesMap.addNeutralLoss(NeutralLoss.H2O, aaMin + 1, peptide.getSequence().length() - aaMax);
            }
        }

        aaMin = peptide.getSequence().length();
        aaMax = 0;

        if (IonFactory.getInstance().getDefaultNeutralLosses().contains(NeutralLoss.NH3)) {
            int firstIndex = peptide.getSequence().indexOf("K");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("K"), aaMax);
            }
            firstIndex = peptide.getSequence().indexOf("N");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("N"), aaMax);
            }
            firstIndex = peptide.getSequence().indexOf("Q");
            if (firstIndex != -1) {
                aaMin = Math.min(firstIndex, aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("Q"), aaMax);
            }
            if (aaMin < peptide.getSequence().length()) {
                neutralLossesMap.addNeutralLoss(NeutralLoss.NH3, aaMin + 1, peptide.getSequence().length() - aaMax);
            }
        }

        int modMin = peptide.getSequence().length();
        int modMax = 0;

        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            PTM ptm = pTMFactory.getPTM(modMatch.getTheoreticPtm());
            if (ptm == null) {
                throw new IllegalArgumentException("PTM " + modMatch.getTheoreticPtm() + " not loaded in PTM factory.");
            }
            for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                ArrayList<Integer> indexes = peptide.getPotentialModificationSites(ptm, ProteinMatch.MatchingType.string, Double.NaN);
                if (!indexes.isEmpty()) {
                    Collections.sort(indexes);
                    modMin = indexes.get(0);
                    modMax = indexes.get(indexes.size() - 1);
                }
                neutralLossesMap.addNeutralLoss(neutralLoss, modMin, peptide.getSequence().length() - modMax + 1);
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
