package com.compomics.util.experiment.identification.protein_inference.proteintree.treebuilder;

import com.compomics.util.experiment.identification.protein_inference.proteintree.Node;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTreeComponentsFactory;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Private class for storing tags.
 *
 * @author Kenneth Verheggen
 */
public class TagSaver implements Runnable {

    /**
     * Instance of the proteintreecomponents factory.
     */
    private final ProteinTreeComponentsFactory componentsFactory;
    /**
     * The waiting handler.
     */
    private final WaitingHandler waitingHandler;
    /**
     * The maximum node size.
     */
    private final int maxNodeSize;
    /**
     * The maximum peptide size.
     */
    private final int maxPeptideSize;
    /**
     * This runnable's tree (this is needed to sync on).
     */
    private final ProteinTree parentTree;
    /**
     * The blocking queue.
     */
    private final BlockingQueue<String> tagsQueue;

    /**
     * Constructor.
     *
     * @param parentTree the parent tree
     * @param tagsQueue the tag queue
     * @param maxNodeSize the maximum node size
     * @param maxPeptideSize the maximum peptide size
     * @param waitingHandler the waiting handler
     * @throws IOException
     */
    public TagSaver(ProteinTree parentTree, BlockingQueue<String> tagsQueue, int maxNodeSize, int maxPeptideSize, WaitingHandler waitingHandler) throws IOException {
        this.tagsQueue = tagsQueue;
        this.waitingHandler = waitingHandler;
        this.maxNodeSize = maxNodeSize;
        this.maxPeptideSize = maxPeptideSize;
        this.parentTree = parentTree;
        this.componentsFactory = ProteinTreeComponentsFactory.getInstance();
    }

    @Override
    public void run() {
        ArrayList<String> tagsToRemove = new ArrayList<String>();
        while (!tagsQueue.isEmpty()) {
            String tag = tagsQueue.poll();
            Node node = parentTree.get(tag);
            try {
                node.splitNode(maxNodeSize, maxPeptideSize);
                componentsFactory.saveNode(tag, node);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    return;
                }
                waitingHandler.increaseSecondaryProgressCounter();
            }
            if (tagsToRemove.size() >= 1000) {
                synchronized (parentTree) {
                    parentTree.keySet().removeAll(tagsToRemove);
                }
                tagsToRemove.clear();
            }
            tagsToRemove.add(tag);
        }
        if (!tagsToRemove.isEmpty()) {
            synchronized (parentTree) {
                parentTree.keySet().removeAll(tagsToRemove);
            }
            tagsToRemove.clear();
        }
    }
}
