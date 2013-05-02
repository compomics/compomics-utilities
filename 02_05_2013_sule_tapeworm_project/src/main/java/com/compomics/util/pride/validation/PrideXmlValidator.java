package com.compomics.util.pride.validation;

import com.sun.msv.verifier.jarv.TheFactoryImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * PRIDE XML schema validation.
 *
 * @author Phil Jones
 * @author Florian Reisinger
 * @author Harald Barsnes
 */
public class PrideXmlValidator {

    /**
     * The verifier factory.
     */
    public static final VerifierFactory VERIFIER_FACTORY = new TheFactoryImpl();
    /**
     * The schema.
     */
    private static Schema SCHEMA = null;
    /**
     * The schema name.
     */
    private static final String SCHEMA_NAME = "pride.xsd";
    /**
     * The XML validation error handler.
     */
    private XMLValidationErrorHandler xveh;
    /**
     * The maximum number of error messages to display in the message to the
     * user before referring to the error log.
     */
    private int maxErrorMessagesToDisplay = 10;

    /**
     * Set up a PrideXmlValidator using the default schema.
     *
     * @throws IOException
     * @throws VerifierConfigurationException
     * @throws SAXException
     */
    public PrideXmlValidator() throws IOException, VerifierConfigurationException, SAXException {
        SCHEMA = VERIFIER_FACTORY.compileSchema(PrideXmlValidator.class.getClassLoader().getResourceAsStream(SCHEMA_NAME));
    }

    /**
     * Set up a PrideXmlValidator using the provided schema.
     *
     * @param schemaUrl the URL to the schema
     * @throws IOException
     * @throws VerifierConfigurationException
     * @throws SAXException
     */
    public PrideXmlValidator(URL schemaUrl) throws IOException, VerifierConfigurationException, SAXException {
        SCHEMA = VERIFIER_FACTORY.compileSchema(schemaUrl.openStream());
    }

    /**
     * Set the schema.
     *
     * @param schemaUrl the schema to set
     * @throws IOException
     * @throws VerifierConfigurationException
     * @throws SAXException
     */
    public void setSchema(URL schemaUrl) throws IOException, VerifierConfigurationException, SAXException {
        SCHEMA = VERIFIER_FACTORY.compileSchema(schemaUrl.openStream());
    }

    /**
     * Validate the file in the reader according to the given schema.
     *
     * @param reader the reader containing the file to validate
     * @param schema the schema to validate against
     * @return an XMLValidationErrorHandler object with the error details, if
     * any
     * @throws IOException
     * @throws VerifierConfigurationException
     * @throws SAXException
     */
    private XMLValidationErrorHandler validate(Reader reader, Schema schema) throws IOException, VerifierConfigurationException, SAXException {

        XMLValidationErrorHandler xmlValidationErrorHandler = new XMLValidationErrorHandler();

        Verifier schemaVerifier = schema.newVerifier();
        schemaVerifier.setErrorHandler(xmlValidationErrorHandler);
        try {
            schemaVerifier.verify(new InputSource(reader));
        } catch (SAXParseException e) {
            xmlValidationErrorHandler.error(e);
        }
        return xmlValidationErrorHandler;
    }

    /**
     * Validate the file in the reader according to the default PRIDE XML
     * schema.
     *
     * @param reader the reader containing the file to validate
     * @return an XMLValidationErrorHandler object with the error details, if
     * any
     * @throws IOException
     * @throws VerifierConfigurationException
     * @throws SAXException
     */
    private XMLValidationErrorHandler validate(Reader reader) throws IOException, VerifierConfigurationException, SAXException {
        if (SCHEMA == null) {
            SCHEMA = VERIFIER_FACTORY.compileSchema(PrideXmlValidator.class.getClassLoader().getResourceAsStream(SCHEMA_NAME));
        }
        return validate(reader, SCHEMA);
    }

    /**
     * Returns true of the given PRIDE XML file is valid.
     *
     * @param prideXmlFile the PRIDE XML file to test
     * @return true of the given PRIDE XML file is valid
     * @throws FileNotFoundException
     * @throws IOException
     * @throws VerifierConfigurationException
     * @throws SAXException
     */
    public boolean validate(File prideXmlFile) throws FileNotFoundException, IOException, VerifierConfigurationException, SAXException {

        PrideXmlValidator validator = new PrideXmlValidator();
        BufferedReader br = new BufferedReader(new FileReader(prideXmlFile));

        xveh = validator.validate(br);
        br.close();

        if (xveh.noErrors()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the errors formatted as s single string. Returns null if the file
     * has not been validated.
     *
     * @return the errors formatted as s single string, null if the file has not
     * yet been validated
     */
    public String getErrorsAsString() {

        if (xveh == null) {
            return "The file has not yet been validated!";
        }

        if (xveh.getErrorsAsList().isEmpty()) {
            return "There are no errors.";
        }

        if (xveh.getErrorsAsList().size() > maxErrorMessagesToDisplay) {
            System.out.println(xveh.getErrorsFormattedAsPlainText());
            return "The PRIDE XML file contains errors. See the tool's log file for details."
                    + "\n\nPlease contact the developers.";
        } else {
            System.out.println(xveh.getErrorsFormattedAsPlainText());
            return "The PRIDE XML file contains errors:"
                    + xveh.getErrorsFormattedAsPlainText()
                    + "\n\nPlease contact the developers.";
        }
    }

    /**
     * Returns the XML validation error object. Returns null if the file has not
     * been validated.
     *
     * @return the XML validation error object, null if the file has not yet
     * been validated
     */
    public XMLValidationErrorHandler getErrors() {
        return xveh;
    }
}