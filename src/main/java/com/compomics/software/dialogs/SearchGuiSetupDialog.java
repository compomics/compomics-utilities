package com.compomics.software.dialogs;

import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;
import com.compomics.software.autoupdater.GUIFileDAO;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.gui.file_handling.FileChooserUtil;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.parameters.UtilitiesUserParameters;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

/**
 * A dialog used to set up the connection between PeptideShaker and SearchGUI.
 *
 * @author Harald Barsnes
 */
public class SearchGuiSetupDialog extends javax.swing.JDialog {

    /**
     * The utilities preferences.
     */
    private UtilitiesUserParameters utilitiesUserParameters;
    /**
     * The selected folder.
     */
    private String lastSelectedFolder = "";
    /**
     * Set to true if the dialog was canceled.
     */
    private boolean dialogCanceled = true;
    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * The parent frame. Can be null.
     */
    private JFrame parentFrame = null;
    /**
     * The parent dialog. Can be null.
     */
    private JDialog parentDialog = null;

    /**
     * Creates a new SearchGuiSetupDialog.
     *
     * @param parent the parent frame
     * @param modal if the dialog is to be modal or not
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public SearchGuiSetupDialog(
            JFrame parent, 
            boolean modal
    ) throws FileNotFoundException, IOException, ClassNotFoundException {
    
        super(parent, modal);
        initComponents();
        parentFrame = parent;
        setLocationRelativeTo(parent);
        setUpGUI();
    }

    /**
     * Creates a new SearchGuiSetupDialog.
     *
     * @param parent the parent dialog
     * @param modal if the dialog is to be modal or not
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public SearchGuiSetupDialog(
            JDialog parent, 
            boolean modal
    ) throws FileNotFoundException, IOException, ClassNotFoundException {
    
        super(parent, modal);
        initComponents();
        parentDialog = parent;
        setLocationRelativeTo(parent);
        setUpGUI();
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {
        
        utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

        if (utilitiesUserParameters.getSearchGuiPath() == null) {
            
            boolean downloaded = downloadSearchGUI();
            
            if (downloaded) {
            
                dialogCanceled = false;
            
            } else {
            
                // display the current searchgui path
                if (utilitiesUserParameters != null) {
                    searchGuiInstallationJTextField.setText(utilitiesUserParameters.getSearchGuiPath());
                    lastSelectedFolder = utilitiesUserParameters.getSearchGuiPath();
                }

                setVisible(true);
            }
        } else {

            // display the current searchgui path
            if (utilitiesUserParameters != null) {
                searchGuiInstallationJTextField.setText(utilitiesUserParameters.getSearchGuiPath());
                lastSelectedFolder = utilitiesUserParameters.getSearchGuiPath();
            }

            setVisible(true);
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

        jLabel2 = new javax.swing.JLabel();
        backgroundPanel = new javax.swing.JPanel();
        searchGuiInstallationPanel = new javax.swing.JPanel();
        searchGuiInstallationJTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        searchGuiJarFileHelpLabel = new javax.swing.JLabel();
        searchGuiDownloadPanel = new javax.swing.JPanel();
        searchGuiInfoLabel = new javax.swing.JLabel();
        searchGuiDownloadLinkLabel = new javax.swing.JLabel();
        searchGuiButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        jLabel2.setText("jLabel2");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("SearchGUI Settings");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        searchGuiInstallationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("SearchGUI Installation"));
        searchGuiInstallationPanel.setOpaque(false);

        searchGuiInstallationJTextField.setEditable(false);
        searchGuiInstallationJTextField.setToolTipText("The folder containing the SearchGUI jar file.");

        browseButton.setText("Browse");
        browseButton.setToolTipText("The folder containing the SearchGUI jar file.");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        searchGuiJarFileHelpLabel.setFont(searchGuiJarFileHelpLabel.getFont().deriveFont((searchGuiJarFileHelpLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        searchGuiJarFileHelpLabel.setText("Please locate the folder containing the SearchGUI jar file.");

        javax.swing.GroupLayout searchGuiInstallationPanelLayout = new javax.swing.GroupLayout(searchGuiInstallationPanel);
        searchGuiInstallationPanel.setLayout(searchGuiInstallationPanelLayout);
        searchGuiInstallationPanelLayout.setHorizontalGroup(
            searchGuiInstallationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchGuiInstallationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchGuiInstallationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchGuiInstallationPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(searchGuiJarFileHelpLabel))
                    .addComponent(searchGuiInstallationJTextField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton)
                .addContainerGap())
        );
        searchGuiInstallationPanelLayout.setVerticalGroup(
            searchGuiInstallationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchGuiInstallationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchGuiInstallationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchGuiInstallationJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchGuiJarFileHelpLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        searchGuiDownloadPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Download SearchGUI"));
        searchGuiDownloadPanel.setOpaque(false);

        searchGuiInfoLabel.setFont(searchGuiInfoLabel.getFont().deriveFont(searchGuiInfoLabel.getFont().getStyle() | java.awt.Font.BOLD));
        searchGuiInfoLabel.setText("SearchGUI -  a graphical user interface for proteomics identification search engines");

        searchGuiDownloadLinkLabel.setText("<html>Download here: <a href>https://compomics.github.io/projects/searchgui.html</a></html>");
        searchGuiDownloadLinkLabel.setToolTipText("Go to https://compomics.github.io/projects/searchgui.html");
        searchGuiDownloadLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchGuiDownloadLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchGuiDownloadLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchGuiDownloadLinkLabelMouseExited(evt);
            }
        });

        searchGuiButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/searchgui-medium-shadow.png"))); // NOI18N
        searchGuiButton.setToolTipText("Go to http://compomics.github.io/projects/searchgui.html");
        searchGuiButton.setBorderPainted(false);
        searchGuiButton.setContentAreaFilled(false);
        searchGuiButton.setFocusPainted(false);
        searchGuiButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchGuiButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchGuiButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchGuiButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout searchGuiDownloadPanelLayout = new javax.swing.GroupLayout(searchGuiDownloadPanel);
        searchGuiDownloadPanel.setLayout(searchGuiDownloadPanelLayout);
        searchGuiDownloadPanelLayout.setHorizontalGroup(
            searchGuiDownloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchGuiDownloadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchGuiDownloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchGuiDownloadPanelLayout.createSequentialGroup()
                        .addComponent(searchGuiInfoLabel)
                        .addGap(0, 32, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchGuiDownloadPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(searchGuiDownloadLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(searchGuiButton)
                .addContainerGap())
        );
        searchGuiDownloadPanelLayout.setVerticalGroup(
            searchGuiDownloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchGuiDownloadPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(searchGuiInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchGuiDownloadLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(searchGuiButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        okButton.setText("OK");
        okButton.setEnabled(false);
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

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchGuiDownloadPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(searchGuiInstallationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchGuiInstallationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchGuiDownloadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
     * Open a file chooser were the user can select the SearchGUI jar file.
     *
     * @param evt
     */
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed

