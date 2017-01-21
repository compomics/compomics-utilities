package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.Util;
import com.compomics.util.db.DerbyUtil;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory.ProteinIterator;
import com.compomics.util.experiment.identification.TagFactory;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.amino_acid_tags.matchers.TagMatcher;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences.MatchingType;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class sorts the proteins into groups.
 *
 * @author Marc Vaudel
 */
public class ProteinTree implements PeptideMapper {

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
    private int cacheSize = 100;
    /**
     * Indicates whether the cache should be used.
     */
    private boolean useCache = true;
    /**
     * Cache of the last queried peptides.
     */
    private HashMap<String, ArrayList<PeptideProteinMapping>> lastQueriedPeptidesCache;
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
    private HashMap<String, ArrayList<PeptideProteinMapping>> lastSlowQueriedPeptidesCache;
    /**
     * Peptide sequences in slow cache.
     */
    private ArrayDeque<String> lastSlowQueriedPeptidesCacheContent = new ArrayDeque<String>(cacheSize);
    /**
     * The version of the protein tree.
     */
    public static final String version = "1.1.2";
    /**
     * The sequence matching preferences of the matches in cache.
     */
    private SequenceMatchingPreferences cacheSequenceMatchingPreferences = null;
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
     * @throws IOException if an IOException occurs
     */
    public ProteinTree(int memoryAllocation, int cacheSize) throws IOException {

        this.memoryAllocation = memoryAllocation;
        this.cacheSize = cacheSize;
        lastSlowQueriedPeptidesCache = new HashMap<String, ArrayList<PeptideProteinMapping>>(cacheSize);
        lastQueriedPeptidesCache = new HashMap<String, ArrayList<PeptideProteinMapping>>(cacheSize);

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
     * @param nThreads the number of threads to use
     * @param initialTagSize the initial tag size
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 500 giving an approximate
     * query time &lt;20ms.
     * @param maxPeptideSize the maximum peptide size
     * @param waitingHandler the waiting handler used to display progress to the
     * user and cancel the process. Can be null but strongly recommended.
     * @param exceptionHandler handler for the exceptions encountered while
     * creating the tree
     * @param printExpectedImportTime if true the expected import time will be
     * printed to the waiting handler
     * @param displayProgress display progress
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws InterruptedException if an InterruptedException occurs
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean printExpectedImportTime, boolean displayProgress, int nThreads)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {
        initiateTree(initialTagSize, maxNodeSize, maxPeptideSize, null, waitingHandler, exceptionHandler, printExpectedImportTime, displayProgress, nThreads);
    }

