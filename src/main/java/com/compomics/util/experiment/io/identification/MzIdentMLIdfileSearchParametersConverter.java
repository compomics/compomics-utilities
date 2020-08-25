package com.compomics.util.experiment.io.identification;

import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Reads basic search parameters from mzIdentML result files.
 *
 * @author Harald Barsnes
 */
public class MzIdentMLIdfileSearchParametersConverter extends ExperimentObject {

    /**
     * The mzIdentML file.
     */
    private File mzIdentMLFile;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The species.
     */
    private String species;
    /**
     * The waiting handler.
     */
    private WaitingHandler waitingHandler;
    /**
     * The parameters report.
     */
    private String parametersReport;

    /**
     * Constructor.
     *
     * @param mzIdentMLFile the mzIdentML file
     * @param searchParameters the search parameters
     * @param species the species
     * @param waitingHandler the waiting handler
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public MzIdentMLIdfileSearchParametersConverter(
            File mzIdentMLFile,
            SearchParameters searchParameters,
            String species,
            WaitingHandler waitingHandler
    ) throws IOException {

        this.mzIdentMLFile = mzIdentMLFile;
        this.searchParameters = searchParameters;
        this.species = species;
        this.waitingHandler = waitingHandler;

    }

    /**
     * Updated the search parameters object and returns the search parameters as
     * a string.
     *
     * @return the search parameters as a string
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if a IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws org.xmlpull.v1.XmlPullParserException if XmlPullParserException
     * occurs
     */
    public String getSearchParameters()
            throws FileNotFoundException, IOException, ClassNotFoundException, XmlPullParserException {

        parametersReport = "<br><b><u>Extracted Search Parameters</u></b><br>";

        // set the waiting handler max value
        if (waitingHandler != null) {

            waitingHandler.setSecondaryProgressCounterIndeterminate(true);

            try (SimpleFileReader reader = SimpleFileReader.getFileReader(mzIdentMLFile)) {

                int lineCounter = 0;
                String line = reader.readLine();

                while (line != null) {
                    line = reader.readLine();
                    lineCounter++;
                }

                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxSecondaryProgressCounter(lineCounter);

            }
        }

        // create the pull parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        // create a reader for the input file
        try (SimpleFileReader reader = SimpleFileReader.getFileReader(mzIdentMLFile)) {

            // set the XML Pull Parser to read from this reader
            parser.setInput(reader.getReader());

            // start the parsing
            int type = parser.next();

            // get the analysis software, the spectra data,the peptides and the psms
            while (type != XmlPullParser.END_DOCUMENT) {

                if (type == XmlPullParser.START_TAG && parser.getName().equals("Enzymes")) {
                    parseEnzymes(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("FragmentTolerance")) {
                    parseFragmentTolerance(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("ParentTolerance")) {
                    parseParentTolerance(parser);
                }

                type = parser.next();

                if (waitingHandler != null) {
                    waitingHandler.setSecondaryProgressCounter(parser.getLineNumber());
                }
            }

        }

        // set the min/max precursor charge
        parametersReport += "<br><br><b>Min Precusor Charge:</b> ";
        parametersReport += searchParameters.getMinChargeSearched() + " (default)";

        parametersReport += "<br><b>Max Precusor Charge:</b> ";
        parametersReport += searchParameters.getMaxChargeSearched() + " (default)";

        // taxonomy and species
        parametersReport += "<br><br><b>Species:</b> ";
        if (species == null || species.length() == 0) {
            parametersReport += "unknown";
        } else {
            parametersReport += species;
        }

        return parametersReport;
    }

    /**
     * Parse an Enzyme object.
     *
     * @param parser the XML parser
     * 
     * @throws IOException if a IOException occurs
     * @throws org.xmlpull.v1.XmlPullParserException if XmlPullParserException
     * occurs
     */
    private void parseEnzymes(
            XmlPullParser parser
    ) throws XmlPullParserException, IOException {

        parser.next();
        parser.next();

        parametersReport += "<br><br><b>Digestion:</b> ";
        DigestionParameters digestionPreferences = DigestionParameters.getDefaultParameters();
        boolean enzymesFound = false;

        while (parser.getName() != null && parser.getName().equalsIgnoreCase("Enzyme")) {

            String enzymeName = null;
            Integer nMissedCleavages = null;
            Boolean semiSpecific = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);

                if (attributeName.equalsIgnoreCase("missedCleavages")) {
                    nMissedCleavages = Integer.valueOf(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("semiSpecific")) {
                    semiSpecific = Boolean.valueOf(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("name")) {
                    enzymeName = parser.getAttributeValue(i);
                }
            }

            if (enzymeName != null) {

                enzymesFound = true;

                com.compomics.util.experiment.biology.enzymes.Enzyme utilitiesEnzyme = EnzymeFactory.getInstance().getEnzyme(enzymeName);

                String utilitiesEnzymeName;

                if (utilitiesEnzyme != null) {
                    utilitiesEnzymeName = utilitiesEnzyme.getName();
                    parametersReport += utilitiesEnzyme.getName();
                } else {
                    utilitiesEnzymeName = "Trypsin";
                    utilitiesEnzyme = EnzymeFactory.getInstance().getEnzyme(utilitiesEnzymeName);
                    parametersReport += utilitiesEnzyme.getName() + " (assumed)";
                }

                parametersReport += ", ";

                if (nMissedCleavages != null) {
                    parametersReport += nMissedCleavages;
                } else {
                    nMissedCleavages = 2;
                    parametersReport += nMissedCleavages + " (assumed)";
                }

                parametersReport += ", ";
                DigestionParameters.Specificity specificity = DigestionParameters.Specificity.specific;

                if (semiSpecific != null) {
                    if (semiSpecific) {
                        specificity = DigestionParameters.Specificity.semiSpecific;
                    }
                    parametersReport += specificity;
                } else {
                    parametersReport += specificity + " (assumed)";
                }

                digestionPreferences.addEnzyme(utilitiesEnzyme);
                digestionPreferences.setSpecificity(utilitiesEnzymeName, specificity);
                digestionPreferences.setnMissedCleavages(utilitiesEnzymeName, nMissedCleavages);
                digestionPreferences.setCleavageParameter(DigestionParameters.CleavageParameter.enzyme);

            }

            parser.next();

            while (parser.getName() != null && !parser.getName().equalsIgnoreCase("Enzyme")) {
                parser.next();
            }

            parser.next();
        }

        if (!enzymesFound) {
            parametersReport += "Trypsin (assumed), 2 allowed missed cleavages (assumed), specific (assumed)";
        }

        searchParameters.setDigestionParameters(digestionPreferences);

    }

    /**
     * Parse a FragmentTolerance object.
     *
     * @param parser the XML parser
     * 
     * @throws IOException if a IOException occurs
     * @throws org.xmlpull.v1.XmlPullParserException if XmlPullParserException
     * occurs
     */
    private void parseFragmentTolerance(
            XmlPullParser parser
    ) throws XmlPullParserException, IOException {

        Double fragmentMinTolerance = null;
        Double fragmentMaxTolerance = null;
        Boolean fragmentToleranceTypeIsPpm = false;

        parser.next();
        parser.next();

        while (parser.getName() != null && parser.getName().equals("cvParam")) {

            String accession = null;
            String unit = null;
            Double value = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);

                if (attributeName.equalsIgnoreCase("accession")) {
                    accession = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("unitAccession")) {
                    unit = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("value")) {
                    value = Double.valueOf(parser.getAttributeValue(i));
                }
            }

            if (accession != null && unit != null && value != null) {
                if (accession.equalsIgnoreCase("MS:1001412")) {
                    fragmentMaxTolerance = value;
                    fragmentToleranceTypeIsPpm = unit.equalsIgnoreCase("UO:0000169");
                } else if (accession.equalsIgnoreCase("MS:1001413")) {
                    fragmentMinTolerance = value;
                    fragmentToleranceTypeIsPpm = unit.equalsIgnoreCase("UO:0000169");
                }
            }

            parser.next();
            parser.next();
            parser.next();
        }

        parser.next();

        parametersReport += "<br><b>Fragment Ion Mass Tolerance:</b> ";

        if (fragmentMinTolerance != null && fragmentMaxTolerance != null) {

            Double fragmentTolerance;

            if (Math.abs(fragmentMinTolerance) - Math.abs(fragmentMaxTolerance) < 0.0000001) {
                fragmentTolerance = Math.abs(fragmentMinTolerance);
            } else {
                fragmentTolerance = Math.max(Math.abs(fragmentMinTolerance), Math.abs(fragmentMaxTolerance));
            }

            searchParameters.setFragmentIonAccuracy(fragmentTolerance);

            if (fragmentToleranceTypeIsPpm) {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
                parametersReport += fragmentTolerance + " ppm";
            } else {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
                parametersReport += fragmentTolerance + " Da";
            }

        } else {
            parametersReport += searchParameters.getFragmentIonAccuracy() + " Da (default)";
        }

    }

