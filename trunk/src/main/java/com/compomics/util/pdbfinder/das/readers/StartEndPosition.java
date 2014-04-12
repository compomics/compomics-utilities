package com.compomics.util.pdbfinder.das.readers;

/**
 * StartEndPosition.
 *
 * @author Niklaas Colaert
 */
public class StartEndPosition {

    /**
     * The start.
     */
    private int iStart;
    /**
     * The end.
     */
    private int iEnd;

    /**
     * Constructor.
     * 
     * @param aStart
     * @param aEnd 
     */
    public StartEndPosition(int aStart, int aEnd) {
        this.iStart = aStart;
        this.iEnd = aEnd;
    }

    /**
     * Returns the start position.
     * 
     * @return the start position
     */
    public int getStartPosition() {
        return this.iStart;
    }

    /**
     * Returns the end position.
     * 
     * @return the end position
     */
    public int getEndPosition() {
        return this.iEnd;
    }
}
