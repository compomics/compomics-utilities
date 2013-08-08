package com.compomics.software.autoupdater;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Davy
 */
public class MetaDataXMLParser {

        private String highestVersionNumber;
        private XMLEvent XMLEvent;

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

        public String getHighestVersionNumber() {
            return highestVersionNumber;
        }
/**
 * parses the version numbers of a maven repository website (or just about any proper xml containing the tag version)
 * @param xmlReader
 * @throws XMLStreamException 
 */
        private void parseVersionNumbers(XMLEventReader xmlReader) throws XMLStreamException {
            CompareVersionNumbers versionNumberComparator = new CompareVersionNumbers();
            while (xmlReader.hasNext()) {
                XMLEvent = xmlReader.nextEvent();
                if (XMLEvent.isStartElement()) {
                    if (XMLEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase("version")) {
                        if (highestVersionNumber == null) {
                            highestVersionNumber = xmlReader.nextEvent().asCharacters().getData();
                        } else {
                            String versionNumberToCompareWith = xmlReader.nextEvent().asCharacters().getData();
                            if (versionNumberComparator.compare(highestVersionNumber, versionNumberToCompareWith) == 1) {
                                highestVersionNumber = versionNumberToCompareWith;
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