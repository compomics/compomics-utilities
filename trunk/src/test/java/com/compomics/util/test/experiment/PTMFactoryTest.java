package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import junit.framework.TestCase;

import java.io.File;
import java.util.Iterator;

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
        File ptmFile = new File("resources/mods.xml");
        try {
            ptmFactory.importModifications(ptmFile);
            Iterator<PTM> ptmIt = ptmFactory.getPtmIterator();
            PTM currentPtm;
            while (ptmIt.hasNext()) {
                currentPtm = ptmIt.next();
            }
        } catch (Exception e) {
            String report = e.getLocalizedMessage();
        }
    }

}
