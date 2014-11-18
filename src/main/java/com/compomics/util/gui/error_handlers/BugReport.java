package com.compomics.util.gui.error_handlers;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.preferences.LastSelectedFolder;
import java.io.*;
import java.util.Date;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

/**
 * A simple bug report dialog.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class BugReport extends javax.swing.JDialog {

    /**
     * The folder to open in the file selection dialog.
     */
    private LastSelectedFolder lastSelectedFolder;
    /**
     * The specific key for bug reports
     */
    public static final String lastSelectedFolderKey = "bug_report";
    /**
     * The name of the tool to get the bug report for, e.g., "PeptideShaker".
     */
    private String toolName;
    /**
     * The google code name of the tool, e.g., "peptide-shaker".
     */
    private String googleCodeToolName;
    /**
     * The version number of the tool the log files belongs to.
     */
    private String toolVersion;
    /**
     * The name of the Google Group.
     */
    private String googleGroup;
    /**
     * The human readable name of the the Google Group.
     */
    private String googleGroupName;
    /**
     * The log file to display.
     */
    private File logFile;

    /**
     * Creates a new BugReport dialog.
     *
     * @param frame the parent frame
     * @param selectionFolder the folder to open in the file selection dialog
     * @param toolName the name of the tool to get the bug report for, e.g.,
     * "PeptideShaker" or "SearchGUI"
     * @param googleCodeToolName the Google Code name of the tool, e.g.,
     * "peptide-shaker"
     * @param toolVersion the version number of the tool the log files belongs
     * to
     * @param googleGroup the name of the Google Group, e.g., peptide-shaker,
     * setting to null will refer to the PeptideShaker Google Group
     * @param googleGroupName the human readable name of the the Google Group,
     * e.g., PeptideShaker, setting to null will refer to the PeptideShaker
     * Google Group
     * @param logFile the log file to display
     */
    public BugReport(JFrame frame, LastSelectedFolder selectionFolder, String toolName, String googleCodeToolName, String toolVersion, String googleGroup, String googleGroupName, File logFile) {
        super(frame, true);
        this.lastSelectedFolder = selectionFolder;
        this.toolName = toolName;
        this.googleCodeToolName = googleCodeToolName;
        this.toolVersion = toolVersion;
        this.googleGroup = googleGroup;
        this.googleGroupName = googleGroupName;
        this.logFile = logFile;
        initComponents();
        setUpGui();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    /**
     * Creates a new BugReport dialog.
     *
     * @param dialog the parent frame
     * @param selectionFolder the folder to open in the file selection dialog
     * @param toolName the name of the tool to get the bug report for, e.g.,
     * PeptideShaker or SearchGUI
     * @param googleCodeToolName the Google Code name of the tool, e.g.,
     * "peptide-shaker"
     * @param toolVersion the version number of the tool the log files belongs
     * to
     * @param googleGroup the name of the Google Group, e.g., peptide-shaker,
     * setting to null will refer to the PeptideShaker Google Group
     * @param googleGroupName the human readable name of the the Google Group,
     * e.g., PeptideShaker, setting to null will refer to the PeptideShaker
     * Google Group
     * @param logFile the log file to display
     */
    public BugReport(JDialog dialog, LastSelectedFolder selectionFolder, String toolName, String googleCodeToolName, String toolVersion, String googleGroup, String googleGroupName, File logFile) {
        super(dialog, true);
        this.lastSelectedFolder = selectionFolder;
        this.toolName = toolName;
        this.googleCodeToolName = googleCodeToolName;
        this.toolVersion = toolVersion;
        this.googleGroup = googleGroup;
        this.googleGroupName = googleGroupName;
        this.logFile = logFile;
        initComponents();
        setUpGui();
        setLocationRelativeTo(dialog);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {
        insertLogFileContent();
        setTitle(toolName + " Bug Report");
        ((TitledBorder) logJPanel.getBorder()).setTitle(toolName + " Log");

        if (googleGroup == null) {
            googleGroup = "peptide-shaker";
        }
        if (googleGroupName == null) {
            googleGroupName = "PeptideShaker";
        }

        infoJEditorPane.setText("<html><head></head><body>"
                + "If you encounter any bugs while processing your data, please do one of the following:<br>"
                + "<ul>"
                + "<li>Create an issue on the <a href=\"http://code.google.com/p/" + googleCodeToolName + "/issues/list\">" + toolName + " web page</a>.</li>"
                + "<li>Send an e-mail to the <a href=\"http://groups.google.com/group/" + googleGroup + "\">" + googleGroupName + " Google Group</a>.</li>"
                + "</ul>"
                + "Please include any relevant information as well as the log message displayed below."
                + "</body>"
                + "</html>");
    }

    /**
     * Displays the content of the log file.
     */
    private void insertLogFileContent() {
        StringBuilder log = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String line;
            while ((line = br.readLine()) != null) {
                log.append(line).append(System.getProperty("line.separator"));
            }
            br.close();
        } catch (FileNotFoundException e) {
            log.append(toolName).append(".log not found.");
            e.printStackTrace();
        } catch (IOException e) {
            log.append("An error occurred while reading resources/").append(toolName).append(".log.");
            e.printStackTrace();
        }

        logTxt.setText(log.toString());
        logTxt.setCaretPosition(0);
    }
    
    /**
     * Returns the last selected folder.
     * 
     * @return the last selected folder
     */
    private String getLastSelectedFolder() {
        String result = null;
        if (lastSelectedFolder != null) {
            result = lastSelectedFolder.getLastSelectedFolder(lastSelectedFolderKey);
            if (result == null) {
                result = lastSelectedFolder.getLastSelectedFolder();
            }
        }
        return result;
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
        logJPanel = new javax.swing.JPanel();
        logJScrollPane = new javax.swing.JScrollPane();
        logTxt = new javax.swing.JTextArea();
        clearJButton = new javax.swing.JButton();
        saveJButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        infoJPanel = new javax.swing.JPanel();
        infoJScrollPane = new javax.swing.JScrollPane();
        infoJEditorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Bug Report");

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        logJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Log"));
        logJPanel.setOpaque(false);

        logTxt.setColumns(20);
        logTxt.setEditable(false);
        logTxt.setRows(3);
        logTxt.setTabSize(4);
        logJScrollPane.setViewportView(logTxt);

        clearJButton.setText("Clear");
        clearJButton.setToolTipText("Clear the log");
        clearJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearJButtonActionPerformed(evt);
            }
        });

        saveJButton.setText("Save");
        saveJButton.setToolTipText("Save the log to a text file");
        saveJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveJButtonActionPerformed(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.setMaximumSize(new java.awt.Dimension(57, 23));
        exitButton.setMinimumSize(new java.awt.Dimension(57, 23));
        exitButton.setPreferredSize(new java.awt.Dimension(57, 23));
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout logJPanelLayout = new javax.swing.GroupLayout(logJPanel);
        logJPanel.setLayout(logJPanelLayout);
        logJPanelLayout.setHorizontalGroup(
            logJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logJPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(exitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        logJPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clearJButton, exitButton, saveJButton});

        logJPanelLayout.setVerticalGroup(
            logJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(logJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveJButton)
                    .addComponent(clearJButton)
                    .addComponent(exitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6))
        );

        logJPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {clearJButton, exitButton, saveJButton});

        infoJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Info"));
        infoJPanel.setOpaque(false);

        infoJEditorPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        infoJEditorPane.setContentType("text/html");
        infoJEditorPane.setEditable(false);
        infoJEditorPane.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\nIf you encounter any bugs while processing your data, please do one of the following:\n<br>\n<ul>\n  <li>Create an issue on the <a href=\"http://code.google.com/p/peptide-shaker/issues/list\">PeptideShaker web page</a>.</li>\n  <li>or Send an e-mail to the <a href=\"http://groups.google.com/group/peptide-shaker\">PeptideShaker mailing list</a>.</li>\n</ul>\nPlease include any relevant information as well as the log message displayed below.\n  </body>\r\n</html>\r\n");
        infoJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                infoJEditorPaneHyperlinkUpdate(evt);
            }
        });
        infoJScrollPane.setViewportView(infoJEditorPane);

        javax.swing.GroupLayout infoJPanelLayout = new javax.swing.GroupLayout(infoJPanel);
        infoJPanel.setLayout(infoJPanelLayout);
        infoJPanelLayout.setHorizontalGroup(
            infoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoJScrollPane)
                .addContainerGap())
        );
        infoJPanelLayout.setVerticalGroup(
            infoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoJPanelLayout.createSequentialGroup()
                .addComponent(infoJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(infoJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
     * Makes the links active.
     *
     * @param evt
     */
    private void infoJEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_infoJEditorPaneHyperlinkUpdate
        if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ENTERED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.EXITED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.toString())) {
            if (evt.getDescription().startsWith("#")) {
                infoJEditorPane.scrollToReference(evt.getDescription());
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                BareBonesBrowserLaunch.openURL(evt.getDescription());
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_infoJEditorPaneHyperlinkUpdate

    /**
     * Clears the log file.
     *
     * @param evt
     */
    private void clearJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearJButtonActionPerformed

        try {
            FileWriter w = new FileWriter(logFile);
            BufferedWriter bw = new BufferedWriter(w);
            bw.write(System.getProperty("line.separator") + new Date() + ": " + toolName + " version " + toolVersion + "." + System.getProperty("line.separator"));
            bw.close();
            w.close();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Failed to clear the log file.", "File Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to clear the log file.", "File Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        insertLogFileContent();
    }//GEN-LAST:event_clearJButtonActionPerformed

    /**
     * Save the log file to a user specified file.
     *
     * @param evt
     */
    private void saveJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveJButtonActionPerformed

        File finalOutputFile = Util.getUserSelectedFile(this, ".txt", "(Text File) *.txt", "Select Destination File", getLastSelectedFolder(), false);

        if (finalOutputFile != null) {

            try {
                if (logFile.exists()) {
                    Util.copyFile(logFile, finalOutputFile);
                }

                if (!finalOutputFile.exists()) {
                    JOptionPane.showMessageDialog(this, "An error occurred when saving the log.", "Save Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "The log has been saved to \'" + finalOutputFile.getPath() + "\'.", "Log Saved", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "An error occurred when saving the log file.", "Save Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_saveJButtonActionPerformed

    /**
     * Closes the dialog.
     *
     * @param evt
     */
    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton clearJButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JEditorPane infoJEditorPane;
    private javax.swing.JPanel infoJPanel;
    private javax.swing.JScrollPane infoJScrollPane;
    private javax.swing.JPanel logJPanel;
    private javax.swing.JScrollPane logJScrollPane;
    private javax.swing.JTextArea logTxt;
    private javax.swing.JButton saveJButton;
    // End of variables declaration//GEN-END:variables
}
