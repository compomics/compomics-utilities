package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import junit.framework.TestCase;

import java.io.File;
import java.util.Iterator;
import junit.framework.Assert;

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
            String name = "methylation of K";
            Assert.assertEquals(testPTM.getName(), name.toLowerCase());
            Assert.assertEquals(testPTM.getMass(), 14.015650);
            Assert.assertEquals(testPTM.getType(), PTM.MODAA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
