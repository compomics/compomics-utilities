package com.compomics.util.experiment.massspectrometry.proteowizard.gui;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.massspectrometry.proteowizard.MsConvertParameters;
import com.compomics.util.experiment.massspectrometry.proteowizard.MsFormat;
import com.compomics.util.experiment.massspectrometry.proteowizard.ProteoWizardFilter;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Dialog for the creation and edition of msconvert parameters.
 *
 * @author Marc Vaudel
 */
public class MsConvertParametersDialog extends javax.swing.JDialog {

    /**
     * Boolean indicating whether the editing was canceled.
     */
    private boolean canceled = false;
    /**
     * Map of the filters to use.
     */
    private HashMap<Integer, String> filters;
    /**
     * List of the indexes of the filters to use.
     */
    private ArrayList<Integer> filterIndexes;
    /**
     * The selected folder.
     */
    private String lastSelectedFolder = "";
    /**
     * The utilities preferences.
     */
    private UtilitiesUserPreferences utilitiesUserPreferences;

    /**
     * Constructor.
     *
     * @param parent the parent frame
     * @param msConvertParameters initial parameters, ignored if null
     */
    public MsConvertParametersDialog(java.awt.Frame parent, MsConvertParameters msConvertParameters) {
        super(parent, true);
        initComponents();
        setUpGUI(msConvertParameters);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Sets up the GUI components.
     *
     * @param msConvertParameters parameters to display on the interface,
     * ignored if null
     */
    private void setUpGUI(MsConvertParameters msConvertParameters) {
        filtersTableScrollPane.getViewport().setOpaque(false);
        filtersTable.getTableHeader().setReorderingAllowed(false);
        if (msConvertParameters != null) {
            outputFormatCmb.setSelectedItem(msConvertParameters.getMsFormat());
            filters = (HashMap<Integer, String>) msConvertParameters.getFiltersMap().clone();
        } else {
            outputFormatCmb.setSelectedItem(MsFormat.mzML);
            filters = new HashMap<Integer, String>(2);
        }

        filterIndexes = new ArrayList<Integer>(filters.keySet());
        Collections.sort(filterIndexes);
        DefaultTableModel tableModel = new FiltersTableModel();
        filtersTable.setModel(tableModel);
        TableColumnModel tableColumnModel = filtersTable.getColumnModel();
        tableColumnModel.getColumn(0).setMinWidth(50);
        tableColumnModel.getColumn(0).setMaxWidth(50);

        TableColumn filterColumn = tableColumnModel.getColumn(1);
        filterColumn.setMinWidth(200);
        filterColumn.setMaxWidth(200);
        JComboBox comboBox = new JComboBox(ProteoWizardFilter.values());
        filterColumn.setCellEditor(new DefaultCellEditor(comboBox));

        for (final ProteoWizardFilter tempFilter : ProteoWizardFilter.values()) {

            JMenuItem tempFilterMenuItem = new javax.swing.JMenuItem(tempFilter.name);

            tempFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (!filters.containsKey(tempFilter.number)) {
                        filters.put(tempFilter.number, "");
                        updateTable();
                    }
                }
            });

