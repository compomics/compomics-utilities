package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * A node of the protein tree.
 *
 * @author Marc Vaudel
 */
public class Node {

    /**
     * The depth of the node in the tree.
     */
    private int depth;
    /**
     * List of accessions contained in this node.
     */
    private HashMap<String, ArrayList<Integer>> accessions = new HashMap<String, ArrayList<Integer>>();
    /**
     * Sutree starting from this node.
     */
    private HashMap<Character, Node> subtree = null;
    /**
     * Instance of the sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The index of the node when saved
     */
    private Long index = null;

    /**
     * Constructor.
     *
     * @param depth the depth of the node
     */
    public Node(int depth) {
        this.depth = depth;
    }

    /**
     * Constructor.
     *
     * @param depth the depth of the node
     * @param accessions the accessions of the node
     */
    public Node(int depth, HashMap<String, ArrayList<Integer>> accessions) {
        this.depth = depth;
        this.accessions = accessions;
    }

    /**
     * Returns the protein mappings for the given peptide sequence. An empty map
     * if not found.
     *
     * @param peptideSequence the given peptide sequence
     * @return the protein mapping for the given peptide sequence
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence) throws IOException, InterruptedException, ClassNotFoundException {
        if (isEmpty()) {
            loadAccessions();
        }
        if (depth == peptideSequence.length()) {
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
            char aa = peptideSequence.charAt(depth);
            Node node = subtree.get(aa);
            if (node != null) {
                return node.getProteinMapping(peptideSequence);
            } else {
                return new HashMap<String, ArrayList<Integer>>();
            }
        }
    }

    /**
     * Splits the node into subnode if its size is larger than the maxNodeSize
     * and does the same for every sub node.
     *
     * @param maxNodeSize the maximal node size allowed when splitting
     * @return returns true if the node was actually splitted and thus needs to
     * be saved in indexed mode
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public boolean splitNode(int maxNodeSize) throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        if (accessions.size() > maxNodeSize) {

            subtree = new HashMap<Character, Node>();
            for (String accession : accessions.keySet()) {
                HashMap<Character, ArrayList<Integer>> indexes = getAA(accession, accessions.get(accession), depth);
                for (char aa : indexes.keySet()) {
                    if (!subtree.containsKey(aa)) {
                        subtree.put(aa, new Node(depth + 1));
                    }
                    Node node = subtree.get(aa);
                    node.addAccession(accession, indexes.get(aa));
                }
            }
            accessions = null;

            return true;
        }
        return false;
    }

    /**
     * Adds an accession to the node.
     *
     * @param accession the accession to add
     * @param indexes the indexes in this accession where the key can be found.
     * Any prior entry will be silently overwritten
     */
    public void addAccession(String accession, ArrayList<Integer> indexes) {
        accessions.put(accession, indexes);
    }

    /**
     * Returns the size of the node in accession*tag.
     *
     * @return the size of the node
     */
    public long getSize() {
        if (accessions != null) {
            return accessions.size();
        } else {
            long result = 0;
            for (Node node : subtree.values()) {
                result += node.getSize();
            }
            return result;
        }
    }

    /**
     * Returns the accessions attribute.
     *
     * @return the accessions attribute
     */
    public HashMap<String, ArrayList<Integer>> getAccessions() {
        return accessions;
    }
    
    /**
     * Returns the subtree. Null if end of the tree.
     * @return 
     */
    public HashMap<Character, Node> getSubtree() {
        return subtree;
    }
    
    /**
     * Clears the accessions of this node
     */
    public void clearAccessions() {
        accessions.clear();
    }
    
    /**
     * Indicates whether the node is empty
     * @return 
     */
    public boolean isEmpty() {
        return subtree == null && accessions.isEmpty();
    }
    
    /**
     * Loads the content of the node from the node factory
     */
    public void loadAccessions() throws IOException {
        NodeFactory nodeFactory = NodeFactory.getInstance();
        accessions = nodeFactory.getAccessions(index);
    }

    /**
     * Returns the depth of the node in the tree.
     *
     * @return the depth of the node in the tree
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns all the protein mapping of the node.
     *
     * @return all the protein mappings of the node
     */
    public HashMap<String, ArrayList<Integer>> getAllMappings() throws IOException {
        if (isEmpty()) {
            loadAccessions();
        }
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

    /**
     * Matches a peptide sequence in a protein sequence based on a seedlist.
     * Example: sequence TESTEIST seeds: 0, 3, 7 peptideSequence: TEI result: 3
     *
     * @param accession the accession of the protein
     * @param seeds the indexes where to start looking for
     * @param peptideSequence the peptide sequence to look for
     * @return a list of indexes having the expected sequence
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    private ArrayList<Integer> matchInProtein(String accession, ArrayList<Integer> seeds, String peptideSequence)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
        ArrayList<Integer> results = new ArrayList<Integer>();
        int peptideLength = peptideSequence.length();

        for (int startIndex : seeds) {
            int endIndex = startIndex + peptideLength;
            if (endIndex <= proteinSequence.length()) {
                String subSequence = proteinSequence.substring(startIndex, endIndex);
                if (subSequence.equals(peptideSequence)) {
                    results.add(startIndex);
                }
            }
        }

        return results;
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
    private HashMap<Character, ArrayList<Integer>> getAA(String accession, ArrayList<Integer> seeds, int offset)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
        HashMap<Character, ArrayList<Integer>> result = new HashMap<Character, ArrayList<Integer>>();

        for (int startIndex : seeds) {
            int tempIndex = startIndex + offset;
            if (tempIndex < proteinSequence.length()) {
                char aa = proteinSequence.charAt(tempIndex);
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
    private ArrayList<Integer> matchInProtein(String accession, ArrayList<Integer> seeds, int offset, char expectedChar)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
        ArrayList<Integer> results = new ArrayList<Integer>();

        for (int startIndex : seeds) {
            int tempIndex = startIndex + offset;
            if (tempIndex < proteinSequence.length()) {
                if (proteinSequence.charAt(tempIndex) == expectedChar) {
                    results.add(startIndex);
                }
            }
        }

        return results;
    }

    /**
     * Returns the index of the node when saved. Null if not set.
     *
     * @return
     */
    public Long getIndex() {
        return index;
    }

    /**
     * Sets the index of the node when saved
     *
     * @param index
     */
    public void setIndex(Long index) {
        this.index = index;
    }
}
