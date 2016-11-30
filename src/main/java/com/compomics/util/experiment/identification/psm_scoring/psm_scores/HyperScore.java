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
 * Simple cross correlation score.
 *
 * @author Marc Vaudel
 */
public class HyperScore {

    /**
     * The peptide fragmentation model to use.
     */
    private PeptideFragmentationModel peptideFragmentationModel;

    private String histogramFile = "C:\\Projects\\scores\\comparison xtandem\\debug_histogram.txt";
    private String valuesFile = "C:\\Projects\\scores\\comparison xtandem\\debug_values.txt";
    private String fittingFile = "C:\\Projects\\scores\\comparison xtandem\\fitting_values.txt";

    private BufferedWriter bwFitting;

    private HashMap<Double, Integer> as = new HashMap<Double, Integer>();

    private HashMap<Double, Integer> bs = new HashMap<Double, Integer>();

    private int nAbInCache = 0;

    /**
     * Constructor.
     *
     * @param peptideFragmentationModel the peptide fragmentation model to use
     */
    public HyperScore(PeptideFragmentationModel peptideFragmentationModel) {
        this.peptideFragmentationModel = peptideFragmentationModel;
        try {
            bwFitting = new BufferedWriter(new FileWriter(fittingFile));
            bwFitting.write("a\tb");
            bwFitting.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor using a uniform fragmentation.
     */
    public HyperScore() {
        this(PeptideFragmentationModel.uniform);
    }

    /**
     * Scores the match between the given peptide and spectrum using an m/z
     * fidelity score. The mass interquartile distance of the fragment ion mass
     * error is used as m/z fidelity score.
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

    public HashMap<Double, Double> getEValueHistogram(ArrayList<Double> hyperScores, boolean debug) throws IOException {
        HashMap<Integer, Integer> histogram = new HashMap<Integer, Integer>();
        Double maxScore = 0.0;
        Double minScore = Double.MAX_VALUE;
        int nValid = 0;
        for (Double score : hyperScores) {
            Integer intValue = score.intValue();
            if (intValue > 0) {
                nValid++;
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
        if (debug) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(valuesFile)));
            bw.write("X\tY");
            bw.newLine();
            for (Integer score : histogram.keySet()) {
                bw.write(score + "\t" + histogram.get(score));
                bw.newLine();
            }
            bw.close();
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
        if (debug) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(histogramFile)));
            bw.write("X\tY");
            bw.newLine();
            for (int i = 0; i < evalueFunctionX.size(); i++) {
                Double x = evalueFunctionX.get(i);
                Double y = evalueFunctionY.get(i);
                bw.write(x + "\t" + y);
                bw.newLine();
            }
            bw.close();
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
        nAbInCache++;
        bwFitting.write(regressionStatistics.a + "\t" + regressionStatistics.b);
        bwFitting.newLine();
        return getInterpolation(hyperScores, regressionStatistics.a, regressionStatistics.b);
    }

    public HashMap<Double, Double> getInterpolation(ArrayList<Double> hyperScores, double a, double b) {
        HashMap<Double, Double> result = new HashMap<Double, Double>();
        for (Double hyperScore : hyperScores) {
            if (hyperScore > 0 && !result.containsKey(hyperScore)) {
                Double logScore = FastMath.log10(hyperScore);
                Double eValue = b + a * logScore;
                result.put(hyperScore, eValue);
            }
        }
        return result;
    }

    public void close() throws IOException {
        bwFitting.close();
    }

    public double getMendianA() {
        return getMedianValue(as, nAbInCache);
    }

    public double getMendianB() {
        return getMedianValue(bs, nAbInCache);
    }

    public static Double getMedianValue(HashMap<Double, Integer> histogram, int nValues) {
        ArrayList<Double> values = new ArrayList<Double>(histogram.keySet());
        Collections.sort(values);
        int currentSum = 0;
        int previousSum = 0;
        Double previousValue = 0.0;
        double limit = ((double) nValues) / 2;
        for (Double value : values) {
            Integer currentOccurence = histogram.get(value);
            currentSum += currentOccurence;
            if (currentSum >= limit) {
                if (previousSum + 1 > limit && previousSum > 0) {
                    return (previousValue + value) / 2;
                }
                return value;
            }
        }
        throw new IllegalArgumentException("Reached the end of the histogram before reaching the median.");
    }
}
