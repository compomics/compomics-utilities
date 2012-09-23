package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Ion.IonType;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

/**
 * The spectrum annotator annotates peaks in a spectrum.
 *
 * @author Marc
 */
public class SpectrumAnnotator {

    /**
     * The theoretic peptide to match.
     */
    private Peptide peptide;
    /**
     * The precursor charge as deduced by the search engine.
     */
    private int precursorCharge;
    /**
     * The theoretic ions for the selected peptide.
     */
    private ArrayList<Ion> peptideIons;
    /**
     * The Fragment factory which will generate the fragment ions.
     */
    private IonFactory fragmentFactory = IonFactory.getInstance();
    /**
     * The key of the currently loaded spectrum.
     */
    private String spectrumKey = "";
    /**
     * The intensity limit to use.
     */
    private double intensityLimit = 0;
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
    private HashMap<String, IonMatch> spectrumAnnotation = new HashMap<String, IonMatch>();
    /**
     * List of unmatched ions.
     */
    private ArrayList<String> unmatchedIons = new ArrayList<String>();
    /**
     * The m/z tolerance for peak matching.
     */
    private double mzTolerance;
    /**
     * boolean indicating whether the tolerance is in ppm (true) or in Dalton
     * (false)
     */
    private boolean isPpm;
    /**
     * m/z shift applied to all theoretic peaks.
     */
    private double massShift = 0;

