/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 03-Sep-2009
 * Time: 11:11:48
 */
package com.compomics.util.gui.spectrum;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
/*
 * CVS information:
 *
 * $Revision$
 * $Date$
 */

/**
 * This class provides a JPanel that can display a chromatogram, or a profile mass spectrum.
 *
 * @author Lennart Martens
 * @author Harald Barsnes
 * @version $Id$
 */
public class ChromatogramPanel extends JPanel {

    /**
     * Color in which the chromatogram points are rendered. Defaults to black.
     */
    private Color iChromatogramPointColor = Color.BLACK;
    /**
     * Color in which the chromatogram polyline is rendered. Defaults to gray.
     */
    private Color iChromatogramLineColor = Color.GRAY;
    /**
     * Size for the point on a chromatogram.
     */
    private Integer iPointSize = 4;
    /**
     * When the mouse is dragged, this represents the
     * X-coordinate of the starting location.
     */
    private int iStartXLoc = 0;
    /**
     * When the mouse is dragged, this represents the
     * Y-coordinate of the starting location.
     */
    private int iStartYLoc = 0;
    /**
     * When the mouse is dragged, this represents the
     * X-coordinate of the ending location.
     */
    private int iEndXLoc = 0;
    /**
     * The current dragging location.
     */
    private int iDragXLoc = 0;
    /**
     * Scale unit for the X axis
     */
    private double iXScaleUnit = 0.0;
    /**
     * Scale unit for the Y axis
     */
    private double iYScaleUnit = 0.0;
    /**
     * Effective distance from the x-axis to the panel border.
     */
    private int iXPadding = 0;
    /**
     * Effective distance from the panel top border
     * to 5 pixels above the top of the highest peak (or y-tick mark).
     */
    private int iTopPadding = 0;
    /**
     * The deviation (both left and right) allowed for point highlighting detection.
     */
    private int iPointDetectionTolerance = 5;
    /**
     * Boolean that will be 'true' when a peak needs highlighting.
     */
    private boolean iHighLight = false;
    /**
     * Index of the peak that needs to be highlighted.
     */
    private int iHighLightIndex = 0;
    /**
     * This boolean is set to 'true' if the mz axis should start at zero.
     */
    private boolean iXAxisStartAtZero = true;
    /**
     * This boolean is set to 'true' when dragging is performed.
     */
    private boolean iDragged = false;
    /**
     * The number of X-axis tags.
     */
    private int xTagCount = 10;
    /**
     * The number of Y-axis tags.
     */
    private int yTagCount = 10;
    /**
     * The padding (distance between the axes and the border of the panel).
     */
    private int padding = 20;
    /**
     * The maximum padding (distance between the axes and the border of the panel).
     */
    private int maxPadding = 50;
    /**
     * The boolean is set to 'true' if the decimals should not be shown for the axis tags.
     */
    private boolean hideDecimals = false;
    /**
     * The double[] with all the x-axis data points.
     * Should at all times be sorted from high to low.
     */
    private double[] iXAxisData = null;
    /**
     * The minimum mass to display.
     */
    private double iXAxisMin = 0.0;
    /**
     * The maximum mass to display.
     */
    private double iXAxisMax = 0.0;
    /**
     * The minimum intensity to display.
     */
    private double iYAxisMin = 0.0;
    /**
     * The maximum intensity to display.
     */
    private double iYAxisMax = 0.0;
    /**
     * The index of the point with the highest y-axis measurement.
     */
    private int iYAxisMaxIndex = 0;
    /**
     * The double[] with all the intensities. Related to the masses by index.
     * So the first intensity is the intensity for the first mass in the 'iMasses'
     * variable.
     */
    private double[] iYAxisData = null;
    /**
     * The label (and unit between brackets, if available) for the x-axis.
     */
    private String iXAxisLabel = null;
    /**
     * The label (and unit between brackets, if available) for the y-axis.
     */
    private String iYAxisLabel = null;
    /**
     * This array will hold the x-coordinates in pixels for
     * all the masses. Link is through index.
     */
    private int[] iXAxisDataInPixels = null;
    /**
     * This array will hold the y-coordinates in pixels for
     * all the masses. Link is through index.
     */
    private int[] iYAxisDataInPixels = null;
    /**
     * Minimal dragging distance in pixels.
     */
    private int iMinDrag = 15;
    /**
     * This variable holds the precursor M/Z.
     */
    private double iPrecursorMZ = 0.0;
    /**
     * This String holds the charge for the precursor.
     */
    private String iPrecursorCharge = null;
    /**
     * The ms level of the current spectrum.
     */
    private int iMSLevel = 0;
    /**
     * The spectrum filename.
     */
    private String iSpecFilename = null;
    /**
     * Boolean to indicate whether a filename should be shown.
     */
    private boolean showFileName = false;
    /**
     * Boolean to indicate whether this is a spectrum.
     */
    private boolean isSpectrum = false;

