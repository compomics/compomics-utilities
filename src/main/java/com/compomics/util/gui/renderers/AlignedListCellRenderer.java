package com.compomics.util.gui.renderers;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * ListCellRenderer with alignment functionality.
 *
 * @author Harald Barsnes
 */
public class AlignedListCellRenderer extends DefaultListCellRenderer {

    /**
     * Empty default constructor
     */
    public AlignedListCellRenderer() {
        align = 0;
    }

    /**
     * One of the following constants defined in SwingConstants: LEFT, CENTER
     * (the default for image-only labels), RIGHT, LEADING (the default for
     * text-only labels) or TRAILING.
     */
    private final int align;

    /**
     * Creates a new AlignedListCellRenderer
     *
     * @param align SwingConstant: LEFT, CENTER, RIGHT, LEADING or TRAILING.
     */
    public AlignedListCellRenderer(int align) {
        this.align = align;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        JLabel lbl = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        // set the standard horizontal alignment
        lbl.setHorizontalAlignment(align);

        return lbl;
    }
}
