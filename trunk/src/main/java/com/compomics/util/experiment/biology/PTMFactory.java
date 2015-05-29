package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.ModificationProfile;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.awt.Color;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This factory will load PTM from an XML file and provide them on demand as
 * standard class.
 *
 * @author Marc Vaudel
 */
public class PTMFactory implements Serializable {

    /**
     * Serial number for serialization compatibility.
     */
    static final long serialVersionUID = 7935264190312934466L;
    /**
     * Instance of the factory.
     */
    private static PTMFactory instance = null;
    /**
     * The folder containing the PTM factory.
     */
    private static String SERIALIZATION_FILE_FOLDER = System.getProperty("user.home") + "/.compomics";
    /**
     * The name of the PTM factory back-up file.
     */
    private static String SERIALIZATION_FILE_NAME = "ptmFactory-3.28.24.cus";
    /**
     * A map linking indexes with modifications.
     */
    private HashMap<String, PTM> ptmMap = new HashMap<String, PTM>();
    /**
     * List of the indexes of default modifications.
     */
    private ArrayList<String> defaultMods = new ArrayList<String>();
    /**
     * List of the indexes of user modifications.
     */
    private ArrayList<String> userMods = new ArrayList<String>();
    /**
     * Mapping of the expected modification names to the color used.
     */
    private HashMap<String, Color> userColors = new HashMap<String, Color>();
    /**
     * Map of the short names.
     */
    private HashMap<String, String> shortNames = new HashMap<String, String>();
    /**
     * Map of OMSSA indexes for default modifications.
     */
    private HashMap<String, Integer> defaultOmssaIndexes = new HashMap<String, Integer>();
    /**
     * Unknown modification to be returned when the modification is not found.
     */
    public static final PTM unknownPTM = new PTM(PTM.MODAA, "unknown", 0, new AminoAcidPattern());
    /**
     * Suffix for the modifications searched but not in the factory.
     */
    public static final String SEARCH_SUFFIX = "|search-only";
    /**
     * Set to true if the default mods are sorted alphabetically.
     */
    public boolean defaultModsSorted = false;
    /**
     * Set to true if the users mods are sorted alphabetically.
     */
    public boolean usersModsSorted = false;

    /**
     * Constructor for the factory.
     */
    private PTMFactory() {
        defaultMods = new ArrayList<String>();
        defaultModsSorted = false;
    }

    /**
     * Static method to get the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static PTMFactory getInstance() {
        if (instance == null) {
            try {
                File savedFile = new File(SERIALIZATION_FILE_FOLDER, SERIALIZATION_FILE_NAME);
                instance = (PTMFactory) SerializationUtils.readObject(savedFile);
            } catch (Exception e) {
                instance = new PTMFactory();
                try {
                    instance.saveFactory();
                } catch (IOException ioe) {
                    // cancel save
                    ioe.printStackTrace();
                }
            }
            instance.setDefaultReporterIons();
        }
        return instance;
    }

    /**
     * Clears the factory getInstance() needs to be called afterwards.
     */
    public void clearFactory() {
        instance = new PTMFactory();
    }

    /**
     * Reloads the factory getInstance() needs to be called afterwards.
     */
    public void reloadFactory() {
        instance = null;
    }

    /**
     * Saves the factory in the user folder.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the ptmFactory
     */
    public void saveFactory() throws IOException {
        File factoryFile = new File(SERIALIZATION_FILE_FOLDER, SERIALIZATION_FILE_NAME);
        if (!factoryFile.getParentFile().exists()) {
            factoryFile.getParentFile().mkdir();
        }
        SerializationUtils.writeObject(instance, factoryFile);
    }

    /**
     * Get a PTM according to its OMSSA index.
     *
     * @param index the PTM index
     * @param modificationProfile the modification profile used for the search
     * @return the selected PTM
     */
    public PTM getPTM(ModificationProfile modificationProfile, int index) {
        String name = modificationProfile.getModification(index);

        if (name != null && ptmMap.get(name) != null) {
            return ptmMap.get(name);
        }

        return unknownPTM;
    }

    /**
     * Returns the standard search compatible PTM corresponding to this pattern.
     * i.e. a pattern targeting a single amino acid and not a complex pattern.
     *
     * @param modification the modification of interest
     * @return a search compatible modification
     */
    public PTM getSearchedPTM(PTM modification) {
        if (!modification.isStandardSearch()) {
            return new PTM(modification.getType(), modification.getName() + SEARCH_SUFFIX, modification.getMass(), modification.getPattern().getStandardSearchPattern());
        } else {
            return modification;
        }
    }

    /**
     * Returns the standard search compatible PTM corresponding to this pattern,
     * i.e., a pattern targeting a single amino acid and not a complex pattern.
     *
     * @param modificationName the name of the modification of interest
     * @return a search compatible modification
     */
    public PTM getSearchedPTM(String modificationName) {
        PTM modification = getPTM(modificationName);
        return getSearchedPTM(modification);
    }

    /**
     * Adds a new user modification.
     *
     * @param ptm the new modification to add
     */
    public void addUserPTM(PTM ptm) {
        String modName = ptm.getName();
        ptmMap.put(modName, ptm);
        if (!userMods.contains(modName)) {
            userMods.add(modName);
        } else {
            userMods.set(userMods.indexOf(modName), modName);
        }
        usersModsSorted = false;
    }

    /**
     * Removes a user PTM.
     *
     * @param ptmName the name of the PTM to remove
     */
    public void removeUserPtm(String ptmName) {
        if (defaultMods.contains(ptmName)) {
            throw new IllegalArgumentException("Impossible to remove default modification " + ptmName);
        }
        ptmMap.remove(ptmName);
        userMods.remove(ptmName);
    }

    /**
     * Returns the PTM indexed by its name.
     *
     * @param name the name of the desired PTM
     * @return The desired PTM
     */
    public PTM getPTM(String name) {
        PTM ptm = ptmMap.get(name);
        if (ptm != null) {
            return ptm;
        }
        return unknownPTM;
    }

    /**
     * Returns a boolean indicating whether the PTM is loaded in the factory.
     *
     * @param name the name of the PTM
     * @return a boolean indicating whether the PTM is loaded in the factory
     */
    public boolean containsPTM(String name) {
        return ptmMap.containsKey(name) || name.equals(unknownPTM.getName());
    }

    /**
     * Import modifications from a modification file.
     *
     * @param modificationsFile A file containing modifications
     * @param userMod A boolean indicating whether the file comprises user
     * designed modification
     * @throws XmlPullParserException exception thrown whenever an error is
     * encountered while parsing
     * @throws IOException exception thrown whenever an error is encountered
     * reading the file
     */
    public void importModifications(File modificationsFile, boolean userMod) throws XmlPullParserException, IOException {
        importModifications(modificationsFile, userMod, false);
    }

    /**
     * Import modifications from a modification file.
     *
     * @param modificationsFile A file containing modifications
     * @param userMod A boolean indicating whether the file comprises user
     * designed modification
     * @param overwrite a boolean indicating whether modifications from the XML
     * file should be overwritten
     * @throws XmlPullParserException exception thrown whenever an error is
     * encountered while parsing
     * @throws IOException exception thrown whenever an error is encountered
     * reading the file
     */
    public void importModifications(File modificationsFile, boolean userMod, boolean overwrite) throws XmlPullParserException, IOException {

        // Create the pull parser.
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        // Create a reader for the input file.
        BufferedReader br = new BufferedReader(new FileReader(modificationsFile));
        // Set the XML Pull Parser to read from this reader.
        parser.setInput(br);
        // Start the parsing.
        int type = parser.next();
        // Go through the whole document.
        while (type != XmlPullParser.END_DOCUMENT) {
            // If we find a 'MSModSpec' start tag,
            // we should parse the mod.
            if (type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec")) {
                parseMSModSpec(parser, userMod, overwrite);
            }
            type = parser.next();
        }
        br.close();
        setDefaultNeutralLosses();
        setDefaultReporterIons();
    }

    /**
     * Returns the default OMSSA index of the modification. Null if not found.
     *
     * @param modificationName the name of the modification
     * @return the default OMSSA index
     */
    public Integer getDefaultOMSSAIndex(String modificationName) {
        return defaultOmssaIndexes.get(modificationName);
    }

