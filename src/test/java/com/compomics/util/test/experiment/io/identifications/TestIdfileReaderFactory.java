package com.compomics.util.test.experiment.io.identifications;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.io.identifications.IdfileReaderFactory;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;

/**
 * Tests the id file reader registration service used in the
 * IdFileReaderFactory.
 *
 * @author Lennart Martens
 */
public class TestIdfileReaderFactory extends TestCase {

    public TestIdfileReaderFactory() {
        this("Test scenario for the IdfileReaderFactory");
    }

    public TestIdfileReaderFactory(String aName) {
        super(aName);
    }

    public void testIdfileReaderRegistration() {

        // First register a new, incorrect class (missing required constructor taking single java.io.File argument).
        IdfileReader tifr = new IdfileReader() {

            @Override
            public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters)
                    throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
                return getAllSpectrumMatches(waitingHandler, searchParameters, null, true);
            }

            @Override
            public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters,
                    SequenceMatchingPreferences sequenceMatchingPreferences, boolean expandAaCombinations)
                    throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
                return null;
            }

            @Override
            public String getExtension() {
                return ".crazyThingThatDoesNotExist";
            }

            @Override
            public void close() throws IOException {
                // Does nothing.
            }

            @Override
            public HashMap<String, ArrayList<String>> getSoftwareVersions() {
                HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
                ArrayList<String> versions = new ArrayList<String>();
                versions.add("X.Y.Z");
                result.put("testIdfileReaderRegistration", versions);
                return result;
            }

            @Override
            public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap() {
                // Does nothing.
                return null;
            }

            @Override
            public void clearTagsMap() {
                // Does nothing.
            }

            @Override
            public boolean hasDeNovoTags() {
                return false;
            }
        };

        IdfileReaderFactory.registerIdFileReader(tifr.getClass(), tifr.getExtension());
        try {
            Assert.assertNull("Should have been unable to register TestIdfileReader in IdfileReaderFactory as "
                    + "it lacks a constructor with a single parameter of type java.io.File!",
                    IdfileReaderFactory.getInstance().getFileReader(new File("c:/test.crazyThingThatDoesNotExist")));
        } catch (Exception e) {
            fail("Exception thrown when attempting to obtain (non-existing) registered IdfileReader: " + e.getMessage());
        }

        // Now register something more decent!
        InnerIdfileReader ifr = new InnerIdfileReader(null);
        Class result = IdfileReaderFactory.registerIdFileReader(ifr.getClass(), ifr.getExtension());
        Assert.assertNull(result);
        // See if it works!
        try {
            Assert.assertNotNull("Should have been able to register TestIdfileReader in IdfileReaderFactory but it was not found!",
                    IdfileReaderFactory.getInstance().getFileReader(new File("c:/test" + ifr.getExtension())));
        } catch (Exception e) {
            fail("Exception thrown when attempting to obtain registered IdfileReader: " + e.getMessage());
        }

        // Now re-register an existing class.
        result = IdfileReaderFactory.registerIdFileReader(ifr.getClass(), ifr.getExtension());
        Assert.assertEquals("Should have had a preregistered test IdfileReader, but it was not found!", ifr.getClass(), result);

        // Finally, try to register something else.
        IdfileReaderFactory.registerIdFileReader(this.getClass(), ".schtuff");
        try {
            Assert.assertNull("Was able to register non-IdfileReader 'TestIdfileReaderFactory' in IdfileReaderFactory!",
                    IdfileReaderFactory.getInstance().getFileReader(new File("c:/test.schtuff")));
        } catch (Exception e) {
            fail("Exception thrown when attempting to obtain (non-existing) registered IdfileReader: " + e.getMessage());
        }
    }

    /**
     * Test instance.
     */
    public static class InnerIdfileReader implements IdfileReader {

        public InnerIdfileReader(File aFile) {
            // Does nothing.
        }

        @Override
        public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters)
                throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
            return getAllSpectrumMatches(waitingHandler, searchParameters, null, true);
        }

        @Override
        public LinkedList<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler, SearchParameters searchParameters, SequenceMatchingPreferences sequenceMatchingPreferences,
                boolean expandAaCombinations) throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException, InterruptedException, JAXBException {
            // Does nothing.
            return null;
        }

        @Override
        public String getExtension() {
            return ".yourNotBelievingThisAreYou";
        }

        @Override
        public void close() throws IOException {
            // Does nothing.
        }

        @Override
        public HashMap<String, ArrayList<String>> getSoftwareVersions() {
            HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
            ArrayList<String> versions = new ArrayList<String>();
            versions.add("X.Y.Z");
            result.put("testIdfileReaderRegistration", versions);
            return result;
        }

        @Override
        public HashMap<String, LinkedList<SpectrumMatch>> getTagsMap() {
            // Does nothing.
            return null;
        }

        @Override
        public void clearTagsMap() {
            // Does nothing.
        }

        @Override
        public boolean hasDeNovoTags() {
            return false;
        }
    }
}
