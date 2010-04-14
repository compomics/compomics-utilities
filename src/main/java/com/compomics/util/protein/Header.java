/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 7-okt-02
 * Time: 13:43:28
 */
package com.compomics.util.protein;
import org.apache.log4j.Logger;

import java.util.StringTokenizer;

/**
 * This class represents the header for a Protein instance.
 * It is meant to work closely with FASTA format notation. The Header class knows how to
 * handle certain often-used headers such as SwissProt and NCBI formatted FASTA headers.<br />
 * Note that the Header class is it's own factory, and should be used as such.
 *
 * @author Lennart Martens
 */
public class Header implements Cloneable {
	// Class specific log4j logger for Header instances.
	static Logger logger = Logger.getLogger(Header.class);

    /**
     * Private constructor to force use of factory methods.
     */
    private Header() {
    }

    /**
     * The ID String corresponds to the String that is present as the first
     * element following the opening '>'. It is most notably 'sw' for SwissProt, and
     * 'gi' for NCBI. <br />
     * ID is the first element in the abbreviated header String.
     */
    private String iID = null;

    /**
     * The foreign ID is the ID of another database this entry is orignally from.
     * Most notably used for SwissProt entries in NCBI. <br />
     * The foreign ID String is an addendum to the accession String in the abbreviated
     * header String.
     */
    private String iForeignID = null;

    /**
     * The accession String is the unique identifier for the sequence in the respective database.
     * Note that for NCBI, the accession number also defines a unique moment in time. <br />
     * Accession String is the second element in the abbreviated header String.
     */
    private String iAccession = null;

    /**
     * The foreign accession String is an accession String in another database of significance.
     * Most notably used for SwissProt accessions that are kept in the NCBI database. <br />
     * The foreign accession String is an addendum to the foreign ID String in the abbreviated header String.
     */
    private String iForeignAccession = null;

    /**
     * The description is a more or less elaborate description of the protein in question. <br />
     * The description is the third element (and final) in the abbreviated header String.
     */
    private String iDescription = null;

    /**
     * The foreign Description is a description for an entry in another DB.
     * Most notably, the SwissProt short description for an entry that is found within
     * NCBI. <br />
     * The foreign description is an addendum to the foreign accession String in the abbreviated
     * header String.
     */
    private String iForeignDescription = null;

    /**
     * This variable holds all unidentified parts for the Header.
     * If the String was not (recognized as) a standard SwissProt or
     * NCBI header, this variable holds the entire header.
     */
    private String iRest = null;

    /**
     * This StringBuffer holds all the addenda for this header.
     */
    private StringBuffer iAddenda = null;

    /**
     * This variable holds a possible startindex for the associated sequence
     */
    private int iStart = -1;

    /**
     * This variable holds a possible endindex for the associated sequence
     */
    private int iEnd = -1;