    /**
     * Initiates the tree. Note: speed and memory are calibrated for the no
     * enzyme case.
     *
     * @param nThreads the number of threads to use
     * @param initialTagSize the initial size of peptide tag. Large initial size
     * are fast to query, low initial size are fast to initiate. I typically use
     * 3 for databases containing less than 100 000 proteins giving an
     * approximate initiation time of 60ms per accession.
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 500 giving an approximate
     * query time &lt;20ms.
     * @param maxPeptideSize the maximum peptide size
     * @param enzyme the enzyme used to select peptides. If null all possible
     * peptides will be indexed
     * @param waitingHandler the waiting handler used to display progress to the
     * user and cancel the process. Can be null but strongly recommended.
     * @param exceptionHandler handler for the exceptions encountered while
     * creating the tree
     * @param printExpectedImportTime if true the expected import time will be
     * printed to the waiting handler
     * @param displayProgress display progress
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while creating the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean printExpectedImportTime, boolean displayProgress, int nThreads)
            throws IOException, InterruptedException, IOException, InterruptedException, ClassNotFoundException, SQLException {

        tree.clear();

        componentsFactory = ProteinTreeComponentsFactory.getInstance();

        try {
            boolean needImport;

            try {
                needImport = !componentsFactory.initiate();
                if (!needImport) {
                    componentsFactory.loadParameters();
                    if (componentsFactory.isCorrupted()) {
                        throw new IllegalArgumentException("Index is corrupted. Database will be reindexed.");
                    }
                    if (!componentsFactory.importComplete()) {
                        throw new IllegalArgumentException("Database import was not successfully completed. Database will be reindexed.");
                    }
                    String tempVersion = componentsFactory.getVersion();
                    if (tempVersion == null || !tempVersion.equals(version)) {
                        throw new IllegalArgumentException("Database index version " + tempVersion + " obsolete. Database will be reindexed.");
                    }
                    if (initialTagSize != componentsFactory.getInitialSize()) {
                        throw new IllegalArgumentException("Different initial size. Database will be reindexed.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                needImport = true;
                DerbyUtil.closeConnection();
                componentsFactory.delete();
                componentsFactory.initiate();
            }

            if (needImport) {
                importDb(initialTagSize, maxNodeSize, maxPeptideSize, enzyme, waitingHandler, exceptionHandler, printExpectedImportTime, displayProgress, nThreads);
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
     * Try to delete the current database. Note: The delete method will attempt
     * to close the connection. It is thus not needed (and not advised) to close
     * the connection before deleting.
     *
     * @return true of the deletion was a success
     */
    public boolean deleteDb() {
        try {
            return componentsFactory.delete();
        } catch (Exception ex) {
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
     * user and cancel the process. Can be null but strongly recommended.
     * @param exceptionHandler handler for the exceptions encountered while
     * creating the tree
     * @param printExpectedImportTime if true the expected import time will be
     * printed to the waiting handler
     * @param nThreads the number of threads to use
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while creating the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private void importDb(int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean printExpectedImportTime, boolean displayProgress, int nThreads)
            throws IOException, InterruptedException, IOException, InterruptedException, ClassNotFoundException, SQLException {

        if (printExpectedImportTime) {
            int nSeconds = getExpectedImportTime();
            String report = "Estimated import time: ";

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
                waitingHandler.appendReport("    See http://compomics.github.io/compomics-utilities/wiki/proteininference.html.", true, true);
            } else {
                System.out.println(report);
                System.out.println("    See http://compomics.github.io/compomics-utilities/wiki/proteininference.html.");
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
            Collections.shuffle(tags);
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
                loadTags(tempTags, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, nThreads, waitingHandler, exceptionHandler, displayProgress);
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
            loadTags(tempTags, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, nThreads, waitingHandler, exceptionHandler, displayProgress);

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
            int timeInSeconds = (int) (1.2 * sequenceFactory.getCurrentFastaFile().length() / (1000 * ratio));
            timeInSeconds = Math.max(timeInSeconds, 1);
            return timeInSeconds;
        }
    }

    /**
     * Loads the tags found in the given proteins in the tree and saves the end
     * nodes in the NodeFactory if not null.
     *
     * @param tags the tags of interest
     * @param waitingHandler the waiting handler used to display progress to the
     * user and cancel the process. Can be null but strongly recommended.
     * @param exceptionHandler handler for the exceptions encountered while
     * creating the tree
     * @param enzyme the enzyme restriction
     * @param loadLengths boolean indicating whether protein lengths should be
     * loaded in the db
     * @param loadedLengths the accessions of the proteins from which the length
     * is already saved
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while creating the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private synchronized void loadTags(ArrayList<String> tags,
            int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, int nThreads, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean displayProgress)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        // find the tags in the proteins and create a node per tag found
        if (nThreads == 1) {
            indexProteinsSingleThread(tags, initialTagSize, enzyme, waitingHandler, displayProgress);
        } else {
            indexProteins(tags, initialTagSize, enzyme, waitingHandler, exceptionHandler, displayProgress, nThreads);
        }

        // split the nodes and save them in the db
        if (nThreads == 1) {
            processRawNodesSingleThread(tags, maxNodeSize, maxPeptideSize, waitingHandler, displayProgress);
        } else {
            processRawNodes(maxNodeSize, maxPeptideSize, waitingHandler, exceptionHandler, displayProgress, nThreads);
        }

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
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while creating the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private void indexProteinsSingleThread(ArrayList<String> tags,
            int initialTagSize, Enzyme enzyme, WaitingHandler waitingHandler, boolean displayProgress)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

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
     * @param exceptionHandler handler for the exceptions encountered while
     * creating the tree
     * @param initialTagSize the initial tag size
     * @param enzyme enzyme to use. Can be null
     * @param loadLengths boolean indicating whether protein lengths should be
     * loaded in the db
     * @param displayProgress boolean indicating whether progress shall be
     * displayed using the waiting handler
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while creating the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private void indexProteins(ArrayList<String> tags,
            int initialTagSize, Enzyme enzyme, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean displayProgress, int nThreads)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        ArrayList<Protein> sequenceBuffer = new ArrayList<Protein>(proteinBatchSize);
        ArrayList<SequenceIndexer> sequenceIndexers = new ArrayList<SequenceIndexer>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);

        ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(sequenceFactory.isDefaultReversed());

        while (proteinIterator.hasNext()) {
            Protein protein = proteinIterator.getNextProtein();
            sequenceBuffer.add(protein);
            if (sequenceBuffer.size() == proteinBatchSize) {
                while (sequenceIndexers.size() == nThreads) {
                    processFinishedIndexers(sequenceIndexers, initialTagSize);
                }
                SequenceIndexer sequenceIndexer = new SequenceIndexer(sequenceBuffer, tags, enzyme, waitingHandler, exceptionHandler, displayProgress);
                pool.submit(new Thread(sequenceIndexer, "sequence indexing"));
                sequenceBuffer = new ArrayList<Protein>(proteinBatchSize);
                sequenceIndexers.add(sequenceIndexer);
            }
            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled() || waitingHandler.isRunFinished()) {
                    pool.shutdownNow();
                    emptyCache();
                    return;
                }
            }
        }

        if (!sequenceBuffer.isEmpty()) {
            SequenceIndexer sequenceIndexer = new SequenceIndexer(sequenceBuffer, tags, enzyme, waitingHandler, exceptionHandler, displayProgress);
            pool.submit(new Thread(sequenceIndexer, "sequence indexing"));
            sequenceIndexers.add(sequenceIndexer);
        }

        while (!sequenceIndexers.isEmpty()) {
            processFinishedIndexers(sequenceIndexers, initialTagSize);
        }
        pool.shutdown();
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
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while creating the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private void processRawNodesSingleThread(ArrayList<String> tags, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, boolean displayProgress)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

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
     * @param exceptionHandler handler for the exceptions encountered while
     * creating the tree
     * @param displayProgress boolean indicating whether progress shall be
     * displayed using the waiting handler
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while creating the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private void processRawNodes(int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean displayProgress, int nThreads)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        ArrayList<NodeSplitter> nodeSplitters = new ArrayList<NodeSplitter>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);

        for (String tag : tree.keySet()) {

            Node node = tree.get(tag);

            while (nodeSplitters.size() == nThreads) {
                processFinishedNodeSplitters(nodeSplitters, null); // @TODO: add waiting handler
            }

            NodeSplitter nodeSplitter = new NodeSplitter(tag, node, maxNodeSize, maxPeptideSize, waitingHandler, exceptionHandler, displayProgress);
            pool.submit(new Thread(nodeSplitter, "Node splitting of tag " + tag));
            nodeSplitters.add(nodeSplitter);

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled() || waitingHandler.isRunFinished()) {
                    emptyCache();
                    pool.shutdownNow();
                    return;
                }
            }
        }

        while (!nodeSplitters.isEmpty()) {
            processFinishedNodeSplitters(nodeSplitters, null); // @TODO: add waiting handler
        }
        pool.shutdown();
    }

    /**
     * Clears the finished raw node splitters from a given list or wait for one
     * to finish and batch saves the splitted nodes.
     *
     * @param nodeProcessors the node processors of interest
     * @param waitingHandler the waiting handler
     *
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while splitting nodes.
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
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while processing finished indexers.
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

    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(String peptideSequence, SequenceMatchingPreferences proteinInferencePreferences)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        long time0 = 0;
        if (debugSpeed) {
            time0 = System.currentTimeMillis();
        }

        ArrayList<PeptideProteinMapping> result = getProteinMapping(peptideSequence, proteinInferencePreferences, false);

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
     * sequence. peptide sequence &gt; protein accession &gt; index in the
     * protein. An empty map if not.
     *
     * @param peptideSequence the peptide sequence
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param reversed boolean indicating whether we are looking at a reversed
     * peptide sequence
     *
     * @return the peptide to protein mapping: Accession &gt; list of indexes
     * where the peptide can be found on the sequence
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private ArrayList<PeptideProteinMapping> getProteinMapping(String peptideSequence, SequenceMatchingPreferences sequenceMatchingPreferences, boolean reversed) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        if (useCache && this.cacheSequenceMatchingPreferences != null && !this.cacheSequenceMatchingPreferences.isSameAs(sequenceMatchingPreferences)) {
            emptyCache();
            this.cacheSequenceMatchingPreferences = sequenceMatchingPreferences;
        }

        ArrayList<PeptideProteinMapping> result = null;
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
                        return getReversedResults(result, reversedSequence);
                    }
                }

                long timeStart = System.currentTimeMillis();

                int initialTagSize = componentsFactory.getInitialSize();
                if (peptideSequence.length() < initialTagSize) {
                    throw new IllegalArgumentException("Peptide (" + peptideSequence + ") should be at least of length " + initialTagSize + ".");
                }

                result = new ArrayList<PeptideProteinMapping>(2);

                AminoAcidSequence peptideAminoAcidSequence = new AminoAcidSequence(peptideSequence);
                Double limitX = null;
                if (sequenceMatchingPreferences.hasLimitX()) {
                    limitX = sequenceMatchingPreferences.getLimitX() * peptideSequence.length() / initialTagSize;
                }
                HashSet<String> initialTags = getInitialTags(peptideAminoAcidSequence, sequenceMatchingPreferences, limitX);

                for (String tag : initialTags) {
                    Node node = getNode(tag);
                    if (node != null) {
                        ArrayList<PeptideProteinMapping> tagResults = node.getProteinMapping(peptideAminoAcidSequence, tag, sequenceMatchingPreferences);
                        result.addAll(tagResults);
                    }
                }

                if (sequenceFactory.isDefaultReversed() && !reversed) {
                    String reversedSequence = SequenceFactory.reverseSequence(peptideSequence);
                    ArrayList<PeptideProteinMapping> reversedResult;
                    if (!reversedSequence.equals(peptideSequence)) {
                        reversedResult = getProteinMapping(reversedSequence, sequenceMatchingPreferences, true);
                        reversedResult = getReversedResults(reversedResult, reversedSequence);
                    } else {
                        reversedResult = getReversedResults(result, reversedSequence);
                    }
                    result.addAll(reversedResult);
                }

                if (!reversed && useCache) {
                    long timeEnd = System.currentTimeMillis();
                    long queryTime = timeEnd - timeStart;
                    addToCache(peptideSequence, result, queryTime);
                }
            }
        }

        return result;
    }

    /**
     * Adds a mapping to the cache.
     *
     * @param peptideSequence the newly mapped peptide sequence
     * @param mapping the protein mapping
     * @param queryTime the mapping time
     */
    private synchronized void addToCache(String peptideSequence, ArrayList<PeptideProteinMapping> mapping, long queryTime) {

        if (queryTime <= queryTimeThreshold) {
            lastQueriedPeptidesCache.put(peptideSequence, mapping);
            lastQueriedPeptidesCacheContent.add(peptideSequence);
            if (lastQueriedPeptidesCacheContent.size() > cacheSize) {
                String key = lastQueriedPeptidesCacheContent.pollLast();
                lastQueriedPeptidesCache.remove(key);
            }
        } else {
            lastSlowQueriedPeptidesCache.put(peptideSequence, mapping);
            lastSlowQueriedPeptidesCacheContent.add(peptideSequence);
            if (lastSlowQueriedPeptidesCacheContent.size() > cacheSize) {
                String key = lastSlowQueriedPeptidesCacheContent.pollLast();
                lastSlowQueriedPeptidesCache.remove(key);
            }
        }
    }

    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        throw new InterruptedException("Error: function not implemented");
    }

    @Override
    public ArrayList<PeptideProteinMapping> getProteinMapping(Tag tag, TagMatcher tagMatcher, SequenceMatchingPreferences sequenceMatchingPreferences, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        int initialTagSize = componentsFactory.getInitialSize();
        AminoAcidPattern longestAminoAcidPattern = null;
        AminoAcidSequence longestAminoAcidSequence = null;
        int componentIndex = -1;
        for (int i = 0; i < tag.getContent().size(); i++) {
            TagComponent tagComponent = tag.getContent().get(i);
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                if (aminoAcidPattern.length() >= initialTagSize && (longestAminoAcidPattern == null || aminoAcidPattern.length() > longestAminoAcidPattern.length()) && (longestAminoAcidSequence == null || aminoAcidPattern.length() > longestAminoAcidSequence.length())) {
                    componentIndex = i;
                    longestAminoAcidPattern = aminoAcidPattern;
                    longestAminoAcidSequence = null;
                }
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                if (aminoAcidSequence.length() >= initialTagSize && (longestAminoAcidPattern == null || aminoAcidSequence.length() > longestAminoAcidPattern.length()) && (longestAminoAcidSequence == null || aminoAcidSequence.length() > longestAminoAcidSequence.length())) {
                    componentIndex = i;
                    longestAminoAcidSequence = aminoAcidSequence;
                    longestAminoAcidPattern = null;
                }
            }
        }
        if (componentIndex == -1) {
            throw new IllegalArgumentException("No amino acid sequence longer than " + initialTagSize + " was found for tag " + tag + ".");
        }
        ArrayList<PeptideProteinMapping> seeds = new ArrayList<PeptideProteinMapping>();
        if (longestAminoAcidPattern != null) {
            for (String peptideSequence : longestAminoAcidPattern.getAllPossibleSequences()) {
                double xShare = ((double) Util.getOccurrence(peptideSequence, 'X')) / peptideSequence.length();
                if (!sequenceMatchingPreferences.hasLimitX() || xShare <= sequenceMatchingPreferences.getLimitX()) {
                    seeds.addAll(getProteinMapping(peptideSequence, sequenceMatchingPreferences));
                }
            }
        } else {
            seeds.addAll(getProteinMapping(longestAminoAcidSequence.getSequence(), sequenceMatchingPreferences));
        }
        ArrayList<PeptideProteinMapping> results = new ArrayList<PeptideProteinMapping>(seeds.size());
        for (PeptideProteinMapping peptideProteinMapping : seeds) {
            String accession = peptideProteinMapping.getProteinAccession();
            String proteinSequence = sequenceFactory.getProtein(accession).getSequence();
            int seedIndex = peptideProteinMapping.getIndex();
            ArrayList<PeptideProteinMapping> tagMapping = tagMatcher.getPeptideMatches(tag, accession, proteinSequence, seedIndex,
                    componentIndex, massTolerance);
            results.addAll(tagMapping);
        }

        return results;
    }

