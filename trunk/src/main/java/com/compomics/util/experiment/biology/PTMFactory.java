package com.compomics.util.experiment.biology;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This factory will load PTM from an XML file and provide them on demand as standard class.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 8:26:09 PM
 */
public class PTMFactory {

    /**
     * Instance of the factory
     */
    private static PTMFactory instance = null;
    /**
     * Parser used to parse modification files
     */
    private XmlPullParser parser;
    /**
     * A map linking indexes with modifications
     */
    private HashMap<Integer, PTM> indexToPTMMap = new HashMap<Integer, PTM>();
    /**
     * A map linking modification names to their index
     */
    private HashMap<String, Integer> nameToIndexMap = new HashMap<String, Integer>();
    /**
     * The set of imported PTM
     */
    private HashSet<PTM> ptmSet = new HashSet<PTM>();
    /**
     * unknown modification to be returned when the modification is not found
     */
    private static final PTM unknownPTM = new PTM(PTM.MODAA, "unknown", 0, new ArrayList<String>());

    /**
     * Constructor for the factory
     */
    private PTMFactory() {
    }

    /**
     * Static method to get the instance of the factory
     *
     * @return the instance of the factory
     */
    public static PTMFactory getInstance() {
        if (instance == null) {
            instance = new PTMFactory();
        }
        return instance;
    }

    /**
     * get a PTM according to its index
     *
     * @param index the PTM index
     * @return the selected PTM
     */
    public PTM getPTM(int index) {
        if (indexToPTMMap.get(index) != null) {
            return indexToPTMMap.get(index);
        }
        return unknownPTM;
    }

    /**
     * replaces an old ptm by a new
     * @param oldName the name of the old ptm
     * @param newPTM  the new ptm
     */
    public void replacePTM(String oldName, PTM newPTM) {
        int index = nameToIndexMap.get(oldName);
        if (!oldName.equals(newPTM.getName())) {
            nameToIndexMap.put(newPTM.getName(), index);
            nameToIndexMap.remove(oldName);
        }
        PTM oldPtm = indexToPTMMap.get(index);
        indexToPTMMap.put(index, newPTM);
        ptmSet.remove(oldPtm);
        ptmSet.add(newPTM);
    }

    /**
     * Returns the PTM indexed by its name
     * @param name  the name of the desired PTM
     * @return      The desired PTM
     */
    public PTM getPTM(String name) {
        if (indexToPTMMap.get(nameToIndexMap.get(name)) != null) {
            return indexToPTMMap.get(nameToIndexMap.get(name));
        }
        if (name.indexOf("@")> 1) {
            try {
                double mass = new Double(name.substring(0, name.indexOf("@")));
            return new PTM(-1, name, mass, new ArrayList<String>());
            }catch (Exception e) {
                return unknownPTM;
            }
        }
        return unknownPTM;
    }

    /**
     * Returns the index of the desired modification
     * @param modificationName  the desired modification name to lower case
     * @return the corresponding index
     */
    public Integer getPTMIndex(String modificationName) {
        return nameToIndexMap.get(modificationName);
    }

    /**
     * getter for the index to PTM map
     *
     * @return the index to ptem map
     */
    public HashMap<Integer, PTM> getPtmMap() {
        return indexToPTMMap;
    }

    /**
     * getter for a ptm according to its measured characteristics
     * /!\ This method can generate inconsistent results in case a measurement matches to various PTMs.
     *
     * @param mass      the measured mass induced by the modification
     * @param location  the modification location
     * @param sequence  the peptide sequence
     * @return the candidate modification, null if none is found
     */
    public PTM getPTM(double mass, String location, String sequence) {
        for (PTM currentPTM : ptmSet) {
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
     * returns an iterator on the imported PTM
     *
     * @return an iterator on imported PTM
     */
    public Iterator<PTM> getPtmIterator() {
        return ptmSet.iterator();
    }

    /**
     * Import modifications from a modification file
     *
     * @param modificationsFile         A file containing modifications
     * @throws XmlPullParserException   exception thrown whenever an error is encountered while parsing
     * @throws IOException              exception thrown whenever an error is encountered reading the file
     */
    public void importModifications(File modificationsFile) throws XmlPullParserException, IOException {

        // Create the pull parser.
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        parser = factory.newPullParser();
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
                parseMSModSpec();
            }
            type = parser.next();
        }
        br.close();
    }

