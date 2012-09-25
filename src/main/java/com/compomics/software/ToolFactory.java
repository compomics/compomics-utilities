package com.compomics.software;

import com.compomics.software.dialogs.PeptideShakerSetupDialog;
import com.compomics.software.dialogs.RelimsSetupDialog;
import com.compomics.software.dialogs.ReporterSetupDialog;
import com.compomics.software.dialogs.SearchGuiSetupDialog;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This class can be used to start compomics tools.
 *
 * @author Marc Vaudel
 */
public class ToolFactory {

    /**
     * Starts PeptideShaker from the location of utilities preferences.
     *
     * @param parent a frame to display the path setting dialog
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException  
     */
    public static void startPeptideShaker(JFrame parent) throws FileNotFoundException, IOException, ClassNotFoundException, InterruptedException {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (utilitiesUserPreferences.getPeptideShakerPath() == null || !(new File(utilitiesUserPreferences.getPeptideShakerPath()).exists())) {
            new PeptideShakerSetupDialog(parent, true);
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        }
        if (utilitiesUserPreferences.getPeptideShakerPath() != null) {
            launch(utilitiesUserPreferences.getPeptideShakerPath(), "PeptideShaker");
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

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (utilitiesUserPreferences.getSearchGuiPath() == null || !(new File(utilitiesUserPreferences.getSearchGuiPath()).exists())) {
            new SearchGuiSetupDialog(parent, true);
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        }
        if (utilitiesUserPreferences.getSearchGuiPath() != null) {
            launch(utilitiesUserPreferences.getSearchGuiPath(), "SearchGUI");
        } else {
            throw new IllegalArgumentException("SearchGUI not found in " + utilitiesUserPreferences.getSearchGuiPath());
        }
    }

    /**
     * Generic method to start a tool.
     *
     * @param toolPath the path to the tool
     * @param toolName the name of the tool (used for bug report)
     */
    private static void launch(String toolPath, String toolName) throws IOException, InterruptedException {

        boolean debug = false;

        String quote = "";

        if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
            quote = "\"";
        }

        String cmdLine = "java -jar " + quote + toolPath + quote; //@TODO check the java home?

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
