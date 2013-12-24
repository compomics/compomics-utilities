package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Ion.IonType;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.TagFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_annotators.TagSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

/**
 * The spectrum annotator annotates peaks in a spectrum.
 *
 * @author Marc Vaudel
 */
public abstract class SpectrumAnnotator {

    /**
     * The precursor charge as deduced by the search engine.
     */
    protected int precursorCharge;
    /**
     * The theoretic fragment ions.
     */
    protected ArrayList<Ion> theoreticalFragmentIons;
    /**
     * The Fragment factory which will generate the fragment ions.
     */
    protected IonFactory fragmentFactory = IonFactory.getInstance();
    /**
     * The key of the currently loaded spectrum.
     */
    private String spectrumKey = "";
    /**
     * The intensity limit to use.
     */
    protected double intensityLimit = 0;
    /**
     * A list of the mz of the peak in the loaded spectrum.
     */
    private ArrayList<Double> mz;
    /**
     * A map of all peaks mz -> peak.
     */
    private HashMap<Double, Peak> peakMap;
    /**
     * The spectrum annotation as a map: theoretic fragment key -> ionmatch.
     */
    protected HashMap<String, IonMatch> spectrumAnnotation = new HashMap<String, IonMatch>();
    /**
     * List of unmatched ions.
     */
    protected ArrayList<String> unmatchedIons = new ArrayList<String>();
    /**
     * The m/z tolerance for peak matching.
     */
    protected double mzTolerance;
    /**
     * Boolean indicating whether the tolerance is in ppm (true) or in Dalton
     * (false).
     */
    protected boolean isPpm;
    /**
     * Boolean indicating whether the isotopic number shall be removed from the
     * theoretic mass when matching an ion. False by default for ms2 ions.
     */
    protected static final boolean subtractIsotope = false;
    /**
     * m/z shift applied to all theoretic peaks.
     */
    protected double massShift = 0;
    /**
     * N-terminal m/z shift applied to all forward ions.
     */
    protected double massShiftNTerm = 0;
    /**
     * C-terminal m/z shift applied to all reverse ions.
     */
    protected double massShiftCTerm = 0;

    /**
     * Translates the list of ion matches into a vector of annotations which can
     * be read by the SpectrumPanel.
     *
     * @param ionMatches list of ion matches
     * @return vector of default spectrum annotations
     */
    public static Vector<DefaultSpectrumAnnotation> getSpectrumAnnotation(ArrayList<IonMatch> ionMatches) {
        Vector<DefaultSpectrumAnnotation> currentAnnotations = new Vector();
        for (IonMatch ionMatch : ionMatches) {
            currentAnnotations.add(new DefaultSpectrumAnnotation(ionMatch.peak.mz, ionMatch.getAbsoluteError(subtractIsotope),
                    SpectrumPanel.determineFragmentIonColor(ionMatch.ion, true), ionMatch.getPeakAnnotation()));
        }
        return currentAnnotations;
    }