    /**
     * Returns a list of possible initial tags.
     *
     * @param aminoAcidSequence the peptide sequence
     * @param sequenceMatchingPreferences the sequence matching preferences to
     * use
     *
     * @return a list of possible initial tags.
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private HashSet<String> getInitialTags(AminoAcidSequence aminoAcidSequence, SequenceMatchingPreferences sequenceMatchingPreferences, Double limitX)
            throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        int initialTagSize = componentsFactory.getInitialSize();
        HashSet<String> result = new HashSet<String>();

        for (int i = 0; i < initialTagSize; i++) {

            AminoAcid aminoAcid = aminoAcidSequence.getAminoAcidAt(i);

            if (result.isEmpty()) {
                if (sequenceMatchingPreferences.getSequenceMatchingType() == MatchingType.string) {
                    String originalAa = aminoAcid.singleLetterCode;
                    result.add(originalAa);
                } else {
                    for (char originalAa : aminoAcid.getSubAminoAcids()) {
                        String newTag = String.valueOf(originalAa);
                        result.add(newTag);
                    }
                    for (char combinationAa : aminoAcid.getCombinations()) {
                        String newTag = String.valueOf(combinationAa);
                        result.add(newTag);
                    }
                    if (sequenceMatchingPreferences.getSequenceMatchingType() == MatchingType.indistiguishableAminoAcids
                            && (aminoAcid == AminoAcid.I || aminoAcid == AminoAcid.J || aminoAcid == AminoAcid.L)) {
                        result.add("I");
                        result.add("J");
                        result.add("L");
                    }
                }
            } else {
                HashSet<String> newResults = new HashSet<String>();
                for (String sequence : result) {
                    if (sequenceMatchingPreferences.getSequenceMatchingType() == MatchingType.string) {
                        String originalAa = aminoAcid.singleLetterCode;
                        newResults.add(sequence + aminoAcid.singleLetterCode);
                    } else {
                        for (char originalAa : aminoAcid.getSubAminoAcids()) {
                            String newTag = sequence + originalAa;
                            newResults.add(newTag);
                        }
                        for (char newAa : aminoAcid.getCombinations()) {
                            String newTag = sequence + newAa;
                            newResults.add(newTag);
                        }
                        if (sequenceMatchingPreferences.getSequenceMatchingType() == MatchingType.indistiguishableAminoAcids
                                && (aminoAcid == AminoAcid.I || aminoAcid == AminoAcid.J || aminoAcid == AminoAcid.L)) {
                            String newTag = sequence + "I";
                            newResults.add(newTag);
                            newTag = sequence + "J";
                            newResults.add(newTag);
                            newTag = sequence + "L";
                            newResults.add(newTag);
                        }
                    }
                }
                result = newResults;
            }
        }
        if (limitX != null && limitX < 1) {
            HashSet<String> filtered = new HashSet<String>();
            for (String sequence : result) {
                double xShare = ((double) Util.getOccurrence(sequence, 'X')) / sequence.length();
                if (xShare <= limitX) {
                    filtered.add(sequence);
                }
            }
            result = filtered;
        }
        return result;
    }

    /**
     * Reverts the indexes and the protein accessions of the given mapping.
     *
     * @param forwardResults the given mapping
     * @param sequence the sequence of interest
     *
     * @return the reversed indexes
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private ArrayList<PeptideProteinMapping> getReversedResults(ArrayList<PeptideProteinMapping> forwardResults, String sequence) throws SQLException, ClassNotFoundException, IOException, InterruptedException {

        ArrayList<PeptideProteinMapping> results = new ArrayList<PeptideProteinMapping>(forwardResults.size());

        for (PeptideProteinMapping peptideProteinMapping : forwardResults) {

            int peptideLength = sequence.length();
            String reversedSequence = SequenceFactory.reverseSequence(sequence);

            String accession = peptideProteinMapping.getProteinAccession();

            String reversedAccession;
            Integer proteinLength;

            if (accession.endsWith(SequenceFactory.getDefaultDecoyAccessionSuffix())) {
                reversedAccession = SequenceFactory.getDefaultTargetAccession(accession);
                proteinLength = getProteinLength(reversedAccession);
                if (proteinLength == null) {
                    throw new IllegalArgumentException("Length of protein " + reversedAccession + " not found.");
                }
            } else {
                reversedAccession = SequenceFactory.getDefaultDecoyAccession(accession);
                proteinLength = getProteinLength(accession);
                if (proteinLength == null) {
                    throw new IllegalArgumentException("Length of protein " + accession + " not found.");
                }
            }

            int forwardIndex = peptideProteinMapping.getIndex();
            int reversedIndex = proteinLength - forwardIndex - peptideLength;
            if (reversedIndex < 0 || reversedIndex >= proteinLength) {
                throw new IllegalArgumentException("Wrong index found for peptide " + reversedSequence + " in protein " + reversedAccession + ": " + reversedIndex + ".");
            }

            PeptideProteinMapping reversedMapping = new PeptideProteinMapping(reversedAccession, reversedSequence, reversedIndex);
            results.add(reversedMapping);
        }

        return results;
    }

    /**
     * Returns a node related to a tag and updates the cache. Null if not found.
     *
     * @param tag the tag of interest
     *
     * @return the corresponding node
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private Node getNode(String tag) throws SQLException, ClassNotFoundException, IOException, InterruptedException {

        Node result = tree.get(tag);

        if (result == null) {
            result = getNodeSynchronized(tag);
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
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private synchronized Node getNodeSynchronized(String tag) throws SQLException, ClassNotFoundException, IOException, InterruptedException {

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

    @Override
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

        // delete outdated trees
        try {
            ProteinTreeComponentsFactory.deletOutdatedTrees();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public synchronized void reduceNodeCacheSize(double share) {
        double limit = tree.size();
        if (limit > 100) {
            limit = share * limit;
        }
        for (int i = 0; i < limit; i++) {
            String tempTag = tagsInTree.pollLast();
            Node tempNode = tree.get(tempTag);
            if (tempNode == null) {
                // another thread already reduced the cache size
                break;
            }
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
     * Returns a PeptideIterator which iterates alphabetically all peptides
     * corresponding to the end of a branch in the tree.
     *
     * @return a PeptideIterator which iterates alphabetically all peptides
     * corresponding to the end of a branch in the tree
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    public PeptideIterator getPeptideIterator() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return new PeptideIterator();
    }

    /**
     * Notifies the tree that a runnable has finished working.
     *
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while waiting.
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
         * @throws IOException exception thrown whenever an error occurs while
         * reading or writing a file.
         * @throws ClassNotFoundException exception thrown whenever an error
         * occurs while deserializing an object.
         * @throws InterruptedException exception thrown whenever a threading
         * issue occurred while interacting with the tree.
         * @throws SQLException if an SQLException exception thrown whenever a
         * problem occurred while interacting with the tree database.
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
     * map: tag &gt; list of indexes in the sequence.
     *
     * @param sequence the sequence of interest
     * @param tags the tags of interest
     * @param enzyme the enzyme restriction
     * @param waitingHandler waiting handler
     *
     * @return all the positions of the given tags
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.F
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
     *
     * @return the length of this protein
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    public Integer getProteinLength(String accession) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        Integer length = proteinLengthsCache.get(accession);
        if (length == null) {
            return getProteinLengthSynchronized(accession);
        }
        return length;
    }

    /**
     * Retrieves the length of a protein.
     *
     * @param accession the accession of the protein of interest
     *
     * @return the length of this protein
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    private synchronized Integer getProteinLengthSynchronized(String accession) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
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
     * Returns the initial tag size of the tree.
     *
     * @return the initial tag size of the tree
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while interacting with the tree.
     * @throws SQLException if an SQLException exception thrown whenever a
     * problem occurred while interacting with the tree database.
     */
    public Integer getInitialTagSize() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return componentsFactory.getInitialSize();
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
         * Handler for the exceptions.
         */
        private ExceptionHandler exceptionHandler;

