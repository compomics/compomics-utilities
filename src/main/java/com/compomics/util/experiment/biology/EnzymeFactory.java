package com.compomics.util.experiment.biology;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * This factory will provide the implemented enzymes.
 *
 * @author Marc Vaudel
 */
public class EnzymeFactory {

    /**
     * The imported enzymes.
     */
    private static HashMap<String, Enzyme> enzymes = null;
    /**
     * The instance of the factory.
     */
    private static EnzymeFactory instance = null;

    /**
     * The factory constructor.
     */
    private EnzymeFactory() {
    }

    /**
     * Static method to get an instance of the factory.
     *
     * @return the factory instance
     */
    public static EnzymeFactory getInstance() {
        if (instance == null) {
            instance = new EnzymeFactory();
        }
        return instance;
    }

    /**
     * Get the imported enzymes.
     *
     * @return The enzymes as ArrayList
     */
    public ArrayList<Enzyme> getEnzymes() {
        return new ArrayList<Enzyme>(enzymes.values());
    }

    /**
     * Returns the enzyme corresponding to the given name. Null if not found.
     *
     * @param enzymeName the name of the desired enzyme
     * @return the corresponding enzyme
     */
    public Enzyme getEnzyme(String enzymeName) {
        return enzymes.get(enzymeName);
    }

    /**
     * Adds an enzyme in the factory.
     *
     * @param enzyme the new enzyme to add
     */
    public void addEnzyme(Enzyme enzyme) {
        enzymes.put(enzyme.getName(), enzyme);
    }

    /**
     * Indicates whether an enzyme is loaded in the factory.
     *
     * @param enzyme the name of the enzyme
     * @return a boolean indicating whether an enzyme is loaded in the factory
     */
    public boolean enzymeLoaded(String enzyme) {
        return enzymes.containsKey(enzyme);
    }

    /**
     * Import enzymes.
     *
     * @param enzymeFile xml file containing the enzymes
     * @throws XmlPullParserException when the parser failed
     * @throws IOException when reading the corresponding file failed
     */
    public void importEnzymes(File enzymeFile) throws XmlPullParserException, IOException {

        // Create the pull parser.
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        // Create a reader for the input file.
        BufferedReader br = new BufferedReader(new FileReader(enzymeFile));
        // Set the XML Pull Parser to read from this reader.
        parser.setInput(br);
        // Start the parsing.
        int type = parser.next();

        enzymes = new HashMap<String, Enzyme>();
        // Go through the whole document.
        while (type != XmlPullParser.END_DOCUMENT) {
            // If we find a 'MSModSpec' start tag,
            // we should parse the mod.
            if (type == XmlPullParser.START_TAG && parser.getName().equals("enzyme")) {
                parseEnzyme(parser);
            }
            type = parser.next();
        }
        br.close();
    }

    /**
     * Parse one enzyme.
     *
     * @param aParser xml parser
     * @throws XmlPullParserException when the parser failed
     * @throws IOException when reading the corresponding file failed
     */
    private void parseEnzyme(XmlPullParser aParser) throws XmlPullParserException, IOException {
        int id;
        String name, aaBefore, restrictionBefore, aaAfter, restrictionAfter;

        // Start tag.
        aParser.nextTag();
        // Validate correctness.
        if (!aParser.getName().equals("id")) {
            throw new XmlPullParserException("Found tag '" + aParser.getName() + "' where 'id' was expected on line " + aParser.getLineNumber() + ".");
        }
        aParser.next();
        String idString = aParser.getText();
        try {
            id = Integer.parseInt(idString.trim());
        } catch (NumberFormatException nfe) {
            throw new XmlPullParserException("Found non-parseable text '" + idString + "' for the value of the 'id' tag on line " + aParser.getLineNumber() + ".");
        }
        // OK, we got the id. Progress to the user-readable name.
        int type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("name"))) {
            type = aParser.next();
        }
        aParser.next();
        name = aParser.getText().trim();
        type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("aminoAcidBefore"))) {
            type = aParser.next();
        }
        aParser.next();
        aaBefore = aParser.getText().trim();
        type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("restrictionBefore"))) {
            type = aParser.next();
        }
        aParser.next();
        restrictionBefore = aParser.getText().trim();
        type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("aminoAcidAfter"))) {
            type = aParser.next();
        }
        aParser.next();
        aaAfter = aParser.getText().trim();
        type = aParser.next();
        while (!(type == XmlPullParser.START_TAG && aParser.getName().equals("restrictionAfter"))) {
            type = aParser.next();
        }
        aParser.next();
        restrictionAfter = aParser.getText().trim();
        enzymes.put(name, new Enzyme(id, name, aaBefore, restrictionBefore, aaAfter, restrictionAfter));
    }
}
