package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

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
    
    public void testImport() throws XmlPullParserException, IOException {
        PTMFactory ptmFactory = PTMFactory.getInstance();
        ptmFactory.clearFactory();
        ptmFactory = PTMFactory.getInstance();
        File ptmFile = new File("src/test/resources/experiment/mods.xml");
            ptmFactory.importModifications(ptmFile, false);
            PTM testPTM = ptmFactory.getPTM("test modification with neutral losses");
            Assert.assertEquals(testPTM.getMass(), 123.456789);
            Assert.assertEquals(testPTM.getType(), PTM.MODAA);
            Assert.assertTrue(testPTM.getPattern().toString().equals("[BO]"));
            Assert.assertEquals(testPTM.getNeutralLosses().size(), 2);
            Assert.assertEquals(testPTM.getNeutralLosses().get(0).mass, 456.789123);
            Assert.assertEquals(testPTM.getNeutralLosses().get(1).mass, 789.123456);
    }
}
