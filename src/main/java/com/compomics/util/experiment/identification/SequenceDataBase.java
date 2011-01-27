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

    public static final String decoyFlag = "REV";  // This ough not to be hard coded
    private String name;
    private String version;
    private HashMap<String, Protein> proteinMap = new HashMap<String, Protein>();
    private int nTargetSequences = 0;

    public SequenceDataBase(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Protein getProtein(String proteinKey) {
        return proteinMap.get(proteinKey);
    }

    public void addProtein(Protein protein) {
        proteinMap.put(protein.getProteinKey(), protein);
        if (!protein.isDecoy()) {
            nTargetSequences++;
        }
    }

    public int getNumberOfTargetSequences() {
        return nTargetSequences;
    }

    public Set<String> getProteinList() {
        return proteinMap.keySet();
    }

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
                header = line;
                accession = fastaHeaderParser.getProteinAccession(header);
                description = fastaHeaderParser.getProteinDescription(header);
                decoy = accession.contains(decoyFlag);
                if (!sequence.equals("")) {
                    newProtein = new Protein(accession, description, sequence, decoy);
                    proteinMap.put(accession, newProtein);
                    if (!decoy) {
                        nTargetSequences++;
                    }
                }
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
}
