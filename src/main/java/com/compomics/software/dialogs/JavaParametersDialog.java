package com.compomics.software.dialogs;

import com.compomics.software.CompomicsWrapper;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.parameters.UtilitiesUserParameters;
import java.awt.Color;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFrame;
import no.uib.jsparklines.renderers.util.Util;

/**
 * A dialog for showing Java memory settings.
 *
 * @author Harald Barsnes
 */
public class JavaParametersDialog extends javax.swing.JDialog {

    /**
     * Reference to the JavaHomeOrMemoryDialogParent.
     */
    private final JavaHomeOrMemoryDialogParent javaHomeOrMemoryDialogParent;
    /**
     * The frame parent.
     */
    private final JFrame frameParent;
    /**
     * The name of the tool, e.g., PeptideShaker.
     */
    private final String toolName;
    /**
     * A reference to the Welcome Dialog.
     */
    private final JDialog welcomeDialog;

    /**
     * Creates a new JavaSettingsDialog.
     *
     * @param parent the parent frame
     * @param javaHomeOrMemoryDialogParent reference to the
     * JavaHomeOrMemoryDialogParent
     * @param toolName the name of the tool, e.g., PeptideShaker
     * @param welcomeDialog reference to the Welcome Dialog, can be null
     * @param modal if the dialog is to be modal or not
     */
    public JavaParametersDialog(
            JFrame parent, 
            JavaHomeOrMemoryDialogParent javaHomeOrMemoryDialogParent, 
            JDialog welcomeDialog, 
            String toolName, 
            boolean modal
    ) {
        super(parent, modal);
        this.frameParent = parent;
        this.javaHomeOrMemoryDialogParent = javaHomeOrMemoryDialogParent;
        this.welcomeDialog = welcomeDialog;
        this.toolName = toolName;
        initComponents();
        setUpGUI();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {
        
        String javaHome = System.getProperty("java.home") + File.separator + "bin" + File.separator;
        javaHomeLabel.setText("<html>" + javaHome + "&nbsp;&nbsp;&nbsp;<a href>Edit</a></u></html>");

        String javaVersion = System.getProperty("java.version");
        versionLabel.setText(javaVersion);

        if (javaVersion.startsWith("1.5") || javaVersion.startsWith("1.6")
                || javaVersion.startsWith("1.7") || javaVersion.startsWith("1.8")) {
            versionLabel.setForeground(Color.red);
            javaHomeLabel.setForeground(Color.red);
        }

        if (CompomicsWrapper.is64BitJava()) {
            bitLabel.setText("64 Bit Java");
        } else {
            bitLabel.setText("32 Bit Java");
            bitLabel.setForeground(Color.red);
        }

        try {
            UtilitiesUserParameters utilitiesUserPreferences = UtilitiesUserParameters.loadUserParameters();
            int maxMemory = utilitiesUserPreferences.getMemoryParameter();
            memoryLabel.setText(
                    "<html>"
                    + Util.roundDouble(maxMemory * 0.000976563, 1)
                    + " GB "
                    + "&nbsp;&nbsp;&nbsp;<a href>Edit</a></u></html>"
            );

            if (maxMemory < 4000) {
                memoryLabel.setForeground(Color.red);
            }
        } catch (Exception e) {
            memoryLabel.setText("Error...");
            e.printStackTrace();
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

        backgroundsPanel = new javax.swing.JPanel();
        javaHomePanel = new javax.swing.JPanel();
        javaHomeLabel = new javax.swing.JLabel();
        versionRecommendationLabel1 = new javax.swing.JLabel();
        versionPanel = new javax.swing.JPanel();
        versionLabel = new javax.swing.JLabel();
        versionRecommendationLabel2 = new javax.swing.JLabel();
        bitPanel = new javax.swing.JPanel();
        bitLabel = new javax.swing.JLabel();
        bitRecommendationLabel = new javax.swing.JLabel();
        memoryPanel = new javax.swing.JPanel();
        memoryLabel = new javax.swing.JLabel();
        memoryRecommendationLabel = new javax.swing.JLabel();
        javaHelpJLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Java Settings");
        setResizable(false);

        backgroundsPanel.setBackground(new java.awt.Color(230, 230, 230));

        javaHomePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Java Home"));
        javaHomePanel.setOpaque(false);

        javaHomeLabel.setText("<html>Java Home...&nbsp;&nbsp;&nbsp;<a href>Edit</a></u></html>");
        javaHomeLabel.setToolTipText("Edit Java Home");
        javaHomeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                javaHomeLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                javaHomeLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                javaHomeLabelMouseReleased(evt);
            }
        });

        versionRecommendationLabel1.setFont(versionRecommendationLabel1.getFont().deriveFont((versionRecommendationLabel1.getFont().getStyle() | java.awt.Font.ITALIC)));
        versionRecommendationLabel1.setText("Required: Java 1.9 or newer");

