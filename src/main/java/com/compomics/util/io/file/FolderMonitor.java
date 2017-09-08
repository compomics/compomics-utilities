/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-nov-02
 * Time: 15:32:28
 */
package com.compomics.util.io.file;
import com.compomics.util.io.ftp.FTPClient;
import org.apache.log4j.Logger;

import com.compomics.util.interfaces.PickUp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class will monitor a specified folder for activity.
 *
 * @author Lennart Martens
 */
public class FolderMonitor implements Runnable {

    // Class specific log4j logger for FolderMonitor instances.
    static Logger logger = Logger.getLogger(FolderMonitor.class);

    public static final String HOST = "HOST";
    public static final String USER = "USER";
    public static final String PASSWORD = "PASSWORD";
    public static final String TEXTMODE = "TEXTMODE";

    public static final String PICKUP = "PICKUP";
    public static final String LIMIT = "LIMIT";

    public static final int FTP_TO_SPECIFIED_DESTINATION = 0;
    public static final int GATHER_FILES_FOR_PICKUP = 1;

    /**
     * Optional Logger for this class.
     */
    private Logger iLogger = null;

    /**
     * Boolean that controls the continuity of Threaded execution.
     */
    private boolean iContinue = true;

    /**
     * The HashMap that will store the params for this operation.
     */
    private HashMap iParams = null;

    /**
     * The operation that we should perform.
     */
    private int iOperation = 0;

    /**
     * The folder we should monitor.
     */
    private File iFolder = null;

    /**
     * When specified, the filter to apply to the folder.
     */
    private FilenameFilter iFilter = null;

    /**
     * The number of files currently in the folder.
     */
    private int iCurrentLength = 0;

    /**
     * The files we should be monitoring.
     */
    private Vector iToMonitor = null;

    /**
     * The files we've already processed.
     */
    private Vector iProcessed = null;

    /**
     * The delay (in milliseconds) to take into account when monitoring files.
     */
    private long iDelay = 0l;

    /**
     * This HashMap holds the properties for the files that are being monitored.
     */
    private HashMap iFileProps = null;

    /**
     * The FTPClient we'll be using for transmitting files if we are in FTP_TRANSFER mode.
     */
    private FTPClient iFTP = null;

    /**
     * This boolean is 'true' while the FolderMonitor is running.
     */
    private boolean iRunning = false;

    /**
     * This Vector is used in the gathering operation, and gathers files.
     */
    private Vector iGatherPlace = null;

    /**
     * The class that will be notified of files to pick up when in
     * 'Gather for pickup' mode.
     */
    private PickUp iPickUp = null;

    /**
     * The maximum number of instances to send.
     * Can remain '-1' to indicate no limits.
     */
    private long iLimit = -1l;

    /**
     * This constructor allows the creation of a FolderMonitor that will take care
     * of performing a specified action, with the specified parameters, whenever a new
     * file is found.
     *
     * @param   aFolder File istance with the folder to check.
     * @param   aDelay  long with the minimal delay in milliseconds between each folder check.
     * @param   aOperation  int with the code for the operation to perform
     *                      (use only constants defined on this class).
     * @param   aParams HashMap with the necessary parameters for the operation.
     */
    public FolderMonitor(File aFolder, long aDelay, int aOperation, HashMap aParams) {
        this(aFolder, aDelay, null, aOperation, aParams, null);
    }

    /**
     * This constructor allows the creation of a FolderMonitor that will take care
     * of performing a specified action, with the specified parameters, whenever a new
     * file is found. It also specifies a logger, to which messages detailing the activity can be logged.
     *
     * @param   aFolder File istance with the folder to check.
     * @param   aDelay  long with the minimal delay in milliseconds between each folder check.
     * @param   aOperation  int with the code for the operation to perform
     *                      (use only constants defined on this class).
     * @param   aParams HashMap with the necessary parameters for the operation.
     * @param   aLogger Logger for messages from this class.
     */
    public FolderMonitor(File aFolder, long aDelay, int aOperation, HashMap aParams, Logger aLogger) {
        this(aFolder, aDelay, null, aOperation, aParams, aLogger);
    }

