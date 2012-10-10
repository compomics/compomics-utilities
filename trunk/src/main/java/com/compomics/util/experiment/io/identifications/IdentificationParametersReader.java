package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.ModificationProfile;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * The identification parameters reader returns the parameters used for
 * identification from a searchGUI parameters file
 *
 * @deprecated use the SearchParameters class instead
 * @author Marc Vaudel
 */
public class IdentificationParametersReader {

    /**
     * @deprecated use the SearchParameters class instead Reference for the
     * database file
     */
    public static final String DATABASE_FILE = "DATABASE_FILE";
    /**
     * @deprecated use the SearchParameters class instead Reference for the
     * enzyme
     */
    public static final String ENZYME = "ENZYME";
    /**
     * Reference for the separation of modifications
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String MODIFICATION_SEPARATOR = "//";
    /**
     * Reference for the separation of modification and its frequency
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String MODIFICATION_USE_SEPARATOR = "_";
    /**
     * Reference for the fixed modifications
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String FIXED_MODIFICATIONS = "FIXED_MODIFICATIONS";
    /**
     * Reference for the variable modifications
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String VARIABLE_MODIFICATIONS = "VARIABLE_MODIFICATIONS";
    /**
     * Reference for the missed cleavages
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String MISSED_CLEAVAGES = "MISSED_CLEAVAGES";
    /**
     * Reference for the precursor mass tolerance
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String PRECURSOR_MASS_TOLERANCE = "PRECURSOR_MASS_TOLERANCE";
    /**
     * Reference for the precursor mass tolerance unit
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String PRECURSOR_MASS_ACCURACY_UNIT = "PRECURSOR_MASS_TOLERANCE_UNIT";
    /**
     * Reference for the fragment ion mass tolerance
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String FRAGMENT_ION_MASS_ACCURACY = "FRAGMENT_MASS_TOLERANCE";
    /**
     * Reference for the lower precursor charge
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String PRECURSOR_CHARGE_LOWER_BOUND = "PRECURSOR_CHARGE_LOWER_BOUND";
    /**
     * Reference for the upper precursor charge
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String PRECURSOR_CHARGE_UPPER_BOUND = "PRECURSOR_CHARGE_UPPER_BOUND";
    /**
     * Reference for the type of fragment ion 1
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String FRAGMENT_ION_TYPE_1 = "FRAGMENT_ION_TYPE_1";
    /**
     * Reference for the type of fragment ion 2
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String FRAGMENT_ION_TYPE_2 = "FRAGMENT_ION_TYPE_2";
    /**
     * Reference for the e-value cutoff
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String EVALUE_CUTOFF = "EVALUE_CUTOFF";
    /**
     * Reference for the maximum length of the hitlist
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String MAXIMUM_HITLIST_LENGTH = "MAXIMUM_HITLIST_LENGTH";
    /**
     * Reference for the precursor charge to start considering multiply charged
     * fragments
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String PRECURSOR_CHARGE_TO_CONSIDER_MULTIPLY_CHARGED_FRAGMENTS = "OMSSA_PRECURSOR_CHARGE_TO_CONSIDER_MULTIPLY_CHARGED_FRAGMENTS";
    /**
     * Reference for the precursor elimination option
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String PRECURSOR_ELIMINATION = "OMSSA_PRECURSOR_ELIMINATION";
    /**
     * Reference for the precursor scaling option
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String PRECURSOR_SCALING = "OMSSA_PRECURSOR_SCALING";
    /**
     * Reference for the minimal peptide size
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String MIN_PEPTIDE_SIZE = "OMSSA_MINIMAL_PEPTIDE_SIZE";
    /**
     * Reference for the maximal peptide size
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String MAX_PEPTIDE_SIZE = "OMSSA_MAXIMAL_PEPTIDE_SIZE";
    /**
     * Reference for the charge estimation
     *
     * @deprecated use the SearchParameters class instead
     */
    public static final String CHARGE_ESTIMATION = "OMSSA_CHARGE_ESTIMATION";

