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
     * The maximal cache size in accession*node.
     */
    private long cacheSize;
    /**
     * Approximate number of accession*node one can store in a GB of memory
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
     * List of the nodes in tree
     */
    private ArrayList<String> tagsInTree = new ArrayList<String>();
    /**
     * The size of the tree in memory in accession*node.
     */
    private long treeSize = 0;
    /**
     * indexed version of the tree
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
     * Creates a tree based on the proteins present in the sequence factory.
     *
     * @param indexFolder the folder where to store the database tree. If null
     * the tree will be kept in memory
     * @param memoryAllocation the number of GB available for the tree in memory
     */
    public ProteinTree(File indexFolder, int memoryAllocation) {

        if (indexFolder != null) {
            nodeFactory = NodeFactory.getInstance(indexFolder);
            cacheSize = memoryAllocation * cacheScale;
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
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are slow to query, low initial size are slow to initiate. I typically use
     * 3.
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
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null but strongly recommended :)
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

        HashMap<String, Boolean> splittingProgress = new HashMap<String, Boolean>();
        int progressTagSize = Math.min(2, initialTagSize);

        if (waitingHandler != null) {
            // For the progress of the splitting we create a list of tags and show when they are encountered
            for (String tag : TagFactory.getAminoAcidCombinations(progressTagSize)) {
                splittingProgress.put(tag, Boolean.FALSE);
            }
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            int totalProgress = sequenceFactory.getAccessions().size() + splittingProgress.size();
            waitingHandler.setMaxSecondaryProgressValue(totalProgress);
            waitingHandler.setSecondaryProgressValue(0);
        }

        long time0 = System.currentTimeMillis();
        long nodeSaved = 0;
        long nodeRead = 0;

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
                Node node = getNode(tag, nodeRead);
                if (node == null) {
                    node = new Node(initialTagSize);
                }
                tree.put(tag, node);
                node.addAccession(accession, tagToIndexesMap.get(tag));
                treeSize++;
                tagsInTree.remove(tag);
                tagsInTree.add(tag);
            }

            while (nodeFactory != null && treeSize >= cacheSize && !tagsInTree.isEmpty()) {
                String tag = tagsInTree.get(0);
                Node node = tree.get(tag);
                treeSize -= node.getSize();
                long nodeIndex = nodeFactory.saveNode(node, tag);
                indexedTree.put(tag, nodeIndex);
                tree.remove(tag);
                tagsInTree.remove(0);
                nodeSaved++;
            }

            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressValue();
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
            }
        }

        long debug = treeSize;
        debug = nodeSaved;
        debug = nodeRead;
        long time1 = System.currentTimeMillis();

        for (String tag : tree.keySet()) {
            Node node = tree.get(tag);
            boolean changed = node.splitNode(maxNodeSize, nodeFactory);
            if (nodeFactory != null && changed) {
                long nodeIndex = nodeFactory.saveNode(node, "");
                indexedTree.put(tag, nodeIndex);
            }

            if (waitingHandler != null) {
                String subTag;
                if (initialTagSize > progressTagSize) {
                    subTag = tag.substring(0, progressTagSize);
                } else {
                    subTag = tag;
                }
                Boolean newTag = splittingProgress.get(subTag);
                if (newTag == null) {
                    throw new IllegalArgumentException("Unexpected amino acid sequence: " + subTag + " when indexing the database.");
                }
                if (!newTag) {
                    waitingHandler.increaseSecondaryProgressValue();
                    splittingProgress.put(subTag, true);
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    }
                }
            }
        }

        long time2 = System.currentTimeMillis();

        long initiationTime = time1 - time0;
        long splittingTime = time2 - time1;

        if (debugSpeed) {
            debugSpeedWriter.write("tree initiation: " + initiationTime + " ms.");
            debugSpeedWriter.newLine();
            debugSpeedWriter.write("nodes splitting: " + splittingTime + " ms.");
            debugSpeedWriter.newLine();
            debugSpeedWriter.flush();
        }
    }

    /**
     * Returns the node corresponding to a tag by looking first in the tree and
     * then in the factory
     *
     * @param tag the tag of interest
     * @return the corresponding node, null if not found
     * @throws IOException
     */
    private Node getNode(String tag, long nodeOpened) throws IOException {
        Node node = tree.get(tag);
        if (node == null) {
            if (nodeFactory != null) {
                Long nodeIndex = indexedTree.get(tag);
                if (nodeIndex != null) {
                        node = nodeFactory.getNode(nodeIndex, tag);
                    nodeOpened++;
                }
            }
        }
        return node;
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

        if (peptideSequence.length() < initialTagSize) {
            throw new IllegalArgumentException("Peptide (" + peptideSequence + ") should be at least of length " + initialTagSize + ".");
        }

        long time0 = System.currentTimeMillis();

        String tag = peptideSequence.substring(0, initialTagSize);
        Node node = tree.get(tag);

        if (node != null) {
            HashMap<String, ArrayList<Integer>> result = node.getProteinMapping(peptideSequence, nodeFactory);
            if (debugSpeed) {
                long time1 = System.currentTimeMillis();
                long queryTime = time1 - time0;
                debugSpeedWriter.write(peptideSequence + "\t" + result.size() + "\t" + queryTime);
                debugSpeedWriter.newLine();
                debugSpeedWriter.flush();
            }
            return result;
        } else {
            return new HashMap<String, ArrayList<Integer>>();
        }
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
