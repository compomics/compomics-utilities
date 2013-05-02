/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 6-jan-03
 * Time: 9:48:16
 */
package com.compomics.util.nucleotide;
import org.apache.log4j.Logger;

import com.compomics.util.interfaces.Sequence;
import com.compomics.util.general.MassCalc;
import com.compomics.util.general.UnknownElementMassException;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Header;

import java.util.Properties;
import java.util.Vector;
import java.util.HashMap;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.6 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class represents a nucleotide sequence (RNA or DNA).
 *
 * @author Lennart Martens
 */
public class NucleotideSequenceImpl implements Sequence {

    // Class specific log4j logger for NucleotideSequenceImpl instances.
    Logger logger = Logger.getLogger(NucleotideSequenceImpl.class);

    /**
     * The sequence.
     */
    private String iSequence = null;

    /**
     * The mass. It uses lazy caching.
     */
    private double iMass = -1.0;

    /**
     * The Properties object with the conversion from DNA to
     * proteins.
     */
    private static Properties iTranslate = null;

    /**
     * HashMap that defines the complementary nucleotides.
     */
    private static Properties iComplement = null;


    /**
     * This constructor allows the construction of an Object,
     * wrapping a nucleotidesequence. The default codon usage table
     * for translation will be used when translating.
     *
     * @param   aSequence   String with the nucleotide sequence.
     */
    public NucleotideSequenceImpl(String aSequence) {
        this.setSequence(aSequence);
    }

    /**
     * This constructor allows the construction of an Object,
     * wrapping a nucleotidesequence. Translation will be done using
     * the specified codon usage table.
     *
     * @param   aSequence   String with the nucleotide sequence.
     * @param   aCodonUsageTable    Properties instance with the codon usage table
     *                              (triplet is key, single-letter amino acid is value).
     */
    public NucleotideSequenceImpl(String aSequence, Properties aCodonUsageTable) {
        iTranslate = aCodonUsageTable;
        this.setSequence(aSequence);
    }

    /**
     * This constructor allows the construction of an Object,
     * wrapping a nucleotidesequence. Translation will be done using
     * the specified codon usage table.
     *
     * @param   aSequence   String with the nucleotide sequence.
     * @param   aCodonUsageTable    Properties instance with the codon usage table
     *                              (triplet is key, single-letter amino acid is value).
     */
    public NucleotideSequenceImpl(String aSequence, String aCodonUsageTable) {
        if(aCodonUsageTable != null) {
            iTranslate = loadProps(aCodonUsageTable);
            if(iTranslate.size() == 0) {
                throw new IllegalArgumentException("Unable to read or parse your codon usage table!");
            }
        }
        this.setSequence(aSequence);
    }

    /**
     * This method will set the sequence. <br />
     * Note that most implementations will also allow you
     * to set this via the constructor.
     *
     * @param	aSequence	String with the sequence.
     */
    public void setSequence(String aSequence) {
        this.iSequence = aSequence.trim().toUpperCase();
        this.iMass = -1.0;
    }

    /**
     * This method reports on the length of the current sequence.
     *
     * @return  int with the length of the sequence.
     */
    public int getLength() {
        return iSequence.length();
    }

    /**
     * This method will return the mass for the sequence.
     *
     * @return	double	with the mass.
     */
    public double getMass() {

        // Lazy caching of the mass.
        if(iMass < 0.0) {
            try {
                this.iMass = new MassCalc(MassCalc.MONONUCLEOTIDES).calculateMass(iSequence);
            } catch(UnknownElementMassException ueme) {
                logger.error(ueme.getMessage(), ueme);
            }
        }

        return iMass;
    }

    /**
     * This method will retrieve the sequence.
     *
     * @return	String	with the sequence.
     */
    public String getSequence() {
        return iSequence;
    }

