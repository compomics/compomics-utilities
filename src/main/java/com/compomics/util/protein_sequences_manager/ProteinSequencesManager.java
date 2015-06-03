package com.compomics.util.protein_sequences_manager;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.FastaIndex;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;

/**
 * The protein sequences manager helps the user managing his fasta files.
 *
 * @author Marc Vaudel
 */
public class ProteinSequencesManager {

    /**
     * Name of the folder containing temporary files.
     */
    public static final String TEMP_FOLDER = ".temp";
    /**
     * Name of the folder containing uniprot files.
     */
    public static final String UNIPROT_FOLDER = "uniprot";
    /**
     * Name of the folder containing user fasta files.
     */
    public static final String USER_FOLDER = "user";
    /**
     * Name of the folder containing DNA translated files
     */
    public static final String DNA_FOLDER = "dna";

    /**
     * Adds a user fasta file to the folder.
     * 
     * @param userFastaFile the fasta file to add
     * @param waitingHandler a waiting handler displaying progress and allowing cancelling the operation.
     * 
     * @throws IOException exception thrown whenever an error occurred while copying the file.
     */
    public static void addUserFastaFile(File userFastaFile, WaitingHandler waitingHandler) throws IOException {
        FastaIndex tempIndex = SequenceFactory.getFastaIndex(userFastaFile, true, waitingHandler);
        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (utilitiesUserPreferences.getDbFolder() == null || !utilitiesUserPreferences.getDbFolder().exists()) {
            throw new IllegalArgumentException("Database folder not set.");
        }
        File folder = utilitiesUserPreferences.getDbFolder();
        String fastaName = tempIndex.getName();
        folder = new File(folder, fastaName);
        String version = tempIndex.getVersion();
        folder = new File(folder, version);
        folder.mkdirs();
        String fileName = userFastaFile.getName();
        fileName = fileName.replaceAll(" ", "_");
        File importedFile = new File(folder, fileName);
        Util.copyFile(userFastaFile, importedFile);
        //add all user specifications to the new index
        FastaIndex newIndex = SequenceFactory.getFastaIndex(userFastaFile, true, waitingHandler);
        newIndex.setName(tempIndex.getName());
        newIndex.setAccessionParsingRule(tempIndex.getAccessionParsingRule());
        newIndex.setDecoyTag(tempIndex.getDecoyTag());
        newIndex.setDescription(tempIndex.getDescription());
        newIndex.setMainDatabaseType(tempIndex.getMainDatabaseType());
        newIndex.setVersion(tempIndex.getVersion());
        SequenceFactory.writeIndex(newIndex, folder);
    }
}
