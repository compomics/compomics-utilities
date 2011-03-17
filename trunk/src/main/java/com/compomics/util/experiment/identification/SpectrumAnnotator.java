package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.FragmentFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
     * Constructor
     */
    public SpectrumAnnotator() {
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a given peak
     * @param peptide       The peptide
     * @param peak          The peak to match
     * @param massTolerance The mass tolerance to use (in Dalton)
     * @param charge        The charge of the fragment to search for
     * @return              A list of potential ion matches
     */
    public ArrayList<IonMatch> matchPeak(Peptide peptide, Peak peak, double massTolerance, Charge charge) {
        setPeptide(peptide);
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();
        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            if (Math.abs(fragmentIon.theoreticMass + charge.value * Atom.H.mass - peak.mz * charge.value) <= massTolerance) {
                result.add(new IonMatch(peak, new PeptideFragmentIon(fragmentIon.getType(), 
                        fragmentIon.getNumber()), charge));
            }
        }
        return result;
    }

    /**
     * Annotates a spectrum and returns a map containing the annotations: ion type -> charge -> Ion match
     * 
     * @param peptide           The theoretic peptide to match
     * @param spectrum          The spectrum
     * @param massTolerance     The mass tolerance to use (in Dalton)
     * @param intensityLimit    The minimal intensity to search for
     * @return a map containing the annotations
     */
    public HashMap<String, HashMap<Integer, IonMatch>> annotateSpectrum(Peptide peptide, MSnSpectrum spectrum, double massTolerance, double intensityLimit) {
        setPeptide(peptide);
        setSpectrum(spectrum, intensityLimit);
        HashMap<String, HashMap<Integer, IonMatch>> results = new HashMap<String, HashMap<Integer, IonMatch>>();

        int inspectedIon, inspectedCharge = spectrum.getPrecursor().getCharge().value;
        double fragmentMass, currentMass;

        // iterate the possible charge states
        while (inspectedCharge > 0) {

            // iterate the fragment ions for the current charge state
            for (PeptideFragmentIon fragmentIon : fragmentIons) {

                inspectedIon = fragmentIon.getType();
                fragmentMass = fragmentIon.theoreticMass + inspectedCharge * Atom.H.mass;

                if (fragmentMass >= inspectedCharge * mz.get(0) - massTolerance
                        && fragmentMass <= inspectedCharge * mz.get(mz.size() - 1) + massTolerance) {

                    int indexMin = 0;
                    int indexMax = mz.size() - 1;
                    int index;
                    Peak currentPeak;

                    currentMass = inspectedCharge * mz.get(indexMax);

                    if (Math.abs(currentMass - fragmentMass) <= massTolerance) {
                        currentPeak = peakMap.get(mz.get(indexMax));
                        if (!results.containsKey(inspectedIon + "_" + fragmentIon.getNumber())
                                || !results.get(inspectedIon + "_" + fragmentIon.getNumber()).containsKey(inspectedCharge)
                                || results.get(inspectedIon + "_" + fragmentIon.getNumber()).get(inspectedCharge).peak.intensity < currentPeak.intensity) {
                            if (!results.containsKey(inspectedIon + "_" + fragmentIon.getNumber())) {
                                results.put(inspectedIon + "_" + fragmentIon.getNumber(), new HashMap<Integer, IonMatch>());
                            }
                            results.get(inspectedIon + "_" + fragmentIon.getNumber()).put(inspectedCharge, new IonMatch(currentPeak, fragmentIon, new Charge(Charge.PLUS, inspectedCharge)));
                        }
                    }

                    currentMass = inspectedCharge * mz.get(indexMin);

                    if (Math.abs(currentMass - fragmentMass) <= massTolerance) {
                        currentPeak = peakMap.get(mz.get(indexMin));
                        if (!results.containsKey(inspectedIon + "_" + fragmentIon.getNumber())
                                || !results.get(inspectedIon + "_" + fragmentIon.getNumber()).containsKey(inspectedCharge)
                                || results.get(inspectedIon + "_" + fragmentIon.getNumber()).get(inspectedCharge).peak.intensity < currentPeak.intensity) {
                            if (!results.containsKey(inspectedIon + "_" + fragmentIon.getNumber())) {
                                results.put(inspectedIon + "_" + fragmentIon.getNumber(), new HashMap<Integer, IonMatch>());
                            }
                            results.get(inspectedIon + "_" + fragmentIon.getNumber()).put(inspectedCharge, new IonMatch(currentPeak, fragmentIon, new Charge(Charge.PLUS, inspectedCharge)));
                        }
                    }

                    while (indexMax - indexMin > 1) {
                        index = (indexMax - indexMin) / 2 + indexMin;
                        currentMass = inspectedCharge * mz.get(index);
                        if (Math.abs(currentMass - fragmentMass) <= massTolerance/inspectedCharge) {
                            currentPeak = peakMap.get(mz.get(index));
                            if (!results.containsKey(inspectedIon + "_" + fragmentIon.getNumber())
                                    || !results.get(inspectedIon + "_" + fragmentIon.getNumber()).containsKey(inspectedCharge)
                                    || results.get(inspectedIon + "_" + fragmentIon.getNumber()).get(inspectedCharge).peak.intensity < currentPeak.intensity) {
                                if (!results.containsKey(inspectedIon + "_" + fragmentIon.getNumber())) {
                                    results.put(inspectedIon + "_" + fragmentIon.getNumber(), new HashMap<Integer, IonMatch>());
                                }
                                results.get(inspectedIon + "_" + fragmentIon.getNumber()).put(inspectedCharge, new IonMatch(currentPeak, fragmentIon, new Charge(Charge.PLUS, inspectedCharge)));
                            }
                        }
                        
                        if (currentMass < fragmentMass) {
                            indexMin = index;
                        } else {
                            indexMax = index;
                        }
                    }
                }
            }

            inspectedCharge--;
        }
        
        return results;
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
        }
    }

    /**
     * Sets a new peptide to match
     * @param peptide   the new peptide
     */
    private void setPeptide(Peptide peptide) {
        if (this.peptide == null || !this.peptide.isSameAs(peptide)) {
            this.peptide = peptide;
            fragmentIons = fragmentFactory.getFragmentIons(peptide);
        }
    }
}
