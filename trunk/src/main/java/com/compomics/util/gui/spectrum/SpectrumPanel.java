/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-mei-2004
 * Time: 16:34:34
 */
package com.compomics.util.gui.spectrum;
import org.apache.log4j.Logger;

import com.compomics.util.gui.events.RescalingEvent;
import com.compomics.util.gui.interfaces.SpectrumAnnotation;
import com.compomics.util.gui.interfaces.SpectrumPanelListener;
import com.compomics.util.interfaces.SpectrumFile;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.9 $
 * $Date: 2009/08/17 15:15:28 $
 */
/**
 * This class presents a JPanel that will hold and display a
 * mass spectrum.
 *
 * @author Lennart Martens
 * @author Harald Barsnes
 * @version $Id: SpectrumPanel.java,v 1.9 2009/08/17 15:15:28 lennart Exp $
 */
public class SpectrumPanel extends JPanel {
	// Class specific log4j logger for SpectrumPanel instances.
	static Logger logger = Logger.getLogger(SpectrumPanel.class);

    /**
     * This status indicates that no annotation will be displayed,
     * but the user will have a fully functional interface (peak clicking, selecting,
     * sequencing etc.)
     */
    public static final int INTERACTIVE_STATUS = 0;
    /**
     * This status indicates that annotation (if present) will be displayed,
     * while limiting the user to zooming in/out.
     */
    public static final int ANNOTATED_STATUS = 1;
    /**
     * This HashMap instance holds all the known mass deltas (if any).
     * The keys are the Doubles with the massdelta, the values are the
     * descriptions.
     */
    private static HashMap iKnownMassDeltas = null;

    // Static init block takes care of reading the 'SpectrumPanel.properties' file if
    // it hasn't already been done.