    /**
     * Factory method that constructs a Header instance based on a FASTA header line.
     *
     * @param   aFASTAHeader    the String with the original FASTA header line.
     * @return  Header  with the Header instance representing the given header. The object
     *                  returned will have been parsed correctly if it is a standard SwissProt
     *                  or NCBI formatted header, and will be plain in all other cases.
     */
    public static Header parseFromFASTA(String aFASTAHeader) {
        Header result = null;

        if(aFASTAHeader == null) {
            // Do nothing, just return 'null'.
        } else if(aFASTAHeader.trim().equals("")) {
            result = new Header();
            result.iRest = "";
        } else {
            result = new Header();
            // Remove leading '>', if present.
            if(aFASTAHeader.startsWith(">")) {
                aFASTAHeader = aFASTAHeader.substring(1);
            }

            // Now check for the possible presence of addenda in the header.
            // First check the description for addenda, and if that should fail, give 'Rest' a chance.
            int liPos = -1;
            if((liPos = aFASTAHeader.indexOf("^A")) >= 0) {
                result.iAddenda = new StringBuffer(aFASTAHeader.substring(liPos));
                aFASTAHeader = aFASTAHeader.substring(0, liPos);
            }
            try {
                // First determine what kind of Header we've got.
                if(aFASTAHeader.startsWith("sw|") || aFASTAHeader.startsWith("SW|")) {
                    // SwissProt.
                    // We need to find three elements:
                    //   - the ID (sw, we already know that one).
                    //   - the accession String (easily retrieved as the next String).
                    //   - the description (composed of the short description and the longer,
                    //     verbose description)
                    StringTokenizer lSt = new StringTokenizer(aFASTAHeader, "|");

                    // There should be at least three tokens.
                    if(lSt.countTokens() < 3) {
                        throw new IllegalArgumentException("Non-standard or false SwissProt header passed. Expecting something like: '>sw|Pxxxx|ACTB_HUMAN xxxx xxx xxxx ...', received '" + aFASTAHeader + "'.");
                    } else {
                        result.iID = lSt.nextToken();
                        result.iAccession = lSt.nextToken();
                        // Check for the presence of a location.
                        int index = -1;
                        if((index = result.iAccession.indexOf(" (")) > 0) {
                            String temp = result.iAccession.substring(index);
                            result.iAccession = result.iAccession.substring(0, index);
                            int open = 2;
                            int minus = temp.indexOf("-");
                            int end = temp.indexOf(")");
                            result.iStart = Integer.parseInt(temp.substring(open, minus));
                            result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                        }
                        result.iDescription = lSt.nextToken();
                        // If there are any more elements, add them to the 'rest' section.
                        if(lSt.hasMoreTokens()) {
                            StringBuffer lBuffer = new StringBuffer();
                            while(lSt.hasMoreTokens()) {
                                lBuffer.append(lSt.nextToken());
                            }
                            result.iRest = lBuffer.toString();
                        }
                    }
                } else if(aFASTAHeader.startsWith("gi|") || aFASTAHeader.startsWith("GI|")) {
                    // NCBI.
                    // We need to check for a number of things here:
                    //   - first of all, we should get the ID (which we already have, 'gi')
                    //   - second is the NCBI accession String
                    //   - third we need to check for a foreign ID and accession
                    //   - If there is a foreign accession, there could also be a description
                    //     associated. Get that one too.
                    //   - finally, get the full NCBI description.
                    StringTokenizer lSt = new StringTokenizer(aFASTAHeader, "|");

                    // We expect to see either two or at least four or more tokens.
                    int tokenCount = lSt.countTokens();
                    if(tokenCount == 3) {
                        result.iID = lSt.nextToken();
                        result.iAccession = lSt.nextToken();
                        // Check for the presence of a location.
                        int index = -1;
                        if((index = result.iAccession.indexOf(" (")) > 0) {
                            String temp = result.iAccession.substring(index);
                            result.iAccession = result.iAccession.substring(0, index);
                            int open = 2;
                            int minus = temp.indexOf("-");
                            int end = temp.indexOf(")");
                            result.iStart = Integer.parseInt(temp.substring(open, minus));
                            result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                        }
                        result.iDescription = lSt.nextToken().trim();
                    } else if(tokenCount < 4) {
                        throw new IllegalArgumentException("Non-standard or false NCBInr header passed. Expecting something like: '>gi|xxxxx|xx|xxxxx|(x) xxxx xxx xxxx ...', received '" + aFASTAHeader + "'.");
                    } else {
                        result.iID = lSt.nextToken();
                        result.iAccession = lSt.nextToken();
                        // Check for the presence of a location.
                        int index = -1;
                        if((index = result.iAccession.indexOf(" (")) > 0) {
                            String temp = result.iAccession.substring(index);
                            result.iAccession = result.iAccession.substring(0, index);
                            int open = 2;
                            int minus = temp.indexOf("-");
                            int end = temp.indexOf(")");
                            result.iStart = Integer.parseInt(temp.substring(open, minus));
                            result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                        }
                        result.iForeignID = lSt.nextToken();
                        // Only retrieve the foreign accession if it is specifed (meaning a token count of 5).
                        if(tokenCount >= 5) {
                            result.iForeignAccession = lSt.nextToken();
                        }
                        // Append all the rest, regardless of further pipes (which have no structural meaning anymore).
                        StringBuffer lSB = new StringBuffer();
                        while(lSt.hasMoreTokens()) {
                            lSB.append(lSt.nextToken());
                        }
                        String temp = lSB.toString();
                        if(temp.startsWith(" ")) {
                            // Only description present.
                            result.iDescription = temp.substring(1);
                        } else {
                            // Up to the first space is foreign description.
                            int location = temp.indexOf(" ");
                            result.iForeignDescription = temp.substring(0, location);
                            result.iDescription = temp.substring(location+1);
                        }
                    }

                } else if(aFASTAHeader.startsWith("IPI:") || aFASTAHeader.startsWith("ipi:") || aFASTAHeader.startsWith("IPI|") || aFASTAHeader.startsWith("ipi|")) {
                        // An IPI header looks like:
                        // >IPI:IPIxxxxxx.y|REFSEQ_XP:XP_aaaaa[|many more like this can be present] Tax_Id=9606 descr
                        result.iID = "IPI";
                        result.iAccession = aFASTAHeader.substring(4, aFASTAHeader.indexOf("|", 4));
                        // Check for the presence of a location.
                        int index = -1;
                        if((index = result.iAccession.indexOf(" (")) > 0) {
                            String temp = result.iAccession.substring(index);
                            result.iAccession = result.iAccession.substring(0, index);
                            int open = 2;
                            int minus = temp.indexOf("-");
                            int end = temp.indexOf(")");
                            result.iStart = Integer.parseInt(temp.substring(open, minus));
                            result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                        }
                        // Take everything from the first '|' we meet after the accession number.
                        result.iDescription = aFASTAHeader.substring(aFASTAHeader.indexOf("|", 5)+1);
                } else if(aFASTAHeader.startsWith("HIT")) {
                    try {
                        //http://www.h-invitational.jp/
                        // A H-Invitation database entry looks like:
                        // >HIT000000001.10|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.
                        result.iID = "";
                        result.iAccession = aFASTAHeader.substring(0, aFASTAHeader.indexOf("|"));
                        // Check for the presence of a location.
                        int index = -1;
                        if((index = result.iAccession.indexOf(" (")) > 0) {
                            String temp = result.iAccession.substring(index);
                            result.iAccession = result.iAccession.substring(0, index);
                            int open = 2;
                            int minus = temp.indexOf("-");
                            int end = temp.indexOf(")");
                            result.iStart = Integer.parseInt(temp.substring(open, minus));
                            result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                        }
                        // Take everything from the first '|' we meet after the accession number.
                        result.iDescription = aFASTAHeader.substring(aFASTAHeader.indexOf("|")+1);
                    } catch(Exception excep) {
                        logger.error(excep.getMessage(), excep);
                        logger.info(aFASTAHeader);
                    }
                } else if(aFASTAHeader.startsWith("OE")) {
                    // Halobacterium header from the Max Planck people.
                    // We need to find two elements:
                    //   - the accession String (easily retrieved as the next String until a space is encountered).
                    //   - the description
                    int accessionEndLoc = aFASTAHeader.indexOf(" ");
                    if(accessionEndLoc < 0 || aFASTAHeader.length() < (accessionEndLoc + 4)) {
                        throw new IllegalArgumentException("Non-standard Halobacterium (Max Planck) header passed. Expecting something like '>OExyz (OExyz) xxx xxx xxx', but was '" + aFASTAHeader + "'!");
                    }
                    // Now we have to see if there is location information present.
                    // This is a bit tricky here, because the accession number itself is repeated between '()' after the space.
                    if(aFASTAHeader.charAt(accessionEndLoc+1) ==  '(' && Character.isDigit(aFASTAHeader.charAt(accessionEndLoc+2))) {
                        // start and end found. Add it to the accession number and remove it from the description.
                        accessionEndLoc = aFASTAHeader.indexOf(")", accessionEndLoc) + 1;
                    }
                    result.iID = "";
                    result.iAccession = aFASTAHeader.substring(0, accessionEndLoc).trim();
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                    result.iDescription = aFASTAHeader.substring(accessionEndLoc).trim();
                } else if(aFASTAHeader.startsWith("hflu_")) {
                    // H Influenza header from Novartis.
                    // We need to find two elements:
                    //   - the accession String (easily retrieved as the next String until a space is encountered).
                    //   - the description
                    int accessionEndLoc = aFASTAHeader.indexOf(" ");
                    if(accessionEndLoc < 0) {
                        throw new IllegalArgumentException("Non-standard H Influenza (Novartis) header passed. Expecting something like '>hflu_lsi_xxxx xxx xxx xxx', but was '" + aFASTAHeader + "'!");
                    }
                    // Now we have to see if there is location information present.
                    if(aFASTAHeader.charAt(accessionEndLoc+1) ==  '(' && Character.isDigit(aFASTAHeader.charAt(accessionEndLoc+2))) {
                        // start and end found. Add it to the accession number and remove it from the description.
                        accessionEndLoc = aFASTAHeader.indexOf(")", accessionEndLoc) + 1;
                    }
                    result.iID = "";
                    result.iAccession = aFASTAHeader.substring(0, accessionEndLoc).trim();
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                    result.iDescription = aFASTAHeader.substring(accessionEndLoc).trim();
                } else if(aFASTAHeader.startsWith("C.tr_") || aFASTAHeader.startsWith("C_trachomatis_")) {
                    // C. Trachomatis header.
                    // We need to find two elements:
                    //   - the accession String (retrieved as the actual accession String which lasts up to the first space).
                    //   - the description (everything after the first space).
                    int accessionEndLoc = aFASTAHeader.indexOf(" ");
                    if(accessionEndLoc < 0) {
                        throw new IllegalArgumentException("Non-standard C trachomatis header passed. Expecting something like '>C_tr_Lx_x [xxx - xxx] | xxx xxx ', but was '" + aFASTAHeader + "'!");
                    }
                    // Now we have to see if there is location information present.
                    if(aFASTAHeader.charAt(accessionEndLoc+1) ==  '(' && Character.isDigit(aFASTAHeader.charAt(accessionEndLoc+2))) {
                        // start and end found. Add it to the accession number and remove it from the description.
                        accessionEndLoc = aFASTAHeader.indexOf(")", accessionEndLoc) + 1;
                    }
                    result.iID = "";
                    result.iAccession = aFASTAHeader.substring(0, accessionEndLoc).trim();
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                    result.iDescription = aFASTAHeader.substring(accessionEndLoc).trim();
                } else if(aFASTAHeader.startsWith(" M. tub.")) {
                    // M. Tuberculosis header.
                    // We need to find two elements:
                    //   - the accession String (retrieved as the first pipe-delimited String).
                    //   - the description (everything after the pipe that closes the accession String).
                    int accessionStartLoc = aFASTAHeader.indexOf("|") + 1;
                    int accessionEndLoc = aFASTAHeader.indexOf("|", accessionStartLoc);
                    if(accessionEndLoc < 0) {
                        throw new IllegalArgumentException("Non-standard M tuberculosis header passed. Expecting something like '>M. tub.xxx|Rvxxx| xxx xxx', but was '" + aFASTAHeader + "'!");
                    }
                    result.iID = aFASTAHeader.substring(0, accessionStartLoc-1);
                    result.iAccession = aFASTAHeader.substring(accessionStartLoc, accessionEndLoc).trim();
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                    result.iDescription = aFASTAHeader.substring(accessionEndLoc+1).trim();
                } else if(aFASTAHeader.matches("^CG.* pep:.*")) {
                    // Drosophile DB.
                    // We need to find two elements:
                    //   - the accession String (retrieved as the trimmed version of everything
                    //     up to (and NOT including) " pep:"
                    //   - the description (everything (trimmed) starting from (and including) the " pep:".
                    int pepLoc = aFASTAHeader.indexOf(" pep:");
                    result.iID = "";
                    result.iAccession = aFASTAHeader.substring(0, pepLoc).trim();
                    String possibleDescriptionPrefix = "";
                    // See if there is "(*xE*)" information wrongly assigned to the accession number.
                    if(result.iAccession.indexOf("(*") > 0) {
                        possibleDescriptionPrefix = result.iAccession.substring(result.iAccession.indexOf("(*"), result.iAccession.indexOf("*)") + 2) + " ";
                        result.iAccession = result.iAccession.substring(0, result.iAccession.indexOf("(*"));
                    }
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                    result.iDescription = possibleDescriptionPrefix + aFASTAHeader.substring(pepLoc).trim();
                } else if(aFASTAHeader.matches(".*SGDID:[^\\s]+,.*")) {
                    // OK, SGD entry. The text up to but not including the first space is deemed accession,
                    // everything else is taken as description.
                    // So we need to find two elements:
                    //   - the accession String (taking into account possible location info).
                    //   - the description
                    int accessionEndLoc = aFASTAHeader.indexOf(" ");
                    if(accessionEndLoc < 0) {
                        throw new IllegalArgumentException("Non-standard SGD header passed. Expecting something like '>xxxx xxx SGDID:xxxx xxx', but was '" + aFASTAHeader + "'!");
                    }
                    // Now we have to see if there is location information present.
                    if(aFASTAHeader.charAt(accessionEndLoc+1) ==  '(' && Character.isDigit(aFASTAHeader.charAt(accessionEndLoc+2))) {
                        // start and end found. Add it to the accession number and remove it from the description.
                        accessionEndLoc = aFASTAHeader.indexOf(")", accessionEndLoc) + 1;
                    }
                    result.iID = "";
                    result.iAccession = aFASTAHeader.substring(0, accessionEndLoc).trim();
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                    result.iDescription = aFASTAHeader.substring(accessionEndLoc).trim();
                } else if(aFASTAHeader.matches("^[^\\s]+_[^\\s]+ \\([PQOA][^\\s]+\\) .*")) {
                    // Old (everything before 9.0 release (31 Oct 2006)) standard SwissProt header as
                    // present in the Expasy FTP FASTA file.
                    // Is formatted something like this:
                    //  >XXX_YYYY (acc) rest
                    int start = aFASTAHeader.indexOf(" (");
                    int end = aFASTAHeader.indexOf(") ");
                    result.iAccession = aFASTAHeader.substring(start+2, end);
                    result.iID = "sw";
                    result.iDescription = aFASTAHeader.substring(0, start) + " " + aFASTAHeader.substring(end+2);
                } else if(aFASTAHeader.matches("^[POQA][^\\s]*\\|[^\\s]+_[^\\s]+ .*")) {
                    // New (9.0 release (31 Oct 2006) and beyond) standard SwissProt header as
                    // present in the Expasy FTP FASTA file.
                    // Is formatted something like this:
                    //  >accession|ID descr rest (including taxonomy, if available)
                    result.iAccession = aFASTAHeader.substring(0, aFASTAHeader.indexOf("|")).trim();
                    // See if there is location information.
                    if(aFASTAHeader.matches("[^\\(]+\\([\\d]+ [\\d]\\)$")) {
                        int openBracket = aFASTAHeader.indexOf("(");
                        result.iAccession = aFASTAHeader.substring(0, openBracket).trim();
                        result.iStart = Integer.parseInt(aFASTAHeader.substring(openBracket, aFASTAHeader.indexOf(" ", openBracket)).trim());
                        result.iEnd = Integer.parseInt(aFASTAHeader.substring(aFASTAHeader.indexOf(" ", openBracket), aFASTAHeader.indexOf(")")).trim());
                    }
                    result.iID = "sw";
                    result.iDescription = aFASTAHeader.substring(aFASTAHeader.indexOf("|")+1);
                }  else if(aFASTAHeader.matches("^sp\\|[POQA][^\\s]*\\|[^\\s]+_[^\\s]+ .*")) {
                    // New (September 2008 and beyond) standard SwissProt header as
                    // present in the Expasy FTP FASTA file.
                    // Is formatted something like this:
                    //  >sp|accession|ID descr rest (including taxonomy, if available)
                    String tempHeader = aFASTAHeader.substring(3);
                    result.iAccession = tempHeader.substring(0, tempHeader.indexOf("|")).trim();
                    // See if there is location information.
                    if(tempHeader.matches("[^\\(]+\\([\\d]+ [\\d]\\)$")) {
                        int openBracket = tempHeader.indexOf("(");
                        result.iAccession = tempHeader.substring(0, openBracket).trim();
                        result.iStart = Integer.parseInt(tempHeader.substring(openBracket, tempHeader.indexOf(" ", openBracket)).trim());
                        result.iEnd = Integer.parseInt(tempHeader.substring(tempHeader.indexOf(" ", openBracket), tempHeader.indexOf(")")).trim());
                    }
                    result.iID = "sw";
                    result.iDescription = tempHeader.substring(tempHeader.indexOf("|")+1);
                } else if(aFASTAHeader.startsWith("dm")) {
                    // A personal D. Melanogaster header from translating the dm genome into protein sequences.
                    // We need to find two elements, separated by a space:
                    //   - the accession String (retrieved as the first part of a space delimited String).
                    //   - the nucleic acid start and stop site (between brackets, separated by a '-').
                    //
                    // ex: >dm345_3L-sense [234353534-234353938]
                    int accessionEndLoc = aFASTAHeader.indexOf(" ");
                    if(accessionEndLoc < 0) {
                        throw new IllegalArgumentException("Incorrect D. melanogaster heading. Expecting something like '>dm345_3L-sense [234353534-234353938]', but was '" + aFASTAHeader + "'!");
                    }
                    result.iID = aFASTAHeader.substring(0, accessionEndLoc).trim();
                    // Parse the location.
                    int index1 = -1;
                    int index2 = -1;
                    int separation = -1;
                    if(((index1 = aFASTAHeader.indexOf("[")) > 0) && ((index2 = aFASTAHeader.indexOf("]")) > 0) && ((separation = aFASTAHeader.lastIndexOf("-")) > 0)) {
                        result.iStart = Integer.parseInt(aFASTAHeader.substring(index1+1, separation));
                        result.iEnd = Integer.parseInt(aFASTAHeader.substring(separation+1, index2));
                    }
                    result.iDescription = aFASTAHeader.substring(accessionEndLoc+1).trim();
                } else if(aFASTAHeader.matches("^[^|\t]* [|] [^|]*[|][^|]*[|][^|]*[|][^|]*")) {
                    // The Arabidopsis thaliana database; TAIR format
                    // We need to find two elements, separated by pipes:
                    //   - the accession number with version (retrieved as the part before the first pipe).
                    //   - the description (retrieved as the part between the second and third pipe).
                    //
                    // ex: >AT1G08520.1 | Symbol: PDE166 | magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum) | chr1:2696415-2700961 FORWARD | Aliases: T27G7.20, T27G7_20, PDE166, PIGMENT DEFECTIVE 166
                    int firstPipeLoc = aFASTAHeader.indexOf("|");
                    result.iAccession = aFASTAHeader.substring(0, firstPipeLoc).trim();
                    result.iID = "";
                    int secondPipeLoc = aFASTAHeader.indexOf("|", firstPipeLoc+1);
                    int thirdPipeLoc = aFASTAHeader.indexOf("|", secondPipeLoc+1);
                    result.iDescription = aFASTAHeader.substring(secondPipeLoc+1, thirdPipeLoc).trim();
                    result.iID = "";
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                } else if(aFASTAHeader.matches("^nrAt[^\t]*\t.*")) {
                    // The PSB Arabidopsis thaliana database; proprietary format
                    // We need to find three elements:
                    //   - the internal accession (at the start, separated by 'tab' and space from the next part).
                    //   - the external accession (between '()', after the internal accession).
                    //   - the description (retrieved as the rest of the header).
                    //
                    // ex: nrAt0.2_1 	(TR:Q8HT11_ARATH) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).
                    int openBracketLoc = aFASTAHeader.indexOf("(");
                    int closeBracketLoc = aFASTAHeader.indexOf(")");
                    // If there is a location, there will be a closing bracket at 'closeBracketLoc+1' as well.
                    // If so, use this one.
                    int tempLoc = closeBracketLoc+1;
                    if(aFASTAHeader.length() > tempLoc && aFASTAHeader.charAt(tempLoc) == ')') {
                        closeBracketLoc = tempLoc;
                    }
                    result.iAccession = aFASTAHeader.substring(openBracketLoc+1, closeBracketLoc).trim();
                    result.iID = aFASTAHeader.substring(0, openBracketLoc).trim();
                    result.iDescription = aFASTAHeader.substring(closeBracketLoc+1).trim();
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                } else if(aFASTAHeader.matches("^L. monocytogenes[^|]*[|][^|]*[|].*")) {
                    // The Listeria database; proprietary format
                    // We need to find three elements:
                    //   - the leader element (at the start, separated by '|' from the next part).
                    //   - the accession number (between '||', after the leader).
                    //   - the description (retrieved as the rest of the header).
                    //
                    // ex: L. monocytogenes EGD-e|LMO02333|'comK: 158 aa - competence transcription factor (C-terminal part)
                    int firstPipe = aFASTAHeader.indexOf("|");
                    int secondPipe = aFASTAHeader.indexOf("|", firstPipe+1);
                    result.iID = aFASTAHeader.substring(0, firstPipe).trim();
                    result.iAccession = aFASTAHeader.substring(firstPipe+1, secondPipe).trim();
                    result.iDescription = aFASTAHeader.substring(secondPipe+1).trim();
                    // Check for the presence of a location.
                    int index = -1;
                    if((index = result.iAccession.indexOf(" (")) > 0) {
                        String temp = result.iAccession.substring(index);
                        result.iAccession = result.iAccession.substring(0, index);
                        int open = 2;
                        int minus = temp.indexOf("-");
                        int end = temp.indexOf(")");
                        result.iStart = Integer.parseInt(temp.substring(open, minus));
                        result.iEnd = Integer.parseInt(temp.substring(minus+1, end));
                    }
                } else {
                    // Okay, try the often-used 'generic' approach. If this fails, we go to the worse-case scenario, ie. do not process at all.
                    // Testing for this is somewhat more complicated.

                    // Often used simple header; looks like:
                    // >NP0465 (NP0465) A description for this protein.
                    // We need to find two elements:
                    //   - the accession String (easily retrieved as the next String until a space is encountered).
                    //   - the description
                    int accessionEndLoc = aFASTAHeader.indexOf(" ");
                    // Temporary storage variables.
                    int startSecAcc = -1;
                    int endSecAcc = -1;
                    String testAccession = null;
                    String testDescription = null;
                    int testStart = -1;
                    int testEnd = -1;
                    if((accessionEndLoc > 0) && (aFASTAHeader.indexOf("(") >= 0) && (aFASTAHeader.indexOf(")", aFASTAHeader.indexOf("(")+1) >= 0)) {
                        // Now we have to see if there is location information present.
                        if(aFASTAHeader.substring(accessionEndLoc+1, aFASTAHeader.indexOf(")", accessionEndLoc+2)+1).matches("[(][0-9]+-[0-9]+[)]") && !aFASTAHeader.substring(accessionEndLoc+2, aFASTAHeader.indexOf(")", accessionEndLoc+2)).equals(aFASTAHeader.substring(0, accessionEndLoc).trim())) {
                            // start and end found. Add it to the accession number and remove it from the description.
                            accessionEndLoc = aFASTAHeader.indexOf(")", accessionEndLoc) + 1;
                        }
                        testAccession = aFASTAHeader.substring(0, accessionEndLoc).trim();
                        // Check for the presence of a location.
                        int index = -1;
                        if((index = testAccession.indexOf(" (")) > 0) {
                            String temp = testAccession.substring(index);
                            testAccession = testAccession.substring(0, index);
                            int open = 2;
                            int minus = temp.indexOf("-");
                            int end = temp.indexOf(")");
                            testStart = Integer.parseInt(temp.substring(open, minus));
                            testEnd = Integer.parseInt(temp.substring(minus+1, end));
                        }
                        testDescription = aFASTAHeader.substring(accessionEndLoc).trim();
                        // Find the second occurrence of the accession number, which should be in the description.
                        int enzymicity = -1;
                        if(testDescription.indexOf("(*") >= 0 && testDescription.indexOf("*)", testDescription.indexOf("(*"+4)) > 0) {
                            enzymicity = testDescription.indexOf("*)") + 2;
                        }
                        startSecAcc = testDescription.indexOf("(", enzymicity);
                        endSecAcc = testDescription.indexOf(")", startSecAcc+2);
                    }
                    // See if the accessions match up.
                    if(startSecAcc >= 0 && endSecAcc >= 0 && testDescription.substring(startSecAcc+1, endSecAcc).trim().equals(testAccession.trim())) {
                        result.iID = "";
                        result.iAccession = testAccession;
                        result.iDescription = testDescription;
                        if(testStart >= 0 && testEnd >= 0) {
                            result.iStart = testStart;
                            result.iEnd = testEnd;
                        }
                    } else {
                        // Unknown.
                        // Everything is rest.
                        result.iRest = aFASTAHeader;
                        // Check for the presence of a location.
                        int index = -1;
                        if( ((index = result.iRest.lastIndexOf(" (")) > 0) && (result.iRest.lastIndexOf(")")>0) && (result.iRest.lastIndexOf("-") > index)) {
                            String temp = result.iRest.substring(index);
                            int open = 2;
                            int minus = temp.indexOf("-");
                            int end = temp.lastIndexOf(")");
                            try {
                                int tempStart = Integer.parseInt(temp.substring(open, minus));
                                int tempEnd = Integer.parseInt(temp.substring(minus+1, end));
                                result.iStart = tempStart;
                                result.iEnd = tempEnd;
                                result.iRest = result.iRest.substring(0, index);
                            } catch(Exception e) {
                                // apparently not location info.
                            }
                        }
                    }
                }
            } catch(RuntimeException excep) {
                logger.error(" * Unable to process FASTA header line:\n\t" + aFASTAHeader + "\n\n");
                throw excep;
            }
        }

        return result;
    }

