package com.compomics.software;

import com.compomics.software.dialogs.PeptideShakerSetupDialog;
import com.compomics.software.dialogs.RelimsSetupDialog;
import com.compomics.software.dialogs.ReporterSetupDialog;
import com.compomics.software.dialogs.SearchGuiSetupDialog;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This class can be used to start compomics tools.
 *
 * @author Marc Vaudel
 */
public class ToolFactory {
    
    /**
     * The command line argument for a cps file for PeptideShaker
     */
    public static final String peptideShakerFile = "-cps";
    /**
     * The command line argument for mgf files for SearchGUI
     */
    public static final String searchGuiSpectrumFile = "-mgf";
    /**
     * The command line argument for a parameters file for SearchGUI
     */
    public static final String searchGuiParametersFile = "-search_parameters";

    /**
     * Starts PeptideShaker from the location of utilities preferences.
     *
     * @param parent a frame to display the path setting dialog (can be null)
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void startPeptideShaker(JFrame parent) throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException {
        startPeptideShaker(parent, null);
    }

    /**
     * Starts PeptideShaker from the location of utilities preferences and opens
     * the file given as argument. If null is given as file or if the file to
     * open is not found, the tool will go for a default start.
     *
     * @param parent a frame to display the path setting dialog (can be null)
     * @param file the file to open (cps format) (can be null)
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void startPeptideShaker(JFrame parent, File cpsFile) throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (utilitiesUserPreferences.getPeptideShakerPath() == null || !(new File(utilitiesUserPreferences.getPeptideShakerPath()).exists())) {
            new PeptideShakerSetupDialog(parent, true);
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        }
        if (utilitiesUserPreferences.getPeptideShakerPath() != null) {
            if (cpsFile != null) {
                ArrayList<String> args = new ArrayList<String>();
                args.add(peptideShakerFile);
                args.add(CommandLineUtils.getCommandLineArgument(cpsFile));
                launch(utilitiesUserPreferences.getPeptideShakerPath(), "PeptideShaker", args);
            } else {
                launch(utilitiesUserPreferences.getPeptideShakerPath(), "PeptideShaker");
            }
        } else {
            throw new IllegalArgumentException("PeptideShaker not found in " + utilitiesUserPreferences.getPeptideShakerPath());
        }
    }

    /**
     * Starts Reporter from the location of utilities preferences.
     *
     * @param parent a frame to display the path setting dialog.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void startReporter(JFrame parent) throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (utilitiesUserPreferences.getReporterPath() == null || !(new File(utilitiesUserPreferences.getReporterPath()).exists())) {
            new ReporterSetupDialog(parent, true);
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        }
        if (utilitiesUserPreferences.getReporterPath() != null) {
            launch(utilitiesUserPreferences.getReporterPath(), "Reporter");
        } else {
            throw new IllegalArgumentException("Reporter not found in " + utilitiesUserPreferences.getReporterPath());
        }
    }

    /**
     * Starts Relims from the location of utilities preferences.
     *
     * @param parent a frame to display the path setting dialog.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @deprecated use PeptideShaker's own Reshake option instead.
     */
    public static void startRelims(JFrame parent) throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();

