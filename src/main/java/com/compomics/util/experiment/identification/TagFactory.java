package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Convenience class for sequence tag generation.
 *
 * @author Marc Vaudel
 * @author Kenneth Verheggen
 */
public class TagFactory {

    /**
     * Returns all the amino acid combinations for a given tag length.
     *
     * @param length the length of the tag
     *
     * @return all the amino acid combinations
     */
    public static String[] getAminoAcidCombinations(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Sequence length must be a positive number.");
        }
        char[] seq = AminoAcid.getAminoAcids();
        StringBuilder builder = new StringBuilder("");
        String[] aminoAcidArray = new String[(int) Math.pow(seq.length, length)];
        int[] pos = new int[length];
        for (int i = 0; i < aminoAcidArray.length; i++) {
            builder.delete(0, length);
            for (int x = 0; x < length; x++) {
                if (pos[x] == seq.length) {
                    pos[x] = 0;
                    if (x + 1 < length) {
                        pos[x + 1]++;
                    }
                }
                builder.append(seq[pos[x]]);
            }
            pos[0]++;
            aminoAcidArray[i] = (builder.toString());
        }
        return aminoAcidArray;
    }
}
