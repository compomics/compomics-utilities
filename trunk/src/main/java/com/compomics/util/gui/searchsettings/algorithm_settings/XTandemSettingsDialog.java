package com.compomics.util.gui.searchsettings.algorithm_settings;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.XtandemParameters;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.ptm.ModificationsDialog;
import com.compomics.util.gui.ptm.PtmDialogParent;
import com.compomics.util.preferences.ModificationProfile;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;

/**
 * Dialog for the X!Tandem specific settings.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class XTandemSettingsDialog extends javax.swing.JDialog implements PtmDialogParent {

    /**
     * The X!Tandem parameters class containing the information to display.
     */
    private XtandemParameters xtandemParameters;
    /**
     * The modification profile used for the search.
     */
    private ModificationProfile modificationProfile;
    /**
     * The fragment ion mass accuracy.
     */
    private double fragmentIonMassAccuracy;
    /**
     * Boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;
    /**
     * The modification table column header tooltips.
     */
    private ArrayList<String> modificationTableToolTips;
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();

    /**
     * Creates new form XtandemParametersDialog.
     *
     * @param parent the parent frame
     * @param xtandemParameters the X!Tandem parameters
     * @param modificationProfile the modification profile of the search
     * @param fragmentIonMassAccuracy the fragment ion mass accuracy of the mass
     * spectrometer
     */
    public XTandemSettingsDialog(java.awt.Frame parent, XtandemParameters xtandemParameters, ModificationProfile modificationProfile, double fragmentIonMassAccuracy) {
        super(parent, true);
        this.xtandemParameters = xtandemParameters;
        this.modificationProfile = new ModificationProfile(modificationProfile);
        this.fragmentIonMassAccuracy = fragmentIonMassAccuracy;
        initComponents();
        setUpGUI();
        fillGUI();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {
        noiseSuppressionCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        quickAcetylCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        quickPyroCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        stpBiasCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        outputProteinsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        outputSequencesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        outputSpectraCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        outputHistogramsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        refinementCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        unanticipatedCleavageCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        semiEnzymaticCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        potentialModificationsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        pointMutationsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        snapsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        spectrumSynthesisCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        modificationTableToolTips = new ArrayList<String>();
        modificationTableToolTips.add(null);
        modificationTableToolTips.add("Modification Name");
        modificationTableToolTips.add("Modification Mass");
        modificationTableToolTips.add("Variable Refinement Modification");
        modificationTableToolTips.add("Fixed Refinement Modification");

        modificationsJScrollPane.getViewport().setOpaque(false);
        modificationsTable.getTableHeader().setReorderingAllowed(false);

        setAllModificationTableProperties();
    }

    /**
     * Set the properties of the all modification table.
     */
    private void setAllModificationTableProperties() {
        modificationsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        modificationsTable.getColumn(" ").setMaxWidth(35);
        modificationsTable.getColumn(" ").setMinWidth(35);
        modificationsTable.getColumn("Mass").setMaxWidth(100);
        modificationsTable.getColumn("Mass").setMinWidth(100);
        modificationsTable.getColumn("F").setCellRenderer(new NimbusCheckBoxRenderer());
        modificationsTable.getColumn("V").setCellRenderer(new NimbusCheckBoxRenderer());
        modificationsTable.getColumn("F").setMaxWidth(30);
        modificationsTable.getColumn("F").setMinWidth(30);
        modificationsTable.getColumn("V").setMaxWidth(30);
        modificationsTable.getColumn("V").setMinWidth(30);
    }

    /**
     * Fills the GUI with the information contained in the X!Tandem settings
     * object.
     */
    private void fillGUI() {
        if (xtandemParameters.getDynamicRange() != null) {
            dynamicRangeTxt.setText(xtandemParameters.getDynamicRange() + "");
        }
        if (xtandemParameters.getnPeaks() != null) {
            nPeaksTxt.setText(xtandemParameters.getnPeaks() + "");
        }
        if (xtandemParameters.getMinFragmentMz() != null) {
            minFragmentMzTxt.setText(xtandemParameters.getMinFragmentMz() + "");
        }
        if (xtandemParameters.getMinPeaksPerSpectrum() != null) {
            minPeaksTxt.setText(xtandemParameters.getMinPeaksPerSpectrum() + "");
        }
        if (xtandemParameters.isUseNoiseSuppression()) {
            noiseSuppressionCmb.setSelectedIndex(0);
            minPrecMassTxt.setEnabled(true);
        } else {
            noiseSuppressionCmb.setSelectedIndex(1);
            minPrecMassTxt.setEnabled(false);
        }
        if (xtandemParameters.getMinPrecursorMass() != null) {
            minPrecMassTxt.setText(xtandemParameters.getMinPrecursorMass() + "");
        }
        if (xtandemParameters.isProteinQuickAcetyl()) {
            quickAcetylCmb.setSelectedIndex(0);
        } else {
            quickAcetylCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isQuickPyrolidone()) {
            quickPyroCmb.setSelectedIndex(0);
        } else {
            quickPyroCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isStpBias()) {
            stpBiasCmb.setSelectedIndex(0);
        } else {
            stpBiasCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefine()) {
            refinementCmb.setSelectedIndex(0);
            maxEValueRefineTxt.setEnabled(true);
            unanticipatedCleavageCmb.setEnabled(true);
            semiEnzymaticCmb.setEnabled(true);
            potentialModificationsCmb.setEnabled(true);
            pointMutationsCmb.setEnabled(true);
            snapsCmb.setEnabled(true);
            spectrumSynthesisCmb.setEnabled(true);
        } else {
            refinementCmb.setSelectedIndex(1);
            maxEValueRefineTxt.setEnabled(false);
            unanticipatedCleavageCmb.setEnabled(false);
            semiEnzymaticCmb.setEnabled(false);
            potentialModificationsCmb.setEnabled(false);
            pointMutationsCmb.setEnabled(false);
            snapsCmb.setEnabled(false);
            spectrumSynthesisCmb.setEnabled(false);
        }
        if (xtandemParameters.getMaximumExpectationValueRefinement() != null) {
            maxEValueRefineTxt.setText(xtandemParameters.getMaximumExpectationValueRefinement() + "");
        }
        if (xtandemParameters.isRefineUnanticipatedCleavages()) {
            unanticipatedCleavageCmb.setSelectedIndex(0);
        } else {
            unanticipatedCleavageCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefineSemi()) {
            semiEnzymaticCmb.setSelectedIndex(0);
        } else {
            semiEnzymaticCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isPotentialModificationsForFullRefinment()) {
            potentialModificationsCmb.setSelectedIndex(0);
        } else {
            potentialModificationsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefinePointMutations()) {
            pointMutationsCmb.setSelectedIndex(0);
        } else {
            pointMutationsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefineSnaps()) {
            snapsCmb.setSelectedIndex(0);
        } else {
            snapsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isRefineSpectrumSynthesis()) {
            spectrumSynthesisCmb.setSelectedIndex(0);
        } else {
            spectrumSynthesisCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.getMaxEValue() != null) {
            eValueTxt.setText(xtandemParameters.getMaxEValue() + "");
        }
        if (xtandemParameters.isOutputProteins()) {
            outputProteinsCmb.setSelectedIndex(0);
            outputSequencesCmb.setEnabled(true);
            if (xtandemParameters.isOutputSequences()) {
                outputSequencesCmb.setSelectedIndex(0);
            } else {
                outputSequencesCmb.setSelectedIndex(1);
            }
        } else {
            outputProteinsCmb.setSelectedIndex(1);
            outputSequencesCmb.setEnabled(false);
            outputSequencesCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isOutputSpectra()) {
            outputSpectraCmb.setSelectedIndex(0);
        } else {
            outputSpectraCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.isOutputHistograms()) {
            outputHistogramsCmb.setSelectedIndex(0);
        } else {
            outputHistogramsCmb.setSelectedIndex(1);
        }
        if (xtandemParameters.getSkylinePath() != null) {
            skylineTxt.setText(xtandemParameters.getSkylinePath() + "");
        }

        // load the ptms
        updateModificationList();
    }

    /**
     * Indicates whether the user canceled the process.
     *
     * @return true if cancel was pressed
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns the user selection as X!tandem parameters object.
     *
     * @return the user selection
     */
    public XtandemParameters getInput() {
        XtandemParameters result = new XtandemParameters();
        String input = dynamicRangeTxt.getText().trim();
        if (!input.equals("")) {
            result.setDynamicRange(new Double(input));
        }
        input = nPeaksTxt.getText().trim();
        if (!input.equals("")) {
            result.setnPeaks(new Integer(input));
        }
        input = minFragmentMzTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinFragmentMz(new Double(input));
        }
        input = minPeaksTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPeaksPerSpectrum(new Integer(input));
        }
        result.setUseNoiseSuppression(noiseSuppressionCmb.getSelectedIndex() == 0);
        input = minPrecMassTxt.getText().trim();
        if (!input.equals("")) {
            result.setMinPrecursorMass(new Double(input));
        }
        result.setProteinQuickAcetyl(quickAcetylCmb.getSelectedIndex() == 0);
        result.setQuickPyrolidone(quickPyroCmb.getSelectedIndex() == 0);
        result.setStpBias(stpBiasCmb.getSelectedIndex() == 0);
        result.setRefine(refinementCmb.getSelectedIndex() == 0);
        input = maxEValueRefineTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaximumExpectationValueRefinement(new Double(input));
        }
        result.setRefineUnanticipatedCleavages(unanticipatedCleavageCmb.getSelectedIndex() == 0);
        result.setRefineSemi(semiEnzymaticCmb.getSelectedIndex() == 0);
        result.setPotentialModificationsForFullRefinment(potentialModificationsCmb.getSelectedIndex() == 0);
        result.setRefinePointMutations(pointMutationsCmb.getSelectedIndex() == 0);
        result.setRefineSnaps(snapsCmb.getSelectedIndex() == 0);
        result.setRefineSpectrumSynthesis(spectrumSynthesisCmb.getSelectedIndex() == 0);
        input = eValueTxt.getText().trim();
        if (!input.equals("")) {
            result.setMaxEValue(new Double(input));
        }
        result.setOutputProteins(outputProteinsCmb.getSelectedIndex() == 0);
        result.setOutputSequences(outputSequencesCmb.getSelectedIndex() == 0);
        result.setOutputSpectra(outputSpectraCmb.getSelectedIndex() == 0);
        result.setOutputHistograms(outputHistogramsCmb.getSelectedIndex() == 0);
        input = skylineTxt.getText().trim();
        if (!input.equals("")) {
            result.setSkylinePath(input);
        }
        return result;
    }
    
    /**
     * Returns the modification profile corresponding to the input by the user.
     * 
     * @return the modification profile corresponding to the input by the user
     */
    public ModificationProfile getModificationProfile() {
        return modificationProfile;
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
        spectrumImportSettingsPanel = new javax.swing.JPanel();
        dynamicRangeLbl = new javax.swing.JLabel();
        dynamicRangeTxt = new javax.swing.JTextField();
        nPeaksTxt = new javax.swing.JTextField();
        nPeaksLbl = new javax.swing.JLabel();
        minFragMzLbl = new javax.swing.JLabel();
        minFragmentMzTxt = new javax.swing.JTextField();
        minPeaksLbl = new javax.swing.JLabel();
        minPeaksTxt = new javax.swing.JTextField();
        minPrecMassLbl = new javax.swing.JLabel();
        minPrecMassTxt = new javax.swing.JTextField();
        noiseSuppressionCmb = new javax.swing.JComboBox();
        noiseSuppressionLabel = new javax.swing.JLabel();
        advancedSearchSettingsPanel = new javax.swing.JPanel();
        quickPyroCmb = new javax.swing.JComboBox();
        quickAcetylCmb = new javax.swing.JComboBox();
        quickAcetylLabel = new javax.swing.JLabel();
        quickPyroLabel = new javax.swing.JLabel();
        stpBiasLabel = new javax.swing.JLabel();
        stpBiasCmb = new javax.swing.JComboBox();
        refinementSettingsPanel = new javax.swing.JPanel();
        refinementCmb = new javax.swing.JComboBox();
        refinementLabel = new javax.swing.JLabel();
        semiEnzymaticLabel = new javax.swing.JLabel();
        semiEnzymaticCmb = new javax.swing.JComboBox();
        maxEValueRefineTxt = new javax.swing.JTextField();
        maxEValueRefinmentLbl = new javax.swing.JLabel();
        pointMutationLabel = new javax.swing.JLabel();
        pointMutationsCmb = new javax.swing.JComboBox();
        snapsLabel = new javax.swing.JLabel();
        snapsCmb = new javax.swing.JComboBox();
        spectrumSynthesisCmb = new javax.swing.JComboBox();
        spectrumSynthesisLabel = new javax.swing.JLabel();
        unanticipatedCleavageCmb = new javax.swing.JComboBox();
        unanticipatedCleavageLabel = new javax.swing.JLabel();
        usePotentialModsLabel = new javax.swing.JLabel();
        potentialModificationsCmb = new javax.swing.JComboBox();
        refinementModificationsJPanel = new javax.swing.JPanel();
        refinementModificationsLabel = new javax.swing.JLabel();
        openModificationSettingsJButton = new javax.swing.JButton();
        modificationsJScrollPane = new javax.swing.JScrollPane();
        modificationsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) modificationTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        outputSettingsPanel = new javax.swing.JPanel();
        eValueLbl = new javax.swing.JLabel();
        eValueTxt = new javax.swing.JTextField();
        outputSequencesLabel = new javax.swing.JLabel();
        outputSequencesCmb = new javax.swing.JComboBox();
        outputProteinsCmb = new javax.swing.JComboBox();
        outputProteinsLabel = new javax.swing.JLabel();
        outputSpectraLabel = new javax.swing.JLabel();
        outputSpectraCmb = new javax.swing.JComboBox();
        skylineTxt = new javax.swing.JTextField();
        skylinePathValueLbl = new javax.swing.JLabel();
        outputHistogramsCmb = new javax.swing.JComboBox();
        outputHistogramsLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();
        advancedSettingsWarningLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Advanced X!Tandem Settings");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        spectrumImportSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectrum Import"));
        spectrumImportSettingsPanel.setOpaque(false);

        dynamicRangeLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/sdr.html\">Dynamic Range (Da)</a></html>");
        dynamicRangeLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dynamicRangeLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dynamicRangeLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dynamicRangeLblMouseExited(evt);
            }
        });

        dynamicRangeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dynamicRangeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dynamicRangeTxtKeyReleased(evt);
            }
        });

        nPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nPeaksTxtKeyReleased(evt);
            }
        });

        nPeaksLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/stp.html\">Number of Peaks</a></html>");
        nPeaksLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                nPeaksLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                nPeaksLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                nPeaksLblMouseExited(evt);
            }
        });

        minFragMzLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/smfmz.html\">Minimum Fragment m/z</a></html>");
        minFragMzLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                minFragMzLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                minFragMzLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                minFragMzLblMouseExited(evt);
            }
        });

        minFragmentMzTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minFragmentMzTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minFragmentMzTxtKeyReleased(evt);
            }
        });

        minPeaksLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/smp.html\">Minimum Peaks</a></html>");
        minPeaksLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                minPeaksLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                minPeaksLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                minPeaksLblMouseExited(evt);
            }
        });

        minPeaksTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPeaksTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPeaksTxtKeyReleased(evt);
            }
        });

        minPrecMassLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/smpmh.html\">Minimum Precursor Mass</a></html>");
        minPrecMassLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                minPrecMassLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                minPrecMassLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                minPrecMassLblMouseExited(evt);
            }
        });

        minPrecMassTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecMassTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecMassTxtKeyReleased(evt);
            }
        });

        noiseSuppressionCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        noiseSuppressionCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseSuppressionCmbActionPerformed(evt);
            }
        });

        noiseSuppressionLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/suns.html\">Noise Suppression</a></html>");
        noiseSuppressionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                noiseSuppressionLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                noiseSuppressionLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                noiseSuppressionLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout spectrumImportSettingsPanelLayout = new javax.swing.GroupLayout(spectrumImportSettingsPanel);
        spectrumImportSettingsPanel.setLayout(spectrumImportSettingsPanelLayout);
        spectrumImportSettingsPanelLayout.setHorizontalGroup(
            spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spectrumImportSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(spectrumImportSettingsPanelLayout.createSequentialGroup()
                        .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dynamicRangeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dynamicRangeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(spectrumImportSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minFragMzLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minFragmentMzTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(spectrumImportSettingsPanelLayout.createSequentialGroup()
                        .addComponent(minPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(spectrumImportSettingsPanelLayout.createSequentialGroup()
                        .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minPrecMassLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(noiseSuppressionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minPrecMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(noiseSuppressionCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        spectrumImportSettingsPanelLayout.setVerticalGroup(
            spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spectrumImportSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dynamicRangeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dynamicRangeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minFragmentMzTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minFragMzLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPeaksTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minPeaksLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noiseSuppressionCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(noiseSuppressionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(spectrumImportSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minPrecMassTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minPrecMassLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        advancedSearchSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced Search"));
        advancedSearchSettingsPanel.setOpaque(false);

        quickPyroCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        quickPyroCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickPyroCmbActionPerformed(evt);
            }
        });

        quickAcetylCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        quickAcetylCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickAcetylCmbActionPerformed(evt);
            }
        });

        quickAcetylLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/pqa.html\">Quick Acetyl</a></html>");
        quickAcetylLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                quickAcetylLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                quickAcetylLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                quickAcetylLabelMouseExited(evt);
            }
        });

        quickPyroLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/pqp.html\">Quick Pyrolidone</a></html>");
        quickPyroLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                quickPyroLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                quickPyroLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                quickPyroLabelMouseExited(evt);
            }
        });

        stpBiasLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/pstpb.html\">stP bias</a></html>");
        stpBiasLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                stpBiasLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                stpBiasLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                stpBiasLabelMouseExited(evt);
            }
        });

        stpBiasCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        javax.swing.GroupLayout advancedSearchSettingsPanelLayout = new javax.swing.GroupLayout(advancedSearchSettingsPanel);
        advancedSearchSettingsPanel.setLayout(advancedSearchSettingsPanelLayout);
        advancedSearchSettingsPanelLayout.setHorizontalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(quickPyroLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickAcetylLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stpBiasLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stpBiasCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickPyroCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickAcetylCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        advancedSearchSettingsPanelLayout.setVerticalGroup(
            advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedSearchSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quickAcetylLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickAcetylCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quickPyroCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickPyroLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedSearchSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stpBiasCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stpBiasLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        refinementSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Refinement"));
        refinementSettingsPanel.setOpaque(false);

        refinementCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        refinementCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refinementCmbActionPerformed(evt);
            }
        });

        refinementLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/refine.html\">Refinement</a></html>");
        refinementLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                refinementLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refinementLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refinementLabelMouseExited(evt);
            }
        });

        semiEnzymaticLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rcsemi.html\">Semi-Enzymatic Cleavage</a></html>");
        semiEnzymaticLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                semiEnzymaticLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                semiEnzymaticLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                semiEnzymaticLabelMouseExited(evt);
            }
        });

        semiEnzymaticCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        maxEValueRefineTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxEValueRefineTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxEValueRefineTxtKeyReleased(evt);
            }
        });

        maxEValueRefinmentLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/refmvev.html\">Maximum Valid Expectation Value</a></html>");
        maxEValueRefinmentLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maxEValueRefinmentLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                maxEValueRefinmentLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                maxEValueRefinmentLblMouseExited(evt);
            }
        });

        pointMutationLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rpm.html\">Point Mutations</a></html>");
        pointMutationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pointMutationLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pointMutationLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pointMutationLabelMouseExited(evt);
            }
        });

        pointMutationsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        snapsLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rsaps.html\">snAPs</a></html>");
        snapsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                snapsLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                snapsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                snapsLabelMouseExited(evt);
            }
        });

        snapsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        spectrumSynthesisCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        spectrumSynthesisLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rss.html\">Spectrum Synthesis</a></html>");
        spectrumSynthesisLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                spectrumSynthesisLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                spectrumSynthesisLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                spectrumSynthesisLabelMouseExited(evt);
            }
        });

        unanticipatedCleavageCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        unanticipatedCleavageLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ruc.html\">Unanticipated Cleavage</a></html>");
        unanticipatedCleavageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                unanticipatedCleavageLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                unanticipatedCleavageLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                unanticipatedCleavageLabelMouseExited(evt);
            }
        });

        usePotentialModsLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/rupmffr.html\">Use Potential Modifications for Full Refinement</a></html>");
        usePotentialModsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                usePotentialModsLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                usePotentialModsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                usePotentialModsLabelMouseExited(evt);
            }
        });

        potentialModificationsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        refinementModificationsJPanel.setOpaque(false);

        refinementModificationsLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/refpmm.htmll\">Refinement Modifications</a></html>");
        refinementModificationsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refinementModificationsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refinementModificationsLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                refinementModificationsLabelMouseReleased(evt);
            }
        });

        openModificationSettingsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        openModificationSettingsJButton.setToolTipText("Edit Modifications");
        openModificationSettingsJButton.setBorder(null);
        openModificationSettingsJButton.setBorderPainted(false);
        openModificationSettingsJButton.setContentAreaFilled(false);
        openModificationSettingsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        openModificationSettingsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openModificationSettingsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                openModificationSettingsJButtonMouseExited(evt);
            }
        });
        openModificationSettingsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openModificationSettingsJButtonActionPerformed(evt);
            }
        });

        modificationsJScrollPane.setPreferredSize(new java.awt.Dimension(100, 60));

        modificationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "Mass", "V", "F"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class, java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        modificationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                modificationsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                modificationsTableMouseReleased(evt);
            }
        });
        modificationsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                modificationsTableMouseMoved(evt);
            }
        });
        modificationsJScrollPane.setViewportView(modificationsTable);

        javax.swing.GroupLayout refinementModificationsJPanelLayout = new javax.swing.GroupLayout(refinementModificationsJPanel);
        refinementModificationsJPanel.setLayout(refinementModificationsJPanelLayout);
        refinementModificationsJPanelLayout.setHorizontalGroup(
            refinementModificationsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(refinementModificationsJPanelLayout.createSequentialGroup()
                .addComponent(refinementModificationsLabel)
                .addGap(246, 246, 246)
                .addComponent(openModificationSettingsJButton)
                .addGap(2, 2, 2))
            .addComponent(modificationsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        refinementModificationsJPanelLayout.setVerticalGroup(
            refinementModificationsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(refinementModificationsJPanelLayout.createSequentialGroup()
                .addGroup(refinementModificationsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(refinementModificationsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openModificationSettingsJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout refinementSettingsPanelLayout = new javax.swing.GroupLayout(refinementSettingsPanel);
        refinementSettingsPanel.setLayout(refinementSettingsPanelLayout);
        refinementSettingsPanelLayout.setHorizontalGroup(
            refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(refinementSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(refinementModificationsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(usePotentialModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(potentialModificationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(refinementLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refinementCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(semiEnzymaticLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(semiEnzymaticCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(maxEValueRefinmentLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(maxEValueRefineTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(pointMutationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pointMutationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(snapsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(snapsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(spectrumSynthesisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(spectrumSynthesisCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(refinementSettingsPanelLayout.createSequentialGroup()
                        .addComponent(unanticipatedCleavageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(unanticipatedCleavageCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        refinementSettingsPanelLayout.setVerticalGroup(
            refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(refinementSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refinementCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refinementLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxEValueRefineTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxEValueRefinmentLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unanticipatedCleavageCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unanticipatedCleavageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(semiEnzymaticCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(semiEnzymaticLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(potentialModificationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usePotentialModsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pointMutationsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pointMutationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(snapsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(snapsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(refinementSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spectrumSynthesisCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spectrumSynthesisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(refinementModificationsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        outputSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));
        outputSettingsPanel.setOpaque(false);

        eValueLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/omvev.html\">E-value Cutoff</a></html>");
        eValueLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                eValueLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                eValueLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                eValueLblMouseExited(evt);
            }
        });

        eValueTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        eValueTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                eValueTxtKeyReleased(evt);
            }
        });

        outputSequencesLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/osequ.html\">Output Sequences</a></html>");
        outputSequencesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                outputSequencesLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                outputSequencesLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                outputSequencesLabelMouseExited(evt);
            }
        });

        outputSequencesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        outputProteinsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        outputProteinsCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputProteinsCmbActionPerformed(evt);
            }
        });

        outputProteinsLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/oprot.html\">Output Proteins</a></html>");
        outputProteinsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                outputProteinsLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                outputProteinsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                outputProteinsLabelMouseExited(evt);
            }
        });

        outputSpectraLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ospec.html\">Output Spectra</a></html>");
        outputSpectraLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                outputSpectraLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                outputSpectraLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                outputSpectraLabelMouseExited(evt);
            }
        });

        outputSpectraCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        skylineTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        skylinePathValueLbl.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ssp.html\">Skyline Path</a></html>");
        skylinePathValueLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                skylinePathValueLblMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                skylinePathValueLblMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                skylinePathValueLblMouseExited(evt);
            }
        });

        outputHistogramsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        outputHistogramsLabel.setText("<html><a href=\"http://www.thegpm.org/TANDEM/api/ohist.html\">Output Histograms</a></html>");
        outputHistogramsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                outputHistogramsLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                outputHistogramsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                outputHistogramsLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout outputSettingsPanelLayout = new javax.swing.GroupLayout(outputSettingsPanel);
        outputSettingsPanel.setLayout(outputSettingsPanelLayout);
        outputSettingsPanelLayout.setHorizontalGroup(
            outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(outputSettingsPanelLayout.createSequentialGroup()
                        .addComponent(eValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eValueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(outputSettingsPanelLayout.createSequentialGroup()
                        .addComponent(skylinePathValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(skylineTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(outputSettingsPanelLayout.createSequentialGroup()
                        .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(outputSequencesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outputProteinsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outputSpectraLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outputHistogramsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(outputSettingsPanelLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(outputSequencesCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(outputProteinsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, outputSettingsPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(outputHistogramsCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(outputSpectraCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        outputSettingsPanelLayout.setVerticalGroup(
            outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eValueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputProteinsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputProteinsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputSequencesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputSequencesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputSpectraCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputSpectraLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputHistogramsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputHistogramsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(skylinePathValueLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(skylineTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        openDialogHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        openDialogHelpJButton.setToolTipText("Help");
        openDialogHelpJButton.setBorder(null);
        openDialogHelpJButton.setBorderPainted(false);
        openDialogHelpJButton.setContentAreaFilled(false);
        openDialogHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openDialogHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                openDialogHelpJButtonMouseExited(evt);
            }
        });
        openDialogHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDialogHelpJButtonActionPerformed(evt);
            }
        });

        advancedSettingsWarningLabel.setText("Note: The advanced settings are for expert use only. See the help for details.");

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(advancedSearchSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spectrumImportSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outputSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 6, Short.MAX_VALUE)
                        .addComponent(refinementSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(openDialogHelpJButton)
                        .addGap(18, 18, 18)
                        .addComponent(advancedSettingsWarningLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {advancedSearchSettingsPanel, outputSettingsPanel, refinementSettingsPanel, spectrumImportSettingsPanel});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addComponent(spectrumImportSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advancedSearchSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outputSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(refinementSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton)
                    .addComponent(okButton)
                    .addComponent(closeButton)
                    .addComponent(advancedSettingsWarningLabel))
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
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the dialog without saving the settings.
     *
     * @param evt
     */
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        cancelled = true;
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    /**
     * Save the settings and then close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput(true)) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Enable/disable the output sequence combo box.
     *
     * @param evt
     */
    private void outputProteinsCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputProteinsCmbActionPerformed
        if (outputProteinsCmb.getSelectedIndex() == 0) {
            outputSequencesCmb.setEnabled(true);
        } else {
            outputSequencesCmb.setSelectedIndex(1);
            outputSequencesCmb.setEnabled(false);
        }
    }//GEN-LAST:event_outputProteinsCmbActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void eValueTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eValueTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_eValueTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxEValueRefineTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxEValueRefineTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxEValueRefineTxtKeyReleased

    /**
     * Enable/disable the refinement setting.
     *
     * @param evt
     */
    private void refinementCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refinementCmbActionPerformed
        if (refinementCmb.getSelectedIndex() == 0) {
            maxEValueRefineTxt.setEnabled(true);
            unanticipatedCleavageCmb.setEnabled(true);
            semiEnzymaticCmb.setEnabled(true);
            potentialModificationsCmb.setEnabled(true);
            pointMutationsCmb.setEnabled(true);
            snapsCmb.setEnabled(true);
            spectrumSynthesisCmb.setEnabled(true);
        } else {
            maxEValueRefineTxt.setEnabled(false);
            unanticipatedCleavageCmb.setEnabled(false);
            semiEnzymaticCmb.setEnabled(false);
            potentialModificationsCmb.setEnabled(false);
            pointMutationsCmb.setEnabled(false);
            snapsCmb.setEnabled(false);
            spectrumSynthesisCmb.setEnabled(false);
        }
    }//GEN-LAST:event_refinementCmbActionPerformed

    /**
     * Check for quick acetyl conflict.
     *
     * @param evt
     */
    private void quickAcetylCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickAcetylCmbActionPerformed
        if (quickAcetylCmb.getSelectedIndex() == 0) {
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (String modName : modificationProfile.getFixedModifications()) {
                PTM ptm = ptmFactory.getPTM(modName);
                if ((ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA)
                        && Math.abs(ptm.getMass() - 42.010565) < fragmentIonMassAccuracy) {
                    JOptionPane.showMessageDialog(this, "The quick acetyl option might conflict with " + modName + ".",
                            "Modification Conflict", JOptionPane.ERROR_MESSAGE);
                    quickAcetylCmb.setSelectedIndex(1);
                    break;
                }
            }
        }
    }//GEN-LAST:event_quickAcetylCmbActionPerformed

    /**
     * Check for quick pyrolidone conflict.
     *
     * @param evt
     */
    private void quickPyroCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickPyroCmbActionPerformed
        if (quickPyroCmb.getSelectedIndex() == 0) {
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (String modName : modificationProfile.getFixedModifications()) {
                PTM ptm = ptmFactory.getPTM(modName);
                if ((ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA)
                        && Math.abs(ptm.getMass() + 17.026549) < fragmentIonMassAccuracy) {
                    JOptionPane.showMessageDialog(this, "The quick pyrolidone option might conflict with " + modName + ".",
                            "Modification Conflict", JOptionPane.ERROR_MESSAGE);
                    quickAcetylCmb.setSelectedIndex(1);
                    break;
                }
            }
        }
    }//GEN-LAST:event_quickPyroCmbActionPerformed

    /**
     * Enable/disable the noise min precursor mass.
     *
     * @param evt
     */
    private void noiseSuppressionCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noiseSuppressionCmbActionPerformed
        if (noiseSuppressionCmb.getSelectedIndex() == 0) {
            minPrecMassTxt.setEnabled(true);
        } else {
            minPrecMassTxt.setEnabled(false);
        }
    }//GEN-LAST:event_noiseSuppressionCmbActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minPrecMassTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecMassTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPrecMassTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minFragmentMzTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minFragmentMzTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minFragmentMzTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nPeaksTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nPeaksTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_nPeaksTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void dynamicRangeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dynamicRangeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_dynamicRangeTxtKeyReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void dynamicRangeLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dynamicRangeLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_dynamicRangeLblMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void dynamicRangeLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dynamicRangeLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_dynamicRangeLblMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void dynamicRangeLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dynamicRangeLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/sdr.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_dynamicRangeLblMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void minFragMzLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minFragMzLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_minFragMzLblMouseEntered

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void minFragMzLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minFragMzLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_minFragMzLblMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void minFragMzLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minFragMzLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/smfmz.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_minFragMzLblMouseReleased

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void nPeaksLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nPeaksLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_nPeaksLblMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void nPeaksLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nPeaksLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/stp.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_nPeaksLblMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void nPeaksLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nPeaksLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_nPeaksLblMouseEntered

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void minPeaksLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minPeaksLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_minPeaksLblMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void minPeaksLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minPeaksLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_minPeaksLblMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void minPeaksLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minPeaksLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/smp.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_minPeaksLblMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void noiseSuppressionLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noiseSuppressionLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_noiseSuppressionLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void noiseSuppressionLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noiseSuppressionLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_noiseSuppressionLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void noiseSuppressionLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noiseSuppressionLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/suns.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_noiseSuppressionLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void minPrecMassLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minPrecMassLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_minPrecMassLblMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void minPrecMassLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minPrecMassLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_minPrecMassLblMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void minPrecMassLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minPrecMassLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/smpmh.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_minPrecMassLblMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void quickAcetylLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quickAcetylLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_quickAcetylLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void quickAcetylLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quickAcetylLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_quickAcetylLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void quickAcetylLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quickAcetylLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/pqa.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_quickAcetylLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void quickPyroLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quickPyroLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_quickPyroLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void quickPyroLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quickPyroLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_quickPyroLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void quickPyroLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quickPyroLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/pqp.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_quickPyroLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void stpBiasLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stpBiasLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_stpBiasLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void stpBiasLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stpBiasLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_stpBiasLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void stpBiasLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stpBiasLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/pstpb.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_stpBiasLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void eValueLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_eValueLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_eValueLblMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void eValueLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_eValueLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_eValueLblMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void eValueLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_eValueLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/omvev.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_eValueLblMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void outputProteinsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputProteinsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_outputProteinsLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void outputProteinsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputProteinsLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputProteinsLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void outputProteinsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputProteinsLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/oprot.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputProteinsLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void outputSequencesLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputSequencesLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_outputSequencesLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void outputSequencesLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputSequencesLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputSequencesLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void outputSequencesLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputSequencesLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/osequ.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputSequencesLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void outputSpectraLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputSpectraLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_outputSpectraLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void outputSpectraLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputSpectraLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputSpectraLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void outputSpectraLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputSpectraLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/ospec.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputSpectraLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void outputHistogramsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputHistogramsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_outputHistogramsLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void outputHistogramsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputHistogramsLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputHistogramsLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void outputHistogramsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputHistogramsLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/ohist.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_outputHistogramsLabelMouseReleased

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void skylinePathValueLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_skylinePathValueLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_skylinePathValueLblMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void skylinePathValueLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_skylinePathValueLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_skylinePathValueLblMouseEntered

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void skylinePathValueLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_skylinePathValueLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/ssp.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_skylinePathValueLblMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void refinementLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refinementLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_refinementLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void refinementLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refinementLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_refinementLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void refinementLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refinementLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/refine.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_refinementLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void maxEValueRefinmentLblMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxEValueRefinmentLblMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_maxEValueRefinmentLblMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void maxEValueRefinmentLblMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxEValueRefinmentLblMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_maxEValueRefinmentLblMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void maxEValueRefinmentLblMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxEValueRefinmentLblMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/refmvev.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_maxEValueRefinmentLblMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void unanticipatedCleavageLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unanticipatedCleavageLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_unanticipatedCleavageLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void unanticipatedCleavageLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unanticipatedCleavageLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_unanticipatedCleavageLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void unanticipatedCleavageLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unanticipatedCleavageLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/ruc.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_unanticipatedCleavageLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void semiEnzymaticLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_semiEnzymaticLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_semiEnzymaticLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void semiEnzymaticLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_semiEnzymaticLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_semiEnzymaticLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void semiEnzymaticLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_semiEnzymaticLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/rcsemi.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_semiEnzymaticLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void usePotentialModsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usePotentialModsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_usePotentialModsLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void usePotentialModsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usePotentialModsLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_usePotentialModsLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void usePotentialModsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usePotentialModsLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/rupmffr.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_usePotentialModsLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void pointMutationLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pointMutationLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_pointMutationLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void pointMutationLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pointMutationLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_pointMutationLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void pointMutationLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pointMutationLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/rpm.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_pointMutationLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void snapsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_snapsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_snapsLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void snapsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_snapsLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_snapsLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void snapsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_snapsLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/rsaps.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_snapsLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void spectrumSynthesisLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectrumSynthesisLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_spectrumSynthesisLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void spectrumSynthesisLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectrumSynthesisLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_spectrumSynthesisLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void spectrumSynthesisLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectrumSynthesisLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/rss.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_spectrumSynthesisLabelMouseReleased

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void modificationsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsTableMouseExited

    /**
     * Opens a color chooser where the color for the PTM can be changed, or
     * allows the users to change of a PTM is in the most used PTMs list.
     *
     * @param evt
     */
    private void modificationsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseReleased
        int row = modificationsTable.rowAtPoint(evt.getPoint());
        int column = modificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == modificationsTable.getColumn(" ").getModelIndex()) {
                Color newColor = JColorChooser.showDialog(this, "Pick a Color", (Color) modificationsTable.getValueAt(row, column));

                if (newColor != null) {
                    ptmFactory.setColor((String) modificationsTable.getValueAt(row, 1), newColor);
                    modificationsTable.setValueAt(newColor, row, 0);
                    ((DefaultTableModel) modificationsTable.getModel()).fireTableDataChanged();
                    modificationsTable.repaint();
                }
            } else if (column == modificationsTable.getColumn("V").getModelIndex()
                    && modificationsTable.getValueAt(row, column) != null) {

                boolean selected = (Boolean) modificationsTable.getValueAt(row, column);
                String ptmName = (String) modificationsTable.getValueAt(row, 1);

                // add/remove the ptm as a refinement ptm
                if (selected) {
                    // add as refinement ptm
                    if (!modificationProfile.getRefinementVariableModifications().contains(ptmName)) {
                        modificationProfile.addRefinementVariableModification(ptmFactory.getPTM(ptmName));
                    }
                } else {
                    // remove the ptm as refinement ptm
                    modificationProfile.removeRefinementVariableModification(ptmName);
                }

                updateModificationList();

                if (row < modificationsTable.getRowCount()) {
                    modificationsTable.setRowSelectionInterval(row, row);
                } else if (row - 1 < modificationsTable.getRowCount() && row >= 0) {
                    modificationsTable.setRowSelectionInterval(row - 1, row - 1);
                }
            } else if (column == modificationsTable.getColumn("F").getModelIndex()
                    && modificationsTable.getValueAt(row, column) != null) {

                boolean selected = (Boolean) modificationsTable.getValueAt(row, column);
                String ptmName = (String) modificationsTable.getValueAt(row, 1);

                // add/remove the ptm as a refinement ptm
                if (selected) {
                    // add as refinement ptm
                    if (!modificationProfile.getRefinementFixedModifications().contains(ptmName)) {
                        modificationProfile.addRefinementFixedModification(ptmFactory.getPTM(ptmName));
                    }
                } else {
                    // remove the ptm as refinement ptm
                    modificationProfile.removeRefinementFixedModification(ptmName);
                }

                updateModificationList();

                if (row < modificationsTable.getRowCount()) {
                    modificationsTable.setRowSelectionInterval(row, row);
                } else if (row - 1 < modificationsTable.getRowCount() && row >= 0) {
                    modificationsTable.setRowSelectionInterval(row - 1, row - 1);
                }
            }
        }
    }//GEN-LAST:event_modificationsTableMouseReleased

    private void modificationsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseMoved
        int row = modificationsTable.rowAtPoint(evt.getPoint());
        int column = modificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == modificationsTable.getColumn(" ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_modificationsTableMouseMoved

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void openModificationSettingsJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openModificationSettingsJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_openModificationSettingsJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void openModificationSettingsJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openModificationSettingsJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openModificationSettingsJButtonMouseExited

    private void openModificationSettingsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openModificationSettingsJButtonActionPerformed
        new ModificationsDialog((Frame) this.getParent(), this, true);
    }//GEN-LAST:event_openModificationSettingsJButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void refinementModificationsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refinementModificationsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_refinementModificationsLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void refinementModificationsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refinementModificationsLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_refinementModificationsLabelMouseExited

    /**
     * Open the link to the X!Tandem help pages.
     *
     * @param evt
     */
    private void refinementModificationsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refinementModificationsLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/TANDEM/api/refpmm.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_refinementModificationsLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/XTandemSettingsDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                "SearchGUI - Help", 500, 50);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;

        eValueLbl.setForeground(Color.BLACK);
        eValueLbl.setToolTipText(null);
        // Validate e-value cutoff
        if (eValueTxt.getText() == null || eValueTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify an e-value cutoff.",
                        "E-value Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select an e-value cuttoff limit");
        }

        // OK, see if it is a number.
        float eValue = -1;

        try {
            eValue = Float.parseFloat(eValueTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff.",
                        "E-value Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (eValue < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the e-value cutoff.",
                        "E-value Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            eValueLbl.setForeground(Color.RED);
            eValueLbl.setToolTipText("Please select a positive number");
        }

        dynamicRangeLbl.setForeground(Color.BLACK);
        dynamicRangeLbl.setToolTipText(null);
        if (dynamicRangeTxt.getText() == null || dynamicRangeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a dynamic range cutoff.",
                        "Dynamic Range Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            dynamicRangeLbl.setForeground(Color.RED);
            dynamicRangeLbl.setToolTipText("Please select a dynamic range cuttoff limit");
        }

        // OK, see if it is a number.
        double test = -1;

        try {
            test = new Double(dynamicRangeTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the dynamic range cutoff.",
                        "Dynamic Range Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            dynamicRangeLbl.setForeground(Color.RED);
            dynamicRangeLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the dynamic range cutoff.",
                        "Dynamic Range Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            dynamicRangeLbl.setForeground(Color.RED);
            dynamicRangeLbl.setToolTipText("Please select a positive number");
        }

        nPeaksLbl.setForeground(Color.BLACK);
        nPeaksLbl.setToolTipText(null);
        if (nPeaksTxt.getText() == null || nPeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a number of peaks.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a number of peaks limit");
        }

        // OK, see if it is a number.
        Integer nPeaks = -1;

        try {
            nPeaks = new Integer(nPeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (nPeaks < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the number of peaks.",
                        "Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            nPeaksLbl.setForeground(Color.RED);
            nPeaksLbl.setToolTipText("Please select a positive number");
        }

        minFragMzLbl.setForeground(Color.BLACK);
        minFragMzLbl.setToolTipText(null);
        if (minFragmentMzTxt.getText() == null || minFragmentMzTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a minimal fragment m/z.",
                        "Minimal Fragment m/z Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minFragMzLbl.setForeground(Color.RED);
            minFragMzLbl.setToolTipText("Please select a minimal fragment m/z limit");
        }

        // OK, see if it is a number.
        test = -1;

        try {
            test = new Double(minFragmentMzTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal fragment m/z.",
                        "Minimal Fragment m/z Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minFragMzLbl.setForeground(Color.RED);
            minFragMzLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal fragment m/z.",
                        "Minimal Fragment m/z Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minFragMzLbl.setForeground(Color.RED);
            minFragMzLbl.setToolTipText("Please select a positive number");
        }

        minPeaksLbl.setForeground(Color.BLACK);
        minPeaksLbl.setToolTipText(null);
        if (minPeaksTxt.getText() == null || minPeaksTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a minimal number of peaks.",
                        "Minimal Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPeaksLbl.setForeground(Color.RED);
            minPeaksLbl.setToolTipText("Please select a minimal number of peaks");
        }

        // OK, see if it is a number.
        nPeaks = -1;

        try {
            nPeaks = new Integer(minPeaksTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of peaks.",
                        "Minimal Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPeaksLbl.setForeground(Color.RED);
            minPeaksLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (nPeaks < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal number of peaks.",
                        "Minimal Number of Peaks Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPeaksLbl.setForeground(Color.RED);
            minPeaksLbl.setToolTipText("Please select a positive number");
        }

        minPrecMassLbl.setForeground(Color.BLACK);
        minPrecMassLbl.setToolTipText(null);
        if (minPrecMassTxt.getText() == null || minPrecMassTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a minimal precursor mass.",
                        "Minimal Precursor Mass Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecMassLbl.setForeground(Color.RED);
            minPrecMassLbl.setToolTipText("Please select a minimal precursor mass");
        }

        // OK, see if it is a number.
        test = -1;

        try {
            test = new Double(minPrecMassTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal precursor mass.",
                        "Minimal Precursor Mass Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecMassLbl.setForeground(Color.RED);
            minPrecMassLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the minimal precursor mass.",
                        "Minimal Precursor Mass Cutoff Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            minPrecMassLbl.setForeground(Color.RED);
            minPrecMassLbl.setToolTipText("Please select a positive number");
        }

        maxEValueRefinmentLbl.setForeground(Color.BLACK);
        maxEValueRefinmentLbl.setToolTipText(null);
        if (maxEValueRefineTxt.getText() == null || maxEValueRefineTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a maximal e-value for the refinement.",
                        "Maximal Refinement E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxEValueRefinmentLbl.setForeground(Color.RED);
            maxEValueRefinmentLbl.setToolTipText("Please select a maximal e-value for the refinement");
        }

        // OK, see if it is a number.
        test = -1;

        try {
            test = new Double(maxEValueRefineTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal e-value for the refinement.",
                        "Maximal Refinement E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxEValueRefinmentLbl.setForeground(Color.RED);
            maxEValueRefinmentLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (test < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number for the maximal e-value for the refinement.",
                        "Maximal Refinement E-Value Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxEValueRefinmentLbl.setForeground(Color.RED);
            maxEValueRefinmentLbl.setToolTipText("Please select a positive number");
        }

        return valid;
    }

    /**
     * Updates the modification list.
     */
    private void updateModificationList() {

        ArrayList<String> allModificationsList = ptmFactory.getPTMs();
        String[] allModificationsAsArray = new String[allModificationsList.size()];

        for (int i = 0; i < allModificationsList.size(); i++) {
            allModificationsAsArray[i] = allModificationsList.get(i);
        }

        Arrays.sort(allModificationsAsArray);

        modificationsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    " ", "Name", "Mass", "V", "F"
                }
        ) {
            Class[] types = new Class[]{
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class, java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        for (String mod : allModificationsAsArray) {
            ((DefaultTableModel) modificationsTable.getModel()).addRow(
                    new Object[]{ptmFactory.getColor(mod),
                        mod,
                        ptmFactory.getPTM(mod).getMass(),
                        modificationProfile.getRefinementVariableModifications().contains(mod),
                        modificationProfile.getRefinementFixedModifications().contains(mod)});
        }
        ((DefaultTableModel) modificationsTable.getModel()).fireTableDataChanged();
        modificationsTable.repaint();

        // get the min and max values for the mass sparklines
        double maxMass = Double.MIN_VALUE;
        double minMass = Double.MAX_VALUE;

        for (String ptm : ptmFactory.getPTMs()) {
            if (ptmFactory.getPTM(ptm).getMass() > maxMass) {
                maxMass = ptmFactory.getPTM(ptm).getMass();
            }
            if (ptmFactory.getPTM(ptm).getMass() < minMass) {
                minMass = ptmFactory.getPTM(ptm).getMass();
            }
        }

        setAllModificationTableProperties();

        modificationsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) modificationsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);

        if (modificationsTable.getRowCount() > 0) {
            modificationsTable.setRowSelectionInterval(0, 0);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedSearchSettingsPanel;
    private javax.swing.JLabel advancedSettingsWarningLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel dynamicRangeLbl;
    private javax.swing.JTextField dynamicRangeTxt;
    private javax.swing.JLabel eValueLbl;
    private javax.swing.JTextField eValueTxt;
    private javax.swing.JTextField maxEValueRefineTxt;
    private javax.swing.JLabel maxEValueRefinmentLbl;
    private javax.swing.JLabel minFragMzLbl;
    private javax.swing.JTextField minFragmentMzTxt;
    private javax.swing.JLabel minPeaksLbl;
    private javax.swing.JTextField minPeaksTxt;
    private javax.swing.JLabel minPrecMassLbl;
    private javax.swing.JTextField minPrecMassTxt;
    private javax.swing.JScrollPane modificationsJScrollPane;
    private javax.swing.JTable modificationsTable;
    private javax.swing.JLabel nPeaksLbl;
    private javax.swing.JTextField nPeaksTxt;
    private javax.swing.JComboBox noiseSuppressionCmb;
    private javax.swing.JLabel noiseSuppressionLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JButton openModificationSettingsJButton;
    private javax.swing.JComboBox outputHistogramsCmb;
    private javax.swing.JLabel outputHistogramsLabel;
    private javax.swing.JComboBox outputProteinsCmb;
    private javax.swing.JLabel outputProteinsLabel;
    private javax.swing.JComboBox outputSequencesCmb;
    private javax.swing.JLabel outputSequencesLabel;
    private javax.swing.JPanel outputSettingsPanel;
    private javax.swing.JComboBox outputSpectraCmb;
    private javax.swing.JLabel outputSpectraLabel;
    private javax.swing.JLabel pointMutationLabel;
    private javax.swing.JComboBox pointMutationsCmb;
    private javax.swing.JComboBox potentialModificationsCmb;
    private javax.swing.JComboBox quickAcetylCmb;
    private javax.swing.JLabel quickAcetylLabel;
    private javax.swing.JComboBox quickPyroCmb;
    private javax.swing.JLabel quickPyroLabel;
    private javax.swing.JComboBox refinementCmb;
    private javax.swing.JLabel refinementLabel;
    private javax.swing.JPanel refinementModificationsJPanel;
    private javax.swing.JLabel refinementModificationsLabel;
    private javax.swing.JPanel refinementSettingsPanel;
    private javax.swing.JComboBox semiEnzymaticCmb;
    private javax.swing.JLabel semiEnzymaticLabel;
    private javax.swing.JLabel skylinePathValueLbl;
    private javax.swing.JTextField skylineTxt;
    private javax.swing.JComboBox snapsCmb;
    private javax.swing.JLabel snapsLabel;
    private javax.swing.JPanel spectrumImportSettingsPanel;
    private javax.swing.JComboBox spectrumSynthesisCmb;
    private javax.swing.JLabel spectrumSynthesisLabel;
    private javax.swing.JComboBox stpBiasCmb;
    private javax.swing.JLabel stpBiasLabel;
    private javax.swing.JComboBox unanticipatedCleavageCmb;
    private javax.swing.JLabel unanticipatedCleavageLabel;
    private javax.swing.JLabel usePotentialModsLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateModifications() {
        updateModificationList();
    }
}
