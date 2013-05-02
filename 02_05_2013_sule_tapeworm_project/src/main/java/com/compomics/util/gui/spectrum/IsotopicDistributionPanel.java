package com.compomics.util.gui.spectrum;

import com.compomics.util.enumeration.MolecularElement;
import com.compomics.util.general.IsotopicDistribution;
import com.compomics.util.general.IsotopicDistributionSpectrum;
import com.compomics.util.general.MassCalc;
import com.compomics.util.general.UnknownElementMassException;
import com.compomics.util.interfaces.SpectrumFile;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.MolecularFormula;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.border.EtchedBorder;
import org.apache.log4j.Logger;

/**
 * This class provides a JPanel that can display a peptide isotopic distribution.
 *
 * @author Harald Barsnes
 * @author Niklaas Colaert
 * @author Lennart Martens
 * @version $Id$
 */
public class IsotopicDistributionPanel extends GraphicsPanel {

    // Class specific log4j logger for MolecularFormula instances.
    Logger logger = Logger.getLogger(MolecularFormula.class);
    /**
     * The color used for the peaks. Default to red.
     */
    private Color aSpectrumPeakColor = Color.RED;
    /**
     * The color used for the area under the curve. Defaults to pink.
     */
    private Color aSpectrumProfileModeLineColor = Color.PINK;
    /**
     * The peptide sequences to display the isotopic distribution for.
     */
    private ArrayList<AASequenceImpl> peptideSequences = null;
    /**
     * The charges of the peptides. Indexed by dataset.
     */
    private ArrayList<Integer> peptideCharges = null;
    /**
     * HashMap with the molecular formula for all the aminoacids
     */
    private HashMap<String, MolecularFormula> iElements;

    /**
     * This constructor creates an IsotopicDistributionPanel based on the passed parameters.
     *
     * @param peptideSequence   the peptide sequence to display the isotopic distribution for
     * @param peptideCharge     the charge of the peptide
     * @param profileMode       if true the peaks will be showned in a profile like mode where
     *                          support peaks are added in front of and after the real peak
     *                          (note that this is unlike the profile modes of the other graphics
     *                          panels)
     * @param labelDifference   the number of neutrons to add due to the label
     * @throws IOException  
     */
    public IsotopicDistributionPanel(String peptideSequence, Integer peptideCharge, boolean profileMode, int labelDifference) throws IOException {

        if (profileMode) {
            this.currentGraphicsPanelType = GraphicsPanelType.isotopicDistributionProfile;
        } else {
            this.currentGraphicsPanelType = GraphicsPanelType.isotopicDistributionCentroid;
        }

        // gets the elements that can be used
        getElements();

        // validate the peptide sequence
        AASequenceImpl validatedPeptideSequence = validatePeptideSequence(peptideSequence);

        peptideSequences = new ArrayList<AASequenceImpl>();
        peptideSequences.add(validatedPeptideSequence);

        peptideCharges = new ArrayList<Integer>();
        peptideCharges.add(peptideCharge);

        // calculate the isotopic distribution
        IsotopicDistributionSpectrum isotopicDistributionSpectrum =
                calculateIsotopicDistribution(validatedPeptideSequence, peptideCharge, labelDifference);

        dataSetCounter = 0;
        this.processIsotopicDistribution(isotopicDistributionSpectrum, aSpectrumPeakColor, aSpectrumProfileModeLineColor);

        // graphical user interface settings
        this.iXAxisStartAtZero = false;
        rescaleWithLeftSidePadding();
        this.iCurrentDrawStyle = DrawingStyle.LINES;
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setBackground(Color.WHITE);
        this.iYAxisLabel = "Int (%)";

        this.iSpecPanelListeners = new ArrayList();
        this.addListeners();
    }

    /**
     * Calculates the isotopic distribution of the peptide.
     *
     * @param validatedPeptideSequence  the peptide to calculate the isotopic distribution for
     * @param peptideCharge             the charge of the peptide
     * @param labelDifference           the number of neutrons to add due to the label
     * @return                          the isotopic distribution as a spectrum
     */
    private IsotopicDistributionSpectrum calculateIsotopicDistribution(AASequenceImpl validatedPeptideSequence, Integer peptideCharge, int labedDifference) {

        // calculate the m/z value of the peptide
        double mzValue = validatedPeptideSequence.getMz(peptideCharge);

        // calculate the distribution
        IsotopicDistribution lIso = validatedPeptideSequence.getIsotopicDistribution();

        //set the label difference if necessary
        if(labedDifference>0){
            lIso.setLabelDifference(labedDifference);
        }
        // add the peaks to the dataset
        HashMap lPeaks = new HashMap();

        try {
            for (int i = 0; i < 15; i++) {

//                      @TODO: refine the adding of additional peaks

                int numberOfSidePeaks = 10;

                // if profile mode, add some additional "profile mode looking" peaks before the peak
                if (currentGraphicsPanelType.equals(GraphicsPanelType.isotopicDistributionProfile)) {
                    for (int j=0; j < numberOfSidePeaks; j++) {
                        lPeaks.put(mzValue + (i * (new MassCalc().calculateMass("H") / (double) peptideCharge)) - 0.01*(numberOfSidePeaks-j), lIso.getPercMax()[i] * j*10);
                    }
                }

                lPeaks.put(mzValue + (i * (new MassCalc().calculateMass("H") / (double) peptideCharge)), lIso.getPercMax()[i] * 100);

//                      @TODO: refine the adding of additional peaks

                // if profile mode, add some additional "profile mode looking" peaks before the peak
                if (currentGraphicsPanelType.equals(GraphicsPanelType.isotopicDistributionProfile)) {
                    for (int j=1; j <= numberOfSidePeaks; j++) {
                        lPeaks.put(mzValue + (i * (new MassCalc().calculateMass("H") / (double) peptideCharge)) + 0.01*j, lIso.getPercMax()[i] * (100 - j*10));
                    }
                }
            }
        } catch (UnknownElementMassException ume) {
            logger.error(ume.getMessage(), ume);
        }

        IsotopicDistributionSpectrum lSpecFile = new IsotopicDistributionSpectrum();
        lSpecFile.setCharge(peptideCharge);
        lSpecFile.setPrecursorMZ(mzValue);
        lSpecFile.setPeaks(lPeaks);

        return lSpecFile;
    }