    public String getID() {
        return this.iID;
    }

    public void setID(String aID) {
        iID = aID;
    }

    public String getForeignID() {
        return iForeignID;
    }

    public void setForeignID(String aForeignID) {
        iForeignID = aForeignID;
    }

    public String getAccession() {
        return iAccession;
    }

    public void setAccession(String aAccession) {
        iAccession = aAccession;
    }

    public String getForeignAccession() {
        return iForeignAccession;
    }

    public void setForeignAccession(String aForeignAccession) {
        iForeignAccession = aForeignAccession;
    }

    public String getDescription() {
        return iDescription;
    }

    public void setDescription(String aDescription) {
        iDescription = aDescription;
    }

    public String getForeignDescription() {
        return iForeignDescription;
    }

    public void setForeignDescription(String aForeignDescription) {
        iForeignDescription = aForeignDescription;
    }

    public String getRest() {
        return iRest;
    }

    public void setRest(String aRest) {
        iRest = aRest;
    }

    /**
     * This method returns an abbreviated version of the Header,
     * suitable for inclusion in FASTA formatted files. <br />
     * The abbreviated header is composed in the following way: <br />
     * <pre>
     *     >[ID]|[accession_string]|([foreign_ID]|[foreign_accession_string]|[foreign_description] )[description]
     * </pre>
     *
     * @return  String  with the abbreviated header.
     */
    public String getAbbreviatedFASTAHeader() {
        StringBuffer result = new StringBuffer(">" + this.getCoreHeader());
        if(this.iID == null) {
            // Apparently we have not been able to identify and parse
            // this header.
            // In that case, the core header already contains everything,
            // so don't do anything.
        } else {
            // Some more appending to be done here.
            if(!this.iID.equals("")) {
                if(this.iID.equalsIgnoreCase("sw") || this.iID.equalsIgnoreCase("IPI") || this.iID.toLowerCase().startsWith("l. monocytogenes")) {
                    // FASTA entry with pipe ('|') separating core header from description.
                    result.append("|"+this.iDescription);
                } else if(this.iID.equalsIgnoreCase("gi")) {
                    // NCBI entry.
                    result.append("|");
                    // See if we have a foreign ID.
                    if(iForeignID != null) {
                        result.append(this.iForeignID+"|"+this.iForeignAccession+"|");
                        // See if we also have a description.
                        if(this.iForeignDescription != null) {
                            result.append(this.iForeignDescription);
                        }
                    }
                    // Add the Description.
                    result.append(" " + this.iDescription);
                } else if(this.iID.startsWith(" M. tub.")) {
                    // Mycobacterium tuberculosis entry.
                    result.append("|" + this.iDescription);
                } else if(this.iID.startsWith("dm")) {
                    // Personal Drosphila melanogaster entry.
                    result = result.delete(result.indexOf("|"), result.length());
                    result.append(" [" + this.iStart + "-" + this.iEnd + "]");
                } else if(this.iID.startsWith("nrAt")) {
                    // Proprietary PSB A. thaliana entry
                    result.append(" " + this.iDescription);
                }

            } else if(this.iID.equals("")) {
                if(iAccession.startsWith("HIT")) {
                    result.append("|" + this.iDescription);
                } else {
                    // Just add a space and the description.
                    result.append(" " + this.iDescription);
                }
            }
        }
        return result.toString();
    }

