/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-mei-2004
 * Time: 16:34:34
 */
package com.compomics.util.gui.spectrum;

import org.apache.log4j.Logger;
import com.compomics.util.interfaces.SpectrumFile;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.*;
import java.util.ArrayList;

/*
 * CVS information:
 *
 * $Revision: 1.9 $
 * $Date: 2009/08/17 15:15:28 $
 */
/**
 * This class presents a JPanel that will hold and display a
 * mass spectrum in centroid or profile mode.
 *
 * @author Lennart Martens
 * @author Harald Barsnes
 * @version $Id: SpectrumPanel.java,v 1.9 2009/08/17 15:15:28 lennart Exp $
 */
public class SpectrumPanel extends GraphicsPanel {

    /**
     * Class specific log4j logger for SpectrumPanel instances.
     */
    static Logger logger = Logger.getLogger(SpectrumPanel.class);
    /**
     * The color used for the peaks. Default to red.
     */
    private Color aSpectrumPeakColor = Color.RED;
    /**
     * The color used for the profile mode spectra. Defaults to pink.
     */
    private Color aSpectrumProfileModeLineColor = Color.PINK;

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
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, 50, false, true, true);
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
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aShowFileName) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aShowFileName, true, true);
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
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be shown in the panel
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aShowFileName, aShowPrecursorDetails, aShowResolution, 0);
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
     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be shown in the panel
     * @param aMSLevel  int with the ms level for the spectrum
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aShowFileName, aShowPrecursorDetails, aShowResolution, aMSLevel, false);
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

     * @param aShowFileName boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution   boolean that specifies if the resolution should be shown in the panel
     * @param aMSLevel  int with the ms level for the spectrum, set to 0 if ms level is unknown
     * @param aProfileMode boolean if set to true the spectrum will be drawn in profile mode
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction,
            Color aSpectrumFilenameColor, int aMaxPadding,
            boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution,
            int aMSLevel, boolean aProfileMode) {
        this.iDrawStyle = aDrawStyle;
        this.iSpecPanelListeners = new ArrayList();
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setBackground(Color.WHITE);
        if (aSpecFile != null) {
            dataSetCounter = 0;
            this.processSpectrumFile(aSpecFile, aSpectrumPeakColor, aSpectrumProfileModeLineColor);
        }
        if (aEnableInteraction) {
            this.addListeners();
        }
        this.iFilenameColor = aSpectrumFilenameColor;
        this.maxPadding = aMaxPadding;
        this.showFileName = aShowFileName;
        this.showPrecursorDetails = aShowPrecursorDetails;
        this.showResolution = aShowResolution;
        this.iMSLevel = aMSLevel;

        if (aProfileMode) {
            this.currentGraphicsPanelType = GraphicsPanelType.profileSpectrum;
        } else {
            this.currentGraphicsPanelType = GraphicsPanelType.centroidSpectrum;
        }
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the spectrumpannels.
     *
     * @param aXAxisData            double[] with all the x-axis values.
     * @param aYAxisData            double[] with all the y-axis values.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge, String aFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, 50, false, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters. 
     * This constructor will be used to annotate matched ions on the spectrumpannels.
     *
     * @param aXAxisData            double[] with all the x-axis values.
     * @param aYAxisData            double[] with all the y-axis values.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge, String aFileName,
            boolean aShowFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, 50, aShowFileName, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the spectrumpannels.
     *
     * @param aXAxisData            double[] with all the x-axis values.
     * @param aYAxisData            double[] with all the y-axis values.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aMaxPadding           int the sets the maximum padding size.
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aShowFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding, aShowFileName, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the spectrumpannels.
     *
     * @param aXAxisData            double[] with all the x-axis values.
     * @param aYAxisData            double[] with all the y-axis values.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aMaxPadding           int the sets the maximum padding size.
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution       boolean that specifies if the resolution should be shown in the panel
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding,
                aShowFileName, aShowPrecursorDetails, aShowResolution, 0);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the spectrumpannels.
     *
     * @param aXAxisData            double[] with all the x-axis values.
     * @param aYAxisData            double[] with all the y-axis values.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aMaxPadding           int the sets the maximum padding size.
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution       boolean that specifies if the resolution should be shown in the panel
     * @param aMSLevel              int with the ms level for the spectrum, set to 0 if ms level is unknown
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding,
                aShowFileName, aShowPrecursorDetails, aShowResolution, aMSLevel, false);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the spectrumpannels.
     *
     * @param aXAxisData            double[] with all the x-axis values.
     * @param aYAxisData            double[] with all the y-axis values.
     * @param aPrecursorMZ          double with the precursor mass.
     * @param aPrecursorCharge      String with the precursor intensity.
     * @param aFileName             String with the title of the Query.
     * @param aMaxPadding           int the sets the maximum padding size.
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution       boolean that specifies if the resolution should be shown in the panel
     * @param aMSLevel              int with the ms level for the spectrum, set to 0 if ms level is unknown
     * @param aProfileMode          boolean if set to true the spectrum will be drawn in profile mode
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel,
            boolean aProfileMode) {
        this.iDrawStyle = LINES;
        this.iSpecPanelListeners = new ArrayList();
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setBackground(Color.WHITE);
        dataSetCounter = 0;
        processXAndYData(aXAxisData, aYAxisData,
                aSpectrumPeakColor, aSpectrumProfileModeLineColor);
        iPrecursorMZ = aPrecursorMZ;
        iPrecursorCharge = aPrecursorCharge;
        iFilename = aFileName;
        this.maxPadding = aMaxPadding;
        this.showFileName = aShowFileName;
        this.showPrecursorDetails = aShowPrecursorDetails;
        this.showResolution = aShowResolution;
        this.iMSLevel = aMSLevel;

        if (aProfileMode) {
            this.currentGraphicsPanelType = GraphicsPanelType.profileSpectrum;
        } else {
            this.currentGraphicsPanelType = GraphicsPanelType.centroidSpectrum;
        }

        this.addListeners();
    }

    /**
     * Adds an additional spectrum dataset to be displayed in the same Spectrum 
     * Panel. Remember to use different colors for the different datasets.
     *
     * @param aXAxisData            double[] with all the x-axis values.
     * @param aYAxisData            double[] with all the y-axis values
     * @param dataPointAndLineColor the color to use for the data points and lines
     * @param areaUnderCurveColor   the color to use for the area under the curve
     */
    public void addAdditionalDataset(double[] aXAxisData, double[] aYAxisData, Color dataPointAndLineColor, Color areaUnderCurveColor) {

        processXAndYData(aXAxisData, aYAxisData, dataPointAndLineColor, areaUnderCurveColor);

        this.showFileName = false;
        this.showPrecursorDetails = false;
        this.showResolution = false;
    }

    /**
     * Change the drawing type of the spectrum. Profile or centroid mode.
     *
     * @param aProfileMode if true, the spectrum is drawn in profile mode
     */
    public void setProfileMode(boolean aProfileMode) {
        if (aProfileMode) {
            this.currentGraphicsPanelType = GraphicsPanelType.profileSpectrum;
        } else {
            this.currentGraphicsPanelType = GraphicsPanelType.centroidSpectrum;
        }
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
        this.processSpectrumFile(aSpecFile, aSpectrumPeakColor, aSpectrumProfileModeLineColor);
    }

    /**
     * This method reads the peaks and their intensities from the specified
     * SpectrumFile and stores these internally for drawing. The masses are sorted
     * in this step.
     *
     * @param aSpecFile SpectrumFile from which the peaks and intensities will be copied.
     * @param dataPointAndLineColor the color to use for the data points and line
     * @param areaUnderCurveColor the color to use for the area under the curve
     */
    private void processSpectrumFile(SpectrumFile aSpecFile, Color dataPointAndLineColor, Color areaUnderCurveColor) {

        if (dataSetCounter == 0) {
            iXAxisData = new ArrayList<double[]>();
            iYAxisData = new ArrayList<double[]>();
        }

        iDataPointAndLineColor.add(dataPointAndLineColor);
        iAreaUnderCurveColor.add(areaUnderCurveColor);

        HashMap peaks = aSpecFile.getPeaks();

        iXAxisData.add(new double[peaks.size()]);
        iYAxisData.add(new double[peaks.size()]);

        iFilename = aSpecFile.getFilename();

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
            iXAxisData.get(dataSetCounter)[count] = mass;
            iYAxisData.get(dataSetCounter)[count] = intensity;
            count++;
        }

        if (iXAxisStartAtZero) {
            this.rescale(0.0, getMaxXAxisValue());
        } else {
            this.rescale(getMinXAxisValue(), getMaxXAxisValue());
        }

        this.iPrecursorMZ = aSpecFile.getPrecursorMZ();
        int liTemp = aSpecFile.getCharge();

        if (liTemp == 0) {
            iPrecursorCharge = "?";
        } else {
            iPrecursorCharge = Integer.toString(liTemp);
            iPrecursorCharge += (liTemp > 0 ? "+" : "-");
        }

        dataSetCounter++;
    }

    /**
     * Returns the peak color to be used for the given peak label. The
     * colors used are based on the color coding used in MascotDatfile.
     *
     * @param peakLabel
     * @return the peak color
     */
    public static Color determineColorOfPeak(String peakLabel) {

        Color currentColor = Color.GRAY;

        if (peakLabel.startsWith("a")) {

            // turquoise
            currentColor = new Color(153, 0, 0);

            if (peakLabel.lastIndexOf("H2O") != -1 || peakLabel.lastIndexOf("H20") != -1) {
                // light purple-blue
                currentColor = new Color(171, 161, 255);
            } else if (peakLabel.lastIndexOf("NH3") != -1) {
                // ugly purple pink
                currentColor = new Color(248, 151, 202);
            }

        } else if (peakLabel.startsWith("b")) {

            // dark blue
            currentColor = new Color(0, 0, 255);

            if (peakLabel.lastIndexOf("H2O") != -1 || peakLabel.lastIndexOf("H20") != -1) {
                // nice blue
                currentColor = new Color(0, 125, 200);
            } else if (peakLabel.lastIndexOf("NH3") != -1) {
                // another purple
                currentColor = new Color(153, 0, 255);
            }

        } else if (peakLabel.startsWith("c")) {

            // purple blue
            currentColor = new Color(188, 0, 255); // ToDo: no colors for H2O and NH3??

        } else if (peakLabel.startsWith("x")) {

            // green
            currentColor = new Color(78, 200, 0); // ToDo: no colors for H2O and NH3??

        } else if (peakLabel.startsWith("y")) {

            // black
            currentColor = new Color(0, 0, 0);

            if (peakLabel.lastIndexOf("H2O") != -1 || peakLabel.lastIndexOf("H20") != -1) {
                // navy blue
                currentColor = new Color(0, 70, 135);
            } else if (peakLabel.lastIndexOf("NH3") != -1) {
                // another purple
                currentColor = new Color(155, 0, 155);
            }

        } else if (peakLabel.startsWith("z")) {

            // dark green
            currentColor = new Color(64, 179, 0); // ToDo: no colors for H2O and NH3??

        } else if (peakLabel.startsWith("Prec") || peakLabel.startsWith("MH")) { // precursor

            // red
            currentColor = Color.gray; // Color.red is used in MascotDatFile

        } else if (peakLabel.startsWith("i")) { // immonimum ion
            // grey
            currentColor = Color.gray;
        }

        return currentColor;
    }
}
