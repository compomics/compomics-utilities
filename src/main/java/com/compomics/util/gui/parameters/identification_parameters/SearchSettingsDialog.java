package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.gui.GuiUtilities;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.protein_sequences_manager.gui.SequenceDbDetailsDialog;
import com.compomics.util.gui.ptm.ModificationsDialog;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.io.ConfigurationFile;
import com.compomics.util.preferences.LastSelectedFolder;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;

/**
 * The search settings dialog.
 *
 * @author Harald Barsnes
 */
public class SearchSettingsDialog extends javax.swing.JDialog {

    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    /**
     * Convenience array for forward ion type selection.
     */
    private String[] forwardIons = {"a", "b", "c"};
    /**
     * Convenience array for rewind ion type selection.
     */
    private String[] rewindIons = {"x", "y", "z"};
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /*
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The modification table column header tooltips.
     */
    private ArrayList<String> modificationTableToolTips;
    /**
     * The dialog parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * The normal dialog icon.
     */
    private Image normalIcon;
    /**
     * The waiting dialog icon.
     */
    private Image waitingIcon;
    /**
     * Reference for the separation of modifications.
     */
    public static final String MODIFICATION_SEPARATOR = "//";
    /**
     * Counts the number of times the users has pressed a key on the keyboard in
     * the search field.
     */
    private int keyPressedCounter = 0;
    /**
     * The current PTM search string.
     */
    private String currentPtmSearchString = "";
    /**
     * The time to wait between keys typed before updating the search.
     */
    private int waitingTime = 500;
    /**
     * The configuration file containing the modification use.
     */
    private ConfigurationFile configurationFile;
    /**
     * The list of the default modifications.
     */
    private ArrayList<String> modificationUse = new ArrayList<String>();
    /**
     * The line to use to reference the modification use in the configuration
     * file.
     */
    public static final String modificationUseInConfiguration = "Modification use:";
    /**
     * Boolean indicating whether the cancel button was pressed.
     */
    private boolean canceled = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;
    /**
     * The last selected folder to use.
     */
    private final LastSelectedFolder lastSelectedFolder;
    /**
     * The horizontal padding used before and after the text in the titled
     * borders. (Needed to make it look as good in Java 7 as it did in Java
     * 6...)
     */
    public static String TITLED_BORDER_HORIZONTAL_PADDING = "";

