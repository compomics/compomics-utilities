package com.compomics.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * A simple FTP file downloader.
 *
 * @author Harald Barsnes
 */
public class FTPDownloader {

    /**
     * The FTP client.
     */
    private FTPClient ftp = null;

    /**
     * Set up an anonymous FTP connection (without protocol commands printed).
     *
     * @param host the FTP host
     * @throws Exception thrown if the connection could not be made
     */
    public FTPDownloader(String host) throws Exception {
        this(host, "anonymous", "anonymous", false);
    }

    /**
     * Set up an anonymous FTP connection.
     *
     * @param host the FTP host
     * @param debug if true, the FTP protocol commands are printed
     * @throws Exception thrown if the connection could not be made
     */
    public FTPDownloader(String host, boolean debug) throws Exception {
        this(host, "anonymous", "anonymous", debug);
    }

    /**
     * Set up an FTP connection.
     *
     * @param host the FTP host
     * @param user the FTP user name
     * @param pwd the FTP password
     * @param debug if true, the FTP protocol commands are printed
     * @throws Exception thrown if the connection could not be made
     */
    public FTPDownloader(String host, String user, String pwd, boolean debug) throws Exception {
        ftp = new FTPClient();
        if (debug) {
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        }

        ftp.connect(host);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Could not connect to FTP Server.");
        }
        ftp.login(user, pwd);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

    /**
     * Download the given file from the FTP server.
     *
     * @param remoteFilePath the remote file path
     * @param localFilePath the local file path
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public void downloadFile(String remoteFilePath, File localFilePath) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(localFilePath);
        ftp.retrieveFile(remoteFilePath, fos);
    }

    /**
     * Disconnect from the FTP server.
     *
     * @throws IOException if an IOException occurs
     */
    public void disconnect() throws IOException {
        if (ftp.isConnected()) {
            ftp.logout();
            ftp.disconnect();
        }
    }
}
