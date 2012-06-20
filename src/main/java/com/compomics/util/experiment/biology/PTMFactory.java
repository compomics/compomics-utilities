package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.ArrayList;
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
     * User ptm file.
     */
    private static final String SERIALIZATION_FILE = System.getProperty("user.home") + "/.compomics/ptmFactory.cus";
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
     * Map of omssa indexes.
     */
    private HashMap<String, ArrayList<Integer>> omssaIndexes = new HashMap<String, ArrayList<Integer>>();
    /**
     * Unknown modification to be returned when the modification is not found.
     */
    public static final PTM unknownPTM = new PTM(PTM.MODAA, "unknown", 0, new ArrayList<String>());

    /**
     * Constructor for the factory.
     */
    private PTMFactory() {
        ptmMap.put(unknownPTM.getName(), unknownPTM);
        defaultMods.add("unknown");
    }

    /**
     * Static method to get the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static PTMFactory getInstance() {
        if (instance == null) {
            try {
                File savedFile = new File(SERIALIZATION_FILE);
                FileInputStream fis = new FileInputStream(savedFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream in = new ObjectInputStream(bis);
                Object factory = in.readObject();
                fis.close();
                bis.close();
                in.close();
                instance = (PTMFactory) factory;
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
        File factoryFile = new File(SERIALIZATION_FILE);
        if (!factoryFile.getParentFile().exists()) {
            factoryFile.getParentFile().mkdir();
        }
        FileOutputStream fos = new FileOutputStream(factoryFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(instance);
        oos.close();
        bos.close();
        fos.close();
    }

    /**
     * Get a PTM according to its omssa index.
     *
     * @param index the PTM index
     * @return the selected PTM
     */
    public PTM getPTM(int index) {
        String name = null;
        for (String ptm : omssaIndexes.keySet()) {
            if (omssaIndexes.get(ptm).contains(index)) {
                name = ptm;
                break;
            }
        }
        if (name != null) {
            return ptmMap.get(name);
        }
        return unknownPTM;
    }

    /**
     * Replaces an old ptm by a new.
     *
     * @param oldName the name of the old ptm
     * @param newPTM the new ptm
     */
    public void replacePTM(String oldName, PTM newPTM) {
        String newName = newPTM.getName();
        if (userMods.contains(oldName)) {
            userMods.remove(oldName);
            if (!userMods.contains(newName)) {
                userMods.add(newName);
            }
        }
        if (defaultMods.contains(oldName)) {
            defaultMods.remove(oldName);
            if (!defaultMods.contains(newName)) {
                defaultMods.add(newName);
            }
        }
        if (!oldName.equals(newName)) {
            ptmMap.remove(oldName);
        }
        ptmMap.put(newName, newPTM);
        if (!newName.equals(oldName)) {
            if (!omssaIndexes.containsKey(newName)) {
                omssaIndexes.put(newName, new ArrayList<Integer>());
            }
            omssaIndexes.get(newName).addAll(omssaIndexes.get(oldName));
            omssaIndexes.remove(oldName);
        }
    }

    /**
     * Adds a new user modification.
     *
     * @param ptm the new modification to add
     */
    public void addUserPTM(PTM ptm) {
        ptmMap.put(ptm.getName(), ptm);
        userMods.add(ptm.getName());
        setUserOmssaIndexes();
    }

    /**
     * Sets the omssa indexes of all loaded user ptms.
     */
    private void setUserOmssaIndexes() {
        String ptm;
        for (int rank = 1; rank <= userMods.size(); rank++) {
            int omssaIndex = rank + 118;
            if (omssaIndex > 128) {
                omssaIndex += 13;
            }
            ptm = userMods.get(rank - 1);
            if (!omssaIndexes.containsKey(ptm)) {
                omssaIndexes.put(ptm, new ArrayList<Integer>());
            }
            omssaIndexes.get(ptm).add(omssaIndex);
        }
    }

    /**
     * Removes a user ptm.
     *
     * @param ptmName the name of the ptm to remove
     */
    public void removeUserPtm(String ptmName) {
        ptmMap.remove(ptmName);
        userMods.remove(ptmName);
        omssaIndexes.remove(ptmName);
        setUserOmssaIndexes(); // I'm too lazy to move indexes here so I recalculate all of them. Should not take long.
    }

    /**
     * Returns the PTM indexed by its name.
     *
     * @param name the name of the desired PTM
     * @return The desired PTM
     */
    public PTM getPTM(String name) {
        if (ptmMap.containsKey(name)) {
            return ptmMap.get(name);
        }
        if (name.indexOf("@") > 1) {
            try {
                double mass = new Double(name.substring(0, name.indexOf("@")));
                return new PTM(-1, name, mass, new ArrayList<String>());
            } catch (Exception e) {
                return unknownPTM;
            }
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
        return ptmMap.containsKey(name);
    }

    /**
     * Returns the index of the desired modification.
     *
     * @param modificationName the desired modification name to lower case
     * @return the corresponding index
     */
    public ArrayList<Integer> getOMSSAIndexes(String modificationName) {
        return omssaIndexes.get(modificationName);
    }

    /**
     * Getter for a ptm according to its measured characteristics /!\ This
     * method can generate inconsistent results in case a measurement matches to
     * various PTMs.
     *
     * @param mass the measured mass induced by the modification
     * @param location the modification location
     * @param sequence the peptide sequence
     * @return the candidate modification, null if none is found
     */
    public PTM getPTM(double mass, String location, String sequence) {
        for (PTM currentPTM : ptmMap.values()) {
            if (currentPTM.getType() == PTM.MODAA
                    || currentPTM.getType() == PTM.MODCAA
                    || currentPTM.getType() == PTM.MODCPAA
                    || currentPTM.getType() == PTM.MODNAA
                    || currentPTM.getType() == PTM.MODNPAA) {
                if (Math.abs(currentPTM.getMass() - mass) < 0.01) {
                    for (String residue : currentPTM.getResidues()) {
                        if (location.equals(residue)) {
                            return currentPTM;
                        }
                    }
                }
            } else if (currentPTM.getType() == PTM.MODC || currentPTM.getType() == PTM.MODCP) {
                if (Math.abs(currentPTM.getMass() - mass) < 0.01 && sequence.endsWith(location)) {
                    return currentPTM;
                }
            } else if (currentPTM.getType() == PTM.MODN || currentPTM.getType() == PTM.MODNP) {
                if (Math.abs(currentPTM.getMass() - mass) < 0.01 && sequence.startsWith(location)) {
                    return currentPTM;
                }
            }
        }
        return unknownPTM;
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
                parseMSModSpec(parser, userMod);
            }
            type = parser.next();
        }
        br.close();
        setUserOmssaIndexes();
        setDefaultNeutralLosses();
        setDefaultReporterIons();
    }

    /**
     * This method parses a single MSModSpec tag's contents from the
     * modifications XML file. It expects the XmlPullParser to be set on the
     * starting 'MSModSpec' tag, and upon completion, the parser will be set on
     * the closing 'MSModSpec' tag.
     *
     * @throws XmlPullParserException when the pull parser failed.
     * @throws IOException when the pull parser could not access the underlying
     * file.
     */
    private void parseMSModSpec(XmlPullParser parser, boolean userMod) throws XmlPullParserException, IOException {
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
        String name = parser.getText().trim();
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
        if (modType.compareTo("modc") == 0 || modType.compareTo("modcp") == 0 || modType.compareTo("modcaa") == 0 || modType.compareTo("modcpaa") == 0) {
            residues.add("]");
        }
        if (modType.compareTo("modn") == 0 || modType.compareTo("modnp") == 0 || modType.compareTo("modnaa") == 0 || modType.compareTo("modnpaa") == 0) {
            residues.add("[");
        }
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
            residues.addAll(aminoAcids);
        }
        // Create and implement modification.
        PTM currentPTM = new PTM(getIndex(modType), name.toLowerCase(), new Double(mass), residues);

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
                        throw new XmlPullParserException("Found non-parseable text '" + doubleString + "' for the value of the 'MSMassSet_monomass' neutral loss tag on line " + parser.getLineNumber() + ".");
                    }
                    cpt++;
                }
            }
            currentPTM.setNeutralLosses(neutralLosses);
        }
        while (!(type == XmlPullParser.END_TAG && parser.getName().equals("MSModSpec"))) {
            type = parser.next();
        }

        if (!currentPTM.getName().startsWith("user modification ")) {
            ptmMap.put(currentPTM.getName(), currentPTM);
            if (userMod) {
                if (!userMods.contains(currentPTM.getName())) {
                    userMods.add(currentPTM.getName());
                }
            } else {
                if (!defaultMods.contains(currentPTM.getName())) {
                    defaultMods.add(currentPTM.getName());
                }
                omssaIndexes.put(currentPTM.getName(), new ArrayList<Integer>());
                omssaIndexes.get(currentPTM.getName()).add(number);
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
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(aFolder, "mods.xml")));
        while ((c = br.read()) != -1) {
            bw.write(c);
        }
        bw.flush();
        bw.close();
        br.close();

        br = new BufferedReader(new FileReader(utilitiesUserModFile));
        bw = new BufferedWriter(new FileWriter(new File(aFolder, "usermods.xml")));
        while ((c = br.read()) != -1) {
            bw.write(c);
        }
        bw.flush();
        bw.close();
        br.close();
    }

    /**
     * Writes the omssa modification file corresponding to the PTMs loaded in
     * the factory in the given file.
     *
     * @param file the file
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    public void writeOmssaUserModificationFile(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        String toWrite = "<?xml version=\"1.0\"?>\n<MSModSpecSet\n"
                + "xmlns=\"http://www.ncbi.nlm.nih.gov\"\n"
                + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "xs:schemaLocation=\"http://www.ncbi.nlm.nih.gov OMSSA.xsd\"\n>\n\n";
        bw.write(toWrite);

        for (int cpt = 1; cpt <= userMods.size(); cpt++) {
            String ptmName = userMods.get(cpt - 1);
            toWrite = getOmssaUserModBloc(ptmName, cpt);
            bw.write(toWrite);
        }
        
        for (int cpt = userMods.size() + 1; cpt <= 30; cpt++) {
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
        }
        toWrite = "</MSModSpecSet>";
        bw.write(toWrite);
        bw.flush();
        bw.close();
    }

    /**
     * Returns an MSModSpec bloc as present in the OMSSA user modification files
     * for a given PTM.
     *
     * @param ptmName the name of the PTM
     * @param cpt the index of this PTM
     * @return a string containing the xml bloc
     */
    public String getOmssaUserModBloc(String ptmName, int cpt) {
        int omssaIndex = cpt + 118;
        if (omssaIndex > 128) {
            omssaIndex += 13;
        }
        PTM ptm = ptmMap.get(ptmName);

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
            for (String aa : ptm.getResidues()) {
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
     * Returns the names of the user defined modifications.
     *
     * @return the names of the user defined modifications
     */
    public ArrayList<String> getUserModifications() {
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
     * Convenience method returning a boolean indicating whether a ptm is user
     * defined or default.
     *
     * @param ptmName 
     * @return boolean indicating whether a ptm is user
     */
    public boolean isUserDefined(String ptmName) {
        return !defaultMods.contains(ptmName);
    }

    /**
     * Sets the default neutral losses of PTMs when not implemented.
     */
    public void setDefaultNeutralLosses() {
        PTM ptm;
        boolean changed = false;
        for (String ptmName : defaultMods) {
            // I hate hard coding this, any more elegant approach welcome...
            if (ptmName.contains("phospho")) {
                ptm = ptmMap.get(ptmName);
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
                ptm = ptmMap.get(ptmName);
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
        PTM ptm;
        boolean changed = false;
        for (String ptmName : defaultMods) {
            // I hate hard coding this, any more elegant approach welcome...
            if (ptmName.contains("itraq")) {
                ptm = ptmMap.get(ptmName);
                if (ptm.getReporterIons().isEmpty()) {
                    changed = true;
                    ptm.addReporterIon(ReporterIon.iTRAQ114);
                    ptm.addReporterIon(ReporterIon.iTRAQ115);
                    ptm.addReporterIon(ReporterIon.iTRAQ116);
                    ptm.addReporterIon(ReporterIon.iTRAQ117);
                    ptm.addReporterIon(ReporterIon.iTRAQ113);
                    ptm.addReporterIon(ReporterIon.iTRAQ118);
                    ptm.addReporterIon(ReporterIon.iTRAQ119);
                    ptm.addReporterIon(ReporterIon.iTRAQ121);
                }
            } else if (ptmName.contains("tmt")) {
                changed = true;
                ptm = ptmMap.get(ptmName);
                if (ptm.getReporterIons().isEmpty()) {
                    ptm.addReporterIon(ReporterIon.TMT0);
                    ptm.addReporterIon(ReporterIon.TMT1);
                    if (ptmName.contains("6")) {
                        ptm.addReporterIon(ReporterIon.TMT2);
                        ptm.addReporterIon(ReporterIon.TMT3);
                        ptm.addReporterIon(ReporterIon.TMT4);
                        ptm.addReporterIon(ReporterIon.TMT5);
                    }
                }
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
