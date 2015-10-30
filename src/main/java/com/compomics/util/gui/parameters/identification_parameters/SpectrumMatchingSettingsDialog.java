package com.compomics.util.gui.parameters.identification_parameters;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
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
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.AndromedaSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.CometSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.DirecTagSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.MsAmandaSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.MsgfSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.MyriMatchSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.NovorSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.OmssaSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.PNovoSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.TideSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.XTandemSettingsDialog;
import com.compomics.util.io.ConfigurationFile;
import com.compomics.util.preferences.LastSelectedFolder;
import java.awt.Dialog;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * SpectrumMatchingSettingsDialog.
 *
 * @author Marc Vaudel
 */
public class SpectrumMatchingSettingsDialog extends javax.swing.JDialog {

    /**
     * The parent frame.
     */
    private java.awt.Frame parentFrame;
    /**
     * Boolean indicating whether the user canceled the editing.
     */
    private boolean canceled = false;
    /**
     * The normal dialog icon.
     */
    private Image normalIcon;
    /**
     * The waiting dialog icon.
     */
    private Image waitingIcon;
    /**
     * Boolean indicating whether the settings can be edited by the user.
     */
    private boolean editable;
    /**
     * The last selected folder to use.
     */
    private final LastSelectedFolder lastSelectedFolder;
    /**
     * The configuration file containing the modification use.
     */
    private ConfigurationFile configurationFile;
    /**
     * The current search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The algorithm specific parameters.
     */
    private HashMap<Integer, IdentificationAlgorithmParameter> algorithmParameters;
    /**
     * List of identification algorithms indexes.
     */
    private ArrayList<Integer> advocates = null;

    /**
     * Creates a new SpectrumMatchingSettingsDialog with a frame as owner.
     *
     * @param parentFrame the parent frame
     * @param settingsName the name of the settings
     * @param searchParameters previous search parameters
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param configurationFile a file containing the modification use
     * @param lastSelectedFolder the last selected folder to use
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SpectrumMatchingSettingsDialog(java.awt.Frame parentFrame, String settingsName, SearchParameters searchParameters, Image normalIcon, Image waitingIcon,
            ConfigurationFile configurationFile, LastSelectedFolder lastSelectedFolder, boolean editable) {
        super(parentFrame, true);

        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
        this.configurationFile = configurationFile;
        this.editable = editable;
        this.searchParameters = searchParameters;
        this.algorithmParameters = searchParameters.getAlgorithmSpecificParameters();

        initComponents();
        setUpGui();
        populateGUI(settingsName, searchParameters);
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    /**
     * Creates a new SpectrumMatchingSettingsDialog with a dialog as owner.
     *
     * @param owner the dialog owner
     * @param parentFrame the parent frame
     * @param settingsName the name of the settings
     * @param searchParameters previous search parameters
     * @param normalIcon the normal dialog icon
     * @param waitingIcon the waiting dialog icon
     * @param configurationFile a file containing the modification use
     * @param lastSelectedFolder the last selected folder to use
     * @param editable boolean indicating whether the settings can be edited by
     * the user
     */
    public SpectrumMatchingSettingsDialog(Dialog owner, java.awt.Frame parentFrame, String settingsName, SearchParameters searchParameters, Image normalIcon, Image waitingIcon,
            ConfigurationFile configurationFile, LastSelectedFolder lastSelectedFolder, boolean editable) {
        super(owner, true);

        this.parentFrame = parentFrame;
        this.normalIcon = normalIcon;
        this.waitingIcon = waitingIcon;
        this.lastSelectedFolder = lastSelectedFolder;
        this.configurationFile = configurationFile;
        this.editable = editable;
        this.searchParameters = searchParameters;
        this.algorithmParameters = searchParameters.getAlgorithmSpecificParameters();

        initComponents();
        setUpGui();
        populateGUI(settingsName, searchParameters);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        if (!editable) {
            editButton.setText("View");
        }

    }

