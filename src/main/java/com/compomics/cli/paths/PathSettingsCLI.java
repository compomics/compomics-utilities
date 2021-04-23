package com.compomics.cli.paths;

import com.compomics.software.CompomicsWrapper;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Allows the user to set the path settings in command line.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PathSettingsCLI {

    /**
     * The input bean containing the user parameters.
     */
    private PathSettingsCLIInputBean pathSettingsCLIInputBean;
    /**
     * Waiting handler used to keep track of the progress.
     */
    private WaitingHandler waitingHandler;
    /**
     * The log folder given on the command line. Null if not set.
     */
    private static File logFolder = null;

    /**
     * Constructor.
     *
     * @param pathSettingsCLIInputBean an input bean containing the user
     * parameters
     */
    public PathSettingsCLI(PathSettingsCLIInputBean pathSettingsCLIInputBean) {
        this.pathSettingsCLIInputBean = pathSettingsCLIInputBean;
    }

    /**
     * Sets the path settings and returns null.
     *
     * @return null
     */
    public Object call() {
        waitingHandler = new WaitingHandlerCLIImpl();
        setPathSettings();
        if (!waitingHandler.isRunCanceled()) {
            System.exit(0);
            return 0;
        } else {
            System.exit(1);
            return 1;
        }
    }

    /**
     * Sets the path settings according to the pathSettingsCLIInputBean.
     */
    public void setPathSettings() {

        if (waitingHandler == null) {
            waitingHandler = new WaitingHandlerCLIImpl();
        }

        // set the log file
        if (pathSettingsCLIInputBean.useLogFile()) {
            if (pathSettingsCLIInputBean.getLogFolder() != null) {
                redirectErrorStream(pathSettingsCLIInputBean.getLogFolder());
            } else {
                redirectErrorStream(new File(getJarFilePath() + File.separator + "resources"));
            }
        } else {
            System.setErr(new java.io.PrintStream(System.out));
        }

    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("SearchCLI.class").getPath(), "SearchGUI");
    }

    /**
     * SearchGUI path settings CLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "The SearchGUI path settings command line allows setting the path of every configuration file created by SearchGUI or set a temporary folder where all files will be stored." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see https://compomics.github.io/projects/searchgui.html and https://compomics.github.io/projects/searchgui/wiki/SearchCLI.html." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Or contact the developers at https://groups.google.com/group/peptide-shaker." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator")
                + System.getProperty("line.separator");
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            Options lOptions = new Options();
            PathSettingsCLIParams.createOptionsCLI(lOptions);
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(lOptions, args);

            if (args.length == 0) {
                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "========================================" + System.getProperty("line.separator"));
                lPrintWriter.print("SearchGUI Path Settings - Command Line" + System.getProperty("line.separator"));
                lPrintWriter.print("========================================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(PathSettingsCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);
            } else {
                PathSettingsCLIInputBean cliInputBean = new PathSettingsCLIInputBean(line);
                PathSettingsCLI pathSettingsCLI = new PathSettingsCLI(cliInputBean);
                pathSettingsCLI.call();
            }
        } catch (OutOfMemoryError e) {
            System.out.println("SearchGUI used up all the memory and had to be stopped. See the SearchGUI log for details.");
            System.err.println("Ran out of memory!");
            System.err.println("Memory given to the Java virtual machine: " + Runtime.getRuntime().maxMemory() + ".");
            System.err.println("Memory used by the Java virtual machine: " + Runtime.getRuntime().totalMemory() + ".");
            System.err.println("Free memory in the Java virtual machine: " + Runtime.getRuntime().freeMemory() + ".");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("SearchGUI processing failed. See the SearchGUI log for details.");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "PathSettingsCLI{"
                + ", cliInputBean=" + pathSettingsCLIInputBean
                + '}';
    }

    /**
     * If the arguments contains changes to the paths these arguments will be
     * extracted and the paths updated, before the remaining non-path options
     * are returned for further processing.
     *
     * @param args the command line arguments
     * @return a list of all non-path related arguments
     * @throws ParseException if a ParseException occurs
     */
    public static String[] extractAndUpdatePathOptions(String[] args) throws ParseException {

        ArrayList<String> allPathOptions = PathSettingsCLIParams.getOptionIDs();

        ArrayList<String> pathSettingArgs = new ArrayList<>();
        ArrayList<String> nonPathSettingArgs = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {

            String currentArg = args[i];

            boolean pathOption = allPathOptions.contains(currentArg);

            if (pathOption) {
                pathSettingArgs.add(currentArg);
            } else {
                nonPathSettingArgs.add(currentArg);
            }

            // check if the argument has a parameter
            if (i + 1 < args.length) {
                String nextArg = args[i + 1];
                if (!nextArg.startsWith("-")) {
                    if (pathOption) {
                        pathSettingArgs.add(args[++i]);
                    } else {
                        nonPathSettingArgs.add(args[++i]);
                    }
                }
            }
        }

        String[] pathSettingArgsAsList = pathSettingArgs.toArray(new String[pathSettingArgs.size()]);
        String[] nonPathSettingArgsAsList = nonPathSettingArgs.toArray(new String[nonPathSettingArgs.size()]);

        // update the paths if needed
        Options pathOptions = new Options();
        PathSettingsCLIParams.createOptionsCLI(pathOptions);
        DefaultParser parser = new DefaultParser();
        CommandLine line = parser.parse(pathOptions, pathSettingArgsAsList);
        PathSettingsCLIInputBean pathSettingsCLIInputBean = new PathSettingsCLIInputBean(line);
        PathSettingsCLI pathSettingsCLI = new PathSettingsCLI(pathSettingsCLIInputBean);
        pathSettingsCLI.setPathSettings();

        return nonPathSettingArgsAsList;
    }

    /**
     * Redirects the error stream to the SearchGUI.log of a given folder.
     *
     * @param aLogFolder the folder where to save the log
     */
    public static void redirectErrorStream(File aLogFolder) {

        logFolder = aLogFolder;

        try {
            aLogFolder.mkdirs();
            File file = new File(aLogFolder, "compomics-utilities.log");
            System.setErr(new java.io.PrintStream(new FileOutputStream(file, true)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
