package com.compomics.software;

import com.compomics.util.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * This class groups some convenience methods for the use of compomics tools in
 * command line.
 *
 * @author Marc Vaudel
 */
public class CommandLineUtils {

    /**
     * The command line argument separator.
     */
    public static final String SEPARATOR = ",";

    /**
     * Returns the list of file as argument for the command line.
     *
     * @param files the files
     * @return the list of file as string for command line argument
     */
    public static String getCommandLineArgument(ArrayList<File> files) {
        String result = "";
        for (File file : files) {
            if (!result.equals("")) {
                result += SEPARATOR;
            }
            result += getQuoteType() + file.getAbsolutePath() + getQuoteType();
        }
        return result;
    }

    /**
     * Returns the list of arguments as space separated string for the command
     * line.
     *
     * @param args the arguments
     * @return a comma separated string
     */
    public static String concatenate(ArrayList<String> args) {

        if (args == null) {
            return null;
        }

        String quote = getQuoteType();
        String result = "";

        for (String arg : args) {
            if (!result.equals("")) {
                result += " ";
            }

            // add quotes around the arguments in order to support file names with spaces
            if (!arg.startsWith("-") && !arg.startsWith("\"") && !arg.startsWith("\'")) {
                arg = quote + arg + quote;
            }

            result += arg;
        }

        return result;
    }

    /**
     * Returns the list of arguments as space separated string for the command
     * line. Adds quotes where they seem to be needed.
     *
     * @param args the arguments
     * @return a comma separated string
     */
    public static String concatenate(String[] args) {

        if (args == null) {
            return null;
        }

        String quote = getQuoteType();
        String result = "";

        for (String arg : args) {
            if (!result.equals("")) {
                result += " ";
            }

            // add quotes around the arguments in order to support file names with spaces
            if (!arg.startsWith("-") && !arg.startsWith("\"") && !arg.startsWith("\'")) {
                arg = quote + arg + quote;
            }

            result += arg;
        }

        return result;
    }

    /**
     * Returns the quote type to use. For example around file paths with spaces.
     *
     * @return the quote type to use
     */
    public static String getQuoteType() {

        String quote = "";

        if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
            quote = "\"";
        }

        return quote;
    }

    /**
     * Returns the file as argument for the command line.
     *
     * @param file the file
     * @return the list of file as string for command line argument
     */
    public static String getCommandLineArgument(File file) {
        return getQuoteType() + file.getAbsolutePath() + getQuoteType();
    }

    /**
     * Splits the input of comma separated command line input and returns the
     * results as an arraylist.
     *
     * @param cliInput the CLI input
     * @return an arraylist containing the results, empty list if empty string
     */
    public static ArrayList<String> splitInput(String cliInput) {
        ArrayList<String> results = new ArrayList<String>();

        // empty input, return the empty list
        if (cliInput == null || cliInput.trim().length() == 0) {
            return results;
        }

        for (String tempInput : cliInput.split(SEPARATOR)) {
            results.add(tempInput.trim());
        }
        return results;
    }

    /**
     * Returns a list of files as imported from the command line option.
     *
     * @param optionInput the command line option
     * @param fileExtentions the file extensions to be considered
     * @return a list of file candidates
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     */
    public static ArrayList<File> getFiles(String optionInput, ArrayList<String> fileExtentions) throws FileNotFoundException {
        ArrayList<File> result = new ArrayList<File>();
        ArrayList<String> files = splitInput(optionInput);
        if (files.size() == 1) {
            File testFile = new File(files.get(0));
            if (testFile.exists()) {
                if (testFile.isDirectory()) {
                    for (File childFile : testFile.listFiles()) {
                        String fileName = Util.getFileName(childFile.getAbsolutePath());
                        for (String extention : fileExtentions) {
                            if (fileName.toLowerCase().endsWith(extention.toLowerCase())) {
                                if (childFile.exists()) {
                                    result.add(childFile);
                                    break;
                                } else {
                                    throw new FileNotFoundException(childFile.getAbsolutePath() + " not found.");
                                }
                            }
                        }
                    }
                } else {
                    String fileName = Util.getFileName(testFile.getAbsolutePath());
                    for (String extention : fileExtentions) {
                        if (fileName.toLowerCase().endsWith(extention.toLowerCase())) {
                            result.add(testFile);
                            break;
                        }
                    }
                }
            } else {
                throw new FileNotFoundException(files.get(0) + " not found.");
            }
        } else {
            for (String file : files) {
                for (String extention : fileExtentions) {
                    if (file.toLowerCase().endsWith(extention.toLowerCase())) {
                        File testFile = new File(file);
                        if (testFile.exists()) {
                            result.add(testFile);
                        } else {
                            throw new FileNotFoundException(file + " not found.");
                        }
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Parses a list of integers from a command line option.
     *
     * @param aString the command line option
     * @param separator the separator used to separate the string
     * @return the list if integers
     */
    public static ArrayList<Integer> getIntegerListFromString(String aString, String separator) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (String component : aString.split(separator)) {
            try {
                Integer input = new Integer(component.trim());
                result.add(input);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot parse " + component.trim() + " into an integer from " + aString + " with separator '" + separator + "'.");
            }
        }
        return result;
    }

    /**
     * Parses a list of doubles from a command line option.
     *
     * @param aString the command line option
     * @param separator the separator used to separate the string
     * @return the list if doubles
     */
    public static ArrayList<Double> getDoubleListFromString(String aString, String separator) {
        ArrayList<Double> result = new ArrayList<Double>();
        for (String component : aString.split(separator)) {
            result.add(new Double(component.trim()));
        }
        return result;
    }
}