    /**
     * Fills the GUI with the given settings.
     *
     * @param searchParameters the spectrum matching settings to display
     */
    private void populateGUI(String settingsName, SearchParameters searchParameters) {
        if (settingsName != null && settingsName.length() > 0) {
            settingsTxt.setText(settingsName);
        } else {
            settingsTxt.setText("Default");
        }
        algorithmSettingsTable.setModel(new AlgorithmSettingsTableModel());
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        generalPanel = new javax.swing.JPanel();
        settingsLbl = new javax.swing.JLabel();
        editButton = new javax.swing.JButton();
        settingsTxt = new javax.swing.JTextField();
        algorithmPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        algorithmSettingsTable = new javax.swing.JTable();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Settings"));
        generalPanel.setOpaque(false);

        settingsLbl.setText("Settings File:");

        editButton.setText("Edit");

        settingsTxt.setEditable(false);
        settingsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(settingsLbl)
                .addGap(18, 18, 18)
                .addComponent(settingsTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settingsLbl)
                    .addComponent(editButton)
                    .addComponent(settingsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        algorithmPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithm Settings"));
        algorithmPanel.setOpaque(false);

        algorithmSettingsTable.setModel(new AlgorithmSettingsTableModel());
        algorithmSettingsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                algorithmSettingsTableMouseMoved(evt);
            }
        });
        algorithmSettingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                algorithmSettingsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                algorithmSettingsTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(algorithmSettingsTable);

        javax.swing.GroupLayout algorithmPanelLayout = new javax.swing.GroupLayout(algorithmPanel);
        algorithmPanel.setLayout(algorithmPanelLayout);
        algorithmPanelLayout.setHorizontalGroup(
            algorithmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algorithmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
                .addContainerGap())
        );
        algorithmPanelLayout.setVerticalGroup(
            algorithmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algorithmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addContainerGap())
        );

        cancelButton.setText("Cancel");

        okButton.setText("OK");

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(algorithmPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(generalPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(algorithmPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
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
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void algorithmSettingsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_algorithmSettingsTableMouseReleased

        int row = algorithmSettingsTable.getSelectedRow();
        int column = algorithmSettingsTable.getSelectedColumn();

        if (row != -1 && column == 1) {
            Integer advocateIndex = advocates.get(row);
            IdentificationAlgorithmParameter identificationAlgorithmParameter = algorithmParameters.get(advocateIndex);
            AlgorithmSettingsDialog algorithmSettingsDialog = getAlgorithmSettingsDialog(identificationAlgorithmParameter);
            if (algorithmSettingsDialog == null) {
                JOptionPane.showMessageDialog(this, "Dialog not implemented for algorithm of index " + advocateIndex + ".", "File Error", JOptionPane.ERROR_MESSAGE);
            } else if (!algorithmSettingsDialog.isCancelled()) {
                identificationAlgorithmParameter = algorithmSettingsDialog.getParameters();
                algorithmParameters.put(advocateIndex, identificationAlgorithmParameter);
            }
        }


    }//GEN-LAST:event_algorithmSettingsTableMouseReleased

    private void algorithmSettingsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_algorithmSettingsTableMouseMoved
        int column = algorithmSettingsTable.columnAtPoint(evt.getPoint());
        if (column == 1) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_algorithmSettingsTableMouseMoved

    private void algorithmSettingsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_algorithmSettingsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_algorithmSettingsTableMouseExited


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel algorithmPanel;
    private javax.swing.JTable algorithmSettingsTable;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton editButton;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel settingsLbl;
    private javax.swing.JTextField settingsTxt;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns the dialog to use to edit the given parameters.
     *
     * @param identificationAlgorithmParameter the parameters to edit
     *
     * @return the dialog to use to edit the given parameters
     */
    public AlgorithmSettingsDialog getAlgorithmSettingsDialog(IdentificationAlgorithmParameter identificationAlgorithmParameter) {
        if (identificationAlgorithmParameter instanceof AndromedaParameters) {
            return new AndromedaSettingsDialog(this, parentFrame, (AndromedaParameters) identificationAlgorithmParameter, editable);
        } else if (identificationAlgorithmParameter instanceof CometSettingsDialog) {
            return new CometSettingsDialog(this, parentFrame, (CometParameters) identificationAlgorithmParameter, editable);
        } else if (identificationAlgorithmParameter instanceof DirecTagParameters) {
            return new DirecTagSettingsDialog(this, parentFrame, searchParameters, editable);
        } else if (identificationAlgorithmParameter instanceof MsAmandaParameters) {
            return new MsAmandaSettingsDialog(this, parentFrame, (MsAmandaParameters) identificationAlgorithmParameter, editable);
        } else if (identificationAlgorithmParameter instanceof MsgfParameters) {
            return new MsgfSettingsDialog(this, parentFrame, (MsgfParameters) identificationAlgorithmParameter, editable);
        } else if (identificationAlgorithmParameter instanceof MyriMatchParameters) {
            return new MyriMatchSettingsDialog(this, parentFrame, (MyriMatchParameters) identificationAlgorithmParameter, editable);
        } else if (identificationAlgorithmParameter instanceof NovorParameters) {
            return new NovorSettingsDialog(this, parentFrame, searchParameters, editable);
        } else if (identificationAlgorithmParameter instanceof OmssaParameters) {
            return new OmssaSettingsDialog(this, parentFrame, (OmssaParameters) identificationAlgorithmParameter, editable);
        } else if (identificationAlgorithmParameter instanceof PNovoParameters) {
            return new PNovoSettingsDialog(this, parentFrame, searchParameters, editable);
        } else if (identificationAlgorithmParameter instanceof TideParameters) {
            return new TideSettingsDialog(this, parentFrame, (TideParameters) identificationAlgorithmParameter, editable);
        } else if (identificationAlgorithmParameter instanceof XtandemParameters) {
            return new XTandemSettingsDialog(this, parentFrame, (XtandemParameters) identificationAlgorithmParameter, searchParameters.getPtmSettings(), searchParameters.getFragmentIonAccuracy(), editable);
        }
        return null;
    }

    /**
     * Table model for the neutral losses table.
     */
    private class AlgorithmSettingsTableModel extends DefaultTableModel {

        /**
         * Constructor.
         */
        public AlgorithmSettingsTableModel() {
            if (algorithmParameters != null) {
                advocates = new ArrayList<Integer>(algorithmParameters.keySet());
                Collections.sort(advocates);
            }
        }

        @Override
        public int getRowCount() {
            if (advocates == null) {
                return 0;
            }
            return advocates.size();
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
                    return "Algorithm";
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
                    Integer advocateIndex = advocates.get(row);
                    Advocate advocate = Advocate.getAdvocate(advocateIndex);
                    if (advocate == null) {
                        return "Unknown";
                    }
                    return advocate.getName();
                case 2:
                    if (editable) {
                        return "Edit";
                    } else {
                        return "View";
                    }
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
