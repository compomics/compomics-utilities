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
    public int length;
    /**
     * The bit field.
     */
    public final long[] bitfield;
    /**
     * The sums.
     */
    private final int[] sums;
    /**
     * Binary: 0101.
     */
    private final long m1 = 0x5555555555555555L;
    /**
     * Binary: 00110011.
     */
    private final long m2 = 0x3333333333333333L;
    /**
     * Binary: 4 zeros, 4 ones.
     */
    private final long m4 = 0x0f0f0f0f0f0f0f0fL;
    /**
     * Binary: 8 zeros, 8 ones.
     */
    private final long m8 = 0x00ff00ff00ff00ffL;
    /**
     * Binary: 16 zeros, 16 ones.
     */
    private final long m16 = 0x0000ffff0000ffffL;
    /**
     * Binary: 32 zeros, 32 ones.
     */
    private final long m32 = 0x00000000ffffffffL;
    /**
     * Binary: all ones.
     */
    private final long hff = 0xffffffffffffffffL;
    /**
     * The sum of 256 to the power of 0,1,2,3.
     */
    private final long h01 = 0x0101010101010101L;
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
        long[] alphabet = new long[2];
        alphabet[0] = aAlphabet[0];
        alphabet[1] = aAlphabet[1];
        length = text.length;
        int half = ((popcount(alphabet[0]) + popcount(alphabet[1])) - 1) >> 1;
        int cnt = 0;

        for (int i = 0; i < 128 && cnt <= half; ++i) {
            int cell = i >> 6;
            int pos = i & 63;
            cnt += (alphabet[cell] >> pos) & 1L;
            alphabet[cell] &= ~(1L << pos);
        }

        int field_len = (length >> 6) + 1;
        bitfield = new long[(int) field_len];
        sums = new int[(int) field_len];
        sums[0] = 0;

        for (int i = 0; i < length; ++i) {
            int cell = i >> 6;
            int pos = i & 63;
            if (pos == 0) {
                bitfield[cell] = 0;
            }
            long bit = (alphabet[(int) (text[(int) i] >> 6L)] >> (text[(int) i] & 63L)) & 1L;
            bitfield[cell] |= (bit << pos);

            if (pos == 0 && i != 0) {
                sums[cell] = sums[cell - 1] + popcount(bitfield[cell - 1]);
            }
        }
    }
    
    /**
     * Constructor when wavelet tree uses huffman coding
     * @param text the text
     * @param ones which letters are encoded by one
     * @param huffman is the wavelet tree huffman coded
     */
    public Rank(byte[] text, long[] ones, boolean huffman) {
        length = text.length;

        int field_len = (length >> 6) + 1;
        bitfield = new long[field_len];
        sums = new int[field_len];
        sums[0] = 0;

        for (int i = 0; i < length; ++i) {
            int cell = i >> shift;
            int pos = i & mask;
            if (pos == 0) {
                bitfield[cell] = 0;
            }
            long bit = (ones[text[i] >> shift] >> (text[i] & mask)) & 1L;
            bitfield[cell] |= (bit << pos);

            if (pos == 0 && i != 0) {
                sums[cell] = sums[cell - 1] + popcount(bitfield[cell - 1]);
            }
        }
    }

    /**
     * Counts the number of 1bits in a 64bit bitfield.
     *
     * @param x the value
     * @return the result
     */
    public int popcount(long x) {
        x -= (x >> 1) & m1;               // put count of each 2 bits into those 2 bits
        x = (x & m2) + ((x >> 2) & m2);   // put count of each 4 bits into those 4 bits 
        x = (x + (x >> 4)) & m4;          // put count of each 8 bits into those 8 bits 
        return (int) ((x * h01) >> 56);   // returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ... 
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
        if (index < length) {
            int cell = index >> shift;
            int pos = index & mask;
            int masked = mask - pos;
            long active_ones = bitfield[cell] << masked;
            int count_ones = sums[cell];
            index += 1;
            count_ones += popcount(active_ones);
            return zeros ? index - count_ones : count_ones;
        }
        throw new ArrayIndexOutOfBoundsException();
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
            if (((bitfield[cell] >> pos) & 1L) == 1) {
                return true;
            }
            return false;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /*
    public int getSelect(int index, boolean zeros) {
        int middle = 0;
        int left = 0;
        int right = length - 1;
        

        while (right - left > 1) {
            middle = (right + left) >> 1;
            int rank = getRank(middle, zeros);
            if (rank >= index) right = middle;
            else left = middle;
        }
        if (getRank(left, zeros) == index) return left;
        return right;
    }
    */
    
    /**
     * Returns the number of bytes for the allocated arrays.
     *
     * @return  the number of bytes for the allocated arrays
     */
    public int getAllocatedBytes(){
        return (bitfield.length << 3) + (sums.length << 2);
    }
    
    /**
     * Returns a list of character and new left / right index for a given range recursively.
     *
     * @param leftIndex left index boundary
     * @param rightIndex right index boundary
     * @param zeros count zeros
     * @param character which character to count
     * @param setCharacter set of counted characters
     */
    public void rangeQuery(int leftIndex, int rightIndex, boolean zeros, byte character, ArrayList<Integer[]> setCharacter){
        int newLeftIndex = getRank(leftIndex, zeros);
        int newRightIndex = getRank(rightIndex, zeros);

        if (newRightIndex - newLeftIndex > 0) setCharacter.add(new Integer[]{(int)character, newLeftIndex, newRightIndex, (int)character});
    }
}
