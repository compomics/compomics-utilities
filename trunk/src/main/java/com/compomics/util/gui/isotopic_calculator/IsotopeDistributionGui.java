package com.compomics.util.gui.isotopic_calculator;

import com.compomics.util.AlternateRowColoursJTable;
import com.compomics.util.enumeration.MolecularElement;
import com.compomics.util.general.IsotopicDistribution;
import com.compomics.util.general.IsotopicDistributionSpectrum;
import com.compomics.util.general.MassCalc;
import com.compomics.util.general.UnknownElementMassException;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.MolecularFormula;
import com.jgoodies.looks.FontPolicies;
import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.FontSet;
import com.jgoodies.looks.FontSets;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * This class is a GUI that visualizes the isotopic calculator.
 * <p>
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 16-Aug-2010
 * Time: 13:24:41
 */
public class IsotopeDistributionGui extends JFrame {

    // Class specific log4j logger for MolecularFormula instances.
    Logger logger = Logger.getLogger(MolecularFormula.class);

    //gui objects
    private JTextArea txtSequence;
    private JLabel lblComp;
    private JLabel lblMass;
    private JButton calculateButton;
    private AlternateRowColoursJTable table1;
    private JPanel jpanContent;
    private JPanel headerTable;
    private JPanel spectrumPanel;
    private JSpinner spinCharge;
    private JLabel lblPeptide;

    /**
     * The amino acid sequence
     */
    private AASequenceImpl iSequence = null;
    /**
     * HashMap with the molecular formula for all the aminoacids
     */
    private HashMap<String, MolecularFormula> iElements;
    /**
     * The charge
     */
    private double iCharge;

