/*
 * UtilitiesDemo.java
 *
 * Created on 16.aug.2010, 14:49:52
 */
package com.compomics.util.examples;

import com.compomics.util.gui.events.RescalingEvent;
import com.compomics.util.gui.interfaces.SpectrumPanelListener;
import com.compomics.util.gui.spectrum.ChromatogramPanel;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.compomics.util.io.PklFile;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This class serves as a demo of how the compomics-utilities library can be
 * used in other projects. It contains several demos, and is also the frame shown
 * if the compomics-utilities jar file is double clicked or ran from the command 
 * line.
 *
 * @author Harald Barsnes
 */
public class UtilitiesDemo extends javax.swing.JFrame {

    private HashMap<Integer, SpectrumPanel> linkedSpectrumPanels;
    private HashMap<Integer, Vector<DefaultSpectrumAnnotation>> allAnnotations;
    private SpectrumPanel spectrumAPanel;
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
     * Creates a new UtilitiesDemo frame and makes it visible.
     */
    public UtilitiesDemo() {
        initComponents();

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/icons/compomics-utilities.png")));

        setUpSpectrumPanelDemo();
        setUpChromatogramPanelDemo();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Error Reading Chromatogram Data", "Error reading chromatogram data:\n" + e.toString(), WIDTH);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error Reading Chromatogram Data", "Error reading chromatogram data:\n" + e.toString(), WIDTH);
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

        // remove the default spectrum panel border, given that our
        // spectrum panel already have a border
        chromatogramPanel.setBorder(null);