    /**
     * Creates a new SearchSettingsDialog with a frame as owner.
     *
     * @param parentFrame the parent frame
     * @param searchParameters previous search parameters
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param setVisible if the dialog is to be visible or not
     * @param modal if the dialog is to be modal
     * @param configurationFile a file containing the modification use
     * @param lastSelectedFolder the last selected folder to use
     * @param settingsName the name of the settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SearchSettingsDialog(java.awt.Frame parentFrame, SearchParameters searchParameters, Image normalIcon, Image waitingIcon,
            boolean setVisible, boolean modal, ConfigurationFile configurationFile, LastSelectedFolder lastSelectedFolder, String settingsName, boolean editable) {
        super(parentFrame, modal);

        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
        this.configurationFile = configurationFile;
        this.editable = editable;

        if (searchParameters == null) {
            this.searchParameters = new SearchParameters();
        } else {
            this.searchParameters = searchParameters;
        }

        try {
            loadModificationUse(configurationFile);
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }

        initComponents();
        setUpGUI();
        formComponentResized(null);
        setLocationRelativeTo(parentFrame);

        String dialogTitle = "Search Settings";
        if (settingsName != null && settingsName.length() > 0) {
            dialogTitle += " - " + settingsName;
        }
        setTitle(dialogTitle);

        if (setVisible) {
            setVisible(true);
        }
    }

    /**
     * Creates a new SearchSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame the parent frame
     * @param searchParameters previous search parameters
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param setVisible if the dialog is to be visible or not
     * @param modal if the dialog is to be modal
     * @param configurationFile a file containing the modification use
     * @param lastSelectedFolder the last selected folder to use
     * @param settingsName the name of the settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SearchSettingsDialog(Dialog owner, java.awt.Frame parentFrame, SearchParameters searchParameters, Image normalIcon, Image waitingIcon,
            boolean setVisible, boolean modal, ConfigurationFile configurationFile, LastSelectedFolder lastSelectedFolder, String settingsName, boolean editable) {
        super(owner, modal);

        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
        this.configurationFile = configurationFile;
        this.editable = editable;

        if (searchParameters == null) {
            this.searchParameters = new SearchParameters();
        } else {
            this.searchParameters = searchParameters;
        }

        try {
            loadModificationUse(configurationFile);
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }

        initComponents();
        setUpGUI();
        formComponentResized(null);
        setLocationRelativeTo(owner);

        String dialogTitle = "Search Settings";
        if (settingsName != null && settingsName.length() > 0) {
            dialogTitle += " - " + settingsName;
        }
        setTitle(dialogTitle);

        if (setVisible) {
            setVisible(true);
        }
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {

        setScreenProps();
        validateParametersInput(false);

        // set the settings editable or not
        enzymesCmb.setEnabled(editable);
        precursorIonAccuracyTxt.setEditable(editable);
        precursorIonUnit.setEnabled(editable);
        fragmentIon1Cmb.setEnabled(editable);
        fragmentIon2Cmb.setEnabled(editable);
        maxMissedCleavagesTxt.setEditable(editable);
        fragmentIonAccuracyTxt.setEditable(editable);
        minPrecursorChargeTxt.setEditable(editable);
        maxPrecursorChargeTxt.setEditable(editable);
        addFixedModification.setEnabled(editable);
        removeFixedModification.setEnabled(editable);
        addVariableModification.setEnabled(editable);
        removeVariableModification.setEnabled(editable);
        if (!editable) {
            editDatabaseDetailsButton.setText("View");
        }

        modificationTypesSplitPane.setDividerLocation(0.5);

        fixedModsTable.getTableHeader().setReorderingAllowed(false);
        variableModsTable.getTableHeader().setReorderingAllowed(false);
        modificationsTable.getTableHeader().setReorderingAllowed(false);

        // centrally align the comboboxes
        modificationsListCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        enzymesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon1Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon2Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        precursorIonUnit.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        ((TitledBorder) dataBasePanelSettings.getBorder()).setTitle(TITLED_BORDER_HORIZONTAL_PADDING + "Database" + TITLED_BORDER_HORIZONTAL_PADDING);
        ((TitledBorder) modificationsPanel.getBorder()).setTitle(TITLED_BORDER_HORIZONTAL_PADDING + "Modifications" + TITLED_BORDER_HORIZONTAL_PADDING);
        ((TitledBorder) proteaseAndFragmentationPanel.getBorder()).setTitle(TITLED_BORDER_HORIZONTAL_PADDING + "Protease & Fragmentation" + TITLED_BORDER_HORIZONTAL_PADDING);

        fixedModsJScrollPane.getViewport().setOpaque(false);
        variableModsJScrollPane.getViewport().setOpaque(false);
        modificationsJScrollPane.getViewport().setOpaque(false);

        fixedModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        variableModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());

        fixedModsTable.getColumn(" ").setMaxWidth(35);
        fixedModsTable.getColumn(" ").setMinWidth(35);
        variableModsTable.getColumn(" ").setMaxWidth(35);
        variableModsTable.getColumn(" ").setMinWidth(35);

        fixedModsTable.getColumn("Mass").setMaxWidth(100);
        fixedModsTable.getColumn("Mass").setMinWidth(100);
        variableModsTable.getColumn("Mass").setMaxWidth(100);
        variableModsTable.getColumn("Mass").setMinWidth(100);

        modificationTableToolTips = new ArrayList<String>();
        modificationTableToolTips.add(null);
        modificationTableToolTips.add("Modification Name");
        modificationTableToolTips.add("Modification Mass");
        modificationTableToolTips.add("<html>Included in the list of the<br>Most Used Modifications</html>");

        setAllModificationTableProperties();

        updateModificationList();
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

        if (modificationsListCombo.getSelectedIndex() == 1) {
            try {
                ImageIcon pinnedIcon = new ImageIcon(this.getClass().getResource("/icons/pinned.png"));
                //ImageIcon unpinnedIcon = new ImageIcon(this.getClass().getResource("/icons/unpinned.png"));
                modificationsTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                        pinnedIcon, null, "<html>Included in the list of the<br>Most Used Modifications</html>", null));
            } catch (Exception e) {
                modificationsTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());
            }
            modificationsTable.getColumn("  ").setMaxWidth(30);
            modificationsTable.getColumn("  ").setMinWidth(30);
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

        backgroundPanel = new javax.swing.JPanel();
        proteaseAndFragmentationPanel = new javax.swing.JPanel();
        enzymeLabel = new javax.swing.JLabel();
        enzymesCmb = new javax.swing.JComboBox();
        maxMissedCleavagesLabel = new javax.swing.JLabel();
        maxMissedCleavagesTxt = new javax.swing.JTextField();
        precursorIonLbl = new javax.swing.JLabel();
        precursorIonAccuracyTxt = new javax.swing.JTextField();
        precursorIonUnit = new javax.swing.JComboBox();
        fragmentIonLbl = new javax.swing.JLabel();
        fragmentIonAccuracyTxt = new javax.swing.JTextField();
        fragmentIonType1Lbl = new javax.swing.JLabel();
        fragmentIon1Cmb = new javax.swing.JComboBox();
        fragmentIon2Cmb = new javax.swing.JComboBox();
        precursorChargeLbl = new javax.swing.JLabel();
        minPrecursorChargeTxt = new javax.swing.JTextField();
        maxPrecursorChargeTxt = new javax.swing.JTextField();
        precursorChargeRangeLabel = new javax.swing.JLabel();
        dataBasePanelSettings = new javax.swing.JPanel();
        databaseSettingsLbl = new javax.swing.JLabel();
        databaseSettingsTxt = new javax.swing.JTextField();
        editDatabaseDetailsButton = new javax.swing.JButton();
        modificationsLayeredPane = new javax.swing.JLayeredPane();
        modificationsPanel = new javax.swing.JPanel();
        modificationTypesSplitPane = new javax.swing.JSplitPane();
        fixedModsPanel = new javax.swing.JPanel();
        fixedModificationsLabel = new javax.swing.JLabel();
        addFixedModification = new javax.swing.JButton();
        removeFixedModification = new javax.swing.JButton();
        fixedModsJScrollPane = new javax.swing.JScrollPane();
        fixedModsTable = new JTable() {
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                //Always toggle on single selection
                super.changeSelection(rowIndex, columnIndex, !extend, extend);
            }
        };
        variableModsPanel = new javax.swing.JPanel();
        variableModificationsLabel = new javax.swing.JLabel();
        addVariableModification = new javax.swing.JButton();
        removeVariableModification = new javax.swing.JButton();
        variableModsJScrollPane = new javax.swing.JScrollPane();
        variableModsTable = new JTable() {
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                //Always toggle on single selection
                super.changeSelection(rowIndex, columnIndex, !extend, extend);
            }
        };
        availableModsPanel = new javax.swing.JPanel();
        modificationsListCombo = new javax.swing.JComboBox();
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
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                //Always toggle on single selection
                super.changeSelection(rowIndex, columnIndex, !extend, extend);
            }
        };
        openModificationSettingsJButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        openDialogHelpJButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search Settings");
        setMinimumSize(new java.awt.Dimension(700, 580));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        proteaseAndFragmentationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Enzyme & Fragmentation"));
        proteaseAndFragmentationPanel.setOpaque(false);

        enzymeLabel.setText("Enzyme");

        enzymesCmb.setMaximumRowCount(15);
        enzymesCmb.setModel(new DefaultComboBoxModel(loadEnzymes()));
        enzymesCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enzymesCmbActionPerformed(evt);
            }
        });

        maxMissedCleavagesLabel.setText("Max Missed Cleavages");

        maxMissedCleavagesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxMissedCleavagesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxMissedCleavagesTxtKeyReleased(evt);
            }
        });

        precursorIonLbl.setText("Precursor Mass Tolerance");

        precursorIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precursorIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                precursorIonAccuracyTxtKeyReleased(evt);
            }
        });

        precursorIonUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));

        fragmentIonLbl.setText("Fragment Mass Tolerance (Da)");

        fragmentIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        fragmentIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fragmentIonAccuracyTxtKeyReleased(evt);
            }
        });

        fragmentIonType1Lbl.setText("Fragment Ion Types");

        fragmentIon1Cmb.setModel(new DefaultComboBoxModel(forwardIons));

        fragmentIon2Cmb.setModel(new DefaultComboBoxModel(rewindIons));

        precursorChargeLbl.setText("Precursor Charge");

        minPrecursorChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecursorChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecursorChargeTxtKeyReleased(evt);
            }
        });

        maxPrecursorChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPrecursorChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPrecursorChargeTxtKeyReleased(evt);
            }
        });

        precursorChargeRangeLabel.setText("-");

        javax.swing.GroupLayout proteaseAndFragmentationPanelLayout = new javax.swing.GroupLayout(proteaseAndFragmentationPanel);
        proteaseAndFragmentationPanel.setLayout(proteaseAndFragmentationPanelLayout);
        proteaseAndFragmentationPanelLayout.setHorizontalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fragmentIonType1Lbl)
                    .addComponent(enzymeLabel)
                    .addComponent(precursorIonLbl))
                .addGap(18, 18, 18)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enzymesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(precursorIonAccuracyTxt)
                            .addComponent(fragmentIon1Cmb, 0, 110, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fragmentIon2Cmb, 0, 110, Short.MAX_VALUE)
                            .addComponent(precursorIonUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(50, 50, 50)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fragmentIonLbl)
                    .addComponent(maxMissedCleavagesLabel)
                    .addComponent(precursorChargeLbl))
                .addGap(18, 18, 18)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxMissedCleavagesTxt)
                    .addComponent(fragmentIonAccuracyTxt)
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .addGap(19, 19, 19)
                        .addComponent(precursorChargeRangeLabel)
                        .addGap(18, 18, 18)
                        .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)))
                .addContainerGap())
        );
        proteaseAndFragmentationPanelLayout.setVerticalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxMissedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enzymeLabel)
                    .addComponent(enzymesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxMissedCleavagesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentIonLbl)
                    .addComponent(precursorIonLbl)
                    .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentIonType1Lbl)
                    .addComponent(fragmentIon2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentIon1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorChargeLbl)
                    .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorChargeRangeLabel))
                .addContainerGap())
        );

        dataBasePanelSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Database"));
        dataBasePanelSettings.setOpaque(false);

        databaseSettingsLbl.setText("Database (FASTA)");

        databaseSettingsTxt.setEditable(false);

        editDatabaseDetailsButton.setText("Edit");
        editDatabaseDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDatabaseDetailsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dataBasePanelSettingsLayout = new javax.swing.GroupLayout(dataBasePanelSettings);
        dataBasePanelSettings.setLayout(dataBasePanelSettingsLayout);
        dataBasePanelSettingsLayout.setHorizontalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(databaseSettingsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(databaseSettingsTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editDatabaseDetailsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        dataBasePanelSettingsLayout.setVerticalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseSettingsLbl)
                    .addComponent(editDatabaseDetailsButton)
                    .addComponent(databaseSettingsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        modificationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modifications"));
        modificationsPanel.setOpaque(false);

        modificationTypesSplitPane.setBorder(null);
        modificationTypesSplitPane.setDividerSize(0);
        modificationTypesSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        modificationTypesSplitPane.setResizeWeight(0.5);
        modificationTypesSplitPane.setOpaque(false);
        modificationTypesSplitPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                modificationTypesSplitPaneComponentResized(evt);
            }
        });

        fixedModsPanel.setOpaque(false);

        fixedModificationsLabel.setFont(fixedModificationsLabel.getFont().deriveFont((fixedModificationsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        fixedModificationsLabel.setText("Fixed Modifications");

        addFixedModification.setText("<<");
        addFixedModification.setToolTipText("Add as fixed modification");
        addFixedModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFixedModificationActionPerformed(evt);
            }
        });

        removeFixedModification.setText(">>");
        removeFixedModification.setToolTipText("Remove as fixed modification");
        removeFixedModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFixedModificationActionPerformed(evt);
            }
        });

        fixedModsJScrollPane.setPreferredSize(new java.awt.Dimension(100, 60));

        fixedModsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "Mass"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class
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
        fixedModsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseExited(evt);
            }
        });
        fixedModsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseMoved(evt);
            }
        });
        fixedModsJScrollPane.setViewportView(fixedModsTable);

        javax.swing.GroupLayout fixedModsPanelLayout = new javax.swing.GroupLayout(fixedModsPanel);
        fixedModsPanel.setLayout(fixedModsPanelLayout);
        fixedModsPanelLayout.setHorizontalGroup(
            fixedModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fixedModsPanelLayout.createSequentialGroup()
                .addGroup(fixedModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fixedModsPanelLayout.createSequentialGroup()
                        .addComponent(fixedModificationsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                        .addGap(242, 242, 242))
                    .addGroup(fixedModsPanelLayout.createSequentialGroup()
                        .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7)))
                .addGroup(fixedModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeFixedModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addFixedModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        fixedModsPanelLayout.setVerticalGroup(
            fixedModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fixedModsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fixedModificationsLabel)
                .addGap(6, 6, 6)
                .addGroup(fixedModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fixedModsPanelLayout.createSequentialGroup()
                        .addComponent(addFixedModification)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFixedModification)
                        .addContainerGap(59, Short.MAX_VALUE))
                    .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        modificationTypesSplitPane.setLeftComponent(fixedModsPanel);

        variableModsPanel.setOpaque(false);

        variableModificationsLabel.setFont(variableModificationsLabel.getFont().deriveFont((variableModificationsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        variableModificationsLabel.setText("Variable Modifications");

        addVariableModification.setText("<<");
        addVariableModification.setToolTipText("Add as variable modification");
        addVariableModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVariableModificationActionPerformed(evt);
            }
        });

        removeVariableModification.setText(">>");
        removeVariableModification.setToolTipText("Remove as variable modification");
        removeVariableModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeVariableModificationActionPerformed(evt);
            }
        });

        variableModsJScrollPane.setPreferredSize(new java.awt.Dimension(100, 60));

        variableModsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "Mass"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class
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
        variableModsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                variableModsTableMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                variableModsTableMouseExited(evt);
            }
        });
        variableModsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                variableModsTableMouseMoved(evt);
            }
        });
        variableModsJScrollPane.setViewportView(variableModsTable);

        javax.swing.GroupLayout variableModsPanelLayout = new javax.swing.GroupLayout(variableModsPanel);
        variableModsPanel.setLayout(variableModsPanelLayout);
        variableModsPanelLayout.setHorizontalGroup(
            variableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(variableModificationsLabel)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, variableModsPanelLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(variableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addVariableModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeVariableModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        variableModsPanelLayout.setVerticalGroup(
            variableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(variableModsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(variableModificationsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(variableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(variableModsPanelLayout.createSequentialGroup()
                        .addComponent(addVariableModification)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeVariableModification)
                        .addContainerGap(59, Short.MAX_VALUE))
                    .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        modificationTypesSplitPane.setRightComponent(variableModsPanel);

        availableModsPanel.setOpaque(false);

        modificationsListCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Most Used Modifications", "All Modifications" }));
        modificationsListCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificationsListComboActionPerformed(evt);
            }
        });

        modificationsJScrollPane.setPreferredSize(new java.awt.Dimension(100, 60));

        modificationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "Mass", "  "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        modificationsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                modificationsTableMouseMoved(evt);
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
        modificationsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                modificationsTableKeyReleased(evt);
            }
        });
        modificationsJScrollPane.setViewportView(modificationsTable);

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

        javax.swing.GroupLayout availableModsPanelLayout = new javax.swing.GroupLayout(availableModsPanel);
        availableModsPanel.setLayout(availableModsPanelLayout);
        availableModsPanelLayout.setHorizontalGroup(
            availableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(modificationsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
            .addGroup(availableModsPanelLayout.createSequentialGroup()
                .addComponent(modificationsListCombo, 0, 317, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openModificationSettingsJButton)
                .addGap(2, 2, 2))
        );
        availableModsPanelLayout.setVerticalGroup(
            availableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availableModsPanelLayout.createSequentialGroup()
                .addGroup(availableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(modificationsListCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openModificationSettingsJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout modificationsPanelLayout = new javax.swing.GroupLayout(modificationsPanel);
        modificationsPanel.setLayout(modificationsPanelLayout);
        modificationsPanelLayout.setHorizontalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modificationTypesSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(availableModsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        modificationsPanelLayout.setVerticalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addGroup(modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modificationTypesSplitPane)
                    .addComponent(availableModsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        modificationsLayeredPane.add(modificationsPanel);
        modificationsPanel.setBounds(0, 0, 800, 0);

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

        openDialogHelpJButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        openDialogHelpJButton1.setToolTipText("Help");
        openDialogHelpJButton1.setBorder(null);
        openDialogHelpJButton1.setBorderPainted(false);
        openDialogHelpJButton1.setContentAreaFilled(false);
        openDialogHelpJButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openDialogHelpJButton1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                openDialogHelpJButton1MouseExited(evt);
            }
        });
        openDialogHelpJButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDialogHelpJButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(proteaseAndFragmentationPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modificationsLayeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(openDialogHelpJButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(proteaseAndFragmentationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton1)
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
     * Validates the parameters.
     *
     * @param evt
     */
    private void maxMissedCleavagesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxMissedCleavagesTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxMissedCleavagesTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void precursorIonAccuracyTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precursorIonAccuracyTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_precursorIonAccuracyTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void fragmentIonAccuracyTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fragmentIonAccuracyTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_fragmentIonAccuracyTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void minPrecursorChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecursorChargeTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_minPrecursorChargeTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void maxPrecursorChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPrecursorChargeTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxPrecursorChargeTxtKeyReleased

