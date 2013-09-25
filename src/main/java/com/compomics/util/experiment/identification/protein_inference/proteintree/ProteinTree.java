package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.TagFactory;
import com.compomics.util.experiment.identification.matches.ProteinMatch.MatchingType;
import com.compomics.util.experiment.identification.protein_inference.proteintree.treebuilder.TreeEnt;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class sorts the proteins into groups.
 *
 * @author Marc Vaudel
 * @author Kenneth Verheggen
 */
public class ProteinTree extends ConcurrentHashMap<String, Node> {

    /**
     * The memory allocation in MB.
     */
    private int memoryAllocation;
    /**
     * Approximate number of accession*node one can store in a GB of memory
     * (empirical value).
     */
    private static final long cacheScale = 12000;
    /**
     * Instance of the sequence factory.
     */
    private final SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * List of the nodes in tree (ie in memory).
     */
    private ArrayList<String> tagsInTree = new ArrayList<String>();
    /**
     * The size of the tree in memory in accession*node.
     */
    private long treeSize = 0;
    /**
     * Indicates whether a debug file with speed metrics shall be created.
     */
    private boolean debugSpeed = false;
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
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>> lastQueriedPeptidesCache 
            = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>>(cacheSize);
    /**
     * Peptide sequences in cache.
     */
    private ArrayList<String> lastQueriedPeptidesCacheContent = new ArrayList<String>(cacheSize);
    /**
     * Time in ms after which a query is considered as slow.
     */
    private int queryTimeThreshold = 20;
    /**
     * Cache of the last queried peptides where the query took long.
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>> lastSlowQueriedPeptidesCache 
            = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>>(cacheSize);
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
                debugSpeedWriter = new BufferedWriter(new FileWriter(new File("treeSpeed.txt")));
                debugSpeedWriter.write("sequence\tnPeptides\tnProteins\tQuery time\tslow cache usage\tcache usage\tDB query");
                debugSpeedWriter.newLine();
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

        this.clear();
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
            int cores = Runtime.getRuntime().availableProcessors();
            if (cores > 1) {
                //remove a core from the calculations for time due to overhead etc...
                cores = cores - 1;
            }
            //what if i want pentameres?
            if (initialTagSize == 3 || initialTagSize == 4) {
                String report = "Expected import time: ";
                int nSeconds;
                if (initialTagSize == 3) {
                    nSeconds = ((sequenceFactory.getNTargetSequences() / (cores)) * 15 / 1000);
                } else {
                    nSeconds = ((sequenceFactory.getNTargetSequences()) / cores) * 2 / 10;
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

        String[] tags = TagFactory.getAminoAcidCombinations(initialTagSize);
        Set<String> accessions = new HashSet<String>();

        if (sequenceFactory.isDefaultReversed()) {
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

        if (ratio == 0) {
            ratio = 1;
        }

        int nPassages = ratio;
        if (tags.length % ratio != 0) {
            nPassages += 1;
        }

        int nTags = tags.length / ratio;
        if (nTags == 0) {
            nTags = 1;
        }
        /*
         if (nPassages > 1) {
         //            Collections.shuffle(tags);
         }
         */
        if (debugSpeed) {
            debugSpeedWriter.write("Critical size: " + criticalSize);
            System.out.println("Critical size: " + criticalSize);
            estimatedTreeSize = estimatedTreeSize / 100;
            debugSpeedWriter.write("Estimated tree size: " + estimatedTreeSize);
            System.out.println("Estimated tree size: " + estimatedTreeSize);
            debugSpeedWriter.write(new Date() + " " + nPassages + " passages needed (" + nTags + " tags of " + tags.length + " per passage)");
            System.out.println(new Date() + " " + nPassages + " passages needed (" + nTags + " tags of " + tags.length + " per passage)");
            debugSpeedWriter.newLine();
            debugSpeedWriter.flush();
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(false);
            int totalProgress = (int) (nPassages * accessions.size() + tags.length); // @TODO: for some reason this now only goes to 75%...
            waitingHandler.setMaxSecondaryProgressCounter(totalProgress);
            waitingHandler.setSecondaryProgressCounter(0);
        }