    /**
     * Matches a theoretic ion in the spectrum.
     *
     * @param theoreticIon the theoretic ion
     * @param inspectedCharge the expected charge
     */
    protected void matchInSpectrum(Ion theoreticIon, int inspectedCharge) {

        Charge charge = new Charge(Charge.PLUS, inspectedCharge);
        IonMatch bestMatch = null;
        double fragmentMz = (theoreticIon.getTheoreticMass() + inspectedCharge * ElementaryIon.proton.getTheoreticMass()) / inspectedCharge;

        double deltaMz;

        if (isPpm) {
            deltaMz = (mzTolerance / 1000000) * fragmentMz;
        } else {
            deltaMz = mzTolerance;
        }

        if (!mz.isEmpty() && fragmentMz >= mz.get(0) - deltaMz
                && fragmentMz <= mz.get(mz.size() - 1) + deltaMz) {

            int indexMin = 0;
            int indexMax = mz.size() - 1;

            IonMatch tempMatch = new IonMatch(new Peak(mz.get(indexMax), 0), theoreticIon, charge);

            if (Math.abs(tempMatch.getError(isPpm, subtractIsotope)) <= mzTolerance) {
                Peak currentPeak = peakMap.get(mz.get(indexMax));
                bestMatch = new IonMatch(currentPeak, theoreticIon, charge);
            }

            tempMatch = new IonMatch(new Peak(mz.get(indexMin), 0), theoreticIon, charge);

            if (Math.abs(tempMatch.getError(isPpm, subtractIsotope)) <= mzTolerance) {
                Peak currentPeak = peakMap.get(mz.get(indexMin));
                if (bestMatch == null || bestMatch.peak.intensity < currentPeak.intensity) {
                    bestMatch = new IonMatch(currentPeak, theoreticIon, charge);
                }
            }

            while (indexMax - indexMin > 1) {

                int index = (indexMax - indexMin) / 2 + indexMin;
                double currentMz = mz.get(index);
                tempMatch = new IonMatch(new Peak(currentMz, 0), theoreticIon, charge);

                if (Math.abs(tempMatch.getError(isPpm, subtractIsotope)) <= mzTolerance) {
                    Peak currentPeak = peakMap.get(mz.get(index));
                    if (bestMatch == null || bestMatch.peak.intensity < currentPeak.intensity) {
                        bestMatch = new IonMatch(currentPeak, theoreticIon, charge);
                    }
                }

                if (currentMz < fragmentMz) {
                    indexMin = index;
                } else {
                    indexMax = index;
                }
            }
        }

        if (bestMatch != null) {
            spectrumAnnotation.put(IonMatch.getPeakAnnotation(theoreticIon, charge), bestMatch);
        } else {
            unmatchedIons.add(IonMatch.getPeakAnnotation(theoreticIon, charge));
        }
    }

    /**
     * Sets a new spectrum to annotate.
     *
     * @param spectrum The spectrum to inspect
     * @param intensityLimit the minimal intensity to account for
     */
    protected void setSpectrum(MSnSpectrum spectrum, double intensityLimit) {
        if (!spectrumKey.equals(spectrum.getSpectrumKey()) || this.intensityLimit != intensityLimit) {
            spectrumKey = spectrum.getSpectrumKey();
            this.intensityLimit = intensityLimit;

            if (intensityLimit == 0) {
                peakMap = spectrum.getPeakMap();
                mz = new ArrayList<Double>(peakMap.keySet());
            } else {
                peakMap = new HashMap<Double, Peak>();
                mz = new ArrayList<Double>();
                for (Peak peak : spectrum.getPeakList()) {
                    if (peak.intensity > intensityLimit) {
                        peakMap.put(peak.mz, peak);
                        mz.add(peak.mz);
                    }
                }
            }
            Collections.sort(mz);
            spectrumAnnotation.clear();
            unmatchedIons.clear();
        }
    }

    /**
     * Sets a new m/z tolerance for peak matching.
     *
     * @param mzTolerance the new m/z tolerance (in m/z, Th)
     * @param isPpm a boolean indicating whether the mass tolerance is in ppm or
     * in Da
     */
    protected void setMassTolerance(double mzTolerance, boolean isPpm) {
        if (mzTolerance != this.mzTolerance) {
            spectrumAnnotation.clear();
            unmatchedIons.clear();
            this.mzTolerance = mzTolerance;
            this.isPpm = isPpm;
        }
    }

