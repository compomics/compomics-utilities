package com.compomics.util.experiment.biology;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Aug 23, 2010
 * Time: 7:30:55 PM
 * This factory will provide the implemented enzymes.
 */
public class EnzymeFactory {

    /*
     * The imported enzymes
     */
    private ArrayList<Enzyme> enzymes = null;

    private static EnzymeFactory instance = null;

    private EnzymeFactory() {

    }

    public static EnzymeFactory getInstance() {
        if (instance == null) {
            instance = new EnzymeFactory();
        }
        return instance;
    }

    /**
     * Get the imported enzymes
     *
     * @return          The enzymes as ArrayList
     */
    public ArrayList<Enzyme> getEnzymes() {
        return enzymes;
    }

    /**
     * Import enzymes
     *
     * @param   enzymeFile   xml file containing the enzymes
     * @throws  XmlPullParserException  when the parser failed
     * @throws  IOException when reading the corresponding file failed
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

        enzymes = new ArrayList<Enzyme>();
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
     * Parse one enzyme
     *
     * @param   aParser   xml parser
     * @throws  XmlPullParserException  when the parser failed
     * @throws  IOException when reading the corresponding file failed
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
        enzymes.add(new Enzyme(id, name, aaBefore, restrictionBefore, aaAfter, restrictionAfter));
    }

}