    /**
     * This constructor creates a ChromatogramPanel based on the passed parameters. This constructor assumes
     * chromatogram data rather than profile spectrum data.
     *
     * @param aXAxisData    double[] with all the X axis data.
     * @param aYAxisData    double[] with all the Y axis data.
     */
    public ChromatogramPanel(double[] aXAxisData, double[] aYAxisData) {
        this(aXAxisData, aYAxisData, null, null, null);
    }

    /**
     * This constructor creates a ChromatogramPanel based on the passed parameters. This constructor assumes
     * chromatogram data rather than profile spectrum data.
     *
     * @param aXAxisData    double[] with all the X axis data.
     * @param aYAxisData    double[] with all the Y axis data.
     * @param aXAxisLabel   String with the label for the x-axis
     *                      (can have a unit between brackets, if available) - can be 'null' for no label
     * @param aYAxisLabel   String with the label for the y-axis
     *                      (can have a unit between brackets, if available) - can be 'null' for no label
     */
    public ChromatogramPanel(double[] aXAxisData, double[] aYAxisData, String aXAxisLabel, String aYAxisLabel) {
        this(aXAxisData, aYAxisData, aXAxisLabel, aYAxisLabel, null);
    }

    /**
     * This constructor creates a ChromatogramPanel based on the passed parameters. This constructor assumes
     * chromatogram data rather than profile spectrum data.
     *
     * @param aXAxisData    double[] with all the X axis data.
     * @param aYAxisData    double[] with all the Y axis data.
     * @param aXAxisLabel   String with the label for the x-axis
     *                      (can have a unit between brackets, if available) - can be 'null' for no label
     * @param aYAxisLabel   String with the label for the y-axis
     *                      (can have a unit between brackets, if available) - can be 'null' for no label
     * @param aPointSize    Integer with the point size to use
     */
    public ChromatogramPanel(double[] aXAxisData, double[] aYAxisData, String aXAxisLabel, String aYAxisLabel, Integer aPointSize) {

        // if point size is given, update the point size, otherwise keep the default point size
        if (aPointSize != null) {
            this.setPointSize(aPointSize);
        }

        initData(aXAxisData, aYAxisData, aXAxisLabel, aYAxisLabel);
        this.addListeners();
    }

    /**
     * This constructor creates a ChromatogramPanel based on the passed parameters.
     * <b>Note</b> that it is intended for use with a profile spectrum rather than a
     * chromatogram, and it flag the display to assume spectrum layout and properties,
     * which are distinct from the chromatogram ones!
     *
     * @param aXAxisData            double[] with all the X axis data.
     * @param aYAxisData            double[] with all the Y axis data.
     * @param aMSLevel              int with the ms level for the spectrum
     * @param aPrecursorMz          Double with the precursor m/z.
     * @param aPrecursorCharge      String with the precursor charge.
     * @param aFilename             String with the title of the spectrum.
     */
    public ChromatogramPanel(double[] aXAxisData, double[] aYAxisData, int aMSLevel, double aPrecursorMz, String aPrecursorCharge, String aFilename) {
        this(aXAxisData, aYAxisData, aMSLevel, aPrecursorMz, aPrecursorCharge, aFilename, null);
    }

    /**
     * This constructor creates a ChromatogramPanel based on the passed parameters.
     * <b>Note</b> that it is intended for use with a profile spectrum rather than a
     * chromatogram, and it flag the display to assume spectrum layout and properties,
     * which are distinct from the chromatogram ones!
     *
     * @param aXAxisData            double[] with all the X axis data.
     * @param aYAxisData            double[] with all the Y axis data.
     * @param aMSLevel              int with the ms level for the spectrum
     * @param aPrecursorMz          Double with the precursor m/z.
     * @param aPrecursorCharge      String with the precursor charge.
     * @param aFilename             String with the title of the spectrum.
     * @param aPointSize            Integer with the point size to use
     */
    public ChromatogramPanel(double[] aXAxisData, double[] aYAxisData, int aMSLevel, double aPrecursorMz, String aPrecursorCharge, String aFilename, Integer aPointSize) {
        this.initData(aXAxisData, aYAxisData, "m/z", "Int");

        if (aFilename != null) {
            iSpecFilename = aFilename;
            showFileName = true;
        }

        iPrecursorMZ = aPrecursorMz;
        iPrecursorCharge = aPrecursorCharge;
        iMSLevel = aMSLevel;
        isSpectrum = true;

        // if point size is given, update the point size, otherwise keep the default point size
        if (aPointSize != null) {
            this.setPointSize(aPointSize);
        }

        this.setChromatogramLineColor(Color.PINK);
        this.setChromatogramPointColor(Color.RED);

        this.addListeners();
    }

