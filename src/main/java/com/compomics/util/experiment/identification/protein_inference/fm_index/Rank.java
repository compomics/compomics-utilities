package com.compomics.util.experiment.identification.protein_inference.fm_index;

import java.util.ArrayList;

/**
 * Rank as used in the FM index.
 *
 * @author Dominik Kopczynski
 */
public class Rank {

    /**
     * The length.
     */
    private int length;
    /**
     * The bit field.
     */
    private final long[] bitfield;
    /**
     * The sums.
     */
    private final int[] sums;
    /**
     * The sums.
     */
    private final byte[] sumsSecondLevel;
    
    /**
     * The shift.
     */
    private final int shift = 6;
    /**
     * The mask.
     */
    private final int mask = 63;

    /**
     * Constructor.
     *
     * @param text the text
     * @param aAlphabet the alphabet
     */
    public Rank(byte[] text, long[] aAlphabet) {
        length = text.length;

        int field_len = (length >> 6) + 1;
        bitfield = new long[field_len];
        sums = new int[(length >> 8) + 1];
        sums[0] = 0;
        sumsSecondLevel = new byte[field_len];
        sumsSecondLevel[0] = 0;

        for (int i = 0; i < length; ++i) {
            int cell = i >> shift;
            int pos = i & mask;
            if (pos == 0) {
                bitfield[cell] = 0;
            }
            long bit = (aAlphabet[text[i] >> shift] >> (text[i] & mask)) & 1L;
            bitfield[cell] |= (bit << pos);

            if (pos == 0 && i != 0) {
                if ((i & 255) == 0) sumsSecondLevel[cell] = 0;
                else{
                    sumsSecondLevel[cell] = (byte)(sumsSecondLevel[cell - 1] + (byte)(Long.bitCount(bitfield[cell - 1])));
                }
            }
            if (((i & 255) == 0) && i != 0){
                sums[i >> 8] = sums[(i >> 8) - 1] + (sumsSecondLevel[cell - 1] & 0xFF) + Long.bitCount(bitfield[cell - 1]);
            }
        }
    }


    /**
     * Returns the rank.
     *
     * @param index the value
     * @return the rank
     */
    public int getRank(int index, boolean zeros) {
        int cell = index >> shift;
        int pos = index & mask;
        long active_ones = bitfield[cell] << (mask - pos);
        int count_ones = (sumsSecondLevel[cell] & 0xFF) + sums[index >> 8] + Long.bitCount(active_ones);
        return zeros ? index + 1 - count_ones : count_ones;
    }

    /**
     * Returns true if the value is equal to one.
     *
     * @param index the value
     * @return true if the value is equal to one
     */
    public boolean isOne(int index) {
        if (0 <= index && index < length) {
            int cell = index >> shift;
            int pos = index & mask;
            return (((bitfield[cell] >> pos) & 1L) == 1);
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    
    /**
     * Returns the number of bytes for the allocated arrays.
     * @return 
     */
    public int getAllocatedBytes(){
        return (bitfield.length << 3) + (sums.length << 2) + sumsSecondLevel.length;
    }
}
