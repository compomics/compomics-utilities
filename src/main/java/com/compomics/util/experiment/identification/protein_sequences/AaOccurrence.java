package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import org.apache.commons.math.util.FastMath;

/**
 * This class gathers statistics on the occurrence of amino acids in a fasta
 * file.
 *
 * @author Marc Vaudel
 */
public class AaOccurrence {

    /**
     * Occurrences of the amino acids without combinations indexed as in the
     * AminoAcid class.
     */
    private final int[] uniqueAaOccurrence;
    /**
     * Shares of the amino acids without combinations indexed as in the
     * AminoAcid class. -Log10 transformed.
     */
    private final double[] uniqueAaShareLog;

    /**
     * Constructor.
     *
     * @param aaOccurrence the occurrence of all amino acids in the database
     * indexed as in the AminoAcid clss.
     */
    public AaOccurrence(int[] aaOccurrence) {

        char[] uniqueAminoAcids = AminoAcid.getUniqueAminoAcids();
        int nUnique = AminoAcid.getNUnique();
        uniqueAaOccurrence = new int[nUnique];
        uniqueAaShareLog = new double[nUnique];
        int sumOccurrences = 0;

        for (int i = 0; i < nUnique; i++) {

            char aa = uniqueAminoAcids[i];
            int aaIndex = AminoAcid.getIndex(aa);
            int occurrence = aaOccurrence[aaIndex];
            uniqueAaOccurrence[i] = occurrence;
            if (occurrence == 0) {
                sumOccurrences += 1;
            } else {
                sumOccurrences += occurrence;
            }
        }

        for (int i = 0; i < nUnique; i++) {
            
            int occurrence = uniqueAaOccurrence[i];
            uniqueAaShareLog[i] = -FastMath.log10(((double) Math.max(occurrence, 1)) / sumOccurrences);
        }
    }

    /**
     * Returns the log10 of the share of the given amino acid in the database.
     *
     * @param aa the amino acid as single letter code
     *
     * @return the log10 of the share of the given amino acid in the database
     */
    public double getP(char aa) {
        int uniqueIndex = AminoAcid.getUniqueIndex(aa);
        return uniqueAaShareLog[uniqueIndex];
    }

    /**
     * Returns the sum of the log10 of the share of the given amino acids in the
     * database. Start and end indexes are 0 based, start is inclusive and end
     * exclusive.
     *
     * @param aas the amino acids as char array of the single letter code
     * @param startIndex the index where to start in the sequence
     * @param endIndex the index where to start in the sequence
     * @param pMax the maximal value p can have
     *
     * @return the sum of the log10 of the share of the given amino acids in the
     * database
     */
    public double getP(char[] aas, int startIndex, int endIndex, double pMax) {

        double p = 0.0;
        for (int i = startIndex; i < endIndex; i++) {
            char aa = aas[i];
            p += getP(aa);
            if (p > pMax) {
                return pMax;
            }
        }
        return p;
    }
}
