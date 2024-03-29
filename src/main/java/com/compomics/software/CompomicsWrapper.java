package com.compomics.software;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.software.autoupdater.GUIFileDAO;
import com.compomics.software.autoupdater.MavenJarFile;
import com.compomics.software.autoupdater.WebDAO;
import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.gui.JOptionEditorPane;
import com.compomics.util.gui.UtilitiesGUIDefaults;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.io.StreamGobbler;
import com.compomics.util.parameters.UtilitiesUserParameters;
import java.awt.Image;
import java.io.*;
import java.net.URLDecoder;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.stream.XMLStreamException;
import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;

/**
 * A general wrapper for compomics tools. All tools shall contain a
 * resources/conf folder. In it a JavaOptions.txt and splash screen. Eventually
 * JavaHome.txt and proxy/uniprotjapi.properties.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 * @author Davy Maddelein
 * @author Carlos Horro
 */
public class CompomicsWrapper {

    /**
     * If set to true debug output will be written to the screen and to
     * startup.log.
     */
    private final boolean useStartUpLog = true;
    /**
     * True if this the first time the wrapper tries to launch the application.
     * If the first launch fails, e.g., due to memory settings, it is set to
     * false.
     */
    private boolean firstTry = true;
    /**
     * Is set to true if proxy settings are found in the JavaOptions file.
     */
    private boolean proxySettingsFound = false;
    /**
     * The user preferences.
     */
    private UtilitiesUserParameters userParameters;
    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;

