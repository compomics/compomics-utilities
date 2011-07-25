package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.FragmentFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
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
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
     * Constructor
     */
    public SpectrumAnnotator() {
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a given peak.
     * 
     * @param peptide       The peptide
     * @param fragmentTypes The fragment ions selected
     * @param charge        The charges of the fragment to search for 
     * @param neutralLosses Map of expected neutral losses: neutral loss -> maximal position in the sequence (first aa is 1). let null if neutral losses should not be considered.
     * @param peak          The peak to match
     * @param massTolerance The mass tolerance to use (in Dalton)
     * @return              A list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, ArrayList<PeptideFragmentIonType> iontypes, ArrayList<Charge> charges, HashMap<NeutralLoss, Integer> neutralLosses, Peak peak, double massTolerance) {

        setPeptide(peptide);
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            if (iontypes.contains(fragmentIon.getType())) {
                for (Charge charge : charges) {
                    if (chargeValidated(fragmentIon, charge) && lossesValidated(neutralLosses, fragmentIon, peptide)) {
                        if (Math.abs(fragmentIon.theoreticMass + charge.value * Atom.H.mass - peak.mz * charge.value) <= massTolerance) {
                            result.add(new IonMatch(peak, fragmentIon, charge));
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
    public Vector<DefaultSpectrumAnnotation> getSpectrumAnnotation(ArrayList<IonMatch> ionMatches) {
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
    private void matchInSpectrum(PeptideFragmentIon peptideFragmentIon, Charge charge) {

        int inspectedCharge = charge.value;
        double fragmentMass, currentMass;
        IonMatch bestMatch = null;

        fragmentMass = peptideFragmentIon.theoreticMass + inspectedCharge * Atom.H.mass;

        if (fragmentMass >= inspectedCharge * mz.get(0) - massTolerance
                && fragmentMass <= inspectedCharge * mz.get(mz.size() - 1) + massTolerance) {

            int indexMin = 0;
            int indexMax = mz.size() - 1;
            int index;
            Peak currentPeak;

            currentMass = inspectedCharge * mz.get(indexMax);

            if (Math.abs(currentMass - fragmentMass) <= massTolerance) {
                currentPeak = peakMap.get(mz.get(indexMax));
                bestMatch = new IonMatch(currentPeak, peptideFragmentIon, charge);
            }

            currentMass = inspectedCharge * mz.get(indexMin);

            if (Math.abs(currentMass - fragmentMass) <= massTolerance) {
                currentPeak = peakMap.get(mz.get(indexMin));
                if (bestMatch == null || bestMatch.peak.intensity < currentPeak.intensity) {
                    bestMatch = new IonMatch(currentPeak, peptideFragmentIon, charge);
                }
            }

            while (indexMax - indexMin > 1) {
                index = (indexMax - indexMin) / 2 + indexMin;
                currentMass = inspectedCharge * mz.get(index);
                if (Math.abs(currentMass - fragmentMass) <= massTolerance / inspectedCharge) {
                    currentPeak = peakMap.get(mz.get(index));
                    if (bestMatch == null || bestMatch.peak.intensity < currentPeak.intensity) {
                        bestMatch = new IonMatch(currentPeak, peptideFragmentIon, charge);
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
            spectrumAnnotation.put(getTheoreticFragmentKey(peptideFragmentIon, charge), bestMatch);
        } else {
            unmatchedIons.add(getTheoreticFragmentKey(peptideFragmentIon, charge));
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
     * @param massTolerance the new m/z tolerance
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
    private void setPeptide(Peptide peptide) {
        if (this.peptide == null || !this.peptide.isSameAs(peptide) || !this.peptide.sameModificationsAs(peptide)) {
            this.peptide = peptide;
            fragmentIons = fragmentFactory.getFragmentIons(peptide);
            spectrumAnnotation.clear();
            unmatchedIons.clear();
        }
    }

    /**
     * Returns the neutral losses map by default for a given peptide and a given list of neutral losses.
     * 
     * @param peptide the peptide of interest
     * @param imposedNeutralLosses a given list of neutral losses
     * @return the expected possible neutral losses
     */
    public HashMap<NeutralLoss, Integer> getDefaultLosses(Peptide peptide, ArrayList<NeutralLoss> imposedNeutralLosses) {

        HashMap<NeutralLoss, Integer> likelyNeutralLosses = new HashMap<NeutralLoss, Integer>();
        int aaMax;
        for (NeutralLoss neutralLoss : imposedNeutralLosses) {
            if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                aaMax = Math.max(peptide.getSequence().lastIndexOf("D"),
                        Math.max(peptide.getSequence().lastIndexOf("E"),
                        Math.max(peptide.getSequence().lastIndexOf("S"),
                        peptide.getSequence().lastIndexOf("T"))));
                if (aaMax >= 0) {
                    likelyNeutralLosses.put(NeutralLoss.H2O, aaMax + 1);
                }
            } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                aaMax = Math.max(peptide.getSequence().lastIndexOf("K"),
                        Math.max(peptide.getSequence().lastIndexOf("R"),
                        Math.max(peptide.getSequence().lastIndexOf("N"),
                        peptide.getSequence().lastIndexOf("Q"))));
                if (aaMax >= 0) {
                    likelyNeutralLosses.put(NeutralLoss.NH3, aaMax + 1);
                }
            } else if (neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                aaMax = -1;
                for (ModificationMatch modMatch : peptide.getModificationMatches()) {
                    if (Math.abs(modMatch.getTheoreticPtm().getMass() - 79.9663) < 0.01) {
                        if (peptide.getSequence().charAt(modMatch.getModificationSite()) == 'Y') {
                            aaMax = Math.max(aaMax, modMatch.getModificationSite() + 1);
                        }
                    }
                    if (aaMax >= 0) {
                        likelyNeutralLosses.put(NeutralLoss.HPO3, aaMax + 1);
                    }
                }
            } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)) {
                aaMax = -1;
                for (ModificationMatch modMatch : peptide.getModificationMatches()) {
                    if (Math.abs(modMatch.getTheoreticPtm().getMass() - 79.9663) < 0.01) {
                        if (peptide.getSequence().charAt(modMatch.getModificationSite()) == 'S'
                                || peptide.getSequence().charAt(modMatch.getModificationSite()) == 'T') {
                            aaMax = Math.max(aaMax, modMatch.getModificationSite() + 1);
                        }
                    }
                    if (aaMax >= 0) {
                        likelyNeutralLosses.put(NeutralLoss.H3PO4, aaMax + 1);
                    }
                }
            } else if (neutralLoss.isSameAs(NeutralLoss.CH4OS)) {
                aaMax = -1;
                for (ModificationMatch modMatch : peptide.getModificationMatches()) {
                    if (Math.abs(modMatch.getTheoreticPtm().getMass() - 15.9949) < 0.01) {
                        if (peptide.getSequence().charAt(modMatch.getModificationSite()) == 'M') {
                            aaMax = Math.max(aaMax, modMatch.getModificationSite() + 1);
                        }
                    }
                    if (aaMax >= 0) {
                        likelyNeutralLosses.put(NeutralLoss.H3PO4, aaMax + 1);
                    }
                }
            }
        }

        return likelyNeutralLosses;
    }

    /**
     * Returns the possible neutral losses expected by default for a given peptide.
     * 
     * @param peptide the peptide of interest
     * @return the expected possible neutral losses
     */
    public HashMap<NeutralLoss, Integer> getDefaultLosses(Peptide peptide) {

        HashMap<NeutralLoss, Integer> likelyNeutralLosses = new HashMap<NeutralLoss, Integer>();

        int aaMax = Math.max(peptide.getSequence().lastIndexOf("D"),
                Math.max(peptide.getSequence().lastIndexOf("E"),
                Math.max(peptide.getSequence().lastIndexOf("S"),
                peptide.getSequence().lastIndexOf("T"))));
        if (aaMax >= 0) {
            likelyNeutralLosses.put(NeutralLoss.H2O, aaMax + 1);
        }

        aaMax = Math.max(peptide.getSequence().lastIndexOf("K"),
                Math.max(peptide.getSequence().lastIndexOf("R"),
                Math.max(peptide.getSequence().lastIndexOf("N"),
                peptide.getSequence().lastIndexOf("Q"))));
        if (aaMax >= 0) {
            likelyNeutralLosses.put(NeutralLoss.NH3, aaMax + 1);
        }

        int aaMaxHPO3 = -1;
        int aaMaxH3PO4 = -1;
        int aaMaxCH4OS = -1;
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            if (Math.abs(modMatch.getTheoreticPtm().getMass() - 79.9663) < 0.01) {
                if (peptide.getSequence().charAt(modMatch.getModificationSite()) == 'Y') {
                    aaMaxHPO3 = Math.max(aaMaxHPO3, modMatch.getModificationSite() + 1);
                } else if (peptide.getSequence().charAt(modMatch.getModificationSite()) == 'S'
                        || peptide.getSequence().charAt(modMatch.getModificationSite()) == 'T') {
                    aaMaxHPO3 = Math.max(aaMaxH3PO4, modMatch.getModificationSite() + 1);
                }
            } else if (Math.abs(modMatch.getTheoreticPtm().getMass() - 15.9949) < 0.01) {
                if (peptide.getSequence().charAt(modMatch.getModificationSite()) == 'M') {
                    aaMaxHPO3 = Math.max(aaMaxCH4OS, modMatch.getModificationSite() + 1);
                }
            }
        }
        if (aaMaxHPO3 >= 0) {
            likelyNeutralLosses.put(NeutralLoss.HPO3, aaMaxHPO3 + 1);
        }
        if (aaMaxH3PO4 >= 0) {
            likelyNeutralLosses.put(NeutralLoss.H3PO4, aaMaxHPO3 + 1);
        }
        if (aaMaxCH4OS >= 0) {
            likelyNeutralLosses.put(NeutralLoss.CH4OS, aaMaxHPO3 + 1);
        }

        return likelyNeutralLosses;
    }

    /**
     * Returns a boolean indicating whether the neutral loss should be accounted for
     * 
     * @param neutralLosses     Map of expected neutral losses: neutral loss -> first position in the sequence (first aa is 1). let null if neutral losses should not be considered.
     * @param neutralLoss       the neutral loss of interest
     * @param fragmentIon       the fragment ion of interest
     * @param peptide           the peptide of interest
     * @return boolean indicating whether the neutral loss should be considered
     */
    public boolean isAccounted(HashMap<NeutralLoss, Integer> neutralLosses, NeutralLoss neutralLoss, PeptideFragmentIon fragmentIon, Peptide peptide) {
        if (neutralLosses == null || neutralLosses.isEmpty() || fragmentIon.getType() == PeptideFragmentIonType.IMMONIUM) {
            return false;
        }
        for (NeutralLoss neutralLossRef : neutralLosses.keySet()) {
            if (neutralLoss.isSameAs(neutralLossRef)) {
                if (fragmentIon.getType() == PeptideFragmentIonType.MH_ION
                        || fragmentIon.getType() == PeptideFragmentIonType.UNKNOWN) {
                    return true;
                } else if (fragmentIon.getType() == PeptideFragmentIonType.A_ION
                        || fragmentIon.getType() == PeptideFragmentIonType.B_ION) {
                    return neutralLosses.get(neutralLossRef) <= fragmentIon.getNumber();
                } else if (fragmentIon.getType() == PeptideFragmentIonType.Y_ION) {
                    return neutralLosses.get(neutralLossRef) >= peptide.getSequence().length() - fragmentIon.getNumber() + 1;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating whether the neutral losses of the given fragment ion are fit the requirement of the given neutral losses map
     * 
     * @param neutralLosses     Map of expected neutral losses: neutral loss -> first position in the sequence (first aa is 1). let null if neutral losses should not be considered.
     * @param fragmentIon       the fragment ion of interest
     * @param peptide           the inspected peptide
     * @return a boolean indicating whether the neutral losses of the given fragment ion are fit the requirement of the given neutral losses map
     */
    public boolean lossesValidated(HashMap<NeutralLoss, Integer> neutralLosses, PeptideFragmentIon fragmentIon, Peptide peptide) {
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
    public boolean chargeValidated(PeptideFragmentIon fragmentIon, Charge charge) {
        if (charge.value > 1) {
            if (fragmentIon.getType() == PeptideFragmentIonType.IMMONIUM) {
                return false;
            } else if (fragmentIon.getType() == PeptideFragmentIonType.A_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.B_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.C_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.X_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.Y_ION
                    || fragmentIon.getType() == PeptideFragmentIonType.Z_ION) {
                return charge.value <= fragmentIon.getNumber();
            }
        }
        return true;
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches
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
    public ArrayList<IonMatch> getSpectrumAnnotation(ArrayList<PeptideFragmentIonType> expectedFragmentIons, HashMap<NeutralLoss, Integer> neutralLosses, ArrayList<Charge> charges, MSnSpectrum spectrum, Peptide peptide, double intensityLimit, double mzTolerance) {
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();
        setSpectrum(spectrum, intensityLimit);
        setPeptide(peptide);
        setMassTolerance(mzTolerance);
        String key;
        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            if (expectedFragmentIons.contains(fragmentIon.getType())
                    && lossesValidated(neutralLosses, fragmentIon, peptide)) {
                for (Charge charge : charges) {
                    if (chargeValidated(fragmentIon, charge)) {
                        key = getTheoreticFragmentKey(fragmentIon, charge);
                        if (!spectrumAnnotation.containsKey(key)
                                && !unmatchedIons.contains(key)) {
                            matchInSpectrum(fragmentIon, charge);

                        }
                        result.add(spectrumAnnotation.get(key));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the key of a theoretic fragment
     * @param fragmentIon   the theoretic fragment ion
     * @param charge        the charge of the theoretic fragment
     * @return the key of a theoretic fragment
     */
    public static String getTheoreticFragmentKey(PeptideFragmentIon fragmentIon, Charge charge) {
        String result = "";
        result += fragmentIon.getIonType() + SEPARATOR;
        result += fragmentIon.getNumber() + SEPARATOR;
        result += charge.value;
        for (NeutralLoss neutralLoss : fragmentIon.getNeutralLosses()) {
            result += neutralLoss.name;
        }
        return result;
    }
}
