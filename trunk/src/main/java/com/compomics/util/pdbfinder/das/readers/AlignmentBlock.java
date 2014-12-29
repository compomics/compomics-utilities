package com.compomics.util.pdbfinder.das.readers;

/**
 * AlignmentBlock.
 * 
 * @author Niklaas Colaert
 */
public class AlignmentBlock {
    
    /**
     * The PDB start.
     */
    private int iPdbStart;
    /**
     * The PDB end.
     */
    private int iPdbEnd;
    /**
     * The SP start.
     */
    private int iSpStart;
    /**
     * The SP end.
     */
    private int iSpEnd;
    /**
     * The PDB accession.
     */
    private String iPdbAccession;
    /**
     * The SP accession.
     */
    private String iSpAccession;

    /**
     * Constructor.
     * 
     * @param aPdbStart the PDB start index
     * @param aPdbEnd the PDB end index
     * @param aSpStart the UniProt start index
     * @param aSpEnd the UniProt end index
     * @param aPdbAccession the PDB accession
     * @param aSpAccession the UniProt accession
     */
    public AlignmentBlock(int aPdbStart, int aPdbEnd, int aSpStart, int aSpEnd, String aPdbAccession, String aSpAccession) {
        this.iPdbStart = aPdbStart;
        this.iPdbEnd = aPdbEnd;
        this.iSpStart = aSpStart;
        this.iSpEnd = aSpEnd;
        this.iPdbAccession = aPdbAccession;
        this.iSpAccession = aSpAccession;
    }

    /**
     * Returns the PDB start.
     * 
     * @return the PDB start
     */
    public int getPdbStart() {
        return this.iPdbStart;
    }

    /**
     * Returns the PDB end.
     * 
     * @return the PDB end
     */
    public int getPdbEnd() {
        return this.iPdbEnd;
    }

    /**
     * Returns the SP start.
     * 
     * @return the SP start
     */
    public int getSpStart() {
        return this.iSpStart;
    }

    /**
     * Returns the SP end.
     * 
     * @return the SP end
     */
    public int getSpEnd() {
        return this.iSpEnd;
    }

    /**
     * Returns the PDB accession.
     * 
     * @return the PDB accession
     */
    public String getPdbAccession() {
        return this.iPdbAccession;
    }

    /**
     * Returns the SP accession.
     * 
     * @return the SP accession
     */
    public String getSpAccession() {
        return this.iSpAccession;
    }

    /**
     * Returns the difference.
     * 
     * @return the difference
     */
    public int getDifference() {
        int diff = iSpStart - iPdbStart;
        return diff;
    }
}
