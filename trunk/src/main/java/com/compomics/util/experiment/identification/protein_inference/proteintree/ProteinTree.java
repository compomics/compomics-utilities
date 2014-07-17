package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidPattern.MatchingType;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.SequenceFactory.ProteinIterator;
import com.compomics.util.experiment.identification.TagFactory;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class sorts the proteins into groups.
 *
 * @author Marc Vaudel
 */
public class ProteinTree {

    /**
     * The memory allocation in MB.
     */
    private int memoryAllocation;
    /**
     * Approximate number of accession*node one can store in a GB of memory
     * (empirical value).
     */
    private static final long cacheScale = 10000;
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
    private ArrayDeque<String> tagsInTree = new ArrayDeque<String>();
    /**
     * The size of the tree in memory in accession*node.
     */
    private long treeSize = 0;
    /**
     * Indicates whether a debug file with speed metrics shall be created.
     */
    private boolean debugSpeed = false;
    /**
     * Indicates whether the number of passages shall be displayed.
     */
    private boolean debugPassages = false;
    /**
     * The writer used to send the output to a debug file.
     */
    private BufferedWriter debugSpeedWriter = null;
    /**
     * The node factory when operating in indexed mode.
     */
    private ProteinTreeComponentsFactory componentsFactory = null;
    /**
     * Size of the cache of the most queried peptides.
     */
    private int cacheSize = 5000;
    /**
     * Indicates whether the cache should be used
     */
    private boolean useCache = false;
    /**
     * Cache of the last queried peptides.
     */
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>> lastQueriedPeptidesCache;
    /**
     * Peptide sequences in cache.
     */
    private ArrayDeque<String> lastQueriedPeptidesCacheContent = new ArrayDeque<String>(cacheSize);
    /**
     * Time in ms after which a query is considered as slow.
     */
    private int queryTimeThreshold = 50;
    /**
     * Cache of the last queried peptides where the query took long.
     */
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>> lastSlowQueriedPeptidesCache;
    /**
     * Peptide sequences in slow cache.
     */
    private ArrayDeque<String> lastSlowQueriedPeptidesCacheContent = new ArrayDeque<String>(cacheSize);
    /**
     * The version of the protein tree.
     */
    public static final String version = "1.0.1";
    /**
     * The matching type of the matches in cache.
     */
    private MatchingType matchingTypeInCache = MatchingType.indistiguishibleAminoAcids;
    /**
     * The mass tolerance of the matches in cache.
     */
    private Double massToleranceInCache = null;
    /**
     * Indicates whether the main thread is listening or preparing to wait.
     */
    private boolean listening = true;
    /**
     * The number of proteins which should be imported at a time.
     */
    public static final int proteinBatchSize = 100;
    /**
     * Cache for the protein lengths.
     */
    private HashMap<String, Integer> proteinLengthsCache = new HashMap<String, Integer>();

