package com.compomics.util.test.math;

import com.compomics.util.math.BigFunctions;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.math.util.FastMath;

/**
 * Tests for the math functions on big integers
 *
 * @author Marc Vaudel
 */
public class TestBigFunctions extends TestCase {

    /**
     * The math context.
     */
    private MathContext mathContext;
    /**
     * The tolerance to use when testing the results.
     */
    private BigDecimal tolerance;

    /**
     * Constructor.
     */
    public TestBigFunctions() {
        mathContext = new MathContext(10, RoundingMode.HALF_DOWN);
        tolerance = new BigDecimal(FastMath.pow(10, -mathContext.getPrecision() + 1));
    }

    /**
     * Tests the exp function
     */
    public void testExp() {

        BigDecimal expLimit = BigFunctions.getMaxExp(mathContext);

        for (int i = -4; i <= 4; i++) {
            
            double x = FastMath.pow(10, i);
            BigDecimal xBD = new BigDecimal(x);

            if (xBD.compareTo(expLimit) == -1) {

                BigDecimal utilitiesResult = BigFunctions.exp(xBD, mathContext);

                if (x < 709) {
                    double fastMathResult = FastMath.exp(x);
                    BigDecimal fastMathResultBD = new BigDecimal(fastMathResult);

                    BigDecimal error = utilitiesResult.subtract(fastMathResultBD);

                    BigDecimal expTolerance = tolerance.multiply(utilitiesResult);

                    Assert.assertEquals(-1, error.abs().compareTo(expTolerance));
                }

                x = -x;
                double fastMathResult = FastMath.exp(x);
                BigDecimal fastMathResultBD = new BigDecimal(fastMathResult);

                xBD = new BigDecimal(x);
                utilitiesResult = BigFunctions.exp(xBD, mathContext);

                BigDecimal error = utilitiesResult.subtract(fastMathResultBD);

                Assert.assertEquals(-1, error.abs().compareTo(tolerance));
            }
        }
    }

    /**
     * Tests the expBD function
     */
    public void testExpBD() {

        BigDecimal expLimit = BigFunctions.getMaxExp(mathContext);

        for (int i = -4; i <= 4; i++) {
            
            double x = FastMath.pow(10, i);
            BigDecimal xBD = new BigDecimal(x);

            if (xBD.compareTo(expLimit) == -1) {
                
                BigDecimal utilitiesResult = BigFunctions.expBD(xBD, mathContext);

                if (x < 709) {
                    double fastMathResult = FastMath.exp(x);
                    BigDecimal fastMathResultBD = new BigDecimal(fastMathResult);

                    BigDecimal error = utilitiesResult.subtract(fastMathResultBD);

                    BigDecimal expTolerance = tolerance.multiply(utilitiesResult);

                    Assert.assertEquals(-1, error.abs().compareTo(expTolerance));
                }

                x = -x;
                double fastMathResult = FastMath.exp(x);
                BigDecimal fastMathResultBD = new BigDecimal(fastMathResult);

                xBD = new BigDecimal(x);
                utilitiesResult = BigFunctions.expBD(xBD, mathContext);

                BigDecimal error = utilitiesResult.subtract(fastMathResultBD);

                Assert.assertEquals(-1, error.abs().compareTo(tolerance));
            }
        }

    }

    /**
     * Tests the ln function
     */
    public void testLn() {

        for (int i = -100; i <= 100; i++) {
            double x = FastMath.pow(10, i);
            double fastMathResult = FastMath.log(x);
            BigDecimal fastMathResultBD = new BigDecimal(fastMathResult);

            BigDecimal xBD = new BigDecimal(x);
            BigDecimal utilitiesResult = BigFunctions.ln(xBD, mathContext);

            BigDecimal error = utilitiesResult.subtract(fastMathResultBD);

            Assert.assertEquals(-1, error.abs().compareTo(tolerance));
        }

    }

    /**
     * Tests the lnBD function
     */
    public void testLnBD() {

        for (int i = -100; i <= 100; i++) {
            double x = FastMath.pow(10, i);
            double fastMathResult = FastMath.log(x);
            BigDecimal fastMathResultBD = new BigDecimal(fastMathResult);

            BigDecimal xDB = new BigDecimal(x);
            BigDecimal utilitiesResult = BigFunctions.lnBD(xDB, mathContext);

            BigDecimal error = utilitiesResult.subtract(fastMathResultBD);

            Assert.assertEquals(-1, error.abs().compareTo(tolerance));
        }

    }

}
