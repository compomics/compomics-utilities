package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.quantification.reporterion.ReporterIonQuantification.ReporterIonMethod;
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
 *
 * @author Marc Vaudel
 *
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
     * @TODO: JavaDoc missing.
     *
     * @return
     */
    public static ReporterMethodFactory getInstance() {
        if (instance == null) {
            instance = new ReporterMethodFactory();
        }
        return instance;
    }

    /**
     * @TODO: JavaDoc missing.
     *
     * @return
     */
    public ArrayList<ReporterMethod> getMethods() {
        return methods;
    }

    /**
     * @TODO: JavaDoc missing.
     *
     * @return
     */
    public String[] getMethodsNames() {
        String[] names = new String[methods.size()];
        for (int i = 0; i < methods.size(); i++) {
            //    names[i] = methods.get(i).getMethodName();
        }
        return names;
    }

    /**
     * @TODO: JavaDoc missing.
     *
     * @param aFile
     */
    public void saveFile(File aFile) {
        // TODO save
    }

    /**
     * @TODO: JavaDoc missing.
     *
     * @param aFile
     * @throws IOException
     * @throws XmlPullParserException
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
                //         methods.add(parseMethod(parser));
            }
            type = parser.next();
        }
        br.close();
    }

    /**
     * @TODO: JavaDoc missing.
     *
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parseMethod(XmlPullParser parser) throws XmlPullParserException, IOException {

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
        ReporterIonMethod index = ReporterIonMethod.ITRAQ_4PLEX;
        if (name.equals("iTRAQ 4Plex")) {
            index = ReporterIonMethod.ITRAQ_4PLEX;
        } else if (name.equals("iTRAQ 8Plex")) {
            index = ReporterIonMethod.ITRAQ_8PLEX;
        } else if (name.equals("TMT6")) {
            index = ReporterIonMethod.TMT6;
        } else if (name.equals("TMT2")) {
            index = ReporterIonMethod.TMT2;
        }
    }

    /**
     * @TODO: JavaDoc missing.
     *
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
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
        return new ReporterIon(name, mass);
    }

    /**
     * @TODO: JavaDoc missing.
     *
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
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
