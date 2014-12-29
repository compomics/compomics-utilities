package com.compomics.util.pride.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Edited using IntelliJ IDEA Date: 16-Sep-2005 Time: 15:26:12
 *
 * @author Phil Jones
 */
public class XMLValidationErrorHandler implements ErrorHandler {

    /**
     * The list of error messages.
     */
    private List<String> errorMessages = null;

    /**
     * Constructor.
     */
    public XMLValidationErrorHandler() {
        super();
    }

    /**
     * Returns true of there were no errors.
     *
     * @return true of there were no errors
     */
    public boolean noErrors() {
        return (errorMessages == null || errorMessages.isEmpty());
    }

    /**
     * Returns the errors formatted as plain text.
     *
     * @return the errors formatted as plain text
     */
    public String getErrorsFormattedAsPlainText() {
        if (noErrors()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String errorMessage : errorMessages) {
            sb.append(errorMessage);
        }
        return sb.toString();
    }

    /**
     * Returns the errors formatted as HTML.
     *
     * @return the errors formatted as HTML
     */
    public String getErrorsFormattedAsHTML() {
        if (noErrors()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String errorMessage : errorMessages) {
            sb.append(errorMessage);
        }
        return (sb.toString().replaceAll("<", "&lt;")).replaceAll(">", "&gt;").replaceAll("\\n", "<br/>");
    }

    /**
     * Returns the errors as a list.
     *
     * @return the errors as a list
     */
    public List<String> getErrorsAsList() {
        return errorMessages;
    }

    /**
     * Initialize the error messages.
     */
    private void initialiseErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<String>();
        }
    }

    /**
     * Receive notification of a recoverable error.
     * <br><br>
     * This corresponds to the definition of "error" in section 1.2 of the W3C
     * XML 1.0 Recommendation. For example, a validating parser would use this
     * callback to report the violation of a validity constraint. The default
     * behavior is to take no action.
     * <br><br>
     * The SAX parser must continue to provide normal parsing events after
     * invoking this method: it should still be possible for the application to
     * process the document through to the end. If the application cannot do so,
     * then the parser should report a fatal error even if the XML 1.0
     * recommendation does not require it to do so.
     * <br><br>
     * Filters may use this method to report other, non-XML errors as well.
     *
     * @param exception The error information encapsulated in a SAX parse
     * exception.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping
     * another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void error(SAXParseException exception) throws SAXException {
        initialiseErrorMessages();
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nNon-fatal XML Parsing error detected on line ")
                .append(exception.getLineNumber())
                .append(".\nError message: ")
                .append(exception.getMessage());
        errorMessages.add(sb.toString());
    }

    /**
     * Receive notification of a non-recoverable error.
     * <br><br>
     * This corresponds to the definition of "fatal error" in section 1.2 of the
     * W3C XML 1.0 Recommendation. For example, a parser would use this callback
     * to report the violation of a well-formedness constraint.
     * <br><br>
     * The application must assume that the document is unusable after the
     * parser has invoked this method, and should continue (if at all) only for
     * the sake of collecting addition error messages: in fact, SAX parsers are
     * free to stop reporting any other events once this method has been
     * invoked.
     *
     * @param exception The error information encapsulated in a SAX parse
     * exception.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping
     * another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        initialiseErrorMessages();
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nFATAL XML Parsing error detected on line ")
                .append(exception.getLineNumber())
                .append(".\nFatal Error message: ")
                .append(exception.getMessage());
        errorMessages.add(sb.toString());
    }

    /**
     * This is a extension method to the #fatalError(SAXParseException) method
     * to allow the handling of IOExceptions. Example: An FileNotFoundException
     * is thrown by the validate method if the file to be validated contains a
     * local reference to a DTD (which can not be found on the server).
     *
     * @param exception the IOException
     */
    public void fatalError(IOException exception) {
        initialiseErrorMessages();
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nFATAL XML Validation error. ");
        if (exception instanceof FileNotFoundException) {
            sb.append("A needed or referenced file was not found!\n")
                    .append(exception.getMessage())
                    .append("\nHint: Please make sure your file does not reference local schema or DTD files.");
        } else {
            sb.append("An I/O error prevented the file from being validated!\n");
        }
        errorMessages.add(sb.toString());
    }

    /**
     * Receive notification of a warning.
     * <br><br>
     * SAX parsers will use this method to report conditions that are not errors
     * or fatal errors as defined by the XML 1.0 recommendation. The default
     * behavior is to take no action.
     * <br><br>
     * The SAX parser must continue to provide normal parsing events after
     * invoking this method: it should still be possible for the application to
     * process the document through to the end.
     * <br><br>
     * Filters may use this method to report other, non-XML warnings as well.
     *
     * @param exception The warning information encapsulated in a SAX parse
     * exception.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping
     * another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void warning(SAXParseException exception) throws SAXException {
        initialiseErrorMessages();
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nWarning: Validation of the XML has detected the following condition on line ")
                .append(exception.getLineNumber())
                .append(".\nWarning message: ")
                .append(exception.getMessage());
        errorMessages.add(sb.toString());
    }
}
