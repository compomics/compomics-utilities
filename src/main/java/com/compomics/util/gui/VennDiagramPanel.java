package com.compomics.util.gui;

import com.compomics.util.math.VennDiagram;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import no.uib.jsparklines.data.XYDataPoint;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.TextAnchor;

/**
 * A panel displaying a Venn diagram based on the provided data.
 *
 * @author Harald Barsnes.
 */
public class VennDiagramPanel extends javax.swing.JPanel {

    /**
     * The supported Venn diagram types.
     */
    public enum VennDiagramType {

        ONE_WAY, TWO_WAY, THREE_WAY, FOUR_WAY
    }
    /**
     * Map each dataset tooltip back to the given dataset.
     */
    private HashMap<String, String> tooltipToDatasetMap;
    /**
     * The current Venn diagram type.
     */
    private VennDiagramType currentVennDiagramType = VennDiagramType.THREE_WAY;
    /**
     * The current Venn diagram results/data.
     */
    private final HashMap<String, ArrayList<String>> vennDiagramResults;
    /**
     * The current Venn diagram group names.
     */
    private final HashMap<String, String> groupNames;
    /**
     * The chart panel.
     */
    private ChartPanel chartPanel;
    /**
     * The font size to use for the values.
     */
    private int fontSizeValues = 17;
    /**
     * The font size to use for the legend.
     */
    private int fontSizeLegend = 14;
    /**
     * If true, the legend is shown.
     */
    private boolean showLegend = true;
    /**
     * The legend location of Dataset A in a one to three way Venn diagram.
     */
    private XYDataPoint legendDatasetAThreeWay = new XYDataPoint(0.86, 0.86);
    /**
     * The legend location of Dataset B in a one to three way Venn diagram.
     */
    private XYDataPoint legendDatasetBThreeWay = new XYDataPoint(1.15, 0.86);
    /**
     * The legend location of Dataset C in a one to three way Venn diagram.
     */
    private XYDataPoint legendDatasetCThreeWay = new XYDataPoint(1.0, 1.22);
    /**
     * The legend location of Dataset A in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetAFourWay = new XYDataPoint(0.13, 0.53);
    /**
     * The legend location of Dataset B in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetBFourWay = new XYDataPoint(0.23, 0.43);
    /**
     * The legend location of Dataset C in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetCFourWay = new XYDataPoint(0.33, 0.33);
    /**
     * The legend location of Dataset D in a four-way Venn diagram.
     */
    private XYDataPoint legendDatasetDFourWay = new XYDataPoint(0.43, 0.23);
    /**
     * The color for dataset A.
     */
    private final Color datasetAColor;
    /**
     * The color for dataset B.
     */
    private final Color datasetBColor;
    /**
     * The color for dataset C.
     */
    private final Color datasetCColor;
    /**
     * The color for dataset D.
     */
    private final Color datasetDColor;

