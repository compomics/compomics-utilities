package com.compomics.util.test.experiment;

import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;

import com.compomics.util.experiment.biology.GlyconFactory;
import com.compomics.util.experiment.biology.ions.Glycon;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 29, 2010
 * Time: 7:23:16 PM
 * This test will read the glycon xml file.
 */
public class GlyconFactoryTest extends TestCase {

    public GlyconFactoryTest() {

    }

    public void testImport() {
        GlyconFactory glyconFactory = GlyconFactory.getInstance();
        File ptmFile = new File("src/test/resources/experiment/glycons.xml");
        try {
            glyconFactory.importGlycons(ptmFile);
            ArrayList<Glycon> glycons = glyconFactory.getGlycons();
            Glycon testGlycon = glycons.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
