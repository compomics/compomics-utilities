package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.FragmentFactory;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
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
     * The theoretic peptide to match
     */
    private Peptide peptide;
    /**
     * The theoretic fragment ions for the selected peptide
     */
    private ArrayList<PeptideFragmentIon> fragmentIons;
    /**
     * The Fragment factory which will generate the fragment ions
     */
    private FragmentFactory fragmentFactory = FragmentFactory.getInstance();
    /**
     * The key of the currently loaded spectrum
     */
    private String spectrumKey = "";
    /**
     * The intensity limit to use
     */
    private double intensityLimit = 0;
    /**
     * A list of the mz of the peak in the loaded spectrum
     */
    private ArrayList<Double> mz;
    /**
     * A map of all peaks mz -> peak
     */
    private HashMap<Double, Peak> peakMap;
    /**
     * The spectrum annotation as a map: theoretic fragment key -> ionmatch
     */
    private HashMap<String, IonMatch> spectrumAnnotation = new HashMap<String, IonMatch>();
    /**
     * List of unmatched ions
     */
    private ArrayList<String> unmatchedIons = new ArrayList<String>();
    /**
     * Separator for the theoretic fragment key components
     */
    public static final String SEPARATOR = "|";
    /**
     * The mass tolerance for peak matching
     */
    private double massTolerance;
    /**
     * m/z shift applied to all theoretic peaks
     */
    private double massShift = 0;

    /**
     * Constructor
     */
    public SpectrumAnnotator() {
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a given peak.
     * 
     * @param peptide       The peptide
     * @param iontypes      The fragment ions selected
     * @param charges       The charges of the fragment to search for 
     * @param neutralLosses Map of expected neutral losses: neutral loss -> maximal position in the sequence (first aa is 1). let null if neutral losses should not be considered.
     * @param peak          The peak to match
     * @param massTolerance The mass tolerance to use (in Dalton)
     * @return              A list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, ArrayList<PeptideFragmentIonType> iontypes, ArrayList<Integer> charges, NeutralLossesMap neutralLosses, Peak peak, double massTolerance) {

        setPeptide(peptide);
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            if (iontypes.contains(fragmentIon.getType())) {
                for (int charge : charges) {
                    if (chargeValidated(fragmentIon, charge) && lossesValidated(neutralLosses, fragmentIon, peptide)) {
                        if (Math.abs(fragmentIon.theoreticMass + charge * Atom.H.mass - peak.mz * charge) <= massTolerance) {
                            result.add(new IonMatch(peak, fragmentIon, new Charge(Charge.PLUS, charge)));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Translates the list of ion matches into a vector of annotations which can be read by the SpectrumPAnel
     * @param ionMatches list of ion matches
     * @return vector of default spectrum annotations
     */
    public static Vector<DefaultSpectrumAnnotation> getSpectrumAnnotation(ArrayList<IonMatch> ionMatches) {
        Vector<DefaultSpectrumAnnotation> currentAnnotations = new Vector();
        for (IonMatch ionMatch : ionMatches) {
            currentAnnotations.add(new DefaultSpectrumAnnotation(ionMatch.peak.mz, ionMatch.getAbsoluteError(),
                    SpectrumPanel.determineColorOfPeak(ionMatch.ion.toString()), ionMatch.getPeakAnnotation()));
        }
        return currentAnnotations;
    }

    /**
     * Annotates a spectrum and returns a map containing the annotations: ion type -> charge -> Ion match
     * 
     * @param peptide           The theoretic peptide to match
     * @param spectrum          The spectrum
     * @param massTolerance     The mass tolerance to use (in Dalton)
     * @param intensityLimit    The minimal intensity to search for
     * @return                  a map containing the annotations
     */
    private void matchInSpectrum(PeptideFragmentIon peptideFragmentIon, int inspectedCharge) {

        double fragmentMass, currentMass;
        IonMatch bestMatch = null;

        fragmentMass = peptideFragmentIon.theoreticMass + inspectedCharge * Ion.proton().theoreticMass;

        if (!mz.isEmpty() && fragmentMass >= inspectedCharge * mz.get(0) - inspectedCharge * massTolerance
                && fragmentMass <= inspectedCharge * mz.get(mz.size() - 1) + inspectedCharge * massTolerance) {

            int indexMin = 0;
            int indexMax = mz.size() - 1;
            int index;
            Peak currentPeak;

            currentMass = inspectedCharge * mz.get(indexMax);

            if (Math.abs(currentMass - fragmentMass) <= inspectedCharge * massTolerance) {
                currentPeak = peakMap.get(mz.get(indexMax));
                bestMatch = new IonMatch(currentPeak, peptideFragmentIon, new Charge(Charge.PLUS, inspectedCharge));
            }

            currentMass = inspectedCharge * mz.get(indexMin);

            if (Math.abs(currentMass - fragmentMass) <= inspectedCharge * massTolerance) {
                currentPeak = peakMap.get(mz.get(indexMin));
                if (bestMatch == null || bestMatch.peak.intensity < currentPeak.intensity) {
                    bestMatch = new IonMatch(currentPeak, peptideFragmentIon, new Charge(Charge.PLUS, inspectedCharge));
                }
            }

            while (indexMax - indexMin > 1) {
                index = (indexMax - indexMin) / 2 + indexMin;
                currentMass = inspectedCharge * mz.get(index);
                if (Math.abs(currentMass - fragmentMass) <= massTolerance * inspectedCharge) {
                    currentPeak = peakMap.get(mz.get(index));
                    if (bestMatch == null || bestMatch.peak.intensity < currentPeak.intensity) {
                        bestMatch = new IonMatch(currentPeak, peptideFragmentIon, new Charge(Charge.PLUS, inspectedCharge));
                    }
                }

                if (currentMass < fragmentMass) {
                    indexMin = index;
                } else {
                    indexMax = index;
                }
            }
        }
        if (bestMatch != null) {
            spectrumAnnotation.put(getTheoreticFragmentKey(peptideFragmentIon, inspectedCharge), bestMatch);
        } else {
            unmatchedIons.add(getTheoreticFragmentKey(peptideFragmentIon, inspectedCharge));
        }
    }

    /**
     * Sets a new spectrum to annotate
     * @param spectrum          The spectrum to inspect
     * @param intensityLimit    the minimal intensity to account for
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
     * Sets a new m/z for peak matching
     * @param massTolerance the new m/z tolerance (in m/z, Th)
     */
    private void setMassTolerance(double massTolerance) {
        if (massTolerance != this.massTolerance) {
            spectrumAnnotation.clear();
            unmatchedIons.clear();
            this.massTolerance = massTolerance;
        }
    }

    /**
     * Sets a new peptide to match
     * @param peptide   the new peptide
     */
    public void setPeptide(Peptide peptide) {
        if (this.peptide == null || !this.peptide.isSameAs(peptide) || !this.peptide.sameModificationsAs(peptide)) {
            this.peptide = peptide;
            fragmentIons = fragmentFactory.getFragmentIons(peptide);
            if (massShift != 0) {
                for (PeptideFragmentIon fragmentIon : fragmentIons) {
                    fragmentIon.theoreticMass += massShift;
                }
            }
            spectrumAnnotation.clear();
            unmatchedIons.clear();
        }
    }
    
    /**
     * Returns the possible neutral losses expected by default for a given peptide.
     * /!\ this method will work only if the ptm found in the peptide are in the PTMFactory
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
            neutralLossesMap.addNeutralLoss(NeutralLoss.H2O, aaMin+1, peptide.getSequence().length()-aaMax);
        }

        aaMin = peptide.getSequence().length();
        aaMax = 0;
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
            neutralLossesMap.addNeutralLoss(NeutralLoss.NH3, aaMin+1, peptide.getSequence().length()-aaMax);
        }

        int aaMinHPO3 = peptide.getSequence().length();
        int aaMinH3PO4 = peptide.getSequence().length();
        int aaMinCH4OS = peptide.getSequence().length();
        int aaMaxHPO3 = 0;
        int aaMaxH3PO4 = 0;
        int aaMaxCH4OS = 0;
        
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            ptm = pTMFactory.getPTM(modMatch.getTheoreticPtm());
            if (Math.abs(ptm.getMass() - 79.9663) < 0.01) { // @TODO: why are these masses hard coded here?!
                if (peptide.getSequence().charAt(modMatch.getModificationSite() - 1) == 'Y') {
                    aaMinHPO3 = Math.min(aaMinHPO3, modMatch.getModificationSite() + 1);
                    aaMaxHPO3 = Math.max(aaMaxHPO3, modMatch.getModificationSite() + 1);
                } else if (peptide.getSequence().charAt(modMatch.getModificationSite() - 1) == 'S'
                        || peptide.getSequence().charAt(modMatch.getModificationSite() - 1) == 'T') {
                    aaMinH3PO4 = Math.min(aaMinH3PO4, modMatch.getModificationSite());
                    aaMaxH3PO4 = Math.max(aaMaxH3PO4, modMatch.getModificationSite());
                }
            } else if (Math.abs(ptm.getMass() - 15.9949) < 0.01) { // @TODO: why are these masses hard coded here?!
                if (peptide.getSequence().charAt(modMatch.getModificationSite() - 1) == 'M') {
                    aaMinCH4OS = Math.min(aaMinCH4OS, modMatch.getModificationSite());
                    aaMaxCH4OS = Math.max(aaMaxCH4OS, modMatch.getModificationSite());
                }
            }
        }
        if (aaMinHPO3 < peptide.getSequence().length()) {
            neutralLossesMap.addNeutralLoss(NeutralLoss.HPO3, aaMinHPO3+1, peptide.getSequence().length()-aaMaxHPO3);
        }
        if (aaMinH3PO4 < peptide.getSequence().length()) {
            neutralLossesMap.addNeutralLoss(NeutralLoss.H3PO4, aaMinH3PO4+1, peptide.getSequence().length()-aaMaxH3PO4);
        }
        if (aaMinCH4OS < peptide.getSequence().length()) {
            neutralLossesMap.addNeutralLoss(NeutralLoss.CH4OS, aaMinCH4OS+1, peptide.getSequence().length()-aaMaxCH4OS);
        }

        return neutralLossesMap;
    }

    /**
     * Returns a boolean indicating whether the neutral loss should be accounted for
     * 
     * @param neutralLosses     Map of expected neutral losses
     * @param neutralLoss       the neutral loss of interest
     * @param fragmentIon       the fragment ion of interest
     * @param peptide           the peptide of interest
     * @return boolean indicating whether the neutral loss should be considered
     */
    public boolean isAccounted(NeutralLossesMap neutralLosses, NeutralLoss neutralLoss, PeptideFragmentIon fragmentIon, Peptide peptide) {
        if (neutralLosses == null || neutralLosses.isEmpty() || fragmentIon.getType() == PeptideFragmentIonType.IMMONIUM) {
            return false;
        }
        for (NeutralLoss neutralLossRef : neutralLosses.getAccountedNeutralLosses()) {
            if (neutralLoss.isSameAs(neutralLossRef)) {
                if (fragmentIon.getType() == PeptideFragmentIonType.PRECURSOR_ION
                        || fragmentIon.getType() == PeptideFragmentIonType.UNKNOWN) {
                    return true;
                } else if (fragmentIon.getType() == PeptideFragmentIonType.A_ION
                        || fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                    return neutralLosses.getBStart(neutralLossRef) <= fragmentIon.getNumber();
                } else if (fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                    return neutralLosses.getYStart(neutralLossRef) <= fragmentIon.getNumber();
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating whether the neutral losses of the given fragment ion are fit the requirement of the given neutral losses map
     * 
     * @param neutralLosses     Map of expected neutral losses: neutral loss.
     * @param fragmentIon       the fragment ion of interest
     * @param peptide           the inspected peptide
     * @return a boolean indicating whether the neutral losses of the given fragment ion are fit the requirement of the given neutral losses map
     */
    public boolean lossesValidated(NeutralLossesMap neutralLosses, PeptideFragmentIon fragmentIon, Peptide peptide) {
        for (NeutralLoss neutralLoss : fragmentIon.getNeutralLosses()) {
            if (!isAccounted(neutralLosses, neutralLoss, fragmentIon, peptide)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a boolean indicating whether the given charge can be found on the given fragment ion
     * @param fragmentIon   the fragment ion of interest
     * @param charge        the candidate charge
     * @return a boolean indicating whether the given charge can be found on the given fragment ion
     */
    public boolean chargeValidated(PeptideFragmentIon fragmentIon, int charge) {
        if (charge > 1) {
            if (fragmentIon.getType() == PeptideFragmentIonType.IMMONIUM) {
                return false;
            } else if (fragmentIon.getType() == PeptideFragmentIonType.A_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.B_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.C_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.X_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.Y_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                return charge <= fragmentIon.getNumber();
            }
        }
        return true;
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches
     * 
     * Note that, except for +1 precursors, fragments ions will be expected to have a charge strictly smaller than the precursor ion charge.
     * 
     * @param expectedFragmentIons  The expected fragment ions to look for
     * @param neutralLosses         Map of expected neutral losses: neutral loss -> first position in the sequence (first aa is 1). let null if neutral losses should not be considered.
     * @param charges               List of expected charges
     * @param spectrum              The spectrum to match
     * @param peptide               The peptide of interest
     * @param intensityLimit        The intensity limit to use
     * @param mzTolerance           The m/z tolerance to use
     * @return an ArrayList of IonMatch containing the ion matches with the given settings
     */
    public ArrayList<IonMatch> getSpectrumAnnotation(ArrayList<PeptideFragmentIonType> expectedFragmentIons, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, MSnSpectrum spectrum, Peptide peptide, double intensityLimit, double mzTolerance) {
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();
        if (spectrum != null) {
            setSpectrum(spectrum, intensityLimit);
        }
        setPeptide(peptide);
        setMassTolerance(mzTolerance);
        String key;
        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            if (expectedFragmentIons.contains(fragmentIon.getType())
                    && lossesValidated(neutralLosses, fragmentIon, peptide)) {
                for (int charge : charges) {
                    if (chargeValidated(fragmentIon, charge)) {
                        key = getTheoreticFragmentKey(fragmentIon, charge);
                        if (!spectrumAnnotation.containsKey(key)
                                && !unmatchedIons.contains(key)) {
                            matchInSpectrum(fragmentIon, charge);
                        }
                        if (!unmatchedIons.contains(key)) {
                            result.add(spectrumAnnotation.get(key));
                        }
                    }
                }
                if (fragmentIon.getType() == PeptideFragmentIonType.PRECURSOR_ION) {
                    int precursorCharge = spectrum.getPrecursor().getCharge().value;
                    key = getTheoreticFragmentKey(fragmentIon, precursorCharge);
                    if (!spectrumAnnotation.containsKey(key)
                            && !unmatchedIons.contains(key)) {
                        matchInSpectrum(fragmentIon, precursorCharge);
                    }
                    if (!unmatchedIons.contains(key)) {
                        result.add(spectrumAnnotation.get(key));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges
     * 
     * Note that, except for +1 precursors, fragments ions will be expected to have a charge strictly smaller than the precursor ion charge.
     * 
     * @param expectedFragmentIons  The expected fragment ions to look for
     * @param neutralLosses         Map of expected neutral losses: neutral loss -> first position in the sequence (first aa is 1). let null if neutral losses should not be considered.
     * @param charges               List of expected charges
     * @param peptide               The peptide of interest
     * @param precursorCharge       The precursor charge
     * @return an ArrayList of IonMatch containing the ion matches with the given settings
     */
    public HashMap<Integer, ArrayList<PeptideFragmentIon>> getExpectedIons(ArrayList<PeptideFragmentIonType> expectedFragmentIons,
            NeutralLossesMap neutralLosses, ArrayList<Integer> charges, Peptide peptide, int precursorCharge) {

        HashMap<Integer, ArrayList<PeptideFragmentIon>> result = new HashMap<Integer, ArrayList<PeptideFragmentIon>>();
        setPeptide(peptide);

        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            if (expectedFragmentIons.contains(fragmentIon.getType())
                    && lossesValidated(neutralLosses, fragmentIon, peptide)) {
                for (int charge : charges) {
                    if (chargeValidated(fragmentIon, charge)) {
                        if (!result.containsKey(charge)) {
                            result.put(charge, new ArrayList<PeptideFragmentIon>());
                        }
                        result.get(charge).add(fragmentIon);
                    }
                }
                if (fragmentIon.getType() == PeptideFragmentIonType.PRECURSOR_ION) {
                    if (chargeValidated(fragmentIon, precursorCharge)) {
                        if (!result.containsKey(precursorCharge)) {
                            result.put(precursorCharge, new ArrayList<PeptideFragmentIon>());
                        }
                        result.get(precursorCharge).add(fragmentIon);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the currently matched ions with the given settings
     * @param expectedFragmentIons  The expected fragment ions to look for
     * @param neutralLosses         Map of expected neutral losses: neutral loss -> first position in the sequence (first aa is 1). let null if neutral losses should not be considered.
     * @param charges               List of expected charges
     * @return the currently matched ions with the given settings
     */
    public ArrayList<IonMatch> getCurrentAnnotation(ArrayList<PeptideFragmentIonType> expectedFragmentIons, NeutralLossesMap neutralLosses, ArrayList<Integer> charges) {
        return getSpectrumAnnotation(expectedFragmentIons, neutralLosses, charges, null, peptide, intensityLimit, massTolerance);
    }

    /**
     * Returns the key of a theoretic fragment
     * @param fragmentIon   the theoretic fragment ion
     * @param charge        the charge of the theoretic fragment
     * @return the key of a theoretic fragment
     */
    public static String getTheoreticFragmentKey(PeptideFragmentIon fragmentIon, int charge) {
        String result = "";
        result += fragmentIon.getIonType() + SEPARATOR;
        result += fragmentIon.getNumber() + SEPARATOR;
        result += charge;
        for (NeutralLoss neutralLoss : fragmentIon.getNeutralLosses()) {
            result += neutralLoss.name;
        }
        return result;
    }

    /**
     * Returns the spectrum currently inspected
     * @return the spectrum currently inspected 
     */
    public String getCurrentlyLoadedSpectrumKey() {
        return spectrumKey;
    }

    /**
     * Returns the currently inspected peptide
     * @return the currently inspected peptide 
     */
    public Peptide getCurrentlyLoadedPeptide() {
        return peptide;
    }

    /**
     * Returns the m/z shift applied to the fragment ions
     * @return the m/z shift applied to the fragment ions 
     */
    public double getMassShift() {
        return massShift;
    }

    /**
     * Sets an m/z shift on all ions. The previous mass shift will be removed
     * @param massShift the m/z shift to apply
     */
    public void setMassShift(double massShift) {
        spectrumAnnotation.clear();
        unmatchedIons.clear();
        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            fragmentIon.theoreticMass += massShift - this.massShift;
        }
        this.massShift = massShift;
    }
}