    static {
        try {
            if (iKnownMassDeltas == null) {
                iKnownMassDeltas = new HashMap();
                Properties temp = new Properties();
                InputStream is = SpectrumPanel.class.getClassLoader().getResourceAsStream("SpectrumPanel.properties");
                if (is != null) {
                    temp.load(is);
                    is.close();
                    Iterator iter = temp.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        iKnownMassDeltas.put(new Double(key), temp.getProperty(key));
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing. So now masses will be known.
        }
    }
    /**
     * This is the color the spectrumfilename should be presented in.
     */
    private Color iSpectrumFilenameColor = null;
    /**
     * Color in which the actual m/z peaks are rendered. Defaults to red.
     */
    private Color iSpectrumColor = Color.red;
    /**
     * The spectrum filename.
     */
    private String iSpecFilename = null;
    /**
     * The list of SpectrumPanelListeners.
     */
    private ArrayList iSpecPanelListeners = null;
    /**
     * The deviation (both left and right) allowed for peak highlighting detection.
     */
    private int iPeakDetectionTolerance = 5;
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
     * When the mouse is dragged, this represents the
     * Y-coordinate of the ending location.
     */
    private int iEndYLoc = 0;
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
     * Graphical unit for the X axis
     */
    private int iXUnit = 0;
    /**
     * Graphical unit for the Y axis
     */
    private int iYUnit = 0;
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
     * The current dragging location.
     */
    private int iDragYLoc = 0;
    /**
     * This boolean is set to 'true' if the mz axis should start at zero.
     */
    private boolean iMzAxisStartAtZero = true;
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
     * The boolean is set to 'true' if the file name is to be shown in the panel.
     */
    private boolean showFileName = true;
    /**
     * The boolean is set to 'true' if the precursor details is to be shown in the panel.
     */
    private boolean showPrecursorDetails = true;
    /**
     * The boolean is set to 'true' if the resolution is to be shown in the panel.
     */
    private boolean showResolution = true;
    /**
     * The double[] with all the masses. Should at all times be sorted from
     * high to low.
     */
    private double[] iMasses = null;
    /**
     * The minimum mass to display.
     */
    private double iMassMin = 0.0;
    /**
     * The maximum mass to display.
     */
    private double iMassMax = 0.0;
    /**
     * The minimum intensity to display.
     */
    private double iIntMin = 0.0;
    /**
     * The maximum intensity to display.
     */
    private double iIntMax = 0.0;
    /**
     * The index of the peak with the highest intensity.
     */
    private int iIntMaxIndex = 0;
    /**
     * The procentual non-inclusive, minimal intensity (compared to the highest
     * peak in the spectrum) a peak should have before being
     * eligible for annotation. Default is '0.0'.
     */
    private double iAnnotationIntensityThreshold = 0.0;
    /**
     * The double[] with all the intensities. Related to the masses by index.
     * So the first intensity is the intensity for the first mass in the 'iMasses'
     * variable.
     */
    private double[] iIntensities = null;
    /**
     * This variable holds the precursor M/Z.
     */
    private double iPrecursorMZ = 0.0;
    /**
     * This String holds the charge for the precursor.
     */
    private String iPrecursorCharge = null;
    /**
     * This array will hold the x-coordinates in pixels for
     * all the masses. Link is through index.
     */
    private int[] iMassInPixels = null;
    /**
     * This array will hold the y-coordinates in pixels for
     * all the masses. Link is through index.
     */
    private int[] iIntensityInPixels = null;
    /**
     * Boolean that will be 'true' when a peak needs highlighting.
     */
    private boolean iHighLight = false;
    /**
     * Index of the peak that needs to be highlighted.
     */
    private int iHighLightIndex = 0;
    /**
     * Boolean that indicates whether a peak has been marked by clicking.
     */
    private boolean iClicked = false;
    /**
     * Int that indicates which peak was clicked.
     */
    private int iClickedIndex = 0;
    /**
     * The Vector that holds all peaks clicked up to now.
     */
    private Vector iClickedList = new Vector(15, 5);
    /**
     * The Vector that holds a set of stored peaks from a previously established list.
     */
    private Vector iStoredSequence = new Vector(15, 5);
    /**
     * The Vector that holds a set of Annotation instances.
     */
    private Vector iAnnotations = new Vector(50, 20);
    /**
     * Minimal dragging distance in pixels.
     */
    private int iMinDrag = 15;
    /**
     * This variable holds the drawing style.
     */
    private int iDrawStyle = -1;
    /**
     * This variable holds the dot radius;
     * only used when drawing style is DOTS style.
     */
    private int iDotRadius = 2;
    /**
     * Drawstyle which draws lines connecting the X-axis with the measurement.
     */
    public static final int LINES = 0;
    /**
     * Drawstyle which draws a dot at the measurement height.
     */
    public static final int DOTS = 1;

    /**
     * This constructor creates a SpectrumPanel based on the spectrum information in
     * the specified SpectrumFile as an interactive lines plot.
     *
     * @param aSpecFile SpectrumFile with the information about masses and intensities
     *                  that will be copied here. Note that mass-sorting will take place
     *                  in this step as well.
     */
    public SpectrumPanel(SpectrumFile aSpecFile) {
        this(aSpecFile, LINES, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum information in
     * the specified SpectrumFile as a line plot.
     *
     * @param aSpecFile SpectrumFile with the information about masses and intensities
     *                  that will be copied here. Note that mass-sorting will take place
     *                  in this step as well.
     * @param aEnableInteraction    boolean that specifies whether user-derived events should
     *                              be caught and dealt with.
     */
    public SpectrumPanel(SpectrumFile aSpecFile, boolean aEnableInteraction) {
        this(aSpecFile, LINES, aEnableInteraction);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum information in
     * the specified SpectrumFile with the specified drawing style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and intensities
     *                  that will be copied here. Note that mass-sorting will take place
     *                  in this step as well.
     * @param aDrawStyle    int with the drawing style to use. It should be one of the constants
     *                      defined on this class.
     * @param aEnableInteraction    boolean that specifies whether user-derived events should
     *                              be caught and dealt with.
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, null);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum information in
     * the specified SpectrumFile with the specified drawing style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and intensities
     *                  that will be copied here. Note that mass-sorting will take place
     *                  in this step as well.
     * @param aDrawStyle    int with the drawing style to use. It should be one of the constants
     *                      defined on this class.
     * @param aEnableInteraction    boolean that specifies whether user-derived events should
     *                              be caught and dealt with.
     * @param aSpectrumFilenameColor    Color with the color for the spectrumfilename on the panel
     *                                  can be 'null' for default coloring.
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, 50, false, true, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum information in
     * the specified SpectrumFile with the specified drawing style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and intensities
     *                  that will be copied here. Note that mass-sorting will take place
     *                  in this step as well.
     * @param aDrawStyle    int with the drawing style to use. It should be one of the constants
     *                      defined on this class.
     * @param aEnableInteraction    boolean that specifies whether user-derived events should
     *                              be caught and dealt with.
     * @param aSpectrumFilenameColor    Color with the color for the spectrumfilename on the panel
     *                                  can be 'null' for default coloring.
     * @param aMaxPadding   int the sets the maximum padding size.
     * @param aHideDecimals boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aHideDecimals, boolean aShowFileName) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aHideDecimals, aShowFileName, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum information in
     * the specified SpectrumFile with the specified drawing style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and intensities
     *                  that will be copied here. Note that mass-sorting will take place
     *                  in this step as well.
     * @param aDrawStyle    int with the drawing style to use. It should be one of the constants
     *                      defined on this class.
     * @param aEnableInteraction    boolean that specifies whether user-derived events should
     *                              be caught and dealt with.
     * @param aSpectrumFilenameColor    Color with the color for the spectrumfilename on the panel
     *                                  can be 'null' for default coloring.
     * @param aMaxPadding   int the sets the maximum padding size.
     * @param aHideDecimals boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be shown in the panel
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction,
            Color aSpectrumFilenameColor, int aMaxPadding, boolean aHideDecimals,
            boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution) {
        this.iDrawStyle = aDrawStyle;
        this.iSpecPanelListeners = new ArrayList();
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setBackground(Color.WHITE);
        if (aSpecFile != null) {
            this.processSpectrumFile(aSpecFile);
        }
        if (aEnableInteraction) {
            this.addListeners();
        }
        this.iSpectrumFilenameColor = aSpectrumFilenameColor;
        this.maxPadding = aMaxPadding;
        this.hideDecimals = aHideDecimals;
        this.showFileName = aShowFileName;
        this.showPrecursorDetails = aShowPrecursorDetails;
        this.showResolution = aShowResolution;
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters. This constructor will be used to annotate matched ions on the spectrumpannels.
     * @param aMZ                   double[] with all the masses to anotate.
     * @param aIntensity            double[] with all the intensity of the peaks.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aHideDecimals boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     */
    public SpectrumPanel(double[] aMZ, double[] aIntensity, double aPrecursorMZ, String aPrecursorCharge, String aFileName,
            boolean aHideDecimals, boolean aShowFileName) {
        this(aMZ, aIntensity, aPrecursorMZ, aPrecursorCharge, aFileName, 50, aHideDecimals, aShowFileName, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters. This constructor will be used to annotate matched ions on the spectrumpannels.
     * @param aMZ                   double[] with all the masses to anotate.
     * @param aIntensity            double[] with all the intensity of the peaks.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     */
    public SpectrumPanel(double[] aMZ, double[] aIntensity, double aPrecursorMZ, String aPrecursorCharge, String aFileName) {
        this(aMZ, aIntensity, aPrecursorMZ, aPrecursorCharge, aFileName, 50, false, true, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters. This constructor will be used to annotate matched ions on the spectrumpannels.
     * @param aMZ                   double[] with all the masses to anotate.
     * @param aIntensity            double[] with all the intensity of the peaks.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aMaxPadding   int the sets the maximum padding size.
     * @param aHideDecimals boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be shown in the panel
     */
    public SpectrumPanel(double[] aMZ, double[] aIntensity, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aHideDecimals, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution) {
        this.iDrawStyle = LINES;
        this.iSpecPanelListeners = new ArrayList();
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setBackground(Color.WHITE);
        processMzsAndIntensities(aMZ, aIntensity);
        iPrecursorMZ = aPrecursorMZ;
        iPrecursorCharge = aPrecursorCharge;
        iSpecFilename = aFileName;
        this.maxPadding = aMaxPadding;
        this.hideDecimals = aHideDecimals;
        this.showFileName = aShowFileName;
        this.showPrecursorDetails = aShowPrecursorDetails;
        this.showResolution = aShowResolution;
        this.addListeners();
    }

   /**
     * This constructor creates a SpectrumPanel based on the passed parameters. This constructor will be used to annotate matched ions on the spectrumpannels.
     * @param aMZ                   double[] with all the masses to anotate.
     * @param aIntensity            double[] with all the intensity of the peaks.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aMaxPadding   int the sets the maximum padding size.
     * @param aHideDecimals boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     */
    public SpectrumPanel(double[] aMZ, double[] aIntensity, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aHideDecimals, boolean aShowFileName) {
        this(aMZ, aIntensity, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding, aHideDecimals, aShowFileName, true, true);
    }

    /**
     * This method sets the start value of the mz axis to zero.
     */
    public void setMzAxisStartAtZero(boolean aMzAxisStartAtZero) {
        iMzAxisStartAtZero = aMzAxisStartAtZero;
    }

    /**
     * This method initializes a SpectrumPanel based on the spectrum information in
     * the specified SpectrumFile.
     *
     * @param aSpecFile SpectrumFile with the information about masses and intensities
     *                  that will be copied here. Note that mass-sorting will take place
     *                  in this step as well.
     */
    public void setSpectrumFile(SpectrumFile aSpecFile) {
        this.processSpectrumFile(aSpecFile);
    }

    /**
     * This method sets all the annotations on this instance. Passing a 'null' value for
     * the Vector will result in simply removing all annotations. Do note that this method
     * will attempt to remove duplictae annotations on a peak by deleting any annotation
     * for which the combination of annotation label and annotation m/z has been seen before!
     *
     * @param aAnnotations  Vector with SpectrumAnnotation instances.
     */
    public void setAnnotations(Vector aAnnotations) {
        this.iAnnotations = new Vector(50, 25);
        if (aAnnotations != null) {
            // Attempt to remove duplicates.
            HashSet removeDupes = new HashSet(aAnnotations.size());
            for (Iterator lIterator = aAnnotations.iterator(); lIterator.hasNext();) {
                SpectrumAnnotation annotation = (SpectrumAnnotation) lIterator.next();
                String key = annotation.getLabel() + annotation.getMZ();
                if(removeDupes.contains(key)) {
                    // Duplicate, ignore!
                } else {
                    removeDupes.add(key);
                    this.iAnnotations.add(annotation);
                }
            }
        }
    }

    /**
     * This method allows the caller to set the procentual minimal, non-inclusive intensity threshold
     * (compared to the highest peak in the spectrum) a peak must pass before being eligible for annotation.
     *
     * @param aThreshold    double with the procentual peak intensity (as compared to the highest peak
     *                      in the spectrum) cutoff threshold for annotation.
     */
    public void setAnnotationIntensityThreshold(double aThreshold) {
        this.iAnnotationIntensityThreshold = (aThreshold / 100) * iIntMax;
    }

    /**
     * This method sets the display color for the spectrumfilename on the panel.
     * Can be 'null' for default coloring.
     *
     * @param aSpectrumFilenameColor    Color to render the filename in on the panel.
     *                                  Can be 'null' for default coloring.
     */
    public void setSpectrumFilenameColor(Color aSpectrumFilenameColor) {
        iSpectrumFilenameColor = aSpectrumFilenameColor;
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
        if (iMasses != null) {
            if (iDragged && iDragXLoc > 0) {
                g.drawLine(iStartXLoc, iStartYLoc, iDragXLoc, iStartYLoc);
                g.drawLine(iStartXLoc, iStartYLoc - 2, iStartXLoc, iStartYLoc + 2);
                g.drawLine(iDragXLoc, iStartYLoc - 2, iDragXLoc, iStartYLoc + 2);
            }
            // @TODO scale.
            drawAxes(g, iMassMin, iMassMax, 2, iIntMin, iIntMax);
            drawPeaks(g);
            if (iClicked && iHighLight && iClickedIndex != iHighLightIndex) {
                // Now we should calculate the distance based on the real masses and
                // draw a line to show this.
                this.drawMeasurementLine(iClickedIndex, iHighLightIndex, g, Color.blue, 0);
            }
            if (iHighLight) {
                this.highLightPeak(iHighLightIndex, g);
                iHighLight = false;
            }
            if (iClicked) {
                this.highlightClicked(iClickedIndex, g);
            }
            // See if there is a daisychain to display.
            int liClickedSize = iClickedList.size();
            if (liClickedSize > 0) {
                for (int i = 0; i < liClickedSize; i++) {
                    // The last one should be connected to iClicked.
                    int first = ((Integer) iClickedList.get(i)).intValue();
                    int second = -1;
                    if ((i + 1) == liClickedSize) {
                        second = iClickedIndex;
                    } else {
                        second = ((Integer) iClickedList.get(i + 1)).intValue();
                    }
                    this.drawMeasurementLine(first, second, g, Color.black, 0);
                }
            }
            // See if there is a secondary daisychain to display.
            if (iStoredSequence.size() > 0) {
                for (int i = 1; i < iStoredSequence.size(); i++) {
                    int first = ((Integer) iStoredSequence.get(i - 1)).intValue();
                    int second = ((Integer) iStoredSequence.get(i)).intValue();
                    this.drawMeasurementLine(first, second, g, Color.red, g.getFontMetrics().getAscent() + 15);
                }
            }
            // See if we should annotate and if any are present.
            if (iAnnotations != null && iAnnotations.size() > 0) {
                // This HashMap will contain the indices of the peaks that already carry
                // an annotation as keys, and the number of annotations as values.
                HashMap annotatedPeaks = new HashMap();
                for (int i = 0; i < iAnnotations.size(); i++) {
                    Object o = iAnnotations.get(i);
                    if (o instanceof SpectrumAnnotation) {
                        SpectrumAnnotation sa = (SpectrumAnnotation) o;
                        this.annotate(sa, g, annotatedPeaks);
                    }
                }
            }
        }
    }

    /**
     * This method reports on the largest m/z in the peak collection.
     *
     * @return double with the largest m/z in the peak collection.
     */
    public double getMaxMass() {
        return iMasses[iMasses.length - 1];
    }

    /**
     * This method reports on the smallest m/z in the peak collection.
     *
     * @return double with the smallest m/z in the peak collection.
     */
    public double getMinMass() {
        return iMasses[0];
    }

    /**
     * This method registers the specified SpectrumPanelListener with this instance
     * and notifies it of all future events. The Listeners will be notified in
     * order of addition (first addition is notified first).
     *
     * @param aListener SpectrumPanelListener to register on this instance.
     */
    public void addSpectrumPanelListener(SpectrumPanelListener aListener) {
        this.iSpecPanelListeners.add(aListener);
    }

    /**
     * This method prints the usage for this class and exits with the error flag raised.
     */
    private static void printUsage() {
        logger.error("\n\nUsage:\n\tSpectrumPanel <spectrumfile>\n\n");
        System.exit(1);
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
                if (iMasses != null) {
                    if (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2) {
                        if (iMzAxisStartAtZero) {
                            rescale(0.0, iMasses[iMasses.length - 1]);
                        } else {
                            rescale(iMasses[0], iMasses[iMasses.length - 1]);
                        }
                        iDragged = false;
                        repaint();
                    } else if (e.getButton() == MouseEvent.BUTTON1) {
                        iEndXLoc = e.getX();
                        iEndYLoc = e.getY();
                        int min = Math.min(iEndXLoc, iStartXLoc);
                        int max = Math.max(iEndXLoc, iStartXLoc);
                        double start = iMassMin + ((min - iXPadding) * iXScaleUnit);
                        double end = iMassMin + ((max - iXPadding) * iXScaleUnit);
                        if (iDragged) {
                            iDragged = false;
                            // Rescale.
                            if ((max - min) > iMinDrag) {
                                rescale(start, end);
                            }
                            iDragXLoc = 0;
                            iDragYLoc = 0;
                            repaint();
                        }
                    }
                }
            }

            /**
             * Invoked when the mouse has been clicked on a component.
             */
            public void mouseClicked(MouseEvent e) {
                if (iMasses != null) {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getModifiersEx() == (MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK)) {
                        iStoredSequence = new Vector(15, 5);
                        repaint();
                    } else if (e.getButton() == MouseEvent.BUTTON1 && e.getModifiersEx() == MouseEvent.CTRL_DOWN_MASK) {
                        iClicked = false;
                        iClickedList = new Vector(15, 5);
                        repaint();
                    } else if (e.getButton() == MouseEvent.BUTTON1 && e.getModifiersEx() == MouseEvent.SHIFT_DOWN_MASK) {
                        // If the clicked peak is the last one in the list of previously clicked peaks,
                        // remove it from the list!
                        if (iClickedList != null && iClickedList.size() > 0 && iHighLightIndex == iClickedIndex) {
                            // Retrieve the previously clicked index from the list and set the currently clicked
                            // one to that value.
                            iClickedIndex = ((Integer) iClickedList.get(iClickedList.size() - 1)).intValue();
                            // Remove the previously clicked index from the list.
                            iClickedList.remove(iClickedList.size() - 1);
                            // Repaint.
                            repaint();
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON1 && e.getModifiersEx() == MouseEvent.ALT_DOWN_MASK) {
                        // See if there is a clicked list and if it contains any values.
                        if (iClickedList != null && iClickedList.size() > 0) {
                            // Copy the current clickedlist into the stored sequence.
                            iStoredSequence = (Vector) iClickedList.clone();
                            iStoredSequence.add(new Integer(iClickedIndex));
                            iClicked = false;
                            // Reset the clicked list.
                            iClickedList = new Vector(15, 5);
                            repaint();
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON1) {
                        if (iClicked && iClickedIndex != iHighLightIndex) {
                            // We need the current peak to be stored in the previously clicked
                            // Vector and set the current one as clicked.
                            iClickedList.add(new Integer(iClickedIndex));
                        }
                        iClicked = true;
                        iClickedIndex = iHighLightIndex;
                        repaint();
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
                iDragYLoc = e.getY();
                // Calculate the selection point.
                int min = Math.min(iDragXLoc, iStartXLoc);
                int max = Math.max(iDragXLoc, iStartXLoc);
                repaint();
            }

            /**
             * Invoked when the mouse button has been moved on a component
             * (with no buttons no down).
             */
            public void mouseMoved(MouseEvent e) {
                if (iMasses != null && iMassInPixels != null) {
                    int x = e.getX();
                    int y = e.getY();
                    for (int i = 0; i < iMassInPixels.length; i++) {
                        int delta = iMassInPixels[i] - x;
                        if (Math.abs(delta) < iPeakDetectionTolerance) {
                            int deltaYPixels = y - iIntensityInPixels[i];
                            if (deltaYPixels < 0 && Math.abs(deltaYPixels) < (getHeight() - iIntensityInPixels[i])) {
                                iHighLight = true;
                                iHighLightIndex = i;
                                repaint();
                            }
                        } else if (delta >= iPeakDetectionTolerance) {
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
     * @param aMinMass  double with the new minimum mass to display.
     * @param aMaxMass  double with the new maximum mass to display.
     */
    public void rescale(double aMinMass, double aMaxMass) {
        this.rescale(aMinMass, aMaxMass, true);
    }

    /**
     * This method sets the color in which the m/z peaks will be rendered.
     *
     * @param aColor Color to render the m/z peaks in.
     */
    public void setSpectrumColor(Color aColor) {
        this.iSpectrumColor = aColor;
    }

    /**
     * This method rescales the X-axis, allowing the caller to specify whether the
     * observers need be notified.
     *
     * @param aMinMass  double with the new minimum mass to display.
     * @param aMaxMass  double with the new maximum mass to display.
     * @param aNotifyListeners  boolean to indicate whether the observers should be notified.
     */
    public void rescale(double aMinMass, double aMaxMass, boolean aNotifyListeners) {
        // Calculate the new max intensity.
        double maxInt = 1.0;
        for (int i = 0; i < iMasses.length; i++) {
            double lMass = iMasses[i];
            if (lMass < aMinMass) {
                continue;
            } else if (lMass > aMaxMass) {
                break;
            } else {
                if (iIntensities[i] > maxInt) {
                    maxInt = iIntensities[i];
                    iIntMaxIndex = i;
                }
            }
        }
        // Init the new params.
        double delta = aMaxMass - aMinMass;

        // Round to nearest order of 10, based on displayed delta.
        double tempOoM = (Math.log(delta)/Math.log(10))-1;
        if(tempOoM < 0) {
            tempOoM--;
        }
        int orderOfMagnitude = (int)tempOoM;
        double power = Math.pow(10, orderOfMagnitude);
        iMassMin = aMinMass - (aMinMass % power);
        iMassMax = aMaxMass + (power-(aMaxMass % power));
//@TODO just some helpful printouts for when this is refined further.
//logger.info(" - Delta: " + delta + "\tAdj. delta: " + (iMassMax-iMassMin) + "\tMinMass: " + iMassMin + "\tMaxMass: " + iMassMax + "\tScale: " + power);


        iIntMax = maxInt + (maxInt / 10);
        int liSize = iSpecPanelListeners.size();
        RescalingEvent re = new RescalingEvent(this, aMinMass, aMaxMass);
        if (aNotifyListeners) {
            for (int i = 0; i < liSize; i++) {
                ((SpectrumPanelListener) iSpecPanelListeners.get(i)).rescaled(re);
            }
        }
    }

    /**
     * This method reads the peaks and their intensities from the specified
     * SpectrumFile and stores these internally for drawing. The masses are sorted
     * in this step.
     *
     * @param aSpecFile SpectrumFile from which the peaks and intensities
     *                  will be copied.
     */
    private void processSpectrumFile(SpectrumFile aSpecFile) {
        HashMap peaks = aSpecFile.getPeaks();
        iMasses = new double[peaks.size()];
        iIntensities = new double[peaks.size()];
        iSpecFilename = aSpecFile.getFilename();
        // Maximum intensity of the peaks.
        double maxInt = 0.0;
        // TreeSets are sorted.
        TreeSet masses = new TreeSet(peaks.keySet());
        Iterator iter = masses.iterator();
        int count = 0;
        while (iter.hasNext()) {
            Double key = (Double) iter.next();
            double mass = key.doubleValue();
            double intensity = ((Double) peaks.get(key)).doubleValue();
            if (intensity > maxInt) {
                maxInt = intensity;
            }
            iMasses[count] = mass;
            iIntensities[count] = intensity;
            count++;
        }
        if (iMzAxisStartAtZero) {
            this.rescale(0.0, iMasses[iMasses.length - 1]);
        } else {
            this.rescale(iMasses[0], iMasses[iMasses.length - 1]);
        }
        this.iPrecursorMZ = aSpecFile.getPrecursorMZ();
        int liTemp = aSpecFile.getCharge();
        if (liTemp == 0) {
            iPrecursorCharge = "?";
        } else {
            iPrecursorCharge = Integer.toString(liTemp);
            iPrecursorCharge += (liTemp > 0 ? "+" : "-");
        }
    }

    /**
     * This method reads the peaks and their intensities from the specified
     * arrays and stores these internally for drawing. The masses are sorted
     * in this step.
     *
     * @param aMzs double[] with the m/z values.
     * @param aInts double[] with the corresponding intensity values.
     */
    private void processMzsAndIntensities(double[] aMzs, double[] aInts) {
        HashMap peaks = new HashMap(aMzs.length);
        iMasses = new double[aMzs.length];
        iIntensities = new double[aMzs.length];
        for (int i = 0; i < aMzs.length; i++) {
            peaks.put(new Double(aMzs[i]), new Double(aInts[i]));
        }
        // Maximum intensity of the peaks.
        double maxInt = 0.0;
        // TreeSets are sorted.
        TreeSet masses = new TreeSet(peaks.keySet());
        Iterator iter = masses.iterator();
        int count = 0;
        while (iter.hasNext()) {
            Double key = (Double) iter.next();
            double mass = key.doubleValue();
            double intensity = ((Double) peaks.get(key)).doubleValue();
            if (intensity > maxInt) {
                maxInt = intensity;
            }
            iMasses[count] = mass;
            iIntensities[count] = intensity;
            count++;
        }
        if (iMzAxisStartAtZero) {
            this.rescale(0.0, iMasses[iMasses.length - 1]);
        } else {
            this.rescale(iMasses[0], iMasses[iMasses.length - 1]);
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
        int intWidth = fm.stringWidth("Int");
        int mzWidth = fm.stringWidth("m/z");
        int minWidth = fm.stringWidth(Double.toString(aYMin));
        int maxWidth = fm.stringWidth(Double.toString(aYMax));
        int max = Math.max(Math.max(intWidth, mzWidth), Math.max(minWidth, maxWidth));
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
        g.drawString("m/z", this.getWidth() - (tempPadding - (padding / 2)), this.getHeight() - tempPadding + 4);
        // Y-axis.
        g.drawLine(tempPadding, this.getHeight() - tempPadding, tempPadding, tempPadding / 2);
        iXPadding = tempPadding;
        int yaxis = this.getHeight() - tempPadding - (tempPadding / 2);
        // Arrowhead on Y axis.
        g.fillPolygon(new int[]{tempPadding - 5, tempPadding + 5, tempPadding},
                new int[]{(tempPadding / 2) + 5, (tempPadding / 2) + 5, tempPadding / 2},
                3);
        // Y-axis label
        g.drawString("Int", tempPadding - intWidth, (tempPadding / 2) - 4);

        // Now the tags along the axes.
        this.drawXTags(g, aXMin, aXMax, aXScale, xaxis, tempPadding);
        int yTemp = yaxis;
        if (iAnnotations != null && iAnnotations.size() > 0) {
            yTemp -= 20;
        }
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
        iXUnit = aXAxisWidth / numberTimes;
        // ... as well as the scale unit.
        double delta = aMax - aMin;
        double scaleUnit = delta / numberTimes;
        iXScaleUnit = delta / aXAxisWidth;
        // Since we know the scale unit, we also know the resolution.
        // This will be displayed on the bottom line.
        String resolution = "";
        if(showResolution){
            resolution = "Resolution: " + new BigDecimal(iXScaleUnit).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }
        // Also print the precursor MZ and charge (if known, '?' otherwise).
        String precursor = "";
        if(showPrecursorDetails){
            precursor = "Precursor M/Z: " + this.iPrecursorMZ + " (" + this.iPrecursorCharge + ")";
        }
        // Finally, we also want the filename.
        String filename = "";
        if (showFileName) {
            filename = "Filename: " + iSpecFilename;
        }
        int precLength = fm.stringWidth(precursor);
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
            xAdditionForFilename = g.getFontMetrics().stringWidth(precursor) + 5;
            xAdditionForResolution = g.getFontMetrics().stringWidth(precursor) / 2;
            xDistance = aPadding;
        }
        g.drawString(precursor, xDistance, yHeight - smallFontCorrection);
        g.drawString(resolution, xDistance + xAdditionForResolution, yHeight);
        Color foreground = null;
        if (iSpectrumFilenameColor != null) {
            foreground = g.getColor();
            g.setColor(iSpectrumFilenameColor);
        }
        g.drawString(filename, xDistance + xAdditionForFilename, yHeight - smallFontCorrection);
        if (foreground != null) {
            g.setColor(foreground);
        }
        // Restore original font.
        g.setFont(oldFont);
        int labelHeight = fm.getAscent() + 5;
        // Now mark each unit.
        for (int i = 0; i < numberTimes; i++) {
            int xLoc = (iXUnit * i) + aPadding;
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
        iYUnit = aYAxisHeight / numberTimes;
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
            int yLoc = (iYUnit * i) + aPadding;
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
     * This method draws all of the peaks in the current massrange
     * on the panel.
     *
     * @param g Graphics object to draw on.
     */
    private void drawPeaks(Graphics g) {
        // Switch the color to red for the time being.
        Color originalColor = g.getColor();
        g.setColor(iSpectrumColor);

        // Cycle the masses and corresponding intensities.
        // Each peak is a line.
        // We also init an array that holds pixel coordinates for each peak.
        iMassInPixels = new int[iMasses.length];
        iIntensityInPixels = new int[iMasses.length];
        for (int i = 0; i < iMasses.length; i++) {
            double lMass = iMasses[i];
            // Only draw those masses within the ('low mass', 'high mass') window.
            if (lMass < iMassMin) {
                continue;
            } else if (lMass > iMassMax) {
                break;
            } else {
                double lIntensity = iIntensities[i];
                // Calculate pixel coordinates for mass and intensity.
                // Mass first.
                double tempDouble = (lMass - iMassMin) / iXScaleUnit;
                int temp = (int) tempDouble;
                if ((tempDouble - temp) >= 0.5) {
                    temp++;
                }
                int massPxl = temp + iXPadding;
                iMassInPixels[i] = massPxl;
                // Now intensity.
                tempDouble = (lIntensity - iIntMin) / iYScaleUnit;
                temp = (int) tempDouble;
                if ((tempDouble - temp) >= 0.5) {
                    temp++;
                }
                int intPxl = this.getHeight() - (temp + iXPadding);
                iIntensityInPixels[i] = intPxl;
                if (iDrawStyle == LINES) {
                    // Draw the line.
                    g.drawLine(massPxl, this.getHeight() - iXPadding, massPxl, intPxl);
                } else if (iDrawStyle == DOTS) {
                    // Draw the dot.
                    g.fillOval(massPxl - iDotRadius, intPxl - iDotRadius, iDotRadius * 2, iDotRadius * 2);
                }
            }
        }
        // Change the color back to its original setting.
        g.setColor(originalColor);
    }

    /**
     * This method will draw a highlighting triangle + mass on top of the marked peak.
     *
     * @param aIndex int with the index of the peak to highlight.
     * @param g Graphics object to draw the highlighting on.
     */
    private void highLightPeak(int aIndex, Graphics g) {
        this.highLight(aIndex, g, Color.blue, null, 0, true);
    }

    /**
     * This method will draw a highlighting triangle + mass on top of the clicked marked peak.
     *
     * @param aIndex int with the index of the clicked peak to highlight.
     * @param g Graphics object to draw the highlighting on.
     */
    private void highlightClicked(int aIndex, Graphics g) {
        this.highLight(aIndex, g, Color.BLACK, null, 0, true);
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
        int x = iMassInPixels[aIndex];
        int y = 0;
        if (aPixelsSpacer < 0) {
            y = iTopPadding;
        } else {
            y = iIntensityInPixels[aIndex] - aPixelsSpacer;
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
            String mass = Double.toString(iMasses[aIndex]);
            int halfWayMass = g.getFontMetrics().stringWidth(mass) / 2;
            g.drawString(mass, x - halfWayMass, y - arrowSpacer);
        }
        // If we drew above the peak, drop a dotted line.
        if (aPixelsSpacer != 0 && aShowArrow) {
            dropDottedLine(aIndex, y + 2, g);
        }
        // Restore original color.
        g.setColor(originalColor);

    }

    /**
     * This method draws a line, measuring the distance between two peaks in real mass units.
     *
     * @param aFirstIndex int with the first peak index to draw from.
     * @param aSecondIndex int with the second peak index to draw to.
     * @param g Graphics object on which to draw.
     * @param aColor Color object with the color for all the drawing.
     * @param aExtraPadding int with an optional amount of extra padding (lower on the graph
     *                      if positive, higher on the graph if negative)
     */
    private void drawMeasurementLine(int aFirstIndex, int aSecondIndex, Graphics g, Color aColor, int aExtraPadding) {
        // First get the x coordinates of the two peaks.
        int x1 = iMassInPixels[aFirstIndex];
        int x2 = iMassInPixels[aSecondIndex];
        if (x1 == 0 && x2 == 0) {
            return;
        } else if (x1 == 0) {
            if (iMasses[aFirstIndex] < iMassMin) {
                x1 = iXPadding + 1;
            } else {
                x1 = this.getWidth() - iXPadding - 1;
            }
        } else if (x2 == 0) {
            if (iMasses[aSecondIndex] < iMassMin) {
                x2 = iXPadding + 1;
            } else {
                x2 = this.getWidth() - iXPadding - 1;
            }
        }
        // Now the real mass difference as a String.
        double delta = Math.abs(iMasses[aFirstIndex] - iMasses[aSecondIndex]);
        String deltaMass = new BigDecimal(delta).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        String matches = this.findDeltaMassMatches(delta, 0.2);
        int width = g.getFontMetrics().stringWidth(deltaMass);

        // Vertical position of the bar will the position of the highest peak + a margin.
        int y = (int) (iYScaleUnit / iIntMax + (iXPadding / 2)) + aExtraPadding;

        // Draw the line, color is black.
        Color originalColor = g.getColor();
        g.setColor(aColor);
        g.drawLine(x1, y, x2, y);
        g.drawLine(x1, y - 3, x1, y + 3);
        g.drawLine(x2, y - 3, x2, y + 3);
        // Drop a dotted line down to the peaks.
        dropDottedLine(aFirstIndex, y - 3, g);
        dropDottedLine(aSecondIndex, y - 3, g);
        int xPosText = Math.min(x1, x2) + (Math.abs(x1 - x2) / 2) - (width / 2);
        g.drawString(deltaMass, xPosText, y - 5);
        if (!matches.trim().equals("")) {
            g.drawString(" (" + matches + ")", xPosText + width, y - 5);
        }
        // Return original color.
        g.setColor(originalColor);
    }

    /**
     * This method drops a dotted line from the specified total height to the
     * top of the indicated peak.
     *
     * @param aPeakIndex    int with the index of the peak to draw the dotted line for.
     * @param aTotalHeight  int with the height (in pixels) to drop the dotted line from.
     * @param g Graphics object to draw the dotted line on.
     */
    private void dropDottedLine(int aPeakIndex, int aTotalHeight, Graphics g) {
        int x = iMassInPixels[aPeakIndex];
        int y = iIntensityInPixels[aPeakIndex];

        // Draw the dotted line.
        if ((y - aTotalHeight) > 10) {
            int start = aTotalHeight;
            while (start < y) {
                g.drawLine(x, start, x, start + 2);
                start += 7;
            }
        }
    }

    /**
     * This method attempts to find a list of known mass deltas,
     * corresponding with the specified mass in the given window.
     *
     * @param aDelta
     * @param aWindow
     * @return String with the description of the matching mass delta
     *                or empty String if none was found.
     */
    private String findDeltaMassMatches(double aDelta, double aWindow) {
        StringBuffer result = new StringBuffer("");
        boolean appended = false;
        if (iKnownMassDeltas != null) {
            Iterator iter = iKnownMassDeltas.keySet().iterator();
            while (iter.hasNext()) {
                Double mass = (Double) iter.next();
                if (Math.abs(mass.doubleValue() - aDelta) < aWindow) {
                    if (appended) {
                        result.append("/");
                    } else {
                        appended = true;
                    }
                    result.append(iKnownMassDeltas.get(mass));
                }
            }
        }

        return result.toString();
    }

    /**
     * This method attempts to find the specified SpectrumAnnotation in
     * the current peak list and if so, annotates it correspondingly on the screen.
     *
     * @param aSA   SpectrumAnnotation with the annotation to find.
     * @param g Graphics instance to annotate on.
     * @param aAlReadyAnnotated HashMap with the index of a peak as key, and the number
     *                          of times it has been annotated as value (or 'null' if not
     *                          yet annotated).
     */
    private void annotate(SpectrumAnnotation aSA, Graphics g, HashMap aAlReadyAnnotated) {
        double mz = aSA.getMZ();
        double error = Math.abs(aSA.getErrorMargin());

        // Only do those that fall within the current visual range.
        if (!(mz < iMassMin || mz > iMassMax)) {
            // See if any match is to be found.
            boolean foundMatch = false;
            int peakIndex = -1;
            for (int i = 0; i < iMasses.length; i++) {
                double delta = iMasses[i] - mz;
                if (Math.abs(delta) <= error) {
                    if (!foundMatch) {
                        foundMatch = true;
                        peakIndex = i;
                    } else {
                        // Oops, we already had one...
                        // Take the one with the largest intensity.
                        if (iIntensities[i] > iIntensities[peakIndex]) {
                            peakIndex = i;
                        }
                    }
                } else if (delta > error) {
                    break;
                }
            }
            // If a match was found and it qualifies against the minimal intensity,
            // we now have a peak index so we can annotate.
            if (foundMatch && iIntensities[peakIndex] > iAnnotationIntensityThreshold) {
                //String label = aSA.getLabel() + " (" + new BigDecimal(mz-iMasses[peakIndex]).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + ")";
                String label = aSA.getLabel();
                int spacer = (int) ((iIntensities[peakIndex] - iIntMin) / iYScaleUnit) / 2;
                boolean showArrow = true;
                Integer key = new Integer(peakIndex);
                if (aAlReadyAnnotated.containsKey(key)) {
                    int count = ((Integer) aAlReadyAnnotated.get(key)).intValue();
                    spacer += count * (g.getFontMetrics().getAscent() + 2);
                    aAlReadyAnnotated.put(key, new Integer(count + 1));
                    showArrow = false;
                } else {
                    aAlReadyAnnotated.put(key, new Integer(1));
                }
                this.highLight(peakIndex, g, aSA.getColor(), label, spacer, showArrow);
            }
        }
    }
}
