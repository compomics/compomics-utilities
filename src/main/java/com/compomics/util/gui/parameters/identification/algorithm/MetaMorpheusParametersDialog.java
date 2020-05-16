package com.compomics.util.gui.parameters.identification.algorithm;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.gui.parameters.identification.IdentificationAlgorithmParameter;
import com.compomics.util.gui.GuiUtilities;
import java.awt.Dialog;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import com.compomics.util.gui.parameters.identification.AlgorithmParametersDialog;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters.MetaMorpheusDecoyType;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters.MetaMorpheusDissociationType;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters.MetaMorpheusFragmentationTerminusType;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters.MetaMorpheusInitiatorMethionineBehaviorType;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters.MetaMorpheusMassDiffAcceptorType;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters.MetaMorpheusSearchType;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters.MetaMorpheusToleranceType;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;

/**
 * Dialog for the MetaMorpheus specific parameters.
 *
 * @author Harald Barsnes
 */
public class MetaMorpheusParametersDialog extends javax.swing.JDialog implements AlgorithmParametersDialog {

    /**
     * Boolean indicating whether the used canceled the editing.
     */
    private boolean cancelled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates new form MetaMorpheusDialog with a frame as owner.
     *
     * @param parent the parent frame
     * @param metaMorpheusParameters the MetaMorpheus parameters
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public MetaMorpheusParametersDialog(java.awt.Frame parent, MetaMorpheusParameters metaMorpheusParameters, boolean editable) {
        super(parent, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(metaMorpheusParameters);
        validateInput(false);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates new form MetaMorpheusDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parent the parent frame
     * @param metaMorpheusParameters the MetaMorpheus parameters
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public MetaMorpheusParametersDialog(Dialog owner, java.awt.Frame parent, MetaMorpheusParameters metaMorpheusParameters, boolean editable) {
        super(owner, true);
        this.editable = editable;
        initComponents();
        setUpGui();
        populateGUI(metaMorpheusParameters);
        validateInput(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGui() {

        searchTypeCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        dissociationTypeCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        intitiatorMethBehaviorCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        useDeltaScoreCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        massDiffAcceptorTypeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        writeMzidCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        writePepXmlCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        useProvidedPrecCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        doPrecDeconvCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        deconvMassToleranceTypeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        trimMs1Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        trimMsMsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        normalizePeaksAcrossAllWindowsCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        modifiedPeptidesAreDifferentCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        excludeOneHitWondersCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentationTerminusCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        searchTargetComboBox.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        decoyTypeCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        runGtpmCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        searchTypeCombo.setEnabled(editable);
        numberOfPartitionsTxt.setEditable(editable);
        numberOfPartitionsTxt.setEnabled(editable);
        dissociationTypeCombo.setEnabled(editable);
        maxNumModPeptideTxt.setEditable(editable);
        maxNumModPeptideTxt.setEnabled(editable);
        intitiatorMethBehaviorCombo.setEnabled(editable);
        scoreCutoffTxt.setEditable(editable);
        scoreCutoffTxt.setEnabled(editable);
        useDeltaScoreCmb.setEnabled(editable);
        massDiffAcceptorTypeCmb.setEnabled(editable);
        minPepLengthTxt.setEditable(editable);
        minPepLengthTxt.setEnabled(editable);
        maxPepLengthTxt.setEditable(editable);
        maxPepLengthTxt.setEnabled(editable);
        writeMzidCmb.setEnabled(editable);
        writePepXmlCmb.setEnabled(editable);
        useProvidedPrecCmb.setEnabled(editable);
        doPrecDeconvCmb.setEnabled(editable);
        deconvIntRatioTxt.setEditable(editable);
        deconvIntRatioTxt.setEnabled(editable);
        deconvMassToleranceTxt.setEditable(editable);
        deconvMassToleranceTxt.setEnabled(editable);
        deconvMassToleranceTypeCmb.setEnabled(editable);
        trimMs1Cmb.setEnabled(editable);
        trimMsMsCmb.setEnabled(editable);
        numPeaksPerWindowTxt.setEditable(editable);
        numPeaksPerWindowTxt.setEnabled(editable);
        minAllowedIntensityRatioToBasePeakTxt.setEditable(editable);
        minAllowedIntensityRatioToBasePeakTxt.setEnabled(editable);
        windowWidthThompsonTxt.setEditable(editable);
        windowWidthThompsonTxt.setEnabled(editable);
        numberOfWindowsTxt.setEditable(editable);
        numberOfWindowsTxt.setEnabled(editable);
        normalizePeaksAcrossAllWindowsCmb.setEnabled(editable);
        modifiedPeptidesAreDifferentCmb.setEnabled(editable);
        excludeOneHitWondersCmb.setEnabled(editable);
        fragmentationTerminusCmb.setEnabled(editable);
        maxFragmentSizeTxt.setEditable(editable);
        maxFragmentSizeTxt.setEnabled(editable);
        searchTargetComboBox.setEnabled(editable);
        decoyTypeCmb.setEnabled(editable);
        maxModIsoformsTxt.setEditable(editable);
        maxModIsoformsTxt.setEnabled(editable);
        minVariantDepthTxt.setEditable(editable);
        minVariantDepthTxt.setEnabled(editable);
        maxHetroVariantsTxt.setEditable(editable);
        maxHetroVariantsTxt.setEnabled(editable);
        runGtpmCmb.setEnabled(editable);

        gPtmTable.setEnabled(editable);
        
        gPtmScrollPane.getViewport().setOpaque(false);
        gPtmTable.getTableHeader().setReorderingAllowed(false);

        gPtmTable.getColumn(" ").setMaxWidth(35);
        gPtmTable.getColumn(" ").setMinWidth(35);
        gPtmTable.getColumn("  ").setMaxWidth(35);
        gPtmTable.getColumn("  ").setMinWidth(35);
        gPtmTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());
        gPtmTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());
    }

    /**
     * Populates the GUI using the given settings.
     *
     * @param metaMorpheusParameters the parameters to display
     */
    private void populateGUI(MetaMorpheusParameters metaMorpheusParameters) {

        searchTypeCombo.setSelectedItem(metaMorpheusParameters.getSearchType());
        numberOfPartitionsTxt.setText(metaMorpheusParameters.getTotalPartitions() + "");
        dissociationTypeCombo.setSelectedItem(metaMorpheusParameters.getDissociationType());
        maxNumModPeptideTxt.setText(metaMorpheusParameters.getMaxModsForPeptide() + "");
        intitiatorMethBehaviorCombo.setSelectedItem(metaMorpheusParameters.getInitiatorMethionineBehavior());
        scoreCutoffTxt.setText(metaMorpheusParameters.getScoreCutoff() + "");

        if (metaMorpheusParameters.getUseDeltaScore()) {
            useDeltaScoreCmb.setSelectedIndex(0);
        } else {
            useDeltaScoreCmb.setSelectedIndex(1);
        }

        massDiffAcceptorTypeCmb.setSelectedItem(metaMorpheusParameters.getMassDiffAcceptorType());
        minPepLengthTxt.setText(metaMorpheusParameters.getMinPeptideLength() + "");
        maxPepLengthTxt.setText(metaMorpheusParameters.getMaxPeptideLength() + "");

        if (metaMorpheusParameters.getWriteMzId()) {
            writeMzidCmb.setSelectedIndex(0);
        } else {
            writeMzidCmb.setSelectedIndex(1);
        }

        if (metaMorpheusParameters.getWritePepXml()) {
            writePepXmlCmb.setSelectedIndex(0);
        } else {
            writePepXmlCmb.setSelectedIndex(1);
        }

        if (metaMorpheusParameters.getUseProvidedPrecursorInfo()) {
            useProvidedPrecCmb.setSelectedIndex(0);
        } else {
            useProvidedPrecCmb.setSelectedIndex(1);
        }

        if (metaMorpheusParameters.getDoPrecursorDeconvolution()) {
            doPrecDeconvCmb.setSelectedIndex(0);
        } else {
            doPrecDeconvCmb.setSelectedIndex(1);
        }

        deconvIntRatioTxt.setText(metaMorpheusParameters.getDeconvolutionIntensityRatio() + "");
        deconvMassToleranceTxt.setText(metaMorpheusParameters.getDeconvolutionMassTolerance() + "");
        deconvMassToleranceTypeCmb.setSelectedItem(metaMorpheusParameters.getDeconvolutionMassToleranceType());

        if (metaMorpheusParameters.getTrimMs1Peaks()) {
            trimMs1Cmb.setSelectedIndex(0);
        } else {
            trimMs1Cmb.setSelectedIndex(1);
        }

        if (metaMorpheusParameters.getTrimMsMsPeaks()) {
            trimMsMsCmb.setSelectedIndex(0);
        } else {
            trimMsMsCmb.setSelectedIndex(1);
        }

        numPeaksPerWindowTxt.setText(metaMorpheusParameters.getNumberOfPeaksToKeepPerWindow() + "");
        minAllowedIntensityRatioToBasePeakTxt.setText(metaMorpheusParameters.getMinAllowedIntensityRatioToBasePeak() + "");

        if (metaMorpheusParameters.getWindowWidthThomsons() != null) {
            windowWidthThompsonTxt.setText(metaMorpheusParameters.getWindowWidthThomsons() + "");
        }

        if (metaMorpheusParameters.getNumberOfWindows() != null) {
            numberOfWindowsTxt.setText(metaMorpheusParameters.getNumberOfWindows() + "");
        }

        if (metaMorpheusParameters.getNormalizePeaksAcrossAllWindows()) {
            normalizePeaksAcrossAllWindowsCmb.setSelectedIndex(0);
        } else {
            normalizePeaksAcrossAllWindowsCmb.setSelectedIndex(1);
        }

        if (metaMorpheusParameters.getModPeptidesAreDifferent()) {
            modifiedPeptidesAreDifferentCmb.setSelectedIndex(0);
        } else {
            modifiedPeptidesAreDifferentCmb.setSelectedIndex(1);
        }

        if (metaMorpheusParameters.getNoOneHitWonders()) {
            excludeOneHitWondersCmb.setSelectedIndex(0);
        } else {
            excludeOneHitWondersCmb.setSelectedIndex(1);
        }

        fragmentationTerminusCmb.setSelectedItem(metaMorpheusParameters.getFragmentationTerminus());
        maxFragmentSizeTxt.setText(metaMorpheusParameters.getMaxFragmentSize() + "");

        if (metaMorpheusParameters.getSearchTarget()) {
            searchTargetComboBox.setSelectedIndex(0);
        } else {
            searchTargetComboBox.setSelectedIndex(1);
        }

        decoyTypeCmb.setSelectedItem(metaMorpheusParameters.getDecoyType());
        maxModIsoformsTxt.setText(metaMorpheusParameters.getMaxModificationIsoforms() + "");
        minVariantDepthTxt.setText(metaMorpheusParameters.getMinVariantDepth() + "");
        maxHetroVariantsTxt.setText(metaMorpheusParameters.getMaxHeterozygousVariants() + "");

        if (metaMorpheusParameters.runGptm()) {
            runGtpmCmb.setSelectedIndex(0);
        } else {
            runGtpmCmb.setSelectedIndex(1);
        }

        for (int i = 0; i < ModificationCategory.values().length; i++) {
            
            ModificationCategory tempModCategory = ModificationCategory.values()[i];
            
            ((DefaultTableModel) gPtmTable.getModel()).addRow(
                    new Object[]{i+1,
                        tempModCategory,
                        metaMorpheusParameters.getGPtmCategories().contains(tempModCategory)
                    });
        }

    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public IdentificationAlgorithmParameter getParameters() {
        return getInput();
    }

    /**
     * Returns the user selection as MetaMorpheus parameters object.
     *
     * @return the user selection
     */
    public MetaMorpheusParameters getInput() {

        MetaMorpheusParameters tempMetaMorpheusParameters = new MetaMorpheusParameters();

        tempMetaMorpheusParameters.setSearchType((MetaMorpheusSearchType) searchTypeCombo.getSelectedItem());
        String input = numberOfPartitionsTxt.getText().trim();
        tempMetaMorpheusParameters.setTotalPartitions(Integer.valueOf(input));
        tempMetaMorpheusParameters.setDissociationType((MetaMorpheusDissociationType) dissociationTypeCombo.getSelectedItem());
        input = maxNumModPeptideTxt.getText().trim();
        tempMetaMorpheusParameters.setMaxModsForPeptide(Integer.valueOf(input));
        tempMetaMorpheusParameters.setInitiatorMethionineBehavior((MetaMorpheusInitiatorMethionineBehaviorType) intitiatorMethBehaviorCombo.getSelectedItem());
        input = scoreCutoffTxt.getText().trim();
        tempMetaMorpheusParameters.setScoreCutoff(Double.valueOf(input));
        tempMetaMorpheusParameters.setUseDeltaScore(useDeltaScoreCmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setMassDiffAcceptorType((MetaMorpheusMassDiffAcceptorType) massDiffAcceptorTypeCmb.getSelectedItem());
        input = minPepLengthTxt.getText().trim();
        tempMetaMorpheusParameters.setMinPeptideLength(Integer.valueOf(input));
        input = maxPepLengthTxt.getText().trim();
        tempMetaMorpheusParameters.setMaxPeptideLength(Integer.valueOf(input));
        tempMetaMorpheusParameters.setWriteMzId(writeMzidCmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setWritePepXml(writePepXmlCmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setUseProvidedPrecursorInfo(useProvidedPrecCmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setDoPrecursorDeconvolution(doPrecDeconvCmb.getSelectedIndex() == 0);
        input = deconvIntRatioTxt.getText().trim();
        tempMetaMorpheusParameters.setDeconvolutionIntensityRatio(Double.valueOf(input));
        input = deconvMassToleranceTxt.getText().trim();
        tempMetaMorpheusParameters.setDeconvolutionMassTolerance(Double.valueOf(input));
        tempMetaMorpheusParameters.setDeconvolutionMassToleranceType((MetaMorpheusToleranceType) deconvMassToleranceTypeCmb.getSelectedItem());
        tempMetaMorpheusParameters.setTrimMs1Peaks(trimMs1Cmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setTrimMsMsPeaks(trimMsMsCmb.getSelectedIndex() == 0);
        input = numPeaksPerWindowTxt.getText().trim();
        tempMetaMorpheusParameters.setNumberOfPeaksToKeepPerWindow(Integer.valueOf(input));
        input = minAllowedIntensityRatioToBasePeakTxt.getText().trim();
        tempMetaMorpheusParameters.setMinAllowedIntensityRatioToBasePeak(Double.valueOf(input));
        input = windowWidthThompsonTxt.getText().trim();
        if (!input.equals("")) {
            tempMetaMorpheusParameters.setWindowWidthThomsons(new Double(input));
        }
        input = numberOfWindowsTxt.getText().trim();
        if (!input.equals("")) {
            tempMetaMorpheusParameters.setNumberOfWindows(new Integer(input));
        }
        tempMetaMorpheusParameters.setNormalizePeaksAcrossAllWindows(normalizePeaksAcrossAllWindowsCmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setModPeptidesAreDifferent(modifiedPeptidesAreDifferentCmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setNoOneHitWonders(excludeOneHitWondersCmb.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setFragmentationTerminus((MetaMorpheusFragmentationTerminusType) fragmentationTerminusCmb.getSelectedItem());
        input = maxFragmentSizeTxt.getText().trim();
        tempMetaMorpheusParameters.setMaxFragmentSize(Double.valueOf(input));
        tempMetaMorpheusParameters.setSearchTarget(searchTargetComboBox.getSelectedIndex() == 0);
        tempMetaMorpheusParameters.setDecoyType((MetaMorpheusDecoyType) decoyTypeCmb.getSelectedItem());
        input = maxModIsoformsTxt.getText().trim();
        tempMetaMorpheusParameters.setMaxModificationIsoforms(Integer.valueOf(input));
        input = minVariantDepthTxt.getText().trim();
        tempMetaMorpheusParameters.setMinVariantDepth(Integer.valueOf(input));
        input = maxHetroVariantsTxt.getText().trim();
        tempMetaMorpheusParameters.setMaxHeterozygousVariants(Integer.valueOf(input));
        tempMetaMorpheusParameters.setRunGptm(runGtpmCmb.getSelectedIndex() == 0);

        ArrayList<ModificationCategory> gPtmCategories = new ArrayList<>();
        
        for (int i=0; i<gPtmTable.getRowCount(); i++) {
            if ((Boolean) gPtmTable.getValueAt(i, 2)) {
                gPtmCategories.add((ModificationCategory) gPtmTable.getValueAt(i, 1));
            }
        }

        tempMetaMorpheusParameters.setGPtmCategories(gPtmCategories);

        return tempMetaMorpheusParameters;
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
        openDialogHelpJButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        advancedSettingsWarningLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchTypeLabel = new javax.swing.JLabel();
        searchTypeCombo = new javax.swing.JComboBox();
        numberOfPartitionsLbl = new javax.swing.JLabel();
        numberOfPartitionsTxt = new javax.swing.JTextField();
        dissociationTypeLbl = new javax.swing.JLabel();
        dissociationTypeCombo = new javax.swing.JComboBox();
        maxNumModPeptideLbl = new javax.swing.JLabel();
        maxNumModPeptideTxt = new javax.swing.JTextField();
        intitiatorMethBehaviorLabel = new javax.swing.JLabel();
        intitiatorMethBehaviorCombo = new javax.swing.JComboBox();
        scoreCutoffLabel = new javax.swing.JLabel();
        scoreCutoffTxt = new javax.swing.JTextField();
        useDeltaScoreLbl = new javax.swing.JLabel();
        useDeltaScoreCmb = new javax.swing.JComboBox();
        massDiffAcceptorTypeLbl = new javax.swing.JLabel();
        massDiffAcceptorTypeCmb = new javax.swing.JComboBox();
        peptideLengthJLabel = new javax.swing.JLabel();
        minPepLengthTxt = new javax.swing.JTextField();
        peptideLengthDividerLabel = new javax.swing.JLabel();
        maxPepLengthTxt = new javax.swing.JTextField();
        outputPanel = new javax.swing.JPanel();
        writeMzidLbl = new javax.swing.JLabel();
        writeMzidCmb = new javax.swing.JComboBox();
        writePepXmlLbl = new javax.swing.JLabel();
        writePepXmlCmb = new javax.swing.JComboBox();
        deisotopingPanel = new javax.swing.JPanel();
        useProvidedPrecLabel = new javax.swing.JLabel();
        useProvidedPrecCmb = new javax.swing.JComboBox();
        doPrecDeconvLbl = new javax.swing.JLabel();
        doPrecDeconvCmb = new javax.swing.JComboBox();
        deconvIntRatioLbl = new javax.swing.JLabel();
        deconvIntRatioTxt = new javax.swing.JTextField();
        deconvMassToleranceLbl = new javax.swing.JLabel();
        deconvMassToleranceTxt = new javax.swing.JTextField();
        deconvMassToleranceTypeLbl = new javax.swing.JLabel();
        deconvMassToleranceTypeCmb = new javax.swing.JComboBox();
        peakTrimmingPanel = new javax.swing.JPanel();
        trimMs1Lbl = new javax.swing.JLabel();
        trimMs1Cmb = new javax.swing.JComboBox();
        trimMsMsLbl = new javax.swing.JLabel();
        trimMsMsCmb = new javax.swing.JComboBox();
        numPeaksPerWindowLbl = new javax.swing.JLabel();
        numPeaksPerWindowTxt = new javax.swing.JTextField();
        minAllowedIntensityRatioToBasePeakLbl = new javax.swing.JLabel();
        minAllowedIntensityRatioToBasePeakTxt = new javax.swing.JTextField();
        windowWidthThompsonLbl = new javax.swing.JLabel();
        windowWidthThompsonTxt = new javax.swing.JTextField();
        numberOfWindowsLbl = new javax.swing.JLabel();
        numberOfWindowsTxt = new javax.swing.JTextField();
        normalizePeaksAcrossAllWindowsLbl = new javax.swing.JLabel();
        normalizePeaksAcrossAllWindowsCmb = new javax.swing.JComboBox();
        proteinGroupingPanel = new javax.swing.JPanel();
        modifiedPeptidesAreDifferentLbl = new javax.swing.JLabel();
        modifiedPeptidesAreDifferentCmb = new javax.swing.JComboBox();
        excludeOneHitWondersLbl = new javax.swing.JLabel();
        excludeOneHitWondersCmb = new javax.swing.JComboBox();
        inSilicoDigestionPanel = new javax.swing.JPanel();
        fragmentationTerminusLbl = new javax.swing.JLabel();
        fragmentationTerminusCmb = new javax.swing.JComboBox();
        maxFragmentSizeLbl = new javax.swing.JLabel();
        maxFragmentSizeTxt = new javax.swing.JTextField();
        searchTargetLabel = new javax.swing.JLabel();
        searchTargetComboBox = new javax.swing.JComboBox();
        decoyTypeLbl = new javax.swing.JLabel();
        decoyTypeCmb = new javax.swing.JComboBox();
        maxModIsoformsLbl = new javax.swing.JLabel();
        maxModIsoformsTxt = new javax.swing.JTextField();
        minVariantDepthLabel = new javax.swing.JLabel();
        minVariantDepthTxt = new javax.swing.JTextField();
        maxHetroVariantsLbl = new javax.swing.JLabel();
        maxHetroVariantsTxt = new javax.swing.JTextField();
        gptmPanel = new javax.swing.JPanel();
        runGptmLbl = new javax.swing.JLabel();
        runGtpmCmb = new javax.swing.JComboBox();
        gPtmScrollPane = new javax.swing.JScrollPane();
        gPtmTable = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MetaMorpheus Advanced Settings");
        setMinimumSize(new java.awt.Dimension(600, 500));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

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

        advancedSettingsWarningLabel.setText("Click to open the MetaMorpheus help page.");

        tabbedPane.setBackground(new java.awt.Color(230, 230, 230));
        tabbedPane.setOpaque(true);
        tabbedPane.setPreferredSize(new java.awt.Dimension(616, 180));

        searchPanel.setBackground(new java.awt.Color(230, 230, 230));
        searchPanel.setPreferredSize(new java.awt.Dimension(518, 143));

        searchTypeLabel.setText("Search Type");

        searchTypeCombo.setModel(new DefaultComboBoxModel(MetaMorpheusSearchType.values()));

        numberOfPartitionsLbl.setText("Number of Partitions");

        numberOfPartitionsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfPartitionsTxt.setText("1");
        numberOfPartitionsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numberOfPartitionsTxtKeyReleased(evt);
            }
        });

        dissociationTypeLbl.setText("Dissociation Type");

        dissociationTypeCombo.setModel(new DefaultComboBoxModel(MetaMorpheusParameters.MetaMorpheusDissociationType.values()));

        maxNumModPeptideLbl.setText("Maximum Number of Modifications per Peptide");

        maxNumModPeptideTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxNumModPeptideTxt.setText("2");
        maxNumModPeptideTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxNumModPeptideTxtKeyReleased(evt);
            }
        });

        intitiatorMethBehaviorLabel.setText("Initiator Methionine Behavior");

        intitiatorMethBehaviorCombo.setModel(new DefaultComboBoxModel(MetaMorpheusParameters.MetaMorpheusInitiatorMethionineBehaviorType.values()));

        scoreCutoffLabel.setText("Score Cut-off");

        scoreCutoffTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        scoreCutoffTxt.setText("5.0");
        scoreCutoffTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                scoreCutoffTxtKeyReleased(evt);
            }
        });

        useDeltaScoreLbl.setText("Use Delta Score");

        useDeltaScoreCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        useDeltaScoreCmb.setSelectedIndex(1);

        massDiffAcceptorTypeLbl.setText("Mass Difference Acceptor Type");

        massDiffAcceptorTypeCmb.setModel(new DefaultComboBoxModel(MetaMorpheusParameters.MetaMorpheusMassDiffAcceptorType.values()));

        peptideLengthJLabel.setText("Peptide Length (min - max)");

        minPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPepLengthTxt.setText("8");
        minPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPepLengthTxtKeyReleased(evt);
            }
        });

