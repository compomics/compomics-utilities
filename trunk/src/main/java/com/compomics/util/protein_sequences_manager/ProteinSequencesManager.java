package com.compomics.util.protein_sequences_manager;

import com.compomics.util.protein_sequences_manager.enums.SequenceInputType;
import com.compomics.util.Util;
import com.compomics.util.experiment.identification.FastaIndex;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * The protein sequences manager helps the user managing FASTA files.
 *
 * @author Marc Vaudel
 */
public class ProteinSequencesManager {

    /**
     * Name of the folder containing temporary files.
     */
    public static final String TEMP_FOLDER = ".temp";
    /**
     * Name of the folder containing UniProt files.
     */
    public static final String UNIPROT_FOLDER = "uniprot";
    /**
     * Name of the folder containing user FASTA files.
     */
    public static final String USER_FOLDER = "user";
    /**
     * Name of the folder containing DNA translated files.
     */
    public static final String DNA_FOLDER = "dna";
    /**
     * The list of databases loaded.
     */
    private ArrayList<String> databaseNames;
    /**
     * Map of input types for every database name.
     */
    private HashMap<String, SequenceInputType> databaseInputTypes;
    /**
     * Map of the index for every database: name - version - FastaIndex.
     */
    private HashMap<String, HashMap<String, FastaIndex>> databaseIndexes;
    /**
     * The working folder.
     */
    private File workingFolder;

    /**
     * Constructor. The Protein Sequences Manager folder must be set in the
     * utilities preferences before calling the constructor.
     */
    public ProteinSequencesManager() {
        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        workingFolder = utilitiesUserPreferences.getProteinSequencesManagerFolder();
        if (workingFolder == null || !workingFolder.exists()) {
            throw new IllegalArgumentException("Working folder not set.");
        }
        parseWorkingFolder();
    }

    /**
     * Parses the databases found in the working folder.
     */
    private void parseWorkingFolder() {
        databaseNames = new ArrayList<String>();
        databaseInputTypes = new HashMap<String, SequenceInputType>();
        databaseIndexes = new HashMap<String, HashMap<String, FastaIndex>>();
        parseSubFolder(getUniprotFolder(), SequenceInputType.uniprot);
        parseSubFolder(getUserFolder(), SequenceInputType.user);
        parseSubFolder(getDnaFolder(), SequenceInputType.dna);
    }

    /**
     * Parses a folder containing database folders and loads the content in the
     * maps.
     *
     * @param folder the folder to inspect
     * @param sequenceInputType the type of input
     */
    private void parseSubFolder(File folder, SequenceInputType sequenceInputType) {
        if (!folder.exists()) {
            folder.mkdirs();
            if (!folder.exists()) {
                throw new IllegalArgumentException("Impossible to write into the working folder.");
            }
            return;
        }
        for (File fastaFolder : folder.listFiles()) {
            if (fastaFolder.isDirectory()) {
                String name = fastaFolder.getName();
                boolean dbFound = false;
                for (File versionFolder : fastaFolder.listFiles()) {
                    if (versionFolder.isDirectory()) {
                        String version = versionFolder.getName();
                        FastaIndex fastaIndex = null;
                        for (File subFile : versionFolder.listFiles()) {
                            if (subFile.getName().endsWith(".cui")) {
                                try {
                                    FastaIndex tempIndex = (FastaIndex) SerializationUtils.readObject(subFile);
                                    String correctedName = correctFastaName(tempIndex.getName());
                                    if (correctedName.equals(name)) {
                                        File fastaFile = new File(versionFolder, tempIndex.getFileName());
                                        if (fastaFile.exists()) {
                                            fastaIndex = tempIndex;
                                        }
                                    }
                                } catch (Exception e) {
                                    // ignore
                                }
                            }
                        }
                        if (fastaIndex != null) {
                            dbFound = true;
                            databaseNames.add(fastaIndex.getName());
                            databaseInputTypes.put(name, sequenceInputType);
                            HashMap<String, FastaIndex> fastaMap = databaseIndexes.get(name);
                            if (fastaMap == null) {
                                fastaMap = new HashMap<String, FastaIndex>(1);
                                databaseIndexes.put(name, fastaMap);
                            }
                            fastaMap.put(version, fastaIndex);
                        } else {
                            // corrupted folder, delete
                            Util.deleteDir(versionFolder);
                        }
                    }
                }
                if (!dbFound) {
                    // corrupted folder, delete
                    Util.deleteDir(fastaFolder);
                }
            }
        }
    }

    /**
     * Returns the folder where UniProt databases are stored.
     *
     * @return the folder where UniProt databases are stored
     */
    public File getUniprotFolder() {
        return new File(workingFolder, UNIPROT_FOLDER);
    }

    /**
     * Returns the folder where user databases are stored.
     *
     * @return the folder where user databases are stored
     */
    public File getUserFolder() {
        return new File(workingFolder, USER_FOLDER);
    }

