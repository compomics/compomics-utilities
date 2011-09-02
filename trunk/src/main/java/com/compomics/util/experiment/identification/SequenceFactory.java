package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.protein.Header;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * factory retrieving the information of the loaded fasta file
 *
 * @author marc
 */
public class SequenceFactory {

    /**
     * Instance of the factory
     */
    private static SequenceFactory instance = null;
    /**
     * map of the currently loaded Headers
     */
    private HashMap<String, Header> currentHeaderMap = new HashMap<String, Header>();
    /**
     * Map of the currently loaded proteins
     */
    private HashMap<String, Protein> currentProteinMap = new HashMap<String, Protein>();
    /**
     * Index of the fasta file
     */
    private FastaIndex fastaIndex;
    /**
     * Random access file of the current fasta file
     */
    private RandomAccessFile currentFastaFile;
    /**
     * Number of proteins to keep in cache, 1 by default
     */
    private int nCache = 1;
    /**
     * List of accessions of the loaded proteins
     */
    private ArrayList<String> loadedProteins = new ArrayList<String>();
    /**
     * Recognized flags for a decoy protein
     */
    public static final String[] decoyFlags = {"REVERSED", "RND"};

    /**
     * Constructor
     */
    private SequenceFactory() {
    }

    /**
     * static method returning the instance of the factory
     * @return the instance of the factory
     */
    public static SequenceFactory getInstance() {
        if (instance == null) {
            instance = new SequenceFactory();
        }
        return instance;
    }

    /**
     * returns the instance of the factory with the specified cache size
     * @param nCache    the new cache size
     * @return the instance of the factory with the specified cache size
     */
    public static SequenceFactory getInstance(int nCache) {
        if (instance == null) {
            instance = new SequenceFactory();
        }
        instance.setnCache(nCache);
        return instance;
    }

    /**
     * Returns the desired protein
     * @param accession     accession of the desired protein
     * @return the desired protein
     * @throws IOException exception thrown whenever an error is encountered while reading the fasta file
     */
    public Protein getProtein(String accession) throws IOException {
        Protein currentProtein = currentProteinMap.get(accession);
        if (currentProtein == null) {
            Header currentHeader = null;
            long index = fastaIndex.getIndex(accession);
            currentFastaFile.seek(index);
            String line, sequence = "";
            while ((line = currentFastaFile.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(">")) {
                    if (!sequence.equals("")) {
                        break;
                    }
                    currentHeader = Header.parseFromFASTA(line);
                } else {
                    sequence += line;
                }
            }
            currentProtein = new Protein(accession, currentHeader.getDatabaseType(), sequence, isDecoy(accession));

            if (loadedProteins.size() == nCache) {
                currentProteinMap.remove(loadedProteins.get(0));
                currentHeaderMap.remove(loadedProteins.get(0));
                loadedProteins.remove(0);
            }
            loadedProteins.add(accession);
            currentProteinMap.put(accession, currentProtein);
            currentHeaderMap.put(accession, currentHeader);
        }
        return currentProtein;
    }

    /**
     * returns the desired header for the protein in the fasta file
     * @param accession accession of the desired protein
     * @return  the corresponding header
     * @throws IOException exception thrown whenever an error occurred while reading the fasta file
     */
    public Header getHeader(String accession) throws IOException {
        Header result = currentHeaderMap.get(accession);
        if (result == null) {
            long index = fastaIndex.getIndex(accession);
            currentFastaFile.seek(index);
            result = Header.parseFromFASTA(currentFastaFile.readLine());
        }
        return result;
    }

    /**
     * loads a new fasta file in the factory. Only one fasta file can be loaded at a time
     * @param fastaFile the fasta file to load
     * @throws FileNotFoundException    exception thrown if the file was not found
     * @throws IOException  exception thrown if an error occurred while reading the fasta file
     * @throws ClassNotFoundException exception thrown whenever an error occurred while deserializing the file index
     */
    public void loadFastaFile(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        currentFastaFile = new RandomAccessFile(fastaFile, "r");
        fastaIndex = getFastaIndex(fastaFile);
    }

    /**
     * loads a new fasta file in the factory. Only one fasta file can be loaded at a time
     * @param fastaFile the fasta file to load
     * @param progressDialog a progress dialog showing the progress
     * @throws FileNotFoundException    exception thrown if the file was not found
     * @throws IOException  exception thrown if an error occurred while reading the fasta file
     * @throws ClassNotFoundException exception thrown whenever an error occurred while deserializing the file index
     */
    public void loadFastaFile(File fastaFile, ProgressDialogX progressDialog) throws FileNotFoundException, IOException, ClassNotFoundException {
        currentFastaFile = new RandomAccessFile(fastaFile, "r");
        fastaIndex = getFastaIndex(fastaFile, progressDialog);
    }

