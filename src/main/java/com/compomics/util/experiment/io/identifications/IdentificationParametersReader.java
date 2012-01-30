package com.compomics.util.experiment.io.identifications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * The identification parameters reader returns the parameters used for
 * identification from a searchGUI parameters file
 *
 * @author Marc Vaudel
 */
public class IdentificationParametersReader {

    /**
     * Reference for the database file
     */
    public static final String DATABASE_FILE = "DATABASE_FILE";
    /**
     * Reference for the enzyme
     */
    public static final String ENZYME = "ENZYME";
    /**
     * Reference for the separation of modifications
     */
    public static final String MODIFICATION_SEPARATOR = "//";
    /**
     * Reference for the separation of modification and its frequency
     */
    public static final String MODIFICATION_USE_SEPARATOR = "_";
    /**
     * Reference for the fixed modifications
     */
    public static final String FIXED_MODIFICATIONS = "FIXED_MODIFICATIONS";
    /**
     * Reference for the variable modifications
     */
    public static final String VARIABLE_MODIFICATIONS = "VARIABLE_MODIFICATIONS";
    /**
     * Reference for the missed cleavages
     */
    public static final String MISSED_CLEAVAGES = "MISSED_CLEAVAGES";
    /**
     * Reference for the precursor mass tolerance
     */
    public static final String PRECURSOR_MASS_TOLERANCE = "PRECURSOR_MASS_TOLERANCE";
    /**
     * Reference for the precursor mass tolerance unit
     */
    public static final String PRECURSOR_MASS_ACCURACY_UNIT = "PRECURSOR_MASS_TOLERANCE_UNIT";
    /**
     * Reference for the fragment ion mass tolerance
     */
    public static final String FRAGMENT_ION_MASS_ACCURACY = "FRAGMENT_MASS_TOLERANCE";
    /**
     * Reference for the lower precursor charge
     */
    public static final String PRECURSOR_CHARGE_LOWER_BOUND = "PRECURSOR_CHARGE_LOWER_BOUND";
    /**
     * Reference for the upper precursor charge
     */
    public static final String PRECURSOR_CHARGE_UPPER_BOUND = "PRECURSOR_CHARGE_UPPER_BOUND";
    /**
     * Reference for the type of fragment ion 1
     */
    public static final String FRAGMENT_ION_TYPE_1 = "FRAGMENT_ION_TYPE_1";
    /**
     * Reference for the type of fragment ion 2
     */
    public static final String FRAGMENT_ION_TYPE_2 = "FRAGMENT_ION_TYPE_2";
    /**
     * Reference for the e-value cutoff
     */
    public static final String EVALUE_CUTOFF = "EVALUE_CUTOFF";
    /**
     * Reference for the maximum length of the hitlist
     */
    public static final String MAXIMUM_HITLIST_LENGTH = "MAXIMUM_HITLIST_LENGTH";
    /**
     * Reference for the precursor charge to start considering multiply charged
     * fragments
     */
    public static final String PRECURSOR_CHARGE_TO_CONSIDER_MULTIPLY_CHARGED_FRAGMENTS = "OMSSA_PRECURSOR_CHARGE_TO_CONSIDER_MULTIPLY_CHARGED_FRAGMENTS";
    /**
     * Reference for the precursor elimination option
     */
    public static final String PRECURSOR_ELIMINATION = "OMSSA_PRECURSOR_ELIMINATION";
    /**
     * Reference for the precursor scaling option
     */
    public static final String PRECURSOR_SCALING = "OMSSA_PRECURSOR_SCALING";
    /**
     * Reference for the minimal peptide size
     */
    public static final String MIN_PEPTIDE_SIZE = "OMSSA_MINIMAL_PEPTIDE_SIZE";
    /**
     * Reference for the maximal peptide size
     */
    public static final String MAX_PEPTIDE_SIZE = "OMSSA_MAXIMAL_PEPTIDE_SIZE";
    /**
     * Reference for the charge estimation
     */
    public static final String CHARGE_ESTIMATION = "OMSSA_CHARGE_ESTIMATION";

    /**
     * Loads the search properties from a SearchGUI properties file.
     *
     * @param aFile a searchGUI properties file
     * @return the corresponding properties, indexed by the static fields
     * @throws FileNotFoundException exception thrown if the file is not found
     * @throws IOException exception thrown whenever a problem occurs while
     * reading the file
     */
    public static Properties loadProperties(File aFile) throws FileNotFoundException, IOException {
        Properties screenProps = new Properties();
        FileInputStream fis = new FileInputStream(aFile);
        if (fis != null) {
            screenProps.load(fis);
            fis.close();
        } else {
            throw new IllegalArgumentException("Could not read the file you specified ('" + aFile.getAbsolutePath() + "').");
        }
        return screenProps;
    }

    /**
     * This method parses a modification line from a properties file.
     *
     * @param aLine String with the modification line from the properties file.
     * @return ArrayList with the parsed PTM indexes.
     */
    public static ArrayList<String> parseModificationLine(String aLine) {

        ArrayList<String> result = new ArrayList<String>();

        // Split the different modifications.
        int start;

        while ((start = aLine.indexOf(IdentificationParametersReader.MODIFICATION_SEPARATOR)) >= 0) {
            String name = aLine.substring(0, start);
            aLine = aLine.substring(start + 2);
            if (!name.trim().equals("")) {
                result.add(name);
            }
        }

        // Fence post.
        if (!aLine.trim().equals("")) {
            result.add(aLine);
        }

        return result;
    }
}