        File selectedFile = FileChooserUtil.getUserSelectedFile(
                this, 
                ".jar", 
                "SearchGUI jar file (.jar)", 
                "Select SearchGUI Jar File", 
                lastSelectedFolder, 
                null, 
                true
        );

        if (selectedFile != null) {
            
            if (!selectedFile.getName().endsWith(".jar")) {
                
                JOptionPane.showMessageDialog(
                        this, 
                        "The selected file is not a jar file.", 
                        "Wrong File Selected", 
                        JOptionPane.WARNING_MESSAGE
                );
                okButton.setEnabled(false);
                
            } else if (!selectedFile.getName().contains("SearchGUI")) {
 
                JOptionPane.showMessageDialog(
                        this, 
                        "The selected file is not a SearchGUI jar file.", 
                        "Wrong File Selected", 
                        JOptionPane.WARNING_MESSAGE
                );
                okButton.setEnabled(false);
                
            } else {
               
                // file assumed to be ok
                lastSelectedFolder = selectedFile.getPath();
                searchGuiInstallationJTextField.setText(lastSelectedFolder);
                okButton.setEnabled(true);
            
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    /**
     * Save the SearchGUI mapping and close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        utilitiesUserParameters.setSearchGuiPath(searchGuiInstallationJTextField.getText());

        try {

            UtilitiesUserParameters.saveUserParameters(utilitiesUserParameters);
            dialogCanceled = false;

        } catch (Exception e) {

            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while saving the preferences.", "Error", JOptionPane.WARNING_MESSAGE);

        }

        dispose();

    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void searchGuiDownloadLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGuiDownloadLinkLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchGuiDownloadLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void searchGuiDownloadLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGuiDownloadLinkLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchGuiDownloadLinkLabelMouseExited

    /**
     * Opens the SearchGUI web page.
     *
     * @param evt
     */
    private void searchGuiDownloadLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGuiDownloadLinkLabelMouseClicked
        openSearchGuiWebPage();
    }//GEN-LAST:event_searchGuiDownloadLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void searchGuiButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGuiButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchGuiButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void searchGuiButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGuiButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchGuiButtonMouseExited

    /**
     * Opens the SearchGUI web page.
     *
     * @param evt
     */
    private void searchGuiButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGuiButtonMouseClicked
        openSearchGuiWebPage();
    }//GEN-LAST:event_searchGuiButtonMouseClicked

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
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton okButton;
    private javax.swing.JButton searchGuiButton;
    private javax.swing.JLabel searchGuiDownloadLinkLabel;
    private javax.swing.JPanel searchGuiDownloadPanel;
    private javax.swing.JLabel searchGuiInfoLabel;
    private javax.swing.JTextField searchGuiInstallationJTextField;
    private javax.swing.JPanel searchGuiInstallationPanel;
    private javax.swing.JLabel searchGuiJarFileHelpLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Opens the SearchGUI web page.
     */
    private void openSearchGuiWebPage() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://compomics.github.io/projects/searchgui.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Returns true of the dialog was canceled by the user.
     *
     * @return the dialogCanceled
     */
    public boolean isDialogCanceled() {
        return dialogCanceled;
    }

