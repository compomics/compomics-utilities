package com.compomics.util.experiment.identification.protein_inference.proteintree.treebuilder;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.Node;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTreeComponentsFactory;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AccessionLoader class.
 *
 * @author Kenneth Verheggen
 */
public class AccessionLoader implements Runnable {

    /**
     * Instance of the sequence factory.
     */
    private final SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * Instance of the proteintreecomponents factory.
     */
    private final ProteinTreeComponentsFactory componentsFactory;
    /**
     * The accession queue.
     */
    private final BlockingQueue accessionsQueue;
    /**
     * The waiting handler.
     */
    private final WaitingHandler waitingHandler;
    /**
     * The list of loaded accessions.
     */
    private final ArrayList<String> loadedAccessions;
    /**
     * The tags.
     */
    private final String[] tags;
    /**
     * The enzyme.
     */
    private final Enzyme enzyme;
    /**
     * The initial tag size.
     */
    private final int initialTagSize;
    /**
     * This runnable's tree (this is needed to sync on)
     */
    private final ConcurrentHashMap<String, Node> subTree;

    /**
     * Constructor.
     *
     * @param subTree the sub tree
     * @param accessionsQueue the accession queue
     * @param waitingHandler the waiting handler
     * @param loadedAccessions the list of loaded accessions
     * @param tags the tags
     * @param enzyme the enzyme
     * @param initialTagSize the initial tag size
     */
    public AccessionLoader(ConcurrentHashMap<String, Node> subTree, BlockingQueue<String> accessionsQueue, WaitingHandler waitingHandler, ArrayList<String> loadedAccessions, String[] tags, Enzyme enzyme, int initialTagSize) throws IOException {
        this.accessionsQueue = accessionsQueue;
        this.waitingHandler = waitingHandler;
        this.loadedAccessions = loadedAccessions;
        this.tags = tags;
        this.enzyme = enzyme;
        this.initialTagSize = initialTagSize;
        this.subTree = subTree;
        this.componentsFactory = ProteinTreeComponentsFactory.getInstance();
    }

    @Override
    public void run() {

        HashMap<String, ArrayList<Integer>> tagToIndexesMap = new HashMap<String, ArrayList<Integer>>(tags.length);

        while (!accessionsQueue.isEmpty()) {

            String accession = (String) accessionsQueue.poll();

            try {
                String sequence;
                // synchronized (sequenceFactory) {
                sequence = sequenceFactory.getProtein(accession).getSequence();
                //    }
                if (!loadedAccessions.contains(accession)) {
                    componentsFactory.saveProteinLength(accession, sequence.length());
                    loadedAccessions.add(accession);
                }

                // reuse the same map = lower memory footprint
                tagToIndexesMap = getTagToIndexesMap(sequence, enzyme, tagToIndexesMap);

                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    }
                }
                for (String tag : tagToIndexesMap.keySet()) {
                    Node node = subTree.get(tag);
                    if (node == null) {
                        node = new Node(initialTagSize);
                        subTree.put(tag, node);
                    }
                    node.addAccession(accession, tagToIndexesMap.get(tag));
                }

                tagToIndexesMap.clear();
                if (waitingHandler != null) {
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    }
                    waitingHandler.increaseSecondaryProgressCounter();
                }
                /* } catch (IOException ex) {
                 accessionsQueue.offer(accession);
                 ex.printStackTrace();
                 } catch (IllegalArgumentException ex) {
                 accessionsQueue.offer(accession);
                 ex.printStackTrace();
                 } catch (InterruptedException ex) {
                 accessionsQueue.offer(accession);
                 ex.printStackTrace();
                 } catch (ClassNotFoundException ex) {
                 accessionsQueue.offer(accession);
                 ex.printStackTrace();
                 } catch (SQLException ex) {
                 accessionsQueue.offer(accession);
                 ex.printStackTrace();*/
            } catch (Exception e) {
                e.printStackTrace();
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
    private HashMap<String, ArrayList<Integer>> getTagToIndexesMap(String sequence, Enzyme enzyme, HashMap<String, ArrayList<Integer>> tagToIndexesMap) throws SQLException, IOException, ClassNotFoundException {

        Integer initialTagSize = componentsFactory.getInitialSize();

        // trim the map to improve memory efficiency
        tagToIndexesMap = new HashMap<String, ArrayList<Integer>>(tagToIndexesMap);
        ArrayList<Integer> indexes;

        //@TODO: only add what is required to use !!!! ---> will definatly speed up iterations later !
        for (int i = 0; i < sequence.length() - initialTagSize; i++) {
            if (enzyme == null || i == 0 || enzyme.isCleavageSite(sequence.charAt(i - 1), sequence.charAt(i))) {
                String tag = sequence.substring(i, i + initialTagSize);
                if (!tagToIndexesMap.containsKey(tag)) {
                    indexes = new ArrayList<Integer>();
                    tagToIndexesMap.put(tag, indexes);
                } else {
                    indexes = tagToIndexesMap.get(tag);
                }
                indexes.add(i);
            }
        }
        return tagToIndexesMap;
    }
}
