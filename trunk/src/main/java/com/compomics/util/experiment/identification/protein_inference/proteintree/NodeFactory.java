/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This factory provides nodes to protein trees from indexed text files
 *
 * @author Marc
 */
public class NodeFactory {

    /**
     * Instance of the sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * Instance of the factory.
     */
    private static NodeFactory instance = null;
    /**
     * The folder containing the indexed files
     */
    private File folder;
    /**
     * Random access file of the current index file.
     */
    private BufferedRandomAccessFile currentRandomAccessFile = null;
    /**
     * The separator used to separate line contents
     */
    public final static String separator = "\t";
    /**
     * The line separator
     */
    private final String lineSeparator = System.getProperty("line.separator");

    /**
     * Constructor
     */
    private NodeFactory() {
    }

    /**
     * Static method returning the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static NodeFactory getInstance(File folder) {
        if (instance == null) {
            instance = new NodeFactory();
        }
        instance.setFolder(folder);
        return instance;
    }

    /**
     * The folder containing the index files
     *
     * @param folder
     */
    private void setFolder(File folder) {
        this.folder = folder;
    }

    /**
     * Initiates the factory on the sequence factory
     *
     * @throws IOException
     */
    public void initiateFactory() throws IOException {
        if (currentRandomAccessFile != null) {
            currentRandomAccessFile.close();
        }
        currentRandomAccessFile = new BufferedRandomAccessFile(getDestinationFile(), "w", 1024 * 100);
    }

    /**
     * Saves a node
     *
     * @param node
     * @return the index of the node
     */
    public long saveNode(Node node) throws IOException {
        long nodeIndex = currentRandomAccessFile.length();
        currentRandomAccessFile.seek(nodeIndex);
        currentRandomAccessFile.writeBytes(">" + node.getDepth() + separator);
        if (node.getAccessions() != null) {
            currentRandomAccessFile.writeBytes("accessions" + lineSeparator);
            HashMap<String, ArrayList<Integer>> accessions = node.getAccessions();
            for (String accession : accessions.keySet()) {
                currentRandomAccessFile.writeBytes(accession + separator);
                for (int index : accessions.get(accession)) {
                    currentRandomAccessFile.writeBytes(separator + index);
                }
            }
            currentRandomAccessFile.writeBytes(lineSeparator);
        } else {
            currentRandomAccessFile.writeBytes("indices" + lineSeparator);
            HashMap<Character, Long> subNodesIndexes = node.getSubNodesIndexes();
            for (Character aa : subNodesIndexes.keySet()) {
                currentRandomAccessFile.writeBytes(aa + subNodesIndexes.get(aa) + separator);
            }
            currentRandomAccessFile.writeBytes(lineSeparator);
        }
        currentRandomAccessFile.writeBytes(lineSeparator);
        return nodeIndex;
    }

    /**
     * Returns the node indexed by the given long.
     *
     * @param index the index of the node
     * @return the node indexed by the given long. Null if not found
     */
    public Node getNode(long index) throws IOException {
        currentRandomAccessFile.seek(index);
        String line = currentRandomAccessFile.getNextLine();
        String component = line.substring(1, line.indexOf(separator));
        int depth = new Integer(component);
        component = line.substring(line.indexOf(separator));
        if (component.equals("accessions")) {
            HashMap<String, ArrayList<Integer>> accessions = new HashMap<String, ArrayList<Integer>>();
            line = currentRandomAccessFile.getNextLine();
            String[] split = line.split(separator);
            ArrayList<Integer> indices = new ArrayList<Integer>();
            String accession = "";
            for (String part : split) {
                try {
                    int aa = new Integer(part);
                    indices.add(aa);
                } catch (Exception e) {
                    if (!accession.equals("")) {
                        accessions.put(accession, indices);
                        indices = new ArrayList<Integer>();
                    }
                    accession = part;
                }
            }
            return new Node(depth, accessions);
        } else {
            HashMap<Character, Long> subNodesIndexes = new HashMap<Character, Long>();
            line = currentRandomAccessFile.getNextLine();
            String[] split = line.split(separator);
            for (String part : split) {
                char aa = part.charAt(0);
                long nodeIndex = new Long(part.substring(1));
                subNodesIndexes.put(aa, nodeIndex);
            }
            return new Node(subNodesIndexes, depth);
        }
    }

    /**
     * Returns the destination file where to read/write the nodes
     *
     * @return the destination file where to read/write the nodes
     */
    private File getDestinationFile() {
        return new File(folder, sequenceFactory.getFileName() + ".cpi");
    }
}
