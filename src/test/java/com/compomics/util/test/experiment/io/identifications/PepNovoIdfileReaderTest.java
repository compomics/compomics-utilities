package com.compomics.util.test.experiment.io.identifications;

import com.compomics.util.experiment.io.identification.idfilereaders.PepNovoIdfileReader;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
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
        idfileReader = new PepNovoIdfileReader(new File("src/test/resources/experiment/test.mgf.out"));
    }

    @Test
    public void testGetAllSpectrumMatches() throws Exception {
        LinkedList<SpectrumMatch> allSpectrumMatches = idfileReader.getAllSpectrumMatches(null, null);
        for (SpectrumMatch sm : allSpectrumMatches) {
            if (sm.getKey().contains("Scan 835")) {
                assertEquals("test.mgf_cus_7: Scan 835 (rt=12.4589) [NQIGDKEK]", sm.getKey());
            }
        }
    }
}