    /**
     * Creates a tree based on the proteins present in the sequence factory.
     *
     * @param memoryAllocation the number of MB available for the tree in
     * memory.
     * @param cacheSize the peptide queries caches size (note, there are two of
     * them)
     *
     * @throws IOException
     */
    public ProteinTree(int memoryAllocation, int cacheSize) throws IOException {

        this.memoryAllocation = memoryAllocation;
        this.cacheSize = cacheSize;
        lastSlowQueriedPeptidesCache = new HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>>(cacheSize);
        lastQueriedPeptidesCache = new HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>>(cacheSize);

        if (debugSpeed) {
            try {
                debugSpeedWriter = new BufferedWriter(new FileWriter(new File("treeSpeed.txt")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the memory allocation.
     *
     * @return the memory allocation
     */
    public int getMemoryAllocation() {
        return memoryAllocation;
    }

    /**
     * Sets the memory allocation.
     *
     * @param memoryAllocation the memory allocation
     */
    public void setMemoryAllocation(int memoryAllocation) {
        this.memoryAllocation = memoryAllocation;
    }

    /**
     * Initiates the tree.
     *
     * @param initialTagSize the initial tag size
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 500 giving an approximate
     * query time <20ms.
     * @param maxPeptideSize the maximum peptide size
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null but strongly recommended :)
     * @param printExpectedImportTime if true the expected import time will be
     * printed to the waiting handler
     * @param displayProgress display progress
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, boolean printExpectedImportTime, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {
        initiateTree(initialTagSize, maxNodeSize, maxPeptideSize, null, waitingHandler, printExpectedImportTime, displayProgress);
    }

    /**
     * Initiates the tree. Note: speed and memory are calibrated for the no
     * enzyme case.
     *
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are fast to query, low initial size are fast to initiate. I typically use
     * 3 for databases containing less than 100 000 proteins giving an
     * approximate initiation time of 60ms per accession.
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 500 giving an approximate
     * query time <20ms.
     * @param maxPeptideSize the maximum peptide size
     * @param enzyme the enzyme used to select peptides. If null all possible
     * peptides will be indexed
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null.
     * @param printExpectedImportTime if true the expected import time will be
     * printed to the waiting handler
     * @param displayProgress display progress
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, WaitingHandler waitingHandler, boolean printExpectedImportTime, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        // delete outdated trees
        try {
            ProteinTreeComponentsFactory.deletOutdatedTrees();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    String tempVersion = componentsFactory.getVersion();
                    if (tempVersion == null || !tempVersion.equals(version)) {
                        throw new IllegalArgumentException("Database version " + tempVersion + " obsolete. Tree will be reindexed.");
                    }
                    if (initialTagSize != componentsFactory.getInitialSize()) {
                        throw new IllegalArgumentException("Different initial size. Tree will be reindexed.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                needImport = true;
                componentsFactory.delete();
                componentsFactory.initiate();
            }

            if (needImport) {
                importDb(initialTagSize, maxNodeSize, maxPeptideSize, enzyme, waitingHandler, printExpectedImportTime, displayProgress);
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

        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return;
        }

        try {
            componentsFactory.loadTags();
        } catch (Exception e) {
            // ignore, tree will just be slower
            if (waitingHandler == null || !waitingHandler.isRunCanceled()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Try to delete the current database.
     *
     * @return true of the deletion was a success
     */
    public boolean deleteDb() {
        try {
            return componentsFactory.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Imports the db which is in the sequence factory into the tree and saves
     * it in the nodeFactory.
     *
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are slow to query, low initial size are slow to initiate. I typically use
     * 3 for databases containing less than 100 000 proteins.
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 5000.
     * @param maxPeptideSize the maximum peptide size
     * @param enzyme the enzyme used to select peptides. If null all possible
     * peptides will be indexed
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null.
     * @param printExpectedImportTime if true the expected import time will be
     * printed to the waiting handler
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private void importDb(int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, WaitingHandler waitingHandler, boolean printExpectedImportTime, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        if (printExpectedImportTime) {
            int nSeconds = getExpectedImportTime();
            String report = "Expected import time: ";

            if (nSeconds < 120) {
                report += nSeconds + " seconds.";
            } else {
                int nMinutes = nSeconds / 60;
                if (nMinutes < 120) {
                    report += nMinutes + " minutes.";
                } else {
                    int nHours = nMinutes / 60;
                    report += nHours + " hours.";
                }
            }

            if (waitingHandler != null && waitingHandler.isReport()) {
                waitingHandler.appendReport(report, true, true);
                waitingHandler.appendReport("    See http://code.google.com/p/compomics-utilities/wiki/ProteinInference.", true, true);
            } else {
                System.out.println(report);
                System.out.println("    See http://code.google.com/p/compomics-utilities/wiki/ProteinInference.");
            }
        }

        componentsFactory.saveInitialSize(initialTagSize);

        ArrayList<String> tags = TagFactory.getAminoAcidCombinations(initialTagSize);

        int nAccessions;
        if (sequenceFactory.isDefaultReversed()) {
            nAccessions = sequenceFactory.getNTargetSequences();
        } else {
            nAccessions = sequenceFactory.getNSequences();
        }

        long tagsSize = 500; // The space needed for tags in percent (empirical value)
        long criticalSize = tagsSize * nAccessions;

        // try to estimate the number of tags we can process at a time given the memory settings. We might want to fine tune this
        long capacity = memoryAllocation * cacheScale;
        long estimatedTreeSize = 6 * criticalSize; // as far as I tested, 6% of the proteins are covered by a tag in general (ie median)
        int ratio = (int) (estimatedTreeSize / capacity);

        if (ratio == 0) {
            ratio = 1;
        }

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
//            Collections.shuffle(tags);
        }

        if (debugPassages) {
            System.out.println("Estimated tree size: " + estimatedTreeSize);
            System.out.println(new Date() + " " + nPassages + " passages needed (" + nTags + " tags of " + tags.size() + " per passage)");
        }

        if (debugSpeed) {
            debugSpeedWriter.write("Critical size: " + criticalSize);
            System.out.println("Critical size: " + criticalSize);
            estimatedTreeSize = estimatedTreeSize / 100;
            debugSpeedWriter.write("Estimated tree size: " + estimatedTreeSize);
            debugSpeedWriter.write(new Date() + " " + nPassages + " passages needed (" + nTags + " tags of " + tags.size() + " per passage)");
            debugSpeedWriter.newLine();
            debugSpeedWriter.flush();
        }

        if (waitingHandler != null && displayProgress && !waitingHandler.isRunCanceled()) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            int totalProgress = (int) (nPassages * nAccessions + tags.size() * 2);
            waitingHandler.setMaxSecondaryProgressCounter(totalProgress);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return;
        }

        long time0 = System.currentTimeMillis();

        ArrayList<String> tempTags = new ArrayList<String>(nTags);
        int tagsLoaded = 0;
        boolean first = true;

        for (String tag : tags) {
            if (tempTags.size() == nTags) {
                loadTags(tempTags, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, first, waitingHandler, displayProgress);
                if (first) {
                    first = false;
                }
                tagsLoaded += tempTags.size();
                tempTags.clear();
                if (debugSpeed) {
                    debugSpeedWriter.write(new Date() + " " + tagsLoaded + " tags of " + tags.size() + " loaded.");
                    System.out.println(new Date() + " " + tagsLoaded + " tags of " + tags.size() + " loaded.");
                    debugSpeedWriter.newLine();
                    debugSpeedWriter.flush();
                }
            }
            tempTags.add(tag);

            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                return;
            }
        }

        if (!tempTags.isEmpty()) {
            loadTags(tempTags, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, first, waitingHandler, displayProgress);

            if (debugSpeed) {
                debugSpeedWriter.write(new Date() + " " + tagsLoaded + " tags of " + tags.size() + " loaded.");
                System.out.println(new Date() + " " + tagsLoaded + " tags of " + tags.size() + " loaded.");
                debugSpeedWriter.newLine();
                debugSpeedWriter.flush();
            }
        }

        tagsInTree.addAll(tree.keySet());
        for (Node node : tree.values()) {
            treeSize += node.getSize();
        }

        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return;
        }

        componentsFactory.setVersion(version);
        componentsFactory.setFastaFilePath(sequenceFactory.getCurrentFastaFile().getAbsolutePath());
        componentsFactory.setImportComplete(true);

        long time1 = System.currentTimeMillis();
        long initiationTime = time1 - time0;

        if (sequenceFactory.getNSequences() > 1000) {
            UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
            utilitiesUserPreferences.addProteinTreeImportTime(sequenceFactory.getCurrentFastaFile().length(), initiationTime);
            UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);
        }

        if (debugSpeed) {
            debugSpeedWriter.write("tree initiation: " + initiationTime + " ms.");
            System.out.println("tree initiation: " + initiationTime + " ms.");
            debugSpeedWriter.newLine();
            debugSpeedWriter.flush();
        }
    }

    /**
     * Estimates the import time for the database in the sequence factory.
     *
     * @return the import time in seconds
     */
    private int getExpectedImportTime() {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        HashMap<Long, ArrayList<Long>> importTimeMap = utilitiesUserPreferences.getProteinTreeImportTime();

        if (importTimeMap.isEmpty()) {
            return sequenceFactory.getNTargetSequences() * 16 / 1000;
        } else {

            ArrayList<Double> ratios = new ArrayList<Double>();

            for (Long size : importTimeMap.keySet()) {
                for (Long time : importTimeMap.get(size)) {
                    double ratio = (double) (size / time);
                    ratios.add(ratio);
                }
            }

            double ratio = BasicMathFunctions.percentile(ratios, 0.05);
            return (int) (1.2 * sequenceFactory.getCurrentFastaFile().length() / (1000 * ratio));
        }
    }

    /**
     * Loads the tags found in the given proteins in the tree and saves the end
     * nodes in the NodeFactory if not null.
     *
     * @param tags the tags of interest
     * @param waitingHandler waiting handler displaying progress to the user -
     * can be null
     * @param enzyme the enzyme restriction
     * @param loadLengths boolean indicating whether protein lengths should be
     * loaded in the db
     * @param loadedLengths the accessions of the proteins from which the length
     * is already saved
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private synchronized void loadTags(ArrayList<String> tags,
            int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, boolean loadLengths, WaitingHandler waitingHandler, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        // find the tags in the proteins and create a node per tag found
        indexProteinsSingleThread(tags, initialTagSize, enzyme, waitingHandler, displayProgress);

        // split the nodes and save them in the db
        processRawNodesSingleThread(tags, maxNodeSize, maxPeptideSize, waitingHandler, displayProgress);

        // clear memory before further processing
        tree.clear();
        System.gc();
    }

    /**
     * Iterates all the proteins and indexes the given tags in their sequences
     * by batches of proteinBatchSize using a SequenceIndexer in a separate
     * thread. When sequence indexers are finished, a node per tag is created
     * and stored in the tree map.
     *
     * @param tags the tags to index
     * @param waitingHandler waiting handler providing feedback on the process
     * and allowing canceling the process
     * @param initialTagSize the initial tag size
     * @param enzyme enzyme to use. Can be null
     * @param loadLengths boolean indicating whether protein lengths should be
     * loaded in the db
     * @param displayProgress boolean indicating whether progress shall be
     * displayed using the waiting handler
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void indexProteinsSingleThread(ArrayList<String> tags,
            int initialTagSize, Enzyme enzyme, WaitingHandler waitingHandler, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(sequenceFactory.isDefaultReversed());

        while (proteinIterator.hasNext()) {
            Protein protein = proteinIterator.getNextProtein();
            String accession = protein.getAccession();

            if (protein.getLength() > 0) { // ignore empty protein sequences

                HashMap<String, ArrayList<Integer>> indexesMap = getTagToIndexesMap(protein.getSequence(), tags, enzyme, waitingHandler);

                for (String tag : indexesMap.keySet()) {
                    ArrayList<Integer> indexes = indexesMap.get(tag);
                    if (!indexes.isEmpty()) {
                        Node node = tree.get(tag);
                        if (node == null) {
                            node = new Node(initialTagSize);
                            tree.put(tag, node);
                        }
                        node.addAccession(accession, indexes);
                    }

                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        break;
                    }
                }

                if (displayProgress && waitingHandler != null) {
                    waitingHandler.increaseSecondaryProgressCounter();
                }

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    tree.clear();
                    return;
                }
            }
        }
    }

    /**
     * Iterates all the proteins and indexes the given tags in their sequences
     * by batches of proteinBatchSize using a SequenceIndexer in a separate
     * thread. When sequence indexers are finished, a node per tag is created
     * and stored in the tree map.
     *
     * @param tags the tags to index
     * @param waitingHandler waiting handler providing feedback on the process
     * and allowing canceling the process
     * @param initialTagSize the initial tag size
     * @param enzyme enzyme to use. Can be null
     * @param loadLengths boolean indicating whether protein lengths should be
     * loaded in the db
     * @param displayProgress boolean indicating whether progress shall be
     * displayed using the waiting handler
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void indexProteins(ArrayList<String> tags,
            int initialTagSize, Enzyme enzyme, WaitingHandler waitingHandler, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        int nThreads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        ArrayList<Protein> sequenceBuffer = new ArrayList<Protein>(proteinBatchSize);
        ArrayList<SequenceIndexer> sequenceIndexers = new ArrayList<SequenceIndexer>(nThreads);
        int nAccessions;
        if (sequenceFactory.isDefaultReversed()) {
            nAccessions = sequenceFactory.getNTargetSequences();
        } else {
            nAccessions = sequenceFactory.getNSequences();
        }

        ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(sequenceFactory.isDefaultReversed());

        while (proteinIterator.hasNext()) {
            Protein protein = proteinIterator.getNextProtein();
            sequenceBuffer.add(protein);
            if (sequenceBuffer.size() == proteinBatchSize) {
                while (sequenceIndexers.size() == nThreads) {
                    processFinishedIndexers(sequenceIndexers, initialTagSize);
                }
                SequenceIndexer sequenceIndexer = new SequenceIndexer(sequenceBuffer, tags, enzyme, waitingHandler, displayProgress);
                new Thread(sequenceIndexer, "sequence indexing").start();
                sequenceBuffer = new ArrayList<Protein>(proteinBatchSize);
                sequenceIndexers.add(sequenceIndexer);
            }
            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled() || waitingHandler.isRunFinished()) {
                    emptyCache();
                    return;
                }
            }
        }

        if (!sequenceBuffer.isEmpty()) {
            SequenceIndexer sequenceIndexer = new SequenceIndexer(sequenceBuffer, tags, enzyme, waitingHandler, displayProgress);
            new Thread(sequenceIndexer, "sequence indexing").start();
            sequenceIndexers.add(sequenceIndexer);
        }

        while (!sequenceIndexers.isEmpty()) {
            processFinishedIndexers(sequenceIndexers, initialTagSize);
        }
    }

    /**
     * Splits the raw nodes and saves them in the database.
     *
     * @param tags the tags indexed
     * @param maxNodeSize the maximal size allowed for a node
     * @param maxPeptideSize the maximal peptide length allowed
     * @param waitingHandler waiting handler providing feedback on the process
     * and allowing canceling the process
     * @param displayProgress boolean indicating whether progress shall be
     * displayed using the waiting handler
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void processRawNodesSingleThread(ArrayList<String> tags, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        int batchSize = (int) Math.ceil(tree.size() / 3);
        batchSize = Math.min(10000, batchSize);
        batchSize = Math.max(1000, batchSize);

        HashMap<String, Object> splittedNodes = new HashMap<String, Object>(batchSize);

        for (String tag : tags) {

            Node node = tree.get(tag);

            if (node != null) {

                node.splitNode(maxNodeSize, maxPeptideSize);
                splittedNodes.put(tag, node);

                if (splittedNodes.size() == batchSize) {
                    componentsFactory.saveNodes(splittedNodes, waitingHandler);
                    splittedNodes.clear();
                }

            }
            if (waitingHandler != null) {
                if (displayProgress) {
                    waitingHandler.increaseSecondaryProgressCounter();
                    if (node == null) {
                        waitingHandler.increaseSecondaryProgressCounter();
                    }
                }
                if (waitingHandler.isRunCanceled() || waitingHandler.isRunFinished()) {
                    emptyCache();
                    return;
                }
            }
        }

        if (!splittedNodes.isEmpty()) {
            componentsFactory.saveNodes(splittedNodes, waitingHandler);
            splittedNodes.clear();
        }
    }

    /**
     * Splits the raw nodes and saves them in the database
     *
     * @param maxNodeSize the maximal size allowed for a node
     * @param maxPeptideSize the maximal peptide length allowed
     * @param waitingHandler waiting handler providing feedback on the process
     * and allowing canceling the process
     * @param displayProgress boolean indicating whether progress shall be
     * displayed using the waiting handler
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void processRawNodes(int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, boolean displayProgress)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        int nThreads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        ArrayList<NodeSplitter> nodeSplitters = new ArrayList<NodeSplitter>(nThreads);

        for (String tag : tree.keySet()) {

            Node node = tree.get(tag);

            while (nodeSplitters.size() == nThreads) {
                processFinishedNodeSplitters(nodeSplitters, null); // @TODO: add waiting handler
            }

            NodeSplitter nodeSplitter = new NodeSplitter(tag, node, maxNodeSize, maxPeptideSize, waitingHandler, displayProgress);
            new Thread(nodeSplitter, "Node splitting of tag " + tag).start();
            nodeSplitters.add(nodeSplitter);

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled() || waitingHandler.isRunFinished()) {
                    emptyCache();
                    return;
                }
            }
        }

        while (!nodeSplitters.isEmpty()) {
            processFinishedNodeSplitters(nodeSplitters, null); // @TODO: add waiting handler
        }
    }

    /**
     * Clears the finished raw node splitters from a given list or wait for one
     * to finish and batch saves the splitted nodes.
     *
     * @param nodeProcessors the node processors of interest
     * @param waitingHandler the waiting handler
     *
     * @throws InterruptedException
     */
    private synchronized void processFinishedNodeSplitters(ArrayList<NodeSplitter> nodeSplitters, WaitingHandler waitingHandler) throws InterruptedException, SQLException, IOException {

        listening = false;
        ArrayList<NodeSplitter> done = new ArrayList<NodeSplitter>();

        for (NodeSplitter nodeSplitter : nodeSplitters) {
            if (nodeSplitter.isFinished()) {
                done.add(nodeSplitter);
            }
        }

        if (done.isEmpty()) {
            listening = true;
            wait();
            for (NodeSplitter nodeSplitter : nodeSplitters) {
                if (nodeSplitter.isFinished()) {
                    done.add(nodeSplitter);
                }
            }
        }

        listening = true;
        HashMap<String, Object> splittedNodes = new HashMap<String, Object>(done.size());

        for (NodeSplitter nodeSplitter : done) {
            splittedNodes.put(nodeSplitter.getTag(), nodeSplitter.getNode());
            nodeSplitter.clear();
        }

        componentsFactory.saveNodes(splittedNodes, waitingHandler);
        nodeSplitters.removeAll(done);
    }

    /**
     * Stores the result of the finished indexers and updates the list. Waits if
     * none is finished.
     *
     * @param sequenceIndexers the sequence indexers
     * @param initialTagSize the initial tag size
     *
     * @throws InterruptedException
     */
    private synchronized void processFinishedIndexers(ArrayList<SequenceIndexer> sequenceIndexers, int initialTagSize) throws InterruptedException {

        listening = false;
        ArrayList<SequenceIndexer> done = new ArrayList<SequenceIndexer>();

        for (SequenceIndexer sequenceIndexer : sequenceIndexers) {
            if (sequenceIndexer.isFinished()) {
                done.add(sequenceIndexer);
            }
        }

        if (done.isEmpty()) {
            listening = true;
            wait();
            for (SequenceIndexer sequenceIndexer : sequenceIndexers) {
                if (sequenceIndexer.isFinished()) {
                    done.add(sequenceIndexer);
                }
            }
        }

        listening = true;

        for (SequenceIndexer sequenceIndexer : done) {
            HashMap<String, HashMap<String, ArrayList<Integer>>> tagToIndexesMap = sequenceIndexer.getIndexes();
            for (String accession : tagToIndexesMap.keySet()) {
                for (String tag : tagToIndexesMap.get(accession).keySet()) {
                    ArrayList<Integer> indexes = tagToIndexesMap.get(accession).get(tag);
                    if (!indexes.isEmpty()) {
                        Node node = tree.get(tag);
                        if (node == null) {
                            node = new Node(initialTagSize);
                            tree.put(tag, node);
                        }
                        node.addAccession(accession, indexes);
                    }
                }
            }
            sequenceIndexer.clear();
        }

        sequenceIndexers.removeAll(done);
    }

    /**
     * Returns the protein mapping in the sequence factory for the given peptide
     * sequence based on string matching only.
     *
     * @param peptideSequence the peptide sequence
     *
     * @return the peptide to protein mapping: Accession -> list of indexes
     * where the peptide can be found on the sequence. An empty map if not
     * found.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public HashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        HashMap<String, HashMap<String, ArrayList<Integer>>> mapping = getProteinMapping(peptideSequence, MatchingType.string, null, false);

        if (mapping.size() > 1) {
            throw new IllegalArgumentException("Different mappings found for peptide " + peptideSequence + " in string matching. Only one expected.");
        }

        HashMap<String, ArrayList<Integer>> result = mapping.get(peptideSequence);

        if (result != null) {
            return result;
        }

        return new HashMap<String, ArrayList<Integer>>();
    }

    /**
     * Returns the protein mapping in the sequence factory for the given peptide
     * sequence.
     *
     * @param peptideSequence the peptide sequence
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     * @param limitXs if true the share of Xs in the peptide sequences will be
     * limited in order to increase sequencing speed.
     *
     * @return the peptide to protein mapping: peptide sequence -> protein
     * accession -> index in the protein An empty map if not
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence, MatchingType matchingType, Double massTolerance, boolean limitXs) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        long time0 = 0;
        if (debugSpeed) {
            time0 = System.currentTimeMillis();
        }

        HashMap<String, HashMap<String, ArrayList<Integer>>> result = getProteinMapping(peptideSequence, matchingType, massTolerance, limitXs, false);

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
     * sequence. peptide sequence -> protein accession -> index in the protein.
     * An empty map if not.
     *
     * @param peptideSequence the peptide sequence
     * @param reversed boolean indicating whether we are looking at a reversed
     * peptide sequence
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     * @param limitXs if true the share of Xs in the peptide sequences will be
     * limited in order to increase sequencing speed.
     *
     * @return the peptide to protein mapping: Accession -> list of indexes
     * where the peptide can be found on the sequence
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence, MatchingType matchingType, Double massTolerance, boolean limitXs, boolean reversed) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        if (matchingType != matchingTypeInCache || matchingType == MatchingType.indistiguishibleAminoAcids && (massToleranceInCache == null || !massToleranceInCache.equals(massTolerance))) {
            //@TODO adapt the cache to the different matching types
            emptyCache();
            matchingTypeInCache = matchingType;
            massToleranceInCache = massTolerance;
        }

        HashMap<String, HashMap<String, ArrayList<Integer>>> result = null;
        if (useCache) {
        result = lastQueriedPeptidesCache.get(peptideSequence);
        }

        if (result == null) {
            if (useCache) {
            result = lastSlowQueriedPeptidesCache.get(peptideSequence);
            }
            if (result == null) {
                if (sequenceFactory.isDefaultReversed() && useCache) {
                    String reversedSequence = SequenceFactory.reverseSequence(peptideSequence);
                    result = lastQueriedPeptidesCache.get(reversedSequence);
                    if (result == null) {
                        result = lastSlowQueriedPeptidesCache.get(reversedSequence);
                    }
                    if (result != null) {
                        return getReversedResults(result);
                    }
                }

                long timeStart = System.currentTimeMillis();

                int initialTagSize = componentsFactory.getInitialSize();
                if (peptideSequence.length() < initialTagSize) {
                    throw new IllegalArgumentException("Peptide (" + peptideSequence + ") should be at least of length " + initialTagSize + ".");
                }

                result = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

                AminoAcidPattern peptidePattern = new AminoAcidPattern(peptideSequence);
                ArrayList<String> initialTags = getInitialTags(peptidePattern, matchingType, massTolerance);

                for (String tag : initialTags) {
                    Node node = getNode(tag);
                    if (node != null) {
                        HashMap<String, HashMap<String, ArrayList<Integer>>> tagResults = node.getProteinMapping(peptidePattern, tag, matchingType, massTolerance, limitXs);
                        for (String tagSequence : tagResults.keySet()) {
                            HashMap<String, ArrayList<Integer>> mapping = result.get(tagSequence);
                            HashMap<String, ArrayList<Integer>> tagMapping = tagResults.get(tagSequence);
                            if (mapping == null && !tagMapping.isEmpty()) {
                                result.put(tagSequence, tagMapping);
                            } else {
                                for (String tagAccession : tagMapping.keySet()) {
                                    ArrayList<Integer> indexes = mapping.get(tagAccession);
                                    ArrayList<Integer> tagIndexes = tagMapping.get(tagAccession);
                                    if (indexes == null) {
                                        mapping.put(tagAccession, tagIndexes);
                                    } else {
                                        for (int newIndex : tagIndexes) {
                                            if (!indexes.contains(newIndex)) {
                                                indexes.add(newIndex);
                                            }
                                        }
                                        Collections.sort(indexes);
                                    }
                                }
                            }
                        }
                    }
                }

                if (sequenceFactory.isDefaultReversed() && !reversed) {
                    String reversedSequence = SequenceFactory.reverseSequence(peptideSequence);
                    HashMap<String, HashMap<String, ArrayList<Integer>>> reversedResult;
                    if (!reversedSequence.equals(peptideSequence)) {
                        reversedResult = getProteinMapping(reversedSequence, matchingType, massTolerance, limitXs, true);
                        reversedResult = getReversedResults(reversedResult);
                    } else {
                        reversedResult = getReversedResults(result);
                    }
                    for (String tempReversedSequence : reversedResult.keySet()) {
                        HashMap<String, ArrayList<Integer>> mapping = result.get(tempReversedSequence);
                        if (mapping != null) {
                            mapping.putAll(reversedResult.get(tempReversedSequence));
                        } else {
                            result.put(tempReversedSequence, reversedResult.get(tempReversedSequence));
                        }
                    }
                }

                if (!reversed && useCache) {
                    long timeEnd = System.currentTimeMillis();
                    long queryTime = timeEnd - timeStart;

                    if (queryTime <= queryTimeThreshold) {
                        lastQueriedPeptidesCache.put(peptideSequence, result);
                        lastQueriedPeptidesCacheContent.add(peptideSequence);
                        if (lastQueriedPeptidesCacheContent.size() > cacheSize) {
                            String key = lastQueriedPeptidesCacheContent.pollLast();
                            lastQueriedPeptidesCache.remove(key);
                        }
                    } else {
                        lastSlowQueriedPeptidesCache.put(peptideSequence, result);
                        lastSlowQueriedPeptidesCacheContent.add(peptideSequence);
                        if (lastSlowQueriedPeptidesCacheContent.size() > cacheSize) {
                            String key = lastSlowQueriedPeptidesCacheContent.pollLast();
                            lastSlowQueriedPeptidesCache.remove(key);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the protein mappings for the given peptide sequence. peptide
     * sequence -> protein accession -> index in the protein. An empty map if
     * not found.
     *
     * @param tag the tag to look for in the tree. Must contain a consecutive
     * amino acid sequence of longer or equal size than the initialTagSize of
     * the tree.
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     * @param fixedModifications the fixed modifications to consider
     * @param variableModifications the variable modifications to consider
     * @param limitXs if true the share of Xs in the peptide sequences will be
     * limited in order to increase sequencing speed.
     * @param reportFixedPtms a boolean indicating whether fixed PTMs should be
     * reported in the Peptide object
     *
     * @return the protein mapping for the given peptide sequence
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public HashMap<Peptide, HashMap<String, ArrayList<Integer>>> getProteinMapping(Tag tag, AminoAcidPattern.MatchingType matchingType,
            Double massTolerance, ArrayList<String> fixedModifications, ArrayList<String> variableModifications, boolean limitXs,
            boolean reportFixedPtms) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        int initialTagSize = componentsFactory.getInitialSize();
        AminoAcidPattern longestAminoAcidPattern = new AminoAcidPattern();
        int componentIndex = -1;
        for (int i = 0; i < tag.getContent().size(); i++) {
            TagComponent tagComponent = tag.getContent().get(i);
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                if (aminoAcidPattern.length() >= initialTagSize && aminoAcidPattern.length() > longestAminoAcidPattern.length()) {
                    componentIndex = i;
                    longestAminoAcidPattern = aminoAcidPattern;
                }
            }
        }
        if (componentIndex == -1) {
            throw new IllegalArgumentException("No amino acid sequence longer than " + initialTagSize + " was found for tag " + tag + ".");
        }
        HashMap<String, HashMap<String, ArrayList<Integer>>> seeds = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
        for (String peptideSequence : longestAminoAcidPattern.getAllPossibleSequences()) {
            double xShare = ((double) Util.getOccurrence(peptideSequence, 'X')) / peptideSequence.length();
            if (!limitXs || xShare <= ProteinMatch.maxX) {
                seeds.putAll(getProteinMapping(peptideSequence, matchingType, massTolerance, limitXs));
            }
        }
        HashMap<Peptide, HashMap<String, ArrayList<Integer>>> results = new HashMap<Peptide, HashMap<String, ArrayList<Integer>>>();
        for (String tagSeed : seeds.keySet()) {
            double xShare = ((double) Util.getOccurrence(tagSeed, 'X')) / tagSeed.length();
            if (!limitXs || xShare <= ProteinMatch.maxX) {
                for (String accession : seeds.get(tagSeed).keySet()) {
                    String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
                    for (int seedIndex : seeds.get(tagSeed).get(accession)) {
                        HashMap<Integer, ArrayList<Peptide>> matches = tag.getPeptideMatches(proteinSequence, seedIndex, componentIndex, matchingType, massTolerance, fixedModifications, variableModifications, reportFixedPtms);
                        for (int aa : matches.keySet()) {
                            for (Peptide peptide : matches.get(aa)) {
                                HashMap<String, ArrayList<Integer>> proteinToIndexMap = results.get(peptide);
                                if (proteinToIndexMap == null) {
                                    proteinToIndexMap = new HashMap<String, ArrayList<Integer>>();
                                    results.put(peptide, proteinToIndexMap);
                                }
                                ArrayList<Integer> peptideIndexes = proteinToIndexMap.get(accession);
                                if (peptideIndexes == null) {
                                    peptideIndexes = new ArrayList<Integer>();
                                    proteinToIndexMap.put(accession, peptideIndexes);
                                }
                                peptideIndexes.add(aa);
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Batch loads the nodes needed for a peptide mapping.
     *
     * @param peptideSequence the sequence of the peptide
     * @param matchingType the matching type
     * @param massTolerance the ms2 mass tolerance
     *
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private void batchLoadNodes(String peptideSequence, MatchingType matchingType, Double massTolerance) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        AminoAcidPattern aminoAcidPattern = new AminoAcidPattern(peptideSequence);
        ArrayList<String> tags = getInitialTags(aminoAcidPattern, matchingType, massTolerance);
        String reversedSequence = SequenceFactory.reverseSequence(peptideSequence);
        aminoAcidPattern = new AminoAcidPattern(reversedSequence);
        tags.addAll(getInitialTags(aminoAcidPattern, matchingType, massTolerance));
        ArrayList<String> toLoad = new ArrayList<String>();

        for (String tag : tags) {
            Node result = tree.get(tag);
            if (result == null) {
                toLoad.add(tag);
            }
        }

        componentsFactory.loadNodes(toLoad);
    }

    /**
     * Returns a list of possible initial tags.
     *
     * @param peptideSequence the peptide sequence
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise.
     *
     * @returna list of possible initial tags.
     *
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ArrayList<String> getInitialTags(AminoAcidPattern aminoAcidPattern, MatchingType matchingType, Double massTolerance) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        int initialTagSize = componentsFactory.getInitialSize();
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < initialTagSize; i++) {
            for (AminoAcid aminoAcid : aminoAcidPattern.getTargetedAA(i)) {
                if (result.isEmpty()) {
                    if (matchingType == MatchingType.string) {
                        result.add(aminoAcid.singleLetterCode);
                    } else {
                        for (char newAa : aminoAcid.getSubAminoAcids()) {
                            if (!aminoAcidPattern.getExcludedAA(i).contains(AminoAcid.getAminoAcid(newAa))) {
                                String newTag = String.valueOf(newAa);
                                if (!result.contains(newTag)) {
                                    result.add(newTag);
                                }
                            }
                        }
                        for (char newAa : aminoAcid.getCombinations()) {
                            if (!aminoAcidPattern.getExcludedAA(i).contains(AminoAcid.getAminoAcid(newAa))) {
                                String newTag = String.valueOf(newAa);
                                if (!result.contains(newTag)) {
                                    result.add(newTag);
                                }
                            }
                        }
                        if (matchingType == MatchingType.indistiguishibleAminoAcids) {
                            for (char newAa : aminoAcid.getIndistinguishibleAminoAcids(massTolerance)) {
                                if (!aminoAcidPattern.getExcludedAA(i).contains(AminoAcid.getAminoAcid(newAa))) {
                                    String newTag = String.valueOf(newAa);
                                    if (!result.contains(newTag)) {
                                        result.add(newTag);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ArrayList<String> newResults = new ArrayList<String>();
                    for (String sequence : result) {
                        if (matchingType == MatchingType.string) {
                            newResults.add(sequence + aminoAcid.singleLetterCode);
                        } else {
                            for (char newAa : aminoAcid.getSubAminoAcids()) {
                                if (!aminoAcidPattern.getExcludedAA(i).contains(AminoAcid.getAminoAcid(newAa))) {
                                    String newTag = sequence + newAa;
                                    if (!newResults.contains(newTag)) {
                                        newResults.add(newTag);
                                    }
                                }
                            }
                            for (char newAa : aminoAcid.getCombinations()) {
                                if (!aminoAcidPattern.getExcludedAA(i).contains(AminoAcid.getAminoAcid(newAa))) {
                                    String newTag = sequence + newAa;
                                    if (!newResults.contains(newTag)) {
                                        newResults.add(newTag);
                                    }
                                }
                            }
                            if (matchingType == MatchingType.indistiguishibleAminoAcids) {
                                for (char newAa : aminoAcid.getIndistinguishibleAminoAcids(massTolerance)) {
                                    if (!aminoAcidPattern.getExcludedAA(i).contains(AminoAcid.getAminoAcid(newAa))) {
                                        String newTag = sequence + newAa;
                                        if (!newResults.contains(newTag)) {
                                            newResults.add(newTag);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    result = newResults;
                }
            }
        }
        return result;
    }

    /**
     * Reverts the indexes and the protein accessions of the given mapping.
     *
     * @param forwardResults the given mapping
     * @param peptideSequence the sequence of interest
     * @return the reversed indexes
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private HashMap<String, HashMap<String, ArrayList<Integer>>> getReversedResults(HashMap<String, HashMap<String, ArrayList<Integer>>> forwardResults) throws SQLException, ClassNotFoundException, IOException, InterruptedException {

        HashMap<String, HashMap<String, ArrayList<Integer>>> results = new HashMap<String, HashMap<String, ArrayList<Integer>>>(forwardResults.keySet().size());

        for (String sequence : forwardResults.keySet()) {

            int peptideLength = sequence.length();
            String reversedSequence = SequenceFactory.reverseSequence(sequence);
            HashMap<String, ArrayList<Integer>> mapping = new HashMap<String, ArrayList<Integer>>(forwardResults.get(sequence).size());

            for (String accession : forwardResults.get(sequence).keySet()) {

                String newAccession;
                Integer proteinLength;

                if (accession.endsWith(SequenceFactory.getDefaultDecoyAccessionSuffix())) {
                    newAccession = SequenceFactory.getDefaultTargetAccession(accession);
                    proteinLength = getProteinLength(newAccession);
                    if (proteinLength == null) {
                        throw new IllegalArgumentException("Length of protein " + newAccession + " not found.");
                    }
                } else {
                    newAccession = SequenceFactory.getDefaultDecoyAccession(accession);
                    proteinLength = getProteinLength(accession);
                    if (proteinLength == null) {
                        throw new IllegalArgumentException("Length of protein " + accession + " not found.");
                    }
                }

                ArrayList<Integer> forwardIndexes = forwardResults.get(sequence).get(accession);
                ArrayList<Integer> reversedIndexes = new ArrayList<Integer>(forwardIndexes.size());

                for (int index : forwardIndexes) {
                    int reversedIndex = proteinLength - index - peptideLength;
                    if (reversedIndex < 0 || reversedIndex >= proteinLength) {
                        throw new IllegalArgumentException("Wrong index found for peptide " + reversedSequence + " in protein " + newAccession + ": " + reversedIndex + ".");
                    }
                    reversedIndexes.add(reversedIndex);
                }

                mapping.put(newAccession, reversedIndexes);
            }

            results.put(reversedSequence, mapping);
        }

        return results;
    }

    /**
     * Returns a node related to a tag and updates the cache. Null if not found.
     *
     * @param tag the tag of interest
     * @return the corresponding node
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private Node getNode(String tag) throws SQLException, ClassNotFoundException, IOException, InterruptedException {

        Node result = tree.get(tag);

        if (result == null) {

            result = componentsFactory.getNode(tag);

            if (result != null) {

                long capacity = memoryAllocation * cacheScale;

                while (treeSize > capacity && !tagsInTree.isEmpty()) {
                    String tempTag = tagsInTree.pollLast();
                    Node tempNode = tree.get(tempTag);
                    treeSize -= tempNode.getSize();
                    tree.remove(tempTag);
                }

                tree.put(tag, result);
                treeSize += result.getSize();
                tagsInTree.addFirst(tag);
            }
        }

        return result;
    }

    /**
     * Closes all connections to files.
     *
     * @throws IOException
     * @throws SQLException
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
        emptyCache();
        componentsFactory.close();
    }

    /**
     * Returns the size of the cache used for peptide mappings (note that there
     * are two of them).
     *
     * @return the size of the cache used for peptide mappings
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the size of the cache used for peptide mappings (note that there are
     * two of them).
     *
     * @param cacheSize the size of the cache used for peptide mappings
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Empties the cache.
     */
    public void emptyCache() {
        tree.clear();
        tagsInTree.clear();
        lastQueriedPeptidesCache.clear();
        lastQueriedPeptidesCacheContent.clear();
        lastSlowQueriedPeptidesCache.clear();
        lastSlowQueriedPeptidesCacheContent.clear();
        proteinLengthsCache.clear();
    }

    /**
     * Reduces the node cache size by the given share. If less than 100 nodes
     * are left they will all be removed.
     *
     * @param share the share of the cache to remove. 0.5 means 50%
     */
    public void reduceNodeCacheSize(double share) {
        double limit = tree.size();
        if (limit > 100) {
            limit = share * limit;
        }
        for (int i = 0; i < limit; i++) {
            String tempTag = tagsInTree.pollLast();
            Node tempNode = tree.get(tempTag);
            treeSize -= tempNode.getSize();
            tree.remove(tempTag);
        }
    }

    /**
     * Returns the number of nodes currently loaded in cache.
     *
     * @return the number of nodes currently loaded in cache
     */
    public int getNodesInCache() {
        return tree.size();
    }

    /**
     * Returns a list of peptides matched using the given peptide sequence in
     * the given protein according the provided matching settings.
     *
     * @param peptideSequence the original peptide sequence
     * @param proteinAccession the accession of the protein of interest
     * @param matchingType the matching type
     * @param massTolerance the mass tolerance for indistinguishable amino acids
     * matching mode
     * @param limitXs if true the share of Xs in the peptide sequences will be
     * limited in order to increase sequencing speed.
     *
     * @return a list of peptides matched and their indexes in the protein
     * sequence
     * @throws IOException
     * @throws InterruptedException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public HashMap<String, ArrayList<Integer>> getMatchedPeptideSequences(String peptideSequence, String proteinAccession, MatchingType matchingType, Double massTolerance, boolean limitXs)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        HashMap<String, HashMap<String, ArrayList<Integer>>> mapping = getProteinMapping(peptideSequence, matchingType, massTolerance, limitXs);
        HashMap<String, ArrayList<Integer>> tempMapping, result = new HashMap<String, ArrayList<Integer>>();

        for (String peptide : mapping.keySet()) {
            tempMapping = mapping.get(peptide);
            if (tempMapping.containsKey(proteinAccession)) {
                result.put(peptide, tempMapping.get(proteinAccession));
            }
        }

        return result;
    }

    /**
     * Returns a PeptideIterator which iterates alphabetically all peptides
     * corresponding to the end of a branch in the tree.
     *
     * @return a PeptideIterator which iterates alphabetically all peptides
     * corresponding to the end of a branch in the tree
     *
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws java.lang.InterruptedException
     */
    public PeptideIterator getPeptideIterator() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return new PeptideIterator();
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
     * Alphabetical iterator for the tree.
     */
    public class PeptideIterator implements Iterator {

        /**
         * The initial tag size of the tree.
         */
        private Integer initialTagSize;
        /**
         * The list of possible initial tags.
         */
        private ArrayList<String> tags;
        /**
         * The current node.
         */
        private Node currentNode = null;
        /**
         * The parent node.
         */
        private Node parentNode = null;
        /**
         * The current peptide sequence.
         */
        private String currentSequence = null;
        /**
         * List of amino acids found in the current node subtree if any.
         */
        private ArrayList<Character> aas = null;
        /**
         * The current iterator position in the tags.
         */
        private int i = -1;
        /**
         * The current iterator position in the amino acid list.
         */
        private int j = 0;

        /**
         * Constructor.
         *
         * @throws SQLException
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private PeptideIterator() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
            initialTagSize = componentsFactory.getInitialSize();
            tags = TagFactory.getAminoAcidCombinations(initialTagSize);
        }

        @Override
        public boolean hasNext() {

            try {
                if (currentNode != null && currentNode.getDepth() == initialTagSize && currentNode.getAccessions() != null && i < tags.size() - 1) {
                    // ok we're done with this node
                    parentNode = null;
                    aas = null;
                    j = 0;
                    currentSequence = tags.get(++i);
                    currentNode = getNode(currentSequence);
                }

                while (++i < tags.size() && currentNode == null && parentNode == null) {
                    currentSequence = tags.get(i);
                    currentNode = getNode(currentSequence);
                }

                if (i < tags.size()) {
                    if (aas != null) {

                        int parentDepth = currentSequence.length() - 1;
                        currentSequence = currentSequence.substring(0, parentDepth);

                        if (++j == aas.size()) {
                            if (!parentNode.getTermini().isEmpty()) {
                                currentNode = null;
                                return true;
                            } else {
                                j++;
                            }
                        }

                        if (j == aas.size() + 1) {
                            if (parentDepth <= initialTagSize) {
                                // ok we're done with this node
                                currentSequence = null;
                                currentNode = null;
                                parentNode = null;
                                aas = null;
                                j = 0;
                            } else {
                                parentDepth = currentSequence.length() - 1;
                                String parentSequence = currentSequence.substring(0, parentDepth);
                                char aa = currentSequence.charAt(parentDepth);
                                if (parentDepth == initialTagSize) {
                                    parentNode = getNode(parentSequence);
                                } else {
                                    String tag = parentSequence.substring(0, initialTagSize);
                                    parentNode = getNode(tag).getSubNode(parentSequence);
                                }
                                currentNode = parentNode.getSubtree().get(aa);
                                aas = new ArrayList<Character>(parentNode.getSubtree().keySet());
                                Collections.sort(aas);
                                j = aas.indexOf(aa);
                            }

                            return hasNext();
                        }

                        char aa = aas.get(j);
                        currentSequence += aa;
                        currentNode = parentNode.getSubtree().get(aa);
                    }

                    while (currentNode.getAccessions() == null) {

                        j = 0;
                        aas = new ArrayList<Character>(currentNode.getSubtree().keySet());
                        parentNode = currentNode;

                        if (!aas.isEmpty()) {
                            Collections.sort(aas);
                            char aa = aas.get(j);
                            currentSequence += aa;
                            currentNode = currentNode.getSubtree().get(aa);
                        } else {
                            currentNode = null;
                            return true;
                        }
                    }

                    return true;
                }

                return false;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("An error occurred while iterating the tree. See previous exception.");
            }
        }

        @Override
        public Object next() {
            return currentSequence;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("ProteinTrees are not editable.");
        }

        /**
         * Returns the protein mapping of the current peptide.
         *
         * @return the protein mapping of the current peptide.
         */
        public HashMap<String, ArrayList<Integer>> getMapping() {
            if (currentNode != null) {
                return currentNode.getAccessions();
            } else {
                return parentNode.getTermini();
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
     * @param waitingHandler waiting handler
     * @return all the positions of the given tags
     */
    private HashMap<String, ArrayList<Integer>> getTagToIndexesMap(String sequence, ArrayList<String> tags, Enzyme enzyme,
            WaitingHandler waitingHandler) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        HashMap<String, ArrayList<Integer>> tagToIndexesMap = new HashMap<String, ArrayList<Integer>>(tags.size());
        Integer initialTagSize = componentsFactory.getInitialSize();

        for (String tag : tags) {
            tagToIndexesMap.put(tag, new ArrayList<Integer>());
        }

        for (int i = 0; i < sequence.length() - initialTagSize; i++) {

            if (enzyme == null || i == 0 || enzyme.isCleavageSite(sequence.charAt(i - 1), sequence.charAt(i))) {

                char[] tagValue = new char[initialTagSize];

                for (int j = 0; j < initialTagSize; j++) {
                    char aa = sequence.charAt(i + j);
                    tagValue[j] = aa;
                }

                String tag = new String(tagValue);
                ArrayList<Integer> tempIndexes = tagToIndexesMap.get(tag);

                if (tempIndexes != null) {
                    tempIndexes.add(i);
                }
            }

            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                break;
            }
        }

        return tagToIndexesMap;
    }

    /**
     * Retrieves the length of a protein.
     *
     * @param accession the accession of the protein of interest
     * @return the length of this protein
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public Integer getProteinLength(String accession) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        Integer length = proteinLengthsCache.get(accession);
        if (length == null) {
            Protein protein = sequenceFactory.getProtein(accession);
            if (protein != null) {
                length = protein.getLength();
            } else {
                throw new IllegalArgumentException("Length of protein " + accession + " not found.");
            }
            proteinLengthsCache.put(accession, length);
        }
        return length;
    }

    /**
     * Runnable used for the indexing of a protein sequence.
     */
    private class SequenceIndexer implements Runnable {

        /**
         * The proteins to process.
         */
        private ArrayList<Protein> proteins;
        /**
         * Boolean indicating whether the thread shall be interrupted.
         */
        private boolean finished = false;
        /**
         * List of tags to inspect.
         */
        private ArrayList<String> tags;
        /**
         * The enzyme to use.
         */
        private Enzyme enzyme;
        /**
         * The result of the indexing.
         */
        private HashMap<String, HashMap<String, ArrayList<Integer>>> indexes = new HashMap<String, HashMap<String, ArrayList<Integer>>>(proteinBatchSize);
        /**
         * The waiting handler.
         */
        private WaitingHandler waitingHandler;
        /**
         * Boolean indicating whether progress should be displayed.
         */
        private boolean displayProgress;

        /**
         * Constructor.
         *
         * @param proteins the proteins to process
         * @param tags the tags to process
         * @param enzyme enzyme to use (can be null)
         * @param waitingHandler waiting handler displaying progress to the user
         * (can be null)
         * @param displayProgress boolean indicating whether progress shall be
         * displayed on the progress bar of the waiting handler
         */
        public SequenceIndexer(ArrayList<Protein> proteins, ArrayList<String> tags, Enzyme enzyme, WaitingHandler waitingHandler, boolean displayProgress) {
            this.proteins = proteins;
            this.tags = tags;
            this.enzyme = enzyme;
            this.waitingHandler = waitingHandler;
            this.displayProgress = displayProgress;
        }

        @Override
        public synchronized void run() {

            try {
                for (Protein protein : proteins) {

                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        return;
                    }

                    indexes.put(protein.getAccession(), getTagToIndexesMap(protein.getSequence(), tags, enzyme, waitingHandler));

                    if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                        waitingHandler.increaseSecondaryProgressCounter();
                    }

                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        return;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
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
            tags = null;
            indexes.clear();
        }
    }

    /**
     * Runnable used to process raw nodes and store them in the database.
     */
    private class NodeSplitter implements Runnable {

        /**
         * The tag of the node.
         */
        private String tag;
        /**
         * The node.
         */
        private Node node;
        /**
         * the max node size.
         */
        private int maxNodeSize;
        /**
         * The max peptide size.
         */
        private int maxPeptideSize;
        /**
         * Boolean indicating whether the thread shall be interrupted.
         */
        private boolean finished = false;
        /**
         * The waiting handler
         */
        private WaitingHandler waitingHandler;
        /**
         * boolean indicating whether progress should be displayed
         */
        private boolean displayProgress;

        /**
         * Constructor.
         *
         * @param tag the tag of interest
         * @param node the node to process
         */
        public NodeSplitter(String tag, Node node, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, boolean displayProgress) {
            this.tag = tag;
            this.node = node;
            this.waitingHandler = waitingHandler;
            this.displayProgress = displayProgress;
        }

        @Override
        public synchronized void run() {

            try {
                node.splitNode(maxNodeSize, maxPeptideSize);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            finished = true;

            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

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
         * Clears the content of the runnable.
         */
        public void clear() {
            node = null;
        }

        /**
         * Returns the tag of the splitted node.
         *
         * @return the tag of the splitted node
         */
        public String getTag() {
            return tag;
        }

        /**
         * Returns the splitted node.
         *
         * @return the splitted node
         */
        public Node getNode() {
            return node;
        }
    }
}
