package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.Enzyme;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Aug 30, 2010
 * Time: 10:43:21 AM
 * This test will parse the enzyme file.
 */
public class EnzymeFactoryTest extends TestCase {

    public EnzymeFactoryTest() {

    }

    public void testImport() {
        EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
        File enzymeFile = new File("src/test/resources/experiment/enzymes.xml");
        try {
            enzymeFactory.importEnzymes(enzymeFile);
            ArrayList<Enzyme> enzymes = enzymeFactory.getEnzymes();
            Enzyme testEnzyme = enzymes.get(0);
        } catch (Exception e) {
            String report = e.getLocalizedMessage();
        }
    }
}
