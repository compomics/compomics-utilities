package com.compomics.util.gui.modification;

import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.*;
import com.compomics.util.experiment.biology.ions.impl.ReporterIon;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.gui.AminoAcidPatternDialog;
import com.compomics.util.gui.atoms.AtomChainDialog;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.ToolTipComboBoxRenderer;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.pride.PtmToPrideMap;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;

/**
 * This dialog allows the user to create/edit Modifications.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ModificationDialog extends javax.swing.JDialog {

    /**
     * The post translational modifications factory.
     */
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
    /**
     * The edited Modification.
     */
    private Modification currentPtm = null;
    /**
     * The neutral losses.
     */
    private ArrayList<NeutralLoss> neutralLosses = new ArrayList<>();
    /**
     * The reporter ions.
     */
    private ArrayList<ReporterIon> reporterIons = new ArrayList<>();
    /**
     * Boolean indicating whether the user can edit the Modification or not.
     */
    private boolean editable;
    /**
     * The amino acid pattern of the modification
     */
    private AminoAcidPattern pattern;
    /**
     * The atom chain added by the modification.
     */
    private AtomChain atomChainAdded;
    /**
     * The atom chain removed by the modification.
     */
    private AtomChain atomChainRemoved;
    /**
     * The reporter ion table column header tooltips.
     */
    private ArrayList<String> reporterIonTableToolTips;
    /**
     * The neutral losses table column header tooltips.
     */
    private ArrayList<String> neutralLossesTableToolTips;
    /**
     * Boolean indicating whether the edition has been canceled by the user.
     */
    private boolean canceled = false;

    /**
     * Creates a new Modification dialog.
     *
     * @param parent the JDialog parent
     * @param currentModification the Modification to edit (can be null)
     * @param editable boolean indicating whether the user can edit the Modification
     * details
     */
    public ModificationDialog(JDialog parent, Modification currentModification, boolean editable) {
        super(parent, true);

        this.currentPtm = currentModification;
        if (currentModification != null) {
            this.pattern = currentPtm.getPattern();
            this.atomChainAdded = currentModification.getAtomChainAdded();
            this.atomChainRemoved = currentModification.getAtomChainRemoved();
        } else {
            pattern = null;
            this.atomChainAdded = new AtomChain();
            this.atomChainRemoved = new AtomChain();
        }
        this.editable = editable;

        initComponents();
        setUpGui();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates a new Modification dialog.
     *
     * @param parent the JFrame parent
     * @param modificationToPrideMap the Modification to PRIDE map
     * @param currentModification the Modification to edit (can be null)
     * @param editable boolean indicating whether the user can edit the Modification
     * details
     */
    public ModificationDialog(JFrame parent, PtmToPrideMap modificationToPrideMap, Modification currentModification, boolean editable) {
        super(parent, true);

        this.currentPtm = currentModification;
        this.editable = editable;
        if (currentModification != null) {
            this.pattern = currentPtm.getPattern();
            this.atomChainAdded = currentModification.getAtomChainAdded();
            this.atomChainRemoved = currentModification.getAtomChainRemoved();
        } else {
            pattern = null;
            this.atomChainAdded = new AtomChain();
            this.atomChainRemoved = new AtomChain();
        }

        initComponents();
        setUpGui();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        // centrally align the comboboxes
        typeCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        // set table properties
        neutralLossesTable.getTableHeader().setReorderingAllowed(false);
        reporterIonsTable.getTableHeader().setReorderingAllowed(false);

        // make sure that the scroll panes are see-through
        neutralLossesJScrollPane.getViewport().setOpaque(false);
        reporterIonsJScrollPane.getViewport().setOpaque(false);

        // the index column
        neutralLossesTable.getColumn(" ").setMaxWidth(50);
        neutralLossesTable.getColumn(" ").setMinWidth(50);
        neutralLossesTable.getColumn("Fixed").setMaxWidth(50);
        neutralLossesTable.getColumn("Fixed").setMinWidth(50);
        reporterIonsTable.getColumn(" ").setMaxWidth(50);
        reporterIonsTable.getColumn(" ").setMinWidth(50);

        // set the fixed clumn cell renderer
        neutralLossesTable.getColumn("Fixed").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/selected_green.png")),
                null,
                "Fixed", null));

        reporterIonTableToolTips = new ArrayList<>();
        reporterIonTableToolTips.add(null);
        reporterIonTableToolTips.add("Reporter Ion Name");
        reporterIonTableToolTips.add("Reporter Ion Composition");
        reporterIonTableToolTips.add("Reporter Ion Mass (m/z)");

        neutralLossesTableToolTips = new ArrayList<>();
        neutralLossesTableToolTips.add(null);
        neutralLossesTableToolTips.add("Neutral Loss Name");
        neutralLossesTableToolTips.add("Neutral Loss Composition");
        neutralLossesTableToolTips.add("Neutral Loss Mass");
        neutralLossesTableToolTips.add("Fixed Neutral Loss");

        Vector comboboxTooltips = new Vector();
        comboboxTooltips.add("Modification at particular amino acids");
        comboboxTooltips.add("Modification at the N terminus of a protein");
        comboboxTooltips.add("Modification at the N terminus of a protein at particular amino acids");
        comboboxTooltips.add("Modification at the C terminus of a protein");
        comboboxTooltips.add("Modification at the C terminus of a protein at particular amino acids");
        comboboxTooltips.add("Modification at the N terminus of a peptide");
        comboboxTooltips.add("Modification at the N terminus of a peptide at particular amino acids");
        comboboxTooltips.add("Modification at the C terminus of a peptide");
        comboboxTooltips.add("Modification at the C terminus of a peptide at particular amino acids");
        typeCmb.setRenderer(new ToolTipComboBoxRenderer(comboboxTooltips, SwingConstants.CENTER));

        typeCmb.setEnabled(editable);
        nameTxt.setEditable(editable);
        nameShortTxt.setEditable(editable);
        addNeutralLoss.setEnabled(editable);
        removeNeutralLoss.setEnabled(editable);
        addReporterIon.setEnabled(editable);
        removerReporterIon.setEnabled(editable);
        patternTxt.setEditable(editable);
        unimodAccessionJTextField.setEditable(editable);
        unimodNameJTextField.setEditable(editable);

        if (currentPtm != null) {
            typeCmb.setSelectedIndex(currentPtm.getModificationType().index);
            nameTxt.setText(currentPtm.getName());
            nameShortTxt.setText(currentPtm.getShortName());

            String addition = "";
            if (atomChainAdded != null) {
                addition = atomChainAdded.toString();
            }
            String deletion = "";
            if (atomChainRemoved != null) {
                deletion = "-" + atomChainRemoved.toString();
            }
            String temp = addition + " " + deletion;
            compositionTxt.setText(temp.trim());

            if (pattern != null) {
                patternTxt.setText(pattern.toString());
            }
            updateMass();

            if (!currentPtm.getNeutralLosses().isEmpty()) {
                for (NeutralLoss tempNeutralLoss : currentPtm.getNeutralLosses()) {
                    neutralLosses.add(tempNeutralLoss.clone());
                }
            }
            if (!currentPtm.getReporterIons().isEmpty()) {
                for (ReporterIon tempReporterIon : currentPtm.getReporterIons()) {
                    reporterIons.add(tempReporterIon.clone());
                }
            }

            updateTables();

            CvTerm cvTerm = currentPtm.getCvTerm();
            if (cvTerm != null) {
                updateModMappingText(cvTerm);
            }

            // special case for the default modifications without cv terms
            if (cvTerm == null) {
                unimodAccessionJTextField.setEditable(true);
                unimodNameJTextField.setEditable(true);
            }

            setTitle("Edit Modification");
        }

        validateInput(false);
    }

    /**
     * Update the mass field.
     */
    private void updateMass() {
        try {
            double mass = 0.0;
            if (atomChainAdded != null) {
                mass += atomChainAdded.getMass();
            }
            if (atomChainRemoved != null) {
                mass -= atomChainRemoved.getMass();
            }
            massTxt.setText("" + Util.roundDouble(mass, 4));
        } catch (IllegalArgumentException e) {
            // ignore error, handled in the validateInput method
            massTxt.setText("");
        }
    }

    /**
     * Indicates whether the edition was canceled by the user.
     *
     * @return a boolean indicating whether the edition was canceled by the user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns a boolean indicating whether the input can be translated into a
     * Modification.
     *
     * @return a boolean indicating whether the input can be translated into a
     * Modification
     */
    private boolean validateInput(boolean showMessage) {

        boolean error = false;

        nameLabel.setForeground(Color.BLACK);
        compositionLabel.setForeground(Color.BLACK);
        patternLabel.setForeground(Color.BLACK);
        unimodAccessionLabel.setForeground(Color.BLACK);
        unimodNameLabel.setForeground(Color.BLACK);
        ((TitledBorder) neutralLossesPanel.getBorder()).setTitleColor(Color.BLACK);
        neutralLossesPanel.repaint();
        ((TitledBorder) reporterIonsPanel.getBorder()).setTitleColor(Color.BLACK);
        reporterIonsPanel.repaint();

        nameLabel.setToolTipText(null);
        nameTxt.setToolTipText(null);
        compositionLabel.setToolTipText(null);
        compositionTxt.setToolTipText(null);
        patternLabel.setToolTipText(null);
        patternTxt.setToolTipText(null);
        unimodAccessionLabel.setToolTipText(null);
        unimodAccessionJTextField.setToolTipText(null);
        neutralLossesPanel.setToolTipText(null);
        reporterIonsPanel.setToolTipText(null);

        // check the modification mass
        if (compositionTxt.getText().trim().length() == 0) {
            error = true;
            compositionLabel.setForeground(Color.RED);
            compositionLabel.setToolTipText("Please provide a modification composition");
        }

        String name = nameTxt.getText().trim();

        // check the length of the modification name
        if (name.length() == 0) {
            error = true;
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("Please provide a modification name");
            nameTxt.setToolTipText("Please provide a modification name");
        }

        // check if name contains '|'
        if (name.lastIndexOf("|") != -1) {
            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this, "Modification names cannot contain \'|\'.");
            }
            error = true;
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("Modification names cannot contain \'|\'");
            nameTxt.setToolTipText("Modification names cannot contain \'|\'");
        }

        // check if name contains ','
        if (name.lastIndexOf(",") != -1) {
            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this, "Modification names cannot contain \',\'.");
            }
            error = true;
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("Modification names cannot contain \',\'");
            nameTxt.setToolTipText("Modification names cannot contain \',\'");
        }

        // check if name contains the modification separator
        if (name.contains(Peptide.MODIFICATION_SEPARATOR)) {
            String newName = name.replace(Peptide.MODIFICATION_SEPARATOR, " ");

            if (showMessage && !error) {
                int outcome = JOptionPane.showConfirmDialog(this, "\'" + Peptide.MODIFICATION_SEPARATOR
                        + "\' should be avoided in modification names."
                        + "\nShall " + name + " be replaced by "
                        + newName + "?", "'" + Peptide.MODIFICATION_SEPARATOR + "' in Name", JOptionPane.YES_NO_OPTION);
                if (outcome == JOptionPane.YES_OPTION) {
                    nameTxt.setText(newName);
                } else {
                    error = true;
                    nameLabel.setForeground(Color.RED);
                    nameLabel.setToolTipText("\'" + Peptide.MODIFICATION_SEPARATOR + "\' should be avoided in modification names");
                    nameTxt.setToolTipText("\'" + Peptide.MODIFICATION_SEPARATOR + "\' should be avoided in modification names");
                }
            } else {
                error = true;
                nameLabel.setForeground(Color.RED);
                nameLabel.setToolTipText("\'" + Peptide.MODIFICATION_SEPARATOR + "\' should be avoided in modification names");
                nameTxt.setToolTipText("\'" + Peptide.MODIFICATION_SEPARATOR + "\' should be avoided in modification names");
            }
        }

        // check if name contains the modification location separator
        if (name.contains(Peptide.MODIFICATION_LOCALIZATION_SEPARATOR)) {
            String newName = name.replace(Peptide.MODIFICATION_LOCALIZATION_SEPARATOR, "AT-AA");

            if (showMessage && !error) {
                int outcome = JOptionPane.showConfirmDialog(this, "\'" + Peptide.MODIFICATION_LOCALIZATION_SEPARATOR
                        + "\' should be avoided in modification names.\n"
                        + "Shall " + name + " be replaced by "
                        + newName + "?", "'" + Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + "' in Name", JOptionPane.YES_NO_OPTION);
                if (outcome == JOptionPane.YES_OPTION) {
                    nameTxt.setText(newName);
                } else {
                    error = true;
                    nameLabel.setForeground(Color.RED);
                    nameLabel.setToolTipText("\'" + Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + "\' should be avoided in modification names.");
                    nameTxt.setToolTipText("\'" + Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + "\' should be avoided in modification names.");
                }
            } else {
                error = true;
                nameLabel.setForeground(Color.RED);
                nameLabel.setToolTipText("\'" + Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + "\' should be avoided in modification names.");
                nameTxt.setToolTipText("\'" + Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + "\' should be avoided in modification names.");
            }
        }

        // check if name ends with a protected suffix
        if (name.contains(ModificationFactory.SINGLE_AA_SUFFIX)) {
            String newName = name.replace(ModificationFactory.SINGLE_AA_SUFFIX, "SEARCH-ONLY");

            if (showMessage && !error) {
                int outcome = JOptionPane.showConfirmDialog(this, "\'" + ModificationFactory.SINGLE_AA_SUFFIX
                        + "\' should be avoided in the end of modification names.\n"
                        + "Shall " + name + " be replaced by "
                        + newName + "?", "'" + ModificationFactory.SINGLE_AA_SUFFIX + "' Ending Name", JOptionPane.YES_NO_OPTION);
                if (outcome == JOptionPane.YES_OPTION) {
                    nameTxt.setText(newName);
                } else {
                    error = true;
                    nameLabel.setForeground(Color.RED);
                    nameLabel.setToolTipText("\'" + ModificationFactory.SINGLE_AA_SUFFIX + "\' should be avoided in modification names.");
                    nameTxt.setToolTipText("\'" + ModificationFactory.SINGLE_AA_SUFFIX + "\' should be avoided in modification names.");
                }
            } else {
                error = true;
                nameLabel.setForeground(Color.RED);
                nameLabel.setToolTipText("\'" + ModificationFactory.SINGLE_AA_SUFFIX + "\' should be avoided in modification names.");
                nameTxt.setToolTipText("\'" + ModificationFactory.SINGLE_AA_SUFFIX + "\' should be avoided in modification names.");
            }
        }

        // check that the modification name does not already exist as a default modification
        name = nameTxt.getText().trim();
        if (modificationFactory.getDefaultModifications().contains(name)
                && (currentPtm == null || !name.equals(currentPtm.getName()))) {
            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this, "A modification named \'" + name + "\' already exists in the "
                        + "default modification lists.\n"
                        + "Please select the default modification or use another name.",
                        "Modification Already Exists", JOptionPane.WARNING_MESSAGE);
            } else {
                nameLabel.setForeground(Color.RED);
                nameLabel.setToolTipText(
                        "<html>A modification named \'" + name + "\' already exists in the "
                        + "default modification lists.<br>"
                        + "Please select the default modification or use another name.</html>");
                nameTxt.setToolTipText(
                        "<html>A modification named \'" + name + "\' already exists in the "
                        + "default modification lists.<br>"
                        + "Please select the default modification or use another name.</html>");
            }
            error = true;
        }

        // check that the modification does not already exist as a user defined modification
        if (modificationFactory.getUserModifications().contains(name)
                && (currentPtm == null || !name.equals(currentPtm.getName()))) {
            if (showMessage && !error) {
                JOptionPane.showMessageDialog(this, "There is already a modification named \'" + name + "\'!",
                        "Modification Already Exists", JOptionPane.WARNING_MESSAGE);
            }
            error = true;
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("There is already a modification named \'" + name + "\'!");
            nameTxt.setToolTipText("There is already a modification named \'" + name + "\'!");
        }

        // check that a modification pattern is given
        if (patternTxt.getText().length() == 0) {

            if (typeCmb.getSelectedIndex() == 0
                    || typeCmb.getSelectedIndex() == 2
                    || typeCmb.getSelectedIndex() == 4
                    || typeCmb.getSelectedIndex() == 6
                    || typeCmb.getSelectedIndex() == 8) {

                if (showMessage && !error) {
                    JOptionPane.showMessageDialog(this, "Please verify the input for the modification pattern.",
                            "Missing Pattern", JOptionPane.WARNING_MESSAGE);
                }
                error = true;
                patternLabel.setForeground(Color.RED);
                patternLabel.setToolTipText("Please provide a modification pattern");
                patternTxt.setToolTipText("Please provide a modification pattern");
            }
        }

        // check that the unimod cv term accesion is an integer
        if (!unimodAccessionJTextField.getText().trim().isEmpty()) {
            try {
                new Integer(unimodAccessionJTextField.getText().trim());
            } catch (NumberFormatException e) {
                if (showMessage && !error) {
                    JOptionPane.showMessageDialog(this, "Please provide the Unimod accession number as an integer.", "Unimod Accession", JOptionPane.WARNING_MESSAGE);
                }
                error = true;
                unimodAccessionLabel.setForeground(Color.RED);
                unimodAccessionLabel.setToolTipText("Please provide the Unimod accession number as an integer");
                unimodAccessionJTextField.setToolTipText("Please provide the Unimod accession number as an integer");
            }
        }

        // check that the neutral losses and reporter ions are not already in use
        if (!neutralLosses.isEmpty()) {
            for (NeutralLoss tempNeutralLoss : neutralLosses) {
                NeutralLoss existingNeutralLoss = NeutralLoss.getNeutralLoss(tempNeutralLoss.name);
                if (existingNeutralLoss != null && !tempNeutralLoss.isSameAs(existingNeutralLoss)) {
                    if (showMessage && !error) {
                        JOptionPane.showMessageDialog(this, "A neutral loss named \'" + tempNeutralLoss.name 
                                + "\' already exists. Please choose a different name.", "Neutral Loss", JOptionPane.WARNING_MESSAGE);
                    }
                    error = true;
                    ((TitledBorder) neutralLossesPanel.getBorder()).setTitleColor(Color.RED);
                    neutralLossesPanel.setToolTipText("A neutral loss named \'" + tempNeutralLoss.name + "\' already exists");
                    neutralLossesPanel.repaint();
                }
            }
        }
        if (!reporterIons.isEmpty()) {
            for (ReporterIon tempReporterIon : reporterIons) {
                ReporterIon existingReporterIon = ReporterIon.getReporterIon(tempReporterIon.getName());
                if (existingReporterIon != null && !tempReporterIon.isSameAs(existingReporterIon)) {
                    if (showMessage && !error) {
                        JOptionPane.showMessageDialog(this, "A reporter ion named \'" + tempReporterIon.getName() 
                                + "\' already exists. Please choose a different name.", "Reporter Ion", JOptionPane.WARNING_MESSAGE);
                    }
                    error = true;
                    ((TitledBorder) reporterIonsPanel.getBorder()).setTitleColor(Color.RED);
                    reporterIonsPanel.setToolTipText("A reporter ion named \'" + tempReporterIon.getName() + "\' already exists");
                    reporterIonsPanel.repaint();
                }
            }
        }

        okButton.setEnabled(!error);

        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        scrollPanePanel = new javax.swing.JPanel();
        unimodMappingPanel = new javax.swing.JPanel();
        unimodAccessionJTextField = new javax.swing.JTextField();
        unimodAccessionLabel = new javax.swing.JLabel();
        unimodNameLabel = new javax.swing.JLabel();
        unimodNameJTextField = new javax.swing.JTextField();
        unimodLinkLabel = new javax.swing.JLabel();
        cvExampleLabel = new javax.swing.JLabel();
        reporterIonsPanel = new javax.swing.JPanel();
        reporterIonsJScrollPane = new javax.swing.JScrollPane();
        reporterIonsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return (String) reporterIonTableToolTips.get(realIndex);
                    }
                };
            }
        };
        removerReporterIon = new javax.swing.JButton();
        addReporterIon = new javax.swing.JButton();
        detailsPanel = new javax.swing.JPanel();
        typeCmb = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        compositionTxt = new javax.swing.JTextField();
        patternLabel = new javax.swing.JLabel();
        patternTxt = new javax.swing.JTextField();
        nameShortLabel = new javax.swing.JLabel();
        nameShortTxt = new javax.swing.JTextField();
        compositionLabel = new javax.swing.JLabel();
        massLabel = new javax.swing.JLabel();
        massTxt = new javax.swing.JTextField();
        neutralLossesPanel = new javax.swing.JPanel();
        neutralLossesJScrollPane = new javax.swing.JScrollPane();
        neutralLossesTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return (String) neutralLossesTableToolTips.get(realIndex);
                    }
                };
            }
        };
        addNeutralLoss = new javax.swing.JButton();
        removeNeutralLoss = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Modification");
        setMinimumSize(new java.awt.Dimension(500, 500));

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        okButton.setText("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.setBorder(null);
        helpJButton.setBorderPainted(false);
        helpJButton.setContentAreaFilled(false);
        helpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                helpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                helpJButtonMouseExited(evt);
            }
        });
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        scrollPane.setBorder(null);

        scrollPanePanel.setBackground(new java.awt.Color(230, 230, 230));

        unimodMappingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Unimod Mapping"));
        unimodMappingPanel.setOpaque(false);

        unimodAccessionJTextField.setEditable(false);
        unimodAccessionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        unimodAccessionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unimodAccessionJTextFieldKeyReleased(evt);
            }
        });

        unimodAccessionLabel.setText("Accession");

        unimodNameLabel.setText("PSI-MS Name");

        unimodNameJTextField.setEditable(false);
        unimodNameJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        unimodNameJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unimodNameJTextFieldKeyReleased(evt);
            }
        });

        unimodLinkLabel.setText("<html><a href>See: http://www.unimod.org</a></html>");
        unimodLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                unimodLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                unimodLinkLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                unimodLinkLabelMouseReleased(evt);
            }
        });

        cvExampleLabel.setFont(cvExampleLabel.getFont().deriveFont((cvExampleLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        cvExampleLabel.setText("Ex.: Accession:1, PSI-MS Name: Acetyl");

        javax.swing.GroupLayout unimodMappingPanelLayout = new javax.swing.GroupLayout(unimodMappingPanel);
        unimodMappingPanel.setLayout(unimodMappingPanelLayout);
        unimodMappingPanelLayout.setHorizontalGroup(
            unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(unimodMappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(unimodNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unimodAccessionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(unimodMappingPanelLayout.createSequentialGroup()
                        .addComponent(cvExampleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(unimodLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(unimodAccessionJTextField)
                    .addComponent(unimodNameJTextField, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        unimodMappingPanelLayout.setVerticalGroup(
            unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(unimodMappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unimodAccessionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unimodAccessionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unimodNameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unimodNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unimodLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cvExampleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        reporterIonsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Reporter Ions"));
        reporterIonsPanel.setOpaque(false);

        reporterIonsTable.setModel(new ReporterIonsTable());
        reporterIonsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        reporterIonsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                reporterIonsTableMouseReleased(evt);
            }
        });
        reporterIonsJScrollPane.setViewportView(reporterIonsTable);

        removerReporterIon.setText("Remove");
        removerReporterIon.setToolTipText("Remove the selected reporter ion");
        removerReporterIon.setEnabled(false);
        removerReporterIon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removerReporterIonActionPerformed(evt);
            }
        });

        addReporterIon.setText("Add");
        addReporterIon.setToolTipText("Add a reporter ion");
        addReporterIon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addReporterIonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout reporterIonsPanelLayout = new javax.swing.GroupLayout(reporterIonsPanel);
        reporterIonsPanel.setLayout(reporterIonsPanelLayout);
        reporterIonsPanelLayout.setHorizontalGroup(
            reporterIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterIonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reporterIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addReporterIon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removerReporterIon, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reporterIonsJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        reporterIonsPanelLayout.setVerticalGroup(
            reporterIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterIonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reporterIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reporterIonsJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(reporterIonsPanelLayout.createSequentialGroup()
                        .addGap(0, 33, Short.MAX_VALUE)
                        .addComponent(addReporterIon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removerReporterIon)))
                .addContainerGap())
        );

        detailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));
        detailsPanel.setOpaque(false);

        typeCmb.setMaximumRowCount(15);
        typeCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Particular Amino Acid", "Protein N-term", "Protein N-term - Particular Amino Acid(s)", "Protein C-term", "Protein C-term - Particular Amino Acid(s)", "Peptide N-term", "Peptide N-term - Particular Amino Acid(s)", "Peptide C-term", "Peptide C-term - Particular Amino Acid(s)" }));
        typeCmb.setToolTipText("The modification type. See help for details.");
        typeCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeCmbActionPerformed(evt);
            }
        });

        jLabel1.setText("Type");
        jLabel1.setToolTipText("The modification type. See help for details.");

        nameLabel.setText("Name");
        nameLabel.setToolTipText("The modification name");

        nameTxt.setEditable(false);
        nameTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nameTxt.setToolTipText("The modification name");
        nameTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTxtKeyReleased(evt);
            }
        });

        compositionTxt.setEditable(false);
        compositionTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        compositionTxt.setToolTipText("Monoisotopic mass in Dalton");
        compositionTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                compositionTxtMouseReleased(evt);
            }
        });

        patternLabel.setText("Pattern");
        patternLabel.setToolTipText("Residues modified");

        patternTxt.setEditable(false);
        patternTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        patternTxt.setToolTipText("Residues modified");
        patternTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                patternTxtMouseReleased(evt);
            }
        });

        nameShortLabel.setText("Short Name");
        nameShortLabel.setToolTipText("The modification name");

        nameShortTxt.setEditable(false);
        nameShortTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nameShortTxt.setToolTipText("The modification name");
        nameShortTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameShortTxtKeyReleased(evt);
            }
        });

        compositionLabel.setText("Composition");

        massLabel.setText("Mass");
        massLabel.setToolTipText("Monoisotopic mass");

        massTxt.setEditable(false);
        massTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        massTxt.setToolTipText("Monoisotopic mass in Dalton");

        javax.swing.GroupLayout detailsPanelLayout = new javax.swing.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(typeCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nameTxt)))
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addComponent(patternLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(patternTxt))
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameShortLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(compositionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(detailsPanelLayout.createSequentialGroup()
                                .addComponent(compositionTxt)
                                .addGap(18, 18, 18)
                                .addComponent(massLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(massTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(nameShortTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE))))
                .addContainerGap())
        );

        detailsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, nameLabel, nameShortLabel, patternLabel});

        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameShortTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameShortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compositionLabel)
                    .addComponent(massLabel)
                    .addComponent(massTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(patternTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        neutralLossesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Neutral Losses"));
        neutralLossesPanel.setOpaque(false);

        neutralLossesTable.setModel(new NeutralLossesTable());
        neutralLossesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        neutralLossesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                neutralLossesTableMouseReleased(evt);
            }
        });
        neutralLossesJScrollPane.setViewportView(neutralLossesTable);

        addNeutralLoss.setText("Add");
        addNeutralLoss.setToolTipText("Add a neutral loss");
        addNeutralLoss.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNeutralLossActionPerformed(evt);
            }
        });

        removeNeutralLoss.setText("Remove");
        removeNeutralLoss.setToolTipText("Remove the selected neutral loss");
        removeNeutralLoss.setEnabled(false);
        removeNeutralLoss.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeNeutralLossActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout neutralLossesPanelLayout = new javax.swing.GroupLayout(neutralLossesPanel);
        neutralLossesPanel.setLayout(neutralLossesPanelLayout);
        neutralLossesPanelLayout.setHorizontalGroup(
            neutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(neutralLossesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(neutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addNeutralLoss, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeNeutralLoss, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(neutralLossesJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        neutralLossesPanelLayout.setVerticalGroup(
            neutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(neutralLossesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(neutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(neutralLossesJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(neutralLossesPanelLayout.createSequentialGroup()
                        .addGap(0, 33, Short.MAX_VALUE)
                        .addComponent(addNeutralLoss)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeNeutralLoss)))
                .addContainerGap())
        );

        javax.swing.GroupLayout scrollPanePanelLayout = new javax.swing.GroupLayout(scrollPanePanel);
        scrollPanePanel.setLayout(scrollPanePanelLayout);
        scrollPanePanelLayout.setHorizontalGroup(
            scrollPanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(detailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(neutralLossesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(unimodMappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(reporterIonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        scrollPanePanelLayout.setVerticalGroup(
            scrollPanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scrollPanePanelLayout.createSequentialGroup()
                .addComponent(detailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(neutralLossesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reporterIonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unimodMappingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        scrollPane.setViewportView(scrollPanePanel);

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPane)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane)
                .addGap(11, 11, 11)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Add the Modification to the PtmDialogParent.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        if (validateInput(true)) {

            if (editable) {

                // check if the unimod cv term mapping is provided
                boolean cvTermOk = true;
                if (!unimodNameJTextField.getText().trim().isEmpty()) {
                    try {
                        new Integer(unimodAccessionJTextField.getText().trim());
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Please provide the Unimod accession number as an integer.", "Unimod Accession", JOptionPane.WARNING_MESSAGE);
                        cvTermOk = false;
                        unimodAccessionLabel.setForeground(Color.RED);
                        unimodAccessionLabel.setToolTipText("Please provide the Unimod accession number as an integer");
                        unimodAccessionJTextField.setToolTipText("Please provide the Unimod accession number as an integer");
                    }
                } else {
                    cvTermOk = false;

                    int option = JOptionPane.showConfirmDialog(this,
                            "Adding a controlled vocabulary mapping is strongly recommended. This\n"
                            + "is for example mandatory when exporting the data to mzIdentML.\n\n"
                            + "Continue without such a mapping?", "Modification Controlled Vocabulary",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (option != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // create the unimod cv term
                CvTerm cvTerm = null;
                if (cvTermOk) {
                    int unimodAccession = new Integer(unimodAccessionJTextField.getText().trim());
                    cvTerm = new CvTerm("UNIMOD", "UNIMOD:" + unimodAccession, unimodNameJTextField.getText().trim(), null);
                }

                ModificationType modificationType = ModificationType.values()[typeCmb.getSelectedIndex()];
                Modification newModification = new Modification(modificationType,
                        nameTxt.getText().trim(),
                        nameShortTxt.getText().trim().toLowerCase(),
                        atomChainAdded, atomChainRemoved, pattern, cvTerm);
                newModification.setNeutralLosses(neutralLosses);
                newModification.setReporterIons(reporterIons);

                for (String modification : modificationFactory.getModifications()) {
                    if (currentPtm == null || !modification.equals(currentPtm.getName())) {
                        Modification otherModification = modificationFactory.getModification(modification);
                        if (newModification.isSameAs(otherModification)) {
                            int outcome = JOptionPane.showConfirmDialog(this, "The modification \'" + modification
                                    + "\' presents characteristics similar to your input.\n"
                                    + "Are you sure you want to create this new modification?",
                                    "Modification Already Exists", JOptionPane.YES_NO_OPTION);
                            if (outcome == JOptionPane.NO_OPTION) {
                                return;
                            }
                        }
                    }
                }

                modificationFactory.addUserModification(newModification); // note: "editable" is here used to decide if it's a user modification
            }

            saveChanges();
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Update the type selection.
     *
     * @param evt
     */
    private void typeCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeCmbActionPerformed
        if (typeCmb.getSelectedIndex() == 0
                || typeCmb.getSelectedIndex() == 2
                || typeCmb.getSelectedIndex() == 4
                || typeCmb.getSelectedIndex() == 6
                || typeCmb.getSelectedIndex() == 8) {
            patternTxt.setEnabled(true);
        } else {
            pattern = null;
            patternTxt.setText(null);
            patternTxt.setEnabled(false);
        }
        validateInput(false);
    }//GEN-LAST:event_typeCmbActionPerformed

    /**
     * Changes the cursor to a hand cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseEntered

    /**
     * Change the cursor to the default cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseExited

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/PtmDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                null, "New Modification - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Add a new Modification dependent neutral losses.
     *
     * @param evt
     */
    private void addNeutralLossActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNeutralLossActionPerformed
        neutralLosses.add(new NeutralLoss("new neutral loss", new AtomChain(), false, null, false));
        updateTables();
    }//GEN-LAST:event_addNeutralLossActionPerformed

    /**
     * Add a new Modification dependent reporter ion.
     *
     * @param evt
     */
    private void addReporterIonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addReporterIonActionPerformed
        reporterIons.add(new ReporterIon("New reporter ion", 0.0, false));
        updateTables();
    }//GEN-LAST:event_addReporterIonActionPerformed

    /**
     * Remove a neutral loss.
     *
     * @param evt
     */
    private void removeNeutralLossActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeNeutralLossActionPerformed
        int row = neutralLossesTable.getSelectedRow();
        if (row != -1) {
            int index = neutralLossesTable.convertRowIndexToModel(row);
            neutralLosses.remove(index);
            updateTables();
        }
        row = neutralLossesTable.getSelectedRow();
        removeNeutralLoss.setEnabled(row != -1);
    }//GEN-LAST:event_removeNeutralLossActionPerformed

    /**
     * Remove a reporter ion.
     *
     * @param evt
     */
    private void removerReporterIonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removerReporterIonActionPerformed
        int row = reporterIonsTable.getSelectedRow();
        if (row != -1) {
            int index = reporterIonsTable.convertRowIndexToModel(row);
            reporterIons.remove(index);
            updateTables();
        }
        row = reporterIonsTable.getSelectedRow();
        removerReporterIon.setEnabled(row != -1);
    }//GEN-LAST:event_removerReporterIonActionPerformed

    /**
     * Enable/disable the remove neutral loss button.
     *
     * @param evt
     */
    private void neutralLossesTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neutralLossesTableMouseReleased
        if (editable) {
            int row = neutralLossesTable.getSelectedRow();
            removeNeutralLoss.setEnabled(row != -1);
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2 && editable) {
            int column = neutralLossesTable.columnAtPoint(evt.getPoint());
            if (column == 2 || column == 3) {
                int row = neutralLossesTable.rowAtPoint(evt.getPoint());
                NeutralLoss neutralLoss = neutralLosses.get(row);
                AtomChain atomChain = neutralLoss.getComposition();
                AtomChainDialog atomChainDialog = new AtomChainDialog(this, atomChain, new AtomChain(), true);
                if (!atomChainDialog.isCanceled()) {
                    atomChain = atomChainDialog.getAtomChainAdded();
                    if (atomChain.size() > 0) {
                        neutralLoss.setComposition(atomChain);
                    }
                    updateTables();
                }
            }
        }
    }//GEN-LAST:event_neutralLossesTableMouseReleased

    /**
     * Enable/disable the remove reporter ion button.
     *
     * @param evt
     */
    private void reporterIonsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reporterIonsTableMouseReleased
        if (editable) {
            int row = reporterIonsTable.getSelectedRow();
            removerReporterIon.setEnabled(row != -1);
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2 && editable) {
            int column = reporterIonsTable.columnAtPoint(evt.getPoint());
            if (column == 2 || column == 3) {
                int row = reporterIonsTable.rowAtPoint(evt.getPoint());
                ReporterIon reporterIon = reporterIons.get(row);
                AtomChain atomChain = reporterIon.getAtomicComposition();
                AtomChainDialog atomChainDialog = new AtomChainDialog(this, atomChain, new AtomChain(), true);
                if (!atomChainDialog.isCanceled()) {
                    atomChain = atomChainDialog.getAtomChainAdded();
                    if (atomChain.size() > 0) {
                        reporterIon.setAtomicComposition(atomChain);
                    }
                    updateTables();
                }
            }
        }
    }//GEN-LAST:event_reporterIonsTableMouseReleased

    /**
     * Open the amino acid pattern dialog.
     *
     * @param evt
     */
    private void patternTxtMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_patternTxtMouseReleased
        if (editable && patternTxt.isEnabled() && evt.getButton() == MouseEvent.BUTTON1) {
            AminoAcidPatternDialog dialog = new AminoAcidPatternDialog(null, pattern, editable);
            if (!dialog.isCanceled()) {
                pattern = dialog.getPattern();
                patternTxt.setText(pattern.toString());
                validateInput(false);
            }
        }
    }//GEN-LAST:event_patternTxtMouseReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nameTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_nameTxtKeyReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void unimodLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unimodLinkLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_unimodLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void unimodLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unimodLinkLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_unimodLinkLabelMouseExited

    /**
     * Open the Unimod web page.
     *
     * @param evt
     */
    private void unimodLinkLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unimodLinkLabelMouseReleased
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.unimod.org");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_unimodLinkLabelMouseReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void unimodAccessionJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_unimodAccessionJTextFieldKeyReleased
        validateInput(false);
    }//GEN-LAST:event_unimodAccessionJTextFieldKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void unimodNameJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_unimodNameJTextFieldKeyReleased
        validateInput(false);
    }//GEN-LAST:event_unimodNameJTextFieldKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nameShortTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameShortTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_nameShortTxtKeyReleased

    /**
     * Open the AtomChainDialog for editing the atomics composition.
     *
     * @param evt
     */
    private void compositionTxtMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_compositionTxtMouseReleased
        if (editable) {
            AtomChainDialog atomChainDialog = new AtomChainDialog(this, atomChainAdded, atomChainRemoved, false);
            if (!atomChainDialog.isCanceled()) {
                atomChainAdded = atomChainDialog.getAtomChainAdded();
                atomChainRemoved = atomChainDialog.getAtomChainRemoved();
                
                String addition = "";
                if (atomChainAdded.size() > 0) {
                    addition = atomChainAdded.toString();
                }
                String deletion = "";
                if (atomChainRemoved.size() > 0) {
                    deletion = "-" + atomChainRemoved.toString();
                }
                String temp = addition + " " + deletion;
                compositionTxt.setText(temp.trim());
            }

            validateInput(false);
            updateMass();
        }
    }//GEN-LAST:event_compositionTxtMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNeutralLoss;
    private javax.swing.JButton addReporterIon;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel compositionLabel;
    private javax.swing.JTextField compositionTxt;
    private javax.swing.JLabel cvExampleLabel;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel massLabel;
    private javax.swing.JTextField massTxt;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel nameShortLabel;
    private javax.swing.JTextField nameShortTxt;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JScrollPane neutralLossesJScrollPane;
    private javax.swing.JPanel neutralLossesPanel;
    private javax.swing.JTable neutralLossesTable;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel patternLabel;
    private javax.swing.JTextField patternTxt;
    private javax.swing.JButton removeNeutralLoss;
    private javax.swing.JButton removerReporterIon;
    private javax.swing.JScrollPane reporterIonsJScrollPane;
    private javax.swing.JPanel reporterIonsPanel;
    private javax.swing.JTable reporterIonsTable;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JPanel scrollPanePanel;
    private javax.swing.JComboBox typeCmb;
    private javax.swing.JTextField unimodAccessionJTextField;
    private javax.swing.JLabel unimodAccessionLabel;
    private javax.swing.JLabel unimodLinkLabel;
    private javax.swing.JPanel unimodMappingPanel;
    private javax.swing.JTextField unimodNameJTextField;
    private javax.swing.JLabel unimodNameLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Displays the Unimod mapping information.
     */
    private void updateModMappingText(CvTerm cvTerm) {
        unimodAccessionJTextField.setText(cvTerm.getAccession().substring("Unimod:".length()));
        unimodNameJTextField.setText(cvTerm.getName());
        unimodNameJTextField.setCaretPosition(0);
    }

    /**
     * Update the neutral losses and reporter ions tables.
     */
    private void updateTables() {
        ((DefaultTableModel) neutralLossesTable.getModel()).fireTableDataChanged();
        ((DefaultTableModel) reporterIonsTable.getModel()).fireTableDataChanged();
        validateInput(false);
    }

    /**
     * Saves the changes of the Modification factory.
     */
    private void saveChanges() {
        try {
            modificationFactory.saveFactory();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while saving the modification.", "Saving Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Table model for the neutral losses table.
     */
    private class NeutralLossesTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return neutralLosses.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                case 2:
                    return "Composition";
                case 3:
                    return "Mass";
                case 4:
                    return "Fixed";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return neutralLosses.get(row).name;
                case 2:
                    NeutralLoss neutralLoss = neutralLosses.get(row);
                    if (neutralLoss.getComposition() != null) {
                        return neutralLoss.getComposition().toString();
                    }
                    return "";
                case 3:
                    neutralLoss = neutralLosses.get(row);
                    return neutralLoss.getMass();
                case 4:
                    return neutralLosses.get(row).isFixed();
                default:
                    return "";
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, columnIndex) != null) {
                    return getValueAt(i, columnIndex).getClass();
                }
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 1 || columnIndex == 4) && editable;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            int index = neutralLossesTable.convertRowIndexToModel(row);
            NeutralLoss neutralLoss = neutralLosses.get(index);
            if (column == 1) {
                String newName = aValue.toString();
                NeutralLoss newLoss = new NeutralLoss(newName, neutralLoss.getComposition(), neutralLoss.isFixed());
                neutralLosses.set(index, newLoss);
            } else if (column == 4) {
                neutralLoss.setFixed((Boolean) aValue);
            }
        }
    }

    /**
     * Table model for the reporter ions table.
     */
    private class ReporterIonsTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return reporterIons.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
                case 2:
                    return "Composition";
                case 3:
                    return "Mass";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return reporterIons.get(row).getName();
                case 2:
                    ReporterIon reporterIon = reporterIons.get(row);
                    if (reporterIon.getAtomicComposition() != null) {
                        return reporterIon.getAtomicComposition().toString();
                    }
                    return "";
                case 3:
                    reporterIon = reporterIons.get(row);
                    return reporterIon.getTheoreticMz(1);
                default:
                    return "";
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, columnIndex) != null) {
                    return getValueAt(i, columnIndex).getClass();
                }
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 && editable;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            int index = reporterIonsTable.convertRowIndexToModel(row);
            ReporterIon reporterIon = reporterIons.get(index);
            if (column == 1) {
                reporterIon.setName((String) aValue);
            }
        }
    }
}
