package com.compomics.util.gui;

import no.uib.jsparklines.data.XYDataPoint;

/**
 * Dialog for setting the location of the legend items in a Venn diagram.
 *
 * @author Harald Barsnes
 */
public class VennDiagramLegendLocationDialog extends javax.swing.JDialog {

    /**
     * The VennDiagramDialog parent.
     */
    private final VennDiagramDialog vennDiagramDialog;
    /**
     * Legend shift size.
     */
    private double legendShiftSize = 0.01;

    /**
     * Creates a new VennDiagramLegendLocationDialog.
     *
     * @param vennDiagramDialog the VennDiagramDialog parent
     * @param modal if the dialog is to be modal or not
     */
    public VennDiagramLegendLocationDialog(VennDiagramDialog vennDiagramDialog, boolean modal) {
        super(vennDiagramDialog, modal);
        initComponents();
        this.vennDiagramDialog = vennDiagramDialog;

        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            upDatasetDButton.setEnabled(false);
            downDatasetDButton.setEnabled(false);
            leftDatasetDButton.setEnabled(false);
            rightDatasetDButton.setEnabled(false);
            datasetDLabel.setEnabled(false);
        }

        setLocationRelativeTo(vennDiagramDialog);
        setVisible(true);
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
        legendLocationPanel = new javax.swing.JPanel();
        datasetALabel = new javax.swing.JLabel();
        upDatasetAButton = new javax.swing.JButton();
        downDatasetAButton = new javax.swing.JButton();
        leftDatasetAButton = new javax.swing.JButton();
        rightDatasetAButton = new javax.swing.JButton();
        datasetBLabel = new javax.swing.JLabel();
        upDatasetBButton = new javax.swing.JButton();
        downDatasetBButton = new javax.swing.JButton();
        leftDatasetBButton = new javax.swing.JButton();
        rightDatasetBButton = new javax.swing.JButton();
        datasetCLabel = new javax.swing.JLabel();
        upDatasetCButton = new javax.swing.JButton();
        downDatasetCButton = new javax.swing.JButton();
        leftDatasetCButton = new javax.swing.JButton();
        rightDatasetCButton = new javax.swing.JButton();
        datasetDLabel = new javax.swing.JLabel();
        upDatasetDButton = new javax.swing.JButton();
        downDatasetDButton = new javax.swing.JButton();
        leftDatasetDButton = new javax.swing.JButton();
        rightDatasetDButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        shiftSizeSpinner = new javax.swing.JSpinner();
        shiftSizeLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Legend");

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        legendLocationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Legend Location"));
        legendLocationPanel.setOpaque(false);

        datasetALabel.setText("Dataset A");