    /**
     * Returns a boolean indicating whether the neutral loss should be accounted
     * for.
     *
     * @param neutralLosses Map of expected neutral losses
     * @param neutralLoss the neutral loss of interest
     * @param ion the fragment ion of interest
     * @return boolean indicating whether the neutral loss should be considered
     */
    public boolean isAccounted(NeutralLossesMap neutralLosses, NeutralLoss neutralLoss, Ion ion) {
        if (neutralLosses == null || neutralLosses.isEmpty()) {
            return false;
        }
        for (NeutralLoss neutralLossRef : neutralLosses.getAccountedNeutralLosses()) {
            if (neutralLoss.isSameAs(neutralLossRef)) {
                switch (ion.getType()) {
                    case PEPTIDE_FRAGMENT_ION:
                        PeptideFragmentIon peptideFragmentIon = ((PeptideFragmentIon) ion);
                        switch (ion.getSubType()) {
                            case PeptideFragmentIon.A_ION:
                            case PeptideFragmentIon.B_ION:
                            case PeptideFragmentIon.C_ION:
                                return neutralLosses.getBStart(neutralLossRef) <= peptideFragmentIon.getNumber();
                            case PeptideFragmentIon.X_ION:
                            case PeptideFragmentIon.Y_ION:
                            case PeptideFragmentIon.Z_ION:
                                return neutralLosses.getYStart(neutralLossRef) <= peptideFragmentIon.getNumber();
                            default:
                                throw new UnsupportedOperationException("Fragment ion type " + ion.getSubTypeAsString() + " not implemented in the spectrum annotator.");
                        }
                    case TAG_FRAGMENT_ION:
                        TagFragmentIon tagFragmentIon = ((TagFragmentIon) ion);
                        switch (ion.getSubType()) {
                            case TagFragmentIon.A_ION:
                            case TagFragmentIon.B_ION:
                            case TagFragmentIon.C_ION:
                                return neutralLosses.getBStart(neutralLossRef) <= tagFragmentIon.getNumber();
                            case TagFragmentIon.X_ION:
                            case TagFragmentIon.Y_ION:
                            case TagFragmentIon.Z_ION:
                                return neutralLosses.getYStart(neutralLossRef) <= tagFragmentIon.getNumber();
                            default:
                                throw new UnsupportedOperationException("Fragment ion type " + ion.getSubTypeAsString() + " not implemented in the spectrum annotator.");
                        }
                    default:
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating whether the neutral losses of the given
     * fragment ion are fit the requirement of the given neutral losses map.
     *
     * @param neutralLosses Map of expected neutral losses: neutral loss.
     * @param theoreticIon the ion of interest
     * @return a boolean indicating whether the neutral losses of the given
     * fragment ion are fit the requirement of the given neutral losses map
     */
    public boolean lossesValidated(NeutralLossesMap neutralLosses, Ion theoreticIon) {
        for (NeutralLoss neutralLoss : theoreticIon.getNeutralLosses()) {
            if (!isAccounted(neutralLosses, neutralLoss, theoreticIon)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a boolean indicating whether the given charge can be found on the
     * given fragment ion.
     *
     * @param theoreticIon the ion of interest
     * @param charge the candidate charge
     * @param precursorCharge the precursor charge
     * @return a boolean indicating whether the given charge can be found on the
     * given fragment ion
     */
    public boolean chargeValidated(Ion theoreticIon, int charge, int precursorCharge) {
        switch (theoreticIon.getType()) {
            case IMMONIUM_ION:
                return charge == 1;
            case REPORTER_ION: // Note, it is possible to implement higher charges for the reporter ion but then modify IonMatch.getPeakAnnotation(boolean html) as well to see the charge displayed on the spectrum
                return charge == 1;
            case PEPTIDE_FRAGMENT_ION:
                PeptideFragmentIon peptideFragmentIon = ((PeptideFragmentIon) theoreticIon);
                return charge <= peptideFragmentIon.getNumber() && (charge < precursorCharge || precursorCharge == 1);
            case TAG_FRAGMENT_ION:
                TagFragmentIon tagFragmentIon = ((TagFragmentIon) theoreticIon);
                return charge <= tagFragmentIon.getNumber() && (charge < precursorCharge || precursorCharge == 1);
            case PRECURSOR_ION:
//                if ((theoreticIon.getNeutralLossesAsString().lastIndexOf("TMT_C") != -1
//                        || theoreticIon.getNeutralLossesAsString().lastIndexOf("iTRAQ_C") != -1)
//                        && theoreticIon.getNeutralLosses().size() == 1) {
//                    return true; // special case for TMT cluster ions
//                } else {
                return charge >= precursorCharge;
//                }
            default:
                throw new UnsupportedOperationException("Ion type " + theoreticIon.getTypeAsString() + " not implemented in the spectrum annotator.");
        }
    }

    /**
     * Returns the currently matched ions with the given settings.
     *
     * @param spectrum the spectrum of interest
     * @param iontypes The expected fragment ions to look for
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * first position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param charges List of expected charges
     * @return the currently matched ions with the given settings
     */
    public abstract ArrayList<IonMatch> getCurrentAnnotation(MSnSpectrum spectrum, HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses, ArrayList<Integer> charges);

    /**
     * Returns the spectrum currently inspected.
     *
     * @return the spectrum currently inspected
     */
    public String getCurrentlyLoadedSpectrumKey() {
        return spectrumKey;
    }

    /**
     * Returns the m/z shift applied to the fragment ions.
     *
     * @return the m/z shift applied to the fragment ions
     */
    public double getMassShift() {
        return massShift;
    }

    /**
     * Returns the N-terminal m/z shift applied to all forward ions.
     *
     * @return the N-terminal m/z shift applied to all forward ions
     */
    public double getMassShiftNTerm() {
        return massShiftNTerm;
    }

    /**
     * Returns the C-terminal m/z shift applied to all reverse ions.
     *
     * @return the C-terminal m/z shift applied to all reverse ions
     */
    public double getMassShiftCTerm() {
        return massShiftNTerm;
    }

    /**
     * Sets an m/z shift on all ions. The previous mass main shift will be
     * removed.
     *
     * @param aMassShift the m/z shift to apply
     */
    public void setMassShift(double aMassShift) {
        this.massShift = aMassShift;
        updateMassShifts();
    }

    /**
     * Sets the m/z shifts. The previous mass shifts will be removed.
     *
     * @param aMassShift the m/z shift to apply
     * @param aMassShiftNTerm the n-terminal mass shift to apply to all forward
     * ions
     * @param aMassShiftCTerm the c-terminal mass shift to apply to all reverse
     * ions
     */
    public void setMassShifts(double aMassShift, double aMassShiftNTerm, double aMassShiftCTerm) {
        this.massShift = aMassShift;
        this.massShiftNTerm = aMassShiftNTerm;
        this.massShiftCTerm = aMassShiftCTerm;
        updateMassShifts();
    }

    /**
     * Sets the terminal m/z shifts.
     *
     * @param aMassShiftNTerm the n-terminal mass shift to apply to all forward
     * ions
     * @param aMassShiftCTerm the c-terminal mass shift to apply to all reverse
     * ions
     */
    public void setTerminalMassShifts(double aMassShiftNTerm, double aMassShiftCTerm) {
        this.massShiftNTerm = aMassShiftNTerm;
        this.massShiftCTerm = aMassShiftCTerm;
        updateMassShifts();
    }

    /**
     * Updates the mass shifts.
     */
    private void updateMassShifts() {
        spectrumAnnotation.clear();
        unmatchedIons.clear();
        if (theoreticalFragmentIons != null) {
            for (Ion ion : theoreticalFragmentIons) {
                if (ion.getType() == IonType.PEPTIDE_FRAGMENT_ION) {
                    if (ion.getSubType() == PeptideFragmentIon.A_ION || ion.getSubType() == PeptideFragmentIon.B_ION || ion.getSubType() == PeptideFragmentIon.C_ION) {
                        ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);
                    } else if (ion.getSubType() == PeptideFragmentIon.X_ION || ion.getSubType() == PeptideFragmentIon.Y_ION || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                        ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);
                    }
                }
                if (ion.getType() == IonType.TAG_FRAGMENT_ION) {
                    if (ion.getSubType() == TagFragmentIon.A_ION || ion.getSubType() == TagFragmentIon.B_ION || ion.getSubType() == TagFragmentIon.C_ION) {
                        ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);
                    } else if (ion.getSubType() == TagFragmentIon.X_ION || ion.getSubType() == TagFragmentIon.Y_ION || ion.getSubType() == TagFragmentIon.Z_ION) {
                        ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);
                    }
                }
            }
        }
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide. /!\ this method will work only if the PTM found in the peptide
     * are in the PTMFactory.
     *
     * @param spectrumIdentificationAssumption the
     * spectrumIdentificationAssumption of interest
     * @param matchingType the matching type to map ptms on the peptide sequence
     * @param mzTolerance the ms2 m/z tolerance to use
     * 
     * @return the expected possible neutral losses
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public static NeutralLossesMap getDefaultLosses(SpectrumIdentificationAssumption spectrumIdentificationAssumption, AminoAcidPattern.MatchingType matchingType, double mzTolerance) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {
        if (spectrumIdentificationAssumption instanceof PeptideAssumption) {
            PeptideAssumption peptideAssumption = (PeptideAssumption) spectrumIdentificationAssumption;
            return PeptideSpectrumAnnotator.getDefaultLosses(peptideAssumption.getPeptide(), matchingType, mzTolerance);
        } else if (spectrumIdentificationAssumption instanceof TagAssumption) {
            TagAssumption tagAssumption = (TagAssumption) spectrumIdentificationAssumption;
            return TagSpectrumAnnotator.getDefaultLosses(tagAssumption.getTag(), matchingType, mzTolerance);
        } else {
            throw new IllegalArgumentException("Default neutral loss map not implemented for SpectrumIdentificationAssumption " + spectrumIdentificationAssumption.getClass() + ".");
        }
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a
     * given peak. 
     * Note: fragment ions need to be initiated by the SpectrumAnnotator extending class.
     *
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
    protected ArrayList<IonMatch> matchPeak(HashMap<Ion.IonType, ArrayList<Integer>> iontypes, ArrayList<Integer> charges, int precursorCharge, NeutralLossesMap neutralLosses, Peak peak) {

        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        if (iontypes.containsKey(Ion.IonType.PRECURSOR_ION)) {
            charges.add(precursorCharge);
            charges.add(precursorCharge + 1);
        }

        for (Ion peptideIon : theoreticalFragmentIons) {
            if (iontypes.containsKey(peptideIon.getType())
                    && iontypes.get(peptideIon.getType()).contains(peptideIon.getSubType())) {
                for (int charge : charges) {
                    if (chargeValidated(peptideIon, charge, precursorCharge)
                            && lossesValidated(neutralLosses, peptideIon)) {
                        IonMatch ionMatch = new IonMatch(peak, peptideIon, new Charge(Charge.PLUS, charge));
                        if (Math.abs(ionMatch.getError(isPpm, subtractIsotope)) <= mzTolerance) {
                            result.add(ionMatch);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * Note: fragment ions need to be initiated by the SpectrumAnnotator extending class.
     * 
     * @param iontypes The expected ions to look for
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * first position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param charges List of expected charges
     * @param precursorCharge The precursor charge
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    protected HashMap<Integer, ArrayList<Ion>> getExpectedIons(HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, int precursorCharge) {

        HashMap<Integer, ArrayList<Ion>> result = new HashMap<Integer, ArrayList<Ion>>();
        for (Ion peptideIon : theoreticalFragmentIons) {
            if (iontypes.containsKey(peptideIon.getType())
                    && iontypes.get(peptideIon.getType()).contains(peptideIon.getSubType())
                    && lossesValidated(neutralLosses, peptideIon)) {
                for (int charge : charges) {
                    if (chargeValidated(peptideIon, charge, precursorCharge)) {
                        if (!result.containsKey(charge)) {
                            result.put(charge, new ArrayList<Ion>());
                        }
                        result.get(charge).add(peptideIon);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Convenience method to match a reporter ion in a spectrum. The charge is assumed to be 1.
     * 
     * @param theoreticIon the theoretic ion to look for
     * @param spectrum the spectrum
     * @param massTolerance the mass tolerance to use
     * 
     * @return a list of all the ion matches
     */
    public static ArrayList<IonMatch> matchReporterIon(Ion theoreticIon, Spectrum spectrum, double massTolerance) {
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();
        double targetMass = theoreticIon.getTheoreticMass();
        for (double mz : spectrum.getOrderedMzValues()) {
            if (Math.abs(mz-targetMass) <= massTolerance) {
                result.add(new IonMatch(spectrum.getPeakMap().get(mz), theoreticIon, new Charge(Charge.PLUS, 1)));
            }
            if (mz > targetMass + massTolerance) {
                break;
            }
        }
        return result;
    }

}
