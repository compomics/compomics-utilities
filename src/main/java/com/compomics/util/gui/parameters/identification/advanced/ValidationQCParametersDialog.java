package com.compomics.util.gui.parameters.identification.advanced;

import com.compomics.util.experiment.filtering.Filter;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.parameters.identification.advanced.ValidationQcParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 * Dialog for the edition of validation QC filters.
 *
 * @author Marc Vaudel
 */
public class ValidationQCParametersDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * A parent handling the edition of filters.
     */
    private ValidationQCParametersDialogParent validationQCPreferencesDialogParent;
    /**
     * List of the PSM quality filters.
     */
    private ArrayList<Filter> psmFilters = new ArrayList<>();
    /**
     * List of the peptide quality filters.
     */
    private ArrayList<Filter> peptideFilters = new ArrayList<>();
    /**
     * List of the protein quality filters.
     */
    private ArrayList<Filter> proteinFilters = new ArrayList<>();
    /**
     * Boolean indicating whether the dialog was canceled.
     */
    boolean canceled = false;
    /**
     * Boolean indicating whether the settings have been changed.
     */
    private boolean userInput = false;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;

    /**
     * Creates a new ValidationQCPreferencesDialog with a frame as owner.
     *
     * @param parentFrame the parent frame
     * @param validationQCPreferencesDialogParent a parent handling the edition
     * of filters
     * @param validationQCPreferences the validation QC preferences
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public ValidationQCParametersDialog(java.awt.Frame parentFrame, ValidationQCParametersDialogParent validationQCPreferencesDialogParent, ValidationQcParameters validationQCPreferences, boolean editable) {
        super(parentFrame, true);
        initComponents();

        this.parentFrame = parentFrame;
        this.validationQCPreferencesDialogParent = validationQCPreferencesDialogParent;
        this.editable = editable;

        ArrayList<Filter> originalPsmFilters = validationQCPreferences.getPsmFilters();
        if (originalPsmFilters != null) {
            for (Filter filter : originalPsmFilters) {
                psmFilters.add(filter.clone());
            }
        } else {
            psmFilters = new ArrayList<>();
        }

        ArrayList<Filter> originalPeptidesFilters = validationQCPreferences.getPeptideFilters();
        if (originalPeptidesFilters != null) {
            for (Filter filter : originalPeptidesFilters) {
                peptideFilters.add(filter.clone());
            }
        } else {
            peptideFilters = new ArrayList<>();
        }

        ArrayList<Filter> originalProteinFilters = validationQCPreferences.getProteinFilters();
        if (originalProteinFilters != null) {
            for (Filter filter : originalProteinFilters) {
                proteinFilters.add(filter.clone());
            }
        } else {
            proteinFilters = new ArrayList<>();
        }

        setUpGUI(validationQCPreferences);

        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new ValidationQCPreferencesDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame the parent frame
     * @param validationQCPreferencesDialogParent a parent handling the edition
     * of filters
     * @param validationQCPreferences the validation QC preferences
     * @param editable boolean indicating whether the settings can be edited by the user
     */
    public ValidationQCParametersDialog(Dialog owner, java.awt.Frame parentFrame, ValidationQCParametersDialogParent validationQCPreferencesDialogParent, ValidationQcParameters validationQCPreferences, boolean editable) {
        super(owner, true);
        initComponents();

        this.parentFrame = parentFrame;
        this.validationQCPreferencesDialogParent = validationQCPreferencesDialogParent;
        this.editable = editable;

        ArrayList<Filter> originalPsmFilters = validationQCPreferences.getPsmFilters();
        if (originalPsmFilters != null) {
            for (Filter filter : originalPsmFilters) {
                psmFilters.add(filter.clone());
            }
        } else {
            psmFilters = new ArrayList<>();
        }

        ArrayList<Filter> originalPeptidesFilters = validationQCPreferences.getPeptideFilters();
        if (originalPeptidesFilters != null) {
            for (Filter filter : originalPeptidesFilters) {
                peptideFilters.add(filter.clone());
            }
        } else {
            peptideFilters = new ArrayList<>();
        }

        ArrayList<Filter> originalProteinFilters = validationQCPreferences.getProteinFilters();
        if (originalProteinFilters != null) {
            for (Filter filter : originalProteinFilters) {
                proteinFilters.add(filter.clone());
            }
        } else {
            proteinFilters = new ArrayList<>();
        }

        setUpGUI(validationQCPreferences);

        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Fills the GUI according to the validation QC preferences.
     *
     * @param validationQCPreferences the validation QC preferences
     */
    private void setUpGUI(ValidationQcParameters validationQCPreferences) {
        
        dbCheck.setSelected(validationQCPreferences.isDbSize());
        nTargetCheck.setSelected(validationQCPreferences.isFirstDecoy());
        confidenceCheck.setSelected(validationQCPreferences.getConfidenceMargin() != 0.0);

        psmTable.getColumn(" ").setMaxWidth(30);
        peptideTable.getColumn(" ").setMaxWidth(30);
        proteinTable.getColumn(" ").setMaxWidth(30);

        // make sure that the scroll panes are see-through
        proteinScrollPane.getViewport().setOpaque(false);
        peptideScrollPane.getViewport().setOpaque(false);
        psmScrollPane.getViewport().setOpaque(false);

        proteinTable.getTableHeader().setReorderingAllowed(false);
        peptideTable.getTableHeader().setReorderingAllowed(false);
        psmTable.getTableHeader().setReorderingAllowed(false);
        
        dbCheck.setEnabled(editable);
        nTargetCheck.setEnabled(editable);
        confidenceCheck.setEnabled(editable);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        psmPopupMenu = new javax.swing.JPopupMenu();
        addPsmFilterMenuItem = new javax.swing.JMenuItem();
        editPsmFilterMenuItem = new javax.swing.JMenuItem();
        removePsmFilterMenuItem = new javax.swing.JMenuItem();
        peptidePopupMenu = new javax.swing.JPopupMenu();
        addPeptideFilterMenuItem = new javax.swing.JMenuItem();
        editPeptideFilterMenuItem = new javax.swing.JMenuItem();
        removePeptideFilterMenuItem = new javax.swing.JMenuItem();
        proteinPopupMenu = new javax.swing.JPopupMenu();
        addProteinFilterMenuItem = new javax.swing.JMenuItem();
        editProteinFilterMenuItem = new javax.swing.JMenuItem();
        removeProteinFilterMenuItem = new javax.swing.JMenuItem();
        validationQCPreferencesDialogPanel = new javax.swing.JPanel();
        generalSettingsPanel = new javax.swing.JPanel();
        dbCheck = new javax.swing.JCheckBox();
        nTargetCheck = new javax.swing.JCheckBox();
        markDoubtfulLabel = new javax.swing.JLabel();
        confidenceCheck = new javax.swing.JCheckBox();
        proteinFiltersPanel = new javax.swing.JPanel();
        proteinScrollPane = new javax.swing.JScrollPane();
        proteinTable = new javax.swing.JTable();
        peptideFiltersPanel = new javax.swing.JPanel();
        peptideScrollPane = new javax.swing.JScrollPane();
        peptideTable = new javax.swing.JTable();
        psmFiltersPanel = new javax.swing.JPanel();
        psmScrollPane = new javax.swing.JScrollPane();
        psmTable = new javax.swing.JTable();
        helpLbl = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();

        addPsmFilterMenuItem.setText("Add Filter");
        addPsmFilterMenuItem.setToolTipText("Add a new filter");
        addPsmFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPsmFilterMenuItemActionPerformed(evt);
            }
        });
        psmPopupMenu.add(addPsmFilterMenuItem);

        editPsmFilterMenuItem.setText("Edit Filter");
        editPsmFilterMenuItem.setToolTipText("Edit the selected filter");
        editPsmFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPsmFilterMenuItemActionPerformed(evt);
            }
        });
        psmPopupMenu.add(editPsmFilterMenuItem);

        removePsmFilterMenuItem.setText("Remove Filter");
        removePsmFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePsmFilterMenuItemActionPerformed(evt);
            }
        });
        psmPopupMenu.add(removePsmFilterMenuItem);

        addPeptideFilterMenuItem.setText("Add Filter");
        addPeptideFilterMenuItem.setToolTipText("Add a new filter");
        addPeptideFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPeptideFilterMenuItemActionPerformed(evt);
            }
        });
        peptidePopupMenu.add(addPeptideFilterMenuItem);

        editPeptideFilterMenuItem.setText("Edit Filter");
        editPeptideFilterMenuItem.setToolTipText("Edit the selected filter");
        editPeptideFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPeptideFilterMenuItemActionPerformed(evt);
            }
        });
        peptidePopupMenu.add(editPeptideFilterMenuItem);

        removePeptideFilterMenuItem.setText("Remove Filter");
        removePeptideFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePeptideFilterMenuItemActionPerformed(evt);
            }
        });
        peptidePopupMenu.add(removePeptideFilterMenuItem);

        addProteinFilterMenuItem.setText("Add Filter");
        addProteinFilterMenuItem.setToolTipText("Add a new filter");
        addProteinFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProteinFilterMenuItemActionPerformed(evt);
            }
        });
        proteinPopupMenu.add(addProteinFilterMenuItem);

        editProteinFilterMenuItem.setText("Edit Filter");
        editProteinFilterMenuItem.setToolTipText("Edit the selected filter");
        editProteinFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProteinFilterMenuItemActionPerformed(evt);
            }
        });
        proteinPopupMenu.add(editProteinFilterMenuItem);

        removeProteinFilterMenuItem.setText("Remove Filter");
        removeProteinFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProteinFilterMenuItemActionPerformed(evt);
            }
        });
        proteinPopupMenu.add(removeProteinFilterMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Quality Control (beta)");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        validationQCPreferencesDialogPanel.setBackground(new java.awt.Color(230, 230, 230));

        generalSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Settings"));
        generalSettingsPanel.setOpaque(false);

        dbCheck.setText("Hits obtained on small databases (<" + SearchParameters.preferredMinSequences + " protein sequences)");
        dbCheck.setIconTextGap(15);
        dbCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbCheckActionPerformed(evt);
            }
        });

        nTargetCheck.setText("Datasets with a low number of target hits");
        nTargetCheck.setIconTextGap(15);
        nTargetCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nTargetCheckActionPerformed(evt);
            }
        });

        markDoubtfulLabel.setText("Mark as Doubtful");

        confidenceCheck.setText("Hits near the confidence threshold (margin= 1 x resolution)");
        confidenceCheck.setIconTextGap(15);
        confidenceCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confidenceCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout generalSettingsPanelLayout = new javax.swing.GroupLayout(generalSettingsPanel);
        generalSettingsPanel.setLayout(generalSettingsPanelLayout);
        generalSettingsPanelLayout.setHorizontalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(confidenceCheck)
                            .addComponent(dbCheck)
                            .addComponent(nTargetCheck)))
                    .addComponent(markDoubtfulLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        generalSettingsPanelLayout.setVerticalGroup(
            generalSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(markDoubtfulLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dbCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nTargetCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(confidenceCheck)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        proteinFiltersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Protein Filters"));
        proteinFiltersPanel.setOpaque(false);

        proteinScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                proteinScrollPaneMouseReleased(evt);
            }
        });

        proteinTable.setModel(new FiltersTableModel(proteinFilters));
        proteinTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                proteinTableMouseReleased(evt);
            }
        });
        proteinScrollPane.setViewportView(proteinTable);

        javax.swing.GroupLayout proteinFiltersPanelLayout = new javax.swing.GroupLayout(proteinFiltersPanel);
        proteinFiltersPanel.setLayout(proteinFiltersPanelLayout);
        proteinFiltersPanelLayout.setHorizontalGroup(
            proteinFiltersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteinFiltersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proteinScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
                .addContainerGap())
        );
        proteinFiltersPanelLayout.setVerticalGroup(
            proteinFiltersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteinFiltersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proteinScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addContainerGap())
        );

        peptideFiltersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptide Filters"));
        peptideFiltersPanel.setOpaque(false);

        peptideScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                peptideScrollPaneMouseReleased(evt);
            }
        });

        peptideTable.setModel(new FiltersTableModel(peptideFilters));
        peptideTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                peptideTableMouseReleased(evt);
            }
        });
        peptideScrollPane.setViewportView(peptideTable);

        javax.swing.GroupLayout peptideFiltersPanelLayout = new javax.swing.GroupLayout(peptideFiltersPanel);
        peptideFiltersPanel.setLayout(peptideFiltersPanelLayout);
        peptideFiltersPanelLayout.setHorizontalGroup(
            peptideFiltersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peptideFiltersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(peptideScrollPane)
                .addContainerGap())
        );
        peptideFiltersPanelLayout.setVerticalGroup(
            peptideFiltersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peptideFiltersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(peptideScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addContainerGap())
        );

        psmFiltersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PSM Filters"));
        psmFiltersPanel.setOpaque(false);

        psmScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                psmScrollPaneMouseReleased(evt);
            }
        });

        psmTable.setModel(new FiltersTableModel(psmFilters));
        psmTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                psmTableMouseReleased(evt);
            }
        });
        psmScrollPane.setViewportView(psmTable);

        javax.swing.GroupLayout psmFiltersPanelLayout = new javax.swing.GroupLayout(psmFiltersPanel);
        psmFiltersPanel.setLayout(psmFiltersPanelLayout);
        psmFiltersPanelLayout.setHorizontalGroup(
            psmFiltersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psmFiltersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(psmScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
                .addContainerGap())
        );
        psmFiltersPanelLayout.setVerticalGroup(
            psmFiltersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psmFiltersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(psmScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addContainerGap())
        );

        helpLbl.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        helpLbl.setText("Right-click in the tables to edit the filters.");

        okButton.setText("OK");
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

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.setBorder(null);
        helpJButton.setBorderPainted(false);
        helpJButton.setContentAreaFilled(false);
        helpJButton.setFocusable(false);
        helpJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        helpJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
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

        javax.swing.GroupLayout validationQCPreferencesDialogPanelLayout = new javax.swing.GroupLayout(validationQCPreferencesDialogPanel);
        validationQCPreferencesDialogPanel.setLayout(validationQCPreferencesDialogPanelLayout);
        validationQCPreferencesDialogPanelLayout.setHorizontalGroup(
            validationQCPreferencesDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validationQCPreferencesDialogPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(validationQCPreferencesDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(validationQCPreferencesDialogPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(helpJButton)
                        .addGap(18, 18, 18)
                        .addComponent(helpLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(generalSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(psmFiltersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(proteinFiltersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(peptideFiltersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        validationQCPreferencesDialogPanelLayout.setVerticalGroup(
            validationQCPreferencesDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, validationQCPreferencesDialogPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(proteinFiltersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peptideFiltersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(psmFiltersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(validationQCPreferencesDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpJButton)
                    .addComponent(helpLbl)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(validationQCPreferencesDialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(validationQCPreferencesDialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the dialog without saving the changes.
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
        if (!userInput) {
            canceled = true;
        }
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Add a new PSM filter.
     *
     * @param evt
     */
    private void addPsmFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPsmFilterMenuItemActionPerformed
        Filter newFilter = validationQCPreferencesDialogParent.createPsmFilter();
        if (newFilter != null) {
            psmFilters.add(newFilter);
            ((DefaultTableModel) psmTable.getModel()).fireTableDataChanged();
            userInput = true;
        }
    }//GEN-LAST:event_addPsmFilterMenuItemActionPerformed

    /**
     * Edit a PSM filter.
     *
     * @param evt
     */
    private void editPsmFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPsmFilterMenuItemActionPerformed
        int row = psmTable.getSelectedRow();
        Filter selectedFilter = psmFilters.get(row);
        Filter editedFilter = validationQCPreferencesDialogParent.editFilter(selectedFilter);
        if (editedFilter != null) {
            psmFilters.set(row, editedFilter);
            ((DefaultTableModel) psmTable.getModel()).fireTableDataChanged();
            userInput = true;
        }
    }//GEN-LAST:event_editPsmFilterMenuItemActionPerformed

    /**
     * Show the PSM filter popup menu or edit a PSM filter.
     *
     * @param evt
     */
    private void psmTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psmTableMouseReleased
        if (evt != null && psmTable.rowAtPoint(evt.getPoint()) != -1) {
            int row = psmTable.rowAtPoint(evt.getPoint());
            psmTable.setRowSelectionInterval(row, row);
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON3 && editable) {
            editPsmFilterMenuItem.setVisible(psmTable.getSelectedRow() != -1);
            removePsmFilterMenuItem.setVisible(psmTable.getSelectedRow() != -1);
            psmPopupMenu.show(psmTable, evt.getX(), evt.getY());
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2 && editable) {
            editPsmFilterMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_psmTableMouseReleased

    /**
     * Add a new peptide filter.
     *
     * @param evt
     */
    private void addPeptideFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPeptideFilterMenuItemActionPerformed
        Filter newFilter = validationQCPreferencesDialogParent.createPeptideFilter();
        if (newFilter != null) {
            peptideFilters.add(newFilter);
            ((DefaultTableModel) peptideTable.getModel()).fireTableDataChanged();
            userInput = true;
        }
    }//GEN-LAST:event_addPeptideFilterMenuItemActionPerformed

    /**
     * Edit a peptide filter.
     *
     * @param evt
     */
    private void editPeptideFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPeptideFilterMenuItemActionPerformed
        int row = peptideTable.getSelectedRow();
        Filter selectedFilter = peptideFilters.get(row);
        Filter editedFilter = validationQCPreferencesDialogParent.editFilter(selectedFilter);
        if (editedFilter != null) {
            peptideFilters.set(row, editedFilter);
            ((DefaultTableModel) peptideTable.getModel()).fireTableDataChanged();
            userInput = true;
        }
    }//GEN-LAST:event_editPeptideFilterMenuItemActionPerformed

    /**
     * Add a new protein filter.
     *
     * @param evt
     */
    private void addProteinFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProteinFilterMenuItemActionPerformed
        Filter newFilter = validationQCPreferencesDialogParent.createProteinFilter();
        if (newFilter != null) {
            proteinFilters.add(newFilter);
            ((DefaultTableModel) proteinTable.getModel()).fireTableDataChanged();
            userInput = true;
        }
    }//GEN-LAST:event_addProteinFilterMenuItemActionPerformed

    /**
     * Edit a protein filter.
     *
     * @param evt
     */
    private void editProteinFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProteinFilterMenuItemActionPerformed
        int row = proteinTable.getSelectedRow();
        Filter selectedFilter = proteinFilters.get(row);
        Filter editedFilter = validationQCPreferencesDialogParent.editFilter(selectedFilter);
        if (editedFilter != null) {
            proteinFilters.set(row, editedFilter);
            ((DefaultTableModel) proteinTable.getModel()).fireTableDataChanged();
            userInput = true;
        }
    }//GEN-LAST:event_editProteinFilterMenuItemActionPerformed

    /**
     * Show the peptide filter popup menu or edit a peptide filter.
     *
     * @param evt
     */
    private void peptideTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideTableMouseReleased
        if (evt != null && peptideTable.rowAtPoint(evt.getPoint()) != -1) {
            int row = peptideTable.rowAtPoint(evt.getPoint());
            peptideTable.setRowSelectionInterval(row, row);
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON3 && editable) {
            editPeptideFilterMenuItem.setVisible(peptideTable.getSelectedRow() != -1);
            removePeptideFilterMenuItem.setVisible(peptideTable.getSelectedRow() != -1);
            peptidePopupMenu.show(peptideTable, evt.getX(), evt.getY());
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2 && editable) {
            editPeptideFilterMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_peptideTableMouseReleased

    /**
     * Show the protein filter popup menu or edit a protein filter.
     *
     * @param evt
     */
    private void proteinTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_proteinTableMouseReleased
        if (evt != null && proteinTable.rowAtPoint(evt.getPoint()) != -1) {
            int row = proteinTable.rowAtPoint(evt.getPoint());
            proteinTable.setRowSelectionInterval(row, row);
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON3 && editable) {
            editProteinFilterMenuItem.setVisible(proteinTable.getSelectedRow() != -1);
            removeProteinFilterMenuItem.setVisible(proteinTable.getSelectedRow() != -1);
            proteinPopupMenu.show(proteinTable, evt.getX(), evt.getY());
        }
        if (evt != null && evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2 && editable) {
            editProteinFilterMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_proteinTableMouseReleased

    /**
     * Remove a protein filter.
     *
     * @param evt
     */
    private void removeProteinFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeProteinFilterMenuItemActionPerformed
        int row = proteinTable.getSelectedRow();
        proteinFilters.remove(row);
        ((DefaultTableModel) proteinTable.getModel()).fireTableDataChanged();
        userInput = true;
    }//GEN-LAST:event_removeProteinFilterMenuItemActionPerformed

    /**
     * Remove a peptide filter.
     *
     * @param evt
     */
    private void removePeptideFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePeptideFilterMenuItemActionPerformed
        int row = peptideTable.getSelectedRow();
        peptideFilters.remove(row);
        ((DefaultTableModel) peptideTable.getModel()).fireTableDataChanged();
        userInput = true;
    }//GEN-LAST:event_removePeptideFilterMenuItemActionPerformed

    /**
     * Remove a PSM filter.
     *
     * @param evt
     */
    private void removePsmFilterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePsmFilterMenuItemActionPerformed
        int row = psmTable.getSelectedRow();
        psmFilters.remove(row);
        ((DefaultTableModel) psmTable.getModel()).fireTableDataChanged();
        userInput = true;
    }//GEN-LAST:event_removePsmFilterMenuItemActionPerformed

    /**
     * Show the protein filter popup menu or edit a protein filter.
     *
     * @param evt
     */
    private void proteinScrollPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_proteinScrollPaneMouseReleased
        proteinTableMouseReleased(evt);
    }//GEN-LAST:event_proteinScrollPaneMouseReleased

    /**
     * Closes the dialog without saving.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        canceled = true;
    }//GEN-LAST:event_formWindowClosing

    /**
     * Show the peptide filter popup menu or edit a peptide filter.
     *
     * @param evt
     */
    private void peptideScrollPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideScrollPaneMouseReleased
        peptideTableMouseReleased(evt);
    }//GEN-LAST:event_peptideScrollPaneMouseReleased

    /**
     * Show the PSM filter popup menu or edit a PSM filter.
     *
     * @param evt
     */
    private void psmScrollPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_psmScrollPaneMouseReleased
        psmTableMouseReleased(evt);
    }//GEN-LAST:event_psmScrollPaneMouseReleased

    /**
     * Set user input to true.
     * 
     * @param evt 
     */
    private void dbCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbCheckActionPerformed
        userInput = true;
    }//GEN-LAST:event_dbCheckActionPerformed

    /**
     * Set user input to true.
     * 
     * @param evt 
     */
    private void nTargetCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nTargetCheckActionPerformed
        userInput = true;
    }//GEN-LAST:event_nTargetCheckActionPerformed

    /**
     * Set user input to true.
     * 
     * @param evt 
     */
    private void confidenceCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confidenceCheckActionPerformed
        userInput = true;
    }//GEN-LAST:event_confidenceCheckActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(parentFrame, getClass().getResource("/helpFiles/QualityControlPreferences.html"),
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
            "Quality Control - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addPeptideFilterMenuItem;
    private javax.swing.JMenuItem addProteinFilterMenuItem;
    private javax.swing.JMenuItem addPsmFilterMenuItem;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox confidenceCheck;
    private javax.swing.JCheckBox dbCheck;
    private javax.swing.JMenuItem editPeptideFilterMenuItem;
    private javax.swing.JMenuItem editProteinFilterMenuItem;
    private javax.swing.JMenuItem editPsmFilterMenuItem;
    private javax.swing.JPanel generalSettingsPanel;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel helpLbl;
    private javax.swing.JLabel markDoubtfulLabel;
    private javax.swing.JCheckBox nTargetCheck;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel peptideFiltersPanel;
    private javax.swing.JPopupMenu peptidePopupMenu;
    private javax.swing.JScrollPane peptideScrollPane;
    private javax.swing.JTable peptideTable;
    private javax.swing.JPanel proteinFiltersPanel;
    private javax.swing.JPopupMenu proteinPopupMenu;
    private javax.swing.JScrollPane proteinScrollPane;
    private javax.swing.JTable proteinTable;
    private javax.swing.JPanel psmFiltersPanel;
    private javax.swing.JPopupMenu psmPopupMenu;
    private javax.swing.JScrollPane psmScrollPane;
    private javax.swing.JTable psmTable;
    private javax.swing.JMenuItem removePeptideFilterMenuItem;
    private javax.swing.JMenuItem removeProteinFilterMenuItem;
    private javax.swing.JMenuItem removePsmFilterMenuItem;
    private javax.swing.JPanel validationQCPreferencesDialogPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Indicates whether the preference edition was canceled by the user.
     *
     * @return a boolean indicating whether the preference edition was canceled
     * by the user
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the validation QC preferences as set by the user.
     *
     * @return the validation QC preferences as set by the user
     */
    public ValidationQcParameters getValidationQCPreferences() {
        ValidationQcParameters validationQCPreferences = new ValidationQcParameters();
        validationQCPreferences.setDbSize(dbCheck.isSelected());
        validationQCPreferences.setFirstDecoy(nTargetCheck.isSelected());
        if (!confidenceCheck.isSelected()) {
            validationQCPreferences.setConfidenceMargin(0.0);
        } else {
            validationQCPreferences.setConfidenceMargin(1.0);
        }
        validationQCPreferences.setPsmFilters(psmFilters);
        validationQCPreferences.setPeptideFilters(peptideFilters);
        validationQCPreferences.setProteinFilters(proteinFilters);
        return validationQCPreferences;
    }

    /**
     * Table model for a filters table.
     */
    private class FiltersTableModel extends DefaultTableModel {

        /**
         * List of filters to display.
         */
        private final ArrayList<Filter> filters;

        /**
         * Creates a new table model.
         */
        public FiltersTableModel(ArrayList<Filter> filters) {
            this.filters = filters;
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
                    return "Description";
                default:
                    return "";
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            Filter filter = filters.get(row);
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return filter.getName();
                case 2:
                    return filter.getDescription();
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
}
