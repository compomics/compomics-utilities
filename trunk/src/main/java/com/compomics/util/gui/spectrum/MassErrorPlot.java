package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.SpectrumAnnotator.SpectrumAnnotationMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
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
    private SpectrumAnnotationMap annotations;
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
     * Creates a new MassErrorPlot.
     *
     * @param annotations                   the full list of spectrum annotations
     * @param currentFragmentIons           the currently selected fragment ion types
     * @param currentSpectrum               the current spectrum
     * @param massTolerance                 the mass error tolerance
     * @param includeSinglyCharge           if singly charged fragment ions are to be included
     * @param includeDoublyCharge           if doubly charged fragment ions are to be included
     * @param includeMoreThanTwoCharges     if fragment ions with more than two charges are to be included
     */
    public MassErrorPlot(
            SpectrumAnnotationMap annotations,
            ArrayList<PeptideFragmentIon.PeptideFragmentIonType> currentFragmentIons,
            MSnSpectrum currentSpectrum,
            double massTolerance,
            boolean includeSinglyCharge,
            boolean includeDoublyCharge,
            boolean includeMoreThanTwoCharges) {
        super();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        currentlyUsedIonMatches = new ArrayList<IonMatch>();

        this.annotations = annotations;
        this.currentFragmentIons = currentFragmentIons;
        this.includeSinglyCharge = includeSinglyCharge;
        this.includeDoublyCharge = includeDoublyCharge;
        this.includeMoreThanTwoCharges = includeMoreThanTwoCharges;

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

            for (int i = 0; i < currentlyUsedIonMatches.size(); i++) {

                double[][] dataXY = new double[2][1];

                IonMatch ionMatch = (IonMatch) currentlyUsedIonMatches.get(i);

                dataXY[0][0] = ionMatch.peak.mz;
                dataXY[1][0] = ionMatch.getError();

                xyDataset.addSeries(i, dataXY);

                int alphaLevel = Double.valueOf((ionMatch.peak.intensity / totalIntensity) / (maxAnnotatedIntensity / totalIntensity) * 255).intValue();

                colors.add(new Color(255, 0, 0, alphaLevel)); // @TODO: make color selectable by the user?
            }

            JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, xyDataset, PlotOrientation.VERTICAL, false, false, false);

            // fine tune the chart properites
            XYPlot plot = chart.getXYPlot();

            DefaultXYItemRenderer renderer = new DefaultXYItemRenderer();

            // set the colors and shape for the datapoints
            for (int i = 0; i < colors.size(); i++) {

                if (useIntensityGrading) {
                    renderer.setSeriesPaint(i, colors.get(i));
                    renderer.setSeriesShape(i, renderer.getBaseShape());
                } else {
                    renderer.setSeriesPaint(i, Color.RED); // @TODO: make this selectable by the user?
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

            ChartPanel chartPanel = new ChartPanel(chart) {

                @Override
                public String getToolTipText(MouseEvent e) {
                    return "Mass Error Plot";
                }

                @Override
                public String getToolTipText() {
                    return "Mass Error Plot";
                }
            };

            chartPanel.setBackground(Color.WHITE);

            chartPanel.setDomainZoomable(false);
            chartPanel.setRangeZoomable(false);
            chartPanel.setPopupMenu(null);

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
}