    /**
     * Returns the folder where DNA databases are stored.
     *
     * @return the folder where DNA databases are stored
     */
    public File getDnaFolder() {
        return new File(workingFolder, DNA_FOLDER);
    }

    /**
     * Adds a FASTA file to the working folder.
     *
     * @param fastaFile the FASTA file to add
     * @param sequenceInputType the type of input
     * @param waitingHandler a waiting handler displaying progress and allowing
     * canceling the operation.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * copying the file.
     */
    public void addFastaFile(File fastaFile, SequenceInputType sequenceInputType, WaitingHandler waitingHandler) throws IOException {
        FastaIndex tempIndex = SequenceFactory.getFastaIndex(fastaFile, false, waitingHandler);
        if (!waitingHandler.isRunCanceled()) {
            String fastaName = tempIndex.getName();
            File folder = getFolder(sequenceInputType);
            folder = new File(folder, fastaName);
            String version = tempIndex.getVersion();
            folder = new File(folder, version);
            folder.mkdirs();
            if (!folder.exists()) {
                throw new IllegalArgumentException("Impossible to write into the working folder.");
            }
            String fileName = fastaFile.getName();
            fileName = correctFastaName(fileName);
            File importedFile = new File(folder, fileName);
            Util.copyFile(fastaFile, importedFile);
            
            //add all user specifications to the new index
            FastaIndex newIndex = SequenceFactory.getFastaIndex(fastaFile, true, waitingHandler);
            newIndex.setName(tempIndex.getName());
            newIndex.setAccessionParsingRule(tempIndex.getAccessionParsingRule());
            newIndex.setDecoyTag(tempIndex.getDecoyTag());
            newIndex.setDescription(tempIndex.getDescription());
            newIndex.setMainDatabaseType(tempIndex.getMainDatabaseType());
            newIndex.setVersion(tempIndex.getVersion());
            SequenceFactory.writeIndex(newIndex, folder);
            
            // add fasta file to the mapping
            if (!databaseNames.contains(fastaName)) {
                databaseNames.add(fastaName);
                databaseInputTypes.put(fastaName, sequenceInputType);
            }
            HashMap<String, FastaIndex> databaseMap = databaseIndexes.get(fastaName);
            if (databaseMap == null) {
                databaseMap = new HashMap<String, FastaIndex>(1);
                databaseIndexes.put(fastaName, databaseMap);
            }
            databaseMap.put(version, newIndex);
        }
    }

    /**
     * Corrects the name of the given fasta file.
     *
     * @param fastaName the name of the given fasta file
     *
     * @return a corrected name for the given fasta file
     */
    public static String correctFastaName(String fastaName) {
        return fastaName.replaceAll(" ", "_");
    }

    /**
     * Returns the folder to be used for the given input type.
     *
     * @param sequenceInputType the type of input
     *
     * @return the folder to be used for the given input type
     */
    public File getFolder(SequenceInputType sequenceInputType) {
        switch (sequenceInputType) {
            case uniprot:
                return new File(workingFolder, UNIPROT_FOLDER);
            case user:
                return new File(workingFolder, USER_FOLDER);
            case dna:
                return new File(workingFolder, DNA_FOLDER);
            default:
                throw new UnsupportedOperationException("Folder not implemented for input type " + sequenceInputType + ".");
        }
    }

    /**
     * Returns the temporary folder.
     *
     * @return the temporary folder
     */
    public File getTempFolder() {
        return new File(workingFolder, TEMP_FOLDER);
    }

    /**
     * Returns the list of database names parsed from the working folder.
     *
     * @return the list of database names parsed from the working folder
     */
    public ArrayList<String> getDatabaseNames() {
        return databaseNames;
    }

    /**
     * Returns the list of versions for the given database name as parsed from
     * the working folder.
     *
     * @param databaseName the name of the database of interest
     *
     * @return the list of versions for the given database name as parsed from
     * the working folder
     */
    public ArrayList<String> getVersionsForDb(String databaseName) {
        ArrayList<String> result = new ArrayList<String>(databaseIndexes.get(databaseName).keySet());
        Collections.sort(result);
        return result;
    }

    /**
     * Returns the FASTA index of the given database name and version.
     *
     * @param databaseName the name of the database of interest
     * @param version the version of the database of interest
     *
     * @return the corresponding FASTA index
     */
    public FastaIndex getFastaIndex(String databaseName, String version) {
        HashMap<String, FastaIndex> databaseMap = databaseIndexes.get(databaseName);
        if (databaseMap == null) {
            return null;
        }
        return databaseMap.get(version);
    }

    /**
     * Returns the input type of the given database.
     *
     * @param databaseName the name of the database
     *
     * @return the input type of the given database
     */
    public SequenceInputType getInputType(String databaseName) {
        return databaseInputTypes.get(databaseName);
    }
}