    /**
     * Constructor.
     */
    public CompomicsWrapper() {
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param toolName the name of the tool
     * @param jarFile the jar file to execute
     * @param splashName the splash name, for example peptide-shaker-splash.png
     * @param mainClass the main class to execute, for example
     * eu.isas.peptideshaker.gui.PeptideShakerGUI
     */
    public void launchTool(
            String toolName,
            File jarFile,
            String splashName,
            String mainClass
    ) {
        launchTool(
                toolName,
                jarFile,
                splashName,
                mainClass,
                null
        );
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param toolName the name of the tool
     * @param jarFile the jar file to execute
     * @param splashName the splash name, for example peptide-shaker-splash.png,
     * can be null
     * @param mainClass the main class to execute, for example
     * eu.isas.peptideshaker.gui.PeptideShakerGUI
     * @param args the arguments to pass to the tool (ignored if null)
     */
    public void launchTool(
            String toolName,
            File jarFile,
            String splashName,
            String mainClass,
            String[] args
    ) {

        BufferedWriter bw = null;

        try {
            try {

                userParameters = UtilitiesUserParameters.loadUserParameters();

            } catch (Exception e) {

                e.printStackTrace();

            }

            if (useStartUpLog) {

                File folder = new File(jarFile.getParentFile(), "resources/conf");

                if (!folder.exists()) {

                    String path = URLDecoder.decode(jarFile.getParentFile().getAbsolutePath(), "UTF-8");
                    folder = new File(path, "resources/conf");

                }

                if (!folder.exists()) {

                    if (folder.mkdirs()) {

                        copyDefaultJavaOptionsFile(folder);

                    } else {

                        throw new FileNotFoundException(folder.getAbsolutePath() + " not found!");

                    }

                } else if (!new File(folder.getAbsolutePath(), "JavaOptions.txt").exists()) {

                    copyDefaultJavaOptionsFile(folder);

                }

                File debugOutput = new File(folder, "startup.log");
                FileWriter fw = new FileWriter(debugOutput);
                bw = new BufferedWriter(fw);

                bw.write("Memory settings read from the user preferences: " + userParameters.getMemoryParameter() + System.getProperty("line.separator"));
                bw.write("args: ");

                if (args != null) {

                    bw.write(
                            String.join(" ", args)
                    );

                }

                bw.newLine();

            }

            checkJavaVersion(toolName);

            try {

                UtilitiesGUIDefaults.setLookAndFeel();

            } catch (FileNotFoundException e) {

                e.printStackTrace();

                // perhaps not the optimal way of catching this exitValue, but seems to work
                JOptionPane.showMessageDialog(null,
                        "Seems like you are trying to start " + toolName + " from within a zip file!",
                        toolName + " - Startup Failed", JOptionPane.ERROR_MESSAGE);

            } catch (Exception e) {

                e.printStackTrace();

            }

            launch(jarFile, splashName, mainClass, args, bw);

        } catch (Exception e) {

            e.printStackTrace();

            try {

                if (useStartUpLog && bw != null) {

                    bw.write(e.getMessage());
                    bw.close();

                }

                JOptionPane.showMessageDialog(null,
                        "Failed to start " + toolName + ":" + System.getProperty("line.separator")
                        + e.getMessage(),
                        toolName + " - Startup Failed", JOptionPane.ERROR_MESSAGE);

            } catch (IOException ex) {

                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start " + toolName + ":" + System.getProperty("line.separator")
                        + e.getMessage() + "\nCould not write to statup.log file",
                        toolName + " - Startup Failed", JOptionPane.ERROR_MESSAGE);

            }
        }
    }

    /**
     * Launches the jar file with parameters to the jvm.
     *
     * @param jarFile the jar file to execute
     * @param splashName the splash name, for example peptide-shaker-splash.png,
     * can be null
     * @param mainClass the main class to execute, for example
     * eu.isas.peptideshaker.gui.PeptideShakerGUI
     * @param args the arguments to pass to the tool (ignored if null)
     * @param bw buffered writer for the log files, can be null
     *
     * @throws java.lang.Exception
     */
    private void launch(
            File jarFile,
            String splashName,
            String mainClass,
            String[] args,
            BufferedWriter bw
    ) throws UnsupportedEncodingException,
            FileNotFoundException,
            IOException {

        File confFolder = new File(jarFile.getParentFile(), "resources/conf");

        // check if the user has set a non-standard Java home location
        String javaHome = getJavaHome(confFolder, bw);

        // get the splash 
        String splashPath = null;
        if (splashName != null) {

            splashPath = "resources/conf" + File.separator + splashName;

            // set the correct slashes for the splash path
            if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {

                splashPath = splashPath.replace("/", "\\");

                // remove the initial '\' at the start of the line 
                if (splashPath.startsWith("\\") && !splashPath.startsWith("\\\\")) {
                    splashPath = splashPath.substring(1);
                }

            }

        }

        String uniprotProxyClassPath = "";
        String quote = CommandLineUtils.getQuoteType();

        // add the classpath for the uniprot proxy file
        if (proxySettingsFound) {

            uniprotProxyClassPath = confFolder.getAbsolutePath() + File.separator + "proxy";

            // set the correct slashes for the proxy path
            if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
                uniprotProxyClassPath = uniprotProxyClassPath.replace("/", "\\");

                // remove the initial '\' at the start of the line 
                if (uniprotProxyClassPath.startsWith("\\") && !uniprotProxyClassPath.startsWith("\\\\")) {
                    uniprotProxyClassPath = uniprotProxyClassPath.substring(1);
                }

            }

            uniprotProxyClassPath = ";" + quote + uniprotProxyClassPath + quote;
        }

        String jarFilePath = jarFile.getAbsolutePath();

        if (!new File(jarFilePath).exists()) {
            jarFilePath = URLDecoder.decode(jarFile.getAbsolutePath(), "UTF-8");
        }

        // create the command line
        ArrayList<String> process_name_array = new ArrayList();

        process_name_array.add(javaHome);

        // splash screen
        if (splashName != null) {
            process_name_array.add("-splash:" + splashPath);
        }

        // get the java options
        ArrayList<String> optionsAsList = getJavaOptions(confFolder, bw);

        for (String currentOption : optionsAsList) {
            process_name_array.add(currentOption);
        }

        process_name_array.add("-cp");

        // get the class path
        String classPath = quote + jarFilePath;

        if (uniprotProxyClassPath.trim().length() > 0) {
            classPath += uniprotProxyClassPath;
        }

        classPath += quote;
        process_name_array.add(classPath);

        // add the main class
        process_name_array.add(mainClass);

        // add arguments, if any
        if (args != null) {
            process_name_array.addAll(Arrays.asList(args));
        }

        process_name_array.trimToSize();

        if (useStartUpLog) {

            // Print the java runtime version
            bw.write("Java runtime version: " + System.getProperty("java.runtime.version") + System.getProperty("line.separator"));

            // print the command to the log file
            System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "Command line: ");
            bw.write(System.getProperty("line.separator") + "Command line: " + System.getProperty("line.separator"));

            for (String processEntry : process_name_array) {
                System.out.print(processEntry + " ");
                bw.write(processEntry + " ");
            }

            bw.write(System.getProperty("line.separator"));
            System.out.println(System.getProperty("line.separator"));

            bw.flush();

        }

        ProcessBuilder pb = new ProcessBuilder(process_name_array);