    /**
     * Opens a file chooser where the user can select the database file.
     *
     * @param evt
     */
    private void editDatabaseDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDatabaseDetailsButtonActionPerformed

        // clear the factory
        if (databaseSettingsTxt.getText().trim().length() == 0) {
            try {
                sequenceFactory.clearFactory();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to clear the sequence factory.", "File Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to clear the sequence factory.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        SequenceDbDetailsDialog sequenceDbDetailsDialog = new SequenceDbDetailsDialog(parentFrame, lastSelectedFolder, editable, normalIcon, waitingIcon);

        boolean success = sequenceDbDetailsDialog.selectDB(true);
        if (success) {
            sequenceDbDetailsDialog.setVisible(true);
        }

        if (sequenceFactory.getCurrentFastaFile() != null) {
            databaseSettingsTxt.setText(sequenceFactory.getCurrentFastaFile().getAbsolutePath());
        }

        validateParametersInput(false);
    }//GEN-LAST:event_editDatabaseDetailsButtonActionPerformed

    /**
     * Add fixed modifications.
     *
     * @param evt
     */
    private void addFixedModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFixedModificationActionPerformed
        int nSelected = fixedModsTable.getRowCount();
        int nNew = modificationsTable.getSelectedRows().length;
        String[] fixedModifications = new String[nSelected + nNew];
        int cpt = 0;

        for (int i = 0; i < nSelected; i++) {
            fixedModifications[cpt] = (String) fixedModsTable.getValueAt(i, 1);
            cpt++;
        }

        for (int selectedRow : modificationsTable.getSelectedRows()) {
            String name = (String) modificationsTable.getValueAt(selectedRow, 1);
            boolean found = false;
            for (int i = 0; i < fixedModsTable.getModel().getRowCount(); i++) {
                if (((String) fixedModsTable.getValueAt(i, 1)).equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fixedModifications[cpt] = name;
                cpt++;
                if (!modificationUse.contains(name)) {
                    modificationUse.add(name);
                }
            }
        }

        DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
        fixedModel.getDataVector().removeAllElements();

        for (String fixedMod : fixedModifications) {
            ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getPtmSettings().getColor(fixedMod), fixedMod, ptmFactory.getPTM(fixedMod).getMass()});
        }
        ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
        fixedModsTable.repaint();

