package com.compomics.util.gui.export.report;

import com.compomics.util.io.export.ExportFactory;
import com.compomics.util.io.export.ExportFeature;
import com.compomics.util.io.export.ExportScheme;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;

/**
 * Dialog for editing reports.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ReportEditor extends javax.swing.JDialog {

    /**
     * The export factory.
     */
    private ExportFactory exportFactory;
    /**
     * The original name of the report.
     */
    private String oldName;
    /**
     * A boolean indicating whether the report can be edited.
     */
    private boolean editable = true;
    /**
     * The original selection.
     */
    private HashMap<String, ArrayList<ExportFeature>> selection = new HashMap<>();
    /**
     * The selected section name.
     */
    private String sectionName = null;
    /**
     * The list of implemented features for the selected section.
     */
    private ArrayList<ExportFeature> featuresList;

    /**
     * Constructor.
     *
     * @param parent the parent frame
     * @param exportFactory the export factory containing the implemented
     * exports for the running software
     * @param exportSchemeName the name of the export scheme to edit
     * @param editable if the report is editable
     */
    public ReportEditor(java.awt.Frame parent, ExportFactory exportFactory, String exportSchemeName, boolean editable) {
        super(parent, true);
        this.exportFactory = exportFactory;
        this.editable = editable;
        initComponents();
        oldName = exportSchemeName;
        selection = new HashMap<>();
        ExportScheme exportScheme = exportFactory.getExportScheme(exportSchemeName);
        for (String section : exportScheme.getSections()) {
            if (!selection.containsKey(section)) {
                selection.put(section, new ArrayList<>());
            }
            selection.get(section).addAll(exportScheme.getExportFeatures(section));
        }
        setUpGUI();
        this.setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Constructor.
     *
     * @param parent the parent frame
     * @param exportFactory the export factory containing the implemented
     * exports for the running software
     */
    public ReportEditor(java.awt.Frame parent, ExportFactory exportFactory) {
        super(parent, true);
        this.exportFactory = exportFactory;
        initComponents();
        setUpGUI();
        this.setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Sets up the GUI components.
     */
    private void setUpGUI() {

        sectionsTable.getTableHeader().setReorderingAllowed(false);
        featuresTable.getTableHeader().setReorderingAllowed(false);

        sectionsTable.getColumn(" ").setMaxWidth(30);
        sectionsTable.getColumn(" ").setMinWidth(30);

        featuresTable.getColumn(" ").setMaxWidth(30);
        featuresTable.getColumn(" ").setMinWidth(30);
        featuresTable.getColumn("  ").setMaxWidth(30);
        featuresTable.getColumn("  ").setMinWidth(30);

        featuresTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());

        nameTxt.setEnabled(editable);
        maintTitleCheckBox.setEnabled(editable);
        tabRadioButton.setEnabled(editable);
        semicolonRadioButton.setEnabled(editable);
        commaRadioButton.setEnabled(editable);
        spaceRadioButton.setEnabled(editable);
        sectionTitleCheckBox.setEnabled(editable);
        separationLinesSpinner.setEnabled(editable);
        sectionsTable.setEnabled(editable);
        featuresTable.setEnabled(editable);
        lineNumberCheckBox.setEnabled(editable);
        headerCheckBox.setEnabled(editable);
        validatedCheck.setEnabled(editable);
        decoysCheck.setEnabled(editable);

        // make sure that the scroll panes are see-through
        featuresScrollPane.getViewport().setOpaque(false);
        sectionsScrollPane.getViewport().setOpaque(false);

        if (oldName != null) {
            nameTxt.setText(oldName);

            ExportScheme exportScheme = exportFactory.getExportScheme(oldName);
            if (exportScheme.getMainTitle() != null) {
                maintTitleCheckBox.setSelected(true);
                mainTitleTxt.setText(exportScheme.getMainTitle());
            }

            if (exportScheme.getSeparator().equalsIgnoreCase("\\t")) {
                tabRadioButton.setSelected(true);
            } else if (exportScheme.getSeparator().equalsIgnoreCase(";")) {
                semicolonRadioButton.setSelected(true);
            } else if (exportScheme.getSeparator().equalsIgnoreCase(",")) {
                commaRadioButton.setSelected(true);
            } else if (exportScheme.getSeparator().equalsIgnoreCase(" ")) {
                spaceRadioButton.setSelected(true);
            }

            sectionTitleCheckBox.setSelected(exportScheme.isIncludeSectionTitles());
            separationLinesSpinner.setValue(exportScheme.getSeparationLines());
            lineNumberCheckBox.setSelected(exportScheme.isIndexes());
            headerCheckBox.setSelected(exportScheme.isHeader());
            validatedCheck.setSelected(exportScheme.isValidatedOnly());
            decoysCheck.setSelected(exportScheme.isIncludeDecoy());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        delimiterButtonGroup = new javax.swing.ButtonGroup();
        backgroundPanel = new javax.swing.JPanel();
        reporterTypePanel = new javax.swing.JPanel();
        nameTxt = new javax.swing.JTextField();
        reporterSettingsPanel = new javax.swing.JPanel();
        maintTitleCheckBox = new javax.swing.JCheckBox();
        sectionTitleCheckBox = new javax.swing.JCheckBox();
        separationLinesSpinner = new javax.swing.JSpinner();
        numberOfSeparationLinesLabel = new javax.swing.JLabel();
        lineNumberCheckBox = new javax.swing.JCheckBox();
        headerCheckBox = new javax.swing.JCheckBox();
        tabRadioButton = new javax.swing.JRadioButton();
        semicolonRadioButton = new javax.swing.JRadioButton();
        commaRadioButton = new javax.swing.JRadioButton();
        spaceRadioButton = new javax.swing.JRadioButton();
        columnDelimiterLabel = new javax.swing.JLabel();
        rowDelimiterLabel = new javax.swing.JLabel();
        structureLabel = new javax.swing.JLabel();
        reportTitleLabel = new javax.swing.JLabel();
        mainTitleTxt = new javax.swing.JTextField();
        matchesLbl = new javax.swing.JLabel();
        validatedCheck = new javax.swing.JCheckBox();
        decoysCheck = new javax.swing.JCheckBox();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        sectionsPanel = new javax.swing.JPanel();
        sectionsScrollPane = new javax.swing.JScrollPane();
        sectionsTable = new javax.swing.JTable();
        sectionContentPanel = new javax.swing.JPanel();
        featuresScrollPane = new javax.swing.JScrollPane();
        featuresTable = new javax.swing.JTable();
        advancedFeaturesCheck = new javax.swing.JCheckBox();
        subFeaturesCheck = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Report");
        setMinimumSize(new java.awt.Dimension(720, 650));

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        reporterTypePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Report Type"));
        reporterTypePanel.setOpaque(false);

        javax.swing.GroupLayout reporterTypePanelLayout = new javax.swing.GroupLayout(reporterTypePanel);
        reporterTypePanel.setLayout(reporterTypePanelLayout);
        reporterTypePanelLayout.setHorizontalGroup(
            reporterTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reporterTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameTxt)
                .addContainerGap())
        );
        reporterTypePanelLayout.setVerticalGroup(
            reporterTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        reporterSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        reporterSettingsPanel.setOpaque(false);

        maintTitleCheckBox.setText("Report Title");
        maintTitleCheckBox.setIconTextGap(15);
        maintTitleCheckBox.setOpaque(false);
        maintTitleCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maintTitleCheckBoxActionPerformed(evt);
            }
        });

        sectionTitleCheckBox.setText("Section Titles");
        sectionTitleCheckBox.setIconTextGap(15);
        sectionTitleCheckBox.setOpaque(false);

        separationLinesSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        separationLinesSpinner.setToolTipText("Number of empty lines between each section");
        separationLinesSpinner.setEnabled(false);

        numberOfSeparationLinesLabel.setFont(numberOfSeparationLinesLabel.getFont().deriveFont((numberOfSeparationLinesLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        numberOfSeparationLinesLabel.setText("#lines between sections");
        numberOfSeparationLinesLabel.setToolTipText("Number of empty lines between each section");

        lineNumberCheckBox.setSelected(true);
        lineNumberCheckBox.setText("Line Numbers");
        lineNumberCheckBox.setIconTextGap(15);
        lineNumberCheckBox.setOpaque(false);

        headerCheckBox.setSelected(true);
        headerCheckBox.setText("Table Headers");
        headerCheckBox.setIconTextGap(15);
        headerCheckBox.setOpaque(false);

        delimiterButtonGroup.add(tabRadioButton);
        tabRadioButton.setSelected(true);
        tabRadioButton.setText("Tab");
        tabRadioButton.setIconTextGap(15);
        tabRadioButton.setOpaque(false);

        delimiterButtonGroup.add(semicolonRadioButton);
        semicolonRadioButton.setText("Semicolon");
        semicolonRadioButton.setIconTextGap(15);
        semicolonRadioButton.setOpaque(false);

        delimiterButtonGroup.add(commaRadioButton);
        commaRadioButton.setText("Comma");
        commaRadioButton.setIconTextGap(15);
        commaRadioButton.setOpaque(false);

        delimiterButtonGroup.add(spaceRadioButton);
        spaceRadioButton.setText("Space");
        spaceRadioButton.setIconTextGap(15);
        spaceRadioButton.setOpaque(false);

        columnDelimiterLabel.setText("Column Delimiter");

        rowDelimiterLabel.setText("Row Delimiter");

        structureLabel.setText("Structure");

        reportTitleLabel.setText("Report Title");

        mainTitleTxt.setEnabled(false);

        matchesLbl.setText("Matches");

        validatedCheck.setSelected(true);
        validatedCheck.setText("Validated Only");
        validatedCheck.setIconTextGap(15);
        validatedCheck.setOpaque(false);

        decoysCheck.setText("Include Decoys");
        decoysCheck.setIconTextGap(15);
        decoysCheck.setOpaque(false);

        javax.swing.GroupLayout reporterSettingsPanelLayout = new javax.swing.GroupLayout(reporterSettingsPanel);
        reporterSettingsPanel.setLayout(reporterSettingsPanelLayout);
        reporterSettingsPanelLayout.setHorizontalGroup(
            reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(maintTitleCheckBox)
                                        .addComponent(sectionTitleCheckBox)
                                        .addComponent(lineNumberCheckBox))
                                    .addComponent(headerCheckBox)))
                            .addComponent(structureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(columnDelimiterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spaceRadioButton)
                                    .addComponent(semicolonRadioButton)
                                    .addComponent(tabRadioButton)
                                    .addComponent(commaRadioButton))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(validatedCheck)
                                    .addComponent(decoysCheck)))
                            .addComponent(matchesLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rowDelimiterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(separationLinesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(numberOfSeparationLinesLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE))
                    .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                        .addComponent(reportTitleLabel)
                        .addGap(18, 18, 18)
                        .addComponent(mainTitleTxt)))
                .addGap(14, 14, 14))
        );

        reporterSettingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {headerCheckBox, lineNumberCheckBox, maintTitleCheckBox, sectionTitleCheckBox});

        reporterSettingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {semicolonRadioButton, tabRadioButton});

        reporterSettingsPanelLayout.setVerticalGroup(
            reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                        .addComponent(columnDelimiterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabRadioButton)
                        .addGap(0, 0, 0)
                        .addComponent(semicolonRadioButton)
                        .addGap(0, 0, 0)
                        .addComponent(commaRadioButton))
                    .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                .addComponent(structureLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maintTitleCheckBox)
                                .addGap(0, 0, 0)
                                .addComponent(sectionTitleCheckBox)
                                .addGap(0, 0, 0)
                                .addComponent(lineNumberCheckBox))
                            .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(validatedCheck))
                                    .addGroup(reporterSettingsPanelLayout.createSequentialGroup()
                                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(rowDelimiterLabel)
                                            .addComponent(matchesLbl))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(separationLinesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(numberOfSeparationLinesLabel))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(decoysCheck)))
                        .addGap(0, 0, 0)
                        .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(headerCheckBox)
                            .addComponent(spaceRadioButton))))
                .addGap(18, 18, 18)
                .addGroup(reporterSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reportTitleLabel)
                    .addComponent(mainTitleTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        reporterSettingsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {mainTitleTxt, numberOfSeparationLinesLabel});

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

        sectionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sections"));
        sectionsPanel.setOpaque(false);

        sectionsTable.setModel(new SectionsTableModel());
        sectionsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sectionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sectionsTableMouseReleased(evt);
            }
        });
        sectionsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sectionsTableKeyReleased(evt);
            }
        });
        sectionsScrollPane.setViewportView(sectionsTable);

        javax.swing.GroupLayout sectionsPanelLayout = new javax.swing.GroupLayout(sectionsPanel);
        sectionsPanel.setLayout(sectionsPanelLayout);
        sectionsPanelLayout.setHorizontalGroup(
            sectionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sectionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sectionsScrollPane)
                .addContainerGap())
        );
        sectionsPanelLayout.setVerticalGroup(
            sectionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sectionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sectionsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addContainerGap())
        );

        sectionContentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Section Content"));
        sectionContentPanel.setOpaque(false);

        featuresTable.setModel(new FeaturesTableModel());
        featuresTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        featuresScrollPane.setViewportView(featuresTable);

        advancedFeaturesCheck.setText("Show Advanced Features");
        advancedFeaturesCheck.setIconTextGap(10);
        advancedFeaturesCheck.setOpaque(false);
        advancedFeaturesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedFeaturesCheckActionPerformed(evt);
            }
        });

        subFeaturesCheck.setText("Show Sub Features");
        subFeaturesCheck.setIconTextGap(10);
        subFeaturesCheck.setOpaque(false);
        subFeaturesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subFeaturesCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sectionContentPanelLayout = new javax.swing.GroupLayout(sectionContentPanel);
        sectionContentPanel.setLayout(sectionContentPanelLayout);
        sectionContentPanelLayout.setHorizontalGroup(
            sectionContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sectionContentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sectionContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(sectionContentPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(advancedFeaturesCheck)
                        .addGap(18, 18, 18)
                        .addComponent(subFeaturesCheck))
                    .addComponent(featuresScrollPane))
                .addContainerGap())
        );
        sectionContentPanelLayout.setVerticalGroup(
            sectionContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sectionContentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(featuresScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sectionContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(advancedFeaturesCheck)
                    .addComponent(subFeaturesCheck)))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reporterSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(sectionContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sectionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reporterTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reporterTypePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reporterSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sectionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sectionContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
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
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Save the export scheme and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (editable) {
            String newName = nameTxt.getText().trim();

            if (newName.trim().length() == 0) {
                JOptionPane.showMessageDialog(this, "Please provide a name for the report type.", "Report Type Missing", JOptionPane.INFORMATION_MESSAGE);
                nameTxt.requestFocus();
                return;
            }

            if (oldName != null && !oldName.contains(newName)) {
                exportFactory.removeExportScheme(oldName);
            }

            // get the separator
            String separator;

            if (tabRadioButton.isSelected()) {
                separator = "\t";
            } else if (semicolonRadioButton.isSelected()) {
                separator = ";";
            } else if (commaRadioButton.isSelected()) {
                separator = ",";
            } else { // space selected
                separator = " ";
            }

            HashMap<String, ArrayList<ExportFeature>> features = new HashMap<>(selection);

            if (maintTitleCheckBox.isSelected()) {
                ExportScheme exportScheme = new ExportScheme(newName, true, features, separator,
                        lineNumberCheckBox.isSelected(), headerCheckBox.isSelected(), (Integer) separationLinesSpinner.getValue(),
                        sectionTitleCheckBox.isSelected(), validatedCheck.isSelected(), decoysCheck.isSelected(), mainTitleTxt.getText().trim());
                exportFactory.addExportScheme(exportScheme);
            } else {
                ExportScheme exportScheme = new ExportScheme(newName, true, features, separator,
                        lineNumberCheckBox.isSelected(), headerCheckBox.isSelected(), (Integer) separationLinesSpinner.getValue(), sectionTitleCheckBox.isSelected(), validatedCheck.isSelected(), decoysCheck.isSelected());
                exportFactory.addExportScheme(exportScheme);
            }
        }
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Enable/disable the main title text field.
     *
     * @param evt
     */
    private void maintTitleCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maintTitleCheckBoxActionPerformed
        mainTitleTxt.setEnabled(maintTitleCheckBox.isSelected());
    }//GEN-LAST:event_maintTitleCheckBoxActionPerformed

    /**
     * Update the section content table.
     *
     * @param evt
     */
    private void sectionsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sectionsTableMouseReleased
        if (sectionsTable.getSelectedRow() != -1) {
            sectionName = (String) sectionsTable.getValueAt(sectionsTable.getSelectedRow(), 1);
            updateFeatureTableContent();
        }
    }//GEN-LAST:event_sectionsTableMouseReleased

    /**
     * Update the section content table.
     *
     * @param evt
     */
    private void sectionsTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sectionsTableKeyReleased
        sectionsTableMouseReleased(null);
    }//GEN-LAST:event_sectionsTableKeyReleased

    private void subFeaturesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subFeaturesCheckActionPerformed
        updateFeatureTableContent();
    }//GEN-LAST:event_subFeaturesCheckActionPerformed

    private void advancedFeaturesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedFeaturesCheckActionPerformed
        updateFeatureTableContent();
    }//GEN-LAST:event_advancedFeaturesCheckActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox advancedFeaturesCheck;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel columnDelimiterLabel;
    private javax.swing.JRadioButton commaRadioButton;
    private javax.swing.JCheckBox decoysCheck;
    private javax.swing.ButtonGroup delimiterButtonGroup;
    private javax.swing.JScrollPane featuresScrollPane;
    private javax.swing.JTable featuresTable;
    private javax.swing.JCheckBox headerCheckBox;
    private javax.swing.JCheckBox lineNumberCheckBox;
    private javax.swing.JTextField mainTitleTxt;
    private javax.swing.JCheckBox maintTitleCheckBox;
    private javax.swing.JLabel matchesLbl;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JLabel numberOfSeparationLinesLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel reportTitleLabel;
    private javax.swing.JPanel reporterSettingsPanel;
    private javax.swing.JPanel reporterTypePanel;
    private javax.swing.JLabel rowDelimiterLabel;
    private javax.swing.JPanel sectionContentPanel;
    private javax.swing.JCheckBox sectionTitleCheckBox;
    private javax.swing.JPanel sectionsPanel;
    private javax.swing.JScrollPane sectionsScrollPane;
    private javax.swing.JTable sectionsTable;
    private javax.swing.JRadioButton semicolonRadioButton;
    private javax.swing.JSpinner separationLinesSpinner;
    private javax.swing.JRadioButton spaceRadioButton;
    private javax.swing.JLabel structureLabel;
    private javax.swing.JCheckBox subFeaturesCheck;
    private javax.swing.JRadioButton tabRadioButton;
    private javax.swing.JCheckBox validatedCheck;
    // End of variables declaration//GEN-END:variables

    /**
     * Indicates whether a feature has been selected in the given section.
     *
     * @param section the section of interest
     * @param exportFeature the export feature of interest
     * @return a boolean indicating whether a feature has been selected in the
     * given section
     */
    private boolean isSelected(String section, ExportFeature exportFeature) {
        ArrayList<ExportFeature> selectedSectionFeatures = selection.get(section);
        if (selectedSectionFeatures != null && selectedSectionFeatures.contains(exportFeature)) {
            return true;
        }
        return false;
    }

    /**
     * Sets whether a feature is selected in the given section.
     *
     * @param section the section of interest
     * @param exportFeature the feature of interest
     * @param selected a boolean indicating whether the feature shall be
     * selected or not
     */
    private void setSelected(String section, ExportFeature exportFeature, Boolean selected) {
        if (selected) {
            if (!selection.containsKey(section)) {
                selection.put(section, new ArrayList<>());
            }
            if (!selection.get(section).contains(exportFeature)) {
                selection.get(section).add(exportFeature);
            }
        } else {
            if (selection.containsKey(section)) {
                selection.get(section).remove(exportFeature);
                if (selection.get(section).isEmpty()) {
                    selection.remove(section);
                }
            }
        }
    }

    /**
     * Updates the feature table content based on the section name.
     */
    private void updateFeatureTableContent() {
        featuresList = new ArrayList<>();
        if (sectionName != null) {
            if (advancedFeaturesCheck.isSelected()) {
                featuresList.addAll(exportFactory.getExportFeatures(sectionName, subFeaturesCheck.isSelected()));
            } else {
                for (ExportFeature exportFeature : exportFactory.getExportFeatures(sectionName, subFeaturesCheck.isSelected())) {
                    if (!exportFeature.isAdvanced()) {
                        featuresList.add(exportFeature);
                    }
                }
            }
        }
        ((DefaultTableModel) featuresTable.getModel()).fireTableDataChanged();
    }

    /**
     * Table model for the reports table.
     */
    private class SectionsTableModel extends DefaultTableModel {

        /**
         * The list of implemented sections.
         */
        private final ArrayList<String> sectionList;

        /**
         * Constructor.
         */
        public SectionsTableModel() {
            sectionList = new ArrayList<>();
            sectionList.addAll(exportFactory.getImplementedSections());
        }

        @Override
        public int getRowCount() {
            if (sectionList == null) {
                return 0;
            }
            return sectionList.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Name";
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
                    return sectionList.get(row);
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
            return false;
        }
    }

    /**
     * Table model for the reports table.
     */
    private class FeaturesTableModel extends DefaultTableModel {

        /**
         * Constructor
         */
        public FeaturesTableModel() {
        }

        @Override
        public int getRowCount() {
            if (featuresList == null) {
                return 0;
            }
            return featuresList.size();
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
                    return "  ";
                case 2:
                    return "Section";
                case 3:
                    return "Name";
                case 4:
                    return "Description";
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
                    return isSelected(sectionName, featuresList.get(row));
                case 2:
                    return featuresList.get(row).getFeatureFamily();
                case 3:
                    StringBuilder result = new StringBuilder();
                        if (result.length() > 0) {
                            result.append(", ");
                        }
                        result.append(featuresList.get(row).getTitle());
                    return result.toString();
                case 4:
                    return featuresList.get(row).getDescription();
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
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            setSelected(sectionName, featuresList.get(row), (Boolean) aValue);
        }
    }
}
