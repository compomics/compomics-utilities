package com.compomics.util.gui.renderers;

import java.awt.Color;
import java.awt.Component;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * A combo box renderer that allows tooltip for each element in the combo box
 * list.
 *
 * @author Harald Barsnes
 */
public class ToolTipComboBoxRenderer extends BasicComboBoxRenderer {

    /**
     * Empty default constructor
     */
    public ToolTipComboBoxRenderer() {
    }

    /**
     * The tooltips for each element in the list.
     */
    private Vector tooltips;
    /**
     * The horizontal alignment of the text.
     */
    private int align;
    /**
     * The maximum width of a tool tip (in number of characters). Tooltips
     * longer than the maximum will be split into multiple lines.
     */
    private final int MAX_TOOLTIP_WIDTH = 40;
    /**
     * The background color for the list. Defaults to white if not set.
     */
    private Color backgroundColor = Color.WHITE;

    /**
     * Creates a new instance of the MyComboBoxRenderer.
     *
     * @param tooltips vector containing the tooltips
     * @param align the horizontal alignment of the text
     */
    public ToolTipComboBoxRenderer(Vector tooltips, int align) {
        this.tooltips = tooltips;
        this.align = align;
    }

    /**
     * Creates a new instance of the MyComboBoxRenderer.
     *
     * @param tooltips vector containing the tooltips
     * @param align the horizontal alignment of the text
     * @param backgroundColor the background color for the list, defaults to
     * white if not set
     */
    public ToolTipComboBoxRenderer(Vector tooltips, int align, Color backgroundColor) {
        this.tooltips = tooltips;
        this.align = align;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Set the tooltips.
     *
     * @param tooltips vector containing the tooltips
     */
    public void setToolTips(Vector tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        // DefaultListCellRenderer uses a JLabel as the rendering component:
        JLabel lbl = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        if (isSelected) {

            lbl.setBackground(list.getSelectionBackground());
            lbl.setForeground(list.getSelectionForeground());

            if (tooltips != null) {
                if (-1 < index && index < tooltips.size()) {

                    if (tooltips.get(index) != null) {

                        String toolTip = (String) tooltips.get(index);
                        StringTokenizer tok = new StringTokenizer(toolTip);
                        String temp = "", temp2 = "";

                        // splits the tooltip over multiple lines if needed
                        while (tok.hasMoreTokens()) {
                            temp += tok.nextToken() + " ";

                            if (temp.length() > MAX_TOOLTIP_WIDTH) {
                                temp2 += temp + "<br>";
                                temp = "";
                            }
                        }

                        if (temp.length() > 0) {
                            temp2 += temp;
                        }

                        list.setToolTipText("<html>" + temp2 + "</html>");
                    } else {
                        list.setToolTipText(null);
                    }
                }
            } else {
                list.setToolTipText(null);
            }
        } else {
            lbl.setBackground(backgroundColor);
            lbl.setForeground(list.getForeground());
        }

        lbl.setFont(list.getFont());
        lbl.setText((value == null) ? "" : value.toString());

        // set the alignment of the label text
        if (("" + value).length() < MAX_TOOLTIP_WIDTH + 20) {
            lbl.setHorizontalAlignment(align);
        } else {
            lbl.setHorizontalAlignment(SwingConstants.LEADING);
        }

        return lbl;
    }
}