    /**
     * This method reports on the entire header.
     *
     * @return  String  with the full header.
     */
    public String toString() {
        String result = null;
        if(this.iID == null) {
            result = this.getAbbreviatedFASTAHeader();
        } else {
            result = this.getAbbreviatedFASTAHeader();
            if(this.iRest != null) {
                result += " " + this.iRest;
            }
        }
        return result;
    }

    /**
     * This method will attribute a score to the current header, based on the
     * following scoring list:
     *   <ul>
     *      <li> SwissProt : 4 </li>
     *      <li> IPI, SwissProt reference : 3 </li>
     *      <li> IPI, TrEMBL or REFSEQ_NP reference : 2 </li>
     *      <li> IPI, without SwissProt, TrEMBL or REFSEQ_NP reference : 1 </li>
     *      <li> NCBI, SwissProt reference : 2</li>
     *      <li> NCBI, other reference : 1</li>
     *      <li> Unknown header format : 0</li>
     *   </ul>
     *
     * @return  int with the header score. The higher the score, the more interesting
     *              a Header is.
     */
    public int getScore() {
        int score = -1;

        // Score the header...
        if(this.iID == null || this.iID.equals("") || this.iID.startsWith(" M. tub.") || this.iID.startsWith("nrAt") || this.iID.startsWith("L. monocytogenes")) {
            score = 0;
        } else if(this.iID.equalsIgnoreCase("sw")) {
            score = 4;
        } else if(this.iID.equalsIgnoreCase("ipi")) {
            if(this.iDescription != null && this.iDescription.toUpperCase().indexOf("SWISS-PROT") >= 0) {
                score = 3;
            } else if(this.iDescription != null && ( (this.iDescription.toUpperCase().indexOf("TREMBL") >= 0) || (this.iDescription.toUpperCase().indexOf("REFSEQ_NP") >= 0) )) {
                score = 2;
            } else {
                score = 1;
            }
        } else if(this.iID.equalsIgnoreCase("gi")) {
            if(this.iForeignID != null && this.iForeignID.equals("sp")) {
                score = 2;
            } else {
                score = 1;
            }
        }
        return score;
    }