    /**
     * Constructor.
     */
    public SpectrumAnnotator() {
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
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();
        IonMatch ionMatch;
        if (iontypes.containsKey(Ion.IonType.PRECURSOR_ION)) {
            charges.add(precursorCharge);
            charges.add(precursorCharge+1);
        }

        for (Ion peptideIon : peptideIons) {
            if (iontypes.containsKey(peptideIon.getType())
                    && iontypes.get(peptideIon.getType()).contains(peptideIon.getSubType())) {
                for (int charge : charges) {
                    if (chargeValidated(peptideIon, charge, precursorCharge) 
                            && lossesValidated(neutralLosses, peptideIon, peptide)) {
                        ionMatch = new IonMatch(peak, peptideIon, new Charge(Charge.PLUS, charge));
                        if (Math.abs(ionMatch.getError(isPpm)) <= mzTolerance) {
                            result.add(ionMatch);
                        }
                    }
                }
            }
        }
        return result;
    }

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
            currentAnnotations.add(new DefaultSpectrumAnnotation(ionMatch.peak.mz, ionMatch.getAbsoluteError(),
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
    private void matchInSpectrum(Ion theoreticIon, int inspectedCharge) {

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
            
            if (Math.abs(tempMatch.getError(isPpm)) <= mzTolerance) {
                Peak currentPeak = peakMap.get(mz.get(indexMax));
                bestMatch = new IonMatch(currentPeak, theoreticIon, charge);
            }

            tempMatch = new IonMatch(new Peak(mz.get(indexMin), 0), theoreticIon, charge);
            
            if (Math.abs(tempMatch.getError(isPpm)) <= mzTolerance) {
                Peak currentPeak = peakMap.get(mz.get(indexMin));
                if (bestMatch == null || bestMatch.peak.intensity < currentPeak.intensity) {
                    bestMatch = new IonMatch(currentPeak, theoreticIon, charge);
                }
            }

            while (indexMax - indexMin > 1) {
                
                int index = (indexMax - indexMin) / 2 + indexMin;
                double currentMz = mz.get(index);
                tempMatch = new IonMatch(new Peak(currentMz, 0), theoreticIon, charge);
                
                if (Math.abs(tempMatch.getError(isPpm)) <= mzTolerance) {
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
    private void setSpectrum(MSnSpectrum spectrum, double intensityLimit) {
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
    private void setMassTolerance(double mzTolerance, boolean isPpm) {
        if (mzTolerance != this.mzTolerance) {
            spectrumAnnotation.clear();
            unmatchedIons.clear();
            this.mzTolerance = mzTolerance;
            this.isPpm = isPpm;
        }
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
            peptideIons = fragmentFactory.getFragmentIons(peptide);
            if (massShift != 0) {
                for (Ion ion : peptideIons) {
                    if (ion.getType() == IonType.PEPTIDE_FRAGMENT_ION ) {
                        ion.setTheoreticMass(ion.getTheoreticMass() + massShift);
                    }
                }
            }
            spectrumAnnotation.clear();
            unmatchedIons.clear();
        }
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide. /!\ this method will work only if the ptm found in the peptide
     * are in the PTMFactory.
     *
     * @param peptide the peptide of interest
     * @return the expected possible neutral losses
     */
    public static NeutralLossesMap getDefaultLosses(Peptide peptide) {

        PTMFactory pTMFactory = PTMFactory.getInstance();
        PTM ptm;
        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        int aaMin = peptide.getSequence().length();
        int aaMax = 0;
        if (IonFactory.getInstance().getDefaultNeutralLosses().contains(NeutralLoss.H2O)) {
            if (peptide.getSequence().indexOf("D") != -1) {
                aaMin = Math.min(peptide.getSequence().indexOf("D"), aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("D"), aaMax);
            }
            if (peptide.getSequence().indexOf("E") != -1) {
                aaMin = Math.min(peptide.getSequence().indexOf("E"), aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("E"), aaMax);
            }
            if (peptide.getSequence().indexOf("S") != -1) {
                aaMin = Math.min(peptide.getSequence().indexOf("S"), aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("S"), aaMax);
            }
            if (peptide.getSequence().indexOf("T") != -1) {
                aaMin = Math.min(peptide.getSequence().indexOf("T"), aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("T"), aaMax);
            }
            if (aaMin < peptide.getSequence().length()) {
                neutralLossesMap.addNeutralLoss(NeutralLoss.H2O, aaMin + 1, peptide.getSequence().length() - aaMax);
            }
        }

        aaMin = peptide.getSequence().length();
        aaMax = 0;
        if (IonFactory.getInstance().getDefaultNeutralLosses().contains(NeutralLoss.NH3)) {
            if (peptide.getSequence().indexOf("K") != -1) {
                aaMin = Math.min(peptide.getSequence().indexOf("K"), aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("K"), aaMax);
            }
            if (peptide.getSequence().indexOf("N") != -1) {
                aaMin = Math.min(peptide.getSequence().indexOf("N"), aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("N"), aaMax);
            }
            if (peptide.getSequence().indexOf("Q") != -1) {
                aaMin = Math.min(peptide.getSequence().indexOf("Q"), aaMin);
                aaMax = Math.max(peptide.getSequence().lastIndexOf("Q"), aaMax);
            }
            if (aaMin < peptide.getSequence().length()) {
                neutralLossesMap.addNeutralLoss(NeutralLoss.NH3, aaMin + 1, peptide.getSequence().length() - aaMax);
            }
        }

        int modMin;
        int modMax;
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            ptm = pTMFactory.getPTM(modMatch.getTheoreticPtm());
            for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                modMin = peptide.getSequence().length();
                modMax = 0;
                for (String aa : ptm.getResidues()) {
                    if (aa.equals("[")) {
                        modMin = 0;
                    } else if (aa.equals("]")) {
                        modMax = peptide.getSequence().length();
                    } else {
                        modMin = Math.min(modMin, peptide.getSequence().indexOf(aa));
                        modMax = Math.max(modMax, peptide.getSequence().lastIndexOf(aa));
                    }
                }
                neutralLossesMap.addNeutralLoss(neutralLoss, aaMin + 1, peptide.getSequence().length() - aaMax);
            }
        }

        return neutralLossesMap;
    }

    /**
     * Returns a boolean indicating whether the neutral loss should be accounted
     * for.
     *
     * @param neutralLosses Map of expected neutral losses
     * @param neutralLoss the neutral loss of interest
     * @param ion the fragment ion of interest
     * @param peptide the peptide of interest
     * @return boolean indicating whether the neutral loss should be considered
     */
    public boolean isAccounted(NeutralLossesMap neutralLosses, NeutralLoss neutralLoss, Ion ion, Peptide peptide) {
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
     * @param peptide the inspected peptide
     * @return a boolean indicating whether the neutral losses of the given
     * fragment ion are fit the requirement of the given neutral losses map
     */
    public boolean lossesValidated(NeutralLossesMap neutralLosses, Ion theoreticIon, Peptide peptide) {
        for (NeutralLoss neutralLoss : theoreticIon.getNeutralLosses()) {
            if (!isAccounted(neutralLosses, neutralLoss, theoreticIon, peptide)) {
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
            case PRECURSOR_ION:
                return charge >= precursorCharge; // @TODO take into account lower charge? Like precursor -iTRAQ+?
            default:
                throw new UnsupportedOperationException("Ion type " + theoreticIon.getTypeAsString() + " not implemented in the spectrum annotator.");
        }
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
        for (int i=1; i<=precursorCharge; i++) {
            precursorCharges.add(i);
        }
  
        for (Ion peptideIon : peptideIons) {
            
            if (iontypes.containsKey(peptideIon.getType())
                    && iontypes.get(peptideIon.getType()).contains(peptideIon.getSubType())
                    && lossesValidated(neutralLosses, peptideIon, peptide)) {
                
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
        
        return result;
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

        HashMap<Integer, ArrayList<Ion>> result = new HashMap<Integer, ArrayList<Ion>>();
        setPeptide(peptide, precursorCharge);

        for (Ion peptideIon : peptideIons) {
            if (iontypes.containsKey(peptideIon.getType())
                    && iontypes.get(peptideIon.getType()).contains(peptideIon.getSubType())
                    && lossesValidated(neutralLosses, peptideIon, peptide)) {
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
     * Returns the currently matched ions with the given settings.
     *
     * @param iontypes The expected fragment ions to look for
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * first position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param charges List of expected charges
     * @return the currently matched ions with the given settings
     */
    public ArrayList<IonMatch> getCurrentAnnotation(HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses, ArrayList<Integer> charges) {
        return getSpectrumAnnotation(iontypes, neutralLosses, charges, precursorCharge, null, peptide, intensityLimit, mzTolerance, isPpm);
    }

    /**
     * Returns the spectrum currently inspected.
     *
     * @return the spectrum currently inspected
     */
    public String getCurrentlyLoadedSpectrumKey() {
        return spectrumKey;
    }

    /**
     * Returns the currently inspected peptide.
     *
     * @return the currently inspected peptide
     */
    public Peptide getCurrentlyLoadedPeptide() {
        return peptide;
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
     * Sets an m/z shift on all ions. The previous mass shift will be removed.
     *
     * @param massShift the m/z shift to apply
     */
    public void setMassShift(double massShift) {
        spectrumAnnotation.clear();
        unmatchedIons.clear();
        for (Ion ion : peptideIons) { 
            if (ion.getType() == IonType.PEPTIDE_FRAGMENT_ION ) {
                ion.setTheoreticMass(ion.getTheoreticMass() + massShift);
            } 
        }
        this.massShift = massShift;
    }
}
