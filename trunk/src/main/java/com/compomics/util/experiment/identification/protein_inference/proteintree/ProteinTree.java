package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.TagFactory;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * This class sorts the proteins into groups.
 *
 * @author Marc Vaudel
 */
public class ProteinTree {

    /**
     * The memory allocation in GB.
     */
    private int memoryAllocation;
    /**
     * Approximate number of accession*node one can store in a GB of memory (empirical value).
     */
    private static final long cacheScale = 12000000;
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
    private ProteinTreeComponentsFactory componentsFactory = null;
    /**
     * size of the cache of the most queried peptides
     */
    private int cacheSize = 20000;
    /**
     * Cache of the last queried peptides
     */
    private HashMap<String, HashMap<String, ArrayList<Integer>>> lastQueriedPeptidesCache = new HashMap<String, HashMap<String, ArrayList<Integer>>>(cacheSize);
    /**
     * peptide sequences in cache
     */
    private ArrayList<String> lastQueriedPeptidesCacheContent = new ArrayList<String>(cacheSize);
    /**
     * time in ms after which a query is considered as slow
     */
    private int queryTimeThreshold = 50;
    /**
     * Cache of the last queried peptides where the query took long
     */
    private HashMap<String, HashMap<String, ArrayList<Integer>>> lastSlowQueriedPeptidesCache = new HashMap<String, HashMap<String, ArrayList<Integer>>>(cacheSize);
    /**
     * peptide sequences in slow cache
     */
    private ArrayList<String> lastSlowQueriedPeptidesCacheContent = new ArrayList<String>(cacheSize);

    /**
     * Creates a tree based on the proteins present in the sequence factory.
     *
     * @param memoryAllocation the number of GB available for the tree in
     * memory.
     */
    public ProteinTree(int memoryAllocation) throws IOException {

        this.memoryAllocation = memoryAllocation;

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
     * to initiate but slow to query. I typically use 500.
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null but strongly recommended :)
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {
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
     * to initiate but slow to query. I typically use 500.
     * @param enzyme the enzyme used to select peptides. If null all possible
     * peptides will be indexed
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null.
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, Enzyme enzyme, WaitingHandler waitingHandler)
            throws IOException, IllegalArgumentException, InterruptedException, IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        tree.clear();

        componentsFactory = ProteinTreeComponentsFactory.getInstance();