    /**
     * This method reports on the core information for the header, which is comprised
     * of the ID and the accession String:
     *   <pre>
     *     [ID]|[accession_string]
     *   </pre>
     * This is mostly useful for appending this core as an addendum to another header.
     *
     * @return  String  with the header core data ([ID]|[accession_string]).
     */
    public String getCoreHeader() {
        String result = null;
        if(iID != null && iID.startsWith("nrAt")) {
            result = this.getID() + " \t(" + this.getAccession();
        } else if(iID != null && !iID.equals("")) {
            result = this.getID() + "|" + this.getAccession();
        } else if(iID != null && iID.equals("")) {
            // No ID given, so just take the accession.
            result = this.getAccession();
        } else if(iID == null) {
            result = this.iRest;
        }

        // See if we need to add information about the location.
        if(iStart >= 0) {
            result += " (" + Integer.toString(iStart) + "-" + Integer.toString(iEnd) + ")";
        }

        // For the PSB A. Thaliana, we need to include the closing ')'.
        if(iID != null && iID.startsWith("nrAt")) {
            result += ")";
        }

        return result;
    }

    /**
     * This method allows the addition of an addendum to the list.
     * If the addendum is already preceded with '^A', it is added
     * as is, otherwise '^A' is prepended before addition to the list.
     *
     * @param   aAddendum   String with the addendum, facultatively preceded by '^A'.
     */
    public void addAddendum(String aAddendum) {
        // First see if we have addenda already.
        if(this.iAddenda == null) {
            iAddenda = new StringBuffer();
        }

        // Now check for the presence of the '^A' sequence.
        if(aAddendum.startsWith("^A")) {
            iAddenda.append(aAddendum);
        } else {
            iAddenda.append("^A"+aAddendum);
        }
    }

