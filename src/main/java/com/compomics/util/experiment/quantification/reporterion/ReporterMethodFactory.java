package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.quantification.QuantificationMethod;
import com.compomics.util.experiment.utils.ExperimentObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This factory imports reporter methods details from an xml file.
 * User: Marc
 * Date: Sep 29, 2010
 * Time: 6:02:41 PM
 */
public class ReporterMethodFactory extends ExperimentObject {

    private ArrayList<ReporterMethod> methods;
    private static ReporterMethodFactory instance = null;

    private ReporterMethodFactory() {
    }

    public static ReporterMethodFactory getInstance() {
        if (instance == null) {
            instance = new ReporterMethodFactory();
        }
        return instance;
    }

    public ArrayList<ReporterMethod> getMethods() {
        return methods;
    }

    public String[] getMethodsNames() {
        String[] names = new String[methods.size()];
        for (int i= 0 ; i < methods.size() ; i++) {
            names[i] = methods.get(i).getMethodName();
        }
        return names;
    }

    public void saveFile(File aFile) {
        // TODO save
    }

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

    private ReporterMethod parseMethod(XmlPullParser parser) throws XmlPullParserException, IOException {

        int type = parser.next();

        while (type != XmlPullParser.START_TAG || !parser.getName().equals("name")) {
            type = parser.next();
        }
        type = parser.next();
        String name = parser.getText().trim();
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("reporterIonList")) {
            type = parser.next();
        }
        ArrayList<ReporterIon> reporterIons = new ArrayList<ReporterIon>();
        while (type != XmlPullParser.END_TAG || !parser.getName().equals("reporterIonList")) {
            reporterIons.add(parseIon(parser));
            type = parser.next();
            while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_TAG) {
                type = parser.next();
            }
        }

        while (type != XmlPullParser.START_TAG || !parser.getName().equals("correctionFactorList")) {
            type = parser.next();
        }
        ArrayList<CorrectionFactor> correctionFactors = new ArrayList<CorrectionFactor>();
        while (type != XmlPullParser.END_TAG || !parser.getName().equals("correctionFactorList")) {
            correctionFactors.add(parseCorrectionFactor(parser));
            type = parser.next();
            while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_TAG) {
                type = parser.next();
            }
        }
        int index = -1;
        if (name.equals("iTRAQ 4Plex")) {
            index = QuantificationMethod.ITRAQ_4PLEX;
        } else if (name.equals("iTRAQ 8Plex")) {
            index = QuantificationMethod.ITRAQ_8PLEX;
        } else if (name.equals("TMT6")) {
            index = QuantificationMethod.TMT6;
        }   else if (name.equals("TMT2")) {
            index = QuantificationMethod.TMT2;
        }
        return new ReporterMethod(index, name, reporterIons, correctionFactors);
    }

    private ReporterIon parseIon(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type = parser.next();
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("name")) {
            type = parser.next();
        }
        type = parser.next();
        String name = parser.getText().trim();
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("id")) {
            type = parser.next();
        }
        type = parser.next();
        Integer id = new Integer(parser.getText().trim());
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("mass")) {
            type = parser.next();
        }
        type = parser.next();
        Double mass = new Double(parser.getText().trim());
        while (type != XmlPullParser.END_TAG || !parser.getName().equals("reporterIon")) {
            type = parser.next();
        }
        return new ReporterIon(id, name, mass);
    }

    private CorrectionFactor parseCorrectionFactor(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type = parser.next();
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("ionId")) {
            type = parser.next();
        }
        type = parser.next();
        Integer id = new Integer(parser.getText().trim());
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("minus2")) {
            type = parser.next();
        }
        type = parser.next();
        Double minus2 = new Double(parser.getText().trim());
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("minus1")) {
            type = parser.next();
        }
        type = parser.next();
        Double minus1 = new Double(parser.getText().trim());
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("plus1")) {
            type = parser.next();
        }
        type = parser.next();
        Double plus1 = new Double(parser.getText().trim());
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("plus2")) {
            type = parser.next();
        }
        type = parser.next();
        Double plus2 = new Double(parser.getText().trim());
        while (type != XmlPullParser.END_TAG || !parser.getName().equals("correctionFactor")) {
            type = parser.next();
        }
        return new CorrectionFactor(id, minus2, minus1, plus1, plus2);
    }
}