        fixedModificationsLabel.setText("Fixed Modifications (" + fixedModifications.length + ")");
        updateModificationList();
    }//GEN-LAST:event_addFixedModificationActionPerformed

    /**
     * Remove fixed modifications.
     *
     * @param evt
     */
    private void removeFixedModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFixedModificationActionPerformed
        int nSelected = fixedModsTable.getRowCount();
        int nToRemove = fixedModsTable.getSelectedRows().length;
        String[] fixedModifications = new String[nSelected - nToRemove];
        int cpt = 0;

        for (int i = 0; i < fixedModsTable.getRowCount(); i++) {
            boolean found = false;
            for (int selectedRow : fixedModsTable.getSelectedRows()) {
                if (((String) fixedModsTable.getValueAt(i, 1)).equals((String) fixedModsTable.getValueAt(selectedRow, 1))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fixedModifications[cpt] = (String) fixedModsTable.getValueAt(i, 1);
                cpt++;
            }
        }

        DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
        fixedModel.getDataVector().removeAllElements();

        for (String fixedMod : fixedModifications) {
            ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getPtmSettings().getColor(fixedMod), fixedMod, ptmFactory.getPTM(fixedMod).getMass()});
        }
        ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
        fixedModsTable.repaint();

        fixedModificationsLabel.setText("Fixed Modifications (" + fixedModifications.length + ")");
        updateModificationList();
    }//GEN-LAST:event_removeFixedModificationActionPerformed

    /**
     * Add variable modifications.
     *
     * @param evt
     */
    private void addVariableModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVariableModificationActionPerformed
        int nSelected = variableModsTable.getRowCount();
        int nNew = modificationsTable.getSelectedRows().length;
        String[] variableModifications = new String[nSelected + nNew];
        int cpt = 0;

        for (int i = 0; i < nSelected; i++) {
            variableModifications[cpt] = (String) variableModsTable.getValueAt(i, 1);
            cpt++;
        }

        for (int selectedRow : modificationsTable.getSelectedRows()) {
            String name = (String) modificationsTable.getValueAt(selectedRow, 1);
            boolean found = false;
            for (int i = 0; i < variableModsTable.getRowCount(); i++) {
                if (((String) variableModsTable.getValueAt(i, 1)).equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                variableModifications[cpt] = name;
                cpt++;
                if (!modificationUse.contains(name)) {
                    modificationUse.add(name);
                }
            }
        }

        DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
        variableModel.getDataVector().removeAllElements();

        for (String variabledMod : variableModifications) {
            ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getPtmSettings().getColor(variabledMod), variabledMod, ptmFactory.getPTM(variabledMod).getMass()});
        }
        ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
        variableModsTable.repaint();

        variableModificationsLabel.setText("Variable Modifications (" + variableModifications.length + ")");

        if (variableModifications.length > 6) {
            JOptionPane.showMessageDialog(this,
                    "It is not recommended to use more than 6 variable modifications in the same search.", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        updateModificationList();
    }//GEN-LAST:event_addVariableModificationActionPerformed

    /**
     * Remove variable modifications.
     *
     * @param evt
     */
    private void removeVariableModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeVariableModificationActionPerformed
        int nSelected = variableModsTable.getRowCount();
        int nToRemove = variableModsTable.getSelectedRows().length;
        String[] variableModifications = new String[nSelected - nToRemove];
        int cpt = 0;

        for (int i = 0; i < variableModsTable.getRowCount(); i++) {
            boolean found = false;
            for (int selectedRow : variableModsTable.getSelectedRows()) {
                if (((String) variableModsTable.getValueAt(i, 1)).equals((String) variableModsTable.getValueAt(selectedRow, 1))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                variableModifications[cpt] = (String) variableModsTable.getValueAt(i, 1);
                cpt++;
            }
        }

        DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
        variableModel.getDataVector().removeAllElements();

        for (String variabledMod : variableModifications) {
            ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getPtmSettings().getColor(variabledMod), variabledMod, ptmFactory.getPTM(variabledMod).getMass()});
        }
        ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
        variableModsTable.repaint();

        variableModificationsLabel.setText("Variable Modifications (" + variableModifications.length + ")");
        updateModificationList();
    }//GEN-LAST:event_removeVariableModificationActionPerformed

    /**
     * Make sure that the fixed and variable modification panels have equal
     * size.
     *
     * @param evt
     */
    private void modificationTypesSplitPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_modificationTypesSplitPaneComponentResized
        modificationTypesSplitPane.setDividerLocation(0.5);
    }//GEN-LAST:event_modificationTypesSplitPaneComponentResized

    /**
     * Update the modification lists.
     *
     * @param evt
     */
    private void modificationsListComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificationsListComboActionPerformed
        updateModificationList();
    }//GEN-LAST:event_modificationsListComboActionPerformed

    /**
     * Close the window without saving the changes.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Save the changes and then close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Validate the parameters.
     *
     * @param evt
     */
    private void enzymesCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enzymesCmbActionPerformed
        validateParametersInput(false);
    }//GEN-LAST:event_enzymesCmbActionPerformed

    /**
     * Resize the layered panes.
     *
     * @param evt
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // resize the plot area
        modificationsLayeredPane.getComponent(0).setBounds(0, 0, modificationsLayeredPane.getWidth(), modificationsLayeredPane.getHeight());
        modificationsLayeredPane.revalidate();
        modificationsLayeredPane.repaint();
    }//GEN-LAST:event_formComponentResized

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void fixedModsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixedModsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_fixedModsTableMouseExited

    /**
     * Changes the cursor to a hand cursor if over the color column.
     *
     * @param evt
     */
    private void fixedModsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixedModsTableMouseMoved
        int row = fixedModsTable.rowAtPoint(evt.getPoint());
        int column = fixedModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            String ptmName = (String) fixedModsTable.getValueAt(row, fixedModsTable.getColumn("Name").getModelIndex());
            PTM ptm = ptmFactory.getPTM(ptmName);
            fixedModsTable.setToolTipText(ptm.getHtmlTooltip());

            if (column == fixedModsTable.getColumn(" ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        } else {
            fixedModsTable.setToolTipText(null);
        }
    }//GEN-LAST:event_fixedModsTableMouseMoved

    /**
     * Opens a file chooser where the color for the PTM can be changed.
     *
     * @param evt
     */
    private void fixedModsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixedModsTableMouseReleased
        int row = fixedModsTable.rowAtPoint(evt.getPoint());
        int column = fixedModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == fixedModsTable.getColumn(" ").getModelIndex()) {
                Color newColor = JColorChooser.showDialog(this, "Pick a Color", (Color) fixedModsTable.getValueAt(row, column));

                if (newColor != null) {
                    searchParameters.getPtmSettings().setColor((String) fixedModsTable.getValueAt(row, 1), newColor);
                    fixedModsTable.setValueAt(newColor, row, 0);
                    ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
                    fixedModsTable.repaint();
                }
            }
        }

        enableAddRemoveButtons();
    }//GEN-LAST:event_fixedModsTableMouseReleased

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void modificationsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsTableMouseExited

    /**
     * Changes the cursor to a hand cursor if over the color column.
     *
     * @param evt
     */
    private void modificationsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseMoved
        int row = modificationsTable.rowAtPoint(evt.getPoint());
        int column = modificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            String ptmName = (String) modificationsTable.getValueAt(row, modificationsTable.getColumn("Name").getModelIndex());
            PTM ptm = ptmFactory.getPTM(ptmName);
            modificationsTable.setToolTipText(ptm.getHtmlTooltip());

            if (column == modificationsTable.getColumn(" ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        } else {
            modificationsTable.setToolTipText(null);
        }
    }//GEN-LAST:event_modificationsTableMouseMoved

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
            } else if (modificationsListCombo.getSelectedIndex() == 1
                    && column == modificationsTable.getColumn("  ").getModelIndex()
                    && modificationsTable.getValueAt(row, column) != null) {

                boolean selected = (Boolean) modificationsTable.getValueAt(row, column);
                String ptmName = (String) modificationsTable.getValueAt(row, 1);

                // change if the ptm is considered as default
                if (modificationsListCombo.getSelectedIndex() == 0) {
                    // remove from default ptm set
                    modificationUse.remove(ptmName);
                } else {
                    if (selected) {
                        // add to default ptm set
                        if (!modificationUse.contains(ptmName)) {
                            modificationUse.add(ptmName);
                        }
                    } else {
                        // remove from default ptm set
                        modificationUse.remove(ptmName);
                    }
                }

                Point viewPosition = modificationsJScrollPane.getViewport().getViewPosition();

                updateModificationList();

                if (row < modificationsTable.getRowCount()) {
                    modificationsTable.setRowSelectionInterval(row, row);
                } else if (row - 1 < modificationsTable.getRowCount() && row >= 0) {
                    modificationsTable.setRowSelectionInterval(row - 1, row - 1);
                }

                modificationsJScrollPane.getViewport().setViewPosition(viewPosition);
                modificationsJScrollPane.repaint();
            }

            enableAddRemoveButtons();
        }
    }//GEN-LAST:event_modificationsTableMouseReleased

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void variableModsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_variableModsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_variableModsTableMouseExited

    /**
     * Changes the cursor to a hand cursor if over the color column.
     *
     * @param evt
     */
    private void variableModsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_variableModsTableMouseMoved
        int row = variableModsTable.rowAtPoint(evt.getPoint());
        int column = variableModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            String ptmName = (String) variableModsTable.getValueAt(row, variableModsTable.getColumn("Name").getModelIndex());
            PTM ptm = ptmFactory.getPTM(ptmName);
            variableModsTable.setToolTipText(ptm.getHtmlTooltip());

            if (column == variableModsTable.getColumn(" ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        } else {
            variableModsTable.setToolTipText(null);
        }
    }//GEN-LAST:event_variableModsTableMouseMoved

    /**
     * Opens a file chooser where the color for the PTM can be changed.
     *
     * @param evt
     */
    private void variableModsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_variableModsTableMouseReleased
        int row = variableModsTable.rowAtPoint(evt.getPoint());
        int column = variableModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == variableModsTable.getColumn(" ").getModelIndex()) {
                Color newColor = JColorChooser.showDialog(this, "Pick a Color", (Color) variableModsTable.getValueAt(row, column));

                if (newColor != null) {
                    searchParameters.getPtmSettings().setColor((String) variableModsTable.getValueAt(row, 1), newColor);
                    variableModsTable.setValueAt(newColor, row, 0);
                    ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
                    variableModsTable.repaint();
                }
            }
        }

        enableAddRemoveButtons();
    }//GEN-LAST:event_variableModsTableMouseReleased

    /**
     * Open the modifications pop up menu.
     *
     * @param evt
     */
    private void openModificationSettingsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openModificationSettingsJButtonActionPerformed
        new ModificationsDialog(parentFrame, true);
        updateModificationList();
    }//GEN-LAST:event_openModificationSettingsJButtonActionPerformed

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void openModificationSettingsJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openModificationSettingsJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openModificationSettingsJButtonMouseExited

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void openModificationSettingsJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openModificationSettingsJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_openModificationSettingsJButtonMouseEntered

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void openDialogHelpJButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButton1ActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/SearchSettingsDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                normalIcon,
                "Search Settings Help", 500, 100);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButton1ActionPerformed

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButton1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButton1MouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButton1MouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButton1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButton1MouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButton1MouseExited

    /**
     * Jump to the row with the PTM starting with the typed letters.
     *
     * @param evt
     */
    private void modificationsTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_modificationsTableKeyReleased

        char currentChar = evt.getKeyChar();

        if (Character.isLetterOrDigit(currentChar) || Character.isWhitespace(currentChar)) {

            keyPressedCounter++;
            currentPtmSearchString += currentChar;

            new Thread("FindThread") {
                @Override
                public synchronized void run() {

                    try {
                        wait(waitingTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        // see if the gui is to be updated or not
                        if (keyPressedCounter == 1) {

                            // search in the ptm table
                            for (int i = 0; i < modificationsTable.getRowCount(); i++) {

                                String currentPtmName = ((String) modificationsTable.getValueAt(i, modificationsTable.getColumn("Name").getModelIndex())).toLowerCase();

                                if (currentPtmName.startsWith(currentPtmSearchString.toLowerCase())) {
                                    modificationsTable.scrollRectToVisible(modificationsTable.getCellRect(i, i, false));
                                    modificationsTable.repaint();
                                    modificationsTable.setRowSelectionInterval(i, i);
                                    modificationsTable.repaint();
                                    break;
                                }
                            }

                            // gui updated, reset the counter
                            keyPressedCounter = 0;
                            currentPtmSearchString = "";
                        } else {
                            // gui not updated, decrease the counter
                            keyPressedCounter--;
                        }
                    } catch (Exception e) {
                        keyPressedCounter = 0;
                        currentPtmSearchString = "";
                        modificationsTable.repaint();
                    }
                }
            }.start();
        }
    }//GEN-LAST:event_modificationsTableKeyReleased

    /**
     * Close the window without saving the changes.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFixedModification;
    private javax.swing.JButton addVariableModification;
    private javax.swing.JPanel availableModsPanel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataBasePanelSettings;
    private javax.swing.JLabel databaseSettingsLbl;
    private javax.swing.JTextField databaseSettingsTxt;
    private javax.swing.JButton editDatabaseDetailsButton;
    private javax.swing.JLabel enzymeLabel;
    private javax.swing.JComboBox enzymesCmb;
    private javax.swing.JLabel fixedModificationsLabel;
    private javax.swing.JScrollPane fixedModsJScrollPane;
    private javax.swing.JPanel fixedModsPanel;
    private javax.swing.JTable fixedModsTable;
    private javax.swing.JComboBox fragmentIon1Cmb;
    private javax.swing.JComboBox fragmentIon2Cmb;
    private javax.swing.JTextField fragmentIonAccuracyTxt;
    private javax.swing.JLabel fragmentIonLbl;
    private javax.swing.JLabel fragmentIonType1Lbl;
    private javax.swing.JLabel maxMissedCleavagesLabel;
    private javax.swing.JTextField maxMissedCleavagesTxt;
    private javax.swing.JTextField maxPrecursorChargeTxt;
    private javax.swing.JTextField minPrecursorChargeTxt;
    private javax.swing.JSplitPane modificationTypesSplitPane;
    private javax.swing.JScrollPane modificationsJScrollPane;
    private javax.swing.JLayeredPane modificationsLayeredPane;
    private javax.swing.JComboBox modificationsListCombo;
    private javax.swing.JPanel modificationsPanel;
    private javax.swing.JTable modificationsTable;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton1;
    private javax.swing.JButton openModificationSettingsJButton;
    private javax.swing.JLabel precursorChargeLbl;
    private javax.swing.JLabel precursorChargeRangeLabel;
    private javax.swing.JTextField precursorIonAccuracyTxt;
    private javax.swing.JLabel precursorIonLbl;
    private javax.swing.JComboBox precursorIonUnit;
    private javax.swing.JPanel proteaseAndFragmentationPanel;
    private javax.swing.JButton removeFixedModification;
    private javax.swing.JButton removeVariableModification;
    private javax.swing.JLabel variableModificationsLabel;
    private javax.swing.JScrollPane variableModsJScrollPane;
    private javax.swing.JPanel variableModsPanel;
    private javax.swing.JTable variableModsTable;
    // End of variables declaration//GEN-END:variables

    /**
     * Loads the implemented enzymes.
     *
     * @return the list of enzyme names
     */
    private String[] loadEnzymes() {

        ArrayList<String> tempEnzymes = new ArrayList<String>();

        for (int i = 0; i < enzymeFactory.getEnzymes().size(); i++) {
            tempEnzymes.add(enzymeFactory.getEnzymes().get(i).getName());
        }

        Collections.sort(tempEnzymes);

        String[] enzymes = new String[tempEnzymes.size() + 1];

        enzymes[0] = "--- Select ---";

        for (int i = 0; i < tempEnzymes.size(); i++) {
            enzymes[i + 1] = tempEnzymes.get(i);
        }

        return enzymes;
    }

    /**
     * This method takes the specified search parameters instance and reads the
     * values for (some of) the GUI components from it.
     */
    private void setScreenProps() {

        File fastaFile = searchParameters.getFastaFile();
        if (fastaFile != null) {
            String fastaPath = fastaFile.getAbsolutePath();
            databaseSettingsTxt.setText(fastaPath);
            if (!fastaFile.equals(sequenceFactory.getCurrentFastaFile()) && fastaFile.exists()) {
                loadFastaFile(fastaFile);
            }
        }

        ArrayList<String> missingPtms = new ArrayList<String>();
        PtmSettings modificationProfile = searchParameters.getPtmSettings();
        if (modificationProfile != null) {
            ArrayList<String> fixedMods = modificationProfile.getFixedModifications();

            for (String ptmName : fixedMods) {
                if (!ptmFactory.containsPTM(ptmName)) {
                    missingPtms.add(ptmName);
                }
            }

            for (String missing : missingPtms) {
                fixedMods.remove(missing);
            }

            if (!missingPtms.isEmpty()) {
                if (missingPtms.size() == 1) {
                    JOptionPane.showMessageDialog(this, "The following modification is currently not recognized by SearchGUI: "
                            + missingPtms.get(0) + ".\nPlease import it in the Modification Editor.", "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                } else {
                    String output = "The following modifications are currently not recognized by SearchGUI:\n";
                    boolean first = true;

                    for (String ptm : missingPtms) {
                        if (first) {
                            first = false;
                        } else {
                            output += ", ";
                        }
                        output += ptm;
                    }

                    output += ".\nPlease import them in the Modification Editor.";
                    JOptionPane.showMessageDialog(this, output, "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }

            DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
            fixedModel.getDataVector().removeAllElements();

            for (String fixedMod : fixedMods) {
                ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getPtmSettings().getColor(fixedMod), fixedMod, ptmFactory.getPTM(fixedMod).getMass()});
            }
            ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
            fixedModsTable.repaint();
            fixedModificationsLabel.setText("Fixed Modifications (" + fixedMods.size() + ")");

            ArrayList<String> variableMods = modificationProfile.getVariableModifications();

            for (String ptmName : variableMods) {
                if (!ptmFactory.containsPTM(ptmName)) {
                    missingPtms.add(ptmName);
                }
            }

            for (String missing : missingPtms) {
                variableMods.remove(missing);
            }

            if (!missingPtms.isEmpty()) {
                if (missingPtms.size() == 1) {
                    JOptionPane.showMessageDialog(this, "The following modification is currently not recognized by SearchGUI: "
                            + missingPtms.get(0) + ".\nPlease import it in the Modification Editor.", "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                } else {
                    String output = "The following modifications are currently not recognized by SearchGUI:\n";
                    boolean first = true;

                    for (String ptm : missingPtms) {
                        if (first) {
                            first = false;
                        } else {
                            output += ", ";
                        }
                        output += ptm;
                    }

                    output += ".\nPlease import them in the Modification Editor.";
                    JOptionPane.showMessageDialog(this, output, "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
            DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
            variableModel.getDataVector().removeAllElements();
            for (String variableMod : variableMods) {
                ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getPtmSettings().getColor(variableMod), variableMod, ptmFactory.getPTM(variableMod).getMass()});
            }
            ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
            variableModsTable.repaint();
            variableModificationsLabel.setText("Variable Modifications (" + variableMods.size() + ")");

            updateModificationList();
        }

        Enzyme enzyme = searchParameters.getEnzyme();
        if (enzyme != null) {
            String enzymeName = enzyme.getName();

            if (!enzymeFactory.enzymeLoaded(enzymeName)) {
                enzymeFactory.addEnzyme(searchParameters.getEnzyme());
            }
            enzymesCmb.setSelectedItem(enzymeName);
        } else {
            enzymesCmb.setSelectedIndex(0);
        }

        if (searchParameters.getIonSearched1() != null) {
            fragmentIon1Cmb.setSelectedItem(PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched1()));
        }

        if (searchParameters.getIonSearched2() != null) {
            fragmentIon2Cmb.setSelectedItem(PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched2()));
        }

        if (searchParameters.getnMissedCleavages() != null) {
            maxMissedCleavagesTxt.setText(searchParameters.getnMissedCleavages() + "");
        }

        if (searchParameters.getPrecursorAccuracy() != null) {
            precursorIonAccuracyTxt.setText(searchParameters.getPrecursorAccuracy() + "");
        }

        if (searchParameters.getPrecursorAccuracyType() != null) {
            if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
                precursorIonUnit.setSelectedItem("ppm");
            } else if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {
                precursorIonUnit.setSelectedItem("Da");
            }
        }

        if (searchParameters.getFragmentIonAccuracy() != null) {
            fragmentIonAccuracyTxt.setText(searchParameters.getFragmentIonAccuracy() + "");
        }

        if (searchParameters.getMinChargeSearched() != null) {
            minPrecursorChargeTxt.setText(searchParameters.getMinChargeSearched().value + "");
        }

        if (searchParameters.getMaxChargeSearched() != null) {
            maxPrecursorChargeTxt.setText(searchParameters.getMaxChargeSearched().value + "");
        }
    }

    /**
     * Loads the FASTA file in the factory.
     *
     * @param file the FASTA file
     */
    private void loadFastaFile(File file) {

        final File finalFile = file;

        progressDialog = new ProgressDialogX(this, parentFrame, normalIcon, waitingIcon, true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Loading Database. Please Wait...");

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("importThread") {
            public void run() {

                try {
                    progressDialog.setTitle("Importing Database. Please Wait...");
                    progressDialog.setPrimaryProgressCounterIndeterminate(false);
                    sequenceFactory.loadFastaFile(finalFile, progressDialog);
                } catch (IOException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SearchSettingsDialog.this,
                            new String[]{"FASTA Import Error.", "File " + finalFile.getAbsolutePath() + " not found."},
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (ClassNotFoundException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SearchSettingsDialog.this,
                            new String[]{"FASTA Import Error.", "File index of " + finalFile.getName() + " could not be imported. Please contact the developers."},
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (StringIndexOutOfBoundsException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SearchSettingsDialog.this,
                            e.getMessage(),
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (IllegalArgumentException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SearchSettingsDialog.this,
                            e.getMessage(),
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                }
                progressDialog.setRunFinished();
            }
        }.start();
    }

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateParametersInput(boolean showMessage) {

        boolean valid = true;
        databaseSettingsLbl.setForeground(Color.BLACK);
        enzymeLabel.setForeground(Color.BLACK);

        databaseSettingsLbl.setToolTipText(null);
        enzymeLabel.setToolTipText(null);

        if (databaseSettingsTxt.getText() == null || databaseSettingsTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a search database.", "Search Database Not Found", JOptionPane.WARNING_MESSAGE);
            }
            databaseSettingsLbl.setForeground(Color.RED);
            databaseSettingsLbl.setToolTipText("Please select a valid '.fasta' or '.fas' database file");
            valid = false;
        } else {
            File test = new File(databaseSettingsTxt.getText().trim());
            if (!test.exists()) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "The database file could not be found.", "Search Database Not Found", JOptionPane.WARNING_MESSAGE);
                }
                databaseSettingsLbl.setForeground(Color.RED);
                databaseSettingsLbl.setToolTipText("Database file could not be found!");
                valid = false;
            }
        }

        // validate missed cleavages, precursor mass tolerances, fragment mass tolerances and precursor charges
        valid = GuiUtilities.validateIntegerInput(this, maxMissedCleavagesLabel, maxMissedCleavagesTxt, "number of allowed missed cleavages", "Missed Cleavages Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, precursorIonLbl, precursorIonAccuracyTxt, "precursor mass tolerance", "Precursor Mass Tolerance Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, fragmentIonLbl, fragmentIonAccuracyTxt, "fragment mass tolerance", "Fragment Mass Tolerance Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, precursorChargeLbl, minPrecursorChargeTxt, "lower bound for the precursor charge", "Precursor Charge Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, precursorChargeLbl, maxPrecursorChargeTxt, "upper bound for the precursor charge", "Precursor Charge Error", true, showMessage, valid);

        // make sure that the lower charge is smaller than the upper charge
        try {
            double chargeLowerBound = Integer.parseInt(minPrecursorChargeTxt.getText().trim());
            double chargeUpperBound = Integer.parseInt(maxPrecursorChargeTxt.getText().trim());

            if (chargeUpperBound < chargeLowerBound) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "The minimum precursor charge must be lower than or equal to the maximum precursor charge.",
                            "Precursor Charge Error", JOptionPane.WARNING_MESSAGE);
                }
                valid = false;
                precursorChargeLbl.setForeground(Color.RED);
                precursorChargeLbl.setToolTipText("Minimum precursor charge > Maximum precursor charge!");
            }

        } catch (NumberFormatException e) {
            // ignore, error already caught above
        }

        // valdiate that an enzyme is selected
        if (enzymesCmb.getSelectedIndex() == 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "Please select an enzyme.", "Enzyme Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            enzymeLabel.setForeground(Color.RED);
            enzymeLabel.setToolTipText("No enzyme selected!");
        }

        okButton.setEnabled(valid);

        return valid;
    }

    /**
     * Returns a SearchParameters instance based on the user input in the GUI.
     *
     * @return a SearchParameters instance based on the user input in the GUI
     */
    public SearchParameters getSearchParameters() {

        SearchParameters tempSearchParameters = new SearchParameters(searchParameters);

        String dbPath = databaseSettingsTxt.getText().trim();
        if (!dbPath.equals("")) {
            File fastaFile = new File(databaseSettingsTxt.getText().trim());
            tempSearchParameters.setFastaFile(fastaFile);
        }

        Enzyme enzyme = enzymeFactory.getEnzyme(enzymesCmb.getSelectedItem().toString());
        tempSearchParameters.setEnzyme(enzyme);

        double fragmentAccuracy = new Double(fragmentIonAccuracyTxt.getText().trim());

        boolean acetylConflict = false;
        boolean pyroConflict = false;
        PtmSettings modificationProfile = new PtmSettings();
        for (int i = 0; i < fixedModsTable.getRowCount(); i++) {
            String modName = (String) fixedModsTable.getValueAt(i, 1);
            PTM ptm = ptmFactory.getPTM(modName);
            modificationProfile.addFixedModification(ptm);
            modificationProfile.addRefinementFixedModification(ptm);
            modificationProfile.setColor(modName, (Color) fixedModsTable.getValueAt(i, 0));
            if ((ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA) && Math.abs(ptm.getMass() - 42.010565) < fragmentAccuracy) {
                acetylConflict = true;
            }
            if ((ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA) && Math.abs(ptm.getMass() + 17.026549) < fragmentAccuracy) {
                pyroConflict = true;
            }
        }

        for (int i = 0; i < variableModsTable.getRowCount(); i++) {
            String modName = (String) variableModsTable.getValueAt(i, 1);
            modificationProfile.addVariableModification(ptmFactory.getPTM(modName));
            modificationProfile.setColor(modName, (Color) variableModsTable.getValueAt(i, 0));
        }

        tempSearchParameters.setPtmSettings(modificationProfile);

        tempSearchParameters.setnMissedCleavages(new Integer(maxMissedCleavagesTxt.getText().trim()));
        tempSearchParameters.setPrecursorAccuracy(new Double(precursorIonAccuracyTxt.getText().trim()));
        if (precursorIonUnit.getSelectedIndex() == 0) {
            tempSearchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
        } else {
            tempSearchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
        }
        tempSearchParameters.setFragmentIonAccuracy(fragmentAccuracy);
        tempSearchParameters.setIonSearched1(fragmentIon1Cmb.getSelectedItem().toString().trim());
        tempSearchParameters.setFragmentIonAccuracy(new Double(fragmentIonAccuracyTxt.getText().trim()));
        tempSearchParameters.setIonSearched2(fragmentIon2Cmb.getSelectedItem().toString().trim());
        int charge = new Integer(minPrecursorChargeTxt.getText().trim());
        tempSearchParameters.setMinChargeSearched(new Charge(Charge.PLUS, charge));
        charge = new Integer(maxPrecursorChargeTxt.getText().trim());
        tempSearchParameters.setMaxChargeSearched(new Charge(Charge.PLUS, charge));

        // Adapt X!Tandem options
        XtandemParameters xtandemParameters = (XtandemParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex());
        if (xtandemParameters != null) {
            xtandemParameters.setProteinQuickAcetyl(!acetylConflict);
            xtandemParameters.setQuickPyrolidone(!pyroConflict);
        }

        // Adapt Comet options
        CometParameters cometParameters = (CometParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex());
        if (cometParameters != null) {
            double binoffset = fragmentAccuracy / 2;
            cometParameters.setFragmentBinOffset(binoffset);
        }

        return tempSearchParameters;
    }

    /**
     * Updates the modification list (right).
     */
    private void updateModificationList() {
        ArrayList<String> allModificationsList = new ArrayList<String>();
        if (modificationsListCombo.getSelectedIndex() == 0) {
            for (String name : modificationUse) {
                if (modificationUse.contains(name)) {
                    allModificationsList.add(name);
                }
            }
        } else {
            allModificationsList = ptmFactory.getPTMs();
        }

        int nFixed = fixedModsTable.getRowCount();
        int nVariable = variableModsTable.getRowCount();
        ArrayList<String> allModifications = new ArrayList<String>();

        for (String name : allModificationsList) {
            boolean found = false;
            for (int j = 0; j < nFixed; j++) {
                if (((String) fixedModsTable.getValueAt(j, 1)).equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (int j = 0; j < nVariable; j++) {
                    if (((String) variableModsTable.getValueAt(j, 1)).equals(name)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                allModifications.add(name);
            }
        }

        String[] allModificationsAsArray = new String[allModifications.size()];

        for (int i = 0; i < allModifications.size(); i++) {
            allModificationsAsArray[i] = allModifications.get(i);
        }

        Arrays.sort(allModificationsAsArray);

        if (modificationsListCombo.getSelectedIndex() == 0) {
            modificationsTable.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{},
                    new String[]{
                        " ", "Name", "Mass"
                    }
            ) {
                Class[] types = new Class[]{
                    java.lang.Object.class, java.lang.String.class, java.lang.Double.class
                };
                boolean[] canEdit = new boolean[]{
                    false, false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        } else {
            modificationsTable.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{},
                    new String[]{
                        " ", "Name", "Mass", "  "
                    }
            ) {
                Class[] types = new Class[]{
                    java.lang.Object.class, java.lang.String.class, java.lang.Double.class, java.lang.Boolean.class
                };
                boolean[] canEdit = new boolean[]{
                    false, false, false, true
                };

                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        for (String mod : allModificationsAsArray) {
            ((DefaultTableModel) modificationsTable.getModel()).addRow(new Object[]{ptmFactory.getColor(mod), mod, ptmFactory.getPTM(mod).getMass(), modificationUse.contains(mod)});
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
        fixedModsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) fixedModsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);
        variableModsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) variableModsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);

        if (modificationsTable.getRowCount() > 0) {
            //modificationsTable.setRowSelectionInterval(0, 0);
            modificationsTable.scrollRectToVisible(modificationsTable.getCellRect(0, 0, false));
            modificationsTable.requestFocus();
        }

        // enable/disable the add/remove ptm buttons
        enableAddRemoveButtons();
    }

    /**
     * Enable/disable the add/remove PTM buttons.
     */
    private void enableAddRemoveButtons() {
        removeVariableModification.setEnabled(variableModsTable.getSelectedRow() != -1 && editable);
        addVariableModification.setEnabled(modificationsTable.getSelectedRow() != -1 && editable);
        removeFixedModification.setEnabled(fixedModsTable.getSelectedRow() != -1 && editable);
        addFixedModification.setEnabled(modificationsTable.getSelectedRow() != -1 && editable);
    }

    /**
     * Updates the tooltip to the selected modification.
     *
     * @param list the list to update the tooltip for
     * @param evt the mouse event used to locate the item the mouse is hovering
     * over
     */
    private void updateListToolTip(JList list, java.awt.event.MouseEvent evt) {

        // @TODO: reimplement me??
        String toolTip = null;

        int index = list.locationToIndex(evt.getPoint());
        Rectangle bounds = list.getCellBounds(index, index);

        // update tooltips
        if (index != -1 && bounds.contains(evt.getPoint())) {
            String name = (String) list.getModel().getElementAt(index);

            PTM ptm = ptmFactory.getPTM(name);

            String residuesAsString = "";

            if (ptm.getType() == PTM.MODN) {
                residuesAsString += "protein N-term";
            } else if (ptm.getType() == PTM.MODNP) {
                residuesAsString += "peptide N-term";
            } else if (ptm.getType() == PTM.MODNAA) {
                residuesAsString += "protein starting by " + ptm.getPattern().toString();
            } else if (ptm.getType() == PTM.MODNPAA) {
                residuesAsString += "peptide starting by " + ptm.getPattern().toString();
            }
            if (ptm.getType() == PTM.MODC) {
                residuesAsString += "protein C-term";
            } else if (ptm.getType() == PTM.MODCP) {
                residuesAsString += "peptide C-term";
            } else if (ptm.getType() == PTM.MODCAA) {
                residuesAsString += "protein ending by " + ptm.getPattern().toString();
            } else if (ptm.getType() == PTM.MODCPAA) {
                residuesAsString += "peptide ending by " + ptm.getPattern().toString();
            } else if (ptm.getType() == PTM.MODAA) {
                residuesAsString += ptm.getPattern().toString();
            }

            toolTip = "<html>"
                    + "<table border=\"0\">"
                    + "<tr>"
                    + "<td>Name:</td>"
                    + "<td>" + ptm.getName() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Mass:</td>"
                    + "<td>" + ptm.getRoundedMass() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Target:</td>"
                    + "<td>" + residuesAsString + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</html>";
        }

        list.setToolTipText(toolTip);
    }

    /**
     * Returns a string with the modifications used.
     *
     * @param configurationFile the file to load the modifications from
     *
     * @return a list with the modifications used
     *
     * @throws java.io.IOException exception thrown whenever an error occurred
     * when loading the modifications from the file
     */
    public static ArrayList<String> loadModificationsUse(ConfigurationFile configurationFile) throws IOException {

        ArrayList<String> modificationUse = new ArrayList<String>();

        String modificationLine = configurationFile.getParameterLine(modificationUseInConfiguration);
        if (modificationLine != null) {
            // Split the different modifications.
            int start;

            ArrayList<String> modificationUses = new ArrayList<String>();
            while ((start = modificationLine.indexOf(MODIFICATION_SEPARATOR)) >= 0) {
                String name = modificationLine.substring(0, start);
                modificationLine = modificationLine.substring(start + 2);
                if (!name.trim().equals("")) {
                    modificationUses.add(name);
                }
            }

            for (String name : modificationUses) {
                String modificationName = name;

                if (PTMFactory.getInstance().containsPTM(modificationName)) {
                    modificationUse.add(modificationName);
                }
            }
        }

        return modificationUse;
    }

    /**
     * Saves the modification use to the given configuration file.
     *
     * @param configurationFile the configuration file where to save the
     * modification use
     * @param modificationUse the modification use
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the modification file
     */
    public static void saveModificationUse(ConfigurationFile configurationFile, ArrayList<String> modificationUse) throws IOException {
        String modificationUseAsString = getModificationUseAsString(modificationUse);
        configurationFile.setParameter(modificationUseInConfiguration, modificationUseAsString);
    }

    /**
     * Returns a line with the most used modifications.
     *
     * @param modificationUse the list of modifications
     * @return a line containing the most used modifications
     */
    public static String getModificationUseAsString(ArrayList<String> modificationUse) {
        String result = "";
        for (String name : modificationUse) {
            result += name + MODIFICATION_SEPARATOR;
        }
        return result;
    }

    /**
     * Saves the user preferences and closes the dialog.
     */
    private void close() {
        try {
            saveModificationUse(configurationFile, modificationUse);
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }
        dispose();
    }

    /**
     * Loads the modification use from a configuration file.
     *
     * @param configurationFile the configuration file
     */
    private void loadModificationUse(ConfigurationFile configurationFile) throws IOException {
        modificationUse = loadModificationsUse(configurationFile);
    }

    /**
     * Indicates whether the cancel button was pressed by the user.
     *
     * @return a boolean indicating whether the cancel button was pressed by the
     * user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Saves the search parameters to a file of the users choice.
     *
     * @param parentDialog the parent dialog
     * @param newSearchParameters the current search parameters
     * @param searchParametersFile the file where to save the parameters
     * @param lastSelectedFolder the last selected folder
     *
     * @return the file where the new settings were saved, null if not saved
     */
    public static File saveSearchParameters(JDialog parentDialog, SearchParameters newSearchParameters, File searchParametersFile,
            LastSelectedFolder lastSelectedFolder) {

        int value = JOptionPane.showConfirmDialog(parentDialog, "The search parameters have changed."
                + "\nDo you want to save the changes?", "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);

        if (value == JOptionPane.YES_OPTION) {

            boolean userSelectFile = false;

            // see if the user wants to overwrite the current settings file
            if (searchParametersFile != null) {
                value = JOptionPane.showConfirmDialog(parentDialog, "Overwrite current settings file?", "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.NO_OPTION) {
                    userSelectFile = true;
                } else if (value == JOptionPane.CANCEL_OPTION || value == JOptionPane.CLOSED_OPTION) {
                    return null;
                }

            } else {
                // no params file > have the user select a file
                userSelectFile = true;
            }

            if (userSelectFile) {
                File startLocation = null;
                if (lastSelectedFolder != null) {
                    startLocation = new File(lastSelectedFolder.getLastSelectedFolder());
                }

                if (searchParametersFile != null) {
                    startLocation = searchParametersFile;
                }

                File selectedFile;

                JFileChooser fc = new JFileChooser(startLocation);
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File myFile) {
                        return myFile.getName().toLowerCase().endsWith(".par") || myFile.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "SearchGUI settings file (.par)";
                    }
                };
                fc.setFileFilter(filter);
                int result = fc.showSaveDialog(parentDialog);

                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fc.getSelectedFile();
                    if (lastSelectedFolder != null) {
                        lastSelectedFolder.setLastSelectedFolder(selectedFile.getAbsolutePath());
                    }
                    // make sure the file is appended with '.par'
                    if (!selectedFile.getName().toLowerCase().endsWith(".par")) {
                        selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".par");
                    } else {
                        selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName());
                    }
                    if (selectedFile.exists()) {
                        int choice = JOptionPane.showConfirmDialog(parentDialog,
                                new String[]{"The file " + selectedFile.getName() + " already exists.", "Overwrite?"},
                                "File Already Exists", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.NO_OPTION) {
                            return null;
                        }
                    }

                } else {
                    return null;
                }
            }

            if (searchParametersFile != null) {

                try {
                    SearchParameters.saveIdentificationParameters(newSearchParameters, searchParametersFile);
                    return searchParametersFile;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(parentDialog, "An error occurred when saving the search parameter:\n"
                            + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return null;
                }
            }
        } else if (value == JOptionPane.NO_OPTION) {
            return searchParametersFile;
        } else {
            return null; // canceled by the user
        }

        return null; // dialog canceled by the user
    }
}