            addFilterMenu.add(tempFilterMenuItem);
        }

        outputFormatCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();

        if (utilitiesUserPreferences.getProteoWizardPath() == null) {
            int option = JOptionPane.showConfirmDialog(this, "Cannot find ProteoWizard. Do you want to download it now?", "Download ProteoWizard?", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                openWebPage();
            }
        }

        // display the current path
        if (utilitiesUserPreferences != null) {
            installationJTextField.setText(utilitiesUserPreferences.getProteoWizardPath());
            lastSelectedFolder = utilitiesUserPreferences.getProteoWizardPath();
        }
    }

    /**
     * Updates the table.
     */
    private void updateTable() {
        filterIndexes = new ArrayList<Integer>(filters.keySet());
        Collections.sort(filterIndexes);
        ((DefaultTableModel) filtersTable.getModel()).fireTableDataChanged();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filtersPopupMenu = new javax.swing.JPopupMenu();
        removeFilterMenuItem = new javax.swing.JMenuItem();
        addFilterMenu = new javax.swing.JMenu();
        backgourdPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        msconvertParameters = new javax.swing.JPanel();
        outputFormatLbl = new javax.swing.JLabel();
        outputFormatCmb = new javax.swing.JComboBox();
        filtersLbl = new javax.swing.JLabel();
        filtersTableScrollPane = new javax.swing.JScrollPane();
        filtersTable = new javax.swing.JTable();
        filterHelpLabel = new javax.swing.JLabel();
        installationPanel = new javax.swing.JPanel();
        installationJTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        folderHelpLabel = new javax.swing.JLabel();

        removeFilterMenuItem.setText("Remove Filter");
        removeFilterMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                removeFilterMenuItemMouseReleased(evt);
            }
        });
        filtersPopupMenu.add(removeFilterMenuItem);

        addFilterMenu.setText("Add Filter");
        filtersPopupMenu.add(addFilterMenu);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MSConvert Settings");
        setMinimumSize(new java.awt.Dimension(400, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgourdPanel.setBackground(new java.awt.Color(230, 230, 230));

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

        msconvertParameters.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        msconvertParameters.setOpaque(false);

        outputFormatLbl.setText("Output Format");

        outputFormatCmb.setMaximumRowCount(10);
        outputFormatCmb.setModel(new DefaultComboBoxModel(MsFormat.getDataFormats(null, true)));

        filtersLbl.setText("Filters (right click in the table to edit)");

        filtersTableScrollPane.setOpaque(false);
        filtersTableScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                filtersTableScrollPaneMouseReleased(evt);
            }
        });

        filtersTable.setModel(new FiltersTableModel());
        filtersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                filtersTableMouseReleased(evt);
            }
        });
        filtersTableScrollPane.setViewportView(filtersTable);

        javax.swing.GroupLayout msconvertParametersLayout = new javax.swing.GroupLayout(msconvertParameters);
        msconvertParameters.setLayout(msconvertParametersLayout);
        msconvertParametersLayout.setHorizontalGroup(
            msconvertParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(msconvertParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(msconvertParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filtersTableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                    .addGroup(msconvertParametersLayout.createSequentialGroup()
                        .addComponent(filtersLbl)
                        .addGap(0, 507, Short.MAX_VALUE))
                    .addGroup(msconvertParametersLayout.createSequentialGroup()
                        .addComponent(outputFormatLbl)
                        .addGap(18, 18, 18)
                        .addComponent(outputFormatCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        msconvertParametersLayout.setVerticalGroup(
            msconvertParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, msconvertParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(msconvertParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputFormatLbl)
                    .addComponent(outputFormatCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(filtersLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filtersTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addContainerGap())
        );

        filterHelpLabel.setText("<html><a href>Help with the msconvert filters</a></html>");
        filterHelpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                filterHelpLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                filterHelpLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                filterHelpLabelMouseReleased(evt);
            }
        });

        installationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("ProteoWizard Folder"));
        installationPanel.setOpaque(false);

        installationJTextField.setEditable(false);
        installationJTextField.setToolTipText("The folder containing the PeptideShaker jar file.");

        browseButton.setText("Browse");
        browseButton.setToolTipText("The folder containing the PeptideShaker jar file.");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        folderHelpLabel.setFont(folderHelpLabel.getFont().deriveFont((folderHelpLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        folderHelpLabel.setText("Please locate the ProteoWizard installation folder");

        javax.swing.GroupLayout installationPanelLayout = new javax.swing.GroupLayout(installationPanel);
        installationPanel.setLayout(installationPanelLayout);
        installationPanelLayout.setHorizontalGroup(
            installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, installationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(installationJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, installationPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(folderHelpLabel)
                        .addGap(11, 11, 11)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton)
                .addContainerGap())
        );
        installationPanelLayout.setVerticalGroup(
            installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(installationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(installationJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(folderHelpLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout backgourdPanelLayout = new javax.swing.GroupLayout(backgourdPanel);
        backgourdPanel.setLayout(backgourdPanelLayout);
        backgourdPanelLayout.setHorizontalGroup(
            backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgourdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgourdPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(filterHelpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(msconvertParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(installationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgourdPanelLayout.setVerticalGroup(
            backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgourdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(installationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(msconvertParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgourdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(filterHelpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgourdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgourdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Cancel the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        boolean formatCheck = true;

        if (((MsFormat) outputFormatCmb.getSelectedItem()) != MsFormat.mgf) {
            int value = JOptionPane.showConfirmDialog(this, "Mgf is the only format compatible with SearchGUI. Proceed anyway?",
                    "Output Format Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//            int value = JOptionPane.showConfirmDialog(this, "Mgf is the only format compatible with PeptideShaker. Proceed anyway?", 
//                    "Output Format Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (value == JOptionPane.NO_OPTION) {
                formatCheck = false;
            }
        }

        if (formatCheck) {
            utilitiesUserPreferences.setProteoWizardPath(installationJTextField.getText());
            try {
                UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while saving the preferences.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Remove an item from the filters.
     *
     * @param evt
     */
    private void removeFilterMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeFilterMenuItemMouseReleased
        int row = filtersTable.getSelectedRow();
        if (row >= 0) {
            String itemName = filtersTable.getValueAt(row, 1).toString();
            ProteoWizardFilter proteoWizardFilter = ProteoWizardFilter.getFilter(itemName);
            if (proteoWizardFilter != null) {
                filters.remove(proteoWizardFilter.number);
                updateTable();
            }
        }
    }//GEN-LAST:event_removeFilterMenuItemMouseReleased

    /**
     * Show the filter popup menu.
     *
     * @param evt
     */
    private void filtersTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filtersTableMouseReleased
        if (evt != null && filtersTable.rowAtPoint(evt.getPoint()) != -1) {
            int row = filtersTable.rowAtPoint(evt.getPoint());
            filtersTable.setRowSelectionInterval(row, row);
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON3) {
            removeFilterMenuItem.setVisible(filtersTable.getSelectedRow() != -1);
            filtersPopupMenu.show(filtersTable, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_filtersTableMouseReleased

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Change the cursor to a hand icon.
     *
     * @param evt
     */
    private void filterHelpLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterHelpLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_filterHelpLabelMouseEntered

    /**
     * Change the cursor back to the default icon.
     *
     * @param evt
     */
    private void filterHelpLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterHelpLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_filterHelpLabelMouseExited

    /**
     * Open the msConvert filters helps.
     *
     * @param evt
     */
    private void filterHelpLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterHelpLabelMouseReleased
        BareBonesBrowserLaunch.openURL("http://proteowizard.sourceforge.net/tools/filters.html");
    }//GEN-LAST:event_filterHelpLabelMouseReleased

    /**
     * Open the filter popup menu.
     *
     * @param evt
     */
    private void filtersTableScrollPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filtersTableScrollPaneMouseReleased
        filtersTableMouseReleased(evt);
    }//GEN-LAST:event_filtersTableScrollPaneMouseReleased

    /**
     * Open a file chooser were the user can select the installation folder.
     *
     * @param evt
     */
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed

        File selectedFile = Util.getUserSelectedFolder(this, "ProteoWizard Installation Folder", lastSelectedFolder, "ProteoWizard installation folder", "OK", true);

        if (selectedFile != null) {
            // check if it is a valid folder
            if (!(new File(selectedFile, "msconvert.exe").exists() || new File(selectedFile, "msconvert").exists())) {
                JOptionPane.showMessageDialog(this, "The selected folder is not a valid ProteoWizard folder!", "Wrong Folder Selected", JOptionPane.WARNING_MESSAGE);
                okButton.setEnabled(false);
            } else {
                // assumed to be valid folder
                lastSelectedFolder = selectedFile.getPath();
                installationJTextField.setText(lastSelectedFolder);
                okButton.setEnabled(true);
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu addFilterMenu;
    private javax.swing.JPanel backgourdPanel;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel filterHelpLabel;
    private javax.swing.JLabel filtersLbl;
    private javax.swing.JPopupMenu filtersPopupMenu;
    private javax.swing.JTable filtersTable;
    private javax.swing.JScrollPane filtersTableScrollPane;
    private javax.swing.JLabel folderHelpLabel;
    private javax.swing.JTextField installationJTextField;
    private javax.swing.JPanel installationPanel;
    private javax.swing.JPanel msconvertParameters;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox outputFormatCmb;
    private javax.swing.JLabel outputFormatLbl;
    private javax.swing.JMenuItem removeFilterMenuItem;
    // End of variables declaration//GEN-END:variables

    /**
     * Indicates whether the editing was canceled by the user.
     *
     * @return a boolean indicating whether the editing was canceled by the user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the parameters as created by the user.
     *
     * @return the parameters as created by the user
     */
    public MsConvertParameters getMsConvertParameters() {
        MsConvertParameters msConvertParameters = new MsConvertParameters();
        msConvertParameters.setMsFormat((MsFormat) outputFormatCmb.getSelectedItem());
        for (Integer filterIndex : filters.keySet()) {
            msConvertParameters.addFilter(filterIndex, filters.get(filterIndex));
        }
        return msConvertParameters;
    }

    /**
     * Table model for the filters.
     */
    private class FiltersTableModel extends DefaultTableModel {

        public FiltersTableModel() {
        }

        @Override
        public int getRowCount() {
            if (filters == null) {
                return 0;
            }
            return filters.size();
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
                    return "Name";
                case 2:
                    return "Value";
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
                    Integer index = filterIndexes.get(row);
                    ProteoWizardFilter proteoWizardFilter = ProteoWizardFilter.getFilter(index);
                    String itemName = proteoWizardFilter.name;
                    return itemName;
                case 2:
                    index = filterIndexes.get(row);
                    String value = filters.get(index);
                    if (value == null) {
                        value = null;
                    }
                    return value;
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            switch (column) {
                case 1:
                    Integer index = filterIndexes.get(row);
                    filters.remove(index);
                    ProteoWizardFilter proteoWizardFilter = (ProteoWizardFilter) value;
                    filters.put(proteoWizardFilter.number, "");
                    break;
                case 2:
                    index = filterIndexes.get(row);
                    filters.put(index, (String) value);
                    break;
            }
            updateTable();
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
        public boolean isCellEditable(int row, int column) {
            return column > 0;
        }
    }

    /**
     * Opens the ProteoWizard web page.
     */
    private void openWebPage() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://proteowizard.sourceforge.net");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }
}
