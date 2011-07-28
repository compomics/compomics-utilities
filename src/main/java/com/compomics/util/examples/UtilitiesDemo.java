package com.compomics.util.examples;

import com.compomics.util.Export;
import com.compomics.util.Util;
import com.compomics.util.enumeration.ImageType;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
import com.compomics.util.general.IsotopicDistribution;
import com.compomics.util.gui.UtilitiesGUIDefaults;
import com.compomics.util.gui.events.RescalingEvent;
import com.compomics.util.gui.interfaces.SpectrumPanelListener;
import com.compomics.util.gui.protein.ProteinSequencePane;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.gui.spectrum.ChromatogramPanel;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.IsotopicDistributionPanel;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.io.PklFile;
import com.compomics.util.io.filefilters.PdfFileFilter;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntegerColorTableCellRenderer;
import org.apache.batik.transcoder.TranscoderException;

/**
 * This class serves as a demo of how the compomics-utilities library can be
 * used in other projects. It contains several demos, and is also the frame shown
 * if the compomics-utilities jar file is double clicked or ran from the command 
 * line.
 *
 * @author Harald Barsnes
 */
public class UtilitiesDemo extends javax.swing.JFrame {
 
    /**
     * The PEFF annotations color map.
     */
    private HashMap<Integer, Color> peffAnnotationsColorMap;
    /**
     * The PEFF annotations tooltip map.
     */
    private HashMap<Integer, String> peffAnnotationsTooltipMap;
    /**
     * If true, the format shown is PEFF, if false FASTA is assumed.
     */
    private boolean peffFormat = true;
    /**
     * The map of the PEFF key value pairs.
     */
    private TreeMap<String, String> keyValuePairs;
    /**
     * The current PEFF (or FASTA) sequence file.
     */
    private File currentSequenceFile;
    /**
     * The current PEFF (or FASTA) buffered reader.
     */
    private BufferedReader currentPeffBufferedReader;
    /**
     * The current PEFF (or FASTA) sequence.
     */
    private String currentSequence = "";
    /**
     * The current PEFF (or FASTA) sequence line.
     */
    private String currentSequenceLine;
    /**
     * A hashmap of both the linked spectra.
     */
    private HashMap<Integer, SpectrumPanel> linkedSpectrumPanels;
    /**
     * A hashmap of all spectrum panel annotations.
     */
    private HashMap<Integer, Vector<DefaultSpectrumAnnotation>> allAnnotations;
    /**
     * The first spectrum panel.
     */
    private SpectrumPanel spectrumAPanel;
    /**
     *  The second spectrum panel
     */
    private SpectrumPanel spectrumBPanel;
    /**
     * The maximum padding allowed in the spectrum panels.
     * Increase if font size on the y-axis becomes too small.
     */
    private int spectrumPanelMaxPadding = 50;
    /**
     * The maximum padding allowed in the chromatogram panels.
     * Increase if font size on the y-axis becomes too small.
     */
    private int chromatogramPanelMaxPadding = 65;
    /**
     * Used to read the enzyme details from file.
     */
    private MascotEnzymeReader mascotEnzymeReader = null;
    /**
     * Used for the in silico digestion example.
     */
    private String cleanProteinSequence = null;
    /**
     * The tag colors for PEFF formatting.
     */
    private HashMap<String, Color> selectedAnnotationTypes;

    /** 
     * Creates a new UtilitiesDemo frame and makes it visible.
     */
    public UtilitiesDemo() {
        initComponents();

        keyValuePairs = new TreeMap<String, String>();
        selectedAnnotationTypes = new HashMap<String, Color>();

        // set the title including version number
        this.setTitle(this.getTitle() + " " + getVersion() + " - Demo");

        // insert the text in the information tab
        insertInformationTabText();

        // centrally align the comboboxes
        silacLabelPeptideAJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        silacLabelPeptideBJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        enzymesJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        // set column properties
        setColumnProperties();

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/icons/compomics-utilities.png")));

        // set up the demos
        setUpSpectrumPanelDemo();
        setUpChromatogramPanelDemo();
        setUpIsotopicDistributionPanelDemo();
        setUpInSilicoDigestionDemo();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Sets the column size and creates the column tooltips.
     */
    private void setColumnProperties() {

        peptidesJXTable.getTableHeader().setReorderingAllowed(false);
        peptideAJXTable.getTableHeader().setReorderingAllowed(false);
        peptideBJXTable.getTableHeader().setReorderingAllowed(false);
        peffHeaderJTable.getTableHeader().setReorderingAllowed(false);
        peffAnnotationsJTable.getTableHeader().setReorderingAllowed(false);
        proteinHeaderJTable.getTableHeader().setReorderingAllowed(false);

        peptidesJXTable.getColumn(" ").setMinWidth(40);
        peptidesJXTable.getColumn(" ").setMaxWidth(40);
        peptidesJXTable.getColumn("Start").setMinWidth(40);
        peptidesJXTable.getColumn("Start").setMaxWidth(40);
        peptidesJXTable.getColumn("End").setMinWidth(40);
        peptidesJXTable.getColumn("End").setMaxWidth(40);

        peffHeaderJTable.getColumn(" ").setMinWidth(40);
        peffHeaderJTable.getColumn(" ").setMaxWidth(40);

        proteinHeaderJTable.getColumn(" ").setMinWidth(40);
        proteinHeaderJTable.getColumn(" ").setMaxWidth(40);

        peffAnnotationsJTable.getColumn(" ").setMinWidth(40);
        peffAnnotationsJTable.getColumn(" ").setMaxWidth(40);
        peffAnnotationsJTable.getColumn("F").setMinWidth(40);
        peffAnnotationsJTable.getColumn("F").setMaxWidth(40);
        peffAnnotationsJTable.getColumn("B").setMinWidth(40);
        peffAnnotationsJTable.getColumn("B").setMaxWidth(40);
        peffAnnotationsJTable.getColumn("  ").setMinWidth(40);
        peffAnnotationsJTable.getColumn("  ").setMaxWidth(40);

        peffAnnotationsJTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());
    }

    /**
     * Inserts the html text into the information tab.
     */
    private void insertInformationTabText() {

        String htmlText = "<html><head></head><body>"
                + "<h1>Compomics-Utilities</h1>"
                + "<p style=\"margin-top: 0\">"
                + "The <a href=\"http://www.compomics.com\">Computational Omics Group</a> at the <a href=\"http://www.ugent.be/en\">University of Ghent</a> "
                + "develops various bioinformatics tools for analyzing omics data."
                + "<br><br>"
                + "Compomics-utilities is a library containing code shared by many of our research projects, amongst others containing panels <br>"
                + "for visualizing spectra and chromatograms  and objects for representing peptides and proteins etc.  We believe that this library<br>"
                + "can be of use to other research groups doing computational proteomics, and have therefore made it available as open source."
                + "<br><br>"
                + "This demo contains four examples of how the library can be used. For the complete source code of the examples, see the<br>"
                + "<i>com.compomics.util.examples</i> package. Click the tabs at the top to select one of the demos."
                + "<br><br>"
                + "Additional info can be found at the project's <a href=\"http://googlecode.com\">Google Code</a> home page: "
                + "<a href=\"http://compomics-utilities.googlecode.com\">http://compomics-utilities.googlecode.com</a>."
                + "</p>"
                + "<br><br><br><br>"
                + "<b>The Computational Omics Group is grateful to be supported by:<b>"
                + "<br><br>"
                + "<a href=\"http://www.compomics.com\"><img src=\"" + getClass().getResource("/icons/compomics.png") + "\" border=\"0\" width=\"222\" height=\"111\"></a>"
                + "<a href=\"http://www.ugent.be/en\"><img src=\"" + getClass().getResource("/icons/ugent.png") + "\" border=\"0\"></a>"
                + "<a href=\"http://www.vib.be\"><img src=\"" + getClass().getResource("/icons/vib.png") + "\" border=\"0\"></a>"
                + "<a href=\"http://java.com/en\"><img src=\"" + getClass().getResource("/icons/java.png") + "\" border=\"0\"></a>"
                + "<a href=\"http://maven.apache.org\"><img src=\"" + getClass().getResource("/icons/maven.png") + "\" border=\"0\"></a>"
                + "<a href=\"http://www.jetbrains.com/idea/\"><img src=\"" + getClass().getResource("/icons/intelliJ.png") + "\" border=\"0\"></a>"
                + "<a href=\"http://www.yourkit.com/\"><img src=\"" + getClass().getResource("/icons/yourkit.png") + "\" border=\"0\"></a>"
                + "</body>"
                + "</html>";

        informationJEditorPane.setText(htmlText);
    }

