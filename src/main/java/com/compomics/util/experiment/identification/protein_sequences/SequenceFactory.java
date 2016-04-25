package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapperType;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.protein.Header;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JProgressBar;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * Factory retrieving the information of the loaded FASTA file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 * @author Dominik Kopczynski
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
     * The FASTA file currently loaded.
     */
    private File currentFastaFile = null;
    /**
     * Number of proteins to keep in cache, 100000 by default.
     */
    private int nCache = 100000;
    /**
     * List of accessions of the loaded proteins.
     */
    private ArrayList<String> loadedProteins = new ArrayList<String>();
    /**
     * Recognized flags for a decoy protein.
     */
    private static final String[] DECOY_FLAGS = {"REVERSED", "RND", "SHUFFLED", "DECOY"};
    /**
     * HashMap of the currently calculated protein molecular weights.
     */
    private HashMap<String, Double> molecularWeights = new HashMap<String, Double>();
    /**
     * The default peptide to protein mapper.
     */
    private PeptideMapper defaultPeptideMapper = null;
    /**
     * Boolean indicating that the factory is reading the file.
     */
    private boolean reading = false;
    /**
     * The time out in milliseconds when querying the file.
     */
    public final static long TIME_OUT = 10000;
    /**
     * Indicates whether the decoy hits should be kept in memory.
     */
    private boolean decoyInMemory = true;
    /**
     * The minimal protein count required for reliable target/decoy based
     * statistics.
     */
    public static int minProteinCount = 1000; // @TODO: use a better metric

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
     * Indicates whether the database contained enough protein sequences for
     * reliability of the target/decoy based statistics.
     *
     * @return a boolean indicating whether the database contained enough
     * protein sequences for reliability of the target/decoy based statistics
     */
    public boolean hasEnoughSequences() {
        return getNTargetSequences() > minProteinCount;
    }

    /**
     * Clears the factory getInstance() needs to be called afterwards.
     *
     * @throws IOException if an IOException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void clearFactory() throws IOException, SQLException {
        closeFile();
        defaultPeptideMapper = null;
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
        if (defaultPeptideMapper != null) {
            defaultPeptideMapper.emptyCache();
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
     * @throws InterruptedException if an InterruptedException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public Protein getProtein(String accession) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException {
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
     * @throws InterruptedException if an InterruptedException occurs
     */
    private Protein getProtein(String accession, boolean reindex) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException {

        if (fastaIndex == null) {
            throw new IllegalArgumentException("Protein sequences not loaded in the sequence factory.");
        }

        Protein currentProtein = currentProteinMap.get(accession);

        if (currentProtein == null && isDefaultReversed() && isDecoyAccession(accession)) {
            if (decoyInMemory) {
                currentProtein = getDecoyProteinFromTargetSynchronized(accession, reindex);
            } else {
                currentProtein = getDecoyProteinFromTarget(accession, reindex);
            }
        }

        if (currentProtein == null) {
            currentProtein = getProteinSynchronized(accession, reindex);
        }
        if (currentProtein == null) {
            throw new IllegalArgumentException("Protein not found: " + accession + ".");
        }

        return currentProtein;
    }

    /**
     * Returns a decoy protein from a target protein or looks for the sequence
     * in the cache if not found.
     *
     * @param accession the accession of the decoy protein to look for
     * @param reindex a boolean indicating whether the database should be
     * re-indexed in case the protein is not found.
     *
     * @return the protein of interest, null if not found
     *
     * @throws IOException if an IOException occurs
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public synchronized Protein getDecoyProteinFromTargetSynchronized(String accession, boolean reindex)
            throws IOException, IllegalArgumentException, FileNotFoundException {

        // check whether another thread did the job already
        Protein currentProtein = currentProteinMap.get(accession);
        if (currentProtein == null) {
            currentProtein = getDecoyProteinFromTarget(accession, reindex);
        }
        return currentProtein;
    }

    /**
     * Returns a decoy protein from a target protein or looks for the sequence
     * in the cache if not found.
     *
     * @param accession the accession of the decoy protein to look for
     * @param reindex a boolean indicating whether the database should be
     * re-indexed in case the protein is not found.
     *
     * @return the protein of interest, null if not found
     *
     * @throws IOException if an IOException occurs
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public Protein getDecoyProteinFromTarget(String accession, boolean reindex) throws IOException, IllegalArgumentException, FileNotFoundException {
        Protein currentProtein = null;
        String targetAccession = getDefaultTargetAccession(accession);
        try {
            Protein targetProtein = currentProteinMap.get(targetAccession);
            if (targetProtein == null && decoyInMemory) {
                currentProtein = getProteinSynchronized(accession, reindex);
            } else {
                if (targetProtein == null) {
                    targetProtein = getProtein(targetAccession, reindex);
                }
                currentProtein = new Protein(accession, targetProtein.getDatabaseType(), reverseSequence(targetProtein.getSequence()), true);
                if (decoyInMemory) {
                    addProteinToCache(accession, currentProtein);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return currentProtein;
    }

    /**
     * Returns the desired protein. Eventually re-indexes the database if the
     * protein is not found. Synchronized version serving as a queue for
     * threads.
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
    private synchronized Protein getProteinSynchronized(String accession, boolean reindex) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException {

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

            return getProtein(accession, index, 1);
        }

        return currentProtein;
    }

    /**
     * Returns the protein indexed by the given index. It can be that the IO is
     * busy (especially when working on distant servers) thus returning an
     * error. The method will then retry after waiting waitingTime milliseconds.
     * The waitingTime is doubled for the next try. The method throws an
     * exception after timeout (see timeOut attribute).
     *
     * @param index the index where to look at
     * @param waitingTime the waiting time before retry
     * @return the header indexed by the given index
     * @throws InterruptedException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private synchronized Protein getProtein(String accession, long index, long waitingTime) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException {

        if (waitingTime <= 0) {
            throw new IllegalArgumentException("Waiting time should be a positive number.");
        }

        try {
            if (reading) {
                throw new IllegalStateException("Attempting to read new line before current read operation is completed.");
            }
            reading = true;
            currentRandomAccessFile.seek(index);
            String line, sequence = "";
            Header currentHeader = currentHeaderMap.get(accession);
            boolean headerFound = false;

            while ((line = currentRandomAccessFile.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(">")) {
                    if (!sequence.equals("") || headerFound) {
                        break;
                    }
                    if (currentHeader == null) {
                        currentHeader = Header.parseFromFASTA(line);
                        if (currentHeader == null) {
                            throw new IllegalArgumentException("Could not parse FASTA header \"" + line + "\".");
                        }
                        currentHeaderMap.put(accession, currentHeader);
                    }
                    headerFound = true;
                } else {
                    sequence += Util.removeSubString(line, "\\*");
                }
            }

            Protein currentProtein = new Protein(accession, currentHeader.getDatabaseType(), sequence, isDecoyAccession(accession));

            addProteinToCache(accession, currentProtein);

            reading = false;

            return currentProtein;

        } catch (IOException e) {
            reading = false;
            if (waitingTime < TIME_OUT) {
                wait(waitingTime);
                e.printStackTrace();
                return getProtein(accession, index, 2 * waitingTime);
            } else {
                throw e;
            }
        }
    }

    /**
     * Adds a protein to the cache and keeps it under the desired size.
     *
     * @param accession the accession of the protein to add
     * @param protein the protein to add
     */
    private synchronized void addProteinToCache(String accession, Protein protein) {
        while (loadedProteins.size() >= nCache) {
            currentProteinMap.remove(loadedProteins.get(0));
            currentHeaderMap.remove(loadedProteins.get(0));
            loadedProteins.remove(0);
        }

        loadedProteins.add(accession);
        currentProteinMap.put(accession, protein);
    }

    /**
     * Returns the desired header for the protein in the FASTA file.
     *
     * @param accession accession of the desired protein
     *
     * @return the corresponding header
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the FASTA file
     * @throws java.lang.InterruptedException exception thrown whenever an error
     * occurred while waiting for the connection to the FASTA file to recover.
     */
    public Header getHeader(String accession) throws IOException, InterruptedException {
        return getHeader(accession, true);
    }

    /**
     * Returns the desired header for the protein in the FASTA file.
     *
     * @param accession accession of the desired protein
     *
     * @return the corresponding header
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the FASTA file
     * @throws java.lang.InterruptedException exception thrown whenever an error
     * occurred while waiting for the connection to the FASTA file to recover.
     */
    private Header getHeader(String accession, boolean reindex) throws IOException, InterruptedException {

        Header result = currentHeaderMap.get(accession);

        if (result == null) {

            Long index = fastaIndex.getIndex(accession);

            if (index == null) {
                if (reindex) {
                    fastaIndex = getFastaIndex(true, null);
                    result = getHeader(accession, false);
                }
                throw new IllegalArgumentException("Protein not found: " + accession + ".");
            }

            result = getHeader(index, 0);

            currentHeaderMap.put(accession, result);
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
     *
     * @return the header indexed by the given index
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the FASTA file
     * @throws java.lang.InterruptedException exception thrown whenever an error
     * occurred while waiting for the connection to the FASTA file to recover.
     */
    private synchronized Header getHeader(long index, int nTries) throws InterruptedException, IOException {

        if (reading) {
            throw new IllegalStateException("Attempting to read new line before current read operation is completed.");
        }
        try {
            reading = true;
            currentRandomAccessFile.seek(index);
            Header result = Header.parseFromFASTA(currentRandomAccessFile.readLine());
            reading = false;
            return result;
        } catch (IOException e) {
            reading = false;
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
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     */
    public void loadFastaFile(File fastaFile) throws IOException, ClassNotFoundException, StringIndexOutOfBoundsException {
        loadFastaFile(fastaFile, null);
    }

    /**
     * Loads a new FASTA file in the factory. Only one FASTA file can be loaded
     * at a time.
     *
     * @param fastaFile the FASTA file to load
     * @param waitingHandler a waitingHandler showing the progress
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     */
    public void loadFastaFile(File fastaFile, WaitingHandler waitingHandler) throws IOException, ClassNotFoundException, StringIndexOutOfBoundsException {

        if (!fastaFile.exists()) {
            throw new FileNotFoundException("The FASTA file \'" + fastaFile.getAbsolutePath() + "\' could not be found!");
        }

        defaultPeptideMapper = null;
        currentFastaFile = fastaFile;
        currentRandomAccessFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);
        fastaIndex = getFastaIndex(false, waitingHandler);
    }

    /**
     * Indicates whether the connection to the random access file has been
     * closed.
     *
     * @return a boolean indicating whether the connection to the random access
     * file has been closed.
     */
    public boolean isClosed() {
        return currentFastaFile == null;
    }

    /**
     * Resets the connection to the random access file.
     *
     * @throws IOException if an IOException occurs
     */
    public void resetConnection() throws IOException {
        currentRandomAccessFile.close();
        currentRandomAccessFile = new BufferedRandomAccessFile(currentFastaFile, "r", 1024 * 100);
    }

    /**
     * Returns the file index of a FASTA file.
     *
     * @param fastaFile the FASTA file
     *
     * @return the index of the FASTA file
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file index
     */
    private FastaIndex getFastaIndex() throws IOException, ClassNotFoundException {
        return getFastaIndex(false, null);
    }

    /**
     * Returns the file index of the FASTA file loaded in the factory (see
     * currentFastaFile attribute). If a deserialization problem occurs the file
     * will be automatically overwritten and the stacktrace printed.
     *
     * @param overwrite boolean indicating whether the index .cui file shall be
     * overwritten if present, even if the file has not been changed
     * @param waitingHandler a waitingHandler showing the progress
     *
     * @return the index of the FASTA file
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    private synchronized FastaIndex getFastaIndex(boolean overwrite, WaitingHandler waitingHandler) throws IOException, StringIndexOutOfBoundsException {
        return getFastaIndex(currentFastaFile, overwrite, waitingHandler);
    }

    /**
     * Returns the file index of the given FASTA file. If a problem occurs while
     * reading an older index the file will be automatically overwritten and the
     * stacktrace printed.
     *
     * @param fastaFile the FASTA file to index
     * @param overwrite boolean indicating whether the index .cui file shall be
     * overwritten if present, even if the file has not been changed
     * @param waitingHandler a waitingHandler showing the progress
     *
     * @return the index of the FASTA file
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the FASTA file
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     * @throws IllegalArgumentException if non unique accession numbers are
     * found
     */
    public static synchronized FastaIndex getFastaIndex(File fastaFile, boolean overwrite, WaitingHandler waitingHandler) throws IOException, StringIndexOutOfBoundsException {

        FastaIndex tempFastaIndex;
        String fileName = fastaFile.getName();
        if (!overwrite) {
            File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
            if (indexFile.exists()) {
                try {
                    tempFastaIndex = (FastaIndex) SerializationUtils.readObject(indexFile);
                    Long indexLastModified = tempFastaIndex.getLastModified();
                    if (indexLastModified != null) {
                        long fileLastModified = fastaFile.lastModified();
                        if (indexLastModified == fileLastModified) {
                            return tempFastaIndex;
                        } else {
                            System.err.println("Reindexing: " + fileName + ". (changes in the file detected)");
                        }
                    }
                } catch (InvalidClassException e) {
                    System.err.println("Reindexing: " + fileName + ". (Reason: " + e.getLocalizedMessage() + ")");
                } catch (Exception e) {
                    System.err.println("Reindexing: " + fileName + ". (Reason: " + e.getLocalizedMessage() + ")");
                }
            }
        }

        // try to rescue user settings
        String decoyTag = null;
        String name = null;
        String version = null;
        String description = null;
        String accessionParsingRule = null;
        File indexFile = new File(fastaFile.getParent(), fileName + ".cui");

        if (indexFile.exists()) {
            try {
                tempFastaIndex = (FastaIndex) SerializationUtils.readObject(indexFile);
                decoyTag = tempFastaIndex.getDecoyTag();
                version = tempFastaIndex.getVersion();
                name = tempFastaIndex.getName();
                description = tempFastaIndex.getDescription();
                accessionParsingRule = tempFastaIndex.getAccessionParsingRule();
            } catch (Exception e) {
                // Fail silently
            }
        }

        System.out.println("Reindexing: " + fileName + ".");
        tempFastaIndex = createFastaIndex(fastaFile, name, decoyTag, version, waitingHandler);
        tempFastaIndex.setDescription(description);
        tempFastaIndex.setAccessionParsingRule(accessionParsingRule);

        if (waitingHandler == null || !waitingHandler.isRunCanceled()) {
            try {
                writeIndex(tempFastaIndex, fastaFile.getParentFile());
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
     * @param mainDatabaseType the main database type. Will be inferred if null.
     * @param version the version. last modification of the file will be used if
     * null.
     * @param name the name of the database. Set to file name if null.
     *
     * @return the corresponding FASTA index
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws StringIndexOutOfBoundsException thrown if issues occur during the
     * parsing of the protein headers
     */
    private static FastaIndex createFastaIndex(File fastaFile, String name, String decoyTag, String version,
            WaitingHandler waitingHandler) throws IOException, StringIndexOutOfBoundsException {

        HashMap<String, Long> indexes = new HashMap<String, Long>();
        HashSet<String> decoyAccessions = new HashSet<String>();
        BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(fastaFile, "r", 1024 * 100);

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(100);
        }

        long progressUnit = bufferedRandomAccessFile.length() / 100;

        String line;
        boolean decoy = false, defaultReversed = false;
        int nTarget = 0;
        long index = bufferedRandomAccessFile.getFilePointer();

        // a map of the database header types
        HashMap<Header.DatabaseType, Integer> databaseTypes = new HashMap<Header.DatabaseType, Integer>();

        // a map of the species
        HashMap<String, Integer> species = new HashMap<String, Integer>();

        while ((line = bufferedRandomAccessFile.readLine()) != null) {

            if (line.startsWith(">")) {

                Header fastaHeader = Header.parseFromFASTA(line);
                String accession = fastaHeader.getAccessionOrRest();

//                if (fastaHeader.getStartLocation() != -1) {
//                    accession += " (" + fastaHeader.getStartLocation() + "-" + fastaHeader.getEndLocation() + ")"; // special dbtoolkit pattern
//                }
//                if (indexes.containsKey(accession)) {
//                    throw new IllegalArgumentException("Non unique accession number found \'" + accession + "\'!\nPlease check the FASTA file.");
//                }
                indexes.put(accession, index);
                if (decoyTag == null) {
                    decoyTag = getDecoyFlag(accession);
                }
                if (decoyTag == null || !isDecoy(accession, decoyTag)) {
                    nTarget++;

                    // get the database type
                    Header.DatabaseType tempDatabaseType = fastaHeader.getDatabaseType();
                    Integer typeCounter = databaseTypes.get(tempDatabaseType);

                    if (typeCounter == null) {
                        databaseTypes.put(tempDatabaseType, 1);
                    } else {
                        databaseTypes.put(tempDatabaseType, typeCounter + 1);
                    }

                    // get the species
                    String taxonomy = fastaHeader.getTaxonomy();
                    if (taxonomy == null || taxonomy.equals("")) {
                        taxonomy = SpeciesFactory.UNKNOWN;
                    }
                    Integer occurrence = species.get(taxonomy);
                    if (occurrence == null) {
                        species.put(taxonomy, 1);
                    } else {
                        species.put(taxonomy, occurrence + 1);
                    }

                } else {
                    decoyAccessions.add(accession);
                    if (!decoy) {
                        decoy = true;
                        if (accession.endsWith(getDefaultDecoyAccessionSuffix())) {
                            defaultReversed = true;
                        }
                    }
                }

                if (waitingHandler != null && progressUnit != 0) {
                    waitingHandler.setSecondaryProgressCounter((int) (index / progressUnit));
                    if (waitingHandler.isRunCanceled()) {
                        break;
                    }
                }
                index = bufferedRandomAccessFile.getFilePointer();
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
            name = Util.removeExtension(fileName);
        }

        // find the main database type
        Header.DatabaseType mainDatabaseType = null;
        int maxCounter = 0;
        Iterator<Header.DatabaseType> iterator = databaseTypes.keySet().iterator();
        while (iterator.hasNext()) {
            Header.DatabaseType tempDatabaseType = iterator.next();
            if (databaseTypes.get(tempDatabaseType) > maxCounter) {
                maxCounter = databaseTypes.get(tempDatabaseType);
                mainDatabaseType = tempDatabaseType;
            }
        }

        return new FastaIndex(indexes, decoyAccessions, fileName, name, decoy, defaultReversed, nTarget, lastModified, mainDatabaseType, databaseTypes, decoyTag, version, species);
    }

    /**
     * Serializes the FASTA file index in a given directory.
     *
     * @param fastaIndex the index of the FASTA file
     * @param directory the directory where to write the file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    public static void writeIndex(FastaIndex fastaIndex, File directory) throws IOException {
        // Serialize the file index as compomics utilities index
        File destinationFile = new File(directory, getIndexName(fastaIndex.getFileName()));
        SerializationUtils.writeObject(fastaIndex, destinationFile);
    }

    /**
     * Returns the name of the FASTA index corresponding to the given FASTA file
     * name.
     *
     * @param fastaName the name of the FASTA file
     *
     * @return the name of the index
     */
    public static String getIndexName(String fastaName) {
        return fastaName + ".cui";
    }

    /**
     * Saves the index.
     *
     * @throws IOException if an IOException occurs
     */
    public void saveIndex() throws IOException {
        writeIndex(fastaIndex, currentFastaFile.getParentFile());
    }

    /**
     * Closes the opened file.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * closing the file
     * @throws SQLException if an SQLException occurs
     */
    public void closeFile() throws IOException, SQLException {
        if (currentRandomAccessFile != null) {
            currentRandomAccessFile.close();
            currentFastaFile = null;
        }
        if (defaultPeptideMapper != null) {
            defaultPeptideMapper.close();
        }
    }

    /**
     * Returns a boolean indicating whether a protein is decoy or not based on
     * the protein accession and a given decoy flag. Note: in most cases the
     * faster isDecoyAccession method should be used instead!
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

        return proteinAccession.matches(start) || proteinAccession.matches(end);
    }

    /**
     * Returns the default tag matched in the sequence if any. Null else.
     *
     * @param proteinAccession the protein accession
     *
     * @return the decoy tag matched by this protein
     */
    private static String getDecoyFlag(String proteinAccession) {
        for (String flag : DECOY_FLAGS) {
            if (isDecoy(proteinAccession, flag)) {
                return flag;
            }
        }
        return null;
    }

    /**
     * Indicates whether a protein is a decoy in the selected loaded FASTA file.
     *
     * @param proteinAccession the protein accession of interest.
     * @return true if decoy
     */
    public boolean isDecoyAccession(String proteinAccession) {
        return fastaIndex.isDecoy(proteinAccession);
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
     * Returns the number of sequences in the FASTA file.
     *
     * @return the number of sequences in the FASTA file
     */
    public int getNSequences() {
        return fastaIndex.getNSequences();
    }

    /**
     * Appends decoy sequences to the desired file.
     *
     * @param destinationFile the destination file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws InterruptedException if an InterruptedException occurs
     * @throws ClassNotFoundException if an ClassNotFoundException occurs
     */
    public void appendDecoySequences(File destinationFile) throws IOException, InterruptedException, ClassNotFoundException {
        appendDecoySequences(destinationFile, null);
    }

    /**
     * Appends decoy sequences to the desired file while displaying progress.
     *
     * @param destinationFile the destination file
     * @param waitingHandler the waiting handler
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     * @throws InterruptedException if an InterruptedException occurs
     * @throws ClassNotFoundException if an ClassNotFoundException occurs
     */
    public void appendDecoySequences(File destinationFile, WaitingHandler waitingHandler)
            throws IOException, InterruptedException, ClassNotFoundException {

        if (waitingHandler != null) {
            waitingHandler.resetSecondaryProgressCounter();
            waitingHandler.setMaxSecondaryProgressCounter(fastaIndex.getNTarget());
        }

        // first create the new target-decoy file
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destinationFile));
        String lineBreak = System.getProperty("line.separator");

        try {
            ProteinIterator proteinIterator = getProteinIterator(true);

            while (proteinIterator.hasNext()) {

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    break;
                }

                if (waitingHandler != null) {
                    waitingHandler.increaseSecondaryProgressCounter();
                }

                Protein currentProtein = proteinIterator.getNextProtein();
                String accession = currentProtein.getAccession();
                Header currentHeader = getHeader(accession);

                String decoyAccession = getDefaultDecoyAccession(currentProtein.getAccession());
//                String decoyAccession ="rev_" + currentProtein.getAccession(); Header for the TPP
                String currentRawHeader = currentHeader.getRawHeader();

                // replace the accession number
                String escapedString = java.util.regex.Pattern.quote(accession);
                currentRawHeader = currentRawHeader.replaceAll(escapedString, decoyAccession);

                // add decoy to the description
                if (currentHeader.getDescription() != null && !currentHeader.getDescription().isEmpty()) {
                    escapedString = java.util.regex.Pattern.quote(currentHeader.getDescription());
                    currentRawHeader = currentRawHeader.replaceAll(escapedString, getDefaultDecoyDescription(currentHeader.getDescription()));
                }

                // write the target protein to the fasta file
                bufferedWriter.write(currentHeader.getRawHeader() + lineBreak);
                bufferedWriter.write(currentProtein.getSequence() + lineBreak);

                // write the decoy protein to the fasta file
                bufferedWriter.write(currentRawHeader + lineBreak);
                bufferedWriter.write(reverseSequence(currentProtein.getSequence()) + lineBreak);

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

        } finally {
            bufferedWriter.close();
        }

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
    public Set<String> getAccessions() {
        Set<String> setToFill = new HashSet<String>();
        if (fastaIndex != null) {
            setToFill = fastaIndex.getIndexes().keySet();
        }
        return setToFill;
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
     *
     * @return a map containing all amino acid occurrence in the database
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the database
     * @throws InterruptedException if an InterruptedException occurs
     * @throws ClassNotFoundException if an ClassNotFoundException occurs
     */
    public HashMap<String, Integer> getAAOccurrences(JProgressBar progressBar) throws IOException, InterruptedException, ClassNotFoundException {

        HashMap<String, Integer> aaMap = new HashMap<String, Integer>();
        Set<String> accessions = getAccessions();

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
     * Returns the protein's molecular weight in kDa.
     *
     * @param accession the protein's accession number
     *
     * @return the protein's molecular weight
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading the protein sequence
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while reading the protein sequence
     */
    public double computeMolecularWeight(String accession) throws IOException, InterruptedException, ClassNotFoundException {

        if (isDefaultReversed() && isDecoyAccession(accession)) {
            // Don't really see where we would need that...
            try {
                return computeMolecularWeight(getDefaultTargetAccession(accession));
            } catch (Exception e) {
                // back to standard mode
            }
        }

        // see if we've already calculated the weight of this protein
        Double molecularWeight = molecularWeights.get(accession);
        if (molecularWeight == null) {
            Protein protein = getProtein(accession);
            molecularWeight = protein.computeMolecularWeight() / 1000;
            molecularWeights.put(accession, molecularWeight);
        }
        return molecularWeight;
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
     * Returns the currently loaded FASTA file.
     *
     * @return the currently loaded FASTA file
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
        return "_" + DECOY_FLAGS[0];
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
        return targetDescription + "-" + DECOY_FLAGS[0];
    }

    /**
     * Returns the default target accession of a given decoy protein. Note:
     * works only for the accessions constructed according to
     * getDefaultDecoyAccession(String targetAccession).
     *
     * @param decoyAccession the decoy accession
     *
     * @return the target accession
     */
    public static String getDefaultTargetAccession(String decoyAccession) {
        return decoyAccession.substring(0, decoyAccession.length() - getDefaultDecoyAccessionSuffix().length());
    }

    /**
     * Returns the FASTA index of the currently loaded file.
     *
     * @return the FASTA index of the currently loaded file
     */
    public FastaIndex getCurrentFastaIndex() {
        return fastaIndex;
    }

    /**
     * Returns the default peptide mapper. Null if none created.
     *
     * @return the default peptide mapper
     */
    public PeptideMapper getDefaultPeptideMapper() {
        return defaultPeptideMapper;
    }

    /**
     * Returns the default peptide to protein mapper for the database loaded in
     * factory according to the sequence matching preferences. Creates a new one
     * if none found.
     *
     * @param sequenceMatchingPreferences the sequences matching preferences
     * @param waitingHandler waiting handler displaying progress to the user
     * during the indexation of the database
     * @param exceptionHandler handler for the exceptions encountered while
     * indexing the database
     *
     * @return the default peptide mapper
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while indexing the database.
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with an SQL database.
     */
    public PeptideMapper getDefaultPeptideMapper(SequenceMatchingPreferences sequenceMatchingPreferences, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        int nThreads = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        return getDefaultPeptideMapper(sequenceMatchingPreferences, waitingHandler, exceptionHandler, true, nThreads);
    }

    /**
     * Returns the default peptide to protein mapper for the database loaded in
     * factory according to the sequence matching preferences. Creates a new one
     * if none found.
     *
     * @param sequenceMatchingPreferences the sequences matching preferences
     * @param waitingHandler waiting handler displaying progress to the user
     * during the indexation of the database
     * @param exceptionHandler handler for the exceptions encountered while
     * indexing the database
     * @param nThreads the number of threads to use during indexing
     *
     * @return the default peptide mapper
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while indexing the database.
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with an SQL database.
     */
    public PeptideMapper getDefaultPeptideMapper(SequenceMatchingPreferences sequenceMatchingPreferences, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        return getDefaultPeptideMapper(sequenceMatchingPreferences, waitingHandler, exceptionHandler, true, nThreads);
    }

    /**
     * Returns the default peptide to protein mapper for the database loaded in
     * factory according to the sequence matching preferences. Creates a new one
     * if none found.
     *
     * @param sequenceMatchingPreferences the sequences matching preferences
     * @param waitingHandler waiting handler displaying progress to the user
     * during the indexation of the database
     * @param exceptionHandler handler for the exceptions encountered while
     * indexing the database
     * @param displayProgress boolean indicating whether the progress of the
     * indexing should be displayed
     * @param nThreads the number of threads to use during indexing
     *
     * @return the default peptide mapper
     *
     * @throws IOException exception thrown whenever an error occurs while
     * reading or writing a file.
     * @throws ClassNotFoundException exception thrown whenever an error occurs
     * while deserializing an object.
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while indexing the database.
     * @throws SQLException exception thrown whenever a problem occurred while
     * interacting with an SQL database.
     */
    public synchronized PeptideMapper getDefaultPeptideMapper(SequenceMatchingPreferences sequenceMatchingPreferences, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, boolean displayProgress, int nThreads) throws IOException, InterruptedException, ClassNotFoundException, SQLException {
        if (defaultPeptideMapper == null) {

            PeptideMapperType peptideMapperType = sequenceMatchingPreferences.getPeptideMapperType();
            switch (peptideMapperType) {
                case fm_index:
                    defaultPeptideMapper = new FMIndex();
                    break;
                case tree:

                    UtilitiesUserPreferences userPreferences = UtilitiesUserPreferences.loadUserPreferences();
                    int memoryPreference = userPreferences.getMemoryPreference();
                    int memoryAllocated = 3 * memoryPreference / 4;
                    int cacheSize = 250000;
                    if (memoryPreference < 2500) {
                        cacheSize = 5000;
                    } else if (memoryPreference < 10000) {
                        cacheSize = 25000;
                    }

                    ProteinTree defaultProteinTree = new ProteinTree(memoryAllocated, cacheSize);

                    int tagLength = 3;
                    defaultProteinTree.initiateTree(tagLength, 50, 50, waitingHandler, exceptionHandler, true, displayProgress, nThreads);
                    emptyCache();

                    int treeSize = memoryPreference / 4;
                    defaultProteinTree.setMemoryAllocation(treeSize);

                    // close and delete the database if the process was canceled
                    if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                        defaultProteinTree.deleteDb();
                    }
                    defaultPeptideMapper = defaultProteinTree;
                    break;
                default:
                    throw new UnsupportedOperationException("Peptide mapper type " + peptideMapperType + " not supported.");
            }
        }

        return defaultPeptideMapper;
    }

    /**
     * Returns an iterator of all the headers in the FASTA file. Note: when
     * reaching the end of the file the connection will be closed. Do it using
     * the close() method if the end is never reached.
     *
     * @param targetOnly boolean indicating whether only target accessions shall
     * be iterated
     * @return a header iterator
     * 
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public HeaderIterator getHeaderIterator(boolean targetOnly) throws FileNotFoundException {
        return new HeaderIterator(currentFastaFile, targetOnly);
    }

    /**
     * Returns an iterator of all the proteins in the FASTA file. Note: when
     * reaching the end of the file the connection will be closed. Do it using
     * the close() method if the end is never reached.
     *
     * @param targetOnly boolean indicating whether only target accessions shall
     * be iterated
     *
     * @return a protein iterator
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     */
    public ProteinIterator getProteinIterator(boolean targetOnly) throws FileNotFoundException {
        return new ProteinIterator(currentFastaFile, targetOnly);
    }

    /**
     * Returns whether decoys should be kept in memory.
     *
     * @return true if decoys should be kept in memory
     */
    public boolean isDecoyInMemory() {
        return decoyInMemory;
    }

    /**
     * Sets whether decoys should be kept in memory.
     *
     * @param decoyInMemory true if decoys should be kept in memory
     */
    public void setDecoyInMemory(boolean decoyInMemory) {
        this.decoyInMemory = decoyInMemory;
    }

    /**
     * Convenience iterator iterating the headers of a FASTA file without using
     * the cache. The order is the one in the FASTA file.
     */
    public class HeaderIterator {

        /**
         * The header of the next protein.
         */
        private Header nextHeader = null;
        /**
         * The buffered reader.
         */
        private BufferedReader br;
        /**
         * Boolean indicating whether target protein only should be iterated.
         */
        private final boolean targetOnly;

        /**
         * Constructor.
         *
         * @param targetOnly if true only target proteins will be iterated
         * @param file the FASTA file to iterate
         * @throws FileNotFoundException if a FileNotFoundException occurs
         */
        public HeaderIterator(File file, boolean targetOnly) throws FileNotFoundException {
            this.targetOnly = targetOnly;
            br = new BufferedReader(new FileReader(file));
        }

        /**
         * Returns true if there is a next header.
         *
         * @return true if there is a next header
         * @throws IOException if a IOException occurs
         */
        public boolean hasNext() throws IOException {
            nextHeader = null;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) {
                    if (line.startsWith(">")) {
                        nextHeader = Header.parseFromFASTA(line);
                        if (!targetOnly || !isDecoyAccession(nextHeader.getAccession())) {
                            break;
                        } else {
                            nextHeader = null;
                        }
                    }
                }
            }
            if (nextHeader != null) {
                return true;
            } else {
                close();
                return false;
            }
        }

        /**
         * Returns the next header in the FASTA file.
         *
         * @return the next header in the FASTA file
         */
        public Header getNext() {
            return nextHeader;
        }

        /**
         * Closes the connection to the file.
         *
         * @throws IOException if a IOException occurs
         */
        public void close() throws IOException {
            br.close();
        }
    }

    /**
     * Convenience iterator iterating all proteins in a FASTA file without using
     * index or cache.
     */
    public class ProteinIterator {

        /**
         * The header of the next protein.
         */
        private Header nextHeader = null;

        /**
         * The next protein.
         */
        private Protein nextProtein = null;

        /**
         * The buffered reader.
         */
        private BufferedReader br;
        /**
         * Boolean indicating whether target protein only should be iterated.
         */
        private final boolean targetOnly;

        /**
         * Constructor.
         *
         * @param targetOnly if true only target proteins will be iterated
         * @param file the FASTA file.
         *
         * @throws FileNotFoundException if a FileNotFoundException occurs
         */
        public ProteinIterator(File file, boolean targetOnly) throws FileNotFoundException {
            this.targetOnly = targetOnly;
            br = new BufferedReader(new FileReader(file));
        }

        /**
         * Returns true if there is another protein.
         *
         * @return true if there is another protein
         *
         * @throws IOException if an IOException occurs
         */
        public boolean hasNext() throws IOException {

            nextProtein = null;
            String sequence = "";
            Header header = nextHeader;
            boolean newHeaderFound = false;

            String line = br.readLine();

            // reached end of file
            if (line == null) {
                return false;
            }

            while (line != null) {
                if (line.startsWith(">")) {
                    Header tempHeader = Header.parseFromFASTA(line);
                    String accession = tempHeader.getAccessionOrRest();
                    if (targetOnly && isDecoyAccession(accession)) {
                        while ((line = br.readLine()) != null) {
                            if (line.startsWith(">")) {
                                tempHeader = Header.parseFromFASTA(line);
                                if (!isDecoyAccession(tempHeader.getAccessionOrRest())) {
                                    break;
                                }
                            }
                        }
                        if (line == null) {
                            break;
                        }
                    }
                    if (header == null) {
                        header = tempHeader;
                    } else {
                        nextHeader = tempHeader;
                        newHeaderFound = true;
                        break;
                    }
                } else {
                    sequence += line.trim();
                }

                line = br.readLine();
            }
            if (newHeaderFound || line == null) { // line == null means that we read the last protein
                String accession = header.getAccessionOrRest();
                nextProtein = new Protein(accession, header.getDatabaseType(), sequence, isDecoyAccession(accession));
                currentHeaderMap.put(accession, header);
                return true;
            } else {
                close();
                return false;
            }
        }

        /**
         * Returns the next protein.
         *
         * @return the next protein
         */
        public Protein getNextProtein() {
            return nextProtein;
        }

        /**
         * Closes the connection to the file.
         *
         * @throws IOException if an IOException occurs
         */
        public void close() throws IOException {
            br.close();
        }
    }
}
