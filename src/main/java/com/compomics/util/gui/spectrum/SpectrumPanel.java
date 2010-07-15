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

    // Class specific log4j logger for SpectrumPanel instances.
    static Logger logger = Logger.getLogger(SpectrumPanel.class);
    
    private Color aSpectrumPeakColor = Color.RED;
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
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aHideDecimals, boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aHideDecimals, aShowFileName, aShowPrecursorDetails, aShowResolution, 0);
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
     * @param aMSLevel  int with the ms level for the spectrum
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aHideDecimals, boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aHideDecimals, aShowFileName, aShowPrecursorDetails, aShowResolution, aMSLevel, false);
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
     * @param aShowResolution   boolean that specifies if the resolution should be shown in the panel
     * @param aMSLevel  int with the ms level for the spectrum, set to 0 if ms level is unknown
     * @param aProfileMode boolean if set to true the spectrum will be drawn in profile mode
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction,
            Color aSpectrumFilenameColor, int aMaxPadding, boolean aHideDecimals,
            boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution,
            int aMSLevel, boolean aProfileMode) {
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
        this.iFilenameColor = aSpectrumFilenameColor;
        this.maxPadding = aMaxPadding;
        this.hideDecimals = aHideDecimals;
        this.showFileName = aShowFileName;
        this.showPrecursorDetails = aShowPrecursorDetails;
        this.showResolution = aShowResolution;
        this.iMSLevel = aMSLevel;

        if(aProfileMode){
            this.currentGraphicsPanelType = GraphicsPanelType.profileSpectrum;
        } else {
            this.currentGraphicsPanelType = GraphicsPanelType.centroidSpectrum;
        }

        
        this.iDataPointColor = aSpectrumPeakColor;
        this.iChromatogramLineColor = aSpectrumProfileModeLineColor;
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
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, 50, false, true, true, true);
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
     * @param aHideDecimals         boolean that specifies if the decimals for the axis tags should be shown.
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge, String aFileName,
            boolean aHideDecimals, boolean aShowFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, 50, aHideDecimals, aShowFileName, true, true);
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
     * @param aHideDecimals         boolean that specifies if the decimals for the axis tags should be shown.
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aHideDecimals, boolean aShowFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding, aHideDecimals, aShowFileName, true, true);
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
     * @param aHideDecimals         boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution       boolean that specifies if the resolution should be shown in the panel
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aHideDecimals, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding, aHideDecimals,
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
     * @param aHideDecimals         boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution       boolean that specifies if the resolution should be shown in the panel
     * @param aMSLevel              int with the ms level for the spectrum, set to 0 if ms level is unknown
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aHideDecimals, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding, aHideDecimals,
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
     * @param aHideDecimals         boolean that specifies if the decimals for the axis tags should be shown
     * @param aShowFileName         boolean that specifies if the file name should be shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor details should be shown in the panel
     * @param aShowResolution       boolean that specifies if the resolution should be shown in the panel
     * @param aMSLevel              int with the ms level for the spectrum, set to 0 if ms level is unknown
     * @param aProfileMode          boolean if set to true the spectrum will be drawn in profile mode
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aHideDecimals, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel,
            boolean aProfileMode) {
        this.iDrawStyle = LINES;
        this.iSpecPanelListeners = new ArrayList();
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setBackground(Color.WHITE);
        processXAndYData(aXAxisData, aYAxisData);
        iPrecursorMZ = aPrecursorMZ;
        iPrecursorCharge = aPrecursorCharge;
        iFilename = aFileName;
        this.maxPadding = aMaxPadding;
        this.hideDecimals = aHideDecimals;
        this.showFileName = aShowFileName;
        this.showPrecursorDetails = aShowPrecursorDetails;
        this.showResolution = aShowResolution;
        this.iMSLevel = aMSLevel;

        if(aProfileMode){
            this.currentGraphicsPanelType = GraphicsPanelType.profileSpectrum;
        } else {
            this.currentGraphicsPanelType = GraphicsPanelType.centroidSpectrum;
        }

        this.iDataPointColor = aSpectrumPeakColor;
        this.iChromatogramLineColor = aSpectrumProfileModeLineColor;

        this.addListeners();
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
     * This method reads the peaks and their intensities from the specified
     * SpectrumFile and stores these internally for drawing. The masses are sorted
     * in this step.
     *
     * @param aSpecFile SpectrumFile from which the peaks and intensities will be copied.
     */
    private void processSpectrumFile(SpectrumFile aSpecFile) {
        HashMap peaks = aSpecFile.getPeaks();
        iXAxisData = new double[peaks.size()];
        iYAxisData = new double[peaks.size()];
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
            iXAxisData[count] = mass;
            iYAxisData[count] = intensity;
            count++;
        }
        if (iXAxisStartAtZero) {
            this.rescale(0.0, iXAxisData[iXAxisData.length - 1]);
        } else {
            this.rescale(iXAxisData[0], iXAxisData[iXAxisData.length - 1]);
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
}
