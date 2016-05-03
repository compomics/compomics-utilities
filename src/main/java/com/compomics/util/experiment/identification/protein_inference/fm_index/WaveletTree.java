package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.waiting.WaitingHandler;

/**
 * Wavelet tree.
 *
 * @author Dominik Kopczynski
 */
public class WaveletTree {

    /**
     * Instance of a rank
     */
    private final Rank rank;
    
    /**
     * Stored alphabet in a 128 bitfield
     */
    private final long[] alphabet;
    
    /**
     * Number of characters in alphabet list
     */
    private final int lenAlphabet;
    
    /**
     * Text length.
     */
    private final int lenText;
    
    /**
     * Characters in Alphabet stored as list.
     */
    private final char[] charAlphabetField;
    
    /**
     * Half number of the alphabet length.
     */
    private final int halfAlphabet;
    
    /**
     * Left child of the wavelet tree.
     */
    private WaveletTree leftChild;
    
    /**
     * Right child of the wavelet tree.
     */
    private WaveletTree rightChild;
    
    /**
     * Shift number for fast bitwise divisions.
     */
    private final int shift = 6;
    
    /**
     * Mask for fast bitwise modulo operations.
     */
    private final int mask = 63;

    /**
     * Constructor.
     *
     * @param text the text
     * @param aAlphabet the alphabet
     * @param waitingHandler the waiting Handler
     */
    public WaveletTree(byte[] text, long[] aAlphabet, WaitingHandler waitingHandler) {
        long[] alphabet_left = new long[2];
        long[] alphabet_right = new long[2];
        alphabet = new long[2];

        alphabet_right[0] = alphabet[0] = aAlphabet[0];
        alphabet_right[1] = alphabet[1] = aAlphabet[1];
        alphabet_left[0] = alphabet_left[1] = 0;
        lenText = text.length;
        rank = new Rank(text, alphabet);
        leftChild = null;
        rightChild = null;

        lenAlphabet = rank.popcount(alphabet[0]) + rank.popcount(alphabet[1]);
        charAlphabetField = new char[lenAlphabet];
        int c = 0;
        for (int i = 0; i < 128; ++i) {
            if (((alphabet[i >> 6] >> (i & 63L)) & 1L) == 1) {
                charAlphabetField[c++] = (char) i;
            }
        }

        halfAlphabet = (lenAlphabet - 1) >> 1;
        int countCharacters = 0;
        for (int i = 0; i < 128 && countCharacters <= halfAlphabet; ++i) {
            int cell = i >> 6;
            int pos = i & 63;
            long bit = (int) ((alphabet[cell] >> pos) & 1L);
            countCharacters += bit;
            alphabet_right[cell] &= ~(1L << pos);
            alphabet_left[cell] |= bit << pos;
        }

        int len_alphabet_left = rank.popcount(alphabet_left[0]) + rank.popcount(alphabet_left[1]);
        int len_alphabet_right = rank.popcount(alphabet_right[0]) + rank.popcount(alphabet_right[1]);

        if (len_alphabet_left > 1) {
            int len_text_left = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >> 6L;
                int pos = text[i] & 63;
                len_text_left += (int) ((alphabet_left[cell] >> pos) & 1L);
            }
            byte[] text_left = new byte[len_text_left + 1];
            text_left[len_text_left] = 0;
            int j = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >> 6L;
                int pos = text[i] & 63;
                long bit = (alphabet_left[cell] >> pos) & 1L;
                if (bit > 0) {
                    text_left[j] = text[i];
                    ++j;
                }
            }
            leftChild = new WaveletTree(text_left, alphabet_left, waitingHandler);
        }
        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return;
        }

        if (len_alphabet_right > 1) {
            int len_text_right = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >> 6;
                int pos = text[i] & 63;
                len_text_right += (int) ((alphabet_right[cell] >> pos) & 1L);
            }
            byte[] text_right = new byte[len_text_right + 1];
            text_right[len_text_right] = 0;
            int j = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >> 6;
                int pos = text[i] & 63;
                long bit = (alphabet_right[cell] >> pos) & 1L;
                if (bit > 0) {
                    text_right[j] = text[i];
                    ++j;
                }
            }
            rightChild = new WaveletTree(text_right, alphabet_right, waitingHandler);
        }
    }

    /**
     * Create the less table.
     *
     * @return the less table
     */
    public int[] createLessTable() {
        int[] less = new int[128];
        int cumulativeSum = 0;
        for (int i = 0; i < 128; ++i) {
            less[i] = cumulativeSum;
            if (((alphabet[i >> 6] >> (i & 63)) & 1) != 0) {
                cumulativeSum += getRank(rank.length - 1, i);
            }
        }
        return less;
    }

    /**
     * Returns the number of occurrences of a given character until position index.
     *
     * @param index the index
     * @param character the character
     * @return the rank
     */
    public int getRank(int index, int character) {
        if (index < 0) return 0;
        if (index < lenText) {
            int cell = character >> shift;
            int pos = character & mask;
            int masked = mask - pos;
            long active_ones = alphabet[cell] << masked;
            int positionCharacter = rank.popcount(active_ones);
            positionCharacter += cell * rank.popcount(alphabet[0]);
            positionCharacter -= 1;
            boolean left = (positionCharacter <= halfAlphabet);
            int result = rank.getRank(index, left);
            if (result == 0) {
                return result;
            }

            if (left && leftChild != null) {
                return leftChild.getRank(result - 1, character);
            } else if (!left && rightChild != null) {
                return rightChild.getRank(result - 1, character);
            }
            return result;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Returns the character at a given index.
     *
     * @param index the index
     * @return the character
     */
    public char getCharacter(int index) {
        if (0 <= index && index < lenText) {
            boolean left = !rank.isOne(index);
            int result = rank.getRank(index, left);
            if (result == 0) {
                return charAlphabetField[result];
            }

            char character;
            result -= 1;
            if (left) {
                if (leftChild == null) {
                    character = charAlphabetField[0];
                } else {
                    character = leftChild.getCharacter(result);
                }
            } else if (rightChild == null) {
                character = charAlphabetField[charAlphabetField.length - 1];
            } else {
                character = rightChild.getCharacter(result);
            }
            return character;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    
    public int getSelect(int occurence, int character){
        if (occurence <= 0) return -1;
        if (occurence < lenText) {
            int cell = character >> shift;
            int pos = character & mask;
            if (((alphabet[cell] >> pos) & 1) != 1) return 0;
            int masked = mask - pos;
            long active_ones = alphabet[cell] << masked;
            int positionCharacter = rank.popcount(active_ones);
            positionCharacter += cell * rank.popcount(alphabet[0]);
            positionCharacter -= 1;
            boolean left = (positionCharacter <= halfAlphabet);
            if (left){
                if (leftChild == null){
                    return rank.getSelect(occurence, left);
                }
                else {
                    int position = leftChild.getSelect(occurence, character) + 1;
                    return rank.getSelect(position, left);
                }
            }
            else {
                if (rightChild == null){
                    return rank.getSelect(occurence, left);
                }
                else {
                    int position = rightChild.getSelect(occurence, character) + 1;
                    return rank.getSelect(position, left);
                }
            }
        }
        else throw new ArrayIndexOutOfBoundsException();
    }
    
    public int getAllocatedBytes(){
        int bytes = 0;
        if (leftChild != null) bytes += leftChild.getAllocatedBytes();
        if (rightChild != null) bytes += rightChild.getAllocatedBytes();
        return bytes + rank.getAllocatedBytes();
    }
}