        // try to run the command line
        try {
            Process p = pb.start();
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
            Thread errorThread = (new Thread(errorGobbler));
            StreamGobbler inputGobbler = new StreamGobbler(p.getInputStream());
            Thread inputThread = (new Thread(inputGobbler));
            errorThread.start();
            inputThread.start();

            int exitValue = p.waitFor();

            errorGobbler.setContinueReading(true);
            inputGobbler.setContinueReading(true);

            if (useStartUpLog) {
                System.out.println("Process exitValue: " + exitValue + System.getProperty("line.separator"));
                bw.write("Process exitValue: " + exitValue + System.getProperty("line.separator"));
            }

            // an error occured
            if (exitValue != 0) {

                firstTry = false;
                String errorMessage = errorGobbler.getMessages().toLowerCase();
                String inputMessage = inputGobbler.getMessages().toLowerCase();

                // if needed, try re-launching with reduced memory settings
                if (errorMessage.contains("could not create the java virtual machine") || inputMessage.contains("could not reserve enough space")) {

                    if (userParameters.getMemoryParameter() > 4 * 1024) {

                        userParameters.setMemoryParameter(userParameters.getMemoryParameter() / 2);
                        UtilitiesUserParameters.saveUserParameters(userParameters);
                        launch(jarFile, splashName, mainClass, args, bw);

                    } else if (userParameters.getMemoryParameter() > 2 * 1024) {

                        userParameters.setMemoryParameter(userParameters.getMemoryParameter() - 1024);
                        UtilitiesUserParameters.saveUserParameters(userParameters);
                        launch(jarFile, splashName, mainClass, args, bw);

                    } else if (userParameters.getMemoryParameter() > 1024) {

                        userParameters.setMemoryParameter(userParameters.getMemoryParameter() - 512);
                        UtilitiesUserParameters.saveUserParameters(userParameters);
                        launch(jarFile, splashName, mainClass, args, bw);

                    } else if (userParameters.getMemoryParameter() <= 1024) {

                        userParameters.setMemoryParameter(800); // one last desperate try!
                        UtilitiesUserParameters.saveUserParameters(userParameters);
                        launch(jarFile, splashName, mainClass, args, bw);

                    } else {

                        if (useStartUpLog) {
                            bw.write("Memory Limit: " + userParameters.getMemoryParameter() + System.getProperty("line.separator"));
                            bw.close();
                        }

                        javax.swing.JOptionPane.showMessageDialog(null,
                                JOptionEditorPane.getJOptionEditorPane("Failed to create the Java virtual machine.<br><br>"
                                        + "Inspect the log file for details: resources/conf/startup.log.<br><br>"
                                        + "Then go to <a href=\"https://compomics.github.io/projects/compomics-utilities/wiki/JavaTroubleShooting.html\">Java TroubleShooting</a>."),
                                "Startup Failed", JOptionPane.ERROR_MESSAGE);

                        System.exit(0);

                    }

                } else if (errorMessage.toLowerCase().contains("cgcontextgetctm: invalid context")
                        || errorMessage.toLowerCase().contains("cgcontextsetbasectm: invalid context")) {

                    System.out.println("Mac OS/Java error (can be ignored): " + errorMessage);

                } else {

                    if (errorMessage.lastIndexOf("noclassdeffound") != -1) {

                        JOptionPane.showMessageDialog(null,
                                "It seems like you are trying to start the tool from within a zip file!",
                                "Startup Failed", JOptionPane.ERROR_MESSAGE);

                    } else {

                        System.out.println("Unknown error: " + errorMessage);

                        javax.swing.JOptionPane.showMessageDialog(null,
                                JOptionEditorPane.getJOptionEditorPane("An error occurred when starting the tool.<br><br>"
                                        + "Inspect the log file for details: resources/conf/startup.log.<br><br>"
                                        + "Then go to <a href=\"https://compomics.github.io/projects/compomics-utilities/wiki/JavaTroubleShooting.html\">Java TroubleShooting</a>."),
                                "Startup Error", JOptionPane.ERROR_MESSAGE);

                    }

                    if (useStartUpLog) {
                        bw.write(errorMessage);
                        bw.write(inputMessage);
                        bw.close();
                    }

                    System.exit(0);
                }

            } else {

                if (useStartUpLog && bw != null) {
                    bw.close();
                }

                System.exit(0);
            }

        } catch (Throwable t) {

            if (useStartUpLog && bw != null) {
                bw.write(t.getMessage());
                bw.close();
            }

            t.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Returns the path to the jar file. Verifies that the path exists and tries
     * to decode from Unicode if not.
     *
     * @param classPath the class path to extract the jar file path from
     * @param toolName the name of the tool, e.g., "PeptideShaker" or
     * "SearchGUI".
     * @return the path to the jar file
     */
    public static String getJarFilePath(String classPath, String toolName) {

        String path = classPath;
        toolName = toolName + "-";

        if (path.lastIndexOf("/" + toolName) != -1) {

            // remove starting 'file:' tag if there
            if (path.startsWith("file:")) {
                path = path.substring("file:".length(), path.lastIndexOf("/" + toolName));
            } else {
                path = path.substring(0, path.lastIndexOf("/" + toolName));
            }

            path = path.replace("%20", " ");
            path = path.replace("%5b", "[");
            path = path.replace("%5d", "]");

            if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
                path = path.replace("/", "\\");
            }

        } else {
            path = ".";
        }

        // try to decode the path to fix any special characters
        try {

            if (!new File(path).exists()) {
                path = URLDecoder.decode(path, "UTF-8");
            }

            if (!new File(path).exists()) {
                System.out.println(path + " not found!");
                FileNotFoundException ex = new FileNotFoundException(path + " not found!");
                ex.printStackTrace();
            }

        } catch (UnsupportedEncodingException ex) {
            System.out.println("Error reading file " + path + "!");
            ex.printStackTrace();
        }

        return path;
    }