        peptideLengthDividerLabel.setText("-");

        maxPepLengthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPepLengthTxt.setText("30");
        maxPepLengthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPepLengthTxtKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(searchTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(peptideLengthJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dissociationTypeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(maxNumModPeptideLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(intitiatorMethBehaviorLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(numberOfPartitionsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(massDiffAcceptorTypeLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(useDeltaScoreLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(scoreCutoffLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(massDiffAcceptorTypeCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(useDeltaScoreCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(scoreCutoffTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(intitiatorMethBehaviorCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxNumModPeptideTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(numberOfPartitionsTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchTypeCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dissociationTypeCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                        .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(peptideLengthDividerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );

        searchPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {dissociationTypeCombo, intitiatorMethBehaviorCombo, massDiffAcceptorTypeCmb, maxNumModPeptideTxt, numberOfPartitionsTxt, scoreCutoffTxt, searchTypeCombo, useDeltaScoreCmb});

        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchTypeLabel)
                    .addComponent(searchTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberOfPartitionsLbl)
                    .addComponent(numberOfPartitionsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dissociationTypeLbl)
                    .addComponent(dissociationTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxNumModPeptideLbl)
                    .addComponent(maxNumModPeptideTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intitiatorMethBehaviorLabel)
                    .addComponent(intitiatorMethBehaviorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scoreCutoffLabel)
                    .addComponent(scoreCutoffTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(useDeltaScoreLbl)
                    .addComponent(useDeltaScoreCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(massDiffAcceptorTypeLbl)
                    .addComponent(massDiffAcceptorTypeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideLengthJLabel)
                    .addComponent(minPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPepLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideLengthDividerLabel))
                .addContainerGap())
        );

        tabbedPane.addTab("Search", searchPanel);

        outputPanel.setBackground(new java.awt.Color(230, 230, 230));

        writeMzidLbl.setText("Write mzIdentML");

        writeMzidCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        writePepXmlLbl.setText("Write pepXML");

        writePepXmlCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        writePepXmlCmb.setSelectedIndex(1);

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(writeMzidLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(writePepXmlLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(writeMzidCmb, 0, 185, Short.MAX_VALUE)
                    .addComponent(writePepXmlCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(writeMzidLbl)
                    .addComponent(writeMzidCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(writePepXmlLbl)
                    .addComponent(writePepXmlCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Output", outputPanel);

        deisotopingPanel.setBackground(new java.awt.Color(230, 230, 230));
        deisotopingPanel.setPreferredSize(new java.awt.Dimension(518, 143));

        useProvidedPrecLabel.setText("Use Provided Precursor Info");

        useProvidedPrecCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        doPrecDeconvLbl.setText("Do Precursor Deconvolution");

        doPrecDeconvCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        deconvIntRatioLbl.setText("Deconvolution Intensity Ratio");

        deconvIntRatioTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        deconvIntRatioTxt.setText("3.0");
        deconvIntRatioTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                deconvIntRatioTxtKeyReleased(evt);
            }
        });

        deconvMassToleranceLbl.setText("Deconvolution Mass Tolerance");

        deconvMassToleranceTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        deconvMassToleranceTxt.setText("4.0");
        deconvMassToleranceTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                deconvMassToleranceTxtKeyReleased(evt);
            }
        });

        deconvMassToleranceTypeLbl.setText("Deconvolution Mass Tolerance Type");

        deconvMassToleranceTypeCmb.setModel(new DefaultComboBoxModel(MetaMorpheusToleranceType.values()));

        javax.swing.GroupLayout deisotopingPanelLayout = new javax.swing.GroupLayout(deisotopingPanel);
        deisotopingPanel.setLayout(deisotopingPanelLayout);
        deisotopingPanelLayout.setHorizontalGroup(
            deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deisotopingPanelLayout.createSequentialGroup()
                .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(deisotopingPanelLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useProvidedPrecLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deconvIntRatioLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(doPrecDeconvLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deconvMassToleranceLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, deisotopingPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(deconvMassToleranceTypeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(useProvidedPrecCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, 185, Short.MAX_VALUE)
                    .addComponent(doPrecDeconvCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deconvIntRatioTxt, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(deconvMassToleranceTxt, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(deconvMassToleranceTypeCmb, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        deisotopingPanelLayout.setVerticalGroup(
            deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, deisotopingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useProvidedPrecLabel)
                    .addComponent(useProvidedPrecCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doPrecDeconvLbl)
                    .addComponent(doPrecDeconvCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deconvIntRatioLbl)
                    .addComponent(deconvIntRatioTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deconvMassToleranceLbl)
                    .addComponent(deconvMassToleranceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deisotopingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deconvMassToleranceTypeLbl)
                    .addComponent(deconvMassToleranceTypeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(99, 99, 99))
        );

        tabbedPane.addTab("Deisotoping", deisotopingPanel);

        peakTrimmingPanel.setBackground(new java.awt.Color(230, 230, 230));

        trimMs1Lbl.setText("Trim MS1 Peaks");

        trimMs1Cmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        trimMs1Cmb.setSelectedIndex(1);

        trimMsMsLbl.setText("Trim MSMS Peaks");

        trimMsMsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        numPeaksPerWindowLbl.setText("Number of Peaks to Keep per Window");

        numPeaksPerWindowTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numPeaksPerWindowTxt.setText("200");
        numPeaksPerWindowTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numPeaksPerWindowTxtKeyReleased(evt);
            }
        });

        minAllowedIntensityRatioToBasePeakLbl.setText("Minimum Allowed Intensity Ratio to Base Peak ");

        minAllowedIntensityRatioToBasePeakTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minAllowedIntensityRatioToBasePeakTxt.setText("0.01");
        minAllowedIntensityRatioToBasePeakTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minAllowedIntensityRatioToBasePeakTxtKeyReleased(evt);
            }
        });

        windowWidthThompsonLbl.setText("Window Width in Thomson");

        windowWidthThompsonTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        windowWidthThompsonTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                windowWidthThompsonTxtKeyReleased(evt);
            }
        });

        numberOfWindowsLbl.setText("Number of Windows");

        numberOfWindowsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfWindowsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numberOfWindowsTxtKeyReleased(evt);
            }
        });

        normalizePeaksAcrossAllWindowsLbl.setText("Normalize Peaks Accross All Windows");

        normalizePeaksAcrossAllWindowsCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        normalizePeaksAcrossAllWindowsCmb.setSelectedIndex(1);

        javax.swing.GroupLayout peakTrimmingPanelLayout = new javax.swing.GroupLayout(peakTrimmingPanel);
        peakTrimmingPanel.setLayout(peakTrimmingPanelLayout);
        peakTrimmingPanelLayout.setHorizontalGroup(
            peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, peakTrimmingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(peakTrimmingPanelLayout.createSequentialGroup()
                        .addComponent(normalizePeaksAcrossAllWindowsLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(38, 38, 38))
                    .addGroup(peakTrimmingPanelLayout.createSequentialGroup()
                        .addComponent(numberOfWindowsLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(51, 51, 51))
                    .addGroup(peakTrimmingPanelLayout.createSequentialGroup()
                        .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(windowWidthThompsonLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(trimMs1Lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(trimMsMsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(numPeaksPerWindowLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minAllowedIntensityRatioToBasePeakLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(trimMs1Cmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trimMsMsCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numPeaksPerWindowTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minAllowedIntensityRatioToBasePeakTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(windowWidthThompsonTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numberOfWindowsTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(normalizePeaksAcrossAllWindowsCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        peakTrimmingPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {minAllowedIntensityRatioToBasePeakTxt, numPeaksPerWindowTxt, numberOfWindowsTxt, windowWidthThompsonTxt});

        peakTrimmingPanelLayout.setVerticalGroup(
            peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peakTrimmingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trimMs1Lbl)
                    .addComponent(trimMs1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trimMsMsLbl)
                    .addComponent(trimMsMsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numPeaksPerWindowLbl)
                    .addComponent(numPeaksPerWindowTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minAllowedIntensityRatioToBasePeakLbl)
                    .addComponent(minAllowedIntensityRatioToBasePeakTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(windowWidthThompsonLbl)
                    .addComponent(windowWidthThompsonTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberOfWindowsLbl)
                    .addComponent(numberOfWindowsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakTrimmingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(normalizePeaksAcrossAllWindowsLbl)
                    .addComponent(normalizePeaksAcrossAllWindowsCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Peak Trimming", peakTrimmingPanel);

        proteinGroupingPanel.setBackground(new java.awt.Color(230, 230, 230));

        modifiedPeptidesAreDifferentLbl.setText("Modified Peptides Are Different");

        modifiedPeptidesAreDifferentCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        modifiedPeptidesAreDifferentCmb.setSelectedIndex(1);

        excludeOneHitWondersLbl.setText("Exlude One Hit Wonders");

        excludeOneHitWondersCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        excludeOneHitWondersCmb.setSelectedIndex(1);

        javax.swing.GroupLayout proteinGroupingPanelLayout = new javax.swing.GroupLayout(proteinGroupingPanel);
        proteinGroupingPanel.setLayout(proteinGroupingPanelLayout);
        proteinGroupingPanelLayout.setHorizontalGroup(
            proteinGroupingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteinGroupingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(proteinGroupingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modifiedPeptidesAreDifferentLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(excludeOneHitWondersLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(proteinGroupingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(modifiedPeptidesAreDifferentCmb, 0, 185, Short.MAX_VALUE)
                    .addComponent(excludeOneHitWondersCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        proteinGroupingPanelLayout.setVerticalGroup(
            proteinGroupingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proteinGroupingPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(proteinGroupingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modifiedPeptidesAreDifferentLbl)
                    .addComponent(modifiedPeptidesAreDifferentCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteinGroupingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(excludeOneHitWondersLbl)
                    .addComponent(excludeOneHitWondersCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Protein Grouping", proteinGroupingPanel);

        inSilicoDigestionPanel.setBackground(new java.awt.Color(230, 230, 230));

        fragmentationTerminusLbl.setText("Fragmentation Terminus");

        fragmentationTerminusCmb.setModel(new DefaultComboBoxModel(MetaMorpheusParameters.MetaMorpheusFragmentationTerminusType.values()));

        maxFragmentSizeLbl.setText("Max Fragment Size");

        maxFragmentSizeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxFragmentSizeTxt.setText("30000");
        maxFragmentSizeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxFragmentSizeTxtKeyReleased(evt);
            }
        });

        searchTargetLabel.setText("Search Target");

        searchTargetComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

        decoyTypeLbl.setText("Decoy Type");

        decoyTypeCmb.setModel(new DefaultComboBoxModel(MetaMorpheusParameters.MetaMorpheusDecoyType.values()));

        maxModIsoformsLbl.setText("Max Modification Isoforms");

        maxModIsoformsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxModIsoformsTxt.setText("1024");
        maxModIsoformsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxModIsoformsTxtKeyReleased(evt);
            }
        });

        minVariantDepthLabel.setText("Minimum Variant Depth");

        minVariantDepthTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minVariantDepthTxt.setText("1");
        minVariantDepthTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minVariantDepthTxtKeyReleased(evt);
            }
        });

        maxHetroVariantsLbl.setText("Maximum Heterozygous Variants");

        maxHetroVariantsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxHetroVariantsTxt.setText("4");
        maxHetroVariantsTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxHetroVariantsTxtKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout inSilicoDigestionPanelLayout = new javax.swing.GroupLayout(inSilicoDigestionPanel);
        inSilicoDigestionPanel.setLayout(inSilicoDigestionPanelLayout);
        inSilicoDigestionPanelLayout.setHorizontalGroup(
            inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inSilicoDigestionPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchTargetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxFragmentSizeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentationTerminusLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minVariantDepthLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxModIsoformsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(decoyTypeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxHetroVariantsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fragmentationTerminusCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxFragmentSizeTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchTargetComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(decoyTypeCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxModIsoformsTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minVariantDepthTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxHetroVariantsTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );
        inSilicoDigestionPanelLayout.setVerticalGroup(
            inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inSilicoDigestionPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentationTerminusLbl)
                    .addComponent(fragmentationTerminusCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxFragmentSizeLbl)
                    .addComponent(maxFragmentSizeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchTargetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchTargetLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(decoyTypeLbl)
                    .addComponent(decoyTypeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxModIsoformsLbl)
                    .addComponent(maxModIsoformsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minVariantDepthLabel)
                    .addComponent(minVariantDepthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inSilicoDigestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxHetroVariantsLbl)
                    .addComponent(maxHetroVariantsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("In Silico Digestion", inSilicoDigestionPanel);

        gptmPanel.setBackground(new java.awt.Color(230, 230, 230));

        runGptmLbl.setText("Run G-PTM Search");

        runGtpmCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        runGtpmCmb.setSelectedIndex(1);
        runGtpmCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runGtpmCmbActionPerformed(evt);
            }
        });

        gPtmTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Category", "  "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        gPtmTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gPtmScrollPane.setViewportView(gPtmTable);

        javax.swing.GroupLayout gptmPanelLayout = new javax.swing.GroupLayout(gptmPanel);
        gptmPanel.setLayout(gptmPanelLayout);
        gptmPanelLayout.setHorizontalGroup(
            gptmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gptmPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(gptmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(gPtmScrollPane)
                    .addGroup(gptmPanelLayout.createSequentialGroup()
                        .addComponent(runGptmLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(runGtpmCmb, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        gptmPanelLayout.setVerticalGroup(
            gptmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gptmPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(gptmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runGptmLbl)
                    .addComponent(runGtpmCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(gPtmScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("G-PTM Search", gptmPanel);

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(openDialogHelpJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advancedSettingsWarningLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton)
                    .addComponent(advancedSettingsWarningLabel)
                    .addComponent(okButton)
                    .addComponent(closeButton))
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
     * Save the settings and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput(true)) {
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the without saving.
     *
     * @param evt
     */
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        cancelled = true;
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

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
     * Open the MetaMorpheus help page.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://github.com/smith-chem-wisc/MetaMorpheus/wiki");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxFragmentSizeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxFragmentSizeTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxFragmentSizeTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxPepLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPepLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxPepLengthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minPepLengthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPepLengthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minPepLengthTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void numPeaksPerWindowTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numPeaksPerWindowTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_numPeaksPerWindowTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void deconvIntRatioTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deconvIntRatioTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_deconvIntRatioTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void deconvMassToleranceTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deconvMassToleranceTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_deconvMassToleranceTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxNumModPeptideTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxNumModPeptideTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxNumModPeptideTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void numberOfPartitionsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberOfPartitionsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_numberOfPartitionsTxtKeyReleased

    /**
     * Close the without saving.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minAllowedIntensityRatioToBasePeakTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minAllowedIntensityRatioToBasePeakTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minAllowedIntensityRatioToBasePeakTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void windowWidthThompsonTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_windowWidthThompsonTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_windowWidthThompsonTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void numberOfWindowsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberOfWindowsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_numberOfWindowsTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxModIsoformsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxModIsoformsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxModIsoformsTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void maxHetroVariantsTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxHetroVariantsTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_maxHetroVariantsTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void scoreCutoffTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scoreCutoffTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_scoreCutoffTxtKeyReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void minVariantDepthTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minVariantDepthTxtKeyReleased
        validateInput(false);
    }//GEN-LAST:event_minVariantDepthTxtKeyReleased

    /**
     * Enable/disable the G-PTM table.
     *
     * @param evt
     */
    private void runGtpmCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runGtpmCmbActionPerformed
        gPtmTable.setEnabled(runGtpmCmb.getSelectedIndex() == 0);
    }//GEN-LAST:event_runGtpmCmbActionPerformed

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateInput(boolean showMessage) {

        boolean valid = true;

        valid = GuiUtilities.validateIntegerInput(this, numberOfPartitionsLbl, numberOfPartitionsTxt, "number of partitions", "Number of Partitions Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxNumModPeptideLbl, maxNumModPeptideTxt, "number of modifictions per peptide", "Number of Modifications Error", false, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, scoreCutoffLabel, scoreCutoffTxt, "score cut-off", "Score Cut-off Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, peptideLengthJLabel, minPepLengthTxt, "minimim peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, peptideLengthJLabel, maxPepLengthTxt, "maximum peptide length", "Peptide Length Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, deconvIntRatioLbl, deconvIntRatioTxt, "deconvolution intensity ratio", "Deconvolution Itensity Ratio Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, deconvMassToleranceLbl, deconvMassToleranceTxt, "deconvolution mass tolerance", "Deconvolution Mass Tolerance Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, numPeaksPerWindowLbl, numPeaksPerWindowTxt, "number of peaks per window", "Number of Peaks per Window Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, minAllowedIntensityRatioToBasePeakLbl, minAllowedIntensityRatioToBasePeakTxt, "minimum allowed intensity ratio", "Minimum Intensity Ratio Error", true, showMessage, valid);

        if (!windowWidthThompsonTxt.getText().isEmpty()) {
            valid = GuiUtilities.validateDoubleInput(this, windowWidthThompsonLbl, windowWidthThompsonTxt, "window with in Thompson", "Window Thompson Width Error", true, showMessage, valid);
        }
        if (!numberOfWindowsTxt.getText().isEmpty()) {
            valid = GuiUtilities.validateIntegerInput(this, numberOfWindowsLbl, numberOfWindowsTxt, "number of windows", "Number of Windows Error", true, showMessage, valid);
        }

        valid = GuiUtilities.validateDoubleInput(this, maxFragmentSizeLbl, maxFragmentSizeTxt, "maximum fragment size", "Maximum Fragment Size Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxModIsoformsLbl, maxModIsoformsTxt, "maximum modification isoforms", "Maximum Modification Isoforms Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, minVariantDepthLabel, minVariantDepthTxt, "minimum variant depth", "Minimum Variant Depth Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, maxHetroVariantsLbl, maxHetroVariantsTxt, "maximum hetrozygous variants", "Maximum Hetrozygous Variants Error", true, showMessage, valid);

        // peptide length: the low value should be lower than the high value
        try {
            double lowValue = Double.parseDouble(minPepLengthTxt.getText().trim());
            double highValue = Double.parseDouble(maxPepLengthTxt.getText().trim());

            if (lowValue > highValue) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "The lower range value has to be smaller than the upper range value.",
                            "Peptide Length Error", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                peptideLengthJLabel.setForeground(Color.RED);
                peptideLengthJLabel.setToolTipText("Please select a valid range (upper <= higher)");
            }
        } catch (NumberFormatException e) {
            // ignore, handled above
        }

        okButton.setEnabled(valid);

        return valid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel advancedSettingsWarningLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel deconvIntRatioLbl;
    private javax.swing.JTextField deconvIntRatioTxt;
    private javax.swing.JLabel deconvMassToleranceLbl;
    private javax.swing.JTextField deconvMassToleranceTxt;
    private javax.swing.JComboBox deconvMassToleranceTypeCmb;
    private javax.swing.JLabel deconvMassToleranceTypeLbl;
    private javax.swing.JComboBox decoyTypeCmb;
    private javax.swing.JLabel decoyTypeLbl;
    private javax.swing.JPanel deisotopingPanel;
    private javax.swing.JComboBox dissociationTypeCombo;
    private javax.swing.JLabel dissociationTypeLbl;
    private javax.swing.JComboBox doPrecDeconvCmb;
    private javax.swing.JLabel doPrecDeconvLbl;
    private javax.swing.JComboBox excludeOneHitWondersCmb;
    private javax.swing.JLabel excludeOneHitWondersLbl;
    private javax.swing.JComboBox fragmentationTerminusCmb;
    private javax.swing.JLabel fragmentationTerminusLbl;
    private javax.swing.JScrollPane gPtmScrollPane;
    private javax.swing.JTable gPtmTable;
    private javax.swing.JPanel gptmPanel;
    private javax.swing.JPanel inSilicoDigestionPanel;
    private javax.swing.JComboBox intitiatorMethBehaviorCombo;
    private javax.swing.JLabel intitiatorMethBehaviorLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox massDiffAcceptorTypeCmb;
    private javax.swing.JLabel massDiffAcceptorTypeLbl;
    private javax.swing.JLabel maxFragmentSizeLbl;
    private javax.swing.JTextField maxFragmentSizeTxt;
    private javax.swing.JLabel maxHetroVariantsLbl;
    private javax.swing.JTextField maxHetroVariantsTxt;
    private javax.swing.JLabel maxModIsoformsLbl;
    private javax.swing.JTextField maxModIsoformsTxt;
    private javax.swing.JLabel maxNumModPeptideLbl;
    private javax.swing.JTextField maxNumModPeptideTxt;
    private javax.swing.JTextField maxPepLengthTxt;
    private javax.swing.JLabel minAllowedIntensityRatioToBasePeakLbl;
    private javax.swing.JTextField minAllowedIntensityRatioToBasePeakTxt;
    private javax.swing.JTextField minPepLengthTxt;
    private javax.swing.JLabel minVariantDepthLabel;
    private javax.swing.JTextField minVariantDepthTxt;
    private javax.swing.JComboBox modifiedPeptidesAreDifferentCmb;
    private javax.swing.JLabel modifiedPeptidesAreDifferentLbl;
    private javax.swing.JComboBox normalizePeaksAcrossAllWindowsCmb;
    private javax.swing.JLabel normalizePeaksAcrossAllWindowsLbl;
    private javax.swing.JLabel numPeaksPerWindowLbl;
    private javax.swing.JTextField numPeaksPerWindowTxt;
    private javax.swing.JLabel numberOfPartitionsLbl;
    private javax.swing.JTextField numberOfPartitionsTxt;
    private javax.swing.JLabel numberOfWindowsLbl;
    private javax.swing.JTextField numberOfWindowsTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JPanel peakTrimmingPanel;
    private javax.swing.JLabel peptideLengthDividerLabel;
    private javax.swing.JLabel peptideLengthJLabel;
    private javax.swing.JPanel proteinGroupingPanel;
    private javax.swing.JLabel runGptmLbl;
    private javax.swing.JComboBox runGtpmCmb;
    private javax.swing.JLabel scoreCutoffLabel;
    private javax.swing.JTextField scoreCutoffTxt;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JComboBox searchTargetComboBox;
    private javax.swing.JLabel searchTargetLabel;
    private javax.swing.JComboBox searchTypeCombo;
    private javax.swing.JLabel searchTypeLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JComboBox trimMs1Cmb;
    private javax.swing.JLabel trimMs1Lbl;
    private javax.swing.JComboBox trimMsMsCmb;
    private javax.swing.JLabel trimMsMsLbl;
    private javax.swing.JComboBox useDeltaScoreCmb;
    private javax.swing.JLabel useDeltaScoreLbl;
    private javax.swing.JComboBox useProvidedPrecCmb;
    private javax.swing.JLabel useProvidedPrecLabel;
    private javax.swing.JLabel windowWidthThompsonLbl;
    private javax.swing.JTextField windowWidthThompsonTxt;
    private javax.swing.JComboBox writeMzidCmb;
    private javax.swing.JLabel writeMzidLbl;
    private javax.swing.JComboBox writePepXmlCmb;
    private javax.swing.JLabel writePepXmlLbl;
    // End of variables declaration//GEN-END:variables
}
