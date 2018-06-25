package com.compomics.util.experiment.identification.protein_inference.fm_index;

import com.compomics.util.waiting.WaitingHandler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Wavelet tree.
 *
 * @author Dominik Kopczynski
 */
public class WaveletTree implements Serializable {

    /**
     * Instance of a rank.
     */
    private Rank rank;
    
    /**
     * Stored alphabet in a 128 bitfield.
     */
    private long[] alphabetDirections = new long[2];  // 1 equals left child
    
    /**
     * First character in alphabet.
     */
    private int firstChar;
    
    /**
     * Last character in alphabet.
     */
    private int lastChar;
    
    /**
     * Text length.
     */
    private int lenText;
    
    /**
     * Continue range query for left child.
     */
    private boolean continueLeftRangeQuery = false;
    
    /**
     * Continue range query for right child.
     */
    private boolean continueRightRangeQuery = false;
    
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
     * number of masses
     */
    private int numMasses;
    
    /**
     * left right mask
     */
    private int leftRightMask;
    
    private int[] less;
    

    /**
     * Class for huffman nodes.
     */
    public class HuffmanNode implements Comparable<HuffmanNode> {

        long[] alphabet = new long[]{0, 0};
        int counts = 0;
        int depth = 0;
        int innernodes = 0;
        HuffmanNode leftChild = null;
        HuffmanNode rightChild = null;
        ArrayList<Byte> charAlphabet = new ArrayList<>();

        public HuffmanNode(int counts, int character) {
            this.counts = counts;
            alphabet[character >>> shift] |= 1L << (character & mask);
            charAlphabet.add((byte) character);
        }

        public HuffmanNode(HuffmanNode first, HuffmanNode second) {
            this.counts = first.counts + second.counts;
            alphabet[0] |= first.alphabet[0];
            alphabet[0] |= second.alphabet[0];
            alphabet[1] |= first.alphabet[1];
            alphabet[1] |= second.alphabet[1];
            leftChild = first;
            rightChild = second;
            charAlphabet.addAll(first.charAlphabet);
            charAlphabet.addAll(second.charAlphabet);
            depth = Math.max(first.depth, second.depth) + 1;
            innernodes = first.innernodes + second.innernodes + 1;
        }

        @Override
        public int compareTo(HuffmanNode argument) {
            if (counts < argument.counts) {
                return -1;
            }
            if (counts > argument.counts) {
                return 1;
            }
            return 0;
        }
    }
    
    
    

    /**
     * Constructor.
     *
     * @param text the text
     * @param aAlphabet the alphabet
     * @param waitingHandler the waiting handler
     * @param numMasses  number of masses plus modifications
     * @param hasPTMatTerminus indicates how to handle / sign
     */
    public WaveletTree(byte[] text, long[] aAlphabet, WaitingHandler waitingHandler, int numMasses, boolean hasPTMatTerminus) {
        prepareWaveletTree(text, aAlphabet, waitingHandler, numMasses, hasPTMatTerminus);
    }

    /**
     * Constructor.
     *
     * @param text the text
     * @param aAlphabet the alphabet
     * @param waitingHandler the waiting handler
     * @param numMasses  number of masses plus modifications
     */
    public WaveletTree(byte[] text, long[] aAlphabet, WaitingHandler waitingHandler, int numMasses) {
        prepareWaveletTree(text, aAlphabet, waitingHandler, numMasses, false);
    }