        javax.swing.GroupLayout javaHomePanelLayout = new javax.swing.GroupLayout(javaHomePanel);
        javaHomePanel.setLayout(javaHomePanelLayout);
        javaHomePanelLayout.setHorizontalGroup(
            javaHomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javaHomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(javaHomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(versionRecommendationLabel1)
                .addContainerGap())
        );
        javaHomePanelLayout.setVerticalGroup(
            javaHomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javaHomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(javaHomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaHomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(versionRecommendationLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        versionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Java Version"));
        versionPanel.setOpaque(false);

        versionLabel.setText("1.7");

        versionRecommendationLabel2.setFont(versionRecommendationLabel2.getFont().deriveFont((versionRecommendationLabel2.getFont().getStyle() | java.awt.Font.ITALIC)));
        versionRecommendationLabel2.setText("Required: Java 1.9 or newer");

        javax.swing.GroupLayout versionPanelLayout = new javax.swing.GroupLayout(versionPanel);
        versionPanel.setLayout(versionPanelLayout);
        versionPanelLayout.setHorizontalGroup(
            versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(versionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(versionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(versionRecommendationLabel2)
                .addContainerGap())
        );
        versionPanelLayout.setVerticalGroup(
            versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(versionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionRecommendationLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bitPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("32 Bit or 64 Bit"));
        bitPanel.setOpaque(false);

        bitLabel.setText("64 Bit Java");

        bitRecommendationLabel.setFont(bitRecommendationLabel.getFont().deriveFont((bitRecommendationLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        bitRecommendationLabel.setText("Recommended: 64 Bit Java");

        javax.swing.GroupLayout bitPanelLayout = new javax.swing.GroupLayout(bitPanel);
        bitPanel.setLayout(bitPanelLayout);
        bitPanelLayout.setHorizontalGroup(
            bitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bitPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(bitRecommendationLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bitPanelLayout.setVerticalGroup(
            bitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bitPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bitLabel)
                    .addComponent(bitRecommendationLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        memoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Memory"));
        memoryPanel.setOpaque(false);

        memoryLabel.setText("<html>60 GB&nbsp;&nbsp;&nbsp;<a href>Edit</a></u></html>");
        memoryLabel.setToolTipText("Edit memory settings");
        memoryLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                memoryLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                memoryLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                memoryLabelMouseReleased(evt);
            }
        });

        memoryRecommendationLabel.setFont(memoryRecommendationLabel.getFont().deriveFont((memoryRecommendationLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        memoryRecommendationLabel.setText("Recommended: at least 4 GB");

        javax.swing.GroupLayout memoryPanelLayout = new javax.swing.GroupLayout(memoryPanel);
        memoryPanel.setLayout(memoryPanelLayout);
        memoryPanelLayout.setHorizontalGroup(
            memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(memoryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(memoryRecommendationLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        memoryPanelLayout.setVerticalGroup(
            memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memoryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryRecommendationLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javaHelpJLabel.setForeground(new java.awt.Color(0, 0, 255));
        javaHelpJLabel.setText("<html><u><i>Java setup help</i></u></html>");
        javaHelpJLabel.setToolTipText("Open Java Help");
        javaHelpJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                javaHelpJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                javaHelpJLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                javaHelpJLabelMouseReleased(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundsPanelLayout = new javax.swing.GroupLayout(backgroundsPanel);
        backgroundsPanel.setLayout(backgroundsPanelLayout);
        backgroundsPanelLayout.setHorizontalGroup(
            backgroundsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(javaHomePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(memoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundsPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(javaHelpJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton))
                    .addComponent(bitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(versionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundsPanelLayout.setVerticalGroup(
            backgroundsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(javaHomePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bitPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(memoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(javaHelpJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void javaHelpJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_javaHelpJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_javaHelpJLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void javaHelpJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_javaHelpJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_javaHelpJLabelMouseExited

    /**
     * Open the JavaTroubleShooting web page.
     *
     * @param evt
     */
    private void javaHelpJLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_javaHelpJLabelMouseReleased
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://compomics.github.io/projects/compomics-utilities/wiki/JavaTroubleShooting.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_javaHelpJLabelMouseReleased

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void javaHomeLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_javaHomeLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_javaHomeLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void javaHomeLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_javaHomeLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_javaHomeLabelMouseExited

    /**
     * Open the JavaHomeDialog.
     *
     * @param evt
     */
    private void javaHomeLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_javaHomeLabelMouseReleased
        new JavaHomeDialog(frameParent, javaHomeOrMemoryDialogParent, welcomeDialog, toolName);
    }//GEN-LAST:event_javaHomeLabelMouseReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void memoryLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memoryLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_memoryLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void memoryLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memoryLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_memoryLabelMouseExited

    /**
     * Open the JavaMemoryDialog.
     *
     * @param evt
     */
    private void memoryLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memoryLabelMouseReleased
        new JavaMemoryDialog(frameParent, javaHomeOrMemoryDialogParent, welcomeDialog, toolName);
    }//GEN-LAST:event_memoryLabelMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundsPanel;
    private javax.swing.JLabel bitLabel;
    private javax.swing.JPanel bitPanel;
    private javax.swing.JLabel bitRecommendationLabel;
    private javax.swing.JLabel javaHelpJLabel;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JPanel javaHomePanel;
    private javax.swing.JLabel memoryLabel;
    private javax.swing.JPanel memoryPanel;
    private javax.swing.JLabel memoryRecommendationLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JPanel versionPanel;
    private javax.swing.JLabel versionRecommendationLabel1;
    private javax.swing.JLabel versionRecommendationLabel2;
    // End of variables declaration//GEN-END:variables
}
