/**
 * Created by IntelliJ IDEA. User: Lennart Date: 11-mei-2004 Time: 16:34:34
 */
package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
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
 * $Revision: 1.9 $ $Date: 2009/08/17 15:15:28 $
 */
/**
 * This class presents a JPanel that will hold and display a mass spectrum in
 * centroid or profile mode.
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
     * Color map for the ion annotation.
     */
    private static HashMap<Ion.IonType, HashMap<Integer, HashMap<String, Color>>> colorMap = new HashMap<Ion.IonType, HashMap<Integer, HashMap<String, Color>>>();

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile as an interactive lines plot.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     */
    public SpectrumPanel(SpectrumFile aSpecFile) {
        this(aSpecFile, LINES, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile as a line plot.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     * @param aEnableInteraction boolean that specifies whether user-derived
     * events should be caught and dealt with.
     */
    public SpectrumPanel(SpectrumFile aSpecFile, boolean aEnableInteraction) {
        this(aSpecFile, LINES, aEnableInteraction);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile with the specified drawing
     * style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     * @param aDrawStyle int with the drawing style to use. It should be one of
     * the constants defined on this class.
     * @param aEnableInteraction boolean that specifies whether user-derived
     * events should be caught and dealt with.
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, null);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile with the specified drawing
     * style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     * @param aDrawStyle int with the drawing style to use. It should be one of
     * the constants defined on this class.
     * @param aEnableInteraction boolean that specifies whether user-derived
     * events should be caught and dealt with.
     * @param aSpectrumFilenameColor Color with the color for the
     * spectrumfilename on the panel can be 'null' for default coloring.
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, 50, false, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile with the specified drawing
     * style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     * @param aDrawStyle int with the drawing style to use. It should be one of
     * the constants defined on this class.
     * @param aEnableInteraction boolean that specifies whether user-derived
     * events should be caught and dealt with.
     * @param aSpectrumFilenameColor Color with the color for the
     * spectrumfilename on the panel can be 'null' for default coloring.
     * @param aMaxPadding int the sets the maximum padding size.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aShowFileName) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aShowFileName, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile with the specified drawing
     * style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     * @param aDrawStyle int with the drawing style to use. It should be one of
     * the constants defined on this class.
     * @param aEnableInteraction boolean that specifies whether user-derived
     * events should be caught and dealt with.
     * @param aSpectrumFilenameColor Color with the color for the
     * spectrumfilename on the panel can be 'null' for default coloring.
     * @param aMaxPadding int the sets the maximum padding size.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor
     * details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be
     * shown in the panel
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aShowFileName, aShowPrecursorDetails, aShowResolution, 0);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile with the specified drawing
     * style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     * @param aDrawStyle int with the drawing style to use. It should be one of
     * the constants defined on this class.
     * @param aEnableInteraction boolean that specifies whether user-derived
     * events should be caught and dealt with.
     * @param aSpectrumFilenameColor Color with the color for the
     * spectrumfilename on the panel can be 'null' for default coloring.
     * @param aMaxPadding int the sets the maximum padding size.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor
     * details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be
     * shown in the panel
     * @param aMSLevel int with the ms level for the spectrum
     */
    public SpectrumPanel(SpectrumFile aSpecFile, int aDrawStyle, boolean aEnableInteraction, Color aSpectrumFilenameColor,
            int aMaxPadding, boolean aShowFileName, boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel) {
        this(aSpecFile, aDrawStyle, aEnableInteraction, aSpectrumFilenameColor, aMaxPadding, aShowFileName, aShowPrecursorDetails, aShowResolution, aMSLevel, false);
    }

    /**
     * This constructor creates a SpectrumPanel based on the spectrum
     * information in the specified SpectrumFile with the specified drawing
     * style.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     * @param aDrawStyle int with the drawing style to use. It should be one of
     * the constants defined on this class.
     * @param aEnableInteraction boolean that specifies whether user-derived
     * events should be caught and dealt with.
     * @param aSpectrumFilenameColor Color with the color for the
     * spectrumfilename on the panel can be 'null' for default coloring.
     * @param aMaxPadding int the sets the maximum padding size.
     *
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor
     * details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be
     * shown in the panel
     * @param aMSLevel int with the ms level for the spectrum, set to 0 if ms
     * level is unknown
     * @param aProfileMode boolean if set to true the spectrum will be drawn in
     * profile mode
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
     * This constructor will be used to annotate matched ions on the
     * spectrumpannels.
     *
     * @param aXAxisData double[] with all the x-axis values.
     * @param aYAxisData double[] with all the y-axis values.
     * @param aPrecursorMZ double with the precursor mass.
     * @param aPrecursorCharge String with the precursor intensity.
     * @param aFileName String with the title of the Query.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge, String aFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, 50, false, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the
     * spectrumpannels.
     *
     * @param aXAxisData double[] with all the x-axis values.
     * @param aYAxisData double[] with all the y-axis values.
     * @param aPrecursorMZ double with the precursor mass.
     * @param aPrecursorCharge String with the precursor intensity.
     * @param aFileName String with the title of the Query.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge, String aFileName,
            boolean aShowFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, 50, aShowFileName, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the
     * spectrumpannels.
     *
     * @param aXAxisData double[] with all the x-axis values.
     * @param aYAxisData double[] with all the y-axis values.
     * @param aPrecursorMZ double with the precursor mass.
     * @param aPrecursorCharge String with the precursor intensity.
     * @param aFileName String with the title of the Query.
     * @param aMaxPadding int the sets the maximum padding size.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel.
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aShowFileName) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding, aShowFileName, true, true);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the
     * spectrumpannels.
     *
     * @param aXAxisData double[] with all the x-axis values.
     * @param aYAxisData double[] with all the y-axis values.
     * @param aPrecursorMZ double with the precursor mass.
     * @param aPrecursorCharge String with the precursor intensity.
     * @param aFileName String with the title of the Query.
     * @param aMaxPadding int the sets the maximum padding size.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor
     * details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be
     * shown in the panel
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding,
                aShowFileName, aShowPrecursorDetails, aShowResolution, 0);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the
     * spectrumpannels.
     *
     * @param aXAxisData double[] with all the x-axis values.
     * @param aYAxisData double[] with all the y-axis values.
     * @param aPrecursorMZ double with the precursor mass.
     * @param aPrecursorCharge String with the precursor intensity.
     * @param aFileName String with the title of the Query.
     * @param aMaxPadding int the sets the maximum padding size.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor
     * details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be
     * shown in the panel
     * @param aMSLevel int with the ms level for the spectrum, set to 0 if ms
     * level is unknown
     */
    public SpectrumPanel(double[] aXAxisData, double[] aYAxisData, double aPrecursorMZ, String aPrecursorCharge,
            String aFileName, int aMaxPadding, boolean aShowFileName,
            boolean aShowPrecursorDetails, boolean aShowResolution, int aMSLevel) {
        this(aXAxisData, aYAxisData, aPrecursorMZ, aPrecursorCharge, aFileName, aMaxPadding,
                aShowFileName, aShowPrecursorDetails, aShowResolution, aMSLevel, false);
    }

    /**
     * This constructor creates a SpectrumPanel based on the passed parameters.
     * This constructor will be used to annotate matched ions on the
     * spectrumpannels.
     *
     * @param aXAxisData double[] with all the x-axis values.
     * @param aYAxisData double[] with all the y-axis values.
     * @param aPrecursorMZ double with the precursor mass.
     * @param aPrecursorCharge String with the precursor charge.
     * @param aFileName String with the title of the Query.
     * @param aMaxPadding int the sets the maximum padding size.
     * @param aShowFileName boolean that specifies if the file name should be
     * shown in the panel
     * @param aShowPrecursorDetails boolean that specifies if the precursor
     * details should be shown in the panel
     * @param aShowResolution boolean that specifies if the resolution should be
     * shown in the panel
     * @param aMSLevel int with the ms level for the spectrum, set to 0 if ms
     * level is unknown
     * @param aProfileMode boolean if set to true the spectrum will be drawn in
     * profile mode
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
     * @param aXAxisData double[] with all the x-axis values.
     * @param aYAxisData double[] with all the y-axis values
     * @param dataPointAndLineColor the color to use for the data points and
     * lines
     * @param areaUnderCurveColor the color to use for the area under the curve
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
     * If true only the annotated peaks will be drawn. The default value is
     * false, and result in all peaks being drawn. Note that this setting is
     * ignored when in profile mode!
     *
     * @param aAnnotatedPeaks if true only the annotated peaks will be drawn
     */
    public void showAnnotatedPeaksOnly(boolean aAnnotatedPeaks) {
        this.showAllPeaks = !aAnnotatedPeaks;
    }

    /**
     * This method initializes a SpectrumPanel based on the spectrum information
     * in the specified SpectrumFile.
     *
     * @param aSpecFile SpectrumFile with the information about masses and
     * intensities that will be copied here. Note that mass-sorting will take
     * place in this step as well.
     */
    public void setSpectrumFile(SpectrumFile aSpecFile) {
        this.processSpectrumFile(aSpecFile, aSpectrumPeakColor, aSpectrumProfileModeLineColor);
    }

    /**
     * This method reads the peaks and their intensities from the specified
     * SpectrumFile and stores these internally for drawing. The masses are
     * sorted in this step.
     *
     * @param aSpecFile SpectrumFile from which the peaks and intensities will
     * be copied.
     * @param dataPointAndLineColor the color to use for the data points and
     * line
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
     * Returns the peak color to be used for the given peak label. The colors
     * used are based on the color coding used in MascotDatfile.
     *
     * @deprecated it is advised to use methods based on the ion type rather
     * than on the peak label
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

    /**
     * Filters the annotations and returns the annotations matching the
     * currently selected types.
     *
     * @deprecated don't use method based on the peak labels but on the data
     * type
     * @param annotations the annotations to be filtered, the annotations are
     * assumed to have the following form: ion type + [ion number] + [charge] +
     * [neutral loss]
     * @param iontypes the fragment ion types to include, assumed to be one of
     * the Ion types, e.g, IonType.PeptideFragmentIon > PeptideFragmentIon.B_ION
     * @param neutralLosses list of neutral losses to display
     * @param singleChargeSelected if singly charged fragments are to be
     * included
     * @param doubleChargeSelected if double charged fragments are to be
     * included
     * @param moreThanTwoChargesSelected if fragments with more than two charges
     * are to be included
     * @return the filtered annotations
     */
    public static Vector<DefaultSpectrumAnnotation> filterAnnotations(
            Vector<DefaultSpectrumAnnotation> annotations,
            HashMap<Ion.IonType, ArrayList<Integer>> iontypes,
            ArrayList<NeutralLoss> neutralLosses,
            boolean singleChargeSelected,
            boolean doubleChargeSelected,
            boolean moreThanTwoChargesSelected) {

        Vector<DefaultSpectrumAnnotation> filteredAnnotations = new Vector();

        for (int i = 0; i < annotations.size(); i++) {

            String currentLabel = annotations.get(i).getLabel();

            boolean useAnnotation = false;

            // check ion type
            if (currentLabel.startsWith("a")) {
                if (iontypes.containsKey(Ion.IonType.PEPTIDE_FRAGMENT_ION)
                        && iontypes.get(Ion.IonType.PEPTIDE_FRAGMENT_ION).contains(PeptideFragmentIon.A_ION)) {
                    useAnnotation = true;
                }
            } else if (currentLabel.startsWith("b")) {
                if (iontypes.containsKey(Ion.IonType.PEPTIDE_FRAGMENT_ION)
                        && iontypes.get(Ion.IonType.PEPTIDE_FRAGMENT_ION).contains(PeptideFragmentIon.B_ION)) {
                    useAnnotation = true;
                }
            } else if (currentLabel.startsWith("c")) {
                if (iontypes.containsKey(Ion.IonType.PEPTIDE_FRAGMENT_ION)
                        && iontypes.get(Ion.IonType.PEPTIDE_FRAGMENT_ION).contains(PeptideFragmentIon.C_ION)) {
                    useAnnotation = true;
                }
            } else if (currentLabel.startsWith("x")) {
                if (iontypes.containsKey(Ion.IonType.PEPTIDE_FRAGMENT_ION)
                        && iontypes.get(Ion.IonType.PEPTIDE_FRAGMENT_ION).contains(PeptideFragmentIon.X_ION)) {
                    useAnnotation = true;
                }
            } else if (currentLabel.startsWith("y")) {
                if (iontypes.containsKey(Ion.IonType.PEPTIDE_FRAGMENT_ION)
                        && iontypes.get(Ion.IonType.PEPTIDE_FRAGMENT_ION).contains(PeptideFragmentIon.Y_ION)) {
                    useAnnotation = true;
                }
            } else if (currentLabel.startsWith("z")) {
                if (iontypes.containsKey(Ion.IonType.PEPTIDE_FRAGMENT_ION)
                        && iontypes.get(Ion.IonType.PEPTIDE_FRAGMENT_ION).contains(PeptideFragmentIon.Z_ION)) {
                    useAnnotation = true;
                }
            } else { // other
                if (iontypes.containsKey(Ion.IonType.IMMONIUM_ION)
                        || iontypes.containsKey(Ion.IonType.PRECURSOR_ION)
                        || iontypes.containsKey(Ion.IonType.IMMONIUM_ION)) {
                    useAnnotation = true;
                }
            }

            // check neutral losses
            if (useAnnotation) {
                boolean h2oLossSelected = false;
                boolean nh3LossSelected = false;
                boolean phosphoLossSelected = false;
                boolean moxLossSelected = false;

                for (NeutralLoss neutralLoss : neutralLosses) {
                    if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                        h2oLossSelected = true;
                    } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                        nh3LossSelected = true;
                    } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)
                            || neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                        phosphoLossSelected = true;
                    } else if (neutralLoss.isSameAs(NeutralLoss.CH4OS)) {
                        moxLossSelected = true;
                    }
                }
                if (currentLabel.lastIndexOf("-H2O") != -1 || currentLabel.lastIndexOf("-H20") != -1) {
                    if (!h2oLossSelected) {
                        useAnnotation = false;
                    }
                }

                if (currentLabel.lastIndexOf("-NH3") != -1) {
                    if (!nh3LossSelected) {
                        useAnnotation = false;
                    }
                }
                if (currentLabel.lastIndexOf("-H3PO4") != -1
                        || currentLabel.lastIndexOf("-HPO3") != -1) {
                    if (!phosphoLossSelected) {
                        useAnnotation = false;
                    }
                }
                if (currentLabel.lastIndexOf("-CH4OS") != -1) {
                    if (!moxLossSelected) {
                        useAnnotation = false;
                    }
                }
            }

            // check ion charge
            if (useAnnotation) {
                if (currentLabel.lastIndexOf("+") == -1) {

                    // test needed to be able to show ions in the "other" group
                    if (currentLabel.startsWith("a") || currentLabel.startsWith("b") || currentLabel.startsWith("c")
                            || currentLabel.startsWith("x") || currentLabel.startsWith("y") || currentLabel.startsWith("z")) {
                        if (!singleChargeSelected) {
                            useAnnotation = false;
                        }
                    }
                } else if (currentLabel.lastIndexOf("+++") != -1) {
                    if (!moreThanTwoChargesSelected) {
                        useAnnotation = false;
                    }
                } else if (currentLabel.lastIndexOf("++") != -1) {
                    if (!doubleChargeSelected) {
                        useAnnotation = false;
                    }
                }
            }

            if (useAnnotation) {
                filteredAnnotations.add(annotations.get(i));
            }
        }

        return filteredAnnotations;
    }

    /**
     * Sets an annotation color for the given ion.
     *
     * @param ion the ion
     * @param color the new color
     */
    public static void setIonColor(Ion ion, Color color) {
        if (!colorMap.containsKey(ion.getType())) {
            colorMap.put(ion.getType(), new HashMap<Integer, HashMap<String, Color>>());
        }
        if (!colorMap.get(ion.getType()).containsKey(ion.getSubType())) {
            colorMap.get(ion.getType()).put(ion.getSubType(), new HashMap<String, Color>());
        }
        colorMap.get(ion.getType()).get(ion.getSubType()).put(ion.getNeutralLossesAsString(), color);
    }

    /**
     * Returns the peak color to be used for the given peak label according to
     * the color map. If not implemented returns the default color.
     *
     * @param ion the ion
     * @param isSpectrum if true, the special spectrum color is used for the
     * y-ion
     * @return the peak color
     */
    public static Color determineFragmentIonColor(Ion ion, boolean isSpectrum) {
        if (colorMap.containsKey(ion.getType())
                && colorMap.get(ion.getType()).containsKey(ion.getSubType())
                && colorMap.get(ion.getType()).get(ion.getSubType()).containsKey(ion.getNeutralLossesAsString())) {
            return colorMap.get(ion.getType()).get(ion.getSubType()).get(ion.getNeutralLossesAsString());
        }
        return determineDefaultFragmentIonColor(ion, isSpectrum);
    }

    /**
     * Returns the peak color to be used for the given peak label. The colors
     * used are based on the color coding used in MascotDatfile.
     *
     * @param ion the ion
     * @param isSpectrum if true, the special spectrum color is used for the
     * y-ion
     * @return the peak color
     */
    public static Color determineDefaultFragmentIonColor(Ion ion, boolean isSpectrum) {

        switch (ion.getType()) {
            case PEPTIDE_FRAGMENT_ION:
                switch (ion.getSubType()) {
                    case PeptideFragmentIon.A_ION:
                        if (ion.getNeutralLosses().size() == 1) {
                            NeutralLoss neutralLoss = ion.getNeutralLosses().get(0);
                            if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                                // light purple-blue
                                return new Color(171, 161, 255);
                            } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                                // ugly purple pink
                                return new Color(248, 151, 202);
                            } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)
                                    || neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                                return Color.BLACK; // @TODO: black can _not_ be used here!!
                            }
                        } else if (ion.getNeutralLosses().size() > 1) {
                            return Color.GRAY;
                        }
                        // turquoise
                        return new Color(153, 0, 0);
                    case PeptideFragmentIon.B_ION:
                        if (ion.getNeutralLosses().size() == 1) {
                            NeutralLoss neutralLoss = ion.getNeutralLosses().get(0);
                            if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                                // nice blue
                                return new Color(0, 125, 200);
                            } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                                // another purple
                                return new Color(153, 0, 255);
                            } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)
                                    || neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                                return Color.BLACK; // @TODO: black can _not_ be used here!!
                            }
                        } else if (ion.getNeutralLosses().size() > 1) {
                            return Color.GRAY;
                        }
                        // dark blue
                        return new Color(0, 0, 255);
                    case PeptideFragmentIon.C_ION:
                        if (ion.getNeutralLosses().size() == 1) {
                            NeutralLoss neutralLoss = ion.getNeutralLosses().get(0);
                            if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                                // ??
                                return new Color(188, 150, 255);
                            } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                                // ??
                                return new Color(255, 0, 255);
                            } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)
                                    || neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                                return Color.BLACK; // @TODO: black can _not_ be used here!!
                            }
                        } else if (ion.getNeutralLosses().size() > 1) {
                            return Color.GRAY;
                        }
                        // purple blue
                        return new Color(188, 0, 255);
                    case PeptideFragmentIon.X_ION:
                        if (ion.getNeutralLosses().size() == 1) {
                            NeutralLoss neutralLoss = ion.getNeutralLosses().get(0);
                            if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                                // ??
                                return new Color(78, 200, 150);
                            } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                                // ??
                                return new Color(255, 200, 255);
                            } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)
                                    || neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                                return Color.BLACK; // @TODO: black can _not_ be used here!!
                            }
                        } else if (ion.getNeutralLosses().size() > 1) {
                            return Color.GRAY;
                        }
                        // green
                        return new Color(78, 200, 0);
                    case PeptideFragmentIon.Y_ION:
                        if (ion.getNeutralLosses().size() == 1) {
                            NeutralLoss neutralLoss = ion.getNeutralLosses().get(0);
                            if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                                if (isSpectrum) {
                                    // navy blue
                                    return new Color(0, 70, 135);
                                } else {
                                    // orange
                                    return new Color(255, 150, 0);
                                }
                            } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                                if (isSpectrum) {
                                    // another purple
                                    return new Color(155, 0, 155);
                                } else {
                                    // pink
                                    return new Color(255, 0, 150);
                                }
                            } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)
                                    || neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                                return Color.BLACK; // @TODO: black can _not_ be used here!!
                            }
                        } else if (ion.getNeutralLosses().size() > 1) {
                            return Color.GRAY;
                        }

                        if (isSpectrum) {
                            // black
                            return Color.BLACK; // special case for spectra, as the default peak color is red...
                        } else {
                            // red
                            return new Color(255, 0, 0);
                        }
                    case PeptideFragmentIon.Z_ION:
                        if (ion.getNeutralLosses().size() == 1) {
                            NeutralLoss neutralLoss = ion.getNeutralLosses().get(0);
                            if (neutralLoss.isSameAs(NeutralLoss.H2O)) {
                                // ??
                                return new Color(64, 179, 150);
                            } else if (neutralLoss.isSameAs(NeutralLoss.NH3)) {
                                // ??
                                return new Color(255, 179, 150);
                            } else if (neutralLoss.isSameAs(NeutralLoss.H3PO4)
                                    || neutralLoss.isSameAs(NeutralLoss.HPO3)) {
                                return Color.BLACK; // @TODO: black can _not_ be used here!!
                            }
                        } else if (ion.getNeutralLosses().size() > 1) {
                            return Color.GRAY;
                        }
                        // dark green
                        return new Color(64, 179, 0);
                    default:
                        return Color.GRAY;
                }
            case PRECURSOR_ION:
                return Color.GRAY;
            case IMMONIUM_ION:
                return Color.GRAY;
            case REPORTER_ION:
                return Color.ORANGE;
            default:
                return Color.GRAY;
        }
    }

    /**
     * Returns the color to use for the given fragment ion label.
     *
     * @deprecated use the method based on the Ion class instead
     * @param seriesLabel the series label
     * @return the fragment ion color
     */
    public static Color determineFragmentIonColor(String seriesLabel) {

        Color currentColor = Color.GRAY;

        if (seriesLabel.startsWith("a")) {

            // turquoise
            currentColor = new Color(153, 0, 0);

            if (seriesLabel.lastIndexOf("H2O") != -1 || seriesLabel.lastIndexOf("H20") != -1) {
                // light purple-blue
                currentColor = new Color(171, 161, 255);
            } else if (seriesLabel.lastIndexOf("NH3") != -1) {
                // ugly purple pink
                currentColor = new Color(248, 151, 202);
            }

            // change color slightly if a double charge is detected
            if (seriesLabel.lastIndexOf("++") != -1) {
                currentColor = new Color(currentColor.getRed() - 100, currentColor.getGreen(), currentColor.getBlue());
            }

        } else if (seriesLabel.startsWith("b")) {

            // dark blue
            currentColor = new Color(0, 0, 255);

            // change color slightly if a neutral loss is detected
            if (seriesLabel.lastIndexOf("H2O") != -1 || seriesLabel.lastIndexOf("H20") != -1) {
                currentColor = new Color(0, 150, 255);
            } else if (seriesLabel.lastIndexOf("NH3") != -1 || seriesLabel.equalsIgnoreCase("b ions - mod.")) {
                currentColor = new Color(150, 0, 255);
            }

            // change color slightly if a double charge is detected
            if (seriesLabel.lastIndexOf("++") != -1) {
                currentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue() - 100);
            }

        } else if (seriesLabel.startsWith("c")) {

            // purple blue
            currentColor = new Color(188, 0, 255);

            // change color slightly if a neutral loss is detected
            if (seriesLabel.lastIndexOf("H2O") != -1 || seriesLabel.lastIndexOf("H20") != -1) {
                currentColor = new Color(188, 150, 255);
            } else if (seriesLabel.lastIndexOf("NH3") != -1) {
                currentColor = new Color(255, 0, 255);
            }

            // change color slightly if a double charge is detected
            if (seriesLabel.lastIndexOf("++") != -1) {
                currentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue() - 100);
            }

        } else if (seriesLabel.startsWith("x")) {

            // green
            currentColor = new Color(78, 200, 0);

            // change color slightly if a neutral loss is detected
            if (seriesLabel.lastIndexOf("H2O") != -1 || seriesLabel.lastIndexOf("H20") != -1) {
                currentColor = new Color(78, 200, 150);
            } else if (seriesLabel.lastIndexOf("NH3") != -1) {
                currentColor = new Color(255, 200, 255);
            }

            // change color slightly if a double charge is detected
            if (seriesLabel.lastIndexOf("++") != -1) {
                currentColor = new Color(currentColor.getRed(), currentColor.getGreen() - 100, currentColor.getBlue());
            }

        } else if (seriesLabel.startsWith("y")) {

            // red
            currentColor = new Color(255, 0, 0);

            // change color slightly if a neutral loss is detected
            if (seriesLabel.lastIndexOf("H2O") != -1 || seriesLabel.lastIndexOf("H20") != -1) {
                currentColor = new Color(255, 150, 0);
            } else if (seriesLabel.lastIndexOf("NH3") != -1 || seriesLabel.equalsIgnoreCase("y ions - mod.")) {
                currentColor = new Color(255, 0, 150);
            }

            // change color slightly if a double charge is detected
            if (seriesLabel.lastIndexOf("++") != -1) {
                currentColor = new Color(currentColor.getRed() - 100, currentColor.getGreen(), currentColor.getBlue());
            }

        } else if (seriesLabel.startsWith("z")) {

            // dark green
            currentColor = new Color(64, 179, 0);

            // change color slightly if a neutral loss is detected
            if (seriesLabel.lastIndexOf("H2O") != -1 || seriesLabel.lastIndexOf("H20") != -1) {
                currentColor = new Color(64, 179, 150);
            } else if (seriesLabel.lastIndexOf("NH3") != -1) {
                currentColor = new Color(255, 179, 150);
            }

            // change color slightly if a double charge is detected
            if (seriesLabel.lastIndexOf("++") != -1) {
                currentColor = new Color(currentColor.getRed(), currentColor.getGreen() - 100, currentColor.getBlue());
            }

        }

        return currentColor;
    }

    /**
     * Add reference areas annotating the de novo tags.
     *
     * @param currentPeptide the current peptide sequence
     * @param annotations the current fragment ion annotations
     * @param aForwardIon the forward de novo sequencing fragment ion type,
     * i.e., PeptideFragmentIon.A_ION, PeptideFragmentIon.B_ION or
     * PeptideFragmentIon.C_ION
     * @param aReverseIon the reverse de novo sequencing fragment ion type,
     * i.e., PeptideFragmentIon.X_ION, PeptideFragmentIon.Y_ION or
     * PeptideFragmentIon.Z_ION
     * @param aDeNovoCharge the de novo sequencing charge
     * @param showForwardTags if true, the forward de novo sequencing tags are
     * displayed
     * @param showReverseTags if true, the reverse de novo sequencing tags are
     * displayed
     */
    public void addAutomaticDeNovoSequencing(
            Peptide currentPeptide, ArrayList<IonMatch> annotations,
            int aForwardIon, int aReverseIon, int aDeNovoCharge,
            boolean showForwardTags, boolean showReverseTags) {

        int forwardIon = aForwardIon;
        int reverseIon = aReverseIon;
        int deNovoCharge = aDeNovoCharge;

        IonMatch[] forwardIons = new IonMatch[currentPeptide.getSequence().length()];
        IonMatch[] reverseIons = new IonMatch[currentPeptide.getSequence().length()];

        // iterate the annotations and find the de novo tags
        for (int i = 0; i < annotations.size(); i++) {

            IonMatch tempMatch = annotations.get(i);

            if (tempMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION
                    && tempMatch.ion.getNeutralLosses().isEmpty()
                    && tempMatch.charge.value == deNovoCharge) {

                PeptideFragmentIon fragmentIon = (PeptideFragmentIon) tempMatch.ion;

                if (fragmentIon.getSubType() == forwardIon) {
                    forwardIons[fragmentIon.getNumber() - 1] = tempMatch;
                } else if (fragmentIon.getSubType() == reverseIon) {
                    reverseIons[fragmentIon.getNumber() - 1] = tempMatch;
                }
            }
        }

        // add reverse ion de novo tags (x, y or c)
        if (showReverseTags) {

            Color annotationColor = SpectrumPanel.determineFragmentIonColor(Ion.getGenericIon(Ion.IonType.PEPTIDE_FRAGMENT_ION, reverseIon), false);

            for (int i = 1; i < reverseIons.length; i++) {
                if (reverseIons[i] != null && reverseIons[i - 1] != null) {
                    addReferenceAreaXAxis(new ReferenceArea(
                            "r" + i,
                            currentPeptide.getSequence().substring(currentPeptide.getSequence().length() - i - 1, currentPeptide.getSequence().length() - i),
                            reverseIons[i - 1].peak.mz, reverseIons[i].peak.mz, annotationColor, 0.2f, false, true, annotationColor, true, Color.lightGray, 0.2f, 1));
                }
            }
        }

        // add forward ion de novo tags (a, b or c)
        if (showForwardTags) {

            Color annotationColor = SpectrumPanel.determineFragmentIonColor(Ion.getGenericIon(Ion.IonType.PEPTIDE_FRAGMENT_ION, forwardIon), false);

            for (int i = 1; i < forwardIons.length; i++) {
                if (forwardIons[i] != null && forwardIons[i - 1] != null) {
                    addReferenceAreaXAxis(new ReferenceArea(
                            "f" + i,
                            currentPeptide.getSequence().substring(i, i + 1),
                            forwardIons[i - 1].peak.mz, forwardIons[i].peak.mz, annotationColor, 0.2f, false, true, annotationColor, true, Color.lightGray, 0.2f, 0.9));
                }
            }
        }
    }
}
