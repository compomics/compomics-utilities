package com.compomics.util.experiment.identification.protein_inference.proteintree.kenneth;

import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.TagFactory;
import com.compomics.util.experiment.identification.matches.ProteinMatch.MatchingType;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class sorts the proteins into groups.
 *
 * @author Marc Vaudel
 */
public class KProteinTree extends ProteinTree {

    /**
     * The result of the indexing.
     */
    protected HashMap<String, ArrayList<Integer>> indexes = new HashMap<String, ArrayList<Integer>>();
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
    private int accessionsProcessed;
    private static final ConcurrentHashMap<String, String> currentBufferedSequences = new ConcurrentHashMap<String, String>();
    private String[] accessionsInDb;

    /**
     * Creates a tree based on the proteins present in the sequence factory.
     *
     * @param memoryAllocation the number of MB available for the tree in
     * memory.
     * @throws IOException
     */
    public KProteinTree(int memoryAllocation) throws IOException {
        super(memoryAllocation);
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
    public ConcurrentHashMap<char[], HashMap<String, ArrayList<Integer>>> getTagToIndexes(int batchSize) {
        long t0 = System.currentTimeMillis();
        System.out.println("Building sequenceFactory for now...Later we could put the fasta reads directly in here");
        accessionsInDb = new String[sequenceFactory.getAccessions().size()];
        Iterator it = sequenceFactory.getAccessions().iterator();
        int i = 0;
        while (it.hasNext()) {
            accessionsInDb[i] = (String) it.next();
            i++;
        }
        ConcurrentHashMap<char[], HashMap<String, ArrayList<Integer>>> tagToIndexesMap = new ConcurrentHashMap<char[], HashMap<String, ArrayList<Integer>>>();
        try {
            System.out.println("Making all combo's possible");
            ArrayList<String> tags = TagFactory.getAminoAcidCombinations(3);
            System.out.println("OPERATION : " + accessionsInDb.length + " accessions covered by " + tags.size() + " tags.");
            System.out.println("Starting threads...");
            while (accessionsProcessed < accessionsInDb.length) {
                System.out.println("Buffering " + batchSize + " sequences");
                bufferSequences(batchSize);
                BlockingQueue<String> tagQueue = new ArrayBlockingQueue<String>(tags.size());
                tagQueue.addAll(tags);
                ExecutorService exec = Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors()));
                Future[] myFutures = new Future[Runtime.getRuntime().availableProcessors()];
                for (int cores = 0; cores < Runtime.getRuntime().availableProcessors(); i++) {
                    exec.submit(new TagFinder(tagQueue));
                }
                exec.shutdown();
                for (Future aFuture : myFutures) {
                    tagToIndexesMap.putAll((Map<? extends char[], ? extends HashMap<String, ArrayList<Integer>>>) aFuture.get());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(KProteinTree.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(KProteinTree.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(KProteinTree.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(KProteinTree.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Processing time : " + (System.currentTimeMillis() - t0) + "ms");
            System.out.println(tagToIndexesMap.size());
            return tagToIndexesMap;
        }

    }

    private ConcurrentHashMap<String, String> bufferSequences(int batchSize) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException,
            ClassNotFoundException {
        currentBufferedSequences.clear();
        if (accessionsInDb == null || accessionsInDb.length == 0) {
            accessionsInDb = sequenceFactory.getAccessions().toArray(accessionsInDb);
        }
        //load batch of sequences in memory
        if (batchSize > (accessionsInDb.length - accessionsProcessed)) {
            batchSize = accessionsInDb.length - accessionsProcessed;
        }
        for (int i = accessionsProcessed; i <= (accessionsProcessed + batchSize - 1); i++) {
            currentBufferedSequences.put(accessionsInDb[i], sequenceFactory.getProtein(accessionsInDb[i]).getSequence());
        }
        accessionsProcessed += batchSize;
        return currentBufferedSequences;
    }

    private class TagFinder implements Callable<ConcurrentHashMap<char[], HashMap<String, ArrayList<Integer>>>> {

        private final Queue<String> tagQueue;
        private char[] tag;
        private final HashMap<String, ArrayList<Integer>> accessionOccurenceMap = new HashMap<String, ArrayList<Integer>>();
        private final ConcurrentHashMap<char[], HashMap<String, ArrayList<Integer>>> finderTagToIndexesMap = new ConcurrentHashMap<char[], HashMap<String, ArrayList<Integer>>>();

        private TagFinder(Queue tagQueue) {
            this.tagQueue = tagQueue;
        }

        @Override
        public ConcurrentHashMap<char[], HashMap<String, ArrayList<Integer>>> call() throws Exception {
            char[] sequence;
            ArrayList<Integer> occurences;
            long t0;
            while (!tagQueue.isEmpty()) {
                t0 = System.currentTimeMillis();
                tag = tagQueue.poll().toCharArray();
                System.out.println("Thread : " + Thread.currentThread().getId() + " is working on tag " + new String(tag));
                for (String accession : currentBufferedSequences.keySet()) {
                    occurences = new ArrayList<Integer>();
                    sequence = currentBufferedSequences.get(accession).toCharArray();
                    for (int i = 0; i < (sequence.length - (tag.length - 1)); i++) {
                        char[] subArray = Arrays.copyOfRange(sequence, i, i + tag.length);
                        if (tag == subArray) {
                            occurences.add(i);
                        }
                    }
                    if (!occurences.isEmpty()) {
                        accessionOccurenceMap.put(accession, occurences);
                    }
                }
                finderTagToIndexesMap.put(tag, accessionOccurenceMap);
                accessionOccurenceMap.clear();
                System.out.println("Thread : " + Thread.currentThread().getId() + " is done with tag " + new String(tag) + "in " + (System.currentTimeMillis() - t0) + " ms");
            }
            return finderTagToIndexesMap;
        }
    }
    ///////////////////////TODO CLEANUP THESE
}