        // add the spectrum panel to the frame
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
            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.006822999999997137, determineColorOfPeak("y1"), "y1"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(389.251235, 4.6299999996790575E-4, determineColorOfPeak("y3"), "y3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(460.288345, -0.003290999999990163, determineColorOfPeak("y4"), "y4"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(559.356755, -2.4200000007112976E-4, determineColorOfPeak("y5"), "y5"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(660.404435, -0.002686000000039712, determineColorOfPeak("y6"), "y6"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(820.4350840000001, 8.09999999091815E-5, determineColorOfPeak("y7"), "y7"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(271.177006, -0.003444999999999254, determineColorOfPeak("y[2]-NH3"), "y[2]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(288.203555, -0.002484999999978754, determineColorOfPeak("y2"), "y2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -5.020000000115488E-4, determineColorOfPeak("y[1]-NH3"), "y[1]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(372.224686, 0.001030999999954929, determineColorOfPeak("y[3]-NH3"), "y[3]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(443.261796, 0.0025039999999876272, determineColorOfPeak("y[4]-NH3"), "y[4]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(274.12253400000003, 0.00181899999995494, determineColorOfPeak("b2"), "b2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(458.20561749999996, 0.05911150000002863, determineColorOfPeak("Prec-H2O 2+"), "Prec-H2O 2+"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(129.000000, 0.10726900000000228, determineColorOfPeak("iR"), "iR"));

//            // ------------test: annotation second spectrum--------------
//            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.010621000000014647, determineColorOfPeak("y1"), "y1*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(387.27196499999997, -0.0044499999999629836, determineColorOfPeak("y3"), "y3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(500.356025, -0.002353999999968437, determineColorOfPeak("y4"), "y4*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(571.393135, -0.004269000000022061, determineColorOfPeak("y5"), "y5*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(685.436065, -0.013534999999933461, determineColorOfPeak("y6"), "y6*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(813.494645, 0.005993999999986954, determineColorOfPeak("y7"), "y7*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(257.161356, -0.007209999999986394, determineColorOfPeak("y[2]-NH3"), "y[2]-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(370.245416, -9.159999999610591E-4, determineColorOfPeak("y[3]-NH3"), "y[3]-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(796.468096, 0.0018540000000939472, determineColorOfPeak("y[7]-NH3"), "y[7]-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(274.187905, -0.004702000000008866, determineColorOfPeak("y2"), "y2*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -0.008444000000025653, determineColorOfPeak("y[1]-NH3"), "y[1]-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(668.4095159999999, 0.0019680000000334985, determineColorOfPeak("y[6]-NH3"), "y[6]-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(276.134815, -0.002712000000030912, determineColorOfPeak("b2"), "b2*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(259.108266, -0.004803000000038082, determineColorOfPeak("b[2]-NH3"), "b[2]-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(242.164738, -0.08587800000000811, determineColorOfPeak("y[4]++-NH3"), "y[4]++-NH3*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(129, 0.09981500000000665, determineColorOfPeak("iR"), "iR*"));
//            currentAnnotations.add(new DefaultSpectrumAnnotation(120, 0.08159999999999457, determineColorOfPeak("iF"), "iF*"));
//            // ------------test: annotation second spectrum--------------

            // store the annotations for later use
            allAnnotations.put(new Integer(0), currentAnnotations);
            spectrumAPanel.setAnnotations(currentAnnotations);

            // store a unique reference to each spectrum panel for linking purposes
            linkedSpectrumPanels.put(new Integer(0), spectrumAPanel);

            // remove the default spectrum panel border, given that our
            // spectrum panel already have a border
            spectrumAPanel.setBorder(null);

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
            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.010621000000014647, determineColorOfPeak("y1"), "y1"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(387.27196499999997, -0.0044499999999629836, determineColorOfPeak("y3"), "y3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(500.356025, -0.002353999999968437, determineColorOfPeak("y4"), "y4"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(571.393135, -0.004269000000022061, determineColorOfPeak("y5"), "y5"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(685.436065, -0.013534999999933461, determineColorOfPeak("y6"), "y6"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(813.494645, 0.005993999999986954, determineColorOfPeak("y7"), "y7"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(257.161356, -0.007209999999986394, determineColorOfPeak("y[2]-NH3"), "y[2]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(370.245416, -9.159999999610591E-4, determineColorOfPeak("y[3]-NH3"), "y[3]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(796.468096, 0.0018540000000939472, determineColorOfPeak("y[7]-NH3"), "y[7]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(274.187905, -0.004702000000008866, determineColorOfPeak("y2"), "y2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -0.008444000000025653, determineColorOfPeak("y[1]-NH3"), "y[1]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(668.4095159999999, 0.0019680000000334985, determineColorOfPeak("y[6]-NH3"), "y[6]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(276.134815, -0.002712000000030912, determineColorOfPeak("b2"), "b2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(259.108266, -0.004803000000038082, determineColorOfPeak("b[2]-NH3"), "b[2]-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(242.164738, -0.08587800000000811, determineColorOfPeak("y[4]++-NH3"), "y[4]++-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(129, 0.09981500000000665, determineColorOfPeak("iR"), "iR"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(120, 0.08159999999999457, determineColorOfPeak("iF"), "iF"));

            // store the annotations for later use
            allAnnotations.put(new Integer(1), currentAnnotations);
            spectrumBPanel.setAnnotations(currentAnnotations);

            // store a unique reference to each spectrum panel for linking purposes
            linkedSpectrumPanels.put(new Integer(1), spectrumBPanel);

            // remove the default spectrum panel border, given that our
            // spectrum panel already have a border
            spectrumBPanel.setBorder(null);

            // add the spectrum panel to the frame
            spectrumBJPanel.add(spectrumBPanel);
            spectrumBJPanel.validate();
            spectrumBJPanel.repaint();

            // update the fragment ions
            aIonsJCheckBoxActionPerformed(null);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error Reading Spectrum Data", "Error reading spectrum data:\n" + e.toString(), WIDTH);
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
                spectrumPanelMaxPadding, false, false, false, false, 2, profileMode);

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

        } else if (peakLabel.startsWith("Prec")) { // precursor

            // red
            currentColor = Color.gray; // Color.red is used in MascotDatFile

        } else if (peakLabel.startsWith("i")) { // immonimum ion
            // grey
            currentColor = Color.gray;
        }

        return currentColor;
    }

    /**
     * Returns the path to the jar file.
     *
     * @return
     */
    private String getJarFilePath() {
        String path = this.getClass().getResource("UtilitiesDemo.class").getPath();
        path = path.substring(5, path.lastIndexOf("/utilities-"));
        path = path.replace("%20", " ");
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
        spectrumPanelInfoJLabel = new javax.swing.JLabel();
        profileSelectionJPanel = new javax.swing.JPanel();
        profileSpectrumJCheckBox = new javax.swing.JCheckBox();
        linkedSelectionJPanel = new javax.swing.JPanel();
        linkedSpectraJCheckBox = new javax.swing.JCheckBox();
        spectrumAJTextField = new javax.swing.JTextField();
        spectrumBJTextField = new javax.swing.JTextField();
        spectrumAJPanel = new javax.swing.JPanel();
        spectrumBJPanel = new javax.swing.JPanel();
        spectrumPanelHelpJLabel = new javax.swing.JLabel();
        chromatogramJPanel = new javax.swing.JPanel();
        chromatogramAJPanel = new javax.swing.JPanel();
        chromatogramPanelInfoJLabel = new javax.swing.JLabel();
        chromatogramPanelHelpJLabel = new javax.swing.JLabel();
        isotopicDistributionJPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        isotopicDistributionCalculatorInfoJLabel = new javax.swing.JLabel();
        isotopicDistributionCalculatorHelpJLabel = new javax.swing.JLabel();
        proteinDigestionJPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        proteinDigestionJLabel = new javax.swing.JLabel();
        inSilicoDigestionHelpJLabel = new javax.swing.JLabel();

        jRadioButton1.setText("jRadioButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Compomics-Utilities Demo");
        setMinimumSize(new java.awt.Dimension(900, 0));

        jTabbedPane.setMinimumSize(new java.awt.Dimension(235, 102));

        informationJEditorPane.setContentType("text/html");
        informationJEditorPane.setEditable(false);
        informationJEditorPane.setText("<html>\r\n  <head>\r\n\n  </head>\r\n  <body>\r\n\t\n<h1>Compomics-Utilities</h1>\n\n    <p style=\"margin-top: 0\">\r\n      \r\tThe <a href=\"http://www.compomics.com\">Computational Omics Group</a> at the <a href=\"http://www.ugent.be/en\">University of Ghent</a> develops various bioinformatics tools for analyzing omics data.\n\t<br><br>\n\tCompomics-utilities is a library containing code shared by many of our research projects, amongst others containing panels <br>\n\tfor visualizing spectra and chromatograms  and objects for representing peptides and proteins etc.  We believe that this library<br>\n\tcan be of use to other research groups doing computational proteomics, and have therefore made it available as open source.\n\t<br><br>\n\tThis demo contains four examples of how the library can be used. For the complete source code of the examples, see the<br>\n\t<i>com.compomics.util.examples</i> package. Click the tabs at the top to select one of the demos. \n\t<br><br>\n\tAdditional info can be found at the project's <a href=\"http://googlecode.com\">Google Code</a> home page: <a href=\"http://compomics-utilities.googlecode.com\">http://compomics-utilities.googlecode.com</a>.\n    </p>\r\n\t<br><br><br>\n\t<b>The Computational Omics Group is grateful to be supported by:<b>\n\t<br><br>\n\t<a href=\"http://www.compomics.com\"><img src=\"http://genesis.ugent.be/public_data/image/compomics.png\" border=\"0\" width=\"222\" height=\"111\"></a>\n\t<a href=\"http://www.ugent.be/en\"><img src=\"http://genesis.ugent.be/public_data/image/ugent.png\" border=\"0\"></a>\n\t<a href=\"http://www.vib.be\"><img src=\"http://genesis.ugent.be/public_data/image/vib.png\" border=\"0\"></a>\n\t<a href=\"http://java.com/en\"><img src=\" http://genesis.ugent.be/public_data/image/java.png\" border=\"0\"></a>\n\t<a href=\"http://maven.apache.org\"><img src=\" http://genesis.ugent.be/public_data/image/maven.png\" border=\"0\"></a>\n\t<a href=\"http://www.jetbrains.com/idea/\"><img src=\"http://genesis.ugent.be/public_data/image/intelliJ.png\" border=\"0\"></a>\n\t<a href=\"http://www.yourkit.com/\"><img src=\"http://genesis.ugent.be/public_data/image/yourkit.png\" border=\"0\"></a>\n  </body>\r\n</html>\r\n");
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
                .add(informationJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1163, Short.MAX_VALUE)
                .addContainerGap())
        );
        informationJPanelLayout.setVerticalGroup(
            informationJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(informationJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(informationJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane.addTab("Introduction to Compomics-Utilities", informationJPanel);

        spectrumJPanel.setRequestFocusEnabled(false);

        ionSelectionJPanel.setBackground(new java.awt.Color(255, 255, 255));
        ionSelectionJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        aIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        bIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        cIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        yIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        xIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        zIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        chargeOneJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        chargeTwoJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        chargeOverTwoJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        chargeOverTwoJCheckBox.setText(">2");
        chargeOverTwoJCheckBox.setToolTipText("Show ions with charge >2");
        chargeOverTwoJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chargeOverTwoJCheckBoxActionPerformed(evt);
            }
        });

        H2OIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        NH3IonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        NH3IonsJCheckBox.setText("NH3");
        NH3IonsJCheckBox.setToolTipText("Show ions with NH3 loss");
        NH3IonsJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NH3IonsJCheckBoxActionPerformed(evt);
            }
        });

        otherIonsJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
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

        org.jdesktop.layout.GroupLayout ionSelectionJPanelLayout = new org.jdesktop.layout.GroupLayout(ionSelectionJPanel);
        ionSelectionJPanel.setLayout(ionSelectionJPanelLayout);
        ionSelectionJPanelLayout.setHorizontalGroup(
            ionSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ionSelectionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ionSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chargeOverTwoJCheckBox)
                    .add(chargeTwoJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(chargeOneJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(zIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(yIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(xIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(bIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(aIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(NH3IonsJCheckBox)
                    .add(H2OIonsJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .add(otherIonsJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        ionSelectionJPanelLayout.linkSize(new java.awt.Component[] {H2OIonsJCheckBox, NH3IonsJCheckBox, aIonsJCheckBox, bIonsJCheckBox, cIonsJCheckBox, chargeOneJCheckBox, chargeOverTwoJCheckBox, chargeTwoJCheckBox, otherIonsJCheckBox, xIonsJCheckBox, yIonsJCheckBox, zIonsJCheckBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        ionSelectionJPanelLayout.linkSize(new java.awt.Component[] {jSeparator1, jSeparator2, jSeparator3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

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
                .add(11, 11, 11)
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ionSelectionJPanelLayout.linkSize(new java.awt.Component[] {H2OIonsJCheckBox, NH3IonsJCheckBox, aIonsJCheckBox, bIonsJCheckBox, cIonsJCheckBox, chargeOneJCheckBox, chargeOverTwoJCheckBox, chargeTwoJCheckBox, otherIonsJCheckBox, xIonsJCheckBox, yIonsJCheckBox, zIonsJCheckBox}, org.jdesktop.layout.GroupLayout.VERTICAL);

        spectrumPanelInfoJLabel.setFont(spectrumPanelInfoJLabel.getFont().deriveFont((spectrumPanelInfoJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        spectrumPanelInfoJLabel.setText("Spectrum Panel makes is easy to visualize spectra with annotations. It supports zooming and calculation of distances representing amino acids.");

        profileSelectionJPanel.setBackground(new java.awt.Color(255, 255, 255));
        profileSelectionJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        profileSpectrumJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        profileSpectrumJCheckBox.setSelected(true);
        profileSpectrumJCheckBox.setText("Profile");
        profileSpectrumJCheckBox.setToolTipText("<html>\nIf selected the spectra are displayed in <br>\nprofile mode, i.e., connecting the peaks. <br>\nOtherwise only the peaks are shown.\n</html>");
        profileSpectrumJCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        profileSpectrumJCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        profileSpectrumJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileSpectrumJCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout profileSelectionJPanelLayout = new org.jdesktop.layout.GroupLayout(profileSelectionJPanel);
        profileSelectionJPanel.setLayout(profileSelectionJPanelLayout);
        profileSelectionJPanelLayout.setHorizontalGroup(
            profileSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, profileSelectionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(profileSpectrumJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                .addContainerGap())
        );
        profileSelectionJPanelLayout.setVerticalGroup(
            profileSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(profileSelectionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(profileSpectrumJCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        linkedSelectionJPanel.setBackground(new java.awt.Color(255, 255, 255));
        linkedSelectionJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        linkedSpectraJCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        linkedSpectraJCheckBox.setSelected(true);
        linkedSpectraJCheckBox.setText("Linked");
        linkedSpectraJCheckBox.setToolTipText("<html>\nIf selected the two spectra are linked, <br>\nmeaning that zooming in one spectrum <br>\nresults in zooming in both spectra.\n</html>");
        linkedSpectraJCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        linkedSpectraJCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        org.jdesktop.layout.GroupLayout linkedSelectionJPanelLayout = new org.jdesktop.layout.GroupLayout(linkedSelectionJPanel);
        linkedSelectionJPanel.setLayout(linkedSelectionJPanelLayout);
        linkedSelectionJPanelLayout.setHorizontalGroup(
            linkedSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, linkedSelectionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(linkedSpectraJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                .addContainerGap())
        );
        linkedSelectionJPanelLayout.setVerticalGroup(
            linkedSelectionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(linkedSelectionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(linkedSpectraJCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        spectrumAJTextField.setEditable(false);
        spectrumAJTextField.setFont(spectrumAJTextField.getFont().deriveFont(spectrumAJTextField.getFont().getStyle() | java.awt.Font.BOLD));
        spectrumAJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        spectrumAJTextField.setText("NH2-LC<Cmm*>TVATLR-COOH");

        spectrumBJTextField.setEditable(false);
        spectrumBJTextField.setFont(spectrumBJTextField.getFont().deriveFont(spectrumBJTextField.getFont().getStyle() | java.awt.Font.BOLD));
        spectrumBJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        spectrumBJTextField.setText("NH2-FQNALLVR-COOH");

        spectrumAJPanel.setBackground(new java.awt.Color(255, 255, 255));
        spectrumAJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        spectrumAJPanel.setLayout(new javax.swing.BoxLayout(spectrumAJPanel, javax.swing.BoxLayout.LINE_AXIS));

        spectrumBJPanel.setBackground(new java.awt.Color(255, 255, 255));
        spectrumBJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        spectrumBJPanel.setLayout(new javax.swing.BoxLayout(spectrumBJPanel, javax.swing.BoxLayout.LINE_AXIS));

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

        org.jdesktop.layout.GroupLayout spectrumJPanelLayout = new org.jdesktop.layout.GroupLayout(spectrumJPanel);
        spectrumJPanel.setLayout(spectrumJPanelLayout);
        spectrumJPanelLayout.setHorizontalGroup(
            spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spectrumJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spectrumPanelInfoJLabel)
                    .add(spectrumAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1053, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, spectrumBJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1053, Short.MAX_VALUE)
                    .add(spectrumAJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1053, Short.MAX_VALUE)
                    .add(spectrumBJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1053, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(profileSelectionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, linkedSelectionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, ionSelectionJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(spectrumPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        spectrumJPanelLayout.setVerticalGroup(
            spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, spectrumJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spectrumJPanelLayout.createSequentialGroup()
                        .add(ionSelectionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(profileSelectionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(linkedSelectionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(spectrumJPanelLayout.createSequentialGroup()
                        .add(spectrumAJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(spectrumAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(spectrumBJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(spectrumBJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                        .add(7, 7, 7)))
                .add(0, 0, 0)
                .add(spectrumJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(spectrumPanelInfoJLabel)
                    .add(spectrumPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane.addTab("Spectrum Panel - Demo", spectrumJPanel);

        chromatogramAJPanel.setBackground(new java.awt.Color(255, 255, 255));
        chromatogramAJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        chromatogramAJPanel.setForeground(new java.awt.Color(255, 255, 255));
        chromatogramAJPanel.setLayout(new javax.swing.BoxLayout(chromatogramAJPanel, javax.swing.BoxLayout.LINE_AXIS));

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

        org.jdesktop.layout.GroupLayout chromatogramJPanelLayout = new org.jdesktop.layout.GroupLayout(chromatogramJPanel);
        chromatogramJPanel.setLayout(chromatogramJPanelLayout);
        chromatogramJPanelLayout.setHorizontalGroup(
            chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(chromatogramJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(chromatogramAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1163, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, chromatogramJPanelLayout.createSequentialGroup()
                        .add(chromatogramPanelInfoJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 407, Short.MAX_VALUE)
                        .add(chromatogramPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        chromatogramJPanelLayout.setVerticalGroup(
            chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chromatogramJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(chromatogramAJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chromatogramJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chromatogramPanelHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chromatogramPanelInfoJLabel))
                .addContainerGap())
        );

        jTabbedPane.addTab("Chromatogram Panel - Demo", chromatogramJPanel);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText("Not yet implemented...");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1159, Short.MAX_VALUE)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel2Layout.createSequentialGroup()
                    .add(0, 515, Short.MAX_VALUE)
                    .add(jLabel3)
                    .add(0, 514, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 536, Short.MAX_VALUE)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel2Layout.createSequentialGroup()
                    .add(0, 260, Short.MAX_VALUE)
                    .add(jLabel3)
                    .add(0, 260, Short.MAX_VALUE)))
        );

        isotopicDistributionCalculatorInfoJLabel.setFont(isotopicDistributionCalculatorInfoJLabel.getFont().deriveFont((isotopicDistributionCalculatorInfoJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        isotopicDistributionCalculatorInfoJLabel.setText("Isotopic Distribution Calculator calculates and visualizes the isotopic distribution for a given peptide.");

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

        org.jdesktop.layout.GroupLayout isotopicDistributionJPanelLayout = new org.jdesktop.layout.GroupLayout(isotopicDistributionJPanel);
        isotopicDistributionJPanel.setLayout(isotopicDistributionJPanelLayout);
        isotopicDistributionJPanelLayout.setHorizontalGroup(
            isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(isotopicDistributionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(isotopicDistributionJPanelLayout.createSequentialGroup()
                        .add(isotopicDistributionCalculatorInfoJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 491, Short.MAX_VALUE)
                        .add(isotopicDistributionCalculatorHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        isotopicDistributionJPanelLayout.setVerticalGroup(
            isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, isotopicDistributionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(isotopicDistributionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(isotopicDistributionCalculatorInfoJLabel)
                    .add(isotopicDistributionCalculatorHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane.addTab("Isotopic Distribution Calculator - Demo", isotopicDistributionJPanel);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Not yet implemented...");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1159, Short.MAX_VALUE)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel3Layout.createSequentialGroup()
                    .add(0, 515, Short.MAX_VALUE)
                    .add(jLabel4)
                    .add(0, 514, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 536, Short.MAX_VALUE)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel3Layout.createSequentialGroup()
                    .add(0, 260, Short.MAX_VALUE)
                    .add(jLabel4)
                    .add(0, 260, Short.MAX_VALUE)))
        );

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

        org.jdesktop.layout.GroupLayout proteinDigestionJPanelLayout = new org.jdesktop.layout.GroupLayout(proteinDigestionJPanel);
        proteinDigestionJPanel.setLayout(proteinDigestionJPanelLayout);
        proteinDigestionJPanelLayout.setHorizontalGroup(
            proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(proteinDigestionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(proteinDigestionJPanelLayout.createSequentialGroup()
                        .add(proteinDigestionJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 264, Short.MAX_VALUE)
                        .add(inSilicoDigestionHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        proteinDigestionJPanelLayout.setVerticalGroup(
            proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, proteinDigestionJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(proteinDigestionJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(proteinDigestionJLabel)
                    .add(inSilicoDigestionHelpJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane.addTab("In Silico Protein Digestion - Demo", proteinDigestionJPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1192, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the demo and terminates the program.
     *
     * @param evt
     */
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
            Vector<DefaultSpectrumAnnotation> currentAnnotations = allAnnotations.get(key);

            // update the ion coverage annotations
            currentSpectrumPanel.setAnnotations(filterAnnotations(currentAnnotations));
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
    }//GEN-LAST:event_profileSpectrumJCheckBoxActionPerformed

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
        // @TODO: implement...
        JOptionPane.showMessageDialog(this, "Sorry, the help text is not yet available.", "Not Implemented...", JOptionPane.INFORMATION_MESSAGE);
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
        // @TODO: implement...
        JOptionPane.showMessageDialog(this, "Sorry, the help text is not yet available.", "Not Implemented...", JOptionPane.INFORMATION_MESSAGE);
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
     * Openes the help dialog.
     *
     * @param urlAsString the URL (as a String) of the help file to display
     */
    private void openHelpDialog(String urlAsString){
        new HelpWindow(this, getClass().getResource(urlAsString));
    }

    /**
     * Filters the annotations and returns the annotations matching the currently selected list.
     *
     * @param annotations the annotations to be filtered
     * @return the filtered annotations
     */
    private Vector<DefaultSpectrumAnnotation> filterAnnotations(Vector<DefaultSpectrumAnnotation> annotations) {

        // ToDo: This method could be moved into the PlotUtil class?

        Vector<DefaultSpectrumAnnotation> filteredAnnotations = new Vector();

        for (int i = 0; i < annotations.size(); i++) {
            String currentLabel = annotations.get(i).getLabel();

            boolean useAnnotation = true;

            // check ion type
            if (currentLabel.startsWith("a")) {
                if (!aIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            } else if (currentLabel.startsWith("b")) {
                if (!bIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            } else if (currentLabel.startsWith("c")) {
                if (!cIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            } else if (currentLabel.startsWith("x")) {
                if (!xIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            } else if (currentLabel.startsWith("y")) {
                if (!yIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            } else if (currentLabel.startsWith("z")) {
                if (!zIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            } else if (currentLabel.startsWith("z")) {
                if (!zIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            } else {
                if (!otherIonsJCheckBox.isSelected()) {
                    useAnnotation = false;
                }
            }

            // check neutral losses
            if (useAnnotation) {
                if (currentLabel.lastIndexOf("-H2O") != -1 || currentLabel.lastIndexOf("-H20") != -1) {
                    if (!H2OIonsJCheckBox.isSelected()) {
                        useAnnotation = false;
                    }
                }

                if (currentLabel.lastIndexOf("-NH3") != -1) {
                    if (!NH3IonsJCheckBox.isSelected()) {
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
                        if (!chargeOneJCheckBox.isSelected()) {
                            useAnnotation = false;
                        }
                    }
                } else if (currentLabel.lastIndexOf("+++") != -1) {
                    if (!chargeOverTwoJCheckBox.isSelected()) {
                        useAnnotation = false;
                    }
                } else if (currentLabel.lastIndexOf("++") != -1) {
                    if (!chargeTwoJCheckBox.isSelected()) {
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
     * Starts the UtilitiesDemo.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                try {
                    PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                    UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
                } catch (UnsupportedLookAndFeelException e) {
                    //e.printStackTrace();
                    // ignore error
                }

                new UtilitiesDemo();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox H2OIonsJCheckBox;
    private javax.swing.JCheckBox NH3IonsJCheckBox;
    private javax.swing.JCheckBox aIonsJCheckBox;
    private javax.swing.JCheckBox bIonsJCheckBox;
    private javax.swing.JCheckBox cIonsJCheckBox;
    private javax.swing.JCheckBox chargeOneJCheckBox;
    private javax.swing.JCheckBox chargeOverTwoJCheckBox;
    private javax.swing.JCheckBox chargeTwoJCheckBox;
    private javax.swing.JPanel chromatogramAJPanel;
    private javax.swing.JPanel chromatogramJPanel;
    private javax.swing.JLabel chromatogramPanelHelpJLabel;
    private javax.swing.JLabel chromatogramPanelInfoJLabel;
    private javax.swing.JLabel inSilicoDigestionHelpJLabel;
    private javax.swing.JEditorPane informationJEditorPane;
    private javax.swing.JPanel informationJPanel;
    private javax.swing.JScrollPane informationJScrollPane;
    private javax.swing.JPanel ionSelectionJPanel;
    private javax.swing.JLabel isotopicDistributionCalculatorHelpJLabel;
    private javax.swing.JLabel isotopicDistributionCalculatorInfoJLabel;
    private javax.swing.JPanel isotopicDistributionJPanel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JPanel linkedSelectionJPanel;
    private javax.swing.JCheckBox linkedSpectraJCheckBox;
    private javax.swing.JCheckBox otherIonsJCheckBox;
    private javax.swing.JPanel profileSelectionJPanel;
    private javax.swing.JCheckBox profileSpectrumJCheckBox;
    private javax.swing.JLabel proteinDigestionJLabel;
    private javax.swing.JPanel proteinDigestionJPanel;
    private javax.swing.JPanel spectrumAJPanel;
    private javax.swing.JTextField spectrumAJTextField;
    private javax.swing.JPanel spectrumBJPanel;
    private javax.swing.JTextField spectrumBJTextField;
    private javax.swing.JPanel spectrumJPanel;
    private javax.swing.JLabel spectrumPanelHelpJLabel;
    private javax.swing.JLabel spectrumPanelInfoJLabel;
    private javax.swing.JCheckBox xIonsJCheckBox;
    private javax.swing.JCheckBox yIonsJCheckBox;
    private javax.swing.JCheckBox zIonsJCheckBox;
    // End of variables declaration//GEN-END:variables
}
