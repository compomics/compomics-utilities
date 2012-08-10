/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters.filtercreation;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.filters.massspectrometry.spectrumfilters.CombFilter;
import com.compomics.util.experiment.filters.massspectrometry.spectrumfilters.FingerprintPattern;
import com.compomics.util.experiment.filters.massspectrometry.spectrumfilters.MzFilter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * This dialog allows the creation of an MzFilter
 *
 * @author Marc
 */
public class FingerprintDialog extends javax.swing.JDialog {

    /**
     * The created filter
     */
    private SpectrumFilter spectrumFilter = null;
    /**
     * List of the desired m/z to filter
     */
    private ArrayList<Double> mzArray = new ArrayList<Double>();
    /**
     * List of the desired intensities to filter
     */
    private ArrayList<Double> intArray = new ArrayList<Double>();
    /**
     * Constructor
     * @param parent the parent frame
     * @param mzTolerance the default mzTolerance. Can be null.
     * @param intensityQuantile the default intensity quantile. Can be null.
     */
    public FingerprintDialog(JFrame parent, Double mzTolerance, Double intensityQuantile, Boolean isPpm) {
        super(parent, true);
        initComponents();
        if (mzTolerance != null) {
            mzTolTxt.setText(mzTolerance + "");
        }
        if (intensityQuantile != null) {
            intTxt.setText(intensityQuantile + "");
        }
        if (isPpm != null) {
            if (!isPpm) {
                ppmCmb.setSelectedIndex(0);
            } else {
                ppmCmb.setSelectedIndex(1);
            }
        }
        setLocationRelativeTo(parent);
        setVisible(true);
    }
    
    /**
     * Validates the user input
     * @return a boolean indicating whether the user input can be used
     */
    public boolean validateInput() {
        if (mzArray.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one peak.",
                    "Wrong peak", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double test = new Double(mzTolTxt.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the m/z tolerance.",
                    "Wrong m/z tolerance", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double test = new Double(intTxt.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the intensity tolerance.",
                    "Wrong intensity tolerance", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Returns the filter created. Null if none.
     * @return the filter created. Null if none.
     */
    public SpectrumFilter getFilter() {
        return spectrumFilter;
    }

    /**
     * Table model for the peak table.
     */
    private class MzTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return mzArray.size();
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
                    return "m/z";
                case 2:
                    return "intensity";
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
                    return mzArray.get(row);
                case 2:
                    return intArray.get(row);
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (column == 1) {
            String value = aValue.toString().trim();
            if (!value.equals("")) {
                try {
                    Double newValue = new Double(value);
                    mzArray.set(row, newValue);
                } catch (Exception e) {
                    
                }
            }
            } else if (column == 2) {
                String value = aValue.toString().trim();
            if (!value.equals("")) {
                try {
                    Double newValue = new Double(value);
                    intArray.set(row, newValue);
                } catch (Exception e) {
                    
                }
            }
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, columnIndex) != null) {
                    return getValueAt(i, columnIndex).getClass();
                }
            }
            return (new Double(0.0)).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }
    }
    
    /**
     * Repaints the table.
     */
    private void repaintTable() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                mzTable.revalidate();
                mzTable.repaint();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ppmCmb = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        intTxt = new javax.swing.JTextField();
        mzTolTxt = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        mzTable = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("peaks:");

        jLabel2.setText("m/z tolerance:");

        ppmCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Da", "ppm" }));

        jLabel3.setText("Intensity tolerance:");

        jLabel4.setText("%");

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("OK");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        intTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        intTxt.setText("0");

        mzTolTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        mzTolTxt.setText("0.01");

        mzTable.setModel(new MzTable());
        jScrollPane1.setViewportView(mzTable);

        jButton3.setText("+");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("-");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(mzTolTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ppmCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(intTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4))))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(ppmCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mzTolTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(intTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (validateInput()) {
            Double mzTol = new Double(mzTolTxt.getText());
            Double intensityTolerance = new Double(intTxt.getText());
            spectrumFilter = new FingerprintPattern(mzArray, intArray, mzTol, ppmCmb.getSelectedIndex()==1, intensityTolerance);
            String name = "Fingerprint (";
            boolean first = true;
            for (int i = 0 ; i < mzArray.size() ; i++) {
                if (first) {
                    first = false;
                } else {
                    name += ", ";
                }
                name += "[" + mzArray.get(i) + "-" + intArray.get(i) + "]";
            }
            name += ")";
            spectrumFilter.setName(name);
            dispose();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String outcome = JOptionPane.showInputDialog(this, "Please select an m/z to add to the filter");
        try {
            Double mz = new Double(outcome);
            outcome = JOptionPane.showInputDialog(this, "Please select an intensity for peak at " + mz);
            Double intensity = new Double(outcome);
            mzArray.add(mz);
            intArray.add(intensity);
            repaintTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input could not be parsed.",
                    "Wrong input", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        int row = mzTable.convertRowIndexToModel(mzTable.getSelectedRow());
        if (row >= 0) {
        mzArray.remove(row);
        intArray.remove(row);
            repaintTable();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField intTxt;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable mzTable;
    private javax.swing.JTextField mzTolTxt;
    private javax.swing.JComboBox ppmCmb;
    // End of variables declaration//GEN-END:variables
}
