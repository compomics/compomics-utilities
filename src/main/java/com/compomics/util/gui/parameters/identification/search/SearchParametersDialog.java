package com.compomics.util.gui.parameters.identification.search;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.CometParameters;
import com.compomics.util.parameters.identification.tool_specific.XtandemParameters;
import com.compomics.util.gui.GuiUtilities;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.tool_specific.MsgfParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.modification.ModificationsDialog;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters.CleavageParameter;
import com.compomics.util.parameters.identification.search.DigestionParameters.Specificity;
import com.compomics.util.io.file.LastSelectedFolder;
import com.compomics.util.parameters.UtilitiesUserParameters;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
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
public class SearchParametersDialog extends javax.swing.JDialog {

    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
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
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
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
     * Counts the number of times the users has pressed a key on the keyboard in
     * the search field.
     */
    private int keyPressedCounter = 0;
    /**
     * The current Modification search string.
     */
    private String currentPtmSearchString = "";
    /**
     * The time to wait between keys typed before updating the search.
     */
    private int waitingTime = 500;
    /**
     * The modifications to include in the table by default.
     */
    private HashSet<String> defaultModifications;
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
     * Reference mass for the conversion of the fragment ion tolerance from ppm
     * to Dalton.
     */
    private Double refMass;
    /**
     * The utilities user parameters.
     */
    private UtilitiesUserParameters utilitiesUserParameters = null;
    /**
     * The selected fasta file.
     */
    private File selectedFastaFile = null;
    /**
     * The parameters used to parse the fasta file.
     */
    private FastaParameters fastaParameters = null;

    /**
     * Empty default constructor
     */
    public SearchParametersDialog() {
        lastSelectedFolder = null;
    }

