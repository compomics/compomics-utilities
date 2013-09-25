/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.proteintree.treebuilder;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.Node;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTreeComponentsFactory;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Kenneth
 */
public class TreeEnt extends ConcurrentHashMap<String, Node> {

    /**
     * Instance of the sequence factory.
     */
    private final SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * Instance of the proteintreecomponents factory.
     */
    private final ProteinTreeComponentsFactory componentsFactory;
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
     * The maximum node size.
     */
    private final int maxNodeSize;
    /**
     * The maximum peptide size.
     */
    private final int maxPeptideSize;
    /**
     * The tree that this ent will be planted on.
     */
    private final ProteinTree parentTree;

    public TreeEnt(ProteinTree parentTree, WaitingHandler waitingHandler, ArrayList<String> loadedAccessions, String[] tags, Enzyme enzyme, int initialTagSize, int maxNodeSize, int maxPeptideSize) throws IOException {
        this.componentsFactory = ProteinTreeComponentsFactory.getInstance();
        this.waitingHandler = waitingHandler;
        this.loadedAccessions = loadedAccessions;
        this.tags = tags;
        this.enzyme = enzyme;
        this.initialTagSize = initialTagSize;
        this.maxNodeSize = maxNodeSize;
        this.maxPeptideSize = maxPeptideSize;
        this.parentTree = parentTree;
    }

    public AccessionLoader getAccessionLoader(BlockingQueue accessionsQueue) throws IOException {
        return new AccessionLoader(this, accessionsQueue, waitingHandler, loadedAccessions, tags, enzyme, initialTagSize);
    }

    public TagSaver getTagSaver(BlockingQueue<String> tagsQueue) throws IOException {
        return new TagSaver(parentTree, tagsQueue, maxNodeSize, maxPeptideSize, waitingHandler);
    }
}
