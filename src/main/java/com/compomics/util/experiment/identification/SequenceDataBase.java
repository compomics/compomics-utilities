package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.protein.Header;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * This class models a protein database used for peptide/protein identification (like uniprot)
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SequenceDataBase extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -8651416887737619199L;
    /**
     * Flag for a decoy protein
     */
    public static final String decoyFlag = "REVERSED";
    /**
     * Name of the database
     */
    private String name;
    /**
     * Version of the database
     */
    private String version;
    /**
     * Protein map with proteins indexed by their key.
     */
    private HashMap<String, Protein> proteinMap = new HashMap<String, Protein>();
    /**
     * Protein header map with headers indexed by the protein key. The protein header contains all descriptive information given in the database file.
     */
    private HashMap<String, Header> headerMap = new HashMap<String, Header>();
    /**
     * Number of targeted sequences loaded
     */
    private int nTargetSequences = 0;

    /**
     * Constructor for a sequence database.
     */
    public SequenceDataBase() {
    }
    
    /**
     * Constructor for a sequence database.
     * 
     * @param name      Name of the database
     * @param version   Version of the database
     */
    public SequenceDataBase(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Returns a protein indexed by its key.
     * 
     * @param proteinKey protein key
     * @return the corresponding protein
     */
    public Protein getProtein(String proteinKey) {
        return proteinMap.get(proteinKey);
    }

    /**
     * Returns the protein header of the desired protein.
     * 
     * @param proteinKey protein key
     * @return the corresponding header
     */
    public Header getProteinHeader(String proteinKey) {
        return headerMap.get(proteinKey);
    }

    /**
     * Adds a protein to the map.
     * 
     * @param protein the new protein
     */
    public void addProtein(Protein protein) {
        proteinMap.put(protein.getProteinKey(), protein);
        if (!protein.isDecoy()) {
            nTargetSequences++;
        }
    }

    /**
     * Returns the number of target hits loaded.
     * 
     * @return the number of target hits loaded
     */
    public int getNumberOfTargetSequences() {
        return nTargetSequences;
    }

    /**
     * Returns the list of all protein imported.
     * 
     * @return list of all protein imported
     */
    public Set<String> getProteinList() {
        return proteinMap.keySet();
    }

    /**
     * Imports a sequence database from a fasta file.
     *
     * @param fastaFile                 The fasta file to import
     * @throws FileNotFoundException    Exception thrown when the fasta file is not found
     * @throws IOException              Exception thrown whenever an error is encountered while parsing the file
     * @throws IllegalArgumentException Exception thrown whenever the fasta header is not of correct format
     */
    public void importDataBase(File fastaFile) throws FileNotFoundException, IOException {
        
        FileReader f = new FileReader(fastaFile);
        BufferedReader b = new BufferedReader(f);

        String line = b.readLine();
        String accession = "", databaseType = null, sequence = "";
        Protein newProtein;
        Header fastaHeader = null;
        boolean decoy = false;

        while (line != null) {
            
            line = line.trim();
            
            if (line.startsWith(">")) {
                if (!sequence.equals("")) {
                    newProtein = new Protein(accession, databaseType, sequence, decoy);
                    proteinMap.put(newProtein.getProteinKey(), newProtein);
                    headerMap.put(newProtein.getProteinKey(), fastaHeader);
                    
                    if (!decoy) {
                        nTargetSequences++;
                    }
                }
                
                fastaHeader = Header.parseFromFASTA(line);
                accession = fastaHeader.getAccession();
                databaseType = fastaHeader.getDatabaseType();

                if (accession != null) {
                    decoy = accession.contains(decoyFlag);
                } else {
                    decoy = false;
                }

                sequence = "";
            } else {
                sequence += line;
            }
            
            line = b.readLine();
        }
        
        newProtein = new Protein(accession, databaseType, sequence, decoy);
        proteinMap.put(newProtein.getProteinKey(), newProtein);
        headerMap.put(newProtein.getProteinKey(), fastaHeader);
        
        if (!decoy) {
            nTargetSequences++;
        }
    }
    
    /**
     * Appends reversed sequences to the database.
     * 
     * @throws IllegalArgumentException     Exception thrown if the database already contains decoy sequences
     */
    public void appendDecoySequences() {
        appendDecoySequences(null);
    }

    /**
     * Appends reversed sequences to the database.
     * 
     * @param progressDialog                The progress dialog, can be null
     * @throws IllegalArgumentException     Exception thrown if the database already contains decoy sequences
     */
    public void appendDecoySequences(ProgressDialogX progressDialog) {
        
        if (nTargetSequences < proteinMap.size()) {
            throw new IllegalArgumentException("The database already contains decoy sequences!");
        }
        
        ArrayList<String> proteinKeys = new ArrayList<String>(proteinMap.keySet());
        
        if (progressDialog != null) {
            progressDialog.setIntermidiate(false);
            progressDialog.setMax(proteinKeys.size());
        }
        
        int counter = 1;
        
        for (String key : proteinKeys) {
            
            if (progressDialog != null) {
                progressDialog.setValue(counter++);
            }
            
            Header decoyHeader = Header.parseFromFASTA(headerMap.get(key).toString());
            decoyHeader.setAccession(decoyHeader.getAccession() + "_" + decoyFlag);
            decoyHeader.setDescription(decoyHeader.getDescription() + "-" + decoyFlag);
            Protein decoyProtein = new Protein(decoyHeader.getAccession(), null, reverseSequence(proteinMap.get(key).getSequence()), true);

            proteinMap.put(decoyProtein.getProteinKey(), decoyProtein);
            headerMap.put(decoyProtein.getProteinKey(), decoyHeader);
        }
        
        if (progressDialog != null) {
            progressDialog.setIntermidiate(true);
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
     * Exports the sequence database as a fasta file. Target sequences first then 
     * decoy sequences. Proteins are sorted by accession alphabetic order.
     * 
     * @param fastaFile         The fasta file where sequences should be output
     * @throws IOException      Exception thrown whenever a problem occurred while writing the file.
     */
    public void exportAsFasta(File fastaFile) throws IOException {
        exportAsFasta(fastaFile, null);
    }
    
    /**
     * Exports the sequence database as a fasta file. Target sequences first then 
     * decoy sequences. Proteins are sorted by accession alphabetic order.
     * 
     * @param fastaFile         The fasta file where sequences should be output
     * @param progressDialog    The progress dialog, can be null
     * @throws IOException      Exception thrown whenever a problem occurred while writing the file.
     */
    public void exportAsFasta(File fastaFile, ProgressDialogX progressDialog) throws IOException {
        
        Writer proteinWriter = new BufferedWriter(new FileWriter(fastaFile));
        ArrayList<String> keys = new ArrayList<String>(proteinMap.keySet());
        Collections.sort(keys);
        
        // @TODO: check if it is possible to do this without having to iterate the keys twice?
        
        if (progressDialog != null) {
            progressDialog.setIntermidiate(false);
            progressDialog.setMax(keys.size()*2);
        }
        
        int counter = 1;

        for (String proteinKey : keys) {
            
            if (progressDialog != null) {
                progressDialog.setValue(counter++);
            }
            
            if (!proteinMap.get(proteinKey).isDecoy()) {
                proteinWriter.write(headerMap.get(proteinKey).toString() + "\n");
                proteinWriter.write(proteinMap.get(proteinKey).getSequence() + "\n");
            }
        }
        
        for (String proteinKey : keys) {
            
            if (progressDialog != null) {
                progressDialog.setValue(counter++);
            }
            
            if (proteinMap.get(proteinKey).isDecoy()) {
                proteinWriter.write(headerMap.get(proteinKey).toString() + "\n");
                proteinWriter.write(proteinMap.get(proteinKey).getSequence() + "\n");
            }
        }
        
        if (progressDialog != null) {
            progressDialog.setIntermidiate(true);
        }
        
        proteinWriter.close();
    }

    /**
     * Returns the database name.
     *
     * @return the database name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the database version.
     *
     * @return the database version
     */
    public String getVersion() {
        return version;
    }
}