    /**
     * Creates a new VennDiagramPanel.
     *
     * @param a dataset A
     * @param b dataset B
     * @param c dataset C
     * @param d dataset D
     * @param groupA the name for dataset A
     * @param groupB the name for dataset B
     * @param groupC the name for dataset C
     * @param groupD the name for dataset D
     * @param datasetAColor the color for dataset A
     * @param datasetBColor the color for dataset B
     * @param datasetCColor the color for dataset C
     * @param datasetDColor the color for dataset D
     */
    public VennDiagramPanel(ArrayList<String> a, ArrayList<String> b, ArrayList<String> c, ArrayList<String> d,
            String groupA, String groupB, String groupC, String groupD,
            Color datasetAColor, Color datasetBColor, Color datasetCColor, Color datasetDColor) {
        initComponents();

        vennDiagramResults = VennDiagram.vennDiagramMaker(a, b, c, d);

        if (b.isEmpty() && c.isEmpty() && d.isEmpty()) {
            currentVennDiagramType = VennDiagramType.ONE_WAY;
        } else if (c.isEmpty() && d.isEmpty()) {
            currentVennDiagramType = VennDiagramType.TWO_WAY;
        } else if (d.isEmpty()) {
            currentVennDiagramType = VennDiagramType.THREE_WAY;
        } else {
            currentVennDiagramType = VennDiagramType.FOUR_WAY;
        }

        groupNames = new HashMap<>();

        groupNames.put("a", groupA);
        groupNames.put("b", groupB);
        groupNames.put("c", groupC);
        groupNames.put("d", groupD);

        this.datasetAColor = datasetAColor;
        this.datasetBColor = datasetBColor;
        this.datasetCColor = datasetCColor;
        this.datasetDColor = datasetDColor;

        updatePlot();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        plotPanel = new javax.swing.JPanel();

        setOpaque(false);

        plotPanel.setOpaque(false);
        plotPanel.setLayout(new javax.swing.BoxLayout(plotPanel, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Update the plot.
     */
    public void updatePlot() {

        plotPanel.removeAll();
        tooltipToDatasetMap = new HashMap<>();

        DefaultXYZDataset xyzDataset = new DefaultXYZDataset();

        JFreeChart chart = ChartFactory.createBubbleChart(null, "X", "Y", xyzDataset, PlotOrientation.VERTICAL, false, true, false);
        XYPlot plot = chart.getXYPlot();

        if (currentVennDiagramType == VennDiagramType.ONE_WAY) {
            plot.getRangeAxis().setRange(0.86, 1.24);
            plot.getDomainAxis().setRange(0.85, 1.25);
        } else if (currentVennDiagramType == VennDiagramType.TWO_WAY) {
            plot.getRangeAxis().setRange(0.86, 1.24);
            plot.getDomainAxis().setRange(0.85, 1.25);
        } else if (currentVennDiagramType == VennDiagramType.THREE_WAY) {
            plot.getRangeAxis().setRange(0.86, 1.24);
            plot.getDomainAxis().setRange(0.85, 1.25);
        } else {
            plot.getRangeAxis().setRange(-0.04, 0.6);
            plot.getDomainAxis().setRange(-0.08, 0.7);
        }

        plot.getRangeAxis().setVisible(false);
        plot.getDomainAxis().setVisible(false);

        double radius = 0.1;
        Ellipse2D ellipse = new Ellipse2D.Double(1 - radius, 1 - radius, radius + radius, radius + radius);
        XYShapeAnnotation xyShapeAnnotation = new XYShapeAnnotation(ellipse, new BasicStroke(2f), new Color(140, 140, 140, 150), datasetAColor); // @TODO: make it possible set the line color and width?
        plot.addAnnotation(xyShapeAnnotation);

        if (currentVennDiagramType == VennDiagramType.TWO_WAY || currentVennDiagramType == VennDiagramType.THREE_WAY) {
            ellipse = new Ellipse2D.Double(1 - radius + 0.1, 1 - radius, radius + radius, radius + radius);
            xyShapeAnnotation = new XYShapeAnnotation(ellipse, new BasicStroke(2f), new Color(140, 140, 140, 150), datasetBColor);
            plot.addAnnotation(xyShapeAnnotation);
        }

        if (currentVennDiagramType == VennDiagramType.THREE_WAY) {
            ellipse = new Ellipse2D.Double(1 - radius + 0.05, 1 - radius + 0.1, radius + radius, radius + radius);
            xyShapeAnnotation = new XYShapeAnnotation(ellipse, new BasicStroke(2f), new Color(140, 140, 140, 150), datasetCColor);
            plot.addAnnotation(xyShapeAnnotation);
        }

        XYTextAnnotation anotation;

        if (currentVennDiagramType == VennDiagramType.ONE_WAY) {

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 1.0, 1.0);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAThreeWay.getX(), legendDatasetAThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }

        } else if (currentVennDiagramType == VennDiagramType.TWO_WAY) {

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 0.96, 1.0);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("b").size(), 1.14, 1.0);
            anotation.setToolTipText(groupNames.get("b"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "b");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ab").size(), 1.05, 1.0);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ab");

            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAThreeWay.getX(), legendDatasetAThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("b"), legendDatasetBThreeWay.getX(), legendDatasetBThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }

        } else if (currentVennDiagramType == VennDiagramType.THREE_WAY) {

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 0.96, 0.97);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("b").size(), 1.14, 0.97);
            anotation.setToolTipText(groupNames.get("b"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "b");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ab").size(), 1.05, 0.97);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ab");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("c").size(), 1.05, 1.14);
            anotation.setToolTipText(groupNames.get("c"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "c");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ac").size(), 0.99, 1.065);
            anotation.setToolTipText("<html>" + groupNames.get("a") + "  &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ac");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bc").size(), 1.11, 1.065);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bc");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abc").size(), 1.05, 1.036);
            anotation.setToolTipText("<html>" + groupNames.get("a") + "  &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abc");

            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAThreeWay.getX(), legendDatasetAThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("b"), legendDatasetBThreeWay.getX(), legendDatasetBThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("c"), legendDatasetCThreeWay.getX(), legendDatasetCThreeWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }

        } else if (currentVennDiagramType == VennDiagramType.FOUR_WAY) {

            XYBoxAnnotation anotation2 = new XYBoxAnnotation(0, 0, 0.2, 0.5, new BasicStroke(2), Color.LIGHT_GRAY, datasetAColor);
            plot.addAnnotation(anotation2);

            anotation2 = new XYBoxAnnotation(0.1, 0, 0.3, 0.4, new BasicStroke(2), Color.LIGHT_GRAY, datasetBColor);
            plot.addAnnotation(anotation2);

            anotation2 = new XYBoxAnnotation(0, 0.1, 0.4, 0.3, new BasicStroke(2), Color.LIGHT_GRAY, datasetCColor);
            plot.addAnnotation(anotation2);

            anotation2 = new XYBoxAnnotation(0, 0, 0.5, 0.2, new BasicStroke(2), Color.LIGHT_GRAY, datasetDColor);
            plot.addAnnotation(anotation2);

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("a").size(), 0.15, 0.45);
            anotation.setToolTipText(groupNames.get("a"));
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "a");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ab").size(), 0.15, 0.35);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ab");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abc").size(), 0.15, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abc");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abcd").size(), 0.15, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abcd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("abd").size(), 0.15, 0.05);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("b") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "abd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ac").size(), 0.05, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ac");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("acd").size(), 0.05, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "acd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("ad").size(), 0.05, 0.05);
            anotation.setToolTipText("<html>" + groupNames.get("a") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "ad");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("b").size(), 0.25, 0.35);
            anotation.setToolTipText("<html>" + groupNames.get("b") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "b");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bc").size(), 0.25, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bc");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bcd").size(), 0.25, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bcd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("bd").size(), 0.25, 0.05);
            anotation.setToolTipText("<html>" + groupNames.get("b") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "bd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("c").size(), 0.35, 0.25);
            anotation.setToolTipText("<html>" + groupNames.get("c") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "c");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("cd").size(), 0.35, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("c") + " &#8745; " + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "cd");

            anotation = new XYTextAnnotation("" + vennDiagramResults.get("d").size(), 0.45, 0.15);
            anotation.setToolTipText("<html>" + groupNames.get("d") + "</html>");
            anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeValues));
            plot.addAnnotation(anotation);
            tooltipToDatasetMap.put(anotation.getToolTipText(), "d");

            // legend
            if (showLegend) {
                anotation = new XYTextAnnotation(groupNames.get("a"), legendDatasetAFourWay.getX(), legendDatasetAFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("b"), legendDatasetBFourWay.getX(), legendDatasetBFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("c"), legendDatasetCFourWay.getX(), legendDatasetCFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
                anotation = new XYTextAnnotation(groupNames.get("d"), legendDatasetDFourWay.getX(), legendDatasetDFourWay.getY());
                anotation.setTextAnchor(TextAnchor.BASELINE_LEFT);
                anotation.setFont(new Font(anotation.getFont().getFontName(), Font.BOLD, fontSizeLegend));
                plot.addAnnotation(anotation);
            }
        }

        // set up the renderer
        XYBubbleRenderer renderer = new XYBubbleRenderer(XYBubbleRenderer.SCALE_ON_RANGE_AXIS);
        renderer.setBaseToolTipGenerator(new StandardXYZToolTipGenerator());
        plot.setRenderer(renderer);

        // make all datapoints semitransparent
        plot.setForegroundAlpha(0.5f);

        // remove space before/after the domain axis
        plot.getDomainAxis().setUpperMargin(0);
        plot.getDomainAxis().setLowerMargin(0);

        plot.setRangeGridlinePaint(Color.black);

        // hide unwanted chart details
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        chart.getPlot().setOutlineVisible(false);

        // set background color
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);
        chartPanel = new ChartPanel(chart);

        // disable the pop up menu
        chartPanel.setPopupMenu(null);

        chartPanel.setBackground(Color.WHITE);

        // add the plot to the chart
        plotPanel.add(chartPanel);
        plotPanel.revalidate();
        plotPanel.repaint();

        // add chart mouse listener
        chartPanel.addChartMouseListener(
                new ChartMouseListener() {
                    public void chartMouseClicked(ChartMouseEvent cme) {
                        mouseClickedInChart(cme);
                    }

                    public void chartMouseMoved(ChartMouseEvent cme) {
                        mouseMovedInChart(cme);
                    }
                });

        // add more chart mouse listeners
        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
    }

    /**
     * Handles mouse clicks in the chart panel.
     *
     * @param event the chart mouse event
     */
    private void mouseClickedInChart(ChartMouseEvent event) {

        ArrayList<ChartEntity> entities = getEntitiesForPoint(event.getTrigger().getPoint().x, event.getTrigger().getPoint().y);

        if (entities.isEmpty()) {
            return;
        }

        boolean dataPointFound = false;
        String dataPointTooltip = "";

        for (ChartEntity tempEntity : entities) {
            if (tempEntity instanceof XYAnnotationEntity) {
                if (((XYAnnotationEntity) tempEntity).getToolTipText() != null) {
                    dataPointFound = true;
                    dataPointTooltip = ((XYAnnotationEntity) tempEntity).getToolTipText();
                }
            }
        }

        if (dataPointFound) {
            String dataset = tooltipToDatasetMap.get(dataPointTooltip);
            JOptionPane.showMessageDialog(this, dataPointTooltip + ":\n" + vennDiagramResults.get(dataset), "Selected Values", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Handles mouse movements in the chart panel.
     *
     * @param event the chart mouse event
     */
    private void mouseMovedInChart(ChartMouseEvent event) {

        ArrayList<ChartEntity> entities = getEntitiesForPoint(event.getTrigger().getPoint().x, event.getTrigger().getPoint().y);

        boolean dataPointFound = false;

        for (ChartEntity tempEntity : entities) {
            if (tempEntity instanceof XYAnnotationEntity) {
                if (((XYAnnotationEntity) tempEntity).getToolTipText() != null) {
                    dataPointFound = true;
                }
            }
        }

        if (dataPointFound) {
            chartPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else {
            chartPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Returns a list of the entities at the given x, y view location.
     *
     * @param viewX the x location
     * @param viewY the y location
     * @return a list of the entities
     */
    public ArrayList<ChartEntity> getEntitiesForPoint(int viewX, int viewY) {

        ArrayList<ChartEntity> entitiesForPoint = new ArrayList<>();
        ChartRenderingInfo info = chartPanel.getChartRenderingInfo();

        if (info != null) {
            Insets insets = chartPanel.getInsets();
            double x = (viewX - insets.left) / chartPanel.getScaleX();
            double y = (viewY - insets.top) / chartPanel.getScaleY();
            EntityCollection allEntities = info.getEntityCollection();
            int numEntities = allEntities.getEntityCount();

            for (int i = 0; i < numEntities; i++) {
                ChartEntity entity = allEntities.getEntity(i);
                if (entity.getArea().contains(x, y)) {
                    entitiesForPoint.add(entity);
                }
            }
        }

        return entitiesForPoint;
    }

    /**
     * Returns the chart panel.
     *
     * @return the chart panel
     */
    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    /**
     * Returns a standard map of the group names. Keys: a, b, c and d.
     *
     * @return a standard map of the group names
     */
    public HashMap<String, String> getGroupNames() {
        return groupNames;
    }

    /**
     * Returns the legend location of Dataset A in a three way Venn diagram.
     *
     * @return the legendDatasetAThreeWay
     */
    public XYDataPoint getLegendDatasetAThreeWay() {
        return legendDatasetAThreeWay;
    }

    /**
     * Set the legend location of Dataset A in a three way Venn diagram.
     *
     * @param legendDatasetAThreeWay the legendDatasetAThreeWay to set
     */
    public void setLegendDatasetAThreeWay(XYDataPoint legendDatasetAThreeWay) {
        this.legendDatasetAThreeWay = legendDatasetAThreeWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset B in a three way Venn diagram.
     *
     * @return the legendDatasetBThreeWay
     */
    public XYDataPoint getLegendDatasetBThreeWay() {
        return legendDatasetBThreeWay;
    }

    /**
     * Set the legend location of Dataset B in a three way Venn diagram.
     *
     * @param legendDatasetBThreeWay the legendDatasetBThreeWay to set
     */
    public void setLegendDatasetBThreeWay(XYDataPoint legendDatasetBThreeWay) {
        this.legendDatasetBThreeWay = legendDatasetBThreeWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset C in a three way Venn diagram.
     *
     * @return the legendDatasetCThreeWay
     */
    public XYDataPoint getLegendDatasetCThreeWay() {
        return legendDatasetCThreeWay;
    }

    /**
     * Set the legend location of Dataset C in a three way Venn diagram.
     *
     * @param legendDatasetCThreeWay the legendDatasetCThreeWay to set
     */
    public void setLegendDatasetCThreeWay(XYDataPoint legendDatasetCThreeWay) {
        this.legendDatasetCThreeWay = legendDatasetCThreeWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset A in a four way Venn diagram.
     *
     * @return the legendDatasetAFourWay
     */
    public XYDataPoint getLegendDatasetAFourWay() {
        return legendDatasetAFourWay;
    }

    /**
     * Set the legend location of Dataset A in a four way Venn diagram.
     *
     * @param legendDatasetAFourWay the legendDatasetAFourWay to set
     */
    public void setLegendDatasetAFourWay(XYDataPoint legendDatasetAFourWay) {
        this.legendDatasetAFourWay = legendDatasetAFourWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset B in a four way Venn diagram.
     *
     * @return the legendDatasetBFourWay
     */
    public XYDataPoint getLegendDatasetBFourWay() {
        return legendDatasetBFourWay;
    }

    /**
     * Set the legend location of Dataset B in a four way Venn diagram.
     *
     * @param legendDatasetBFourWay the legendDatasetBFourWay to set
     */
    public void setLegendDatasetBFourWay(XYDataPoint legendDatasetBFourWay) {
        this.legendDatasetBFourWay = legendDatasetBFourWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset C in a four way Venn diagram.
     *
     * @return the legendDatasetCFourWay
     */
    public XYDataPoint getLegendDatasetCFourWay() {
        return legendDatasetCFourWay;
    }

    /**
     * Set the legend location of Dataset C in a four way Venn diagram.
     *
     * @param legendDatasetCFourWay the legendDatasetCFourWay to set
     */
    public void setLegendDatasetCFourWay(XYDataPoint legendDatasetCFourWay) {
        this.legendDatasetCFourWay = legendDatasetCFourWay;
        updatePlot();
    }

    /**
     * Returns the legend location of dataset D in a four way Venn diagram.
     *
     * @return the legendDatasetDFourWay
     */
    public XYDataPoint getLegendDatasetDFourWay() {
        return legendDatasetDFourWay;
    }

    /**
     * Set the legend location of Dataset D in a four way Venn diagram.
     *
     * @param legendDatasetDFourWay the legendDatasetDFourWay to set
     */
    public void setLegendDatasetDFourWay(XYDataPoint legendDatasetDFourWay) {
        this.legendDatasetDFourWay = legendDatasetDFourWay;
        updatePlot();
    }

    /**
     * Returns the current Venn diagram type.
     *
     * @return the currentVennDiagramType
     */
    public VennDiagramType getCurrentVennDiagramType() {
        return currentVennDiagramType;
    }

    public void resizePlot(int parentWidth, int parentHeight) {
        if (currentVennDiagramType != VennDiagramType.FOUR_WAY) {
            int min = Math.min(parentWidth, parentHeight);
            setMaximumSize(new Dimension(min, min));
            setPreferredSize(new Dimension(min, min));
            updatePlot();
        } else {
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            updatePlot();
        }
    }

    /**
     * Show the legend or not.
     * 
     * @return show the legend or not
     */
    public boolean showLegend() {
        return showLegend;
    }

    /**
     * Set if the legend is to be shown.
     * 
     * @param showLegend if the legend is to be shown
     */
    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    /**
     * Get the font size.
     * 
     * @return the font size
     */
    public int getFontSize() {
        return fontSizeValues;
    }

    /**
     * Set the font size.
     * 
     * @param fontSizeValues the font size
     */
    public void setFontSize(int fontSizeValues) {
        this.fontSizeValues = fontSizeValues;
    }

    /**
     * Get the font size for the legend.
     * 
     * @return the font size for the legend
     */
    public int getFontSizeLegend() {
        return fontSizeLegend;
    }

    /**
     * Set the font size of the legend.
     * 
     * @param fontSizeLegend the font size of the legend
     */
    public void setFontSizeLegend(int fontSizeLegend) {
        this.fontSizeLegend = fontSizeLegend;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel plotPanel;
    // End of variables declaration//GEN-END:variables
}
