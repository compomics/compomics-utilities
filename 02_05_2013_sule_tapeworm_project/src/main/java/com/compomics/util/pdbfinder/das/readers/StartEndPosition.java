package com.compomics.util.pdbfinder.das.readers;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas Colaert
 * Date: 20-mrt-2008
 * Time: 8:30:45
 */
public class StartEndPosition {
    
    // @TODO: add JavaDoc...

    private int iStart;
    private int iEnd;

    public StartEndPosition(int aStart, int aEnd) {
        this.iStart = aStart;
        this.iEnd = aEnd;
    }

    public int getStartPosition() {
        return this.iStart;
    }

    public int getEndPosition() {
        return this.iEnd;
    }
}
