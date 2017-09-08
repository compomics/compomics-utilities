/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 16-okt-02
 * Time: 12:11:35
 */
package com.compomics.util.io.file;
import org.apache.log4j.Logger;

import com.compomics.util.interfaces.Monitorable;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class provides a specific FilterInputStream that can be monitored for progress.
 * It is expected to be a bit slower than a regular InputStream, albeit not very much
 * (typical difference compared with only regular FileInputStream for reading a file of
 * appr. 20MB with a wrapped BufferedReader is undetectable).
 *
 * @author Lennart Martens
 */
public class MonitorableInputStream extends FilterInputStream implements Monitorable {

    // Class specific log4j logger for MonitorableInputStream instances.
    Logger logger = Logger.getLogger(MonitorableInputStream.class);

    /**
     * The total length to be read from the specified InputStream.
     */
    private int iMaximum = 0;

    /**
     * This variable holds the remainder in the InputStream.
     */
    private int iRemainder = 0;

    /**
     * This variable reports on whether or not the maximum read size has been set
     * through a parameter by the constructor caller.
     */
    private boolean iMaxSet = false;

    /**
     * When this boolean is set, the continuous cache is bypassed.
     */
    private boolean iBypassCache = false;

    /**
     * A MonitorableInputStream can only be constructed around an existing InputStream.
     *  This InputStream will be the one monitored.
     *
     * @param   aIn InputStream to monitor.
     * @exception   IOException when something is wrong with the InpuStream.
     */
    public MonitorableInputStream(InputStream aIn) throws IOException {
        this(aIn, false);
    }

    /**
     * A MonitorableInputStream can only be constructed around an existing InputStream.
     *  This InputStream will be the one monitored. This constructor allows the setting
     * of the maximum readsize. It is useful when monitoring certain files, which cannot
     * report on the available bytes.
     *
     * @param   aIn InputStream to monitor.
     * @param   aMaximum    int with the maximum number of bytes that can be read.
     * @exception   IOException when something is wrong with the InpuStream.
     */
    public MonitorableInputStream(InputStream aIn, int aMaximum) throws IOException {
        this(aIn, false);
        iMaxSet = true;
    }

    /**
     * A MonitorableInputStream can only be constructed around an existing InputStream.
     *  This InputStream will be the one monitored. This constructor can instruct the instance
     * to bypass the progress cache and update the information directly. This is useful when
     * monitoring read progress in zipfiles, which cannot report on the available bytes, and whose
     * underlying streams are masked by a PushBackInputStream.
     *
     * @param   aIn InputStream to monitor.
     * @param   aBypassCache    boolean that indicates whether the cache should be bypassed.
     * @exception   IOException when something is wrong with the InpuStream.
     */
    public MonitorableInputStream(InputStream aIn, boolean aBypassCache) throws IOException {
        super(aIn);
        iMaximum = available();
        iRemainder = iMaximum;
        iMaxSet = false;
        iBypassCache = aBypassCache;
    }

    /**
     * This method reports on the total amount of bytes that can be read
     * from the monitored InputStream.
     *
     * @return  int with the total bytes that can be read from the InputStream.
     */
    public int getMaximum() {
        return this.iMaximum;
    }

    /**
     * This method returns the progress (as  measured by taking the value
     * of iMaximum, minus the current available bytes as reported by the underlying
     * InputStream - variable iRemainder).
     *
     * @return  int with the progress.
     */
    public int monitorProgress() {
        int result = 0;
        if(iBypassCache) {
            try {
                result = this.iMaximum - available();
            } catch(IOException ioe) {
                throw new RuntimeException("Failure when monitoring real-time read progress!");
            }
        } else {
            result = this.iMaximum - this.iRemainder;
        }
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
        if(iMaxSet) {
            if(result >= 0) {
                iRemainder -= result;
            }
        } else {
            iRemainder = available();
        }
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
        if(iMaxSet) {
            iRemainder--;
        } else {
            iRemainder = available();
        }
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
        int result = super.read(b, off, len);
        if(iMaxSet) {
            if(result >= 0) {
                iRemainder -= result;
            }
        } else {
            iRemainder = available();
        }
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
        if(iMaxSet) {
            iRemainder -= (int)n;
        } else {
            iRemainder = available();
        }
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
        if(!iMaxSet) {
            iMaximum = available();
        }
        iRemainder = iMaximum;
    }
}
