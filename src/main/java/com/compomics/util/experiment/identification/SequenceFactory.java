package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.protein.Header;
import java.io.*;
import java.sql.SQLException;
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
    private BufferedRandomAccessFile currentRandomAccessFile = null;
    /**
     * The FASTA file currently loaded
     */
    private File currentFastaFile = null;
    /**
     * Number of proteins to keep in cache, 1 by default. For my good old
     * uniprot swissprot human, 20,000 sequences take ~300MB of memory.
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
     * The tag added after adding decoy sequences to a FASTA file.
     */
    private static String targetDecoyFileNameTag = "_concatenated_target_decoy.fasta";
    /**
     * The default protein tree attached to the database loaded
     */
    private ProteinTree defaultProteinTree = null;

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
     *
     * @throws IOException
     */
    public void clearFactory() throws IOException, SQLException {
        closeFile();
        defaultProteinTree = null;
        currentHeaderMap.clear();
        currentProteinMap.clear();
        fastaIndex = null;
        currentRandomAccessFile = null;
        currentFastaFile = null;
        loadedProteins.clear();
        molecularWeights.clear();
    }

    /**
     * Empties the cache of the factory.
     */
    public void emptyCache() {
        currentHeaderMap.clear();
        currentProteinMap.clear();
        loadedProteins.clear();
        molecularWeights.clear();
        if (defaultProteinTree != null) {
            defaultProteinTree.emptyCache();
        }
    }

    /**
     * Returns the desired protein. If the protein is not found, the database
     * will be re-indexed.
     *
     * @param accession accession of the desired protein
     * @return the desired protein
     * @throws IOException thrown whenever an error is encountered while reading
     * the FASTA file
     * @throws IllegalArgumentException thrown whenever an error is encountered
     * while reading the FASTA file
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public Protein getProtein(String accession) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {
        return getProtein(accession, true);
    }

    /**
     * Returns the desired protein. Eventually re-indexes the database if the
     * protein is not found.
     *
     * @param accession accession of the desired protein
     * @param reindex a boolean indicating whether the database should be
     * re-indexed in case the protein is not found.
     * @return the desired protein
     * @throws IOException thrown whenever an error is encountered while reading
     * the FASTA file
     * @throws IllegalArgumentException thrown whenever an error is encountered
     * while reading the FASTA file
     * @throws InterruptedException
     */
    private Protein getProtein(String accession, boolean reindex) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        if (isDefaultReversed() && isDecoyAccession(accession)) {
            String targetAccession = getDefaultTargetAccession(accession);
            try {
                Protein targetProtein = getProtein(targetAccession, reindex);
                return new Protein(accession, targetProtein.getDatabaseType(), reverseSequence(targetProtein.getSequence()), true);
            } catch (Exception e) {
                // Back to old school mode
            }
        }

        Protein currentProtein = currentProteinMap.get(accession);

        if (currentProtein == null) {

            Long index = fastaIndex.getIndex(accession);

            if (index == null) {
                if (reindex) {
                    fastaIndex = getFastaIndex(true, null);
                    return getProtein(accession, false);
                }
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
     * @throws InterruptedException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private synchronized Protein getProtein(String accession, long index, int nTries) throws InterruptedException, IOException, IllegalArgumentException {

        try {
            currentRandomAccessFile.seek(index);
            String line, sequence = "";
            Header currentHeader = currentHeaderMap.get(accession);

            while ((line = currentRandomAccessFile.readLine()) != null) {
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

            return new Protein(accession, currentHeader.getDatabaseType(), sequence, isDecoyAccession(accession));

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
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public Header getHeader(String accession) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {
        return getHeader(accession, true);
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
    private Header getHeader(String accession, boolean reindex) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        Header result = currentHeaderMap.get(accession);

        if (result == null) {

            Long index = fastaIndex.getIndex(accession);

            if (index == null) {
                if (reindex) {
                    fastaIndex = getFastaIndex(true, null);
                    return getHeader(accession, false);
                }
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
            currentRandomAccessFile.seek(index);
            return Header.parseFromFASTA(currentRandomAccessFile.readLine());
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
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    public void loadFastaFile(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException, StringIndexOutOfBoundsException, IllegalArgumentException {
        loadFastaFile(fastaFile, null);
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

        if (!fastaFile.exists()) {
            throw new FileNotFoundException("The FASTA file \'" + fastaFile.getAbsolutePath() + "\' could not be found!");
        }
        defaultProteinTree = null;
        currentFastaFile = fastaFile;
        currentRandomAccessFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);
        fastaIndex = getFastaIndex(false, waitingHandler);
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
    private FastaIndex getFastaIndex() throws FileNotFoundException, IOException, ClassNotFoundException, IllegalArgumentException {
        return getFastaIndex(false, null);
    }

    /**
     * Returns the file index of the FASTA file loaded in the factory (see
     * currentFastaFile attribute). If a deserialization problem occurs the file
     * will be automatically overwritten and the stacktrace printed.
     *
     * @param fastaFile the FASTA file
     * @param overwrite boolean indicating whether the index .cui file shall be
     * overwritten if present.
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
    private FastaIndex getFastaIndex(boolean overwrite, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, ClassNotFoundException, StringIndexOutOfBoundsException {

        FastaIndex tempFastaIndex;
        if (!overwrite) {
            File indexFile = new File(currentFastaFile.getParent(), currentFastaFile.getName() + ".cui");
            if (indexFile.exists()) {
                try {
                    tempFastaIndex = (FastaIndex) SerializationUtils.readObject(indexFile);
                    Long indexLastModified = tempFastaIndex.getLastModified();
                    if (indexLastModified != null) {
                        long fileLastModified = currentFastaFile.lastModified();
                        if (indexLastModified == fileLastModified) {
                            return tempFastaIndex;
                        } else {
                            System.err.println("Reindexing: " + currentFastaFile.getName() + ". (changes in the file detected)");
                        }
                    }
                } catch (InvalidClassException e) {
                    System.err.println("Reindexing: " + currentFastaFile.getName() + ". (Reason: " + e.getLocalizedMessage() + ")");
                } catch (Exception e) {
                    System.err.println("Reindexing: " + currentFastaFile.getName() + ". (Reason: " + e.getLocalizedMessage() + ")");
                }
            }
        }

        // try to rescue user settings
        String decoyTag = null;
        String name = null;
        String version = null;
        Header.DatabaseType databaseType = null;
        File indexFile = new File(currentFastaFile.getParent(), currentFastaFile.getName() + ".cui");
        if (indexFile.exists()) {
            try {
                tempFastaIndex = (FastaIndex) SerializationUtils.readObject(indexFile);
                decoyTag = tempFastaIndex.getDecoyTag();
                version = tempFastaIndex.getVersion();
                databaseType = tempFastaIndex.getDatabaseType();
                name = tempFastaIndex.getName();
            } catch (Exception e) {
                // Fail silently
            }
        }

        System.out.println("Reindexing: " + currentFastaFile.getName() + ".");
        tempFastaIndex = createFastaIndex(currentFastaFile, name, decoyTag, databaseType, version, waitingHandler);

        if (waitingHandler == null || (waitingHandler != null && !waitingHandler.isRunCanceled())) {
            try {
                writeIndex(tempFastaIndex, currentFastaFile.getParentFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return tempFastaIndex;
    }

    /**
     * Static method to create a FASTA index for a FASTA file.
     *
     * @param fastaFile the FASTA file
     * @param progressBar a progress bar showing the progress
     * @param decoyTag the decoy tag. Will be inferred if null.
     * @param databaseType the database type. Will be inferred if null.
     * @param version the version. last modification of the file will be used if
     * null.
     * @param name the name of the database. Set to file name if null.
     *
     * @return the corresponding FASTA index
     *
     * @throws FileNotFoundException exception thrown if the FASTA file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    private static FastaIndex createFastaIndex(File fastaFile, String name, String decoyTag, Header.DatabaseType databaseType, String version,
            WaitingHandler waitingHandler) throws FileNotFoundException, IOException, StringIndexOutOfBoundsException, IllegalArgumentException {

        HashMap<String, Long> indexes = new HashMap<String, Long>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String line;
        boolean decoy = false, defaultReversed = false, multipleType = false;
        int nTarget = 0;
        long index = bufferedRandomAccessFile.getFilePointer();
        if (databaseType == null) {
            databaseType = Header.DatabaseType.Unknown;
        }

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
                if (decoyTag == null) {
                    decoyTag = getDecoyFlag(accession);
                }
                if (decoyTag == null || !isDecoy(accession, decoyTag)) {
                    nTarget++;
                    if (!multipleType) {
                        if (databaseType == Header.DatabaseType.Unknown) {
                            databaseType = fastaHeader.getDatabaseType();
                        } else if (fastaHeader.getDatabaseType() != databaseType && databaseType != Header.DatabaseType.Generic_Header) {
                            databaseType = Header.DatabaseType.Unknown;
                            multipleType = true;
                        }
                    }
                } else if (!decoy) {
                    decoy = true;
                    if (accession.endsWith(getDefaultDecoyAccessionSuffix())) {
                        defaultReversed = true;
                    }
                }

                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressCounter((int) (index / progressUnit));
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                }
            } else {
                index = bufferedRandomAccessFile.getFilePointer();
            }
        }

        if (waitingHandler != null) {
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        }

        bufferedRandomAccessFile.close();

        long lastModified = fastaFile.lastModified();

        if (version == null) {
            version = FastaIndex.getDefaultVersion(lastModified);
        }

        String fileName = fastaFile.getName();
        if (name == null) {
            name = fileName;
        }

        return new FastaIndex(indexes, fileName, name, decoy, defaultReversed, nTarget, lastModified, databaseType, decoyTag, version);
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
     * Saves the index.
     *
     * @throws IOException
     */
    public void saveIndex() throws IOException {
        writeIndex(fastaIndex, currentFastaFile.getParentFile());
    }

    /**
     * Closes the opened file.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * closing the file
     */
    public void closeFile() throws IOException, SQLException {
        if (currentRandomAccessFile != null) {
            currentRandomAccessFile.close();
        }
        if (defaultProteinTree != null) {
            defaultProteinTree.close();
        }
    }

    /**
     * Returns a boolean indicating whether a protein is decoy or not based on
     * the protein accession and a given decoy flag.
     *
     * @param proteinAccession The accession of the protein
     * @param decoyFlag the decoy flag
     * @return a boolean indicating whether the protein is Decoy.
     */
    public static boolean isDecoy(String proteinAccession, String decoyFlag) {

        // test if the decoy tag is empty, and return false if it is
        if (decoyFlag == null || decoyFlag.isEmpty()) {
            return false;
        }

        String start = decoyFlag + ".*";
        String end = ".*" + decoyFlag;

        if (proteinAccession.matches(start) || proteinAccession.matches(end)) {
            return true;
        }

        return false;
    }

    /**
     * Returns the default tag matched in the sequence if any. Null else.
     *
     * @param proteinAccession the protein accession
     *
     * @return the decoy tag matched by this protein
     */
    public static String getDecoyFlag(String proteinAccession) {
        for (String flag : decoyFlags) {
            if (isDecoy(proteinAccession, flag)) {
                return flag;
            }
        }
        return null;
    }

    /**
     * Indicates whether a protein is decoy in the selected loaded FASTA file.
     *
     * @param proteinAccession the protein accession of interest.
     * @return true if decoy
     */
    public boolean isDecoyAccession(String proteinAccession) {
        return isDecoy(proteinAccession, fastaIndex.getDecoyTag());
    }

    /**
     * Indicates whether a protein accession is decoy according to the standard
     * decoy flags.
     *
     * @deprecated deprecated, use the isDecoy(proteinAccession, flag) with file
     * dependent flag or isDecoyAccession(String proteinAccession) instead.
     * @param proteinAccession the accession of interest
     * @return true if decoy
     */
    public static boolean isDecoy(String proteinAccession) {
        for (String flag : decoyFlags) {
            if (isDecoy(proteinAccession, flag)) {
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
        return fastaIndex.isConcatenatedTargetDecoy();
    }

    /**
     * Indicates whether the decoy sequences are reversed versions of the target
     * and the decoy accessions built based on the sequence factory methods. See
     * getDefaultDecoyAccession(String targetAccession).
     *
     * @return true if the the decoy sequences are reversed versions of the
     * target and the decoy accessions built based on the sequence factory
     * method
     */
    public boolean isDefaultReversed() {
        return fastaIndex.isDefaultReversed();
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
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(fastaIndex.getNTarget());
        }

        // first create the new target-decoy file
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destinationFile));

        for (String accession : fastaIndex.getIndexes().keySet()) {

            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                break;
            }

            if (waitingHandler != null) {
                waitingHandler.increaseSecondaryProgressCounter();
            }

            Protein currentProtein = getProtein(accession);
            Header currentHeader = getHeader(accession);

            String decoyAccession = getDefaultDecoyAccession(currentProtein.getAccession());
            Header decoyHeader = Header.parseFromFASTA(currentHeader.toString());
            decoyHeader.setAccession(decoyAccession);
            decoyHeader.setDescription(getDefaultDecoyDescription(decoyHeader.getDescription()));

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
            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        }

        boolean indexFile = true;

        if (waitingHandler != null) {
            if (waitingHandler.isRunCanceled()) {
                indexFile = false;
            }
        }

        if (indexFile) {
            // now (re-)index the new target-decoy file
            loadFastaFile(destinationFile, waitingHandler);
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
    public static String reverseSequence(String sequence) {
        return new StringBuilder(sequence).reverse().toString();
    }

    /**
     * Returns the sequences present in the database. An empty list if no file
     * is loaded.
     *
     * @return the sequences present in the database
     */
    public ArrayList<String> getAccessions() {
        if (fastaIndex != null) {
            return new ArrayList<String>(fastaIndex.getIndexes().keySet());
        } else {
            return new ArrayList<String>();
        }
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
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public HashMap<String, Integer> getAAOccurrences(JProgressBar progressBar) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        HashMap<String, Integer> aaMap = new HashMap<String, Integer>();
        ArrayList<String> accessions = getAccessions();

        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(accessions.size());
            progressBar.setValue(0);
        }

        for (String accession : accessions) {

            if (!isDecoyAccession(accession)) {
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
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public double computeMolecularWeight(String accession) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        if (isDefaultReversed() && isDecoyAccession(accession)) {
            // Don't really see where we would need that...
            try {
                return computeMolecularWeight(getDefaultTargetAccession(accession));
            } catch (Exception e) {
                // back to standard mode
            }
        }

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

    /**
     * Returns the target-decoy file name tag.
     *
     * @return the targetDecoyFileNameTag
     */
    public static String getTargetDecoyFileNameTag() {
        return targetDecoyFileNameTag;
    }

    /**
     * Set the target-decoy file name tag.
     *
     * @param targetDecoyFileNameTag the targetDecoyFileNameTag to set
     */
    public static void setTargetDecoyFileNameTag(String targetDecoyFileNameTag) {
        SequenceFactory.targetDecoyFileNameTag = targetDecoyFileNameTag;
    }

    /**
     * Returns the name of the loaded FASTA file. Null if none loaded.
     *
     * @return the name of the loaded FASTA file
     */
    public String getFileName() {
        if (fastaIndex == null) {
            return null;
        }
        return fastaIndex.getFileName();
    }

    /**
     * Returns the currently loaded fasta file.
     *
     * @return the currently loaded fasta file
     */
    public File getCurrentFastaFile() {
        return currentFastaFile;
    }

    /**
     * Returns the default suffix for a decoy accession.
     *
     * @return the default suffix for a decoy accession
     */
    public static String getDefaultDecoyAccessionSuffix() {
        return "_" + decoyFlags[0];
    }

    /**
     * Returns the default decoy accession for a target accession.
     *
     * @param targetAccession the target accession
     * @return the default decoy accession
     */
    public static String getDefaultDecoyAccession(String targetAccession) {
        return targetAccession + getDefaultDecoyAccessionSuffix();
    }

    /**
     * Returns the default description for a decoy protein.
     *
     * @param targetDescription the description of a target protein
     * @return the default description of the decoy protein
     */
    public static String getDefaultDecoyDescription(String targetDescription) {
        return targetDescription + "-" + decoyFlags[0];
    }

    /**
     * Returns the default target accession of a given decoy protein. Note:
     * works only for the accessions constructed according to
     * getDefaultDecoyAccession(String targetAccession).
     *
     * @param decoyAccession the decoy accession
     * @return the target accession
     */
    public static String getDefaultTargetAccession(String decoyAccession) {
        return decoyAccession.substring(0, decoyAccession.length() - getDefaultDecoyAccessionSuffix().length());
    }

    /**
     * Returns the fasta index of the currently loaded file.
     *
     * @return the fasta index of the currently loaded file
     */
    public FastaIndex getCurrentFastaIndex() {
        return fastaIndex;
    }

    /**
     * Returns the default protein tree corresponding to the database loaded in
     * factory
     *
     * @return the default protein tree
     */
    public ProteinTree getDefaultProteinTree() throws IOException, InterruptedException, ClassNotFoundException, IllegalArgumentException, SQLException {
        return getDefaultProteinTree(null);
    }

    /**
     * Returns the default protein tree corresponding to the database loaded in
     * factory
     *
     * @param waitingHandler waiting handler displaying progress to the user
     * during the initiation of the tree
     *
     * @return the default protein tree
     */
    public ProteinTree getDefaultProteinTree(WaitingHandler waitingHandler) throws IOException, InterruptedException, ClassNotFoundException, IllegalArgumentException, SQLException {
        if (defaultProteinTree == null) {

            UtilitiesUserPreferences userPreferences = UtilitiesUserPreferences.loadUserPreferences();
            int memoryPreference = userPreferences.getMemoryPreference();
            int treeSize = memoryPreference / 4;
            defaultProteinTree = new ProteinTree(treeSize);

            int tagLength = 3;
            if (getNTargetSequences() > 100000) {
                tagLength = 4;
                if (memoryPreference > 4000) {
                    defaultProteinTree.setCacheSize(100000);
                }
            }
            if (memoryPreference < 2000) {
                defaultProteinTree.setCacheSize(500);
            }
            defaultProteinTree.initiateTree(tagLength, 500, 50, waitingHandler, true);
        }
        return defaultProteinTree;
    }
}