    /**
     * This constructor allows the creation of a FolderMonitor for the specified filtered files that will take care
     * of performing a specified action, with the specified parameters, whenever a new
     * file is found.
     *
     * @param   aFolder File instance with the folder to check.
     * @param   aDelay  long with the minimal delay in milliseconds between each folder check.
     * @param   aFilter String to filter the files in the folder through.
     * @param   aOperation  int with the code for the operation to perform
     *                      (use only constants defined on this class).
     * @param   aParams HashMap with the necessary parameters for the operation.
     */
    public FolderMonitor(File aFolder, long aDelay, String aFilter, int aOperation, HashMap aParams) {
        this(aFolder, aDelay, aFilter, aOperation, aParams, null);
    }

    /**
     * This constructor allows the creation of a FolderMonitor for the specified filtered files that will take care
     * of performing a specified action, with the specified parameters, whenever a new
     * file is found. It also specifies a logger, to which messages detailing the activity can be logged.
     *
     * @param   aFolder File instance with the folder to check.
     * @param   aDelay  long with the minimal delay in milliseconds between each folder check.
     * @param   aFilter String to filter the files in the folder through.
     * @param   aOperation  int with the code for the operation to perform
     *                      (use only constants defined on this class).
     * @param   aParams HashMap with the necessary parameters for the operation.
     * @param   aLogger Logger for messages from this class.
     */
    public FolderMonitor(File aFolder, long aDelay, String aFilter, int aOperation, HashMap aParams, Logger aLogger) {
        // Check whether the File instance exists and points to a folder, not a file!
        if(!aFolder.exists() || !aFolder.isDirectory()) {
            throw new IllegalArgumentException("The File instance you passed does not " + (aFolder.exists()?"point to a directory":"exist") + " (" + aFolder.toString() + ")!");
        } else {
            this.iFolder = aFolder;
        }

        // Check whether we know the operation.
        switch(aOperation) {
            case FTP_TO_SPECIFIED_DESTINATION:
                this.iOperation = aOperation;
                if(  (aParams.get(HOST) == null) || (aParams.get(USER) == null) || (aParams.get(PASSWORD) == null)  ) {
                    throw new IllegalArgumentException("You did not specify all necessary parameters for the FTP operation (I need host, user AND password)!");
                } else {
                    this.iParams = aParams;
                    iFTP = new FTPClient((String)iParams.get(HOST), (String)iParams.get(USER), (String)iParams.get(PASSWORD));
                }
                break;
            case GATHER_FILES_FOR_PICKUP:
                this.iOperation = aOperation;
                if(aParams.get(PICKUP) == null) {
                    throw new IllegalArgumentException("You did not specify the PICKUP class for the gathered files!");
                } else {
                    iPickUp = (PickUp)aParams.get(PICKUP);
                    Object tempObject = aParams.get(LIMIT);
                    if(tempObject != null) {
                        this.iLimit = ((Number)tempObject).longValue();
                    } else {
                        this.iLimit = -1l;
                    }
                    this.iGatherPlace = new Vector();
                }
                break;
            default:
                throw new IllegalArgumentException("The operation you specified (code=" + aOperation + ") is not known to me!");
        }

        if(aFilter != null) {
            this.iFilter = new FilenameExtensionFilter(aFilter);
        }

        this.iDelay = aDelay;

        iToMonitor = new Vector();
        iProcessed = new Vector();

        iFileProps = new HashMap();
        iLogger = aLogger;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see     java.lang.Thread#run()
     */
    public void run() {
        iRunning = true;
        while(iContinue) {
            // The list of stuff we'll have to process in this run.
            Vector toProcess = new Vector();

            // The files that will be found in the folder.
            File[] files;

            // Get all the files, currently stored in the folder.
            if(this.iFilter != null) {
                files = this.iFolder.listFiles(iFilter);
            } else {
                files = this.iFolder.listFiles();
            }

            // Find all new files to monitor.
            if((files != null) && (files.length != 0) && (files.length > iCurrentLength)) {
                for(int i = 0; i < files.length; i++) {
                    File lFile = files[i];
                    if(!lFile.isDirectory() && !iToMonitor.contains(lFile) && !iProcessed.contains(lFile)) {
                        iToMonitor.add(lFile);
                        if(iLogger != null) {
                            String time = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date());
                            iLogger.info(" - Added file: '" + lFile.getName() + "' to the monitoring list (" + time + ").");
                        }
                    }
                }
            }

            // Monitor (and process) all files we are monitoring.
            int liSize = iToMonitor.size();
            Vector toRemove = new Vector();
            for(int i = 0; i < liSize; i++) {
                File lFile = (File)iToMonitor.elementAt(i);
                String lName = lFile.getName();

                // If the file no longer exists, just remove it.
                if(!lFile.exists()) {
                    toRemove.add(lFile);
                    continue;
                }

                // If the filesize is not yet stored, store it now.
                if(iFileProps.get(lName) == null) {
                    try {
                        iFileProps.put(lName, Long.valueOf(lFile.length()));
                    } catch(Exception e) {
                    }
                } else {
                    long previous = ((Long)iFileProps.get(lName)).longValue();
                    // We already have a filesize, compare it.
                    try {
                        if(previous == lFile.length()) {
                            // The file has remained stable;
                            // add it to the list to be processed.
                            toProcess.add(lFile);
                        } else {
                            iFileProps.put(lName, Long.valueOf(lFile.length()));
                        }
                    } catch(Exception e){
                    }
                }
            }

            for(int i = 0; i < toRemove.size(); i++) {
                File lFile = (File)toRemove.elementAt(i);
                iToMonitor.remove(lFile);
                if(iLogger != null) {
                    String time = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date());
                    iLogger.info(" # Removed file '" + lFile.getName() + "' from the monitoring list since it was deleted (" + time + ").");
                }
            }
            toRemove = null;

            // Now the 'toProcess' Vector holds all files that have remained stable.
            // Process each.
            int processSize = toProcess.size();
            boolean processSuccessful = true;

            switch(iOperation) {
                case FTP_TO_SPECIFIED_DESTINATION:
                    try {
                        this.sendFilesViaFTP(toProcess);
                    } catch(IOException ioe) {
                        if(iLogger != null) {
                            String time = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date());
                            iLogger.error(" IOException occurred while attempting to process files, message: " + ioe.getMessage() + "(" + time + ")!");
                            logger.error(ioe.getMessage(), ioe);
                        }
                        processSuccessful = false;
                    }
                    break;
                case GATHER_FILES_FOR_PICKUP:
                    // See if we need to add stuff to the gathering place.
                    if(processSize > 0) {
                        for(int i = 0; i < processSize; i++) {
                            iGatherPlace.add(toProcess.elementAt(i));
                        }
                    }
                    // See if we have anything gathered.
                    if(iGatherPlace.size() > 0) {
                        this.sendGathered();
                    }
                    break;
            }

