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

        if (x < 0 || x > n) {
            throw new IllegalArgumentException("Attempting to estimate the probability at " + x + ".");
        }

        if (p == 0 || p == 1) {
            return BigDecimal.ZERO;
        }

        int precisionLimit = -300 + mathContext.getPrecision();
        int k = (int) x;
        int extraPrecision = 0;
        if (k > 0) {
            extraPrecision = (int) FastMath.log10((double) k);
        }

        // check whether the calculation needs to be done with big objects
        boolean needBigObjects = false;
        Long combinations = BasicMathFunctions.getCombination(k, n);
        BigInteger conbinationsBI = null;

        if (combinations == null) {
            BigInteger iBI = new BigInteger(k + "");
            conbinationsBI = BigFunctions.getCombination(iBI, getNBI());
            if (conbinationsBI.compareTo(new BigInteger(Long.MAX_VALUE + "")) == -1) {
                combinations = conbinationsBI.longValue();
            } else {
                needBigObjects = true;
            }
        }

        if (!needBigObjects && (k > 0 && k * precisionP <= precisionLimit + extraPrecision || n - k > 0 && (n - k) * precisionP <= precisionLimit + extraPrecision)) {
            needBigObjects = true;
        }

        BigDecimal result;

        if (needBigObjects) {
            if (k == 0) {
                result = BigDecimal.ONE;
            } else if (k == 1) {
                result = getPBigDecimal();
            } else {
                result = getPBigDecimal().pow(k);
            }
            if (combinations != null) {
                result = result.multiply(new BigDecimal(combinations));
            } else {
                BigDecimal combinationsBD = new BigDecimal(conbinationsBI);
                result = result.multiply(combinationsBD);
            }
            if (n - k == 1) {
                result = result.multiply(getOneMinusPBigDecimal());
            } else if (n - k > 1) {
                BigDecimal oneMinusPNK = getOneMinusPBigDecimal().pow(n - k);
                result = result.multiply(oneMinusPNK);
            }
        } else {
            double product = FastMath.pow(p, k);
            product *= combinations;
            product *= FastMath.pow(1 - p, n - k);
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
