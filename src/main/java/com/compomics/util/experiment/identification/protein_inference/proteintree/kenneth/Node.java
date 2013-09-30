package com.compomics.util.experiment.identification.protein_inference.proteintree.kenneth;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch.MatchingType;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * A node of the protein tree.
 *
 * @author Marc Vaudel
 */
public class Node implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 8936868785405252371L;
    /**
     * The depth of the node in the tree.
     */
    private int depth;
    /**
     * List of accessions contained in this node.
     */
    private HashMap<String, ArrayList<Integer>> accessions = new HashMap<String, ArrayList<Integer>>();
    /**
     * In case of splitting, the terminal mappings are put here.
     */
    private HashMap<String, ArrayList<Integer>> termini = new HashMap<String, ArrayList<Integer>>();
    /**
     * Subtree starting from this node.
     */
    private HashMap<Character, Node> subtree = null;

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
     * Returns the protein mappings for the given peptide sequence. peptide
     * sequence -> protein accession -> index in the protein. An empty map if
     * not found.
     *
     * @param peptideSequence the given peptide sequence
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return the protein mapping for the given peptide sequence
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence, ProteinMatch.MatchingType matchingType, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException {
        HashMap<String, HashMap<String, ArrayList<Integer>>> result = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
        if (depth == peptideSequence.length()) {
            result.put(peptideSequence, getAllMappings());
        } else if (accessions != null) {
            for (String accession : accessions.keySet()) {
                HashMap<String, ArrayList<Integer>> indexes = matchInProtein(accession, accessions.get(accession), peptideSequence, matchingType, massTolerance);
                for (String tempSequence : indexes.keySet()) {
                    HashMap<String, ArrayList<Integer>> mapping = result.get(tempSequence);
                    if (mapping == null) {
                        mapping = new HashMap<String, ArrayList<Integer>>();
                        result.put(tempSequence, mapping);
                    }
                    mapping.put(accession, indexes.get(tempSequence));
                }
            }
        } else {
            for (char aa : getNextAminoAcids(peptideSequence, matchingType, massTolerance)) {
                Node node = subtree.get(aa);
                if (node != null) {
                    result.putAll(node.getProteinMapping(peptideSequence, matchingType, massTolerance));
                }
            }
        }
        return result;
    }

    /**
     * Returns the possible next amino acids.
     *
     * @param peptideSequence the peptide sequence
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance
     *
     * @return the possible next amino acids
     */
    private ArrayList<Character> getNextAminoAcids(String peptideSequence, ProteinMatch.MatchingType matchingType, Double massTolerance) {

        char aa = peptideSequence.charAt(depth);
        ArrayList<Character> result = new ArrayList<Character>();

        if (matchingType == ProteinMatch.MatchingType.string) {
            result.add(aa);
            return result;
        }

        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);

        for (char aaChar : aminoAcid.getSubAminoAcids()) {
            result.add(aaChar);
        }

        for (char aaChar : aminoAcid.getCombinations()) {
            result.add(aaChar);
        }

        if (matchingType == ProteinMatch.MatchingType.indistiguishibleAminoAcids) {
            for (char aaChar : aminoAcid.getIndistinguishibleAminoAcids(massTolerance)) {
                if (!result.contains(aaChar)) {
                    result.add(aaChar);
                }
            }
        }

        return result;
    }

    /**
     * Splits the node into subnode if its size is larger than the maxNodeSize
     * and does the same for every sub node.
     *
     * @param maxNodeSize the maximal node size allowed when splitting
     * @param maxDepth the maximum depth
     * @return returns true if the node was actually splitted and thus needs to
     * be saved in indexed mode
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public boolean splitNode(int maxNodeSize, int maxDepth) throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {
        if (accessions.size() > maxNodeSize && depth <= maxDepth) {
            subtree = new HashMap<Character, Node>();
            for (String accession : accessions.keySet()) {
                HashMap<Character, ArrayList<Integer>> indexes = getAA(accession, accessions.get(accession), depth);
                if (indexes.isEmpty()) {
                    indexes = getAA(accession, accessions.get(accession), depth);
                }
                for (char aa : indexes.keySet()) {
                    if (!subtree.containsKey(aa)) {
                        subtree.put(aa, new Node(depth + 1));
                    }
                    Node node = subtree.get(aa);
                    node.addAccession(accession, indexes.get(aa));
                }
            }
            accessions = null;

            for (Node node : subtree.values()) {
                node.splitNode(maxNodeSize, maxDepth);
            }
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
            if (subtree != null) {
                for (Node node : subtree.values()) {
                    if (node != null) {
                        result += node.getSize();
                    }
                }
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
     * Returns the terminal mappings (they are not in the subtree).
     *
     * @return the terminal mappings
     */
    public HashMap<String, ArrayList<Integer>> getTermini() {
        return termini;
    }

    /**
     * Returns the subtree. Null if end of the tree.
     *
     * @return the subtree
     */
    public HashMap<Character, Node> getSubtree() {
        return subtree;
    }

    /**
     * Clears the accessions of this node.
     */
    public void clearAccessions() {
        accessions.clear();
    }

    /**
     * Indicates whether the node is empty.
     *
     * @return whether the node is empty
     */
    public boolean isEmpty() {
        return subtree == null && accessions.isEmpty();
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
     * @throws IOException
     */
    public HashMap<String, ArrayList<Integer>> getAllMappings() throws IOException {
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
            for (String accession : termini.keySet()) {
                ArrayList<Integer> indexes = result.get(accession);
                if (indexes == null) {
                    indexes = new ArrayList<Integer>();
                    result.put(accession, indexes);
                }
                for (Integer index : termini.get(accession)) {
                    if (!indexes.contains(index)) {
                        indexes.add(index);
                    }
                }
            }
            return result;
        }
    }

    /**
     * Matches a peptide sequence in a protein sequence based on a seedlist.
     * Returns a map found sequence -> indexes. Example: sequence TESTEIST
     * seeds: 0, 3, 7 peptideSequence: TEI result: TEI -> {3}
     *
     * @param accession the accession of the protein
     * @param seeds the indexes where to start looking for
     * @param peptideSequence the peptide sequence to look for
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance
     *
     * @return a list of indexes having the expected sequence
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    private HashMap<String, ArrayList<Integer>> matchInProtein(String accession, ArrayList<Integer> seeds, String peptideSequence, MatchingType matchingType, Double massTolerance)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        String proteinSequence = SequenceFactory.getInstance().getProtein(accession).getSequence();
        HashMap<String, ArrayList<Integer>> results = new HashMap<String, ArrayList<Integer>>();
        int peptideLength = peptideSequence.length();
        for (int startIndex : seeds) {
            int endIndex = startIndex + peptideLength;
            if (endIndex <= proteinSequence.length()) {
                String subSequence = proteinSequence.substring(startIndex, endIndex);
                AminoAcidPattern pattern = new AminoAcidPattern(peptideSequence);
                if (pattern.matches(subSequence, matchingType, massTolerance)) {
                    ArrayList<Integer> indexes = results.get(subSequence);
                    if (indexes == null) {
                        indexes = new ArrayList<Integer>();
                        results.put(subSequence, indexes);
                    }
                    indexes.add(startIndex);
                }
            }
        }
        return results;
    }

    /**
     * Returns a map of the amino acids found on the sequence: aa -> indexes. If
     * the termination of the protein is reached the terminal character is used
     * (see static field)
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

        String proteinSequence = SequenceFactory.getInstance().getProtein(accession).getSequence();
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
                if (!indexes.contains(startIndex)) {
                    indexes.add(startIndex);
                }
            } else if (tempIndex == proteinSequence.length()) {
                ArrayList<Integer> indexes = termini.get(accession);
                if (indexes == null) {
                    indexes = new ArrayList<Integer>();
                    termini.put(accession, indexes);
                }
                if (!indexes.contains(startIndex)) {
                    indexes.add(startIndex);
                }
            } else {
                throw new IllegalArgumentException("Attempting to index after the protein termini.");
            }
        }
        return result;
    }

    /**
     * Returns the subnode associated to an amino acid sequence.
     *
     * @param sequence the amino acid sequence
     *
     * @return the corresponding subnode
     */
    public Node getSubNode(String sequence) {
        if (sequence.length() <= depth) {
            throw new IllegalArgumentException(sequence + " is not subnode of the node (depth=" + depth + ").");
        }
        char aa = sequence.charAt(depth);
        if (depth < sequence.length() - 1) {
            return subtree.get(aa).getSubNode(sequence);
        } else if (depth == sequence.length() - 1) {
            return subtree.get(aa);
        } else {
            throw new IllegalArgumentException("depth " + depth + " longer than sequence " + sequence + ".");
        }
    }
}
