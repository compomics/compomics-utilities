package com.compomics.util.gui.spectrum;

import com.compomics.util.XYZDataPoint;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.SpectrumAnnotator.SpectrumAnnotationMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

/**
 * Creates a MassErrorBubblePlot displaying the mz values vs the mass error
 * with the intensity as the size of the bubbles.
 *
 * @author Harald Barsnes
 */
public class MassErrorBubblePlot extends JPanel {

    /**
     * The default fragment ion marker color.
     */
    private static Color defaultMarkerColor = new Color(0, 0, 255, 25); // light blue
    /**
     * The default b ion marker color.
     */
    private static Color bFragmentIonColor = new Color(0, 0, 255, 25); // light  blue
    /**
     * The default y ion marker color.
     */
    private static Color yFragmentIonColor = new Color(0, 255, 0, 25); // light green
    /**
     * The default other marker color.
     */
    private static Color otherFragmentIonColor = new Color(255, 0, 0, 25); // light red
    /**
     * The default visible alpha level.
     */
    public static final float DEFAULT_VISIBLE_MARKER_ALPHA = 1.0f;
    /**
     * The default non-visible alpha level.
     */
    public static final float DEFAULT_NON_VISIBLE_MARKER_ALPHA = 0.0f;
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
     * @param massTolerance                 the mass error tolerance
     * @param includeSinglyCharge           if singly charged fragment ions are to be included
     * @param includeDoublyCharge           if doubly charged fragment ions are to be included
     * @param includeMoreThanTwoCharges     if fragment ions with more than two charges are to be included
     * @param fragmentIonLabels             if true, the fragment ion type is used as the data series key,
     *                                      otherwise the psm index is used
     * @param addMarkers                    if true interval markers for the fragment ions will be shown
     */
    public MassErrorBubblePlot(
            ArrayList<SpectrumAnnotationMap> annotations,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> currentFragmentIons,
            ArrayList<MSnSpectrum> currentSpectra,
            double massTolerance,
            boolean includeSinglyCharge,
            boolean includeDoublyCharge,
            boolean includeMoreThanTwoCharges,
            boolean fragmentIonLabels,
            boolean addMarkers) {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        currentlyUsedIonMatches = new ArrayList<IonMatch>();

        this.currentFragmentIons = currentFragmentIons;
        this.includeSinglyCharge = includeSinglyCharge;
        this.includeDoublyCharge = includeDoublyCharge;
        this.includeMoreThanTwoCharges = includeMoreThanTwoCharges;

        DefaultXYZDataset xyzDataset = new DefaultXYZDataset();

        HashMap<String, ArrayList<XYZDataPoint>> fragmentIonDataset =
                            new HashMap<String, ArrayList<XYZDataPoint>>();

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

                        if (data.get(ionMatch.getPeakAnnotation() + " (" + (j + 1) + ")") != null) {
                            data.get(ionMatch.getPeakAnnotation() + " (" + (j + 1) + ")").add(
                                    new XYZDataPoint(ionMatch.peak.mz, ionMatch.getError(), ionMatch.peak.intensity / totalIntensity));
                        } else {
                            ArrayList<XYZDataPoint> temp = new ArrayList<XYZDataPoint>();
                            temp.add(new XYZDataPoint(ionMatch.peak.mz, ionMatch.getError(), ionMatch.peak.intensity / totalIntensity));
                            data.put(ionMatch.getPeakAnnotation() + " (" + (j + 1) + ")", temp);
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

                        if (fragmentIonDataset.get(ionMatch.getPeakAnnotation()) != null) {
                            fragmentIonDataset.get(ionMatch.getPeakAnnotation()).add(
                                    new XYZDataPoint(ionMatch.peak.mz, ionMatch.getError(), ionMatch.peak.intensity / totalIntensity));
                        } else {
                            ArrayList<XYZDataPoint> temp = new ArrayList<XYZDataPoint>();
                            temp.add(new XYZDataPoint(ionMatch.peak.mz, ionMatch.getError(), ionMatch.peak.intensity / totalIntensity));
                            fragmentIonDataset.put(ionMatch.getPeakAnnotation(), temp);
                        }
                    }

                    xyzDataset.addSeries("" + (j + 1), dataXYZ);
                }
            }
        }

        JFreeChart chart = ChartFactory.createBubbleChart(null, "m/z", "Mass Error (Da)", xyzDataset, PlotOrientation.VERTICAL, !fragmentIonLabels, true, false);

        if (chart.getLegend() != null) {
            chart.getLegend().setPosition(RectangleEdge.RIGHT);
        }

        // add fragment ion bar highlighters
        if (addMarkers) {
            addFragmentIonTypeMarkers(fragmentIonDataset, chart, true);
        }

        // fine tune the chart properites
        XYPlot plot = chart.getXYPlot();

        // set the data series colors if fragment ion label type is currently used
        if (fragmentIonLabels) {
            for (int i = 0; i < xyzDataset.getSeriesCount(); i++) {
                plot.getRenderer().setSeriesPaint(i, SpectrumPanel.determineFragmentIonColor((String) xyzDataset.getSeriesKey(i)));
            }
        }

        // set the mass error range
        plot.getRangeAxis().setLowerBound(-massTolerance);
        plot.getRangeAxis().setUpperBound(massTolerance);

        plot.getDomainAxis().setLowerBound(0);
        plot.getDomainAxis().setUpperBound(plot.getDomainAxis().getUpperBound() + 100);

        // remove space before/after the domain axis
        plot.getDomainAxis().setUpperMargin(0);
        plot.getDomainAxis().setLowerMargin(0);

        plot.setRangeGridlinePaint(Color.black);

        // make semi see through
        plot.setForegroundAlpha(0.5f);

        // set background color
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);

        this.add(chartPanel);
    }

    /**
     * Adds interval markers for all the fragment ion types.
     *
     * @param data          the data to get the interval markers from
     * @param chart         the chart to add the markers to
     * @param showMarkers   if true interval markers for the fragment ions will be added
     */
    public static void addFragmentIonTypeMarkers(HashMap<String, ArrayList<XYZDataPoint>> data, JFreeChart chart, boolean showMarkers) {

        int horizontalFontPadding = 13;

        Iterator<String> iterator = data.keySet().iterator();

        // iterate the data and add one interval marker for each fragment ion type
        while (iterator.hasNext()) {

            String fragmentIonType = iterator.next();

            // get the mz value of the current fragment ion type
            ArrayList<XYZDataPoint> dataPoints = data.get(fragmentIonType);
            XYZDataPoint currentDataPoint = dataPoints.get(0);
            double currentXValue = currentDataPoint.getX();

            // create the interval marker
            IntervalMarker intervalMarker = new IntervalMarker(currentXValue - 5, currentXValue + 5, defaultMarkerColor);
            intervalMarker.setLabel(fragmentIonType);
            intervalMarker.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
            intervalMarker.setLabelPaint(Color.GRAY);
            intervalMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);

            // set the fragment ion marker color
            if (fragmentIonType.startsWith("b")) {
                intervalMarker.setPaint(bFragmentIonColor);
            } else if (fragmentIonType.startsWith("y")) {
                intervalMarker.setPaint(yFragmentIonColor);
            } else {
                intervalMarker.setPaint(otherFragmentIonColor);
            }

            // make the marker visible or not
            if (showMarkers) {
                intervalMarker.setAlpha(DEFAULT_VISIBLE_MARKER_ALPHA);
            } else {
                intervalMarker.setAlpha(DEFAULT_NON_VISIBLE_MARKER_ALPHA);
            }

            // set the horizontal location of the markers label
            // this is need so that not all labels appear on top of each other
            if (fragmentIonType.startsWith("y")) {
                intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding, 0, horizontalFontPadding, 0));
            }

            if (fragmentIonType.lastIndexOf("H2O") != -1) {
                intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 2, 0, horizontalFontPadding * 2, 0));
            }

            if (fragmentIonType.lastIndexOf("NH3") != -1) {
                intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 3, 0, horizontalFontPadding * 3, 0));
            }

            if (fragmentIonType.lastIndexOf("Prec") != -1) {
                intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 4, 0, horizontalFontPadding * 4, 0));

                if (fragmentIonType.lastIndexOf("H2O") != -1) {
                    intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 5, 0, horizontalFontPadding * 5, 0));
                }

                if (fragmentIonType.lastIndexOf("NH3") != -1) {
                    intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 6, 0, horizontalFontPadding * 6, 0));
                }
            }

            if (fragmentIonType.startsWith("i")) {
                intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 5, 0, horizontalFontPadding * 5, 0));
            }

            if (fragmentIonType.lastIndexOf("++") != -1) {
                intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 7, 0, horizontalFontPadding * 7, 0));

                if (fragmentIonType.lastIndexOf("H2O") != -1) {
                    intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 8, 0, horizontalFontPadding * 8, 0));
                }

                if (fragmentIonType.lastIndexOf("NH3") != -1) {
                    intervalMarker.setLabelOffset(new RectangleInsets(horizontalFontPadding * 9, 0, horizontalFontPadding * 9, 0));
                }
            }

            // add the interval marker to the plot
            ((XYPlot) chart.getPlot()).addDomainMarker(intervalMarker, Layer.BACKGROUND);
        }
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