        long time0 = System.currentTimeMillis();
        ArrayList<String> loadedAccessions = new ArrayList<String>();

        int arrayPointer = 0;
        String[] bufferedArray = new String[nTags];
        for (int i = 0; i < nPassages; i++) {
            System.arraycopy(tags, arrayPointer, bufferedArray, 0, nTags);
            arrayPointer = arrayPointer + nTags;
            loadTags(bufferedArray, accessions, waitingHandler, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, loadedAccessions);
        }
        if (arrayPointer != tags.length) {
            System.arraycopy(tags, arrayPointer, bufferedArray, 0, ((tags.length - arrayPointer)));//array starts from 0
            loadTags(bufferedArray, accessions, waitingHandler, initialTagSize, maxNodeSize, maxPeptideSize, enzyme, loadedAccessions);
        }
        tagsInTree.addAll(this.keySet());
        for (Node node : this.values()) {
            treeSize += node.getSize();
        }

        componentsFactory.setVersion(version);
        componentsFactory.setImportComplete(true);

        if (debugSpeed) {

            long time1 = System.currentTimeMillis();
            long initiationTime = time1 - time0;

            debugSpeedWriter.write("tree initiation: " + initiationTime + " ms.");
            System.out.println("tree initiation: " + initiationTime + " ms.");
            debugSpeedWriter.write("tree size: " + this.size() + " tags, " + treeSize + " node.accession loaded and saved.");
            System.out.println("tree size: " + this.size() + " tags, " + treeSize + " node.accession loaded and saved.");
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
    private void loadTags(String[] tags, Set<String> accessions, WaitingHandler waitingHandler,
            int initialTagSize, int maxNodeSize, int maxPeptideSize, Enzyme enzyme, ArrayList<String> loadedAccessions)
            throws IOException, IllegalArgumentException, InterruptedException, ClassNotFoundException, SQLException {

        // setup queue for multithreading
        BlockingQueue<String> accessionsQueue = new LinkedBlockingQueue<String>();
        accessionsQueue.addAll(accessions);

        // setup executor service for multithreading
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        long currentTime = System.currentTimeMillis();
        if (waitingHandler != null) {
            if (availableProcessors == 1) {
                waitingHandler.appendReport("Starting " + availableProcessors + " thread to import protein accessions.", true, true);
            } else {
                waitingHandler.appendReport("Starting " + availableProcessors + " threads to import protein accessions.", true, true);
            }
        }
        if (debugSpeed) {
            System.out.println("Starting " + availableProcessors + " threads to import accessions");
        }

        //Make an array of subtrees for the threads to use?
        TreeEnt ents[] = new TreeEnt[availableProcessors];

        ExecutorService exec = Executors.newFixedThreadPool(availableProcessors);
        for (int i = 0; i < availableProcessors; i++) {
            ents[i] = new TreeEnt(this, waitingHandler, loadedAccessions, tags, enzyme, initialTagSize, maxNodeSize, maxPeptideSize);
            exec.submit(ents[i].getAccessionLoader(accessionsQueue));
        }
        // notify executor that the threads should be started
        exec.shutdown();
        while (exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS) & !accessionsQueue.isEmpty()) {
            //  wait for all threads to finish = as soon as the queue is empty...
            Thread.sleep(500);
        }
        if (waitingHandler != null) {
            waitingHandler.appendReport("Building peptide to protein map.", true, true);
        }
        if (debugSpeed) {
            System.out.println("Building peptide to protein map (" + tags.length + " leaves)");
        }
        //merge back into one tree
        if (waitingHandler != null) {
            waitingHandler.appendReport("Cleaning peptide to protein map.", true, true);
        }
        if (debugSpeed) {
            System.out.println("Merging ents into parent tree");
        }
        for (int i = 0; i < availableProcessors; i++) {
            this.putAll(ents[i]);
        }
        if (debugSpeed) {
            System.out.println("Done : parent contains " + this.size() + " tags");
        }
        // setup queue for multithreading
        BlockingQueue<String> tagsQueue = new LinkedBlockingQueue<String>(this.keySet());

        exec = Executors.newFixedThreadPool(availableProcessors);
        if (debugSpeed) {
            System.out.println("Saving tags");
        }
        for (int i = 0; i < availableProcessors; i++) {
            exec.submit(ents[i].getTagSaver(tagsQueue));
        }
        //shut down the executor service now
        exec.shutdown();
        while (!tagsQueue.isEmpty()) {
            // wait for all threads to finish = as soon as the queue is empty...
            Thread.sleep(500);
        }

        currentTime = System.currentTimeMillis() - currentTime;
        long hours = TimeUnit.MILLISECONDS.toHours(currentTime);
        currentTime -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime);
        currentTime -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime);

        if (waitingHandler != null) {
            waitingHandler.appendReport("Finished importing accessions in " + hours + " hours, " + minutes + " minutes and " + seconds + " seconds!", true, true);
        }
        if (debugSpeed) {
            System.out.println("Finished importing accessions in " + hours + " hours, " + minutes + " minutes and " + seconds + " seconds!");
        }
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
    public ConcurrentHashMap<String, ArrayList<Integer>> getProteinMapping(String peptideSequence) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> mapping = getProteinMapping(peptideSequence, MatchingType.string, null);
        if (mapping.size() > 1) {
            throw new IllegalArgumentException("Different mappings found for peptide " + peptideSequence + " in string matching. Only one expected.");
        }
        ConcurrentHashMap<String, ArrayList<Integer>> result = mapping.get(peptideSequence);
        if (result != null) {
            return result;
        }
        return new ConcurrentHashMap<String, ArrayList<Integer>>();
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
    public ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence, MatchingType matchingType, Double massTolerance) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        long time0 = 0;
        boolean dbQuery = false;
        if (debugSpeed) {
            time0 = System.currentTimeMillis();
            ArrayList<String> initialTags = getInitialTags(peptideSequence, matchingType, massTolerance);
            for (String tag : initialTags) {
                if (this.containsKey(tag)) {
                    dbQuery = true;
                    break;
                }
            }
        }
        ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> result = getProteinMapping(peptideSequence, matchingType, massTolerance, false);
        if (debugSpeed) {
            long time1 = System.currentTimeMillis();
            long queryTime = time1 - time0;
            int nProteins = 0;
            for (String peptide : result.keySet()) {
                nProteins += result.get(peptide).size();
            }
            double slowCacheUsage = ((double) lastSlowQueriedPeptidesCacheContent.size()) / cacheSize;
            double cacheUsage = ((double) lastQueriedPeptidesCache.size()) / cacheSize;
            debugSpeedWriter.write(peptideSequence + "\t" + result.size() + "\t" + nProteins + "\t" + queryTime + "\t" + slowCacheUsage + "\t" + cacheUsage + "\t" + dbQuery);
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
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> getProteinMapping(String peptideSequence, MatchingType matchingType, Double massTolerance, boolean reversed) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        if (matchingType != matchingTypeInCache || matchingType == MatchingType.indistiguishibleAminoAcids && (massToleranceInCache == null || !massToleranceInCache.equals(massTolerance))) {
            //@TODO adapt the cache to the different matching types
            emptyCache();
            matchingTypeInCache = matchingType;
            massToleranceInCache = massTolerance;
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> result = lastQueriedPeptidesCache.get(peptideSequence);

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

                result = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>();

                ArrayList<String> initialTags = getInitialTags(peptideSequence, matchingType, massTolerance);

                for (String tag : initialTags) {
                    Node node = getNode(tag);
                    if (node != null) {
                        ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> tagResults = node.getProteinMapping(peptideSequence, matchingType, massTolerance);
                        for (String tagSequence : tagResults.keySet()) {
                            ConcurrentHashMap<String, ArrayList<Integer>> mapping = result.get(tagSequence), tagMapping = tagResults.get(tagSequence);
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
                    ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> reversedResult;
                    if (!reversedSequence.equals(peptideSequence)) {
                        reversedResult = getProteinMapping(reversedSequence, matchingType, massTolerance, true);
                        reversedResult = getReversedResults(reversedResult);
                    } else {
                        reversedResult = getReversedResults(result);
                    }
                    for (String tempReversedSequence : reversedResult.keySet()) {
                        ConcurrentHashMap<String, ArrayList<Integer>> mapping = result.get(tempReversedSequence);
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
                throw new IllegalArgumentException("Unknown amino-acid " + aa + " found in peptide sequence " + peptideSequence + ".");
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
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> getReversedResults(ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> forwardResults) throws SQLException, ClassNotFoundException, IOException {
        ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> results = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>(forwardResults.keySet().size());
        for (String sequence : forwardResults.keySet()) {
            int peptideLength = sequence.length();
            String reversedSequence = SequenceFactory.reverseSequence(sequence);
            ConcurrentHashMap<String, ArrayList<Integer>> mapping = new ConcurrentHashMap<String, ArrayList<Integer>>(forwardResults.get(sequence).size());
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

        Node result = this.get(tag);
        if (result == null) {
            result = componentsFactory.getNode(tag);
            //@TODO: tried to only add arraylists when needed, I think this is causing this error...
            // The result can technically never be null anymore, it can be empty however !
            if (result.isEmpty()) {
                throw new IllegalArgumentException("Tag " + tag + " not found in database.");
            }

            long capacity = memoryAllocation * cacheScale;

            ArrayList<String> tempTags = new ArrayList<String>();
            while (treeSize > capacity && !this.keySet().isEmpty()) {
                int index = tagsInTree.size() - 1;
                String tempTag = tagsInTree.get(index);
                Node tempNode = this.get(tempTag);
                treeSize -= tempNode.getSize();
                tempTags.add(tempTag);
                tagsInTree.remove(index);
            }
            synchronized (this) {
                this.keySet().removeAll(tempTags);
                this.put(tag, result);
            }
            treeSize += result.getSize();
            tagsInTree.add(0, tag);
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
        this.clear();
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
    public ConcurrentHashMap<String, ArrayList<Integer>> getMatchedPeptideSequences(String peptideSequence, String proteinAccession, MatchingType matchingType, Double massTolerance)
            throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> mapping = getProteinMapping(peptideSequence, matchingType, massTolerance);
        ConcurrentHashMap<String, ArrayList<Integer>> tempMapping, result = new ConcurrentHashMap<String, ArrayList<Integer>>();

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
        private String[] tags;
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
        private TreeSet<Character> aas = null;
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
            ArrayList<Character> aasAsList = new ArrayList<Character>();
            try {
                if (currentNode != null && currentNode.getDepth() == initialTagSize && currentNode.getAccessions() != null && i < tags.length - 1) {
                    // ok we're done with this node
                    parentNode = null;
                    aas = null;
                    j = 0;
                    currentSequence = tags[++i];
                    currentNode = getNode(currentSequence);
                }
                while (++i < tags.length && currentNode == null && parentNode == null) {
                    currentSequence = tags[i];
                    currentNode = getNode(currentSequence);
                }
                if (i < tags.length) {
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
                                aas = new TreeSet<Character>(parentNode.getSubtree().keySet()) {
                                };
                                j = aas.headSet(aa).size();
                            }
                            return hasNext();
                        }

                        aasAsList.addAll(aas);
                        char aa = aasAsList.get(j);
                        currentSequence += aa;
                        currentNode = parentNode.getSubtree().get(aa);
                    }
                    while (currentNode.getAccessions() == null) {
                        j = 0;
                        aas = new TreeSet<Character>(currentNode.getSubtree().keySet());
                        parentNode = currentNode;
                        if (!aas.isEmpty()) {
                            char aa = aasAsList.get(j);
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
        public ConcurrentHashMap<String, ArrayList<Integer>> getMapping() {
            if (currentNode != null) {
                return currentNode.getAccessions();
            } else {
                return parentNode.getTermini();
            }
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Node> m) {
        Node motherNode;
        for (String aTag : m.keySet()) {
            if (!this.contains(m)) {
                synchronized (this) {
                    this.put(aTag, m.get(aTag));
                }
            } else {
                motherNode = this.get(aTag);
                synchronized (this) {
                    motherNode.getAccessions().putAll(m.get(aTag).getAccessions());
                }
                synchronized (this) {
                    motherNode.getTermini().putAll(m.get(aTag).getTermini());
                }
            }
        }
    }
}