            // Now move the processed dudes from 'iToMonitor' to 'iProcessed', and
            // remove them as keys in 'iFileProps'.
            if(processSuccessful) {
                for(int i = 0; i < processSize; i++) {
                    File lFile = (File)toProcess.elementAt(i);
                    if(iLogger != null) {
                        String time = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date());
                        iLogger.info(" * Processed file '" + lFile.getName() + "' (" + time + ").");
                    }
                    iProcessed.add(lFile);
                    iToMonitor.remove(lFile);
                    iFileProps.remove(lFile.getName());
                }
            }

            // Destroy stale references.
            toProcess = null;
            files = null;

            try {
                Thread.sleep(iDelay);
            } catch(Exception e) {
                if(iLogger != null) {
                    iLogger.info("FolderMonitor Thread interrupted.");
                }
            }
        }
        iRunning = false;
    }

    /**
     * This method can be used to signal the monitor to halt its monitoring.
     */
    public void signalStop() {
        this.iContinue = false;
    }

    /**
     * This method can be consulted to find out whether the monitor is running.
     *
     * @return  boolean that indicates whether the monitor is running.
     */
    public boolean isRunning() {
        return iRunning;
    }

    /**
     * This method will FTP the specified files to the FTP server.
     *
     * @param   aFiles  Vector with the files to send.
     * @exception IOException   when the sending process failed.
     */
    private void sendFilesViaFTP(Vector aFiles) throws IOException {
        // The mode flag.
        boolean binary = true;
        // See if we need to switch to text mode.
        if(iParams.containsKey(TEXTMODE)) {
            binary = false;
        }

        // Get all the filenames.
        int liSize = aFiles.size();
        String[] allNames = new String[liSize];
        for(int i = 0; i < liSize; i++) {
            File lFile = (File)aFiles.elementAt(i);
            allNames[i] = lFile.getAbsolutePath();
        }

        // Send them all.
        if(allNames.length > 0) {
            iFTP.sendFiles(allNames, binary);
        }
    }

    /**
     * This method will send gathered files to a gatherer (PickUp).
     */
    private void sendGathered() {
        // Empty the contents of the gathering Vector.
        while(this.iGatherPlace.size() > 0) {
            // Some vars we'll need.
            int size = this.iGatherPlace.size();
            boolean doLimitCheck = iLimit > 0;
            Vector v = new Vector(size);

            // Gather all files.
            for(int i=0;i<size;i++) {
                // see if we're not exceeding the limit.
                if((doLimitCheck) && (i >= iLimit)) {
                    break;
                } else {
                    v.add(iGatherPlace.elementAt(i));
                }
            }

            // Send the files.
            File[] result = new File[v.size()];
            v.toArray(result);
            iPickUp.sendIncoming(result);

            // Now we can safely delete the sent items from the
            // gatherplace.
            size = v.size();
            for(int i=0;i<size;i++) {
                iGatherPlace.remove(v.get(i));
            }
        }
    }

    /**
     * Main method starting the foldermonitor that looks for changes to a monitored folder.
     *
     * @param args start-up arguments
     */
    public static void main(String[] args) {
        /*
        // FTP funtionality.
        HashMap params = new HashMap(3);
        params.put(FolderMonitor.HOST, "polaris");
        params.put(FolderMonitor.USER, "ftpUser");
        params.put(FolderMonitor.PASSWORD, "openFTP");
        FolderMonitor fm = new FolderMonitor(new File("f:/temp"), 5000, "txt", FolderMonitor.FTP_TO_SPECIFIED_DESTINATION, params, new DefaultOutputLoggerImplementation());
        Thread t = new Thread(fm);
        t.start();
        */

        HashMap params2 = new HashMap(2);
        params2.put(FolderMonitor.PICKUP, new PickUp(){

            /**
                 * This method should be called by the notifier when appropriate.
                 *
                 * @param   aObject Object with the data that should be sent.
                 */
            public void sendIncoming(Object aObject) {
                try {
                    File[] files = (File[])aObject;
                    for(int i = 0; i < files.length; i++) {
                        File lFile = files[i];
                        logger.info("File sent: " + lFile.getCanonicalPath());
                    }
                } catch(IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);
                }
            }
        });
        params2.put(FolderMonitor.LIMIT, Integer.valueOf(5));

        FolderMonitor fm = new FolderMonitor(new File("f:/temp"), 1000, FolderMonitor.GATHER_FILES_FOR_PICKUP, params2, null);
        Thread t = new Thread(fm);
        t.start();
    }
}
