package com.compomics.util.gui.waiting.waitinghandlers;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.gui.DummyFrame;
import com.compomics.util.threading.SimpleSemaphore;
import com.compomics.util.waiting.WaitingActionListener;
import com.compomics.util.waiting.WaitingHandler;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;

/**
 * A dialog displaying progress details.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class WaitingDialog extends javax.swing.JDialog implements WaitingHandler {

    /**
     * Needed for the shaking feature.
     */
    private JDialog dialog;
    /**
     * Used in the shaking feature.
     */
    private Point naturalLocation;
    /**
     * Timer for the shaking feature.
     */
    private Timer shakeTimer;
    /**
     * Boolean indicating whether the process is finished.
     */
    private boolean runFinished = false;
    /**
     * Boolean indicating whether the process is canceled.
     */
    private boolean runCanceled = false;
    /**
     * Set if the waiting handler is to show the progress for the current
     * process or not. Useful when running subprocesses that one wants to be
     * able to cancel but do not want to show the progress for.
     */
    private boolean displayProgress = true;
    /**
     * An array list of the tip of the day.
     */
    private ArrayList<String> tips = new ArrayList<>();
    /**
     * The current tip index.
     */
    private int currentTipIndex = -1;
    /**
     * The last selected folder
     */
    private String lastSelectedFolder;
    /**
     * The dialog/frame icon to use when waiting.
     */
    private Image waitingIcon;
    /**
     * The dialog/frame icon to use when done.
     */
    private Image normalIcon;
    /**
     * The waitingHandlerParent.
     */
    private Frame waitingHandlerParent;
    /**
     * If true, the dialog will shake when completed. Mainly a PeptideShaker
     * feature.
     */
    private boolean shakeWhenFinished;
    /**
     * The name of the process we are waiting for, e.g., 'Import Data' or
     * 'Search'.
     */
    private String processName;
    /**
     * The name of the tool using the waiting dialog. Used for the report.
     */
    private String toolName;
    /**
     * The version number of the tool using the waiting dialog. Used for the
     * report.
     */
    private String toolVersion;
    /**
     * The waiting action listener.
     */
    private WaitingActionListener waitingActionListener = null;
    /**
     * The line break type.
     */
    private String lineBreak = System.getProperty("line.separator");
    /**
     * Mutex to synchronize multiple threads on the primary error bar.
     */
    private final SimpleSemaphore primaryMutex = new SimpleSemaphore(1);
    /**
     * Mutex to synchronize multiple threads on the secondary error bar.
     */
    private final SimpleSemaphore secondaryMutex = new SimpleSemaphore(1);
    /**
     * Mutex to synchronize multiple threads writing text.
     */
    private final SimpleSemaphore textMutex = new SimpleSemaphore(1);

    /**
     * Empty default constructor
     */
    public WaitingDialog() {
    }

    /**
     * Creates a new WaitingDialog.
     *
     * @param waitingHandlerParent a reference to the handler parent
     * @param waitingIcon the dialog/frame icon to use when waiting
     * @param normalIcon the dialog/frame icon to use when done
     * @param shakeWhenFinished shake when completed, mainly a PeptideShaker
     * feature
     * @param processName the name of the process we are waiting for, e.g.,
     * 'Import Data' or 'Search'
     * @param toolName the name of the tool, need for the report
     * @param toolVersion the version number of the tool, needed for the report
     * @param modal if the dialog is to be modal or not
     */
    public WaitingDialog(
            Frame waitingHandlerParent, 
            Image normalIcon, 
            Image waitingIcon, 
            boolean shakeWhenFinished, 
            String processName,
            String toolName, 
            String toolVersion, 
            boolean modal
    ) {
        this(
                waitingHandlerParent, 
                normalIcon, 
                waitingIcon, 
                shakeWhenFinished, 
                new ArrayList<String>(0), 
                processName, 
                toolName, 
                toolVersion, 
                modal
        );
    }

    /**
     * Creates a new WaitingDialog.
     *
     * @param waitingHandlerParent a reference to the handler parent
     * @param waitingIcon the dialog/frame icon to use when waiting
     * @param normalIcon the dialog/frame icon to use when done
     * @param shakeWhenFinished shake when completed, mainly a PeptideShaker
     * feature
     * @param processName the name of the process we are waiting for
     * @param modal if the dialog is to be modal or not
     * @param toolName the name of the tool, need for the report
     * @param toolVersion the version number of the tool, needed for the report
     * @param tips the list of Tip of the day
     */
    public WaitingDialog(
            Frame waitingHandlerParent, 
            Image normalIcon, 
            Image waitingIcon, 
            boolean shakeWhenFinished,
            ArrayList<String> tips, 
            String processName, 
            String toolName, 
            String toolVersion, 
            boolean modal
    ) {
        super(waitingHandlerParent, modal);
        initComponents();

        ((DefaultCaret) this.reportTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        this.waitingHandlerParent = waitingHandlerParent;
        this.waitingIcon = waitingIcon;
        this.normalIcon = normalIcon;
        this.shakeWhenFinished = shakeWhenFinished;
        this.processName = processName;
        this.toolName = toolName;
        this.toolVersion = toolVersion;

        setTitle(processName + ". Please Wait...");

        setSecondaryProgressCounterIndeterminate(true);

        // update the layout in the layered pane
        resizeLayeredPanes();

        // set up the tip of the day
        if (tips != null && !tips.isEmpty()) {
            setTipOfTheDay(tips);
            tipOfTheDayEditorPane.setText(getTipOfTheDay());
        } else {
            tipOfTheDayJPanel.setVisible(false);
            showTipOfTheDayCheckBox.setSelected(false);
            showTipOfTheDayCheckBox.setEnabled(false);
        }

        if (waitingHandlerParent != null) {
            this.setLocationRelativeTo(waitingHandlerParent);

            // change the icon to a "waiting version"
            if (waitingIcon != null) {
                waitingHandlerParent.setIconImage(waitingIcon);
            }
        }
    }

    /**
     * Set if the dialog is to be closed when the process is complete.
     *
     * @param close if the dialog is to be closed when the process is complete
     */
    public void closeWhenComplete(
            boolean close
    ) {
        closeDialogWhenImportCompletesCheckBox.setSelected(close);
    }

    /**
     * Set up the list of tip of the day.
     */
    private void setTipOfTheDay(
            ArrayList<String> tips
    ) {
    
        this.tips = tips;

        if (tips == null || tips.isEmpty()) {
            showTipOfTheDayCheckBox.setSelected(false);
            showTipOfTheDayCheckBox.setEnabled(false);
            tipOfTheDayJPanel.setVisible(false);
        }
    }

    @Override
    public void setPrimaryProgressCounter(
            int value
    ) {
        if (displayProgress) {
            
            primaryMutex.acquire();
            progressBar.setValue(value);
            primaryMutex.release();
            
        }
    }

    @Override
    public void setMaxPrimaryProgressCounter(
            int maxProgressValue
    ) {
        if (displayProgress) {
            progressBar.setMaximum(maxProgressValue);
        }
    }

    @Override
    public void increasePrimaryProgressCounter() {
        increasePrimaryProgressCounter(1);
    }

    @Override
    public void increasePrimaryProgressCounter(
            int value
    ) {
        primaryMutex.acquire();
        final int newValue = progressBar.getValue() + value;
        
        if (displayProgress) {
        
            progressBar.setValue(newValue);
        
        }
        
        primaryMutex.release();
    }

    @Override
    public void setMaxSecondaryProgressCounter(
            int maxProgressValue
    ) {
        if (displayProgress) {
            secondaryJProgressBar.setValue(0);
            secondaryJProgressBar.setMaximum(maxProgressValue);
        }
    }

    @Override
    public void resetSecondaryProgressCounter() {
        if (displayProgress) {
            secondaryJProgressBar.setIndeterminate(false);
            secondaryJProgressBar.setStringPainted(true);
            secondaryJProgressBar.setValue(0);
        }
    }

    @Override
    public void resetPrimaryProgressCounter() {
        if (displayProgress) {
            // @TODO: perhaps this should be added to the waiting handler interface?
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
            progressBar.setValue(0);
        }
    }

    /**
     * Increase the secondary progress bar value by one "counter".
     */
    @Override
    public void increaseSecondaryProgressCounter() {
        increaseSecondaryProgressCounter(1);
    }

    @Override
    public void setSecondaryProgressCounter(
            int value
    ) {
        if (displayProgress) {
            
            secondaryMutex.acquire();
            secondaryJProgressBar.setValue(value);
            secondaryMutex.release();
            
        }
    }

    @Override
    public void increaseSecondaryProgressCounter(
            int value
    ) {
        
        secondaryMutex.acquire();
        
        final int newValue = secondaryJProgressBar.getValue() + value;
        
        if (displayProgress) {
            secondaryJProgressBar.setValue(newValue);
        }
        
        secondaryMutex.release();
    }

    @Override
    public void setSecondaryProgressCounterIndeterminate(boolean indeterminate) {
        if (displayProgress) {
            // this split pane trick should not be needed, but if not used the look and feel of the
            // indeterminate progress bar changes when moving back and forth between the two...
            if (indeterminate) {
                secondaryProgressBarSplitPane.setDividerLocation(secondaryProgressBarSplitPane.getWidth());
            } else {
                secondaryProgressBarSplitPane.setDividerLocation(0);
            }
        }
    }

    /**
     * Returns the last selected folder. Null if none set.
     *
     * @return the last selected folder as string
     */
    public String getLastSelectedFolder() {
        return lastSelectedFolder;
    }

    /**
     * Sets the last selected folder.
     *
     * @param lastSelectedFolder the last selected folder as string
     */
    public void setLastSelectedFolder(String lastSelectedFolder) {
        this.lastSelectedFolder = lastSelectedFolder;
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
        processProgressPanel = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        layeredPane = new javax.swing.JLayeredPane();
        tipOfTheDayJPanel = new javax.swing.JPanel();
        tipOfTheDayLayeredPane = new javax.swing.JLayeredPane();
        tipOfTheDayScrollPane = new javax.swing.JScrollPane();
        tipOfTheDayEditorPane = new javax.swing.JEditorPane();
        closeJButton = new javax.swing.JButton();
        nextJButton = new javax.swing.JButton();
        reportScrollPane = new javax.swing.JScrollPane();
        reportTextArea = new javax.swing.JTextArea();
        secondaryProgressBarSplitPane = new javax.swing.JSplitPane();
        secondaryJProgressBar = new javax.swing.JProgressBar();
        tempJProgressBar = new javax.swing.JProgressBar();
        okButton = new javax.swing.JButton();
        saveReportLabel = new javax.swing.JLabel();
        showTipOfTheDayCheckBox = new javax.swing.JCheckBox();
        closeDialogWhenImportCompletesCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Importing Data. Please Wait...");
        setMinimumSize(new java.awt.Dimension(700, 500));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        processProgressPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Progress"));
        processProgressPanel.setOpaque(false);

        progressBar.setToolTipText("Total Progress");
        progressBar.setStringPainted(true);

        tipOfTheDayJPanel.setOpaque(false);
        tipOfTheDayJPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                tipOfTheDayJPanelComponentResized(evt);
            }
        });

        tipOfTheDayScrollPane.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
        tipOfTheDayScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tipOfTheDayScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        tipOfTheDayEditorPane.setEditable(false);
        tipOfTheDayEditorPane.setBackground(new java.awt.Color(240, 240, 240));
        tipOfTheDayEditorPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 240, 240), 20));
        tipOfTheDayEditorPane.setContentType("text/html"); // NOI18N
        tipOfTheDayEditorPane.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n<body style=\"background-color:#F0F0F0;\">\n    <p style=\"margin-top: 0\">\r\n     <b> \rTip of the Day!</b>\n     <br><br>\n     Did you know that. Did you know that. Did you know that. Did you know that. Did you know that. \n     Did you know that.  Did you know that.  Did you know that.  Did you know that.  Did you know that.\n    <br><br>\n    Did you know that.  Did you know that.  Did you know that.  Did you know that.  Did you know that.\n    </p>\r\n  </body>\r\n</html>\r\n");
        tipOfTheDayEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                tipOfTheDayEditorPaneHyperlinkUpdate(evt);
            }
        });
        tipOfTheDayScrollPane.setViewportView(tipOfTheDayEditorPane);

        tipOfTheDayLayeredPane.add(tipOfTheDayScrollPane);
        tipOfTheDayScrollPane.setBounds(0, 2, 210, 260);

        closeJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/close_grey.png"))); // NOI18N
        closeJButton.setToolTipText("Close");
        closeJButton.setBorderPainted(false);
        closeJButton.setContentAreaFilled(false);
        closeJButton.setIconTextGap(0);
        closeJButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        closeJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/close.png"))); // NOI18N
        closeJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeJButtonMouseExited(evt);
            }
        });
        closeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeJButtonActionPerformed(evt);
            }
        });
        tipOfTheDayLayeredPane.setLayer(closeJButton, javax.swing.JLayeredPane.POPUP_LAYER);
        tipOfTheDayLayeredPane.add(closeJButton);
        closeJButton.setBounds(170, 0, 40, 41);

        nextJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/next_grey.png"))); // NOI18N
        nextJButton.setToolTipText("Next Tip");
        nextJButton.setBorderPainted(false);
        nextJButton.setContentAreaFilled(false);
        nextJButton.setIconTextGap(0);
        nextJButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        nextJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/next.png"))); // NOI18N
        nextJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                nextJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                nextJButtonMouseExited(evt);
            }
        });
        nextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextJButtonActionPerformed(evt);
            }
        });
        tipOfTheDayLayeredPane.setLayer(nextJButton, javax.swing.JLayeredPane.POPUP_LAYER);
        tipOfTheDayLayeredPane.add(nextJButton);
        nextJButton.setBounds(170, 230, 40, 41);

        javax.swing.GroupLayout tipOfTheDayJPanelLayout = new javax.swing.GroupLayout(tipOfTheDayJPanel);
        tipOfTheDayJPanel.setLayout(tipOfTheDayJPanelLayout);
        tipOfTheDayJPanelLayout.setHorizontalGroup(
            tipOfTheDayJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tipOfTheDayLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
        );
        tipOfTheDayJPanelLayout.setVerticalGroup(
            tipOfTheDayJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tipOfTheDayLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
        );

        layeredPane.setLayer(tipOfTheDayJPanel, javax.swing.JLayeredPane.POPUP_LAYER);
        layeredPane.add(tipOfTheDayJPanel);
        tipOfTheDayJPanel.setBounds(610, 200, 210, 270);

        reportTextArea.setEditable(false);
        reportTextArea.setColumns(20);
        reportTextArea.setLineWrap(true);
        reportTextArea.setRows(5);
        reportTextArea.setWrapStyleWord(true);
        reportScrollPane.setViewportView(reportTextArea);

        layeredPane.add(reportScrollPane);
        reportScrollPane.setBounds(0, 0, 840, 490);

        secondaryProgressBarSplitPane.setBorder(null);
        secondaryProgressBarSplitPane.setDividerLocation(0);
        secondaryProgressBarSplitPane.setDividerSize(0);

        secondaryJProgressBar.setToolTipText("Current Process Progress");
        secondaryJProgressBar.setStringPainted(true);
        secondaryProgressBarSplitPane.setRightComponent(secondaryJProgressBar);

        tempJProgressBar.setToolTipText("Current Process Progress");
        tempJProgressBar.setIndeterminate(true);
        tempJProgressBar.setString("");
        secondaryProgressBarSplitPane.setLeftComponent(tempJProgressBar);

        javax.swing.GroupLayout processProgressPanelLayout = new javax.swing.GroupLayout(processProgressPanel);
        processProgressPanel.setLayout(processProgressPanelLayout);
        processProgressPanelLayout.setHorizontalGroup(
            processProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processProgressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(processProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(layeredPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 842, Short.MAX_VALUE)
                    .addGroup(processProgressPanelLayout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secondaryProgressBarSplitPane, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        processProgressPanelLayout.setVerticalGroup(
            processProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processProgressPanelLayout.createSequentialGroup()
                .addGroup(processProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(secondaryProgressBarSplitPane)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(layeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                .addContainerGap())
        );

        okButton.setText("Cancel");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        saveReportLabel.setText("<html><a style=\"text-decoration: none\" href=\\\"dummy_link\">Save Report</a></html>");
        saveReportLabel.setToolTipText("Save the report to a text file");
        saveReportLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                saveReportLabelMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveReportLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveReportLabelMouseExited(evt);
            }
        });

        showTipOfTheDayCheckBox.setSelected(true);
        showTipOfTheDayCheckBox.setText("Show Tip of the Day");
        showTipOfTheDayCheckBox.setToolTipText("Show/Hide Tip of the Day");
        showTipOfTheDayCheckBox.setIconTextGap(10);
        showTipOfTheDayCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTipOfTheDayCheckBoxActionPerformed(evt);
            }
        });

        closeDialogWhenImportCompletesCheckBox.setText("Close this dialog when the process is complete.");
        closeDialogWhenImportCompletesCheckBox.setIconTextGap(10);
        closeDialogWhenImportCompletesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDialogWhenImportCompletesCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(processProgressPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(saveReportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(showTipOfTheDayCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeDialogWhenImportCompletesCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(processProgressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(saveReportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showTipOfTheDayCheckBox)
                    .addComponent(closeDialogWhenImportCompletesCheckBox))
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
     * Cancels the process if ongoing or opens the results if finished.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (runFinished || runCanceled) {

            // remove the dummy frame parent as well
            if (waitingHandlerParent instanceof DummyFrame) {
                ((DummyFrame) waitingHandlerParent).dispose();
            }

            this.dispose();
        } else {
            setRunCanceled();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Changes the cursor into a hand cursor when hovering above the Save Report
     * link.
     *
     * @param evt
     */
    private void saveReportLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveReportLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
}//GEN-LAST:event_saveReportLabelMouseEntered

    /**
     * Changing the cursor back to the default cursor.
     *
     * @param evt
     */
    private void saveReportLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveReportLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_saveReportLabelMouseExited

    /**
     * Saves the search report to file.
     *
     * @param evt
     */
    private void saveReportLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveReportLabelMouseReleased
        File outputFile = null;
        JFileChooser fc = new JFileChooser(getLastSelectedFolder());

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File myFile) {
                return myFile.getName().toLowerCase().endsWith("txt")
                        || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Supported formats: txt (.txt)";
            }
        };

        fc.setFileFilter(filter);
        int result = fc.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            outputFile = fc.getSelectedFile();
            if (outputFile.exists()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        new String[]{"The file " + outputFile.getName() + " already exists!", "Overwrite?"},
                        "File Already Exists", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }

        if (outputFile != null) {
            saveReport(outputFile);
        }
}//GEN-LAST:event_saveReportLabelMouseReleased

    /**
     * Update the layout in the layered pane.
     *
     * @param evt
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

        // resize the layered panels
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                resizeLayeredPanes();
            }
        });
    }//GEN-LAST:event_formComponentResized

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        okButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Update the tip of the day layered pane.
     *
     * @param evt
     */
    private void tipOfTheDayJPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tipOfTheDayJPanelComponentResized
        // resize the layered panels
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                resizeLayeredPanes();
            }
        });
    }//GEN-LAST:event_tipOfTheDayJPanelComponentResized

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void closeJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_closeJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void closeJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_closeJButtonMouseExited

    /**
     * Hide the tip of the day.
     *
     * @param evt
     */
    private void closeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeJButtonActionPerformed
        tipOfTheDayJPanel.setVisible(false);
        showTipOfTheDayCheckBox.setSelected(false);
    }//GEN-LAST:event_closeJButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void nextJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nextJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_nextJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void nextJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nextJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_nextJButtonMouseExited

    /**
     * Open the next random tip.
     *
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
        tipOfTheDayEditorPane.setText(getTipOfTheDay());
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * Show/hide the tip of the day.
     *
     * @param evt
     */
    private void showTipOfTheDayCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTipOfTheDayCheckBoxActionPerformed
        tipOfTheDayJPanel.setVisible(showTipOfTheDayCheckBox.isSelected());
    }//GEN-LAST:event_showTipOfTheDayCheckBoxActionPerformed

    /**
     * Make the links active.
     *
     * @param evt
     */
    private void tipOfTheDayEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_tipOfTheDayEditorPaneHyperlinkUpdate
        if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.toString())) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            BareBonesBrowserLaunch.openURL(evt.getDescription());
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_tipOfTheDayEditorPaneHyperlinkUpdate

    /**
     * Save the user preference for if the waiting dialog is to be closed or
     * not.
     *
     * @param evt
     */
    private void closeDialogWhenImportCompletesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeDialogWhenImportCompletesCheckBoxActionPerformed
        // @TODO: save the setting!!
        //peptideShakerGUI.getUserPreferences().setCloseWaitingDialogWhenImportCompletes(closeDialogWhenImportCompletesCheckBox.isSelected());
        //peptideShakerGUI.saveUserPreferences();
    }//GEN-LAST:event_closeDialogWhenImportCompletesCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JCheckBox closeDialogWhenImportCompletesCheckBox;
    private javax.swing.JButton closeJButton;
    private javax.swing.JLayeredPane layeredPane;
    private javax.swing.JButton nextJButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel processProgressPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane reportScrollPane;
    private javax.swing.JTextArea reportTextArea;
    private javax.swing.JLabel saveReportLabel;
    private javax.swing.JProgressBar secondaryJProgressBar;
    private javax.swing.JSplitPane secondaryProgressBarSplitPane;
    private javax.swing.JCheckBox showTipOfTheDayCheckBox;
    private javax.swing.JProgressBar tempJProgressBar;
    private javax.swing.JEditorPane tipOfTheDayEditorPane;
    private javax.swing.JPanel tipOfTheDayJPanel;
    private javax.swing.JLayeredPane tipOfTheDayLayeredPane;
    private javax.swing.JScrollPane tipOfTheDayScrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setRunFinished() {
        runFinished = true;
        okButton.setText("OK");
        progressBar.setIndeterminate(false);
        progressBar.setValue(progressBar.getMaximum());
        progressBar.setStringPainted(true);
        this.setTitle(processName + " - Completed!");

        secondaryProgressBarSplitPane.setDividerLocation(0);
        secondaryJProgressBar.setIndeterminate(false);
        secondaryJProgressBar.setValue(secondaryJProgressBar.getMaximum());
        secondaryJProgressBar.setString(processName + " Completed!");

        // change the icon back to the default version
        if (normalIcon != null && waitingHandlerParent != null) {
            waitingHandlerParent.setIconImage(normalIcon);
        }

        // make the dialog shake for a couple of seconds
        if (shakeWhenFinished) {
            startShake();
        }

        if (closeDialogWhenImportCompletesCheckBox.isSelected()) {
            this.dispose();
        } else {
            closeDialogWhenImportCompletesCheckBox.setEnabled(false);
        }
    }

    @Override
    public void setRunCanceled() {

        if (!runCanceled) {
            runCanceled = true;
            if (waitingActionListener != null) {
                waitingActionListener.cancelPressed();
            }
            appendReportEndLine();
            appendReport(processName + " Canceled!", true, true);
            okButton.setText("OK");
        }

        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        secondaryProgressBarSplitPane.setDividerLocation(0);
        secondaryJProgressBar.setIndeterminate(false);
        secondaryJProgressBar.setValue(0);
        secondaryJProgressBar.setString(processName + " Canceled!");

        this.setTitle(processName + " - Canceled");

        // change the icon back to the default version
        if (normalIcon != null && waitingHandlerParent != null) {
            waitingHandlerParent.setIconImage(normalIcon);
        }
    }

    @Override
    public void appendReport(String report, boolean includeDate, boolean addNewLine) {
        if (displayProgress) {
            
            textMutex.acquire();
            
            if (includeDate) {
                Date date = new Date();
                if (addNewLine) {
                    reportTextArea.append(date + TAB_NON_HTML + report + lineBreak);
                } else {
                    reportTextArea.append(date + TAB_NON_HTML + report);
                }
            } else {
                if (addNewLine) {
                    reportTextArea.append(report + lineBreak);
                } else {
                    reportTextArea.append(report);
                }
            }
            
            textMutex.release();
            
        }
    }

    @Override
    public void appendReportNewLineNoDate() {
        
        if (displayProgress) {
            
            textMutex.acquire();
            reportTextArea.append(TAB_NON_HTML);
            textMutex.release();
            
        }
    }

    @Override
    public void appendReportEndLine() {
        
        if (displayProgress) {
            
            textMutex.acquire();
            reportTextArea.append(lineBreak);
            textMutex.release();
            
        }
    }

    @Override
    public boolean isRunCanceled() {
        return runCanceled;
    }

    /**
     * Saves the report in the given file.
     *
     * @param aFile file to save the report in
     */
    private void saveReport(
            File aFile
    ) {

        String report = getReport(aFile);

        BufferedWriter bw = null;

        try {
            String filePath = aFile.getAbsolutePath();

            if (!filePath.endsWith(".txt")) {
                filePath += ".txt";
            }

            bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(report);
            bw.flush();
            JOptionPane.showMessageDialog(this, "Report written to file '" + filePath + "'.", "Report Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, new String[]{"Error writing report to file:", ioe.getMessage()}, "Save Failed", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(this, new String[]{"Error writing report to file:", ioe.getMessage()}, "Save Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Make the dialog shake when the process has completed.
     */
    public void startShake() {

        naturalLocation = this.getLocation();
        final long startTime = System.currentTimeMillis();

        dialog = this;

        shakeTimer = new Timer(5, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double TWO_PI = Math.PI * 2.0;
                double SHAKE_CYCLE = 50;

                long elapsed = System.currentTimeMillis() - startTime;
                double waveOffset = (elapsed % SHAKE_CYCLE) / SHAKE_CYCLE;
                double angle = waveOffset * TWO_PI;

                int SHAKE_DISTANCE = 10;

                int shakenX = (int) ((Math.sin(angle) * SHAKE_DISTANCE) + naturalLocation.x);
                dialog.setLocation(shakenX, naturalLocation.y);
                dialog.repaint();

                int SHAKE_DURATION = 1000;

                if (elapsed >= SHAKE_DURATION) {
                    stopShake();
                }
            }
        });

        shakeTimer.start();
    }

    /**
     * Stop the dialog shake.
     */
    private void stopShake() {
        shakeTimer.stop();
        dialog.setLocation(naturalLocation);
        dialog.repaint();

        appendReport("Your peptides have been shaken!", true, true);
        appendReportEndLine();
    }

    /**
     * Returns the secondary progress bar for updates from external processes.
     *
     * @return the secondary progress bar
     */
    public JProgressBar getSecondaryProgressBar() {
        return secondaryJProgressBar;
    }

    /**
     * Resize the layered panes.
     */
    private void resizeLayeredPanes() {

        // invoke later to give time for components to update
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                if (layeredPane.getComponentCount() == 2) { // check if the dialog has been canceled

                    // resize the report area
                    layeredPane.getComponent(1).setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());

                    // move the tip of the day panel
                    layeredPane.getComponent(0).setBounds(layeredPane.getWidth() - 255, layeredPane.getHeight() - 300, 230, 280);

                    layeredPane.revalidate();
                    layeredPane.repaint();

                    // resize the tip of the day panel
                    tipOfTheDayLayeredPane.getComponent(2).setBounds(0, 0, tipOfTheDayLayeredPane.getWidth(), tipOfTheDayLayeredPane.getHeight());

                    // move the buttons
                    tipOfTheDayLayeredPane.getComponent(0).setBounds(
                            tipOfTheDayLayeredPane.getWidth() - 40, -2,
                            tipOfTheDayLayeredPane.getComponent(0).getWidth(), tipOfTheDayLayeredPane.getComponent(0).getHeight());

                    tipOfTheDayLayeredPane.getComponent(1).setBounds(
                            tipOfTheDayLayeredPane.getWidth() - 40, tipOfTheDayLayeredPane.getHeight() - 35,
                            tipOfTheDayLayeredPane.getComponent(1).getWidth(), tipOfTheDayLayeredPane.getComponent(1).getHeight());

                    tipOfTheDayLayeredPane.revalidate();
                    tipOfTheDayLayeredPane.repaint();
                }
            }
        });
    }

    /**
     * Returns a random tip of the day text as HTML ready to insert into the tip
     * of the day panel.
     *
     * @return a random tip of the day text as HTML
     */
    private String getTipOfTheDay() {

        String htmlStart = "<html><head></head><body style=\"background-color:#F0F0F0;\">"
                + " <p style=\"margin-top: 0\">"
                + "<b>Tip of the Day!</b><br><br>";

        String htmlEnd = "</p></body></html>";

        int newTipIndex = (int) (Math.random() * tips.size());

        while (newTipIndex == currentTipIndex) {
            newTipIndex = (int) (Math.random() * tips.size());
        }

        currentTipIndex = newTipIndex;
        return htmlStart + tips.get(currentTipIndex) + htmlEnd;
    }

    @Override
    public void setWaitingText(String text) {
        if (displayProgress) {
            setTitle(text);
        }
    }

    @Override
    public boolean isRunFinished() {
        return runFinished;
    }

    /**
     * Returns the report.
     *
     * @param aFile The file to send the report to. Note that only the name is
     * used her and included in the report, the report is <b>not</b> sent to the
     * file. Can be null.
     * @return the report
     */
    public String getReport(File aFile) {

        StringBuffer output = new StringBuffer();
        String host = " @ ";

        try {
            host += InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            // Disregard. It's not so bad if we cannot report this.
        }

        // Write the file header.        
        output.append("# ------------------------------------------------------------------" + lineBreak
                + "# " + toolName + " " + toolVersion + " Report File" + lineBreak
                + "#" + lineBreak
                + "# Originally saved by: " + System.getProperty("user.name") + host + lineBreak
                + "#                  on: " + SIMPLE_DATA_FORMAT.format(new Date()) + lineBreak);
        if (aFile != null) {
            output.append("#                  as: " + aFile.getName() + lineBreak);
        }

        output.append("# ------------------------------------------------------------------" + lineBreak + lineBreak + lineBreak);
        output.append(reportTextArea.getText());

        return output.toString();
    }

    @Override
    public void setPrimaryProgressCounterIndeterminate(boolean indeterminate) {
        if (displayProgress) {
            progressBar.setIndeterminate(indeterminate);
            progressBar.setStringPainted(!indeterminate);
        }
    }

    /**
     * Set if the value for if the dialog is to be closed when completed and if
     * the option is enabled or not.
     *
     * @param closeWhenComplete if true, the dialog closes when completed
     * @param enabled enable or disable the check box
     */
    public void setCloseDialogWhenImportCompletes(boolean closeWhenComplete, boolean enabled) {
        closeDialogWhenImportCompletesCheckBox.setSelected(closeWhenComplete);
        closeDialogWhenImportCompletesCheckBox.setEnabled(enabled);
    }

    /**
     * Set the run to not finished.
     */
    public void setRunNotFinished() {

        runFinished = false;
        okButton.setText("Cancel");
        //progressBar.setPrimaryProgressCounterIndeterminate(true);
        //secondaryJProgressBar.setPrimaryProgressCounterIndeterminate(true);

        secondaryJProgressBar.setString(null);

        // change the icon back to the waiting version
        if (waitingIcon != null && waitingHandlerParent != null) {
            waitingHandlerParent.setIconImage(waitingIcon);
        }
    }

    @Override
    public boolean isReport() {
        return true;
    }

    /**
     * Adds a waiting action listener.
     *
     * @param waitingActionListener the waiting action listener
     */
    public void addWaitingActionListener(WaitingActionListener waitingActionListener) {
        this.waitingActionListener = waitingActionListener;
    }

    @Override
    public void setSecondaryProgressText(String text) {
        if (displayProgress) {
            secondaryJProgressBar.setString(text);
        }
    }

    @Override
    public int getPrimaryProgressCounter() {
        return progressBar.getValue();
    }

    @Override
    public int getMaxPrimaryProgressCounter() {
        return progressBar.getMaximum();
    }

    @Override
    public int getSecondaryProgressCounter() {
        return secondaryJProgressBar.getValue();
    }

    @Override
    public int getMaxSecondaryProgressCounter() {
        return secondaryJProgressBar.getMaximum();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        resizeLayeredPanes(); // update the layout in the layered pane
    }

    @Override
    public void setDisplayProgress(boolean displayProgress) {
        this.displayProgress = displayProgress;
    }

    @Override
    public boolean getDisplayProgress() {
        return displayProgress;
    }
}
