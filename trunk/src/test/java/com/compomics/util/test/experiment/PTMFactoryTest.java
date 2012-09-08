package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 9:08:04 PM
 * This test will display the xml parsing.
 */
public class PTMFactoryTest extends TestCase {
    
    public PTMFactoryTest() {
        
    }
    
    public void testImport() {
        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();
        File ptmFile = new File("src/test/resources/experiment/mods.xml");
        try {
            ptmFactory.importModifications(ptmFile, false);
            String ptmName = ptmFactory.getPTMs().get(0);
            PTM testPTM = ptmFactory.getPTM(ptmName);
            String name = "Test modification with neutral losses";
            Assert.assertEquals(testPTM.getName(), name.toLowerCase());
            Assert.assertEquals(testPTM.getMass(), 123.456789);
            Assert.assertEquals(testPTM.getType(), PTM.MODAA);
            Assert.assertEquals(testPTM.getResidues().size(), 2);
            Assert.assertEquals(testPTM.getResidues().get(0), "B");
            Assert.assertEquals(testPTM.getResidues().get(1), "O");
            Assert.assertEquals(testPTM.getNeutralLosses().size(), 2);
            Assert.assertEquals(testPTM.getNeutralLosses().get(0).mass, 456.789123);
            Assert.assertEquals(testPTM.getNeutralLosses().get(1).mass, 789.123456);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
