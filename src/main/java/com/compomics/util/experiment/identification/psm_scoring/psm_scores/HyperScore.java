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
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.HistogramUtils;
import com.compomics.util.math.statistics.linear_regression.LinearRegression;
import com.compomics.util.math.statistics.linear_regression.RegressionStatistics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.util.FastMath;

/**
 * Hyperscore as variation of the score implemented in X!Tandem www.thegpm.org/tandem. 
 * 
 * The original X!Tandem score at the link above is governed by the Artistic license (https://opensource.org/licenses/artistic-license-1.0).
 * X! tandem is a component of the X! proteomics software development project. Copyright Ronald C Beavis, all rights reserved.
 * 
 * The code below does not use or reuse any of the X!Tandem code, but the scoring approach is the same. No copyright infringement intended.
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
    private HashMap<Double, Integer> as = new HashMap<Double, Integer>();
    /**
     * Histogram of the values found for b in the fitting.
     */
    private HashMap<Double, Integer> bs = new HashMap<Double, Integer>();

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
     */
    public double getScore(Peptide peptide, MSnSpectrum spectrum, AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings, PeptideSpectrumAnnotator peptideSpectrumAnnotator) {

        ArrayList<IonMatch> matches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide);
        if (matches.isEmpty()) {
            return 0.0;
        }

        SpectrumIndex spectrumIndex = new SpectrumIndex();
        spectrumIndex = (SpectrumIndex) spectrum.getUrParam(spectrumIndex);
        if (spectrumIndex == null) {
            // Create new index
            spectrumIndex = new SpectrumIndex(spectrum.getPeakMap(), spectrum.getIntensityLimit(annotationSettings.getAnnotationIntensityLimit()),
                    annotationSettings.getFragmentIonAccuracy(), annotationSettings.isFragmentIonPpm());
            spectrum.addUrParam(spectrumIndex);
        }

        double xCorr = 0;
        HashSet<Integer> ionsForward = new HashSet<Integer>(1);
        HashSet<Integer> ionsRewind = new HashSet<Integer>(1);
        for (IonMatch ionMatch : matches) {
            Peak peakI = ionMatch.peak;
            Double x0I = peakI.intensity / spectrumIndex.getTotalIntensity();
            xCorr += x0I;
            Ion ion = ionMatch.ion;
            if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ion;
                if (ion.getSubType() == PeptideFragmentIon.X_ION
                        || ion.getSubType() == PeptideFragmentIon.Y_ION
                        || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                    ionsForward.add(peptideFragmentIon.getNumber());
                } else if (ion.getSubType() == PeptideFragmentIon.A_ION
                        || ion.getSubType() == PeptideFragmentIon.B_ION
                        || ion.getSubType() == PeptideFragmentIon.C_ION) {
                    ionsRewind.add(peptideFragmentIon.getNumber());
                }
            }
        }
        int nForward = ionsForward.size() > 20 ? 20 : ionsForward.size();
        int nRewind = ionsRewind.size() > 20 ? 20 : ionsRewind.size();
        long forwardFactorial = BasicMathFunctions.factorial(nForward);
        long rewindFactorial = BasicMathFunctions.factorial(nRewind);
        return xCorr * forwardFactorial * rewindFactorial;
    }

    /**
     * Returns the e-value corresponding to a list of scores in a map. If not enough scores are present or if they are not spread the method returns null.
     * 
     * @param hyperScores the different scores
     * 
     * @return the e-values corresponding to the given scores
     */
    public HashMap<Double, Double> getEValueHistogram(ArrayList<Double> hyperScores) {
        HashMap<Integer, Integer> histogram = new HashMap<Integer, Integer>();
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
        ArrayList<Integer> bins = new ArrayList<Integer>(histogram.keySet());
        for (Integer bin : bins) {
            if (bin > secondEmptybin) {
                histogram.remove(bin);
            } else if (bin > firstEmptybin) {
                histogram.put(bin, 1);
            }
        }
        bins = new ArrayList<Integer>(histogram.keySet());
        Collections.sort(bins, Collections.reverseOrder());
        ArrayList<Double> evalueFunctionX = new ArrayList<Double>(histogram.size());
        ArrayList<Double> evalueFunctionY = new ArrayList<Double>(histogram.size());
        Integer currentSum = 0;
        for (Integer bin : bins) {
            Integer nInBin = histogram.get(bin);
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
        return getInterpolation(hyperScores, regressionStatistics.a, regressionStatistics.b);
    }

    /**
     * Returns the interpolation of a list of hyperscores using a linear interpolation of the form result = a * log(score) + b. If the score is null, returns the number of hyperscores. The value at every score is returned in a map.
     * 
     * @param hyperScores a list of hyperscores
     * @param a the slope of the interpolation
     * @param b the offset of the interpolation
     * 
     * @return the interpolation for every score in a map.
     */
    public HashMap<Double, Double> getInterpolation(ArrayList<Double> hyperScores, Double a, Double b) {
        HashMap<Double, Double> result = new HashMap<Double, Double>();
        for (Double hyperScore : hyperScores) {
            if (!result.containsKey(hyperScore)) {
                if (hyperScore > 0) {
                Double logScore = FastMath.log10(hyperScore);
                Double eValue = getInterpolation(logScore, a, b);
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
     * Returns the interpolated value for a given score in log. result = a * logScore + b.
     * 
     * @param logScore the log of the score
     * @param a the slope of the interpolation
     * @param b the offset of the interpolation
     * 
     * @return the interpolated value
     */
    public static Double getInterpolation(Double logScore, Double a, Double b) {
        return b + a * logScore;
    }

    /**
     * Returns the rounded median of the as found in the previously interpolated scores. Null if none found.
     * 
     * @return the rounded median of the as found in the previously interpolated scores
     */
    public Double getMendianA() {
        if (as.isEmpty()) {
            return null;
        }
        return HistogramUtils.getMedianValue(as);
    }

    /**
     * Returns the rounded median of the bs found in the previously interpolated scores. Null if none found.
     * 
     * @return the rounded median of the bs found in the previously interpolated scores
     */
    public Double getMendianB() {
        if (bs.isEmpty()) {
            return null;
        }
        return HistogramUtils.getMedianValue(bs);
    }

    /**
     * Returns a histogram of the as found in the previously interpolated scores.
     * 
     * @return a histogram of the as found in the previously interpolated scores
     */
    public HashMap<Double, Integer> getAs() {
        return as;
    }

    /**
     * Returns a histogram of the bs found in the previously interpolated scores.
     * 
     * @return a histogram of the bs found in the previously interpolated scores
     */
    public HashMap<Double, Integer> getBs() {
        return bs;
    }
    
    
}
