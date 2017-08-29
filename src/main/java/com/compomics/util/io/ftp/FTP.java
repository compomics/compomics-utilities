/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 29-nov-02
 * Time: 13:40:47
 */
package com.compomics.util.io.ftp;
import org.apache.log4j.Logger;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class was modified from a source found on the net (java.sun.com, search in
 * developer section on 'FtpProtocolException PORT').
 *
 * @author Lennart Martens + someone on the net...
 */
public class FTP {

    // Class specific log4j logger for FTP instances.
    Logger logger = Logger.getLogger(FTP.class);

    /**
     * FTP port to use for connection.
     */
    public static final int FTP_PORT = 21;

    /**
     * Pre-defined state.
     */
    static int FTP_UNKNOWN = -1;
    /**
     * Pre-defined state.
     */
    static int FTP_SUCCESS = 1;
    /**
     * Pre-defined state.
     */
    static int FTP_TRY_AGAIN = 2;
    /**
     * Pre-defined state.
     */
    static int FTP_ERROR = 3;
    /**
     * Pre-defined state.
     */
    static int FTP_NOCONNECTION = 100;
    /**
     * Pre-defined state.
     */
    static int FTP_BADUSER = 101;
    /**
     * Pre-defined state.
     */
    static int FTP_BADPASS = 102;
    /**
     * Pre-defined state.
     */
    public static int FILE_GET = 1;
    /**
     * Pre-defined state.
     */
    public static int FILE_PUT = 2;
    /**
     * socket for data transfer to and from the server.
     */
    private Socket dataSocket = null;
    /**
     * Flag that indicates that we need to process a previous commands
     * reply first.
     */
    private boolean replyPending = false;
    /**
     * Boolean to indicate the use of binary mode.
     */
    private boolean binaryMode = false;
    /**
     * Boolean to indicate the use of passive mode.
     */
    private boolean passiveMode = false;
    /**
     * Boolena to indicite whether we are currently receiving a file.
     */
    private boolean m_bGettingFile = false;
    /**
     * The ser name for login.
     */
    String user = null;
    /**
     * The password for login.
     */
    String password = null;
    /**
     * The last command issued.
     */
    public String command;
    /**
     * The last reply code from the ftp daemon.
     */
    int lastReplyCode;
    /**
     * Welcome message from the server, if any.
     */
    public String welcomeMsg;
    /**
     * Array of strings (usually 1 entry) for the last reply from the server.
     */
    protected Vector serverResponse = new Vector(1);
    /**
     * Socket for communicating commands with the server.
     */
    protected Socket controlSocket = null;
    /**
     * Stream for printing to the server.
     */
    public PrintWriter serverOutput;
    /**
     * Buffered stream for reading replies from server.
     */
    public InputStream serverInput;
    /**
     * String to hold the file we are up/downloading
     */
    protected String strFileNameAndPath;
    protected String m_strSource;
    protected String m_strDestination;

    /**
     * This method sets the name of the file to up- or download.
     *
     * @param   strFile   String with the filename.
     */
    public void setFilename(String strFile) {
        strFileNameAndPath = strFile;
    }

    /**
     * This method reports on the filename used for current
     * transfer.
     *
     * @return  String  with the filename.
     */
    String getFileName() {
        return strFileNameAndPath;
    }

    /**
     * This method allows to set the source for the file that is to be
     * transferred.
     *
     * @param   strSourceFile   String with the sourec for the file to be
     *                          transferred.
     */
    public void setSourceFile(String strSourceFile) {
        m_strSource = strSourceFile;
    }

    /**
     * This method allows the specification of the destinationfile.
     *
     * @param   strDestinationFile  String with the destination file.
     */
    public void setDestinationFile(String strDestinationFile) {
        m_strDestination = strDestinationFile;
    }

    /**
     * This method reports on the sourcefile currently used in transfer.
     *
     * @return  String with the source file.
     */
    public String getSourceFile() {
        return m_strSource;
    }

    /**
     * This method reports on the destinationfile currently used in transfer.
     *
     * @return  String with the destination file.
     */
    public String getDestinationFile() {
        return m_strDestination;
    }

    /**
     * Return server connection status
     *
     * @return  boolean 'true' when connected, 'false' otherwise.
     */
    public boolean serverIsOpen() {
        return controlSocket != null;
    }

    /**
     * Set Passive mode Transfers. This is particularly useful
     * when attempting to FTP via passive mode.
     *
     * @param   mode    boolean to indicate whether passive mode
     *                  should be used.
     */
    public void setPassive(boolean mode) {
        passiveMode = mode;
    }