    /**
     * Constructor forward function.
     *
     * @param text the text
     * @param aAlphabet the alphabet
     * @param waitingHandler the waiting handler
     * @param numMasses  number of masses plus modifications
     * @param hasPTMatTerminus indicates how to handle / sign
     */
    private void prepareWaveletTree(byte[] text, long[] aAlphabet, WaitingHandler waitingHandler, int numMasses, boolean hasPTMatTerminus) {

        int[] counts = new int[128];
        for (byte c : text) {
            ++counts[c];
        }

        ArrayList<HuffmanNode> huffmanNodes = new ArrayList<>();
        for (int i = 0; i < 128; ++i) {
            if (((aAlphabet[i >>> shift] >>> (i & mask)) & 1L) == 1) {
                huffmanNodes.add(new HuffmanNode(counts[i], i));
            }
        }

        while (huffmanNodes.size() > 1) {
            Collections.sort(huffmanNodes);
            HuffmanNode first = huffmanNodes.remove(0);
            HuffmanNode second = huffmanNodes.remove(0);
            huffmanNodes.add(new HuffmanNode(first, second));
        }
        
        createWaveletTreeHuffman(text, waitingHandler, huffmanNodes.get(0), numMasses, hasPTMatTerminus);
        
        
        less = new int[128];
        long[] alphabet = new long[2];
        alphabet[0] = huffmanNodes.get(0).alphabet[0];
        alphabet[1] = huffmanNodes.get(0).alphabet[1];
        int cumulativeSum = 0;
        for (int i = 0; i < 128; ++i) {
            less[i] = cumulativeSum;
            if (((alphabet[i >>> shift] >>> (i & mask)) & 1L) != 0) {
                cumulativeSum += getRank(lenText - 1, i);
            }
        }
    }

    /**
     * Constructor.
     *
     * @param text the text
     * @param waitingHandler the waiting handler
     * @param root the root
     * @param numMasses  number of masses plus modifications
     * @param hasPTMatTerminus if there is a PTM at the terminus
     */
    public WaveletTree(byte[] text, WaitingHandler waitingHandler, HuffmanNode root, int numMasses, boolean hasPTMatTerminus) {
        this.numMasses = numMasses;
        createWaveletTreeHuffman(text, waitingHandler, root, numMasses, hasPTMatTerminus);
    }
    
    