    /**
     * This method translates the specified nucleotidesequence into
     * the six reading frames.
     * If an unkown nucleic acid 'N' is part of the codon, amino
     * acid 'X' will be inserted in the string.
     *
     * @return  AASequenceImpl[]    with the maximum of 6 translated protein sequences.
     */
    public AASequenceImpl[] translate() {
        Vector seqs = new Vector(6);

        // We'll translate all based on the translation map.
        // This map is lazily cached at class level.
        if(iTranslate == null) {
            iTranslate = this.loadProps("DNA_Protein_Translation.properties");
        }

        // We need to do sense first, and subsequently the complement.
        for(int i=0;i<3;i++) {
            String tempSeq = this.translate(iSequence, i);
            if((tempSeq != null) && (!tempSeq.trim().equals(""))) {
                seqs.add(new AASequenceImpl(tempSeq));
            }
        }

        // Now the reverse complement.
        String inverse = this.getReverseComplementary();
        for(int i=0;i<3;i++) {
            String tempSeq = this.translate(inverse, i);
            if((tempSeq != null) && (!tempSeq.trim().equals(""))) {
                seqs.add(new AASequenceImpl(tempSeq));
            }
        }

        AASequenceImpl[] result = new AASequenceImpl[seqs.size()];
        seqs.toArray(result);

        return result;
    }

    /**
     * This method translates the specified nucleotidesequence into
     * the six reading frames. While doing so, distinct entries are
     * generated when stop codons are encountered in the nucleotide sequence.
     * If an unkown nucleic acid 'N' is part of the codon, amino
     * acid 'X' will be inserted in the string.
     *
     *
     * @param aDatabaseIdentifier String to include the database origin in the protein entry annotation.
     * @param aShortOrganism String to include the organism origin in the protein entry annotation.
     * @return  Vector    with the maximum of 6 translated reading frames.
     *                    Each Vector element is a HashMap with all the proteins from one reading frame.
     *                    HashMap structure : Key - Header Instance ; Value - Protein instance of the translation.
     */
    public Vector translateToStopCodonSeparatedEntries(String aDatabaseIdentifier, String aShortOrganism) {
        Vector allSeqs = new Vector(6);

        // We'll translate all based on the translation map.
        // This map is lazily cached at class level.
        if(iTranslate == null) {
            iTranslate = this.loadProps("DNA_Protein_Translation.properties");
        }

        // We need to do sense first, and subsequently the complement.
        for(int i=0;i<3;i++) {
            String lEntryIdentifier = aDatabaseIdentifier + "-sense-" + (i+1);
            HashMap lHashSeq = this.translateEntriesSeparatedByStopCodon(iSequence, i, lEntryIdentifier, aShortOrganism);
            if(!lHashSeq.isEmpty()) {
                allSeqs.add(lHashSeq);
            }
        }

        // Now the reverse complement.
        String inverse = this.getReverseComplementary();
        for(int i=0;i<3;i++) {
            String lEntryIdentifier = aDatabaseIdentifier + "-antisense-" + (i+1);
            HashMap lHashSeq = this.translateEntriesSeparatedByStopCodon(inverse, i, lEntryIdentifier, aShortOrganism);
            if(!lHashSeq.isEmpty()) {
                allSeqs.add(lHashSeq);
            }
        }

        return allSeqs;
    }

    /**
     * This method returns the reverse complementary strand for the
     * sequence.
     *
     * @return  String  with the reverse complementary sequence.
     */
    public String getReverseComplementary() {
        // Lazy cache of complementary residues.
        if(iComplement == null) {
            iComplement = this.loadProps("complementaryNucleotides.properties");
        }

        StringBuffer complement = new StringBuffer();

        for(int i=iSequence.length();i>0;i--) {
            String key = iSequence.substring(i-1, i);
            complement.append(iComplement.get(key));
        }

        return complement.toString();
    }

    /**
     * This method will translate the specified DNA sequence into a
     * single String of amino acids, starting from the specified
     * base.
     * If an unkown nucleic acid 'N' is part of the codon, amino
     * acid 'X' will be inserted in the string.
     *
     * @param   aSequence   String with the sequence to be translated.
     * @param   aStartFrame int with the index of the element that we
     *                      will start from (it is modulo'd by 3!!)
     * @return  String  with the translated sequence.
     */
    private String translate(String aSequence, int aStartFrame) {
        StringBuffer aminoacidsequence = new StringBuffer();
        aStartFrame = aStartFrame%3;

        for(int i=aStartFrame;i<aSequence.length()-2;i+=3) {
            String key = aSequence.substring(i, i+3);
            // If An Unknown Nucleic Acid 'N' is part of the Codon -> append amino acid 'X'.
            if(key.indexOf('N') >= 0){
                aminoacidsequence.append("X");
            }else{
            // Otherwise translate the codon to the correct amino acid by the Codon Usage Table.    
                aminoacidsequence.append((String)iTranslate.get(key));
            }
        }
        return aminoacidsequence.toString();
    }