    /**
     * Returns if the Java version used is 64 bit.
     *
     * @return true if the Java version is 64 bit
     */
    public static boolean is64BitJava() {
        String arch = System.getProperty("os.arch");
        return !arch.endsWith("x86");
    }

    /**
     * Checks if the user is running a supported version of Java, shows a
     * warning if not and links to the JavaTroubleShooting page.
     *
     * @param toolName the name of the tool, e.g., "PeptideShaker"
     */
    public static void checkJavaVersion(String toolName) {

        // check java version
        String javaVersion = System.getProperty("java.runtime.version");

        int javaVersionNumber;

        if (javaVersion.contains(".")) {
            javaVersionNumber = Integer.parseInt(javaVersion.substring(0, javaVersion.indexOf(".")));
        } else if (javaVersion.contains("+")) {
            javaVersionNumber = Integer.parseInt(javaVersion.substring(0, javaVersion.indexOf("+")));
        } else {
            javaVersionNumber = Integer.parseInt(javaVersion);
        }

        if (javaVersionNumber < 11) {

            // create an empty label to put the message in
            JLabel label = new JLabel();

            // html content 
            JEditorPane ep = new JEditorPane(
                    "text/html", "<html><body bgcolor=\"#"
                    + Util.color2Hex(label.getBackground()) + "\">"
                    + "Running with Java version "
                    + javaVersion
                    + ", "
                    + toolName + " works best with Java >= 11.<br><br>"
                    + "See <a href=\"https://compomics.github.io/projects/compomics-utilities/wiki/JavaTroubleShooting.html\">Java Troubleshooting</a> for more details."
                    + "</body></html>");

            // handle link events 
            ep.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        BareBonesBrowserLaunch.openURL(e.getURL().toString());
                    }
                }
            });

            ep.setBorder(null);
            ep.setEditable(false);

            JOptionPane.showMessageDialog(
                    null,
                    ep,
                    "Update Java?",
                    JOptionPane.INFORMATION_MESSAGE
            );

        }

        if (javaVersionNumber == 17) {

            // create an empty label to put the message in
            JLabel label = new JLabel();

            // html content 
            JEditorPane ep = new JEditorPane(
                    "text/html", "<html><body bgcolor=\"#"
                    + Util.color2Hex(label.getBackground())
                    + "\">"
                    + "Running with Java version "
                    + javaVersion
                    + ", issues with Java 17 have been reported when using "
                    + toolName
                    + ", we recommend updating to Java 18.<br><br>"
                    + "See <a href=\"https://compomics.github.io/projects/compomics-utilities/wiki/JavaTroubleShooting.html\">Java Troubleshooting</a> for more details."
                    + "</body></html>");

            // handle link events 
            ep.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        BareBonesBrowserLaunch.openURL(e.getURL().toString());
                    }
                }
            });

            ep.setBorder(null);
            ep.setEditable(false);

            JOptionPane.showMessageDialog(null, ep, "Update Java?", JOptionPane.INFORMATION_MESSAGE);

        }

        // check for 64 bit
        String arch = System.getProperty("os.arch");

        if (arch.endsWith("x86")) {

            // create an empty label to put the message in
            JLabel label = new JLabel();

            // html content 
            JEditorPane ep = new JEditorPane(
                    "text/html", "<html><body bgcolor=\"#"
                    + Util.color2Hex(label.getBackground())
                    + "\">"
                    + toolName
                    + " works best with Java 64 bit.<br><br>"
                    + "See <a href=\"https://compomics.github.io/projects/compomics-utilities/wiki/JavaTroubleShooting.html\">Java Troubleshooting</a> for more details."
                    + "</body></html>");

            // handle link events 
            ep.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        BareBonesBrowserLaunch.openURL(e.getURL().toString());
                    }
                }
            });

            ep.setBorder(null);
            ep.setEditable(false);

            JOptionPane.showMessageDialog(
                    null,
                    ep,
                    "Java 64 Bit?",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Check if a newer version of the tool is deployed in the Maven repository,
     * and closes the tool if the user decided to update.
     *
     * @param toolName the name of the tool, e.g., PeptideShaker or SearchGUI
     * @param oldMavenJarFile the old jar file
     * @param jarRepository the Maven repository
     * @param iconName the icon name
     * @param deleteOldFiles if true, the old version of the tool is tried
     * deleted
     * @param startDownloadedVersion if true, the new version is started when
     * downloaded
     * @param addDesktopIcon if true, a desktop icon is added
     * @param normalIcon the normal icon for the progress dialog
     * @param waitingIcon the waiting icon for the progress dialog
     * @param exitJavaOnCancel if true, the JVM will be shut down if the update
     * is canceled by the user before it has started or when the update is done
     * @return true if a new version is to be downloaded
     */
    public static boolean checkForNewDeployedVersion(
            final String toolName,
            final MavenJarFile oldMavenJarFile,
            final URL jarRepository,
            final String iconName,
            final boolean deleteOldFiles,
            final boolean startDownloadedVersion,
            final boolean addDesktopIcon,
            Image normalIcon,
            Image waitingIcon,
            final boolean exitJavaOnCancel
    ) {

        boolean update = false;

        try {
            // check if a new version is available
            if (WebDAO.newVersionReleased(oldMavenJarFile, jarRepository)) {

                int option = JOptionPane.showConfirmDialog(null,
                        "A newer version of " + toolName + " is available.\n"
                        + "Do you want to update?",
                        "Update Available",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if (option == JOptionPane.YES_OPTION) {

                    update = true;

                } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {

                    if (exitJavaOnCancel) {
                        System.exit(0);
                    }

                }
            }

            // download the new version
            if (update) {

                progressDialog = new ProgressDialogX(
                        new JFrame(),
                        normalIcon,
                        waitingIcon,
                        true
                );
                progressDialog.setPrimaryProgressCounterIndeterminate(true);
                progressDialog.setTitle("Updating " + toolName + ". Please Wait...");

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            progressDialog.setVisible(true);
                        } catch (IndexOutOfBoundsException e) {
                            // ignore
                        }
                    }
                }, "ProgressDialog").start();

                new Thread("DisplayThread") {
                    @Override
                    public void run() {
                        try {

                            downloadLatestZipFromRepo(
                                    oldMavenJarFile.getJarPath().toURL(),
                                    toolName,
                                    deleteOldFiles,
                                    iconName,
                                    null,
                                    jarRepository,
                                    startDownloadedVersion,
                                    addDesktopIcon,
                                    progressDialog
                            );

                            if (!progressDialog.isRunFinished()) {
                                progressDialog.setRunFinished();
                            }

                            // incrementing the counter for a PeptideShaker or SearchGUI update 
                            if (toolName.equals("PeptideShaker")) {
                                Util.sendGAUpdate("UA-36198780-1", "update", "peptide-shaker");
                            } else if (toolName.equals("SearchGUI")) {
                                Util.sendGAUpdate("UA-36198780-2", "update", "searchgui");
                            }

                            if (exitJavaOnCancel) {
                                System.exit(0);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            if (!progressDialog.isRunFinished()) {
                                progressDialog.setRunFinished();
                            }
                            JOptionPane.showMessageDialog(
                                    null,
                                    "An error occured when trying to update "
                                    + toolName
                                    + ":\n"
                                    + e.getMessage(),
                                    "Update Failed",
                                    JOptionPane.WARNING_MESSAGE
                            );

                            System.exit(0);
                        }
                    }
                }.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return update;
    }

    /**
     * Check if a newer version of the tool is deployed in the Maven repository,
     * and closes the tool if the user decided to update.
     *
     * @param toolName the name of the tool, e.g., PeptideShaker or SearchGUI
     * @param groupId the group id
     * @param artifactId the artifact id
     * @param downloadFolder the folder to download to
     * @param jarRepository the Maven repository
     * @param iconName the icon name
     * @param startDownloadedVersion if true, the new version is started when
     * downloaded
     * @param addDesktopIcon if true, a desktop icon is added
     * @param normalIcon the normal icon for the progress dialog
     * @param waitingIcon the waiting icon for the progress dialog
     * @param exitJavaOnCancel if true, the JVM will be shut down if the update
     * is canceled by the user before it has started or when the update is done
     * @return true if a new version is to be downloaded
     */
    public static boolean downloadLatestVersion(
            final String toolName,
            final String groupId,
            final String artifactId,
            final File downloadFolder,
            final URL jarRepository,
            final String iconName,
            final boolean startDownloadedVersion,
            final boolean addDesktopIcon,
            Image normalIcon,
            Image waitingIcon,
            final boolean exitJavaOnCancel
    ) {

        boolean update = false;

        // download the latest version
        progressDialog = new ProgressDialogX(
                new JFrame(),
                normalIcon,
                waitingIcon,
                true
        );

        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Downloading " + toolName + ". Please Wait...");

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("DisplayThread") {
            @Override
            public void run() {
                try {
                    downloadLatestZipFromRepo(
                            downloadFolder,
                            toolName,
                            groupId,
                            artifactId,
                            iconName,
                            null,
                            jarRepository,
                            startDownloadedVersion,
                            addDesktopIcon,
                            new GUIFileDAO(),
                            progressDialog
                    );

                    if (!progressDialog.isRunFinished()) {
                        progressDialog.setRunFinished();
                    }
                    if (exitJavaOnCancel) {
                        System.exit(0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }

        }.start();

        return update;
    }

    /**
     * Check if the application with name condaAppName is running in a Conda
     * environment.
     *
     * @param condaAppName name of the executable in the Conda environment
     * @return true if condaAppName is running in a Conda environment
     */
    public static boolean appRunningIntoConda(String condaAppName) {

        String someCondaEnvironmentActivated = System.getenv().get("CONDA_DEFAULT_ENV");
        boolean running = false;

        if (someCondaEnvironmentActivated != null) {

            ProcessBuilder processBuilder = new ProcessBuilder();

            // bash is only available in Linux and Mac, but, as Bioconda itself
            // does not support Windows, returning false in windows is adequate too
            processBuilder.command("bash", "-c", "which " + condaAppName);

            try {
                Process process = processBuilder.start();
                int exitVal = process.waitFor();

                // returns 0 only if condaAppName exists
                if (exitVal == 0) {
                    running = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return running;
    }

    /**
     * Get the Java options.
     *
     * @param confFolder the conf folder for the project
     * @param bw a buffered writer for log and errors, can be null
     * @return the options to the Java Virtual Machine
     * @throws IOException
     */
    private ArrayList<String> getJavaOptions(
            File confFolder,
            BufferedWriter bw
    ) throws IOException {

        try {
            userParameters = UtilitiesUserParameters.loadUserParameters();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> options = new ArrayList<>();

        File javaOptions = new File(confFolder, "JavaOptions.txt");
        File uniprotApiPropertiesFile = new File(confFolder, "proxy/uniprotjapi.properties");
        String uniprotApiProperties = "";

        // read any java option settings
        if (javaOptions.exists()) {

            try {
                FileReader f = new FileReader(javaOptions);
                BufferedReader b = new BufferedReader(f);

                String currentOption = b.readLine();

                while (currentOption != null) {

                    if (currentOption.startsWith("-Xmx")) {

                        if (firstTry) {

                            currentOption = currentOption.substring(4, currentOption.length() - 1); // @TODO: what about GB, e.g., 6G..?
                            boolean input = false;

                            for (char c : currentOption.toCharArray()) {
                                if (c != '*') {
                                    input = true;
                                    break;
                                }
                            }

                            if (input) {

                                try {
                                    userParameters.setMemoryParameter(Integer.valueOf(currentOption));
                                    UtilitiesUserParameters.saveUserParameters(userParameters);

                                    if (bw != null) {
                                        bw.write(
                                                "New memory setting saved: "
                                                + userParameters.getMemoryParameter()
                                                + System.getProperty("line.separator")
                                        );
                                    }

                                } catch (Exception e) {

                                    javax.swing.JOptionPane.showMessageDialog(
                                            null,
                                            "Could not parse the memory setting: "
                                            + currentOption
                                            + ". The value was reset to"
                                            + userParameters.getMemoryParameter()
                                            + ".",
                                            "Wrong memory settings",
                                            JOptionPane.WARNING_MESSAGE
                                    );

                                }
                            }
                        }

                    } else if (!currentOption.startsWith("#")) {

                        // extract the proxy settings as these are needed for uniprotjapi.properties
                        if (currentOption.startsWith("-Dhttp")) {

                            proxySettingsFound = true;
                            String[] tempProxySetting = currentOption.split("=");

                            if (tempProxySetting[0].equalsIgnoreCase("-Dhttp.proxyHost")) { // proxy host
                                uniprotApiProperties += "proxy.host=" + tempProxySetting[1] + System.getProperty("line.separator");
                            } else if (tempProxySetting[0].equalsIgnoreCase("-Dhttp.proxyPort")) { // proxy port
                                uniprotApiProperties += "proxy.port=" + tempProxySetting[1] + System.getProperty("line.separator");
                            } else if (tempProxySetting[0].equalsIgnoreCase("-Dhttp.proxyUser")) { // proxy user name
                                uniprotApiProperties += "username=" + tempProxySetting[1] + System.getProperty("line.separator");
                            } else if (tempProxySetting[0].equalsIgnoreCase("-Dhttp.proxyPassword")) { // proxy password
                                uniprotApiProperties += "password=" + tempProxySetting[1] + System.getProperty("line.separator");
                            }
                        }

                        options.add(currentOption.trim());
                    }

                    currentOption = b.readLine();
                }

                // create the uniprot japi proxy settings file
                if (proxySettingsFound) {

                    FileWriter uniprotProxyWriter = new FileWriter(uniprotApiPropertiesFile);
                    BufferedWriter uniprotProxyBufferedWriter = new BufferedWriter(uniprotProxyWriter);
                    uniprotProxyBufferedWriter.write(uniprotApiProperties);
                    uniprotProxyBufferedWriter.close();
                    uniprotProxyWriter.close();

                }

                b.close();
                f.close();

                options.add("-Xmx" + userParameters.getMemoryParameter() + "M"); // @TODO: should also support GB?

            } catch (FileNotFoundException ex) {

                ex.printStackTrace();
                if (bw != null) {
                    bw.write(ex.getMessage());
                }

            } catch (IOException ex) {

                ex.printStackTrace();
                if (bw != null) {
                    bw.write(ex.getMessage());
                }

            }
        } else {

            options.add("-Xms128M");
            options.add("-Xmx1024M");

        }

        return options;
    }

    /**
     * Returns the Java home.
     *
     * @param confFolder the conf folder
     * @param bw a buffered writer for log and errors, can be null
     *
     * @return the Java home
     * @throws IOException
     */
    private String getJavaHome(
            File confFolder,
            BufferedWriter bw)
            throws IOException {

        boolean usingStandardJavaHome = true;

        // get the default java home location
        String javaHome = System.getProperty("java.home") + File.separator + "bin" + File.separator;

        // get the user set java home
        File nonStandardJavaHome = new File(confFolder, "JavaHome.txt");

        if (nonStandardJavaHome.exists()) {

            try {
                FileReader f = new FileReader(nonStandardJavaHome);
                BufferedReader b = new BufferedReader(f);

                String tempLocation = b.readLine();

                // add an ending slash if not there
                if (!tempLocation.endsWith(File.separator)) {
                    tempLocation += File.separator;
                }

                if (new File(tempLocation).exists()
                        && (new File(tempLocation, "java.exe").exists() || new File(tempLocation, "java").exists())) {

                    javaHome = tempLocation;
                    usingStandardJavaHome = false;

                } else if (firstTry) {

                    JOptionPane.showMessageDialog(
                            null,
                            "Non-standard Java home location not found.\n"
                            + "Using default Java home.",
                            "Java Home Not Found!",
                            JOptionPane.WARNING_MESSAGE
                    );

                }

                b.close();
                f.close();

            } catch (FileNotFoundException ex) {

                if (firstTry) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                            null,
                            "Non-standard Java home location not found.\n"
                            + "Using default Java home",
                            "Java Home Not Found!",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

            } catch (IOException ex) {

                if (firstTry) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                            null,
                            "Error when reading non-standard Java home location.\n"
                            + "Using default Java home.",
                            "Java Home Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

            }
        } else {

            try {
                userParameters = UtilitiesUserParameters.loadUserParameters();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // see if the user has set the java home via the gui
            if (userParameters.getJavaHome() != null
                    && new File(userParameters.getJavaHome()).exists()
                    && (new File(userParameters.getJavaHome(), "java.exe").exists()
                    || new File(userParameters.getJavaHome(), "java").exists())) {
                javaHome = userParameters.getJavaHome();
                usingStandardJavaHome = false;
            }
        }

        if (bw != null) {
            bw.write("original java.home: " + javaHome + System.getProperty("line.separator"));
        }

        // @TODO: should rather run java -version!!!
        // try to force the use of 64 bit Java if available
        if (usingStandardJavaHome && javaHome.lastIndexOf(" (x86)") != -1
                && System.getProperty("os.name").lastIndexOf("Windows") != -1) {

            // @TODO: add similar tests for Mac and Linux...
            // default java 32 bit windows home looks like this:    C:\Program Files (x86)\Java\jre6\bin\javaw.exe
            // default java 64 bit windows home looks like this:    C:\Program Files\Java\jre6\bin\javaw.exe
            String tempJavaHome = javaHome.replaceAll(" \\(x86\\)", "");

            if (bw != null) {
                bw.write("temp java.home: " + tempJavaHome + System.getProperty("line.separator"));
            }

            // @TODO: replace this simple test and rather do the real test below instead. otherwise we would always default to an old 64 bit version of using an old 32 bit version initially...
            if (new File(tempJavaHome).exists()) {
                javaHome = tempJavaHome;
            } else {
                // @TODO: code below needs more testing!!
//                // try to find the newest 64 bit Java version
//                File defaultJavaHome64Bit = new File("C:\\Program Files\\Java");
//
//                // check if the default java 64 bit folder exists
//                if (defaultJavaHome64Bit.exists() && defaultJavaHome64Bit.listFiles().length > 0) {
//
//                    File[] javaFolders = defaultJavaHome64Bit.listFiles();
//                    ArrayList<File> possibleJavaFolders = new ArrayList<File>();
//
//                    // find the valid java folders
//                    for (File tempFile : javaFolders) {
//                        if (tempFile.isDirectory() && (tempFile.getName().startsWith("jre") || tempFile.getName().startsWith("jdk"))) {
//                            if (new File(tempFile, "bin/java.exe").exists()) {
//                                possibleJavaFolders.add(tempFile);
//                            }
//                        }
//                    }
//
//                    if (!possibleJavaFolders.isEmpty()) {
//
//                        File newestJavaFolder = possibleJavaFolders.get(0);
//                        
//                        
//                        // @TODO: start the tool and get the version numbe from the command line
//                        
//                        String newestVersionNumber = newestJavaFolder.getName().substring(3, newestJavaFolder.getName().length());
//                        CompareVersionNumbers versionComparator = new CompareVersionNumbers();
//
//                        // iterate the versions and find the most recent one
//                        for (int i = 1; i < possibleJavaFolders.size(); i++) {
//
//                            File tempJavaFolder = possibleJavaFolders.get(i);
//                            
//                            // @TODO: start the tool and get the version numbe from the command line
//                            
//                            String tempVersionNumber = tempJavaFolder.getName().substring(3, tempJavaFolder.getName().length());
//
//                            if (versionComparator.compare(newestVersionNumber, tempVersionNumber) == 1) {
//                                newestVersionNumber = tempVersionNumber;
//                                newestJavaFolder = tempJavaFolder;
//                            }
//                        }
//
//                        javaHome = new File(newestJavaFolder, "bin").getAbsolutePath();
//
//                        // add an ending slash if not there
//                        if (!javaHome.endsWith(File.separator)) {
//                            javaHome += File.separator;
//                        }
//                    }
//                }
            }
        }

        if (bw != null) {
            bw.write("new java.home: " + javaHome + System.getProperty("line.separator"));
        }

        // set up the quote type, windows or linux/mac
        String quote = CommandLineUtils.getQuoteType();

        javaHome = quote + javaHome + "java" + quote;

        return javaHome;
    }

    /**
     * Returns an array list containing the Java home plus any parameters to the
     * JVM. The fist index in the list will always contain the Java home. Note
     * that this method assumes that the tool has folder called resources/conf
     * in the same folder as the jar file.
     *
     * @param toolPath the path to the jar file of the tool
     * @return an array list containing the Java home plus any parameters to the
     * JVM
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public ArrayList<String> getJavaHomeAndOptions(String toolPath)
            throws FileNotFoundException, IOException, ClassNotFoundException {

        ArrayList<String> javaHomeAndOptions = new ArrayList<>();

        CompomicsWrapper wrapper = new CompomicsWrapper();
        File confFolder = new File(new File(toolPath).getParentFile(), "resources/conf");

        if (!confFolder.exists()) {
            String path = URLDecoder.decode(new File(toolPath).getParentFile().getAbsolutePath(), "UTF-8");
            confFolder = new File(path, "resources/conf");
        }

        String javaHome = wrapper.getJavaHome(confFolder, null);
        javaHomeAndOptions.add(javaHome);

        ArrayList<String> optionsAsList = wrapper.getJavaOptions(confFolder, null);

        for (String tempOption : optionsAsList) {
            javaHomeAndOptions.add(tempOption);
        }

        return javaHomeAndOptions;
    }

    /**
     * Copy the default JavaOptions file to the given location.
     *
     * @param folder the folder to copy the file to
     * @throws IOException if an IOException occurs
     */
    private void copyDefaultJavaOptionsFile(File folder) throws IOException {

        File destination = new File(folder.getAbsolutePath() + "/JavaOptions.txt");
        InputStream resStreamIn = getClass().getClassLoader().getResourceAsStream("DefaultJavaOptions.txt");
        OutputStream resStreamOut = new FileOutputStream(destination);

        try {
            int readBytes;
            byte[] buffer = new byte[1024];
            while ((readBytes = resStreamIn.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } finally {
            resStreamIn.close();
            resStreamOut.close();
        }
    }
}
