package com.compomics.util.experiment.identification;

/**
 * This class will parse headers of fasta files
 *
 * @author Marc
 */
public class FastaHeaderParser {

    private String beforeAccession;
    private String afterAccession;

    public FastaHeaderParser(String beforeAccession, String afterAccession) {
        if (beforeAccession.equals("")) {
            beforeAccession = ">";
        }
        this.beforeAccession = beforeAccession;
        this.afterAccession = afterAccession;
    }

    public String getProteinAccession(String header) {
        int start = header.indexOf(beforeAccession);
        int end = header.indexOf(afterAccession, ++start);
        return header.substring(start, end);
    }

    public String getProteinDescription(String header) {
        int start = header.indexOf(beforeAccession);
        int end = header.indexOf(afterAccession, ++start);
        return header.substring(++end);
    }
}
