package com.compomics.util.experiment.identification.protein_inference.fm_index;

import java.io.Serializable;

/**
 * Rank as used in the FM index.
 *
 * @author Dominik Kopczynski
 */
public class Rank implements Serializable {

    private static final long serialVersionUID = 832090205423902L;
    /**
     * Empty default constructor.
     */
    public Rank() {
        length = 0;
        bitfield = null;
        sums = null;
        sumsSecondLevel = null;
    }

    /**
     * The length.
     */
    public final int length;
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
    private static final int BIT_SHIFT = 6;
    /**
     * The mask.
     */
    private static final int BIT_MASK = 63;

    /**
     * Constructor.
     *
     * @param text the text
     * @param aAlphabet the alphabet
     */
    public Rank(byte[] text, long[] aAlphabet) {
        length = text.length;

        int field_len = (length >>> 6) + 1;
        bitfield = new long[field_len];
        sums = new int[(length >>> 8) + 1];
        sums[0] = 0;
        sumsSecondLevel = new byte[field_len];
        sumsSecondLevel[0] = 0;

        for (int i = 0; i < length; ++i) {
            int cell = i >>> BIT_SHIFT;
            int pos = i & BIT_MASK;
            if (pos == 0) {
                bitfield[cell] = 0;
            }
            long bit = (aAlphabet[text[i] >>> BIT_SHIFT] >>> (text[i] & BIT_MASK)) & 1L;
            bitfield[cell] |= (bit << pos);

            if (pos == 0 && i != 0) {
                if ((i & 255) == 0) {
                    sumsSecondLevel[cell] = 0;
                } else {
                    sumsSecondLevel[cell] = (byte) (sumsSecondLevel[cell - 1] + (byte) (Long.bitCount(bitfield[cell - 1])));
                }
            }
            if (((i & 255) == 0) && i != 0) {
                sums[i >>> 8] = sums[(i >>> 8) - 1] + (sumsSecondLevel[cell - 1] & 0xFF) + Long.bitCount(bitfield[cell - 1]);
            }
        }
    }
    

    /**
     * Constructor.
     *
     * @param originalBitfield an original bitfield
     * @param length length of original bitfield
     */
    public Rank(long[] originalBitfield, int length) {
        this.length = length;

        int field_len = (length >>> 6) + 1;
        bitfield = originalBitfield;
        sums = new int[(length >>> 8) + 1];
        sums[0] = 0;
        sumsSecondLevel = new byte[field_len];
        sumsSecondLevel[0] = 0;

        for (int i = 0; i < length; ++i) {
            int cell = i >>> BIT_SHIFT;
            int pos = i & BIT_MASK;
            if (pos == 0 && i != 0) {
                if ((i & 255) == 0) {
                    sumsSecondLevel[cell] = 0;
                } else {
                    sumsSecondLevel[cell] = (byte) (sumsSecondLevel[cell - 1] + (byte) (Long.bitCount(bitfield[cell - 1])));
                }
            }
            if (((i & 255) == 0) && i != 0) {
                sums[i >>> 8] = sums[(i >>> 8) - 1] + (sumsSecondLevel[cell - 1] & 0xFF) + Long.bitCount(bitfield[cell - 1]);
            }
        }
    }

    /**
     * Returns the rank.
     *
     * @param index the value
     * @param zeros the zeros
     * @return the rank
     */
    public int getRank(int index, boolean zeros) {
        if (index < 0) return 0;
        int cell = index >>> BIT_SHIFT;
        int pos = index & BIT_MASK;
        long active_ones = bitfield[cell] << (BIT_MASK - pos);
        int count_ones = (sumsSecondLevel[cell] & 0xFF) + sums[index >>> 8] + Long.bitCount(active_ones);
        return zeros ? index + 1 - count_ones : count_ones;
    }

    /**
     * Returns the rank of ones.
     *
     * @param index the value
     * @return the rank
     */
    public final int getRankOne(int index) {
        if (index < 0) return 0;
        final int cell = index >>> BIT_SHIFT;
        final int pos = index & BIT_MASK;
        final long active_ones = bitfield[cell] << (BIT_MASK - pos);
        final int count_ones = (sumsSecondLevel[cell] & 0xFF) + sums[index >>> 8] + Long.bitCount(active_ones);
        return count_ones;
    }

    /**
     * Returns the rank of zeros.
     *
     * @param index the value
     * @return the rank
     */
    public int getRankZero(int index) {
        if (index < 0) return 0;
        int cell = index >>> BIT_SHIFT;
        int pos = index & BIT_MASK;
        long active_ones = bitfield[cell] << (BIT_MASK - pos);
        int count_ones = (sumsSecondLevel[cell] & 0xFF) + sums[index >>> 8] + Long.bitCount(active_ones);
        return index + 1 - count_ones;
    }

    /**
     * Returns true if the value is equal to one.
     *
     * @param index the value
     * @return true if the value is equal to one
     */
    public boolean isOne(int index) {
        if (index < 0 || length <= index) return false;
        int cell = index >>> BIT_SHIFT;
        int pos = index & BIT_MASK;
        return (((bitfield[cell] >>> pos) & 1L) == 1);
    }
    
    
    
    /**
     * Returns the position of the i-th one
     * @param index of the i-th one
     * @return position of the i-th one
     */
    public int getSelect(int index){
        // implementation is currently solved in O(log(length))
        // can be optimized to O(1)
        
        if (index < 1 || getRankOne(length - 1) < index) return -1;
        int l = 0, r = length - 1;

        while (l <= r){
            int m = (l + r) >> 1;
            if (getRankOne(m) < index) l = m + 1;
            else r = m - 1;
        }

        if (0 < l && getRankOne(l - 1) == index) return l - 1;
        else if (getRankOne(l) == index) return l;
        else if (l < length - 1 && getRankOne(l + 1) == index) return l + 1;
        
        return -1;
    }
    

    /**
     * Returns true if the value is equal to one.
     *
     * @param index the value
     * @return the bit
     */
    public int isOneInt(int index) {
        int cell = index >>> BIT_SHIFT;
        int pos = index & BIT_MASK;
        return (int) ((bitfield[cell] >>> pos) & 1L);
    }

    /**
     * Returns the number of bytes for the allocated arrays.
     *
     * @return the number of bytes for the allocated arrays
     */
    public int getAllocatedBytes() {
        return (bitfield.length << 3) + (sums.length << 2) + sumsSecondLevel.length;
    }
}