    /**
     * This method allows the caller to retrieve all addenda for the current header,
     * or 'null' if there aren't any.
     *
     * @return  String  with the addenda, or 'null' if there aren't any.
     */
    public String getAddenda() {
        String result = null;
        if(this.iAddenda != null) {
            result = iAddenda.toString();
        }
        return result;
    }

    /**
     * This method reports on the presence of addenda for this header.
     *
     * @return  boolean whether addenda are present.
     */
    public boolean hasAddenda() {
        boolean result = false;

        if(this.iAddenda != null) {
            result = true;
        }

        return result;
    }

    /**
     * This method reports on the full header, with the addenda (if present).
     * If no addenda are present, this method reports the same information as
     * the 'toString()' method.
     *
     * @return  String  with the header and addenda (if any).
     */
    public String getFullHeaderWithAddenda() {
        String result = this.toString();

        if(this.iAddenda != null) {
            result += iAddenda.toString();
        }

        return result;
    }

    /**
     * This method returns an abbreviated version of the Header,
     * suitable for inclusion in FASTA formatted files. <br />
     * The abbreviated header is composed in the following way: <br />
     * <pre>
     *     >[ID]|[accession_string]|([foreign_ID]|[foreign_accession_string]|[foreign_description] )[description]([addenda])
     * </pre>
     * Note that the output of this method is identical to that of the getAbbreviatedFASTAHeader()
     * if no addenda are present.
     *
     * @return  String  with the abbreviated header and addenda (if any).
     */
    public String getAbbreviatedFASTAHeaderWithAddenda() {
        String result = this.getAbbreviatedFASTAHeader();

        if(this.iAddenda != null) {
            result += iAddenda.toString();
        }

        return result;
    }

