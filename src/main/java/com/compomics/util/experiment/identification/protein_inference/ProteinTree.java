package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class sorts the proteins into groups.
 *
 * @author Marc Vaudel
 */
public class ProteinTree {

    /**
     * The maximal size allowed for a node.
     */
    private int maxNodeSize;
    /**
     * The maximal size allowed for a node.
     */
    private int initialTagSize;
    /**
     * The maximal cache size in number of peptides.
     */
    private int cacheSize;
    /**
     * Instance of the sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * the tree containing the accessions indexed by sequence tags.
     */
    private HashMap<String, Node> tree = new HashMap<String, Node>();

    /**
     * Creates a tree based on the proteins present in the sequence factory.
     */
    public ProteinTree() {
    }

    /**
     * Initiates the tree.
     *
     * @TODO: add progress bar?
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are slow to query, low initial size are slow to initiate. I typically use
     * 3.
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 5000.
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize) throws IOException, IllegalArgumentException, InterruptedException {
        initiateTree(initialTagSize, maxNodeSize, null);
    }

    /**
     * Initiates the tree.
     *
     * @TODO: add progress bar?
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are slow to query, low initial size are slow to initiate. I typically use
     * 3.
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 5000.
     * @param enzyme the enzyme used to select peptides. If null all possible
     * peptides will be indexed (takes more memory)
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, Enzyme enzyme) throws IOException, IllegalArgumentException, InterruptedException {
        this.initialTagSize = initialTagSize;
        this.maxNodeSize = maxNodeSize;
        tree.clear();
        for (String accession : sequenceFactory.getAccessions()) {
            String sequence = sequenceFactory.getProtein(accession).getSequence();
            HashMap<String, ArrayList<Integer>> tagToIndexesMap = new HashMap<String, ArrayList<Integer>>(sequence.length());
            for (int i = 0; i < sequence.length() - initialTagSize; i++) {
                if (enzyme == null || i == 0 || enzyme.isCleavageSite(sequence.charAt(i - 1) + "", sequence.charAt(i) + "")) {
                    char[] tagValue = new char[initialTagSize];
                    for (int j = 0; j < initialTagSize; j++) {
                        char aa = sequence.charAt(i + j);
                        tagValue[j] = aa;
                    }
                    for (int j = 0; j < initialTagSize - 1; j++) {
                        tagValue[j] = sequence.charAt(i + j);
                    }
                    String tag = new String(tagValue);
                    ArrayList<Integer> indexes = tagToIndexesMap.get(tag);
                    if (indexes == null) {
                        indexes = new ArrayList<Integer>();
                        tagToIndexesMap.put(tag, indexes);
                    }
                    indexes.add(i);
                }
            }
            for (String tag : tagToIndexesMap.keySet()) {
                Node node = tree.get(tag);
                if (node == null) {
                    node = new Node(initialTagSize);
                    tree.put(tag, node);
                }
                node.addAccession(accession, tagToIndexesMap.get(tag));
            }
        }
        for (Node node : tree.values()) {
            node.splitNode();
        }
    }

    /**
     * Returns the protein mapping in the sequence factory for the given peptide
     * sequence.
     *
     * @param peptideSequence the peptide sequence
     * @return the peptide to protein mapping: Accession -> list of indexes
     * where the peptide can be found on the sequence
     * @throws IOException
     * @throws InterruptedException
     */
    public HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence) throws IOException, InterruptedException {
        if (peptideSequence.length() < initialTagSize) {
            throw new IllegalArgumentException("Peptide (" + peptideSequence + ") should be at least of length " + initialTagSize + ".");
        }
        String tag = peptideSequence.substring(0, initialTagSize);
        Node node = tree.get(tag);
        if (node != null) {
            return node.getProteinMapping(peptideSequence);
        } else {
            return new HashMap<String, ArrayList<Integer>>();
        }
    }

    /**
     * Returns a map of the amino acids found on the sequence: aa -> indexes.
     *
     * @param accession the accession of the protein of interest
     * @param seeds the indexes where to start looking at
     * @param offset the offset between the seed and the target
     * @return a map of the amino acids found at seed + offset
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    HashMap<Character, ArrayList<Integer>> getAA(String accession, ArrayList<Integer> seeds, int offset) throws IOException, IllegalArgumentException, InterruptedException {
        String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
        HashMap<Character, ArrayList<Integer>> result = new HashMap<Character, ArrayList<Integer>>();
        for (int startIndex : seeds) {
            int index = startIndex + offset;
            if (index < proteinSequence.length()) {
                char aa = proteinSequence.charAt(index);
                ArrayList<Integer> indexes = result.get(aa);
                if (indexes == null) {
                    indexes = new ArrayList<Integer>();
                    result.put(aa, indexes);
                }
                indexes.add(startIndex);
            }
        }
        return result;
    }

    /**
     * Matches an amino-acid in a protein sequence based on a seedlist. Example:
     * sequence TESTEIST seeds: 0, 3, 7 offset: 2 expectedChar: I result: 3
     *
     *
     * @param accession the accession of the protein
     * @param seeds the indexes where to start looking for
     * @param offset the offset where to look for the amino-acid.
     * @param expectedChar the single letter code amino acid to look for
     * @return a list of indexes having the expected amino acid at the index +
     * offset position
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    private ArrayList<Integer> matchInProtein(String accession, ArrayList<Integer> seeds, int offset, char expectedChar) throws IOException, IllegalArgumentException, InterruptedException {
        String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
        ArrayList<Integer> results = new ArrayList<Integer>();
        int index;
        for (int startIndex : seeds) {
            index = startIndex + offset;
            if (index < proteinSequence.length()) {
                if (proteinSequence.charAt(index) == expectedChar) {
                    results.add(startIndex);
                }
            }
        }
        return results;
    }

    /**
     * Matches a peptide sequence in a protein sequence based on a seedlist.
     * Example: sequence TESTEIST seeds: 0, 3, 7 peptideSequence: TEI result: 3
     *
     *
     * @param accession the accession of the protein
     * @param seeds the indexes where to start looking for
     * @param peptideSequence the peptide sequence to look for
     * @return a list of indexes having the expected sequence
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    private ArrayList<Integer> matchInProtein(String accession, ArrayList<Integer> seeds, String peptideSequence) throws IOException, IllegalArgumentException, InterruptedException {
        String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
        ArrayList<Integer> results = new ArrayList<Integer>();
        int peptideLength = peptideSequence.length();
        for (int startIndex : seeds) {
            int endIndex = startIndex + peptideLength;
            if (endIndex < proteinSequence.length()) {
                String subSequence = proteinSequence.substring(startIndex, endIndex);
                if (subSequence.equals(peptideSequence)) {
                    results.add(startIndex);
                }
            }
        }
        return results;
    }

    /**
     * Class representing a node in the tree.
     */
    private class Node {

        /**
         * The index, ie depth in the tree, of the node.
         */
        private int index;
        /**
         * List of accessions contained in this node.
         */
        private HashMap<String, ArrayList<Integer>> accessions = new HashMap<String, ArrayList<Integer>>();
        /**
         * Sutree starting from this node.
         */
        private HashMap<Character, Node> subtree = null;

        /**
         * Constructor.
         *
         * @param index the depth of the node
         */
        public Node(int index) {
            this.index = index;
        }

        /**
         * Returns the protein mappings for the given peptide sequence. An empty
         * map if not found.
         *
         * @param peptideSequence the given peptide sequence
         * @return the protein mapping for the given peptide sequence
         * @throws IOException
         * @throws InterruptedException
         */
        public HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence) throws IOException, InterruptedException {
            if (index == peptideSequence.length()) {
                return getAllMappings();
            } else if (accessions != null) {
                HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>(accessions.size());
                for (String accession : accessions.keySet()) {
                    ArrayList<Integer> indexes = matchInProtein(accession, accessions.get(accession), peptideSequence);
                    if (!indexes.isEmpty()) {
                        result.put(accession, indexes);
                    }
                }
                return result;
            } else {
                char aa = peptideSequence.charAt(index);
                Node node = subtree.get(aa);
                if (node != null) {
                    return node.getProteinMapping(peptideSequence);
                } else {
                    return new HashMap<String, ArrayList<Integer>>();
                }
            }
        }

        /**
         * Splits the node into subnode if its size is larger than the
         * maxNodeSize and does the same for every sub node.
         *
         * @throws IOException
         * @throws IllegalArgumentException
         * @throws InterruptedException
         */
        public void splitNode() throws IOException, IllegalArgumentException, InterruptedException {
            if (accessions.size() > maxNodeSize) {
                subtree = new HashMap<Character, Node>();
                for (String accession : accessions.keySet()) {
                    HashMap<Character, ArrayList<Integer>> indexes = getAA(accession, accessions.get(accession), index);
                    for (char aa : indexes.keySet()) {
                        if (!subtree.containsKey(aa)) {
                            subtree.put(aa, new Node(index + 1));
                        }
                        Node node = subtree.get(aa);
                        node.addAccession(accession, indexes.get(aa));
                    }
                }
                accessions = null;
                for (Node node : subtree.values()) {
                    node.splitNode();
                }
            }
        }

        /**
         * Adds an accession to the node.
         *
         * @param accession the accession to add
         * @param indexes the indexes in this accession where the key can be
         * found. Any prior entry will be silently overwritten
         */
        public void addAccession(String accession, ArrayList<Integer> indexes) {
            accessions.put(accession, indexes);
        }

        /**
         * Returns all the protein mapping of the node.
         *
         * @return all the protein mappings of the node
         */
        public HashMap<String, ArrayList<Integer>> getAllMappings() {
            if (accessions != null) {
                return accessions;
            } else {
                HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
                for (Node node : subtree.values()) {
                    HashMap<String, ArrayList<Integer>> subResult = node.getAllMappings();
                    for (String accession : subResult.keySet()) {
                        ArrayList<Integer> indexes = result.get(accession);
                        if (indexes == null) {
                            indexes = new ArrayList<Integer>();
                            indexes.addAll(subResult.get(accession));
                            result.put(accession, indexes);
                        } else {
                            indexes.addAll(subResult.get(accession));
                            Collections.sort(indexes);
                            int previousIndex = -1;
                            ArrayList<Integer> singleIndexes = new ArrayList<Integer>(indexes.size());
                            for (int tempIndex : indexes) {
                                if (tempIndex != previousIndex) {
                                    singleIndexes.add(tempIndex);
                                    previousIndex = tempIndex;
                                }
                            }
                            result.put(accession, singleIndexes);
                        }
                    }
                }
                return result;
            }
        }
    }
}
