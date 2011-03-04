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

    private Peptide peptide;
    private ArrayList<PeptideFragmentIon> fragmentIons;
    private FragmentFactory fragmentFactory = FragmentFactory.getInstance();
    private String spectrumKey = "";
    private double intensityLimit = 0;
    private ArrayList<Double> mz;
    private HashMap<Double, Peak> peakMap;

    /**
     * Constructor
     */
    public SpectrumAnnotator() {
    }

    public ArrayList<IonMatch> matchPeak(Peptide peptide, Peak peak, double mzTolerance, Charge charge) {
        setPeptide(peptide);
        ArrayList<IonMatch> result = new ArrayList<IonMatch>();
        for (PeptideFragmentIon fragmentIon : fragmentIons) {
            if (Math.abs(fragmentIon.theoreticMass - peak.mz * charge.value) <= mzTolerance) {
                result.add(new IonMatch(peak, new PeptideFragmentIon(fragmentIon.getType(), fragmentIon.getNumber(), charge)));
            }
        }
        return result;
    }

    /**
     * Annotates a spectrum and returns a map containing the annotations: ion type -> charge -> Ion match
     * @param peptide
     * @param spectrum
     * @param mzTolerance
     * @param intensityLimit
     * @return
     */
    public HashMap<Integer, HashMap<Integer, IonMatch>> annotateSpectrum(Peptide peptide, MSnSpectrum spectrum, double mzTolerance, double intensityLimit) {
        setPeptide(peptide);
        setSpectrum(spectrum, intensityLimit);
        HashMap<Integer, HashMap<Integer, IonMatch>> results = new HashMap<Integer, HashMap<Integer, IonMatch>>();

        int inspectedIon, inspectedCharge = spectrum.getPrecursor().getCharge().value;
        double fragmentMZ;

        // iterate the possible charge states
        while (inspectedCharge > 0) {

            // iterate the fragment ions for the current charge state
            for (PeptideFragmentIon fragmentIon : fragmentIons) {

                // set the current charge for the fragment ion
                fragmentIon.setCharge(new Charge(Charge.PLUS, inspectedCharge));

                inspectedIon = fragmentIon.getType();
                fragmentMZ = (fragmentIon.theoreticMass + inspectedCharge * Atom.H.mass) / inspectedCharge;

                if (fragmentMZ >= mz.get(0) - mzTolerance
                        && fragmentMZ <= mz.get(mz.size() - 1) + mzTolerance) {

                    int indexMin = 0;
                    int indexMax = mz.size() - 1;
                    int index;
                    Peak currentPeak;
                    double currentMz;

                    currentMz = mz.get(indexMax);

                    if (Math.abs(currentMz - fragmentMZ) <= mzTolerance) {
                        currentPeak = peakMap.get(currentMz);
                        if (!results.containsKey(inspectedIon) || !results.get(inspectedIon).containsKey(inspectedCharge)
                                || results.get(inspectedIon).get(inspectedCharge).peak.intensity < currentPeak.intensity) {
                            if (!results.containsKey(inspectedIon)) {
                                results.put(inspectedIon, new HashMap<Integer, IonMatch>());
                            }
                            results.get(inspectedIon).put(inspectedCharge, new IonMatch(currentPeak, fragmentIon));
                        }
                    }

                    currentMz = mz.get(indexMin);

                    if (Math.abs(currentMz - fragmentMZ) <= mzTolerance) {
                        currentPeak = peakMap.get(currentMz);
                        if (!results.containsKey(inspectedIon) || !results.get(inspectedIon).containsKey(inspectedCharge)
                                || results.get(inspectedIon).get(inspectedCharge).peak.intensity < currentPeak.intensity) {
                            if (!results.containsKey(inspectedIon)) {
                                results.put(inspectedIon, new HashMap<Integer, IonMatch>());
                            }
                            results.get(inspectedIon).put(inspectedCharge, new IonMatch(currentPeak, fragmentIon));
                        }
                    }

                    while (indexMax - indexMin > 1) {
                        index = (indexMax - indexMin) / 2 + indexMin;
                        currentMz = mz.get(index);
                        if (Math.abs(currentMz - fragmentMZ) <= mzTolerance) {
                            currentPeak = peakMap.get(currentMz);
                            if (!results.containsKey(inspectedIon) || !results.get(inspectedIon).containsKey(inspectedCharge)
                                    || results.get(inspectedIon).get(inspectedCharge).peak.intensity < currentPeak.intensity) {
                                if (!results.containsKey(inspectedIon)) {
                                    results.put(inspectedIon, new HashMap<Integer, IonMatch>());
                                }
                                results.get(inspectedIon).put(inspectedCharge, new IonMatch(currentPeak, fragmentIon));

//                                System.out.println("added: "
//                                        + fragmentIon.getIonType()
//                                        + fragmentIon.getNumber()
//                                        + fragmentIon.getCharge().getChargeAsFormattedString()
//                                        + fragmentIon.getNeutralLoss()
//                                        + " - " + currentPeak.mz);
                            }
                        }
                        
                        if (currentMz < fragmentMZ) {
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

    private void setPeptide(Peptide peptide) {
        if (this.peptide == null || !this.peptide.isSameAs(peptide)) {
            this.peptide = peptide;
            fragmentIons = fragmentFactory.getFragmentIons(peptide);
        }
    }
}
