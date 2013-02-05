package com.compomics.software;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.gui.UtilitiesGUIDefaults;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.*;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * A general wrapper for compomics tools. All tools shall contain a
 * resources/conf folder. In it a JavaOptions.txt and splash screen. Eventually
 * JavaHome.txt and proxy/uniprotjapi.properties.
 *
 * @author Marc Vaudel
 */
public class CompomicsWrapper {

    /**
     * If set to true debug output will be written to the screen and to
     * startup.log.
     */
    private boolean useStartUpLog = true;
    /**
     * Writes the debug output to startup.log.
     */
    private BufferedWriter bw = null;
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
    private UtilitiesUserPreferences userPreferences;

    /**
     * Constructor.
     */
    public CompomicsWrapper() {
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param toolName
     * @param jarFile the jar file to execute
     * @param splashName the splash name, for example peptide-shaker-splash.png
     * @param mainClass the main class to execute, for example
     * eu.isas.peptideshaker.gui.PeptideShakerGUI
     */
    public void launchTool(String toolName, File jarFile, String splashName, String mainClass) {
        launchTool(toolName, jarFile, splashName, mainClass, null);
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param toolName
     * @param jarFile the jar file to execute
     * @param splashName the splash name, for example peptide-shaker-splash.png
     * @param mainClass the main class to execute, for example
     * eu.isas.peptideshaker.gui.PeptideShakerGUI
     * @param args the arguments to pass to the tool (ignored if null)
     */
    public void launchTool(String toolName, File jarFile, String splashName, String mainClass, String[] args) {

        try {
            try {
                userPreferences = UtilitiesUserPreferences.loadUserPreferences();
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
                    throw new FileNotFoundException(folder.getAbsolutePath() + " not found!");
                }
                File debugOutput = new File(folder, "startup.log");
                bw = new BufferedWriter(new FileWriter(debugOutput));
                bw.write("Memory settings read from the user preferences: " + userPreferences.getMemoryPreference() + System.getProperty("line.separator"));
                String arguments = "args: ";
                for (String arg : args) {
                    arguments += arg + " ";
                }
                bw.write(arguments);
                bw.newLine();
            }

            try {
                UtilitiesGUIDefaults.setLookAndFeel();
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                // perhaps not the optimal way of catching this error, but seems to work
                JOptionPane.showMessageDialog(null,
                        "Seems like you are trying to start " + toolName + " from within a zip file!",
                        toolName + " - Startup Failed", JOptionPane.ERROR_MESSAGE);
            }
            launch(jarFile, splashName, mainClass, args);

            if (useStartUpLog) {
                bw.flush();
                bw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "Failed to start " + toolName + ":" + System.getProperty("line.separator")
                    + e.getMessage(),
                    toolName + " - Startup Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Launches the jar file with parameters to the jvm.
     *
     * @throws java.lang.Exception
     * @param jarFile the jar file to execute
     * @param splashName the splash name, for example peptide-shaker-splash.png
     * @param mainClass the main class to execute, for example
     * eu.isas.peptideshaker.gui.PeptideShakerGUI
     */
    private void launch(File jarFile, String splashName, String mainClass) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        launch(jarFile, splashName, mainClass, null);
    }

    /**
     * Launches the jar file with parameters to the jvm.
     *
     * @throws java.lang.Exception
     * @param jarFile the jar file to execute
     * @param splashName the splash name, for example peptide-shaker-splash.png
     * @param mainClass the main class to execute, for example
     * eu.isas.peptideshaker.gui.PeptideShakerGUI
     * @param args the arguments to pass to the tool (ignored if null)
     */
    private void launch(File jarFile, String splashName, String mainClass, String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {

        String temp = "", cmdLine;
        String arguments = "";
        if (args != null) {
            arguments = CommandLineUtils.concatenate(args);
        }

        File confFolder = new File(jarFile.getParentFile(), "resources/conf");
        if (!confFolder.exists()) {
            String path = URLDecoder.decode(jarFile.getParentFile().getAbsolutePath(), "UTF-8");
            confFolder = new File(path, "resources/conf");
        }
        if (!confFolder.exists()) {
            throw new FileNotFoundException(confFolder.getAbsolutePath() + " not found!");
        }

        // get the java options
        ArrayList<String> optionsAsList = getJavaOptions(confFolder, jarFile, bw);
        String options = "";
        for (String currentOption : optionsAsList) {
            options += currentOption + " ";
        }

        // check if the user has set a non-standard Java home location
        String javaHome = getJavaHome(confFolder, bw);

        // get the splash 
        String splashPath = confFolder.getAbsolutePath() + File.separator + splashName;

        // set the correct slashes for the splash path
        if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
            splashPath = splashPath.replace("/", "\\");

            // remove the initial '\' at the start of the line 
            if (splashPath.startsWith("\\") && !splashPath.startsWith("\\\\")) {
                splashPath = splashPath.substring(1);
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

        // create the complete command line
        cmdLine = javaHome + " -splash:" + quote + splashPath + quote + " " + options + " -cp "
                + quote + jarFilePath + quote + uniprotProxyClassPath
                + " " + mainClass + " " + arguments;

        if (useStartUpLog) {
            System.out.println(System.getProperty("line.separator") + cmdLine + System.getProperty("line.separator") + System.getProperty("line.separator"));
            bw.write(System.getProperty("line.separator") + "Command line: " + cmdLine + System.getProperty("line.separator") + System.getProperty("line.separator"));
        }

        // try to run the command line
        try {
            Process p = Runtime.getRuntime().exec(cmdLine);

            InputStream stderr = p.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);

            String line = br.readLine();

            boolean error = false;

            while (line != null) {

                if (useStartUpLog) {
                    System.out.println(line);
                    bw.write(line + System.getProperty("line.separator"));
                }

                temp += line + System.getProperty("line.separator");
                line = br.readLine();
                error = true;
            }

            int exitVal = p.waitFor();

            if (useStartUpLog) {
                System.out.println("Process exitValue: " + exitVal);
                bw.write("Process exitValue: " + exitVal + System.getProperty("line.separator"));
            }

            // an error occured
            if (error) {

                firstTry = false;
                temp = temp.toLowerCase();

                // if needed, try re-launching with reduced memory settings
                if (temp.contains("could not create the java virtual machine")) {
                    if (userPreferences.getMemoryPreference() > 3 * 1024) {
                        userPreferences.setMemoryPreference(userPreferences.getMemoryPreference() - 1024);
                        saveNewSettings(jarFile);
                        launch(jarFile, splashName, mainClass, args);
                    } else if (userPreferences.getMemoryPreference() > 1024) {
                        userPreferences.setMemoryPreference(userPreferences.getMemoryPreference() - 512);
                        saveNewSettings(jarFile);
                        launch(jarFile, splashName, mainClass, args);
                    } else {
                        if (useStartUpLog) {
                            bw.write("Memory Limit:" + userPreferences.getMemoryPreference() + System.getProperty("line.separator"));
                            bw.flush();
                            bw.close();
                        }

                        javax.swing.JOptionPane.showMessageDialog(null,
                                "Failed to create the Java virtual machine.\n\n"
                                + "Inspect the log file for details: resources/conf/startup.log.\n\n"
                                + "Then go to Troubleshooting at http://peptide-shaker.googlecode.com", // @TODO: move help to tool independent website
                                "Startup Failed", JOptionPane.ERROR_MESSAGE);

                        System.exit(0);
                    }
                } else if (temp.toLowerCase().contains("cgcontextgetctm: invalid context") || temp.toLowerCase().contains("cgcontextsetbasectm: invalid context")) {
                    System.out.println("Mac OS/Java error (can be ignored): " + temp);
                } else {

                    if (useStartUpLog) {
                        bw.flush();
                        bw.close();
                    }

                    if (temp.lastIndexOf("noclassdeffound") != -1) {
                        JOptionPane.showMessageDialog(null,
                                "Seems like you are trying to start the tool from within a zip file!",
                                "Startup Failed", JOptionPane.ERROR_MESSAGE);
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(null,
                                "An error occurred when starting the tool.\n\n"
                                + "Inspect the log file for details: resources/conf/startup.log.\n\n"
                                + "Then go to Troubleshooting at http://peptide-shaker.googlecode.com", // @TODO: move help to tool independent website
                                "Startup Error", JOptionPane.ERROR_MESSAGE);
                    }

                    System.exit(0);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Saves the new memory settings.
     */
    private void saveNewSettings(File jarFile) throws FileNotFoundException, UnsupportedEncodingException {
        try {
            UtilitiesUserPreferences.saveUserPreferences(userPreferences);
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveJavaOptions(jarFile);
    }

    /**
     * Creates a new javaOptions text file with the new settings.
     */
    private void saveJavaOptions(File jarFile) throws FileNotFoundException, UnsupportedEncodingException {

        String currentLine, lines = "";

        File confFolder = new File(jarFile.getParentFile(), "resources/conf");
        if (!confFolder.exists()) {
            String path = URLDecoder.decode(jarFile.getParentFile().getAbsolutePath(), "UTF-8");
            confFolder = new File(path, "resources/conf");
        }
        if (!confFolder.exists()) {
            throw new FileNotFoundException(confFolder.getAbsolutePath() + " not found!");
        }
        File javaOptions = new File(confFolder, "JavaOptions.txt");

        // read any java option settings
        if (javaOptions.exists()) {

            try {
                FileReader f = new FileReader(javaOptions);
                BufferedReader b = new BufferedReader(f);

                while ((currentLine = b.readLine()) != null) {
                    if (!currentLine.startsWith("-Xmx")) {
                        lines += currentLine + System.getProperty("line.separator");
                    }
                }
                b.close();
                f.close();

                FileWriter fw = new FileWriter(javaOptions);
                BufferedWriter bow = new BufferedWriter(fw);
                bow.write(lines);
                bow.write("-Xmx" + userPreferences.getMemoryPreference() + "M" + System.getProperty("line.separator"));

                bow.close();
                fw.close();

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
                JOptionPane.showMessageDialog(null, path + " not found!", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (UnsupportedEncodingException ex) {
            JOptionPane.showMessageDialog(null, "Error reading file " + path + ".", "File Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        return path;
    }

    /**
     * Check if a newer version of the tool is available on GoogleCode.
     *
     * @param currentVersion the version number of the tool currently running
     * @param toolName the name of the tool, e.g., "PeptideShaker"
     * @param googleCodeToolName the GoogleCode name of the tool, e.g.,
     * "peptide-shaker"
     */
    public static void checkForNewVersion(String currentVersion, String toolName, String googleCodeToolName) {

        try {
            boolean deprecatedOrDeleted = false;
            URL downloadPage = new URL(
                    "http://code.google.com/p/" + googleCodeToolName + "/downloads/detail?name=" + toolName + "-"
                    + currentVersion + ".zip");

            if ((java.net.HttpURLConnection) downloadPage.openConnection() != null) {

                int respons = ((java.net.HttpURLConnection) downloadPage.openConnection()).getResponseCode();

                // 404 means that the file no longer exists, which means that
                // the running version is no longer available for download,
                // which again means that a never version is available.
                if (respons == 404) {
                    deprecatedOrDeleted = true;
                } else {

                    // also need to check if the available running version has been
                    // deprecated (but not deleted)
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(downloadPage.openStream()));

                    String inputLine;

                    while ((inputLine = in.readLine()) != null && !deprecatedOrDeleted) {
                        if (inputLine.lastIndexOf("Deprecated") != -1
                                && inputLine.lastIndexOf("Deprecated Downloads") == -1
                                && inputLine.lastIndexOf("Deprecated downloads") == -1) {
                            deprecatedOrDeleted = true;
                        }
                    }

                    in.close();
                }

                // informs the user about an updated version of the tool, unless the user
                // is running a beta version
                if (deprecatedOrDeleted && currentVersion.lastIndexOf("beta") == -1
                        && currentVersion.lastIndexOf("${version}") == -1) {
                    int option = JOptionPane.showConfirmDialog(null,
                            "A newer version of " + toolName + " is available.\n"
                            + "Do you want to upgrade?",
                            "Upgrade Available",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        BareBonesBrowserLaunch.openURL("http://" + googleCodeToolName + ".googlecode.com/");
                        System.exit(0);
                    } else if (option == JOptionPane.CANCEL_OPTION) {
                        System.exit(0);
                    }
                }
            }
        } catch (UnknownHostException e) {
            // ignore exception
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the Java options.
     *
     * @param confFolder the conf folder for the project
     * @param jarFile the jar file for the project
     * @param bw a buffered writer for log and errors, can be null
     * @return the options to the Java Virtual Machine
     * @throws IOException
     */
    private ArrayList<String> getJavaOptions(File confFolder, File jarFile, BufferedWriter bw) throws IOException {

        try {
            userPreferences = UtilitiesUserPreferences.loadUserPreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> options = new ArrayList<String>();

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
                            currentOption = currentOption.substring(4, currentOption.length() - 1);
                            boolean input = false;
                            for (char c : currentOption.toCharArray()) {
                                if (c != '*') {
                                    input = true;
                                    break;
                                }
                            }
                            if (input) {
                                try {
                                    userPreferences.setMemoryPreference(new Integer(currentOption));
                                    saveNewSettings(jarFile);
                                    if (bw != null) {
                                        bw.write("New memory setting saved: " + userPreferences.getMemoryPreference() + System.getProperty("line.separator"));
                                    }
                                } catch (Exception e) {
                                    javax.swing.JOptionPane.showMessageDialog(null,
                                            "Could not parse the memory setting:" + currentOption
                                            + ". The value was reset to" + userPreferences.getMemoryPreference() + ".",
                                            "Wrong memory settings", JOptionPane.WARNING_MESSAGE);
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

                options.add("-Xmx" + userPreferences.getMemoryPreference() + "M");

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
    private String getJavaHome(File confFolder, BufferedWriter bw) throws IOException {

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

                if (new File(tempLocation).exists()
                        && (new File(tempLocation, "java.exe").exists() || new File(tempLocation, "java").exists())) {
                    javaHome = tempLocation;
                    usingStandardJavaHome = false;
                } else {
                    if (firstTry) {
                        JOptionPane.showMessageDialog(null, "Non-standard Java home location not found.\n"
                                + "Using default Java home.", "Java Home Not Found!", JOptionPane.WARNING_MESSAGE);
                    }
                }

                b.close();
                f.close();

            } catch (FileNotFoundException ex) {
                if (firstTry) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Non-standard Java home location not found.\n"
                            + "Using default Java home", "Java Home Not Found!", JOptionPane.WARNING_MESSAGE);
                }
            } catch (IOException ex) {
                if (firstTry) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error when reading non-standard Java home location.\n"
                            + "Using default Java home.", "Java Home Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        if (bw != null) {
            bw.write("original java.home: " + javaHome + System.getProperty("line.separator"));
        }

        // try to force the use of 64 bit Java if available
        if (usingStandardJavaHome && javaHome.lastIndexOf(" (x86)") != -1 && System.getProperty("os.name").lastIndexOf("Windows") != -1) {

            // default java 32 bit windows home looks like this:    C:\Program Files (x86)\Java\jre6\bin\javaw.exe
            // default java 64 bit windows home looks like this:    C:\Program Files\Java\jre6\bin\javaw.exe

            String tempJavaHome = javaHome.replaceAll(" \\(x86\\)", "");

            if (bw != null) {
                bw.write("temp java.home: " + tempJavaHome + System.getProperty("line.separator"));
            }

            if (new File(tempJavaHome).exists()) {
                javaHome = tempJavaHome;
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
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public ArrayList<String> getJavaHomeAndOptions(String toolPath) throws FileNotFoundException, IOException, ClassNotFoundException {

        ArrayList<String> javaHomeAndOptions = new ArrayList<String>();

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        CompomicsWrapper wrapper = new CompomicsWrapper();
        File confFolder = new File(new File(toolPath).getParentFile(), "resources/conf");
        if (!confFolder.exists()) {
            String path = URLDecoder.decode(new File(toolPath).getParentFile().getAbsolutePath(), "UTF-8");
            confFolder = new File(path, "resources/conf");
        }
        if (!confFolder.exists()) {
            throw new FileNotFoundException(confFolder.getAbsolutePath() + " not found!");
        }
        File debugOutput = new File(confFolder, "startup.log");
        BufferedWriter bw = new BufferedWriter(new FileWriter(debugOutput));
        String javaHome = wrapper.getJavaHome(confFolder, bw);

        javaHomeAndOptions.add(javaHome);

        ArrayList<String> optionsAsList = wrapper.getJavaOptions(confFolder, new File(utilitiesUserPreferences.getPeptideShakerPath()), bw);

        for (String tempOption : optionsAsList) {
            javaHomeAndOptions.add(tempOption);
        }

        return javaHomeAndOptions;
    }
}