    /**
     * This method will translate the specified DNA sequence
     * starting from the specified base into a HashMap with
     * a Header instance as a keys and an amino acid sequence as values.
     * A new entry is created each time we hit a STOP-CODON
     * in the nucleic acid sequence.
     *
     * If an unkown nucleic acid 'N' is part of the codon, amino
     * acid 'X' will be inserted in the string.
     *
     * @param   aSequence   String with the sequence to be translated.
     * @param   aStartFrame int with the index of the element that we
     *                      will start from (it is modulo'd by 3!!)
     * @param   aEntryIdentifier String that will be used in the Protein accession.
     * @param   aShortOrganism String to include the organism origin in the protein entry annotation.
     * @return  HashMap Key - Header Instance ; HashMap Value - Protein instance of the translation.
     *
     *          <br /><strong>NOTE!</strong> Each Header instance contains the originale nucleic acid sequence that was being translated in the iRest field.
     */
    private HashMap translateEntriesSeparatedByStopCodon(String aSequence, int aStartFrame, String aEntryIdentifier, String aShortOrganism) {
        HashMap seqs = new HashMap();
        int counter = 0;

        StringBuffer aminoacidSequence = new StringBuffer();
        aStartFrame = aStartFrame%3;
        String key = null;
        try {
            for(int i=aStartFrame;i<aSequence.length()-2;i+=3) {
                key = aSequence.substring(i, i+3);
                // 1. If An Unknown Nucleic Acid 'N' is part of the Codon -> append amino acid 'X'.
                if(key.indexOf('N') >= 0){
                    aminoacidSequence.append("X");
                // 2. If a STOP-CODON ("_") is encountered
                //    OR
                //    If this is the last loop, we have to add a new entry in the sequence hashmap.
                }else if((iTranslate.get(key).equals("_")) || ((i+3) >= aSequence.length()-2)){
                    // Don't forget to attach the last codon!
                    if((key.length() == 3) && (!iTranslate.get(key).equals("_"))){
                        aminoacidSequence.append((String)iTranslate.get(key));
                    }
                    String sequence = aminoacidSequence.toString();
                    // If two stop codons are next to eachother, we have an empty protein sequence. Don't do anything then!
                    if (!sequence.equals("")) {
                        counter = counter + 1;
                        int start = i - ((sequence.length())*3);
                        int stop = i - 1;
                        // Include the last entry if no stop codon.
                        if(!iTranslate.get(key).equals("_")){
                            stop = stop + 3;
                            start = start + 3;
                        }
                        String nucleicSequence = aSequence.substring(start, (stop+1));
                        // Protein accession is formed here.
                        String lEntryIdentifier = aShortOrganism + counter + "_" + aEntryIdentifier;
                        Header lEntryHeader = Header.parseFromFASTA(lEntryIdentifier + " [" + (start+1) + "-" + (stop+1) + "]" );
                        // Set the protein accession.
                        lEntryHeader.setAccession(lEntryHeader.getAccession());
                        // Set the nucleic sequence.
                        lEntryHeader.setRest(nucleicSequence);

                        seqs.put(lEntryHeader, aminoacidSequence.toString());
                        // "reset" the StringBuffer && continue the loop.
                        aminoacidSequence = new StringBuffer();
                    }
                }else{
                // 3. Otherwise translate the codon into the correct amino acid by the Codon Usage Table.
                aminoacidSequence.append((String)iTranslate.get(key));
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return seqs;
    }

    /**
    * This method loads a Properties instance from the classpath.
    * It returns an empty instance and displays an error message
    * if the Properties instance was not found.
    *
    * @param	aPropFileName	String with the filename for the
    *							properties file.
    * @return	Properties	with the props from the file, or an empty
    *						instance if the file was not found.
    */
    private Properties loadProps(String aPropFileName) {
        Properties p = new Properties();
        try {
            p.load(this.getClass().getClassLoader().getResourceAsStream(aPropFileName));
        } catch(IOException ioe) {
            logger.error("\nProperties file ("+aPropFileName+") not found in classpath!");
            logger.error("All resultant values will be computed to 0.0!!\n");
        }
        return p;
    }
}
