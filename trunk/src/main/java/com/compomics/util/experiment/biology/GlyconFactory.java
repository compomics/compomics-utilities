package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.Glycon;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This factory will provide theoretic glycons.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 29, 2010
 * Time: 6:55:51 PM
 */
public class GlyconFactory {

    /**
     * The instance of the factory
     */
    private static GlyconFactory instance = null;

    /**
     * The glycons
     */
    private ArrayList<Glycon> glycons = new ArrayList<Glycon>();

    /**
     * Constructor for the glycon factory
     */
    private GlyconFactory() {

    }

    /**
     * Static method to get the factory instance.
     *
     * @return the instance of the factory
     */
    public static GlyconFactory getInstance() {
        if (instance == null) {
            instance = new GlyconFactory();
        }
        return instance;
    }

    /**
     * A getter to access the glycons
     *
     * @return all glycons imported
     */
    public ArrayList<Glycon> getGlycons() {
        return glycons;
    }

    /**
     * Import glycons from an xml file
     *
     * @param aFile                     xml file to parse
     * @throws XmlPullParserException   exception thrown if a parsing issue is encountered
     * @throws IOException              exception thrown if an issue with the file is encountered
     */
    public void importGlycons(File aFile) throws XmlPullParserException, IOException {

        // Create the pull parser.
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        // Create a reader for the input file.
        BufferedReader br = new BufferedReader(new FileReader(aFile));
        // Set the XML Pull Parser to read from this reader.
        parser.setInput(br);
        // Start the parsing.
        int type = parser.next();
        // Go through the whole document.
        while (type != XmlPullParser.END_DOCUMENT) {
            // If we find a 'MSModSpec' start tag,
            // we should parse the mod.
            if (type == XmlPullParser.START_TAG && parser.getName().equals("glycon")) {
                parseGlycon(parser);
            }
            type = parser.next();
        }
        br.close();
    }

    /**
     * Method which parses a glycon in the xml file
     *
     * @param parser                    The xml parser
     * @throws XmlPullParserException   Exception thrown whenever a parsing issue is encountered
     * @throws IOException              Exception thrown whenever an issue is encountered with the file
     */
    private void parseGlycon(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type = parser.next();

        while (type != XmlPullParser.START_TAG || !parser.getName().equals("name")) {
            type = parser.next();
        }
        type = parser.next();
        String name = parser.getText().trim();
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("shortName")) {
            type = parser.next();
        }
        type = parser.next();
        String shortName = parser.getText().trim();
        Glycon currentGlycon = new Glycon(shortName, name);
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("underivatisedMass")) {
            type = parser.next();
        }
        type = parser.next();
        Double mass = new Double(parser.getText().trim());
        currentGlycon.addMass(Glycon.UNDERIVATED_MASS, mass);
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("permethylatedMass")) {
            type = parser.next();
        }
        type = parser.next();
        mass = new Double(parser.getText().trim());
        currentGlycon.addMass(Glycon.PERMETHYLATED_MASS, mass);
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("deuteromethylatedMass")) {
            type = parser.next();
        }
        type = parser.next();
        mass = new Double(parser.getText().trim());
        currentGlycon.addMass(Glycon.DEUTEROMETHYLATED_MASS, mass);
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("peracetylatedMass")) {
            type = parser.next();
        }
        type = parser.next();
        mass = new Double(parser.getText().trim());
        currentGlycon.addMass(Glycon.PERACETYLATED_MASS, mass);
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("deuteroacetylatedMass")) {
            type = parser.next();
        }
        type = parser.next();
        mass = new Double(parser.getText().trim());
        currentGlycon.addMass(Glycon.DEUTEROACETYLATED_MASS, mass);
        glycons.add(currentGlycon);
    }
}