        upDatasetAButton.setText("<html>&#9650;</html>");
        upDatasetAButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upDatasetAButtonActionPerformed(evt);
            }
        });

        downDatasetAButton.setText("<html>&#9660;</html>");
        downDatasetAButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downDatasetAButtonActionPerformed(evt);
            }
        });

        leftDatasetAButton.setText("<html>&#9668;</html>");
        leftDatasetAButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftDatasetAButtonActionPerformed(evt);
            }
        });

        rightDatasetAButton.setText("<html>&#9658;</html>");
        rightDatasetAButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightDatasetAButtonActionPerformed(evt);
            }
        });

        datasetBLabel.setText("Dataset B");

        upDatasetBButton.setText("<html>&#9650;</html>");
        upDatasetBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upDatasetBButtonActionPerformed(evt);
            }
        });

        downDatasetBButton.setText("<html>&#9660;</html>");
        downDatasetBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downDatasetBButtonActionPerformed(evt);
            }
        });

        leftDatasetBButton.setText("<html>&#9668;</html>");
        leftDatasetBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftDatasetBButtonActionPerformed(evt);
            }
        });

        rightDatasetBButton.setText("<html>&#9658;</html>");
        rightDatasetBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightDatasetBButtonActionPerformed(evt);
            }
        });

        datasetCLabel.setText("Dataset C");

        upDatasetCButton.setText("<html>&#9650;</html>");
        upDatasetCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upDatasetCButtonActionPerformed(evt);
            }
        });

        downDatasetCButton.setText("<html>&#9660;</html>");
        downDatasetCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downDatasetCButtonActionPerformed(evt);
            }
        });

        leftDatasetCButton.setText("<html>&#9668;</html>");
        leftDatasetCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftDatasetCButtonActionPerformed(evt);
            }
        });

        rightDatasetCButton.setText("<html>&#9658;</html>");
        rightDatasetCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightDatasetCButtonActionPerformed(evt);
            }
        });

        datasetDLabel.setText("Dataset D");

        upDatasetDButton.setText("<html>&#9650;</html>");
        upDatasetDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upDatasetDButtonActionPerformed(evt);
            }
        });

        downDatasetDButton.setText("<html>&#9660;</html>");
        downDatasetDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downDatasetDButtonActionPerformed(evt);
            }
        });

        leftDatasetDButton.setText("<html>&#9668;</html>");
        leftDatasetDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftDatasetDButtonActionPerformed(evt);
            }
        });

        rightDatasetDButton.setText("<html>&#9658;</html>");
        rightDatasetDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightDatasetDButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout legendLocationPanelLayout = new javax.swing.GroupLayout(legendLocationPanel);
        legendLocationPanel.setLayout(legendLocationPanelLayout);
        legendLocationPanelLayout.setHorizontalGroup(
            legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(datasetALabel)
                    .addGroup(legendLocationPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(datasetBLabel)))
                .addGap(18, 18, 18)
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(upDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(legendLocationPanelLayout.createSequentialGroup()
                        .addComponent(leftDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rightDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(legendLocationPanelLayout.createSequentialGroup()
                        .addComponent(leftDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rightDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(legendLocationPanelLayout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(datasetCLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, legendLocationPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(datasetDLabel)))
                .addGap(18, 18, 18)
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(upDatasetCButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(legendLocationPanelLayout.createSequentialGroup()
                        .addComponent(leftDatasetCButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rightDatasetCButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(downDatasetCButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(legendLocationPanelLayout.createSequentialGroup()
                        .addComponent(leftDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(rightDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        legendLocationPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {downDatasetAButton, downDatasetBButton, downDatasetCButton, downDatasetDButton, leftDatasetAButton, leftDatasetBButton, leftDatasetCButton, leftDatasetDButton, rightDatasetAButton, rightDatasetBButton, rightDatasetCButton, rightDatasetDButton, upDatasetAButton, upDatasetBButton, upDatasetCButton, upDatasetDButton});

        legendLocationPanelLayout.setVerticalGroup(
            legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upDatasetCButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(datasetALabel)
                    .addComponent(leftDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetCLabel)
                    .addComponent(leftDatasetCButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightDatasetCButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(downDatasetAButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downDatasetCButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(datasetBLabel)
                    .addComponent(leftDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datasetDLabel)
                    .addComponent(leftDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(legendLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(downDatasetDButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downDatasetBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        legendLocationPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {downDatasetAButton, downDatasetBButton, downDatasetCButton, downDatasetDButton, leftDatasetAButton, leftDatasetBButton, leftDatasetCButton, leftDatasetDButton, rightDatasetAButton, rightDatasetBButton, rightDatasetCButton, rightDatasetDButton, upDatasetAButton, upDatasetBButton, upDatasetCButton, upDatasetDButton});

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        shiftSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.01d), Double.valueOf(0.0d), null, Double.valueOf(0.01d)));
        shiftSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                shiftSizeSpinnerStateChanged(evt);
            }
        });

        shiftSizeLabel.setText("Shift Size");

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(shiftSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(shiftSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton))
                    .addComponent(legendLocationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {okButton, shiftSizeSpinner});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(legendLocationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shiftSizeLabel)
                    .addComponent(shiftSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Move the Dataset A legend upwards.
     *
     * @param evt
     */
    private void upDatasetAButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upDatasetAButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAThreeWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() + legendShiftSize));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() + legendShiftSize));
        }
    }//GEN-LAST:event_upDatasetAButtonActionPerformed

    /**
     * Move the Dataset A legend downwards.
     *
     * @param evt
     */
    private void downDatasetAButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downDatasetAButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAThreeWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() - legendShiftSize));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() - legendShiftSize));
        }
    }//GEN-LAST:event_downDatasetAButtonActionPerformed

    /**
     * Move the Dataset A legend to the left.
     *
     * @param evt
     */
    private void leftDatasetAButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftDatasetAButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAThreeWay(new XYDataPoint(oldLocation.getX() - legendShiftSize, oldLocation.getY()));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAFourWay(new XYDataPoint(oldLocation.getX() - legendShiftSize, oldLocation.getY()));
        }
    }//GEN-LAST:event_leftDatasetAButtonActionPerformed

    /**
     * Move the Dataset A legend to the right.
     *
     * @param evt
     */
    private void rightDatasetAButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightDatasetAButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAThreeWay(new XYDataPoint(oldLocation.getX() + legendShiftSize, oldLocation.getY()));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetAFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetAFourWay(new XYDataPoint(oldLocation.getX() + legendShiftSize, oldLocation.getY()));
        }
    }//GEN-LAST:event_rightDatasetAButtonActionPerformed

    /**
     * Move the Dataset B legend upwards.
     *
     * @param evt
     */
    private void upDatasetBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upDatasetBButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBThreeWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() + legendShiftSize));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() + legendShiftSize));
        }
    }//GEN-LAST:event_upDatasetBButtonActionPerformed

    /**
     * Move the Dataset B legend downwards.
     *
     * @param evt
     */
    private void downDatasetBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downDatasetBButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBThreeWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() - legendShiftSize));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() - legendShiftSize));
        }
    }//GEN-LAST:event_downDatasetBButtonActionPerformed

    /**
     * Move the Dataset B legend to the left.
     *
     * @param evt
     */
    private void leftDatasetBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftDatasetBButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBThreeWay(new XYDataPoint(oldLocation.getX() - legendShiftSize, oldLocation.getY()));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBFourWay(new XYDataPoint(oldLocation.getX() - legendShiftSize, oldLocation.getY()));
        }
    }//GEN-LAST:event_leftDatasetBButtonActionPerformed

    /**
     * Move the Dataset B legend to the right.
     *
     * @param evt
     */
    private void rightDatasetBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightDatasetBButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBThreeWay(new XYDataPoint(oldLocation.getX() + legendShiftSize, oldLocation.getY()));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetBFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetBFourWay(new XYDataPoint(oldLocation.getX() + legendShiftSize, oldLocation.getY()));
        }
    }//GEN-LAST:event_rightDatasetBButtonActionPerformed

    /**
     * Move the Dataset C legend upwards.
     *
     * @param evt
     */
    private void upDatasetCButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upDatasetCButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCThreeWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() + legendShiftSize));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() + legendShiftSize));
        }
    }//GEN-LAST:event_upDatasetCButtonActionPerformed

    /**
     * Move the Dataset C legend downwards.
     *
     * @param evt
     */
    private void downDatasetCButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downDatasetCButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCThreeWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() - legendShiftSize));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() - legendShiftSize));
        }
    }//GEN-LAST:event_downDatasetCButtonActionPerformed

    /**
     * Move the Dataset C legend to the left.
     *
     * @param evt
     */
    private void leftDatasetCButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftDatasetCButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCThreeWay(new XYDataPoint(oldLocation.getX() - legendShiftSize, oldLocation.getY()));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCFourWay(new XYDataPoint(oldLocation.getX() - legendShiftSize, oldLocation.getY()));
        }
    }//GEN-LAST:event_leftDatasetCButtonActionPerformed

    /**
     * Move the Dataset C legend to the right.
     *
     * @param evt
     */
    private void rightDatasetCButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightDatasetCButtonActionPerformed
        if (vennDiagramDialog.getVennDiagramPanel().getCurrentVennDiagramType() != VennDiagramPanel.VennDiagramType.FOUR_WAY) {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCThreeWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCThreeWay(new XYDataPoint(oldLocation.getX() + legendShiftSize, oldLocation.getY()));
        } else {
            XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetCFourWay();
            vennDiagramDialog.getVennDiagramPanel().setLegendDatasetCFourWay(new XYDataPoint(oldLocation.getX() + legendShiftSize, oldLocation.getY()));
        }
    }//GEN-LAST:event_rightDatasetCButtonActionPerformed

    /**
     * Move the Dataset D legend upwards.
     *
     * @param evt
     */
    private void upDatasetDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upDatasetDButtonActionPerformed
        XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetDFourWay();
        vennDiagramDialog.getVennDiagramPanel().setLegendDatasetDFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() + legendShiftSize));
    }//GEN-LAST:event_upDatasetDButtonActionPerformed

    /**
     * Move the Dataset D legend downwards.
     *
     * @param evt
     */
    private void downDatasetDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downDatasetDButtonActionPerformed
        XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetDFourWay();
        vennDiagramDialog.getVennDiagramPanel().setLegendDatasetDFourWay(new XYDataPoint(oldLocation.getX(), oldLocation.getY() - legendShiftSize));
    }//GEN-LAST:event_downDatasetDButtonActionPerformed

    /**
     * Move the Dataset D legend to the left.
     *
     * @param evt
     */
    private void leftDatasetDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftDatasetDButtonActionPerformed
        XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetDFourWay();
        vennDiagramDialog.getVennDiagramPanel().setLegendDatasetDFourWay(new XYDataPoint(oldLocation.getX() - legendShiftSize, oldLocation.getY()));
    }//GEN-LAST:event_leftDatasetDButtonActionPerformed

    /**
     * Move the Dataset D legend to the right.
     *
     * @param evt
     */
    private void rightDatasetDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightDatasetDButtonActionPerformed
        XYDataPoint oldLocation = vennDiagramDialog.getVennDiagramPanel().getLegendDatasetDFourWay();
        vennDiagramDialog.getVennDiagramPanel().setLegendDatasetDFourWay(new XYDataPoint(oldLocation.getX() + legendShiftSize, oldLocation.getY()));
    }//GEN-LAST:event_rightDatasetDButtonActionPerformed

    /**
     * Update the shift size.
     * 
     * @param evt 
     */
    private void shiftSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_shiftSizeSpinnerStateChanged
        legendShiftSize = (Double) shiftSizeSpinner.getValue();
    }//GEN-LAST:event_shiftSizeSpinnerStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JLabel datasetALabel;
    private javax.swing.JLabel datasetBLabel;
    private javax.swing.JLabel datasetCLabel;
    private javax.swing.JLabel datasetDLabel;
    private javax.swing.JButton downDatasetAButton;
    private javax.swing.JButton downDatasetBButton;
    private javax.swing.JButton downDatasetCButton;
    private javax.swing.JButton downDatasetDButton;
    private javax.swing.JButton leftDatasetAButton;
    private javax.swing.JButton leftDatasetBButton;
    private javax.swing.JButton leftDatasetCButton;
    private javax.swing.JButton leftDatasetDButton;
    private javax.swing.JPanel legendLocationPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton rightDatasetAButton;
    private javax.swing.JButton rightDatasetBButton;
    private javax.swing.JButton rightDatasetCButton;
    private javax.swing.JButton rightDatasetDButton;
    private javax.swing.JLabel shiftSizeLabel;
    private javax.swing.JSpinner shiftSizeSpinner;
    private javax.swing.JButton upDatasetAButton;
    private javax.swing.JButton upDatasetBButton;
    private javax.swing.JButton upDatasetCButton;
    private javax.swing.JButton upDatasetDButton;
    // End of variables declaration//GEN-END:variables
}