    /**
     * This method allows the caller to capture the server response.
     *
     * @return  int with the server's response.
     * @throws IOException if an IOException occurs
     */
    public int readServerResponse() throws IOException {
        StringBuffer replyBuf = new StringBuffer(32);
        int c;
        int continuingCode = -1;
        int code = -1;
        String response;
        try {
            while(true) {
                while((c = serverInput.read()) != -1) {
                    if(c == '\r') {
                        if((c = serverInput.read()) != '\n') {
                            replyBuf.append('\r');
                        }
                    }
                    replyBuf.append((char)c);
                    if(c == '\n') break;
                }
                response = replyBuf.toString();
                replyBuf.setLength(0);
                try {
                    code = Integer.parseInt(response.substring(0, 3));
                } catch(NumberFormatException e) {
                    code = -1;
                } catch(StringIndexOutOfBoundsException e) {
                    // this line doesn't contain a response code,
                    // so we just completely ignore it.
                    continue;
                }
                serverResponse.addElement(response);
                if(continuingCode != -1) {
                    // we've seen an XXX- sequence!
                    if(code != continuingCode || (response.length() >= 4 && response.charAt(3) == '-')) {
                        continue;
                    } else {
                        // We've seen the end of code sequence.
                        continuingCode = -1;
                        break;
                    }
                } else if(response.length() >= 4 && response.charAt(3) == '-') {
                    continuingCode = code;
                    continue;
                } else {
                    break;
                }
            }
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        // Store the response for later reference.
        return lastReplyCode = code;
    }

    /**
     * Sends command <i>cmd</i> to the server.
     *
     * @param   cmd String with the command to send.
     */
    public void sendServer(String cmd) {
        serverOutput.println(cmd);
    }

    /**
     * Returns all server response strings.
     * It also clears the server-response buffer!
     *
     * @return  String  with all the server response strings.
     */
    public String getResponseString() {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < serverResponse.size(); i++) {
            sb.append(serverResponse.elementAt(i));
        }
        serverResponse = new Vector(1);
        return sb.toString();
    }

