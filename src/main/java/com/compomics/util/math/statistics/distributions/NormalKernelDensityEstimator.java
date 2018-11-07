package com.compomics.util.math.statistics.distributions;

import java.util.ArrayList;
import java.util.Arrays;
import umontreal.iro.lecuyer.gof.KernelDensity;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.KernelDensityGen;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * This class makes use of "SSJ: Stochastic Simulation in Java" library from
 * iro.umontreal.ca to estimate probability density function of an array of
 * double. It first generates independent and identically distributed random
 * variables from the dataset, at which the density needs to be computed and
 * then generates the vector of density estimates at the corresponding
 * variables.
 *
 * The KernelDensityGen class from the same library is used: the class
 * implements random variate generators for distributions obtained via kernel
 * density estimation methods from a set of n individual observations x1,...,
 * xn. The basic idea is to center a copy of the same symmetric density at each
 * observation and take an equally weighted mixture of the n copies as an
 * estimator of the density from which the observations come. The resulting
 * kernel density has the general form: fn(x) = (1/nh)?i=1nk((x - xi)/h). K is
 * the kernel (here a Gaussian is chosen) and h is the bandwidth (smoothing
 * factor).
 *
 * @author Paola Masuzzo
 */
public class NormalKernelDensityEstimator {

    /**
     * Empty default constructor
     */
    public NormalKernelDensityEstimator() {
    }

    /**
     * N, estimation precision, is set to a default of 512, as in most KDE
     * algorithms default values, i.e. R "density"function, OmicSoft, Matlab
     * algorithm.
     */
    private final int n = 4096;
    /**
     * The empirical distribution.
     */
    private EmpiricalDist empiricalDist;
    /**
     * The kernel density generator.
     */
    private KernelDensityGen kernelDensityGen;
    /**
     * The dataset size.
     */
    private double datasetSize;

    /**
     * This method initiates the KDE, i.e. sort values in ascending order,
     * compute an empirical distribution out of it, makes use of a NormalGen to
     * generate random variates from the normal distribution, and then use these
     * variates to generate a kernel density generator of the empirical
     * distribution.
     *
     * @param data the data
     */
    private void init(double[] data) {
        datasetSize = (double) data.length;
        Arrays.sort(data);
        empiricalDist = new EmpiricalDist(data);
        // new Stream to randomly generate numbers
        // combined multiple recursive generator (CMRG)
        RandomStream stream = new MRG31k3p();
        NormalGen normalKernelDensityGen = new NormalGen(stream);
        kernelDensityGen = new KernelDensityGen(stream, empiricalDist, normalKernelDensityGen);
    }

    /**
     * Estimate the density function.
     *
     * @param data the data
     * @return the estimated density function
     */
    public ArrayList estimateDensityFunction(Double[] data) {
        init(excludeNullValues(data)); // init the KDE with a normal generator
        return estimateDensityFunction();
    }

    /**
     * Estimate the density function.
     *
     * @param data the data
     * @return the estimated density function
     */
    public ArrayList estimateDensityFunction(double[] data) {
        init(data); // init the KDE with a normal generator
        return estimateDensityFunction();
    }

    /**
     * Estimate the density function.
     *
     * @return the estimated density function
     */
    private ArrayList estimateDensityFunction() {

        ArrayList densityFunction = new ArrayList();

        // array for random samples
        double[] randomSamples = new double[n];

        // compute x values
        for (int i = 0; i < n; i++) {
            double nextDouble = kernelDensityGen.nextDouble();
            randomSamples[i] = nextDouble;
        }

        Arrays.sort(randomSamples);
        densityFunction.add(randomSamples);

        // compute y values
        // use normal default kernel
        NormalDist kern = new NormalDist();

        // calculate optimal bandwidth with the (ROBUST) Silverman's "rule of thumb" (Scott Variation uses factor = 1.06)
        double bandWidth = 0.99 * Math.min(empiricalDist.getSampleStandardDeviation(), (empiricalDist.getInterQuartileRange() / 1.34)) / Math.pow(datasetSize, 0.2);

        // estimate density and store values in a vector
        double[] estimatedDensityValues = KernelDensity.computeDensity(empiricalDist, kern, bandWidth, randomSamples);
        densityFunction.add(estimatedDensityValues);

        return densityFunction;
    }

    /**
     * Exclude null values from an array of double.
     *
     * @param data the data
     * @return another double array with no longer null values
     */
    public double[] excludeNullValues(Double[] data) {
        ArrayList<Double> list = new ArrayList<>();
        for (Double value : data) {
            if (value != null) {
                list.add(value);
            }
        }
        double[] newArray = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            newArray[i] = list.get(i);
        }
        return newArray;
    }
}
