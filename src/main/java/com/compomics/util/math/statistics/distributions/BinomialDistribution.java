package com.compomics.util.math.statistics.distributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.BigFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import java.util.HashSet;
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
     * The maximal number of keys kept in each cache map.
     */
    private int cacheSize = 1000;
    /**
     * A cache for the probabilities.
     */
    private HashMap<Integer, BigDecimal> pCache = new HashMap<Integer, BigDecimal>();
    /**
     * A cache for the cumulative probabilities.
     */
    private HashMap<Integer, BigDecimal> cumulativePCache = new HashMap<Integer, BigDecimal>();
    /**
     * A cache for the cumulative probabilities.
     */
    private HashMap<Integer, BigDecimal> descendingCumulativePCache = new HashMap<Integer, BigDecimal>();

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
            // Check if the result is in cache
            BigDecimal pInCache = pCache.get(k);
            if (pInCache != null) {
                return pInCache;
            }
            // Estimate p
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
            addPToCache(k, result);
        } else {
            double product = FastMath.pow(p, k);
            product *= combinations;
            product *= FastMath.pow(1 - p, n - k);
            result = new BigDecimal(product);
        }

        return result;
    }

    /**
     * Adds a probability to the cache and manages the cache size.
     *
     * @param k the value
     * @param p the probability
     */
    private synchronized void addPToCache(int k, BigDecimal p) {
        if (pCache.size() >= cacheSize) {
            HashSet<Integer> keys = new HashSet<Integer>(pCache.keySet());
            for (Integer key : keys) {
                pCache.remove(key);
                if (pCache.size() < cacheSize) {
                    break;
                }
            }
        }
        pCache.put(k, p);
    }

    @Override
    public BigDecimal getCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {

        int k = (int) x;
        int extraPrecision = Math.max(k, n - k) + precisionP;
        MathContext tempMathContext = new MathContext(mathContext.getPrecision() + extraPrecision, mathContext.getRoundingMode());

        BigDecimal result = BigDecimal.ZERO;

        // Check if the result is in cache
        BigDecimal pInCache = cumulativePCache.get(k);
        if (pInCache != null) {
            return pInCache;
        }
        int nOperations = 0;

        // Estimate p
        if (k > n * p) {

            // estimate 1-p to be faster
            if (k < n) {
                for (int i = k + 1; i <= n; i++) {
                    BigDecimal probability = getProbabilityAt(i, tempMathContext);
                    result = result.add(probability);
                    nOperations++;
                }
            }

            result = BigDecimal.ONE.subtract(result);

        } else {

            for (int i = 0; i <= k; i++) {
                BigDecimal probability = getProbabilityAt(i, tempMathContext);
                result = result.add(probability);
                nOperations++;
            }
        }

        if (nOperations > 50) {
            addCumulativePToCache(k, result);
        }

        return result;
    }

    /**
     * Adds a probability to the cache and manages the cache size.
     *
     * @param k the value
     * @param p the probability
     */
    private synchronized void addCumulativePToCache(int k, BigDecimal p) {
        if (cumulativePCache.size() >= cacheSize) {
            HashSet<Integer> keys = new HashSet<Integer>(cumulativePCache.keySet());
            for (Integer key : keys) {
                cumulativePCache.remove(key);
                if (cumulativePCache.size() < cacheSize) {
                    break;
                }
            }
        }
        cumulativePCache.put(k, p);
    }

    @Override
    public BigDecimal getDescendingCumulativeProbabilityAt(double x, MathContext mathContext) throws MathException {

        int k = (int) x;
        int extraPrecision = Math.max(k, n - k);
        MathContext tempMathContext = new MathContext(mathContext.getPrecision() + extraPrecision, mathContext.getRoundingMode());
        BigDecimal result = BigDecimal.ZERO;

        // Check if the result is in cache
        BigDecimal pInCache = descendingCumulativePCache.get(k);
        if (pInCache != null) {
            return pInCache;
        }
        int nOperations = 0;

        // Estimate p
        if (k < n * p) {

            // estimate 1-p to be faster
            for (int i = 0; i < k; i++) {
                BigDecimal probability = getProbabilityAt(i, tempMathContext);
                result = result.add(probability);
                nOperations++;
            }

            result = BigDecimal.ONE.subtract(result);

        } else {
            for (int i = k; i <= n; i++) {
                BigDecimal probability = getProbabilityAt(i, tempMathContext);
                result = result.add(probability);
                nOperations++;
            }
        }

        if (nOperations > 50) {
            addDescendingCumulativePToCache(k, result);
        }

        return result;
    }

    /**
     * Adds a probability to the cache and manages the cache size.
     *
     * @param k the value
     * @param p the probability
     */
    private synchronized void addDescendingCumulativePToCache(int k, BigDecimal p) {
        if (descendingCumulativePCache.size() >= cacheSize) {
            HashSet<Integer> keys = new HashSet<Integer>(descendingCumulativePCache.keySet());
            for (Integer key : keys) {
                descendingCumulativePCache.remove(key);
                if (descendingCumulativePCache.size() < cacheSize) {
                    break;
                }
            }
        }
        descendingCumulativePCache.put(k, p);
    }

    /**
     * Indicates whether all caches are empty.
     *
     * @return a boolean indicating whether all caches are empty
     */
    public boolean isCacheEmpty() {
        return pCache.isEmpty() && cumulativePCache.isEmpty() && descendingCumulativePCache.isEmpty();
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
