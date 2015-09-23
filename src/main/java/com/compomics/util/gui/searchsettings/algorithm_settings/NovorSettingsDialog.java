package com.compomics.util.gui.searchsettings.algorithm_settings;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.DirecTagParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsAmandaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsgfParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.NovorParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PNovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PepnovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

/**
 * Dialog for editing Novor advanced settings. ¨
 *
 * @author Harald Barsnes
 */
public class NovorSettingsDialog extends javax.swing.JDialog {

    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * True if the dialog was canceled by the user.
     */
    private boolean canceled = false;

    /**
     * Creates a new NovorSettingsDialog.
     *
     * @param parent the parent frame
     * @param searchParameters the search parameters
     * @param modal whether the dialog is modal or not
     */
    public NovorSettingsDialog(JFrame parent, SearchParameters searchParameters, boolean modal) {
        super(parent, modal);
        this.searchParameters = searchParameters;
        initComponents();
        setUpGUI();
        insertData();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {
        fragmentationMethodCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        massAnalyzerCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
    }

    /**
     * Insert the settings.
     */
    private void insertData() {

        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), new OmssaParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), new XtandemParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), new MsgfParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), new CometParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), new MsAmandaParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), new MyriMatchParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), new PepnovoParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), new DirecTagParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), new PNovoParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex()) == null) {
            searchParameters.setIdentificationAlgorithmParameter(Advocate.novor.getIndex(), new NovorParameters());
        }

        NovorParameters novorParameters = (NovorParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex());

        fragmentationMethodCmb.setSelectedItem(novorParameters.getFragmentationMethod());
        massAnalyzerCmb.setSelectedItem(novorParameters.getMassAnalyzer());
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
        okButton = new javax.swing.JButton();
        novorPanel = new javax.swing.JPanel();
        fragmentationMethodLabel = new javax.swing.JLabel();
        fragmentationMethodCmb = new javax.swing.JComboBox();
        massAnalyzerLabel = new javax.swing.JLabel();
        massAnalyzerCmb = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Novor Advanced Settings");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        novorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("De Novo Settings"));
        novorPanel.setOpaque(false);

        fragmentationMethodLabel.setText("Fragmentation Method");

        fragmentationMethodCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "HCD", "CID" }));

        massAnalyzerLabel.setText("Mass Analyzer");

        massAnalyzerCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Trap", "TOF", "FT" }));

        javax.swing.GroupLayout novorPanelLayout = new javax.swing.GroupLayout(novorPanel);
        novorPanel.setLayout(novorPanelLayout);
        novorPanelLayout.setHorizontalGroup(
            novorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(novorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(novorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(novorPanelLayout.createSequentialGroup()
                        .addComponent(fragmentationMethodLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(fragmentationMethodCmb, 0, 130, Short.MAX_VALUE))
                    .addGroup(novorPanelLayout.createSequentialGroup()
                        .addComponent(massAnalyzerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(massAnalyzerCmb, 0, 130, Short.MAX_VALUE)))
                .addGap(20, 20, 20))
        );
        novorPanelLayout.setVerticalGroup(
            novorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(novorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(novorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentationMethodLabel)
                    .addComponent(fragmentationMethodCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(novorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(massAnalyzerLabel)
                    .addComponent(massAnalyzerCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        openDialogHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        openDialogHelpJButton.setToolTipText("Open the Novor web page");
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

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(novorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(openDialogHelpJButton)
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
                .addComponent(novorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openDialogHelpJButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addGap(5, 5, 5))
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

        boolean valid = validateParametersInput(true);

        if (valid) {
            setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Open the Novor web page.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://rapidnovor.com");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Change the icon into a hand cursor.
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
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox fragmentationMethodCmb;
    private javax.swing.JLabel fragmentationMethodLabel;
    private javax.swing.JComboBox massAnalyzerCmb;
    private javax.swing.JLabel massAnalyzerLabel;
    private javax.swing.JPanel novorPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateParametersInput(boolean showMessage) {
        boolean valid = true;
        okButton.setEnabled(valid);
        return valid;
    }

    /**
     * Returns the search parameters as set in the GUI.
     *
     * @return the search parameters as set in the GUI
     */
    public SearchParameters getSearchParametersFromGUI() {

        SearchParameters tempSearchParameters = new SearchParameters();
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex()));
        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.novor.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex()));

        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), new OmssaParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), new XtandemParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), new MsgfParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), new MsAmandaParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), new MyriMatchParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), new CometParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), new TideParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), new AndromedaParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), new PepnovoParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), new DirecTagParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), new PNovoParameters());
        }
        if (tempSearchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex()) == null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.novor.getIndex(), new NovorParameters());
        }

        tempSearchParameters.setEnzyme(searchParameters.getEnzyme());
        tempSearchParameters.setParametersFile(searchParameters.getParametersFile());
        tempSearchParameters.setFragmentIonAccuracy(searchParameters.getFragmentIonAccuracy());
        tempSearchParameters.setFragmentAccuracyType(searchParameters.getFragmentAccuracyType());
        tempSearchParameters.setPrecursorAccuracy(searchParameters.getPrecursorAccuracy());
        tempSearchParameters.setPrecursorAccuracyType(searchParameters.getPrecursorAccuracyType());
        tempSearchParameters.setPtmSettings(searchParameters.getPtmSettings());

        NovorParameters novorParameters = new NovorParameters();
        novorParameters.setFragmentationMethod((String) fragmentationMethodCmb.getSelectedItem());
        novorParameters.setMassAnalyzer((String) massAnalyzerCmb.getSelectedItem());

        tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.novor.getIndex(), novorParameters);

        return tempSearchParameters;
    }

    /**
     * Returns true if the dialog was canceled by the user.
     * 
     * @return the canceled
     */
    public boolean isCanceled() {
        return canceled;
    }
}
