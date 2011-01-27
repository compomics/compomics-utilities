package com.compomics.util.experiment.identification;

/**
 * This class will parse headers of fasta files
 *
 * @author Marc
 */
public class FastaHeaderParser {

    /**
     * String found before the protein accession
     */
    private String beforeAccession;
    /**
     * String found after the protein accession
     */
    private String afterAccession;

    /**
     * Constructor for a fasta header parser
     * @param beforeAccession   String found before the protein accession
     * @param afterAccession    String found after the protein accession
     */
    public FastaHeaderParser(String beforeAccession, String afterAccession) {
        if (beforeAccession.equals("")) {
            beforeAccession = ">";
        }
        this.beforeAccession = beforeAccession;
        this.afterAccession = afterAccession;
    }

    /**
     * Returns the protein accession from a fasta header
     * @param header the fasta header to parse
     * @return  The corresponding accession
     */
    public String getProteinAccession(String header) {
        int start = header.indexOf(beforeAccession);
        int end = header.indexOf(afterAccession, ++start);
        return header.substring(start, end);
    }

    /**
     * Returns the Protein description from a fasta header
     * @param header the fasta header to parse
     * @return The corresponding description
     */
    public String getProteinDescription(String header) {
        int start = header.indexOf(beforeAccession);
        int end = header.indexOf(afterAccession, ++start);
        return header.substring(++end);
    }
}
