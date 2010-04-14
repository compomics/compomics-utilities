/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 31-jan-03
 * Time: 22:34:59
 */
package com.compomics.util.io;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements an InputStream that reports on the progress of reading
 * a file in integer quantities, even though the underlying file may have a binary size that is outside
 * of integer range.
 *
 * @author Lennart Martens
 */
public class MonitorableFileInputStream extends MonitorableInputStream {
	// Class specific log4j logger for MonitorableFileInputStream instances.
	Logger logger = Logger.getLogger(MonitorableFileInputStream.class);

    /**
     * The maximum readable size.
     */
    private long iMaximum = 0;

    /**
     * The current amount of bytes read from the file.
     */
    private long iCurrent = 0;

    /**
     * The number of actual bytes will be divided by 1024 to the 'iFactor' power
     * to yield an integer scale.
     */
    private int iFactor = 0;


    /**
     * This constructor takes a file to load the FileInputStream from.
     *
     * @param   aFile   File to connect the inputstream to.
     * @exception   IOException when the file could not be raed.
     */
    public MonitorableFileInputStream(File aFile) throws IOException {
        super(new FileInputStream(aFile));
        long length = aFile.length();
        int factor = 0;
        long max = Integer.MAX_VALUE;
        while(length >= max) {
            length /= 1024;
            factor++;
        }
        this.iMaximum = aFile.length();
        this.iFactor = factor;
    }

    /**
     * This constructor takes a file to load the FileInputStream from.
     *
     * @param   aFile   String with the filename for the file to connect
     *                  the inputstream to.
     * @exception   IOException when the file could not be raed.
     */
    public MonitorableFileInputStream(String aFile) throws IOException {
        this(new File(aFile));

    }

    /**
     * This method reports on the maximum scale for the monitor.
     *
     * @return  int with the maximum for the monitor.
     */
    public int getMaximum() {
        return (int)(this.iMaximum/Math.pow(1024, iFactor));
    }

    /**
     * This method returns the progress (as  measured by taking the value
     * of iMaximum, minus the current progress as reported by the cache).
     *
     * @return  int with the progress.
     */
    public int monitorProgress() {
        int result = (int)((this.iMaximum - (this.iMaximum-this.iCurrent))/Math.pow(1024, iFactor));
        return result;
    }

    /**
     * Reads up to <code>byte.length</code> bytes of data from this
     * input stream into an array of bytes. This method blocks until some
     * input is available.
     * <p>
     * This method simply performs the call
     * <code>read(b, 0, b.length)</code> and returns
     * the  result. It is important that it does
     * <i>not</i> do <code>in.read(b)</code> instead;
     * certain subclasses of  <code>FilterInputStream</code>
     * depend on the implementation strategy actually
     * used.
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#read(byte[], int, int)
     */
    public int read(byte b[]) throws IOException {
        int result =  super.read(b);
        iCurrent += result;
        return result;
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public int read() throws IOException {
        int result = super.read();
        iCurrent++;
        return result;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. This method blocks until some input is
     * available.
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code>
     * and returns the result.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        FilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        int result = super.read(b, off, len);;
        iCurrent += result;
        return result;
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the
     * input stream. The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly <code>0</code>. The actual number of bytes skipped is
     * returned.
     * <p>
     * This method
     * simply performs <code>in.skip(n)</code>.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        long result = super.skip(n);
        iCurrent += n;
        return result;
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     * <p>
     * This method
     * simply performs <code>in.reset()</code>.
     * <p>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parse, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails.
     * If this happens within readlimit bytes, it allows the outer
     * code to reset the stream and try another parser.
     *
     * @exception  IOException  if the stream has not been marked or if the
     *               mark has been invalidated.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.FilterInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
        super.reset();
        iCurrent = 0l;
    }

}
