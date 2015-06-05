package com.compomics.util.protein_sequences_manager.gui;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.FastaIndex;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.protein_sequences_manager.ProteinSequencesManager;
import com.compomics.util.protein_sequences_manager.SequenceInputType;
import com.compomics.util.protein_sequences_manager.gui.preferences.ProteinSequencesPreferencesDialog;
import com.compomics.util.protein_sequences_manager.gui.sequences_import.ImportSequencesFromDnaDialog;
import com.compomics.util.protein_sequences_manager.gui.sequences_import.ImportSequencesFromFilesDialog;
import com.compomics.util.protein_sequences_manager.gui.sequences_import.ImportSequencesFromUniprotDialog;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

/**
 * ProteinSequencesManager.
 *
 * @author Marc Vaudel
 */
public class ProteinSequencesManagerGUI extends javax.swing.JDialog {

    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * The utilities user preferences.
     */
    private UtilitiesUserPreferences utilitiesUserPreferences = null;
    /**
     * The protein sequences manager.
     */
    private ProteinSequencesManager proteinSequencesManager;
    /**
     * convenience array for yes or no.
     */
    private final String[] yesNo = new String[]{"Yes", "No"};
    /**
     * String to display when an option is not available.
     */
    private final String notAvailable = "Not Available";
    /**
     * The icon to display when waiting.
     */
    private Image waitingImage;
    /**
     * The normal icon.
     */
    private Image normalImange;
    /**
     * The selected version for every database indexed by name.
     */
    private HashMap<String, String> selectedVersion;
    /**
     * The selection for reviewed for every database indexed by name.
     */
    private HashMap<String, String> reviewedSelection;
    /**
     * The selection for isoforms for every database indexed by name.
     */
    private HashMap<String, String> isoformsSelection;
    /**
     * The selection for contaminants for every database indexed by name.
     */
    private HashMap<String, String> contaminantsSelection;
    /**
     * The selection for decoy for every database indexed by name.
     */
    private HashMap<String, String> decoySelection;

    /**
     * Main method to start a standalone version.
     *
     * @param args
     */
    public static void main(String[] args) {
        ProteinSequencesManagerGUI m = new ProteinSequencesManagerGUI(null, null, null);
    }

    /**
     * Creates a new ProteinSequencesManager.
     *
     * @param parent the parent frame
     * @param normalImange the normal icon
     * @param waitingImage the waiting icon
     */
    public ProteinSequencesManagerGUI(java.awt.Frame parent, Image normalImange, Image waitingImage) {
        super(parent, true);
        initComponents();
        this.parentFrame = parent;
        setTitle("Protein Sequences Manager");
        setUpGui();
        setVisible(true);
    }

