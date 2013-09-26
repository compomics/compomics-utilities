package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.TagFactory;
import com.compomics.util.experiment.identification.matches.ProteinMatch.MatchingType;
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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final long cacheScale = 6000;
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
     * Size of the cache of the most queried peptides.
     */
    private int cacheSize = 10000;
    /**
     * Cache of the last queried peptides.
     */
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>> lastQueriedPeptidesCache = new HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>>(cacheSize);
    /**
     * Peptide sequences in cache.
     */
    private ArrayList<String> lastQueriedPeptidesCacheContent = new ArrayList<String>(cacheSize);
    /**
     * Time in ms after which a query is considered as slow.
     */
    private int queryTimeThreshold = 50;
    /**
     * Cache of the last queried peptides where the query took long.
     */
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>> lastSlowQueriedPeptidesCache = new HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>>(cacheSize);
    /**
     * Peptide sequences in slow cache.
     */
    private ArrayList<String> lastSlowQueriedPeptidesCacheContent = new ArrayList<String>(cacheSize);
    /**
     * The version of the protein tree.
     */
    public static final String version = "1.0.0";
    /**
     * The matching type of the matches in cache.
     */
    private MatchingType matchingTypeInCache = MatchingType.indistiguishibleAminoAcids;
    /**
     * The mass tolerance of the matches in cache.
     */
    private Double massToleranceInCache = null;
    /**
     * indicates whether the main thread is listening or preparing to wait
     */
    private boolean listening = true;

    /**
     * Creates a tree based on the proteins present in the sequence factory.
     *
     * @param memoryAllocation the number of MB available for the tree in
     * memory.
     * @throws IOException
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
     * @param initialTagSize the initial tag size
     * @param maxNodeSize the maximal size of a node. large nodes will be fast
     * to initiate but slow to query. I typically use 500 giving an approximate
     * query time <20ms.
     * @param maxPeptideSize the maximum peptide size
     * @param waitingHandler the waiting handler used to display progress to the
     * user. Can be null but strongly recommended :)
     * @param printExpectedImportTime if true the expected import time will be
     * printed to the waiting handler
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler, boolean printExpectedImportTime)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {
        initiateTree(initialTagSize, maxNodeSize, maxPeptideSize, null, waitingHandler, printExpectedImportTime);
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
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void initiateTree(int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, WaitingHandler waitingHandler, boolean printExpectedImportTime)
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
                importDb(initialTagSize, maxNodeSize, maxPeptideSize, enzyme, waitingHandler, printExpectedImportTime);
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
    private void importDb(int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, WaitingHandler waitingHandler, boolean printExpectedImportTime)
            throws IOException, IllegalArgumentException, InterruptedException, IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        if (printExpectedImportTime && waitingHandler != null && waitingHandler.isReport()) {
            if (initialTagSize == 3 || initialTagSize == 4) {
                String report = "Expected import time: ";
                int nSeconds;
                if (initialTagSize == 3) {
                    nSeconds = sequenceFactory.getNTargetSequences() * 15 / 1000;
                } else {
                    nSeconds = sequenceFactory.getNTargetSequences() * 2 / 10;
                }
                if (nSeconds < 120) {
                    report += nSeconds + " seconds. (First time only.)";
                } else {
                    int nMinutes = nSeconds / 60;
                    if (nMinutes < 120) {
                        report += nMinutes + " minutes. (First time only.)";
                    } else {
                        int nHours = nMinutes / 60;
                        report += nHours + " hours. (First time only.)";
                    }
                }
                waitingHandler.appendReport(report, true, true);
            }
        }

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
            accessions = new ArrayList<String>(sequenceFactory.getAccessions());
        }

        long tagsSize = 500; // The space needed for tags in percent (empirical value)
        long criticalSize = tagsSize * accessions.size();

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

        if (debugSpeed) {
            debugSpeedWriter.write("Critical size: " + criticalSize);
            System.out.println("Critical size: " + criticalSize);
            estimatedTreeSize = estimatedTreeSize / 100;
            debugSpeedWriter.write("Estimated tree size: " + estimatedTreeSize);
            System.out.println("Estimated tree size: " + estimatedTreeSize);
            debugSpeedWriter.write(new Date() + " " + nPassages + " passages needed (" + nTags + " tags of " + tags.size() + " per passage)");
            System.out.println(new Date() + " " + nPassages + " passages needed (" + nTags + " tags of " + tags.size() + " per passage)");
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
        int tagsLoaded = 0;
        ArrayList<String> loadedAccessions = new ArrayList<String>();

        for (String tag : tags) {
            if (tempTags.size() == nTags) {
                loadTags(tempTags, accessions, waitingHandler, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, loadedAccessions);
                tagsLoaded += tempTags.size();
                tempTags.clear();
                if (sequenceFactory.getnCache() < accessions.size()) {
                    Collections.reverse(accessions);
                }
                if (debugSpeed) {
                    debugSpeedWriter.write(new Date() + " " + tagsLoaded + " tags of " + tags.size() + " loaded.");
                    System.out.println(new Date() + " " + tagsLoaded + " tags of " + tags.size() + " loaded.");
                    debugSpeedWriter.newLine();
                    debugSpeedWriter.flush();
                }
            }
            tempTags.add(tag);
        }

        if (!tempTags.isEmpty()) {
            loadTags(tempTags, accessions, waitingHandler, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, loadedAccessions);

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

        componentsFactory.setVersion(version);
        componentsFactory.setImportComplete(true);

        if (debugSpeed) {

            long time1 = System.currentTimeMillis();
            long initiationTime = time1 - time0;

            debugSpeedWriter.write("tree initiation: " + initiationTime + " ms.");
            System.out.println("tree initiation: " + initiationTime + " ms.");
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
     * @param saveLength boolean indicating whether the length of the proteins
     * shall be saved (mandatory when computing reverse indexes on the fly)
     * @param loadedAccessions the accessions already loaded in the factory
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private synchronized void loadTags(ArrayList<String> tags, ArrayList<String> accessions, WaitingHandler waitingHandler,
            int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, ArrayList<String> loadedAccessions)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        int nThreads = Math.max(Runtime.getRuntime().availableProcessors()-1, 1);

        ArrayList<Protein> sequenceBuffer = new ArrayList<Protein>(nThreads);
        ArrayList<SequenceIndexer> sequenceIndexers = new ArrayList<SequenceIndexer>(nThreads);

        for (String accession : accessions) {

            Protein protein = sequenceFactory.getProtein(accession);
            if (!loadedAccessions.contains(accession)) {
                componentsFactory.saveProteinLength(accession, protein.getLength());
                loadedAccessions.add(accession);
            }
            sequenceBuffer.add(protein);

            if (sequenceBuffer.size() == nThreads) {
                while (sequenceIndexers.size() == nThreads) {
                    processFinishedIndexers(sequenceIndexers, initialTagSize);
                }
                ArrayList<Protein> proteinsToProcess = new ArrayList<Protein>(sequenceBuffer.subList(0, nThreads - sequenceIndexers.size()));
                for (Protein tempProtein : proteinsToProcess) {
                    sequenceBuffer.remove(tempProtein);
                    SequenceIndexer sequenceIndexer = new SequenceIndexer(tempProtein.getAccession(), tempProtein.getSequence(), tags, enzyme);
                    sequenceIndexers.add(sequenceIndexer);
                    new Thread(sequenceIndexer, "sequence indexing of protein " + tempProtein.getAccession()).start();
                }
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
                waitingHandler.increaseSecondaryProgressCounter();
            }
        }
        while (!sequenceBuffer.isEmpty()) {
            if (sequenceIndexers.size() == nThreads) {
                processFinishedIndexers(sequenceIndexers, initialTagSize);
            }
            ArrayList<Protein> proteinsToProcess = new ArrayList<Protein>(sequenceBuffer.subList(0, Math.min(nThreads - sequenceIndexers.size(), sequenceBuffer.size())));
            for (Protein tempProtein : proteinsToProcess) {
                sequenceBuffer.remove(tempProtein);
                SequenceIndexer sequenceIndexer = new SequenceIndexer(tempProtein.getAccession(), tempProtein.getSequence(), tags, enzyme);
                sequenceIndexers.add(sequenceIndexer);
                new Thread(sequenceIndexer, "sequence indexing of protein " + tempProtein.getAccession()).start();
            }
        }
        while (!sequenceIndexers.isEmpty()) {
            processFinishedIndexers(sequenceIndexers, initialTagSize);
        }
        ArrayList<RawNodeProcessor> rawNodeProcessors = new ArrayList<RawNodeProcessor>(nThreads);
        if (waitingHandler != null) {
            waitingHandler.increaseSecondaryProgressCounter(tags.size() - tree.size());
        }
        for (String tag : tree.keySet()) {
            Node node = tree.get(tag);
            while (rawNodeProcessors.size() == nThreads) {
                processFinishedNodeProcessors(rawNodeProcessors);
            }
            RawNodeProcessor rawNodeProcessor = new RawNodeProcessor(tag, node, maxNodeSize, maxPeptideSize);
            new Thread(rawNodeProcessor, "Raw node process of tag " + tag).start();
            rawNodeProcessors.add(rawNodeProcessor);
            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
                waitingHandler.increaseSecondaryProgressCounter();
            }
        }
        while (!rawNodeProcessors.isEmpty()) {
            processFinishedNodeProcessors(rawNodeProcessors);
        }
        tree.clear();
        System.gc();
    }

    /**
     * Clears the finished raw node processors from a given list or wait for one
     * to finish.
     *
     * @param nodeProcessors the node processors of interest
     *
     * @throws InterruptedException
     */
    private synchronized void processFinishedNodeProcessors(ArrayList<RawNodeProcessor> nodeProcessors) throws InterruptedException {
        listening = false;
        ArrayList<RawNodeProcessor> done = new ArrayList<RawNodeProcessor>();
        for (RawNodeProcessor rawNodeProcessor : nodeProcessors) {
            if (rawNodeProcessor.isFinished()) {
                done.add(rawNodeProcessor);
            }
        }
        if (done.isEmpty()) {
            listening = true;
            wait();
            for (RawNodeProcessor rawNodeProcessor : nodeProcessors) {
                if (rawNodeProcessor.isFinished()) {
                    done.add(rawNodeProcessor);
                }
            }
        }
        listening = true;
        nodeProcessors.removeAll(done);
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
            HashMap<String, ArrayList<Integer>> tagToIndexesMap = sequenceIndexer.getIndexes();
            for (String tag : tagToIndexesMap.keySet()) {
                ArrayList<Integer> indexes = tagToIndexesMap.get(tag);
                if (!indexes.isEmpty()) {
                    Node node = tree.get(tag);
                    if (node == null) {
                        node = new Node(initialTagSize);
                        tree.put(tag, node);
                    }
                    node.addAccession(sequenceIndexer.getAccession(), tagToIndexesMap.get(tag));
                }
            }
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
        HashMap<String, HashMap<String, ArrayList<Integer>>> mapping = getProteinMapping(peptideSequence, MatchingType.string, null);
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
     *
     * @return the peptide to protein mapping: peptide sequence -> protein
     * accession -> index in the protein An empty map if not
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence, MatchingType matchingType, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        long time0 = 0;
        if (debugSpeed) {
            time0 = System.currentTimeMillis();
        }
        HashMap<String, HashMap<String, ArrayList<Integer>>> result = getProteinMapping(peptideSequence, matchingType, massTolerance, false);
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
     *
     * @return the peptide to protein mapping: Accession -> list of indexes
     * where the peptide can be found on the sequence
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    private HashMap<String, HashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence, MatchingType matchingType, Double massTolerance, boolean reversed) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        if (matchingType != matchingTypeInCache || matchingType == MatchingType.indistiguishibleAminoAcids && (massToleranceInCache == null || !massToleranceInCache.equals(massTolerance))) {
            //@TODO adapt the cache to the different matching types
            emptyCache();
            matchingTypeInCache = matchingType;
            massToleranceInCache = massTolerance;
        }

        HashMap<String, HashMap<String, ArrayList<Integer>>> result = lastQueriedPeptidesCache.get(peptideSequence);

        if (result != null) {
            lastQueriedPeptidesCacheContent.remove(peptideSequence);
            lastQueriedPeptidesCacheContent.add(peptideSequence);
        } else {

            result = lastSlowQueriedPeptidesCache.get(peptideSequence);

            if (result != null) {
                lastSlowQueriedPeptidesCacheContent.remove(peptideSequence);
                lastSlowQueriedPeptidesCacheContent.add(peptideSequence);
            } else {

                if (sequenceFactory.isDefaultReversed()) {
                    String reversedSequence = SequenceFactory.reverseSequence(peptideSequence);
                    result = lastQueriedPeptidesCache.get(reversedSequence);
                    if (result != null) {
                        lastQueriedPeptidesCacheContent.remove(peptideSequence);
                        lastQueriedPeptidesCacheContent.add(peptideSequence);
                    } else {

                        result = lastSlowQueriedPeptidesCache.get(reversedSequence);

                        if (result != null) {
                            lastSlowQueriedPeptidesCacheContent.remove(peptideSequence);
                            lastSlowQueriedPeptidesCacheContent.add(peptideSequence);
                        }
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

                ArrayList<String> initialTags = getInitialTags(peptideSequence, matchingType, massTolerance);

                for (String tag : initialTags) {
                    Node node = getNode(tag);
                    if (node != null) {
                        HashMap<String, HashMap<String, ArrayList<Integer>>> tagResults = node.getProteinMapping(peptideSequence, matchingType, massTolerance);
                        for (String tagSequence : tagResults.keySet()) {
                            HashMap<String, ArrayList<Integer>> mapping = result.get(tagSequence), tagMapping = tagResults.get(tagSequence);
                            if (mapping == null && !tagMapping.isEmpty()) {
                                result.put(tagSequence, tagMapping);
                            } else {
                                for (String tagAccession : tagMapping.keySet()) {
                                    ArrayList<Integer> indexes = mapping.get(tagAccession),
                                            tagIndexes = tagMapping.get(tagAccession);
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
                        reversedResult = getProteinMapping(reversedSequence, matchingType, massTolerance, true);
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
                if (!reversed) {
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
                    }
                }
            }
        }
        return result;
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
    private ArrayList<String> getInitialTags(String peptideSequence, MatchingType matchingType, Double massTolerance) throws SQLException, IOException, ClassNotFoundException {

        int initialTagSize = componentsFactory.getInitialSize();
        ArrayList<String> tempTags, result = new ArrayList<String>();

        if (matchingType == MatchingType.string) {
            result.add(peptideSequence.substring(0, initialTagSize));
            return result;
        }
        for (int i = 0; i < initialTagSize; i++) {
            char aa = peptideSequence.charAt(i);
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            if (aminoAcid == null) {
                throw new IllegalArgumentException("Unknown amino acid " + aa + " found in peptide sequence " + peptideSequence + ".");
            }
            tempTags = new ArrayList<String>(result);
            result.clear();
            if (tempTags.isEmpty()) {
                for (char newAa : aminoAcid.getSubAminoAcids()) {
                    String newTag = newAa + "";
                    if (!result.contains(newTag)) {
                        result.add(newTag);
                    }
                }
                for (char newAa : aminoAcid.getCombinations()) {
                    String newTag = newAa + "";
                    if (!result.contains(newTag)) {
                        result.add(newTag);
                    }
                }
                if (matchingType == MatchingType.indistiguishibleAminoAcids) {
                    for (char newAa : aminoAcid.getIndistinguishibleAminoAcids(massTolerance)) {
                        String newTag = newAa + "";
                        if (!result.contains(newTag)) {
                            result.add(newTag);
                        }
                    }
                }
            } else {
                for (String sequence : tempTags) {
                    for (char newAa : aminoAcid.getSubAminoAcids()) {
                        String newTag = sequence + newAa;
                        if (!result.contains(newTag)) {
                            result.add(newTag);
                        }
                    }
                    for (char newAa : aminoAcid.getCombinations()) {
                        String newTag = sequence + newAa;
                        if (!result.contains(newTag)) {
                            result.add(newTag);
                        }
                    }
                    if (matchingType == MatchingType.indistiguishibleAminoAcids) {
                        for (char newAa : aminoAcid.getIndistinguishibleAminoAcids(massTolerance)) {
                            String newTag = sequence + newAa;
                            if (!result.contains(newTag)) {
                                result.add(newTag);
                            }
                        }
                    }
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
    private HashMap<String, HashMap<String, ArrayList<Integer>>> getReversedResults(HashMap<String, HashMap<String, ArrayList<Integer>>> forwardResults) throws SQLException, ClassNotFoundException, IOException {
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
                    proteinLength = componentsFactory.getProteinLength(newAccession);
                    if (proteinLength == null) {
                        throw new IllegalArgumentException("Length of protein " + newAccession + " not found.");
                    }
                } else {
                    newAccession = SequenceFactory.getDefaultDecoyAccession(accession);
                    proteinLength = componentsFactory.getProteinLength(accession);
                    if (proteinLength == null) {
                        throw new IllegalArgumentException("Length of protein " + accession + " not found.");
                    }
                }
                ArrayList<Integer> reversedIndexes = new ArrayList<Integer>();
                for (int index : forwardResults.get(sequence).get(accession)) {
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
    private Node getNode(String tag) throws SQLException, ClassNotFoundException, IOException {
        Node result = tree.get(tag);
        if (result == null) {
            result = componentsFactory.getNode(tag);
            if (result != null) {
                long capacity = memoryAllocation * cacheScale;
                while (treeSize > capacity && !tagsInTree.isEmpty()) {
                    int index = tagsInTree.size() - 1;
                    String tempTag = tagsInTree.get(index);
                    Node tempNode = tree.get(tempTag);
                    treeSize -= tempNode.getSize();
                    tree.remove(tempTag);
                    tagsInTree.remove(index);
                }
                tree.put(tag, result);
                treeSize += result.getSize();
                tagsInTree.add(0, tag);
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
     *
     * @return a list of peptides matched and their indexes in the protein
     * sequence
     * @throws IOException
     * @throws InterruptedException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public HashMap<String, ArrayList<Integer>> getMatchedPeptideSequences(String peptideSequence, String proteinAccession, MatchingType matchingType, Double massTolerance)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        HashMap<String, HashMap<String, ArrayList<Integer>>> mapping = getProteinMapping(peptideSequence, matchingType, massTolerance);
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
     */
    public PeptideIterator getPeptideIterator() throws SQLException, IOException, ClassNotFoundException {
        return new PeptideIterator();
    }

    /**
     * Notifies the tree that a runnable has finished working
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
        private PeptideIterator() throws SQLException, IOException, ClassNotFoundException {
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
     * Runnable used for the indexing of a protein sequence.
     */
    private class SequenceIndexer implements Runnable {

        /**
         * The accession of the protein being indexed
         */
        private String accession;
        /**
         * The protein sequence to index
         */
        private String proteinSequence;
        /**
         * Boolean indicating whether the thread shall be interrupted.
         */
        private boolean finished = false;
        /**
         * List of tags to inspect
         */
        private ArrayList<String> tags;
        /**
         * the enzyme to use
         */
        private Enzyme enzyme;
        /**
         * The result of the indexing
         */
        private HashMap<String, ArrayList<Integer>> indexes = null;

        /**
         * Constructor
         *
         * @param proteinSequence the protein sequence
         * @param tags the tags to inspect
         * @param enzyme the enzyme to use, can be null
         */
        public SequenceIndexer(String accession, String proteinSequence, ArrayList<String> tags, Enzyme enzyme) {
            this.accession = accession;
            this.proteinSequence = proteinSequence;
            this.tags = tags;
            this.enzyme = enzyme;
        }

        @Override
        public synchronized void run() {
            try {
                indexes = getTagToIndexesMap(proteinSequence, tags, enzyme);
            } catch (SQLException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            }
            finished = true;
            try {
                runnableFinished();
            } catch (InterruptedException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
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
         * returns the indexes
         *
         * @return the indexes
         */
        public HashMap<String, ArrayList<Integer>> getIndexes() {
            return indexes;
        }

        /**
         * Returns the accession of the protein being indexed
         *
         * @return the accession of the protein being indexed
         */
        public String getAccession() {
            return accession;
        }

        /**
         * Returns all the positions of the given tags on the given sequence in
         * a map: tag -> list of indexes in the sequence.
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

                if (enzyme == null || i == 0 || enzyme.isCleavageSite(sequence.charAt(i - 1), sequence.charAt(i))) {
                    char[] tagValue = new char[initialTagSize]; // @TODO: possible to not create a new char array every time? replace my StringBuilder?
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
    }

    /**
     * Runnable used to process raw nodes and store them in the database.
     */
    private class RawNodeProcessor implements Runnable {

        /**
         * The tag of the node
         */
        private String tag;
        /**
         * The node
         */
        private Node node;
        /**
         * the max node size
         */
        private int maxNodeSize;
        /**
         * The max peptide size
         */
        private int maxPeptideSize;
        /**
         * Boolean indicating whether the thread shall be interrupted.
         */
        private boolean finished = false;

        /**
         * Constructor
         *
         * @param tag the tag of interest
         * @param node the node to process
         */
        public RawNodeProcessor(String tag, Node node, int maxNodeSize, int maxPeptideSize) {
            this.tag = tag;
            this.node = node;
        }

        @Override
        public synchronized void run() {
            try {
                node.splitNode(maxNodeSize, maxPeptideSize);
                componentsFactory.saveNode(tag, node);
            } catch (IOException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
            }
            finished = true;
            try {
                runnableFinished();
            } catch (InterruptedException ex) {
                Logger.getLogger(ProteinTree.class.getName()).log(Level.SEVERE, null, ex);
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
    }
}