    /**
     * This method wraps all the shared logic of the various constructors.
     *
     * @param aXAxisData    double[] with all the X axis data.
     * @param aYAxisData    double[] with all the Y axis data.
     * @param aXAxisLabel   String with the label for the x-axis
     *                      (can have a unit between brackets, if available) - can be 'null' for no label
     * @param aYAxisLabel   String with the label for the y-axis
     *                      (can have a unit between brackets, if available) - can be 'null' for no label
     */
    private void initData(double[] aXAxisData, double[] aYAxisData, String aXAxisLabel, String aYAxisLabel) {
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setBackground(Color.WHITE);
        processXandYData(aXAxisData, aYAxisData);
        this.iXAxisLabel = (aXAxisLabel == null ? "unknown" : aXAxisLabel);
        this.iYAxisLabel = (aYAxisLabel == null ? "unknown" : aYAxisLabel);
    }

    /**
     * This method sets the start value of the x-axis to zero.
     */
    public void setXAxisStartAtZero(boolean aXAxisStartAtZero) {
        iXAxisStartAtZero = aXAxisStartAtZero;
    }

    /**
     * Invoked by Swing to draw components.
     * Applications should not invoke <code>paint</code> directly,
     * but should instead use the <code>repaint</code> method to
     * schedule the component for redrawing.
     * <p/>
     * This method actually delegates the work of painting to three
     * protected methods: <code>paintComponent</code>,
     * <code>paintBorder</code>,
     * and <code>paintChildren</code>.  They're called in the order
     * listed to ensure that children appear on top of component itself.
     * Generally speaking, the component and its children should not
     * paint in the insets area allocated to the border. Subclasses can
     * just override this method, as always.  A subclass that just
     * wants to specialize the UI (look and feel) delegate's
     * <code>paint</code> method should just override
     * <code>paintComponent</code>.
     *
     * @param g the <code>Graphics</code> context in which to paint
     * @see #paintComponent
     * @see #paintBorder
     * @see #paintChildren
     * @see #getComponentGraphics
     * @see #repaint
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (iXAxisData != null) {
            if (iDragged && iDragXLoc > 0) {
                g.drawLine(iStartXLoc, iStartYLoc, iDragXLoc, iStartYLoc);
                g.drawLine(iStartXLoc, iStartYLoc - 2, iStartXLoc, iStartYLoc + 2);
                g.drawLine(iDragXLoc, iStartYLoc - 2, iDragXLoc, iStartYLoc + 2);
            }
            drawAxes(g, iXAxisMin, iXAxisMax, 2, iYAxisMin, iYAxisMax);
            drawChromatogram(g);
            if (iHighLight) {
                this.highLightPoint(iHighLightIndex, g);
                iHighLight = false;
            }
        }
    }

    /**
     * This method reports on the largest x-axis measurement.
     *
     * @return double with the largest x-axis measurement.
     */
    public double getMaxXAxisMeasurement() {
        return iXAxisData[iXAxisData.length - 1];
    }

    /**
     * This method reports on the smallest x-axis measurement.
     *
     * @return double with the smallest x-axis measurement.
     */
    public double getMinXAxisMeasurement() {
        return iXAxisData[0];
    }

    /**
     * This method allows the caller to set the point size for the
     * chromatogram. b>Note</b> that this number needs to be even, so
     * any uneven number will be replaced by the closest, lower, even
     * integer (e.g., 5 becomes 4, 13 becomes 12).
     *
     * @param aPointSize int with the point size, that will be reduced
     *                   to the closest, lower even integer  (e.g.,
     *                   5 becomes 4, 13 becomes 12).
     */
    public void setPointSize(Integer aPointSize) {
        if (aPointSize % 2 != 0) {
            aPointSize--;
        }
        iPointSize = aPointSize;
    }

