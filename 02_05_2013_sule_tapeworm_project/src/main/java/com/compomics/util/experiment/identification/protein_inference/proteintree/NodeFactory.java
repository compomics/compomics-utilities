package com.compomics.util.experiment.identification.protein_inference.proteintree;

import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This factory provides protein trees node contents from indexed text files.
 *
 * @author Marc Vaudel
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
     * The folder containing the indexed files.
     */
    private File folder;
    /**
     * Random access file of the current index file.
     */
    private BufferedRandomAccessFile currentRandomAccessFile = null;
    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * The line separator.
     */
    private final String lineSeparator = System.getProperty("line.separator");
    /**
     * Boolean indicating whether the factory is in debug mode.
     */
    private boolean debug = true;

    /**
     * Constructor.
     */
    private NodeFactory() {
    }

    /**
     * Static method returning the instance of the factory and setting the
     * serialization folder.
     *
     * @param folder the serialization folder
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
     * Static method returning the instance of the factory. Note: the
     * serialization folder should have been already set.
     *
     * @return the instance of the factory
     */
    public static NodeFactory getInstance() {
        if (instance == null) {
            instance = new NodeFactory();
        }
        return instance;
    }

    /**
     * The folder containing the index files.
     *
     * @param folder
     */
    private void setFolder(File folder) {
        this.folder = folder;
    }

    /**
     * Initiates the factory on the sequence factory.
     *
     * @throws IOException
     */
    public void initiateFactory() throws IOException {
        if (currentRandomAccessFile != null) {
            currentRandomAccessFile.close();
        }
        File destinationFile = getDestinationFile();
        if (destinationFile.exists()) {
            try {
                destinationFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentRandomAccessFile = new BufferedRandomAccessFile(getDestinationFile(), "rw", 1024 * 100);
    }

    /**
     * Saves all the downstream accessions.
     *
     * @param node the node
     * @param emptyNode boolean indicating whether the node should be emptied
     * (if true the accession mapping will be cleared)
     * @throws IOException
     */
    public void saveAccessions(Node node, boolean emptyNode) throws IOException {
        Long nodeIndex = currentRandomAccessFile.length();
        currentRandomAccessFile.seek(nodeIndex);
        if (node.getSubtree() == null) {
            HashMap<String, ArrayList<Integer>> accessions = node.getAccessions();
            for (String accession : accessions.keySet()) {
                currentRandomAccessFile.writeBytes(accession + separator);
                for (int index : accessions.get(accession)) {
                    currentRandomAccessFile.writeBytes(index + separator);
                }
            }
            currentRandomAccessFile.writeBytes(lineSeparator);
            node.setIndex(nodeIndex);
            if (emptyNode) {
                node.clearAccessions();
            }
        } else {
            for (Node subNode : node.getSubtree().values()) {
                saveAccessions(subNode, emptyNode);
            }
        }
    }

    /**
     * Returns the accessions at a given index.
     *
     * @param index the index of the node
     * @return the accessions at a given index, null if not found
     * @throws IOException
     */
    public HashMap<String, ArrayList<Integer>> getAccessions(long index) throws IOException {
        currentRandomAccessFile.seek(index);
        String line = currentRandomAccessFile.getNextLine();
        if (line == null) {
            // prevent bug in the ebi BufferedRandomAccessFile
            currentRandomAccessFile.seek(currentRandomAccessFile.length());
            currentRandomAccessFile.seek(index);
            line = currentRandomAccessFile.getNextLine();
            if (line == null) {
                throw new IllegalArgumentException("Node at index " + index + " not found.");
            }
        }
        HashMap<String, ArrayList<Integer>> accessions = new HashMap<String, ArrayList<Integer>>();
        String[] split = line.split(separator);
        ArrayList<Integer> indices = new ArrayList<Integer>();
        String accession = "";
        for (String part : split) {
            if (!part.equals("")) {
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
        }
        accessions.put(accession, indices);
        return accessions;
    }

    /**
     * Returns the destination file where to read/write the nodes.
     *
     * @return the destination file where to read/write the nodes
     */
    private File getDestinationFile() {
        return new File(folder, sequenceFactory.getFileName() + ".cpi");
    }

    /**
     * Closes the factory, closes all connection and deletes the file.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (currentRandomAccessFile != null) {
            currentRandomAccessFile.close();
        }
        File destinationFile = getDestinationFile();
        if (destinationFile.exists()) {
            destinationFile.delete();
        }
    }
}
