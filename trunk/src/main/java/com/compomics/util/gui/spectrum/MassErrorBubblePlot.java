package com.compomics.util.gui.spectrum;

import com.compomics.util.XYZDataPoint;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.SpectrumAnnotator.SpectrumAnnotationMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYZDataset;

/**
 * Creates a MassErrorBubblePlot displaying the mz values vs the mass error
 * with the intensity as the size of the bubbles.
 *
 * @author Harald Barsnes
 */
public class MassErrorBubblePlot extends JPanel {

    /**
     * The currently selected fragment ion types.
     */
    private ArrayList<PeptideFragmentIon.PeptideFragmentIonType> currentFragmentIons;
    /**
     * If singly charged fragment ions are to be included.
     */
    private boolean includeSinglyCharge;
    /**
     * If doubly charged fragment ions are to be included.
     */
    private boolean includeDoublyCharge;
    /**
     * If fragment ions with more than two charges are to be included.
     */
    private boolean includeMoreThanTwoCharges;
    /**
     * The list of currently used ions.
     */
    private ArrayList<IonMatch> currentlyUsedIonMatches;

    /**
     * Creates a new MassErrorBubblePlot.
     *
     * @param annotations                   the full list of spectrum annotations
     * @param currentFragmentIons           the currently selected fragment ion types
     * @param currentSpectra                the current spectra
     * @param includeSinglyCharge           if singly charged fragment ions are to be included
     * @param includeDoublyCharge           if doubly charged fragment ions are to be included
     * @param includeMoreThanTwoCharges     if fragment ions with more than two charges are to be included
     * @param fragmentIonLabels             if true, the fragment ion type is used as the data series key,
     *                                      otherwise the psm index is used
     */
    public MassErrorBubblePlot(
            ArrayList<SpectrumAnnotationMap> annotations,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> currentFragmentIons,
            ArrayList<MSnSpectrum> currentSpectra,
            boolean includeSinglyCharge,
            boolean includeDoublyCharge,
            boolean includeMoreThanTwoCharges,
            boolean fragmentIonLabels) {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        currentlyUsedIonMatches = new ArrayList<IonMatch>();

        this.currentFragmentIons = currentFragmentIons;
        this.includeSinglyCharge = includeSinglyCharge;
        this.includeDoublyCharge = includeDoublyCharge;
        this.includeMoreThanTwoCharges = includeMoreThanTwoCharges;

        DefaultXYZDataset xyzDataset = new DefaultXYZDataset();

        for (int j = 0; j < annotations.size(); j++) {

            SpectrumAnnotationMap currentAnnotations = annotations.get(j);
            MSnSpectrum currentSpectrum = currentSpectra.get(j);

            // the annotated ion matches
            currentlyUsedIonMatches = getCurrentlyUsedIonMatches(currentAnnotations);

            if (currentlyUsedIonMatches.size() > 0) {

                // find the most intense annotated peak
                double maxAnnotatedIntensity = 0.0;

                for (int i = 0; i < currentlyUsedIonMatches.size(); i++) {

                    IonMatch ionMatch = (IonMatch) currentlyUsedIonMatches.get(i);

                    if (ionMatch.peak.intensity > maxAnnotatedIntensity) {
                        maxAnnotatedIntensity = ionMatch.peak.intensity;
                    }
                }

                double totalIntensity = currentSpectrum.getTotalIntensity();

                if (fragmentIonLabels) {

                    HashMap<String, ArrayList<XYZDataPoint>> data =
                            new HashMap<String, ArrayList<XYZDataPoint>>();

                    for (int i = 0; i < currentlyUsedIonMatches.size(); i++) {

                        IonMatch ionMatch = (IonMatch) currentlyUsedIonMatches.get(i);

                        if (data.get(ionMatch.getPeakAnnotation()) != null) {
                            data.get(ionMatch.getPeakAnnotation()).add(
                                    new XYZDataPoint(ionMatch.peak.mz, ionMatch.getError(), ionMatch.peak.intensity / totalIntensity));
                        } else {
                            ArrayList<XYZDataPoint> temp = new ArrayList<XYZDataPoint>();
                            temp.add(new XYZDataPoint(ionMatch.peak.mz, ionMatch.getError(), ionMatch.peak.intensity / totalIntensity));
                            data.put(ionMatch.getPeakAnnotation(), temp);
                        }
                    }

                    xyzDataset = addXYZDataSeries(data);

                } else {

                    double[][] dataXYZ = new double[3][currentlyUsedIonMatches.size()];

                    for (int i = 0; i < currentlyUsedIonMatches.size(); i++) {

                        IonMatch ionMatch = (IonMatch) currentlyUsedIonMatches.get(i);

                        dataXYZ[0][i] = ionMatch.peak.mz;
                        dataXYZ[1][i] = ionMatch.getError();
                        dataXYZ[2][i] = ionMatch.peak.intensity / totalIntensity;
                    }

                    xyzDataset.addSeries("" + (j + 1), dataXYZ);
                }
            }
        }

        JFreeChart chart = ChartFactory.createBubbleChart(null, "m/z", "Mass Error (Da)", xyzDataset, PlotOrientation.VERTICAL, true, true, false);

        // fine tune the chart properites
        XYPlot plot = chart.getXYPlot();

        // set the data series colors if fragment ion label type is currently used
        if (fragmentIonLabels) {
            for (int i = 0; i < xyzDataset.getSeriesCount(); i++) {
                plot.getRenderer().setSeriesPaint(i, SpectrumPanel.determineFragmentIonColor((String) xyzDataset.getSeriesKey(i)));
            }
        }

        // remove space before/after the domain axis
        plot.getDomainAxis().setUpperMargin(0);
        plot.getDomainAxis().setLowerMargin(0);

        plot.setRangeGridlinePaint(Color.black);

        // set background color
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);

