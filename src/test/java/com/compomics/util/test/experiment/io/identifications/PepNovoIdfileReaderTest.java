package com.compomics.util.test.experiment.io.identifications;

import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.io.identification.idfilereaders.PepNovoIdfileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the PepNovoIDfileReader.
 *
 * @author Thilo Muth
 */
public class PepNovoIdfileReaderTest extends TestCase {

    private PepNovoIdfileReader idfileReader;

    @Before
    public void setUp() throws ClassNotFoundException, IOException, InterruptedException {
        idfileReader = new PepNovoIdfileReader(
                new File("src/test/resources/experiment/test.mgf.out")
        );
    }

    @Test
    public void testGetAllSpectrumMatches() throws Exception {

        HashMap<String, HashMap<String, ArrayList<SpectrumIdentificationAssumption>>> results = idfileReader.getAllSpectrumMatches(null, null, null);

        HashMap<String, ArrayList<SpectrumIdentificationAssumption>> fileResults = results.get("test.mgf");
                assertEquals(fileResults.size(), 4);

        for (Entry<String, ArrayList<SpectrumIdentificationAssumption>> entry : fileResults.entrySet()) {
            if (entry.getKey().contains("Scan 835")) {
                assertEquals(
                        "Incorrect title parsing for scan 835",
                        "7: Scan 835 (rt=12.4589) [NQIGDKEK]",
                        entry.getKey()
                );
                assertEquals(entry.getValue().size(), 10);
            }
        }
    }
}