        /**
         * Constructor.
         *
         * @param proteins the proteins to process
         * @param tags the tags to process
         * @param enzyme enzyme to use (can be null)
         * @param waitingHandler waiting handler providing feedback on the
         * process and allowing canceling the process
         * @param exceptionHandler handler for the exceptions encountered while
         * creating the tree
         * @param displayProgress boolean indicating whether progress shall be
         * displayed on the progress bar of the waiting handler
         */
        public SequenceIndexer(ArrayList<Protein> proteins, ArrayList<String> tags, Enzyme enzyme, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean displayProgress) {
            this.proteins = proteins;
            this.tags = tags;
            this.enzyme = enzyme;
            this.waitingHandler = waitingHandler;
            this.exceptionHandler = exceptionHandler;
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
            } catch (Exception ex) {
                if (exceptionHandler != null) {
                    exceptionHandler.catchException(ex);
                } else {
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
         * Returns the indexes: protein accession &gt; tag &gt; indexes of the
         * tag on the protein sequence
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
         * The waiting handler.
         */
        private WaitingHandler waitingHandler;
        /**
         * Boolean indicating whether progress should be displayed.
         */
        private boolean displayProgress;
        /**
         * Handler for the exceptions.
         */
        private ExceptionHandler exceptionHandler;

        /**
         * Constructor.
         *
         *
         * @param maxNodeSize the maximal size allowed for a node
         * @param maxPeptideSize the maximal peptide length allowed
         * @param waitingHandler waiting handler providing feedback on the
         * process and allowing canceling the process
         * @param exceptionHandler handler for the exceptions encountered while
         * creating the tree
         * @param displayProgress boolean indicating whether progress shall be
         * displayed using the waiting handler
         */
        public NodeSplitter(String tag, Node node, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean displayProgress) {
            this.tag = tag;
            this.node = node;
            this.waitingHandler = waitingHandler;
            this.exceptionHandler = exceptionHandler;
            this.displayProgress = displayProgress;
        }

        @Override
        public synchronized void run() {

            try {
                node.splitNode(maxNodeSize, maxPeptideSize);
            } catch (Exception ex) {
                if (exceptionHandler != null) {
                    exceptionHandler.catchException(ex);
                } else {
                    ex.printStackTrace();
                }
            }

            finished = true;

            if (displayProgress && waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            try {
                runnableFinished();
            } catch (Exception ex) {
                if (exceptionHandler != null) {
                    exceptionHandler.catchException(ex);
                } else {
                    ex.printStackTrace();
                }
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
         * Returns the tag of the split node.
         *
         * @return the tag of the split node
         */
        public String getTag() {
            return tag;
        }

        /**
         * Returns the split node.
         *
         * @return the split node
         */
        public Node getNode() {
            return node;
        }
    }
}
