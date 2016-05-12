package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Wavelet tree.
 *
 * @author Dominik Kopczynski
 */
public class WaveletTree {

    /**
     * Instance of a rank
     */
    private Rank rank;
    
    /**
     * Stored alphabet in a 128 bitfield
     */
    private long[] alphabet = new long[2];
    
    /**
     * Stored alphabet in a 128 bitfield
     */
    private long[] alphabetDirections = new long[2];  // 1 equals left child
    
    /**
     * Number of characters in alphabet list
     */
    private int lenAlphabet;
    
    /**
     * Text length.
     */
    private int lenText;
    
    
    /**
     * WaveletTree huffman coded.
     */
    private boolean huffmanCoded;    
    
    /**
     * Characters in Alphabet stored as list.
     */
    private byte[] charAlphabetField;
    
    /**
     * Half number of the alphabet length.
     */
    private int halfAlphabet;
    
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
        createWaveletTree(text, aAlphabet, waitingHandler);
    }
        
    public void createWaveletTree(byte[] text, long[] aAlphabet, WaitingHandler waitingHandler) {
        long[] alphabet_left = new long[2];
        long[] alphabet_right = new long[2];
        huffmanCoded = false;

        alphabet_right[0] = alphabet[0] = aAlphabet[0];
        alphabet_right[1] = alphabet[1] = aAlphabet[1];
        alphabet_left[0] = alphabet_left[1] = 0;
        lenText = text.length;
        rank = new Rank(text, alphabet);
        leftChild = null;
        rightChild = null;

        lenAlphabet = rank.popcount(alphabet[0]) + rank.popcount(alphabet[1]);
        charAlphabetField = new byte[lenAlphabet];
        int c = 0;
        for (int i = 0; i < 128; ++i) {
            if (((alphabet[i >> 6] >> (i & 63L)) & 1L) == 1) {
                charAlphabetField[c++] = (byte) i;
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
    
    
    public class HuffmanNode implements Comparable<HuffmanNode> {
        long[] alphabet = new long[]{0, 0};
        int counts = 0;
        HuffmanNode leftChild = null;
        HuffmanNode rightChild = null;
        ArrayList<Byte> charAlphabet = new ArrayList<Byte>();
        
        public HuffmanNode(int counts, int character){
            this.counts = counts;
            alphabet[character >> shift] |= 1L << (character & mask);
            charAlphabet.add((byte)character);
        }
        
        public HuffmanNode(HuffmanNode first, HuffmanNode second){
            this.counts = first.counts + second.counts;
            alphabet[0] |= first.alphabet[0];
            alphabet[0] |= second.alphabet[0];
            alphabet[1] |= first.alphabet[1];
            alphabet[1] |= second.alphabet[1];
            leftChild = first;
            rightChild = second;
            charAlphabet.addAll(first.charAlphabet);
            charAlphabet.addAll(second.charAlphabet);
        }
        
        @Override
        public int compareTo( HuffmanNode argument ) {
            if( counts < argument.counts ) return -1;
            if( counts > argument.counts ) return 1;
            return 0;
        }
    }
    
    
    
    
    public WaveletTree(byte[] text, long[] aAlphabet, WaitingHandler waitingHandler, boolean huffman) {
        this.huffmanCoded = huffman;
        if (!huffman){
            createWaveletTree(text, aAlphabet, waitingHandler);
        }
        else {
            int[] counts = new int[128];
            for (byte c : text){
                ++counts[c];
            }        

            ArrayList<HuffmanNode> huffmanNodes = new ArrayList<HuffmanNode>();
            for (int i = 0; i < 128; ++i){
                if (((aAlphabet[i >> shift] >> (i & mask)) & 1L) == 1) {
                    huffmanNodes.add(new HuffmanNode(counts[i], i));
                }
            }

            while (huffmanNodes.size() > 1){
                Collections.sort(huffmanNodes);
                HuffmanNode first = huffmanNodes.remove(0);
                HuffmanNode second = huffmanNodes.remove(0);
                huffmanNodes.add(new HuffmanNode(first, second));
            }

            createWaveletTreeHuffman(text, waitingHandler, huffmanNodes.get(0));
        }
    }
        
    public WaveletTree(byte[] text, WaitingHandler waitingHandler, HuffmanNode root) {
        createWaveletTreeHuffman(text, waitingHandler, root);        
    }
    
    public void createWaveletTreeHuffman(byte[] text, WaitingHandler waitingHandler, HuffmanNode root) {
        huffmanCoded = true;
        alphabet[0] = root.alphabet[0];
        alphabet[1] = root.alphabet[1];
    
        long[] alphabet_left = new long[2];
        long[] alphabet_right = new long[2];

        alphabetDirections[0] = alphabet_left[0] = root.leftChild.alphabet[0];
        alphabetDirections[1] = alphabet_left[1] = root.leftChild.alphabet[1];
        alphabet_right[0] = root.rightChild.alphabet[0];
        alphabet_right[1] = root.rightChild.alphabet[1];
        
        
        lenText = text.length;
        rank = new Rank(text, alphabet_right, true);
        leftChild = null;
        rightChild = null;

        lenAlphabet = rank.popcount(alphabet[0]) + rank.popcount(alphabet[1]);
        charAlphabetField = new byte[lenAlphabet];
        
        
        for (int i = 0; i < root.charAlphabet.size(); ++i) charAlphabetField[i] = root.charAlphabet.get(i);
                
        int len_alphabet_left = rank.popcount(alphabet_left[0]) + rank.popcount(alphabet_left[1]);
        int len_alphabet_right = rank.popcount(alphabet_right[0]) + rank.popcount(alphabet_right[1]);

        if (len_alphabet_left > 1) {
            int len_text_left = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >> shift;
                int pos = text[i] & mask;
                len_text_left += (int) ((alphabet_left[cell] >> pos) & 1L);
            }
            if (len_text_left > 0){
                byte[] text_left = new byte[len_text_left];
                int j = 0;
                for (int i = 0; i < text.length; ++i) {
                    int cell = text[i] >> shift;
                    int pos = text[i] & mask;
                    long bit = (alphabet_left[cell] >> pos) & 1L;
                    if (bit > 0) text_left[j++] = text[i];
                }
                leftChild = new WaveletTree(text_left, waitingHandler, root.leftChild);
            }
        }
        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return;
        }

        if (len_alphabet_right > 1) {
            int len_text_right = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >> shift;
                int pos = text[i] & mask;
                len_text_right += (int) ((alphabet_right[cell] >> pos) & 1L);
            }
            if (len_text_right > 0){
                byte[] text_right = new byte[len_text_right];
                int j = 0;
                for (int i = 0; i < text.length; ++i) {
                    int cell = text[i] >> shift;
                    int pos = text[i] & mask;
                    long bit = (alphabet_right[cell] >> pos) & 1L;
                    if (bit > 0) text_right[j++] = text[i];
                }
                rightChild = new WaveletTree(text_right, waitingHandler, root.rightChild);
            }
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
            if (((alphabet[i >> shift] >> (i & mask)) & 1L) != 0) {
//System.out.println("less: " + (char)i + " " + cumulativeSum);
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
            boolean left = false;
            if (((alphabet[cell] >> pos) & 1L) == 0) return 0;
            
            if (huffmanCoded) {
                left = ((alphabetDirections[cell] >> pos) & 1) == 1;
            }
            else {
                int masked = mask - pos;
                long active_ones = alphabet[cell] << masked;
                int positionCharacter = rank.popcount(active_ones);
                positionCharacter += cell * rank.popcount(alphabet[0]);
                positionCharacter -= 1;
                left = (positionCharacter <= halfAlphabet);
            }

            int result = rank.getRank(index, left);
            if (result == 0) return result;

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
    public byte getCharacter(int index) {
        if (0 <= index && index < lenText) {
            boolean left = !rank.isOne(index);
            int result = rank.getRank(index, left);
            if (result == 0) {
                return charAlphabetField[result];
            }

            byte character;
            result -= 1;
            if (left) {
                if (leftChild == null) {
                    character = charAlphabetField[0];
                } else {
                    character = leftChild.getCharacter(result);
                }
            } else if (rightChild == null) {
                character = charAlphabetField[lenAlphabet - 1];
            } else {
                character = rightChild.getCharacter(result);
            }
            return character;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    
    /*
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
    */
    
    /**
     * Returns the number of bytes for the allocated arrays.
     * @return 
     */
    public int getAllocatedBytes(){
        int bytes = rank.getAllocatedBytes();
        if (leftChild != null) bytes += leftChild.getAllocatedBytes();
        if (rightChild != null) bytes += rightChild.getAllocatedBytes();
        return bytes;
    }
    
    /**
     * Retruns a list of character and new left / right index for a given range.
     * @param leftIndex
     * @param rightIndex
     * @return 
     */
    public ArrayList<Integer[]> rangeQuery(int leftIndex, int rightIndex){
        ArrayList<Integer[]> setCharacter = new ArrayList<Integer[]>(26);
        rangeQuery(leftIndex, rightIndex, setCharacter);
        return setCharacter;
    }
    
    /**
     * Retruns a list of character and new left / right index for a given range recursively.
     * @param leftIndex
     * @param rightIndex
     * @param setCharacter 
     */
    public void rangeQuery(int leftIndex, int rightIndex, ArrayList<Integer[]> setCharacter){
        if (rightIndex == leftIndex) return;
        
        int newLeftIndex = rank.getRank(leftIndex, true);
        int newRightIndex = rank.getRank(rightIndex, true);
        
        if (leftChild != null){
            leftChild.rangeQuery(newLeftIndex - 1, newRightIndex - 1, setCharacter);
        }
        else {
            rank.rangeQuery(leftIndex, rightIndex, true, charAlphabetField[0], setCharacter);
        }
        
        if (rightChild != null){
            rightChild.rangeQuery(leftIndex - newLeftIndex, rightIndex - newRightIndex, setCharacter);
        }
        else {
            rank.rangeQuery(leftIndex, rightIndex, false, charAlphabetField[lenAlphabet - 1], setCharacter);
        }
    }
    
}