    /**
     * Set up the in silico digestion demo.
     */
    private void setUpInSilicoDigestionDemo() {

        try {
            // locate the file with the enzyme details
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("enzymes.txt");

            if (in != null) {
                // get the enzyme details from file
                mascotEnzymeReader = new MascotEnzymeReader(in);
                String[] allEnzymeNames = mascotEnzymeReader.getEnzymeNames();

                // order the enzymes and add to combobox
                Arrays.sort(allEnzymeNames);
                enzymesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(allEnzymeNames));

                // set the default enzyme to trypsin
                enzymesJComboBox.setSelectedItem("Trypsin");

                // update the results
                enzymesJComboBoxActionPerformed(null);

                // select the first peptide in the list
                if (peptidesJXTable.getRowCount() > 0) {
                    peptidesJXTable.setRowSelectionInterval(0, 0);
                    formatProteinSequence();
                }
            } else {
                JOptionPane.showMessageDialog(this, "enzymes.txt not found...", "Error Setting Up In Silico Digestion Demo", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Setting Up In Silico Digestion Demo", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sets up the IsotopicDistribution Panel demo.
     */
    private void setUpIsotopicDistributionPanelDemo() {

        // remove previuos isotopic distributions
        isotopicDistributionAJPanel.removeAll();
        peptideAColorJPanel.setBackground(new JButton().getBackground());
        peptideBColorJPanel.setBackground(new JButton().getBackground());

        try {
            // check if we have to use label modifiers
            String labelPeptideA = (String) silacLabelPeptideAJComboBox.getSelectedItem();
            int labelDifferencePeptideA = 0;

            if (labelPeptideA.startsWith("+")) {
                labelDifferencePeptideA = Integer.valueOf(labelPeptideA.substring(1));
            }

            // create the first isotopic distribution
            IsotopicDistributionPanel isotopicDistributionPanel =
                    new IsotopicDistributionPanel(peptideSequenceAJTextField.getText(),
                    (Integer) chargePeptideAJSpinner.getValue(), true, labelDifferencePeptideA);

            // add the distribution to the table as well
            AASequenceImpl peptideSequence = isotopicDistributionPanel.getPeptideSequences().get(0);
            IsotopicDistribution lIso = peptideSequence.getIsotopicDistribution();

            if (labelDifferencePeptideA > 0) {
                lIso.setLabelDifference(labelDifferencePeptideA);
            }

            while (peptideAJXTable.getRowCount() > 0) {
                ((DefaultTableModel) peptideAJXTable.getModel()).removeRow(0);
            }

            for (int i = 0; i < 15; i++) {
                if (Util.roundDouble(lIso.getPercTot()[i], 2) > 0) {

                    ((DefaultTableModel) peptideAJXTable.getModel()).addRow(
                            new Object[]{
                                new Integer(i),
                                Math.floor(lIso.getPercTot()[i] * 10000.0) / 100.0,
                                Math.floor(lIso.getPercMax()[i] * 10000.0) / 100.0});
                }
            }

            // update the dataset colors in the demo panel
            peptideAColorJPanel.setBackground(isotopicDistributionPanel.getAreaUnderCurveColors().get(0));

            // display mz and molecular composition of peptide
            peptideAMzJTextField.setText("" + Util.roundDouble(peptideSequence.getMz((Integer) chargePeptideAJSpinner.getValue()) + labelDifferencePeptideA, 4));

            if (labelDifferencePeptideA > 0) {
                peptideACompositionJTextField.setText(peptideSequence.getMolecularFormula().toString() + " + " + labelDifferencePeptideA + "n");
            } else {
                peptideACompositionJTextField.setText(peptideSequence.getMolecularFormula().toString());
            }

            // add the second peptide if data has been inserted
            if (peptideSequenceBJTextField.getText().length() > 0) {
                //check if we have to use label modifiers
                String labelPeptideB = (String) silacLabelPeptideBJComboBox.getSelectedItem();

                int labelDifferencePeptideB = 0;

                if (labelPeptideB.startsWith("+")) {
                    labelDifferencePeptideB = Integer.valueOf(labelPeptideB.substring(1));
                }

                isotopicDistributionPanel.addAdditionalDataset(
                        peptideSequenceBJTextField.getText(),
                        (Integer) chargePeptideBJSpinner.getValue(), Color.BLUE, new Color(85, 85, 255), labelDifferencePeptideB);

                // add the distribution to the table as well
                peptideSequence = isotopicDistributionPanel.getPeptideSequences().get(1);

                lIso = peptideSequence.getIsotopicDistribution();

                if (labelDifferencePeptideB > 0) {
                    lIso.setLabelDifference(labelDifferencePeptideB);
                }

                while (peptideBJXTable.getRowCount() > 0) {
                    ((DefaultTableModel) peptideBJXTable.getModel()).removeRow(0);
                }

                for (int i = 0; i < 15; i++) {
                    if (Util.roundDouble(lIso.getPercTot()[i], 2) > 0) {

                        ((DefaultTableModel) peptideBJXTable.getModel()).addRow(
                                new Object[]{
                                    new Integer(i),
                                    Math.floor(lIso.getPercTot()[i] * 10000.0) / 100.0,
                                    Math.floor(lIso.getPercMax()[i] * 10000.0) / 100.0});
                    }
                }

                // update the dataset colors in the demo panel
                peptideBColorJPanel.setBackground(isotopicDistributionPanel.getAreaUnderCurveColors().get(1));

                // display mz and molecular composition of peptide
                peptideBMzJTextField.setText("" + Util.roundDouble(peptideSequence.getMz((Integer) chargePeptideBJSpinner.getValue()) + labelDifferencePeptideB, 4));

                if (labelDifferencePeptideB > 0) {
                    peptideBCompositionJTextField.setText(peptideSequence.getMolecularFormula().toString() + " + " + labelDifferencePeptideB + "n");
                } else {
                    peptideBCompositionJTextField.setText(peptideSequence.getMolecularFormula().toString());
                }
            } else {
                // clear the results for peptide B
                peptideBMzJTextField.setText("");
                peptideBCompositionJTextField.setText("");

                while (peptideBJXTable.getRowCount() > 0) {
                    ((DefaultTableModel) peptideBJXTable.getModel()).removeRow(0);
                }
            }

            // remove the default isotopic distribution panel border, given that our
            // isotopic distribution panel already has a border
            isotopicDistributionPanel.setBorder(null);

            // add the isotopic distribution panel to the frame
            isotopicDistributionAJPanel.add(isotopicDistributionPanel);
            isotopicDistributionAJPanel.validate();
            isotopicDistributionAJPanel.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Setting Up Isotopic Distributions", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sets up the Chromatogram Panel demo.
     */
    private void setUpChromatogramPanelDemo() {

        // get data for the chromatogram
        File chromatogramFile = new File(getJarFilePath() + "/exampleFiles/exampleChromatogram.txt");

        ArrayList<Double> xValuesAsArray = new ArrayList<Double>();
        ArrayList<Double> yValuesAsArray = new ArrayList<Double>();

        try {
            FileReader f = new FileReader(chromatogramFile);
            BufferedReader b = new BufferedReader(f);

            String currentLine = b.readLine();

            while (currentLine != null) {

                String[] peakDetails = currentLine.split(" ");

                if (peakDetails.length != 2) {
                    throw new IOException("Error reading chromatogram file - incorrect number of peak paramaters!");
                }

                xValuesAsArray.add(new Double(peakDetails[0]));
                yValuesAsArray.add(new Double(peakDetails[1]));

                currentLine = b.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading chromatogram data:\n" + e.toString(), "Error Reading Chromatogram Data", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading chromatogram data:\n" + e.toString(), "Error Reading Chromatogram Data", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // convert the data to the required Chromatogram Panel format
        double[] xValues = new double[xValuesAsArray.size()];
        double[] yValues = new double[yValuesAsArray.size()];

        for (int i = 0; i < xValuesAsArray.size(); i++) {
            xValues[i] = xValuesAsArray.get(i);
            yValues[i] = yValuesAsArray.get(i);
        }

        // create the chromatogram
        ChromatogramPanel chromatogramPanel = new ChromatogramPanel(
                xValues, yValues, "Time (minutes)", "Intensity (number of counts)");
        chromatogramPanel.setMaxPadding(chromatogramPanelMaxPadding);

        // reference lines example
//        chromatogramPanel.addReferenceAreaXAxis(new ReferenceArea("A", 40, 50, Color.yellow, 0.3f, true, true));
//        chromatogramPanel.addReferenceAreaXAxis(new ReferenceArea("B", 70, 80, Color.yellow, 0.3f, false, true));

        // remove the default chromatogram panel border, given that our
        // chromatogram panel already has a border
        chromatogramPanel.setBorder(null);

        // add the chromatogram panel to the frame
        chromatogramAJPanel.add(chromatogramPanel);
        chromatogramAJPanel.validate();
        chromatogramAJPanel.repaint();
    }

    /**
     * Sets up the Spectrum Panel demo.
     */
    private void setUpSpectrumPanelDemo() {

        linkedSpectrumPanels = new HashMap<Integer, SpectrumPanel>();
        allAnnotations = new HashMap<Integer, Vector<DefaultSpectrumAnnotation>>();

        try {
            // create and add two spectra to the view

            // get the peaks for the first spectrum
            File spectrumFile = new File(getJarFilePath() + "/exampleFiles/exampleSpectrumA.pkl");
            PklFile pklFileA = new PklFile(spectrumFile);

            // create the first spectrum panel
            spectrumAPanel = getSpectrumPanel(pklFileA, profileSpectrumJCheckBox.isSelected());

            // add the fragment ions annotations for the first spectrum
            Vector<DefaultSpectrumAnnotation> currentAnnotations = new Vector();
            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.006822999999997137, SpectrumPanel.determineColorOfPeak("y1"), "y1"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(389.251235, 4.6299999996790575E-4, SpectrumPanel.determineColorOfPeak("y3"), "y3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(460.288345, -0.003290999999990163, SpectrumPanel.determineColorOfPeak("y4"), "y4"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(559.356755, -2.4200000007112976E-4, SpectrumPanel.determineColorOfPeak("y5"), "y5"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(660.404435, -0.002686000000039712, SpectrumPanel.determineColorOfPeak("y6"), "y6"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(820.4350840000001, 8.09999999091815E-5, SpectrumPanel.determineColorOfPeak("y7"), "y7"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(271.177006, -0.003444999999999254, SpectrumPanel.determineColorOfPeak("y2-NH3"), "y2-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(288.203555, -0.002484999999978754, SpectrumPanel.determineColorOfPeak("y2"), "y2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -5.020000000115488E-4, SpectrumPanel.determineColorOfPeak("y1-NH3"), "y1-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(372.224686, 0.001030999999954929, SpectrumPanel.determineColorOfPeak("y3-NH3"), "y3-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(443.261796, 0.0025039999999876272, SpectrumPanel.determineColorOfPeak("y4-NH3"), "y4-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(274.12253400000003, 0.00181899999995494, SpectrumPanel.determineColorOfPeak("b2"), "b2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(458.20561749999996, 0.05911150000002863, SpectrumPanel.determineColorOfPeak("Prec-H2O 2+"), "Prec-H2O 2+"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(129.000000, 0.10726900000000228, SpectrumPanel.determineColorOfPeak("iR"), "iR"));

//            // ------------test: annotation second spectrum--------------
//            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.010621000000014647, SpectrumPanel.determineColorOfPeak("y1"), "y1*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(387.27196499999997, -0.0044499999999629836, SpectrumPanel.determineColorOfPeak("y3"), "y3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(500.356025, -0.002353999999968437, SpectrumPanel.determineColorOfPeak("y4"), "y4*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(571.393135, -0.004269000000022061, SpectrumPanel.determineColorOfPeak("y5"), "y5*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(685.436065, -0.013534999999933461, SpectrumPanel.determineColorOfPeak("y6"), "y6*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(813.494645, 0.005993999999986954, SpectrumPanel.determineColorOfPeak("y7"), "y7*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(257.161356, -0.007209999999986394, SpectrumPanel.determineColorOfPeak("y2-NH3"), "y2-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(370.245416, -9.159999999610591E-4, SpectrumPanel.determineColorOfPeak("y3-NH3"), "y3-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(796.468096, 0.0018540000000939472, SpectrumPanel.determineColorOfPeak("y7-NH3"), "y7-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(274.187905, -0.004702000000008866, SpectrumPanel.determineColorOfPeak("y2"), "y2*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -0.008444000000025653, SpectrumPanel.determineColorOfPeak("y1-NH3"), "y1-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(668.4095159999999, 0.0019680000000334985, SpectrumPanel.determineColorOfPeak("y6-NH3"), "y6-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(276.134815, -0.002712000000030912, SpectrumPanel.determineColorOfPeak("b2"), "b2*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(259.108266, -0.004803000000038082, SpectrumPanel.determineColorOfPeak("b2-NH3"), "b2-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(242.164738, -0.08587800000000811, SpectrumPanel.determineColorOfPeak("y4++-NH3"), "y4++-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(129, 0.09981500000000665, SpectrumPanel.determineColorOfPeak("iR"), "iR*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(120, 0.08159999999999457, SpectrumPanel.determineColorOfPeak("iF"), "iF*"));
//            // ------------test: annotation second spectrum--------------

            // store the annotations for later use
            allAnnotations.put(new Integer(0), currentAnnotations);
            spectrumAPanel.setAnnotations(currentAnnotations);

            // store a unique reference to each spectrum panel for linking purposes
            linkedSpectrumPanels.put(new Integer(0), spectrumAPanel);

            // remove the default spectrum panel border, given that our
            // spectrum panel already has a border
            spectrumAPanel.setBorder(null);

            // reference lines example
//            spectrumAPanel.addReferenceAreaXAxis(new ReferenceArea("B", 400, 500, Color.blue, 0.1f, false, true));
//            spectrumAPanel.addReferenceAreaXAxis(new ReferenceArea("A", 200, 250, Color.blue, 0.1f, false, true));
//
//            spectrumAPanel.addReferenceAreaYAxis(new ReferenceArea("Low", 0, 500, Color.ORANGE, 0.3f, false, true));
//            spectrumAPanel.addReferenceAreaYAxis(new ReferenceArea("Medium", 500, 2000, Color.YELLOW, 0.3f, false, true));
//            spectrumAPanel.addReferenceAreaYAxis(new ReferenceArea("High", 2000, 3500, Color.GREEN, 0.3f, false, true));

            // add the spectrum panel to the frame
            spectrumAJPanel.add(spectrumAPanel);
            spectrumAJPanel.validate();
            spectrumAJPanel.repaint();

            // get the peaks for the second spectrum
            spectrumFile = new File(getJarFilePath() + "/exampleFiles/exampleSpectrumB.pkl");
            PklFile pklFileB = new PklFile(spectrumFile);


//            // ------------test multiple spectra ---------------
//
//            spectrumAPanel.addAdditionalDataset(pklFileB.getMzValues(), pklFileB.getIntensityValues(), Color.BLUE, Color.BLUE);
//
//
//            // add the spectrum panel to the frame
//            spectrumAJPanel.add(spectrumAPanel);
//            spectrumAJPanel.validate();
//            spectrumAJPanel.repaint();
//
//            // ------------test multiple spectra ---------------


            // create the second spectrum panel
            spectrumBPanel = getSpectrumPanel(pklFileB, profileSpectrumJCheckBox.isSelected());

            // add the fragment ions annotations for the second spectrum
            currentAnnotations = new Vector();
            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.010621000000014647, SpectrumPanel.determineColorOfPeak("y1"), "y1"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(387.27196499999997, -0.0044499999999629836, SpectrumPanel.determineColorOfPeak("y3"), "y3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(500.356025, -0.002353999999968437, SpectrumPanel.determineColorOfPeak("y4"), "y4"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(571.393135, -0.004269000000022061, SpectrumPanel.determineColorOfPeak("y5"), "y5"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(685.436065, -0.013534999999933461, SpectrumPanel.determineColorOfPeak("y6"), "y6"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(813.494645, 0.005993999999986954, SpectrumPanel.determineColorOfPeak("y7"), "y7"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(257.161356, -0.007209999999986394, SpectrumPanel.determineColorOfPeak("y2-NH3"), "y2-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(370.245416, -9.159999999610591E-4, SpectrumPanel.determineColorOfPeak("y3-NH3"), "y3-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(796.468096, 0.0018540000000939472, SpectrumPanel.determineColorOfPeak("y7-NH3"), "y7-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(274.187905, -0.004702000000008866, SpectrumPanel.determineColorOfPeak("y2"), "y2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -0.008444000000025653, SpectrumPanel.determineColorOfPeak("y1-NH3"), "y1-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(668.4095159999999, 0.0019680000000334985, SpectrumPanel.determineColorOfPeak("y6-NH3"), "y6-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(276.134815, -0.002712000000030912, SpectrumPanel.determineColorOfPeak("b2"), "b2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(259.108266, -0.004803000000038082, SpectrumPanel.determineColorOfPeak("b2-NH3"), "b2-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(242.164738, -0.08587800000000811, SpectrumPanel.determineColorOfPeak("y4++-NH3"), "y4++-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(129, 0.09981500000000665, SpectrumPanel.determineColorOfPeak("iR"), "iR"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(120, 0.08159999999999457, SpectrumPanel.determineColorOfPeak("iF"), "iF"));

            // store the annotations for later use
            allAnnotations.put(new Integer(1), currentAnnotations);
            spectrumBPanel.setAnnotations(currentAnnotations);

            // store a unique reference to each spectrum panel for linking purposes
            linkedSpectrumPanels.put(new Integer(1), spectrumBPanel);

            // remove the default spectrum panel border, given that our
            // spectrum panel already has a border
            spectrumBPanel.setBorder(null);

            // add the spectrum panel to the frame
            spectrumBJPanel.add(spectrumBPanel);
            spectrumBJPanel.validate();
            spectrumBJPanel.repaint();

            // update the fragment ions
            aIonsJCheckBoxActionPerformed(null);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading spectrum data:\n" + e.toString(), "Error Reading Spectrum Data", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns a spectrum panel containing the provided data.
     *
     * @param pklFile the pkl file containing the spectrum
     * @param profileMode if true the spectrum is drawn in profile mode
     * @return the created spectrum panel
     * @throws IOException
     */
    private SpectrumPanel getSpectrumPanel(PklFile pklFile, boolean profileMode) throws IOException {

        SpectrumPanel spectrumPanel = new SpectrumPanel(
                pklFile.getMzValues(), pklFile.getIntensityValues(),
                pklFile.getPrecursorMz(), "" + pklFile.getPrecurorCharge(),
                "" + pklFile.getFileName(),
                spectrumPanelMaxPadding, false, false, false, 2, profileMode);

        spectrumPanel.addSpectrumPanelListener(new SpectrumPanelListener() {

            public void rescaled(RescalingEvent rescalingEvent) {
                SpectrumPanel source = (SpectrumPanel) rescalingEvent.getSource();
                double minMass = rescalingEvent.getMinMass();
                double maxMass = rescalingEvent.getMaxMass();

                Iterator<Integer> iterator = linkedSpectrumPanels.keySet().iterator();

                while (iterator.hasNext()) {
                    SpectrumPanel currentSpectrumPanel = linkedSpectrumPanels.get(iterator.next());
                    if (currentSpectrumPanel != source && linkedSpectraJCheckBox.isSelected()) {
                        currentSpectrumPanel.rescale(minMass, maxMass, false);
                        currentSpectrumPanel.repaint();
                    }
                }
            }
        });

        return spectrumPanel;
    }

    /**
     * Returns the path to the jar file.
     *
     * @return
     */
    private String getJarFilePath() {
        String path = this.getClass().getResource("UtilitiesDemo.class").getPath();

        if (path.lastIndexOf("/utilities-") != -1) {
            path = path.substring(5, path.lastIndexOf("/utilities-"));
            path = path.replace("%20", " ");
        } else {
            path = ".";
        }

        return path;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRadioButton1 = new javax.swing.JRadioButton();
        jTabbedPane = new javax.swing.JTabbedPane();
        informationJPanel = new javax.swing.JPanel();
        informationJScrollPane = new javax.swing.JScrollPane();
        informationJEditorPane = new javax.swing.JEditorPane();
        spectrumJPanel = new javax.swing.JPanel();
        ionSelectionJPanel = new javax.swing.JPanel();
        aIonsJCheckBox = new javax.swing.JCheckBox();
        bIonsJCheckBox = new javax.swing.JCheckBox();
        cIonsJCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        yIonsJCheckBox = new javax.swing.JCheckBox();
        xIonsJCheckBox = new javax.swing.JCheckBox();
        zIonsJCheckBox = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        chargeOneJCheckBox = new javax.swing.JCheckBox();
        chargeTwoJCheckBox = new javax.swing.JCheckBox();
        chargeOverTwoJCheckBox = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        H2OIonsJCheckBox = new javax.swing.JCheckBox();
        NH3IonsJCheckBox = new javax.swing.JCheckBox();
        otherIonsJCheckBox = new javax.swing.JCheckBox();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        profileSpectrumJCheckBox = new javax.swing.JCheckBox();
        linkedSpectraJCheckBox = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        exportJButton = new javax.swing.JButton();
        allPeaksJCheckBox = new javax.swing.JCheckBox();
        spectrumPanelInfoJLabel = new javax.swing.JLabel();
        spectrumPanelHelpJLabel = new javax.swing.JLabel();
        spectraJPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        spectrumBJPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        spectrumAJPanel = new javax.swing.JPanel();
        chromatogramJPanel = new javax.swing.JPanel();
        chromatogramPanelInfoJLabel = new javax.swing.JLabel();
        chromatogramPanelHelpJLabel = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        chromatogramAJPanel = new javax.swing.JPanel();
        isotopicDistributionJPanel = new javax.swing.JPanel();
        isotopicDistributionCalculatorInfoJLabel = new javax.swing.JLabel();
        isotopicDistributionCalculatorHelpJLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        peptideSequenceAJTextField = new javax.swing.JTextField();
        peptideSequenceBJTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        chargePeptideAJSpinner = new javax.swing.JSpinner();
        chargePeptideBJSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        silacLabelPeptideAJComboBox = new javax.swing.JComboBox();
        silacLabelPeptideBJComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        peptideAJScrollPane = new javax.swing.JScrollPane();
        peptideAJXTable = new org.jdesktop.swingx.JXTable();
        jLabel12 = new javax.swing.JLabel();
        peptideACompositionJTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        peptideAMzJTextField = new javax.swing.JTextField();
        peptideAColorJPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        peptideBJScrollPane = new javax.swing.JScrollPane();
        peptideBJXTable = new org.jdesktop.swingx.JXTable();
        jLabel14 = new javax.swing.JLabel();
        peptideBCompositionJTextField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        peptideBMzJTextField = new javax.swing.JTextField();
        peptideBColorJPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        isotopicDistributionAJPanel = new javax.swing.JPanel();
        proteinDigestionJPanel = new javax.swing.JPanel();
        proteinDigestionJLabel = new javax.swing.JLabel();
        inSilicoDigestionHelpJLabel = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        enzymesJComboBox = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        missedCleavagesJSpinner = new javax.swing.JSpinner();
        siteJTextField = new javax.swing.JTextField();
        inhibitorsJTextField = new javax.swing.JTextField();
        positionJTextField = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        lowerMassJSpinner = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        upperMassJSpinner = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        sequenceFileJTextField = new javax.swing.JTextField();
        browseJButton = new javax.swing.JButton();
        nextJButton = new javax.swing.JButton();
        sequenceJTabbedPane = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        peffAnnotationsJTable = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        proteinHeaderJTable = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        peffHeaderJTable = new javax.swing.JTable();
        peptidesJPanel = new javax.swing.JPanel();
        peptidesJScrollPane = new javax.swing.JScrollPane();
        peptidesJXTable = new org.jdesktop.swingx.JXTable();
        sequenceCoverageJPanel = new javax.swing.JPanel();
        proteinCoverageJScrollPane = new javax.swing.JScrollPane();
        proteinSequenceCoverageJEditorPane = new javax.swing.JEditorPane();

        jRadioButton1.setText("jRadioButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Compomics-Utilities");
        setMinimumSize(new java.awt.Dimension(1000, 750));

        jTabbedPane.setMinimumSize(new java.awt.Dimension(235, 102));
        jTabbedPane.setPreferredSize(new java.awt.Dimension(20, 680));

        informationJEditorPane.setContentType("text/html");
        informationJEditorPane.setEditable(false);
        informationJEditorPane.setMargin(new java.awt.Insets(30, 20, 10, 20));
        informationJEditorPane.setMinimumSize(new java.awt.Dimension(10, 10));
        informationJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                informationJEditorPaneHyperlinkUpdate(evt);
            }
        });
        informationJScrollPane.setViewportView(informationJEditorPane);

        org.jdesktop.layout.GroupLayout informationJPanelLayout = new org.jdesktop.layout.GroupLayout(informationJPanel);
        informationJPanel.setLayout(informationJPanelLayout);
        informationJPanelLayout.setHorizontalGroup(
            informationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(informationJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(informationJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1178, Short.MAX_VALUE)
                .addContainerGap())
        );
        informationJPanelLayout.setVerticalGroup(
            informationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(informationJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(informationJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane.addTab("Introduction to Compomics-Utilities", informationJPanel);

        spectrumJPanel.setRequestFocusEnabled(false);

        ionSelectionJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));

        aIonsJCheckBox.setText("a");
        aIonsJCheckBox.setToolTipText("Show a-ions");
        aIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        aIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        aIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        aIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aIonsJCheckBoxActionPerformed(evt);
            }
        });

        bIonsJCheckBox.setSelected(true);
        bIonsJCheckBox.setText("b");
        bIonsJCheckBox.setToolTipText("Show b-ions");
        bIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        bIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        bIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        bIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bIonsJCheckBoxActionPerformed(evt);
            }
        });

        cIonsJCheckBox.setText("c");
        cIonsJCheckBox.setToolTipText("Show c-ions");
        cIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        cIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        cIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        cIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cIonsJCheckBoxActionPerformed(evt);
            }
        });

        yIonsJCheckBox.setSelected(true);
        yIonsJCheckBox.setText("y");
        yIonsJCheckBox.setToolTipText("Show y-ions");
        yIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        yIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        yIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        yIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yIonsJCheckBoxActionPerformed(evt);
            }
        });

        xIonsJCheckBox.setText("x");
        xIonsJCheckBox.setToolTipText("Show x-ions");
        xIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        xIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        xIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        xIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xIonsJCheckBoxActionPerformed(evt);
            }
        });

        zIonsJCheckBox.setText("z");
        zIonsJCheckBox.setToolTipText("Show z-ions");
        zIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        zIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        zIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        zIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zIonsJCheckBoxActionPerformed(evt);
            }
        });

        chargeOneJCheckBox.setSelected(true);
        chargeOneJCheckBox.setText("+");
        chargeOneJCheckBox.setToolTipText("Show ions with charge 1");
        chargeOneJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        chargeOneJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        chargeOneJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        chargeOneJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chargeOneJCheckBoxActionPerformed(evt);
            }
        });

        chargeTwoJCheckBox.setText("++");
        chargeTwoJCheckBox.setToolTipText("Show ions with charge 2");
        chargeTwoJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        chargeTwoJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        chargeTwoJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        chargeTwoJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chargeTwoJCheckBoxActionPerformed(evt);
            }
        });

        chargeOverTwoJCheckBox.setText(">2");
        chargeOverTwoJCheckBox.setToolTipText("Show ions with charge >2");
        chargeOverTwoJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chargeOverTwoJCheckBoxActionPerformed(evt);
            }
        });

        H2OIonsJCheckBox.setText("H2O");
        H2OIonsJCheckBox.setToolTipText("Show ions with H2O loss");
        H2OIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        H2OIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        H2OIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        H2OIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                H2OIonsJCheckBoxActionPerformed(evt);
            }
        });

        NH3IonsJCheckBox.setText("NH3");
        NH3IonsJCheckBox.setToolTipText("Show ions with NH3 loss");
        NH3IonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NH3IonsJCheckBoxActionPerformed(evt);
            }
        });

        otherIonsJCheckBox.setText("Other");
        otherIonsJCheckBox.setToolTipText("Show other ions");
        otherIonsJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        otherIonsJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        otherIonsJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        otherIonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherIonsJCheckBoxActionPerformed(evt);
            }
        });

        profileSpectrumJCheckBox.setSelected(true);
        profileSpectrumJCheckBox.setText("Profile");
        profileSpectrumJCheckBox.setToolTipText("Select profile or centroid mode.\n");
        profileSpectrumJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        profileSpectrumJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        profileSpectrumJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        profileSpectrumJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileSpectrumJCheckBoxActionPerformed(evt);
            }
        });

        linkedSpectraJCheckBox.setSelected(true);
        linkedSpectraJCheckBox.setText("Linked");
        linkedSpectraJCheckBox.setToolTipText("<html>\nLink the spectra such that zooming in one <br>\nalso results in zooming in the other.\n</html>");
        linkedSpectraJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        linkedSpectraJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        linkedSpectraJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));

        exportJButton.setText("Export");
        exportJButton.setToolTipText("Export the spectra to a PDF file.");
        exportJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportJButtonActionPerformed(evt);
            }
        });

        allPeaksJCheckBox.setSelected(true);
        allPeaksJCheckBox.setText("All");
        allPeaksJCheckBox.setToolTipText("Display all peaks or just the annotated peaks");
        allPeaksJCheckBox.setEnabled(false);
        allPeaksJCheckBox.setMaximumSize(new java.awt.Dimension(39, 23));
        allPeaksJCheckBox.setMinimumSize(new java.awt.Dimension(39, 23));
        allPeaksJCheckBox.setPreferredSize(new java.awt.Dimension(39, 23));
        allPeaksJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allPeaksJCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout ionSelectionJPanelLayout = new org.jdesktop.layout.GroupLayout(ionSelectionJPanel);
        ionSelectionJPanel.setLayout(ionSelectionJPanelLayout);
        ionSelectionJPanelLayout.setHorizontalGroup(
            ionSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ionSelectionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ionSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(allPeaksJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSeparator9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .add(jSeparator5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .add(jSeparator6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .add(jSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, otherIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, NH3IonsJCheckBox)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, H2OIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, chargeOverTwoJCheckBox)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, chargeTwoJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, chargeOneJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, zIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, yIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, xIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, aIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, bIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.CENTER, cIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(profileSpectrumJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(linkedSpectraJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(exportJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                .addContainerGap())
        );

        ionSelectionJPanelLayout.linkSize(new java.awt.Component[] {H2OIonsJCheckBox, NH3IonsJCheckBox, aIonsJCheckBox, bIonsJCheckBox, cIonsJCheckBox, chargeOneJCheckBox, chargeOverTwoJCheckBox, chargeTwoJCheckBox, linkedSpectraJCheckBox, otherIonsJCheckBox, profileSpectrumJCheckBox, xIonsJCheckBox, yIonsJCheckBox, zIonsJCheckBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        ionSelectionJPanelLayout.linkSize(new java.awt.Component[] {jSeparator1, jSeparator2, jSeparator3, jSeparator4, jSeparator6}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        ionSelectionJPanelLayout.setVerticalGroup(
            ionSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ionSelectionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(aIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(xIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(yIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(zIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chargeOneJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chargeTwoJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chargeOverTwoJCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(H2OIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(NH3IonsJCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(otherIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(profileSpectrumJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allPeaksJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(linkedSpectraJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(exportJButton)
                .addContainerGap(73, Short.MAX_VALUE))
        );

        ionSelectionJPanelLayout.linkSize(new java.awt.Component[] {H2OIonsJCheckBox, NH3IonsJCheckBox, aIonsJCheckBox, bIonsJCheckBox, cIonsJCheckBox, chargeOneJCheckBox, chargeOverTwoJCheckBox, chargeTwoJCheckBox, otherIonsJCheckBox, xIonsJCheckBox, yIonsJCheckBox, zIonsJCheckBox}, org.jdesktop.layout.GroupLayout.VERTICAL);

        spectrumPanelInfoJLabel.setFont(spectrumPanelInfoJLabel.getFont().deriveFont((spectrumPanelInfoJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        spectrumPanelInfoJLabel.setText("Spectrum Panel makes is easy to visualize spectra with annotations. It supports zooming and calculation of distances representing amino acids.");

        spectrumPanelHelpJLabel.setForeground(new java.awt.Color(0, 0, 255));
        spectrumPanelHelpJLabel.setText("<html> <a href=\\\"dummy_link\">Click here for Help</a></html>");
        spectrumPanelHelpJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spectrumPanelHelpJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                spectrumPanelHelpJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                spectrumPanelHelpJLabelMouseExited(evt);
            }
        });

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("NH2-FQNALLVR-COOH"));
        jPanel7.setPreferredSize(new java.awt.Dimension(1050, 265));

        spectrumBJPanel.setBackground(new java.awt.Color(255, 255, 255));
        spectrumBJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        spectrumBJPanel.setLayout(new javax.swing.BoxLayout(spectrumBJPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumBJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1039, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumBJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("NH2-LC<Cmm*>TVATLR-COOH"));
        jPanel6.setPreferredSize(new java.awt.Dimension(1050, 266));

        spectrumAJPanel.setBackground(new java.awt.Color(255, 255, 255));
        spectrumAJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        spectrumAJPanel.setLayout(new javax.swing.BoxLayout(spectrumAJPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1039, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout spectraJPanelLayout = new org.jdesktop.layout.GroupLayout(spectraJPanel);
        spectraJPanel.setLayout(spectraJPanelLayout);
        spectraJPanelLayout.setHorizontalGroup(
            spectraJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1071, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1071, Short.MAX_VALUE)
        );
        spectraJPanelLayout.setVerticalGroup(
            spectraJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spectraJPanelLayout.createSequentialGroup()
                .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout spectrumJPanelLayout = new org.jdesktop.layout.GroupLayout(spectrumJPanel);
        spectrumJPanel.setLayout(spectrumJPanelLayout);
        spectrumJPanelLayout.setHorizontalGroup(
            spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spectrumJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, spectrumJPanelLayout.createSequentialGroup()
                        .add(spectraJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ionSelectionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, spectrumJPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(spectrumPanelInfoJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 397, Short.MAX_VALUE)
                        .add(spectrumPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        spectrumJPanelLayout.setVerticalGroup(
            spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spectrumJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spectraJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(ionSelectionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(spectrumPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(spectrumPanelInfoJLabel))
                .addContainerGap())
        );

        jTabbedPane.addTab("Spectrum Panel - Demo", spectrumJPanel);

        chromatogramPanelInfoJLabel.setFont(chromatogramPanelInfoJLabel.getFont().deriveFont((chromatogramPanelInfoJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        chromatogramPanelInfoJLabel.setText("Chromatogram Panel makes it easy to visualize chromatograms. It supports zooming and other user interactions. ");

        chromatogramPanelHelpJLabel.setForeground(new java.awt.Color(0, 0, 255));
        chromatogramPanelHelpJLabel.setText("<html> <a href=\\\"dummy_link\">Click here for Help</a></html>");
        chromatogramPanelHelpJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                chromatogramPanelHelpJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                chromatogramPanelHelpJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                chromatogramPanelHelpJLabelMouseExited(evt);
            }
        });

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Chromatogram"));

        chromatogramAJPanel.setBackground(new java.awt.Color(255, 255, 255));
        chromatogramAJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        chromatogramAJPanel.setForeground(new java.awt.Color(255, 255, 255));
        chromatogramAJPanel.setLayout(new javax.swing.BoxLayout(chromatogramAJPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(chromatogramAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1146, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(chromatogramAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout chromatogramJPanelLayout = new org.jdesktop.layout.GroupLayout(chromatogramJPanel);
        chromatogramJPanel.setLayout(chromatogramJPanelLayout);
        chromatogramJPanelLayout.setHorizontalGroup(
            chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chromatogramJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(chromatogramJPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(chromatogramPanelInfoJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 538, Short.MAX_VALUE)
                        .add(chromatogramPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        chromatogramJPanelLayout.setVerticalGroup(
            chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chromatogramJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chromatogramPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chromatogramPanelInfoJLabel))
                .addContainerGap())
        );

        jTabbedPane.addTab("Chromatogram Panel - Demo", chromatogramJPanel);

        isotopicDistributionJPanel.setPreferredSize(new java.awt.Dimension(1187, 652));

        isotopicDistributionCalculatorInfoJLabel.setFont(isotopicDistributionCalculatorInfoJLabel.getFont().deriveFont((isotopicDistributionCalculatorInfoJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        isotopicDistributionCalculatorInfoJLabel.setText("Isotopic Distribution Calculator calculates and visualizes the isotopic distribution of peptides.");

        isotopicDistributionCalculatorHelpJLabel.setForeground(new java.awt.Color(0, 0, 255));
        isotopicDistributionCalculatorHelpJLabel.setText("<html> <a href=\\\"dummy_link\">Click here for Help</a></html>");
        isotopicDistributionCalculatorHelpJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                isotopicDistributionCalculatorHelpJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                isotopicDistributionCalculatorHelpJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                isotopicDistributionCalculatorHelpJLabelMouseExited(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptide Properties"));

        jLabel1.setText("Peptide A:");

        jLabel5.setText("NH2-");

        jLabel3.setText("NH2-");

        peptideSequenceAJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideSequenceAJTextField.setText("PEPTIDERPEPTIDER");
        peptideSequenceAJTextField.setMaximumSize(new java.awt.Dimension(2147483647, 22));
        peptideSequenceAJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                peptideSequenceAJTextFieldKeyReleased(evt);
            }
        });

        peptideSequenceBJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideSequenceBJTextField.setText("PEPTIDERPEPTIDER");
        peptideSequenceBJTextField.setMaximumSize(new java.awt.Dimension(2147483647, 22));
        peptideSequenceBJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                peptideSequenceBJTextFieldKeyReleased(evt);
            }
        });

        jLabel7.setText("-COOH");

        jLabel6.setText("-COOH");

        chargePeptideAJSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        chargePeptideAJSpinner.setMaximumSize(new java.awt.Dimension(32767, 22));
        chargePeptideAJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chargePeptideAJSpinnerStateChanged(evt);
            }
        });

        chargePeptideBJSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        chargePeptideBJSpinner.setMaximumSize(new java.awt.Dimension(32767, 22));
        chargePeptideBJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chargePeptideBJSpinnerStateChanged(evt);
            }
        });

        jLabel8.setText("Charge:");

        jLabel9.setText("Charge:");

        jLabel10.setText("Neutrons:");
        jLabel10.setToolTipText("<html>\nThe number of additional neutrons. <br>\nFor example due to SILAC labeling.\n</html>");

        jLabel11.setText("Neutrons:");
        jLabel11.setToolTipText("<html>\nThe number of additional neutrons. <br>\nFor example due to SILAC labeling.\n</html>");

        silacLabelPeptideAJComboBox.setMaximumRowCount(20);
        silacLabelPeptideAJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "+1", "+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9", "+10" }));
        silacLabelPeptideAJComboBox.setSelectedIndex(2);
        silacLabelPeptideAJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                silacLabelPeptideAJComboBoxActionPerformed(evt);
            }
        });

        silacLabelPeptideBJComboBox.setMaximumRowCount(20);
        silacLabelPeptideBJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "+1", "+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9", "+10" }));
        silacLabelPeptideBJComboBox.setSelectedIndex(5);
        silacLabelPeptideBJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                silacLabelPeptideBJComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Peptide B:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(peptideSequenceAJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
                    .add(peptideSequenceBJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel7)
                        .add(18, 18, 18)
                        .add(jLabel9)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(chargePeptideBJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel11)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(silacLabelPeptideBJComboBox, 0, 73, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel6)
                        .add(18, 18, 18)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(chargePeptideAJSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(jLabel10)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(silacLabelPeptideAJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {silacLabelPeptideAJComboBox, silacLabelPeptideBJComboBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.linkSize(new java.awt.Component[] {chargePeptideAJSpinner, chargePeptideBJSpinner}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(13, 13, 13)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(silacLabelPeptideAJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(chargePeptideAJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8)
                    .add(jLabel6)
                    .add(peptideSequenceAJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel2)
                    .add(jLabel5)
                    .add(peptideSequenceBJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(jLabel7)
                    .add(jLabel9)
                    .add(chargePeptideBJSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .add(jLabel11)
                    .add(silacLabelPeptideBJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(13, 13, 13))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {chargePeptideAJSpinner, chargePeptideBJSpinner}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptide A"));
        jPanel2.setPreferredSize(new java.awt.Dimension(246, 215));

        peptideAJXTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Isotope", "% Total", "% Max"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        peptideAJXTable.setOpaque(false);
        peptideAJScrollPane.setViewportView(peptideAJXTable);

        jLabel12.setText("m/z:");
        jLabel12.setToolTipText("The mass over charge ratio of the peptide");

        peptideACompositionJTextField.setEditable(false);
        peptideACompositionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideACompositionJTextField.setToolTipText("The elemental composition of the peptide");

        jLabel13.setText("Comp.:");
        jLabel13.setToolTipText("The elemental composition of the peptide");

        peptideAMzJTextField.setEditable(false);
        peptideAMzJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideAMzJTextField.setToolTipText("The mass over charge ratio of the peptide");

        peptideAColorJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        peptideAColorJPanel.setToolTipText("The color used for Peptide A");

        org.jdesktop.layout.GroupLayout peptideAColorJPanelLayout = new org.jdesktop.layout.GroupLayout(peptideAColorJPanel);
        peptideAColorJPanel.setLayout(peptideAColorJPanelLayout);
        peptideAColorJPanelLayout.setHorizontalGroup(
            peptideAColorJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        peptideAColorJPanelLayout.setVerticalGroup(
            peptideAColorJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, peptideAJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel13)
                            .add(jLabel12))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                                .add(peptideAMzJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(peptideAColorJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(peptideACompositionJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(peptideAJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(peptideAColorJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel12)
                        .add(peptideAMzJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(peptideACompositionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {peptideAColorJPanel, peptideACompositionJTextField, peptideAMzJTextField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptide B"));
        jPanel4.setPreferredSize(new java.awt.Dimension(246, 215));

        peptideBJXTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Isotope", "% Total", "% Max"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        peptideBJXTable.setOpaque(false);
        peptideBJScrollPane.setViewportView(peptideBJXTable);

        jLabel14.setText("m/z:");
        jLabel14.setToolTipText("The mass over charge ratio of the peptide");

        peptideBCompositionJTextField.setEditable(false);
        peptideBCompositionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideBCompositionJTextField.setToolTipText("The elemental composition of the peptide");

        jLabel15.setText("Comp.:");
        jLabel15.setToolTipText("The elemental composition of the peptide");

        peptideBMzJTextField.setEditable(false);
        peptideBMzJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptideBMzJTextField.setToolTipText("The mass over charge ratio of the peptide");

        peptideBColorJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        peptideBColorJPanel.setToolTipText("The color used for Peptide B");

        org.jdesktop.layout.GroupLayout peptideBColorJPanelLayout = new org.jdesktop.layout.GroupLayout(peptideBColorJPanel);
        peptideBColorJPanel.setLayout(peptideBColorJPanelLayout);
        peptideBColorJPanelLayout.setHorizontalGroup(
            peptideBColorJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 21, Short.MAX_VALUE)
        );
        peptideBColorJPanelLayout.setVerticalGroup(
            peptideBColorJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, peptideBJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel15)
                            .add(jLabel14))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                                .add(peptideBMzJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(peptideBColorJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(peptideBCompositionJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(peptideBJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(peptideBColorJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(peptideBMzJTextField)
                        .add(jLabel14)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(peptideBCompositionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(new java.awt.Component[] {peptideBColorJPanel, peptideBCompositionJTextField, peptideBMzJTextField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Isotopic Distribution"));

        isotopicDistributionAJPanel.setBackground(new java.awt.Color(255, 255, 255));
        isotopicDistributionAJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        isotopicDistributionAJPanel.setLayout(new javax.swing.BoxLayout(isotopicDistributionAJPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(isotopicDistributionAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(isotopicDistributionAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout isotopicDistributionJPanelLayout = new org.jdesktop.layout.GroupLayout(isotopicDistributionJPanel);
        isotopicDistributionJPanel.setLayout(isotopicDistributionJPanelLayout);
        isotopicDistributionJPanelLayout.setHorizontalGroup(
            isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(isotopicDistributionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, isotopicDistributionJPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(isotopicDistributionCalculatorInfoJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 641, Short.MAX_VALUE)
                        .add(isotopicDistributionCalculatorHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, isotopicDistributionJPanelLayout.createSequentialGroup()
                        .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))))
                .addContainerGap())
        );
        isotopicDistributionJPanelLayout.setVerticalGroup(
            isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(isotopicDistributionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(isotopicDistributionJPanelLayout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                    .add(isotopicDistributionJPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(isotopicDistributionCalculatorHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(isotopicDistributionCalculatorInfoJLabel))
                .addContainerGap())
        );

        jTabbedPane.addTab("Isotopic Distribution Panel - Demo", isotopicDistributionJPanel);

        proteinDigestionJPanel.setPreferredSize(new java.awt.Dimension(20, 20));

        proteinDigestionJLabel.setFont(proteinDigestionJLabel.getFont().deriveFont((proteinDigestionJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        proteinDigestionJLabel.setText("In Silico Protein Digestion theoretically cleaves a protein sequence to calculate the number of peptides, the maximum protein coverage etc.");

        inSilicoDigestionHelpJLabel.setForeground(new java.awt.Color(0, 0, 255));
        inSilicoDigestionHelpJLabel.setText("<html> <a href=\\\"dummy_link\">Click here for Help</a></html>");
        inSilicoDigestionHelpJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inSilicoDigestionHelpJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                inSilicoDigestionHelpJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                inSilicoDigestionHelpJLabelMouseExited(evt);
            }
        });

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Enzyme"));

        jLabel4.setText("Enzyme:");

        enzymesJComboBox.setMaximumRowCount(20);
        enzymesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " - Select -" }));
        enzymesJComboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                enzymesJComboBoxPopupMenuWillBecomeVisible(evt);
            }
        });
        enzymesJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enzymesJComboBoxActionPerformed(evt);
            }
        });

        jLabel16.setText("Site:");

        jLabel17.setText("Inhibitors:");

        jLabel18.setText("Position:");

        jLabel19.setText("Missed Cleavages:");

        missedCleavagesJSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        missedCleavagesJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                missedCleavagesJSpinnerStateChanged(evt);
            }
        });

        siteJTextField.setEditable(false);
        siteJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        inhibitorsJTextField.setEditable(false);
        inhibitorsJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        positionJTextField.setEditable(false);
        positionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel20.setText("Lower:");

        lowerMassJSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(500), Integer.valueOf(0), null, Integer.valueOf(100)));
        lowerMassJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lowerMassJSpinnerStateChanged(evt);
            }
        });

        jLabel21.setText("Upper:");

        upperMassJSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(3500), Integer.valueOf(0), null, Integer.valueOf(100)));
        upperMassJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                upperMassJSpinnerStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel19)
                    .add(jLabel4)
                    .add(jLabel18)
                    .add(jLabel17)
                    .add(jLabel16))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(inhibitorsJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, siteJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, positionJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                    .add(enzymesJComboBox, 0, 319, Short.MAX_VALUE)
                    .add(jPanel11Layout.createSequentialGroup()
                        .add(missedCleavagesJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel20)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lowerMassJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel21)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(upperMassJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel11Layout.linkSize(new java.awt.Component[] {jLabel20, jLabel21}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel11Layout.linkSize(new java.awt.Component[] {lowerMassJSpinner, upperMassJSpinner}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(enzymesJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(siteJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel16))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inhibitorsJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel17))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel18)
                    .add(positionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(30, 30, 30)
                .add(jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(missedCleavagesJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel20)
                    .add(lowerMassJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel21)
                    .add(upperMassJSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Protein Sequence"));
        jPanel3.setPreferredSize(new java.awt.Dimension(200, 215));

        jLabel24.setText("Sequence File:");

        sequenceFileJTextField.setEditable(false);

        browseJButton.setText("Browse");
        browseJButton.setToolTipText("Find a PEFF or FASTA file");
        browseJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseJButtonActionPerformed(evt);
            }
        });

        nextJButton.setText("Next");
        nextJButton.setToolTipText("Go to the next sequence in the file");
        nextJButton.setEnabled(false);
        nextJButton.setPreferredSize(new java.awt.Dimension(55, 14));
        nextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextJButtonActionPerformed(evt);
            }
        });

        peffAnnotationsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Term", "F", "B", "  "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        peffAnnotationsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peffAnnotationsJTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(peffAnnotationsJTable);

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 691, Short.MAX_VALUE)
            .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 79, Short.MAX_VALUE)
            .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
        );

        sequenceJTabbedPane.addTab("Sequence Annotations", jPanel9);

        proteinHeaderJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Term", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(proteinHeaderJTable);

        org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 691, Short.MAX_VALUE)
            .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 79, Short.MAX_VALUE)
            .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
        );

        sequenceJTabbedPane.addTab("Protein Details", jPanel12);

        peffHeaderJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Term", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(peffHeaderJTable);

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 691, Short.MAX_VALUE)
            .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 79, Short.MAX_VALUE)
            .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
        );

        sequenceJTabbedPane.addTab("PEFF Header", jPanel10);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sequenceJTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel24)
                        .add(18, 18, 18)
                        .add(sequenceFileJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseJButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {browseJButton, nextJButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel24)
                    .add(sequenceFileJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseJButton))
                .add(18, 18, 18)
                .add(sequenceJTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {browseJButton, nextJButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        peptidesJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptides"));
        peptidesJPanel.setPreferredSize(new java.awt.Dimension(317, 277));

        peptidesJXTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Sequence", "Mass", "Start", "End"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        peptidesJXTable.setOpaque(false);
        peptidesJXTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peptidesJXTableMouseClicked(evt);
            }
        });
        peptidesJXTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                peptidesJXTableKeyReleased(evt);
            }
        });
        peptidesJScrollPane.setViewportView(peptidesJXTable);

        org.jdesktop.layout.GroupLayout peptidesJPanelLayout = new org.jdesktop.layout.GroupLayout(peptidesJPanel);
        peptidesJPanel.setLayout(peptidesJPanelLayout);
        peptidesJPanelLayout.setHorizontalGroup(
            peptidesJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(peptidesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(peptidesJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addContainerGap())
        );
        peptidesJPanelLayout.setVerticalGroup(
            peptidesJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(peptidesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(peptidesJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );

        sequenceCoverageJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sequence Coverage"));
        sequenceCoverageJPanel.setPreferredSize(new java.awt.Dimension(500, 341));
        sequenceCoverageJPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                sequenceCoverageJPanelComponentResized(evt);
            }
        });

        proteinCoverageJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        proteinSequenceCoverageJEditorPane.setContentType("text/html");
        proteinSequenceCoverageJEditorPane.setEditable(false);
        proteinSequenceCoverageJEditorPane.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n\n    </p>\r\n  </body>\r\n</html>\r\n");
        proteinSequenceCoverageJEditorPane.setMargin(new java.awt.Insets(10, 10, 10, 10));
        proteinSequenceCoverageJEditorPane.setMinimumSize(new java.awt.Dimension(22, 22));
        proteinSequenceCoverageJEditorPane.setPreferredSize(new java.awt.Dimension(22, 22));
        proteinSequenceCoverageJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                proteinSequenceCoverageJEditorPaneHyperlinkUpdate(evt);
            }
        });
        proteinCoverageJScrollPane.setViewportView(proteinSequenceCoverageJEditorPane);

        org.jdesktop.layout.GroupLayout sequenceCoverageJPanelLayout = new org.jdesktop.layout.GroupLayout(sequenceCoverageJPanel);
        sequenceCoverageJPanel.setLayout(sequenceCoverageJPanelLayout);
        sequenceCoverageJPanelLayout.setHorizontalGroup(
            sequenceCoverageJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sequenceCoverageJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(proteinCoverageJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
                .addContainerGap())
        );
        sequenceCoverageJPanelLayout.setVerticalGroup(
            sequenceCoverageJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sequenceCoverageJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(proteinCoverageJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout proteinDigestionJPanelLayout = new org.jdesktop.layout.GroupLayout(proteinDigestionJPanel);
        proteinDigestionJPanel.setLayout(proteinDigestionJPanelLayout);
        proteinDigestionJPanelLayout.setHorizontalGroup(
            proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(proteinDigestionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(proteinDigestionJPanelLayout.createSequentialGroup()
                        .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(peptidesJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                            .add(jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(sequenceCoverageJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, proteinDigestionJPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(proteinDigestionJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 415, Short.MAX_VALUE)
                        .add(inSilicoDigestionHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        proteinDigestionJPanelLayout.setVerticalGroup(
            proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(proteinDigestionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3, 0, 197, Short.MAX_VALUE)
                    .add(jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(8, 8, 8)
                .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sequenceCoverageJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                    .add(peptidesJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inSilicoDigestionHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(proteinDigestionJLabel))
                .addContainerGap())
        );

        jTabbedPane.addTab("In Silico Protein Digestion - Demo", proteinDigestionJPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1203, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Updates the ion coverage annotations.
     *
     * @param evt
     */
    private void aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aIonsJCheckBoxActionPerformed

        Iterator<Integer> iterator = linkedSpectrumPanels.keySet().iterator();

        while (iterator.hasNext()) {

            Integer key = iterator.next();
            SpectrumPanel currentSpectrumPanel = linkedSpectrumPanels.get(key);

            // update the ion coverage annotations
            currentSpectrumPanel.setAnnotations(allAnnotations.get(key));
            currentSpectrumPanel.validate();
            currentSpectrumPanel.repaint();
        }
}//GEN-LAST:event_aIonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void bIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bIonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_bIonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void cIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cIonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_cIonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void yIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yIonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_yIonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void xIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xIonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_xIonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void zIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zIonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_zIonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void chargeOneJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chargeOneJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_chargeOneJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void chargeTwoJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chargeTwoJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_chargeTwoJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void chargeOverTwoJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chargeOverTwoJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
}//GEN-LAST:event_chargeOverTwoJCheckBoxActionPerformed

    /**
     * Makes the hyperlinks active.
     *
     * @param evt
     */
    private void informationJEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_informationJEditorPaneHyperlinkUpdate
        if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ENTERED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.EXITED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.toString())) {
            if (evt.getDescription().startsWith("#")) {
                informationJEditorPane.scrollToReference(evt.getDescription());
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                BareBonesBrowserLaunch.openURL(evt.getDescription());
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_informationJEditorPaneHyperlinkUpdate

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void NH3IonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NH3IonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_NH3IonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void H2OIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_H2OIonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_H2OIonsJCheckBoxActionPerformed

    /**
     * @see #aIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent)
     */
    private void otherIonsJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherIonsJCheckBoxActionPerformed
        aIonsJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_otherIonsJCheckBoxActionPerformed

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void spectrumPanelHelpJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectrumPanelHelpJLabelMouseClicked
        openHelpDialog("/helpFiles/SpectrumPanel.html");
    }//GEN-LAST:event_spectrumPanelHelpJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the help link.
     *
     * @param evt
     */
    private void spectrumPanelHelpJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectrumPanelHelpJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_spectrumPanelHelpJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the help link.
     *
     * @param evt
     */
    private void spectrumPanelHelpJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectrumPanelHelpJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_spectrumPanelHelpJLabelMouseExited

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void chromatogramPanelHelpJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chromatogramPanelHelpJLabelMouseClicked
        openHelpDialog("/helpFiles/ChromatogramPanel.html");
    }//GEN-LAST:event_chromatogramPanelHelpJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the help link.
     *
     * @param evt
     */
    private void chromatogramPanelHelpJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chromatogramPanelHelpJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_chromatogramPanelHelpJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the help link.
     *
     * @param evt
     */
    private void chromatogramPanelHelpJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chromatogramPanelHelpJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_chromatogramPanelHelpJLabelMouseExited

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void isotopicDistributionCalculatorHelpJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_isotopicDistributionCalculatorHelpJLabelMouseClicked
        openHelpDialog("/helpFiles/IsotopicDistributionPanel.html");
    }//GEN-LAST:event_isotopicDistributionCalculatorHelpJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the help link.
     *
     * @param evt
     */
    private void isotopicDistributionCalculatorHelpJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_isotopicDistributionCalculatorHelpJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_isotopicDistributionCalculatorHelpJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the help link.
     *
     * @param evt
     */
    private void isotopicDistributionCalculatorHelpJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_isotopicDistributionCalculatorHelpJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_isotopicDistributionCalculatorHelpJLabelMouseExited

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void inSilicoDigestionHelpJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inSilicoDigestionHelpJLabelMouseClicked
        openHelpDialog("/helpFiles/InSilicoProteinDigestion.html");
    }//GEN-LAST:event_inSilicoDigestionHelpJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the help link.
     *
     * @param evt
     */
    private void inSilicoDigestionHelpJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inSilicoDigestionHelpJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_inSilicoDigestionHelpJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the help link.
     *
     * @param evt
     */
    private void inSilicoDigestionHelpJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inSilicoDigestionHelpJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_inSilicoDigestionHelpJLabelMouseExited

    /**
     * Updates the isotopic distributions according to the current values 
     * if the user clicks 'enter' in the peptide sequence field.
     *
     * @param evt
     */
    private void peptideSequenceAJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_peptideSequenceAJTextFieldKeyReleased

        chargePeptideAJSpinner.setEnabled(peptideSequenceAJTextField.getText().trim().length() != 0);
        silacLabelPeptideAJComboBox.setEnabled(peptideSequenceAJTextField.getText().length() != 0);

        chargePeptideBJSpinner.setEnabled(peptideSequenceBJTextField.getText().trim().length() != 0);
        peptideSequenceBJTextField.setEnabled(peptideSequenceAJTextField.getText().trim().length() != 0);
        silacLabelPeptideBJComboBox.setEnabled(peptideSequenceBJTextField.getText().length() != 0);

        if (peptideSequenceAJTextField.getText().trim().length() != 0) {
            setUpIsotopicDistributionPanelDemo();
        } else {
            // clear the results for peptide A
            peptideAMzJTextField.setText("");
            peptideACompositionJTextField.setText("");

            while (peptideAJXTable.getRowCount() > 0) {
                ((DefaultTableModel) peptideAJXTable.getModel()).removeRow(0);
            }

            // remove previuos isotopic distributions
            isotopicDistributionAJPanel.removeAll();
            isotopicDistributionAJPanel.validate();
            isotopicDistributionAJPanel.repaint();
        }
    }//GEN-LAST:event_peptideSequenceAJTextFieldKeyReleased

    /**
     * Updates the isotopic distributions according to the current values 
     * if the user clicks 'enter' in the peptide sequence field.
     *
     * @param evt
     */
    private void peptideSequenceBJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_peptideSequenceBJTextFieldKeyReleased

        chargePeptideBJSpinner.setEnabled(peptideSequenceBJTextField.getText().trim().length() != 0);
        silacLabelPeptideBJComboBox.setEnabled(peptideSequenceBJTextField.getText().length() != 0);

        if (peptideSequenceBJTextField.getText().trim().length() == 0) {
            // clear the results for peptide B
            peptideBMzJTextField.setText("");
            peptideBCompositionJTextField.setText("");

            while (peptideBJXTable.getRowCount() > 0) {
                ((DefaultTableModel) peptideBJXTable.getModel()).removeRow(0);
            }

            // remove previuos isotopic distributions
            //isotopicDistributionAJPanel.removeAll();
        }

        setUpIsotopicDistributionPanelDemo();
    }//GEN-LAST:event_peptideSequenceBJTextFieldKeyReleased

    /**
     * Updates the isotopic distributions according to the current values 
     * if the user clicks changes the peptide charge.
     *
     * @param evt
     */
    private void chargePeptideAJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chargePeptideAJSpinnerStateChanged
        setUpIsotopicDistributionPanelDemo();
    }//GEN-LAST:event_chargePeptideAJSpinnerStateChanged

    /**
     * Updates the isotopic distributions according to the current values 
     * if the user clicks changes the peptide charge.
     *
     * @param evt
     */
    private void chargePeptideBJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chargePeptideBJSpinnerStateChanged
        setUpIsotopicDistributionPanelDemo();
    }//GEN-LAST:event_chargePeptideBJSpinnerStateChanged

    /**
     * Turns the profile spectrum mode on or off.
     * 
     * @param evt
     */
    private void profileSpectrumJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileSpectrumJCheckBoxActionPerformed
        spectrumAPanel.setProfileMode(profileSpectrumJCheckBox.isSelected());
        spectrumAPanel.validate();
        spectrumAPanel.repaint();

        spectrumBPanel.setProfileMode(profileSpectrumJCheckBox.isSelected());
        spectrumBPanel.validate();
        spectrumBPanel.repaint();

        allPeaksJCheckBox.setEnabled(!profileSpectrumJCheckBox.isSelected());

        if (profileSpectrumJCheckBox.isSelected()) {
            allPeaksJCheckBox.setSelected(true);
        }

        allPeaksJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_profileSpectrumJCheckBoxActionPerformed

    /**
     * @see #setVariableComoboBoxPopupMenuWidth()
     */
    private void enzymesJComboBoxPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_enzymesJComboBoxPopupMenuWillBecomeVisible
        setVariableComoboBoxPopupMenuWidth(evt);
    }//GEN-LAST:event_enzymesJComboBoxPopupMenuWillBecomeVisible

    /**
     * Performs the cleaving of the sequence and displays the results.
     *
     * @param evt
     */
    private void enzymesJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enzymesJComboBoxActionPerformed

        // update the enzyme selection
        Enzyme selectedEnzyme = mascotEnzymeReader.getEnzyme(enzymesJComboBox.getSelectedItem().toString());

        if (selectedEnzyme.getCleavage() != null) {
            siteJTextField.setText(new String(selectedEnzyme.getCleavage()));
        } else {
            siteJTextField.setText("");
        }

        if (selectedEnzyme.getRestrict() != null) {
            inhibitorsJTextField.setText(new String(selectedEnzyme.getRestrict()));
        } else {
            inhibitorsJTextField.setText("");
        }

        positionJTextField.setText(selectedEnzyme.getPosition() == Enzyme.CTERM ? "C-terminal" : "N-terminal");

        // perform the digestion and display the results
        updateInSilicoDigestion();
    }//GEN-LAST:event_enzymesJComboBoxActionPerformed

    /**
     * Cleaves the current sequence according to the currently selected parameters 
     * and displays the results.
     */
    private void updateInSilicoDigestion() {

        // clear previous results from the peptide table
        while (peptidesJXTable.getRowCount() > 0) {
            ((DefaultTableModel) peptidesJXTable.getModel()).removeRow(0);
        }

        // and clear the peptide sequence coverage details
        proteinSequenceCoverageJEditorPane.setText("");

        // get the sequence and perform the digestion
        if (currentSequence.length() > 0) {

            // this will contain the sequence without indices, white space etc
            cleanProteinSequence = "";

            try {
                // get the currently selected enzyme
                Enzyme selectedEnzyme = mascotEnzymeReader.getEnzyme(enzymesJComboBox.getSelectedItem().toString());
                selectedEnzyme.setMiscleavages((Integer) missedCleavagesJSpinner.getValue());

                Protein[] cleavedPeptides;

                Protein protein = new Protein(currentSequence);
                String fullHeader = protein.getHeader().getFullHeaderWithAddenda();

                while (proteinHeaderJTable.getModel().getRowCount() > 0) {
                    ((DefaultTableModel) proteinHeaderJTable.getModel()).removeRow(0);
                }

                if (peffFormat) {
                    keyValuePairs = getHeaderValuePairsPeffFormat(fullHeader);
                } else {
                    keyValuePairs = new TreeMap<String, String>();
                    getHeaderValuesFastaFormat(protein.getHeader());
                }

                // get the tag colors
                selectedAnnotationTypes = new HashMap<String, Color>();

                for (int i = 0; i < peffAnnotationsJTable.getRowCount(); i++) {
                    
                    if ((Boolean) peffAnnotationsJTable.getValueAt(i, 4)) {
                        selectedAnnotationTypes.put((String) peffAnnotationsJTable.getValueAt(i, 1) + "_Foreground", peffAnnotationsColorMap.get((Integer) peffAnnotationsJTable.getValueAt(i, 2)));
                        selectedAnnotationTypes.put((String) peffAnnotationsJTable.getValueAt(i, 1) + "_Background", peffAnnotationsColorMap.get((Integer) peffAnnotationsJTable.getValueAt(i, 3)));
                    }
                }

                int width = getPreferredColumnWidth(proteinHeaderJTable, proteinHeaderJTable.getColumn("Term").getModelIndex(), 6);
                proteinHeaderJTable.getColumn("Term").setMinWidth(width);
                proteinHeaderJTable.getColumn("Term").setMaxWidth(width);

                cleavedPeptides = selectedEnzyme.cleave(protein);
                cleanProteinSequence = protein.getSequence().getSequence();

                int counter = 0;

                // cycle the peptides and add them to the peptide table
                for (int i = 0; i < cleavedPeptides.length; i++) {

                    // only add peptides within the current lower and upper mass limits
                    if (cleavedPeptides[i].getMass() >= ((Integer) lowerMassJSpinner.getValue()).intValue()
                            && cleavedPeptides[i].getMass() <= ((Integer) upperMassJSpinner.getValue()).intValue()) {
                        ((DefaultTableModel) peptidesJXTable.getModel()).addRow(new Object[]{
                                    ++counter,
                                    cleavedPeptides[i].getSequence().getSequence(),
                                    cleavedPeptides[i].getMass(),
                                    cleavedPeptides[i].getHeader().getStartLocation(),
                                    cleavedPeptides[i].getHeader().getEndLocation()});
                    }
                }

                // display the sequence coverage in the sequence coverage panel
                double coverage = formatProteinSequence();

                ((TitledBorder) sequenceCoverageJPanel.getBorder()).setTitle("Sequence Coverage (" + Util.roundDouble(coverage, 2) + "%)");
                sequenceCoverageJPanel.repaint();

            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error Parsing Protein Sequence", JOptionPane.ERROR_MESSAGE);
            }
        }

        // update the number of peptides in the peptide table count
        ((TitledBorder) peptidesJPanel.getBorder()).setTitle("Peptides (" + peptidesJXTable.getRowCount() + ")");
        peptidesJPanel.repaint();
    }

    /**
     * Extracts and returns the PEFF key-value pairs from the PEFF header.
     *
     * @param header    the header to extract the key-value pairs from
     * @return          the key-value pairs
     */
    private TreeMap<String, String> getHeaderValuePairsPeffFormat(String header) {

        TreeMap<String, String> map = new TreeMap<String, String>();

        String[] valuePairs = header.split("\\\\");

        if (valuePairs.length > 0) {

            String tempAccession = valuePairs[0].substring(1);
            
            if (tempAccession.indexOf(":") != -1) {
                tempAccession = tempAccession.substring(tempAccession.indexOf(":") + 1); 
            }
            
            map.put("Accession", tempAccession);

            ((DefaultTableModel) proteinHeaderJTable.getModel()).addRow(new Object[]{
                        proteinHeaderJTable.getRowCount() + 1,
                        "Accession",
                        tempAccession
                    });

            for (int i = 1; i < valuePairs.length; i++) {
                String temp = valuePairs[i].trim();
                
                String key = temp.substring(0, temp.indexOf("="));
                String values = temp.substring(temp.indexOf("=") + 1);
                
                map.put(key, values);

                ((DefaultTableModel) proteinHeaderJTable.getModel()).addRow(new Object[]{
                            proteinHeaderJTable.getRowCount() + 1,
                            key,
                            values
                        });
            }
        }

        return map;
    }

    /**
     * Extracts and returns the FASTA key-value pairs from the FASTA header.
     *
     * @param header    the header to extract the key-value pairs from
     * @return          the key-value pairs
     */
    private void getHeaderValuesFastaFormat(Header proteinHeader) {

        ((DefaultTableModel) proteinHeaderJTable.getModel()).addRow(new Object[]{
                    proteinHeaderJTable.getRowCount() + 1,
                    "Accession",
                    proteinHeader.getAccession()
                });

        ((DefaultTableModel) proteinHeaderJTable.getModel()).addRow(new Object[]{
                    proteinHeaderJTable.getRowCount() + 1,
                    "Database",
                    proteinHeader.getDatabaseType()
                });

        ((DefaultTableModel) proteinHeaderJTable.getModel()).addRow(new Object[]{
                    proteinHeaderJTable.getRowCount() + 1,
                    "ID",
                    proteinHeader.getID()
                });

        ((DefaultTableModel) proteinHeaderJTable.getModel()).addRow(new Object[]{
                    proteinHeaderJTable.getRowCount() + 1,
                    "Description",
                    proteinHeader.getDescription()
                });
    }

    /**
     * @see #updateInSilicoDigestion()
     */
    private void missedCleavagesJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_missedCleavagesJSpinnerStateChanged
        updateInSilicoDigestion();
    }//GEN-LAST:event_missedCleavagesJSpinnerStateChanged

    /**
     * @see #updateInSilicoDigestion()
     */
    private void lowerMassJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lowerMassJSpinnerStateChanged
        updateInSilicoDigestion();
    }//GEN-LAST:event_lowerMassJSpinnerStateChanged

    /**
     * @see #updateInSilicoDigestion()
     */
    private void upperMassJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_upperMassJSpinnerStateChanged
        updateInSilicoDigestion();
    }//GEN-LAST:event_upperMassJSpinnerStateChanged

    /**
     * @see #updateInSilicoDigestion()
     */
    /**
     * @see #formatProteinSequence(java.lang.String)
     */
    private void peptidesJXTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptidesJXTableMouseClicked
        if (peptidesJXTable.getSelectedRow() != -1) {
            formatProteinSequence();
        }
    }//GEN-LAST:event_peptidesJXTableMouseClicked

    /**
     * @see #formatProteinSequence(java.lang.String)
     */
    private void peptidesJXTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_peptidesJXTableKeyReleased
        if (peptidesJXTable.getSelectedRow() != -1) {
            formatProteinSequence();
        }
    }//GEN-LAST:event_peptidesJXTableKeyReleased

    /**
     * Updates the isotopic distributions according to the current values
     * if the user clicks changes the peptide charge.
     *
     * @param evt
     */
    private void silacLabelPeptideAJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_silacLabelPeptideAJComboBoxActionPerformed
        setUpIsotopicDistributionPanelDemo();
    }//GEN-LAST:event_silacLabelPeptideAJComboBoxActionPerformed

    /**
     * Updates the isotopic distributions according to the current values
     * if the user clicks changes the peptide charge.
     *
     * @param evt
     */
    private void silacLabelPeptideBJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_silacLabelPeptideBJComboBoxActionPerformed
        setUpIsotopicDistributionPanelDemo();
    }//GEN-LAST:event_silacLabelPeptideBJComboBoxActionPerformed

    /**
     * Export the spectra to a PDF file. To export to a different format see
     * the hints in the code. Supported formats are found in the ImageType
     * enum. To export other compoents change the input to the exportComponent
     * method.
     * 
     * @param evt
     */
    private void exportJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportJButtonActionPerformed

        JFileChooser chooser = new JFileChooser("user.home");
        chooser.setFileFilter(new PdfFileFilter()); // change if exporting to a different format

        int selection = chooser.showSaveDialog(this);

        if (selection == JFileChooser.APPROVE_OPTION) {
            try {
                String selectedFile = chooser.getSelectedFile().getAbsolutePath();

                if (!selectedFile.endsWith(ImageType.PDF.getExtension())) { // change if exporting to a different format
                    selectedFile += ImageType.PDF.getExtension();
                }

                boolean saveFile = true;

                if (new File(selectedFile).exists()) {
                    int option = JOptionPane.showConfirmDialog(this,
                            "The file " + selectedFile + " already exists. Overwrite?",
                            "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                    if (option != JOptionPane.YES_OPTION) {
                        saveFile = false;
                    }
                }

                if (saveFile) {
                    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    Export.exportComponent(spectraJPanel, spectraJPanel.getBounds(), new File(selectedFile), ImageType.PDF); // change if exporting to a different format
                    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    JOptionPane.showMessageDialog(this, "Spectra saved to " + selectedFile, "Spectra Saved", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Unable to export spectra: " + e.getMessage(), "Error Exporting Spectra", JOptionPane.INFORMATION_MESSAGE);
            } catch (TranscoderException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Unable to export spectra: " + e.getMessage(), "Error Exporting Spectra", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_exportJButtonActionPerformed

    /**
     * Update the sequence coverage pane when the frame width changes.
     * 
     * @param evt
     */
    private void sequenceCoverageJPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_sequenceCoverageJPanelComponentResized
        formatProteinSequence();
    }//GEN-LAST:event_sequenceCoverageJPanelComponentResized

    /**
     * Displays information about the modification (or similar) when a given
     * residue contains a modification.
     *
     * @param evt
     */
    private void proteinSequenceCoverageJEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_proteinSequenceCoverageJEditorPaneHyperlinkUpdate

        if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            proteinSequenceCoverageJEditorPane.setToolTipText(evt.getDescription());
        } else if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            
            String proteinAccession = keyValuePairs.get("Accession");
            String proteinUniProtUrl = "http://www.uniprot.org/uniprot/" + proteinAccession.trim() + "#section_features";
            
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            BareBonesBrowserLaunch.openURL(proteinUniProtUrl);
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        } else {
            proteinSequenceCoverageJEditorPane.setToolTipText(null);
        }

    }//GEN-LAST:event_proteinSequenceCoverageJEditorPaneHyperlinkUpdate

    /**
     * Opens a file browser where the user can select the PEFF or FASTA file
     * to display.
     *
     * @param evt
     */
    private void browseJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseJButtonActionPerformed

        JFileChooser fileChooser = new JFileChooser("user.home");

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File myFile) {
                return myFile.getName().toLowerCase().endsWith("fasta")
                        || myFile.getName().toLowerCase().endsWith("fast")
                        || myFile.getName().toLowerCase().endsWith("fas")
                        || myFile.getName().toLowerCase().endsWith("peff")
                        || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "FASTA or PEFF (.fasta, .peff)";
            }
        };

        fileChooser.setFileFilter(filter);

        int returnVal = fileChooser.showOpenDialog(this.getParent());

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            currentSequenceFile = fileChooser.getSelectedFile();
            sequenceFileJTextField.setText(currentSequenceFile.getPath());

            peffFormat = currentSequenceFile.getAbsolutePath().toLowerCase().endsWith(".peff");

            while (peffHeaderJTable.getModel().getRowCount() > 0) {
                ((DefaultTableModel) peffHeaderJTable.getModel()).removeRow(0);
            }

            while (peffAnnotationsJTable.getModel().getRowCount() > 0) {
                ((DefaultTableModel) peffAnnotationsJTable.getModel()).removeRow(0);
            }

            try {
                int colorCounter = 0;

                // set up the color maps
                peffAnnotationsColorMap = new HashMap<Integer, Color>();
                peffAnnotationsColorMap.put(colorCounter++, Color.WHITE);
                peffAnnotationsColorMap.put(colorCounter++, new Color(102, 153, 255));
                peffAnnotationsColorMap.put(colorCounter++, Color.WHITE);
                peffAnnotationsColorMap.put(colorCounter++, new Color(255, 102, 102));
                peffAnnotationsColorMap.put(colorCounter++, Color.WHITE);
                peffAnnotationsColorMap.put(colorCounter++, new Color(0, 204, 51));
                peffAnnotationsColorMap.put(colorCounter++, Color.WHITE);
                peffAnnotationsColorMap.put(colorCounter++, new Color(0, 204, 51));
                peffAnnotationsColorMap.put(colorCounter++, Color.WHITE);
                peffAnnotationsColorMap.put(colorCounter++, Color.ORANGE);


                // set up the protein inference tooltip map
                peffAnnotationsTooltipMap = new HashMap<Integer, String>();

                for (int i = 0; i < colorCounter; i++) {
                    peffAnnotationsTooltipMap.put(i, "Double click to change color");
                }

                peffAnnotationsJTable.getColumn("B").setCellRenderer(
                        new JSparklinesIntegerColorTableCellRenderer(Color.PINK, peffAnnotationsColorMap, peffAnnotationsTooltipMap));

                peffAnnotationsJTable.getColumn("F").setCellRenderer(
                        new JSparklinesIntegerColorTableCellRenderer(Color.PINK, peffAnnotationsColorMap, peffAnnotationsTooltipMap));

                colorCounter = 0;

                if (peffFormat) {

                    ((DefaultTableModel) peffAnnotationsJTable.getModel()).addRow(new Object[]{
                                (peffAnnotationsJTable.getRowCount() + 1),
                                "ModRes",
                                colorCounter++,
                                colorCounter++,
                                true
                            });

                    ((DefaultTableModel) peffAnnotationsJTable.getModel()).addRow(new Object[]{
                                (peffAnnotationsJTable.getRowCount() + 1),
                                "Variant",
                                colorCounter++,
                                colorCounter++,
                                true
                            });
                    
                    ((DefaultTableModel) peffAnnotationsJTable.getModel()).addRow(new Object[]{
                                (peffAnnotationsJTable.getRowCount() + 1),
                                "Signal",
                                colorCounter++,
                                colorCounter++,
                                false
                            });
                    
                    ((DefaultTableModel) peffAnnotationsJTable.getModel()).addRow(new Object[]{
                                (peffAnnotationsJTable.getRowCount() + 1),
                                "Site",
                                colorCounter++,
                                colorCounter++,
                                false
                            });
                    
                    ((DefaultTableModel) peffAnnotationsJTable.getModel()).addRow(new Object[]{
                                (peffAnnotationsJTable.getRowCount() + 1),
                                "Multiple",
                                colorCounter++,
                                colorCounter++,
                                true
                            });
                }

                sequenceJTabbedPane.setEnabledAt(2, peffFormat);

                FileReader peffFileReader = new FileReader(currentSequenceFile);
                currentPeffBufferedReader = new BufferedReader(peffFileReader);

                currentSequenceLine = currentPeffBufferedReader.readLine();

                // read the file header
                while (currentSequenceLine.startsWith("#")) {

                    if (peffFormat) {

                        if (currentSequenceLine.indexOf(" //") == -1) {

                            String term = currentSequenceLine.substring(currentSequenceLine.lastIndexOf("#") + 2, currentSequenceLine.lastIndexOf("=")).trim();
                            String value = currentSequenceLine.substring(currentSequenceLine.lastIndexOf("=") + 1).trim();

                            ((DefaultTableModel) peffHeaderJTable.getModel()).addRow(new Object[]{
                                        (peffHeaderJTable.getRowCount() + 1),
                                        term,
                                        value});

                            if (term.equalsIgnoreCase("SpecificValue")) {
                                // @TODO: extract values and map on sequence!!
                                //                            if (value.indexOf("")) {
                                //                                
                                //                            }
                            }
                        } else {
                            ((DefaultTableModel) peffHeaderJTable.getModel()).addRow(new Object[]{
                                        (peffHeaderJTable.getRowCount() + 1),
                                        "//"});
                        }
                    }

                    currentSequenceLine = currentPeffBufferedReader.readLine();
                }

                int width = getPreferredColumnWidth(peffHeaderJTable, peffHeaderJTable.getColumn("Term").getModelIndex(), 6);
                peffHeaderJTable.getColumn("Term").setMinWidth(width);
                peffHeaderJTable.getColumn("Term").setMaxWidth(width);

                // skip the empty space
                while (!currentSequenceLine.startsWith(">")) {
                    currentSequenceLine = currentPeffBufferedReader.readLine();
                }

                currentSequence = currentSequenceLine + "\n";
                currentSequenceLine = currentPeffBufferedReader.readLine();

                // read the first sequence
                while (currentSequenceLine != null && !currentSequenceLine.startsWith(">")) {

                    currentSequenceLine.trim();
                    currentSequenceLine = currentSequenceLine.replaceAll("\\W", "");

                    currentSequence += currentSequenceLine + "\n";
                    currentSequenceLine = currentPeffBufferedReader.readLine();
                }

                if (currentSequenceLine != null) {
                    nextJButton.setEnabled(true);
                } else {
                    nextJButton.setEnabled(false);
                }

                updateInSilicoDigestion();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_browseJButtonActionPerformed

    /**
     * Moves to the next sequence in the PEFF or FASTA file. If the end of the
     * file is reached an "end of file" message is displayed.
     *
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        try {

            currentSequence = currentSequenceLine + "\n";
            currentSequenceLine = currentPeffBufferedReader.readLine();

            // read the first sequence
            while (currentSequenceLine != null && !currentSequenceLine.startsWith(">")) {

                currentSequenceLine.trim();
                currentSequenceLine = currentSequenceLine.replaceAll("\\W", "");

                currentSequence += currentSequenceLine + "\n";
                currentSequenceLine = currentPeffBufferedReader.readLine();
            }

            if (currentSequenceLine == null) {
                nextJButton.setEnabled(false);
                JOptionPane.showMessageDialog(this, "End of file");
            } else {
                updateInSilicoDigestion();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * Turns the display of all or just the annotated peaks on or off.
     * 
     * @param evt 
     */
    private void allPeaksJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allPeaksJCheckBoxActionPerformed
        spectrumAPanel.showAnnotatedPeaksOnly(!allPeaksJCheckBox.isSelected());
        spectrumBPanel.showAnnotatedPeaksOnly(!allPeaksJCheckBox.isSelected());

        spectrumAPanel.repaint();
        spectrumBPanel.repaint();
    }//GEN-LAST:event_allPeaksJCheckBoxActionPerformed

    private void peffAnnotationsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peffAnnotationsJTableMouseClicked

        int row = peffAnnotationsJTable.getSelectedRow();
        int column = peffAnnotationsJTable.getSelectedColumn();

        if (row != -1) {

            if (column == 4) {
                
                selectedAnnotationTypes = new HashMap<String, Color>();
                
                for (int i=0; i<peffAnnotationsJTable.getRowCount(); i++) {
                    if ((Boolean) peffAnnotationsJTable.getValueAt(i, column)) {
                        selectedAnnotationTypes.put((String) peffAnnotationsJTable.getValueAt(i, 1) + "_Foreground", peffAnnotationsColorMap.get((Integer) peffAnnotationsJTable.getValueAt(i, 2)));
                        selectedAnnotationTypes.put((String) peffAnnotationsJTable.getValueAt(i, 1) + "_Background", peffAnnotationsColorMap.get((Integer) peffAnnotationsJTable.getValueAt(i, 3)));
                    }
                }

                formatProteinSequence();

            } else if (column == 2 || column == 3) { 
                
                Color tempColor = JColorChooser.showDialog(this, "Choose Color", peffAnnotationsColorMap.get((Integer) peffAnnotationsJTable.getValueAt(row, column)));
                
                if (tempColor != null) {
                     
                    peffAnnotationsColorMap.put((Integer) peffAnnotationsJTable.getValueAt(row, column), tempColor);

                    peffAnnotationsJTable.getColumn("B").setCellRenderer(
                            new JSparklinesIntegerColorTableCellRenderer(Color.PINK, peffAnnotationsColorMap, peffAnnotationsTooltipMap));

                    peffAnnotationsJTable.getColumn("F").setCellRenderer(
                            new JSparklinesIntegerColorTableCellRenderer(Color.PINK, peffAnnotationsColorMap, peffAnnotationsTooltipMap));

                    // get the tag colors
                    selectedAnnotationTypes = new HashMap<String, Color>();

                    for (int i = 0; i < peffAnnotationsJTable.getRowCount(); i++) {
                        if ((Boolean) peffAnnotationsJTable.getValueAt(i, 4)) {
                            selectedAnnotationTypes.put((String) peffAnnotationsJTable.getValueAt(i, 1) + "_Foreground", peffAnnotationsColorMap.get((Integer) peffAnnotationsJTable.getValueAt(i, 2)));
                            selectedAnnotationTypes.put((String) peffAnnotationsJTable.getValueAt(i, 1) + "_Background", peffAnnotationsColorMap.get((Integer) peffAnnotationsJTable.getValueAt(i, 3)));
                        }
                    }

                    formatProteinSequence();
                }
            }
        }
    }//GEN-LAST:event_peffAnnotationsJTableMouseClicked

    /**
     * Opens the help dialog.
     *
     * @param urlAsString the URL (as a String) of the help file to display
     */
    private void openHelpDialog(String urlAsString) {
        new HelpWindow(this, getClass().getResource(urlAsString));
    }

    /**
     * Formats the protein sequence such that both the covered parts of the sequence
     * and the peptide selected in the peptide table is highlighted, and any
     * modifications and variants of a PEFF file.
     * 
     * @return the sequence coverage in percent
     */
    public double formatProteinSequence() {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        double sequenceCoverage = 0.0;

        if (cleanProteinSequence != null) {

            int selectedPeptideStart = -1;
            int selectedPeptideEnd = -1;

            // find the start end end indices for the currently selected peptide, if any
            if (peptidesJXTable.getSelectedRow() != -1) {
                selectedPeptideStart = ((Integer) peptidesJXTable.getValueAt(peptidesJXTable.getSelectedRow(), 3)).intValue();
                selectedPeptideEnd = ((Integer) peptidesJXTable.getValueAt(peptidesJXTable.getSelectedRow(), 4)).intValue();
            }

            // an array containing the coverage index for each residue
            int[] coverage = new int[cleanProteinSequence.length() + 1];

            // iterate the peptide table and store the coverage for each peptide
            for (int i = 0; i < peptidesJXTable.getRowCount(); i++) {

                int tempPeptideStart = ((Integer) peptidesJXTable.getValueAt(i, 3)).intValue();
                int tempPeptideEnd = ((Integer) peptidesJXTable.getValueAt(i, 4)).intValue();

                for (int j = tempPeptideStart; j <= tempPeptideEnd; j++) {
                    coverage[j]++;
                }
            }

            // create and display the formatted sequence
            sequenceCoverage = ProteinSequencePane.formatProteinSequence(
                    proteinSequenceCoverageJEditorPane, cleanProteinSequence, selectedPeptideStart, selectedPeptideEnd, coverage,
                    keyValuePairs, selectedAnnotationTypes);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        return sequenceCoverage;
    }
    
    /**
     * Returns the selected neutral losses
     * @return an ArrayList containing the selected neutral losses 
     */
    private ArrayList<NeutralLoss> getNeutralLosses() {
        ArrayList<NeutralLoss> neutralLosses = new ArrayList<NeutralLoss>();
        if (H2OIonsJCheckBox.isSelected()) {
            neutralLosses.add(NeutralLoss.H2O);
        }
        if (NH3IonsJCheckBox.isSelected()) {
            neutralLosses.add(NeutralLoss.NH3);
        }
        if (otherIonsJCheckBox.isSelected()) {
            neutralLosses.add(NeutralLoss.CH4OS);
            neutralLosses.add(NeutralLoss.H3PO4);
            neutralLosses.add(NeutralLoss.HPO3);
        }
        return neutralLosses;
    }

    /**
     * Returns an arraylist of the currently selected fragment ion types.
     *
     * @return an arraylist of the currently selected fragment ion types
     */
    private ArrayList<PeptideFragmentIonType> getCurrentFragmentIonTypes() {

        ArrayList<PeptideFragmentIonType> fragmentIontypes = new ArrayList<PeptideFragmentIonType>();

        if (aIonsJCheckBox.isSelected()) {
            fragmentIontypes.add(PeptideFragmentIonType.A_ION);
        }

        if (bIonsJCheckBox.isSelected()) {
            fragmentIontypes.add(PeptideFragmentIonType.B_ION);
        }

        if (cIonsJCheckBox.isSelected()) {
            fragmentIontypes.add(PeptideFragmentIonType.C_ION);
        }

        if (xIonsJCheckBox.isSelected()) {
            fragmentIontypes.add(PeptideFragmentIonType.X_ION);
        }

        if (yIonsJCheckBox.isSelected()) {
            fragmentIontypes.add(PeptideFragmentIonType.Y_ION);
        }

        if (zIonsJCheckBox.isSelected()) {
            fragmentIontypes.add(PeptideFragmentIonType.Z_ION);
        }

        if (otherIonsJCheckBox.isSelected()) {
            fragmentIontypes.add(PeptideFragmentIonType.IMMONIUM);
            fragmentIontypes.add(PeptideFragmentIonType.PRECURSOR_ION);

        }

        return fragmentIontypes;
    }

    /**
     * Makes sure that the combox is always wide enough to
     * display the longest element.
     */
    private void setVariableComoboBoxPopupMenuWidth(javax.swing.event.PopupMenuEvent evt) {
        JComboBox box = (JComboBox) evt.getSource();
        Object comp = box.getUI().getAccessibleChild(box, 0);

        if (!(comp instanceof JPopupMenu)) {
            return;
        }

        JPopupMenu popupMenu = (JPopupMenu) comp;

        JComponent scrollPane = (JComponent) popupMenu.getComponent(0);
        Dimension size = new Dimension();

        if (box.getPreferredSize().width > scrollPane.getPreferredSize().width) {
            size.width = box.getPreferredSize().width;
            size.height = scrollPane.getPreferredSize().height;
            scrollPane.setPreferredSize(size);
            scrollPane.setMaximumSize(size);
        }
    }

    /**
     * Starts the UtilitiesDemo.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                UtilitiesGUIDefaults.setLookAndFeel();
                new UtilitiesDemo();
            }
        });
    }

    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of compomics-utilities
     */
    public String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("compomics-utilities.properties");
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("compomics-utilities.version");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox H2OIonsJCheckBox;
    private javax.swing.JCheckBox NH3IonsJCheckBox;
    private javax.swing.JCheckBox aIonsJCheckBox;
    private javax.swing.JCheckBox allPeaksJCheckBox;
    private javax.swing.JCheckBox bIonsJCheckBox;
    private javax.swing.JButton browseJButton;
    private javax.swing.JCheckBox cIonsJCheckBox;
    private javax.swing.JCheckBox chargeOneJCheckBox;
    private javax.swing.JCheckBox chargeOverTwoJCheckBox;
    private javax.swing.JSpinner chargePeptideAJSpinner;
    private javax.swing.JSpinner chargePeptideBJSpinner;
    private javax.swing.JCheckBox chargeTwoJCheckBox;
    private javax.swing.JPanel chromatogramAJPanel;
    private javax.swing.JPanel chromatogramJPanel;
    private javax.swing.JLabel chromatogramPanelHelpJLabel;
    private javax.swing.JLabel chromatogramPanelInfoJLabel;
    private javax.swing.JComboBox enzymesJComboBox;
    private javax.swing.JButton exportJButton;
    private javax.swing.JLabel inSilicoDigestionHelpJLabel;
    private javax.swing.JEditorPane informationJEditorPane;
    private javax.swing.JPanel informationJPanel;
    private javax.swing.JScrollPane informationJScrollPane;
    private javax.swing.JTextField inhibitorsJTextField;
    private javax.swing.JPanel ionSelectionJPanel;
    private javax.swing.JPanel isotopicDistributionAJPanel;
    private javax.swing.JLabel isotopicDistributionCalculatorHelpJLabel;
    private javax.swing.JLabel isotopicDistributionCalculatorInfoJLabel;
    private javax.swing.JPanel isotopicDistributionJPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JCheckBox linkedSpectraJCheckBox;
    private javax.swing.JSpinner lowerMassJSpinner;
    private javax.swing.JSpinner missedCleavagesJSpinner;
    private javax.swing.JButton nextJButton;
    private javax.swing.JCheckBox otherIonsJCheckBox;
    private javax.swing.JTable peffAnnotationsJTable;
    private javax.swing.JTable peffHeaderJTable;
    private javax.swing.JPanel peptideAColorJPanel;
    private javax.swing.JTextField peptideACompositionJTextField;
    private javax.swing.JScrollPane peptideAJScrollPane;
    private org.jdesktop.swingx.JXTable peptideAJXTable;
    private javax.swing.JTextField peptideAMzJTextField;
    private javax.swing.JPanel peptideBColorJPanel;
    private javax.swing.JTextField peptideBCompositionJTextField;
    private javax.swing.JScrollPane peptideBJScrollPane;
    private org.jdesktop.swingx.JXTable peptideBJXTable;
    private javax.swing.JTextField peptideBMzJTextField;
    private javax.swing.JTextField peptideSequenceAJTextField;
    private javax.swing.JTextField peptideSequenceBJTextField;
    private javax.swing.JPanel peptidesJPanel;
    private javax.swing.JScrollPane peptidesJScrollPane;
    private org.jdesktop.swingx.JXTable peptidesJXTable;
    private javax.swing.JTextField positionJTextField;
    private javax.swing.JCheckBox profileSpectrumJCheckBox;
    private javax.swing.JScrollPane proteinCoverageJScrollPane;
    private javax.swing.JLabel proteinDigestionJLabel;
    private javax.swing.JPanel proteinDigestionJPanel;
    private javax.swing.JTable proteinHeaderJTable;
    private javax.swing.JEditorPane proteinSequenceCoverageJEditorPane;
    private javax.swing.JPanel sequenceCoverageJPanel;
    private javax.swing.JTextField sequenceFileJTextField;
    private javax.swing.JTabbedPane sequenceJTabbedPane;
    private javax.swing.JComboBox silacLabelPeptideAJComboBox;
    private javax.swing.JComboBox silacLabelPeptideBJComboBox;
    private javax.swing.JTextField siteJTextField;
    private javax.swing.JPanel spectraJPanel;
    private javax.swing.JPanel spectrumAJPanel;
    private javax.swing.JPanel spectrumBJPanel;
    private javax.swing.JPanel spectrumJPanel;
    private javax.swing.JLabel spectrumPanelHelpJLabel;
    private javax.swing.JLabel spectrumPanelInfoJLabel;
    private javax.swing.JSpinner upperMassJSpinner;
    private javax.swing.JCheckBox xIonsJCheckBox;
    private javax.swing.JCheckBox yIonsJCheckBox;
    private javax.swing.JCheckBox zIonsJCheckBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets the preferred width of the column specified by vColIndex. The column
     * will be just wide enough to show the column head and the widest cell in the 
     * column. Margin pixels are added to the left and right (resulting in an additional 
     * width of 2*margin pixels.
     * 
     * @param table         the table
     * @param colIndex      the colum index
     * @param margin        the margin to add
     * @return              the prefereed width of the column 
     */
    public int getPreferredColumnWidth(JTable table, int colIndex, int margin) {

        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(colIndex);
        int width = 0;

        // get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }

        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        // get maximum width of column data
        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, colIndex);
            comp = renderer.getTableCellRendererComponent(
                    table, table.getValueAt(r, colIndex), false, false, r, colIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // add margin
        width += 2 * margin;

        return width;
    }
}