    /**
     * Creates a new SearchSettingsDialog with a frame as owner.
     *
     * @param parentFrame the parent frame
     * @param searchParameters previous search parameters
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param setVisible if the dialog is to be visible or not
     * @param modal if the dialog is to be modal
     * @param lastSelectedFolder the last selected folder to use
     * @param settingsName the name of the settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SearchParametersDialog(java.awt.Frame parentFrame, SearchParameters searchParameters, Image normalIcon, Image waitingIcon,
            boolean setVisible, boolean modal, LastSelectedFolder lastSelectedFolder, String settingsName, boolean editable) {
        super(parentFrame, modal);

        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
        this.editable = editable;

        if (searchParameters == null) {
            this.searchParameters = new SearchParameters();
            this.searchParameters.setDigestionParameters(DigestionParameters.getDefaultParameters());
        } else {
            this.searchParameters = searchParameters;
            this.selectedFastaFile = searchParameters.getFastaFile();
            this.fastaParameters = searchParameters.getFastaParameters();
        }

        loadUserPreferences();

        defaultModifications = utilitiesUserParameters.getDefaultModifications();

        initComponents();
        setUpGUI();
        formComponentResized(null);
        setLocationRelativeTo(parentFrame);

        String dialogTitle = "Spectrum Matching";
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
     * @param lastSelectedFolder the last selected folder to use
     * @param settingsName the name of the settings
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SearchParametersDialog(Dialog owner, java.awt.Frame parentFrame, SearchParameters searchParameters, Image normalIcon, Image waitingIcon,
            boolean setVisible, boolean modal, LastSelectedFolder lastSelectedFolder, String settingsName, boolean editable) {
        super(owner, modal);

        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
        this.editable = editable;

        if (searchParameters == null) {
            this.searchParameters = new SearchParameters();
            this.searchParameters.setDigestionParameters(DigestionParameters.getDefaultParameters());
        } else {
            this.searchParameters = searchParameters;
            this.selectedFastaFile = searchParameters.getFastaFile();
            this.fastaParameters = searchParameters.getFastaParameters();
        }

        loadUserPreferences();

        defaultModifications = utilitiesUserParameters.getDefaultModifications();

        initComponents();
        setUpGUI();
        formComponentResized(null);
        setLocationRelativeTo(owner);

        String dialogTitle = "Spectrum Matching";
        if (settingsName != null && settingsName.length() > 0) {
            dialogTitle += " - " + settingsName;
        }
        setTitle(dialogTitle);

        if (setVisible) {
            setVisible(true);
        }
    }

    /**
     * Loads the user preferences.
     */
    private void loadUserPreferences() {

        try {

            utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {

        setScreenProps();
        validateParametersInput(false);

        // Set reference mass for ppm to Da conversion
        this.refMass = searchParameters.getRefMass();

        // set the settings editable or not
        digestionCmb.setEnabled(editable);
        enzymesCmb.setEnabled(editable && ((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == DigestionParameters.CleavageParameter.enzyme);
        specificityComboBox.setEnabled(editable && ((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == DigestionParameters.CleavageParameter.enzyme);
        precursorIonAccuracyTxt.setEditable(editable);
        precursorIonUnit.setEnabled(editable);
        fragmentIon1Cmb.setEnabled(editable);
        fragmentIon2Cmb.setEnabled(editable);
        fragmentIonUnit.setEnabled(editable);
        maxMissedCleavagesTxt.setEditable(editable);
        fragmentIonAccuracyTxt.setEditable(editable);
        minPrecursorChargeTxt.setEditable(editable);
        maxPrecursorChargeTxt.setEditable(editable);
        isotopeMinTxt.setEditable(editable);
        isotopeMaxTxt.setEditable(editable);
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
        digestionCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon1Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon2Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        precursorIonUnit.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIonUnit.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        specificityComboBox.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

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

        modificationTableToolTips = new ArrayList<>();
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
        isotopesLbl = new javax.swing.JLabel();
        isotopeMinTxt = new javax.swing.JTextField();
        isotopeRangeLabel = new javax.swing.JLabel();
        isotopeMaxTxt = new javax.swing.JTextField();
        fragmentIonUnit = new javax.swing.JComboBox();
        digestionLabel = new javax.swing.JLabel();
        digestionCmb = new javax.swing.JComboBox();
        specificityLabel = new javax.swing.JLabel();
        specificityComboBox = new javax.swing.JComboBox();
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
        setTitle("Spectrum Matching");
        setMinimumSize(new java.awt.Dimension(700, 650));
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

        precursorIonLbl.setText("Precursor Tolerance");

        precursorIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precursorIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                precursorIonAccuracyTxtKeyReleased(evt);
            }
        });

        precursorIonUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));

        fragmentIonLbl.setText("Fragment Tolerance");

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

        isotopesLbl.setText("Isotopes");

        isotopeMinTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        isotopeMinTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                isotopeMinTxtKeyReleased(evt);
            }
        });

        isotopeRangeLabel.setText("-");

        isotopeMaxTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        isotopeMaxTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isotopeMaxTxtActionPerformed(evt);
            }
        });
        isotopeMaxTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                isotopeMaxTxtKeyReleased(evt);
            }
        });

        fragmentIonUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));

        digestionLabel.setText("Digestion");

        digestionCmb.setMaximumRowCount(15);
        digestionCmb.setModel(new DefaultComboBoxModel(CleavageParameter.values()));
        digestionCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                digestionCmbActionPerformed(evt);
            }
        });

        specificityLabel.setText("Specificity");

        specificityComboBox.setModel(new DefaultComboBoxModel(Specificity.values()));

        javax.swing.GroupLayout proteaseAndFragmentationPanelLayout = new javax.swing.GroupLayout(proteaseAndFragmentationPanel);
        proteaseAndFragmentationPanel.setLayout(proteaseAndFragmentationPanelLayout);
        proteaseAndFragmentationPanelLayout.setHorizontalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enzymeLabel)
                    .addComponent(fragmentIonType1Lbl)
                    .addComponent(maxMissedCleavagesLabel)
                    .addComponent(specificityLabel)
                    .addComponent(digestionLabel))
                .addGap(26, 26, 26)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(digestionCmb, 0, 209, Short.MAX_VALUE)
                    .addComponent(enzymesCmb, 0, 209, Short.MAX_VALUE)
                    .addComponent(maxMissedCleavagesTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addComponent(fragmentIon1Cmb, 0, 95, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(fragmentIon2Cmb, 0, 96, Short.MAX_VALUE))
                    .addComponent(specificityComboBox, 0, 209, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 75, Short.MAX_VALUE)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(precursorChargeLbl)
                    .addComponent(isotopesLbl)
                    .addComponent(precursorIonLbl)
                    .addComponent(fragmentIonLbl))
                .addGap(18, 18, 18)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                    .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                    .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                    .addComponent(isotopeMinTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                .addGap(5, 5, 5)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(precursorChargeRangeLabel)
                    .addComponent(isotopeRangeLabel))
                .addGap(5, 5, 5)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(precursorIonUnit, javax.swing.GroupLayout.Alignment.TRAILING, 0, 122, Short.MAX_VALUE)
                    .addComponent(fragmentIonUnit, javax.swing.GroupLayout.Alignment.TRAILING, 0, 122, Short.MAX_VALUE)
                    .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                    .addComponent(isotopeMaxTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                .addContainerGap())
        );
        proteaseAndFragmentationPanelLayout.setVerticalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(precursorIonLbl)
                            .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fragmentIonLbl)
                            .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fragmentIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(precursorChargeRangeLabel))
                            .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(precursorChargeLbl)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(isotopesLbl)
                            .addComponent(isotopeMinTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(isotopeMaxTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(isotopeRangeLabel)))
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(digestionLabel)
                            .addComponent(digestionCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(enzymeLabel)
                            .addComponent(enzymesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(specificityLabel)
                            .addComponent(specificityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(maxMissedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxMissedCleavagesLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fragmentIon1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fragmentIon2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fragmentIonType1Lbl))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        fixedModsTable.setMinimumSize(new java.awt.Dimension(0, 0));
        fixedModsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseMoved(evt);
            }
        });
        fixedModsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseReleased(evt);
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
                        .addComponent(fixedModificationsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 242, Short.MAX_VALUE))
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
                        .addContainerGap(58, Short.MAX_VALUE))
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
        variableModsTable.setMinimumSize(new java.awt.Dimension(0, 0));
        variableModsTable.setName(""); // NOI18N
        variableModsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                variableModsTableMouseMoved(evt);
            }
        });
        variableModsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                variableModsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                variableModsTableMouseReleased(evt);
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
                .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
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
                        .addContainerGap(58, Short.MAX_VALUE))
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
            .addComponent(modificationsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
            .addGroup(availableModsPanelLayout.createSequentialGroup()
                .addComponent(modificationsListCombo, 0, 318, Short.MAX_VALUE)
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
                .addComponent(modificationTypesSplitPane)
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
        modificationsPanel.setBounds(0, 0, 820, 344);

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(modificationsLayeredPane)
                    .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(proteaseAndFragmentationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
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
                .addComponent(modificationsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
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
     * Opens a file chooser where the user can select the database file.
     *
     * @param evt
     */
    private void editDatabaseDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDatabaseDetailsButtonActionPerformed

        SequenceDbDetailsDialog sequenceDbDetailsDialog = new SequenceDbDetailsDialog(this, parentFrame, selectedFastaFile, fastaParameters, lastSelectedFolder, editable, normalIcon, waitingIcon);

        loadUserPreferences();

        boolean success = sequenceDbDetailsDialog.selectDB(true);

        if (success) {

            sequenceDbDetailsDialog.setVisible(true);

            if (!sequenceDbDetailsDialog.isCanceled()) {

                selectedFastaFile = sequenceDbDetailsDialog.getSelectedFastaFile();
                fastaParameters = sequenceDbDetailsDialog.getFastaParameters();

                databaseSettingsTxt.setText(selectedFastaFile.getAbsolutePath());

            }

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

                if (!defaultModifications.contains(name)) {

                    defaultModifications.add(name);

                }
            }
        }

        DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
        fixedModel.getDataVector().removeAllElements();

        for (String fixedMod : fixedModifications) {

            ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationParameters().getColor(fixedMod), fixedMod, modificationFactory.getModification(fixedMod).getMass()});

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

            ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationParameters().getColor(fixedMod), fixedMod, modificationFactory.getModification(fixedMod).getMass()});

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

                if (!defaultModifications.contains(name)) {

                    defaultModifications.add(name);

                }
            }
        }

        DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
        variableModel.getDataVector().removeAllElements();

        for (String variabledMod : variableModifications) {

            ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationParameters().getColor(variabledMod), variabledMod, modificationFactory.getModification(variabledMod).getMass()});

        }

        ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
        variableModsTable.repaint();

        variableModificationsLabel.setText("Variable Modifications (" + variableModifications.length + ")");

        if (variableModifications.length > SearchParameters.preferredMaxVariableModifications) {

            JOptionPane.showMessageDialog(this,
                    "It is not recommended to use more than " + SearchParameters.preferredMaxVariableModifications + " variable modifications in the same search.", "Warning", JOptionPane.WARNING_MESSAGE);

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

            ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationParameters().getColor(variabledMod), variabledMod, modificationFactory.getModification(variabledMod).getMass()});

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

        utilitiesUserParameters.setDefaultModifications(defaultModifications);

        try {

            UtilitiesUserParameters.saveUserParameters(utilitiesUserParameters);

        } catch (Exception e) {

            // Ignore
            e.printStackTrace();

        }

        dispose();

    }//GEN-LAST:event_okButtonActionPerformed

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

            String modificationName = (String) fixedModsTable.getValueAt(row, fixedModsTable.getColumn("Name").getModelIndex());
            Modification modification = modificationFactory.getModification(modificationName);
            fixedModsTable.setToolTipText(modification.getHtmlTooltip());

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
     * Opens a file chooser where the color for the Modification can be changed.
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

                    searchParameters.getModificationParameters().setColor((String) fixedModsTable.getValueAt(row, 1), newColor.getRGB());
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

            String modificationName = (String) modificationsTable.getValueAt(row, modificationsTable.getColumn("Name").getModelIndex());
            Modification modification = modificationFactory.getModification(modificationName);
            modificationsTable.setToolTipText(modification.getHtmlTooltip());

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
     * Opens a color chooser where the color for the Modification can be
     * changed, or allows the users to change of a Modification is in the most
     * used Modifications list.
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

                    modificationFactory.setColor((String) modificationsTable.getValueAt(row, 1), newColor.getRGB());
                    modificationsTable.setValueAt(newColor, row, 0);
                    ((DefaultTableModel) modificationsTable.getModel()).fireTableDataChanged();
                    modificationsTable.repaint();

                }
            } else if (modificationsListCombo.getSelectedIndex() == 1
                    && column == modificationsTable.getColumn("  ").getModelIndex()
                    && modificationsTable.getValueAt(row, column) != null) {

                boolean selected = (Boolean) modificationsTable.getValueAt(row, column);
                String modificationName = (String) modificationsTable.getValueAt(row, 1);

                // change if the modification is considered as default
                if (modificationsListCombo.getSelectedIndex() == 0) {

                    // remove from default modification set
                    defaultModifications.remove(modificationName);

                } else if (selected) {

                    // add to default modification set
                    if (!defaultModifications.contains(modificationName)) {

                        defaultModifications.add(modificationName);

                    }

                } else {

                    // remove from default modification set
                    defaultModifications.remove(modificationName);

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

            String modificationName = (String) variableModsTable.getValueAt(row, variableModsTable.getColumn("Name").getModelIndex());
            Modification modification = modificationFactory.getModification(modificationName);
            variableModsTable.setToolTipText(modification.getHtmlTooltip());

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
     * Opens a file chooser where the color for the Modification can be changed.
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

                    searchParameters.getModificationParameters().setColor((String) variableModsTable.getValueAt(row, 1), newColor.getRGB());
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
                "Spectrum Matching - Help", 500, 100);
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
     * Jump to the row with the Modification starting with the typed letters.
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

                            // search in the modification table
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

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void isotopeMaxTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_isotopeMaxTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_isotopeMaxTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void isotopeMaxTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isotopeMaxTxtActionPerformed
        validateParametersInput(false);
    }//GEN-LAST:event_isotopeMaxTxtActionPerformed

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void isotopeMinTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_isotopeMinTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_isotopeMinTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void maxPrecursorChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPrecursorChargeTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxPrecursorChargeTxtKeyReleased

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
    private void fragmentIonAccuracyTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fragmentIonAccuracyTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_fragmentIonAccuracyTxtKeyReleased

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
    private void maxMissedCleavagesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxMissedCleavagesTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxMissedCleavagesTxtKeyReleased

    /**
     * Validate the parameters.
     *
     * @param evt
     */
    private void enzymesCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enzymesCmbActionPerformed
        validateParametersInput(false);
    }//GEN-LAST:event_enzymesCmbActionPerformed

    /**
     * Enable/disable the enzymes.
     *
     * @param evt
     */
    private void digestionCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_digestionCmbActionPerformed

        enzymesCmb.setEnabled(((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == DigestionParameters.CleavageParameter.enzyme);
        maxMissedCleavagesTxt.setEnabled((((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == DigestionParameters.CleavageParameter.enzyme));
        specificityComboBox.setEnabled(((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == DigestionParameters.CleavageParameter.enzyme);

        if (!(((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == DigestionParameters.CleavageParameter.enzyme)) {
            enzymesCmb.setSelectedIndex(0);
        }

        validateParametersInput(false);
    }//GEN-LAST:event_digestionCmbActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFixedModification;
    private javax.swing.JButton addVariableModification;
    private javax.swing.JPanel availableModsPanel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataBasePanelSettings;
    private javax.swing.JLabel databaseSettingsLbl;
    private javax.swing.JTextField databaseSettingsTxt;
    private javax.swing.JComboBox digestionCmb;
    private javax.swing.JLabel digestionLabel;
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
    private javax.swing.JComboBox fragmentIonUnit;
    private javax.swing.JTextField isotopeMaxTxt;
    private javax.swing.JTextField isotopeMinTxt;
    private javax.swing.JLabel isotopeRangeLabel;
    private javax.swing.JLabel isotopesLbl;
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
    private javax.swing.JComboBox specificityComboBox;
    private javax.swing.JLabel specificityLabel;
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

        ArrayList<String> tempEnzymes = new ArrayList<>();

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

        if (selectedFastaFile != null) {

            String fastaPath = selectedFastaFile.getAbsolutePath();
            databaseSettingsTxt.setText(fastaPath);

        }

        ArrayList<String> missingPtms = new ArrayList<>();
        ModificationParameters modificationProfile = searchParameters.getModificationParameters();

        if (modificationProfile != null) {

            ArrayList<String> fixedMods = modificationProfile.getFixedModifications();

            for (String modificationName : fixedMods) {

                if (!modificationFactory.containsModification(modificationName)) {

                    missingPtms.add(modificationName);

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

                    for (String modification : missingPtms) {

                        if (first) {

                            first = false;

                        } else {

                            output += ", ";

                        }

                        output += modification;
                    }

                    output += ".\nPlease import them in the Modification Editor.";
                    JOptionPane.showMessageDialog(this, output, "Modification Not Found", JOptionPane.WARNING_MESSAGE);

                }
            }

            DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
            fixedModel.getDataVector().removeAllElements();

            for (String fixedMod : fixedMods) {

                ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationParameters().getColor(fixedMod), fixedMod, modificationFactory.getModification(fixedMod).getMass()});

            }

            ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
            fixedModsTable.repaint();
            fixedModificationsLabel.setText("Fixed Modifications (" + fixedMods.size() + ")");

            ArrayList<String> variableMods = modificationProfile.getVariableModifications();

            for (String modificationName : variableMods) {

                if (!modificationFactory.containsModification(modificationName)) {

                    missingPtms.add(modificationName);

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

                    for (String modification : missingPtms) {

                        if (first) {

                            first = false;

                        } else {

                            output += ", ";

                        }

                        output += modification;
                    }

                    output += ".\nPlease import them in the Modification Editor.";
                    JOptionPane.showMessageDialog(this, output, "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }

            DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
            variableModel.getDataVector().removeAllElements();
            for (String variableMod : variableMods) {

                ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationParameters().getColor(variableMod), variableMod, modificationFactory.getModification(variableMod).getMass()});

            }

            ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
            variableModsTable.repaint();
            variableModificationsLabel.setText("Variable Modifications (" + variableMods.size() + ")");

            updateModificationList();

        }

        DigestionParameters digestionPreferences = searchParameters.getDigestionParameters();

        if (digestionPreferences.getCleavageParameter() != null) {

            digestionCmb.setSelectedItem(digestionPreferences.getCleavageParameter());

        }

        // set enzyme
        if (digestionPreferences.getCleavageParameter() == CleavageParameter.enzyme) {

            if (digestionPreferences.hasEnzymes()) {

                Enzyme enzyme = digestionPreferences.getEnzymes().get(0);  // @TODO: allow the selection of multiple enzymes?
                String enzymeName = enzyme.getName();
                enzymesCmb.setSelectedItem(enzymeName);

                // set missed cleavages
                Integer nMissedCleavages = digestionPreferences.getnMissedCleavages(enzymeName);

                if (nMissedCleavages != null) {

                    maxMissedCleavagesTxt.setText(nMissedCleavages + "");

                } else {

                    maxMissedCleavagesTxt.setText("Not set");

                }

                // set specificity
                specificityComboBox.setSelectedItem(digestionPreferences.getSpecificity(enzymeName));

            } else {

                enzymesCmb.setSelectedIndex(0);

            }
        }

        // enable/disable enzyme settings
        digestionCmbActionPerformed(null);

        if (searchParameters.getForwardIons() != null && !searchParameters.getForwardIons().isEmpty()) {

            Integer ionSearched = searchParameters.getForwardIons().get(0);
            fragmentIon1Cmb.setSelectedItem(PeptideFragmentIon.getSubTypeAsString(ionSearched));

        }

        if (searchParameters.getRewindIons() != null && !searchParameters.getRewindIons().isEmpty()) {

            Integer ionSearched = searchParameters.getRewindIons().get(0);
            fragmentIon2Cmb.setSelectedItem(PeptideFragmentIon.getSubTypeAsString(ionSearched));

        }

        if (searchParameters.getPrecursorAccuracy() > 0.0) {

            precursorIonAccuracyTxt.setText(searchParameters.getPrecursorAccuracy() + "");

        }

        if (searchParameters.getPrecursorAccuracyType() != null) {

            if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM) {

                precursorIonUnit.setSelectedItem("ppm");

            } else if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {

                precursorIonUnit.setSelectedItem("Da");

            }
        }

        if (searchParameters.getFragmentIonAccuracy() > 0.0) {

            fragmentIonAccuracyTxt.setText(searchParameters.getFragmentIonAccuracy() + "");

        }

        if (searchParameters.getFragmentAccuracyType() != null) {

            if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.PPM) {

                fragmentIonUnit.setSelectedItem("ppm");

            } else if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.DA) {

                fragmentIonUnit.setSelectedItem("Da");

            }
        }

        if (searchParameters.getMinChargeSearched() > 0) {

            minPrecursorChargeTxt.setText(searchParameters.getMinChargeSearched() + "");

        }

        if (searchParameters.getMaxChargeSearched() > 0) {

            maxPrecursorChargeTxt.setText(searchParameters.getMaxChargeSearched() + "");

        }

        if (searchParameters.getMinIsotopicCorrection() >= 0) {

            isotopeMinTxt.setText(searchParameters.getMinIsotopicCorrection() + "");

        }

        if (searchParameters.getMaxIsotopicCorrection() > 0) {

            isotopeMaxTxt.setText(searchParameters.getMaxIsotopicCorrection() + "");

        }
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
        maxMissedCleavagesLabel.setForeground(Color.BLACK);

        databaseSettingsLbl.setToolTipText(null);
        enzymeLabel.setToolTipText(null);
        maxMissedCleavagesLabel.setToolTipText(null);

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

        // validateprecursor mass tolerances, fragment mass tolerances and precursor charges
        valid = GuiUtilities.validateDoubleInput(this, precursorIonLbl, precursorIonAccuracyTxt, "precursor mass tolerance", "Precursor Mass Tolerance Error", true, showMessage, valid);
        valid = GuiUtilities.validateDoubleInput(this, fragmentIonLbl, fragmentIonAccuracyTxt, "fragment mass tolerance", "Fragment Mass Tolerance Error", true, showMessage, valid);

        boolean lowerChargeValid = GuiUtilities.validateIntegerInput(this, precursorChargeLbl, minPrecursorChargeTxt, "lower bound for the precursor charge", "Precursor Charge Error", true, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, precursorChargeLbl, maxPrecursorChargeTxt, "upper bound for the precursor charge", "Precursor Charge Error", true, showMessage, valid);

        if (!lowerChargeValid) {

            GuiUtilities.validateIntegerInput(this, precursorChargeLbl, minPrecursorChargeTxt, "lower bound for the precursor charge", "Precursor Charge Error", true, showMessage, valid);

        }

        boolean lowerBoundValid = GuiUtilities.validateIntegerInput(this, isotopesLbl, isotopeMinTxt, "lower bound for the precursor isotope", "Precursor Isotope Error", false, showMessage, valid);
        valid = GuiUtilities.validateIntegerInput(this, isotopesLbl, isotopeMaxTxt, "upper bound for the precursor isotope", "Precursor Isotope Error", true, showMessage, valid);

        if (!lowerBoundValid) {

            GuiUtilities.validateIntegerInput(this, isotopesLbl, isotopeMinTxt, "lower bound for the precursor isotope", "Precursor Isotope Error", false, showMessage, valid);

        }

        // make sure that the lower charge is smaller than the upper charge
        try {

            int chargeLowerBound = Integer.parseInt(minPrecursorChargeTxt.getText().trim());
            int chargeUpperBound = Integer.parseInt(maxPrecursorChargeTxt.getText().trim());

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

        // make sure that the lower isotope is smaller than the upper isotope
        try {

            int isotopeLowerBound = Integer.parseInt(isotopeMinTxt.getText().trim());
            int isotopeUpperBound = Integer.parseInt(isotopeMaxTxt.getText().trim());

            if (isotopeUpperBound < isotopeLowerBound) {

                if (showMessage && valid) {

                    JOptionPane.showMessageDialog(this, "The minimum precursor isotope must be lower than or equal to the maximum precursor isotope.",
                            "Precursor Isotope Error", JOptionPane.WARNING_MESSAGE);

                }

                valid = false;
                isotopesLbl.setForeground(Color.RED);
                isotopesLbl.setToolTipText("Minimum precursor isotope > Maximum precursor isotope!");

            }

        } catch (NumberFormatException e) {
            // ignore, error already caught above
        }

        // valdiate that an enzyme is selected
        if (((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == CleavageParameter.enzyme && enzymesCmb.getSelectedIndex() == 0) {

            if (showMessage && valid) {

                JOptionPane.showMessageDialog(this, "Please select an enzyme.", "Enzyme Error", JOptionPane.WARNING_MESSAGE);

            }

            valid = false;
            enzymeLabel.setForeground(Color.RED);
            enzymeLabel.setToolTipText("No enzyme selected!");

        }

        // validate missed cleavages
        if (((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem()) == CleavageParameter.enzyme) {

            valid = GuiUtilities.validateIntegerInput(this, maxMissedCleavagesLabel, maxMissedCleavagesTxt, "number of allowed missed cleavages", "Missed Cleavages Error", true, showMessage, valid);

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
        tempSearchParameters.setFastaParameters(fastaParameters);

        DigestionParameters digestionPreferences = new DigestionParameters();

        // set the digestion type
        digestionPreferences.setCleavageParameter((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem());

        // set the enzyme
        if ((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem() == DigestionParameters.CleavageParameter.enzyme) {
            Enzyme enzyme = enzymeFactory.getEnzyme(enzymesCmb.getSelectedItem().toString());
            digestionPreferences.addEnzyme(enzyme);

            // enzyme specificity
            String enzymeName = enzyme.getName();
            digestionPreferences.setSpecificity(enzymeName, (DigestionParameters.Specificity) specificityComboBox.getSelectedItem());

            // max missed cleavages
            digestionPreferences.setnMissedCleavages(enzymeName, new Integer(maxMissedCleavagesTxt.getText().trim()));
        }

        // save the digestion settings
        tempSearchParameters.setDigestionParameters(digestionPreferences);

// Precursor m/z tolerance
        tempSearchParameters.setPrecursorAccuracy(new Double(precursorIonAccuracyTxt.getText().trim()));
        if (precursorIonUnit.getSelectedIndex() == 0) {
            tempSearchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
        } else {
            tempSearchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
        }

        // Fragment m/z tolerance
        tempSearchParameters.setFragmentIonAccuracy(new Double(fragmentIonAccuracyTxt.getText().trim()));
        if (fragmentIonUnit.getSelectedIndex() == 0) {
            tempSearchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
        } else {
            tempSearchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        }

        // Check for conflicts in the X!Tandem quick acetyl and pyrolidone options
        double fragmentIonToleranceDa = tempSearchParameters.getFragmentIonAccuracyInDaltons(refMass);
        boolean acetylConflict = false;
        boolean pyroConflict = false;
        ModificationParameters modificationProfile = new ModificationParameters();
        for (int i = 0; i < fixedModsTable.getRowCount(); i++) {
            
            String modName = (String) fixedModsTable.getValueAt(i, 1);
            Modification modification = modificationFactory.getModification(modName);
            Color modificationColor = (Color) fixedModsTable.getValueAt(i, 0);
            
            modificationProfile.addFixedModification(modification);
            modificationProfile.addRefinementFixedModification(modification);
            modificationProfile.setColor(modName, modificationColor.getRGB());
            if ((modification.getModificationType() == ModificationType.modn_peptide || modification.getModificationType() == ModificationType.modnaa_peptide || modification.getModificationType() == ModificationType.modn_protein || modification.getModificationType() == ModificationType.modnaa_protein) && Math.abs(modification.getMass() - 42.010565) < fragmentIonToleranceDa) {
                acetylConflict = true;
            }
            if ((modification.getModificationType() == ModificationType.modn_peptide || modification.getModificationType() == ModificationType.modnaa_peptide || modification.getModificationType() == ModificationType.modn_protein || modification.getModificationType() == ModificationType.modnaa_protein) && Math.abs(modification.getMass() + 17.026549) < fragmentIonToleranceDa) {
                pyroConflict = true;
            }
        }

        for (int i = 0; i < variableModsTable.getRowCount(); i++) {
            
            String modName = (String) variableModsTable.getValueAt(i, 1);
            Color modificationColor = (Color) fixedModsTable.getValueAt(i, 0);
            
            modificationProfile.addVariableModification(modificationFactory.getModification(modName));
            modificationProfile.setColor(modName, modificationColor.getRGB());
            
        }

        // re-add the variable refinement modifications
        ArrayList<String> variableRefinemetModifications = tempSearchParameters.getModificationParameters().getRefinementVariableModifications();
        for (String varRefinementMod : variableRefinemetModifications) {
            Modification modification = modificationFactory.getModification(varRefinementMod);
            modificationProfile.addRefinementVariableModification(modification);
        }

        tempSearchParameters.setModificationParameters(modificationProfile);

        ArrayList<Integer> selectedForwardIons = new ArrayList<>(1);
        Integer ionType = PeptideFragmentIon.getIonType(fragmentIon1Cmb.getSelectedItem().toString().trim());
        selectedForwardIons.add(ionType);
        tempSearchParameters.setForwardIons(selectedForwardIons);
        ArrayList<Integer> selectedRewindIons = new ArrayList<>(1);
        ionType = PeptideFragmentIon.getIonType(fragmentIon2Cmb.getSelectedItem().toString().trim());
        selectedRewindIons.add(ionType);
        tempSearchParameters.setRewindIons(selectedRewindIons);
        tempSearchParameters.setFragmentIonAccuracy(new Double(fragmentIonAccuracyTxt.getText().trim()));
        int charge = new Integer(minPrecursorChargeTxt.getText().trim());
        tempSearchParameters.setMinChargeSearched(charge);
        charge = new Integer(maxPrecursorChargeTxt.getText().trim());
        tempSearchParameters.setMaxChargeSearched(charge);
        Integer minIsotope = new Integer(isotopeMinTxt.getText());
        tempSearchParameters.setMinIsotopicCorrection(minIsotope);
        Integer maxIsotope = new Integer(isotopeMaxTxt.getText());
        tempSearchParameters.setMaxIsotopicCorrection(maxIsotope);

        // Adapt X!Tandem options
        XtandemParameters xtandemParameters = (XtandemParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex());
        if (xtandemParameters == null) {
            xtandemParameters = new XtandemParameters();
            searchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), xtandemParameters);
        }
        xtandemParameters.setProteinQuickAcetyl(!acetylConflict);
        xtandemParameters.setQuickPyrolidone(!pyroConflict);

        // Adapt Comet options
        CometParameters cometParameters = (CometParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex());
        if (cometParameters == null) {
            cometParameters = new CometParameters();
            searchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), cometParameters);
        }
        double binoffset = tempSearchParameters.getFragmentIonAccuracyInDaltons(refMass) / 2;
        cometParameters.setFragmentBinOffset(binoffset);
        if (maxIsotope > 0) {
            cometParameters.setIsotopeCorrection(1);
        } else {
            cometParameters.setIsotopeCorrection(0);
        }
        if ((DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem() == DigestionParameters.CleavageParameter.enzyme) {
            DigestionParameters.Specificity specificity = (DigestionParameters.Specificity) specificityComboBox.getSelectedItem();
            switch (specificity) {
                case specific:
                    cometParameters.setEnzymeType(2);
                    break;
                case semiSpecific:
                    cometParameters.setEnzymeType(1);
                    break;
                case specificNTermOnly:
                    cometParameters.setEnzymeType(8);
                    break;
                case specificCTermOnly:
                    cometParameters.setEnzymeType(9);
                    break;
                default:
                    throw new UnsupportedOperationException("Specificity " + specificity + " not supported.");
            }
        }

        // Adapt ms-gf+ options
        MsgfParameters msgfParameters = (MsgfParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex());
        if (msgfParameters == null) {
            msgfParameters = new MsgfParameters();
            searchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), msgfParameters);
        }
        DigestionParameters.CleavageParameter cleavagePreference = (DigestionParameters.CleavageParameter) digestionCmb.getSelectedItem();
        if (cleavagePreference == DigestionParameters.CleavageParameter.enzyme) {
            DigestionParameters.Specificity specificity = (DigestionParameters.Specificity) specificityComboBox.getSelectedItem();
            switch (specificity) {
                case specific:
                    msgfParameters.setNumberTolerableTermini(2);
                    break;
                case semiSpecific:
                case specificNTermOnly:
                case specificCTermOnly:
                    msgfParameters.setNumberTolerableTermini(1);
                    break;
                default:
                    throw new UnsupportedOperationException("Specificity " + specificity + " not supported.");
            }
        } else if (cleavagePreference == CleavageParameter.unSpecific) {
            msgfParameters.setNumberTolerableTermini(0);
        }

        // Adapt Myrimatch options
        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex());
        if (myriMatchParameters == null) {
            myriMatchParameters = new MyriMatchParameters();
            searchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), myriMatchParameters);
        }
        if (cleavagePreference == DigestionParameters.CleavageParameter.enzyme) {
            DigestionParameters.Specificity specificity = (DigestionParameters.Specificity) specificityComboBox.getSelectedItem();
            switch (specificity) {
                case specific:
                    myriMatchParameters.setMinTerminiCleavages(2);
                    break;
                case semiSpecific:
                case specificNTermOnly:
                case specificCTermOnly:
                    myriMatchParameters.setMinTerminiCleavages(1);
                    break;
                default:
                    throw new UnsupportedOperationException("Specificity " + specificity + " not supported.");
            }
        } else if (cleavagePreference == CleavageParameter.unSpecific) {
            myriMatchParameters.setMinTerminiCleavages(0);
        }

        return tempSearchParameters;
    }

    /**
     * Updates the modification list (right).
     */
    private void updateModificationList() {

        ArrayList<String> allModificationsList = new ArrayList<>();

        if (modificationsListCombo.getSelectedIndex() == 0) {

            for (String name : defaultModifications) {

                if (defaultModifications.contains(name)) {

                    allModificationsList.add(name);

                }
            }

        } else {

            allModificationsList = modificationFactory.getModifications();

        }

        int nFixed = fixedModsTable.getRowCount();
        int nVariable = variableModsTable.getRowCount();
        ArrayList<String> allModifications = new ArrayList<>();

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

        String[] allModificationsAsArray = allModifications.stream()
                .sorted()
                .toArray(String[]::new);

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
            ((DefaultTableModel) modificationsTable.getModel()).addRow(new Object[]{modificationFactory.getColor(mod), mod, modificationFactory.getModification(mod).getMass(), defaultModifications.contains(mod)});
        }
        ((DefaultTableModel) modificationsTable.getModel()).fireTableDataChanged();
        modificationsTable.repaint();

        // get the min and max values for the mass sparklines
        double maxMass = Double.MIN_VALUE;
        double minMass = Double.MAX_VALUE;

        for (String modification : modificationFactory.getModifications()) {
            if (modificationFactory.getModification(modification).getMass() > maxMass) {
                maxMass = modificationFactory.getModification(modification).getMass();
            }
            if (modificationFactory.getModification(modification).getMass() < minMass) {
                minMass = modificationFactory.getModification(modification).getMass();
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

        // enable/disable the add/remove modification buttons
        enableAddRemoveButtons();
    }

    /**
     * Enable/disable the add/remove Modification buttons.
     */
    private void enableAddRemoveButtons() {
        removeVariableModification.setEnabled(variableModsTable.getSelectedRow() != -1 && editable);
        addVariableModification.setEnabled(modificationsTable.getSelectedRow() != -1 && editable);
        removeFixedModification.setEnabled(fixedModsTable.getSelectedRow() != -1 && editable);
        addFixedModification.setEnabled(modificationsTable.getSelectedRow() != -1 && editable);
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
}
