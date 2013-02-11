package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.gui.waiting.WaitingHandler;
import com.compomics.util.io.SerializationUtils;
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
    private HashMap<String, Header> currentHeaderMap = new HashMap<String, Header>();
    /**
     * Map of the currently loaded proteins.
     */
    private HashMap<String, Protein> currentProteinMap = new HashMap<String, Protein>();
    /**
     * Index of the FASTA file.
     */
    private FastaIndex fastaIndex = null;
    /**
     * Random access file of the current FASTA file.
     */
    private BufferedRandomAccessFile currentFastaFile = null;
    /**
     * Number of proteins to keep in cache, 1 by default.
     */
    private int nCache = 1;
    /**
     * List of accessions of the loaded proteins.
     */
    private ArrayList<String> loadedProteins = new ArrayList<String>();
    /**
     * Recognized flags for a decoy protein.
     */
    private static final String[] decoyFlags = {"REVERSED", "RND", "SHUFFLED"};
    /**
     * HashMap of the currently calculated protein molecular weights.
     */
    private HashMap<String, Double> molecularWeights = new HashMap<String, Double>();

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
        currentHeaderMap.clear();
        currentProteinMap.clear();
        fastaIndex = null;
        currentFastaFile = null;
        loadedProteins.clear();
        molecularWeights.clear();
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
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    public void loadFastaFile(File fastaFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, ClassNotFoundException, StringIndexOutOfBoundsException, IllegalArgumentException {
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
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    private FastaIndex getFastaIndex(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException, IllegalArgumentException {
        return getFastaIndex(fastaFile, null);
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
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    private FastaIndex getFastaIndex(File fastaFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, ClassNotFoundException, StringIndexOutOfBoundsException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        FastaIndex tempFastaIndex;
        if (indexFile.exists()) {
            try {
                tempFastaIndex = (FastaIndex) SerializationUtils.readObject(indexFile);
                return tempFastaIndex;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tempFastaIndex = createFastaIndex(fastaFile, waitingHandler);

        if (waitingHandler == null || (waitingHandler != null && !waitingHandler.isRunCanceled())) {
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
        File destinationFile = new File(directory, fastaIndex.getFileName() + ".cui");
        SerializationUtils.writeObject(fastaIndex, destinationFile);
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
     * @param progressBar a progress bar showing the progress
     * @return the corresponding FASTA index
     * @throws FileNotFoundException exception thrown if the FASTA file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    private static FastaIndex createFastaIndex(File fastaFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, StringIndexOutOfBoundsException, IllegalArgumentException {

        HashMap<String, Long> indexes = new HashMap<String, Long>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
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

//                if (fastaHeader.getStartLocation() != -1) {
//                    accession += " (" + fastaHeader.getStartLocation() + "-" + fastaHeader.getEndLocation() + ")"; // special dbtoolkit pattern
//                }

                if (accession == null) {
                    accession = fastaHeader.getRest();
                }

                if (indexes.containsKey(accession)) {
                    throw new IllegalArgumentException("Non unique accession number found \'" + accession + "\'!\nPlease check the FASTA file.");
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
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public void appendDecoySequences(File destinationFile) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {
        appendDecoySequences(destinationFile, null);
    }

    /**
     * Appends decoy sequences to the desired file while displaying progress.
     *
     * @param destinationFile the destination file
     * @param waitingHandler the waiting handler
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws IllegalArgumentException exception thrown whenever a protein is
     * not found
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public void appendDecoySequences(File destinationFile, WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(false);
            waitingHandler.setMaxSecondaryProgressValue(fastaIndex.getNTarget());
            waitingHandler.setSecondaryProgressValue(0);
        }

        // first create the new target-decoy file
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destinationFile));

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

            bufferedWriter.write(currentHeader.toString() + System.getProperty("line.separator"));
            bufferedWriter.write(currentProtein.getSequence() + System.getProperty("line.separator"));

            // @TODO: this might not be the best way of doing this, but was easier than trying to change the parsing in the Header class...
            if (decoyHeader.toString().equalsIgnoreCase(currentHeader.toString())) {
                decoyHeader.setRest(decoyAccession);
            }

            bufferedWriter.write(decoyHeader.toString() + System.getProperty("line.separator"));
            bufferedWriter.write(decoySequence + System.getProperty("line.separator"));
            
            // possible fix for the dbtoolkit uniprot format
//            Protein currentProtein = getProtein(accession);
//            Header currentHeader = getHeader(accession);
//            String reversedSequence = reverseSequence(currentProtein.getSequence());
//
//            bufferedWriter.write(currentHeader.toString() + System.getProperty("line.separator"));
//            bufferedWriter.write(currentProtein.getSequence() + System.getProperty("line.separator"));
//
//            // @TODO: this might not be the best way of doing this, but was easier than trying to change the parsing in the Header class...
//            if (currentHeader.toString("_" + decoyFlags[0]).equalsIgnoreCase(currentHeader.toString())) {
//                currentHeader.setRest(currentProtein.getAccession() + "_" + decoyFlags[0]);
//            }
//
//            bufferedWriter.write(currentHeader.toString("_" + decoyFlags[0]) + System.getProperty("line.separator"));
//            bufferedWriter.write(reversedSequence + System.getProperty("line.separator"));
        }

        bufferedWriter.close();

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressDialogIndeterminate(true);
        }


        if (waitingHandler.isRunCanceled()) {
            destinationFile.delete();
        } else {
            // now (re-)index the new target-decoy file
            loadFastaFile(destinationFile);
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
        this.nCache = nCache;
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

    /**
     * Returns the protein's molecular weight.
     *
     * @param accession the protein's accession number
     * @return the protein's molecular weight
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     */
    public double computeMolecularWeight(String accession) throws IOException, IllegalArgumentException, InterruptedException {

        // see if we've already calculated the weight of this protein
        if (molecularWeights.containsKey(accession)) {
            return molecularWeights.get(accession);
        }

        // weight unknown, we need to calculate the weight
        Protein protein = getProtein(accession);
        double weight = protein.computeMolecularWeight() / 1000;
        molecularWeights.put(accession, weight);
        return weight;
    }
}
