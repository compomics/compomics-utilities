package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.TagFactory;
import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
     * The memory allocation in GB.
     */
    private int memoryAllocation;
    /**
     * Approximate number of accession*node one can store in a GB of memory.
     */
    private static final long cacheScale = 7000000;
    /**
     * Instance of the sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The tree containing the accessions indexed by sequence tags.
     */
    private HashMap<String, Node> tree = new HashMap<String, Node>();
    /**
     * List of the nodes in tree.
     */
    private ArrayList<String> tagsInTree = new ArrayList<String>();
    /**
     * The size of the tree in memory in accession*node.
     */
    private long treeSize = 0;
    /**
     * Indexed version of the tree.
     */
    private HashMap<String, Long> indexedTree = new HashMap<String, Long>();
    /**
     * Indicates whether a debug file with speed metrics shall be created.
     */
    private boolean debugSpeed = true;
    /**
     * The writer used to send the output to a debug file.
     */
    private BufferedWriter debugSpeedWriter = null;
    /**
     * The node factory when operating in indexed mode.
     */
    private NodeFactory nodeFactory = null;
    /**
     * Convenience mapping of the length of the proteins in case of reversed
     * sequences.
     */
    private HashMap<String, Integer> proteinLengthMap = new HashMap<String, Integer>();

    /**
     * Creates a tree based on the proteins present in the sequence factory.
     *
     * @param indexFolder the folder where to store the database tree. If null
     * the tree will be kept in memory
     * @param memoryAllocation the number of GB available for the tree in
     * memory.
     */
    public ProteinTree(File indexFolder, int memoryAllocation) {

        if (indexFolder != null) {
            nodeFactory = NodeFactory.getInstance(indexFolder);
            this.memoryAllocation = memoryAllocation;
        }

        if (debugSpeed) {
            try {
                debugSpeedWriter = new BufferedWriter(new FileWriter(new File("dbSpeed.txt")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initiates the tree.
     *
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 5000.
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null but strongly recommended :)
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {
        initiateTree(initialTagSize, maxNodeSize, null, waitingHandler);
    }

    /**
     * Initiates the tree. Note: the memory consumption is for now only
     * calibrated for the no enzyme case.
     *
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are slow to query, low initial size are slow to initiate. I typically use
     * 3 for databases containing less than 100 000 proteins.
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 5000.
     * @param enzyme the enzyme used to select peptides. If null all possible
     * peptides will be indexed (takes more memory)
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null.
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, Enzyme enzyme, WaitingHandler waitingHandler)
            throws IOException, IllegalArgumentException, InterruptedException, IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        this.initialTagSize = initialTagSize;
        this.maxNodeSize = maxNodeSize;
        tree.clear();

        if (nodeFactory != null) {
            nodeFactory.initiateFactory();
        }

        ArrayList<String> tags = TagFactory.getAminoAcidCombinations(initialTagSize);
        ArrayList<String> accessions;
        if (sequenceFactory.isDefaultReversed()) {
            accessions = new ArrayList<String>();
            for (String accession : sequenceFactory.getAccessions()) {
                if (!SequenceFactory.isDecoy(accession)) {
                    accessions.add(accession);
                }
            }
        } else {
            accessions = sequenceFactory.getAccessions();
        }
        long criticalSize = accessions.size() * tags.size();
        // try to estimate the number of tags we can process at a time given the memory settings. We might want to fine tune this
        long capacity = memoryAllocation * cacheScale;
        long estimatedTreeSize = 6 * criticalSize; //in percent, as far as I tested, 6% of the proteins are covered by a tag in general (ie median)
        int ratio = (int) (estimatedTreeSize / capacity);
        int nTags = 100 * tags.size() / ratio;
        int nPassages = (int) (ratio / 100) + 1;
        if (nPassages > 1) {
            Collections.shuffle(tags);
        }
        if (debugSpeed) {
            debugSpeedWriter.write("Critical size: " + criticalSize);
            System.out.println("Critical size: " + criticalSize);
            estimatedTreeSize = estimatedTreeSize / 100;
            debugSpeedWriter.write("Estimated tree size: " + estimatedTreeSize);
            System.out.println("Estimated tree size: " + estimatedTreeSize);
            debugSpeedWriter.write(nPassages + " passages needed (" + nTags + " tags of " + tags.size() + " per passage)");
            System.out.println(nPassages + " passages needed (" + nTags + " tags of " + tags.size() + " per passage)");
            debugSpeedWriter.newLine();
            debugSpeedWriter.flush();
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            int totalProgress = (int) (nPassages * accessions.size() + tags.size());
            waitingHandler.setMaxSecondaryProgressValue(totalProgress);
            waitingHandler.setSecondaryProgressValue(0);
        }

        long time0 = System.currentTimeMillis();

        ArrayList<String> tempTags = new ArrayList<String>(nTags);
        int roundsCpt = 0;
        for (String tag : tags) {
            if (tempTags.size() == nTags) {
                loadTags(tempTags, accessions, waitingHandler, enzyme, true, false);
                tempTags.clear();
                if (sequenceFactory.getnCache() < accessions.size()) {
                    Collections.reverse(accessions);
                }
                if (debugSpeed) {
                    debugSpeedWriter.write(++roundsCpt + " passages completed");
                    System.out.println(roundsCpt + " passages completed");
                    debugSpeedWriter.newLine();
                    debugSpeedWriter.flush();
                }
            } else {
                tempTags.add(tag);
            }
        }
        if (!tempTags.isEmpty()) {
            loadTags(tempTags, accessions, waitingHandler, enzyme, false, sequenceFactory.isDefaultReversed());
            if (debugSpeed) {
                debugSpeedWriter.write(++roundsCpt + " rounds completed");
                System.out.println(roundsCpt + " rounds completed");
                debugSpeedWriter.newLine();
                debugSpeedWriter.flush();
            }
        }

        if (debugSpeed) {

            long time1 = System.currentTimeMillis();
            long initiationTime = time1 - time0;

            debugSpeedWriter.write("tree initiation: " + initiationTime + " ms.");
            System.out.println("tree initiation: " + initiationTime + " ms.");
            debugSpeedWriter.write("tree size: " + tree.size() + " tags, " + treeSize + " node.accession loaded and saved.");
            System.out.println("tree size: " + tree.size() + " tags, " + treeSize + " node.accession loaded and saved.");
            debugSpeedWriter.newLine();
            debugSpeedWriter.flush();
        }
    }

    /**
     * Loads the tags found in the given proteins in the tree and saves the end
     * nodes in the NodeFactory if not null.
     *
     * @param tags the tags of interest
     * @param accessions the accessions of the proteins of interest
     * @param waitingHandler waiting handler displaying progress to the user -
     * can be null
     * @param enzyme the enzyme restriction
     * @param clearNodes boolean indicating whether the end nodes shall be
     * cleared when saving
     * @param saveLength boolean indicating whether the length of the proteins
     * shall be saved (mandatory when computing reverse indexes on the fly)
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private void loadTags(ArrayList<String> tags, ArrayList<String> accessions, WaitingHandler waitingHandler,
            Enzyme enzyme, boolean clearNodes, boolean saveLength) throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException {

        for (String accession : accessions) {

            //@TODO: would be cool to have this multithreaded
            String sequence = sequenceFactory.getProtein(accession).getSequence();

            if (saveLength) {
                proteinLengthMap.put(accession, sequence.length());
            }

            HashMap<String, ArrayList<Integer>> tagToIndexesMap = getTagToIndexesMap(sequence, tags, enzyme);

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
            }

            for (String tag : tagToIndexesMap.keySet()) {
                ArrayList<Integer> indexes = tagToIndexesMap.get(tag);
                if (!indexes.isEmpty()) {
                    Node node = tree.get(tag);
                    if (node == null) {
                        node = new Node(initialTagSize);
                        tagsInTree.add(tag);
                    }
                    tree.put(tag, node);
                    node.addAccession(accession, tagToIndexesMap.get(tag));
                    treeSize++;
                }
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
                waitingHandler.increaseSecondaryProgressValue();
            }

        }

        for (String tag : tags) {

            Node node = tree.get(tag);

            if (node != null) {
                node.splitNode(maxNodeSize);
                if (nodeFactory != null) {
                    nodeFactory.saveAccessions(node, clearNodes);
                }
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
                waitingHandler.increaseSecondaryProgressValue();
            }

        }
    }

    /**
     * Returns all the positions of the given tags on the given sequence in a
     * map: tag -> list of indexes in the sequence.
     *
     * @param sequence the sequence of interest
     * @param tags the tags of interest
     * @param enzyme the enzyme restriction
     * @return all the positions of the given tags
     */
    private HashMap<String, ArrayList<Integer>> getTagToIndexesMap(String sequence, ArrayList<String> tags, Enzyme enzyme) {
        HashMap<String, ArrayList<Integer>> tagToIndexesMap = new HashMap<String, ArrayList<Integer>>(tags.size());
        for (String tag : tags) {
            tagToIndexesMap.put(tag, new ArrayList<Integer>());
        }

        for (int i = 0; i < sequence.length() - initialTagSize; i++) {

            if (enzyme == null || i == 0 || enzyme.isCleavageSite(sequence.charAt(i - 1) + "", sequence.charAt(i) + "")) {
                char[] tagValue = new char[initialTagSize];
                for (int j = 0; j < initialTagSize; j++) {
                    char aa = sequence.charAt(i + j);
                    tagValue[j] = aa;
                }
                String tag = new String(tagValue);
                ArrayList<Integer> indexes = tagToIndexesMap.get(tag);
                if (indexes != null) {
                    indexes.add(i);
                }
            }
        }
        return tagToIndexesMap;
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
     * @throws ClassNotFoundException
     */
    public HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence) throws IOException, InterruptedException, ClassNotFoundException {
        long time0 = System.currentTimeMillis();
        HashMap<String, ArrayList<Integer>> result = getProteinMapping(peptideSequence, false);
        if (debugSpeed) {
            long time1 = System.currentTimeMillis();
            long queryTime = time1 - time0;
            debugSpeedWriter.write(peptideSequence + "\t" + result.size() + "\t" + queryTime);
            debugSpeedWriter.newLine();
            debugSpeedWriter.flush();
        }
        return result;
    }

    /**
     * Returns the protein mapping in the sequence factory for the given peptide
     * sequence.
     *
     * @param peptideSequence the peptide sequence
     * @param reversed boolean indicating whether we are looking at a reversed
     * peptide sequence
     * @return the peptide to protein mapping: Accession -> list of indexes
     * where the peptide can be found on the sequence
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence, boolean reversed) throws IOException, InterruptedException, ClassNotFoundException {

        if (peptideSequence.length() < initialTagSize) {
            throw new IllegalArgumentException("Peptide (" + peptideSequence + ") should be at least of length " + initialTagSize + ".");
        }

        HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();

        String tag = peptideSequence.substring(0, initialTagSize);
        Node node = tree.get(tag);

        if (node != null) {
            result.putAll(node.getProteinMapping(peptideSequence));
        }
        if (sequenceFactory.isDefaultReversed() && !reversed) {
            String reversedSequence = SequenceFactory.reverseSequence(peptideSequence);
            HashMap<String, ArrayList<Integer>> reversedResult = getProteinMapping(reversedSequence, true);
            int peptideLength = peptideSequence.length();
            for (String accession : reversedResult.keySet()) {
                String reversedAccession = SequenceFactory.getDefaultDecoyAccession(accession);
                ArrayList<Integer> reversedIndexes = new ArrayList<Integer>();
                Integer proteinLength = proteinLengthMap.get(accession);
                if (proteinLength == null) {
                    throw new IllegalArgumentException("Length of protein " + accession + " not found.");
                }
                for (int index : reversedResult.get(accession)) {
                    int reversedIndex = proteinLength - index - peptideLength;
                    if (reversedIndex < 0) {
                        throw new IllegalArgumentException("Negative index found for peptide " + reversedSequence + " in protein " + reversedAccession + ".");
                    }
                    reversedIndexes.add(reversedIndex);
                }
                result.put(reversedAccession, reversedIndexes);
            }
        }
        return result;
    }

    /**
     * Closes all connections to files.
     */
    public void close() {
        if (debugSpeed) {
            try {
                debugSpeedWriter.flush();
                debugSpeedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (nodeFactory != null) {
            try {
                nodeFactory.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