    /**
     * Adds an additional isotopic distribution dataset to be displayed in the same
     * panel. Remember to use different colors for the different datasets.
     *
     * @param peptideSequence       the peptide sequence to display the isotopic distribution for
     * @param peptideCharge         the charge of the peptide
     * @param dataPointAndLineColor the color to use for the data points and lines
     * @param areaUnderCurveColor   the color to use for the area under the curve
     * @param labelDifference       the number of neutrons to add due to the label
     * @throws IOException
     */
    public void addAdditionalDataset(String peptideSequence, Integer peptideCharge, Color dataPointAndLineColor, Color areaUnderCurveColor, int labelDifference) throws IOException {

        // validate the peptide sequence
        AASequenceImpl validatedPeptideSequence = validatePeptideSequence(peptideSequence);

        peptideSequences.add(validatedPeptideSequence);
        peptideCharges.add(peptideCharge);

        IsotopicDistributionSpectrum isotopicDistributionSpectrum =
                calculateIsotopicDistribution(validatedPeptideSequence, peptideCharge, labelDifference);

        this.processIsotopicDistribution(isotopicDistributionSpectrum, dataPointAndLineColor, areaUnderCurveColor);

        rescaleWithLeftSidePadding();

        this.showFileName = false;
        this.showPrecursorDetails = false;
        this.showResolution = false;
    }

    /**
     * Rescales to show all peaks, adds a minimum padding on the left side
     * to make sure the that first peak is not too close to the y-axis.
     */
    private void rescaleWithLeftSidePadding() {

        double tempMinXValue = getMinXAxisValue();
        tempMinXValue -= 1;

        if (tempMinXValue < 0) {
            tempMinXValue = 0;
        }

        this.rescale(tempMinXValue, getMaxXAxisValue());
    }

    /**
     * Validates the peptide sequence to check for non amino acid elements.
     *
     * @param peptideSequence the peptide sequence to validate
     * @return the validated peptide sequence 
     * @throws IOException
     */
    private AASequenceImpl validatePeptideSequence(String peptideSequence) throws IOException {

        // get the sequence
        String lSeq = peptideSequence;

        //exclude unwanted characters
        lSeq = lSeq.trim().toUpperCase();
        lSeq = lSeq.replace("\n", "");
        lSeq = lSeq.replace("\t", "");
        lSeq = lSeq.replace(" ", "");

        // check the amino acids
        for (int i = 0; i < lSeq.length(); i++) {
            String lLetter = String.valueOf(lSeq.charAt(i));
            if (!isElement(lLetter)) {
                throw new IOException(lLetter + " at position " + (i + 1) + " is not a valid element!");
            }
        }
        if (lSeq.length() == 0) {
            throw new IOException("Sequence cannot be of length zero!");
        }

        // return the sequence
        return new AASequenceImpl(lSeq);
    }

    /**
     * Gets the elements that can be used.
     */
    private void getElements() {

        //get the elements
        iElements = new HashMap<String, MolecularFormula>();

        //get the elements that can be used
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("elements.txt")));
            String line;
            String[] lHeaderElements = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    //do nothing
                } else if (line.startsWith("Header")) {
                    String lTemp = line.substring(line.indexOf("=") + 1);
                    lHeaderElements = lTemp.split(",");
                } else {
                    String lAa = line.substring(0, line.indexOf("="));
                    String[] lContribution = line.substring(line.indexOf("=") + 1).split(",");

                    MolecularFormula lAaFormula = new MolecularFormula();

                    for (int i = 0; i < lHeaderElements.length; i++) {
                        for (MolecularElement lMolecularElement : MolecularElement.values()) {
                            if (lMolecularElement.toString().equalsIgnoreCase(lHeaderElements[i])) {
                                lAaFormula.addElement(lMolecularElement, Integer.valueOf(lContribution[i]));
                            }
                        }
                    }
                    iElements.put(lAa, lAaFormula);
                }
            }
            br.close();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Method that checks if a given string is an element we can calculate an isotopic distribution for
     *
     * @param lElement String with the element to check
     * @return boolean that indicates if we can use this element
     */
    public boolean isElement(String lElement) {
        Object lValue = iElements.get(lElement);
        if (lValue == null) {
            return false;
        }
        return true;
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
    private void processIsotopicDistribution(SpectrumFile aSpecFile, Color dataPointAndLineColor, Color areaUnderCurveColor) {

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
     * Get the set of peptide sequences.
     *
     * @return the peptideSequences
     */
    public ArrayList<AASequenceImpl> getPeptideSequences() {
        return peptideSequences;
    }
}
