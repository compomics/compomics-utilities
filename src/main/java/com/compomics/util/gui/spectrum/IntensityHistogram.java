package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 * Creates an IntensityHistogram plot.
 *
 * @author Harald Barsnes
 */
public class IntensityHistogram extends JPanel {

    /**
     * The chart panel for external access.
     */
    private ChartPanel chartPanel;

    /**
     * Creates an IntensityHistogram plot
     *
     * @param annotations the full list of spectrum annotations
     * @param currentSpectrum the current spectrum
     * @param intensityLevel annotation intensity level, e.g., 0.75 for 75%
     * 
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public IntensityHistogram(
            ArrayList<IonMatch> annotations,
            MSnSpectrum currentSpectrum,
            double intensityLevel) throws InterruptedException {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        // the non annotated intensities
        ArrayList<Double> nonAnnotatedPeakIntensities =
                currentSpectrum.getPeaksAboveIntensityThreshold(currentSpectrum.getIntensityLimit(intensityLevel));

        // the annotated intensities
        ArrayList<Double> annotatedPeakIntensities = new ArrayList<>();

        // get the list of annotated and not annotated intensities
        for (IonMatch ionMatch : annotations) {
            annotatedPeakIntensities.add(ionMatch.peak.intensity);
            nonAnnotatedPeakIntensities.remove(ionMatch.peak.intensity);
        }

        // create the peak histograms
        int bins = 30; // @TODO: make this a user selection! // @TODO use the Freedman-Diaconis rule

        // the non annotated peaks histogram
        double[] nonAnnotatedIntensities = new double[nonAnnotatedPeakIntensities.size()];

        // the annotated peaks histogram
        double[] annotatedIntensities = new double[annotatedPeakIntensities.size()];

        if (nonAnnotatedIntensities.length > 0) {

            for (int i = 0; i < nonAnnotatedPeakIntensities.size(); i++) {
                nonAnnotatedIntensities[i] = nonAnnotatedPeakIntensities.get(i);
            }

            for (int i = 0; i < annotatedPeakIntensities.size(); i++) {
                annotatedIntensities[i] = annotatedPeakIntensities.get(i);
            }

            HistogramDataset dataset = new HistogramDataset();
            dataset.setType(HistogramType.RELATIVE_FREQUENCY); // @TODO: use SCALE_AREA_TO_1 instead??
            dataset.addSeries("Not Annotated", nonAnnotatedIntensities, bins, 0, currentSpectrum.getMaxIntensity());
            dataset.addSeries("Annotated", annotatedIntensities, bins, 0, currentSpectrum.getMaxIntensity());

            JFreeChart chart = ChartFactory.createHistogram(null, null, null,
                    dataset, PlotOrientation.VERTICAL, false, true, false);

            chartPanel = new ChartPanel(chart);
            chartPanel.setBorder(null);
            chart.setBorderVisible(false);

            XYPlot plot = chart.getXYPlot();

            // set up the chart renderer
            XYBarRenderer renderer = new XYBarRenderer();
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
            renderer.setShadowVisible(false);
            renderer.setSeriesPaint(0, new Color(210, 210, 210, 150)); // @TODO: make this selectable by the user
            //renderer.setSeriesPaint(0, new Color(0, 0, 250, 150)); // @TODO: make this selectable by the user
            renderer.setSeriesPaint(1, new Color(110, 196, 97)); // @TODO: make this selectable by the user
            plot.setRenderer(renderer);

            //plot.getRangeAxis().setRange(0, plot.getRangeAxis().getUpperBound() / 3); // @TODO: make the "zoom" selectable by the user
            plot.getRangeAxis().setRange(0, plot.getRangeAxis().getUpperBound());

            // hide unwanted chart details
            plot.setOutlineVisible(false);

            plot.setBackgroundPaint(Color.WHITE);
            chartPanel.setBackground(Color.WHITE);
            chart.setBackgroundPaint(Color.WHITE);

            this.add(chartPanel);
        }
    }

    /**
     * Returns the chart panel.
     *
     * @return the chart panel
     */
    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
