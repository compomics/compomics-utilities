/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.gui;

/**
 * This class groups properties for the tables
 *
 * @author Marc
 */
public class TableProperties {
    
    /**
     * The label with for the numbers in the jsparklines columns.
     */
    private static final int labelWidth = 50;
    /**
     * The color to use for the HTML tags for the selected rows, in HTML color
     * code.
     */
    private static final String selectedRowHtmlTagFontColor = "#FFFFFF";
    /**
     * The color to use for the HTML tags for the rows that are not selected, in
     * HTML color code.
     */
    private static final String notSelectedRowHtmlTagFontColor = "#0101DF";
    

    /**
     * Returns the label width for the sparklines.
     *
     * @return the labelWidth
     */
    public static int getLabelWidth() {
        return labelWidth;
    }

    /**
     * Returns the color to use for the HTML tags for the selected rows, in HTML
     * color code.
     *
     * @return the color to use for the HTML tags for the selected rows, in HTML
     * color code
     */
    public static String getSelectedRowHtmlTagFontColor() {
        return selectedRowHtmlTagFontColor;
    }

    /**
     * Returns the color to use for the HTML tags for the rows that are not
     * selected, in HTML color code.
     *
     * @return the color to use for the HTML tags for the rows that are not
     * selected, in HTML color code
     */
    public static String getNotSelectedRowHtmlTagFontColor() {
        return notSelectedRowHtmlTagFontColor;
    }
}