    /**
     * Download SearchGUI.
     *
     * @return true if not canceled
     */
    public boolean downloadSearchGUI() {

        int option = JOptionPane.showConfirmDialog(this, "Cannot find SearchGUI. "
                    + "Do you want to download it now? (Select \'No\' if already downloaded.)", "Download SearchGUI?", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {

            String installPath = "user.home";
            
            if (utilitiesUserParameters.getPeptideShakerPath() != null) {
                if (new File(utilitiesUserParameters.getPeptideShakerPath()).getParentFile() != null
                        && new File(utilitiesUserParameters.getPeptideShakerPath()).getParentFile().getParentFile() != null) {
                    installPath = new File(utilitiesUserParameters.getPeptideShakerPath()).getParentFile().getParent();
                }
            }

            final File downloadFolder = FileChooserUtil.getUserSelectedFolder(
                    this, 
                    "Select SearchGUI Folder", 
                    installPath, 
                    "SearchGUI Folder", 
                    "Select", 
                    false
            );

            if (downloadFolder != null) {

                if (parentFrame != null) {
                    progressDialog = new ProgressDialogX(parentFrame,
                            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                            true);
                } else if (parentDialog != null) {
                    progressDialog = new ProgressDialogX(parentDialog, (JFrame) parentDialog.getParent(),
                            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                            true);
                } else {
                    progressDialog = new ProgressDialogX(new JFrame(),
                            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                            true);
                }

                progressDialog.setPrimaryProgressCounterIndeterminate(true);
                progressDialog.setTitle("Downloading SearchGUI. Please Wait...");

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            progressDialog.setVisible(true);
                        } catch (IndexOutOfBoundsException e) {
                            // ignore
                        }
                    }
                }, "ProgressDialog").start();

                Thread thread = new Thread("DownloadThread") {
                    @Override
                    public void run() {
                        try {
                            URL jarRepository = new URL(
                                    "https", 
                                    "genesis.ugent.be", 
                                    new StringBuilder().append("/maven2/").toString()
                            );
                            downloadLatestZipFromRepo(
                                    downloadFolder, 
                                    "SearchGUI", 
                                    "eu.isas.searchgui", 
                                    "SearchGUI", 
                                    "searchgui.ico",
                                    null, 
                                    jarRepository, 
                                    false, 
                                    true, 
                                    new GUIFileDAO(), 
                                    progressDialog
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (XMLStreamException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (progressDialog.isRunCanceled()) {
                    progressDialog.setRunFinished();
                    return false;
                } else {
                    if (!progressDialog.isRunFinished()) {
                        progressDialog.setRunFinished();
                    }
                }

                return true;
            }
        }

        return false;
    }
}