    /**
     * This method adds the event listeners to the panel.
     */
    private void addListeners() {
        this.addMouseListener(new MouseAdapter() {

            /**
             * Invoked when a mouse button has been released on a component.
             */
            public void mouseReleased(MouseEvent e) {
                if (iXAxisData != null) {
                    if (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2) {
                        if (iXAxisStartAtZero) {
                            rescale(0.0, iXAxisData[iXAxisData.length - 1]);
                        } else {
                            rescale(iXAxisData[0], iXAxisData[iXAxisData.length - 1]);
                        }
                        iDragged = false;
                        repaint();
                    } else if (e.getButton() == MouseEvent.BUTTON1) {
                        iEndXLoc = e.getX();
                        int min = Math.min(iEndXLoc, iStartXLoc);
                        int max = Math.max(iEndXLoc, iStartXLoc);
                        double start = iXAxisMin + ((min - iXPadding) * iXScaleUnit);
                        double end = iXAxisMin + ((max - iXPadding) * iXScaleUnit);
                        if (iDragged) {
                            iDragged = false;
                            // Rescale.
                            if ((max - min) > iMinDrag) {
                                rescale(start, end);
                            }
                            iDragXLoc = 0;
                            repaint();
                        }
                    }
                }
            }

            /**
             * Invoked when a mouse button has been pressed on a component.
             */
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    iStartXLoc = e.getX();
                    iStartYLoc = e.getY();
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {

            /**
             * Invoked when a mouse button is pressed on a component and then
             * dragged.  Mouse drag events will continue to be delivered to
             * the component where the first originated until the mouse button is
             * released (regardless of whether the mouse position is within the
             * bounds of the component).
             */
            public void mouseDragged(MouseEvent e) {
                iDragged = true;
                iDragXLoc = e.getX();
                repaint();
            }

            /**
             * Invoked when the mouse button has been moved on a component
             * (with no buttons no down).
             */
            public void mouseMoved(MouseEvent e) {
                if (iXAxisData != null && iXAxisDataInPixels != null) {
                    int x = e.getX();
                    int y = e.getY();
                    for (int i = 0; i < iXAxisDataInPixels.length; i++) {
                        int delta = iXAxisDataInPixels[i] - x;
                        if (Math.abs(delta) < iPointDetectionTolerance) {
                            int deltaYPixels = y - iYAxisDataInPixels[i];
                            if (deltaYPixels < 0 && Math.abs(deltaYPixels) < (getHeight() - iYAxisDataInPixels[i])) {
                                iHighLight = true;
                                iHighLightIndex = i;
                                repaint();
                            }
                        } else if (delta >= iPointDetectionTolerance) {
                            break;
                        }
                    }
                    repaint();
                }
            }
        });
    }

    /**
     * This method rescales the X-axis while notifying the observers.
     *
     * @param aMinXAxisMeasurement  double with the new minimum x-axis measurement to display.
     * @param aMaxXAxisMeasurement  double with the new maximum x-axis measurement to display.
     */
    public void rescale(double aMinXAxisMeasurement, double aMaxXAxisMeasurement) {
        this.rescale(aMinXAxisMeasurement, aMaxXAxisMeasurement, true);
    }

    /**
     * This method sets the color in which the points on the chromatogram will be rendered.
     *
     * @param aColor Color to render the points on the chromatogram in.
     */
    public void setChromatogramPointColor(Color aColor) {
        this.iChromatogramPointColor = aColor;
    }

    /**
     * This method sets the color in which the line of the chromatogram will be rendered.
     *
     * @param aColor Color to render the line of the chromatogram in.
     */
    public void setChromatogramLineColor(Color aColor) {
        this.iChromatogramLineColor = aColor;
    }

    /**
     * This method rescales the X-axis, allowing the caller to specify whether the
     * observers need be notified.
     *
     * @param aMinXAxisMeasurement  double with the new minimum x-axis measurement to display.
     * @param aMaxXAxisMeasurement  double with the new maximum x-axis measurement to display.
     * @param aNotifyListeners  boolean to indicate whether the observers should be notified.
     */
    public void rescale(double aMinXAxisMeasurement, double aMaxXAxisMeasurement, boolean aNotifyListeners) {
        // Calculate the new max intensity.
        double maxInt = 1.0;
        for (int i = 0; i < iXAxisData.length; i++) {
            double lMass = iXAxisData[i];
            if (lMass < aMinXAxisMeasurement) {
                continue;
            } else if (lMass > aMaxXAxisMeasurement) {
                break;
            } else {
                if (iYAxisData[i] > maxInt) {
                    maxInt = iYAxisData[i];
                    iYAxisMaxIndex = i;
                }
            }
        }
        // Init the new params.
        double delta = aMaxXAxisMeasurement - aMinXAxisMeasurement;

        // Round to nearest order of 10, based on displayed delta.
        double tempOoM = (Math.log(delta) / Math.log(10)) - 1;
        if (tempOoM < 0) {
            tempOoM--;
        }
        int orderOfMagnitude = (int) tempOoM;
        double power = Math.pow(10, orderOfMagnitude);
        iXAxisMin = aMinXAxisMeasurement - (aMinXAxisMeasurement % power);
        iXAxisMax = aMaxXAxisMeasurement + (power - (aMaxXAxisMeasurement % power));

        iYAxisMax = maxInt + (maxInt / 10);
    }

    /**
     * This method will highlight a point.
     *
     * @param aIndex int with the index of the point to highlight.
     * @param g Graphics object to draw the highlighting on.
     */
    private void highLightPoint(int aIndex, Graphics g) {
        this.highLight(aIndex, g, Color.blue, null, 15, true);
    }

    /**
     * This method reads the X and Y axes data from the specified
     * arrays and stores these internally for drawing. The axes are sorted
     * in this step.
     *
     * @param aXAxisData double[] with the x-axis measurements.
     * @param aYAxisData double[] with the Y-axis measurements.
     */
    private void processXandYData(double[] aXAxisData, double[] aYAxisData) {
        HashMap peaks = new HashMap(aXAxisData.length);
        iXAxisData = new double[aXAxisData.length];
        iYAxisData = new double[aXAxisData.length];
        for (int i = 0; i < aXAxisData.length; i++) {
            peaks.put(new Double(aXAxisData[i]), new Double(aYAxisData[i]));
        }
        // Maximum intensity of the peaks.
        double maxYAxisMeasurement = 0.0;
        // TreeSets are sorted.
        TreeSet xAxisData = new TreeSet(peaks.keySet());
        Iterator iter = xAxisData.iterator();
        int count = 0;
        while (iter.hasNext()) {
            Double key = (Double) iter.next();
            double xMeasurement = key.doubleValue();
            double yMeasurement = ((Double) peaks.get(key)).doubleValue();
            if (yMeasurement > maxYAxisMeasurement) {
                maxYAxisMeasurement = yMeasurement;
            }
            iXAxisData[count] = xMeasurement;
            iYAxisData[count] = yMeasurement;
            count++;
        }
        if (iXAxisStartAtZero) {
            this.rescale(0.0, iXAxisData[iXAxisData.length - 1]);
        } else {
            this.rescale(iXAxisData[0], iXAxisData[iXAxisData.length - 1]);
        }
    }

    /**
     * This method draws the axes and their labels on the specified Graphics object,
     * taking into account the padding.
     *
     * @param g Graphics object to draw on.
     * @param aXMin double with the minimal x value.
     * @param aXMax double with the maximum x value.
     * @param aXScale int with the scale to display for the X-axis labels (as used in BigDecimal's setScale).
     * @param aYMin double with the minimal y value.
     * @param aYMax double with the maximum y value.
     * @return int[] with the length of the X axis and Y axis respectively.
     */
    private int[] drawAxes(Graphics g, double aXMin, double aXMax, int aXScale, double aYMin, double aYMax) {
        // Recalibrate padding so that it holds the axis labels.
        FontMetrics fm = g.getFontMetrics();
        int xAxisLabelWidth = fm.stringWidth(iXAxisLabel);
        int yAxisLabelWidth = fm.stringWidth(iYAxisLabel);
        int minWidth = fm.stringWidth(Double.toString(aYMin));
        int maxWidth = fm.stringWidth(Double.toString(aYMax));
        int max = Math.max(Math.max(yAxisLabelWidth, xAxisLabelWidth), Math.max(minWidth, maxWidth));
        int tempPadding = padding;
        if ((padding - max) < 0) {
            tempPadding += max;
            if (tempPadding > maxPadding) {
                tempPadding = maxPadding;
            }
        } else {
            tempPadding *= 2;
        }
        // X-axis.
        int xaxis = (this.getWidth() - (2 * tempPadding));
        g.drawLine(tempPadding, this.getHeight() - tempPadding, this.getWidth() - tempPadding, this.getHeight() - tempPadding);
        // Arrowhead on X-axis.
        g.fillPolygon(new int[]{this.getWidth() - tempPadding - 5, this.getWidth() - tempPadding - 5, this.getWidth() - tempPadding},
                new int[]{this.getHeight() - tempPadding + 5, this.getHeight() - tempPadding - 5, this.getHeight() - tempPadding},
                3);
        // X-axis label
        if(iXAxisLabel.equalsIgnoreCase("m/z")){
            g.drawString(iXAxisLabel, this.getWidth() - (tempPadding - (padding / 2)), this.getHeight() - tempPadding + 4);
        } else {
            g.drawString(iXAxisLabel, this.getWidth() - (xAxisLabelWidth + 5), this.getHeight() - (tempPadding / 2));
        }
        // Y-axis.
        g.drawLine(tempPadding, this.getHeight() - tempPadding, tempPadding, tempPadding / 2);
        iXPadding = tempPadding;
        int yaxis = this.getHeight() - tempPadding - (tempPadding / 2);
        // Arrowhead on Y axis.
        g.fillPolygon(new int[]{tempPadding - 5, tempPadding + 5, tempPadding},
                new int[]{(tempPadding / 2) + 5, (tempPadding / 2) + 5, tempPadding / 2},
                3);
        // Y-axis label
        if(iYAxisLabel.equalsIgnoreCase("Int")){
            g.drawString(iYAxisLabel, tempPadding - yAxisLabelWidth, (tempPadding / 2) - 4);
        } else {
            g.drawString(iYAxisLabel, tempPadding - (yAxisLabelWidth / 5), (tempPadding / 2) - 4);
        }

        // Now the tags along the axes.
        this.drawXTags(g, aXMin, aXMax, aXScale, xaxis, tempPadding);
        int yTemp = yaxis;
        iTopPadding = this.getHeight() - yTemp - 5;
        this.drawYTags(g, aYMin, aYMax, yTemp, tempPadding);

        return new int[]{xaxis, yaxis};
    }

    /**
     * This method draws tags on the X axis.
     *
     * @param aMin  double with the minimum value for the axis.
     * @param aMax  double with the maximum value for the axis.
     * @param aXScale int with the scale to display for the X-axis labels (as used in BigDecimal's setScale).
     * @param g Graphics object to draw on.
     * @param aXAxisWidth   int with the axis width in pixels.
     * @param aPadding  int with the amount of padding to take into account.
     */
    private void drawXTags(Graphics g, double aMin, double aMax, int aXScale, int aXAxisWidth, int aPadding) {
        // Font Metrics. We'll be needing these.
        FontMetrics fm = g.getFontMetrics();
        // Find out how many tags we will have. At most, we'll have xTagCount tags, and if the resolution
        // of the screen is too small, we'll have less.
        int tagWidthEstimate = fm.stringWidth("1545.99") + 15;
        int numberTimes = (aXAxisWidth / tagWidthEstimate);
        if (numberTimes > xTagCount) {
            numberTimes = xTagCount;
        } else if (numberTimes == 0) {
            numberTimes = 1;
        }
        // Calculate the graphical unit, ...
        int xUnit = aXAxisWidth / numberTimes;
        // ... as well as the scale unit.
        double delta = aMax - aMin;
        double scaleUnit = delta / numberTimes;
        iXScaleUnit = delta / aXAxisWidth;

        // The next section will only be drawn for profile spectra.
        if (isSpectrum) {
            // Since we know the scale unit, we also know the resolution.
            // This will be displayed on the bottom line.
            String resolution = "Resolution: " + new BigDecimal(iXScaleUnit).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            String msLevel_and_optional_precursor = "MS level: " + iMSLevel;
            if (iMSLevel > 1) {
                // Also print the precursor MZ and charge (if known, '?' otherwise).
                msLevel_and_optional_precursor += "   Precursor M/Z: " + this.iPrecursorMZ + " (" + this.iPrecursorCharge + ")";
            }
            // Finally, we also want the filename.
            String filename = "";
            if (showFileName) {
                filename = "Filename: " + iSpecFilename;
            }
            int precLength = fm.stringWidth(msLevel_and_optional_precursor);
            int resLength = fm.stringWidth(resolution);
            int xDistance = ((this.getWidth() - (iXPadding * 2)) / 4) - (precLength / 2);
            int fromBottom = fm.getAscent() / 2;
            Font oldFont = this.getFont();

            int smallFontCorrection = 0;
            int yHeight = this.getHeight() - fromBottom;
            int xAdditionForResolution = precLength + 15;
            int xAdditionForFilename = xAdditionForResolution + resLength + 15;
            if (precLength + resLength + 45 + fm.stringWidth(filename) > aXAxisWidth) {
                g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
                smallFontCorrection = g.getFontMetrics().getAscent();
                xAdditionForFilename = g.getFontMetrics().stringWidth(msLevel_and_optional_precursor) + 5;
                xAdditionForResolution = g.getFontMetrics().stringWidth(msLevel_and_optional_precursor) / 2;
                xDistance = aPadding;
            }
            g.drawString(msLevel_and_optional_precursor, xDistance, yHeight - smallFontCorrection);
            g.drawString(resolution, xDistance + xAdditionForResolution, yHeight);
            Color foreground = null;
            g.drawString(filename, xDistance + xAdditionForFilename, yHeight - smallFontCorrection);
            if (foreground != null) {
                g.setColor(foreground);
            }
            // Restore original font.
            g.setFont(oldFont);
        }

        int labelHeight = fm.getAscent() + 5;
        // Now mark each unit.
        for (int i = 0; i < numberTimes; i++) {
            int xLoc = (xUnit * i) + aPadding;
            g.drawLine(xLoc, this.getHeight() - aPadding, xLoc, this.getHeight() - aPadding + 3);
            BigDecimal bd = new BigDecimal(aMin + (scaleUnit * i));
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            String label = bd.toString();

            if (hideDecimals) {
                label = "" + bd.intValue();
            }

            int labelWidth = fm.stringWidth(label);
            g.drawString(label, xLoc - (labelWidth / 2), this.getHeight() - aPadding + labelHeight);
        }
    }

    /**
     * This method draws tags on the Y axis.
     *
     * @param aMin  double with the minimum value for the axis.
     * @param aMax  double with the maximum value for the axis.
     * @param g Graphics object to draw on.
     * @param aYAxisHeight   int with the axis height in pixels.
     * @param aPadding  int with the amount of padding to take into account.
     */
    private void drawYTags(Graphics g, double aMin, double aMax, int aYAxisHeight, int aPadding) {
        // Font Metrics. We'll be needing these.
        FontMetrics fm = g.getFontMetrics();
        int labelHeight = fm.getAscent();
        // Find out how many tags we will have. At most, we'll have xTagCount tags, and if the resolution
        // of the screen is too small, we'll have less.
        int tagHeightEstimate = labelHeight + 10;
        int numberTimes = (aYAxisHeight / tagHeightEstimate);
        if (numberTimes > yTagCount) {
            numberTimes = yTagCount;
        } else if (numberTimes == 0) {
            numberTimes = 1;
        }
        // Calculate the graphical unit, ...
        int yUnit = aYAxisHeight / numberTimes;
        // ... as well as the scale unit.
        double delta = aMax - aMin;
        double scaleUnit = delta / numberTimes;
        iYScaleUnit = delta / aYAxisHeight;

        // Find the largest display intensity.
        BigDecimal bdLargest = new BigDecimal(aMin + (scaleUnit * (numberTimes - 1)));
        bdLargest = bdLargest.setScale(2, BigDecimal.ROUND_HALF_UP);
        String largestLabel = bdLargest.toString();

        if (hideDecimals) {
            largestLabel = "" + bdLargest.intValue();
        }

        int largestWidth = 0;
        // Old font storage.
        Font oldFont = g.getFont();
        int sizeCounter = 0;
        int margin = aPadding - 10;
        while ((largestWidth = g.getFontMetrics().stringWidth(largestLabel)) >= margin) {
            sizeCounter++;
            g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - sizeCounter));
        }

