package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.gui.waiting.WaitingHandler;
import com.compomics.util.protein.Header;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JProgressBar;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * Factory retrieving the information of the loaded FASTA file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SequenceFactory {

    /**
     * Instance of the factory.
     */
    private static SequenceFactory instance = null;
    /**
     * Map of the currently loaded Headers.
     */
    private static HashMap<String, Header> currentHeaderMap = new HashMap<String, Header>();
    /**
     * Map of the currently loaded proteins.
     */
    private static HashMap<String, Protein> currentProteinMap = new HashMap<String, Protein>();
    /**
     * Index of the FASTA file.
     */
    private static FastaIndex fastaIndex;
    /**
     * Random access file of the current FASTA file.
     */
    private static BufferedRandomAccessFile currentFastaFile;
    /**
     * Number of proteins to keep in cache, 1 by default.
     */
    private static int nCache = 1;
    /**
     * List of accessions of the loaded proteins.
     */
    private static ArrayList<String> loadedProteins = new ArrayList<String>();
    /**
     * Recognized flags for a decoy protein.
     */
    public static final String[] decoyFlags = {"REVERSED", "RND", "SHUFFLED"};

    /**
     * Constructor.
     */
    private SequenceFactory() {
    }

    /**
     * Static method returning the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static SequenceFactory getInstance() {
        if (instance == null) {
            instance = new SequenceFactory();
        }
        return instance;
    }

    /**
     * Returns the instance of the factory with the specified cache size.
     *
     * @param nCache the new cache size
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
     * Clears the factory getInstance() needs to be called afterwards.
     */
    public void clearFactory() {
        instance = new SequenceFactory();
    }

    /**
     * Returns the desired protein.
     *
     * @param accession accession of the desired protein
     * @return the desired protein
     * @throws IOException thrown whenever an error is encountered while reading
     * the FASTA file
     * @throws IllegalArgumentException thrown whenever an error is encountered
     * while reading the FASTA file
     * @throws InterruptedException  
     */
    public Protein getProtein(String accession) throws IOException, IllegalArgumentException, InterruptedException {

        Protein currentProtein = currentProteinMap.get(accession);

        if (currentProtein == null) {

            Long index = fastaIndex.getIndex(accession);

            if (index == null) {
                throw new IllegalArgumentException("Protein not found: " + accession + ".");
            }

            currentProtein = getProtein(accession, index, 0);

            if (loadedProteins.size() == nCache) {
                currentProteinMap.remove(loadedProteins.get(0));
                currentHeaderMap.remove(loadedProteins.get(0));
                loadedProteins.remove(0);
            }

            loadedProteins.add(accession);
            currentProteinMap.put(accession, currentProtein);
        }
        if (currentProtein == null) {
            throw new IllegalArgumentException("Protein not found: " + accession + ".");
        }
        return currentProtein;
    }

    /**
     * Returns the protein indexed by the given index. It can be that the IO is
     * busy (especially when working on distant servers) thus returning an
     * error. The method will then try 100 times at 0.01 second intervals.
     *
     * @param index the index where to look at
     * @param nTries the number of tries already made
     * @return the header indexed by the given index
     */
    private synchronized Protein getProtein(String accession, long index, int nTries) throws InterruptedException, IOException {

        try {
            currentFastaFile.seek(index);
            String line, sequence = "";
            Header currentHeader = currentHeaderMap.get(accession);

            while ((line = currentFastaFile.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(">")) {
                    if (!sequence.equals("")) {
                        break;
                    }
                    if (currentHeader == null) {
                        currentHeader = Header.parseFromFASTA(line);
                        currentHeaderMap.put(accession, currentHeader);
                    }
                } else {
                    sequence += line;
                }
            }

            return new Protein(accession, currentHeader.getDatabaseType(), sequence, isDecoy(accession));

        } catch (IOException e) {
            if (nTries <= 100) {
                wait(10);
                return getProtein(accession, index, nTries + 1);
            } else {
                throw e;
            }
        }
    }

    /**
     * Returns the desired header for the protein in the FASTA file.
     *
     * @param accession accession of the desired protein
     * @return the corresponding header
     * @throws IOException exception thrown whenever an error occurred while
     * reading the FASTA file
     * @throws IllegalArgumentException exception thrown whenever a protein is
     * not found
     * @throws InterruptedException  
     */
    public Header getHeader(String accession) throws IOException, IllegalArgumentException, InterruptedException {

        Header result = currentHeaderMap.get(accession);

        if (result == null) {

            Long index = fastaIndex.getIndex(accession);

            if (index == null) {
                throw new IllegalArgumentException("Protein not found: " + accession + ".");
            }
            return getHeader(index, 0);
        }

        return result;
    }

    /**
     * Returns the header indexed by the given index. It can be that the IO is
     * busy (especially when working on distant servers) thus returning an
     * error. The method will then try 100 times at 0.01 second intervals.
     *
     * @param index the index where to look at
     * @param nTries the number of tries already made
     * @return the header indexed by the given index
     */
    private synchronized Header getHeader(long index, int nTries) throws InterruptedException, IOException {

        try {
            currentFastaFile.seek(index);
            return Header.parseFromFASTA(currentFastaFile.readLine());
        } catch (IOException e) {
            if (nTries <= 100) {
                wait(10);
                return getHeader(index, nTries + 1);
            } else {
                throw e;
            }
        }
    }

    /**
     * Loads a new FASTA file in the factory. Only one FASTA file can be loaded
     * at a time.
     *
     * @param fastaFile the FASTA file to load
     * @throws FileNotFoundException exception thrown if the file was not found
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    public void loadFastaFile(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        currentFastaFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);
        fastaIndex = getFastaIndex(fastaFile);
    }

    /**
     * Loads a new FASTA file in the factory. Only one FASTA file can be loaded
     * at a time.
     *
     * @param fastaFile the FASTA file to load
     * @param waitingHandler a waitingHandler showing the progress
     * @throws FileNotFoundException exception thrown if the file was not found
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    public void loadFastaFile(File fastaFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, ClassNotFoundException {
        currentFastaFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);
        fastaIndex = getFastaIndex(fastaFile, waitingHandler);
    }

    /**
     * Returns the file index of a FASTA file.
     *
     * @param fastaFile the FASTA file
     * @return the index of the FASTA file
     * @throws FileNotFoundException exception thrown if the file was not found
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    private FastaIndex getFastaIndex(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        FastaIndex tempFastaIndex;
        if (indexFile.exists()) {
            tempFastaIndex = getIndex(indexFile);
            return tempFastaIndex;
        }
        tempFastaIndex = createFastaIndex(fastaFile);
        writeIndex(tempFastaIndex, fastaFile.getParentFile());
        return tempFastaIndex;
    }

    /**
     * Returns the file index of a FASTA file.
     *
     * @param fastaFile the FASTA file
     * @param waitingHandler a waitingHandler showing the progress
     * @return the index of the FASTA file
     * @throws FileNotFoundException exception thrown if the file was not found
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    private FastaIndex getFastaIndex(File fastaFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, ClassNotFoundException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        FastaIndex tempFastaIndex;
        if (indexFile.exists()) {
            tempFastaIndex = getIndex(indexFile);
            return tempFastaIndex;
        }
        tempFastaIndex = createFastaIndex(fastaFile, waitingHandler);

        if (!waitingHandler.isRunCanceled()) {
            writeIndex(tempFastaIndex, fastaFile.getParentFile());
        }
        return tempFastaIndex;
    }

    /**
     * Serializes the FASTA file index in a given directory.
     *
     * @param fastaIndex the index of the FASTA file
     * @param directory the directory where to write the file
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    private void writeIndex(FastaIndex fastaIndex, File directory) throws IOException {
        // Serialize the file index as compomics utilities index
        FileOutputStream fos = new FileOutputStream(new File(directory, fastaIndex.getFileName() + ".cui"));
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(fastaIndex);
        oos.close();
        bos.close();
        fos.close();
    }

    /**
     * Deserializes the index of the FASTA file.
     *
     * @param fastaIndex the FASTA cui index file
     * @return the corresponding FastaIndex instance
     * @throws FileNotFoundException exception thrown if the file was not found
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    private FastaIndex getIndex(File fastaIndex) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fastaIndex);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream in = new ObjectInputStream(bis);
        FastaIndex index = (FastaIndex) in.readObject();
        fis.close();
        bis.close();
        in.close();
        return index;
    }

    /**
     * Closes the opened file.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * closing the file
     */
    public void closeFile() throws IOException {
        if (currentFastaFile != null) {
            currentFastaFile.close();
        }
    }

    /**
     * Static method to create a FASTA index for a FASTA file.
     *
     * @param fastaFile the FASTA file
     * @return the corresponding FASTA index
     * @throws FileNotFoundException exception thrown if the FASTA file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     */
    private static FastaIndex createFastaIndex(File fastaFile) throws FileNotFoundException, IOException {
        return createFastaIndex(fastaFile, null);
    }

    /**
     * Static method to create a FASTA index for a FASTA file.
     *
     * @param fastaFile the FASTA file
     * @param progressBar a progress bar showing the progress
     * @return the corresponding FASTA index
     * @throws FileNotFoundException exception thrown if the FASTA file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     */
    private static FastaIndex createFastaIndex(File fastaFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException {

        HashMap<String, Long> indexes = new HashMap<String, Long>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            //progressBar.setStringPainted(true);
            waitingHandler.setMaxSecondaryProgressValue(100);
            waitingHandler.setSecondaryProgressValue(0);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String line;
        boolean decoy = false;
        int nTarget = 0;
        long index = bufferedRandomAccessFile.getFilePointer();

        while ((line = bufferedRandomAccessFile.readLine()) != null) {
            if (line.startsWith(">")) {
                Header fastaHeader = Header.parseFromFASTA(line);
                String accession = fastaHeader.getAccession();
                if (accession == null) {
                    accession = fastaHeader.getRest();
                }
                indexes.put(accession, index);
                if (!isDecoy(accession)) {
                    nTarget++;
                } else if (!decoy) {
                    decoy = true;
                }
                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressValue((int) (index / progressUnit));
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                }
            } else {
                index = bufferedRandomAccessFile.getFilePointer();
            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(true);
            //progressBar.setStringPainted(false);
        }

        bufferedRandomAccessFile.close();

        return new FastaIndex(indexes, fastaFile.getName(), decoy, nTarget);
    }

    /**
     * Returns a boolean indicating whether a protein is decoy or not based on
     * the protein accession. Recognized flags for decoy proteins are listed as
     * a static field.
     *
     * @param proteinAccession The accession of the protein
     * @return a boolean indicating whether the protein is Decoy.
     */
    public static boolean isDecoy(String proteinAccession) {
        for (String flag : decoyFlags) {

            String start = flag + ".*"; // @TODO: perhaps this can be further optimized?
            String end = ".*" + flag;

            if (proteinAccession.matches(start) || proteinAccession.matches(end)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether the database loaded contains decoy sequences.
     *
     * @return a boolean indicating whether the database loaded contains decoy
     * sequences
     */
    public boolean concatenatedTargetDecoy() {
        return fastaIndex.isDecoy();
    }

    /**
     * Returns the number of target sequences in the database.
     *
     * @return the number of target sequences in the database
     */
    public int getNTargetSequences() {
        return fastaIndex.getNTarget();
    }

    /**
     * Appends decoy sequences to the desired file.
     *
     * @param destinationFile the destination file
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws IllegalArgumentException
     * @throws InterruptedException  
     */
    public void appendDecoySequences(File destinationFile) throws IOException, IllegalArgumentException, InterruptedException {
        appendDecoySequences(destinationFile, null);
    }

    /**
     * Appends decoy sequences to the desired file while displaying progress.
     *
     * @param destinationFile the destination file
     * @param waitingHandler the waiting handler
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws IllegalArgumentException exdeption thrown whenever a protein is
     * not found
     * @throws InterruptedException  
     */
    public void appendDecoySequences(File destinationFile, WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, InterruptedException {

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            //progressBar.setStringPainted(true); //@TODO: not true by default?
            waitingHandler.setMaxSecondaryProgressValue(fastaIndex.getNTarget());
            waitingHandler.setSecondaryProgressValue(0);
        }

        BufferedRandomAccessFile newFile = new BufferedRandomAccessFile(destinationFile, "rw", 1024 * 100);
        HashMap<String, Long> indexes = new HashMap<String, Long>();

        int counter = 1;

        for (String accession : fastaIndex.getIndexes().keySet()) {

            if (waitingHandler.isRunCanceled()) {
                break;
            }

            waitingHandler.increaseSecondaryProgressValue();

            Protein currentProtein = getProtein(accession);
            Header currentHeader = getHeader(accession);

            String decoyAccession = currentProtein.getAccession() + "_" + decoyFlags[0];
            Header decoyHeader = Header.parseFromFASTA(currentHeader.toString());
            decoyHeader.setAccession(decoyAccession);
            decoyHeader.setDescription(decoyHeader.getDescription() + "-" + decoyFlags[0]);

            String decoySequence = reverseSequence(currentProtein.getSequence());

            indexes.put(currentProtein.getAccession(), newFile.getFilePointer());
            newFile.writeBytes(currentHeader.toString() + System.getProperty("line.separator"));
            newFile.writeBytes(currentProtein.getSequence() + System.getProperty("line.separator"));

            indexes.put(decoyAccession, newFile.getFilePointer());

            // @TODO: this might not be the best way of doing this, but was easier than trying to change the parsing in the Header class...
            if (decoyHeader.toString().equalsIgnoreCase(currentHeader.toString())) {
                decoyHeader.setRest(decoyAccession);
            }

            newFile.writeBytes(decoyHeader.toString() + System.getProperty("line.separator"));
            newFile.writeBytes(decoySequence + System.getProperty("line.separator"));
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(true);
            //waitingHandler.setStringPainted(false);
        }

        newFile.close();

        if (!waitingHandler.isRunCanceled()) {
            FastaIndex newIndex = new FastaIndex(indexes, destinationFile.getName(), true, counter - 1);
            writeIndex(newIndex, destinationFile.getParentFile());
        } else {
            destinationFile.delete();
        }
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
     * Returns the sequences present in the database.
     *
     * @return the sequences present in the database
     */
    public ArrayList<String> getAccessions() {
        return new ArrayList<String>(fastaIndex.getIndexes().keySet());
    }

    /**
     * Returns the size of the cache.
     *
     * @return the size of the cache
     */
    public int getnCache() {
        return nCache;
    }

    /**
     * Sets the size of the cache.
     *
     * @param nCache the new size of the cache
     */
    public void setnCache(int nCache) {
        SequenceFactory.nCache = nCache;
    }

    /**
     * Returns the occurrence of every amino acid in the database.
     *
     * @param progressBar a progress bar, can be null
     * @return a map containing all amino acid occurrence in the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws IllegalArgumentException
     * @throws InterruptedException  
     */
    public HashMap<String, Integer> getAAOccurrences(JProgressBar progressBar) throws IOException, IllegalArgumentException, InterruptedException {

        HashMap<String, Integer> aaMap = new HashMap<String, Integer>();
        ArrayList<String> accessions = getAccessions();

        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(accessions.size());
            progressBar.setValue(0);
        }

        for (String accession : accessions) {

            if (!isDecoy(accession)) {
                Protein protein = getProtein(accession);
                for (String aa : protein.getSequence().split("")) {
                    Integer n = aaMap.get(aa);
                    if (n == null) {
                        n = 0;
                    }
                    aaMap.put(aa, n + 1);
                }
            }
            if (progressBar != null) {
                progressBar.setValue(progressBar.getValue() + 1);
            }
        }

        if (progressBar != null) {
            progressBar.setIndeterminate(true);
        }

        return aaMap;
    }
}
