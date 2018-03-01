package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
 * @author Marc Vaudel
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
     * @param intensityThresholdType the type of intensity threshold
     * @param intensityThreshold the intensity threshold
     */
    public IntensityHistogram(
            Stream<IonMatch> annotations,
            Spectrum currentSpectrum,
            AnnotationParameters.IntensityThresholdType intensityThresholdType,
            double intensityThreshold) {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        // the annotated intensities
        double[] annotatedPeakIntensities = annotations.mapToDouble(ionMatch -> ionMatch.peak.intensity)
                .toArray();
        HashSet<Double> annotatedPeakIntensitiesSet = Arrays.stream(annotatedPeakIntensities)
                .boxed().collect(Collectors.toCollection(HashSet::new));

        // the non annotated intensities above threshold
        double[] nonAnnotatedPeakIntensities
                = currentSpectrum.getPeaksAboveIntensityThreshold(currentSpectrum.getIntensityLimit(intensityThresholdType, intensityThreshold))
                        .filter(intensity -> !annotatedPeakIntensitiesSet.contains(intensity))
                        .toArray();

        // create the peak histograms
        int bins = 30; // @TODO: make this a user selection! // @TODO use the Freedman-Diaconis rule

        if (nonAnnotatedPeakIntensities.length > 0) {

            HistogramDataset dataset = new HistogramDataset();
            dataset.setType(HistogramType.RELATIVE_FREQUENCY); // @TODO: use SCALE_AREA_TO_1 instead??
            dataset.addSeries("Not Annotated", nonAnnotatedPeakIntensities, bins, 0, currentSpectrum.getMaxIntensity());
            dataset.addSeries("Annotated", annotatedPeakIntensities, bins, 0, currentSpectrum.getMaxIntensity());

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
