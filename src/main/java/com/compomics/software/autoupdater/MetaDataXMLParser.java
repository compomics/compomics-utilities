package com.compomics.software.autoupdater;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * MetaDataXMLParser.
 *
 * @author Davy Maddelein
 */
public class MetaDataXMLParser {

    /**
     * Empty default constructor
     */
    public MetaDataXMLParser() {
    }

    /**
     * The highest version number.
     */
    private String highestVersionNumber;
    /**
     * The XML event.
     */
    private XMLEvent XMLEvent;

    /**
     * Create a new MetaDataXMLParser.
     *
     * @param xmlReader the XML reader
     * @throws XMLStreamException if an XMLStreamException occurs
     */
    public MetaDataXMLParser(XMLEventReader xmlReader) throws XMLStreamException {
        while (xmlReader.hasNext()) {
            XMLEvent = xmlReader.nextEvent();
            if (XMLEvent.isStartElement()) {
                if (XMLEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase("versions")) {
                    parseVersionNumbers(xmlReader);
                    break;
                }
            }
        }
    }

    /**
     * Returns the highest version number.
     *
     * @return the highest version number
     */
    public String getHighestVersionNumber() {
        return highestVersionNumber;
    }

    /**
     * Parses the version numbers of a Maven repository web site (or just about
     * any proper XML containing the tag version).
     *
     * @param xmlReader
     * @throws XMLStreamException
     */
    private void parseVersionNumbers(XMLEventReader xmlReader) throws XMLStreamException {
        CompareVersionNumbers versionNumberComparator = new CompareVersionNumbers();
        while (xmlReader.hasNext()) {
            XMLEvent = xmlReader.nextEvent();
            if (XMLEvent.isStartElement()) {
                if (XMLEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase("version")) {

                    String currentVersionNumber = xmlReader.nextEvent().asCharacters().getData();

                    if (!currentVersionNumber.contains("b") && !currentVersionNumber.contains("beta")) {
                        if (highestVersionNumber == null) {
                            highestVersionNumber = currentVersionNumber;
                        } else {
                            if (versionNumberComparator.compare(highestVersionNumber, currentVersionNumber) == 1) {
                                highestVersionNumber = currentVersionNumber;
                            }
                        }
                    }
                }
            } else if (XMLEvent.isEndElement()) {
                if (XMLEvent.asEndElement().getName().getLocalPart().equalsIgnoreCase("versions")) {
                    break;
                }
            }
        }
    }
}
