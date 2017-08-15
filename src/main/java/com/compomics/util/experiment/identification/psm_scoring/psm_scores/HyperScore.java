package com.compomics.util.experiment.identification.psm_scoring.psm_scores;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.peptide_fragmentation.PeptideFragmentationModel;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.spectra.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.spectra.Peak;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.HistogramUtils;
import com.compomics.util.math.statistics.linear_regression.LinearRegression;
import com.compomics.util.math.statistics.linear_regression.RegressionStatistics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.MathException;
import org.apache.commons.math.util.FastMath;

/**
 * Hyperscore as variation of the score implemented in X!Tandem
 * www.thegpm.org/tandem.
 *
 * The original X!Tandem score at the link above is governed by the Artistic
 * license (https://opensource.org/licenses/artistic-license-1.0). X! tandem is
 * a component of the X! proteomics software development project. Copyright
 * Ronald C Beavis, all rights reserved.
 *
 * The code below does not use or reuse any of the X!Tandem code, but the
 * scoring approach is in many points similar. No copyright infringement
 * intended.
 *
 * @author Marc Vaudel
 */
public class HyperScore {

    /**
     * The peptide fragmentation model to use.
     */
    private PeptideFragmentationModel peptideFragmentationModel;
    /**
     * Histogram of the values found for a in the fitting.
     */
    private HashMap<Double, Integer> as = new HashMap<>();
    /**
     * Histogram of the values found for b in the fitting.
     */
    private HashMap<Double, Integer> bs = new HashMap<>();

    /**
     * Constructor.
     *
     * @param peptideFragmentationModel the peptide fragmentation model to use
     */
    public HyperScore(PeptideFragmentationModel peptideFragmentationModel) {
        this.peptideFragmentationModel = peptideFragmentationModel;
    }

    /**
     * Constructor using a uniform fragmentation.
     */
    public HyperScore() {
        this(PeptideFragmentationModel.uniform);
    }

    /**
     * Returns the hyperscore.
     *
     * @param peptide the peptide of interest
     * @param spectrum the spectrum of interest
     * @param annotationSettings the general spectrum annotation settings
     * @param specificAnnotationSettings the annotation settings specific to
     * this PSM
     * @param peptideSpectrumAnnotator the spectrum annotator to use
     *
     * @return the score of the match
     * 
     * @throws java.lang.InterruptedException exception thrown if a threading error occurred when estimating the noise level
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings, PeptideSpectrumAnnotator peptideSpectrumAnnotator) throws InterruptedException, MathException {
        ArrayList<IonMatch> ionMatches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide);
        return getScore(peptide, specificAnnotationSettings.getPrecursorCharge(), spectrum, ionMatches);
    }

    /**
     * Returns the hyperscore.
     *
     * @param peptide the peptide of interest
     * @param charge the charge
     * @param spectrum the spectrum of interest
     * @param ionMatches the ion matches obtained from spectrum annotation
     *
     * @return the score of the match
     * 
     * @throws java.lang.InterruptedException exception thrown if a threading error occurred when estimating the noise level
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public double getScore(Peptide peptide, int charge, MSnSpectrum spectrum, ArrayList<IonMatch> ionMatches) throws InterruptedException, MathException {

        boolean peakMatched = false;
        Double coveredIntensity = 0.0;
        HashSet<Double> coveredMz = new HashSet<>(2);
        for (IonMatch ionMatch : ionMatches) {
            Ion ion = ionMatch.ion;
            Peak peak = ionMatch.peak;
            if (!coveredMz.contains(peak.mz)) {
                coveredIntensity += peak.intensity;
                coveredMz.add(peak.mz);
            }
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                if (peptideFragmentIon.hasNeutralLosses() || peptideFragmentIon.getNumber() < 2) {
                } else {
                    peakMatched = true;
                }
            }
        }
        if (!peakMatched) {
            return 0.0;
        }

        Double consideredIntensity = spectrum.getTotalIntensity() - coveredIntensity;

        double xCorr = 0;
        HashSet<Integer> ionsForward = new HashSet<>(1);
        HashSet<Integer> ionsRewind = new HashSet<>(1);
        HashSet<Double> accountedFor = new HashSet<>(ionMatches.size());
        for (IonMatch ionMatch : ionMatches) {
            Peak peakI = ionMatch.peak;
            Double mz = peakI.mz;
            Ion ion = ionMatch.ion;
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION && !ion.hasNeutralLosses() && !accountedFor.contains(mz)) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                int number = peptideFragmentIon.getNumber();
                if (number > 1) {
                    accountedFor.add(mz);
                    Double x0I = peakI.intensity / consideredIntensity;
                    xCorr += x0I;
                    if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION && !ion.hasNeutralLosses()) {
                        if (ion.getSubType() == PeptideFragmentIon.X_ION
                                || ion.getSubType() == PeptideFragmentIon.Y_ION
                                || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                            ionsForward.add(number);
                        } else if (ion.getSubType() == PeptideFragmentIon.A_ION
                                || ion.getSubType() == PeptideFragmentIon.B_ION
                                || ion.getSubType() == PeptideFragmentIon.C_ION) {
                            ionsRewind.add(number);
                        }
                    }
                }
            }
        }
        int nForward = ionsForward.size() / (Math.max(charge - 1, 1));
        int nRewind = ionsRewind.size() / (Math.max(charge - 1, 1));
        nForward = nForward > 20 ? 20 : nForward;
        nRewind = nRewind > 20 ? 20 : nRewind;
        long forwardFactorial = BasicMathFunctions.factorial(nForward);
        long rewindFactorial = BasicMathFunctions.factorial(nRewind);
        return xCorr * forwardFactorial * rewindFactorial;
    }

    /**
     * Returns the e-value corresponding to a list of scores in a map. If not
     * enough scores are present or if they are not spread the method returns
     * null.
     *
     * @param hyperScores the different scores
     *
     * @return the e-values corresponding to the given scores
     */
    public HashMap<Double, Double> getEValueMap(ArrayList<Double> hyperScores) {
        return getEValueMap(hyperScores, true);
    }

