package com.compomics.util.experiment.massspectrometry;

import java.util.HashMap;

/**
 * This class represents a collection of spectra acquired during a proteomicAnalysis
 *
 * @author Marc
 */
public class SpectrumCollection {

    /**
     * Map of all ms1 spectra indexed by the spectrum index
     */
    private HashMap<String, MS1Spectrum> ms1Map = new HashMap<String, MS1Spectrum>();
    /**
     * Map of all msn spectra indexed by the acquisition level and the spectrum index
     */
    private HashMap<Integer, HashMap<String, MSnSpectrum>> msnMap = new HashMap<Integer, HashMap<String, MSnSpectrum>>();

    /**
     * Constructor
     */
    public SpectrumCollection() {

    }

    /**
     * Adds an ms1 spectrum to the collection
     * @param ms1Spectrum   the selected ms1 spectrum
     */
    public void addSpectrum(MS1Spectrum ms1Spectrum) {
        ms1Map.put(ms1Spectrum.getSpectrumTitle(), ms1Spectrum);
    }

    /**
     * Adds an msn spectrum to the collection
     * @param msnSpectrum the selected msn spectrum
     */
    public void addSpectrum(MSnSpectrum msnSpectrum) {
        int level = msnSpectrum.getLevel();
        if (!msnMap.containsKey(level)) {
            msnMap.put(level, new HashMap<String, MSnSpectrum>());
        }
        msnMap.get(level).put(msnSpectrum.getSpectrumTitle(), msnSpectrum);
    }

    /**
     * Getter for an ms1 spectrum
     * @param spectrumKey   the spectrum key
     * @return  the corresponding spectrum
     */
    public MS1Spectrum getMS1Spectrum(String spectrumKey) {
        return ms1Map.get(spectrumKey);
    }

    /**
     * Getter for an ms2 spectrum
     * @param level         acquisition level of the desired spectrum
     * @param spectrumKey   key of the desired spectrum
     * @return the desired spectrum
     */
    public MSnSpectrum getMSnSpectrum(int level, String spectrumKey) {
        return msnMap.get(level).get(spectrumKey);
    }

}
