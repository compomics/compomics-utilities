package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.AminoAcid;
import java.util.ArrayList;

/**
 * Convenience class for sequence tag generation.
 *
 * @author Marc Vaudel
 */
public class TagFactory {

    /**
     * Returns all the amino acid combinations for a given tag length.
     *
     * @param length the length of the tag
     * 
     * @return all the amino acid combinations
     */
    public static ArrayList<String> getAminoAcidCombinations(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Sequence length must be a positive number.");
        }
        if (length == 0) {
            return new ArrayList<>();
        }
        ArrayList<String> tempList, result = new ArrayList<> (AminoAcid.getAminoAcidsList());
        for (int i = 1; i < length; i++) {
            tempList = new ArrayList<>();
            for (String tag : result) {
                for (char aa : AminoAcid.getAminoAcids()) {
                    tempList.add(tag + aa);
                }
            }
            result = tempList;
        }
        return result;
    }
}