        this.add(chartPanel);
    }

    /**
     * Returns the currently selected ions.
     *
     * @return the currently selected ions
     */
    private ArrayList<IonMatch> getCurrentlyUsedIonMatches(SpectrumAnnotationMap annotations) {

        currentlyUsedIonMatches = new ArrayList<IonMatch>();

        Iterator<String> ionTypeIterator = annotations.getAnnotations().keySet().iterator();

        // iterate the annotations and store the needed data
        while (ionTypeIterator.hasNext()) {
            String ionType = ionTypeIterator.next();

            HashMap<Integer, IonMatch> chargeMap = annotations.getAnnotations().get(ionType);
            Iterator<Integer> chargeIterator = chargeMap.keySet().iterator();

            while (chargeIterator.hasNext()) {
                Integer currentCharge = chargeIterator.next();
                IonMatch ionMatch = chargeMap.get(currentCharge);

                PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

                // set up the data for the mass error and instensity histograms
                if (currentFragmentIons.contains(fragmentIon.getType())) {

                    if ((currentCharge == 1 && includeSinglyCharge)
                            || (currentCharge == 2 && includeDoublyCharge)
                            || (currentCharge > 2 && includeMoreThanTwoCharges)) {
                        currentlyUsedIonMatches.add(ionMatch);
                    }
                }
            }
        }

        return currentlyUsedIonMatches;
    }

    /**
     * Returns the current number of data points in the mass error plot.
     *
     * @return the current number of data points
     */
    public int getNumberOfDataPointsInPlot() {
        return currentlyUsedIonMatches.size();
    }

    /**
     * Adds the provided data series to an XYZ data set.
     *
     * @param data the data to add
     * @return the created data set
     */
    public static DefaultXYZDataset addXYZDataSeries(HashMap<String, ArrayList<XYZDataPoint>> data) {

        // sort the keys
        ArrayList<String> sortedKeys = new ArrayList<String>();

        Iterator<String> iterator = data.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            sortedKeys.add(key);
        }

        java.util.Collections.sort(sortedKeys);

        DefaultXYZDataset dataset = new DefaultXYZDataset();

        HashMap<Double, ArrayList<Double>> xAndYValues = new HashMap<Double, ArrayList<Double>>();

        for (int j = 0; j < sortedKeys.size(); j++) {

            String key = sortedKeys.get(j);

            ArrayList<XYZDataPoint> currentData = data.get(key);

            double[][] tempXYZData = new double[3][currentData.size()];

            for (int i = 0; i < currentData.size(); i++) {
                tempXYZData[0][i] = currentData.get(i).getX();
                tempXYZData[1][i] = currentData.get(i).getY();
                tempXYZData[2][i] = currentData.get(i).getZ();
            }

            dataset.addSeries(key, tempXYZData);
        }

        return dataset;
    }
}