    /**
     * This method allows the caller to add information to the header about
     * location of the sequence in a certain master sequence. <br/>
     * This information is typically specified right after the accession number:
     *   <pre>
     *     [id]|[accession_string] ([startindex]-[endindex])|...
     *   </pre>
     * <b>Please note the following:</b>
     *   <ul>
     *     <li>If an index is already present, it is removed and replaced.</li>
     *     <li>If the header is of unknown format, the indeces are appended to the end of the header.</li>
     *   </ul>
     *
     * @param   aStart  int with the startindex.
     * @param   aEnd    int with the endindex.
     */
    public void setLocation(int aStart, int aEnd) {
        this.iStart = aStart;
        this.iEnd = aEnd;
    }

    /**
     * This method reports on the start index of the header.
     * It returns '-1' if no location is specified.
     *
     * @return  int with the start location, or '-1' if none
     *              was defined.
     */
    public int getStartLocation() {
        return iStart;
    }

    /**
     * This method reports on the end index of the header.
     * It returns '-1' if no location is specified.
     *
     * @return  int with the end location, or '-1' if none
     *              was defined.
     */
    public int getEndLocation() {
        return iEnd;
    }

    /**
     * This method provides a deep copy of the Header instance.
     *
     * @return  Object  Header that is a deep copy of this Header.
     */
    public Object clone() {
        Object result = null;
        try {
            result = super.clone();
        } catch(CloneNotSupportedException cnse) {
            logger.error(cnse.getMessage(), cnse);
        }
        return result;
    }
}
