package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.ElementaryIon;
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
import java.util.HashMap;

/**
 * This factory imports reporter methods details from an XMl file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ReporterMethodFactory extends ExperimentObject {

    /**
     * The reporter methods names.
     */
    private ArrayList<String> methodsNames;
    /**
     * The reporter methods.
     */
    private HashMap<String, ReporterMethod> methods;
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
    public HashMap<String, ReporterMethod> getMethods() {
        return methods;
    }

    /**
     * Returns the name of the methods present in the factory.
     *
     * @return the name of the methods present in the factory
     */
    public ArrayList<String> getMethodsNames() {
        return methodsNames;
    }

    /**
     * Returns the methods names as array.
     *
     * @return the methods names
     */
    public String[] getMethodsNamesAsArray() {

        String[] array = methodsNames.toArray(new String[methodsNames.size()]);
        return array;
    }

    /**
     * Returns the reporter methods corresponding to the given name.
     *
     * @param methodName the name of the method
     *
     * @return the reporter methods
     */
    public ReporterMethod getReporterMethod(String methodName) {
        return methods.get(methodName);
    }

    /**
     * Save to file.
     *
     * @param aFile the file to save to
     *
     * @throws IOException thrown whenever a problem occurred while writing the
     * file
     */
    public void saveFile(File aFile) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(aFile));

        try {
            String indent = "\t";
            writer.write("<xml>");
            writer.newLine();

            for (String reporterMethodName : methodsNames) {

                ReporterMethod reporterMethod = methods.get(reporterMethodName);

                writer.write(indent + "<reporterMethod>");
                writer.newLine();
                writer.write(indent + indent + "<name>" + reporterMethod.getName() + "</name>");
                writer.newLine();
                writer.write(indent + indent + "<reagentList>");
                writer.newLine();
                ArrayList<String> reagentNames = new ArrayList<String>(reporterMethod.getReagentsSortedByMass());

                for (String reagentName : reagentNames) {
                    Reagent reagent = reporterMethod.getReagent(reagentName);
                    writer.write(indent + indent + indent + "<reagent>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<name>" + reagent.getName() + "</name>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<monoisotopicMz>" + (reagent.getReporterIon().getTheoreticMz(1)) + "</monoisotopicMz>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<minus2>" + reagent.getMinus2() + "</minus2>");
                    writer.newLine();
                    writer.write(indent + indent + indent + indent + "<minus1>" + reagent.getMinus1() + "</minus1>");
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
                writer.write(indent + "</reporterMethod>");
                writer.newLine();
            }

            writer.write("</xml>");
            writer.newLine();
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

        methodsNames = new ArrayList<String>();
        methods = new HashMap<String, ReporterMethod>();

        // create the pull parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        // create a reader for the input file
        BufferedReader br = new BufferedReader(new FileReader(aFile));

        try {

            // set the XML Pull Parser to read from this reader
            parser.setInput(br);

            // start the parsing
            int type = parser.next();

            // go through the whole document
            while (type != XmlPullParser.END_DOCUMENT) {
                // if we find a 'reporterMethod' start tag, we should parse the mod
                if (type == XmlPullParser.START_TAG && parser.getName().equals("reporterMethod")) {
                    ReporterMethod reporterMethod = parseMethod(parser);
                    String methodName = reporterMethod.getName();
                    methodsNames.add(methodName);
                    methods.put(methodName, reporterMethod);
                }
                type = parser.next();
            }

        } finally {
            br.close();
        }
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

        // list of reagents
        ArrayList<Reagent> reagents = new ArrayList<Reagent>();

        // iterate the reagents
        while (type != XmlPullParser.END_TAG || !parser.getName().equals("reagentList")) {

            // create the empry reagent
            Reagent reagent = new Reagent();

            // reagent name
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("name")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagentList")) {
                    throw new IllegalArgumentException("Unexpected end of reagent list when parsing method " + name + ".");
                }
            }
            type = parser.next();
            String reagentName = parser.getText().trim();
            reagent.setName(reagentName);
            ReporterIon reporterIon = ReporterIon.getReporterIon(reagentName);

            // monoisotopic m/z or minus 2
            while (type != XmlPullParser.START_TAG || (!parser.getName().equals("monoisotopicMz") && !parser.getName().equals("minus2"))) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            if (parser.getName().equals("monoisotopicMz")) {
                type = parser.next();
                Double monoisotopicMass = new Double(parser.getText().trim());
                reporterIon = new ReporterIon(reagentName, monoisotopicMass - ElementaryIon.proton.getTheoreticMass());

                // minus 2
                while (type != XmlPullParser.START_TAG || !parser.getName().equals("minus2")) {
                    type = parser.next();
                    if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                        throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                    }
                }
            } else if (!parser.getName().equals("minus2")) {
                throw new IllegalArgumentException("Found " + parser.getName() + " start tag where minus2 was expected.");
            }
            if (reporterIon == null) {
                throw new IllegalArgumentException("No mass found for reporter ion " + reagentName + ".");
            }
            reagent.setReporterIon(reporterIon);

            type = parser.next();
            Double correctionFactor = new Double(parser.getText().trim());
            reagent.setMinus2(correctionFactor);

            // minus 1
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("minus1")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setMinus1(correctionFactor);

            // ref
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("ref")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setRef(correctionFactor);

            // plus 1
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("plus1")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setPlus1(correctionFactor);

            // plus 2
            while (type != XmlPullParser.START_TAG || !parser.getName().equals("plus2")) {
                type = parser.next();
                if (type == XmlPullParser.END_TAG && parser.getName().equals("reagent")) {
                    throw new IllegalArgumentException("Unexpected end of reagent details when parsing reagent " + reagentName + " in method " + name + ".");
                }
            }
            type = parser.next();
            correctionFactor = new Double(parser.getText().trim());
            reagent.setPlus2(correctionFactor);

            // add the reagent to the list
            reagents.add(reagent);

            // move to the next reagent
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
