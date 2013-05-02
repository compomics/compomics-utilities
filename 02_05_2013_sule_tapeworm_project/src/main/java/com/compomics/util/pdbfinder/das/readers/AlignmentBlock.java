package com.compomics.util.pdbfinder.das.readers;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 25-jan-2008
 * Time: 9:18:16
 */
public class AlignmentBlock {
    
    // @TODO: add JavaDoc...

    private int iPdbStart;
    private int iPdbEnd;
    private int iSpStart;
    private int iSpEnd;
    private String iPdbAccession;
    private String iSpAccession;

    public AlignmentBlock(int aPdbStart, int aPdbEnd, int aSpStart, int aSpEnd, String aPdbAccession, String aSpAccession) {
        this.iPdbStart = aPdbStart;
        this.iPdbEnd = aPdbEnd;
        this.iSpStart = aSpStart;
        this.iSpEnd = aSpEnd;
        this.iPdbAccession = aPdbAccession;
        this.iSpAccession = aSpAccession;
    }

    //getters
    public int getPdbStart() {
        return this.iPdbStart;
    }

    public int getPdbEnd() {
        return this.iPdbEnd;
    }

    public int getSpStart() {
        return this.iSpStart;
    }

    public int getSpEnd() {
        return this.iSpEnd;
    }

    public String getPdbAccession() {
        return this.iPdbAccession;
    }

    public String getSpAccession() {
        return this.iSpAccession;
    }

    public int getDifference() {
        int diff = iSpStart - iPdbStart;
        return diff;
    }
}
