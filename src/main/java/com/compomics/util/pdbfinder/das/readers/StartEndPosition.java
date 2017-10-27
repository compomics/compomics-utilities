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
    private final int iStart;
    /**
     * The end.
     */
    private final int iEnd;

    /**
     * Constructor.
     * 
     * @param aStart the start position
     * @param aEnd the end position
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
