package com.compomics.util.test.experiment.io.identifications;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.io.identifications.IdfileReaderFactory;
import com.compomics.util.waiting.WaitingHandler;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: martlenn
 * Date: 8/09/12
 * Time: 23:42
 * To change this template use File | Settings | File Templates.
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
        IdfileReader tifr = new IdfileReader(){
            public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getExtension() {
                return ".crazyThingThatDoesNotExist";
            }

            public void close() throws IOException {
                // Does nothing.
            }

            @Override
            public String getSoftwareVersion() {
                return "X.Y.Z";
            }

            @Override
            public String getSoftware() {
                return "testIdfileReaderRegistration";
            }
        };

        IdfileReaderFactory.registerIdFileReader(tifr.getClass(), tifr.getExtension());
        try {
            Assert.assertNull("Should have been unable to register TestIdfileReader in IdfileReaderFactory as it lacks a constructor with a single parameter of type java.io.File!", IdfileReaderFactory.getInstance().getFileReader(new File("c:/test.crazyThingThatDoesNotExist")));
        } catch(Exception e) {
            fail("Exception thrown when attempting to obtain (non-existing) registered IdfileReader: " + e.getMessage());
        }

        // Now register something more decent!
        InnerIdfileReader ifr = new InnerIdfileReader(null);
        Class result = IdfileReaderFactory.registerIdFileReader(ifr.getClass(), ifr.getExtension());
        Assert.assertNull(result);
        // See if it works!
        try {
            Assert.assertNotNull("Should have been able to register TestIdfileReader in IdfileReaderFactory but it was not found!", IdfileReaderFactory.getInstance().getFileReader(new File("c:/test" + ifr.getExtension())));
        } catch(Exception e) {
            fail("Exception thrown when attempting to obtain registered IdfileReader: " + e.getMessage());
        }


        // Now re-register an existing class.
        result = IdfileReaderFactory.registerIdFileReader(ifr.getClass(), ifr.getExtension());
        Assert.assertEquals("Should have had a preregistered test IdfileReader, but it was not found!", ifr.getClass(), result);

        // Finally, try to register something else.
        IdfileReaderFactory.registerIdFileReader(this.getClass(), ".schtuff");
        try {
            Assert.assertNull("Was able to register non-IdfileReader 'TestIdfileReaderFactory' in IdfileReaderFactory!", IdfileReaderFactory.getInstance().getFileReader(new File("c:/test.schtuff")));
        } catch(Exception e) {
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

        public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {
            // Does nothing.
            return null;
        }

        public String getExtension() {
            return ".yourNotBelievingThisAreYou";
        }

        public void close() throws IOException {
            // Does nothing.
        }

        @Override
        public String getSoftwareVersion() {
            return "X.Y.Z";
        }

        @Override
        public String getSoftware() {
            return "InnerIdfileReader";
        }
    }
}
