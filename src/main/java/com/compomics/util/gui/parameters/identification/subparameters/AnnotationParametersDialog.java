package com.compomics.util.gui.parameters.identification.subparameters;

import com.compomics.util.experiment.biology.ions.Ion.IonType;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.impl.TagFragmentIon;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;

/**
 * A simple dialog for setting the spectrum annotation preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class AnnotationParametersDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * List of possible reporter ions.
     */
    private ArrayList<Integer> reporterIons;
    /**
     * Map of the neutral losses selection.
     */
    private HashMap<NeutralLoss, Boolean> neutralLossesMap;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;
    /**
     * The fragment ion accuracy used for the search.
     */
    private double maxFragmentIonAccuracy;

    /**
     * Creates a new AnnotationPreferencesDialog with a frame as owner.
     *
     * @param parentFrame the parent frame
     * @param annotationSettings previous annotation settings
     * @param maxFragmentIonAccuracy the fragment ion accuracy used for the
     * search
     * @param possibleNeutralLosses the list of possible neutral losses
     * @param reporterIons the list of possible reporter ions indexed by their
     * subtypes
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public AnnotationParametersDialog(java.awt.Frame parentFrame, AnnotationSettings annotationSettings, double maxFragmentIonAccuracy,
            ArrayList<NeutralLoss> possibleNeutralLosses, ArrayList<Integer> reporterIons, boolean editable) {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.reporterIons = reporterIons;
        this.editable = editable;
        this.maxFragmentIonAccuracy = maxFragmentIonAccuracy;
        initComponents();
        setUpGui();
        populateGui(annotationSettings, possibleNeutralLosses);
        this.setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new AnnotationPreferencesDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame the parent frame
     * @param annotationSettings previous annotation settings
     * @param maxFragmentIonAccuracy the fragment ion accuracy used for the
     * search
     * @param possibleNeutralLosses the list of possible neutral losses
     * @param reporterIons the list of possible reporter ions indexed by their
     * subtypes
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public AnnotationParametersDialog(Dialog owner, java.awt.Frame parentFrame, AnnotationSettings annotationSettings, double maxFragmentIonAccuracy,
            ArrayList<NeutralLoss> possibleNeutralLosses, ArrayList<Integer> reporterIons, boolean editable) {
        super(owner, true);
        this.parentFrame = parentFrame;
        this.reporterIons = reporterIons;
        this.editable = editable;
        this.maxFragmentIonAccuracy = maxFragmentIonAccuracy;
        initComponents();
        setUpGui();
        populateGui(annotationSettings, possibleNeutralLosses);
        this.setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Sets up the GUI.
     */
    private void setUpGui() {

        // set main table properties
        neutralLossesTable.getTableHeader().setReorderingAllowed(false);

        // make sure that the scroll panes are see-through
        neutralLossScrollPane.getViewport().setOpaque(false);

        neutralLossesTable.getColumn(" ").setMaxWidth(50);
        neutralLossesTable.getColumn(" ").setMinWidth(50);
        neutralLossesTable.getColumn("  ").setMaxWidth(30);
        neutralLossesTable.getColumn("  ").setMinWidth(30);

        neutralLossesTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());
        neutralLossesTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/selected_green.png")),
                null,
                "Selected", null));
        

        aBox.setEnabled(editable);
        bBox.setEnabled(editable);
        cBox.setEnabled(editable);
        xBox.setEnabled(editable);
        yBox.setEnabled(editable);
        zBox.setEnabled(editable);
        precursorBox.setEnabled(editable);
        immoniumBox.setEnabled(editable);
        reporterBox.setEnabled(editable);
        relatedBox.setEnabled(editable);
        intensitySpinner.setEnabled(editable);
        intensityThresholdCmb.setEnabled(editable);
        accuracySpinner.setEnabled(editable);
        highResolutionBox.setEnabled(editable);
    }

    /**
     * Populates the GUI using the given annotation settings.
     *
     * @param annotationSettings the annotation settings to display
     * @param possibleNeutralLosses the possible neutral losses
     */
    private void populateGui(AnnotationSettings annotationSettings, ArrayList<NeutralLoss> possibleNeutralLosses) {

        neutralLossesMap = new HashMap<>(possibleNeutralLosses.size()); // @TODO: should not use NeutralLoss as key?
        ArrayList<NeutralLoss> selectedNeutralLosses = annotationSettings.getNeutralLosses();
        for (NeutralLoss possibleNeutralLoss : possibleNeutralLosses) {
            boolean found = false;
            for (NeutralLoss selectedNeutralLoss : selectedNeutralLosses) {
                if (possibleNeutralLoss.isSameAs(selectedNeutralLoss)) {
                    found = true;
                    break;
                }
            }
            neutralLossesMap.put(possibleNeutralLoss, found);
        }
        ((NeutralLossesTableModel) neutralLossesTable.getModel()).updateData();

        intensityThresholdCmb.setSelectedItem(annotationSettings.getIntensityThresholdType());
        intensitySpinner.setValue((int) (annotationSettings.getAnnotationIntensityLimit() * 100));

        double fragmentIonAccuracy = annotationSettings.getFragmentIonAccuracy();
        double stepSize;
        if (fragmentIonAccuracy > 10) { // @TODO: find a more generic way of setting the step size
            stepSize = 1;
        } else if (fragmentIonAccuracy > 1) {
            stepSize = 0.1;
        } else if (fragmentIonAccuracy > 0.1) {
            stepSize = 0.01;
        } else {
            stepSize = 0.001;
        }
        accuracySpinner.setModel(new javax.swing.SpinnerNumberModel(fragmentIonAccuracy, 0.0d, maxFragmentIonAccuracy, stepSize));
        if (annotationSettings.isFragmentIonPpm()) {
            fragmentIonAccuracyTypeLabel.setText("ppm");
        } else {
            fragmentIonAccuracyTypeLabel.setText("Da");
        }

        aBox.setSelected(false);
        bBox.setSelected(false);
        cBox.setSelected(false);
        xBox.setSelected(false);
        yBox.setSelected(false);
        zBox.setSelected(false);
        precursorBox.setSelected(false);
        immoniumBox.setSelected(false);
        reporterBox.setSelected(annotationSettings.getReporterIons());
        reporterBox.setSelected(false);

        for (IonType ionType : annotationSettings.getIonTypes().keySet()) {
            if (null != ionType) {
                switch (ionType) {
                    case IMMONIUM_ION:
                        immoniumBox.setSelected(true);
                        break;
                    case PEPTIDE_FRAGMENT_ION:
                        for (int subType : annotationSettings.getIonTypes().get(ionType)) {
                            switch (subType) {
                                case PeptideFragmentIon.A_ION:
                                    aBox.setSelected(true);
                                    break;
                                case PeptideFragmentIon.B_ION:
                                    bBox.setSelected(true);
                                    break;
                                case PeptideFragmentIon.C_ION:
                                    cBox.setSelected(true);
                                    break;
                                case PeptideFragmentIon.X_ION:
                                    xBox.setSelected(true);
                                    break;
                                case PeptideFragmentIon.Y_ION:
                                    yBox.setSelected(true);
                                    break;
                                case PeptideFragmentIon.Z_ION:
                                    zBox.setSelected(true);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case PRECURSOR_ION:
                        precursorBox.setSelected(true);
                        break;
                    case REPORTER_ION:
                        reporterBox.setSelected(true);
                        break;
                    case RELATED_ION:
                        relatedBox.setSelected(true);
                        break;
                    default:
                        break;
                }
            }
        }

        highResolutionBox.setSelected(annotationSettings.getTiesResolution() == SpectrumAnnotator.TiesResolution.mostAccurateMz); //@TODO: change for a drop down menu
    }

    /**
     * Indicates whether the user canceled the editing.
     *
     * @return a boolean indicating whether the user canceled the editing
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the annotation settings as set by the user.
     *
     * @return the annotation settings as set by the user
     */
    public AnnotationSettings getAnnotationSettings() {

        AnnotationSettings annotationSettings = new AnnotationSettings();

        if (aBox.isSelected()) {
            annotationSettings.addIonType(IonType.PEPTIDE_FRAGMENT_ION, PeptideFragmentIon.A_ION);
            annotationSettings.addIonType(IonType.TAG_FRAGMENT_ION, TagFragmentIon.A_ION);
        }
        if (bBox.isSelected()) {
            annotationSettings.addIonType(IonType.PEPTIDE_FRAGMENT_ION, PeptideFragmentIon.B_ION);
            annotationSettings.addIonType(IonType.TAG_FRAGMENT_ION, TagFragmentIon.B_ION);
        }
        if (cBox.isSelected()) {
            annotationSettings.addIonType(IonType.PEPTIDE_FRAGMENT_ION, PeptideFragmentIon.C_ION);
            annotationSettings.addIonType(IonType.TAG_FRAGMENT_ION, TagFragmentIon.C_ION);
        }
        if (xBox.isSelected()) {
            annotationSettings.addIonType(IonType.PEPTIDE_FRAGMENT_ION, PeptideFragmentIon.X_ION);
            annotationSettings.addIonType(IonType.TAG_FRAGMENT_ION, TagFragmentIon.X_ION);
        }
        if (yBox.isSelected()) {
            annotationSettings.addIonType(IonType.PEPTIDE_FRAGMENT_ION, PeptideFragmentIon.Y_ION);
            annotationSettings.addIonType(IonType.TAG_FRAGMENT_ION, TagFragmentIon.Y_ION);
        }
        if (zBox.isSelected()) {
            annotationSettings.addIonType(IonType.PEPTIDE_FRAGMENT_ION, PeptideFragmentIon.Z_ION);
            annotationSettings.addIonType(IonType.TAG_FRAGMENT_ION, TagFragmentIon.Z_ION);
        }
        if (precursorBox.isSelected()) {
            annotationSettings.addIonType(IonType.PRECURSOR_ION);
        }
        if (immoniumBox.isSelected()) {
            annotationSettings.addIonType(IonType.IMMONIUM_ION);
        }
        if (reporterBox.isSelected()) {
            for (Integer reporterIonSubType : reporterIons) {
                annotationSettings.addIonType(IonType.REPORTER_ION, reporterIonSubType);
            }
        }
        if (relatedBox.isSelected()) {
            annotationSettings.addIonType(IonType.RELATED_ION);
        }

        annotationSettings.setIntensityThresholdType((AnnotationSettings.IntensityThresholdType) intensityThresholdCmb.getSelectedItem());
        annotationSettings.setIntensityLimit(((Integer) intensitySpinner.getValue()) / 100.0);
        annotationSettings.setFragmentIonAccuracy((Double) accuracySpinner.getValue());
        SpectrumAnnotator.TiesResolution tiesResolution = highResolutionBox.isSelected() ? SpectrumAnnotator.TiesResolution.mostAccurateMz : SpectrumAnnotator.TiesResolution.mostIntense;
        annotationSettings.setTiesResolution(tiesResolution); //@TODO: replace by a drop down menu

        for (NeutralLoss neutralLoss : neutralLossesMap.keySet()) {
            if (neutralLossesMap.get(neutralLoss)) {
                annotationSettings.addNeutralLoss(neutralLoss);
            }
        }

        return annotationSettings;
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
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        annotationPreferencesHelpJButton = new javax.swing.JButton();
        ionsPanel = new javax.swing.JPanel();
        aBox = new javax.swing.JCheckBox();
        bBox = new javax.swing.JCheckBox();
        cBox = new javax.swing.JCheckBox();
        xBox = new javax.swing.JCheckBox();
        yBox = new javax.swing.JCheckBox();
        zBox = new javax.swing.JCheckBox();
        precursorBox = new javax.swing.JCheckBox();
        immoniumBox = new javax.swing.JCheckBox();
        reporterBox = new javax.swing.JCheckBox();
        relatedBox = new javax.swing.JCheckBox();
        neutralLossPanel = new javax.swing.JPanel();
        neutralLossScrollPane = new javax.swing.JScrollPane();
        neutralLossesTable = new javax.swing.JTable();
        peakMatchingPanel = new javax.swing.JPanel();
        fragmentIonAccuracyLabel = new javax.swing.JLabel();
        fragmentIonAccuracyTypeLabel = new javax.swing.JLabel();
        intensitySpinner = new javax.swing.JSpinner();
        annotationLevelPercentLabel = new javax.swing.JLabel();
        annotationLevelLabel = new javax.swing.JLabel();
        accuracySpinner = new javax.swing.JSpinner();
        highResolutionBox = new javax.swing.JCheckBox();
        intensityThresholdCmb = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Spectrum Annotation");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        annotationPreferencesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        annotationPreferencesHelpJButton.setToolTipText("Help");
        annotationPreferencesHelpJButton.setBorder(null);
        annotationPreferencesHelpJButton.setBorderPainted(false);
        annotationPreferencesHelpJButton.setContentAreaFilled(false);
        annotationPreferencesHelpJButton.setFocusable(false);
        annotationPreferencesHelpJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        annotationPreferencesHelpJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        annotationPreferencesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                annotationPreferencesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                annotationPreferencesHelpJButtonMouseExited(evt);
            }
        });
        annotationPreferencesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotationPreferencesHelpJButtonActionPerformed(evt);
            }
        });

        ionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Ion Types"));
        ionsPanel.setOpaque(false);

        aBox.setText("a-ion");
        aBox.setIconTextGap(10);

        bBox.setText("b-ion");
        bBox.setIconTextGap(10);

        cBox.setText("c-ion");
        cBox.setIconTextGap(10);

        xBox.setText("x-ion");
        xBox.setIconTextGap(10);

        yBox.setText("y-ion");
        yBox.setIconTextGap(10);

        zBox.setText("z-ion");
        zBox.setIconTextGap(10);

        precursorBox.setText("Precursor");
        precursorBox.setToolTipText("Precursor ions");
        precursorBox.setIconTextGap(10);

        immoniumBox.setText("Immonium");
        immoniumBox.setToolTipText("Immonium ions");
        immoniumBox.setIconTextGap(10);

        reporterBox.setText("Reporter");
        reporterBox.setToolTipText("Report ions");
        reporterBox.setIconTextGap(10);

        relatedBox.setText("Related");
        relatedBox.setToolTipText("Related ions");
        relatedBox.setIconTextGap(10);

        javax.swing.GroupLayout ionsPanelLayout = new javax.swing.GroupLayout(ionsPanel);
        ionsPanel.setLayout(ionsPanelLayout);
        ionsPanelLayout.setHorizontalGroup(
            ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ionsPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aBox)
                    .addComponent(bBox)
                    .addComponent(cBox))
                .addGap(50, 50, 50)
                .addGroup(ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yBox)
                    .addComponent(xBox)
                    .addComponent(zBox))
                .addGap(50, 50, 50)
                .addGroup(ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reporterBox)
                    .addComponent(immoniumBox)
                    .addGroup(ionsPanelLayout.createSequentialGroup()
                        .addComponent(precursorBox)
                        .addGap(50, 50, 50)
                        .addComponent(relatedBox)))
                .addGap(25, 25, 25))
        );

        ionsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {aBox, bBox, cBox, immoniumBox, precursorBox, xBox, yBox, zBox});

        ionsPanelLayout.setVerticalGroup(
            ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aBox)
                    .addComponent(xBox)
                    .addComponent(precursorBox)
                    .addComponent(relatedBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bBox)
                    .addComponent(yBox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(immoniumBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cBox)
                    .addComponent(zBox)
                    .addComponent(reporterBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        neutralLossPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Neutral Loss"));
        neutralLossPanel.setOpaque(false);

        neutralLossesTable.setModel(new NeutralLossesTableModel());
        neutralLossesTable.setOpaque(false);
        neutralLossScrollPane.setViewportView(neutralLossesTable);

        javax.swing.GroupLayout neutralLossPanelLayout = new javax.swing.GroupLayout(neutralLossPanel);
        neutralLossPanel.setLayout(neutralLossPanelLayout);
        neutralLossPanelLayout.setHorizontalGroup(
            neutralLossPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(neutralLossPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(neutralLossScrollPane)
                .addContainerGap())
        );
        neutralLossPanelLayout.setVerticalGroup(
            neutralLossPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(neutralLossPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(neutralLossScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addContainerGap())
        );

        peakMatchingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peak Matching"));
        peakMatchingPanel.setOpaque(false);

        fragmentIonAccuracyLabel.setText("Fragment Ion Accuracy");
        fragmentIonAccuracyLabel.setToolTipText("Fragment ion annotation accuracy ");

        fragmentIonAccuracyTypeLabel.setText("Da");

        intensitySpinner.setModel(new javax.swing.SpinnerNumberModel(25, 0, 100, 1));
        intensitySpinner.setToolTipText("<html>\nDisplay a certain percent of the<br>\npossible annotations relative<br>\nto the most intense peak\n</html>");

        annotationLevelPercentLabel.setText("%");

        annotationLevelLabel.setText("Annotation Level");
        annotationLevelLabel.setToolTipText("<html>\nDisplay a certain percent of the<br>\npossible annotations relative<br>\nto the most intense peak\n</html>");

        accuracySpinner.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.0d, 0.05d, 0.001d));
        accuracySpinner.setToolTipText("Fragment ion annotation accuracy");

        highResolutionBox.setSelected(true);
        highResolutionBox.setText("High Resolution");
        highResolutionBox.setIconTextGap(10);

        intensityThresholdCmb.setModel(new DefaultComboBoxModel(AnnotationSettings.IntensityThresholdType.values()));

        javax.swing.GroupLayout peakMatchingPanelLayout = new javax.swing.GroupLayout(peakMatchingPanel);
        peakMatchingPanel.setLayout(peakMatchingPanelLayout);
        peakMatchingPanelLayout.setHorizontalGroup(
            peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peakMatchingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fragmentIonAccuracyLabel)
                    .addComponent(annotationLevelLabel))
                .addGap(18, 18, 18)
                .addGroup(peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(intensitySpinner)
                    .addComponent(accuracySpinner))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fragmentIonAccuracyTypeLabel)
                    .addComponent(annotationLevelPercentLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(35, 35, 35)
                .addGroup(peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(peakMatchingPanelLayout.createSequentialGroup()
                        .addComponent(highResolutionBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(intensityThresholdCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        peakMatchingPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {annotationLevelPercentLabel, fragmentIonAccuracyTypeLabel});

        peakMatchingPanelLayout.setVerticalGroup(
            peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peakMatchingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(annotationLevelLabel)
                    .addComponent(intensitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(annotationLevelPercentLabel)
                    .addComponent(intensityThresholdCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(peakMatchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fragmentIonAccuracyLabel)
                    .addComponent(accuracySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentIonAccuracyTypeLabel)
                    .addComponent(highResolutionBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(annotationPreferencesHelpJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(peakMatchingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(neutralLossPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(neutralLossPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peakMatchingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                    .addComponent(annotationPreferencesHelpJButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
     * Open the help dialog.
     *
     * @param evt
     */
    private void annotationPreferencesHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotationPreferencesHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(parentFrame, getClass().getResource("/helpFiles/AnnotationPreferences.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
                "Spectrum Annotation - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_annotationPreferencesHelpJButtonActionPerformed

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void annotationPreferencesHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_annotationPreferencesHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_annotationPreferencesHelpJButtonMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void annotationPreferencesHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_annotationPreferencesHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_annotationPreferencesHelpJButtonMouseEntered

    /**
     * Close the dialog and update the spectrum annotations.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Closes the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Closes the dialog without saving.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        canceled = true;
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox aBox;
    private javax.swing.JSpinner accuracySpinner;
    private javax.swing.JLabel annotationLevelLabel;
    private javax.swing.JLabel annotationLevelPercentLabel;
    private javax.swing.JButton annotationPreferencesHelpJButton;
    private javax.swing.JCheckBox bBox;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JCheckBox cBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel fragmentIonAccuracyLabel;
    private javax.swing.JLabel fragmentIonAccuracyTypeLabel;
    private javax.swing.JCheckBox highResolutionBox;
    private javax.swing.JCheckBox immoniumBox;
    private javax.swing.JSpinner intensitySpinner;
    private javax.swing.JComboBox intensityThresholdCmb;
    private javax.swing.JPanel ionsPanel;
    private javax.swing.JPanel neutralLossPanel;
    private javax.swing.JScrollPane neutralLossScrollPane;
    private javax.swing.JTable neutralLossesTable;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel peakMatchingPanel;
    private javax.swing.JCheckBox precursorBox;
    private javax.swing.JCheckBox relatedBox;
    private javax.swing.JCheckBox reporterBox;
    private javax.swing.JCheckBox xBox;
    private javax.swing.JCheckBox yBox;
    private javax.swing.JCheckBox zBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Table model for the neutral losses table.
     */
    private class NeutralLossesTableModel extends DefaultTableModel {

        /**
         * Name to neutral loss map.
         */
        private HashMap<String, NeutralLoss> namesMap = new HashMap<>();

        /**
         * List of the names of the neutral losses to display.
         */
        private ArrayList<String> namesList = new ArrayList<>();

        /**
         * Constructor.
         */
        public NeutralLossesTableModel() {
            updateData();
        }

        /**
         * Update the table content.
         */
        public void updateData() {
            if (neutralLossesMap != null) {
                for (NeutralLoss neutralLoss : neutralLossesMap.keySet()) {
                    namesMap.put(neutralLoss.name, neutralLoss);
                }
                namesList = new ArrayList<>(namesMap.keySet());
                Collections.sort(namesList);
            }
        }

        @Override
        public int getRowCount() {
            if (namesList == null) {
                return 0;
            }
            return namesList.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Neutral Loss";
                case 2:
                    return "  ";
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
                    return namesList.get(row);
                case 2:
                    return neutralLossesMap.get(namesMap.get(namesList.get(row)));
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
            return columnIndex == 2 && editable;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            NeutralLoss neutralLoss = namesMap.get(namesList.get(row));
            neutralLossesMap.put(neutralLoss, !neutralLossesMap.get(neutralLoss));
        }
    }
}
