package com.compomics.util.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 * A class containing simple GUI helper methods.
 *
 * @author Harald Barsnes
 */
public class GuiUtilities {

    /**
     * Returns the preferred width of a given cell in a table.
     *
     * @param table the table
     * @param colIndex the colum index
     * @param rowIndex the row index
     * @return the preferred width of the cell
     */
    public static int getPreferredWidthOfCell(JTable table, int rowIndex, int colIndex) {

        int width = 0;

        // get width of column data
        TableCellRenderer renderer = table.getCellRenderer(rowIndex, colIndex);
        Component comp = renderer.getTableCellRendererComponent(
                table, table.getValueAt(rowIndex, colIndex), false, false, rowIndex, colIndex);
        width = Math.max(width, comp.getPreferredSize().width);

        return width;
    }

    /**
     * Validate integer input.
     *
     * @param parentComponent the parent component
     * @param label the label of the input
     * @param textField the text field containing the input
     * @param valueDescription the description of the input
     * @param errorTitle the error title
     * @param positiveValue if true, only positive values will pass the filter
     * @param showMessage if true, a message will be shown if the validation
     * fails
     * @param valid the status of previous validations
     * @return true of the field is validated, false if not (or if valid is
     * false)
     */
    public static boolean validateIntegerInput(Component parentComponent, JLabel label, JTextField textField,
            String valueDescription, String errorTitle, boolean positiveValue, boolean showMessage, boolean valid) {

        label.setForeground(Color.BLACK);
        label.setToolTipText(null);

        // check that a value is specified
        if (textField.getText() == null || textField.getText().trim().equals("")) {
            if (showMessage) {
                JOptionPane.showMessageDialog(parentComponent, "You need to specify the " + valueDescription + ".", errorTitle, JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            label.setForeground(Color.RED);
            label.setToolTipText("Please select the " + valueDescription);
        }

        int tempValue = -1;

        try {
            tempValue = Integer.parseInt(textField.getText().trim());
        } catch (NumberFormatException nfe) {
            // unparseable integer!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(parentComponent, "You need to specify an integer for " + valueDescription + ".",
                        errorTitle, JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            label.setForeground(Color.RED);
            label.setToolTipText("Please select an integer");
        }

        // and it should be zero or more
        if (positiveValue && tempValue < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(parentComponent, "You need to specify a positive integer for " + valueDescription + ".",
                        errorTitle, JOptionPane.WARNING_MESSAGE);
                textField.requestFocus();
            }
            valid = false;
            label.setForeground(Color.RED);
            label.setToolTipText("Please select a positive integer");
        }

        return valid;
    }

    /**
     * Validate double input.
     *
     * @param parentComponent the parent component
     * @param label the label of the input
     * @param textField the text field containing the input
     * @param valueDescription the description of the input
     * @param errorTitle the error title
     * @param positiveValue if true, only positive values will pass the filter
     * @param showMessage if true, a message will be shown if the validation
     * fails
     * @param valid the status of previous validations
     * @return true of the field is validated, false if not (or if valid is
     * false)
     */
    public static boolean validateDoubleInput(Component parentComponent, JLabel label, JTextField textField,
            String valueDescription, String errorTitle, boolean positiveValue, boolean showMessage, boolean valid) {

        label.setForeground(Color.BLACK);
        label.setToolTipText(null);

        // check that a value is specified
        if (textField.getText() == null || textField.getText().trim().equals("")) {
            if (showMessage) {
                JOptionPane.showMessageDialog(parentComponent, "You need to specify the " + valueDescription + ".", errorTitle, JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            label.setForeground(Color.RED);
            label.setToolTipText("Please select the " + valueDescription);
        }

        double tempValue = -1;

        try {
            tempValue = Double.parseDouble(textField.getText().trim());
        } catch (NumberFormatException nfe) {
            // unparseable double!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(parentComponent, "You need to specify a number for " + valueDescription + ".",
                        errorTitle, JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            label.setForeground(Color.RED);
            label.setToolTipText("Please select a number");
        }

        // and it should be zero or more
        if (positiveValue && tempValue < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(parentComponent, "You need to specify a positive number for " + valueDescription + ".",
                        errorTitle, JOptionPane.WARNING_MESSAGE);
                textField.requestFocus();
            }
            valid = false;
            label.setForeground(Color.RED);
            label.setToolTipText("Please select a positive number");
        }

        return valid;
    }
}