    /**
     * Create wavelet tree huffman.
     *
     * @param text the text
     * @param waitingHandler the waiting handler
     * @param root the root
     * @param numMasses  number of masses plus modifications
     * @param hasPTMatTerminus if there is a PTM at the terminus
     */
    public void createWaveletTreeHuffman(byte[] text, WaitingHandler waitingHandler, HuffmanNode root, int numMasses, boolean hasPTMatTerminus) {
        this.numMasses = numMasses;
        long[] alphabet = new long[2];
        alphabet[0] = root.alphabet[0];
        alphabet[1] = root.alphabet[1];

        long[] alphabetExcluded = new long[2];
        alphabetExcluded[0] = 1L << '$';
        if (!hasPTMatTerminus) alphabetExcluded[0] |= 1L << '/';
        /*
        alphabetExcluded[1] = 1L << ('B' & 63);
        alphabetExcluded[1] |= 1L << ('X' & 63);
        alphabetExcluded[1] |= 1L << ('Z' & 63);
        */

        long[] alphabet_left = new long[2];
        long[] alphabet_right = new long[2];

        alphabetDirections[0] = alphabet_left[0] = root.leftChild.alphabet[0];
        alphabetDirections[1] = alphabet_left[1] = root.leftChild.alphabet[1];
        alphabet_right[0] = root.rightChild.alphabet[0];
        alphabet_right[1] = root.rightChild.alphabet[1];

        continueLeftRangeQuery = (((alphabet_left[0] & (~alphabetExcluded[0])) + (alphabet_left[1] & (~alphabetExcluded[1]))) > 0);
        continueRightRangeQuery = (((alphabet_right[0] & (~alphabetExcluded[0])) + (alphabet_right[1] & (~alphabetExcluded[1]))) > 0);

        lenText = text.length;
        rank = new Rank(text, alphabet_right);
        leftChild = null;
        rightChild = null;

        int lenAlphabet = Long.bitCount(alphabet[0]) + Long.bitCount(alphabet[1]);
        byte[] charAlphabetField = new byte[lenAlphabet];

        for (int i = 0; i < root.charAlphabet.size(); ++i) {
            charAlphabetField[i] = root.charAlphabet.get(i);
        }
        firstChar = charAlphabetField[0];
        lastChar = charAlphabetField[lenAlphabet - 1];

        int len_alphabet_left = Long.bitCount(alphabet_left[0]) + Long.bitCount(alphabet_left[1]);
        int len_alphabet_right = Long.bitCount(alphabet_right[0]) + Long.bitCount(alphabet_right[1]);

        if (len_alphabet_left > 1) {
            int len_text_left = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >>> shift;
                int pos = text[i] & mask;
                len_text_left += (int) ((alphabet_left[cell] >>> pos) & 1L);
            }
            if (len_text_left > 0) {
                byte[] text_left = new byte[len_text_left];
                int j = 0;
                for (int i = 0; i < text.length; ++i) {
                    int cell = text[i] >>> shift;
                    int pos = text[i] & mask;
                    long bit = (alphabet_left[cell] >>> pos) & 1L;
                    if (bit > 0) {
                        text_left[j++] = text[i];
                    }
                }
                leftChild = new WaveletTree(text_left, waitingHandler, root.leftChild, numMasses, hasPTMatTerminus);
            }
        }
        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return;
        }

        if (len_alphabet_right > 1) {
            int len_text_right = 0;
            for (int i = 0; i < text.length; ++i) {
                int cell = text[i] >>> shift;
                int pos = text[i] & mask;
                len_text_right += (int) ((alphabet_right[cell] >>> pos) & 1L);
            }
            if (len_text_right > 0) {
                byte[] text_right = new byte[len_text_right];
                int j = 0;
                for (int i = 0; i < text.length; ++i) {
                    int cell = text[i] >>> shift;
                    int pos = text[i] & mask;
                    long bit = (alphabet_right[cell] >>> pos) & 1L;
                    if (bit > 0) {
                        text_right[j++] = text[i];
                    }
                }
                rightChild = new WaveletTree(text_right, waitingHandler, root.rightChild, numMasses, hasPTMatTerminus);
            }
        }
        if (leftChild != null) leftRightMask = 4;
        if (rightChild != null) leftRightMask |= 2;
    }

    /**
     * Create the less table.
     *
     * @return the less table
     */
    public int[] createLessTable() {
        return less;
    }

    /**
     * Returns the number of occurrences of a given character until position
     * index.
     *
     * @param index the index
     * @param character the character
     * @return the rank
     */
    public int getRank(int index, int character) {
        if (index < lenText) {
            return getRankRecursive(index, character);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Returns the number of occurrences of a given character until position
     * index.
     *
     * @param index the index
     * @param character the character
     * @return the rank
     */
    public int getRankRecursive(int index, int character) {
        if (index >= 0) {
            int cell = character >>> shift;
            int pos = character & mask;
            boolean left = ((alphabetDirections[cell] >>> pos) & 1) == 1;
            int result = rank.getRank(index, left);

            if (left && leftChild != null) {
                return leftChild.getRankRecursive(result - 1, character);
            } else if (!left && rightChild != null) {
                return rightChild.getRankRecursive(result - 1, character);
            }
            return result;
        }
        return 0;
    }

    /**
     * Returns the character and rank at a given index.
     *
     * @param index the index
     * @return the character and rank
     */
    public int[] getCharacterInfo(int index) {
        if (index < lenText) {
            boolean left = !rank.isOne(index);
            int result = rank.getRank(index, left);
            if (result == 0) {
                return new int[]{firstChar, 0};
            }

            result -= 1;
            if (left) {
                if (leftChild == null) {
                    return new int[]{firstChar, result};
                } else {
                    return leftChild.getCharacterInfo(result);
                }
            } else if (rightChild == null) {
                return new int[]{lastChar, result};
            } else {
                return rightChild.getCharacterInfo(result);
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Returns the number of bytes for the allocated arrays.
     *
     * @return number of allocated bytes
     */
    public int getAllocatedBytes() {
        int bytes = rank.getAllocatedBytes();
        if (leftChild != null) {
            bytes += leftChild.getAllocatedBytes();
        }
        if (rightChild != null) {
            bytes += rightChild.getAllocatedBytes();
        }
        return bytes;
    }

    /**
     * Returns a list of character and new left/right index for a given range.
     *
     * @param leftIndex left index boundary
     * @param rightIndex right index boundary
     * @return list of counted characters
     */
    public int[][] rangeQuery(int leftIndex, int rightIndex) {
        int[][] query = new int[numMasses + 1][];
        query[numMasses] = new int[]{0};
        
        if (leftIndex + 1 < rightIndex) rangeQuery(leftIndex, rightIndex, query);
        else rangeQueryOneValue(rightIndex, query);
        
        return query;
    }

    /**
     * Fills a list of character and new left/right index for a given range.
     *
     * @param leftIndex left index boundary
     * @param rightIndex right index boundary
     * @param setCharacter list of counted characters
     */
    public void rangeQuery(int leftIndex, int rightIndex, int[][] setCharacter) {
        int newLeftIndex = (leftIndex >= 0) ? rank.getRankOne(leftIndex) : 0;
        int newRightIndex = (rightIndex >= 0) ? rank.getRankOne(rightIndex) : 0;
        
        if (continueRightRangeQuery && newRightIndex - newLeftIndex > 0) {
            if (rightChild != null) {
                rightChild.rangeQuery(newLeftIndex - 1, newRightIndex - 1, setCharacter);
            } else {
                setCharacter[setCharacter[numMasses][0]++] = new int[]{lastChar, newLeftIndex, newRightIndex, lastChar, -1};
            }
        }

        newLeftIndex = leftIndex - newLeftIndex;
        newRightIndex = rightIndex - newRightIndex;
        if (continueLeftRangeQuery && newRightIndex - newLeftIndex > 0) {
            if (leftChild != null) {
                leftChild.rangeQuery(newLeftIndex, newRightIndex, setCharacter);
            } else {
                setCharacter[setCharacter[numMasses][0]++] = new int[]{firstChar, newLeftIndex + 1, newRightIndex + 1, firstChar, -1};
            }
        }
    }
    

    /**
     * Fills a list of character and new left/right index for a given index.
     *
     * @param index index boundary
     * @param setCharacter list of counted characters
     */
    public void rangeQueryOneValue(int index, int[][] setCharacter) {
        int switchOption = rank.isOneInt(index);
        switchOption += leftRightMask & (4 >> switchOption);
        switch(switchOption){
                
            case 3: // go right and right child avaliable
                int newIndex3 = rank.getRankOne(index);
                rightChild.rangeQueryOneValue(newIndex3 - 1, setCharacter);
                break;
                
            case 4: // go left and left child avaliable
                int newIndex4 = rank.getRankZero(index);
                leftChild.rangeQueryOneValue(newIndex4 - 1, setCharacter);
                break;
                
            case 0: // go left and no left child avaliable
                int newIndex0 = rank.getRankZero(index);
                setCharacter[setCharacter[numMasses][0]++] = new int[]{firstChar, newIndex0 - 1, newIndex0, firstChar, -1};
                break;
                
            case 1: // go right and no right child avaliable
                int newIndex1 = rank.getRankOne(index);
                setCharacter[setCharacter[numMasses][0]++] = new int[]{lastChar, newIndex1 - 1, newIndex1, lastChar, -1};
                break;
        }
    }
    
    
    

    /**
     * Returns a new left/right index range for a given character
     * recursively.
     *
     * @param leftIndex left index boundary
     * @param rightIndex right index boundary
     * @param character character to check
     * @return a list of character and new left/right index for a given range
     * recursively
     */
    public int[] singleRangeQuery(int leftIndex, int rightIndex, int character) {
        boolean left = ((alphabetDirections[character >>> shift] >>> (character & mask)) & 1) == 1;

        if (left) {
            int newLeftIndex = (leftIndex >= 0) ? rank.getRankZero(leftIndex) : 0;
            int newRightIndex = (rightIndex >= 0) ? rank.getRankZero(rightIndex) : 0;
            if (leftChild != null) {
                return leftChild.singleRangeQuery(newLeftIndex - 1, newRightIndex - 1, character);
            } else {
                return new int[]{newLeftIndex, newRightIndex};
            }
        } else {
            int newLeftIndex = (leftIndex >= 0) ? rank.getRankOne(leftIndex) : 0;
            int newRightIndex = (rightIndex >= 0) ? rank.getRankOne(rightIndex) : 0;

            if (rightChild != null) {
                return rightChild.singleRangeQuery(newLeftIndex - 1, newRightIndex - 1, character);
            } else {
                return new int[]{newLeftIndex, newRightIndex};
            }
        }
    }
}
