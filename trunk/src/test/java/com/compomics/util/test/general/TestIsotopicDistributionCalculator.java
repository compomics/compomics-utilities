package com.compomics.util.test.general;

import com.compomics.util.general.IsotopicDistribution;
import junit.TestCaseLM;
import junit.framework.Assert;
import org.apache.log4j.Logger;

/**
 * Test for the IsotopicDistribution class.
 *
 * @see com.compomics.util.general.IsotopicDistribution
 * @author	NiklaasColaert
 */
public class TestIsotopicDistributionCalculator extends TestCaseLM {
    
    // Class specific log4j logger for IsotopicDistribution instances.
    Logger logger = Logger.getLogger(TestMassCalc.class);

    public TestIsotopicDistributionCalculator() {
        this("Test for the IsotopicDistribution class.");
    }

    public TestIsotopicDistributionCalculator(String aName) {
        super(aName);
    }

    /**
     * This method test the actual calculation algorithm.
     */
    public void testCalculator() {
        try {

            IsotopicDistribution lCalc = new IsotopicDistribution(60, 13, 86, 13, 2);

            Assert.assertEquals(0.39350045799282984, lCalc.getPercMax()[2]);
            Assert.assertEquals(0.1662899391500604, lCalc.getPercTot()[2]);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }
}
