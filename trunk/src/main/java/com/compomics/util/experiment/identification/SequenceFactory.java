package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.protein.Header;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JProgressBar;

/**
 * Factory retrieving the information of the loaded FASTA file
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
     * Index of the FASTA file
     */
    private FastaIndex fastaIndex;
    /**
     * Random access file of the current FASTA file
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
    public static final String[] decoyFlags = {"REVERSED", "RND", "SHUFFLED"};

    /**
     * Constructor
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
     * returns the instance of the factory with the specified cache size.
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
     * Returns the desired protein.
     *
     * @param accession accession of the desired protein
     * @return the desired protein
     * @throws IOException thrown whenever an error is encountered while reading
     * the FASTA file
     * @throws IllegalArgumentException thrown whenever an error is encountered
     * while reading the FASTA file
     */
    public Protein getProtein(String accession) throws IOException, IllegalArgumentException {

        Protein currentProtein = currentProteinMap.get(accession);

        if (currentProtein == null) {

            Long index = fastaIndex.getIndex(accession);

            if (index == null) {
                throw new IllegalArgumentException("Protein not found: " + accession + ".");
            }

            currentFastaFile.seek(index);
            String line, sequence = "";
            Header currentHeader = null;

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
        if (currentProtein == null) {
            throw new IllegalArgumentException("Protein not found: " + accession + ".");
        }
        return currentProtein;
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
     */
    public Header getHeader(String accession) throws IOException, IllegalArgumentException {

        Header result = currentHeaderMap.get(accession);

        if (result == null) {

            Long index = fastaIndex.getIndex(accession);

            if (index == null) {
                throw new IllegalArgumentException("Protein not found: " + accession + ".");
            }

            currentFastaFile.seek(index);
            result = Header.parseFromFASTA(currentFastaFile.readLine());
        }

        return result;
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
        currentFastaFile = new RandomAccessFile(fastaFile, "r");
        fastaIndex = getFastaIndex(fastaFile);
    }

    /**
     * Loads a new FASTA file in the factory. Only one FASTA file can be loaded
     * at a time.
     *
     * @param fastaFile the FASTA file to load
     * @param progressBar a progress bar showing the progress
     * @throws FileNotFoundException exception thrown if the file was not found
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    public void loadFastaFile(File fastaFile, JProgressBar progressBar) throws FileNotFoundException, IOException, ClassNotFoundException {
        currentFastaFile = new RandomAccessFile(fastaFile, "r");
        fastaIndex = getFastaIndex(fastaFile, progressBar);
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
    public FastaIndex getFastaIndex(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        FastaIndex tempFastaIndex;
        if (indexFile.exists()) {
            try {
                tempFastaIndex = getIndex(indexFile);
                return tempFastaIndex;
            } catch (Exception e) {
            }
        }
        tempFastaIndex = createFastaIndex(fastaFile);
        writeIndex(tempFastaIndex, fastaFile.getParentFile());
        return tempFastaIndex;
    }

    /**
     * Returns the file index of a FASTA file.
     *
     * @param fastaFile the FASTA file
     * @param progressBar a progress bar showing the progress
     * @return the index of the FASTA file
     * @throws FileNotFoundException exception thrown if the file was not found
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    public FastaIndex getFastaIndex(File fastaFile, JProgressBar progressBar) throws FileNotFoundException, IOException, ClassNotFoundException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        FastaIndex tempFastaIndex;
        if (indexFile.exists()) {
            try {
                tempFastaIndex = getIndex(indexFile);
                return tempFastaIndex;
            } catch (Exception e) {
            }
        }
        tempFastaIndex = createFastaIndex(fastaFile, progressBar);
        writeIndex(tempFastaIndex, fastaFile.getParentFile());
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
    public void writeIndex(FastaIndex fastaIndex, File directory) throws IOException {
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
    public FastaIndex getIndex(File fastaIndex) throws FileNotFoundException, IOException, ClassNotFoundException {
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
    public static FastaIndex createFastaIndex(File fastaFile) throws FileNotFoundException, IOException {
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
    public static FastaIndex createFastaIndex(File fastaFile, JProgressBar progressBar) throws FileNotFoundException, IOException {

        HashMap<String, Long> indexes = new HashMap<String, Long>();

        RandomAccessFile randomAccessFile = new RandomAccessFile(fastaFile, "r");

        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
            progressBar.setMaximum(100);
            progressBar.setValue(0);
        }

        long progressUnit = randomAccessFile.length() / 100;

        String line;
        boolean decoy = false;
        int nTarget = 0;
        Header fastaHeader;
        long index = randomAccessFile.getFilePointer();

        while ((line = randomAccessFile.readLine()) != null) {
            if (line.startsWith(">")) {
                fastaHeader = Header.parseFromFASTA(line);
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
                if (progressBar != null) {
                    progressBar.setValue((int) (index / progressUnit));
                }
            } else {
                index = randomAccessFile.getFilePointer();
            }
        }

        if (progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(false);
        }

        randomAccessFile.close();

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
     */
    public void appendDecoySequences(File destinationFile) throws IOException {
        appendDecoySequences(destinationFile, null);
    }

    /**
     * Appends decoy sequences to the desired file while displaying progress.
     *
     * @param destinationFile the destination file
     * @param progressBar the progress bar
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws IllegalArgumentException exdeption thrown whenever a protein is
     * not found
     */
    public void appendDecoySequences(File destinationFile, JProgressBar progressBar) throws IOException, IllegalArgumentException {

        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
            progressBar.setMaximum(fastaIndex.getNTarget());
            progressBar.setValue(0);
        }

        RandomAccessFile newFile = new RandomAccessFile(destinationFile, "rw");
        HashMap<String, Long> indexes = new HashMap<String, Long>();

        int counter = 1;
        Header decoyHeader, currentHeader;
        Protein currentProtein;
        String decoySequence, decoyAccession;

        for (String accession : fastaIndex.getIndexes().keySet()) {

            if (progressBar != null) {
                progressBar.setValue(counter++);
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

            // @TODO: this might not be the best way of doing this, but was easier than trying to change the parsing in the Header class...
            if (decoyHeader.toString().equalsIgnoreCase(currentHeader.toString())) {
                decoyHeader.setRest(decoyAccession);
            }

            newFile.writeBytes(decoyHeader.toString() + "\n");
            newFile.writeBytes(decoySequence + "\n");
        }

        if (progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(false);
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
     * Returns the occurrence of every amino acid in the database
     *
     * @param progressBar a progress bar, can be null
     * @return a map containing all amino acid occurrence in the database
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     */
    public HashMap<String, Integer> getAAOccurrences(JProgressBar progressBar) throws IOException {
        HashMap<String, Integer> aaMap = new HashMap<String, Integer>();
        Protein protein;
        Integer n;
        ArrayList<String> accessions = getAccessions();
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(accessions.size());
            progressBar.setValue(0);
        }
        for (String accession : accessions) {
            if (!isDecoy(accession)) {
                protein = getProtein(accession);
                for (String aa : protein.getSequence().split("")) {
                    n = aaMap.get(aa);
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
