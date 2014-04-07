package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.personalization.ExperimentObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This factory imports reporter methods details from an XMl file.
 *
 * @author Marc Vaudel
 */
public class ReporterMethodFactory extends ExperimentObject {

    /**
     * The reporter methods.
     */
    private ArrayList<ReporterMethod> methods;
    /**
     * The reporter factory.
     */
    private static ReporterMethodFactory instance = null;

    /**
     * Constructor.
     */
    private ReporterMethodFactory() {
    }

    /**
     * Constructor for the factory.
     *
     * @return the reporter method factory
     */
    public static ReporterMethodFactory getInstance() {
        if (instance == null) {
            instance = new ReporterMethodFactory();
        }
        return instance;
    }

    /**
     * Returns the methods implemented in the factory.
     *
     * @return the methods implemented in the factory
     */
    public ArrayList<ReporterMethod> getMethods() {
        return methods;
    }

    /**
     * Returns the name of the methods present in the factory.
     *
     * @return the name of the methods present in the factory
     */
    public String[] getMethodsNames() {
        String[] names = new String[methods.size()];
        for (int i = 0; i < methods.size(); i++) {
            names[i] = methods.get(i).getName();
        }
        return names;
    }

    /**
     * Save to file.
     *
     * @param aFile the file to save to
     *
     * @throws java.io.IOException exception thrown whenever a problem occurred
     * while writing the file
     */
    public void saveFile(File aFile) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(aFile));
        try {
            String indent = "\t";
            writer.write("<xml>");
            writer.newLine();
            for (ReporterMethod reporterMethod : methods) {
                writer.write(indent + "<reorterMethod>");
                writer.newLine();
                writer.write(indent + indent + "<name>" + reporterMethod.getName() + "</name>");
                writer.newLine();
                writer.write(indent + indent + "<reagentList>");
                writer.newLine();
                ArrayList<String> reagentNames = new ArrayList<String>(reporterMethod.getReagentNames());
                Collections.sort(reagentNames);
                for (String reagentName : reagentNames) {
                    Reagent reagent = reporterMethod.getReagent(reagentName);
                    writer.write(indent + indent + indent + "<reagent>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<name>" + reagent.getName() + "</name>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<monoisotopicMass>" + reagent.getReporterIon().getTheoreticMass() + "</monoisotopicMass>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<minus2>" + reagent.getMinus2() + "</minus2>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<minus1>" + reagent.getMinus1() + "</minus2>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<ref>" + reagent.getRef() + "</ref>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<plus1>" + reagent.getPlus1() + "</plus1>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<plus2>" + reagent.getPlus2() + "</plus2>");
                    writer.newLine();
                    writer.write(indent + indent + indent + "</reagent>");
                    writer.newLine();
                }
                writer.write(indent + indent + "</reagentList>");
                writer.newLine();
                writer.write(indent + "</reorterMethod>");
                writer.newLine();
                writer.write("</xml>");
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    /**
     * Imports the methods from an XML file.
     *
     * @param aFile the XML file
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws XmlPullParserException exception thrown whenever an error
     * occurred while parsing the XML file
     */
    public void importMethods(File aFile) throws IOException, XmlPullParserException {
        methods = new ArrayList();
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
            // If we find a 'reporterMethod' start tag,
            // we should parse the mod.
            if (type == XmlPullParser.START_TAG && parser.getName().equals("reporterMethod")) {
                methods.add(parseMethod(parser));
            }
            type = parser.next();
        }
        br.close();
    }

    /**
     * Parses a bloc describing a reporter method.
     *
     * @param parser the XML parser
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws XmlPullParserException exception thrown whenever an error
     * occurred while parsing the XML file
     */
    private ReporterMethod parseMethod(XmlPullParser parser) throws XmlPullParserException, IOException {

        int type = parser.next();

        while (type != XmlPullParser.START_TAG || !parser.getName().equals("name")) {
            type = parser.next();
        }
        type = parser.next();
        String name = parser.getText().trim();

        ArrayList<Reagent> reagents = new ArrayList<Reagent>();
        while (type != XmlPullParser.END_TAG || !parser.getName().equals("reagentList")) {
            Reagent reagent = new Reagent();
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("name")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagentList")) {
                    throw new IllegalArgumentException("Unexpected end of reagent list when parsing method " + name + ".");
                }
            }
            type = parser.next();
            String reagentName = parser.getText().trim();
            reagent.setName(reagentName);
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("monoisotopicMass")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            Double monoisotopicMass = new Double(parser.getText().trim());
            ReporterIon reporterIon = new ReporterIon(reagentName, monoisotopicMass);
            reagent.setReporterIon(reporterIon);
            //@TODO: set reporter ion
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("minus2")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            Double correctionFactor = new Double(parser.getText().trim());
            reagent.setMinus2(correctionFactor);
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("minus1")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setMinus1(correctionFactor);
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("ref")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setRef(correctionFactor);
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("plus1")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setPlus1(correctionFactor);
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("plus2")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setPlus2(correctionFactor);

            reagents.add(reagent);
            while (type != XmlPullParser.END_TAG || !parser.getName().equals("reagent")) {
                type = parser.next();
            }
            type = parser.next();
            while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_TAG) {
                type = parser.next();
            }
        }

        return new ReporterMethod(name, reagents);
    }
}