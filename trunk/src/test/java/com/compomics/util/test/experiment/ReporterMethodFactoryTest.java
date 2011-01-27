package com.compomics.util.test.experiment;

import junit.framework.TestCase;
import com.compomics.util.experiment.quantification.reporterion.ReporterMethodFactory;
import com.compomics.util.experiment.quantification.reporterion.ReporterMethod;

import java.io.File;
import java.util.ArrayList;


/**
 * This test class will test that the xml file can be properly read.
 * User: Marc
 * Date: Sep 29, 2010
 * Time: 6:10:49 PM
 */
public class ReporterMethodFactoryTest extends TestCase {

    public void testImport() {
        ReporterMethodFactory reporterFactory = ReporterMethodFactory.getInstance();
        File methodsFile = new File("exampleFiles/experiment/reporterMethods.xml");
        try {
            reporterFactory.importMethods(methodsFile);
            ArrayList<ReporterMethod> methods = reporterFactory.getMethods();
            String[] names = reporterFactory.getMethodsNames();
            int test = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