    /**
     * This method parses a single MSModSpec tag's contents from the modifications XML file.
     * It expects the XmlPullParser to be set on the starting 'MSModSpec' tag, and upon completion,
     * the parser will be set on the closing 'MSModSpec' tag.
     *
     * @throws XmlPullParserException when the pull parser failed.
     * @throws IOException            when the pull parser could not access the underlying file.
     */
    private void parseMSModSpec() throws XmlPullParserException, IOException {
        // Check whether the XmlPullParser is correctly positioned (i.e. directly on the 'MSModSpec' start tag)
        if (!(parser.getName().equals("MSModSpec") && parser.getEventType() == XmlPullParser.START_TAG)) {
            throw new IllegalArgumentException("XmlPullParser should have been on the start tag for 'MSModSpec', but was on '" + parser.getName() + "' instead!");
        }
        // Structure now is: tag, whitespace, tag, NUMBER (which we need).
        // Start tag.
        parser.nextTag();
        // Validate correctness.
        if (!parser.getName().equals("MSModSpec_mod")) {
            throw new XmlPullParserException("Found tag '" + parser.getName() + "' where 'MSModSpec_mod' was expected on line " + parser.getLineNumber() + "!");
        }
        parser.nextTag();
        // Validate correctness.
        if (!parser.getName().equals("MSMod")) {
            throw new XmlPullParserException("Found tag '" + parser.getName() + "' where 'MSMod' was expected on line " + parser.getLineNumber() + "!");
        }
        // We need the value here.
        parser.next();
        String numberString = parser.getText();
        int number = -1;
        try {
            number = Integer.parseInt(numberString);
        } catch (NumberFormatException nfe) {
            throw new XmlPullParserException("Found non-parseable text '" + numberString + "' for the value of the 'MSMod' tag on line " + parser.getLineNumber() + "!");
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
                type = parser.next();
                type = parser.next();
            }
            if (aminoAcids.size() > 1) {
                residues.add("[");
            }
            residues.addAll(aminoAcids);
            if (aminoAcids.size() > 1) {
                residues.add("]");
            }
        }
        // Move the parser to the end tag of this modification.
        type = parser.next();
        while (!(type == XmlPullParser.END_TAG && parser.getName().equals("MSModSpec"))) {
            type = parser.next();
        }

        // Create and implement modification.
        PTM currentPTM = new PTM(getIndex(modType), name.toLowerCase(), new Double(mass), residues);
        ptmSet.add(currentPTM);
        indexToPTMMap.put(number, currentPTM);
        nameToIndexMap.put(currentPTM.getName().toLowerCase(), number);
    }

    /**
     * get the index of a file
     *
     * @param modType   modification type found
     * @return corresponding static index
     */
    private int getIndex(String modType) {
        if (modType.compareTo("modaa") == 0) {
            return PTM.MODAA;
        } else if (modType.compareTo("modn") == 0) {
            return PTM.MODN;
        } else if (modType.compareTo("modc") == 0) {
            return PTM.MODC;
        } else if (modType.compareTo("modcpaa") == 0) {
            return PTM.MODCPAA;
        } else if (modType.compareTo("modcp") == 0) {
            return PTM.MODCP;
        } else if (modType.compareTo("modnp") == 0) {
            return PTM.MODNP;
        } else if (modType.compareTo("modnaa") == 0) {
            return PTM.MODNAA;
        } else if (modType.compareTo("modnpaa") == 0) {
            return PTM.MODNPAA;
        } else if (modType.compareTo("modcaa") == 0) {
            return PTM.MODCAA;
        } else if (modType.compareTo("modcpaa") == 0) {
            return PTM.MODCPAA;
        } else if (modType.compareTo("modnaa") == 0) {
            return PTM.MODNAA;
        } else if (modType.compareTo("modnpaa") == 0) {
            return PTM.MODCPAA;
        } else if (modType.compareTo("modcaa") == 0) {
            return PTM.MODCAA;
        }
        return -1;
    }

    /**
     * Write the OMSSA modification files to the given folder.
     *
     * @param aFolder the folder to write the modification files to
     * @param utilitiesModFile the utilities corresponding mod file
     * @param utilitiesUserModFile the utilities corresponding usermod file
     * @throws IOException an IOException is thrown in case an issue is encountered while reading or writing a file.
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
}
