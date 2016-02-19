package com.compomics.util.math.statistics.distributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.BigFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import org.apache.commons.math.MathException;
import org.apache.commons.math.util.FastMath;

/**
 * Implementation of a binomial distribution.
 *
 * @author Marc Vaudel
 */
public class BinomialDistribution implements Distribution {

    /**
     * The number of trials.
     */
    private int n;
    /**
     * The number of trials as big integer.
     */
    private BigInteger nBI = null;
    /**
     * The probability of success of each trial.
     */
    private double p;
    /**
     * The precision needed for p.
     */
    private int precisionP;
    /**
     * p as BigDecimal.
     */
    private BigDecimal pBD = null;
    /**
     * 1-p as BigDecimal.
     */
    private BigDecimal oneMinuspBD = null;

    /**
     * Constructor.
     *
     * @param n the number of trials
     * @param p the probability of success of each trial
     */
    public BinomialDistribution(int n, double p) {
        this.n = n;
        this.p = p;
        precisionP = (int) -BasicMathFunctions.log(p, 10);
    }

    /**
     * Returns n as BigInteger.
     *
     * @return n as BigInteger
     */
    public BigInteger getNBI() {
        if (nBI == null) {
            nBI = new BigInteger(n + "");
        }
        return nBI;
    }

    /**
     * Returns p as big decimal.
     *
     * @return p as big decimal
     */
    public BigDecimal getPBigDecimal() {
        if (pBD == null) {
            pBD = new BigDecimal(p);
        }
        return pBD;
    }

    /**
     * Returns 1-p as big decimal.
     *
     * @return 1-p as big decimal
     */
    public BigDecimal getOneMinusPBigDecimal() {
        if (oneMinuspBD == null) {
            oneMinuspBD = new BigDecimal(1 - p);
        }
        return oneMinuspBD;
    }

    @Override
    public BigDecimal getProbabilityAt(double x, MathContext mathContext) {

        if (p == 0 || p == 1) {
            return BigDecimal.ZERO;
        }

        int precisionLimit = -300 + mathContext.getPrecision();
        int i = (int) x;
        int extraPrecision = 0;
        if (i > 0) {
            extraPrecision = (int) FastMath.log10((double) i);
        }

        // check whether the calculation needs to be done with big objects
        boolean needBigObjects = false;
        Long combinations = BasicMathFunctions.getCombination(i, n);
        BigInteger conbinationsBI = null;

        if (combinations == null) {
            BigInteger iBI = new BigInteger(i + "");
            conbinationsBI = BigFunctions.getCombination(iBI, getNBI());
            if (conbinationsBI.compareTo(new BigInteger(Long.MAX_VALUE + "")) == -1) {
                combinations = conbinationsBI.longValue();
            } else {
                needBigObjects = true;
            }
        }

        if (!needBigObjects && (i > 0 && i * precisionP <= precisionLimit + extraPrecision || n - i > 0 && (n - i) * precisionP <= precisionLimit + extraPrecision)) {
            needBigObjects = true;
        }

        BigDecimal result;

        if (needBigObjects) {
            if (i > 1) {
                result = BigDecimal.ONE;
            } else if (i == 1) {
                result = getPBigDecimal();
            } else {
                result = getPBigDecimal().pow(i);
            }
            if (combinations != null) {
                result.multiply(new BigDecimal(combinations));
            } else {
                result.multiply(new BigDecimal(conbinationsBI));
            }
            if (n - i == 1) {
                result = result.multiply(getOneMinusPBigDecimal());
            } else if (n - i > 1) {
                result = result.multiply(getOneMinusPBigDecimal().pow(n - i));
            }
        } else {
            double product = FastMath.pow(p, i);
            product *= combinations;
            product *= FastMath.pow(1 - p, n - i);
            result = new BigDecimal(product);
        }

        return result;
    }

    @Override
    public BigDecimal getCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {

        int k = (int) x;
        int extraPrecision = Math.max(k, n - k) + precisionP;
        MathContext tempMathContext = new MathContext(mathContext.getPrecision() + extraPrecision, mathContext.getRoundingMode());
        BigDecimal result = BigDecimal.ZERO;

        if (k > n * p) {

            // estimate 1-P to be faster
            if (k < n) {
                for (int i = k + 1; i <= n; i++) {
                    BigDecimal probability = getProbabilityAt(i, tempMathContext);
                    result = result.add(probability);
                }
            }

            return BigDecimal.ONE.subtract(result);

        } else {

            for (int i = 0; i <= k; i++) {
                BigDecimal probability = getProbabilityAt(i, tempMathContext);
                result = result.add(probability);
            }
        }

        return result;
    }

    @Override
    public BigDecimal getDescendingCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {

        int k = (int) x;
        int extraPrecision = Math.max(k, n - k);
        MathContext tempMathContext = new MathContext(mathContext.getPrecision() + extraPrecision, mathContext.getRoundingMode());
        BigDecimal result = BigDecimal.ZERO;

        if (k < n * p) {

            // estimate 1-P to be faster
            for (int i = 0; i < k; i++) {
                BigDecimal probability = getProbabilityAt(i, tempMathContext);
                result = result.add(probability);
            }

            return BigDecimal.ONE.subtract(result);

        } else {

            for (int i = k; i <= n; i++) {
                BigDecimal probability = getProbabilityAt(i, tempMathContext);
                result = result.add(probability);
            }

        }

        return result;
    }

    @Override
    public BigDecimal getSmallestCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BigDecimal getMaxValueForProbability(double p, MathContext mathContext) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public BigDecimal getMinValueForProbability(double p, MathContext mathContext) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public BigDecimal getValueAtCumulativeProbability(double p, MathContext mathContext) throws MathException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public BigDecimal getValueAtDescendingCumulativeProbability(double p, MathContext mathContext) throws MathException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