    /**
     * Sets up the tool and populates the GUI.
     */
    public void setUpGui() {

        checkSetup();

        progressDialog = new ProgressDialogX(this, parentFrame,
                normalImange,
                waitingImage,
                true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Importing Databases. Please Wait...");

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
                progressDialog.setTitle("Importing Databases. Please Wait...");

                importFromFileMenuItem.setEnabled(true);
                importFromUnitprot.setEnabled(true);
                importFromDNAMenuItem.setEnabled(true);
                proteinSequencesManager = new ProteinSequencesManager();
                if (!progressDialog.isRunCanceled()) {
                    setUpTable();
                    ArrayList<String> databaseNames = proteinSequencesManager.getDatabaseNames();
                    selectedVersion = new HashMap<String, String>(databaseNames.size());
                    reviewedSelection = new HashMap<String, String>(databaseNames.size());
                    isoformsSelection = new HashMap<String, String>(databaseNames.size());
                    contaminantsSelection = new HashMap<String, String>(databaseNames.size());
                    decoySelection = new HashMap<String, String>(databaseNames.size());
                    updateGUI();
                } else {
                    close();
                }
                progressDialog.setRunFinished();
            }
        }.start();
    }

    /**
     * Checks that the too is properly installed. Displays the configuration
     * dialog if not.
     */
    public void checkSetup() {
        boolean error = false;
        utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        if (utilitiesUserPreferences.getProteinSequencesManagerFolder() == null || !utilitiesUserPreferences.getProteinSequencesManagerFolder().exists()) {
            error = true;
            JOptionPane.showMessageDialog(this,
                    new String[]{"Please select a folder to store the databases."},
                    "Configuration", JOptionPane.INFORMATION_MESSAGE);
        }
        if (error) {
            new ProteinSequencesPreferencesDialog(parentFrame);
            checkSetup();
        }
    }

    /**
     * Updates the gui after the adding of new database(s).
     */
    public void updateGUI() {
        ArrayList<String> databaseNames = proteinSequencesManager.getDatabaseNames();
        exportMenu.setEnabled(!databaseNames.isEmpty());
        for (String databaseName : databaseNames) {
            SequenceInputType sequenceInputType = proteinSequencesManager.getInputType(databaseName);
            ArrayList<String> versionNames = proteinSequencesManager.getVersionsForDb(databaseName);
            if (!selectedVersion.containsKey(databaseName)) {
                selectedVersion.put(databaseName, versionNames.get(versionNames.size() - 1));
                if (sequenceInputType == SequenceInputType.uniprot) {
                    reviewedSelection.put(databaseName, yesNo[0]);
                    isoformsSelection.put(databaseName, yesNo[1]);
                } else {
                    reviewedSelection.put(databaseName, notAvailable);
                    isoformsSelection.put(databaseName, notAvailable);
                }
                contaminantsSelection.put(databaseName, yesNo[0]);
                decoySelection.put(databaseName, yesNo[0]);
            }
        }
        updateTable();
    }

    /**
     * Sets up the design of the table.
     */
    public void setUpTable() {

        DefaultTableModel tableModel = new DatabasesTableModel();
        dbTable.setModel(tableModel);
        TableColumnModel tableColumnModel = dbTable.getColumnModel();

        dbTable.getTableHeader().setReorderingAllowed(false);
        dbTableScrollPane.getViewport().setOpaque(false);

        tableColumnModel.getColumn(0).setMaxWidth(50);
        tableColumnModel.getColumn(10).setMaxWidth(50);

    }

    /**
     * Updates the table.
     */
    public void updateTable() {
        ((DefaultTableModel) dbTable.getModel()).fireTableDataChanged();
        dbTable.setCellEditor(new DatabasesTableCellEditor());
    }

    /**
     * Deletes the temp folder and closes the dialog.
     */
    public void close() {
        Util.deleteDir(proteinSequencesManager.getTempFolder());
        dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        backgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        dbPanel = new javax.swing.JPanel();
        dbTableScrollPane = new javax.swing.JScrollPane();
        dbTable = new javax.swing.JTable();
        fileMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        importMenu = new javax.swing.JMenu();
        importFromUnitprot = new javax.swing.JMenuItem();
        importFromFileMenuItem = new javax.swing.JMenuItem();
        importFromDNAMenuItem = new javax.swing.JMenuItem();
        exportMenu = new javax.swing.JMenu();
        andromedaExportMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        configMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        bugReportMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();

        jMenu1.setText("jMenu1");

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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

        dbPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sequence Databases"));
        dbPanel.setOpaque(false);

        dbTable.setModel(new DatabasesTableModel());
        dbTableScrollPane.setViewportView(dbTable);

        javax.swing.GroupLayout dbPanelLayout = new javax.swing.GroupLayout(dbPanel);
        dbPanel.setLayout(dbPanelLayout);
        dbPanelLayout.setHorizontalGroup(
            dbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1149, Short.MAX_VALUE)
                .addContainerGap())
        );
        dbPanelLayout.setVerticalGroup(
            dbPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbTableScrollPane)
                .addContainerGap())
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dbPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        fileMenu.setText("File");

        importMenu.setText("Import From...");

        importFromUnitprot.setText("UniProtKB");
        importFromUnitprot.setEnabled(false);
        importFromUnitprot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importFromUnitprotActionPerformed(evt);
            }
        });
        importMenu.add(importFromUnitprot);

        importFromFileMenuItem.setText("Fasta File(s)");
        importFromFileMenuItem.setEnabled(false);
        importFromFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importFromFileMenuItemActionPerformed(evt);
            }
        });
        importMenu.add(importFromFileMenuItem);

        importFromDNAMenuItem.setText("DNA Sequences");
        importFromDNAMenuItem.setEnabled(false);
        importFromDNAMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importFromDNAMenuItemActionPerformed(evt);
            }
        });
        importMenu.add(importFromDNAMenuItem);

        fileMenu.add(importMenu);

        exportMenu.setText("Export...");

        andromedaExportMenu.setText("Andromeda Config. File");
        exportMenu.add(andromedaExportMenu);

        fileMenu.add(exportMenu);
        fileMenu.add(jSeparator1);

        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        fileMenuBar.add(fileMenu);

        editMenu.setText("Edit");

        configMenuItem.setText("Configuration");
        configMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(configMenuItem);

        fileMenuBar.add(editMenu);

        helpMenu.setText("Help");

        helpMenuItem.setText("Help");
        helpMenu.add(helpMenuItem);

        bugReportMenuItem.setText("Bug Report");
        helpMenu.add(bugReportMenuItem);
        helpMenu.add(jSeparator2);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        fileMenuBar.add(helpMenu);

        setJMenuBar(fileMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Import from file.
     *
     * @param evt
     */
    private void importFromFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importFromFileMenuItemActionPerformed
        ImportSequencesFromFilesDialog importSequencesFromFilesDialog = new ImportSequencesFromFilesDialog(parentFrame, normalImange, waitingImage);
        if (!importSequencesFromFilesDialog.isCanceled()) {
            File selectedFile = importSequencesFromFilesDialog.getSelectedFile();
            if (selectedFile != null) {
                importNewFile(selectedFile, SequenceInputType.user);
            }
        }
    }//GEN-LAST:event_importFromFileMenuItemActionPerformed

    /**
     * Open the ProteinSequencesPreferencesDialog.
     *
     * @param evt
     */
    private void configMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configMenuItemActionPerformed
        new ProteinSequencesPreferencesDialog(parentFrame);
    }//GEN-LAST:event_configMenuItemActionPerformed

    /**
     * Deletes the temp folder and closes the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        close();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Saves the input and closes the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // @TODO: Set selected database
        UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);
        close();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Import sequences from UniProt.
     *
     * @param evt
     */
    private void importFromUnitprotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importFromUnitprotActionPerformed
        ImportSequencesFromUniprotDialog importSequencesFromUniprotDialog = new ImportSequencesFromUniprotDialog(parentFrame);
        if (!importSequencesFromUniprotDialog.isCanceled()) {
            File selectedFile = importSequencesFromUniprotDialog.getSelectedFile();
            if (selectedFile != null) {
                importNewFile(selectedFile, SequenceInputType.user);
            }
        }
    }//GEN-LAST:event_importFromUnitprotActionPerformed

    /**
     * Import DNA.
     * 
     * @param evt 
     */
    private void importFromDNAMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importFromDNAMenuItemActionPerformed
        ImportSequencesFromDnaDialog importSequencesFromDnaDialog = new ImportSequencesFromDnaDialog(parentFrame);
        if (!importSequencesFromDnaDialog.isCanceled()) {
            File selectedFile = importSequencesFromDnaDialog.getSelectedFile();
            if (selectedFile != null) {
                importNewFile(selectedFile, SequenceInputType.user);
            }
        }
    }//GEN-LAST:event_importFromDNAMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem andromedaExportMenu;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JMenuItem bugReportMenuItem;
    private javax.swing.JButton cancelButton;
    private javax.swing.JMenuItem configMenuItem;
    private javax.swing.JPanel dbPanel;
    private javax.swing.JTable dbTable;
    private javax.swing.JScrollPane dbTableScrollPane;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu exportMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar fileMenuBar;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JMenuItem importFromDNAMenuItem;
    private javax.swing.JMenuItem importFromFileMenuItem;
    private javax.swing.JMenuItem importFromUnitprot;
    private javax.swing.JMenu importMenu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Imports a file in the protein sequence manager and updates the GUI.
     *
     * @param selectedFile the file to import
     * @param sequenceInputType the type of input
     */
    public void importNewFile(final File selectedFile, final SequenceInputType sequenceInputType) {

        progressDialog = new ProgressDialogX(this, parentFrame,
                normalImange,
                waitingImage,
                true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Importing Database. Please Wait...");

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

                progressDialog.setTitle("Importing Database. Please Wait...");
                try {
                    proteinSequencesManager.addFastaFile(selectedFile, SequenceInputType.user, progressDialog);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "An error occurred while importing " + selectedFile.getName() + ", please make sure that the tool can write in the working folder.", "Error", JOptionPane.ERROR_MESSAGE);
                    checkSetup();
                    return;
                } catch (StringIndexOutOfBoundsException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(ProteinSequencesManagerGUI.this,
                            e.getMessage(),
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (IllegalArgumentException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(ProteinSequencesManagerGUI.this,
                            e.getMessage(),
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                }
                if (!progressDialog.isRunCanceled()) {
                    updateGUI();
                }
                progressDialog.setRunFinished();
            }
        }.start();
    }

    /**
     * Table model for the filter items.
     */
    private class DatabasesTableModel extends DefaultTableModel {

        public DatabasesTableModel() {

        }

        @Override
        public int getRowCount() {
            if (proteinSequencesManager == null) {
                return 0;
            }
            return proteinSequencesManager.getDatabaseNames().size();
        }

        @Override
        public int getColumnCount() {
            return 11;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "Type";
                case 2:
                    return "Name";
                case 3:
                    return "Version";
                case 4:
                    return "#Sequences";
                case 5:
                    return "Description";
                case 6:
                    return "Reviewed";
                case 7:
                    return "Isoforms";
                case 8:
                    return "Contaminants";
                case 9:
                    return "Decoy";
                case 10:
                    return "  ";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            String databaseName = proteinSequencesManager.getDatabaseNames().get(row);
            String version = selectedVersion.get(databaseName);
            FastaIndex fastaIndex = proteinSequencesManager.getFastaIndex(databaseName, version);
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return fastaIndex.getMainDatabaseType().getFullName();
                case 2:
                    return databaseName;
                case 3:
                    return version;
                case 4:
                    return fastaIndex.getNTarget();
                case 5:
                    return fastaIndex.getDescription();
                case 6:
                    return reviewedSelection.get(databaseName);
                case 7:
                    return isoformsSelection.get(databaseName);
                case 8:
                    return contaminantsSelection.get(databaseName);
                case 9:
                    return decoySelection.get(databaseName);
                case 10:
                    return "+"; // @TODO add an icon
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
        }

        @Override
        public Class getColumnClass(int column) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, column) != null) {
                    return getValueAt(i, column).getClass();
                }
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            String databaseName = proteinSequencesManager.getDatabaseNames().get(row);
            SequenceInputType sequenceInputType = proteinSequencesManager.getInputType(databaseName);
            return column == 3
                    || column == 6 && sequenceInputType == SequenceInputType.uniprot
                    || column == 7 && sequenceInputType == SequenceInputType.uniprot
                    || column == 8
                    || column == 9;
        }
    }

    /**
     * Database table cell editor.
     */
    private class DatabasesTableCellEditor implements TableCellEditor {

        /**
         * Map of the individual cell editors. row - column - cell editor.
         */
        private HashMap<Integer, HashMap<Integer, DefaultCellEditor>> cellEditorsMap;
        /**
         * The last accessed cell editor.
         */
        private DefaultCellEditor lastAccessedEditor = null;

        /**
         * Constructor.
         */
        public DatabasesTableCellEditor() {

            JComboBox comboBox = new JComboBox(yesNo);
            DefaultCellEditor yesNo = new DefaultCellEditor(comboBox);

            ArrayList<String> databaseNames = proteinSequencesManager.getDatabaseNames();
            cellEditorsMap = new HashMap<Integer, HashMap<Integer, DefaultCellEditor>>(databaseNames.size());
            for (int row = 0; row < databaseNames.size(); row++) {
                String databaseName = databaseNames.get(row);
                SequenceInputType sequenceInputType = proteinSequencesManager.getInputType(databaseName);
                HashMap<Integer, DefaultCellEditor> rowCellEditors = new HashMap<Integer, DefaultCellEditor>(5);
                // Version column
                comboBox = new JComboBox(proteinSequencesManager.getVersionsForDb(databaseName).toArray());
                DefaultCellEditor version = new DefaultCellEditor(comboBox);
                rowCellEditors.put(3, version);
                if (sequenceInputType == SequenceInputType.uniprot) {
                    // Reviewed column
                    rowCellEditors.put(6, yesNo);
                    // Isoforms column
                    rowCellEditors.put(7, yesNo);
                }
                // Contaminants column
                rowCellEditors.put(8, yesNo);
                // Decoy column
                rowCellEditors.put(9, yesNo);
                cellEditorsMap.put(row, rowCellEditors);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            HashMap<Integer, DefaultCellEditor> rowMap = cellEditorsMap.get(row);
            if (rowMap != null) {
                DefaultCellEditor defaultCellEditor = rowMap.get(column);
                if (defaultCellEditor != null) {
                    lastAccessedEditor = defaultCellEditor;
                    return defaultCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
                }
            }
            return null;
        }

        @Override
        public Object getCellEditorValue() {
            if (lastAccessedEditor != null) {
                return lastAccessedEditor.getCellEditorValue();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return lastAccessedEditor != null;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean stopCellEditing() {
            if (lastAccessedEditor == null) {
                return true;
            }
            return lastAccessedEditor.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            if (lastAccessedEditor != null) {
                lastAccessedEditor.cancelCellEditing();
            }
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            if (lastAccessedEditor != null) {
                lastAccessedEditor.addCellEditorListener(l);
            }
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
            if (lastAccessedEditor != null) {
                lastAccessedEditor.removeCellEditorListener(l);
            }
        }
    }
}
