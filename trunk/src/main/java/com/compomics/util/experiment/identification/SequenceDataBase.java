package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * This class models a protein database used for peptide/protein identification (like uniprot)
 *
 * @author Marc
 */
public class SequenceDataBase extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -8651416887737619199L;
    /**
     * Flag for a decoy protein
     */
    public static final String decoyFlag = "REV";  // This ough not to be hard coded
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
     * Number of targeted sequences loaded
     */
    private int nTargetSequences = 0;

    /**
     * Constructor for a sequence database
     * @param name      Name of the database
     * @param version   Version of the database
     */
    public SequenceDataBase(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Returns a protein indexed by its key
     * @param proteinKey protein key
     * @return the corresponding key
     */
    public Protein getProtein(String proteinKey) {
        return proteinMap.get(proteinKey);
    }

    /**
     * Adds a protein to the map
     * @param protein the new protein
     */
    public void addProtein(Protein protein) {
        proteinMap.put(protein.getProteinKey(), protein);
        if (!protein.isDecoy()) {
            nTargetSequences++;
        }
    }

    /**
     * returns the number of target hits loaded
     * @return the number of target hits loaded
     */
    public int getNumberOfTargetSequences() {
        return nTargetSequences;
    }

    /**
     * Returns the list of all protein imported
     * @return list of all protein imported
     */
    public Set<String> getProteinList() {
        return proteinMap.keySet();
    }

    /**
     * Imports a sequence database from a fasta file
     *
     * @param fastaHeaderParser         Parser for a fasta header
     * @param fastaFile                 The fasta file to import
     * @throws FileNotFoundException    Error thrown when the fasta file is not found
     * @throws IOException              Error thrown whenever an error is encountered while parsing the file
     */
    public void importDataBase(FastaHeaderParser fastaHeaderParser, File fastaFile) throws FileNotFoundException, IOException {
        FileReader f = new FileReader(fastaFile);
        BufferedReader b = new BufferedReader(f);

        String header, line = b.readLine();
        String accession = "", description = "", sequence = "";
        Protein newProtein;
        boolean decoy = false;

        while (line != null) {
            line = line.trim();
            if (line.startsWith(">")) {
                if (!sequence.equals("")) {
                    newProtein = new Protein(accession, description, sequence, decoy);
                    proteinMap.put(accession, newProtein);
                    if (!decoy) {
                        nTargetSequences++;
                    }
                }
                header = line;
                accession = fastaHeaderParser.getProteinAccession(header);
                description = fastaHeaderParser.getProteinDescription(header);
                decoy = accession.contains(decoyFlag);
                sequence = "";
            } else {
                sequence += line;
            }
            line = b.readLine();
        }
        newProtein = new Protein(accession, description, sequence, decoy);
        proteinMap.put(accession, newProtein);
        if (!decoy) {
            nTargetSequences++;
        }
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