    /**
     * Parse a ParentTolerance object.
     *
     * @param parser the XML parser
     * 
     * @throws IOException if a IOException occurs
     * @throws org.xmlpull.v1.XmlPullParserException if XmlPullParserException
     * occurs
     */
    private void parseParentTolerance(
            XmlPullParser parser
    ) throws XmlPullParserException, IOException {

        Double precursorMinTolerance = null;
        Double precursorMaxTolerance = null;
        Boolean precursorToleranceTypeIsPpm = false;

        parser.next();
        parser.next();

        while (parser.getName() != null && parser.getName().equals("cvParam")) {

            String accession = null;
            String unit = null;
            Double value = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);

                if (attributeName.equalsIgnoreCase("accession")) {
                    accession = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("unitAccession")) {
                    unit = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("value")) {
                    value = Double.valueOf(parser.getAttributeValue(i));
                }
            }

            if (accession != null && unit != null && value != null) {
                if (accession.equalsIgnoreCase("MS:1001412")) {
                    precursorMaxTolerance = value;
                    precursorToleranceTypeIsPpm = unit.equalsIgnoreCase("UO:0000169");
                } else if (accession.equalsIgnoreCase("MS:1001413")) {
                    precursorMinTolerance = value;
                    precursorToleranceTypeIsPpm = unit.equalsIgnoreCase("UO:0000169");
                }
            }

            parser.next();
            parser.next();
            parser.next();
        }

        parser.next();

        parametersReport += "<br><b>Precursor Ion Mass Tolerance:</b> ";

        if (precursorMinTolerance != null && precursorMaxTolerance != null) {

            Double precursorTolerance;

            if (Math.abs(precursorMinTolerance) - Math.abs(precursorMaxTolerance) < 0.0000001) {
                precursorTolerance = Math.abs(precursorMinTolerance);
            } else {
                precursorTolerance = Math.max(Math.abs(precursorMinTolerance), Math.abs(precursorMaxTolerance));
            }

            searchParameters.setPrecursorAccuracy(precursorTolerance);

            if (precursorToleranceTypeIsPpm) {
                searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
                parametersReport += precursorTolerance + " ppm";
            } else {
                searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                parametersReport += precursorTolerance + " Da";
            }

        } else {
            parametersReport += searchParameters.getPrecursorAccuracy() + " Da (default)";
        }

    }
}