    /**
     * Imports the OMSSA indexes from an XML file.
     *
     * @param modificationsFile the modification file
     * @return a map of all indexes: modification name &gt; OMSSA index
     * @throws XmlPullParserException if an XmlPullParserException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public static HashMap<String, Integer> getOMSSAIndexes(File modificationsFile) throws XmlPullParserException, FileNotFoundException, IOException {

        HashMap<String, Integer> indexes = new HashMap<String, Integer>();

        // Create the pull parser.
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        // Create a reader for the input file.
        BufferedReader br = new BufferedReader(new FileReader(modificationsFile));
        // Set the XML Pull Parser to read from this reader.
        parser.setInput(br);
        // Start the parsing.
        int type = parser.next();
        Integer number = null;
        // Go through the whole document.
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG && parser.getName().equals("MSMod")) {
                parser.next();
                String numberString = parser.getText();
                try {
                    number = new Integer(numberString);
                } catch (NumberFormatException nfe) {
                    throw new XmlPullParserException("Found non-parseable text '" + numberString
                            + "' for the value of the 'MSMod' tag on line " + parser.getLineNumber() + ".");
                }
            }
            if (type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec_name")) {
                parser.next();
                String name = parser.getText();
                if (number != null) {
                    indexes.put(name, number);
                }
            }
            type = parser.next();
        }
        br.close();

        return indexes;
    }

    /**
     * This method parses a single MSModSpec tag's contents from the
     * modifications XML file. It expects the XmlPullParser to be set on the
     * starting 'MSModSpec' tag, and upon completion, the parser will be set on
     * the closing 'MSModSpec' tag.
     *
     * @param parser the parser
     * @param userMod a boolean indicating whether we are parsing user
     * modifications or not
     * @param overwrite a boolean indicating whether modifications from the XML
     * file should be overwritten
     * @throws XmlPullParserException when the pull parser failed.
     * @throws IOException when the pull parser could not access the underlying
     * file.
     */
    private void parseMSModSpec(XmlPullParser parser, boolean userMod, boolean overwrite) throws XmlPullParserException, IOException {
        // Check whether the XmlPullParser is correctly positioned (i.e. directly on the 'MSModSpec' start tag)
        if (!(parser.getName().equals("MSModSpec") && parser.getEventType() == XmlPullParser.START_TAG)) {
            throw new IllegalArgumentException("XmlPullParser should have been on the start tag for 'MSModSpec', but was on '" + parser.getName() + "' instead.");
        }
        // Structure now is: tag, whitespace, tag, NUMBER (which we need).
        // Start tag.
        parser.nextTag();
        // Validate correctness.
        if (!parser.getName().equals("MSModSpec_mod")) {
            throw new XmlPullParserException("Found tag '" + parser.getName() + "' where 'MSModSpec_mod' was expected on line " + parser.getLineNumber() + ".");
        }
        parser.nextTag();
        // Validate correctness.
        if (!parser.getName().equals("MSMod")) {
            throw new XmlPullParserException("Found tag '" + parser.getName() + "' where 'MSMod' was expected on line " + parser.getLineNumber() + ".");
        }
        // We need the value here.
        parser.next();
        String numberString = parser.getText();
        int number = -1;
        try {
            number = Integer.parseInt(numberString);
        } catch (NumberFormatException nfe) {
            throw new XmlPullParserException("Found non-parseable text '" + numberString + "' for the value of the 'MSMod' tag on line " + parser.getLineNumber() + ".");
        }
        // Modification type
        int type = parser.next();
        while (!(type == XmlPullParser.START_TAG && parser.getName().equals("MSModType"))) {
            type = parser.next();
        }
        String modType = parser.getAttributeValue(0);
        // OK, we got the number. Progress to the user-readable name.
        type = parser.next();
        while (!(type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec_name"))) {
            type = parser.next();
        }
        // Right, we should be on the right start tag, so get the value.
        parser.next();
        String name = parser.getText().trim().toLowerCase();
        // Mass
        type = parser.next();
        while (!(type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec_monomass"))) {
            type = parser.next();
        }
        parser.next();
        String mass = parser.getText().trim();
        // Residue
        type = parser.next();
        ArrayList<String> residues = new ArrayList();
        if (modType.compareTo("modcaa") == 0 || modType.compareTo("modcpaa") == 0 || modType.compareTo("modnaa") == 0 || modType.compareTo("modnpaa") == 0 || modType.compareTo("modaa") == 0) {
            while (!(type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec_residues_E"))) {
                type = parser.next();
            }
            ArrayList<String> aminoAcids = new ArrayList();
            while (type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec_residues_E")) {
                parser.next();
                aminoAcids.add(parser.getText().trim());
                parser.next();
                parser.next();
                type = parser.next();
            }
            for (String aa : aminoAcids) {
                if (!residues.contains(aa)) {
                    residues.add(aa);
                }
            }
        }
        // Create and implement modification.
        AminoAcidPattern pattern = new AminoAcidPattern(residues);
        PTM currentPTM = new PTM(getIndex(modType), name, new Double(mass), pattern);

        while (!(type == XmlPullParser.START_TAG && parser.getName().equals("MSModSpec_neutralloss"))
                && !(type == XmlPullParser.END_TAG && parser.getName().equals("MSModSpec"))) {
            type = parser.next();
        }
        if (parser.getName().equals("MSModSpec_neutralloss")) {
            ArrayList<NeutralLoss> neutralLosses = new ArrayList<NeutralLoss>();
            int cpt = 1;
            while (!(type == XmlPullParser.END_TAG && parser.getName().equals("MSModSpec_neutralloss"))) {
                type = parser.next();
                if (type == XmlPullParser.START_TAG && parser.getName().equals("MSMassSet_monomass")) {
                    parser.next();
                    String doubleString = "";
                    try {
                        doubleString = parser.getText().trim();
                        double neutralLossMass = new Double(doubleString);
                        neutralLosses.add(new NeutralLoss(name + " " + cpt, neutralLossMass, true));
                    } catch (Exception e) {
                        throw new XmlPullParserException("Found non-parseable text '" + doubleString
                                + "' for the value of the 'MSMassSet_monomass' neutral loss tag on line " + parser.getLineNumber() + ".");
                    }
                    cpt++;
                }
            }
            currentPTM.setNeutralLosses(neutralLosses);
        }
        while (!(type == XmlPullParser.END_TAG && parser.getName().equals("MSModSpec"))) {
            type = parser.next();
        }

        if (!name.startsWith("user modification ") && !name.equals(unknownPTM.getName())) {
            if (!name.endsWith(SEARCH_SUFFIX)) {

                // add any saved neutral losses or reporter ions for user mods
                if (userMod && ptmMap.get(name) != null) {
                    if (!ptmMap.get(name).getNeutralLosses().isEmpty()) {
                        for (NeutralLoss neutralLoss : ptmMap.get(name).getNeutralLosses()) {
                            currentPTM.addNeutralLoss(neutralLoss);
                        }
                    }
                    if (!ptmMap.get(name).getReporterIons().isEmpty()) {
                        for (ReporterIon reporterIon : ptmMap.get(name).getReporterIons()) {
                            currentPTM.addReporterIon(reporterIon);
                        }
                    }
                }

                ptmMap.put(name, currentPTM);
            } else {
                name = name.substring(0, name.lastIndexOf(SEARCH_SUFFIX));
            }
            if (userMod) {
                if (defaultMods.contains(name)) {
                    if (!name.equalsIgnoreCase("unknown")) {
                        throw new IllegalArgumentException("Impossible to load \'" + name + "\' as user modification. Already defined as default modification.");
                    } else {
                        System.out.println("Impossible to load \'" + name + "\' as user modification. Already defined as default modification.");
                    }
                } else if (!userMods.contains(name) || overwrite) {
                    userMods.add(name);
                    usersModsSorted = false;
                }
            } else {
                if (!defaultMods.contains(name) || overwrite) {
                    defaultMods.add(name);
                    defaultOmssaIndexes.put(name, number);
                    defaultModsSorted = false;
                }
            }
        }
    }

    /**
     * Get the index of a file.
     *
     * @param modType modification type found
     * @return corresponding static index
     */
    private int getIndex(String modType) {
        if (modType.compareTo("modaa") == 0) {
            return PTM.MODAA;
        } else if (modType.compareTo("modn") == 0) {
            return PTM.MODN;
        } else if (modType.compareTo("modnaa") == 0) {
            return PTM.MODNAA;
        } else if (modType.compareTo("modnp") == 0) {
            return PTM.MODNP;
        } else if (modType.compareTo("modnpaa") == 0) {
            return PTM.MODNPAA;
        } else if (modType.compareTo("modc") == 0) {
            return PTM.MODC;
        } else if (modType.compareTo("modcaa") == 0) {
            return PTM.MODCAA;
        } else if (modType.compareTo("modcp") == 0) {
            return PTM.MODCP;
        } else if (modType.compareTo("modcpaa") == 0) {
            return PTM.MODCPAA;
        }
        return -1;
    }

    /**
     * Write the OMSSA modification files to the given folder.
     *
     * @param aFolder the folder to write the modification files to
     * @param utilitiesModFile the utilities corresponding mod file
     * @param utilitiesUserModFile the utilities corresponding usermod file
     * @throws IOException an IOException is thrown in case an issue is
     * encountered while reading or writing a file.
     */
    public void writeOmssaModificationsFiles(File aFolder, File utilitiesModFile, File utilitiesUserModFile) throws IOException {
        int c;
        BufferedReader br = new BufferedReader(new FileReader(utilitiesModFile));
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(aFolder, "mods.xml")));
            try {
                while ((c = br.read()) != -1) {
                    bw.write(c);
                }
            } finally {
                bw.close();
            }
        } finally {
            br.close();
        }

        br = new BufferedReader(new FileReader(utilitiesUserModFile));
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(aFolder, "usermods.xml")));
            try {
                while ((c = br.read()) != -1) {
                    bw.write(c);
                }
            } finally {
                bw.close();
            }
        } finally {
            br.close();
        }
    }

    /**
     * Writes the OMSSA modification file corresponding to the PTMs loaded in
     * the factory in the given file.
     *
     * @param file the file
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    public void writeOmssaUserModificationFile(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            String toWrite = "<?xml version=\"1.0\"?>\n<MSModSpecSet\n"
                    + "xmlns=\"http://www.ncbi.nlm.nih.gov\"\n"
                    + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "xs:schemaLocation=\"http://www.ncbi.nlm.nih.gov OMSSA.xsd\"\n>\n\n";
            bw.write(toWrite);

            int cpt = 1;
            for (String ptmName : userMods) {
                if (!ptmName.equals(unknownPTM.getName())) {
                    toWrite = getOmssaUserModBloc(ptmName, cpt++);
                    bw.write(toWrite);
                }
            }

            while (cpt <= 30) {
                int omssaIndex = cpt + 118;
                if (omssaIndex > 128) {
                    omssaIndex += 13;
                }
                toWrite = "\t<MSModSpec>\n"
                        + "\t\t<MSModSpec_mod>\n"
                        + "\t\t\t<MSMod value=\"usermod" + cpt + "\">" + omssaIndex + "</MSMod>\n"
                        + "\t\t</MSModSpec_mod>\n"
                        + "\t\t<MSModSpec_type>\n"
                        + "\t\t\t<MSModType value=\"modaa\">0</MSModType>\n"
                        + "\t\t</MSModSpec_type>\n"
                        + "\t\t<MSModSpec_name>User modification " + cpt + "</MSModSpec_name>\n"
                        + "\t\t<MSModSpec_monomass>0</MSModSpec_monomass>\n"
                        + "\t\t<MSModSpec_averagemass>0</MSModSpec_averagemass>\n"
                        + "\t\t<MSModSpec_n15mass>0</MSModSpec_n15mass>\n"
                        + "\t\t<MSModSpec_residues>\n"
                        + "\t\t\t<MSModSpec_residues_E>X</MSModSpec_residues_E>\n"
                        + "\t\t</MSModSpec_residues>\n"
                        + "\t</MSModSpec>\n";
                bw.write(toWrite);

                cpt++;
            }
            toWrite = "</MSModSpecSet>";
            bw.write(toWrite);
        } finally {
            bw.close();
        }
    }

    /**
     * Returns an MSModSpec bloc as present in the OMSSA user modification files
     * for a given PTM. Only the amino acids targeted by the pattern of the PTM
     * will be considered.
     *
     * @param ptmName the name of the PTM
     * @param cpt the index of this PTM
     * @return a string containing the XML bloc
     */
    public String getOmssaUserModBloc(String ptmName, int cpt) {
        int omssaIndex = cpt + 118;
        if (omssaIndex > 128) {
            omssaIndex += 13;
        }
        PTM ptm = getSearchedPTM(ptmName);

        String result = "\t<MSModSpec>\n";
        result += "\t\t<MSModSpec_mod>\n";
        result += "\t\t\t<MSMod value=\"usermod" + cpt + "\">" + omssaIndex + "</MSMod>\n";
        result += "\t\t</MSModSpec_mod>\n"
                + "\t\t<MSModSpec_type>\n";
        if (ptm.getType() == PTM.MODAA) {
            result += "\t\t\t<MSModType value=\"modaa\">" + PTM.MODAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODN) {
            result += "\t\t\t<MSModType value=\"modn\">" + PTM.MODN + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODNAA) {
            result += "\t\t\t<MSModType value=\"modnaa\">" + PTM.MODNAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODNP) {
            result += "\t\t\t<MSModType value=\"modnp\">" + PTM.MODNP + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODNPAA) {
            result += "\t\t\t<MSModType value=\"modnpaa\">" + PTM.MODNPAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODC) {
            result += "\t\t\t<MSModType value=\"modc\">" + PTM.MODC + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODCAA) {
            result += "\t\t\t<MSModType value=\"modcaa\">" + PTM.MODCAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODCP) {
            result += "\t\t\t<MSModType value=\"modcp\">" + PTM.MODCP + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODCPAA) {
            result += "\t\t\t<MSModType value=\"modcpaa\">" + PTM.MODCPAA + "</MSModType>\n";
        }
        result += "\t\t</MSModSpec_type>\n";
        result += "\t\t<MSModSpec_name>" + ptm.getName() + "</MSModSpec_name>\n";
        result += "\t\t<MSModSpec_monomass>" + ptm.getMass() + "</MSModSpec_monomass>\n"
                + "\t\t<MSModSpec_averagemass>0</MSModSpec_averagemass>\n"
                + "\t\t<MSModSpec_n15mass>0</MSModSpec_n15mass>\n";
        if (ptm.getType() == PTM.MODAA
                || ptm.getType() == PTM.MODNAA
                || ptm.getType() == PTM.MODNPAA
                || ptm.getType() == PTM.MODCAA
                || ptm.getType() == PTM.MODCPAA) {
            result += "\t\t<MSModSpec_residues>\n";
            for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                result += "\t\t\t<MSModSpec_residues_E>" + aa + "</MSModSpec_residues_E>\n";
            }
            result += "\t\t</MSModSpec_residues>\n";
        }
        boolean first = true;
        for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
            if (neutralLoss.isFixed()) {
                if (first) {
                    result += "\t\t<MSModSpec_neutralloss>\n";
                    first = false;
                }
                result += "\t\t\t<MSMassSet>\n";
                result += "\t\t\t\t<MSMassSet_monomass>" + neutralLoss.mass + "</MSMassSet_monomass>\n";
                result += "\t\t\t\t<MSMassSet_averagemass>0</MSMassSet_averagemass>";
                result += "\t\t\t\t<MSMassSet_n15mass>0</MSMassSet_n15mass>";
                result += "\t\t\t</MSMassSet>\n";
            }
        }
        if (!first) {
            result += "\t\t</MSModSpec_neutralloss>\n";
        }
        result += "\t</MSModSpec>\n";
        return result;
    }

    /**
     * Returns the names of the default modifications.
     *
     * @return the names of the default modifications
     */
    public ArrayList<String> getDefaultModifications() {
        return defaultMods;
    }

    /**
     * Returns the alphabetically ordered names of the default modifications.
     *
     * @return the alphabetically ordered names of the default modifications
     */
    public ArrayList<String> getDefaultModificationsOrdered() {
        if (!defaultModsSorted) {
            Collections.sort(defaultMods);
            defaultModsSorted = true;
        }
        return defaultMods;
    }

    /**
     * Returns the names of the user defined modifications.
     *
     * @return the names of the user defined modifications
     */
    public ArrayList<String> getUserModifications() {
        return userMods;
    }

    /**
     * Returns the alphabetically ordered names of the user defined
     * modifications.
     *
     * @return the alphabetically ordered names of the user defined
     * modifications
     */
    public ArrayList<String> getUserModificationsOrdered() {
        if (!usersModsSorted) {
            Collections.sort(userMods);
            usersModsSorted = true;
        }
        return userMods;
    }

    /**
     * Returns the names of all imported PTMs.
     *
     * @return the names of all imported PTMs
     */
    public ArrayList<String> getPTMs() {
        return new ArrayList<String>(ptmMap.keySet());
    }

    /**
     * Convenience method returning a boolean indicating whether a PTM is user
     * defined or default.
     *
     * @param ptmName the name of the PTM
     * @return boolean indicating whether a PTM is user defined
     */
    public boolean isUserDefined(String ptmName) {
        return !defaultMods.contains(ptmName);
    }

    /**
     * Verifies that the modifications backed-up in the search parameters are
     * loaded and alerts the user in case conflicts are found.
     *
     * @param searchParameters the search parameters to load
     * @param overwrite if true, overwrite the modification
     * @return returns a list of modifications already loaded which should be
     * checked.
     */
    public ArrayList<String> loadBackedUpModifications(SearchParameters searchParameters, boolean overwrite) {
        ModificationProfile modificationProfile = searchParameters.getModificationProfile();
        ArrayList<String> toCheck = new ArrayList<String>();
        for (String modification : modificationProfile.getBackedUpPtms()) {
            if (containsPTM(modification)) {
                PTM oldPTM = getPTM(modification);
                PTM newPTM = modificationProfile.getPtm(modification);
                if (!oldPTM.isSameAs(newPTM)) {
                    toCheck.add(modification);
                    if (overwrite) {
                        ptmMap.put(modification, modificationProfile.getPtm(modification));
                    }
                }
            } else {
                addUserPTM(modificationProfile.getPtm(modification));
            }
            if (!shortNames.containsKey(modification)) {
                PTM ptm = modificationProfile.getPtm(modification);
                if (ptm.getShortName() != null) {
                    shortNames.put(modification, ptm.getShortName());
                }
            }
        }
        return toCheck;
    }

    /**
     * Returns the expected modifications based on the modification profile, the
     * peptide found and the modification details. Returns the names in a map
     * where the modification names are indexed by the index on the sequence. 1
     * is the first amino acid.
     *
     * @param modificationProfile the modification profile used for the search
     * (available in the search parameters)
     * @param peptide the peptide identified with the parent proteins (necessary
     * for protein termini modifications)
     * @param modificationMass the modification mass as found in the search
     * results
     * @param ptmMassTolerance the mass tolerance to use to match the
     * modification mass
     * @param sequenceMatchingPreferences the sequence matching preferences for peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences for ptm to peptide mapping
     *
     * @return a map of expected PTMs corresponding to the given
     * characteristics. Empty if none found.
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public HashMap<Integer, ArrayList<String>> getExpectedPTMs(ModificationProfile modificationProfile, Peptide peptide,
            double modificationMass, double ptmMassTolerance, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, FileNotFoundException, SQLException {

        HashMap<Integer, ArrayList<String>> mapping = new HashMap<Integer, ArrayList<String>>();

        for (String ptmName : modificationProfile.getAllNotFixedModifications()) {
            PTM ptm = getPTM(ptmName);
            if (Math.abs(ptm.getMass() - modificationMass) <= ptmMassTolerance) {
                for (int site : peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences)) {
                    ArrayList<String> modifications = mapping.get(site);
                    if (modifications == null) {
                        modifications = new ArrayList<String>();
                        mapping.put(site, modifications);
                    }
                    modifications.add(ptmName);
                }
            }
        }

        return mapping;
    }

    /**
     * Returns the names of the possibly expected modification based on the name
     * of the expected modification in a map where the PTM names are indexed by
     * their potential site on the sequence. 1 is the first amino acid.
     * Candidate PTMs are expected non fixed modifications with the same mass.
     *
     * @param modificationProfile the modification profile used for the search
     * (available in the search parameters)
     * @param peptide the peptide
     * @param ptmName the name of the searched PTM
     * @param ptmMassTolerance the PTM mass tolerance
     * @param sequenceMatchingPreferences the sequence matching preferences for peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences for ptm to peptide mapping
     *
     * @return the possible expected modification names. Empty if not found.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public HashMap<Integer, ArrayList<String>> getExpectedPTMs(ModificationProfile modificationProfile, Peptide peptide, String ptmName,
            Double ptmMassTolerance, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {
        PTM ptm = getPTM(ptmName);
        return getExpectedPTMs(modificationProfile, peptide, ptm.getMass(), ptmMassTolerance, sequenceMatchingPreferences, ptmSequenceMatchingPreferences);
    }

    /**
     * Removes the fixed modifications of the peptide and remaps the one
     * searched for according to the ModificationProfile. Note: for protein
     * terminal modification the protein must be loaded in the sequence factory.
     *
     * @param modificationProfile the modification profile
     * @param peptide the peptide
     * @param sequenceMatchingPreferences the sequence matching preferences for peptide to protein mapping
     * @param ptmSequenceMatchingPreferences the sequence matching preferences for PTM to peptide mapping
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void checkFixedModifications(ModificationProfile modificationProfile, Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences, SequenceMatchingPreferences ptmSequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        ArrayList<ModificationMatch> toRemove = new ArrayList<ModificationMatch>();
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            if (!modMatch.isVariable()) {
                toRemove.add(modMatch);
            }
        }
        for (ModificationMatch modMatch : toRemove) {
            peptide.getModificationMatches().remove(modMatch);
        }
        HashMap<Integer, Double> taken = new HashMap<Integer, Double>();

        for (String fixedModification : modificationProfile.getFixedModifications()) {
            PTM ptm = getPTM(fixedModification);
            if (ptm.getType() == PTM.MODAA) {
                for (int pos : peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences)) {
                    if (!taken.containsKey(pos)) {
                        taken.put(pos, ptm.getMass());
                        peptide.addModificationMatch(new ModificationMatch(fixedModification, false, pos));
                    } else if (taken.get(pos) != ptm.getMass()) {
                        throw new IllegalArgumentException("Attempting to put two fixed modifications of different masses ("
                                + taken.get(pos) + ", " + ptm.getMass() + ") at position " + pos + " in peptide " + peptide.getSequence() + ".");
                    }
                }
            } else if (ptm.getType() == PTM.MODC) {
                if (!peptide.isCterm(sequenceMatchingPreferences).isEmpty()) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
                }
            } else if (ptm.getType() == PTM.MODN) {
                if (!peptide.isNterm(sequenceMatchingPreferences).isEmpty()) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                }
            } else if (ptm.getType() == PTM.MODCAA) {
                String sequence = peptide.getSequence();
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(sequence.length())) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
                }
            } else if (ptm.getType() == PTM.MODNAA) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(1)) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                }
            } else if (ptm.getType() == PTM.MODCP) {
                peptide.addModificationMatch(new ModificationMatch(fixedModification, false, peptide.getSequence().length()));
            } else if (ptm.getType() == PTM.MODNP) {
                peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
            } else if (ptm.getType() == PTM.MODCPAA) {
                String sequence = peptide.getSequence();
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(sequence.length())) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, sequence.length()));
                }
            } else if (ptm.getType() == PTM.MODNPAA) {
                if (peptide.getPotentialModificationSites(ptm, sequenceMatchingPreferences, ptmSequenceMatchingPreferences).contains(1)) {
                    peptide.addModificationMatch(new ModificationMatch(fixedModification, false, 1));
                }
            }
        }
    }

    /**
     * Removes the fixed modifications of the given tag and remaps the one
     * searched for according to the ModificationProfile. Note: for protein
     * terminal modification the protein must be loaded in the sequence factory.
     *
     * @param modificationProfile the modification profile
     * @param tag the tag
     * @param sequenceMatchingPreferences the sequence matching preferences for ptm to amino acid mapping
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     */
    public void checkFixedModifications(ModificationProfile modificationProfile, Tag tag, SequenceMatchingPreferences sequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException, SQLException {

        int indexInTag = 0, componentNumber = 0;
        for (TagComponent tagComponent : tag.getContent()) {
            componentNumber++;
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                ArrayList<ModificationMatch> toRemove = new ArrayList<ModificationMatch>();
                for (int aa : aminoAcidPattern.getModificationIndexes()) {
                    ArrayList<ModificationMatch> modificationMatches = aminoAcidPattern.getModificationsAt(aa);
                    for (ModificationMatch modMatch : modificationMatches) {
                        if (!modMatch.isVariable()) {
                            toRemove.add(modMatch);
                        }
                    }
                    for (ModificationMatch modMatch : toRemove) {
                        aminoAcidPattern.removeModificationMatch(aa, modMatch);
                    }
                }
                for (int aa = 1; aa <= aminoAcidPattern.length(); aa++) {
                    indexInTag++;
                    Double modification = null;

                    for (String fixedModification : modificationProfile.getFixedModifications()) {
                        PTM ptm = getPTM(fixedModification);
                        if (ptm.getType() == PTM.MODAA) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(indexInTag)) {
                                if (modification == null) {
                                    modification = ptm.getMass();
                                    aminoAcidPattern.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                                } else if (modification != ptm.getMass()) {
                                    throw new IllegalArgumentException("Attempting to put two fixed modifications of different masses ("
                                            + modification + ", " + ptm.getMass() + ") at position " + aa + " in pattern "
                                            + aminoAcidPattern.asSequence() + " of tag " + tag.asSequence() + ".");
                                }
                            }
                        } else if (ptm.getType() == PTM.MODCP && componentNumber == tag.getContent().size() && aa == aminoAcidPattern.length()) {
                            aminoAcidPattern.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                        } else if (ptm.getType() == PTM.MODNP && componentNumber == 1 && aa == 1) {
                            aminoAcidPattern.addModificationMatch(1, new ModificationMatch(fixedModification, false, 1));
                        } else if (ptm.getType() == PTM.MODCPAA && componentNumber == tag.getContent().size() && aa == aminoAcidPattern.length()) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(indexInTag)) {
                                aminoAcidPattern.addModificationMatch(aa, new ModificationMatch(fixedModification, false, aa));
                            }
                        } else if (ptm.getType() == PTM.MODNPAA && componentNumber == 1 && aa == 1) {
                            if (tag.getPotentialModificationSites(ptm, sequenceMatchingPreferences).contains(1)) {
                                aminoAcidPattern.addModificationMatch(1, new ModificationMatch(fixedModification, false, 1));
                            }
                        }
                    }
                }
            } else {
                indexInTag++;
            }
        }
    }

    /**
     * Set the OMSSA indexes used for this search.
     *
     * @param modificationProfile the modification profile of this search
     */
    public void setSearchedOMSSAIndexes(ModificationProfile modificationProfile) {
        for (int rank = 1; rank <= userMods.size(); rank++) {
            int omssaIndex = rank + 118; //Note that this index must be the same as in the getOmssaUserModBloc method
            if (omssaIndex > 128) {
                omssaIndex += 13;
            }
            String ptm = userMods.get(rank - 1);
            if (modificationProfile.contains(ptm)) {
                modificationProfile.setOmssaIndex(ptm, omssaIndex);
            }
        }
        for (String ptm : defaultOmssaIndexes.keySet()) {
            if (modificationProfile.contains(ptm)) {
                modificationProfile.setOmssaIndex(ptm, defaultOmssaIndexes.get(ptm));
            }
        }
    }

    /**
     * Sets the default neutral losses of PTMs when not implemented.
     */
    public void setDefaultNeutralLosses() {

        boolean changed = false;

        for (String ptmName : defaultMods) {

            // @TODO: I hate hard coding this, any more elegant approach welcome...
            if (ptmName.contains("phospho")) {
                PTM ptm = ptmMap.get(ptmName);
                if (ptmName.contains(" s")
                        || ptmName.contains(" t")) {
                    boolean found = false;
                    for (NeutralLoss implemented : ptm.getNeutralLosses()) {
                        if (implemented.isSameAs(NeutralLoss.H3PO4)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ptm.addNeutralLoss(NeutralLoss.H3PO4);
                    }
                }
                if (ptmName.contains(" y")) {
                    boolean found = false;
                    for (NeutralLoss implemented : ptm.getNeutralLosses()) {
                        if (implemented.isSameAs(NeutralLoss.HPO3)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ptm.addNeutralLoss(NeutralLoss.HPO3);
                    }
                }
            } else if (ptmName.contains("oxidation") && ptmName.contains("M")) {
                PTM ptm = ptmMap.get(ptmName);
                boolean found = false;
                for (NeutralLoss implemented : ptm.getNeutralLosses()) {
                    if (implemented.isSameAs(NeutralLoss.CH4OS)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ptm.addNeutralLoss(NeutralLoss.CH4OS);
                }
            }
            if (changed) {
                try {
                    saveFactory();
                } catch (IOException e) {
                    // cancel save
                }
            }
        }
    }

    /**
     * Sets the default reporter ions of PTMs when not implemented.
     */
    public void setDefaultReporterIons() {

        boolean changed = false;

        // add default reporter ions for the user modifications
        for (String ptmName : userMods) {
            boolean tempChange = addDefaultReporterIons(ptmName);
            if (tempChange) {
                changed = true;
            }
        }

        // add default reporter ions for the default modifications
        for (String ptmName : defaultMods) {
            boolean tempChange = addDefaultReporterIons(ptmName);
            if (tempChange) {
                changed = true;
            }
        }

        if (changed) {
            try {
                saveFactory();
            } catch (IOException e) {
                // cancel save
            }
        }
    }

    /**
     * Add default reporter ions.
     *
     * @param ptmName the name of the PTM
     * @return true if reporter ions where added
     */
    private boolean addDefaultReporterIons(String ptmName) {

        boolean changed = false;

        // @TODO: remove the hard coding...
        if (ptmName.contains("itraq")) {
            PTM ptm = ptmMap.get(ptmName);
            if (ptm.getReporterIons().isEmpty()) {

                ptm.addReporterIon(ReporterIon.iTRAQ114);
                ptm.addReporterIon(ReporterIon.iTRAQ115);
                ptm.addReporterIon(ReporterIon.iTRAQ116);
                ptm.addReporterIon(ReporterIon.iTRAQ117);

                if (ptmName.contains("8")) {
                    ptm.addReporterIon(ReporterIon.iTRAQ113);
                    ptm.addReporterIon(ReporterIon.iTRAQ118);
                    ptm.addReporterIon(ReporterIon.iTRAQ119);
                    ptm.addReporterIon(ReporterIon.iTRAQ121);
                    ptm.addReporterIon(ReporterIon.iTRAQ_305);
                } else {
                    ptm.addReporterIon(ReporterIon.iTRAQ_145);
                }

                changed = true;
            }
        } else if (ptmName.contains("tmt")) {

            PTM ptm = ptmMap.get(ptmName);

            if (ptm.getReporterIons().isEmpty()) {

                if (ptmName.contains("old")) {

                    ptm.addReporterIon(ReporterIon.TMT126_old);
                    ptm.addReporterIon(ReporterIon.TMT127_old);

                    if (ptmName.contains("6")) {
                        ptm.addReporterIon(ReporterIon.TMT128_old);
                        ptm.addReporterIon(ReporterIon.TMT129_old);
                        ptm.addReporterIon(ReporterIon.TMT130_old);
                        ptm.addReporterIon(ReporterIon.TMT131_old);
                        ptm.addReporterIon(ReporterIon.TMT_230);
                    } else {
                        ptm.addReporterIon(ReporterIon.TMT_226);
                    }
                } else {

                    if (ptmName.contains("duplex")) {
                        ptm.addReporterIon(ReporterIon.TMT126_hcd);
                        ptm.addReporterIon(ReporterIon.TMT126_etd);
                        ptm.addReporterIon(ReporterIon.TMT127_hcd);
                        ptm.addReporterIon(ReporterIon.TMT127_etd);
                        ptm.addReporterIon(ReporterIon.TMT_226);
                    } else if (ptmName.contains("6")) {
                        ptm.addReporterIon(ReporterIon.TMT126_hcd);
                        ptm.addReporterIon(ReporterIon.TMT126_etd);
                        ptm.addReporterIon(ReporterIon.TMT127_hcd);
                        ptm.addReporterIon(ReporterIon.TMT127_etd);
                        ptm.addReporterIon(ReporterIon.TMT128_hcd);
                        ptm.addReporterIon(ReporterIon.TMT128_etd);
                        ptm.addReporterIon(ReporterIon.TMT129_hcd);
                        ptm.addReporterIon(ReporterIon.TMT129_etd);
                        ptm.addReporterIon(ReporterIon.TMT130_hcd);
                        ptm.addReporterIon(ReporterIon.TMT130_etd);
                        ptm.addReporterIon(ReporterIon.TMT131_hcd);
                        ptm.addReporterIon(ReporterIon.TMT131_etd);
                        ptm.addReporterIon(ReporterIon.TMT_230);
                    } else if (ptmName.contains("10")) {
                        ptm.addReporterIon(ReporterIon.TMT126_hcd);
                        ptm.addReporterIon(ReporterIon.TMT127N_hcd);
                        ptm.addReporterIon(ReporterIon.TMT127C_hcd);
                        ptm.addReporterIon(ReporterIon.TMT128N_hcd);
                        ptm.addReporterIon(ReporterIon.TMT128C_hcd);
                        ptm.addReporterIon(ReporterIon.TMT129N_hcd);
                        ptm.addReporterIon(ReporterIon.TMT129C_hcd);
                        ptm.addReporterIon(ReporterIon.TMT130N_hcd);
                        ptm.addReporterIon(ReporterIon.TMT130C_hcd);
                        ptm.addReporterIon(ReporterIon.TMT131_hcd);
                        ptm.addReporterIon(ReporterIon.TMT_230);
                    }
                }

                changed = true;
            }
        } else if (ptmName.contains("acetylation of k")) {

            PTM ptm = ptmMap.get(ptmName);

            if (ptm.getReporterIons().isEmpty()) {
                ptm.addReporterIon(ReporterIon.ACE_K_126);
                ptm.addReporterIon(ReporterIon.ACE_K_143);
                changed = true;
            }
        } else if (ptmName.contains("phosphorylation of y")) {

            PTM ptm = ptmMap.get(ptmName);

            if (ptm.getReporterIons().isEmpty()) {
                ptm.addReporterIon(ReporterIon.PHOSPHO_Y);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Sets the short name for a modification.
     *
     * @param modification the modification name
     * @param shortName the short name
     */
    public void setShortName(String modification, String shortName) {
        shortNames.put(modification, shortName);
    }

    /**
     * Returns the user favorite short name, a default short name otherwise.
     *
     * @param modification the name of the modification
     * @return the corresponding short name
     */
    public String getShortName(String modification) {
        if (shortNames.containsKey(modification)) {
            return shortNames.get(modification);
        } else {
            PTM ptm = getPTM(modification);
            if (ptm.getShortName() != null) {
                return ptm.getShortName();
            }
            return getDefaultShortName(modification);
        }
    }

    /**
     * Returns a default short name for a given modification.
     *
     * @param modificationName the full name of the modification
     * @return the default short name
     */
    public static String getDefaultShortName(String modificationName) {
//        if (modificationName.startsWith("oxidation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(13) + "";
//            aa.toUpperCase();
//            return aa + "ox";
//        }
        if (modificationName.contains("oxidation")) {
            return "ox";
        }
//        if (modificationName.startsWith("phosphorylation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(19) + "";
//            aa.toUpperCase();
//            return "p" + aa;
//        }
        if (modificationName.contains("phospho")) {
            return "p";
        }
//        if (modificationName.startsWith("sulfation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(19) + "";
//            aa.toUpperCase();
//            return "sulf" + aa;
//        }
        if (modificationName.contains("sulfation")) {
            return "sulf";
        }
//        if (modificationName.startsWith("acetylation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(15) + "";
//            aa.toUpperCase();
//            return "ace" + aa;
//        }
        if (modificationName.contains("acetylation")) {
            return "ace";
        }
//        if (modificationName.startsWith("deamidation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(15) + "";
//            aa.toUpperCase();
//            return "deam" + aa;
//        }
        if (modificationName.contains("deamidation")) {
            return "deam";
        }
        if (modificationName.contains("itraq")) {
            return "iTRAQ";
        }
        if (modificationName.contains("icat")) {
            return "icat";
        }
        if (modificationName.contains("heavy arginine")) {
            return "heavyR";
        }
        if (modificationName.contains("heavy lysine")) {
            return "heavyK";
        }
        if (modificationName.contains("o18")) {
            return "o18";
        }
        if (modificationName.contains("tmt")) {
            return "tmt";
        }
        if (modificationName.contains("carbamidomethyl")) {
            return "cmm";
        }
//        if (modificationName.startsWith("di-methylation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(15) + "";
//            aa.toUpperCase();
//            return "dimeth" + aa;
//        }
        if (modificationName.contains("di-methylation")
                || modificationName.contains("dimethylation")) {
            return "dimeth";
        }
//        if (modificationName.startsWith("tri-methylation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(15) + "";
//            aa.toUpperCase();
//            return "trimeth" + aa;
//        }
        if (modificationName.contains("tri-methylation")
                || modificationName.contains("trimethylation")) {
            return "trimeth";
        }
//        if (modificationName.startsWith("methylation of ") && !modificationName.contains("term")) {
//            String aa = modificationName.charAt(15) + "";
//            aa.toUpperCase();
//            return "meth" + aa;
//        }
        if (modificationName.contains("methylation")) {
            return "meth";
        }
        if (modificationName.contains("pyro")) {
            return "pyro";
        }
        String result = modificationName;
        if (result.contains(" ")) {
            result = result.substring(0, result.indexOf(" "));
        }
        return result;
    }

    /**
     * Returns the color used to code the given modification.
     *
     * @param modification the name of the given expected modification
     * @return the corresponding color
     */
    public Color getColor(String modification) {
        if (!userColors.containsKey(modification)) {
            setColor(modification, getDefaultColor(modification));
        }
        return userColors.get(modification);
    }

    /**
     * Sets a new color for the given expected modification.
     *
     * @param expectedModification the name of the expected modification
     * @param color the new color
     */
    public void setColor(String expectedModification, Color color) {
        userColors.put(expectedModification, color);
    }

    /**
     * Returns a default color based on the modification name.
     *
     * @param modification the name of the modification
     * @return a default color.
     */
    public static Color getDefaultColor(String modification) {
        if (modification.contains("no modification")) {
            return Color.LIGHT_GRAY;
        } else if (modification.contains("phospho")) {
            return Color.RED;
        } else if (modification.contains("ox")) {
            return Color.BLUE;
        } else if (modification.contains("itraq")) {
            return Color.magenta;
        } else if (modification.contains("carbamido")) {
            return Color.LIGHT_GRAY;
        } else if (modification.contains("ace")) {
            return new Color(153, 153, 0);
        } else if (modification.contains("glyco")) {
            return Color.ORANGE;
        } else {
            float r = (float) Math.random();
            float g = (float) Math.random();
            float b = (float) Math.random();
            return new Color(r, g, b);
        }
    }

    /**
     * Tries to convert a PRIDE PTM to utilities PTM name, and add it to the
     * modification profile. Unknown PTMs are added to the unknown PTMs
     * arraylist.
     *
     * @param pridePtmName the PRIDE PTM name
     * @param modProfile the modification profile to add the PTMs to
     * @param unknownPtms the list of unknown PTMS, updated during this method
     * @param isFixed if true, the PTM will be added as a fixed modification
     * @return a pride parameters report as a string (for use in PRIDE Reshake)
     */
    public String convertPridePtm(String pridePtmName, ModificationProfile modProfile, ArrayList<String> unknownPtms, boolean isFixed) {

        String prideParametersReport = "";

        // special cases for when multiple ptms are needed
        if (pridePtmName.equalsIgnoreCase("iTRAQ4plex")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex reporter+balance reagent N-acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent N6-acylated lysine")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent O4&apos;-acylated tyrosine")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent acylated N-terminal")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-114 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-116 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ4plex-117 reporter+balance reagent N6-acylated lysine")) {

            if (!modProfile.contains("itraq114 on k")) {
                modProfile.addFixedModification(getPTM("itraq114 on k"));
                prideParametersReport += "<br>" + "itraq114 on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq114 on nterm")) {
                modProfile.addFixedModification(getPTM("itraq114 on nterm"));
                prideParametersReport += "<br>" + "itraq114 on nterm" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq114 on y")) {
                modProfile.addVariableModification(getPTM("itraq114 on y"));
                prideParametersReport += "<br>" + "itraq114 on y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("iTRAQ8plex")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-113 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-114 reporter+balance reagent N6-acylated lysine")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-114 reporter+balance reagent O4&apos;-acylated tyrosine")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-114 reporter+balance reagent acylated N-terminal")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-115 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex-116 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("iTRAQ8plex:13C(6)15N(2)")) {

            if (!modProfile.contains("itraq8plex:13c(6)15n(2) on k")) {
                modProfile.addFixedModification(getPTM("itraq8plex:13c(6)15n(2) on k"));
                prideParametersReport += "<br>" + "itraq8plex:13c(6)15n(2) on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq8plex:13c(6)15n(2) on nterm")) {
                modProfile.addFixedModification(getPTM("itraq8plex:13c(6)15n(2) on nterm"));
                prideParametersReport += "<br>" + "itraq8plex:13c(6)15n(2) on nterm" + " (assumed fixed)";
            }
            if (!modProfile.contains("itraq8plex:13c(6)15n(2) on y")) {
                modProfile.addVariableModification(getPTM("itraq8plex:13c(6)15n(2) on y"));
                prideParametersReport += "<br>" + "itraq8plex:13c(6)15n(2) on y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("TMT2plex") || pridePtmName.equalsIgnoreCase("TMTduplex")) {

            if (!modProfile.contains("tmt duplex on k")) {
                modProfile.addFixedModification(getPTM("tmt duplex on k"));
                prideParametersReport += "<br>" + "tmt duplex on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("tmt duplex on n-term peptide")) {
                modProfile.addFixedModification(getPTM("tmt duplex on n-term peptide"));
                prideParametersReport += "<br>" + "tmt duplex on n-term peptide" + " (assumed fixed)";
            }
        } else if (pridePtmName.equalsIgnoreCase("TMT6plex")
                || pridePtmName.equalsIgnoreCase("TMT6plex-126 reporter+balance reagent acylated residue")
                || pridePtmName.equalsIgnoreCase("TMT6plex-131 reporter+balance reagent N6-acylated lysine")) { // @TODO: add the new or old TMT tags..?

            if (!modProfile.contains("tmt 6-plex on k")) {
                modProfile.addFixedModification(getPTM("tmt 6-plex on k"));
                prideParametersReport += "<br>" + "tmt 6-plex on k" + " (assumed fixed)";
            }
            if (!modProfile.contains("tmt 6-plex on n-term peptide")) {
                modProfile.addFixedModification(getPTM("tmt 6-plex on n-term peptide"));
                prideParametersReport += "<br>" + "tmt 6-plex on n-term peptide" + " (assumed fixed)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Phosphorylation")
                || pridePtmName.equalsIgnoreCase("L-aspartic 4-phosphoric anhydride")
                || pridePtmName.equalsIgnoreCase("O-phosphorylated residue")
                || pridePtmName.equalsIgnoreCase("Phospho")
                || pridePtmName.equalsIgnoreCase("phosphorylated residue")) {

            if (!modProfile.contains("phosphorylation of s")) {
                modProfile.addVariableModification(getPTM("phosphorylation of s"));
                prideParametersReport += "<br>" + "phosphorylation of s" + " (assumed variable)";
            }
            if (!modProfile.contains("phosphorylation of t")) {
                modProfile.addVariableModification(getPTM("phosphorylation of t"));
                prideParametersReport += "<br>" + "phosphorylation of t" + " (assumed variable)";
            }
            if (!modProfile.contains("phosphorylation of y")) {
                modProfile.addVariableModification(getPTM("phosphorylation of y"));
                prideParametersReport += "<br>" + "phosphorylation of y" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Palmitoylation")) {

            if (!modProfile.contains("palmitoylation of c")) {
                modProfile.addVariableModification(getPTM("palmitoylation of c"));
                prideParametersReport += "<br>" + "palmitoylation of c" + " (assumed variable)";
            }
            if (!modProfile.contains("palmitoylation of k")) {
                modProfile.addVariableModification(getPTM("palmitoylation of k"));
                prideParametersReport += "<br>" + "palmitoylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("palmitoylation of s")) {
                modProfile.addVariableModification(getPTM("palmitoylation of s"));
                prideParametersReport += "<br>" + "palmitoylation of s" + " (assumed variable)";
            }
            if (!modProfile.contains("palmitoylation of t")) {
                modProfile.addVariableModification(getPTM("palmitoylation of t"));
                prideParametersReport += "<br>" + "palmitoylation of t" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Formylation")) {

            if (!modProfile.contains("formylation of k")) {
                modProfile.addVariableModification(getPTM("formylation of k"));
                prideParametersReport += "<br>" + "formylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("formylation of peptide n-term")) {
                modProfile.addVariableModification(getPTM("formylation of peptide n-term"));
                prideParametersReport += "<br>" + "formylation of peptide n-term" + " (assumed variable)";
            }
            if (!modProfile.contains("formylation of protein c-term")) {
                modProfile.addVariableModification(getPTM("formylation of protein c-term"));
                prideParametersReport += "<br>" + "formylation of protein c-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Carbamylation")) {

            if (!modProfile.contains("carbamylation of k")) {
                modProfile.addVariableModification(getPTM("carbamylation of k"));
                prideParametersReport += "<br>" + "carbamylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("carbamylation of n-term peptide")) {
                modProfile.addVariableModification(getPTM("carbamylation of n-term peptide"));
                prideParametersReport += "<br>" + "carbamylation of n-term peptide" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(12)C labeled N6-propanoyl-L-lysine")) {

            if (!modProfile.contains("propionyl light k")) {
                modProfile.addVariableModification(getPTM("propionyl light k"));
                prideParametersReport += "<br>" + "propionyl light k" + " (assumed variable)";
            }
            if (!modProfile.contains("propionyl light on peptide n-term")) {
                modProfile.addVariableModification(getPTM("propionyl light on peptide n-term"));
                prideParametersReport += "<br>" + "propionyl light on peptide n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(13)C labeled N6-propanoyl-L-lysine")) {

            if (!modProfile.contains("propionyl heavy k")) {
                modProfile.addVariableModification(getPTM("propionyl heavy k"));
                prideParametersReport += "<br>" + "propionyl heavy k" + " (assumed variable)";
            }
            if (!modProfile.contains("propionyl heavy peptide n-term")) {
                modProfile.addVariableModification(getPTM("propionyl heavy peptide n-term"));
                prideParametersReport += "<br>" + "propionyl heavy peptide n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("3x(2)H residue methyl ester")) {

            if (!modProfile.contains("tri-deuteromethylation of d")) {
                modProfile.addVariableModification(getPTM("tri-deuteromethylation of d"));
                prideParametersReport += "<br>" + "tri-deuteromethylation of d" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-deuteromethylation of e")) {
                modProfile.addVariableModification(getPTM("tri-deuteromethylation of e"));
                prideParametersReport += "<br>" + "tri-deuteromethylation of e" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-deuteromethylation of peptide c-term")) {
                modProfile.addVariableModification(getPTM("tri-deuteromethylation of peptide c-term"));
                prideParametersReport += "<br>" + "tri-deuteromethylation of peptide c-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled residue")) {

            if (!modProfile.contains("heavy arginine-13C6")) {
                modProfile.addVariableModification(getPTM("heavy arginine-13C6"));
                prideParametersReport += "<br>" + "heavy arginine-13C6" + " (assumed variable)";
            }
            if (!modProfile.contains("heavy lysine-13C6")) {
                modProfile.addVariableModification(getPTM("heavy lysine-13C6"));
                prideParametersReport += "<br>" + "heavy lysine-13C6" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("Acetyl")
                || pridePtmName.equalsIgnoreCase("N-acetylated residue")
                || pridePtmName.equalsIgnoreCase("N-acylated residue")
                || pridePtmName.equalsIgnoreCase("acetylated residue")) {

            if (!modProfile.contains("acetylation of k")) {
                modProfile.addVariableModification(getPTM("acetylation of k"));
                prideParametersReport += "<br>" + "acetylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("acetylation of protein n-term")) {
                modProfile.addVariableModification(getPTM("acetylation of protein n-term"));
                prideParametersReport += "<br>" + "acetylation of protein n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("carbamoylated residue")) {

            if (!modProfile.contains("carbamylation of k")) {
                modProfile.addVariableModification(getPTM("carbamylation of k"));
                prideParametersReport += "<br>" + "carbamylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("carbamylation of n-term peptide")) {
                modProfile.addVariableModification(getPTM("carbamylation of n-term peptide"));
                prideParametersReport += "<br>" + "carbamylation of n-term peptide" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("dimethylated residue")) {

            if (!modProfile.contains("di-methylation of k")) {
                modProfile.addVariableModification(getPTM("di-methylation of k"));
                prideParametersReport += "<br>" + "di-methylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("di-methylation of r")) {
                modProfile.addVariableModification(getPTM("di-methylation of r"));
                prideParametersReport += "<br>" + "di-methylation of r" + " (assumed variable)";
            }
            if (!modProfile.contains("di-methylation of peptide n-term")) {
                modProfile.addVariableModification(getPTM("di-methylation of peptide n-term"));
                prideParametersReport += "<br>" + "di-methylation of peptide n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("trimethylated residue")) {

            if (!modProfile.contains("tri-methylation of k")) {
                modProfile.addVariableModification(getPTM("tri-methylation of k"));
                prideParametersReport += "<br>" + "tri-methylation of k" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-methylation of r")) {
                modProfile.addVariableModification(getPTM("tri-methylation of r"));
                prideParametersReport += "<br>" + "tri-methylation of r" + " (assumed variable)";
            }
            if (!modProfile.contains("tri-methylation of protein n-term")) {
                modProfile.addVariableModification(getPTM("tri-methylation of protein n-term"));
                prideParametersReport += "<br>" + "tri-methylation of protein n-term" + " (assumed variable)";
            }

        } else if (pridePtmName.equalsIgnoreCase("No PTMs are included in the dataset")) {
            // ignore
        } else {

            // single ptm mapping
            String utilitiesPtmName = convertPridePtmToUtilitiesPtm(pridePtmName);

            if (utilitiesPtmName != null) {
                if (!modProfile.contains(utilitiesPtmName)) {
                    if (isFixed) {
                        modProfile.addFixedModification(getPTM(utilitiesPtmName));
                        prideParametersReport += "<br>" + utilitiesPtmName + " (assumed fixed)";
                    } else {
                        modProfile.addVariableModification(getPTM(utilitiesPtmName));
                        prideParametersReport += "<br>" + utilitiesPtmName + " (assumed variable)";
                    }
                }
            } else {
                if (!unknownPtms.contains(pridePtmName)) {
                    unknownPtms.add(pridePtmName);
                }
            }
        }

        return prideParametersReport;
    }

    /**
     * Tries to convert a PRIDE PTM name to utilities PTM name.
     *
     * @param pridePtmName the PRIDE PTM name
     * @return the utilities PTM name, or null if there is no mapping
     */
    private String convertPridePtmToUtilitiesPtm(String pridePtmName) {

        // @TODO: check for more unmapped ptms! everything used in getDefaultCVTerm in PtmToPrideMap should be mapped back as well!!
        if (pridePtmName.equalsIgnoreCase("Carbamidomethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxamidomethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetamide - site C")
                || pridePtmName.equalsIgnoreCase("iodoacetamide derivatized residue")
                || pridePtmName.equalsIgnoreCase("Iodoacetamide derivative")) {
            return "carbamidomethyl c";
        } else if (pridePtmName.equalsIgnoreCase("Oxidation")
                || pridePtmName.equalsIgnoreCase("monohydroxylated residue")
                || pridePtmName.equalsIgnoreCase("oxidized residue")) {
            return "oxidation of m";
        } else if (pridePtmName.equalsIgnoreCase("Acetylation")) {
            return "acetylation of k";
        } else if (pridePtmName.equalsIgnoreCase("Amidation")) {
            return "amidation of peptide c-term";
        } else if (pridePtmName.equalsIgnoreCase("Carboxymethyl")
                || pridePtmName.equalsIgnoreCase("S-carboxymethyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("iodoacetic acid derivatized residue")) {
            return "carboxymethyl c";
        } else if (pridePtmName.equalsIgnoreCase("Farnesylation")) {
            return "farnesylation of c";
        } else if (pridePtmName.equalsIgnoreCase("Geranyl-geranyl")) {
            return "geranyl-geranyl";
        } else if (pridePtmName.equalsIgnoreCase("Guanidination")) {
            return "guanidination of k";
        } else if (pridePtmName.equalsIgnoreCase("Homoserine")) {
            return "homoserine";
        } else if (pridePtmName.equalsIgnoreCase("Homoserine lactone")) {
            return "homoserine lactone";
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C")
                || pridePtmName.equalsIgnoreCase("Applied Biosystems cleavable ICAT(TM) light")) {
            return "icat light";
        } else if (pridePtmName.equalsIgnoreCase("ICAT-C:13C(9)")
                || pridePtmName.equalsIgnoreCase("Applied Biosystems cleavable ICAT(TM) heavy")) {
            return "icat heavy";
        } else if (pridePtmName.equalsIgnoreCase("Lipoyl")) {
            return "lipoyl k";
        } else if (pridePtmName.equalsIgnoreCase("Methylthio")) {
            return "beta-methylthiolation of d (duplicate of 13)";
        } else if (pridePtmName.equalsIgnoreCase("NIPCAM(C)")) {
            return "nipcam";
        } else if (pridePtmName.equalsIgnoreCase("Phosphopantetheine")) {
            return "phosphopantetheine s";
        } else if (pridePtmName.equalsIgnoreCase("Propionamide(C)")
                || pridePtmName.equalsIgnoreCase("Acrylamide adduct")) {
            return "propionamide c";
        } else if (pridePtmName.equalsIgnoreCase("Pyridylethyl")) {
            return "s-pyridylethylation of c";
        } else if (pridePtmName.equalsIgnoreCase("Pyridylethyl")) {
            return "s-pyridylethylation of c";
        } else if (pridePtmName.equalsIgnoreCase("Sulfo")
                || pridePtmName.equalsIgnoreCase("sulfated residue")) {
            return "sulfation of y"; // not completely sure about this one...
        } else if (pridePtmName.equalsIgnoreCase("Dehydratation")) {
            return "dehydro of s and t";
        } else if (pridePtmName.equalsIgnoreCase("Deamination")
                || pridePtmName.equalsIgnoreCase("Deamidated")
                || pridePtmName.equalsIgnoreCase("deamidated L-glutamine")
                || pridePtmName.equalsIgnoreCase("deamidated residue")
                || pridePtmName.equalsIgnoreCase("deaminated residue")) {
            return "deamidation of n and q"; // note that this does not separate between deamidation on only n and deamidation on n and q
        } else if (pridePtmName.equalsIgnoreCase("Dioxidation")) {
            return "sulphone of m";
        } else if (pridePtmName.equalsIgnoreCase("(18)O label at both C-terminal oxygens")) {
            return "di-o18 on peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("(18)O monosubstituted residue")) {
            return "o18 on peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("(4,4,5,5-(2)H4)-L-lysine")) {
            return "heavy lysine - 2h4";
        } else if (pridePtmName.equalsIgnoreCase("2-pyrrolidone-5-carboxylic acid (Gln)")
                || pridePtmName.equalsIgnoreCase("Ammonia-loss")) {
            return "pyro-glu from n-term q";
        } else if (pridePtmName.equalsIgnoreCase("2-pyrrolidone-5-carboxylic acid (Glu)")
                || pridePtmName.equalsIgnoreCase("Glu->pyro-Glu")) {
            return "pyro-glu from n-term e";
        } else if (pridePtmName.equalsIgnoreCase("3-hydroxy-L-proline")) {
            return "hydroxylation of p";
        } else if (pridePtmName.equalsIgnoreCase("3x(2)H labeled L-aspartic acid 4-methyl ester")) {
            return "tri-deuteromethylation of d";
        } else if (pridePtmName.equalsIgnoreCase("4x(2)H labeled alpha-dimethylamino N-terminal residue")) {
            return "chd2-di-methylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("4x(2)H labeled dimethylated L-lysine")) {
            return "chd2-di-methylation of k";
        } else if (pridePtmName.equalsIgnoreCase("5-methyl-L-arginine")) {
            return "methyl r";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled L-arginine")
                || pridePtmName.equalsIgnoreCase("6x(13)C,4x(15)N labeled L-arginine")) {
            return "heavy arginine-13C6";
        } else if (pridePtmName.equalsIgnoreCase("6x(13)C labeled L-lysine")
                || pridePtmName.equalsIgnoreCase("6x(13)C,2x(15)N labeled L-lysine")) {
            return "heavy lysine-13C6";
        } else if (pridePtmName.equalsIgnoreCase("L-aspartic acid 4-methyl ester")) {
            return "methyl ester of d";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteic acid (L-cysteine sulfonic acid)")) {
            return "oxidation of C to cysteic acid";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteine glutathione disulfide")) {
            return "glutathione disulfide";
        } else if (pridePtmName.equalsIgnoreCase("L-cysteine methyl disulfide")
                || pridePtmName.equalsIgnoreCase("methyl methanethiosulfonate")) {
            return "mmts on c";
        } else if (pridePtmName.equalsIgnoreCase("L-cystine (cross-link)")) {
            return "2-amino-3-oxo-butanoic acid t";
        } else if (pridePtmName.equalsIgnoreCase("L-glutamic acid 5-methyl ester (Glu)")
                || pridePtmName.equalsIgnoreCase("methylated glutamic acid")) {
            return "methylation of e";
        } else if (pridePtmName.equalsIgnoreCase("L-homoarginine")) {
            return "guanidination of k";
        } else if (pridePtmName.equalsIgnoreCase("L-methionine (R)-sulfoxide")
                || pridePtmName.equalsIgnoreCase("L-methionine (S)-sulfoxide")
                || pridePtmName.equalsIgnoreCase("L-methionine sulfoxide")) {
            return "oxidation of m";
        } else if (pridePtmName.equalsIgnoreCase("L-methionine sulfone")) {
            return "sulphone of m";
        } else if (pridePtmName.equalsIgnoreCase("N-acetyl-L-asparagine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-cysteine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-glutamic acid")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-isoleucine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-serine")
                || pridePtmName.equalsIgnoreCase("N-acetyl-L-tyrosine")) {
            return "acetylation of protein n-term";
        } else if (pridePtmName.equalsIgnoreCase("N-acetylated L-lysine")
                || pridePtmName.equalsIgnoreCase("N6-acetyl-L-lysine")) {
            return "acetylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N-ethylmaleimide derivatized cysteine")) {
            return "nem c";
        } else if (pridePtmName.equalsIgnoreCase("N-formyl-L-methionine")) {
            return "n-formyl met addition";
        } else if (pridePtmName.equalsIgnoreCase("N-formylated residue")) {
            return "formylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("N-methyl-L-serine")) {
            return "methyl ester of s";
        } else if (pridePtmName.equalsIgnoreCase("N2-acetyl-L-tryptophan")) {
            return "acetylation of protein n-term";
        } else if (pridePtmName.equalsIgnoreCase("N6,N6-dimethyl-L-lysine")) {
            return "di-methylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N6-formyl-L-lysine")) {
            return "formylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N6-methyl-L-lysine")
                || pridePtmName.equalsIgnoreCase("methylated lysine")
                || pridePtmName.equalsIgnoreCase("monomethylated L-lysine")) {
            return "methylation of k";
        } else if (pridePtmName.equalsIgnoreCase("N6-propanoyl-L-lysine")) {
            return "propionyl light k";
        } else if (pridePtmName.equalsIgnoreCase("O-(N-acetylamino)glucosyl-L-serine")) {
            return "serine hexnac";
        } else if (pridePtmName.equalsIgnoreCase("O-(N-acetylamino)glucosyl-L-threonine")) {
            return "threonine hexnac";
        } else if (pridePtmName.equalsIgnoreCase("O-phospho-L-serine")) {
            return "phosphorylation of s";
        } else if (pridePtmName.equalsIgnoreCase("O-phospho-L-threonine")) {
            return "phosphorylation of t";
        } else if (pridePtmName.equalsIgnoreCase("O4&apos;-phospho-L-tyrosine")) {
            return "phosphorylation of y";
        } else if (pridePtmName.equalsIgnoreCase("S-carboxamidoethyl-L-cysteine")) {
            return "propionamide c";
        } else if (pridePtmName.equalsIgnoreCase("S-methyl-L-cysteine")) {
            return "methyl c";
        } else if (pridePtmName.equalsIgnoreCase("alpha-amino acetylated residue")) {
            return "acetylation of protein n-term";
        } else if (pridePtmName.equalsIgnoreCase("alpha-amino dimethylated residue")) {
            return "di-methylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("amidated residue")) {
            return "amidation of peptide c-term";
        } else if (pridePtmName.equalsIgnoreCase("deamidated L-asparagine")
                || pridePtmName.equalsIgnoreCase("deglycosylated asparagine")) {
            return "deamidation of n";
        } else if (pridePtmName.equalsIgnoreCase("dehydrated residue")) {
            return "dehydro of s and t";
        } else if (pridePtmName.equalsIgnoreCase("dihydroxylated residue - site W")) {
            return "oxidation of w to formylkynurenin";
        } else if (pridePtmName.equalsIgnoreCase("diiodinated residue")) {
            return "di-iodination of y";
        } else if (pridePtmName.equalsIgnoreCase("hydroxylated lysine")
                || pridePtmName.equalsIgnoreCase("monohydroxylated lysine")) {
            return "hydroxylation of k";
        } else if (pridePtmName.equalsIgnoreCase("iodoacetamide -site E")) {
            return "carboxyamidomethylation of e";
        } else if (pridePtmName.equalsIgnoreCase("iodoacetamide N6-derivatized lysine")) {
            return "carboxyamidomethylation of k";
        } else if (pridePtmName.equalsIgnoreCase("monomethylated L-aspartic acid")) {
            return "methylation of d";
        } else if (pridePtmName.equalsIgnoreCase("thioacylation of primary amines - site N-term")) {
            return "gammathiopropionylation of peptide n-term";
        } else if (pridePtmName.equalsIgnoreCase("ubiquitination signature dipeptidyl lysine")) {
            return "ubiquitinylation residue";
        } else {
            return null;
        }
    }

    /**
     * Returns the folder where the factory is saved.
     *
     * @return the folder where the factory is saved
     */
    public static String getSerializationFolder() {
        return SERIALIZATION_FILE_FOLDER;
    }

    /**
     * Sets the folder where the factory is saved.
     *
     * @param serializationFolder the folder where the factory is saved
     */
    public static void setSerializationFolder(String serializationFolder) {
        PTMFactory.SERIALIZATION_FILE_FOLDER = serializationFolder;
    }
}
