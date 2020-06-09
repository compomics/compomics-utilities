package com.compomics.util.gui.modification;

import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.ions.impl.ReporterIon;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.gui.AminoAcidPatternDialog;
import com.compomics.util.gui.atoms.AtomChainDialog;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.renderers.ToolTipComboBoxRenderer;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.pride.PtmToPrideMap;
import java.awt.Color;
import java.awt.ComponentOrientation;
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
     * @param editable boolean indicating whether the user can edit the
     * Modification details
     */
    public ModificationDialog(JDialog parent, Modification currentModification, boolean editable) {
        super(parent, true);

        this.currentPtm = currentModification;
        if (currentModification != null) {
            this.pattern = currentPtm.getPattern();
            this.atomChainAdded = currentPtm.getAtomChainAdded();
            this.atomChainRemoved = currentPtm.getAtomChainRemoved();
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
     * @param editable boolean indicating whether the user can edit the
     * Modification details
     */
    public ModificationDialog(JFrame parent, PtmToPrideMap modificationToPrideMap, 
            Modification currentModification, boolean editable) {
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

        // make the tabs in the losses and report ions tabbed pane go from right to left
        neutralLossesAndReportIonsTabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // centrally align the comboboxes
        typeCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        categoryCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

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
                new ImageIcon(this.getClass().getResource("/icons/selected_green-new.png")),
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
        for (ModificationType modType : ModificationType.values()) {
            comboboxTooltips.add(modType.description);
        }
        typeCmb.setRenderer(new ToolTipComboBoxRenderer(comboboxTooltips, SwingConstants.CENTER));

        typeCmb.setEnabled(editable);
        categoryCmb.setEnabled(editable);
        nameTxt.setEditable(editable);
        nameShortTxt.setEditable(editable);
        addNeutralLoss.setEnabled(editable);
        removeNeutralLoss.setEnabled(editable);
        addReporterIon.setEnabled(editable);
        removerReporterIon.setEnabled(editable);
        patternTxt.setEditable(editable);
        unimodAccessionJTextField.setEditable(editable);
        unimodNameJTextField.setEditable(editable);
        psiModAccessionJTextField.setEditable(editable);
        psiModNameJTextField.setEditable(editable);

        if (currentPtm != null) {
            typeCmb.setSelectedItem(currentPtm.getModificationType());
            categoryCmb.setSelectedItem(currentPtm.getCategory());
            nameTxt.setText(currentPtm.getName());
            nameShortTxt.setText(currentPtm.getShortName());

            String addition = "";
            if (atomChainAdded != null && atomChainAdded.size() > 0) {
                addition = atomChainAdded.toString();
            }
            String deletion = "";
            if (atomChainRemoved != null && atomChainRemoved.size() > 0) {
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

            CvTerm unimodCvTerm = currentPtm.getUnimodCvTerm();
            CvTerm psiModcvTerm = currentPtm.getPsiModCvTerm();
            updateModMappingText(unimodCvTerm, psiModcvTerm);

            // special case for the default modifications without cv terms
            if (unimodCvTerm == null) {
                unimodAccessionJTextField.setEditable(true);
                unimodNameJTextField.setEditable(true);
            }
            if (psiModcvTerm == null) {
                psiModAccessionJTextField.setEditable(true);
                psiModNameJTextField.setEditable(true);
            }

            setTitle("Edit Modification");
        }
        
        cvTermSplitPane.setDividerLocation(0.5);

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
        ((TitledBorder) reporterIonsAndNeutralLossesPanel.getBorder()).setTitleColor(Color.BLACK);
        reporterIonsAndNeutralLossesPanel.repaint();

        nameLabel.setToolTipText(null);
        nameTxt.setToolTipText(null);
        compositionLabel.setToolTipText(null);
        compositionTxt.setToolTipText(null);
        patternLabel.setToolTipText(null);
        patternTxt.setToolTipText(null);
        unimodAccessionLabel.setToolTipText(null);
        unimodAccessionJTextField.setToolTipText(null);
        reporterIonsAndNeutralLossesPanel.setToolTipText(null);
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

            ModificationType tempModificationType = (ModificationType) typeCmb.getSelectedItem();
            
            if (tempModificationType == ModificationType.modaa
                || tempModificationType == ModificationType.modcaa_peptide
                || tempModificationType == ModificationType.modcaa_protein
                || tempModificationType == ModificationType.modnaa_peptide
                || tempModificationType == ModificationType.modnaa_protein) {

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

        if (!psiModAccessionJTextField.getText().trim().isEmpty()) {
            try {
                new Integer(psiModAccessionJTextField.getText().trim());
            } catch (NumberFormatException e) {
                if (showMessage && !error) {
                    JOptionPane.showMessageDialog(this, "Please provide the PSI-MOD accession number as an integer.", "PSI-MOD Accession", JOptionPane.WARNING_MESSAGE);
                }
                error = true;
                psiModAccessionLabel.setForeground(Color.RED);
                psiModAccessionLabel.setToolTipText("Please provide the PSI-MOD accession number as an integer");
                psiModAccessionJTextField.setToolTipText("Please provide the PSI-MOD accession number as an integer");
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
                    ((TitledBorder) reporterIonsAndNeutralLossesPanel.getBorder()).setTitleColor(Color.RED);
                    reporterIonsAndNeutralLossesPanel.setToolTipText("A neutral loss named \'" + tempNeutralLoss.name + "\' already exists");
                    reporterIonsAndNeutralLossesPanel.repaint();
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
        propertiesPanel = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        typeCmb = new javax.swing.JComboBox();
        categoryLabel = new javax.swing.JLabel();
        categoryCmb = new javax.swing.JComboBox();
        nameLabel = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        nameShortLabel = new javax.swing.JLabel();
        nameShortTxt = new javax.swing.JTextField();
        compositionLabel = new javax.swing.JLabel();
        compositionTxt = new javax.swing.JTextField();
        massLabel = new javax.swing.JLabel();
        massTxt = new javax.swing.JTextField();
        patternLabel = new javax.swing.JLabel();
        patternTxt = new javax.swing.JTextField();
        reporterIonsAndNeutralLossesPanel = new javax.swing.JPanel();
        neutralLossesAndReportIonsTabbedPane = new javax.swing.JTabbedPane();
        neutralLossesPanel = new javax.swing.JPanel();
        removeNeutralLoss = new javax.swing.JButton();
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
        addReporterIon = new javax.swing.JButton();
        removerReporterIon = new javax.swing.JButton();
        unimodMappingPanel = new javax.swing.JPanel();
        cvTermSplitPane = new javax.swing.JSplitPane();
        unimodPanel = new javax.swing.JPanel();
        unimodNameJTextField = new javax.swing.JTextField();
        unimodAccessionJTextField = new javax.swing.JTextField();
        unimodLinkLabel = new javax.swing.JLabel();
        unimodLabel = new javax.swing.JLabel();
        unimodNameLabel = new javax.swing.JLabel();
        unimodAccessionLabel = new javax.swing.JLabel();
        psiModPanel = new javax.swing.JPanel();
        psiModLinkLabel = new javax.swing.JLabel();
        psiModAccessionJTextField = new javax.swing.JTextField();
        psiModLabel = new javax.swing.JLabel();
        psiModNameJTextField = new javax.swing.JTextField();
        psiModNameLabel = new javax.swing.JLabel();
        psiModAccessionLabel = new javax.swing.JLabel();
        helpJButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Modification");
        setMinimumSize(new java.awt.Dimension(400, 500));

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));
        backgroundPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));
        propertiesPanel.setOpaque(false);

        typeLabel.setText("Type");
        typeLabel.setToolTipText("The modification type. See help for details.");

        typeCmb.setMaximumRowCount(15);
        typeCmb.setModel(new DefaultComboBoxModel(ModificationType.values()));
        typeCmb.setToolTipText("The modification type. See help for details.");
        typeCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeCmbActionPerformed(evt);
            }
        });

        categoryLabel.setText("Category");
        categoryLabel.setToolTipText("The modification type. See help for details.");

        categoryCmb.setMaximumRowCount(15);
        categoryCmb.setModel(new DefaultComboBoxModel(ModificationCategory.values()));
        categoryCmb.setToolTipText("The modification category.");

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

        nameShortLabel.setText("Short");
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

        compositionTxt.setEditable(false);
        compositionTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        compositionTxt.setToolTipText("Monoisotopic mass in Dalton");
        compositionTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                compositionTxtMouseReleased(evt);
            }
        });

        massLabel.setText("Mass");
        massLabel.setToolTipText("Monoisotopic mass");

        massTxt.setEditable(false);
        massTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        massTxt.setToolTipText("Monoisotopic mass in Dalton");

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

        javax.swing.GroupLayout propertiesPanelLayout = new javax.swing.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(propertiesPanelLayout.createSequentialGroup()
                        .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                            .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(typeCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(categoryCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(propertiesPanelLayout.createSequentialGroup()
                                .addComponent(nameTxt)
                                .addGap(18, 18, 18)
                                .addComponent(nameShortLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nameShortTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(propertiesPanelLayout.createSequentialGroup()
                        .addComponent(categoryLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(propertiesPanelLayout.createSequentialGroup()
                        .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(patternLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(compositionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(propertiesPanelLayout.createSequentialGroup()
                                .addComponent(compositionTxt)
                                .addGap(18, 18, 18)
                                .addComponent(massLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(massTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(patternTxt))))
                .addContainerGap())
        );

        propertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nameLabel, patternLabel, typeLabel});

        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(typeLabel))
                .addGap(0, 0, 0)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryLabel)
                    .addComponent(categoryCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel)
                    .addComponent(nameShortLabel)
                    .addComponent(nameShortTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compositionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compositionLabel)
                    .addComponent(massLabel)
                    .addComponent(massTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(patternTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patternLabel))
                .addContainerGap())
        );

        reporterIonsAndNeutralLossesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Reporter Ions & Neutral Losses"));
        reporterIonsAndNeutralLossesPanel.setOpaque(false);

        neutralLossesAndReportIonsTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        neutralLossesPanel.setOpaque(false);

        removeNeutralLoss.setText("Remove");
        removeNeutralLoss.setToolTipText("Remove the selected neutral loss");
        removeNeutralLoss.setEnabled(false);
        removeNeutralLoss.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeNeutralLossActionPerformed(evt);
            }
        });

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
                .addComponent(neutralLossesJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                .addContainerGap())
        );
        neutralLossesPanelLayout.setVerticalGroup(
            neutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(neutralLossesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(neutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(neutralLossesJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(neutralLossesPanelLayout.createSequentialGroup()
                        .addComponent(addNeutralLoss)
                        .addGap(0, 0, 0)
                        .addComponent(removeNeutralLoss)
                        .addGap(0, 68, Short.MAX_VALUE)))
                .addContainerGap())
        );

        neutralLossesAndReportIonsTabbedPane.addTab("Neutral Losses", neutralLossesPanel);

        reporterIonsPanel.setOpaque(false);

        reporterIonsTable.setModel(new ReporterIonsTable());
        reporterIonsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        reporterIonsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                reporterIonsTableMouseReleased(evt);
            }
        });
        reporterIonsJScrollPane.setViewportView(reporterIonsTable);

        addReporterIon.setText("Add");
        addReporterIon.setToolTipText("Add a reporter ion");
        addReporterIon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addReporterIonActionPerformed(evt);
            }
        });

        removerReporterIon.setText("Remove");
        removerReporterIon.setToolTipText("Remove the selected reporter ion");
        removerReporterIon.setEnabled(false);
        removerReporterIon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removerReporterIonActionPerformed(evt);
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
                .addComponent(reporterIonsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                .addContainerGap())
        );
        reporterIonsPanelLayout.setVerticalGroup(
            reporterIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterIonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reporterIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reporterIonsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .addGroup(reporterIonsPanelLayout.createSequentialGroup()
                        .addComponent(addReporterIon)
                        .addGap(0, 0, 0)
                        .addComponent(removerReporterIon)))
                .addContainerGap())
        );

        neutralLossesAndReportIonsTabbedPane.addTab("Reporter Ions", reporterIonsPanel);

        neutralLossesAndReportIonsTabbedPane.setSelectedIndex(1);

        javax.swing.GroupLayout reporterIonsAndNeutralLossesPanelLayout = new javax.swing.GroupLayout(reporterIonsAndNeutralLossesPanel);
        reporterIonsAndNeutralLossesPanel.setLayout(reporterIonsAndNeutralLossesPanelLayout);
        reporterIonsAndNeutralLossesPanelLayout.setHorizontalGroup(
            reporterIonsAndNeutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterIonsAndNeutralLossesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(neutralLossesAndReportIonsTabbedPane)
                .addContainerGap())
        );
        reporterIonsAndNeutralLossesPanelLayout.setVerticalGroup(
            reporterIonsAndNeutralLossesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterIonsAndNeutralLossesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(neutralLossesAndReportIonsTabbedPane))
        );

        unimodMappingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Unimod and PSI-MOD Mapping"));
        unimodMappingPanel.setOpaque(false);

        cvTermSplitPane.setDividerLocation(300);
        cvTermSplitPane.setDividerSize(0);
        cvTermSplitPane.setResizeWeight(0.5);

        unimodPanel.setOpaque(false);

        unimodNameJTextField.setEditable(false);
        unimodNameJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        unimodNameJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unimodNameJTextFieldKeyReleased(evt);
            }
        });

        unimodAccessionJTextField.setEditable(false);
        unimodAccessionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        unimodAccessionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                unimodAccessionJTextFieldKeyReleased(evt);
            }
        });

        unimodLinkLabel.setText("<html><a href>See: unimod.org</a></html>");
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

        unimodLabel.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        unimodLabel.setText("Unimod");

        unimodNameLabel.setText("PSI-MS Name");

        unimodAccessionLabel.setText("Accession");

        javax.swing.GroupLayout unimodPanelLayout = new javax.swing.GroupLayout(unimodPanel);
        unimodPanel.setLayout(unimodPanelLayout);
        unimodPanelLayout.setHorizontalGroup(
            unimodPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(unimodPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(unimodPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(unimodPanelLayout.createSequentialGroup()
                        .addComponent(unimodLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(unimodPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(unimodPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(unimodAccessionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(unimodNameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(unimodPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, unimodPanelLayout.createSequentialGroup()
                                .addGap(0, 109, Short.MAX_VALUE)
                                .addComponent(unimodLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(unimodNameJTextField)
                            .addComponent(unimodAccessionJTextField))))
                .addContainerGap())
        );
        unimodPanelLayout.setVerticalGroup(
            unimodPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(unimodPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(unimodLabel)
                .addGroup(unimodPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unimodAccessionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unimodAccessionLabel))
                .addGap(0, 0, 0)
                .addGroup(unimodPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unimodNameLabel)
                    .addComponent(unimodNameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(unimodLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cvTermSplitPane.setLeftComponent(unimodPanel);

        psiModPanel.setOpaque(false);

        psiModLinkLabel.setText("<html><a href>See: ebi.ac.uk/ols/ontologies/mod</a></html>");
        psiModLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                psiModLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                psiModLinkLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                psiModLinkLabelMouseReleased(evt);
            }
        });

        psiModAccessionJTextField.setEditable(false);
        psiModAccessionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        psiModAccessionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                psiModAccessionJTextFieldKeyReleased(evt);
            }
        });

        psiModLabel.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        psiModLabel.setText("PSI-MOD");

        psiModNameJTextField.setEditable(false);
        psiModNameJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        psiModNameJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                psiModNameJTextFieldKeyReleased(evt);
            }
        });

        psiModNameLabel.setText("Name");

        psiModAccessionLabel.setText("Accession");

        javax.swing.GroupLayout psiModPanelLayout = new javax.swing.GroupLayout(psiModPanel);
        psiModPanel.setLayout(psiModPanelLayout);
        psiModPanelLayout.setHorizontalGroup(
            psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psiModPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(psiModPanelLayout.createSequentialGroup()
                        .addComponent(psiModLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(psiModPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(psiModPanelLayout.createSequentialGroup()
                                .addGroup(psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(psiModAccessionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(psiModNameLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(psiModAccessionJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                                    .addComponent(psiModNameJTextField)))
                            .addComponent(psiModLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        psiModPanelLayout.setVerticalGroup(
            psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psiModPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(psiModLabel)
                .addGap(0, 0, 0)
                .addGroup(psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psiModAccessionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(psiModAccessionLabel))
                .addGap(0, 0, 0)
                .addGroup(psiModPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psiModNameLabel)
                    .addComponent(psiModNameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(psiModLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cvTermSplitPane.setRightComponent(psiModPanel);

        javax.swing.GroupLayout unimodMappingPanelLayout = new javax.swing.GroupLayout(unimodMappingPanel);
        unimodMappingPanel.setLayout(unimodMappingPanelLayout);
        unimodMappingPanelLayout.setHorizontalGroup(
            unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(unimodMappingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cvTermSplitPane)
                .addContainerGap())
        );
        unimodMappingPanelLayout.setVerticalGroup(
            unimodMappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cvTermSplitPane)
        );

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

        okButton.setText("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(propertiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(unimodMappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(reporterIonsAndNeutralLossesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(propertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reporterIonsAndNeutralLossesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unimodMappingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
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
                        JOptionPane.showMessageDialog(this,
                                "Please provide the Unimod accession number as an integer.",
                                "Unimod Accession", JOptionPane.WARNING_MESSAGE);
                        cvTermOk = false;
                        unimodAccessionLabel.setForeground(Color.RED);
                        unimodAccessionLabel.setToolTipText("Please provide the Unimod accession number as an integer");
                        unimodAccessionJTextField.setToolTipText("Please provide the Unimod accession number as an integer");
                    }
                } else {
                    cvTermOk = false;

                    int option = JOptionPane.showConfirmDialog(this,
                            "Adding a mapping to Unimod is strongly recommended. This\n"
                            + "is for example mandatory when exporting data to mzIdentML.\n\n"
                            + "Continue without such a mapping?", "Unimod Mapping",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (option != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // create the unimod cv term
                CvTerm unimodCvTerm = null;
                if (cvTermOk) {
                    int unimodAccession = new Integer(unimodAccessionJTextField.getText().trim());
                    unimodCvTerm = new CvTerm("UNIMOD", "UNIMOD:" + unimodAccession, unimodNameJTextField.getText().trim(), null);
                }

                // check if the psi-mod cv term mapping is provided
                cvTermOk = true;
                if (!psiModNameJTextField.getText().trim().isEmpty()) {
                    try {
                        new Integer(psiModAccessionJTextField.getText().trim());
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Please provide the PSI-MOD accession number as an integer.", "PSI-MOD Accession", JOptionPane.WARNING_MESSAGE);
                        cvTermOk = false;
                        psiModAccessionLabel.setForeground(Color.RED);
                        psiModAccessionLabel.setToolTipText("Please provide the PSI-MOD accession number as an integer");
                        psiModAccessionJTextField.setToolTipText("Please provide the PSI-MOD accession number as an integer");
                    }
                } else {
                    cvTermOk = false;
//
//                    int option = JOptionPane.showConfirmDialog(this,
//                            "Adding a controlled vocabulary mapping is strongly recommended. This\n"
//                            + "is for example mandatory when exporting the data to mzIdentML.\n\n"
//                            + "Continue without such a mapping?", "Modification Controlled Vocabulary",
//                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
//
//                    if (option != JOptionPane.YES_OPTION) {
//                        return;
//                    }
                }

                // create the psi-mod cv term
                CvTerm psiModCvTerm = null;
                if (cvTermOk) {
                    int psiModAccession = new Integer(psiModAccessionJTextField.getText().trim());
                    psiModCvTerm = new CvTerm("MOD", "MOD:" + psiModAccession, psiModNameJTextField.getText().trim(), null);
                }

                Modification newModification = new Modification(
                        (ModificationType) typeCmb.getSelectedItem(),
                        nameTxt.getText().trim(),
                        nameShortTxt.getText().trim().toLowerCase(),
                        atomChainAdded, atomChainRemoved, pattern, unimodCvTerm, psiModCvTerm,
                        (ModificationCategory) categoryCmb.getSelectedItem());
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

        ModificationType tempModificationType = (ModificationType) typeCmb.getSelectedItem();

        if (tempModificationType == ModificationType.modaa
                || tempModificationType == ModificationType.modcaa_peptide
                || tempModificationType == ModificationType.modcaa_protein
                || tempModificationType == ModificationType.modnaa_peptide
                || tempModificationType == ModificationType.modnaa_protein) {
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

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void psiModNameJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_psiModNameJTextFieldKeyReleased
        validateInput(false);
    }//GEN-LAST:event_psiModNameJTextFieldKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void psiModAccessionJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_psiModAccessionJTextFieldKeyReleased
        validateInput(false);
    }//GEN-LAST:event_psiModAccessionJTextFieldKeyReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void psiModLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psiModLinkLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_psiModLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void psiModLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psiModLinkLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_psiModLinkLabelMouseExited

    /**
     * Open the OLS PSI-MOD web page.
     *
     * @param evt
     */
    private void psiModLinkLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psiModLinkLabelMouseReleased
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://www.ebi.ac.uk/ols/ontologies/mod");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_psiModLinkLabelMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNeutralLoss;
    private javax.swing.JButton addReporterIon;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox categoryCmb;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JLabel compositionLabel;
    private javax.swing.JTextField compositionTxt;
    private javax.swing.JSplitPane cvTermSplitPane;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel massLabel;
    private javax.swing.JTextField massTxt;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel nameShortLabel;
    private javax.swing.JTextField nameShortTxt;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JTabbedPane neutralLossesAndReportIonsTabbedPane;
    private javax.swing.JScrollPane neutralLossesJScrollPane;
    private javax.swing.JPanel neutralLossesPanel;
    private javax.swing.JTable neutralLossesTable;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel patternLabel;
    private javax.swing.JTextField patternTxt;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JTextField psiModAccessionJTextField;
    private javax.swing.JLabel psiModAccessionLabel;
    private javax.swing.JLabel psiModLabel;
    private javax.swing.JLabel psiModLinkLabel;
    private javax.swing.JTextField psiModNameJTextField;
    private javax.swing.JLabel psiModNameLabel;
    private javax.swing.JPanel psiModPanel;
    private javax.swing.JButton removeNeutralLoss;
    private javax.swing.JButton removerReporterIon;
    private javax.swing.JPanel reporterIonsAndNeutralLossesPanel;
    private javax.swing.JScrollPane reporterIonsJScrollPane;
    private javax.swing.JPanel reporterIonsPanel;
    private javax.swing.JTable reporterIonsTable;
    private javax.swing.JComboBox typeCmb;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField unimodAccessionJTextField;
    private javax.swing.JLabel unimodAccessionLabel;
    private javax.swing.JLabel unimodLabel;
    private javax.swing.JLabel unimodLinkLabel;
    private javax.swing.JPanel unimodMappingPanel;
    private javax.swing.JTextField unimodNameJTextField;
    private javax.swing.JLabel unimodNameLabel;
    private javax.swing.JPanel unimodPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Displays the CV mapping information.
     */
    private void updateModMappingText(CvTerm uniModCvTerm, CvTerm psiModCvTerm) {
        if (uniModCvTerm != null) {
            unimodAccessionJTextField.setText(uniModCvTerm.getAccession().substring("Unimod:".length()));
            unimodNameJTextField.setText(uniModCvTerm.getName());
            unimodNameJTextField.setCaretPosition(0);
        }
        if (psiModCvTerm != null) {
            psiModAccessionJTextField.setText(psiModCvTerm.getAccession().substring("MOD:".length()));
            psiModNameJTextField.setText(psiModCvTerm.getName());
            psiModNameJTextField.setCaretPosition(0);
        }
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
