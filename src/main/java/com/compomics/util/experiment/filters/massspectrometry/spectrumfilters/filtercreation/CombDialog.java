package com.compomics.util.experiment.filters.massspectrometry.spectrumfilters.filtercreation;

import com.compomics.util.experiment.filters.massspectrometry.SpectrumFilter;
import com.compomics.util.experiment.filters.massspectrometry.spectrumfilters.CombFilter;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * This dialog allows the creation of an MzFilter.
 *
 * @author Marc Vaudel
 */
public class CombDialog extends javax.swing.JDialog {

    /**
     * The created filter.
     */
    private SpectrumFilter spectrumFilter = null;
    /**
     * List of the desired m/z to filter.
     */
    private ArrayList<Double> mzArray = new ArrayList<Double>();

    /**
     * Constructor.
     *
     * @param parent the parent frame
     * @param mzTolerance the default mzTolerance. Can be null.
     * @param intensityQuantile the default intensity quantile. Can be null.
     * @param isPpm  
     */
    public CombDialog(JFrame parent, Double mzTolerance, Double intensityQuantile, Boolean isPpm) {
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
     * Validates the user input.
     *
     * @return a boolean indicating whether the user input can be used
     */
    public boolean validateInput() {
        if (mzArray.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one m/z.",
                    "Wrong m/z", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            new Double(mzTolTxt.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the m/z tolerance.",
                    "Wrong m/z tolerance", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            new Double(intTxt.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please verify the input for the intensity quantile.",
                    "Wrong intensity quantile", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Returns the filter created. Null if none.
     *
     * @return the filter created. Null if none.
     */
    public SpectrumFilter getFilter() {
        return spectrumFilter;
    }

    /**
     * Table model for the m/z table.
     */
    private class MzTable extends DefaultTableModel {

        @Override
        public int getRowCount() {
            return mzArray.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return " ";
                case 1:
                    return "m/z";
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
        cancelButton = new javax.swing.JButton();
        okButtonButton = new javax.swing.JButton();
        intTxt = new javax.swing.JTextField();
        mzTolTxt = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        mzTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("m/z:");

        jLabel2.setText("m/z Accuracy:");

        ppmCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Da", "ppm" }));

        jLabel3.setText("Intensity Quantile:");

        jLabel4.setText("%");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButtonButton.setText("OK");
        okButtonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonButtonActionPerformed(evt);
            }
        });

        intTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        intTxt.setText("0");

        mzTolTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        mzTolTxt.setText("0.01");

        mzTable.setModel(new MzTable());
        jScrollPane1.setViewportView(mzTable);

        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("-");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
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
                            .addComponent(intTxt)
                            .addComponent(mzTolTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ppmCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel4))))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButtonButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                            .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)))
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
                    .addComponent(cancelButton)
                    .addComponent(okButtonButton))
                .addContainerGap())
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
     * Saves the information and then closes the dialog.
     * 
     * @param evt 
     */
    private void okButtonButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonButtonActionPerformed
        if (validateInput()) {
            Double mzTol = new Double(mzTolTxt.getText());
            Double intQuantile = new Double(intTxt.getText());
            Collections.sort(mzArray);
            spectrumFilter = new CombFilter(mzArray, mzTol, ppmCmb.getSelectedIndex() == 1, intQuantile);
            String name = "Comb (";
            boolean first = true;
            for (double mz : mzArray) {
                if (first) {
                    first = false;
                } else {
                    name += ", ";
                }
                name += mz;
            }
            name += ")";
            spectrumFilter.setName(name);
            dispose();
        }
    }//GEN-LAST:event_okButtonButtonActionPerformed

    /**
     * Add a filter.
     * 
     * @param evt 
     */
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Object outcome = JOptionPane.showInputDialog(this, "Please select an m/z to add to the filter");
        try {
            Double mz = new Double(outcome + "");
            mzArray.add(mz);
            repaintTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "m/z: " + outcome + " could not be added.",
                    "Wrong m/z", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addButtonActionPerformed

    /**
     * Remove a filter.
     * 
     * @param evt 
     */
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int row = mzTable.convertRowIndexToModel(mzTable.getSelectedRow());
        if (row >= 0) {
            mzArray.remove(row);
            repaintTable();
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField intTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable mzTable;
    private javax.swing.JTextField mzTolTxt;
    private javax.swing.JButton okButtonButton;
    private javax.swing.JComboBox ppmCmb;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
