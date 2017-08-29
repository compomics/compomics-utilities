package com.compomics.util.gui.parameters.identification;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.ions.IonFactory;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationParametersFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.gui.parameters.identification.subparameters.AnnotationParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.FractionParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.GeneParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.MatchesImportParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.ModificationLocalizationParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.PeptideVariantsParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.ProteinInferenceParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.PsmScoringParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.SearchParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.SequenceMatchingParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.ValidationQCParametersDialog;
import com.compomics.util.gui.parameters.identification.subparameters.ValidationParametersDialog;
import com.compomics.util.parameters.identification.FractionParameters;
import com.compomics.util.parameters.identification.GeneParameters;
import com.compomics.util.parameters.identification.IdMatchValidationParameters;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.io.file.LastSelectedFolder;
import com.compomics.util.parameters.identification.ModificationLocalizationParameters;
import com.compomics.util.parameters.identification.PeptideVariantsParameters;
import com.compomics.util.parameters.identification.ProteinInferenceParameters;
import com.compomics.util.parameters.identification.PsmScoringParameters;
import com.compomics.util.parameters.identification.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.ValidationQcParameters;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import com.compomics.util.gui.parameters.identification.subparameters.ValidationQCParametersDialogParent;

/**
 * IdentificationParametersEditionDialog.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IdentificationParametersEditionDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * The normal icon.
     */
    private Image normalIcon;
    /**
     * The waiting icon.
     */
    private Image waitingIcon;
    /**
     * The last selected folder
     */
    private LastSelectedFolder lastSelectedFolder;
    /**
     * Boolean indicating whether the parameters can be edited.
     */
    private boolean editable;
    /**
     * The peak annotation settings.
     */
    private AnnotationSettings annotationSettings;
    /**
     * The parameters used for the spectrum matching.
     */
    private SearchParameters searchParameters;
    /**
     * The peptide to protein matching preferences.
     */
    private SequenceMatchingParameters sequenceMatchingPreferences;
    /**
     * The peptide variants preferences.
     */
    private PeptideVariantsParameters peptideVariantsPreferences;
    /**
     * The gene preferences.
     */
    private GeneParameters genePreferences;
    /**
     * The PSM scores to use.
     */
    private PsmScoringParameters psmScoringPreferences;
    /**
     * The PSM filter.
     */
    private PeptideAssumptionFilter peptideAssumptionFilter = new PeptideAssumptionFilter();
    /**
     * The PTM localization scoring preferences.
     */
    private ModificationLocalizationParameters ptmScoringPreferences = new ModificationLocalizationParameters();
    /**
     * The protein inference preferences.
     */
    private ProteinInferenceParameters proteinInferencePreferences;
    /**
     * The identification validation preferences.
     */
    private IdMatchValidationParameters idValidationPreferences = new IdMatchValidationParameters();
    /**
     * The fraction settings.
     */
    private FractionParameters fractionSettings;
    /**
     * A parent handling the edition of QC filters.
     */
    private ValidationQCParametersDialogParent validationQCPreferencesDialogParent;
    /**
     * If yes, the advanced settings are shown.
     */
    private boolean showAdvancedSettings = false;
    /**
     * The identification parameters factory.
     */
    private IdentificationParametersFactory identificationParametersFactory = IdentificationParametersFactory.getInstance();
    /**
     * The old identification settings.
     */
    private IdentificationParameters oldIdentificationParameters;

    /**
     * Creates a new IdentificationParametersEditionDialog with a frame as
     * owner.
     *
     * @param parentFrame the parent frame
     * @param identificationParameters the identification parameters to display
     * @param configurationFile the configuration file containing the PTM usage
     * preferences
     * @param normalIcon the normal icon
     * @param waitingIcon the waiting icon
     * @param lastSelectedFolder the last selected folder
     * @param validationQCPreferencesDialogParent a parent handling the edition
     * of QC filters
     * @param editable boolean indicating whether the parameters can be edited
     */
    public IdentificationParametersEditionDialog(java.awt.Frame parentFrame, IdentificationParameters identificationParameters,
            ConfigurationFile configurationFile, Image normalIcon, Image waitingIcon, LastSelectedFolder lastSelectedFolder,
            ValidationQCParametersDialogParent validationQCPreferencesDialogParent, boolean editable) {
        super(parentFrame, true);

        this.parentFrame = parentFrame;
        this.oldIdentificationParameters = identificationParameters;

        if (identificationParameters != null) {
            extractParameters(identificationParameters);
        }

        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.configurationFile = configurationFile;
        this.lastSelectedFolder = lastSelectedFolder;
        this.validationQCPreferencesDialogParent = validationQCPreferencesDialogParent;
        this.editable = editable;

        initComponents();
        setUpGui();
        if (identificationParameters != null) {
            nameTxt.setText(identificationParameters.getName());
            updateGUI();
        } else {
            clearGUI();
            saveButton.setEnabled(false);
        }
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new IdentificationParametersEditionDialog with a dialog as
     * owner.
     *
     * @param owner the dialog owner
     * @param parentFrame the parent frame
     * @param identificationParameters the identification parameters to display
     * @param configurationFile the configuration file containing the PTM usage
     * preferences
     * @param normalIcon the normal icon
     * @param waitingIcon the waiting icon
     * @param lastSelectedFolder the last selected folder
     * @param validationQCPreferencesDialogParent a parent handling the edition
     * of QC filters
     * @param editable boolean indicating whether the parameters can be edited
     */
    public IdentificationParametersEditionDialog(Dialog owner, java.awt.Frame parentFrame, IdentificationParameters identificationParameters,
            ConfigurationFile configurationFile, Image normalIcon, Image waitingIcon, LastSelectedFolder lastSelectedFolder,
            ValidationQCParametersDialogParent validationQCPreferencesDialogParent, boolean editable) {
        super(owner, true);

        this.parentFrame = parentFrame;
        this.oldIdentificationParameters = identificationParameters;

        if (identificationParameters != null) {
            extractParameters(identificationParameters);
        }

        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.configurationFile = configurationFile;
        this.lastSelectedFolder = lastSelectedFolder;
        this.validationQCPreferencesDialogParent = validationQCPreferencesDialogParent;
        this.editable = editable;

        initComponents();
        setUpGui();
        if (identificationParameters != null) {
            nameTxt.setText(identificationParameters.getName());
            updateGUI();
        } else {
            clearGUI();
            saveButton.setEnabled(false);
        }
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {
        nameTxt.setEditable(editable);
        updateAdvancedSettings();
    }

    /**
     * Extracts all the parameters.
     *
     * @param identificationParameters
     */
    private void extractParameters(IdentificationParameters identificationParameters) {
        annotationSettings = identificationParameters.getAnnotationPreferences();
        searchParameters = identificationParameters.getSearchParameters();
        sequenceMatchingPreferences = identificationParameters.getSequenceMatchingPreferences();
        peptideVariantsPreferences = identificationParameters.getPeptideVariantsPreferences();
        genePreferences = identificationParameters.getGenePreferences();
        psmScoringPreferences = identificationParameters.getPsmScoringPreferences();
        peptideAssumptionFilter = identificationParameters.getPeptideAssumptionFilter();
        ptmScoringPreferences = identificationParameters.getPtmScoringPreferences();
        proteinInferencePreferences = identificationParameters.getProteinInferencePreferences();
        if (proteinInferencePreferences.getProteinSequenceDatabase() == null && searchParameters.getFastaFile() != null) {
            proteinInferencePreferences.setProteinSequenceDatabase(searchParameters.getFastaFile());
        }
        idValidationPreferences = identificationParameters.getIdValidationPreferences();
        fractionSettings = identificationParameters.getFractionSettings();
    }

    /**
     * Populates the GUI using the given identification parameters.
     */
    public void updateGUI() {

        // show the parameter details
        int columnWidth = 150;
        int maxDescriptionLength = 150;

        spectrumMatchingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Spectrum Matching</b></td>"
                + "<td><font size=2>" + formatDescription(searchParameters.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        geneMappingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Gene Annotation</b></td>"
                + "<td><font size=2>" + formatDescription(genePreferences.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        spectrumAnnotationButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Spectrum Annotation</b></td>"
                + "<td><font size=2>" + formatDescription(annotationSettings.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        sequenceMatchingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Sequence Matching</b></td>"
                + "<td><font size=2>" + formatDescription(sequenceMatchingPreferences.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        peptideVariantsButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Peptide Variants</b></td>"
                + "<td><font size=2>" + formatDescription(peptideVariantsPreferences.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        matchesFiltersButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Import Filters</b></td>"
                + "<td><font size=2>" + formatDescription(peptideAssumptionFilter.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        psmScoringButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>PSM Scoring</b></td>"
                + "<td><font size=2>" + formatDescription(psmScoringPreferences.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        ptmLocalizationButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>PTM Localization</b></td>"
                + "<td><font size=2>" + formatDescription(ptmScoringPreferences.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        proteinInferenceButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Protein Inference</b></td>"
                + "<td><font size=2>" + formatDescription(proteinInferencePreferences.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        validationButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Validation Levels</b></td>"
                + "<td><font size=2>" + formatDescription(idValidationPreferences.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        fractionsButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Fraction Analysis</b></td>"
                + "<td><font size=2>" + formatDescription(fractionSettings.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        qualityControlButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Quality Control</b></td>"
                + "<td><font size=2>" + formatDescription(idValidationPreferences.getValidationQCPreferences().getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");

        geneMappingButton.setEnabled(true);
        spectrumAnnotationButton.setEnabled(true);
        sequenceMatchingButton.setEnabled(true);
        matchesFiltersButton.setEnabled(true);
        psmScoringButton.setEnabled(true);
        ptmLocalizationButton.setEnabled(true);
        proteinInferenceButton.setEnabled(true);
        validationButton.setEnabled(true);
        fractionsButton.setEnabled(true);
        qualityControlButton.setEnabled(true);
//        peptideVariantsButton.setEnabled(sequenceMatchingPreferences.getPeptideMapperType() == PeptideMapperType.fm_index);
        peptideVariantsButton.setEnabled(false);

        validateInput();
    }

    /**
     * Clear the settings.
     */
    private void clearGUI() {
        nameTxt.setText(null);

        // show the parameter details
        int columnWidth = 150;

        spectrumMatchingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Spectrum Matching</b></td>"
                + "<td><font size=2>" + "-- please click to edit --" + "</font></td></tr></table></html>");

        geneMappingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Gene Annotation</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        spectrumAnnotationButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Spectrum Annotation</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        sequenceMatchingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Sequence Matching</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        peptideVariantsButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Peptide Variants</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        matchesFiltersButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Import Filters</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        psmScoringButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>PSM Scoring</b></td>"
                + "<td><font size=2>" + "(not yet available)" + "</font></td></tr></table></html>"); // @TODO: remove when the psm scoring settings are added!

        ptmLocalizationButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>PTM Localization</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        proteinInferenceButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Protein Inference</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        validationButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Validation Levels</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        fractionsButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Fraction Analysis</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        qualityControlButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Quality Control</b></td>"
                + "<td><font size=2></font></td></tr></table></html>");

        geneMappingButton.setEnabled(false);
        spectrumAnnotationButton.setEnabled(false);
        sequenceMatchingButton.setEnabled(false);
        peptideVariantsButton.setEnabled(false);
        matchesFiltersButton.setEnabled(false);
        psmScoringButton.setEnabled(false);
        ptmLocalizationButton.setEnabled(false);
        proteinInferenceButton.setEnabled(false);
        validationButton.setEnabled(false);
        fractionsButton.setEnabled(false);
        qualityControlButton.setEnabled(false);

        validateInput();
    }

    /**
     * Make sure that the parameter description is not too long.
     *
     * @param description original description
     * @param maxDescriptionLength max number of characters
     * @return the new description
     */
    private String formatDescription(String description, int maxDescriptionLength) {
        if (description.length() > maxDescriptionLength) {
            description = description.substring(0, maxDescriptionLength) + "...";
        }
        return description;
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
     * Returns the identification parameters as set by the user.
     *
     * @return the identification parameters as set by the user
     */
    public IdentificationParameters getIdentificationParameters() {
        IdentificationParameters identificationParameters = new IdentificationParameters();
        identificationParameters.setName(nameTxt.getText());
        identificationParameters.setDescription(searchParameters.getShortDescription(), true);
        identificationParameters.setAnnotationSettings(annotationSettings);
        identificationParameters.setSearchParameters(searchParameters);
        identificationParameters.setSequenceMatchingPreferences(sequenceMatchingPreferences);
        identificationParameters.setGenePreferences(genePreferences);
        identificationParameters.setIdFilter(peptideAssumptionFilter);
        identificationParameters.setPsmScoringPreferences(psmScoringPreferences);
        identificationParameters.setPtmScoringPreferences(ptmScoringPreferences);
        identificationParameters.setPeptideVariantsPreferences(peptideVariantsPreferences);
        identificationParameters.setProteinInferencePreferences(proteinInferencePreferences);
        identificationParameters.setIdValidationPreferences(idValidationPreferences);
        identificationParameters.setFractionSettings(fractionSettings);
        return identificationParameters;
    }

    /**
     * Validates the user input.
     *
     * @return a boolean indicating whether the user input is valid
     */
    public boolean validateInput() {

        boolean valid = true;

        if (nameTxt.getText().isEmpty()) {
            valid = false;
        }

        if (valid) {
            String name = nameTxt.getText();
            for (char character : name.toCharArray()) {
                String charAsString = character + "";
                if (charAsString.matches("[^\\dA-Za-z _-]")) {
                    JOptionPane.showMessageDialog(this, "Unsupported character in parameters name (" + character + "). Please avoid special characters in parameters name.",
                            "Special Character", JOptionPane.INFORMATION_MESSAGE);
                    valid = false;
                }
            }
        }

        if (valid) {
            nameLabel.setForeground(Color.BLACK);
            nameLabel.setToolTipText(null);
            nameTxt.setToolTipText(null);
        } else {
            nameLabel.setForeground(Color.RED);
            nameLabel.setToolTipText("Please provide a settings file name");
            nameTxt.setToolTipText("Please provide a settings file name");
        }

        if (valid) {
            valid = validateParametersInput(false);
        }

        saveButton.setEnabled(valid);

        if (searchParameters != null) {
            IdentificationParameters currentParameters = getIdentificationParameters();

            if (!identificationParametersFactory.getParametersList().contains(currentParameters.getName())
                    || (identificationParametersFactory.getParametersList().contains(currentParameters.getName())
                    && !identificationParametersFactory.getIdentificationParameters(currentParameters.getName()).equals(currentParameters))) {
                saveButton.setText("Save");
            } else {
                saveButton.setText("OK");
            }
        }

        return valid;
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
        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        settingsOuterPanel = new javax.swing.JPanel();
        attributesPanel = new javax.swing.JPanel();
        nameTxt = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        settingsPanel = new javax.swing.JPanel();
        validationButton = new javax.swing.JButton();
        qualityControlButton = new javax.swing.JButton();
        sequenceMatchingButton = new javax.swing.JButton();
        spectrumAnnotationButton = new javax.swing.JButton();
        psmScoringButton = new javax.swing.JButton();
        ptmLocalizationButton = new javax.swing.JButton();
        geneMappingButton = new javax.swing.JButton();
        spectrumMatchingButton = new javax.swing.JButton();
        proteinInferenceButton = new javax.swing.JButton();
        matchesFiltersButton = new javax.swing.JButton();
        fractionsButton = new javax.swing.JButton();
        advancedSettingsLabel = new javax.swing.JLabel();
        peptideVariantsButton = new javax.swing.JButton();
        exportLabel = new javax.swing.JLabel();
        importLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Identification Settings");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        scrollPane.setBorder(null);

        settingsOuterPanel.setBackground(new java.awt.Color(230, 230, 230));

        attributesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings Description"));
        attributesPanel.setOpaque(false);

        nameTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nameTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTxtKeyReleased(evt);
            }
        });

        nameLabel.setText("Name");

        javax.swing.GroupLayout attributesPanelLayout = new javax.swing.GroupLayout(attributesPanel);
        attributesPanel.setLayout(attributesPanelLayout);
        attributesPanelLayout.setHorizontalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addGap(18, 18, 18)
                .addComponent(nameTxt)
                .addContainerGap())
        );
        attributesPanelLayout.setVerticalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Identification Settings"));
        settingsPanel.setOpaque(false);

        validationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        validationButton.setText("Validation Levels");
        validationButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        validationButton.setIconTextGap(15);
        validationButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        validationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validationButtonActionPerformed(evt);
            }
        });

        qualityControlButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        qualityControlButton.setText("Quality Control");
        qualityControlButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        qualityControlButton.setIconTextGap(15);
        qualityControlButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        qualityControlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qualityControlButtonActionPerformed(evt);
            }
        });

        sequenceMatchingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        sequenceMatchingButton.setText("Sequence Matching");
        sequenceMatchingButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        sequenceMatchingButton.setIconTextGap(15);
        sequenceMatchingButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        sequenceMatchingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequenceMatchingButtonActionPerformed(evt);
            }
        });

        spectrumAnnotationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        spectrumAnnotationButton.setText("Spectrum Annotation");
        spectrumAnnotationButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        spectrumAnnotationButton.setIconTextGap(15);
        spectrumAnnotationButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        spectrumAnnotationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumAnnotationButtonActionPerformed(evt);
            }
        });

        psmScoringButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        psmScoringButton.setText("PSM Scoring");
        psmScoringButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        psmScoringButton.setIconTextGap(15);
        psmScoringButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        psmScoringButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psmScoringButtonActionPerformed(evt);
            }
        });

        ptmLocalizationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        ptmLocalizationButton.setText("PTM Localization");
        ptmLocalizationButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ptmLocalizationButton.setIconTextGap(15);
        ptmLocalizationButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        ptmLocalizationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ptmLocalizationButtonActionPerformed(evt);
            }
        });

        geneMappingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        geneMappingButton.setText("Gene Annotation");
        geneMappingButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        geneMappingButton.setIconTextGap(15);
        geneMappingButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        geneMappingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneMappingButtonActionPerformed(evt);
            }
        });

        spectrumMatchingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        spectrumMatchingButton.setText("Spectrum Matching");
        spectrumMatchingButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        spectrumMatchingButton.setIconTextGap(15);
        spectrumMatchingButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        spectrumMatchingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectrumMatchingButtonActionPerformed(evt);
            }
        });

        proteinInferenceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        proteinInferenceButton.setText("Protein Inference");
        proteinInferenceButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        proteinInferenceButton.setIconTextGap(15);
        proteinInferenceButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        proteinInferenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proteinInferenceButtonActionPerformed(evt);
            }
        });

        matchesFiltersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        matchesFiltersButton.setText("Import Filters");
        matchesFiltersButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        matchesFiltersButton.setIconTextGap(15);
        matchesFiltersButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        matchesFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchesFiltersButtonActionPerformed(evt);
            }
        });

        fractionsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        fractionsButton.setText("Fraction Analysis");
        fractionsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        fractionsButton.setIconTextGap(15);
        fractionsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        fractionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fractionsButtonActionPerformed(evt);
            }
        });

        advancedSettingsLabel.setText("<html><a href>Hide Advanced Settings</a></html>");
        advancedSettingsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                advancedSettingsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                advancedSettingsLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                advancedSettingsLabelMouseReleased(evt);
            }
        });

        peptideVariantsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        peptideVariantsButton.setText("Peptide Variants");
        peptideVariantsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        peptideVariantsButton.setIconTextGap(15);
        peptideVariantsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        peptideVariantsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                peptideVariantsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(advancedSettingsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(spectrumAnnotationButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sequenceMatchingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(matchesFiltersButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(spectrumMatchingButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(psmScoringButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ptmLocalizationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(proteinInferenceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(validationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fractionsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(qualityControlButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(geneMappingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideVariantsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(spectrumMatchingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(advancedSettingsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(spectrumAnnotationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(sequenceMatchingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(peptideVariantsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(matchesFiltersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(psmScoringButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(ptmLocalizationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(geneMappingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(proteinInferenceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(validationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(fractionsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(qualityControlButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout settingsOuterPanelLayout = new javax.swing.GroupLayout(settingsOuterPanel);
        settingsOuterPanel.setLayout(settingsOuterPanelLayout);
        settingsOuterPanelLayout.setHorizontalGroup(
            settingsOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsOuterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attributesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsOuterPanelLayout.setVerticalGroup(
            settingsOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsOuterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        scrollPane.setViewportView(settingsOuterPanel);

        exportLabel.setText("<html> <a href>Export to File</a> </html>");
        exportLabel.setToolTipText("Export the identification settings to a file");
        exportLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                exportLabelMouseReleased(evt);
            }
        });

        importLabel.setText("<html> <a href>Import from File</a> </html>");
        importLabel.setToolTipText("Import identification settings from a file");
        importLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                importLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                importLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                importLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(exportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(importLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
            .addComponent(scrollPane)
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, saveButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addComponent(scrollPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(saveButton)
                    .addComponent(exportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(importLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
     * Open the AnnotationSettingsDialog.
     *
     * @param evt
     */
    private void spectrumAnnotationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumAnnotationButtonActionPerformed
        ArrayList<NeutralLoss> neutralLosses = IonFactory.getNeutralLosses(searchParameters.getPtmSettings());
        ArrayList<Integer> reporterIons = new ArrayList<>(IonFactory.getReporterIons(searchParameters.getPtmSettings()));
        AnnotationParametersDialog annotationSettingsDialog = new AnnotationParametersDialog(this, parentFrame, annotationSettings,
                searchParameters.getFragmentIonAccuracy(), neutralLosses, reporterIons, editable);
        if (!annotationSettingsDialog.isCanceled()) {
            annotationSettings = annotationSettingsDialog.getAnnotationSettings();
            updateGUI();
        }
    }//GEN-LAST:event_spectrumAnnotationButtonActionPerformed

    /**
     * Open the SearchSettingsDialog.
     *
     * @param evt
     */
    private void spectrumMatchingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectrumMatchingButtonActionPerformed
        String name = nameTxt.getText();
        SearchParametersDialog searchSettingsDialog = new SearchParametersDialog(this, parentFrame, searchParameters,
                normalIcon, waitingIcon, true, true, configurationFile, lastSelectedFolder, name, editable);
        if (!searchSettingsDialog.isCanceled()) {
            SearchParameters oldSearchParameters = searchParameters;
            searchParameters = searchSettingsDialog.getSearchParameters();

            boolean extactParameters = false;
            if (oldSearchParameters == null) {
                extactParameters = true;
            } else if (!searchParameters.equals(oldSearchParameters)) {
                // @TODO: check if the question is really needed..?
                int value = JOptionPane.showConfirmDialog(this, "Spectrum matching settings changed. Update advanced settings accordingly?", "Update Advanced Parameters?", JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.YES_OPTION) {
                    extactParameters = true;
                }
            }

            if (extactParameters) {
                IdentificationParameters identificationParameters = new IdentificationParameters(searchParameters);
                extractParameters(identificationParameters);
            }

            if (!nameTxt.getText().isEmpty()) {
                saveButton.setEnabled(true);
            }

            updateGUI();
            validateInput();
        }
    }//GEN-LAST:event_spectrumMatchingButtonActionPerformed

    /**
     * Open the SequenceMatchingSettingsDialog.
     *
     * @param evt
     */
    private void sequenceMatchingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequenceMatchingButtonActionPerformed
        SequenceMatchingParametersDialog sequenceMatchingSettingsDialog = new SequenceMatchingParametersDialog(this, parentFrame, sequenceMatchingPreferences, editable);
        if (!sequenceMatchingSettingsDialog.isCanceled()) {
            sequenceMatchingPreferences = sequenceMatchingSettingsDialog.getSequenceMatchingPreferences();
            updateGUI();
        }
    }//GEN-LAST:event_sequenceMatchingButtonActionPerformed

    /**
     * Open the SpeciesDialog.
     *
     * @param evt
     */
    private void geneMappingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneMappingButtonActionPerformed
        GeneParametersDialog genePreferencesDialog = new GeneParametersDialog(this, parentFrame, genePreferences, searchParameters, editable);
        if (!genePreferencesDialog.isCanceled()) {
            genePreferences = genePreferencesDialog.getGenePreferences();
            updateGUI();
        }
    }//GEN-LAST:event_geneMappingButtonActionPerformed

    /**
     * Open the MatchesImportFiltersDialog.
     *
     * @param evt
     */
    private void matchesFiltersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchesFiltersButtonActionPerformed
        MatchesImportParametersDialog matchesImportFiltersDialog = new MatchesImportParametersDialog(this, parentFrame, peptideAssumptionFilter, editable);
        if (!matchesImportFiltersDialog.isCanceled()) {
            peptideAssumptionFilter = matchesImportFiltersDialog.getFilter();
            updateGUI();
        }
    }//GEN-LAST:event_matchesFiltersButtonActionPerformed

    /**
     * Open the PsmScoringSettingsDialog.
     *
     * @param evt
     */
    private void psmScoringButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_psmScoringButtonActionPerformed
        PsmScoringParametersDialog psmScoringSettingsDialog = new PsmScoringParametersDialog(this, parentFrame, psmScoringPreferences, editable);
        if (!psmScoringSettingsDialog.isCanceled()) {
            psmScoringPreferences = psmScoringSettingsDialog.getPsmScoringPreferences();
            updateGUI();
        }
    }//GEN-LAST:event_psmScoringButtonActionPerformed

    /**
     * Open the PTMLocalizationParametersDialog.
     *
     * @param evt
     */
    private void ptmLocalizationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptmLocalizationButtonActionPerformed
        ModificationLocalizationParametersDialog ptmLocalizationParametersDialog = new ModificationLocalizationParametersDialog(this, parentFrame, ptmScoringPreferences, editable);
        if (!ptmLocalizationParametersDialog.isCanceled()) {
            ptmScoringPreferences = ptmLocalizationParametersDialog.getPtmScoringPreferences();
            updateGUI();
        }
    }//GEN-LAST:event_ptmLocalizationButtonActionPerformed

    /**
     * Open the ProteinInferenceSettingsDialog.
     *
     * @param evt
     */
    private void proteinInferenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proteinInferenceButtonActionPerformed
        ProteinInferenceParametersDialog proteinInferenceSettingsDialog = new ProteinInferenceParametersDialog(this, parentFrame, proteinInferencePreferences, normalIcon, waitingIcon, lastSelectedFolder, editable);
        if (!proteinInferenceSettingsDialog.isCanceled()) {
            proteinInferencePreferences = proteinInferenceSettingsDialog.getProteinInferencePreferences();
            updateGUI();
        }
    }//GEN-LAST:event_proteinInferenceButtonActionPerformed

    /**
     * Open the ValidationSettingsDialog.
     *
     * @param evt
     */
    private void validationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationButtonActionPerformed
        ValidationParametersDialog validationSettingsDialog = new ValidationParametersDialog(this, parentFrame, idValidationPreferences, editable);
        if (!validationSettingsDialog.isCanceled()) {
            ValidationQcParameters validationQCPreferences = idValidationPreferences.getValidationQCPreferences();
            idValidationPreferences = validationSettingsDialog.getIdMatchValidationPreferences();
            idValidationPreferences.setValidationQCPreferences(validationQCPreferences);
            updateGUI();
        }
    }//GEN-LAST:event_validationButtonActionPerformed

    /**
     * Open the ValidationQCPreferencesDialog.
     *
     * @param evt
     */
    private void qualityControlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qualityControlButtonActionPerformed
        ValidationQcParameters validationQCPreferences = idValidationPreferences.getValidationQCPreferences();
        ValidationQCParametersDialog validationQCPreferencesDialog = new ValidationQCParametersDialog(this, parentFrame, validationQCPreferencesDialogParent, validationQCPreferences, editable && validationQCPreferencesDialogParent != null);
        if (!validationQCPreferencesDialog.isCanceled()) {
            idValidationPreferences = new IdMatchValidationParameters(idValidationPreferences);
            idValidationPreferences.setValidationQCPreferences(validationQCPreferencesDialog.getValidationQCPreferences());
            updateGUI();
        }
    }//GEN-LAST:event_qualityControlButtonActionPerformed

    /**
     * Open the FractionSettingsDialog.
     *
     * @param evt
     */
    private void fractionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fractionsButtonActionPerformed
        FractionParametersDialog fractionSettingsDialog = new FractionParametersDialog(this, parentFrame, fractionSettings, editable);
        if (!fractionSettingsDialog.isCanceled()) {
            fractionSettings = fractionSettingsDialog.getFractionSettings();
            updateGUI();
        }
    }//GEN-LAST:event_fractionsButtonActionPerformed

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        if (validateInput()) {

            IdentificationParameters newParameters = getIdentificationParameters();

            try {
                // check if the file already exists and the settings are not the same
                if (identificationParametersFactory.getParametersList().contains(newParameters.getName())
                        && !identificationParametersFactory.getIdentificationParameters(newParameters.getName()).equals(newParameters)) {

                    int value = JOptionPane.showConfirmDialog(this, "A settings file with the same name already exists. Overwrite file?", "Overwrite File?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (value != JOptionPane.YES_OPTION) {
                        return;
                    }

                    identificationParametersFactory.addIdentificationParameters(newParameters);
                    dispose();

                } else {
                    identificationParametersFactory.addIdentificationParameters(newParameters);
                    dispose();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error occurred while saving " + newParameters.getName() + ". Please verify the settings.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Change the icon into a hand icon.
     *
     * @param evt
     */
    private void advancedSettingsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_advancedSettingsLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_advancedSettingsLabelMouseEntered

    /**
     * Change the icon back to the default icon.
     *
     * @param evt
     */
    private void advancedSettingsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_advancedSettingsLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_advancedSettingsLabelMouseExited

    /**
     * Show/hide the advanced settings.
     *
     * @param evt
     */
    private void advancedSettingsLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_advancedSettingsLabelMouseReleased
        showAdvancedSettings = !showAdvancedSettings;
        updateAdvancedSettings();
    }//GEN-LAST:event_advancedSettingsLabelMouseReleased

    /**
     * Validate the input.
     *
     * @param evt
     */
    private void nameTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTxtKeyReleased
        validateInput();
    }//GEN-LAST:event_nameTxtKeyReleased

    /**
     * Cancel the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        canceled = true;
    }//GEN-LAST:event_formWindowClosing

    /**
     * Change the icon to a hand icon.
     *
     * @param evt
     */
    private void exportLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_exportLabelMouseEntered

    /**
     * Change the icon to the default icon.
     *
     * @param evt
     */
    private void exportLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_exportLabelMouseExited

    /**
     * Save the settings to file.
     *
     * @param evt
     */
    private void exportLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportLabelMouseReleased
        File selectedFile = Util.getUserSelectedFile(this, ".par", "Identification settings file (.par)", "Export Identification Settings", lastSelectedFolder.getLastSelectedFolder(), null, false);

        if (selectedFile != null) {
            try {
                IdentificationParameters.saveIdentificationParameters(getIdentificationParameters(), selectedFile);
                JOptionPane.showMessageDialog(null, "Identification settings saved to " + selectedFile.getAbsolutePath() + ".", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error occurred while saving " + selectedFile + ". Please verify the file.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_exportLabelMouseReleased

    /**
     * Change the icon to a hand icon.
     *
     * @param evt
     */
    private void importLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_importLabelMouseEntered

    /**
     * Change the icon to the default icon.
     *
     * @param evt
     */
    private void importLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_importLabelMouseExited

    /**
     * Open a file chooser to select a settings file.
     *
     * @param evt
     */
    private void importLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importLabelMouseReleased
        File selectedFile = Util.getUserSelectedFile(this, ".par", "Identification settings file (.par)", "Import Identification Settings File", lastSelectedFolder.getLastSelectedFolder(), null, true);

        if (selectedFile != null) {

            lastSelectedFolder.setLastSelectedFolder(selectedFile.getAbsolutePath());

            try {
                oldIdentificationParameters = null;
                IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(selectedFile);
                extractParameters(identificationParameters);
                nameTxt.setText(identificationParameters.getName());
                saveButton.setEnabled(true);
                updateGUI();

                validateParametersInput(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error occurred while reading " + selectedFile + ". Please verify the file.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        validateInput();
    }//GEN-LAST:event_importLabelMouseReleased

    /**
     * Open the peptide variants dialog.
     *
     * @param evt
     */
    private void peptideVariantsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_peptideVariantsButtonActionPerformed
        PeptideVariantsParametersDialog peptideVariantsSettingsDialog = new PeptideVariantsParametersDialog(this, parentFrame, peptideVariantsPreferences, editable);
        if (!peptideVariantsSettingsDialog.isCanceled()) {
            peptideVariantsPreferences = peptideVariantsSettingsDialog.getPeptideVariantsPreferences();
            updateGUI();
        }
    }//GEN-LAST:event_peptideVariantsButtonActionPerformed

    /**
     * Show/hide the advanced settings.
     */
    private void updateAdvancedSettings() {
        geneMappingButton.setVisible(showAdvancedSettings);
        spectrumAnnotationButton.setVisible(showAdvancedSettings);
        sequenceMatchingButton.setVisible(showAdvancedSettings);
        peptideVariantsButton.setVisible(showAdvancedSettings);
        matchesFiltersButton.setVisible(showAdvancedSettings);
        psmScoringButton.setVisible(showAdvancedSettings);
        ptmLocalizationButton.setVisible(showAdvancedSettings);
        proteinInferenceButton.setVisible(showAdvancedSettings);
        validationButton.setVisible(showAdvancedSettings);
        fractionsButton.setVisible(showAdvancedSettings);
        qualityControlButton.setVisible(showAdvancedSettings);

        repaint();

        if (showAdvancedSettings) {
            advancedSettingsLabel.setText("<html><a href>Hide Advanced Settings</a></html>");
        } else {
            advancedSettingsLabel.setText("<html><a href>Show Advanced Settings</a></html>");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel advancedSettingsLabel;
    private javax.swing.JPanel attributesPanel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel exportLabel;
    private javax.swing.JButton fractionsButton;
    private javax.swing.JButton geneMappingButton;
    private javax.swing.JLabel importLabel;
    private javax.swing.JButton matchesFiltersButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JButton peptideVariantsButton;
    private javax.swing.JButton proteinInferenceButton;
    private javax.swing.JButton psmScoringButton;
    private javax.swing.JButton ptmLocalizationButton;
    private javax.swing.JButton qualityControlButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton sequenceMatchingButton;
    private javax.swing.JPanel settingsOuterPanel;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JButton spectrumAnnotationButton;
    private javax.swing.JButton spectrumMatchingButton;
    private javax.swing.JButton validationButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Inspects the search parameter validity.
     *
     * @param showMessage if true an error message is shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateParametersInput(boolean showMessage) {

        if (searchParameters == null) {
            return false;
        }

        String name = nameTxt.getText();
        SearchParametersDialog searchSettingsDialog = new SearchParametersDialog(this, parentFrame, searchParameters,
                normalIcon, waitingIcon, false, true, configurationFile, lastSelectedFolder, name, editable);

        boolean valid = searchSettingsDialog.validateParametersInput(false);
        int columnWidth = 150;
        int maxDescriptionLength = 150;

        if (!valid) {
            spectrumMatchingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b><font color=\"red\">Spectrum Matching</font></b></td>"
                    + "<td><font size=2>" + formatDescription(searchParameters.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");
            spectrumMatchingButton.setToolTipText("Please check the search settings");

            if (showMessage) {
                searchSettingsDialog.validateParametersInput(true);
                searchSettingsDialog.setVisible(true);

                if (!searchSettingsDialog.isCanceled()) {
                    searchParameters = searchSettingsDialog.getSearchParameters();
                }
            }
        } else {
            spectrumMatchingButton.setText("<html><table><tr><td width=\"" + columnWidth + "\"><b>Spectrum Matching</b></td>"
                    + "<td><font size=2>" + formatDescription(searchParameters.getShortDescription(), maxDescriptionLength) + "</font></td></tr></table></html>");
            spectrumMatchingButton.setToolTipText("Please check the search settings");
            spectrumMatchingButton.setToolTipText(null);
        }

        return valid;
    }
}