        // Now mark each unit.
        for (int i = 0; i < numberTimes; i++) {
            int yLoc = (yUnit * i) + aPadding;
            g.drawLine(aPadding, this.getHeight() - yLoc, aPadding - 3, this.getHeight() - yLoc);
            BigDecimal bd = new BigDecimal(aMin + (scaleUnit * i));
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            String label = bd.toString();

            if (hideDecimals) {
                label = "" + bd.intValue();
            }

            int labelWidth = g.getFontMetrics().stringWidth(label) + 5;
            g.drawString(label, aPadding - labelWidth, this.getHeight() - yLoc + (g.getFontMetrics().getAscent() / 2) - 1);
        }
        // Restore original font.
        g.setFont(oldFont);
    }

    /**
     * This method draws the chromatogram in the current massrange
     * on the panel.
     *
     * @param g Graphics object to draw on.
     */
    private void drawChromatogram(Graphics g) {
        // Switch the color to red for the time being.
        Color originalColor = g.getColor();

        // Cycle the masses and corresponding intensities.
        // Each point is rendered.
        // We also init an array that holds pixel coordinates for each point.
        iXAxisDataInPixels = new int[iXAxisData.length];
        iYAxisDataInPixels = new int[iXAxisData.length];
        // These arrays only contain the visible points.
        ArrayList<Integer> xAxisPointsShown = new ArrayList<Integer>();
        ArrayList<Integer> yAxisPointsShown = new ArrayList<Integer>();

        // Cycle all points.
        for (int i = 0; i < iXAxisData.length; i++) {
            double xMeasurement = iXAxisData[i];
            // Only draw those x-axis measurements within the ('low x', 'high x') window.
            if (xMeasurement < iXAxisMin) {
                continue;
            } else if (xMeasurement > iXAxisMax) {
                break;
            } else {
                // See if we need to initialize the start index.
                double yMeasurement = iYAxisData[i];
                // Calculate pixel coordinates for X and Y.
                // X first.
                double tempDouble = (xMeasurement - iXAxisMin) / iXScaleUnit;
                int temp = (int) tempDouble;
                if ((tempDouble - temp) >= 0.5) {
                    temp++;
                }
                int xAxisPxl = temp + iXPadding;
                iXAxisDataInPixels[i] = xAxisPxl;
                // Now intensity.
                tempDouble = (yMeasurement - iYAxisMin) / iYScaleUnit;
                temp = (int) tempDouble;
                if ((tempDouble - temp) >= 0.5) {
                    temp++;
                }
                int yAxisPxl = this.getHeight() - (temp + iXPadding);
                iYAxisDataInPixels[i] = yAxisPxl;
                // Add to the list of points shwon.
                xAxisPointsShown.add(xAxisPxl);
                yAxisPointsShown.add(yAxisPxl);
            }
        }
        // First draw the filled polygon.
        g.setColor(iChromatogramLineColor);
        int[] xTemp = new int[xAxisPointsShown.size() + 2];
        int[] yTemp = new int[yAxisPointsShown.size() + 2];
        xTemp[0] = xAxisPointsShown.get(0).intValue();
        yTemp[0] = this.getHeight() - iXPadding;
        for (int i = 0; i < xAxisPointsShown.size(); i++) {
            xTemp[i + 1] = xAxisPointsShown.get(i).intValue();
            yTemp[i + 1] = yAxisPointsShown.get(i).intValue();
        }
        xTemp[xTemp.length - 1] = xAxisPointsShown.get(xAxisPointsShown.size() - 1).intValue();
        yTemp[xTemp.length - 1] = this.getHeight() - iXPadding;
        // Fill out the chromatogram.
        g.fillPolygon(xTemp, yTemp, xTemp.length);

        // Now draw the points, and a line connecting them.
        g.setColor(iChromatogramPointColor);
        g.drawPolyline(xTemp, yTemp, xTemp.length);
        // Skip the point for the first and last element;
        // these are just there to nicely fill the polygon.
        for (int i = 1; i < xTemp.length - 1; i++) {
            int x = xTemp[i] - (iPointSize / 2);
            int y = yTemp[i] - (iPointSize / 2);
            g.fillOval(x, y, iPointSize, iPointSize);
        }

        // Change the color back to its original setting.
        g.setColor(originalColor);
    }

    /**
     * This method will highlight the specified peak in the specified color by
     * drawing a floating triangle and mass above it.
     *
     * @param aIndex    int with the index.
     * @param g Graphics object to draw on
     * @param aColor    Color to draw the highlighting in.
     * @param aComment  String with an optional comment. Can be 'null' in which case
     *                  it will be omitted.
     * @param aPixelsSpacer int that gives the vertical spacer in pixels for the highlighting.
     * @param aShowArrow boolean that indicates whether a downward-pointing arrow and dotted line
     *                           should be drawn over the peak.
     */
    private void highLight(int aIndex, Graphics g, Color aColor, String aComment, int aPixelsSpacer, boolean aShowArrow) {

        int x = iXAxisDataInPixels[aIndex];
        int y = 0;

        if (aPixelsSpacer < 0) {
            y = iTopPadding;
        } else {
            y = iYAxisDataInPixels[aIndex] - aPixelsSpacer;
            // Correct for absurd heights.
            if (y < iTopPadding / 3) {
                y = iTopPadding / 3;
            }
        }

        // Temporarily change the color to blue.
        Color originalColor = g.getColor();
        g.setColor(aColor);

        // Draw the triangle first, if appropriate.
        int arrowSpacer = 6;
        if (aShowArrow) {
            g.fillPolygon(new int[]{x - 3, x + 3, x},
                    new int[]{y - 6, y - 6, y - 3},
                    3);
            arrowSpacer = 9;
        }

        // Now the mass.
        // If there is any, print the comment instead of the mass.
        if (aComment != null && !aComment.trim().equals("")) {
            aComment = aComment.trim();
            g.drawString(aComment, x - g.getFontMetrics().stringWidth(aComment) / 2, y - arrowSpacer);
        } else {
            // No comment, so print the mass.
            String mass = Double.toString(iXAxisData[aIndex]);
            int halfWayMass = g.getFontMetrics().stringWidth(mass) / 2;
            g.drawString(mass, x - halfWayMass, y - arrowSpacer);
        }

        // Restore original color.
        g.setColor(originalColor);
    }
}
