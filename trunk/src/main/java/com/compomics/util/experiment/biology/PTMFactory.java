package com.compomics.util.experiment.biology;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 8:26:09 PM
 * This factory will load PTM from an XML file and provide them on demand as standard class.
 */
public class PTMFactory {

    private static PTMFactory instance = null;

    private XmlPullParser parser;

    private HashMap<String, PTM> mascotNameToPTMMap = new HashMap<String, PTM>();
    private HashMap<Integer, PTM> indexToPTMMap = new HashMap<Integer, PTM>();

    private HashSet<PTM> ptmSet = new HashSet<PTM>();


    private PTMFactory() {
    }

    public static PTMFactory getInstance() {
        if (instance == null) {
            instance = new PTMFactory();
        }
        return instance;
    }

    public PTM getPTM(int index) {
        return indexToPTMMap.get(index);
    }

    public PTM getPTM(double mass, String location, String sequence) {
        for (PTM currentPTM : ptmSet) {
            if (currentPTM.getType() == PTM.MODAA
                    || currentPTM.getType() == PTM.MODCAA
                    || currentPTM.getType() == PTM.MODCPAA
                    || currentPTM.getType() == PTM.MODNAA
                    || currentPTM.getType() == PTM.MODNPAA) {
                if (Math.abs(currentPTM.getMass() - mass) < 0.01) {
                    for (String residue : currentPTM.getResiduesArray()) {
                        if (location.equals(residue)) {
                            return currentPTM;
                        }
                    }
                }
            } else if (currentPTM.getType() == PTM.MODC) {
                if (Math.abs(currentPTM.getMass() - mass) < 0.01 && sequence.endsWith(location)) {
                    return currentPTM;
                }
            } else if (currentPTM.getType() == PTM.MODN) {
                if (Math.abs(currentPTM.getMass() - mass) < 0.01 && sequence.startsWith(location)) {
                    return currentPTM;
                }
            }
        }
        return null;
    }

    public PTM getPTMFromMascotName(String aMascotName) {
        return mascotNameToPTMMap.get(aMascotName);
    }

    public Iterator<PTM> getPtmIterator() {
        return ptmSet.iterator();
    }

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
                parseMSModSpec(parser);
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
     * @param aParser XmlPullParser to parse the tag's content from. Should be set on
     *                the starting 'MSModSpec' tag
     * @return SearchFreeModification  with the parsed modification.
     * @throws XmlPullParserException when the pull parser failed.
     * @throws IOException            when the pull parser could not access the underlying file.
     */
    private void parseMSModSpec(XmlPullParser aParser) throws XmlPullParserException, IOException {
        // Check whether the XmlPullParser is correctly positioned (i.e. directly on the 'MSModSpec' start tag)
        if (!(aParser.getName().equals("MSModSpec") && aParser.getEventType() == XmlPullParser.START_TAG)) {
            throw new IllegalArgumentException("XmlPullParser should have been on the start tag for 'MSModSpec', but was on '" + aParser.getName() + "' instead!");
        }
        // Structure now is: tag, whitespace, tag, NUMBER (which we need).
        // Start tag.
        aParser.nextTag();
        // Validate correctness.
        if (!aParser.getName().equals("MSModSpec_mod")) {
            throw new XmlPullParserException("Found tag '" + parser.getName() + "' where 'MSModSpec_mod' was expected on line " + parser.getLineNumber() + "!");
        }
        aParser.nextTag();
        // Validate correctness.
        if (!aParser.getName().equals("MSMod")) {
            throw new XmlPullParserException("Found tag '" + parser.getName() + "' where 'MSMod' was expected on line " + parser.getLineNumber() + "!");
        }
        // We need the value here.
        aParser.next();
        String numberString = aParser.getText();
        int number = -1;
        try {
            number = Integer.parseInt(numberString);
        } catch (NumberFormatException nfe) {
            throw new XmlPullParserException("Found non-parseable text '" + numberString + "' for the value of the 'MSMod' tag on line " + parser.getLineNumber() + "!");
        }
        // Modification type
        int type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("MSModType"))) {
            type = aParser.next();
        }
        String modType = aParser.getAttributeValue(0);
        // OK, we got the number. Progress to the user-readable name.
        type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("MSModSpec_name"))) {
            type = aParser.next();
        }
        // Right, we should be on the right start tag, so get the value.
        aParser.next();
        String name = aParser.getText().trim();
        type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("MascotShortType"))) {
            type = aParser.next();
        }
        // Right, we should be on the right start tag, so get the value.
        aParser.next();
        String mascotName = aParser.getText().trim();
        // Mass
        type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("MSModSpec_monomass"))) {
            type = aParser.next();
        }
        aParser.next();
        String mass = aParser.getText().trim();
        // Residue
        type = aParser.next();
        ArrayList<String> residues = new ArrayList();
        if (modType.compareTo("modc") == 0 || modType.compareTo("modcp") == 0 || modType.compareTo("modcaa") == 0 || modType.compareTo("modcpaa") == 0) {
            residues.add("]");
        }
        if (modType.compareTo("modn") == 0 || modType.compareTo("modnp") == 0 || modType.compareTo("modnaa") == 0 || modType.compareTo("modnpaa") == 0) {
            residues.add("[");
        }
        if (modType.compareTo("modcaa") == 0 || modType.compareTo("modcpaa") == 0 || modType.compareTo("modnaa") == 0 || modType.compareTo("modnpaa") == 0 || modType.compareTo("modaa") == 0) {
            while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("MSModSpec_residues_E"))) {
                type = aParser.next();
            }
            ArrayList<String> aminoAcids = new ArrayList();
            while (type == XmlPullParser.START_TAG && aParser.getName().equals("MSModSpec_residues_E")) {
                aParser.next();
                aminoAcids.add(aParser.getText().trim());
                aParser.next();
                type = aParser.next();
                type = aParser.next();
            }
            if (aminoAcids.size() > 1) {
                residues.add("[");
            }
            residues.addAll(aminoAcids);
            if (aminoAcids.size() > 1) {
                residues.add("]");
            }
        }
        String[] residuesArray = new String[residues.size()];
        residues.toArray(residuesArray);
        // Move the parser to the end tag of this modification.
        type = aParser.next();
        while (!(type == XmlPullParser.END_TAG && aParser.getName().equals("MSModSpec"))) {
            type = aParser.next();
        }

        // Create and implement modification.
        PTM currentPTM = new PTM(getIndex(modType), name, new Double(mass), residuesArray);
        ptmSet.add(currentPTM);
        mascotNameToPTMMap.put(mascotName, currentPTM);
        indexToPTMMap.put(number, currentPTM);
    }

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
}
