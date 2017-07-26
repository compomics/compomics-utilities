package com.compomics.util.experiment.identification.spectrum_annotation;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.TagFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import java.util.HashMap;

/**
 * Cache for the keys of the ions. Warning: the cache is not thread safe,
 * separate caches should be used for different threads. Using a single cache
 * results in locks or concurrent modifications.
 *
 * @author Marc Vaudel
 */
public class IonMatchKeysCache {

    /**
     * Cache for the ion type keys.
     */
    private final HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<String, HashMap<Integer, String>>>>> ionKeysCache = new HashMap<>(8);

    /**
     * Constructor.
     */
    public IonMatchKeysCache() {

    }

    /**
     * Returns the key for the ion match uniquely representing a peak
     * annotation.
     *
     * @param ion the ion matched
     * @param charge the charge
     *
     * @return the key for the ion match
     */
    public String getMatchKey(Ion ion, int charge) {
        Ion.IonType ionType = ion.getType();
        int ionTypeIndex = ionType.index;
        int ionSubType = ion.getSubType();
        int fragmentIonNumber;
        if (ionType == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
            PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);
            fragmentIonNumber = fragmentIon.getNumber();
        } else if (ionType == Ion.IonType.TAG_FRAGMENT_ION) {
            TagFragmentIon tagFragmentIon = ((TagFragmentIon) ion);
            fragmentIonNumber = tagFragmentIon.getNumber();
        } else {
            fragmentIonNumber = 0;
        }
        String neutralLossesAsString = ion.getNeutralLossesAsString();

        HashMap<Integer, HashMap<Integer, HashMap<String, HashMap<Integer, String>>>> ionTypeMap = ionKeysCache.get(ionTypeIndex);
        if (ionTypeMap == null) {
            ionTypeMap = new HashMap<>(8);
            ionKeysCache.put(ionTypeIndex, ionTypeMap);
        }
        HashMap<Integer, HashMap<String, HashMap<Integer, String>>> ionSubTypeMap = ionTypeMap.get(ionSubType);
        if (ionSubTypeMap == null) {
            ionSubTypeMap = new HashMap<>(2);
            ionTypeMap.put(ionSubType, ionSubTypeMap);
        }
        HashMap<String, HashMap<Integer, String>> ionNumberMap = ionSubTypeMap.get(fragmentIonNumber);
        if (ionNumberMap == null) {
            ionNumberMap = new HashMap<>(8);
            ionSubTypeMap.put(fragmentIonNumber, ionNumberMap);
        }
        HashMap<Integer, String> ionNeutralLossesMap = ionNumberMap.get(neutralLossesAsString);
        if (ionNeutralLossesMap == null) {
            ionNeutralLossesMap = new HashMap<>(4);
            ionNumberMap.put(neutralLossesAsString, ionNeutralLossesMap);
        }
        String key = ionNeutralLossesMap.get(charge);
        if (key == null) {
            key = IonMatch.getMatchKey(ionTypeIndex, ionSubType, fragmentIonNumber, neutralLossesAsString, charge);
            ionNeutralLossesMap.put(charge, key);
        }
        return key;
    }
}