    /**
     * Loads the search properties from a SearchGUI properties file.
     *
     * @deprecated use the SearchParameters class instead
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
     * @deprecated use the SearchParameters class instead
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

    /**
     * Conversion methods which converts an old school set of identification
     * properties into a SearchParameters instance. The enzymes must be loaded
     * in the Enzyme factory. The PTMs in the PTM factory.
     *
     * @param aProps the identification properties
     * @return the corresponding SearchParameters object
     */
    public static SearchParameters getSearchParameters(Properties aProps) {
        SearchParameters searchParameters = new SearchParameters();

        String temp = aProps.getProperty(IdentificationParametersReader.DATABASE_FILE);
        if (temp != null && !temp.equals("")) {
            searchParameters.setFastaFile(new File(temp.trim()));
        }

        ModificationProfile modificationProfile = searchParameters.getModificationProfile();

        temp = aProps.getProperty(IdentificationParametersReader.FIXED_MODIFICATIONS);

        if (temp != null && !temp.trim().equals("")) {

            PTMFactory ptmFactory = PTMFactory.getInstance();

            ArrayList<String> fixedMods = IdentificationParametersReader.parseModificationLine(temp.trim());

            for (String ptmName : fixedMods) {
                modificationProfile.addFixedModification(ptmFactory.getPTM(ptmName));
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.VARIABLE_MODIFICATIONS);

        if (temp != null && !temp.trim().equals("")) {

            PTMFactory ptmFactory = PTMFactory.getInstance();

            ArrayList<String> variableMods = IdentificationParametersReader.parseModificationLine(temp.trim());

            for (String ptmName : variableMods) {
                modificationProfile.addVariableModification(ptmFactory.getPTM(ptmName));
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.ENZYME);
        if (temp != null && !temp.equals("")) {
            EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
            if (enzymeFactory.enzymeLoaded(temp)) {
                searchParameters.setEnzyme(enzymeFactory.getEnzyme(temp.trim()));
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.FRAGMENT_ION_TYPE_1);
        if (temp != null && !temp.equals("")) {
            searchParameters.setIonSearched1(temp.trim());
        }

        temp = aProps.getProperty(IdentificationParametersReader.FRAGMENT_ION_TYPE_2);
        if (temp != null && !temp.equals("")) {
            searchParameters.setIonSearched2(temp.trim());
        }

        temp = aProps.getProperty(IdentificationParametersReader.MISSED_CLEAVAGES);
        if (temp != null) {
            try {
                searchParameters.setnMissedCleavages(new Integer(temp.trim()));
            } catch (Exception e) {
                // ignore
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.PRECURSOR_MASS_TOLERANCE);
        if (temp != null) {
            try {
                searchParameters.setPrecursorAccuracy(new Double(temp.trim()));
            } catch (Exception e) {
                // ignore
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.PRECURSOR_MASS_ACCURACY_UNIT);
        if (temp != null) {
            if (temp.equals("ppm")) {
                searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.PPM);
            } else if (temp.equals("Da")) {
                searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.FRAGMENT_ION_MASS_ACCURACY);
        if (temp != null) {
            try {
                searchParameters.setFragmentIonAccuracy(new Double(temp.trim()));
            } catch (Exception e) {
                // ignore
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.PRECURSOR_CHARGE_LOWER_BOUND);
        try {
            int charge = new Integer(temp.trim());
            searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, charge));
        } catch (Exception e) {
            // ignore
        }

        temp = aProps.getProperty(IdentificationParametersReader.PRECURSOR_CHARGE_UPPER_BOUND);
        try {
            int charge = new Integer(temp.trim());
            searchParameters.setMaxChargeSearched(new Charge(Charge.PLUS, charge));
        } catch (Exception e) {
            // ignore
        }

        // Specific parameters
        temp = aProps.getProperty(IdentificationParametersReader.EVALUE_CUTOFF);
        if (temp != null) {
            try {
                searchParameters.setMaxEValue(new Double(temp.trim()));
            } catch (Exception e) {
                // ignore
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.MAXIMUM_HITLIST_LENGTH);
        try {
            int length = new Integer(temp.trim());
            searchParameters.setHitListLength(length);
        } catch (Exception e) {
            // ignore
        }

        temp = aProps.getProperty(IdentificationParametersReader.PRECURSOR_CHARGE_TO_CONSIDER_MULTIPLY_CHARGED_FRAGMENTS);
        try {
            int charge = new Integer(temp.trim());
            searchParameters.setMinimalChargeForMultipleChargedFragments(new Charge(Charge.PLUS, charge));
        } catch (Exception e) {
            // ignore
        }

        temp = aProps.getProperty(IdentificationParametersReader.MIN_PEPTIDE_SIZE);
        try {
            int length = new Integer(temp.trim());
            searchParameters.setMinPeptideLength(length);
        } catch (Exception e) {
            // ignore
        }

        temp = aProps.getProperty(IdentificationParametersReader.MAX_PEPTIDE_SIZE);
        try {
            int length = new Integer(temp.trim());
            searchParameters.setMaxPeptideLength(length);
        } catch (Exception e) {
            // ignore
        }

        temp = aProps.getProperty(IdentificationParametersReader.PRECURSOR_ELIMINATION);
        if (temp != null) {
            temp = temp.trim();
            if (temp.equals("Yes")) {
                searchParameters.setRemovePrecursor(true);
            } else if (temp.equals("No")) {
                searchParameters.setRemovePrecursor(false);
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.PRECURSOR_SCALING);
        if (temp != null) {
            temp = temp.trim();
            if (temp.equals("Yes")) {
                searchParameters.setScalePrecursor(true);
            } else if (temp.equals("No")) {
                searchParameters.setScalePrecursor(false);
            }
        }

        temp = aProps.getProperty(IdentificationParametersReader.CHARGE_ESTIMATION);
        if (temp != null) {
            temp = temp.trim();
            if (temp.equals("Yes")) {
                searchParameters.setEstimateCharge(true);
            } else if (temp.equals("No")) {
                searchParameters.setEstimateCharge(false);
            }
        }

        return searchParameters;
    }
}
