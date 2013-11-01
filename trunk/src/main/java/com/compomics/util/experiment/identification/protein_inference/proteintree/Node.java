package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Protein;
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
     * The number of proteins which should be imported at a time.
     */
    public static final int proteinBatchSize = 100;
    /**
     * Indicates whether the main thread is listening or preparing to wait.
     */
    private boolean listening = true;

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
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence,
            ProteinMatch.MatchingType matchingType, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException {

        HashMap<String, HashMap<String, ArrayList<Integer>>> result = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

        if (depth == peptideSequence.length()) {
            result.put(peptideSequence, getAllMappings());
        } else if (accessions != null) {

            int nThreads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
            ArrayList<Protein> sequenceBuffer = new ArrayList<Protein>(proteinBatchSize);
            HashMap<String, ArrayList<Integer>> seeds = new HashMap<String, ArrayList<Integer>>(proteinBatchSize);
            ArrayList<SequenceMatcher> sequenceMatchers = new ArrayList<SequenceMatcher>(nThreads);
            SequenceFactory sequenceFactory = SequenceFactory.getInstance();

            for (String accession : accessions.keySet()) {
                Protein protein = sequenceFactory.getProtein(accession);    
                sequenceBuffer.add(protein);
                seeds.put(accession, accessions.get(accession));
                if (sequenceBuffer.size() == proteinBatchSize) {
                    while (sequenceMatchers.size() == nThreads) {
                        processFinishedMatchers(sequenceMatchers, result);
                    }
                    SequenceMatcher sequenceMatcher = new SequenceMatcher(sequenceBuffer, seeds, peptideSequence, matchingType, massTolerance);
                    new Thread(sequenceMatcher, "sequence indexing").start();
                    sequenceMatchers.add(sequenceMatcher);
                    sequenceBuffer = new ArrayList<Protein>(proteinBatchSize);
                    seeds = new HashMap<String, ArrayList<Integer>>(proteinBatchSize);
                }
            }

            if (!sequenceBuffer.isEmpty()) {
                SequenceMatcher sequenceMatcher = new SequenceMatcher(sequenceBuffer, seeds, peptideSequence, matchingType, massTolerance);
                new Thread(sequenceMatcher, "sequence indexing").start();
                sequenceMatchers.add(sequenceMatcher);
            }

            while (!sequenceMatchers.isEmpty()) {
                processFinishedMatchers(sequenceMatchers, result);
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
     * Stores the result of the finished indexers and updates the list. Waits if
     * none is finished.
     *
     * @param sequenceIndexers the sequence indexers
     * @param result a map where to store the results
     *
     * @throws InterruptedException
     */
    private synchronized void processFinishedMatchers(ArrayList<SequenceMatcher> sequenceMatchers, HashMap<String, HashMap<String, ArrayList<Integer>>> result) throws InterruptedException {

        listening = false;
        ArrayList<SequenceMatcher> done = new ArrayList<SequenceMatcher>();

        for (SequenceMatcher sequenceMatcher : sequenceMatchers) {
            if (sequenceMatcher.isFinished()) {
                done.add(sequenceMatcher);
            }
        }

        if (done.isEmpty()) {
            listening = true;
            wait();
            for (SequenceMatcher sequenceMatcher : sequenceMatchers) {
                if (sequenceMatcher.isFinished()) {
                    done.add(sequenceMatcher);
                }
            }
        }

        listening = true;

        for (SequenceMatcher sequenceMatcher : done) {
            HashMap<String, HashMap<String, ArrayList<Integer>>> indexes = sequenceMatcher.getIndexes();
            for (String accession : indexes.keySet()) {
                for (String tempSequence : indexes.get(accession).keySet()) {
                    HashMap<String, ArrayList<Integer>> mapping = result.get(tempSequence);
                    if (mapping == null) {
                        mapping = new HashMap<String, ArrayList<Integer>>(1);
                        result.put(tempSequence, mapping);
                    }
                    mapping.put(accession, indexes.get(accession).get(tempSequence));
                }
            }
            sequenceMatcher.clear();
        }

        sequenceMatchers.removeAll(done);
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

            accessions.clear();
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
                        indexes = new ArrayList<Integer>(subResult.get(accession).size());
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
                    indexes = new ArrayList<Integer>(0);
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
     * @param protein the protein to inspect
     * @param seeds the indexes where to start looking for
     * @param peptidePattern the peptide sequence as an amino acid pattern
     * @param peptideLength the peptide length
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance
     *
     * @return a list of indexes having the expected sequence
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    private HashMap<String, ArrayList<Integer>> matchInProtein(Protein protein, ArrayList<Integer> seeds,
            AminoAcidPattern peptidePattern, int peptideLength, MatchingType matchingType, Double massTolerance)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        String proteinSequence = protein.getSequence();
        HashMap<String, ArrayList<Integer>> results = new HashMap<String, ArrayList<Integer>>();

        for (int startIndex : seeds) {
            int endIndex = startIndex + peptideLength;
            if (endIndex <= proteinSequence.length()) {
                String subSequence = proteinSequence.substring(startIndex, endIndex);
                if (peptidePattern.matches(subSequence, matchingType, massTolerance)) {
                    ArrayList<Integer> indexes = results.get(subSequence);
                    if (indexes == null) {
                        indexes = new ArrayList<Integer>(0);
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
                    indexes = new ArrayList<Integer>(0);
                    result.put(aa, indexes);
                }
                if (!indexes.contains(startIndex)) {
                    indexes.add(startIndex);
                }
            } else if (tempIndex == proteinSequence.length()) {
                ArrayList<Integer> indexes = termini.get(accession);
                if (indexes == null) {
                    indexes = new ArrayList<Integer>(0);
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

    /**
     * Notifies the tree that a runnable has finished working.
     *
     * @throws InterruptedException
     */
    private synchronized void runnableFinished() throws InterruptedException {
        while (!listening) {
            wait(10);
        }
        notify();
    }

    /**
     * Runnable used for matching peptides on protein sequences.
     */
    private class SequenceMatcher implements Runnable {

        /**
         * The proteins to process.
         */
        private ArrayList<Protein> proteins;
        /**
         * Boolean indicating whether the thread shall be interrupted.
         */
        private boolean finished = false;
        /**
         * The seed indexes where to look for the peptide in a map. Protein
         * accession -> list of seed indexes.
         */
        private HashMap<String, ArrayList<Integer>> seeds;
        /**
         * The peptide sequence to look for.
         */
        private String peptideSequence;
        /**
         * The type of matching.
         */
        private MatchingType matchingType;
        /**
         * The mass tolerance at the MS2 level.
         */
        private Double massTolerance;
        /**
         * The result of the indexing. protein accession -> peptide found ->
         * list of indexes where the peptide is found.
         */
        private HashMap<String, HashMap<String, ArrayList<Integer>>> indexes = new HashMap<String, HashMap<String, ArrayList<Integer>>>(proteinBatchSize);

        /**
         * Constructor.
         *
         * @param proteins the proteins to look into
         * @param seeds the seed indexes where to look for in a map protein
         * accession -> list of indexes
         * @param peptideSequence the peptide sequence to look for
         * @param matchingType the peptide to protein matching type
         * @param massTolerance the mass tolerance used for the matching
         */
        public SequenceMatcher(ArrayList<Protein> proteins, HashMap<String, ArrayList<Integer>> seeds, String peptideSequence, MatchingType matchingType, Double massTolerance) {
            this.proteins = proteins;
            this.seeds = seeds;
            this.peptideSequence = peptideSequence;
            this.matchingType = matchingType;
            this.massTolerance = massTolerance;
        }

        @Override
        public synchronized void run() {

            AminoAcidPattern peptidePattern = new AminoAcidPattern(peptideSequence);

            for (Protein protein : proteins) {
                try {
                    String accession = protein.getAccession();
                    indexes.put(accession, matchInProtein(protein, seeds.get(accession), peptidePattern, peptideSequence.length(), matchingType, massTolerance));
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

            finished = true;

            try {
                runnableFinished();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Indicates whether the run is finished.
         *
         * @return true if the thread is finished.
         */
        public boolean isFinished() {
            return finished;
        }

        /**
         * Returns the indexes: protein accession -> tag -> indexes of the tag
         * on the protein sequence
         *
         * @return the indexes
         */
        public HashMap<String, HashMap<String, ArrayList<Integer>>> getIndexes() {
            return indexes;
        }

        /**
         * Clears the content of the runnable.
         */
        public void clear() {
            proteins.clear();
            seeds.clear();
            indexes.clear();
        }
    }
}
