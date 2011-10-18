package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
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
     * @param annotations                   the full list of spectrum annotations
     * @param currentFragmentIons           the currently selected fragment ion types
     * @param currentSpectrum               the current spectrum
     * @param annotateMostIntensePeaks      if only the most intense peaks are to be included
     * @param intensityLevel                annotation intensity level in percent, e.g., 0.75
     * @param includeSinglyCharge           if singly charged fragment ions are to be included
     * @param includeDoublyCharge           if doubly charged fragment ions are to be included
     * @param includeMoreThanTwoCharges     if fragment ions with more than two charges are to be included
     */
    public IntensityHistogram(
            ArrayList<IonMatch> annotations,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> currentFragmentIons,
            MSnSpectrum currentSpectrum,
            double intensityLevel,
            boolean includeSinglyCharge,
            boolean includeDoublyCharge,
            boolean includeMoreThanTwoCharges) {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        // the non annotated intensities
        ArrayList<Double> nonAnnotatedPeakIntensities =
                currentSpectrum.getPeaksAboveIntensityThreshold(currentSpectrum.getIntensityLimit(intensityLevel));

        // the annotated intensities
        ArrayList<Double> annotatedPeakIntensities = new ArrayList<Double>();

        int currentCharge;
        for (IonMatch ionMatch : annotations) {
            currentCharge = ionMatch.charge.value;

            PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

            // set up the data for the mass error and instensity histograms
            if (currentFragmentIons.contains(fragmentIon.getType())) {

                if ((currentCharge == 1 && includeSinglyCharge)
                        || (currentCharge == 2 && includeDoublyCharge)
                        || (currentCharge > 2 && includeMoreThanTwoCharges)) {
                    annotatedPeakIntensities.add(ionMatch.peak.intensity);
                    nonAnnotatedPeakIntensities.remove(ionMatch.peak.intensity);
                }
            }
        }

        // create the peak histograms
        int bins = 30; // @TODO: make this a user selection!

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
