package com.compomics.util.math.statistics.distributions;

import com.compomics.util.math.statistics.Distribution;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.special.Beta;

/**
 * Implementation of a binomial distribution.
 *
 * @author Marc Vaudel
 */
public class BinomialDistribution implements Distribution {

    /**
     * Instance of the apache distribution.
     */
    private BinomialDistributionImpl binomialDistributionImpl;
    /**
     * The number of trials.
     */
    private int n;
    /**
     * The probability of success of each trial.
     */
    private double p;
    /**
     * The maximal number of keys kept in each cache map.
     */
    private int cacheSize = 1000;
    /**
     * A cache for the probabilities.
     */
    private HashMap<Integer, Double> pCache = new HashMap<Integer, Double>();
    /**
     * A cache for the cumulative probabilities.
     */
    private HashMap<Integer, Double> descendingCumulativePCache = new HashMap<Integer, Double>();

    /**
     * Constructor.
     *
     * @param n the number of trials
     * @param p the probability of success of each trial
     */
    public BinomialDistribution(int n, double p) {
        this.n = n;
        this.p = p;
        binomialDistributionImpl = new BinomialDistributionImpl(n, p);
    }

    @Override
    public Double getProbabilityAt(double x) {

        if (x < 0 || x > n) {
            return 0.0;
        }

        int k = (int) x;
        Double result = pCache.get(k);
        if (result == null) {
            result = binomialDistributionImpl.probability(k);
            addPToCache(k, result);
        }

        return result;

    }

    /**
     * Adds a probability to the cache and manages the cache size.
     *
     * @param k the value
     * @param p the probability
     */
    private synchronized void addPToCache(int k, Double p) {
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
    public Double getCumulativeProbabilityAt(double x) throws MathException {

        return 1.0 - getDescendingCumulativeProbabilityAt(x);
    }

    @Override
    public Double getDescendingCumulativeProbabilityAt(double x) throws MathException {

        int k = (int) x;
        if (k > n) {
            return 0.0;
        } else if (k < 0) {
            return 1.0;
        }
        Double result = pCache.get(k);
        if (result == null) {
            // adapted from http://commons.apache.org/proper/commons-math/apidocs/src-html/org/apache/commons/math3/distribution/BinomialDistribution.html#line.130
            result = Beta.regularizedBeta(p, x + 1.0, n - x);
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
    private synchronized void addDescendingCumulativePToCache(int k, Double p) {
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
        return pCache.isEmpty() && descendingCumulativePCache.isEmpty();
    }

    @Override
    public Double getSmallestCumulativeProbabilityAt(double x) throws MathException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Double getMaxValueForProbability(double p) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Double getMinValueForProbability(double p) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Double getValueAtCumulativeProbability(double p) throws MathException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Double getValueAtDescendingCumulativeProbability(double p) throws MathException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