    /**
     * Returns the e-value corresponding to a list of scores in a map. If not
     * enough scores are present or if they are not spread the method returns
     * null.
     *
     * @param hyperScores the different scores
     * @param useCache if true the interpolation values will be stored in the
     * histograms in cache
     *
     * @return the e-values corresponding to the given scores
     */
    public HashMap<Double, Double> getEValueMap(ArrayList<Double> hyperScores, boolean useCache) {
        HashMap<Integer, Integer> histogram = new HashMap<>();
        Double maxScore = 0.0;
        Double minScore = Double.MAX_VALUE;
        for (Double score : hyperScores) {
            Integer intValue = score.intValue();
            if (intValue > 0) {
                Integer nScores = histogram.get(intValue);
                if (nScores == null) {
                    nScores = 1;
                } else {
                    nScores++;
                }
                histogram.put(intValue, nScores);
                if (score > maxScore) {
                    maxScore = score;
                }
                if (score < minScore) {
                    minScore = score;
                }
            }
        }
        Integer lowestBin = minScore.intValue();
        Integer highestBin = maxScore.intValue();
        Integer secondEmptybin = highestBin;
        Integer firstEmptybin = highestBin;
        boolean emptyBin = false;
        for (Integer bin = lowestBin; bin <= highestBin; bin++) {
            if (!histogram.containsKey(bin)) {
                if (!emptyBin) {
                    emptyBin = true;
                    firstEmptybin = bin;
                } else {
                    secondEmptybin = bin;
                    break;
                }
            }
        }
        ArrayList<Integer> bins = new ArrayList<>(histogram.keySet());
        for (Integer bin : bins) {
            if (bin > secondEmptybin) {
                histogram.remove(bin);
            } else if (bin > firstEmptybin) {
                histogram.put(bin, 1);
            }
        }
        double[] ab = getInterpolationValues(histogram, useCache);
        if (ab == null) {
            return null;
        }
        return getInterpolation(hyperScores, ab[0], ab[1]);
    }

    /**
     * Returns the interpolation values for the given scores in the form {a, b}.
     *
     * @param scores the scores
     * @param useCache if true the interpolation values will be stored in the
     * histograms in cache
     *
     * @return the interpolation values for the given scores
     */
    public double[] getInterpolationValues(int[] scores, boolean useCache) {
        HashMap<Integer, Integer> scoreHistogram = new HashMap<>();
        int maxScore = 0;
        int minScore = Integer.MAX_VALUE;
        for (int score : scores) {
            if (score > 0) {
                Integer nScores = scoreHistogram.get(score);
                if (nScores == null) {
                    nScores = 1;
                } else {
                    nScores++;
                }
                scoreHistogram.put(score, nScores);
                if (score > maxScore) {
                    maxScore = score;
                }
                if (score < minScore) {
                    minScore = score;
                }
            }
        }
        Integer secondEmptybin = maxScore;
        Integer firstEmptybin = maxScore;
        boolean emptyBin = false;
        for (int bin = minScore; bin <= maxScore; bin++) {
            if (!scoreHistogram.containsKey(bin)) {
                if (!emptyBin) {
                    emptyBin = true;
                    firstEmptybin = bin;
                } else {
                    secondEmptybin = bin;
                    break;
                }
            }
        }
        ArrayList<Integer> bins = new ArrayList<>(scoreHistogram.keySet());
        for (Integer bin : bins) {
            if (bin > secondEmptybin) {
                scoreHistogram.remove(bin);
            } else if (bin > firstEmptybin) {
                scoreHistogram.put(bin, 1);
            }
        }
        return getInterpolationValues(scoreHistogram, useCache);
    }