        try {
            boolean needImport;
            try {
                needImport = !componentsFactory.initiate();
                if (!needImport) {
                    componentsFactory.loadParameters();
                    if (componentsFactory.isCorrupted()) {
                        throw new IllegalArgumentException("Database is corrupted. Tree will be reindexed.");
                    }
                    if (!componentsFactory.importComplete()) {
                        throw new IllegalArgumentException("Database import was not successfully completed. Tree will be reindexed.");
                    }
                    if (initialTagSize != componentsFactory.getInitialSize()) {
                        throw new IllegalArgumentException("Different initial size. Tree will be reindexed.");
                    }
                }
            } catch (Exception e) {
                needImport = true;
                componentsFactory.delete();
                componentsFactory.initiate();
            }
            if (needImport) {
                importDb(initialTagSize, maxNodeSize, enzyme, waitingHandler);
            } else {
                componentsFactory.loadProteinLenths();
            }
        } catch (IOException e) {
            componentsFactory.delete();
            throw e;
        } catch (IllegalArgumentException e) {
            componentsFactory.delete();
            throw e;
        } catch (InterruptedException e) {
            componentsFactory.delete();
            throw e;
        } catch (ClassNotFoundException e) {
            componentsFactory.delete();
            throw e;
        } catch (SQLException e) {
            componentsFactory.delete();
            throw e;
        }
    }

    /**
     * Imports the db which is in the sequence factory into the tree and saves
     * it in the nodeFactory
     *
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are slow to query, low initial size are slow to initiate. I typically use
     * 3 for databases containing less than 100 000 proteins.
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 5000.
     * @param enzyme the enzyme used to select peptides. If null all possible
     * peptides will be indexed
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null.
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private void importDb(int initialTagSize, int maxNodeSize, Enzyme enzyme, WaitingHandler waitingHandler)
            throws IOException, IllegalArgumentException, InterruptedException, IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        componentsFactory.saveInitialSize(initialTagSize);

        ArrayList<String> tags = TagFactory.getAminoAcidCombinations(initialTagSize);
        ArrayList<String> accessions;
        if (sequenceFactory.isDefaultReversed()) {
            accessions = new ArrayList<String>();
            for (String accession : sequenceFactory.getAccessions()) {
                if (!sequenceFactory.isDecoyAccession(accession)) {
                    accessions.add(accession);
                }
            }
        } else {
            accessions = sequenceFactory.getAccessions();
        }
        long tagsSize = 500; // The space needed for tags in percent (empirical value)
        long criticalSize = tagsSize * accessions.size();
        // try to estimate the number of tags we can process at a time given the memory settings. We might want to fine tune this
        long capacity = memoryAllocation * cacheScale;
        long estimatedTreeSize = 6 * criticalSize; // as far as I tested, 6% of the proteins are covered by a tag in general (ie median)
        int ratio = (int) (estimatedTreeSize / capacity);
        int nPassages = (int) (ratio);
        if (tags.size() % ratio != 0) {
            nPassages += 1;
        }
        int nTags;
        if (ratio > 0) {
            nTags = tags.size() / ratio;
            if (nTags == 0) {
                nTags = 1;
            }
        } else {
            nTags = tags.size();
        }
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
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            int totalProgress = (int) (nPassages * accessions.size() + tags.size());
            waitingHandler.setMaxSecondaryProgressCounter(totalProgress);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        long time0 = System.currentTimeMillis();

        ArrayList<String> tempTags = new ArrayList<String>(nTags);
        int roundsCpt = 0;
        ArrayList<String> loadedAccessions = new ArrayList<String>();
        for (String tag : tags) {
            if (tempTags.size() == nTags) {
                loadTags(tempTags, accessions, waitingHandler, initialTagSize, maxNodeSize, enzyme, true, loadedAccessions);
                tempTags.clear();
                if (sequenceFactory.getnCache() < accessions.size()) {
                    Collections.reverse(accessions);
                }
                if (debugSpeed) {
                    debugSpeedWriter.write(new Date() + " " + ++roundsCpt + " passages completed");
                    System.out.println(new Date() + " " + roundsCpt + " passages completed");
                    debugSpeedWriter.newLine();
                    debugSpeedWriter.flush();
                }
            } else {
                tempTags.add(tag);
            }
        }
        if (!tempTags.isEmpty()) {
            loadTags(tempTags, accessions, waitingHandler, initialTagSize, maxNodeSize, enzyme, false, loadedAccessions);
            if (debugSpeed) {
                debugSpeedWriter.write(new Date() + " " + ++roundsCpt + " passages completed");
                System.out.println(new Date() + " " + roundsCpt + " passages completed");
                debugSpeedWriter.newLine();
                debugSpeedWriter.flush();
            }
        }

        componentsFactory.setImportComplete(true);

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
     * @param loadedAccessions the accessions already loaded in the factory
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private void loadTags(ArrayList<String> tags, ArrayList<String> accessions, WaitingHandler waitingHandler,
            int initialTagSize, int maxNodeSize, Enzyme enzyme, boolean clearNodes, ArrayList<String> loadedAccessions) throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        //@TODO: would be cool to have this multithreaded
        for (String accession : accessions) {

            String sequence = sequenceFactory.getProtein(accession).getSequence();
            if (!loadedAccessions.contains(accession)) {
                componentsFactory.saveProteinLength(accession, sequence.length());
                loadedAccessions.add(accession);
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
                    }
                    tree.put(tag, node);
                    node.addAccession(accession, tagToIndexesMap.get(tag));
                }
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
                waitingHandler.increaseSecondaryProgressCounter();
            }

        }

        for (String tag : tags) {

            Node node = tree.get(tag);

            if (node != null) {
                node.splitNode(maxNodeSize);
                componentsFactory.saveNode(tag, node);
                if (clearNodes) {
                    tree.remove(tag);
                } else {
                    tagsInTree.add(tag);
                    treeSize += node.getSize();
                }
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
                waitingHandler.increaseSecondaryProgressCounter();
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
    private HashMap<String, ArrayList<Integer>> getTagToIndexesMap(String sequence, ArrayList<String> tags, Enzyme enzyme) throws SQLException, IOException, ClassNotFoundException {

        HashMap<String, ArrayList<Integer>> tagToIndexesMap = new HashMap<String, ArrayList<Integer>>(tags.size());
        Integer initialTagSize = componentsFactory.getInitialSize();

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
    public HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
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
    private HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence, boolean reversed) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        HashMap<String, ArrayList<Integer>> result = lastQueriedPeptidesCache.get(peptideSequence);

        if (result != null) {
            lastQueriedPeptidesCacheContent.remove(peptideSequence);
            lastQueriedPeptidesCacheContent.add(peptideSequence);
        } else {

            result = lastSlowQueriedPeptidesCache.get(peptideSequence);

            if (result != null) {
                lastSlowQueriedPeptidesCacheContent.remove(peptideSequence);
                lastSlowQueriedPeptidesCacheContent.add(peptideSequence);
            } else {

                long timeStart = System.currentTimeMillis();

                int initialTagSize = componentsFactory.getInitialSize();
                if (peptideSequence.length() < initialTagSize) {
                    throw new IllegalArgumentException("Peptide (" + peptideSequence + ") should be at least of length " + initialTagSize + ".");
                }

                result = new HashMap<String, ArrayList<Integer>>();

                String tag = peptideSequence.substring(0, initialTagSize);
                Node node = getNode(tag);

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
                        Integer proteinLength = componentsFactory.getProteinLength(accession);
                        if (proteinLength == null) {
                            throw new IllegalArgumentException("Length of protein " + accession + " not found.");
                        }
                        for (int index : reversedResult.get(accession)) {
                            int reversedIndex = proteinLength - index - peptideLength;
                            if (reversedIndex < 0 || reversedIndex >= proteinLength) {
                                throw new IllegalArgumentException("Wrong index found for peptide " + reversedSequence + " in protein " + reversedAccession + ": " + reversedIndex + ".");
                            }
                            reversedIndexes.add(reversedIndex);
                        }
                        result.put(reversedAccession, reversedIndexes);
                    }
                }

                long timeEnd = System.currentTimeMillis();
                long queryTime = timeEnd - timeStart;

                if (queryTime <= queryTimeThreshold) {
                    lastQueriedPeptidesCache.put(peptideSequence, result);
                    lastQueriedPeptidesCacheContent.add(peptideSequence);
                    if (lastQueriedPeptidesCacheContent.size() > cacheSize) {
                        String key = lastQueriedPeptidesCacheContent.get(0);
                        lastQueriedPeptidesCache.remove(key);
                        lastQueriedPeptidesCacheContent.remove(0);
                    }
                } else {
                    lastSlowQueriedPeptidesCache.put(peptideSequence, result);
                    lastSlowQueriedPeptidesCacheContent.add(peptideSequence);
                    if (lastSlowQueriedPeptidesCacheContent.size() > cacheSize) {
                        String key = lastSlowQueriedPeptidesCacheContent.get(0);
                        lastSlowQueriedPeptidesCache.remove(key);
                        lastSlowQueriedPeptidesCacheContent.remove(0);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a node related to a tag and updates the cache. Null if not found.
     *
     * @param tag the tag of interest
     *
     * @return the corresponding node
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private Node getNode(String tag) throws SQLException, ClassNotFoundException, IOException {
        Node result = tree.get(tag);
        if (result == null) {
            result = componentsFactory.getNode(tag);
            if (result != null) {
                tree.put(tag, result);
                treeSize += result.getSize();
                long capacity = memoryAllocation * cacheScale;
                while (treeSize > capacity && !tagsInTree.isEmpty()) {
                    int index = tagsInTree.size() - 1;
                    String tempTag = tagsInTree.get(index);
                    Node tempNode = tree.get(tempTag);
                    treeSize -= tempNode.getSize();
                    tree.remove(tempTag);
                    tagsInTree.remove(index);
                }
                tagsInTree.add(0, tag);
            }
        }
        return result;
    }

    /**
     * Closes all connections to files.
     */
    public void close() throws IOException, SQLException {
        if (debugSpeed) {
            try {
                debugSpeedWriter.flush();
                debugSpeedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        componentsFactory.close();
    }

    /**
     * Returns the size of the cache used for peptide mappings (note that there are two of them)
     * @return the size of the cache used for peptide mappings
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the size of the cache used for peptide mappings (note that there are two of them)
     * @param cacheSize the size of the cache used for peptide mappings
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