        if (utilitiesUserPreferences.getRelimsPath() == null
                || (utilitiesUserPreferences.getRelimsPath() != null && !(new File(utilitiesUserPreferences.getRelimsPath()).exists()))) {
            new RelimsSetupDialog(parent, true);
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        }
        if (utilitiesUserPreferences.getRelimsPath() != null) {
            launch(utilitiesUserPreferences.getRelimsPath(), "Relims");
        } else {
            throw new IllegalArgumentException("Relims not found in " + utilitiesUserPreferences.getRelimsPath());
        }
    }

    /**
     * Starts SearchGUI from the location of utilities preferences.
     *
     * @param parent a frame to display the path setting dialog.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void startSearchGUI(JFrame parent) throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException {
        startSearchGUI(parent, null, null);
    }

    /**
     * Starts SearchGUI from the location of utilities preferences.
     *
     * @param parent a frame to display the path setting dialog.
     * @param mgfFiles the mgf files to search (can be null)
     * @param searchParameters the search parameters as a file (can be null)
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void startSearchGUI(JFrame parent, ArrayList<File> mgfFiles, File searchParameters) throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (utilitiesUserPreferences.getSearchGuiPath() == null || !(new File(utilitiesUserPreferences.getSearchGuiPath()).exists())) {
            new SearchGuiSetupDialog(parent, true);
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        }
        if (utilitiesUserPreferences.getSearchGuiPath() != null) {
            if (mgfFiles == null && searchParameters == null) {
                launch(utilitiesUserPreferences.getSearchGuiPath(), "SearchGUI");
            } else {
                ArrayList<String> args = new ArrayList<String>();
                if (mgfFiles != null) {
                    args.add(searchGuiSpectrumFile);
                    args.add(CommandLineUtils.getCommandLineArgument(mgfFiles));
                } else if (searchParameters != null) {
                    args.add(searchGuiParametersFile);
                    args.add(CommandLineUtils.getCommandLineArgument(searchParameters));
                }
                launch(utilitiesUserPreferences.getSearchGuiPath(), "SearchGUI", args);
            }
        } else {
            throw new IllegalArgumentException("SearchGUI not found in " + utilitiesUserPreferences.getSearchGuiPath());
        }
    }

    /**
     * Generic method to start a tool.
     *
     * @param toolPath the path to the tool
     * @param toolName the name of the tool (used for bug report)
     * @param args the arguments to pass to the tool (ignored if null)
     */
    private static void launch(String toolPath, String toolName) throws IOException, InterruptedException {
        launch(toolPath, toolName, null);
    }

    /**
     * Generic method to start a tool.
     *
     * @param toolPath the path to the tool
     * @param toolName the name of the tool (used for bug report)
     * @param args the arguments to pass to the tool (ignored if null)
     */
    private static void launch(String toolPath, String toolName, ArrayList<String> args) throws IOException, InterruptedException {

        String arguments = "";
        if (args != null) {
            arguments = CommandLineUtils.concatenate(args);
        }

        boolean debug = false;

        String quote = "";

        if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
            quote = "\"";
        }

        String cmdLine = "java -jar " + quote + toolPath + quote + " " + arguments; //@TODO check the java home?

        if (debug) {
            System.out.println(cmdLine);
        }

        Process p = Runtime.getRuntime().exec(cmdLine);

        InputStream stderr = p.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);

        String temp = "<ERROR>" + System.getProperty("line.separator") + System.getProperty("line.separator");

        if (debug) {
            System.out.println("<ERROR>");
        }

        String line = br.readLine();

        boolean error = false;

        while (line != null) {

            if (debug) {
                System.out.println(line);
            }

            temp += line + System.getProperty("line.separator");
            line = br.readLine();
            error = true;
        }

        if (debug) {
            System.out.println("</ERROR>");
        }

        temp += System.getProperty("line.separator") + "The command line executed:" + System.getProperty("line.separator");
        temp += cmdLine + System.getProperty("line.separator");
        temp += System.getProperty("line.separator") + "</ERROR>" + System.getProperty("line.separator");
        int exitVal = p.waitFor();

        if (debug) {
            System.out.println("Process exitValue: " + exitVal);
        }

        if (error) {
            File logFile = new File(toolPath + "/resources/conf", toolName + ".log");
            FileWriter f = new FileWriter(logFile, true);
            f.write(System.getProperty("line.separator") + System.getProperty("line.separator") + temp + System.getProperty("line.separator") + System.getProperty("line.separator"));
            f.close();

            javax.swing.JOptionPane.showMessageDialog(null,
                    "Failed to start " + toolName + ".\n\n"
                    + "Inspect the log file for details: resources/conf/" + toolName + ".log.\n\n"
                    + "Then go to Troubleshooting at http://peptide-shaker.googlecode.com.",
                    toolName + " - Startup Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