    /**
     * This method allows the caller to read the response strings from
     * the server, without resetting the internal buffer (and thus not clearing the
     * messages read by this messages).
     *
     * @return  String  with the server responses.
     */
    public String getResponseStringNoReset() {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < serverResponse.size(); i++) {
            sb.append(serverResponse.elementAt(i));
        }
        return sb.toString();
    }

    /**
     * Issue the QUIT command to the FTP server and close the connection.
     * @throws IOException if an IOException occurs
     */
    public void closeServer() throws IOException {
        if(serverIsOpen()) {
            issueCommand("QUIT");
            if(!serverIsOpen()) {
                return;
            }
            controlSocket.close();
            controlSocket = null;
            serverInput = null;
            serverOutput = null;
        }
    }

    /**
     * This method allows the caller to issue a command to the server.
     *
     * @param   cmd String with the command to issue.
     * @return  int with the server reply status.
     * @exception   IOException when the connection with the server failed.
     */
    protected int issueCommand(String cmd) throws IOException {
        command = cmd;
        int reply;

        if(replyPending) {

            if(readReply() == FTP_ERROR) {
                logger.info("Error reading pending reply\n");
            }
        }
        replyPending = false;
        do {
            sendServer(cmd);
            reply = readReply();
        } while(reply == FTP_TRY_AGAIN);
        return reply;
    }

    /**
     * This method will issue the specified command and throw an exception
     * whenever the reply is not equal to success! It basically converts an
     * FTP error code into an FtpProtocolException.
     *
     * @param   cmd String with the command to issue (and verify the response for)
     * @exception   IOException when the connection failed OR FtpProtocolException when the
     *                          command was not understood by the server.
     */
    protected void issueCommandCheck(String cmd) throws IOException {
        if(issueCommand(cmd) != FTP_SUCCESS) {
            throw new FtpProtocolException(cmd);
        }
    }

    /**
     * This method attempts to read a reply from the FTP server.
     *
     * @return  int with the reply from the server.
     * @exception   IOException whenever the reply could not be read.
     */
    protected int readReply() throws IOException {
        // The last replycode is read from the server.
        lastReplyCode = readServerResponse();
        // Determine the nature of the response.
        switch(lastReplyCode / 100) {
            case 1:
                replyPending = true;
                // Falls into ...
            case 2:
                //This case is for future purposes. If not properly used, it might cause an infinite loop.
                //Don't add any code here , unless you know what you are doing.
            case 3:
                return FTP_SUCCESS;
            case 5:
                // 530 is login exception.
                if(lastReplyCode == 530) {
                    if(user == null) {
                        throw new FtpLoginException("Not logged in");
                    }
                    return FTP_ERROR;
                }
                // 550 means logged in with wrong password (no access)
                // OR no access because file not found!
                if(lastReplyCode == 550) {
                    if(!command.startsWith("PASS")) {
                        throw new FileNotFoundException(command);
                    } else {
                        throw new FtpLoginException("Error: Wrong Password!");
                    }
                }
        }
        return FTP_ERROR;
    }

    /**
     * This method will set up the networking for client-server data transfer and
     * it will send the specified command to the server. This is typically used only
     * for true file transfer.
     *
     * @param   cmd String with the command to issue.
     * @return  Socket  with the data connection socket.
     * @exception   IOException whenever communications could not be established.
     */
    protected Socket openDataConnection(String cmd) throws IOException {
        ServerSocket portSocket = null;
        String portCmd;
        // Local address.
        InetAddress myAddress = InetAddress.getLocalHost();
        byte addr[] = myAddress.getAddress();
        int shift;
        String ipaddress;
        int port = 0;
        IOException e;
        if(this.passiveMode) {
            /* First let's attempt to initiate Passive transfers */
            try {    // PASV code
                // Clear the response buffer.
                getResponseString();
                // Test the support for passive mode transfer.
                if(issueCommand("PASV") == FTP_ERROR) {
                    e = new FtpProtocolException("PASV");
                    throw e;
                }
                // OK, passive mode supported, get the servers response.
                String reply = getResponseStringNoReset();
                // Section between brackets contains host and port information.
                reply = reply.substring(reply.indexOf("(") + 1, reply.indexOf(")"));
                StringTokenizer st = new StringTokenizer(reply, ",");
                String[] nums = new String[6];
                int i = 0;
                while(st.hasMoreElements()) {
                    try {
                        nums[i] = st.nextToken();
                        i++;
                    } catch(Exception a) {
                        logger.error(a.getMessage(), a);
                    }
                }
                // Reconstruct the IP address.
                ipaddress = nums[0] + "." + nums[1] + "." + nums[2] + "." + nums[3];
                try {
                    int firstbits = Integer.parseInt(nums[4]) << 8;
                    int lastbits = Integer.parseInt(nums[5]);
                    // Reconstruct the port from the information in the header.
                    port = firstbits + lastbits;
                } catch(Exception b) {
                    logger.error(b.getMessage(), b);
                }
                // If we were successful in reconstituting IP and port information,
                // create a socket to this port.
                // Else the protocol was not understandable.
                if((ipaddress != null) && (port != 0)) {
                    dataSocket = new Socket(ipaddress, port);
                } else {
                    e = new FtpProtocolException("PASV");
                    throw e;
                }
                // Try to execute the command.
                if(issueCommand(cmd) == FTP_ERROR) {
                    e = new FtpProtocolException(cmd);
                    throw e;
                }
            } catch(FtpProtocolException fpe) {  // PASV was not supported...resort to PORT
                portCmd = "PORT ";
                // Append host address to the command.
                for(int i = 0; i < addr.length; i++) {
                    portCmd = portCmd + (addr[i] & 0xFF) +",";
                }
                // Append port number to the command.
                portCmd = portCmd + ((portSocket.getLocalPort() >>> 8) & 0xff) + "," + (portSocket.getLocalPort() & 0xff);
                // Try to issue the command over the local port.
                if(issueCommand(portCmd) == FTP_ERROR) {
                    e = new FtpProtocolException("PORT");
                    portSocket.close();
                    throw e;
                }
                if(issueCommand(cmd) == FTP_ERROR) {
                    e = new FtpProtocolException(cmd);
                    portSocket.close();
                    throw e;
                }
                dataSocket = portSocket.accept();
                portSocket.close();
            }
        }//end if passive
        else {  //do a port transfer
            try {
                // Set-up a local port for the communication.
                portSocket = new ServerSocket(0, 1, myAddress);
            } catch(Exception b) {
                logger.error(b.getMessage(), b);
            }
            portCmd = "PORT ";
            // Append host address.
            for(int i = 0; i < addr.length; i++) {
                portCmd = portCmd + (addr[i] & 0xFF) +",";
            }
            // Append port number.
            portCmd = portCmd + ((portSocket.getLocalPort() >>> 8) & 0xff) + "," + (portSocket.getLocalPort() & 0xff);
            // This should work - unless the client is locked down by a firewall,
            // or bypassed via a proxy.
            if(issueCommand(portCmd) == FTP_ERROR) {
                e = new FtpProtocolException("PORT");
                portSocket.close();
                throw e;
            }
            // Issue the command to the server.
            if(issueCommand(cmd) == FTP_ERROR) {
                e = new FtpProtocolException(cmd);
                portSocket.close();
                throw e;
            }
            // Wait for incoming data.
            dataSocket = portSocket.accept();
            portSocket.close();
        }//end of port transfer
        return dataSocket;     // return the dataSocket
    }

    /**
     * open a FTP connection to host <i>host</i>.
     *
     * @param   host    String with the hostname (or IP) to connect to.
     * @exception   IOException whenever connection could not be established.
     * @exception   UnknownHostException    when the hostname cannot be resolved.
     */
    public void openServer(String host) throws IOException, UnknownHostException {
        // FTP ports default to port 21.
        int port = FTP_PORT;
        // If we have a connection going, close it first.
        if(controlSocket != null) closeServer();
        // Attempt a connection.
        controlSocket = new Socket(host, FTP_PORT);
        // Get streams for communication.
        serverOutput = new PrintWriter(new BufferedOutputStream(controlSocket.getOutputStream()), true);
        serverInput = new BufferedInputStream(controlSocket.getInputStream());
    }

    /**
     * Open an FTP connection to host <i>host</i> on port <i>port</i>.
     * This method can be used whenever the default FTP port (21) does not apply.
     *
     * @param   host    String with the hostname (or IP) of the FTP server.
     * @param   port    int with the portnumber the FTP server is listening on.
     * @exception   IOException whenever connection could not be established.
     * @exception   UnknownHostException    when the hostname cannot be resolved.
     */
    public void openServer(String host, int port) throws IOException, UnknownHostException {
        // If we have a connection going, close it first.
        if(controlSocket != null) closeServer();
        // Open the socket.
        controlSocket = new Socket(host, port);
        //controlSocket.setSoLinger(true,30000);
        // Get control input and output Streams.
        serverOutput = new PrintWriter(new BufferedOutputStream(controlSocket.getOutputStream()), true);
        serverInput = new BufferedInputStream(controlSocket.getInputStream());
        // If the reply is not what we expect, signal this - we're probably not connected to an FTP server then!
        if(readReply() == FTP_ERROR) throw new FtpConnectionException("Welcome message");
    }

    /**
     * Login user to a host with username <i>user</i> and password
     * <i>password</i>.
     *
     * @param   user    String with the username to use.
     * @param   password    String with the password to use (passwords are sent in plain text in FTP!)
     * @throws IOException if an IOException occurs
     */
    public void login(String user, String password) throws IOException {
        // See if we are connected first.
        if(!serverIsOpen()) {
            throw new FtpLoginException("Error: not connected to host.\n");
        }
        this.user = user;
        this.password = password;
        if(issueCommand("USER " + user) == FTP_ERROR) {
            throw new FtpLoginException("Error: User not found.\n");
        }
        if(password != null && issueCommand("PASS " + password) == FTP_ERROR){
            throw new FtpLoginException("Error: Wrong Password.\n");
        }
    }

    /**
     * Login user to a host with username <i>user</i> and no password
     * such as HP server which uses the form "&lt;username&gt;/&lt;password&gt;,user.&lt;group&gt;.
     *
     * @param   user    String with the username (and possibly coded information such as password).
     * @throws IOException if an IOException occurs
     */
    public void login(String user) throws IOException {
        // See if we are connected first.
        if(!serverIsOpen()) {
            throw new FtpLoginException("not connected to host");
        }
        this.user = user;
        if(issueCommand("USER " + user) == FTP_ERROR) {
            throw new FtpLoginException("Error: Invalid Username.\n");
        }
    }

    /**
     * GET a file from the FTP server in Ascii mode.
     *
     * @param   filename    String with the filename to get from the server.
     * @return  BufferedReader  to the file on the server.
     * @exception   IOException whenever the file could not be read.
     */
    public BufferedReader getAscii(String filename) throws IOException {
        // Flag that we are processing an incoming file.
        m_bGettingFile = true;
        Socket s = null;
        try {
            // Get a connection for the retrieval ('RETR' command) of the
            // specified file.
            s = openDataConnection("RETR " + filename);
        } catch(FileNotFoundException fileException) {
            throw new FileNotFoundException();
        }
        return new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    /**
     * GET a file from the FTP server in Binary mode.
     *
     * @param   filename    String with the filename to get from the server.
     * @return  BufferedInputStream  to the file on the server.
     * @exception   IOException whenever the file could not be read.
     */
    public BufferedInputStream getBinary(String filename) throws IOException {
        // Flag that we are processing an incoming file.
        m_bGettingFile = true;
        Socket s = null;
        try {
            // Get a connection for the retrieval ('RETR' command) of the
            // specified file.
            s = openDataConnection("RETR " + filename);
        } catch(FileNotFoundException fileException) {
            throw new FileNotFoundException();
        }
        return new BufferedInputStream(s.getInputStream());
    }

    /**
     * PUT a file on the FTP server in Ascii mode.
     *
     * @param   filename    String with the filename to put on the server.
     * @return  BufferedWriter  writer for completing the file on the server.
     * @exception   IOException whenever the file could not be sent.
     */
    public BufferedWriter putAscii(String filename) throws IOException {
        // Indicate that we are sending (as opposed to getting).
        m_bGettingFile = false;
        // Get a connection for the storage ('STOR' command) of the
        // specified file.
        Socket s = openDataConnection("STOR " + filename);
        // Writer has 4 megabyte buffer.
        return new BufferedWriter(new OutputStreamWriter(s.getOutputStream()), 4096);
    }

    /**
     * PUT a file to the FTP server in Binary mode
     *
     * @param   filename    String with the filename to put on the server.
     * @return  BufferedOutputStream  outputstream for completing the file on the server.
     * @exception   IOException whenever the file could not be sent.
     */
    public BufferedOutputStream putBinary(String filename) throws IOException {
        // Indicate that we are sending (as opposed to getting).
        m_bGettingFile = false;
        // Get a connection for the storage ('STOR' command) of the
        // specified file.
        Socket s = openDataConnection("STOR " + filename);
        return new BufferedOutputStream(s.getOutputStream());
    }

    /**
     * APPEND (with create) to a file to the FTP server in Ascii mode.
     *
     * @param   filename    String with the name of the file to append to.
     * @return  BufferedWriter  with the stream for appending to.
     * @exception   IOException whenever the writer fails.
     */
    public BufferedWriter appendAscii(String filename) throws IOException {
        // Indicate that we are sending (as opposed to getting).
        m_bGettingFile = false;
        // Get a connection for appending ('APPE' command) the
        // specified file.
        Socket s = openDataConnection("APPE " + filename);
        // Writer has 4 megabyte buffer.
        return new BufferedWriter(new OutputStreamWriter(s.getOutputStream()), 4096);
    }

    /**
     * APPEND (with create) to a file to the FTP server in Binary mode.
     *
     * @param   filename    String with the name of the file to append to.
     * @return  BufferedOutputStream  with the stream for appending to.
     * @exception   IOException whenever the writer fails.
     */
    public BufferedOutputStream appendBinary(String filename) throws IOException {
        // Indicate that we are sending (as opposed to getting).
        m_bGettingFile = false;
        // Get a connection for appending ('APPE' command) the
        // specified file.
        Socket s = openDataConnection("APPE " + filename);
        return new BufferedOutputStream(s.getOutputStream());
    }

    /**
     * NLIST files on a remote FTP server
     *
     * @return  BufferedReader  to read the listing from.
     * @exception   IOException whenever the listing failed.
     */
    public BufferedReader nlist() throws IOException {
        Socket s = openDataConnection("NLST");
        return new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    /**
     * LIST files on a remote FTP server.
     *
     * @return  BufferedReader  to read the listing from.
     * @exception   IOException whenever the listing failed.
     */
    public BufferedReader list() throws IOException {
        Socket s = openDataConnection("LIST");
        return new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    /**
     * Folder-list files on a remote FTP server.
     *
     * @return  BufferedReader  to read the listing from.
     * @exception   IOException whenever the listing failed.
     */
    public BufferedReader ls() throws IOException {
        Socket s = openDataConnection("LS");
        return new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    /**
     * Folder-list files on a remote FTP server.
     *
     * @return  BufferedReader  to read the listing from.
     * @exception   IOException whenever the listing failed.
     */
    public BufferedReader dir() throws IOException {
        Socket s = openDataConnection("DIR");
        return new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    /**
     * CD to a specific directory on a remote FTP server.
     *
     * @param   remoteDirectory String with the directory to CD to.
     * @exception   IOException whenever the CD failed.
     */
    public void cd(String remoteDirectory) throws IOException {
        issueCommandCheck("CWD " + remoteDirectory);
    }

    /**
     * Change working directory to a specific directory on a remote FTP server.
     *
     * @param   remoteDirectory String with the directory to CWD to.
     * @exception   IOException whenever the CWD failed.
     */
    public void cwd(String remoteDirectory) throws IOException {
        issueCommandCheck("CWD " + remoteDirectory);
    }

    /**
     * Rename a file on the remote server.
     *
     * @param   oldFile String with the original filename for the file to rename.
     * @param   newFile String with the filename to rename the file to.
     * @exception   IOException when the renaming failed.
     */
    public void rename(String oldFile, String newFile) throws IOException {
        issueCommandCheck("RNFR " + oldFile);
        issueCommandCheck("RNTO " + newFile);
    }

    /**
     * Site Command
     *
     * @param   params  String with the parameters for the SITE command.
     * @exception   IOException when the SITE command failed.
     */
    public void site(String params) throws IOException {
        issueCommandCheck("SITE " + params);
    }

    /**
     * Set transfer type to 'I' (binary transfer).
     *
     * @exception   IOException when the binary mode could not be set up.
     */
    public void binary() throws IOException {
        issueCommandCheck("TYPE I");
        binaryMode = true;
    }

    /**
     * Set transfer type to 'A' (ascii transfer).
     *
     * @exception   IOException when the ASCII mode could not be set up.
     */
    public void ascii() throws IOException {
        issueCommandCheck("TYPE A");
        binaryMode = false;
    }

    /**
     * Send Abort command.
     *
     * @exception   IOException when the cancellation could not be
     *                          executed.
     */
    public void abort() throws IOException {
        issueCommandCheck("ABOR");
    }

    /**
     * Go up one directory on remote system.
     *
     * @exception   IOException when the CDUP failed.
     */
    public void cdup() throws IOException {
        issueCommandCheck("CDUP");
    }

    /**
     * Create a directory on the remote system
     *
     * @param   aDir   String with the name for the directory to be created.
     * @exception   IOException when the directory could not be created.
     */
    public void mkdir(String aDir) throws IOException {
        issueCommandCheck("MKD " + aDir);
    }

    /**
     * Delete the specified directory from the ftp server file system.
     *
     * @param   aDir    String with the directory to delete.
     * @exception   IOException when the deletion did not succeed.
     */
    public void rmdir(String aDir) throws IOException {
        issueCommandCheck("RMD " + aDir);
    }

    /**
     * Delete the specified file from the ftp server file system.
     *
     * @param   aFile   String with the filename for the file to delete.
     * @exception   IOException when the deletion did not succeed.
     */
    public void delete(String aFile) throws IOException {
        issueCommandCheck("DELE " + aFile);
    }

    /**
     * Get the name of the present working directory on the ftp server file system.
     *
     * @exception   IOException whenever the server did not report on the pwd.
     */
    public void pwd() throws IOException {
        issueCommandCheck("PWD");
    }

    /**
     * Retrieve the system type from the remote server.
     *
     * @exception   IOException whenever the system type could not be determined.
     */
    public void syst() throws IOException {
        issueCommandCheck("SYST");
    }

    /**
     * Constructor for an FTP client connected to host <i>host</i>.
     * Note that this constructor automatically makes the connection.
     *
     * @param   host    String with the hostname (or IP) to connect to.
     * @exception   IOException whenever a connection could not be made.
     */
    public FTP(String host) throws IOException {
        openServer(host, FTP_PORT);
    }

    /**
     * Constructor for an FTP client connected to host <i>host</i>
     * and port <i>port</i>.
     * Note that this constructor automatically makes the connection.
     *
     * @param   host    String with the hostname (or IP) to connect to.
     * @param   port    int with the portnumber for the host FTP server.
     * @exception   IOException whenever a connection could not be made.
     */
    public FTP(String host, int port) throws IOException {
        openServer(host, port);
    }

    /**
     * This method sets the file transfer mode.
     *
     * @param   nMode   int with the mode (either FILE_GET for retrieval,
     *                  or any other int for sending a file).
     */
    public void SetFileMode(int nMode) {
        if(nMode == FILE_GET) {
            m_bGettingFile = true;
        } else {
            m_bGettingFile = false;
        }
    }
}