    /**
     * Returns the file index of a fasta file
     * @param fastaFile the fasta file
     * @return the index of the fasta file
     * @throws FileNotFoundException    exception thrown if the file was not found
     * @throws IOException  exception thrown if an error occurred while reading the fasta file
     * @throws ClassNotFoundException exception thrown whenever an error occurred while deserializing the file index
     */
    public FastaIndex getFastaIndex(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        if (indexFile.exists()) {
            return getIndex(indexFile);
        } else {
            FastaIndex tempFastaIndex = createFastaIndex(fastaFile);
            writeIndex(tempFastaIndex, fastaFile.getParentFile());
            return tempFastaIndex;
        }
    }

    /**
     * Returns the file index of a fasta file
     * @param fastaFile the fasta file
     * @param progressDialog a progress dialog showing the progress
     * @return the index of the fasta file
     * @throws FileNotFoundException    exception thrown if the file was not found
     * @throws IOException  exception thrown if an error occurred while reading the fasta file
     * @throws ClassNotFoundException exception thrown whenever an error occurred while deserializing the file index
     */
    public FastaIndex getFastaIndex(File fastaFile, ProgressDialogX progressDialog) throws FileNotFoundException, IOException, ClassNotFoundException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        if (indexFile.exists()) {
            return getIndex(indexFile);
        } else {
            FastaIndex tempFastaIndex = createFastaIndex(fastaFile, progressDialog);
            writeIndex(tempFastaIndex, fastaFile.getParentFile());
            return tempFastaIndex;
        }
    }

    /**
     * serializes the fasta file index in a given directory
     * @param fastaIndex    the index of the fasta file
     * @param directory     the directory where to write the file
     * @throws IOException  exception thrown whenever an error occurred while writing the file
     */
    public void writeIndex(FastaIndex fastaIndex, File directory) throws IOException {
        // Serialize the file index as compomics utilities index
        FileOutputStream fos = new FileOutputStream(new File(directory, fastaIndex.getFileName() + ".cui"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(fastaIndex);
        oos.close();
    }

    /**
     * Deserializes the index of the fasta file
     * @param fastaIndex    the fasta cui index file
     * @return the corresponding FastaIndex instance
     * @throws FileNotFoundException    exception thrown if the file was not found
     * @throws IOException  exception thrown if an error occurred while reading the fasta file
     * @throws ClassNotFoundException exception thrown whenever an error occurred while deserializing the file index
     */
    public FastaIndex getIndex(File fastaIndex) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fastaIndex);
        ObjectInputStream in = new ObjectInputStream(fis);
        FastaIndex index = (FastaIndex) in.readObject();
        in.close();
        return index;
    }

    /**
     * Closes the opened file
     * @throws IOException exception thrown whenever an error occurred while closing the file
     */
    public void closeFile() throws IOException { 
        if (currentFastaFile != null) {
            currentFastaFile.close();
        }
    }

    /**
     * static method to create a fasta index for a fasta file
     * @param fastaFile the fasta file
     * @return  the corresponding fasta index
     * @throws FileNotFoundException    exception thrown if the fasta file was not found
     * @throws IOException  exception thrown whenever an error occurred while reading the file
     */
    public static FastaIndex createFastaIndex(File fastaFile) throws FileNotFoundException, IOException {
        return createFastaIndex(fastaFile, null);
    }

    /**
     * static method to create a fasta index for a fasta file
     * @param fastaFile the fasta file
     * @param progressDialog a progress dialog showing the progress
     * @return  the corresponding fasta index
     * @throws FileNotFoundException    exception thrown if the fasta file was not found
     * @throws IOException  exception thrown whenever an error occurred while reading the file
     */
    public static FastaIndex createFastaIndex(File fastaFile, ProgressDialogX progressDialog) throws FileNotFoundException, IOException {

        HashMap<String, Long> indexes = new HashMap<String, Long>();

        RandomAccessFile randomAccessFile = new RandomAccessFile(fastaFile, "r");

        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
        }

        long progressUnit = randomAccessFile.length()/100;
        
        String line, accession = "";
        boolean decoy = false;
        int nTarget = 0;
        Header fastaHeader;
        long index = randomAccessFile.getFilePointer();

        while ((line = randomAccessFile.readLine()) != null) {
            if (line.startsWith(">")) {
                fastaHeader = Header.parseFromFASTA(line);
                accession = fastaHeader.getAccession();
                indexes.put(accession, index);
                if (!isDecoy(accession)) {
                    nTarget++;
                } else if (!decoy) {
                    decoy = true;
                }
                if (progressDialog != null) {
                    progressDialog.setValue((int) (index/progressUnit));
                }
            } else {
                index = randomAccessFile.getFilePointer();
            }
        }

        if (progressDialog != null) {
            progressDialog.setIndeterminate(true);
        }

        randomAccessFile.close();

        return new FastaIndex(indexes, fastaFile.getName(), decoy, nTarget);
    }

    /**
     * Returns a boolean indicating whether a protein is decoy or not based on the protein accession. 
     * Recognized flags for decoy proteins are listed as a static field.
     * 
     * @param proteinAccession  The accession of the protein
     * @return a boolean indicating whether the protein is Decoy.
     */
    public static boolean isDecoy(String proteinAccession) {
        for (String flag : decoyFlags) {
            if (proteinAccession.endsWith(flag) || proteinAccession.startsWith(flag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether the database loaded contains decoy sequences
     * @return a boolean indicating whether the database loaded contains decoy sequences
     */
    public boolean concatenatedTargetDecoy() {
        return fastaIndex.isDecoy();
    }

    /**
     * Returns the number of target sequences in the database
     * @return the number of target sequences in the database
     */
    public int getNTargetSequences() {
        return fastaIndex.getNTarget();
    }

    /**
     * Appends decoy sequences to the desired file
     * @param destinationFile the destination file
     * @throws IOException exception thrown whenever an error occurred while reading or writing a file
     */
    public void appendDecoySequences(File destinationFile) throws IOException {
        appendDecoySequences(destinationFile, null);
    }

    /**
     * Appends decoy sequences to the desired file while displaying progress
     * @param destinationFile   the destination file
     * @param progressDialog    the progress dialog
     * @throws IOException exception thrown whenever an error occurred while reading or writing a file 
     */
    public void appendDecoySequences(File destinationFile, ProgressDialogX progressDialog) throws IOException {

        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(fastaIndex.getNTarget());
        }

        RandomAccessFile newFile = new RandomAccessFile(destinationFile, "rw");
        HashMap<String, Long> indexes = new HashMap<String, Long>();

        int counter = 1;
        Header decoyHeader, currentHeader;
        Protein currentProtein;
        String decoySequence, decoyAccession;

        for (String accession : fastaIndex.getIndexes().keySet()) {

            if (progressDialog != null) {
                progressDialog.setValue(counter++);
            }

            currentProtein = getProtein(accession);
            currentHeader = getHeader(accession);

            decoyAccession = currentProtein.getAccession() + "_" + decoyFlags[0];
            decoyHeader = Header.parseFromFASTA(currentHeader.toString());
            decoyHeader.setAccession(decoyAccession);
            decoyHeader.setDescription(decoyHeader.getDescription() + "-" + decoyFlags[0]);
            decoySequence = reverseSequence(currentProtein.getSequence());

            indexes.put(currentProtein.getAccession(), newFile.getFilePointer());
            newFile.writeBytes(currentHeader.toString() + "\n");
            newFile.writeBytes(currentProtein.getSequence() + "\n");

            indexes.put(decoyAccession, newFile.getFilePointer());
            newFile.writeBytes(decoyHeader.toString() + "\n");
            newFile.writeBytes(decoySequence + "\n");
        }

        if (progressDialog != null) {
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Saving. Please Wait...");
        }

        FastaIndex newIndex = new FastaIndex(indexes, destinationFile.getName(), true, counter - 1);
        writeIndex(newIndex, destinationFile.getParentFile());


        newFile.close();
    }

    /**
     * Reverses a protein sequence.
     * 
     * @param sequence the protein sequence
     * @return the reversed protein sequence
     */
    private String reverseSequence(String sequence) {
        return new StringBuilder(sequence).reverse().toString();
    }

    /**
     * Returns the sequences present in the database
     * @return the sequences present in the database 
     */
    public ArrayList<String> getAccessions() {
        return new ArrayList<String>(fastaIndex.getIndexes().keySet());
    }

    /**
     * Returns the size of the cache
     * @return the size of the cache 
     */
    public int getnCache() {
        return nCache;
    }

    /**
     * Sets the size of the cache
     * @param nCache  the new size of the cache
     */
    public void setnCache(int nCache) {
        this.nCache = nCache;
    }
}