    /**
     * The constructor
     *
     * @param lStandAlone Boolean that indicates if this is a standalone JFrame
     */
    public IsotopeDistributionGui(boolean lStandAlone) {
        super("Isotopic distribution calculator");
        $$$setupUI$$$();
        if (lStandAlone) {
            //set the frame parameters
            this.setContentPane(jpanContent);
            this.setSize(800, 800);
            this.setLocationRelativeTo(null);
            this.setVisible(true);
            this.setIconImage(new ImageIcon(getClass().getResource("/icons/compomics-utilities.png")).getImage());
            //add a closing window listener
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    System.exit(0);
                }
            });
            // Look n feel.
            try {
                FontSet fontSet = FontSets.createDefaultFontSet(
                        new Font("Tahoma", Font.PLAIN, 11),    // control font
                        new Font("Tahoma", Font.PLAIN, 11),    // menu font
                        new Font("Tahoma", Font.BOLD, 11)     // title font
                );
                FontPolicy fixedPolicy = FontPolicies.createFixedPolicy(fontSet);
                PlasticLookAndFeel.setFontPolicy(fixedPolicy);
                PlasticLookAndFeel.setPlasticTheme(new Silver());
                UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            } catch (UnsupportedLookAndFeelException e) {
                logger.error(e.getMessage(), e);
            }
        }

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
        calculateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculate();
            }
        });
        spinCharge.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                calculate();
            }
        });
    }

    /**
     * This method will do the calculations
     */
    public void calculate() {
        //Get the sequence
        String lSeq = txtSequence.getText();
        //exclude unwanted characters
        lSeq = lSeq.trim().toUpperCase();
        lSeq = lSeq.replace("\n", "");
        lSeq = lSeq.replace("\t", "");
        lSeq = lSeq.replace(" ", "");
        //check the aminoacids
        for (int i = 0; i < lSeq.length(); i++) {
            String lLetter = String.valueOf(lSeq.charAt(i));
            if (!isElement(lLetter)) {
                //return
                JOptionPane.showMessageDialog(this, lLetter + " at position " + (i + 1) + " is not a valid element", "Not valid element", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (lSeq.length() == 0) {
            //return
            JOptionPane.showMessageDialog(this, "Sequence cannot be of length zero!", "Not valid sequence", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //create the sequence
        iSequence = new AASequenceImpl(lSeq);
        //get the charge
        iCharge = (Double) spinCharge.getValue();
        //set the labels
        lblComp.setText(iSequence.getMolecularFormula().toString());
        double lMz = iSequence.getMz((int) iCharge);
        lblMass.setText(String.valueOf(Math.floor(lMz * 10000.0) / 10000.0) + " Da");
        lblPeptide.setText("NH2-" + lSeq + "-COOH (" + iCharge + "+)");
        //calculate the distribution
        IsotopicDistribution lIso = iSequence.getIsotopicDistribution();
        HashMap lPeaks = new HashMap();
        //add the data to the table
        for (int i = 0; i < 10; i++) {
            table1.setValueAt(i, i, 0);
            table1.setValueAt(Math.floor(lIso.getPercTot()[i] * 10000.0) / 100.0, i, 1);
            table1.setValueAt(Math.floor(lIso.getPercMax()[i] * 10000.0) / 100.0, i, 2);
            try {
                lPeaks.put(lMz + (i * (new MassCalc().calculateMass("H") / iCharge)), lIso.getPercMax()[i]);
            } catch (UnknownElementMassException ume) {
                logger.error(ume.getMessage(), ume);
            }
        }
        //do gui updates an add the spectrum panel
        table1.updateUI();
        IsotopicDistributionSpectrum lSpecFile = new IsotopicDistributionSpectrum();
        lSpecFile.setCharge(Integer.valueOf(String.valueOf(iCharge).substring(0, String.valueOf(iCharge).indexOf("."))));
        lSpecFile.setPrecursorMZ(lMz);
        lSpecFile.setPeaks(lPeaks);
        spectrumPanel.removeAll();
        SpectrumPanel lSpecPanel = new SpectrumPanel(lSpecFile, false);
        lSpecPanel.rescale(lMz - (0.5 / iCharge), lMz + (0.5 / iCharge) + (10.0 / iCharge));
        spectrumPanel.add(lSpecPanel);
        spectrumPanel.updateUI();
    }

    /**
     * This methods gives a JPanel holding everything from this frame
     *
     * @return JPanel
     */
    public JPanel getContentPane() {
        return jpanContent;
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
     * Main method
     *
     * @param Args The arguments
     */
    public static void main(String[] Args) {
        new IsotopeDistributionGui(true);
    }

    /**
     * Create gui components
     */
    private void createUIComponents() {
        String[] columnNames = {"Isotope Number", "% Total", "% Maximum"};
        TableModel lModel = new SparseTableModel(10, columnNames);
        for (int i = 0; i < 10; i++) {
            lModel.setValueAt(i, i, 0);
            lModel.setValueAt(0.0, i, 1);
            lModel.setValueAt(0.0, i, 2);
        }
        table1 = new AlternateRowColoursJTable(lModel);
        headerTable = new JPanel();
        headerTable.setLayout(new BoxLayout(headerTable, BoxLayout.Y_AXIS));
        headerTable.add(table1.getTableHeader());

        spectrumPanel = new JPanel();
        spectrumPanel.setLayout(new BoxLayout(spectrumPanel, BoxLayout.X_AXIS));
        spectrumPanel.add(Box.createVerticalStrut(1));
        spectrumPanel.add(new ImagePanel("icons/compomics.png"));
        //spectrumPanel.add(Box.createVerticalStrut(1));
        spectrumPanel.add(Box.createVerticalGlue());

        spinCharge = new JSpinner();
        spinCharge.setModel(new SpinnerNumberModel(1.0, 1.0, 20.0, 1.0));
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(11);
        label1.setText("Composition:");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.ipadx = 30;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment(11);
        label2.setText("M/Z");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(label2, gbc);
        lblComp = new JLabel();
        lblComp.setFont(new Font(lblComp.getFont().getName(), lblComp.getFont().getStyle(), 16));
        lblComp.setText("/");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(lblComp, gbc);
        lblMass = new JLabel();
        lblMass.setFont(new Font(lblMass.getFont().getName(), lblMass.getFont().getStyle(), 16));
        lblMass.setText("/");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(lblMass, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 10, 10);
        jpanContent.add(table1, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 0, 10);
        jpanContent.add(headerTable, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setMaximumSize(new Dimension(50, 50));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(panel1, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(30);
        scrollPane1.setMaximumSize(new Dimension(50, 50));
        scrollPane1.setVerticalScrollBarPolicy(20);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel1.add(scrollPane1, gbc);
        txtSequence = new JTextArea();
        txtSequence.setMaximumSize(new Dimension(50, 50));
        scrollPane1.setViewportView(txtSequence);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 6;
        gbc.gridheight = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(spectrumPanel, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(spinCharge, gbc);
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment(11);
        label3.setText("Charge:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.ipadx = 30;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(label3, gbc);
        calculateButton = new JButton();
        calculateButton.setText("Calculate");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        jpanContent.add(calculateButton, gbc);
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(separator1, gbc);
        lblPeptide = new JLabel();
        lblPeptide.setFont(new Font(lblPeptide.getFont().getName(), Font.ITALIC, 18));
        lblPeptide.setHorizontalAlignment(0);
        lblPeptide.setHorizontalTextPosition(0);
        lblPeptide.setText(" ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 6;
        jpanContent.add(lblPeptide, gbc);
    }

    /**
     * Returns the root components.
     * 
     * @return the root component
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }

    /**
     * This is an imagepane class
     * It paint a given image in its panel
     */
    class ImagePanel extends JPanel {

        /**
         * The image
         */
        private BufferedImage image;

        /**
         * Constructor
         *
         * @param lFilename String with the filename that has to be in the classpath
         */
        public ImagePanel(String lFilename) {
            try {
                image = ImageIO.read(getClass().getResource("/" + lFilename));
            } catch (IOException ex) {
                logger.error(ex);
            }
        }

        @Override
        /**
         * Paint method
         */
        public void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
    }

    /**
     * This is a table model extension
     */
    class SparseTableModel extends AbstractTableModel {

        /**
         * The lookup hashtable
         */
        private Hashtable lookup;

        /**
         * number of rows
         */
        private final int rows;

        /**
         * number of columns
         */
        private final int columns;

        /**
         * The colum headers
         */
        private final String headers[];

        /**
         * Constructor
         *
         * @param rows          number of rows
         * @param columnHeaders the column headers
         */
        public SparseTableModel(int rows, String columnHeaders[]) {
            if ((rows < 0) || (columnHeaders == null)) {
                throw new IllegalArgumentException("Invalid row count/columnHeaders");
            }
            this.rows = rows;
            this.columns = columnHeaders.length;
            headers = columnHeaders;
            lookup = new Hashtable();
        }

        /**
         * Getter for the number of columns
         *
         * @return int with the number of columns
         */
        public int getColumnCount() {
            return columns;
        }

        /**
         * Getter for the number of rows
         *
         * @return int with the number of rows
         */
        public int getRowCount() {
            return rows;
        }

        /**
         * Getter for the column title
         *
         * @param column int with the column number
         * @return String with the column title
         */
        public String getColumnName(int column) {
            return headers[column];
        }

        /**
         * Getter for the element on the given position
         *
         * @param row    int with the row number
         * @param column int with the column number
         * @return Object with the element at that position
         */
        public Object getValueAt(int row, int column) {
            return lookup.get(new Point(row, column));
        }

        /**
         * Setter for the element on the given position
         *
         * @param value  The object that has to be set
         * @param row    int with the row number
         * @param column int with the column number
         */
        public void setValueAt(Object value, int row, int column) {
            if ((rows < 0) || (columns < 0)) {
                throw new IllegalArgumentException("Invalid row/column setting");
            }
            if ((row < rows) && (column < columns)) {
                lookup.put(new Point(row, column), value);
            }
        }
    }
}