/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 26-sep-02
 * Time: 22:10:45
 */
package com.compomics.util.io;
import org.apache.log4j.Logger;

import java.util.Vector;
import java.util.StringTokenizer;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements a PushBackReader for Strings which allows the reading
 * of entire lines as well as the pushing back of entire lines. <br>
 * Little more functionality is present at this time.
 *
 * @author Lennart Martens
 */
public class PushBackStringReader {

    // Class specific log4j logger for PushBackStringReader instances.
    Logger logger = Logger.getLogger(PushBackStringReader.class);

    /**
     * This Vector caches the entire String in memory, separating the elements through
     * endlines as delimiters.
     */
    private Vector iData = null;

    /**
     * Current index into the Vector.
     */
    private int iLocation = 0;

    /**
     * EOF marker.
     */
    private boolean iEOF = false;

    /**
     * This constructor requires the passing of a String from which to
     * construct the PushBackStringReader.
     *
     * @param   aSource String to construct the PushBackStringReader from.
     */
    public PushBackStringReader(String aSource) {
        StringTokenizer st = new StringTokenizer(aSource, "\n");
        iData = new Vector(st.countTokens());
        while(st.hasMoreTokens()) {
            iData.add(st.nextToken());
        }
        iLocation = 0;
    }

    /**
     * This method reads a line of text from the buffer.
     * It returns a 'null' if the EOF is encountered.
     *
     * @return  String  with the current line in the buffer, or 'null' if the end
     *                  of the buffer has been reached.
     */
    public String readLine() {
        String result = null;
        if(!iEOF) {
            int returnIndex = iLocation;
            iLocation++;
            if(iLocation == iData.size()) {
                iEOF = true;
            } else if(iLocation > iData.size()) {
                throw new ArrayIndexOutOfBoundsException("Vector elements count up to element " + (iData.size()-1) + " and current index is " + iLocation + ".");
            }
            result = (String)iData.get(returnIndex);
        }
        return result;
    }

    /**
     * This method 'unreads' a line of text from the Buffer.
     */
    public void unreadLine() {
        iLocation--;
        iEOF = false;
    }
}
