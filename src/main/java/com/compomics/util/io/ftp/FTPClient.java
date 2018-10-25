/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-okt-02
 * Time: 13:10:11
 */
package com.compomics.util.io.ftp;
import org.apache.log4j.Logger;

import java.io.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements a 'directory listener' to listen for new files and
 * ftp them to a remote FTP server. <br>
 * This class uses the implementation found on the web for an FTP client.
 *
 * @author Lennart Martens
 */
public class FTPClient {

    /**
     * Empty default constructor
     */
    public FTPClient() {
    }

    // Class specific log4j logger for FTPClient instances.
    Logger logger = Logger.getLogger(FTPClient.class);

    /**
     * The String with the hostname for the FTP server.
     */
    private String iHost = null;

    /**
     * The String with the username for the connection
     */
    private String iUser = null;

    /**
     * The String with the password for the user.
     */
    private String iPassword = null;

    /**
     * Constructor that takes the three parameters necessary for the
     * FTP connection. Note that in FTP, passwords are typically sent in
     * plain text!!!
     *
     * @param   aHost   String with the hostname of the FTP server to connect to.
     * @param   aUser   String with the username to connect with.
     * @param   aPassword   String with the password for the specified user.
     */
    public FTPClient(String aHost, String aUser, String aPassword) {
        this.iHost = aHost;
        this.iUser = aUser;
        this.iPassword = aPassword;
    }

    /**
     * This method sends a text file to the default FTP location on the server.
     *
     * @param   aFilename   String with the filename to send.
     * @exception   IOException when retrieving the file fails, or sending the file
     *                          failed.
     */
    public void sendTextFile(String aFilename) throws IOException {
        String[] temp = new String[] {aFilename};
        this.sendFiles(temp, false);
    }

    /**
     * This method sends a binary file to the default FTP location on the server.
     *
     * @param   aFilename   String with the filename to send.
     * @exception   IOException when retrieving the file fails, or sending the file
     *                          failed.
     */
    public void sendBinaryFile(String aFilename) throws IOException {
        String[] temp = new String[] {aFilename};
        this.sendFiles(temp, true);
    }

    /**
     * This method sends a group of files to the default FTP location on the server.
     * It also allows the specification of binary or text mode.
     *
     * @param   aFilenames  String[] with the filenames of the files to send.
     * @param   aBinaryMode boolean[] to indicate whether the files are to be send in binary
     *                      transfer mode ('true') or text mode ('false'). Note that this
     *                      setting applies to all files in the group.
     * @exception   IOException when retrieving the file fails, or sending the file
     *                          failed.
     */
    public void sendFiles(String[] aFilenames, boolean[] aBinaryMode) throws IOException {
        // Create a connection to the FTP server.
        FTP client = new FTP(iHost);
        // A passive connection allows the FTP client to traverse proxy servers e.d.
        client.setPassive(true);
        client.login(iUser, iPassword);

        // Cycle all files.
        for(int i = 0; i < aFilenames.length; i++) {
            String lFilename = aFilenames[i];
            boolean lMode = aBinaryMode[i];
            File f = new File(lFilename);
            // Check if the file exists.
            if(!f.exists()) {
                throw new IOException("Attempting to send non-existant file: '" + lFilename + "'!");
            }
            String name = f.getName();
            // Get the streams.
            FileInputStream fis = new FileInputStream(f);
            // Check which mode to use.
            if(lMode) {
                // Binary mode.
                BufferedOutputStream bos = client.putBinary(name);

                // Write!
                int current = -1;
                while((current = fis.read()) != -1) {
                    bos.write(current);
                }
                bos.flush();
                bos.close();

            } else {
                // ASCII mode.
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                PrintWriter pw = new PrintWriter(client.putAscii(name));
                String line= null;
                while((line = br.readLine()) != null) {
                    pw.print(line + "\n");
                }
                br.close();
                pw.flush();
                pw.close();
            }


            // Flush and close the streams.
            fis.close();
        }
        client.closeServer();
    }

    /**
     * This method sends a group of files to the default FTP location on the server.
     * It also allows the specification of binary or text mode.
     *
     * @param   aFilenames  String[] with the filenames of the files to send.
     * @param   aBinaryMode boolean to indicate whether the files are to be send in binary
     *                      transfer mode ('true') or text mode ('false'). Note that this
     *                      setting applies to all files in the group.
     * @exception   IOException when retrieving the file fails, or sending the file
     *                          failed.
     */
    public void sendFiles(String[] aFilenames, boolean aBinaryMode) throws IOException {
        boolean[] temp = new boolean[aFilenames.length];
        for(int i = 0; i < aFilenames.length; i++) {
            temp[i] = aBinaryMode;
        }
        this.sendFiles(aFilenames, temp);
    }

    /**
     * This method can be used to test the connection with the FTP server.
     *
     * @exception   IOException whenever a connection could not be established.
     */
    public void testFTPConnection() throws IOException {
        // Create a connection to the FTP server.
        FTP client = new FTP(iHost);
        client.login(iUser, iPassword);

        // Close the connection again. Apparently, it worked.
        client.closeServer();
    }
}