    /**
     * Returns the interpolation values for the given score histogram in the
     * form {a, b}.
     *
     * @param scoreHistogram the score histogram
     * @param useCache if true the interpolation values will be stored in the
     * histograms in cache
     *
     * @return the interpolation values for the given score histogram
     */
    public double[] getInterpolationValues(HashMap<Integer, Integer> scoreHistogram, boolean useCache) {

        ArrayList<Integer> bins = new ArrayList<>(scoreHistogram.keySet());
        Collections.sort(bins, Collections.reverseOrder());
        ArrayList<Double> evalueFunctionX = new ArrayList<>(scoreHistogram.size());
        ArrayList<Double> evalueFunctionY = new ArrayList<>(scoreHistogram.size());
        Integer currentSum = 0;
        for (Integer bin : bins) {
            Integer nInBin = scoreHistogram.get(bin);
            if (nInBin != null) {
                currentSum += nInBin;
            }
            if (currentSum > 0) {
                Double xValue = new Double(bin);
                xValue = FastMath.log10(xValue);
                evalueFunctionX.add(xValue);
                Double yValue = new Double(currentSum);
                yValue = FastMath.log10(yValue);
                evalueFunctionY.add(yValue);
            }
        }
        if (evalueFunctionX.size() <= 1) {
            return null;
        }
        RegressionStatistics regressionStatistics = LinearRegression.getSimpleLinearRegression(evalueFunctionX, evalueFunctionY);
        if (useCache) {
            Double roundedA = Util.roundDouble(regressionStatistics.a, 2);
            Double roundedB = Util.roundDouble(regressionStatistics.b, 2);
            Integer nA = as.get(roundedA);
            if (nA == null) {
                as.put(roundedA, 1);
            } else {
                as.put(roundedA, nA + 1);
            }
            Integer nB = bs.get(roundedB);
            if (nB == null) {
                bs.put(roundedB, 1);
            } else {
                bs.put(roundedB, nB + 1);
            }
        }
        return new double[]{regressionStatistics.a, regressionStatistics.b};
    }

    /**
     * Returns the interpolation of a list of hyperscores using a linear
     * interpolation of the form result = a * log(score) + b. If the score is
     * null, returns the number of hyperscores. The value at every score is
     * returned in a map.
     *
     * @param hyperScores a list of hyperscores
     * @param a the slope of the interpolation
     * @param b the offset of the interpolation
     *
     * @return the interpolation for every score in a map.
     */
    public HashMap<Double, Double> getInterpolation(ArrayList<Double> hyperScores, Double a, Double b) {
        HashMap<Double, Double> result = new HashMap<>();
        for (Double hyperScore : hyperScores) {
            if (!result.containsKey(hyperScore)) {
                if (hyperScore > 0) {
                    double logScore = FastMath.log10(hyperScore);
                    double eValue = getInterpolation(logScore, a, b);
                    result.put(hyperScore, eValue);
                } else {
                    Double eValue = new Double(hyperScores.size());
                    result.put(hyperScore, eValue);
                }
            }
        }
        return result;
    }

    /**
     * Returns the interpolated value for a given score in log. result = a *
     * logScore + b.
     *
     * @param logScore the log of the score
     * @param a the slope of the interpolation
     * @param b the offset of the interpolation
     *
     * @return the interpolated value
     */
    public static double getInterpolation(double logScore, double a, double b) {
        return b + a * logScore;
    }

    /**
     * Returns the rounded median of the as found in the previously interpolated
     * scores. Null if none found.
     *
     * @return the rounded median of the as found in the previously interpolated
     * scores
     */
    public Double getMendianA() {
        if (as.isEmpty()) {
            return null;
        }
        return HistogramUtils.getMedianValue(as);
    }

    /**
     * Returns the rounded median of the bs found in the previously interpolated
     * scores. Null if none found.
     *
     * @return the rounded median of the bs found in the previously interpolated
     * scores
     */
    public Double getMendianB() {
        if (bs.isEmpty()) {
            return null;
        }
        return HistogramUtils.getMedianValue(bs);
    }

    /**
     * Returns a histogram of the as found in the previously interpolated
     * scores.
     *
     * @return a histogram of the as found in the previously interpolated scores
     */
    public HashMap<Double, Integer> getAs() {
        return as;
    }

    /**
     * Returns a histogram of the bs found in the previously interpolated
     * scores.
     *
     * @return a histogram of the bs found in the previously interpolated scores
     */
    public HashMap<Double, Integer> getBs() {
        return bs;
    }
}
