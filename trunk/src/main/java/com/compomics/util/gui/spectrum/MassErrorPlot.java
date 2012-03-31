package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.awt.Color;
import java.util.ArrayList;
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
 */
public class MassErrorPlot extends JPanel {

    /**
     * The complete list of possible spectrum annotations.
     */
    private ArrayList<IonMatch> annotations;
    /**
     * The currently selected fragment ion types.
     */
    private ArrayList<Integer> currentFragmentIons;
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
     * The chart panel.
     */
    private ChartPanel chartPanel;
    /**
     * If true the relative error (ppm) is used instead of the absolute error
     * (Da).
     */
    private boolean useRelativeError = false;

    /**
     * Creates a new MassErrorPlot.
     *
     * @param annotations the full list of spectrum annotations
     * @param currentFragmentIons the currently selected fragment ion types
     * @param currentSpectrum the current spectrum
     * @param massTolerance the mass error tolerance
     * @param includeSinglyCharge if singly charged fragment ions are to be
     * included
     * @param includeDoublyCharge if doubly charged fragment ions are to be
     * included
     * @param includeMoreThanTwoCharges if fragment ions with more than two
     * charges are to be included
     */
    public MassErrorPlot(
            ArrayList<IonMatch> annotations,
            ArrayList<Integer> currentFragmentIons,
            MSnSpectrum currentSpectrum,
            double massTolerance,
            boolean includeSinglyCharge,
            boolean includeDoublyCharge,
            boolean includeMoreThanTwoCharges) {
        this(annotations, currentFragmentIons, currentSpectrum, massTolerance, includeSinglyCharge, includeDoublyCharge, includeMoreThanTwoCharges, false);
    }

    /**
     * Creates a new MassErrorPlot.
     *
     * //@TODO improve charge compatibility
     * @param annotations the full list of spectrum annotations
     * @param currentFragmentIons the currently selected fragment ion types
     * @param currentSpectrum the current spectrum
     * @param massTolerance the mass error tolerance
     * @param includeSinglyCharge if singly charged fragment ions are to be
     * included
     * @param includeDoublyCharge if doubly charged fragment ions are to be
     * included
     * @param includeMoreThanTwoCharges if fragment ions with more than two
     * charges are to be included
     * @param useRelativeError if true the relative error (ppm) is used instead
     * of the absolute error (Da)
     */
    public MassErrorPlot(
            ArrayList<IonMatch> annotations,
            ArrayList<Integer> currentFragmentIons,
            MSnSpectrum currentSpectrum,
            double massTolerance,
            boolean includeSinglyCharge,
            boolean includeDoublyCharge,
            boolean includeMoreThanTwoCharges,
            boolean useRelativeError) {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        currentlyUsedIonMatches = new ArrayList<IonMatch>();

        this.annotations = annotations;
        this.currentFragmentIons = currentFragmentIons;
        this.includeSinglyCharge = includeSinglyCharge;
        this.includeDoublyCharge = includeDoublyCharge;
        this.includeMoreThanTwoCharges = includeMoreThanTwoCharges;
        this.useRelativeError = useRelativeError;

        // the annotated ion matches
        currentlyUsedIonMatches = getCurrentlyUsedIonMatches();

        if (currentlyUsedIonMatches.size() > 0) {

            boolean useIntensityGrading = false;  // @TODO: make this selectable by the user?

            DefaultXYDataset xyDataset = new DefaultXYDataset();

            ArrayList<Color> colors = new ArrayList<Color>();

            // find the most intense annotated peak
            double maxAnnotatedIntensity = 0.0;

            for (int i = 0; i < currentlyUsedIonMatches.size(); i++) {

                IonMatch ionMatch = (IonMatch) currentlyUsedIonMatches.get(i);
                if (ionMatch.peak.intensity > maxAnnotatedIntensity) {
                    maxAnnotatedIntensity = ionMatch.peak.intensity;
                }
            }

            double totalIntensity = currentSpectrum.getTotalIntensity();
            double maxError = 0.0;

            for (int i = 0; i < currentlyUsedIonMatches.size(); i++) {

                double[][] dataXY = new double[2][1];

                IonMatch ionMatch = (IonMatch) currentlyUsedIonMatches.get(i);

                dataXY[0][0] = ionMatch.peak.mz;

                if (useRelativeError) {
                    dataXY[1][0] = ionMatch.getRelativeError();
                } else {
                    dataXY[1][0] = ionMatch.getAbsoluteError();
                }

                if (Math.abs(dataXY[1][0]) > maxError) {
                    maxError = Math.abs(dataXY[1][0]);
                }

                xyDataset.addSeries(ionMatch.getPeakAnnotation(true), dataXY);

                // use the two lines below is all points ought to have the same color
                //int alphaLevel = Double.valueOf((ionMatch.peak.intensity / totalIntensity) / (maxAnnotatedIntensity / totalIntensity) * 255).intValue();
                //colors.add(new Color(255, 0, 0, alphaLevel)); // @TODO: make color selectable by the user?

                // use the same colors as for the SpectrumPanel annotation
                colors.add(SpectrumPanel.determineFragmentIonColor(ionMatch.ion, false));
            }

            JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, xyDataset, PlotOrientation.VERTICAL, false, false, false);

            // fine tune the chart properites
            XYPlot plot = chart.getXYPlot();

            DefaultXYItemRenderer renderer = new DefaultXYItemRenderer();
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

            // set the colors and shape for the datapoints
            for (int i = 0; i < colors.size(); i++) {

                if (useIntensityGrading) {
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
            if (useRelativeError) {
                plot.getRangeAxis().setLowerBound(-maxError * 1.1);
                plot.getRangeAxis().setUpperBound(maxError * 1.1);
            } else {
                plot.getRangeAxis().setLowerBound(-massTolerance);
                plot.getRangeAxis().setUpperBound(massTolerance);
            }

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
     * Returns the currently selected ions.
     *
     * @return the currently selected ions
     */
    private ArrayList<IonMatch> getCurrentlyUsedIonMatches() {

        currentlyUsedIonMatches = new ArrayList<IonMatch>();

        for (IonMatch ionMatch : annotations) {
            if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ionMatch.ion);

                // set up the data for the mass error and instensity histograms
                if (currentFragmentIons.contains(fragmentIon.getSubType())) {
                    int currentCharge = ionMatch.charge.value;
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
     * Returns the chart panel.
     *
     * @return the chart panel
     */
    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
