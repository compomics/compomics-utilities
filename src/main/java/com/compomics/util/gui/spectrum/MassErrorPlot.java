package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

/**
 * Creates a MassErrorPlot displaying the mz values vs the mass error.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class MassErrorPlot extends JPanel {

    /**
     * The complete list of possible spectrum annotations.
     */
    private IonMatch[] annotations;
    /**
     * The chart panel.
     */
    private ChartPanel chartPanel;

    /**
     * Creates a new MassErrorPlot.
     *
     * @param annotations the full list of spectrum annotations
     * @param currentSpectrum the current spectrum
     * @param massTolerance the mass error tolerance
     *
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     */
    public MassErrorPlot(
            IonMatch[] annotations,
            Spectrum currentSpectrum,
            double massTolerance) throws InterruptedException {
        this(annotations, currentSpectrum, massTolerance, false);
    }

    /**
     * Creates a new MassErrorPlot.
     *
     * @param annotations the full list of spectrum annotations
     * @param currentSpectrum the current spectrum
     * @param massTolerance the mass error tolerance
     * @param useRelativeError if true the relative error (ppm) is used instead
     * of the absolute error (Da)
     */
    public MassErrorPlot(
            IonMatch[] annotations,
            Spectrum currentSpectrum,
            double massTolerance,
            boolean useRelativeError) {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        this.annotations = annotations;

        if (annotations.length > 0) {

            boolean useIntensityGrading = false;  // @TODO: make this selectable by the user?
            DefaultXYDataset xyDataset = new DefaultXYDataset();
            ArrayList<Color> colors = new ArrayList<>();

            // find the most intense annotated peak
            double maxAnnotatedIntensity = Arrays.stream(annotations)
                    .mapToDouble(ionMatch -> ionMatch.peak.intensity)
                    .max()
                    .orElse(0.0);

            double maxError = useRelativeError ? Arrays.stream(annotations)
                            .mapToDouble(ionMatch -> ionMatch.getRelativeError())
                            .max()
                            .orElse(0.0)
                    : Arrays.stream(annotations)
                            .mapToDouble(ionMatch -> ionMatch.getAbsoluteError())
                            .max()
                            .orElse(0.0);

            Arrays.stream(annotations)
                    .forEach(ionMatch -> {
                double[][] dataXY = new double[2][1];
                dataXY[0][0] = ionMatch.peak.mz;

                dataXY[1][0] = useRelativeError ? ionMatch.getRelativeError() : ionMatch.getAbsoluteError();

                xyDataset.addSeries(ionMatch.getPeakAnnotation(true), dataXY);

                // use the two lines below if all points ought to have the same color
                //int alphaLevel = Double.valueOf((ionMatch.peak.intensity / totalIntensity) / (maxAnnotatedIntensity / totalIntensity) * 255).intValue();
                //colors.add(new Color(255, 0, 0, alphaLevel)); // @TODO: make color selectable by the user?
                // use the same colors as for the SpectrumPanel annotation
                colors.add(SpectrumPanel.determineFragmentIonColor(ionMatch.ion, false));
            });

            JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, xyDataset, PlotOrientation.VERTICAL, false, false, false);

            // fine tune the chart properites
            XYPlot plot = chart.getXYPlot();

            DefaultXYItemRenderer renderer = new DefaultXYItemRenderer();
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

            // set the colors and shape for the datapoints
            for (int i = 0; i < colors.size(); i++) {
                if (useIntensityGrading) { // @TODO: implement itensity color grading?
                    renderer.setSeriesPaint(i, colors.get(i));
                    renderer.setSeriesShape(i, renderer.getBaseShape());
                } else {
                    renderer.setSeriesPaint(i, colors.get(i));
                    renderer.setSeriesShape(i, renderer.getBaseShape());
                }
            }

            plot.setRenderer(renderer);

            // remove space before/after the domain axis
            plot.getDomainAxis().setUpperMargin(0);
            plot.getDomainAxis().setLowerMargin(0);

            // set the mass error range
            plot.getRangeAxis().setLowerBound(-massTolerance);
            plot.getRangeAxis().setUpperBound(massTolerance);

            plot.setRangeGridlinePaint(Color.black);

            ValueAxis domainAxis = plot.getDomainAxis();
            domainAxis.setRange(0, currentSpectrum.getMaxMz());

            // hide unwanted chart details
            plot.setDomainGridlinesVisible(false);
            chart.getPlot().setOutlineVisible(false);

            // set background color
            chart.getPlot().setBackgroundPaint(Color.WHITE);
            chart.setBackgroundPaint(Color.WHITE);

            chartPanel = new ChartPanel(chart);
            chartPanel.setBackground(Color.WHITE);
            this.add(chartPanel);

        }
    }

    /**
     * Returns the current number of data points in the mass error plot.
     *
     * @return the current number of data points
     */
    public int getNumberOfDataPointsInPlot() {
        return annotations.length;
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
